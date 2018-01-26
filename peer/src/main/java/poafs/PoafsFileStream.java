package poafs;

import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Collection;
import java.util.HashMap;
import java.util.Random;

import poafs.auth.IAuthenticator;
import poafs.cryto.IDecrypter;
import poafs.exception.KeyException;
import poafs.exception.NoValidPeersException;
import poafs.exception.ProtocolException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;
import poafs.file.FileManager;
import poafs.file.FileMeta;
import poafs.file.tracking.ITracker;
import poafs.peer.IPeer;
import poafs.peer.NetworkPeer;

/**
 * An input stream that represents a file that exists on the network.
 * @author rkk2
 *
 */
public class PoafsFileStream extends InputStream {
	
	/**
	 * The authenticator being used by this peer.
	 */
	private IAuthenticator auth;
	
	/**
	 * The file that this stream reads from.
	 */
	private String fileId;
	
	/**
	 * The block that is currently being read from.
	 */
	private int nextFetchIndex = 0;
	
	/**
	 * The block that is currently being read from.
	 */
	private int currentReadBlockIndex = 0;
	
	/**
	 * The current position inside the block we are reading from.
	 */
	private int currentReadIndex = 0;
	
	/**
	 * The number of file blocks should be fetched before they are required.
	 */
	private int preloadDistance;
	
	/**
	 * The internal block fetchers.
	 */
	private HashMap<Integer, BlockFetcher> fetchers =  new HashMap<Integer, BlockFetcher>();
	
	/**
	 * The files meta data.
	 */
	private FileMeta info;
	
	/**
	 * The poafs file that is being loaded.
	 */
	private HashMap<Integer, FileBlock> fileContent = new HashMap<Integer, FileBlock>();
	
	/**
	 * The decrypter to use to decrypt the file stream.
	 */
	private IDecrypter decrypter;
	
	private ITracker tracker;
	
	/**
	 * The local peers file manager.
	 */
	private FileManager fm;
	
	private boolean ready = false;
	
	public PoafsFileStream(String fileId, int preloadDistance, IAuthenticator auth, IDecrypter decrypter, ITracker tracker, FileManager fm) {
		this.auth = auth;
		info = auth.getInfoForFile(fileId);
		this.fileId = fileId;
		this.preloadDistance = Math.min(preloadDistance, info.getLength());
		
		this.decrypter = decrypter;
		this.tracker = tracker;
		this.fm = fm;
		
		initialFetch();
	}

	/**
	 * Start fetching data.
	 */
	private void initialFetch() {
		for (int i = 0; i < preloadDistance; i++) {
			startFetcher();
		}
	}
	
	/**
	 * Step to the next block.
	 * @return Whether there was a next block to step to.
	 */
	private boolean stepToNextBlock() {
		//check that the next block exists
		if (currentReadBlockIndex + 1 < info.getLength()) {
			//increment the counters
			currentReadBlockIndex ++;
			currentReadIndex = 0;
			
			//keep the fetchers up to date
			if (nextFetchIndex < info.getLength()) {
				startFetcher();
			}

			return true;
		} else {
			return false;
		}
	}
	
	/**
	 * Start up a new block fetcher.
	 */
	private void startFetcher() {
		//start up a new block fetcher
		BlockFetcher bf = new BlockFetcher(fileId, nextFetchIndex, auth, fileContent, decrypter, tracker, fm);
		fetchers.put(nextFetchIndex, bf);
		new Thread(bf).start();
		nextFetchIndex++;
	}

	@Override
	public int read() throws IOException {
		try {
			//wait until the fetcher has finished getting the block before allowing the read
			BlockFetcher fetcher = fetchers.get(currentReadBlockIndex);
			
			synchronized (fetcher) {
				while (fileContent.get(currentReadBlockIndex) == null) {
					fetcher.wait();
				}
				
				int output = fileContent.get(currentReadBlockIndex).getContent()[currentReadIndex];
				//move the read head
				currentReadIndex ++;
				
				return output + 128;
			}
		} catch (IndexOutOfBoundsException e) {
			//move to the next block
			if (stepToNextBlock()) {
				return read();
			} else {
				return -1;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
			return -1;
		} catch (NullPointerException e) {
			System.out.println(currentReadBlockIndex);
			System.out.println(nextFetchIndex);
			
			return -1;
		}
	}
}
/**
 * A class that fetches a block and decrypts it.
 * @author rkk2
 *
 */
class  BlockFetcher implements Runnable {
	
