package poafs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

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
import poafs.exception.NoValidPeersException;
import poafs.exception.ProtocolException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;
import poafs.file.FileManager;
import poafs.file.PoafsFile;
import poafs.file.tracking.FileInfo;
import poafs.file.tracking.ITracker;
import poafs.file.tracking.NetTracker;
import poafs.file.tracking.PeerInfo;
import poafs.lib.Reference;
import poafs.local.PropertiesManager;
import poafs.peer.IPeer;
import poafs.peer.NetworkPeer;
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
	 * The key store used to encrypt and decrypt files.
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
		tracker = new NetTracker();
		
		
		//start the local server
		new Thread(new Server(Reference.port, tracker, fileManager)).start();
		
		connect();
	}
	
	/**
	 * Connect to the known peer.
	 * @throws IOException 
	 * @throws ProtocolException 
	 * @throws UnknownHostException 
	 */
	private void connect() throws UnknownHostException, ProtocolException, IOException {
		PropertiesManager pm = Application.getPropertiesManager();
		
		if (pm.getKnownPeerId() != "") {
			
			tracker.registerPeer(pm.getKnownPeerId(), new InetSocketAddress(pm.getKnownPeerAddress(), pm.getKnownPeerPort()));
			
			IPeer knownPeer = new NetworkPeer(new Socket(pm.getKnownPeerAddress(), pm.getKnownPeerPort()), tracker, fileManager);
			
			System.out.println("Connected to known peer");
			
			tracker.registerPeers(knownPeer.requestKnownPeers());
			System.out.println("Fetched known peers");
			
			tracker.registerFiles(pm.getKnownPeerId(), knownPeer.requestAvailableFiles());
			System.out.println("Fetched known files");
		}
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

	/**
	 * Upload a file to the network.
	 * @param fileId The id of the file to upload.
	 * @throws NoValidPeersException 
	 */
	public void uploadFile(String fileId) throws NoValidPeersException {
		PoafsFile file = fileManager.getFile(fileId);
		
		if (file != null) {
			//upload each block
			for (Entry<Integer, FileBlock> block:file.getBlocks().entrySet()) {
				uploadBlock(fileId, block.getValue());
			}
		} else {
			System.out.println(fileId + " doesn't exist locally, can't upload");
		}
	}
	
	private void uploadBlock(String fileId, FileBlock block) throws NoValidPeersException {
		long startTime = System.currentTimeMillis();
		
		Random r = new Random();
		Set<String> peers = tracker.getPeers().keySet();
		
		//remove the local peer from the set
		peers.remove(Application.getPropertiesManager().getPeerId());
		
		String peerId = null;
		
		//loop until we get the block or run out of peers
		while (!peers.isEmpty()) {
			try {
				//choose a random peer
				peerId = peers.toArray(new String[peers.size()])[r.nextInt(peers.size())];
				
				InetSocketAddress addr = tracker.getHostForPeer(peerId);
				
				System.out.println("Uploading to " + peerId + " at " + addr.getHostName());
				//get the block off of the peer
				IPeer peer = new NetworkPeer(new Socket(addr.getHostName(), addr.getPort()), tracker, fileManager);
				
				System.out.println("Uploading block: " + fileId + ":" + block.getIndex());
				peer.sendBlock(fileId, block);
				
				long time = System.currentTimeMillis() - startTime;
				
				System.out.println("Upload for " + fileId + ":" + block.getIndex() + " took " + 
						time + "ms " + ((double)time)/block.getContent().length + "B/ms");
				
				break;
			} catch (IOException e) {
				System.err.println(peerId + " was unreachable");
			} catch (ProtocolException e) {
				System.err.println(e.getMessage());
			} finally {
				peers.remove(peerId);
				
				if (peers.size() == 0) {
					break;
				}
			}
		}
		throw new NoValidPeersException();
	}
	
	public List<FileInfo> listFiles() throws ProtocolException {
		return tracker.listFiles();
	}
	
	public PoafsFileStream fetchFile(String fileId) {
		return new PoafsFileStream(fileId, 5, auth, keyStore, tracker, fileManager);
	}

	
	public List<PeerInfo> listPeers() {
		return tracker.getPeers().entrySet().parallelStream().map(e -> e.getValue()).collect(Collectors.toList());
	}
}
