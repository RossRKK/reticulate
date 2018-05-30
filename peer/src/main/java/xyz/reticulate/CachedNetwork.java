package xyz.reticulate;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;

import xyz.reticulate.exception.KeyException;
import xyz.reticulate.exception.NoValidPeersException;
import xyz.reticulate.exception.ProtocolException;
import xyz.reticulate.lib.Reference;

/**
 * This is a class that provides all the same functionality as the network class but will report pending operations as completed.
 * This is supposed to streamline the user experience
 * @author rossrkk
 *
 */
public class CachedNetwork extends Network {
	
	private HashMap<String, FileUpdater> fileUpdaters = new HashMap<String, FileUpdater>();
	
	private HashMap<String, byte[]> cache = new HashMap<String, byte[]>();
	
	public CachedNetwork(Credentials creds, String contractAddress) throws ProtocolException, IOException, CipherException {
		super(creds, contractAddress);
	}
	
	@Override
	public synchronized void registerFile(String id, byte[] bytes) throws NoSuchAlgorithmException, ProtocolException, KeyException, NoSuchAlgorithmException, IOException, NoValidPeersException {
		cache.put(id, bytes);
		
		long startTime = System.currentTimeMillis();
		
		//create a new updater if necessary
		boolean starting = !fileUpdaters.containsKey(id);
		if (starting) {
			fileUpdaters.put(id, new FileUpdater(id, fileUpdaters));
		}

		Runnable transaction = () -> {
			//kick off the registration
			try {
				super.registerFile(id, bytes);
			} catch (NoSuchAlgorithmException | ProtocolException | KeyException | IOException
					| NoValidPeersException e) {
				e.printStackTrace();
			}
			
			//remove this from the cache, it may change later
			cache.remove(id);
			
			log.info("Finished register operation for " + id + " it took " + (System.currentTimeMillis() - startTime) + "ms");
		};
		
		fileUpdaters.get(id).addTransaction(transaction);
		
		//start the updater if its new
		if (starting) {
			new Thread(fileUpdaters.get(id)).start();
		}
	}
	
	@Override
	public void updateFileContent(String fileId, byte[] bytes) throws KeyException, NoSuchAlgorithmException, NoValidPeersException {
		//predict whether the operation will succeed
		if (auth.getAccessLevel(fileId, creds.getAddress()) >= Reference.WRITE) {
			cache.put(fileId, bytes);
			long startTime = System.currentTimeMillis();
			
			//create a new updater if necessary
			boolean starting = !fileUpdaters.containsKey(fileId);
			if (starting) {
				fileUpdaters.put(fileId, new FileUpdater(fileId, fileUpdaters));
			}
	
			Runnable transaction = () -> {
				//kick off the registration
				try {
					super.updateFileContent(fileId, bytes);
				} catch (NoSuchAlgorithmException | KeyException 
						| NoValidPeersException e) {
					e.printStackTrace();
				}
				
				//remove this from the cache, it may change later
				cache.remove(fileId);
				
				log.info("Finished write operation for " + fileId + " it took " + (System.currentTimeMillis() - startTime) + "ms");
			};
	
			fileUpdaters.get(fileId).addTransaction(transaction);
			
			//start the updater if its new
			if (starting) {
				new Thread(fileUpdaters.get(fileId)).start();
			}
		}
	}
	
	@Override
	public InputStream fetchFile(String fileId) {
		if (cache.containsKey(fileId)) {
			return new ByteStream(cache.get(fileId));
		} else {
			return super.fetchFile(fileId);
		}
	}
	
	@Override
	public boolean share(String fileId, String userAddress, byte[] publicKey, int accessLevel) throws KeyException {
		//TODO
		return super.share(fileId, userAddress, publicKey, accessLevel);
	}
	
	@Override
	public boolean revokeShare(String fileId, String userAddress) {
		//TODO
		return super.revokeShare(fileId, userAddress);
	}
	
	@Override
	public boolean modifyAccessLevel(String fileId, String userAddress, int accessLevel) {
		//TODO
		return super.modifyAccessLevel(fileId, userAddress, accessLevel);
	}
	
	@Override
	public boolean removeFile(String fileId) {
		//TODO
		return super.removeFile(fileId);
	}
	
	@Override
	public void shutdown() {
		//TODO ensure that all write operations complete before shutting down
		super.shutdown();
	}

}
class ByteStream extends InputStream {
	
	private byte[] bytes;
	
	private int index = 0;
	
	public ByteStream(byte[] bytes) {
		this.bytes = bytes;
	}

	@Override
	public int read() throws IOException {
		if (index < bytes.length) {
			return bytes[index++] + 128;
		} else {
			return -1;
		}
	}
	
}
class FileUpdater implements Runnable {
	
	private HashMap<String, FileUpdater> fileUpdaters;
	private String fileId;
	
	public FileUpdater(String fileId, HashMap<String, FileUpdater> fileUpdaters) {
		this.fileId = fileId;
		this.fileUpdaters = fileUpdaters;
	}
	
	private BlockingQueue<Runnable> transactions = new LinkedBlockingQueue<Runnable>();
	
	public synchronized void addTransaction(Runnable transaction) {
		try {
			transactions.put(transaction);
		} catch (InterruptedException e) {
		}
	}
	
	@Override
	public void run() {
		while (!transactions.isEmpty()) {
			try {
				Runnable t = transactions.take();
				System.out.println(fileId + " has a transaction. Starting execution.");
				t.run();
			} catch (InterruptedException e) {
			}
		}
		//remove the reference to this file updater so that it can be garbage collected
		fileUpdaters.remove(fileId);
		System.out.println("Completed queued operations for " + fileId);
	}
}
