package com.viettel.it.model;

import javax.persistence.*;

import static javax.persistence.GenerationType.SEQUENCE;

@Entity
@Table(name = "PARAM_NODE_VAL")
public class ParamNodeVal {

	private Long id;
	private String nodeCode;
	private String interfacePort;
	private String paramKey;
	private String paramValue;

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
	@GeneratedValue(strategy = SEQUENCE, generator = "generator")
    @SequenceGenerator(name = "generator", sequenceName = "PARAM_NODE_VAL_SEQ", allocationSize = 1)
	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name="NODE_CODE", length = 255)
	public String getNodeCode() {
		return nodeCode;
	}

	public void setNodeCode(String nodeCode) {
		this.nodeCode = nodeCode;
	}

	@Column(name="PARAM_KEY", length = 255)
	public String getParamKey() {
		return paramKey;
	}

	public void setParamKey(String paramKey) {
		this.paramKey = paramKey;
	}

	@Column(name="PARAM_VALUE", length = 255)
	public String getParamValue() {
		return paramValue;
	}

	public void setParamValue(String paramValue) {
		this.paramValue = paramValue;
	}

	@Column(name="INTERFACE_PORT", length = 255)
	public String getInterfacePort() {
		return interfacePort;
	}

	public void setInterfacePort(String interfacePort) {
		this.interfacePort = interfacePort;
	}

}
