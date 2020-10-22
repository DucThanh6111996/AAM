package com.viettel.model;

// Created May 5, 2016 4:56:36 PM by quanns2

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.viettel.bean.ServiceDatabase;
import com.viettel.exception.AppException;
import com.viettel.persistence.ActionServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.util.Date;

/**
 * @author quanns2
 */
@Entity
@Table(name = "ACTION_DETAIL_DATABASE")
public class ActionDetailDatabase implements java.io.Serializable {
	private static Logger logger = LogManager.getLogger(ActionDetailDatabase.class);

	private Long id;
	private Long actionId;
//	private Long dbId;
	private ServiceDatabase serviceDb;
	private Long appDbId;

	private Long action;
	private Long actionOrder;
	private String scriptExecute;
	private String scriptBackup;
	private Long type;
	private String template;
	private String templatePath;
	private String cmdCompile;
	private String sperator;
	private String script_path;
	private Long typeImport;
    private String rollbackFile;

	private String scriptText;
	private String backupText;
	private String rollbackText;
	private Integer backupStatus;
	private Integer runStatus;
	
	private Date runStartTime;
	private Date runEndTime;
	private Date backupStartTime;
	private Date backupEndTime;

	private String importDataFile;
	private String localDataFile;

	private Integer kbGroup;

	private Integer rollbackStatus;
	private Date rollbackStartTime;
	private Date rollbackEndTime;
	private Integer testbedMode;

	private Integer runRollbackOnly;
	private ServiceDatabase serviceDatabase;
	//20180918_tudn_start them thoi gian timeout
	private Integer timeOutBackup;
	private Integer timeOutImpact;
	private Integer timeOutRollback;
	//20180918_tudn_end them thoi gian timeout

    /*20181024_hoangnd_timeout db_start*/
	private Integer timeOutTurnOff;
	private Integer timeOutTurnOn;

    @Column(name = "TIMEOUT_TURN_OFF")
	public Integer getTimeOutTurnOff() {
		return timeOutTurnOff;
	}

	public void setTimeOutTurnOff(Integer timeOutTurnOff) {
		this.timeOutTurnOff = timeOutTurnOff;
	}

	@Column(name = "TIMEOUT_TURN_ON")
	public Integer getTimeOutTurnOn() {
		return timeOutTurnOn;
	}

	public void setTimeOutTurnOn(Integer timeOutTurnOn) {
		this.timeOutTurnOn = timeOutTurnOn;
	}
	/*20181024_hoangnd_timeout db_end*/

	/*20181123_hoangnd_log command_start*/
	private String tempFile;

	@Column(name = "TEMP_FILE")
	public String getTempFile() {
		return tempFile;
	}

	public void setTempFile(String tempFile) {
		this.tempFile = tempFile;
	}
	/*20181123_hoangnd_log command_end*/

	/*20181214_hoangnd_them trang thai add buoc fail vao list rollback_start*/
	private Integer isAddRollback;

	@Column(name = "IS_ADD_ROLLBACK")
	public Integer getIsAddRollback() {
		return isAddRollback;
	}

	public void setIsAddRollback(Integer isAddRollback) {
		this.isAddRollback = isAddRollback;
	}
	/*20181214_hoangnd_them trang thai add buoc fail vao list rollback_end*/

	public ActionDetailDatabase() {
	}

	public ActionDetailDatabase(Long id) {
		this.id = id;
	}

