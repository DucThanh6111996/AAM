package com.viettel.model;

// Created Oct 4, 2016 1:42:24 AM by quanns2

import com.fasterxml.jackson.annotation.JsonBackReference;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Date;

/**
 * @author quanns2
 */
@Entity
@Table(name = "ACTION_CUSTOM_ACTION")
public class ActionCustomAction implements java.io.Serializable{

	private Long id;
	@JsonBackReference
	private ActionCustomGroup actionCustomGroup;
	private Integer type;
	private Integer action;
	private Integer priority;
	private Integer runStatus;
	private Integer rollbackTestStatus;
	private Long moduleId;
	private Long dbId;
	private Long fileId;
	private Integer moduleAction;
	private Integer dbAction;
	private String upcodePath;
	private String uploadCodePath;
	private String dbScriptFile;
	private String dbScriptRb;
	private String dbScriptBackup;
	private String exportStatement;
	private String exportCount;
	private String lstFileRemove;
	private String waitReason;
	private String sqlImport;
	private String separator;
	private String importDataFile;
	private String localDataFile;

	private Date runStartTime;
	private Date runEndTime;

	private Integer rollbackTestPriority;

	private String resultFile;
	private String resultRollbackFile;

	private Integer rollbackStatus;
	private Date rollbackStartTime;
	private Date rollbackEndTime;

	public ActionCustomAction() {
	}

	public ActionCustomAction(Long id) {
		this.id = id;
	}

