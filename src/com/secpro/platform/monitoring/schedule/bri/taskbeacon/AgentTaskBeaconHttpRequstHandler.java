package com.secpro.platform.monitoring.schedule.bri.taskbeacon;

import javax.xml.bind.annotation.XmlElement;

import org.jboss.netty.handler.codec.http.HttpRequest;

import com.secpro.platform.api.client.InterfaceParameter;
import com.secpro.platform.api.server.IHttpRequestHandler;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.utils.Assert;
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
		// find the parameter in HTTP request header.
		if (_agentTaskBeaconInterface == null) {
			_agentTaskBeaconInterface = ServiceHelper.findService(AgentTaskBeaconInterface.class);
		}
		try {
			// TODO ??MCA need to change and fill this header parameter.
			String region = request.getHeader(InterfaceParameter.HttpHeaderParameter.REGION);
			if (Assert.isEmptyString(region) == true) {
				throw new Exception("invalid parameter " + InterfaceParameter.HttpHeaderParameter.REGION);
			}
			//
			String operations = request.getHeader(InterfaceParameter.HttpHeaderParameter.OPERATIONS);
			if (Assert.isEmptyString(operations) == true) {
				throw new Exception("invalid parameter " + InterfaceParameter.HttpHeaderParameter.OPERATIONS);
			}
			//
			int counter = Integer.parseInt(request.getHeader(InterfaceParameter.HttpHeaderParameter.COUNTER));
			if (counter <= 0) {
				throw new Exception("invalid parameter " + InterfaceParameter.HttpHeaderParameter.COUNTER);
			}
			//
			String publicKey = request.getHeader(InterfaceParameter.HttpHeaderParameter.PUBLIC_KEY);
			if (Assert.isEmptyString(publicKey) == true) {
				throw new Exception("invalid parameter " + InterfaceParameter.HttpHeaderParameter.PUBLIC_KEY);
			}
			//
			String mcaName = request.getHeader(InterfaceParameter.HttpHeaderParameter.MCA_NAME);
			if (Assert.isEmptyString(mcaName) == true) {
				throw new Exception("invalid parameter " + InterfaceParameter.HttpHeaderParameter.MCA_NAME);
			}
			//
			return _agentTaskBeaconInterface.fetchTask(region, operations, counter, mcaName, publicKey);
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
