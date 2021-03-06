package com.viettel.it.model;

// Generated Sep 20, 2016 10:49:00 AM by Hibernate Tools 4.0.0

import javax.persistence.*;

/**
 * ParamInOut generated by hbm2java
 */
@Entity
@Table(name = "PARAM_IN_OUT" )
public class ParamInOut implements java.io.Serializable {

	private ParamInOutId id;
	private ActionOfFlow actionOfFlowByActionFlowOutId;
	private ActionOfFlow actionOfFlowByActionFlowInId;
	private ActionCommand actionCommandByActionCommandOutputId;
	private ParamInput paramInput;
	private ActionCommand actionCommandByActionCommandInputId;
	private Long nodeId;

	private Integer paramInOutOrder;
	
	public ParamInOut() {
	}

	public ParamInOut(ParamInOutId id, ActionCommand actionCommandByActionCommandOutputId, ParamInput paramInput, ActionCommand actionCommandByActionCommandInputId) {
		this.id = id;
		this.actionCommandByActionCommandOutputId = actionCommandByActionCommandOutputId;
		this.paramInput = paramInput;
		this.actionCommandByActionCommandInputId = actionCommandByActionCommandInputId;
	}

	public ParamInOut(ParamInOutId id, ActionOfFlow actionOfFlowByActionFlowOutId, ActionOfFlow actionOfFlowByActionFlowInId, ActionCommand actionCommandByActionCommandOutputId,
                      ParamInput paramInput, ActionCommand actionCommandByActionCommandInputId) {
		this.id = id;
		this.actionOfFlowByActionFlowOutId = actionOfFlowByActionFlowOutId;
		this.actionOfFlowByActionFlowInId = actionOfFlowByActionFlowInId;
		this.actionCommandByActionCommandOutputId = actionCommandByActionCommandOutputId;
		this.paramInput = paramInput;
		this.actionCommandByActionCommandInputId = actionCommandByActionCommandInputId;
	}

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "actionCommandInputId", column = @Column(name = "ACTION_COMMAND_INPUT_ID", nullable = false, precision = 22, scale = 0)),
			@AttributeOverride(name = "paramInputId", column = @Column(name = "PARAM_INPUT_ID", nullable = false, precision = 22, scale = 0)),
			@AttributeOverride(name = "actionCommandOutputId", column = @Column(name = "ACTION_COMMAND_OUTPUT_ID", nullable = false, precision = 22, scale = 0)) })
	public ParamInOutId getId() {
		return this.id;
	}

	public void setId(ParamInOutId id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.EAGER )
	@JoinColumn(name = "ACTION_FLOW_OUT_ID")
	public ActionOfFlow getActionOfFlowByActionFlowOutId() {
		return this.actionOfFlowByActionFlowOutId;
	}

	public void setActionOfFlowByActionFlowOutId(ActionOfFlow actionOfFlowByActionFlowOutId) {
		this.actionOfFlowByActionFlowOutId = actionOfFlowByActionFlowOutId;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ACTION_FLOW_IN_ID", insertable = false, updatable = false)
	public ActionOfFlow getActionOfFlowByActionFlowInId() {
		return this.actionOfFlowByActionFlowInId;
	}

	public void setActionOfFlowByActionFlowInId(ActionOfFlow actionOfFlowByActionFlowInId) {
		this.actionOfFlowByActionFlowInId = actionOfFlowByActionFlowInId;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ACTION_COMMAND_OUTPUT_ID", nullable = false)
	public ActionCommand getActionCommandByActionCommandOutputId() {
		return this.actionCommandByActionCommandOutputId;
	}

	public void setActionCommandByActionCommandOutputId(ActionCommand actionCommandByActionCommandOutputId) {
		this.actionCommandByActionCommandOutputId = actionCommandByActionCommandOutputId;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "PARAM_INPUT_ID", nullable = false, insertable = false, updatable = false)
	public ParamInput getParamInput() {
		return this.paramInput;
	}

	public void setParamInput(ParamInput paramInput) {
		this.paramInput = paramInput;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@JoinColumn(name = "ACTION_COMMAND_INPUT_ID", nullable = false, insertable = false, updatable = false)
	public ActionCommand getActionCommandByActionCommandInputId() {
		return this.actionCommandByActionCommandInputId;
	}

	public void setActionCommandByActionCommandInputId(ActionCommand actionCommandByActionCommandInputId) {
		this.actionCommandByActionCommandInputId = actionCommandByActionCommandInputId;
	}

	@Transient
	public Integer getParamInOutOrder() {
		return paramInOutOrder;
	}

	public void setParamInOutOrder(Integer paramInOutOrder) {
		this.paramInOutOrder = paramInOutOrder;
	}

	@Column(name = "NODE_ID", nullable = true, precision = 22, scale = 0)
	public Long getNodeId() {
		return nodeId;
	}

	public void setNodeId(Long nodeId) {
		this.nodeId = nodeId;
	}

	
	
}
