package com.viettel.it.object;

/**
 * Created by hanhnv68 on 7/26/2017.
 */
public class CmdMigreateModel {
    private String actionName;
    private String commands;

    public CmdMigreateModel() {
    }

    public CmdMigreateModel(String actionName, String commands) {
        this.actionName = actionName;
        this.commands = commands;
    }

    public String getActionName() {
        return actionName;
    }

    public void setActionName(String actionName) {
        this.actionName = actionName;
    }

    public String getCommands() {
        return commands;
    }

    public void setCommands(String commands) {
        this.commands = commands;
    }
}
