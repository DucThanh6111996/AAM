package com.viettel.it.object;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by khiemvk on 10/11/2015.
 */
public class RequestInputBO implements Serializable {
    
    private String code;
    private List<List<ParameterBO>> data;
    private List<ParameterBO> params;
    private String query;

    public RequestInputBO() {
        params = new ArrayList<>();
    }

    public RequestInputBO(String code) {
        params = new ArrayList<>();
        this.code = code;
    }

    public RequestInputBO(String code, List<ParameterBO> params) {
        this.code = code;
        this.params = params;
    }

    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public List<ParameterBO> getParams() {
        return params;
    }

    public void setParams(List<ParameterBO> params) {
        this.params = params;
    }

    public String getQuery() {
        return query;
    }

    public void setQuery(String query) {
        this.query = query;
    }

    public List<List<ParameterBO>> getData() {
        return data;
    }

    public void setData(List<List<ParameterBO>> data) {
        this.data = data;
    }
}
