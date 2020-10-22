package com.viettel.bean;

import com.google.common.base.MoreObjects;

import java.io.Serializable;

/**
 * Created by quanns2 on 4/7/17.
 */
public class Service implements Serializable {
    private Long serviceId;
    private String serviceCode;
    private String serviceName;
    private Long unitId;
    private Long userManager;
    private Long serviceStatus;
    private String projectArea;
    private String stream;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public Long getUserManager() {
        return userManager;
    }

    public void setUserManager(Long userManager) {
        this.userManager = userManager;
    }

    public Long getServiceStatus() {
        return serviceStatus;
    }

    public void setServiceStatus(Long serviceStatus) {
        this.serviceStatus = serviceStatus;
    }

    public String getProjectArea() {
        return projectArea;
    }

    public void setProjectArea(String projectArea) {
        this.projectArea = projectArea;
    }

    public String getStream() {
        return stream;
    }

    public void setStream(String stream) {
        this.stream = stream;
    }

    @Override
    public String toString() {
        return serviceCode + " - " + serviceName;
    }
}
