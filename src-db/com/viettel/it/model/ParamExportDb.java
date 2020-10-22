package com.viettel.it.model;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "PARAM_EXPORT_DB")
public class ParamExportDb implements java.io.Serializable{

	private Long id;
	private Long paramExportQltnId;
	private Long dbId;
	private String owner;
	private String tab;
	private String par;
	private String tablespaceName;
	private String pathDump;
	private String content;
	private String logFile;
	private Long flowRunId;
	private String userFtp;
	private String passFtp;
	private String ipFtp;
	private Integer portFtp;
	private String pathFtp;
	private String passwordRoot;
	private String userSqlPlus;

	public ParamExportDb() {
		super();
	}

	@Id
    @Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "NODE_SEQ", allocationSize = 1)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "PARAM_EXPORT_QLTN_ID", precision = 22, scale = 0)
	public Long getParamExportQltnId() {
		return paramExportQltnId;
	}

	public void setParamExportQltnId(Long paramExportQltnId) {
		this.paramExportQltnId = paramExportQltnId;
	}

	@Column(name = "DB_ID", precision = 22, scale = 0)
	public Long getDbId() {
		return dbId;
	}

	public void setDbId(Long dbId) {
		this.dbId = dbId;
	}

	@Column(name = "OWNER", length = 200)
	public String getOwner() {
		return owner;
	}

	public void setOwner(String owner) {
		this.owner = owner;
	}

	@Column(name = "TAB", length = 200)
	public String getTab() {
		return tab;
	}

	public void setTab(String tab) {
		this.tab = tab;
	}

	@Column(name = "PAR", length = 200)
	public String getPar() {
		return par;
	}

	public void setPar(String par) {
		this.par = par;
	}

	@Column(name = "TABLESPACE_NAME", length = 200)
	public String getTablespaceName() {
		return tablespaceName;
	}

	public void setTablespaceName(String tablespaceName) {
		this.tablespaceName = tablespaceName;
	}

	@Column(name = "PATH_DUMP", length = 200)
	public String getPathDump() {
		return pathDump;
	}

	public void setPathDump(String pathDump) {
		this.pathDump = pathDump;
	}

	@Column(name = "CONTENT", length = 200)
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	@Column(name = "LOG_FILE", length = 200)
	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
	}

	@Column(name = "FLOW_RUN_ID", precision = 22, scale = 0)
	public Long getFlowRunId() {
		return flowRunId;
	}

	public void setFlowRunId(Long flowRunId) {
		this.flowRunId = flowRunId;
	}

	@Column(name = "USER_FTP", length = 200)
	public String getUserFtp() {
		return userFtp;
	}

	public void setUserFtp(String userFtp) {
		this.userFtp = userFtp;
	}

	@Column(name = "PASS_FTP", length = 200)
	public String getPassFtp() {
		return passFtp;
	}

	public void setPassFtp(String passFtp) {
		this.passFtp = passFtp;
	}

	@Column(name = "IP_FTP", length = 200)
	public String getIpFtp() {
		return ipFtp;
	}

	public void setIpFtp(String ipFtp) {
		this.ipFtp = ipFtp;
	}

	@Column(name = "PORT_FTP", precision = 22, scale = 0)
	public Integer getPortFtp() {
		return portFtp;
	}

	public void setPortFtp(Integer portFtp) {
		this.portFtp = portFtp;
	}

	@Column(name = "PATH_FTP", length = 200)
	public String getPathFtp() {
		return pathFtp;
	}

	public void setPathFtp(String pathFtp) {
		this.pathFtp = pathFtp;
	}

	@Column(name = "PASSWORD_ROOT", length = 200)
	public String getPasswordRoot() {
		return passwordRoot;
	}

	public void setPasswordRoot(String passwordRoot) {
		this.passwordRoot = passwordRoot;
	}

	@Column(name = "USER_SQL_PLUS", length = 200)
	public String getUserSqlPlus() {
		return userSqlPlus;
	}

	public void setUserSqlPlus(String userSqlPlus) {
		this.userSqlPlus = userSqlPlus;
	}

}
