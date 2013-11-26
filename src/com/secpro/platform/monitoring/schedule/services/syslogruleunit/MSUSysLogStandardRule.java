package com.secpro.platform.monitoring.schedule.services.syslogruleunit;

/**
 * @author baiyanwei Oct 24, 2013
 * 
 *         Task schedule bean.
 * 
 */
public class MSUSysLogStandardRule {
	// Id
	// rule_key varchar2(20)
	// rule_value varchar2(100)
	// check_num number(2)
	// check_action varchar2(2)
	// type_code varchar2(10)
	final public static String RULE_ID_TITLE = "ruid";
	final public static String RULE_KEY_TITLE = "kt";
	final public static String RULE_VALUE_TITLE = "kv";
	final public static String RULE_CHECK_NUM_TITLE = "cn";
	final public static String RULE_CHECK_ACTION_TITLE = "ra";
	final public static String RULE_TYPE_CODE_TITLE = "tc";
	//
	private long _ruleID = 0;
	private String _ruleKey = null;
	private String _ruleValue = null;
	private long _checkNum = 0;
	private String _checkAction = null;
	private String _typeCode = null;

	//
	public MSUSysLogStandardRule() {

	}

	//
	public MSUSysLogStandardRule(long ruleID, String ruleKey, String ruleValue, long checkNum, String checkAction, String typeCode) {
		super();
		this._ruleID = ruleID;
		this._ruleKey = ruleKey;
		this._ruleValue = ruleValue;
		this._checkNum = checkNum;
		this._checkAction = checkAction;
		this._typeCode = typeCode;
	}

	//
	public long getRuleID() {
		return _ruleID;
	}

	public void setRuleID(long ruleID) {
		this._ruleID = ruleID;
	}

	public String getRuleKey() {
		return _ruleKey;
	}

	public void setRuleKey(String ruleKey) {
		this._ruleKey = ruleKey;
	}

	public String getRuleValue() {
		return _ruleValue;
	}

	public void setRuleValue(String ruleValue) {
		this._ruleValue = ruleValue;
	}

	public long getCheckNum() {
		return _checkNum;
	}

	public void setCheckNum(long checkNum) {
		this._checkNum = checkNum;
	}

	public String getCheckAction() {
		return _checkAction;
	}

	public void setCheckAction(String checkAction) {
		this._checkAction = checkAction;
	}

	public String getTypeCode() {
		return _typeCode;
	}

	public void setTypeCode(String typeCode) {
		this._typeCode = typeCode;
	}

}
