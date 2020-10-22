/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice.object;

import java.util.ArrayList;

/**
 *
 * @author hienhv4
 */
public class MopFileOutputDTO {
    private int resultCode;
    private String resultMessage;
    private ArrayList<NodeDTO> nodes = new ArrayList<>();
    private String mopFileContent;
    private String mopFileType;
    private String mopFileName;
    
    private String kpiFileContent;
    private String kpiFileType;
    private String kpiFileName;
    
    public String getKpiFileContent() {
        return kpiFileContent;
    }

    public void setKpiFileContent(String kpiFileContent) {
        this.kpiFileContent = kpiFileContent;
    }

    public String getKpiFileType() {
        return kpiFileType;
    }

    public void setKpiFileType(String kpiFileType) {
        this.kpiFileType = kpiFileType;
    }

    public String getKpiFileName() {
        return kpiFileName;
    }

    public void setKpiFileName(String kpiFileName) {
        this.kpiFileName = kpiFileName;
    }

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

    public ArrayList<NodeDTO> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<NodeDTO> nodes) {
        this.nodes = nodes;
    }

    public String getMopFileContent() {
        return mopFileContent;
    }

    public void setMopFileContent(String mopFileContent) {
        this.mopFileContent = mopFileContent;
    }

    public String getMopFileType() {
        return mopFileType;
    }

    public void setMopFileType(String mopFileType) {
        this.mopFileType = mopFileType;
    }

    public String getMopFileName() {
        return mopFileName;
    }

    public void setMopFileName(String mopFileName) {
        this.mopFileName = mopFileName;
    }
}
