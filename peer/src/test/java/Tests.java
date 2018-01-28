import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.util.Random;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.junit.Test;

//import poafs.auth.DummyAuthenticator;
import poafs.cryto.HybridDecrypter;
import poafs.cryto.HybridEncrypter;
import poafs.exception.KeyException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;
import poafs.file.PoafsFile;

public class Tests {
	/**
	 * Generate a key pair.
	 * @return A key pair.
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPair buildRSAKeyPair() throws NoSuchAlgorithmException {
        final int keySize = 2048;
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
        keyPairGenerator.initialize(keySize);      
        return keyPairGenerator.genKeyPair();
    }
	
	/**
	 * Generate a key pair.
	 * @return A key pair.
	 * @throws NoSuchAlgorithmException
	 */
	public static SecretKey buildAESKeyPair() throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256); // for example
		return keyGen.generateKey();
    }
	
	/**
	 * Generate random data to run tests on.
	 * @param length The length of the random byte array.
	 * @return A random byte array.
	 */
	public static byte[] randomData(int length) {
		Random r = new Random();
		byte[] data = new byte[length];
		
		r.nextBytes(data);
		
		return data;
	}
	
	@Test
	public void hybridTest() throws KeyException, NoSuchAlgorithmException {
		KeyPair keys = buildRSAKeyPair();
		
		byte[] data = randomData(1024);
		
		HybridEncrypter e = new HybridEncrypter(keys.getPublic());
		HybridDecrypter d = new HybridDecrypter(keys.getPrivate());
		
		FileBlock input = new FileBlock("test", data, 0);
		
		EncryptedFileBlock encrypted = e.encrypt(input);
		FileBlock decrypted = d.decrypt(encrypted);
		
		for (int i = 0; i < data.length; i++) {
			assertEquals(data[i], decrypted.getContent()[i]);
		}
	}
	
	
	/*@Test
	public void peerTest() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, KeyException, ProtocolException {
		KeyPair keys = buildRSAKeyPair();
		
		IPeer p = new DummyPeer("test-peer", keys.getPublic(), keys.getPrivate());
		
		byte[] data = randomData(1024);
		
		FileBlock input = new FileBlock("test-block", data, 0);
		
		p.sendBlock("test-file", input);
		
		FileBlock returned = p.requestBlock("test-file", 0);
		
		for (int i = 0; i < data.length; i++) {
			assertEquals(data[i], returned.getContent()[i]);
		}
	}*/
	
	@Test
	public void saveTest() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException {
		byte[] data = "Hello, World!".getBytes();
		
		FileBlock input = new FileBlock("test", data, 0);
		
		PoafsFile file = new PoafsFile("normal");
		
		file.addBlock(input);
		
		file.saveFile();
	}
	
	@Test
	public void saveEncryptedTest() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException, IOException, KeyException {
		KeyPair keys = buildRSAKeyPair();
		
		byte[] data = "Hello, World!".getBytes();
		
		HybridEncrypter e = new HybridEncrypter(keys.getPublic());
		
		FileBlock input = new FileBlock("test", data, 0);
		
		EncryptedFileBlock encrypted = e.encrypt(input);
		
		PoafsFile file = new PoafsFile("encrypted");
		
		file.addBlock(encrypted);
		
		file.saveFile();
	}
	
	@Test
	public void blockingTest() {
		
	}
	
	
	/**
	 * This test basically just tests everything, it is completely ridiculous.
	 * @throws NoSuchAlgorithmException
	 * @throws InvalidKeyException
	 * @throws NoSuchPaddingException
	 * @throws IllegalBlockSizeException
	 * @throws BadPaddingException
	 */
	/*@Test
	public void networkDecryptTest() throws NoSuchAlgorithmException, InvalidKeyException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {
		//declare some stuff
		String peerId = "test-peer";
		String localPeerId = "local-peer";
		String fileId = "test-file";
		
		byte[] data = "Hello, World!".getBytes();
		
		//generate the keys
		KeyPair keys = buildRSAKeyPair();
		HybridEncrypter e = new HybridEncrypter(keys.getPublic());
		
		//start the server
		FileManager fm = new FileManager();
		Server s = new Server(Reference.DEFAULT_PORT, fm);
		Thread server = new Thread(s);
		server.start();

		//register the peer with the dummy autheticator
		//DummyAuthenticator auth = new DummyAuthenticator();
		auth.registerPeer(peerId, keys.getPrivate(), new InetSocketAddress("localhost", Reference.DEFAULT_PORT));
		IPeer p = new NetworkPeer(auth.getHostForPeer(peerId));
		
		//generate a dummy file
		FileBlock input = new FileBlock(localPeerId, data, 0);
		EncryptedFileBlock encrypted = e.encrypt(input);
		PoafsFile file = new PoafsFile(fileId);
		file.addBlock(encrypted);
		
		//register the file with a file manager
		fm.registerFile(file);
		
		//request the file
		p.openConnection();
		FileBlock returned = p.requestBlock(fileId, 0);
		
		//decrypt the file
		FileBlock decrypted = auth.getKeyForPeer(peerId).decrypt((EncryptedFileBlock) returned);
		
		//test that the decrypted returned file is correct
		for (int i = 0; i < decrypted.getContent().length; i++) {
			assertTrue(decrypted.getContent()[i] == input.getContent()[i]);
		}
		
		server.interrupt();
	}*/
}
