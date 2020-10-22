package com.viettel.bean;

/**
 * Created by quytv7 on 10/18/2019.
 */
public class AccountChangeForAppDTO {
    private String usernameAuth;
    private String passwordAuth;
    private String startTime;
    private String endTime;

    public String getUsernameAuth() {
        return usernameAuth;
    }

    public void setUsernameAuth(String usernameAuth) {
        this.usernameAuth = usernameAuth;
    }

    public String getPasswordAuth() {
        return passwordAuth;
    }

    public void setPasswordAuth(String passwordAuth) {
        this.passwordAuth = passwordAuth;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }
}
