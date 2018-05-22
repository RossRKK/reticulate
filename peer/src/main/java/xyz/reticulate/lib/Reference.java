package xyz.reticulate.lib;

public class Reference {
	/**
	 * The default port number poafs runs on.
	 */
	public final static int DEFAULT_PORT = 5964;
	
	/**
	 * The size that each block should be.
	 */
	public static final int BLOCK_SIZE = 1048576;

	public static final String CONFIG_PATH = "config.properties";

	public static final String FILE_PATH = "files";
	
	public static final String RSA_CIPHER = "RSA";
	
	public static final String AES_CIPHER = "AES";

	public static final int DEFAULT_REDUNDANCY = 2;

	public static final int ADMIN = 3;
	public static final int WRITE = 2;
	public static final int READ = 1;
	
}
