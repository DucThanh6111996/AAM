package com.viettel.bean;

import java.io.Serializable;
import java.util.List;

/**
 * @author quanns2
 */
public class ModuleChecklist implements Serializable {
    private Long processId;
    private String processCode;
    private List<ChecklistAlarm> checklistAlarms;
    private String timeMonitor;
    private String advance;
    private Integer serviceId;

    private String logPath;
    private List<LogError> logErrors;

    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    public String getProcessCode() {
        return processCode;
    }

    public void setProcessCode(String processCode) {
        this.processCode = processCode;
    }

    public List<ChecklistAlarm> getChecklistAlarms() {
        return checklistAlarms;
    }

    public void setChecklistAlarms(List<ChecklistAlarm> checklistAlarms) {
        this.checklistAlarms = checklistAlarms;
    }

    public String getTimeMonitor() {
        return timeMonitor;
    }

    public void setTimeMonitor(String timeMonitor) {
        this.timeMonitor = timeMonitor;
    }

    public String getAdvance() {
        return advance;
    }

    public void setAdvance(String advance) {
        this.advance = advance;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getLogPath() {
        return logPath;
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public List<LogError> getLogErrors() {
        return logErrors;
    }

    public void setLogErrors(List<LogError> logErrors) {
        this.logErrors = logErrors;
    }
}
