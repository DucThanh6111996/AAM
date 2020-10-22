/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.object;

import com.viettel.it.webservice.object.ParamSOC;

import java.util.List;

/**
 *
 * @author hienhv4
 */
public class MessageItBusObject {

    private Long flowRunId; //ID cua action can chay
    private Long flowRunLogId;
    private String actionName;
    private String runningType; //rescue or normal impact
    private String protocol;
    private String protocolLog;
    private List<CmdObject> lstImpactCmds;
    private List<CmdObject> lstLogCmds;
    private List<Long> lstNodeId;
    private List<Long> lstNodeLogId;
    private List<String> lstPassImpact;
    private List<String> lstUserImpact;
    private List<String> lstPassWriteLog;
    private List<String> lstUserWriteLog;

    private List<CmdNodeObject> impactNodes; // Danh sach thong tin va lenh tac dong cac node
    private List<CmdNodeObject> writeLogNodes; // Danh sach thong tin lenh va
    private Integer numOfThread; // So luong tien trinh ung voi moi node mang
    private List<Long> sidnDatasId; // danh sach id tung ban ghi lay duoc tu database chan cat thue bao
    private List<SidnBccsObj> sidnBccsDatas;

    private ParamSOC paramSOC = null;

    public MessageItBusObject() {
    }

    public Long getFlowRunId() {
        return flowRunId;
    }

    public void setFlowRunId(Long flowRunId) {
        this.flowRunId = flowRunId;
    }

    public Long getFlowRunLogId() {
        return flowRunLogId;
    }

    public void setFlowRunLogId(Long flowRunLogId) {
        this.flowRunLogId = flowRunLogId;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getRunningType() {
        return runningType;
    }

    public void setRunningType(String runningType) {
        this.runningType = runningType;
    }

    public String getProtocolLog() {
        return protocolLog;
    }

    public void setProtocolLog(String protocolLog) {
        this.protocolLog = protocolLog;
    }

    public List<CmdObject> getLstImpactCmds() {
        return lstImpactCmds;
    }

    public void setLstImpactCmds(List<CmdObject> lstImpactCmds) {
        this.lstImpactCmds = lstImpactCmds;
    }

    public List<CmdObject> getLstLogCmds() {
        return lstLogCmds;
    }

    public void setLstLogCmds(List<CmdObject> lstLogCmds) {
        this.lstLogCmds = lstLogCmds;
    }

    public List<Long> getLstNodeId() {
        return lstNodeId;
    }

    public void setLstNodeId(List<Long> lstNodeId) {
        this.lstNodeId = lstNodeId;
    }

    public List<Long> getLstNodeLogId() {
        return lstNodeLogId;
    }

    public void setLstNodeLogId(List<Long> lstNodeLogId) {
        this.lstNodeLogId = lstNodeLogId;
    }

    public List<String> getLstPassImpact() {
        return lstPassImpact;
    }

    public void setLstPassImpact(List<String> lstPassImpact) {
        this.lstPassImpact = lstPassImpact;
    }

    public List<String> getLstUserImpact() {
        return lstUserImpact;
    }

    public void setLstUserImpact(List<String> lstUserImpact) {
        this.lstUserImpact = lstUserImpact;
    }

    public List<String> getLstPassWriteLog() {
        return lstPassWriteLog;
    }

    public void setLstPassWriteLog(List<String> lstPassWriteLog) {
        this.lstPassWriteLog = lstPassWriteLog;
    }

    public List<String> getLstUserWriteLog() {
        return lstUserWriteLog;
    }

    public void setLstUserWriteLog(List<String> lstUserWriteLog) {
        this.lstUserWriteLog = lstUserWriteLog;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public List<CmdNodeObject> getImpactNodes() {
        return impactNodes;
    }

    public void setImpactNodes(List<CmdNodeObject> impactNodes) {
        this.impactNodes = impactNodes;
    }

    public List<CmdNodeObject> getWriteLogNodes() {
        return writeLogNodes;
    }

    public void setWriteLogNodes(List<CmdNodeObject> writeLogNodes) {
        this.writeLogNodes = writeLogNodes;
    }

    public Integer getNumOfThread() {
        return numOfThread;
    }

    public void setNumOfThread(Integer numOfThread) {
        this.numOfThread = numOfThread;
    }

    public List<Long> getSidnDatasId() {
        return sidnDatasId;
    }

    public void setSidnDatasId(List<Long> sidnDatasId) {
        this.sidnDatasId = sidnDatasId;
    }

    public List<SidnBccsObj> getSidnBccsDatas() {
        return sidnBccsDatas;
    }

    public void setSidnBccsDatas(List<SidnBccsObj> sidnBccsDatas) {
        this.sidnBccsDatas = sidnBccsDatas;
    }

    public ParamSOC getParamSOC() {
        return paramSOC;
    }

    public void setParamSOC(ParamSOC paramSOC) {
        this.paramSOC = paramSOC;
    }
}
