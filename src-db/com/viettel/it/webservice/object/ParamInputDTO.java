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
public class ParamInputDTO {

    private List<ParamDTO> params;

    public ParamInputDTO() {
    }

    public ParamInputDTO(List<ParamDTO> params) {
        this.params = params;
    }

    public List<ParamDTO> getParams() {
        return params;
    }

    public void setParams(List<ParamDTO> params) {
        this.params = params;
    }
}
