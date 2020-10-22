package com.viettel.it.webservice.object;

import java.util.ArrayList;

/**
 * Created by hanh on 4/20/2017.
 */
public class ResetFlowTemplatesDTO {

    private String networkType;
    private String vendor;
    private String message;
    private ArrayList<ResetMopGroupDTO> mopGroupsDTO = new ArrayList<>();

    public ResetFlowTemplatesDTO() {
    }

    public ArrayList<ResetMopGroupDTO> getMopGroupsDTO() {
        return mopGroupsDTO;
    }

    public void setMopGroupsDTO(ArrayList<ResetMopGroupDTO> mopGroupsDTO) {
        this.mopGroupsDTO = mopGroupsDTO;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public String getNetworkType() {
        return networkType;
    }

    public void setNetworkType(String networkType) {
        this.networkType = networkType;
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }
}
