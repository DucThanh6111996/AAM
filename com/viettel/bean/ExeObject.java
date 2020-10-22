package com.viettel.bean;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.viettel.model.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;
import java.util.List;

public class ExeObject implements Serializable {
    private static Logger logger = LogManager.getLogger(ExeObject.class);

    protected Action action;

    protected ActionCustomAction customAction;

    protected ActionDetailApp detailApp;
    protected ActionModule actionModule;
    protected Module module; // thông tin về module
    protected List<String> actions;

    protected ActionDetailDatabase actionDatabase;
    protected Collection<ActionDetailDatabase> detailDatabases;
    protected ServiceDatabase serviceDb;
    protected Integer actionDb;
    protected ActionDtFile actionDtFile;

    protected StringBuilder log = new StringBuilder(); // log
    protected Date beginDate; // thơi gian băt đầu
    protected Date endDate; // thời gian kết thúc
    protected String createUser; // người thực hiện
    protected String description; // thông tin cơ bản về modune và lệnh sẽ chạy
    protected Integer runStt; // trạng thái của chương trình : đang chờ thực hiện
    // đang chạy ; kết thúc thành công ; két thúc thất
    // bại ; bị cancel
    protected int orderIndex = 1; // thứ tự thực hiện
//    private int statusAction = Constant.VIEW_ACTION; // hành động cần thực thi
    protected boolean stopProcess = false; // true la dung ; fail la van duoc chay
    protected String ipAddress; // địa chỉ IP
    private String userComment;
    private Integer waitAction;
    private String waitDescription;
    private String waitCause;
    private Long waitId;
    // anhnt2
    private String waitTimeShutdown;
    // 0: time out, 1: Cr fail, 2: retry
    private Integer optionWhenTimeOut;

    // 0: confirm, 1: Ok fail, 2: Shutdown again
    private Integer confirmWhenServerUp;

    // 0: confirm, 1: continue, 2: cancel,  3: shutdown don't turn back
    private Integer userConfirmForShutdown;

    private boolean runCaseRebootShutdown = false;

    protected Multimap<String, String> backupTables = HashMultimap.create();
    protected Multimap<String, String> executeTables = HashMultimap.create();
    protected Multimap<String, String> rollbackTables = HashMultimap.create();

    protected boolean startFlag = false;

    public StringBuilder getLog() {
        return log;
    }

    public void setLog(StringBuilder log) {
        this.log = log;
    }

    public Date getBeginDate() {
        return beginDate;
    }

