package com.viettel.webservice;

import java.util.Date;
import java.util.List;

/**
 * Created by quanns2 on 8/23/2016.
 */
public class MopInfo {
    private String name;
    private String code;
    private Date createdDate;
    private List<String> ips;
    private List<String> affectIps;
    private List<String> affectServices;
    private List<ModuleInfo> moduleInfos;
    private String nationCode = "VNM";
    private String mopFile;
    private String mopRollbackFile;
    private String mopFileContent;
    private String mopRollbackFileContent;

    public String getMopFile() {
        return mopFile;
    }

    public void setMopFile(String mopFile) {
        this.mopFile = mopFile;
    }

    public String getMopRollbackFile() {
        return mopRollbackFile;
    }

    public void setMopRollbackFile(String mopRollbackFile) {
        this.mopRollbackFile = mopRollbackFile;
    }

    public String getMopFileContent() {
        return mopFileContent;
    }

    public void setMopFileContent(String mopFileContent) {
        this.mopFileContent = mopFileContent;
    }

    public String getMopRollbackFileContent() {
        return mopRollbackFileContent;
    }

    public void setMopRollbackFileContent(String mopRollbackFileContent) {
        this.mopRollbackFileContent = mopRollbackFileContent;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public Date getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(Date createdDate) {
        this.createdDate = createdDate;
    }

    public List<String> getIps() {
        return ips;
    }

    public void setIps(List<String> ips) {
        this.ips = ips;
    }

    public List<String> getAffectIps() {
        return affectIps;
    }

    public void setAffectIps(List<String> affectIps) {
        this.affectIps = affectIps;
    }

    public List<String> getAffectServices() {
        return affectServices;
    }

    public void setAffectServices(List<String> affectServices) {
        this.affectServices = affectServices;
    }

    public String getNationCode() {
        return nationCode == null ? "VNM" : nationCode;
    }

    public void setNationCode(String nationCode) {
        this.nationCode = nationCode;
    }

    public List<ModuleInfo> getModuleInfos() {
        return moduleInfos;
    }

    public void setModuleInfos(List<ModuleInfo> moduleInfos) {
        this.moduleInfos = moduleInfos;
    }
}
