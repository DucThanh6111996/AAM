package com.viettel.it.object;

/**
 * Created by hanhnv68 on 7/19/2017.
 */
public class CmdObject {
    private String command;
    private Long writeLogOrder;
    private Long cmdDetailId;
    private Long cmdOrder;

    public CmdObject() {
    }

    public CmdObject(String command, Long writeLogOrder, Long cmdDetailId) {
        this.command = command;
        this.writeLogOrder = writeLogOrder;
        this.cmdDetailId = cmdDetailId;
    }

    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    public Long getWriteLogOrder() {
        return writeLogOrder;
    }

    public void setWriteLogOrder(Long writeLogOrder) {
        this.writeLogOrder = writeLogOrder;
    }

    public Long getCmdDetailId() {
        return cmdDetailId;
    }

    public void setCmdDetailId(Long cmdDetailId) {
        this.cmdDetailId = cmdDetailId;
    }

    public Long getCmdOrder() {
        return cmdOrder;
    }

    public void setCmdOrder(Long cmdOrder) {
        this.cmdOrder = cmdOrder;
    }
}
