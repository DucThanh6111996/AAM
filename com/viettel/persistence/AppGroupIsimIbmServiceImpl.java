package com.viettel.persistence;

// Created May 12, 2016 11:21:40 AM by quanns2

import com.viettel.model.AppGroupIsimIbm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Service implement for interface UnitService.
 * @see AppGroupIsimIbmService
 * @author quanns2
 */

@Service(value = "appGroupIsimIbmService")
@Scope("session")
public class AppGroupIsimIbmServiceImpl extends GenericDaoImpl<AppGroupIsimIbm, Serializable> implements AppGroupIsimIbmService, Serializable {
	private static Logger logger = LogManager.getLogger(AppGroupIsimIbmServiceImpl.class);
}
