package database;

import java.io.Serializable;
import java.sql.Date;

@SuppressWarnings("serial")
public class WorktimeKey implements Serializable {
	
	private Date day;
	private final int worker;
	
	public WorktimeKey(int workerId) {
		this.worker = workerId;
	}

	public Date getDay() {
		return day;
	}

	public void setDay(Date day) {
		this.day = day;
	}

	public int getWorker() {
		return worker;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((day == null) ? 0 : day.hashCode());
		result = prime * result + worker;
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
		WorktimeKey other = (WorktimeKey) obj;
		if (day == null) {
			if (other.day != null)
				return false;
		} else if (!day.equals(other.day))
			return false;
		if (worker != other.worker)
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "WorktimeKey [day=" + day + ", worker=" + worker + "]";
	}
	
	
}
