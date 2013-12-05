package com.secpro.platform.monitoring.schedule.node;

public class InterfaceParameter {
	public static final class HttpHeaderParameter {
		final public static String REGION = "region";
		final public static String OPERATIONS = "operations";
		final public static String COUNTER = "counter";
		final public static String PUBLIC_KEY = "public_key";
		final public static String MCA_NAME = "mca_name";
		final public static String SYSLOG_RULE_PUSH_URL = "push_url";
		final public static String OPERATION_TYPE = "operationType";
	}

	public static final class ManagementParameter {
		final public static String OPERATION_TYPE = "operationType";
	}

	public static final class MonitoringTask {
		public static final String TASK_ID_PROPERTY_NAME = "tid";
		public static final String TASK_SCHEDULE_ID_PROPERTY_NAME = "sid";
		public static final String TASK_REGION_PROPERTY_NAME = "reg";
		public static final String TASK_OPERATION_PROPERTY_NAME = "ope";
		public static final String TASK_CREATED_AD_PROPERTY_NAME = "cat";
		public static final String TASK_SCHEDULE_POINT_PROPERTY_NAME = "sat";
		public static final String TASK_TARGET_IP_PROPERTY_NAME = "tip";
		public static final String TASK_TARGET_PORT_PROPERTY_NAME = "tpt";
		public static final String TASK_CONTENT_PROPERTY_NAME = "con";
		public static final String TASK_META_DATA_NAME = "mda";
	}
}
