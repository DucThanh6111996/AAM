package com.viettel.bean;

import java.io.Serializable;

/**
 * @author quanns2
 */
public class ActionSpecial implements Serializable {
    private Long mdActionSpecialId;
    private Long actionTypeId;
    private Long servicesDbId;
    private String actionContent;
    private Integer status;
    private String description;
    private Integer actionUser;
    private Long mdId;
    private Long actionModeId;
    private String username;
    private String lstIpVirtual;
    private String lstIpPhysical;
    private String dbName;
    private String servicesName;
    private String code;
    private String name;
    private String codeMode;
    private String nameMode;

    public Long getMdActionSpecialId() {
        return mdActionSpecialId;
    }

    public void setMdActionSpecialId(Long mdActionSpecialId) {
        this.mdActionSpecialId = mdActionSpecialId;
    }

    public Long getActionTypeId() {
        return actionTypeId;
    }

    public void setActionTypeId(Long actionTypeId) {
        this.actionTypeId = actionTypeId;
    }

    public Long getServicesDbId() {
        return servicesDbId;
    }

    public void setServicesDbId(Long servicesDbId) {
        this.servicesDbId = servicesDbId;
    }

    public String getActionContent() {
        return actionContent;
    }

    public void setActionContent(String actionContent) {
        this.actionContent = actionContent;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getActionUser() {
        return actionUser;
    }

    public void setActionUser(Integer actionUser) {
        this.actionUser = actionUser;
    }

    public Long getMdId() {
        return mdId;
    }

    public void setMdId(Long mdId) {
        this.mdId = mdId;
    }

    public Long getActionModeId() {
        return actionModeId;
    }

    public void setActionModeId(Long actionModeId) {
        this.actionModeId = actionModeId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getLstIpVirtual() {
        return lstIpVirtual;
    }

    public void setLstIpVirtual(String lstIpVirtual) {
        this.lstIpVirtual = lstIpVirtual;
    }

    public String getLstIpPhysical() {
        return lstIpPhysical;
    }

    public void setLstIpPhysical(String lstIpPhysical) {
        this.lstIpPhysical = lstIpPhysical;
    }

    public String getDbName() {
        return dbName;
    }

    public void setDbName(String dbName) {
        this.dbName = dbName;
    }

    public String getServicesName() {
        return servicesName;
    }

    public void setServicesName(String servicesName) {
        this.servicesName = servicesName;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCodeMode() {
        return codeMode;
    }

    public void setCodeMode(String codeMode) {
        this.codeMode = codeMode;
    }

    public String getNameMode() {
        return nameMode;
    }

    public void setNameMode(String nameMode) {
        this.nameMode = nameMode;
    }
}
