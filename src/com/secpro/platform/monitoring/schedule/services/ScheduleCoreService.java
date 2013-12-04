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
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.action.UpdateScheduleStatusAction;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.RegionScheduleStack;
import com.secpro.platform.monitoring.schedule.storages.DataBaseStorageAdapter;

/**
 * @author baiyanwei Jul 6, 2013
 * 
 */
@ServiceInfo(description = "ScheduleCoreService", configurationPath = "app/msu/services/ScheduleCoreService/")
public class ScheduleCoreService extends AbstractMetricMBean implements IService, DynamicMBean {
	//
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(TaskCoreService.class);
	//
	@XmlElement(name = "jmxObjectName", defaultValue = "secpro.msu:type=ScheduleCoreService")
	public String _jmxObjectName = "secpro.msu:type=ScheduleCoreService";

	@XmlElement(name = "updateSizeBatchSize", type = Integer.class, defaultValue = "100")
	public int _updateSizeBatchSize = 100;
	//
	private HashMap<String, RegionScheduleStack> _regionScheduleStackMap = new HashMap<String, RegionScheduleStack>();

	private ArrayList<String[]> _waitToUpdateScheduleStatusList = new ArrayList<String[]>();

	@Override
	public void start() throws PlatformException {
		// register itself as dynamic bean
		this.registerMBean(_jmxObjectName, this);
		//
		initRegionScheduleData();
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
	private void initRegionScheduleData() {
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
		synchronized (_regionScheduleStackMap) {
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
				_regionScheduleStackMap.put(regionScheduleStack._region, regionScheduleStack);
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

	//
	/**
	 * @param statusList
	 * 
	 * update the schedule instance in batch.
	 */
	public void updateScheduleStatus(ArrayList<String[]> statusList) {
		if (Assert.isEmptyCollection(statusList) == true) {
			return;
		}
		if (statusList.size() >= _updateSizeBatchSize) {
			UpdateScheduleStatusAction updateAction = new UpdateScheduleStatusAction(statusList);
			updateAction.start();
			return;
		}
		synchronized (_waitToUpdateScheduleStatusList) {
			_waitToUpdateScheduleStatusList.addAll(statusList);
			if (_waitToUpdateScheduleStatusList.size() >= _updateSizeBatchSize) {
				ArrayList<String[]> updateList = new ArrayList<String[]>();
				updateList.addAll(_waitToUpdateScheduleStatusList);
				_waitToUpdateScheduleStatusList.clear();
				UpdateScheduleStatusAction updateAction = new UpdateScheduleStatusAction(updateList);
				updateAction.start();
			}
		}
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
	 * get next no fetching schedule.
	 * 
	 * @param region
	 * @param operations
	 * @param num
	 * @return
	 */
	public List<MSUSchedule> nextNofetchingMSUSchedules(String region, String operations, int num) {
		if (Assert.isEmptyString(region) == true || Assert.isEmptyString(operations) == true || num <= 0) {
			return new ArrayList<MSUSchedule>();
		}

		return this._regionScheduleStackMap.get(region).nextSchedules(operations, num);
	}

	/**
	 * put or insert a MSU task into region.
	 * 
	 * @param msuTask
	 * @return
	 */
	public void putMSUSchedule(MSUSchedule msuSchedule, boolean isRealTime) {
		if (msuSchedule == null) {
			return;
		}
		this._regionScheduleStackMap.get(msuSchedule.getRegion()).putScheduleToBottom(msuSchedule.getOperation(), msuSchedule, isRealTime);
	}

	public void putMSUSchedules(String region, String operation, List<MSUSchedule> msuScheduleList, boolean isRealTime) {
		if (Assert.isEmptyCollection(msuScheduleList) == true || Assert.isEmptyString(region) == true || Assert.isEmptyString(operation) == true) {
			return;
		}
		this._regionScheduleStackMap.get(region).putSchedules(operation, msuScheduleList, isRealTime);
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
		this._regionScheduleStackMap.get(region).removeSchedule(taskID);
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