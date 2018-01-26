package poafs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.UUID;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import poafs.auth.EthAuth;
import poafs.auth.IAuthenticator;
import poafs.cryto.KeyStore;
import poafs.exception.KeyException;
import poafs.exception.ProtocolException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;
import poafs.file.FileManager;
import poafs.file.PoafsFile;
import poafs.file.tracking.FileInfo;
import poafs.file.tracking.ITracker;
import poafs.file.tracking.NetTracker;
import poafs.lib.Reference;
import poafs.peer.Server;

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
		Credentials creds = WalletUtils.loadCredentials(pass, path);
		System.out.println(creds.getAddress());
		keyStore = new KeyStore(KeyStore.buildRSAKeyPairFromWallet(creds));
		this.auth = new EthAuth(creds);
		tracker = new NetTracker(fileManager);
		
		
		//start the local server
		new Thread(new Server(Reference.DEFAULT_PORT, fileManager)).start();
		
		//register the local peer with the tracker
		tracker.registerPeer(Application.getPropertiesManager().getPeerId(), new InetSocketAddress("localhost", Reference.DEFAULT_PORT));
	}
	
	public static SecretKey buildAESKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256); // for example
		return keyGen.generateKey();
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
	public void registerFile(String path, String fileName) throws IOException, ProtocolException, KeyException, NoSuchAlgorithmException {
		String id = UUID.randomUUID().toString();
		System.out.println(id);

		SecretKey key = buildAESKey();
		
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
			block.setKey(key);
			
			EncryptedFileBlock encrypted = keyStore.encrypt(block);
			
			file.addBlock(encrypted);
			
			tracker.registerTransfer(Application.getPropertiesManager().getPeerId(), id, i);
		}
		
		System.out.println("Encrypted");
		
		fileManager.registerFile(file);
		file.saveFile();
		
		auth.registerFile(file, fileName, noBlocks, ((EncryptedFileBlock)file.getBlocks().get(0)).getWrappedKey());
		System.out.println("Registered");
	}

	public FileInfo[] listFiles() throws ProtocolException {
		return tracker.listFiles();
	}
	
	public PoafsFileStream fetchFile(String fileId) {
		return new PoafsFileStream(fileId, 5, auth, keyStore, tracker, fileManager);
	}
}
