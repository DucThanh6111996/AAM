package com.viettel.it.util;

/**
 * Kết quả của lệnh được gửi đi<br>
 *
 * @author Nguyễn Xuân Huy <huynx6@viettel.com.vn>
 * @sin Oct 3, 2014
 * @
 * @version 1.0
 */
public class Result {

    Boolean isSuccessSent;
    String result;

    public Result() {
        super();
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
}
