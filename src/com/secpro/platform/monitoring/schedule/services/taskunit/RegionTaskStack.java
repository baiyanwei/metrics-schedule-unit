package com.secpro.platform.monitoring.schedule.services.taskunit;

import java.util.ArrayList;
import java.util.HashMap;

import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.utils.Assert;

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
		if (msuTask == null) {
			return null;
		}
		synchronized (_regionTaskMap) {
			if (_regionTaskMap.containsKey(msuTask._id) == false) {
				this._regionTaskMap.put(msuTask._id, msuTask);
			} else {
				throw new Exception(msuTask._id + " is already in Stack.");
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
				this._regionTaskMap.put(msuTaskList.get(i)._id, msuTaskList.get(i));
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
	public MSUTask updateMUSTask(String taskID, MSUTask msuTask) {
		if (Assert.isEmptyString(taskID) == true || msuTask == null) {
			return null;
		}
		synchronized (_regionTaskMap) {
			this._regionTaskMap.put(taskID, msuTask);
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
}
