/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.model;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 *
 * @author hienhv4
 */
@Entity
@Table(name = "VERSION")
public class Version implements java.io.Serializable {

    private Long versionId;
    private String versionName;
    private String description;
    private List<Node> nodes = new ArrayList<Node>(0);
    private List<CommandDetail> commandDetails = new ArrayList<CommandDetail>(0);

    @Id
    @Column(name = "VERSION_ID", unique = true, nullable = false, precision = 10, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "VERSION_SEQ", allocationSize = 1)
    public Long getVersionId() {
        return versionId;
    }

    public void setVersionId(Long versionId) {
        this.versionId = versionId;
    }

    @Column(name = "VERSION_NAME", nullable = false, length = 100)
    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    @Column(name = "DESCRIPTION", length = 1500)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Transient
    public List<Node> getNodes() {
        return this.nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    @Transient
    public List<CommandDetail> getCommandDetails() {
        return this.commandDetails;
    }

    public void setCommandDetails(List<CommandDetail> commandDetails) {
        this.commandDetails = commandDetails;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((versionId == null) ? 0 : versionId.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (getClass() != obj.getClass()) {
            return false;
        }
        Version other = (Version) obj;
        if (versionId == null) {
            if (other.versionId != null) {
                return false;
            }
        } else if (!versionId.equals(other.versionId)) {
            return false;
        }
        return true;
    }
 

	@Override
	public String toString() {
		return "Version [versionName=" + versionName + "]";
	}
	
	
}
