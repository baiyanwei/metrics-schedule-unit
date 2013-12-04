package com.secpro.platform.monitoring.schedule.storages;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
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
import com.secpro.platform.monitoring.schedule.services.scheduleunit.MSUSchedule;
import com.secpro.platform.monitoring.schedule.services.syslogruleunit.MSUSysLogStandardRule;
import com.secpro.platform.monitoring.schedule.services.taskunit.MsuTask;
import com.secpro.platform.storage.services.DataBaseStorageService;

/**
 * @author baiyanwei Jul 6, 2013
 * 
 */
@ServiceInfo(description = "MSU storage service", configurationPath = "app/msu/services/DataBaseStorageAdapter/")
public class DataBaseStorageAdapter extends AbstractMetricMBean implements IService, DynamicMBean {
	//
	// Logging Object
	//
	final private static PlatformLogger theLogger = PlatformLogger.getLogger(DataBaseStorageAdapter.class);
	// field
	final private static String msuScheduleField = "TASK_ID,SCHEDULE_ID,SCHEDULE_POINT,CREATE_AT,REGION,OPERATION,FETCH_AT,FETCH_BY,EXECUTE_AT,EXECUTE_COST,EXECUTE_STATUS,EXECUTE_DESCRIPTION";
	// field
	final private static String msuTaskField = "ID,REGION,CREATE_AT,SCHEDULE,OPERATION,TARGET_IP,TARGET_PORT,CONTENT,META_DATA,RES_ID,IS_REALTIME";
	// field for SYSLOG standard rule.
	final private static String msuSysLogStandardRuleField = "ID,RULE_KEY,RULE_VALUE,CHECK_NUM,CHECK_ACTION,TYPE_CODE";
	//
	@XmlElement(name = "jmxObjectName", defaultValue = "secpro.msu:type=DataBaseStorageAdapter")
	public String _jmxObjectName = "secpro.msu:type=DataBaseStorageAdapter";

	private DataBaseStorageService _dataBaseStorageService = null;

	@Override
	public void start() throws PlatformException {
		// register itself as dynamic bean
		this.registerMBean(_jmxObjectName, this);
		this._dataBaseStorageService = ServiceHelper.findService(DataBaseStorageService.class);
		theLogger.info("startUp");
	}

	@Override
	public void stop() throws PlatformException {
		// unregister itself
		this.unRegisterMBean(_jmxObjectName);
		this._dataBaseStorageService = null;
		theLogger.info("stopped");
	}

	/**
	 * Store the schedule into DB
	 * 
	 * @param taskScheduleObj
	 */
	public int insertSchedule(MSUSchedule taskSchedule) {
		if (taskSchedule == null) {
			return 0;
		}
		// CREATE TABLE MSU_SCHEDULE
		// (
		// TASK_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_POINT NUMBER(20) NOT NULL,
		// CREATE_AT NUMBER(20) NOT NULL,
		// REGION VARCHAR2(50) NOT NULL,
		// OPERATION VARCHAR2(50) NOT NULL,
		// FETCH_AT NUMBER(20),
		// EXECUTE_AT NUMBER(20),
		// EXECUTE_COST NUMBER(20),
		// EXECUTE_STATUS NUMBER(1),
		// EXECUTE_DESCRIPTION VARCHAR2(50)
		// )
		StringBuffer insertSQL = new StringBuffer();
		// TASK_ID,SCHEDULE_ID,SCHEDULE_POINT,CREATE_AT,FETCH_AT,FETCH_BY,EXECUTE_AT,EXECUTE_COST,EXECUTE_STATUS,EXECUTE_DESCRIPTION
		insertSQL
				.append("INSERT INTO MSU_SCHEDULE (TASK_ID,SCHEDULE_ID,SCHEDULE_POINT,CREATE_AT,REGION,OPERATION,FETCH_AT,FETCH_BY,EXECUTE_AT,EXECUTE_COST,EXECUTE_STATUS,EXECUTE_DESCRIPTION) VALUES(");
		insertSQL.append("'").append(taskSchedule.getTaskID()).append("',");
		insertSQL.append("'").append(taskSchedule.getScheduleID()).append("',");
		insertSQL.append(taskSchedule.getSchedulePoint()).append(",");
		insertSQL.append(taskSchedule.getCreateAt()).append(",");
		insertSQL.append("'").append(taskSchedule.getRegion()).append("',");
		insertSQL.append("'").append(taskSchedule.getOperation()).append("',");
		insertSQL.append(taskSchedule.getFetchAt()).append(",");
		insertSQL.append("'").append(taskSchedule.getFetchBy()).append("',");
		insertSQL.append(taskSchedule.getExecuteAt()).append(",");
		insertSQL.append(taskSchedule.getExecuteCost()).append(",");
		insertSQL.append(taskSchedule.getExecuteStatus()).append(",");
		insertSQL.append("'").append(taskSchedule.getExecuteDescription()).append("'");
		insertSQL.append(")");
		return updateRecords(insertSQL.toString());
	}

