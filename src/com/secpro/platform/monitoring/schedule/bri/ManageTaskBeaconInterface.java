package com.secpro.platform.monitoring.schedule.bri;

import com.secpro.platform.api.common.http.server.HttpServer;

/**
 * @author baiyanwei Jul 21, 2013
 * 
 *         SysLog listenter
 * 
 */
public class ManageTaskBeaconInterface extends HttpServer {

	/**
	 * add a new task in MSU
	 * 
	 * @param taskObj
	 * @return
	 */
	public String AddTask(Object taskObj) {
		return "";
	}

	/**
	 * remove a task.
	 * 
	 * @param taskObj
	 * @return
	 */
	public String removeTask(Object taskObj) {
		return "";
	}

	/**
	 * update task content
	 * 
	 * @param taskObj
	 * @return
	 */
	public String upDateTask(Object taskObj) {
		return "";
	}
}
