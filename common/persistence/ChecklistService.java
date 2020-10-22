package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.Checklist;

import java.io.Serializable;
import java.util.Collection;
import java.util.List;

/**
 * Service interface for domain model class Checklist.
 * @see Checklist
 * @author quanns2
 */

public interface ChecklistService extends GenericDao<Checklist, Serializable> {
    public List<Checklist> findCheckListAppByAction(Long actionId) throws AppException, SysException;
    public List<Checklist> findCheckListDbByAction(Long actionId) throws AppException, SysException;
    public List<Checklist> findCheckListByAction(Collection<Long> ids) throws AppException, SysException;
}
