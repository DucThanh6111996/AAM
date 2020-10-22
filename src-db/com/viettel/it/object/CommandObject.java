/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.object;

import com.viettel.it.model.CommandDetail;
import com.viettel.it.model.Node;

/**
 *
 * @author hienhv4
 */
public class CommandObject {
    private String cmd;
    private String cmdSend;
    private String result;
    private String resultDetail;
    private Node nodeRun;
    private CommandDetail commandDetail;
    private String account;
    private String password;
    
    //Cac truong filter
    private Long filterVersionId;
    private Long filterVendorId;
    private Long filterNodeTypeId;

    public Long getFilterVersionId() {
        return filterVersionId;
    }

    public void setFilterVersionId(Long filterVersionId) {
        this.filterVersionId = filterVersionId;
    }

    public Long getFilterVendorId() {
        return filterVendorId;
    }

    public void setFilterVendorId(Long filterVendorId) {
        this.filterVendorId = filterVendorId;
    }

    public Long getFilterNodeTypeId() {
        return filterNodeTypeId;
    }

    public void setFilterNodeTypeId(Long filterNodeTypeId) {
        this.filterNodeTypeId = filterNodeTypeId;
    }

    public String getAccount() {
        return account;
    }

    public void setAccount(String account) {
        this.account = account;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public CommandDetail getCommandDetail() {
        return commandDetail;
    }

    public void setCommandDetail(CommandDetail commandDetail) {
        this.commandDetail = commandDetail;
    }

    public Node getNodeRun() {
        return nodeRun;
    }

    public void setNodeRun(Node nodeRun) {
        this.nodeRun = nodeRun;
    }

    public String getCmd() {
        return cmd;
    }

    public void setCmd(String cmd) {
        this.cmd = cmd;
    }

    public String getCmdSend() {
        return cmdSend;
    }

    public void setCmdSend(String cmdSend) {
        this.cmdSend = cmdSend;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getResultDetail() {
        return resultDetail;
    }

    public void setResultDetail(String resultDetail) {
        this.resultDetail = resultDetail;
    }
}
