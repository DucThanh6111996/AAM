package com.viettel.it.controller;

import com.viettel.bean.ResultGetAccount;
import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.ItActionAccount;
import com.viettel.it.model.MapUserCountryBO;
import com.viettel.it.model.Node;
import com.viettel.it.model.NodeAccount;
import com.viettel.it.persistence.ItActionAccountServicesImpl;
import com.viettel.it.persistence.MapUserCountryServiceImpl;
import com.viettel.it.persistence.NodeAccountServiceImpl;
import com.viettel.it.persistence.NodeServiceImpl;
import com.viettel.it.util.*;
import com.viettel.passprotector.PassProtector;
import com.viettel.util.Constant;
import com.viettel.it.util.SecurityService;
import com.viettel.util.SessionUtil;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;

/**
 * Created by taitd on 6/13/2017.
 */
@ViewScoped
@ManagedBean
public class ItNodeAccountController {
    protected static final Logger logger = LoggerFactory.getLogger(ItNodeAccountController.class);

    @ManagedProperty(value = "#{nodeAccountService}")
    private NodeAccountServiceImpl itNodeAccountService;

    private LazyDataModel<NodeAccount> lazyNode;
    private NodeAccount selectedNode;
    private Node selectedServer;
    private boolean isEdit;
    private Map<String, Long> accountType;
    Map<String, Long> nodeName;
    List<Node> nodes;
    private long selectionVendor;
    private Node selectedServerNode;
    /*20190116_hoangnd_them thi truong_start*/
    private String passwordBeforeChange;
    /*20190116_hoangnd_them thi truong_end*/

