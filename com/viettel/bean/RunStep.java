package com.viettel.bean;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Map;

/**
 * Created by quan on 7/28/2016.
 */
public class RunStep {
//    private Integer value;
    private MapEntry value;
    private String label;
    private String status;
    private String description;

/*    public Integer getValue() {
        return value;
    }

    public void setValue(Integer value) {
        this.value = value;
    }*/

    public MapEntry getValue() {
        return value;
    }

    public void setValue(MapEntry value) {
        this.value = value;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        RunStep runStep = (RunStep) o;

        return new EqualsBuilder()
                .append(value, runStep.value)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(value)
                .toHashCode();
    }

    //06-11-2018 KienPD add checkbox value start
    private boolean checkbox;

    public boolean isCheckbox() {
        return checkbox;
    }

    public void setCheckbox(boolean checkbox) {
        this.checkbox = checkbox;
    }
    //06-11-2018 KienPD add checkbox value end
}
