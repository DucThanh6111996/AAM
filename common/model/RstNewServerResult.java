package com.viettel.model;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

@Entity
@Table(name = "SPCL_NEW_SERVER_RESULT")
public class RstNewServerResult implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Long id;
	private String ip;
	private Long appGroupId;
	private String appGroupCode;
	private String appGroupName;
	private Long appId;
	private String appCode;
	private String appName;
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
	
	
	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "SPCL_NEW_SERVER_RESULT_SEQ", allocationSize=1)
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
	
	@Column(name = "APP_ID")
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}
	
	@Column(name = "APP_CODE")
	public String getAppCode() {
		return appCode;
	}
	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}
	
	@Column(name = "APP_NAME")
	public String getAppName() {
		return appName;
	}
	public void setAppName(String appName) {
		this.appName = appName;
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
	
	@Column(name = "KPI_NAME")
	public String getKpiName() {
		return kpiName;
	}
	public void setKpiName(String kpiName) {
		this.kpiName = kpiName;
	}
	
	
	
	
	

}
