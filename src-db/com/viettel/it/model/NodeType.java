package com.viettel.it.model;

// Generated Sep 8, 2016 5:07:30 PM by Hibernate Tools 4.0.0


import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * NodeType generated by hbm2java
 */
@Entity
@Table(name = "NODE_TYPE")
public class NodeType implements java.io.Serializable {

	private Long typeId;
	private String typeName;
	private String description;
	private List<CommandDetail> commandDetails = new ArrayList<CommandDetail>(0);
//	private List<ActionDetail> actionDetails = new ArrayList<ActionDetail>(0);
	private List<Node> nodes = new ArrayList<Node>(0);

    public NodeType() {
    }

    @Id
    @Column(name = "TYPE_ID", unique = true, nullable = false, precision = 10, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "NODE_TYPE_SEQ", allocationSize = 1)
    public Long getTypeId() {
        return this.typeId;
    }

    public void setTypeId(Long typeId) {
        this.typeId = typeId;
    }

    @Column(name = "TYPE_NAME", nullable = false, length = 600)
    public String getTypeName() {
        return this.typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    @Column(name = "DESCRIPTION", length = 1500)
    public String getDescription() {
        return this.description;
    }

//	@OneToMany(fetch = FetchType.EAGER, mappedBy = "nodeType")
//	@LazyCollection(LazyCollectionOption.EXTRA)
	@Transient
	public List<CommandDetail> getCommandDetails() {
		return this.commandDetails;
	}


//	@OneToMany(fetch = FetchType.EAGER, mappedBy = "nodeType")
//	public List<ActionDetail> getActionDetails() {
//		return this.actionDetails;
//	}
//
//	public void setActionDetails(List<ActionDetail> actionDetails) {
//		this.actionDetails = actionDetails;
//	}
//
	//@OneToMany(fetch = FetchType.EAGER, mappedBy = "nodeType")
	//@LazyCollection(LazyCollectionOption.EXTRA)
	@Transient
	public List<Node> getNodes() {
		return this.nodes;
	}

	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setCommandDetails(List<CommandDetail> commandDetails) {
		this.commandDetails = commandDetails;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((typeId == null) ? 0 : typeId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		NodeType other = (NodeType) obj;
		if (typeId == null) {
			if (other.typeId != null)
				return false;
		} else if (!typeId.equals(other.typeId))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "NodeType [typeName=" + typeName + "]";
	}
	

}
