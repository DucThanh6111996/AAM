package com.viettel.bean;

import java.util.List;
import java.util.Map;

/**
 * Created by VTN-PTPM-NV36 on 1/3/2019.
 */
public class ResponseBO {
    private List<Map<String, Object>> data;

    public List<Map<String, Object>> getData() {
        return data;
    }

    public void setData(List<Map<String, Object>> data) {
        this.data = data;
    }
}
