package rap;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.WebClient;

import database.LinkConnector;
import database.Message;
import database.Worker;
import database.Worktime;


public class BasicApplication implements ApplicationConfiguration {

	public void configure(Application application) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(WebClient.PAGE_TITLE, "Система");
        application.addEntryPoint("/home", BasicEntryPoint.class, properties); 
        
        new Thread(new Runnable() {
			@Override
			public void run() {
			
				System.out.println("WORKER Table");
			List<Worker> userList = LinkConnector.getWorkers();
		        for (Worker usr : userList) {
		            System.out.println(usr.toString());
		        }
		        System.out.println("Size: " + userList.size());

		        System.out.println("WORKTIME Table");
		        List<Worktime> worktimeList = LinkConnector.getWorktimes();
		        for (Worktime wt : worktimeList) {
		        	System.out.println(wt.toString());
		        }
		        System.out.println("Size: " + worktimeList.size());
		        
		        System.out.println("MESSAGE Table");
		        List<Message> msgList = LinkConnector.getMessages(Message.Status.UNREAD);
		        for (Message msg : msgList) {
		            System.out.println(msg.toString());
		        }
		        System.out.println("Size: " + msgList.size());
			}
        	
        }).start();
    }
}
