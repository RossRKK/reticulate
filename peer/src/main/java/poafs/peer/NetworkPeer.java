package poafs.peer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import poafs.Application;
import poafs.cryto.HybridDecrypter;
import poafs.exception.KeyException;
import poafs.exception.ProtocolException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;
import poafs.file.FileManager;
import poafs.file.tracking.PeerInfo;

/**
 * A peer that is somewhere else on the network.
 * @author rossrkk
 *
 */
public class NetworkPeer implements IPeer {

	/**
	 * The socket that this peer speaks over.
	 */
	private Socket s;
	
	/**
	 * The output stream to the peer.
	 */
	private PrintWriter out;
	
	/**
	 * The scanner that reads input from the other peer.
	 */
	private Scanner sc;
	
	/**
	 * The id of the peer.
	 */
	private String id;
	
	/**
	 * The local peer's file manager.
	 */
	private FileManager fm;
	
	/**
	 * Open a connection to a new peer.
	 * @param s The socket to connect through.
	 * @throws IOException
	 */
	public NetworkPeer(Socket s, FileManager fm) throws ProtocolException {
		try {
			this.s = s;
			this.fm = fm;
			
			//TODO register the connected peer with the tracker
			
			out = new PrintWriter(s.getOutputStream());
			sc = new Scanner(s.getInputStream());
			
			out.println("Reticulate 0.1");
			out.println(Application.getPropertiesManager().getPeerId());
			
			out.flush();
			
			//TODO check that protocol versions are compatible
			String versionDec = sc.nextLine();
	
			this.id = sc.nextLine();
			
			//start the peer listening in a new thread
			new Thread(this).start();
		} catch (IOException | IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error opening connection to peer " + id);
		}
	}
	
	/**
	 * Handle incoming requests.
	 */
	@Override
	public void run() {
		try {
			
			//read all requests for file segments
			while (!s.isClosed()) {
				String request = sc.nextLine();
				
				//requests should take the form <file id>:<block index>
				String[] info = request.split(":");
				String fileId = info[0];
				int blockIndex = Integer.parseInt(info[1]);
				
				EncryptedFileBlock block = (EncryptedFileBlock) fm.getFileBlock(fileId, blockIndex);
				
				//out.write(block.getContent());
				out.println(Base64.getEncoder().encodeToString(block.getContent()));
				
				out.flush();
			}
			
			sc.close();
		} finally {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	/**
	 * Request a block from the connected peer.
	 * @param fileId The id of the file you want.
	 * @param index The index of the block you want.
	 * 
	 * @return The relevant block.
	 * @throws ProtocolException 
	 */
	@Override
	public synchronized FileBlock requestBlock(String fileId, int index) throws ProtocolException {
		try {
			String request = fileId + ":" + index;
			
			out.println(request);
			
			out.flush();
			
			return readResponse(index);
		} catch (IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error retrieving block from peer " + id);
		}
	}

	/**
	 * Read a block from an input stream.
	 * @return The required block.
	 * @throws ProtocolException 
	 */
	private synchronized FileBlock readResponse(int index) throws ProtocolException {
		try {
			//read in the content of the block
			String content64 = sc.nextLine();
			byte[] content = Base64.getDecoder().decode(content64);
			
			//return the relevant block
			return new EncryptedFileBlock(id, content, index, null);
		} catch (IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error retrieving block from peer " + id);
		}
	}


	@Override
	public synchronized void sendBlock(String fileId, FileBlock block) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public synchronized String getId() {
		return id;
	}

	@Override
	public List<PeerInfo> getKnownPeers() {
		// TODO Auto-generated method stub
		return null;
	}
}
