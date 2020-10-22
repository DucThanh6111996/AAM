package com.viettel.bean;

import com.google.common.collect.Multimap;
//import com.mchange.v2.collection.MapEntry;
import com.viettel.bean.MapEntry;
import com.viettel.model.*;
import org.apache.commons.lang3.mutable.MutableInt;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Date;
import java.util.List;
import java.util.Map;

/**
 * @author quanns2
 */
public class ChecklistInfo implements Serializable {
    private static Logger logger = LogManager.getLogger(ChecklistInfo.class);

    private Action action;
    private ActionHistory history;
//    private MutableInt currentStep = new MutableInt();
    private MapEntry currentStep = new MapEntry(0, 0);
    private Map<MapEntry, Integer> stepResult;
    private Integer exeType;
    private Long runId;
    private List<Integer> kbGroups;
//    private List<SelectItem> runSteps;

    private String resourceDir;
    private String reasonRollback;
    private Date startTime;
    private Date endTime;
    private String username;

    //Verify
    private List<ExeObject> checkApps;
    private List<ExeObject> checkDbs;
    private Multimap<Module, Checklist> checklistApps;
    private Multimap<MonitorDatabase, QueueChecklist> checklistDbs;

    //Tac dong
    private Multimap<Module, Checklist> cklAppBefore;
//    private Multimap<Module, Checklist> cklAppMain;
    private Multimap<Module, Checklist> cklAppAfter;
//    private Multimap<Module, Checklist> cklAppRollback;
    private Map<MapEntry, Multimap<Module, Checklist>> newCklAppMain;
    private Map<MapEntry, Multimap<Module, Checklist>> newCklAppRollback;

    private Multimap<MonitorDatabase, QueueChecklist> cklDbBefore;
    private Multimap<MonitorDatabase, QueueChecklist> cklDbMain;
    private Multimap<MonitorDatabase, QueueChecklist> cklDbAfter;
    private Multimap<MonitorDatabase, QueueChecklist> cklDbRollback;

    private Map<MapEntry, KpiServerSetting> kpiServerSettingMap;


/*    private List<ExeObject> stopObjects;
    private List<ExeObject> backupObjects;
    private List<ExeObject> startObjects;
    private List<ExeObject> upcodeStartObjects;
    private List<ExeObject> restartObjects;
    private List<ExeObject> clearCacheObjects;
    private List<ExeObject> upcodeObjects;
    private List<ExeObject> checkStatusObjects;
    private List<ExeObject> executeDbObjects;
    private List<ExeObject> backupDbObjects;*/

    private List<ExeObject> checkVersionAppObjects;

    /*private List<ExeObject> rollbackStopObjects;
    private List<ExeObject> rollbackCodeObjects;
    private List<ExeObject> rollbackDbObjects;
    private List<ExeObject> rollbackStartObjects;
    private List<ExeObject> rollbackRestartObjects;
    private List<ExeObject> rollbackcodeStartObjects;
    private List<ExeObject> rollbackClearCacheObjects;*/

    private Multimap<MapEntry, ExeObject> impactObjects;
    private Multimap<MapEntry, ExeObject> rollbackObjects;
    private Multimap<MapEntry, ExeObject> customExeObjectMultimap;
    private Multimap<MapEntry, ExeObject> rollbackCustomExeObjectMultimap;
    private List<ExeObject> waitingActions;
    //20181023_tudn_start load pass security
    private String userTD;

    public String getUserTD() {
        return userTD;
    }

    public void setUserTD(String userTD) {
        this.userTD = userTD;
    }
    //20181023_tudn_start load pass security

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Multimap<Module, Checklist> getChecklistApps() {
        return checklistApps;
    }

    public void setChecklistApps(Multimap<Module, Checklist> checklistApps) {
        this.checklistApps = checklistApps;
    }

    public Multimap<MonitorDatabase, QueueChecklist> getChecklistDbs() {
        return checklistDbs;
    }

