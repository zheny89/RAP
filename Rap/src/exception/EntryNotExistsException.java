package exception;

public class EntryNotExistsException extends Exception {
	
	public EntryNotExistsException() {
		super();
	}
	
	public EntryNotExistsException(String message) {
		super(message);
	}
}
