package com.viettel.it.model;

import javax.persistence.*;
import java.util.Date;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by hanh on 5/15/2017.
 */

@Entity
@Table(name = "IT_COMMAND_LOG")
public class ItCommandLog implements java.io.Serializable {

    private Long id;
    private ItActionLog actionLog;
    private String command;
    private String log;
    private Long orderRun;
    private Date insertTime;
    private Long logType;
    private CommandDetail commandDetail;

    @Id
    @Column(name = "ID", unique = true, nullable = false, precision = 12, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "IT_COMMAND_LOG_SEQ", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ACTION_LOG_ID", nullable = false)
    public ItActionLog getActionLog() {
        return actionLog;
    }

    public void setActionLog(ItActionLog actionLog) {
        this.actionLog = actionLog;
    }

    @Column(name = "COMMAND", length = 2000)
    public String getCommand() {
        return command;
    }

    public void setCommand(String command) {
        this.command = command;
    }

    @Column(name = "LOG")
    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    @Column(name = "ORDER_RUN", precision = 12, scale = 0)
    public Long getOrderRun() {
        return orderRun;
    }

    public void setOrderRun(Long orderRun) {
        this.orderRun = orderRun;
    }

    @Column(name = "LOG_TYPE", precision = 12, scale = 0)
    public Long getLogType() {
        return logType;
    }

    public void setLogType(Long logType) {
        this.logType = logType;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "INSERT_TIME", length = 7)
    public Date getInsertTime() {
        return insertTime;
    }

    public void setInsertTime(Date insertTime) {
        this.insertTime = insertTime;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "COMMAND_DETAIL_ID", nullable = true)
    public CommandDetail getCommandDetail() {
        return commandDetail;
    }

    public void setCommandDetail(CommandDetail commandDetail) {
        this.commandDetail = commandDetail;
    }
}
