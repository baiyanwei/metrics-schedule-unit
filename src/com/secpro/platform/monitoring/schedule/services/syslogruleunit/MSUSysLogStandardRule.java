package com.secpro.platform.monitoring.schedule.services.syslogruleunit;

/**
 * @author baiyanwei Oct 24, 2013
 * 
 *         Task schedule bean.
 * 
 */
public class MSUSysLogStandardRule {
	final public static String RULE_ID_TITLE = "ruid";
	final public static String CONTENT_TITLE = "con";
	// final public static String OPERATION_TITLE = "ope";
	// final public static String CREATE_AT_TITLE = "cat";
	// final public static String SCHEDULE_TITLE = "sch";
	// final public static String CONTENT_TITLE = "con";
	// final public static String META_DATA_TITLE = "mda";
	// final public static String RES_ID_TITLE = "rid";
	// final public static String IS_REALTIME_TITLE = "isrt";
	private String _ruleID = null;
	private String _content = null;

	public MSUSysLogStandardRule(String ruleID, String content) {
		super();
		this._ruleID = ruleID;
		this._content = content;
	}

	public MSUSysLogStandardRule() {
	}

	public String getRuleID() {
		return _ruleID;
	}

	public void setRuleID(String ruleID) {
		this._ruleID = ruleID;
	}

	public String getContent() {
		return _content;
	}

	public void setContent(String content) {
		this._content = content;
	}

}
