package poafs.auth;

import poafs.file.FileMeta;
import poafs.file.PoafsFile;

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
	
	public FileMeta getInfoForFile(String fileId);
	
	public boolean registerFile(PoafsFile file, String fileName, byte[] wrappedKey);

	public int getAccessLevel(String fileId, String user);

	public boolean removeFile(String fileId);

	public boolean shareFile(String fileId, String user, byte[] recipientKey, int accessLevel);

	public boolean revokeShare(String fileId, String user);

	public boolean modifyAccessLevel(String fileId, String user, int accessLevel);
}
