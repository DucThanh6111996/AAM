package com.viettel.it.webservice.object;

import java.util.ArrayList;
import java.util.Date;

/**
 * Created by quytv7 on 3/21/2018.
 */
public class TicketAlarmDTO {
    //<editor-fold defaultstate="collapsed" desc="Param">
    private Long ticketId;
    private Long alarmId;
    private String ticketCode;
    private String alarmDomain;
    private Long templateId;
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
    private Long typeRunMop;
    private String countryCode;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Get-Set">
    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public Long getTypeRunMop() {
        return typeRunMop;
    }

    public void setTypeRunMop(Long typeRunMop) {
        this.typeRunMop = typeRunMop;
    }

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

    public Long getTicketId() {
        return ticketId;
    }

    public void setTicketId(Long ticketId) {
        this.ticketId = ticketId;
    }

    public Long getAlarmId() {
        return alarmId;
    }

    public void setAlarmId(Long alarmId) {
        this.alarmId = alarmId;
    }

    public String getTicketCode() {
        return ticketCode;
    }

    public void setTicketCode(String ticketCode) {
        this.ticketCode = ticketCode;
    }

    public String getAlarmDomain() {
        return alarmDomain;
    }

    public void setAlarmDomain(String alarmDomain) {
        this.alarmDomain = alarmDomain;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
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
    //</editor-fold>
}
