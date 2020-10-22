package com.viettel.persistence;

// Created Oct 4, 2016 5:24:54 AM by quanns2

import com.viettel.model.ActionDtFile;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Service implement for interface ActionDtFileService.
 * @see com.viettel.persistence.ActionDtFileService
 * @author quanns2
 */

@Service(value = "actionDtFileService")
@Scope("session")
public class ActionDtFileServiceImpl extends GenericDaoImpl<ActionDtFile, Serializable> implements ActionDtFileService,
		Serializable {
	private static Logger logger = LogManager.getLogger(ActionDtFileServiceImpl.class);
}
