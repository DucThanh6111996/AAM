package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.model.ImpactProcess;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Service implement for interface ImpactProcessService.
 *
 * @author quanns2
 * @see ImpactProcessService
 */

@Service(value = "impactProcessService")
@Scope("session")
public class ImpactProcessServiceImpl extends GenericDaoImpl<ImpactProcess, Serializable> implements ImpactProcessService, Serializable {
    private static Logger logger = LogManager.getLogger(ImpactProcessServiceImpl.class);
}
