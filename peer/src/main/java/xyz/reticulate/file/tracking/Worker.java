package xyz.reticulate.file.tracking;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.Collection;
import java.util.logging.Level;
import java.util.logging.Logger;

import xyz.reticulate.Network;
import xyz.reticulate.auth.IAuthenticator;
import xyz.reticulate.exception.NoValidPeersException;
import xyz.reticulate.exception.ProtocolException;
import xyz.reticulate.file.FileBlock;
import xyz.reticulate.file.FileManager;
import xyz.reticulate.file.ReticulateFile;

/**
 * A class that runs a thread that at a specified interval, updates the state of the network.
 * @author rossrkk
 *
 */
public class Worker implements Runnable {
	
	private static final long INTERVAL = 30000;
	
	private Network net;
	
	private Logger log = Logger.getLogger(Worker.class.getSimpleName());
	
	private boolean active;
	
	private int redun;
	
	private FileManager fm;
	
	private ITracker t;
	
	private IAuthenticator auth;
	
	private Thread thread;

	public Worker(Network net, IAuthenticator auth, FileManager fm, ITracker t, int redun) {
		super();
		log.setLevel(Level.INFO);
		this.net = net;
		this.redun = redun;
		this.t = t;
		this.fm = fm;
		this.auth = auth;
		active = true;
		
		thread = new Thread(this);
		thread.start();
	}
	
	public void stop() {
		active = false;
		thread.interrupt();
	}


	@Override
	public void run() {
		while (active) {
			//sleep
			try {
				Thread.sleep(INTERVAL);
			} catch (InterruptedException e) {
				break;
			}
			
			
			//attempt to traverse the network
			try {
				net.startTraversal();
			} catch (ProtocolException | IOException e1) {
				e1.printStackTrace();
			}
			
			
			int numPeers = net.listPeers().size();
			
			//we can't have redundancy higher than the number of peers on the network
			loopOverFiles(redun < numPeers ? redun : numPeers);
		}
	}
	
	/**
	 * Ensure that every file on this peer has sufficient redundancy.
	 * @param r The desired level of redundancy (not including the current peer)
	 */
	private void loopOverFiles(int r) {
		//for each file this peer has a copy of TODO the user may want to ensure file that there isn't a local copy of are maintained as well
		for (ReticulateFile file: fm.getAvailableFiles().values()) {
			for (FileBlock block: file.getBlocks().values()) {
				
				//check that the checksum is up to date
				ensureCorrectChecksum(file, fm.getFileBlock(file.getId(), block.getIndex()));
				
				//ensure that redundant copies of this file block exist
				ensureRedundancy(file, block, r);
			}
		}
	}
	
	/**
	 * Check that a file block is up to date (i.e. has the correct checksum)
	 * @param file The file being checked.
	 * @param block The specific block being checked.
	 */
	private void ensureCorrectChecksum(ReticulateFile file, FileBlock block) {
		try {
			if (!auth.compareCheckSum(file.getId(), block.getIndex(), block.getChecksum())) {
				log.log(Level.FINE,"File Block " + file.getId() + ":" + block.getIndex() + " is out of date");
				//checksums don't match, take corrective actions
				try {
					net.downloadBlock(file.getId(), block.getIndex());

					log.log(Level.FINE,"File Block " + file.getId() + ":" + block.getIndex() + " updated.");
				} catch (NoValidPeersException e) {
					log.log(Level.WARNING, "File Block " + file.getId() + ":" + block.getIndex() + " couldn't be updated, no valid peers.");
				}
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Ensure that a specific file block has sufficient redundant copies
	 * @param file The file the block is part of
	 * @param block The block we're considering.
	 * @param r How many redundant copies there should be.
	 */
	private void ensureRedundancy(ReticulateFile file, FileBlock block, int r) {
		//get the peers who have a copy of the block
		Collection<String> peerIds = t.findBlock(file.getId(), block.getIndex());
		
		//create redundant copies
		while (peerIds.size() < r) {
			try {
				String id = net.uploadBlockRandomly(file.getId(), fm.getFileBlock(file.getId(), block.getIndex()));
				
				t.registerTransfer(id, file.getId(), block.getIndex());
				
				log.log(Level.FINE, "Created redundant copy of block: " + file.getId() + ":" + block.getIndex());
			} catch (NoValidPeersException e) {
				log.log(Level.WARNING, "Insufficient peers to create redundant backup of block: " + file.getId() + ":" + block.getIndex());
				break;
			}
			
			//update the number of peer ids
			peerIds = t.findBlock(file.getId(), block.getIndex());
		}
	}

}