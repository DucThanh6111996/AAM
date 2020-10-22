package com.viettel.it.object;

import com.viettel.it.model.ActionCommand;
import com.viettel.it.model.ActionOfFlow;
import com.viettel.it.model.ParamInput;

public class ParamInOutObject {

	private Long paramInOutId;
	private ParamInput paramInputIn;
	private ActionCommand actionCommandIn;
	private ActionOfFlow actionOfFlowIn;
	private ActionCommand actionCommandOut;
	private ActionOfFlow actionOfFlowOut;
	
	public ParamInOutObject() {
		super();
	}

	public ParamInOutObject(Long paramInOutId, ParamInput paramInputIn,
                            ActionCommand actionCommandIn, ActionOfFlow actionOfFlowIn,
                            ActionCommand actionCommandOut, ActionOfFlow actionOfFlowOut) {
		super();
		this.paramInOutId = paramInOutId;
		this.paramInputIn = paramInputIn;
		this.actionCommandIn = actionCommandIn;
		this.actionOfFlowIn = actionOfFlowIn;
		this.actionCommandOut = actionCommandOut;
		this.actionOfFlowOut = actionOfFlowOut;
	}

	public Long getParamInOutId() {
		return paramInOutId;
	}

	public void setParamInOutId(Long paramInOutId) {
		this.paramInOutId = paramInOutId;
	}

	public ParamInput getParamInputIn() {
		return paramInputIn;
	}

	public void setParamInputIn(ParamInput paramInputIn) {
		this.paramInputIn = paramInputIn;
	}

	public ActionCommand getActionCommandIn() {
		return actionCommandIn;
	}

	public void setActionCommandIn(ActionCommand actionCommandIn) {
		this.actionCommandIn = actionCommandIn;
	}

	public ActionOfFlow getActionOfFlowIn() {
		return actionOfFlowIn;
	}

	public void setActionOfFlowIn(ActionOfFlow actionOfFlowIn) {
		this.actionOfFlowIn = actionOfFlowIn;
	}

	public ActionCommand getActionCommandOut() {
		return actionCommandOut;
	}

	public void setActionCommandOut(ActionCommand actionCommandOut) {
		this.actionCommandOut = actionCommandOut;
	}

	public ActionOfFlow getActionOfFlowOut() {
		return actionOfFlowOut;
	}

	public void setActionOfFlowOut(ActionOfFlow actionOfFlowOut) {
		this.actionOfFlowOut = actionOfFlowOut;
	}

}
