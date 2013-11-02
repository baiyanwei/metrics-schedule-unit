package com.secpro.platform.monitoring.schedule.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.management.DynamicMBean;
import javax.xml.bind.annotation.XmlElement;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.metrics.AbstractMetricMBean;
import com.secpro.platform.core.metrics.Metric;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.RegionScheduleStack;

/**
 * @author baiyanwei Jul 6, 2013
 * 
 */
@ServiceInfo(description = "ScheduleCoreService", configurationPath = "app/msu/services/ScheduleCoreService/")
public class ScheduleCoreService extends AbstractMetricMBean implements IService, DynamicMBean {

	//
	// Logging Object
	//
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(TaskCoreService.class);
	//
	@XmlElement(name = "jmxObjectName", defaultValue = "secpro.msu:type=ScheduleCoreService")
	public String _jmxObjectName = "secpro.msu:type=ScheduleCoreService";

	private HashMap<String, RegionScheduleStack> _regionScheduleStackMap = new HashMap<String, RegionScheduleStack>();

	@Override
	public void start() throws PlatformException {
		// register itself as dynamic bean
		this.registerMBean(_jmxObjectName, this);
		//
		initScheduleData();
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
	private void initScheduleData() {
		// #1 read the task records from database
		List<String[]> dataArrays = getScheduleRecordsFromDataBase();
		// #2 group the task record by region and operation.
		HashMap<String, HashMap<String, ArrayList<String[]>>> regionGroupDateMap = groupScheduleRecordByRegionOperation(dataArrays);
		// #3
		synchronized (_regionScheduleStackMap) {
			HashMap<String, ArrayList<String[]>> regionOperationMap = null;
			String operationKey = null;
			ArrayList<String[]> scheduleRowList = null;
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
				regionOperationMap = regionGroupDateMap.get(regionScheduleStack._region);
				for (Iterator<String> operationIter = regionOperationMap.keySet().iterator(); operationIter.hasNext();) {
					operationKey = operationIter.next();
					scheduleRowList = regionOperationMap.get(operationKey);
					try {
						regionScheduleStack.putSchedules(operationKey, buildMSUScheduleByRecord(scheduleRowList), false);
					} catch (Exception e) {
						theLogger.exception(e);
						continue;
					}
				}
			}
		}

	}

	private HashMap<String, HashMap<String, ArrayList<String[]>>> groupScheduleRecordByRegionOperation(List<String[]> dataArrays) {
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
				theLogger.exception(e);
				continue;
			}
		}
		return regionDateMap;
	}

	/**
	 * ready the record from DataBase and analyze into String Array.
	 * 
	 * @return
	 */
	private List<String[]> getScheduleRecordsFromDataBase() {
		// TODO Auto-generated method stub
		return new ArrayList<String[]>();
	}

	/**
	 * @param scheduleContentList
	 * @return
	 */
	private ArrayList<MSUSchedule> buildMSUScheduleByRecord(ArrayList<String[]> scheduleContentList) {
		if (Assert.isEmptyCollection(scheduleContentList) == true) {
			return new ArrayList<MSUSchedule>();
		}
		ArrayList<MSUSchedule> msuScheduleList = new ArrayList<MSUSchedule>();
		String[] rowData = null;
		for (int i = 0; i < scheduleContentList.size(); i++) {
			try {
				rowData = scheduleContentList.get(i);
				// String taskID, String scheduleID, long schedulePoint, long
				// createAt, String region, String operation, long fetchAt, long
				// executeAt, long executeCost,
				// long executeStatus, String executeDescription
				msuScheduleList.add(new MSUSchedule(rowData[0], rowData[1], 0, 0, rowData[4], rowData[5], 0, 0, 0, 0, rowData[10]));
			} catch (Exception e) {
				theLogger.exception(e);
				continue;
			}
		}
		return msuScheduleList;
	}

	/**
	 * get Task Content by ID
	 * 
	 * @param region
	 * @param taskIDs
	 * @return
	 */
	public List<MSUSchedule> getMSUSchedules(String region, String operation, String taskID) {
		if (Assert.isEmptyString(region) == true || Assert.isEmptyString(operation) == true || Assert.isEmptyString(taskID) == true) {
			return new ArrayList<MSUSchedule>();
		}
		return this._regionScheduleStackMap.get(region).findSchedules(operation, taskID);
	}

	/**
	 * get Task Content by ID
	 * 
	 * @param region
	 * @param taskID
	 * @return
	 */
	public HashMap<String, List<MSUSchedule>> getMSUSchedules(String region, String taskID) {
		if (Assert.isEmptyString(region) == true || Assert.isEmptyString(taskID) == true) {
			return new HashMap<String, List<MSUSchedule>>();
		}

		return this._regionScheduleStackMap.get(region).findSchedules(taskID);
	}

	/**
	 * put or insert a MSU task into region.
	 * 
	 * @param msuTask
	 * @return
	 */
	public void putMSUSchedule(MSUSchedule msuSchedule) {
		if (msuSchedule == null) {
			return;
		}
		this._regionScheduleStackMap.get(msuSchedule.getRegion()).putScheduleToBottom(msuSchedule.getOperation(), msuSchedule, false);
	}

	public void putMSUSchedules(String region, String operation, List<MSUSchedule> msuScheduleList) {
		if (Assert.isEmptyCollection(msuScheduleList) == true || Assert.isEmptyString(region) == true || Assert.isEmptyString(operation) == true) {
			return;
		}
		this._regionScheduleStackMap.get(region).putSchedules(operation, msuScheduleList, false);
	}

	/**
	 * remove one task in region by task ID.
	 * 
	 * @param region
	 * @param taskID
	 * @return
	 */
	public void removeMSUSchedule(String region, String taskID) {
		if (Assert.isEmptyString(region) == true || Assert.isEmptyString(taskID) == true) {
			return;
		}
		// return this._regionTaskStackMap.get(region).removeMUSTask(taskID);
	}

	//
	// STATISTICAL METHODS
	//
	@Metric(description = "get size of the task region ")
	public String getRegionTaskSize(String region) {
		if (Assert.isEmptyString(region) == true || this._regionScheduleStackMap.containsKey(region) == false) {
			return "No data";
		}
		return this._regionScheduleStackMap.get(region).reportStackSize();
	}

	@Metric(description = "get every detail of task region ")
	public String getEveryRegionTaskSize() {
		StringBuffer reportStr = new StringBuffer();
		String region = null;
		for (Iterator<String> regionIter = this._regionScheduleStackMap.keySet().iterator(); regionIter.hasNext();) {
			region = regionIter.next();
			reportStr.append(reportStr).append("\t\t").append(this._regionScheduleStackMap.get(region).reportStackSize()).append("\r\n");
		}
		return reportStr.toString();
	}

}