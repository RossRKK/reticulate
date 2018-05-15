package poafs.file.tracking;

import java.io.IOException;
import java.util.Collection;

import poafs.Network;
import poafs.exception.NoValidPeersException;
import poafs.exception.ProtocolException;
import poafs.file.FileBlock;
import poafs.file.FileManager;
import poafs.file.PoafsFile;

/**
 * A class that runs a thread that at a specified interval, updates the state of the network.
 * @author rossrkk
 *
 */
public class Worker implements Runnable {
	
	private static final long INTERVAL = 30000;
	
	private Network net;
	
	private boolean active;
	
	private int redun;
	
	private FileManager fm;
	
	private ITracker t;

	public Worker(Network net, FileManager fm, ITracker t, int redun) {
		super();
		this.net = net;
		this.redun = redun;
		this.t = t;
		this.fm = fm;
		active = true;
	}
	
	public void stop() {
		active = false;
	}


	@Override
	public void run() {
		while (active) {
			//sleep
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			
			
			//attempt to traverse the network
			try {
				net.startTraversal();
			} catch (ProtocolException | IOException e1) {
				e1.printStackTrace();
			}
			
			//TODO ensure that all the local files checksums are up to date
			
			int numPeers = net.listPeers().size();
			
			//we can't have redundancy higher than the number of peers on the network
			ensureRedundancy(redun < numPeers ? redun : numPeers);
		}
	}
	
	/**
	 * Ensure that every file on this peer has sufficient redundancy.
	 * @param r The desired level of redundancy (not including the current peer)
	 */
	private void ensureRedundancy(int r) {
		//for each file this peer has a copy of TODO the user may want to ensure file that there isn't a local copy of are maintained as well
		for (PoafsFile file: fm.getAvailableFiles().values()) {
			for (FileBlock block: file.getBlocks().values()) {
				//get the peers who have a copy of the block
				Collection<String> peerIds = t.findBlock(file.getId(), block.getIndex());
				
				//create redundant copies
				while (peerIds.size() < r) {
					try {
						String id = net.uploadBlockRandomly(file.getId(), fm.getFileBlock(file.getId(), block.getIndex()));
						
						t.registerTransfer(id, file.getId(), block.getIndex());
						
						System.out.println("Created redundant copy of block: " + file.getId() + ":" + block.getIndex());
					} catch (NoValidPeersException e) {
						System.err.println("Insufficient peers to create redundant backup of block: " + file.getId() + ":" + block.getIndex());
						break;
					}
					
					//update the number of peer ids
					peerIds = t.findBlock(file.getId(), block.getIndex());
				}
			}
		}
	}

}