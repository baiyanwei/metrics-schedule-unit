package com.secpro.platform.monitoring.schedule.services.taskunit;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.quartz.CronExpression;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.task.TaskUtil;

/**
 * @author baiyanwei Oct 17, 2013
 * 
 * 
 *         The task stack for each region.
 */
public class RegionTaskStack implements IService {
	public String _region = "HB";
	// key:Task ID, Value: Task Content
	private HashMap<String, MSUTask> _regionTaskMap = new HashMap<String, MSUTask>();

	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {

	}

	/**
	 * put a task into bottom
	 * 
	 * @param msuTask
	 */
	public MSUTask putMSUTask(MSUTask msuTask) throws Exception {
		if (msuTask == null || Assert.isEmptyString(msuTask.getID()) == true) {
			return null;
		}
		synchronized (_regionTaskMap) {
			if (_regionTaskMap.containsKey(msuTask.getID()) == false) {
				this._regionTaskMap.put(msuTask.getID(), msuTask);
			} else {
				throw new Exception(msuTask.getID() + " is already in Stack.");
			}
		}
		return msuTask;
	}

	/**
	 * put tasks into stack.
	 * 
	 * @param taskList
	 */
	public void putTasks(ArrayList<MSUTask> msuTaskList) {
		if (Assert.isEmptyCollection(msuTaskList) == true) {
			return;
		}
		synchronized (_regionTaskMap) {
			for (int i = 0; i < msuTaskList.size(); i++) {
				this._regionTaskMap.put(msuTaskList.get(i).getID(), msuTaskList.get(i));
			}
		}
	}

	/**
	 * @param num
	 * @return get task by request number.
	 */
	public MSUTask findMSUTask(String taskID) {
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
	public MSUTask removeMUSTask(String taskID) {
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
	public MSUTask updateMUSTask(MSUTask msuTask) {
		if (msuTask == null) {
			return null;
		}
		synchronized (_regionTaskMap) {
			this._regionTaskMap.put(msuTask.getID(), msuTask);
		}
		return msuTask;
	}

	/**
	 * report the stack size.
	 * 
	 * @return
	 */
	public int reportStackSize() {
		return this._regionTaskMap.size();
	}

	public HashMap<String, List<MSUSchedule>> nextHourSchedule(Date currentPoint) {
		String taskID = null;
		MSUTask taskObj = null;
		long currentTime = currentPoint.getTime();
		HashMap<String, List<MSUSchedule>> scheduleMap = new HashMap<String, List<MSUSchedule>>();
		for (Iterator<String> taskIter = _regionTaskMap.keySet().iterator(); taskIter.hasNext();) {
			taskID = taskIter.next();
			taskObj = _regionTaskMap.get(taskID);
			try {
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
				e.printStackTrace();
				continue;
			}
		}
		return scheduleMap;
	}

	
}
