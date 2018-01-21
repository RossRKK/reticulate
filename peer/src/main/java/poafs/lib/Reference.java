package poafs.lib;

public class Reference {
	/**
	 * The default port number poafs runs on.
	 */
	public static final int DEFAULT_PORT = 5964;
	
	/**
	 * The size that each block should be.
	 */
	//public static final int BLOCK_SIZE = 1024;
	public static final int BLOCK_SIZE = 1048576;
	//public static final int BLOCK_SIZE = 4;

	public static final String CONFIG_PATH = "config.properties";

	public static final String FILE_PATH = "files";
	
	public static final String RSA_CIPHER = "RSA";
	
	public static final String AES_CIPHER = "AES";
}
