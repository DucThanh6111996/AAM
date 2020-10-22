package com.viettel.it.model;

// Generated Sep 8, 2016 5:07:30 PM by Hibernate Tools 4.0.0

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 Created by VTN-PTPM-NV36
 */
@Entity
@Table(name = "COMMAND_BLACKLIST")
public class CommandBlacklist implements java.io.Serializable {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    private Long commandBlacklistId;
    private Vendor vendor;
    private Version version;
    private NodeType nodeType;
    private String cmdRegex;
    private String operator;
    private String standardValue;
    private Date createTime;
    private Date updateTime;
    private String createdBy;
    private String lastUpdateBy;

    public CommandBlacklist() {
        nodeType = new NodeType();
        vendor = new Vendor();
        version = new Version();
    }

    @Id
    @Column(name = "ID", unique = true, nullable = false, precision = 10, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "COMMAND_BLACKLIST_SEQ", allocationSize = 1)
    public Long getCommandBlacklistId() {
        return this.commandBlacklistId;
    }

    public void setCommandBlacklistId(Long commandBlacklistId) {
        this.commandBlacklistId = commandBlacklistId;
    }

    @Fetch(FetchMode.SELECT)
    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "VENDOR_ID", nullable = false)
    public Vendor getVendor() {
        return this.vendor;
    }

    @Fetch(FetchMode.SELECT)
    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "VERSION_ID", nullable = false)
    public Version getVersion() {
        return version;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "NODE_TYPE_ID", nullable = false)
    public NodeType getNodeType() {
        return this.nodeType;
    }

    @Column(name = "CMD_REGEX", length = 500)
    public String getCmdRegex() {
        return cmdRegex;
    }

    @Column(name = "OPERATOR", length = 50)
    public String getOperator() {
        return operator;
    }

    @Column(name = "STANDARD_VALUE", length = 500)
    public String getStandardValue() {
        return this.standardValue;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_TIME", nullable = false, length = 7)
    public Date getCreateTime() {
        return this.createTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "UPDATE_TIME", nullable = false, length = 7)
    public Date getUpdateTime() {
        return this.updateTime;
    }

    @Column(name = "CREATED_BY", nullable = false, length = 200)
    public String getCreatedBy() {
        return createdBy;
    }

    @Column(name = "LAST_UPDATE_BY", nullable = false, length = 200)
    public String getLastUpdateBy() {
        return lastUpdateBy;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    public void setCmdRegex(String cmdRegex) {
        this.cmdRegex = cmdRegex;
    }

    public void setOperator(String operator) {
        this.operator = operator;
    }

    public void setStandardValue(String standardValue) {
        this.standardValue = standardValue;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public void setCreatedBy(String createdBy) {
        this.createdBy = createdBy;
    }

    public void setLastUpdateBy(String lastUpdateBy) {
        this.lastUpdateBy = lastUpdateBy;
    }

    @Override
    public String toString() {
        return "CommandBlacklist{" +
                "commandBlacklistId=" + commandBlacklistId +
                ", vendorId='" + vendor.getVendorId() + '\'' +
                ", versionId='" + version.getVersionId() + '\'' +
                ", nodeTypeId=" + nodeType.getTypeId() +
                ", cmdRegex='" + cmdRegex + '\'' +
                ", operator=" + operator +
                ", standardValue='" + standardValue + '\'' +
                ", createTime='" + createTime + '\'' +
                ", updateTime='" + updateTime + '\'' +
                ", operator='" + operator + '\'' +
                ", createdBy='" + createdBy + '\'' +
                ", lastUpdateBy='" + lastUpdateBy + '\'' +
                '}';
    }
}
