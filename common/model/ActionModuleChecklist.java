package com.viettel.model;

// Created May 5, 2016 4:56:36 PM by quanns2

import javax.persistence.*;

/**
 * @author quanns2
 */
@Entity
@Table(name = "ACTION_MODULE_CHECKLIST")
public class ActionModuleChecklist implements java.io.Serializable {

	private Long id;
	private Long actionModuleId;
	private Long checklistId;
	private String operationData;
	private String status;

	/*20181117_hoangnd_save all step_start*/
	private String statusBefore;
	private String statusImpact;
	private String statusAfter;
	private String statusRollback;
	private String resultBefore;
	private String resultImpact;
	private String resultAfter;
	private String resultRollback;
	private String limitedBefore;
	private String limitedImpact;
	private String limitedAfter;
	private String limitedRollback;
    /*20181117_hoangnd_save all step_end*/

	public ActionModuleChecklist() {
	}

	public ActionModuleChecklist(Long id) {
		this.id = id;
	}

	public ActionModuleChecklist(Long id, Long actionModuleId, Long checklistId) {
		this.id = id;
		this.actionModuleId = actionModuleId;
		this.checklistId = checklistId;
	}

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
	/*@GeneratedValue(generator = "generator")
	@SequenceGenerator(name = "generator", sequenceName = "ACTION_MODULE_CHECKLIST_SEQ")*/
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "ACTION_MODULE_CHECKLIST_SEQ", allocationSize=1)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "ACTION_MODULE_ID", precision = 22, scale = 0)
	public Long getActionModuleId() {
		return this.actionModuleId;
	}

	public void setActionModuleId(Long actionModuleId) {
		this.actionModuleId = actionModuleId;
	}

	@Column(name = "CHECKLIST_ID", precision = 22, scale = 0)
	public Long getChecklistId() {
		return this.checklistId;
	}

	public void setChecklistId(Long checklistId) {
		this.checklistId = checklistId;
	}

	@Column(name = "OPERATION_DATA")
	public String getOperationData() {
		return operationData;
	}

	public void setOperationData(String operationData) {
		this.operationData = operationData;
	}

	@Column(name = "STATUS")
	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}

	/*20181117_hoangnd_save all step_start*/
	@Column(name = "STATUS_BEFORE")
	public String getStatusBefore() {
		return statusBefore;
	}

	public void setStatusBefore(String statusBefore) {
		this.statusBefore = statusBefore;
	}

	@Column(name = "STATUS_IMPACT")
	public String getStatusImpact() {
		return statusImpact;
	}

	public void setStatusImpact(String statusImpact) {
		this.statusImpact = statusImpact;
	}

	@Column(name = "STATUS_AFTER")
	public String getStatusAfter() {
		return statusAfter;
	}

	public void setStatusAfter(String statusAfter) {
		this.statusAfter = statusAfter;
	}

	@Column(name = "STATUS_ROLLBACK")
	public String getStatusRollback() {
		return statusRollback;
	}

	public void setStatusRollback(String statusRollback) {
		this.statusRollback = statusRollback;
	}

	@Column(name = "RESULT_BEFORE")
	public String getResultBefore() {
		return resultBefore;
	}

	public void setResultBefore(String resultBefore) {
		this.resultBefore = resultBefore;
	}

	@Column(name = "RESULT_IMPACT")
	public String getResultImpact() {
		return resultImpact;
	}

	public void setResultImpact(String resultImpact) {
		this.resultImpact = resultImpact;
	}

	@Column(name = "RESULT_AFTER")
	public String getResultAfter() {
		return resultAfter;
	}

	public void setResultAfter(String resultAfter) {
		this.resultAfter = resultAfter;
	}

	@Column(name = "RESULT_ROLLBACK")
	public String getResultRollback() {
		return resultRollback;
	}

	public void setResultRollback(String resultRollback) {
		this.resultRollback = resultRollback;
	}

	@Column(name = "LIMITED_BEFORE")
	public String getLimitedBefore() {
		return limitedBefore;
	}

	public void setLimitedBefore(String limitedBefore) {
		this.limitedBefore = limitedBefore;
	}

	@Column(name = "LIMITED_IMPACT")
	public String getLimitedImpact() {
		return limitedImpact;
	}

	public void setLimitedImpact(String limitedImpact) {
		this.limitedImpact = limitedImpact;
	}

	@Column(name = "LIMITED_AFTER")
	public String getLimitedAfter() {
		return limitedAfter;
	}

	public void setLimitedAfter(String limitedAfter) {
		this.limitedAfter = limitedAfter;
	}

	@Column(name = "LIMITED_ROLLBACK")
	public String getLimitedRollback() {
		return limitedRollback;
	}

	public void setLimitedRollback(String limitedRollback) {
		this.limitedRollback = limitedRollback;
	}
	/*20181117_hoangnd_save all step_end*/
}
