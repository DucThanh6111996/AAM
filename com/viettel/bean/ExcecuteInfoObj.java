package com.viettel.bean;

import java.io.Serializable;

public class ExcecuteInfoObj implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public Long   appId;
	public String appGroupCode;
	public String appGroupName;
	public String appCode;
	public String appName;
	public String fullLog;

	
	
	public StringBuilder logBuilder =new StringBuilder();
	
	public void appendLog(String data){
		this.logBuilder.append("\r\n").append(data);
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
	
	public void setFullLog(String fullLog) {
		this.fullLog = fullLog;
	}
	public String getFullLog() {
		this.fullLog = logBuilder.toString();
		return fullLog;
	}
	
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}
	
}
