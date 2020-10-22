package com.viettel.bean;

import java.io.Serializable;

public class AppResult implements Serializable {
	
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Integer status;
	private Long appGroupId;
	private String appGroupCode;
	private String appGroupName;
	private Long appId;
	private String appCode;
	private String appName;
	private String kpiName;
	private String mathOption;
	private String operationData;
	private String log;
	private String threholdValue;
	
	public String getMathOption() {
		return mathOption;
	}
	public void setMathOption(String mathOption) {
		this.mathOption = mathOption;
	}
	public String getOperationData() {
		return operationData;
	}
	public void setOperationData(String operationData) {
		this.operationData = operationData;
	}
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
		this.log = log;
	}
	public String getThreholdValue() {
		return threholdValue;
	}
	public void setThreholdValue(String threholdValue) {
		this.threholdValue = threholdValue;
	}
	
	
	
	
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	public Long getAppGroupId() {
		return appGroupId;
	}
	public void setAppGroupId(Long appGroupId) {
		this.appGroupId = appGroupId;
	}
	public String getAppGroupCode() {
		return appGroupCode;
	}
	public void setAppGroupCode(String appGroupCode) {
		this.appGroupCode = appGroupCode;
	}
	public String getAppGroupName() {
		return appGroupName;
	}
	public void setAppGroupName(String appGroupName) {
		this.appGroupName = appGroupName;
	}
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}
	public String getAppCode() {
		return appCode;
	}
	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
	}
	public String getKpiName() {
		return kpiName;
	}
	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}
	
}
