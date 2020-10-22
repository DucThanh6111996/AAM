package com.viettel.persistence;

// Created Sep 12, 2016 1:55:33 PM by quanns2

import com.viettel.model.ActionCustomGroup;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Service implement for interface ActionCustomGroupService.
 * @see com.viettel.persistence.ActionCustomGroupService
 * @author quanns2
 */

@Service(value = "actionCustomGroupService")
@Scope("session")
public class ActionCustomGroupServiceImpl extends GenericDaoImpl<ActionCustomGroup, Serializable> implements
		ActionCustomGroupService, Serializable {
	private static Logger logger = LogManager.getLogger(ActionCustomGroupServiceImpl.class);
}
