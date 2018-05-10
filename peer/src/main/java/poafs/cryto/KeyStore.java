package poafs.cryto;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.util.Arrays;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;

import poafs.Application;
import poafs.exception.KeyException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;
import poafs.lib.Reference;

public class KeyStore implements IEncrypter, IDecrypter {
	
	/**
	 * The RSA key sized used by this class.
	 */
    public final static int keySize = 2048;
	
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	private Cipher rsa;
	private Cipher aes;
	
	/**
	 * Generate an RSA key pair.
	 * @return A key pair.
	 * @throws NoSuchAlgorithmException
	 */
	public static KeyPair buildRSAKeyPair() {
		try {
	        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
	        keyPairGenerator.initialize(keySize);
	        return keyPairGenerator.genKeyPair();
		} catch (NoSuchAlgorithmException e) {
			return null;
		}
    }
	
	/**
	 * Construct an RSA key pair using an ethereum wallet as the seed.
	 * @return An RSA key pair.
	 */
	public static KeyPair buildRSAKeyPairFromWallet(Credentials credentials) {
		ECKeyPair ecKeys = credentials.getEcKeyPair();
		
		try {
			SecureRandom random = SecureRandom.getInstance("SHA1PRNG", "SUN");
			
			random.setSeed(Arrays.concatenate(ecKeys.getPublicKey().toByteArray(), ecKeys.getPrivateKey().toByteArray()));
			
			KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance("RSA");
	        keyPairGenerator.initialize(keySize, random);
	        
	        return keyPairGenerator.genKeyPair();
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (NoSuchProviderException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return null;
	}
	/**
	 * Construct a key store from a pre-defined key pair.
	 * @param pair A pair of RSA keys.
	 */
	public KeyStore(KeyPair pair) {
		
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
	 * Unwrap the aes key.
	 * @return The aes key.
	 * @throws NoSuchAlgorithmException 
	 * @throws InvalidKeyException 
	 */
	public SecretKey unwrapKey(byte[] wrappedKey) throws KeyException {
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
			
			return new FileBlock(content, block.getIndex());
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
			
			return new EncryptedFileBlock(encryptedContent, block.getIndex(), wrappedKey);
		} catch (Exception e) {
			throw new KeyException();
		}
	}
	
	public byte[] rewrapKey(byte[] recipientPublicKey, byte[] wrappedKey) throws KeyException {
		PublicKey pub;
		try {
			pub = KeyFactory.getInstance("RSA").generatePublic(new X509EncodedKeySpec(recipientPublicKey));
			SecretKey key = unwrapKey(wrappedKey);
			
			rsa.init(Cipher.WRAP_MODE, pub);
			
			return rsa.wrap(key);
		} catch (InvalidKeySpecException | NoSuchAlgorithmException | InvalidKeyException | IllegalBlockSizeException e) {
			throw new KeyException();
		}
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

}
