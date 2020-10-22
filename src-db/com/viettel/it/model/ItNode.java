package com.viettel.it.model;

import com.viettel.it.persistence.ItNodeServiceImpl;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by hanh on 5/15/2017.
 */

@Entity
@Table(name = "IT_NODE")
public class ItNode implements java.io.Serializable {

    private String nodeCode;
    private String osType;
    private Long nodeId;
    private String provinceCode;
    private Long isLab;
    private String stationCode;
    private String departmentName;
    private String effectIp;
    private String nodeIp;
    private Vendor vendor;
    private Version version;
    private String networkType;
    private String jdbcUrl;
    private Long port;
    private NodeType nodeType;
    private String nodeName;
    private String subnetwork;
    private String userName;
    private String password;
    private boolean isNodeLab;


    @Id
    @Column(name = "NODE_ID", unique = true, nullable = false, precision = 12, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "IT_NODE_SEQ", allocationSize = 1)
    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    @Column(name = "NODE_CODE", nullable = false, length = 200)
    public String getNodeCode() {
        return nodeCode;
    }

    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }

    @Column(name = "OS_TYPE", length = 200)
    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    @Column(name = "PROVINCE_CODE", length = 200)
    public String getProvinceCode() {
        return provinceCode;
    }

    public void setProvinceCode(String provinceCode) {
        this.provinceCode = provinceCode;
    }

    @Column(name = "IS_LAB", precision = 12, scale = 0)
    public Long getIsLab() {
        return isLab;
    }

    public void setIsLab(Long isLab) {
        this.isLab = isLab;
    }

    @Column(name = "STATION_CODE", length = 200)
    public String getStationCode() {
        return stationCode;
    }

    public void setStationCode(String stationCode) {
        this.stationCode = stationCode;
    }

    @Column(name = "DEPARTMENT_NAME", length = 200)
    public String getDepartmentName() {
        return departmentName;
    }

    public void setDepartmentName(String departmentName) {
        this.departmentName = departmentName;
    }

    @Column(name = "EFFECT_IP", length = 200)
    public String getEffectIp() {
        return effectIp;
    }

    public void setEffectIp(String effectIp) {
        this.effectIp = effectIp;
    }

    @Column(name = "NODE_IP", length = 200)
    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "VENDOR_ID")
    public Vendor getVendor() {
        return this.vendor;
    }

    public void setVendor(Vendor vendor) {
        this.vendor = vendor;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "NODE_TYPE_ID")
    public NodeType getNodeType() {
        return this.nodeType;
    }

    public void setNodeType(NodeType nodeType) {
        this.nodeType = nodeType;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "VERSION_ID")
    public Version getVersion() {
        return this.version;
    }

    public void setVersion(Version version) {
        this.version = version;
    }

    @Column(name = "NETWORK_TYPE", length = 200)
    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    @Column(name = "JDBC_URL", length = 200)
    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void setJdbcUrl(String jdbcUrl) {
        this.jdbcUrl = jdbcUrl;
    }

    @Column(name = "PORT", precision = 12, scale = 0)
    public Long getPort() {
        return port;
    }

    public void setPort(Long port) {
        this.port = port;
    }

    @Column(name = "NODE_NAME", length = 200)
    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    @Column(name = "SUBNETWORK", length = 200)
    public String getSubnetwork() {
        return subnetwork;
    }

    public void setSubnetwork(String subnetwork) {
        this.subnetwork = subnetwork;
    }

    @Column(name = "USER_NAME", length = 200)
    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Column(name = "PASSWORD", length = 500)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Transient
    public boolean isNodeLab() {
        return isNodeLab;
    }

    public void setNodeLab(boolean isNodeLab) {
        this.isNodeLab = isNodeLab;
    }
}
