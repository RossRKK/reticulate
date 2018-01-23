package poafs.auth;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import javax.crypto.SecretKey;

import poafs.cryto.IDecrypter;
import poafs.cryto.IEncrypter;
import poafs.exception.KeyException;
import poafs.exception.ProtocolException;
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
	 * @throws IOException 
	 * @throws KeyException 
	 */
	public byte[] getKeyForFile(String fileId);
	
	public FileMeta getInfoForFile(String fileId);
	
	public boolean registerFile(PoafsFile file, String fileName, byte[] wrappedKey);
}
