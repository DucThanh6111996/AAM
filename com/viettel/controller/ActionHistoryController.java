package com.viettel.controller;

// Created Aug 1, 2016 9:16:03 AM by quanns2

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.lazy.LazyActionHistory;
import com.viettel.model.Action;
import com.viettel.model.ActionHistory;
import com.viettel.persistence.ActionHistoryService;
import com.viettel.util.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.Visibility;

/**
 * 
 * @author quanns2
 *
 */
@ViewScoped
@ManagedBean
public class ActionHistoryController implements Serializable {
	private static Logger logger = Logger.getLogger(ActionHistoryController.class);

	@ManagedProperty(value = "#{actionHistoryService}")
	ActionHistoryService actionHistoryService;

	public void setActionHistoryService(ActionHistoryService actionHistoryService) {
		this.actionHistoryService = actionHistoryService;
	}

	private LazyDataModel<ActionHistory> lazyDataModel;
	private ActionHistory selectedObj;
	private ActionHistory newObj;

	private boolean isEdit;

	private Long searchId;
	private Long searchActionId;
	private Date searchStartTime;
	private Date searchEndTime;
	private Integer searchStatus;
	private String searchRunUser;
	private String searchRollbackUser;
	private Date searchStartRollbackTime;
	private String searchEndRollbackTime;
	private Integer searchRollbackStatus;
	private List<Boolean> columnVisibale =new ArrayList<>();

	private String logDetail;
	
	@ManagedProperty(value = "#{historyDetailController}")
	private HistoryDetailController historyDetailController;
	
	public void onToggler(ToggleEvent e){
		this.columnVisibale.set((Integer) e.getData(),e.getVisibility()== Visibility.VISIBLE);
	}
	
