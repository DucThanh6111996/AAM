package com.viettel.it.object;

import java.util.Date;

/**
 * Created by hanhnv68 on 9/28/2017.
 */
public class SidnBccsObj {
    private Long id;
    private String createdDate;
    private String groupCommand;
    private Long psubid;
    private Long pcontracid;
    private String pisdn;
    private String pimsi;
    private Long pcenter;
    private Long status;
    private String pserviceType;

    public SidnBccsObj() {
    }

    public SidnBccsObj(Long id) {
        this.id = id;
    }

    public SidnBccsObj(Long id, String createdDate, String groupCommand, Long psubid,
                       Long pcontracid, String pisdn, String pimsi, Long pcenter, Long status, String pserviceType) {
        this.id = id;
        this.createdDate = createdDate;
        this.groupCommand = groupCommand;
        this.psubid = psubid;
        this.pcontracid = pcontracid;
        this.pisdn = pisdn;
        this.pimsi = pimsi;
        this.pcenter = pcenter;
        this.status = status;
        this.pserviceType = pserviceType;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getCreatedDate() {
        return createdDate;
    }

    public void setCreatedDate(String createdDate) {
        this.createdDate = createdDate;
    }

    public String getGroupCommand() {
        return groupCommand;
    }

    public void setGroupCommand(String groupCommand) {
        this.groupCommand = groupCommand;
    }

    public Long getPsubid() {
        return psubid;
    }

    public void setPsubid(Long psubid) {
        this.psubid = psubid;
    }

    public Long getPcontracid() {
        return pcontracid;
    }

    public void setPcontracid(Long pcontracid) {
        this.pcontracid = pcontracid;
    }

    public String getPisdn() {
        return pisdn;
    }

    public void setPisdn(String pisdn) {
        this.pisdn = pisdn;
    }

    public String getPimsi() {
        return pimsi;
    }

    public void setPimsi(String pimsi) {
        this.pimsi = pimsi;
    }

    public Long getPcenter() {
        return pcenter;
    }

    public void setPcenter(Long pcenter) {
        this.pcenter = pcenter;
    }

    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    public String getPserviceType() {
        return pserviceType;
    }

    public void setPserviceType(String pserviceType) {
        this.pserviceType = pserviceType;
    }
}
