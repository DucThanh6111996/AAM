/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice.object;

import java.util.ArrayList;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author hienhv4
 */
@XmlRootElement
public class MopDetailDTO {
    private String mopId;
    private String mopName;
    private String templateName;
    private String createTime;
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

    public String getMopId() {
        return mopId;
    }

    public void setMopId(String mopId) {
        this.mopId = mopId;
    }

    public String getMopName() {
        return mopName;
    }

    public void setMopName(String mopName) {
        this.mopName = mopName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }

    @XmlElement
    public ArrayList<NodeDTO> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<NodeDTO> nodes) {
        this.nodes = nodes;
    }
}
