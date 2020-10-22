package com.viettel.it.model;

// Generated Sep 8, 2016 5:07:30 PM by Hibernate Tools 4.0.0

import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.Date;

/**
 * ActionCommand generated by hbm2java
 */
@Entity
@Table(name = "ACTION_COMMAND")
public class ActionCommand implements java.io.Serializable {

    private Long actionCommandId;
    private CommandDetail commandDetail;
    private ActionDetail actionDetail;
    private Long type;
    private Long isActive;
    private String userName;
    private Date createTime;
	private Long orderRun;
	private Long logOrderRun;

    public ActionCommand() {
    }

//	public ActionCommand(Long actionCommandId, CommandDetail commandDetail, ActionDetail actionDetail, Long isActive, String userName, Date createTime) {
//		this.actionCommandId = actionCommandId;
//		this.commandDetail = commandDetail;
//		this.actionDetail = actionDetail;
//		this.isActive = isActive;
//		this.userName = userName;
//		this.createTime = createTime;
//	}
//
//	public ActionCommand(Long actionCommandId, CommandDetail commandDetail, ActionDetail actionDetail, Long type, Long isActive, String userName, Date createTime, Long orderRun) {
//		this.actionCommandId = actionCommandId;
//		this.commandDetail = commandDetail;
//		this.actionDetail = actionDetail;
//		this.type = type;
//		this.isActive = isActive;
//		this.userName = userName;
//		this.createTime = createTime;
//		this.orderRun = orderRun;
//	}

    @Id
    @Column(name = "ACTION_COMMAND_ID", unique = true, nullable = false, precision = 12, scale = 0)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "ACTION_COMMAND_SEQ", allocationSize = 1)
    public Long getActionCommandId() {
        return this.actionCommandId;
    }

    public void setActionCommandId(Long actionCommandId) {
        this.actionCommandId = actionCommandId;
    }

	@Fetch(FetchMode.SELECT)
	@ManyToOne(fetch = FetchType.EAGER)
	@LazyCollection(LazyCollectionOption.EXTRA)
	@JoinColumn(name = "COMMAND_DETAIL_ID", nullable = false)
	public CommandDetail getCommandDetail() {
		return this.commandDetail;
	}

    public void setCommandDetail(CommandDetail commandDetail) {
        this.commandDetail = commandDetail;
    }

	@Fetch(FetchMode.SELECT)
	@ManyToOne(fetch = FetchType.EAGER)
	@LazyCollection(LazyCollectionOption.EXTRA)
	@JoinColumn(name = "ACTION_DETAIL_ID", nullable = false)
	public ActionDetail getActionDetail() {
		return this.actionDetail;
	}

    public void setActionDetail(ActionDetail actionDetail) {
        this.actionDetail = actionDetail;
    }

    @Column(name = "TYPE", precision = 6, scale = 0)
    public Long getType() {
        return this.type;
    }

    public void setType(Long type) {
        this.type = type;
    }

    @Column(name = "IS_ACTIVE", nullable = false, precision = 1, scale = 0)
    public Long getIsActive() {
        return this.isActive;
    }

    public void setIsActive(Long isActive) {
        this.isActive = isActive;
    }

    @Column(name = "USER_NAME", nullable = false, length = 200)
    public String getUserName() {
        return this.userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "CREATE_TIME", nullable = false, length = 7)
    public Date getCreateTime() {
        return this.createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

	@Column(name = "ORDER_RUN", precision = 6, scale = 0)
	public Long getOrderRun() {
		return this.orderRun;
	}

	public void setOrderRun(Long orderRun) {
		this.orderRun = orderRun;
	}

    @Column(name = "LOG_ORDER_RUN", precision = 6, scale = 0)
    public Long getLogOrderRun() {
        return logOrderRun;
    }

    public void setLogOrderRun(Long logOrderRun) {
        this.logOrderRun = logOrderRun;
    }

    // hanhnv68 add 20160913
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result
				+ ((actionCommandId == null) ? 0 : actionCommandId.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ActionCommand other = (ActionCommand) obj;
		if (actionCommandId == null) {
			if (other.actionCommandId != null)
				return false;
		} else if (!actionCommandId.equals(other.actionCommandId))
			return false;
		return true;
	}
	// end hanhnv68 add 20160913
}