package poafs.auth;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import poafs.cryto.KeyStore;
import poafs.file.FileMeta;
import poafs.file.PoafsFile;

/**
 * An autheticator that works by contacting a smart contract on the ethereum network.
 * @author rossrkk
 *
 */
public class EthAuth implements IAuthenticator {
	
	private Web3j web3j;
	
	private ReticulateAuth contract;
	
	private KeyStore localKeys;
	
	public EthAuth(Credentials credentials, KeyStore keys) {
		localKeys = keys;
		
		web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/kMVN82WbWTrThVdoRsKH"));
		
		contract = new ReticulateAuth("0x5e548A5437e53ceB0a8CE67b747C47A7F0A08315", web3j, 
				credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);
	}

	/**
	 * Returns the wrapped key for a file.
	 */
	@Override
	public byte[] getKeyForFile(String fileId) {
		try {
			return contract.getKeyForFile(fileId).send();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}


	@Override
	public FileMeta getInfoForFile(String fileId) {
		try {
			String fileName = contract.getFileName(fileId).send();
			int length = 1; //TODO not implemented in smart contract
			
			return new FileMeta(fileId, fileName, length);
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean registerFile(PoafsFile file, String fileName, byte[] wrappedKey) {
		try {
			contract.addFile(file.getId(), fileName, wrappedKey).send();
			
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}


}
