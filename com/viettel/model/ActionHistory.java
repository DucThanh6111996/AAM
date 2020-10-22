package com.viettel.model;

// Created Aug 1, 2016 9:16:03 AM by quanns2

import javax.persistence.*;
import java.util.Date;

/**
 * @author quanns2
 */
@Entity
@Table(name = "ACTION_HISTORY")
public class ActionHistory implements java.io.Serializable {

	private Long id;
//	private Long actionId;
	private Action action;
	private Date startTime;
	private Date endTime;
	private Integer status;
	private String runUser;
	private String rollbackUser;
	private Date startRollbackTime;
	private Date endRollbackTime;
	private Integer rollbackStatus;
	private String reasonRollback;

    /*20181005_hoangnd_continue fail step_start*/
	private Integer currStep;
	private Integer currKbGroup;
    /*20181005_hoangnd_continue fail step_end*/

	public ActionHistory() {
	}

	public ActionHistory(Long id) {
		this.id = id;
	}

	public ActionHistory(Long id, Long actionId, Date startTime, Date endTime, Integer status, String runUser,
			String rollbackUser, Date startRollbackTime, Date endRollbackTime, Integer rollbackStatus) {
		this.id = id;
//		this.actionId = actionId;
		this.startTime = startTime;
		this.endTime = endTime;
		this.status = status;
		this.runUser = runUser;
		this.rollbackUser = rollbackUser;
		this.startRollbackTime = startRollbackTime;
		this.endRollbackTime = endRollbackTime;
		this.rollbackStatus = rollbackStatus;
	}

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 20, scale = 0)
	/*@GeneratedValue(generator = "generator")
	@SequenceGenerator(name = "generator", sequenceName = "ACTION_HISTORY_SEQ")*/
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "ACTION_HISTORY_SEQ", allocationSize=1)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	/*@Column(name = "ACTION_ID", precision = 20, scale = 0)
	public Long getActionId() {
		return this.actionId;
	}

	public void setActionId(Long actionId) {
		this.actionId = actionId;
	}*/

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ACTION_ID")
	public Action getAction() {
		return action;
	}

	public void setAction(Action action) {
		this.action = action;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "START_TIME", length = 7)
	public Date getStartTime() {
		return this.startTime;
	}

	public void setStartTime(Date startTime) {
		this.startTime = startTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "END_TIME", length = 7)
	public Date getEndTime() {
		return this.endTime;
	}

	public void setEndTime(Date endTime) {
		this.endTime = endTime;
	}

	@Column(name = "STATUS", precision = 10, scale = 0)
	public Integer getStatus() {
		return this.status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Column(name = "RUN_USER", length = 100)
	public String getRunUser() {
		return this.runUser;
	}

	public void setRunUser(String runUser) {
		this.runUser = runUser;
	}

	@Column(name = "ROLLBACK_USER", length = 100)
	public String getRollbackUser() {
		return this.rollbackUser;
	}

	public void setRollbackUser(String rollbackUser) {
		this.rollbackUser = rollbackUser;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "START_ROLLBACK_TIME", length = 7)
	public Date getStartRollbackTime() {
		return this.startRollbackTime;
	}

	public void setStartRollbackTime(Date startRollbackTime) {
		this.startRollbackTime = startRollbackTime;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "END_ROLLBACK_TIME", length = 7)
	public Date getEndRollbackTime() {
		return this.endRollbackTime;
	}

	public void setEndRollbackTime(Date endRollbackTime) {
		this.endRollbackTime = endRollbackTime;
	}

	@Column(name = "ROLLBACK_STATUS", precision = 10, scale = 0)
	public Integer getRollbackStatus() {
		return this.rollbackStatus;
	}

	public void setRollbackStatus(Integer rollbackStatus) {
		this.rollbackStatus = rollbackStatus;
	}

	@Column(name = "REASON_ROLLBACK")
	public String getReasonRollback() {
		return reasonRollback;
	}

	public void setReasonRollback(String reasonRollback) {
		this.reasonRollback = reasonRollback;
	}

	/*20181005_hoangnd_continue fail step_start*/
	@Column(name = "CURR_STEP")
	public Integer getCurrStep() {
		return currStep;
	}

	public void setCurrStep(Integer currStep) {
		this.currStep = currStep;
	}

	@Column(name = "CURR_KB_GROUP")
	public Integer getCurrKbGroup() {
		return currKbGroup;
	}

	public void setCurrKbGroup(Integer currKbGroup) {
		this.currKbGroup = currKbGroup;
	}
	/*20181005_hoangnd_continue fail step_end*/
}
