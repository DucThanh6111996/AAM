package com.viettel.it.controller;

import com.google.gson.Gson;
import com.viettel.controller.AppException;
import com.viettel.controller.SysException;
import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.*;
import com.viettel.it.object.*;
import com.viettel.it.persistence.*;
import com.viettel.it.persistence.Category.CategoryConfigGetNodeServiceImpl;
import com.viettel.it.persistence.Category.CategoryDomainServiceImpl;
import com.viettel.it.persistence.Category.CategoryGroupDomainServiceImpl;
import com.viettel.it.thread.FixedExecutionRunnable;
import com.viettel.it.thread.OpenBlockingSidnThread;
import com.viettel.it.util.*;
import com.viettel.passprotector.PassProtector;
import com.viettel.controller.AamConstants;
import com.viettel.util.Constant;
import com.viettel.util.SessionUtil;
import com.viettel.util.SessionWrapper;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.streaming.SXSSFSheet;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.primefaces.context.RequestContext;
import org.primefaces.event.*;
import org.primefaces.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


@ViewScoped
@ManagedBean
public class ItActionController implements Serializable {

    protected static final Logger logger = LoggerFactory.getLogger(ItActionController.class);

    public static final Long ROOT_TREE_ID = -1L;
    public static final Integer NODE_EXPENDED = 1;
    public static final Integer NODE_NOT_EXPEND = 0;
    public static final String SUB_PARENT_NODE = "sub_parent";
    public static final String PARENT_NODE = "parent";
    public static final String CHILD_NODE = "child";
    /*20181225_hoangnd_them thi truong_start*/
    public static final String GRAND_NODE = "grand";
    public static final String COUNTRY_NODE = "country";
    private String countryCode = AamConstants.VNM;
    private Integer treeNodeLevel;
    /*20181225_hoangnd_them thi truong_end*/
    public static final String IMPACT_LABEL_NODE = "impact_label";
    public static final String ROLLBACK_LABEL_NODE = "rollback_label";
    public static final Integer MAX_LENGTH_ACTION_NAME = 500;
    public static final String KEY_START_GET_PARAM_VALUE = "-------------- Danh sach tham so can truyen (cac gia tri cach nhau bang dau ;) -----------";

    @ManagedProperty(value = "#{actionItService}")
    private ActionServiceImpl actionServiceImpl;

    @ManagedProperty(value = "#{actionDetailService}")
    private ActionDetailServiceImpl actionDetailServiceImpl;

    @ManagedProperty(value = "#{vendorService}")
    private VendorServiceImpl vendorServiceImpl;

    @ManagedProperty(value = "#{nodeTypeService}")
    private NodeTypeServiceImpl nodeTypeServiceImpl;

    @ManagedProperty(value = "#{versionService}")
    private VersionServiceImpl versionServiceImpl;

    @ManagedProperty(value = "#{commandTelnetParserService}")
    private CommandTelnetParserServiceImpl commandTelnetParserServiceImpl;

    @ManagedProperty(value = "#{itActionLogService}")
    private ItActionLogServiceImpl itActionLogServiceImpl;

    @ManagedProperty(value = "#{itNodeService}")
    private ItNodeServiceImpl itNodeService;

    @ManagedProperty(value = "#{itNodeActionService}")
    private ItNodeActionServiceImpl itNodeActionService;

    @ManagedProperty(value = "#{itUserBusGroupService}")
    private ItUserBusGroupServiceImpl itUserBusGroupService;

    @ManagedProperty(value = "#{itCommandLogService}")
    private ItCommandLogServiceImpl itCommandLogService;

    @ManagedProperty(value = "#{itUsersServices}")
    private ItUsersServicesImpl itUsersServices;

    @ManagedProperty(value = "#{itUserActionService}")
    private ItUserActionServiceImpl itUserActionService;

    @ManagedProperty("#{excelUtil}")
    private ExcelWriterUtils exelUtil;

    @ManagedProperty("#{flowTemplateMapAlarmService}")
    private FlowTemplateMapAlarmServiceImpl flowTemplateMapAlarmService;

    /*20190109_hoangnd_them thi truong_start*/
    @ManagedProperty("#{itBusGroupService}")
    private ItBusGroupServiceImpl itBusGroupService;
    /*20190109_hoangnd_them thi truong_end*/

    private TreeNode rootNode;
    private TreeNode rootNode2;
    private TreeNode selectedNode;

    private Action insertNode;
    private boolean isEdit = false;
    private boolean isClone = false;

    private Action selectedAction; // action ma action detail link den
    private Vendor selectedVendor;
    private NodeType selectedNodeType;
    private Version selectedVersion;
    private ActionDetail actionDetail;
    private ActionDetail selectedActionDetail;
    private CommandDetail selectedCmdDetail;
    private CommandTelnetParser selectedCmdTelnetParser;
    private ActionCommand selectedActionCommand;

    private Long stationDetailStatus;
    private boolean isEditActionDetail;

    private List<ActionCommand> lstActionCommand = new LinkedList<>();
    private String userName;

    private List<ActionDetail> lstActionDetail = new ArrayList<>();
    private List<ActionCommand> lstActionCmdDel = new ArrayList<>();

    private String keyActionSearch = null;
    private String actionNameToClone;

    private DualListModel<ItUsers> usersPicklist;
    private List<CommandDetail> commandDetails;
    private List<CommandDetail> commandLogDetails;
    private LazyDataModelBaseNew<ItActionLog, Long> lazyActionLog;

    private LazyDataModel<Node> nodeList;
    private Node selectedItNode;
    private List<ItNodeAction> nodeActionList;
    private String userNode;
    private String passNode;
    private StreamedContent fileCommandLog;
    private List<ItCommandLog> commandLogList;
    private List<TreeNode> lstParentNode;
    ArrayList<Long> lstParentId;
    ArrayList<Action> listActionMD;
    List<Action> resultSearch;

    //default value
    private NodeType nodeDefault;
    private Version versionDefault;
    private Action verifyActtion;

    private ItNodeAction itNodeActionSelected;
    private List<NodeAccount> nodeAccounts;
    private NodeAccount nodeAccountSelected;
    private int dem = 0;
    private int build = 0;
    private Map<String, ParamInput> mapParamVals = new HashedMap();
    List<CmdObject> impactCmdsObj = new ArrayList<>();
    List<CmdObject> writeLogCmdsObj = new ArrayList<>();
    private boolean isImportSucess;
    private ItActionLog selectedActionLog;
    private ItCommandLog selectedCmdLog;
    private List<List<String>> sqlDataTables;
    private Integer rescueCycle; // chu ky tu dong thuc hien khai bao ung cuu mo chan
    private Date startTime; // thoi gian bat dau thuc hien chay mo chan
    private Date endTime; // thoi gian ngung thuc hien mo chan
    private Integer openBlockingType; // 1 - manual; 2 - auto
    private Integer numOfThread; // so tien trinh chay song song ung voi moi node

    //tuanda38_20180911_map param alarm_start
    private List<ParamInput> lstParamInputInTemplates = new ArrayList<>();
    private ConcurrentHashMap<String, FlowTemplateMapAlarm> mapParamAlarmInTemplates = new ConcurrentHashMap<>();
    private List<FlowTemplateMapAlarm> lstParamAlarmInTempates = new ArrayList<>();
    private List<FlowTemplateMapAlarm> lstParamNodeAlarmInTempates = new ArrayList<>();
    private ComboBoxObject categoryConfigGetNodeGroup;
    private List<ComboBoxObject> categoryConfigGetNodeGroups;
    private CategoryConfigGetNode categoryConfigGetNode;
    private List<CategoryGroupDomain> categoryGroupDomains;
    private CategoryGroupDomain categoryGroupDomain;
    private String systemTypeMapParam;
    private List<CategoryDomain> categoryDomains;
    private TemplateGroup selectTemplateGroup;
    //tuanda38_20180911_map param alarm_end

