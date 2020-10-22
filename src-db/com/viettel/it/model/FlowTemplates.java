package com.viettel.it.model;

// Generated Sep 8, 2016 5:07:30 PM by Hibernate Tools 4.0.0

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * FlowTemplates generated by hbm2java
 */
@Entity
@Table(name = "FLOW_TEMPLATES")
public class FlowTemplates implements java.io.Serializable {

    private Long flowTemplatesId;
    private FlowTemplates flowTemplates;
    private Long parentId;
    private String flowTemplateName;
    private Date createDate;
    private String createBy;
    private List<FlowTemplates> flowTemplateses = new ArrayList<FlowTemplates>(0);
    private List<ActionOfFlow> actionOfFlows = new ArrayList<ActionOfFlow>(0);
    private List<FlowRunAction> flowRunActions = new ArrayList<FlowRunAction>(0);
    private List<ParamGroup> paramGroups = new ArrayList<ParamGroup>(0);
    private Integer status;
    private Integer templateType; // tac dong gnoc hay tac dong uctt (voffice)
    private boolean generationDT;
    private Long isGenerateDT;

    private TemplateGroup templateGroup;

    /*20190408_chuongtq start check param when create MOP*/
    private List<ParamCondition> paramConditions = new ArrayList<ParamCondition>(0);
    private String updateBy;
    private Long procedureId;
    private Long procedureWorkFlowId;
    /*20190408_chuongtq end check param when create MOP*/

    public FlowTemplates() {
    }

    @Id
    @Column(name = "FLOW_TEMPLATES_ID", unique = true, nullable = false, precision = 22, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "FLOW_TEMPLATES_SEQ", allocationSize = 1)
    public Long getFlowTemplatesId() {
        return this.flowTemplatesId;
    }

    @ManyToOne()
    @JoinColumn(name = "PARENT_ID")
    public FlowTemplates getFlowTemplates() {
        return this.flowTemplates;
    }

    public void setFlowTemplates(FlowTemplates flowTemplates) {
        this.flowTemplates = flowTemplates;
    }

    @Column(name = "PARENT_ID", insertable = false, updatable = false)
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }


    @LazyCollection(LazyCollectionOption.TRUE)
    @Column(name = "FLOW_TEMPLATE_NAME", nullable = false, length = 200)
    public String getFlowTemplateName() {
        return this.flowTemplateName;
    }

    public void setFlowTemplateName(String flowTemplateName) {
        this.flowTemplateName = flowTemplateName;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_DATE", nullable = false, length = 7)
    public Date getCreateDate() {
        return this.createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "CREATE_BY", nullable = false, length = 200)
    public String getCreateBy() {
        return this.createBy;
    }

    @OneToMany(fetch = FetchType.LAZY, mappedBy = "flowTemplates")
    @LazyCollection(LazyCollectionOption.TRUE)
    public List<FlowTemplates> getFlowTemplateses() {
        return this.flowTemplateses;
    }

    public void setFlowTemplateses(List<FlowTemplates> flowTemplateses) {
        this.flowTemplateses = flowTemplateses;
    }

    @Column(name = "IS_GENERATE_DT", precision = 1, scale = 0)
    public Long getIsGenerateDT() {
        return isGenerateDT;
    }

    public void setIsGenerateDT(Long isGenerateDT) {
        if (isGenerateDT != null && isGenerateDT == 1l) {
            generationDT = true;
        } else {
            generationDT = false;
        }
        this.isGenerateDT = isGenerateDT;
    }

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "flowTemplates", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.TRUE)
    @OrderBy(value = "groupActionOrder,stepNumberLabel")
    public List<ActionOfFlow> getActionOfFlows() {
        return this.actionOfFlows;
    }


    public void setFlowTemplatesId(Long flowTemplatesId) {
        this.flowTemplatesId = flowTemplatesId;
    }

    public void setCreateBy(String createBy) {
        this.createBy = createBy;
    }

    public void setActionOfFlows(List<ActionOfFlow> actionOfFlows) {
        this.actionOfFlows = actionOfFlows;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((flowTemplatesId == null) ? 0 : flowTemplatesId.hashCode());
        return result;
    }


    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        FlowTemplates other = (FlowTemplates) obj;
        if (flowTemplatesId == null) {
            if (other.flowTemplatesId != null)
                return false;
        } else if (!flowTemplatesId.equals(other.flowTemplatesId))
            return false;
        return true;
    }

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "flowTemplates", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<FlowRunAction> getFlowRunActions() {
        return this.flowRunActions;
    }

    public void setFlowRunActions(List<FlowRunAction> flowRunActions) {
        this.flowRunActions = flowRunActions;
    }

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "flowTemplates", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<ParamGroup> getParamGroups() {
        return this.paramGroups;
    }

    public void setParamGroups(List<ParamGroup> paramGroups) {
        this.paramGroups = paramGroups;
    }

    @Column(name = "STATUS", nullable = true, length = 7)
    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Column(name = "TEMPLATE_TYPE", nullable = true, length = 7)
    public Integer getTemplateType() {
        return templateType;
    }

    public void setTemplateType(Integer templateType) {
        this.templateType = templateType;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "TEMPLATE_GROUP_ID")
    public TemplateGroup getTemplateGroup() {
        return templateGroup;
    }

    public void setTemplateGroup(TemplateGroup templateGroup) {
        this.templateGroup = templateGroup;
    }

    //20181119_tudn_start them danh sach lenh blacklist
    private Integer crType;

    @Column(name = "CR_TYPE")
    public Integer getCrType() {
        return crType;
    }

    public void setCrType(Integer crType) {
        this.crType = crType;
    }
    //20181119_tudn_end them danh sach lenh blacklist

    /*20190408_chuongtq start check param when create MOP*/
    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "flowTemplates", cascade = CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<ParamCondition> getParamConditions() {
        return paramConditions;
    }

    public void setParamConditions(List<ParamCondition> paramConditions) {
        this.paramConditions = paramConditions;
    }

    @Column(name = "UPDATE_BY")
    public String getUpdateBy() {
        return updateBy;
    }

    public void setUpdateBy(String updateBy) {
        this.updateBy = updateBy;
    }
    /*20190408_chuongtq end check param when create MOP*/

    @Transient
    public boolean isGenerationDT() {
        return generationDT;
    }

    public void setGenerationDT(boolean generationDT) {
        this.generationDT = generationDT;
    }

    @Column(name = "PROCEDURE_ID")
    public Long getProcedureId() {
        return procedureId;
    }

    public void setProcedureId(Long procedureId) {
        this.procedureId = procedureId;
    }

    @Column(name = "PROCEDURE_WORK_FLOW_ID")
    public Long getProcedureWorkFlowId() {
        return procedureWorkFlowId;
    }

    public void setProcedureWorkFlowId(Long procedureWorkFlowId) {
        this.procedureWorkFlowId = procedureWorkFlowId;
    }

    @Override
    public String toString() {
        return "FlowTemplates{" +
                "flowTemplatesId=" + flowTemplatesId +
                ", flowTemplateName='" + flowTemplateName + '\'' +
                ", createDate=" + (createDate == null ? "null" : createDate) +
                ", createBy=" + (createBy == null ? "null" : createBy)+
                ", status=" + status +
                ", templateType=" + (templateType == null ? "null" : templateType) +
                ", templateGroupId=" + (templateGroup == null ? "null" : templateGroup.getId()) +
                ", crType=" + (crType == null ? "null" : crType) +
                '}';
    }
}
