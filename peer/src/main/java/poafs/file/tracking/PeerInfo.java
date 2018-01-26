package poafs.file.tracking;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.HashSet;
import java.util.Objects;
import java.util.Set;

import org.web3j.tuples.generated.Tuple2;

/**
 * A class that represents some meta data about peers.
 * @author rossrkk
 *
 */
public class PeerInfo {
	
	/**
	 * Construct a new peer info object.
	 * @param peerId The peer's id.
	 * @param addr The address of the peer.
	 */
	public PeerInfo(String peerId, InetSocketAddress addr) {
		this.peerId = peerId;
		this.addr = addr;
		
		//create the hashset
		fileBlocks = new HashSet<Tuple2<String, Integer>>();
	}
	
	/**
	 * The id of the peer.
	 */
	private String peerId;
	
	/**
	 * Where the peer can be found.
	 */
	private InetSocketAddress addr;
	
	/**
	 * The ids of the file blocks that this peer has.
	 */
	private Set<Tuple2<String, Integer>> fileBlocks;

	public String getPeerId() {
		return peerId;
	}

	public void setPeerId(String peerId) {
		this.peerId = peerId;
	}

	public InetSocketAddress getAddr() {
		return addr;
	}

	public void setAddr(InetSocketAddress addr) {
		this.addr = addr;
	}

	/**
	 * Get the available file blocks that the peer has.
	 * @return The vailable file blocks.
	 */
	public Set<Tuple2<String, Integer>> getFileBlocks() {
		return fileBlocks;
	}
	
	/**
	 * Add a file block to this peer.
	 * @param fileId The id of the file that the peer has
	 * @param index The index of the block that the peer has.
	 */
	public void addFileBlock(String fileId, int index) {
		fileBlocks.add(new Tuple2<String ,Integer>(fileId, index));
	}
	
	/**
	 * REmove a file block from the set
	 * @param fileId The id of the file
	 * @param index Th index of the block.
	 */
	public void removeFileBlock(String fileId, int index) {
		fileBlocks.remove(new Tuple2<String ,Integer>(fileId, index));
	}
	
	/**
	 * Returns true if the peer ids are the same.
	 */
	@Override
	public boolean equals(Object obj) {
		if (obj instanceof PeerInfo) {
			return ((PeerInfo)obj).peerId.equals(peerId);
		} else {
			return false;
		}
	}
	
	/**
	 * Return the hash of the peerId.
	 */
	@Override
	public int hashCode() {
		return Objects.hash(peerId);
	}
	
}
