package com.viettel.it.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import java.io.Serializable;

/**
 * Created by hanh on 5/15/2017.
 */
@Embeddable
public class ItUserBusinessGroupId implements Serializable {
   private Long userId;
   private Long businessId;

    @Column(name = "USER_ID", precision = 12, scale = 0)
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Column(name = "BUSINESS_ID", precision = 12, scale = 0)
    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ItUserBusinessGroupId that = (ItUserBusinessGroupId) o;

        return new EqualsBuilder()
                .append(userId, that.userId)
                .append(businessId, that.businessId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(userId)
                .append(businessId)
                .toHashCode();
    }
}
