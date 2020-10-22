package com.viettel.bean;

public class MopDatabaseDTO {
    private Long actionOrder;
    private Long appDbId;
    private String dbMap;
    private String scriptBackup;
    private String timeOutBackup;
    private String scriptExecute;
    private String timeOutImpact;
    private String rollbackFile;
    private String timeOutRollback;
    private Integer kbGroup = 1;
    private Long type = 0L;
    private Long typeImport = 1L;
    private Integer testbedMode = 0;
    private String message;
    private String result;

    public MopDatabaseDTO() {
    }


    public MopDatabaseDTO(Long actionOrder,
                          Long appDbId,
                          String dbMap,
                          String scriptBackup,
                          String timeOutBackup,
                          String scriptExecute,
                          String timeOutImpact,
                          String rollbackFile,
                          String timeOutRollback,
                          String message,
                          String result) {
        this.actionOrder = actionOrder;
        this.appDbId = appDbId;
        this.dbMap = dbMap;
        this.scriptBackup = scriptBackup;
        this.timeOutBackup = timeOutBackup;
        this.scriptExecute = scriptExecute;
        this.timeOutImpact = timeOutImpact;
        this.rollbackFile = rollbackFile;
        this.timeOutRollback = timeOutRollback;
        this.message = message;
        this.result = result;
    }


    public Long getActionOrder() {
        return actionOrder;
    }

    public void setActionOrder(Long actionOrder) {
        this.actionOrder = actionOrder;
    }

    public Long getAppDbId() {
        return appDbId;
    }

    public void setAppDbId(Long appDbId) {
        this.appDbId = appDbId;
    }

    public String getDbMap() {
        return dbMap;
    }

    public void setDbMap(String dbMap) {
        this.dbMap = dbMap;
    }

    public String getScriptBackup() {
        return scriptBackup;
    }

    public void setScriptBackup(String scriptBackup) {
        this.scriptBackup = scriptBackup;
    }

    public String getTimeOutBackup() {
        return timeOutBackup;
    }

    public void setTimeOutBackup(String timeOutBackup) {
        this.timeOutBackup = timeOutBackup;
    }

    public String getScriptExecute() {
        return scriptExecute;
    }

    public void setScriptExecute(String scriptExecute) {
        this.scriptExecute = scriptExecute;
    }

    public String getTimeOutImpact() {
        return timeOutImpact;
    }

    public void setTimeOutImpact(String timeOutImpact) {
        this.timeOutImpact = timeOutImpact;
    }

    public String getRollbackFile() {
        return rollbackFile;
    }

    public void setRollbackFile(String rollbackFile) {
        this.rollbackFile = rollbackFile;
    }

    public String getTimeOutRollback() {
        return timeOutRollback;
    }

    public void setTimeOutRollback(String timeOutRollback) {
        this.timeOutRollback = timeOutRollback;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public Integer getKbGroup() {
        return kbGroup;
    }

    public Long getType() {
        return type;
    }

    public Long getTypeImport() {
        return typeImport;
    }

    public Integer getTestbedMode() {
        return testbedMode;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
