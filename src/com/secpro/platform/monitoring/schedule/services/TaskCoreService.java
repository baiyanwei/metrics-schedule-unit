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
import com.secpro.platform.monitoring.schedule.services.taskunit.MSUTask;
import com.secpro.platform.monitoring.schedule.services.taskunit.RegionTaskStack;

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
		List<String[]> dataArrays = getTaskRecordsFromDataBase();
		if (dataArrays == null || dataArrays.size() == 0) {
			theLogger.warn("NoTaskRcoreds");
		}
		// #2 group the task record by region and operation.
		HashMap<String, ArrayList<String[]>> regionGroupDateMap = groupTaskRecordByRegion(dataArrays);
		// #3
		synchronized (_regionTaskStackMap) {
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
				try {
					regionStack.putTasks(buildMSUTaskByRecord(regionGroupDateMap.get(regionStack._region)));
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
	private HashMap<String, ArrayList<String[]>> groupTaskRecordByRegion(List<String[]> dataArrays) {
		HashMap<String, ArrayList<String[]>> regionDateMap = new HashMap<String, ArrayList<String[]>>();
		String[] rowDatas = null;
		// [0] region.
		for (int i = 0; i < dataArrays.size(); i++) {
			try {
				rowDatas = dataArrays.get(i);
				if (regionDateMap.containsKey(rowDatas[0]) == false) {
					ArrayList<String[]> operationRowList = new ArrayList<String[]>();
					operationRowList.add(rowDatas);
					regionDateMap.put(rowDatas[0], operationRowList);
				} else {
					ArrayList<String[]> operationRowList = regionDateMap.get(rowDatas[0]);
					operationRowList.add(rowDatas);
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
	private List<String[]> getTaskRecordsFromDataBase() {
		// TODO Auto-generated method stub
		return new ArrayList<String[]>();
	}

	/**
	 * @param taskContentList
	 * @return
	 */
	private ArrayList<MSUTask> buildMSUTaskByRecord(ArrayList<String[]> taskContentList) {
		if (Assert.isEmptyCollection(taskContentList) == true) {
			return new ArrayList<MSUTask>();
		}
		ArrayList<MSUTask> msuTaskList = new ArrayList<MSUTask>();
		String[] rowData = null;
		MSUTask musTask = null;
		for (int i = 0; i < taskContentList.size(); i++) {
			try {
				rowData = taskContentList.get(i);
				//String id, String region, String operation, String schedule, long createAt, String metaData, String content, long resID, boolean isRealtime
				musTask = new MSUTask(rowData[0], rowData[1], rowData[2], rowData[3], Long.parseLong(rowData[4]), rowData[5],rowData[6],Long.parseLong(rowData[7]),false);
				musTask.setRealtime(false);
				msuTaskList.add(musTask);
			} catch (Exception e) {
				theLogger.exception(e);
				continue;
			}
		}
		return msuTaskList;
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
		if (msuTask == null || Assert.isEmptyString(msuTask._id) == true || Assert.isEmptyString(msuTask._region) == true) {
			return null;
		}
		try {
			return this._regionTaskStackMap.get(msuTask._region).putMSUTask(msuTask);
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
	public MSUTask removeMSUTask(String region, String taskID) {
		if (Assert.isEmptyString(region) == true || Assert.isEmptyString(taskID) == true) {
			return null;
		}
		return this._regionTaskStackMap.get(region).removeMUSTask(taskID);
	}

	/**
	 * update one MSU task.
	 * 
	 * @param region
	 * @param taskID
	 * @param msuTask
	 * @return
	 */
	public MSUTask updateMSUTask(String region, String taskID, MSUTask msuTask) {
		if (Assert.isEmptyString(region) == true || Assert.isEmptyString(taskID) == true || msuTask == null) {
			return null;
		}
		return this._regionTaskStackMap.get(region).updateMUSTask(taskID, msuTask);
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