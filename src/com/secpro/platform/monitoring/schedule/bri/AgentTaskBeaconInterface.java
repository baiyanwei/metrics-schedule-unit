package com.secpro.platform.monitoring.schedule.bri;

import java.util.HashMap;

import com.secpro.platform.api.common.http.server.HttpServer;

/**
 * @author baiyanwei Jul 21, 2013
 * 
 *         SysLog listenter
 * 
 */
public class AgentTaskBeaconInterface extends HttpServer {
	/**
	 * fetch a batch task by request.
	 * 
	 * @param requestParameterMap
	 * @return
	 */
	public String fetchTask(HashMap<String, String> requestParameterMap) {
		return "";
	}
}
