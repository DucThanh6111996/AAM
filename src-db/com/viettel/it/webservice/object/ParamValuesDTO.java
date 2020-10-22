package com.viettel.it.webservice.object;

import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;

/**
 * Created by hanh on 4/18/2017.
 */
@XmlRootElement
public class ParamValuesDTO implements Serializable {

    private Long paramInputId;
    private String paramCode;
    private String paramValue;
    private String paramLabel;
    private String formula;
    private String description;

    private Long groupCode;
    private boolean isDeclare;
    private boolean disableByInOut; //Neu tat ca cac lenh cua tham so deu la tham chieu thi se disable

    public String getParamLabel() {
        return paramLabel;
    }

    public void setParamLabel(String paramLabel) {
        this.paramLabel = paramLabel;
    }

    public Long getParamInputId() {
        return paramInputId;
    }

    public void setParamInputId(Long paramInputId) {
        this.paramInputId = paramInputId;
    }

    public String getParamCode() {
        return paramCode;
    }

    public void setParamCode(String paramCode) {
        this.paramCode = paramCode;
    }

    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }

    public String getFormula() {
        return formula;
    }

    public void setFormula(String formula) {
        this.formula = formula;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Long getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(Long groupCode) {
        this.groupCode = groupCode;
    }

    public boolean isDeclare() {
        return isDeclare;
    }

    public void setDeclare(boolean declare) {
        isDeclare = declare;
    }

    public boolean isDisableByInOut() {
        return disableByInOut;
    }

    public void setDisableByInOut(boolean disableByInOut) {
        this.disableByInOut = disableByInOut;
    }
}
