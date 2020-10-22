package com.viettel.webservice;

/**
 * Created by quanns2 on 8/28/2016.
 */
public class AppGroup {
    private Long appGroupId;
    private String applicationCode;
    private String applicationName;
    private Long serviceStatus;

    public Long getAppGroupId() {
        return this.appGroupId;
    }

    public void setAppGroupId(Long appGroupId) {
        this.appGroupId = appGroupId;
    }

    public String getApplicationCode() {
        return this.applicationCode;
    }

    public void setApplicationCode(String applicationCode) {
        this.applicationCode = applicationCode;
    }

    public String getApplicationName() {
        return this.applicationName;
    }

    public void setApplicationName(String applicationName) {
        this.applicationName = applicationName;
    }

    public Long getServiceStatus() {
        return this.serviceStatus;
    }

    public void setServiceStatus(Long serviceStatus) {
        this.serviceStatus = serviceStatus;
    }
}
