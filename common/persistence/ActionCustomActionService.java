package com.viettel.persistence;

// Created Sep 12, 2016 1:55:33 PM by quanns2

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.ActionCustomAction;

import java.io.Serializable;
import java.util.Date;

/**
 * Service interface for domain model class ActionCustomAction.
 * @see ActionCustomAction
 * @author quanns2
 */

public interface ActionCustomActionService extends GenericDao<ActionCustomAction, Serializable> {
    public void updateRunStatus(Long customActionId, Integer runStatus, Date startTime, Date endTime) throws AppException, SysException;
    public void updateRollbackTest(Long customActionId, Integer runStatus, Date startTime, Date endTime) throws AppException, SysException;
}
