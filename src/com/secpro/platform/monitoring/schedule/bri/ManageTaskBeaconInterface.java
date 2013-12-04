package com.secpro.platform.monitoring.schedule.bri;

import org.json.JSONObject;

import com.secpro.platform.api.common.http.server.HttpServer;
import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.services.MetricsScheduleUnitService;
import com.secpro.platform.monitoring.schedule.services.MetricsSyslogRuleService;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.services.syslogruleunit.MSUSysLogStandardRule;
import com.secpro.platform.monitoring.schedule.services.taskunit.MsuTask;
import com.secpro.platform.monitoring.schedule.task.TaskUtil;

/**
 * @author baiyanwei Jul 21, 2013
 * 
 *         SysLog listenter
 * 
 */
public class ManageTaskBeaconInterface extends HttpServer {
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(ManageTaskBeaconInterface.class);

	final public static String[] MANAGEMENT_OPERATION_TYPE = new String[] { "TOPIC-TASK-ADD", "TOPIC-TASK-UPDATE", "TOPIC-TASK-REMOVE", "TOPIC-SYSLOG-STANDARD-RULE-ADD",
			"TOPIC-SYSLOG-STANDARD-RULE-UPDATE", "TOPIC-SYSLOG-STANDARD-RULE-REMOVE" };
	private MetricsScheduleUnitService _metricsScheduleUnitService = null;
	private MetricsSyslogRuleService _metricsSyslogRuleService = null;

	@Override
	public void start() throws Exception {
		super.start();

	}

	@Override
	public void stop() throws Exception {
		super.stop();
		_metricsScheduleUnitService = null;
		_metricsSyslogRuleService = null;
	}

	public String messageAdapter(String managementOperationType, String messageContent) throws PlatformException {
		System.out.println("Received message" + messageContent + "'");
		//
		try {
			// task add.
			if ("TOPIC-TASK-ADD".equalsIgnoreCase(managementOperationType) == true) {
				MsuTask msuTask = buildMSUTask(messageContent);
				if (msuTask == null) {
					theLogger.error("errorTaskJSONFormat", messageContent);
					throw new PlatformException("errorTaskJSONFormat " + messageContent);
				}
				if (_metricsScheduleUnitService == null) {
					_metricsScheduleUnitService = ServiceHelper.findService(MetricsScheduleUnitService.class);
				}
				if (msuTask.getIsRealtime() == true) {
					// Task is a real-time task.
					// real-time task just add into schedule system.
					MSUSchedule msuSchedule = TaskUtil.createScheduleAsRealTime(msuTask);
					if (msuSchedule == null) {
						theLogger.error("createScheduleError", messageContent);
						throw new PlatformException("createScheduleError " + messageContent);
					}
					_metricsScheduleUnitService._scheduleCoreService.putMSUSchedule(msuSchedule, msuTask.getIsRealtime());
				} else {
					// schedule task.
					_metricsScheduleUnitService._taskCoreService.putMSUTask(msuTask);
				}
			} else if ("TOPIC-TASK-UPDATE".equalsIgnoreCase(managementOperationType) == true) {
				// update a exist task in system.
				MsuTask msuTask = buildMSUTask(messageContent);
				if (msuTask == null) {
					theLogger.error("errorTaskJSONFormat", messageContent);
					throw new PlatformException("errorTaskJSONFormat " + messageContent);
				}
				if (_metricsScheduleUnitService == null) {
					_metricsScheduleUnitService = ServiceHelper.findService(MetricsScheduleUnitService.class);
				}
				_metricsScheduleUnitService._taskCoreService.updateMSUTask(msuTask);
			} else if ("TOPIC-TASK-REMOVE".equalsIgnoreCase(managementOperationType) == true) {
				// remove the task from system.
				MsuTask msuTask = buildMSUTask(messageContent);
				if (msuTask == null) {
					theLogger.error("errorTaskJSONFormat", messageContent);
					throw new PlatformException("errorTaskJSONFormat " + messageContent);
				}
				if (_metricsScheduleUnitService == null) {
					_metricsScheduleUnitService = ServiceHelper.findService(MetricsScheduleUnitService.class);
				}
				_metricsScheduleUnitService._taskCoreService.removeMSUTask(msuTask);
				//
			} else if ("TOPIC-SYSLOG-STANDARD-RULE-ADD".equalsIgnoreCase(managementOperationType) == true) {
				// add a SYSLOG standard rule and publish.
				MSUSysLogStandardRule ruleObj = buildMSUSysLogStandardRule(messageContent);
				if (ruleObj == null) {
					theLogger.error("errorRuleJSONFormat", messageContent);
					throw new PlatformException("errorRuleJSONFormat " + messageContent);
				}
				if (_metricsSyslogRuleService == null) {
					_metricsSyslogRuleService = ServiceHelper.findService(MetricsSyslogRuleService.class);
				}
				_metricsSyslogRuleService.publishSysLogStandardRule(ruleObj);
			} else if ("TOPIC-SYSLOG-STANDARD-RULE-UPDATE".equalsIgnoreCase(managementOperationType) == true) {
				// update rule and republish.
				MSUSysLogStandardRule ruleObj = buildMSUSysLogStandardRule(messageContent);
				if (ruleObj == null) {
					theLogger.error("errorRuleJSONFormat", messageContent);
					throw new PlatformException("errorRuleJSONFormat " + messageContent);
				}
				if (_metricsSyslogRuleService == null) {
					_metricsSyslogRuleService = ServiceHelper.findService(MetricsSyslogRuleService.class);
				}
				_metricsSyslogRuleService.publishSysLogStandardRule(ruleObj);
				//
			} else if ("TOPIC-SYSLOG-STANDARD-RULE-REMOVE".equalsIgnoreCase(managementOperationType) == true) {
				// remove the rule without publishing.
				MSUSysLogStandardRule ruleObj = buildMSUSysLogStandardRule(messageContent);
				if (ruleObj == null) {
					theLogger.error("errorRuleJSONFormat", messageContent);
					throw new PlatformException("errorRuleJSONFormat " + messageContent);
				}
				if (_metricsSyslogRuleService == null) {
					_metricsSyslogRuleService = ServiceHelper.findService(MetricsSyslogRuleService.class);
				}
				_metricsSyslogRuleService.removeSysLogStandardRule(ruleObj);
			} else {
				theLogger.error("invilad management operation message type.");
			}
			return "OK";
		} catch (Exception e) {
			theLogger.exception(e);
			throw new PlatformException("Exception :", e);
		}
	}

