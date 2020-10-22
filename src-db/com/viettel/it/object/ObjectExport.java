package com.viettel.it.object;

import com.viettel.it.model.Node;

import java.util.List;

public class ObjectExport {
	String groupName;
	List<Node> nodes;
	
	public ObjectExport(String groupName, List<Node> nodes) {
		super();
		this.groupName = groupName;
		this.nodes = nodes;
	}
	public String getGroupName() {
		return groupName;
	}
	public void setGroupName(String groupName) {
		this.groupName = groupName;
	}
	public List<Node> getNodes() {
		return nodes;
	}
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	
}
