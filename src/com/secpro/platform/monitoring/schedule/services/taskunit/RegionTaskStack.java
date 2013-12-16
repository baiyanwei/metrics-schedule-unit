package com.secpro.platform.monitoring.schedule.services.taskunit;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.quartz.CronExpression;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.task.TaskUtil;

/**
 * @author baiyanwei Oct 17, 2013
 * 
 * 
 *         The task stack for each region.
 */
public class RegionTaskStack implements IService {
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(RegionTaskStack.class);

	public String _region = "HB";
	// key:Task ID, Value: Task Content
	private HashMap<String, MsuTask> _regionTaskMap = new HashMap<String, MsuTask>();

	@Override
	public void start() throws Exception {
		theLogger.info("RegionTaskStack " + _region + "is started.");
	}

	@Override
	public void stop() throws Exception {
		theLogger.info("RegionTaskStack " + _region + "is stopped.");
	}

	/**
	 * put a task into bottom
	 * 
	 * @param msuTask
	 */
	public MsuTask putMSUTask(MsuTask msuTask) throws Exception {
		if (msuTask == null || Assert.isEmptyString(msuTask.getId()) == true) {
			return null;
		}
		synchronized (_regionTaskMap) {
			if (_regionTaskMap.containsKey(msuTask.getId()) == false) {
				this._regionTaskMap.put(msuTask.getId(), msuTask);
			} else {
				throw new Exception(msuTask.getId() + " is already in Stack.");
			}
		}
		return msuTask;
	}

	/**
	 * put tasks into stack.
	 * 
	 * @param taskList
	 */
	public void putTasks(ArrayList<MsuTask> msuTaskList) {
		if (Assert.isEmptyCollection(msuTaskList) == true) {
			return;
		}
		synchronized (_regionTaskMap) {
			for (int i = 0; i < msuTaskList.size(); i++) {
				this._regionTaskMap.put(msuTaskList.get(i).getId(), msuTaskList.get(i));
			}
		}
	}

	/**
	 * @param num
	 * @return get task by request number.
	 */
	public MsuTask findMSUTask(String taskID) {
		if (Assert.isEmptyString(taskID) == true) {
			return null;
		}
		return this._regionTaskMap.get(taskID);
	}

	/**
	 * remove task
	 * 
	 * @param taskID
	 * @return
	 */
	public MsuTask removeMUSTask(String taskID) {
		if (Assert.isEmptyString(taskID) == true) {
			return null;
		}
		synchronized (_regionTaskMap) {
			return this._regionTaskMap.remove(taskID);
		}
	}

	/**
	 * update a task
	 * 
	 * @param taskID
	 * @return
	 */
	public MsuTask updateMUSTask(MsuTask msuTask) {
		if (msuTask == null) {
			return null;
		}
		synchronized (_regionTaskMap) {
			this._regionTaskMap.put(msuTask.getId(), msuTask);
		}
		return msuTask;
	}

	public HashMap<String, List<MSUSchedule>> nextHourSchedule(Date currentPoint) {
		String taskID = null;
		MsuTask taskObj = null;
		long currentTime = currentPoint.getTime();
		HashMap<String, List<MSUSchedule>> scheduleMap = new HashMap<String, List<MSUSchedule>>();
		for (Iterator<String> taskIter = _regionTaskMap.keySet().iterator(); taskIter.hasNext();) {
			taskID = taskIter.next();
			taskObj = _regionTaskMap.get(taskID);
			try {
				if (taskObj.getIsRealtime() == true) {
					continue;
				}
				CronExpression cron = new CronExpression(taskObj.getSchedule());
				long nextPoint = cron.getNextValidTimeAfter(currentPoint).getTime();
				long flowTime = currentTime - nextPoint;
				// 1 hour.
				if (flowTime >= 0 && flowTime > 3600000L) {
					continue;
				}
				if (scheduleMap.containsKey(taskObj.getOperation()) == false) {
					List<MSUSchedule> scheduleList = new ArrayList<MSUSchedule>();
					scheduleList.add(TaskUtil.createScheduleOnTime(taskObj, nextPoint, currentTime));
					scheduleMap.put(taskObj.getOperation(), scheduleList);
				} else {
					List<MSUSchedule> scheduleList = scheduleMap.get(taskObj.getOperation());
					scheduleList.add(TaskUtil.createScheduleOnTime(taskObj, nextPoint, currentTime));
				}
			} catch (ParseException e) {
				theLogger.exception(e);
				continue;
			}
		}
		return scheduleMap;
	}

	/**
	 * report the stack size.
	 * 
	 * @return
	 */
	public JSONObject reportStackSize() {
		JSONObject report = new JSONObject();
		try {
			report.put("region", this._region);
			report.put("size", this._regionTaskMap.size());
			JSONArray taskArray = new JSONArray();
			for (Iterator<String> iter = this._regionTaskMap.keySet().iterator(); iter.hasNext();) {
				taskArray.put(iter.next());
			}
			report.put("detail", taskArray);
		} catch (JSONException e) {
			theLogger.exception(e);
		}
		return report;
	}
}
