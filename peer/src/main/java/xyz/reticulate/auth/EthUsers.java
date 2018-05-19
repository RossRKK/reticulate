package xyz.reticulate.auth;

import org.web3j.crypto.Credentials;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.http.HttpService;
import org.web3j.tx.Contract;
import org.web3j.tx.ManagedTransaction;

public class EthUsers implements IUsers {
	
	private Web3j web3j;
	
	private ReticulateUsers contract;
	
	public EthUsers(Credentials credentials, String contractAddress) {
		web3j = Web3j.build(new HttpService("https://rinkeby.infura.io/kMVN82WbWTrThVdoRsKH"));
		
		contract = new ReticulateUsers(contractAddress, web3j, 
				credentials, ManagedTransaction.GAS_PRICE, Contract.GAS_LIMIT);
	}

	@Override
	public String getAddressForUserName(String userName) {
		try {
			return contract.getAddressForUserName(userName).send();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getUserNameForAddress(String addr) {
		try {
			return contract.getUserNameForAddress(addr).send();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public byte[] getPublicKeyForUser(String addr) {
		try {
			return contract.getPublicKeyForUser(addr).send();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public byte[] getPublicKeyForUserByName(String userName) {
		try {
			return contract.getPublicKeyForUserByName(userName).send();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getRootDirForUser(String addr) {
		try {
			return contract.getRootDirForUser(addr).send();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public String getRootDirForUserByName(String userName) {
		try {
			return contract.getRootDirForUserByName(userName).send();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	@Override
	public boolean registerUser(String username, byte[] pubKey, String rootDir) {
		try {
			contract.registerUser(username, pubKey, rootDir).send();
			return true;
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean isUserNameTaken(String username) {
		try {
			return contract.isUserNameTaken(username).send();
		} catch (Exception e) {
			e.printStackTrace();
			//better to assume that it is taken
			return true;
		}
	}

}
