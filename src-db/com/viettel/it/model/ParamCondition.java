package com.viettel.it.model;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

/**
 * Created by VTN-PTPM-NV56 on 4/8/2019.
 */
@Entity
@Table(name = "PARAM_CONDITION")
public class ParamCondition implements java.io.Serializable {

    private Long id;
    private ParamGroupId paramGroupId;
    private ParamInput paramInput;
    private FlowTemplates flowTemplates;
    private String conditionOperator;
    private String conditionValue;

    public ParamCondition() {
    }

    public ParamCondition(FlowTemplates flowTemplates) {
        this.flowTemplates = flowTemplates;
    }

    public ParamCondition(ParamCondition paramCondition) {
        this.paramInput = paramCondition.getParamInput();
        this.flowTemplates = paramCondition.getFlowTemplates();
        this.conditionOperator = paramCondition.getConditionOperator();
        this.conditionValue = paramCondition.getConditionValue();
    }

    public ParamCondition(ParamInput paramInput, FlowTemplates flowTemplates) {
        this.paramInput = paramInput;
        this.flowTemplates = flowTemplates;
    }

    public ParamCondition(Long id, ParamInput paramInput, FlowTemplates flowTemplates) {
        this.id = id;
        this.paramInput = paramInput;
        this.flowTemplates = flowTemplates;
    }

    @Id
    @Column(name = "id", unique = true, nullable = false, precision = 22, scale = 0)
    @GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "PARAM_CONDITION_SEQ", allocationSize = 1)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "PARAM_INPUT_ID")
    public ParamInput getParamInput() {
        return this.paramInput;
    }

    public void setParamInput(ParamInput paramInput) {
        this.paramInput = paramInput;
    }

    @ManyToOne(fetch = FetchType.EAGER)
    @LazyCollection(LazyCollectionOption.EXTRA)
    @JoinColumn(name = "FLOW_TEMPLATE_ID")
    public FlowTemplates getFlowTemplates() {
        return this.flowTemplates;
    }

    public void setFlowTemplates(FlowTemplates flowTemplates) {
        this.flowTemplates = flowTemplates;
    }

    @Column(name = "CONDITION_OPERATOR")
    public String getConditionOperator() {
        return conditionOperator;
    }

    public void setConditionOperator(String conditionOperator) {
        this.conditionOperator = conditionOperator;
    }

    @Column(name = "CONDITION_VALUE")
    public String getConditionValue() {
        return conditionValue;
    }

    public void setConditionValue(String conditionValue) {
        this.conditionValue = conditionValue;
    }

    @Transient
    public ParamGroupId getParamGroupId() {
        return new ParamGroupId(this.paramInput.getParamInputId(), this.flowTemplates.getFlowTemplatesId());
    }

    public void setParamGroupId(ParamGroupId paramGroupId) {
        this.paramGroupId = new ParamGroupId(this.paramInput.getParamInputId(), this.flowTemplates.getFlowTemplatesId());
    }
}
