package com.viettel.it.controller;

import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.Node;
import com.viettel.it.model.RelationNode;
import com.viettel.it.object.RelationNodeModelExcel;
import com.viettel.it.persistence.NodeServiceImpl;
import com.viettel.it.persistence.RelationNodeServiceImpl;
import com.viettel.it.util.Importer;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
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
public class RelationNodeController {
	
	protected static final Logger logger = LoggerFactory.getLogger(RelationNodeController.class);

	@ManagedProperty(value="#{relationNodeService}")
	private RelationNodeServiceImpl relationNodeService;

	public void setRelationNodeService(RelationNodeServiceImpl relationNodeService) {
		this.relationNodeService = relationNodeService;
	} 
	
	private LazyDataModel<RelationNode> lazyRelationNode;
	private RelationNode selectedRelationNode;
	
	private Node node;
	private Node nodeRelation;
	private boolean isEdit;
	private StreamedContent file;
	
	@PostConstruct
	public void onStart() {
		try {
			InputStream stream = FacesContext.getCurrentInstance().getExternalContext().getResourceAsStream("/templates/import/Template_import_relation_node.xlsx");
		    file = new DefaultStreamedContent(stream, "application/xls", "Template_import_relation_node.xlsx");
		        
			lazyRelationNode = new LazyDataModelBaseNew<>(relationNodeService);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public List<Node> autoCompleNode(String nodeCode) {
		List<Node> lstNode;
		Map<String, Object> filters = new HashMap<>();
		if (nodeCode != null) {
			filters.put("nodeCode", nodeCode);
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
//			InputStream inputStream = event.getFile().getInputstream();
//			Map<Integer, String> rows = new HashMap<Integer, String>();
//			Workbook workBook;
			
			if (event.getFile().getFileName().endsWith("xls")) {
//				workBook = new HSSFWorkbook(inputStream);
			} else if (event.getFile().getFileName().endsWith("xlsx")) {
//				workBook = new XSSFWorkbook(inputStream);
			} else {
				MessageUtil.setErrorMessageFromRes("message.invalid.header");
				return;
			}

			Importer<RelationNodeModelExcel> importer = new Importer<RelationNodeModelExcel>() {

				protected Class<RelationNodeModelExcel> getDomainClass() {
					// TODO Auto-generated method stub
					return RelationNodeModelExcel.class;
				}

				@Override
				protected Map<Integer, String> getIndexMapFieldClass() {
					// TODO Auto-generated method stub
					Map<Integer, String> model = new HashMap<Integer, String>();
					
					model.put(1, "nodeCode");
					model.put(2, "nodeType");
					model.put(3, "interfacePort");
					model.put(4, "nodeCodeRelation");
					model.put(5, "nodeTypeRelation");
					model.put(6, "interfacePortRelation");
					
					return model;
				}

				@Override
				protected String getDateFormat() {
					// TODO Auto-generated method stub
					return null;
				}
			};
			
//			Sheet sheet = workBook.getSheetAt(0);
//			ExcelParser<RelationNodeModelExcel> ex = new ExcelParser<RelationNodeModelExcel>(1);

//			rows = MapExcelModel.getRelationNodeModel();
//			List<RelationNodeModelExcel> lstRelationNode = ex.getObjects(sheet,RelationNodeModelExcel.class, rows);
			List<RelationNodeModelExcel> lstRelationNode = importer.getDatas(event, 0, "2-");
		
            if (lstRelationNode.isEmpty()) {
            	MessageUtil.setErrorMessageFromRes("datatable.empty");
                return;
            } else {
            	if (valFileImport(lstRelationNode)) {
            		
            		// Kiem tra neu cung node code thi thuc hien update du lieu
            		List<RelationNode> lstRelationNodeImport = new ArrayList<>();
            		RelationNode node;
            		Map<String, Object> filters = new HashMap<>();
             		for (RelationNodeModelExcel nodeExcel : lstRelationNode) {
            			try {
            				filters.put("nodeCode", nodeExcel.getNodeCode());
							node = new RelationNodeServiceImpl().findListExac(filters, null).get(0);
						} catch (Exception e) {
            				logger.error(e.getMessage(), e);
							node = null;
						}
            			if (node == null) {
            				node = new RelationNode();
            				node.setInterfacePort(nodeExcel.getInterfacePort());
            				node.setInterfacePortRelation(nodeExcel.getInterfacePortRelation());
            				node.setNodeCode(nodeExcel.getNodeCode());
            				node.setNodeCodeRelation(nodeExcel.getNodeCodeRelation());
            				node.setNodeType(nodeExcel.getNodeType());
            				node.setNodeTypeRelation(nodeExcel.getNodeTypeRelation());
            			}
            			lstRelationNodeImport.add(node);
            		}
             		
             		if (!lstRelationNodeImport.isEmpty()) {
             			new RelationNodeServiceImpl().saveOrUpdate(lstRelationNodeImport);
             			MessageUtil.setInfoMessageFromRes("label.action.updateOk");
             		} else {
             			MessageUtil.setErrorMessageFromRes("label.noData.save");
             		}
            	} 
            }
		} catch (Exception e) {
			MessageUtil.setErrorMessageFromRes("label.action.updateFail");
			logger.error(e.getMessage(), e);
		}
	}
	
	public boolean valFileImport(List<RelationNodeModelExcel> lstRelationNode) {
		boolean check = true;
		
		boolean isErrEmptyData = false;
		boolean isErrDuplicate = false;
		for(RelationNodeModelExcel node : lstRelationNode) {
			try {
				if (node.getInterfacePort() == null 
						|| node.getInterfacePort().trim().isEmpty()
						|| node.getInterfacePortRelation() == null
						|| node.getInterfacePortRelation().trim().isEmpty()
						|| node.getNodeCode() == null 
						|| node.getNodeCode().trim().isEmpty()
						|| node.getNodeCodeRelation() == null
						|| node.getNodeCodeRelation().trim().isEmpty()) {
					isErrEmptyData = true;
				} else if (node.getNodeCode().trim().equals(node.getNodeCodeRelation().trim())) {
					isErrDuplicate = true;
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				logger.error(e.getMessage());
			}
		}
		
		if (isErrEmptyData) {
			MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
			check = false;
		} else if (isErrDuplicate) {
			MessageUtil.setErrorMessageFromRes("label.err.duplicate.nodecode");
			check = false;
		}
		
		return check;
	}
	
	public void onSelectNode(SelectEvent event) {
		Node node = (Node) event.getObject();
		selectedRelationNode.setNodeType(node.getNodeType().getTypeName());
	}
	
	public void onSelectNodeRelation(SelectEvent event) {
		Node node = (Node) event.getObject();
		selectedRelationNode.setNodeTypeRelation(node.getNodeType().getTypeName());
	}
	
	public void prepareEdit(RelationNode nodeData) {
		selectedRelationNode = nodeData;
		try {
			isEdit = true;
			Map<String, Object> filters = new HashMap<>();
			filters.put("nodeCode", nodeData.getNodeCode());
			
			List<Node> lstNode = new NodeServiceImpl().findListExac(filters, null);
			if (lstNode != null && !lstNode.isEmpty()) {
				node = lstNode.get(0);
			}
			filters.clear();
			// get node relation
			filters.put("nodeCode", nodeData.getNodeCodeRelation());
			lstNode = new NodeServiceImpl().findListExac(filters, null);
			if (lstNode != null && !lstNode.isEmpty()) {
				nodeRelation = lstNode.get(0);
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void saveRelationNode() {
		Date startTime = new Date();
		try {
			if (validateData()) {
				
				RelationNode relationNodeSave = new RelationNode();
				if (isEdit) {
					relationNodeSave.setId(selectedRelationNode.getId());
				} 
				relationNodeSave.setInterfacePort(selectedRelationNode.getInterfacePort());
				relationNodeSave.setInterfacePortRelation(selectedRelationNode.getInterfacePortRelation());
				
				relationNodeSave.setNodeCode(node.getNodeCode());
				relationNodeSave.setNodeCodeRelation(nodeRelation.getNodeCode());
				
				relationNodeSave.setNodeType(selectedRelationNode.getNodeType());
				relationNodeSave.setNodeTypeRelation(selectedRelationNode.getNodeTypeRelation());
				
				relationNodeService.saveOrUpdate(relationNodeSave);
				clear();
				//20180620_tudn_start ghi log DB
				try {
					LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
							LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDbServerController.class.getName(),
							Thread.currentThread().getStackTrace()[1].getMethodName(),
							(isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
							relationNodeSave.toString(), LogUtils.getRequestSessionId());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				//20180620_tudn_end ghi log DB
				MessageUtil.setInfoMessageFromRes("label.action.updateOk");
				
				RequestContext.getCurrentInstance().execute("PF('dlgRelationNode').hide()");
			}
		} catch (Exception e) {
			MessageUtil.setErrorMessageFromRes("label.action.updateFail");
			logger.error(e.getMessage(), e);
		} 
	}
	
	public void prepareDelRelationNode(RelationNode relationNode) {
		if (relationNode != null) {
			selectedRelationNode = relationNode;
		}
	}
	
	public void delRelationNode() {
		if (selectedRelationNode != null) {
			try {
				Date startTime = new Date();
				relationNodeService.delete(selectedRelationNode);
				//20180620_tudn_start ghi log DB
				try {
					LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
							LogUtils.getRemoteIpClient(), LogUtils.getUrl(), RelationNodeController.class.getName(),
							Thread.currentThread().getStackTrace()[1].getMethodName(),
							LogUtils.ActionType.DELETE,
							selectedRelationNode.toString(), LogUtils.getRequestSessionId());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				//20180620_tudn_end ghi log DB
				selectedRelationNode = new RelationNode();
				MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
			} catch (Exception e) {
				MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	private boolean validateData() {
		boolean val = true;
		// Kiem tra xem cac truong da duoc nhap day du du lieu chua
		if (node == null
				|| nodeRelation == null
				|| selectedRelationNode.getInterfacePort() == null
				|| selectedRelationNode.getInterfacePort().trim().isEmpty()
				|| selectedRelationNode.getInterfacePortRelation() == null
				|| selectedRelationNode.getInterfacePortRelation().trim().isEmpty()) {
			MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
			val = false;
		}
		
		// Kiem tra tinh logic cua du lieu
		if (val) {
			if (node.getNodeCode().trim().equals(nodeRelation.getNodeCode().trim())) {
				MessageUtil.setErrorMessageFromRes("label.err.duplicate.nodecode");
				val = false;
			}
		}
		
		return val;
	}
	
	public void clear() {
		selectedRelationNode = new RelationNode();
		node = null;
		nodeRelation = null;
		isEdit = false;
	}

	public LazyDataModel<RelationNode> getLazyRelationNode() {
		return lazyRelationNode;
	}

	public void setLazyRelationNode(LazyDataModel<RelationNode> lazyRelationNode) {
		this.lazyRelationNode = lazyRelationNode;
	}

	public RelationNode getSelectedRelationNode() {
		return selectedRelationNode;
	}

	public void setSelectedRelationNode(RelationNode selectedRelationNode) {
		this.selectedRelationNode = selectedRelationNode;
	}

	public Node getNodeRelation() {
		return nodeRelation;
	}

	public void setNodeRelation(Node nodeRelation) {
		this.nodeRelation = nodeRelation;
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
	
	
}
