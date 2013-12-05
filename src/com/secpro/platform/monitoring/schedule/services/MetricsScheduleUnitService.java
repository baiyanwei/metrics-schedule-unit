package com.secpro.platform.monitoring.schedule.services;

import it.sauronsoftware.base64.Base64;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.OperatingSystemMXBean;
import java.security.Key;
import java.security.KeyFactory;
import java.security.spec.X509EncodedKeySpec;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;

import javax.crypto.Cipher;
import javax.management.DynamicMBean;
import javax.xml.bind.annotation.XmlElement;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.metrics.AbstractMetricMBean;
import com.secpro.platform.core.metrics.Metric;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.core.utils.Utils;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.Activator;
import com.secpro.platform.monitoring.schedule.action.ScheduleAction;
import com.secpro.platform.monitoring.schedule.node.InterfaceParameter;
import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.services.taskunit.MsuTask;
import com.secpro.platform.monitoring.schedule.storages.DataBaseStorageAdapter;

/**
 * @author baiyanwei Jul 6, 2013
 * 
 *         In this unit, We define the task , make the schedule. Task is content
 *         about What task is , Where Task will be executed,What time task will
 *         be fetch. Here have two type of task. one is frequency task ,other is
 *         realTime task. RealTime will be fetched and executed at first. One
 *         task map many schedule on different time point. Task is created by
 *         system management.And the schedule is created by schedule property in
 *         task content. Create schedule on every hour.it is hourly. The all
 *         task and schedule content will be stored in task with starting name
 *         "MSU_"
 */
@ServiceInfo(description = "The main service of Metrics-Schedule-Service", configurationPath = "/app/msu/services/MetricsScheduleUnitService/")
public class MetricsScheduleUnitService extends AbstractMetricMBean implements IService, DynamicMBean {

	//
	// Logging Object
	//
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(MetricsScheduleUnitService.class);

	private static HashMap<String, MessageFormat> messageFormatters = new HashMap<String, MessageFormat>();
	private static String[] messageFiles = new String[] { "msu-task.js" };

	// 最大加密明文大小
	private static final int MAX_ENCRYPT_BLOCK = 117;
	//
	@XmlElement(name = "jmxObjectName", defaultValue = "secpro.msu:type=MetricsScheduleUnitService")
	public String _jmxObjectName = "secpro.msu:type=MetricsScheduleUnitService";

	// cache the version number
	@Metric(description = "The version number of MSU")
	public String _version = Activator._version.toString();
	// schedule interval
	@XmlElement(name = "scheduleTimerExecuteInterval", type = Long.class, defaultValue = "3600000")
	public long _scheduleTimerExecuteInterval = 3600000;
	//
	private HashMap<String, String[]> _regionNameMap = new HashMap<String, String[]>();
	// task service
	public TaskCoreService _taskCoreService = null;
	// schedule service.
	public ScheduleCoreService _scheduleCoreService = null;
	//
	private Timer _scheduleTimer = null;

	static {
		createMessageFormatters();
	};

	private static void createMessageFormatters() {
		for (int index = 0; index < messageFiles.length; index++) {
			String fileName = messageFiles[index];
			StringBuffer stringBuffer = Utils.getInputStream2StringBuffer(MetricsScheduleUnitService.class.getResourceAsStream("messages/" + fileName));

			MessageFormat messageFormat = new MessageFormat(stringBuffer.toString());

			// Remove the file extension.
			int intPos = fileName.indexOf(".");
			if (intPos != -1) {
				fileName = fileName.substring(0, intPos);
			}
			messageFormatters.put(fileName, messageFormat);
		}
	}

	@Override
	public void start() throws PlatformException {

		// register itself as dynamic bean
		this.registerMBean(_jmxObjectName, this);
		// create task core server for task management.
		this._taskCoreService = new TaskCoreService();
		// create schedule core server for task management.
		this._scheduleCoreService = new ScheduleCoreService();
		// register two service into OSGI frame.
		ServiceHelper.registerService(_taskCoreService);
		ServiceHelper.registerService(_scheduleCoreService);
		// Get the region information from database.
		initRegionReferent();
		//
		startScheduleTimer();

		//
		theLogger.info("starUp", _regionNameMap.keySet().toString());

		System.out.println(_scheduleCoreService.getEveryRegionScheduleSize());
		System.out.println(_taskCoreService.getEveryRegionTaskSize());

	}

	@Override
	public void stop() throws PlatformException {
		// unregister itself
		this.unRegisterMBean(_jmxObjectName);
	}

