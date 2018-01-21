package poafs.adapter;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import poafs.Network;

public class WebServer implements Runnable {

	private int port;
	
	private Network net;
	
	public WebServer(int port, Network net) {
		this.port = port;
		this.net = net;
	}
	
	@Override
	public void run() {
		//set up the server socket
		ServerSocket ss;
			try {
				ss = new ServerSocket(port);
			
			
			//loop as long as the server socket is open
			while (!ss.isClosed()) {
				//accept the new connection
				Socket s = ss.accept();
		
				//create a new request handler
				RequestHandler rh = new RequestHandler(s, net);
				
				//start a new thread using that handler
				Thread t = new Thread(rh);
				t.start();
			}
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
