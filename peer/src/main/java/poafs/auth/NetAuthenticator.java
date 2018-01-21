package poafs.auth;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;
import java.util.ArrayList;
import java.util.List;

import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.SSLSocketFactory;

import poafs.Application;
import poafs.cryto.HybridDecrypter;
import poafs.cryto.HybridEncrypter;
import poafs.cryto.IDecrypter;
import poafs.cryto.IEncrypter;
import poafs.exception.KeyException;
import poafs.exception.ProtocolException;
import poafs.file.FileMeta;
import poafs.file.PoafsFile;
import poafs.lib.Reference;

public class NetAuthenticator implements IAuthenticator {
	
	private Socket s;
	
	private BufferedInputStream in;
	
	private PrintWriter out;
	
	private String version;
	
	public NetAuthenticator(String host, int port, boolean ssl) throws ProtocolException {
		try {
			if (ssl) {
				//open an ssl connection to the server
				SSLSocketFactory factory = (SSLSocketFactory) SSLSocketFactory.getDefault();
		        s = factory.createSocket(host, port);
			} else {
				s = new Socket(host, port);
			}
			
	        //create the input and output streams
	        in = new BufferedInputStream(s.getInputStream());
	        out = new PrintWriter(s.getOutputStream());
	        
	        out.println("POAFS Version 0.1");
			out.println(Application.getPropertiesManager().getPeerId());
			
			out.flush();
			
			version = readLine();
		} catch (IOException e) {
			throw new ProtocolException("Error estabilishing connection to server.");
		}
	}
	
	/**
	 * Utility method to read a line from the input.
	 * @return The line from the input
	 * @throws IOException
	 */
	private synchronized String readLine() throws IOException {
		//the input directly from the input stream
		int input;
		//the line that we want to return
		String line = "";
		
		//loop until the character is a new line
		while ((input = in.read()) != '\n') {
			//append the character to the line
			char character = new Character((char) input).charValue();
			line += character;
		}
		
		//tolerate carriage returns
		if (line.endsWith("\r")) {
			line = line.substring(0, line.length() - 1);
		}
		
		//return the line
		return line;
	}

	@Override
	public synchronized IDecrypter getKeyForPeer(String peerId) throws KeyException, ProtocolException {
		try {
			out.println("private-key " + peerId);
			out.flush();
			
			String response = readLine();
			
			int length = Integer.parseInt(response.split(":")[1]);
			
			byte[] key = new byte[length];
			in.read(key);
			
			try {
				return new HybridDecrypter(KeyFactory.getInstance("RSA").generatePrivate(new PKCS8EncodedKeySpec(key)));
			} catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
				throw new KeyException();
			}
		} catch (IOException | IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error retrieving key for peer " + peerId);
		}
	}
	
	/**
	 * Get the hostname this peer can be accessed on.
	 * @param peerId The id of the peer.
	 * @return The hostname of the peer.
	 * @throws IOException 
	 */
	public synchronized InetSocketAddress getHostForPeer(String peerId) throws ProtocolException {
		try {
			out.println("host " + peerId);
			
			out.flush();
			
			String[] tuple = readLine().split(":");
			
			String host = tuple[0];
			int port = Integer.parseInt(tuple[1]);
			
			return new InetSocketAddress(host, port);
		} catch (IOException | IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error retrieving host for peer " + peerId);
		}
	}
	
	@Override
	public synchronized boolean authoriseUser(String userName, String password) throws ProtocolException {
		try {
			out.println("login " + userName);
			out.println(password);
			
			out.flush();
			
			String auth;
			auth = readLine();
			return Boolean.parseBoolean(auth);
		} catch (IOException | IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error logging in");
		}
	}

	@Override
	public synchronized List<FileMeta> listFiles() throws ProtocolException {
		try {
			out.println("list-files *");
			
			out.flush();
			
			String lengthStr = readLine();
			int length = Integer.parseInt(lengthStr.split(":")[1]);
			
			List<FileMeta> files = new ArrayList<FileMeta>();
			for (int i = 0; i < length; i++) {
				String[] info = readLine().split(":");
				int fileLength;
				try {
					fileLength = Integer.parseInt(info[2]);
				} catch (NumberFormatException | IndexOutOfBoundsException e) {
					fileLength = -1;
				}
				files.add(new FileMeta(info[0], info[1], fileLength));
			}
			
			return files;
		} catch (IOException | IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error listing files");
		}
	}

	@Override
	public synchronized FileMeta getInfoForFile(String fileId) throws ProtocolException {
		try {
			out.println("file-info " + fileId);
			out.flush();
			System.out.println("Sent info request");
			
			String id = readLine().split(":")[1];
			
			String name = readLine().split(":")[1];
			
			int length = Integer.parseInt(readLine().split(":")[1]);
			
			return new FileMeta(id, name, length);
		} catch (IOException | IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error retrieving meta data for file " + fileId);
		}
	}

	@Override
	public synchronized List<String> findBlock(String fileId, int blockIndex) throws ProtocolException {
		try {
			out.println("find-block " + fileId + ":" + blockIndex);
			out.flush();
			
			String lengthStr = readLine();
			int length = Integer.parseInt(lengthStr.split(":")[1]);
			
			List<String> peers = new ArrayList<String>();
			
			for (int i = 0; i < length; i++) {
				peers.add(readLine());
			}
			
			return peers;
		} catch (IOException | IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error retrieving peers for file " + fileId);
		}
	}

	@Override
	public synchronized boolean registerFile(PoafsFile file, String fileName) throws ProtocolException {
		try {
			out.println("register-file " + file.getId());
			out.println("length:" + file.getNumBlocks());
			out.println(fileName);
			
			out.flush();
			String success = readLine();
			
			return Boolean.parseBoolean(success);
		} catch (IOException | IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error registering file" + fileName);
		}
	}

	@Override
	public synchronized IEncrypter registerPeer() throws ProtocolException, KeyException {
		
		
		try {
			out.println("register-peer " + Reference.DEFAULT_PORT);
			out.flush();
			
			String lengthStr = readLine();
			try {
				int length = Integer.parseInt(lengthStr.split(":")[1]);
				
				byte[] keyBytes = new byte[length];
			
			
				in.read(keyBytes);
				
				
				X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
			    KeyFactory kf = KeyFactory.getInstance("RSA");
			    return new HybridEncrypter(kf.generatePublic(spec));
			} catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
				throw new KeyException();
			}
		} catch (IOException | IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error registering peer");
		}
	}

	@Override
	public synchronized void registerTransfer(String fileId, int index) {
		out.println("register-transfer " + fileId + ":" + index);
	}
}
