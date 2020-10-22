/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.viettel.dao;

/**
 * @author Pham Manh Hung
 */
public class SqlParam {

    private String name = null;
    private Object object = null;

    public SqlParam() {
        this.name = null;
        this.object = null;
    }

    public SqlParam(String name, Object object) {
        this.name = name;
        this.object = object;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setObject(Object object) {
        this.object = object;
    }

    public String getName() {
        return name;
    }

    public Object getObject() {
        return object;
    }


}
