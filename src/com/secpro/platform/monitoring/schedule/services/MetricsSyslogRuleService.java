package com.secpro.platform.monitoring.schedule.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.management.DynamicMBean;
import javax.xml.bind.annotation.XmlElement;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.metrics.AbstractMetricMBean;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.action.SysLogStandardRulePublishAction;
import com.secpro.platform.monitoring.schedule.services.syslogruleunit.MSUSysLogStandardRule;
import com.secpro.platform.monitoring.schedule.storages.DataBaseStorageAdapter;

/**
 * @author baiyanwei Jul 6, 2013
 * 
 *         The SYSLOG standard rule service will work on rule publish into each
 *         MCA, keep the rule cache in memory.
 * 
 */
@ServiceInfo(description = "Syslog standard rule service", configurationPath = "/app/msu/services/MetricsSyslogRuleService/")
public class MetricsSyslogRuleService extends AbstractMetricMBean implements IService, DynamicMBean {
	//
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(MetricsSyslogRuleService.class);
	//
	@XmlElement(name = "jmxObjectName", defaultValue = "secpro.msu:type=MetricsSyslogRuleService")
	public String _jmxObjectName = "secpro.msu:type=MetricsSyslogRuleService";
	//
	private HashMap<String, URI> _mcaPublishReferentMap = new HashMap<String, URI>();
	private HashMap<Long, MSUSysLogStandardRule> _syslogStandardRuleMap = new HashMap<Long, MSUSysLogStandardRule>();

	@Override
	public void start() throws PlatformException {
		// register itself as dynamic bean
		this.registerMBean(_jmxObjectName, this);
		//
		initSyslogStandardRuleData();
		//
		theLogger.info("startUp");
	}

	@Override
	public void stop() throws PlatformException {
		// unregister itself
		this.unRegisterMBean(_jmxObjectName);
	}

	/**
	 * ready the location and operation from the database.
	 */
	private void initSyslogStandardRuleData() {
		// #1 read DB record.
		List<MSUSysLogStandardRule> unfetchScheduleList = getSyslogRuleRecordsFromDataBase();
		if (Assert.isEmptyCollection(unfetchScheduleList) == true) {
			return;
		}
		// #2 group into cache.
		synchronized (this._syslogStandardRuleMap) {
			for (int i = 0; i < unfetchScheduleList.size(); i++) {
				this._syslogStandardRuleMap.put(unfetchScheduleList.get(i).getRuleID(), unfetchScheduleList.get(i));
			}
		}

	}

	/**
	 * ready the record from DataBase and analyze into String Array.
	 * 
	 * @return
	 */
	private List<MSUSysLogStandardRule> getSyslogRuleRecordsFromDataBase() {
		DataBaseStorageAdapter dataBaseStorageAdapter = ServiceHelper.findService(DataBaseStorageAdapter.class);
		if (dataBaseStorageAdapter == null) {
			return new ArrayList<MSUSysLogStandardRule>();
		}
		return dataBaseStorageAdapter.querySysLogStandardRule();
	}

	/**
	 * fetch the SYSLOG standard rule from MCA.
	 * 
	 * @param region
	 * @param mca
	 * @param pushPath
	 * @return
	 */
	public String fetchSyslogStandardRuleByRequest(String mcaName, URI pushPath) {
		if (Assert.isEmptyString(mcaName) == true || pushPath == null) {
			return null;
		}
		synchronized (this._mcaPublishReferentMap) {
			this._mcaPublishReferentMap.put(mcaName, pushPath);
		}
		return getSysLogStandardRules();
	}

	/**
	 * publish a new standard rule to all MCAs
	 * 
	 * @param id
	 * @param content
	 */
	public void publishSysLogStandardRule(MSUSysLogStandardRule newRule) {
		if (newRule == null) {
			return;
		}
		synchronized (this._syslogStandardRuleMap) {
			this._syslogStandardRuleMap.put(newRule.getRuleID(), newRule);
		}
		for (Iterator<String> keyIter = this._mcaPublishReferentMap.keySet().iterator(); keyIter.hasNext();) {
			try {
				publishSysLogStandardRuleToMCA(getSysLogRulesMessage(newRule), this._mcaPublishReferentMap.get(keyIter.next()));
			} catch (Exception e) {
				theLogger.exception(e);
				continue;
			}
		}

	}

	/**
	 * publish a new standard rule to all MCAs
	 * 
	 * @param id
	 * @param content
	 */
	public void removeSysLogStandardRule(MSUSysLogStandardRule ruleObj) {
		if (ruleObj == null) {
			return;
		}
		synchronized (this._syslogStandardRuleMap) {
			this._syslogStandardRuleMap.remove(ruleObj.getRuleID());
		}
	}

	/**
	 * publish the rule to target.
	 * 
	 * @param content
	 * @param publishPath
	 * @throws Exception
	 */
	private void publishSysLogStandardRuleToMCA(String content, URI publishPath) throws Exception {
		new SysLogStandardRulePublishAction(publishPath, content).start();
	}

	/**
	 * get the SYSLOG rule content.
	 * 
	 * @return
	 */
	private String getSysLogStandardRules() {
		StringBuffer rules = new StringBuffer();
		Long keyName = null;
		for (Iterator<Long> keyIter = this._syslogStandardRuleMap.keySet().iterator(); keyIter.hasNext();) {
			keyName = keyIter.next();
			rules.append(this._syslogStandardRuleMap.get(keyName));
		}
		return (rules.length() == 0 ? "" : rules.toString());
	}

	private String getSysLogRulesMessage(MSUSysLogStandardRule ruleObj) {
		if (ruleObj == null) {
			return "";
		}
		// TODO ??
		return "";
	}

}