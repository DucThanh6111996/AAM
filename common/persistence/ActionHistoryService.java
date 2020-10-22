package com.viettel.persistence;

// Created Aug 1, 2016 9:16:03 AM by quanns2


import com.viettel.model.ActionHistory;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * Service interface for domain model class ActionHistory.
 * @see ActionHistory
 * @author quanns2
 */

public interface ActionHistoryService extends GenericDao<ActionHistory, Serializable> {
	
	public List<ActionHistory> findNew(int first, int pageSize, Map<String, Object> filters, Map<String, String> orders) throws Exception;
	public int countNew(Map<String, Object> filters) throws Exception;
	
}
