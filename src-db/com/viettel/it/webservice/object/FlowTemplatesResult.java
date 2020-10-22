/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice.object;

import java.util.ArrayList;

/**
 *
 * @author hienhv4
 */
public class FlowTemplatesResult {

    private int resultCode;
    private String resultMessage;
    private String requestId;
    private ArrayList<FlowTemplateObj> lstFlowTemplate = new ArrayList<>();

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

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

    public ArrayList<FlowTemplateObj> getLstFlowTemplate() {
        return lstFlowTemplate;
    }

    public void setLstFlowTemplate(ArrayList<FlowTemplateObj> lstFlowTemplate) {
        this.lstFlowTemplate = lstFlowTemplate;
    }
}
