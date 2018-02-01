package poafs.cryto;

import poafs.exception.KeyException;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileBlock;

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
