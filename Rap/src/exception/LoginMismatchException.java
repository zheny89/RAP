package exception;

public class LoginMismatchException extends Exception {
	
	public LoginMismatchException() {
		super();
	}
	
	public LoginMismatchException(String message) {
		super(message);
	}
}
