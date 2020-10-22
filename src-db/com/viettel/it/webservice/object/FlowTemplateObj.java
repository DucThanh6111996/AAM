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
public class FlowTemplateObj {
    private Long templateGroupId;
    private Long templateId;
    private String templateName;
    private String desc;
    private String serviceCode;

    public FlowTemplateObj() {
    }

    public FlowTemplateObj(Long templateGroupId, Long templateId, String templateName, String desc, String serviceCode) {
        this.templateGroupId = templateGroupId;
        this.templateId = templateId;
        this.templateName = templateName;
        this.desc = desc;
        this.serviceCode = serviceCode;
    }

    public Long getTemplateGroupId() {
        return templateGroupId;
    }

    public void setTemplateGroupId(Long templateGroupId) {
        this.templateGroupId = templateGroupId;
    }

    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getDesc() {
        return desc;
    }

    public void setDesc(String desc) {
        this.desc = desc;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }
}
