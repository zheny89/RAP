package autoflagschanger;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import database.LinkConnector;


public class FlagsChanger extends Thread {
	static private FlagsChanger instance;
	private HashMap<String, LinkedList<FlagTask>> tasks;
	private boolean isOn;
	private final String SAVE_FILE_NAME = "tasksFile";
	
	private FlagsChanger(){
		tasks = new HashMap<>();
		isOn = false;
		setDaemon(true);
		loadTasks();
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
	public void run() {
		isOn = true;
		while(isOn){
			doTasks();
			Calendar calendar = Calendar.getInstance();
			int hour = calendar.get(Calendar.HOUR_OF_DAY);
			try{
				sleep(TimeUnit.HOURS.toMillis(24 - hour));
			}catch (InterruptedException e) {
				System.out.println("Завершение FlagChanger");
			}
		}
		saveTasks();
	}
	
	public void saveTasks(){
		try{
		File file = new File(SAVE_FILE_NAME);
		if(file.exists()) file.delete();
		file.createNewFile();
		FileWriter out = new FileWriter(file);
		for(Entry<String, LinkedList<FlagTask>> entry:tasks.entrySet()){
			for(FlagTask task:entry.getValue()){
				StringBuilder builder = new StringBuilder();
				builder.append(task.getWorkerID()).append(":")
				.append(task.getFlag()).append(":").append(task.getDate().toString()).append("\n");
				out.write(builder.toString());
				out.flush();
			}
		}
		out.close();
		}catch (Exception e) {
			System.err.println("Не смог сохранить задачи! "+e.toString());
		}
	}
	
	private void loadTasks(){
		try{
			File file = new File(SAVE_FILE_NAME);
			if(!file.exists()) return;
			BufferedReader in =new BufferedReader(new FileReader(file));
			String taskString;
			while((taskString = in.readLine()) != null){
				String[] strMass = taskString.split(":");
				long workerID = Long.valueOf(strMass[0]);
				short flag = Short.valueOf(strMass[1]);
				LocalDate date = LocalDate.parse(strMass[2]);
				addTask(new FlagTask(workerID, flag, date));
			}
			in.close();
		}catch (Exception e) {
			System.err.println("Не смог загрузить задачи! "+e.toString());
		}
	}
	
	public List<FlagTask> getSortedTasks(){
		if(tasks.size() == 0) return null;
		Set<String> dateStringList = tasks.keySet();
		List<LocalDate> listDate = new ArrayList<LocalDate>(dateStringList.size());
		for(String s:dateStringList)
			listDate.add(LocalDate.parse(s));
		Collections.sort(listDate);
		Collections.reverse(listDate);
		ArrayList<FlagTask> taskList = new ArrayList<>();
		for(LocalDate date:listDate){
			for(FlagTask task:tasks.get(date.toString()))
				taskList.add(task);
		}
		return taskList;
	}
	
	static public FlagsChanger getInstance(){
		if(instance == null)
			instance = new FlagsChanger();
		return instance;
	}
}
