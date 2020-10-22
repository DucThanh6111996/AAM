package com.viettel.it.object;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanhnv68 on 9/12/2017.
 */
public class SidnOpenBlockingObj {
    private Long id;
    private String groupcode;
    private Map<String, String> paramVals = new HashMap<>();

    public SidnOpenBlockingObj() {
    }

    public SidnOpenBlockingObj(Long id, String groupcode, Map<String, String> paramVals) {
        this.id = id;
        this.groupcode = groupcode;
        this.paramVals = paramVals;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getGroupcode() {
        return groupcode;
    }

    public void setGroupcode(String groupcode) {
        this.groupcode = groupcode;
    }

    public Map<String, String> getParamVals() {
        return paramVals;
    }

    public void setParamVals(Map<String, String> paramVals) {
        this.paramVals = paramVals;
    }
}
