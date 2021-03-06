package com.viettel.model;
// Generated Feb 23, 2018 3:20:04 PM by Hibernate Tools 4.3.5.Final

import java.util.Date;
import javax.persistence.*;

/**
 * Test generated by hbm2java
 */
@Entity
@Table(name = "TEST")
public class Test implements java.io.Serializable {

	private Long id;
	private String testString;
	private Date createdTime;

	public Test() {
	}

	public Test(Long id) {
		this.id = id;
	}

	public Test(Long id, String testString, Date createdTime) {
		this.id = id;
		this.testString = testString;
		this.createdTime = createdTime;
	}

	@Id

	@Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "TEST_SEQ", allocationSize = 1)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "TEST_STRING", length = 200)
	public String getTestString() {
		return this.testString;
	}

	public void setTestString(String testString) {
		this.testString = testString;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_TIME", length = 7)
	public Date getCreatedTime() {
		return this.createdTime;
	}

	public void setCreatedTime(Date createdTime) {
		this.createdTime = createdTime;
	}

}