	public void insertSchedules(List<MSUSchedule> taskScheduleList) {
		if (Assert.isEmptyCollection(taskScheduleList) == true) {
			return;
		}
		// CREATE TABLE MSU_SCHEDULE
		// (
		// TASK_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_POINT NUMBER(20) NOT NULL,
		// CREATE_AT NUMBER(20) NOT NULL,
		// REGION VARCHAR2(50) NOT NULL,
		// OPERATION VARCHAR2(50) NOT NULL,
		// FETCH_AT NUMBER(20),
		// EXECUTE_AT NUMBER(20),
		// EXECUTE_COST NUMBER(20),
		// EXECUTE_STATUS NUMBER(1),
		// EXECUTE_DESCRIPTION VARCHAR2(50)
		// )
		ArrayList<String> batchList = new ArrayList<String>();
		StringBuffer insertSQL = new StringBuffer();
		MSUSchedule taskSchedule = null;
		for (int i = 0; i < taskScheduleList.size(); i++) {
			taskSchedule = taskScheduleList.get(i);
			if (taskSchedule == null) {
				continue;
			}
			// TASK_ID,SCHEDULE_ID,SCHEDULE_POINT,CREATE_AT,REGION,OPERATION,FETCH_AT,FETCH_BY,EXECUTE_AT,EXECUTE_COST,EXECUTE_STATUS,EXECUTE_DESCRIPTION
			insertSQL
					.append("INSERT INTO MSU_SCHEDULE (TASK_ID,SCHEDULE_ID,SCHEDULE_POINT,CREATE_AT,REGION,OPERATION,FETCH_AT,FETCH_BY,EXECUTE_AT,EXECUTE_COST,EXECUTE_STATUS,EXECUTE_DESCRIPTION) VALUES(");
			insertSQL.append("'").append(taskSchedule.getTaskID()).append("',");
			insertSQL.append("'").append(taskSchedule.getScheduleID()).append("',");
			insertSQL.append(taskSchedule.getSchedulePoint()).append(",");
			insertSQL.append(taskSchedule.getCreateAt()).append(",");
			insertSQL.append("'").append(taskSchedule.getRegion()).append("',");
			insertSQL.append("'").append(taskSchedule.getOperation()).append("',");
			insertSQL.append(taskSchedule.getFetchAt()).append(",");
			insertSQL.append("'").append(taskSchedule.getFetchBy()).append("',");
			insertSQL.append(taskSchedule.getExecuteAt()).append(",");
			insertSQL.append(taskSchedule.getExecuteCost()).append(",");
			insertSQL.append(taskSchedule.getExecuteStatus()).append(",");
			insertSQL.append("'").append(taskSchedule.getExecuteDescription()).append("'");
			insertSQL.append(")");
			batchList.add(insertSQL.toString());
			insertSQL.setLength(0);
		}
		updateRecordsBatch(batchList);
	}

