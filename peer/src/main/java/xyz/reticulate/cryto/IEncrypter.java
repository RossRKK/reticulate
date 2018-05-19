package xyz.reticulate.cryto;

import xyz.reticulate.exception.KeyException;
import xyz.reticulate.file.EncryptedFileBlock;
import xyz.reticulate.file.FileBlock;

public interface IEncrypter {
	/**
	 * Encrypt the content with this peers public key.
	 * @param input The input data.
	 * 
	 * @return The encrypted output data.
	 * @throws KeyException 
	 */
	EncryptedFileBlock encrypt(FileBlock block) throws KeyException;
}
