package rap;

import java.time.LocalDate;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import autoflagschanger.FlagTask;
import autoflagschanger.FlagsChanger;
import database.LinkConnector;

public class BasicBundleActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		LinkConnector.connect();
		FlagsChanger.getInstance().start();
		System.out.println("Connection to database established");
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LinkConnector.close();
		FlagsChanger.getInstance().saveTasks();
	}

}
