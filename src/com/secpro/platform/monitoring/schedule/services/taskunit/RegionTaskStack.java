package com.secpro.platform.monitoring.schedule.services.taskunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.utils.Assert;

/**
 * @author baiyanwei Oct 17, 2013
 * 
 * 
 *         The task stack for each local.
 */
public class RegionTaskStack implements IService {
	public String _region = "HB";
	private HashMap<String, ArrayList<JSONObject>> _operationTaskMap = new HashMap<String, ArrayList<JSONObject>>();

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
	public void putTaskToBottom(String operation, JSONObject taskObj) {
		if (taskObj == null || Assert.isEmptyString(operation) == true) {
			return;
		}
		putTaskWithPosition(operation, taskObj, 0);
	}

	/**
	 * put a task into top
	 * 
	 * @param taskObj
	 */
	public void putTaskToTop(String operation, JSONObject taskObj) {
		if (taskObj == null || Assert.isEmptyString(operation) == true) {
			return;
		}
		putTaskWithPosition(operation, taskObj, 0);
	}

	/**
	 * @param operation
	 * @param taskObj
	 * @param position
	 * 
	 *            position{0,1} 0 top ,other bottom.
	 */
	private void putTaskWithPosition(String operation, JSONObject taskObj, int position) {
		synchronized (_operationTaskMap) {
			if (_operationTaskMap.containsKey(operation) == false) {
				ArrayList<JSONObject> operationStack = new ArrayList<JSONObject>();
				operationStack.add(taskObj);
				this._operationTaskMap.put(operation, operationStack);
			} else {
				ArrayList<JSONObject> operationStack = _operationTaskMap.get(operation);
				if (position == 0) {
					operationStack.add(0, taskObj);
				} else {
					operationStack.add(taskObj);
				}
			}
		}
	}

	/**
	 * @param operation
	 * @param taskList
	 * 
	 * put task into stack.
	 */
	public void putTasks(String operation, List<JSONObject> taskList) {
		if (Assert.isEmptyString(operation) == true || Assert.isEmptyCollection(taskList) == true) {
			return;
		}
		synchronized (_operationTaskMap) {
			if (_operationTaskMap.containsKey(operation) == false) {
				ArrayList<JSONObject> operationStack = new ArrayList<JSONObject>();
				operationStack.addAll(taskList);
				this._operationTaskMap.put(operation, operationStack);
			} else {
				ArrayList<JSONObject> operationStack = _operationTaskMap.get(operation);
				operationStack.addAll(taskList);
			}
		}
	}

	/**
	 * @param num
	 * @return get task by request number.
	 */
	public JSONArray nextTasks(int num, String[] operations) {
		if (num <= 0 || operations == null || operations.length == 0) {
			return null;
		}
		synchronized (_operationTaskMap) {
			JSONArray nextTaskArray = new JSONArray();
			for (int operationIndex = 0; operationIndex < operations.length; operationIndex++) {
				if (_operationTaskMap.containsKey(operations[operationIndex]) == false) {
					continue;
				}
				ArrayList<JSONObject> operationStack = _operationTaskMap.get(operations[operationIndex]);
				for (int size = operationStack.size(), i = 0; i < num && i < size; i++) {
					nextTaskArray.put(operationStack.remove(0));
				}
			}
			return nextTaskArray;
		}
	}
}
