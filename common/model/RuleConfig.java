package com.viettel.model;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by VTN-PTPM-NV36 on 10/31/2018.
 */
@Entity
@Table(name = "ACTION_DETAIL_APP_RULE_CONFIG")
public class RuleConfig implements java.io.Serializable {

    private Long ruleId;
    private Long actionDetailAppId;
    private Long actionId;
    private String pathFile;
    private String ruleEdit;
    private String keyword;
    private String content;
    private String moduleCode;
    private String path;
    private String fileName;


    @Id
    @Column(name = "RULE_ID", unique = true, nullable = false, precision = 12, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "RULE_CONFIG_SEQ", allocationSize = 1)
    public Long getRuleId() {
        return ruleId;
    }

    public void setRuleId(Long ruleId) {
        this.ruleId = ruleId;
    }

    @Column(name = "ACTION_DETAIL_APP_ID")
    public Long getActionDetailAppId() {
        return actionDetailAppId;
    }

    public void setActionDetailAppId(Long actionDetailAppId) {
        this.actionDetailAppId = actionDetailAppId;
    }

    @Column(name = "ACTION_ID")
    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    @Column(name = "PATH_FILE")
    public String getPathFile() {
        return pathFile;
    }

    public void setPathFile(String pathFile) {
        this.pathFile = pathFile;
    }

    @Column(name = "RULE_EDIT")
    public String getRuleEdit() {
        return ruleEdit;
    }

    public void setRuleEdit(String ruleEdit) {
        this.ruleEdit = ruleEdit;
    }

    @Column(name = "KEYWORD")
    public String getKeyword() {
        return keyword;
    }

    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    @Column(name = "CONTENT")
    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }

    @Transient
    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    @Transient
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    @Transient
    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    @Override
    public String toString() {
        return "RuleConfig{" +
                "ruleId=" + ruleId +
                ", actionDetailAppId=" + actionDetailAppId +
                ", pathFile='" + pathFile + '\'' +
                ", ruleEdit='" + ruleEdit + '\'' +
                ", keyword='" + keyword + '\'' +
                ", content='" + content + '\'' +
                '}';
    }
}

