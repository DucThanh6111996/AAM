package com.viettel.it.model;

import javax.persistence.*;

/**
 * Created by hanh on 5/15/2017.
 */
@Entity
@Table(name = "IT_USER_SERVICE")
public class ItUserService {
    private ItUserServiceId id;
    private ItUsers user;

    public ItUserService() {
    }

    public ItUserService(ItUserServiceId id) {
        this.id = id;
    }

    @EmbeddedId
    @AttributeOverrides({ @AttributeOverride(name = "serviceId", column = @Column(name = "SERVICE_ID", nullable = false, precision = 22, scale = 0)),
            @AttributeOverride(name = "userId", column = @Column(name = "USER_ID", nullable = false, precision = 22, scale = 0)) })
    public ItUserServiceId getId() {
        return id;
    }

    public void setId(ItUserServiceId id) {
        this.id = id;
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
