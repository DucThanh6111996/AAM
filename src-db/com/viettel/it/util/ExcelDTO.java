/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.util;

import java.util.List;

/**
 *
 * @author anhmv6
 */
public class ExcelDTO {

    private List<SheetDTO> lstDataSheel;

    public ExcelDTO() {
        //contructor
    }

    public ExcelDTO(List<SheetDTO> lstDataSheel) {
        this.lstDataSheel = lstDataSheel;
    }

    public List<SheetDTO> getLstDataSheel() {
        return lstDataSheel;
    }

    public void setLstDataSheel(List<SheetDTO> lstDataSheel) {
        this.lstDataSheel = lstDataSheel;
    }

    
}
