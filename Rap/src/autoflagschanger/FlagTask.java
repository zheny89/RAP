package autoflagschanger;

import java.time.LocalDate;

public class FlagTask {
	private long workerID;
	private short flag;
	private LocalDate date;
	
	public FlagTask(long workerID, short flag,LocalDate date){
		this.workerID = workerID;
		this.flag = flag;
		this.date = date;
	}
	
	public short getFlag(){
		return flag;
	}
	
	public LocalDate getDate(){
		return date;
	}
	
	public long getWorkerID(){
		return workerID;
	}
	
	@Override
	public boolean equals(Object obj) {
		if(obj == null) return false;
		if(obj.getClass() != FlagTask.class) return false;
		FlagTask task = (FlagTask)obj;
		if(task.workerID != workerID) return false;
		if(task.flag != flag) return false;
		return task.date.equals(date);
	}
}
