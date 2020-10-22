package com.viettel.it.controller;

import com.rits.cloning.Cloner;
import com.viettel.controller.AppException;
import com.viettel.controller.SysException;
import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.Province;
import com.viettel.it.model.ServiceTemplate;
import com.viettel.it.model.ServiceTemplateId;
import com.viettel.it.persistence.ProvinceServiceImpl;
import com.viettel.it.persistence.ServiceTemplateServiceImpl;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.util.SessionUtil;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.*;

@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class ServiceTemplateController implements Serializable {
	
	private ServiceTemplate serviceTemplate;
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	@ManagedProperty("#{serviceTemplateService}")
	private ServiceTemplateServiceImpl serviceTemplateService;
	
	private LazyDataModel<ServiceTemplate> lazyDataService;
	
	private List<Province> provinces ;
	
	@PostConstruct
	public void onStart(){
		lazyDataService = new LazyDataModelBaseNew<>(serviceTemplateService);
		try {
			provinces = new ProvinceServiceImpl().findList();
		} catch (SysException | AppException e) {
			LOGGER.error(e.getMessage(), e);
			provinces =new ArrayList<Province>();
		}
	}

	public void clean(){
		serviceTemplate =  new ServiceTemplate();
		serviceTemplate.setId(new ServiceTemplateId());
	}
	
	public void saveOrUpdate(){
		try {
			Date startTime = new Date();
			List<ServiceTemplate> serviceTemplates = new ArrayList<ServiceTemplate>();
			//Check data existed
			Map<String, Object> filters = new HashMap<String, Object>();
			//"VENDOR_ID", "VERSION_ID", "SERVICE_CODE", "ACTION"
			filters.put("id.vendorId", serviceTemplate.getVendor().getVendorId());
			filters.put("id.versionId", serviceTemplate.getVersion().getVersionId());
			filters.put("id.serviceCode", serviceTemplate.getId().getServiceCode());
			filters.put("id.action", serviceTemplate.getId().getAction());
			filters.put("id.subring", serviceTemplate.getId().getSubring());
			
			Cloner cloner = new Cloner();
			for (String provinceCode : serviceTemplate.getProvinceCodes()) {
				filters.put("id.provinceCode-EXAC", provinceCode);
				if(serviceTemplateService.findList(filters ).size()>0){
					MessageUtil.setWarnMessage(MessageUtil.getResourceBundleMessage(("warn.service.template.existed"))+": "+provinceCode);;
					continue;
				}
				serviceTemplate.getId().setFlowTemplateId(serviceTemplate.getFlowTemplates().getFlowTemplatesId());
				serviceTemplate.getId().setVendorId(serviceTemplate.getVendor().getVendorId());
				serviceTemplate.getId().setVersionId(serviceTemplate.getVersion().getVersionId());
				serviceTemplate.getId().setProvinceCode(provinceCode);
				serviceTemplates.add(cloner.deepClone(serviceTemplate));
			}
				
			serviceTemplateService.saveOrUpdate(serviceTemplates);
			//20180620_tudn_start ghi log DB
			try {
				LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
						LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ServiceTemplateController.class.getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName(),
						LogUtils.ActionType.UPDATE ,
						serviceTemplates.toString(), LogUtils.getRequestSessionId());
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		//20180620_tudn_end ghi log DB
			MessageUtil.setInfoMessageFromRes("common.message.success");
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
			MessageUtil.setErrorMessageFromRes("error.save.unsuccess");
		}
		
	}
	
	public void preEdit(ServiceTemplate serviceTemplate){
		this.serviceTemplate = serviceTemplate;
		
	}
	
	public void delete(){
		try {
			Date startTime = new Date();
			serviceTemplateService.delete(serviceTemplate);
			//20180620_tudn_start ghi log DB
			try {
				LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
						LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ServiceTemplateController.class.getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName(),
						LogUtils.ActionType.DELETE,
						serviceTemplate.toString(), LogUtils.getRequestSessionId());
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
			//20180620_tudn_end ghi log DB
			MessageUtil.setInfoMessageFromRes("common.message.success");
		} catch (Exception e) {
			LOGGER.error(e.getMessage(), e);
		}
	}
	
	
	public ServiceTemplateServiceImpl getServiceTemplateService() {
		return serviceTemplateService;
	}

	public void setServiceTemplateService(ServiceTemplateServiceImpl serviceTemplateService) {
		this.serviceTemplateService = serviceTemplateService;
	}

	public ServiceTemplate getServiceTemplate() {
		return serviceTemplate;
	}

	public void setServiceTemplate(ServiceTemplate serviceTemplate) {
		this.serviceTemplate = serviceTemplate;
	}

	public LazyDataModel<ServiceTemplate> getLazyDataService() {
		return lazyDataService;
	}

	public void setLazyDataService(LazyDataModel<ServiceTemplate> lazyDataService) {
		this.lazyDataService = lazyDataService;
	}

	public List<Province> getProvinces() {
		return provinces;
	}

	public void setProvinces(List<Province> provinces) {
		this.provinces = provinces;
	}
}
