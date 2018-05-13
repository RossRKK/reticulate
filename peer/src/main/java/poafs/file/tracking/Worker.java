package poafs.file.tracking;

import java.io.IOException;

import poafs.Network;
import poafs.exception.ProtocolException;

/**
 * A class that runs a thread that at a specified interval, updates the state of the network.
 * @author rossrkk
 *
 */
public class Worker implements Runnable {
	
	private static final long INTERVAL = 600000;
	
	private Network net;
	
	private boolean active;

	public Worker(Network net) {
		super();
		this.net = net;
		active = true;
	}
	
	public void stop() {
		active = false;
	}


	@Override
	public void run() {
		while (active) {
			//attempt to traverse the network
			try {
				net.startTraversal();
			} catch (ProtocolException | IOException e1) {
				e1.printStackTrace();
			}
			
			//sleep
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}

}
