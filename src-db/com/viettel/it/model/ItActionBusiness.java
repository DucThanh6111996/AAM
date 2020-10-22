package com.viettel.it.model;

import javax.persistence.*;

/**
 * Created by hanh on 5/15/2017.
 */
@Entity
@Table(name = "IT_ACTION_BUSINESS")
public class ItActionBusiness {
    private ItActionBusinessId id;

    private String businessTitle;

    public ItActionBusiness() {
    }

    public ItActionBusiness(ItActionBusinessId id) {
        this.id = id;
    }

    @EmbeddedId
    @AttributeOverrides({ @AttributeOverride(name = "nodeId", column = @Column(name = "NODE_ID", nullable = false, precision = 22, scale = 0)),
            @AttributeOverride(name = "actionId", column = @Column(name = "ACTION_ID", nullable = false, precision = 22, scale = 0)),
            @AttributeOverride(name = "serviceId", column = @Column(name = "SERVICE_ID", nullable = false, precision = 22, scale = 0)) })
    public ItActionBusinessId getId() {
        return id;
    }

    public void setId(ItActionBusinessId id) {
        this.id = id;
    }

    @Column(name = "BUSINESS_TITLE", length = 220)
    public String getBusinessTitle() {
        return businessTitle;
    }

    public void setBusinessTitle(String businessTitle) {
        this.businessTitle = businessTitle;
    }
}
