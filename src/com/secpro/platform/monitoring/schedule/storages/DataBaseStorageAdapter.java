package com.secpro.platform.monitoring.schedule.storages;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
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
	 * @param regions
	 * @return
	 */
	public List<MsuTask> queryAllFrequencyTask() {
		// ID,REGION,CREATE_AT,SCHEDULE,OPERATION,TARGET_IP,TARGET_PORT,CONTENT,META_DATA,RES_ID,IS_REALTIME
		StringBuffer querySQL = new StringBuffer();
		querySQL.append("SELECT MT.ID,");
		querySQL.append("	       R.TASK_REGION,");
		querySQL.append("	       MT.CREATE_AT,");
		querySQL.append("	       MT.SCHEDULE,");
		querySQL.append("	       MT.OPERATION,");
		querySQL.append("	       MT.TARGET_IP,");
		querySQL.append("	       MT.TARGET_PORT,");
		querySQL.append("	       MT.CONTENT,");
		querySQL.append("	       MT.META_DATA,");
		querySQL.append("	       MT.RES_ID,");
		querySQL.append("	       MT.IS_REALTIME,");
		querySQL.append("	       MT.REGION");
		querySQL.append("	  FROM (SELECT T.ID,");
		querySQL.append("	               T.REGION,");
		querySQL.append("	               T.CREATE_AT,");
		querySQL.append("	               T.SCHEDULE,");
		querySQL.append("	               T.OPERATION,");
		querySQL.append("	               T.TARGET_IP,");
		querySQL.append("	               T.TARGET_PORT,");
		querySQL.append("	               T.CONTENT,");
		querySQL.append("	               T.META_DATA,");
		querySQL.append("	               T.RES_ID,");
		querySQL.append("	               T.IS_REALTIME");
		querySQL.append("	          FROM MSU_TASK T");
		querySQL.append("	         WHERE T.IS_REALTIME = 0) MT");
		querySQL.append("	  LEFT JOIN (SELECT C.TASK_REGION,C.CITY_CODE FROM SYS_CITY C ) R");
		querySQL.append("	    ON MT.REGION = R.CITY_CODE ORDER BY R.TASK_REGION,MT.OPERATION");
		// StringBuffer regionSQL = new StringBuffer();
		// regionSQL.append("select ");
		// regionSQL.append(msuTaskField);
		// regionSQL.append(" from MSU_TASK T WHERE T.IS_REALTIME=0 ");
		// regionSQL.append("ORDER BY REGION,ID,OPERATION");
		//
		List<Object[]> rowData = selectRecords(querySQL.toString());
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
		//
		List<Object[]> rowData = selectRecords("SELECT ID,RULE_KEY,RULE_VALUE,CHECK_NUM,CHECK_ACTION,TYPE_CODE FROM SYSLOG_RULE ORDER BY TYPE_CODE");
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

	public List<MSUSysLogStandardRule> querySysLogStandardRuleByTypeCode(String typeCode) {
		if (Assert.isEmptyString(typeCode) == true) {
			return null;
		}
		//
		List<Object[]> rowData = selectRecords("SELECT ID,RULE_KEY,RULE_VALUE,CHECK_NUM,CHECK_ACTION,TYPE_CODE FROM SYSLOG_RULE WHERE TYPE_CODE='" + typeCode + "'");
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

		// TASK_ID,SCHEDULE_ID,SCHEDULE_POINT,CREATE_AT,REGION,OPERATION,FETCH_AT,FETCH_BY,EXECUTE_AT,EXECUTE_COST,EXECUTE_STATUS,EXECUTE_DESCRIPTION
		StringBuffer regionSQL = new StringBuffer();
		regionSQL.append("SELECT ");
		regionSQL.append(msuScheduleField);
		regionSQL.append(" FROM MSU_SCHEDULE T WHERE T.FETCH_AT=0 AND T.SCHEDULE_POINT>");
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

	/**
	 * get mapping by region and ip.
	 * 
	 * @return
	 */
	public HashMap<String, HashMap<String, String>> queryFireWallTypeCodeMapping() {

		// Map<region,String[]{ip,type_code}>
		StringBuffer querySql = new StringBuffer();
		querySql.append("SELECT T2.TASK_REGION,T1.RES_IP,T1.TYPE_CODE");
		querySql.append("  FROM (SELECT O.RES_IP, O.TYPE_CODE, O.CITY_CODE");
		querySql.append("          FROM SYS_RES_OBJ O");
		querySql.append("         WHERE O.CLASS_ID = 1) T1");
		querySql.append("  LEFT JOIN (SELECT C.CITY_CODE, C.TASK_REGION");
		querySql.append("               FROM SYS_CITY C");
		querySql.append("              WHERE C.TASK_REGION IS NOT NULL) T2");
		querySql.append("    ON T1.CITY_CODE = T2.CITY_CODE");

		//
		List<Object[]> rowData = selectRecords(querySql.toString());
		HashMap<String, HashMap<String, String>> regionMap = new HashMap<String, HashMap<String, String>>();
		if (rowData == null || rowData.isEmpty() == true) {
			return regionMap;
		}
		//
		for (int i = 0; i < rowData.size(); i++) {
			try {
				Object[] row = rowData.get(i);
				String region = (String) row[0];
				String ip = (String) row[1];
				String type_code = (String) row[2];
				if (regionMap.containsKey(region) == false) {
					HashMap<String, String> ipMap = new HashMap<String, String>();
					ipMap.put(ip, type_code);
					regionMap.put(region, ipMap);
				} else {
					HashMap<String, String> ipMap = regionMap.get(region);
					ipMap.put(ip, type_code);
				}
			} catch (Exception e) {
				theLogger.exception(e);
				continue;
			}
		}
		//
		return regionMap;
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
		// TASK_ID,SCHEDULE_ID,SCHEDULE_POINT,CREATE_AT,REGION,OPERATION,FETCH_AT,FETCH_BY,EXECUTE_AT,EXECUTE_COST,EXECUTE_STATUS,EXECUTE_DESCRIPTION
		try {
			schedule.setTaskID((String) data[0]);
			schedule.setScheduleID((String) data[1]);
			schedule.setSchedulePoint(((Number) data[2]).longValue());
			schedule.setCreateAt(((Number) data[3]).longValue());
			schedule.setRegion((String) data[4]);
			schedule.setOperation((String) data[5]);
			schedule.setFetchAt(((Number) data[6]).longValue());
			schedule.setFetchBy((String) data[7]);
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
		if (data == null || data.length != 12) {
			return null;
		}
		MsuTask task = new MsuTask();
		// querySQL.append("SELECT MT.ID,");
		// querySQL.append("	       R.TASK_REGION,");
		// querySQL.append("	       MT.CREATE_AT,");
		// querySQL.append("	       MT.SCHEDULE,");
		// querySQL.append("	       MT.OPERATION,");
		// querySQL.append("	       MT.TARGET_IP,");
		// querySQL.append("	       MT.TARGET_PORT,");
		// querySQL.append("	       MT.CONTENT,");
		// querySQL.append("	       MT.META_DATA,");
		// querySQL.append("	       MT.RES_ID,");
		// querySQL.append("	       MT.IS_REALTIME,");
		// querySQL.append("	       MT.REGION");

		try {
			task.setId((String) data[0]);
			// IF R.TASK_REGION IS EMPTY,WE FIND OTHER REGION MT.REGION
			if (Assert.isEmptyString((String) data[1]) == true) {
				task.setRegion((String) data[11]);
			} else {
				task.setRegion((String) data[1]);
			}
			task.setCreateAt(((Number) data[2]).longValue());
			task.setSchedule((String) data[3]);
			task.setOperation((String) data[4]);
			task.setTargetIp((String) data[5]);
			task.setTargetPort(((Number) data[6]).intValue());
			task.setContent((String) data[7]);
			task.setMetaData((String) data[8]);
			task.setResId(((Number) data[9]).longValue());
			task.setIsRealtime(((Number) data[10]).intValue() == 0 ? false : true);
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