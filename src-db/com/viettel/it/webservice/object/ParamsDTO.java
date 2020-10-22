package com.viettel.it.webservice.object;

import java.util.ArrayList;

/**
 * Created by hanh on 4/19/2017.
 */
public class ParamsDTO {

    private ArrayList<ParamValueDTO> lstParamValues = new ArrayList<>();
    private String messages;
    private String command;


    public ParamsDTO() {
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public ArrayList<ParamValueDTO> getLstParamValues() {
        return lstParamValues;
    }

    public void setLstParamValues(ArrayList<ParamValueDTO> lstParamValues) {
        this.lstParamValues = lstParamValues;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }
}
