package poafs;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

import javax.swing.SwingUtilities;

import com.sun.jna.NativeLibrary;

import poafs.adapter.WebServer;
import poafs.exception.KeyException;
import poafs.exception.ProtocolException;
import poafs.file.FileMeta;
import poafs.gui.VideoPlayer;
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
			pm.loadProperties();
			
			net = new Network(args[0], Integer.parseInt(args[1]), Boolean.parseBoolean(args[2]));
			
			new Thread(new WebServer(8080, net)).start();
			
			//NativeLibrary.addSearchPath("vlc", "/usr/lib/vlc");
			new NativeDiscovery().discover();
	        
			
			ui();
		} catch (ProtocolException e) {
			System.err.println(e.getMessage());
			e.printStackTrace();
		}
	}
	
	private static void ui() {
		
		System.out.println("User Name: ");
		String user = sc.nextLine();
		
		System.out.println("Password: ");
		String pass = sc.nextLine();
		
		boolean authorised = false;
		try {
			authorised = net.login(user, pass);
		} catch (ProtocolException | KeyException e) {
			System.err.println(e.getMessage());
		}
		
		if (authorised) {
			System.out.println("Logged in.");
		
			boolean exit = false;
			
			while (!exit) {
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
					case "exit":
					case "quit":
						exit = true;
						break;
					default:
						System.out.println("Unrecognised Command");
				}
				
			}
		} else {
			System.out.println("Error authenticating");
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
	private static void listFiles(List<FileMeta> files) {		
		for (FileMeta f:files) {
			System.out.println(f.getFileName() + " " + f.getId());
		}
	}
	
	public static PropertiesManager getPropertiesManager() {
		return pm;
	}
}
