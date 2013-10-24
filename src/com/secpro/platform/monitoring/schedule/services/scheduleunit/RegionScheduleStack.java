package com.secpro.platform.monitoring.schedule.services.scheduleunit;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import com.secpro.platform.core.services.ILife;
import com.secpro.platform.core.utils.Assert;

/**
 * @author baiyanwei Oct 17, 2013
 * 
 * 
 *         The schedule task stack for each region.
 */
public class RegionScheduleStack implements ILife {
	final public static int DISTRIBUTE_SCHEDULE_TYPE_AVERAGE = 0;
	final public static int DISTRIBUTE_SCHEDULE_TYPE_FAST = 1;
	public String _region = "HB";
	public int _distributeScheduleType = 0;
	private HashMap<String, ArrayList<MSUSchedule>> _operationRealtimeScheduleMap = new HashMap<String, ArrayList<MSUSchedule>>();
	private HashMap<String, ArrayList<MSUSchedule>> _operationFrequencyScheduleMap = new HashMap<String, ArrayList<MSUSchedule>>();

	@Override
	public void start() throws Exception {
	}

	@Override
	public void stop() throws Exception {

	}

	public List<MSUSchedule> findSchedules(String operation, String taskID) {
		if (Assert.isEmptyString(operation) == true || Assert.isEmptyString(taskID) == true) {
			return new ArrayList<MSUSchedule>();
		}
		List<MSUSchedule> findList = findSchedulesInStack(operation, taskID, this._operationRealtimeScheduleMap);
		findList.addAll(findSchedulesInStack(operation, taskID, this._operationFrequencyScheduleMap));
		return findList;
	}

	public HashMap<String, List<MSUSchedule>> findSchedules(String taskID) {
		if (Assert.isEmptyString(taskID) == true) {
			return new HashMap<String, List<MSUSchedule>>();
		}
		HashMap<String, List<MSUSchedule>> findMap = new HashMap<String, List<MSUSchedule>>();
		String operation = null;
		List<MSUSchedule> findList = null;
		for (Iterator<String> operationIter = this._operationRealtimeScheduleMap.keySet().iterator(); operationIter.hasNext();) {
			operation = operationIter.next();
			findList = findSchedulesInStack(operation, taskID, this._operationRealtimeScheduleMap);
			if (findList == null || findList.isEmpty()) {
				continue;
			}
			findMap.put(operation, findList);
		}
		for (Iterator<String> operationIter = this._operationFrequencyScheduleMap.keySet().iterator(); operationIter.hasNext();) {
			operation = operationIter.next();
			findList = findSchedulesInStack(operation, taskID, this._operationFrequencyScheduleMap);
			if (findList == null || findList.isEmpty()) {
				continue;
			}
			if (findMap.containsKey(operation)) {
				List<MSUSchedule> realTimeList = findMap.get(operation);
				realTimeList.addAll(findList);
			} else {
				findMap.put(operation, findList);
			}
		}
		return findMap;
	}

	private List<MSUSchedule> findSchedulesInStack(String operation, String taskID, HashMap<String, ArrayList<MSUSchedule>> scheduleMap) {
		if (scheduleMap.containsKey(operation) == false) {
			return new ArrayList<MSUSchedule>();
		}
		ArrayList<MSUSchedule> findList = new ArrayList<MSUSchedule>();
		ArrayList<MSUSchedule> scheduleList = scheduleMap.get(operation);
		for (int i = 0; i < scheduleList.size(); i++) {
			if (taskID.equalsIgnoreCase(scheduleList.get(i)._taskID) == true) {
				findList.add(scheduleList.get(i));
			}
		}
		return findList;
	}

	/**
	 * put a task into bottom
	 * 
	 * @param taskSchedule
	 */
	public void putScheduleToBottom(String operation, MSUSchedule taskSchedule, boolean isRealtime) {
		if (taskSchedule == null || Assert.isEmptyString(operation) == true) {
			return;
		}
		putScheduleWithPosition(operation, taskSchedule, 0, isRealtime);
	}

	/**
	 * put a task into top
	 * 
	 * @param taskSchedule
	 */
	public void putScheduleToTop(String operation, MSUSchedule taskSchedule, boolean isRealtime) {
		if (taskSchedule == null || Assert.isEmptyString(operation) == true) {
			return;
		}
		putScheduleWithPosition(operation, taskSchedule, 0, isRealtime);
	}

