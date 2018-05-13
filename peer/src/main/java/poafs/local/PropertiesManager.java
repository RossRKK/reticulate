package poafs.local;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.NoSuchAlgorithmException;
import java.security.NoSuchProviderException;
import java.util.Properties;
import java.util.Scanner;
import java.util.UUID;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.WalletUtils;

import poafs.lib.Reference;

public class PropertiesManager {
	
	/**
	 * The id of this peer.
	 */
	private String peerId;
	
	/**
	 * The ID of the known peer.
	 */
	private String knownPeerId;
	
	/**
	 * The address of the known peer.
	 */
	private String knownPeerAddress;
	
	/**
	 * The port that the known peer works on.
	 */
	private int knownPeerPort;
	
	/**
	 * The path to the wallet file.
	 */
	private String walletPath;
	
	/**
	 * The password for the wallet.
	 */
	private String walletPass;
	
	private String contractAddress;
	
	/**
	 * The properties object.
	 */
	private Properties prop = new Properties();
	
	/**
	 * Load this peers properties from the properties file.
	 * @throws FileNotFoundException 
	 */
	public boolean loadProperties(String path) {
		boolean success = false;
		InputStream input = null;
		try {

			input = new FileInputStream(path);

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			peerId = prop.getProperty("peerId");
			
			knownPeerId = prop.getProperty("knownPeerId");
			knownPeerAddress = prop.getProperty("knownPeerAddress");
			knownPeerPort = Integer.parseInt(prop.getProperty("knownPeerPort"));
			
			walletPath = prop.getProperty("walletPath");
			walletPass = prop.getProperty("walletPass");
			contractAddress = prop.getProperty("contractAddress");
			
			success = true;
			
		} catch (FileNotFoundException e) { 
			System.err.println("Config File Not Found");
			setDefaultProperties(path);
			success = true;
		} catch (IOException ex) {
			System.err.println("Failed to Read Config File");
			ex.printStackTrace();
		} finally {
			if (input != null) {
				try {
					input.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			
			printProperties();
		}
		
		return success;
	}
	
	private void printProperties() {
		System.out.println("Peer ID: " + peerId);
		System.out.println("Known Peer ID: " + knownPeerId);
		System.out.println("Known Peer Address: " + knownPeerAddress + ":" + knownPeerPort);
	}
	
	/**
	 * Initialise the properties file with default properties.
	 */
	private void setDefaultProperties(String path) {
		OutputStream output = null;

		try {
			output = new FileOutputStream(path);

			// set the properties value
			peerId = UUID.randomUUID().toString();
			prop.setProperty("peerId", peerId);
			
			knownPeerId = "noodles";
			prop.setProperty("knownPeerId", knownPeerId);
			
			knownPeerAddress = "noodlesfor.one";
			prop.setProperty("knownPeerAddress", knownPeerAddress);
			
			knownPeerPort = Reference.DEFAULT_PORT;
			prop.setProperty("knownPeerPort", "" + knownPeerPort);
			
			contractAddress = "0xb8733F478bfd755BFBdc50fD2e16FC82245976B7";
			prop.setProperty("contractAddress", contractAddress);

			
			createWallet();
			prop.setProperty("walletPath", "" + walletPath);
			prop.setProperty("walletPass", walletPass);
			
			// save properties to project root folder
			prop.store(output, null);

		} catch (IOException io) {
			io.printStackTrace();
		} finally {
			if (output != null) {
				try {
					output.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	}
	
	/**
	 * Ask the user for the details to make a new wallet file.
	 * @throws IOException 
	 * @throws CipherException 
	 * @throws InvalidAlgorithmParameterException 
	 * @throws NoSuchProviderException 
	 * @throws NoSuchAlgorithmException 
	 */
	private void createWallet() {
		System.out.println("Creating a new Ethereum wallet");
		
		Scanner sc = new Scanner(System.in);
		
		String pass1 = null;
		String pass2 = null;
				
		do {
			if (pass1 != null && pass2 != null) {
				System.out.println("Passwords don't match");
			}
			
			System.out.print("Please enter a password for the new wallet: ");
			pass1 = sc.nextLine();
			
			System.out.print("Please re-enter your password: ");
			pass2 = sc.nextLine();
		} while (!pass1.equals(pass2));
		
		sc.close();
		
		walletPass = pass1;
		try {
			walletPath = WalletUtils.generateNewWalletFile(pass1, new File("").getAbsoluteFile(), true);
		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException
				| CipherException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public String getPeerId() {
		return peerId;
	}

	public String getKnownPeerId() {
		return knownPeerId;
	}

	public String getKnownPeerAddress() {
		return knownPeerAddress;
	}

	public int getKnownPeerPort() {
		return knownPeerPort;
	}

	public Properties getProp() {
		return prop;
	}

	public String getWalletPath() {
		return walletPath;
	}

	public String getWalletPass() {
		return walletPass;
	}

	public String getContractAddress() {
		return contractAddress;
	}
}
