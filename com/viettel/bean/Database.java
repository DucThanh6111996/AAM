package com.viettel.bean;

import java.io.Serializable;

/**
 * @author quanns2
 */
public class Database implements Serializable{
    private Long dbId;
    private String dbName;
    private String dbCode;
    private Long dbtype;
    private String url;
    private String lstIpVirtual;
    private String serviceName;

    public Long getDbId() {
        return dbId;
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getDbCode() {
        return dbCode;
    }

    public void setDbCode(String dbCode) {
        this.dbCode = dbCode;
    }

    public Long getDbtype() {
        return dbtype;
    }

    public void setDbtype(Long dbtype) {
        this.dbtype = dbtype;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getLstIpVirtual() {
        return lstIpVirtual;
    }

    public void setLstIpVirtual(String lstIpVirtual) {
        this.lstIpVirtual = lstIpVirtual;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @Override
    public String toString() {
        return serviceName + "@" + lstIpVirtual;
    }
}
