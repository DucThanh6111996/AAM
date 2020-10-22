/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.util;

import java.util.AbstractMap;
import java.util.List;

/**
 *
 * @author anhmv6
 */
public class SheetDTO {
    private String title;
    private String subTitle;
    private int startRow;
    private int cellTitleIndex;
    private List dataSheet;
    private String headerPrefix;
    private List<AbstractMap.SimpleEntry<String, String>> headerAlign;

    public SheetDTO() {
        //contructor
    }

    public SheetDTO(String title, List dataSheet) {
        this.title = title;
        this.dataSheet = dataSheet;
    }

    public SheetDTO(String title, String subTitle, int startRow, int cellTitleIndex, List dataSheet, String headerPrefix, List<AbstractMap.SimpleEntry<String, String>> headerAlign) {
        this.title = title;
        this.subTitle = subTitle;
        this.startRow = startRow;
        this.cellTitleIndex = cellTitleIndex;
        this.dataSheet = dataSheet;
        this.headerPrefix = headerPrefix;
        this.headerAlign = headerAlign;
    }


    
    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List getDataSheet() {
        return dataSheet;
    }

    public void setDataSheet(List dataSheet) {
        this.dataSheet = dataSheet;
    }

    public String getHeaderPrefix() {
        return headerPrefix;
    }

    public void setHeaderPrefix(String headerPrefix) {
        this.headerPrefix = headerPrefix;
    }

    public List<AbstractMap.SimpleEntry<String, String>> getHeaderAlign() {
        return headerAlign;
    }

    public void setHeaderAlign(List<AbstractMap.SimpleEntry<String, String>> headerAlign) {
        this.headerAlign = headerAlign;
    }

    public String getSubTitle() {
        return subTitle;
    }

    public void setSubTitle(String subTitle) {
        this.subTitle = subTitle;
    }

    public int getStartRow() {
        return startRow;
    }

    public void setStartRow(int startRow) {
        this.startRow = startRow;
    }

    public int getCellTitleIndex() {
        return cellTitleIndex;
    }

    public void setCellTitleIndex(int cellTitleIndex) {
        this.cellTitleIndex = cellTitleIndex;
    }
    
    
}
