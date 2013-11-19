package com.secpro.platform.monitoring.schedule.task;

import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.services.taskunit.MSUTask;

/**
 * @author baiyanwei Oct 23, 2013
 * 
 * 
 *         what element in task.
 * 
 */
public class TaskUtil {

	final static public String getMSUScheduleID(MSUTask task, long schedulePoint, long createAt) {
		return task.getID() + "-" + schedulePoint + "-" + createAt;
	}

	final static public MSUSchedule createScheduleOnTime(MSUTask taskObj, long schedulePoint, long createAt) {
		// String taskID, String scheduleID, long schedulePoint, String region,
		// String operation, long createAt)
		return new MSUSchedule(taskObj.getID(), getMSUScheduleID(taskObj, schedulePoint, createAt), schedulePoint, taskObj.getRegion(), taskObj.getOperation(), createAt);
	}

	final static public MSUSchedule createScheduleAsRealTime(MSUTask taskObj) {
		// String taskID, String scheduleID, long schedulePoint, String region,
		// String operation, long createAt)
		long schedulePoint = System.currentTimeMillis();
		return new MSUSchedule(taskObj.getID(), getMSUScheduleID(taskObj, schedulePoint, schedulePoint), schedulePoint, taskObj.getRegion(), taskObj.getOperation(), schedulePoint);
	}
}
