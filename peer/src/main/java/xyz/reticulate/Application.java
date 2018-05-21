package xyz.reticulate;

import java.awt.Desktop;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Scanner;

import org.web3j.crypto.CipherException;
import org.web3j.crypto.Credentials;
import org.web3j.crypto.WalletUtils;

import xyz.reticulate.auth.EthUsers;
import xyz.reticulate.auth.IUsers;
import xyz.reticulate.exception.KeyException;
import xyz.reticulate.exception.ProtocolException;
import xyz.reticulate.file.tracking.FileInfo;
import xyz.reticulate.file.tracking.PeerInfo;
import xyz.reticulate.lib.Reference;
import xyz.reticulate.local.PropertiesManager;
import xyz.reticulate.spark.SparkServer;

public class Application {
	
	
	/**
	 * System input scanner.
	 */
	private static Scanner sc = new Scanner(System.in);
	
	/**
	 * The network this peer is connected to.
	 */
	private static Network net;

	/**
	 * The application properties manager.
	 */
	private static PropertiesManager pm = new PropertiesManager();
	
	public static void main(String[] args) throws IOException, ProtocolException, KeyException {
			String configPath = args.length > 0 ? args[0] : Reference.CONFIG_PATH;
			
			if (pm.loadProperties(configPath)) {
				System.out.println("Config Loaded");
				try {
					Credentials creds = WalletUtils.loadCredentials(pm.getWalletPass(), pm.getWalletPath());
					net = new CachedNetwork(creds, pm.getContractAddress());
					
					IUsers users = new EthUsers(creds, pm.getUserContractAddress());
					
					SparkServer web = new SparkServer(net, users);
					
					if (Desktop.isDesktopSupported()) {
					    try {
							Desktop.getDesktop().browse(new URI("http://local.reticulate.xyz:4567"));
						} catch (URISyntaxException e) {
							e.printStackTrace();
						}
					}
					
					ui();
				} catch (ProtocolException | CipherException e) {
					System.err.println(e.getMessage());
					e.printStackTrace();
				}
			}
		
	}
	
