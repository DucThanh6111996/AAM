package com.viettel.controller;

import org.apache.commons.lang3.builder.ToStringBuilder;

import javax.persistence.*;
import java.util.Date;

// Created May 5, 2016 4:56:36 PM by quanns2

/**
 * @author quanns2
 */
@Entity
@Table(name = "ACTION_DETAIL_APP")
public class ActionDetailApp implements java.io.Serializable {

	private Long id;
	private Long actionId;
	private Long moduleId;
	private String action;
	private Long moduleOrder;
	private String actionRollback;
	private String groupAction;
	private String backupPath;
	private String upcodePath;
	private String uploadFilePath;
	private String listFileCode;
	private String checkCmd;
	private String checkCmdResult;
	private String description;
	private Integer runStatus;
	private Integer backupStatus;
	private String md5;
	private String lstFileRemove;
	private Date modifyDate ;
	private Date runStartTime;
	private Date runEndTime;
	private Date backupStartTime;
	private Date backupEndTime;

	private Integer rollbackStatus;
	private Date rollbackStartTime;
	private Date rollbackEndTime;

	private Boolean isFile;
	private String ipServer;

	/*20181117_hoangnd_save all step_start*/
	private Integer beforeStatus;
	private Integer afterStatus;
    /*20181117_hoangnd_save all step_end*/

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

	// 20190417_thenv_start change file config
	private String backupFilePath;
	// 20190417_thenv_end change file config

	public ActionDetailApp() {
	}

	public ActionDetailApp(Long id) {
		this.id = id;
	}

	public ActionDetailApp(Long id, Long actionId, Long moduleId, String action, Long moduleOrder,
			String actionRollback, String groupAction, String backupPath, String upcodePath, String uploadFilePath,
			String listFileCode, String checkCmd, String checkCmdResult) {
		this.id = id;
		this.actionId = actionId;
		this.moduleId = moduleId;
		this.action = action;
		this.moduleOrder = moduleOrder;
		this.actionRollback = actionRollback;
		this.groupAction = groupAction;
		this.backupPath = backupPath;
		this.upcodePath = upcodePath;
		this.uploadFilePath = uploadFilePath;
		this.listFileCode = listFileCode;
		this.checkCmd = checkCmd;
		this.checkCmdResult = checkCmdResult;
	}

	
	
