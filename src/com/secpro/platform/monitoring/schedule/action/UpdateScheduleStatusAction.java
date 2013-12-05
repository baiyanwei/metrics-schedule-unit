package com.secpro.platform.monitoring.schedule.action;

import java.util.ArrayList;
import java.util.List;

import com.secpro.platform.core.services.ServiceHelper;
import com.secpro.platform.core.utils.Assert;
import com.secpro.platform.log.utils.PlatformLogger;
import com.secpro.platform.monitoring.schedule.storages.DataBaseStorageAdapter;

/**
 * @author baiyanwei Nov 5, 2013
 * 
 *         update the schedule instance status when it is fetched by MCA.
 * 
 */
public class UpdateScheduleStatusAction extends Thread {
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(UpdateScheduleStatusAction.class);
	// wait to update
	private List<String[]> _waitToUpdateScheduleStatusList = null;

	public UpdateScheduleStatusAction(List<String[]> waitToUpdateScheduleStatusList) {
		this._waitToUpdateScheduleStatusList = waitToUpdateScheduleStatusList;
	}

	@Override
	public void run() {
		updateTheScheduleStatus();
	}

	/**
	 * build Schedule for current hour.
	 */
	private void updateTheScheduleStatus() {
		if (Assert.isEmptyCollection(_waitToUpdateScheduleStatusList) == true) {
			return;
		}
		DataBaseStorageAdapter dataBaseStorageAdapter = ServiceHelper.findService(DataBaseStorageAdapter.class);
		if (dataBaseStorageAdapter == null) {
			theLogger.error("DataBaseStorageAdapter service is not ready.");
			return;
		}
		//
		try {
			ArrayList<String> batchList = new ArrayList<String>();
			StringBuffer updateSQL = new StringBuffer();
			String[] scheduleStatus = null;
			for (int i = 0; i < _waitToUpdateScheduleStatusList.size(); i++) {
				scheduleStatus = _waitToUpdateScheduleStatusList.get(i);
				if (scheduleStatus == null) {
					continue;
				}
				// MSU_SCHEDULE
				// Name Type Nullable Default Comments
				// ------------------- ------------ -------- ------- --------
				// TASK_ID VARCHAR2(50) 任务标识ID
				// SCHEDULE_ID VARCHAR2(50) 任务调度ID
				// SCHEDULE_POINT NUMBER(20) 调度时间
				// CREATE_AT NUMBER(20) 入库时间
				// REGION VARCHAR2(50) 省市县编码
				// OPERATION VARCHAR2(50) 任务操作
				// FETCH_AT NUMBER(20) Y 获取时间
				// FETCH_BY VARCHAR2(50) Y 获取者
				// EXECUTE_AT NUMBER(20) Y 执行时间
				// EXECUTE_COST NUMBER(20) Y 执行时长
				// EXECUTE_STATUS NUMBER(1) Y 执行情况
				// EXECUTE_DESCRIPTION VARCHAR2(50) Y 执行描述
				updateSQL.append("UPDATE MSU_SCHEDULE SET ");
				updateSQL.append("FETCH_AT=").append(scheduleStatus[1]).append(",");
				updateSQL.append("FETCH_BY='").append(scheduleStatus[2]).append("'");
				updateSQL.append(" WHERE SCHEDULE_ID='").append(scheduleStatus[0]).append("'");
				batchList.add(updateSQL.toString());
				updateSQL.setLength(0);
			}
			dataBaseStorageAdapter.updateRecordsBatch(batchList);
		} catch (Exception e) {
			theLogger.exception(e);
		}
	}
}