	/**
	 * Update the schedule into DB
	 * 
	 * @param taskSchedule
	 * @return
	 */
	public int updateSchedule(MSUSchedule taskSchedule) {
		if (taskSchedule == null) {
			return 0;
		}
		// CREATE TABLE MSU_SCHEDULE
		// (
		// TASK_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_POINT NUMBER(20) NOT NULL,
		// CREATE_AT NUMBER(20) NOT NULL,
		// REGION VARCHAR2(50) NOT NULL,
		// OPERATION VARCHAR2(50) NOT NULL,
		// FETCH_AT NUMBER(20),
		// EXECUTE_AT NUMBER(20),
		// EXECUTE_COST NUMBER(20),
		// EXECUTE_STATUS NUMBER(1),
		// EXECUTE_DESCRIPTION VARCHAR2(50)
		// )
		StringBuffer updateSQL = new StringBuffer();
		updateSQL.append("UPDATE MSU_SCHEDULE SET ");
		updateSQL.append("SCHEDULE_POINT=").append(taskSchedule.getSchedulePoint()).append(",");
		updateSQL.append("FETCH_AT=").append(taskSchedule.getFetchAt()).append(",");
		updateSQL.append("FETCH_BY=").append(taskSchedule.getFetchBy()).append(",");
		updateSQL.append("EXECUTE_AT=").append(taskSchedule.getExecuteAt()).append(",");
		updateSQL.append("EXECUTE_COST=").append(taskSchedule.getExecuteCost()).append(",");
		updateSQL.append("EXECUTE_STATUS=").append(taskSchedule.getExecuteStatus()).append(",");
		updateSQL.append("EXECUTE_DESCRIPTION='").append(taskSchedule.getExecuteDescription()).append("'");
		updateSQL.append(" WHERE TASK_ID='").append(taskSchedule.getTaskID()).append("' AND SCHEDULE_ID='").append(taskSchedule.getScheduleID()).equals("'");
		return updateRecords(updateSQL.toString());
	}

	/**
	 * Update the schedule into DB with attribute map.
	 * 
	 * @param taskID
	 * @param scheduleID
	 * @param attribMap
	 * @return
	 */
	public int updateSchedule(String taskID, String scheduleID, HashMap<String, Object> attribMap) {
		if (Assert.isEmptyString(taskID) == true || Assert.isEmptyString(scheduleID) || Assert.isEmptyMap(attribMap) == true) {
			return 0;
		}
		// CREATE TABLE MSU_SCHEDULE
		// (
		// TASK_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_POINT NUMBER(20) NOT NULL,
		// CREATE_AT NUMBER(20) NOT NULL,
		// REGION VARCHAR2(50) NOT NULL,
		// OPERATION VARCHAR2(50) NOT NULL,
		// FETCH_AT NUMBER(20),
		// EXECUTE_AT NUMBER(20),
		// EXECUTE_COST NUMBER(20),
		// EXECUTE_STATUS NUMBER(1),
		// EXECUTE_DESCRIPTION VARCHAR2(50)
		// )
		StringBuffer updateSQL = new StringBuffer();
		updateSQL.append("UPDATE MSU_SCHEDULE SET ");
		String keyName = null;
		for (Iterator<String> keyIter = attribMap.keySet().iterator(); keyIter.hasNext();) {
			keyName = keyIter.next();
			updateSQL.append(keyName).append("=").append(valToSQL(attribMap.get(keyName)));
			if (keyIter.hasNext()) {
				updateSQL.append(",");
			}
		}
		updateSQL.append(" WHERE TASK_ID='").append(taskID).append("' AND SCHEDULE_ID='").append(scheduleID).append("'");
		return updateRecords(updateSQL.toString());
	}

