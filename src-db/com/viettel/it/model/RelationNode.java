package com.viettel.it.model;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "RELATION_NODE")
public class RelationNode {

	private Long id;
	private String nodeCode;
	private String nodeType;
	private String interfacePort;
	private String nodeCodeRelation;
	private String nodeTypeRelation;
	private String interfacePortRelation;

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
	@GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "RELATION_NODE_SEQ", allocationSize = 1)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="NODE_CODE", length = 255)
	public String getNodeCode() {
		return nodeCode;
	}

	public void setNodeCode(String nodeCode) {
		this.nodeCode = nodeCode;
	}

	@Column(name="NODE_TYPE", length = 255)
	public String getNodeType() {
		return nodeType;
	}

	public void setNodeType(String nodeType) {
		this.nodeType = nodeType;
	}

	@Column(name="NODE_CODE_RELATION", length = 255)
	public String getNodeCodeRelation() {
		return nodeCodeRelation;
	}

	public void setNodeCodeRelation(String nodeCodeRelation) {
		this.nodeCodeRelation = nodeCodeRelation;
	}

	@Column(name="NODE_TYPE_RELATION", length = 255)
	public String getNodeTypeRelation() {
		return nodeTypeRelation;
	}

	public void setNodeTypeRelation(String nodeTypeRelation) {
		this.nodeTypeRelation = nodeTypeRelation;
	}

	@Column(name="INTERFACE_PORT", length = 255)
	public String getInterfacePort() {
		return interfacePort;
	}

	public void setInterfacePort(String interfacePort) {
		this.interfacePort = interfacePort;
	}

	@Column(name="INTERFACE_PORT_RELATION", length = 255)
	public String getInterfacePortRelation() {
		return interfacePortRelation;
	}

	public void setInterfacePortRelation(String interfacePortRelation) {
		this.interfacePortRelation = interfacePortRelation;
	}

	@Override
	public String toString() {
		return "RelationNode [" + (nodeCode != null ? "nodeCode=" + nodeCode + ", " : "") + (nodeCodeRelation != null ? "nodeCodeRelation=" + nodeCodeRelation : "") + "]\r\n";
	}
	
	
}
