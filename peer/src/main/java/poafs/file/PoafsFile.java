package poafs.file;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;

import poafs.lib.Reference;

/**
 * A file that represents a file as it was download from the network.
 * @author rossrkk
 *
 */
public class PoafsFile {
	
	private String id;
	
	public PoafsFile(String id) {
		this.id = id;
	}
	
	/**
	 * A list of all the blocks in this file.
	 */
	private HashMap<Integer, FileBlock> blocks = new HashMap<Integer, FileBlock>();
	
	
	/**
	 * Add a block that was just loaded to this 
	 * @param block The block to be added.
	 */
	public void addBlock(FileBlock block) {
		blocks.put(block.getIndex(), block);
	}
	
	/**
	 * Clear the contents of this file.
	 */
	public void clearContents() {
		blocks.clear();
	}
	
	/**
	 * Save the file to disk.
	 * @throws IOException 
	 */
	public void saveFile() throws IOException {
		String path = Reference.FILE_PATH + File.separator + id;
		File folder = new File(path);
		folder.mkdirs();
		
		for (int i = 0; i < blocks.size(); i++) {
			blocks.get(i).save(path);
		}
	}
	
	/* Getters and Setters */
	
	public String getId() {
		return id;
	}
	
	public int getNumBlocks() {
		return blocks.size();
	}
	
	public HashMap<Integer, FileBlock> getBlocks() {
		return blocks;
	}
}
