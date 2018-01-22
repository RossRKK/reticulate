package poafs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.web3j.crypto.CipherException;

import poafs.auth.EthAuth;
import poafs.auth.IAuthenticator;
import poafs.cryto.KeyStore;
import poafs.exception.KeyException;
import poafs.exception.ProtocolException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;
import poafs.file.FileManager;
import poafs.file.FileMeta;
import poafs.file.PoafsFile;
import poafs.file.tracking.DummyTracker;
import poafs.file.tracking.ITracker;
import poafs.lib.Reference;
import poafs.net.Server;

/**
 * This is a class that is designed to represent and entire POAFS network.
 * @author rkk2
 *
 */
public class Network {
	
	/**
	 * The size of each block when its put onto this network.
	 */
	private int blockLength = Reference.BLOCK_SIZE;
	
	/**
	 * The authenticator being used by this peer.
	 */
	private IAuthenticator auth;
	
	/**
	 * The encrypter to be used for all local files.
	 */
	private KeyStore keyStore;
	
	private ITracker tracker;
	
	/**
	 * The file system manager.
	 */
	private FileManager fileManager = new FileManager();
	
	public Network(String path, String pass) throws ProtocolException, IOException, CipherException {
		//this.auth = new NetAuthenticator(hostname, port, ssl);
		this.auth = new EthAuth();
		keyStore = new KeyStore();
		tracker = new DummyTracker();
		
		//start the local server
		new Thread(new Server(Reference.DEFAULT_PORT, fileManager)).start();
	}
	
	/**
	 * Register a local file with the network.
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public void registerFile(String path, String fileName) throws IOException, ProtocolException, KeyException {
		String id = UUID.randomUUID().toString();
		System.out.println(id);
		
		//read in the file
		File orig = new File(path);
		int numOfBytes = (int) orig.length();
		FileInputStream inFile = new FileInputStream(orig);
		byte[] bytes = new byte[numOfBytes];
		inFile.read(bytes);
		inFile.close();
		System.out.println("File read");
		
		int noBlocks = (int) Math.ceil((double)bytes.length / Reference.BLOCK_SIZE);
		
		PoafsFile file = new PoafsFile(id);
		
		for (int i = 0; i < noBlocks; i++) {
			int remainingBytes = bytes.length - (i * blockLength);
			int thisBlockLength = Math.min(blockLength, remainingBytes);
			
			byte[] contents = Arrays.copyOfRange(bytes, i * blockLength, i * blockLength + thisBlockLength);
			
			FileBlock block = new FileBlock(id, contents, i);
			EncryptedFileBlock encrypted = keyStore.encrypt(block);
			
			file.addBlock(encrypted);
		}
		System.out.println("Encrypted");
		
		fileManager.registerFile(file);
		file.saveFile();
		
		auth.registerFile(file, fileName);
		System.out.println("Registered");
	}

	public List<FileMeta> listFiles() throws ProtocolException {
		return auth.listFiles();
	}
	
	public PoafsFileStream fetchFile(String fileId) {
		return new PoafsFileStream(fileId, 5, auth, keyStore, tracker);
	}
}
