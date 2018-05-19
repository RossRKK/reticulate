package xyz.reticulate.util;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.LinkedList;
import java.util.Queue;
import java.util.Scanner;
import java.util.UUID;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * A class that represents an input and output stream. Both streams can be bound to a specific thread by using a "bind id".
 * @author rossrkk
 *
 */
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
	
	/**
	 * An object that is notified when there is a bind change.
	 */
	private Object bindWaiter = new Object();
	
	/**
	 * An object that is notified when there is a line read.
	 */
	private Object lineWaiter = new Object();
	
	/**
	 * The queue of bind ids.
	 */
	private Queue<String> bindQueue = new LinkedList<String>();
	
	/**
	 * All the lines that have been read.
	 */
	private BlockingQueue<String> lineQueue = new LinkedBlockingQueue<String>();
	

	/**
	 * Create a new bindable io stream.
	 * @param in The underlying input stream.
	 * @param out The underlying output stream.
	 */
	public BindableIO(InputStream in, OutputStream out, String id) {
		this.in = new Scanner(in);
		this.out = new PrintWriter(out);

		new Thread(this).start();
	}
	
	/**
	 * Constantly read input lines.
	 */
	@Override
	public void run() {
		while (in.hasNextLine()) {
			try {
				lineQueue.put(in.nextLine());
				
				//notify that there is a new line read in
				synchronized (lineWaiter) {
					lineWaiter.notifyAll();
				}
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
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
			while (lineQueue.isEmpty() || bindId != id) {
				synchronized (lineWaiter) {
					lineWaiter.wait();
				}
			}
			
			//get the line
			String line = lineQueue.take();
			
			return line;
		} catch (InterruptedException e) {
			e.printStackTrace();
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
		} catch (InterruptedException e) {
			e.printStackTrace();
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
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * Flush the output stream.
	 */
	public void flush() {
		out.flush();
	}
	
	/**
	 * Bind the io stream.
	 * @return The binding id.
	 */
	public synchronized String bind() {
		//assign a new bind id
		String newBindId = UUID.randomUUID().toString();
		
		if (bindId == null) {
			//if nothing is bound use the new bind id
			bindId = newBindId;
			
			
			//notify that the bind id has changed
			synchronized (bindWaiter) {
				bindWaiter.notifyAll();
			}
		} else {
			//otherwise add the bind to the queue
			bindQueue.add(newBindId);
		}
		
		//return the assigned bind id
		return newBindId;
	}
	
	
	/**
	 * Unbind the stream.
	 * @param id The bind id.
	 */
	public synchronized void unbind(String id) {
		//if the bind ids match
		if (bindId == id) {
			//flush the output stream for safety
			out.flush();
			//get the next bind id
			bindId = bindQueue.poll();
			
			
			//notify that the bind id has changed
			synchronized (bindWaiter) {
				bindWaiter.notifyAll();
			}
		} else {
			//otherwise remove the id from elsewhere in the queue
			((LinkedList<String>)bindQueue).remove(id);
		}
	}

}