	public ActionDetailApp(Long id,Long appId) {
		super();
		this.moduleId = appId;
	}

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
	/*@GeneratedValue(generator = "generator")
	@SequenceGenerator(name = "generator", sequenceName = "ACTION_DETAIL_APP_SEQ")*/
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "ACTION_DETAIL_APP_SEQ", allocationSize=1)
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

	@Column(name = "MODULE_ID", precision = 22, scale = 0)
	public Long getModuleId() {
		return this.moduleId;
	}

	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}

	@Column(name = "ACTION", length = 200)
	public String getAction() {
		return this.action;
	}

	public void setAction(String action) {
		this.action = action;
	}

	@Column(name = "MODULE_ORDER", precision = 22, scale = 0)
	public Long getModuleOrder() {
		return this.moduleOrder;
	}

	public void setModuleOrder(Long moduleOrder) {
		this.moduleOrder = moduleOrder;
	}

	@Column(name = "ACTION_ROLLBACK", length = 200)
	public String getActionRollback() {
		return this.actionRollback;
	}

	public void setActionRollback(String actionRollback) {
		this.actionRollback = actionRollback;
	}

	@Column(name = "GROUP_ACTION", length = 200)
	public String getGroupAction() {
		return this.groupAction;
	}

	public void setGroupAction(String groupAction) {
		this.groupAction = groupAction;
	}

	@Column(name = "BACKUP_PATH", length = 200)
	public String getBackupPath() {
		return this.backupPath;
	}

	public void setBackupPath(String backupPath) {
		this.backupPath = backupPath;
	}

	@Column(name = "UPCODE_PATH", length = 200)
	public String getUpcodePath() {
		return this.upcodePath;
	}

	public void setUpcodePath(String upcodePath) {
		this.upcodePath = upcodePath;
	}

	@Column(name = "UPLOAD_FILE_PATH", length = 200)
	public String getUploadFilePath() {
		return this.uploadFilePath;
	}

	public void setUploadFilePath(String uploadFilePath) {
		this.uploadFilePath = uploadFilePath;
	}

	@Column(name = "LIST_FILE_CODE")
	public String getListFileCode() {
		return this.listFileCode;
	}

	public void setListFileCode(String listFileCode) {
		this.listFileCode = listFileCode;
	}

	@Column(name = "CHECK_CMD", length = 200)
	public String getCheckCmd() {
		return this.checkCmd;
	}

	public void setCheckCmd(String checkCmd) {
		this.checkCmd = checkCmd;
	}

	@Column(name = "CHECK_CMD_RESULT", length = 200)
	public String getCheckCmdResult() {
		return this.checkCmdResult;
	}

	public void setCheckCmdResult(String checkCmdResult) {
		this.checkCmdResult = checkCmdResult;
	}
	@Column(name = "DESCRIPTION", length = 500)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "RUN_STATUS")
	public Integer getRunStatus() {
		return runStatus;
	}

	public void setRunStatus(Integer runStatus) {
		this.runStatus = runStatus;
	}

	@Column(name = "BACKUP_STATUS")
	public Integer getBackupStatus() {
		return backupStatus;
	}

	public void setBackupStatus(Integer backupStatus) {
		this.backupStatus = backupStatus;
	}

	@Column(name = "MD5")
	public String getMd5() {
		return md5;
	}

	public void setMd5(String md5) {
		this.md5 = md5;
	}

	@Column(name = "LST_FILE_REMOVE")
	public String getLstFileRemove() {
		return lstFileRemove;
	}

	public void setLstFileRemove(String lstFileRemove) {
		this.lstFileRemove = lstFileRemove;
	}
	
	
	@Column(name = "MODIFY_DATE")
	@Temporal(value = TemporalType.TIMESTAMP)
	public Date getModifyDate() {
		return modifyDate;
	}
	
	public void setModifyDate(Date modifyDate) {
		this.modifyDate = modifyDate;
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

	@Column(name = "IS_FILE")
	public Boolean getFile() {
		return isFile;
	}

	public void setFile(Boolean file) {
		isFile = file;
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

	/*20181117_hoangnd_save all step_start*/
	@Column(name = "BEFORE_STATUS")
	public Integer getBeforeStatus() {
		return beforeStatus;
	}

	public void setBeforeStatus(Integer beforeStatus) {
		this.beforeStatus = beforeStatus;
	}

	@Column(name = "AFTER_STATUS")
	public Integer getAfterStatus() {
		return afterStatus;
	}

	public void setAfterStatus(Integer afterStatus) {
		this.afterStatus = afterStatus;
	}
	/*20181117_hoangnd_save all step_end*/

	// 20190417_thenv_start change file config
	@Column(name = "BACKUP_FILE_PATH")
	public String getBackupFilePath() {
		return backupFilePath;
	}
	public void setBackupFilePath(String backupFilePath) {
		this.backupFilePath = backupFilePath;
	}
	// 20190417_thenv_end change file config

	@Override
	public String toString() {
		return new ToStringBuilder(this)
				.append("id", id)
				.append("actionId", actionId)
				.append("moduleId", moduleId)
				.append("action", action)
				.append("moduleOrder", moduleOrder)
				.append("actionRollback", actionRollback)
				.append("groupAction", groupAction)
				.append("backupPath", backupPath)
				.append("upcodePath", upcodePath)
				.append("uploadFilePath", uploadFilePath)
				.append("listFileCode", listFileCode)
				.append("checkCmd", checkCmd)
				.append("checkCmdResult", checkCmdResult)
				.append("description", description)
				.append("runStatus", runStatus)
				.append("backupStatus", backupStatus)
				.append("ipServer", ipServer)
				.toString();
	}

	@Column(name = "IP_SERVER")
	public String getIpServer() {
		return ipServer;
	}

	public void setIpServer(String ipServer) {
		this.ipServer = ipServer;
	}
}
