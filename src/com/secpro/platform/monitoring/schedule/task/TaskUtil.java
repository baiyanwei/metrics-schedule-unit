package com.secpro.platform.monitoring.schedule.task;

import java.util.UUID;

import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.services.taskunit.MsuTask;

/**
 * @author baiyanwei Oct 23, 2013
 * 
 * 
 *         what element in task.
 * 
 */
public class TaskUtil {

	final static public String getMSUScheduleID(long schedulePoint) {
		String sn = UUID.randomUUID().toString();
		String sortSN = sn.substring(0, 8) + sn.substring(9, 13) + sn.substring(14, 18) + sn.substring(19, 23) + sn.substring(24);
		// schedulePoint(13)-UUID(32);
		return schedulePoint + "-" + sortSN.toUpperCase();
	}

	final static public MSUSchedule createScheduleOnTime(MsuTask taskObj, long schedulePoint, long createAt) {
		// String taskID, String scheduleID, long schedulePoint, String region,
		// String operation, long createAt)
		return new MSUSchedule(taskObj.getId(), getMSUScheduleID(schedulePoint), schedulePoint, taskObj.getRegion(), taskObj.getOperation(), createAt);
	}

	final static public MSUSchedule createScheduleAsRealTime(MsuTask taskObj) {
		// String taskID, String scheduleID, long schedulePoint, String region,
		// String operation, long createAt)
		long schedulePoint = System.currentTimeMillis();
		return new MSUSchedule(taskObj.getId(), getMSUScheduleID(schedulePoint), schedulePoint, taskObj.getRegion(), taskObj.getOperation(), schedulePoint);
	}
}
