package com.secpro.platform.monitoring.schedule.bri.managetaskbeacon;

import javax.xml.bind.annotation.XmlElement;

import org.jboss.netty.handler.codec.http.HttpRequest;

import com.secpro.platform.api.server.IHttpRequestHandler;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.bri.ManageTaskBeaconInterface;
import com.secpro.platform.monitoring.schedule.node.InterfaceParameter;

/**
 * @author baiyanwei Sep 24, 2013
 * 
 *         for SYSLOG standard rule receiver.
 * 
 */
public class ManageTaskBeaconHttpRequstHandler implements IHttpRequestHandler {
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(ManageTaskBeaconHttpRequstHandler.class);
	private String id = null;
	private String name = null;
	private String description = null;
	@XmlElement(name = "path", type = String.class)
	public String path = "";
	private ManageTaskBeaconInterface _manageTaskBeaconInterface = null;

	public ManageTaskBeaconHttpRequstHandler() {
		super();
		_manageTaskBeaconInterface = ServiceHelper.findService(ManageTaskBeaconInterface.class);
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
		return POST(request, messageObj);
	}

	@Override
	public Object TRACE(HttpRequest request, Object messageObj) throws Exception {
		return "NotSupport HTTP Method TRACE";
	}

	@Override
	public Object GET(HttpRequest request, Object messageObj) throws Exception {
		return "NotSupport HTTP Method GET";
	}

	@Override
	public Object POST(HttpRequest request, Object messageObj) throws Exception {
		// find the parameter in HTTP request header.
		if (_manageTaskBeaconInterface == null) {
			_manageTaskBeaconInterface = ServiceHelper.findService(ManageTaskBeaconInterface.class);
		}
		try {
			String operationType = request.getHeader(InterfaceParameter.ManagementParameter.OPERATION_TYPE);
			return _manageTaskBeaconInterface.messageAdapter(operationType, messageObj.toString());
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
