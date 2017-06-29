package database;

import lombok.Data;
import java.io.Serializable;
import java.sql.Date;

@SuppressWarnings("serial")
@Data
public class WorktimeKey implements Serializable {
	
	private Date day;
	private final int worker;

}
