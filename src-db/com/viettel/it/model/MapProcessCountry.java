package com.viettel.it.model;

import com.viettel.model.CatCountryBO;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;

/**
 * Created by hienhv4 on 6/15/2017.
 */
@Entity
@Table(name = "MAP_PROCESS_COUNTRY")
public class MapProcessCountry implements java.io.Serializable {
    private Long id;
    private String processIp;
    private Integer processPort;
    private CatCountryBO countryCode;
    private Long status;
    //20181218_tudn_start tao it cho thi truong
    private Long typeModule; //1:module ha tang; 2:module it business
    //20181218_tudn_end tao it cho thi truong

    @Id
    @Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "PROCESS_IP", length = 50)
    public String getProcessIp() {
        return processIp;
    }

    public void setProcessIp(String processIp) {
        this.processIp = processIp;
    }

    @Column(name = "PROCESS_PORT", precision = 5, scale = 0)
    public Integer getProcessPort() {
        return processPort;
    }

    public void setProcessPort(Integer processPort) {
        this.processPort = processPort;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "COUNTRY_CODE")
    public CatCountryBO getCountryCode() {
        return this.countryCode;
    }

    public void setCountryCode(CatCountryBO countryCode) {
        this.countryCode = countryCode;
    }

    @Column(name = "STATUS", precision = 1, scale = 0)
    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    //20181218_tudn_start tao it cho thi truong
    @Column(name = "TYPE_MODULE", precision = 1, scale = 0)
    public Long getTypeModule() {
        return typeModule;
    }

    public void setTypeModule(Long typeModule) {
        this.typeModule = typeModule;
    }
    //20181218_tudn_end tao it cho thi truong
}
