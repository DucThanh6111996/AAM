package com.viettel.it.object;

/**
 * Created by hanhnv68 on 7/27/2017.
 */
public class XmlModel {

    private String name;
    private String value;

    public XmlModel(String name, String value) {
        this.name = name;
        this.value = value;
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
}
