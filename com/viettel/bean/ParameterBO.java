package com.viettel.bean;

/**
 * @author quanns2
 */
public class ParameterBO {
    private String name;
    private Object value;
    private String type;//STRING, NUMBER, DATE
    private String separator;
    private String format;
    private String className;

    public ParameterBO() {
    }

    public ParameterBO(String name, Object value) {
        this(name, value, null);
    }

    public ParameterBO(String name, Object value, String type) {
        this(name, value, type, null);
    }

    public ParameterBO(String name, Object value, String type, String separator) {
        this(name, value, type, separator, null);
    }

    public ParameterBO(String name, Object value, String type, String separator, String format) {
        this.name = name;
        this.value = value;
        this.type = type;
        this.separator = separator;
        this.format = format;

        if (value != null && !value.getClass().isPrimitive())
            className = value.getClass().getName();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
        if (value != null && !value.getClass().isPrimitive())
            className = value.getClass().getName();
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

    public void setClassName(String className) {
        this.className = className;
    }

    public String getClassName() {
        return className;
    }
}
