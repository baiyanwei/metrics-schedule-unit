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
import com.secpro.platform.monitoring.schedule.services.taskunit.MSUTask;
import com.secpro.platform.monitoring.schedule.services.taskunit.RegionTaskStack;
import com.secpro.platform.monitoring.schedule.storages.DataBaseStorageAdapter;

/**
 * @author baiyanwei Jul 6, 2013
 * 
 */
@ServiceInfo(description = "TaskCoreService", configurationPath = "app/msu/services/TaskCoreService/")
public class TaskCoreService extends AbstractMetricMBean implements IService, DynamicMBean {

	//
	// Logging Object
	//
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(TaskCoreService.class);
	//
	@XmlElement(name = "jmxObjectName", defaultValue = "secpro.msu:type=TaskCoreService")
	public String _jmxObjectName = "secpro.msu:type=TaskCoreService";

	private HashMap<String, RegionTaskStack> _regionTaskStackMap = new HashMap<String, RegionTaskStack>();

	@Override
	public void start() throws PlatformException {
		// register itself as dynamic bean
		this.registerMBean(_jmxObjectName, this);
		//
		initRegionTaskData();
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
	private void initRegionTaskData() {
		// #1 read the task records from database
		List<MSUTask> regionTaskList = getRegionTaskFromDataBase();
		if (Assert.isEmptyCollection(regionTaskList) == true) {
			theLogger.warn("NoTaskRcoreds");
			return;
		}
		// #2 group the task record by region and operation.
		HashMap<String, ArrayList<MSUTask>> regionTaskGroupDateMap = groupTaskByRegion(regionTaskList);
		if (Assert.isEmptyMap(regionTaskGroupDateMap) == true) {
			return;
		}
		// #3
		synchronized (_regionTaskStackMap) {
			for (Iterator<String> regionIter = regionTaskGroupDateMap.keySet().iterator(); regionIter.hasNext();) {
				try {
					RegionTaskStack regionStack = createTaskRegion(regionIter.next());
					//
					_regionTaskStackMap.put(regionStack._region, regionStack);
					//
					regionStack.putTasks(regionTaskGroupDateMap.get(regionStack._region));
				} catch (Exception e) {
					theLogger.exception(e);
					continue;
				}
			}
		}

	}

	/**
	 * group the record by region.
	 * 
	 * @param dataArrays
	 * @return
	 */
	private HashMap<String, ArrayList<MSUTask>> groupTaskByRegion(List<MSUTask> regionTaskDataList) {
		//
		HashMap<String, ArrayList<MSUTask>> regionGroupMap = new HashMap<String, ArrayList<MSUTask>>();
		//
		MSUTask msuTask = null;
		for (int i = 0; i < regionTaskDataList.size(); i++) {
			try {
				msuTask = regionTaskDataList.get(i);
				if (regionGroupMap.containsKey(msuTask.getRegion()) == false) {
					ArrayList<MSUTask> taskRowList = new ArrayList<MSUTask>();
					taskRowList.add(msuTask);
					regionGroupMap.put(msuTask.getRegion(), taskRowList);
				} else {
					ArrayList<MSUTask> taskRowList = regionGroupMap.get(msuTask.getRegion());
					taskRowList.add(msuTask);
				}
			} catch (Exception e) {
				theLogger.exception(e);
				continue;
			}
		}
		return regionGroupMap;
	}

	/**
	 * ready the record from DataBase and analyze into String Array.
	 * 
	 * @return
	 */
	private List<MSUTask> getRegionTaskFromDataBase() {
		DataBaseStorageAdapter dataBaseStorageAdapter = ServiceHelper.findService(DataBaseStorageAdapter.class);
		if (dataBaseStorageAdapter == null) {
			return new ArrayList<MSUTask>();
		}
		return dataBaseStorageAdapter.queryAllFrequencyTask();
	}

	/**
	 * create a task region instance.
	 * 
	 * @param region
	 * @return
	 * @throws Exception
	 */
	private RegionTaskStack createTaskRegion(String region) throws Exception {
		RegionTaskStack regionStack = new RegionTaskStack();
		regionStack._region = region;
		regionStack.start();
		return regionStack;
	}

	/**
	 * get Task Content by ID
	 * 
	 * @param region
	 * @param taskIDs
	 * @return
	 */
	public HashMap<String, MSUTask> getMSUTasks(String region, String[] taskIDs) {
		if (Assert.isEmptyString(region) == true || taskIDs == null || taskIDs.length == 0) {
			return new HashMap<String, MSUTask>();
		}
		HashMap<String, MSUTask> msuTaskMap = new HashMap<String, MSUTask>();
		MSUTask msuTask = null;
		for (int i = 0; i < taskIDs.length; i++) {
			msuTask = this._regionTaskStackMap.get(region).findMSUTask(taskIDs[i]);
			if (msuTask == null) {
				continue;
			}
			msuTaskMap.put(taskIDs[i], msuTask);
		}
		return msuTaskMap;
	}

	/**
	 * put or insert a MSU task into region.
	 * 
	 * @param msuTask
	 * @return
	 */
	public MSUTask putMSUTask(MSUTask msuTask) {
		if (msuTask == null || Assert.isEmptyString(msuTask.getID()) == true || Assert.isEmptyString(msuTask.getRegion()) == true) {
			return null;
		}
		try {
			if (_regionTaskStackMap.containsKey(msuTask.getRegion()) == false) {
				RegionTaskStack regionStack = createTaskRegion(msuTask.getRegion());
				synchronized (_regionTaskStackMap) {
					_regionTaskStackMap.put(regionStack._region, regionStack);
				}
				return regionStack.putMSUTask(msuTask);
			}
			return this._regionTaskStackMap.get(msuTask.getRegion()).putMSUTask(msuTask);
		} catch (Exception e) {
			theLogger.exception(e);
			return null;
		}
	}

	/**
	 * get Task Content by ID
	 * 
	 * @param region
	 * @param taskID
	 * @return
	 */
	public MSUTask getMSUTask(String region, String taskID) {
		if (Assert.isEmptyString(region) == true || Assert.isEmptyString(taskID) == true) {
			return null;
		}

		return this._regionTaskStackMap.get(region).findMSUTask(taskID);
	}

	/**
	 * remove one task in region by task ID.
	 * 
	 * @param region
	 * @param taskID
	 * @return
	 */
	public MSUTask removeMSUTask(MSUTask task) {
		if (task == null || Assert.isEmptyString(task.getRegion()) == true || Assert.isEmptyString(task.getID()) == true) {
			return null;
		}
		if (this._regionTaskStackMap.containsKey(task.getRegion()) == false) {
			return null;
		}
		return this._regionTaskStackMap.get(task.getRegion()).removeMUSTask(task.getID());
	}

	/**
	 * update one MSU task.
	 * 
	 * @param region
	 * @param taskID
	 * @param msuTask
	 * @return
	 */
	public MSUTask updateMSUTask(MSUTask msuTask) {
		if (msuTask == null) {
			return null;
		}
		if (this._regionTaskStackMap.containsKey(msuTask.getRegion()) == false) {
			return null;
		}
		return this._regionTaskStackMap.get(msuTask.getRegion()).updateMUSTask(msuTask);
	}

	public HashMap<String, RegionTaskStack> getRegionTaskStackMap() {
		return this._regionTaskStackMap;
	}

	//
	// STATISTICAL METHODS
	//
	@Metric(description = "get size of the task region ")
	public int getRegionTaskSize(String region) {
		if (Assert.isEmptyString(region) == true || this._regionTaskStackMap.containsKey(region) == false) {
			return 0;
		}
		return this._regionTaskStackMap.get(region).reportStackSize();
	}

	@Metric(description = "get every detail of task region ")
	public String getEveryRegionTaskSize() {
		StringBuffer reportStr = new StringBuffer();
		String region = null;
		for (Iterator<String> regionIter = this._regionTaskStackMap.keySet().iterator(); regionIter.hasNext();) {
			region = regionIter.next();
			reportStr.append(reportStr).append("\t\t").append(this._regionTaskStackMap.get(region).reportStackSize()).append("\r\n");
		}
		return reportStr.toString();
	}
}