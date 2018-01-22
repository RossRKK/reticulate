package poafs.cryto;

import java.security.InvalidKeyException;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import poafs.Application;
import poafs.exception.KeyException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;
import poafs.lib.Reference;

public class KeyStore implements IEncrypter, IDecrypter {
	
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	private Cipher rsa;
	private Cipher aes;
	
	public KeyStore() {
		KeyPair pair = buildRSAKeyPair();
		
		privateKey = pair.getPrivate();
		publicKey = pair.getPublic();
		
		try {
			rsa = Cipher.getInstance("RSA");
			aes = Cipher.getInstance("AES");
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchPaddingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * Generate a key pair.
	 * @return A key pair.
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPair buildRSAKeyPair() {
		try {
	        final int keySize = 2048;
	        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
	        keyPairGenerator.initialize(keySize);
	        return keyPairGenerator.genKeyPair();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
    }
	
	/**
	 * Unwrap the aes key.
	 * @return The aes key.
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	private SecretKey unwrapKey(byte[] wrappedKey) throws KeyException {
		try {
			rsa.init(Cipher.UNWRAP_MODE, privateKey);
			return (SecretKey)rsa.unwrap(wrappedKey, Reference.AES_CIPHER, Cipher.SECRET_KEY);
		} catch (Exception e) {
			throw new KeyException();
		}
	}

	/**
	 * Decrypt the given file block.
	 * @param block The file block to be decrypted.
	 * @return The un encrypted file block.
	 */
	@Override
	public FileBlock decrypt(EncryptedFileBlock block) throws KeyException  {
		try {
			SecretKey aesKey = unwrapKey(block.getWrappedKey());
			
			aes.init(Cipher.DECRYPT_MODE, aesKey);
			
			byte[] content = aes.doFinal(block.getContent());
			
			return new FileBlock(block.getOriginPeerId(), content, block.getIndex());
		} catch (Exception e) {
			throw new KeyException();
		}
	}
	
	@Override
	public EncryptedFileBlock encrypt(FileBlock block) throws KeyException {
		try {
			SecretKey aesKey = block.getKey();
			
			aes.init(Cipher.ENCRYPT_MODE, aesKey);
			
			byte[] encryptedContent = aes.doFinal(block.getContent());
			
			rsa.init(Cipher.WRAP_MODE, publicKey);
			byte[] wrappedKey = rsa.wrap(aesKey);
			
			return new EncryptedFileBlock(Application.getPropertiesManager().getPeerId(), encryptedContent, block.getIndex(), wrappedKey);
		} catch (Exception e) {
			throw new KeyException();
		}
	}

}
