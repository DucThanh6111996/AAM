package com.viettel.it.object;

public class ParamTablespaceObject {

	private Long dbId;
	private String dbIp;
	private String dbName;
	private String tbsPrefix;
	private String tbsType;
	private String status;

	public ParamTablespaceObject() {
		super();
	}

	public ParamTablespaceObject(Long dbId, String dbIp, String dbName,
			String tbsPrefix, String tbsType, String status) {
		super();
		this.dbId = dbId;
		this.dbIp = dbIp;
		this.dbName = dbName;
		this.tbsPrefix = tbsPrefix;
		this.tbsType = tbsType;
		this.status = status;
	}

	public Long getDbId() {
		return dbId;
	}

	public void setDbId(Long dbId) {
		this.dbId = dbId;
	}

	public String getDbIp() {
		return dbIp;
	}

	public void setDbIp(String dbIp) {
		this.dbIp = dbIp;
	}

	public String getDbName() {
		return dbName;
	}

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getTbsPrefix() {
		return tbsPrefix;
	}

	public void setTbsPrefix(String tbsPrefix) {
		this.tbsPrefix = tbsPrefix;
	}

	public String getTbsType() {
		return tbsType;
	}

	public void setTbsType(String tbsType) {
		this.tbsType = tbsType;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

}
