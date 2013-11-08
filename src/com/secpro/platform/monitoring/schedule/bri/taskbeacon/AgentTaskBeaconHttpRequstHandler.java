package com.secpro.platform.monitoring.schedule.bri.taskbeacon;

import javax.xml.bind.annotation.XmlElement;

import org.jboss.netty.handler.codec.http.HttpRequest;

import com.secpro.platform.api.server.IHttpRequestHandler;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.bri.AgentTaskBeaconInterface;

/**
 * @author baiyanwei Sep 24, 2013
 * 
 *         for SYSLOG standard rule receiver.
 * 
 */
public class AgentTaskBeaconHttpRequstHandler implements IHttpRequestHandler {
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(AgentTaskBeaconHttpRequstHandler.class);
	private String id = null;
	private String name = null;
	private String description = null;
	@XmlElement(name = "path", type = String.class)
	public String path = "";
	private AgentTaskBeaconInterface _agentTaskBeaconInterface = null;

	public AgentTaskBeaconHttpRequstHandler() {
		super();
		_agentTaskBeaconInterface = ServiceHelper.findService(AgentTaskBeaconInterface.class);
	}

	@Override
	public Object DELETE(HttpRequest request, Object messageObj) throws Exception {
		return "NotSupport HTTP Method DELETE";
	}

	@Override
	public Object HEAD(HttpRequest request, Object messageObj) throws Exception {
		return "NotSupport HTTP Method HEAD";
	}

	@Override
	public Object OPTIONS(HttpRequest request, Object messageObj) throws Exception {
		return "NotSupport HTTP Method OPTIONS";
	}

	@Override
	public Object PUT(HttpRequest request, Object messageObj) throws Exception {
		return "NotSupport HTTP Method PUT";
	}

	@Override
	public Object TRACE(HttpRequest request, Object messageObj) throws Exception {
		return "NotSupport HTTP Method TRACE";
	}

	@Override
	public Object GET(HttpRequest request, Object messageObj) throws Exception {
		return POST(request, messageObj);
	}

	@Override
	public Object POST(HttpRequest request, Object messageObj) throws Exception {
		//find the parameter in HTTP request header.
		if (_agentTaskBeaconInterface == null) {
			_agentTaskBeaconInterface = ServiceHelper.findService(AgentTaskBeaconInterface.class);
		}
		try {
			String region = request.getHeader(AgentTaskBeaconInterface.REGION);
			String operations = request.getHeader(AgentTaskBeaconInterface.OPERATIONS);
			int counter = Integer.parseInt(request.getHeader(AgentTaskBeaconInterface.COUNT));
			String publicKey = request.getHeader(AgentTaskBeaconInterface.PUBLIC_KEY);
			return _agentTaskBeaconInterface.fetchTask(region, operations, counter, publicKey);
		} catch (Exception e) {
			theLogger.exception(e);
			throw e;
		}
	}

	@Override
	public void setID(String id) {
		this.id = id;
	}

	@Override
	public String getID() {
		return this.id;
	}

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return this.name;
	}

	@Override
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String getDescription() {
		return this.description;
	}

	@Override
	public String getRequestMappingPath() {
		return this.path;
	}

	public String toString() {
		return theLogger.MessageFormat("toString", name, path);
	}
}
