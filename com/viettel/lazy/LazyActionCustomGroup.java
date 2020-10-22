package com.viettel.lazy;

// Created Sep 12, 2016 1:55:33 PM by quanns2

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.ActionCustomGroup;
import com.viettel.persistence.ActionCustomGroupService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 * 
 * @author quanns2
 *
 */
public class LazyActionCustomGroup extends LazyDataModel<ActionCustomGroup> {
	private static Logger logger = Logger.getLogger(LazyActionCustomGroup.class);

	private ActionCustomGroupService actionCustomGroupService;
	private Map<String, String> search;

	public LazyActionCustomGroup(ActionCustomGroupService actionCustomGroupService, Map<String, String> search) {
		this.actionCustomGroupService = actionCustomGroupService;
		this.search = search;
	}

	@Override
	public void setRowIndex(int rowIndex) {
		if (rowIndex == -1 || getPageSize() == 0) {
			super.setRowIndex(-1);
		} else
			super.setRowIndex(rowIndex % getPageSize());
	}

	@Override
	public Object getRowKey(ActionCustomGroup object) {
		return object.getId();
	}

	@Override
	public ActionCustomGroup getRowData(String rowKey) {
		ActionCustomGroup object = new ActionCustomGroup();
		try {
			object = actionCustomGroupService.findById(rowKey);
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
	public List<ActionCustomGroup> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
		List<ActionCustomGroup> data = new ArrayList<>();
		int dataSize = 0;

		Map<String, String> order = new HashMap<>();
		if (StringUtils.isEmpty(sortField)) {
			// order.put("username", "ASC");
		} else {
			if (sortOrder.equals(SortOrder.ASCENDING))
				order.put(sortField, "ASC");
			else
				order.put(sortField, "DESC");
		}

		if (search != null) {
			for (Entry<String, String> filter : search.entrySet()) {
				filters.put(filter.getKey(), filter.getValue());
			}
		}

		try {
			data = actionCustomGroupService.findList(first, pageSize, filters, order);
			dataSize = actionCustomGroupService.count(filters);
		} catch (SysException | AppException e) {
			logger.error(e.getMessage(), e);
		}

		this.setRowCount(dataSize);
		return data;
	}
}
