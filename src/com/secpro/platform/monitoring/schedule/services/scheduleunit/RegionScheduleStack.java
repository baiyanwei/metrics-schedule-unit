package com.secpro.platform.monitoring.schedule.services.scheduleunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.monitoring.schedule.task.TaskConstant;

/**
 * @author baiyanwei Oct 17, 2013
 * 
 * 
 *         The schedule task stack for each region.
 */
public class RegionScheduleStack implements IService {
	final public static int DISTRIBUTE_TASK_TYPE_AVERAGE = 0;
	final public static int DISTRIBUTE_TASK_TYPE_FAST = 1;
	public String _region = "HB";
	public int _distributeTaskType = 0;
	private HashMap<String, ArrayList<JSONObject>> _operationRealtimeTaskMap = new HashMap<String, ArrayList<JSONObject>>();
	private HashMap<String, ArrayList<JSONObject>> _operationFrequencyTaskMap = new HashMap<String, ArrayList<JSONObject>>();

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
	public void putTaskToBottom(String operation, JSONObject taskObj, boolean isRealtime) {
		if (taskObj == null || Assert.isEmptyString(operation) == true) {
			return;
		}
		putTaskWithPosition(operation, taskObj, 0, isRealtime);
	}

	/**
	 * put a task into top
	 * 
	 * @param taskObj
	 */
	public void putTaskToTop(String operation, JSONObject taskObj, boolean isRealtime) {
		if (taskObj == null || Assert.isEmptyString(operation) == true) {
			return;
		}
		putTaskWithPosition(operation, taskObj, 0, isRealtime);
	}

	/**
	 * @param operation
	 * @param taskObj
	 * @param position
	 * 
	 *            position{0,1} 0 top ,other bottom.
	 */
	private void putTaskWithPosition(String operation, JSONObject taskObj, int position, boolean isRealtime) {
		if (isRealtime == true) {
			synchronized (_operationRealtimeTaskMap) {
				if (_operationRealtimeTaskMap.containsKey(operation) == false) {
					ArrayList<JSONObject> operationStack = new ArrayList<JSONObject>();
					operationStack.add(taskObj);
					this._operationRealtimeTaskMap.put(operation, operationStack);
				} else {
					ArrayList<JSONObject> operationStack = _operationRealtimeTaskMap.get(operation);
					if (position == 0) {
						operationStack.add(0, taskObj);
					} else {
						operationStack.add(taskObj);
					}
				}
			}
		} else {
			synchronized (_operationFrequencyTaskMap) {
				if (_operationFrequencyTaskMap.containsKey(operation) == false) {
					ArrayList<JSONObject> operationStack = new ArrayList<JSONObject>();
					operationStack.add(taskObj);
					this._operationFrequencyTaskMap.put(operation, operationStack);
				} else {
					ArrayList<JSONObject> operationStack = _operationFrequencyTaskMap.get(operation);
					if (position == 0) {
						operationStack.add(0, taskObj);
					} else {
						operationStack.add(taskObj);
					}
				}
			}
		}
	}

	/**
	 * @param operation
	 * @param taskList
	 * 
	 *            put task into stack.
	 */
	public void putTasks(String operation, List<JSONObject> taskList, boolean isRealtime) {
		if (Assert.isEmptyString(operation) == true || Assert.isEmptyCollection(taskList) == true) {
			return;
		}
		if (isRealtime == true) {
			synchronized (_operationRealtimeTaskMap) {
				if (_operationRealtimeTaskMap.containsKey(operation) == false) {
					ArrayList<JSONObject> operationStack = new ArrayList<JSONObject>();
					operationStack.addAll(taskList);
					this._operationRealtimeTaskMap.put(operation, operationStack);
				} else {
					ArrayList<JSONObject> operationStack = _operationRealtimeTaskMap.get(operation);
					operationStack.addAll(taskList);
				}
			}
		} else {
			synchronized (_operationFrequencyTaskMap) {
				if (_operationFrequencyTaskMap.containsKey(operation) == false) {
					ArrayList<JSONObject> operationStack = new ArrayList<JSONObject>();
					operationStack.addAll(taskList);
					this._operationFrequencyTaskMap.put(operation, operationStack);
				} else {
					ArrayList<JSONObject> operationStack = _operationFrequencyTaskMap.get(operation);
					operationStack.addAll(taskList);
				}
			}
		}
	}

