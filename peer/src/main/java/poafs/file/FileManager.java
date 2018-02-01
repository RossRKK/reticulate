package poafs.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Scanner;

import poafs.file.tracking.FileInfo;
import poafs.lib.Reference;

public class FileManager {
	
	private HashMap<String, PoafsFile> availableFiles = new HashMap<String, PoafsFile>();

	/**
	 * Get a file block from the disk.
	 * @param fileId The id of the file.
	 * @param index The index of the desired block.
	 * @return The relevant file block.
	 */
	public FileBlock getFileBlock(String fileId, int index) {
		return getFile(fileId).getBlocks().get(index);
	}
	
	/**
	 * Get a file from the disk.
	 * @param fileId The id of the file.
	 * @return The file.
	 */
	public PoafsFile getFile(String fileId) {
		if (!availableFiles.containsKey(fileId)) {
			try {
				loadFile(fileId);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		
		return availableFiles.get(fileId);
	}
	
	/**
	 * Utility method to read a line from the input.
	 * @return The line from the input
	 * @throws IOException
	 */
	private String readLine(InputStream in) throws IOException {
		//the input directly from the input stream
		int input;
		//the line that we want to return
		String line = "";
		
		//loop until the character is a new line
		while ((input = in.read()) != '\n') {
			//append the character to the line
			char character = new Character((char) input).charValue();
			line += character;
		}
		
		//tolerate carriage returns
		if (line.endsWith("\r")) {
			line = line.substring(0, line.length() - 1);
		}
		
		//return the line
		return line;
	}
	
	/**
	 * Load a file from the file system.
	 * @param fileId The id of the file.
	 * @throws IOException 
	 */
	private synchronized void loadFile(String fileId) throws IOException {
		File holdingFolder = new File(Reference.FILE_PATH + File.separator + fileId);
		
		if (holdingFolder.exists()) {
			PoafsFile file = new PoafsFile(fileId);
		
			for (String blockFilePath:holdingFolder.list()) {				
				Scanner sc = new Scanner(new FileInputStream(holdingFolder.getPath() + File.separator + blockFilePath));
				
				String originId = sc.nextLine();
				
				//this is an plain block
				String content64 = sc.nextLine();
				
				byte[] content = Base64.getDecoder().decode(content64);
				
				EncryptedFileBlock block = new EncryptedFileBlock(originId, content, Integer.parseInt(blockFilePath), null);
				
				sc.close();
				file.addBlock(block);
			}
			
			availableFiles.put(fileId, file);
		}
	}

	/**
	 * Register a file with the file manager.
	 * @param file The file being registered.
	 */
	public void registerFile(PoafsFile file) {
		availableFiles.put(file.getId(), file);
	}

	/**
	 * Get the files availabel to this peer.
	 * @return The avaialble files (content may be empty)
	 */
	public HashMap<String, PoafsFile> getAvailableFiles() {
		//create a hasmap
		HashMap<String, PoafsFile> available = new HashMap<String, PoafsFile>();
		
		File folder = new File(Reference.FILE_PATH);
		
		String[] availableFileIds = folder.list();
		
		for (String fileId:availableFileIds) {
			try {
				File holdingFolder = new File(Reference.FILE_PATH + File.separator + fileId);
				
				if (holdingFolder.exists()) {
					PoafsFile file = new PoafsFile(fileId);
				
					for (String blockFilePath:holdingFolder.list()) {				
						Scanner sc = new Scanner(new FileInputStream(holdingFolder.getPath() + File.separator + blockFilePath));
						
						String originId = sc.nextLine();
						
						EncryptedFileBlock block = new EncryptedFileBlock(originId, null, Integer.parseInt(blockFilePath), null);
						
						sc.close();
						file.addBlock(block);
					}
					
					available.put(fileId, file);
				}
			} catch (IOException e) {
				System.err.println("Error loading file: " + fileId);
				e.printStackTrace();
			}
		}
		
		return available;
	}

	/**
	 * Register a new file block with this file manager.
	 * @param fileId The id of the file.
	 * @param block The block being registered.
	 */
	public void registerBlock(String fileId, FileBlock block) {
		if (availableFiles.containsKey(fileId)) {
			availableFiles.get(fileId).addBlock(block);
		} else {
			//create a new file and 
			PoafsFile file = new PoafsFile(fileId);
			file.addBlock(block);
			
			availableFiles.put(fileId, file);
		}
		
		//save the file to disk
		try {
			availableFiles.get(fileId).saveFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
