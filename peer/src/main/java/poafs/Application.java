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
		if (args.length > 1) {
			Reference.port = Integer.parseInt(args[1]);
		}
		
		try {
			pm.loadProperties(args[0]);
			
			net = new Network(pm.getWalletPath(), pm.getWalletPass());
			
			//new Thread(new WebServer(8080, net)).start();
			
			//NativeLibrary.addSearchPath("vlc", "/usr/lib/vlc");
			new NativeDiscovery().discover();
	        
			ui();
		} catch (ProtocolException | CipherException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static void ui() {
		boolean exit = false;
		
		while (!exit) {
			System.out.print("> ");
			String command = sc.nextLine();
			switch (command) {
				case "ls":
					try {
						listFiles(net.listFiles());
					} catch (ProtocolException e) {
						System.err.println(e.getMessage());
					}
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
				case "play":
					PoafsFileStream stream = net.fetchFile(sc.nextLine());
					SwingUtilities.invokeLater(new Runnable() {
			            @Override
			            public void run() {
			                new VideoPlayer(stream);
			            }
			        });
					break;
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
			}
			
		}
		
		sc.close();
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
	private static void listFiles(FileInfo[] files) {		
		for (FileInfo f:files) {
			System.out.println(f.getFileId());
		}
	}
	
	public static PropertiesManager getPropertiesManager() {
		return pm;
	}
}
