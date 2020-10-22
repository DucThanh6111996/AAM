package com.viettel.it.model;

import java.io.Serializable;

/**
 * @author quanns2
 */
public class FlowActionResult implements Serializable {
    private String username;
    private String groupActionName;
    private Integer result;
    private String flowRunName;
    private String nodeIp;
    private String nodeCode;
    private String name;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getGroupActionName() {
        return groupActionName;
    }

    public void setGroupActionName(String groupActionName) {
        this.groupActionName = groupActionName;
    }

    public Integer getResult() {
        return result;
    }

    public void setResult(Integer result) {
        this.result = result;
    }

    public String getFlowRunName() {
        return flowRunName;
    }

    public void setFlowRunName(String flowRunName) {
        this.flowRunName = flowRunName;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
