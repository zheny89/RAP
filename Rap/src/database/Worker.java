package database;

import java.util.Arrays;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Worker {
	
	public static interface Flags {
		static final short NONE = (short) 0;
		static final short TIME_OFF = (short) 1;
		static final short SICK_LEAVE = (short) 2;
		static final short VACATION = (short) 3;
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(unique=true)
	String login;
	//byte[] pswdHash;
	boolean isAdmin;
	String name;
	//short flag = Flags.NONE;
	//Date flagStartDate = null; // maybe should change to LocalDateTime
	
	public Worker() {
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getLogin() {
		return login;
	}

	public void setLogin(String login) {
		this.login = login;
	}

	/*public boolean assertPswdHash(byte[] pswdHash) {
		return Arrays.equals(this.pswdHash, pswdHash);
	}*/

	/*public void setPswdHash(byte[] pswdHash) {
		this.pswdHash = pswdHash;
	}*/

	public boolean isAdmin() {
		return isAdmin;
	}

	public void setAdmin(boolean isAdmin) {
		this.isAdmin = isAdmin;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	/*public short getFlag() {
		return flag;
	}
	
	public void setFlag(short flag, Date startDate) {
		this.flag = flag;
		this.flagStartDate = startDate;
	}*/
}
