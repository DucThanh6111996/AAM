package com.viettel.bean;

import java.io.Serializable;

/**
 * Created by khiemvk on 10/11/2015.
 */
public class JsonResponseBO implements Serializable {
    private int status; //0: SUCC; 1: ERRO
    private String detailError;
    private String dataJson;
    private byte[] byteDataJson;
    private String receiverTime;
    private String sendTime;
    private int totalDataJson;

    public JsonResponseBO() {
        this.status = 0;
    }

    public JsonResponseBO(int status, String detailError, String dataJson) {
        this.status = status;
        this.detailError = detailError;
        this.dataJson = dataJson;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public String getDetailError() {
        return detailError;
    }

    public void setDetailError(String detailError) {
        this.detailError = detailError;
    }

    public String getDataJson() {
        return dataJson;
    }

    public void setDataJson(String dataJson) {
        this.dataJson = dataJson;
    }

    public String getReceiverTime() {
        return receiverTime;
    }

    public void setReceiverTime(String receiverTime) {
        this.receiverTime = receiverTime;
    }

    public String getSendTime() {
        return sendTime;
    }

    public void setSendTime(String sendTime) {
        this.sendTime = sendTime;
    }

    public int getTotalDataJson() {
        return totalDataJson;
    }

    public void setTotalDataJson(int totalDataJson) {
        this.totalDataJson = totalDataJson;
    }

    public byte[] getByteDataJson() {
        return byteDataJson;
    }

    public void setByteDataJson(byte[] byteDataJson) {
        this.byteDataJson = byteDataJson;
    }
}
