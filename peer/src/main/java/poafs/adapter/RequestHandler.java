package poafs.adapter;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

import poafs.Network;
import poafs.lib.Reference;

/**
 * This is a class that handles an incoming request.
 * @author rkk2
 *
 */
public class RequestHandler implements Runnable {
	
	/**
	 * The socket that handles this request.
	 */
	private Socket s;
	
	/**
	 * Create a new request handler.
	 * @param s The socket the client is connected on.
	 */
	public RequestHandler(Socket s, Network net) {
		this.s = s;
		this.net = net;
	}
	
	/**
	 * The input stream from the client.
	 */
	private BufferedReader in;
	
	/**
	 * The output stream to the client.
	 */
	private DataOutputStream out;
	
	/**
	 * The network to serve from.
	 */
	private Network net;

	/**
	 * The method that handles the request.
	 */
	@Override
	public void run() {
		String requestMessageLine = null;
		try {
			//setup the io streams
			in = new BufferedReader(new InputStreamReader(s.getInputStream()));
			out = new DataOutputStream(s.getOutputStream());
	
			//read in the request
			requestMessageLine = in.readLine();

			//this throws a null pointer on invalid requests
			StringTokenizer tokenizedLine = new StringTokenizer(requestMessageLine);
			
			//check that this uses a valid verb (for this server)
			if (tokenizedLine.nextToken().equalsIgnoreCase("GET")) {
				String fileId = tokenizedLine.nextToken();
				
				
				//remove the first /
				if (fileId.startsWith("/")){
					fileId = fileId.substring(1);
				}
				System.out.println("Web Request for " + fileId);
				//read in the requested file
				try {
					respondWithFile(fileId);
				} catch (FileNotFoundException e) {
					//if the file didn't load send a 404
					e.printStackTrace();
					error(404);
				} catch (Exception e) {
					//send a 500 on any other exception
					e.printStackTrace();
										
					error(500);
				}
				
				s.close();
			} else {
				//send a 400 if the request isn't a GET
				error(400);
			}
		} catch (IOException e) {
			//there was an IO exception, log it
			e.printStackTrace();
		} catch (NullPointerException e) {
			//someone tried to make an invalid request we can probably ignore it
		}
	}

	/**
	 * Send a file back to the client.
	 * @param fileId The name of the file to send.
	 * @param file The file as a byte array.
	 * @throws IOException
	 */
	private void respondWithFile(String fileId) throws IOException  {
		//write the headers
		out.writeBytes("HTTP/1.0 200 Document Follows\r\n");
		
		//write out the content length header
		//out.writeBytes("Content-Length: " +  + "\r\n");
		out.writeBytes("\r\n");
		
		//write out the file
		InputStream file = net.fetchFile(fileId);
		
		
		int index = 1;
		int currentByte = file.read();
		List<Byte> contents = new ArrayList<Byte>();
		while (currentByte != -1) {
			//out.writeByte(currentByte);
			contents.add((byte) (currentByte - 128));
			
			currentByte = file.read();
			
			if (index % Reference.BLOCK_SIZE == 0) {
				
				byte[] bytes = new byte[contents.size()];
				for (int i = 0; i < contents.size(); i++) {
					bytes[i] = contents.get(i);
				}
				
				contents = new ArrayList<Byte>();
				
				out.write(bytes);
				
				out.flush();
			}
			
			index ++;
		}
	
	}
	
	/**
	 * Respond with an error status code.
	 * @throws IOException
	 */
	private void error(int errorCode) throws IOException {
		//use the correct error message
		String errorMessage;
		switch (errorCode) {
			case 400:
				errorMessage = "Bad Request";
				break;
			case 404:
				errorMessage = "File Not Found";
				break;
			case 500:
			default:
				errorMessage = "Internal Server Error";
		}
		
		
		out.writeBytes("HTTP/1.0 " + errorCode + " " + errorMessage + "\r\n");

		out.flush();
	}
}