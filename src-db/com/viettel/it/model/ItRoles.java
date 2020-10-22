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
@Table(name = "IT_ROLES")
public class ItRoles implements java.io.Serializable {

    private Long roleId;
    private String roleCode;
    private List<ItUserRole> lstUsrRole;

    @Id
    @Column(name = "ROLE_ID", unique = true, nullable = false, precision = 12, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "IT_ROLES_SEQ", allocationSize = 1)
    public Long getRoleId() {
        return roleId;
    }

    public void setRoleId(Long roleId) {
        this.roleId = roleId;
    }

    @Column(name = "ROLE_CODE", length = 200)
    public String getRoleCode() {
        return roleCode;
    }

    public void setRoleCode(String roleCode) {
        this.roleCode = roleCode;
    }

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "role", cascade=CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<ItUserRole> getLstUsrRole() {
        return lstUsrRole;
    }

    public void setLstUsrRole(List<ItUserRole> lstUsrRole) {
        this.lstUsrRole = lstUsrRole;
    }
}
