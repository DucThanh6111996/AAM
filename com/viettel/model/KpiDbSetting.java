package com.viettel.model;

import com.viettel.bean.ChecklistResult;
import com.viettel.bean.ServiceDatabase;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "SPCL_NEW_KPI_DB_SETTING")
public class KpiDbSetting implements Serializable {
	private static Logger logger = LogManager.getLogger(KpiDbSetting.class);

	private Long id;
    private Long viewDbId;
    private Long kpiId;	
	private String sqlCommand;
	private Integer mathOption;
	private Long defaultvalue =0L;	
	private Long appGroupId;
	private Long unitId;
	private Integer dataColumnIndex;

	private ServiceDatabase serviceDatabase;
	private Checklist kpi;

	private ChecklistResult result;

	@Id
	@Column(name = "ID", unique = true, nullable = false)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "SPCL_NEW_DB_SETTING_SEQ")
	public Long getId() {
		return id;
	}
	public void setId(Long id) {
		this.id = id;
	}
	
	@Column(name = "KPI_ID")
	public Long getKpiId() {
		return kpiId;
	}
	public void setKpiId(Long kpiId) {
		this.kpiId = kpiId;
	}
	
	@Column(name = "SQL_COMMAND")
	public String getSqlCommand() {
		return sqlCommand;
	}
	public void setSqlCommand(String sqlCommand) {
		this.sqlCommand = sqlCommand;
	}
	
	@Column(name = "MATH_OPTION")
	public Integer getMathOption() {
		return mathOption;
	}
	public void setMathOption(Integer mathOption) {
		this.mathOption = mathOption;
	}
	
	@Column(name = "DEFAULT_VALUE")
	public Long getDefaultvalue() {
		return defaultvalue;
	}
	public void setDefaultvalue(Long defaultvalue) {
		this.defaultvalue = defaultvalue;
	}
		
	@Column(name = "VIEW_DB_ID")
	public Long getViewDbId() {
		return viewDbId;
	}
	public void setViewDbId(Long viewDbId) {
		this.viewDbId = viewDbId;
	}
	
	@Column(name = "APP_GROUP_ID")
	public Long getAppGroupId() {
		return appGroupId;
	}
	public void setAppGroupId(Long appGroupId) {
		this.appGroupId = appGroupId;
	}

	@Column(name = "UNIT_ID")
	public Long getUnitId() {
		return unitId;
	}

	public void setUnitId(Long unitId) {
		this.unitId = unitId;
	}

	@Column(name = "DATA_COLUMN_INDEX")
	public Integer getDataColumnIndex() {
		return dataColumnIndex;
	}

	public void setDataColumnIndex(Integer dataColumnIndex) {
		this.dataColumnIndex = dataColumnIndex;
	}

	/*@Transient
	public ServiceDatabase getViewDb() {
		try {
			if(this.viewDb==null && this.viewDbId!=null ){
				this.viewDb=(new IimClientServiceImpl()).findServiceDbById(this.viewDbId);
			}
			return ( this.viewDb ==null ? new ServiceDatabase() :  this.viewDb);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new ServiceDatabase();
		}
	}*/




	@Transient
	public ChecklistResult getResult() {
		return result;
	}

	public void setServiceDatabase(ServiceDatabase serviceDatabase) {
		this.serviceDatabase = serviceDatabase;
	}

	@Transient
	public ServiceDatabase getServiceDatabase() {
		return serviceDatabase;
	}

	public void setResult(ChecklistResult result) {
		this.result = result;
	}

	@ManyToOne()
	@LazyCollection(LazyCollectionOption.FALSE)
	@JoinColumn(name = "KPI_ID", insertable = false, updatable = false)
	public Checklist getKpi() {
		return kpi;
	}

	public void setKpi(Checklist kpi) {
		this.kpi = kpi;
	}

/*	@Transient
	public Checklist getKpi() {
		try {
			if(this.kpi==null && this.kpiId!=null ) {
				this.kpi=(new RstKpiDaoImpl()).findById(this.kpiId);
			}
			return ( this.kpi ==null ? new Checklist() :   this.kpi);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return new Checklist();
		}
	}*/

/*	@Transient
	public String getMathOptionStr() {
		return Constant.getOperationMap().get(mathOption);
	}*/
	
}
