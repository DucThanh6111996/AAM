package com.viettel.it.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;

import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by hanh on 5/15/2017.
 */

@Entity
@Table(name = "IT_SERVICES")
public class ItServices implements java.io.Serializable {

    private Long serviceId;
    private Long businessId;
    private String serviceCode;
    private String serviceName;
    private ItBusinessGroup businessGroup;

    @Id
    @Column(name = "SERVICE_ID", unique = true, nullable = false, precision = 12, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "IT_SERVICES_SEQ", allocationSize = 1)
    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    @Column(name = "BUSINESS_ID", precision = 12, scale = 0)
    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    @Column(name = "SERVICE_CODE", length = 200)
    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    @Column(name = "SERVICE_NAME", length = 200)
    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "BUSINESS_ID", nullable = false, insertable = false, updatable = false)
    public ItBusinessGroup getBusinessGroup() {
        return businessGroup;
    }

    public void setBusinessGroup(ItBusinessGroup businessGroup) {
        this.businessGroup = businessGroup;
    }
}
