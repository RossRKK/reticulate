package poafs.auth;

import java.net.InetSocketAddress;
import java.util.List;

import poafs.cryto.IDecrypter;
import poafs.cryto.IEncrypter;
import poafs.exception.KeyException;
import poafs.exception.ProtocolException;
import poafs.file.FileMeta;
import poafs.file.PoafsFile;

/**
 * An autheticator that works by contacting a smart contract on the ethereum network.
 * @author rossrkk
 *
 */
public class EthAuth implements IAuthenticator {

	@Override
	public IDecrypter getKeyForFile(String fileId) throws ProtocolException, KeyException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<FileMeta> listFiles() throws ProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public FileMeta getInfoForFile(String fileId) throws ProtocolException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public boolean registerFile(PoafsFile file, String fileName) throws ProtocolException {
		// TODO Auto-generated method stub
		return false;
	}

}
