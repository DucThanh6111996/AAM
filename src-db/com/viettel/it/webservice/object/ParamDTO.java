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
public class ParamDTO {

    private String paramCode;
    private String paramValue;

    public String getParamCode() {
        return paramCode;
    }

    public void setParamCode(String paramCode) {
        this.paramCode = paramCode;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public ParamDTO(String paramCode, String paramValue) {
        this.paramCode = paramCode;
        this.paramValue = paramValue;
    }

    public ParamDTO() {
    }
}
