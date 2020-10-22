/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.controller;

import com.viettel.bean.ResultGetAccount;
import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.*;
import com.viettel.it.model.ParamChecklistDatabase;
import com.viettel.it.object.TbsNodeObj;
import com.viettel.it.persistence.*;
import com.viettel.it.persistence.common.CatCountryServiceImpl;
import com.viettel.it.thread.*;
import com.viettel.it.util.*;
import com.viettel.it.util.Config.APP_TYPE;
import com.viettel.model.CatCountryBO;
import com.viettel.passprotector.PassProtector;
import com.viettel.controller.AamConstants;
import com.viettel.util.Constant;
import com.viettel.util.SessionUtil;
import com.viettel.util.SessionWrapper;
import org.apache.commons.collections.map.HashedMap;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.model.SelectItem;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * @author hienhv4
 */
@ViewScoped
@ManagedBean
public class MopDatabaseNewController {

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
    protected static final Logger logger = LoggerFactory.getLogger(MopDatabaseNewController.class);

    @ManagedProperty(value = "#{nodeService}")
    private NodeServiceImpl nodeService;
    @ManagedProperty(value = "#{vendorService}")
    private VendorServiceImpl vendorService;
    @ManagedProperty(value = "#{nodeTypeService}")
    private NodeTypeServiceImpl nodeTypeService;
    @ManagedProperty(value = "#{versionService}")
    private VersionServiceImpl versionService;
    @ManagedProperty(value = "#{vDatabaseInfosService}")
    private VDatabaseInfosServiceImpl vDatabaseInfosService;

    private LazyDataModel<VDatabaseInfos> lazyNodeDatabase;
    private LazyDataModel<Node> lazyNode;
    private StreamedContent file;

    private List<FlowTemplates> flowTemplates = new ArrayList<>();
    private FlowTemplates selectedFlowTemplate;
    private List<VDatabaseInfos> selectedDbNodes = new ArrayList<>();
    private Integer cycleMonth;
    private String flowRunActionName;
    private List<SelectItem> checklistParams = new ArrayList<>();

    private String[] checklistParamSelected;
    private boolean isEnableTemplate;

    // table space
    private String tbsDatafileSize;
    private Integer tbsAutoExtend = 1;
    private String tbsInitSize;
    private String tbsMaxSize;

    // datafile
    private String datafileDatafileSize;
    private Integer datafileAutoExtend = 1;
    private String datafileInitSize;
    private String datafileMaxSize;
    private String datafileTablespaceNames;
    private String datafileNumbers;

