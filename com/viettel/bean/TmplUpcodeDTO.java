package com.viettel.bean;

public class TmplUpcodeDTO {
    private int STT;
    private String moduleCode;
    private String moduleName;
    private String upcodePath;
    private String uploadFilePath;
    private String deletePath;
    private String message;
    private String result;


    public TmplUpcodeDTO() {
    }


    public TmplUpcodeDTO(Integer stt,
                         String moduleCode,
                         String moduleName,
                         String upcodePath,
                         String uploadFilePath,
                         String deletePath,
                         String message,
                         String result) {
        this.STT=                                              stt;
        this.moduleCode             =                                       moduleCode;
        this.moduleName             =                                       moduleName;
        this.upcodePath           =                                       upcodePath;
        this.uploadFilePath      =                                       uploadFilePath;
        this.deletePath = deletePath;
        this.message            = message;
        this.result = result;
    }


    public int getStt() {
        return STT;
    }

    public void setStt(int STT) {
        this.STT = STT;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public String getUpcodePath() {
        return upcodePath;
    }

    public void setUpcodePath(String upcodePath) {
        this.upcodePath = upcodePath;
    }

    public String getUploadFilePath() {
        return uploadFilePath;
    }

    public void setUploadFilePath(String uploadFilePath) {
        this.uploadFilePath = uploadFilePath;
    }

    public String getDeletePath() {
        return deletePath;
    }

    public void setDeletePath(String deletePath) {
        this.deletePath = deletePath;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

}
