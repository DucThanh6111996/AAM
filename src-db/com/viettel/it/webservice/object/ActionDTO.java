package com.viettel.it.webservice.object;

import com.viettel.it.model.Action;

/**
 * Created by hanhnv68 on 9/8/2017.
 */
public class ActionDTO {


    private Long actionId;
    private Long parentId;
    private String name;
    private String description;
    private String itbusinessType;
    private Long serviceBusinessId;
    private Long treeLevel;
    private String openBlockGroup;
    private Long provisioningType;

    public ActionDTO(Long actionId, Long parentId, String name, String description,
                     String itbusinessType, Long serviceBusinessId, Long treeLevel,
                     String openBlockGroup, Long provisioningType) {
        this.actionId = actionId;
        this.parentId = parentId;
        this.name = name;
        this.description = description;
        this.itbusinessType = itbusinessType;
        this.serviceBusinessId = serviceBusinessId;
        this.treeLevel = treeLevel;
        this.openBlockGroup = openBlockGroup;
        this.provisioningType = provisioningType;
    }

    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getItbusinessType() {
        return itbusinessType;
    }

    public void setItbusinessType(String itbusinessType) {
        this.itbusinessType = itbusinessType;
    }

    public Long getServiceBusinessId() {
        return serviceBusinessId;
    }

    public void setServiceBusinessId(Long serviceBusinessId) {
        this.serviceBusinessId = serviceBusinessId;
    }

    public Long getTreeLevel() {
        return treeLevel;
    }

    public void setTreeLevel(Long treeLevel) {
        this.treeLevel = treeLevel;
    }

    public String getOpenBlockGroup() {
        return openBlockGroup;
    }

    public void setOpenBlockGroup(String openBlockGroup) {
        this.openBlockGroup = openBlockGroup;
    }

    public Long getProvisioningType() {
        return provisioningType;
    }

    public void setProvisioningType(Long provisioningType) {
        this.provisioningType = provisioningType;
    }
}
