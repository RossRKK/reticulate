package poafs.file.tracking;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FileInfo {

	private String fileId;
	
	private List<Set<String>> blocks;
	
	public FileInfo(String fileId) {
		this.fileId = fileId;
		blocks = new ArrayList<Set<String>>();
	}

	public String getFileId() {
		return fileId;
	}
	
	public Set<String> getPeerIdsForBlock(int index) {
		if (blocks.size() > index) {
			return blocks.get(index);
		} else {
			return new HashSet<String>();
		}
	}

	public void addPeerForBlock(int index, String peerId) {
		if (blocks.size() <= index || blocks.get(index) == null) {
			blocks.add(index, new HashSet<String>());
		}
		blocks.get(index).add(peerId);
	}
	
	public void removePeerForBlock(int index, String peerId) {
		blocks.get(index).remove(peerId);
	}
}
