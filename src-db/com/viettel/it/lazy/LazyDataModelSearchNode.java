/*
 * Created on Sep 11, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.it.lazy;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Restrictions;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortMeta;
import org.primefaces.model.SortOrder;

import com.viettel.it.object.VendorNodeType;
import com.viettel.it.persistence.GenericDaoServiceNewV2;
import com.viettel.it.persistence.common.ConditionQuery;
import com.viettel.it.persistence.common.OrderBy;

/**
 * @author Nguyễn Xuân Huy <huynx6@viettel.com.vn>
 * @sin Jul 29, 2016
 * @version 1.0 
 * @param <T>
 * @param <PK>
 */
public class LazyDataModelSearchNode<T, PK extends Serializable> extends LazyDataModel<T> {
	private static Logger logger = LogManager.getLogger(LazyDataModelSearchNode.class);

	private static final long serialVersionUID = -8213459208378430543L;
	protected GenericDaoServiceNewV2<T, PK> daoService;
	protected Map<String, Object> filters;
	private LinkedHashMap<String, String> orders;
	private Map<String, Object> currFilters;
	private Map<String, Object> initFilters;
	private Set<VendorNodeType> vendorNodeTypes = new HashSet<VendorNodeType>();
	private Integer searchNodeLab =null; 

	public LazyDataModelSearchNode(GenericDaoServiceNewV2<T, PK> daoService) {
		this.daoService = daoService;
	}

	public LazyDataModelSearchNode(GenericDaoServiceNewV2<T, PK> daoService, Map<String, Object> filters, LinkedHashMap<String, String> orders) {
		
		this.daoService = daoService;
		initFilterOrder(filters,orders);
	}
	
	public LazyDataModelSearchNode(GenericDaoServiceNewV2<T, PK> daoService,Object ... filtersOrOrders) {
		
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
				
			}
			
			OrderBy orderBy = new OrderBy();
			ConditionQuery query = new ConditionQuery();
			List<Criterion> predicates = new ArrayList<Criterion>();
			Object node = filters.get("nodeCode");
			if(node!=null && !node.toString().isEmpty()) {
				predicates.add(Restrictions.ilike("nodeCode", node.toString(), MatchMode.ANYWHERE));
			}
			Object ip = filters.get("nodeIp");
			if(ip!=null && !ip.toString().isEmpty()) {
				predicates.add(Restrictions.ilike("nodeIp", ip.toString(), MatchMode.ANYWHERE));
			}
			query.add(Restrictions.or(predicates.toArray(new Criterion[predicates.size()])));
			
			if(vendorNodeTypes.size()>0){
				List<Criterion> predicate2s = new ArrayList<Criterion>();
				for(VendorNodeType vendorNodeType :vendorNodeTypes){
					predicate2s.add(Restrictions.and(Restrictions.eq("version.versionId", vendorNodeType.getVersionId())
							,Restrictions.and(Restrictions.eq("vendor.vendorId", vendorNodeType.getVendorId())
							,Restrictions.eq("nodeType.typeId", vendorNodeType.getNodeTypeId()))
							));
				}
				query.add(Restrictions.or(predicate2s.toArray(new Criterion[predicate2s.size()])));
			}

			// thenv_20180618_countryCode_start
			Object countryCode = filters.get("countryCode.countryCode-EXAC");
			if(countryCode!=null && !countryCode.toString().isEmpty()) {
				query.add(Restrictions.eq("countryCode.countryCode", countryCode));
			}
			// thenv_20180618_countryCode_end

			//20180831_tudn_start cap nhat trang thai
			Object active = filters.get("active");
			if(active!=null && !active.toString().isEmpty()) {
				query.add(Restrictions.eq("active", Long.parseLong(active.toString())));
			}
			//20180831_tudn_start cap nhat trang thai

			for(String skey: new String[]{"vendor.vendorName","nodeType.typeName","version.versionName"}){
				Object field = filters.get(skey);
				if(field!=null && !field.toString().isEmpty()){
					query.add(Restrictions.isNotNull(skey));
					query.add(Restrictions.ilike(skey, field.toString(), MatchMode.ANYWHERE));
				}
			}
			if(searchNodeLab!=null && searchNodeLab!=-1){
				query.add(Restrictions.eq("isLab",searchNodeLab));
			}
			int pageNumber = (first/pageSize)+1;
			data = daoService.findList(query, orderBy, pageNumber, pageSize);
			dataSize = (int)daoService.count(query);
			
			
			
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
		
		return object;
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

	public Set<VendorNodeType> getVendorNodeTypes() {
		return vendorNodeTypes;
	}

	public void setVendorNodeTypes(Set<VendorNodeType> vendorNodeTypes) {
		this.vendorNodeTypes = vendorNodeTypes;
	}

	public void setSearchNodeLab(Integer searchNodeLab) {
		this.searchNodeLab = searchNodeLab;
	}
}