    public void setChecklistDbs(Multimap<MonitorDatabase, QueueChecklist> checklistDbs) {
        this.checklistDbs = checklistDbs;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public List<ExeObject> getCheckApps() {
        return checkApps;
    }

    public void setCheckApps(List<ExeObject> checkApps) {
        this.checkApps = checkApps;
    }

    public List<ExeObject> getCheckDbs() {
        return checkDbs;
    }

    public void setCheckDbs(List<ExeObject> checkDbs) {
        this.checkDbs = checkDbs;
    }

    public Multimap<Module, Checklist> getCklAppBefore() {
        return cklAppBefore;
    }

    public void setCklAppBefore(Multimap<Module, Checklist> cklAppBefore) {
        this.cklAppBefore = cklAppBefore;
    }

    /*public Multimap<Module, Checklist> getCklAppMain() {
        return cklAppMain;
    }

    public void setCklAppMain(Multimap<Module, Checklist> cklAppMain) {
        this.cklAppMain = cklAppMain;
    }*/

    public Multimap<Module, Checklist> getCklAppAfter() {
        return cklAppAfter;
    }

    public void setCklAppAfter(Multimap<Module, Checklist> cklAppAfter) {
        this.cklAppAfter = cklAppAfter;
    }

    /*public Multimap<Module, Checklist> getCklAppRollback() {
        return cklAppRollback;
    }

    public void setCklAppRollback(Multimap<Module, Checklist> cklAppRollback) {
        this.cklAppRollback = cklAppRollback;
    }*/

    public Map<MapEntry, Multimap<Module, Checklist>> getNewCklAppRollback() {
        return newCklAppRollback;
    }

    public void setNewCklAppRollback(Map<MapEntry, Multimap<Module, Checklist>> newCklAppRollback) {
        this.newCklAppRollback = newCklAppRollback;
    }

    public Multimap<MonitorDatabase, QueueChecklist> getCklDbBefore() {
        return cklDbBefore;
    }

    public void setCklDbBefore(Multimap<MonitorDatabase, QueueChecklist> cklDbBefore) {
        this.cklDbBefore = cklDbBefore;
    }

    public Multimap<MonitorDatabase, QueueChecklist> getCklDbMain() {
        return cklDbMain;
    }

    public void setCklDbMain(Multimap<MonitorDatabase, QueueChecklist> cklDbMain) {
        this.cklDbMain = cklDbMain;
    }

    public Multimap<MonitorDatabase, QueueChecklist> getCklDbAfter() {
        return cklDbAfter;
    }

    public void setCklDbAfter(Multimap<MonitorDatabase, QueueChecklist> cklDbAfter) {
        this.cklDbAfter = cklDbAfter;
    }

    public Multimap<MonitorDatabase, QueueChecklist> getCklDbRollback() {
        return cklDbRollback;
    }

    public void setCklDbRollback(Multimap<MonitorDatabase, QueueChecklist> cklDbRollback) {
        this.cklDbRollback = cklDbRollback;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public ActionHistory getHistory() {
        return history;
    }

    public void setHistory(ActionHistory history) {
        this.history = history;
    }

    public MapEntry getCurrentStep() {
        return currentStep;
    }

    public void setCurrentStep(MapEntry currentStep) {
        this.currentStep = currentStep;
    }

    public Map<MapEntry, Integer> getStepResult() {
        return stepResult;
    }

    public void setStepResult(Map<MapEntry, Integer> stepResult) {
        this.stepResult = stepResult;
    }

    public String getResourceDir() {
        return resourceDir;
    }

    public void setResourceDir(String resourceDir) {
        this.resourceDir = resourceDir;
    }

    public String getReasonRollback() {
        return reasonRollback;
    }

    public void setReasonRollback(String reasonRollback) {
        this.reasonRollback = reasonRollback;
    }

    /*public List<ExeObject> getStopObjects() {
        return stopObjects;
    }

    public void setStopObjects(List<ExeObject> stopObjects) {
        this.stopObjects = stopObjects;
    }

    public List<ExeObject> getBackupObjects() {
        return backupObjects;
    }

    public void setBackupObjects(List<ExeObject> backupObjects) {
        this.backupObjects = backupObjects;
    }

    public List<ExeObject> getStartObjects() {
        return startObjects;
    }

    public void setStartObjects(List<ExeObject> startObjects) {
        this.startObjects = startObjects;
    }

    public List<ExeObject> getUpcodeStartObjects() {
        return upcodeStartObjects;
    }

    public void setUpcodeStartObjects(List<ExeObject> upcodeStartObjects) {
        this.upcodeStartObjects = upcodeStartObjects;
    }

    public List<ExeObject> getRestartObjects() {
        return restartObjects;
    }

    public void setRestartObjects(List<ExeObject> restartObjects) {
        this.restartObjects = restartObjects;
    }

    public List<ExeObject> getClearCacheObjects() {
        return clearCacheObjects;
    }

    public void setClearCacheObjects(List<ExeObject> clearCacheObjects) {
        this.clearCacheObjects = clearCacheObjects;
    }

    public List<ExeObject> getUpcodeObjects() {
        return upcodeObjects;
    }

    public void setUpcodeObjects(List<ExeObject> upcodeObjects) {
        this.upcodeObjects = upcodeObjects;
    }

    public List<ExeObject> getCheckStatusObjects() {
        return checkStatusObjects;
    }

    public void setCheckStatusObjects(List<ExeObject> checkStatusObjects) {
        this.checkStatusObjects = checkStatusObjects;
    }

    public List<ExeObject> getCheckVersionAppObjects() {
        return checkVersionAppObjects;
    }

    public void setCheckVersionAppObjects(List<ExeObject> checkVersionAppObjects) {
        this.checkVersionAppObjects = checkVersionAppObjects;
    }

    public List<ExeObject> getExecuteDbObjects() {
        return executeDbObjects;
    }

    public void setExecuteDbObjects(List<ExeObject> executeDbObjects) {
        this.executeDbObjects = executeDbObjects;
    }

    public List<ExeObject> getBackupDbObjects() {
        return backupDbObjects;
    }

    public void setBackupDbObjects(List<ExeObject> backupDbObjects) {
        this.backupDbObjects = backupDbObjects;
    }
*/
    /*public List<ExeObject> getRollbackStopObjects() {
        return rollbackStopObjects;
    }

    public void setRollbackStopObjects(List<ExeObject> rollbackStopObjects) {
        this.rollbackStopObjects = rollbackStopObjects;
    }

    public List<ExeObject> getRollbackCodeObjects() {
        return rollbackCodeObjects;
    }

    public void setRollbackCodeObjects(List<ExeObject> rollbackCodeObjects) {
        this.rollbackCodeObjects = rollbackCodeObjects;
    }

    public List<ExeObject> getRollbackDbObjects() {
        return rollbackDbObjects;
    }

    public void setRollbackDbObjects(List<ExeObject> rollbackDbObjects) {
        this.rollbackDbObjects = rollbackDbObjects;
    }

    public List<ExeObject> getRollbackStartObjects() {
        return rollbackStartObjects;
    }

    public void setRollbackStartObjects(List<ExeObject> rollbackStartObjects) {
        this.rollbackStartObjects = rollbackStartObjects;
    }

    public List<ExeObject> getRollbackRestartObjects() {
        return rollbackRestartObjects;
    }

    public void setRollbackRestartObjects(List<ExeObject> rollbackRestartObjects) {
        this.rollbackRestartObjects = rollbackRestartObjects;
    }

    public List<ExeObject> getRollbackcodeStartObjects() {
        return rollbackcodeStartObjects;
    }

    public void setRollbackcodeStartObjects(List<ExeObject> rollbackcodeStartObjects) {
        this.rollbackcodeStartObjects = rollbackcodeStartObjects;
    }

    public List<ExeObject> getRollbackClearCacheObjects() {
        return rollbackClearCacheObjects;
    }

    public void setRollbackClearCacheObjects(List<ExeObject> rollbackClearCacheObjects) {
        this.rollbackClearCacheObjects = rollbackClearCacheObjects;
    }*/

    public Multimap<MapEntry, ExeObject> getImpactObjects() {
        return impactObjects;
    }

    public void setImpactObjects(Multimap<MapEntry, ExeObject> impactObjects) {
        this.impactObjects = impactObjects;
    }

    public Multimap<MapEntry, ExeObject> getRollbackObjects() {
        return rollbackObjects;
    }

    public void setRollbackObjects(Multimap<MapEntry, ExeObject> rollbackObjects) {
        this.rollbackObjects = rollbackObjects;
    }

    public Multimap<MapEntry, ExeObject> getCustomExeObjectMultimap() {
        return customExeObjectMultimap;
    }

    public void setCustomExeObjectMultimap(Multimap<MapEntry, ExeObject> customExeObjectMultimap) {
        this.customExeObjectMultimap = customExeObjectMultimap;
    }

    public Multimap<MapEntry, ExeObject> getRollbackCustomExeObjectMultimap() {
        return rollbackCustomExeObjectMultimap;
    }

    public void setRollbackCustomExeObjectMultimap(Multimap<MapEntry, ExeObject> rollbackCustomExeObjectMultimap) {
        this.rollbackCustomExeObjectMultimap = rollbackCustomExeObjectMultimap;
    }

    public List<ExeObject> getWaitingActions() {
        return waitingActions;
    }

    public void setWaitingActions(List<ExeObject> waitingActions) {
        this.waitingActions = waitingActions;
    }

    public Integer getExeType() {
        return exeType;
    }

    public void setExeType(Integer exeType) {
        this.exeType = exeType;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public List<Integer> getKbGroups() {
        return kbGroups;
    }

    public void setKbGroups(List<Integer> kbGroups) {
        this.kbGroups = kbGroups;
    }

    public Map<MapEntry, Multimap<Module, Checklist>> getNewCklAppMain() {
        return newCklAppMain;
    }

    public void setNewCklAppMain(Map<MapEntry, Multimap<Module, Checklist>> newCklAppMain) {
        this.newCklAppMain = newCklAppMain;
    }

    public Map<MapEntry, KpiServerSetting> getKpiServerSettingMap() {
        return kpiServerSettingMap;
    }

    public void setKpiServerSettingMap(Map<MapEntry, KpiServerSetting> kpiServerSettingMap) {
        this.kpiServerSettingMap = kpiServerSettingMap;
    }
}
