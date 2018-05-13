package poafs.peer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;

import poafs.exception.ProtocolException;
import poafs.file.FileManager;
import poafs.file.tracking.ITracker;

/**
 * The internal server of a peer.
 * @author rossrkk
 *
 */
public class PeerManager implements Runnable {
	
	/**
	 * The port that this server listens on.
	 */
	private int port;
	
	/**
	 * All of the peers who have connected view this server.
	 */
	private HashMap<String, IPeer> connectedPeers = new HashMap<String, IPeer>();
	
	private FileManager fm;

	private ITracker t;

	public PeerManager(int port, ITracker t, FileManager fm) {
		this.port = port;
		this.fm = fm;
		this.t = t;
	}
	
	/**
	 * Open a connection to a peer. Reuse an existing one if possible.
	 * @param peerId The id of the peer to connect to.
	 * @return That peers object.
	 * @throws UnknownHostException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	public IPeer openConnection(String peerId) throws UnknownHostException, ProtocolException, IOException {
		IPeer connected = connectedPeers.get(peerId);
		
		if (connected != null) {
			return connected;
		} else {
			InetSocketAddress addr = t.getHostForPeer(peerId);
			
			NetworkPeer p =  new NetworkPeer(new Socket(addr.getHostName(), addr.getPort()), t, fm);
			
			connectedPeers.put(peerId, p);
			
			return p;
		}
	}
	
	@Override
	public void run() {
		ServerSocket ss = null;
		try {
			ss = new ServerSocket(port);
			
			while (!ss.isClosed()) {
				Socket s;
				try {
					s = ss.accept();
				
				
					System.out.println("Recieved remote connection from: " + s.getInetAddress().getHostName() + ":" + s.getPort());
					
					NetworkPeer peer = new NetworkPeer(s, t, fm);
					
					connectedPeers.put(peer.getId(), peer);
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ProtocolException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			
		} catch (IOException e) {
			System.err.println("Error starting peer listening server");
		} finally {
			if (ss != null) {
				try {
					ss.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}

}
