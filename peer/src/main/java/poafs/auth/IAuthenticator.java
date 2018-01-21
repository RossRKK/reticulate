package poafs.auth;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

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
	 * Get the decrypter for a given peer.
	 * @param peerId The id of the peer who provided the block we want to decrypt.
	 * @return The decrypter that can decrypt files from the given peer.
	 * @throws IOException 
	 * @throws KeyException 
	 */
	public IDecrypter getKeyForPeer(String peerId) throws ProtocolException, KeyException;
	
	/**
	 * Get the hostname this peer can be accessed on.
	 * @param peerId The id of the peer.
	 * @return The hostname of the peer.
	 * @throws IOException 
	 */
	public InetSocketAddress getHostForPeer(String peerId) throws ProtocolException;
	
	
	/**
	 * Authorise the user TODO
	 * @return Whether the user is authorised.
	 * @throws IOException 
	 */
	public boolean authoriseUser(String userName, String password) throws ProtocolException;
	
	/**
	 * List all available files on this auth server.
	 * @return A list of files.
	 * @throws IOException 
	 */
	public List<FileMeta> listFiles() throws ProtocolException;
	
	public FileMeta getInfoForFile(String fileId) throws ProtocolException;
	
	public List<String> findBlock(String fileId, int blockIndex) throws ProtocolException;

	public boolean registerFile(PoafsFile file, String fileName) throws ProtocolException;

	public IEncrypter registerPeer() throws ProtocolException, KeyException;
	
	/**
	 * Register that this peer has received a file block.
	 * @param fileId The id of the file.
	 * @param index The index of the block.
	 */
	public void registerTransfer(String fileId, int index) throws ProtocolException;
}
