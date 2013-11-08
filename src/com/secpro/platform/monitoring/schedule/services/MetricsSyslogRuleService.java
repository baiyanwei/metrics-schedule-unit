package com.secpro.platform.monitoring.schedule.services;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Timer;

import javax.management.DynamicMBean;
import javax.xml.bind.annotation.XmlElement;

import org.json.JSONArray;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.metrics.AbstractMetricMBean;
import com.secpro.platform.core.metrics.Metric;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.core.utils.Utils;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.Activator;
import com.secpro.platform.monitoring.schedule.action.ScheduleAction;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.RegionScheduleStack;
import com.secpro.platform.monitoring.schedule.services.taskunit.MSUTask;
import com.secpro.platform.monitoring.schedule.storages.DataBaseStorageAdapter;

/**
 * @author baiyanwei Jul 6, 2013
 * 
 *         In this unit, We define the task , make the schedule. Task is content
 *         about What task is , Where Task will be executed,What time task will
 *         be fetch. Here have two type of task. one is frequency task ,other is
 *         realTime task. RealTime will be fetched and executed at first. One
 *         task map many schedule on different time point. Task is created by
 *         system management.And the schedule is created by schedule property in
 *         task content. Create schedule on every hour.it is hourly. The all
 *         task and schedule content will be stored in task with starting name
 *         "MSU_"
 */
@ServiceInfo(description = "The main service of Metrics-Schedule-Service", configurationPath = "/app/msu/services/MetricsScheduleUnitService/")
public class MetricsSyslogRuleService extends AbstractMetricMBean implements IService, DynamicMBean {
	//
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(TaskCoreService.class);
	//
	@XmlElement(name = "jmxObjectName", defaultValue = "secpro.msu:type=ScheduleCoreService")
	public String _jmxObjectName = "secpro.msu:type=ScheduleCoreService";
	//
	private HashMap<String, String> _syslogStandardRuleMap = new HashMap<String, String>();

	@Override
	public void start() throws PlatformException {
		// register itself as dynamic bean
		this.registerMBean(_jmxObjectName, this);
		//
		initSyslogStandardRuleData();
		//
		theLogger.info("startUp");
	}

	@Override
	public void stop() throws PlatformException {
		// unregister itself
		this.unRegisterMBean(_jmxObjectName);
	}

	/**
	 * ready the location and operation from the database.
	 */
	private void initSyslogStandardRuleData() {
		/*
		// #1 read the task records from database
		List<MSUSchedule> unfetchScheduleList = getScheduleRecordsFromDataBase();
		if (Assert.isEmptyCollection(unfetchScheduleList) == true) {
			return;
		}
		// #2 group the task record by region and operation.
		HashMap<String, HashMap<String, ArrayList<MSUSchedule>>> regionGroupDateMap = groupScheduleRecordByRegionOperation(unfetchScheduleList);
		if (Assert.isEmptyMap(regionGroupDateMap) == true) {
			return;
		}
		// #3
		synchronized (_syslogStandardRuleMap) {
			HashMap<String, ArrayList<MSUSchedule>> regionOperationMap = null;
			String operationKey = null;
			for (Iterator<String> regionIter = regionGroupDateMap.keySet().iterator(); regionIter.hasNext();) {
				//
				RegionScheduleStack regionScheduleStack = new RegionScheduleStack();
				try {
					regionScheduleStack.start();
				} catch (Exception e1) {
					theLogger.exception(e1);
					continue;
				}
				regionScheduleStack._region = regionIter.next();
				//
				_syslogStandardRuleMap.put(regionScheduleStack._region, regionScheduleStack);
				//
				regionOperationMap = regionGroupDateMap.get(regionScheduleStack._region);
				//
				for (Iterator<String> operationIter = regionOperationMap.keySet().iterator(); operationIter.hasNext();) {
					operationKey = operationIter.next();
					try {
						regionScheduleStack.putSchedules(operationKey, regionOperationMap.get(operationKey), false);
					} catch (Exception e) {
						theLogger.exception(e);
						continue;
					}
				}
			}
		}
*/
	}

	/**
	 * group the schedule by region operation.
	 * 
	 * @param unfetchScheduleList
	 * @return
	 */
	private HashMap<String, HashMap<String, ArrayList<MSUSchedule>>> groupScheduleRecordByRegionOperation(List<MSUSchedule> unfetchScheduleList) {
		
		HashMap<String, HashMap<String, ArrayList<MSUSchedule>>> regionScheduleMap = new HashMap<String, HashMap<String, ArrayList<MSUSchedule>>>();
		MSUSchedule unfetchSchedule = null;
		// group the schedule.
		for (int i = 0; i < unfetchScheduleList.size(); i++) {
			try {
				unfetchSchedule = unfetchScheduleList.get(i);
				if (regionScheduleMap.containsKey(unfetchSchedule.getRegion()) == false) {
					HashMap<String, ArrayList<MSUSchedule>> operationScheduleMap = new HashMap<String, ArrayList<MSUSchedule>>();
					ArrayList<MSUSchedule> operationScheduleList = new ArrayList<MSUSchedule>();
					operationScheduleList.add(unfetchSchedule);
					operationScheduleMap.put(unfetchSchedule.getOperation(), operationScheduleList);
					regionScheduleMap.put(unfetchSchedule.getRegion(), operationScheduleMap);
				} else {
					HashMap<String, ArrayList<MSUSchedule>> operationScheduleMap = regionScheduleMap.get(unfetchSchedule.getRegion());
					if (operationScheduleMap.containsKey(unfetchSchedule.getOperation()) == false) {
						ArrayList<MSUSchedule> operationScheduleList = new ArrayList<MSUSchedule>();
						operationScheduleList.add(unfetchSchedule);
					} else {
						ArrayList<MSUSchedule> operationScheduleList = operationScheduleMap.get(unfetchSchedule.getOperation());
						operationScheduleList.add(unfetchSchedule);
						operationScheduleMap.put(unfetchSchedule.getOperation(), operationScheduleList);
					}
				}
			} catch (Exception e) {
				theLogger.exception(e);
				continue;
			}
		}
		return regionScheduleMap;
	}

	/**
	 * ready the record from DataBase and analyze into String Array.
	 * 
	 * @return
	 */
	private List<MSUSchedule> getScheduleRecordsFromDataBase() {
		DataBaseStorageAdapter dataBaseStorageAdapter = ServiceHelper.findService(DataBaseStorageAdapter.class);
		if (dataBaseStorageAdapter == null) {
			return new ArrayList<MSUSchedule>();
		}
		return dataBaseStorageAdapter.querySchedulesNofetching(0, System.currentTimeMillis());
	}

	public String fetchScheduleByRequest(String region, String mca, String pushPath) {
		// TODO Auto-generated method stub
		return null;
	}
}