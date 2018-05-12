package poafs.auth;

import java.math.BigInteger;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tuples.generated.Tuple2;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

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
	
	public EthAuth(Credentials credentials, String contractAddress) {
		web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/kMVN82WbWTrThVdoRsKH"));
		
		contract = new ReticulateAuth(contractAddress, web3j, 
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
	public int getFileLength(String fileId) {
		try {
			return contract.getFileLength(fileId).send().intValue();
		} catch (Exception e) {
			e.printStackTrace();
			return -1;
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
	public boolean registerFile(PoafsFile file, int length, byte[] wrappedKey) {
		try {
			contract.addFile(file.getId(), wrappedKey, BigInteger.valueOf(length)).send();
			
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
	 * @return Whether the action succeeded.
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
			contract.modifyAccessLevel(fileId, user, BigInteger.valueOf(accessLevel)).send();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean updateFileLength(String fileId, int newLength) {
		try {
			contract.updateFileLength(fileId, BigInteger.valueOf(newLength)).send();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean updateCheckSum(String fileId, int blockIndex, byte[] checkSum) {
		try {
			//this is done asynchronously because loads of them happen at once
			contract.updateCheckSum(fileId, BigInteger.valueOf(blockIndex), checkSum).sendAsync();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean compareCheckSum(String fileId, int blockIndex, byte[] checkSum) {
		try {
			return contract.compareCheckSum(fileId, BigInteger.valueOf(blockIndex), checkSum).send();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		/*byte[] correctSum = getCheckSum(fileId, blockIndex);
		//doing this locally prevents uncessary execution on the blockchain
		if (correctSum.length == checkSum.length) {
			
			for (int i = 0; i < correctSum.length; i++) {
				if (correctSum[i] != checkSum[i]) {
					return false;
				}
			}
			
			return true;
		} else {
			return false;
		}*/
	}

	@Override
	public byte[] getCheckSum(String fileId, int blockIndex) {
		try {
			return contract.getCheckSum(fileId, BigInteger.valueOf(blockIndex)).send();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
}
