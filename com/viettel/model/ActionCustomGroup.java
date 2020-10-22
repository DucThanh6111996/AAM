package com.viettel.model;

// Created Sep 12, 2016 1:55:33 PM by quanns2

import com.fasterxml.jackson.annotation.JsonManagedReference;

import javax.persistence.*;
import java.util.HashSet;
import java.util.Set;

/**
 * @author quanns2
 */
@Entity
@Table(name = "ACTION_CUSTOM_GROUP")
public class ActionCustomGroup implements java.io.Serializable {

	private Long id;
	private Long actionId;
	private String name;
	private Integer afterGroup;
	private Integer rollbackAfter;
	private Integer kbGroup;

	@JsonManagedReference
	private Set<ActionCustomAction> actionCustomActions = new HashSet<>();

	public ActionCustomGroup() {
	}

	public ActionCustomGroup(Long id) {
		this.id = id;
	}

	public ActionCustomGroup(Long id, Long actionId, String name, Integer afterGroup) {
		this.id = id;
		this.actionId = actionId;
		this.name = name;
		this.afterGroup = afterGroup;
	}

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 20, scale = 0)
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "ACTION_CUSTOM_GROUP_SEQ", allocationSize=1)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "ACTION_ID", precision = 20, scale = 0)
	public Long getActionId() {
		return this.actionId;
	}

	public void setActionId(Long actionId) {
		this.actionId = actionId;
	}

	@Column(name = "NAME", length = 200)
	public String getName() {
		return this.name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@Column(name = "AFTER_GROUP")
	public Integer getAfterGroup() {
		return this.afterGroup;
	}

	public void setAfterGroup(Integer afterGroup) {
		this.afterGroup = afterGroup;
	}

	@Column(name = "ROLLBACK_AFTER")
	public Integer getRollbackAfter() {
		return rollbackAfter;
	}

	public void setRollbackAfter(Integer rollbackAfter) {
		this.rollbackAfter = rollbackAfter;
	}

	@Column(name = "KB_GROUP")
	public Integer getKbGroup() {
		return kbGroup;
	}

	public void setKbGroup(Integer kbGroup) {
		this.kbGroup = kbGroup;
	}

	@OneToMany(fetch = FetchType.EAGER, mappedBy = "actionCustomGroup")
	@OrderBy("priority")
	public Set<ActionCustomAction> getActionCustomActions() {
		return actionCustomActions;
	}

	public void setActionCustomActions(Set<ActionCustomAction> actionCustomActions) {
		this.actionCustomActions = actionCustomActions;
	}
}
