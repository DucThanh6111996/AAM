package com.viettel.it.webservice.object;

import java.util.List;

/**
 * Created by quytv7 on 3/21/2018.
 */
public class ResultTicketAlarmDTO {
    private String resultMessage;
    private int resultCode;
    private List<TicketAlarmDTO> listTicketAlarmDTO;

    public String getResultMessage() {
        return resultMessage;
    }

    public void setResultMessage(String resultMessage) {
        this.resultMessage = resultMessage;
    }

    public int getResultCode() {
        return resultCode;
    }

    public void setResultCode(int resultCode) {
        this.resultCode = resultCode;
    }

    public List<TicketAlarmDTO> getListTicketAlarmDTO() {
        return listTicketAlarmDTO;
    }

    public void setListTicketAlarmDTO(List<TicketAlarmDTO> listTicketAlarmDTO) {
        this.listTicketAlarmDTO = listTicketAlarmDTO;
    }
}
