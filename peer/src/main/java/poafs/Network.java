package poafs;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
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
	
	private Credentials creds;
	
	/**
	 * The file system manager.
	 */
	private FileManager fileManager = new FileManager();
	
	public Network(String path, String pass, String contractAddress) throws ProtocolException, IOException, CipherException {
		creds = WalletUtils.loadCredentials(pass, path);
		System.out.println(creds.getAddress());
		keyStore = new KeyStore(KeyStore.buildRSAKeyPairFromWallet(creds));
		this.auth = new EthAuth(creds, contractAddress);
		tracker = new NetTracker();
		
		
		//start the local server
		new Thread(new Server(Reference.DEFAULT_PORT, tracker, fileManager)).start();
		
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
	 * Write a file to the network, by segnemnting it and encrypting it
	 * @param file The file to be uploaded
	 * @param bytes The contents of the file
	 * @param key The key to encrypt it with
	 * @throws KeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoValidPeersException
	 */
	private byte[][] divideIntoBlocks(PoafsFile file, byte[] bytes, SecretKey key) throws KeyException, NoSuchAlgorithmException, NoValidPeersException {
		
		int noBlocks = (int) Math.ceil((double)bytes.length / Reference.BLOCK_SIZE);
		
		byte[][] checkSums = new byte[noBlocks][];
		
		//clear the contents before adding new blocks
		file.clearContents();
				
		for (int i = 0; i < noBlocks; i++) {
			int remainingBytes = bytes.length - (i * blockLength);
			int thisBlockLength = Math.min(blockLength, remainingBytes);
			
			byte[] contents = Arrays.copyOfRange(bytes, i * blockLength, i * blockLength + thisBlockLength);
			
			FileBlock block = new FileBlock(contents, i);
			block.setKey(key);
			
			EncryptedFileBlock encrypted = keyStore.encrypt(block);
			
			file.addBlock(encrypted);
			
			tracker.registerTransfer(Application.getPropertiesManager().getPeerId(), file.getId(), i);
			
			//register the blocks checksum
			MessageDigest crypt = MessageDigest.getInstance("SHA-1");
	        crypt.reset();
	        crypt.update(encrypted.getContent());
	        
	        checkSums[i] = crypt.digest();
		}
		
		return checkSums;
	}
	
	/**
	 * Register a local file with the network.
	 * @throws IOException 
	 * @throws BadPaddingException 
	 * @throws IllegalBlockSizeException 
	 * @throws NoSuchPaddingException 
	 * @throws NoSuchAlgorithmException 
	 * @throws NoValidPeersException 
	 * @throws InvalidKeyException 
	 */
	public void registerFile(String path) throws IOException, ProtocolException, KeyException, NoSuchAlgorithmException, NoValidPeersException {
		//read in the file
		File orig = new File(path);
		int numOfBytes = (int) orig.length();
		FileInputStream inFile = new FileInputStream(orig);
		byte[] bytes = new byte[numOfBytes];
		inFile.read(bytes);
		inFile.close();
		System.out.println("File read");
		
		registerFile(bytes);
	}
	
	
	
	/**
	 * REgister a file eith the network
	 * @param bytes The contents of the file.
	 * @param fileName The name of the file.
	 * @throws NoSuchAlgorithmException
	 * @throws ProtocolException
	 * @throws KeyException
	 * @throws NoSuchAlgorithmException
	 * @throws IOException
	 * @throws NoValidPeersException 
	 */
	public String registerFile(byte[] bytes) throws NoSuchAlgorithmException, ProtocolException, KeyException, NoSuchAlgorithmException, IOException, NoValidPeersException {
		String id = UUID.randomUUID().toString();
		System.out.println(id);
		
		SecretKey key = buildAESKey();
		
		PoafsFile file = new PoafsFile(id);
		
		byte[][] checkSums = divideIntoBlocks(file, bytes, key);
		
		System.out.println("Encrypted");
		
		fileManager.registerFile(file);
		file.saveFile();
		
		auth.registerFile(file, file.getNumBlocks(), ((EncryptedFileBlock)file.getBlocks().get(0)).getWrappedKey());
		System.out.println("Registered");
		
		//updating checksums
		for (int i = 0; i < checkSums.length; i++) {
			if (!auth.updateCheckSum(file.getId(), i, checkSums[i])) {
				System.err.println("Error updating checksum");
			}
		}
		System.out.println("Checksums updated");
		
		return id;
	}
	
	/**
	 * Write the new contents of a file to the network.
	 * @param fileId The id of a the file.
	 * @param bytes The new contents of the file.
	 * @throws KeyException
	 * @throws NoSuchAlgorithmException
	 * @throws NoValidPeersException
	 */
	public void updateFileContent(String fileId, byte[] bytes) throws KeyException, NoSuchAlgorithmException, NoValidPeersException {
		PoafsFile file = fileManager.getFile(fileId);
		SecretKey key = keyStore.unwrapKey(auth.getKeyForFile(fileId));
		
		byte[][] checkSums = divideIntoBlocks(file, bytes, key);		
		
		auth.updateFileLength(fileId, file.getNumBlocks());
		
		//updating checksums
		for (int i = 0; i < checkSums.length; i++) {
			if (!auth.updateCheckSum(file.getId(), i, checkSums[i])) {
				System.err.println("Error updating checksum");
			}
			
			//send the updated file to effected nodes
			Collection<String> peerIds = tracker.findBlock(fileId, i);

			for(String id:peerIds) {
				try {
					uploadBlockToPeer(id, fileId, file.getBlocks().get(i));
				} catch (IOException e) {
					e.printStackTrace();
				} catch (ProtocolException e) {
					e.printStackTrace();
				}
			}
		}
		System.out.println("Checksums updated");
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
				uploadBlockRandomly(fileId, block.getValue());
			}
		} else {
			System.out.println(fileId + " doesn't exist locally, can't upload");
		}
	}
	
	/**
	 * Upload a file block to a specific peer
	 * @param peerId The id of the peer.
	 * @param fileId The id of the file.
	 * @param block The file block to upload.
	 * @throws NoValidPeersException
	 */
	private void uploadBlockToPeer(String peerId, String fileId, FileBlock block) throws IOException, ProtocolException {
		long startTime = System.currentTimeMillis();
		
		InetSocketAddress addr = tracker.getHostForPeer(peerId);
		
		System.out.println("Uploading to " + peerId + " at " + addr.getHostName());
		//get the block off of the peer
		IPeer peer = new NetworkPeer(new Socket(addr.getHostName(), addr.getPort()), tracker, fileManager);
		
		System.out.println("Uploading block: " + fileId + ":" + block.getIndex());
		peer.sendBlock(fileId, block);
		
		long time = System.currentTimeMillis() - startTime;
		
		System.out.println("Upload for " + fileId + ":" + block.getIndex() + " took " + 
				time + "ms " + ((double)time)/block.getContent().length + "B/ms");
	}
	
	/**
	 * Upload a block to a random peer.
	 * @param fileId The id of the file the block is in.
	 * @param block The block to upload
	 * @throws NoValidPeersException
	 */
	private void uploadBlockRandomly(String fileId, FileBlock block) throws NoValidPeersException {
		long startTime = System.currentTimeMillis();
		
		Random r = new Random();
		Set<String> peers = tracker.getPeers().keySet();
		
		//remove the local peer from the set
		//peers.remove(Application.getPropertiesManager().getPeerId());
		
		String peerId = null;
		
		//loop until we get the block or run out of peers
		while (!peers.isEmpty()) {
			try {
				//choose a random peer
				peerId = peers.toArray(new String[peers.size()])[r.nextInt(peers.size())];
				
				uploadBlockToPeer(peerId, fileId, block);
				
				return;
			} catch (IOException e) {
				System.err.println(peerId + " was unreachable");
				
				peers.remove(peerId);
				
				if (peers.size() == 0) {
					break;
				}
			} catch (ProtocolException e) {
				System.err.println(e.getMessage());
				
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
	
	/**
	 * Share a file with another user.
	 * @param fileId The id of the file to share.
	 * @param userAddress The address of the user being shared with.
	 * @param recipientKey The recipients public key
	 * @param accessLevel The access level they will recieve.
	 * @return Whether the operaiton succeeded.
	 * @throws KeyException 
	 */
	public boolean share(String fileId, String userAddress, byte[] publicKey, int accessLevel) throws KeyException {
		
		byte[] wrappedKey = keyStore.rewrapKey(publicKey, auth.getKeyForFile(fileId));
		
		return auth.shareFile(fileId, userAddress, wrappedKey, accessLevel);
	}
	
	/**
	 * Revoke a users share on a file.
	 * @param fileId The id of the file.
	 * @param userAddress The user's ethereum address.
	 * @return Whether the opertion succeeded.
	 */
	public boolean revokeShare(String fileId, String userAddress) {
		return auth.revokeShare(fileId, userAddress);
	}
	
	/**
	 * Get a user's access level to a file.
	 * @param fileId The id of the file.
	 * @param userAddress The address of the user.
	 * @return The access level the user has on the file.
	 */
	public int getAccessLevel(String fileId, String userAddress) {
		return auth.getAccessLevel(fileId, userAddress);
	}
	
	/**
	 * Modify the access level a user has.
	 * @param fileId The id of the file they use.
	 * @param userAddress The address of the user.
	 * @param accessLevel The new access level.
	 * @return Whether the operation succeeded.
	 */
	public boolean modifyAccessLevel(String fileId, String userAddress, int accessLevel) {
		return auth.modifyAccessLevel(fileId, userAddress, accessLevel);
	}
	
	
	/**
	 * Remove a file from the network.
	 * @param fileId The id of the file to remove.
	 * @return Whether the operation succeeded.
	 */
	public boolean removeFile(String fileId) {
		//TODO tell peers with a copy  of the file that they can delete it.
		return auth.removeFile(fileId);
	}

	public KeyStore getKeyStore() {
		return keyStore;
	}

	public Credentials getCreds() {
		return creds;
	}

	public int getLengthOfFile(String fileId) {
		return auth.getFileLength(fileId);
	}
}
