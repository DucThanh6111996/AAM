package com.viettel.it.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.util.Objects;

/**
 * Created by hanh on 5/15/2017.
 */

@Embeddable
public class ItNodeActionId implements java.io.Serializable {

    private Long actionId;
    private Long nodeId;
    private Long type;

    public ItNodeActionId() {
    }

    public ItNodeActionId(Long actionId, Long nodeId, Long type) {
        this.actionId = actionId;
        this.nodeId = nodeId;
        this.type = type;
    }

    @Column(name = "ACTION_ID", precision = 12, scale = 0)
    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    @Column(name = "NODE_ID", precision = 12, scale = 0)
    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    @Column(name = "TYPE", precision = 6, scale = 0)
    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ItNodeActionId that = (ItNodeActionId) o;
        return Objects.equals(actionId, that.actionId) &&
                Objects.equals(nodeId, that.nodeId) &&
                Objects.equals(type, that.type);
    }

    @Override
    public int hashCode() {
        return Objects.hash(actionId, nodeId, type);
    }
}
