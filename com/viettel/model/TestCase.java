package com.viettel.model;

// Created May 30, 2016 2:10:12 PM by quanns2

import javax.persistence.*;
import java.util.Date;

/**
 * @author quanns2
 */
@Entity
@Table(name = "TEST_CASE")
public class TestCase implements java.io.Serializable {

	private Long id;
	private String fileName;
	private Integer testcaseType;
	private Long actionId;
	private Date dateUpload;
	private String userPerform;

	public TestCase() {
	}

	public TestCase(Long id) {
		this.id = id;
	}

	public TestCase(Long id, String fileName, Integer testcaseType, Long actionId, Date dateUpload) {
		this.id = id;
		this.fileName = fileName;
		this.testcaseType = testcaseType;
		this.actionId = actionId;
		this.dateUpload = dateUpload;
	}

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 20, scale = 0)
	/*@GenericGenerator(name = "generator", strategy = "increment")
	@GeneratedValue(generator="generator")*/
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "ACTION_TEST_CASE", allocationSize=1)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "FILE_NAME", length = 200)
	public String getFileName() {
		return this.fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	@Column(name = "TESTCASE_TYPE", precision = 10, scale = 0)
	public Integer getTestcaseType() {
		return this.testcaseType;
	}

	public void setTestcaseType(Integer testcaseType) {
		this.testcaseType = testcaseType;
	}

	@Column(name = "ACTION_ID", precision = 20, scale = 0)
	public Long getActionId() {
		return this.actionId;
	}

	public void setActionId(Long actionId) {
		this.actionId = actionId;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "DATE_UPLOAD", length = 7)
	public Date getDateUpload() {
		return this.dateUpload;
	}

	public void setDateUpload(Date dateUpload) {
		this.dateUpload = dateUpload;
	}

	@Column(name = "USER_PERFORM")
	public String getUserPerform() {
		return userPerform;
	}

	public void setUserPerform(String userPerform) {
		this.userPerform = userPerform;
	}
}
