package com.secpro.platform.monitoring.schedule.bri;

import java.net.URI;

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

	private MetricsSyslogRuleService _metricsSyslogRuleService = null;

	@Override
	public void start() throws Exception {
		super.start();
		_metricsSyslogRuleService = ServiceHelper.findService(MetricsSyslogRuleService.class);
	}

	@Override
	public void stop() throws Exception {
		super.stop();
		_metricsSyslogRuleService = null;
	}

	/**
	 * fetch a batch task by request.
	 * 
	 * @param requestParameterMap
	 * @return
	 */
	public String fetchSysLogRule(String region, String mca, URI publishURI) throws Exception {
		if (Assert.isEmptyString(region) == true || Assert.isEmptyString(mca) == true || publishURI == null) {
			new Exception("invalid parameter");
		}
		if (_metricsSyslogRuleService == null) {
			_metricsSyslogRuleService = ServiceHelper.findService(MetricsSyslogRuleService.class);
		}

		return _metricsSyslogRuleService.fetchSyslogStandardRuleByRequest(region + "#" + mca, publishURI);
	}
}
