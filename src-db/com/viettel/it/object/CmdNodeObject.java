package com.viettel.it.object;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hanhnv68 on 9/12/2017.
 */
public class CmdNodeObject {

    private Long nodeId;
    private String nodeCode;
    private String nodeName;
    private String nodeIp;
    private String user;
    private String password;
    private String protocol;
    private Long type; // '0 - IMPACT' OR '1 - WRITE LOG'
    private Map<Long, List<CmdObject>> cmdImpacts = new LinkedHashMap<>();

    private String vendorName;
    private String versionName;
    private String typeName;
    private Long totalTimeout = 0l;
    private String url;
    private Integer port;
    private String osType;
    private String effectIp;
    private String serverId;
    private String prompt;

    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    public String getNodeCode() {
        return nodeCode;
    }

    public void setNodeCode(String nodeCode) {
        this.nodeCode = nodeCode;
    }

    public String getNodeName() {
        return nodeName;
    }

    public void setNodeName(String nodeName) {
        this.nodeName = nodeName;
    }

    public String getNodeIp() {
        return nodeIp;
    }

    public void setNodeIp(String nodeIp) {
        this.nodeIp = nodeIp;
    }

    public String getUser() {
        return user;
    }

    public void setUser(String user) {
        this.user = user;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public Map<Long, List<CmdObject>> getCmdImpacts() {
        return cmdImpacts;
    }

    public void setCmdImpacts(Map<Long, List<CmdObject>> cmdImpacts) {
        this.cmdImpacts = cmdImpacts;
    }

    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    public String getVendorName() {
        return vendorName;
    }

    public void setVendorName(String vendorName) {
        this.vendorName = vendorName;
    }

    public String getVersionName() {
        return versionName;
    }

    public void setVersionName(String versionName) {
        this.versionName = versionName;
    }

    public String getTypeName() {
        return typeName;
    }

    public void setTypeName(String typeName) {
        this.typeName = typeName;
    }

    public Long getTotalTimeout() {
        return totalTimeout;
    }

    public void setTotalTimeout(Long totalTimeout) {
        this.totalTimeout = totalTimeout;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getOsType() {
        return osType;
    }

    public void setOsType(String osType) {
        this.osType = osType;
    }

    public String getEffectIp() {
        return effectIp;
    }

    public void setEffectIp(String effectIp) {
        this.effectIp = effectIp;
    }

    public String getServerId() {
        return serverId;
    }

    public void setServerId(String serverId) {
        this.serverId = serverId;
    }

    public String getPrompt() {
        return prompt;
    }

    public void setPrompt(String prompt) {
        this.prompt = prompt;
    }
}
