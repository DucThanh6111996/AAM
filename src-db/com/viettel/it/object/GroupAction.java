package com.viettel.it.object;

import com.viettel.it.model.*;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;

public class GroupAction implements Serializable {
	String groupActionName;
	private boolean declare = true;
	private List<ActionOfFlow> actionOfFlows = new LinkedList<ActionOfFlow>();
	private NodeAccount nodeAccount;

	public GroupAction(String groupActionName, List<ActionOfFlow> actionOfFlows) {
		super();
		this.groupActionName = groupActionName;
		this.actionOfFlows = actionOfFlows;
	}
	
	public GroupAction(String groupActionName,
                       List<ActionOfFlow> actionOfFlows, NodeAccount nodeAccount) {
		super();
		this.groupActionName = groupActionName;
		this.actionOfFlows = actionOfFlows;
		this.nodeAccount = nodeAccount;
	}

	public GroupAction(String groupActionName, boolean declare,
                       List<ActionOfFlow> actionOfFlows, NodeAccount nodeAccount) {
		super();
		this.groupActionName = groupActionName;
		this.declare = declare;
		this.actionOfFlows = actionOfFlows;
		this.nodeAccount = nodeAccount;
	}

	public GroupAction(String groupActionName, Boolean declare) {
		super();
		this.groupActionName = groupActionName;
		this.declare = declare;
	}

	public GroupAction(String groupActionName, List<ActionOfFlow> actionOfFlows, boolean declare) {
		this.groupActionName = groupActionName;
		this.declare = declare;
		this.actionOfFlows = actionOfFlows;
	}

	public String getGroupActionName() {
		return groupActionName;
	}

	public void setGroupActionName(String groupActionName) {
		this.groupActionName = groupActionName;
	}

	

	public List<ActionOfFlow> getActionOfFlows() {
		return actionOfFlows;
	}

	public void setActionOfFlows(List<ActionOfFlow> actionOfFlows) {
		this.actionOfFlows = actionOfFlows;
	}

	public boolean isNoCommand(Node node) {
		for (ActionOfFlow actionOfFlow : actionOfFlows) {
			Action action = actionOfFlow.getAction();
			if (action != null) {
				for (ActionDetail actionDetail : action.getActionDetails()) {
					if (actionDetail.getNodeType().equals(node.getNodeType()) && 
							actionDetail.getVendor().equals(node.getVendor()) &&
							actionDetail.getVersion().equals(node.getVersion())) {
						for (ActionCommand actionCommand : actionDetail.getActionCommands()) {
							if (//actionCommand.getCommandDetail().getNodeType().equals(node.getNodeType()) && 
									actionCommand.getCommandDetail().getVendor().equals(node.getVendor()) &&
									actionCommand.getCommandDetail().getVersion().equals(node.getVersion())) {
								if (actionCommand.getCommandDetail().getCommandTelnetParser() != null)
									return false;
							}
						}
					}
				}
			}
		}
		return true;
	}

	public boolean isDeclare() {
		return declare;
	}

	public void setDeclare(boolean declare) {
		this.declare = declare;
	}

	public NodeAccount getNodeAccount() {
		return nodeAccount;
	}

	public void setNodeAccount(NodeAccount nodeAccount) {
		this.nodeAccount = nodeAccount;
	}

}
