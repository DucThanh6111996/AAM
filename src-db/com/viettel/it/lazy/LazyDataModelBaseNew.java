/*
 * Created on Sep 11, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.it.lazy;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import com.viettel.it.persistence.GenericDaoServiceNewV2;

/**
 * @author Nguyễn Xuân Huy <huynx6@viettel.com.vn>
 * @sin Jul 29, 2016
 * @version 1.0 
 * @param <T>
 * @param <PK>
 */
public class LazyDataModelBaseNew<T, PK extends Serializable> extends LazyDataModel<T> {
	private static Logger logger = LogManager.getLogger(LazyDataModelBaseNew.class);

	private static final long serialVersionUID = -8213459208378430543L;
	protected GenericDaoServiceNewV2<T, PK> daoService;
	protected Map<String, Object> filters;
	private LinkedHashMap<String, String> orders;
	private Map<String, Object> currFilters;
	private Map<String, Object> initFilters;

	/*20180706_hoangnd__start*/
	protected Map<String, Object> sqlRes;
	private String hql = null;
	private String hqlCount = null;
    /*20180706_hoangnd__end*/

	public LazyDataModelBaseNew(GenericDaoServiceNewV2<T, PK> daoService) {
		this.daoService = daoService;
	}

	public LazyDataModelBaseNew(GenericDaoServiceNewV2<T, PK> daoService, Map<String, Object> filters, LinkedHashMap<String, String> orders) {
		
		this.daoService = daoService;
		initFilterOrder(filters,orders);
	}
	
	public LazyDataModelBaseNew(GenericDaoServiceNewV2<T, PK> daoService,Object ... filtersOrOrders) {
		
		if(daoService!=null)
			this.daoService = daoService;
		initFilterOrder(filtersOrOrders);
	}
	
	@SuppressWarnings("unchecked")	
	private void initFilterOrder(Object... filtersOrOrders) {
		// TODO Auto-generated method stub
		if(filtersOrOrders!=null){
			switch (filtersOrOrders.length) {
			case 1:
				if (filtersOrOrders[0] instanceof Map<?, ?>)
					filters = (Map<String, Object>) filtersOrOrders[0];
				break;
			case 2:
				if (filtersOrOrders[0] !=null && filtersOrOrders[0] instanceof Map<?, ?>)
					filters = (Map<String, Object>) filtersOrOrders[0];
				if (filtersOrOrders[1] != null && filtersOrOrders[1] instanceof Map<?, ?>)
					orders = (LinkedHashMap<String, String>) filtersOrOrders[1];
				break;
			/*20180706_hoangnd__start*/
			case 3:
				if (filtersOrOrders[0] != null && filtersOrOrders[0] instanceof Map<?, ?>) {
					filters = (Map<String, Object>) filtersOrOrders[0];
				}
				if (filtersOrOrders[1] != null && filtersOrOrders[1] instanceof Map<?, ?>) {
					sqlRes = (Map<String, Object>) filtersOrOrders[1];
				}
				if (filtersOrOrders[2] != null && filtersOrOrders[2] instanceof Map<?, ?>) {
					orders = (LinkedHashMap<String, String>) filtersOrOrders[2];
				}
				break;
			case 5:
				if (filtersOrOrders[0] != null && filtersOrOrders[0] instanceof Map<?, ?>) {
					filters = (Map<String, Object>) filtersOrOrders[0];
				}
				if (filtersOrOrders[1] != null && filtersOrOrders[1] instanceof Map<?, ?>) {
					sqlRes = (Map<String, Object>) filtersOrOrders[1];
				}
				if (filtersOrOrders[2] != null && filtersOrOrders[2] instanceof Map<?, ?>) {
					orders = (LinkedHashMap<String, String>) filtersOrOrders[2];
				}
				this.hql = (String) filtersOrOrders[3];
				this.hqlCount = (String) filtersOrOrders[4];
				break;
			/*20180706_hoangnd__end*/
			default:
				//No sort or filter
				break;
			
			}
		}
	}

	@Override
	public List<T> load(int first, int pageSize, String sortField,SortOrder sortOrder, Map<String, Object> filters) {
		// TODO Auto-generated method stub
		List<SortMeta> multiSortMeta = new ArrayList<SortMeta>();
		if(sortField!=null)
			multiSortMeta.add(new SortMeta(null, sortField, sortOrder, null));
		return load(first, pageSize, multiSortMeta, filters);
	}

