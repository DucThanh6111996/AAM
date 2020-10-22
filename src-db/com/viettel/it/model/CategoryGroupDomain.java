package com.viettel.it.model;

import javax.persistence.*;
import java.io.Serializable;

/**
 * Created by quytv7 on 1/17/2018.
 */
@Entity
@Table(name = "CATEGORY_GROUP_DOMAIN")
public class CategoryGroupDomain implements Serializable {
    private Long id;
    private String systemType;
    private String groupName;

    public CategoryGroupDomain() {
    }
    @SequenceGenerator(name = "generator", sequenceName = "CATEGORY_GROUP_DOMAIN_SEQ", allocationSize = 1)
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

    @Column(name = "GROUP_NAME")
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CategoryGroupDomain that = (CategoryGroupDomain) o;

        return id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
