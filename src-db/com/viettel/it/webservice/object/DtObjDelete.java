package com.viettel.it.webservice.object;

/**
 * Created by VTN-PTPM-NV55 on 5/30/2019.
 */
public class DtObjDelete{
    private String deleteDtId;

    public DtObjDelete(String deleteDtId) {
        this.deleteDtId = deleteDtId;
    }

    public DtObjDelete() {
    }

    public String getDeleteDtId() {
        return deleteDtId;
    }

    public void setDeleteDtId(String deleteDtId) {
        this.deleteDtId = deleteDtId;
    }
}