	public void removeTask(String id) {
		if (Assert.isEmptyString(id) == true) {
			return;
		}
		synchronized (_operationFrequencyTaskMap) {
			for (Iterator<String> operationIter = _operationFrequencyTaskMap.keySet().iterator(); operationIter.hasNext();) {
				removeTask(id, operationIter.next());
			}
		}
	}

	public void removeTask(String id, String operation) {
		if (Assert.isEmptyString(id) == true || Assert.isEmptyString(operation) == true) {
			return;
		}

		synchronized (_operationFrequencyTaskMap) {
			if (_operationFrequencyTaskMap.containsKey(operation) == false) {
				return;
			}
			ArrayList<JSONObject> operationStack = _operationFrequencyTaskMap.get(operation);
			for (int i = 0; i < operationStack.size(); i++) {
				try {
					if (id.equalsIgnoreCase(operationStack.get(i).getString(TaskConstant.TASK_ID)) == true) {
						operationStack.remove(i);
						i--;
					}
				} catch (Exception e) {
					e.printStackTrace();
					continue;
				}
			}

		}

	}

	/**
	 * @param num
	 * @return get task by request number.
	 */
	public JSONArray nextTasks(int num, String operationAbility) {
		if (num <= 0 || Assert.isEmptyString(operationAbility) == true) {
			return new JSONArray();
		}
		// split operation.
		// I.E. ssh,telnet,snmp
		String[] operations = operationAbility.split(",");
		if (num == 1 || operations.length == 1) {
			return distributeTaskInMin(num, operations);
		} else {
			switch (_distributeTaskType) {
			case RegionScheduleStack.DISTRIBUTE_TASK_TYPE_AVERAGE:
				return distributeTaskInAverage(num, operations);
			case RegionScheduleStack.DISTRIBUTE_TASK_TYPE_FAST:
				return distributeTaskInFast(num, operations);
			default:
				return distributeTaskInAverage(num, operations);
			}
		}
	}

	private JSONArray distributeTaskInMin(int num, String[] operations) {
		JSONArray nextTaskArray = new JSONArray();
		if (num == 1) {
			// first realtime task
			synchronized (_operationRealtimeTaskMap) {
				for (int operationIndex = 0; operationIndex < operations.length; operationIndex++) {
					if (_operationRealtimeTaskMap.containsKey(operations[operationIndex]) == false) {
						continue;
					}
					ArrayList<JSONObject> operationStack = _operationRealtimeTaskMap.get(operations[operationIndex]);
					if (operationStack.isEmpty() == true) {
						continue;
					}
					nextTaskArray.put(operationStack.remove(0));
					return nextTaskArray;
				}
			}
			// second frequency task.
			synchronized (_operationFrequencyTaskMap) {
				for (int operationIndex = 0; operationIndex < operations.length; operationIndex++) {
					if (_operationFrequencyTaskMap.containsKey(operations[operationIndex]) == false) {
						continue;
					}
					ArrayList<JSONObject> operationStack = _operationFrequencyTaskMap.get(operations[operationIndex]);
					if (operationStack.isEmpty() == true) {
						continue;
					}
					nextTaskArray.put(operationStack.remove(0));
					return nextTaskArray;
				}
			}
			return nextTaskArray;
		} else if (operations.length == 1) {
			// first realtime task
			synchronized (_operationRealtimeTaskMap) {
				if (_operationRealtimeTaskMap.containsKey(operations[0]) == true) {
					ArrayList<JSONObject> operationStack = _operationRealtimeTaskMap.get(operations[0]);
					for (int i = 0; i < num && i < operationStack.size(); i++) {
						nextTaskArray.put(operationStack.remove(0));
					}

				}
			}
			if (nextTaskArray.length() >= num) {
				return nextTaskArray;
			}
			// second frequency task.
			synchronized (_operationFrequencyTaskMap) {
				if (_operationFrequencyTaskMap.containsKey(operations[0]) == true) {
					ArrayList<JSONObject> operationStack = _operationFrequencyTaskMap.get(operations[0]);
					for (int i = 0, groupSize = num - nextTaskArray.length(); i < groupSize && i < operationStack.size(); i++) {
						nextTaskArray.put(operationStack.remove(0));
					}

				}
			}
			return nextTaskArray;
		} else {
			return distributeTaskInFast(num, operations);
		}
	}

