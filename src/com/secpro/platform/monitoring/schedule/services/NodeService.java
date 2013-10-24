package com.secpro.platform.monitoring.schedule.services;

import javax.management.DynamicMBean;
import javax.xml.bind.annotation.XmlElement;

import com.secpro.platform.core.metrics.AbstractMetricMBean;
import com.secpro.platform.core.node.INode;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.log.utils.PlatformLogger;

@ServiceInfo(description = "Schedule Node management, work on node information, node ability", configurationPath = "app/msu/services/NodeService/")
public class NodeService extends AbstractMetricMBean implements IService, INode, DynamicMBean {
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(NodeService.class);
	//
	@XmlElement(name = "jmxObjectName", defaultValue = "secpro.msu:type=NodeService")
	public String _jmxObjectName = "secpro.msu:type=NodeService";
	@XmlElement(name = "nodeRegion", defaultValue = "MSU_CENTER")
	public String _nodeRegion = "";

	@Override
	public void start() throws Exception {
		this.registerMBean(_jmxObjectName, this);
		theLogger.info("startUp", this._nodeRegion);
	}

	@Override
	public void stop() throws Exception {
		theLogger.info("stopped", this._nodeRegion);
		this.unRegisterMBean(_jmxObjectName);
	}

	@Override
	public void registerNode() {
		// TODO Auto-generated method stub

	}

	@Override
	public void unregisterNode() {
		// TODO Auto-generated method stub

	}

	@Override
	public String getNodeRegion() {
		return this._nodeRegion;
	}

	@Override
	public String setNodeRegion(String region) {
		return this._nodeRegion = region;
	}
}
