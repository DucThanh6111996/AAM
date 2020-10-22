package com.viettel.model;

// Created May 26, 2016 5:06:08 PM by quanns2

import javax.persistence.*;

/**
 * @author quanns2
 */
@Entity
@Table(name = "ACTION_DB_CHECKLIST")
public class ActionDbChecklist implements java.io.Serializable {

	private Long id;
	private Long actionId;
	private Long appDbId;
	private Long checklistId;
	private String operationData;
	private String status;

	public ActionDbChecklist() {
	}

	public ActionDbChecklist(Long id) {
		this.id = id;
	}

	public ActionDbChecklist(Long id, Long actionId, Long appDbId, Long checklistId) {
		this.id = id;
		this.actionId = actionId;
		this.appDbId = appDbId;
		this.checklistId = checklistId;
	}

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 20, scale = 0)
	/*@GeneratedValue(generator = "generator")
	@SequenceGenerator(name = "generator", sequenceName = "ACTION_DB_CHECKLIST_SEQ")*/
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "ACTION_DB_CHECKLIST_SEQ", allocationSize=1)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "ACTION_ID", precision = 20, scale = 0)
	public Long getActionId() {
		return this.actionId;
	}

	public void setActionId(Long actionId) {
		this.actionId = actionId;
	}

	@Column(name = "APP_DB_ID", precision = 20, scale = 0)
	public Long getAppDbId() {
		return this.appDbId;
	}

	public void setAppDbId(Long appDbId) {
		this.appDbId = appDbId;
	}

	@Column(name = "CHECKLIST_ID", precision = 20, scale = 0)
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
}
