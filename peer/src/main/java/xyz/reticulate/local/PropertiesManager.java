package xyz.reticulate.local;

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

import xyz.reticulate.lib.Reference;

/**
 * Class used to load and generate default properties.
 * @author rossrkk
 *
 */
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
	
	private String userContractAddress;
	
	private String webUsername;
	
	private String webPassword;
	
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
			userContractAddress = prop.getProperty("userContractAddress");
			
			webUsername = prop.getProperty("webUsername");
			webPassword = prop.getProperty("webPassword");
			
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
			
			knownPeerAddress = "beta.reticulate.xyz";
			prop.setProperty("knownPeerAddress", knownPeerAddress);
			
			knownPeerPort = Reference.DEFAULT_PORT;
			prop.setProperty("knownPeerPort", "" + knownPeerPort);
			
			//contractAddress = "0xb8733F478bfd755BFBdc50fD2e16FC82245976B7";
			contractAddress = "0xBD71F105f1f1Dc487550B71EF7f83e9ba6EDF680";
			prop.setProperty("contractAddress", contractAddress);
			
			userContractAddress = "0xCac3310E1f639262e4bFF7a1D891740e94e847Bf";
			prop.setProperty("userContractAddress", userContractAddress);

			
			Scanner sc = new Scanner(System.in);
			
			createWallet(sc);
			prop.setProperty("walletPath", "" + walletPath);
			prop.setProperty("walletPass", walletPass);
			
			/*askForUsernameAndPassword(sc);
			
			prop.setProperty("webUsername", webUsername);
			prop.setProperty("webPassword", webPassword);*/
			
			sc.close();
			
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
	private void createWallet(Scanner sc) {
		System.out.println("Creating a new Ethereum wallet");
		
		
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
		
		walletPass = pass1;
		try {
			walletPath = WalletUtils.generateNewWalletFile(pass1, new File("").getAbsoluteFile(), true);
		} catch (InvalidAlgorithmParameterException | NoSuchAlgorithmException | NoSuchProviderException
				| CipherException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private void askForUsernameAndPassword(Scanner sc) {
		
		System.out.println("For security reasons login credentials are required for using this node on the web.");
		System.out.println("Please enter a username: ");
		
		webUsername = sc.nextLine();
		
		System.out.println("Please enter a password: ");
		
		webPassword = sc.nextLine();
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

	public String getUserContractAddress() {
		return userContractAddress;
	}

	public String getWebUsername() {
		return webUsername;
	}

	public String getWebPassword() {
		return webPassword;
	}
}
