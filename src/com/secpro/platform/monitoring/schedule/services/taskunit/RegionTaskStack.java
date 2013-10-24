package com.secpro.platform.monitoring.schedule.services.taskunit;

import java.util.HashMap;

import org.json.JSONObject;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.monitoring.schedule.task.TaskConstant;

/**
 * @author baiyanwei Oct 17, 2013
 * 
 * 
 *         The task stack for each region.
 */
public class RegionTaskStack implements IService {
	public String _region = "HB";
	private HashMap<String, JSONObject> _regionTaskMap = new HashMap<String, JSONObject>();

	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {

	}

	/**
	 * put a task into bottom
	 * 
	 * @param taskObj
	 */
	public void putTask(JSONObject taskObj) throws Exception {
		if (taskObj == null || taskObj.has(TaskConstant.TASK_ID) == false) {
			return;
		}
		String taskID = taskObj.getString(TaskConstant.TASK_ID);
		if (Assert.isEmptyString(taskID) == false) {
			return;
		}
		synchronized (_regionTaskMap) {
			if (_regionTaskMap.containsKey(taskID) == false) {
				this._regionTaskMap.put(taskID, taskObj);
			} else {
				throw new Exception(taskID + " is already in Stack.");
			}
		}
	}

	/**
	 * @param num
	 * @return get task by request number.
	 */
	public JSONObject lookupTask(String taskID) {
		if (Assert.isEmptyString(taskID) == false) {
			return null;
		}
		return this._regionTaskMap.get(taskID);
	}
}