	public ActionDetailDatabase(Long id, Long actionId, Long dbId, Long action, Long actionOrder,
                                String scriptExecute, String scriptBackup, Long type, String template, String templatePath,
                                String cmdCompile) {
		this.id = id;
		this.actionId = actionId;
//		this.dbId = dbId;
		this.action = action;
		this.actionOrder = actionOrder;
		this.scriptExecute = scriptExecute;
		this.scriptBackup = scriptBackup;
		this.type = type;
		this.template = template;
		this.templatePath = templatePath;
		this.cmdCompile = cmdCompile;
	}

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
	/*@GeneratedValue(generator = "generator")
	@SequenceGenerator(name = "generator", sequenceName = "ACTION_DETAIL_DATABASE_SEQ")*/
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "ACTION_DETAIL_DATABASE_SEQ", allocationSize=1)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "ACTION_ID", precision = 22, scale = 0)
	public Long getActionId() {
		return this.actionId;
	}

	public void setActionId(Long actionId) {
		this.actionId = actionId;
	}

	/*@Column(name = "DB_ID", precision = 22, scale = 0)
	public Long getDbId() {
		return this.dbId;
	}

	public void setDbId(Long dbId) {
		this.dbId = dbId;
	}*/

	@Column(name = "ACTION", length = 200)
	public Long getAction() {
		return this.action;
	}

	public void setAction(Long action) {
		this.action = action;
	}

	@Column(name = "ACTION_ORDER", precision = 22, scale = 0)
	public Long getActionOrder() {
		return this.actionOrder;
	}

	public void setActionOrder(Long actionOrder) {
		this.actionOrder = actionOrder;
	}

	@Column(name = "SCRIPT_EXECUTE")
	public String getScriptExecute() {
		return this.scriptExecute;
	}

	public void setScriptExecute(String scriptExecute) {
		this.scriptExecute = scriptExecute;
	}

	@Column(name = "SCRIPT_BACKUP")
	public String getScriptBackup() {
		return this.scriptBackup;
	}

	public void setScriptBackup(String scriptBackup) {
		this.scriptBackup = scriptBackup;
	}

	@Column(name = "TYPE", precision = 22, scale = 0)
	public Long getType() {
		return this.type;
	}

	public void setType(Long type) {
		this.type = type;
	}

	@Column(name = "TEMPLATE")
	public String getTemplate() {
		return this.template;
	}

	public void setTemplate(String template) {
		this.template = template;
	}

	@Column(name = "TEMPLATE_PATH", length = 200)
	public String getTemplatePath() {
		return this.templatePath;
	}

	public void setTemplatePath(String templatePath) {
		this.templatePath = templatePath;
	}

	@Column(name = "CMD_COMPILE", length = 200)
	public String getCmdCompile() {
		return this.cmdCompile;
	}

	public void setCmdCompile(String cmdCompile) {
		this.cmdCompile = cmdCompile;
	}

	@Column(name = "SPERATOR", length = 200)
	public String getSperator() {
		return sperator;
	}

	public void setSperator(String sperator) {
		this.sperator = sperator;
	}

	@Column(name = "SCRIPT_PATH", length = 200)
	public String getScript_path() {
		return script_path;
	}

	public void setScript_path(String script_path) {
		this.script_path = script_path;
	}

	@Column(name = "TYPE_IMPORT")
	public Long getTypeImport() {
		return typeImport;
	}

	public void setTypeImport(Long typeImport) {
		this.typeImport = typeImport;
	}

	@Transient
	public ServiceDatabase getServiceDatabase() {
		return serviceDatabase;
	}

	public void setServiceDatabase(ServiceDatabase serviceDatabase) {
		this.serviceDatabase = serviceDatabase;
	}
	/*@ManyToOne()
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinColumn(name = "DB_ID")
	public ServiceDb getServiceDb() {
		return serviceDb;
	}

	public void setServiceDb(ServiceDb serviceDb) {
		this.serviceDb = serviceDb;
	}*/
	@Column(name = "ROLLBACK_FILE")
	public String getRollbackFile() {
		return rollbackFile;
	}

	public void setRollbackFile(String rollbackFile) {
		this.rollbackFile = rollbackFile;
	}

	@Column(name = "DB_ID")
	public Long getAppDbId() {
		return appDbId;
	}

	public void setAppDbId(Long appDbId) {
		this.appDbId = appDbId;
	}

	@Column(name = "SCRIPT_TEXT")
	public String getScriptText() {
		return scriptText;
	}

	public void setScriptText(String scriptText) {
		this.scriptText = scriptText;
	}

	@Column(name = "BACKUP_TEXT")
	public String getBackupText() {
		return backupText;
	}

	public void setBackupText(String backupText) {
		this.backupText = backupText;
	}

	@Column(name = "ROLLBACK_TEXT")
	public String getRollbackText() {
		return rollbackText;
	}

	public void setRollbackText(String rollbackText) {
		this.rollbackText = rollbackText;
	}

	@Column(name = "BACKUP_STATUS")
	public Integer getBackupStatus() {
		return backupStatus;
	}

	public void setBackupStatus(Integer backupStatus) {
		this.backupStatus = backupStatus;
	}

	@Column(name = "RUN_STATUS")
	public Integer getRunStatus() {
		return runStatus;
	}

	public void setRunStatus(Integer runStatus) {
		this.runStatus = runStatus;
	}
	
	@Column(name = "RUN_START_TIME")
	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getRunStartTime() {
		return runStartTime;
	}
	public void setRunStartTime(Date runStartTime) {
		this.runStartTime = runStartTime;
	}

	@Column(name = "RUN_END_TIME")
	public Date getRunEndTime() {
		return runEndTime;
	}
	public void setRunEndTime(Date runEndTime) {
		this.runEndTime = runEndTime;
	}

	@Column(name = "BACKUP_START_TIME")
	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getBackupStartTime() {
		return backupStartTime;
	}
	public void setBackupStartTime(Date backupStartTime) {
		this.backupStartTime = backupStartTime;
	}
	
	@Column(name = "BACKUP_END_TIME")
	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getBackupEndTime() {
		return backupEndTime;
	}
	public void setBackupEndTime(Date backupEndTime) {
		this.backupEndTime = backupEndTime;
	}

	@Column(name = "IMPORT_DATA_FILE")
	public String getImportDataFile() {
		return importDataFile;
	}

	public void setImportDataFile(String importDataFile) {
		this.importDataFile = importDataFile;
	}

	@Column(name = "LOCAL_DATA_FILE")
	public String getLocalDataFile() {
		return localDataFile;
	}

	public void setLocalDataFile(String localDataFile) {
		this.localDataFile = localDataFile;
	}

	@Column(name = "KB_GROUP")
	public Integer getKbGroup() {
		return kbGroup;
	}

	public void setKbGroup(Integer kbGroup) {
		this.kbGroup = kbGroup;
	}

	@Column(name = "ROLLBACK_STATUS")
	public Integer getRollbackStatus() {
		return rollbackStatus;
	}

	public void setRollbackStatus(Integer rollbackStatus) {
		this.rollbackStatus = rollbackStatus;
	}

	@Column(name = "ROLLBACK_START_TIME")
	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getRollbackStartTime() {
		return rollbackStartTime;
	}

	public void setRollbackStartTime(Date rollbackStartTime) {
		this.rollbackStartTime = rollbackStartTime;
	}

	@Column(name = "ROLLBACK_END_TIME")
	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getRollbackEndTime() {
		return rollbackEndTime;
	}

	public void setRollbackEndTime(Date rollbackEndTime) {
		this.rollbackEndTime = rollbackEndTime;
	}

	@Column(name = "TESTBED_MODE")
	public Integer getTestbedMode() {
		return testbedMode;
	}

	public void setTestbedMode(Integer testbedMode) {
		this.testbedMode = testbedMode;
	}

	@Column(name = "RUN_ROLLBACK_ONLY")
	public Integer getRunRollbackOnly() {
		return runRollbackOnly;
	}

	public void setRunRollbackOnly(Integer runRollbackOnly) {
		this.runRollbackOnly = runRollbackOnly;
	}

	//20180918_tudn_start them thoi gian timeout
	@Column(name = "TIMEOUT_BACKUP")
	public Integer getTimeOutBackup() {
		return timeOutBackup != null && timeOutBackup == 0 ? null : timeOutBackup;
	}

	public void setTimeOutBackup(Integer timeOutBackup) {
		this.timeOutBackup = timeOutBackup;
	}

	@Column(name = "TIMEOUT_IMPACT")
	public Integer getTimeOutImpact() {
		return timeOutImpact != null && timeOutImpact == 0 ? null : timeOutImpact;
	}

	public void setTimeOutImpact(Integer timeOutImpact) {
		this.timeOutImpact = timeOutImpact;
	}

	@Column(name = "TIMEOUT_ROLLBACK")
	public Integer getTimeOutRollback() {
		return timeOutRollback != null && timeOutRollback == 0 ? null : timeOutRollback;
	}

	public void setTimeOutRollback(Integer timeOutRollback) {
		this.timeOutRollback = timeOutRollback;
	}
	//20180918_tudn_start them thoi gian timeout
}
