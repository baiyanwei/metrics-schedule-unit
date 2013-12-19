package com.secpro.platform.monitoring.schedule.action;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;

import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.services.MetricsScheduleUnitService;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.services.taskunit.RegionTaskStack;
import com.secpro.platform.monitoring.schedule.storages.DataBaseStorageAdapter;

/**
 * @author baiyanwei Nov 5, 2013
 * 
 * 
 *         build the schedule by task.
 */
public class ScheduleAction extends TimerTask {
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(ScheduleAction.class);
	//
	private MetricsScheduleUnitService _metricsScheduleUnitService = null;

	private long _scheduleInterval = 0;

	public ScheduleAction(MetricsScheduleUnitService metricsScheduleUnitService) {
		this._metricsScheduleUnitService = metricsScheduleUnitService;
		this._scheduleInterval = metricsScheduleUnitService._scheduleTimerExecuteInterval;
	}

	@Override
	public void run() {
		Date currentPoint = new Date();
		List<MSUSchedule> storeList = buildScheduleByTime(currentPoint);
		StoreSchedule(storeList);
	}

	/**
	 * build Schedule for current hour.
	 */
	public List<MSUSchedule> buildScheduleByTime(Date currentPoint) {
		List<MSUSchedule> storeList = new ArrayList<MSUSchedule>();
		HashMap<String, RegionTaskStack> regionTaskStackMap = _metricsScheduleUnitService._taskCoreService.getRegionTaskStackMap();
		if (regionTaskStackMap.isEmpty()) {
			return storeList;
		}
		//
		try {
			String region = null;
			RegionTaskStack stack = null;
			for (Iterator<String> regionIter = regionTaskStackMap.keySet().iterator(); regionIter.hasNext();) {
				region = regionIter.next();
				stack = regionTaskStackMap.get(region);
				try {
					HashMap<String, List<MSUSchedule>> scheduleMap = stack.nextHourSchedule(currentPoint,_scheduleInterval);
					// put schedule into region stack.
					for (Iterator<String> operationIter = scheduleMap.keySet().iterator(); operationIter.hasNext();) {
						String operation = operationIter.next();
						List<MSUSchedule> scheduleList = scheduleMap.get(operation);
						//
						storeList.addAll(scheduleList);
						_metricsScheduleUnitService._scheduleCoreService.putMSUSchedules(region, operation, scheduleList, false);
					}
				} catch (Exception t) {
					theLogger.exception(t);
				}
			}
		} catch (Exception e) {
			theLogger.exception(e);
		}
		return storeList;
	}

	private void StoreSchedule(List<MSUSchedule> storeList) {
		if (Assert.isEmptyCollection(storeList) == true) {
			return;
		}
		DataBaseStorageAdapter dataBaseStorageAdapter = ServiceHelper.findService(DataBaseStorageAdapter.class);
		if (dataBaseStorageAdapter == null) {
			theLogger.error("The DataBaseStorageAdapter cann't be find.");
			return;
		}
		if (storeList.size() > 2000) {
			// size is over 2000,group it with 2000 in batch.
			int group = (storeList.size() + 1999) / 2000;
			for (int i = 0; i < group; i++) {
				int groupSize = 2000 * (i + 1);
				if (groupSize > storeList.size()) {
					groupSize = storeList.size();
				}
				dataBaseStorageAdapter.insertSchedules(storeList.subList(i * 2000, groupSize));
			}
		} else {
			dataBaseStorageAdapter.insertSchedules(storeList);
		}

	}
}
