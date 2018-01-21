package poafs.local;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.security.PublicKey;
import java.util.Properties;
import java.util.UUID;

import poafs.lib.Reference;

public class PropertiesManager {
	
	/**
	 * The id of this peer.
	 */
	private String peerId;
	
	/**
	 * The properties object.
	 */
	private Properties prop = new Properties();
	
	/**
	 * Load this peers properties from the properties file.
	 * @throws FileNotFoundException 
	 */
	public void loadProperties() {
		InputStream input = null;
		try {

			input = new FileInputStream(Reference.CONFIG_PATH);

			// load a properties file
			prop.load(input);

			// get the property value and print it out
			peerId = prop.getProperty("peerId");

			System.out.println("Peer ID: " + peerId);
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
		}
	}
	
	/**
	 * Initialise the properties file with default properties.
	 */
	private void setDefaultProperties() {
		OutputStream output = null;

		try {
			output = new FileOutputStream(Reference.CONFIG_PATH);

			// set the properties value
			prop.setProperty("peerId", UUID.randomUUID().toString());

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
}