	/**
	 * @param operation
	 * @param taskSchedule
	 * @param position
	 * 
	 *            position{0,1} 0 top ,other bottom.
	 */
	private void putScheduleWithPosition(String operation, MSUSchedule taskSchedule, int position, boolean isRealtime) {
		if (isRealtime == true) {
			synchronized (_operationRealtimeScheduleMap) {
				if (_operationRealtimeScheduleMap.containsKey(operation) == false) {
					ArrayList<MSUSchedule> operationStack = new ArrayList<MSUSchedule>();
					operationStack.add(taskSchedule);
					this._operationRealtimeScheduleMap.put(operation, operationStack);
				} else {
					ArrayList<MSUSchedule> operationStack = _operationRealtimeScheduleMap.get(operation);
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
					ArrayList<MSUSchedule> operationStack = new ArrayList<MSUSchedule>();
					operationStack.add(taskSchedule);
					this._operationFrequencyScheduleMap.put(operation, operationStack);
				} else {
					ArrayList<MSUSchedule> operationStack = _operationFrequencyScheduleMap.get(operation);
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
	public void putSchedules(String operation, List<MSUSchedule> taskList, boolean isRealtime) {
		if (Assert.isEmptyString(operation) == true || Assert.isEmptyCollection(taskList) == true) {
			return;
		}
		if (isRealtime == true) {
			synchronized (_operationRealtimeScheduleMap) {
				if (_operationRealtimeScheduleMap.containsKey(operation) == false) {
					ArrayList<MSUSchedule> operationStack = new ArrayList<MSUSchedule>();
					operationStack.addAll(taskList);
					this._operationRealtimeScheduleMap.put(operation, operationStack);
				} else {
					ArrayList<MSUSchedule> operationStack = _operationRealtimeScheduleMap.get(operation);
					operationStack.addAll(taskList);
				}
			}
		} else {
			synchronized (_operationFrequencyScheduleMap) {
				if (_operationFrequencyScheduleMap.containsKey(operation) == false) {
					ArrayList<MSUSchedule> operationStack = new ArrayList<MSUSchedule>();
					operationStack.addAll(taskList);
					this._operationFrequencyScheduleMap.put(operation, operationStack);
				} else {
					ArrayList<MSUSchedule> operationStack = _operationFrequencyScheduleMap.get(operation);
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
	public void removeSchedule(String id) {
		if (Assert.isEmptyString(id) == true) {
			return;
		}
		synchronized (_operationFrequencyScheduleMap) {
			for (Iterator<String> operationIter = _operationFrequencyScheduleMap.keySet().iterator(); operationIter.hasNext();) {
				removeSchedule(id, operationIter.next());
			}
		}
	}

	/**
	 * remove tasks by operation and ID.
	 * 
	 * @param id
	 * @param operation
	 */
	public void removeSchedule(String id, String operation) {
		if (Assert.isEmptyString(id) == true || Assert.isEmptyString(operation) == true) {
			return;
		}

		synchronized (_operationFrequencyScheduleMap) {
			if (_operationFrequencyScheduleMap.containsKey(operation) == false) {
				return;
			}
			ArrayList<MSUSchedule> operationStack = _operationFrequencyScheduleMap.get(operation);
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
	public List<MSUSchedule> nextSchedules(int num, String operationAbility) {
		if (num <= 0 || Assert.isEmptyString(operationAbility) == true) {
			return new ArrayList<MSUSchedule>();
		}
		// split operation.
		// I.E. ssh,telnet,snmp
		String[] operations = operationAbility.split(",");
		if (num == 1 || operations.length == 1) {
			return distributeScheduleInMin(num, operations);
		} else {
			switch (_distributeScheduleType) {
			case RegionScheduleStack.DISTRIBUTE_SCHEDULE_TYPE_AVERAGE:
				return distributeScheduleInAverage(num, operations);
			case RegionScheduleStack.DISTRIBUTE_SCHEDULE_TYPE_FAST:
				return distributeScheduleInFast(num, operations);
			default:
				return distributeScheduleInAverage(num, operations);
			}
		}
	}

	private ArrayList<MSUSchedule> distributeScheduleInMin(int num, String[] operations) {
		ArrayList<MSUSchedule> nextScheduleArray = new ArrayList<MSUSchedule>();
		if (num == 1) {
			// first realtime task
			synchronized (_operationRealtimeScheduleMap) {
				for (int operationIndex = 0; operationIndex < operations.length; operationIndex++) {
					if (_operationRealtimeScheduleMap.containsKey(operations[operationIndex]) == false) {
						continue;
					}
					ArrayList<MSUSchedule> operationStack = _operationRealtimeScheduleMap.get(operations[operationIndex]);
					if (operationStack.isEmpty() == true) {
						continue;
					}
					nextScheduleArray.add(operationStack.remove(0));
					return nextScheduleArray;
				}
			}
			// second frequency task.
			synchronized (_operationFrequencyScheduleMap) {
				for (int operationIndex = 0; operationIndex < operations.length; operationIndex++) {
					if (_operationFrequencyScheduleMap.containsKey(operations[operationIndex]) == false) {
						continue;
					}
					ArrayList<MSUSchedule> operationStack = _operationFrequencyScheduleMap.get(operations[operationIndex]);
					if (operationStack.isEmpty() == true) {
						continue;
					}
					nextScheduleArray.add(operationStack.remove(0));
					return nextScheduleArray;
				}
			}
			return nextScheduleArray;
		} else if (operations.length == 1) {
			// first realtime task
			synchronized (_operationRealtimeScheduleMap) {
				if (_operationRealtimeScheduleMap.containsKey(operations[0]) == true) {
					ArrayList<MSUSchedule> operationStack = _operationRealtimeScheduleMap.get(operations[0]);
					for (int i = 0; i < num && i < operationStack.size(); i++) {
						nextScheduleArray.add(operationStack.remove(0));
					}

				}
			}
			if (nextScheduleArray.size() >= num) {
				return nextScheduleArray;
			}
			// second frequency task.
			synchronized (_operationFrequencyScheduleMap) {
				if (_operationFrequencyScheduleMap.containsKey(operations[0]) == true) {
					ArrayList<MSUSchedule> operationStack = _operationFrequencyScheduleMap.get(operations[0]);
					for (int i = 0, groupSize = num - nextScheduleArray.size(); i < groupSize && i < operationStack.size(); i++) {
						nextScheduleArray.add(operationStack.remove(0));
					}

				}
			}
			return nextScheduleArray;
		} else {
			return distributeScheduleInFast(num, operations);
		}
	}

	private ArrayList<MSUSchedule> distributeScheduleInFast(int num, String[] operations) {
		ArrayList<MSUSchedule> nextScheduleArray = new ArrayList<MSUSchedule>();
		synchronized (_operationRealtimeScheduleMap) {
			// sort the operation by queue size.
			ArrayList<Object[]> operationList = sortOperationBySize(_operationRealtimeScheduleMap, operations);
			fillScheduleInAverage(_operationRealtimeScheduleMap, operationList, num, nextScheduleArray);
		}
		if (nextScheduleArray.size() >= num) {
			return nextScheduleArray;
		}
		synchronized (_operationFrequencyScheduleMap) {
			// sort the operation by queue size.
			ArrayList<Object[]> operationList = sortOperationBySize(_operationFrequencyScheduleMap, operations);
			fillScheduleInAverage(_operationFrequencyScheduleMap, operationList, num, nextScheduleArray);
		}
		return nextScheduleArray;
	}

	/**
	 * distribute task in average.
	 * 
	 * @param num
	 * @param operations
	 * @return
	 */
	private ArrayList<MSUSchedule> distributeScheduleInAverage(int num, String[] operations) {
		ArrayList<MSUSchedule> nextScheduleArray = new ArrayList<MSUSchedule>();
		synchronized (_operationRealtimeScheduleMap) {
			// sort the operation by queue size.
			ArrayList<Object[]> operationList = sortOperationBySize(_operationRealtimeScheduleMap, operations);
			fillScheduleInAverage(_operationRealtimeScheduleMap, operationList, num, nextScheduleArray);
		}
		if (nextScheduleArray.size() >= num) {
			return nextScheduleArray;
		}
		synchronized (_operationFrequencyScheduleMap) {
			// sort the operation by queue size.
			ArrayList<Object[]> operationList = sortOperationBySize(_operationFrequencyScheduleMap, operations);
			fillScheduleInAverage(_operationFrequencyScheduleMap, operationList, num, nextScheduleArray);
		}
		return nextScheduleArray;
	}

	/**
	 * fill task array in average.
	 * 
	 * @param operationTaskMap
	 * @param operationList
	 * @param num
	 * @param nextTaskArray
	 */
	private void fillScheduleInAverage(HashMap<String, ArrayList<MSUSchedule>> operationTaskMap, ArrayList<Object[]> operationList, int num, ArrayList<MSUSchedule> nextTaskArray) {
		// fetch task average,
		// (N-S)+(O-I)-1/(O-I)
		for (int operationIndex = 0; operationIndex < operationList.size(); operationIndex++) {
			if (operationTaskMap.containsKey(String.valueOf(operationList.get(operationIndex)[0])) == false) {
				continue;
			}
			ArrayList<MSUSchedule> operationStack = operationTaskMap.get(String.valueOf(operationList.get(operationIndex)[0]));
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
	private ArrayList<Object[]> sortOperationBySize(HashMap<String, ArrayList<MSUSchedule>> operationTaskMap, String[] operations) {
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

	public String reportStackSize() {
		// TODO Auto-generated method stub
		return null;
	}

}
