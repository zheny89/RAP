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
        properties.put(WebClient.PAGE_TITLE, "Hello RAP");
        application.addEntryPoint("/home", BasicEntryPoint.class, properties);
        
        try {
        	try {
				DBConnector conn = new DBConnector(DBConnector.EMBEDDED_DERBY_DRIVER, DBConnector.DERBY_PROTOCOL, "myDB");
				
				conn.connect();
				
				User lolUser = conn.getUser("lol");
				
				conn.close();
				
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			System.out.println("");
		} catch (InstantiationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }

}