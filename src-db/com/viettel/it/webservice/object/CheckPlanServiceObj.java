package com.viettel.it.webservice.object;

/**
 * Created by quytv7 on 7/30/2018.
 */
public class CheckPlanServiceObj {
    private String crCode;
    private String actionCode;
    private Long statusCr;
    private String statusCrName;

    public String getStatusCrName() {
        return statusCrName;
    }

    public void setStatusCrName(String statusCrName) {
        this.statusCrName = statusCrName;
    }

    public String getCrCode() {
        return crCode;
    }

    public void setCrCode(String crCode) {
        this.crCode = crCode;
    }

    public String getActionCode() {
        return actionCode;
    }

    public void setActionCode(String actionCode) {
        this.actionCode = actionCode;
    }

    public Long getStatusCr() {
        return statusCr;
    }

    public void setStatusCr(Long statusCr) {
        this.statusCr = statusCr;
    }
}
