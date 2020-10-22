package com.viettel.it.model;

// Generated Sep 8, 2016 5:07:30 PM by Hibernate Tools 4.0.0

import com.viettel.model.CatCountryBO;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Node generated by hbm2java
 */
@Entity
@Table(name = "NODE_ACCOUNT")
public class NodeAccount implements java.io.Serializable {

	private Long id;
	private String username;
	private String password;
	private String shell;
	private Long serverId;
	private Long accountType; // server = 1, database = 2 , webservice = 3, provisoning = 4
	private Long impactOrMonitor; // impact = 1, monitor = 2
	private Long itBusinessNode; // 1 -> itbusiness node account
	private Long version; // 1 oracle

	private String usernameType;

	private Node nodeInfo;

	/*20181225_hoangnd_them thi truong_start*/
	private CatCountryBO countryCode;

	@ManyToOne(fetch = FetchType.EAGER)
	@LazyCollection(LazyCollectionOption.EXTRA)
	@JoinColumn(name = "COUNTRY_CODE")
	public CatCountryBO getCountryCode() {
		return this.countryCode;
	}
	public void setCountryCode(CatCountryBO countryCode) {
		this.countryCode = countryCode;
	}
    /*20181225_hoangnd_them thi truong_end*/

	//20180831_tudn_start cap nhat trang thai
	private Long active;

	@Column(name = "ACTIVE", precision = 22, scale = 0)
	public Long getActive() {
		return active;
	}

	public void setActive(Long active) {
		this.active = active;
	}
	//20180831_tudn_end cap nhat trang thai

	
	public NodeAccount() {
		super();
	}

	@Id
    @Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "NODE_ACCOUNT_SEQ", allocationSize = 1)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}


	@Column(name = "USERNAME", length = 200)
	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	@Column(name = "PASSWORD", length = 200)
	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	@Column(name = "SHELL", length = 200)
	public String getShell() {
		return shell;
	}

	public void setShell(String shell) {
		this.shell = shell;
	}

	@Column(name = "SERVER_ID", precision = 22, scale = 0)
	public Long getServerId() {
		return serverId;
	}

	public void setServerId(Long serverId) {
		this.serverId = serverId;
	}

	@Column(name = "ACCOUNT_TYPE", precision = 22, scale = 0)
	public Long getAccountType() {
		return accountType;
	}

	public void setAccountType(Long accountType) {
		this.accountType = accountType;
	}

	@Column(name = "IMPACT_OR_MONITOR", precision = 22, scale = 0)
	public Long getImpactOrMonitor() {
		return impactOrMonitor;
	}

	public void setImpactOrMonitor(Long impactOrMonitor) {
		this.impactOrMonitor = impactOrMonitor;
	}

	@Column(name = "IT_BUSINESS_NODE", precision = 22, scale = 0)
	public Long getItBusinessNode() {
		return itBusinessNode;
	}

	@Column(name = "VERSION", precision = 22, scale = 0)
	public void setItBusinessNode(Long itBusinessNode) {
		this.itBusinessNode = itBusinessNode;
	}

	@Column(name = "VERSION", precision = 22, scale = 0)
	public Long getVersion() {
		return version;
	}

	public void setVersion(Long version) {
		this.version = version;
	}

	@Transient
	public String getUsernameType() {
		return usernameType;
	}

	public void setUsernameType(String usernameType) {
		this.usernameType = usernameType;
	}

	@Transient
	public Node getNodeInfo() {
		return nodeInfo;
	}

	public void setNodeInfo(Node nodeInfo) {
		this.nodeInfo = nodeInfo;
	}

	@Override
	public String toString() {
		return "NodeAccount{" +
				"id=" + id +
				", username='" + username + '\'' +
				", password='" + password + '\'' +
				", shell='" + shell + '\'' +
				", serverId=" + serverId +
				", accountType=" + accountType +
				", impactOrMonitor=" + impactOrMonitor +
				", itBusinessNode=" + itBusinessNode +
				", version=" + version +
				", usernameType='" + usernameType + '\'' +
				'}';
	}
}