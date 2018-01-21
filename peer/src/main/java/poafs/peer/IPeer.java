package poafs.peer;

import java.io.IOException;
import java.net.UnknownHostException;

import poafs.exception.ProtocolException;
import poafs.file.FileBlock;

public interface IPeer {
	/**
	 * Handshake with another peer.
	 * @throws IOException 
	 * @throws UnknownHostException 
	 * @throws ProtocolException 
	 */
	public void openConnection() throws UnknownHostException, ProtocolException;
	
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
	void sendBlock(String fileId, FileBlock block);
	
	/**
	 * Get this peers Id.
	 * @return The peer's id.
	 */
	String getId();
}