	/**
	 * remove the schedule
	 * 
	 * @param taskSchedule
	 * @return
	 */
	public int removeSchedule(MSUSchedule taskSchedule) {
		if (taskSchedule == null) {
			return 0;
		}
		StringBuffer removeSQL = new StringBuffer();
		// index on TASK_ID AND SCHEDULE_ID
		removeSQL.append("DELETE FROM MSU_SCHEDULE WHERE SCHEDULE_ID='").append(taskSchedule.getScheduleID()).append("'");
		return updateRecords(removeSQL.toString());
	}

	/**
	 * Update the task content into DB.
	 * 
	 * @param taskObj
	 */
	public int updateTask(MsuTask task) {
		if (task == null) {
			return 0;
		}
		StringBuffer updateSQL = new StringBuffer();
		// ID,REGION,CREATE_AT,SCHEDULE,OPERATION,META_DATA,CONTENT,RES_ID,IS_REALTIME
		updateSQL.append("UPDATE MSU_TASK SET ");
		updateSQL.append("REGION='").append(task.getRegion()).append("',");
		updateSQL.append("SCHEDULE='").append(task.getSchedule()).append("',");
		updateSQL.append("OPERATION='").append(task.getOperation()).append("',");
		updateSQL.append("META_DATA='").append(task.getMetaData()).append("',");
		updateSQL.append("CONTENT='").append(task.getContent()).append("',");
		updateSQL.append("RES_ID=").append(task.getResId());
		updateSQL.append(" WHERE ID='").append(task.getId()).append("'");
		return updateRecords(updateSQL.toString());
	}

	public int updateTask(String taskID, HashMap<String, Object> attribMap) {
		if (Assert.isEmptyString(taskID) == true || Assert.isEmptyMap(attribMap) == true) {
			return 0;
		}
		StringBuffer updateSQL = new StringBuffer();
		updateSQL.append("UPDATE MSU_TASK SET ");
		String keyName = null;
		for (Iterator<String> keyIter = attribMap.keySet().iterator(); keyIter.hasNext();) {
			keyName = keyIter.next();
			updateSQL.append(keyName).append("=").append(valToSQL(attribMap.get(keyName)));
			if (keyIter.hasNext()) {
				updateSQL.append(",");
			}
		}
		updateSQL.append(" WHERE TASK_ID='").append(taskID).append("'");
		return updateRecords(updateSQL.toString());
	}

	/**
	 * @param regions
	 * @return
	 */
	// public List<MSUTask> queryTasks(String[] regions) {
	// StringBuffer regionSQL = new StringBuffer();
	// regionSQL.append("select ID,REGION,CREATE_AT,SCHEDULE,OPERATION,CONTENT,META_DATA,RES_ID,IS_REALTIME from MSU_TASK T WHERE T.IS_REALTIME<>0 ");
	// if (regions != null && regions.length > 0) {
	// if (regions.length > 1000) {
	// theLogger.warn("selectInExpOver1000");
	// }
	// regionSQL.append("AND T.REGION IN (").append(regions[0]);
	// // SQL IN () The number of elements is not over 1000.
	// for (int i = 1; i < regions.length && i < 1000; i++) {
	// regionSQL.append(",").append(regions[i]);
	// }
	// regionSQL.append(") ");
	// }
	// regionSQL.append("ORDER BY REGION,ID,OPERATION");
	// return selectRecords(regionSQL.toString());
	// }

	/**
	 * @param regions
	 * @return
	 */
	public List<MsuTask> queryAllFrequencyTask() {
		StringBuffer regionSQL = new StringBuffer();
		regionSQL.append("select ");
		regionSQL.append(msuTaskField);
		regionSQL.append(" from MSU_TASK T WHERE T.IS_REALTIME<>0 ");
		regionSQL.append("ORDER BY REGION,ID,OPERATION");
		//
		List<Object[]> rowData = selectRecords(regionSQL.toString());
		//
		List<MsuTask> taskList = new ArrayList<MsuTask>();
		if (rowData == null || rowData.isEmpty() == true) {
			return taskList;
		}
		//
		for (int i = 0; i < rowData.size(); i++) {
			try {
				MsuTask task = buildMSUTask(rowData.get(i));
				if (task == null) {
					continue;
				}
				taskList.add(task);
			} catch (Exception e) {
				theLogger.exception(e);
				continue;
			}
		}
		//
		return taskList;
	}

