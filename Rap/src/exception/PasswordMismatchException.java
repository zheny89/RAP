package exception;

public class PasswordMismatchException extends Exception {
	
	public PasswordMismatchException() {
		super();
	}
	
	public PasswordMismatchException(String message) {
		super(message);
	}
}
