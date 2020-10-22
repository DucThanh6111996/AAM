package com.viettel.bean;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class RstSqlData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private StringBuilder logBuilder =new StringBuilder();
	private List<Float> dataList =new ArrayList<>();
	public StringBuilder getLogBuilder() {
		return logBuilder;
	}
	public void setLogBuilder(StringBuilder logBuilder) {
		this.logBuilder = logBuilder;
	}
	public List<Float> getDataList() {
		return dataList;
	}
	public void setDataList(List<Float> dataList) {
		this.dataList = dataList;
	}
	
}
