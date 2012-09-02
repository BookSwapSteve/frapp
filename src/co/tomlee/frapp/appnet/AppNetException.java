package co.tomlee.frapp.appnet;

public class AppNetException extends Exception {
	private static final long serialVersionUID = 1L;

	public AppNetException(String message) {
		super(message);
	}
	
	public AppNetException(Throwable cause) {
		super(cause);
	}
	
	public AppNetException(String message, Throwable cause) {
		super(message, cause);
	}
}
