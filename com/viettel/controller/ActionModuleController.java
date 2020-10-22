package com.viettel.controller;

// Created May 5, 2016 4:56:37 PM by quanns2

import java.io.Serializable;
import java.lang.reflect.InvocationTargetException;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;

import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.util.SessionUtil;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.ActionModule;
import com.viettel.persistence.ActionModuleService;

/**
 * 
 * @author quanns2
 *
 */
@ViewScoped
@ManagedBean
public class ActionModuleController implements Serializable {
	private static Logger logger = LogManager.getLogger(ActionModuleController.class);
	
	@ManagedProperty(value = "#{actionModuleService}")
	ActionModuleService actionModuleService;

	public void setActionModuleService(ActionModuleService actionModuleService) {
		this.actionModuleService = actionModuleService;
	}

	private LazyDataModel<ActionModule> lazyDataModel;
	private ActionModule selectedObj;
	private ActionModule newObj;

	private boolean isEdit;

	private Long searchId;
	private Long searchActionId;
	private Long searchModuleId;

	@PostConstruct
	public void onStart() {
		clear();
		((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form").findComponent("objectTable"))
				.setFirst(0);
//		Map<String, String> filters = new HashMap<String, String>();

//		lazyDataModel = new LazyActionModule(actionModuleService, filters);
	}

//	public void search() {
//		((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form").findComponent("objectTable"))
//				.setFirst(0);
//
//		Map<String, String> filters = new HashMap<String, String>();
//		if (StringUtils.isNotEmpty(searchId))
//			filters.put("id", searchId);
//		if (StringUtils.isNotEmpty(searchActionId))
//			filters.put("actionId", searchActionId);
//		if (StringUtils.isNotEmpty(searchModuleId))
//			filters.put("moduleId", searchModuleId);
//
//		lazyDataModel = new LazyActionModule(actionModuleService, filters);
//	}

	public void prepareEdit(ActionModule obj) {
		isEdit = true;
		selectedObj = obj;
		BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
		try {
			BeanUtils.copyProperties(newObj, obj);
			//newObj.setPassword("");
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void clear() {
		isEdit = false;
		newObj = new ActionModule();
	}

	public void duplicate(ActionModule obj) {
		isEdit = false;
		obj.setId(null);
		selectedObj = obj;
		BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
		try {
			BeanUtils.copyProperties(newObj, obj);
			// newObj.setPassword("");
		} catch (IllegalAccessException | InvocationTargetException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void saveOrUpdate() {
		FacesMessage msg = null;
		Date startTime = new Date();
		try {
			if (isEdit) {
				// oldPass = selectedObj.getPassword();
			}

			selectedObj = new ActionModule();

			BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
			BeanUtils.copyProperties(selectedObj, newObj);

			if (!isEdit) {
				selectedObj.setId(null);
			}
			actionModuleService.saveOrUpdate(selectedObj);
			if (!isEdit) {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("insert.successful"), "");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("update.successful"), "");
			}
			//20180620_tudn_start ghi log DB
			try {
				LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
						LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionModuleController.class.getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName(),
						(isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
						selectedObj.toString(), LogUtils.getRequestSessionId());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			//20180620_tudn_end ghi log DB
		} catch (IllegalAccessException | InvocationTargetException | SysException | AppException e) {
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("update.failed"), "");
			logger.error(e.getMessage(), e);
		} finally {
			FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
			newObj = new ActionModule();
			isEdit = false;
		}

		RequestContext.getCurrentInstance().execute("editDialog.hide()");
	}

	public void delete() {
		FacesMessage msg = null;
		Date startTime = new Date();
		try {
			actionModuleService.delete(selectedObj);
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("delete.successful"), "");
			//20180620_tudn_start ghi log DB
			try {
				LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
						LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionModuleController.class.getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName(),
						(isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
						selectedObj.toString(), LogUtils.getRequestSessionId());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			//20180620_tudn_end ghi log DB
		} catch (SysException | AppException e) {
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("delete.failed"), "");
			logger.error(e.getMessage(), e);
		} finally {
			FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
		}
	}

	public LazyDataModel<ActionModule> getLazyDataModel() {
		return lazyDataModel;
	}

	public void setLazyDataModel(LazyDataModel<ActionModule> lazyDataModel) {
		this.lazyDataModel = lazyDataModel;
	}

	public ActionModule getSelectedObj() {
		return selectedObj;
	}

	public void setSelectedObj(ActionModule selectedObj) {
		this.selectedObj = selectedObj;
	}

	public ActionModule getNewObj() {
		return newObj;
	}

	public void setNewObj(ActionModule newObj) {
		this.newObj = newObj;
	}

	public Boolean getIsEdit() {
		return isEdit;
	}

	public void setEdit(boolean isEdit) {
		this.isEdit = isEdit;
	}

	public Long getSearchId() {
		return this.searchId;
	}

	public void setSearchId(Long searchId) {
		this.searchId = searchId;
	}

	public Long getSearchActionId() {
		return this.searchActionId;
	}

	public void setSearchActionId(Long searchActionId) {
		this.searchActionId = searchActionId;
	}

	public Long getSearchModuleId() {
		return this.searchModuleId;
	}

	public void setSearchModuleId(Long searchModuleId) {
		this.searchModuleId = searchModuleId;
	}
}
