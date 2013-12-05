package com.secpro.platform.monitoring.schedule.bri.syslogrulebeacon;

import java.net.URI;

import javax.xml.bind.annotation.XmlElement;

import org.jboss.netty.handler.codec.http.HttpRequest;

import com.secpro.platform.api.client.InterfaceParameter;
import com.secpro.platform.api.server.IHttpRequestHandler;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.bri.SyslogRuleBeaconInterface;

/**
 * @author baiyanwei Sep 24, 2013
 * 
 *         for SYSLOG standard rule receiver.
 * 
 */
public class SyslogRuleBeaconHttpRequstHandler implements IHttpRequestHandler {
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(SyslogRuleBeaconHttpRequstHandler.class);
	private String id = null;
	private String name = null;
	private String description = null;
	@XmlElement(name = "path", type = String.class)
	public String path = "";
	private SyslogRuleBeaconInterface _syslogRuleBeaconInterface = null;

	public SyslogRuleBeaconHttpRequstHandler() {
		super();
		_syslogRuleBeaconInterface = ServiceHelper.findService(SyslogRuleBeaconInterface.class);
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
		if (_syslogRuleBeaconInterface == null) {
			_syslogRuleBeaconInterface = ServiceHelper.findService(SyslogRuleBeaconInterface.class);
		}
		try {
			// TODO MCA need to change and fill this header parameter.
			String region = request.getHeader(InterfaceParameter.HttpHeaderParameter.REGION);
			if (Assert.isEmptyString(region) == true) {
				throw new Exception("invalid parameter " + InterfaceParameter.HttpHeaderParameter.REGION);
			}
			//
			String mcaName = request.getHeader(InterfaceParameter.HttpHeaderParameter.MCA_NAME);
			if (Assert.isEmptyString(mcaName) == true) {
				throw new Exception("invalid parameter " + InterfaceParameter.HttpHeaderParameter.MCA_NAME);
			}
			//
			String pushPort = request.getHeader(InterfaceParameter.HttpHeaderParameter.SYSLOG_RULE_PUSH_PORT);
			if (Assert.isEmptyString(pushPort) == true) {
				throw new Exception("invalid parameter " + InterfaceParameter.HttpHeaderParameter.SYSLOG_RULE_PUSH_PORT);
			}
			String remoteAddressIp = request.getHeader(InterfaceParameter.HttpHeaderParameter.REMOTE_HOST_ADDR_IP);
			if (Assert.isEmptyString(remoteAddressIp) == true) {
				throw new Exception("invalid parameter " + InterfaceParameter.HttpHeaderParameter.REMOTE_HOST_ADDR_IP);
			}
			//
			return _syslogRuleBeaconInterface.fetchSysLogRule(region, mcaName, new URI("http://" + remoteAddressIp + ":" + pushPort+"/"));
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
