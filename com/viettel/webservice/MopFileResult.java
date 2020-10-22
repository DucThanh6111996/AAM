package com.viettel.webservice;

/**
 * Created by quanns2 on 8/23/2016.
 */
public class MopFileResult {
    private String message;

    private Integer status;

    private String mopFile;
    private String mopFileContent;
    private String mopRollbackFile;
    private String mopRollbackFileContent;

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
}
