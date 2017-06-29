package database;

import lombok.Data;

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
@Data
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
}
