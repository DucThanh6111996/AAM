/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice.object;

/**
 *
 * @author hienhv4
 */
public class MopDetailOutputDTO {
    private int resultCode;
    private String resultMessage;
    private MopDetailDTO mopDetailDTO;

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

    public MopDetailDTO getMopDetailDTO() {
        return mopDetailDTO;
    }

    public void setMopDetailDTO(MopDetailDTO mopDetailDTO) {
        this.mopDetailDTO = mopDetailDTO;
    }
}
