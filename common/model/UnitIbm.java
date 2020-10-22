package com.viettel.model;

import javax.persistence.*;
import java.util.Date;

/**
 * Created by quanns2 on 3/13/17.
 */
@Entity
@Table(name = "UNIT_IBM")
public class UnitIbm {
    private String unitCode;
    private String unitName;
    private Integer status;
    private Date lastUpdateTime;

    private String productName;
    private String pm;

    @Id
    @Column(name = "UNIT_CODE", unique = true, nullable = false)
    public String getUnitCode() {
        return unitCode;
    }

    public void setUnitCode(String unitCode) {
        this.unitCode = unitCode;
    }

    @Column(name = "UNIT_NAME")
    public String getUnitName() {
        return unitName;
    }

    public void setUnitName(String unitName) {
        this.unitName = unitName;
    }

    @Column(name = "STATUS")
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Column(name = "LAST_UPDATE_TIME")
    public Date getLastUpdateTime() {
        return lastUpdateTime;
    }

    public void setLastUpdateTime(Date lastUpdateTime) {
        this.lastUpdateTime = lastUpdateTime;
    }

    @Transient
    public String getPm() {
        return pm;
    }

    public void setPm(String pm) {
        this.pm = pm;
    }

    @Transient
    public String getProductName() {
        return productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }
}
