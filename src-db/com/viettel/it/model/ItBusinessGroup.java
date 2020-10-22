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
@Table(name = "IT_BUSINESS_GROUP")
public class ItBusinessGroup implements java.io.Serializable {

    private Long businessId;
    private String businessName;
    private List<ItUserBusinessGroup> lstUserBusinessGroup;
    private List<ItServices> lstService;
    /*20181226_hoangnd_them thi truong_start*/
    private Action action;
    private Long parentId;
    private ItBusinessGroup parent;
    private String businessGroupName;
    /*20181226_hoangnd_them thi truong_end*/

    @Id
    @Column(name = "BUSINESS_ID", unique = true, nullable = false, precision = 12, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "IT_BUSINESS_GROUP_SEQ", allocationSize = 1)
    public Long getBusinessId() {
        return businessId;
    }

    public void setBusinessId(Long businessId) {
        this.businessId = businessId;
    }

    @Column(name = "BUSINESS_NAME", length = 200)
    public String getBusinessName() {
        return businessName;
    }

    public void setBusinessName(String businessName) {
        this.businessName = businessName;
    }

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "businessGroup", cascade=CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<ItUserBusinessGroup> getLstUserBusinessGroup() {
        return lstUserBusinessGroup;
    }

    public void setLstUserBusinessGroup(List<ItUserBusinessGroup> lstUserBusinessGroup) {
        this.lstUserBusinessGroup = lstUserBusinessGroup;
    }

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "businessGroup", cascade=CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<ItServices> getLstService() {
        return lstService;
    }

    public void setLstService(List<ItServices> lstService) {
        this.lstService = lstService;
    }

    /*20181226_hoangnd_them thi truong_start*/
    @OneToOne
    @JoinColumns(
        {
            @JoinColumn(updatable=false,insertable=false, name="BUSINESS_NAME", referencedColumnName="NAME"),
            @JoinColumn(updatable=false,insertable=false, name="BUSINESS_ID", referencedColumnName="SERVICE_BUSINESS_ID")
        }
    )
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Column(name = "PARENT_ID")
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    @ManyToOne()
    @JoinColumn(name = "PARENT_ID", insertable = false, updatable = false)
    public ItBusinessGroup getParent() {
        return parent;
    }

    public void setParent(ItBusinessGroup parent) {
        this.parent = parent;
    }

    @Transient
    public String getBusinessGroupName() {
        return (this.action != null && this.action.getAction() != null ? this.action.getAction().getName() : "null") + " - " + this.businessName;
    }

    public void setBusinessGroupName(String businessGroupName) {
        this.businessGroupName = businessGroupName;
    }
    /*20181226_hoangnd_them thi truong_end*/
}
