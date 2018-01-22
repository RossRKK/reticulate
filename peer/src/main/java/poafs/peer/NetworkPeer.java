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
import java.util.Scanner;

import poafs.Application;
import poafs.cryto.HybridDecrypter;
import poafs.exception.KeyException;
import poafs.exception.ProtocolException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;

/**
 * A peer that is somewhere else on the network.
 * @author rossrkk
 *
 */
public class NetworkPeer implements IPeer {

	private String host;
	
	private int port;
	
	private Socket s;
	
	private PrintWriter out;
	
	private Scanner sc;
	
	private String id;

	public NetworkPeer(String id, InetSocketAddress addr) {
		this.host = addr.getHostName();
		this.port = addr.getPort();
		this.id = id;
	}
	

	@Override
	public synchronized void openConnection() throws UnknownHostException, ProtocolException {
		try {
			s = new Socket(host, port);
			
			out = new PrintWriter(s.getOutputStream());		
			
			//in = new BufferedInputStream(s.getInputStream());
			sc = new Scanner(s.getInputStream());
			
			//print some headers
			out.println("POAFS Version 0.1");
			out.println(Application.getPropertiesManager().getPeerId());
			
			String versionDec = sc.nextLine();

			String peerId = sc.nextLine();
		} catch (IOException | IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error opening connection to peer " + id);
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
			//TODO get the wrapped key from the ethereum network
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
}
