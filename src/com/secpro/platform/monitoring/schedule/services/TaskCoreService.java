package com.secpro.platform.monitoring.schedule.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.management.DynamicMBean;
import javax.xml.bind.annotation.XmlElement;

import org.json.JSONException;
import org.json.JSONObject;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.metrics.AbstractMetricMBean;
import com.secpro.platform.core.metrics.Metric;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.services.taskunit.MsuTask;
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
		List<MsuTask> regionTaskList = getRegionTaskFromDataBase();
		if (Assert.isEmptyCollection(regionTaskList) == true) {
			theLogger.warn("NoTaskRcoreds");
			return;
		}
		// #2 group the task record by region and operation.
		HashMap<String, ArrayList<MsuTask>> regionTaskGroupDateMap = groupTaskByRegion(regionTaskList);
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
	private HashMap<String, ArrayList<MsuTask>> groupTaskByRegion(List<MsuTask> regionTaskDataList) {
		//
		HashMap<String, ArrayList<MsuTask>> regionGroupMap = new HashMap<String, ArrayList<MsuTask>>();
		//
		MsuTask msuTask = null;
		for (int i = 0; i < regionTaskDataList.size(); i++) {
			try {
				msuTask = regionTaskDataList.get(i);
				if (regionGroupMap.containsKey(msuTask.getRegion()) == false) {
					ArrayList<MsuTask> taskRowList = new ArrayList<MsuTask>();
					taskRowList.add(msuTask);
					regionGroupMap.put(msuTask.getRegion(), taskRowList);
				} else {
					ArrayList<MsuTask> taskRowList = regionGroupMap.get(msuTask.getRegion());
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
	private List<MsuTask> getRegionTaskFromDataBase() {
		DataBaseStorageAdapter dataBaseStorageAdapter = ServiceHelper.findService(DataBaseStorageAdapter.class);
		if (dataBaseStorageAdapter == null) {
			return new ArrayList<MsuTask>();
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
	public HashMap<String, MsuTask> getMSUTasks(String region, String[] taskIDs) {
		if (Assert.isEmptyString(region) == true || taskIDs == null || taskIDs.length == 0) {
			return new HashMap<String, MsuTask>();
		}
		HashMap<String, MsuTask> msuTaskMap = new HashMap<String, MsuTask>();
		MsuTask msuTask = null;
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
	public MsuTask putMSUTask(MsuTask msuTask) {
		if (msuTask == null || Assert.isEmptyString(msuTask.getId()) == true || Assert.isEmptyString(msuTask.getRegion()) == true) {
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
	public MsuTask getMSUTask(String region, String taskID) {
		if (Assert.isEmptyString(region) == true || Assert.isEmptyString(taskID) == true) {
			return null;
		}
		if (this._regionTaskStackMap.containsKey(region) == false) {
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
	public MsuTask removeMSUTask(MsuTask task) {
		if (task == null || Assert.isEmptyString(task.getRegion()) == true || Assert.isEmptyString(task.getId()) == true) {
			return null;
		}
		if (this._regionTaskStackMap.containsKey(task.getRegion()) == false) {
			return null;
		}
		return this._regionTaskStackMap.get(task.getRegion()).removeMUSTask(task.getId());
	}

	/**
	 * remove the task by ID
	 * 
	 * @param taskID
	 * @return
	 */
	public MsuTask removeMSUTaskByTaskID(String taskID) {
		if (Assert.isEmptyString(taskID) == true) {
			return null;
		}
		synchronized (this._regionTaskStackMap) {
			String region = null;
			MsuTask task = null;
			for (Iterator<String> keyIter = this._regionTaskStackMap.keySet().iterator(); keyIter.hasNext();) {
				region = keyIter.next();
				task = this._regionTaskStackMap.get(region).findMSUTask(taskID);
				if (task != null) {
					return this._regionTaskStackMap.get(region).removeMUSTask(taskID);
				}
			}
		}
		return null;
	}

	/**
	 * update one MSU task.
	 * 
	 * @param region
	 * @param taskID
	 * @param msuTask
	 * @return
	 */
	public MsuTask updateMSUTask(MsuTask msuTask) {
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
	public String getRegionTaskSize(String region) {
		if (Assert.isEmptyString(region) == true || this._regionTaskStackMap.containsKey(region) == false) {
			return "{}";
		}
		return this._regionTaskStackMap.get(region).reportStackSize().toString();
	}

	@Metric(description = "get every detail of task region ")
	public String getEveryRegionTaskSize() {
		JSONObject report = new JSONObject();
		String region = null;
		for (Iterator<String> regionIter = this._regionTaskStackMap.keySet().iterator(); regionIter.hasNext();) {
			region = regionIter.next();
			try {
				report.put(region, this._regionTaskStackMap.get(region).reportStackSize());
			} catch (JSONException e) {
				continue;
			}
		}
		return report.toString();
	}
}