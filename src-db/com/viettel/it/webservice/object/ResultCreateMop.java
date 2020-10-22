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
public class ResultCreateMop {

    private int resultCode;
    private String resultMessage;
    private String requestId;
    private Long flowRunId;

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public Long getFlowRunId() {
        return flowRunId;
    }

    public void setFlowRunId(Long flowRunId) {
        this.flowRunId = flowRunId;
    }
}
