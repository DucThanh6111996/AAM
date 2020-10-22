package com.viettel.it.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Created by hanh on 5/15/2017.
 */

@Embeddable
public class ItUserRoleId implements java.io.Serializable {

    private Long roleId;
    private Long userId;

    public ItUserRoleId() {
    }

    public ItUserRoleId(Long roleId, Long userId) {
        this.roleId = roleId;
        this.userId = userId;
    }

    @Column(name = "ROLE_ID", precision = 12, scale = 0)
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Column(name = "USER_ID", precision = 12, scale = 0)
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ItUserRoleId that = (ItUserRoleId) o;

        return new EqualsBuilder()
                .append(roleId, that.roleId)
                .append(userId, that.userId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(roleId)
                .append(userId)
                .toHashCode();
    }
}
