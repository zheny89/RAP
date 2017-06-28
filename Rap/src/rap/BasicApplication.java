package rap;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.WebClient;

import database.DBConnector;
import database.Message;
import database.User;


public class BasicApplication implements ApplicationConfiguration {

    public void configure(Application application) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(WebClient.PAGE_TITLE, "Система");
        application.addEntryPoint("/home", BasicEntryPoint.class, properties);
        new Thread(new Runnable() {
			
			@Override
			public void run() {
		        try {
					DBConnector con = new DBConnector(DBConnector.EMBEDDED_DERBY_DRIVER, DBConnector.DERBY_PROTOCOL, "trol");
					con.connect();
					con.close();
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
		}).start();
        
    }
}