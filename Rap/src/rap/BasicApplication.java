package rap;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.Persistence;
import javax.persistence.Query;

import org.eclipse.rap.rwt.application.Application;
import org.eclipse.rap.rwt.application.ApplicationConfiguration;
import org.eclipse.rap.rwt.client.WebClient;

import database.DBConnector;
import database.Message;
import database.Worker;


public class BasicApplication implements ApplicationConfiguration {

	private static final String PERSISTENCE_UNIT_NAME = "workers";
    private static EntityManagerFactory factory;
    
    public void configure(Application application) {
        Map<String, String> properties = new HashMap<String, String>();
        properties.put(WebClient.PAGE_TITLE, "Система");
        application.addEntryPoint("/home", BasicEntryPoint.class, properties);
        
        new Thread(new Runnable() {
			@Override
			public void run() {
				factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT_NAME);
		        EntityManager em = factory.createEntityManager();
		        // read the existing entries and write to console
		        Query q = em.createQuery("select w from Worker w");
		        List<Worker> userList = q.getResultList();
		        for (Worker usr : userList) {
		            System.out.println(usr.getLogin());
		        }
		        System.out.println("Size: " + userList.size());

		        // create new user
		        em.getTransaction().begin();
		        Worker user = new Worker();
		        user.setLogin("meow");
		        user.setAdmin(false);
		        user.setName("The Meow");
		        em.persist(user);
		        em.getTransaction().commit();

		        // read the existing entries and write to console
		        q = em.createQuery("select w from Worker w");
		        userList = q.getResultList();
		        for (Worker usr : userList) {
		            System.out.println(usr.getLogin());
		        }
		        System.out.println("Size: " + userList.size());
		        
		        em.close();
			}
        	
        }).start();
    }
}