package poafs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import org.web3j.crypto.CipherException;

import poafs.adapter.WebServer;
import poafs.exception.KeyException;
import poafs.exception.ProtocolException;
import poafs.file.tracking.FileInfo;
import poafs.file.tracking.PeerInfo;
import poafs.gui.video.VideoPlayer;
import poafs.lib.Reference;
import poafs.local.PropertiesManager;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

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
		try {
			pm.loadProperties(args[0]);
			
			net = new Network(pm.getWalletPath(), pm.getWalletPass());
			
			new Thread(new WebServer(8080, net)).start();
			
			//NativeLibrary.addSearchPath("vlc", "/usr/lib/vlc");
			//new NativeDiscovery().discover();
	        
			ui();
		} catch (ProtocolException | CipherException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static void ui() {
		boolean exit = false;
		
		while (!exit) {
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
						net.registerFile(sc.nextLine(), sc.nextLine());
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				/*case "play":
					PoafsFileStream stream = net.fetchFile(sc.nextLine());
					SwingUtilities.invokeLater(new Runnable() {
			            @Override
			            public void run() {
			                new VideoPlayer(stream);
			            }
			        });
					break;*/
				case "upload":
					try {
						net.uploadFile(sc.nextLine());
					} catch (Exception e) {
						e.printStackTrace();
					}
					break;
				case "exit":
				case "quit":
					exit = true;
					break;
				default:
					System.out.println("Unrecognised Command");
				case "help":
					System.out.println("\nAvailable commands: ");
					System.out.println("list-files\tlist all files that this peer can access on the network");
					System.out.println("list-peers\tlist all peers that this peer can see");
					System.out.println("load\t\tload a file from the network, the next line should be the if of the file you want to load");
					System.out.println("save\t\tload a file from the network and save it to the path \"file\", the next line should be the if of the file you want to load");
					System.out.println("register-file\tregister a file on the network, the next lines should be the path to the file, then the name the file should have on the network");
					System.out.println("upload\t\tupload a registered file to other peers on the network, the next line should be the id of a file that is available locally");
			}
			
		}
		
		sc.close();
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
	
	public static PropertiesManager getPropertiesManager() {
		return pm;
	}
}
