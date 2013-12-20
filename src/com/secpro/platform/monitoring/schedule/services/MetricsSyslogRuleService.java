package com.secpro.platform.monitoring.schedule.services;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.management.DynamicMBean;
import javax.xml.bind.annotation.XmlElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.metrics.AbstractMetricMBean;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.action.SysLogStandardRulePublishAction;
import com.secpro.platform.monitoring.schedule.bri.ManageTaskBeaconInterface;
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
	// Map<region,Map<mcaName,callback>>
	private HashMap<String, HashMap<String, URI>> _mcaPublishReferentMap = new HashMap<String, HashMap<String, URI>>();
	// MAP<type_code,ruleBean>
	private HashMap<String, List<MSUSysLogStandardRule>> _syslogStandardRuleMap = new HashMap<String, List<MSUSysLogStandardRule>>();
	// MAP<region,Map<ip,type_code>>
	private HashMap<String, HashMap<String, String>> _regionFWTypeCodeMap = new HashMap<String, HashMap<String, String>>();

	@Override
	public void start() throws PlatformException {
		// register itself as dynamic bean
		this.registerMBean(_jmxObjectName, this);
		//
		initSyslogStandardRuleData();
		//
		loadMcaInforForDataBase();
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
		//
		_regionFWTypeCodeMap = getregionFWTypeCodeMappingDataFromFromDataBase();
		// #1 read DB record.
		List<MSUSysLogStandardRule> unfetchScheduleList = getSyslogRuleRecordsFromDataBase();
		if (Assert.isEmptyCollection(unfetchScheduleList) == true) {
			return;
		}
		// TODO??
		// #2 group into cache.
		synchronized (this._syslogStandardRuleMap) {
			for (int i = 0; i < unfetchScheduleList.size(); i++) {
				if (this._syslogStandardRuleMap.containsKey(unfetchScheduleList.get(i).getTypeCode()) == false) {
					List<MSUSysLogStandardRule> typeList = new ArrayList<MSUSysLogStandardRule>();
					typeList.add(unfetchScheduleList.get(i));
					this._syslogStandardRuleMap.put(unfetchScheduleList.get(i).getTypeCode(), typeList);
				} else {
					List<MSUSysLogStandardRule> typeList = this._syslogStandardRuleMap.get(unfetchScheduleList.get(i).getTypeCode());
					typeList.add(unfetchScheduleList.get(i));
				}
			}
		}

	}

	/**
	 * get the region and ip ,type code mapping.
	 * 
	 * @return
	 */
	private HashMap<String, HashMap<String, String>> getregionFWTypeCodeMappingDataFromFromDataBase() {
		DataBaseStorageAdapter dataBaseStorageAdapter = ServiceHelper.findService(DataBaseStorageAdapter.class);
		if (dataBaseStorageAdapter == null) {
			return new HashMap<String, HashMap<String, String>>();
		}
		return dataBaseStorageAdapter.queryFireWallTypeCodeMapping();
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
	public String fetchSyslogStandardRuleByRequest(String region, String mcaName, URI pushPath) {
		if (Assert.isEmptyString(region) == true || Assert.isEmptyString(mcaName) == true || pushPath == null) {
			return null;
		}
		boolean isNew = false;
		synchronized (this._mcaPublishReferentMap) {
			if (_mcaPublishReferentMap.containsKey(region) == false) {
				HashMap<String, URI> mcaMap = new HashMap<String, URI>();
				_mcaPublishReferentMap.put(region, mcaMap);
				mcaMap.put(mcaName, pushPath);
				isNew = true;
			} else {
				HashMap<String, URI> mcaMap = _mcaPublishReferentMap.get(region);
				if (mcaMap.containsKey(mcaName) == false) {
					isNew = true;
				}
				mcaMap.put(mcaName, pushPath);
			}
		}
		if (isNew) {
			this.recordMcaInfor(region, mcaName, pushPath);
		}

		return getSysLogStandardRules(region);
	}

	/**
	 * publish a new standard rule to all MCAs
	 * 
	 * @param id
	 * @param content
	 */
	public void publishSysLogStandardRule(String newTypeCode) {
		if (Assert.isEmptyString(newTypeCode) == true) {
			return;
		}
		List<MSUSysLogStandardRule> ruleList = loadMSUSysLogStandardRuleFromDB(newTypeCode);
		if (Assert.isEmptyCollection(ruleList) == true) {
			return;
		}
		synchronized (this._syslogStandardRuleMap) {
			this._syslogStandardRuleMap.put(newTypeCode, ruleList);
		}
		Object[] referentObj = getIpAndMCAbyFwTypeCode(newTypeCode);
		@SuppressWarnings("unchecked")
		HashMap<String, URI> pushBackMap = (HashMap<String, URI>) referentObj[0];

		if (pushBackMap.isEmpty() == true) {
			return;
		}
		@SuppressWarnings("unchecked")
		ArrayList<String> ipList = (ArrayList<String>) referentObj[1];
		//
		JSONArray publishContentArray = new JSONArray();
		//
		fillAndBuildRulesMessage(ipList, newTypeCode, ruleList, publishContentArray);
		//
		if (publishContentArray.length() == 0) {
			return;
		}
		// publish the change into mca.
		publishSysLogStandardRuleToMCA(ManageTaskBeaconInterface.MSU_COMMAND_SYSLOG_RULE_UPDATE, publishContentArray.toString(), pushBackMap);
	}

	/**
	 * get change FireWall resource IP and relation MCA
	 * 
	 * @param newTypeCode
	 * @return
	 */
	private Object[] getIpAndMCAbyFwTypeCode(String newTypeCode) {
		if (Assert.isEmptyString(newTypeCode) == true) {
			return new Object[] { new HashMap<String, URI>(), new ArrayList<String>() };
		}
		HashMap<String, URI> pushBackMap = new HashMap<String, URI>();
		ArrayList<String> ipList = new ArrayList<String>();
		for (Iterator<String> regionIter = this._regionFWTypeCodeMap.keySet().iterator(); regionIter.hasNext();) {
			String region = regionIter.next();
			HashMap<String, String> fwMap = this._regionFWTypeCodeMap.get(region);
			if (fwMap == null || fwMap.isEmpty() == true) {
				continue;
			}
			boolean isAdd = false;
			for (Iterator<String> fwIter = fwMap.keySet().iterator(); fwIter.hasNext();) {
				String ip = fwIter.next();
				String typeCode = fwMap.get(ip);
				if (typeCode.equalsIgnoreCase(newTypeCode)) {
					ipList.add(ip);
					//
					if (isAdd == false) {
						HashMap<String, URI> callBackMap = this._mcaPublishReferentMap.get(region);
						if (callBackMap != null) {
							pushBackMap.putAll(callBackMap);
						}
						isAdd = true;
					}
				}
			}
		}
		return new Object[] { pushBackMap, ipList };
	}

	private List<MSUSysLogStandardRule> loadMSUSysLogStandardRuleFromDB(String typeCode) {
		if (Assert.isEmptyString(typeCode) == true) {
			return null;
		}
		DataBaseStorageAdapter dataBaseStorageAdapter = ServiceHelper.findService(DataBaseStorageAdapter.class);
		if (dataBaseStorageAdapter == null) {
			return new ArrayList<MSUSysLogStandardRule>();
		}
		return dataBaseStorageAdapter.querySysLogStandardRuleByTypeCode(typeCode);
	}

	/**
	 * publish a new standard rule to all MCAs
	 * 
	 * @param id
	 * @param content
	 */
	public void removeSysLogStandardRule(String typeCode) {
		if (Assert.isEmptyString(typeCode) == true) {
			return;
		}
		synchronized (this._syslogStandardRuleMap) {
			this._syslogStandardRuleMap.remove(typeCode);
		}
		Object[] referentObj = getIpAndMCAbyFwTypeCode(typeCode);
		@SuppressWarnings("unchecked")
		HashMap<String, URI> pushBackMap = (HashMap<String, URI>) referentObj[0];

		if (pushBackMap.isEmpty() == true) {
			return;
		}
		@SuppressWarnings("unchecked")
		ArrayList<String> ipList = (ArrayList<String>) referentObj[1];
		String removeRuleIps = "";
		for (int i = 0; i < ipList.size(); i++) {
			removeRuleIps = removeRuleIps + ipList.get(i) + ",";
		}
		if (removeRuleIps.endsWith(",") == true) {
			removeRuleIps = removeRuleIps.substring(0, removeRuleIps.length() - 1);
		}
		// publish the change into mca.
		publishSysLogStandardRuleToMCA(ManageTaskBeaconInterface.MSU_COMMAND_SYSLOG_RULE_REMOVE, removeRuleIps, pushBackMap);

	}

	/**
	 * add new FireWall resource and publish to MCAs
	 * 
	 * @param messageConent
	 */
	public void addFireWallResource(String messageConent) {
		if (Assert.isEmptyString(messageConent) == true) {
			return;
		}
		// ip#task_region#type_code
		String[] messageArray = messageConent.split("#");
		if (messageArray == null || messageArray.length == 0 || messageArray.length < 3) {
			return;
		}
		if (Assert.isEmptyString(messageArray[0]) == true) {
			return;
		}
		if (Assert.isEmptyString(messageArray[1]) == true) {
			return;
		}
		if (Assert.isEmptyString(messageArray[2]) == true) {
			return;
		}
		synchronized (this._regionFWTypeCodeMap) {
			if (this._regionFWTypeCodeMap.containsKey(messageArray[1]) == true) {
				HashMap<String, String> ipMap = _regionFWTypeCodeMap.get(messageArray[1]);
				if (ipMap.containsKey(messageArray[0]) == true) {
					return;
				}
				ipMap.put(messageArray[0], messageArray[2]);
			} else {
				HashMap<String, String> ipMap = new HashMap<String, String>();
				ipMap.put(messageArray[0], messageArray[2]);
				_regionFWTypeCodeMap.put(messageArray[1], ipMap);
			}
		}
		HashMap<String, URI> pushBackMap = _mcaPublishReferentMap.get(messageArray[1]);

		if (pushBackMap == null || pushBackMap.isEmpty() == true) {
			return;
		}
		// publish the change into mca.
		publishSysLogStandardRuleToMCA(ManageTaskBeaconInterface.MSU_COMMAND_SYSLOG_RULE_REMOVE, messageArray[0], pushBackMap);

	}

	/**
	 * remove the FireWall Resource and publish to MCAs
	 * 
	 * @param messageContent
	 */
	public void removeFireWallResource(String messageContent) {
		if (Assert.isEmptyString(messageContent) == true) {
			return;
		}
		String[] ipArray = messageContent.split(",");
		if (ipArray == null || ipArray.length == 0) {
			return;
		}
		HashMap<String, URI> pushBackMap = new HashMap<String, URI>();
		ArrayList<String> ipList = new ArrayList<String>();
		synchronized (_regionFWTypeCodeMap) {
			for (String ip : ipArray) {
				for (Iterator<String> regionIter = _regionFWTypeCodeMap.keySet().iterator(); regionIter.hasNext();) {
					String region = regionIter.next();
					if (_regionFWTypeCodeMap.get(region).containsKey(ip)) {
						if(_mcaPublishReferentMap.get(region)!=null){
							pushBackMap.putAll(_mcaPublishReferentMap.get(region));
						}
						ipList.add(ip);
						_regionFWTypeCodeMap.get(region).remove(ip);
					}
				}
			}
		}
		if (pushBackMap == null || pushBackMap.isEmpty() == true) {
			return;
		}
		//
		String removeRuleIps = "";
		for (int i = 0; i < ipList.size(); i++) {
			removeRuleIps = removeRuleIps + ipList.get(i) + ",";
		}
		if (removeRuleIps.endsWith(",") == true) {
			removeRuleIps = removeRuleIps.substring(0, removeRuleIps.length() - 1);
		}
		// publish the change into mca.
		publishSysLogStandardRuleToMCA(ManageTaskBeaconInterface.MSU_COMMAND_SYSLOG_RULE_REMOVE, removeRuleIps, pushBackMap);

	}

	/**
	 * publish the rule to target.
	 * 
	 * @param content
	 * @param pushBackMap
	 * @throws Exception
	 */
	private void publishSysLogStandardRuleToMCA(String operationCode, String content, HashMap<String, URI> pushBackMap) {
		for (Iterator<String> mcaIter = pushBackMap.keySet().iterator(); mcaIter.hasNext();) {
			try {
				new SysLogStandardRulePublishAction(pushBackMap.get(mcaIter.next()), operationCode, content).start();
			} catch (Exception e) {
				theLogger.exception(e);
			}
		}
	}

	/**
	 * get the SYSLOG rule content.
	 * 
	 * @param region
	 * 
	 * @return
	 */
	private String getSysLogStandardRules(String region) {
		JSONArray allRullArray = new JSONArray();
		HashMap<String, String> fwMap = this._regionFWTypeCodeMap.get(region);
		if (fwMap == null || fwMap.isEmpty() == true) {
			return allRullArray.toString();
		}
		//
		String fwIp = null;
		String fwTypeCode = null;
		for (Iterator<String> keyIter = fwMap.keySet().iterator(); keyIter.hasNext();) {
			fwIp = keyIter.next();
			fwTypeCode = fwMap.get(fwIp);
			fillAndBuildRulesMessage(fwIp, fwTypeCode, this._syslogStandardRuleMap.get(fwTypeCode), allRullArray);
		}
		return allRullArray.toString();
	}

	private void fillAndBuildRulesMessage(ArrayList<String> ipList, String typeCode, List<MSUSysLogStandardRule> ruleList, JSONArray typeRuleArray) {
		if (Assert.isEmptyCollection(ruleList) == true || Assert.isEmptyCollection(ipList) == true) {
			return;
		}
		for (String ip : ipList) {
			fillAndBuildRulesMessage(ip, typeCode, ruleList, typeRuleArray);
		}
	}

	private void fillAndBuildRulesMessage(String ip, String typeCode, List<MSUSysLogStandardRule> ruleList, JSONArray typeRuleArray) {
		if (Assert.isEmptyCollection(ruleList) == true || typeRuleArray == null) {
			return;
		}
		try {
			// regexs:JSONObject,ip:String,
			JSONObject ruleObj = new JSONObject();
			ruleObj.put("ip", ip);
			ruleObj.put("typeCode", typeCode);
			ruleObj.put("checkAction", ruleList.get(0).getCheckAction());
			ruleObj.put("checkNum", ruleList.get(0).getCheckNum());
			JSONObject regexsObj = new JSONObject();
			for (MSUSysLogStandardRule logRule : ruleList) {
				regexsObj.put(logRule.getRuleKey(), logRule.getRuleValue());
			}
			ruleObj.put("regexs", regexsObj);
			typeRuleArray.put(ruleObj);
		} catch (JSONException e) {
			e.printStackTrace();
		}

	}

	private void recordMcaInfor(String region, String mcaName, URI callBack) {
		DataBaseStorageAdapter dataBaseStorageAdapter = ServiceHelper.findService(DataBaseStorageAdapter.class);
		if (dataBaseStorageAdapter == null) {
			return;
		}
		dataBaseStorageAdapter.insertMCAInfo(region, mcaName, callBack.toString());
	}

	private void loadMcaInforForDataBase() {
		DataBaseStorageAdapter dataBaseStorageAdapter = ServiceHelper.findService(DataBaseStorageAdapter.class);
		if (dataBaseStorageAdapter == null) {
			return;
		}
		HashMap<String, HashMap<String, URI>> mcaMapping = dataBaseStorageAdapter.queryMCAInfoMapping();
		if (mcaMapping != null) {
			synchronized (_mcaPublishReferentMap) {
				this._mcaPublishReferentMap.putAll(mcaMapping);
			}
		}

	}
}