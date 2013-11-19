package com.secpro.platform.monitoring.schedule.services;

import java.util.HashMap;
import java.util.Iterator;

import javax.jms.Connection;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.jms.Topic;
import javax.management.DynamicMBean;
import javax.xml.bind.annotation.XmlElement;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.json.JSONObject;

import com.secpro.platform.core.metrics.AbstractMetricMBean;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.services.syslogruleunit.MSUSysLogStandardRule;
import com.secpro.platform.monitoring.schedule.services.taskunit.MSUTask;
import com.secpro.platform.monitoring.schedule.task.TaskUtil;

@ServiceInfo(description = "ManagementMessageService, build the MQ connnection and do the message by type.", configurationPath = "app/msu/services/ManagementMessageService/")
public class ManagementMessageService extends AbstractMetricMBean implements IService, DynamicMBean {
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(ManagementMessageService.class);
	final public static String[] MANAGEMENT_OPERATION_TYPE = new String[] { "TOPIC-TASK-ADD", "TOPIC-TASK-UPDATE", "TOPIC-TASK-REMOVE", "TOPIC-SYSLOG-STANDARD-RULE-ADD",
			"TOPIC-SYSLOG-STANDARD-RULE-UPDATE", "TOPIC-SYSLOG-STANDARD-RULE-REMOVE" };
	//
	@XmlElement(name = "jmxObjectName", defaultValue = "secpro.msu:type=NodeService")
	public String _jmxObjectName = "secpro.msu:type=NodeService";
	@XmlElement(name = "mqURL", defaultValue = "tcp://localhost:61616")
	public String _mqURL = "";
	@XmlElement(name = "mqUserName", defaultValue = "system")
	public String _mqUserName = "";
	@XmlElement(name = "mqPasswd", defaultValue = "manager")
	public String _mqPasswd = "";

	private HashMap<String, MessageConsumer> _msgConsumerMap = new HashMap<String, MessageConsumer>();
	private Connection _msgConnection = null;
	private Session _msgSession = null;
	//
	private MetricsScheduleUnitService _metricsScheduleUnitService = null;
	private MetricsSyslogRuleService _metricsSyslogRuleService = null;

	@Override
	public void start() throws Exception {
		this.registerMBean(_jmxObjectName, this);
		//
		registerMQListener();
		//
		theLogger.info("startUp", this._msgConsumerMap.keySet().toString());
		//

	}

	@Override
	public void stop() throws Exception {
		//
		this.unRegisterMBean(_jmxObjectName);
		//
		unregisterMQListener();
		_msgConsumerMap.clear();
		//
		_metricsScheduleUnitService = null;
		_metricsSyslogRuleService = null;
		//
		theLogger.info("stopped");
	}

	private void registerMQListener() throws JMSException {
		// Getting JMS connection from the server
		ActiveMQConnectionFactory connectionFactory = new ActiveMQConnectionFactory(_mqURL);
		// set userName and passWd ,change configuration in
		// MQ/conf/credentials.properties or activemq.xml中
		connectionFactory.setUserName(this._mqUserName);
		connectionFactory.setPassword(this._mqPasswd);
		// create connection and start it.
		_msgConnection = connectionFactory.createConnection();
		_msgConnection.start();
		// create session.
		_msgSession = _msgConnection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		// create consumers for every topic.
		for (int i = 0; i < MANAGEMENT_OPERATION_TYPE.length; i++) {
			try {
				Topic topic = _msgSession.createTopic(MANAGEMENT_OPERATION_TYPE[i]);
				// create a consumer for message.
				MessageConsumer consumer = _msgSession.createConsumer(topic);
				// set message listener
				consumer.setMessageListener(new MUSMessageListener(MANAGEMENT_OPERATION_TYPE[i]));
				_msgConsumerMap.put(MANAGEMENT_OPERATION_TYPE[i], consumer);
			} catch (Exception e) {
				theLogger.exception(e);
			}
		}
	}

	private void unregisterMQListener() {
		try {
			// stop then consumers .
			for (Iterator<String> keyIter = _msgConsumerMap.keySet().iterator(); keyIter.hasNext();) {
				try {
					_msgConsumerMap.get(keyIter.next()).close();
				} catch (JMSException jmse) {
					theLogger.exception(jmse);
					continue;
				}
			}
		} catch (Exception e) {
			theLogger.exception(e);
		} finally {
			// finally close connection and session for release the resource.
			if (_msgSession != null) {
				try {
					_msgSession.close();
				} catch (JMSException e) {
				}
			}
			if (_msgConnection != null) {
				try {
					_msgConnection.close();
				} catch (JMSException e) {
				}
			}
		}
	}

