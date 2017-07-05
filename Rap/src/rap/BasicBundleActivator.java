package rap;

import java.io.IOException;
import java.time.LocalDate;
import java.util.Calendar;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import SkypeBot.ActionListener;
import SkypeBot.JSONReader;
import SkypeBot.Skype;
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
		new Thread(new Runnable() {
			
			@Override
			public void run() {
				try{
				Skype skype = Skype.getInstance();
				skype.setDebug(true);
				skype.setSkypeServerPort(2342);
				skype.setSkypeListener("message", new ActionListener() {
					
					@Override
					public void action(JSONReader arg0) {
						System.out.println("Пришло сообщение: "+arg0.getField("text"));
						try{
						skype.replayToMessage(arg0.getField("text"), arg0);
						}catch (Exception e) {
							// TODO: handle exception
						}
						
					}
				});
				skype.connect(appID, appPass);
				String convid = skype.startConversation(null, skype.getUserId("Киселев"));
				skype.sendMessage(convid, "Начало диалога");
				
			}catch (Exception e) {
				// TODO: handle exception
			}
			}
		}).start();
		
		System.out.println("Connection to database established");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		Skype.getInstance().disconnect();
		LinkConnector.close();
		FlagsChanger.getInstance().saveTasks();
	}

}
