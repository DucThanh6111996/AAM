package com.viettel.controller;

import java.io.Serializable;
import java.util.List;

/**
 * @author quanns2
 */
public class ServerChecklist implements Serializable {
    private String moduleName;
    private Integer serviceId;
    private List<ChecklistAlarm> checklistAlarms;
    private String timeMonitor;

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public String getTimeMonitor() {
        return timeMonitor;
    }

    public void setTimeMonitor(String timeMonitor) {
        this.timeMonitor = timeMonitor;
    }

    public List<ChecklistAlarm> getChecklistAlarms() {
        return checklistAlarms;
    }

    public void setChecklistAlarms(List<ChecklistAlarm> checklistAlarms) {
        this.checklistAlarms = checklistAlarms;
    }
}
