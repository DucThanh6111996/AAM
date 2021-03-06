package com.viettel.it.model;

// Generated Sep 8, 2016 5:07:30 PM by Hibernate Tools 4.0.0

import com.viettel.it.persistence.ParamInputServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * ParamInput generated by hbm2java
 */
@Entity
@Table(name = "PARAM_INPUT")
public class ParamInput implements java.io.Serializable {
	private static Logger logger = LogManager.getLogger(ParamInput.class);

	private Long paramInputId;
	private CommandDetail commandDetail;
	private String paramCode;
	private Long paramType;
	private Long isActive;
	private String userName;
	private Date createTime;
	private String paramDefault;
	private Boolean readOnly;
    private String paramValue;
	private List<ParamGroup> paramGroups = new ArrayList<ParamGroup>(0);

	private Boolean referentce=false;
	private String color = "#c1c2c3";
	private List<ParamInOut> paramInOuts = new ArrayList<ParamInOut>(0);
	private Boolean inOut = false;
	private String cmdInOut;

	private String paramFormula;
	private String description;

	private Boolean isFormula = false;
	private Boolean isDeclare;

	/*20190408_chuongtq start check param when create MOP*/
	private List<ParamCondition> paramConditions = new ArrayList<>(0);
	/*20190408_chuongtq end check param when create MOP*/

//	private AutoConfigMopParams configParam;

    public ParamInput() {
    }

	public ParamInput(String paramCode, String paramValue) {
		this.paramCode = paramCode;
		this.paramValue = paramValue;
	}

	@Id
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "PARAM_INPUT_seq", allocationSize = 1)
    @Column(name = "PARAM_INPUT_ID", unique = true, nullable = false, precision = 10, scale = 0)
    public Long getParamInputId() {
        return this.paramInputId;
    }

    public void setParamInputId(Long paramInputId) {
        this.paramInputId = paramInputId;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.TRUE)
    @JoinColumn(name = "CMD_DETAIL_ID", nullable = false)
    public CommandDetail getCommandDetail() {
        return this.commandDetail;
    }

    public void setCommandDetail(CommandDetail commandDetail) {
        this.commandDetail = commandDetail;
    }

    @Column(name = "PARAM_CODE", nullable = false, length = 100)
    public String getParamCode() {
        return this.paramCode;
    }

    public void setParamCode(String paramCode) {
        this.paramCode = paramCode;
    }

    @Column(name = "PARAM_TYPE", nullable = false, precision = 1, scale = 0)
    public Long getParamType() {
        return this.paramType;
    }

    public void setParamType(Long paramType) {
        this.paramType = paramType;
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

    @Column(name = "PARAM_DEFAULT", nullable = true)
    public String getParamDefault() {
        return this.paramDefault;
    }

	@Column(name = "READ_ONLY", nullable = false, precision = 1, scale = 0)
	public Boolean getReadOnly() {
		return this.readOnly;
	}

	public void setReadOnly(Boolean readOnly) {
		this.readOnly = readOnly;
	}

	@Column(name = "DESCRIPTION", nullable = true)
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(name = "PARAM_FORMULA", nullable = true)
	public String getParamFormula() {
		return paramFormula;
	}

	public void setParamFormula(String paramFormula) {
		this.paramFormula = paramFormula;
	}


	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((paramInputId == null) ? 0 : paramInputId.hashCode());
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
		ParamInput other = (ParamInput) obj;
		if (paramInputId == null) {
			if (other.paramInputId != null)
				return false;
		} else if (!paramInputId.equals(other.paramInputId))
			return false;
		return true;
	}
	

	@Fetch(FetchMode.SELECT)
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "paramInput", cascade= CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<ParamGroup> getParamGroups() {
		return this.paramGroups;
	}

	public void setParamGroups(List<ParamGroup> paramGroups) {
		this.paramGroups = paramGroups;
	}
//
//	@OneToMany(fetch = FetchType.EAGER, mappedBy = "paramInput")
//	public List<ParamValue> getParamValues() {
//		return this.paramValues;
//	}
//
//	public void setParamValues(List<ParamValue> paramValues) {
//		this.paramValues = paramValues;
//	}


	public void setParamDefault(String paramDefault) {
		this.paramDefault = paramDefault;
	}

    @Transient
    public String getParamValue() {
        return paramValue;
    }

    public void setParamValue(String paramValue) {
        this.paramValue = paramValue;
    }


    @Transient
	public Boolean isReferentce() {
		return referentce;
	}


	public void setReferentce(Boolean referentce) {
		this.referentce = referentce;
	}


	@Transient
	public String getColor() {
		return color;
	}


	public void setColor(String color) {
		this.color = color;
	}

	@Fetch(FetchMode.SELECT)
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "paramInput", cascade= CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<ParamInOut> getParamInOuts() {
		return this.paramInOuts;
	}

	public void setParamInOuts(List<ParamInOut> paramInOuts) {
		this.paramInOuts = paramInOuts;
	}


	@Transient
	public Boolean getInOut() {
		return inOut;
	}


	public void setInOut(Boolean inOutCmd) {
		this.inOut = inOutCmd;
	}


	@Transient
	public String getCmdInOut() {
		return cmdInOut;
	}


	public void setCmdInOut(String cmdInOut) {
		this.cmdInOut = cmdInOut;
	}

	@Transient
	public Boolean getIsFormula() {
		isFormula = paramFormula != null && !paramFormula.trim().isEmpty();
		return isFormula;
	}

	public void setIsFormula(Boolean isFormula) {
		this.isFormula = isFormula;
	}


	@Transient
	public Boolean getIsDeclare() {
		return isDeclare;
	}

	public void setIsDeclare(Boolean isDeclare) {
		this.isDeclare = isDeclare;
	}

	/*20190408_chuongtq_ start check param when create MOP*/
	@Fetch(FetchMode.SELECT)
	@OneToMany(fetch = FetchType.EAGER, mappedBy = "paramInput", cascade = CascadeType.ALL)
	@LazyCollection(LazyCollectionOption.EXTRA)
	public List<ParamCondition> getParamConditions() {
		return paramConditions;
	}

	public void setParamConditions(List<ParamCondition> paramConditions) {
		this.paramConditions = paramConditions;
	}
    /*20190408_chuongtq end check param when create MOP*/

}
