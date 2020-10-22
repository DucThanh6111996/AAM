package com.viettel.it.model;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by hanh on 5/15/2017.
 */

@Entity
@Table(name = "IT_ACTION")
public class ItAction implements java.io.Serializable {

    private Long actionId;
    private String description;
    private String name;
    private Long parentId;


    @Id
    @Column(name = "ACTION_ID", unique = true, nullable = false, precision = 12, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "ACTION_ID_SEQ", allocationSize = 1)
    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    @Column(name = "DESCRIPTION", length = 200)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Column(name = "NAME", length = 200)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "PARENT_ID", precision = 12, scale = 0)
    public Long getParentId() {
        return parentId;
    }

    public void setParentId(Long parentId) {
        this.parentId = parentId;
    }
}
