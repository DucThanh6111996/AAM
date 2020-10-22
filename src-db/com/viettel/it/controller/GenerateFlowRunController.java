package com.viettel.it.controller;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.rits.cloning.Cloner;
import com.viettel.controller.Module;
import com.viettel.gnoc.cr.CrForOtherSystemService;
import com.viettel.gnoc.cr.CrForOtherSystemServiceImplService;
import com.viettel.gnoc.cr.CrForOtherSystemServiceImplServiceLocator;
import com.viettel.gnoc.cr.ResultDTO;
import com.viettel.controller.AppException;
import com.viettel.controller.SysException;
import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.lazy.LazyDataModelSearchNode;
import com.viettel.it.model.*;
import com.viettel.it.model.ParamChecklistDatabase;
import com.viettel.it.object.*;
import com.viettel.it.persistence.*;
import com.viettel.it.persistence.common.ConditionQuery;
import com.viettel.it.persistence.common.OrderBy;
import com.viettel.it.util.*;
import com.viettel.model.ImpactProcess;
import com.viettel.persistence.ActionServiceImpl;
import com.viettel.persistence.IimService;
import com.viettel.controller.AamConstants;
import com.viettel.util.Constant;
import com.viettel.util.SessionUtil;
import com.viettel.util.SessionWrapper;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.RegionUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.joda.time.DateTime;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.*;
import org.primefaces.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.net.URLEncoder;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

/**
 * @author huynx6
 * @version 1.0.0
 * @since Sep 7, 2016
 */
@SuppressWarnings("serial")
@ViewScoped
@ManagedBean
public class GenerateFlowRunController implements Serializable {

    protected static final Logger logger = LoggerFactory.getLogger(GenerateFlowRunController.class);

    private List<SelectItem> crs = new ArrayList<SelectItem>();
    private List<FlowTemplates> flowTemplates;
    private FlowTemplates selectedFlowTemplates;
    private List<Node> nodes;
    private Node selectedNode;

    /*20180705_hoangnd_fix bug da ngon ngu_start*/
    @ManagedProperty(value = "#{language}")
    LanguageBean languageBean;

    public LanguageBean getLanguageBean() {
        return languageBean;
    }

    public void setLanguageBean(LanguageBean languageBean) {
        this.languageBean = languageBean;
    }

    private String locate;
    /*20180705_hoangnd_fix bug da ngon ngu_end*/

    @ManagedProperty("#{nodeAccountService}")
    private NodeAccountServiceImpl nodeAccountService;

    @ManagedProperty("#{flowTemplatesService}")
    private FlowTemplatesServiceImpl flowTemplatesService;

    @ManagedProperty("#{nodeService}")
    private NodeServiceImpl nodeService;

    Map<Node, List<ParamInput>> mapParam = new HashMap<Node, List<ParamInput>>();
    Map<Node, List<ParamValue>> mapParamValue = new HashMap<Node, List<ParamValue>>();
    private Map<Node, List<GroupAction>> mapGroupAction = new HashMap<Node, List<GroupAction>>();
    private Map<Node, Map<String, NodeAccount>> mapAccGroupAction = new HashMap<Node, Map<String, NodeAccount>>();
    Map<Node, HashMap<String, ParamInput>> mapParamInOut = new HashMap<>();
    private Map<String, List<Node>> mapSubFlowRunNodes = new LinkedHashMap<String, List<Node>>();
    private List<UploadedFile> lstUploadedFile = new LinkedList<>();

    private FlowRunAction flowRunAction = new FlowRunAction();

    private LinkedListMultimap<String, ActionOfFlow> groupActions = LinkedListMultimap.create();

    private LazyDataModel<FlowRunAction> lazyFlowRunAction;

    private LazyDataModel<Node> lazyNode;
    private List<Node> preNodes = new ArrayList<Node>();
    private List<Node> tmpNodes = new ArrayList<Node>();
    private List<Node> nodeSeachs = new ArrayList<Node>();
    private boolean rerender = true;

    private List<NodeType> nodeType4Searchs = new ArrayList<NodeType>();
    private List<Vendor> vendor4Searchs = new ArrayList<Vendor>();
    private List<Version> version4Searchs = new ArrayList<Version>();

    @ManagedProperty("#{flowRunActionService}")
    private GenericDaoServiceNewV2<FlowRunAction, Long> flowRunActionService;

    private boolean isTabExecute = false;

    private Integer searchNodeLab = 0;

    private List<NodeAccount> lstNodeAccount = new ArrayList<>(); // danh sach account node mang
    Map<String, Object> caches = new HashMap<>();

    private com.viettel.model.Action action = new com.viettel.model.Action();

    public void setIimService(IimService iimService) {
        this.iimService = iimService;
    }

    @ManagedProperty(value = "#{iimService}")
    IimService iimService;
    @ManagedProperty(value = "#{mapUserCountryService}")
    private MapUserCountryServiceImpl mapUserCountryService;
    //20181119_tudn_start them danh sach lenh blacklist
    private boolean sureMopContainCmdBlacklist = false;
    private FlowRunAction cloneFlowRunAction;
    private String blackListCommand;
    private String messageBlacklist;
	//20190408_chuongtq start check param when create MOP
    LinkedHashMap<String, CheckParamCondition> mapCheckParamCondition = new LinkedHashMap<>();
    private boolean checkConfigCondition = false;
    Map<Node,List<ParamValue>> mapParamValueForExport = new HashMap<Node, List<ParamValue>>();
    private boolean btnSave;
    private boolean addNew;
    private com.viettel.model.Action action1;
    private com.viettel.model.Action action2;
    private ImpactProcess impactProcess;
    private boolean createMultipleMop ;
    boolean isSaveMapNodeFile = true;
    private String fileTemplateDt="";
    private boolean saveCheckMopDT;
    private String OK = "OK";
    private String NOK = "NOK";
    private int limitedNodeConfig;
    private String logAction = "";
    private String className = GenerateFlowRunController.class.getName();
    private boolean nodeAccount ;
    private String rsUpload = "";
    public boolean isCreateMultipleMop() {
        return createMultipleMop;
    }

    public void setCreateMultipleMop(boolean createMultipleMop) {
        this.createMultipleMop = createMultipleMop;
    }

    private List<Boolean> columnVisibale =new ArrayList<>();

    public void onToggler(ToggleEvent e){
        this.columnVisibale.set((Integer) e.getData(),e.getVisibility() == Visibility.VISIBLE);
    }

    public void setColumnVisibale(List<Boolean> columnVisibale) {
        this.columnVisibale = columnVisibale;
    }

