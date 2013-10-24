package com.secpro.platform.monitoring.schedule.storages;

import javax.management.DynamicMBean;
import javax.xml.bind.annotation.XmlElement;

import com.secpro.platform.core.exception.PlatformException;
import com.secpro.platform.core.metrics.AbstractMetricMBean;
import com.secpro.platform.core.services.IService;
import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.services.ServiceInfo;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.storage.services.DataBaseStorageService;

/**
 * @author baiyanwei Jul 6, 2013
 * 
 */
@ServiceInfo(description = "MSU storage service", configurationPath = "msu/services/DataBaseStorageAdapter/")
public class DataBaseStorageAdapter extends AbstractMetricMBean implements IService, DynamicMBean {
	//
	// Logging Object
	//
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(DataBaseStorageAdapter.class);
	//
	@XmlElement(name = "jmxObjectName", defaultValue = "secpro.msu:type=DataBaseStorageAdapter")
	public String _jmxObjectName = "secpro.msu:type=DataBaseStorageAdapter";

	private DataBaseStorageService _dataBaseStorageService = null;

	@Override
	public void start() throws PlatformException {
		// register itself as dynamic bean
		this.registerMBean(_jmxObjectName, this);
		this._dataBaseStorageService = ServiceHelper.findService(DataBaseStorageService.class);
	}

	@Override
	public void stop() throws PlatformException {
		// unregister itself
		this.unRegisterMBean(_jmxObjectName);
	}

	/**
	 * Store the schedule into DB
	 * 
	 * @param taskScheduleObj
	 */
	public void storeTaskSchedule(Object taskScheduleObj) {

	}

	/**
	 * Store the task content into DB.
	 * 
	 * @param taskObj
	 */
	public void storeTask(Object taskObj) {

	}

	/**
	 * Update the schedule into DB
	 * 
	 * @param taskObj
	 */
	public void updateTaskSchedule(Object taskObj) {

	}

	/**
	 * Update the task content into DB.
	 * 
	 * @param taskObj
	 */
	public void updateTask(Object taskObj) {

	}

	/**
	 * @param regions
	 * @return
	 */
	public String[][] lookupTasks(String[] regions) {
		return null;
	}

	public String[][] lookupTaskScheduleRecords(String region, long startTimePoint, long endTimePoint) {
		return null;
	}
}