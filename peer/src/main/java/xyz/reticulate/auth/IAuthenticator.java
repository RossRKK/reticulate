package xyz.reticulate.auth;

import xyz.reticulate.file.ReticulateFile;

/**
 * An interface that represents the auth service for the network.
 * @author rossrkk
 *
 */
public interface IAuthenticator {
	/**
	 * Get the wrapped key for a given file.
	 * @param peerId The id of the file that we want to decrypt.
	 * @return The wrapped key for that file.
	 */
	public byte[] getKeyForFile(String fileId);
	
	public int getFileLength(String fileId);

	public int getAccessLevel(String fileId, String user);

	public boolean removeFile(String fileId);

	public boolean shareFile(String fileId, String user, byte[] recipientKey, int accessLevel);

	public boolean revokeShare(String fileId, String user);

	public boolean modifyAccessLevel(String fileId, String user, int accessLevel);

	public boolean registerFile(ReticulateFile file, int length, byte[] wrappedKey);
	
	public boolean updateFileLength(String fileId, int newLength);
	
	public boolean updateCheckSum(String fileId, int blockIndex, byte[] checkSum);
	
	public boolean compareCheckSum(String fileId, int blockIndex, byte[] checkSum);
	
	public byte[] getCheckSum(String fileId, int blockIndex);
}
