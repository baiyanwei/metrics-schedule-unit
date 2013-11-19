package com.secpro.platform.monitoring.schedule.action;

import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import com.secpro.platform.monitoring.schedule.services.MetricsScheduleUnitService;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.services.taskunit.RegionTaskStack;

/**
 * @author baiyanwei Nov 5, 2013
 * 
 * 
 *         build the schedule by task.
 */
public class ScheduleAction extends TimerTask {
	private MetricsScheduleUnitService _metricsScheduleUnitService = null;

	public ScheduleAction(MetricsScheduleUnitService metricsScheduleUnitService) {
		this._metricsScheduleUnitService = metricsScheduleUnitService;
	}

	@Override
	public void run() {
		Date currentPoint = new Date();
		buildScheduleByTime(currentPoint);
	}

	/**
	 * build Schedule for current hour.
	 */
	public void buildScheduleByTime(Date currentPoint) {
		HashMap<String, RegionTaskStack> regionTaskStackMap = _metricsScheduleUnitService._taskCoreService.getRegionTaskStackMap();
		String region = null;
		RegionTaskStack stack = null;
		//
		for (Iterator<String> regionIter = regionTaskStackMap.keySet().iterator(); regionIter.hasNext();) {
			region = regionIter.next();
			stack = regionTaskStackMap.get(region);
			HashMap<String, List<MSUSchedule>> scheduleMap = stack.nextHourSchedule(currentPoint);
			// put schedule into region stack.
			for (Iterator<String> operationIter = scheduleMap.keySet().iterator(); operationIter.hasNext();) {
				String operation = operationIter.next();
				List<MSUSchedule> scheduleList = scheduleMap.get(operation);
				_metricsScheduleUnitService._scheduleCoreService.putMSUSchedules(region, operation, scheduleList, false);
			}
		}

	}
}
