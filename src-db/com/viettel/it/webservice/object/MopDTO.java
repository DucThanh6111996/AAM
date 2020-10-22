/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice.object;

/**
 *
 * @author hienhv4
 */
public class MopDTO {
    private String mopId;
    private String mopName;
    private String templateName;
    private String createTime;

    public String getMopId() {
        return mopId;
    }

    public void setMopId(String mopId) {
        this.mopId = mopId;
    }

    public String getMopName() {
        return mopName;
    }

    public void setMopName(String mopName) {
        this.mopName = mopName;
    }

    public String getTemplateName() {
        return templateName;
    }

    public void setTemplateName(String templateName) {
        this.templateName = templateName;
    }

    public String getCreateTime() {
        return createTime;
    }

    public void setCreateTime(String createTime) {
        this.createTime = createTime;
    }
}
