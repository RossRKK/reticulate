package xyz.reticulate.exception;

/**
 * Thrown when the protocol is badly formed.
 * @author rkk2
 *
 */
public class ProtocolException extends Exception {

	private String message;
	
	public ProtocolException(String string) {
		this.message = string;
	}
	
	public String getMessage() {
		return message;
	}

}
