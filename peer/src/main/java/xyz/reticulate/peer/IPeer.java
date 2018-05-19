package xyz.reticulate.peer;

import java.util.List;
import java.util.Map;
import java.util.Set;

import xyz.reticulate.exception.ProtocolException;
import xyz.reticulate.file.FileBlock;
import xyz.reticulate.file.tracking.PeerInfo;

public interface IPeer extends Runnable {
	
	/**
	 * Request a block from a file, the resulting block should be automatically decrypted.
	 * @param fileId The id of the file being requested.
	 * @param index The index of the block being requested.
	 * @throws ProtocolException 
	 */
	FileBlock requestBlock(String fileId, int index) throws ProtocolException;

	/**
	 * Send a block to the remote peer.
	 * @param fileId The id of the file being sent.
	 * @param block The block being sent.
	 */
	void sendBlock(String fileId, FileBlock block);
	
	/**
	 * Get this peers Id.
	 * @return The peer's id.
	 */
	String getId();

	/**
	 * Get a set of available file blocks that this peer can serve.
	 * @return A map of ID and list of available block indicies
	 * @throws ProtocolException 
	 */
	public Map<String, List<Integer>> requestAvailableFiles() throws ProtocolException;

	/**
	 * Get a set of known peers that this peer knows about.
	 * @return A set of known peers.
	 */
	public Set<PeerInfo> requestKnownPeers();
	
	/**
	 * Close the connection.
	 */
	public void disconnect();
}
