package com.viettel.it.webservice.object;

/**
 * Created by tuanda38 on 11/21/2017.
 */
public class JsonResponseDTO {

    private String resultCode;
    private String resultMessage;
    private String dataJson;
    private int totalDataJson;

    public String getResultCode() {
        return resultCode;
    }

    public void setResultCode(String resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }

    public int getTotalDataJson() {
        return totalDataJson;
    }

    public void setTotalDataJson(int totalDataJson) {
        this.totalDataJson = totalDataJson;
    }

}
