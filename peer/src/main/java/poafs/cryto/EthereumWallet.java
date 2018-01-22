package poafs.cryto;

import java.io.IOException;
import java.math.BigInteger;
import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.ECPublicKey;
import java.security.spec.ECParameterSpec;
import java.security.spec.ECPrivateKeySpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;

import org.bouncycastle.asn1.x9.ECNamedCurveTable;
import org.bouncycastle.asn1.x9.X9ECParameters;
import org.bouncycastle.jce.spec.ECNamedCurveSpec;
import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.ECKeyPair;
import org.web3j.crypto.WalletUtils;

import poafs.exception.KeyException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;
import poafs.lib.Reference;

public class EthereumWallet implements IEncrypter, IDecrypter {
	
	private static final String CURVE_NAME = "secp256k1";

	private Credentials credentials;
	
	private PrivateKey privateKey;
	private PublicKey publicKey;
	
	private Cipher dsa;
	private Cipher aes;
	
	public EthereumWallet(String path, String pass) throws IOException, CipherException {
		credentials = WalletUtils.loadCredentials(pass, path);
		
		ECKeyPair keyPair = credentials.getEcKeyPair();
		
		privateKey = decodePrivateKey(keyPair.getPrivateKey(), CURVE_NAME);
		publicKey = decodePublicKey(keyPair.getPublicKey());
		
		try {
			dsa = Cipher.getInstance("DSA");
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
	 * Convert the Big int in the EC key pair into a usable private key.
	 * @param s
	 * @param curveName
	 * @return The private key.
	 */
	private static PrivateKey decodePrivateKey(BigInteger s, String curveName) {
	    X9ECParameters ecCurve = ECNamedCurveTable.getByName(curveName);
	    ECParameterSpec ecParameterSpec = new ECNamedCurveSpec(curveName, ecCurve.getCurve(), ecCurve.getG(), ecCurve.getN(), ecCurve.getH(), ecCurve.getSeed());
	    ECPrivateKeySpec privateKeySpec = new ECPrivateKeySpec(s, ecParameterSpec);
	    try {
	        KeyFactory keyFactory = KeyFactory.getInstance("EC");
	        return keyFactory.generatePrivate(privateKeySpec);
	    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
	        e.printStackTrace();
	        return null;
	    }
	}
	
	/**
	 * Decode the publc key.
	 * @param pubKey The encoded public key.
	 * @return The decoded public key.
	 */
	private static PublicKey decodePublicKey(BigInteger pubKey) {
		X509EncodedKeySpec ks = new X509EncodedKeySpec(pubKey.toByteArray());
	    try {
	    	KeyFactory kf = java.security.KeyFactory.getInstance("EC");
	         return (ECPublicKey)kf.generatePublic(ks);
	    } catch (NoSuchAlgorithmException | InvalidKeySpecException e) {
	        e.printStackTrace();
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
			return (SecretKey)dsa.unwrap(wrappedKey, Reference.AES_CIPHER, Cipher.SECRET_KEY);
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
		//TODO 
		
		return null;
	}

}
