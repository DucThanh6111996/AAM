package com.viettel.it.object;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanhnv68 on 9/13/2017.
 */
public class OpenBlockingCmdObj {
    private String protocol;
    private List<CmdObject> cmdImpacts = new ArrayList<>();
    private List<CmdObject> cmdLogs = new ArrayList<>();

    public OpenBlockingCmdObj() {
    }

    public OpenBlockingCmdObj(List<CmdObject> cmdImpacts, List<CmdObject> cmdLogs) {
        this.cmdImpacts = cmdImpacts;
        this.cmdLogs = cmdLogs;
    }

    public List<CmdObject> getCmdImpacts() {
        return cmdImpacts;
    }

    public void setCmdImpacts(List<CmdObject> cmdImpacts) {
        this.cmdImpacts = cmdImpacts;
    }

    public List<CmdObject> getCmdLogs() {
        return cmdLogs;
    }

    public void setCmdLogs(List<CmdObject> cmdLogs) {
        this.cmdLogs = cmdLogs;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }
}
