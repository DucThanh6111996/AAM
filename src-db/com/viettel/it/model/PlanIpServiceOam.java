package com.viettel.it.model;
// Generated Nov 8, 2016 9:02:16 AM by Hibernate Tools 4.0.0

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * PlanIpServiceOam generated by hbm2java
 */
@Entity
@Table(name = "PLAN_IP_SERVICE_OAM")
public class PlanIpServiceOam implements java.io.Serializable {

	private Long id;
	private String area;
	private String provinceCode;
	private Long maxOfEnodeb;
	private String ipService;
	private String ipOam;
	private String subnetmask;
	private Long curIdex;
	private Long maxIdex;

	public PlanIpServiceOam() {
	}

	public PlanIpServiceOam(Long id) {
		this.id = id;
	}
	public PlanIpServiceOam(Long id, String area, String provinceCode, Long maxOfEnodeb, String ipService, String ipOam, String subnetmask, Long curIdex, Long maxIdex) {
		this.id = id;
		this.area = area;
		this.provinceCode = provinceCode;
		this.maxOfEnodeb = maxOfEnodeb;
		this.ipService = ipService;
		this.ipOam = ipOam;
		this.subnetmask = subnetmask;
		this.curIdex = curIdex;
		this.maxIdex = maxIdex;
	}

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "AREA", length = 20)
	public String getArea() {
		return this.area;
	}

	public void setArea(String area) {
		this.area = area;
	}

	@Column(name = "PROVINCE_CODE", length = 20)
	public String getProvinceCode() {
		return this.provinceCode;
	}

	public void setProvinceCode(String provinceCode) {
		this.provinceCode = provinceCode;
	}

	@Column(name = "MAX_OF_ENODEB", precision = 22, scale = 0)
	public Long getMaxOfEnodeb() {
		return this.maxOfEnodeb;
	}

	public void setMaxOfEnodeb(Long maxOfEnodeb) {
		this.maxOfEnodeb = maxOfEnodeb;
	}

	@Column(name = "IP_SERVICE", length = 20)
	public String getIpService() {
		return this.ipService;
	}

	public void setIpService(String ipService) {
		this.ipService = ipService;
	}

	@Column(name = "IP_OAM", length = 20)
	public String getIpOam() {
		return this.ipOam;
	}

	public void setIpOam(String ipOam) {
		this.ipOam = ipOam;
	}

	@Column(name = "SUBNETMASK", length = 20)
	public String getSubnetmask() {
		return this.subnetmask;
	}

	public void setSubnetmask(String subnetmask) {
		this.subnetmask = subnetmask;
	}

	@Column(name = "CUR_IDEX", precision = 22, scale = 0)
	public Long getCurIdex() {
		return this.curIdex;
	}

	public void setCurIdex(Long curIdex) {
		this.curIdex = curIdex;
	}

	@Column(name = "MAX_IDEX", precision = 22, scale = 0)
	public Long getMaxIdex() {
		return this.maxIdex;
	}

	public void setMaxIdex(Long maxIdex) {
		this.maxIdex = maxIdex;
	}

}