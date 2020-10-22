package com.viettel.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "SPCL_NEW_DB_RESULT")
public class RstNewDbResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private String ip;
	private Long appGroupId;
	private String appGroupCode;
	private String appGroupName;
	private Long dbId;
	private String dbCode;
	private String dbName;
	private Long kpiId;
	private String kpiCode;
	private String kpiName;
	private Integer mathOption;
	private String operationData;
	private String log;
	private String threholdValue;
	private Integer status;
	private Long turnId;
	private Date createDate;
	private String username;
	private String connectionString;
	private String sqlCommand;
	
	
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "SPCL_NEW_DB_RESULT_SEQ", allocationSize=1)
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name = "IP")
	public String getIp() {
		return ip;
	}
	public void setIp(String ip) {
		this.ip = ip;
	}
	
	@Column(name = "APP_GROUP_ID")
	public Long getAppGroupId() {
		return appGroupId;
	}
	public void setAppGroupId(Long appGroupId) {
		this.appGroupId = appGroupId;
	}
	
	@Column(name = "APP_GROUP_CODE")
	public String getAppGroupCode() {
		return appGroupCode;
	}
	public void setAppGroupCode(String appGroupCode) {
		this.appGroupCode = appGroupCode;
	}
	
	@Column(name = "APP_GROUP_NAME")
	public String getAppGroupName() {
		return appGroupName;
	}
	public void setAppGroupName(String appGroupName) {
		this.appGroupName = appGroupName;
	}
	
	@Column(name = "KPI_ID")
	public Long getKpiId() {
		return kpiId;
	}
	public void setKpiId(Long kpiId) {
		this.kpiId = kpiId;
	}
	
	@Column(name = "KPI_CODE")
	public String getKpiCode() {
		return kpiCode;
	}
	public void setKpiCode(String kpiCode) {
		this.kpiCode = kpiCode;
	}
	
	@Column(name = "MATH_OPTION")
	public Integer getMathOption() {
		return mathOption;
	}
	public void setMathOption(Integer mathOption) {
		this.mathOption = mathOption;
	}
	
	@Column(name = "OPERATION_DATA")
	public String getOperationData() {
		return operationData;
	}
	public void setOperationData(String operationData) {
		this.operationData = operationData;
	}
	
	@Column(name = "LOG")
	public String getLog() {
		return log;
	}
	public void setLog(String log) {
		this.log = log;
	}
	
	@Column(name = "THREHOLD_VALUE")
	public String getThreholdValue() {
		return threholdValue;
	}
	public void setThreholdValue(String threholdValue) {
		this.threholdValue = threholdValue;
	}
	
	@Column(name = "STATUS")
	public Integer getStatus() {
		return status;
	}
	public void setStatus(Integer status) {
		this.status = status;
	}
	
	@Column(name = "TURN_ID")
	public Long getTurnId() {
		return turnId;
	}
	public void setTurnId(Long turnId) {
		this.turnId = turnId;
	}
	
	@Column(name = "CREATE_DATE")
	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getCreateDate() {
		return createDate;
	}
	public void setCreateDate(Date createDate) {
		this.createDate = createDate;
	}
	
	@Column(name = "DB_ID")
	public Long getDbId() {
		return dbId;
	}
	public void setDbId(Long dbId) {
		this.dbId = dbId;
	}
	
	@Column(name = "DB_CODE")
	public String getDbCode() {
		return dbCode;
	}
	public void setDbCode(String dbCode) {
		this.dbCode = dbCode;
	}
	
	@Column(name = "DB_NAME")
	public String getDbName() {
		return dbName;
	}
	public void setDbName(String dbName) {
		this.dbName = dbName;
	}
	
	@Column(name = "USERNAME")
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	
	@Column(name = "CONNECTION_STRING")
	public String getConnectionString() {
		return connectionString;
	}
	public void setConnectionString(String connectionString) {
		this.connectionString = connectionString;
	}
	
	@Column(name = "KPI_NAME")
	public String getKpiName() {
		return kpiName;
	}
	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}
	
	@Column(name = "SQL_COMMAND")
	public String getSqlCommand() {
		return sqlCommand;
	}
	public void setSqlCommand(String sqlCommand) {
		this.sqlCommand = sqlCommand;
	}
	
	
	
	
	

}
