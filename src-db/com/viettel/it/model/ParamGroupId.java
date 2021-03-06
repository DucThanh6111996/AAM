package com.viettel.it.model;

// Generated Sep 8, 2016 5:07:30 PM by Hibernate Tools 4.0.0

import javax.persistence.Column;
import javax.persistence.Embeddable;

/**
 * ParamGroupId generated by hbm2java
 */
@Embeddable
public class ParamGroupId implements java.io.Serializable {

	private Long paramInputId;
	private Long flowTemplateId;
	

	public ParamGroupId() {
	}

	public ParamGroupId(Long paramInputId, Long flowTemplateId) {
		this.paramInputId = paramInputId;
		this.flowTemplateId = flowTemplateId;
	}

	@Column(name = "PARAM_INPUT_ID", nullable = false, precision = 22, scale = 0)
	public Long getParamInputId() {
		return this.paramInputId;
	}

	public void setParamInputId(Long paramInputId) {
		this.paramInputId = paramInputId;
	}

	@Column(name = "FLOW_TEMPLATE_ID", nullable = false, precision = 22, scale = 0)
	public Long getFlowTemplateId() {
		return this.flowTemplateId;
	}

	public void setFlowTemplateId(Long flowTemplateId) {
		this.flowTemplateId = flowTemplateId;
	}

	

	@Override
	public boolean equals(Object other) {
		if ((this == other))
			return true;
		if ((other == null))
			return false;
		if (!(other instanceof ParamGroupId))
			return false;
		ParamGroupId castOther = (ParamGroupId) other;

		return ((this.getParamInputId() == castOther.getParamInputId()) || (this.getParamInputId() != null && castOther.getParamInputId() != null && this.getParamInputId().equals(
				castOther.getParamInputId())))
				&& ((this.getFlowTemplateId() == castOther.getFlowTemplateId()) || (this.getFlowTemplateId() != null && castOther.getFlowTemplateId() != null && this.getFlowTemplateId().equals(
						castOther.getFlowTemplateId())));
	}

	@Override
	public int hashCode() {
		int result = 17;

		result = 37 * result + (getParamInputId() == null ? 0 : this.getParamInputId().hashCode());
		result = 37 * result + (getFlowTemplateId() == null ? 0 : this.getFlowTemplateId().hashCode());
		return result;
	}

}
