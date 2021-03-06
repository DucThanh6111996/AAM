package com.viettel.it.model;

// Generated Sep 14, 2016 1:34:53 PM by Hibernate Tools 4.0.0

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;

/**
 * NodeRunGroupAction generated by hbm2java
 */
@Entity
@Table(name = "NODE_RUN_GROUP_ACTION" )
public class NodeRunGroupAction implements java.io.Serializable {

	private NodeRunGroupActionId id;
	private ActionOfFlow actionOfFlow;
	private NodeRun nodeRun;

	public NodeRunGroupAction() {
	}

	public NodeRunGroupAction(NodeRunGroupActionId id, ActionOfFlow actionOfFlow, NodeRun nodeRun) {
		this.id = id;
		this.actionOfFlow = actionOfFlow;
		this.nodeRun = nodeRun;
	}

	@EmbeddedId
	@AttributeOverrides({ @AttributeOverride(name = "nodeId", column = @Column(name = "NODE_ID", nullable = false, precision = 22, scale = 0)),
			@AttributeOverride(name = "flowRunId", column = @Column(name = "FLOW_RUN_ID", nullable = false, precision = 22, scale = 0)),
			@AttributeOverride(name = "stepNum", column = @Column(name = "STEP_NUM", nullable = false, precision = 22, scale = 0)) })
	public NodeRunGroupActionId getId() {
		return this.id;
	}

	public void setId(NodeRunGroupActionId id) {
		this.id = id;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@LazyCollection(LazyCollectionOption.EXTRA)
	@JoinColumn(name = "STEP_NUM", nullable = false, insertable = false, updatable = false)
	public ActionOfFlow getActionOfFlow() {
		return this.actionOfFlow;
	}

	public void setActionOfFlow(ActionOfFlow actionOfFlow) {
		this.actionOfFlow = actionOfFlow;
	}

	@ManyToOne(fetch = FetchType.EAGER)
	@LazyCollection(LazyCollectionOption.EXTRA)
	@JoinColumns({ @JoinColumn(name = "NODE_ID", referencedColumnName = "NODE_ID", nullable = false, insertable = false, updatable = false),
			@JoinColumn(name = "FLOW_RUN_ID", referencedColumnName = "FLOW_RUN_ID", nullable = false, insertable = false, updatable = false) })
	public NodeRun getNodeRun() {
		return this.nodeRun;
	}

	public void setNodeRun(NodeRun nodeRun) {
		this.nodeRun = nodeRun;
	}

}