    public List<Boolean> getColumnVisibale() {
        return columnVisibale;
    }
    //20190408_chuongtq end check param when create MOP
    //20181119_tudn_end them danh sach lenh blacklist
    @PostConstruct
    public void onStart() {
        //20181119_tudn_start them danh sach lenh blacklist
        sureMopContainCmdBlacklist = false;
        //20181119_tudn_end them danh sach lenh blacklist
        Map<String, Object> _filters = new HashMap<String, Object>();
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        logger.info("url: " + context.getRequestMap().get("javax.servlet.forward.request_uri").toString());
        if (context.getRequestMap().get("javax.servlet.forward.request_uri").toString().endsWith("/execute")) {
            isTabExecute = true;
            _filters.put("status-GT", 0L);
        } else {
            isTabExecute = false;
        }
        List<Integer> listHiddenDefault = null ;
        if (isTabExecute) {
            listHiddenDefault = Arrays.asList(6,14,15,16,17);
        } else if (!isTabExecute) {
            listHiddenDefault = Arrays.asList(8,9,14,15,16,17);
        }
        for (int i = 0; i < 19; i++) {
            if (listHiddenDefault.contains(i)) {
                columnVisibale.add(false);
            } else {
                columnVisibale.add(true);
            }
        }

        flowRunAction = new FlowRunAction();
        caches.clear();
        nodes = new ArrayList<Node>();
        Map<String, Object> filters = new HashMap<String, Object>();
        filters.put("parentId", null);
        filters.put("status", 9);
        LinkedHashMap<String, String> orders = new LinkedHashMap<String, String>();
        orders.put("flowTemplateName", "ASC");

//		if (!new SessionUtil().isActionAdmin()) {
//			_filters.put("createBy", SessionWrapper.getCurrentUsername());
//		}

        try {
            //20190408_chuongtq start check param when create MOP
            checkConfigCondition = checkConfigCondition(AamConstants.CFG_CHK_PARAM_CONDITION_FOR_WEB);
            //20190408_chuongtq end check param when create MOP
//			List<String> lstCountry = mapUserCountryService.getListCountryForUser();
//			if (lstCountry != null && lstCountry.size() > 0) {
//				_filters.put("countryCode.countryCode-EXAC", lstCountry);
//			}
            //20180831_tudn_start cap nhat trang thai
            Map<String, Object> _filtersNode = new HashMap<String, Object>();
            _filtersNode.put("active", 1L);
            //20180831_tudn_end cap nhat trang thai

            flowTemplates = flowTemplatesService.findList(filters, orders);
            LinkedHashMap<String, String> _orders = new LinkedHashMap<String, String>();
            _orders.put("createDate", "DESC");
            lazyFlowRunAction = new LazyDataModelBaseNew<FlowRunAction, Long>(flowRunActionService, _filters, _orders);
            lazyNode = new LazyDataModelSearchNode<>(new NodeServiceImpl(), _filtersNode);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        loadCR();


    }

    public void reloadTemplates() {
        //new ActionController().prepareEdit(action);
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        request.getContextPath();

        Map<String, Object> filters = new HashMap<>();
        filters.put("parentId", null);
        filters.put("status", 9);
        if (flowRunAction.getExecuteType() != null && flowRunAction.getExecuteType() == 3)
            filters.put("templateType", 3);
        LinkedHashMap<String, String> orders = new LinkedHashMap<String, String>();
        orders.put("flowTemplateName", "ASC");

        try {
            flowTemplates = flowTemplatesService.findList(filters, orders);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void loadCR() {
        crs.clear();
        //crs =  CrUtil.getListCrNumber();
        //crs.add(0,new SelectItem(null, MessageUtil.getResourceBundleMessage("view.choose.cr"),null, false, true, true));
        crs.add(new SelectItem(Config.CR_DEFAULT, Config.CR_DEFAULT));

    }

    //// huynx6 added Sep 7, 2016
    public static void main(String[] args) {
        try {
            DateTime dt = new DateTime();
            dt = dt.plusHours(-4);
            SimpleDateFormat df = new SimpleDateFormat("yyyy-M-d-H");
            System.err.println(df.format(dt.toDate()));
//			GenerateFlowRunController controller = new GenerateFlowRunController();
//			FlowRunAction _flowRunAction = new FlowRunActionServiceImpl().findById(20L);
//			controller.cloneFlowRun(_flowRunAction);
            /*
			controller.setFlowTemplatesService(new FlowTemplatesServiceImpl());
			controller.onStart();
			controller.setSelectedFlowTemplates(controller.getFlowTemplates().get(0));
			controller.onChangeFlowTemplates();
			List<Node> _nodes = new ArrayList<Node>();
			_nodes = new NodeServiceImpl().findList();
			controller.setNodes(_nodes);
			controller.exportDt();
			/*
			List<ActionCommand> as = new ActionCommandServiceImpl().findList();
			System.err.println(as);
			/*
			GenerateFlowRunController controller = new GenerateFlowRunController();
			controller.setNodeService(new NodeServiceImpl());
			controller.completeNodeRun("a");

			Map<String, Object> filters = new HashMap<String, Object>();
			List<Action> ass = new ActionServiceImpl().findList(filters);
			System.err.println(ass);


			filters = new HashMap<String, Object>();
			//filters.put("parentId", null);
			LinkedHashMap<String, String> orders = new LinkedHashMap<String, String>();
			orders.put("flowTemplateName", "ASC");
			FlowTemplatesServiceImpl flowTemplatesServiceImpl = new FlowTemplatesServiceImpl();
			List<FlowTemplates> as = flowTemplatesServiceImpl.findList(filters, orders);
			//Session ass = flowTemplatesServiceImpl.getCurrentSession();

			System.err.println(as);
			/*
			OrderBy orderBy = new OrderBy();
			ConditionQuery query = new ConditionQuery();
			List<Criterion> predicates = new ArrayList<Criterion>();
			String node = "a";
			if(node!=null && !node.isEmpty())
				predicates.add(Restrictions.ilike("nodeCode", node, MatchMode.ANYWHERE));
			query.add(Restrictions.or(predicates.toArray(new Criterion[predicates.size()])));
			new NodeServiceImpl().findList(query, orderBy, 1, 20);
			*/

            Map<String, Object> filters = new HashMap<>();
            filters.put("actionOfFlowflowRunId", 12216);

            System.out.println(new NodeRunGroupActionServiceImpl().findList("from NodeRunGroupAction where id.flowRunId =?", -1, -1, 12320l).size());
        } catch (Exception e) {
            // TODO Auto-generated catch block
            logger.error(e.getMessage(), e);
        }
    }

    public boolean isEditMop() {
        return (flowRunAction.getStatus() == 0L
                || flowRunAction.getStatus() == 1L
                || flowRunAction.getStatus() == -1L);
    }

    public boolean isEditMopNew() {
        return (flowRunAction.getStatus() == 0L || flowRunAction.getStatus() == -1L);
    }

    public boolean isApproveMop() {
        return flowRunAction.getStatus() == 0L;
    }

    public void preAddFlowRunAction() {
        if (nodes == null)
            nodes = new ArrayList<>();
        nodes.clear();
        flowRunAction = new FlowRunAction();
        flowRunAction.setStatus(-1L);
        flowRunAction.setCreateDate(new Date());
        flowRunAction.setTimeRun(new Date());
        flowRunAction.setCrNumber(Config.CR_DEFAULT);
        mapParamValueGroup.clear();
        mapParamValue.clear();
        selectedFlowTemplates = null;
        nodeSeachs.clear();
        tmpNodes.clear();
        preNodes.clear();
        loadCR();
        //20181119_tudn_start them danh sach lenh blacklist
        sureMopContainCmdBlacklist = false;
        //20181119_tudn_end them danh sach lenh blacklist
        //20190408_chuongtq start check param when create MOP
        mapCheckParamCondition.clear();
        setAddNew(true);
        objectImports.clear();
        mapAccGroupAction.clear();
        mapParam.clear();
        mapParamValue.clear();
        mapAccGroupAction.clear();
        mapGroupAction.clear();
        mapSubFlowRunNodes.clear();
        lstUploadedFile.clear();
        rsUpload = "";
        //20190408_chuongtq end check param when create MOP
    }

    Map<Node, Multimap<Long, ParamValue>> mapParamValueGroup = new HashMap<Node, Multimap<Long, ParamValue>>();

    public List<ParamValue> getParamInputsBk(Node node) {
        try {
//    		caches.clear();
            if (mapParamValue.get(node) == null) {
//                Set<ParamInput> inputs = new LinkedHashSet<>();
                List<ParamInput> inputs = new ArrayList<>();
                Map<Long, Long> mapParamInputGroupCode = new HashMap<>();
                Multimap<Long, ParamValue> _mapParamValueGroup = ArrayListMultimap.create();
//                Boolean checkRefenderParam = false;
                HashMap<Long, Boolean> mapGroupActionDeclare = new HashMap<>();
                List<GroupAction> lstGroupAction = mapGroupAction.get(node);
                for (GroupAction groupAction : lstGroupAction) {
                    for (ActionOfFlow actionOfFlow : groupAction.getActionOfFlows()) {
                        mapGroupActionDeclare.put(actionOfFlow.getStepNum(), groupAction.isDeclare());
                    }
                }
                if (selectedFlowTemplates != null) {
//                	Map<String, Object> filters = new HashMap<>();
                    for (ActionOfFlow actionOfFlow : selectedFlowTemplates.getActionOfFlows()) {
                        if (mapGroupActionDeclare.containsKey(actionOfFlow.getStepNum())) {
//                            checkRefenderParam = mapGroupActionDeclare.get(actionOfFlow.getStepNum());
                        }
                        List<Long> lstActionDetailId = (List<Long>) caches.get("ActionDetail#" + actionOfFlow.getAction().getActionId() + "#" + node.getVendor().getVendorId() + "#"
                                + node.getVersion().getVersionId() + "#" + node.getNodeType().getTypeId());
                        if (lstActionDetailId == null) {
                            lstActionDetailId = new ArrayList<>();
                            logger.info("GET DATA FROM ACTION DETAIL");
//                            actionDetails = new ActionDetailServiceImpl().findList("from ActionDetail where action.actionId =? and vendor.vendorId = ? "
//                                    + "and version.versionId =? and nodeType.typeId = ?", -1, -1, actionOfFlow.getAction().getActionId(), node.getVendor().getVendorId(),
//                                    node.getVersion().getVersionId(), node.getNodeType().getTypeId());

                            try {
                                String sql = "SELECT a.DETAIL_ID "
                                        + "FROM Action_Detail a "
                                        + "WHERE a.VENDOR_ID = ? AND a.ACTION_ID = ? "
                                        + "and a.NODE_TYPE_ID = ? and a.VERSION_ID = ?";
                                List<?> queryData = (List<?>) new ParamInputServiceImpl()
                                        .findListSQLAll(sql, node.getVendor().getVendorId(), actionOfFlow.getAction().getActionId(),
                                                node.getNodeType().getTypeId(), node.getVersion().getVersionId());

                                for (Object detailId : queryData) {
                                    logger.info(((BigDecimal) detailId).longValue() + "");
                                    if (detailId != null) {
                                        lstActionDetailId.add(((BigDecimal) detailId).longValue());
                                    }
                                }

                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }


                            logger.info("END GET DATA FROM ACTION DETAIL");
                            caches.put("ActionDetail#" + actionOfFlow.getAction().getActionId() + "#" + node.getVendor().getVendorId() + "#"
                                    + node.getVersion().getVersionId() + "#" + node.getNodeType().getTypeId(), lstActionDetailId);
                        }
                        for (Long actionDetailId : lstActionDetailId) {
                            {
                                List<ActionCommand> actionCommands = (List<ActionCommand>) caches.get("ActionCommand#" + actionDetailId + "#" + node.getVendor().getVendorId() + "#"
                                        + node.getVersion().getVersionId() + "#" + node.getNodeType().getTypeId());
                                if (actionCommands == null) {
                                    logger.info("GET DATA FROM ACTION ActionCommand");
                                    actionCommands = new ActionCommandServiceImpl().findList("from ActionCommand where actionDetail.detailId = ? and "
                                                    + "commandDetail.vendor.vendorId =? and commandDetail.version.versionId =? ", -1, -1, actionDetailId, node.getVendor().getVendorId(),
                                            node.getVersion().getVersionId());
                                    logger.info("END GET DATA FROM ACTION ActionCommand");
                                    caches.put("ActionCommand#" + actionDetailId + "#" + node.getVendor().getVendorId() + "#"
                                            + node.getVersion().getVersionId() + "#" + node.getNodeType().getTypeId(), actionCommands);
                                }
                                List<Long> commandDetailIds = new ArrayList<>();
                                String commandDetailIdString = "";
                                for (ActionCommand actionCommand : actionCommands) {
                                    CommandDetail commandDetail = actionCommand.getCommandDetail();
                                    commandDetailIds.add(commandDetail.getCommandDetailId());
                                    commandDetailIdString += "#" + commandDetail.getCommandDetailId();
                                }
                                Map<String, Collection<?>> map = new HashMap<>();
                                map.put("commandDetailIds", commandDetailIds);
                                List<ParamInput> paramInputs = (List<ParamInput>) caches.get("ParamInput#-" + commandDetailIdString);
                                if (paramInputs == null) {
                                    if (commandDetailIds.size() > 0) {
                                        paramInputs = new ParamInputServiceImpl().findListWithIn("from ParamInput where commandDetail.commandDetailId in (:commandDetailIds)", -1, -1, map);
                                    } else {
                                        paramInputs = new ArrayList<>();
                                    }
                                    caches.put("ParamInput#-" + commandDetailIdString, paramInputs);
                                    logger.info(node.getNodeCode() + "_" + actionOfFlow.getStepNum() + "_" + actionDetailId + "_" + commandDetailIdString);
                                }
                                Multimap<CommandDetail, ParamInput> mapParamInputs = ArrayListMultimap.create();
                                for (ParamInput paramInput2 : paramInputs) {
                                    mapParamInputs.put(paramInput2.getCommandDetail(), paramInput2);
                                }
                                if (mapParamInputs.size() > 0) {
                                    for (ActionCommand actionCommand : actionCommands) {
                                        CommandDetail commandDetail = actionCommand.getCommandDetail();
//                                                                                                                                if(//commandDetail.getNodeType().getTypeId().equals(node.getNodeType().getTypeId()) &&
//                                                                                                                                                                commandDetail.getVendor().getVendorId().equals(node.getVendor().getVendorId()) &&
//                                                                                                                                                                commandDetail.getVersion().equals(node.getVersion()))
                                        Collection<ParamInput> collection = mapParamInputs.get(commandDetail);
                                        if (collection.size() > 0) {
                                            for (ParamInput paramInput : collection) {
                                                if (paramInput.getParamGroups().size() > 0) {
                                                    for (ParamGroup paramGroup : paramInput.getParamGroups()) {
                                                        if (paramGroup.getFlowTemplates().equals(selectedFlowTemplates)) {
                                                            paramInput.setParamDefault(paramGroup.getParamDefault());
                                                            if (paramGroup.getGroupCode() != null) {
                                                                paramInput.setColor(Config.getCOLORS()[Math.min(paramGroup.getGroupCode().intValue(), Config.getCOLORS().length - 1)]);
                                                            }

                                                            if (paramGroup.getGroupCode() != null) {
                                                                mapParamInputGroupCode.put(paramInput.getParamInputId(), paramGroup.getGroupCode());
                                                            }
                                                        }
                                                    }
                                                }
                                                boolean isInOutCmd = false;
                                                String inOutCmd = null;
                                                for (ParamInOut paramInOut : paramInput.getParamInOuts()) {
                                                    if (paramInOut.getActionCommandByActionCommandInputId().equals(actionCommand)
                                                            && paramInOut.getActionOfFlowByActionFlowInId().equals(actionOfFlow)) {
                                                        isInOutCmd = true;
                                                        inOutCmd = paramInOut.getActionCommandByActionCommandOutputId().getCommandDetail().getCommandTelnetParser().getCmd()
                                                                + " (" + paramInOut.getActionOfFlowByActionFlowOutId().getAction().getName() + ")";
                                                        break;
                                                    }

                                                    /*
                                                     for (ActionCommand actionCommand2 : actionDetail.getActionCommands()) {
                                                     if(paramInOut.getActionCommandByActionCommandInputId().equals(actionCommand2)){
                                                     isInOutCmd = true;
                                                     inOutCmd = actionCommand2.getCommandDetail().getCommandTelnetParser().getCmd();
                                                     break;
                                                     }
                                                     }
                                                     */
                                                    if (isInOutCmd) {
                                                        break;
                                                    }
                                                }

                                                paramInput.setInOut(isInOutCmd);
                                                paramInput.setCmdInOut(inOutCmd);
                                                inputs.add(paramInput);
                                            }

                                        }

                                    }
                                }
                            }
                        }
                    }
                }
                List<ParamValue> paramValues = new LinkedList<>();
                for (ParamInput paramInput : inputs) {

                    ParamValue paramValue = new ParamValue();
                    paramValue.setParamInput(paramInput);
                    paramValue.setParamCode(paramInput.getParamCode());
                    //paramValue.setFlowRunAction(flowRunAction);
                    paramValue.setNodeRun(new NodeRun(new NodeRunId(node.getNodeId(), flowRunAction.getFlowRunId()), flowRunAction, node));

                    paramValue.setParamValue(paramInput.getParamDefault());
//                    if (paramValue.getParamCode().equals("content")) {
//                        String temp = "";
//                        if (node.getLstInterface() != null) {
//                            for (String string : node.getLstInterface()) {
//                                temp += string + ";";
//                            }
//                        }
//                        temp = temp.replaceAll(";$", "");
//                        paramValue.setParamValue(temp);
//                    }
                    Long groupCode = mapParamInputGroupCode.get(paramInput.getParamInputId());
                    paramValue.setGroupCode(groupCode);
                    if (groupCode != null) {
                        _mapParamValueGroup.put(groupCode, paramValue);
                    }
                    paramValues.add(paramValue);
                }
                for (ParamValue paramValue2 : paramValues) {
                    loadParam(paramValue2, paramValues);
                }

                mapParamValue.put(node, paramValues);
                mapParamValueGroup.put(node, _mapParamValueGroup);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return mapParamValue.get(node);
    }

    public List<ParamValue> getParamInputs(Node node) {
        if (mapParamValue.get(node) == null || mapParamValue.get(node).isEmpty()) {
            Set<ParamInput> inputs = new LinkedHashSet<ParamInput>();
            Map<Long, Long> mapParamInputGroupCode = new HashMap<Long, Long>();
            Multimap<Long, ParamValue> _mapParamValueGroup = ArrayListMultimap.create();
            HashMap<String, ParamInput> mapParamInOutInNode = new HashMap<>();
            if (selectedFlowTemplates != null) {
                for (ActionOfFlow actionOfFlow : selectedFlowTemplates.getActionOfFlows()) {

                    List<ActionDetail> actionDetails = (List<ActionDetail>) caches.get("ActionDetail#" + actionOfFlow.getAction().getActionId() + "#" + node.getVendor().getVendorId() + "#" +
                            node.getVersion().getVersionId() + "#" + node.getNodeType().getTypeId());
                    if (actionDetails == null) {
                        actionDetails = new ActionDetailServiceImpl().findList("from ActionDetail where action.actionId =? and vendor.vendorId = ? "
                                        + "and version.versionId =? and nodeType.typeId = ?", -1, -1, actionOfFlow.getAction().getActionId(), node.getVendor().getVendorId(),
                                node.getVersion().getVersionId(), node.getNodeType().getTypeId());
                        caches.put("ActionDetail#" + actionOfFlow.getAction().getActionId() + "#" + node.getVendor().getVendorId() + "#" +
                                node.getVersion().getVersionId() + "#" + node.getNodeType().getTypeId(), actionDetails);
                    }
                    for (ActionDetail actionDetail : actionDetails) {
//						if(actionDetail.getVendor().equals(node.getVendor()) &&
//								actionDetail.getVersion().equals(node.getVersion()) &&
//								actionDetail.getNodeType().equals(node.getNodeType()))
                        {
                            List<ActionCommand> actionCommands = (List<ActionCommand>) caches.get("ActionCommand#" + actionDetail.getDetailId() + "#" + node.getVendor().getVendorId() + "#" +
                                    node.getVersion().getVersionId());
                            if (actionCommands == null) {
                                actionCommands = new ActionCommandServiceImpl().findList("from ActionCommand where actionDetail.detailId = ? and "
                                                + "commandDetail.vendor.vendorId =? and commandDetail.version.versionId =? ", -1, -1, actionDetail.getDetailId(), node.getVendor().getVendorId(),
                                        node.getVersion().getVersionId());
                                caches.put("ActionCommand#" + actionDetail.getDetailId() + "#" + node.getVendor().getVendorId() + "#" +
                                        node.getVersion().getVersionId(), actionCommands);
                            }
                            List<Long> commandDetailIds = new ArrayList<Long>();
                            String commandDetailIdString = "";
                            for (ActionCommand actionCommand : actionCommands) {
                                CommandDetail commandDetail = actionCommand.getCommandDetail();
                                commandDetailIds.add(commandDetail.getCommandDetailId());
                                commandDetailIdString += "#" + commandDetail.getCommandDetailId();
                            }
                            Map<String, Collection<?>> map = new HashMap<String, Collection<?>>();
                            map.put("commandDetailIds", commandDetailIds);
                            List<ParamInput> paramInputs = (List<ParamInput>) caches.get("ParamInput#" + commandDetailIdString);
                            if (paramInputs == null) {
                                if (commandDetailIds.size() > 0) {
                                    try {
                                        paramInputs = new ParamInputServiceImpl().findListWithIn("from ParamInput where commandDetail.commandDetailId in (:commandDetailIds)", -1, -1, map);
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                        paramInputs = new ArrayList<ParamInput>();
                                    }

                                } else {
                                    paramInputs = new ArrayList<ParamInput>();
                                }
                                caches.put("ParamInput#" + commandDetailIdString, paramInputs);
                            }
                            Multimap<CommandDetail, ParamInput> mapParamInputs = ArrayListMultimap.create();
                            for (ParamInput paramInput2 : paramInputs) {
                                mapParamInputs.put(paramInput2.getCommandDetail(), paramInput2);
                            }
                            if (mapParamInputs.size() > 0)
                                for (ActionCommand actionCommand : actionCommands) {
                                    CommandDetail commandDetail = actionCommand.getCommandDetail();
//								if(//commandDetail.getNodeType().getTypeId().equals(node.getNodeType().getTypeId()) &&
//										commandDetail.getVendor().getVendorId().equals(node.getVendor().getVendorId()) &&
//										commandDetail.getVersion().equals(node.getVersion()))
                                    Collection<ParamInput> collection = mapParamInputs.get(commandDetail);
                                    if (collection.size() > 0) {
                                        for (ParamInput paramInput : collection) {
                                            if (paramInput.getParamGroups().size() > 0) {
                                                for (ParamGroup paramGroup : paramInput.getParamGroups()) {
                                                    if (paramGroup.getFlowTemplates().equals(selectedFlowTemplates)) {
                                                        paramInput.setParamDefault(paramGroup.getParamDefault());
                                                        if (paramGroup.getGroupCode() != null)
                                                            paramInput.setColor(Config.getCOLORS()[Math.min(paramGroup.getGroupCode().intValue(), Config.getCOLORS().length - 1)]);

                                                        if (paramGroup.getGroupCode() != null)
                                                            mapParamInputGroupCode.put(paramInput.getParamInputId(), paramGroup.getGroupCode());
                                                    }
                                                }
                                            }
                                            boolean isInOutCmd = false;
                                            String inOutCmd = null;
                                            for (ParamInOut paramInOut : paramInput.getParamInOuts()) {
                                                if (paramInOut.getActionCommandByActionCommandInputId().equals(actionCommand) &&
                                                        paramInOut.getActionOfFlowByActionFlowInId().equals(actionOfFlow)) {
                                                    isInOutCmd = true;
                                                    mapParamInOutInNode.put(paramInput.getParamCode().trim().toLowerCase(), paramInput);
                                                    inOutCmd = paramInOut.getActionCommandByActionCommandOutputId().getCommandDetail().getCommandTelnetParser().getCmd() +
                                                            " (" + paramInOut.getActionOfFlowByActionFlowOutId().getAction().getName() + ")";
                                                    break;
                                                }

											/*
											for (ActionCommand actionCommand2 : actionDetail.getActionCommands()) {
												if(paramInOut.getActionCommandByActionCommandInputId().equals(actionCommand2)){
													isInOutCmd = true;
													inOutCmd = actionCommand2.getCommandDetail().getCommandTelnetParser().getCmd();
													break;
												}
											}
											*/
                                                if (isInOutCmd)
                                                    break;
                                            }
                                            paramInput.setInOut(isInOutCmd);
                                            paramInput.setCmdInOut(inOutCmd);
                                            inputs.add(paramInput);
                                        }

                                    }

                                }
                        }
                    }
                }
            }
            List<ParamValue> paramValues = new LinkedList<ParamValue>();

            NodeAccount vtAdminAccount = null;
            //tuanda38_20180822_get tdtt account when choosing DATABASE template group_start
            NodeAccount tdhtAccount = null;
            //tuanda38_20180822_get tdtt account when choosing DATABASE template group_end
            Map<String, Object> filtersNode = new HashMap<>();
            filtersNode.put("serverId", node.getServerId());
            filtersNode.put("accountType", 1L);
            filtersNode.put("impactOrMonitor", 2L);
            filtersNode.put("active", Constant.status.active);

            try {
                List<NodeAccount> lstNodeAcc = new NodeAccountServiceImpl().findListExac(filtersNode, null);
                if (lstNodeAcc != null && lstNodeAcc.size() >= 1) {
                    for (NodeAccount nodeAccount : lstNodeAcc) {
                        if (nodeAccount.getItBusinessNode() == null || nodeAccount.getItBusinessNode() == 0L) {
                            vtAdminAccount = nodeAccount;
                            break;
                        }
                    }
                }
                //tuanda38_20180822_get tdtt account when choosing DATABASE template group_start
                filtersNode.clear();
                filtersNode.put("serverId", node.getServerId());
                filtersNode.put("accountType", 2L);
                filtersNode.put("impactOrMonitor", 1L);
                filtersNode.put("active", Constant.status.active);
//				filtersNode.put("username","tdht");
                List<NodeAccount> lstNodeAcc2 = new NodeAccountServiceImpl().findListExac(filtersNode, null);
                if (lstNodeAcc2 != null && lstNodeAcc2.size() >= 1) {
                    for (NodeAccount nodeAccount : lstNodeAcc2) {
                        if (nodeAccount.getItBusinessNode() == null || nodeAccount.getItBusinessNode() == 0L) {
                            tdhtAccount = nodeAccount;
                            break;
                        }
                    }
                }
                //tuanda38_20180822_get tdtt account when choosing DATABASE template group_end
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            //tuanda38_20180822_get tdtt account when choosing DATABASE template group_start
            if (mapAccGroupAction.get(node) == null) {
                if (selectedFlowTemplates.getTemplateGroup() != null && (selectedFlowTemplates.getTemplateGroup().getId() == 3 || selectedFlowTemplates.getTemplateGroup().getId() == 4)) {
                    if (tdhtAccount != null) {
                        List<GroupAction> groupActions = mapGroupAction.get(node);
                        if (groupActions != null) {
                            for (int i = 0; i < groupActions.size(); i++) {
                                if (groupActions.get(i).getNodeAccount() == null || "".equals(groupActions.get(i).getNodeAccount())) {
                                    groupActions.get(i).setNodeAccount(tdhtAccount);
                                    if (mapAccGroupAction.get(node) == null) {
                                        mapAccGroupAction.put(node, new HashMap<>());
                                    }
                                    mapAccGroupAction.get(node).put(groupActions.get(i).getGroupActionName() + "==" + i, tdhtAccount);
                                }
                            }
                        }
                    }
                } else {
                    if (vtAdminAccount != null) {
                        List<GroupAction> groupActions = mapGroupAction.get(node);
                        if (groupActions != null) {
                            for (int i = 0; i < groupActions.size(); i++) {
                                //20180831_tudn_start cap nhat trang thai
                                if (groupActions.get(i).getNodeAccount() == null || "".equals(groupActions.get(i).getNodeAccount())) {
                                    //20180831_tudn_end cap nhat trang thai
                                    groupActions.get(i).setNodeAccount(vtAdminAccount);
                                    if (mapAccGroupAction.get(node) == null) {
                                        mapAccGroupAction.put(node, new HashMap<>());
                                    }
                                    mapAccGroupAction.get(node).put(groupActions.get(i).getGroupActionName() + "==" + i, vtAdminAccount);
                                    //20180831_tudn_start cap nhat trang thai
                                }
                                //20180831_tudn_end cap nhat trang thai
                            }
                        }
                    }
                }
                //20180831_tudn_start cap nhat trang thai
            }
            //20180831_tudn_end cap nhat trang thai
            //tuanda38_20180822_get tdtt account when choosing DATABASE template group_end

            for (ParamInput paramInput : inputs) {
                ParamValue paramValue = new ParamValue();
                paramValue.setParamInput(paramInput);
                paramValue.setParamCode(paramInput.getParamCode());
                //paramValue.setFlowRunAction(flowRunAction);
                paramValue.setNodeRun(new NodeRun(new NodeRunId(node.getNodeId(), flowRunAction.getFlowRunId()), flowRunAction, node));

                paramValue.setParamValue(paramInput.getParamDefault());


                if (org.apache.commons.lang3.StringUtils.isEmpty(paramValue.getParamValue())) {
                    if ("vt_admin".equals(paramValue.getParamCode()) && vtAdminAccount != null) {
                        paramValue.setParamValue(vtAdminAccount.getUsername());
                    } else if ("date".equals(paramValue.getParamCode())) {
                        SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmm");
                        paramValue.setParamValue(sdf.format(flowRunAction.getCreateDate() == null ? new Date() : flowRunAction.getCreateDate()));
                    }
                }
//				if(paramValue.getParamCode().equals(PARAMCODE.INTERFACE_UPLINK.value)){
//					String temp="";
//					if(node.getLstInterface()!=null)
//						for (String string : node.getLstInterface()) {
//							temp += string+";";
//						}
//					temp = temp.replaceAll(";$", "");
//					paramValue.setParamValue(temp);
//				}
                Long groupCode = mapParamInputGroupCode.get(paramInput.getParamInputId());
                paramValue.setGroupCode(groupCode);
                if (groupCode != null)
                    _mapParamValueGroup.put(groupCode, paramValue);
                paramValues.add(paramValue);
            }
            for (ParamValue paramValue2 : paramValues) {
                loadParam(paramValue2, paramValues);
            }
//			createActionWithMultiParam(paramValues);
            mapParamInOut.put(node, mapParamInOutInNode);
            mapParamValue.put(node, paramValues);
            mapParamValueGroup.put(node, _mapParamValueGroup);
        }
        return mapParamValue.get(node);
    }

    public List<ParamValue> distinctParamValueSameParamCode(List<ParamValue> paramValues) {
        Set<String> _tmp = new HashSet<String>();
        List<ParamValue> _paramValues = new ArrayList<ParamValue>();
        for (ParamValue paramValue : paramValues) {
            String paramCode = paramValue.getParamInput().getParamCode();
            if (!_tmp.contains(paramCode)) {
                _paramValues.add(paramValue);
                _tmp.add(paramCode);
            } else {
                if (paramValue.getParamInput().getInOut()) {
                    for (ParamValue paramValue2 : _paramValues) {
                        if (paramValue.getParamInput().equals(paramValue2.getParamInput()))
                            paramValue2.getParamInput().setInOut(true);
                    }
                }
            }
        }
        return _paramValues;
    }
//	public void copyValue(ParamValue paramValue, List<ParamValue> paramValues, List<ParamValue> paramValueHasSetups){
//		if(paramValueHasSetups.size()==paramValues.size())
//			return;
//		if(paramValue!=null){
//			for (ParamValue paramValue3 : paramValues) {
//				if(paramValue.getParamInput().getParamCode().equals(paramValue3.getParamInput().getParamCode())){
//					paramValue3.setParamValue(paramValue.getParamValue());
//					paramValueHasSetups.add(paramValue3);
//				}
//			}
//		}
//	}


    /*
	Xu ly khi thay doi gia tri param input value
	 */
    public void onKeyUpValueParam(ParamValue paramValue, List<ParamValue> paramValues) {

        loadParam(paramValue, paramValues);
        createActionWithMultiParam(paramValues);

    }

    public void loadParamOnce() {
        System.err.println("aaa");
        List<ParamValue> paramValues = getParamInputs(selectedNode);
        for (ParamValue paramValue : paramValues) {
            loadParam(paramValue, paramValues);
        }
        createActionWithMultiParam(paramValues);
        RequestContext.getCurrentInstance().execute("PF('loadparam').stop()");
    }

    private void loadParam(ParamValue paramValue, List<ParamValue> paramValues) {
        if (paramValue != null) {
            for (ParamValue paramValue3 : paramValues) {
                if (paramValue.getParamInput().getParamCode().equals(paramValue3.getParamInput().getParamCode())) {
                    if (!paramValue3.getParamInput().getInOut())
                        paramValue3.getParamInput().setInOut(paramValue.getParamInput().getInOut());
                    paramValue3.setParamValue(paramValue.getParamValue());

                    if (paramValue3.getGroupCode() != null) {
                        for (ParamValue paramValue5 : paramValues) {
                            if (paramValue5.getGroupCode() != null && paramValue5.getGroupCode().equals(paramValue3.getGroupCode())) {
                                paramValue5.setParamValue(paramValue.getParamValue());
                                if (!paramValue5.getParamInput().getInOut())
                                    paramValue5.getParamInput().setInOut(paramValue.getParamInput().getInOut());
                                for (ParamValue paramValue4 : paramValues) {
                                    if (paramValue5.getParamInput().getParamCode().equals(paramValue4.getParamInput().getParamCode())) {
                                        paramValue4.setParamValue(paramValue5.getParamValue());
                                        if (!paramValue4.getParamInput().getInOut())
                                            paramValue4.getParamInput().setInOut(paramValue.getParamInput().getInOut());
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (paramValue.getGroupCode() != null) {
                for (ParamValue paramValue5 : paramValues) {
                    if (paramValue5.getGroupCode() != null && paramValue5.getGroupCode().equals(paramValue.getGroupCode())) {
                        paramValue5.setParamValue(paramValue.getParamValue());
                        for (ParamValue paramValue4 : paramValues) {
                            if (paramValue5.getParamInput().getParamCode().equals(paramValue4.getParamInput().getParamCode())) {
                                paramValue4.setParamValue(paramValue5.getParamValue());
                            }
                        }
                    }
                }
            }

        }
    }

    private void createActionWithMultiParam(List<ParamValue> paramValues) {
        logger.info(">>>>>>>>>> START createActionWithMultiParam");
        if (selectedNode != null && selectedNode.getGroupActions() != null) {
            for (GroupAction groupAction : selectedNode.getGroupActions()) {
                for (Iterator<ActionOfFlow> iterator = groupAction.getActionOfFlows().iterator(); iterator.hasNext(); ) {
                    ActionOfFlow actionOfFlow = iterator.next();
                    if (actionOfFlow.getIndexParamValue() != null && actionOfFlow.getIndexParamValue() != 0)
                        iterator.remove();
                }
            }
            for (GroupAction groupAction : selectedNode.getGroupActions()) {
                List<ActionOfFlow> actionOfFlows = groupAction.getActionOfFlows();
                if (actionOfFlows.size() == 0)
                    continue;
                List<Long> acfIds = new ArrayList<Long>();
                Multimap<String, Object> mulTemp = ArrayListMultimap.create();
                for (ActionOfFlow actionOfFlow2 : actionOfFlows) {
                    acfIds.add(actionOfFlow2.getAction().getActionId());
                }

                String sqlAll = "SELECT DISTINCT a.ACTION_ID ,pi.PARAM_INPUT_ID "
                        + "FROM Action_Db_Server a "
                        + "JOIN Action_Detail ad "
                        + "ON a.ACTION_ID=ad.ACTION_ID "
                        + "JOIN Action_Command ac "
                        + "ON ad.DETAIL_ID = ac.ACTION_DETAIL_ID "
                        + "JOIN Command_Detail cd "
                        + "ON cd.COMMAND_DETAIL_ID = ac.COMMAND_DETAIL_ID "
                        + "JOIN Param_Input pi "
                        + "ON pi.CMD_DETAIL_ID = cd.COMMAND_DETAIL_ID "
                        + "JOIN Param_Value pv "
                        + "ON pv.PARAM_INPUT_ID = pi.PARAM_INPUT_ID "
                        + "WHERE a.ACTION_ID  in (:actionIds)";
                Map<String, Object> paramlist = new HashMap<String, Object>();
                paramlist.put("actionIds", acfIds);
                List<?> paramInputAlls = new ParamInputServiceImpl().findListSQLWithMapParameters(null, sqlAll, -1, -1, paramlist);

                for (Object object : paramInputAlls) {
                    Object[] cols = (Object[]) object;
                    mulTemp.put("ParamInput##" + cols[0], cols[1]);
                }
                for (Long acfId : acfIds) {
                    String key = "ParamInput##" + acfId;
                    if (mulTemp.get(key) != null) {
                        caches.put(key, mulTemp.get(key));
                    } else
                        caches.put(key, new ArrayList<>());
                }

                int i = 0;
                while (i < actionOfFlows.size()) {
//				for (int i = 0; i < actionOfFlows.size(); i++) {
                    ActionOfFlow actionOfFlow = actionOfFlows.get(i);
                    int maxValue = 1;

                    String sql = "SELECT DISTINCT pi.PARAM_INPUT_ID "
                            + "FROM Action_Db_Server a "
                            + "JOIN Action_Detail ad "
                            + "ON a.ACTION_ID=ad.ACTION_ID "
                            + "JOIN Action_Command ac "
                            + "ON ad.DETAIL_ID = ac.ACTION_DETAIL_ID "
                            + "JOIN Command_Detail cd "
                            + "ON cd.COMMAND_DETAIL_ID = ac.COMMAND_DETAIL_ID "
                            + "JOIN Param_Input pi "
                            + "ON pi.CMD_DETAIL_ID = cd.COMMAND_DETAIL_ID "
                            + "JOIN Param_Value pv "
                            + "ON pv.PARAM_INPUT_ID = pi.PARAM_INPUT_ID "
                            + "WHERE a.ACTION_ID    = ?";
                    List<?> paramInputs = (List<?>) caches.get("ParamInput##" + actionOfFlow.getAction().getActionId());
                    if (paramInputs == null) {
                        paramInputs = new ParamInputServiceImpl().findListSQLAll(sql, actionOfFlow.getAction().getActionId());
                        caches.put("ParamInput##" + actionOfFlow.getAction().getActionId(), paramInputs);
                    }

                    for (Object paramInput : paramInputs) {
                        for (ParamValue paramValue2 : paramValues) {
                            if (paramInput.toString().equals(paramValue2.getParamInput().getParamInputId().toString()))
                                if (paramValue2.getParamValue() != null)
                                    maxValue = Math.max(maxValue, paramValue2.getParamValue().split(Config.SPLITTER_VALUE, -1).length);
                        }
                    }

//					for (ActionDetail actionDetail : actionOfFlow.getAction().getActionDetails()) {
//						for (ActionCommand actionCommand : actionDetail.getActionCommands()) {
//							for (ParamInput paramInput : actionCommand.getCommandDetail().getParamInputs()) {
//								for (ParamValue paramValue2 : paramValues) {
//									if(paramInput.equals(paramValue2.getParamInput()))
//										if (paramValue2.getParamValue() != null)
//											maxValue = Math.max(maxValue, paramValue2.getParamValue().split(Config.SPLITTER_VALUE, -1).length);
//								}
//							}
//						}
//					}
                    //				System.out.println(actionOfFlow);
                    i++;
                    if (maxValue == 1) {
                        continue;
                    }
                    int countClone = 0;
                    for (int j = 1; j < maxValue; j++) {
                        try {
                            //ActionOfFlow actionOfFlowClone = new Cloner().deepCloneDontCloneInstances(actionOfFlow, actionOfFlow.getFlowTemplates());
//							ActionOfFlow actionOfFlowClone = actionOfFlow.deepClone();
                            ActionOfFlow actionOfFlowClone = (ActionOfFlow) BeanUtils.cloneBean(actionOfFlow);//actionOfFlow.deepClone();
                            actionOfFlowClone.setIndexParamValue(new Integer(j));
                            actionOfFlows.add(Math.min(i + j, actionOfFlows.size()), actionOfFlowClone);
                            countClone++;
                            if (countClone == 100) {
//                                System.gc();
                                countClone = 0;
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                    i = i + maxValue - 1;
                }
            }
//			for (GroupAction groupAction : selectedNode.getGroupActions()) {
//				for (Iterator<ActionOfFlow> iterator = groupAction.getActionOfFlows().iterator(); iterator.hasNext();) {
//					ActionOfFlow actionOfFlow = iterator.next();
//					System.out.println(actionOfFlow.buildCommand(paramValues, false, selectedNode));;
//				}
//			}
        }
        logger.info(">>>>>>>>>> END createActionWithMultiParam");
    }

    public void handleSelectNode(SelectEvent event) {
        Node node = (Node) event.getObject();
        if ("srt".equalsIgnoreCase(node.getNodeType().getTypeName())) {

            RelationNodeUtil relationNodeUtil = new RelationNodeUtil();
            String srtNodecode = node.getNodeCode();
            if (this.nodes == null)
                this.nodes = new ArrayList<Node>();
            List<Node> nodes = relationNodeUtil.getRingSrt(srtNodecode);
            if (nodes != null)
                for (Node node2 : nodes) {
                    if (!this.nodes.contains(node2)) {
                        this.nodes.add(node2);
                        loadGroupAction(node2);
                    }
                }
        }
        handleSelectNode(node);
    }

    public void handleSelectNode(Node obj) {
        Node _node = null;
        for (Node node : nodes) {
            if (node.equals(obj))
                _node = node;
        }
        if (_node != null) {
            loadGroupAction(_node);
            selectedNode = _node;
        }

    }

    public void loadGroupAction(Node _node) {
        _node.getGroupActions().clear();
        NodeAccount accNode;
        for (String groupName : groupActions.keySet()) {
            // lay ra thong tin account tac dong cho tung dau viec
            accNode = null;
            try {
                accNode = getNodeAccGroup(_node, groupActions.get(groupName).get(0));
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            if (accNode == null) {
                boolean check = false;
                if(mapGroupAction.containsKey(_node)){
                    for(GroupAction groupAction : mapGroupAction.get(_node)){
                        if(groupAction.getGroupActionName().equals(groupName)){
                            check = true;
                            break;
                        }
                    }

                }
                if(!check){
                    _node.getGroupActions().add(new GroupAction(groupName, new LinkedList<>(groupActions.get(groupName))));
                    mapGroupAction.put(_node, _node.getGroupActions());
                }
            } else {
                _node.getGroupActions().add(new GroupAction(groupName, new LinkedList<>(groupActions.get(groupName)), accNode));
                mapGroupAction.put(_node, _node.getGroupActions());
            }
        }
    }

    private NodeAccount getNodeAccGroup(Node _node, ActionOfFlow actionFlow) {
        NodeAccount nodeAccount = null;
        try {
            if(flowRunAction.getFlowRunId() != null && _node.getNodeId() != null && actionFlow.getStepNum() != null) {
                logger.info("flowRunId: " + flowRunAction.getFlowRunId() + "  nodeId: " + _node.getServerId() + " actionOfFlowId: " + actionFlow.getStepNum());
                Map<String, Object> filters = new HashMap<>();
                filters.put("flowRunId", flowRunAction.getFlowRunId());
                filters.put("nodeId", _node.getNodeId());
                filters.put("actionOfFlowId", actionFlow.getStepNum());

                List<AccountGroupMop> lstAcGroupMop = new AccountGroupMopServiceImpl().findList(filters, null);
                logger.info(">>> finish get lstAcGroupMop");
                if (lstAcGroupMop != null && !lstAcGroupMop.isEmpty()) {
                    nodeAccount = new NodeAccountServiceImpl().findById(lstAcGroupMop.get(0).getNodeAccountId());
                }
                logger.info(">>> finish get nodeAccount");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return nodeAccount;
    }

    public void onChangeNode(TabChangeEvent changeEvent) {
        try {
            String id = changeEvent.getTab().getClientId();
            id = id.split("-", -1)[1];
            if (nodes != null)
                for (Node node : nodes) {
                    if (node.getNodeId().toString().equalsIgnoreCase(id)) {
                        selectedNode = node;
                        List<ParamValue> paramValues = getParamInputs(selectedNode);
                        if (paramValues != null && paramValues.size() > 0)
                            onKeyUpValueParam(paramValues.get(0), paramValues);
                        break;
                    }
                }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void onChangeActionDeclare(String actionGroup) {

//		Boolean valueChange = selectedNode.getMapGroupActionDeclare().get("get");
//		selectedNode.getMapGroupActionDeclare().put(actionGroup, valueChange);
        for (ActionOfFlow actionOfFlow : groupActions.get(actionGroup)) {

        }
//		System.err.println(actionGroup);
    }

    /**
     * Xu ly nhap param tren nhieu dong
     *
     * @return
     * @author huynx6
     */
    public Multimap<String, BasicDynaBean> joinParamsByNodeCode() {
        Multimap<String, BasicDynaBean> multimapParam = ArrayListMultimap.create();
        for (int i = 0; i < objectImports.size(); i++) {
            BasicDynaBean basicDynaBean = (BasicDynaBean) objectImports.get(i);
            try {
				/*20180724_hoangnd_fix import param to mop_start*/
                Object nodeCode = basicDynaBean.getMap().get(MessageUtil.getResourceBundleMessage("key.nodeCode").toLowerCase());
				/*20180724_hoangnd_fix import param to mop_end*/
                if (nodeCode != null && !"".equals(nodeCode.toString()))
                    multimapParam.put(nodeCode.toString(), basicDynaBean);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return multimapParam;
    }

    public void pushParamToForm() {
         if (this.nodes == null)
            this.nodes = new ArrayList<Node>();
        int countNodeAdded = 0;
        Multimap<String, BasicDynaBean> multimapParam = joinParamsByNodeCode();
        for (String nodeCode : multimapParam.keySet()) {

            List<Node> nodes = new ArrayList<Node>();
            try {
                nodes = completeNodeRun(nodeCode, true, true);
                if (nodes.size() != 1) {
                    continue;
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                continue;
            }
            try {
                Node node = nodes.get(0);
                loadGroupAction(node);
                List<ParamValue> paramValues = getParamInputs(node);
                Collection<BasicDynaBean> basicDynaBeans = multimapParam.get(nodeCode);
                for (ParamValue paramValue : paramValues) {
                    if (paramValue.getParamInput().getReadOnly() || paramValue.getParamInput().getInOut())
                        continue;
                    String value = "";
                    try {
                        for (BasicDynaBean basicDynaBean : basicDynaBeans) {
                            Object object = null;
                            try {
								/*20180724_hoangnd_fix import param to mop_start*/
                                object = basicDynaBean.getMap().get(Util.normalizeParamCode(paramValue.getParamCode().replace(".", "")).toLowerCase());
								/*20180724_hoangnd_fix import param to mop_end*/
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                            if (object != null)
                                value += object.toString() + Config.SPLITTER_VALUE;
                        }
                        value = value.replaceAll(Config.SPLITTER_VALUE + "$", "");

                    } catch (Exception e) {
                        throw e;
                    }
                    if (!value.isEmpty()) {
                        paramValue.setParamValue(value.toString().substring(0, Math.min(3950, value.length())));
                    }
                }
                if (!this.nodes.contains(node))
                    this.nodes.add(node);

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                continue;
            }
            countNodeAdded++;
        }

        if (nodes != null && nodes.size() > 0) {
            selectedNode = nodes.get(0);
            List<ParamValue> paramValues = getParamInputs(selectedNode);
            for (ParamValue paramValue : paramValues) {
                onKeyUpValueParam(paramValue, paramValues);
            }
        }
        MessageUtil.setInfoMessage(MessageUtil.getResourceBundleMessage("info.number.of.node.imported") + ": " + countNodeAdded);
        rerender = true;
    }

    List<?> objectImports = new LinkedList<>();

    @SuppressWarnings("unchecked")
    public void handleImportParams(FileUploadEvent event) {
        objectImports.clear();
        String fileType = null;
        Workbook workbook = null;
        List<UploadedFile> lstFileTemplate = new ArrayList<>();

        try {
//			try {
            InputStream inputstream = event.getFile().getInputstream();

            if (inputstream == null) {
                throw new NullPointerException("inputstream is null");
            }
            //Get the workbook instance for XLS/xlsx file
            try {
                workbook = WorkbookFactory.create(inputstream);
/*				if (workbook==null)
					throw new NullPointerException("workbook is null");*/
            } catch (InvalidFormatException e2) {
                logger.error(e2.getMessage(), e2);
                throw new AppException("File import� Excel 97-2012 (xls, xlsx)!");
            } finally {

            }

            if(lstUploadedFile.size() ==0){
                lstUploadedFile.add(event.getFile());
            }

            else {
                for (UploadedFile uploadedFile : lstUploadedFile) {
                    if (uploadedFile.getFileName().equals(event.getFile().getFileName())) {
                        lstUploadedFile.clear();
                        rsUpload = MessageUtil.getResourceBundleMessage("error.import.file.same.name");
                        return;
                    }
                }
                lstUploadedFile.add(event.getFile());
            }

            for (UploadedFile uploadedFile : lstUploadedFile) {
                fileType = uploadedFile.getFileName().split("\\.")[uploadedFile.getFileName().split("\\.").length - 1];
                    lstFileTemplate.add(uploadedFile);

                    List<ObjectImportDt> params = new LinkedList<ObjectImportDt>();
                    List<String> sheetNames = new LinkedList<>();
                    getContextVar(params, sheetNames);


		            for (int i = 0; i < sheetNames.size(); i++) {
		                String sheetName = sheetNames.get(i);

		                Importer<Serializable> importer = new Importer<Serializable>() {

		                    @Override
		                    protected Map<Integer, String> getIndexMapFieldClass() {
		                        return null;
		                    }

		                    @Override
		                    protected String getDateFormat() {
		                        return null;
		                    }
		                };

		                Map<Integer, String> indexMapFieldClass = new HashMap<Integer, String>();
		                Integer key = 2;
						/*20180724_hoangnd_fix import param to mop_start*/
		                indexMapFieldClass.put(1, MessageUtil.getResourceBundleMessage("key.nodeCode").toLowerCase());
						/*20180724_hoangnd_fix import param to mop_end*/
		                for (String string : params.get(i).getParamNames()) {
		                    indexMapFieldClass.put(key++, string);
		                }

		                importer.setIndexMapFieldClass(indexMapFieldClass);
		                Map<Integer, String> mapHeader = new HashMap<Integer, String>();
						/*20180724_hoangnd_fix import param to mop_start*/
		                mapHeader.put(1, MessageUtil.getResourceBundleMessage("datatable.header.stt").toLowerCase());
						/*20180724_hoangnd_fix import param to mop_end*/
		                importer.setMapHeader(mapHeader);
		                importer.setRowHeaderNumber(6);
		                importer.setIsReplaceSpace(false);
		                List<Serializable> objects = importer.getDatas(workbook, sheetName, "1-");
		                if (objects != null) {
		                    ((List<Object>) objectImports).addAll(objects);
                        }
                    }
               }
//
            if (lstFileTemplate != null && lstFileTemplate.size() >1) {
                RequestContext requestContext = RequestContext.getCurrentInstance();
                requestContext.update("growl");
                requestContext.execute("PF('loadingDialog').hide();PF('importDlg').hide();");
                clearMopFail();
                lstUploadedFile.clear();
                objectImports.clear();
                MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("error.choose.multiple.file"));
                requestContext.execute("PF('addEditFlowRunDlg'.show();");
                return;
            }
            pushParamToForm();
            if (selectedFlowTemplates.getIsGenerateDT().equals(1L)) {
                List<Node> nodeBackUp = this.nodes;
                String flowRunName = flowRunAction.getFlowRunName();
                int groupName = 0;
                for (int i = 0; i < nodeBackUp.size(); i++) {
                    groupName++;
                    Node node = nodeBackUp.get(i);
                    flowRunAction.setFlowRunId(null);
                    flowRunAction.setFlowRunName(flowRunName + "_NODE_" + node.getNodeCode() + "_MOP" + groupName);
                    this.nodes = new ArrayList<>();
                    this.nodes.add(node);
                    mapSubFlowRunNodes = new HashMap<>();
                    mapSubFlowRunNodes.put("Default", nodes);
                    isSaveMapNodeFile = true;
                    boolean rs = saveDT();
                    if (!rs) {
                        groupName--;
                    }
                }
              //  MessageUtil.setInfoMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("label.create.multiple.mop.successfully") ,groupName));
              //  MessageUtil.setInfoMessageFromRes("info.save.dt.success" + ":" +groupName);
                if(lstFileTemplate != null && lstFileTemplate.size() ==1) {
                    RequestContext.getCurrentInstance().execute("PF('addEditFlowRunDlg').hide();PF('importDlg').hide();");
                    RequestContext.getCurrentInstance().update("dataForm:tableFlowRunId");
                    RequestContext.getCurrentInstance().execute("PF('tableFlowRunId').show();");
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("error.import.param.fail");
        } finally {
            if (workbook != null)
                try {
                    workbook.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
        }

    }

    private void clearFileUpload() {
        lstUploadedFile.clear();
    }

    public void clearMopFail(){
        nodes.clear();
    }

    public void exportTemplateImportParam() {
        HttpServletResponse servletResponse = preHeader();
        String file = CommonExport.getTemplateMultiExport(MessageUtil.getResourceBundleMessage("key.template.file.import.param"));
        try (InputStream is = new FileInputStream(file)) {
            //servletResponse.getOutputStream()
            try (OutputStream os = servletResponse.getOutputStream()) {

                Context context = new Context();
                List<ObjectImportDt> params = new LinkedList<ObjectImportDt>();
                List<String> sheetNames = new LinkedList<>();
                getContextVar(params, sheetNames);
                context.putVar("params", params);
                context.putVar("sheetNames", sheetNames);
                JxlsHelper.getInstance().setDeleteTemplateSheet(true).processTemplateAtCell(is, os, context, "Sheet2!A1");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        FacesContext.getCurrentInstance().responseComplete();
    }

    private HttpServletResponse preHeader() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse servletResponse = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        servletResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        servletResponse.setHeader("Expires", "0");
        servletResponse.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        servletResponse.setHeader("Pragma", "public");
        try {
            servletResponse.setHeader("Content-disposition", "attachment;filename=" +
                    URLEncoder.encode(MessageUtil.getResourceBundleMessage("key.template.file.import.param").replace(".xlsx", "") + "_" + selectedFlowTemplates.getFlowTemplateName() + ".xlsx", "UTF-8"));
        } catch (Exception e1) {
            logger.error(e1.getMessage(), e1);
        }
        return servletResponse;
    }

    public StreamedContent exportOldTemp() {
        try {
            File file = new File(CommonExport.getFolderSave() + File.separator + flowRunAction.getFileNameImportDt());
            if (!file.exists())
                return null;
            InputStream input = new FileInputStream(file);
            ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
            StreamedContent fileDispatch = new DefaultStreamedContent(input, externalContext.getMimeType(flowRunAction.getFileNameImportDt()), flowRunAction.getFileNameImportDt());
            return fileDispatch;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;

    }

    public void handleImportTempFile(FileUploadEvent fileUploadEvent) {
        this.flowRunAction.setFileImportDT(fileUploadEvent.getFile());
    }

    public void getContextVar(List<ObjectImportDt> params, List<String> sheetNames) {
        Set<InfoNode> infoNodes = new HashSet<InfoNode>();
        for (ActionOfFlow actionOfFlow : selectedFlowTemplates.getActionOfFlows()) {
            for (ActionDetail actionDetail : actionOfFlow.getAction().getActionDetails()) {
                infoNodes.add(new InfoNode(actionDetail.getVendor(), actionDetail.getVersion(), actionDetail.getNodeType()));
            }
        }
        long i = 0;
        for (InfoNode infoNode : infoNodes) {

            ObjectImportDt objectImportDt = new ObjectImportDt();
            Node node = new Node();
            node.setNodeId(i--);
            node.setVendor(infoNode.getVendor());
            node.setVersion(infoNode.getVersion());
            node.setNodeType(infoNode.getNodeType());
            objectImportDt.setVendor(infoNode.getVendor());
            objectImportDt.setVersion(infoNode.getVersion());
            objectImportDt.setNodeType(infoNode.getNodeType());

            sheetNames.add(infoNode.getNodeType().getTypeName() + "-" + infoNode.getVendor().getVendorName() + "-" + infoNode.getVersion().getVersionName());
            params.add(objectImportDt);

            List<ParamValue> _paramValues = getParamInputs(node);
            if (_paramValues.size() == 0)
                continue;
            List<ParamValue> paramValues = distinctParamValueSameParamCode(_paramValues);
            List<String> paramNames = new LinkedList<String>();
            List<List<Object>> paramValueDefaults2 = new ArrayList<List<Object>>();
            List<Object> paramValueDefaults = new LinkedList<>();
            for (ParamValue paramValue : paramValues) {

                if (paramValue.getParamInput().getReadOnly() || paramValue.getParamInput().getInOut())
                    continue;
                paramNames.add(paramValue.getParamInput().getParamCode());
                paramValueDefaults.add(paramValue.getParamValue());
            }
            paramValueDefaults2.add(paramValueDefaults);
            objectImportDt.setParamNames(paramNames);
            objectImportDt.setParamValues(paramValueDefaults2);
        }

    }

    public void exportDt() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        try {
            HttpServletResponse servletResponse = (HttpServletResponse) facesContext.getExternalContext().getResponse();
            servletResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
            servletResponse.setHeader("Expires", "0");
            servletResponse.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
            servletResponse.setHeader("Pragma", "public");
            servletResponse.setHeader("Content-disposition", "attachment;filename=" + new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date()) + "-exportDt.xlsx");
			/*20180705_hoangnd_fix_bug_da_ngon_ngu_start*/
            locate = languageBean.getLocaleCode();
            String file;
            if (locate.equals("vi")) {
                file = CommonExport.getTemplateMultiExport(MessageUtil.getResourceBundleMessage("key.template.export.dt.vi"));
            } else {
                file = CommonExport.getTemplateMultiExport(MessageUtil.getResourceBundleMessage("key.template.export.dt.en"));
            }
			/*20180705_hoangnd_fix_bug_da_ngon_ngu_end*/
            //Save temp file
            File fileOut = new File(Util.getTEMP_DIR() + File.separator + "tmp" + new Date().getTime() + ".xlsx");
            exportToFile(file, fileOut, servletResponse.getOutputStream());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        facesContext.responseComplete();
    }

    public void exportToFile(String file, File fileOut, OutputStream outStream) {

        try (InputStream is = new FileInputStream(file)) {
            //servletResponse.getOutputStream()

            // get list param checklist value
            String paramChecklist = buildGuilineChecklist(flowRunAction.getFlowRunId());

            try (OutputStream os = new FileOutputStream(fileOut)) {

                Context context = new Context();

                List<ObjectExport> objectExports = new LinkedList<ObjectExport>();

                for (Iterator<String> iterator = groupActions.keySet().iterator(); iterator.hasNext(); ) {
                    String groupName = iterator.next();
                    objectExports.add(new ObjectExport(groupName, new LinkedList<Node>()));
                }

                int groupIndex = 0;
                for (ObjectExport objectExport : objectExports) {
                    LinkedList<Node> nodes = new LinkedList<Node>(this.nodes);
                    for (Node node : nodes) {
                        List<ActionOfFlow> actionOfFlows = new ArrayList<ActionOfFlow>(groupActions.get(objectExport.getGroupName()));
                        boolean contNode = false;
                        if (mapGroupAction.get(node) != null)
                            for (GroupAction groupAction : mapGroupAction.get(node)) {
                                if (groupAction.isNoCommand(node)) {
                                    continue;

                                }
                                if (!groupAction.isDeclare() && actionOfFlows.containsAll(groupAction.getActionOfFlows())) {
                                    contNode = true;
                                    break;
                                }
                                if (objectExport.getGroupName().equals(groupAction.getGroupActionName())) {
                                    actionOfFlows = groupAction.getActionOfFlows();
                                    break;
                                }
                            }
                        if (contNode)
                            continue;

                        NodeAccount nodeAcc = null;
                        int index = 0;
                        for (ActionOfFlow actionOfFlow : actionOfFlows) {
                            // lay thong tin account tac dong cua dau viec
                            if (index == 0) {
                                nodeAcc = getNodeAccGroup(node, actionOfFlow);
                            }
                            List<String> commandExecutes = new MyLinkedList<String>();
                            List<String> commandRollbacks = new MyLinkedList<String>();
                            List<CommandDetail> cmdDetailExecs = new MyLinkedList<CommandDetail>();
                            List<CommandDetail> cmdDetailRollbacks = new MyLinkedList<CommandDetail>();
                            for (ActionDetail actionDetail : actionOfFlow.getAction().getActionDetails()) {
                                if (node.getVendor().equals(actionDetail.getVendor()) &&
                                        node.getNodeType().equals(actionDetail.getNodeType()) &&
                                        node.getVersion().equals(actionDetail.getVersion())) {
                                    for (ActionCommand actionCommand : actionDetail.getActionCommands()) {
                                        String cmd = actionCommand.getCommandDetail().buildCommand(getParamInputs(node), false, actionOfFlow);
                                        if (cmd != null && !cmd.isEmpty()) {
                                            if (actionCommand.getType() == Config.EXECUTE_CMD) {
//												if ((groupIndex == 0 || groupIndex == (objectExports.size() - 1))
//														&& index == 0) {
//													commandExecutes.add(paramChecklist);
//												} else {
                                                commandExecutes.add(cmd);
//												}
//												actionCommand.getCommandDetail().setExpectedResult("aaaaaaaaaa");
                                                cmdDetailExecs.add(actionCommand.getCommandDetail());
//												logger.info(">>>>>>>>>>> add expect result: aaaaaa");
                                            } else if (actionCommand.getType() == Config.ROLLBACK_CMD) {
                                                commandRollbacks.add(cmd);
                                                cmdDetailRollbacks.add(actionCommand.getCommandDetail());
                                            }
                                        }
                                    }
                                }
                            } // end loop for
                            if (nodeAcc != null) {
                                actionOfFlow.setNodeAccount(nodeAcc);
                            } else {
                                actionOfFlow.setNodeAccount(new NodeAccount());
                            }
                            actionOfFlow.getAction().setCommandExecutes(commandExecutes);
                            actionOfFlow.getAction().setCommandRollbacks(commandRollbacks);
                            actionOfFlow.getAction().setLstCmdDetailExec(cmdDetailExecs);
                            actionOfFlow.getAction().setLstCmdDetailRollback(cmdDetailRollbacks);
                            actionOfFlow.setAction(new Cloner().deepClone(actionOfFlow.getAction()));
                            index++;
                        }

                        for (Iterator<ActionOfFlow> iterator = actionOfFlows.iterator(); iterator.hasNext(); ) {
                            ActionOfFlow actionOfFlow = iterator.next();
                            for (ActionOfFlow actionOfFlow2 : actionOfFlows) {
                                if (actionOfFlow.getStepNumberLabel().equals(actionOfFlow2.getPreviousStep()) &&
                                        (actionOfFlow.getIndexParamValue() == null || actionOfFlow2.getIndexParamValue() == null ||
                                                (actionOfFlow.getIndexParamValue() != null && actionOfFlow2.getIndexParamValue() != null
                                                        && actionOfFlow.getIndexParamValue().equals(actionOfFlow2.getIndexParamValue()))) &&
                                        //huynx6 edited Oct 31, 2016
                                        //actionOfFlow2.getIfValue().equalsIgnoreCase("0")
                                        actionOfFlow2.getIsRollback().equals(1L)
                                        ) {
                                    actionOfFlow.getAction().getCommandRollbacks().clear();
                                    actionOfFlow.getAction().getCommandRollbacks().addAll(actionOfFlow2.getAction().getCommandExecutes());
                                    actionOfFlow.getAction().setNameRollback(actionOfFlow2.getAction().getName());
                                    break;
                                }
                            }
                            if (actionOfFlow.getAction().getCommandExecutes().size() == 0) {
                                iterator.remove();
                                continue;
                            }

                            //Set size rollback equa execute commands
                            int tmp = actionOfFlow.getAction().getCommandRollbacks().size() - actionOfFlow.getAction().getCommandExecutes().size();
                            if (tmp > 0) {
                                for (int i = 0; i < Math.abs(tmp); i++) {
                                    actionOfFlow.getAction().getCommandExecutes().add(null);
                                    actionOfFlow.getAction().getLstCmdDetailExec().add(null);
                                }
                            } else if (tmp < 0) {
                                for (int i = 0; i < Math.abs(tmp); i++) {
                                    actionOfFlow.getAction().getCommandRollbacks().add(null);
                                    actionOfFlow.getAction().getLstCmdDetailRollback().add(null);
                                }
                            } else {
                                if (actionOfFlow.getAction().getCommandRollbacks().size() == 0) {
                                    actionOfFlow.getAction().getCommandExecutes().add(null);
                                    actionOfFlow.getAction().getCommandRollbacks().add(null);
                                    actionOfFlow.getAction().getLstCmdDetailExec().add(null);
                                    actionOfFlow.getAction().getLstCmdDetailRollback().add(null);
                                }
                            }
                        }
                        if (actionOfFlows.size() == 0)
                            continue;
                        node.setActionOfFlows(actionOfFlows);
                        Node newNode = new Node();
                        newNode.setActionOfFlows(actionOfFlows);
                        newNode.setNodeCode(node.getNodeCode());
                        newNode.setNodeIp(node.getNodeIp());
                        newNode.setNodeId(node.getNodeId());
                        Node deepClone = new Cloner().deepClone(newNode);

                        objectExport.getNodes().add(deepClone);
                    }
                    groupIndex++;
                }
                //Get Check before/After Group action
                List<Node> nodeCheckBefores = new ArrayList<Node>();
                List<Node> nodeCheckAfters = new ArrayList<Node>();

                //Remove group action no node
                for (Iterator<ObjectExport> iterator2 = objectExports.iterator(); iterator2.hasNext(); ) {
                    ObjectExport objectExport2 = iterator2.next();
                    if (objectExport2.getNodes().size() == 0)
                        iterator2.remove();
                    else {
                        for (Node node : objectExport2.getNodes()) {
                            logger.info("=================== export2 object node: " + node.getNodeId());
                            for (Iterator<ActionOfFlow> iterator = node.getActionOfFlows().iterator(); iterator.hasNext(); ) {
                                ActionOfFlow actionOfFlow = iterator.next();
                                //huynx6 edited Oct 31, 2016
                                //if(actionOfFlow.getIfValue().equals("0"))
                                if (actionOfFlow.getIsRollback() == 1L)
                                    iterator.remove();
                            }
                        }
                    }
                }
                for (int i = 0; i < objectExports.size(); i++) {
                    if (i == 0 || i == objectExports.size() - 1) {
                        ObjectExport objectExport = objectExports.get(i);
                        for (Node node : objectExport.getNodes()) {
                            logger.info("<><><><><><> export object node: " + node.getNodeId());
                        }

                        for (Node node : objectExport.getNodes()) {
                            if (node.getActionOfFlows().size() > 0) {
                                if (i == 0) {
                                    logger.info(">>>>>>>>>> node checklist before: " + node.getNodeId() + "  ===  " + node.getNodeCode());
                                    NodeAccount nodeAcc = null;
                                    try {
                                        nodeAcc = getNodeAccGroup(node, node.getActionOfFlows().get(0));
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                    Node nodeClone = new Cloner().deepClone(node);
                                    nodeClone.getActionOfFlows().clear();

                                    for (ActionOfFlow actionOfFlow : node.getActionOfFlows()) {
                                        ActionOfFlow bak = actionOfFlow;
                                        if (nodeAcc != null) {
                                            bak.setNodeAccount(nodeAcc);
                                        } else {
                                            bak.setNodeAccount(new NodeAccount());
                                        }
                                        nodeClone.getActionOfFlows().add(bak);
                                        //node.getActionOfFlows().remove(bak);
                                    }
                                    nodeCheckBefores.add(nodeClone);
                                }
                                if (i == objectExports.size() - 1) {
                                    logger.info(">>>>>>>>>> node checklist after: " + node.getNodeId() + "  ===  " + node.getNodeCode());
                                    NodeAccount nodeAcc = null;
                                    try {
                                        nodeAcc = getNodeAccGroup(node, node.getActionOfFlows().get(0));
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                    Node nodeClone = new Cloner().deepClone(node);
                                    nodeClone.getActionOfFlows().clear();
                                    for (ActionOfFlow actionOfFlow : node.getActionOfFlows()) {
                                        ActionOfFlow bak = actionOfFlow;
                                        if (nodeAcc != null) {
                                            bak.setNodeAccount(nodeAcc);
                                        } else {
                                            bak.setNodeAccount(new NodeAccount());
                                        }
                                        nodeClone.getActionOfFlows().add(bak);
                                        //node.getActionOfFlows().remove(bak);
                                    }
                                    nodeCheckAfters.add(nodeClone);
                                }
                            }
                        }
                    }
                }
                //Remove 2 groups action check
                if (objectExports.size() > 0)
                    objectExports.remove(0);
                if (objectExports.size() > 0)
                    objectExports.remove(objectExports.size() - 1);


                context.putVar("objectExports", objectExports);
                context.putVar("nodeCheckBefores", nodeCheckBefores);
                context.putVar("nodeCheckAfters", nodeCheckAfters);
                //huynx6 added Nov 5, 2016

                context.putVar("nodes", this.nodes);
//				EmployeeBean manageOfEmployee = new VHRService().getManageOfEmployee();
//				flowRunAction.setApproveBy(manageOfEmployee.getEmail()+" ("+manageOfEmployee.getFullName()+")");
                context.putVar("flowRunAction", this.flowRunAction);
                context.putVar("employees", new ArrayList<>()
                        //new VHRService().getEmployeesOfDepartment(BAN_DVCD.KV1.getValue())
                );

                JxlsHelper.getInstance().processTemplate(is, os, context);

                //Merge, Style Cell
                postProcessTemplate(fileOut, outStream);
            }
            logger.info(">>>>>> FINISH EXPORT FILE");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private String buildGuilineChecklist(Long flowRunId) {
        String guiline = "B1: Truy cap link checklist:10.60.5.245:8844/CHECKLIST/checklistDb \r\n"
                + "B2: Thuc hien nhap IP \r\n"
                + "B3: Kiem tra ket qua danh sach checklist. \r\n "
                + "Ket qua thanh cong la OK, nguoc lai la NOTOK \r\n";
        try {
            guiline += getChecklistVal(flowRunId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return guiline;
    }

    private String getChecklistVal(Long flowRunId) {
        String vals = "";
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("flowRunActionId", flowRunId);
            List<ParamChecklistDatabase> lstParamChecklist = new ParamChecklistDatabaseServiceImpl().findList(filters);
            if (lstParamChecklist != null && !lstParamChecklist.isEmpty()) {
                List<SelectItem> lstAllParamChecklist = new Util().getChecklistName();
                if (!lstAllParamChecklist.isEmpty()) {
                    Map<String, String> mapParamNamecode = new HashMap<>();
                    for (SelectItem item : lstAllParamChecklist) {
                        mapParamNamecode.put((String) item.getValue(), (String) item.getLabel());
                    }

                    for (ParamChecklistDatabase paramCode : lstParamChecklist) {
                        if (mapParamNamecode.get(paramCode.getParamCode()) != null) {
                            vals += " - ".concat(mapParamNamecode.get(paramCode.getParamCode())).concat("\r\n");
                        } else {
                            vals += " - ".concat(paramCode.getParamCode()).concat("\r\n");
                        }
                    }
                }
            }
            vals += "";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return vals;
    }

    /**
     * merge cell, style cell
     *
     * @param fileOut
     * @param outStream
     * @throws IOException
     * @author huynx6
     */
    private void postProcessTemplate(File fileOut, OutputStream outStream) throws IOException {
        // TODO Auto-generated method stub

        Workbook workbook = null;
        try {
            workbook = WorkbookFactory.create(fileOut);

            CellStyle cellStyle = workbook.createCellStyle();
            Font createFont = workbook.createFont();
            createFont.setBoldweight(Font.BOLDWEIGHT_BOLD);
            cellStyle.setFont(createFont);
            cellStyle.setWrapText(true);
            cellStyle.setVerticalAlignment(CellStyle.VERTICAL_CENTER);
            cellStyle.setBorderTop(CellStyle.BORDER_THIN);
            cellStyle.setBorderBottom(CellStyle.BORDER_THIN);
            cellStyle.setBorderLeft(CellStyle.BORDER_THIN);
            cellStyle.setBorderRight(CellStyle.BORDER_THIN);
            for (int iSheet = 0; iSheet < 3; iSheet++) {
                Sheet sheet = workbook.getSheetAt(iSheet);
                int iRow = 0;
                List<Integer> firstRows = new LinkedList<>();
                for (Iterator<Row> rowIterator = sheet.iterator(); rowIterator
                        .hasNext(); ) {
                    Row row = rowIterator.next();
                    if (iRow++ < 8)
                        continue;
                    Cell cell = row.getCell(3);
                    if (cell != null && !cell.getStringCellValue().isEmpty()) {
                        firstRows.add(iRow);
                    }
                    if (cell != null && cellStyle != null)
                        if (rowIterator.hasNext()) {
                            cell.setCellStyle(cellStyle);
                        }
                }
                firstRows.add(iRow + 1);
//				System.err.println(firstRows);
                for (int i = 0; i < firstRows.size() - 1; i++) {
                    Integer integer = firstRows.get(i);
                    int firstRow = integer;
                    int lastRow = firstRows.get(i + 1);
                    if (firstRow < lastRow - 1) {
                        int _firstRow = firstRow - 1;
                        int _lastRow = lastRow - 2;

                        for (CellRangeAddress cellRangeAddress2 : sheet.getMergedRegions()) {
                            if (_firstRow == cellRangeAddress2.getFirstRow())
                                _firstRow++;
                            if (_lastRow == cellRangeAddress2.getLastRow()) {
                                _lastRow--;
                            }
                        }
                        CellRangeAddress cellRangeAddress = new CellRangeAddress(_firstRow, _lastRow, 3, 3);
                        if (_firstRow < _lastRow) {
                            sheet.addMergedRegion(cellRangeAddress);
                            RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, cellRangeAddress, sheet, workbook);
                            RegionUtil.setBorderRight(CellStyle.BORDER_THIN, cellRangeAddress, sheet, workbook);
                            RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, cellRangeAddress, sheet, workbook);
                        }

                        if (iSheet == 1) {
                            if (_firstRow < _lastRow) {
                                CellRangeAddress cellRangeAddress2 = new CellRangeAddress(_firstRow, _lastRow, 10, 10);
                                sheet.addMergedRegion(cellRangeAddress2);
                                RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, cellRangeAddress2, sheet, workbook);
                                RegionUtil.setBorderRight(CellStyle.BORDER_THIN, cellRangeAddress2, sheet, workbook);
                            }
                        }
                    }
                }
                for (int i = firstRows.get(0); i <= sheet.getLastRowNum(); i++) {
                    for (int c = 0; c < 3; c++) {
                        CellRangeAddress cellRangeAddress = new CellRangeAddress(i, i, c, c);
                        RegionUtil.setBorderLeft(CellStyle.BORDER_THIN, cellRangeAddress, sheet, workbook);
                        RegionUtil.setBorderRight(CellStyle.BORDER_THIN, cellRangeAddress, sheet, workbook);
                        RegionUtil.setBorderBottom(CellStyle.BORDER_THIN, cellRangeAddress, sheet, workbook);
                        RegionUtil.setBorderTop(CellStyle.BORDER_THIN, cellRangeAddress, sheet, workbook);
                    }
                }
            }
            //huynx6 added Nov 9, 2016
            //Add template import
            if (flowRunAction.getFileNameImportDt() != null) {
                File file = new File(CommonExport.getFolderSave() + File.separator + flowRunAction.getFileNameImportDt());
                Workbook workbookTemp = WorkbookFactory.create(file);
                for (Iterator<Sheet> iterator = workbookTemp.iterator(); iterator.hasNext(); ) {
                    Sheet sheetTemp = iterator.next();
                    Sheet sheet = workbook.createSheet();
                    ExcelUtil.copySheets(sheet, sheetTemp);
                    workbook.setSheetOrder(sheetTemp.getSheetName(), 10);
                }
                if (workbookTemp != null)
                    workbookTemp.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (workbook != null) {
                workbook.write(outStream);
                //delete tmp file
                fileOut.delete();
            }
        }


    }

    public void deleteFlowRunAction(FlowRunAction flowRunAction) {
        try {
            Date startTime = new Date();
            Object[] objs = new FlowRunActionServiceImpl().openTransaction();
            Session session = (Session) objs[0];
            Transaction tx = (Transaction) objs[1];
            new AccountGroupMopServiceImpl().execteBulk2("delete from AccountGroupMop where flowRunId = ?", session, tx, false, flowRunAction.getFlowRunId());
            new ParamValueServiceImpl().execteBulk2("delete from ParamValue where nodeRun.id.flowRunId = ?", session, tx, false, flowRunAction.getFlowRunId());
            new NodeRunGroupActionServiceImpl().execteBulk2("delete from NodeRunGroupAction where id.flowRunId = ? ", session, tx, false, flowRunAction.getFlowRunId());
            new NodeRunServiceImpl().execteBulk2("delete from NodeRun where id.flowRunId = ?", session, tx, false, flowRunAction.getFlowRunId());
            new FlowRunActionServiceImpl().execteBulk2("delete from FlowRunAction where flowRunId = ? ", session, tx, true, flowRunAction.getFlowRunId());

			/*
			Ghi log tac dong nguoi dung
            */
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), GenerateFlowRunController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.DELETE,
                        flowRunAction.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
        }
    }

    public void approveDT() {
        try {
            if (isApproveMop())
                flowRunAction.setStatus(Config.EXECUTE_AVAILABLE);
            else
                flowRunAction.setStatus(Config.SAVE_DRAFT);
            if (flowRunAction.getFlowRunId() == null) {
                MessageUtil.setErrorMessageFromRes("error.dt.not.save");
                return;
            }

            new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction);
            MessageUtil.setInfoMessageFromRes("info.dt.approve.success");
        } catch (SysException | AppException e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("error.dt.approve.unsuccess");
        }

    }

    //<editor-fold defaultstate="collapsed" desc="Check param when create MOP">
    //20190408_chuongtq start check param when create MOP
    public Boolean checkConfigCondition(String key){
        // lay cau hinh ma quy trinh nguy hiem
        CatConfigServiceImpl catConfigService = new CatConfigServiceImpl();
        Map<String, Object> filter = new HashMap<>();
        filter.put("id.configGroup-EXAC", AamConstants.CFG_CHK_PARAM_CONDITION_GROUP);
        filter.put("id.propertyKey-EXAC", key);
        filter.put("isActive", 1L);
        try {
            List<CatConfig> lstConfig = catConfigService.findList(filter);
            if (lstConfig == null || lstConfig.isEmpty()) {
                return true;
            }else{
                if(lstConfig.get(0) == null || lstConfig.get(0).getPropertyValue() == null || lstConfig.get(0).getPropertyValue().equalsIgnoreCase("0"))
                    return false;
                else
                    return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }

    public void handleUnSelectNode(UnselectEvent event) {
        Node node = (Node) event.getObject();
        mapParamValue.remove(node);
        mapParamValueGroup.remove(node);
        mapGroupAction.remove(node);
        mapParamValueForExport.remove(node);
    }
    public void exportCheckParamDT() {
        HttpServletResponse servletResponse = preHeaderExport();
        String file = CommonExport.getTemplateMultiExport(MessageUtil.getResourceBundleMessage("key.template.file.export.param"));
        logger.info("Export check param when create MOP: " + file);
        try (InputStream is = new FileInputStream(file)) {
            try (OutputStream os = servletResponse.getOutputStream()) {

                Context context = new Context();
                List<ObjectImportDt> params = new LinkedList<>();
                List<String> sheetNames = new LinkedList<>();
                getContextVarExportCheckParam(params,sheetNames);
                context.putVar("params", params);
                context.putVar("sheetNames", sheetNames);

                JxlsHelper.getInstance().setDeleteTemplateSheet(true).processTemplateAtCell(is, os, context, "Sheet2!A1");
//                JxlsHelper.getInstance().processTemplate(is, os, context);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        FacesContext.getCurrentInstance().responseComplete();
    }

    //    20180412-hoangnd-export_dt-START
    private HttpServletResponse preHeaderExport() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse servletResponse = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        servletResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        servletResponse.setHeader("Expires", "0");
        servletResponse.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        servletResponse.setHeader("Pragma", "public");
        try {
            servletResponse.setHeader("Content-disposition", "attachment;filename="
                    + URLEncoder.encode(MessageUtil.getResourceBundleMessage("key.template.file.export.param").replace(".xlsx", "") + "_" + selectedFlowTemplates.getFlowTemplateName() + ".xlsx", "UTF-8"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return servletResponse;
    }

    private void getContextVarExportCheckParam(List<ObjectImportDt> params,List<String> sheetNames) {

        try {
            Set<InfoNode> infoNodes = new HashSet<>();
            for (ActionOfFlow actionOfFlow : selectedFlowTemplates.getActionOfFlows()) {
                for (ActionDetail actionDetail : actionOfFlow.getAction().getActionDetails()) {
                    infoNodes.add(new InfoNode(actionDetail.getVendor(), actionDetail.getVersion(), actionDetail.getNodeType()));
                }
            }

            List<Integer> rows = new LinkedList<>();
            int countRow = 0;
            for (InfoNode infoNode : infoNodes) {
                ObjectImportDt objectImportDt = new ObjectImportDt();
                objectImportDt.setVendor(infoNode.getVendor());
                objectImportDt.setVersion(infoNode.getVersion());
                objectImportDt.setNodeType(infoNode.getNodeType());
                List<Node> lstNode = new ArrayList<>();
                List<List<Object>> paramValueDefaults2 = new ArrayList<>();
                List<String> paramNames = new LinkedList<>();
                boolean checkParamNames = false;
                for (Map.Entry<Node, List<ParamValue>> entry : mapParamValueForExport.entrySet()) {
                    if (entry.getKey().getNodeCode() != null) {
                        String nodeCode = entry.getKey().getNodeCode();
                        Map<String, Object> filters = new HashMap<>();
                        filters.put("nodeCode-EXAC", nodeCode);
                        List<Node> findList = new NodeServiceImpl().findList(filters);

                        if (!findList.isEmpty()) {
                            for (Node node : findList) {
                                if (infoNode.getVendor().getVendorName().equals(node.getVendor().getVendorName())
                                        && infoNode.getVersion().getVersionName().equals(node.getVersion().getVersionName())
                                        && infoNode.getNodeType().getTypeName().equals(node.getNodeType().getTypeName())) {

                                    List<Object> paramValueDefaults;
                                    List<ParamValue> paramValues = distinctParamValueSameParamCode(entry.getValue());
                                    int maxRow = 0;
                                    String[] mParamValue;

                                    int j = 0;
                                    for (ParamValue paramValue : paramValues) {
//									if (paramValue.isIsDeclare()) {
                                        if (checkParamNames) {
                                            if (paramNames.size() == 0) {
                                                paramNames.add(paramValue.getParamInput().getParamCode());
                                            } else {
                                                if (paramNames.get(j).equals("")) {
                                                    paramNames.set(j, paramValue.getParamInput().getParamCode());
                                                }
                                            }
                                        } else {
                                            paramNames.add(paramValue.getParamInput().getParamCode());
                                        }

                                        if (paramValue.getParamValue() != null) {
                                            mParamValue = paramValue.getParamValue().split(";", -1);
                                            if (maxRow < mParamValue.length) {
                                                maxRow = mParamValue.length;
                                            }
                                        }
//									} else {
//										if (!checkParamNames) {
//											paramNames.add("");
//										}
//									}
                                        j++;
                                    }
                                    //them header result
                                    if (!paramNames.contains("RESULT CONDITION"))
                                        paramNames.add("RESULT CONDITION");

                                    if (maxRow == 0) {
                                        paramValueDefaults = new LinkedList<>();
                                        for (ParamValue paramValue : paramValues) {
//										if (paramValue.isIsDeclare()) {
                                            if (paramValue.getParamInput().getInOut()) {
                                                StringBuilder sb = new StringBuilder();
                                                sb.append(MessageUtil.getResourceBundleMessage("info.reference.value"));
                                                sb.append(" (");
                                                sb.append(paramValue.getParamInput().getCmdInOut());
                                                sb.append(")");
                                                paramValueDefaults.add(sb.toString());
                                            } else {
                                                paramValueDefaults.add("");
                                            }
//										} else {
//											paramValueDefaults.add("");
//										}
                                        }
                                        paramValueDefaults2.add(paramValueDefaults);
                                        countRow++;
                                        rows.add(countRow);
                                        lstNode.add(node);
                                    } else {
                                        for (int i = 0; i < maxRow; i++) {
                                            paramValueDefaults = new LinkedList<>();
                                            String resultCondition = "";
                                            for (ParamValue paramValue : paramValues) {
//											if (paramValue.isIsDeclare()) {
                                                if (paramValue.getParamInput().getInOut()) {
                                                    StringBuilder sb = new StringBuilder();
                                                    sb.append(MessageUtil.getResourceBundleMessage("info.reference.value"));
                                                    sb.append(" (");
                                                    sb.append(paramValue.getParamInput().getCmdInOut());
                                                    sb.append(")");
                                                    paramValueDefaults.add(sb.toString());
                                                } else {
                                                    if (paramValue.getParamValue() != null) {
                                                        mParamValue = paramValue.getParamValue().split(";", -1);
                                                        if (mParamValue.length > i) {
                                                            paramValueDefaults.add(mParamValue[i]);
                                                            //them chuoi string result
                                                            CheckParamCondition cpc = mapCheckParamCondition.get(nodeCode + "#" + paramValue.getParamInput().getParamCode().toLowerCase().trim() + "#" + mParamValue[i]);
                                                            String str = "";
                                                            if (cpc != null) {
                                                                str = paramValue.getParamInput().getParamCode() + "-" + cpc.getResult()
                                                                        + ("".equals(cpc.getCondition()) ? "" : "(" + cpc.getCondition() + ")");
                                                            }
//                                                            resultCondition = ("".equals(resultCondition) ? str  : resultCondition + (!"".equals(str) ? ";\n\r" + str  : ""));
                                                            resultCondition = ("".equals(resultCondition) ? str : resultCondition + (!"".equals(str) ? "\n\r;" + str : ""));
                                                        } else {
//                                                            paramValueDefaults.add(mParamValue[0]);
                                                            paramValueDefaults.add("");
                                                            //them chuoi string result
                                                            CheckParamCondition cpc = mapCheckParamCondition.get(nodeCode + "#" + paramValue.getParamInput().getParamCode().toLowerCase().trim() + "#" + "");
                                                            String str = "";
                                                            if (cpc != null) {
                                                                str = paramValue.getParamInput().getParamCode() + "-" + cpc.getResult()
                                                                        + ("".equals(cpc.getCondition()) ? "" : "(" + cpc.getCondition() + ")");
                                                            }
                                                            resultCondition = ("".equals(resultCondition) ? str : resultCondition + (!"".equals(str) ? "\n\r;" + str : ""));
                                                        }
                                                    } else {
                                                        paramValueDefaults.add("");
                                                    }
                                                }
//											}
                                            }
                                            //cong don chuoi string vao ket qua
                                            paramValueDefaults.add(resultCondition);


                                            countRow++;
                                            paramValueDefaults2.add(paramValueDefaults);
                                            lstNode.add(node);
                                            rows.add(countRow);
                                        }
                                    }
                                    checkParamNames = true;
                                }
                            }
                            objectImportDt.setParamNames(paramNames);
                            objectImportDt.setParamValues(paramValueDefaults2);
                            objectImportDt.setNodes(lstNode);
                        }
                    }
                }
                String sName = infoNode.getNodeType().getTypeName() + "-" + infoNode.getVendor().getVendorName() + "-" + infoNode.getVersion().getVersionName();
                if (sName.length() > 31) {
                    sName = sName.substring(0, 30);
                }
                sheetNames.add(sName);
                params.add(objectImportDt);
                objectImportDt.setRows(rows);
                rows = new LinkedList<>();
                countRow = 0;
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
    //20190408_chuongtq end check param when create MOP
    //</editor-fold>
    //20180831_tudn_start cap nhat trang thai
    public String indexTab = "0";

    public String getIndexTab() {
        return indexTab;
    }

    public void setIndexTab(String indexTab) {
        this.indexTab = indexTab;
    }
    //20180831_tudn_end cap nhat trang thai

    //20181119_tudn_start them danh sach lenh blacklist
    public boolean checkCmdBlacklist() {
        try {
            blackListCommand = "";
            for (Node node : nodes) {
                if (mapGroupAction != null) {
                    CommandBlacklistController cmdBlController = new CommandBlacklistController();
                    Map<String, Object> filters = new HashMap<>();
                    filters.put("vendor.vendorId", node.getVendor().getVendorId());
                    filters.put("version.versionId", node.getVersion().getVersionId());
                    filters.put("nodeType.typeId", node.getNodeType().getTypeId());
                    List<CommandBlacklist> cmdBlacklists = new CommandBlacklistServiceImpl().findList(filters);
                    if (cmdBlacklists != null && !cmdBlacklists.isEmpty()) {
                        for (GroupAction groupAction : mapGroupAction.get(node)) {
                            if (groupAction.isDeclare()) {
                                for (ActionOfFlow actionOfFlow : groupAction.getActionOfFlows()) {
                                    for (ActionDetail actionDetail : actionOfFlow.getAction().getActionDetails()) {
                                        if (actionDetail.getVendor().equals(node.getVendor())
                                                && actionDetail.getVersion().equals(node.getVersion())
                                                && actionDetail.getNodeType().equals(node.getNodeType())) {
                                            for (ActionCommand actionCommand : actionDetail.getActionCommands()) {
                                                String cmd = actionCommand.getCommandDetail().buildCommand(getParamInputs(node), false, actionOfFlow);
                                                // check cmd co phai blacklist hay khong
                                                if (cmdBlController.checkCommandBlacklist(cmd, cmdBlacklists)) {
                                                    blackListCommand = actionDetail.getAction().getName();
                                                    return true;
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return true;
        }

        return false;
    }

    public boolean checkCloneCmdBlacklist(List<NodeRun> nodeRun, Map<Node, List<GroupAction>> mapGroupActionClone, List<ParamValue> paramValue) {
        try {
            blackListCommand = "";
            for (NodeRun nodeRunGroupAction : nodeRun) {
                CommandBlacklistController cmdBlController = new CommandBlacklistController();
                Map<String, Object> filters = new HashMap<>();
                filters.put("vendor.vendorId", nodeRunGroupAction.getNode().getVendor().getVendorId());
                filters.put("version.versionId", nodeRunGroupAction.getNode().getVersion().getVersionId());
                filters.put("nodeType.typeId", nodeRunGroupAction.getNode().getNodeType().getTypeId());
                List<CommandBlacklist> cmdBlacklists = new CommandBlacklistServiceImpl().findList(filters);
                if (cmdBlacklists != null && !cmdBlacklists.isEmpty()) {
                    for (GroupAction groupAction : mapGroupActionClone.get(nodeRunGroupAction.getNode())) {
                        if (groupAction.isDeclare()) {
                            for (ActionOfFlow actionOfFlow : groupAction.getActionOfFlows()) {
                                for (ActionDetail actionDetail : actionOfFlow.getAction().getActionDetails()) {
                                    if (actionDetail.getVendor().equals(nodeRunGroupAction.getNode().getVendor())
                                            && actionDetail.getVersion().equals(nodeRunGroupAction.getNode().getVersion())
                                            && actionDetail.getNodeType().equals(nodeRunGroupAction.getNode().getNodeType())) {
                                        for (ActionCommand actionCommand : actionDetail.getActionCommands()) {
                                            String cmd = actionCommand.getCommandDetail().buildCommand(paramValue, false, actionOfFlow);
                                            // check cmd co phai blacklist hay khong
                                            if (cmdBlController.checkCommandBlacklist(cmd, cmdBlacklists)) {
                                                blackListCommand = actionDetail.getAction().getName();
                                                return true;
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }

                }
            }

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return true;
        }

        return false;
    }

    public void sureSaveDT() {
        sureMopContainCmdBlacklist = true;
        saveDT();
    }

    public void sureCloneDT() {
        sureMopContainCmdBlacklist = true;
        cloneFlowRun(cloneFlowRunAction);
    }
    //20181119_tudn_end them danh sach lenh blacklist

    public boolean saveDT() {
        //20181119_tudn_start them danh sach lenh blacklist
        if (!sureMopContainCmdBlacklist) {
            if (checkCmdBlacklist()) {
                messageBlacklist = String.format(MessageUtil.getResourceBundleMessage("view.dialog.header.save.flowrunaction.containCmdBlacklist.confirm"),blackListCommand);
                RequestContext.getCurrentInstance().execute("PF('comfirmSaveMopCmdBlacklist').show()");
                return false;
            }
        }
        //20181119_tudn_end them danh sach lenh blacklist
        boolean isCmdReboot = false;
		/*20181207_hoangnd_bao loi neu chon cac node inactive_start*/
        int totalNode = 0;
        int inactiveNode = 0;
		/*20181207_hoangnd_bao loi neu chon cac node inactive_end*/
		/*
		 * check fill all account execute on each group
		 */
        try {
            for (Entry<Node, Map<String, NodeAccount>> entry : mapAccGroupAction.entrySet()) {
                int count = 0;
                for (Entry<String, NodeAccount> a : entry.getValue().entrySet()) {
                    logger.info(entry.getKey().getNodeIp() + " -- " + a.getValue().getUsername() + " -- " + a.getKey());
                    count++;
                }
            }

            if (nodes != null && nodes.size() > 0) {
                for (Node node : nodes) {
					/*20181207_hoangnd_bao loi neu chon cac node inactive_start*/
                    totalNode += 1;
					/*20181207_hoangnd_bao loi neu chon cac node inactive_end*/
                    //20180831_tudn_start cap nhat trang thai
                    if (node.getActive() != null && node.getActive() == 1L) {
                        //20180831_tudn_end cap nhat trang thai
                        if (mapGroupAction.get(node) != null) {
                            List<GroupAction> lstGroupAction = mapGroupAction.get(node);
                            ActionDetail actionDetail = null;
                            for (GroupAction group : lstGroupAction) {

                                if (group.isDeclare()
                                        && group.getActionOfFlows() != null
                                        && !group.getActionOfFlows().isEmpty()) {

                                    actionDetail = group.getActionOfFlows().get(0).getAction().getActionDetails().get(0);

                                    if (actionDetail.getNodeType().equals(node.getNodeType())
                                            && actionDetail.getVendor().equals(node.getVendor())
                                            && actionDetail.getVersion().equals(node.getVersion())) {
                                        if (group.getNodeAccount() == null) {
                                            MessageUtil.setErrorMessageFromRes("label.err.fill.account.group");
                                            return false;
                                        }
                                        //20180831_tudn_start cap nhat trang thai
                                        else {
                                            if (group.getNodeAccount().getActive() == null || group.getNodeAccount().getActive() == 0L) {
                                                MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("label.err.not.acc"), node.getNodeCode(), group.getGroupActionName(), group.getNodeAccount().getUsername()));
                                                indexTab = String.valueOf(nodes.indexOf(node) != -1 ? nodes.indexOf(node) : "");
                                                return false;
                                            }
                                        }
                                        //20180831_tudn_end cap nhat trang thai
                                    }
                                    // START anhnt2 - Check command contains reboot (All DV)
                                    for (GroupAction groupAction : mapGroupAction.get(node)) {
                                        if (isCmdReboot) {
                                            break;
                                        }
                                        List<ActionOfFlow> actionOfFlows = groupAction.getActionOfFlows();
                                        for (ActionOfFlow actionOfFlow : actionOfFlows) {
                                            Action action = actionOfFlow.getAction();
                                            List<ActionDetail> actionDetail1s = action.getActionDetails();
                                            for (ActionDetail actionDetail1 : actionDetail1s) {
                                                if (actionDetail1 != null) {
                                                    List<ActionCommand> actionCommands = actionDetail1.getActionCommands();
                                                    if (actionCommands != null && actionCommands.size() > 0) {
                                                        for (ActionCommand actionCommand : actionCommands) {
                                                            CommandDetail commandDetail = actionCommand.getCommandDetail();
                                                            CommandTelnetParser commandTelnetParser = commandDetail.getCommandTelnetParser();
                                                            if (commandTelnetParser != null) {
                                                                if (commandTelnetParser.getCmd().toUpperCase().contains("reboot".toUpperCase())) {
                                                                    isCmdReboot = true;
                                                                }
                                                            }
                                                        }
                                                    }
                                                }
                                            }

                                        }
                                    } // END anhnt2 - Check command contains reboot
                                }
                            }
                        }
                    }
					/*20181207_hoangnd_bao loi neu chon cac node inactive_start*/
                    else {
                        inactiveNode += 1;
                    }
					/*20181207_hoangnd_bao loi neu chon cac node inactive_end*/
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

		/*20181207_hoangnd_bao loi neu chon cac node inactive_start*/
        if (totalNode == inactiveNode) {
            MessageUtil.setErrorMessageFromRes("label.error.not.select.node");
            return false;
        }
		/*20181207_hoangnd_bao loi neu chon cac node inactive_end*/

		/*
		 * Check fill paramvalue
		 */
        if (!validateInputParam()) {
            MessageUtil.setErrorMessageFromRes("error.save.dt.fail");
            return false;
        }

        // When command contain reboot and list ip > 1
        if (isCmdReboot && nodes.size() > 1) {
            MessageUtil.setErrorMessageFromRes("error.node.reboot.exceed");
            return false;
        }
        // Check same next of action screen
        List<String> ipServers = new ArrayList<>();
        for (Node node : nodes) {
            ipServers.add(node.getNodeIp());
        }

        List<Module> targets = null;
        try {
            com.viettel.model.Action actionService = null;
            if (flowRunAction.getServiceActionId() != null) {
                actionService = new ActionServiceImpl().findById(flowRunAction.getServiceActionId());
                if (actionService != null && actionService.getImpactProcess() != null) {
                    targets = iimService.findModules(actionService.getImpactProcess().getNationCode(), new ArrayList<>(), new ArrayList<>(), ipServers, AamConstants.KB_TYPE.BD_SERVER);
                    List<String> moduleTypes = new ActionServiceImpl().findModuleTypeNotDb();

                    if (moduleTypes != null && !moduleTypes.isEmpty()) {
                        // End process for check only BD server
                        for (Module module : targets) {
                            if (moduleTypes.contains(module.getModuleTypeCode())) {
                                MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("mop.common.uctt.not_support_md"), module.getModuleName(), module.getModuleTypeCode()));
                                return false;
                            }
                        }
                    }
                }
            }

        } catch (Exception ex) {
        }


        //flowRunAction;
        List<NodeRun> nodeRuns = new ArrayList<NodeRun>();
//		flowRunAction.setCreateDate(new Date());
        if (flowRunAction.getFlowRunName() != null)
            flowRunAction.setFlowRunName(flowRunAction.getFlowRunName().trim());
        flowRunAction.setStatus(0L);
        flowRunAction.setCreateBy(SessionWrapper.getCurrentUsername());
        //huynx6 added Nov 7, 2016
        if (flowRunAction.getFileImportDT() != null) {
            try {
                String path = CommonExport.getPathSaveFileExport(flowRunAction.getFileImportDT().getFileName());
                String fileName = path.substring(path.lastIndexOf(File.separator));
                FileOutputStream out = new FileOutputStream(path);
                out.write(flowRunAction.getFileImportDT().getContents());
                out.close();
                flowRunAction.setFileNameImportDt(fileName);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }


//		Object[] objs = new FlowRunActionServiceImpl().openTransaction();
//		Session session = (Session) objs[0];
//		Transaction tx = (Transaction) objs[1];
        Long flowRunActionId = null;
        try {
            Date startTime = new Date();

            if (flowRunAction.getFlowRunId() == null) {
                flowRunAction.setCrNumber(Constant.DEFAULT_CR_NUMBER_INFRA);
//				flowRunAction.setCrNumber(Config.CR_DEFAULT);
                flowRunAction.setCreateBy(SessionWrapper.getCurrentUsername());
                flowRunActionId = new FlowRunActionServiceImpl().save(flowRunAction, null, null, true);
                flowRunAction.setFlowRunId(flowRunActionId);
                if (flowRunAction.getExecuteType() == 3 && flowRunAction.getFlowTemplates() != null && flowRunAction.getFlowTemplates().getTemplateType() == 3)
                    flowRunAction.setStatus(1L);
                else
                    flowRunAction.setStatus(0L);
            } else {
                flowRunActionId = flowRunAction.getFlowRunId();
                new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction, null, null, true);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), GenerateFlowRunController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.CREATE,
                            flowRunAction.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
            }

            logger.info("start process delete and update value");

            //20181119_tudn_start them danh sach lenh blacklist
            if (sureMopContainCmdBlacklist) {
                sureMopContainCmdBlacklist = false;
                flowRunAction.setMopType(4L);
            }
            //20181119_tudn_end them danh sach lenh blacklist

            new AccountGroupMopServiceImpl().execteBulk2("delete from AccountGroupMop where flowRunId = ?", null, null, true, flowRunAction.getFlowRunId());
            new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction, null, null, true);
            //20180620_tudn_start ghi log DB
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), GenerateFlowRunController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.CREATE,
                        flowRunAction.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            //20180620_tudn_end ghi log DB
            new ParamValueServiceImpl().execteBulk2("delete from ParamValue where nodeRun.id.flowRunId = ?", null, null, true, flowRunAction.getFlowRunId());
            new NodeRunGroupActionServiceImpl().execteBulk2("delete from NodeRunGroupAction where id.flowRunId = ? ", null, null, true, flowRunAction.getFlowRunId());
            new NodeRunServiceImpl().execteBulk2("delete from NodeRun where id.flowRunId = ?", null, null, true, flowRunAction.getFlowRunId());
            List<ParamValue> paramValues = new ArrayList<ParamValue>();

            logger.info(">>>>>>>>>>>>>>>> start update and insert");

            List<NodeRunGroupAction> nodeRunGroupActions = new ArrayList<NodeRunGroupAction>();
            List<AccountGroupMop> lstAccGroupMop = new ArrayList<>();
            if (nodes != null && nodes.size() > 0) {
                for (Node node : nodes) {
                    //20180831_tudn_start cap nhat trang thai
                    if (node.getActive() != null && node.getActive() == 1L) {
                        //20180831_tudn_end cap nhat trang thai
                        NodeRun nodeRun = new NodeRun(new NodeRunId(node.getNodeId(), flowRunAction.getFlowRunId()), flowRunAction, node);
                        nodeRuns.add(nodeRun);
                        List<ParamValue> _paramValueOfNode = mapParamValue.get(node);
                        if (_paramValueOfNode != null) {
                            for (ParamValue paramValue : _paramValueOfNode) {
                                //paramValue.setFlowRunAction(flowRunAction);
                                paramValue.setNodeRun(nodeRun);
                                paramValue.setCreateTime(new Date());
                                paramValue.setParamValueId(null);
                            }
                            paramValues.addAll(_paramValueOfNode);
                        }
                        if (mapGroupAction.get(node) != null)
                            for (GroupAction groupAction : mapGroupAction.get(node)) {
                                if (groupAction.isDeclare() && groupAction.getActionOfFlows().size() > 0 && !groupAction.isNoCommand(node)) {
                                    NodeRunGroupAction nodeRunGroupAction = new NodeRunGroupAction(
                                            new NodeRunGroupActionId(node.getNodeId(),
                                                    flowRunAction.getFlowRunId(),
                                                    groupAction.getActionOfFlows().get(0).getStepNum())
                                            , groupAction.getActionOfFlows().get(0), nodeRun);
                                    nodeRunGroupActions.add(nodeRunGroupAction);
                                }
                                // hanhnv68 add 2016 12 01
                                // add thong tin account tac dong cho tung dau viec
                                if (groupAction.isDeclare() && (groupAction.getNodeAccount() != null)) {
                                    AccountGroupMop accGroup = new AccountGroupMop();
//							accGroup.setGroupOrderRun(groupAction.getActionOfFlows().get(0).getGroupActionOrder());
                                    accGroup.setNodeAccountId(groupAction.getNodeAccount().getId());
                                    accGroup.setNodeId(node.getNodeId());
                                    accGroup.setFlowRunId(flowRunActionId);
                                    accGroup.setActionOfFlowId(groupAction.getActionOfFlows().get(0).getStepNum());

                                    lstAccGroupMop.add(accGroup);
                                }
                                // end hanhnv68 add 2016 12 01

                            } // end loop for group action
                    }
                }
//				session.clear();
                flowRunAction.setNodeRuns(nodeRuns);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), GenerateFlowRunController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.CREATE,
                            nodeRuns.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
                new AccountGroupMopServiceImpl().saveOrUpdate(lstAccGroupMop, null, null, true); // luu thong tin account
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), GenerateFlowRunController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.CREATE,
                            lstAccGroupMop.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
                new NodeRunServiceImpl().saveOrUpdate(nodeRuns, null, null, true);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), GenerateFlowRunController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.CREATE,
                            nodeRuns.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
                new ParamValueServiceImpl().saveOrUpdate(paramValues, null, null, true);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), GenerateFlowRunController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.CREATE,
                            paramValues.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB

                //20190408_chuongtq start check param when create MOP
                mapCheckParamCondition.clear();
                if(btnSave && checkConfigCondition){
                    if(!new CheckParamCondition().checkParamCondition(selectedFlowTemplates.getParamConditions(), nodes, mapParamValue, mapCheckParamCondition, true)) {
                        if (btnSave) {
                            btnSave = false;
                            RequestContext.getCurrentInstance().execute("PF('cloneResultDlg').show();");
                            if (addNew) {
                                flowRunAction.setFlowRunId(null);
                                flowRunAction.setStatus(0L);
                               
                            }
                            this.mapParamValueForExport.clear();
                            this.mapParamValueForExport = new HashMap<>(mapParamValue);
                            return false;
                        }
                    }
                }
                //20190408_chuongtq end check param when create MOP
//		 		for (NodeRunGroupAction nodeRunGroupAction2 : nodeRunGroupActions) {
//		 			//session.merge(nodeRunGroupAction2.getNodeRun());
//		 			session.merge(nodeRunGroupAction2);
//				}
//		 		session.flush();
//		 		session.clear();
                new NodeRunGroupActionServiceImpl().saveOrUpdate(nodeRunGroupActions, null, null, true);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), GenerateFlowRunController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.CREATE,
                            nodeRunGroupActions.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB

            } else {

            }

            logger.info("end process delete and update value");

            //Save File to database
            String file2 = CommonExport.getTemplateMultiExport(MessageUtil.getResourceBundleMessage("key.template.export.dt"));
            File fileTemp2 = new File("tmp" + new Date().getTime() + ".xlsx");
            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
            exportToFile(file2, fileTemp2, outStream);
            flowRunAction.setFileContent(outStream.toByteArray());

            // Process for service action. Create 1 action for case BD database.
            com.viettel.model.Action actionCreateMobDb = null;
            if (isCmdReboot) {
                actionCreateMobDb = ActionUtil.createMobDb(nodes, flowRunAction);
                if (actionCreateMobDb != null) {
                    flowRunAction.setServiceActionId(actionCreateMobDb.getId());
                }
            }
            try {
                new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                // If an error occurs, delete record created by call createMobDb
                if (actionCreateMobDb != null) {
                    new ActionServiceImpl().delete(actionCreateMobDb);
                }
            }
			/*
			Ghi log tac dong nguoi dung
            */
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), GenerateFlowRunController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.UPDATE,
                        flowRunAction.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            //MessageUtil.setInfoMessageFromRes("info.save.dt.success");

            logger.info(">>>>>>>> Update DT to GNOC");
            // Cap nhat file DT sang GNOC
            FileOutputStream os = null;
            File fileOut = null;
            try {
                if (flowRunAction.getCrNumber() != null
                        && !flowRunAction.getCrNumber().equals(Config.CR_DEFAULT)
                        && !flowRunAction.getCrNumber().equals(Config.CR_AUTO_DECLARE_CUSTOMER)) {
                    if (GNOCService.isCanUpdateDT(flowRunAction.getCrNumber())) {
                        File fileTemp = new File("tmp" + new Date().getTime() + ".xlsx");
                        fileOut = new File("tmp_out" + new Date().getTime() + ".xlsx");
                        String file = CommonExport
                                .getTemplateMultiExport(MessageUtil
                                        .getResourceBundleMessage("key.template.export.dt"));

                        os = new FileOutputStream(fileOut);
                        exportToFile(file, fileTemp, os);
                        os.flush();

                        String[] nodeIPs = null;
                        String[] nodeEffects = null;

                        if (nodes != null) {
                            nodeIPs = new String[nodes.size()];
                            nodeEffects = new String[nodes.size()];
                            for (int i = 0; i < nodes.size(); i++) {
                                nodeIPs[i] = nodes.get(i).getNodeIp();
                                nodeEffects[i] = nodes.get(i).getEffectIp();
                            }
                        }
                        ResourceBundle bundle = ResourceBundle.getBundle("config");
                        CrForOtherSystemServiceImplService service = new CrForOtherSystemServiceImplServiceLocator();
                        CrForOtherSystemService gnocService = service.getCrForOtherSystemServiceImplPort(new URL(bundle.getString("ws_gnoc_new")));

                        // thenv_20180618_countryCode_start
                        String nationCode = flowRunAction.getCountryCode() == null ? AamConstants.VNM : flowRunAction.getCountryCode().getCountryCode();
                        if (nationCode.equalsIgnoreCase(AamConstants.VTP)) {
                            nationCode = AamConstants.VNM;
                        }
                        ResultDTO resultDTO = gnocService.updateDtInfo(bundle.getString("ws_gnoc_user"),
                                com.viettel.util.PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), SessionWrapper.getCurrentUsername(), flowRunAction.getCrNumber(),
                                flowRunAction.getFlowRunName(), nodeIPs, nodeEffects, fileOut.getName(), Base64.encodeBase64String(FileUtils.readFileToByteArray(fileOut)),
                                "", "", null, nationCode);
                        // thenv_20180618_countryCode_end
                        logger.info(resultDTO.getKey() + "\t" + resultDTO.getMessage());

//						boolean result = GNOCService.updateDTInfo(
//								SessionUtil.getCurrentUsername(),
//								flowRunAction.getCrNumber(), nodeIPs,
//								flowRunAction.getFlowRunName(), fileOut);

//						logger.info(result ? "UPDATE DT GNOC SUCCESS" : "UPDATE DT GNOC FAIL");
                    }
                }
                logger.info("???????????????? FFFIIIINISSSH");
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            } finally {
                if (os != null) {
                    try {
                        os.close();
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
                if (fileOut != null) {
                    fileOut.delete();
                }
            }

			/*
			 * Goi tien trinh chay tu dong lay tham so tham chieu
			 * Sau do cap nhat lai DT
			 */
//			createParamValueInOut(flowRunAction);
            MessageUtil.setInfoMessageFromRes("info.save.dt.success");
            return true;
        } catch (Exception e) {
//			if(tx.getStatus()!=TransactionStatus.ROLLED_BACK)
//				tx.rollback();
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("error.save.dt.fail");
            return false;
        } finally {
//			if(session != null && session.isOpen())
//				try {
//					session.close();
//				} catch (Exception e2) {
//					// TODO: handle exception
//				}

        }

    }

    private boolean validateInputParam() {
        boolean check = true;
		/*if (nodes != null && !nodes.isEmpty()) {
			for (Node node : nodes) {

				List<ParamValue> _paramValueOfNode = mapParamValue.get(node);
				if(_paramValueOfNode!=null){
					for (ParamValue paramValue : _paramValueOfNode) {
						if (paramValue.getParamValue() == null || paramValue.getParamValue().trim().isEmpty()) {
							return false;
						}
					}
				}
			}
		}*/

        return check;
    }

    private void createParamValueInOut(FlowRunAction flowRunAction) {
        try {
            String encrytedMess = new String(Base64.encodeBase64(flowRunAction.getFlowRunId().toString().getBytes()), "UTF-8");

            String serverIp = MessageUtil.getResourceBundleConfig("process_socket_ip");
            int serverPort = Integer.parseInt(MessageUtil.getResourceBundleConfig("process_socket_port"));
            SocketClient client = new SocketClient(serverIp, serverPort);
            client.sendMsg(encrytedMess);

            String socketResult = client.receiveResult();
            if (socketResult != null && socketResult.contains("NOK")) {
                throw new Exception(socketResult);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Sao chep Kich ban DT
     *
     * @param flowRunAction
     * @author huynx6
     */
    public void cloneFlowRun(FlowRunAction flowRunAction) {
        try {
            //20181119_tudn_start them danh sach lenh blacklist
            cloneFlowRunAction = null;
            FlowRunAction clFlowRunAction = new FlowRunActionServiceImpl().findById(flowRunAction.getFlowRunId());
            //20181119_tudn_end them danh sach lenh blacklist
            Date startTime = new Date();
            if (flowTemplatesService.findById(flowRunAction.getFlowTemplates().getFlowTemplatesId()).getStatus() != 9) {
                MessageUtil.setErrorMessageFromRes("error.clone.dt.fail");
                MessageUtil.setErrorMessageFromRes("error.template.not.approved");
                return;
            }

//			FlowRunAction flowRunActionClone = flowRunAction.clone();
            //20181119_tudn_start them danh sach lenh blacklist
            FlowRunAction flowRunActionClone = clFlowRunAction.clone();
            cloneFlowRunAction = clFlowRunAction;
            //20181119_tudn_end them danh sach lenh blacklist

            flowRunActionClone.setCreateBy(SessionWrapper.getCurrentUsername());
            Long flowRunId = flowRunAction.getFlowRunId();
            List<NodeRun> nodeRunClones = new NodeRunServiceImpl().findList("from NodeRun where id.flowRunId = ?", -1, -1, flowRunId);
            List<NodeRunGroupAction> nodeRunGroupActionClones = new NodeRunGroupActionServiceImpl().findList("from NodeRunGroupAction where id.flowRunId = ?", -1, -1, flowRunId);
            List<ParamValue> paramValueClones = new ParamValueServiceImpl().findList("from ParamValue where nodeRun.id.flowRunId = ?", -1, -1, flowRunId);
            //20190408_chuongtq start check param when create MOP
            mapCheckParamCondition.clear();
            List<Node> ns = new ArrayList<>();
            for (NodeRun nr : nodeRunClones) {
                ns.add(nr.getNode());
            }
            Map<Node, List<ParamValue>> mapParamValues = new HashMap<>();
            String[] actionOfFolowIds;
            for (ParamValue pv : paramValueClones) {
                if (mapParamValues.containsKey(pv.getNodeRun().getNode())) {
                    mapParamValues.get(pv.getNodeRun().getNode()).add(pv);
                } else {
                    List<ParamValue> pvs = new ArrayList<>();
                    pvs.add(pv);
                    mapParamValues.put(pv.getNodeRun().getNode(), pvs);
                }
            }

            if(checkConfigCondition && !new CheckParamCondition().checkParamCondition(flowRunAction.getFlowTemplates().getParamConditions(), ns, mapParamValues, mapCheckParamCondition, true)){
                selectedFlowTemplates = flowRunActionClone.getFlowTemplates();
                this.mapParamValueForExport.clear();
                this.mapParamValueForExport = mapParamValues;
                RequestContext.getCurrentInstance().execute("PF('cloneResultDlg').show();");
                return;
            }
            //20190408_chuongtq end check param when create MOP
            //20181119_tudn_start them danh sach lenh blacklist
            List<NodeRunGroupActionId> nodeRunGroupActions = new NodeRunGroupActionServiceImpl().findNodeGroup(flowRunAction.getFlowRunId());
            Map<Node, List<GroupAction>> mapGroupActionClone = new HashMap<Node, List<GroupAction>>();
            LinkedListMultimap<String, ActionOfFlow> groupActionsClones = LinkedListMultimap.create();
            List<ActionOfFlow> lstActionOfFlows = flowRunAction.getFlowTemplates().getActionOfFlows();
            for (ActionOfFlow actionOfFlow : lstActionOfFlows) {
                if (!isActionDuplicate(groupActionsClones.get(actionOfFlow.getGroupActionName()), actionOfFlow))
                    groupActionsClones.put(actionOfFlow.getGroupActionName(), actionOfFlow);
            }
            for (NodeRun nodeRun : flowRunAction.getNodeRuns()) {
                nodeRun.getNode().getGroupActions().clear();
                for (String groupName : groupActionsClones.keySet()) {
                    boolean isDeclare = false;
                    List<ActionOfFlow> actionOfFlows = groupActionsClones.get(groupName);
                    List<Long> actionOfFlowIds = new ArrayList<Long>();

                    for (ActionOfFlow actionOfFlow2 : actionOfFlows) {
                        actionOfFlowIds.add(actionOfFlow2.getStepNum());
                    }

                    for (NodeRunGroupActionId nodeRunGroupAction : nodeRunGroupActions) {
                        if (nodeRunGroupAction.getNodeId().equals(nodeRun.getId().getNodeId()))
                            if (actionOfFlowIds.contains(nodeRunGroupAction.getStepNum())) {
                                isDeclare = true;
                            }
                    }
                    List<ActionOfFlow> _actionOfFlows = new LinkedList<>();
                    for (ActionOfFlow actionOfFlow : actionOfFlows) {
                        for (ActionDetail actionDetail : actionOfFlow.getAction().getActionDetails()) {
                            if (actionDetail.getNodeType().equals(nodeRun.getNode().getNodeType()) &&
                                    actionDetail.getVendor().equals(nodeRun.getNode().getVendor()) &&
                                    actionDetail.getVersion().equals(nodeRun.getNode().getVersion())) {
                                _actionOfFlows.add(actionOfFlow);
                            }
                        }
                    }
                    nodeRun.getNode().getGroupActions().add(new GroupAction(groupName, _actionOfFlows, isDeclare));
                }
                mapGroupActionClone.put(nodeRun.getNode(), nodeRun.getNode().getGroupActions());
            }

            if (!sureMopContainCmdBlacklist) {
                if (checkCloneCmdBlacklist(nodeRunClones, mapGroupActionClone, paramValueClones)) {
                    messageBlacklist = String.format(MessageUtil.getResourceBundleMessage("view.dialog.header.clone.flowrunaction.containCmdBlacklist.confirm"),blackListCommand);
                    RequestContext.getCurrentInstance().execute("PF('comfirmCloneMopCmdBlacklist').show()");
                    return;
                }
            }
            //20181119_tudn_end them danh sach lenh blacklist
            //20181119_tudn_start them danh sach lenh blacklist
            if (sureMopContainCmdBlacklist) {
                sureMopContainCmdBlacklist = false;
                flowRunActionClone.setMopType(4L);
            } else {
                flowRunActionClone.setMopType(0L);
            }
            //20181119_tudn_end them danh sach lenh blacklist
            Object[] objs = new FlowRunActionServiceImpl().openTransaction();
            Session session = (Session) objs[0];
            Transaction tx = (Transaction) objs[1];
            new FlowRunActionServiceImpl().saveOrUpdate(flowRunActionClone, session, tx, false);
            Long flowRunIdClone = flowRunActionClone.getFlowRunId();
            for (NodeRun nodeRun : nodeRunClones) {
                nodeRun.getId().setFlowRunId(flowRunIdClone);
            }
            new NodeRunServiceImpl().saveOrUpdate(nodeRunClones, session, tx, false);

            for (NodeRunGroupAction nodeRunGroupAction : nodeRunGroupActionClones) {
                nodeRunGroupAction.getId().setFlowRunId(flowRunIdClone);
            }
            new NodeRunGroupActionServiceImpl().saveOrUpdate(nodeRunGroupActionClones, session, tx, false);

            for (ParamValue paramValue : paramValueClones) {
                paramValue.getNodeRun().getId().setFlowRunId(flowRunIdClone);
                paramValue.setCreateTime(new Date());
                paramValue.setParamValueId(null);
            }

            new ParamValueServiceImpl().saveOrUpdate(paramValueClones, session, tx, true);
            if (session.isOpen()) {
                session.close();
            }
            //20180620_tudn_start ghi log DB
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), GenerateFlowRunController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.CREATE,
                        flowRunActionClone.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            //20180620_tudn_end ghi log DB
            MessageUtil.setInfoMessageFromRes("info.clone.dt.success");
            onStart();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("error.clone.dt.fail");
        }

    }

    public void preEditFlowRunAction(FlowRunAction flowRunAction) {
        try {
			/*20190408_chuongtq start check param when create MOP*/
            setAddNew(false);
            /*20190408_chuongtq end check param when create MOP*/
            caches = new HashMap<>();

            if (nodeSeachs == null)
                nodeSeachs = new ArrayList<Node>();
            else
                nodeSeachs.clear();
            if (tmpNodes == null)
                tmpNodes = new ArrayList<Node>();
            else
                tmpNodes.clear();
            if (preNodes == null)
                preNodes = new ArrayList<Node>();
            else
                preNodes.clear();
            try {
                flowRunAction = flowRunActionService.findById(flowRunAction.getFlowRunId());
            } catch (SysException | AppException e) {
                logger.error(e.getMessage(), e);
            }
            this.flowRunAction = flowRunAction;
            loadCR();
            crs.add(new SelectItem(flowRunAction.getCrNumber(), flowRunAction.getCrNumber()));
            if (flowRunAction.getFlowTemplates().getStatus() == 9)
                selectedFlowTemplates = flowRunAction.getFlowTemplates();
            else
                selectedFlowTemplates = null;
            ResourceBundle bundle = ResourceBundle.getBundle("cas");
            if (bundle.getString("service").contains("10.61.127.190")) {
                selectedFlowTemplates = flowRunAction.getFlowTemplates();
            }

            nodes.clear();
            mapParamValue.clear();
            groupActions.clear();
            onChangeFlowTemplates();
//			if(selectedFlowTemplates!=null)
//				loadGroupAction();
            Map<String, Object> filters = new HashMap<>();
            filters.put("id.flowRunId", flowRunAction.getFlowRunId() + "");

            logger.info("flowRunAction.getFlowRunId(): " + flowRunAction.getFlowRunId());
//			List<NodeRunGroupAction> nodeRunGroupActions = new NodeRunGroupActionServiceImpl().findList("from NodeRunGroupAction where id.flowRunId =?", -1, -1, flowRunAction.getFlowRunId());
            List<NodeRunGroupActionId> nodeRunGroupActions = new NodeRunGroupActionServiceImpl().findNodeGroup(flowRunAction.getFlowRunId());
//						List<NodeRunGroupAction> nodeRunGroupActions = new NodeRunGroupActionServiceImpl().findList(filters, new HashMap<>());
            //			List<NodeRunGroupAction> nodeRunGroupActions = new NodeRunGroupActionServiceImpl().findList(filters);

            Set<Long> nodeIds = new HashSet<>();
            for (NodeRun nodeRun : flowRunAction.getNodeRuns()) {
                nodeIds.add(nodeRun.getId().getNodeId());
            }
            List<ParamValue> allParamValues = new ParamValueServiceImpl().findNodeGroup(flowRunAction.getFlowRunId(), new ArrayList<>(nodeIds));

            Multimap<Long, ParamValue> paramValueMultimap = HashMultimap.create();
            for (ParamValue paramValue : allParamValues) {
                paramValueMultimap.put(paramValue.getNodeId(), paramValue);
            }

            for (NodeRun nodeRun : flowRunAction.getNodeRuns()) {
                nodes.add(nodeRun.getNode());
                nodeRun.getNode().getGroupActions().clear();
                for (String groupName : groupActions.keySet()) {
                    boolean isDeclare = false;
                    List<ActionOfFlow> actionOfFlows = groupActions.get(groupName);
                    List<Long> actionOfFlowIds = new ArrayList<Long>();

                    for (ActionOfFlow actionOfFlow2 : actionOfFlows) {
                        actionOfFlowIds.add(actionOfFlow2.getStepNum());
                    }

                    for (NodeRunGroupActionId nodeRunGroupAction : nodeRunGroupActions) {
                        if (nodeRunGroupAction.getNodeId().equals(nodeRun.getId().getNodeId()))
                            if (actionOfFlowIds.contains(nodeRunGroupAction.getStepNum())) {
                                isDeclare = true;
                            }
                    }
                    List<ActionOfFlow> _actionOfFlows = new LinkedList<>();
                    for (ActionOfFlow actionOfFlow : actionOfFlows) {
                        for (ActionDetail actionDetail : actionOfFlow.getAction().getActionDetails()) {
                            if (actionDetail.getNodeType().equals(nodeRun.getNode().getNodeType()) &&
                                    actionDetail.getVendor().equals(nodeRun.getNode().getVendor()) &&
                                    actionDetail.getVersion().equals(nodeRun.getNode().getVersion())) {
                                _actionOfFlows.add(actionOfFlow);
                            }
                        }
                    }
                    // lay ra thong tin account tac dong cho tung dau viec
                    NodeAccount accNode = null;
                    try {
                        accNode = getNodeAccGroup(nodeRun.getNode(), groupActions.get(groupName).get(0));
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                    if (accNode != null) {
                        logger.info(">>>>>>>>>>>>>>>>> accNode: " + accNode.getUsername() + " ======== " + groupName);
                        nodeRun.getNode().getGroupActions().add(new GroupAction(groupName, isDeclare, _actionOfFlows, accNode));
                    } else {
                        logger.info(">>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<< " + groupName);
                        nodeRun.getNode().getGroupActions().add(new GroupAction(groupName, _actionOfFlows, isDeclare));
                    }
                }
                mapGroupAction.put(nodeRun.getNode(), nodeRun.getNode().getGroupActions());
//				if(nodes.size()>0)
//					selectedNode = nodes.get(0);

//			for (String string : nodeRun.getNode().getMapGroupActionDeclare().keySet()) {
//				nodeRun.getNode().getMapGroupActionDeclare().put(string, false);
//			}
                logger.info("START GET PARAM INPUT");
                getParamInputs(nodeRun.getNode());
//			for (NodeRunGroupAction nodeRunGroupAction : nodeRun.getNodeRunGroupActions()) {
//				nodeRun.getNode().getMapGroupActionDeclare().put(nodeRunGroupAction.getActionOfFlow().getGroupActionName(), true);
//			}
                List<ParamValue> paramValues = mapParamValue.get(nodeRun.getNode());
                logger.info("START GET ParamValue");

//				List<ParamValue> paramValue2s = new ParamValueServiceImpl().findList("from ParamValue where nodeRun.id.flowRunId =? and nodeRun.id.nodeId =?", -1, -1, flowRunAction.getFlowRunId(),nodeRun.getId().getNodeId());
                Collection<ParamValue> paramValue2s = paramValueMultimap.get(nodeRun.getId().getNodeId());

                for (ParamValue paramValue : paramValues) {
                    for (ParamValue paramValue2 : paramValue2s) {
                        if (paramValue.getParamInput().getParamInputId().equals(paramValue2.getParamInputId())) {
                            paramValue.setParamValueId(paramValue2.getParamValueId());
                            paramValue.setParamValue(paramValue2.getParamValue());
                        }
                    }
                }
                //List<ParamValue> paramValues = flowRunAction.getParamValues();
                //mapParamValue.put(nodeRun.getNode(), paramValues);
                //mapParamValue.put(nodeRun.getNode(), flowRunAction.getParamValues());
                for (ParamValue paramValue3 : paramValues) {
                    logger.info("start load param");
                    loadParam(paramValue3, paramValues);
                    //onKeyUpValueParam(paramValue3, paramValues);
                }
//				createActionWithMultiParam(paramValues);
            }

            if (flowRunAction.getServiceActionId() != null) {
                action = new ActionServiceImpl().findById(flowRunAction.getServiceActionId());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        //onKeyUpValueParam(paramValue, paramValues);
        if (nodes.size() > 0) {
            selectedNode = nodes.get(0);
            List<ParamValue> paramValues = mapParamValue.get(nodes.get(0));
            if (paramValues != null) {
                createActionWithMultiParam(paramValues);
            }
        }

        logger.info(">>>>>>>>>>>> FINISH PREPARE VIEW MOP");
    }

    public void preEditFlowRunActionBk(FlowRunAction flowRunAction) {
        try {
            if (nodeSeachs == null)
                nodeSeachs = new ArrayList<Node>();
            else
                nodeSeachs.clear();
            if (tmpNodes == null)
                tmpNodes = new ArrayList<Node>();
            else
                tmpNodes.clear();
            if (preNodes == null)
                preNodes = new ArrayList<Node>();
            else
                preNodes.clear();
            try {
                flowRunAction = flowRunActionService.findById(flowRunAction.getFlowRunId());
            } catch (SysException | AppException e) {
                logger.error(e.getMessage(), e);
            }
            this.flowRunAction = flowRunAction;
            loadCR();
            crs.add(new SelectItem(flowRunAction.getCrNumber(), flowRunAction.getCrNumber()));
            if (flowRunAction.getFlowTemplates().getStatus() == 9)
                selectedFlowTemplates = flowRunAction.getFlowTemplates();
            else
                selectedFlowTemplates = null;
            onChangeFlowTemplates();
            nodes.clear();
            mapParamValue.clear();
            groupActions.clear();
            if (selectedFlowTemplates != null)
                loadGroupAction();

            for (NodeRun nodeRun : flowRunAction.getNodeRuns()) {
                nodes.add(nodeRun.getNode());
                nodeRun.getNode().getGroupActions().clear();
                for (String groupName : groupActions.keySet()) {
                    boolean isDeclare = false;
                    List<ActionOfFlow> actionOfFlows = groupActions.get(groupName);
                    for (NodeRunGroupAction nodeRunGroupAction : nodeRun.getNodeRunGroupActions()) {
                        if (actionOfFlows.contains(nodeRunGroupAction.getActionOfFlow())) {
                            isDeclare = true;
                        }
                    }
                    List<ActionOfFlow> _actionOfFlows = new LinkedList<>();
                    for (ActionOfFlow actionOfFlow : actionOfFlows) {
                        for (ActionDetail actionDetail : actionOfFlow.getAction().getActionDetails()) {
                            if (actionDetail.getNodeType().equals(nodeRun.getNode().getNodeType()) &&
                                    actionDetail.getVendor().equals(nodeRun.getNode().getVendor()) &&
                                    actionDetail.getVersion().equals(nodeRun.getNode().getVersion())) {
                                _actionOfFlows.add(actionOfFlow);
                            }
                        }
                    }

                    // lay ra thong tin account tac dong cho tung dau viec
                    NodeAccount accNode = null;
                    try {
                        accNode = getNodeAccGroup(nodeRun.getNode(), groupActions.get(groupName).get(0));
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    if (accNode != null) {
                        logger.info(">>>>>>>>>>>>>>>>> accNode: " + accNode.getUsername() + " ======== " + groupName);
                        nodeRun.getNode().getGroupActions().add(new GroupAction(groupName, isDeclare, _actionOfFlows, accNode));
                    } else {
                        logger.info(">>>>>>>>>>>>>>>>><<<<<<<<<<<<<<<< " + groupName);
                        nodeRun.getNode().getGroupActions().add(new GroupAction(groupName, _actionOfFlows, isDeclare));
                    }
                }
                mapGroupAction.put(nodeRun.getNode(), nodeRun.getNode().getGroupActions());
                if (nodes.size() > 0)
                    selectedNode = nodes.get(0);

//			for (String string : nodeRun.getNode().getMapGroupActionDeclare().keySet()) {
//				nodeRun.getNode().getMapGroupActionDeclare().put(string, false);
//			}
                getParamInputs(nodeRun.getNode());
//			for (NodeRunGroupAction nodeRunGroupAction : nodeRun.getNodeRunGroupActions()) {
//				nodeRun.getNode().getMapGroupActionDeclare().put(nodeRunGroupAction.getActionOfFlow().getGroupActionName(), true);
//			}
                for (ParamValue paramValue : mapParamValue.get(nodeRun.getNode())) {
                    for (ParamValue paramValue2 : nodeRun.getParamValues()) {
                        if (paramValue.getParamInput().equals(paramValue2.getParamInput())) {
                            paramValue.setParamValueId(paramValue2.getParamValueId());
                            paramValue.setParamValue(paramValue2.getParamValue());
                        }
                    }
                }
                //List<ParamValue> paramValues = flowRunAction.getParamValues();
                //mapParamValue.put(nodeRun.getNode(), paramValues);
                //mapParamValue.put(nodeRun.getNode(), flowRunAction.getParamValues());
                for (ParamValue paramValue3 : mapParamValue.get(nodeRun.getNode())) {
                    onKeyUpValueParam(paramValue3, mapParamValue.get(nodeRun.getNode()));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        //onKeyUpValueParam(paramValue, paramValues);

    }

    public void loadGroupAction() {
        List<ActionOfFlow> actionOfFlows = selectedFlowTemplates.getActionOfFlows();

        for (ActionOfFlow actionOfFlow : actionOfFlows) {
            if (!isActionDuplicate(groupActions.get(actionOfFlow.getGroupActionName()), actionOfFlow))
                groupActions.put(actionOfFlow.getGroupActionName(), actionOfFlow);
        }
    }

    Set<VendorNodeType> vendorNodeTypes = new HashSet<VendorNodeType>();

    public void onChangeFlowTemplates() {
        flowRunAction.setFlowTemplates(selectedFlowTemplates);
        groupActions = LinkedListMultimap.create();

        if (selectedFlowTemplates != null) {
            loadGroupAction();
        }
        nodeType4Searchs.clear();
        vendor4Searchs.clear();
        version4Searchs.clear();

        vendorNodeTypes.clear();

        if (selectedFlowTemplates != null) {
            for (ActionOfFlow actionOfFlow : selectedFlowTemplates.getActionOfFlows()) {
                for (ActionDetail actionDetail : actionOfFlow.getAction().getActionDetails()) {
                    vendorNodeTypes.add(new VendorNodeType(actionDetail.getVendor().getVendorId(),
                            actionDetail.getNodeType().getTypeId(), actionDetail.getVersion().getVersionId()));
                    if (!version4Searchs.contains(actionDetail.getVersion()))
                        version4Searchs.add(actionDetail.getVersion());
                    if (!vendor4Searchs.contains(actionDetail.getVendor()))
                        vendor4Searchs.add(actionDetail.getVendor());
                    if (!nodeType4Searchs.contains(actionDetail.getNodeType()))
                        nodeType4Searchs.add(actionDetail.getNodeType());
					/*
					for (ActionCommand actionCommand : actionDetail.getActionCommands()) {
						CommandDetail commandDetail = actionCommand.getCommandDetail();
						vendorNodeTypes.add(new VendorNodeType(commandDetail.getVendor().getVendorId(), commandDetail.getNodeType().getTypeId()));
					}*/
                }
            }
        }
        if (nodes == null)
            nodes = new ArrayList<Node>();
        nodes.clear();


        initLazySearchNode();
    }


    // thenv_20180615_countryCode_start
	/*@SuppressWarnings({"unchecked", "rawtypes"})
	private void initLazySearchNode() {
		((LazyDataModelSearchNode)lazyNode).setVendorNodeTypes(vendorNodeTypes);
		changeTypeNode();

	}*/
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void initLazySearchNode() {
        ((LazyDataModelSearchNode) lazyNode).setVendorNodeTypes(vendorNodeTypes);
        changeCountry();
        changeTypeNode();

    }

    public void changeCountry() {
        if (flowRunAction.getCountryCode() != null) {
            Map<String, Object> filters = new HashMap<>();
            filters.put("countryCode.countryCode-" + NodeServiceImpl.EXAC, flowRunAction.getCountryCode().getCountryCode());
            //20180831_tudn_start cap nhat trang thai
            filters.put("active", 1L);
            //20180831_tudn_end cap nhat trang thai

            lazyNode = new LazyDataModelSearchNode<>(new NodeServiceImpl(), filters);
            ((LazyDataModelSearchNode) lazyNode).setVendorNodeTypes(vendorNodeTypes);
        } else {
            //20180831_tudn_start cap nhat trang thai
            Map<String, Object> filters = new HashMap<>();
            filters.put("active", 1L);
//			lazyNode = new LazyDataModelSearchNode<>(new NodeServiceImpl());
            lazyNode = new LazyDataModelSearchNode<>(new NodeServiceImpl(), filters);
            //20180831_tudn_end cap nhat trang thai
            ((LazyDataModelSearchNode) lazyNode).setVendorNodeTypes(vendorNodeTypes);
        }
    }

    // thenv_20180615_countryCode_end
    public void onChangeTypeNode(int type) {
        Date startTime = new Date();
        if (preNodes.size() > 0) {
            preNodes.get(0).setIsLab(type);

            try {
                new NodeServiceImpl().saveOrUpdate(preNodes.get(0));
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), GenerateFlowRunController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.UPDATE,
                            preNodes.get(0).toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
                preNodes.clear();
            } catch (SysException | AppException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    @SuppressWarnings("rawtypes")
    public void changeTypeNode() {
        ((LazyDataModelSearchNode) lazyNode).setSearchNodeLab(searchNodeLab);
    }

    public void preSearch() {
        rerender = false;
        nodeSeachs.clear();
        preNodes.clear();
        tmpNodes.clear();
        if (nodes != null)
            nodeSeachs.addAll(nodes);
        DataTable dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("searchNodeForm:tableSearchNode");
        dataTable.setFirst(0);
        dataTable = (DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("searchNodeForm:tableSelectedNode");
        dataTable.setFirst(0);
    }

    public void addNodeSearch() {
        if (nodeSeachs == null)
            nodeSeachs = new ArrayList<Node>();
        if (preNodes != null)
            for (Node node : preNodes) {
                if (!nodeSeachs.contains(node))
                    nodeSeachs.add(node);
            }

    }

    public void removeNodeSearch() {
        if (nodeSeachs == null)
            nodeSeachs = new ArrayList<Node>();
        if (tmpNodes != null) {
            for (Node node : tmpNodes) {
                nodeSeachs.remove(node);
            }
        }
    }

    public void copyNodeSearch() {
        if (nodes == null)
            nodes = new ArrayList<Node>();
        //nodes.clear();
        for (Node node2 : nodeSeachs) {
            if (!nodes.contains(node2))
                nodes.add(node2);
        }
        if (nodeSeachs.size() == 0)
            nodes.clear();
        for (Node _node : nodes) {
            loadGroupAction(_node);
        }
        if (nodes.size() > 0) {
            handleSelectNode(nodes.get(0));
        }
        rerender = true;
    }

    public List<Node> completeNodeRun(String node) {
        return completeNodeRun(node, false, false);
    }

    public List<Node> completeNodeRun(String node, boolean isExac, boolean isOverided) {
        OrderBy orderBy = new OrderBy();
        orderBy.add(Order.asc("nodeCode"));
        ConditionQuery query = new ConditionQuery();
        List<Criterion> predicates = new ArrayList<Criterion>();
        if (node != null && !node.isEmpty()) {
            if (isExac)
                predicates.add(Restrictions.eq("nodeCode", node));
            else
                predicates.add(Restrictions.ilike("nodeCode", node, MatchMode.ANYWHERE));
        }
        query.add(Restrictions.or(predicates.toArray(new Criterion[predicates.size()])));
        List<Long> _nodes = new ArrayList<Long>();
        if (!isOverided && nodes != null) {
            for (Node node1 : nodes)
                _nodes.add(node1.getNodeId());
            if (!_nodes.isEmpty())
                query.add(Restrictions.not(Restrictions.in("nodeId", _nodes)));
        }
        if (vendorNodeTypes.size() > 0) {
            List<Criterion> predicate2s = new ArrayList<Criterion>();
            for (VendorNodeType vendorNodeType : vendorNodeTypes) {
                predicate2s.add(Restrictions.and(Restrictions.eq("vendor.vendorId", vendorNodeType.getVendorId()),
                        Restrictions.eq("nodeType.typeId", vendorNodeType.getNodeTypeId()),
                        //huynx6 edited Oct 10, 2016
                        Restrictions.eq("version.versionId", vendorNodeType.getVersionId())
                ));
            }
            query.add(Restrictions.or(predicate2s.toArray(new Criterion[predicate2s.size()])));
        } else {
            return new ArrayList<Node>();
        }

        // thenv_20180618_countryCode_start
        String coutryCode = flowRunAction.getCountryCode() == null ? AamConstants.VNM : flowRunAction.getCountryCode().getCountryCode();
        query.add(Restrictions.and(Restrictions.eq("countryCode.countryCode", coutryCode)));
        // thenv_20180618_countryCode_end

        //20180831_tudn_start cap nhat trang thai
        query.add(Restrictions.and(Restrictions.eq("active", 1L)));
        //20180831_tudn_end cap nhat trang thai

        List<Node> findList = nodeService.findList(query, orderBy, 1, 20);
        return findList;

    }

    private boolean isActionDuplicate(List<ActionOfFlow> actionOfFlows, ActionOfFlow actionOfFlow) {
        if (actionOfFlows == null || actionOfFlows.size() == 0)
            return false;
        for (ActionOfFlow _actionOfFlow : actionOfFlows) {
            if (_actionOfFlow.getAction().equals(actionOfFlow.getAction())
                    && _actionOfFlow.getStepNumberLabel().equals(actionOfFlow.getStepNumberLabel()))
                return true;
        }
        return false;
    }

	/*
	 * hanhnv68 add 2016_30_11
	 */

    public void saveData() {
        try {
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            String templateId = params.get("templateId");
            logger.info("<><><><><> nodeId: " + templateId);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public List<NodeAccount> nodeAccountCompleteMethod(Node node, String userName) {
        if (userName != null) {
            try {
                // get server node
                Map<String, Object> filtersNode = new HashMap<>();
                //20180821_tudn_start fix bug phai tim chinh xac khong se sai
                filtersNode.put("nodeId", node.getNodeId());
                filtersNode.put("active", Constant.status.active);
//				filtersNode.put("nodeIp", node.getNodeIp());
                //20180821_tudn_end fix bug phai tim chinh xac khong se sai
                filtersNode.put("vendor.vendorId", Config.APP_TYPE.SERVER.value);
                Node server = null;
                try {
                    server = new NodeServiceImpl().findList(filtersNode).get(0);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                List<NodeAccount> lstNodeAcc;
                logger.info(">>>>>>> node add account: " + node.getNodeIp());
                Map<String, Object> filters = new HashMap<>();
                filters.put("serverId", node.getServerId());
                //20180831_tudn_start cap nhat trang thai
                filters.put("active", 1L);
                //20180831_tudn_end cap nhat trang thai
                lstNodeAcc = new NodeAccountServiceImpl().findListExac(filters, null);
                if (lstNodeAcc == null) {
                    lstNodeAcc = new ArrayList<>();
                }
                for (int i = 0; i < lstNodeAcc.size(); i++) {
                    lstNodeAcc.get(i).setUsernameType("database");
                }

                if (server != null) {
                    filters.put("serverId", server.getServerId());
                    List<NodeAccount> lstAccServer = new NodeAccountServiceImpl().findListExac(filters, null);
                    if (lstAccServer != null && !lstAccServer.isEmpty()) {
                        for (int i = 0; i < lstAccServer.size(); i++) {
                            lstAccServer.get(i).setUsernameType("server");
                        }
                        lstNodeAcc.addAll(lstAccServer);
                    }
                }


                return lstNodeAcc;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return new ArrayList<>();
    }

    public List<String> findNodeCodes(Long flowRunId) {
        try {
            if (flowRunId == null)
                return new ArrayList<>();
            List<String> nodeCodes = nodeService.findNodeCodeByFlow(flowRunId);
            if (nodeCodes.size() > 10) {
                List<String> subList = nodeCodes.subList(0, 9);
                subList.add("..................................");
                return subList;
            } else {
                return nodeCodes;
            }
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
        }

        return new ArrayList<>();
    }

    public void onChangeNodeAccount(Node node, int index, String groupActionName) {
        try {
            logger.info(">>>>>>>>>>> vao onchange: " + index + "___" + groupActionName);
            if (node != null) {
                NodeAccount nodeAcc = mapGroupAction.get(node).get(index).getNodeAccount();
                logger.info("get account nodeAcc: " + nodeAcc.getUsername());
                if (mapAccGroupAction.get(node) == null) {
                    mapAccGroupAction.put(node, new HashMap<String, NodeAccount>());
                }
                mapAccGroupAction.get(node).put(groupActionName + "==" + index, nodeAcc);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public List<NodeAccount> getLstAccountNode(Node node) {

        try {
            logger.info(">>>>>>> node add account: " + node.getNodeIp());
            Map<String, Object> filters = new HashMap<>();
            filters.put("accountType", node.getVendor().getVendorId());
            filters.put("serverId", node.getServerId());

            Map<String, String> orders = new HashMap<>();
            orders.put("username", "ASC");
            return new NodeAccountServiceImpl().findListExac(filters, null);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return new ArrayList<>();
    }
	/*
	 * end hanhnv68 add 2016_30_11
	 */

    //20180831_tudn_start cap nhat trang thai
    public String getJsonNodes(List<Node> lsNode) {
        if (lsNode == null || lsNode.size() == 0) return "";
        String json = "[";
        for (Node n : lsNode) {
            json += "{\"nodeCode\":\"" + n.getNodeCode() + "\",\"active\":\"" + n.getActive() + "\"},";
        }
        json = json.substring(0, json.length() - 1) + "]";
        return json;
    }
    //20180831_tudn_end cap nhat trang thai

    public void loadParamInputs() {

    }

    public String onFlowProcess(FlowEvent event) {

        return null;
    }

    public List<SelectItem> getCrs() {
        return crs;
    }

    public void setCrs(List<SelectItem> crs) {
        this.crs = crs;
    }

    public List<FlowTemplates> getFlowTemplates() {
        return flowTemplates;
    }

    public void setFlowTemplates(List<FlowTemplates> flowTemplates) {
        this.flowTemplates = flowTemplates;
    }

    public FlowTemplates getSelectedFlowTemplates() {
        return selectedFlowTemplates;
    }

    public void setSelectedFlowTemplates(FlowTemplates selectedFlowTemplates) {
        this.selectedFlowTemplates = selectedFlowTemplates;
    }

    public FlowTemplatesServiceImpl getFlowTemplatesService() {
        return flowTemplatesService;
    }

    public void setFlowTemplatesService(FlowTemplatesServiceImpl flowTemplatesService) {
        this.flowTemplatesService = flowTemplatesService;
    }

    public List<Node> getNodes() {
        return nodes;
    }

    public void setNodes(List<Node> nodes) {
        this.nodes = nodes;
    }

    public NodeServiceImpl getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeServiceImpl nodeService) {
        this.nodeService = nodeService;
    }

    public LinkedListMultimap<String, ActionOfFlow> getGroupActions() {
        return groupActions;
    }

    public void setGroupActions(LinkedListMultimap<String, ActionOfFlow> groupActions) {
        this.groupActions = groupActions;
    }

    public GenericDaoServiceNewV2<FlowRunAction, Long> getFlowRunActionService() {
        return flowRunActionService;
    }

    public void setFlowRunActionService(GenericDaoServiceNewV2<FlowRunAction, Long> flowRunActionService) {
        this.flowRunActionService = flowRunActionService;
    }

    public LazyDataModel<FlowRunAction> getLazyFlowRunAction() {
        return lazyFlowRunAction;
    }

    public void setLazyFlowRunAction(LazyDataModel<FlowRunAction> lazyFlowRunAction) {
        this.lazyFlowRunAction = lazyFlowRunAction;
    }

    public FlowRunAction getFlowRunAction() {
        return flowRunAction;
    }

    public void setFlowRunAction(FlowRunAction flowRunAction) {
        this.flowRunAction = flowRunAction;
    }

    public Node getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(Node selectedNode) {
        this.selectedNode = selectedNode;
    }

    public Map<Node, List<GroupAction>> getMapGroupAction() {
        return mapGroupAction;
    }

    public void setMapGroupAction(Map<Node, List<GroupAction>> mapGroupAction) {
        this.mapGroupAction = mapGroupAction;
    }

    public LazyDataModel<Node> getLazyNode() {
        return lazyNode;
    }

    public void setLazyNode(LazyDataModel<Node> lazyNode) {
        this.lazyNode = lazyNode;
    }

    public List<Node> getPreNodes() {
        return preNodes;
    }

    public void setPreNodes(List<Node> preNodes) {
        this.preNodes = preNodes;
    }

    public List<Node> getTmpNodes() {
        return tmpNodes;
    }

    public void setTmpNodes(List<Node> tmpNodes) {
        this.tmpNodes = tmpNodes;
    }

    public List<Node> getNodeSeachs() {
        return nodeSeachs;
    }

    public void setNodeSeachs(List<Node> nodeSeachs) {
        this.nodeSeachs = nodeSeachs;
    }

    public boolean isRerender() {
        return rerender;
    }

    public void setRerender(boolean rerender) {
        this.rerender = rerender;
    }

    public List<Vendor> getVendor4Searchs() {
        return vendor4Searchs;
    }

    public void setVendor4Searchs(List<Vendor> vendor4Searchs) {
        this.vendor4Searchs = vendor4Searchs;
    }

    public List<NodeType> getNodeType4Searchs() {
        return nodeType4Searchs;
    }

    public void setNodeType4Searchs(List<NodeType> nodeType4Searchs) {
        this.nodeType4Searchs = nodeType4Searchs;
    }

    public List<Version> getVersion4Searchs() {
        return version4Searchs;
    }

    public void setVersion4Searchs(List<Version> version4Searchs) {
        this.version4Searchs = version4Searchs;
    }

    public boolean isTabExecute() {
        return isTabExecute;
    }

    public void setTabExecute(boolean isTabExecute) {
        this.isTabExecute = isTabExecute;
    }

    public Integer getSearchNodeLab() {
        return searchNodeLab;
    }

    public void setSearchNodeLab(Integer searchNodeLab) {
        this.searchNodeLab = searchNodeLab;
    }

    public List<NodeAccount> getLstNodeAccount() {
        return lstNodeAccount;
    }

    public void setLstNodeAccount(List<NodeAccount> lstNodeAccount) {
        this.lstNodeAccount = lstNodeAccount;
    }

    public Map<Node, Map<String, NodeAccount>> getMapAccGroupAction() {
        return mapAccGroupAction;
    }

    public void setMapAccGroupAction(Map<Node, Map<String, NodeAccount>> mapAccGroupAction) {
        this.mapAccGroupAction = mapAccGroupAction;
    }

    public MapUserCountryServiceImpl getMapUserCountryService() {
        return mapUserCountryService;
    }

    public void setMapUserCountryService(MapUserCountryServiceImpl mapUserCountryService) {
        this.mapUserCountryService = mapUserCountryService;
    }

    public Map<Node, HashMap<String, ParamInput>> getMapParamInOut() {
        return mapParamInOut;
    }

    public void setMapParamInOut(Map<Node, HashMap<String, ParamInput>> mapParamInOut) {
        this.mapParamInOut = mapParamInOut;
    }

    public Map<Node, Multimap<Long, ParamValue>> getMapParamValueGroup() {
        return mapParamValueGroup;
    }

    public void setMapParamValueGroup(Map<Node, Multimap<Long, ParamValue>> mapParamValueGroup) {
        this.mapParamValueGroup = mapParamValueGroup;
    }

    //20181119_tudn_start them danh sach lenh blacklist
    public String getMessageBlacklist() {
        return messageBlacklist;
    }

    public void setMessageBlacklist(String messageBlacklist) {
        this.messageBlacklist = messageBlacklist;
    }
    //20181119_tudn_end them danh sach lenh blacklist
	/*20190408_chuongtq start check param when create MOP*/

    public Map<Node, List<ParamValue>> getMapParamValue() {
        return mapParamValue;
    }

    public void setMapParamValue(Map<Node, List<ParamValue>> mapParamValue) {
        this.mapParamValue = mapParamValue;
    }

    public boolean isBtnSave() {
        return btnSave;
    }

    public void setBtnSave(boolean btnSave) {
        this.btnSave = btnSave;
    }

    public boolean isAddNew() {
        return addNew;
    }

    public void setAddNew(boolean addNew) {
        this.addNew = addNew;
    }
	/*20190408_chuongtq end check param when create MOP*/

    public Map<Node, List<ParamInput>> getMapParam() {
        return mapParam;
    }

    public void setMapParam(Map<Node, List<ParamInput>> mapParam) {
        this.mapParam = mapParam;
    }
    public List<UploadedFile> getLstUploadedFile() {
        return lstUploadedFile;
    }

    public void setLstUploadedFile(List<UploadedFile> lstUploadedFile) {
        this.lstUploadedFile = lstUploadedFile;
    }

    //20190524_tudn_start tac dong toan trinh SR GNOC
    public List<?> getObjectImports() {
        return objectImports;
    }

    public void setObjectImports(List<?> objectImports) {
        this.objectImports = objectImports;
    }
    //20190524_tudn_end tac dong toan trinh SR GNOC

    public NodeAccountServiceImpl getNodeAccountService() {
        return nodeAccountService;
    }

    public void setNodeAccountService(NodeAccountServiceImpl nodeAccountService) {
        this.nodeAccountService = nodeAccountService;
    }
}
