package com.secpro.platform.monitoring.schedule.services.scheduleunit;

/**
 * @author baiyanwei Oct 24, 2013
 * 
 *         Task schedule bean.
 * 
 */
public class MSUSchedule {
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
	private String taskID = "";
	private String scheduleID = "";
	private long schedulePoint = 0;
	private long createAt = 0;
	private String region = "";
	private String operation = "";
	private long fetchAt = 0;
	private String fetchBy = "";
	private long executeAt = 0;
	private long executeCost = 0;
	private long executeStatus = 0;
	private String executeDescription = "";

	public MSUSchedule() {
	}

	public MSUSchedule(String taskID, String scheduleID, long schedulePoint, String region, String operation, long createAt) {
		this.taskID = taskID;
		this.scheduleID = scheduleID;
		this.schedulePoint = schedulePoint;
		this.region = region;
		this.operation = operation;
		this.createAt = createAt;
	}

	public String getTaskID() {
		return taskID;
	}

	public void setTaskID(String taskID) {
		this.taskID = taskID;
	}

	public String getScheduleID() {
		return scheduleID;
	}

	public void setScheduleID(String scheduleID) {
		this.scheduleID = scheduleID;
	}

	public long getSchedulePoint() {
		return schedulePoint;
	}

	public void setSchedulePoint(long schedulePoint) {
		this.schedulePoint = schedulePoint;
	}

	public long getCreateAt() {
		return createAt;
	}

	public void setCreateAt(long createAt) {
		this.createAt = createAt;
	}

	public String getRegion() {
		return region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public String getOperation() {
		return operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public long getFetchAt() {
		return fetchAt;
	}

	public void setFetchAt(long fetchAt) {
		this.fetchAt = fetchAt;
	}

	public String getFetchBy() {
		return fetchBy;
	}

	public void setFetchBy(String fetchBy) {
		this.fetchBy = fetchBy;
	}

	public long getExecuteAt() {
		return executeAt;
	}

	public void setExecuteAt(long executeAt) {
		this.executeAt = executeAt;
	}

	public long getExecuteCost() {
		return executeCost;
	}

	public void setExecuteCost(long executeCost) {
		this.executeCost = executeCost;
	}

	public long getExecuteStatus() {
		return executeStatus;
	}

	public void setExecuteStatus(long executeStatus) {
		this.executeStatus = executeStatus;
	}

	public String getExecuteDescription() {
		return executeDescription;
	}

	public void setExecuteDescription(String executeDescription) {
		this.executeDescription = executeDescription;
	}

}
