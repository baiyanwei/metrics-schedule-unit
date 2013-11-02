package com.secpro.platform.monitoring.schedule.storages;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;

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
import com.secpro.platform.monitoring.schedule.services.taskunit.MSUTask;
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
		// FETCH_AT NUMBER(20),
		// EXECUTE_AT NUMBER(20),
		// EXECUTE_COST NUMBER(20),
		// EXECUTE_STATUS NUMBER(1),
		// EXECUTE_DESCRIPTION VARCHAR2(50)
		// )
		StringBuffer insertSQL = new StringBuffer();
		// TASK_ID,SCHEDULE_ID,SCHEDULE_POINT,CREATE_AT,FETCH_AT,EXECUTE_AT,EXECUTE_COST,EXECUTE_STATUS,EXECUTE_DESCRIPTION
		insertSQL.append("INSERT INTO MSU_SCHEDULE (TASK_ID,SCHEDULE_ID,SCHEDULE_POINT,CREATE_AT,FETCH_AT,EXECUTE_AT,EXECUTE_COST,EXECUTE_STATUS,EXECUTE_DESCRIPTION) VALUES(");
		insertSQL.append("'").append(taskSchedule.getTaskID()).append("',");
		insertSQL.append("'").append(taskSchedule.getScheduleID()).append("',");
		insertSQL.append(taskSchedule.getSchedulePoint()).append(",");
		insertSQL.append(taskSchedule.getCreateAt()).append(",");
		insertSQL.append(taskSchedule.getFetchAt()).append(",");
		insertSQL.append(taskSchedule.getExecuteAt()).append(",");
		insertSQL.append(taskSchedule.getExecuteCost()).append(",");
		insertSQL.append(taskSchedule.getExecuteStatus()).append(",");
		insertSQL.append("'").append(taskSchedule.getExecuteDescription()).append("'");
		insertSQL.append(")");
		return updateRecords(insertSQL.toString());
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
		removeSQL.append("DELETE FROM MSU_SCHEDULE WHERE TASK_ID='").append(taskSchedule.getTaskID()).append("' AND SCHEDULE_ID='").append(taskSchedule.getScheduleID())
				.append("'");
		return updateRecords(removeSQL.toString());
	}

	/**
	 * Store the task content into DB.
	 * 
	 * @param taskObj
	 */
	public int insertTask(MSUTask task) {
		if (task == null) {
			return 0;
		}
		StringBuffer insertSQL = new StringBuffer();
		//
		// CREATE TABLE MSU_TASK (
		// ID VARCHAR2(50) NOT NULL,
		// REGION VARCHAR2(50) NOT NULL,
		// CREATE_AT NUMBER(20) NOT NULL,
		// SCHEDULE VARCHAR2(255),
		// OPERATION VARCHAR2(50) NOT NULL,
		// META_DATA VARCHAR2(500),
		// CONTENT VARCHAR2(1000),
		// RES_ID NUMBER(20) NOT NULL,
		// IS_REALTIME NUMBER(1) DEFAULT 1
		// )
		insertSQL.append("INSERT INTO MSU_TASK (ID,REGION,CREATE_AT,SCHEDULE,OPERATION,META_DATA,CONTENT,RES_ID,IS_REALTIME) VALUES(");
		insertSQL.append("'").append(task._id).append("',");
		insertSQL.append("'").append(task._region).append("',");
		insertSQL.append(task._createAt).append(",");
		insertSQL.append("'").append(task._schedule).append("',");
		insertSQL.append("'").append(task._operation).append("',");
		insertSQL.append("'").append(task._metaData).append("',");
		insertSQL.append("'").append(task._content).append("',");
		insertSQL.append(task._resID).append(",");
		insertSQL.append(task._isRealtime);
		insertSQL.append(")");
		return updateRecords(insertSQL.toString());
	}

	/**
	 * Update the task content into DB.
	 * 
	 * @param taskObj
	 */
	public int updateTask(MSUTask task) {
		if (task == null) {
			return 0;
		}
		StringBuffer updateSQL = new StringBuffer();
		// ID,REGION,CREATE_AT,SCHEDULE,OPERATION,META_DATA,CONTENT,RES_ID,IS_REALTIME
		updateSQL.append("UPDATE MSU_TASK SET ");
		updateSQL.append("REGION='").append(task._region).append("',");
		updateSQL.append("SCHEDULE='").append(task._schedule).append("',");
		updateSQL.append("OPERATION='").append(task._operation).append("',");
		updateSQL.append("META_DATA='").append(task._metaData).append("',");
		updateSQL.append("CONTENT='").append(task._content).append("',");
		updateSQL.append("RES_ID=").append(task._resID);
		updateSQL.append(" WHERE ID='").append(task._id).append("'");
		return updateRecords(updateSQL.toString());
	}
	public int updateTask(String taskID, HashMap<String, Object> attribMap) {
		if (Assert.isEmptyString(taskID) == true  || Assert.isEmptyMap(attribMap) == true) {
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
	 * remove the task
	 * 
	 * @param task
	 * @return
	 */
	public int removeTask(MSUTask task) {
		if (task == null) {
			return 0;
		}
		StringBuffer removeSQL = new StringBuffer();
		// ID,REGION,CREATE_AT,SCHEDULE,OPERATION,META_DATA,CONTENT,RES_ID,IS_REALTIME
		removeSQL.append("DELETE FROM MSU_TASK WHERE ID='").append(task._id).append("'");
		return updateRecords(removeSQL.toString());
	}

	/**
	 * @param regions
	 * @return
	 */
	public Object[][] queryTasks(String[] regions) {
		StringBuffer regionSQL = new StringBuffer();
		regionSQL.append("select ID,REGION,CREATE_AT,SCHEDULE,OPERATION,CONTENT,META_DATA,RES_ID,IS_REALTIME from MSU_TASK T WHERE T.IS_REALTIME<>0 ");
		if (regions != null && regions.length > 0) {
			if (regions.length > 1000) {
				theLogger.warn("selectInExpOver1000");
			}
			regionSQL.append("AND T.REGION IN (").append(regions[0]);
			// SQL IN () The number of elements is not over 1000.
			for (int i = 1; i < regions.length && i < 1000; i++) {
				regionSQL.append(",").append(regions[i]);
			}
			regionSQL.append(") ");
		}
		regionSQL.append("ORDER BY REGION,ID,OPERATION");
		return selectRecords(regionSQL.toString());
	}

	public Object[][] queryTasksSchedules(String region, long startTimePoint, long endTimePoint) {
		
		// CREATE TABLE MSU_SCHEDULE
				// (
				// TASK_ID VARCHAR2(50) NOT NULL,
				// SCHEDULE_ID VARCHAR2(50) NOT NULL,
				// SCHEDULE_POINT NUMBER(20) NOT NULL,
				// CREATE_AT NUMBER(20) NOT NULL,
				// FETCH_AT NUMBER(20),
				// EXECUTE_AT NUMBER(20),
				// EXECUTE_COST NUMBER(20),
				// EXECUTE_STATUS NUMBER(1),
				// EXECUTE_DESCRIPTION VARCHAR2(50)
				// )
		StringBuffer regionSQL = new StringBuffer();
		regionSQL.append("select TASK_ID,SCHEDULE_ID,SCHEDULE_POINT,CREATE_AT,FETCH_AT,EXECUTE_AT,,EXECUTE_COST,EXECUTE_STATUS,EXECUTE_DESCRIPTION from MSU_SCHEDULE T WHERE T.SCHEDULE_POINT>"+startTimePoint+" AND SCHEDULE_POINT<"+endTimePoint+" ORDER BY TASK_ID,SCHEDULE_ID,SCHEDULE_POINT");
		return selectRecords(regionSQL.toString());
	}

	public Object[][] selectRecords(String sql) {
		if (Assert.isEmptyString(sql) == true) {
			return null;
		}
		theLogger.debug("sql", sql);
		//
		Connection conn = null;
		Statement statment = null;
		ResultSet resultSet = null;
		Object[][] resulteData = null;
		try {
			conn = _dataBaseStorageService.getConnection();
			statment = conn.createStatement();
			resultSet = statment.executeQuery(sql);
			resulteData = new Object[resultSet.getRow()][resultSet.getMetaData().getColumnCount()];
			int i = 0;
			while (resultSet.next()) {
				for (int c = 0; c < resultSet.getMetaData().getColumnCount(); c++) {
					resulteData[i][c] = resultSet.getObject(c);
				}
				i++;
			}
		} catch (SQLException e) {
			e.printStackTrace();
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
}