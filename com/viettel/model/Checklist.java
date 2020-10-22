package com.viettel.model;

// Created May 13, 2016 9:51:54 AM by quanns2

import com.viettel.bean.ChecklistResult;
import com.viettel.controller.Module;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

/**
 * @author quanns2
 */
@Entity
@Table(name = "SPCL_NEW_KPI")
public class Checklist implements java.io.Serializable {

	private Long id;
	private String code;
	private String name;
	private Integer type;
	private Integer checkoutType;
	private Float numberDefaultValue;
	private Integer defaultMathOption;
	private String exceptionValue;
	private String stringDefaultValue;

	private Long moduleId;

	private ChecklistResult result;
	private Module module;

	/*20181116_hoangnd_save all step_start*/
	private Long actionModuleChecklistId;
	private Long actionDbChecklistId;
	private String checklistType;

	private String statusBefore;
	private String statusImpact;
	private String statusAfter;
	private String statusRollback;
	private String resultBefore;
	private String resultImpact;
	private String resultAfter;
	private String resultRollback;
	private String limitedBefore;
	private String limitedImpact;
	private String limitedAfter;
	private String limitedRollback;
	/*20181116_hoangnd_save all step_end*/

	public Checklist() {
	}

	public Checklist(Long id) {
		this.id = id;
	}

	public Checklist(Long id, String code, String name, Integer type) {
		this.id = id;
		this.code = code;
		this.name = name;
		this.type = type;
	}

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 20, scale = 0)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "CODE", length = 200)
	public String getCode() {
		return this.code;
	}

	public void setCode(String code) {
		this.code = code;
	}

	@Column(name = "NAME", length = 200)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "TYPE", precision = 10, scale = 0)
	public Integer getType() {
		return this.type;
	}

	public void setType(Integer type) {
		this.type = type;
	}

	@Column(name = "CHECKOUT_TYPE", precision = 22, scale = 0)
	public Integer getCheckoutType() {
		return this.checkoutType;
	}

	public void setCheckoutType(Integer checkoutType) {
		this.checkoutType = checkoutType;
	}

	@Column(name = "NUMBER_DEFAULT_VALUE")
	public Float getNumberDefaultValue() {
		return this.numberDefaultValue;
	}

	public void setNumberDefaultValue(Float numberDefaultValue) {
		this.numberDefaultValue = numberDefaultValue;
	}

	@Column(name = "DEFAULT_MATH_OPTION", precision = 22, scale = 0)
	public Integer getDefaultMathOption() {
		return this.defaultMathOption;
	}

	public void setDefaultMathOption(Integer defaultMathOption) {
		this.defaultMathOption = defaultMathOption;
	}

	@Column(name = "EXCEPTION_VALUE", length = 4000)
	public String getExceptionValue() {
		return this.exceptionValue;
	}

	public void setExceptionValue(String exceptionValue) {
		this.exceptionValue = exceptionValue;
	}

	@Column(name = "STRING_DEFAULT_VALUE")
	public String getStringDefaultValue() {
		return this.stringDefaultValue;
	}

	public void setStringDefaultValue(String stringDefaultValue) {
		this.stringDefaultValue = stringDefaultValue;
	}

	@Transient
	public Long getModuleId() {
		return moduleId;
	}

	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}

	@Transient
	public Module getModule() {
		return module;
	}

	public void setModule(Module module) {
		this.module = module;
	}

	@Transient
	public ChecklistResult getResult() {
		return result;
	}

	public void setResult(ChecklistResult result) {
		this.result = result;
	}

	/*20181116_hoangnd_save all step_start*/
	@Transient
	public Long getActionModuleChecklistId() {
		return actionModuleChecklistId;
	}

	public void setActionModuleChecklistId(Long actionModuleChecklistId) {
		this.actionModuleChecklistId = actionModuleChecklistId;
	}

	@Transient
	public Long getActionDbChecklistId() {
		return actionDbChecklistId;
	}

	public void setActionDbChecklistId(Long actionDbChecklistId) {
		this.actionDbChecklistId = actionDbChecklistId;
	}

	@Transient
	public String getChecklistType() {
		return checklistType;
	}

	public void setChecklistType(String checklistType) {
		this.checklistType = checklistType;
	}

	@Transient
	public String getStatusBefore() {
		return statusBefore;
	}

	public void setStatusBefore(String statusBefore) {
		this.statusBefore = statusBefore;
	}

	@Transient
	public String getStatusImpact() {
		return statusImpact;
	}

	public void setStatusImpact(String statusImpact) {
		this.statusImpact = statusImpact;
	}

	@Transient
	public String getStatusAfter() {
		return statusAfter;
	}

	public void setStatusAfter(String statusAfter) {
		this.statusAfter = statusAfter;
	}

	@Transient
	public String getStatusRollback() {
		return statusRollback;
	}

	public void setStatusRollback(String statusRollback) {
		this.statusRollback = statusRollback;
	}

	@Transient
	public String getResultBefore() {
		return resultBefore;
	}

	public void setResultBefore(String resultBefore) {
		this.resultBefore = resultBefore;
	}

	@Transient
	public String getResultImpact() {
		return resultImpact;
	}

	public void setResultImpact(String resultImpact) {
		this.resultImpact = resultImpact;
	}

	@Transient
	public String getResultAfter() {
		return resultAfter;
	}

	public void setResultAfter(String resultAfter) {
		this.resultAfter = resultAfter;
	}

	@Transient
	public String getResultRollback() {
		return resultRollback;
	}

	public void setResultRollback(String resultRollback) {
		this.resultRollback = resultRollback;
	}

	@Transient
	public String getLimitedBefore() {
		return limitedBefore;
	}

	public void setLimitedBefore(String limitedBefore) {
		this.limitedBefore = limitedBefore;
	}

	@Transient
	public String getLimitedImpact() {
		return limitedImpact;
	}

	public void setLimitedImpact(String limitedImpact) {
		this.limitedImpact = limitedImpact;
	}

	@Transient
	public String getLimitedAfter() {
		return limitedAfter;
	}

	public void setLimitedAfter(String limitedAfter) {
		this.limitedAfter = limitedAfter;
	}

	@Transient
	public String getLimitedRollback() {
		return limitedRollback;
	}

	public void setLimitedRollback(String limitedRollback) {
		this.limitedRollback = limitedRollback;
	}
	/*20181116_hoangnd_save all step_end*/

	/*@Transient
        @JsonProperty(value = "checklistResult")
        @JsonSerialize(using = CklResultSerializer.class)
        @JsonDeserialize(using = CklResultDeserializer.class)*/

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		Checklist checklist = (Checklist) o;

		return new EqualsBuilder()
				.append(id, checklist.id)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(id)
				.toHashCode();
	}

	@Override
	public String toString() {
		return this.code;
	}
}
