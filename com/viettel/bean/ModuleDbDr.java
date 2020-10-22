package com.viettel.bean;

import java.io.Serializable;

/**
 * @author quanns2
 */
public class ModuleDbDr implements Serializable {
    private Long mdConfigId;
    private Long moduleId;
    private Long dbId;
    private String filePath;
    private String fileName;
    private String drFilePath;
    private String drFileName;

    public Long getMdConfigId() {
        return mdConfigId;
    }

    public void setMdConfigId(Long mdConfigId) {
        this.mdConfigId = mdConfigId;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public String getFilePath() {
        return filePath;
    }

    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getDrFilePath() {
        return drFilePath;
    }

    public void setDrFilePath(String drFilePath) {
        this.drFilePath = drFilePath;
    }

    public String getDrFileName() {
        return drFileName;
    }

    public void setDrFileName(String drFileName) {
        this.drFileName = drFileName;
    }

    public Long getDbId() {
        return dbId;
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }
}
