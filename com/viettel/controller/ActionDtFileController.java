package com.viettel.controller;

// Created Oct 4, 2016 5:24:54 AM by quanns2

import com.viettel.bean.Service;
import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.model.ActionDtFile;

import com.viettel.lazy.LazyActionDtFile;
import com.viettel.persistence.ActionDtFileService;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import com.viettel.persistence.ActionService;
import com.viettel.persistence.IimService;
import com.viettel.util.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

/**
 * 
 * @author quanns2
 *
 */
@ViewScoped
@ManagedBean
public class ActionDtFileController implements Serializable {
	private static Logger logger = Logger.getLogger(ActionDtFileController.class);

	@ManagedProperty(value = "#{actionDtFileService}")
	ActionDtFileService actionDtFileService;

	public void setActionDtFileService(ActionDtFileService actionDtFileService) {
		this.actionDtFileService = actionDtFileService;
	}

	@ManagedProperty(value = "#{actionService}")
	ActionService actionService;

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	public void setIimService(IimService iimService) {
		this.iimService = iimService;
	}

	@ManagedProperty(value = "#{iimService}")
	IimService iimService;

	private LazyDataModel<ActionDtFile> lazyDataModel;
	private ActionDtFile selectedObj;
	private ActionDtFile newObj;

	private boolean isEdit;

	private Long searchId;
	private Long searchAppGroupId;
	private String searchImpactFile;
	private String searchRollbackFile;
	private String searchName;
	private String searchImpactDescription;
	private String searchRollbackDescription;
	private String searchLocalFilename;
	private Integer searchImpactTime;

	private String username;
	private List<Service> services;
	private List<SelectItem> appGroups;

