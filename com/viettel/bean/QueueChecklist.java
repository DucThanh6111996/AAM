package com.viettel.bean;

import java.io.Serializable;

/**
 * @author quanns2
 */
public class QueueChecklist implements Serializable {
    private String queueCode;
    private String query;
    private String advance;
    private String timeMonitor;
    private Integer serviceId;
    private Long queueId;
    private Long qltnDbId;
    private Long appId;

    private ChecklistResult result;
    private MonitorDatabase monitorDatabase;

    /*20181119_hoangnd_save all step_start*/
    private Long actionDbChecklistId;
    private String checklistType;

    private String statusBefore;
    private String statusImpact;
    private String statusAfter;
    private String statusRollback;
    private String resultBefore;
    private String resultImpact;
    private String resultAfter;
    private String resultRollback;
    private String limitedBefore;
    private String limitedImpact;
    private String limitedAfter;
    private String limitedRollback;
    /*20181119_hoangnd_save all step_end*/

    public String getQueueCode() {
        return queueCode;
    }

    public void setQueueCode(String queueCode) {
        this.queueCode = queueCode;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public String getAdvance() {
        return advance;
    }

    public void setAdvance(String advance) {
        this.advance = advance;
    }

    public String getTimeMonitor() {
        return timeMonitor;
    }

    public void setTimeMonitor(String timeMonitor) {
        this.timeMonitor = timeMonitor;
    }

    public Integer getServiceId() {
        return serviceId;
    }

    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    public Long getQueueId() {
        return queueId;
    }

    public void setQueueId(Long queueId) {
        this.queueId = queueId;
    }

    public Long getQltnDbId() {
        return qltnDbId;
    }

    public void setQltnDbId(Long qltnDbId) {
        this.qltnDbId = qltnDbId;
    }

    public Long getAppId() {
        return appId;
    }

    public void setAppId(Long appId) {
        this.appId = appId;
    }

    public ChecklistResult getResult() {
        return result;
    }

    public void setResult(ChecklistResult result) {
        this.result = result;
    }

    public MonitorDatabase getMonitorDatabase() {
        return monitorDatabase;
    }

    public void setMonitorDatabase(MonitorDatabase monitorDatabase) {
        this.monitorDatabase = monitorDatabase;
    }

    /*20181119_hoangnd_save all step_start*/
    public Long getActionDbChecklistId() {
        return actionDbChecklistId;
    }

    public void setActionDbChecklistId(Long actionDbChecklistId) {
        this.actionDbChecklistId = actionDbChecklistId;
    }

    public String getChecklistType() {
        return checklistType;
    }

    public void setChecklistType(String checklistType) {
        this.checklistType = checklistType;
    }

    public String getStatusBefore() {
        return statusBefore;
    }

    public void setStatusBefore(String statusBefore) {
        this.statusBefore = statusBefore;
    }

    public String getStatusImpact() {
        return statusImpact;
    }

    public void setStatusImpact(String statusImpact) {
        this.statusImpact = statusImpact;
    }

    public String getStatusAfter() {
        return statusAfter;
    }

    public void setStatusAfter(String statusAfter) {
        this.statusAfter = statusAfter;
    }

    public String getStatusRollback() {
        return statusRollback;
    }

    public void setStatusRollback(String statusRollback) {
        this.statusRollback = statusRollback;
    }

    public String getResultBefore() {
        return resultBefore;
    }

    public void setResultBefore(String resultBefore) {
        this.resultBefore = resultBefore;
    }

    public String getResultImpact() {
        return resultImpact;
    }

    public void setResultImpact(String resultImpact) {
        this.resultImpact = resultImpact;
    }

    public String getResultAfter() {
        return resultAfter;
    }

    public void setResultAfter(String resultAfter) {
        this.resultAfter = resultAfter;
    }

    public String getResultRollback() {
        return resultRollback;
    }

    public void setResultRollback(String resultRollback) {
        this.resultRollback = resultRollback;
    }

    public String getLimitedBefore() {
        return limitedBefore;
    }

    public void setLimitedBefore(String limitedBefore) {
        this.limitedBefore = limitedBefore;
    }

    public String getLimitedImpact() {
        return limitedImpact;
    }

    public void setLimitedImpact(String limitedImpact) {
        this.limitedImpact = limitedImpact;
    }

    public String getLimitedAfter() {
        return limitedAfter;
    }

    public void setLimitedAfter(String limitedAfter) {
        this.limitedAfter = limitedAfter;
    }

    public String getLimitedRollback() {
        return limitedRollback;
    }

    public void setLimitedRollback(String limitedRollback) {
        this.limitedRollback = limitedRollback;
    }
    /*20181119_hoangnd_save all step_end*/
}
