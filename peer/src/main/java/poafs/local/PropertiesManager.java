package poafs.local;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.UUID;

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
	
	/**
	 * The properties object.
	 */
	private Properties prop = new Properties();
	
	/**
	 * Load this peers properties from the properties file.
	 * @throws FileNotFoundException 
	 */
	public void loadProperties(String path) {
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
			
		} catch (FileNotFoundException e) { 
			setDefaultProperties();
		} catch (IOException ex) {
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
	}
	
	private void printProperties() {
		System.out.println("Peer ID: " + peerId);
		System.out.println("Known Peer ID: " + knownPeerId);
		System.out.println("Known Peer Address: " + knownPeerAddress + ":" + knownPeerPort);
	}
	
	/**
	 * Initialise the properties file with default properties.
	 */
	private void setDefaultProperties() {
		OutputStream output = null;

		try {
			output = new FileOutputStream(Reference.CONFIG_PATH);

			// set the properties value
			peerId = UUID.randomUUID().toString();
			prop.setProperty("peerId", peerId);
			
			knownPeerId = "noodles";
			prop.setProperty("knownPeerId", knownPeerId);
			
			knownPeerAddress = "noodlesfor.one";
			prop.setProperty("knownPeerAddress", knownPeerAddress);
			
			knownPeerPort = Reference.DEFAULT_PORT;
			prop.setProperty("knownPeerPort", "" + knownPeerPort);

			walletPath = "wallet.json";
			prop.setProperty("walletPath", "" + walletPath);
			
			walletPass = "password";
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
}
