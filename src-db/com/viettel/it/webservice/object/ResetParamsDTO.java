package com.viettel.it.webservice.object;

import com.viettel.passprotector.PassProtector;

import java.util.ArrayList;

/**
 * Created by hanh on 4/19/2017.
 */
public class ResetParamsDTO {

    private ArrayList<ParamValsDTO> lstParamValOfNode = new ArrayList<>();
    private String messages;

    public ResetParamsDTO() {
    }

    public ResetParamsDTO(ArrayList<ParamValsDTO> lstParamValOfNode) {
        this.lstParamValOfNode = lstParamValOfNode;
    }

    public String getMessages() {
        return messages;
    }

    public void setMessages(String messages) {
        this.messages = messages;
    }

    public ArrayList<ParamValsDTO> getLstParamValOfNode() {
        return lstParamValOfNode;
    }

    public void setLstParamValOfNode(ArrayList<ParamValsDTO> lstParamValOfNode) {
        this.lstParamValOfNode = lstParamValOfNode;
    }

//    public static void main(String args[]) {
//        try {
//           System.out.println(PassProtector.decrypt("nIrui5TseRBbWAXxJ9C0GKr/iWY7Z7UbXIX07dNByyA4wBKdJSIW5GpoUcfNCMl4jO+ArCoa3xhM6PiiN9btlQ==", "ipchange"));
//            System.out.println(PassProtector.decrypt("OGQz2RMz+woYW36dBTkd6ciJhhbvNNN7GZbFT7SdUMFytqNI4t4Z6yV3Xg4nl2Rp", "ipchange"));
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
}
