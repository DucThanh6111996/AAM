/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice.object;

import java.util.List;

/**
 *
 * @author hienhv4
 */
public class MopOutputDTO {
    private int resultCode;
    private String resultMessage;
    private List<MopDTO> mops;

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

    public List<MopDTO> getMops() {
        return mops;
    }

    public void setMops(List<MopDTO> mops) {
        this.mops = mops;
    }
}
