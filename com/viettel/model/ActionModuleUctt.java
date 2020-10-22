package com.viettel.model;

import javax.persistence.*;

/**
 * @author quanns2
 */
@Entity
@Table(name = "ACTION_MODULE_UCTT")
public class ActionModuleUctt implements java.io.Serializable {
    private Long id;
    private Long actionId;
    private Long moduleId;

    @Id
    @Column(name = "ID", unique = true, nullable = false, precision = 20, scale = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
    @SequenceGenerator(name = "ID", sequenceName = "ACTION_MODULE_UCTT_SEQ", allocationSize=1)
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

    @Column(name = "MODULE_ID")
    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }
}
