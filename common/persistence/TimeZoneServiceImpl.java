package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.model.TimeZone;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Service implement for interface TimeZoneService.
 *
 * @author quanns2
 * @see TimeZoneService
 */

@Service(value = "timeZoneService")
@Scope("session")
public class TimeZoneServiceImpl extends GenericDaoImpl<TimeZone, Serializable> implements TimeZoneService, Serializable {
    private static Logger logger = LogManager.getLogger(TimeZoneServiceImpl.class);
}
