package com.viettel.model;

import javax.persistence.*;

/**
 * @author quanns2
 */
@Entity
@Table(name = "ACTION_DATABASE")
public class ActionDatabase implements java.io.Serializable {
    private Long id;
    private Long actionId;
    private Long dbId;

    @Id
    @Column(name = "ID", unique = true, nullable = false, precision = 20, scale = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
    @SequenceGenerator(name = "ID", sequenceName = "ACTION_DATABASE_SEQ", allocationSize=1)
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

    @Column(name = "DB_ID")
    public Long getDbId() {
        return dbId;
    }

    public void setDbId(Long dbId) {
        this.dbId = dbId;
    }
}
