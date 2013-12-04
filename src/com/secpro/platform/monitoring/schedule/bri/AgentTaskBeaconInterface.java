package com.secpro.platform.monitoring.schedule.bri;

import com.secpro.platform.api.common.http.server.HttpServer;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.monitoring.schedule.services.MetricsScheduleUnitService;

/**
 * @author baiyanwei Jul 21, 2013
 * 
 *         fetching Interface for MCA , transform the parameter from the HTTP
 *         request. implements IService ,then APIEngineService will register it
 *         into OSGI with no start and no property.
 */
public class AgentTaskBeaconInterface extends HttpServer implements IService {

	private MetricsScheduleUnitService _metricsScheduleUnitService = null;

	@Override
	public void start() throws Exception {
		super.start();
		_metricsScheduleUnitService = ServiceHelper.findService(MetricsScheduleUnitService.class);
	}

	@Override
	public void stop() throws Exception {
		// TODO Auto-generated method stub
		super.stop();
		_metricsScheduleUnitService = null;
	}

	/**
	 * fetch a batch task by request.
	 * 
	 * @param requestParameterMap
	 * @return
	 */
	public String fetchTask(String region, String operations, int counter, String fetcher, String publicKey) throws Exception {
		if (Assert.isEmptyString(region) == true || Assert.isEmptyString(operations) == true || Assert.isEmptyString(fetcher) == true || Assert.isEmptyString(publicKey) == true
				|| counter <= 0) {
			new Exception("invalid parameter");
		}
		if (_metricsScheduleUnitService == null) {
			_metricsScheduleUnitService = ServiceHelper.findService(MetricsScheduleUnitService.class);
		}
		//
		return _metricsScheduleUnitService.fetchScheduleByRequest(region, operations, counter, fetcher, publicKey);
	}
}
