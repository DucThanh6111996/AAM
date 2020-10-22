package com.viettel.util;

import java.io.OutputStream;

/**
 *@author quanns2
 */
public class Result {
    Boolean isSuccessSent;
    String result;
    OutputStream out;

    public Result() {
        super();
        // TODO Auto-generated constructor stub
    }

    public Boolean isSuccessSent() {
        return isSuccessSent;
    }

    public void setSuccessSent(Boolean isSuccessSent) {
        this.isSuccessSent = isSuccessSent;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public OutputStream getOut() {
        return out;
    }

    public void setOut(OutputStream out) {
        this.out = out;
    }

}