package com.viettel.lazy;

// Created May 30, 2016 2:10:12 PM by quanns2

import java.util.*;
import java.util.Map.Entry;

import com.viettel.controller.Module;
import com.viettel.persistence.IimService;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

/**
 * 
 * @author quanns2
 *
 */
public class LazyModule extends LazyDataModel<Module> {
	private static Logger logger = Logger.getLogger(LazyModule.class);

	private IimService iimService;
	private Map<String, String> search;
	private Set<Long> impactModules;
	private List<Long> unitId;
	private String nationCode;

	public LazyModule(IimService iimService, Map<String, String> search, Set<Long> impactModules, List<Long> unitId, String nationCode) {
		this.iimService = iimService;
		this.search = search;
		this.impactModules = impactModules;
		this.unitId = unitId;
		this.nationCode = nationCode;
	}

	@Override
	public void setRowIndex(int rowIndex) {
		if (rowIndex == -1 || getPageSize() == 0) {
			super.setRowIndex(-1);
		} else
			super.setRowIndex(rowIndex % getPageSize());
	}

	@Override
	public Object getRowKey(Module object) {
		return object.getModuleId();
	}

	@Override
	public Module getRowData(String rowKey) {
		Module module = null;
		try {
			if (rowKey != null)
				module = iimService.findModuleById(nationCode, Long.parseLong(rowKey));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return module;
	}

	@Override
	public List<Module> load(int first, int pageSize, String sortField, SortOrder sortOrder, Map<String, Object> filters) {
		List<Module> data = new ArrayList<>();
		int dataSize = 0;

		Map<String, String> order = new HashMap<String, String>();
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
			data = iimService.findFilterModule(nationCode, first, pageSize, filters, impactModules, unitId);
			dataSize = iimService.countFilterModule(nationCode, filters, impactModules, unitId);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		this.setRowCount(dataSize);
		return data;
	}
}
