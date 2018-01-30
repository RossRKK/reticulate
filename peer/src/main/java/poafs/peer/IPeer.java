package poafs.peer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import poafs.exception.ProtocolException;
import poafs.file.FileBlock;
import poafs.file.tracking.PeerInfo;

public interface IPeer extends Runnable {
	
	/**
	 * Request a block from a file, the resulting block should be automatically decrypted.
	 * @param fileId The id of the file being requested.
	 * @param index The index of the block being requested.
	 * @throws ProtocolException 
	 */
	FileBlock requestBlock(String fileId, int index) throws ProtocolException;

	/**
	 * Send a block to this peer.
	 * @param fileId
	 * @param block
	 */
	void sendBlock(String fileId, FileBlock block) throws ProtocolException;
	
	/**
	 * Get this peers Id.
	 * @return The peer's id.
	 */
	String getId();

	/**
	 * Get a set of available file blocks that this peer can serve.
	 * @return A map of ID and list of available block indicies
	 */
	public Map<String, List<Integer>> requestAvailableFiles();

	/**
	 * Get a set of known peers that this peer knows about.
	 * @return A set of known peers.
	 */
	public Set<PeerInfo> requestKnownPeers();
}
