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
@Table(name = "CATEGORY_DOMAIN")
public class CategoryDomain implements Serializable {
    private Long id;
    private String systemType;
    private String domainCode;
    private String description;
    private CategoryGroupDomain groupDomain;
    private Date updateTime;
    private String createUser;

    public CategoryDomain() {
    }

    @SequenceGenerator(name = "generator", sequenceName = "CATEGORY_DOMAIN_SEQ", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator")
    @Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "SYSTEM_TYPE")
    public String getSystemType() {
        return systemType;
    }

    public void setSystemType(String systemType) {
        this.systemType = systemType;
    }

    @Column(name = "DOMAIN_CODE")
    public String getDomainCode() {
        return domainCode;
    }

    public void setDomainCode(String domainCode) {
        this.domainCode = domainCode;
    }

    @Column(name = "DESCRIPTION")
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "GROUP_ID", nullable = false)
    public CategoryGroupDomain getGroupDomain() {
        return groupDomain;
    }

    public void setGroupDomain(CategoryGroupDomain groupDomain) {
        this.groupDomain = groupDomain;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CategoryDomain that = (CategoryDomain) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
