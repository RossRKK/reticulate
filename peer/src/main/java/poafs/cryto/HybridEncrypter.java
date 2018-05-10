package poafs.cryto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import poafs.Application;
import poafs.exception.KeyException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;
import poafs.lib.Reference;

public class HybridEncrypter implements IEncrypter {
	/**
	 * The rsa cipher object that is used.
	 */
	private Cipher rsa;
	
	public HybridEncrypter(PublicKey rsaKey) throws KeyException {
		try {
			rsa = Cipher.getInstance(Reference.RSA_CIPHER);
		
		
			rsa.init(Cipher.WRAP_MODE, rsaKey);
		} catch (NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException e) {
			throw new KeyException();
		}
	}
	
	/**
	 * Generate a new aes key.
	 * @return The new aes key.
	 */
	private SecretKey genAesKey() throws KeyException {
		try {
			KeyGenerator keyGen = KeyGenerator.getInstance(Reference.AES_CIPHER);
			keyGen.init(256);
			return keyGen.generateKey();
		} catch (Exception e) {
			throw new KeyException();
		}
	}
	
	/**
	 * Encrypt a file block.
	 * @param block The block to be encrypted.
	 * @return The resulting encrypted block.
	 * @throws KeyException 
	 */
	@Override
	public EncryptedFileBlock encrypt(FileBlock block) throws KeyException  {
		try {
			SecretKey aesKey = genAesKey();
			
			Cipher aes = Cipher.getInstance(Reference.AES_CIPHER);
			
			aes.init(Cipher.ENCRYPT_MODE, aesKey);
			
			byte[] encryptedContent = aes.doFinal(block.getContent());
			
			byte[] wrappedKey = rsa.wrap(aesKey);
			
			return new EncryptedFileBlock(encryptedContent, block.getIndex(), wrappedKey);
		} catch (Exception e) {
			throw new KeyException();
		}
	}
}
