package com.viettel.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author quanns2
 */
public class CodeChange implements Serializable {
    private String upcodeDir;
    private String zipCodeFile;
    private List<String> changeFiles;
    private List<String> deleteFiles;
    private boolean isDir;

    private List<String> excludeChangeFiles;
    private List<String> excludeDeleteFiles;

    public String getUpcodeDir() {
        return upcodeDir;
    }

    public void setUpcodeDir(String upcodeDir) {
        this.upcodeDir = upcodeDir;
    }

    public String getZipCodeFile() {
        return zipCodeFile;
    }

    public void setZipCodeFile(String zipCodeFile) {
        this.zipCodeFile = zipCodeFile;
    }

    public List<String> getChangeFiles() {
        return changeFiles;
    }

    public void setChangeFiles(List<String> changeFiles) {
        this.changeFiles = changeFiles;
    }

    public List<String> getDeleteFiles() {
        return deleteFiles;
    }

    public void setDeleteFiles(List<String> deleteFiles) {
        this.deleteFiles = deleteFiles;
    }

    public boolean isDir() {
        return isDir;
    }

    public void setDir(boolean dir) {
        isDir = dir;
    }

    public List<String> getExcludeChangeFiles() {
        return excludeChangeFiles;
    }

    public void setExcludeChangeFiles(List<String> excludeChangeFiles) {
        this.excludeChangeFiles = excludeChangeFiles;
    }

    public List<String> getExcludeDeleteFiles() {
        return excludeDeleteFiles;
    }

    public void setExcludeDeleteFiles(List<String> excludeDeleteFiles) {
        this.excludeDeleteFiles = excludeDeleteFiles;
    }
}
