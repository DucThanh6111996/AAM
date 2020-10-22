package com.viettel.it.model;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "PARAM_EXPORT_DUMP_SIZE_DB")
public class ParamExportDumpSizeDb implements java.io.Serializable{

	private Long id;
	private String logFile;
	private Long paramExportQltnId;
	private Long dbId;
	private Double dumpSize;
	

	public ParamExportDumpSizeDb() {
		super();
	}

    @Column(name = "LOG_FILE",  length=200)
	public String getLogFile() {
		return logFile;
	}

	public void setLogFile(String logFile) {
		this.logFile = logFile;
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

	@Column(name = "DUMP_SIZE", precision = 22, scale = 0)
	public Double getDumpSize() {
		return dumpSize;
	}

	public void setDumpSize(Double dumpSize) {
		this.dumpSize = dumpSize;
	}

	@Id
	 @Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
	 @GeneratedValue(strategy = SEQUENCE, generator = "generator")
	 @SequenceGenerator(name = "generator", sequenceName = "PARAM_EXPORT_DUMP_SIZE_DB_SEQ", allocationSize = 1)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	

}
