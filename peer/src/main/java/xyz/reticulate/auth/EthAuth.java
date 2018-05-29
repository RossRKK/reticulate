package xyz.reticulate.auth;

import java.math.BigInteger;
import java.util.Base64;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import org.web3j.abi.datatypes.Address;
import org.web3j.abi.datatypes.Type;
import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

import xyz.reticulate.file.ReticulateFile;

/**
 * An autheticator that works by contacting a smart contract on the ethereum network.
 * @author rossrkk
 *
 */
public class EthAuth implements IAuthenticator {
	
	private Web3j web3j;
	
	private ReticulateAuth contract;
	
	private Logger log = Logger.getLogger(EthAuth.class.getSimpleName());
	
	public EthAuth(Credentials credentials, String contractAddress) {
		log.setLevel(Level.INFO);
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
			log.log(Level.SEVERE, "Error getting key for file " + fileId, e);
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
			log.log(Level.SEVERE, "Error getting length of file " + fileId, e);
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
	public boolean registerFile(ReticulateFile file, int length, byte[] wrappedKey) {
		try {
			contract.addFile(file.getId(), wrappedKey, BigInteger.valueOf(length)).send();
			
			return true;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error registering file " + file.getId(), e);
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
			log.log(Level.SEVERE, "Error getting access level for user " + user + "on  file " + fileId, e);
			return 0;
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
			log.log(Level.SEVERE, "Error removing file " + fileId, e);
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
			log.log(Level.SEVERE, "Error sharing file " + fileId + " with user " + user + " and access level " + accessLevel, e);
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
			log.log(Level.SEVERE, "Error revoking user's (" + user + ") share for file " + fileId, e);
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
			log.log(Level.SEVERE, "Error modifiying access level for file " + fileId + " and user " + user + " to level " + accessLevel, e);
			return false;
		}
	}

	@Override
	public boolean updateFileLength(String fileId, int newLength) {
		try {
			contract.updateFileLength(fileId, BigInteger.valueOf(newLength)).send();
			return true;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error updating file length for file " + fileId, e);
			return false;
		}
	}

	@Override
	public boolean updateCheckSum(String fileId, int blockIndex, byte[] checkSum) {
		try {
			contract.updateCheckSum(fileId, BigInteger.valueOf(blockIndex), checkSum).send();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			log.log(Level.SEVERE, "Error updating checksum for block " + fileId + ":" + blockIndex, e);
			return false;
		}
	}

	@Override
	public boolean compareCheckSum(String fileId, int blockIndex, byte[] checkSum) {
		/*try {
			return contract.compareCheckSum(fileId, BigInteger.valueOf(blockIndex), checkSum).send();
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}*/
		byte[] correctSum = getCheckSum(fileId, blockIndex);
		boolean correct = true;
		//doing this locally prevents uncessary execution on the blockchain
		if (correctSum.length == checkSum.length) {
			for (int i = 0; i < correctSum.length; i++) {
				if (correctSum[i] != checkSum[i]) {
					correct = false;
					break;
				}
			}
		} else {
			correct = false;
		}
		
		if (!correct) {
			log.finer("Failed checksum comparison for " + fileId + ":" + blockIndex + ", Expected " + Base64.getEncoder().encodeToString(checkSum) + " to equal " + Base64.getEncoder().encodeToString(correctSum));
		}
		
		return correct;
	}

	@Override
	public byte[] getCheckSum(String fileId, int blockIndex) {
		try {
			return contract.getCheckSum(fileId, BigInteger.valueOf(blockIndex)).send();
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error getting checksum for file " + fileId, e);
			return null;
		}
	}
	
	@Override
	public List<String> getAllUsersWithAccess(String fileId) {
		try {
			List<Address> addrs = contract.getAllUsersWithAccess(fileId).send();
			List<String> out = addrs.parallelStream().map(addr -> addr.getValue()).collect(Collectors.toList());
			return out;
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error getting users for file " + fileId, e);
			return null;
		}
	}
}
