package exception;

public class EntryAlreadyExistsException extends Exception {
	
	public EntryAlreadyExistsException() {
		super();
	}
	
	public EntryAlreadyExistsException(String message) {
		super(message);
	}
}
