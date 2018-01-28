package poafs.peer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Base64;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Scanner;
import java.util.Set;
import java.util.StringTokenizer;

import poafs.Application;
import poafs.exception.ProtocolException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;
import poafs.file.FileManager;
import poafs.file.tracking.ITracker;
import poafs.file.tracking.PeerInfo;
import poafs.lib.Reference;

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
	 * The local tracker object.
	 */
	private ITracker t;
	
	/**
	 * Open a connection to a new peer.
	 * @param s The socket to connect through.
	 * @throws IOException
	 */
	public NetworkPeer(Socket s, ITracker t, FileManager fm) throws ProtocolException {
		try {
			this.s = s;
			this.fm = fm;
			this.t = t;
			
			out = new PrintWriter(s.getOutputStream());
			sc = new Scanner(s.getInputStream());
			
			out.println("Reticulate 0.1");
			out.println(Application.getPropertiesManager().getPeerId());
			
			out.flush();
			
			
			//TODO check that protocol versions are compatible
			String versionDec = sc.nextLine();
	
			this.id = sc.nextLine();
			//register this peer with the tracker
			t.registerPeer(id, new InetSocketAddress(s.getInetAddress().getHostName(), s.getPort()));
			
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
				
				StringTokenizer tokens = new StringTokenizer(request);
				
				switch (tokens.nextToken())  {
					//someone is trying to fetch a block
					case "fetch":
						fetch(tokens.nextToken());
						break;
					case "known-peers":
						knownPeers();
						break;
				}
				
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
	 * Send back a file that a user has fetched.
	 * @param request The file block that they fetched.
	 */
	private void fetch(String request) {
		//requests should take the form <file id>:<block index>
		String[] info = request.split(":");
		String fileId = info[0];
		int blockIndex = Integer.parseInt(info[1]);
		
		EncryptedFileBlock block = (EncryptedFileBlock) fm.getFileBlock(fileId, blockIndex);
		
		//out.write(block.getContent());
		out.println(Base64.getEncoder().encodeToString(block.getContent()));
		
		out.flush();
	}
	
	/**
	 * Send back a list of known peers and their associated file blocks
	 */
	private synchronized void knownPeers() {
		//get all the peers we know about
		Map<String, PeerInfo> peers = t.getPeers();
		
		//tell the remote peer how many entries to expect
		out.println("length " + peers.size());
		
		for (Entry<String, PeerInfo> entry:peers.entrySet()) {
			//output the id and address of the peer as "<peer id> <host name>:<port>"
			out.println(entry.getKey() + " " + entry.getValue().getAddr().getHostName() + ":" + entry.getValue().getAddr().getPort());
			
			out.flush();
		}
	}
	
	/**
	 * Request known peers of the remote peer.
	 */
	public synchronized Set<PeerInfo> requestKnownPeers() {
		Set<PeerInfo> peers = new HashSet<PeerInfo>();
		
		out.println("known-peers");
		out.flush();
		
		String[] lengthLine = sc.nextLine().split(" ");
		int length = Integer.parseInt(lengthLine[1]);
		
		//read each line that has peer info on it
		for (int i = 0; i < length; i++) {
			String line = sc.nextLine();
			
			StringTokenizer tokens = new StringTokenizer(line, " :");
			//parse the details
			String id = tokens.nextToken();
			String host = tokens.nextToken();
			int port =  tokens.hasMoreTokens() ? Integer.parseInt(tokens.nextToken()) : Reference.DEFAULT_PORT;
			
			//add the peer to the set
			peers.add(new PeerInfo(id, new InetSocketAddress(host, port)));
		}
		
		return peers;
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
	public String getId() {
		return id;
	}

	@Override
	public List<PeerInfo> getKnownPeers() {
		// TODO Auto-generated method stub
		return null;
	}
}
