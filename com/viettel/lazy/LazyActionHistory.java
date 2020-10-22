package com.viettel.lazy;

// Created Aug 1, 2016 9:16:03 AM by quanns2



import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.ActionHistory;
import com.viettel.persistence.ActionHistoryService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 * 
 * @author quanns2
 *
 */
public class LazyActionHistory extends LazyDataModel<ActionHistory> {
	private static Logger logger = Logger.getLogger(LazyActionHistory.class);

	private ActionHistoryService actionHistoryService;
	private Map<String, String> search;

	public LazyActionHistory(ActionHistoryService actionHistoryService, Map<String, String> search) {
		this.actionHistoryService = actionHistoryService;
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
	public Object getRowKey(ActionHistory object) {
		return object.getId();
	}

	@Override
	public ActionHistory getRowData(String rowKey) {
		ActionHistory object = new ActionHistory();
		try {
			object = actionHistoryService.findById(rowKey);
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
	public List<ActionHistory> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
		List<ActionHistory> data = new ArrayList<>();
		int dataSize = 0;

		Map<String, String> order = new HashMap<>();
		if (StringUtils.isEmpty(sortField)) {
			 order.put("startTime", "DESC");
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
			data = actionHistoryService.findList(first, pageSize, filters, order);
			dataSize = actionHistoryService.count(filters);
		} catch (SysException | AppException e) {
			logger.error(e.getMessage(), e);
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		}

		this.setRowCount(dataSize);
		return data;
	}
}
