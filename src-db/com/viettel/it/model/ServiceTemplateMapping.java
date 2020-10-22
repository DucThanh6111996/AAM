/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.model;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;

/**
 *
 * @author hienhv4
 */
@Entity
@Table(name = "SERVICE_TEMPLATE_MAPPING")
public class ServiceTemplateMapping implements java.io.Serializable {
    private Long mappingId;
    private String serviceCode;
//    private Long templateId;
    private FlowTemplates template;
    private Vendor vendor;
    private Version version;
    private NodeType nodeType;

    @Id
    @Column(name = "MAPPING_ID", nullable = false, precision = 22, scale = 0)
    public Long getMappingId() {
        return mappingId;
    }

    public void setMappingId(Long mappingId) {
        this.mappingId = mappingId;
    }

//    @Id
    @Column(name = "SERVICE_CODE", length = 100)
    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

/*    @Column(name = "TEMPLATE_ID", nullable = false, precision = 10, scale = 0)
    public Long getTemplateId() {
        return templateId;
    }

    public void setTemplateId(Long templateId) {
        this.templateId = templateId;
    }*/

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "TEMPLATE_ID")
    public FlowTemplates getTemplate() {
        return template;
    }

    public void setTemplate(FlowTemplates template) {
        this.template = template;
    }


    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "VENDOR_ID")
    public Vendor getVendor() {
        return vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "VERSION_ID")
    public Version getVersion() {
        return version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "NODE_TYPE_ID")
    public NodeType getNodeType() {
        return nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

}