	public void setHistory(ActionHistory history){
		try {
			this.historyDetailController.setup(history);
			TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot()
					.findComponent("form").findComponent("lst");
			Tab tab = (Tab) tabView.findComponent("edittab");
			// tab.setDisabled(true);
			if (!tab.isRendered()) {
				tab.setRendered(true);
			}
			tabView.setActiveIndex(1);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void loadHistory() {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest req = (HttpServletRequest) context.getRequest();
		String actionIdStr = req.getParameter("action");

		if (actionIdStr != null) {
			Long historyId = Long.valueOf(actionIdStr);
			ActionHistory history;
			try {
				history = actionHistoryService.findById(historyId);
				setHistory(history);

				RequestContext.getCurrentInstance().update("form:lst");

			} catch (AppException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}

	@PostConstruct
	public void onStart() {
/*		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest req = (HttpServletRequest) context.getRequest();
		String actionIdStr = req.getParameter("action");*/

		clear();
		((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form").findComponent("lst").findComponent("objectTable"))
				.setFirst(0);
		Map<String, String> filters = new HashMap<String, String>();

		lazyDataModel = new LazyActionHistory(actionHistoryService, filters);
		this.columnVisibale =new ArrayList<Boolean>(){
			private static final long serialVersionUID = 1L;
			{
				add(Boolean.TRUE);
				add(Boolean.TRUE);
				add(Boolean.TRUE);
				add(Boolean.TRUE);
				add(Boolean.TRUE);
				add(Boolean.FALSE);
				add(Boolean.FALSE);
				add(Boolean.FALSE);
				add(Boolean.FALSE);
				add(Boolean.FALSE);
				add(Boolean.TRUE);
				add(Boolean.FALSE);
				add(Boolean.TRUE);
				add(Boolean.TRUE);
				add(Boolean.FALSE);
				add(Boolean.FALSE);
				add(Boolean.FALSE);
				add(Boolean.TRUE);
			}	
		};

		/*if (actionIdStr != null) {
			Long historyId = Long.valueOf(actionIdStr);
			ActionHistory history;
			try {
				history = actionHistoryService.findById(historyId);
				setHistory(history);

				RequestContext.getCurrentInstance().update("form:lst");

			} catch (AppException e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot()
					.findComponent("form").findComponent("lst");
			Tab tab = (Tab) tabView.findComponent("edittab");
			tab.setRendered(false);
			RequestContext.getCurrentInstance().update("form:lst");

		}*/
	}

	public void search() {
		((DataTable) FacesContext.getCurrentInstance().getViewRoot().
				findComponent("form").findComponent("lst").findComponent("mainTab")
				.findComponent("objectTable"))
				.setFirst(0);

		Map<String, String> filters = new HashMap<String, String>();

		if (StringUtils.isNotEmpty(searchRunUser))
			filters.put("runUser", searchRunUser);
		if (StringUtils.isNotEmpty(searchRollbackUser))
			filters.put("rollbackUser", searchRollbackUser);


		lazyDataModel = new LazyActionHistory(actionHistoryService, filters);
	}

	public void prepareEdit(ActionHistory obj) {
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
		newObj = new ActionHistory();
	}

	public void duplicate(ActionHistory obj) {
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

	public void download(Action obj) throws FileNotFoundException {
		File logFile = new File(UploadFileUtils.getLogFolder(obj) +  ".zip");

		try {
			FacesContext facesContext = FacesContext.getCurrentInstance();
			ActionController.writeExcelToResponse(((HttpServletResponse) facesContext.getExternalContext().getResponse()), logFile.getName(), logFile.getPath());
			facesContext.responseComplete();

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {

		}
	}

	public void saveOrUpdate() {
		FacesMessage msg = null;
		Date startTime = new Date();
		try {
			if (isEdit) {
				// oldPass = selectedObj.getPassword();
			}

			selectedObj = new ActionHistory();

			BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
			BeanUtils.copyProperties(selectedObj, newObj);

			if (!isEdit) {
				selectedObj.setId(null);
			}
			actionHistoryService.saveOrUpdate(selectedObj);
			if (!isEdit) {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("insert.successful"), "");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("update.successful"), "");
			}
			//20180620_tudn_start ghi log DB
			try {
				LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
						LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionHistoryController.class.getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName(),
						(isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
						selectedObj.toString(), LogUtils.getRequestSessionId());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			//20180620_tudn_end ghi log DB
		} catch (Exception e) {
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("update.failed"), "");
			logger.error(e.getMessage(), e);
		} finally {
			FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
			newObj = new ActionHistory();
			isEdit = false;
		}

		RequestContext.getCurrentInstance().execute("editDialog.hide()");
	}

	public void delete() {
		FacesMessage msg = null;
		Date startTime = new Date();
		try {
			actionHistoryService.delete(selectedObj);
			//20180620_tudn_start ghi log DB
			try {
				LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
						LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionHistoryController.class.getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName(),
						LogUtils.ActionType.DELETE,
						selectedObj.toString(), LogUtils.getRequestSessionId());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			//20180620_tudn_end ghi log DB
			msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("delete.successful"), "");
		} catch (SysException | AppException e) {
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("delete.failed"), "");
			logger.error(e.getMessage(), e);
		} finally {
			FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
		}
	}

	public String statusDetail(Integer status) {
		if (status == null)
			return "";
		String detail = "";

		switch (status) {
			case Constant.STAND_BY_STATUS:
				detail = MessageUtil.getResourceBundleMessage("waiting");
				break;
			case Constant.RUNNING_STATUS:
				detail = MessageUtil.getResourceBundleMessage("executing");
				break;
			case AamConstants.RUN_STATUS.FINISH_SUCCESS_STATUS_STOP:
			case Constant.FINISH_SUCCESS_STATUS:
				detail = MessageUtil.getResourceBundleMessage("successful");
				break;
			case Constant.FINISH_FAIL_STATUS:
				detail = MessageUtil.getResourceBundleMessage("fail");
				break;
			case Constant.CANCEL_STATUS:
				detail = MessageUtil.getResourceBundleMessage("cancel");
				break;
			case Constant.NOT_ALLOW_STATUS:
				detail = MessageUtil.getResourceBundleMessage("forbidden");
				break;
			case Constant.WARNING_STATUS:
				detail = MessageUtil.getResourceBundleMessage("alert");
				break;
			case Constant.FINISH_SUCCESS_WITH_WARNING:
				detail = MessageUtil.getResourceBundleMessage("successful.warning");
				break;
			default:
				break;
		}

		return detail;
	}

	public LazyDataModel<ActionHistory> getLazyDataModel() {
		return lazyDataModel;
	}

	public void setLazyDataModel(LazyDataModel<ActionHistory> lazyDataModel) {
		this.lazyDataModel = lazyDataModel;
	}

	public ActionHistory getSelectedObj() {
		return selectedObj;
	}

	public void setSelectedObj(ActionHistory selectedObj) {
		this.selectedObj = selectedObj;
	}

	public ActionHistory getNewObj() {
		return newObj;
	}

	public void setNewObj(ActionHistory newObj) {
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

	public Date getSearchStartTime() {
		return this.searchStartTime;
	}

	public void setSearchStartTime(Date searchStartTime) {
		this.searchStartTime = searchStartTime;
	}

	public Date getSearchEndTime() {
		return this.searchEndTime;
	}

	public void setSearchEndTime(Date searchEndTime) {
		this.searchEndTime = searchEndTime;
	}

	public Integer getSearchStatus() {
		return this.searchStatus;
	}

	public void setSearchStatus(Integer searchStatus) {
		this.searchStatus = searchStatus;
	}

	public String getSearchRunUser() {
		return this.searchRunUser;
	}

	public void setSearchRunUser(String searchRunUser) {
		this.searchRunUser = searchRunUser;
	}

	public String getSearchRollbackUser() {
		return this.searchRollbackUser;
	}

	public void setSearchRollbackUser(String searchRollbackUser) {
		this.searchRollbackUser = searchRollbackUser;
	}

	public Date getSearchStartRollbackTime() {
		return this.searchStartRollbackTime;
	}

	public void setSearchStartRollbackTime(Date searchStartRollbackTime) {
		this.searchStartRollbackTime = searchStartRollbackTime;
	}

	public String getSearchEndRollbackTime() {
		return this.searchEndRollbackTime;
	}

	public void setSearchEndRollbackTime(String searchEndRollbackTime) {
		this.searchEndRollbackTime = searchEndRollbackTime;
	}

	public Integer getSearchRollbackStatus() {
		return this.searchRollbackStatus;
	}

	public void setSearchRollbackStatus(Integer searchRollbackStatus) {
		this.searchRollbackStatus = searchRollbackStatus;
	}
	
	public List<Boolean> getColumnVisibale() {
		return columnVisibale;
	}
	
	public HistoryDetailController getHistoryDetailController() {
		return historyDetailController;
	}

	public void setHistoryDetailController(HistoryDetailController historyDetailController) {
		this.historyDetailController = historyDetailController;
	}

	public String getLogDetail() {
		return logDetail;
	}

	public void setLogDetail(String logDetail) {
		this.logDetail = logDetail;
	}
}
