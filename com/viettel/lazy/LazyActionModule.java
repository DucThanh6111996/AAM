package com.viettel.lazy;

// Created May 5, 2016 4:56:37 PM by quanns2

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.ActionModule;
import com.viettel.persistence.ActionModuleService;

/**
 * 
 * @author quanns2
 * 
 */
public class LazyActionModule extends LazyDataModel<ActionModule> {
	private static Logger logger = LogManager.getLogger(LazyActionModule.class);

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ActionModuleService actionModuleService;
	private Long actionId;

	public LazyActionModule(ActionModuleService actionModuleService, Long actionId) {
		this.actionModuleService = actionModuleService;
		this.actionId = actionId;
	}

	@Override
	public void setRowIndex(int rowIndex) {
		if (rowIndex == -1 || getPageSize() == 0) {
			super.setRowIndex(-1);
		} else
			super.setRowIndex(rowIndex % getPageSize());
	}

	@Override
	public Object getRowKey(ActionModule object) {
		return object.getId();
	}

	@Override
	public ActionModule getRowData(String rowKey) {
		ActionModule object = new ActionModule();
		try {
			object = actionModuleService.findById(rowKey);
		} catch (NumberFormatException e) {
			logger.error(e.getMessage(), e);
		} catch (SysException e) {
			logger.error(e.getMessage(), e);
		} catch (AppException e) {
			logger.error(e.getMessage(), e);
		}
		return (ActionModule) object;
	}

	@Override
	public List<ActionModule> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
		List<ActionModule> data = new ArrayList<>();
		int dataSize = 0;

		
		try {
			data = actionModuleService.findList(first, pageSize, actionId);

			dataSize = actionModuleService.count(actionId);
		} catch (SysException e) {
			logger.error(e.getMessage(), e);
		}

		this.setRowCount(dataSize);
		return data;
	}
}
