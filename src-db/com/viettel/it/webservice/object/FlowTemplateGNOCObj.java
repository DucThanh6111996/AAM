package com.viettel.it.webservice.object;

/**
 * Created by VTN-PTPM-NV55 on 5/23/2019.
 */
public class FlowTemplateGNOCObj {
    private String templateId;
    private String tempFileContent;
//    private int resultCode;
//    private String resultMessage;

    public FlowTemplateGNOCObj() {
    }

    public FlowTemplateGNOCObj(String templateId) {
        this.templateId = templateId;
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

//    public int getResultCode() {
//        return resultCode;
//    }
//
//    public void setResultCode(int resultCode) {
//        this.resultCode = resultCode;
//    }
//
//    public String getResultMessage() {
//        return resultMessage;
//    }
//
//    public void setResultMessage(String resultMessage) {
//        this.resultMessage = resultMessage;
//    }
}
