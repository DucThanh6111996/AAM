package com.viettel.it.model;

import javax.persistence.*;
import java.util.List;

/**
 * Created by hanh on 5/15/2017.
 */
@Entity
@Table(name = "IT_USER_BUSINESS_GROUP")
public class ItUserBusinessGroup {
    private ItUserBusinessGroupId id;
    private ItBusinessGroup businessGroup;
    private ItUsers user;

    public ItUserBusinessGroup() {
    }

    public ItUserBusinessGroup(ItUserBusinessGroupId id) {
        this.id = id;
    }

    @EmbeddedId
    @AttributeOverrides({ @AttributeOverride(name = "userId", column = @Column(name = "USER_ID", nullable = false, precision = 22, scale = 0)),
            @AttributeOverride(name = "businessId", column = @Column(name = "BUSINESS_ID", nullable = false, precision = 22, scale = 0)) })
    public ItUserBusinessGroupId getId() {
        return id;
    }

    public void setId(ItUserBusinessGroupId id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BUSINESS_ID", nullable = false, insertable = false, updatable = false)
    public ItBusinessGroup getBusinessGroup() {
        return businessGroup;
    }

    public void setBusinessGroup(ItBusinessGroup businessGroup) {
        this.businessGroup = businessGroup;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "USER_ID", nullable = false, insertable = false, updatable = false)
    public ItUsers getUser() {
        return user;
    }

    public void setUser(ItUsers user) {
        this.user = user;
    }

}