	@PostConstruct
	public void onStart() {
		username = SessionUtil.getCurrentUsername() == null ? "N/A" : SessionUtil.getCurrentUsername();
		clear();
		((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form").findComponent("objectTable")).setFirst(0);
		Map<String, String> filters = new HashMap<>();

		lazyDataModel = new LazyActionDtFile(actionDtFileService, filters);

		appGroups = new ArrayList<>();
		try {
//			catAppGroups = catAppGroupService.findByUser(username);
			if ("quanns2".equals(username))
				services = iimService.findServiceByUser(AamConstants.NATION_CODE.VIETNAM, "sonnh30");
			else
				services = iimService.findServiceByUser(AamConstants.NATION_CODE.VIETNAM, username);
			for (Service service : services) {
				appGroups.add(new SelectItem(service.getServiceId(), service.getServiceName()));
			}
		} catch (AppException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void search() {
		((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form").findComponent("objectTable")).setFirst(0);
		Map<String, String> filters = new HashMap<>();

		if (StringUtils.isNotEmpty(searchImpactFile))
			filters.put("impactFile", searchImpactFile);
		if (StringUtils.isNotEmpty(searchRollbackFile))
			filters.put("rollbackFile", searchRollbackFile);
		if (StringUtils.isNotEmpty(searchName))
			filters.put("name", searchName);
		if (StringUtils.isNotEmpty(searchImpactDescription))
			filters.put("impactDescription", searchImpactDescription);
		if (StringUtils.isNotEmpty(searchRollbackDescription))
			filters.put("rollbackDescription", searchRollbackDescription);
		if (StringUtils.isNotEmpty(searchLocalFilename))
			filters.put("localFilename", searchLocalFilename);

		lazyDataModel = new LazyActionDtFile(actionDtFileService, filters);
	}

	public void handleUploadImpactFile(FileUploadEvent event) {
		UploadedFile file = event.getFile();
		String uploadFolder = UploadFileUtils.getImpactFileFolder();
		if (file != null) {
			BigDecimal seq = null;
			try {
				seq = actionService.nextFileSeq();
			} catch (AppException e) {
				logger.error(e.getMessage(), e);
			}
			String sourceCode = Util.convertUTF8ToNoSign(FilenameUtils.getBaseName(file.getFileName()) + "_" + seq + "." + FilenameUtils.getExtension(file.getFileName()));

			FileHelper.uploadFile(uploadFolder, file, sourceCode);
			newObj.setImpactFile(event.getFile().getFileName());
			newObj.setLocalFilename(sourceCode);
		}
	}

	public void handleUploadRollbackFile(FileUploadEvent event) {
		UploadedFile file = event.getFile();
		String uploadFolder = UploadFileUtils.getImpactFileFolder();
		if (file != null) {
			BigDecimal seq = null;
			try {
				seq = actionService.nextFileSeq();
			} catch (AppException e) {
				logger.error(e.getMessage(), e);
			}
			String sourceCode = Util.convertUTF8ToNoSign(FilenameUtils.getBaseName(file.getFileName()) + "_" + seq + "." + FilenameUtils.getExtension(file.getFileName()));

			FileHelper.uploadFile(uploadFolder, file, sourceCode);
			newObj.setRollbackFile(event.getFile().getFileName());
			newObj.setLocalRollbackFilename(sourceCode);
		}
	}

	public StreamedContent downloadFile(ActionDtFile obj) {
		StreamedContent fileInput = null;
		String filePath = UploadFileUtils.getImpactFileFolder() + File.separator + obj.getLocalFilename();

		try {
			fileInput = new DefaultStreamedContent(new FileInputStream(filePath), "", obj.getImpactFile());

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return fileInput;
	}
	public StreamedContent downloadRollbackFile(ActionDtFile obj) {
		StreamedContent fileInput = null;
		String filePath = UploadFileUtils.getImpactFileFolder() + File.separator + obj.getLocalRollbackFilename();

		try {
			fileInput = new DefaultStreamedContent(new FileInputStream(filePath), "", obj.getRollbackFile());

		} catch (FileNotFoundException e) {
			logger.error(e.getMessage(), e);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		return fileInput;
	}

	public void prepareEdit(ActionDtFile obj) {
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
		newObj = new ActionDtFile();
	}

	public void duplicate(ActionDtFile obj) {
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

			selectedObj = new ActionDtFile();

			BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
			BeanUtils.copyProperties(selectedObj, newObj);

			if (!isEdit) {
				selectedObj.setId(null);
			}
			actionDtFileService.saveOrUpdate(selectedObj);
			if (!isEdit) {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("insert.successful"), "");
			} else {
				msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("update.successful"), "");
			}
			/*
            Ghi log tac dong nguoi dung
            */
			//20180620_tudn_start ghi log DB
			try {
				LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
						LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDtFileController.class.getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName(),
						(isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
						selectedObj.toString(), LogUtils.getRequestSessionId());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			//20180620_tudn_end ghi log DB
			RequestContext.getCurrentInstance().execute("PF('editDialog').hide()");
		} catch (IllegalAccessException | InvocationTargetException | SysException | AppException e) {
			msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("update.failed"), "");
			logger.error(e.getMessage(), e);
		} finally {
			FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
			newObj = new ActionDtFile();
			isEdit = false;
		}
	}

	public void delete() {
		FacesMessage msg = null;
		Date startTime = new Date();
		try {
			actionDtFileService.delete(selectedObj);
			//20180620_tudn_start ghi log DB
			try {
				LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
						LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDtFileController.class.getName(),
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

	public LazyDataModel<ActionDtFile> getLazyDataModel() {
		return lazyDataModel;
	}

	public void setLazyDataModel(LazyDataModel<ActionDtFile> lazyDataModel) {
		this.lazyDataModel = lazyDataModel;
	}

	public ActionDtFile getSelectedObj() {
		return selectedObj;
	}

	public void setSelectedObj(ActionDtFile selectedObj) {
		this.selectedObj = selectedObj;
	}

	public ActionDtFile getNewObj() {
		return newObj;
	}

	public void setNewObj(ActionDtFile newObj) {
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

	public Long getSearchAppGroupId() {
		return this.searchAppGroupId;
	}

	public void setSearchAppGroupId(Long searchAppGroupId) {
		this.searchAppGroupId = searchAppGroupId;
	}

	public String getSearchImpactFile() {
		return this.searchImpactFile;
	}

	public void setSearchImpactFile(String searchImpactFile) {
		this.searchImpactFile = searchImpactFile;
	}

	public String getSearchRollbackFile() {
		return this.searchRollbackFile;
	}

	public void setSearchRollbackFile(String searchRollbackFile) {
		this.searchRollbackFile = searchRollbackFile;
	}

	public String getSearchName() {
		return this.searchName;
	}

	public void setSearchName(String searchName) {
		this.searchName = searchName;
	}

	public String getSearchImpactDescription() {
		return this.searchImpactDescription;
	}

	public void setSearchImpactDescription(String searchImpactDescription) {
		this.searchImpactDescription = searchImpactDescription;
	}

	public String getSearchRollbackDescription() {
		return this.searchRollbackDescription;
	}

	public void setSearchRollbackDescription(String searchRollbackDescription) {
		this.searchRollbackDescription = searchRollbackDescription;
	}

	public String getSearchLocalFilename() {
		return this.searchLocalFilename;
	}

	public void setSearchLocalFilename(String searchLocalFilename) {
		this.searchLocalFilename = searchLocalFilename;
	}

	public Integer getSearchImpactTime() {
		return this.searchImpactTime;
	}

	public void setSearchImpactTime(Integer searchImpactTime) {
		this.searchImpactTime = searchImpactTime;
	}

	public List<SelectItem> getAppGroups() {
		return appGroups;
	}

	public void setAppGroups(List<SelectItem> appGroups) {
		this.appGroups = appGroups;
	}

	public List<Service> getServices() {
		return services;
	}

	public void setServices(List<Service> services) {
		this.services = services;
	}
}
