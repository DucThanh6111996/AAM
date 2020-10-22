package com.viettel.it.webservice.object;

/**
 * Created by hanh on 4/18/2017.
 */
public class FlowTemplatesDTO {

    private Long templateGroupId;
    private Long templateId;
    private String templateName;
    private String desc;

    public FlowTemplatesDTO() {
    }

    public FlowTemplatesDTO(Long templateGroupId, Long templateId, String templateName, String desc) {
        this.templateGroupId = templateGroupId;
        this.templateId = templateId;
        this.templateName = templateName;
        this.desc = desc;
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
}
