package rap;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import SkypeBot.ActionListener;
import SkypeBot.JSONReader;
import SkypeBot.Skype;
import SkypeBot.User;
import autoflagschanger.FlagsChanger;
import database.LinkConnector;
import database.Worker;
import database.Worktime;

public class BasicBundleActivator implements BundleActivator {
	
    final static String botName = "MySkypeBot";
    final static String appID = "743a6fa8-996f-46b5-b0d8-b32b062a8a4d";
    final static String appPass = "TZ3obOposwmdCwFZa4ZvGrO";

	@Override
	public void start(BundleContext context) throws Exception {
		LinkConnector.connect();
		FlagsChanger.getInstance().start();
		runSkype();
		
		System.out.println("Connection to database established");
	}
	
	private void runSkype(){
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try{
				Skype skype = Skype.getInstance();
				skype.setDebug(true);
				skype.setSkypeServerPort(2342);
				skype.connect(appID, appPass);
				skype.setSkypeListener("contactRelationUpdate", new ActionListener() {
					
					@Override
					public void action(JSONReader arg0) {
						if(arg0.getField("action").equals("add")){
							try{
							String convID = skype.startConversation(null, arg0.getField("from.id"));
							skype.sendMessage(convID, "Пожалуйста, введите логин, чтобы мы могли вас идентифицировать.");
							}catch (IOException e) {
								System.err.println(e.toString());
							}
						}
						
					}
				});
				skype.setSkypeListener("message", new ActionListener() {
					
					@Override
					public void action(JSONReader arg0) {
						User user = skype.getUserByID(arg0.getField("from.id"));
						if(user == null) return;
						user.setName(arg0.getField("text"));
						try{
							skype.replayToMessage("Спасибо =)", arg0);
						}catch (IOException e) {
							System.err.println(e.toString());
						}
						skype.saveContacts();
						
					}
				});
				while(true){
					LocalDate date = LocalDate.now();
					
					List<Worktime> worktimes = LinkConnector.getWorktimes(date, date);
					List<Worker> presentWorkers = new ArrayList<Worker>(worktimes.size());
					for (Worktime wt : worktimes)
						presentWorkers.add(wt.getWorker());
					List<Worker> filteredWorkers  = LinkConnector.getWorkers();
					filteredWorkers.removeAll(presentWorkers);	
					
					User user = null;
					for(Worker worker:filteredWorkers){
						if(worker.getFlag() == Worker.Flags.NONE)
							if((user = skype.getUserByName(worker.getLogin())) != null){
								String convID = skype.startConversation(null, user.getId());
								skype.sendMessage(convID, "Доброе утро! Не забудьте отметиться на сайте\nhttp://127.0.0.1:10080/home");
							}
					}
					
					TimeUnit.MINUTES.sleep(1);
					LocalDateTime now = LocalDateTime.now();
					LocalDateTime tomorrow10AM = LocalDateTime.of(now.toLocalDate().plusDays(0), LocalTime.of(15, 20));
					long minutesSleep = now.until(tomorrow10AM, ChronoUnit.MINUTES);
					System.out.println(minutesSleep);
					TimeUnit.MINUTES.sleep(minutesSleep);
				}
				
			}catch (Exception e) {
				System.err.println(e.toString());
			}
			}
		});
		th.setDaemon(true);
		th.start();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Skype.getInstance().disconnect();
		LinkConnector.close();
		FlagsChanger.getInstance().saveTasks();
	}

}
