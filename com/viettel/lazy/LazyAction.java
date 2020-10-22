package com.viettel.lazy;

// Created May 5, 2016 4:56:37 PM by quanns2

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.Action;
import com.viettel.persistence.ActionService;

/**
 * 
 * @author quanns2
 *
 */
public class LazyAction extends LazyDataModel<Action> {
	private static Logger logger = LogManager.getLogger(LazyAction.class);

	// thenv_20180630_start
	private ActionService actionService;
	private Map<String, Object> search;
	private List<Long> actionTypes;

	public LazyAction(ActionService actionService, Map<String, Object> search, List<Long> actionTypes) {
		this.actionService = actionService;
		this.search = search;
		this.actionTypes = actionTypes;
	}
	// thenv_20180630_end

	@Override
	public void setRowIndex(int rowIndex) {
		if (rowIndex == -1 || getPageSize() == 0) {
			super.setRowIndex(-1);
		} else
			super.setRowIndex(rowIndex % getPageSize());
	}

	@Override
	public Object getRowKey(Action object) {
		return object.getId();
	}

	@Override
	public Action getRowData(String rowKey) {
		Action object = new Action();
		try {
			object = actionService.findById(rowKey);
		} catch (NumberFormatException e) {
			logger.error(e.getMessage(), e);
		} catch (SysException e) {
			logger.error(e.getMessage(), e);
		} catch (AppException e) {
			logger.error(e.getMessage(), e);
		}
		return object;
	}

	@Override
	public List<Action> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
		List<Action> data = new ArrayList<>();
		int dataSize = 0;

		Map<String, String> order = new HashMap<>();
		if (StringUtils.isEmpty(sortField)) {
			 order.put("createdTime", "DESC");
		} else {
			if (sortOrder.equals(SortOrder.ASCENDING))
				order.put(sortField, "ASC");
			else
				order.put(sortField, "DESC");
		}
		
		// thenv_20180630_start
		if (search != null) {
			for (Entry<String, Object> filter : search.entrySet()) {
				filters.put(filter.getKey(), filter.getValue());
			}
		}
		// thenv_20180630_end

		try {
			data = actionService.findList(first, pageSize, filters, order, actionTypes);
			dataSize = actionService.count(filters, actionTypes);
		} catch (SysException | AppException e) {
			logger.error(e.getMessage(), e);
		}

		this.setRowCount(dataSize);
		return data;
	}
}
