package com.viettel.it.webservice.object;

import java.util.ArrayList;

/**
 * Created by hanh on 4/18/2017.
 */
public class ParamValsDTO {
    private String message;
    private Long nodeId;
    private String nodeCode;
    private ArrayList<ParamValuesDTO> lstParamValues = new ArrayList<>();

    public ParamValsDTO() {
    }

    public ParamValsDTO(String message, ArrayList<ParamValuesDTO> lstParamValues) {
        this.message = message;
        this.lstParamValues = lstParamValues;
    }


    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public ArrayList<ParamValuesDTO> getLstParamValues() {
        return lstParamValues;
    }

    public void setLstParamValues(ArrayList<ParamValuesDTO> lstParamValues) {
        this.lstParamValues = lstParamValues;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }
}
