package xyz.reticulate;

import java.io.IOException;
import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;

import xyz.reticulate.exception.KeyException;
import xyz.reticulate.exception.NoValidPeersException;
import xyz.reticulate.exception.ProtocolException;

/**
 * This is a class that provides all the same functionality as the network class but will report pending operations as completed.
 * This is supposed to streamline the user experience
 * @author rossrkk
 *
 */
public class CachedNetwork extends Network {
	
	private HashMap<String, byte[]> cache = new HashMap<String, byte[]>();
	
	public CachedNetwork(Credentials creds, String contractAddress) throws ProtocolException, IOException, CipherException {
		super(creds, contractAddress);
	}
	
	@Override
	public void registerFile(String id, byte[] bytes) throws NoSuchAlgorithmException, ProtocolException, KeyException, NoSuchAlgorithmException, IOException, NoValidPeersException {
		cache.put(id, bytes);
		
		long startTime = System.currentTimeMillis();

		Thread t = new Thread(() -> {
			//kick off the registration
			try {
				super.registerFile(id, bytes);
			} catch (NoSuchAlgorithmException | ProtocolException | KeyException | IOException
					| NoValidPeersException e) {
				e.printStackTrace();
			}
			
			//remove this from the cache, it may change later
			cache.remove(id);
			
			System.out.println("Finished register operation for " + id + " it took " + (System.currentTimeMillis() - startTime) + "ms");
		});
		
		t.start();
	}
	
	@Override
	public void updateFileContent(String fileId, byte[] bytes) throws KeyException, NoSuchAlgorithmException, NoValidPeersException {
		cache.put(fileId, bytes);
		long startTime = System.currentTimeMillis();

		Thread t = new Thread(() -> {
			//kick off the registration
			try {
				super.updateFileContent(fileId, bytes);
			} catch (NoSuchAlgorithmException | KeyException 
					| NoValidPeersException e) {
				e.printStackTrace();
			}
			
			//remove this from the cache, it may change later
			cache.remove(fileId);
			
			System.out.println("Finished write operation for " + fileId + " it took " + (System.currentTimeMillis() - startTime) + "ms");
		});

		t.start();
	}
	
	@Override
	public InputStream fetchFile(String fileId) {
		if (cache.containsKey(fileId)) {
			return new ByteStream(cache.get(fileId));
		} else {
			return super.fetchFile(fileId);
		}
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
