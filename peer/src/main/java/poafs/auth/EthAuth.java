package poafs.auth;

import java.util.List;

import javax.crypto.SecretKey;

import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;

import poafs.cryto.IDecrypter;
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
	
	private Web3j web3j;
	
	public EthAuth() {
		web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/kMVN82WbWTrThVdoRsKH"));
	}

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
	public boolean registerFile(PoafsFile file, String fileName, SecretKey key) throws ProtocolException {
		// TODO Auto-generated method stub
		return false;
	}

}
