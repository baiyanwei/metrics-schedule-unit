package com.secpro.platform.monitoring.schedule;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Version;

import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.services.MetricsScheduleUnitService;
import com.secpro.platform.monitoring.schedule.services.MetricsSyslogRuleService;
import com.secpro.platform.monitoring.schedule.services.NodeService;
import com.secpro.platform.monitoring.schedule.storages.DataBaseStorageAdapter;

/**
 * @author baiyanwei Oct 17, 2013
 * 
 * 
 * 
 */
public class Activator implements BundleActivator {
	// Logging Object
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(Activator.class);

	public static Version _version = null;
	private static BundleContext _context = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
	 * )
	 */
	public void start(BundleContext context) throws Exception {
		theLogger.info("####start the metric collect agent service");
		_context = context;
		_version = context.getBundle().getVersion();
		registerServices();
		theLogger.info("####The Metrics Schedule Unit stared completely");

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
	 */
	public void stop(BundleContext context) throws Exception {
	}

	public static BundleContext getContext() {
		return _context;
	}

	/**
	 * @throws Exception
	 *             register MSU all services.
	 */
	private void registerServices() throws Exception {
		//
		ServiceHelper.registerService(new NodeService());
		// The database connection service.
		ServiceHelper.registerService(new DataBaseStorageAdapter());
		// The task schedule core service.
		ServiceHelper.registerService(new MetricsScheduleUnitService());
		//
		ServiceHelper.registerService(new MetricsSyslogRuleService());
	}
}
