package com.viettel.it.object;

/**
 * Created by hanh on 5/10/2017.
 */
public class TbsNodeObj {

    private String tbsName;
    private Integer numOfDatafile;

    public TbsNodeObj(String tbsName, Integer numOfDatafile) {
        this.tbsName = tbsName;
        this.numOfDatafile = numOfDatafile;
    }

    public String getTbsName() {
        return tbsName;
    }

    public void setTbsName(String tbsName) {
        this.tbsName = tbsName;
    }

    public Integer getNumOfDatafile() {
        return numOfDatafile;
    }

    public void setNumOfDatafile(Integer numOfDatafile) {
        this.numOfDatafile = numOfDatafile;
    }
}
