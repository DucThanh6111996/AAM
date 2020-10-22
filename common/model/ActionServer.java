package com.viettel.model;

import javax.persistence.*;

/**
 * @author quanns2
 */
@Entity
@Table(name = "ACTION_SERVER")
public class ActionServer implements java.io.Serializable {
    private Long id;
    private Long actionId;
    private String ipServer;
    private String monitorAccount;

    @Id
    @Column(name = "ID", unique = true, nullable = false, precision = 20, scale = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
    @SequenceGenerator(name = "ID", sequenceName = "ACTION_SERVER_SEQ", allocationSize=1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "ACTION_ID")
    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    @Column(name = "IP_SERVER")
    public String getIpServer() {
        return ipServer;
    }

    public void setIpServer(String ipServer) {
        this.ipServer = ipServer;
    }

    @Column(name = "MONITOR_ACCOUNT")
    public String getMonitorAccount() {
        return monitorAccount;
    }

    public void setMonitorAccount(String monitorAccount) {
        this.monitorAccount = monitorAccount;
    }
}
