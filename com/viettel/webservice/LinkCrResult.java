package com.viettel.webservice;

/**
 * @author quanns2
 */
public class LinkCrResult implements java.io.Serializable {
    private String message;

    private Integer status;

    public LinkCrResult() {
    }

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
}
