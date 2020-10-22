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
@Table(name = "IT_USERS")
public class ItUsers implements java.io.Serializable {

    private Long userId;
    private String userName;
    private String fullName;
    private String email;
    private String phone;
    private Long staffCode;
    private Long status;

    private List<ItUserRole> lstUserRole;
    private List<ItUserBusinessGroup> lstUserBusinessGroup;
    private List<ItUserService> lstUserService;
    private List<ItUserAction> lstUserAction;
    private String secretKey;
    private String optType;

    @Id
    @Column(name = "USER_ID", unique = true, nullable = false, precision = 12, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "IT_USERS_SEQ", allocationSize = 1)
    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    @Column(name = "USER_NAME", length = 200)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }



    @Column(name = "FULL_NAME", length = 200)
    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    @Column(name = "EMAIL", length = 200)
    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    @Column(name = "PHONE", length = 200)
    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    @Column(name = "STAFF_CODE", precision = 12, scale = 0)
    public Long getStaffCode() {
        return staffCode;
    }

    public void setStaffCode(Long staffCode) {
        this.staffCode = staffCode;
    }

    @Column(name = "STATUS", precision = 12, scale = 0)
    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    @Column(name = "SECRET_KEY")
    public String getSecretKey() {
        return secretKey;
    }

    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    @Column(name = "OTP_TYPE")
    public String getOptType() {
        return optType;
    }

    public void setOptType(String optType) {
        this.optType = optType;
    }

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade=CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<ItUserRole> getLstUserRole() {
        return lstUserRole;
    }

    public void setLstUserRole(List<ItUserRole> lstUserRole) {
        this.lstUserRole = lstUserRole;
    }

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade=CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<ItUserBusinessGroup> getLstUserBusinessGroup() {
        return lstUserBusinessGroup;
    }

    public void setLstUserBusinessGroup(List<ItUserBusinessGroup> lstUserBusinessGroup) {
        this.lstUserBusinessGroup = lstUserBusinessGroup;
    }

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade=CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<ItUserService> getLstUserService() {
        return lstUserService;
    }

    public void setLstUserService(List<ItUserService> lstUserService) {
        this.lstUserService = lstUserService;
    }

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "user", cascade=CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<ItUserAction> getLstUserAction() {
        return lstUserAction;
    }

    public void setLstUserAction(List<ItUserAction> lstUserAction) {
        this.lstUserAction = lstUserAction;
    }
}
