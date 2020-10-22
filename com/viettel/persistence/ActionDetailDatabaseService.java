package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.ActionDetailDatabase;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Service interface for domain model class ActionDetailDatabase.
 * @see ActionDetailDatabase
 * @author quanns2
 */

public interface ActionDetailDatabaseService extends GenericDao<ActionDetailDatabase, Serializable> {
    public List<ActionDetailDatabase> findListDetailDb(Long actionId, Integer kbGroup, boolean order, boolean includeTestbed);

    public void updateRunStatus(Long actionId, Integer runStatus, Date startTime, Date endTime) throws AppException, SysException;
    public void updateBackupStatus(Long actionId, Integer backupStatus, Date startTime, Date endTime) throws AppException, SysException;
}
