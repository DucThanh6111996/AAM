package com.viettel.it.model;

// Generated Sep 26, 2016 5:12:14 PM by Hibernate Tools 4.0.0

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;

/**
 * TestTable2 generated by hbm2java
 */
@Entity
@Table(name = "TEST_TABLE2", schema = "AUTODT")
public class TestTable2 implements java.io.Serializable {

	private Long column1;
	private String column2;
	private String column3;

	public TestTable2() {
	}

	public TestTable2(Long column1) {
		this.column1 = column1;
	}

	public TestTable2(Long column1, String column2, String column3) {
		this.column1 = column1;
		this.column2 = column2;
		this.column3 = column3;
	}

	@Id
	@Column(name = "COLUMN1", unique = true, nullable = false, precision = 22, scale = 0)
//	@GenericGenerator(name = "generator", strategy = "increment")
//    @GeneratedValue(generator = "generator")
	public Long getColumn1() {
		return this.column1;
	}

	public void setColumn1(Long column1) {
		this.column1 = column1;
	}

	@Column(name = "COLUMN2", length = 20)
	public String getColumn2() {
		return this.column2;
	}

	public void setColumn2(String column2) {
		this.column2 = column2;
	}

	@Column(name = "COLUMN3", length = 20)
	public String getColumn3() {
		return this.column3;
	}

	public void setColumn3(String column3) {
		this.column3 = column3;
	}

}
