/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.controller;

import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.Node;
import com.viettel.it.model.NodeType;
import com.viettel.it.model.Vendor;
import com.viettel.it.model.Version;
import com.viettel.it.persistence.*;
import com.viettel.it.persistence.common.CatCountryServiceImpl;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.model.CatCountryBO;
import com.viettel.controller.AamConstants;
import com.viettel.util.Constant;
import com.viettel.util.SessionUtil;
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
 *
 * @author hienhv4
 */
@ViewScoped
@ManagedBean
public class NodeController {

    protected static final Logger logger = LoggerFactory.getLogger(NodeController.class);

    @ManagedProperty(value = "#{nodeService}")
    private NodeServiceImpl nodeService;
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

    //20180615_thenv_country_Start
    @ManagedProperty(value = "#{catCountryService}")
    private CatCountryServiceImpl catCountryService;
    @ManagedProperty(value = "#{mapUserCountryService}")
    private MapUserCountryServiceImpl mapUserCountryService;
    private List<CatCountryBO> countrys;
    //20180615_thenv_country_End

    @PostConstruct
    public void onStart() {
        try {
            Map<String, String> orderVendor = new HashMap<>();
            orderVendor.put("vendorName", "asc");
            vendors = vendorService.findList(null, orderVendor);

            Map<String, String> orderNodeType = new HashMap<>();
            orderNodeType.put("typeName", "asc");
            nodeTypes = nodeTypeService.findList(null, orderNodeType);

            Map<String, String> orderVersion = new HashMap<>();
            orderVersion.put("versionName", "asc");
            versions = versionService.findList(null, orderVersion);

            //20180615_thenv_country_start
            List<String> lstCountry = mapUserCountryService.getListCountryForUser();
            Map<String, Object> filterCountry = new HashMap<>();
            if (lstCountry != null && lstCountry.size() > 0) {
                filterCountry.put("countryCode-EXAC", lstCountry);
                countrys = catCountryService.findList(filterCountry);
            }
            //20180615_thenv_country_End

            /*20180713_hoangnd_filter theo country code_start*/
            Map<String, Object> filters = new HashMap<>();
            filters.put("active", Constant.status.active);
            if (lstCountry != null && lstCountry.size() > 0) {
                filters.put("countryCode.countryCode-EXAC", lstCountry);
            }
            lazyNode = new LazyDataModelBaseNew<>(nodeService, filters, null);
            /*20180713_hoangnd_filter theo country code_end*/
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void prepareEdit(Node node) {
        selectedNode = node;
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
                nodeService.delete(selectedNode);

                /*
			    Ghi log tac dong nguoi dung
                */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), NodeController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.DELETE, selectedNode.toString(),
                            LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                selectedNode = new Node();
                MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
            } catch (Exception e) {
                MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void saveNode() {
        try {
            Date startTime = new Date();
            if (validateData()) {

                Node nodeSave = new Node();
                if (isEdit) {
                    nodeSave.setNodeId(selectedNode.getNodeId());
                }
                // thenv_20180615_dang bi loi_start
//                else {
//
//                }
//                nodeSave.setIsLab(selectedNode.isNodeLab() ? 1 : 0);
                // thenv_20180615_dang bi loi_end
                nodeSave.setNodeCode(selectedNode.getNodeCode());
                nodeSave.setNodeIp(selectedNode.getNodeIp());
                nodeSave.setVendor(selectedNode.getVendor());
                nodeSave.setVersion(selectedNode.getVersion());
                nodeSave.setNodeType(selectedNode.getNodeType());
                nodeSave.setPort(selectedNode.getPort());
                nodeSave.setOsType(selectedNode.getOsType() == null ? null : selectedNode.getOsType().trim());
                nodeSave.setJdbcUrl(selectedNode.getJdbcUrl() == null ? null : selectedNode.getJdbcUrl().trim());
                nodeSave.setSubnetwork(selectedNode.getSubnetwork() == null ? null : selectedNode.getSubnetwork().trim());
                nodeSave.setNetworkType(selectedNode.getNetworkType() == null ? null : selectedNode.getNetworkType().trim());
                nodeSave.setIsLab(selectedNode.getIsLab());
                nodeSave.setEffectIp(selectedNode.getEffectIp() == null ? null : selectedNode.getEffectIp().trim());

                if (nodeSave.getEffectIp() == null || nodeSave.getEffectIp().trim().isEmpty()) {
                    nodeSave.setEffectIp(selectedNode.getNodeIp());
                }

                //20180615_thenv_add default_start
                if(selectedNode.getCountryCode() == null){
                    CatCountryBO catCountryBO = new CatCountryBO();
                    catCountryBO.setCountryCode(AamConstants.VNM);
                    selectedNode.setCountryCode(catCountryBO);
                }
                nodeSave.setCountryCode(selectedNode.getCountryCode());
                //20180615_thenv_add default_end

                /*20181210_hoangnd_fix bug node active null_start*/
                nodeSave.setActive(selectedNode.getActive() != null ? selectedNode.getActive() : Constant.status.active);
                /*20181210_hoangnd_fix bug node active null_end*/

                nodeService.saveOrUpdate(nodeSave);

                /*
			    Ghi log tac dong nguoi dung
                */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), NodeController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            (isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
                            nodeSave.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                clear();

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
                || selectedNode.getVersion() == null
                || selectedNode.getNodeType() == null
                || selectedNode.getNodeIp() == null
                || selectedNode.getNodeIp().trim().isEmpty()) {
            MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
            val = false;
        }

        return val;
    }

    public List<Node> autoCompleNode(String nodeCode) {
        List<Node> lstNode;
        Map<String, Object> filters = new HashMap<>();
        filters.put("active", Constant.status.active);
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

    public void clear() {
        selectedNode = new Node();
        isEdit = false;
    }

    public boolean isChecked() {
        if (selectedNode != null) {
            return selectedNode.getIsLab() != null && selectedNode.getIsLab() > 0;
        }
        return false;
    }

    public void setChecked(boolean checked) {
        if (selectedNode != null) {
            selectedNode.setIsLab(checked ? 1 : 0);
        }
    }

    public NodeServiceImpl getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeServiceImpl nodeService) {
        this.nodeService = nodeService;
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

    // thenv_20180615_country_start
    public CatCountryServiceImpl getCatCountryService() {
        return catCountryService;
    }
    public void setCatCountryService(CatCountryServiceImpl catCountryService) {
        this.catCountryService = catCountryService;
    }
    public MapUserCountryServiceImpl getMapUserCountryService() {
        return mapUserCountryService;
    }
    public void setMapUserCountryService(MapUserCountryServiceImpl mapUserCountryService) {
        this.mapUserCountryService = mapUserCountryService;
    }
    public List<CatCountryBO> getCountrys() {
        return countrys;
    }
    public void setCountrys(List<CatCountryBO> countrys) {
        this.countrys = countrys;
    }
    // thenv_20180615_country_end
}