	/**
	 * read the SYSLOG standard rule and package it into bean .
	 * 
	 * @param startTimePoint
	 * @param endTimePoint
	 * @return
	 */
	public List<MSUSysLogStandardRule> querySysLogStandardRule() {

		// CREATE TABLE MSU_SCHEDULE
		// (
		// TASK_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_POINT NUMBER(20) NOT NULL,
		// CREATE_AT NUMBER(20) NOT NULL,
		// REGION VARCHAR2(50) NOT NULL,
		// OPERATION VARCHAR2(50) NOT NULL,
		// FETCH_AT NUMBER(20),
		// EXECUTE_AT NUMBER(20),
		// EXECUTE_COST NUMBER(20),
		// EXECUTE_STATUS NUMBER(1),
		// EXECUTE_DESCRIPTION VARCHAR2(50)
		// )
		StringBuffer regionSQL = new StringBuffer();
		regionSQL.append("select ");
		regionSQL.append(msuSysLogStandardRuleField);
		regionSQL.append(" from SYSLOG_RULE");
		regionSQL.append(" ORDER BY ID");
		//
		List<Object[]> rowData = selectRecords(regionSQL.toString());
		List<MSUSysLogStandardRule> syslogStandardRuleList = new ArrayList<MSUSysLogStandardRule>();
		if (rowData == null || rowData.isEmpty() == true) {
			return syslogStandardRuleList;
		}
		//
		for (int i = 0; i < rowData.size(); i++) {
			try {
				MSUSysLogStandardRule schedule = buildMSUSysLogStandardRule(rowData.get(i));
				if (schedule == null) {
					continue;
				}
				syslogStandardRuleList.add(schedule);
			} catch (Exception e) {
				theLogger.exception(e);
				continue;
			}
		}
		//
		return syslogStandardRuleList;
	}

	public List<MSUSchedule> querySchedulesNofetching(long startTimePoint, long endTimePoint) {

		// CREATE TABLE MSU_SCHEDULE
		// (
		// TASK_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_POINT NUMBER(20) NOT NULL,
		// CREATE_AT NUMBER(20) NOT NULL,
		// REGION VARCHAR2(50) NOT NULL,
		// OPERATION VARCHAR2(50) NOT NULL,
		// FETCH_AT NUMBER(20),
		// EXECUTE_AT NUMBER(20),
		// EXECUTE_COST NUMBER(20),
		// EXECUTE_STATUS NUMBER(1),
		// EXECUTE_DESCRIPTION VARCHAR2(50)
		// )
		StringBuffer regionSQL = new StringBuffer();
		regionSQL.append("select ");
		regionSQL.append(msuScheduleField);
		regionSQL.append(" from MSU_SCHEDULE T WHERE T.SCHEDULE_POINT>");
		regionSQL.append(startTimePoint).append(" AND SCHEDULE_POINT<");
		regionSQL.append(endTimePoint);
		regionSQL.append(" ORDER BY TASK_ID,SCHEDULE_ID,SCHEDULE_POINT");
		//
		List<Object[]> rowData = selectRecords(regionSQL.toString());
		List<MSUSchedule> scheduleList = new ArrayList<MSUSchedule>();
		if (rowData == null || rowData.isEmpty() == true) {
			return scheduleList;
		}
		//
		for (int i = 0; i < rowData.size(); i++) {
			try {
				MSUSchedule schedule = buildMSUSchedule(rowData.get(i));
				if (schedule == null) {
					continue;
				}
				scheduleList.add(schedule);
			} catch (Exception e) {
				theLogger.exception(e);
				continue;
			}
		}
		//
		return scheduleList;
	}

