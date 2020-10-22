package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.model.ActionServer;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Service implement for interface ActionServerService.
 *
 * @author quanns2
 * @see ActionServerService
 */

@Service(value = "actionServerService")
@Scope("session")
public class ActionServerServiceImpl extends GenericDaoImpl<ActionServer, Serializable> implements ActionServerService, Serializable {
    private static Logger logger = LogManager.getLogger(ActionServerServiceImpl.class);
}
