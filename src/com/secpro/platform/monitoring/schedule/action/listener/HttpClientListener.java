package com.secpro.platform.monitoring.schedule.action.listener;

import com.secpro.platform.api.client.IClientResponseListener;
import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.log.utils.PlatformLogger;

/**
 * @author baiyanwei Jul 13, 2013
 * 
 * 
 */
public class HttpClientListener implements IClientResponseListener {
	//
	// Logging Object
	//
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(HttpClientListener.class);

	// 0 false 1 true
	// private byte _isHasResponse = 0;

	private String _listenerID = "HttpClientListener";

	private String _listenerName = "HttpClientListener";

	private String _listenerDescription = "HttpClientListener";

	public HttpClientListener() {
	}

	@Override
	public void setID(String id) {
		this._listenerID = id;
	}

	@Override
	public String getID() {
		return this._listenerID + this.hashCode();
	}

	@Override
	public void setName(String name) {
		this._listenerName = name;
	}

	@Override
	public String getName() {
		return this._listenerName;
	}

	@Override
	public void setDescription(String description) {
		this._listenerDescription = description;
	}

	@Override
	public String getDescription() {
		return this._listenerDescription;
	}

	@Override
	public void fireSucceed(Object messageObj) throws PlatformException {

		if (messageObj != null) {
			theLogger.info(this._listenerName + ":" + messageObj.toString());
		}

	}

	@Override
	public void fireError(Object messageObj) throws PlatformException {
		if (messageObj != null) {
			theLogger.warn(this._listenerName + ":" + messageObj.toString());
			theLogger.exception(new Exception(this._listenerName + ":" + messageObj.toString()));
		}
	}
}