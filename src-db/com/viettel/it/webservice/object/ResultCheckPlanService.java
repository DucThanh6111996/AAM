package com.viettel.it.webservice.object;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by quytv7 on 7/30/2018.
 */
public class ResultCheckPlanService {
    private List<CheckPlanServiceObj> CheckPlanServiceObjs = new ArrayList<>();
    private int status;
    private int resultCode;
    private String resultDetail = "";

    public List<CheckPlanServiceObj> getCheckPlanServiceObjs() {
        return CheckPlanServiceObjs;
    }

    public void setCheckPlanServiceObjs(List<CheckPlanServiceObj> checkPlanServiceObjs) {
        CheckPlanServiceObjs = checkPlanServiceObjs;
    }

    public int getStatus() {
        return status;
    }

    public void setStatus(int status) {
        this.status = status;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultDetail() {
        return resultDetail;
    }

    public void setResultDetail(String resultDetail) {
        this.resultDetail = resultDetail;
    }
}
