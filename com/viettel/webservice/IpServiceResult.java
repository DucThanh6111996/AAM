package com.viettel.webservice;

import java.util.List;

/**
 * Created by quanns2 on 8/28/2016.
 */
public class IpServiceResult {
    private String message;

    private Integer status;

    private List<String> ipAddress;

    public List<String> getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(List<String> ipAddress) {
        this.ipAddress = ipAddress;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
