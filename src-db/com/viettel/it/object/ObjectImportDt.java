package com.viettel.it.object;

import com.viettel.it.model.Node;
import com.viettel.it.model.NodeType;
import com.viettel.it.model.Vendor;
import com.viettel.it.model.Version;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class ObjectImportDt implements Serializable {
	Vendor vendor;
	Version version;
	NodeType nodeType;
	List<String> paramNames = new ArrayList<String>();
	private List<List<Object>> paramValues = new ArrayList<>();
	private String sheetName;
	/*20190408_chuongtq start check param when create MOP*/
	List<Node> nodes;
	List<Integer> rows;
	/*20190408_chuongtq end check param when create MOP*/
	
	public Vendor getVendor() {
		return vendor;
	}
	public void setVendor(Vendor vendor) {
		this.vendor = vendor;
	}
	public Version getVersion() {
		return version;
	}
	public void setVersion(Version version) {
		this.version = version;
	}
	public NodeType getNodeType() {
		return nodeType;
	}
	public void setNodeType(NodeType nodeType) {
		this.nodeType = nodeType;
	}
	public List<String> getParamNames() {
		return paramNames;
	}
	public void setParamNames(List<String> paramNames) {
		this.paramNames = paramNames;
	}
	public String getSheetName() {
		return sheetName;
	}
	public void setSheetName(String sheetName) {
		this.sheetName = sheetName;
	}
	public List<List<Object>> getParamValues() {
		return paramValues;
	}
	public void setParamValues(List<List<Object>> paramValues) {
		this.paramValues = paramValues;
	}
	/*20190408_chuongtq start check param when create MOP*/
	public List<Node> getNodes() {
		return nodes;
	}
	public void setNodes(List<Node> nodes) {
		this.nodes = nodes;
	}
	public List<Integer> getRows() {
		return rows;
	}
	public void setRows(List<Integer> rows) {
		this.rows = rows;
	}
	/*20190408_chuongtq end check param when create MOP*/
	
	
}
