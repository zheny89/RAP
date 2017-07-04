package rap;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import database.LinkConnector;

public class BasicBundleActivator implements BundleActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		LinkConnector.connect();
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		LinkConnector.close();
	}

}