	private void messageAdapter(String managementOperationType, String messageContent) {
		System.out.println("Received message" + messageContent + "'");
		//
		try {
			// task add.
			if ("TOPIC-TASK-ADD".equalsIgnoreCase(managementOperationType) == true) {
				MSUTask msuTask = buildMSUTask(messageContent);
				if (msuTask == null) {
					theLogger.error("errorTaskJSONFormat", messageContent);
					return;
				}
				if (_metricsScheduleUnitService == null) {
					_metricsScheduleUnitService = ServiceHelper.findService(MetricsScheduleUnitService.class);
				}
				if (msuTask.isRealtime() == true) {
					// Task is a real-time task.
					// real-time task just add into schedule system.
					MSUSchedule msuSchedule = TaskUtil.createScheduleAsRealTime(msuTask);
					if (msuSchedule == null) {
						theLogger.error("createScheduleError", messageContent);
						return;
					}
					_metricsScheduleUnitService._scheduleCoreService.putMSUSchedule(msuSchedule, msuTask.isRealtime());
				} else {
					// schedule task.
					_metricsScheduleUnitService._taskCoreService.putMSUTask(msuTask);
				}
			} else if ("TOPIC-TASK-UPDATE".equalsIgnoreCase(managementOperationType) == true) {
				// update a exist task in system.
				MSUTask msuTask = buildMSUTask(messageContent);
				if (msuTask == null) {
					theLogger.error("errorTaskJSONFormat", messageContent);
					return;
				}
				if (_metricsScheduleUnitService == null) {
					_metricsScheduleUnitService = ServiceHelper.findService(MetricsScheduleUnitService.class);
				}
				_metricsScheduleUnitService._taskCoreService.updateMSUTask(msuTask);
			} else if ("TOPIC-TASK-REMOVE".equalsIgnoreCase(managementOperationType) == true) {
				// remove the task from system.
				MSUTask msuTask = buildMSUTask(messageContent);
				if (msuTask == null) {
					theLogger.error("errorTaskJSONFormat", messageContent);
					return;
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
					return;
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
					return;
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
					return;
				}
				if (_metricsSyslogRuleService == null) {
					_metricsSyslogRuleService = ServiceHelper.findService(MetricsSyslogRuleService.class);
				}
				_metricsSyslogRuleService.removeSysLogStandardRule(ruleObj);
			} else {
				theLogger.error("invilad management operation message type.");
			}
		} catch (Exception e) {
			theLogger.exception(e);
		}
	}

	/**
	 * build the MSUTask from database records.
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	final private MSUTask buildMSUTask(String content) throws Exception {
		if (Assert.isEmptyString(content) == true) {
			return null;
		}
		JSONObject contentObj = new JSONObject(content);
		MSUTask task = new MSUTask();

		task.setID(contentObj.getString(MSUTask.ID_TITLE));
		task.setRegion(contentObj.getString(MSUTask.REGION_TITLE));
		task.setCreateAt(contentObj.getLong(MSUTask.CREATE_AT_TITLE));
		task.setSchedule(contentObj.getString(MSUTask.SCHEDULE_TITLE));
		task.setOperation(contentObj.getString(MSUTask.OPERATION_TITLE));
		task.setMetaData(contentObj.getString(MSUTask.META_DATA_TITLE));
		task.setContent(contentObj.getString(MSUTask.CONTENT_TITLE));
		task.setResID(contentObj.getLong(MSUTask.RES_ID_TITLE));
		task.setRealtime(contentObj.getBoolean(MSUTask.IS_REALTIME_TITLE));

		return task;
	}

	final private MSUSysLogStandardRule buildMSUSysLogStandardRule(String content) throws Exception {
		if (Assert.isEmptyString(content) == true) {
			return null;
		}
		JSONObject contentObj = new JSONObject(content);
		MSUSysLogStandardRule msuSyslogRule = new MSUSysLogStandardRule();
		//
		msuSyslogRule.setRuleID(contentObj.getString(MSUSysLogStandardRule.RULE_ID_TITLE));
		msuSyslogRule.setContent(contentObj.getString(MSUSysLogStandardRule.CONTENT_TITLE));
		//
		return msuSyslogRule;
	}

	//
	class MUSMessageListener implements MessageListener {
		private String _msgTypeTitle = null;

		public MUSMessageListener(String msgTypeTitle) {
			this._msgTypeTitle = msgTypeTitle;
		}

		@Override
		public void onMessage(Message message) {
			try {
				if (message instanceof TextMessage) {
					TextMessage textMessage = (TextMessage) message;
					messageAdapter(this._msgTypeTitle, textMessage.getText());
				}
			} catch (JMSException e) {
				theLogger.exception(e);
			}
		}

	}
}
