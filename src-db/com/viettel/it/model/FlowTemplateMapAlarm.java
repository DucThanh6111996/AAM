package com.viettel.it.model;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Date;

/**
 * Created by quytv7 on 1/17/2018.
 */
@Entity
@Table(name = "FLOW_TEMPLATE_MAP_ALARM")
public class FlowTemplateMapAlarm implements Serializable {
    private Long id;
    private FlowTemplates flowTemplates;
    private ParamInput paramInput;
    private CategoryDomain domain;
    private String regex;
    private String description;
    private Date updateTime;
    private String createUser;
    private Long paramType;
    private String paramCode;
    private String paramValue;
    private CategoryConfigGetNode configGetNode;

    private Action action;

    public FlowTemplateMapAlarm() {
    }

    @SequenceGenerator(name = "generator", sequenceName = "FLOW_TEMPLATE_MAP_ALARM_SEQ", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator")
    @Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "FLOW_TEMPLATES_ID", nullable = false)
    public FlowTemplates getFlowTemplates() {
        return flowTemplates;
    }

    public void setFlowTemplates(FlowTemplates flowTemplates) {
        this.flowTemplates = flowTemplates;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "PARAM_INPUT_ID")
    public ParamInput getParamInput() {
        return paramInput;
    }

    public void setParamInput(ParamInput paramInput) {
        this.paramInput = paramInput;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "ALARM_DOMAIN_ID")
    public CategoryDomain getDomain() {
        return domain;
    }

    public void setDomain(CategoryDomain domain) {
        this.domain = domain;
    }

    @Column(name = "REGEX")
    public String getRegex() {
        return regex;
    }

    public void setRegex(String regex) {
        this.regex = regex;
    }

    @Temporal(TemporalType.DATE)
    @Column(name = "UPDATE_TIME", length = 7)
    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    @Column(name = "CREATE_USER")
    public String getCreateUser() {
        return createUser;
    }

    public void setCreateUser(String createUser) {
        this.createUser = createUser;
    }

    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "PARAM_TYPE")
    public Long getParamType() {
        return paramType;
    }

    public void setParamType(Long paramType) {
        this.paramType = paramType;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "CONFIG_GET_NODE_ID")
    public CategoryConfigGetNode getConfigGetNode() {
        return configGetNode;
    }

    public void setConfigGetNode(CategoryConfigGetNode configGetNode) {
        this.configGetNode = configGetNode;
    }

    @Column(name = "PARAM_CODE")
    public String getParamCode() {
        return paramCode;
    }

    public void setParamCode(String paramCode) {
        this.paramCode = paramCode;
    }

    @Column(name = "PARAM_VALUE")
    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "ACTION_ID")
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }
}
