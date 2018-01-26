package poafs.file.tracking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileInfo {

	private String fileId;
	
	private List<Set<String>> blocks;
	
	public FileInfo(String fileId, int length) {
		this.fileId = fileId;
		blocks = new ArrayList<Set<String>>(length);
		
		for (int i = 0; i < length; i++) {
			blocks.add(new HashSet<String>());
		}
	}

	public String getFileId() {
		return fileId;
	}
	
	public Set<String> getPeerIdsForBlock(int index) {
		return blocks.get(index);
	}

	public void addPeerForBlock(int index, String peerId) {
		blocks.get(index).add(peerId);
	}
	
	public void removePeerForBlock(int index, String peerId) {
		blocks.get(index).remove(peerId);
	}
}
