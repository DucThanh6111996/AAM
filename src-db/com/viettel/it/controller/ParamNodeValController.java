package com.viettel.it.controller;

import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.Node;
import com.viettel.it.model.ParamNodeVal;
import com.viettel.it.object.ParamNodeValModelExcel;
import com.viettel.it.persistence.NodeServiceImpl;
import com.viettel.it.persistence.ParamNodeValServiceImpl;
import com.viettel.it.util.Importer;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.util.Constant;
import com.viettel.util.SessionUtil;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import java.io.InputStream;
import java.util.*;

@ViewScoped
@ManagedBean
public class ParamNodeValController {
	
	protected static final Logger logger = LoggerFactory.getLogger(ParamNodeValController.class);

	@ManagedProperty(value="#{paramNodeValService}")
	private ParamNodeValServiceImpl paramNodeValService;
	
	public void setParamNodeValService(ParamNodeValServiceImpl paramNodeValService) {
		this.paramNodeValService = paramNodeValService;
	}

	private LazyDataModel<ParamNodeVal> lazyParamVal;
	private ParamNodeVal selectedParamVal;
	
	private Node node;
	private boolean isEdit;
	private StreamedContent file;
	
	@PostConstruct
	public void onStart() {
		try {
			InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/templates/import/Template_import_params_node_value.xlsx");
		    file = new DefaultStreamedContent(stream, "application/xls", "Template_import_params_node_value.xlsx");
		        
		    lazyParamVal = new LazyDataModelBaseNew<>(paramNodeValService);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public List<Node> autoCompleNode(String nodeCode) {
		List<Node> lstNode;
		Map<String, Object> filters = new HashMap<>();
		if (nodeCode != null) {
			filters.put("nodeCode", nodeCode);
			filters.put("active", Constant.status.active);
		}
		try {
			lstNode = new NodeServiceImpl().findList(0, 100, filters);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			lstNode = new ArrayList<>();
		}
		return lstNode;
	}
	
	public void handUploadFile(FileUploadEvent event) {
		try {
			
			Importer<ParamNodeValModelExcel> importer = new Importer<ParamNodeValModelExcel>() {

				protected Class<ParamNodeValModelExcel> getDomainClass() {
					// TODO Auto-generated method stub
					return ParamNodeValModelExcel.class;
				}

				@Override
				protected Map<Integer, String> getIndexMapFieldClass() {
					// TODO Auto-generated method stub
					Map<Integer, String> model = new HashMap<Integer, String>();
					
					model.put(1, "nodeCode");
					model.put(2, "interfacePort");
					model.put(3, "paramKey");
					model.put(4, "paramVal");
					
					return model;
				}

				@Override
				protected String getDateFormat() {
					// TODO Auto-generated method stub
					return null;
				}
			};
			
			List<ParamNodeValModelExcel> lstParamVal = importer.getDatas(event, 0, "2-");
		
            if (lstParamVal.isEmpty()) {
            	MessageUtil.setErrorMessageFromRes("datatable.empty");
                return;
            } else {
            	if (valFileImport(lstParamVal)) {
            		
            		// Kiem tra neu cung node code thi thuc hien update du lieu
            		List<ParamNodeVal> lstParamSave = new ArrayList<>();
            		ParamNodeVal param;
            		
             		for (ParamNodeValModelExcel paramExcel : lstParamVal) {
             			param = new ParamNodeVal();
             			param.setInterfacePort(paramExcel.getInterfacePort());
             			param.setNodeCode(paramExcel.getNodeCode());
             			param.setParamKey(paramExcel.getNodeCode());
             			param.setParamValue(paramExcel.getParamVal());
             			
             			lstParamSave.add(param);
            		}
             		
             		if (!lstParamSave.isEmpty()) {
             			paramNodeValService.saveOrUpdate(lstParamSave);
             			MessageUtil.setInfoMessageFromRes("label.action.updateOk");
             		} else {
             			MessageUtil.setErrorMessageFromRes("label.noData.save");
             		}
            	} 
            }
            RequestContext.getCurrentInstance().execute("PF('dlgUploadParamVal').hide()");
		} catch (Exception e) {
			MessageUtil.setErrorMessageFromRes("label.action.updateFail");
			logger.error(e.getMessage(), e);
		}
	}
	
	public boolean valFileImport(List<ParamNodeValModelExcel> lstParamNodeVal) {
		boolean check = true;
		for(ParamNodeValModelExcel param : lstParamNodeVal) {
			try {
				if (param.getInterfacePort() == null 
						|| param.getInterfacePort().trim().isEmpty()
						|| param.getNodeCode() == null
						|| param.getNodeCode().trim().isEmpty()
						|| param.getParamKey() == null 
						|| param.getParamKey().trim().isEmpty()
						|| param.getParamVal() == null
						|| param.getParamVal().trim().isEmpty()) {
					check = false;
				} 
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		return check;
	}
	
	public void onSelectNode(SelectEvent event) {
//		Node node = (Node) event.getObject();
	}
	
	public void prepareEdit(ParamNodeVal paramNodeVal) {
		selectedParamVal = paramNodeVal;
		try {
			isEdit = true;
			Map<String, Object> filters = new HashMap<>();
			filters.put("nodeCode", paramNodeVal.getNodeCode());
			filters.put("active", Constant.status.active);

			List<Node> lstNode = new NodeServiceImpl().findListExac(filters, null);
			if (lstNode != null && !lstNode.isEmpty()) {
				node = lstNode.get(0);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void saveParamVal() {
		Date startTime = new Date();
		try {
			if (validateData()) {
				
				ParamNodeVal paramSave = new ParamNodeVal();
				if (isEdit) {
					paramSave.setId(selectedParamVal.getId());
				} 
				paramSave.setInterfacePort(selectedParamVal.getInterfacePort());
				paramSave.setParamKey(selectedParamVal.getParamKey());
				paramSave.setParamValue(selectedParamVal.getParamValue());
				paramSave.setNodeCode(node.getNodeCode());
				
				paramNodeValService.saveOrUpdate(paramSave);
				clear();
				//20180620_tudn_start ghi log DB
				try {
					LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
							LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ParamNodeValController.class.getName(),
							Thread.currentThread().getStackTrace()[1].getMethodName(),
							(isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
							paramSave.toString(), LogUtils.getRequestSessionId());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				//20180620_tudn_end ghi log DB
				
				MessageUtil.setInfoMessageFromRes("label.action.updateOk");
				
				RequestContext.getCurrentInstance().execute("PF('dlgParamNodeInfo').hide()");
			}
		} catch (Exception e) {
			MessageUtil.setErrorMessageFromRes("label.action.updateFail");
			logger.error(e.getMessage(), e);
		} 
	}
	
	private boolean validateData() {
		boolean val = true;
		// Kiem tra xem cac truong da duoc nhap day du du lieu chua
		if (node == null
				|| selectedParamVal.getInterfacePort() == null
				|| selectedParamVal.getInterfacePort().trim().isEmpty()
				|| selectedParamVal.getParamKey() == null
				|| selectedParamVal.getParamKey().trim().isEmpty()
				|| selectedParamVal.getParamValue() == null
				|| selectedParamVal.getParamValue().trim().isEmpty()) {
			MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
			val = false;
		}
		
		return val;
	}
	
	public void prepareDelParamVal(ParamNodeVal paramNodeVal) {
		if (paramNodeVal != null) {
			selectedParamVal = paramNodeVal;
		}
	}
	
	public void delParamNodeVal() {
		if (selectedParamVal != null) {
			try {
				Date startTime = new Date();
				paramNodeValService.delete(selectedParamVal);
				//20180620_tudn_start ghi log DB
				try {
					LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
							LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ParamNodeValController.class.getName(),
							Thread.currentThread().getStackTrace()[1].getMethodName(),
							LogUtils.ActionType.DELETE,
							selectedParamVal.toString(), LogUtils.getRequestSessionId());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				//20180620_tudn_end ghi log DB
				selectedParamVal = new ParamNodeVal();
				MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
			} catch (Exception e) {
				MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public void clear() {
		selectedParamVal = new ParamNodeVal();
		node = null;
		isEdit = false;
	}

	public Node getNode() {
		return node;
	}

	public void setNode(Node node) {
		this.node = node;
	}

	public boolean isEdit() {
		return isEdit;
	}

	public void setEdit(boolean isEdit) {
		this.isEdit = isEdit;
	}

	public StreamedContent getFile() {
		return file;
	}

	public void setFile(StreamedContent file) {
		this.file = file;
	}

	public LazyDataModel<ParamNodeVal> getLazyParamVal() {
		return lazyParamVal;
	}

	public void setLazyParamVal(LazyDataModel<ParamNodeVal> lazyParamVal) {
		this.lazyParamVal = lazyParamVal;
	}

	public ParamNodeVal getSelectedParamVal() {
		return selectedParamVal;
	}

	public void setSelectedParamVal(ParamNodeVal selectedParamVal) {
		this.selectedParamVal = selectedParamVal;
	}
	
	
}