	private static final int ATTEMPT_LIMIT = 3;
	
	private IAuthenticator auth;
	
	private String fileId;
	
	private int index;
	
	private int attempt = 0;
	
	private HashMap<Integer, FileBlock> fileContent;
	
	private IDecrypter d;
	
	private ITracker t;
	
	private FileManager fm;
	
	BlockFetcher(String fileId, int index, IAuthenticator auth, HashMap<Integer, FileBlock> fileContent, IDecrypter d, ITracker tracker, FileManager fm) {
		this.auth = auth;
		this.fileId = fileId;
		this.index = index;
		this.fileContent = fileContent;
		this.d = d;
		this.fm = fm;
		t = tracker;
	}

	@Override
	public void run() {
		System.out.println("Fetching " + fileId + ":" + index);
		try {
			FileBlock block = decryptBlock(getBlock(fileId, index));
			
			
			if (block == null) {
				if (attempt < ATTEMPT_LIMIT) {
					System.out.println("Error reading: " + fileId + ":" + index + " retrying");
					attempt++;
					
					run();
				} else {
					System.out.println("Failed to read: " + fileId + ":" + index + " after " + attempt + " attempts");
					fileContent.put(index, block);
				}
			} else {
				fileContent.put(index, block);
			}
			synchronized (this) {
				this.notifyAll();
			}
		} catch (ProtocolException | NoValidPeersException e) {
			System.err.println("Error fetching block: " + fileId + ":" + index);
		}
	}
	
	/**
	 * Decrypt a file block.
	 * @param block The file block to be decrypted.
	 * @return The decrypted file block.
	 */
	private FileBlock decryptBlock(FileBlock block) {
		long startTime = System.currentTimeMillis();
		if (block instanceof EncryptedFileBlock) {
			String peerId = block.getOriginPeerId();

			((EncryptedFileBlock)block).setWrappedKey(auth.getKeyForFile(fileId));
			
			try {
				long time = System.currentTimeMillis() - startTime;
				
				FileBlock out = d.decrypt((EncryptedFileBlock)block);
				
				System.out.println("Decrypt for " + fileId + ":" + index + " took " + 
						time + "ms " + ((double)time)/out.getContent().length + "B/ms");
				
				return out;
			} catch (KeyException e) {
				System.out.println("Error decrypting " + fileId + ":" + index);
				System.out.println(block.getContent().length);
				e.printStackTrace();
				return null;
			}
		} else {
			return block;
		}
	}
	
	/**
	 * Get a file block off of a peer that has it.
	 * @param fileId The id of the file.
	 * @param block The index of the block.
	 * @return The file block.
	 * @throws NoValidPeersException 
	 * @throws IOException 
	 */
	private FileBlock getBlock(String fileId, int block) throws ProtocolException, NoValidPeersException {
		long startTime = System.currentTimeMillis();
		
		Random r = new Random();
		Collection<String> peerIds = t.findBlock(fileId, block);
		String peerId = null;
		
		//loop until we get the block or run out of peers
		while (!peerIds.isEmpty()) {
			try {
				//choose a random peer
				peerId = peerIds.toArray(new String[peerIds.size()])[r.nextInt(peerIds.size())];
				
				InetSocketAddress addr = t.getHostForPeer(peerId);
				
				//only go to the network if there isn't a local copy
				if (addr.getHostName() != "localhost") {
					
					//get the block off of the peer
					IPeer peer = new NetworkPeer(new Socket(addr.getHostName(), addr.getPort()), fm);
					
					FileBlock out = peer.requestBlock(fileId, block);
					
					long time = System.currentTimeMillis() - startTime;
					
					System.out.println("Fetch for " + fileId + ":" + index + " took " + 
							time + "ms " + ((double)time)/out.getContent().length + "B/ms");
					
					
					return out;
				} else {
					return fm.getFileBlock(fileId, block);
				}
			} catch (IOException e) {
				System.err.println(peerId + " was unreachable");
				peerIds.remove(peerId);
				
				if (peerIds.size() == 0) {
					break;
				}
			} catch (ProtocolException e) {
				System.err.println(e.getMessage());
				peerIds.remove(peerId);
				
				if (peerIds.size() == 0) {
					break;
				}
			}
		}
		throw new NoValidPeersException();
	}
}
