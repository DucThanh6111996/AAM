package com.viettel.it.controller;

import com.viettel.controller.AppException;
import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.*;
import com.viettel.it.persistence.*;
import com.viettel.it.util.Config;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.it.util.PasswordEncoder;
import com.viettel.util.Constant;
import com.viettel.util.SessionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.util.*;

/**
 * Created by taitd on 5/25/2017.
 */
@ViewScoped
@ManagedBean
public class ItNodeController {

    protected static final Logger logger = LoggerFactory.getLogger(NodeController.class);

    @ManagedProperty(value = "#{nodeService}")
    private NodeServiceImpl itNodeService;
    @ManagedProperty(value = "#{vendorService}")
    private VendorServiceImpl vendorService;
    @ManagedProperty(value = "#{nodeTypeService}")
    private NodeTypeServiceImpl nodeTypeService;
    @ManagedProperty(value = "#{versionService}")
    private VersionServiceImpl versionService;

    private LazyDataModel<Node> lazyNode;
    private Node selectedNode;
    private boolean isEdit;
    private StreamedContent file;

    private List<Vendor> vendors;
    private List<NodeType> nodeTypes;
    private List<Version> versions;

    @PostConstruct
    public void onStart() {
        try {
            Map<String, String> orderNodeType = new HashMap<>();
            orderNodeType.put("typeName", "asc");
            nodeTypes = nodeTypeService.findList(null, orderNodeType);

            getVendor();

            Map<String, String> orderVersion = new HashMap<>();
            orderVersion.put("versionName", "asc");
            versions = versionService.findList(null, orderVersion);

            /*20181224_hoangnd_them filter thi truong theo username_start*/
            Map<String, List<String>> filters = new HashedMap();
            List<String> lstCountry = new MapUserCountryServiceImpl().getListCountryForUser();
            if(CollectionUtils.isNotEmpty(lstCountry)){
                filters.put("countryCode.countryCode-EXAC", lstCountry);
			}
			List<String> statuss  = new ArrayList<>();
            statuss.add(Constant.status.active.toString());
			filters.put("active", statuss);
            lazyNode = new LazyDataModelBaseNew<Node,Long>(itNodeService,filters,null);
            /*20181224_hoangnd_them filter thi truong theo username_end*/
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void clear() {
        selectedNode = new Node();
        isEdit = false;
    }

    public void getVendor() throws AppException {
        Map<String, String> orderVendor = new HashMap<>();
        orderVendor.put("vendorName", "asc");
        List<Vendor> lstVendor = new VendorServiceImpl().findList(null,orderVendor);
        vendors = new ArrayList<>();
        for(int  i = 0 ; i< lstVendor.size() ; i++ ){
//            if(lstVendor.get(i).getVendorId() == 3l || lstVendor.get(i).getVendorId() == 4l){
                vendors.add(lstVendor.get(i));
//            }
        }
    }

    public void saveNode() throws AppException {
        Version defaultVersion = new VersionServiceImpl().findById(Config.VERSION_DEFAULT_ID);
        NodeType defaultNodeType = new NodeTypeServiceImpl().findById(Config.NODE_DEFAULT_ID);

        try {
            Date startTime = new Date();
            if (validateData()) {

                Node nodeSave = new Node();
                nodeSave.setActive(Constant.status.active);
                if (isEdit) {
                    nodeSave.setNodeId(selectedNode.getNodeId());
                }
//                nodeSave.setIsLab(selectedNode.isNodeLab() ? 1 : 0);
                nodeSave.setNodeCode(selectedNode.getNodeCode());
                nodeSave.setNodeIp(selectedNode.getNodeIp());
                nodeSave.setEffectIp(selectedNode.getNodeIp());
                nodeSave.setVendor(selectedNode.getVendor());
                nodeSave.setVersion(defaultVersion);
                nodeSave.setNodeType(defaultNodeType);
                nodeSave.setPort(selectedNode.getPort());
                nodeSave.setOsType(selectedNode.getOsType() == null ? null : selectedNode.getOsType().trim());
                nodeSave.setJdbcUrl(selectedNode.getJdbcUrl() == null ? null : selectedNode.getJdbcUrl().trim());
//                nodeSave.setSubnetwork(selectedNode.getSubnetwork() == null ? null : selectedNode.getSubnetwork().trim());
//                nodeSave.setNetworkType(selectedNode.getNetworkType() == null ? null : selectedNode.getNetworkType().trim());
                nodeSave.setItBusinessNode(1l);
                //nodeSave.setEffectIp(selectedNode.getEffectIp() == null ? null : selectedNode.getEffectIp().trim());
               /* nodeSave.setUserManager(selectedNode.getUserManager());
                nodeSave.setPassword(PasswordEncoder.encrypt(selectedNode.getPassword()));*/
                /*20181224_hoangnd_them thi truong_start*/
                nodeSave.setCountryCode(selectedNode.getCountryCode());
                /*20181224_hoangnd_them thi truong_end*/

                if (nodeSave.getEffectIp() == null || nodeSave.getEffectIp().trim().isEmpty()) {
                    nodeSave.setEffectIp(selectedNode.getNodeIp());
                }

                itNodeService.saveOrUpdate(nodeSave);

                /*
			    Ghi log tac dong nguoi dung
                */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItNodeController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            (isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
                            nodeSave.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                clear();

                Map<String, Object> filters = new HashedMap();
                filters.put("active", Constant.status.active);
                lazyNode = new LazyDataModelBaseNew<Node,Long>(itNodeService, filters,null);

                MessageUtil.setInfoMessageFromRes("label.action.updateOk");
                RequestContext.getCurrentInstance().execute("PF('dlgNodeInfo').hide()");
            }
        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
            logger.error(e.getMessage(), e);
        }
    }

    private boolean validateData() {
        boolean val = true;
        // Kiem tra xem cac truong da duoc nhap day du du lieu chua
        if (selectedNode.getNodeCode() == null
                || selectedNode.getNodeCode().trim().isEmpty()
                || selectedNode.getVendor() == null
                || selectedNode.getNodeIp() == null
                || selectedNode.getNodeIp().trim().isEmpty()
                || selectedNode.getCountryCode() == null) {
            MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
            val = false;
        }

        return val;
    }

    public void prepareEdit(Node node) {
        selectedNode = node;
        if(selectedNode.getIsLab() != null && selectedNode.getIsLab() == 1){
            selectedNode.setNodeLab(true);
        } else if(selectedNode.getIsLab() != null && selectedNode.getIsLab() == 0){
            selectedNode.setNodeLab(false);
        }
        isEdit = true;
    }

    public void prepareDel(Node node) {
        if (node != null) {
            selectedNode = node;
        }
    }

    public void delNode() {
        if (selectedNode != null) {
            try {
                Date startTime = new Date();
                itNodeService.delete(selectedNode);

                /*
			    Ghi log tac dong nguoi dung
                */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItNodeController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.DELETE, selectedNode.toString(),
                            LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                selectedNode = new Node();

                Map<String, Object> filters = new HashedMap();
                filters.put("itBusinessNode", 1l);
                lazyNode = new LazyDataModelBaseNew<Node,Long>(itNodeService,filters,null);

                MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
            } catch (Exception e) {
                MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
                logger.error(e.getMessage(), e);
            }
        }
    }

    public NodeServiceImpl getItNodeService() {
        return itNodeService;
    }

    public void setItNodeService(NodeServiceImpl itNodeService) {
        this.itNodeService = itNodeService;
    }

    public VendorServiceImpl getVendorService() {
        return vendorService;
    }

    public void setVendorService(VendorServiceImpl vendorService) {
        this.vendorService = vendorService;
    }

    public NodeTypeServiceImpl getNodeTypeService() {
        return nodeTypeService;
    }

    public void setNodeTypeService(NodeTypeServiceImpl nodeTypeService) {
        this.nodeTypeService = nodeTypeService;
    }

    public VersionServiceImpl getVersionService() {
        return versionService;
    }

    public void setVersionService(VersionServiceImpl versionService) {
        this.versionService = versionService;
    }

    public LazyDataModel<Node> getLazyNode() {
        return lazyNode;
    }

    public void setLazyNode(LazyDataModel<Node> lazyNode) {
        this.lazyNode = lazyNode;
    }

    public Node getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(Node selectedNode) {
        this.selectedNode = selectedNode;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
    }

    public StreamedContent getFile() {
        return file;
    }

    public void setFile(StreamedContent file) {
        this.file = file;
    }

    public List<Vendor> getVendors() {
        return vendors;
    }

    public void setVendors(List<Vendor> vendors) {
        this.vendors = vendors;
    }

    public List<NodeType> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(List<NodeType> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }
}
