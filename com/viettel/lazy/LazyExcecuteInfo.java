package com.viettel.lazy;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;

import com.viettel.bean.ExcecuteInfoObj;


public class LazyExcecuteInfo extends LazyDataModel<ExcecuteInfoObj> {
	private static Logger logger = LogManager.getLogger(LazyExcecuteInfo.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private List<ExcecuteInfoObj> datasource;

	public LazyExcecuteInfo(List<ExcecuteInfoObj> datasource) {
		this.datasource = datasource;
		if(this.datasource==null) this.datasource =new ArrayList<ExcecuteInfoObj>();
	}

	@Override
	public ExcecuteInfoObj getRowData(String rowKey) {

		try {
			long gId = Long.parseLong(rowKey);
			for (ExcecuteInfoObj obj : datasource) {
				if (obj.getAppId().longValue() == gId)
					return obj;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public void setRowIndex(int rowIndex) {
		if (rowIndex == -1 || getPageSize() == 0) {
			super.setRowIndex(-1);
		} else
			super.setRowIndex(rowIndex % getPageSize());
	}

	@Override
	public Object getRowKey(ExcecuteInfoObj obj) {
		return obj.getAppId().longValue();
	}

	@Override
	public List<ExcecuteInfoObj> load(int first, int pageSize, String sortField, SortOrder sortOrder,
			Map<String, Object> filters) {
		List<ExcecuteInfoObj> data = new ArrayList<ExcecuteInfoObj>();

		// filter
		for (ExcecuteInfoObj obj : datasource) {
			boolean match = true;

			for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
				try {
					String filterProperty = it.next();
					String filterValue = String.valueOf(filters.get(filterProperty));			
		
					String fieldValue = String.valueOf(obj.getClass().getField(filterProperty).get(obj));
					if (filterValue == null || fieldValue.contains(filterValue)) {
						match = true;
					} else {
						match = false;
						break;
					}					
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
					match = false;
				}
			}

			if (match) {
				data.add(obj);
			}
		}

		/*
		 * //sort if(sortField != null) { Collections.sort(data, new
		 * GroupsSorter(sortField, sortOrder)); }
		 */

		// rowCount
		int dataSize = data.size();
		this.setRowCount(dataSize);

		// paginate
		if (dataSize > pageSize) {
			try {
				return data.subList(first, first + pageSize);
			} catch (IndexOutOfBoundsException e) {
				logger.error(e.getMessage(), e);
				return data.subList(first, first + (dataSize % pageSize));
			}
		} else {
			return data;
		}
	}
}
