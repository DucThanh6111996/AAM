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
public class ItUserActionId implements java.io.Serializable {

    private Long actionId;
    private Long userId;

    public ItUserActionId() {
    }

    public ItUserActionId(Long actionId, Long userId) {
        this.actionId = actionId;
        this.userId = userId;
    }

    @Column(name = "ACTION_ID", precision = 12, scale = 0)
    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    @Column(name = "USER_ID", precision = 12, scale = 0)
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object other) {
        if ((this == other))
            return true;
        if ((other == null))
            return false;
        if (!(other instanceof ItUserActionId))
            return false;
        ItUserActionId castOther = (ItUserActionId) other;

        return ((this.getActionId() == castOther.getActionId()) || (this.getActionId() != null && castOther.getActionId() != null && this.getActionId().equals(
                castOther.getActionId())))
                && ((this.getUserId() == castOther.getUserId()) || (this.getUserId() != null && castOther.getUserId() != null && this.getUserId().equals(
                castOther.getUserId())));
    }

    @Override
    public int hashCode() {
        int result = 17;

        result = 37 * result + (getActionId() == null ? 0 : this.getActionId().hashCode());
        result = 37 * result + (getUserId() == null ? 0 : this.getUserId().hashCode());
        return result;
    }


}
