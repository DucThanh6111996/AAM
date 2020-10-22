package com.viettel.it.webservice.object;

import java.util.ArrayList;

/**
 * Created by hanh on 4/20/2017.
 */
public class ResultFlowTemplatesDTO {

    private String message;
    private ArrayList<FlowTemplatesDTO> lstFlowTemplate = new ArrayList<>();

    public ResultFlowTemplatesDTO() {
    }

    public ResultFlowTemplatesDTO(ArrayList<FlowTemplatesDTO> lstFlowTemplate) {
        this.lstFlowTemplate = lstFlowTemplate;
    }

    public ArrayList<FlowTemplatesDTO> getLstFlowTemplate() {
        return lstFlowTemplate;
    }

    public void setLstFlowTemplate(ArrayList<FlowTemplatesDTO> lstFlowTemplate) {
        this.lstFlowTemplate = lstFlowTemplate;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
