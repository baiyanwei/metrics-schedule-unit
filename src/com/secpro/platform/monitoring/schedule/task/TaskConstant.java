package com.secpro.platform.monitoring.schedule.task;

import com.secpro.platform.monitoring.schedule.services.taskunit.MSUTask;

/**
 * @author baiyanwei Oct 23, 2013
 * 
 * 
 *         what element in task.
 * 
 */
public class TaskConstant {
	final static public String TASK_ID = "id";
	final static public String TASK_OPERATION = "operation";
	
	final static public String getMSUScheduleID(MSUTask task,long schedulePoint,long createAt){
		return task.getID()+"-"+schedulePoint+"-"+createAt;
	}
}
