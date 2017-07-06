package rap;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.concurrent.TimeUnit;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import SkypeBot.ActionListener;
import SkypeBot.JSONReader;
import SkypeBot.Skype;
import SkypeBot.User;
import autoflagschanger.FlagTask;
import autoflagschanger.FlagsChanger;
import database.LinkConnector;

public class BasicBundleActivator implements BundleActivator {
	
    final static String botName = "MySkypeBot";
    final static String appID = "743a6fa8-996f-46b5-b0d8-b32b062a8a4d";
    final static String appPass = "TZ3obOposwmdCwFZa4ZvGrO";

	@Override
	public void start(BundleContext context) throws Exception {
		LinkConnector.connect();
		FlagsChanger.getInstance().start();
		Thread th = new Thread(new Runnable() {
			
			@Override
			public void run() {
				try{
				Skype skype = Skype.getInstance();
				skype.setSkypeServerPort(2342);
				skype.connect(appID, appPass);
				skype.setDebug(true);
				/*Calendar calendar = Calendar.getInstance();
				int hour = calendar.get(Calendar.HOUR_OF_DAY);
				int minuts = calendar.get(Calendar.MINUTE);
				minuts = 60 - minuts;
				if(hour < 10)
					hour = 10 - hour;
				else hour = 24 - hour + 10;*/
				while(true){
					ArrayList<User> list = skype.getUserList();
					for(User user:list){
						String convID = skype.startConversation(null, user.getId());
						skype.sendMessage(convID, String.format("Доброе утро! Уважаемый, %s, не забудьте отметиться на сайте", user.getName()));	
					}
					TimeUnit.MINUTES.sleep(1);
					//TimeUnit.HOURS.sleep(hour);
					LocalDateTime now = LocalDateTime.now();
					LocalDateTime tomorrow10AM = LocalDateTime.of(now.toLocalDate().plusDays(0), LocalTime.of(15, 20));
					long minutesSleep = now.until(tomorrow10AM, ChronoUnit.MINUTES);
					System.out.println(minutesSleep);
					TimeUnit.MINUTES.sleep(minutesSleep);
				}
				
			}catch (Exception e) {
				// TODO: handle exception
			}
			}
		});
		th.setDaemon(true);
		th.start();
		
		System.out.println("Connection to database established");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Skype.getInstance().disconnect();
		LinkConnector.close();
		FlagsChanger.getInstance().saveTasks();
	}

}
