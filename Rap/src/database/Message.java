package database;

import java.io.Serializable;
import java.util.Date;

public class Message implements Serializable {
	
	public static interface Status {
		static final short UNREAD = (short) 0;
		static final short READ = (short) 1;
	}
	
	int id;
	Date date;
	User sender;
	String message;
	short status;

	public Message() {}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public Date getDate() {
		return date;
	}

	public void setDate(Date date) {
		this.date = date;
	}

	public User getSender() {
		return sender;
	}

	public void setSender(User sender) {
		this.sender = sender;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

	public short getStatus() {
		return status;
	}

	public void setStatus(short status) {
		this.status = status;
	}
	
	
}
