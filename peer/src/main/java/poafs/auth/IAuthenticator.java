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
	 * Get the decrypter for a given file.
	 * @param peerId The id of the file that we want to decrypt.
	 * @return The decrypter that can decrypt that file.
	 * @throws IOException 
	 * @throws KeyException 
	 */
	public IDecrypter getKeyForFile(String fileId) throws ProtocolException, KeyException;
	
	
	/**
	 * List all available files on this auth server.
	 * @return A list of files.
	 * @throws IOException 
	 */
	public List<FileMeta> listFiles() throws ProtocolException;
	
	public FileMeta getInfoForFile(String fileId) throws ProtocolException;

	public boolean registerFile(PoafsFile file, String fileName, SecretKey key) throws ProtocolException;
}
