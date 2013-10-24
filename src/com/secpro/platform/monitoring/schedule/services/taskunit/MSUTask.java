package com.secpro.platform.monitoring.schedule.services.taskunit;


/**
 * @author baiyanwei
 * Oct 24, 2013
 *
 * 
 * Task bean
 */
public class MSUTask {
	public String _id = "";
	public String _region = "";
	public String _operation = "";
	public String _schedule = "";
	public long _createAt = 0;
	public String _metaData = null;
	public boolean _isRealtime = false;

	public MSUTask(String iD, String region, String operation, String schedule, long createAt, String metaData) {
		super();
		_id = iD;
		this._region = region;
		this._operation = operation;
		this._schedule = schedule;
		this._createAt = createAt;
		this._metaData = metaData;
	}

	public String getID() {
		return _id;
	}

	public void setID(String iD) {
		_id = iD;
	}

	public String getRegion() {
		return _region;
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

	public String getSchedule() {
		return _schedule;
	}

	public void setSchedule(String schedule) {
		this._schedule = schedule;
	}

	public long getCreateAt() {
		return _createAt;
	}

	public void setCreateAt(long createAt) {
		this._createAt = createAt;
	}

	public String getMetaData() {
		return _metaData;
	}

	public void setMetaData(String metaData) {
		this._metaData = metaData;
	}

	public boolean isRealtime() {
		return _isRealtime;
	}

	public void setRealtime(boolean isRealtime) {
		this._isRealtime = isRealtime;
	}

}