	public ActionCustomAction(Long id, Long groupId, Integer type, Integer action, Integer priority, Integer runStatus,
							  Long moduleId, Long dbId, Long fileId, Integer moduleAction, Integer dbAction, String upcodePath,
							  String uploadCodePath, String dbScriptFile, String dbScriptRb, String exportStatement, String exportCount) {
		this.id = id;
		this.type = type;
		this.action = action;
		this.priority = priority;
		this.runStatus = runStatus;
		this.moduleId = moduleId;
		this.dbId = dbId;
		this.fileId = fileId;
		this.moduleAction = moduleAction;
		this.dbAction = dbAction;
		this.upcodePath = upcodePath;
		this.uploadCodePath = uploadCodePath;
		this.dbScriptFile = dbScriptFile;
		this.dbScriptRb = dbScriptRb;
		this.exportStatement = exportStatement;
		this.exportCount = exportCount;
	}

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 20, scale = 0)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "ACTION_CUSTOM_ACTION_SEQ", allocationSize=1)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@ManyToOne()
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinColumn(name = "GROUP_ID")
	public ActionCustomGroup getActionCustomGroup() {
		return actionCustomGroup;
	}

	public void setActionCustomGroup(ActionCustomGroup actionCustomGroup) {
		this.actionCustomGroup = actionCustomGroup;
	}

	@Column(name = "TYPE", precision = 10, scale = 0)
	public Integer getType() {
		return this.type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "ACTION", precision = 10, scale = 0)
	public Integer getAction() {
		return this.action;
	}

	public void setAction(Integer action) {
		this.action = action;
	}

	@Column(name = "PRIORITY", precision = 10, scale = 0)
	public Integer getPriority() {
		return this.priority;
	}

	public void setPriority(Integer priority) {
		this.priority = priority;
	}

	@Column(name = "RUN_STATUS", precision = 10, scale = 0)
	public Integer getRunStatus() {
		return this.runStatus;
	}

	public void setRunStatus(Integer runStatus) {
		this.runStatus = runStatus;
	}

	@Column(name = "MODULE_ID", precision = 20, scale = 0)
	public Long getModuleId() {
		return this.moduleId;
	}

	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}

	@Column(name = "DB_ID", precision = 20, scale = 0)
	public Long getDbId() {
		return this.dbId;
	}

	public void setDbId(Long dbId) {
		this.dbId = dbId;
	}

	@Column(name = "FILE_ID", precision = 20, scale = 0)
	public Long getFileId() {
		return this.fileId;
	}

	public void setFileId(Long fileId) {
		this.fileId = fileId;
	}

	@Column(name = "MODULE_ACTION", precision = 10, scale = 0)
	public Integer getModuleAction() {
		return this.moduleAction;
	}

	public void setModuleAction(Integer moduleAction) {
		this.moduleAction = moduleAction;
	}

	@Column(name = "DB_ACTION", precision = 10, scale = 0)
	public Integer getDbAction() {
		return this.dbAction;
	}

	public void setDbAction(Integer dbAction) {
		this.dbAction = dbAction;
	}

	@Column(name = "UPCODE_PATH", length = 200)
	public String getUpcodePath() {
		return this.upcodePath;
	}

	public void setUpcodePath(String upcodePath) {
		this.upcodePath = upcodePath;
	}

	@Column(name = "UPLOAD_CODE_PATH", length = 200)
	public String getUploadCodePath() {
		return this.uploadCodePath;
	}

	public void setUploadCodePath(String uploadCodePath) {
		this.uploadCodePath = uploadCodePath;
	}

	@Column(name = "DB_SCRIPT_FILE", length = 200)
	public String getDbScriptFile() {
		return this.dbScriptFile;
	}

	public void setDbScriptFile(String dbScriptFile) {
		this.dbScriptFile = dbScriptFile;
	}

	@Column(name = "DB_SCRIPT_RB", length = 200)
	public String getDbScriptRb() {
		return this.dbScriptRb;
	}

	public void setDbScriptRb(String dbScriptRb) {
		this.dbScriptRb = dbScriptRb;
	}

	@Column(name = "EXPORT_STATEMENT", length = 4000)
	public String getExportStatement() {
		return this.exportStatement;
	}

	public void setExportStatement(String exportStatement) {
		this.exportStatement = exportStatement;
	}

	@Column(name = "EXPORT_COUNT", length = 4000)
	public String getExportCount() {
		return this.exportCount;
	}

	public void setExportCount(String exportCount) {
		this.exportCount = exportCount;
	}

	@Column(name = "LST_FILE_REMOVE")
	public String getLstFileRemove() {
		return lstFileRemove;
	}

	public void setLstFileRemove(String lstFileRemove) {
		this.lstFileRemove = lstFileRemove;
	}

	@Column(name = "WAIT_REASON")
	public String getWaitReason() {
		return waitReason;
	}

	public void setWaitReason(String waitReason) {
		this.waitReason = waitReason;
	}

	@Column(name = "SQL_IMPORT")
	public String getSqlImport() {
		return sqlImport;
	}

	public void setSqlImport(String sqlImport) {
		this.sqlImport = sqlImport;
	}

	@Column(name = "SEPARATOR")
	public String getSeparator() {
		return separator;
	}

	public void setSeparator(String separator) {
		this.separator = separator;
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

	@Column(name = "DB_SCRIPT_BACKUP")
	public String getDbScriptBackup() {
		return dbScriptBackup;
	}

	public void setDbScriptBackup(String dbScriptBackup) {
		this.dbScriptBackup = dbScriptBackup;
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

	@Column(name = "ROLLBACK_TEST_PRIORITY")
	public Integer getRollbackTestPriority() {
		return rollbackTestPriority;
	}

	public void setRollbackTestPriority(Integer rollbackTestPriority) {
		this.rollbackTestPriority = rollbackTestPriority;
	}

	@Column(name = "ROLLBACK_TEST_STATUS")
	public Integer getRollbackTestStatus() {
		return rollbackTestStatus;
	}

	public void setRollbackTestStatus(Integer rollbackTestStatus) {
		this.rollbackTestStatus = rollbackTestStatus;
	}

	@Column(name = "RESULT_FILE")
	public String getResultFile() {
		return resultFile;
	}

	public void setResultFile(String resultFile) {
		this.resultFile = resultFile;
	}

	@Column(name = "RESULT_ROLLBACK_FILE")
	public String getResultRollbackFile() {
		return resultRollbackFile;
	}

	public void setResultRollbackFile(String resultRollbackFile) {
		this.resultRollbackFile = resultRollbackFile;
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

	/*	@Override
	public int compareTo(ActionCustomAction o) {
		return this.priority.compareTo(o.priority);
	}*/
}
