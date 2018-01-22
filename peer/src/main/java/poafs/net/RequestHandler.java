package poafs.net;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketException;
import java.util.Base64;
import java.util.Scanner;

import poafs.Application;
import poafs.file.EncryptedFileBlock;
import poafs.file.FileManager;

public class RequestHandler implements Runnable {
	
	FileManager fm;
	/**
	 * The socket that the request is coming from.
	 */
	private Socket sock;
	
	//BufferedOutputStream out;
	private PrintWriter out;
	
	Scanner in;
	
	public RequestHandler(Socket socket, FileManager fm) throws SocketException {
		sock = socket;
		this.fm = fm;
		sock.setKeepAlive(true);
		
		try {
			//out = new BufferedOutputStream(sock.getOutputStream());
			out = new PrintWriter(sock.getOutputStream());
			in = new Scanner(sock.getInputStream());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void run() {
		try {
			
			//print some headers
			//out.write("POAFS Version 0.1\n".getBytes());
			out.println("POAFS Version 0.1");
			//out.write((Application.getPropertiesManager().getPeerId() + "\n").getBytes());
			out.println(Application.getPropertiesManager().getPeerId());
			
			out.flush();
			
			String versionDec = in.nextLine();
			String peerId = in.nextLine();
			
			//read all requests for file segments
			while (in.hasNextLine()) {
				String request = in.nextLine();
				
				//requests should take the form <file id>:<block index>
				String[] info = request.split(":");
				String fileId = info[0];
				int blockIndex = Integer.parseInt(info[1]);
				
				EncryptedFileBlock block = (EncryptedFileBlock) fm.getFileBlock(fileId, blockIndex);
				
				//out.write(block.getContent());
				out.println(Base64.getEncoder().encodeToString(block.getContent()));
				
				out.flush();
			}
			
			in.close();
		} finally {
			try {
				sock.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	
	private void println(String str) throws IOException {
		//out.write((str + "\r\n").getBytes());
		out.println(str);
		
		out.flush();
	}

}
