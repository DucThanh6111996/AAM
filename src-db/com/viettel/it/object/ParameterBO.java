package com.viettel.it.object;

/**
 * Created by khiemvk on 11/11/2015.
 */
public class ParameterBO {
    private String name;
    private String value;
    private String type;//STRING, NUMBER, DATE
    private String separator;
    private String format;

    public ParameterBO() {
    }

    public ParameterBO(String name, String value) {
        this.name = name;
        this.value = value;
    }

    public ParameterBO(String name, String value, String type) {
        this.name = name;
        this.value = value;
        this.type = type;
    }

    public ParameterBO(String name, String value, String type, String separator) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.separator = separator;
    }

    public ParameterBO(String name, String value, String type, String separator, String format) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.separator = separator;
        this.format = format;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getSeparator() {
        return separator;
    }

    public void setSeparator(String separator) {
        this.separator = separator;
    }

    public String getFormat() {
        return format;
    }

    public void setFormat(String format) {
        this.format = format;
    }
}
