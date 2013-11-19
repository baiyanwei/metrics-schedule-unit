package com.secpro.platform.monitoring.schedule.services.taskunit;

/**
 * @author baiyanwei Oct 24, 2013
 * 
 * 
 *         Task bean
 */
public class MSUTask {
	final public static String ID_TITLE = "tid";
	final public static String REGION_TITLE = "reg";
	final public static String OPERATION_TITLE = "ope";
	final public static String CREATE_AT_TITLE = "cat";
	final public static String SCHEDULE_TITLE = "sch";
	final public static String CONTENT_TITLE = "con";
	final public static String META_DATA_TITLE = "mda";
	final public static String RES_ID_TITLE = "rid";
	final public static String IS_REALTIME_TITLE = "isrt";
	//

	private String _id = "";
	private String _region = "";
	private String _operation = "";
	private String _schedule = "";
	private long _createAt = 0;
	private String _metaData = null;
	private String _content = null;
	private long _resID = 0;
	private boolean _isRealtime = false;

	public MSUTask(String id, String region, String operation, String schedule, long createAt, String metaData, String content, long resID, boolean isRealtime) {
		super();
		this._id = id;
		this._region = region;
		this._operation = operation;
		this._schedule = schedule;
		this._createAt = createAt;
		this._metaData = metaData;
		this._content = content;
		this._resID = resID;
		this._isRealtime = isRealtime;
	}

	public MSUTask() {
		// TODO Auto-generated constructor stub
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

	public String getContent() {
		return _content;
	}

	public void setContent(String content) {
		this._content = content;
	}

	public long getResID() {
		return _resID;
	}

	public void setResID(long resID) {
		this._resID = resID;
	}

}
