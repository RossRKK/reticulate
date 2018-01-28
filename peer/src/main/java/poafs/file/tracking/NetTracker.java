package poafs.file.tracking;

import java.net.InetSocketAddress;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import poafs.Application;
import poafs.exception.ProtocolException;
import poafs.file.FileManager;

public class NetTracker implements ITracker {
	
	/**
	 * A set of all known peers.
	 */
	private HashMap<String, PeerInfo> peers = new HashMap<String, PeerInfo>();
	
	/**
	 * The locations of all known files.
	 */
	private HashMap<String, FileInfo> files = new HashMap<String, FileInfo>();
	

	@Override
	public InetSocketAddress getHostForPeer(String peerId) throws ProtocolException {
		return peers.get(peerId).getAddr();
	}

	@Override
	public Set<String> findBlock(String fileId, int blockIndex) throws ProtocolException {
		if (files.get(fileId) == null) {
			Set<String> peers = new HashSet<String>();
			return peers;
		} else {
			return files.get(fileId).getPeerIdsForBlock(blockIndex);
		}
	}
	

	@Override
	public void registerTransfer(String peerId, String fileId, int index) throws ProtocolException {
		if (peers.containsKey(peerId)) {
			peers.get(peerId).addFileBlock(fileId, index);
		}
		
		//register it in the files map
		if (!files.containsKey(fileId)) {
			files.put(fileId, new FileInfo(fileId));
		}
		files.get(fileId).addPeerForBlock(index, peerId);
	}
	
	@Override
	public void registerPeer(String peerId, InetSocketAddress addr) {
		peers.put(peerId, new PeerInfo(peerId, addr));
	}

	@Override
	public FileInfo[] listFiles() throws ProtocolException {
		return (FileInfo[])files.entrySet().parallelStream().map(e -> e.getValue()).toArray();
	}

	@Override
	public Map<String, PeerInfo> getPeers() {
		return peers;
	}

}
