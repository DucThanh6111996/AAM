package com.viettel.bean;

public class LogError {
    private String errorText;
    private String excludeText;
    private Integer frequency;
    private Integer clear;

    public String getErrorText() {
        return errorText;
    }

    public void setErrorText(String errorText) {
        this.errorText = errorText;
    }

    public String getExcludeText() {
        return excludeText;
    }

    public void setExcludeText(String excludeText) {
        this.excludeText = excludeText;
    }

    public Integer getFrequency() {
        return frequency;
    }

    public void setFrequency(Integer frequency) {
        this.frequency = frequency;
    }

    public Integer getClear() {
        return clear;
    }

    public void setClear(Integer clear) {
        this.clear = clear;
    }

    @Override
    public String toString() {
        return "LogError{" +
                "errorText='" + errorText + '\'' +
                ", excludeText='" + excludeText + '\'' +
                ", frequency=" + frequency +
                ", clear=" + clear +
                '}';
    }
}
