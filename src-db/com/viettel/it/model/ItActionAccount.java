package com.viettel.it.model;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by hanh on 5/15/2017.
 */

@Entity
@Table(name = "IT_ACTION_ACCOUNT")
public class ItActionAccount implements java.io.Serializable {

    private Long id;
    private Long nodeActionId;
    private Long nodeAccountId;
    private ItNodeAction nodeAction;
    private NodeAccount nodeAccount;

    @Id
    @Column(name = "ID", unique = true, nullable = false, precision = 12, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "IT_ACTION_ACCOUNT_SEQ", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "NODE_ACTION_ID", precision = 12, scale = 0)
    public Long getNodeActionId() {
        return nodeActionId;
    }

    public void setNodeActionId(Long nodeActionId) {
        this.nodeActionId = nodeActionId;
    }

    @Column(name = "NODE_ACCOUNT_ID", precision = 12, scale = 0)
    public Long getNodeAccountId() {
        return nodeAccountId;
    }

    public void setNodeAccountId(Long nodeAccountId) {
        this.nodeAccountId = nodeAccountId;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "NODE_ACTION_ID", nullable = false, insertable = false, updatable = false)
    public ItNodeAction getNodeAction() {
        return nodeAction;
    }

    public void setNodeAction(ItNodeAction nodeAction) {
        this.nodeAction = nodeAction;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "NODE_ACCOUNT_ID", nullable = false, insertable = false, updatable = false)
    public NodeAccount getNodeAccount() {
        return nodeAccount;
    }

    public void setNodeAccount(NodeAccount nodeAccount) {
        this.nodeAccount = nodeAccount;
    }
}
