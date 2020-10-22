package com.viettel.it.object;

import java.io.Serializable;

public class ParamNodeValModelExcel implements Serializable {

	private String nodeCode;
	private String interfacePort;
	private String paramKey;
	private String paramVal;

	public String getNodeCode() {
		return nodeCode;
	}

	public void setNodeCode(String nodeCode) {
		this.nodeCode = nodeCode;
	}

	public String getInterfacePort() {
		return interfacePort;
	}

	public void setInterfacePort(String interfacePort) {
		this.interfacePort = interfacePort;
	}

	public String getParamKey() {
		return paramKey;
	}

	public void setParamKey(String paramKey) {
		this.paramKey = paramKey;
	}

	public String getParamVal() {
		return paramVal;
	}

	public void setParamVal(String paramVal) {
		this.paramVal = paramVal;
	}

}
