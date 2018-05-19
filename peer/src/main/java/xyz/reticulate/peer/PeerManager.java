package xyz.reticulate.peer;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map.Entry;

import xyz.reticulate.exception.ProtocolException;
import xyz.reticulate.file.FileManager;
import xyz.reticulate.file.tracking.ITracker;
import xyz.reticulate.lib.Reference;

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
	 * @return That peers object, null if the connection failed.
	 * @throws UnknownHostException
	 * @throws ProtocolException
	 * @throws IOException
	 */
	public IPeer openConnection(String peerId) throws UnknownHostException, ProtocolException, IOException {
		try {
			IPeer connected = connectedPeers.get(peerId);
			
			if (connected != null) {
				return connected;
			} else {
				InetSocketAddress addr = t.getHostForPeer(peerId);
				
				if (addr != null) {
					
					//limit socket connection timeout
					Socket s = new Socket();
					s.connect(addr, 1000);
					
					NetworkPeer p =  new NetworkPeer(s, t, fm, this);
					
					connectedPeers.put(peerId, p);
					
					return p;
				} else {
					return null;
				}
			}
		} catch (ProtocolException | IOException e) {
			System.err.println("Failed to connect to: " + peerId);
			//remove the peer from the tracker
			t.removePeer(peerId);
			return null;
		}
	}
	
	/**
	 * Handle a peer disconnecting.
	 * @param id The id of the peer who disconnected.
	 */
	void onDisconnect(String id) {
		connectedPeers.remove(id);
	}
	
	public void closeConnections() {
		for (Entry<String, IPeer> e:connectedPeers.entrySet()) {
			e.getValue().disconnect();
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
					
					NetworkPeer peer = new NetworkPeer(s, t, fm, this);
					
					connectedPeers.put(peer.getId(), peer);
					
					t.registerPeer(peer.getId(), new InetSocketAddress(s.getInetAddress().getHostName(), Reference.DEFAULT_PORT));
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
