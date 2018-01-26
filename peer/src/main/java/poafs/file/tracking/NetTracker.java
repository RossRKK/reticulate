package poafs.file.tracking;

import java.net.InetAddress;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import poafs.exception.ProtocolException;
import poafs.file.FileMeta;

public class NetTracker implements ITracker {
	
	/**
	 * A set of all known peers.
	 */
	private HashMap<String, PeerInfo> peers = new HashMap<String, PeerInfo>();
	
	private HashMap<String, FileInfo> files = new HashMap<String, FileInfo>();

	@Override
	public InetAddress getHostForPeer(String peerId) throws ProtocolException {
		return peers.get(peerId).getAddr();
	}
	
	/**
	 * Find a peer by querying the network.
	 * @param peerId The id of the peer.
	 * @return The peer's info.
	 */
	private PeerInfo findPeer(String peerId) {
		return null;
	}

	@Override
	public Set<String> findBlock(String fileId, int blockIndex) throws ProtocolException {
		return files.get(fileId).getPeerIdsForBlock(blockIndex);
	}
	
	/**
	 * Find the block by querying the network.
	 * @param fileId The id of the file to find.
	 * @param index The index of the block.
	 * @return The id of a peer who has the file block.
	 */
	private String findBlockOnNetwork(String fileId, int index) {
		return null;
	}

	@Override
	public void registerTransfer(String peerId, String fileId, int index) throws ProtocolException {
		if (!peers.containsKey(peerId)) {
			peers.put(peerId, new PeerInfo(peerId, null));
		}
		peers.get(peerId).addFileBlock(fileId, index);
	}

	@Override
	public List<FileMeta> listFiles() throws ProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

}
