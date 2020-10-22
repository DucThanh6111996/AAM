package com.viettel.it.webservice.object;

import java.util.List;

/**
 * Created by VTN-PTPM-NV55 on 5/28/2019.
 */
public class ResultCreateDtByFileInput {
    private int resultCode;
    private String resultMessage;
    private List<DtObjDTO> listDtDelete;
    private List<DtObjDTO> listDtCreate;
//    private List<FlowTemplateGNOCObj> flowTemplatesObj;

    public ResultCreateDtByFileInput() {
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

    public List<DtObjDTO> getListDtDelete() {
        return listDtDelete;
    }

    public void setListDtDelete(List<DtObjDTO> listDtDelete) {
        this.listDtDelete = listDtDelete;
    }

    public List<DtObjDTO> getListDtCreate() {
        return listDtCreate;
    }

    public void setListDtCreate(List<DtObjDTO> listDtCreate) {
        this.listDtCreate = listDtCreate;
    }

//    public List<FlowTemplateGNOCObj> getFlowTemplatesObj() {
//        return flowTemplatesObj;
//    }
//
//    public void setFlowTemplatesObj(List<FlowTemplateGNOCObj> flowTemplatesObj) {
//        this.flowTemplatesObj = flowTemplatesObj;
//    }
}
