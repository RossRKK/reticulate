package poafs;

import java.io.FileOutputStream;
import java.io.IOException;

public class FileGen {
	public static void main(String[] args) throws IOException {
		FileOutputStream out = new FileOutputStream("bytes");
		
		for (byte i = Byte.MIN_VALUE; i < Byte.MAX_VALUE; i++) {
			out.write(i);
		}
		
		out.flush();
		out.close();
	}
}
