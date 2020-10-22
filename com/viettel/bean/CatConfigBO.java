package com.viettel.bean;

/**
 * Created by VTN-PTPM-NV36 on 1/3/2019.
 */
public class CatConfigBO {
    private String key;
    private String propertyValue;
    private String configGroup;
    private Long isActive;

    public CatConfigBO() {
    }

    public CatConfigBO(String key, String propertyValue, String configGroup, Long isActive) {
        this.key = key;
        this.propertyValue = propertyValue;
        this.configGroup = configGroup;
        this.isActive = isActive;
    }

    public String getKey() {
        return key;
    }

    public void setKey(String key) {
        this.key = key;
    }

    public String getPropertyValue() {
        return propertyValue;
    }

    public void setPropertyValue(String propertyValue) {
        this.propertyValue = propertyValue;
    }

    public String getConfigGroup() {
        return configGroup;
    }

    public void setConfigGroup(String configGroup) {
        this.configGroup = configGroup;
    }

    public Long getIsActive() {
        return isActive;
    }

    public void setIsActive(Long isActive) {
        this.isActive = isActive;
    }
}
