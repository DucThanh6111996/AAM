/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice.object;

/**
 *
 * @author hienhv4
 */
public class NodeDTO {

    private String nodeCode;
    private String nodeIp;

    public NodeDTO() {
    }

    public NodeDTO(String nodeCode, String nodeIp) {
        this.nodeCode = nodeCode;
        this.nodeIp = nodeIp;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }
}
