package poafs.auth;

import java.math.BigInteger;

import org.web3j.abi.datatypes.Address;
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
	
	public EthAuth(Credentials credentials) {
		web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/kMVN82WbWTrThVdoRsKH"));
		
		contract = new ReticulateAuth("0x5e548A5437e53ceB0a8CE67b747C47A7F0A08315", web3j, 
				credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);
	}

	/**
	 * Returns the wrapped key for a file.
	 * @param fileId The files id
	 * @return The wrapped key for the file.
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


	/**
	 * Get a files meta data.
	 * @param fileId The files id.
	 * @return The files meta data
	 */
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

	/**
	 * Register a file to the network.
	 * @param file The file to be registered.
	 * @param fileName This files name.
	 * @param wrappedKey The wrapped key for the user of this node.
	 * @return Whether the action was successful.
	 */
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
	
	/**
	 * Get a user's access level to a file.
	 * @param fileId The file's id.
	 * @param user The user's address.
	 * @return The user's access level.
	 */
	@Override
	public int getAccessLevel(String fileId, String user) {
		try {
			return contract.getAccessLevel(fileId, user).send().intValueExact();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
		}
	}
	
	/**
	 * Remove a file from the network.
	 * @param The file's if
	 * @return Whether the aciton succeeded.
	 */
	@Override
	public boolean removeFile(String fileId) {
		try {
			contract.removeFile(fileId).send();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Share a file with another user.
	 * @param fileId The id of the file being shared.
	 * @param user The address of the recipient.
	 * @param recipientKey The recipients version of the wrapped key.
	 * @param accessLevel The access level granted to the recipient.
	 * @return Whether the action succeeded.
	 */
	@Override
	public boolean shareFile(String fileId, String user, byte[] recipientKey, int accessLevel) {
		try {
			contract.shareFile(fileId, user, recipientKey, BigInteger.valueOf(accessLevel)).send();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
	
	/**
	 * Revoke a user's permission to use a file.
	 * @param fileId The id of the file.
	 * @param user The user's address.
	 * @return Whether the aciton succeeded.
	 */
	@Override
	public boolean revokeShare(String fileId, String user) {
		try {
			contract.revokeShare(fileId, user).send();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	/**
	 * Modify the access level a user has to a file.
	 * @param fileId The id of the file.
	 * @param user The user's address/
	 * @param accessLevel The new access level.
	 * @return Whether the action succeeded.
	 */
	@Override
	public boolean modifyAccessLevel(String fileId, String user, int accessLevel) {
		try {
			//FIXME there's a typo in the contract
			contract.modifyAccesLevel(fileId, user, BigInteger.valueOf(accessLevel)).send();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}
