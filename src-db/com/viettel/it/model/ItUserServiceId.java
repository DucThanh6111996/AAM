package com.viettel.it.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * Created by hanh on 5/15/2017.
 */

@Embeddable
public class ItUserServiceId implements java.io.Serializable {

    private Long serviceId;
    private Long userId;

    public ItUserServiceId() {
    }

    public ItUserServiceId(Long serviceId, Long userId) {
        this.serviceId = serviceId;
        this.userId = userId;
    }

    @Column(name = "SERVICE_ID", precision = 12, scale = 0)
    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
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

        ItUserServiceId that = (ItUserServiceId) o;

        return new EqualsBuilder()
                .append(serviceId, that.serviceId)
                .append(userId, that.userId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(serviceId)
                .append(userId)
                .toHashCode();
    }
}