	private void startScheduleTimer() {

		long currentPoint = System.currentTimeMillis();
		//
		long delayPoint = 3600000 - currentPoint % 3600000;
		_scheduleTimer = new Timer("MetricsScheduleUnitService._scheduleTimer");
		// start on next hour 00:00
		_scheduleTimer.schedule(new ScheduleAction(this), delayPoint, _scheduleTimerExecuteInterval);
		// test
		if (delayPoint > 60000) {
			new ScheduleAction(this).run();
		}
	}

	/**
	 * read the region information
	 */
	private void initRegionReferent() {
		DataBaseStorageAdapter dataBaseStorageAdapter = ServiceHelper.findService(DataBaseStorageAdapter.class);
		if (dataBaseStorageAdapter == null) {
			return;
		}
		//
		List<Object[]> resultData = dataBaseStorageAdapter.selectRecords(theLogger.getMessageFormat("SQL_SELECT_ALL_REGION"));
		if (resultData == null || resultData.isEmpty() == true) {
			return;
		}
		// {{CITY_NAME,CITY_CODE,CITY_LEVEL,PARENT_CODE},}
		for (int i = 0; i < resultData.size(); i++) {
			this._regionNameMap.put((String) resultData.get(i)[0], new String[] { (String) resultData.get(i)[0], (String) resultData.get(i)[1], (String) resultData.get(i)[2],
					(String) resultData.get(i)[3] });
		}
	}

	/**
	 * get The schedule by condition.
	 * 
	 * @param region
	 * @param operations
	 * @param num
	 * @return
	 */
	public String fetchScheduleByRequest(String region, String operations, int counter, String fetcher, String publicKey) throws Exception {
		if (Assert.isEmptyString(region) == true || Assert.isEmptyString(operations) == true || Assert.isEmptyString(fetcher) == true || Assert.isEmptyString(publicKey) == true
				|| counter <= 0) {
			new Exception("invalid parameter");
		}
		List<MSUSchedule> scheduleList = _scheduleCoreService.nextNofetchingMSUSchedules(region, operations, counter);
		//
		JSONArray packageTaskArray = new JSONArray();
		ArrayList<String[]> updateScheduleList = new ArrayList<String[]>();
		long fetchAt = System.currentTimeMillis();
		MsuTask msuTask = null;
		JSONObject taskContent = null;
		for (int i = 0; i < scheduleList.size(); i++) {
			msuTask = this._taskCoreService.getMSUTask(scheduleList.get(i).getRegion(), scheduleList.get(i).getTaskID());
			taskContent = packageMSUTaskToJSON(msuTask, scheduleList.get(i), publicKey);
			if (taskContent == null) {
				continue;
			}
			// scheduleID,fetching time, fetcher.
			updateScheduleList.add(new String[] { scheduleList.get(i).getScheduleID(), String.valueOf(fetchAt), fetcher });
			packageTaskArray.put(taskContent);
		}
		this._scheduleCoreService.updateScheduleStatus(updateScheduleList);
		//System.out.println(">>>" + packageTaskArray.toString());
		return packageTaskArray.toString();
	}

	private String encryptBASE64(byte[] bytes) {
		return new String(Base64.encode(bytes));
	}

