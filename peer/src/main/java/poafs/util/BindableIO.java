package poafs.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.UUID;

public class BindableIO implements Runnable {
	
	/**
	 * The input stream.
	 */
	private Scanner in;
	
	/**
	 * The output stream.
	 */
	private PrintWriter out;
	
	/**
	 * The id that is currently binding the io streams.
	 */
	private String bindId = null;
	
	private Object bindWaiter = new Object();
	
	/**
	 * The currently read in line.
	 */
	private String line = null;
	
	private boolean shouldWaitForLine = true;
	private Object lineWaiter = new Object();
	
	private Queue<String> bindQueue = new LinkedList<String>();

	/**
	 * Create a new bindable io stream.
	 * @param in The underlying input stream.
	 * @param out The underlying output stream.
	 */
	public BindableIO(InputStream in, OutputStream out) {
		this.in = new Scanner(in);
		this.out = new PrintWriter(out);
		
		new Thread(this).start();
	}
	
	@Override
	public void run() {
		while (in.hasNextLine()) {
			try {
				shouldWaitForLine = true;
				
				line = in.nextLine();
				
				shouldWaitForLine = false;
				//notify that there is a new line read in
				synchronized (lineWaiter) {
					lineWaiter.notifyAll();
				}
				
				//wait until the next line is requested
				synchronized (in) {
					in.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
	}
	
	/**
	 * Wait for the bind id to match the provided ID and then read a line.
	 * @param id The bind id of the read operation being performed. (Provide null to be the default receiver).
	 * @return The read line.
	 * @throws InterruptedException
	 */
	public String nextLine(String id) {
		try {
			//wait for the bind ids to match
			
			//wait for a line to be read
			while (shouldWaitForLine || bindId != id) {
				synchronized (lineWaiter) {
					lineWaiter.wait();
				}
			}
			
			/*synchronized (bindWaiter) {
				//wait for the bind ids to match
				while (bindId != id) {
					bindWaiter.wait();
				}
			}*/
			
			//get the line
			String line = this.line;
			
			//tell the reader thread to read the next line
			synchronized (in) {
				in.notifyAll();
			}
			
			//String line = in.nextLine();
			shouldWaitForLine = true;
			
			return line;
		} catch (InterruptedException e) {
			return null;
		}
	}
	
	/**
	 * Write a line to the output stream.
	 * @param line The line to be written.
	 * @param id The bind id of the operation.
	 * @throws InterruptedException
	 */
	public void println(String line, String id) {
		try {
			synchronized (bindWaiter) {
				//wait for the bind ids to match
				while (bindId != id) {
					bindWaiter.wait();
				}
			}
			
			//print the line
			out.println(line);
			
			out.flush();
		} catch (InterruptedException e) {
			System.err.println("Interrupted");
		}
	}
	
	/**
	 * Write a line to the output stream.
	 * @param line The line to be written.
	 * @param id The bind id of the operation.
	 * @throws InterruptedException
	 */
	public void print(String line, String id) {
		try {
			synchronized (bindWaiter) {
				//wait for the bind ids to match
				while (bindId != id) {
					bindWaiter.wait();
				}
			}
			
			//print the line
			out.print(line);
			
			out.flush();
		} catch (InterruptedException e) {
			
		}
	}
	
	/**
	 * Bind the io stream.
	 * @return The binding id.
	 */
	public String bind() {
		String newBindId = UUID.randomUUID().toString();
		
		if (bindId == null) {
			bindId = newBindId;
			
			//notify that the bind id has changed
			synchronized (bindWaiter) {
				bindWaiter.notifyAll();
			}
		} else {
			bindQueue.add(newBindId);
		}
		
		return newBindId;
	}
	
	/**
	 * Unbind the stream.
	 * @param id The bind id.
	 */
	public void unbind(String id) {
		if (bindId == id) {
			bindId = bindQueue.poll();
			
			//notify that the bind id has changed
			synchronized (bindWaiter) {
				bindWaiter.notifyAll();
			}
		} else {
			((LinkedList<String>)bindQueue).remove(id);
		}
	}

}
