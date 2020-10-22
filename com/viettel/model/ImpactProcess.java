package com.viettel.model;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

/**
 * @author quanns2
 */
@Entity
@Table(name = "IMPACT_PROCESS")
public class ImpactProcess implements Serializable {
    private Long id;
    private String name;
    private Long unitId;
    private String link;
    private String username;
    private String password;
    private Integer status;

    private String aomWsUrl;
    private String aomWsUsername;
    private String aomWsPassword;

    private String nationCode;

    //tuanda38_20180619_start
    private String CyberArkIps;

    // anhnt2 07/18/2018
    private Integer active;

    @Column(name = "CYBER_ARK_IPS")
    public String getCyberArkIps() {
        return CyberArkIps;
    }

    public void setCyberArkIps(String cyberArkIps) {
        CyberArkIps = cyberArkIps;
    }
    //tuanda38_20180619_end

    @Id
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "UNIT_ID")
    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    @Column(name = "AOM_WS_URL")
    public String getAomWsUrl() {
        return aomWsUrl;
    }

    public void setAomWsUrl(String aomWsUrl) {
        this.aomWsUrl = aomWsUrl;
    }

    @Column(name = "AOM_WS_USERNAME")
    public String getAomWsUsername() {
        return aomWsUsername;
    }

    public void setAomWsUsername(String aomWsUsername) {
        this.aomWsUsername = aomWsUsername;
    }

    @Column(name = "AOM_WS_PASSWORD")
    public String getAomWsPassword() {
        return aomWsPassword;
    }

    public void setAomWsPassword(String aomWsPassword) {
        this.aomWsPassword = aomWsPassword;
    }

    @Column(name = "NATION_CODE")
    public String getNationCode() {
        return nationCode;
    }

    public void setNationCode(String nationCode) {
        this.nationCode = nationCode;
    }

    @Column(name = "ACTIVE")
    public Integer getActive() {
        return active;
    }

    public void setActive(Integer active) {
        this.active = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        ImpactProcess that = (ImpactProcess) o;

        return new EqualsBuilder()
                .append(id, that.id)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(id)
                .toHashCode();
    }
}
