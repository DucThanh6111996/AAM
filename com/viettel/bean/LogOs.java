package com.viettel.bean;

import java.io.Serializable;

/**
 * Created by quanns2 on 4/14/17.
 */
public class LogOs implements Serializable {
    private Long moduleId;
    private String link;
    private String keyWord;
    private String logType;
    private String fileName;

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getKeyWord() {
        return keyWord;
    }

    public void setKeyWord(String keyWord) {
        this.keyWord = keyWord;
    }

    public String getLogType() {
        return logType;
    }

    public void setLogType(String logType) {
        this.logType = logType;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getFullPath() {
        if (link == null || fileName == null)
            return null;
        else {
            if (link.contains(":\\"))
                return link + (link.endsWith("\\")?"":"\\") + fileName;
            else
                return link + (link.endsWith("/")?"":"/") + fileName;

        }
    }
}
