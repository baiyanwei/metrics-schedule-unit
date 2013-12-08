package com.secpro.platform.monitoring.schedule.services.taskunit;

/**
 * @author baiyanwei Oct 24, 2013
 * 
 * 
 *         Task bean
 */
public class MsuTask {
	final public static String ID_TITLE = "tid";
	final public static String REGION_TITLE = "reg";
	final public static String OPERATION_TITLE = "ope";
	final public static String CREATE_AT_TITLE = "cat";
	final public static String SCHEDULE_TITLE = "sch";
	final public static String CONTENT_TITLE = "con";
	final public static String TARGET_IP_TITLE = "tip";
	final public static String TARGET_PORT_TITLE = "tpt";
	final public static String META_DATA_TITLE = "mda";
	final public static String RES_ID_TITLE = "rid";
	final public static String IS_REALTIME_TITLE = "isrt";
	//

	// Fields

	private String id;
	private String region;
	private Long createAt;
	private String schedule;
	private String operation;
	private String targetIp;
	private Integer targetPort;
	private String metaData;
	private String content;
	private Long resId;
	private Boolean isRealtime;

	// Constructors

	/** default constructor */
	public MsuTask() {
	}

	/** minimal constructor */
	public MsuTask(String id, String region, Long createAt, String schedule, String operation, String targetIp, Integer targetPort, String metaData, String content, Long resId) {
		this.id = id;
		this.region = region;
		this.createAt = createAt;
		this.schedule = schedule;
		this.operation = operation;
		this.targetIp = targetIp;
		this.targetPort = targetPort;
		this.metaData = metaData;
		this.content = content;
		this.resId = resId;
	}

	/** full constructor */
	public MsuTask(String id, String region, Long createAt, String schedule, String operation, String targetIp, Integer targetPort, String metaData, String content, Long resId,
			Boolean isRealtime) {
		this.id = id;
		this.region = region;
		this.createAt = createAt;
		this.schedule = schedule;
		this.operation = operation;
		this.targetIp = targetIp;
		this.targetPort = targetPort;
		this.metaData = metaData;
		this.content = content;
		this.resId = resId;
		this.isRealtime = isRealtime;
	}

	// Property accessors
	public String getId() {
		return this.id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getRegion() {
		return this.region;
	}

	public void setRegion(String region) {
		this.region = region;
	}

	public Long getCreateAt() {
		return this.createAt;
	}

	public void setCreateAt(Long createAt) {
		this.createAt = createAt;
	}

	public String getSchedule() {
		return this.schedule;
	}

	public void setSchedule(String schedule) {
		this.schedule = schedule;
	}

	public String getOperation() {
		return this.operation;
	}

	public void setOperation(String operation) {
		this.operation = operation;
	}

	public String getTargetIp() {
		return this.targetIp;
	}

	public void setTargetIp(String targetIp) {
		this.targetIp = targetIp;
	}

	public Integer getTargetPort() {
		return this.targetPort;
	}

	public void setTargetPort(Integer targetPort) {
		this.targetPort = targetPort;
	}

	public String getMetaData() {
		return this.metaData;
	}

	public void setMetaData(String metaData) {
		this.metaData = metaData;
	}

	public String getContent() {
		return this.content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public Long getResId() {
		return this.resId;
	}

	public void setResId(Long resId) {
		this.resId = resId;
	}

	public Boolean getIsRealtime() {
		return this.isRealtime;
	}

	public void setIsRealtime(Boolean isRealtime) {
		this.isRealtime = isRealtime;
	}

}
