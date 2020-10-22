package com.viettel.it.model;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by hanh on 4/14/2017.
 */

@Entity
@Table(name = "TEMPLATE_GROUP")
public class TemplateGroup implements java.io.Serializable {

    private Long id;
    private String groupName;

    @SequenceGenerator(name = "generator", sequenceName = "TEMPLATE_GROUP_SEQ", allocationSize = 1)
    @Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "GROUP_NAME", length = 200)
    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }
}
