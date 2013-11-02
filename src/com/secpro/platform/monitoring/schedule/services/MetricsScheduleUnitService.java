package com.secpro.platform.monitoring.schedule.services;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.management.DynamicMBean;
import javax.xml.bind.annotation.XmlElement;

import org.json.JSONArray;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.metrics.AbstractMetricMBean;
import com.secpro.platform.core.metrics.Metric;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.Activator;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.services.taskunit.RegionTaskStack;
import com.secpro.platform.monitoring.schedule.storages.DataBaseStorageAdapter;

/**
 * @author baiyanwei Jul 6, 2013
 * 
 */
@ServiceInfo(description = "The main service of Metrics-Schedule-Service", configurationPath = "/app/msu/services/MetricsScheduleUnitService/")
public class MetricsScheduleUnitService extends AbstractMetricMBean implements IService, DynamicMBean {

	//
	// Logging Object
	//
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(MetricsScheduleUnitService.class);
	//
	@XmlElement(name = "jmxObjectName", defaultValue = "secpro.mca:type=MonitoringService")
	public String _jmxObjectName = "secpro.mca:type=MonitoringService";

	// cache the version number
	@Metric(description = "The version number of MSU")
	public String _version = Activator._version.toString();
	//
	@XmlElement(name = "isFetchCacheTaskOnError", type = Boolean.class, defaultValue = "true")
	public Boolean _isFetchCacheTaskOnError = new Boolean(true);
	private HashMap<String, String[]> _regionNameMap = new HashMap<String, String[]>();
	private ScheduleCoreService _scheduleCoreService = null;
	private TaskCoreService _taskCoreService = null;

	@Override
	public void start() throws PlatformException {
		// register itself as dynamic bean
		this.registerMBean(_jmxObjectName, this);
		//
		initRegionInfor();
		//
		initTaskInfor();
		//
		// theLogger.info("startUp", _workflowThreshold.intValue(),
		// _fetchTSSTaskInterval, _operationCapabilities);
	}

	@Override
	public void stop() throws PlatformException {
		// unregister itself
		this.unRegisterMBean(_jmxObjectName);
	}

	public JSONArray fetchTaskByRequest(String region, String operatons, String num) {
		return null;
	}

	/**
	 * read the region information
	 */
	private void initRegionInfor() {
		DataBaseStorageAdapter dataBaseStorageAdapter = ServiceHelper.findService(DataBaseStorageAdapter.class);
		if (dataBaseStorageAdapter == null) {
			return;
		}
		//
		String sql = "SELECT CITY_NAME,CITY_CODE,CITY_LEVEL,PARENT_CODE FROM SYS_CITY";
		Object[][] resultData = dataBaseStorageAdapter.selectRecords(sql);
		if (resultData == null || resultData.length == 0) {
			return;
		}
		// {{CITY_NAME,CITY_CODE,CITY_LEVEL,PARENT_CODE},}
		for (int i = 0; i < resultData.length; i++) {
			this._regionNameMap.put((String) resultData[i][0], new String[] { (String) resultData[i][0], (String) resultData[i][1], (String) resultData[i][2],
					(String) resultData[i][3] });
		}
	}

	private void initTaskInfor() {
		DataBaseStorageAdapter dataBaseStorageAdapter = ServiceHelper.findService(DataBaseStorageAdapter.class);
		if (dataBaseStorageAdapter == null) {
			return;
		}
		//
		String sql = "SELECT CITY_NAME,CITY_CODE,CITY_LEVEL,PARENT_CODE FROM SYS_CITY";
		Object[][] resultData = dataBaseStorageAdapter.selectRecords(sql);
		if (resultData == null || resultData.length == 0) {
			return;
		}
		// {{CITY_NAME,CITY_CODE,CITY_LEVEL,PARENT_CODE},}
		for (int i = 0; i < resultData.length; i++) {
			this._regionNameMap.put((String) resultData[i][0], new String[] { (String) resultData[i][0], (String) resultData[i][1], (String) resultData[i][2],
					(String) resultData[i][3] });
		}
	}

	public void buildScheduleOnTime() {
		HashMap<String, RegionTaskStack> regionTaskStackMap = _taskCoreService.getRegionTaskStackMap();
		String region = null;
		RegionTaskStack stack = null;
		Date currentPoint = new Date();
		for (Iterator<String> regionIter = regionTaskStackMap.keySet().iterator(); regionIter.hasNext();) {
			region = regionIter.next();
			stack = regionTaskStackMap.get(region);
			HashMap<String, List<MSUSchedule>> scheduleMap = stack.nextHourSchedule(currentPoint);
			for (Iterator<String> operationIter = scheduleMap.keySet().iterator(); operationIter.hasNext();) {
				String operation = operationIter.next();
				List<MSUSchedule> scheduleList = scheduleMap.get(operation);
				_scheduleCoreService.putMSUSchedules(region, operation, scheduleList);
			}
		}

	}

	//
	// STATISTICAL METHODS
	//
	@Metric(description = "get the error rate of the MonitoringService")
	public double fetchDelayRate() {
		return 0.0D;
	}

	@Metric(description = "get the processing average of MonitoringService")
	public double getTaskWaitToFetchAverage() {
		return 0.0D;
	}

	@Metric(description = "get the system total memory status")
	public long getSystemTotalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	@Metric(description = "get the system free memory status")
	public long getSystemFreeMemory() {
		return Runtime.getRuntime().freeMemory();
	}

	@Metric(description = "get the system load average status")
	public double getSystemLoadAverage() {
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		double cpuTime = 0;
		if (os instanceof OperatingSystemMXBean) {
			cpuTime = ((OperatingSystemMXBean) os).getSystemLoadAverage();
		}
		return cpuTime;
	}

	@Metric(description = "get size of the task queue times ")
	public int getTaskQueueTimesSize() {
		return 0;
	}
}