package autoflagschanger;

import java.time.LocalDate;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.concurrent.TimeUnit;

import database.LinkConnector;


public class FlagsChanger extends Thread {
	static private FlagsChanger instance;
	private HashMap<String, LinkedList<FlagTask>> tasks;
	
	private FlagsChanger(){
		tasks = new HashMap<>();
	}
	
	public void addTask(FlagTask task){
		String key = task.getDate().toString();
		LinkedList<FlagTask> list = tasks.get(key);
		if(list == null){
			list = new LinkedList<>();
			tasks.put(key, list);
			list.add(task);
		}else{
			boolean isExist = false;
			for(FlagTask targetTask:list){
				if(targetTask.equals(task)){
					isExist = true;
					break;
				}
			}
			if(!isExist)
				list.add(task);
		}
	}
	
	public void removeTask(FlagTask task){
		String key = task.getDate().toString();
		LinkedList<FlagTask> list = tasks.get(key);
		if(list == null) return;
		for(Iterator<FlagTask> iterator = list.iterator();iterator.hasNext();){
			FlagTask targetTask = iterator.next();
			if(targetTask.equals(targetTask)){
				iterator.remove();
				break;
			}
		}
	}
	
	private void doTasks(){
		LocalDate date = LocalDate.now();
		LinkedList<FlagTask> taskList = tasks.get(date.toString());
		if(taskList == null) return;
		for(FlagTask task:taskList)
			LinkConnector.updateWorkerFlag((int)task.getWorkerID(), task.getFlag(),null);
		tasks.remove(taskList);
	}
	
	@Override
	public synchronized void start() {
		doTasks();
		Calendar calendar = Calendar.getInstance();
		int hour = calendar.get(Calendar.HOUR_OF_DAY);
		try{
		sleep(TimeUnit.HOURS.toMillis(24 - hour));
		}catch (InterruptedException e) {
			throw new RuntimeException(e.toString());
		}
	}
	
	static public FlagsChanger getInstance(){
		if(instance == null)
			instance = new FlagsChanger();
		return instance;
	}
}
