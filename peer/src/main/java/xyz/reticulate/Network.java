package xyz.reticulate;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;

import xyz.reticulate.auth.EthAuth;
import xyz.reticulate.auth.IAuthenticator;
import xyz.reticulate.cryto.KeyStore;
import xyz.reticulate.exception.KeyException;
import xyz.reticulate.exception.NoValidPeersException;
import xyz.reticulate.exception.ProtocolException;
import xyz.reticulate.file.EncryptedFileBlock;
import xyz.reticulate.file.FileBlock;
import xyz.reticulate.file.FileManager;
import xyz.reticulate.file.ReticulateFile;
import xyz.reticulate.file.tracking.FileInfo;
import xyz.reticulate.file.tracking.ITracker;
import xyz.reticulate.file.tracking.NetTracker;
import xyz.reticulate.file.tracking.PeerInfo;
import xyz.reticulate.file.tracking.Worker;
import xyz.reticulate.lib.Reference;
import xyz.reticulate.local.PropertiesManager;
import xyz.reticulate.peer.IPeer;
import xyz.reticulate.peer.PeerManager;

/**
 * This is a class that is designed to represent and entire Reticulate network.
 * @author rkk2
 *
 */
public class Network {
	
	protected Logger log = Logger.getLogger(Network.class.getSimpleName());

	/**
	 * The size of AES key that should be used.
	 */
	private static final int AES_KEY_SIZE = 256;
	
	/**
	 * The size of each block when its put onto this network.
	 */
	private int blockLength = Reference.BLOCK_SIZE;
	
	/**
	 * The authenticator being used by this peer.
	 */
	protected IAuthenticator auth;
	
	/**
	 * The key store used to encrypt and decrypt files.
	 */
	private KeyStore keyStore;
	
	/**
	 * The object that tracks the addresses of peers and files.
	 */
	private ITracker tracker;
	
	/**
	 * The user's ethereum wallet credentials.
	 */
	protected Credentials creds;
	
	/**
	 * The file system manager.
	 */
	private FileManager fileManager;
	
	/**
	 * The object used to track active connections to other peers.
	 */
	private PeerManager peerManager;
	
	private Worker worker;
	
	/**
	 * Create a new network object.
	 * @param creds The ethereum credentials this network should use.
	 * @param contractAddress The address of the Reticulate auth contract.
	 * @throws ProtocolException
	 * @throws IOException
	 * @throws CipherException
	 */
	public Network(Credentials creds, String contractAddress) throws ProtocolException, IOException, CipherException {
		this.creds = creds;
		log.setLevel(Level.INFO);
		
		log.log(Level.CONFIG, creds.getAddress());
		
		//automatically derive an rsa key pair from the ethereum wallet, this prevenets the need to store RSA keys seperately.
		keyStore = new KeyStore(KeyStore.buildRSAKeyPairFromWallet(creds));
		
		this.auth = new EthAuth(creds, contractAddress);
		fileManager = new FileManager(auth);
		tracker = new NetTracker();
		
		peerManager = new PeerManager(Reference.DEFAULT_PORT, tracker, fileManager);
		
		//start the local server
		new Thread(peerManager).start();
		
		//start up the worker thread
		worker = new Worker(this, auth, fileManager, tracker, Reference.DEFAULT_REDUNDANCY);
		
		try {
			startTraversal();
		} catch (ProtocolException | IOException e) {
			log.log(Level.SEVERE, "Failed to connect to known peer", e);
		}
	}
	
	/**
	 * Connect to the known peer.
	 * @throws IOException 
	 * @throws ProtocolException 
	 * @throws UnknownHostException 
	 */
	public void startTraversal() throws UnknownHostException, ProtocolException, IOException {
		PropertiesManager pm = Application.getPropertiesManager();
		
		if (pm.getKnownPeerId() != "") {
			
			startTraversal(pm.getKnownPeerId(), pm.getKnownPeerAddress(), pm.getKnownPeerPort());
		}
	}
	
	/**
	 * Set used to track which 
	 */
	private Set<String> traversed = new HashSet<String>();
	
	/**
	 * The maximum depth to traverse over.
	 */
	private static final int MAX_TRAVERSAL_DEPTH = 10;
	
	/**
	 * Start a network traversal by using the specified peer.
	 * @param peerId The id of the peer.
	 * @param host The peers host name.
	 * @param port The port the peer listens on.
	 */
	public void startTraversal(String peerId, String host, int port) {
		traversed = new HashSet<String>();
		
		traverse(new PeerInfo(peerId, new InetSocketAddress(host, port)), 0);

		log.fine("Completed network traversal");
	}
	