	/**
	 * build the MSUTask from database records.
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	final private MsuTask buildMSUTask(String content) throws Exception {
		if (Assert.isEmptyString(content) == true) {
			return null;
		}
		JSONObject contentObj = new JSONObject(content);
		MsuTask task = new MsuTask();

		task.setId(contentObj.getString(MsuTask.ID_TITLE));
		task.setRegion(contentObj.getString(MsuTask.REGION_TITLE));
		task.setCreateAt(contentObj.getLong(MsuTask.CREATE_AT_TITLE));
		task.setSchedule(contentObj.getString(MsuTask.SCHEDULE_TITLE));
		task.setOperation(contentObj.getString(MsuTask.OPERATION_TITLE));
		task.setTargetIp(contentObj.getString(MsuTask.TARGET_IP_TITLE));
		task.setTargetPort(contentObj.getInt(MsuTask.TARGET_PORT_TITLE));
		task.setMetaData(contentObj.getString(MsuTask.META_DATA_TITLE));
		task.setContent(contentObj.getString(MsuTask.CONTENT_TITLE));
		task.setResId(contentObj.getLong(MsuTask.RES_ID_TITLE));
		task.setIsRealtime(contentObj.getBoolean(MsuTask.IS_REALTIME_TITLE));

		return task;
	}

	final private MSUSysLogStandardRule buildMSUSysLogStandardRule(String content) throws Exception {
		if (Assert.isEmptyString(content) == true) {
			return null;
		}
		JSONObject contentObj = new JSONObject(content);
		MSUSysLogStandardRule msuSyslogRule = new MSUSysLogStandardRule();
		//
		msuSyslogRule.setRuleID(contentObj.getLong(MSUSysLogStandardRule.RULE_ID_TITLE));
		msuSyslogRule.setRuleKey(contentObj.getString(MSUSysLogStandardRule.RULE_KEY_TITLE));
		msuSyslogRule.setRuleValue(contentObj.getString(MSUSysLogStandardRule.RULE_VALUE_TITLE));
		msuSyslogRule.setCheckNum(contentObj.getLong(MSUSysLogStandardRule.RULE_CHECK_NUM_TITLE));
		msuSyslogRule.setCheckAction(contentObj.getString(MSUSysLogStandardRule.RULE_CHECK_ACTION_TITLE));
		msuSyslogRule.setTypeCode(contentObj.getString(MSUSysLogStandardRule.RULE_TYPE_CODE_TITLE));
		//
		return msuSyslogRule;
	}
}
