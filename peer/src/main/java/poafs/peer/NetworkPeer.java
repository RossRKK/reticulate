package poafs.peer;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.Base64;
import java.util.HashMap;
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
import poafs.file.PoafsFile;
import poafs.file.tracking.ITracker;
import poafs.file.tracking.PeerInfo;
import poafs.lib.Reference;
import poafs.util.BindableIO;

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
	 * The bindable IO stream used to communicate between peers.
	 */
	private BindableIO io;
	
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
			
			/*out = new PrintWriter(s.getOutputStream());
			sc = new Scanner(s.getInputStream());*/
			
			io = new BindableIO(s.getInputStream(), s.getOutputStream());
			
			String bindId = io.bind();
			
			io.println("Reticulate 0.1", bindId);
			
			io.println(Application.getPropertiesManager().getPeerId(), bindId);
			//out.flush();
			
			//TODO check that protocol versions are compatible
			String versionDec = io.nextLine(bindId);
			this.id = io.nextLine(bindId);
			
			//register this peer with the tracker
			//TODO allow the port to be variable
			//can't use s.getPort() because the port an outgoing connection leaves on is not the same as the port they are listening on
			t.registerPeer(id, new InetSocketAddress(s.getInetAddress().getHostName(), Reference.port));
			
			io.unbind(bindId);
			
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
				String request = io.nextLine(null);
				
				System.out.println(request);
				
				StringTokenizer tokens = new StringTokenizer(request);
				
				switch (tokens.nextToken())  {
					//someone is trying to fetch a block
					case "fetch":
						fetch(tokens.nextToken());
						break;
					case "send":
						receiveBlock(tokens.nextToken());
						break;
					case "known-peers":
						knownPeers();
						break;
					case "available-files":
						availableFiles();
						break;
				}
				
				//out.flush();
			}
			
			//sc.close();
		} finally {
			try {
				s.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Recieve a pushed block from another peer.
	 * @param fileBlockId The id of the file block bein retriveed (i.e. <file id>:<block index>)
	 */
	private void receiveBlock(String fileBlockId) {
		String bindId = io.bind();
		
		String[] id = fileBlockId.split(":");
		
		try {
			FileBlock block = readBlock(Integer.parseInt(id[1]), bindId);
			
			fm.registerBlock(id[0], block);
		} catch (ArrayIndexOutOfBoundsException | NumberFormatException | ProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		io.unbind(bindId);
	}
	
	/**
	 * Send back a file that a user has fetched.
	 * @param request The file block that they fetched.
	 */
	private void fetch(String request) {
		String bindId = io.bind();
		
		//requests should take the form <file id>:<block index>
		String[] info = request.split(":");
		String fileId = info[0];
		int blockIndex = Integer.parseInt(info[1]);
		
		EncryptedFileBlock block = (EncryptedFileBlock) fm.getFileBlock(fileId, blockIndex);
		
		//out.write(block.getContent());
		io.println(Base64.getEncoder().encodeToString(block.getContent()), bindId);
		
		io.unbind(bindId);
		//out.flush();
	}
	
	/**
	 * Send back a list of known peers and their associated addresses
	 */
	private synchronized void knownPeers() {
		String bindId = io.bind();
		//get all the peers we know about
		Map<String, PeerInfo> peers = t.getPeers();
		
		//tell the remote peer how many entries to expect
		io.println("length " + peers.size(), bindId);
		
		for (Entry<String, PeerInfo> entry:peers.entrySet()) {
			//output the id and address of the peer as "<peer id> <host name>:<port>"
			io.println(entry.getKey() + " " + entry.getValue().getAddr().getHostName() + ":" + entry.getValue().getAddr().getPort(), bindId);
			
			//out.flush();
		}
		
		io.unbind(bindId);
	}
	
	
	/**
	 * Request known peers of the remote peer.
	 */
	@Override
	public synchronized Set<PeerInfo> requestKnownPeers() {
		String bindId = io.bind();
		Set<PeerInfo> peers = new HashSet<PeerInfo>();
		
		io.println("known-peers", bindId);
		//out.flush();
		
		String[] lengthLine = io.nextLine(bindId).split(" ");
		int length = Integer.parseInt(lengthLine[1]);
		
		//read each line that has peer info on it
		for (int i = 0; i < length; i++) {
			String line = io.nextLine(bindId);
			
			StringTokenizer tokens = new StringTokenizer(line, " :");
			//parse the details
			String id = tokens.nextToken();
			String host = tokens.nextToken();
			int port =  tokens.hasMoreTokens() ? Integer.parseInt(tokens.nextToken()) : Reference.port;
			
			//add the peer to the set
			peers.add(new PeerInfo(id, new InetSocketAddress(host, port)));
		}
		
		io.unbind(bindId);
		
		return peers;
	}
	
	/**
	 * Send back a list of available files
	 */
	private synchronized void availableFiles() {
		String bindId = io.bind();
		//get all the peers we know about
		Map<String, PoafsFile> files = fm.getAvailableFiles();
		
		
		//tell the remote peer how many entries to expect
		io.println("length " + files.size(), bindId);
		
		for (Entry<String, PoafsFile> entry:files.entrySet()) {
			//output the id and address of the peer as "<peer id> <host name>:<port>"
			io.println(entry.getKey() + " ", bindId);
			
			//print the block indcies that the local peer has a copy of
			for (Entry<Integer, FileBlock> block:entry.getValue().getBlocks().entrySet()) {
				io.print(block.getKey() + ",", bindId);
			}
			
			//out.flush();
		}
		
		io.unbind(bindId);
	}
	
	/**
	 * Request which files are available from the remote peer.
	 * @return A map containing available file ids and block indicies.
	 */
	@Override
	public synchronized Map<String, List<Integer>> requestAvailableFiles() {
		String bindId = io.bind();
		
		Map<String, List<Integer>> files = new HashMap<String, List<Integer>>();
		
		io.println("available-files", bindId);
		//out.flush();
		
		String[] lengthLine = io.nextLine(bindId).split(" ");
		int length = Integer.parseInt(lengthLine[1]);
		
		//read each line that has peer info on it
		for (int i = 0; i < length; i++) {
			String[] line = io.nextLine(bindId).split(" ");
			
			
			String id = line[0];
			StringTokenizer tokens = new StringTokenizer(line[1], ",");
			
			//parse all of the indicies
			List<Integer> indicies = new ArrayList<Integer>();
			while (tokens.hasMoreTokens()) {
				indicies.add(Integer.parseInt(tokens.nextToken()));
			}
			
			files.put(id, indicies);
		}
		
		io.unbind(bindId);
		
		return files;
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
			String bindId = io.bind();
			String request = "fetch " + fileId + ":" + index;
			
			io.println(request, bindId);
			
			FileBlock block = readBlock(index, bindId);
			
			io.unbind(bindId);
			
			return block;
		} catch (IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error retrieving block from peer " + id);
		}
	}

	/**
	 * Read a block from an input stream.
	 * @return The required block.
	 * @throws ProtocolException 
	 */
	private synchronized FileBlock readBlock(int index, String bindId) throws ProtocolException {
		try {
			//read in the content of the block
			String content64 = io.nextLine(bindId);
			byte[] content = Base64.getDecoder().decode(content64);
			
			//return the relevant block
			return new EncryptedFileBlock(id, content, index, null);
		} catch (IndexOutOfBoundsException | NumberFormatException e) {
			throw new ProtocolException("Error retrieving block from peer " + id);
		}
	}

	/**
	 * Send a file block to the remote peer.
	 * @param fileId The id of the file being sent.
	 * @param block The file block being sent.
	 */
	@Override
	public synchronized void sendBlock(String fileId, FileBlock block) {
		String bindId = io.bind();
		
		//tell the remote peer which block it's about to recieve
		io.println("send " + fileId + ":" + block.getIndex(), bindId);
		
		//send the block content
		io.println(Base64.getEncoder().encodeToString(block.getContent()), bindId);
		
		io.unbind(bindId);
	}

	@Override
	public String getId() {
		return id;
	}
}
