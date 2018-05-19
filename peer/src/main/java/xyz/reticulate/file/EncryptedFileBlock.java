package xyz.reticulate.file;

public class EncryptedFileBlock extends FileBlock {
	
	public EncryptedFileBlock(byte[] content, int index, byte[] wrappedKey) {
		super(content, index);
		this.wrappedKey = wrappedKey;
	}
	
	
	/**
	 * The wrapped AES key that was used to encrypt this block.
	 */
	private byte[] wrappedKey;
	
	public byte[] getWrappedKey() {
		return wrappedKey;
	}

	public void setWrappedKey(byte[] wrappedKey) {
		this.wrappedKey = wrappedKey;
	}
}
