package poafs.cryto;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import poafs.exception.KeyException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;

public interface IDecrypter {
	/**
	 * Decrypt a file block.
	 * @param input The encrypted input data.
	 * 
	 * @return The decrypted output data.
	 * @throws KeyException 
	 */
	FileBlock decrypt(EncryptedFileBlock block) throws KeyException;
}