	@SuppressWarnings("unchecked")
	@Override
	public List<T> load(int first, int pageSize, List<SortMeta> multiSortMeta, Map<String, Object> filters) {
	
		if(filters==null)
			filters = new HashMap<String, Object>();
		List<T> data = new ArrayList<T>();
		int dataSize = 0;
		try{
			if(initFilters!=null){
				filters.putAll(initFilters);
				initFilters.clear();
			}
				
			if(this.filters!=null){
				filters.putAll(this.filters);
				/*
				for (Iterator<String> iterator = this.filters.keySet().iterator(); iterator.hasNext();) {
					String field = (String) iterator.next();
					Object value = this.filters.get(field);
					
					if (value instanceof String) {
						filters.put(field, (String) value);
					}else if (value instanceof Date[]) {
						Date[] filDate = (Date[]) value;
						SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
						Date fromDate;
						Date toDate;
						switch (filDate.length) {
						case 1:
							fromDate = filDate[0];
							if (fromDate!=null)
								filters.put(field, formatter.format(fromDate)+"-");
							break;
						case 2:
							fromDate = filDate[0];
							toDate = filDate[1];
							if (fromDate==null &&toDate!=null)
								filters.put(field, "-"+formatter.format(toDate));
							else if (fromDate!=null &&toDate==null)
								filters.put(field, formatter.format(fromDate)+"-");
							else if (fromDate!=null &&toDate!=null)
								filters.put(field, formatter.format(fromDate)+"-"+formatter.format(toDate));
							break;

						default:
							break;
						}
					}else if (value instanceof Number) {
						filters.put(field, ((Number) value).toString());
					}else if (value instanceof Map<?,?>){
					
					}
					
				}
				*/
			}
			LinkedHashMap<String, String> sorter =null;
			if (this.orders != null){
				sorter = new LinkedHashMap<String, String>();
				for (Iterator<String> iterator = this.orders.keySet().iterator(); iterator.hasNext();) {
					String field = (String) iterator.next();
					String value = this.orders.get(field);
					sorter.put(field, value);
				}
			}
			if(multiSortMeta!=null)
				for (SortMeta sortMeta : multiSortMeta) {
					String sortField = sortMeta.getSortField();
					SortOrder sortOrder = sortMeta.getSortOrder();
					if(sortField!=null){
						if (sorter==null)
							sorter = new LinkedHashMap<String, String>();
						switch (sortOrder) {
						case ASCENDING:
							sorter.put(sortField, "ASC");
							break;
						case DESCENDING:
							sorter.put(sortField, "DESC");
							break;
						case UNSORTED:
						default:
							sorter=null;
							break;
						}
					}
				}
			
			
			if(this.filters!=null && this.filters.get("OR_1")!=null){
				Set<T> data2 = new HashSet<>();
				Set<T> data3 = new HashSet<>();
				for (Iterator<String> iterator = this.filters.keySet().iterator(); iterator.hasNext();) {
					Map<String, Object> filterOr = new HashMap<String, Object>(filters);
					String field = (String) iterator.next();
					Map<String, String> value = (Map<String, String>) this.filters.get(field);
					filterOr.putAll(value);
					data2.addAll(daoService.findList(first, pageSize, filterOr,sorter));
					data3.addAll(daoService.findList(-1, -1, filterOr,null));
				}
				data = new ArrayList<>(data2);
				dataSize = data3.size();
			}else{
				this.currFilters = filters;
				if(filters.get("search")!=null){
					
				}else{
					/*20180706_hoangnd__start*/
					if (this.sqlRes == null || this.sqlRes.isEmpty()) {
						if (hql!=null  && hqlCount!=null){
							data = daoService.findListWithMap(hql,first,pageSize,filters);
							dataSize = daoService.countWithMap(hqlCount,filters);
						}else {
							data = daoService.findList(first, pageSize, filters, sorter);
							dataSize = daoService.count2(filters);
						}
					}else {
						data = daoService.findList(first, pageSize, filters, sqlRes, sorter);
						dataSize = daoService.count2(filters, sqlRes);
					}
					/*20180706_hoangnd__end*/
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		this.setRowCount(dataSize);
		return data;
	}
	@Override
	public T getRowData(String rowKey) {
		T object = null;
		try {
			object = daoService.findById((PK) rowKey);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} 
		
		return (T) object;
	}
	public Map<String, Object> getFilters() {
		return filters;
	}

	public void setFilters(Map<String, Object> filters) {
		this.filters = filters;
	}
	

	public static void main(String[] args) {
//		Map<String,Map<String,String>> filtersOrOrders = new HashMap<>();
//		Map<String,String> filter = new HashMap<>();
//		filter.put("deptReportId", "6892");
//		filtersOrOrders.put("OR_1", filter );
//		filter = new HashMap<>();
//		filter.put("id-EXAC", "1");
//		filtersOrOrders.put("OR_2", filter);
//		LazyDataModel<Dispatch> test = new LazyDataModelBaseNew<>(DAO.dispatchService());
//		filter.put("DispatchHasDepts_FILTER", "Phòng");
//		List<Dispatch> objs = test.load(0, 25, null, null, filter);
//		System.err.println(objs.size());
		//LazyDataModel<TaskForLazy> test = new LazyDataModelBaseNew<>(DAO.taskForLazyService());
		//filter.put("TaskHasDepts_FILTER", "Phòng");
//		List<TaskForLazy> objs = test.load(0, 25, null, null, filter);
		//System.err.println(objs.size());
		
	}

	public Map<String, Object> getCurrFilters() {
		return currFilters;
	}

	public void setCurrFilters(Map<String, Object> currFilters) {
		this.currFilters = currFilters;
	}

	public Map<String, Object> getInitFilters() {
		return initFilters;
	}

	public void setInitFilters(Map<String, Object> initFilters) {
		this.initFilters = initFilters;
	}
}