	private JSONObject packageMSUTaskToJSON(MsuTask msuTask, MSUSchedule msuSchedule, String publicKey) {
		if (msuTask == null || msuSchedule == null) {
			return null;
		}
		// '{'"tid":"{0}","sid": "{1}","reg": "{2}","ope":
		// "{3}","cat":"{4}","sat":"{5}","tip":"{6}","tpt":"{7}","con":'{8}',"mda":'{9}''}'
		JSONObject taskObj = new JSONObject();
		try {
			taskObj.put(InterfaceParameter.MonitoringTask.TASK_ID_PROPERTY_NAME, msuTask.getId());
			taskObj.put(InterfaceParameter.MonitoringTask.TASK_SCHEDULE_ID_PROPERTY_NAME, msuSchedule.getScheduleID());
			taskObj.put(InterfaceParameter.MonitoringTask.TASK_REGION_PROPERTY_NAME, msuTask.getRegion());
			taskObj.put(InterfaceParameter.MonitoringTask.TASK_OPERATION_PROPERTY_NAME, msuTask.getOperation());
			taskObj.put(InterfaceParameter.MonitoringTask.TASK_CREATED_AD_PROPERTY_NAME, String.valueOf(msuTask.getCreateAt()));
			taskObj.put(InterfaceParameter.MonitoringTask.TASK_SCHEDULE_POINT_PROPERTY_NAME, String.valueOf(msuSchedule.getSchedulePoint()));
			taskObj.put(InterfaceParameter.MonitoringTask.TASK_TARGET_IP_PROPERTY_NAME, msuTask.getTargetIp());
			taskObj.put(InterfaceParameter.MonitoringTask.TASK_TARGET_PORT_PROPERTY_NAME, String.valueOf(msuTask.getTargetPort()));
			taskObj.put(InterfaceParameter.MonitoringTask.TASK_CONTENT_PROPERTY_NAME, msuTask.getContent());
			//
			JSONObject metaObj = new JSONObject(msuTask.getMetaData());
			String[] metaPPNames = JSONObject.getNames(metaObj);
			if (metaPPNames != null && metaPPNames.length > 0) {
				for (int i = 0; i < metaPPNames.length; i++) {
					try {
						String ppValue = metaObj.getString(metaPPNames[i]);
						if (Assert.isEmptyString(ppValue) == true) {
							continue;
						}
						String encryptStr = encryptBASE64(encryptByPublicKey(ppValue.getBytes(), publicKey));
						//
						if (Assert.isEmptyString(encryptStr) == true) {
							continue;
						}
						metaObj.put(metaPPNames[i], encryptStr);
					} catch (Exception e) {
						theLogger.exception(e);
					}
				}
			}
			taskObj.put(InterfaceParameter.MonitoringTask.TASK_META_DATA_NAME, metaObj);
		} catch (JSONException e) {
			theLogger.exception(e);
			return null;
		}
		return taskObj;
	}

	/**
	 * 用公钥加密data
	 * 
	 * @param data
	 * @param key
	 * @return
	 */
	public byte[] encryptByPublicKey(byte[] data, String publicKey) {
		if (Assert.isNull(data) == true || Assert.isEmptyString(publicKey) == true) {
			return null;
		}
		// 对公钥解密
		byte[] keyBytes = Base64.decode(publicKey.getBytes());
		// 取得公钥
		X509EncodedKeySpec x509KeySpec = new X509EncodedKeySpec(keyBytes);
		KeyFactory keyFactory = null;
		byte[] encryptedData = null;
		ByteArrayOutputStream out = null;
		try {
			keyFactory = KeyFactory.getInstance("RSA");
			Key publicK = keyFactory.generatePublic(x509KeySpec);
			// 对数据加密
			Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
			cipher.init(Cipher.ENCRYPT_MODE, publicK);
			int inputL = data.length;
			out = new ByteArrayOutputStream();
			int offset = 0;
			byte[] cache = null;
			int i = 0;
			// 对数据进行分段加密
			while (inputL - offset > 0) {
				if (inputL - offset > MAX_ENCRYPT_BLOCK) {
					cache = cipher.doFinal(data, offset, MAX_ENCRYPT_BLOCK);
				} else {
					cache = cipher.doFinal(data, offset, inputL - offset);
				}
				out.write(cache, 0, cache.length);
				i++;
				offset = i * MAX_ENCRYPT_BLOCK;
			}
			encryptedData = out.toByteArray();
		} catch (Exception e) {
			theLogger.exception(e);
		} finally {
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
				}
			}
		}
		return encryptedData;
	}

	//
	// STATISTICAL METHODS
	//
	@Metric(description = "get the error rate of the MonitoringService")
	public double fetchDelayRate() {
		return 0.0D;
	}

	@Metric(description = "get the processing average of MonitoringService")
	public double getTaskWaitToFetchAverage() {
		return 0.0D;
	}

	@Metric(description = "get the system total memory status")
	public long getSystemTotalMemory() {
		return Runtime.getRuntime().totalMemory();
	}

	@Metric(description = "get the system free memory status")
	public long getSystemFreeMemory() {
		return Runtime.getRuntime().freeMemory();
	}

	@Metric(description = "get the system load average status")
	public double getSystemLoadAverage() {
		OperatingSystemMXBean os = ManagementFactory.getOperatingSystemMXBean();
		double cpuTime = 0;
		if (os instanceof OperatingSystemMXBean) {
			cpuTime = ((OperatingSystemMXBean) os).getSystemLoadAverage();
		}
		return cpuTime;
	}

	@Metric(description = "get size of the task queue times ")
	public int getTaskQueueTimesSize() {
		return 0;
	}
}