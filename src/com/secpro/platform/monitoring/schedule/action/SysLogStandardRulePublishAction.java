package com.secpro.platform.monitoring.schedule.action;

import java.io.IOException;
import java.net.URI;
import java.security.NoSuchAlgorithmException;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.TreeMap;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpVersion;

import com.secpro.platform.api.client.ClientConfiguration;
import com.secpro.platform.api.client.InterfaceParameter;
import com.secpro.platform.api.common.http.client.HttpClient;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.core.utils.Constants;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.action.listener.HttpClientListener;

/**
 * @author baiyanwei Nov 5, 2013
 * 
 * 
 *         build the schedule by task.
 */
public class SysLogStandardRulePublishAction extends Thread {
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(SysLogStandardRulePublishAction.class);
	private URI _targetURI = null;
	private String _publishConten = null;
	private String _operationCode = null;

	public SysLogStandardRulePublishAction(URI publishTarget, String operationCode, String content) {
		this._targetURI = publishTarget;
		this._publishConten = content;
		this._operationCode = operationCode;
	}

	@Override
	public void run() {
		publishToTarget();
	}

	/**
	 * build Schedule for current hour.
	 */
	private void publishToTarget() {
		if (Assert.isEmptyString(this._publishConten) || _targetURI == null) {
			return;
		}
		try {
			String scheme = _targetURI.getScheme() == null ? "http" : _targetURI.getScheme();
			String host = _targetURI.getHost() == null ? "localhost" : _targetURI.getHost();
			int port = _targetURI.getPort();
			if (port == -1) {
				if (scheme.equalsIgnoreCase("http")) {
					port = 80;
				} else if (scheme.equalsIgnoreCase("https")) {
					port = 443;
				}
			}

			if (!scheme.equalsIgnoreCase("http")) {
				System.err.println("Only HTTP is supported.");
				return;
			}
			String path = _targetURI.getPath();
			if (path == null || path.trim().equals("")) {
				path = "/";
			}
			DefaultHttpRequest httpRequestV2 = createHttpMessage(host, port, path, HttpMethod.PUT, this._publishConten);
			//
			HttpClient client = new HttpClient();
			ClientConfiguration config = new ClientConfiguration();
			config._endPointHost = host;
			config._endPointPort = port;
			config._synchronousConnection = false;
			config._httpRequest = httpRequestV2;
			config._responseListener = new HttpClientListener();
			//
			config._responseListener.setName("Target->" + this._targetURI.toString());
			//
			config._parameterMap = new HashMap<String, String>();
			config._parameterMap.put(InterfaceParameter.ManagementParameter.OPERATION_TYPE, _operationCode);
			config._content = this._publishConten;
			//
			client.configure(config);
			//
			client.start();
			//
		} catch (Exception e) {
			theLogger.exception("publishToTarget", e);
		}
	}

	private DefaultHttpRequest createHttpMessage(String host, int port, String accessPath, HttpMethod httpMethod, String content) throws NoSuchAlgorithmException, IOException,
			Exception {
		if (content == null) {
			content = "";
		}

		DefaultHttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, httpMethod, accessPath);
		// identify HTTP port we use
		if (80 == port) {
			request.addHeader(HttpHeaders.Names.HOST, host);
		} else {
			request.addHeader(HttpHeaders.Names.HOST, host + ":" + port);
		}
		theLogger.info(host + ":" + port + accessPath);
		TreeMap<String, String> requestHeaders = new TreeMap<String, String>(new Comparator<String>() {
			public int compare(String string0, String string1) {
				return string0.compareToIgnoreCase(string1);
			}
		});
		//
		requestHeaders.put(HttpHeaders.Names.DATE, new SimpleDateFormat("EEE, d MMM yyyy HH:mm:ss Z", Locale.CHINESE).format(new Date()));
		requestHeaders.put(HttpHeaders.Names.CONTENT_TYPE, "text/json");

		// We need to set the content encoding to be UTF-8 in order to have the
		// message properly decoded.
		requestHeaders.put(HttpHeaders.Names.CONTENT_ENCODING, Constants.DEFAULT_ENCODING);
		// Add the customer headers to the request.
		Iterator<String> iterator = requestHeaders.keySet().iterator();
		while (iterator.hasNext() == true) {
			String name = iterator.next();
			String value = requestHeaders.get(name);
			request.addHeader(name, value);
		}

		// Needs to use the size of the bytes in the string.
		byte[] bytes = content.getBytes(Constants.DEFAULT_CHARSET);

		request.addHeader(HttpHeaders.Names.CONTENT_LENGTH, String.valueOf(bytes.length));
		request.addHeader(HttpHeaders.Names.USER_AGENT, "Mectrics-Collect-Agent");

		ChannelBuffer channelBuffer = ChannelBuffers.buffer(bytes.length);
		channelBuffer.writeBytes(bytes);
		request.setContent(channelBuffer);
		return request;
	}
}
