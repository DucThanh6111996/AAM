package com.viettel.it.model;

import com.viettel.it.persistence.ItActionLogServiceImpl;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Date;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by hanh on 5/15/2017.
 */

@Entity
@Table(name = "IT_ACTION_LOG")
public class ItActionLog implements java.io.Serializable {

    private Long id;
    private Action action;
    private Date startTime;
    private Date endTime;
    private String userRun;
    private Long status;
    private List<ItCommandLog> commandsLog;
    private String description;

    private String socitId;

    //tuanda38_20180914_map param alarm_start
    private String system;
    private Integer isResponse;
    private Long monitorId;
    private String crNumber;
    //tuanda38_20180914_map param alarm_start

    @Id
    @Column(name = "ID", unique = true, nullable = false, precision = 12, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "IT_ACTION_LOG_SEQ", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "START_TIME", length = 7)
    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "END_TIME", length = 7)
    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    @Column(name = "USER_RUN", length = 200)
    public String getUserRun() {
        return userRun;
    }

    public void setUserRun(String userRun) {
        this.userRun = userRun;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ACTION_ID", nullable = false)
    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    @Column(name = "STATUS", precision = 12, scale = 0)
    public Long getStatus() {
        return status;
    }

    public void setStatus(Long status) {
        this.status = status;
    }

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "actionLog")
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<ItCommandLog> getCommandsLog() {
        return commandsLog;
    }

    public void setCommandsLog(List<ItCommandLog> commandsLog) {
        this.commandsLog = commandsLog;
    }

    public static void main(String args[]) {

    }

    @Column(name = "DESCRIPTION", length = 500)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "SOCIT_ID")
    public String getSocitId() {
        return socitId;
    }

    public void setSocitId(String socitId) {
        this.socitId = socitId;
    }

    //tuanda38_20180914_map param alarm_start
    @Column(name = "SYSTEM")
    public String getSystem() {
        return system;
    }

    public void setSystem(String system) {
        this.system = system;
    }

    @Column(name = "IS_RESPONSE")
    public Integer getIsResponse() {
        return isResponse;
    }

    public void setIsResponse(Integer isResponse) {
        this.isResponse = isResponse;
    }

    @Column(name = "MONITOR_ID")
    public Long getMonitorId() {
        return monitorId;
    }

    public void setMonitorId(Long monitorId) {
        this.monitorId = monitorId;
    }

    @Column(name = "CR_NUMBER")
    public String getCrNumber() {
        return crNumber;
    }

    public void setCrNumber(String crNumber) {
        this.crNumber = crNumber;
    }
    //tuanda38_20180914_map param alarm_start
}
