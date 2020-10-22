package com.viettel.it.controller;

import com.viettel.it.model.*;
import com.viettel.it.persistence.NodeActionOffImpl;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.util.SessionUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.*;

/**
 * @author Nguyễn Xuân Huy <huynx6@viettel.com.vn>
 * @sin Oct 19, 2016
 * @version 1.0 
 */
@SuppressWarnings("serial")
@ManagedBean
@ViewScoped
public class NodeLayerController implements Serializable {
	List<NodeActionOff> nodeActionOffs = new LinkedList<NodeActionOff>();
	
	@ManagedProperty(value = "#{buildTemplateFlowController}")
    BuildTemplateFlowController buildTemplateFlowController;
	
	@ManagedProperty(value = "#{actionDbServerController}")
    ActionDbServerController actionController;
	
	List<NodeType> nodeTypes = new ArrayList<NodeType>();
	
	private List<List<NodeActionOff>> nodeLayers = new LinkedList<>();
	
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
	
	public void addNodeLayer(){
		NodeActionOff nodeActionOff = new NodeActionOff();
		nodeActionOff.setActionOfFlows(getGroupAction());
		nodeActionOffs.add(nodeActionOff);
	}
	public void buildNodeLayer(){
		getNodeType();
		loadNodeLayer();
		loadNodeActionOff();
	}
	
