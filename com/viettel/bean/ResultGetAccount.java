package com.viettel.bean;

import com.viettel.controller.TextUtils;

import java.io.Serializable;

/**
 * quytv7
 */
public class ResultGetAccount implements Serializable {
    private Boolean resultStatus;
    private String resultMessage;
    private String result;

    public Boolean getResultStatus() {
        return resultStatus;
    }

    public void setResultStatus(Boolean resultStatus) {
        this.resultStatus = resultStatus;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        if(resultMessage != null && resultMessage.trim().isEmpty()){
            this.resultMessage = TextUtils.isNullOrEmpty(this.resultMessage) ? resultMessage : (this.resultMessage + ";\n" + resultMessage);
        }else if(resultMessage != null && !resultMessage.trim().isEmpty()){
            this.resultMessage = resultMessage;
        }
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }
}
