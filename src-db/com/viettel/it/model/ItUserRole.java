package com.viettel.it.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by hanh on 5/15/2017.
 */
@Entity
@Table(name = "IT_USER_ROLE")
public class ItUserRole implements Serializable {

    private ItUserRoleId id;
    private ItRoles role;
    private ItUsers user;


    public ItUserRole() {
    }

    public ItUserRole(ItUserRoleId id) {
        this.id = id;
    }

    @EmbeddedId
    @AttributeOverrides({ @AttributeOverride(name = "roleId", column = @Column(name = "ROLE_ID", nullable = false, precision = 22, scale = 0)),
            @AttributeOverride(name = "userId", column = @Column(name = "USER_ID", nullable = false, precision = 22, scale = 0)) })
    public ItUserRoleId getId() {
        return id;
    }

    public void setId(ItUserRoleId id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ROLE_ID", nullable = false, insertable = false, updatable = false)
    public ItRoles getRole() {
        return role;
    }

    public void setRole(ItRoles role) {
        this.role = role;
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
