package xyz.reticulate.cryto;

import xyz.reticulate.exception.KeyException;
import xyz.reticulate.file.EncryptedFileBlock;
import xyz.reticulate.file.FileBlock;

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
