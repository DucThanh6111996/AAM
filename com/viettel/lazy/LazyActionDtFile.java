package com.viettel.lazy;

// Created Oct 4, 2016 5:24:54 AM by quanns2

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.ActionDtFile;

import com.viettel.persistence.ActionDtFileService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 * 
 * @author quanns2
 *
 */
public class LazyActionDtFile extends LazyDataModel<ActionDtFile> {
	private static Logger logger = Logger.getLogger(LazyActionDtFile.class);

	private ActionDtFileService actionDtFileService;
	private Map<String, String> search;

	public LazyActionDtFile(ActionDtFileService actionDtFileService, Map<String, String> search) {
		this.actionDtFileService = actionDtFileService;
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
	public Object getRowKey(ActionDtFile object) {
		return object.getId();
	}

	@Override
	public ActionDtFile getRowData(String rowKey) {
		ActionDtFile object = new ActionDtFile();
		try {
			object = actionDtFileService.findById(rowKey);
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
	public List<ActionDtFile> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String,Object> filters) {
		List<ActionDtFile> data = new ArrayList<>();
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
			data = actionDtFileService.findList(first, pageSize, filters, order);
			dataSize = actionDtFileService.count(filters);
		} catch (SysException | AppException e) {
			logger.error(e.getMessage(), e);
		}

		this.setRowCount(dataSize);
		return data;
	}
}
