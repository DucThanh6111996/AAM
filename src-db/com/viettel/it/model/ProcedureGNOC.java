package com.viettel.it.model;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.*;

/**
 * Created by tudn on 04/01/2019.
 */
@Entity
@Table(name = "PROCEDURE_GNOC")
public class ProcedureGNOC implements Serializable {
    private Long id;
    private Long procedureGNOCId;
    private String procedureGNOCName;
    private String procedureGNOCCode;
    private Long procedureLevel;
    private Long parentId;
    private ProcedureGNOC parent;
    private String procedureName;
    private Long gnocCrTypeId;
    private String gnocCrTypeName;
    private String gnocDescription;
    private Long gnocDeviceTypeId;
    private String gnocDeviceTypeName;
    private Long gnocImpactSegmentId;
    private String gnocImpactSegmentName;
    private Long isActive;
    //20190729_tudn_start sua dau viec quy trinh cho GNOC
    private String procedureGNOCNameEn;
    //20190729_tudn_end sua dau viec quy trinh cho GNOC

    //20200819_namlh38_add_Quản lý template - đầu việc
    private List<FlowTemplates> flowTemplates;
    private String flowTemplateName;
    private String procedureGNOCNameSearch;

    public ProcedureGNOC() {
        flowTemplates = new ArrayList<>();
    }

    @SequenceGenerator(name = "generator", sequenceName = "PROCEDURE_GNOC_SEQ", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator")
    @Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "PROCEDURE_GNOC_ID")
    public Long getProcedureGNOCId() {
        return procedureGNOCId;
    }

    public void setProcedureGNOCId(Long procedureGNOCId) {
        this.procedureGNOCId = procedureGNOCId;
    }

    @Column(name = "PROCEDURE_GNOC_NAME")
    public String getProcedureGNOCName() {
        return procedureGNOCName;
    }

    public void setProcedureGNOCName(String procedureGNOCName) {
        this.procedureGNOCName = procedureGNOCName;
    }

    @Column(name = "PROCEDURE_GNOC_CODE")
    public String getProcedureGNOCCode() {
        return procedureGNOCCode;
    }

    public void setProcedureGNOCCode(String procedureGNOCCode) {
        this.procedureGNOCCode = procedureGNOCCode;
    }

    @Column(name = "PROCEDURE_LEVEL")
    public Long getProcedureLevel() {
        return procedureLevel;
    }

    public void setProcedureLevel(Long procedureLevel) {
        this.procedureLevel = procedureLevel;
    }

    @Column(name = "PARENT_ID")
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

//    @ManyToOne()
//    @JoinColumn(name = "PARENT_ID", insertable = false, updatable = false)
//    public ProcedureGNOC getParent() {
//        return parent;
//    }
//
//    public void setParent(ProcedureGNOC parent) {
//        this.parent = parent;
//    }

    @Column(name = "PROCEDURE_NAME")
    public String getProcedureName() {
        return procedureName;
    }

    public void setProcedureName(String procedureName) {
        this.procedureName = procedureName;
    }

    @Column(name = "GNOC_CR_TYPE_ID")
    public Long getGnocCrTypeId() {
        return gnocCrTypeId;
    }

    public void setGnocCrTypeId(Long gnocCrTypeId) {
        this.gnocCrTypeId = gnocCrTypeId;
    }

    @Column(name = "GNOC_DESCRIPTION")
    public String getGnocDescription() {
        return gnocDescription;
    }

    public void setGnocDescription(String gnocDescription) {
        this.gnocDescription = gnocDescription;
    }

    @Column(name = "GNOC_DEVICE_TYPE_ID")
    public Long getGnocDeviceTypeId() {
        return gnocDeviceTypeId;
    }

    public void setGnocDeviceTypeId(Long gnocDeviceTypeId) {
        this.gnocDeviceTypeId = gnocDeviceTypeId;
    }

    @Column(name = "GNOC_IMPACT_SEGMENT_ID")
    public Long getGnocImpactSegmentId() {
        return gnocImpactSegmentId;
    }

    public void setGnocImpactSegmentId(Long gnocImpactSegmentId) {
        this.gnocImpactSegmentId = gnocImpactSegmentId;
    }

    @Column(name = "GNOC_IS_ACTIVE")
    public Long getIsActive() {
        return isActive;
    }

    public void setIsActive(Long isActive) {
        this.isActive = isActive;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProcedureGNOC that = (ProcedureGNOC) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        if (id==null)
            return -1;
        return id.hashCode();
    }

    //20190729_tudn_start sua dau viec quy trinh cho GNOC
    @Column(name = "PROCEDURE_GNOC_NAME_EN")
    public String getProcedureGNOCNameEn() {
        return procedureGNOCNameEn;
    }

    public void setProcedureGNOCNameEn(String procedureGNOCNameEn) {
        this.procedureGNOCNameEn = procedureGNOCNameEn;
    }
    //20190729_tudn_end sua dau viec quy trinh cho GNOC


    @Column(name = "GNOC_CR_TYPE_NAME")
    public String getGnocCrTypeName() {
        return gnocCrTypeName;
    }

    public void setGnocCrTypeName(String gnocCrTypeName) {
        this.gnocCrTypeName = gnocCrTypeName;
    }

    @Column(name = "GNOC_DEVICE_TYPE_NAME")
    public String getGnocDeviceTypeName() {
        return gnocDeviceTypeName;
    }

    public void setGnocDeviceTypeName(String gnocDeviceTypeName) {
        this.gnocDeviceTypeName = gnocDeviceTypeName;
    }

    @Column(name = "GNOC_IMPACT_SEGMENT_NAME")
    public String getGnocImpactSegmentName() {
        return gnocImpactSegmentName;
    }

    public void setGnocImpactSegmentName(String gnocImpactSegmentName) {
        this.gnocImpactSegmentName = gnocImpactSegmentName;
    }

    @Transient
    public List<FlowTemplates> getFlowTemplates() {
        return flowTemplates;
    }

    public void setFlowTemplates(List<FlowTemplates> flowTemplates) {
        this.flowTemplates = flowTemplates;
    }

    @Transient
    public String getFlowTemplateName() {
        return flowTemplateName;
    }

    public void setFlowTemplateName(String flowTemplateName) {
        this.flowTemplateName = flowTemplateName;
    }

    @Transient
    public String getProcedureGNOCNameSearch() {
        this.procedureGNOCNameSearch = this.procedureGNOCName+this.procedureGNOCNameEn;
        return procedureGNOCNameSearch;
    }

    public void setProcedureGNOCNameSearch(String procedureGNOCNameSearch) {
        this.procedureGNOCNameSearch = procedureGNOCNameSearch;
    }
}
