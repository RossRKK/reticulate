package poafs.peer;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

import poafs.exception.ProtocolException;
import poafs.file.FileManager;
import poafs.file.tracking.ITracker;

/**
 * The internal server of a peer.
 * @author rossrkk
 *
 */
public class Server implements Runnable {
	
	/**
	 * The port that this server listens on.
	 */
	private int port;
	
	/**
	 * All of the peers who have connected view this server.
	 */
	private List<IPeer> connectedPeers = new ArrayList<IPeer>();
	
	private FileManager fm;

	private ITracker t;

	public Server(int port, ITracker t, FileManager fm) {
		this.port = port;
		this.fm = fm;
		this.t = t;
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
					
					connectedPeers.add(peer);
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