	/**
	 * Private method to traverse the network to get the current state.
	 * @param peerInfo The next peer to connect to.
	 * @param depth The current depth the traversal is at.
	 */
	private void traverse(PeerInfo peerInfo, int depth) {
		tracker.registerPeer(peerInfo.getPeerId(), peerInfo.getAddr());
		log.fine("Found peer: " + peerInfo.getPeerId());
		
		traversed.add(peerInfo.getPeerId());
		
		try {
			//open a connection to this peer
			IPeer peer = peerManager.openConnection(peerInfo.getPeerId());

			log.log(Level.FINER, "Connected to peer: " + peerInfo.getPeerId());
			
			if (peer != null) {
				//find all the peers it knows about
				Set<PeerInfo> knownPeers = peer.requestKnownPeers();
				
				//register them
				tracker.registerPeers(knownPeers);
				
				log.log(Level.FINEST, "Got known peers from: " + peerInfo.getPeerId());
				
				//register it's files
				tracker.registerFiles(peerInfo.getPeerId(), peer.requestAvailableFiles());
				
				log.log(Level.FINEST, "Got known files from: " + peerInfo.getPeerId());
				
				if (depth < MAX_TRAVERSAL_DEPTH) {
					//apply the same operation to all the peers it knows about
					for (PeerInfo pi:knownPeers) {
						//only look at a peer again if we haven't connected to it before
						if (!traversed.contains(pi.getPeerId())) {
							//recursively traverse the tree
							traverse(pi, depth + 1);
						} else {
							log.log(Level.FINE, "Reached maximum traversal depth");
						}
					}
				}
			}
		} catch (ProtocolException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Generate a new AES key to encrypt a file.
	 * @return The new AES key.
	 * @throws NoSuchAlgorithmException If the "AES" algorithm isn't recognised.
	 */
	public static SecretKey buildAESKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance(Reference.AES_CIPHER);
		keyGen.init(AES_KEY_SIZE);
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
	private byte[][] divideIntoBlocks(ReticulateFile file, byte[] bytes, SecretKey key) throws KeyException, NoSuchAlgorithmException, NoValidPeersException {
		
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
			/*MessageDigest crypt = MessageDigest.getInstance("SHA-1");
	        crypt.reset();
	        crypt.update(encrypted.getContent());
	        
	        checkSums[i] = crypt.digest();*/
			checkSums[i] = encrypted.getChecksum();
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
		log.info("File read");
		
		String id = UUID.randomUUID().toString();
		
		registerFile(id, bytes);
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
	public void registerFile(String id, byte[] bytes) throws NoSuchAlgorithmException, ProtocolException, KeyException, NoSuchAlgorithmException, IOException, NoValidPeersException {
		
		log.info("New file ID: " + id);
		
		SecretKey key = buildAESKey();
		
		ReticulateFile file = new ReticulateFile(id);
		
		byte[][] checkSums = divideIntoBlocks(file, bytes, key);
		
		log.fine("File " + id + " encrypted");
		
		fileManager.registerFile(file);
		file.saveFile();
		
		auth.registerFile(file, file.getNumBlocks(), ((EncryptedFileBlock)file.getBlocks().get(0)).getWrappedKey());
		
		log.fine("File " + id + " registered");
		
		//updating checksums
		updateChecksums(file, checkSums);
		log.fine("File " + id + " checksums updated");
		
		//return id;
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
		ReticulateFile file;
		//there may not be a local copy
		if (fileManager.getFile(fileId) == null) {
			file = new ReticulateFile(fileId);
		} else {
			file = fileManager.getFile(fileId);
		}
		SecretKey key = keyStore.unwrapKey(auth.getKeyForFile(fileId));
		
		byte[][] checkSums = divideIntoBlocks(file, bytes, key);		
		
		//save a trip to the ethereum network if we can
		if (auth.getFileLength(fileId) != file.getNumBlocks()) {
			auth.updateFileLength(fileId, file.getNumBlocks());
			
			log.fine("File " + fileId + " length updated to " + file.getNumBlocks());
		} else {
			log.fine("File " + fileId + " length updated unecessary, was already " + file.getNumBlocks());
		}
		
		fileManager.registerFile(file);
		
		updateChecksums(file, checkSums);
	}

	/**
	 * Update the checksums of a file to match the provided ones.
	 * @param file The file being updated.
	 * @param checkSums The new checksums.
	 */
	private void updateChecksums(ReticulateFile file, byte[][] checkSums) {
		Object waiter = new Object();
		boolean[] completed = new boolean[file.getNumBlocks()];
		
		//updating checksums
		for (int i = 0; i < checkSums.length; i++) {
			
			Integer index = new Integer(i);
			
			//this allows the upload to happen after the checksum has been updated
			Runnable updateAndUpload = () -> { 
				if (!auth.updateCheckSum(file.getId(), index.intValue(), checkSums[index.intValue()])) {
					log.severe("Error updating checksum for block " + file.getId() + ":" + index.intValue());
				} else {
				
					//send the updated file to effected nodes
					Collection<String> peerIds = tracker.findBlock(file.getId(), index.intValue());
		
					for(String id:peerIds) {
						try {
							uploadBlockToPeer(id, file.getId(), file.getBlocks().get(index));
						} catch (IOException e) {
							e.printStackTrace();
						} catch (ProtocolException e) {
							e.printStackTrace();
						}
					}
					log.fine("Finished update and upload of " + file.getId() + ":" + index.intValue());
					
				}
				//mark this block as completed
				completed[index.intValue()] = true;
				synchronized (waiter) {
					waiter.notifyAll();
				}
			};

			new Thread(updateAndUpload).start();
		}
		
		synchronized (waiter) {
			while (!allDone(completed)) {
				try {
					waiter.wait();
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			}
		}
		
		log.info("Completed updating checksums for " + file.getId());
	}

	private boolean allDone(boolean[] completed) {
		for (int i = 0; i < completed.length; i++) {
			if (!completed[i]) {
				return false;
			}
		}
		return true;
	}
	
	/**
	 * Upload a file to the network.
	 * @param fileId The id of the file to upload.
	 * @throws NoValidPeersException 
	 */
	public void uploadFile(String fileId) throws NoValidPeersException {
		ReticulateFile file = fileManager.getFile(fileId);
		
		if (file != null) {
			//upload each block
			for (Entry<Integer, FileBlock> block:file.getBlocks().entrySet()) {
				uploadBlockRandomly(fileId, block.getValue());
			}
		} else {
			log.warning(fileId + " doesn't exist locally, can't upload");
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
		
		log.finer("Uploading " + fileId + ":" + block.getIndex() + "to " + peerId);
		
		//get the block off of the peer
		//IPeer peer = new NetworkPeer(new Socket(addr.getHostName(), addr.getPort()), tracker, fileManager);
		IPeer peer = peerManager.openConnection(peerId);
		
		if (peer != null) {
			
			peer.sendBlock(fileId, block);
			
			long time = System.currentTimeMillis() - startTime;
			
			log.finest("Upload for " + fileId + ":" + block.getIndex() + " took " + 
					time + "ms " + ((double)time)/block.getContent().length + "B/ms");
		} else {
			log.severe("Error connecting to peer: " + peerId);
			throw new IOException("Error connecting to peer: " + peerId);
		}
	}
	
	/**
	 * Upload a block to a random peer.
	 * @param fileId The id of the file the block is in.
	 * @param block The block to upload
	 * @return The id of the peer it was uploaded to.
	 * @throws NoValidPeersException
	 */
	public String uploadBlockRandomly(String fileId, FileBlock block) throws NoValidPeersException {
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
				
				uploadBlockToPeer(peerId, fileId, block);
				
				return peerId;
			} catch (IOException e) {
				log.warning(peerId + " was unreachable");
				
				peers.remove(peerId);
				
				if (peers.size() == 0) {
					break;
				}
			} catch (ProtocolException e) {
				log.warning("Protocol exception with peer " + peerId + " : " + e.getMessage());
				
				peers.remove(peerId);
				
				if (peers.size() == 0) {
					break;
				}
			}
		}
		throw new NoValidPeersException();
	}
	
	/**
	 * Download a file block from a the network (doesn't include decrpytion).
	 * @param fileId The id of the file.
	 * @param block The index of the block.
	 * @return The downloaded block.
	 * @throws NoValidPeersException If no peer will serve the file.
	 */
	public FileBlock downloadBlock(String fileId, int block) throws NoValidPeersException {
		//fetch the file from the network
		
		long startTime = System.currentTimeMillis();
		
		Random r = new Random();
		Collection<String> peerIds = tracker.findBlock(fileId, block);
		String peerId = null;
		
		//loop until we get the block or run out of peers
		while (!peerIds.isEmpty()) {
			try {
				//choose a random peer
				peerId = peerIds.toArray(new String[peerIds.size()])[r.nextInt(peerIds.size())];
				
				//InetSocketAddress addr = t.getHostForPeer(peerId);
				
				IPeer peer = peerManager.openConnection(peerId);
				
				//get the block off of the peer
				//IPeer peer = new NetworkPeer(new Socket(addr.getHostName(), addr.getPort()), t, fm);
				
				log.finer("Requesting block: " + fileId + ":" + block + " from " + peer.getId());
				FileBlock out = peer.requestBlock(fileId, block);
				
				long time = System.currentTimeMillis() - startTime;
				
				log.finest("Fetch for " + fileId + ":" + block + " took " + 
						time + "ms " + ((double)time)/out.getContent().length + "B/ms");
				
				fileManager.registerBlock(fileId, out);
				tracker.registerTransfer(Application.getPropertiesManager().getPeerId(), fileId, block);
				
				
				return out;
			} catch (Exception e) {
				log.warning("Peer " + peerId + " caused " + e.getMessage());
				peerIds.remove(peerId);
				
				if (peerIds.size() == 0) {
					break;
				}
			}
		}
		throw new NoValidPeersException();
	}
	
	public List<FileInfo> listFiles() throws ProtocolException {
		return tracker.listFiles();
	}
	
	public InputStream fetchFile(String fileId) {
		return new ReticulateFileStream(fileId, 5, this, auth, keyStore, tracker, fileManager, peerManager);
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

	public void shutdown() {
		worker.stop();
		peerManager.closeConnections();
	}
}
