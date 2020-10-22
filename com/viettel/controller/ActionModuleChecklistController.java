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
import com.viettel.model.ActionModuleChecklist;
import com.viettel.persistence.ActionModuleChecklistService;

/**
 * 
 * @author quanns2
 *
 */
@ViewScoped
@ManagedBean
public class ActionModuleChecklistController implements Serializable {
	private static Logger logger = LogManager.getLogger(ActionModuleChecklistController.class);
	
	@ManagedProperty(value = "#{actionModuleChecklistService}")
	ActionModuleChecklistService actionModuleChecklistService;

	public void setActionModuleChecklistService(ActionModuleChecklistService actionModuleChecklistService) {
		this.actionModuleChecklistService = actionModuleChecklistService;
	}

	private LazyDataModel<ActionModuleChecklist> lazyDataModel;
	private ActionModuleChecklist selectedObj;
	private ActionModuleChecklist newObj;

	private boolean isEdit;

	private Long searchId;
	private Long searchActionModuleId;
	private Long searchChecklistId;

	@PostConstruct
	public void onStart() {
		clear();
		((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form").findComponent("objectTable"))
				.setFirst(0);
//		Map<String, String> filters = new HashMap<String, String>();

//		lazyDataModel = new LazyActionModuleChecklist(actionModuleChecklistService, filters);
	}

//	public void search() {
//		((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form").findComponent("objectTable"))
//				.setFirst(0);
//
//		Map<String, String> filters = new HashMap<String, String>();
//		if (StringUtils.isNotEmpty(searchId))
//			filters.put("id", searchId);
//		if (StringUtils.isNotEmpty(searchActionModuleId))
//			filters.put("actionModuleId", searchActionModuleId);
//		if (StringUtils.isNotEmpty(searchChecklistId))
//			filters.put("checklistId", searchChecklistId);
//
//		lazyDataModel = new LazyActionModuleChecklist(actionModuleChecklistService, filters);
//	}

	public void prepareEdit(ActionModuleChecklist obj) {
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
		newObj = new ActionModuleChecklist();
	}

	public void duplicate(ActionModuleChecklist obj) {
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
		Date startTime  = new Date();
		try {
			if (isEdit) {
				// oldPass = selectedObj.getPassword();
			}

			selectedObj = new ActionModuleChecklist();

			BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
			BeanUtils.copyProperties(selectedObj, newObj);

			if (!isEdit) {
				selectedObj.setId(null);
			}
			actionModuleChecklistService.saveOrUpdate(selectedObj);
			if (!isEdit) {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("insert.successful"), "");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("update.successful"), "");
			}
			//20180620_tudn_start ghi log DB
			try {
				LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
						LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionModuleChecklistController.class.getName(),
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
			newObj = new ActionModuleChecklist();
			isEdit = false;
		}

		RequestContext.getCurrentInstance().execute("editDialog.hide()");
	}

	public void delete() {
		FacesMessage msg = null;
		Date startTime = new Date();
		try {
			actionModuleChecklistService.delete(selectedObj);
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("delete.successful"), "");
			//20180620_tudn_start ghi log DB
			try {
				LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
						LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionModuleChecklistController.class.getName(),
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

	public LazyDataModel<ActionModuleChecklist> getLazyDataModel() {
		return lazyDataModel;
	}

	public void setLazyDataModel(LazyDataModel<ActionModuleChecklist> lazyDataModel) {
		this.lazyDataModel = lazyDataModel;
	}

	public ActionModuleChecklist getSelectedObj() {
		return selectedObj;
	}

	public void setSelectedObj(ActionModuleChecklist selectedObj) {
		this.selectedObj = selectedObj;
	}

	public ActionModuleChecklist getNewObj() {
		return newObj;
	}

	public void setNewObj(ActionModuleChecklist newObj) {
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

	public Long getSearchActionModuleId() {
		return this.searchActionModuleId;
	}

	public void setSearchActionModuleId(Long searchActionModuleId) {
		this.searchActionModuleId = searchActionModuleId;
	}

	public Long getSearchChecklistId() {
		return this.searchChecklistId;
	}

	public void setSearchChecklistId(Long searchChecklistId) {
		this.searchChecklistId = searchChecklistId;
	}
}