    @PostConstruct
    public void onStart() {
        try {
            selectedNode = new NodeAccount();
            accountType = new HashMap<>();
            accountType.put("Account Database", 1l);
            accountType.put("Account Server", 2l);
            accountType.put("Account WebService", 3l);
            accountType.put("Account Provisioning", 4l);
            LinkedHashMap orders = new LinkedHashMap();
            orders.put("id", "DESC");

            Map<String, Object> filters = new HashedMap();
            filters.put("itBusinessNode", 1l);

            /*20181225_hoangnd_them thi truong_start*/
            List<String> lstCountry = new MapUserCountryServiceImpl().getListCountryForUser();
            if(CollectionUtils.isNotEmpty(lstCountry))
                filters.put("countryCode.countryCode-EXAC", lstCountry);
            /*20181225_hoangnd_them thi truong_end*/

            lazyNode = new LazyDataModelBaseNew<NodeAccount, Long>(itNodeAccountService, filters, orders);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public List<Node> autoServerMethod(String nodeCode) {
        if (nodeCode != null) {
            Map<String, Object> filters = new HashedMap();
            filters.put("nodeCode", nodeCode);
            filters.put("active", Constant.status.active);
			if(selectedNode != null && selectedNode.getAccountType() != null) {
                if (selectedNode.getAccountType().equals(5L)) {
                    filters.put("vendor.vendorId", 1l);
                } else {
                    filters.put("vendor.vendorId", selectedNode.getAccountType());
                }
            }
            List<String> lstCountry = new MapUserCountryServiceImpl().getListCountryForUser();
            if(CollectionUtils.isNotEmpty(lstCountry))
                filters.put("countryCode.countryCode-EXAC", lstCountry);
            try {
                nodes = new NodeServiceImpl().findList(filters);
                if (nodes == null) {
                    return new ArrayList<>();
                } else {
                    selectedServer = null;
                    return nodes;

                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return new ArrayList<>();
    }

    public List<Node> filterServerByVendor() {
        Map<String, Object> filters = new HashedMap();
        filters.put("active", Constant.status.active);
        if(this.selectedNode.getAccountType().longValue() == 5L) {
            filters.put("vendor.vendorId", Long.valueOf(1L));
        } else {
            filters.put("vendor.vendorId", this.selectedNode.getAccountType());
        }
        try {
            nodes = new NodeServiceImpl().findList(filters);
            if (nodes == null) {
                return new ArrayList<>();
            } else {
                selectedServer = null;
                return nodes;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return new ArrayList<>();
    }


    public void saveNode() {

        try {
            Date startTime = new Date();
            if (validateData()) {

                NodeAccount nodeSave = new NodeAccount();
                if (isEdit) {
                    nodeSave.setId(selectedNode.getId());
                }
                nodeSave.setUsername(selectedNode.getUsername());
                /*20190116_hoangnd_them thi truong_start*/
                nodeSave.setPassword((passwordBeforeChange != null && passwordBeforeChange.equals(selectedNode.getPassword())) ? selectedNode.getPassword() : PassProtector.encrypt(selectedNode.getPassword(), Config.SALT));
                /*20190116_hoangnd_them thi truong_end*/
                nodeSave.setShell(selectedNode.getShell());
                nodeSave.setAccountType(selectedNode.getAccountType());
                nodeSave.setServerId(selectedServer.getServerId() == null ? selectedServer.getNodeId() : selectedServer.getServerId());
                nodeSave.setImpactOrMonitor(selectedNode.getImpactOrMonitor());
                nodeSave.setItBusinessNode(1l);

                /*20181210_hoangnd_fix bug insert active = null_start*/
                nodeSave.setActive(selectedNode.getActive() != null ? selectedNode.getActive() : Constant.status.active);
                /*20181210_hoangnd_fix bug insert active = null_end*/
				/*20190115_hoangnd_them thi truong_start*/
                nodeSave.setCountryCode(selectedServer.getCountryCode());
                /*20190115_hoangnd_them thi truong_end*/

                new NodeAccountServiceImpl().saveOrUpdate(nodeSave);

                /*
                Ghi log tac dong nguoi dung
                */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItNodeAccountController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            (isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
                            nodeSave.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                LinkedHashMap orders = new LinkedHashMap();
                orders.put("id", "DESC");

                Map<String, Object> filters = new HashedMap();
                filters.put("itBusinessNode", 1l);

                /*20181225_hoangnd_them thi truong_start*/
                List<String> lstCountry = new MapUserCountryServiceImpl().getListCountryForUser();
                if(CollectionUtils.isNotEmpty(lstCountry))
                    filters.put("countryCode.countryCode-EXAC", lstCountry);
                /*20181225_hoangnd_them thi truong_end*/

                lazyNode = new LazyDataModelBaseNew<NodeAccount, Long>(itNodeAccountService, filters, orders);
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
        if (selectedNode.getPassword() == null
                || selectedServer == null
                || selectedNode.getImpactOrMonitor() == null) {
            MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
            val = false;
        }

        return val;
    }

    public String displayServerInfo(NodeAccount node) {
        if (node != null) {
            Node server = null;
            Map<String, Object> filters = new HashedMap();
            try {

                filters.put("serverId", node.getServerId());
                filters.put("active", Constant.status.active);
                if (node.getAccountType().equals(5L)) {
                    filters.put("vendor.vendorId", Constant.ACCOUNT_TYPE_SERVER);
                } else {
                    filters.put("vendor.vendorId", node.getAccountType());
                }
                List<Node> nodes = new NodeServiceImpl().findList(filters);
                if (nodes != null && !nodes.isEmpty()) {
                    server = nodes.get(0);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            if (server == null) {
                try {
                    filters.clear();
                    filters.put("nodeId", node.getServerId());
                    filters.put("active", Constant.status.active);
                    if (node.getAccountType().equals(5L)) {
                        filters.put("vendor.vendorId", Constant.ACCOUNT_TYPE_SERVER);
                    } else {
                        filters.put("vendor.vendorId", node.getAccountType());
                    }
                    List<Node> nodes = new NodeServiceImpl().findList(filters);
                    if (nodes != null && !nodes.isEmpty()) {
                        server = nodes.get(0);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            if (server != null) {
//                logger.info(">>>> " + (server.getEffectIp() + " -- " + server.getNodeCode()));
                return (server.getEffectIp() + " -- " + server.getNodeCode());
            }
        }
        return "";
    }


    public void prepareEdit(NodeAccount node) throws GeneralSecurityException, IOException {
        try {
            /*20181210_hoangnd_fix bug khi update thanh insert moi_start*/
            isEdit = true;
            /*20181210_hoangnd_fix bug khi update thanh insert moi_end*/
            selectedNode = node;
            /*20190116_hoangnd_them thi truong_start*/
            passwordBeforeChange = selectedNode.getPassword().replaceAll("(\\r\\n|\\r|\\n|\\n\\r)", "");
            /*20190116_hoangnd_them thi truong_end*/
            try {
                //20181023_tudn_start load pass security
//                selectedNode.setPassword(PassProtector.decrypt(node.getPassword(), Config.SALT));
                /*if(node.getServerId()!=null) {
                    Map<String, Object> filters = new HashedMap();
                    Node nodeIp = new Node() ;
                    try {
                        filters.put("serverId", node.getServerId());
                        List<Node> nodes = new NodeServiceImpl().findList(filters);
                        if (nodes != null && !nodes.isEmpty()) {
                            nodeIp = nodes.get(0);
                        }
                        String accType = null;
                        if(node !=null && node.getAccountType()!=null  ) {
                            if (Constant.ACCOUNT_TYPE_SERVER.equalsIgnoreCase(node.getAccountType().toString())) {
                                accType = Constant.SECURITY_SERVER;
                            } else if (Constant.ACCOUNT_TYPE_DATABASE.equalsIgnoreCase(node.getAccountType().toString())) {
                                accType = Constant.SECURITY_DATABASE;
                            }
                        }
                        String passBackup = "";
                        try {
                            passBackup = PassProtector.decrypt(node.getPassword(), Config.SALT);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                            passBackup = node.getPassword();
                        }
                        Map<String, String> mapConfigSecurity = SecurityService.getConfigSecurity();
                        ResultGetAccount resultGetAccount = SecurityService.getPassSecurity(nodeIp.getNodeIp(),SessionUtil.getCurrentUsername()
                                ,node.getUsername(),accType,null,null,nodeIp.getCountryCode().getCountryCode()
                                ,LogUtils.getRequestSessionId(),passBackup,mapConfigSecurity);
                        if(!resultGetAccount.getResultStatus() && SecurityService.isNullOrEmpty(resultGetAccount.getResult())){
                            MessageUtil.setErrorMessage(resultGetAccount.getResultMessage());
                            return;
                        }
                        selectedNode.setPassword(resultGetAccount.getResult());

                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }else{*/
                    selectedNode.setPassword(PassProtector.decrypt(node.getPassword(), Config.SALT));
                //}
                //20181023_tudn_end load pass security
            } catch (Exception e) {
                try {
                    selectedNode.setPassword(PasswordEncoder.decrypt(node.getPassword()));
                }catch(Exception ex){
                    logger.error(e.getMessage(), e);
                }
                logger.error(e.getMessage(), e);
            }

            Map<String, Object> filters = new HashedMap();
            try {
                filters.put("active", Constant.status.active);
                filters.put("serverId", selectedNode.getServerId());
                if(selectedNode != null && selectedNode.getAccountType() != null) {
                    if (selectedNode.getAccountType().equals(5L)) {
                        filters.put("vendor.vendorId", 1l);
                    } else {
                        filters.put("vendor.vendorId", selectedNode.getAccountType());
                    }
                }
                selectedServer = new NodeServiceImpl().findList(filters).get(0);
            } catch (Exception e) {
                selectedServer = null;
                logger.error(e.getMessage(), e);
            }
            if (selectedServer == null) {
                try {
                    filters.clear();
                    filters.put("nodeId", selectedNode.getServerId());
                    filters.put("active", Constant.status.active);
                    if(selectedNode != null && selectedNode.getAccountType() != null) {
                        if (selectedNode.getAccountType().equals(5L)) {
                            filters.put("vendor.vendorId", 1l);
                        } else {
                            filters.put("vendor.vendorId", selectedNode.getAccountType());
                        }
                    }
                    nodes = new NodeServiceImpl().findList(filters);
                    if (nodes != null && !nodes.isEmpty()) {
                        selectedServer = nodes.get(0);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }


       /* if(selectedNode.getIsLab() != null && selectedNode.getIsLab() == 1){
            selectedNode.setNodeLab(true);
        } else if(selectedNode.getIsLab() != null && selectedNode.getIsLab() == 0){
            selectedNode.setNodeLab(false);
        }*/
        /*20181210_hoangnd_fix bug khi update thanh insert moi_start*/
//            isEdit = true;
        /*20181210_hoangnd_fix bug khi update thanh insert moi_end*/
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public List<Node> completeSearchNode(String nodeIp) {
        if (nodeIp != null) {
            Map<String, Object> filters = new HashedMap();
            filters.put("nodeIp", nodeIp);
            filters.put("active", Constant.status.active);
            List<Node> nodes = new ArrayList<>();
            try {
                nodes = new NodeServiceImpl().findList(filters);
                if (nodes == null) {
                    nodes = new ArrayList<>();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            return nodes;

        } else {
            return new ArrayList<>();
        }
    }

    public void handleSelectNode() {
        try {
            /*20190123_hoangnd_fix bug merge insert ko ma hoa pass_start*/
            passwordBeforeChange = null;
            /*20190123_hoangnd_fix bug merge insert ko ma hoa pass_end*/
            Map<String, Object> filters = new HashedMap();
            filters.put("itBusinessNode", 1l);

            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            orders.put("serverId", "ASC");
            orders.put("username", "ASC");

            if (selectedServerNode != null) {

                if (selectedServerNode.getServerId() != null) {
                    filters.put("serverId", selectedServerNode.getServerId());
                } else {
                    filters.put("serverId", selectedServerNode.getNodeId());
                }

            }
            lazyNode = new LazyDataModelBaseNew<NodeAccount, Long>(itNodeAccountService, filters, orders);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    public void clear() {
        selectedNode = new NodeAccount();
        selectionVendor = 1;
        isEdit = false;
        selectedServer = new Node();
        nodes = null;
    }

    public void prepareDel(NodeAccount node) {
        if (node != null) {
            selectedNode = node;
        }
    }

    public void delNode() {
        if (selectedNode != null) {
            try {
                List<ItActionAccount> itActionAccount = new ItActionAccountServicesImpl().findListAll("from ItActionAccount where nodeAccount = ?", selectedNode);
                if (!itActionAccount.isEmpty()) {
                    MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
                    return;
                }
                Date startTime = new Date();
                new NodeAccountServiceImpl().delete(selectedNode);

                /*
			    Ghi log tac dong nguoi dung
                */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItNodeAccountController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.DELETE, selectedNode.toString(),
                            LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                selectedNode = new NodeAccount();
                MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
            } catch (Exception e) {
                MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
                logger.error(e.getMessage(), e);
            }
        }
        LinkedHashMap orders = new LinkedHashMap();
        orders.put("id", "DESC");
        Map<String, Object> filters = new HashedMap();
//        filters.put("itBusinessNode", 1l);
        lazyNode = new LazyDataModelBaseNew<NodeAccount, Long>(itNodeAccountService, filters, orders);
    }


    public NodeAccountServiceImpl getItNodeAccountService() {
        return itNodeAccountService;
    }

    public void setItNodeAccountService(NodeAccountServiceImpl itNodeAccountService) {
        this.itNodeAccountService = itNodeAccountService;
    }

    public LazyDataModel<NodeAccount> getLazyNode() {
        return lazyNode;
    }

    public void setLazyNode(LazyDataModel<NodeAccount> lazyNode) {
        this.lazyNode = lazyNode;
    }

    public NodeAccount getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(NodeAccount selectedNode) {
        this.selectedNode = selectedNode;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean edit) {
        isEdit = edit;
    }

    public Node getSelectedServer() {
        return selectedServer;
    }

    public void setSelectedServer(Node selectedServer) {
        this.selectedServer = selectedServer;
    }

    public Map<String, Long> getAccountType() {
        return accountType;
    }

    public void setAccountType(Map<String, Long> accountType) {
        this.accountType = accountType;
    }

    public Map<String, Long> getNodeName() {
        return nodeName;
    }

    public void setNodeName(Map<String, Long> nodeName) {
        this.nodeName = nodeName;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public long getSelectionVendor() {
        return selectionVendor;
    }

    public void setSelectionVendor(long selectionVendor) {
        this.selectionVendor = selectionVendor;
    }

    public Node getSelectedServerNode() {
        return selectedServerNode;
    }

    public void setSelectedServerNode(Node selectedServerNode) {
        this.selectedServerNode = selectedServerNode;
    }

}
