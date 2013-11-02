package com.secpro.platform.monitoring.schedule.services.scheduleunit;

/**
 * @author baiyanwei Oct 24, 2013
 * 
 *         Task schedule bean.
 * 
 */
public class MSUSchedule {
	// (
	// task_id VARCHAR2(50) not null,
	// schedule_id VARCHAR2(50) not null,
	// schedule_point NUMBER(20) not null,
	// create_at NUMBER(20) not null,
	// region VARCHAR2(50) not null,
	// operation VARCHAR2(50) not null,
	// fetch_at NUMBER(20),
	// execute_at NUMBER(20),
	// execute_cost NUMBER(20),
	// execute_status NUMBER(1),
	// execute_description VARCHAR2(50)
	// )
	public String _taskID = null;
	public String _scheduleID = null;
	public long _schedulePoint = 0;
	public long _createAt = 0;
	public String _region = null;
	public String _operation = null;
	public long _fetchAt = 0;
	public long _executeAt = 0;
	public long _executeCost = 0;
	public long _executeStatus = 0;
	public String _executeDescription = "";

	public MSUSchedule(String taskID, String scheduleID, long schedulePoint, long createAt, String region, String operation, long fetchAt, long executeAt, long executeCost,
			long executeStatus, String executeDescription) {
		this._taskID = taskID;
		this._scheduleID = scheduleID;
		this._schedulePoint = schedulePoint;
		this._region = region;
		this._operation = operation;
		this._createAt = createAt;
		this._fetchAt = fetchAt;
		this._executeAt = executeAt;
		this._executeCost = executeCost;
		this._executeStatus = executeStatus;
		this._executeDescription = executeDescription;
	}

	public MSUSchedule(String taskID, String scheduleID, long schedulePoint, String region, String operation, long createAt) {
		this._taskID = taskID;
		this._scheduleID = scheduleID;
		this._schedulePoint = schedulePoint;
		this._region = region;
		this._operation = operation;
		this._createAt = createAt;
	}

	public String getTaskID() {
		return _taskID;
	}

	public void setTaskID(String taskID) {
		this._taskID = taskID;
	}

	public String getScheduleID() {
		return _scheduleID;
	}

	public void setScheduleID(String scheduleID) {
		this._scheduleID = scheduleID;
	}

	public long getSchedulePoint() {
		return _schedulePoint;
	}

	public void setSchedulePoint(long schedulePoint) {
		this._schedulePoint = schedulePoint;
	}

	public long getCreateAt() {
		return _createAt;
	}

	public void setCreateAt(long createAt) {
		this._createAt = createAt;
	}

	public String getRegion() {
		return _taskID;
	}

	public void setRegion(String region) {
		this._region = region;
	}

	public String getOperation() {
		return _operation;
	}

	public void setOperation(String operation) {
		this._operation = operation;
	}

	public long getFetchAt() {
		return _fetchAt;
	}

	public void setFetchAt(long fetchAt) {
		this._fetchAt = fetchAt;
	}

	public long getExecuteAt() {
		return _executeAt;
	}

	public void setExecuteAt(long executeAt) {
		this._executeAt = executeAt;
	}

	public long getExecuteCost() {
		return _executeCost;
	}

	public void setExecuteCost(long executeCost) {
		this._executeCost = executeCost;
	}

	public long getExecuteStatus() {
		return _executeStatus;
	}

	public void setExecuteStatus(long executeStatus) {
		this._executeStatus = executeStatus;
	}

	public String getExecuteDescription() {
		return _executeDescription;
	}

	public void setExecuteDescription(String executeDescription) {
		this._executeDescription = executeDescription;
	}

}
