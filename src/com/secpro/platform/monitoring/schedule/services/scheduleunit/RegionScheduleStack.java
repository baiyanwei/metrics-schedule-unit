package com.secpro.platform.monitoring.schedule.services.scheduleunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.utils.Assert;

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
	private HashMap<String, ArrayList<MSUTaskSchedule>> _operationRealtimeScheduleMap = new HashMap<String, ArrayList<MSUTaskSchedule>>();
	private HashMap<String, ArrayList<MSUTaskSchedule>> _operationFrequencyScheduleMap = new HashMap<String, ArrayList<MSUTaskSchedule>>();

	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {

	}

	/**
	 * put a task into bottom
	 * 
	 * @param taskSchedule
	 */
	public void putTaskToBottom(String operation, MSUTaskSchedule taskSchedule, boolean isRealtime) {
		if (taskSchedule == null || Assert.isEmptyString(operation) == true) {
			return;
		}
		putTaskWithPosition(operation, taskSchedule, 0, isRealtime);
	}

	/**
	 * put a task into top
	 * 
	 * @param taskSchedule
	 */
	public void putTaskToTop(String operation, MSUTaskSchedule taskSchedule, boolean isRealtime) {
		if (taskSchedule == null || Assert.isEmptyString(operation) == true) {
			return;
		}
		putTaskWithPosition(operation, taskSchedule, 0, isRealtime);
	}

	/**
	 * @param operation
	 * @param taskSchedule
	 * @param position
	 * 
	 *            position{0,1} 0 top ,other bottom.
	 */
	private void putTaskWithPosition(String operation, MSUTaskSchedule taskSchedule, int position, boolean isRealtime) {
		if (isRealtime == true) {
			synchronized (_operationRealtimeScheduleMap) {
				if (_operationRealtimeScheduleMap.containsKey(operation) == false) {
					ArrayList<MSUTaskSchedule> operationStack = new ArrayList<MSUTaskSchedule>();
					operationStack.add(taskSchedule);
					this._operationRealtimeScheduleMap.put(operation, operationStack);
				} else {
					ArrayList<MSUTaskSchedule> operationStack = _operationRealtimeScheduleMap.get(operation);
					if (position == 0) {
						operationStack.add(0, taskSchedule);
					} else {
						operationStack.add(taskSchedule);
					}
				}
			}
		} else {
			synchronized (_operationFrequencyScheduleMap) {
				if (_operationFrequencyScheduleMap.containsKey(operation) == false) {
					ArrayList<MSUTaskSchedule> operationStack = new ArrayList<MSUTaskSchedule>();
					operationStack.add(taskSchedule);
					this._operationFrequencyScheduleMap.put(operation, operationStack);
				} else {
					ArrayList<MSUTaskSchedule> operationStack = _operationFrequencyScheduleMap.get(operation);
					if (position == 0) {
						operationStack.add(0, taskSchedule);
					} else {
						operationStack.add(taskSchedule);
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
	public void putTasks(String operation, List<MSUTaskSchedule> taskList, boolean isRealtime) {
		if (Assert.isEmptyString(operation) == true || Assert.isEmptyCollection(taskList) == true) {
			return;
		}
		if (isRealtime == true) {
			synchronized (_operationRealtimeScheduleMap) {
				if (_operationRealtimeScheduleMap.containsKey(operation) == false) {
					ArrayList<MSUTaskSchedule> operationStack = new ArrayList<MSUTaskSchedule>();
					operationStack.addAll(taskList);
					this._operationRealtimeScheduleMap.put(operation, operationStack);
				} else {
					ArrayList<MSUTaskSchedule> operationStack = _operationRealtimeScheduleMap.get(operation);
					operationStack.addAll(taskList);
				}
			}
		} else {
			synchronized (_operationFrequencyScheduleMap) {
				if (_operationFrequencyScheduleMap.containsKey(operation) == false) {
					ArrayList<MSUTaskSchedule> operationStack = new ArrayList<MSUTaskSchedule>();
					operationStack.addAll(taskList);
					this._operationFrequencyScheduleMap.put(operation, operationStack);
				} else {
					ArrayList<MSUTaskSchedule> operationStack = _operationFrequencyScheduleMap.get(operation);
					operationStack.addAll(taskList);
				}
			}
		}
	}

	/**
	 * remove all tasks by ID(in all operations) ,
	 * 
	 * @param id
	 */
	public void removeTask(String id) {
		if (Assert.isEmptyString(id) == true) {
			return;
		}
		synchronized (_operationFrequencyScheduleMap) {
			for (Iterator<String> operationIter = _operationFrequencyScheduleMap.keySet().iterator(); operationIter.hasNext();) {
				removeTask(id, operationIter.next());
			}
		}
	}

	/**
	 * remove tasks by operation and ID.
	 * 
	 * @param id
	 * @param operation
	 */
	public void removeTask(String id, String operation) {
		if (Assert.isEmptyString(id) == true || Assert.isEmptyString(operation) == true) {
			return;
		}

		synchronized (_operationFrequencyScheduleMap) {
			if (_operationFrequencyScheduleMap.containsKey(operation) == false) {
				return;
			}
			ArrayList<MSUTaskSchedule> operationStack = _operationFrequencyScheduleMap.get(operation);
			for (int i = 0; i < operationStack.size(); i++) {
				if (id.equalsIgnoreCase(operationStack.get(i)._taskID) == true) {
					operationStack.remove(i);
					i--;
				}
			}

		}

	}

	/**
	 * @param num
	 * @return get task by request number.
	 */
	public List<MSUTaskSchedule> nextTasks(int num, String operationAbility) {
		if (num <= 0 || Assert.isEmptyString(operationAbility) == true) {
			return new ArrayList<MSUTaskSchedule>();
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

	private ArrayList<MSUTaskSchedule> distributeTaskInMin(int num, String[] operations) {
		ArrayList<MSUTaskSchedule> nextTaskArray = new ArrayList<MSUTaskSchedule>();
		if (num == 1) {
			// first realtime task
			synchronized (_operationRealtimeScheduleMap) {
				for (int operationIndex = 0; operationIndex < operations.length; operationIndex++) {
					if (_operationRealtimeScheduleMap.containsKey(operations[operationIndex]) == false) {
						continue;
					}
					ArrayList<MSUTaskSchedule> operationStack = _operationRealtimeScheduleMap.get(operations[operationIndex]);
					if (operationStack.isEmpty() == true) {
						continue;
					}
					nextTaskArray.add(operationStack.remove(0));
					return nextTaskArray;
				}
			}
			// second frequency task.
			synchronized (_operationFrequencyScheduleMap) {
				for (int operationIndex = 0; operationIndex < operations.length; operationIndex++) {
					if (_operationFrequencyScheduleMap.containsKey(operations[operationIndex]) == false) {
						continue;
					}
					ArrayList<MSUTaskSchedule> operationStack = _operationFrequencyScheduleMap.get(operations[operationIndex]);
					if (operationStack.isEmpty() == true) {
						continue;
					}
					nextTaskArray.add(operationStack.remove(0));
					return nextTaskArray;
				}
			}
			return nextTaskArray;
		} else if (operations.length == 1) {
			// first realtime task
			synchronized (_operationRealtimeScheduleMap) {
				if (_operationRealtimeScheduleMap.containsKey(operations[0]) == true) {
					ArrayList<MSUTaskSchedule> operationStack = _operationRealtimeScheduleMap.get(operations[0]);
					for (int i = 0; i < num && i < operationStack.size(); i++) {
						nextTaskArray.add(operationStack.remove(0));
					}

				}
			}
			if (nextTaskArray.size() >= num) {
				return nextTaskArray;
			}
			// second frequency task.
			synchronized (_operationFrequencyScheduleMap) {
				if (_operationFrequencyScheduleMap.containsKey(operations[0]) == true) {
					ArrayList<MSUTaskSchedule> operationStack = _operationFrequencyScheduleMap.get(operations[0]);
					for (int i = 0, groupSize = num - nextTaskArray.size(); i < groupSize && i < operationStack.size(); i++) {
						nextTaskArray.add(operationStack.remove(0));
					}

				}
			}
			return nextTaskArray;
		} else {
			return distributeTaskInFast(num, operations);
		}
	}

	private ArrayList<MSUTaskSchedule> distributeTaskInFast(int num, String[] operations) {
		ArrayList<MSUTaskSchedule> nextTaskArray = new ArrayList<MSUTaskSchedule>();
		synchronized (_operationRealtimeScheduleMap) {
			// sort the operation by queue size.
			ArrayList<Object[]> operationList = sortOperationBySize(_operationRealtimeScheduleMap, operations);
			fillTaskInAverage(_operationRealtimeScheduleMap, operationList, num, nextTaskArray);
		}
		if (nextTaskArray.size() >= num) {
			return nextTaskArray;
		}
		synchronized (_operationFrequencyScheduleMap) {
			// sort the operation by queue size.
			ArrayList<Object[]> operationList = sortOperationBySize(_operationFrequencyScheduleMap, operations);
			fillTaskInAverage(_operationFrequencyScheduleMap, operationList, num, nextTaskArray);
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
	private ArrayList<MSUTaskSchedule> distributeTaskInAverage(int num, String[] operations) {
		ArrayList<MSUTaskSchedule> nextTaskArray = new ArrayList<MSUTaskSchedule>();
		synchronized (_operationRealtimeScheduleMap) {
			// sort the operation by queue size.
			ArrayList<Object[]> operationList = sortOperationBySize(_operationRealtimeScheduleMap, operations);
			fillTaskInAverage(_operationRealtimeScheduleMap, operationList, num, nextTaskArray);
		}
		if (nextTaskArray.size() >= num) {
			return nextTaskArray;
		}
		synchronized (_operationFrequencyScheduleMap) {
			// sort the operation by queue size.
			ArrayList<Object[]> operationList = sortOperationBySize(_operationFrequencyScheduleMap, operations);
			fillTaskInAverage(_operationFrequencyScheduleMap, operationList, num, nextTaskArray);
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
	private void fillTaskInAverage(HashMap<String, ArrayList<MSUTaskSchedule>> operationTaskMap, ArrayList<Object[]> operationList, int num, ArrayList<MSUTaskSchedule> nextTaskArray) {
		// fetch task average,
		// (N-S)+(O-I)-1/(O-I)
		for (int operationIndex = 0; operationIndex < operationList.size(); operationIndex++) {
			if (operationTaskMap.containsKey(String.valueOf(operationList.get(operationIndex)[0])) == false) {
				continue;
			}
			ArrayList<MSUTaskSchedule> operationStack = operationTaskMap.get(String.valueOf(operationList.get(operationIndex)[0]));
			for (int size = operationStack.size(), i = 0, groupSize = (num - nextTaskArray.size() + operationList.size() - operationIndex - 1)
					/ (operationList.size() - operationIndex); i < groupSize && i < size; i++) {
				nextTaskArray.add(operationStack.remove(0));
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
	private ArrayList<Object[]> sortOperationBySize(HashMap<String, ArrayList<MSUTaskSchedule>> operationTaskMap, String[] operations) {
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
