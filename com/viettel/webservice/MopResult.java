package com.viettel.webservice;

import java.util.List;

/**
 * @author quanns2
 */
public class MopResult implements java.io.Serializable {
    private List<MopInfo> mopInfos;

    private String message;

    private Integer status;

    public MopResult() {
    }

    public List<MopInfo> getMopInfos() {
        return mopInfos;
    }

    public void setMopInfos(List<MopInfo> mopInfos) {
        this.mopInfos = mopInfos;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }
}