	private JSONArray distributeTaskInFast(int num, String[] operations) {
		JSONArray nextTaskArray = new JSONArray();
		synchronized (_operationRealtimeTaskMap) {
			// sort the operation by queue size.
			ArrayList<Object[]> operationList = sortOperationBySize(_operationRealtimeTaskMap, operations);
			fillTaskInAverage(_operationRealtimeTaskMap, operationList, num, nextTaskArray);
		}
		if (nextTaskArray.length() >= num) {
			return nextTaskArray;
		}
		synchronized (_operationFrequencyTaskMap) {
			// sort the operation by queue size.
			ArrayList<Object[]> operationList = sortOperationBySize(_operationFrequencyTaskMap, operations);
			fillTaskInAverage(_operationFrequencyTaskMap, operationList, num, nextTaskArray);
		}
		return nextTaskArray;
	}

	/**
	 * distribute task in average.
	 * 
	 * @param num
	 * @param operations
	 * @return
	 */
	private JSONArray distributeTaskInAverage(int num, String[] operations) {
		JSONArray nextTaskArray = new JSONArray();
		synchronized (_operationRealtimeTaskMap) {
			// sort the operation by queue size.
			ArrayList<Object[]> operationList = sortOperationBySize(_operationRealtimeTaskMap, operations);
			fillTaskInAverage(_operationRealtimeTaskMap, operationList, num, nextTaskArray);
		}
		if (nextTaskArray.length() >= num) {
			return nextTaskArray;
		}
		synchronized (_operationFrequencyTaskMap) {
			// sort the operation by queue size.
			ArrayList<Object[]> operationList = sortOperationBySize(_operationFrequencyTaskMap, operations);
			fillTaskInAverage(_operationFrequencyTaskMap, operationList, num, nextTaskArray);
		}
		return nextTaskArray;
	}

	/**
	 * fill task array in average.
	 * 
	 * @param operationTaskMap
	 * @param operationList
	 * @param num
	 * @param nextTaskArray
	 */
	private void fillTaskInAverage(HashMap<String, ArrayList<JSONObject>> operationTaskMap, ArrayList<Object[]> operationList, int num, JSONArray nextTaskArray) {
		// fetch task average,
		// (N-S)+(O-I)-1/(O-I)
		for (int operationIndex = 0; operationIndex < operationList.size(); operationIndex++) {
			if (operationTaskMap.containsKey(String.valueOf(operationList.get(operationIndex)[0])) == false) {
				continue;
			}
			ArrayList<JSONObject> operationStack = operationTaskMap.get(String.valueOf(operationList.get(operationIndex)[0]));
			for (int size = operationStack.size(), i = 0, groupSize = (num - nextTaskArray.length() + operationList.size() - operationIndex - 1)
					/ (operationList.size() - operationIndex); i < groupSize && i < size; i++) {
				nextTaskArray.put(operationStack.remove(0));
			}
		}
	}

	/**
	 * sort the operation by stack size.
	 * 
	 * @param operationTaskMap
	 * @param operations
	 * @return
	 */
	private ArrayList<Object[]> sortOperationBySize(HashMap<String, ArrayList<JSONObject>> operationTaskMap, String[] operations) {
		ArrayList<Object[]> operationList = new ArrayList<Object[]>();
		// # find the operation stack size.
		for (int operationIndex = 0; operationIndex < operations.length; operationIndex++) {
			if (operationTaskMap.containsKey(operations[operationIndex]) == false) {
				continue;
			}
			operationList.add(new Object[] { operations[operationIndex], operationTaskMap.get(operations[operationIndex]).size() });
		}
		Object[] changeObj = null;
		// # sort operation by size.
		for (int i = 0, size = operationList.size() - 1; i < size; i++) {
			for (int j = i + 1; j < operationList.size(); j++) {
				if (((Integer) operationList.get(i)[1]).intValue() > ((Integer) operationList.get(j)[1]).intValue()) {
					changeObj = operationList.get(i);
					operationList.set(i, operationList.get(j));
					operationList.set(j, changeObj);
				}
			}
		}
		return operationList;
	}
}
