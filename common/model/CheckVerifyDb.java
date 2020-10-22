package com.viettel.model;

// Created Oct 12, 2016 10:55:54 AM by quanns2

import javax.persistence.*;
import java.util.Date;

/**
 * @author quanns2
 */
@Entity
@Table(name = "CHECK_VERIFY_DB")
public class CheckVerifyDb implements java.io.Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private Integer id;
	private Date createdDate;
	private String text;

	public CheckVerifyDb() {
	}

	public CheckVerifyDb(Integer id) {
		this.id = id;
	}

	public CheckVerifyDb(Integer id, Date createdDate, String text) {
		this.id = id;
		this.createdDate = createdDate;
		this.text = text;
	}

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 10, scale = 0)
	public Integer getId() {
		return this.id;
	}

	public void setId(Integer id) {
		this.id = id;
	}

	@Temporal(TemporalType.TIMESTAMP)
	@Column(name = "CREATED_DATE", length = 7)
	public Date getCreatedDate() {
		return this.createdDate;
	}

	public void setCreatedDate(Date createdDate) {
		this.createdDate = createdDate;
	}

	@Column(name = "TEXT")
	public String getText() {
		return this.text;
	}

	public void setText(String text) {
		this.text = text;
	}

}
