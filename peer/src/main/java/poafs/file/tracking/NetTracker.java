package poafs.file.tracking;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

import poafs.exception.ProtocolException;

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
	public InetSocketAddress getHostForPeer(String peerId) {
		if (peers.get(peerId) != null) {
			return peers.get(peerId).getAddr();
		} else {
			return null;
		}
	}

	@Override
	public Set<String> findBlock(String fileId, int blockIndex) {
		if (files.get(fileId) == null) {
			Set<String> peers = new HashSet<String>();
			
			//TODO do another network traversal to try and find it
			
			return peers;
		} else {
			return files.get(fileId).getPeerIdsForBlock(blockIndex);
		}
	}
	

	@Override
	public void registerTransfer(String peerId, String fileId, int index) {
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
	public List<FileInfo> listFiles() throws ProtocolException {
		return files.entrySet().parallelStream().map(e -> e.getValue()).collect(Collectors.toList());
	}

	@Override
	public Map<String, PeerInfo> getPeers() {
		return peers;
	}

	@Override
	public Map<String, FileInfo> getFiles() {
		return files;
	}

	@Override
	public void registerPeers(Collection<PeerInfo> peers) {
		for (PeerInfo p:peers) {
			this.peers.put(p.getPeerId(), p);
		}
	}
	
	@Override
	public void registerFiles(String peerId, Map<String, List<Integer>> availableFiles) {
		for(Entry<String, List<Integer>> entry:availableFiles.entrySet()) {
			for (int index:entry.getValue()) {
				registerTransfer(peerId, entry.getKey(), index);
			}
		}
	}

	@Override
	public void removePeer(String peerId) {
		peers.remove(peerId);
	}

}
