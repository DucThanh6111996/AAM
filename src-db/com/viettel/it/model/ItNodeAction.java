package com.viettel.it.model;

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by hanh on 5/15/2017.
 */
@Entity
@Table(name = "IT_NODE_ACTION")
public class ItNodeAction implements Serializable {

    private Long id;
    private Long actionId;
    private Long nodeId;
    private Long type;
    private Action actionNode;
    private Node node;
    private List<ItActionAccount> actionAccounts;
    private Long logOrderRun;

    public ItNodeAction() {
    }

    public ItNodeAction(Long id) {
        this.id = id;
    }

    @Id
    @Column(name = "ID", unique = true, nullable = false, precision = 12, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "IT_NODE_ACTION_SEQ", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "ACTION_ID", precision = 12, scale = 0)
    public Long getActionId() {
        return actionId;
    }

    public void setActionId(Long actionId) {
        this.actionId = actionId;
    }

    @Column(name = "NODE_ID", precision = 12, scale = 0)
    public Long getNodeId() {
        return nodeId;
    }

    public void setNodeId(Long nodeId) {
        this.nodeId = nodeId;
    }

    @Column(name = "TYPE", precision = 12, scale = 0)
    public Long getType() {
        return type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "ACTION_ID", nullable = false, insertable = false, updatable = false)
    public Action getActionNode() {
        return actionNode;
    }

    public void setActionNode(Action actionNode) {
        this.actionNode = actionNode;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "NODE_ID", nullable = false, insertable = false, updatable = false)
    public Node getNode() {
        return node;
    }

    public void setNode(Node node) {
        this.node = node;
    }

    @Fetch(FetchMode.SELECT)
    @OneToMany(fetch = FetchType.EAGER, mappedBy = "nodeAction", cascade=CascadeType.ALL)
    @LazyCollection(LazyCollectionOption.EXTRA)
    public List<ItActionAccount> getActionAccounts() {
        return actionAccounts;
    }

    public void setActionAccounts(List<ItActionAccount> actionAccounts) {
        this.actionAccounts = actionAccounts;
    }

    @Column(name = "LOG_ORDER_RUN", precision = 12, scale = 0)
    public Long getLogOrderRun() {
        return logOrderRun;
    }

    public void setLogOrderRun(Long logOrderRun) {
        this.logOrderRun = logOrderRun;
    }
}
