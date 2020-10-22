package com.viettel.it.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Created by hanh on 5/15/2017.
 */

@Embeddable
public class ItActionBusinessId implements java.io.Serializable {

    private Long nodeId;
    private Long actionId;
    private Long serviceId;

    public ItActionBusinessId() {
    }

    public ItActionBusinessId(Long nodeId, Long actionId, Long serviceId) {
        this.nodeId = nodeId;
        this.actionId = actionId;
        this.serviceId = serviceId;
    }

    @Column(name = "NODE_ID", precision = 12, scale = 0)
    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    @Column(name = "ACTION_ID", precision = 12, scale = 0)
    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    @Column(name = "SERVICE_ID", precision = 12, scale = 0)
    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ItActionBusinessId that = (ItActionBusinessId) o;

        return new EqualsBuilder()
                .append(nodeId, that.nodeId)
                .append(actionId, that.actionId)
                .append(serviceId, that.serviceId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(nodeId)
                .append(actionId)
                .append(serviceId)
                .toHashCode();
    }
}
