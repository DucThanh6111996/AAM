package com.viettel.it.webservice.object;

import java.util.List;

/**
 * Created by VTN-PTPM-NV55 on 5/24/2019.
 */
public class DtObjDTO {
    private String deleteDtId;
    private String dtId;
    private String dtName;
    private String templateId;
    private String tempFileContent;
    private List<NodeDTO> listNode;
    private int resultCode;
    private String resultMessage;

    public DtObjDTO() {
    }

    public DtObjDTO(String dtId) {
        this.dtId = dtId;
    }

    public String getDeleteDtId() {
        return deleteDtId;
    }

    public void setDeleteDtId(String deleteDtId) {
        this.deleteDtId = deleteDtId;
    }

    public String getDtId() {
        return dtId;
    }

    public void setDtId(String dtId) {
        this.dtId = dtId;
    }

    public String getDtName() {
        return dtName;
    }

    public void setDtName(String dtName) {
        this.dtName = dtName;
    }

    public String getTemplateId() {
        return templateId;
    }

    public void setTemplateId(String templateId) {
        this.templateId = templateId;
    }

    public String getTempFileContent() {
        return tempFileContent;
    }

    public void setTempFileContent(String tempFileContent) {
        this.tempFileContent = tempFileContent;
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

    public List<NodeDTO> getListNode() {
        return listNode;
    }

    public void setListNode(List<NodeDTO> listNode) {
        this.listNode = listNode;
    }
}
