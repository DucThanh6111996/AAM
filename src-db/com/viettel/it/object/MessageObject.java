/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.object;

/**
 *
 * @author hienhv4
 */
public class MessageObject {
    private Long flowRunId; //ID cua flow_run_action can chay
    private String username; //username nguoi thuc hien dt
    private Long flowRunLogId;
    private Long actionOfFlowIdStart;
    private Long actionOfFlowIdEnd;
    private String flowRunName;
    private String actionName;
    private int runType; //Chay tac dong hoac chay rollback
    private int errorMode; //1:Gap loi thi rollback, 2:Gap loi thi tam dung
    /*20180628_hoangnd__start*/
    private int runningType; //Chay doc lap (1) hoac chay phu thuoc (2)
    /*20180628_hoangnd__end*/

    private String userImpact;
    private String passImpact;

    public MessageObject(Long flowRunId, String username,
            Long flowRunLogId, String flowRunName, String actionName) {
        this.flowRunId = flowRunId;
        this.username = username;
        this.flowRunLogId = flowRunLogId;
        this.flowRunName = flowRunName;
        this.actionName = actionName;
    }

    public MessageObject(Long flowRunId, String username, String userImpact, String passImpact,
                         Long flowRunLogId, String flowRunName, String actionName) {
        this.flowRunId = flowRunId;
        this.username = username;
        this.userImpact = userImpact;
        this.passImpact = passImpact;
        this.flowRunLogId = flowRunLogId;
        this.flowRunName = flowRunName;
        this.actionName = actionName;
    }

    public int getErrorMode() {
        return errorMode;
    }

    public void setErrorMode(int errorMode) {
        this.errorMode = errorMode;
    }

    public Long getActionOfFlowIdStart() {
        return actionOfFlowIdStart;
    }

    public void setActionOfFlowIdStart(Long actionOfFlowIdStart) {
        this.actionOfFlowIdStart = actionOfFlowIdStart;
    }

    public Long getActionOfFlowIdEnd() {
        return actionOfFlowIdEnd;
    }

    public void setActionOfFlowIdEnd(Long actionOfFlowIdEnd) {
        this.actionOfFlowIdEnd = actionOfFlowIdEnd;
    }

    public int getRunType() {
        return runType;
    }

    public void setRunType(int runType) {
        this.runType = runType;
    }
    
    public String getFlowRunName() {
        return flowRunName;
    }

    public void setFlowRunName(String flowRunName) {
        this.flowRunName = flowRunName;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public Long getFlowRunLogId() {
        return flowRunLogId;
    }

    public void setFlowRunLogId(Long flowRunLogId) {
        this.flowRunLogId = flowRunLogId;
    }

    public Long getFlowRunId() {
        return flowRunId;
    }

    public void setFlowRunId(Long flowRunId) {
        this.flowRunId = flowRunId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getUserImpact() {
        return userImpact;
    }

    public void setUserImpact(String userImpact) {
        this.userImpact = userImpact;
    }

    public String getPassImpact() {
        return passImpact;
    }

    public void setPassImpact(String passImpact) {
        this.passImpact = passImpact;
    }

    /*20180628_hoangnd__start*/
    public int getRunningType() {
        return runningType;
    }

    public void setRunningType(int runningType) {
        this.runningType = runningType;
    }
    /*20180628_hoangnd__end*/
}
