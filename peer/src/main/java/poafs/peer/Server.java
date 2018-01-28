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
	 * All of the open requests.
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
				Socket s = ss.accept();
				
				NetworkPeer peer = new NetworkPeer(s, t, fm);
				
				connectedPeers.add(peer);
			}
			
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ProtocolException e) {
			e.printStackTrace();
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
