package com.viettel.bean;

import org.apache.commons.lang3.builder.EqualsBuilder;

import java.io.InputStream;
import java.io.Serializable;

/**
 * @author anhnt2 - Rikkeisoft
 */
public class GetFileFromServer implements Serializable {
    private boolean ok;
    private InputStream inputStream;
    private String contentFile;
    private String backupFileName;

    public boolean isOk() {
        return ok;
    }

    public void setOk(boolean ok) {
        this.ok = ok;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    public String getContentFile() {
        return contentFile;
    }

    public void setContentFile(String contentFile) {
        this.contentFile = contentFile;
    }

    public String getBackupFileName() {
        return backupFileName;
    }

    public void setBackupFileName(String backupFileName) {
        this.backupFileName = backupFileName;
    }
}
