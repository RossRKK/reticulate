package xyz.reticulate.file;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import xyz.reticulate.auth.IAuthenticator;
import xyz.reticulate.lib.Reference;

public class FileManager {
	
	private IAuthenticator auth;
	
	public FileManager(IAuthenticator auth) {
		this.auth = auth;
	}
	
	private HashMap<String, ReticulateFile> availableFiles = new HashMap<String, ReticulateFile>();

	/**
	 * Get a file block from the disk.
	 * @param fileId The id of the file.
	 * @param index The index of the desired block.
	 * @return The relevant file block.
	 */
	public FileBlock getFileBlock(String fileId, int index) {
		ReticulateFile file = getFile(fileId);
		if (file != null) {
			return file.getBlocks().get(index);
		} else {
			return null;
		}
	}
	
	/**
	 * Get a file from the disk.
	 * @param fileId The id of the file.
	 * @return The file.
	 */
	public ReticulateFile getFile(String fileId) {
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
	 * Load a file from the file system.
	 * @param fileId The id of the file.
	 * @throws IOException 
	 */
	private synchronized void loadFile(String fileId) throws IOException {
		File holdingFolder = new File(Reference.FILE_PATH + File.separator + fileId);
		
		if (holdingFolder.exists()) {
			ReticulateFile file = new ReticulateFile(fileId);
		
			for (String blockFilePath:holdingFolder.list()) {
				//read in the content of the file with the encrypted file block content
				File f = new File(holdingFolder.getPath() + File.separator + blockFilePath);
				FileInputStream fis = new FileInputStream(f);
				byte[] content = new byte[(int) f.length()];
				fis.read(content);
				fis.close();
				
				EncryptedFileBlock block = new EncryptedFileBlock(content, Integer.parseInt(blockFilePath), null);
				
				file.addBlock(block);
			}
			
			availableFiles.put(fileId, file);
		}
	}

	/**
	 * Register a file with the file manager.
	 * @param file The file being registered.
	 */
	public void registerFile(ReticulateFile file) {
		availableFiles.put(file.getId(), file);
		try {
			file.saveFile();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Get the files availabel to this peer.
	 * @return The avaialble files (content will be empty)
	 */
	public HashMap<String, ReticulateFile> getAvailableFiles() {
		//create a hasmap
		HashMap<String, ReticulateFile> available = new HashMap<String, ReticulateFile>();
		
		File folder = new File(Reference.FILE_PATH);
		
		String[] availableFileIds = folder.list();
		
		for (String fileId:availableFileIds) {
			File holdingFolder = new File(Reference.FILE_PATH + File.separator + fileId);
			
			if (holdingFolder.exists()) {
				ReticulateFile file = new ReticulateFile(fileId);
			
				for (String blockFilePath:holdingFolder.list()) {				
					EncryptedFileBlock block = new EncryptedFileBlock(null, Integer.parseInt(blockFilePath), null);
					
					file.addBlock(block);
				}
				
				available.put(fileId, file);
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
		try {
			if (auth.compareCheckSum(fileId, block.getIndex(), block.getChecksum())) {
				if (availableFiles.containsKey(fileId)) {
					availableFiles.get(fileId).addBlock(block);
				} else {
					//create a new file and 
					ReticulateFile file = new ReticulateFile(fileId);
					file.addBlock(block);
					
					availableFiles.put(fileId, file);
				}
				
				//save the file to disk
				try {
					availableFiles.get(fileId).saveFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			} else {
				System.out.println("Recieved " + fileId + ":" + block.index + " but it didn't have a valid checksum");
			}
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
