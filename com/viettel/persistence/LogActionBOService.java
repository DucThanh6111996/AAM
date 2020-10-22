package com.viettel.persistence;

// Created Sep 12, 2016 1:55:33 PM by quanns2

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.LogAction;

import java.io.Serializable;
import java.util.Date;

/**
 * Service interface for domain model class ActionCustomAction.
 * @see LogAction
 * @author quanns2
 */

public interface LogActionBOService extends GenericDao<LogAction, Serializable> {
    public void writeLog(java.util.Date startTime,java.util.Date endTime,String appCode,String user,String className,String actionMenthod,String actionType,java.util.Date createDate,
                         String content,String detailResult,String requestId) throws AppException, SysException;
}
