package com.viettel.it.webservice.object;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * Created by anhnt2 on 05/19/2018.
 */
public class AuditAlarmDTO {
    private Long auditId;
    private Long templateId;
    private String auditCode;
    private List<String> domain;
    private Long templateGroupId;
    private String dtName;
    private Long dtId;
    private Date createTime;
    private ArrayList<NodeDTO> nodes = new ArrayList<>();
    private String dtFileContent;
    private String dtFileType;
    private String dtFileName;
    private int result;
    private String resultDetail = "";

    public int getResult() {
        return result;
    }

    public void setResult(int result) {
        this.result = result;
    }

    public String getResultDetail() {
        return resultDetail;
    }

    public void setResultDetail(String resultDetail) {
        this.resultDetail = resultDetail;
    }

    public Long getAuditId() {
        return auditId;
    }

    public void setAuditId(Long auditId) {
        this.auditId = auditId;
    }

    public String getAuditCode() {
        return auditCode;
    }

    public void setAuditCode(String auditCode) {
        this.auditCode = auditCode;
    }

    public Long getTemplateGroupId() {
        return templateGroupId;
    }

    public void setTemplateGroupId(Long templateGroupId) {
        this.templateGroupId = templateGroupId;
    }

    public String getDtName() {
        return dtName;
    }

    public void setDtName(String dtName) {
        this.dtName = dtName;
    }

    public Long getDtId() {
        return dtId;
    }

    public void setDtId(Long dtId) {
        this.dtId = dtId;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public ArrayList<NodeDTO> getNodes() {
        return nodes;
    }

    public void setNodes(ArrayList<NodeDTO> nodes) {
        this.nodes = nodes;
    }

    public String getDtFileContent() {
        return dtFileContent;
    }

    public void setDtFileContent(String dtFileContent) {
        this.dtFileContent = dtFileContent;
    }

    public String getDtFileType() {
        return dtFileType;
    }

    public void setDtFileType(String dtFileType) {
        this.dtFileType = dtFileType;
    }

    public String getDtFileName() {
        return dtFileName;
    }

    public void setDtFileName(String dtFileName) {
        this.dtFileName = dtFileName;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public List<String> getDomain() {
        return domain;
    }

    public void setDomain(List<String> domain) {
        this.domain = domain;
    }
}
