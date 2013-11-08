package com.secpro.platform.monitoring.schedule.bri;

import com.secpro.platform.api.common.http.server.HttpServer;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.monitoring.schedule.services.MetricsSyslogRuleService;

/**
 * @author baiyanwei Jul 21, 2013
 * 
 *         SYSLOG standard rule Interface for MCA , transform the parameter from
 *         the HTTP request. implements IService ,then APIEngineService will
 *         register it into OSGI with no start and no property.
 */
public class SyslogRuleBeaconInterface extends HttpServer implements IService {
	final public static String REGION = "r";
	final public static String MCA = "o";
	final public static String PUSH_URL = "c";

	private MetricsSyslogRuleService _metricsSyslogRuleService = null;

	@Override
	public void start() throws Exception {
		super.start();
		_metricsSyslogRuleService = ServiceHelper.findService(MetricsSyslogRuleService.class);
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		super.stop();
		// _MetricsSyslogRuleService = null;
	}

	/**
	 * fetch a batch task by request.
	 * 
	 * @param requestParameterMap
	 * @return
	 */
	public String fetchSysLogRule(String region, String mca, String pushPath) throws Exception {
		if (Assert.isEmptyString(region) == true || Assert.isEmptyString(mca) == true || Assert.isEmptyString(pushPath) == true) {
			new Exception("invalid parameter");
		}
		if (_metricsSyslogRuleService == null) {
			_metricsSyslogRuleService = ServiceHelper.findService(MetricsSyslogRuleService.class);
		}

		return _metricsSyslogRuleService.fetchScheduleByRequest(region, mca, pushPath);
	}
}
