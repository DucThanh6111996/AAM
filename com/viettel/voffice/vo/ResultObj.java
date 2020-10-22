package com.viettel.voffice.vo;

import org.apache.commons.lang3.builder.ToStringBuilder;

/**
 * Created by quanns2 on 2/9/2017.
 */
public class ResultObj {
    private String transCode;//ma giao dich
    private String actionDate;//ngay xu ly
    private String signComment;//comment ky
    private String signStatus;//trang thai xu ly 1: van thu tu choi, 2: lanh dao tu cho, 3: ky duyet, 4: huy luong, 5: ban hanh
    private String documentCode;//ma ban hanh
    private String publishOganizationCode;
    private String lastSignEmail;//mail nguoi ky cuoi
    private Long voTextId;//id van ban
    private String publishDate;//ngay ban hanh
    private String appCode;
    private String wsdl;

    public String getTransCode() {
        return transCode;
    }

    public void setTransCode(String transCode) {
        this.transCode = transCode;
    }

    public String getActionDate() {
        return actionDate;
    }

    public void setActionDate(String actionDate) {
        this.actionDate = actionDate;
    }

    public String getSignComment() {
        return signComment;
    }

    public void setSignComment(String signComment) {
        this.signComment = signComment;
    }

    public String getSignStatus() {
        return signStatus;
    }

    public void setSignStatus(String signStatus) {
        this.signStatus = signStatus;
    }

    public String getDocumentCode() {
        return documentCode;
    }

    public void setDocumentCode(String documentCode) {
        this.documentCode = documentCode;
    }

    public String getPublishOganizationCode() {
        return publishOganizationCode;
    }

    public void setPublishOganizationCode(String publishOganizationCode) {
        this.publishOganizationCode = publishOganizationCode;
    }

    public String getLastSignEmail() {
        return lastSignEmail;
    }

    public void setLastSignEmail(String lastSignEmail) {
        this.lastSignEmail = lastSignEmail;
    }

    public Long getVoTextId() {
        return voTextId;
    }

    public void setVoTextId(Long voTextId) {
        this.voTextId = voTextId;
    }

    public String getPublishDate() {
        return publishDate;
    }

    public void setPublishDate(String publishDate) {
        this.publishDate = publishDate;
    }

    public String getAppCode() {
        return appCode;
    }

    public void setAppCode(String appCode) {
        this.appCode = appCode;
    }

    public String getWsdl() {
        return wsdl;
    }

    public void setWsdl(String wsdl) {
        this.wsdl = wsdl;
    }

    @Override
    public String toString() {
        return new ToStringBuilder(this)
                .append("transCode", transCode)
                .append("actionDate", actionDate)
                .append("signComment", signComment)
                .append("signStatus", signStatus)
                .append("documentCode", documentCode)
                .append("publishOganizationCode", publishOganizationCode)
                .append("lastSignEmail", lastSignEmail)
                .append("voTextId", voTextId)
                .append("publishDate", publishDate)
                .append("appCode", appCode)
                .append("wsdl", wsdl)
                .toString();
    }
}
