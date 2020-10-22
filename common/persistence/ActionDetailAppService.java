package com.viettel.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.controller.ActionDetailApp;

import java.util.Date;
import java.util.List;

// Created May 5, 2016 4:56:37 PM by quanns2

/**
 * Service interface for domain model class ActionDetailApp.
 * @see ActionDetailApp
 * @author quanns2
 */

public interface ActionDetailAppService extends GenericDao<ActionDetailApp, Long> {
	public List<ActionDetailApp> findListDetailApp(Long actionId);
	public List<ActionDetailApp> findListDetailApp(Long actionId, String groupAction, List<Long> moduleIds, Boolean includeTestbed);
	public List<ActionDetailApp> findListDetailApp(Long actionId, String groupAction, boolean order, List<Long> moduleIds, Boolean includeTestbed);
	public List<ActionDetailApp> findListDetailApp(Long actionId, String groupAction, boolean order, Long moduleId, List<Long> moduleIds, Boolean includeTestbed);
	public List<ActionDetailApp> findListDetailApp(Long actionId, String groupAction, Long moduleId, List<Long> moduleIds, Boolean includeTestbed);

	public void updateRunStatus(Long actionId, Integer runStatus, Date startTime, Date endTime) throws AppException, SysException;
	public void updateBackupStatus(Long actionId, Integer backupStatus, Date startTime, Date endTime) throws AppException, SysException;

}
