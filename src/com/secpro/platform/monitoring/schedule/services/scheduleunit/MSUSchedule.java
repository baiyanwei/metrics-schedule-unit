package com.secpro.platform.monitoring.schedule.services.scheduleunit;

/**
 * @author baiyanwei Oct 24, 2013
 * 
 *         Task schedule bean.
 * 
 */
public class MSUSchedule {
	public long _scheduleTimePoint = 0;
	public String _taskID = "";

	public MSUSchedule(long scheduleTimePoint, String taskID) {
		super();
		this._scheduleTimePoint = scheduleTimePoint;
		this._taskID = taskID;
	}

	public String get_taskID() {
		return _taskID;
	}

	public void set_taskID(String _taskID) {
		this._taskID = _taskID;
	}
}
