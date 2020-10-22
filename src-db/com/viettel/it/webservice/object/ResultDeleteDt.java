package com.viettel.it.webservice.object;

import java.util.List;

/**
 * Created by VTN-PTPM-NV55 on 5/23/2019.
 */
public class ResultDeleteDt  {
    private int resultCode;
    private String resultMessage;
    private List<DtObjDTO> listDtDelete;

    public ResultDeleteDt() {
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public List<DtObjDTO> getListDtDelete() {
        return listDtDelete;
    }

    public void setListDtDelete(List<DtObjDTO> listDtDelete) {
        this.listDtDelete = listDtDelete;
    }
}