	public List<Object[]> selectRecords(String sql) {
		if (Assert.isEmptyString(sql) == true) {
			return null;
		}
		theLogger.debug("sql", sql);
		//
		Connection conn = null;
		Statement statment = null;
		ResultSet resultSet = null;
		List<Object[]> resulteData = new ArrayList<Object[]>();
		try {
			conn = _dataBaseStorageService.getConnection();
			statment = conn.createStatement();
			resultSet = statment.executeQuery(sql);
			while (resultSet.next()) {
				Object[] rowData = new Object[resultSet.getMetaData().getColumnCount()];
				for (int c = 0; c < resultSet.getMetaData().getColumnCount(); c++) {
					rowData[c] = resultSet.getObject(c + 1);
				}
				resulteData.add(rowData);
			}
		} catch (Exception e) {
			theLogger.exception("sql:" + sql, e);
		} finally {
			if (resultSet != null) {
				try {
					resultSet.close();
				} catch (SQLException e) {
				}
			}
			if (statment != null) {
				try {
					statment.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return resulteData;
	}

	private int updateRecords(String updateSQL) {
		if (Assert.isEmptyString(updateSQL) == true) {
			return 0;
		}
		theLogger.debug("sql", updateSQL);
		//
		Connection conn = null;
		Statement statment = null;
		int rowNumber = 0;
		try {
			conn = _dataBaseStorageService.getConnection();
			statment = conn.createStatement();
			rowNumber = statment.executeUpdate(updateSQL);
		} catch (SQLException e) {
			theLogger.exception(e);
		} finally {
			if (statment != null) {
				try {
					statment.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
		return rowNumber;
	}

	/**
	 * execute insert update delete in batch.
	 * 
	 * @param updateSQLList
	 */
	public void updateRecordsBatch(ArrayList<String> updateSQLList) {
		if (Assert.isEmptyCollection(updateSQLList) == true) {
			return;
		}
		//
		Connection conn = null;
		Statement statment = null;
		boolean isAutoCommit = true;
		try {
			conn = _dataBaseStorageService.getConnection();
			isAutoCommit = conn.getAutoCommit();
			conn.setAutoCommit(false);
			statment = conn.createStatement();
			for (int i = 0; i < updateSQLList.size(); i++) {
				if (Assert.isEmptyString(updateSQLList.get(i)) == true) {
					continue;
				}
				statment.addBatch(updateSQLList.get(i));
			}
			statment.executeBatch();
			conn.commit();
		} catch (SQLException e) {
			theLogger.exception(e);
		} finally {
			if (statment != null) {
				try {
					statment.close();
				} catch (SQLException e) {
				}
			}
			if (conn != null) {
				try {
					conn.setAutoCommit(isAutoCommit);
					conn.close();
				} catch (SQLException e) {
				}
			}
		}
	}

	/**
	 * translate object to SQL value like 'Object' or Object
	 * 
	 * @param val
	 * @return
	 */
	private String valToSQL(Object val) {
		if (val == null) {
			return "";
		}
		if (val instanceof String) {
			return "'" + val + "'";
		} else {
			return val.toString();
		}
	}

	/**
	 * build the MSUSchedule from database records.
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	final private MSUSchedule buildMSUSchedule(Object[] data) throws Exception {
		if (data == null || data.length != 12) {
			return null;
		}
		MSUSchedule schedule = new MSUSchedule();
		// CREATE TABLE MSU_SCHEDULE
		// (
		// TASK_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_ID VARCHAR2(50) NOT NULL,
		// SCHEDULE_POINT NUMBER(20) NOT NULL,
		// CREATE_AT NUMBER(20) NOT NULL,
		// REGION VARCHAR2(50) NOT NULL,
		// OPERATION VARCHAR2(50) NOT NULL,
		// FETCH_AT NUMBER(20),
		// EXECUTE_AT NUMBER(20),
		// EXECUTE_COST NUMBER(20),
		// EXECUTE_STATUS NUMBER(1),
		// EXECUTE_DESCRIPTION VARCHAR2(50)
		// )
		try {
			schedule.setTaskID((String) data[0]);
			schedule.setScheduleID((String) data[1]);
			schedule.setSchedulePoint(((Number) data[2]).longValue());
			schedule.setCreateAt(((Number) data[3]).longValue());
			schedule.setRegion((String) data[4]);
			schedule.setOperation((String) data[5]);
			schedule.setFetchAt(((Number) data[6]).longValue());
			schedule.setFetchAt(((Number) data[7]).longValue());
			schedule.setExecuteAt(((Number) data[8]).longValue());
			schedule.setExecuteCost(((Number) data[9]).longValue());
			schedule.setExecuteStatus(((Number) data[10]).longValue());
			schedule.setExecuteDescription((String) data[11]);
		} catch (Exception e) {
			throw e;
		}
		return schedule;
	}

	/**
	 * build the MSUTask from database records.
	 * 
	 * @param data
	 * @return
	 * @throws Exception
	 */
	final private MsuTask buildMSUTask(Object[] data) throws Exception {
		if (data == null || data.length != 11) {
			return null;
		}
		MsuTask task = new MsuTask();
		// ID VARCHAR2(50) NOT NULL,
		// REGION VARCHAR2(50) NOT NULL,
		// CREATE_AT NUMBER(20) NOT NULL,
		// SCHEDULE VARCHAR2(50) NOT NULL,
		// OPERATION VARCHAR2(50) NOT NULL,
		// TARGET_IP VARCHAR2(50) NOT NULL,
		// TARGET_PORT NUMBER(5) NOT NULL,
		// CONTENT VARCHAR2(1000) NOT NULL,
		// META_DATA VARCHAR2(500) NOT NULL,
		// RES_ID NUMBER(20) NOT NULL,
		// IS_REALTIME NUMBER(1) DEFAULT 1

		try {
			task.setId((String) data[0]);
			task.setRegion((String) data[1]);
			task.setCreateAt(((Number) data[2]).longValue());
			task.setSchedule((String) data[3]);
			task.setOperation((String) data[4]);
			task.setTargetIp((String) data[5]);
			task.setTargetPort(((Number) data[6]).intValue());
			task.setContent((String) data[7]);
			task.setMetaData((String) data[8]);
			task.setResId(((Number) data[9]).longValue());
			task.setIsRealtime(((Number) data[10]).intValue() == 1 ? false : true);
		} catch (Exception e) {
			throw e;
		}
		return task;
	}

	final private MSUSysLogStandardRule buildMSUSysLogStandardRule(Object[] data) throws Exception {
		if (data == null || data.length != 6) {
			return null;
		}
		MSUSysLogStandardRule msuSyslogRule = new MSUSysLogStandardRule();
		// CREATE TABLE SYSLOG_RULE
		// (
		// ID NUMBER(20) NOT NULL,
		// RULE_KEY VARCHAR2(20) NOT NULL,
		// RULE_VALUE VARCHAR2(100) NOT NULL,
		// CHECK_NUM NUMBER(20) NOT NULL,
		// CHECK_ACTION VARCHAR2(2) NOT NULL,
		// TYPE_CODE VARCHAR2(50) NOT NULL
		// )
		try {
			msuSyslogRule.setRuleID(((Number) data[0]).longValue());
			msuSyslogRule.setRuleKey((String) data[1]);
			msuSyslogRule.setRuleValue((String) data[2]);
			msuSyslogRule.setCheckNum(((Number) data[3]).longValue());
			msuSyslogRule.setCheckAction((String) data[4]);
			msuSyslogRule.setTypeCode((String) data[5]);
		} catch (Exception e) {
			throw e;
		}
		return msuSyslogRule;
	}
}