	public void removeNodeLayer(NodeActionOff nodeActionOff){
		nodeActionOffs.remove(nodeActionOff);
	}
	public void loadNodeLayer(){
		nodeLayers.clear();
		List<ActionOfFlow> groupActions = getGroupAction();
		for (int j=0; j< groupActions.size();j++) {
			List<NodeActionOff> list = new LinkedList<>();
			for (int i=0; i< nodeActionOffs.size();i++) {
				NodeActionOff actionOff = nodeActionOffs.get(i).clone();
				actionOff.setActionOfFlow(groupActions.get(j).clone());
				list.add(actionOff);
			}
			nodeLayers.add(list);
		}
	}
	public void loadNodeActionOff(){
		//nodeActionOffs.clear();
		FlowTemplates selectedFlowTemplate = buildTemplateFlowController.getSelectedFlowTemplate();
		List<ActionOfFlow> groupActions = getGroupAction();
		if(selectedFlowTemplate!=null){
			Map<String, Object> filters = new HashMap<String, Object>();
			filters.put("flowTemplates.flowTemplatesId", selectedFlowTemplate.getFlowTemplatesId());
			try {
				List<NodeActionOff> nodeActionOffs = new NodeActionOffImpl().findList(filters);
				for (NodeActionOff nodeActionOff : nodeActionOffs) {
					nodeActionOff.getNodeType();
					for (int i = 0; i < groupActions.size(); i++) {
						for (int j = 0; j < this.nodeActionOffs.size(); j++) {
							if(nodeLayers.get(i).get(j).equals(nodeActionOff))
								nodeLayers.get(i).get(j).setDeclare(false);
//							MutableBoolean
						}
					}
				}
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
		}
	}
	
	public List<ActionOfFlow> getGroupAction(){
		List<ActionOfFlow> actionOfFlows = new LinkedList<ActionOfFlow>();
		Map<String, ActionOfFlow> mapGroupAction = new HashMap<String, ActionOfFlow>();
		if(buildTemplateFlowController.getSelectedFlowTemplate()!=null){
			for (ActionOfFlow actionOfFlow : buildTemplateFlowController.getSelectedFlowTemplate().getActionOfFlows()) {
				if(!mapGroupAction.containsKey(actionOfFlow.getGroupActionName())){
					actionOfFlows.add(actionOfFlow.clone());
					mapGroupAction.put(actionOfFlow.getGroupActionName(), actionOfFlow);
				}
			}
		}
		return actionOfFlows;
	}
	
	public void getNodeType(){
		nodeTypes.clear();
		nodeActionOffs.clear();
		FlowTemplates selectedFlowTemplate = buildTemplateFlowController.getSelectedFlowTemplate();
		if(selectedFlowTemplate!=null){
			for (ActionOfFlow actionOfFlow : selectedFlowTemplate.getActionOfFlows()) {
				for (ActionDetail actionDetail : actionOfFlow.getAction().getActionDetails()) {
					if(!nodeTypes.contains(actionDetail.getNodeType())){
						nodeTypes.add(actionDetail.getNodeType());
						if(actionDetail.getNodeType().getTypeId()==6){ //AGG
							nodeActionOffs.add(new NodeActionOff(actionDetail.getNodeType(),1L,selectedFlowTemplate));
							nodeActionOffs.add(new NodeActionOff(actionDetail.getNodeType(),0L,selectedFlowTemplate));
						}else{
							nodeActionOffs.add(new NodeActionOff(actionDetail.getNodeType(),2L,selectedFlowTemplate));
						}
					}
				}
			}
		}
	}
	
	public void saveNodeLayer(){
		Session _session = null;
		Transaction _tx = null;
		Date startTime = new Date();
		try {
			List<NodeActionOff> actionOffs = new ArrayList<>();
			for (List<NodeActionOff> list : nodeLayers) {
				for (NodeActionOff nodeActionOff : list) {
					if(!nodeActionOff.getDeclare())
						actionOffs.add(nodeActionOff);
				}
			}
			Long flowTemplatesId = buildTemplateFlowController.getSelectedFlowTemplate().getFlowTemplatesId();
			Object[] trs = new NodeActionOffImpl().openTransaction();
			_session = (Session) trs[0];
			_tx = (Transaction) trs[1];
			new NodeActionOffImpl().execteBulk2("delete NodeActionOff where flowTemplates.flowTemplatesId = ? ", _session, _tx, false,
						flowTemplatesId);
			new NodeActionOffImpl().saveOrUpdate(actionOffs, _session, _tx,true);
			//20180620_tudn_start ghi log DB
			try {
				LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
						LogUtils.getRemoteIpClient(), LogUtils.getUrl(), NodeLayerController.class.getName(),
						Thread.currentThread().getStackTrace()[1].getMethodName(),
						LogUtils.ActionType.UPDATE,
						actionOffs.toString(), LogUtils.getRequestSessionId());
			} catch (Exception e) {
				LOGGER.error(e.getMessage(), e);
			}
			//20180620_tudn_end ghi log DB
			MessageUtil.setInfoMessageFromRes("info.save.success");
		} catch (Exception e) {
			if(_tx!=null && _tx.getStatus()!= TransactionStatus.ROLLED_BACK)
				_tx.rollback();
			LOGGER.error(e.getMessage(), e);
			MessageUtil.setErrorMessageFromRes("error.save.unsuccess");
		}finally{
			if(_session!=null && _session.isOpen()){
				_session.close();
			}
		}
	}


	public List<NodeActionOff> getNodeActionOffs() {
		return nodeActionOffs;
	}

	public void setNodeActionOffs(List<NodeActionOff> nodeActionOffs) {
		this.nodeActionOffs = nodeActionOffs;
	}

	public BuildTemplateFlowController getBuildTemplateFlowController() {
		return buildTemplateFlowController;
	}

	public void setBuildTemplateFlowController(BuildTemplateFlowController buildTemplateFlowController) {
		this.buildTemplateFlowController = buildTemplateFlowController;
	}

	public ActionDbServerController getActionController() {
		return actionController;
	}

	public void setActionController(ActionDbServerController actionController) {
		this.actionController = actionController;
	}

	public List<List<NodeActionOff>> getNodeLayers() {
		return nodeLayers;
	}

	public void setNodeLayers(List<List<NodeActionOff>> nodeLayers) {
		this.nodeLayers = nodeLayers;
	}
	
	
	
}