    @PostConstruct
    public void onStart() {
        try {
            isEnableTemplate = false;
            Map<String, Object> filters = new HashMap<>();
            filters.put("vendor.vendorId", APP_TYPE.DATABASE.value);
            lazyNode = new LazyDataModelBaseNew<>(nodeService, filters, null);

			/*20181210_hoangnd_them filter theo active_start*/
            filters = new HashMap<>();
            filters.put("active", Constant.status.active);
            lazyNodeDatabase = new LazyDataModelBaseNew<>(vDatabaseInfosService, filters, null);
            /*20181210_hoangnd_them filter theo active_end*/
            buildFlowTemplatesDB();

            checklistParams = new Util().getChecklistName();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void buildFlowTemplatesDB() {
        try {
            flowTemplates = new ArrayList<>();
            Map<String, Object> filters = new HashMap<>();
            filters.put("flowTemplatesId", 10082l);
//    		flowTemplates = new FlowTemplatesServiceImpl().findList(filters);

            flowTemplates = new FlowTemplatesServiceImpl().findList();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void onChangeFlowTemplate() {
        if (selectedDbNodes != null && !selectedDbNodes.isEmpty() && selectedFlowTemplate != null) {

            // gan ten cua flow run action
            flowRunActionName = SessionWrapper.getCurrentUsername() + "_"
                    + selectedFlowTemplate.getFlowTemplateName() + "_" + df.format(new Date());

            if (selectedFlowTemplate.getFlowTemplatesId().equals(Config.FLOW_TEMPLATE_EXPORT_DB)) {
                for (VDatabaseInfos vDatabase : selectedDbNodes) {
                    vDatabase.setLstNodeIpServer(getNodesIpServer(vDatabase));
                }
            } else if (selectedFlowTemplate.getFlowTemplatesId().equals(Config.FLOW_TEMPLATE_ADD_DATAFILE_DB)) {
                getTbsName();
            }

            RequestContext.getCurrentInstance().update(":form-create-mop:dlgConfirmCreateMop");
            RequestContext.getCurrentInstance().execute("PF('dlgConfirmCreateMop').show()");
        } else {
            MessageUtil.setErrorMessageFromRes("label.err.node.db.select");
        }
    }

    private void getTbsName() {
        if (selectedDbNodes != null) {
            Connection conn = null;
            PreparedStatement prepare = null;
            List<String> lstTbsName = new ArrayList<>();
            TbsNodeObj tbsNode = null;
            List<TbsNodeObj> lstTbsObj = new ArrayList<>();
            Map<String, String> mapConfigSecurity = new HashMap<>();
            mapConfigSecurity = SecurityService.getConfigSecurity();
            Map<String, ResultGetAccount> mapPassGet = new HashMap<>();
            for (VDatabaseInfos vDatabase : selectedDbNodes) {
                try {
                    lstTbsName = new ArrayList<>();

                    // get account monitor
                    NodeAccount account = new Util().getAccount(null, Long.valueOf(vDatabase.getDbId()), Config.APP_TYPE.DATABASE.value,
                            true, 2l);
                    Map<String, Object> filters = new HashedMap();
                    Node databaseNode = new Node();
                    if (account != null) {
                        filters.put("serverId", Long.valueOf(vDatabase.getDbId()));
                        List<Node> nodes = new NodeServiceImpl().findList(filters);
                        if (nodes != null && !nodes.isEmpty()) {
                            databaseNode = nodes.get(0);
                        }

                        String passBackup = null;
                        try {
                            passBackup = PasswordEncoder.decrypt(account.getPassword());
                        } catch (Exception ex) {
                            try {
                                passBackup = PassProtector.decrypt(account.getPassword(), Config.SALT);
                            } catch (Exception e) {
                                logger.debug(e.getMessage(), e);
                            }
                            logger.debug(ex.getMessage(), ex);
                        }
                        if (Util.isNullOrEmpty(passBackup)) {
                            logger.info("Khong lay duoc pass back up");
                        }
                        String accType = null;
                        if (account.getAccountType() != null) {
                            if (Constant.ACCOUNT_TYPE_SERVER.equalsIgnoreCase(account.getAccountType().toString())) {
                                accType = Constant.SECURITY_SERVER;
                            } else if (Constant.ACCOUNT_TYPE_DATABASE.equalsIgnoreCase(account.getAccountType().toString())) {
                                accType = Constant.SECURITY_DATABASE;
                            }
                        }
                        ResultGetAccount resultGetAccount = SecurityService.getPassSecurity(databaseNode.getNodeIp(), "System"
                                , account.getUsername(), accType, account.getServerId().toString(), null, databaseNode.getCountryCode().getCountryCode()
                                , databaseNode.getNodeId().toString(), account.getPassword(), mapConfigSecurity, mapPassGet);
                        if (!resultGetAccount.getResultStatus()) {
                            conn = null;
                            logger.info("Khong lay duoc pass tu security dung chay");

                        } else {
                            account.setPassword(resultGetAccount.getResult());
                            conn = new Util().getConnection(vDatabase.getJdbcUrl(), account.getUsername(), account.getPassword());
                        }
                    }

                    if (conn != null) {
                        prepare = conn.prepareStatement(Config.SELECT_TBSNAME_FROM_NODE);
                        prepare.setFetchSize(1000);
                        ResultSet rs = prepare.executeQuery();
                        while (rs.next()) {
                            lstTbsName.add(rs.getString("TABLESPACE_NAME"));
                        }
                    }

                    vDatabase.setLstTbsName(lstTbsName);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                } finally {
                    if (prepare != null) {
                        try {
                            prepare.close();
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }

                    if (conn != null) {
                        try {
                            conn.close();
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }

                }
            } // end loop for
        }
    }

    private List<String> getNodesIpServer(VDatabaseInfos vDatabase) {
        List<String> lstNodeIpServer = new ArrayList<>();
        try {
            List<String> lstIpServer = Arrays.asList(vDatabase.getListIp().trim().split(","));
            if (!lstIpServer.isEmpty()) {
                Map<String, Object> filters = new HashMap<>();
                for (String ip : lstIpServer) {
                    filters.put("nodeIp", ip.trim());
                    filters.put("vendor.vendorId", APP_TYPE.DATABASE.value);
                    filters.put("active", Constant.status.active);
                    List<Node> nodesServer = new NodeServiceImpl().findListExac(filters, null);

                    for (Node object : nodesServer) {
                        if (!lstNodeIpServer.contains(object.getNodeIp())) {
                            lstNodeIpServer.add(object.getNodeIp());
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstNodeIpServer;
    }

    public void onSelectRowData() {
        if (selectedDbNodes.isEmpty()) {
            isEnableTemplate = false;
        } else {
            isEnableTemplate = true;
        }
    }

    public void onChangeValueSpin(String vId, String tbsName, Integer numOfDatafile) {
        try {
            for (VDatabaseInfos v : selectedDbNodes) {
                if (vId.equals(v.getvId())) {
                    for (int i = 0; i < v.getLstTbsObj().size(); i++) {
                        if (tbsName.equals(v.getLstTbsObj().get(i).getTbsName())) {
                            v.getLstTbsObj().get(i).setNumOfDatafile(numOfDatafile);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void tbsSelectionChanged(String vId) {
        try {
            for (VDatabaseInfos v : selectedDbNodes) {
                if (vId.equals(v.getvId())) {
                    List<TbsNodeObj> lstCurTbsObj = new ArrayList<>();
                    if (v.getLstTbsNameSelected() != null) {
                        Map<String, TbsNodeObj> mapTbsObj = new HashMap<>();
                        if (v.getLstTbsObj() != null) {
                            for (TbsNodeObj tbs : v.getLstTbsObj()) {
                                mapTbsObj.put(tbs.getTbsName(), tbs);
                            }
                        }

                        for (String tbs : v.getLstTbsNameSelected()) {
                            if (mapTbsObj.get(tbs) != null) {
                                lstCurTbsObj.add(mapTbsObj.get(tbs));
                            } else {
                                lstCurTbsObj.add(new TbsNodeObj(tbs, 0));
                            }
                        }
                    }
                    v.setLstTbsObj(lstCurTbsObj);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Ham thuc hien goi tien trinh xu ly lay cac tham so
     * cho cac template add partition, table space, quay vong du lieu, ...
     */
    public void startRunBuildMop() {
        Date startTime = new Date();
        if (selectedDbNodes != null
                && !selectedDbNodes.isEmpty()) {
            logger.info("Start build mop");
            // validate param
            if (!validateInputData()) {
                return;
            }
            // get account default impact database

            FlowRunAction flowRunAction = new FlowRunAction();
            flowRunAction.setCreateDate(new Date());
            flowRunAction.setFlowRunName(flowRunActionName.trim());
            flowRunAction.setStatus(Config.DT_BUIDING);
            flowRunAction.setCreateBy(SessionWrapper.getCurrentUsername());
            flowRunAction.setCrNumber(Constant.DEFAULT_CR_NUMBER_INFRA);
            flowRunAction.setFlowTemplates(selectedFlowTemplate);
            flowRunAction.setTimeRun(new Date());
            flowRunAction.setExecuteType(Config.EXECUTE_TYPE.GNOC.value);
            //quytv7_20182607_fix bug create mop alarm_start
            try {
                HashMap<String, Object> filters = new HashMap<>();
                filters.put("countryCode", AamConstants.VNM);
                List<CatCountryBO> catCountryBOs = new CatCountryServiceImpl().findList(filters);
                flowRunAction.setCountryCode(catCountryBOs.get(0));
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
            //quytv7_20182607_fix bug create mop alarm_end
            List<NodeRun> nodeRuns = new ArrayList<NodeRun>();

            Session session = null;
            Transaction tx = null;
            try {

                Map<Long, List<ActionOfFlow>> mapGroupAction = new HashMap<>();
                for (ActionOfFlow actionOfFlow : selectedFlowTemplate.getActionOfFlows()) {
                    if (mapGroupAction.get(actionOfFlow.getGroupActionOrder()) == null) {
                        mapGroupAction.put(actionOfFlow.getGroupActionOrder(), new ArrayList<ActionOfFlow>());
                    }
                    mapGroupAction.get(actionOfFlow.getGroupActionOrder()).add(actionOfFlow);
                }

                if (selectedDbNodes != null && selectedDbNodes.size() > 0) {

                    new ParamChecklistDatabaseServiceImpl().execteBulk2("delete from ParamChecklistDatabase where flowRunActionId = ?", null, null, true, flowRunAction.getFlowRunId());
                    new AccountGroupMopServiceImpl().execteBulk2("delete from AccountGroupMop where flowRunId = ?", null, null, true, flowRunAction.getFlowRunId());
                    new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction, null, null, true);
                    new ParamValueServiceImpl().execteBulk2("delete from ParamValue where nodeRun.id.flowRunId = ?", null, null, true, flowRunAction.getFlowRunId());
                    new NodeRunGroupActionServiceImpl().execteBulk2("delete from NodeRunGroupAction where id.flowRunId = ? ", null, null, true, flowRunAction.getFlowRunId());
                    new NodeRunServiceImpl().execteBulk2("delete from NodeRun where id.flowRunId = ?", null, null, true, flowRunAction.getFlowRunId());

                    List<NodeRunGroupAction> nodeRunGroupActions = new ArrayList<NodeRunGroupAction>();
                    List<AccountGroupMop> lstAccGroupMop = new ArrayList<>();
                    Node node = null;
                    for (VDatabaseInfos nodeVdatabase : selectedDbNodes) {

                        node = getNodeData(nodeVdatabase, null);
                        if (selectedFlowTemplate.getFlowTemplatesId().equals(Config.FLOW_TEMPLATE_EXPORT_DB)) {
                            node = getNodeData(nodeVdatabase, nodeVdatabase.getSelectedNodeIpImpact());
                            node.setNodeIp(nodeVdatabase.getSelectedNodeIpImpact());
                            node.setEffectIp(nodeVdatabase.getSelectedNodeIpImpact());
                        } else if (selectedFlowTemplate.getFlowTemplatesId().equals(Config.FLOW_TEMPLATE_ADD_DATAFILE_DB)) {
                            node.setLstTbsObj(nodeVdatabase.getLstTbsObj());
                        }

                        if (node != null) {
                            NodeAccount nodeAccImpact = null;
                            if (selectedFlowTemplate.getFlowTemplatesId().equals(Config.FLOW_TEMPLATE_EXPORT_DB)) {
                                // hanhnv68 update get account server 20170711
                                Node nodeServer = getServerNode(node.getNodeIp(), Config.APP_TYPE.SERVER.value);
                                if (nodeServer == null) {
                                    MessageUtil.setErrorMessageFromRes("label.action.updateFail");
                                    return;
                                }
                                // end hanhnv68 update get account server 20170711
                                nodeAccImpact = getAccImpactDefault(nodeServer, Config.APP_TYPE.SERVER.value, Config.ACCOUNT_IMPACT_MONITOR_TYPE.MONITOR.value);
                            } else {
                                nodeAccImpact = getAccImpactDefault(node, Config.APP_TYPE.DATABASE.value, Config.ACCOUNT_IMPACT_MONITOR_TYPE.IMPACT.value);
                            }

                            if (nodeAccImpact == null) {
                                MessageUtil.setErrorMessageFromRes("label.action.updateFail");
                                return;
                            }
                            logger.info(">>>>>> flowRunAction.getFlowRunId(): " + flowRunAction.getFlowRunId());
                            NodeRun nodeRun = new NodeRun(new NodeRunId(node.getNodeId(), flowRunAction.getFlowRunId()), flowRunAction, node);
                            nodeRuns.add(nodeRun);

                            for (Map.Entry<Long, List<ActionOfFlow>> entry : mapGroupAction.entrySet()) {
                                NodeRunGroupAction nodeRunGroupAction = new NodeRunGroupAction(
                                        new NodeRunGroupActionId(node.getNodeId(),
                                                flowRunAction.getFlowRunId(),
                                                entry.getValue().get(0).getStepNum())
                                        , entry.getValue().get(0), nodeRun);
                                nodeRunGroupActions.add(nodeRunGroupAction);

                                // save account default impact database
                                if (nodeAccImpact != null) {
                                    AccountGroupMop accGroup = new AccountGroupMop();
                                    //	    							accGroup.setGroupOrderRun(groupAction.getActionOfFlows().get(0).getGroupActionOrder());
                                    accGroup.setNodeAccountId(nodeAccImpact.getId());
                                    accGroup.setNodeId(node.getNodeId());
                                    accGroup.setFlowRunId(flowRunAction.getFlowRunId());
                                    accGroup.setActionOfFlowId(entry.getValue().get(0).getStepNum());

                                    lstAccGroupMop.add(accGroup);
                                }

                            } // end loop for group action
                        } else {
                            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
                            return;
                        }
                    }

//    				Object[] objs = new FlowRunActionServiceImpl().openTransaction();
//    				session = (Session) objs[0];
//    	    		tx = (Transaction) objs[1];


//        			session.clear();
                    List<ParamChecklistDatabase> lstParamChecklist = new ArrayList<>();
                    if (checklistParamSelected != null && checklistParamSelected.length > 0) {
                        ParamChecklistDatabase paramChecklist = null;
                        for (String paramCode : checklistParamSelected) {
                            paramChecklist = new ParamChecklistDatabase();
                            paramChecklist.setFlowRunActionId(flowRunAction.getFlowRunId());
                            paramChecklist.setParamCode(paramCode);
                            lstParamChecklist.add(paramChecklist);
                        }
                    }

                    if (!lstParamChecklist.isEmpty()) {
                        new ParamChecklistDatabaseServiceImpl().saveOrUpdate(lstParamChecklist);
                        //20180620_tudn_start ghi log DB
                        try {
                            LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                    LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItCommandController.class.getName(),
                                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                                    LogUtils.ActionType.UPDATE,
                                    lstParamChecklist.toString(), LogUtils.getRequestSessionId());
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        //20180620_tudn_end ghi log DB
                    }
                    if (!lstAccGroupMop.isEmpty()) {
                        new AccountGroupMopServiceImpl().saveOrUpdate(lstAccGroupMop);
                        //20180620_tudn_start ghi log DB
                        try {
                            LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                    LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItCommandController.class.getName(),
                                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                                    LogUtils.ActionType.UPDATE,
                                    lstAccGroupMop.toString(), LogUtils.getRequestSessionId());
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        //20180620_tudn_end ghi log DB
                    }
                    new NodeRunServiceImpl().saveOrUpdate(nodeRuns);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItCommandController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.UPDATE,
                                nodeRuns.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB

//    		 		session.flush();
//    		 		session.clear();

                    new NodeRunGroupActionServiceImpl().saveOrUpdate(nodeRunGroupActions);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItCommandController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.UPDATE,
                                nodeRunGroupActions.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                }
    			/*
			    Ghi log tac dong nguoi dung
                */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), MopDatabaseNewController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.CREATE,
                            flowRunAction.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                MessageUtil.setInfoMessageFromRes("label.action.updateOk");
            } catch (Exception e) {
                if (tx != null) {
                    try {
                        tx.rollback();
                    } catch (Exception e2) {
                        logger.error(e2.getMessage(), e2);
                    }
                }
                logger.error(e.getMessage(), e);
                MessageUtil.setErrorMessageFromRes("label.action.updateFail");
            } finally {
                if (session != null) {
                    try {
                        session.close();
                    } catch (Exception e2) {
                        logger.error(e2.getMessage(), e2);
                    }
                }
            }

            try {
                // start process get param in template
                callBuidParamValProc(nodeRuns, flowRunAction);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        } else {
            MessageUtil.setErrorMessageFromRes("Khong co node mang nao duoc chon");
        }
    }

    private boolean validateInputData() {
        boolean check = true;
        if (selectedFlowTemplate.getFlowTemplatesId() == 10083) {
//    		if (tbsDatafileSize == null || tbsDatafileSize.trim().isEmpty()) {
//    			MessageUtil.setErrorMessageFromRes("label.error.notFillAllData");
//    			check = false;
//    		} else if (tbsAutoExtend == 1) {
//    			if (tbsInitSize == null || tbsInitSize.trim().isEmpty()
//    					|| tbsMaxSize == null || tbsMaxSize.trim().isEmpty()) {
//    				MessageUtil.setErrorMessageFromRes("label.error.notFillAllData");
//    				check = false;
//    			}
//    		}
        } else if (selectedFlowTemplate.getFlowTemplatesId() == 10121) {
            for (VDatabaseInfos db : selectedDbNodes) {
                if (db.getLstTbsObj() == null || db.getLstTbsObj().isEmpty()) {
                    MessageUtil.setErrorMessageFromRes("label.error.notFillAllData");
                    check = false;
                    break;
                }
            }
        }
        return check;
    }

    private NodeAccount getAccImpactDefault(Node node, Long accountType, Long impactOrMonitor) {
        NodeAccount acc = null;
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("impactOrMonitor", impactOrMonitor);
            filters.put("accountType", accountType);
            filters.put("serverId", node.getServerId());
            filters.put("active", Constant.status.active);
            acc = new NodeAccountServiceImpl().findListExac(filters, null).get(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            acc = null;
        }
        return acc;
    }

    private Node getServerNode(String nodeIp, Long nodeType) {
        Node node = null;
        if (nodeIp != null) {
            Map<String, Object> filters = new HashMap<>();
            filters.put("nodeIp", nodeIp);
            filters.put("active", Constant.status.active);
            if (nodeType != null && nodeType != 0) {
                filters.put("vendor.vendorId", nodeType);
            }
            try {
                node = new NodeServiceImpl().findList(filters).get(0);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                node = null;
            }
        }
        return node;
    }

    private Node getNodeData(VDatabaseInfos databaseInfo, String nodeIp) {
        Node node = null;
        if (databaseInfo != null) {
            try {
                Map<String, Object> filters = new HashMap<>();
                filters.put("serverId", databaseInfo.getDbId());
                filters.put("port", databaseInfo.getPort());
                filters.put("serviceName", databaseInfo.getServiceName());
                filters.put("jdbcUrl", databaseInfo.getJdbcUrl());
                filters.put("active", Constant.status.active);
                if (nodeIp != null) {
                    filters.put("nodeIp", nodeIp);
                }
                node = new NodeServiceImpl().findList(filters).get(0);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                node = null;
            }
        }
        return node;
    }

    private void callBuidParamValProc(List<NodeRun> nodeRuns, FlowRunAction flowRunAction) {
        try {
            logger.info(">>>>>>>>>>>> Start call process get param values");
            Properties pro = new Properties();
            pro.load(this.getClass().getClassLoader().getResourceAsStream(Config.CONFIG_FILE));
            String urlQltn = pro.getProperty("qltn_url");
            String usernameQltn = pro.getProperty("qltn_user");
            String passwordQltn = PassProtector.decrypt(pro.getProperty("qltn_pass"), Config.SALT);

            Map<String, String> mapConfigSecurity = new HashMap<>();
            mapConfigSecurity = SecurityService.getConfigSecurity();
            Map<String, ResultGetAccount> mapPassGet = new HashMap<>();

            ExecutorService executor = Executors.newFixedThreadPool(4);

            // start get parameter add partition process
            long templateId = flowRunAction.getFlowTemplates().getFlowTemplatesId();
            Long keySession = System.currentTimeMillis();

            ManagerThreadGetParamIns.getInstance().getMapSessionThreadStatus().put(keySession, new AtomicInteger(nodeRuns.size()));
            if (templateId == Config.FLOW_TEMPLATE_ADD_PARTITION_DB.longValue()) {

                for (NodeRun node : nodeRuns) {
                    GetParamAddPartitionThread paramAddPartitionThread = new GetParamAddPartitionThread(flowRunAction, node.getNode(),
                            node, cycleMonth, urlQltn, usernameQltn, passwordQltn, keySession, logger, mapConfigSecurity, mapPassGet);
                    executor.execute(paramAddPartitionThread);
                }
            } else if (templateId == Config.FLOW_TEMPLATE_ADD_TABLESPACE_DB.longValue()) {
                // check fill all data
                for (NodeRun node : nodeRuns) {
                    GetParamAddTableSpaceThread paramTablespaceThread = new GetParamAddTableSpaceThread(flowRunAction, node.getNode(), node, cycleMonth,
                            urlQltn, usernameQltn, passwordQltn, keySession, logger, mapConfigSecurity, mapPassGet);
                    executor.execute(paramTablespaceThread);
                }
            } else if (templateId == Config.FLOW_TEMPLATE_ADD_DATAFILE_DB.longValue()) {
                // check fill all data
//    			if (getMapNumberDatafile() == null) {
//    				logger.error(">>>>>>>>>>>>>> ERROR GET MAP NUMBER OF EACH DATAFILE");
//    				return;
//    			}
//    			datafileTablespaceNames = datafileTablespaceNames.toUpperCase();
                for (NodeRun node : nodeRuns) {
                    GetParamAddDatafileThread paramDatafileThread = new GetParamAddDatafileThread(flowRunAction, node.getNode(), node, 1,
                            urlQltn, usernameQltn, passwordQltn, keySession, logger, mapConfigSecurity, mapPassGet);
                    executor.execute(paramDatafileThread);
                }
            } else if (templateId == Config.FLOW_TEMPLATE_EXPORT_DB) {
                for (NodeRun node : nodeRuns) {
                    GetParamExportTableThread paramExportThread = new GetParamExportTableThread(flowRunAction, node.getNode(), node, null, urlQltn, usernameQltn, passwordQltn, keySession, logger, mapConfigSecurity, mapPassGet);
                    executor.execute(paramExportThread);
                }
            } else if (templateId == Config.FLOW_TEMPLATE_DROP_PAR_TBS_DB) {
                for (NodeRun node : nodeRuns) {
                    GetParamDropTableThread paramDropThread = new GetParamDropTableThread(flowRunAction, node.getNode(), node, null, urlQltn, usernameQltn, passwordQltn, keySession, logger, mapConfigSecurity, mapPassGet);
                    executor.execute(paramDropThread);
                }
            } else if (templateId == Config.FLOW_TEMPLATE_RESIZE_PARTITION) {
                for (NodeRun node : nodeRuns) {
                    GetParamResizePartitionThread paramresizeThread = new GetParamResizePartitionThread(flowRunAction, node.getNode(), node, null, urlQltn, usernameQltn, passwordQltn, keySession, logger, mapConfigSecurity, mapPassGet);
                    executor.execute(paramresizeThread);
                }
            }
            logger.info("<<<<<<<<<<<< End call process get param values");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Map<String, Integer> getMapNumberDatafile() {
        if (datafileNumbers != null && datafileTablespaceNames != null) {
            Map<String, Integer> mapNumberDatafile = new HashMap<>();
            try {
                List<String> lstTbsName = Arrays.asList(datafileTablespaceNames.trim().split(Config.SPLITTER_VALUE));
                List<String> lstDtfNumber = Arrays.asList(datafileNumbers.trim().split(Config.SPLITTER_VALUE));

                for (int i = 0; i < lstTbsName.size(); i++) {
                    mapNumberDatafile.put(lstTbsName.get(i), Integer.valueOf(lstDtfNumber.get(i)));
                }
                return mapNumberDatafile;
            } catch (Exception e) {
//				mapNumberDatafile = null;
                logger.error(e.getMessage(), e);
            }
        } else {
            return null;
        }
        return null;
    }

    public static void main(String[] args) {
        try {
            String passwordQltn = PassProtector.decrypt("aNXwe7427MfI7PplhiCtwQu+SPBqRK6qF9nHxeiCYu1d4g85G9EpciKrKJ2wgkvor5GueiSni2oGxyd2iHxFrtAJvZiY7gn+4VGrJ9BIu0gFYyS3/AjOOJI7L6jwz3lJnZMvnZapVKZTd1wFpPA3t2ZXqUVngxdbPCJ871wgOiAQPITJy9XSp4lkC3BIjOGr", Config.SALT);
            System.out.println(passwordQltn);
//			Map<String, Object> filters = new HashMap<String, Object>();
//			filters.put("vendor.vendorId", 1l);
//			List<Node> lstNodeOld = new NodeServiceImpl().findListExac(filters, null);
//			Map<String, Node> mapNodeOld = new HashMap<String, Node>();
//			System.out.println(lstNodeOld.size());

//			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmm");
//			System.out.println(df.format(new Date()));
//
//			Map<String, Object> filters = new HashedMap();
//			filters.put("nodeIp", "10.58.3.14");
//			List<Node> lst = new NodeServiceImpl().findListExac(filters, null);
//			for (Node n : lst) {
//				System.out.println(n.getNodeIp());
//			}

//			System.out.print(PassProtector.encrypt("qwertyuiop", Config.SALT));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    static class Test implements Runnable {

        @Override
        public void run() {
            try {
                System.out.println("start");
                Thread.sleep(30000);
                System.out.println("finish");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }


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

    public StreamedContent getFile() {
        return file;
    }

    public void setFile(StreamedContent file) {
        this.file = file;
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

    public List<FlowTemplates> getFlowTemplates() {
        return flowTemplates;
    }

    public void setFlowTemplates(List<FlowTemplates> flowTemplates) {
        this.flowTemplates = flowTemplates;
    }

    public FlowTemplates getSelectedFlowTemplate() {
        return selectedFlowTemplate;
    }

    public void setSelectedFlowTemplate(FlowTemplates selectedFlowTemplate) {
        this.selectedFlowTemplate = selectedFlowTemplate;
    }

    public List<VDatabaseInfos> getSelectedDbNodes() {
        return selectedDbNodes;
    }

    public void setSelectedDbNodes(List<VDatabaseInfos> selectedDbNodes) {
        this.selectedDbNodes = selectedDbNodes;
    }

    public Integer getCycleMonth() {
        return cycleMonth;
    }

    public void setCycleMonth(Integer cycleMonth) {
        this.cycleMonth = cycleMonth;
    }

    public String getFlowRunActionName() {
        return flowRunActionName;
    }

    public void setFlowRunActionName(String flowRunActionName) {
        this.flowRunActionName = flowRunActionName;
    }

    public LazyDataModel<VDatabaseInfos> getLazyNodeDatabase() {
        return lazyNodeDatabase;
    }

    public void setLazyNodeDatabase(LazyDataModel<VDatabaseInfos> lazyNodeDatabase) {
        this.lazyNodeDatabase = lazyNodeDatabase;
    }

    public VDatabaseInfosServiceImpl getvDatabaseInfosService() {
        return vDatabaseInfosService;
    }

    public void setvDatabaseInfosService(
            VDatabaseInfosServiceImpl vDatabaseInfosService) {
        this.vDatabaseInfosService = vDatabaseInfosService;
    }

    public List<SelectItem> getChecklistParams() {
        return checklistParams;
    }

    public void setChecklistParams(List<SelectItem> checklistParams) {
        this.checklistParams = checklistParams;
    }

    public String[] getChecklistParamSelected() {
        return checklistParamSelected;
    }

    public void setChecklistParamSelected(String[] checklistParamSelected) {
        this.checklistParamSelected = checklistParamSelected;
    }

    public boolean isEnableTemplate() {
        return isEnableTemplate;
    }

    public void setEnableTemplate(boolean isEnableTemplate) {
        this.isEnableTemplate = isEnableTemplate;
    }

    public String getTbsDatafileSize() {
        return tbsDatafileSize;
    }

    public void setTbsDatafileSize(String tbsDatafileSize) {
        this.tbsDatafileSize = tbsDatafileSize;
    }

    public Integer getTbsAutoExtend() {
        return tbsAutoExtend;
    }

    public void setTbsAutoExtend(Integer tbsAutoExtend) {
        this.tbsAutoExtend = tbsAutoExtend;
    }

    public String getTbsInitSize() {
        return tbsInitSize;
    }

    public void setTbsInitSize(String tbsInitSize) {
        this.tbsInitSize = tbsInitSize;
    }

    public String getTbsMaxSize() {
        return tbsMaxSize;
    }

    public void setTbsMaxSize(String tbsMaxSize) {
        this.tbsMaxSize = tbsMaxSize;
    }

    public String getDatafileDatafileSize() {
        return datafileDatafileSize;
    }

    public void setDatafileDatafileSize(String datafileDatafileSize) {
        this.datafileDatafileSize = datafileDatafileSize;
    }

    public Integer getDatafileAutoExtend() {
        return datafileAutoExtend;
    }

    public void setDatafileAutoExtend(Integer datafileAutoExtend) {
        this.datafileAutoExtend = datafileAutoExtend;
    }

    public String getDatafileInitSize() {
        return datafileInitSize;
    }

    public void setDatafileInitSize(String datafileInitSize) {
        this.datafileInitSize = datafileInitSize;
    }

    public String getDatafileMaxSize() {
        return datafileMaxSize;
    }

    public void setDatafileMaxSize(String datafileMaxSize) {
        this.datafileMaxSize = datafileMaxSize;
    }

    public String getDatafileTablespaceNames() {
        return datafileTablespaceNames;
    }

    public void setDatafileTablespaceNames(String datafileTablespaceNames) {
        this.datafileTablespaceNames = datafileTablespaceNames;
    }

    public String getDatafileNumbers() {
        return datafileNumbers;
    }

    public void setDatafileNumbers(String datafileNumbers) {
        this.datafileNumbers = datafileNumbers;
    }


}
