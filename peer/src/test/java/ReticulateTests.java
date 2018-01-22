import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.KeyGenerator;
import javax.crypto.SecretKey;

import org.junit.Test;
import org.web3j.crypto.CipherException;

import poafs.cryto.KeyStore;
import poafs.exception.KeyException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;

public class ReticulateTests {
	
	/**
	 * Generate an aes key.
	 * @return An AES key.
	 * @throws NoSuchAlgorithmException
	 */
	public static SecretKey buildAESKey() throws NoSuchAlgorithmException {
		KeyGenerator keyGen = KeyGenerator.getInstance("AES");
		keyGen.init(256); // for example
		return keyGen.generateKey();
    }
	
	@Test
	public void loadEthereumWallet() throws IOException, CipherException {
		KeyStore ks = new KeyStore();
	}
	
	@Test
	public void encryptDecrypt() throws KeyException, NoSuchAlgorithmException {
		KeyStore ks = new KeyStore();
		
		byte[] testData = {0, 1, 2, 3, 4};
		
		FileBlock block = new FileBlock("test-file", testData, 0);
		block.setKey(buildAESKey());
		
		EncryptedFileBlock encrypted = ks.encrypt(block);
		
		FileBlock result = ks.decrypt(encrypted);
		
		for (int i = 0; i < testData.length; i++) {
			assertEquals(testData[i], result.getContent()[i]);
		}
	}

}
