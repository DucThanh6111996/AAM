package com.viettel.it.webservice.object;

import java.util.List;

/**
 * Created by VTN-PTPM-NV55 on 5/27/2019.
 */
public class ResultGetListTemplatesByProcedure {
    private int resultCode;
    private String resultMessage;
    private Long procedureId;
    private Long workFlowId;
    private List<FlowTemplateGNOCObjDTO> flowTemplatesObj;

    public ResultGetListTemplatesByProcedure() {
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

    public Long getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(Long procedureId) {
        this.procedureId = procedureId;
    }

    public Long getWorkFlowId() {
        return workFlowId;
    }

    public void setWorkFlowId(Long workFlowId) {
        this.workFlowId = workFlowId;
    }

    public List<FlowTemplateGNOCObjDTO> getFlowTemplatesObj() {
        return flowTemplatesObj;
    }

    public void setFlowTemplatesObj(List<FlowTemplateGNOCObjDTO> flowTemplatesObj) {
        this.flowTemplatesObj = flowTemplatesObj;
    }
}
