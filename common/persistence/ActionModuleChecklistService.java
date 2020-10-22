package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.model.ActionModuleChecklist;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Service interface for domain model class ActionModuleChecklist.
 * @see com.viettel.common.model.ActionModuleChecklist
 * @author quanns2
 */

public interface ActionModuleChecklistService extends GenericDao<ActionModuleChecklist, Serializable> {

	public Map<Long,List<Long>> getMapCheckList(List<Long> listActionModuleId);

}
