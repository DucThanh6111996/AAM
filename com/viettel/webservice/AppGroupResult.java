package com.viettel.webservice;

import java.util.List;

/**
 * Created by quanns2 on 8/28/2016.
 */
public class AppGroupResult {
    private String message;

    private Integer status;

    private List<AppGroup> appGroups;

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public List<AppGroup> getAppGroups() {
        return appGroups;
    }

    public void setAppGroups(List<AppGroup> appGroups) {
        this.appGroups = appGroups;
    }
}
