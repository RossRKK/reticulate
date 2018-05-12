package poafs.file;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Base64;

import javax.crypto.SecretKey;

/**
 * A class that represents a single block of a file from a remote peer.
 * @author rossrkk
 *
 */
public class FileBlock {

	public FileBlock(byte[] content, int index) {
		this.content = content;
		this.index = index;
	}

	/**
	 * The unique id of the peer this block originated from.
	 */
	protected String originPeerId;
	
	/**
	 * The content of the block.
	 */
	protected byte[] content;
	
	/**
	 * The position this block has in the file.
	 */
	protected int index;
	
	/**
	 * The key this block should be encrypted with.
	 */
	private SecretKey key;
	
	/**
	 * Get the key to encrypt the block with.
	 * @return The key to encrypt the block with.
	 */
	public SecretKey getKey() {
		return key;
	}
	
	
	public void setKey(SecretKey key) {
		this.key = key;
	}

	public String getOriginPeerId() {
		return originPeerId;
	}

	public byte[] getContent() {
		return content;
	}

	public int getIndex() {
		return index;
	}
	
	protected String getHeaders() {
		return originPeerId;
	}

	public void save(String path) throws IOException {
		/*PrintWriter out = new PrintWriter(new FileOutputStream(path + File.separator + index));
		
		out.println(getHeaders());
		
		String base64Encoded = Base64.getEncoder().encodeToString(content);
		
		out.println(base64Encoded);
		
		out.close();*/
		FileOutputStream out = new FileOutputStream(path + File.separator + index);
		out.write(this.content);
		out.close();
	}


	public byte[] getChecksum() throws NoSuchAlgorithmException {
		MessageDigest crypt = MessageDigest.getInstance("SHA-1");
        crypt.reset();
        crypt.update(content);
		return crypt.digest();
	}
}
