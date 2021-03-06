package com.viettel.it.model;

// Generated Sep 8, 2016 5:07:30 PM by Hibernate Tools 4.0.0

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Action generated by hbm2java
 */
@Entity
@Table(name = "ACCOUNT_GROUP_MOP")
public class AccountGroupMop implements java.io.Serializable {

	private Long id;
	private Long flowRunId;
	private Long nodeAccountId;
	private Long nodeId;
	private Long actionOfFlowId;

	@Id
    @Column(name = "ID", unique = true, nullable = false, precision = 12, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "ACCOUNT_GROUP_MOP_SEQ", allocationSize = 1)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "FLOW_RUN_ID", precision = 12, scale = 0)
	public Long getFlowRunId() {
		return flowRunId;
	}

	public void setFlowRunId(Long flowRunId) {
		this.flowRunId = flowRunId;
	}

	@Column(name = "NODE_ACCOUNT_ID", precision = 12, scale = 0)
	public Long getNodeAccountId() {
		return nodeAccountId;
	}

	public void setNodeAccountId(Long nodeAccountId) {
		this.nodeAccountId = nodeAccountId;
	}

	@Column(name = "NODE_ID", precision = 12, scale = 0)
	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	@Column(name = "ACTION_OF_FLOW_ID", precision = 12, scale = 0)
	public Long getActionOfFlowId() {
		return actionOfFlowId;
	}

	public void setActionOfFlowId(Long actionOfFlowId) {
		this.actionOfFlowId = actionOfFlowId;
	}

}
