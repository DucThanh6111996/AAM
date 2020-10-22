package com.viettel.bean;

import java.io.Serializable;
import java.util.List;

/**
 * quytv7
 */
public class AccountForAppDTO implements Serializable {
    private String usernameAuth;
    private String passwordAuth;
    private String userName;
    private String password;
    private List<String> ipList;
    private String type;
    private String host;
    private String ip;
    private String objectCode;
    private String objectCodeParent;
    private String dbid;
    private String countryCode;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

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

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public List<String> getIpList() {
        return ipList;
    }

    public void setIpList(List<String> ipList) {
        this.ipList = ipList;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getObjectCode() {
        return objectCode;
    }

    public void setObjectCode(String objectCode) {
        this.objectCode = objectCode;
    }

    public String getObjectCodeParent() {
        return objectCodeParent;
    }

    public void setObjectCodeParent(String objectCodeParent) {
        this.objectCodeParent = objectCodeParent;
    }

    public String getDbid() {
        return dbid;
    }

    public void setDbid(String dbid) {
        this.dbid = dbid;
    }

}