    @PostConstruct
    public void onStart() {
        try {
            selectedCmdLog = new ItCommandLog();
            //selectedNodeType = new NodeTypeServiceImpl().findById(Config.NODE_TYPE_ID_DEFAULT);
            listActionMD = new ArrayList<>();
            userName = SessionWrapper.getCurrentUsername();
            actionDetail = new ActionDetail();
            insertNode = new Action();
            selectedAction = new Action();
            if (new SessionUtil().isItBusinessAdmin()) {
                createTree();
            } else {
                createTree2();
            }
            for (long i = 1; i <= 10; i++) {
                checkIsAdmin(i);
            }
            selectedActionDetail = new ActionDetail();
            selectedActionDetail.setActionCommands(new ArrayList<ActionCommand>(0));

            usersPicklist = new DualListModel<>();

            nodeDefault = nodeTypeServiceImpl.findById(Config.NODE_DEFAULT_ID);
            versionDefault = versionServiceImpl.findById(Config.VERSION_DEFAULT_ID);

            LinkedHashMap orders = new LinkedHashMap();
            orders.put("startTime", "DESC");
            orders.put("endTime", "DESC");
            orders.put("status", "DESC");
            orders.put("userRun", "DESC");
            orders.put("action.name", "ASC");
            lazyActionLog = new LazyDataModelBaseNew<ItActionLog, Long>(itActionLogServiceImpl, null, orders);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void reloadActionLog() {
        try {
//			logger.info("reload");
            LinkedHashMap orders = new LinkedHashMap();
            orders.put("startTime", "DESC");
            orders.put("endTime", "DESC");
            orders.put("status", "DESC");
            orders.put("userRun", "DESC");
            orders.put("action.name", "ASC");
            lazyActionLog = new LazyDataModelBaseNew<ItActionLog, Long>(itActionLogServiceImpl, null, orders);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    private List<ItUsers> getUsersImpactAvalable() {
        List<ItUsers> lstUsers = new ArrayList<>();
        try {
            Map<String, Object> filters = new HashedMap();
            filters.put("", "");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstUsers;
    }

    /**
     * @author huynx6
     * Tim kiem tren tree
     */
    public void searchActionNode() {
        Action root;
        try {
            root = actionServiceImpl.findById(ROOT_TREE_ID);
            if (root != null) {
                rootNode = new DefaultTreeNode("action", "Root", null);

                Map<String, Object> filter = new HashMap<>();
                if (keyActionSearch != null)
                    filter.put("name", keyActionSearch);

                List<Action> resultSearch = actionServiceImpl.findList(filter);

                Set<Action> actionParents = new HashSet<>();
                actionParents.addAll(resultSearch);
                for (Action action : resultSearch) {
                    action.setExpanded(true);
                    getParent(action, actionParents);
                }

                buildTree(root, rootNode, new ArrayList<>(actionParents), 1);
                rootNode.setExpanded(true);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void searchActionNode2() {
        resultSearch = new ArrayList<>();
        Action root;
        try {
            root = actionServiceImpl.findById(ROOT_TREE_ID);
            if (root != null) {
                rootNode2 = new DefaultTreeNode("action", "Root", null);

                Map<String, Object> filter = new HashMap<>();
                if (keyActionSearch != null) {
                    //tuanda38_20180914_search by actionId_start
                    if (keyActionSearch.contains(":")) {
                        filter.put("actionId", Long.parseLong(keyActionSearch.replace(":", "").trim()));
                    } else {
                        filter.put("name", keyActionSearch);
                    }
                    //tuanda38_20180914_search by actionId_end
                }

                resultSearch = actionServiceImpl.findList(filter);

                Set<Action> actionParents = new HashSet<>();
                actionParents.addAll(resultSearch);
                for (Action action : resultSearch) {
                    action.setExpanded(true);
                    getParent(action, actionParents);
                }
                if (new SessionUtil().isItBusinessAdmin()) {
                    buildTree(root, rootNode2, new ArrayList<>(actionParents), 1);
                    rootNode2.setExpanded(true);
                } else {
                    ArrayList<Action> listS = new ArrayList<>(actionParents);
                    ArrayList<Action> listAdd = new ArrayList<>();
                    for (Action action : listS) {
                        for (Action action2 : listActionMD) {
                            if (action.getName().equals(action2.getName())) {
                                listAdd.add(action);
                            }
                        }
                    }
                    if (listAdd.size() > 0) {
                        buildTree(root, rootNode2, listAdd, 1);
                        rootNode2.setExpanded(true);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    public void getParent(Action action, Set<Action> actionParents) {
        if (action.getAction() != null) {
            action.setExpanded(true);
            actionParents.add(action.getAction());
            getParent(action.getAction(), actionParents);
        } else
            return;
    }

    public void prepareExcute() {
        isImportSucess = false;
        selectedAction = new Action();
        commandDetails = new ArrayList<>();
        commandLogDetails = new ArrayList<>();
        try {
            selectedAction = (Action) selectedNode.getData();

            if (selectedAction.getActionDetails() == null
                    || selectedAction.getActionDetails().isEmpty()) {
                MessageUtil.setErrorMessageFromRes("message.err.no.comamnd.action");
            } else if (selectedAction.getNodeActions() == null ||
                    selectedAction.getNodeActions().isEmpty()) {
                MessageUtil.setErrorMessageFromRes("message.err.action.no.node.impact");
            } else {
                List<ActionCommand> actionCommands = selectedAction.getActionDetails().get(0).getActionCommands();
                for (ActionCommand actionCmd : actionCommands) {
                    if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.IMPACT.value) {
                        commandDetails.add(actionCmd.getCommandDetail());
                    } else if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.LOG.value) {
                        CommandDetail logCmdDetail = actionCmd.getCommandDetail();
                        logCmdDetail.setCmdLogOrderRun(actionCmd.getLogOrderRun());
                        commandLogDetails.add(actionCmd.getCommandDetail());
                    }
                }

                if (!valNodeAccount()) {
                    return;
                } else if (!valParamCodesLog()) {
                    MessageUtil.setErrorMessageFromRes("msg.err.get.paramlog");
                } else {

                    // if execute manual
                    mapParamVals = new HashMap<>();
                    for (CommandDetail cmdDetail : commandDetails) {
                        if (cmdDetail.getParamInputs() != null) {
                            for (ParamInput param : cmdDetail.getParamInputs()) {
                                if (mapParamVals.get(param.getParamCode()) == null
                                        || (mapParamVals.get(param.getParamCode()) != null && param.getParamDefault() != null)) {
                                    if (param.getParamDefault() != null) {
                                        param.setParamValue(param.getParamDefault());
                                    }
                                    mapParamVals.put(param.getParamCode(), param);
                                }
                            }
                        }
                    }

                    RequestContext.getCurrentInstance().update("panelAddParamExcute");
                    RequestContext.getCurrentInstance().execute("PF('dlgAddParamExcute').show()");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private boolean valNodeAccount() {
        boolean check = true;
        try {
            List<ItNodeAction> nodeActionImpact = getNodeAction(Config.ACTION_NODE_TYPE.IMPACT.value, selectedAction, null);
            List<ItNodeAction> nodeActionLogs = getNodeAction(Config.ACTION_NODE_TYPE.LOG.value, selectedAction, null);

            if (nodeActionImpact == null) {
                MessageUtil.setErrorMessageFromRes("label.err.node.node.impact");
                check = false;
            } else if (nodeActionLogs == null && commandLogDetails != null && !commandLogDetails.isEmpty()) {
                MessageUtil.setErrorMessageFromRes("label.err.node.node.write.log");
                check = false;
            } else if (nodeActionLogs != null && !valAccount(nodeActionLogs)) {
                MessageUtil.setErrorMessageFromRes("label.err.node.account.imact");
                check = false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            check = false;
        }

        return check;
    }

    /*
      Check all param code command log are contained in list command
     */
    private boolean valParamCodesLog() {
        boolean check = true;
        if (commandDetails != null && commandLogDetails != null) {
            try {
                Map<String, String> mapParamImpact = new HashMap<>();
                for (CommandDetail cmdDetail : commandDetails) {
                    if (cmdDetail.getParamInputs() != null) {
                        for (ParamInput param : cmdDetail.getParamInputs()) {
                            mapParamImpact.put(param.getParamCode(), "1");
                        }
                    }
                }

                for (CommandDetail cmdDetail : commandLogDetails) {
                    if (cmdDetail.getParamInputs() != null) {
                        for (ParamInput param : cmdDetail.getParamInputs()) {
                            if (mapParamImpact.get(param.getParamCode()) == null) {
                                return false;
                            }
                        }
                    }
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        return check;
    }

    public void clearParam() {
        try {
            if (mapParamVals != null && !mapParamVals.isEmpty()) {
                for (Map.Entry<String, ParamInput> entry : mapParamVals.entrySet()) {
                    entry.getValue().setParamValue(null);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void buildParamManual() {
//        impactCmdsObj = new ArrayList<>();
//        writeLogCmdsObj = new ArrayList<>();
        try {
            for (Map.Entry<String, ParamInput> entry : mapParamVals.entrySet()) {
                if (entry.getValue().getParamValue() == null || entry.getValue().getParamValue().trim().isEmpty()) {
//                    impactCmdsObj = new ArrayList<>();
//                    writeLogCmdsObj = new ArrayList<>();
                    MessageUtil.setInfoMessageFromRes("label.error.notFillAllData");
                    return;
                }
            }

            impactCmdsObj = buildCmdObj(commandDetails, mapParamVals, false, null);
            if (commandLogDetails != null && !commandLogDetails.isEmpty()) {
                writeLogCmdsObj = buildCmdObj(commandLogDetails, mapParamVals, true, null);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void valActionSelected(int type) {
        if (selectedAction == null || selectedAction.getActionId() == null) {
            RequestContext.getCurrentInstance().update("form:mainMessage");
            MessageUtil.setErrorMessageFromRes("message.err.no.node.selected");
        } else {
            /*20180710_hoangnd_chi cho import action muc la_start*/
            if (selectedAction.getActions() != null && !selectedAction.getActions().isEmpty() && type == 2) {
                RequestContext.getCurrentInstance().update("form:mainMessage");
                MessageUtil.setErrorMessageFromRes("error.leaf.action");
                return;
            }
            /*20180710_hoangnd_chi cho import action muc la_end*/
            if (type == 1) {
                RequestContext.getCurrentInstance().update("panelAddParamExcute:dlgAddParamExcute");
                RequestContext.getCurrentInstance().execute("PF('dlgAddParamExcute').show()");
            } else if (type == 2) {
                RequestContext.getCurrentInstance().update("panelAddParamExcute:dlgImportParam");
                RequestContext.getCurrentInstance().execute("PF('dlgImportParam').show()");
            }
        }
    }

    /*
    validate data before confirm excute
     */
    public void valExcute() {
        if (selectedAction == null
                || selectedAction.getActionId() == null
                || impactCmdsObj == null
                || impactCmdsObj.isEmpty()) {
            MessageUtil.setErrorMessageFromRes("message.err.no.node.selected");
        } else {
            if (commandDetails != null && !commandDetails.isEmpty()) {
                if (!impactCmdsObj.isEmpty()) {

                    RequestContext.getCurrentInstance().update("formConfirmExcute:confirmExcuteAction");
                    RequestContext.getCurrentInstance().execute("PF('confirmExcuteAction').show()");
                }
            } else {
                MessageUtil.setErrorMessageFromRes("label.error.no.command");
            }
        }
        RequestContext.getCurrentInstance().update("form:mainMessage");
    }

    public void preCheckRescue() {
        if (openBlockingType == null || openBlockingType.intValue() == 0) {
            MessageUtil.setErrorMessageFromRes("label.error.rescue.open.not.select.type");
        } else {
            RequestContext.getCurrentInstance().update("confirmOpenBlockingForm:confirmExcuteOpenBlocking");
            RequestContext.getCurrentInstance().execute("PF('confirmExcuteOpenBlocking').show()");
        }
    }

    /*
    Start execute rescue open blocking msisdn
     */
    public void excuteOpenBlocking() {
        /*
         Validate data
         */
        try {
            boolean check = true;
            if (openBlockingType == Config.OPEN_BLOCKING_SIDN_TYPE.AUTO.value.intValue()) {
                if (rescueCycle == null
                        || rescueCycle == 0
                        || numOfThread == null
                        || numOfThread == 0
                        || startTime == null
                        || endTime == null) {
                    check = false;
                }
            }
            if (!check) {
                MessageUtil.setErrorMessageFromRes("label.error.notFillAllData");
                return;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        // Get running counter
        int runningNumber = 1;
        // Number of second initial delay for first time running
        long initialDelay = 0;

        if (openBlockingType == Config.OPEN_BLOCKING_SIDN_TYPE.AUTO.value.intValue()) {
            Date currTime = new Date();
            if (startTime.before(currTime)) {
                MessageUtil.setErrorMessageFromRes("label.err.time.before.now");
                return;
            } else if (endTime.before(startTime)) {
                MessageUtil.setErrorMessageFromRes("service.mop.cr_uctt.warning.end_time.before_start");
                return;
            }

            Long secondDistances = (endTime.getTime() - startTime.getTime()) / 1000;
            runningNumber = secondDistances.intValue() / rescueCycle.intValue();
            initialDelay = (startTime.getTime() - currTime.getTime()) / 1000;
        }

        /*
        Get database config
         */
        String urlSidnDb = null;
        String userSidnDb = null;
        String passSidnDb = null;
        String sqlQuery = null;
        try {
            urlSidnDb = MessageUtil.getResourceBundleConfig("open_blocking_database_url");
            userSidnDb = MessageUtil.getResourceBundleConfig("open_blocking_database_user");
            passSidnDb = MessageUtil.getResourceBundleConfig("open_blocking_database_password");
            sqlQuery = MessageUtil.getResourceBundleConfig("open_blocking_database_query");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (urlSidnDb == null
                || userSidnDb == null
                || passSidnDb == null
                || sqlQuery == null) {
            MessageUtil.setErrorMessageFromRes("label.err.get.config.openblock");
            return;
        }

        // Schedule run open blocking sidn
        try {
            if (rescueCycle == null || rescueCycle == 0) {
                rescueCycle = 1;
            }
            if (numOfThread == null || numOfThread.intValue() == 0) {
                numOfThread = 1;
            }
            ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);
            new FixedExecutionRunnable(
                    new OpenBlockingSidnThread(urlSidnDb, userSidnDb, passSidnDb, sqlQuery, SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), LogUtils.getRequestSessionId(), new Util(), numOfThread),
                    runningNumber, scheduler).runNTimes(rescueCycle, initialDelay, TimeUnit.SECONDS);

            openBlockingType = 0;
            rescueCycle = 0;
            numOfThread = 0;

            MessageUtil.setInfoMessageFromRes("message.execute.success");
        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("message.execute.fail");
            logger.error(e.getMessage(), e);
        }
    }

    /*
    start impact action and node
    */
    public void excuteImpactAction() {

        startTime = new Date();
        selectedActionLog = new ItActionLog();
        if (impactCmdsObj == null || impactCmdsObj.isEmpty()) {
            MessageUtil.setInfoMessageFromRes("label.error.notFillAllData");
            return;
        }

        List<List<CmdObject>> lstCmdLogsRun = new ArrayList<>();
        if (writeLogCmdsObj != null) {
            Map<Long, List<CmdObject>> mapCmdLogs = new HashMap<>();
            for (CmdObject cmdLog : writeLogCmdsObj) {
                if (mapCmdLogs.get(cmdLog.getWriteLogOrder()) == null) {
                    mapCmdLogs.put(cmdLog.getWriteLogOrder(), new ArrayList<CmdObject>());
                }
                mapCmdLogs.get(cmdLog.getWriteLogOrder()).add(cmdLog);
            }
            for (Long order : mapCmdLogs.keySet()) {
                lstCmdLogsRun.add(mapCmdLogs.get(order));
            }

        }

        ItActionLog actionLog = new ItActionLog();
        try {
            String serverIp = MessageUtil.getResourceBundleConfig("process_socket_it_business_ip");
            int serverPort = Integer.parseInt(MessageUtil.getResourceBundleConfig("process_socket_it_business_port"));

            if (selectedAction.getProvisioningType() != null
                    && selectedAction.getProvisioningType().intValue() == Config.PROVISIONING_LIB_TYPE.AP.value) {
                serverIp = MessageUtil.getResourceBundleConfig("process_socket_it_business_ip_lib_ap");
                serverPort = Integer.parseInt(MessageUtil.getResourceBundleConfig("process_socket_it_business_port_lib_ap"));
            }

            List<ItNodeAction> nodeActionImpact = getNodeAction(Config.ACTION_NODE_TYPE.IMPACT.value, selectedAction, null);
            List<ItNodeAction> nodeActionLog = getNodeAction(Config.ACTION_NODE_TYPE.LOG.value, selectedAction, null);

            if (nodeActionLog != null) {
                Collections.sort(nodeActionLog, new Comparator<ItNodeAction>() {
                    public int compare(final ItNodeAction object1, final ItNodeAction object2) {
                        return object1.getLogOrderRun().compareTo(object2.getLogOrderRun());
                    }
                });
            }

            MessageItBusObject mesObj = new MessageItBusObject();

            actionLog.setAction(selectedAction);
            actionLog.setStartTime(new Date());
            actionLog.setUserRun(SessionUtil.getCurrentUsername());
            actionLog.setStatus(1l);

            Long actionLogId = new ItActionLogServiceImpl().save(actionLog);
            actionLog.setId(actionLogId);

            selectedActionLog = actionLog;

            mesObj.setActionName(selectedAction.getName());
            mesObj.setFlowRunId(selectedAction.getActionId());
            mesObj.setLstImpactCmds(impactCmdsObj);
            mesObj.setLstLogCmds(writeLogCmdsObj);

            String username;
            String pass;

            /*
            Xu ly cac node tac dong
             */

            //20181023_tudn_start load pass security
            Map<String, String> mapNodePassword = new HashMap<>();
            //20181023_tudn_end load pass security

            List<CmdNodeObject> cmdNodeObjects = new ArrayList<>();
            for (ItNodeAction nodeAction : nodeActionImpact) {

                CmdNodeObject nodeObject = new CmdNodeObject();
                nodeObject.setNodeCode(nodeAction.getNode().getNodeCode());
                nodeObject.setNodeId(nodeAction.getNode().getNodeId());
                nodeObject.setNodeIp(nodeAction.getNode().getNodeIp());
                nodeObject.setNodeName(nodeAction.getNode().getNodeName());
                nodeObject.setProtocol(commandDetails.get(0).getProtocol());
                nodeObject.setType(Config.ACTION_NODE_TYPE.IMPACT.value);
                nodeObject.setEffectIp(nodeAction.getNode().getEffectIp());
                nodeObject.setOsType(nodeAction.getNode().getOsType());
                nodeObject.setPort(nodeAction.getNode().getPort());
                nodeObject.setUrl(nodeAction.getNode().getJdbcUrl());
                nodeObject.setServerId(nodeAction.getNode().getServerId() + "");
                nodeObject.setVendorName(nodeAction.getNode().getVendor().getVendorName());
                nodeObject.setTotalTimeout(120000l);

                if (nodeAction.getActionAccounts() != null && !nodeAction.getActionAccounts().isEmpty()) {
                    username = PasswordEncoder.encrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getUsername());
                    try {
                        //20181023_tudn_start load pass security
//                        pass = PasswordEncoder.encrypt(PassProtector.decrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword(), Config.SALT));
                        try {
                            pass = PasswordEncoder.encrypt(PassProtector.decrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword(), Config.SALT));
                        } catch (Exception e) {
                            pass = nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword();
                            logger.error(e.getMessage(), e);
                        }
                        //Quytv7_tam thoi off phan business call security
                        /*String accType = null;
                        if(nodeAction.getActionAccounts().get(0).getNodeAccount() !=null && nodeAction.getActionAccounts().get(0).getNodeAccount().getAccountType()!=null  ) {
                            if (Constant.ACCOUNT_TYPE_SERVER.equalsIgnoreCase(nodeAction.getActionAccounts().get(0).getNodeAccount().getAccountType().toString())) {
                                accType = Constant.SECURITY_SERVER;
                            } else if (Constant.ACCOUNT_TYPE_DATABASE.equalsIgnoreCase(nodeAction.getActionAccounts().get(0).getNodeAccount().getAccountType().toString())) {
                                accType = Constant.SECURITY_DATABASE;
                            }
                        }
                        Map<String, String> mapConfigSecurity = SecurityService.getConfigSecurity();
                        ResultGetAccount resultGetAccount = SecurityService.getPassSecurity(nodeAction.getNode().getNodeIp(),SessionUtil.getCurrentUsername()
                                ,nodeAction.getActionAccounts().get(0).getNodeAccount().getUsername(),accType, null,null,nodeAction.getNode().getCountryCode().getCountryCode()
                                ,selectedAction.getActionId().toString(),pass,mapConfigSecurity);
                        if(!resultGetAccount.getResultStatus() && SecurityService.isNullOrEmpty(resultGetAccount.getResult())){
                            MessageUtil.setErrorMessage(resultGetAccount.getResultMessage());
                            return;
                        }
                        pass = resultGetAccount.getResult();*/

                        //20181023_tudn_end load pass security
                    } catch (Exception e) {
                        pass = nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword();
                        logger.error(e.getMessage(), e);
                    }
                } else {
                    username = "";
                    pass = "";
                }
                nodeObject.setUser(username);
                nodeObject.setPassword(pass);
                // Gan gia tri lenh tac dong
                Map<Long, List<CmdObject>> mapCmdObjs = new LinkedHashMap<>();
                mapCmdObjs.put(1l, impactCmdsObj);
//                lstImpactCmds.add(impactCmdsObj);
                nodeObject.setCmdImpacts(mapCmdObjs);

                cmdNodeObjects.add(nodeObject);
            }
            mesObj.setImpactNodes(cmdNodeObjects);

			/*
            Xu ly du lieu node ghi log
			 */
            String usernameLog;
            String passLog;
            List<CmdNodeObject> cmdLogNodeObjects = new ArrayList<>();
            if (nodeActionLog != null
                    && !nodeActionLog.isEmpty()
                    && commandLogDetails != null
                    && !commandLogDetails.isEmpty()) {
                int i = 0;
                for (ItNodeAction nodeAction : nodeActionLog) {

                    CmdNodeObject nodeObject = new CmdNodeObject();
                    nodeObject.setNodeCode(nodeAction.getNode().getNodeCode());
                    nodeObject.setNodeId(nodeAction.getNode().getNodeId());
                    nodeObject.setNodeIp(nodeAction.getNode().getNodeIp());
                    nodeObject.setNodeName(nodeAction.getNode().getNodeName());
                    nodeObject.setProtocol(commandLogDetails.get(0).getProtocol());
                    nodeObject.setType(Config.ACTION_NODE_TYPE.LOG.value);
                    nodeObject.setEffectIp(nodeAction.getNode().getEffectIp());
                    nodeObject.setOsType(nodeAction.getNode().getOsType());
                    nodeObject.setPort(nodeAction.getNode().getPort());
                    nodeObject.setUrl(nodeAction.getNode().getJdbcUrl());
                    nodeObject.setServerId(nodeAction.getNode().getServerId() + "");
                    nodeObject.setVendorName(nodeAction.getNode().getVendor().getVendorName());
                    nodeObject.setTotalTimeout(120000l);
                    if (nodeAction.getActionAccounts() != null && !nodeAction.getActionAccounts().isEmpty()) {
                        usernameLog = PasswordEncoder.encrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getUsername());
                        try {
                            //20181023_tudn_start load pass security
//                            passLog = PasswordEncoder.encrypt(PassProtector.decrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword(), Config.SALT));
                            try {
                                passLog = PasswordEncoder.encrypt(PassProtector.decrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword(), Config.SALT));
                            } catch (Exception e) {
                                passLog = nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword();
                                logger.error(e.getMessage(), e);
                            }
                            /*String accType = null;
                            if(nodeAction.getActionAccounts().get(0).getNodeAccount() !=null && nodeAction.getActionAccounts().get(0).getNodeAccount().getAccountType()!=null  ) {
                                if (Constant.ACCOUNT_TYPE_SERVER.equalsIgnoreCase(nodeAction.getActionAccounts().get(0).getNodeAccount().getAccountType().toString())) {
                                    accType = Constant.SECURITY_SERVER;
                                } else if (Constant.ACCOUNT_TYPE_DATABASE.equalsIgnoreCase(nodeAction.getActionAccounts().get(0).getNodeAccount().getAccountType().toString())) {
                                    accType = Constant.SECURITY_DATABASE;
                                }
                            }
                            Map<String, String> mapConfigSecurity = SecurityService.getConfigSecurity();
                            ResultGetAccount resultGetAccount = SecurityService.getPassSecurity(nodeAction.getNode().getNodeIp(),SessionUtil.getCurrentUsername()
                                    ,nodeAction.getActionAccounts().get(0).getNodeAccount().getUsername(),accType,null,null,nodeAction.getNode().getCountryCode().getCountryCode()
                                    ,selectedAction.getActionId().toString(),passLog,mapConfigSecurity);
                            if(!resultGetAccount.getResultStatus() && SecurityService.isNullOrEmpty(resultGetAccount.getResult())){
                                MessageUtil.setErrorMessage(resultGetAccount.getResultMessage());
                                return;
                            }
                            passLog = resultGetAccount.getResult();*/
                            //20181023_tudn_end load pass security
                        } catch (Exception e) {
                            passLog = nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword();
                            logger.error(e.getMessage(), e);
                        }

                    } else {
                        usernameLog = "";
                        passLog = "";
                    }
                    nodeObject.setUser(usernameLog);
                    nodeObject.setPassword(passLog);
                    // set command log of node
                    if (i < lstCmdLogsRun.size()) {
                        // Gan gia tri lenh tac dong
                        Map<Long, List<CmdObject>> mapCmdObjs = new LinkedHashMap<>();
                        mapCmdObjs.put(Long.valueOf(i), lstCmdLogsRun.get(i));
                        nodeObject.setCmdImpacts(mapCmdObjs);
                    }
                    cmdLogNodeObjects.add(nodeObject);
                    i++;
                }
            }

            mesObj.setWriteLogNodes(cmdLogNodeObjects);

            mesObj.setFlowRunLogId(actionLogId);
            mesObj.setRunningType(Config.IT_BUSINESS_RUNNING_TYPE.IMPACT.value);

            mesObj.setProtocol(commandDetails.get(0).getProtocol());
            if (commandLogDetails != null && !commandLogDetails.isEmpty()) {
                mesObj.setProtocolLog(commandLogDetails.get(0).getProtocol());
            }

            String encrytedMess = new String(org.apache.commons.codec.binary.Base64.encodeBase64((new Gson()).toJson(mesObj).getBytes("UTF-8")), "UTF-8");

            //20181218_tudn_start tao it cho thi truong
            String socketResult = "NOK";
            SocketClient client = null;
//            SocketClient client = new SocketClient(serverIp, serverPort);
//            client.sendMsg(encrytedMess);
//
//            String socketResult = client.receiveResult();
//            if (socketResult != null && socketResult.contains("NOK")) {
//                throw new Exception(socketResult);
//            }

//            String countryCode = selectedFlowRunAction.getCountryCode() == null ? AamConstants.VNM : selectedFlowRunAction.getCountryCode().getCountryCode();
            loadCountryCode();
            Map<String, Object> filters = new HashMap<>();
            filters.put("countryCode.countryCode-" + MapProcessCountryServiceImpl.EXAC, countryCode);
            filters.put("status", 1L);
            filters.put("typeModule", 2L);

            List<MapProcessCountry> maps = new MapProcessCountryServiceImpl().findList(filters);

            if (maps != null && !maps.isEmpty()) {
                //Sap xep lai maps theo thu tu random
                Collections.shuffle(maps);

                int i = 0;
                for (MapProcessCountry process : maps) {
                    try {
                        serverPort = process.getProcessPort();
                        serverIp = process.getProcessIp();

                        client = new SocketClient(serverIp, serverPort);
                        client.sendMsg(encrytedMess);

                        socketResult = client.receiveResult();
                        if (socketResult != null && socketResult.contains("NOK")) {
                            if (i == maps.size() - 1) {
                                throw new MessageException(socketResult);
                            }
                        } else {
                            logger.info(serverIp);
                            break;
                        }
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                        if (i == maps.size() - 1) {
                            throw new MessageException(MessageUtil.getResourceBundleMessage("error.not.impact.connect.refused"));
                        }
                    }
                    i++;
                }
            }
            //20181218_tudn_end tao it cho thi truong

            /*
            Ghi log tac dong nguoi dung
            */
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.IMPACT,
                        selectedAction.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

//            clear();

            MessageUtil.setInfoMessageFromRes("message.execute.success");
            RequestContext.getCurrentInstance().execute("PF('confirmExcuteAction').hide()");
            RequestContext.getCurrentInstance().execute("PF('dlgAddParamExcute').hide()");
            RequestContext.getCurrentInstance().execute("PF('pollUpdateLog').start()");

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            try {
                Date startTime = new Date();
                new ItActionLogServiceImpl().delete(actionLog);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.DELETE,
                            actionLog.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            if (ex.getMessage() != null) {
                switch (ex.getMessage()) {
                    case "NOK_MAX_SESSION":
                        MessageUtil.setErrorMessageFromRes("message.error.max.session");
                        break;
                }
            }

            MessageUtil.setErrorMessageFromRes("message.execute.fail");
            //20181023_tudn_start load pass security
            RequestContext.getCurrentInstance().execute("PF('pollUpdateLog').stop()");
            //20181023_tudn_end load pass security
        }
    }

    /*20181226_hoangnd_them thi truong_start*/
    public void loadCountryCode() {
        Action action = null;
        if (selectedNode != null && selectedNode.getData() != null)
            action = (Action) selectedNode.getData();
        loadCountryCodeByAction(action);
    }

    public void loadCountryCodeByAction(Action action) {

        if (action != null && action.getAction() != null) {
            if (action.getAction().getActionId().equals(-1L)) {
                countryCode = action.getName();
            } else {
                loadCountryCodeByAction(action.getAction());
            }
        }
    }
    /*20181226_hoangnd_them thi truong_end*/

    private String buildContent(Action actionExport, boolean isImport) {
        if (actionExport != null) {
            List<ActionCommand> actionCommands = actionExport.getActionDetails().get(0).getActionCommands();
            List<CommandDetail> impactCmds = new ArrayList<>();
            List<CommandDetail> writeLogCmds = new ArrayList<>();

            for (ActionCommand actionCmd : actionCommands) {
                if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.IMPACT.value) {
                    impactCmds.add(actionCmd.getCommandDetail());
                } else if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.LOG.value) {
                    CommandDetail logCmdDetail = actionCmd.getCommandDetail();
                    logCmdDetail.setCmdLogOrderRun(actionCmd.getLogOrderRun());
                    writeLogCmds.add(logCmdDetail);
                }
            }

            // get list param
            final Map<String, String> mapParams = getImpactParams(impactCmds);


            StringBuilder strData = new StringBuilder();
            strData.append(MessageUtil.getResourceBundleMessage("label.dt.export.action.name") + ": ").append(actionExport.getName()).append("\r\n")
                    .append(MessageUtil.getResourceBundleMessage("label.dt.export.action.description") + ": ").append(actionExport.getDescription()).append("\r\n")
                    .append(MessageUtil.getResourceBundleMessage("label.dt.export.action.id") + ": ").append(actionExport.getActionId()).append("\r\n");

            strData.append("-------------- " + MessageUtil.getResourceBundleMessage("label.dt.export.command.list") + " ----------- \r\n");
            if (!isImport) {
                for (CommandDetail cmd : impactCmds) {
                    strData.append(MessageUtil.getResourceBundleMessage("label.dt.export.command.name") + ": ").append(cmd.getCommandName())
                            .append(" . " + MessageUtil.getResourceBundleMessage("label.dt.export.command.value") + ": ").append(cmd.getCommandTelnetParser().getCmd()).append("\r\n");
                }
            } else if (impactCmdsObj != null) {
                for (CmdObject cmd : impactCmdsObj) {
                    strData.append(MessageUtil.getResourceBundleMessage("label.dt.export.command.value") + ": ").append(cmd.getCommand()).append("\r\n");
                }
            }


            if (!isImport) {
                strData.append("-------------- " + MessageUtil.getResourceBundleMessage("label.dt.export.param.list") + " ----------- \r\n");
                String params = "";
                for (Map.Entry<String, String> entry : mapParams.entrySet()) {
                    params += entry.getValue() + ";";
//                    strData.append(entry.getValue()).append(",");
                }
                if (params.endsWith(";")) {
                    params = params.substring(0, params.length() - 1);
                }
                strData.append(params).append("\r\n");

            } else if (writeLogCmdsObj != null) {
                strData.append("-------------- " + MessageUtil.getResourceBundleMessage("label.dt.export.command.log") + " ----------- \r\n");
                for (CmdObject cmd : writeLogCmdsObj) {
                    strData.append(MessageUtil.getResourceBundleMessage("label.dt.export.command.value") + ": ").append(cmd.getCommand()).append("\r\n");
                }
            }

            logger.info(strData.toString());
            return strData.toString();

        } else {
            return null;
        }
    }

    public void exportTemplate(boolean isImport) {
        //tudn_start fix download template
        if (selectedAction.getActionDetails() != null && selectedAction.getActionDetails().size() > 0) {
            String content = buildContent(selectedAction, isImport);
            if (content != null) {
                FacesContext fc = FacesContext.getCurrentInstance();
                HttpServletResponse response = (HttpServletResponse) fc.getExternalContext().getResponse();
                response.reset();
                response.setContentType("text/plain");
//            response.setContentLength(content.length());
                String attachmentName = "attachment; filename=\"" + selectedAction.getName().replaceAll("(\r\n|\n)+", "").replaceAll(" +", "_") + ".txt \"";
                response.setHeader("Content-Disposition", attachmentName);
//			response.setHeader("Content-Type", "text/plain; utf-8");
                response.setCharacterEncoding("UTF-8");
                try {
                    OutputStream output = response.getOutputStream();
                    output.write(content.getBytes());
                    output.flush();
                    output.close();
                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                }

                fc.responseComplete();
            } else {
                MessageUtil.setErrorMessageFromRes("");
            }
        } else {
            MessageUtil.setErrorMessageFromRes("error.leaf.action");
        }
        //tudn_start fix download template
    }


    public void handleImportParams(FileUploadEvent event) {
        impactCmdsObj = new ArrayList<>();
        writeLogCmdsObj = new ArrayList<>();
        isImportSucess = false;
        /*20180725_hoangnd_fix bug import param_start*/
        String ketStartGetParam = MessageUtil.getResourceBundleMessage("key.start.get.param.value");
        /*20180725_hoangnd_fix bug import param_end*/
        if (event != null) {
            try {
                InputStream input = event.getFile().getInputstream();

                BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(input, "UTF-8"));
                String line;
                List<String> datas = new ArrayList<>();
                while ((line = bufferedReader.readLine()) != null) {
                    datas.add(line);
                }

                int idxStart = 0;
                for (String data : datas) {
                    /*20180725_hoangnd_fix bug import param_start*/
                    if (ketStartGetParam.equalsIgnoreCase(data.trim())) {
                    /*20180725_hoangnd_fix bug import param_end*/
                        break;
                    } else {
                        idxStart++;
                    }
                }

                boolean checkFillParam = true;
                Map<String, List<String>> mapParamVals = new HashedMap();
                List<String> paramCodes;
                Map<Integer, String> mapParamCodes = new HashedMap();
                if (idxStart + 1 < datas.size()) {
                    paramCodes = Arrays.asList(datas.get(idxStart + 1).trim().split(";"));
                    if (paramCodes != null) {
                        for (int i = 0; i < paramCodes.size(); i++) {
                            mapParamVals.put(paramCodes.get(i), new ArrayList<>());
                            mapParamCodes.put(i, paramCodes.get(i));
                        }
                    }
                }

                List<String> paramVals;
                for (int i = idxStart + 2; i < datas.size(); i++) {
                    try {
                        paramVals = Arrays.asList(datas.get(i).trim().split(";"));
                        if (paramVals == null || paramVals.isEmpty()) {
                            checkFillParam = false;
                            break;
                        }

                        for (int j = 0; j < paramVals.size(); j++) {
                            mapParamVals.get(mapParamCodes.get(j)).add(paramVals.get(j));
                        }

                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                List<ActionCommand> actionCommands = selectedAction.getActionDetails().get(0).getActionCommands();
                List<CommandDetail> impactCmds = new ArrayList<>();
                List<CommandDetail> writeLogCmds = new ArrayList<>();

                for (ActionCommand actionCmd : actionCommands) {
                    if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.IMPACT.value) {
                        impactCmds.add(actionCmd.getCommandDetail());
                    } else if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.LOG.value) {
                        CommandDetail logCmdDetail = actionCmd.getCommandDetail();
                        logCmdDetail.setCmdLogOrderRun(actionCmd.getLogOrderRun());
                        writeLogCmds.add(logCmdDetail);
                    }
                }

                // get params from template
                Map<String, String> paramsTemplate = getImpactParams(impactCmds);
                for (Map.Entry<String, String> entry : paramsTemplate.entrySet()) {
                    if (mapParamVals.get(entry.getKey()) == null || mapParamVals.get(entry.getKey()).isEmpty()) {
                        checkFillParam = false;
                        break;
                    }
                }

                if (!checkFillParam) {
                    MessageUtil.setErrorMessageFromRes("label.err.fillout.param");
                    return;
                }

                // build command
                impactCmdsObj = buildCmdObj(impactCmds, null, false, mapParamVals);
                writeLogCmdsObj = buildCmdObj(writeLogCmds, null, false, mapParamVals);

                for (CmdObject cmd : impactCmdsObj) {
                    if (cmd.getCommand().contains("@{")) {
                        impactCmdsObj = new ArrayList<>();
                        writeLogCmdsObj = new ArrayList<>();
                        isImportSucess = false;
                        MessageUtil.setInfoMessageFromRes("label.error.notFillAllData");
                        return;
                    }
                }

                if (impactCmdsObj != null && !impactCmdsObj.isEmpty()) {
                    isImportSucess = true;
                    MessageUtil.setInfoMessageFromRes("label.inform.upload.sucess");
                } else {
                    isImportSucess = false;
                    MessageUtil.setErrorMessageFromRes("label.inform.err.import.param");
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    private Map<String, String> getImpactParams(List<CommandDetail> impactCmds) {
        if (impactCmds != null) {

            // get list param
            Map<String, String> mapParams = new HashedMap();
            for (CommandDetail cmd : impactCmds) {
                if (cmd.getParamInputs() != null && !cmd.getParamInputs().isEmpty()) {
                    List<ParamInput> paramInputs = cmd.getParamInputs();
                    for (ParamInput p : paramInputs) {
                        mapParams.put(p.getParamCode(), p.getParamCode());
                    }
                }
            }
            return mapParams;
        } else {
            return null;
        }
    }

    /*
    Lay ra thong tin node mang tac dong cua dau viec
     */
    public List<ItNodeAction> getNodeAction(Long type, Action action, Long actionId) {
        try {
            Map<String, Object> filters = new HashedMap();
            filters.put("actionId", action == null ? actionId : action.getActionId());
            filters.put("type", type);

            List<ItNodeAction> nodeActions = new ItNodeActionServiceImpl().findList(filters);
            if (nodeActions != null && !nodeActions.isEmpty()) {
                return nodeActions;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    /*
    Kiem tra xem node mang da duoc gan account hay chua
     */
    private boolean valAccount(List<ItNodeAction> nodeActions) {
        if (nodeActions != null) {
            for (ItNodeAction nodeAction : nodeActions) {
                if (nodeAction.getActionAccounts() == null || nodeAction.getActionAccounts().isEmpty()) {
                    return false;
                }
            }
            return true;
        } else {
            return false;
        }
    }

    private String buildCmd(List<CommandDetail> commandDetails, Map<String, ParamInput> mapParamValue, boolean isCmdLog) {
        StringBuilder cmdRuns = new StringBuilder();
        String cmdRun = null;
        try {
            int idx = 0;
            if (Config.PROTOCOL_EXCHANGE.equals(commandDetails.get(0).getProtocol())) {
                for (CommandDetail cmd : commandDetails) {
                    cmdRun = cmd.getCommandTelnetParser().getCmd();
                    for (ParamInput param : cmd.getParamInputs()) {
                        cmdRun = cmdRun.replace("@{" + param.getParamCode() + "}",
                                ((mapParamValue != null && mapParamValue.get(param.getParamCode()) != null) ? mapParamValue.get(param.getParamCode()).getParamValue() + ";" : ";"));
                    }
                    cmdRun = cmdRun.endsWith(";") ? cmdRun.substring(0, cmdRun.length() - 1) : cmdRun;
                    if (idx == commandDetails.size() - 1) {
                        cmdRuns.append(cmdRun);
                        if (isCmdLog) {
                            cmdRuns.append("||").append(cmd.getCmdLogOrderRun());
                        }
                    } else {
                        cmdRuns.append(cmdRun);
                        if (isCmdLog) {
                            cmdRuns.append("||").append(cmd.getCmdLogOrderRun());
                        }
                        cmdRuns.append("|||");
                    }
                    idx++;
                }
            } else {
                for (CommandDetail cmd : commandDetails) {
                    cmdRun = cmd.getCommandTelnetParser().getCmd();
                    for (ParamInput param : cmd.getParamInputs()) {
                        cmdRun = cmdRun.replace("@{" + param.getParamCode() + "}",
                                ((mapParamValue != null && mapParamValue.get(param.getParamCode()) != null) ? mapParamValue.get(param.getParamCode()).getParamValue() : ""));
                    }

                    if (idx == commandDetails.size() - 1) {
                        cmdRuns.append(cmdRun);
                        if (isCmdLog) {
                            cmdRuns.append("||").append(cmd.getCmdLogOrderRun());
                        }
                    } else {
                        cmdRuns.append(cmdRun);
                        if (isCmdLog) {
                            cmdRuns.append("||").append(cmd.getCmdLogOrderRun());
                        }
                        cmdRuns.append("|||");
                    }
                    idx++;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return cmdRuns.toString();
    }

    public List<CmdObject> buildCmdObj(List<CommandDetail> commandDetails,
                                       Map<String, ParamInput> mapParamValue, boolean isCmdLog,
                                       Map<String, List<String>> mapParamInport) {
        String cmdRun = null;
        List<CmdObject> cmds = new ArrayList<>();
        try {
//            if (Config.PROTOCOL_EXCHANGE.equals(commandDetails.get(0).getProtocol())) {
//                if (mapParamInport == null) {
//                    for (CommandDetail cmd : commandDetails) {
//                        cmdRun = cmd.getCommandTelnetParser().getCmd();
//                        for (ParamInput param : cmd.getParamInputs()) {
//                            cmdRun = cmdRun.replace("@{" + param.getParamCode() + "}",
//                                    ((mapParamValue != null && mapParamValue.get(param.getParamCode()) != null) ? mapParamValue.get(param.getParamCode()).getParamValue() + ";" : ";"));
//                        }
//                        cmdRun = cmdRun.endsWith(";") ? cmdRun.substring(0, cmdRun.length() - 1) : cmdRun;
//
//                        CmdObject cmdObj = new CmdObject();
//                        cmdObj.setCommand(cmdRun);
//                        cmdObj.setCmdDetailId(cmd.getCommandDetailId());
//                        if (isCmdLog) {
//                            cmdObj.setWriteLogOrder(cmd.getCmdLogOrderRun());
//                        }
//                        cmds.add(cmdObj);
//
//                    } // end loop for command
//
//                } else {
//
//                    // build command import file
//                    for (CommandDetail cmd : commandDetails) {
//
//                        Long cmdClone = getMaxCmdClone(cmd, mapParamInport);
//                        for (int i = 0; i < cmdClone; i++) {
//                            cmdRun = cmd.getCommandTelnetParser().getCmd();
//                            for (ParamInput param : cmd.getParamInputs()) {
//                                int idx = (mapParamInport.get(param.getParamCode()).size() > i) ? i : mapParamInport.get(param.getParamCode()).size() - 1;
//                                cmdRun = cmdRun.replace("@{" + param.getParamCode() + "}", mapParamInport.get(param.getParamCode()).get(idx).trim() + ";");
//                            }
//                            cmdRun = cmdRun.endsWith(";") ? cmdRun.substring(0, cmdRun.length() - 1) : cmdRun;
//
//                            CmdObject cmdObj = new CmdObject();
//                            cmdObj.setCommand(cmdRun);
//                            cmdObj.setCmdDetailId(cmd.getCommandDetailId());
//                            if (isCmdLog) {
//                                cmdObj.setWriteLogOrder(cmd.getCmdLogOrderRun());
//                            }
//                            cmds.add(cmdObj);
//                        }
//                    }
//                }
//
//            } else {
            // build command
            long idxCmd = 0;
            if (mapParamInport == null) {
                mapParamInport = new HashedMap();
                for (Map.Entry<String, ParamInput> entry : mapParamValue.entrySet()) {
                    if (entry.getValue().getParamValue() != null && !entry.getValue().getParamValue().trim().isEmpty()) {
                        List<String> paramValues = Arrays.asList(entry.getValue().getParamValue().trim().split(";"));
                        mapParamInport.put(entry.getKey(), paramValues);
                    }
                }
//                for (CommandDetail cmd : commandDetails) {
//                    cmdRun = cmd.getCommandTelnetParser().getCmd();
//                    for (ParamInput param : cmd.getParamInputs()) {
//                        cmdRun = cmdRun.replace("@{" + param.getParamCode() + "}",
//                                ((mapParamValue != null && mapParamValue.get(param.getParamCode()) != null) ? mapParamValue.get(param.getParamCode()).getParamValue() : ""));
//                    }
//
//                    CmdObject cmdObj = new CmdObject();
//                    cmdObj.setCommand(cmdRun);
//                    cmdObj.setCmdDetailId(cmd.getCommandDetailId());
//                    cmdObj.setCmdOrder(idxCmd);
//                    if (isCmdLog) {
//                        cmdObj.setWriteLogOrder(cmd.getCmdLogOrderRun());
//                    }
//                    cmds.add(cmdObj);
//                    idxCmd++;
//                } // end loop for command detail

            }

            // build command import file
            for (CommandDetail cmd : commandDetails) {

                Long cmdClone = getMaxCmdClone(cmd, mapParamInport);
//                if (mapParamInport.isEmpty() && cmdClone == 0) {
//                    cmdClone = 1l;
//                }
                if (cmdClone == 0) {
                    cmdClone = 1l;
                }
                for (int i = 0; i < cmdClone; i++) {
                    cmdRun = cmd.getCommandTelnetParser().getCmd();
                    for (ParamInput param : cmd.getParamInputs()) {
                        int idx = (mapParamInport.get(param.getParamCode()).size() > i) ? i : mapParamInport.get(param.getParamCode()).size() - 1;
                        cmdRun = cmdRun.replace("@{" + param.getParamCode() + "}", mapParamInport.get(param.getParamCode()).get(idx).trim());
                    }

                    CmdObject cmdObj = new CmdObject();
                    cmdObj.setCommand(cmdRun);
                    cmdObj.setCmdDetailId(cmd.getCommandDetailId());
                    cmdObj.setCmdOrder(idxCmd);
                    if (isCmdLog) {
                        cmdObj.setWriteLogOrder(cmd.getCmdLogOrderRun());
                    }
                    cmds.add(cmdObj);
                    idxCmd++;
                }
            }

//            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return cmds;
    }

    private Long getMaxCmdClone(CommandDetail cmd, Map<String, List<String>> mapParams) {
        long maxLeng = 0;
        List<ParamInput> paramInputs = cmd.getParamInputs();
        if (paramInputs != null && !paramInputs.isEmpty()) {
            for (ParamInput p : paramInputs) {
                if (mapParams.get(p.getParamCode()).size() > maxLeng) {
                    maxLeng = mapParams.get(p.getParamCode()).size();
                }
            }
        }
        return maxLeng;
    }

    private Node getNodeByType(Long type, Action action) {
        if (action != null && action.getNodeActions() != null) {
            for (ItNodeAction nodeAction : action.getNodeActions()) {
                if (nodeAction.getType().equals(type)) {
                    return nodeAction.getNode();
                }
            }
        }
        return null;
    }

    public void prepareShowCmdInAction() {
        selectedAction = new Action();
        try {
            selectedAction = (Action) selectedNode.getData();
            selectedAction = actionServiceImpl.findById(selectedAction.getActionId());
            if (selectedAction.getActionDetails() != null
                    && !selectedAction.getActionDetails().isEmpty()) {
                RequestContext.getCurrentInstance().execute("PF('dlgShowCmdsInAction').show()");
            } else {
                MessageUtil.setErrorMessageFromRes("datatable.empty");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void onclickAction() {
        isImportSucess = false;
        selectedAction = new Action();
        commandDetails = new ArrayList<>();
        commandLogDetails = new ArrayList<>();
        impactCmdsObj = new ArrayList<>();
        writeLogCmdsObj = new ArrayList<>();
        mapParamVals = new HashedMap();
        commandLogList = new ArrayList<>();
        openBlockingType = 0;
        rescueCycle = 0;
        numOfThread = 0;
        try {
            selectedAction = (Action) selectedNode.getData();

            if (selectedAction.getActionDetails() == null
                    || selectedAction.getActionDetails().isEmpty()) {
                MessageUtil.setErrorMessageFromRes("message.err.no.comamnd.action");
            } else if (selectedAction.getNodeActions() == null ||
                    selectedAction.getNodeActions().isEmpty()) {
                MessageUtil.setErrorMessageFromRes("message.err.action.no.node.impact");
            } else {
                List<ActionCommand> actionCommands = selectedAction.getActionDetails().get(0).getActionCommands();
                if (actionCommands == null || actionCommands.isEmpty()) {
                    MessageUtil.setErrorMessageFromRes("label.error.no.command");
                    return;
                }
                for (ActionCommand actionCmd : actionCommands) {
                    if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.IMPACT.value) {
                        commandDetails.add(actionCmd.getCommandDetail());
                    } else if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.LOG.value) {
                        CommandDetail logCmdDetail = actionCmd.getCommandDetail();
                        logCmdDetail.setCmdLogOrderRun(actionCmd.getLogOrderRun());
                        commandLogDetails.add(actionCmd.getCommandDetail());
                    }
                }

                if (!valNodeAccount()) {
                    return;
                } else if (!valParamCodesLog()) {
                    MessageUtil.setErrorMessageFromRes("msg.err.get.paramlog");
                } else {

                    // if execute manual
                    mapParamVals = new HashMap<>();
                    for (CommandDetail cmdDetail : commandDetails) {
                        if (cmdDetail.getParamInputs() != null) {
                            for (ParamInput param : cmdDetail.getParamInputs()) {
                                if (mapParamVals.get(param.getParamCode()) == null
                                        || (mapParamVals.get(param.getParamCode()) != null && param.getParamDefault() != null)) {
                                    if (param.getParamDefault() != null) {
                                        param.setParamValue(param.getParamDefault());
                                    }
                                    mapParamVals.put(param.getParamCode(), param);
                                }
                            }
                        }
                    }
                }

                // set list command objects
                if (commandDetails != null && !commandDetails.isEmpty()) {
                    long count = 0;
                    for (CommandDetail cmd : commandDetails) {
                        CmdObject cmdObject = new CmdObject();
                        cmdObject.setCmdOrder(count);
                        cmdObject.setCmdDetailId(cmd.getCommandDetailId());
                        cmdObject.setCommand(cmd.getCommandTelnetParser().getCmd());
                        impactCmdsObj.add(cmdObject);
                        count++;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void preAddActionToTemplate() {

        if (selectedNode != null) {
            try {
                selectedAction = (Action) selectedNode.getData();
                selectedAction = actionServiceImpl.findById(selectedAction.getActionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    /*
     * Tao cay danh muc action
     */
    public void createTree() {
        if (new SessionUtil().isItBusinessAdmin()) {
            Action root;
            try {
                root = actionServiceImpl.findById(ROOT_TREE_ID);
                if (root != null) {
                    rootNode2 = new DefaultTreeNode("action", "Root", null);

                    LinkedHashMap<String, String> orders = new LinkedHashMap<>();
                    orders.put("name", "ASC");

                    //List<Action> lstAllData = null;
                    try {
                        listActionMD = (ArrayList<Action>) actionServiceImpl.findList(null, orders);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    buildTree(root, rootNode2, listActionMD, 1);
                    rootNode2.setExpanded(true);
                }
            } catch (SysException | AppException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void createTree2() {
        ItUsers users = null;
        try {
            if (new SessionUtil().isItBusinessUserGroup()) {
                /*20190115_hoangnd_them thi truong_start*/
                Map<String, Object> filter3 = new HashMap<>();
                filter3.put("userName", SessionWrapper.getCurrentUsername());
                users = itUsersServices.findList(filter3).get(0);
                List<Action> root = new ArrayList<Action>();
                Map<String, Object> filter = new HashMap<>();
                filter.put("id.userId", users.getUserId());
                List<ItUserBusinessGroup> itUserBusinessGroupList = itUserBusGroupService.findList(filter);
                List<Long> businessIds = new ArrayList<>();
                for (int i = 0; i < itUserBusinessGroupList.size(); i++) {
                    //hoangnd fix bug duplicate node thi truong start
                    if (itUserBusinessGroupList.get(i).getBusinessGroup().getAction().getAction() != null
                            && !root.contains(itUserBusinessGroupList.get(i).getBusinessGroup().getAction().getAction())) {
                        root.add(itUserBusinessGroupList.get(i).getBusinessGroup().getAction().getAction());
                    }
                    //hoangnd fix bug duplicate node thi truong end
                    businessIds.add(itUserBusinessGroupList.get(i).getBusinessGroup().getAction().getActionId());
                }
                if (root.size() > 0) {
                    rootNode2 = new DefaultTreeNode("action", "Root", null);

                    // Action -1
                    Action rootParent;
                    rootParent = actionServiceImpl.findById(ROOT_TREE_ID);
                    listActionMD.add(rootParent);
                    TreeNode newNode = new DefaultTreeNode(GRAND_NODE, rootParent, rootNode2);
                    newNode.setExpanded(true);
                    for (int j = 0; j < root.size(); j++) {
                        listActionMD.add(root.get(j));
                        buildTree2(root.get(j), newNode, businessIds);
                    }
                }
            } else if (new SessionUtil().isItBusinessUserNormal()) {
                Map<String, Object> filter3 = new HashMap<>();
                filter3.put("userName", SessionWrapper.getCurrentUsername());
                System.out.println(SessionWrapper.getCurrentUsername());
                users = itUsersServices.findList(filter3).get(0);


                Map<String, Object> filter2 = new HashMap<>();
                filter2.put("id.userId", users.getUserId());
                List<ItUserAction> itUserActionList = itUserActionService.findList(filter2);
                lstParentId = new ArrayList<>();
                lstParentNode = new ArrayList<>();
                if (itUserActionList != null && itUserActionList.size() > 0) {
                    Action root;
                    root = actionServiceImpl.findById(ROOT_TREE_ID);
                    if (root != null) {
                        rootNode2 = new DefaultTreeNode("action", "Root", null);

                        for (int i = 0; i < itUserActionList.size(); i++) {
                            int note = 0;
                            for (int j = 0; j < lstParentId.size(); j++) {
                                if (lstParentId.get(j) == itUserActionList.get(i).getActionUser().getAction().getActionId()) {
                                    buildTreeFromChild(itUserActionList.get(i).getActionUser(), null, j);
                                    note = 1;
                                }
                            }
                            if (note == 0) {
                                buildTreeFromChild(itUserActionList.get(i).getActionUser(), null, -1);
                            }
                        }
                        ArrayList<Action> lstTreeNode = new ArrayList<>();

                        Set<Action> set = new HashSet<>();
                        set.addAll(listActionMD);
                        listActionMD = new ArrayList<Action>(set);

                        lstTreeNode.addAll(listActionMD);
                        if (lstTreeNode.size() > 0) {
                            buildTree(root, rootNode2, lstTreeNode, 1);
                            rootNode2.setExpanded(true);
                        }

                    }

					/*Action rootParent;
                    rootParent = actionServiceImpl.findById(ROOT_TREE_ID);
					listActionMD.add(rootParent);
					TreeNode newNode = null;
					newNode = new DefaultTreeNode(PARENT_NODE, rootParent, rootNode2);
					newNode.setExpanded(true);*/


                }
            }
        } catch (SysException | AppException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /*
     * Ham tao cay theo nut
     */
    public TreeNode buildTree(Action treeObj, TreeNode parent, List<Action> lstAllData, int level) {
        TreeNode newNode = null;
        List<Action> childNode = new ArrayList<>();
        try {
            /*if(resultSearch.contains(treeObj)){
                childNode = treeObj.getActions();
			}
			else{*/
            childNode = getLstChid(lstAllData, treeObj.getActionId());
//			}

            /*20181225_hoangnd_them thi truong_start*/
            if (childNode != null && childNode.size() > 0) {
                if (level == 1) {
                    newNode = new DefaultTreeNode(GRAND_NODE, treeObj, parent);
                    newNode.setExpanded(true);
                } else if (level == 2) {
                    newNode = new DefaultTreeNode(COUNTRY_NODE, treeObj, parent);
                } else if (level == 5) {
                    newNode = new DefaultTreeNode(SUB_PARENT_NODE, treeObj, parent);
                } else {
                    newNode = new DefaultTreeNode(PARENT_NODE, treeObj, parent);
                }
                for (Action tt : childNode) {
                    buildTree(tt, newNode, lstAllData, (level + 1));
                }
            } else if (level == 5) {
                newNode = new DefaultTreeNode(SUB_PARENT_NODE, treeObj, parent);
            } else if (level <= 4) {
                if (level == 1) {
                    newNode = new DefaultTreeNode(GRAND_NODE, treeObj, parent);
                    newNode.setExpanded(true);
                } else if (level == 2) {
                    newNode = new DefaultTreeNode(COUNTRY_NODE, treeObj, parent);
                } else {
                    newNode = new DefaultTreeNode(PARENT_NODE, treeObj, parent);
                }
            } else {
                newNode = new DefaultTreeNode(CHILD_NODE, treeObj, parent);
            }
            /*20181225_hoangnd_them thi truong_end*/
            if (treeObj.isExpanded())
                newNode.setExpanded(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return newNode;
    }

    /*20181225_hoangnd_them thi truong_start*/
    public TreeNode buildTree2(Action treeObj, TreeNode parent, List<Long> businessIds) {
        TreeNode newNode = null;
        try {
            List<Action> childNode = treeObj.getActions();

            Integer level = getNodeLevel(parent);
            if (level != null) {
                if (level == 1) {
                    newNode = new DefaultTreeNode(COUNTRY_NODE, treeObj, parent);
                } else if (level == 2) {
                    for (Long businessId : businessIds) {
                        if (businessId.equals(treeObj.getActionId())) {
                            newNode = new DefaultTreeNode(PARENT_NODE, treeObj, parent);
                        }
                    }
                } else if (level == 3) {
                    newNode = new DefaultTreeNode(PARENT_NODE, treeObj, parent);
                } else if (level == 4) {
                    newNode = new DefaultTreeNode(SUB_PARENT_NODE, treeObj, parent);
                } else {
                    newNode = new DefaultTreeNode(CHILD_NODE, treeObj, parent);
                }
                if (childNode != null && childNode.size() > 0) {
                    for (Action tt : childNode) {
                        listActionMD.add(tt);
                        buildTree2(tt, newNode, businessIds);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return newNode;
    }
    /*20181225_hoangnd_them thi truong_end*/

    public TreeNode buildTreeFromChild(Action treeObj, Action childNode, int parentId) {
        TreeNode newNode = null;
        try {
            if (treeObj.getActionId() == -1 && dem == 0) {
                listActionMD.add(treeObj);
                dem++;
            } else if (treeObj.getActionId() != -1) {
                listActionMD.add(treeObj);
            }

            Action parentNode = treeObj.getAction();
            if (parentId == -1) {
                if (parentNode != null) {
                    if (treeObj.getTreeLevel() != null && treeObj.getTreeLevel() <= 4) {
                        newNode = (treeObj.getTreeLevel() == 4)
                                ? new DefaultTreeNode(SUB_PARENT_NODE, treeObj, buildTreeFromChild(parentNode, childNode, parentId))
                                : new DefaultTreeNode(PARENT_NODE, treeObj, buildTreeFromChild(parentNode, childNode, parentId));
                        if (treeObj.getTreeLevel() == 4) {
                            lstParentId.add(treeObj.getActionId());
                            lstParentNode.add(newNode);
                        }
                    } else
                        newNode = new DefaultTreeNode(CHILD_NODE, treeObj, buildTreeFromChild(parentNode, childNode, parentId));
                }
            } else {
                newNode = new DefaultTreeNode(CHILD_NODE, treeObj, lstParentNode.get(parentId));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return newNode;
    }

    private List<Action> getLstChid(List<Action> lstAction, Long parentId) {
        List<Action> lstSubAction = new ArrayList<>();
        try {
            for (Iterator<Action> iterator = lstAction.iterator(); iterator.hasNext(); ) {
                Action ac = iterator.next();
                if ((ac.getAction() != null) && (ac.getAction().getActionId() == parentId)) {
                    lstSubAction.add(ac);
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        Collections.sort(lstSubAction, new Comparator<Action>() {

            @Override
            public int compare(Action o1, Action o2) {
                String preName1 = o1.getName().substring(0, Math.max(o1.getName().indexOf(" "), 0));
                String preName2 = o2.getName().substring(0, Math.max(o2.getName().indexOf(" "), 0));

                String name1 = o1.getName().substring(o1.getName().indexOf(" ") + 1);
                String name2 = o2.getName().substring(o2.getName().indexOf(" ") + 1);

                String[] tmpPre1s = preName1.split("[,.]", -1);
                String[] tmpPre2s = preName2.split("[,.]", -1);
                int r = comp(tmpPre1s, tmpPre2s);
                if (r == 0) {
                    return name1.trim().compareTo(name2.trim());
                }
                return r;
            }

            public int comp(String[] as, String[] bs) {
                int min = Math.min(as.length, bs.length);
                if (as.length < bs.length)
                    return 1;
                if (as.length > bs.length)
                    return -1;
                if (min == 0)
                    return 0;
                int i = Math.min(0, min - 1);
                if (as[i].equals(bs[i])) {
                    as = ArrayUtils.remove(as, i);
                    bs = ArrayUtils.remove(bs, i);
                    return comp(as, bs);
                }
                return as[i].compareTo(bs[i]);
            }
        });
        return lstSubAction;
    }

    public void prepareCloneAction() {
        logger.info("vao prepare clone");
        if (selectedNode != null) {
            try {

                if (selectedNode.getParent() == null) {
                    logger.error("parent null cmnr !");
                }

                logger.info("chuan bi du lieu");
                isClone = true;
                isEdit = false;
                Action action = (Action) selectedNode.getData();
                action = actionServiceImpl.findById(action.getActionId());

                actionNameToClone = action.getName();

                insertNode = new Action();
                insertNode.setName("");
                insertNode.setDescription("");
                insertNode.setActionId(null);
                insertNode.setActionDetails(action.getActionDetails());
                insertNode.setActionOfFlows(null);
                insertNode.setActions(null);
                insertNode.setAction(action.getAction());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private void clear() {
        isImportSucess = false;
        selectedAction = null;
        writeLogCmdsObj = new ArrayList<>();
        impactCmdsObj = new ArrayList<>();
        nodeAccounts = new ArrayList<>();
        commandLogDetails = new ArrayList<>();
        mapParamVals = new HashedMap();
        nodeAccountSelected = null;
        commandDetails = new ArrayList<>();
        actionDetail = null;
        selectedActionDetail = null;
        selectedCmdDetail = null;
        selectedCmdTelnetParser = null;
        selectedActionCommand = null;

        stationDetailStatus = null;
        isEditActionDetail = false;
        selectedActionLog = null;
        lstActionCommand = new LinkedList<>();
    }

    /**
     * check node is show command set group code
     *
     * @return
     */
    public boolean isShowSetGroupCode() {
        boolean check = false;
        if (selectedNode != null) {
            Action action = (Action) selectedNode.getData();
            if (action.getTreeLevel() == 4) {
                check = true;
            }
        }
        return check;
    }

    public boolean checkIsAdmin(long menu) {
        if (new SessionUtil().isItBusinessAdmin() || new SessionUtil().isItBusinessUserGroup()) {
            return true;
        } else if (new SessionUtil().isItBusinessUserNormal()) {
            if (menu == 4l || menu == 8l) {
                return true;
            }
        }
        return false;
    }

	/*public boolean checkIsDefault(){
        Action action = new Action();
		action = (Action) selectedNode.getData();
		if(action.getStatus() != 1 && checkIsAdmin() ==true){
			return true;
		}else{
			return false;
		}
	}

	public boolean checkIsVerify(){
		Action action = new Action();
		action = (Action) selectedNode.getData();
		if(action.getStatus() == 1l){
			return true;
		}else{
			return false;
		}

	}*/

    public void prepareVerifyVoffice() {
        verifyActtion = new Action();
        verifyActtion = (Action) selectedNode.getData();
        System.out.println(verifyActtion);

    }

    public void updateStatusVoffice() {
        try {
            Date startTime = new Date();
            if (verifyActtion != null) {
                ItVoffice voffice = new ItVoffice();
                Long status = voffice.signVoffice(verifyActtion, "222222a@");
                System.out.println(status);
                verifyActtion.setStatus(2l);
                new ActionServiceImpl().saveOrUpdate(verifyActtion);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.UPDATE,
                            verifyActtion.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
            }
            MessageUtil.setInfoMessageFromRes("info.save.success");
        } catch (AppException e) {
            MessageUtil.setInfoMessageFromRes("have.some.error");
            logger.error(e.getMessage(), e);
        }
    }

    public void prepareVerifyAction() {
        verifyActtion = new Action();
        verifyActtion = (Action) selectedNode.getData();
        System.out.println(verifyActtion);

    }

    public void updateStatusAction() {
        Date startTime = new Date();
        if (verifyActtion != null) {
            verifyActtion.setStatus(1l);
            try {
                new ActionServiceImpl().saveOrUpdate(verifyActtion);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.UPDATE,
                            verifyActtion.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
                MessageUtil.setInfoMessageFromRes("info.save.success");
            } catch (AppException e) {
                MessageUtil.setErrorMessageFromRes("have.some.error");
                logger.error(e.getMessage(), e);
            }
        } else {
            MessageUtil.setErrorMessageFromRes("have.some.error");
        }
    }

    /*20190109_hoangnd_them thi truong_start*/
    public void prepareEdit(int lv) {

        treeNodeLevel = lv;
    /*20190109_hoangnd_them thi truong_end*/
        isEdit = true;
        if (selectedNode != null) {
            try {
                Action action = (Action) selectedNode.getData();
                action = actionServiceImpl.findById(action.getActionId());
                insertNode = new Action();
                insertNode.setName(action.getName());
                insertNode.setDescription(action.getDescription());
                insertNode.setActionId(action.getActionId());
                insertNode.setActionDetails(action.getActionDetails());
                insertNode.setActionOfFlows(action.getActionOfFlows());
                insertNode.setActions(action.getActions());
                insertNode.setAction(action.getAction());

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.error(">>>>>>>> ERROR selectedNode is null");
        }
    }

    public void prepareDelAction() {
        boolean checkShowDlg = true;
        try {
            switch (selectedNode.getType()) {
                /*20190110_hoangnd_them thi truong_start*/
                case GRAND_NODE:
                case COUNTRY_NODE:
                /*20190110_hoangnd_them thi truong_end*/
                case PARENT_NODE:
                case SUB_PARENT_NODE:
                    if (selectedNode.getChildCount() > 0) {
                        checkShowDlg = false;
                        MessageUtil.setErrorMessageFromRes("label.err.action.exist.child");
                    }
                    break;
                case CHILD_NODE:
                    Action actionDel = (Action) selectedNode.getData();
                    actionDel = new ActionServiceImpl().findById(actionDel.getActionId());
                    if ((actionDel.getActionDetails() != null && !actionDel.getActionDetails().isEmpty())
                            || actionDel.getActionOfFlows() != null && !actionDel.getActionOfFlows().isEmpty()) {
                        checkShowDlg = false;
                        MessageUtil.setErrorMessageFromRes("label.error.action.have.detail");
                    }
                    break;

                default:
                    break;
            }
            if (checkShowDlg) {
                RequestContext.getCurrentInstance().execute("PF('confDlgDelAction').show()");
            }
        } catch (Exception e) {
//			checkShowDlg = false;
            logger.error(e.getMessage(), e);
        }

    }

    public void preEditOpenBlockGroup() {
        if (selectedNode != null) {
            try {
                selectedAction = (Action) selectedNode.getData();
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void saveOpenBlockGroup() {
        Date startTime = new Date();
        if (selectedAction != null) {
            if (selectedAction.getOpenBlockGroup() == null
                    || selectedAction.getOpenBlockGroup().trim().isEmpty()
                    || selectedAction.getProvisioningType() == null) {
                MessageUtil.setErrorMessageFromRes("label.error.notFillAllData");
            } else {
                // Check group code is exist
                Map<String, Object> filters = new HashedMap();
                filters.put("openBlockGroup", selectedAction.getOpenBlockGroup().trim());
                try {
                    List<Action> actions = new ActionServiceImpl().findListExac(filters, null);
                    if (actions != null && !actions.isEmpty()) {

                        boolean isExist = false;
                        for (Action action : actions) {
                            if (action.getActionId().intValue() != selectedAction.getActionId().intValue()
                                    && action.getOpenBlockGroup().trim().equals(selectedAction.getOpenBlockGroup().trim())) {
                                isExist = true;
                                break;
                            }
                        }

                        if (isExist) {
                            MessageUtil.setErrorMessageFromRes("existed");
                            return;
                        }
                    }

                    new ActionServiceImpl().saveOrUpdate(selectedAction);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.UPDATE,
                                selectedAction.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                    MessageUtil.setInfoMessageFromRes("common.message.success");
                    selectedAction = null;

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    public void preSetProvisionType() {
        if (selectedNode != null) {
            try {
                selectedAction = (Action) selectedNode.getData();
                Map<String, Object> filters = new HashedMap();
                filters.put("action.actionId", selectedAction.getActionId());
                List<ActionDetail> actionDetails = new ActionDetailServiceImpl().findList(filters);
                if (actionDetails != null && !actionDetails.isEmpty()) {

                    boolean isProCmd = false;
                    for (ActionDetail actionDetail : actionDetails) {
                        if (actionDetail.getVendor().getVendorId() == Config.APP_TYPE.PROVISIONING.value) {
                            isProCmd = true;
                        }
                    }

                    if (isProCmd) {
                        RequestContext.getCurrentInstance().update("provisioningTypeForm:dlgprovisioningType");
                        RequestContext.getCurrentInstance().execute("PF('dlgprovisioningType').show()");
                    } else {
                        MessageUtil.setErrorMessageFromRes("invalid");
                    }
                } else {
                    MessageUtil.setErrorMessageFromRes("invalid");
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void saveProvisionType() {
        Date startTime = new Date();
        if (selectedAction != null) {
            if (selectedAction.getProvisioningType() == null || selectedAction.getProvisioningType().intValue() == 0) {
                MessageUtil.setErrorMessageFromRes("label.error.notFillAllData");
            } else {
                try {
                    new ActionServiceImpl().saveOrUpdate(selectedAction);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.UPDATE,
                                selectedAction.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                    MessageUtil.setInfoMessageFromRes("common.message.success");
                    selectedAction = null;

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
    }

    /*20181226_hoangnd_them thi truong_start*/
    public boolean isCountryNodeExist() {

        boolean isExist = false;
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("name-EXAC", insertNode.getName());
            List<Action> actions = actionServiceImpl.findList(filters);
            if (CollectionUtils.isNotEmpty(actions))
                isExist = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return isExist;
    }

    public boolean isBusinessExist() {

        boolean isExist = false;
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("businessName-EXAC", insertNode.getName());
            filters.put("parentId", null);
            List<ItBusinessGroup> business = itBusGroupService.findList(filters);
            if (CollectionUtils.isNotEmpty(business))
                isExist = true;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return isExist;
    }

    public boolean isChildExist() {

        boolean isExist = false;
        List<TreeNode> lstChild = null;
        if (selectedNode.getParent() != null) {
            if (isEdit) {
                if (!selectedNode.getType().equals(COUNTRY_NODE)) {
                    lstChild = selectedNode.getParent().getChildren();
                }
            } else {
                lstChild = selectedNode.getChildren();
            }
            if (CollectionUtils.isNotEmpty(lstChild)) {
                for (TreeNode action : lstChild) {
                    if (((Action) action.getData()).getName().equals(insertNode.getName())) {
                        isExist = true;
                        break;
                    }
                }
            }
        }
        return isExist;
    }

    private String getNodeLabel(int nodeLevel) {
        if (nodeLevel < 6) {
            if (nodeLevel == 1)
                return GRAND_NODE;
            if (nodeLevel == 2)
                return COUNTRY_NODE;
            else if (nodeLevel == 5)
                return SUB_PARENT_NODE;
            else
                return PARENT_NODE;
        } else {
            return CHILD_NODE;
        }
    }
    /*20181226_hoangnd_them thi truong_end*/

    /**
     * Ham them node con moi cho node hien tai
     */
    public void insertChildNode() {

        try {
            Date startTime = new Date();
            if (StringUtils.isEmpty(insertNode.getName())) {
                MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
                return;
            } else if (insertNode.getName().trim().length() > MAX_LENGTH_ACTION_NAME) {
                MessageUtil.setErrorMessageFromRes("label.validate.length.action");
                return;
            }

            if (insertNode.getDescription() == null || "".equals(insertNode.getDescription().trim())) {
                MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
                return;
            } else if (insertNode.getDescription().trim().length() > MAX_LENGTH_ACTION_NAME) {
                MessageUtil.setErrorMessageFromRes("label.validate.length.action.desc");
                return;
            }

            insertNode.setName(insertNode.getName().replaceAll(" +", " ").trim());
            insertNode.setDescription(insertNode.getDescription().replaceAll(" +", " ").trim());

            if (!isClone) {
                /*20190110_hoangnd_them thi truong_start*/
                ItBusinessGroup itBusinessGroup = new ItBusinessGroup();
                /*20190110_hoangnd_them thi truong_end*/
                if (selectedNode == null || selectedNode.getData() == null) {
                    MessageUtil.setErrorMessageFromRes("message.err.no.node.selected");
                    return;
                }

                /*20190110_hoangnd_them thi truong_start*/
                if (selectedNode != null && selectedNode.getType() != null) {
                    if (selectedNode.getType().equals(GRAND_NODE) && (isCountryNodeExist() || isBusinessExist())) {
                        MessageUtil.setErrorMessageFromRes("message.err.company.exist");
                        return;
                    } else if (isChildExist() && !((Action) selectedNode.getData()).getName().equals(insertNode.getName())) {
                        MessageUtil.setErrorMessageFromRes("message.err.action.exist");
                        return;
                    }
                }

                if (isEdit) {
                    insertNode.setActionId(((Action) selectedNode.getData()).getActionId());
                    insertNode.setServiceBusinessId(((Action) selectedNode.getData()).getServiceBusinessId());
                    if (selectedNode.getParent() != null && (selectedNode.getParent().getType().equals(GRAND_NODE) || selectedNode.getParent().getType().equals(COUNTRY_NODE))) {
                        itBusinessGroup.setBusinessId(((Action) selectedNode.getData()).getServiceBusinessId());
                        itBusinessGroup.setBusinessName(insertNode.getName());
                        if (selectedNode.getParent() != null && selectedNode.getParent().getType().equals(COUNTRY_NODE))
                            itBusinessGroup.setParentId(insertNode.getAction().getServiceBusinessId());
                        itBusGroupService.saveOrUpdate(itBusinessGroup);
                    }
                    Long treeLevel;
                    if (selectedNode.getType().equals(GRAND_NODE)) {
                        treeLevel = 0L;
                    } else if (selectedNode.getType().equals(COUNTRY_NODE)) {
                        treeLevel = 1L;
                    } else if (selectedNode.getType().equals(PARENT_NODE)) {
                        if (selectedNode.getParent() != null && selectedNode.getParent().getType().equals(COUNTRY_NODE)) {
                            treeLevel = 2L;
                        } else {
                            treeLevel = 3L;
                        }
                    } else if (selectedNode.getType().equals(SUB_PARENT_NODE)) {
                        treeLevel = 4L;
                    } else {
                        treeLevel = 5L;
                    }
                    insertNode.setTreeLevel(treeLevel);
                } else {
                    if (selectedNode.getType() != null) {
                        if (selectedNode.getType().equals(GRAND_NODE) || selectedNode.getType().equals(COUNTRY_NODE)) {
                            itBusinessGroup.setBusinessName(insertNode.getName());
                            if (selectedNode.getType().equals(COUNTRY_NODE)) {
                                itBusinessGroup.setParentId(((Action) selectedNode.getData()).getServiceBusinessId());
                            }
                            itBusGroupService.saveOrUpdate(itBusinessGroup);
                            insertNode.setServiceBusinessId(itBusinessGroup.getBusinessId());
                        }
                        Long treeLevel;
                        if (selectedNode.getType().equals(GRAND_NODE)) {
                            treeLevel = 1L;
                        } else if (selectedNode.getType().equals(COUNTRY_NODE)) {
                            treeLevel = 2L;
                        } else if (selectedNode.getType().equals(PARENT_NODE)) {
                            if (selectedNode.getParent() != null && selectedNode.getParent().getType().equals(COUNTRY_NODE)) {
                                treeLevel = 3L;
                            } else {
                                treeLevel = 4L;
                            }
                        } else {
                            treeLevel = 5L;
                        }
                        insertNode.setTreeLevel(treeLevel);
                    }
                    insertNode.setAction(actionServiceImpl.findById(((Action) selectedNode.getData()).getActionId()));
                }
                /*20190110_hoangnd_them thi truong_end*/

                actionServiceImpl.saveOrUpdate(insertNode);
                /*20190110_hoangnd_them thi truong_end*/
            } else {

				/*
                 * Ham clone du lieu cho action
				 */
                List<ActionDetail> actionsDetail = insertNode.getActionDetails();
                insertNode.setActionDetails(null);
                // luu action clone
                Long actionId = actionServiceImpl.save(insertNode);
                insertNode = actionServiceImpl.findById(actionId);

                // Tao moi cac action detail di theo action clone

                if (actionsDetail != null && !actionsDetail.isEmpty()) {
                    List<ActionCommand> lstActionCommand;
                    Long actionDetailId;
                    ActionDetail actionDetail;
                    for (int i = 0; i < actionsDetail.size(); i++) {

                        lstActionCommand = actionsDetail.get(i).getActionCommands();
                        actionsDetail.get(i).setDetailId(null);
                        actionsDetail.get(i).setAction(insertNode);
                        actionsDetail.get(i).setActionCommands(null);

                        actionDetailId = actionDetailServiceImpl.save(actionsDetail.get(i));
                        actionDetail = actionsDetail.get(i);
                        actionDetail.setDetailId(actionDetailId);

                        if (lstActionCommand != null) {
                            for (int j = 0; j < lstActionCommand.size(); j++) {
                                lstActionCommand.get(j).setActionCommandId(null);
                                lstActionCommand.get(j).setActionDetail(actionDetail);
                            }
                            new ActionCommandServiceImpl().saveOrUpdate(lstActionCommand);
                        }
                    }

                }

            }


            // them moi node con vao node hien tai
            if (!isEdit && !isClone) {
                int nodeLevel = getNodeLevel(selectedNode);
                /*20181226_hoangnd_them thi truong_start*/
                String label = getNodeLabel(nodeLevel);
                /*20181226_hoangnd_them thi truong_end*/
                TreeNode node = new DefaultTreeNode(label, insertNode, selectedNode);
                selectedNode.getChildren().add(node);

            } else {

                // hanhnv68 20161017
                // Cap nhat lai trang thai cua cac template co action nay
                if (!isClone) {
                    updateTemplateReference(insertNode);
                }
                // end hanhnv68 20161017

                List<TreeNode> lstChildOfCurNode = selectedNode.getChildren();
                TreeNode parent = selectedNode.getParent();
                if (parent == null || parent.getChildren() == null) {
                    logger.info("Error parent null");
                    return;
                }

                List<TreeNode> lstChild = parent.getChildren();
                if (lstChild != null) {
                    int nodeLevel = getNodeLevel(selectedNode);
                    /*20181226_hoangnd_them thi truong_start*/
                    String label = getNodeLabel(nodeLevel);
                    /*20181226_hoangnd_them thi truong_end*/
                    if (!isClone) {
                        for (TreeNode node : lstChild) {
                            if (((Action) node.getData()).getActionId() == ((Action) selectedNode.getData()).getActionId()) {
                                lstChild.remove(node);
                                break;
                            }
                        } // end loop for
                    }

                    TreeNode newNode = new DefaultTreeNode(label, insertNode, parent);
                    newNode.setParent(parent);
                    newNode.getChildren().addAll(lstChildOfCurNode);

                    // add and sort list child node

                    parent.getChildren().add(0, newNode);
                    Collections.sort(parent.getChildren(), new Comparator<TreeNode>() {
                        @Override
                        public int compare(final TreeNode object1, final TreeNode object2) {
                            return ((Action) object1.getData()).getName().compareTo(((Action) object2.getData()).getName());
                        }
                    });

                }
            }

            /*20190114_hoangnd_fix bug load lai sau insert/update_start*/
            if (new SessionUtil().isItBusinessAdmin()) {
                createTree();
            } else {
                createTree2();
            }
            /*20190114_hoangnd_fix bug load lai sau insert/update_end*/

            /*
            Ghi log tac dong nguoi dung
            */
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        (isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
                        insertNode.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            /*20181226_hoangnd_them thi truong_start*/
            clean();
            RequestContext.getCurrentInstance().execute("PF('dlgAddAction').hide()");
//            RequestContext.getCurrentInstance().execute("PF('dlgAddCountry').hide()");
            /*20181226_hoangnd_them thi truong_end*/
            MessageUtil.setInfoMessageFromRes("label.action.updateOk");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
        } finally {
//            clean();
        }
    }

    private void updateTemplateReference(Action action) {
        if (action != null) {
            List<ActionOfFlow> lstActionFlow = action.getActionOfFlows();
            if (lstActionFlow != null && !lstActionFlow.isEmpty()) {
                List<FlowTemplates> lstFlowTemplate = new ArrayList<>();
                try {
                    for (ActionOfFlow actionFlow : lstActionFlow) {
                        FlowTemplates flowTemplate = actionFlow.getFlowTemplates();
                        if (flowTemplate != null) {
                            flowTemplate.setStatus(Config.APPROVAL_STATUS_DEFAULT);
                            lstFlowTemplate.add(flowTemplate);
                        }
                    }

                    new FlowTemplatesServiceImpl().saveOrUpdate(lstFlowTemplate);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

            }
        }
    }

    private Integer getNodeLevel(TreeNode node) {
        /*20190114_hoangnd_them thi truong_start*/
        int level = 0;
        /*20190114_hoangnd_them thi truong_end*/
        while (node != null) {
            node = node.getParent();
            if (node != null) {
                level++;
            }
        }
        return level;
    }

    /*20190114_hoangnd_them thi truong_start*/
    private Integer getActionLevel(Action action) {
        int level = 0;
        while (action != null) {
            action = action.getAction();
            if (action != null) {
                level++;
            }
        }
        return level;
    }
    /*20190114_hoangnd_them thi truong_end*/

    /**
     * Ham xoa du lieu node con
     */
    public void deleteActionNode() {
        if (selectedNode != null) {
            try {
                /*20190110_hoangnd_them thi truong_start*/
                if (selectedNode != null) {
                    Action action = (Action) selectedNode.getData();
                    deleteItBusinessGroup(action.getServiceBusinessId());
                }
                /*20190110_hoangnd_them thi truong_end*/
                deleteNodeOfTree(selectedNode);
                MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
            } catch (Exception e) {
                MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
                logger.error(e.getMessage(), e);
            } finally {
                clean();
            }
        }
    }

    /*20190110_hoangnd_them thi truong_start*/
    public void deleteItBusinessGroup(Long businessId) {

        if (businessId != null) {
            ItBusinessGroup itBusinessGroup;
            List<ItBusinessGroup> itBusinessGroups;
            Map<String, Object> filters;
            try {
                itBusinessGroup = itBusGroupService.findById(businessId);
                if (itBusinessGroup != null)
                    itBusGroupService.delete(itBusinessGroup);

                filters = new HashMap<>();
                filters.put("parentId", businessId);
                itBusinessGroups = itBusGroupService.findList(filters);
                if (CollectionUtils.isNotEmpty(itBusinessGroups)) {
                    for (ItBusinessGroup businessGroup : itBusinessGroups) {
                        deleteItBusinessGroup(businessGroup.getBusinessId());
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }
    /*20190110_hoangnd_them thi truong_end*/

    public void deleteNodeOfTree(TreeNode selectedNode) {
        if (selectedNode != null) {
            try {
                Date startTime = new Date();
                selectedNode.getChildren().clear();
                selectedNode.getParent().getChildren().remove(selectedNode);

                List<TreeNode> lstNodeDelete = selectedNode.getChildren();
                if (lstNodeDelete != null && !lstNodeDelete.isEmpty()) {
                    for (TreeNode node : lstNodeDelete) {
                        try {
                            // goi de quy
                            deleteNodeOfTree(node);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } // end loop for
                }

                actionServiceImpl.delete((Action) selectedNode.getData());

                /*
                Ghi log tac dong nguoi dung
                */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.DELETE,
                            ((Action) selectedNode.getData()).toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public boolean checkAddAction(int lv) {
        /*20190109_hoangnd_them thi truong_start*/
        treeNodeLevel = lv;
        /*20190109_hoangnd_them thi truong_end*/
        boolean check = false;
        try {
            if (selectedNode != null) {
//				System.out.println((Action) selec);
                if (selectedNode.getChildCount() > 0) {
                    check = true;
                } else {
                    Action action = (Action) selectedNode.getData();
                    if (action.getActionOfFlows() == null || action.getActionOfFlows().isEmpty()) {
                        check = true;
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return check;
    }

    public boolean checkIsLeft() {
        boolean check = false;
        try {
            if (selectedNode != null) {
                if (selectedNode.getChildCount() == 0) {
                    check = true;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return check;
    }

    public boolean checkAddActionToTemplate() {
        boolean check = false;
        try {

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return check;
    }

    public void pollGetCmdLog() {
        try {
            logger.info("start get command log");
            commandLogList = new ArrayList<>();
            if (selectedActionLog != null) {
                Map<String, Object> filter = new HashMap<>();
                filter.put("actionLog.id", selectedActionLog.getId());
                LinkedHashMap<String, String> orders = new LinkedHashMap<>();
                orders.put("orderRun", "ASC");
                orders.put("logType", "ASC");
                commandLogList = new ItCommandLogServiceImpl().findList(filter, orders);
                selectedActionLog = new ItActionLogServiceImpl().findById(selectedActionLog.getId());

                if (selectedActionLog.getEndTime() != null) {
                    MessageUtil.setInfoMessageFromRes("gnoc.cr.status.7");
                    RequestContext.getCurrentInstance().execute("PF('pollUpdateLog').stop()");
                    RequestContext.getCurrentInstance().update("form:mainMessage");
                    RequestContext.getCurrentInstance().update("form:panelLogDetail");
                    //20182906_tudn_start them hien thi process cho
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    //20182906_tudn_end them hien thi process cho
                }
                //tuanda38_20180905_start
                if (selectedActionLog.getStartTime() != null) {
                    if (Calendar.getInstance().getTime().getTime() - selectedActionLog.getStartTime().getTime() > (1 * 60 * 1000)) {
                        /*20190115_hoangnd_them thi truong_start*/
//                        MessageUtil.setInfoMessage("Lệnh đang thực hiện hoặc bị lỗi. Xem kết quả chi tiết trong lịch sử tác động");
                        MessageUtil.setInfoMessage("Command is executing or error. See detailed results in the impact history");
                        /*20190115_hoangnd_them thi truong_end*/
                        RequestContext.getCurrentInstance().execute("PF('pollUpdateLog').stop()");
                        RequestContext.getCurrentInstance().update("form:mainMessage");
                        RequestContext.getCurrentInstance().update("form:panelLogDetail");
                        //20182906_tudn_start them hien thi process cho
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        //20182906_tudn_end them hien thi process cho
                    }
                }
                //tuanda38_20180905_end
            } else {
                RequestContext.getCurrentInstance().execute("PF('pollUpdateLog').stop()");
                RequestContext.getCurrentInstance().update("form:panelLogDetail");
                RequestContext.getCurrentInstance().update("form:mainMessage");
                //20182906_tudn_start them hien thi process cho
                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                //20182906_tudn_end them hien thi process cho
            }

        } catch (Exception e) {
            commandLogList = new ArrayList<>();
            //20182906_tudn_start them hien thi process cho
            RequestContext.getCurrentInstance().execute("PF('pollUpdateLog').stop()");
            RequestContext.getCurrentInstance().update("form:panelLogDetail");
            RequestContext.getCurrentInstance().update("form:mainMessage");
            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
            MessageUtil.setErrorMessageFromRes("message.error.run");
            //20182906_tudn_end them hien thi process cho
            logger.error(e.getMessage(), e);
        }
    }

    public List<String> buildColumnsLabel(String value) {
        if (value != null && !value.isEmpty()) {
            List<String> tbsDatas = Arrays.asList(value.trim().split("\\{CRLF}"));
            List<String> columnsName = new ArrayList<>();
            if (tbsDatas != null && !tbsDatas.isEmpty()) {
                columnsName = Arrays.asList(tbsDatas.get(0).trim().split("\\{,}"));
            }
            return columnsName;
        } else {
            return new ArrayList<>();
        }
    }

    public List<List<String>> buildSelectSqlVal(String value) {
        List<List<String>> sqlDataTables = new LinkedList<>();
        List<String> tbsDatas = Arrays.asList(value.trim().split("\\{CRLF}"));
        if (tbsDatas != null && !tbsDatas.isEmpty()) {
            if (tbsDatas.size() > 1) {
                for (int i = 1; i < tbsDatas.size(); i++) {
                    List<String> rowData = Arrays.asList(tbsDatas.get(i).trim().split("\\{,}"));
                    sqlDataTables.add(rowData);
                }
            }
        }
        return sqlDataTables;
    }

    public TreeNode buildTreeNodeData(String xmlData) {
        TreeNode root = null;
        //tuanda38
        xmlData = xmlData.replaceAll("&", " and ");
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document doc = saxBuilder.build(new StringReader(xmlData));
            Element rootElement = doc.getRootElement();

            root = new DefaultTreeNode(new XmlModel("root", ""), null);
            root.setExpanded(true);
            TreeNode rootElementNode = new DefaultTreeNode(new XmlModel(rootElement.getName(), ""), root);
            rootElementNode.setExpanded(true);
            if (rootElement.getChildren() != null && !rootElement.getChildren().isEmpty()) {
                List<Element> childs = rootElement.getChildren();
                for (Element child : childs) {
                    recurBuildTreeNode(child, rootElementNode);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return root;
    }

    private void recurBuildTreeNode(Element element, TreeNode treeNode) {
        try {
//            TreeNode curNode = new DefaultTreeNode(new XmlModel(element.getName(), ""), treeNode);
            if (element.getChildren() != null && !element.getChildren().isEmpty()) {
                TreeNode curNode = new DefaultTreeNode(new XmlModel(element.getName(), ""), treeNode);
                curNode.setExpanded(true);
                List<Element> elements = element.getChildren();
                for (Element child : elements) {
                    recurBuildTreeNode(child, curNode);
                }
            } else {
                new DefaultTreeNode(new XmlModel(element.getName(), element.getText()), treeNode);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void clean() {
        isClone = false;
        isEdit = false;
        selectedNode = null;
        insertNode = new Action();

        actionNameToClone = "";
        selectedAction = new Action();
        selectedActionCommand = new ActionCommand();
        selectedActionDetail = new ActionDetail();
        selectedVersion = new Version();
        selectedCmdDetail = new CommandDetail();
        selectedCmdTelnetParser = new CommandTelnetParser();
        selectedNodeType = new NodeType();
        selectedVendor = new Vendor();
    }

    public String getNameActionSelected() {
        if (selectedNode != null) {
            return ((Action) selectedNode.getData()).getName();
        }
        return "";
    }

    public TreeNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(TreeNode rootNode) {
        this.rootNode = rootNode;
    }

    public ActionServiceImpl getActionServiceImpl() {
        return actionServiceImpl;
    }

    public void setActionServiceImpl(ActionServiceImpl actionServiceImpl) {
        this.actionServiceImpl = actionServiceImpl;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public Action getInsertNode() {
        return insertNode;
    }

    public void setInsertNode(Action insertNode) {
        this.insertNode = insertNode;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public List<Action> autoCompleAction(String actionName) {
        List<Action> lstAction = new ArrayList<>();
        try {
            Map<String, Object> filters = new HashMap<>();
            if (actionName != null) {
                filters.put("action.name", actionName);
            }
            LinkedHashMap<String, String> order = new LinkedHashMap<String, String>();
            order.put("action.name", "ASC");
            lstAction = actionServiceImpl.findList(filters, order);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstAction;
    }

    public List<Vendor> autoCompleVendor(String actionName) {
        List<Vendor> lstAction = new ArrayList<>();
        try {
            //lstAction = vendorServiceImpl.findList();
            Map<String, Object> filters = new HashMap<>();
            if (actionName != null) {
                filters.put("vendorName", actionName);
            }
            LinkedHashMap<String, String> order = new LinkedHashMap<String, String>();
            order.put("vendorName", "ASC");
            lstAction = vendorServiceImpl.findList(filters, order);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstAction;
    }

    public void addActionDetail() {
        if (selectedVendor != null
                //&& nodeDefault != null
                && selectedAction != null
            //&& versionDefault != null
                ) {
            try {
                // kiem tra xem action detail them moi da ton tai hay chua
                if (!ActionUtil.checkExistActionDetail2(nodeDefault.getTypeId(), versionDefault.getVersionId(),
                        selectedAction.getActionId())) {

                    if (!isEditActionDetail) {
                        ActionDetail newAcDetail = new ActionDetail();
                        newAcDetail.setAction(selectedAction);
                        newAcDetail.setNodeType(nodeDefault);
                        newAcDetail.setVendor(selectedVendor);
                        newAcDetail.setUserName(SessionUtil.getCurrentUsername());
                        newAcDetail.setActionCommands(new ArrayList<ActionCommand>(0));
                        newAcDetail.setIsActive(1l);
                        newAcDetail.setVersion(versionDefault);

                        new ActionDetailServiceImpl().saveOrUpdate(newAcDetail);
                        isEditActionDetail = false;
                        selectedVendor = null;
                        selectedNodeType = null;
                        selectedVersion = null;

                    } else {
                        selectedActionDetail.setVendor(selectedVendor);
                        selectedActionDetail.setNodeType(selectedNodeType);
                        selectedActionDetail.setIsActive(stationDetailStatus);
                        new ActionDetailServiceImpl().saveOrUpdate(selectedActionDetail);
                    }

                    selectedAction = new ActionServiceImpl().findById(selectedAction.getActionId());

                    // hanhnv68 20161017
                    // Cap nhat lai trang thai cac template co tham chieu den action
                    updateTemplateReference(selectedAction);
                    // end hanhnv68 20161017
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                (isEditActionDetail ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
                                selectedAction.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                    MessageUtil.setInfoMessageFromRes("label.action.updateOk");

                } else {
                    // thong bao loi ban ghi da ton tai
                    MessageUtil.setErrorMessageFromRes("label.error.exist");
                }
            } catch (Exception e) {
                MessageUtil.setErrorMessageFromRes("label.action.updateFail");
                logger.error(e.getMessage(), e);
            }

        } else {
            // thong bao loi chua nhap day du thong tin
            MessageUtil.setErrorMessageFromRes("label.error.notFillAllData");
        }
    }

    public List<NodeType> autoCompleNodeType(String actionName) {
        List<NodeType> lstAction = new ArrayList<>();
        try {
            Map<String, Object> filters = new HashMap<>();
            if (actionName != null) {
                filters.put("typeName", actionName);
            }
            LinkedHashMap<String, String> order = new LinkedHashMap<String, String>();
            order.put("typeName", "ASC");
            lstAction = nodeTypeServiceImpl.findList(filters, order);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstAction;
    }

    public List<Version> autoCompleteVersion(String version) {
        List<Version> lstVertion = new ArrayList<>();
        try {
            Map<String, Object> filters = new HashMap<>();
            if (version != null && !version.trim().isEmpty()) {
                filters.put("versionName", version);
            }
            LinkedHashMap<String, String> order = new LinkedHashMap<String, String>();
            order.put("versionName", "ASC");
            lstVertion = new VersionServiceImpl().findList(filters, order);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstVertion;
    }


    public void onSelectActionDetail(SelectEvent event) {
        try {
            selectedActionDetail = (ActionDetail) event.getObject();
            lstActionCommand = new LinkedList<>();
            lstActionCmdDel = new ArrayList<>();

            ActionCommand actionCmd;
            int size = selectedActionDetail.getActionCommands().size();
            for (int i = 0; i < size; i++) {
                actionCmd = selectedActionDetail.getActionCommands().get(i);
                actionCmd.setOrderRun(Long.valueOf(i));
                lstActionCommand.add(actionCmd);
            }

            if (lstActionCommand.size() > 0) {
                Collections.sort(lstActionCommand, new Comparator<ActionCommand>() {
                    @Override
                    public int compare(final ActionCommand object1, final ActionCommand object2) {
                        return object1.getOrderRun().compareTo(object2.getOrderRun());
                    }
                });
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void prepareEditActionDetail() {
        try {
            isEditActionDetail = true;
            selectedNodeType = selectedActionDetail.getNodeType();
            selectedVendor = selectedActionDetail.getVendor();
            selectedVersion = selectedActionDetail.getVersion();
            stationDetailStatus = selectedActionDetail.getIsActive();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void deleteActionDetail() {
        try {
            if (selectedActionDetail != null) {
                Date startTime = new Date();
                if (selectedActionDetail.getActionCommands() != null && !selectedActionDetail.getActionCommands().isEmpty()) {
                    MessageUtil.setErrorMessageFromRes("label.err.del.action.have.cmd");
                    return;
                }
                new ActionDetailServiceImpl().delete(selectedActionDetail);
                lstActionCommand = new LinkedList<>();
                selectedActionDetail = new ActionDetail();
                selectedAction = new ActionServiceImpl().findById(selectedAction.getActionId());

                // hanhnv68 20161017
                // Cap nhat lai trang thai cua cac template co action nay
                updateTemplateReference(selectedAction);
                // end hanhnv68 20161017

                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.DELETE,
                            selectedActionDetail.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
                MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
            } else {
                MessageUtil.setErrorMessageFromRes("message.choose.delete");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
        }
    }

    public void closeDlgActionDetail() {
        rebuildTree();
        clean();
    }

    public void rebuildTree() {
        try {
            if (selectedNode != null && selectedNode.getParent() != null) {
                List<TreeNode> lstChildOfCurNode = selectedNode.getChildren();
                TreeNode parent = selectedNode.getParent();
                if (parent.getChildren() != null) {
                    List<TreeNode> lstChild = parent.getChildren();
                    if (lstChild != null) {
                        int count = 0;
                        for (TreeNode node : lstChild) {
                            count++;
                            if (((Action) node.getData()).getActionId() == ((Action) selectedNode.getData()).getActionId()) {
                                lstChild.remove(node);
                                break;
                            }
                        } // end loop for

                        TreeNode newNode = new DefaultTreeNode(CHILD_NODE, selectedAction, parent);
                        newNode.getChildren().addAll(lstChildOfCurNode);
                        parent.getChildren().add((count > 0 ? (count - 1) : count), newNode);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void onUnSelectActionDetail(SelectEvent event) {

    }

    public List<CommandDetail> autoCmdMethod(String cmd) {

        Map<String, Object> filters = new HashMap<>();
        if (cmd != null && !cmd.trim().isEmpty()) {
            filters.put("commandTelnetParser.cmd", cmd);
        }
        filters.put("vendor.vendorId", selectedActionDetail.getVendor().getVendorId());
//		filters.put("nodeType.typeId", selectedActionDetail.getNodeType().getTypeId());
//		filters.put("version.versionId", selectedActionDetail.getVersion().getVersionId());
//		filters.put("userName",SessionWrapper.getCurrentUsername());
        filters.put("commandType", 0l);
        filters.put("commandClassify", 1l);
        List<CommandDetail> lstCmd = new ArrayList<>();
        LinkedHashMap<String, String> order = new LinkedHashMap<String, String>();
        try {
            order.put("commandTelnetParser.cmd", "ASC");
            lstCmd = new CommandDetailServiceImpl().findList(0, 100, filters, order);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            lstCmd = new ArrayList<>();
        }

        // Lay them danh sach cac lenh ghi log tac dong dang database
        filters.clear();
        try {
//			filters.put("vendor.vendorId", selectedActionDetail.getVendor().getVendorId());
//			filters.put("userName",SessionWrapper.getCurrentUsername());
            filters.put("commandType", 3l);
            List<CommandDetail> lstCmdLogDetail = new CommandDetailServiceImpl().findList(0, 100, filters, order);
            if (lstCmdLogDetail != null) {
                lstCmd.addAll(lstCmdLogDetail);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return lstCmd;
    }

    public static void main(String[] args) {
        List<FlowTemplateMapAlarm> flowTemplateMapAlarms = new ArrayList<>();
        flowTemplateMapAlarms.add(null);
        System.out.println(flowTemplateMapAlarms.size());
//        try {
//            SimpleDateFormat format =  new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
//            Date date = format.parse("2018-09-05 15:57:00");
//            System.out.println(Calendar.getInstance().getTime().getTime() - date.getTime());
//            System.out.println(format.format(Calendar.getInstance().getTime()));
//        } catch (ParseException e) {
//            e.printStackTrace();
//        }
//        try {
//            System.out.println("kjhfd@{dkjf}".contains("@{"));
//            System.out.println(PasswordEncoder.decrypt("lsVbO9GBtHNakCGkJ47hmw=="));
//        } catch (Exception var2) {
//            logger.error(var2.getMessage(), var2);
//        }

    }

    public void prepareAddUser() {
        selectedAction = new Action();
        List<ItUsers> lstUserBusinessGroup = new ArrayList<>();
        if (selectedNode != null) {
            try {
                selectedAction = ((Action) selectedNode.getData());
                // reload data
                selectedAction = new ActionServiceImpl().findById(selectedAction.getActionId());
                // get business action
                Action businessAction = selectedAction.getAction().getAction();
                logger.info("business group name: " + businessAction.getName());
                // get list user
                /*20190124_hoangnd_check null_start*/
                ItBusinessGroup businessGroup = null;
                if (businessAction != null && businessAction.getServiceBusinessId() != null) {
                    businessGroup = new ItBusGroupServiceImpl().findById(businessAction.getServiceBusinessId());
                }
                /*20190124_hoangnd_check null_end*/
                if (businessGroup != null) {
                    List<ItUserBusinessGroup> lstUserGroup = businessGroup.getLstUserBusinessGroup();
                    if (lstUserGroup != null && !lstUserGroup.isEmpty()) {
                        for (ItUserBusinessGroup userGroup : lstUserGroup) {
                            lstUserBusinessGroup.add(userGroup.getUser());
                        }
                    }
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            try {
                List<ItUsers> lstUserSourceDis = new ArrayList<>();
                List<ItUsers> lstUserSource = new ArrayList<>();
                List<ItUsers> lstUserTarget = new ArrayList<>();
                Map<Long, Long> mapUser = new HashMap<>();
                if (selectedAction.getUserActions() != null && !selectedAction.getUserActions().isEmpty()) {
                    List<ItUserAction> lstUserAction = selectedAction.getUserActions();

                    for (ItUserAction userAction : lstUserAction) {
                        if (mapUser.get(userAction.getUser().getUserId()) == null) {
                            mapUser.put(userAction.getUser().getUserId(), 1l);
                            lstUserTarget.add(userAction.getUser());
                        }
                    }
                }

                /*20190103_hoangnd_them thi truong_start*/
                loadCountryCode();
                String sql = " select a.USER_ID userId, a.USER_NAME userName, a.FULL_NAME fullName, a.EMAIL email, a.PHONE phone, a.STAFF_CODE staffCode, a.STATUS status " +
                        " from IT_USERS a " +
                        " left join MAP_USER_COUNTRY b on a.USER_NAME = b.USER_NAME " +
                        " where b.COUNTRY_CODE = :countryCode and b.STATUS = 1 ";
                Query query = HibernateUtil.openSession().createSQLQuery(sql)
                        .addScalar("userId", StandardBasicTypes.LONG)
                        .addScalar("userName", StandardBasicTypes.STRING)
                        .addScalar("fullName", StandardBasicTypes.STRING)
                        .addScalar("email", StandardBasicTypes.STRING)
                        .addScalar("phone", StandardBasicTypes.STRING)
                        .addScalar("staffCode", StandardBasicTypes.LONG)
                        .addScalar("status", StandardBasicTypes.LONG)
                        .setParameter("countryCode", countryCode)
                        .setResultTransformer(Transformers.aliasToBean(ItUsers.class));
                lstUserSource = query.list();
                /*20190103_hoangnd_them thi truong_end*/

                if (lstUserTarget.size() > 0) {
                    for (int i = 0; i < lstUserTarget.size(); i++) {
                        for (int j = 0; j < lstUserSource.size(); j++) {
                            String userName1 = lstUserTarget.get(i).getUserName();
                            String userName2 = lstUserSource.get(j).getUserName();
                            if (userName1.equals(userName2)) {
                                lstUserSource.remove(j);
                            }
                        }
                    }
                }
                lstUserSourceDis.addAll(lstUserSource);


				/*for (ItUsers user : lstUserBusinessGroup) {
                    if (mapUser.get(user.getUserId()) == null) {
						lstUserSource.add(user);
					}
				}*/
                usersPicklist = new DualListModel<ItUsers>(lstUserSourceDis, lstUserTarget);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void saveAddUserExcute() {
        List<ItUsers> usersSelected = usersPicklist.getTarget();
        Date startTime = new Date();
        try {
            // Delete all current user-actions
            List<ItUserAction> oldUsersAction = selectedAction.getUserActions();
            if (oldUsersAction != null && !oldUsersAction.isEmpty()) {
                new ItUserActionServiceImpl().delete(oldUsersAction);
            }

            // Insert new user-actions
            if (usersSelected != null && !usersSelected.isEmpty()) {
                List<ItUserAction> newUserActions = new ArrayList<>();
                ItUserAction userAction;
                for (ItUsers user : usersSelected) {
                    userAction = new ItUserAction();
                    userAction.setId(new ItUserActionId(selectedAction.getActionId(), user.getUserId()));
                    userAction.setActionUser(selectedAction);
                    userAction.setUser(user);
                    newUserActions.add(userAction);
                }

                if (!newUserActions.isEmpty()) {
                    new ItUserActionServiceImpl().saveOrUpdate(newUserActions);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.UPDATE,
                                newUserActions.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                }
            }

            MessageUtil.setInfoMessageFromRes("info.save.success");
        } catch (Exception e) {
            MessageUtil.setInfoMessageFromRes("have.some.error");
            logger.error(e.getMessage(), e);
        }
    }

    public void prepareAddNode() {
        if (selectedNode != null) {

            Map<String, Object> filters = new HashMap<>();
            filters.put("active", Constant.status.active);
            /*20181226_hoangnd_them thi truong_start*/
            loadCountryCode();
            filters.put("countryCode.countryCode-EXAC", countryCode);
            /*20181226_hoangnd_them thi truong_end*/
            nodeList = new LazyDataModelBaseNew<>(new NodeServiceImpl(), filters, null);
            selectedItNode = new Node();
            try {
                selectedAction = ((Action) selectedNode.getData());
                // reload data
                selectedAction = new ActionServiceImpl().findById(selectedAction.getActionId());
                String hql = "from ItNodeAction where actionId = " + selectedAction.getActionId();
                nodeActionList = itNodeActionService.findList(hql, -1, -1);

            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void resetAction() {
        Date startTime = new Date();
        selectedAction = null;
        try {
            // update data
            new ItNodeActionServiceImpl().saveOrUpdate(nodeActionList);
            //20180620_tudn_start ghi log DB
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.UPDATE,
                        nodeActionList.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            //20180620_tudn_end ghi log DB
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void deleteNodeExcute() {
        Date startTime = new Date();
        if (itNodeActionSelected != null) {
            try {
                new ItNodeActionServiceImpl().delete(itNodeActionSelected);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.DELETE,
                            itNodeActionSelected.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
                nodeActionList = new ArrayList<>();
                nodeActionList = itNodeActionService.findList("from ItNodeAction where actionId = ?", -1, -1, selectedAction.getActionId());
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
        }
        itNodeActionSelected = null;
    }

    /*
    Luu user tac dong cua node mang
     */
    public void saveAccountImpact() {
        Date startTime = new Date();
        try {
            if (nodeAccountSelected != null) {

                // Xoa toan bo account tac dong cu
                Map<String, Object> filters = new HashedMap();
                filters.put("nodeActionId", itNodeActionSelected.getId());
//				filters.put("nodeAccountId", nodeAccountSelected.getId());

                List<ItActionAccount> actionAccounts = new ItActionAccountServicesImpl().findList(filters);
                if (actionAccounts != null) {
                    new ItActionAccountServicesImpl().delete(actionAccounts);
                }

                ItActionAccount actionAccount = new ItActionAccount();
                actionAccount.setNodeAccountId(nodeAccountSelected.getId());
                actionAccount.setNodeActionId(itNodeActionSelected.getId());

                new ItActionAccountServicesImpl().saveOrUpdate(actionAccount);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.UPDATE,
                            actionAccount.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
                nodeActionList = itNodeActionService.findList("from ItNodeAction where actionId = ?", -1, -1, selectedAction.getActionId());
                RequestContext.getCurrentInstance().execute("PF('dlgAddAccountAction').hide()");
                MessageUtil.setInfoMessageFromRes("info.save.success");
            } else {
                MessageUtil.setErrorMessageFromRes("common.data.not.found");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        itNodeActionSelected = null;
    }

    /*
    Ham chuan bi hien thi
     */
    public void prepareAddAccountImpact() {
        if (itNodeActionSelected != null) {
            try {
                Map<String, Object> filters = new HashMap<>();
                filters.put("active", Constant.status.active);
                if (itNodeActionSelected.getNode().getServerId() == null) {
                    filters.put("serverId", itNodeActionSelected.getNode().getNodeId());
                } else {
                    filters.put("serverId", itNodeActionSelected.getNode().getServerId());
                }
                filters.put("itBusinessNode", 1l);

                /*20181226_hoangnd_them thi truong_start*/
                loadCountryCode();
                filters.put("countryCode.countryCode-EXAC", countryCode);
                /*20181226_hoangnd_them thi truong_end*/

                nodeAccounts = new NodeAccountServiceImpl().findList(filters);
                if (nodeAccounts != null) {
                    for (int i = 0; i < nodeAccounts.size(); i++) {
                        nodeAccounts.get(i).setNodeInfo(getNodeInfo(nodeAccounts.get(i).getServerId()));
                    }
                } else {
                    nodeAccounts = new ArrayList<>();
                }

                RequestContext.getCurrentInstance().update("panelAddNodeExcute:dlgAddAccountAction");
                RequestContext.getCurrentInstance().execute("PF('dlgAddAccountAction').show()");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
        }
    }

    public String showAccImpact(ItNodeAction nodeAction) {
        if (nodeAction != null
                && nodeAction.getActionAccounts() != null
                && !nodeAction.getActionAccounts().isEmpty()) {
            /*20181210_hoangnd_fix bug IndexOutOfBoundsException_start*/
            try {
                return nodeAction.getActionAccounts().get(0).getNodeAccount().getUsername();
            } catch (Exception ex) {
                return "";
            }
            /*20181210_hoangnd_fix bug IndexOutOfBoundsException_end*/
        } else {
            return "";
        }
    }

    public void saveAddNodeExcute(long typeSelected) {
        Date startTime = new Date();
        boolean isInsert = false;
        try {
            // Delete all current node-action
//			String hql  = "from ItNodeAction where id.actionId = " + selectedAction.getActionId() +  " and id.type = " +typeSelected;
//			List<ItNodeAction> oldNodeAction = itNodeActionService.findList(hql,-1,-1);
//			if (oldNodeAction != null && !oldNodeAction.isEmpty()) {
//				svc.delete(oldNodeAction);
//			}

            // Insert new node-action
            if (selectedItNode != null) {
                ItNodeAction nodeAction = new ItNodeAction();
//				ItNodeActionId nodeActionId = new ItNodeActionId();
//				nodeActionId.setActionId(selectedAction.getActionId());
//				nodeActionId.setNodeId(selectedItNode.getNodeId());
//				nodeActionId.setType(typeSelected);
                nodeAction.setActionId(selectedAction.getActionId());
                nodeAction.setNodeId(selectedItNode.getNodeId());
                nodeAction.setType(typeSelected);
                new ItNodeActionServiceImpl().saveOrUpdate(nodeAction);
                isInsert = true;
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.CREATE,
                            nodeAction.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
            }
            nodeActionList = itNodeActionService.findList("from ItNodeAction where actionId = ?", -1, -1, selectedAction.getActionId());

            if (isInsert) {
                MessageUtil.setInfoMessageFromRes("info.save.success");
            } else {
                MessageUtil.setErrorMessageFromRes("common.data.not.found");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void prepareAddActionDetail() {
        selectedAction = new Action();
        lstActionCommand = new LinkedList<>();
        lstActionDetail = new ArrayList<>();

        if (selectedNode != null) {
            selectedAction = ((Action) selectedNode.getData());
            try {
                selectedAction = actionServiceImpl.findById(selectedAction.getActionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            logger.info("selected action: " + selectedAction.getName());
        } else {
            selectedAction = new Action();
            selectedAction.setActionDetails(new ArrayList<ActionDetail>());
        }
    }

    public void addActionCommand() {
        try {
            if (selectedCmdDetail != null) {
                ActionCommand actionCmd = new ActionCommand();
                actionCmd.setActionDetail(selectedActionDetail);
                actionCmd.setCreateTime(new Date());
                actionCmd.setUserName(userName);
                actionCmd.setType(1L);
                actionCmd.setIsActive(1L);
                actionCmd.setCommandDetail(selectedCmdDetail);
                actionCmd.setOrderRun(Long.valueOf(lstActionCommand.size()));
                lstActionCommand.add(actionCmd);
            }
        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
            logger.error(e.getMessage(), e);
        }
    }

    public void deleteCmdDetail(ActionCommand cmdDel, int indexRow) {
        try {
            boolean isDelete = true;
            if (cmdDel.getActionCommandId() != null) {
                // Kiem tra xem cau lenh da duoc sinh MOP hay chua
//				if (cmdDel.getActionDetail().getAction().getActionOfFlows() != null
//						&& !cmdDel.getActionDetail().getAction().getActionOfFlows().isEmpty()) {
//					List<ActionOfFlow> lstActionFlow = cmdDel.getActionDetail().getAction().getActionOfFlows();
//					for (ActionOfFlow actionFLow : lstActionFlow) {
//						if (actionFLow.getNodeRunGroupActions() != null
//								&& !actionFLow.getNodeRunGroupActions().isEmpty()) {
//							MessageUtil.setErrorMessageFromRes("label.cmd.created.mop");
//							return;
//						}
//					}
//				}

                Map<String, Object> filters = new HashMap<>();
                filters.put("actionCommandByActionCommandInputId.actionCommandId", cmdDel.getActionCommandId());
                if (new ParamInOutServiceImpl().count2(filters) > 0) {
                    MessageUtil.setErrorMessageFromRes("message.choose.using");
                    isDelete = false;
                } else {
                    lstActionCmdDel.add(cmdDel);
                }
            }

            if (isDelete) {
                lstActionCommand.remove(indexRow);
                for (int i = 0; i < lstActionCommand.size(); i++) {
                    lstActionCommand.get(i).setOrderRun(Long.valueOf(i));
                }
            }
        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
            logger.error(e.getMessage(), e);
        }
    }

    public void onCellEdit(CellEditEvent event) {

    }

    public void submitActionCommandData() {
        Session session = null;
        try {
            if (selectedActionDetail.getDetailId() == null) {
                return;
            }

            List<ActionCommand> lstActionCmdId = new ArrayList<>();
            List<ActionCommand> lstActionCmdNoId = new ArrayList<>();
            for (ActionCommand actionCmd : lstActionCommand) {
                if (actionCmd.getActionCommandId() == null) {
                    lstActionCmdNoId.add(actionCmd);
                } else {
                    lstActionCmdId.add(actionCmd);
                }
            }

            Object[] objs = new ActionCommandServiceImpl().openTransaction();

            session = (Session) objs[0];
            Transaction tx = (Transaction) objs[1];

            if (lstActionCmdDel != null) {
                new ActionCommandServiceImpl().delete(lstActionCmdDel, session, tx, false);
                List<FlowTemplateMapAlarm> flowTemplateMapAlarms = new ArrayList<>();
                HashMap<String, Object> filters = new HashMap<>();
                for (ActionCommand actionCmdDel : lstActionCmdDel) {
                    List<ParamInput> paramInputs = actionCmdDel.getCommandDetail().getParamInputs();
                    for (ParamInput paramInput : paramInputs) {
                        filters.put("paramInput.paramInputId", paramInput.getParamInputId());
                        List<FlowTemplateMapAlarm> flowTemplateMapAlarmList = flowTemplateMapAlarmService.findList(filters);
                        if (flowTemplateMapAlarmList != null) {
                            flowTemplateMapAlarms.addAll(flowTemplateMapAlarmList);
                        }
                    }
                }
                if (flowTemplateMapAlarms != null && flowTemplateMapAlarms.size() > 0) {
                    flowTemplateMapAlarmService.delete(flowTemplateMapAlarms);
                }
            }
            new ActionCommandServiceImpl().saveOrUpdate(lstActionCmdNoId, session, tx, false);
            new ActionCommandServiceImpl().saveOrUpdate(lstActionCmdId, session, tx, false);

            tx.commit();

            selectedActionDetail = new ActionDetailServiceImpl().findById(selectedActionDetail.getDetailId());

            // hanhnv68 20161017
            // Cap nhat lai trang thai cua cac template co action nay
            Date startTime = new Date();
            Action updateAction = new Action();
            updateAction = selectedAction;
            String protocol = "";

            if (lstActionCommand.size() > 0) {
                if (lstActionCommand.get(0).getCommandDetail().getProtocol().equals(Config.PROTOCOL_EXCHANGE)) {
                    protocol = "PROV@";
                } else if (lstActionCommand.get(0).getCommandDetail().getProtocol().equals(Config.PROTOCOL_SQL)) {
                    protocol = "SQL@";
                } else if (lstActionCommand.get(0).getCommandDetail().getProtocol().equals(Config.PROTOCOL_WEBSERVICE)) {
                    protocol = "WS@";
                } else if (lstActionCommand.get(0).getCommandDetail().getProtocol().equals(Config.PROTOCOL_SSH)) {
                    protocol = "SSH@";
                }
            }
            String name[] = selectedAction.getName().split("@");
            String updateNodeName = null;
            if (name.length > 1) {
                updateNodeName = (protocol + name[1]);
            } else {
                updateNodeName = (protocol + name[0]);
            }
            updateAction.setName(updateNodeName);
            updateTemplateReference(selectedAction);
            new ActionServiceImpl().saveOrUpdate(updateAction);
            // end hanhnv68 20161017
            //20180620_tudn_start ghi log DB
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.UPDATE,
                        updateAction.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            //20180620_tudn_end ghi log DB

            MessageUtil.setInfoMessageFromRes("label.action.updateOk");
        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
            logger.error(e.getMessage(), e);
        } finally {
            lstActionCmdDel = new ArrayList<>();
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e2) {
                    logger.error(e2.getMessage(), e2);
                }
            }
        }
    }


    public void reorderDataTable(ReorderEvent event) {
        // luu lai danh sach action command moi
        for (int i = 0; i < lstActionCommand.size(); i++) {
            lstActionCommand.get(i).setOrderRun(Long.valueOf(i));
        }
    }

    public void closeActionDetailDlg() {
        selectedAction = new Action();
        selectedAction.setActionDetails(new ArrayList<ActionDetail>());
    }

    public void nodeExpand(NodeExpandEvent event) {
        event.getTreeNode().setExpanded(true);
    }

    public void nodeCollapse(NodeCollapseEvent event) {
        event.getTreeNode().setExpanded(false);
    }

    public Node getNodeInfo(Long serverId) {
        if (serverId != null) {
            Node node = null;
            Map<String, Object> filters = new HashedMap();
            try {
                filters.put("serverId", serverId);
                filters.put("active", Constant.status.active);
                List<Node> nodes = new NodeServiceImpl().findList(filters);
                if (nodes != null && !nodes.isEmpty()) {
                    node = nodes.get(0);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                node = new Node();
            }
            if (node == null) {
                try {
                    filters.clear();
                    filters.put("nodeId", node.getServerId());
                    filters.put("active", Constant.status.active);
                    List<Node> nodes = new NodeServiceImpl().findList(filters);
                    if (nodes != null && !nodes.isEmpty()) {
                        node = nodes.get(0);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    node = new Node();
                }
            }
            return node;
        }
        return new Node();
    }

    //tuanda38_export excel_1/12/2017_start
    public StreamedContent onExport(ItCommandLog itCommandLog) {
        try {
            List<List<String>> sqlDataTables = new LinkedList<>();
            List<String> tbsDatas = Arrays.asList(itCommandLog.getLog().trim().split("\\{CRLF}"));
            List<String> columnsName = new ArrayList<>();
            if (tbsDatas != null && !tbsDatas.isEmpty()) {
                columnsName = Arrays.asList(tbsDatas.get(0).trim().split("\\{,}"));
                if (tbsDatas.size() > 1) {
                    for (int i = 1; i < tbsDatas.size(); i++) {
                        List<String> rowData = Arrays.asList(tbsDatas.get(i).trim().split("\\{,}"));
                        sqlDataTables.add(rowData);
                    }
                }
            }
            exelUtil.createWorkbook();

            this.writeData(columnsName, sqlDataTables, itCommandLog.getCommand());

            String reportName = exelUtil.getReportName("log_itbusiness");
            String exportFolder = exelUtil.getFolderSave();

            String pathOut = exportFolder + reportName;
            exelUtil.setSheetSelectedSXSSF(0);
            exelUtil.saveToFileExcelSXSSF(pathOut);

            File fileExport = new File(pathOut);
            return new DefaultStreamedContent(new FileInputStream(fileExport), ".xlsx", fileExport.getName());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private void writeData(List<String> columnsName, List<List<String>> rowDatas, String command) {
        try {
            SXSSFSheet sheet = exelUtil.getSXSSFworkbook().createSheet("ItBusiness");

            CellStyle csHeader = exelUtil.getCsColHeader();
//            CellStyle csCenter = exelUtil.getCsCenterBoder();
//            CellStyle csDate = exelUtil.getCsDateBoder();
            CellStyle csLeft = exelUtil.getCsLeftBoder();
//            CellStyle csRight = exelUtil.getCsRightBoder();
            CellStyle csTitle = exelUtil.getCsTitle();

            List<Object[]> headerInfors = new ArrayList<>();
            columnsName.forEach(col -> {
                headerInfors.add(new Object[]{col, 3000});
            });

            int size = headerInfors.size();

            exelUtil.createCellObject(sheet, 0, 0, command, csTitle);
            sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, size - 1));
            exelUtil.setRowHeight(sheet, 0, 630);

            for (int col = 0; col < size; col++) {
                Object[] infor = headerInfors.get(col);
                String label = (String) infor[0];
                Integer width = (Integer) infor[1];
                exelUtil.createCellObject(sheet, col, 1, label, csHeader);
                sheet.setColumnWidth(col, width);
            }
            exelUtil.setRowHeight(sheet, 1, 600);

//            int index = 1;
            int row = 2;
            for (List<String> rowData : rowDatas) {
                int col = -1;
                for (String data : rowData) {
                    exelUtil.createCellObject(sheet, ++col, row, data, csLeft);
                }
                row++;
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    //tuanda38_export excel_1/12/2017_end

    //tuanda38_20180914_map param alarm_start
    public void buildParamInTemplate() {
        try {
            lstParamInputInTemplates = new ArrayList<>();
            lstParamAlarmInTempates = new ArrayList<>();
            lstParamNodeAlarmInTempates = new ArrayList<>();
            mapParamAlarmInTemplates.clear();
            systemTypeMapParam = MessageUtil.getResourceBundleMessage("label.category.domain.system.type.nocpro");
            categoryGroupDomains = new ArrayList<>();
            categoryDomains = new ArrayList<>();
            categoryConfigGetNodeGroups = new ArrayList<>();
            categoryConfigGetNodeGroup = null;
            categoryGroupDomain = null;
            categoryConfigGetNode = null;
            afterSystemTypeSelected();
            HashMap<String, Object> filters = new HashMap<>();
            //Quytv7_lay tham so node mang start
            filters.clear();
            filters.put("groupId", 3l);
            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            orders.put("groupId", "ASC");
            List<CategoryConfigGetNode> categoryConfigGetNodes = new CategoryConfigGetNodeServiceImpl().findList(filters, orders);
            HashMap<Long, CategoryConfigGetNode> mapGroupConfigGetNode = new HashMap<>();

            for (CategoryConfigGetNode object : categoryConfigGetNodes) {
                if (!mapGroupConfigGetNode.containsKey(object.getGroupId())) {
                    mapGroupConfigGetNode.put(object.getGroupId(), object);
                    ComboBoxObject boxObject = new ComboBoxObject();
                    boxObject.setLabel(object.getConfigName());
                    boxObject.setValue(object.getGroupId().toString());
                    categoryConfigGetNodeGroups.add(boxObject);
                }
            }
            filters.clear();
            filters.put("action.actionId", selectedAction.getActionId());
            filters.put("paramType", 1L);
            lstParamNodeAlarmInTempates = new FlowTemplateMapAlarmServiceImpl().findList(filters);
            if (lstParamNodeAlarmInTempates != null && lstParamNodeAlarmInTempates.size() > 0) {
                if (categoryGroupDomain == null || systemTypeMapParam == null && lstParamNodeAlarmInTempates.get(0).getDomain() != null) {
                    categoryGroupDomain = lstParamNodeAlarmInTempates.get(0).getDomain().getGroupDomain();
                    systemTypeMapParam = lstParamNodeAlarmInTempates.get(0).getDomain().getSystemType();
                }
                categoryConfigGetNode = lstParamNodeAlarmInTempates.get(0).getConfigGetNode();
                categoryConfigGetNodeGroup = new ComboBoxObject();
                categoryConfigGetNodeGroup.setValue(lstParamNodeAlarmInTempates.get(0).getConfigGetNode().getGroupId().toString());
                categoryConfigGetNodeGroup.setLabel(lstParamNodeAlarmInTempates.get(0).getConfigGetNode().getConfigName());
            }
            getListCategoryDomain(false);
//            Quytv7_lay tham so node mang end
            filters.clear();
            filters.put("action.actionId", selectedAction.getActionId());
            filters.put("paramType", 0L);
            List<FlowTemplateMapAlarm> lstTemp = new FlowTemplateMapAlarmServiceImpl().findList(filters);

            ConcurrentHashMap<String, String> mapTemp = new ConcurrentHashMap<>();
            if (lstTemp != null && lstTemp.size() > 0) {

                for (FlowTemplateMapAlarm flowTemplateMapAlarm1 : lstTemp) {

                    if (!mapParamAlarmInTemplates.containsKey(flowTemplateMapAlarm1.getParamInput().getParamCode().toLowerCase().trim())) {
                        mapParamAlarmInTemplates.put(flowTemplateMapAlarm1.getParamCode().toLowerCase().trim(), flowTemplateMapAlarm1);
                    }
                }
            }

            if (selectedAction != null) {
                try {
                    if (selectedAction.getActionDetails() == null || selectedAction.getActionDetails().isEmpty()) {
                        logger.error("Error no commands are not declared");
                        return;
                    }

                    ActionDetail actionDetail = selectedAction.getActionDetails().get(0);
                    List<ActionCommand> actionCommands = actionDetail.getActionCommands();
                    if (actionCommands == null || actionCommands.isEmpty()) {
                        logger.error("Error no commands are not declared in detail");
                        return;
                    }
                    for (ActionCommand actionCommand : actionCommands) {
                        lstParamInputInTemplates.addAll(actionCommand.getCommandDetail().getParamInputs());
                        List<ParamInput> paramInputs = actionCommand.getCommandDetail().getParamInputs();
                        for (ParamInput paramInput : paramInputs) {
                            mapTemp.put(paramInput.getParamCode().toLowerCase().trim(), paramInput.getParamCode().toLowerCase().trim());
                            if (!mapParamAlarmInTemplates.containsKey(paramInput.getParamCode().toLowerCase().trim())) {
                                FlowTemplateMapAlarm flowTemplateMapAlarm = new FlowTemplateMapAlarm();
                                flowTemplateMapAlarm.setParamInput(paramInput);
                                flowTemplateMapAlarm.setParamCode(paramInput.getParamCode());
                                mapParamAlarmInTemplates.put(paramInput.getParamCode().toLowerCase().trim(), flowTemplateMapAlarm);
                                lstParamAlarmInTempates.add(flowTemplateMapAlarm);
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }


            for (FlowTemplateMapAlarm flowTemplateMapAlarm1 : lstTemp) {
                if (mapTemp.containsKey(flowTemplateMapAlarm1.getParamCode().toLowerCase().trim())) {
                    lstParamAlarmInTempates.add(flowTemplateMapAlarm1);
                }
            }

            Collections.sort(lstParamAlarmInTempates, new Comparator<FlowTemplateMapAlarm>() {
                @Override
                public int compare(final FlowTemplateMapAlarm object1, final FlowTemplateMapAlarm object2) {
                    return object1.getParamCode().compareTo(object2.getParamCode());
                }
            });
            RequestContext.getCurrentInstance().execute("PF('dlgMapParamAlarm').show()");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void afterSystemTypeSelected() {
        try {
            if (systemTypeMapParam != null && !"".equalsIgnoreCase(systemTypeMapParam)) {
                LinkedHashMap<String, String> orders = new LinkedHashMap<>();
                orders.put("groupName", "ASC");
                HashMap<String, Object> filters = new HashMap<>();
                filters.put("systemType", systemTypeMapParam.toLowerCase());
                categoryGroupDomains = new CategoryGroupDomainServiceImpl().findList(filters, orders);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void getListCategoryDomain(boolean isReload) {
        try {
            if (categoryGroupDomain != null && categoryGroupDomain.getGroupName() != null) {
                LinkedHashMap<String, String> orders = new LinkedHashMap<>();
                orders.put("domainCode", "ASC");
                HashMap<String, Object> filters = new HashMap<>();
                filters.put("groupDomain.id", categoryGroupDomain.getId());
                categoryDomains = new CategoryDomainServiceImpl().findList(filters, orders);
            }
            if (isReload) {
                if (categoryGroupDomain == null) {
                    categoryDomains.clear();
                }
                for (FlowTemplateMapAlarm flowTemplateMapAlarmTemp : lstParamNodeAlarmInTempates) {
                    flowTemplateMapAlarmTemp.setDomain(null);
                }
                for (FlowTemplateMapAlarm flowTemplateMapAlarmTemp : lstParamAlarmInTempates) {
                    flowTemplateMapAlarmTemp.setDomain(null);
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void getListParamNodes() {
        try {
            HashMap<String, Object> filters = new HashMap<>();
            if (categoryConfigGetNodeGroup != null) {
                if (categoryConfigGetNode != null && categoryConfigGetNodeGroup.getValue().equalsIgnoreCase(categoryConfigGetNode.getGroupId().toString())) {

                    filters.clear();
                    filters.put("action.actionId", selectedAction.getActionId());
                    filters.put("paramType", 1L);
                    lstParamNodeAlarmInTempates = new FlowTemplateMapAlarmServiceImpl().findList(filters);
                } else {
                    lstParamNodeAlarmInTempates.clear();
                    filters.clear();
                    filters.put("groupId", categoryConfigGetNodeGroup.getValue());
                    List<CategoryConfigGetNode> categoryConfigGetNodes = new CategoryConfigGetNodeServiceImpl().findList(filters);
                    FlowTemplateMapAlarm flowTemplateMapAlarmTemp;
                    for (CategoryConfigGetNode categoryConfigGetNode1 : categoryConfigGetNodes) {
                        flowTemplateMapAlarmTemp = new FlowTemplateMapAlarm();
                        flowTemplateMapAlarmTemp.setParamCode(categoryConfigGetNode1.getParamName());
                        flowTemplateMapAlarmTemp.setRegex(categoryConfigGetNode1.getRegexDefault());
                        flowTemplateMapAlarmTemp.setConfigGetNode(categoryConfigGetNode1);
                        lstParamNodeAlarmInTempates.add(flowTemplateMapAlarmTemp);
                    }
                }
            } else {
                lstParamNodeAlarmInTempates.clear();
//                filters.clear();
//                filters.put("action.actionId",selectedAction.getActionId());
//                filters.put("paramType", 1L);
//                lstParamNodeAlarmInTempates = new FlowTemplateMapAlarmServiceImpl().findList(filters);
//                if(lstParamNodeAlarmInTempates != null && !lstParamNodeAlarmInTempates.isEmpty()){
//
//                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void onSaveOrUpdateMapParamAlarm() {
        try {
            List<FlowTemplateMapAlarm> lstFinal = new ArrayList<>();
            Date startTime = new Date();
            boolean check = true;
            HashMap<String, Object> filters = new HashMap<>();
            filters.put("flowTemplateName", "ITBUSINESS");
            FlowTemplates flowTemplates = null;
            List<FlowTemplates> flowTemplateLst = new FlowTemplatesServiceImpl().findList(filters);
            if (flowTemplateLst != null) {
                flowTemplates = flowTemplateLst.get(0);
            }
            if (!lstParamNodeAlarmInTempates.isEmpty()) {
                for (FlowTemplateMapAlarm flowTemplateMapAlarm : lstParamNodeAlarmInTempates) {
                    if (flowTemplateMapAlarm.getDomain() == null) {
                        MessageUtil.setErrorMessageFromRes("message.error.is.null.param.get.node.alarm.domain");
                        return;
                    }
                    flowTemplateMapAlarm.setAction(selectedAction);
                    flowTemplateMapAlarm.setFlowTemplates(flowTemplates);
                    flowTemplateMapAlarm.setCreateUser(SessionWrapper.getCurrentUsername());
                    flowTemplateMapAlarm.setUpdateTime(new Date());
                    flowTemplateMapAlarm.setParamType(1L);
                    flowTemplateMapAlarm.setId(null);
                    lstFinal.add(flowTemplateMapAlarm);
                }
            } else {
                filters.clear();
                filters.put("action.actionId", selectedAction.getActionId());
                filters.put("paramType", 1L);
                lstParamNodeAlarmInTempates = new FlowTemplateMapAlarmServiceImpl().findList(filters);
                if (lstParamNodeAlarmInTempates != null && !lstParamNodeAlarmInTempates.isEmpty()) {
                    flowTemplateMapAlarmService.delete(lstParamNodeAlarmInTempates);
                }
//                MessageUtil.setErrorMessageFromRes("message.error.is.null.param.get.node.alarm");
//                return;
            }
            if (!lstParamAlarmInTempates.isEmpty()) {
                for (FlowTemplateMapAlarm flowTemplateMapAlarm : lstParamAlarmInTempates) {
                    flowTemplateMapAlarm.setAction(selectedAction);
                    flowTemplateMapAlarm.setFlowTemplates(flowTemplates);
                    flowTemplateMapAlarm.setCreateUser(SessionWrapper.getCurrentUsername());
                    flowTemplateMapAlarm.setUpdateTime(new Date());
                    flowTemplateMapAlarm.setParamType(0L);
                    flowTemplateMapAlarm.setId(null);
                    lstFinal.add(flowTemplateMapAlarm);
                }
            }
            Object[] objs = new FlowTemplateMapAlarmServiceImpl().openTransaction();
            Session session = (Session) objs[0];
            Transaction tx = (Transaction) objs[1];
//            if (!lstParamAlarmInTempates.isEmpty() && selectedFlowTemplate != null && selectedFlowTemplate.getFlowTemplatesId() != null) {
            if (selectedAction != null && selectedAction.getActionId() != null) {
                try {
                    new FlowTemplateMapAlarmServiceImpl().execteBulk2("delete from FlowTemplateMapAlarm where action.actionId = ?", session, tx, false, selectedAction.getActionId());
                    new FlowTemplateMapAlarmServiceImpl().saveOrUpdate(lstFinal, session, tx, true);
//                    new FlowTemplateMapAlarmServiceImpl().saveOrUpdate(lstParamAlarmInTempates);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.CREATE,
                                lstFinal.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                    MessageUtil.setInfoMessageFromRes("info.save.success");
                    RequestContext.getCurrentInstance().execute("PF('dlgMapParamAlarm').hide()");
                } catch (Exception ex) {
                    logger.info(ex.getMessage(), ex);
                } finally {
                    if (tx.getStatus() != TransactionStatus.ROLLED_BACK && tx.getStatus() != TransactionStatus.COMMITTED) {
                        tx.rollback();
                    }
                    if (session.isOpen()) {
                        session.close();
                    }
                }
            } else {
                MessageUtil.setInfoMessage("Data is null or flow template is null");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("error.save.unsuccess");
        }
    }

    public void addRegexDefault(FlowTemplateMapAlarm flowTemplateMapAlarm) {
        if (flowTemplateMapAlarm != null) {
            if ("system".equals(flowTemplateMapAlarm.getParamCode().toLowerCase().trim())) {
                flowTemplateMapAlarm.setRegex(Config.getConfigFromDB("system"));
            } else if ("is_response".equals(flowTemplateMapAlarm.getParamCode().toLowerCase().trim())) {
                flowTemplateMapAlarm.setRegex(Config.getConfigFromDB("is_response"));
            } else if ("monitor_id".equals(flowTemplateMapAlarm.getParamCode().toLowerCase().trim())) {
                flowTemplateMapAlarm.setRegex(Config.getConfigFromDB("monitor_id"));
            }
        }
    }

    //tuanda38_20180914_map param alarm_end

    public ActionDetailServiceImpl getActionDetailServiceImpl() {
        return actionDetailServiceImpl;
    }

    public void setActionDetailServiceImpl(ActionDetailServiceImpl actionDetailServiceImpl) {
        this.actionDetailServiceImpl = actionDetailServiceImpl;
    }

    public Action getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(Action selectedAction) {
        this.selectedAction = selectedAction;
    }

    public VendorServiceImpl getVendorServiceImpl() {
        return vendorServiceImpl;
    }

    public void setVendorServiceImpl(VendorServiceImpl vendorServiceImpl) {
        this.vendorServiceImpl = vendorServiceImpl;
    }

    public NodeTypeServiceImpl getNodeTypeServiceImpl() {
        return nodeTypeServiceImpl;
    }

    public void setNodeTypeServiceImpl(NodeTypeServiceImpl nodeTypeServiceImpl) {
        this.nodeTypeServiceImpl = nodeTypeServiceImpl;
    }

    public Vendor getSelectedVendor() {
        return selectedVendor;
    }

    public void setSelectedVendor(Vendor selectedVendor) {
        this.selectedVendor = selectedVendor;
    }

    public NodeType getSelectedNodeType() {
        return selectedNodeType;
    }

    public void setSelectedNodeType(NodeType selectedNodeType) {
        this.selectedNodeType = selectedNodeType;
    }

    public ActionDetail getActionDetail() {
        return actionDetail;
    }

    public void setActionDetail(ActionDetail actionDetail) {
        this.actionDetail = actionDetail;
    }

    public CommandTelnetParserServiceImpl getCommandTelnetParserServiceImpl() {
        return commandTelnetParserServiceImpl;
    }

    public void setCommandTelnetParserServiceImpl(
            CommandTelnetParserServiceImpl commandTelnetParserServiceImpl) {
        this.commandTelnetParserServiceImpl = commandTelnetParserServiceImpl;
    }

    public List<ActionDetail> getLstActionDetail() {
        return lstActionDetail;
    }

    public void setLstActionDetail(List<ActionDetail> lstActionDetail) {
        this.lstActionDetail = lstActionDetail;
    }

    public ActionDetail getSelectedActionDetail() {
        return selectedActionDetail;
    }

    public void setSelectedActionDetail(ActionDetail selectedActionDetail) {
        this.selectedActionDetail = selectedActionDetail;
    }

    public CommandDetail getSelectedCmdDetail() {
        return selectedCmdDetail;
    }

    public void setSelectedCmdDetail(CommandDetail selectedCmdDetail) {
        this.selectedCmdDetail = selectedCmdDetail;
    }

    public CommandTelnetParser getSelectedCmdTelnetParser() {
        return selectedCmdTelnetParser;
    }

    public void setSelectedCmdTelnetParser(CommandTelnetParser selectedCmdTelnetParser) {
        this.selectedCmdTelnetParser = selectedCmdTelnetParser;
    }

    public List<ActionCommand> getLstActionCommand() {
        return lstActionCommand;
    }

    public void setLstActionCommand(List<ActionCommand> lstActionCommand) {
        this.lstActionCommand = lstActionCommand;
    }

    public Long getStationDetailStatus() {
        return stationDetailStatus;
    }

    public void setStationDetailStatus(Long stationDetailStatus) {
        this.stationDetailStatus = stationDetailStatus;
    }

    public boolean isEditActionDetail() {
        return isEditActionDetail;
    }

    public void setEditActionDetail(boolean isEditActionDetail) {
        this.isEditActionDetail = isEditActionDetail;
    }

    public ActionCommand getSelectedActionCommand() {
        return selectedActionCommand;
    }

    public void setSelectedActionCommand(ActionCommand selectedActionCommand) {
        this.selectedActionCommand = selectedActionCommand;
    }

    public List<ActionCommand> getLstActionCmdDel() {
        return lstActionCmdDel;
    }

    public void setLstActionCmdDel(List<ActionCommand> lstActionCmdDel) {
        this.lstActionCmdDel = lstActionCmdDel;
    }

    public Version getSelectedVersion() {
        return selectedVersion;
    }

    public void setSelectedVersion(Version selectedVersion) {
        this.selectedVersion = selectedVersion;
    }

    public String getKeyActionSearch() {
        return keyActionSearch;
    }

    public void setKeyActionSearch(String keyActionSearch) {
        this.keyActionSearch = keyActionSearch;
    }

    public boolean isClone() {
        return isClone;
    }

    public void setClone(boolean isClone) {
        this.isClone = isClone;
    }

    public String getActionNameToClone() {
        return actionNameToClone;
    }

    public void setActionNameToClone(String actionNameToClone) {
        this.actionNameToClone = actionNameToClone;
    }

    public DualListModel<ItUsers> getUsersPicklist() {
        return usersPicklist;
    }

    public void setUsersPicklist(DualListModel<ItUsers> usersPicklist) {
        this.usersPicklist = usersPicklist;
    }

    public List<CommandDetail> getCommandDetails() {
        return commandDetails;
    }

    public void setCommandDetails(List<CommandDetail> commandDetails) {
        this.commandDetails = commandDetails;
    }

    public LazyDataModelBaseNew<ItActionLog, Long> getLazyActionLog() {
        return lazyActionLog;
    }

    public void setLazyActionLog(LazyDataModelBaseNew<ItActionLog, Long> lazyActionLog) {
        this.lazyActionLog = lazyActionLog;
    }

    public ItNodeServiceImpl getItNodeService() {
        return itNodeService;
    }

    public void setItNodeService(ItNodeServiceImpl itNodeService) {
        this.itNodeService = itNodeService;
    }

    public LazyDataModel<Node> getNodeList() {
        return nodeList;
    }

    public void setNodeList(LazyDataModel<Node> nodeList) {
        this.nodeList = nodeList;
    }

    public Node getSelectedItNode() {
        return selectedItNode;
    }

    public void setSelectedItNode(Node selectedItNode) {
        this.selectedItNode = selectedItNode;
    }

    public ItNodeActionServiceImpl getItNodeActionService() {
        return itNodeActionService;
    }

    public void setItNodeActionService(ItNodeActionServiceImpl itNodeActionService) {
        this.itNodeActionService = itNodeActionService;
    }

    public List<ItNodeAction> getNodeActionList() {
        return nodeActionList;
    }

    public void setNodeActionList(List<ItNodeAction> nodeActionList) {
        this.nodeActionList = nodeActionList;
    }

    public String getUserNode() {
        return userNode;
    }

    public void setUserNode(String userNode) {
        this.userNode = userNode;
    }

    public String getPassNode() {
        return passNode;
    }

    public void setPassNode(String passNode) {
        this.passNode = passNode;
    }

    public ItActionLogServiceImpl getItActionLogServiceImpl() {
        return itActionLogServiceImpl;
    }

    public void setItActionLogServiceImpl(ItActionLogServiceImpl itActionLogServiceImpl) {
        this.itActionLogServiceImpl = itActionLogServiceImpl;
    }

    public ItUserBusGroupServiceImpl getItUserBusGroupService() {
        return itUserBusGroupService;
    }

    public void setItUserBusGroupService(ItUserBusGroupServiceImpl itUserBusGroupService) {
        this.itUserBusGroupService = itUserBusGroupService;
    }

    public TreeNode getRootNode2() {
        return rootNode2;
    }

    public void setRootNode2(TreeNode rootNode2) {
        this.rootNode2 = rootNode2;
    }

    public StreamedContent getFileCommandLog() {
        return fileCommandLog;
    }

    public void setFileCommandLog(StreamedContent fileCommandLog) {
        this.fileCommandLog = fileCommandLog;
    }

    public ItCommandLogServiceImpl getItCommandLogService() {
        return itCommandLogService;
    }

    public void setItCommandLogService(ItCommandLogServiceImpl itCommandLogService) {
        this.itCommandLogService = itCommandLogService;
    }

    public List<ItCommandLog> getCommandLogList() {
        return commandLogList;
    }

    public void setCommandLogList(List<ItCommandLog> commandLogList) {
        this.commandLogList = commandLogList;
    }

    public ItUsersServicesImpl getItUsersServices() {
        return itUsersServices;
    }

    public void setItUsersServices(ItUsersServicesImpl itUsersServices) {
        this.itUsersServices = itUsersServices;
    }

    public ItUserActionServiceImpl getItUserActionService() {
        return itUserActionService;
    }

    public void setItUserActionService(ItUserActionServiceImpl itUserActionService) {
        this.itUserActionService = itUserActionService;
    }

    public List<TreeNode> getLstParentNode() {
        return lstParentNode;
    }

    public void setLstParentNode(List<TreeNode> lstParentNode) {
        this.lstParentNode = lstParentNode;
    }

    public ArrayList<Long> getLstParentId() {
        return lstParentId;
    }

    public void setLstParentId(ArrayList<Long> lstParentId) {
        this.lstParentId = lstParentId;
    }

    public ArrayList<Action> getListActionMD() {
        return listActionMD;
    }

    public void setListActionMD(ArrayList<Action> listActionMD) {
        this.listActionMD = listActionMD;
    }

    public List<Action> getResultSearch() {
        return resultSearch;
    }

    public void setResultSearch(List<Action> resultSearch) {
        this.resultSearch = resultSearch;
    }

    public NodeType getNodeDefault() {
        return nodeDefault;
    }

    public void setNodeDefault(NodeType nodeDefault) {
        this.nodeDefault = nodeDefault;
    }

    public Version getVersionDefault() {
        return versionDefault;
    }

    public void setVersionDefault(Version versionDefault) {
        this.versionDefault = versionDefault;
    }

    public VersionServiceImpl getVersionServiceImpl() {
        return versionServiceImpl;
    }

    public void setVersionServiceImpl(VersionServiceImpl versionServiceImpl) {
        this.versionServiceImpl = versionServiceImpl;
    }

    public Action getVerifyActtion() {
        return verifyActtion;
    }

    public void setVerifyActtion(Action verifyActtion) {
        this.verifyActtion = verifyActtion;
    }

    public ItNodeAction getItNodeActionSelected() {
        return itNodeActionSelected;
    }

    public void setItNodeActionSelected(ItNodeAction itNodeActionSelected) {
        this.itNodeActionSelected = itNodeActionSelected;
    }

    public List<NodeAccount> getNodeAccounts() {
        return nodeAccounts;
    }

    public void setNodeAccounts(List<NodeAccount> nodeAccounts) {
        this.nodeAccounts = nodeAccounts;
    }

    public NodeAccount getNodeAccountSelected() {
        return nodeAccountSelected;
    }

    public void setNodeAccountSelected(NodeAccount nodeAccountSelected) {
        this.nodeAccountSelected = nodeAccountSelected;
    }

    public Map<String, ParamInput> getMapParamVals() {
        return mapParamVals;
    }

    public void setMapParamVals(Map<String, ParamInput> mapParamVals) {
        this.mapParamVals = mapParamVals;
    }

    public List<CmdObject> getImpactCmdsObj() {
        return impactCmdsObj;
    }

    public void setImpactCmdsObj(List<CmdObject> impactCmdsObj) {
        this.impactCmdsObj = impactCmdsObj;
    }

    public List<CmdObject> getWriteLogCmdsObj() {
        return writeLogCmdsObj;
    }

    public void setWriteLogCmdsObj(List<CmdObject> writeLogCmdsObj) {
        this.writeLogCmdsObj = writeLogCmdsObj;
    }

    public boolean isImportSucess() {
        return isImportSucess;
    }

    public void setImportSucess(boolean importSucess) {
        isImportSucess = importSucess;
    }

    public ItActionLog getSelectedActionLog() {
        return selectedActionLog;
    }

    public void setSelectedActionLog(ItActionLog selectedActionLog) {
        this.selectedActionLog = selectedActionLog;
    }

    public ItCommandLog getSelectedCmdLog() {
        return selectedCmdLog;
    }

    public void setSelectedCmdLog(ItCommandLog selectedCmdLog) {
        this.selectedCmdLog = selectedCmdLog;
    }

    public List<List<String>> getSqlDataTables() {
        return sqlDataTables;
    }

    public void setSqlDataTables(List<List<String>> sqlDataTables) {
        this.sqlDataTables = sqlDataTables;
    }

    public Integer getRescueCycle() {
        return rescueCycle;
    }

    public void setRescueCycle(Integer rescueCycle) {
        this.rescueCycle = rescueCycle;
    }

    public Date getStartTime() {
        return startTime;
    }

    public void setStartTime(Date startTime) {
        this.startTime = startTime;
    }

    public Date getEndTime() {
        return endTime;
    }

    public void setEndTime(Date endTime) {
        this.endTime = endTime;
    }

    public Integer getOpenBlockingType() {
        return openBlockingType;
    }

    public void setOpenBlockingType(Integer openBlockingType) {
        this.openBlockingType = openBlockingType;
    }

    public Integer getNumOfThread() {
        return numOfThread;
    }

    public void setNumOfThread(Integer numOfThread) {
        this.numOfThread = numOfThread;
    }

    public ExcelWriterUtils getExelUtil() {
        return exelUtil;
    }

    public void setExelUtil(ExcelWriterUtils exelUtil) {
        this.exelUtil = exelUtil;
    }


    public TemplateGroup getSelectTemplateGroup() {
        return selectTemplateGroup;
    }

    public void setSelectTemplateGroup(TemplateGroup selectTemplateGroup) {
        this.selectTemplateGroup = selectTemplateGroup;
    }

    public List<CategoryDomain> getCategoryDomains() {
        return categoryDomains;
    }

    public void setCategoryDomains(List<CategoryDomain> categoryDomains) {
        this.categoryDomains = categoryDomains;
    }

    public String getSystemTypeMapParam() {
        return systemTypeMapParam;
    }

    public void setSystemTypeMapParam(String systemTypeMapParam) {
        this.systemTypeMapParam = systemTypeMapParam;
    }

    public CategoryGroupDomain getCategoryGroupDomain() {
        return categoryGroupDomain;
    }

    public void setCategoryGroupDomain(CategoryGroupDomain categoryGroupDomain) {
        this.categoryGroupDomain = categoryGroupDomain;
    }

    public List<CategoryGroupDomain> getCategoryGroupDomains() {
        return categoryGroupDomains;
    }

    public void setCategoryGroupDomains(List<CategoryGroupDomain> categoryGroupDomains) {
        this.categoryGroupDomains = categoryGroupDomains;
    }

    public CategoryConfigGetNode getCategoryConfigGetNode() {
        return categoryConfigGetNode;
    }

    public void setCategoryConfigGetNode(CategoryConfigGetNode categoryConfigGetNode) {
        this.categoryConfigGetNode = categoryConfigGetNode;
    }

    public List<ComboBoxObject> getCategoryConfigGetNodeGroups() {
        return categoryConfigGetNodeGroups;
    }

    public void setCategoryConfigGetNodeGroups(List<ComboBoxObject> categoryConfigGetNodeGroups) {
        this.categoryConfigGetNodeGroups = categoryConfigGetNodeGroups;
    }

    public ComboBoxObject getCategoryConfigGetNodeGroup() {
        return categoryConfigGetNodeGroup;
    }

    public void setCategoryConfigGetNodeGroup(ComboBoxObject categoryConfigGetNodeGroup) {
        this.categoryConfigGetNodeGroup = categoryConfigGetNodeGroup;
    }

    public List<FlowTemplateMapAlarm> getLstParamNodeAlarmInTempates() {
        return lstParamNodeAlarmInTempates;
    }

    public void setLstParamNodeAlarmInTempates(List<FlowTemplateMapAlarm> lstParamNodeAlarmInTempates) {
        this.lstParamNodeAlarmInTempates = lstParamNodeAlarmInTempates;
    }

    public List<FlowTemplateMapAlarm> getLstParamAlarmInTempates() {
        return lstParamAlarmInTempates;
    }

    public void setLstParamAlarmInTempates(List<FlowTemplateMapAlarm> lstParamAlarmInTempates) {
        this.lstParamAlarmInTempates = lstParamAlarmInTempates;
    }

    public ConcurrentHashMap<String, FlowTemplateMapAlarm> getMapParamAlarmInTemplates() {
        return mapParamAlarmInTemplates;
    }

    public void setMapParamAlarmInTemplates(ConcurrentHashMap<String, FlowTemplateMapAlarm> mapParamAlarmInTemplates) {
        this.mapParamAlarmInTemplates = mapParamAlarmInTemplates;
    }

    public List<ParamInput> getLstParamInputInTemplates() {
        return lstParamInputInTemplates;
    }

    public void setLstParamInputInTemplates(List<ParamInput> lstParamInputInTemplates) {
        this.lstParamInputInTemplates = lstParamInputInTemplates;
    }

    public FlowTemplateMapAlarmServiceImpl getFlowTemplateMapAlarmService() {
        return flowTemplateMapAlarmService;
    }

    public void setFlowTemplateMapAlarmService(FlowTemplateMapAlarmServiceImpl flowTemplateMapAlarmService) {
        this.flowTemplateMapAlarmService = flowTemplateMapAlarmService;
    }

    /*20190109_hoangnd_them thi truong_start*/
    public ItBusGroupServiceImpl getItBusGroupService() {
        return itBusGroupService;
    }

    public void setItBusGroupService(ItBusGroupServiceImpl itBusGroupService) {
        this.itBusGroupService = itBusGroupService;
    }

    public Integer getTreeNodeLevel() {
        return treeNodeLevel;
    }

    public void setTreeNodeLevel(Integer treeNodeLevel) {
        this.treeNodeLevel = treeNodeLevel;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }
    /*20190109_hoangnd_them thi truong_end*/
}
