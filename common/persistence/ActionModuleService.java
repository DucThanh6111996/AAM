package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.model.ActionModule;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Service interface for domain model class ActionModule.
 * @see ActionModule
 * @author quanns2
 */

public interface ActionModuleService extends GenericDao<ActionModule, Serializable> {
	
	public List<ActionModule> findList(int first, int pageSize, Long actionId);
    public int count(Long actionId);
    
    public List<Long> findListActionModuleId(Long actionId);
    public List<Long> findListModuleId(Long actionId, Integer kbGroup, Boolean includeTestbed);
    public ActionModule findModule(Long actionId, Long moduleId);

    public Map<Long, ActionModule> getMapObjByListAppId(Long actionId);
}
