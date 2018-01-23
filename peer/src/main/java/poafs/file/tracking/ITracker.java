package poafs.file.tracking;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.util.List;

import poafs.exception.ProtocolException;
import poafs.file.FileMeta;

/**
 * Interface for something that tracks the lcoations of files.
 * @author rossrkk
 *
 */
public interface ITracker {

	/**
	 * Get the hostname this peer can be accessed on.
	 * @param peerId The id of the peer.
	 * @return The hostname of the peer.
	 * @throws IOException 
	 */
	public InetSocketAddress getHostForPeer(String peerId) throws ProtocolException;
	
	public List<String> findBlock(String fileId, int blockIndex) throws ProtocolException;
	
	/**
	 * Register that this peer has received a file block.
	 * @param fileId The id of the file.
	 * @param index The index of the block.
	 */
	public void registerTransfer(String fileId, int index) throws ProtocolException;
	
	/**
	 * List all available files on this auth server.
	 * @return A list of files.
	 * @throws IOException 
	 */
	public List<FileMeta> listFiles() throws ProtocolException;
}