	private static void ui() {
		boolean exit = false;
		
		while (!exit) {
			System.out.print("> ");
			String command = sc.nextLine();
			switch (command) {
				case "list-files":
					try {
						listFiles(net.listFiles());
					} catch (ProtocolException e) {
						System.err.println(e.getMessage());
					}
					break;
				case "list-peers":
					listPeers(net.listPeers());
					break;
				case "load":
					printFile(net.fetchFile(sc.nextLine()));
					break;
				case "save":
					saveFile(net.fetchFile(sc.nextLine()));
					break;
				case "register-file":
					try {
						net.registerFile(sc.nextLine());
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case "write-file":
					try {
						String id = sc.nextLine();
						
						File orig = new File(sc.nextLine());
						int numOfBytes = (int) orig.length();
						FileInputStream inFile = new FileInputStream(orig);
						byte[] bytes = new byte[numOfBytes];
						inFile.read(bytes);
						inFile.close();
						System.out.println("File read");
						net.updateFileContent(id, bytes);
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case "upload":
					try {
						net.uploadFile(sc.nextLine());
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case "share":
					shareFile(sc.nextLine(), sc.nextLine(), sc.nextLine(), Integer.parseInt(sc.nextLine()));
					System.out.println("Complete.");
					break;
				case "revoke-share":
					net.revokeShare(sc.nextLine(), sc.nextLine());
					System.out.println("Complete.");
					break;
				case "get-access":
					System.out.println(net.getAccessLevel(sc.nextLine(), sc.nextLine()));
					break;
				case "modify-access":
					net.modifyAccessLevel(sc.nextLine(), sc.nextLine(), Integer.parseInt(sc.nextLine()));
					System.out.println("Complete.");
					break;
				case "file-info":
					fileInfo(sc.nextLine());
					break;
				case "address":
					System.out.println(net.getCreds().getAddress());
					break;
				case "public-key":
					System.out.println(Base64.getEncoder().encodeToString(net.getKeyStore().getPublicKey().getEncoded()));
					break;
				case "exit":
				case "quit":
					exit = true;
					break;
				default:
					System.out.println("Unrecognised Command");
				case "help":
					System.out.println("Available commands: ");
					System.out.println("address\t\tprint the address of the current user");
					System.out.println("public-key\tprint the public key of the current user (base 64)");
					System.out.println();
					System.out.println("list-files\tlist all files that this peer can access on the network");
					System.out.println("list-peers\tlist all peers that this peer can see");
					System.out.println("file-info\tget the meta data for a file, file id");
					System.out.println();
					System.out.println("load\t\tload a file from the network, the next line should be the if of the file you want to load");
					System.out.println("save\t\tload a file from the network and save it to the path \"file\", the next line should be the if of the file you want to load");
					System.out.println("register-file\tregister a file on the network, the next lines should be the path to the file, then the name the file should have on the network");
					System.out.println("write-file\tupdate the contents of a file on the network, the next lines should be the id of the file, then the path to the file");
					System.out.println("upload\t\tupload a registered file to other peers on the network, the next line should be the id of a file that is available locally");
					System.out.println();
					System.out.println("share\t\tshare a file with another user, fileId, recipient address, recipient public key (base 64), access level");
					System.out.println("revoke-share\trevoke a share a file with another user, file id, recipient address");
					System.out.println("get-access\tget a user's access level for a file, fileId, user address");
					System.out.println("modify-access\tmodify a user's access level for a file, fileId, user address, new access level");
			}
			
		}
		
		net.shutdown();
		
		sc.close();
		
		System.exit(0);
	}
	
	private static void listPeers(List<PeerInfo> peers) {
		for (PeerInfo p:peers) {
			System.out.println(p.getPeerId() + " " + p.getAddr());
		}
		System.out.println("End");
		
	}

	private static void saveFile(InputStream fileStream) {
		int in;
		try {
			FileOutputStream out = new FileOutputStream("file");
			List<Byte> contents = new ArrayList<Byte>();
			in = fileStream.read();
		
			while (in != -1) {
				contents.add((byte) (in - 128));
				
				in = fileStream.read();
			}
			
			System.out.println("File Read");
			
			byte[] bytes = new byte[contents.size()];
			for (int i = 0; i < contents.size(); i++) {
				bytes[i] = contents.get(i);
			}
			
			long startTime = System.currentTimeMillis();
			
			out.write(bytes);
			out.flush();
			
			long time = System.currentTimeMillis() - startTime;
			
			System.out.println("Write took " + time + "ms");
			
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private static void printFile(InputStream fileStream) {
		int in;
		try {
			List<Byte> contents = new ArrayList<Byte>();
			in = fileStream.read();
		
			while (in != -1) {
				contents.add((byte) in);
				
				in = fileStream.read();
			}
			
			System.out.println("File Read");
			
			byte[] bytes = new byte[contents.size()];
			for (int i = 0; i < contents.size(); i++) {
				bytes[i] = contents.get(i);
			}
			
			System.out.println(new String(bytes));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Print out the list of files.
	 * @param files The files to be listed.
	 */
	private static void listFiles(List<FileInfo> files) {		
		for (FileInfo f:files) {
			System.out.println(f.getFileId());
		}
		System.out.println("End");
	}
	
	private static void shareFile(String fileId, String recipientAddress, String recipientPublicKey, int accessLevel) {
		
		byte[] publicKey = Base64.getDecoder().decode(recipientPublicKey);
		
		try {
			net.share(fileId, recipientAddress, publicKey, accessLevel);
		} catch (KeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	private static void fileInfo(String fileId) {
		int length = net.getLengthOfFile(fileId);
		System.out.println(fileId + " " + length);
	}
	
	public static PropertiesManager getPropertiesManager() {
		return pm;
	}
}
