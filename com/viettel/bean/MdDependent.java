package com.viettel.bean;

import java.io.Serializable;

/**
 * @author quanns2
 */
public class MdDependent implements Serializable {
    private Long mdId;
    private Long dependentId;
    private Integer level;
    private String code;

    public Long getMdId() {
        return mdId;
    }

    public void setMdId(Long mdId) {
        this.mdId = mdId;
    }

    public Long getDependentId() {
        return dependentId;
    }

    public void setDependentId(Long dependentId) {
        this.dependentId = dependentId;
    }

    public Integer getLevel() {
        return level;
    }

    public void setLevel(Integer level) {
        this.level = level;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }
}
