package com.viettel.it.object;

import java.io.Serializable;

public class RelationNodeModelExcel implements Serializable {

	private String nodeCode;
	private String nodeType;
	private String interfacePort;
	private String nodeCodeRelation;
	private String nodeTypeRelation;
	private String interfacePortRelation;

	public RelationNodeModelExcel() {
		super();
	}

	public RelationNodeModelExcel(String nodeCode, String nodeType,
			String interfacePort, String nodeCodeRelation,
			String nodeTypeRelation, String interfacePortRelation) {
		super();
		this.nodeCode = nodeCode;
		this.nodeType = nodeType;
		this.interfacePort = interfacePort;
		this.nodeCodeRelation = nodeCodeRelation;
		this.nodeTypeRelation = nodeTypeRelation;
		this.interfacePortRelation = interfacePortRelation;
	}

	public String getNodeCode() {
		return nodeCode;
	}

	public void setNodeCode(String nodeCode) {
		this.nodeCode = nodeCode;
	}

	public String getInterfacePort() {
		return interfacePort;
	}

	public void setInterfacePort(String interfacePort) {
		this.interfacePort = interfacePort;
	}

	public String getNodeCodeRelation() {
		return nodeCodeRelation;
	}

	public void setNodeCodeRelation(String nodeCodeRelation) {
		this.nodeCodeRelation = nodeCodeRelation;
	}

	public String getInterfacePortRelation() {
		return interfacePortRelation;
	}

	public void setInterfacePortRelation(String interfacePortRelation) {
		this.interfacePortRelation = interfacePortRelation;
	}

	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	public String getNodeTypeRelation() {
		return nodeTypeRelation;
	}

	public void setNodeTypeRelation(String nodeTypeRelation) {
		this.nodeTypeRelation = nodeTypeRelation;
	}

}
