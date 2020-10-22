package com.viettel.model;

import com.viettel.bean.ModuleChecklist;
import com.viettel.controller.ServerChecklist;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "SPCL_NEW_KPI_SERVER_SETTING")
public class KpiServerSetting implements Serializable {
	private static Logger logger = LogManager.getLogger(KpiServerSetting.class);
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	private Long id;
	private Long groupId;
	private Long appId;
	private Long kpiId;
	private Integer mathOption;
	private String stringDefaultValue ;
	private Float numberDefaultValue = 0F;
	private Long unitId;

/*	private Module rstApp;
	private RstKpi rstKpi;*/
	private String exceptionValue;

	private List<ModuleChecklist> moduleChecklists;
	private List<ServerChecklist> serverChecklists;

	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "SPCL_NEW_SERVER_SETTING_SEQ")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "APP_ID")
	public Long getAppId() {
		return appId;
	}
	public void setAppId(Long appId) {
		this.appId = appId;
	}

	@Column(name = "KPI_ID")
	public Long getKpiId() {
		return kpiId;
	}
	public void setKpiId(Long kpiId) {
		this.kpiId = kpiId;
	}

	@Column(name = "MATH_OPTION")
	public Integer getMathOption() {
		return mathOption;
	}
	public void setMathOption(Integer mathOption) {
		this.mathOption = mathOption;
	}

	@Column(name = "STRING_DEFAULT_VALUE")
	public String getStringDefaultValue() {
		return stringDefaultValue;
	}
	public void setStringDefaultValue(String stringDefaultValue) {
		this.stringDefaultValue = stringDefaultValue;
	}

	@Column(name = "NUMBER_DEFAULT_VALUE")
	public Float getNumberDefaultValue() {
		return numberDefaultValue;
	}
	public void setNumberDefaultValue(Float numberDefaultValue) {
		this.numberDefaultValue = numberDefaultValue;
	}

	@Column(name = "GROUP_ID")
	public Long getGroupId() {
		return groupId;
	}
	public void setGroupId(Long groupId) {
		this.groupId = groupId;
	}

	@Column(name = "UNIT_ID")
	public Long getUnitId() {
		return unitId;
	}
	public void setUnitId(Long unitId) {
		this.unitId = unitId;
	}

	@Column(name = "EXCEPTION_VALUE")
	public String getExceptionValue() {
		return exceptionValue;
	}
	public void setExceptionValue(String exceptionValue) {
		this.exceptionValue = exceptionValue;
	}

	@Transient
	public List<ModuleChecklist> getModuleChecklists() {
		return moduleChecklists;
	}

	public void setModuleChecklists(List<ModuleChecklist> moduleChecklists) {
		this.moduleChecklists = moduleChecklists;
	}

	@Transient
	public List<ServerChecklist> getServerChecklists() {
		return serverChecklists;
	}

	public void setServerChecklists(List<ServerChecklist> serverChecklists) {
		this.serverChecklists = serverChecklists;
	}


	/*@Transient
	public Module getRstApp() {
		try {
			if(this.rstApp==null && this.appId!=null ){
				this.rstApp=(new IimServiceImpl()).findModuleById(this.appId);
			}
			return ( this.rstApp ==null ? new  Module() :  this.rstApp);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new Module();
		}
	}
	public void setRstApp(Module rstApp) {
		this.rstApp = rstApp;
	}


	@Transient
	public RstKpi getRstKpi() {
		try {
			if(this.rstKpi==null && this.kpiId!=null ){
				this.rstKpi=(new RstKpiDaoImpl()).findById(this.kpiId);
			}
			return ( this.rstKpi ==null ? new  RstKpi() :  this.rstKpi);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new RstKpi();
		}
	}
	public void setRstKpi(RstKpi rstKpi) {
		this.rstKpi = rstKpi;
	}*/
}
