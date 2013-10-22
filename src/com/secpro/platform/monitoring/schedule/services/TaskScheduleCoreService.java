package com.secpro.platform.monitoring.schedule.services;

import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.management.DynamicMBean;
import javax.xml.bind.annotation.XmlElement;

import org.json.JSONArray;
import org.json.JSONObject;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.metrics.AbstractMetricMBean;
import com.secpro.platform.core.metrics.Metric;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.Activator;
import com.secpro.platform.monitoring.schedule.services.taskunit.RegionTaskStack;

/**
 * @author baiyanwei Jul 6, 2013
 * 
 */
@ServiceInfo(description = "The main service of Metrics-Collect-Agent", configurationPath = "msu/services/TaskScheduleCoreService/")
public class TaskScheduleCoreService extends AbstractMetricMBean implements IService, DynamicMBean {

	//
	// Logging Object
	//
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(TaskScheduleCoreService.class);
	//
	@XmlElement(name = "jmxObjectName", defaultValue = "secpro.mca:type=MonitoringService")
	public String _jmxObjectName = "secpro.mca:type=MonitoringService";

	// cache the version number
	@Metric(description = "The version number of MSU")
	public String _version = Activator._version.toString();
	//
	@XmlElement(name = "isFetchCacheTaskOnError", type = Boolean.class, defaultValue = "true")
	public Boolean _isFetchCacheTaskOnError = new Boolean(true);

	private HashMap<String, RegionTaskStack> _regionTaskStackMap = new HashMap<String, RegionTaskStack>();

	@Override
	public void start() throws PlatformException {
		// register itself as dynamic bean
		this.registerMBean(_jmxObjectName, this);
		//
		initOperationData();
		// theLogger.info("startUp", _workflowThreshold.intValue(),
		// _fetchTSSTaskInterval, _operationCapabilities);
	}

	@Override
	public void stop() throws PlatformException {
		// unregister itself
		this.unRegisterMBean(_jmxObjectName);
	}

	/**
	 * ready the location and operation from the database.
	 */
	private void initOperationData() {
		// #1 read the task records from database
		List<String[]> dataArrays = new ArrayList<String[]>();
		// #2 group the task record by region and operation.
		HashMap<String, HashMap<String, ArrayList<String[]>>> regionGroupDateMap = groupTaskRecordByRegionOperation(dataArrays);
		// #3
		synchronized (_regionTaskStackMap) {
			HashMap<String, ArrayList<String[]>> regionOperationMap = null;
			String operationKey = null;
			ArrayList<String[]> taskRowList = null;
			for (Iterator<String> regionIter = regionGroupDateMap.keySet().iterator(); regionIter.hasNext();) {
				//
				RegionTaskStack regionStack = new RegionTaskStack();
				try {
					regionStack.start();
				} catch (Exception e1) {
					e1.printStackTrace();
					continue;
				}
				regionStack._region = regionIter.next();
				regionOperationMap = regionGroupDateMap.get(regionStack._region);
				for (Iterator<String> operationIter = regionOperationMap.keySet().iterator(); operationIter.hasNext();) {
					operationKey = operationIter.next();
					taskRowList = regionOperationMap.get(operationKey);
					try {
						regionStack.putTasks(operationKey, analyesTaskContent(taskRowList));
					} catch (Exception e) {
						e.printStackTrace();
						continue;
					}
				}
			}
		}

	}

	private HashMap<String, HashMap<String, ArrayList<String[]>>> groupTaskRecordByRegionOperation(List<String[]> dataArrays) {
		HashMap<String, HashMap<String, ArrayList<String[]>>> regionDateMap = new HashMap<String, HashMap<String, ArrayList<String[]>>>();
		String[] rowDatas = null;
		for (int i = 0; i < dataArrays.size(); i++) {
			try {
				rowDatas = dataArrays.get(i);
				if (regionDateMap.containsKey(rowDatas[0]) == false) {
					HashMap<String, ArrayList<String[]>> operationRowDataMap = new HashMap<String, ArrayList<String[]>>();
					ArrayList<String[]> operationRowList = new ArrayList<String[]>();
					operationRowList.add(rowDatas);
					operationRowDataMap.put(rowDatas[1], operationRowList);
					regionDateMap.put(rowDatas[0], operationRowDataMap);
				} else {
					HashMap<String, ArrayList<String[]>> operationRowDataMap = regionDateMap.get(rowDatas[0]);
					if (operationRowDataMap.containsKey(rowDatas[1]) == false) {
						ArrayList<String[]> operationRowList = new ArrayList<String[]>();
						operationRowList.add(rowDatas);
					} else {
						ArrayList<String[]> operationRowList = operationRowDataMap.get(rowDatas[1]);
						operationRowList.add(rowDatas);
						operationRowDataMap.put(rowDatas[1], operationRowList);
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
				continue;
			}
		}
		return regionDateMap;
	}

	private ArrayList<JSONObject> analyesTaskContent(ArrayList<String[]> taskContentList) {
		return null;
	}

	public JSONArray fetchTaskByRequest(String region, String operatons, String num) {
		return null;
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