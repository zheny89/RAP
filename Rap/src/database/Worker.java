package database;

import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

@Entity
public class Worker {
	
	public static interface Flags {
		static final short NONE = (short) 0;

		static final short TIME_OFF = (short) 1;
		static final short SICK_LEAVE = (short) 2;
		static final short VACATION = (short) 3;
		static final short FIRED = (short) 4;
		
		static String toString(short flag) {
			switch(flag) {
			case TIME_OFF : return "В отгуле";
			case SICK_LEAVE: return "На больничном";
			case VACATION: return "В отпуске";
			case FIRED: return "Уволен";
			case NONE: return "(нет)";
			default: return "";
			}
		}
		
		static String toSmallString(short flag) {
			switch(flag) {
			case TIME_OFF : return "ОТГ";
			case SICK_LEAVE: return "Б";
			case VACATION: return "ОТ";
			case FIRED: return "--";
			default: return "?";
			}
		}
	}
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	@Column(unique=true)
	private String login;
	//byte[] pswdHash;
	private boolean isAdmin;
	private String name;
	private short flag;
	private Date flagStartDate; // maybe should change to LocalDateTime
	
	@OneToMany(mappedBy = "sender")
	private List<Message> messages = new ArrayList<Message>();
	@OneToMany(mappedBy = "worker")
	private List<Worktime> worktime = new ArrayList<Worktime>();
	
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
	public short getFlag() {
		return flag;
	}
	public void setFlag(short flag) {
		this.flag = flag;
	}
	public Date getFlagStartDate() {
		return flagStartDate;
	}
	public void setFlagStartDate(Date flagStartDate) {
		this.flagStartDate = flagStartDate;
	}
	public List<Message> getMessages() {
		return messages;
	}
	public void setMessages(List<Message> messages) {
		this.messages = messages;
	}
	public List<Worktime> getWorktime() {
		return worktime;
	}
	public void setWorktime(List<Worktime> worktime) {
		this.worktime = worktime;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + flag;
		result = prime * result + ((flagStartDate == null) ? 0 : flagStartDate.hashCode());
		result = prime * result + id;
		result = prime * result + (isAdmin ? 1231 : 1237);
		result = prime * result + ((login == null) ? 0 : login.hashCode());
		result = prime * result + ((name == null) ? 0 : name.hashCode());
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Worker other = (Worker) obj;
		if (flag != other.flag)
			return false;
		if (flagStartDate == null) {
			if (other.flagStartDate != null)
				return false;
		} else if (!flagStartDate.equals(other.flagStartDate))
			return false;
		if (id != other.id)
			return false;
		if (isAdmin != other.isAdmin)
			return false;
		if (login == null) {
			if (other.login != null)
				return false;
		} else if (!login.equals(other.login))
			return false;
		if (name == null) {
			if (other.name != null)
				return false;
		} else if (!name.equals(other.name))
			return false;
		return true;
	}
	@Override
	public String toString() {
		return "Worker [id=" + id + ", login=" + login + ", isAdmin=" + isAdmin + ", name=" + name + ", flag=" + flag
				+ ", flagStartDate=" + flagStartDate + "]";
	}
}