    public void setBeginDate(Date beginDate) {
        this.beginDate = beginDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getRunStt() {
        return runStt;
    }

    public void setRunStt(Integer runStt) {
        this.runStt = runStt;
    }

    public Module getModule() {
        return module;
    }

    public void setModule(Module module) {
        this.module = module;
    }

    public List<String> getActions() {
        return actions;
    }

    public void setActions(List<String> actions) {
        this.actions = actions;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }
/*
    public int getStatusAction() {
        return statusAction;
    }

    public void setStatusAction(int statusAction) {
        this.statusAction = statusAction;
    }*/

    public boolean isStopProcess() {
        return stopProcess;
    }

    public void setStopProcess(boolean stopProcess) {
        this.stopProcess = stopProcess;
    }

    public String getIpAddress() {
        return ipAddress;
    }

    public void setIpAddress(String ipAddress) {
        this.ipAddress = ipAddress;
    }

    public ActionDetailApp getDetailApp() {
        return detailApp;
    }

    public void setDetailApp(ActionDetailApp detailApp) {
        this.detailApp = detailApp;
    }

    public ActionModule getActionModule() {
        return actionModule;
    }

    public void setActionModule(ActionModule actionModule) {
        this.actionModule = actionModule;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public ActionDetailDatabase getActionDatabase() {
        return actionDatabase;
    }

    public void setActionDatabase(ActionDetailDatabase actionDatabase) {
        this.actionDatabase = actionDatabase;
    }

    public ServiceDatabase getServiceDb() {
        return serviceDb;
    }

    public void setServiceDb(ServiceDatabase serviceDb) {
        this.serviceDb = serviceDb;
    }

    public Integer getActionDb() {
        return actionDb;
    }

    public void setActionDb(Integer actionDb) {
        this.actionDb = actionDb;
    }

    public Multimap<String, String> getBackupTables() {
        return backupTables;
    }

    public void setBackupTables(Multimap<String, String> backupTables) {
        this.backupTables = backupTables;
    }

    public Multimap<String, String> getExecuteTables() {
        return executeTables;
    }

    public void setExecuteTables(Multimap<String, String> executeTables) {
        this.executeTables = executeTables;
    }

    public Multimap<String, String> getRollbackTables() {
        return rollbackTables;
    }

    public void setRollbackTables(Multimap<String, String> rollbackTables) {
        this.rollbackTables = rollbackTables;
    }

    public Collection<ActionDetailDatabase> getDetailDatabases() {
        return detailDatabases;
    }

    public void setDetailDatabases(Collection<ActionDetailDatabase> detailDatabases) {
        this.detailDatabases = detailDatabases;
    }

    public ActionCustomAction getCustomAction() {
        return customAction;
    }

    public void setCustomAction(ActionCustomAction customAction) {
        this.customAction = customAction;
    }

    public ActionDtFile getActionDtFile() {
        return actionDtFile;
    }

    public void setActionDtFile(ActionDtFile actionDtFile) {
        this.actionDtFile = actionDtFile;
    }

    public boolean isStartFlag() {
        return startFlag;
    }

    public void setStartFlag(boolean startFlag) {
        this.startFlag = startFlag;
    }

    public String getDescription() {
        return description;
    }

    public String getUserComment() {
        return userComment;
    }

    public void setUserComment(String userComment) {
        this.userComment = userComment;
    }

    public String getWaitDescription() {
        return waitDescription;
    }

    public void setWaitDescription(String waitDescription) {
        this.waitDescription = waitDescription;
    }

    public Integer getWaitAction() {
        return waitAction;
    }

    public void setWaitAction(Integer waitAction) {
        this.waitAction = waitAction;
    }

    public Long getWaitId() {
        return waitId;
    }

    public void setWaitId(Long waitId) {
        this.waitId = waitId;
    }

    public String getWaitTimeShutdown() {
        return waitTimeShutdown;
    }

    public void setWaitTimeShutdown(String waitTimeShutdown) {
        this.waitTimeShutdown = waitTimeShutdown;
    }

    public Integer getOptionWhenTimeOut() {
        return optionWhenTimeOut;
    }

    public void setOptionWhenTimeOut(Integer optionWhenTimeOut) {
        this.optionWhenTimeOut = optionWhenTimeOut;
    }

    public Integer getConfirmWhenServerUp() {
        return confirmWhenServerUp;
    }

    public void setConfirmWhenServerUp(Integer confirmWhenServerUp) {
        this.confirmWhenServerUp = confirmWhenServerUp;
    }

    public Integer getUserConfirmForShutdown() {
        return userConfirmForShutdown;
    }

    public void setUserConfirmForShutdown(Integer userConfirmForShutdown) {
        this.userConfirmForShutdown = userConfirmForShutdown;
    }

    public boolean isRunCaseRebootShutdown() {
        return runCaseRebootShutdown;
    }

    public void setRunCaseRebootShutdown(boolean runCaseRebootShutdown) {
        this.runCaseRebootShutdown = runCaseRebootShutdown;
    }

    // 22-11-2018 KienPD confirm service dead start
    private Integer confirmWhenServiceDead;

    public Integer getConfirmWhenServiceDead() {
        return confirmWhenServiceDead;
    }

    public void setConfirmWhenServiceDead(Integer confirmWhenServiceDead) {
        this.confirmWhenServiceDead = confirmWhenServiceDead;
    }

    public String getWaitCause() {
        return waitCause;
    }

    public void setWaitCause(String waitCause) {
        this.waitCause = waitCause;
    }
    // 22-11-2018 KienPD confirm service dead end
}
