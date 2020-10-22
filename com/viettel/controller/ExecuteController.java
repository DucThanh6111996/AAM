package com.viettel.controller;

import com.captcha.botdetect.web.jsf.JsfCaptcha;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.LinkedListMultimap;
import com.google.common.collect.Multimap;
import com.rits.cloning.Cloner;
import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.gnoc.cr.service.CrOutputForQLTNDTO;
import com.viettel.it.model.CatConfig;
import com.viettel.it.persistence.CatConfigServiceImpl;
import com.viettel.it.util.GNOCService;
import com.viettel.it.util.MessageUtil;
import com.viettel.lazy.LazyExcecuteInfo;
import com.viettel.model.*;
import com.viettel.persistence.*;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.LockMode;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.html.HtmlOutputText;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.rpc.ServiceException;
import java.io.*;
import java.text.SimpleDateFormat;
import java.util.*;

//import com.mchange.v2.collection.MapEntry;

/**
 * @author quanns2
 */
@ViewScoped
@ManagedBean
public class ExecuteController implements Serializable {
    private static Logger logger = LogManager.getLogger(ExecuteController.class);

    private List<SelectItem> runSteps;
    private RunStep selectedRunStep;

    @ManagedProperty(value = "#{actionService}")
    ActionService actionService;

    public void setActionService(ActionService actionService) {
        this.actionService = actionService;
    }

    @ManagedProperty(value = "#{executeChecklistController}")
    ExecuteChecklistController executeChecklistController;

    public void setExecuteChecklistController(ExecuteChecklistController executeChecklistController) {
        this.executeChecklistController = executeChecklistController;
    }

    @ManagedProperty(value = "#{actionDetailAppService}")
    ActionDetailAppService actionDetailAppService;

    public void setActionDetailAppService(ActionDetailAppService actionDetailAppService) {
        this.actionDetailAppService = actionDetailAppService;
    }

    @ManagedProperty(value = "#{actionDetailDatabaseService}")
    ActionDetailDatabaseService actionDetailDatabaseService;

    public void setActionDetailDatabaseService(ActionDetailDatabaseService actionDetailDatabaseService) {
        this.actionDetailDatabaseService = actionDetailDatabaseService;
    }

    @ManagedProperty(value = "#{actionModuleService}")
    ActionModuleService actionModuleService;

    public void setActionModuleService(ActionModuleService actionModuleService) {
        this.actionModuleService = actionModuleService;
    }

    @ManagedProperty(value = "#{actionHistoryService}")
    ActionHistoryService actionHistoryService;

    public void setActionHistoryService(ActionHistoryService actionHistoryService) {
        this.actionHistoryService = actionHistoryService;
    }


    @ManagedProperty(value = "#{actionCustomActionService}")
    ActionCustomActionService actionCustomActionService;

    public void setActionCustomActionService(ActionCustomActionService actionCustomActionService) {
        this.actionCustomActionService = actionCustomActionService;
    }

    @ManagedProperty(value = "#{actionDtFileService}")
    ActionDtFileService actionDtFileService;

    public void setActionDtFileService(ActionDtFileService actionDtFileService) {
        this.actionDtFileService = actionDtFileService;
    }

    public void setIimService(IimService iimService) {
        this.iimService = iimService;
    }

    @ManagedProperty(value = "#{iimService}")
    IimService iimService;

    public void setImpactProcessService(ImpactProcessService impactProcessService) {
        this.impactProcessService = impactProcessService;
    }

    @ManagedProperty(value = "#{impactProcessService}")
    ImpactProcessService impactProcessService;

    @ManagedProperty(value = "#{kpiServerSettingService}")
    KpiServerSettingService kpiServerSettingService;

    public void setKpiServerSettingService(KpiServerSettingService kpiServerSettingService) {
        this.kpiServerSettingService = kpiServerSettingService;
    }

    public void setAomClientService(AomClientService aomClientService) {
        this.aomClientService = aomClientService;
    }

    @ManagedProperty(value = "#{aomService}")
    AomClientService aomClientService;

    private List<SelectItem> lstActions;
    private Action selectedAction;

    //	private MutableInt selectedStep;
    private MapEntry selectedStep;

    private Multimap<MapEntry, ExeObject> impactObjects;
    private Multimap<MapEntry, ExeObject> rollbackObjects;
    private Multimap<MapEntry, ExeObject> customExeObjectMultimap;
    private Multimap<MapEntry, ExeObject> rollbackCustomExeObjectMultimap;
    private List<ExeObject> waitingActions;

    private List<ExeObject> checkVersionAppObjects;
    private List<SelectItem> statusFitterList = Constant.getStatusList();
    private String username;
    private String logDetail;
    private String scriptDetail;

    private String secureCode;
    private String reasonRollback;

    private List<String> appGroupnames;
    private Map<MapEntry, Integer> stepResult;
    List<Integer> kbGroups = null;

    private ActionHistory history;

    boolean change;
    private String resourceDir;

    // Longl6 add
    private List<ExcecuteInfoObj> infoList = new ArrayList<>();
    private SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    private LazyExcecuteInfo lazyExcecuteInfo = new LazyExcecuteInfo(null);

    private Integer runStatus;
    private boolean dialogOpen;
    private Integer dialogSize;

    private JsfCaptcha captcha;
    private ResourceBundle bundle;
    private DateTimeFormatter dateTimeFormatter;
    private Boolean isTest;
    private Long runId;

    private List<ImpactProcess> impactProcesses;
    private Boolean isRunning = Boolean.FALSE;
    private Boolean isAutoMode = Boolean.FALSE;
    //anhnt2
    private boolean renderWaitTimeShutdown = false;
    private int DEFAULT_OPTION_TIME_OUT = AamConstants.defaultOptionWhenTimeOut;
    //20181126_tudn_start kiem tra module tac dong rollback
    private String messageModule = MessageUtil.getResourceBundleMessage("get.module.count.message");
    private Boolean sufficientImpact = Boolean.TRUE;
    private Boolean sufficientRollback = Boolean.TRUE;
    private Integer executeTypeModule = Constant.EXE_TD; //2:la tac dong, 3:la rollback
    private Boolean repeat = Boolean.FALSE;
    private Map<String, String> mapResultMess;
    private Boolean clickButtonImpact = Boolean.FALSE;
    private Boolean clickButtonRollback = Boolean.FALSE;
    private Boolean isReload = Boolean.TRUE;
    //	private Long onStart = 0L; //0:moi vao tranh, 1:da vao trang
    //20181126_tudn_end kiem tra module tac dong rollback
    /*20181023_hoangnd_continue fail step_start*/
    private List<RunStep> lstImpactSteps = new ArrayList<>();
    private boolean showSelectProcess = false;
    private final String newLine = "<br />";
    private String failStep;
    /*20181023_hoangnd_continue fail step_end*/

    // 03-12-2018 KienPD check server dead start
    private long numberRetry = 0;
    private long timeRetry = 0;
    private boolean checkBuildRb = false;
    // 03-12-2018 KienPD check server dead end

    @PostConstruct
    public void onStart() {
        ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
        HttpServletRequest req = (HttpServletRequest) context.getRequest();

//		String ridStr = req.getParameter("rid");
        String actionIdStr = req.getParameter("action");

//		logger.info(context.getRequestParameterMap().get("action_id"));

        dialogOpen = false;
        dialogSize = 0;
        selectedAction = new Action();

        change = false;
        appGroupnames = new ArrayList<>();

//		selectedStep = new MutableInt(0);
        selectedStep = new MapEntry(0, 0);
        executeChecklistController.setExecuteController(this);
        username = SessionUtil.getCurrentUsername() == null ? "system" : SessionUtil.getCurrentUsername();

        customExeObjectMultimap = HashMultimap.create();
        rollbackCustomExeObjectMultimap = HashMultimap.create();
        impactObjects = HashMultimap.create();
        rollbackObjects = HashMultimap.create();

        checkVersionAppObjects = new ArrayList<>();

        // longlt6 add
        this.infoList = new ArrayList<>();
        this.lazyExcecuteInfo = new LazyExcecuteInfo(this.infoList);
        //

        stepResult = new HashMap<>();
        history = new ActionHistory();
        runSteps = new ArrayList<>();

        lstActions = new ArrayList<>();

        try {
            // 03-12-2018 KienPD check server dead start
            Map<String, Object> fillter = new HashMap<>();
            fillter.put("id.configGroup", Constant.CONFIG_GROUP);
            fillter.put("id.propertyKey-EXAC", Constant.NUMBER_RETRY_SERVER_DEAD);
            try {
                List<CatConfig> lstNumber = new CatConfigServiceImpl().findList(fillter);
                if (lstNumber != null && !lstNumber.isEmpty()) {
                    CatConfig number = lstNumber.get(0);
                    if (number != null && number.getPropertyValue() != null) {
                        numberRetry = Long.valueOf(number.getPropertyValue());
                    }
                }
                fillter.put("id.propertyKey-EXAC", Constant.TIME_CALL_SERVER_DEAD);
                List<CatConfig> lstTime = new CatConfigServiceImpl().findList(fillter);
                if (lstTime != null && !lstTime.isEmpty()) {
                    CatConfig time = lstNumber.get(0);
                    if (time != null && time.getPropertyValue() != null) {
                        timeRetry = Long.valueOf(time.getPropertyValue());
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            // 03-12-2018 KienPD check server dead end
            DateTime currentDate = new DateTime();
            isTest = Arrays.asList("quanns2", "hunghq2", "anttt2", "viethq", "quytv7", "haont27").contains(username);
            List<Action> actions = actionService.findCrToExecute(currentDate, username, isTest);

            dateTimeFormatter = DateTimeFormat.forPattern("dd/MM/yyyy HH:mm:ss");
            bundle = ResourceBundle.getBundle("config");
            for (Action action : actions) {
                action.setTestbedMode(false);
                //20200107 sua chi duoc chay cac mop uctt khi da duoc z78 phe duyet
//                if (action.getCreatedBy().equals(username) && action.getActionType().equals(Constant.ACTION_TYPE_CR_UCTT) && currentDate.isAfter(action.getBeginTime().getTime()) && currentDate.isBefore(action.getEndTime().getTime())) {
                if (action.getExeImpactUctt() != null && action.getStartTimeImpactUctt() != null && action.getEndTimeImpactUctt() != null
                        && action.getExeImpactUctt().equals(username) && currentDate.isAfter(action.getStartTimeImpactUctt().getTime())
                        && currentDate.isBefore(action.getEndTimeImpactUctt().getTime())) {
                    action.setTestbedMode(false);
                    lstActions.add(new SelectItem(action, action.getCrNumber()));
                } else if (action.getExeRollback() != null && action.getStartTimeRollback() != null && action.getEndTimeRollback() != null && action.getExeRollback().equals(username) && currentDate.isAfter(action.getStartTimeRollback().getTime()) && currentDate.isBefore(action.getEndTimeRollback().getTime())) {
                    action.setTestbedMode(false);
                    lstActions.add(new SelectItem(action, action.getCrNumber()));
                } else if (isTest && action.getCrNumber().startsWith("TEST_VAS_")) {
                    action.setTestbedMode(false);
                    lstActions.add(new SelectItem(action, action.getCrNumber()));
                }else if(action.getActionType().equals(Constant.ACTION_TYPE_CR_UCTT)){

                }
                else {
                    try {
                        CrOutputForQLTNDTO qltndto = GNOCService.getCrByCode(action.getCrNumber());

                        if ("OK".equals(qltndto.getResultCode()) && StringUtils.isNotEmpty(qltndto.getCrNumber())) {
                            DateTime startDateTime = DateTime.parse(qltndto.getImpactStartTime(), dateTimeFormatter);
                            DateTime endDateTime = DateTime.parse(qltndto.getImpactEndTime(), dateTimeFormatter);

                            if (qltndto.getUserExecute().equals(username) && currentDate.isAfter(startDateTime.minusHours(2)) && currentDate.isBefore(endDateTime)) {
                                action.setTestbedMode(false);
                                lstActions.add(new SelectItem(action, action.getCrNumber()));
                            }
                        } else {
                            logger.error(action.getCrNumber());
                            if (action.getIncludeTestbed() == 1) {
                                action.setTestbedMode(true);
                                lstActions.add(new SelectItem(action, action.getTdCode() + " - TESTBED"));
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage() + "\t" + action.getCrNumber(), e);
                    }
                }
            }

            Map<String, Object> filters = new HashMap<>();
            filters.put("status", "1");
            impactProcesses = impactProcessService.findList(filters, new HashMap<>());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
        resourceDir = request.getRealPath("resources");

        if (actionIdStr != null) {
            Long actionId = Long.valueOf(actionIdStr);

            try {
                selectedAction = actionService.findById(actionId);
                //tudn_start fix khong xem duoc chi tiet
                if (selectedAction != null) {
                    selectedAction.setTestbedMode(false);
                    subjectSelectionChanged(null);
                }
                //tudn_end fix khong xem duoc chi tiet

            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void init() {
        actionService = new ActionServiceImpl();
        actionDetailAppService = new ActionDetailAppServiceImpl();
        actionDetailDatabaseService = new ActionDetailDatabaseServiceImpl();
        actionModuleService = new ActionModuleServiceImpl();
        actionHistoryService = new ActionHistoryServiceImpl();
        actionCustomActionService = new ActionCustomActionServiceImpl();
        actionDtFileService = new ActionDtFileServiceImpl();
        iimService = new IimServiceImpl();
        impactProcessService = new ImpactProcessServiceImpl();
        kpiServerSettingService = new KpiServerSettingServiceImpl();
        aomClientService = new AomClientServiceImpl();

        executeChecklistController = new ExecuteChecklistController();
        executeChecklistController.setExecuteController(this);
        executeChecklistController.init();

        isAutoMode = Boolean.TRUE;
    }

    public boolean checkExecutable() {
        if (selectedAction == null || selectedAction.getId() == null)
            return false;

        boolean check = false;
        DateTime currentDate = new DateTime();

        try {
            if (selectedAction.getCreatedBy().equals(username) && selectedAction.getActionType().equals(Constant.ACTION_TYPE_CR_UCTT) && currentDate.isAfter(selectedAction.getBeginTime().getTime()) && currentDate.isBefore(selectedAction.getEndTime().getTime())) {
                check = true;
            } else if (selectedAction.getExeRollback() != null && selectedAction.getStartTimeRollback() != null && selectedAction.getEndTimeRollback() != null && selectedAction.getExeRollback().equals(username) && currentDate.isAfter(selectedAction.getStartTimeRollback().getTime()) && currentDate.isBefore(selectedAction.getEndTimeRollback().getTime())) {
            }
            else if (selectedAction.getExeImpactUctt() != null && selectedAction.getStartTimeImpactUctt() != null && selectedAction.getEndTimeImpactUctt() != null && selectedAction.getExeImpactUctt().equals(username) && currentDate.isAfter(selectedAction.getStartTimeImpactUctt().getTime()) && currentDate.isBefore(selectedAction.getEndTimeImpactUctt().getTime())) {
                check = true;
            }
            else if (isTest && selectedAction.getCrNumber().startsWith("TEST_VAS_")) {
                check = true;
            } else {
                try {
                    CrOutputForQLTNDTO qltndto = GNOCService.getCrByCode(selectedAction.getCrNumber());
                    if ("OK".equals(qltndto.getResultCode()) && qltndto.getCrNumber() != null) {
                        DateTime startDateTime = DateTime.parse(qltndto.getImpactStartTime(), dateTimeFormatter);
                        DateTime endDateTime = DateTime.parse(qltndto.getImpactEndTime(), dateTimeFormatter);

                        if (qltndto.getUserExecute().equals(username) && currentDate.isAfter(startDateTime.minusHours(2)) && currentDate.isBefore(endDateTime)) {
                            check = true;
                        }
                    } else {
                        if (selectedAction.getIncludeTestbed() == 1) {
                            selectedAction.setTestbedMode(true);
                            check = true;
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return check;
    }

    public boolean checkRollbackable() {
        if (selectedAction == null || selectedAction.getId() == null)
            return false;

        boolean check = false;
        DateTime currentDate = new DateTime();

        try {
            if (selectedAction.getCreatedBy().equals(username) && selectedAction.getActionType().equals(Constant.ACTION_TYPE_CR_UCTT) && currentDate.isAfter(selectedAction.getBeginTime().getTime()) && currentDate.isBefore(selectedAction.getEndTime().getTime())) {
                check = true;
            } else if (selectedAction.getExeRollback() != null && selectedAction.getStartTimeRollback() != null && selectedAction.getEndTimeRollback() != null && selectedAction.getExeRollback().equals(username) && currentDate.isAfter(selectedAction.getStartTimeRollback().getTime()) && currentDate.isBefore(selectedAction.getEndTimeRollback().getTime())) {
                check = true;
            }else if (selectedAction.getExeImpactUctt() != null && selectedAction.getStartTimeImpactUctt() != null && selectedAction.getEndTimeImpactUctt() != null && selectedAction.getExeImpactUctt().equals(username) && currentDate.isAfter(selectedAction.getStartTimeImpactUctt().getTime()) && currentDate.isBefore(selectedAction.getEndTimeImpactUctt().getTime())) {
                check = true;
            }
            else if (isTest && selectedAction.getCrNumber().startsWith("TEST_VAS_")) {
                check = true;
            } else {
                try {
                    CrOutputForQLTNDTO qltndto = GNOCService.getCrByCode(selectedAction.getCrNumber());

                    if ("OK".equals(qltndto.getResultCode())) {
                        if (qltndto.getImpactStartTime() != null && qltndto.getImpactEndTime() != null) {
                            DateTime startDateTime = DateTime.parse(qltndto.getImpactStartTime(), dateTimeFormatter);
                            DateTime endDateTime = DateTime.parse(qltndto.getImpactEndTime(), dateTimeFormatter);

                            if (qltndto.getUserExecute().equals(username) && currentDate.isAfter(startDateTime.minusHours(2)) && currentDate.isBefore(endDateTime)) {
                                check = true;
                            }
                        }
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return check;
    }

    /*20181009_hoangnd_continue fail step_start*/
    public boolean isErrorAction() {

        boolean flag = false;
        if (history == null || history.getCurrStep() == null || history.getCurrKbGroup() == null) {
            loadHistory();
        }
        if (history != null && history.getCurrStep() != null && history.getCurrKbGroup() != null) {
            flag = true;
        }
        return flag;
    }

    public void loadHistory() {

        if (selectedAction != null && selectedAction.getId() != null) {
            Map<String, Object> filters = new HashMap<>();
            filters.put("action.id", selectedAction.getId());
            List<ActionHistory> histories = null;
            try {
                histories = actionHistoryService.findList(filters, new HashMap<>());
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
            if (CollectionUtils.isNotEmpty(histories))
                history = histories.get(0);
        }
    }

    public void selectProcess() {
        RequestContext reqCtx = RequestContext.getCurrentInstance();
        lstImpactSteps = new ArrayList<>();
        lstImpactSteps.addAll(getLstSteps());
        reqCtx.update("execute:selectProcess");
        reqCtx.execute("PF('confirmProcess').hide()");
        reqCtx.execute("PF('selectProcess').show()");
    }

    public void selectRollback() {
        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.update("execute:selectRollback");
        reqCtx.execute("PF('confirmRollback').hide()");
        reqCtx.execute("PF('selectRollback').show()");
    }

    public String currStepClass(MapEntry mapEntry) {

        if (history != null && mapEntry != null
                && history.getCurrStep() != null && history.getCurrKbGroup() != null
                && mapEntry.getKey() != null && mapEntry.getValue() != null
                && history.getCurrStep().equals(mapEntry.getKey())
                && history.getCurrKbGroup().equals(mapEntry.getValue()))
            return "fa fa-stop-circle Red";
        return "";
    }

    public String currStepStyle(MapEntry mapEntry) {

        if (history != null && mapEntry != null
                && history.getCurrStep() != null && history.getCurrKbGroup() != null
                && mapEntry.getKey() != null && mapEntry.getValue() != null
                && history.getCurrStep().equals(mapEntry.getKey())
                && history.getCurrKbGroup().equals(mapEntry.getValue()))
            return "margin-right: 5px";
        return "";
    }

    public boolean isFailStep(MapEntry mapEntry) {

        if (history != null && mapEntry != null
                && history.getCurrStep() != null && history.getCurrKbGroup() != null
                && mapEntry.getKey() != null && mapEntry.getValue() != null
                && history.getCurrStep().equals(mapEntry.getKey())
                && history.getCurrKbGroup().equals(mapEntry.getValue()))
            return true;
        return false;
    }
    /*20181009_hoangnd_continue fail step_end*/

    /*20181023_hoangnd_approval impact step_start*/
    public boolean isApprovalImpactStep() {

        boolean flag = false;

        if (selectedAction != null
                && StringUtils.isNotBlank(selectedAction.getExeImpactStep())
                && StringUtils.isNotBlank(selectedAction.getReasonImpactStep())
                && StringUtils.isNotBlank(SessionUtil.getCurrentUsername())
                && selectedAction.getExeImpactStep().equals(SessionUtil.getCurrentUsername())) {
            flag = true;
        }

        return flag;
    }
    /*20181023_hoangnd_approval impact step_end*/

    public void prepareExecute(MapEntry mapEntry, Boolean check) {
        /*20181009_hoangnd_continue fail step_start*/
        RequestContext reqCtx = RequestContext.getCurrentInstance();
        if(check){
            if (selectedAction.getRunAuto() != null && selectedAction.getRunAuto().equals(1)) {
                reqCtx.execute("PF('dlgRunAutoConfirm').show()");
            }else{
                if (mapEntry == null) {
                    if (isErrorAction() || isApprovalImpactStep()) {
                        showSelectProcess = true;
                        selectProcess();
                    } else {
                        reqCtx.execute("PF('confirmProcess').show()");
                    }
                } else if (mapEntry.getKey() != null && mapEntry.getValue() != null) {
                    history.setCurrStep(mapEntry.getKey());
                    history.setCurrKbGroup(mapEntry.getValue());
                    reqCtx.execute("PF('selectProcess').hide()");
                    reqCtx.execute("PF('confirmProcess').show()");
                }
            }
        }else {
            if (mapEntry == null) {
                if (isErrorAction() || isApprovalImpactStep()) {
                    showSelectProcess = true;
                    selectProcess();
                } else {
                    reqCtx.execute("PF('confirmProcess').show()");
                }
            } else if (mapEntry.getKey() != null && mapEntry.getValue() != null) {
                history.setCurrStep(mapEntry.getKey());
                history.setCurrKbGroup(mapEntry.getValue());
                reqCtx.execute("PF('selectProcess').hide()");
                reqCtx.execute("PF('confirmProcess').show()");
            }
        }
        /*20181009_hoangnd_continue fail step_end*/
    }

    public void prepareRollback(MapEntry mapEntry) {
        //30-11-2018 KienPD check disabled button check list before and action start
        try {
            if (selectedAction != null && selectedAction.getRunningStatus() == null) {
                selectedAction = actionService.findById(selectedAction.getId());
                if (selectedAction != null) {
                    selectedAction.setTestbedMode(false);
                    subjectSelectionChanged(null);
                }
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        //30-11-2018 KienPD check disabled button check list before and action end
        /*20181009_hoangnd_continue fail step_start*/
        RequestContext reqCtx = RequestContext.getCurrentInstance();
        if (mapEntry == null) {
            /* kienpd_20180911_start */
            if ((isErrorAction() && history.getCurrStep().intValue() < 320 && history.getCurrStep().intValue() > 300) || isApprovalImpactStep()) {
                /* kienpd_20180911_end */
                showSelectProcess = true;
                selectRollback();
            } else {
                //05-11-2018 KienPD add failed step to rollback list start
                confirm = "";
                reasonRollback = "";
                lstStep = new ArrayList<>();
                runStepsOld = new ArrayList<>(runSteps);
                if (!stepFail.isEmpty() && history != null && history.getCurrStep() != null && history.getCurrKbGroup() != null) {
                    labelStep = Constant.getRunStep(new MapEntry(history.getCurrStep(), history.getCurrKbGroup())).getLabel();
                } else if (!stepImpactFail.isEmpty()) {
                    for (MapEntry map : stepImpactFail.keySet()) {
                        labelStep = Constant.getRunStep(new MapEntry(map.getKey(), map.getValue())).getLabel();
                    }
                }
                showSelectProcess = false;
                reqCtx.update("execute:confirmRollback");
                //05-11-2018 KienPD add failed step to rollback list end
                reqCtx.execute("PF('confirmRollback').show()");
            }
        } else if (mapEntry.getKey() != null && mapEntry.getValue() != null) {
            reasonRollback = "";
            history.setCurrStep(mapEntry.getKey());
            history.setCurrKbGroup(mapEntry.getValue());
            /*20181217_hoangnd_bo save curr_step khi chon_start*/
            /*try {
                new ActionHistoryServiceImpl().saveOrUpdate(history);
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }*/
            /*20181217_hoangnd_bo save curr_step khi chon_end*/
            reqCtx.execute("PF('selectRollback').hide()");
            reqCtx.execute("PF('confirmRollback').show()");
        }
        /*20181009_hoangnd_continue fail step_end*/
    }

    public void prepareChecklist() {
        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.execute("PF('confirmChecklist').show()");
    }

    public void prepareChecklistLast() {
        RequestContext reqCtx = RequestContext.getCurrentInstance();
        reqCtx.execute("PF('confirmChecklistLast').show()");
    }

    public boolean renderCr() {
        /*if (ThreadExeManager.getInstance().getExeCommandThread(username, selectedAction.getId()) == null)
            return true;
		else
			return false;*/
//		return checkProcessRunning() == null;
        return !isRunning;
    }

    public boolean renderChecklist() {
        if (selectedAction == null)
            return false;

        /*20181217_hoangnd_disable btn checklist sau khi da chay action_start*/
        if (selectedAction.getRunningStatus() != null
                && (selectedAction.getRunningStatus().equals(Constant.RunningStatus.SUCCESS) || selectedAction.getRunningStatus().equals(Constant.RunningStatus.FAIL)))
            return false;

        if(history != null && history.getStatus() != null
                && (history.getStatus().equals(Constant.FINISH_FAIL_STATUS) || history.getStatus().equals(Constant.FINISH_SUCCESS_STATUS)))
            return false;
        /*20181217_hoangnd_disable btn checklist sau khi da chay action_end*/

        /*20190128_hoangnd_fix bug checklist after_start*/
        boolean validChecklistApp = true;
        boolean validChecklistDb = true;

        if (executeChecklistController.getCklAppAfter() != null && CollectionUtils.isNotEmpty(executeChecklistController.getCklAppAfter().values())) {
            for (Checklist checklist : executeChecklistController.getCklAppAfter().values()) {
                //1-12-2018 KienPD check null start
                if ((checklist.getResult() != null && checklist.getResult().getStatus() != null)
                        || checklist.getStatusAfter() != null) {
                    validChecklistApp = false;
                    break;
                } else {
                    continue;
                }
                //1-12-2018 KienPD check null end
            }
        }

        if (executeChecklistController.getCklDbAfter() != null && CollectionUtils.isNotEmpty(executeChecklistController.getCklDbAfter().values())) {
            for (QueueChecklist checklist : executeChecklistController.getCklDbAfter().values()) {
                //1-12-2018 KienPD check null start
                if ((checklist.getResult() != null && checklist.getResult().getStatus() != null)
                        || checklist.getStatusAfter() != null) {
                    validChecklistDb = false;
                    break;
                } else {
                    continue;
                }
                //1-12-2018 KienPD check null end
            }
        }

        if (!validChecklistApp || !validChecklistDb) {
            return false;
        }
        /*20190128_hoangnd_fix bug checklist after_end*/

        if (isRunning)
            return Boolean.FALSE;

        if (!executeChecklistController.isSufficientChecklist()) {
            return false;
        }

        return true;
    }

    public boolean renderChecklistAfter() {
        if (selectedAction == null)
            return false;

        if (selectedAction.getRunStatus() == null)
            return false;

		/*if (checkProcessRunning() != null)
			return false;*/
        if (isRunning)
            return Boolean.FALSE;

        return true;
    }

    public boolean renderExecute() {
        if (selectedAction == null || selectedAction.getTdCode() == null)
            return false;

        /*20181217_hoangnd_disable btn checklist va action_start*/
        if (selectedAction.getRunningStatus() != null
                && (selectedAction.getRunningStatus().equals(Constant.RunningStatus.SUCCESS) || selectedAction.getRunningStatus().equals(Constant.RunningStatus.FAIL)))
            return false;

        if(history != null && history.getStatus() != null
                && (history.getStatus().equals(Constant.FINISH_FAIL_STATUS) || history.getStatus().equals(Constant.FINISH_SUCCESS_STATUS)))
            return false;
        /*20181217_hoangnd_disable btn checklist va action_end*/

        if (isRunning)
            return Boolean.FALSE;

        /*20181120_hoangnd_save all step_start*/
        boolean validChecklistApp = true;
        boolean validChecklistDb = true;

        if (executeChecklistController.getCklAppBefore() != null && CollectionUtils.isNotEmpty(executeChecklistController.getCklAppBefore().values())) {
            for (Checklist checklist : executeChecklistController.getCklAppBefore().values()) {
                //1-12-2018 KienPD check null start
                if ((checklist.getResult() != null && checklist.getResult().getStatus() != null && checklist.getResult().getStatus().equals(1))
                        || (checklist.getStatusBefore() != null && checklist.getStatusBefore().equals("1"))) {
                    continue;
                } else {
                    validChecklistApp = false;
                    break;
                }
                //1-12-2018 KienPD check null end
            }
        }

        if (executeChecklistController.getCklDbBefore() != null && CollectionUtils.isNotEmpty(executeChecklistController.getCklDbBefore().values())) {
            for (QueueChecklist checklist : executeChecklistController.getCklDbBefore().values()) {
                //1-12-2018 KienPD check null start
                if ((checklist.getResult() != null && checklist.getResult().getStatus() != null && checklist.getResult().getStatus().equals(1))
                        || (checklist.getStatusBefore() != null && checklist.getStatusBefore().equals("1"))) {
                    continue;
                } else {
                    validChecklistDb = false;
                    break;
                }
                //1-12-2018 KienPD check null end
            }
        }

        /*20190128_hoangnd_fix bug checklist after_start*/
        if (executeChecklistController.getCklAppAfter() != null && CollectionUtils.isNotEmpty(executeChecklistController.getCklAppAfter().values())) {
            for (Checklist checklist : executeChecklistController.getCklAppAfter().values()) {
                //1-12-2018 KienPD check null start
                if ((checklist.getResult() != null && checklist.getResult().getStatus() != null)
                        || checklist.getStatusAfter() != null) {
                    validChecklistApp = false;
                    break;
                } else {
                    continue;
                }
                //1-12-2018 KienPD check null end
            }
        }

        if (executeChecklistController.getCklDbAfter() != null && CollectionUtils.isNotEmpty(executeChecklistController.getCklDbAfter().values())) {
            for (QueueChecklist checklist : executeChecklistController.getCklDbAfter().values()) {
                //1-12-2018 KienPD check null start
                if ((checklist.getResult() != null && checklist.getResult().getStatus() != null)
                        || checklist.getStatusAfter() != null) {
                    validChecklistDb = false;
                    break;
                } else {
                    continue;
                }
                //1-12-2018 KienPD check null end
            }
        }
        /*20190128_hoangnd_fix bug checklist after_end*/

        if (!validChecklistApp || !validChecklistDb) {
            return false;
        }

        if (!executeChecklistController.isSufficientChecklist()) {
            return false;
        }

        return true;
        /*20181120_hoangnd_save all step_end*/
    }

    private ChecklistInfo checkProcessRunning() {
        logger.info("Bat dau check ProcessRuning");
        if (selectedAction == null || selectedAction.getId() == null)
            return null;
        List<ChecklistInfo> processRunnings = new ArrayList<>();

        Map<String, Object> filters = new HashMap<>();
        filters.put("status", "1");
        try {
//			for (ImpactProcess impactProcess : impactProcesses) {
            ImpactProcess impactProcess = selectedAction.getImpactProcess();
            try {
                Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

                WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("ls");
                Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);

                Response response = builder.get();
                if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                    List<ChecklistInfo> checklistInfos = response.readEntity(new GenericType<List<ChecklistInfo>>() {
                    });

                    for (ChecklistInfo checklistInfo : checklistInfos) {
                        if ((checklistInfo.getExeType().equals(Constant.EXE_CHECKLIST)
                                || checklistInfo.getExeType().equals(Constant.EXE_TD)
                                || checklistInfo.getExeType().equals(Constant.EXE_ROLLBACK)
                                || checklistInfo.getExeType().equals(Constant.EXE_CHECKLIST_LAST))
                                && checklistInfo.getEndTime() == null) {
                            processRunnings.add(checklistInfo);
                        }
                    }
                } else {
                    logger.error(response.getStatus());
                }
            } catch (Exception e) {
                logger.debug(e.getMessage() + "\t" + impactProcess.getLink(), e);
                logger.error(e.getMessage() + "\t" + impactProcess.getName() + "\t" + impactProcess.getLink());
            } finally {
            }
//			}
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        for (ChecklistInfo processRunning : processRunnings) {
            //20190508_quytv7 sua them chi lay nhung process khong phai la verify(Khong nam trong luong chay)
            if (processRunning.getCheckDbs() == null && processRunning.getAction().getTdCode() != null && processRunning.getAction().getTdCode().equals(selectedAction.getTdCode()) && processRunning.getExeType() != null && processRunning.getExeType() > 0 && processRunning.getExeType() < 5) {
                logger.info("Ket thuc check ProcessRuning " + processRunning != null ? "null" : processRunning.getAction().getTdCode());
                return processRunning;
            }

        }
        logger.info("Ket thuc check ProcessRuning null roi");
        return null;
    }

    public boolean renderRollback() {
        if (selectedAction == null)
            return false;

        if (history != null && history.getRollbackStatus() != null)
            return false;

		/*if (checkProcessRunning() != null)
			return false;*/
        if (isRunning)
            return Boolean.FALSE;

        if (selectedAction.getRunStatus() != null)
            return true;

        return false;
    }

    public void execute(Integer exeType) {
        Date startTime = new Date();

        if (runSteps == null || runSteps.isEmpty()) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, MessageUtil.getResourceBundleMessage("cr.do.not.select"), "");
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            return;
        }

        if (exeType.equals(Constant.EXE_ROLLBACK)) {
            if (StringUtils.isEmpty(reasonRollback)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, MessageUtil.getResourceBundleMessage("reason.rollback.do.not.enter"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                return;
            } else if (!checkRollbackable()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, MessageUtil.getResourceBundleMessage("not.in.time.for.rollback"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                return;
            } else {
                RequestContext reqCtx = RequestContext.getCurrentInstance();
                reqCtx.execute("PF('confirmRollback').hide()");
            }
            //20181126_tudn_start kiem tra module tac dong rollback
            executeTypeModule = Constant.EXE_ROLLBACK;
            if (!sufficientRollback) {
                clickButtonRollback = Boolean.TRUE;
                repeat = Boolean.TRUE;
                if (!sufficientRollback) {
                    messageModule = mapResultMess.get("sufficientRollback");
                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    return;
                }
            }
            ;
            //20181126_tudn_end kiem tra module tac dong rollback
        }
        if (exeType.equals(Constant.EXE_TD)) {
            if (!isAutoMode && !isReload) {
                boolean isHuman = secureCode != null && captcha.validate(secureCode);
                secureCode = "";
                if (!isHuman) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, MessageUtil.getResourceBundleMessage("capcha.code.do.not.exactly"), "");
                    FacesContext.getCurrentInstance().addMessage("designGrowl", msg);

                    return;
                } else if (!checkExecutable()) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, MessageUtil.getResourceBundleMessage("not.in.time.for.impact"), "");
                    FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                    return;
                }
            }
            //20181205_tudn_start kiem tra module tac dong impact
            executeTypeModule = Constant.EXE_TD;
            if (!sufficientImpact) {
                clickButtonImpact = Boolean.TRUE;
                repeat = Boolean.TRUE;
                if (!sufficientImpact) {
                    messageModule = mapResultMess.get("sufficientImpact");
                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    return;
                }
            }
            executeTypeModule = Constant.EXE_ROLLBACK;
            //20181205_tudn_end kiem tra module tac dong impact
        }
        secureCode = "";

        if (exeType.equals(Constant.EXE_CHECKLIST)) {
            //20181205_tudn_start kiem tra module tac dong impact
            executeTypeModule = Constant.EXE_CHECKLIST;
            //20181205_tudn_end kiem tra module tac dong impact
            if (!executeChecklistController.getCklAppBefore().isEmpty()) {
                stepResult.put(new MapEntry(1, 1), Constant.STAND_BY_STATUS);

                for (Checklist checklist : executeChecklistController.getCklAppBefore().values()) {
                    checklist.setResult(new ChecklistResult());
                }
            }
            if (!executeChecklistController.getCklDbBefore().isEmpty()) {
                stepResult.put(new MapEntry(2, 1), Constant.STAND_BY_STATUS);

                for (QueueChecklist dbResult : executeChecklistController.getCklDbBefore().values()) {

                    dbResult.setResult(new ChecklistResult());
                }
            }
        } else if (exeType.equals(Constant.EXE_CHECKLIST_LAST)) {
            //20181205_tudn_start kiem tra module tac dong impact
            executeTypeModule = Constant.EXE_CHECKLIST_LAST;
            //20181205_tudn_end kiem tra module tac dong impact
            if (!executeChecklistController.getCklAppAfter().isEmpty()) {
                stepResult.put(new MapEntry(22, 1), Constant.STAND_BY_STATUS);

                for (Checklist appResult : executeChecklistController.getCklAppAfter().values()) {
                    appResult.setResult(new ChecklistResult());
                }
            }
            if (!executeChecklistController.getCklDbAfter().isEmpty()) {
                stepResult.put(new MapEntry(23, 1), Constant.STAND_BY_STATUS);

                for (QueueChecklist dbResult : executeChecklistController.getCklDbAfter().values()) {
                    dbResult.setResult(new ChecklistResult());
                }
            }

            if (!checkVersionAppObjects.isEmpty()) {
                stepResult.put(new MapEntry(25, 1), Constant.STAND_BY_STATUS);

                for (ExeObject exeObject : checkVersionAppObjects) {
                    exeObject.setRunStt(Constant.STAND_BY_STATUS);
                    exeObject.setLog(new StringBuilder());
                    exeObject.setBeginDate(null);
                    exeObject.setEndDate(null);
                }
            }
        }

        ChecklistInfo checklistInfo = new ChecklistInfo();
        checklistInfo.setExeType(exeType);
        checklistInfo.setUsername(username);

        checklistInfo.setCklAppBefore(executeChecklistController.getCklAppBefore());
        checklistInfo.setNewCklAppMain(executeChecklistController.getNewCklAppMain());
        checklistInfo.setCklAppAfter(executeChecklistController.getCklAppAfter());
//			checklistInfo.setCklAppRollback(executeChecklistController.getCklAppRollback());
        checklistInfo.setNewCklAppRollback(executeChecklistController.getNewCklAppRollback());

        checklistInfo.setCklDbBefore(executeChecklistController.getCklDbBefore());
        checklistInfo.setCklDbMain(executeChecklistController.getCklDbMain());
        checklistInfo.setCklDbAfter(executeChecklistController.getCklDbAfter());
        checklistInfo.setCklDbRollback(executeChecklistController.getCklDbRollback());

        //20181023_tudn_start load pass security
        checklistInfo.setUserTD(username);
        selectedAction.setUserExecute(username);
        //20181023_tudn_end load pass security
        checklistInfo.setAction(selectedAction);
        checklistInfo.setResourceDir(resourceDir);
        waitingActions = new ArrayList<>();
        checklistInfo.setWaitingActions(waitingActions);


        Map<MapEntry, KpiServerSetting> kpiServerSettingMap = new HashMap<>();
        try {
            List<Long> moduleIds = actionModuleService.findListModuleId(selectedAction.getId(), null, true);
//			List<KpiServerSetting> kpiServerSettings = kpiServerSettingService.findSettingForModules(new ArrayList<>(moduleIds));
            if (moduleIds.size() > 0) {
                List<Module> modules = iimService.findModulesByIds(selectedAction.getImpactProcess().getNationCode(), moduleIds);
                List<KpiServerSetting> kpiServerSettings = kpiServerSettingService.findAomSettingForModules(new ArrayList<>(modules));
                if (kpiServerSettings != null) {
                    for (KpiServerSetting kpiServerSetting : kpiServerSettings) {
                        kpiServerSettingMap.put(new MapEntry(kpiServerSetting.getAppId().intValue(), kpiServerSetting.getKpiId().intValue()), kpiServerSetting);
                    }
                }
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        checklistInfo.setKpiServerSettingMap(kpiServerSettingMap);

        checklistInfo.setCustomExeObjectMultimap(customExeObjectMultimap);
        checklistInfo.setImpactObjects(impactObjects);
        checklistInfo.setRollbackObjects(rollbackObjects);


        checklistInfo.setReasonRollback(reasonRollback);
        checklistInfo.setRollbackCustomExeObjectMultimap(rollbackCustomExeObjectMultimap);

        checklistInfo.setUsername(username);
        checklistInfo.setCurrentStep(selectedStep);

        checklistInfo.setKbGroups(kbGroups);
        checklistInfo.setStepResult(stepResult);
//			checklistInfo.setRunSteps(runSteps);
        checklistInfo.setHistory(history);


        ImpactProcess impactProcess = selectedAction.getImpactProcess();
        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("add").queryParam("runType", exeType);
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);

        Response response = builder.post(Entity.json(checklistInfo));
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            runId = response.readEntity(Long.class);

            if (!isAutoMode) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("execute.impact.cr") + " " + selectedAction.getCrNumber(), "");
                FacesContext.getCurrentInstance().addMessage("verifyGrowl", msg);
                RequestContext reqCtx = RequestContext.getCurrentInstance();
                reqCtx.execute("PF('pollListener').start()");
                checkBuildRb = true;
            }

            isRunning = Boolean.TRUE;
        } else {
            logger.error(response.getStatus() + "\t" + impactProcess.getLink());
        }

        try {
            if (!isAutoMode)
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), this.getClass().getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.IMPACT,
                        selectedAction.actionLog(), LogUtils.getRequestSessionId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String statusIcon(MapEntry step) {
        String fa = "";
        if (stepResult == null || stepResult.get(step) == null) {
//            logger.info("stepResult null: " + step.getKey());
            return "";
        }
        Integer status = stepResult.get(step);

        if (status == null)
            return "";

        switch (status) {
            case Constant.STAND_BY_STATUS:
                break;
            case Constant.RUNNING_STATUS:
                fa = " fa-forward Yellow";
                break;
            case Constant.FINISH_SUCCESS_STATUS:
                fa = " fa-check Green";
                break;
            case Constant.FINISH_FAIL_STATUS:
                fa = " fa-close Red";
                break;
            case Constant.FINISH_SUCCESS_WITH_WARNING:
                fa = " fa-warning Yellow";
                break;
            //05-12-2018 KienPD add icon FAIL_BUT_SKIPED_BY_USER start
            case AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER:
                fa = " fa-warning Yellow";
                break;
            //05-12-2018 KienPD add icon FAIL_BUT_SKIPED_BY_USER end
            default:
                break;

        }

        return fa;
    }

    private void addCustomStep(Integer customStep, Integer kbGroup) {
        MapEntry entryKey = new MapEntry(customStep, kbGroup);
        if (!customExeObjectMultimap.get(entryKey).isEmpty()) {
            RunStep step = new RunStep();
            ActionCustomGroup customGroup = customExeObjectMultimap.get(entryKey).iterator().next().getCustomAction().getActionCustomGroup();
            step.setValue(entryKey);
            step.setLabel(customGroup.getName());
            step.setDescription(customGroup.getName());

            runSteps.add(new SelectItem(step, step.getLabel()));
            stepResult.put(entryKey, Constant.STAND_BY_STATUS);
        }
    }

    private void addRollbackCustomStep(Integer customStep, Integer kbGroup) {
        MapEntry entryKey = new MapEntry(customStep, kbGroup);
        if (!rollbackCustomExeObjectMultimap.get(entryKey).isEmpty()) {
            RunStep step = new RunStep();
            ActionCustomGroup customGroup = findRollback(customStep, kbGroup);

            step.setValue(entryKey);
            step.setLabel(customGroup.getName());
            step.setDescription(customGroup.getName());

            runSteps.add(new SelectItem(step, step.getLabel()));
            stepResult.put(entryKey, Constant.STAND_BY_STATUS);
        }
    }

    public ActionCustomGroup findRollback(Integer rollbackStep, Integer kbGroup) {
        MapEntry entryKey = new MapEntry(rollbackStep, kbGroup);

        Collection<ExeObject> exeObjects = rollbackCustomExeObjectMultimap.asMap().get(entryKey);

        if (exeObjects.isEmpty())
            return null;
        else
            return exeObjects.iterator().next().getCustomAction().getActionCustomGroup();
    }

    public void subjectSelectionChanged(final AjaxBehaviorEvent event) {
//		ExecuteCommandThread executeCommandThread = ThreadExeManager.getInstance().getExeCommandThread(username, selectedAction.getId());
/*		if (checkProcessRunning() != null) {
			onloadCr();
		} else {*/
        //04-12-2018 KienPD add step fail to rollback list start
        labelStep = "";
        lstStep = new ArrayList<>();
        reasonRollback = "";
        //04-12-2018 KienPD add step fail to rollback list end
        try {
            logger.info("CR tac dong: " + selectedAction.getCrNumber());
            logger.info("User tac dong: " + SessionWrapper.getCurrentUsername());
        } catch (Exception ex) {
            logger.error(ex.getMessage());
        }
        runSteps = new ArrayList<>();
        stepResult = new HashMap<>();
        logger.info("Bat dau reload:" + new Date());
        //20181205_tudn_start kiem tra module tac dong impact
        repeat = Boolean.FALSE;
        clickButtonImpact = Boolean.FALSE;
        isReload = Boolean.FALSE;
        //20181205_tudn_end kiem tra module tac dong impact
        reload(event == null);
        logger.info("Ket thuc reload:" + new Date());

        logger.info("Bat dau reload chekclist:" + new Date());
        executeChecklistController.reload();
        logger.info("Ket thuc reload chekclist:" + new Date());

        MapEntry entryKey;
        SelectItem item;
        if (selectedAction != null) {
            entryKey = new MapEntry(-5, 1);
            item = new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel());
            item.setDisabled(true);
            runSteps.add(item);

            entryKey = new MapEntry(30, 1);
            item = new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel());
            runSteps.add(item);
            stepResult.put(entryKey, Constant.STAND_BY_STATUS);
        }

        entryKey = new MapEntry(-1, 1);
        item = new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel());
        item.setDisabled(true);
        runSteps.add(item);
        // stepResult.put(1, Constant.STAND_BY_STATUS);

        if (!executeChecklistController.getCklAppBefore().isEmpty()) {
            entryKey = new MapEntry(1, 1);
            item = new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel());
            runSteps.add(item);
            stepResult.put(entryKey, Constant.STAND_BY_STATUS);
        }
        if (!executeChecklistController.getCklDbBefore().isEmpty()) {
            entryKey = new MapEntry(2, 1);
            runSteps.add(new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel()));
            stepResult.put(entryKey, Constant.STAND_BY_STATUS);
        }

        entryKey = new MapEntry(-2, 1);
        item = new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel());
        item.setDisabled(true);
        runSteps.add(item);
        // stepResult.put(1, Constant.STAND_BY_STATUS);

        /*20181030_hoangnd_save all step_start*/
		/*entryKey = new MapEntry(Constant.SUB_STEP_CHECK_STATUS, 1);
//		if (!checkStatusObjects.isEmpty()) {
		if (!impactObjects.get(entryKey).isEmpty()) {
			runSteps.add(new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel()));
			stepResult.put(entryKey, Constant.STAND_BY_STATUS);
		}*/
        /*20181030_hoangnd_save all step_end*/


        try {
            kbGroups = actionService.findKbGroups(selectedAction.getId());
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        if (kbGroups == null)
            kbGroups = new ArrayList<>();

        if (!kbGroups.contains(1)) {
            kbGroups.add(1);
        }

        if (kbGroups.contains(null))
            kbGroups.remove(null);

        Collections.sort(kbGroups);
//		kbGroups = new ArrayList<>();

        for (Integer kbGroup : kbGroups) {
            /*20181030_hoangnd_save all step_start*/
            entryKey = new MapEntry(Constant.SUB_STEP_CHECK_STATUS, kbGroup);
            if (!impactObjects.get(entryKey).isEmpty()) {
                runSteps.add(new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel()));
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }
            /*20181030_hoangnd_save all step_end*/
            addCustomStep(Constant.CUSTOM_STEP_CHECK_STATUS, kbGroup);

            entryKey = new MapEntry(Constant.SUB_STEP_STOP_APP, kbGroup);
//		if (!stopObjects.isEmpty()) {
            if (!impactObjects.get(entryKey).isEmpty()) {
                runSteps.add(new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel()));
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addCustomStep(Constant.CUSTOM_STEP_STOP_APP, kbGroup);

            entryKey = new MapEntry(Constant.SUB_STEP_BACKUP_APP, kbGroup);
//		if (!backupObjects.isEmpty()) {
            if (!impactObjects.get(entryKey).isEmpty()) {
                runSteps.add(new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel()));
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addCustomStep(Constant.CUSTOM_STEP_BACKUP_APP, kbGroup);

            entryKey = new MapEntry(Constant.SUB_STEP_BACKUP_DB, kbGroup);
//		if (!backupDbObjects.isEmpty()) {
            if (!impactObjects.get(entryKey).isEmpty()) {
                runSteps.add(new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel()));
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addCustomStep(Constant.CUSTOM_STEP_BACKUP_DB, kbGroup);

            entryKey = new MapEntry(Constant.SUB_STEP_UPCODE, kbGroup);
//		if (!upcodeObjects.isEmpty()) {
            if (!impactObjects.get(entryKey).isEmpty()) {
                runSteps.add(new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel()));
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addCustomStep(Constant.CUSTOM_STEP_UPCODE, kbGroup);

            entryKey = new MapEntry(Constant.SUB_STEP_TD_DB, kbGroup);
//		if (!executeDbObjects.isEmpty()) {
            if (!impactObjects.get(entryKey).isEmpty()) {
                runSteps.add(new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel()));
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addCustomStep(Constant.CUSTOM_STEP_TD_DB, kbGroup);

            entryKey = new MapEntry(Constant.SUB_STEP_CLEARCACHE, kbGroup);
//		if (!clearCacheObjects.isEmpty()) {
            if (!impactObjects.get(entryKey).isEmpty()) {
                runSteps.add(new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel()));
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addCustomStep(Constant.CUSTOM_STEP_CLEARCACHE, kbGroup);

            entryKey = new MapEntry(Constant.SUB_STEP_RESTART_APP, kbGroup);
//		if (!restartObjects.isEmpty()) {
            if (!impactObjects.get(entryKey).isEmpty()) {
                runSteps.add(new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel()));
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addCustomStep(Constant.CUSTOM_STEP_RESTART_APP, kbGroup);

            entryKey = new MapEntry(Constant.SUB_STEP_START_APP, kbGroup);
//		if (!startObjects.isEmpty()) {
            if (!impactObjects.get(entryKey).isEmpty()) {
                runSteps.add(new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel()));
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            entryKey = new MapEntry(Constant.SUB_STEP_UPCODE_START_APP, kbGroup);
//		if (!upcodeStartObjects.isEmpty()) {
            if (!impactObjects.get(entryKey).isEmpty()) {
                runSteps.add(new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel()));
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addCustomStep(Constant.CUSTOM_STEP_START_APP, kbGroup);

            /*20181031_hoangnd_save all step_start*/
            entryKey = new MapEntry(Constant.SUB_STEP_CHECKLIST_APP, kbGroup);
			/*Multimap<Module, Checklist> cklModules = executeChecklistController.getNewCklAppMain().get(entryKey);
			if (cklModules != null && !cklModules.isEmpty()) {
//				entryKey = new MapEntry(Constant.SUB_STEP_CHECKLIST_APP, 1);
				RunStep runStep = Constant.getRunStep(entryKey);
				runSteps.add(new SelectItem(runStep, runStep.getLabel()));
				stepResult.put(entryKey, Constant.STAND_BY_STATUS);
			}*/
            if (!impactObjects.get(entryKey).isEmpty()) {
                runSteps.add(new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel()));
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addCustomStep(Constant.CUSTOM_STEP_CHECKLIST_APP, kbGroup);

			/*entryKey = new MapEntry(Constant.SUB_STEP_CHECKLIST_DB, kbGroup);
			if (!impactObjects.get(entryKey).isEmpty()) {
				runSteps.add(new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel()));
				stepResult.put(entryKey, Constant.STAND_BY_STATUS);
			}

			addCustomStep(Constant.SUB_STEP_CHECKLIST_DB, kbGroup);*/

            entryKey = new MapEntry(Constant.SUB_STEP_CHECKLIST_DB, kbGroup);
            if (!impactObjects.get(entryKey).isEmpty()) {
                runSteps.add(new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel()));
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addCustomStep(Constant.SUB_STEP_CHECKLIST_DB, kbGroup);
            /*20181031_hoangnd_save all step_end*/
        }
			/*if (!executeChecklistController.getCklAppAfter().isEmpty()) {
				entryKey = new MapEntry(Constant.SUB_STEP_CHECKLIST_APP, 1);
				runSteps.add(new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel()));
				stepResult.put(entryKey, Constant.STAND_BY_STATUS);
			}*/

        /*20181031_hoangnd_save all step_start*/
		/*if (!executeChecklistController.getCklDbMain().isEmpty()) {
			entryKey = new MapEntry(Constant.SUB_STEP_CHECKLIST_DB, 1);
			runSteps.add(new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel()));
			stepResult.put(entryKey, Constant.STAND_BY_STATUS);
		}*/
        /*20181031_hoangnd_save all step_end*/

        // longlt6 add
/*			item = new SelectItem(Constant.getSteps().get(-6), Constant.getSteps().get(-6).getLabel());
			item.setDisabled(true);
			runSteps.add(item);

			item = new SelectItem(Constant.getSteps().get(26), Constant.getSteps().get(26).getLabel());
			runSteps.add(item);
			stepResult.put(26, Constant.STAND_BY_STATUS);*/

        // end longlt6 add
        /*20181009_hoangnd_continue fail step_start*/
		/*Map<String, Object> filters = new HashMap<>();
		filters.put("action.id", selectedAction.getId() + "");
		List<ActionHistory> histories = null;
		try {
			histories = actionHistoryService.findList(filters, new HashMap<>());
		} catch (AppException e) {
			logger.error(e.getMessage(), e);
		}
		logger.info("Bat dau load rollback:" + new Date());
		if (histories != null && !histories.isEmpty())
			history = histories.get(0);*/
        loadHistory();
        if (history == null) {
            history = new ActionHistory();
            history.setAction(selectedAction);
            history.setRunUser(username);
        }
		/*20181009_hoangnd_continue fail step_end*/
        //20181205_tudn_start kiem tra module tac dong impact
        if (history != null && history.getStatus() != null) {
            executeTypeModule = Constant.EXE_ROLLBACK;
        } else {
            executeTypeModule = Constant.EXE_TD;
        }
        //20181205_tudn_end kiem tra module tac dong impact

        if (history != null) {
            //20181126_tudn_start kiem tra module tac dong rollback
            clickButtonRollback = Boolean.FALSE;
            //20181126_tudn_end kiem tra module tac dong rollback
            loadRollback(event == null); //true la webservice goi, false la webservice khong goi
        }

//		//20181205_tudn_start kiem tra module tac dong impact
//		if (history != null && history.getStatus()!=null) {
//			if (!sufficientRollback) {
//				if(repeat) {
//					messageModule = mapResultMess.get("sufficientRollback");
//				}else{
//					messageModule = MessageUtil.getResourceBundleMessage("get.module.count.message");
//				}
//				RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
//				RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
//				return;
//			}
//		}else{
//			if (!sufficientImpact) {
//				if(repeat) {
//					messageModule = mapResultMess.get("sufficientImpact");
//				}else{
//					messageModule = MessageUtil.getResourceBundleMessage("get.module.count.message");
//				}
//				RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
//				RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
//				return;
//			}
//		}
//		//20181205_tudn_end kiem tra module tac dong impact
//		selectedStep.setValue(30);
        selectedStep = new MapEntry(30, 1);
        logger.info("Ket thuc load rollback:" + new Date());
//		this.createInformation();
        // End longlt6 add
        logger.info("Bat dau onloadCr:" + new Date());
        if (checkProcessRunning() != null) {
            logger.info("checkProcessRunning khac null load lai cr");
            onloadCr();
        } else {
        }
        logger.info("Ket thuc onloadCr:" + new Date());

        /*20190118_hoangnd_set fail step name_start*/
        if(history != null && history.getCurrStep() != null && history.getCurrKbGroup() != null) {
            MapEntry mapEntry = new MapEntry(history.getCurrStep(), history.getCurrKbGroup());
            RunStep failStep = Constant.getRunStep(mapEntry);
            this.failStep = String.format(MessageUtil.getResourceBundleMessage("confirm.select.step"), failStep.getLabel());
        } else {
            this.failStep = MessageUtil.getResourceBundleMessage("confirm.select.step1");
        }
        /*20190118_hoangnd_set fail step name_end*/

        //20181205_tudn_start kiem tra module tac dong impact
        if (event != null) {
            if (!isRunning && (selectedAction.getRunningStatus() == null || selectedAction.getRunningStatus() != 2) && history.getRollbackStatus() == null) {
                if (history != null && history.getStatus() != null) {
                    if (!sufficientRollback) {
                        if (repeat) {
                            messageModule = mapResultMess.get("sufficientRollback");
                        } else {
                            messageModule = MessageUtil.getResourceBundleMessage("get.module.count.message");
                        }
                        RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        return;
                    }
                } else {
                    if (!sufficientImpact) {
                        if (repeat) {
                            messageModule = mapResultMess.get("sufficientImpact");
                        } else {
                            messageModule = MessageUtil.getResourceBundleMessage("get.module.count.message");
                        }
                        RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        return;
                    }
                }
            }
        }
        //20181205_tudn_end kiem tra module tac dong impact
        logger.info("Ket thuc onloadCr:" + new Date());
//		}
        //anhnt2
        loadRenderWaitTimeShutdown();
		/*20181015_hoangnd_add status icon_start*/
        //05-12-2018 KienPD add step rollback check status start
        Multimap<MapEntry, Integer> mapRunStep = HashMultimap.create();
        //05-12-2018 KienPD add step rollback check status end
        try {
            List<ActionDetailApp> appList = actionDetailAppService.findListDetailApp(this.selectedAction.getId());
            List<ActionDetailDatabase> dbList = actionDetailDatabaseService.findListDetailDb(this.selectedAction.getId(), null, false, true);
            for (ActionDetailApp detailApp : appList) {
                String groupAction = detailApp.getGroupAction();
                /*20181112_hoangnd_save all step_start*/
                ActionModule actionModule = new ActionModule();
                if (detailApp.getModuleId() != null && !detailApp.getModuleId().equals(0L)) {
                    Module app = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), detailApp.getModuleId());
                    if (app == null)
                        continue;
                    actionModule = this.actionModuleService.findModule(this.selectedAction.getId(), app.getModuleId());
                    if (actionModule == null) {
                        continue;
                    }
                }
                /*20181112_hoangnd_save all step_end*/
                if (groupAction.equals(Constant.STEP_RESTART) || groupAction.equals(Constant.STEP_RESTART_CMD)) {
                    mapRunStep.put(new MapEntry(AamConstants.ACTION.SUB_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRunStatus());
                    // 15-12-2018 KienPD start
                    if (detailApp.getRunStatus() != null || (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().intValue() == 1)) {
                        // 15-12-2018 KienPD end
                        mapRunStep.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRollbackStatus());
                    }
                } else if (groupAction.equals(Constant.STEP_UPCODE)) {
                    mapRunStep.put(new MapEntry(AamConstants.ACTION.SUB_STEP_BACKUP_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getBackupStatus());
                    mapRunStep.put(new MapEntry(AamConstants.ACTION.SUB_STEP_UPCODE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRunStatus());
                    // 15-12-2018 KienPD start
                    if (detailApp.getRunStatus() != null || (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().intValue() == 1)) {
                        // 15-12-2018 KienPD end
                        mapRunStep.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_SOURCE_CODE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRollbackStatus());
                    }
                } else {
                    if (groupAction.equals(Constant.STEP_CHECK_STATUS)) {
                        mapRunStep.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CHECK_STATUS, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRunStatus());
                        //5-12-2018 KienPD add step check status start
                        if (detailApp.getRunStatus() != null || (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().intValue() == 1)) {
                            mapRunStep.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECK_STATUS, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRollbackStatus());
                        }
                        //5-12-2018 KienPD add step check status end
                    } else if (groupAction.equals(Constant.STEP_STOP)) {
                        mapRunStep.put(new MapEntry(AamConstants.ACTION.SUB_STEP_STOP_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRunStatus());
                        // 15-12-2018 KienPD start
                        if (detailApp.getRunStatus() != null || (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().intValue() == 1)) {
                            // 15-12-2018 KienPD end
                            mapRunStep.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_START_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRollbackStatus());
                        }
                    } else if (groupAction.equals(Constant.STEP_CLEARCACHE)) {
                        mapRunStep.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CLEARCACHE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRunStatus());
                        // 15-12-2018 KienPD start
                        if (detailApp.getRunStatus() != null || (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().intValue() == 1)) {
                            // 15-12-2018 KienPD end
                            mapRunStep.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CLEARCACHE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRollbackStatus());
                        }
                    } else if (groupAction.equals(Constant.STEP_START)) {
                        mapRunStep.put(new MapEntry(AamConstants.ACTION.SUB_STEP_START_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRunStatus());
                        // 15-12-2018 KienPD start
                        if (detailApp.getRunStatus() != null || (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().intValue() == 1)) {
                            // 15-12-2018 KienPD end
                            mapRunStep.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_STOP_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRollbackStatus());
                        }
                        /*20181108_hoangnd_save all step_start*/
                    } else if (groupAction.equals(Constant.STEP_CHECKLIST_APP)) {
                        mapRunStep.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CHECKLIST_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRunStatus());
                        mapRunStep.put(new MapEntry(AamConstants.ACTION.BEFORE_STEP_CHECKLIST_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getBeforeStatus());
                        /*20190125_hoangnd_fix bug show rollback checklist_start*/
                        // 15-12-2018 KienPD start
                        if (detailApp.getRunStatus() != null || (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().intValue() == 1)
                                || detailApp.getRollbackStatus() != null) {
                            // 15-12-2018 KienPD end
                        /*20190125_hoangnd_fix bug show rollback checklist_end*/
                            mapRunStep.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRollbackStatus());
                            mapRunStep.put(new MapEntry(AamConstants.ACTION.AFTER_STEP_CHECKLIST_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getAfterStatus());
                        }
                    } else if (groupAction.equals(Constant.STEP_CHECKLIST_DB)) {
                        mapRunStep.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CHECKLIST_DB, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRunStatus());
                        mapRunStep.put(new MapEntry(AamConstants.ACTION.BEFORE_STEP_CHECKLIST_DB, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getBeforeStatus());
                        /*20190125_hoangnd_fix bug show rollback checklist_start*/
                        // 15-12-2018 KienPD start
                        if (detailApp.getRunStatus() != null || (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().intValue() == 1)
                                || detailApp.getRollbackStatus() != null) {
                            // 15-12-2018 KienPD end
                        /*20190125_hoangnd_fix bug show rollback checklist_end*/
                            mapRunStep.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_DB, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getRollbackStatus());
                            mapRunStep.put(new MapEntry(AamConstants.ACTION.AFTER_STEP_CHECKLIST_DB, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), detailApp.getAfterStatus());
                        }
                    }
                    /*20181108_hoangnd_save all step_start*/
                }
            }

            for (ActionDetailDatabase detailDb : dbList) {
                ServiceDatabase database = iimService.findServiceDbById(selectedAction.getImpactProcess().getNationCode(), detailDb.getAppDbId());
                if (database == null)
                    continue;
                detailDb.setServiceDatabase(database);
                mapRunStep.put(new MapEntry(AamConstants.ACTION.SUB_STEP_BACKUP_DB, detailDb.getKbGroup() == null ? 1 : detailDb.getKbGroup()), detailDb.getBackupStatus());
                mapRunStep.put(new MapEntry(AamConstants.ACTION.SUB_STEP_TD_DB, detailDb.getKbGroup() == null ? 1 : detailDb.getKbGroup()), detailDb.getRunStatus());
                //12-11-2018 KienPD start
                mapRunStep.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_DB, detailDb.getKbGroup() == null ? 1 : detailDb.getKbGroup()), detailDb.getRollbackStatus());
                //12-11-2018 KienPD end
            }

            for (Integer kbGroup : kbGroups) {
                //05-12-2018 KienPD add step rollback check status start
                this.setupStatus(mapRunStep, new MapEntry(Constant.ROLLBACK_STEP_CHECK_STATUS, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.ROLLBACK_STEP_CHECKLIST_APP, kbGroup));
                /*20190125_hoangnd_fix bug icon_start*/
                this.setupStatus(mapRunStep, new MapEntry(Constant.ROLLBACK_STEP_CHECKLIST_DB, kbGroup));
                /*20190125_hoangnd_fix bug icon_end*/

                /*20181108_hoangnd_save all step_start*/
                this.setupStatus(mapRunStep, new MapEntry(Constant.SUB_STEP_CHECK_STATUS, kbGroup));
                /*20181108_hoangnd_save all step_end*/
                this.setupStatus(mapRunStep, new MapEntry(Constant.SUB_STEP_STOP_APP, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.SUB_STEP_BACKUP_APP, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.SUB_STEP_BACKUP_DB, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.SUB_STEP_UPCODE, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.SUB_STEP_TD_DB, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.SUB_STEP_CLEARCACHE, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.SUB_STEP_RESTART_APP, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.SUB_STEP_START_APP, kbGroup));
                /*20181108_hoangnd_save all step_start*/
                this.setupStatus(mapRunStep, new MapEntry(Constant.SUB_STEP_CHECKLIST_APP, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.BEFORE_STEP_CHECKLIST_APP, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.AFTER_STEP_CHECKLIST_APP, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.SUB_STEP_CHECKLIST_DB, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.BEFORE_STEP_CHECKLIST_DB, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.AFTER_STEP_CHECKLIST_DB, kbGroup));
                /*20181108_hoangnd_save all step_end*/

                this.setupStatus(mapRunStep, new MapEntry(Constant.ROLLBACK_STEP_STOP_APP, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.ROLLBACK_STEP_SOURCE_CODE, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.ROLLBACK_STEP_DB, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.ROLLBACK_STEP_CLEARCACHE, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.ROLLBACK_STEP_RESTART_APP, kbGroup));
                this.setupStatus(mapRunStep, new MapEntry(Constant.ROLLBACK_STEP_START_APP, kbGroup));
                //05-12-2018 KienPD add step rollback check status end
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        /*20181015_hoangnd_add status icon_end*/

        /*20190118_hoangnd_set fail step name_start*/
        /*if(history != null && history.getCurrStep() != null && history.getCurrKbGroup() != null) {
            MapEntry mapEntry = new MapEntry(history.getCurrStep(), history.getCurrKbGroup());
            RunStep failStep = Constant.getRunStep(mapEntry);
            this.failStep = String.format(MessageUtil.getResourceBundleMessage("confirm.select.step"), failStep.getLabel());
        } else {
            this.failStep = MessageUtil.getResourceBundleMessage("confirm.select.step1");
        }*/
        /*20190118_hoangnd_set fail step name_end*/
    }

    //05-12-2018 KienPD add step rollback check status start
    /*20181015_hoangnd_add status icon_start*/
    private void setupStatus(Multimap<MapEntry, Integer> mapRunStep, MapEntry mapEntry) {
        if (stepResult.get(mapEntry) == null) {
            return;
        }
        if (mapRunStep.isEmpty() || mapRunStep.get(mapEntry).isEmpty()) {
            stepResult.put(mapEntry, Constant.STAND_BY_STATUS);
            return;
        }
        int size = mapRunStep.get(mapEntry).size();
        if (size == 1) {
            stepResult.put(mapEntry, mapRunStep.get(mapEntry).iterator().next() == null ? 0 : mapRunStep.get(mapEntry).iterator().next());
            return;
        } else {
            if (mapRunStep.get(mapEntry).contains(-2)) {
                stepResult.put(mapEntry, Constant.FINISH_FAIL_STATUS);
                return;
            }
            int cntNotRun = 0;
            int cntSuccess = 0;
            int cntWarning = 0;
            int cntRunning = 0;
            for (Integer stt : mapRunStep.get(mapEntry)) {
                if (stt != null) {
                    switch (stt) {
                        case 0:
                            cntNotRun++;
                            break;
                        case 1:
                            cntRunning++;
                            break;
                        case 2:
                            cntSuccess++;
                            break;
                        case 3:
                            cntWarning++;
                            break;
                        case 4:
                            cntWarning++;
                            break;
                        default:
                            break;
                    }
                }
            }
            if (cntNotRun > 0) {
                stepResult.put(mapEntry, Constant.STAND_BY_STATUS);
            } else if (cntRunning > 0 && cntNotRun == 0) {
                stepResult.put(mapEntry, Constant.RUNNING_STATUS);
            } else if (cntNotRun == 0 && cntRunning == 0 && cntWarning == 0 && cntSuccess > 0) {
                stepResult.put(mapEntry, Constant.FINISH_SUCCESS_STATUS);
            } else if (cntNotRun == 0 && cntRunning == 0 && cntWarning > 0) {
                stepResult.put(mapEntry, Constant.FINISH_SUCCESS_WITH_WARNING);
            }
            return;
        }
    }
    /*20181015_hoangnd_add status icon_end*/
    //05-12-2018 KienPD add step rollback check status end


    public void onloadCr() {
        logger.info("onloadCr");
//		ExecuteCommandThread executeCommandThread = ThreadExeManager.getInstance().getExeCommandThread(username, selectedAction.getId());
        ChecklistInfo checklistInfo = checkProcessRunning();
        if (checklistInfo != null) {
            customExeObjectMultimap = checklistInfo.getCustomExeObjectMultimap();
            impactObjects = checklistInfo.getImpactObjects();
            rollbackObjects = checklistInfo.getRollbackObjects();

            rollbackCustomExeObjectMultimap = checklistInfo.getRollbackCustomExeObjectMultimap();

            executeChecklistController.setCklAppBefore(checklistInfo.getCklAppBefore());
            executeChecklistController.setNewCklAppMain(checklistInfo.getNewCklAppMain());
            executeChecklistController.setCklAppAfter(checklistInfo.getCklAppAfter());
//			executeChecklistController.setCklAppRollback(checklistInfo.getCklAppRollback());
            executeChecklistController.setNewCklAppRollback(checklistInfo.getNewCklAppRollback());

            executeChecklistController.setCklDbBefore(checklistInfo.getCklDbBefore());
            executeChecklistController.setCklDbMain(checklistInfo.getCklDbMain());
            executeChecklistController.setCklDbAfter(checklistInfo.getCklDbAfter());
            executeChecklistController.setCklDbRollback(checklistInfo.getCklDbRollback());

            selectedAction = checklistInfo.getAction();
            selectedStep = checklistInfo.getCurrentStep();
            stepResult = checklistInfo.getStepResult();
            history = checklistInfo.getHistory();
//			runSteps = checklistInfo.getNodeExe().getRunSteps();
            waitingActions = checklistInfo.getWaitingActions();
            runId = checklistInfo.getRunId();

            this.infoList = new ArrayList<>();
            // longlt6 add
//			this.createInformation();

            isRunning = Boolean.TRUE;
            RequestContext reqCtx = RequestContext.getCurrentInstance();
            if (reqCtx != null) {
                reqCtx.execute("PF('pollListener').start()");
                checkBuildRb = true;
            }
        }
    }

    private void reload(Boolean callService) {

        //20181205_tudn_start kiem tra module tac dong impact
        if (!callService) {
            mapResultMess = new HashMap<>();
            RequestContext.getCurrentInstance().execute("PF('loadingDialog').show()");
            logger.info("Type of impact:-----IMPACT-----");
        }
        sufficientImpact = Boolean.TRUE;
        //20181205_tudn_end kiem tra module tac dong impact
        customExeObjectMultimap = HashMultimap.create();
        impactObjects = HashMultimap.create();
        appGroupnames = new ArrayList<>();

        checkVersionAppObjects = new ArrayList<>();

        history = new ActionHistory();

        if (selectedAction != null) {
            Module module = null;
            ActionModule actionModule;
            history.setAction(selectedAction);
            history.setRunUser(username);

            List<ActionDetailApp> detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_STOP, null, true);
            Long maxOrder = -1L;
            int minOrder = 0;
            //20181205_tudn_start kiem tra module tac dong impact
            int countModule = 0;
            if (!callService) {
                if (detailApps.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
            //20181205_tudn_end kiem tra module tac dong impact

            for (ActionDetailApp app : detailApps) {
                actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
                ExeObject rstObject = new ExeObject();
                //20181205_tudn_start kiem tra module tac dong impact
                if (sufficientImpact) {
                    //20181205_tudn_end kiem tra module tac dong impact
                    try {
                        module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                    } catch (AppException e) {
                        module= null;
                        logger.error(e.getMessage(), e);
                    }
                    if (module != null) {
                        appGroupnames.add(module.getServiceName());
                        //20181205_tudn_start kiem tra module tac dong impact
                        if (!callService) {
                            countModule++;
                            logger.info("GET MODULE IMPACT: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP STOP BY IIM");
                        }
                        //20181205_tudn_end kiem tra module tac dong impact
                    }
                    //20181205_tudn_start kiem tra module tac dong impact
                    else {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                                String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info("GET MODULE IMPACT FAIL: " + moduleCode + " STEP STOP BY IIM");
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        }
                    }
                    if (!callService) {
                        if (countModule == detailApps.size()) {
                            logger.info("GET ENOUGH MODULES IMPACT STEP STOP IIM");
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                }
                //20181205_tudn_end kiem tra module tac dong impact

                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
                rstObject.setActionModule(actionModule);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(app.getModuleOrder().intValue());

                if (app.getModuleOrder() > maxOrder) {
                    maxOrder = app.getModuleOrder();
                }

                if (app.getModuleOrder() < minOrder)
                    minOrder = app.getModuleOrder().intValue();

                if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                    impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_STOP_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
//				stopObjects.add(rstObject);
            }

            Map<String, Object> filters = new HashMap<>();
            filters.put("type", "2");
            filters.put("actionId", selectedAction.getId() + "");
            try {
                List<ActionDetailDatabase> databases = actionDetailDatabaseService.findList(filters, new HashMap<>());
                //20181126_tudn_start kiem tra module tac dong impact
                int countDB = 0;
                if (!callService) {
                    if (databases.size() == 0) {
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
                //20181126_tudn_end kiem tra module tac dong impact

                for (ActionDetailDatabase database : databases) {
                    ServiceDatabase serviceDatabase = iimService.findServiceDbById(selectedAction.getImpactProcess().getNationCode(), database.getAppDbId());
                    //20181126_tudn_start kiem tra module tac dong impact
//				if (serviceDatabase == null){
//					continue;
                    if (serviceDatabase == null) {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                String dbName = database.getServiceDatabase() != null && database.getServiceDatabase().getDbName() != null ? database.getServiceDatabase().getDbName() : String.valueOf(database.getAppDbId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.database.count.sucess"), String.valueOf(countDB), String.valueOf(databases.size()), dbName);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info(messageModule);
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        } else {
                            continue;
                        }
                    } else {
                        countDB++;
                    }
                    if (!callService) {
                        if (countDB == databases.size()) {
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                    //20181126_tudn_end kiem tra module tac dong impact

                    database.setServiceDatabase(serviceDatabase);

                    ExeObject rstObject = new ExeObject();
                    rstObject.setAction(selectedAction);
                    rstObject.setActionDatabase(database);
                    rstObject.setServiceDb(serviceDatabase);
                    rstObject.setCreateUser(username);
                    rstObject.setRunStt(Constant.STAND_BY_STATUS);
//					rstObject.setOrderIndex(database.getActionOrder().intValue() + maxOrder.intValue());
                    rstObject.setOrderIndex(minOrder - database.getActionOrder().intValue());

                    if ((database.getRunRollbackOnly() != null && database.getRunRollbackOnly() == AamConstants.ON_OFF_FLAG.START) || (database.getTypeImport().equals(1L) && StringUtils.isEmpty(database.getScriptExecute())) || (database.getTypeImport().equals(0L) && StringUtils.isEmpty(database.getScriptText())))
                        continue;

                    rstObject.setActionDb(2);
//					stopObjects.add(rstObject);
                    if ((selectedAction.getTestbedMode() && database.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && database.getTestbedMode() != 1))
                        impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_STOP_APP, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }

            /*20181103_hoangnd_save all step_start*/
            detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_CHECK_STATUS, null, true);

            //20181205_tudn_start kiem tra module tac dong impact
            if (!callService) {
                countModule = 0;
                if (detailApps.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
            //20181205_tudn_end kiem tra module tac dong impact

            for (ActionDetailApp app : detailApps) {
                actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
                /*20181103_hoangnd_save all step_start*/
				/*if (actionModule.getAppTypeCode() != null && actionModule.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
					continue;
				}*/
                /*20181103_hoangnd_save all step_end*/
                ExeObject rstObject = new ExeObject();
                //20181205_tudn_start kiem tra module tac dong impact
                if (sufficientImpact) {
                    //20181205_tudn_end kiem tra module tac dong impact
                    try {
                        module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                    } catch (AppException e) {
                        module= null;
                        logger.error(e.getMessage(), e);
                    }
                    if (module != null) {
                        appGroupnames.add(module.getServiceName());
                        //20181205_tudn_start kiem tra module tac dong impact
                        if (!callService) {
                            countModule++;
                            logger.info("GET MODULE IMPACT: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP STOP NOT CODETAPTRUNG_TYPE  BY IIM");
                        }
                        //20181205_tudn_end kiem tra module tac dong impact
                    }
                    //20181205_tudn_start kiem tra module tac dong impact
                    else {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                                String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info("GET MODULE IMPACT FAIL: " + moduleCode + " STEP STOP NOT CODETAPTRUNG_TYPE BY IIM");
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        }
                    }
                    if (!callService) {
                        if (countModule == detailApps.size()) {
                            logger.info("GET ENOUGH MODULES IMPACT STEP STOP NOT CODETAPTRUNG_TYPE BY IIM");
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                }
                //20181205_tudn_end kiem tra module tac dong impact
				/*20181103_hoangnd_save all step_start*/
//				app.setGroupAction(Constant.STEP_CHECK_STATUS);
                /*20181103_hoangnd_save all step_end*/
                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
                rstObject.setActionModule(actionModule);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                /*20181103_hoangnd_save all step_start*/
                rstObject.setOrderIndex(app.getModuleOrder().intValue());
                /*20181103_hoangnd_save all step_end*/
                if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                    impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CHECK_STATUS, 1), rstObject);
//				checkStatusObjects.add(rstObject);
            }

            detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_UPCODE, null, true);

            //20181205_tudn_start kiem tra module tac dong impact
            if (!callService) {
                countModule = 0;
                if (detailApps.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
            //20181205_tudn_end kiem tra module tac dong impact

            for (ActionDetailApp app : detailApps) {
                actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
                ExeObject rstObject = new ExeObject();
                //20181205_tudn_start kiem tra module tac dong impact
                if (sufficientImpact) {
                    //20181205_tudn_end kiem tra module tac dong impact
                    try {
                        module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                    } catch (AppException e) {
                        module= null;
                        logger.error(e.getMessage(), e);
                    }
                    if (module != null) {
                        appGroupnames.add(module.getServiceName());
                        //20181205_tudn_start kiem tra module tac dong impact
                        if (!callService) {
                            countModule++;
                            logger.info("GET MODULE IMPACT: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP UPCODE BY IIM");
                        }
                        //20181205_tudn_end kiem tra module tac dong impact
                    }
                    //20181205_tudn_start kiem tra module tac dong impact
                    else {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                                String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info("GET MODULE IMPACT FAIL: " + moduleCode + " STEP UPCODE BY IIM");
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        }
                    }
                    if (!callService) {
                        if (countModule == detailApps.size()) {
                            logger.info("GET ENOUGH MODULES IMPACT STEP UPCODE IIM");
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                }
                //20181205_tudn_end kiem tra module tac dong impact

                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
                rstObject.setActionModule(actionModule);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(app.getModuleOrder().intValue());
                if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                    impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_UPCODE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
//				upcodeObjects.add(rstObject);
            }

            detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_UPCODE, null, true);

            //20181205_tudn_start kiem tra module tac dong impact
            if (!callService) {
                countModule = 0;
                if (detailApps.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
            //20181205_tudn_end kiem tra module tac dong impact

            for (ActionDetailApp app : detailApps) {
                actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
                ExeObject rstObject = new ExeObject();
                //20181205_tudn_start kiem tra module tac dong impact
                if (sufficientImpact) {
                    //20181205_tudn_end kiem tra module tac dong impact
                    try {
                        module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                    } catch (AppException e) {
                        module= null;
                        logger.error(e.getMessage(), e);
                    }
                    if (module != null) {
                        appGroupnames.add(module.getServiceName());
                        //20181205_tudn_start kiem tra module tac dong impact
                        if (!callService) {
                            countModule++;
                            logger.info("GET MODULE IMPACT: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP UPCODE BY IIM");
                        }
                        //20181205_tudn_end kiem tra module tac dong impact
                    }
                    //20181205_tudn_start kiem tra module tac dong impact
                    else {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                                String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info("GET MODULE IMPACT FAIL: " + moduleCode + " STEP UPCODE BY IIM");
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        }
                    }
                    if (!callService) {
                        if (countModule == detailApps.size()) {
                            logger.info("GET ENOUGH MODULES IMPACT STEP UPCODE BY IIM");
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                }
                //20181205_tudn_end kiem tra module tac dong impact

                app.setGroupAction(Constant.STEP_BACKUP);
                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
                rstObject.setActionModule(actionModule);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(app.getModuleOrder().intValue());
                if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                    impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_BACKUP_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
//				backupObjects.add(rstObject);
            }

            detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_CLEARCACHE, null, true);
            //20181205_tudn_start kiem tra module tac dong impact
            if (!callService) {
                countModule = 0;
                if (detailApps.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
            //20181205_tudn_end kiem tra module tac dong impact
            for (ActionDetailApp app : detailApps) {
                actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
                ExeObject rstObject = new ExeObject();
                //20181205_tudn_start kiem tra module tac dong impact
                if (sufficientImpact) {
                    //20181205_tudn_end kiem tra module tac dong impact
                    try {
                        module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                    } catch (AppException e) {
                        module= null;
                        logger.error(e.getMessage(), e);
                    }
                    if (module != null) {
                        appGroupnames.add(module.getServiceName());
                        //20181205_tudn_start kiem tra module tac dong impact
                        if (!callService) {
                            countModule++;
                            logger.info("GET MODULE IMPACT: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP CLEARCACHE BY IIM");
                        }
                        //20181205_tudn_end kiem tra module tac dong impact
                    }
                    //20181205_tudn_start kiem tra module tac dong impact
                    else {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                                String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info("GET MODULE IMPACT FAIL: " + moduleCode + " STEP CLEARCACHE BY IIM");
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        }
                    }
                    if (!callService) {
                        if (countModule == detailApps.size()) {
                            logger.info("GET ENOUGH MODULES IMPACT STEP CLEARCACHE BY IIM");
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                }
                //20181205_tudn_end kiem tra module tac dong impact

                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
                rstObject.setActionModule(actionModule);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(app.getModuleOrder().intValue());
                if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                    impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CLEARCACHE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
//				clearCacheObjects.add(rstObject);
            }

            detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_START, null, true);
            //20181205_tudn_start kiem tra module tac dong impact
            if (!callService) {
                countModule = 0;
                if (detailApps.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
            //20181205_tudn_end kiem tra module tac dong impact
            minOrder = 0;
            maxOrder = -1L;

            for (ActionDetailApp app : detailApps) {
                if (app.getModuleOrder() < minOrder)
                    minOrder = app.getModuleOrder().intValue();

                if (app.getModuleOrder() > maxOrder) {
                    maxOrder = app.getModuleOrder();
                }

                actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
                ExeObject rstObject = new ExeObject();
                //20181205_tudn_start kiem tra module tac dong impact
                if (sufficientImpact) {
                    //20181205_tudn_end kiem tra module tac dong impact
                    try {
                        module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                    } catch (AppException e) {
                        module= null;
                        logger.error(e.getMessage(), e);
                    }
                    if (module != null) {
                        appGroupnames.add(module.getServiceName());
                        //20181205_tudn_start kiem tra module tac dong impact
                        if (!callService) {
                            countModule++;
                            logger.info("GET MODULE IMPACT: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP START BY IIM");
                        }
                        //20181205_tudn_end kiem tra module tac dong impact
                    }
                    //20181205_tudn_start kiem tra module tac dong impact
                    else {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                                String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info("GET MODULE IMPACT FAIL: " + moduleCode + " STEP START BY IIM");
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        }
                    }
                    if (!callService) {
                        if (countModule == detailApps.size()) {
                            logger.info("GET ENOUGH MODULES IMPACT STEP START IIM");
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                }
                //20181205_tudn_end kiem tra module tac dong impact

                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
                rstObject.setActionModule(actionModule);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(app.getModuleOrder().intValue());

                if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                    impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_START_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
//				startObjects.add(rstObject);
            }

            detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_UPCODE_STOP_START, null, true);
            //20181205_tudn_start kiem tra module tac dong impact
            if (!callService) {
                countModule = 0;
                if (detailApps.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
            //20181205_tudn_end kiem tra module tac dong impact
            for (ActionDetailApp app : detailApps) {
                if (app.getModuleOrder() < minOrder)
                    minOrder = app.getModuleOrder().intValue();

                if (app.getModuleOrder() > maxOrder) {
                    maxOrder = app.getModuleOrder();
                }

                actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
                ExeObject rstObject = new ExeObject();
                //20181205_tudn_start kiem tra module tac dong impact
                if (sufficientImpact) {
                    //20181205_tudn_end kiem tra module tac dong impact
                    try {
                        module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                    } catch (AppException e) {
                        module= null;
                        logger.error(e.getMessage(), e);
                    }
                    if (module != null) {
                        appGroupnames.add(module.getServiceName());
                        //20181205_tudn_start kiem tra module tac dong impact
                        if (!callService) {
                            countModule++;
                            logger.info("GET MODULE IMPACT: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP UPCODE_STOP_START BY IIM");
                        }
                        //20181205_tudn_end kiem tra module tac dong impact
                    }
                    //20181205_tudn_start kiem tra module tac dong impact
                    else {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                                String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info("GET MODULE IMPACT FAIL: " + moduleCode + " STEP UPCODE_STOP_START BY IIM");
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        }
                    }
                    if (!callService) {
                        if (countModule == detailApps.size()) {
                            logger.info("GET ENOUGH MODULES IMPACT STEP UPCODE_STOP_START IIM");
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                }
                //20181205_tudn_end kiem tra module tac dong impact

                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
                rstObject.setActionModule(actionModule);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(app.getModuleOrder().intValue());
                if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                    impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_UPCODE_START_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
//				upcodeStartObjects.add(rstObject);
            }

            filters = new HashMap<>();
            filters.put("type", "2");
            filters.put("actionId", selectedAction.getId() + "");
            Map<String, String> orders = new HashMap<>();
            orders.put("actionOrder", "DESC");

            try {
                List<ActionDetailDatabase> databases = actionDetailDatabaseService.findList(filters, orders);
                //20181126_tudn_start kiem tra module tac dong impact
                int countDB = 0;
                if (!callService) {
                    if (databases.size() == 0) {
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
                //20181126_tudn_end kiem tra module tac dong impact

                for (ActionDetailDatabase database : databases) {
                    ServiceDatabase serviceDatabase = iimService.findServiceDbById(selectedAction.getImpactProcess().getNationCode(), database.getAppDbId());
                    //20181126_tudn_start kiem tra module tac dong impact
//				if (serviceDatabase == null){
//					continue;
                    if (serviceDatabase == null) {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                String dbName = database.getServiceDatabase() != null && database.getServiceDatabase().getDbName() != null ? database.getServiceDatabase().getDbName() : String.valueOf(database.getAppDbId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.database.count.sucess"), String.valueOf(countDB), String.valueOf(databases.size()), dbName);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info(messageModule);
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        } else {
                            continue;
                        }
                    } else {
                        countDB++;
                    }
                    if (!callService) {
                        if (countDB == databases.size()) {
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                    //20181126_tudn_end kiem tra module tac dong impact

                    database.setServiceDatabase(serviceDatabase);

                    ExeObject rstObject = new ExeObject();
                    rstObject.setAction(selectedAction);
                    rstObject.setActionDatabase(database);
                    rstObject.setServiceDb(serviceDatabase);
                    rstObject.setCreateUser(username);
                    rstObject.setRunStt(Constant.STAND_BY_STATUS);
                    rstObject.setOrderIndex(maxOrder.intValue() + database.getActionOrder().intValue());
                    rstObject.setStartFlag(true);

                    if ((database.getRunRollbackOnly() != null && database.getRunRollbackOnly() == AamConstants.ON_OFF_FLAG.STOP) || (database.getTypeImport().equals(1L) && StringUtils.isEmpty(database.getRollbackFile())) || (database.getTypeImport().equals(0L) && StringUtils.isEmpty(database.getRollbackText())))
                        continue;

                    rstObject.setActionDb(2);
                    if ((selectedAction.getTestbedMode() && database.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && database.getTestbedMode() != 1))
                        impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_START_APP, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
//					startObjects.add(rstObject);
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }

            detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_RESTART, null, true);
            //20181205_tudn_start kiem tra module tac dong impact
            if (!callService) {
                countModule = 0;
                if (detailApps.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
            //20181205_tudn_end kiem tra module tac dong impact
            for (ActionDetailApp app : detailApps) {
                actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
                ExeObject rstObject = new ExeObject();
                //20181205_tudn_start kiem tra module tac dong impact
                if (sufficientImpact) {
                    //20181205_tudn_end kiem tra module tac dong impact
                    try {
                        module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                    } catch (AppException e) {
                        module= null;
                        logger.error(e.getMessage(), e);
                    }
                    if (module != null) {
                        appGroupnames.add(module.getServiceName());
                        //20181205_tudn_start kiem tra module tac dong impact
                        if (!callService) {
                            countModule++;
                            logger.info("GET MODULE IMPACT: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP RESTART BY IIM");
                        }
                        //20181205_tudn_end kiem tra module tac dong impact
                    }
                    //20181205_tudn_start kiem tra module tac dong impact
                    else {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                                String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info("GET MODULE IMPACT FAIL: " + moduleCode + " STEP RESTART BY IIM");
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        }
                    }
                    if (!callService) {
                        if (countModule == detailApps.size()) {
                            logger.info("GET ENOUGH MODULES IMPACT STEP RESTART IIM");
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                }
                //20181205_tudn_end kiem tra module tac dong impact

                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
                rstObject.setActionModule(actionModule);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(app.getModuleOrder().intValue());
                if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                    impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
//				restartObjects.add(rstObject);
            }

            /*20181103_hoangnd_save all step_start*/
			/*detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_RESTART, null, true);
			for (ActionDetailApp app : detailApps) {
				actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
				if (actionModule.getAppTypeCode() != null && actionModule.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
					continue;
				}
				ExeObject rstObject = new ExeObject();
				try {
					module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
				} catch (AppException e) {
					logger.error(e.getMessage(), e);
				}
				if (module != null) {
					appGroupnames.add(module.getServiceName());
				}

				app.setGroupAction(Constant.STEP_CHECK_STATUS);
				rstObject.setModule(module);
				rstObject.setAction(selectedAction);
				rstObject.setDetailApp(app);
				rstObject.setActionModule(actionModule);
				rstObject.setCreateUser(username);
				rstObject.setRunStt(Constant.STAND_BY_STATUS);
				rstObject.setOrderIndex(app.getModuleOrder().intValue());

				if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
					impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CHECK_STATUS, 1), rstObject);
//				checkStatusObjects.add(rstObject);
			}*/
            /*20181103_hoangnd_save all step_end*/

            detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_RESTART_CMD, null, true);
            //20181205_tudn_start kiem tra module tac dong impact
            if (!callService) {
                countModule = 0;
                if (detailApps.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
            //20181205_tudn_end kiem tra module tac dong impact
            for (ActionDetailApp app : detailApps) {
                actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
                ExeObject rstObject = new ExeObject();
                //20181205_tudn_start kiem tra module tac dong impact
                if (sufficientImpact) {
                    //20181205_tudn_end kiem tra module tac dong impact
                    try {
                        module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                    } catch (AppException e) {
                        module= null;
                        logger.error(e.getMessage(), e);
                    }
                    if (module != null) {
                        appGroupnames.add(module.getServiceName());
                        //20181205_tudn_start kiem tra module tac dong impact
                        if (!callService) {
                            countModule++;
                            logger.info("GET MODULE IMPACT: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP RESTART_CMD BY IIM");
                        }
                        //20181205_tudn_end kiem tra module tac dong impact
                    }
                    //20181205_tudn_start kiem tra module tac dong impact
                    else {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                                String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info("GET MODULE IMPACT FAIL: " + moduleCode + " STEP RESTART_CMD BY IIM");
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        }
                    }
                    if (!callService) {
                        if (countModule == detailApps.size()) {
                            logger.info("GET ENOUGH MODULES IMPACT STEP RESTART_CMD IIM");
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                }
                //20181205_tudn_end kiem tra module tac dong impact

                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
                rstObject.setActionModule(actionModule);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(app.getModuleOrder().intValue());

                if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                    impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
//				restartObjects.add(rstObject);
            }

            /*20181103_hoangnd_save all step_start*/
			/*detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_RESTART_CMD, null, true);
			for (ActionDetailApp app : detailApps) {
				actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
				if (actionModule.getAppTypeCode() != null && actionModule.getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) {
					continue;
				}
				ExeObject rstObject = new ExeObject();
				try {
					module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
				} catch (AppException e) {
					logger.error(e.getMessage(), e);
				}
				if (module != null) {
					appGroupnames.add(module.getServiceName());
				}

				app.setGroupAction(Constant.STEP_CHECK_STATUS);
				rstObject.setModule(module);
				rstObject.setAction(selectedAction);
				rstObject.setDetailApp(app);
				rstObject.setActionModule(actionModule);
				rstObject.setCreateUser(username);
				rstObject.setRunStt(Constant.STAND_BY_STATUS);

				if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
					impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CHECK_STATUS, 1), rstObject);
//				checkStatusObjects.add(rstObject);
			}*/
            /*20181103_hoangnd_save all step_end*/

            /*20181103_hoangnd_save all step_start*/
            detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_CHECKVERSION_APP, null, true);

            //20181205_tudn_start kiem tra module tac dong impact
            if (!callService) {
                countModule = 0;
                if (detailApps.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
            //20181205_tudn_end kiem tra module tac dong impact
			/*20181103_hoangnd_save all step_end*/
            for (ActionDetailApp app : detailApps) {
                actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
                ExeObject rstObject = new ExeObject();
                //20181205_tudn_start kiem tra module tac dong impact
                if (sufficientImpact) {
                    //20181205_tudn_end kiem tra module tac dong impact
                    try {
                        module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                    } catch (AppException e) {
                        module= null;
                        logger.error(e.getMessage(), e);
                    }
                    if (module != null) {
                        appGroupnames.add(module.getServiceName());
                        //20181205_tudn_start kiem tra module tac dong impact
                        if (!callService) {
                            countModule++;
                            logger.info("GET MODULE IMPACT: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP UPCODE BY IIM");
                        }
                        //20181205_tudn_end kiem tra module tac dong impact
                    }
					/*20181103_hoangnd_save all step_start*/
                    //20181205_tudn_start kiem tra module tac dong impact
                    else {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                                String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info("GET MODULE IMPACT FAIL: " + moduleCode + " STEP_CHECKVERSION_APP BY IIM");
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        }
                    }
                    if (!callService) {
                        if (countModule == detailApps.size()) {
                            logger.info("GET ENOUGH MODULES IMPACT STEP_CHECKVERSION_APP IIM");
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                }
                //20181205_tudn_end kiem tra module tac dong impact

//				app.setGroupAction(Constant.STEP_CHECKVERSION_APP);
                /*20181103_hoangnd_save all step_end*/
                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
                rstObject.setActionModule(actionModule);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(app.getModuleOrder().intValue());
                if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                    checkVersionAppObjects.add(rstObject);
            }

            /*20181102_hoangnd_save all step_start*/
            detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_CHECKLIST_APP, null, true);
            //20181205_tudn_start kiem tra module tac dong impact
            /*if (!callService) {
                countModule = 0;
                if (detailApps.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }*/
            //20181205_tudn_end kiem tra module tac dong impact
            for (ActionDetailApp app : detailApps) {
//				actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
                ExeObject rstObject = new ExeObject();
                //20181205_tudn_start kiem tra module tac dong impact
//                if (sufficientImpact) {
                //20181205_tudn_end kiem tra module tac dong impact
                try {
                    module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                } catch (AppException e) {
                    module= null;
                    logger.error(e.getMessage(), e);
                }
                if (module != null) {
                    appGroupnames.add(module.getServiceName());
                    //20181205_tudn_start kiem tra module tac dong impact
                        /*if (!callService) {
                            countModule++;
                            logger.info("GET MODULE IMPACT: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP UPCODE BY IIM");
                        }*/
                    //20181205_tudn_end kiem tra module tac dong impact

                }
                //20181205_tudn_start kiem tra module tac dong impact
                    /*else {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                                String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info("GET MODULE IMPACT FAIL: " + moduleCode + " STEP_CHECKLIST_APP BY IIM");
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        }
                    }
                    if (!callService) {
                        if (countModule == detailApps.size()) {
                            logger.info("GET ENOUGH MODULES IMPACT STEP_CHECKLIST_APP IIM");
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                }*/
                //20181205_tudn_end kiem tra module tac dong impact

                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
//				rstObject.setActionModule(actionModule);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(app.getModuleOrder().intValue());
//				if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CHECKLIST_APP, 1), rstObject);

                rstObject = new ExeObject();
                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(app.getModuleOrder().intValue());
                impactObjects.put(new MapEntry(AamConstants.ACTION.BEFORE_STEP_CHECKLIST_APP, 1), rstObject);
//				clearCacheObjects.add(rstObject);
            }

            detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_CHECKLIST_DB, null, true);
            //20181205_tudn_start kiem tra module tac dong impact
            /*if (!callService) {
                countModule = 0;
                if (detailApps.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }*/
            //20181205_tudn_end kiem tra module tac dong impact
            for (ActionDetailApp app : detailApps) {
//				actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
                ExeObject rstObject = new ExeObject();
                //20181205_tudn_start kiem tra module tac dong impact
//                if (sufficientImpact) {
                //20181205_tudn_end kiem tra module tac dong impact
                try {
                    module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                } catch (AppException e) {
                    module= null;
                    logger.error(e.getMessage(), e);
                }
                if (module != null) {
                    appGroupnames.add(module.getServiceName());
                    //20181205_tudn_start kiem tra module tac dong impact
                        /*if (!callService) {
                            countModule++;
                            logger.info("GET MODULE IMPACT: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP UPCODE BY IIM");
                        }*/
                    //20181205_tudn_end kiem tra module tac dong impact

                }
                //20181205_tudn_start kiem tra module tac dong impact
                    /*else {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                                String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info("GET MODULE IMPACT FAIL: " + moduleCode + " STEP_CHECKLIST_DB BY IIM");
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        }
                    }
                    if (!callService) {
                        if (countModule == detailApps.size()) {
                            logger.info("GET ENOUGH MODULES IMPACT STEP_CHECKLIST_DB IIM");
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                }*/
                //20181205_tudn_end kiem tra module tac dong impact

                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
//				rstObject.setActionModule(actionModule);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(app.getModuleOrder().intValue());
//				if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CHECKLIST_DB, 1), rstObject);

                rstObject = new ExeObject();
                rstObject.setModule(module);
                rstObject.setAction(selectedAction);
                rstObject.setDetailApp(app);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(app.getModuleOrder().intValue());
                impactObjects.put(new MapEntry(AamConstants.ACTION.BEFORE_STEP_CHECKLIST_DB, 1), rstObject);
//				clearCacheObjects.add(rstObject);
            }
            /*20181102_hoangnd_save all step_end*/
            filters = new HashMap<>();
            filters.put("actionId", selectedAction.getId() + "");
            try {
                List<ActionDetailDatabase> databases = actionDetailDatabaseService.findList(filters,
                        new HashMap<>());
                //20181126_tudn_start kiem tra module tac dong impact
                int countDB = 0;
                if (!callService) {
                    if (databases.size() == 0) {
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
                //20181126_tudn_end kiem tra module tac dong impact

                for (ActionDetailDatabase database : databases) {
                    if (database.getType().equals(2L))
                        continue;

                    ServiceDatabase serviceDatabase = iimService.findServiceDbById(selectedAction.getImpactProcess().getNationCode(), database.getAppDbId());
                    //20181126_tudn_start kiem tra module tac dong impact
//				if (serviceDatabase == null){
//					continue;
                    if (serviceDatabase == null) {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_TD;
                                sufficientImpact = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                String dbName = database.getServiceDatabase() != null && database.getServiceDatabase().getDbName() != null ? database.getServiceDatabase().getDbName() : String.valueOf(database.getAppDbId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.database.count.sucess"), String.valueOf(countDB), String.valueOf(databases.size()), dbName);
                                mapResultMess.put("sufficientImpact", messageModule);
                                logger.info(messageModule);
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        } else {
                            continue;
                        }
                    } else {
                        countDB++;
                    }
                    if (!callService) {
                        if (countDB == databases.size()) {
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                    //20181126_tudn_end kiem tra module tac dong impact

                    database.setServiceDatabase(serviceDatabase);

                    ExeObject rstObject = new ExeObject();

                    rstObject.setAction(selectedAction);

                    rstObject.setActionDatabase(database);
                    rstObject.setServiceDb(serviceDatabase);
                    rstObject.setCreateUser(username);
                    rstObject.setRunStt(Constant.STAND_BY_STATUS);
                    rstObject.setOrderIndex(database.getActionOrder().intValue());

                    rstObject.setActionDb(1);
                    if ((selectedAction.getTestbedMode() && database.getTestbedMode() != null && database.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && database.getTestbedMode() != null && database.getTestbedMode() != 1))
                        impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_TD_DB, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
//					executeDbObjects.add(rstObject);

                    if ((database.getType().equals(0L) && ((database.getTypeImport().equals(1L) && StringUtils.isNotEmpty(database.getScriptBackup()))
                            || (database.getTypeImport().equals(0L) && StringUtils.isNotEmpty(database.getBackupText()))))
                            || (database.getType().equals(1L) && StringUtils.isNotEmpty(database.getScriptBackup()))) {
                        rstObject = new ExeObject();
                        rstObject.setAction(selectedAction);

                        rstObject.setActionDatabase(database);
                        rstObject.setServiceDb(serviceDatabase);
                        rstObject.setCreateUser(username);
                        rstObject.setRunStt(Constant.STAND_BY_STATUS);
                        rstObject.setOrderIndex(database.getActionOrder().intValue());

                        rstObject.setActionDb(0);
                        if ((selectedAction.getTestbedMode() && database.getTestbedMode() != null && database.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && database.getTestbedMode() != null && database.getTestbedMode() != 1))
                            impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_BACKUP_DB, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
//						backupDbObjects.add(rstObject);
                    }
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }

            filters = new HashMap<>();
            filters.put("actionCustomGroup.actionId", selectedAction.getId());
            orders = new HashMap<>();
//			orders.put("actionCustomGroup.afterGroup", "ASC");
            orders.put("priority", "ASC");

            Map<Integer, Integer> maxOrders = new HashMap<>();

            ExeObject rstObject;
            try {
                List<ActionCustomAction> customActions = actionCustomActionService.findList(filters, orders);
                for (ActionCustomAction customAction : customActions) {
                    rstObject = new ExeObject();
                    rstObject.setAction(selectedAction);
                    rstObject.setCustomAction(customAction);
                    rstObject.setCreateUser(username);
                    rstObject.setRunStt(Constant.STAND_BY_STATUS);
                    rstObject.setOrderIndex(customAction.getPriority());

                    Integer max = maxOrders.get(customAction.getActionCustomGroup().getAfterGroup() + 20);
                    if (max == null || customAction.getPriority() > max)
                        maxOrders.put(customAction.getActionCustomGroup().getAfterGroup() + 20, customAction.getPriority());

                    switch (customAction.getType()) {
                        case 0:
                            actionModule = actionModuleService.findModule(selectedAction.getId(), customAction.getModuleId());
                            module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), customAction.getModuleId());

                            rstObject.setModule(module);
                            rstObject.setActionModule(actionModule);
                            break;
                        case 1:
                            ServiceDatabase database = iimService.findServiceDbById(selectedAction.getImpactProcess().getNationCode(), customAction.getDbId());

                            rstObject.setServiceDb(database);
                            rstObject.setActionDb(1);
                            break;
                        case 2:
                            ActionDtFile actionDtFile = actionDtFileService.findById(customAction.getFileId());

                            rstObject.setActionDtFile(actionDtFile);
                            break;
                        case 3:
                            break;
                        case 4:
                            List<String> ipServers = actionService.findIpReboot(selectedAction.getId());

                            String ipServer = ipServers.get(0);
                            rstObject.setIpAddress(ipServer);
                            break;
                        default:
                            rstObject = null;
                            break;
                    }

                    if (rstObject != null) {
                        customExeObjectMultimap.put(new MapEntry(customAction.getActionCustomGroup().getAfterGroup() + 20, 1), rstObject);

                        if (rstObject.getCustomAction().getType() == 0) {
                            ExeObject exeObject = new ExeObject();

                            ActionCustomAction newCustomAction;
                            Cloner cloner = new Cloner();
                            newCustomAction = cloner.deepClone(rstObject.getCustomAction());

                            if (rstObject.getCustomAction().getModuleAction() == Constant.SPECIAL_UPCODETEST_RESTART_STOP_START)
                                newCustomAction.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_RESTART_STOP_START);
                            else if (rstObject.getCustomAction().getModuleAction() == Constant.SPECIAL_UPCODETEST_RESTART)
                                newCustomAction.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_RESTART);
                            else if (rstObject.getCustomAction().getModuleAction() == Constant.SPECIAL_UPCODETEST_START)
                                newCustomAction.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_STOP);
                            else if (rstObject.getCustomAction().getModuleAction() == Constant.SPECIAL_UPCODETEST_STOP_START)
                                newCustomAction.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_STOP_START);
                            else if (rstObject.getCustomAction().getModuleAction() == Constant.SPECIAL_RESTART_STOP_START)
                                newCustomAction.setModuleAction(Constant.SPECIAL_ROLLBACK_RESTART_STOP_START);
                            else if (rstObject.getCustomAction().getModuleAction() == Constant.SPECIAL_RESTART)
                                newCustomAction.setModuleAction(Constant.SPECIAL_ROLLBACK_RESTART);
                            else if (rstObject.getCustomAction().getModuleAction() == Constant.SPECIAL_START)
                                newCustomAction.setModuleAction(Constant.SPECIAL_ROLLBACK_STOP);

                            exeObject.setAction(selectedAction);
                            exeObject.setCustomAction(newCustomAction);
                            exeObject.setCreateUser(username);
                            exeObject.setRunStt(Constant.STAND_BY_STATUS);
                            exeObject.setOrderIndex(customAction.getPriority());

                            exeObject.setModule(rstObject.getModule());
                            exeObject.setActionModule(rstObject.getActionModule());

                            customExeObjectMultimap.put(new MapEntry(customAction.getActionCustomGroup().getAfterGroup() + 20, customAction.getActionCustomGroup().getKbGroup() == null ? 1 : customAction.getActionCustomGroup().getKbGroup()), exeObject);
                        }
                    }
                }

                for (Map.Entry<MapEntry, ExeObject> entry : customExeObjectMultimap.entries()) {
                    if (entry.getValue().getCustomAction().getType().equals(0) && (entry.getValue().getCustomAction().getModuleAction() >= (200)))
                        entry.getValue().setOrderIndex(entry.getValue().getOrderIndex() + maxOrders.get(entry.getKey()));
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void handleChange(AjaxBehaviorEvent event) {
        change = true;
    }

    private void loadRollback(Boolean callService) {
        //20181126_tudn_start kiem tra module tac dong rollback
        if (!callService) {
            if (mapResultMess == null) {
                mapResultMess = new HashMap<>();
            }
            RequestContext.getCurrentInstance().execute("PF('loadingDialog').show()");
            logger.info("Type of impact:-----ROLLBACK-----");
            sufficientRollback = Boolean.TRUE;
        }
        //20181126_tudn_end kiem tra module tac dong rollback
        rollbackCustomExeObjectMultimap = HashMultimap.create();
        rollbackObjects = HashMultimap.create();

        Module module = null;
        ActionModule actionModule;

        Map<String, Object> filters = new HashMap<>();
        filters.put("actionCustomGroup.actionId", selectedAction.getId());
        Map<String, String> orders = new HashMap<>();
        orders.put("priority", "DESC");

        ExeObject rstObject;
        //05-11-2018 KienPD add step fail to rollback list start
        rollbackObjects = LinkedListMultimap.create();
        loadHistory();
        mapStepFail = LinkedListMultimap.create();
        stepFail = HashMultimap.create();
        stepImpactFail = new HashMap<>();
        //05-11-2018 KienPD add step fail to rollback list end
        try {
            List<ActionCustomAction> customActions = actionCustomActionService.findList(filters, orders);
            for (ActionCustomAction customAction : customActions) {
                if (customAction.getRunStatus() == null || (customAction.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
                        && customAction.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING && customAction.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER))
                    continue;

                rstObject = new ExeObject();
                rstObject.setAction(selectedAction);
                rstObject.setCustomAction(customAction);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
                rstObject.setOrderIndex(customAction.getPriority());

                switch (customAction.getType()) {
                    case 0:
                        actionModule = actionModuleService.findModule(selectedAction.getId(), customAction.getModuleId());
                        module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), customAction.getModuleId());

                        if (customAction.getRunStatus() == null || (!customAction.getRunStatus().equals(Constant.FINISH_SUCCESS_STATUS) && !customAction.getRunStatus().equals(Constant.FINISH_SUCCESS_WITH_WARNING)))
                            continue;

                        if (customAction.getRollbackTestStatus() != null && (customAction.getRollbackTestStatus().equals(Constant.FINISH_SUCCESS_STATUS) || customAction.getRollbackTestStatus().equals(Constant.FINISH_SUCCESS_WITH_WARNING)))
                            continue;

                        if (customAction.getModuleAction() == Constant.SPECIAL_UPCODETEST_RESTART_STOP_START)
                            customAction.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_RESTART_STOP_START);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_UPCODETEST_RESTART)
                            customAction.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_RESTART);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_UPCODETEST_START)
                            customAction.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_STOP);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_UPCODETEST_STOP_START)
                            customAction.setModuleAction(Constant.SPECIAL_ROLLBACKTEST_STOP_START);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_RESTART_STOP_START)
                            customAction.setModuleAction(Constant.SPECIAL_ROLLBACK_RESTART_STOP_START);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_RESTART)
                            customAction.setModuleAction(Constant.SPECIAL_ROLLBACK_RESTART);
                        else if (customAction.getModuleAction() == Constant.SPECIAL_START)
                            customAction.setModuleAction(Constant.SPECIAL_ROLLBACK_STOP);

                        rstObject.setModule(module);
                        rstObject.setActionModule(actionModule);
                        break;
                    case 1:
                        if (customAction.getDbAction() != 0 || StringUtils.isEmpty(customAction.getDbScriptRb()))
                            continue;
                        ServiceDatabase database = iimService.findServiceDbById(selectedAction.getImpactProcess().getNationCode(), customAction.getDbId());

                        rstObject.setServiceDb(database);
                        rstObject.setActionDb(1);
                        break;
                    case 2:
                        ActionDtFile actionDtFile = actionDtFileService.findById(customAction.getFileId());

                        if (StringUtils.isEmpty(actionDtFile.getRollbackFile()))
                            continue;
                        rstObject.setActionDtFile(actionDtFile);
                        break;
                    case 3:
                        break;
                    case 4:
//						rstObject.setIpAddress();
                        break;
                    default:
                        rstObject = null;
                        break;
                }

                Integer stepCustomRollback = 0;

                switch (customAction.getActionCustomGroup().getRollbackAfter()) {
                    case Constant.ROLLBACK_STEP_CHECK_STATUS:
                        stepCustomRollback = Constant.ROLLBACK_CUSTOM_STEP_CHECK_STATUS;
                        break;
                    case Constant.ROLLBACK_STEP_STOP_APP:
                        stepCustomRollback = Constant.ROLLBACK_CUSTOM_STEP_STOP_APP;
                        break;
                    case Constant.ROLLBACK_STEP_SOURCE_CODE:
                        stepCustomRollback = Constant.ROLLBACK_CUSTOM_STEP_UPCODE;
                        break;
                    case Constant.ROLLBACK_STEP_DB:
                        stepCustomRollback = Constant.ROLLBACK_CUSTOM_STEP_TD_DB;
                        break;
                    case Constant.ROLLBACK_STEP_CLEARCACHE:
                        stepCustomRollback = Constant.ROLLBACK_CUSTOM_STEP_CLEARCACHE;
                        break;
                    case Constant.ROLLBACK_STEP_RESTART_APP:
                        stepCustomRollback = Constant.ROLLBACK_CUSTOM_STEP_RESTART_APP;
                        break;
                    case Constant.ROLLBACK_STEP_START_APP:
                        stepCustomRollback = Constant.ROLLBACK_CUSTOM_STEP_START_APP;
                        break;
                    default:
                        break;
                }
                if (customAction.getType() != 4)
                    rollbackCustomExeObjectMultimap.put(new MapEntry(stepCustomRollback, customAction.getActionCustomGroup().getKbGroup() == null ? 1 : customAction.getActionCustomGroup().getKbGroup()), rstObject);
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        /*20181225_hoangnd_khong add buoc check status vao rollback_start*/
        List<ActionDetailApp> detailApps;
        //23-11-2018 KienPD add step check status start
        /*List<ActionDetailApp> detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_CHECK_STATUS, false, null, true);
        for (ActionDetailApp app : detailApps) {
            actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
            rstObject = new ExeObject();
            try {
                module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
            if (module != null) {
                appGroupnames.add(module.getServiceName());
            }

            app.setGroupAction(Constant.STEP_ROLLBACK_CHECK_STATUS);
            rstObject.setModule(module);
            rstObject.setAction(selectedAction);
            rstObject.setDetailApp(app);
            rstObject.setActionModule(actionModule);
            rstObject.setCreateUser(username);
            rstObject.setRunStt(Constant.STAND_BY_STATUS);
            rstObject.setOrderIndex(app.getModuleOrder().intValue());

            if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1)) {
                if ((history.getCurrStep() != null && history.getCurrStep().equals(AamConstants.ACTION.SUB_STEP_CHECK_STATUS))
                        || (app.getRunStatus() != null && app.getRunStatus().equals(-2))) {
                    mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECK_STATUS, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    stepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECK_STATUS, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    app.setGroupAction(Constant.STEP_CHECK_STATUS);
                    rstObject.setDetailApp(app);
                    stepImpactFail.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CHECK_STATUS, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), new Cloner().deepClone(rstObject));
                }
                if (app.getRunStatus() != null && (!(app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
                        && app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING
                        *//*20181214_hoangnd_them trang thai add buoc fail vao list rollback_start*//*
                        && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER)) || (app.getIsAddRollback() != null && app.getIsAddRollback().equals(1))) {
                        *//*20181214_hoangnd_them trang thai add buoc fail vao list rollback_end*//*
                    rollbackObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECK_STATUS, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECK_STATUS, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                }
            }
        }*/
        //23-11-2018 KienPD add step check status end
        /*20181225_hoangnd_khong add buoc check status vao rollback_end*/
        detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_START, false, null, true);
        Long maxOrder = 0L;
        int minOrder = 0;
        //20181126_tudn_start kiem tra module tac dong rollback
        int countModule = 0;
        if (!callService) {
            if (detailApps.size() == 0) {
                if (!callService) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
        }
        //20181126_tudn_end kiem tra module tac dong rollback

        for (ActionDetailApp app : detailApps) {
            /* kienpd_20180911_start */
			/*if (app.getRunStatus() == null || (app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
					&& app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER))
				continue;*/
            /* kienpd_20180911_end */

            if (app.getModuleOrder() > maxOrder)
                maxOrder = app.getModuleOrder();

            if (app.getModuleOrder() < minOrder)
                minOrder = app.getModuleOrder().intValue();


            actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
            rstObject = new ExeObject();
            //20181126_tudn_start kiem tra module tac dong rollback
            if (sufficientRollback) {
                //20181126_tudn_end kiem tra module tac dong rollback
                try {
                    module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                } catch (AppException e) {
                    module= null;
                    logger.error(e.getMessage(), e);
                }
                if (module != null) {
                    appGroupnames.add(module.getServiceName());
                    //20181126_tudn_start kiem tra module tac dong rollback
                    if (!callService) {
                        countModule++;
                        logger.info("GET MODULE ROLLBACK: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP START BY IIM");
                    }
                    //20181126_tudn_end kiem tra module tac dong rollback
                }
                //20181126_tudn_start kiem tra module tac dong rollback
                else {
                    if (!callService) {
                        if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                            executeTypeModule = Constant.EXE_ROLLBACK;
                            sufficientRollback = Boolean.FALSE;
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                            ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                            String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                            messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                            mapResultMess.put("sufficientRollback", messageModule);
                            logger.info("GET MODULE ROLLBACK FAIL: " + moduleCode + " STEP START BY IIM");
                            if (repeat) {
                                RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                return;
                            }
                        }
                    }
                }
                if (!callService) {
                    if (countModule == detailApps.size()) {
                        logger.info("GET ENOUGH MODULES ROLLBACK STEP START BY IIM");
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
            }
            //20181126_tudn_end kiem tra module tac dong rollback

            app.setGroupAction(Constant.STEP_ROLLBACK_STOP);
            rstObject.setModule(module);
            rstObject.setAction(selectedAction);
            rstObject.setDetailApp(app);
            rstObject.setActionModule(actionModule);
            rstObject.setCreateUser(username);
            rstObject.setRunStt(Constant.STAND_BY_STATUS);
            rstObject.setOrderIndex(app.getModuleOrder().intValue());

//			rollbackStopObjects.add(rstObject);
            if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                //05-11-2018 KienPD add failed step to rollback list start
                if ((null != history.getCurrStep() && history.getCurrStep().equals(AamConstants.ACTION.SUB_STEP_START_APP))
                        || (app.getRunStatus() != null && app.getRunStatus().equals(-2))) {
                    mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_STOP_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    stepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_STOP_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    app.setGroupAction(Constant.STEP_START);
                    rstObject.setDetailApp(app);
                    stepImpactFail.put(new MapEntry(AamConstants.ACTION.SUB_STEP_START_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), new Cloner().deepClone(rstObject));
                }
            if (app.getRunStatus() != null && (!(app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
                    && app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING
                    /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_start*/
                    && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER)) || (app.getIsAddRollback() != null && app.getIsAddRollback().equals(1))) {
                    /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_end*/
                rollbackObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_STOP_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_STOP_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
            }
            //05-11-2018 KienPD add failed step to rollback list end
        }

        filters = new HashMap<>();
        filters.put("type", "2");
        filters.put("actionId", selectedAction.getId() + "");
        orders = new HashMap<>();
        orders.put("actionOrder", "ASC");

        try {
            List<ActionDetailDatabase> databases = actionDetailDatabaseService.findList(filters, orders);
            //20181126_tudn_start kiem tra module tac dong rollback
            int countDB = 0;
            if (!callService) {
                if (databases.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
            //20181126_tudn_end kiem tra module tac dong rollback

            for (ActionDetailDatabase database : databases) {
                if ((database.getRunRollbackOnly() != null && database.getRunRollbackOnly() == AamConstants.ON_OFF_FLAG.STOP) || database.getBackupStatus() == null || (!database.getBackupStatus().equals(Constant.FINISH_SUCCESS_STATUS) && database.getBackupStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER))
                    continue;

                ServiceDatabase serviceDatabase = iimService.findServiceDbById(selectedAction.getImpactProcess().getNationCode(), database.getAppDbId());

                //20181126_tudn_start kiem tra module tac dong rollback
//				if (serviceDatabase == null){
//					continue;
                if (serviceDatabase == null) {
                    if (!callService) {
                        if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                            executeTypeModule = Constant.EXE_ROLLBACK;
                            sufficientRollback = Boolean.FALSE;
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                            String dbName = database.getServiceDatabase() != null && database.getServiceDatabase().getDbName() != null ? database.getServiceDatabase().getDbName() : String.valueOf(database.getAppDbId());
                            messageModule = String.format(MessageUtil.getResourceBundleMessage("get.database.count.sucess"), String.valueOf(countDB), String.valueOf(databases.size()), dbName);
                            mapResultMess.put("sufficientRollback", messageModule);
                            logger.info(messageModule);
                            if (repeat) {
                                RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                return;
                            }
                        }
                    } else {
                        continue;
                    }
                } else {
                    countDB++;
                }
                if (!callService) {
                    if (countDB == databases.size()) {
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
                //20181126_tudn_end kiem tra module tac dong rollback

                database.setServiceDatabase(serviceDatabase);

                rstObject = new ExeObject();
                rstObject.setAction(selectedAction);
                rstObject.setActionDatabase(database);
                rstObject.setServiceDb(serviceDatabase);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
//				rstObject.setOrderIndex(0 - database.getActionOrder().intValue());
                rstObject.setOrderIndex(maxOrder.intValue() + database.getActionOrder().intValue());
                rstObject.setStartFlag(false);

                rstObject.setActionDb(2);
                if ((selectedAction.getTestbedMode() && database.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && database.getTestbedMode() != 1))
                    //05-11-2018 KienPD add failed step to rollback list start
                    if ((history.getCurrStep() != null && history.getCurrStep().equals(AamConstants.ACTION.SUB_STEP_START_APP))
                            || (null != database.getRunStatus() && database.getRunStatus().equals(-2))) {
                        mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_STOP_APP, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                        stepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_STOP_APP, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                        stepFail.put(new MapEntry(AamConstants.ACTION.SUB_STEP_START_APP, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                    }
                rollbackObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_STOP_APP, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_STOP_APP, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                //05-11-2018 KienPD add failed step to rollback list end
//				rollbackStopObjects.add(rstObject);
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_UPCODE, false, null, true);

        //20181126_tudn_start kiem tra module tac dong rollback
        if (!callService) {
            countModule = 0;
            if (detailApps.size() == 0) {
                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
            }
        }
        //20181126_tudn_end kiem tra module tac dong rollback
        for (ActionDetailApp app : detailApps) {
            /* kienpd_20180911_start */
			/*if (app.getRunStatus() == null || (app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
					&& app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING  && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER))
				continue;*/
            /* kienpd_20180911_end */


            actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
            rstObject = new ExeObject();
            //20181126_tudn_start kiem tra module tac dong rollback
            if (sufficientRollback) {
                //20181126_tudn_end kiem tra module tac dong rollback
                try {
                    module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                } catch (AppException e) {
                    module= null;
                    logger.error(e.getMessage(), e);
                }
                if (module != null) {
                    appGroupnames.add(module.getServiceName());
                    //20181126_tudn_start kiem tra module tac dong rollback
                    if (!callService) {
                        countModule++;
                        logger.info("GET MODULE ROLLBACK: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP UPCODE BY IIM");
                    }
                    //20181126_tudn_end kiem tra module tac dong rollback
                }
                //20181126_tudn_start kiem tra module tac dong rollback
                else {
                    if (!callService) {
                        if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                            executeTypeModule = Constant.EXE_ROLLBACK;
                            sufficientRollback = Boolean.FALSE;
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                            ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                            String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                            messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                            mapResultMess.put("sufficientRollback", messageModule);
                            logger.info("GET MODULE ROLLBACK FAIL: " + moduleCode + " STEP UPCODE BY IIM");
                            if (repeat) {
                                RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                return;
                            }
                        }
                    }
                }
                if (!callService) {
                    if (countModule == detailApps.size()) {
                        logger.info("GET ENOUGH MODULES ROLLBACK STEP STOP BY IIM");
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
            }
            //20181126_tudn_end kiem tra module tac dong rollback

            app.setGroupAction(Constant.STEP_ROLLBACKCODE);
            rstObject.setModule(module);
            rstObject.setAction(selectedAction);
            rstObject.setDetailApp(app);
            rstObject.setActionModule(actionModule);
            rstObject.setCreateUser(username);
            rstObject.setRunStt(Constant.STAND_BY_STATUS);
            rstObject.setOrderIndex(app.getModuleOrder().intValue());

            if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                //05-11-2018 KienPD add failed step to rollback list start
                if ((history.getCurrStep() != null && history.getCurrStep().equals(AamConstants.ACTION.SUB_STEP_UPCODE))
                        || (app.getRunStatus() != null && app.getRunStatus().equals(-2))) {
                    mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_SOURCE_CODE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    stepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_SOURCE_CODE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    app.setGroupAction(Constant.STEP_UPCODE);
                    rstObject.setDetailApp(app);
                    stepImpactFail.put(new MapEntry(AamConstants.ACTION.SUB_STEP_UPCODE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), new Cloner().deepClone(rstObject));
                }
            if (app.getRunStatus() != null && (!(app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
                    && app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING
                        /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_start*/
                    && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER)) || (app.getIsAddRollback() != null && app.getIsAddRollback().equals(1))) {
                        /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_end*/
                rollbackObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_SOURCE_CODE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_SOURCE_CODE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
            }
            //05-11-2018 KienPD add failed step to rollback list end
//			rollbackCodeObjects.add(rstObject);
        }

        detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_CLEARCACHE, false, null, true);
        //20181126_tudn_start kiem tra module tac dong rollback
        if (!callService) {
            countModule = 0;
            if (detailApps.size() == 0) {
                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
            }
        }
        //20181126_tudn_end kiem tra module tac dong rollback
        for (ActionDetailApp app : detailApps) {
            /* kienpd_20180911_start */
			/*if (app.getRunStatus() == null || (app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
					&& app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER))
				continue;*/
            /* kienpd_20180911_end */


            actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
            rstObject = new ExeObject();
            //20181126_tudn_start kiem tra module tac dong rollback
            if (sufficientRollback) {
                //20181126_tudn_end kiem tra module tac dong rollback
                try {
                    module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                } catch (AppException e) {
                    module= null;
                    logger.error(e.getMessage(), e);
                }
                if (module != null) {
                    appGroupnames.add(module.getServiceName());
                    //20181126_tudn_start kiem tra module tac dong rollback
                    if (!callService) {
                        countModule++;
                        logger.info("GET MODULE ROLLBACK: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP CLEARCACHE BY IIM");
                    }
                    //20181126_tudn_end kiem tra module tac dong rollback
                }
                //20181126_tudn_start kiem tra module tac dong rollback
                else {
                    if (!callService) {
                        if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                            executeTypeModule = Constant.EXE_ROLLBACK;
                            sufficientRollback = Boolean.FALSE;
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                            ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                            String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                            messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                            mapResultMess.put("sufficientRollback", messageModule);
                            logger.info("GET MODULE ROLLBACK FAIL: " + moduleCode + " STEP CLEARCACHE BY IIM");
                            if (repeat) {
                                RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                return;
                            }
                        }
                    }
                }
                if (!callService) {
                    if (countModule == detailApps.size()) {
                        logger.info("GET ENOUGH MODULES ROLLBACK STEP CLEARCACHE BY IIM");
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
            }
            //20181126_tudn_end kiem tra module tac dong rollback

            app.setGroupAction(Constant.STEP_ROLLBACK_CLEARCACHE);
            rstObject.setModule(module);
            rstObject.setAction(selectedAction);
            rstObject.setDetailApp(app);
            rstObject.setActionModule(actionModule);
            rstObject.setCreateUser(username);
            rstObject.setRunStt(Constant.STAND_BY_STATUS);
            rstObject.setOrderIndex(app.getModuleOrder().intValue());

            if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                //05-11-2018 KienPD add failed step to rollback list start
                if ((history.getCurrStep() != null && history.getCurrStep().equals(AamConstants.ACTION.SUB_STEP_CLEARCACHE))
                        || (app.getRunStatus() != null && app.getRunStatus().equals(-2))) {
                    mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CLEARCACHE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    stepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CLEARCACHE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    app.setGroupAction(Constant.STEP_CLEARCACHE);
                    rstObject.setDetailApp(app);
                    stepImpactFail.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CLEARCACHE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), new Cloner().deepClone(rstObject));
                }
            if (app.getRunStatus() != null && (!(app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
                    && app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING
                    /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_start*/
                    && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER)) || (app.getIsAddRollback() != null && app.getIsAddRollback().equals(1))) {
                    /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_end*/
                rollbackObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CLEARCACHE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CLEARCACHE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
            }
            //05-11-2018 KienPD add failed step to rollback list end
//			rollbackClearCacheObjects.add(rstObject);
        }

        maxOrder = -1L;

        detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_STOP, false, null, true);
        //20181126_tudn_start kiem tra module tac dong rollback
        if (!callService) {
            countModule = 0;
            if (detailApps.size() == 0) {
                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
            }
        }
        //20181126_tudn_end kiem tra module tac dong rollback
        for (ActionDetailApp app : detailApps) {
            if (app.getModuleOrder() > maxOrder) {
                maxOrder = app.getModuleOrder();
            }

            /* kienpd_20180911_start */
			/*if (app.getRunStatus() == null || (app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
					&& app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER))
				continue;*/
            /* kienpd_20180911_end */


            actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
            rstObject = new ExeObject();
            //20181126_tudn_start kiem tra module tac dong rollback
            if (sufficientRollback) {
                //20181126_tudn_end kiem tra module tac dong rollback
                try {
                    module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                } catch (AppException e) {
                    module= null;
                    logger.error(e.getMessage(), e);
                }
                if (module != null) {
                    appGroupnames.add(module.getServiceName());
                    //20181126_tudn_start kiem tra module tac dong rollback
                    if (!callService) {
                        countModule++;
                        logger.info("GET MODULE ROLLBACK: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP STOP BY IIM");
                    }
                    //20181126_tudn_end kiem tra module tac dong rollback
                }
                //20181126_tudn_start kiem tra module tac dong rollback
                else {
                    if (!callService) {
                        if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                            executeTypeModule = Constant.EXE_ROLLBACK;
                            sufficientRollback = Boolean.FALSE;
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                            ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                            String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                            messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                            mapResultMess.put("sufficientRollback", messageModule);
                            logger.info("GET MODULE ROLLBACK FAIL: " + moduleCode + " STEP STOP BY IIM");
                            if (repeat) {
                                RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                return;
                            }
                        }
                    }
                }
                if (!callService) {
                    if (countModule == detailApps.size()) {
                        logger.info("GET ENOUGH MODULES ROLLBACK STEP STOP BY IIM");
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
            }
            //20181126_tudn_end kiem tra module tac dong rollback

            app.setGroupAction(Constant.STEP_ROLLBACK_START);
            rstObject.setModule(module);
            rstObject.setAction(selectedAction);
            rstObject.setDetailApp(app);
            rstObject.setActionModule(actionModule);
            rstObject.setCreateUser(username);
            rstObject.setRunStt(Constant.STAND_BY_STATUS);
            rstObject.setOrderIndex(app.getModuleOrder().intValue());

            if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                //05-11-2018 KienPD add failed step to rollback list start
                if ((null != history.getCurrStep() && history.getCurrStep().equals(AamConstants.ACTION.SUB_STEP_STOP_APP))
                        || (app.getRunStatus() != null && app.getRunStatus().equals(-2))) {
                    mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_START_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    stepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_START_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    app.setGroupAction(Constant.STEP_STOP);
                    rstObject.setDetailApp(app);
                    stepImpactFail.put(new MapEntry(AamConstants.ACTION.SUB_STEP_STOP_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), new Cloner().deepClone(rstObject));
                }
            if (app.getRunStatus() != null && (!(app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
                    && app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING
                    /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_start*/
                    && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER)) || (app.getIsAddRollback() != null && app.getIsAddRollback().equals(1))) {
                    /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_end*/
                rollbackObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_START_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_START_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
            }
            //05-11-2018 KienPD add failed step to rollback list end
//			rollbackStartObjects.add(rstObject);
        }

        filters = new HashMap<>();
        filters.put("type", "2");
        filters.put("actionId", selectedAction.getId() + "");
        orders = new HashMap<>();
        orders.put("actionOrder", "DESC");

        try {
            List<ActionDetailDatabase> databases = actionDetailDatabaseService.findList(filters, orders);
            //20181126_tudn_start kiem tra module tac dong rollback
            int countDB = 0;
            if (!callService) {
                if (databases.size() == 0) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
            //20181126_tudn_end kiem tra module tac dong rollback

            for (ActionDetailDatabase database : databases) {
                if ((database.getRunRollbackOnly() != null && database.getRunRollbackOnly() == AamConstants.ON_OFF_FLAG.START) || database.getRunStatus() == null || (!database.getRunStatus().equals(Constant.FINISH_SUCCESS_STATUS) && database.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER))
                    continue;

                ServiceDatabase serviceDatabase = iimService.findServiceDbById(selectedAction.getImpactProcess().getNationCode(), database.getAppDbId());
                //20181126_tudn_start kiem tra module tac dong rollback
//				if (serviceDatabase == null){
//					continue;
                if (serviceDatabase == null) {
                    if (!callService) {
                        if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                            executeTypeModule = Constant.EXE_ROLLBACK;
                            sufficientRollback = Boolean.FALSE;
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                            String dbName = database.getServiceDatabase() != null && database.getServiceDatabase().getDbName() != null ? database.getServiceDatabase().getDbName() : String.valueOf(database.getAppDbId());
                            messageModule = String.format(MessageUtil.getResourceBundleMessage("get.database.count.sucess"), String.valueOf(countDB), String.valueOf(databases.size()), dbName);
                            mapResultMess.put("sufficientRollback", messageModule);
                            logger.info(messageModule);
                            if (repeat) {
                                RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                return;
                            }
                        }
                    } else {
                        continue;
                    }
                } else {
                    countDB++;
                }
                if (!callService) {
                    if (countDB == databases.size()) {
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
                //20181126_tudn_end kiem tra module tac dong rollback

                database.setServiceDatabase(serviceDatabase);

                rstObject = new ExeObject();
                rstObject.setAction(selectedAction);
                rstObject.setActionDatabase(database);
                rstObject.setServiceDb(serviceDatabase);
                rstObject.setCreateUser(username);
                rstObject.setRunStt(Constant.STAND_BY_STATUS);
//				rstObject.setOrderIndex(maxOrder.intValue() + database.getActionOrder().intValue());
                rstObject.setOrderIndex(minOrder - database.getActionOrder().intValue());
                rstObject.setStartFlag(true);

                rstObject.setActionDb(2);
//				rollbackStartObjects.add(rstObject);
                if ((selectedAction.getTestbedMode() && database.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && database.getTestbedMode() != 1))
                    //05-11-2018 KienPD add failed step to rollback list start
                    if ((null != history.getCurrStep() && history.getCurrStep().equals(AamConstants.ACTION.SUB_STEP_STOP_APP))
                            || (null != database.getRunStatus() && database.getRunStatus().equals(-2))) {
                        mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_START_APP, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                        stepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_START_APP, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                        stepImpactFail.put(new MapEntry(AamConstants.ACTION.SUB_STEP_STOP_APP, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                    }
                rollbackObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_START_APP, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_START_APP, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                //05-11-2018 KienPD add failed step to rollback list end
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_UPCODE_STOP_START, false, null, true);
        //20181126_tudn_start kiem tra module tac dong rollback
        if (!callService) {
            countModule = 0;
            if (detailApps.size() == 0) {
                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
            }
        }
        //20181126_tudn_end kiem tra module tac dong rollback
        for (ActionDetailApp app : detailApps) {
            /* kienpd_20180911_start */
			/*if (app.getRunStatus() == null || (app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
					&& app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING  && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER))
				continue;*/
            /* kienpd_20180911_end */


            actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
            rstObject = new ExeObject();
            //20181126_tudn_start kiem tra module tac dong rollback
            if (sufficientRollback) {
                //20181126_tudn_end kiem tra module tac dong rollback
                try {
                    module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                } catch (AppException e) {
                    module= null;
                    logger.error(e.getMessage(), e);
                }
                if (module != null) {
                    appGroupnames.add(module.getServiceName());
                    //20181126_tudn_start kiem tra module tac dong rollback
                    if (!callService) {
                        countModule++;
                        logger.info("GET MODULE ROLLBACK: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP UPCODE_STOP_START BY IIM");
                    }
                    //20181126_tudn_end kiem tra module tac dong rollback
                }
                //20181126_tudn_start kiem tra module tac dong rollback
                else {
                    if (!callService) {
                        if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                            executeTypeModule = Constant.EXE_ROLLBACK;
                            sufficientRollback = Boolean.FALSE;
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                            ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                            String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                            messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                            mapResultMess.put("sufficientRollback", messageModule);
                            logger.info("GET MODULE ROLLBACK FAIL: " + moduleCode + " STEP UPCODE_STOP_START BY IIM");
                            if (repeat) {
                                RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                return;
                            }
                        }
                    }
                }
                if (!callService) {
                    if (countModule == detailApps.size()) {
                        logger.info("GET ENOUGH MODULES ROLLBACK STEP UPCODE_STOP_START BY IIM");
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
            }
            //20181126_tudn_end kiem tra module tac dong rollback

            app.setGroupAction(Constant.STEP_ROLLBACK_CODE_STOP_START);
            rstObject.setModule(module);
            rstObject.setAction(selectedAction);
            rstObject.setDetailApp(app);
            rstObject.setActionModule(actionModule);
            rstObject.setCreateUser(username);
            rstObject.setRunStt(Constant.STAND_BY_STATUS);
            rstObject.setOrderIndex(app.getModuleOrder().intValue());

            if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                //05-11-2018 KienPD add failed step to rollback list start
                if ((history.getCurrStep() != null && history.getCurrStep().equals(AamConstants.ACTION.SUB_STEP_UPCODE_START_APP))
                        || (app.getRunStatus() != null && app.getRunStatus().equals(-2))) {
                    mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_UPCODE_START, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    stepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_UPCODE_START, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    app.setGroupAction(Constant.STEP_UPCODE_STOP_START);
                    rstObject.setDetailApp(app);
                    stepImpactFail.put(new MapEntry(AamConstants.ACTION.SUB_STEP_UPCODE_START_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), new Cloner().deepClone(rstObject));
                }
            if (app.getRunStatus() != null && (!(app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
                    && app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING
                    /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_start*/
                    && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER)) || (app.getIsAddRollback() != null && app.getIsAddRollback().equals(1))) {
                    /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_end*/
                rollbackObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_UPCODE_START, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_UPCODE_START, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
            }
            //05-11-2018 KienPD add failed step to rollback list end
//			rollbackcodeStartObjects.add(rstObject);
        }

        detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_RESTART, false, null, true);
        //20181126_tudn_start kiem tra module tac dong rollback
        if (!callService) {
            countModule = 0;
            if (detailApps.size() == 0) {
                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
            }
        }
        //20181126_tudn_end kiem tra module tac dong rollback
        for (ActionDetailApp app : detailApps) {
            /* kienpd_20180911_start */
			/*if (app.getRunStatus() == null || (app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
					&& app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER))
				continue;*/
            /* kienpd_20180911_end */


            actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
            rstObject = new ExeObject();
            //20181126_tudn_start kiem tra module tac dong rollback
            if (sufficientRollback) {
                //20181126_tudn_end kiem tra module tac dong rollback
                try {
                    module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                } catch (AppException e) {
                    module= null;
                    logger.error(e.getMessage(), e);
                }
                if (module != null) {
                    appGroupnames.add(module.getServiceName());
                    //20181126_tudn_start kiem tra module tac dong rollback
                    if (!callService) {
                        countModule++;
                        logger.info("GET MODULE ROLLBACK: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP RESTART BY IIM");
                    }
                    //20181126_tudn_end kiem tra module tac dong rollback
                }
                //20181126_tudn_start kiem tra module tac dong rollback
                else {
                    if (!callService) {
                        if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                            executeTypeModule = Constant.EXE_ROLLBACK;
                            sufficientRollback = Boolean.FALSE;
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                            ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                            String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                            messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                            mapResultMess.put("sufficientRollback", messageModule);
                            logger.info("GET MODULE ROLLBACK FAIL: " + moduleCode + " STEP RESTART BY IIM");
                            if (repeat) {
                                RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                return;
                            }
                        }
                    }
                }
                if (!callService) {
                    if (countModule == detailApps.size()) {
                        logger.info("GET ENOUGH MODULES ROLLBACK STEP RESTART BY IIM");
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
            }
            //20181126_tudn_end kiem tra module tac dong rollback

            app.setGroupAction(Constant.STEP_ROLLBACK_RESTART);
            rstObject.setModule(module);
            rstObject.setAction(selectedAction);
            rstObject.setDetailApp(app);
            rstObject.setActionModule(actionModule);
            rstObject.setCreateUser(username);
            rstObject.setRunStt(Constant.STAND_BY_STATUS);
            rstObject.setOrderIndex(app.getModuleOrder().intValue());

            if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                //05-11-2018 KienPD add failed step to rollback list start
                if ((history.getCurrStep() != null && history.getCurrStep().equals(AamConstants.ACTION.SUB_STEP_RESTART_APP))
                        || (app.getRunStatus() != null && app.getRunStatus().equals(-2))) {
                    mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    stepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    app.setGroupAction(Constant.STEP_RESTART);
                    rstObject.setDetailApp(app);
                    stepImpactFail.put(new MapEntry(AamConstants.ACTION.SUB_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), new Cloner().deepClone(rstObject));
                }
            if (app.getRunStatus() != null && (!(app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
                    && app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING
                    /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_start*/
                    && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER)) || (app.getIsAddRollback() != null && app.getIsAddRollback().equals(1))) {
                    /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_end*/
                rollbackObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
            }
            //05-11-2018 KienPD add failed step to rollback list end
//			rollbackRestartObjects.add(rstObject);
        }

        detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_RESTART_CMD, false, null, true);
        //20181126_tudn_start kiem tra module tac dong rollback
        if (!callService) {
            countModule = 0;
            if (detailApps.size() == 0) {
                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
            }
        }
        //20181126_tudn_end kiem tra module tac dong rollback
        for (ActionDetailApp app : detailApps) {
            /* kienpd_20180911_start */
			/*if (app.getRunStatus() == null || (app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
					&& app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER))
				continue;*/
            /* kienpd_20180911_end */


            actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
            rstObject = new ExeObject();
            //20181126_tudn_start kiem tra module tac dong rollback
            if (sufficientRollback) {
                //20181126_tudn_end kiem tra module tac dong rollback
                try {
                    module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
                } catch (AppException e) {
                    module= null;
                    logger.error(e.getMessage(), e);
                }
                if (module != null) {
                    appGroupnames.add(module.getServiceName());
                    //20181126_tudn_start kiem tra module tac dong rollback
                    if (!callService) {
                        countModule++;
                        logger.info("GET MODULE ROLLBACK: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP RESTART_CMD BY IIM");
                    }
                    //20181126_tudn_end kiem tra module tac dong rollback
                }
                //20181126_tudn_start kiem tra module tac dong rollback
                else {
                    if (!callService) {
                        if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                            executeTypeModule = Constant.EXE_ROLLBACK;
                            sufficientRollback = Boolean.FALSE;
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                            ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                            String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                            messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                            mapResultMess.put("sufficientRollback", messageModule);
                            logger.info("GET MODULE ROLLBACK FAIL: " + moduleCode + " STEP RESTART_CMD BY IIM");
                            if (repeat) {
                                RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                return;
                            }
                        }
                    }
                }
                if (!callService) {
                    if (countModule == detailApps.size()) {
                        logger.info("GET ENOUGH MODULES ROLLBACK STEP RESTART_CMD BY IIM");
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
            }
            //20181126_tudn_end kiem tra module tac dong rollback

            app.setGroupAction(Constant.STEP_ROLLBACK_RESTART_CMD);
            rstObject.setModule(module);
            rstObject.setAction(selectedAction);
            rstObject.setDetailApp(app);
            rstObject.setActionModule(actionModule);
            rstObject.setCreateUser(username);
            rstObject.setRunStt(Constant.STAND_BY_STATUS);
            rstObject.setOrderIndex(app.getModuleOrder().intValue());

            if ((selectedAction.getTestbedMode() && actionModule.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && actionModule.getTestbedMode() != 1))
                //05-11-2018 KienPD add failed step to rollback list start
                if ((history.getCurrStep() != null && history.getCurrStep().equals(AamConstants.ACTION.SUB_STEP_RESTART_APP))
                        || (app.getRunStatus() != null && app.getRunStatus().equals(-2))) {
                    mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    stepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                    app.setGroupAction(Constant.STEP_RESTART_CMD);
                    rstObject.setDetailApp(app);
                    stepImpactFail.put(new MapEntry(AamConstants.ACTION.SUB_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), new Cloner().deepClone(rstObject));
                }
            if (app.getRunStatus() != null && (!(app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
                    && app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING
                    /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_start*/
                    && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER)) || (app.getIsAddRollback() != null && app.getIsAddRollback().equals(1))) {
                    /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_end*/
                rollbackObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
                mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), rstObject);
            }
            //05-11-2018 KienPD add failed step to rollback list end
//			rollbackRestartObjects.add(rstObject);
        }

        List<ActionDetailDatabase> databases = actionDetailDatabaseService.findListDetailDb(selectedAction.getId(), null, false, true);
        //05-12-2018 KienPD check add step to rollback list start
        boolean check = true;
        for (ActionDetailDatabase database : databases) {
            if (database != null && database.getRunStatus() != null) {
                if (database.getRunStatus().intValue() == -2) {
                    check = false;
                    break;
                }
            }
        }
        //05-12-2018 KienPD check add step to rollback list end
        //20181126_tudn_start kiem tra module tac dong rollback
        int countDB = 0;
        if (!callService) {
            if (databases.size() == 0) {
                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
            }
        }
        //20181126_tudn_end kiem tra module tac dong rollback

        for (ActionDetailDatabase database : databases) {
            /* kienpd_20180911_start */
			/*if (database.getRunStatus() == null || (database.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
					&& database.getRunStatus() != Constant.FINISH_FAIL_STATUS  && database.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER))
				continue;*/
            /* kienpd_20180911_end */

            if ((database.getType().equals(0L) && ((database.getTypeImport().equals(1L) && StringUtils.isNotEmpty(database.getRollbackFile()))
                    || (database.getTypeImport().equals(0L) && StringUtils.isNotEmpty(database.getRollbackText()))))
                    || (database.getType().equals(1L) && StringUtils.isNotEmpty(database.getRollbackFile()))) {
                rstObject = new ExeObject();

                ServiceDatabase serviceDatabase = null;
                try {
                    serviceDatabase = iimService.findServiceDbById(selectedAction.getImpactProcess().getNationCode(), database.getAppDbId());

                    //20181126_tudn_start kiem tra module tac dong rollback
//				if (serviceDatabase == null){
//					continue;
                    if (serviceDatabase == null) {
                        if (!callService) {
                            if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                                executeTypeModule = Constant.EXE_ROLLBACK;
                                sufficientRollback = Boolean.FALSE;
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                String dbName = database.getServiceDatabase() != null && database.getServiceDatabase().getDbName() != null ? database.getServiceDatabase().getDbName() : String.valueOf(database.getAppDbId());
                                messageModule = String.format(MessageUtil.getResourceBundleMessage("get.database.count.sucess"), String.valueOf(countDB), String.valueOf(databases.size()), dbName);
                                mapResultMess.put("sufficientRollback", messageModule);
                                logger.info(messageModule);
                                if (repeat) {
                                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                    return;
                                }
                            }
                        } else {
                            continue;
                        }
                    } else {
                        countDB++;
                    }
                    if (!callService) {
                        if (countDB == databases.size()) {
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                        }
                    }
                    //20181126_tudn_end kiem tra module tac dong rollback

                    database.setServiceDatabase(serviceDatabase);

                    // rstObject.setApp(detail);
                    rstObject.setAction(selectedAction);
                    // rstObject.setDetailApp(app);
                    // rstObject.setActionModule(actionModule);
                    rstObject.setActionDatabase(database);
                    rstObject.setServiceDb(serviceDatabase);
                    rstObject.setCreateUser(username);
                    rstObject.setRunStt(Constant.STAND_BY_STATUS);
                    rstObject.setOrderIndex(database.getActionOrder().intValue());

                    rstObject.setActionDb(2);

                    if ((selectedAction.getTestbedMode() && database.getTestbedMode() == 1) || (!selectedAction.getTestbedMode() && database.getTestbedMode() != 1))
                        //05-11-2018 KienPD add failed step to rollback list start
                        if ((history.getCurrStep() != null && history.getCurrStep().equals(AamConstants.ACTION.SUB_STEP_TD_DB))
                                || (null != database.getRunStatus() && database.getRunStatus().equals(-2))) {
                            mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_DB, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                            stepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_DB, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                            stepImpactFail.put(new MapEntry(AamConstants.ACTION.SUB_STEP_TD_DB, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                        /*20181217_hoangnd_fix bug duplicate step fail_start*/
                        } else if (database.getRunStatus() != null) {
                            mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_DB, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                        }
                        /*20181217_hoangnd_fix bug duplicate step fail_end*/
                        /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_start*/
                    if ((check && database.getRunStatus() != null && !(database.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
                            //								&& database.getRunStatus() != Constant.FINISH_FAIL_STATUS
                            && database.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER))
                            || (database.getRunStatus() != null && database.getIsAddRollback() != null && database.getIsAddRollback().equals(1))) {
                        rollbackObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_DB, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                    }
                        /*20181214_hoangnd_them trang thai add buoc fail vao list rollback_end*/
                    /*20181217_hoangnd_fix bug duplicate step fail_start*/
                    /*if (database.getRunStatus() != null) {
                        mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_DB, database.getKbGroup() == null ? 1 : database.getKbGroup()), rstObject);
                    }*/
                    /*20181217_hoangnd_fix bug duplicate step fail_end*/
                    //05-11-2018 KienPD add failed step to rollback list end
//				rollbackDbObjects.add(rstObject);
                } catch (AppException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }

        /*20181117_hoangnd_save all step_start*/
        detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_CHECKLIST_APP, false, null, true);
        //20181126_tudn_start kiem tra module tac dong rollback
        /*countModule = 0;
        if (!callService) {
            if (detailApps.size() == 0) {
                if (!callService) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
        }*/
        //20181126_tudn_end kiem tra module tac dong rollback
        for (ActionDetailApp app : detailApps) {
//			actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
            rstObject = new ExeObject();
            //20181126_tudn_start kiem tra module tac dong rollback
//            if (sufficientRollback) {
            //20181126_tudn_end kiem tra module tac dong rollback
            try {
                module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
            } catch (AppException e) {
                module= null;
                logger.error(e.getMessage(), e);
            }
            if (module != null) {
                appGroupnames.add(module.getServiceName());
                //20181126_tudn_start kiem tra module tac dong rollback
                    /*if (!callService) {
                        countModule++;
                        logger.info("GET MODULE ROLLBACK: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP START BY IIM");
                    }*/
                //20181126_tudn_end kiem tra module tac dong rollback
            }
            //20181126_tudn_start kiem tra module tac dong rollback
                /*else {
                    if (!callService) {
                        if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                            executeTypeModule = Constant.EXE_ROLLBACK;
                            sufficientRollback = Boolean.FALSE;
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                            ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                            String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                            messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                            mapResultMess.put("sufficientRollback", messageModule);
                            logger.info("GET MODULE ROLLBACK FAIL: " + moduleCode + " STEP_CHECKLIST_APP BY IIM");
                            if (repeat) {
                                RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                return;
                            }
                        }
                    }
                }
                if (!callService) {
                    if (countModule == detailApps.size()) {
                        logger.info("GET ENOUGH MODULES ROLLBACK STEP_CHECKLIST_APP BY IIM");
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
            }*/
            //20181126_tudn_end kiem tra module tac dong rollback

            rstObject.setModule(module);
            rstObject.setAction(selectedAction);
            rstObject.setDetailApp(app);
//			rstObject.setActionModule(actionModule);
            rstObject.setCreateUser(username);
            rstObject.setRunStt(Constant.STAND_BY_STATUS);
            rstObject.setOrderIndex(app.getModuleOrder().intValue());

            /*20190125_hoangnd_luon add buoc rollback checklist_start*/
//            if (app.getRunStatus() != null && (!(app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
//                    && app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING
//                    && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER)) || (app.getIsAddRollback() != null && app.getIsAddRollback().equals(1))) {
            rollbackObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_APP, 1), rstObject);
            mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_APP, 1), rstObject);
            rollbackObjects.put(new MapEntry(AamConstants.ACTION.AFTER_STEP_CHECKLIST_APP, 1), rstObject);
//            }
            /*20190125_hoangnd_luon add buoc rollback checklist_end*/
        }

        detailApps = actionDetailAppService.findListDetailApp(selectedAction.getId(), Constant.STEP_CHECKLIST_DB, false, null, true);
        //20181126_tudn_start kiem tra module tac dong rollback
        /*countModule = 0;
        if (!callService) {
            if (detailApps.size() == 0) {
                if (!callService) {
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            }
        }*/
        //20181126_tudn_end kiem tra module tac dong rollback
        for (ActionDetailApp app : detailApps) {
//			actionModule = actionModuleService.findModule(selectedAction.getId(), app.getModuleId());
            rstObject = new ExeObject();
            //20181126_tudn_start kiem tra module tac dong rollback
//            if (sufficientRollback) {
            //20181126_tudn_end kiem tra module tac dong rollback
            try {
                module = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), app.getModuleId());
            } catch (AppException e) {
                module= null;
                logger.error(e.getMessage(), e);
            }
            if (module != null) {
                appGroupnames.add(module.getServiceName());
                //20181126_tudn_start kiem tra module tac dong rollback
                    /*if (!callService) {
                        countModule++;
                        logger.info("GET MODULE ROLLBACK: " + module.getModuleName() + " " + countModule + "/" + detailApps.size() + " STEP START BY IIM");
                    }*/
                //20181126_tudn_end kiem tra module tac dong rollback
            }
            //20181126_tudn_start kiem tra module tac dong rollback
                /*else {
                    if (!callService) {
                        if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && executeTypeModule != Constant.EXE_CHECKLIST) {
                            executeTypeModule = Constant.EXE_ROLLBACK;
                            sufficientRollback = Boolean.FALSE;
                            RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                            ActionModule actionMod = actionModuleService.findModule(app.getActionId(), app.getModuleId());
                            String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(app.getModuleId());
                            messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"), String.valueOf(countModule), String.valueOf(detailApps.size()), moduleCode);
                            mapResultMess.put("sufficientRollback", messageModule);
                            logger.info("GET MODULE ROLLBACK FAIL: " + moduleCode + " STEP_CHECKLIST_DB BY IIM");
                            if (repeat) {
                                RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                return;
                            }
                        }
                    }
                }
                if (!callService) {
                    if (countModule == detailApps.size()) {
                        logger.info("GET ENOUGH MODULES ROLLBACK STEP_CHECKLIST_DB BY IIM");
                        RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    }
                }
            }*/
            //20181126_tudn_end kiem tra module tac dong rollback

            rstObject.setModule(module);
            rstObject.setAction(selectedAction);
            rstObject.setDetailApp(app);
//			rstObject.setActionModule(actionModule);
            rstObject.setCreateUser(username);
            rstObject.setRunStt(Constant.STAND_BY_STATUS);
            rstObject.setOrderIndex(app.getModuleOrder().intValue());

            /*20190125_hoangnd_luon add buoc rollback checklist_start*/
//            if (app.getRunStatus() != null && (!(app.getRunStatus() != Constant.FINISH_SUCCESS_STATUS
//                    && app.getRunStatus() != Constant.FINISH_SUCCESS_WITH_WARNING
//                    && app.getRunStatus() != AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER)) || (app.getIsAddRollback() != null && app.getIsAddRollback().equals(1))) {
            rollbackObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_DB, 1), rstObject);
            mapStepFail.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_DB, 1), rstObject);
            rollbackObjects.put(new MapEntry(AamConstants.ACTION.AFTER_STEP_CHECKLIST_DB, 1), rstObject);
//            }
            /*20190125_hoangnd_luon add buoc rollback checklist_end*/
        }
        /*20181117_hoangnd_save all step_end*/

        List<SelectItem> oldRollback = new ArrayList<>();
        for (SelectItem runStep : runSteps) {
            if (((RunStep) runStep.getValue()).getValue().equals(new MapEntry(-4, 1)) || ((RunStep) runStep.getValue()).getValue().equals(new MapEntry(-3, 1))
                    || (((RunStep) runStep.getValue()).getValue().getKey() >= 200) && ((RunStep) runStep.getValue()).getValue().getKey() <= 400) {
//				check = true;
                oldRollback.add(runStep);
            }
        }

        runSteps.removeAll(oldRollback);

        if (selectedAction.getRunStatus() == null)
            return;

        MapEntry entryKey = new MapEntry(-3, 1);
        SelectItem item = new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel());
        item.setDisabled(true);
        runSteps.add(item);

        if (!executeChecklistController.getCklAppAfter().isEmpty()) {
            entryKey = new MapEntry(Constant.AFTER_STEP_CHECKLIST_APP, 1);
            runSteps.add(new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel()));
            if (stepResult.get(entryKey) == null)
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
        }
        if (!executeChecklistController.getCklDbAfter().isEmpty()) {
            entryKey = new MapEntry(Constant.AFTER_STEP_CHECKLIST_DB, 1);
            runSteps.add(new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel()));
            if (stepResult.get(entryKey) == null)
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
        }

        if (!checkVersionAppObjects.isEmpty()) {
            entryKey = new MapEntry(Constant.AFTER_STEP_CHECK_VERSION, 1);
            runSteps.add(new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel()));
            if (stepResult.get(entryKey) == null)
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
        }

        entryKey = new MapEntry(-4, 1);
        item = new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel());
        item.setDisabled(true);
        runSteps.remove(item);
        runSteps.add(item);

        List<Integer> rbGroups = (List<Integer>) ((ArrayList) kbGroups).clone();
        Collections.reverse(rbGroups);
        for (Integer rbGroup : kbGroups) {
            addRollbackCustomStep(Constant.ROLLBACK_CUSTOM_STEP_CHECK_STATUS, rbGroup);

            // 23-11-2018 KienPD add step check status start
            entryKey = new MapEntry(Constant.ROLLBACK_STEP_CHECK_STATUS, rbGroup);
            item = new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel());
            runSteps.remove(item);
            if (!rollbackObjects.get(entryKey).isEmpty()) {
                runSteps.add(item);
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }
            // 23-11-2018 KienPD add step check status end

            entryKey = new MapEntry(Constant.ROLLBACK_STEP_STOP_APP, rbGroup);
            item = new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel());
            runSteps.remove(item);
//		if (!rollbackStopObjects.isEmpty()) {
            if (!rollbackObjects.get(entryKey).isEmpty()) {
                runSteps.add(item);
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addRollbackCustomStep(Constant.ROLLBACK_CUSTOM_STEP_STOP_APP, rbGroup);

            entryKey = new MapEntry(Constant.ROLLBACK_STEP_SOURCE_CODE, rbGroup);
            item = new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel());
            runSteps.remove(item);
//		if (!rollbackCodeObjects.isEmpty()) {
            if (!rollbackObjects.get(entryKey).isEmpty()) {
                runSteps.add(item);
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addRollbackCustomStep(Constant.ROLLBACK_CUSTOM_STEP_UPCODE, rbGroup);

            entryKey = new MapEntry(Constant.ROLLBACK_STEP_DB, rbGroup);
            item = new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel());
            runSteps.remove(item);
//		if (!rollbackDbObjects.isEmpty()) {
            if (!rollbackObjects.get(entryKey).isEmpty()) {
                runSteps.add(item);
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addRollbackCustomStep(Constant.ROLLBACK_CUSTOM_STEP_TD_DB, rbGroup);

            entryKey = new MapEntry(Constant.ROLLBACK_STEP_CLEARCACHE, rbGroup);
            item = new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel());
            runSteps.remove(item);
//		if (!rollbackClearCacheObjects.isEmpty()) {
            if (!rollbackObjects.get(entryKey).isEmpty()) {
                runSteps.add(item);
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addRollbackCustomStep(Constant.ROLLBACK_CUSTOM_STEP_CLEARCACHE, rbGroup);

            entryKey = new MapEntry(Constant.ROLLBACK_STEP_RESTART_APP, rbGroup);
            item = new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel());
            runSteps.remove(item);
//		if (!rollbackRestartObjects.isEmpty()) {
            if (!rollbackObjects.get(entryKey).isEmpty()) {
                runSteps.add(item);
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addRollbackCustomStep(Constant.ROLLBACK_CUSTOM_STEP_RESTART_APP, rbGroup);

            entryKey = new MapEntry(Constant.ROLLBACK_STEP_START_APP, rbGroup);
            item = new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel());
            runSteps.remove(item);
//		if (!rollbackStartObjects.isEmpty()) {
            if (!rollbackObjects.get(entryKey).isEmpty()) {
                runSteps.add(item);
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            entryKey = new MapEntry(Constant.ROLLBACK_STEP_UPCODE_START, rbGroup);
            item = new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel());
            runSteps.remove(item);
//		if (!rollbackcodeStartObjects.isEmpty()) {
            if (!rollbackObjects.get(entryKey).isEmpty()) {
                runSteps.add(item);
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }

            addRollbackCustomStep(Constant.ROLLBACK_CUSTOM_STEP_START_APP, rbGroup);


            entryKey = new MapEntry(Constant.ROLLBACK_STEP_CHECKLIST_APP, rbGroup);
            item = new SelectItem(Constant.getRunStep(entryKey), Constant.getRunStep(entryKey).getLabel());
            runSteps.remove(item);
//		if (!rollbackStartObjects.isEmpty()) {
            if (executeChecklistController.getNewCklAppRollback().get(entryKey) != null && !executeChecklistController.getNewCklAppRollback().get(entryKey).isEmpty()) {
                runSteps.add(item);
                stepResult.put(entryKey, Constant.STAND_BY_STATUS);
            }
        }
		/*if (!executeChecklistController.getNewCklAppRollback().isEmpty()) {
			entryKey = new MapEntry(Constant.ROLLBACK_STEP_CHECKLIST_APP, 1);
			runSteps.add(new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel()));
			stepResult.put(entryKey, Constant.STAND_BY_STATUS);
		}*/
        if (!executeChecklistController.getCklDbRollback().isEmpty()) {
            entryKey = new MapEntry(Constant.ROLLBACK_STEP_CHECKLIST_DB, 1);
            runSteps.add(new SelectItem(Constant.getSteps().get(entryKey), Constant.getSteps().get(entryKey).getLabel()));
            stepResult.put(entryKey, Constant.STAND_BY_STATUS);
        }
    }

    public void pollListener() {
        long startTime = System.currentTimeMillis();
//		logger.info(startTime);
        RequestContext reqCtx = RequestContext.getCurrentInstance();

        ImpactProcess impactProcess = selectedAction.getImpactProcess();
        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("get").path(runId.toString());
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);

        // kienpd check process die start
        try {
            Response response = builder.get();
            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
                ChecklistInfo checklistInfo = response.readEntity(ChecklistInfo.class);
                if (checklistInfo == null) {
                    reqCtx.execute("PF('pollListener').stop()");
                    reqCtx.execute("PF('dlgFailProcess').show()");
                    return;
                } else {
//                    logger.info(System.currentTimeMillis() - startTime + "\t" + checklistInfo.getRunId());

                    executeChecklistController.setCklAppBefore(checklistInfo.getCklAppBefore());
                    executeChecklistController.setNewCklAppMain(checklistInfo.getNewCklAppMain());
                    executeChecklistController.setCklAppAfter(checklistInfo.getCklAppAfter());
                    executeChecklistController.setNewCklAppRollback(checklistInfo.getNewCklAppRollback());

                    executeChecklistController.setCklDbBefore(checklistInfo.getCklDbBefore());
                    executeChecklistController.setCklDbMain(checklistInfo.getCklDbMain());
                    executeChecklistController.setCklDbAfter(checklistInfo.getCklDbAfter());
                    executeChecklistController.setCklDbRollback(checklistInfo.getCklDbRollback());

//			selectedStep.setValue(checklistInfo.getCurrentStep().getValue());
                    selectedStep = checklistInfo.getCurrentStep();
                    stepResult = checklistInfo.getStepResult();

                    impactObjects = checklistInfo.getImpactObjects();
                    rollbackObjects = checklistInfo.getRollbackObjects();
                    customExeObjectMultimap = checklistInfo.getCustomExeObjectMultimap();
                    rollbackCustomExeObjectMultimap = checklistInfo.getRollbackCustomExeObjectMultimap();
                    waitingActions = checklistInfo.getWaitingActions();
                    history = checklistInfo.getHistory();
                    selectedAction = checklistInfo.getAction();
                    for (SelectItem lstAction : lstActions) {
                        Action action = (Action) lstAction.getValue();
                        if (action.getId().equals(selectedAction.getId())) {
                            action.setRunStatus(selectedAction.getRunStatus());
                        }
                    }

                    if (!dialogOpen && !waitingActions.isEmpty()) {
                        logger.info("---------------------------------");
                        dialogOpen = true;
                        dialogSize = waitingActions.size();
                        // anhnt2
                        loadRenderWaitTimeShutdown();
                        //reqCtx.update(":execute:waitingActions:waitTimeShutdown");

                        reqCtx.update("execute:waitingActions");
                        reqCtx.execute("PF('waitinguserconfirm').show()");
                        logger.info("---------------------------------");
                        // anhnt2
//				boolean isConfirmWhenLongTimeServerUp = false;
//				boolean isConfirmWhenServerUp = false;
//				for (ExeObject exeObject: waitingActions) {
//					if (exeObject.getOptionWhenTimeOut() != null && exeObject.getOptionWhenTimeOut() == 0) {
//						reqCtx.execute("PF('confirmWhenLongTimeServerUp').show()");
//						isConfirmWhenLongTimeServerUp = true;
//						break;
//					}
//					if (exeObject.getConfirmWhenServerUp() != null && exeObject.getConfirmWhenServerUp() == 0) {
//						reqCtx.execute("PF('confirmWhenServerUp').show()");
//						isConfirmWhenServerUp = true;
//						break;
//					}
//				}
//				if (!isConfirmWhenLongTimeServerUp && !isConfirmWhenServerUp) {
//					logger.info("---------------------------------");
//					dialogOpen = true;
//					dialogSize = waitingActions.size();
//					reqCtx.update("execute:waitingActions");
//					reqCtx.execute("PF('waitinguserconfirm').show()");
//					logger.info("---------------------------------");
//				}
                    } else if (dialogSize != waitingActions.size()) {
                        dialogSize = waitingActions.size();
                        reqCtx.update("execute:scroll");
                    }

                    if (checklistInfo.getEndTime() != null) {
//				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("check.list.app.complete") + " " + actionController.getNewObj().getCrNumber(), "");
//				FacesContext.getCurrentInstance().addMessage("verifyGrowl", msg);
                        if (history.getStartRollbackTime() == null) {
                            //20181126_tudn_start kiem tra module tac dong rollback
                            clickButtonRollback = Boolean.FALSE;
                            repeat = Boolean.FALSE;
                            if (executeTypeModule != Constant.EXE_CHECKLIST && executeTypeModule != Constant.EXE_CHECKLIST_LAST) {
                                if(checkBuildRb) {
                                    checkBuildRb = false;
                                    logger.info("---Build LoadRollback---");
                                    loadRollback(false); //true la webservice goi, false la webservice khong goi
                                }
                            }
                        }
                        isRunning = Boolean.FALSE;
                        reqCtx.execute("PF('pollListener').stop()");

                        //20181126_tudn_start kiem tra module tac dong rollback
                        if (executeTypeModule != Constant.EXE_CHECKLIST_LAST && history.getRollbackStatus() == null) {
                            if (!sufficientRollback) {
                                if (repeat) {
                                    messageModule = mapResultMess.get("sufficientRollback");
                                } else {
                                    messageModule = MessageUtil.getResourceBundleMessage("get.module.count.message");
                                }
                                RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                                RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                                return;
                            }
                        }
                        //20181126_tudn_end kiem tra module tac dong rollback
                        //reqCtx.update(":mop:lst:verifyGrowl");
                        //reqCtx.update("mop:lst:tabVerify:verifyAppMsg");
                    }
                }
            } else {
                logger.error(response.getStatus());
            }

        } catch (Exception e) {
            logger.info("Start retrying...");
            logger.error(e.getMessage(), e);
            cntRetry++;
            while (cntRetry == numberRetry) {
                logger.info("End retrying...");
                reqCtx.execute("PF('pollListener').stop()");
                reqCtx.execute("PF('dlgFailProcess').show()");
                return;
            }
            try {
                Thread.sleep(timeRetry * 1000);
            } catch (InterruptedException ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
        // kienpd check process die end
    }


    //20181126_tudn_start kiem tra module tac dong rollback
    public void reloadModule() {

        RequestContext.getCurrentInstance().execute("PF('confirmCountModule').hide()");
        repeat = Boolean.TRUE;
        isReload = Boolean.TRUE;
        if (executeTypeModule == Constant.EXE_ROLLBACK) {
            loadRollback(false); //true la webservice goi, false la webservice khong goi
            //start them vao de load lai checklist
            if (checkProcessRunning() != null) {
                ChecklistInfo checklistInfo = checkProcessRunning();
                if (checklistInfo != null) {
                    customExeObjectMultimap = checklistInfo.getCustomExeObjectMultimap();
                    impactObjects = checklistInfo.getImpactObjects();
                    rollbackObjects = checklistInfo.getRollbackObjects();

                    rollbackCustomExeObjectMultimap = checklistInfo.getRollbackCustomExeObjectMultimap();

                    executeChecklistController.setCklAppBefore(checklistInfo.getCklAppBefore());
                    executeChecklistController.setNewCklAppMain(checklistInfo.getNewCklAppMain());
                    executeChecklistController.setCklAppAfter(checklistInfo.getCklAppAfter());
                    executeChecklistController.setNewCklAppRollback(checklistInfo.getNewCklAppRollback());

                    executeChecklistController.setCklDbBefore(checklistInfo.getCklDbBefore());
                    executeChecklistController.setCklDbMain(checklistInfo.getCklDbMain());
                    executeChecklistController.setCklDbAfter(checklistInfo.getCklDbAfter());
                    executeChecklistController.setCklDbRollback(checklistInfo.getCklDbRollback());

                    selectedAction = checklistInfo.getAction();
                    selectedStep = checklistInfo.getCurrentStep();
                    stepResult = checklistInfo.getStepResult();
                    history = checklistInfo.getHistory();
                    waitingActions = checklistInfo.getWaitingActions();
                    runId = checklistInfo.getRunId();

                    this.infoList = new ArrayList<>();
                }
            }
            //end them vao de load lai checklist

            if (!sufficientRollback) {
                repeat = Boolean.TRUE;
                if (!sufficientRollback) {
                    messageModule = mapResultMess.get("sufficientRollback");
                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    return;
                }
            } else {
                if (clickButtonRollback) {
                    execute(Constant.EXE_ROLLBACK);
                }
            }
        } else {
            reload(false); //true la webservice goi, false la webservice khong goi
            //start them vao de load lai checklist
            executeChecklistController.reload();
            //end them vao de load lai checklist

            if (!sufficientImpact) {
                repeat = Boolean.TRUE;
                if (!sufficientImpact) {
                    messageModule = mapResultMess.get("sufficientImpact");
                    RequestContext.getCurrentInstance().execute("PF('confirmCountModule').show()");
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                    return;
                }
            } else {
                if (clickButtonImpact) {
                    execute(Constant.EXE_TD);
                }
            }
        }
    }

    public void closeModule() {
        RequestContext.getCurrentInstance().execute("PF('confirmCountModule').hide()");
    }
    //20181126_tudn_end kiem tra module tac dong rollback

    /*20181123_hoangnd_save all step_start*/
    public void exeLogDetail(ExeObject exeObject) {
        /*20190131_hoangnd_check null_start*/
        if (exeObject != null) {
            logDetail = StringUtils.isNotBlank(exeObject.getLog()) ? exeObject.getLog().toString() : "";
            if (StringUtils.isBlank(logDetail)) {
                viewLogDetail(exeObject);
            }
            if (StringUtils.isBlank(logDetail) && exeObject.getActionDatabase() != null
                    && StringUtils.isNotBlank(exeObject.getActionDatabase().getTempFile())) {
                viewTempFile(exeObject);
            }
        } else {
            logDetail = "";
        }
        /*20190131_hoangnd_check null_end*/
    }
    /*20181123_hoangnd_save all step_end*/

    /*20181023_hoangnd_continue fail step_start*/
    public void viewLogDetail(ExeObject exeObject) {
        logDetail = "";
        try {
            File dir = new File(UploadFileUtils.getLogFolder(exeObject.getAction()));
            String logFile = null;
            if (exeObject.getDetailApp() != null) {
                switch (exeObject.getDetailApp().getGroupAction()) {
                    case Constant.STEP_CHECK_STATUS:
                        logFile = "tacdong" + File.separator + "status";
                        break;
                    //11-12-2018 KienPD start
                    case Constant.STEP_ROLLBACK_CHECK_STATUS:
                        logFile = "rollback" + File.separator + "status";
                        break;
                    //11-12-2018 KienPD end
                    case Constant.STEP_STOP:
                        logFile = "tacdong" + File.separator + "stop";
                        break;
                    case Constant.STEP_ROLLBACK_STOP:
                        logFile = "rollback" + File.separator + "stop";
                        break;
                    case Constant.STEP_BACKUP:
                        logFile = "tacdong" + File.separator + "backup";
                        break;
                    case Constant.STEP_UPCODE:
                        logFile = "tacdong" + File.separator + "upcode";
                        break;
                    case Constant.STEP_CLEARCACHE:
                        logFile = "tacdong" + File.separator + "clearcache";
                        break;
                    case Constant.STEP_ROLLBACK_CLEARCACHE:
                        logFile = "rollback" + File.separator + "clearcache";
                        break;
                    case Constant.STEP_RESTART:
                        logFile = "tacdong" + File.separator + "restart";
                        break;
                    case Constant.STEP_ROLLBACK_RESTART:
                        logFile = "rollback" + File.separator + "restart";
                        break;
                    case Constant.STEP_RESTART_CMD:
                        logFile = "tacdong" + File.separator + "restart";
                        break;
                    case Constant.STEP_ROLLBACK_RESTART_CMD:
                        logFile = "rollback" + File.separator + "restart";
                        break;
                    case Constant.STEP_START:
                        logFile = "tacdong" + File.separator + "start";
                        break;
                    case Constant.STEP_ROLLBACK_START:
                        logFile = "rollback" + File.separator + "start";
                        break;
                    case Constant.STEP_ROLLBACKCODE:
                        logFile = "rollback" + File.separator + "code";
                        break;
                    case Constant.STEP_CHECKVERSION_APP:
                        logFile = "sautacdong" + File.separator + "checkversion";
                        break;
                    /*20181225_hoangnd_them buoc upcode special_start*/
                    case Constant.STEP_UPCODE_STOP_START:
                        logFile = "tacdong" + File.separator + "upcode_stop_start";
                        break;
                    case Constant.STEP_ROLLBACK_CODE_STOP_START:
                        logFile = "rollback" + File.separator + "code_stop_start";
                        break;
                    /*20181225_hoangnd_them buoc upcode special_end*/
                    default:
                        break;
                }

                if (Arrays.asList(Constant.STEP_ROLLBACKCODE, Constant.STEP_BACKUP, Constant.STEP_UPCODE, Constant.STEP_CHECKVERSION_APP).contains(exeObject.getDetailApp().getGroupAction())) {
                    logFile += File.separator + exeObject.getActionModule().getInstalledUser() + "_" + exeObject.getActionModule().getIpServer() + "_" + exeObject.getActionModule().getAppCode() + "_" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\./", "").replaceAll("/", "_") + ".log";
                } else {
                    logFile += File.separator + exeObject.getActionModule().getInstalledUser() + "_" + exeObject.getActionModule().getIpServer() + "_" + exeObject.getActionModule().getAppCode() + ".log";
                }
            } else if (exeObject.getActionDatabase() != null) {
                if (exeObject.getActionDb() == 0) {
                    logFile = "tacdong" + File.separator + "backupdb";
                } else if (exeObject.getActionDb() == 1) {
                    logFile = "tacdong" + File.separator + "tddb";
                } else if (exeObject.getActionDb() == 2) {
                    logFile = "rollback" + File.separator + "rollbackdb";
                }

                logFile += File.separator + exeObject.getServiceDb().getUsername() + "_" + exeObject.getServiceDb().getDbName() + "_" + exeObject.getServiceDb().getIpVirtual() + "_" + exeObject.getActionDatabase().getId() + ".log";
            }

            /*20190124_hoangnd_view log tac dong sau khi chay xong_start*/

			/*20190218_hoangnd_fix bug view log file_start*/
            File dirFile = new File(dir + File.separator + logFile);
            if(!dirFile.exists()) {
			/*20190218_hoangnd_fix bug view log file_end*/
                logger.info("Not found " + dir);
                dir = new File(UploadFileUtils.getLogFolder(exeObject.getAction()) + ".zip");
                if(!dir.exists()) {
                    logger.info("Not found " + dir);
                    com.viettel.it.util.MessageUtil.setErrorMessageFromRes("not.found.folder.contains.file");
                    return;
                }

                logger.info("Found " + dir);
                ZipFile zipFile = new ZipFile(dir);
                FileHeader fileHeader = zipFile.getFileHeader(logFile);

                if (fileHeader != null) {
                    InputStream inputStream = zipFile.getInputStream(fileHeader);
//				StreamedContent dFile = new DefaultStreamedContent(inputStream, "", FilenameUtils.getName(logFile));
                    logDetail = IOUtils.toString(inputStream);

                    if (inputStream != null) {
                        inputStream.close();
                    }
                }
                return;
            }

            logger.info("Start open file : " + dir + File.separator + logFile);
            InputStream inputStream = new FileInputStream(dir + File.separator + logFile);
            logDetail = IOUtils.toString(inputStream);

            if (inputStream != null) {
                inputStream.close();
            }
            logger.info("End open file : " + dir + File.separator + logFile);
            return;
            /*20190124_hoangnd_view log tac dong sau khi chay xong_end*/
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void viewTempFile(ExeObject exeObject) {
        logDetail = "";
        try {
            File dir = new File(exeObject.getActionDatabase().getTempFile());

            if (!dir.exists()) {
                com.viettel.it.util.MessageUtil.setErrorMessageFromRes("not.found.folder.contains.file");
                return;
            }

            logger.info("open file : " + dir);
            InputStream inputStream = new FileInputStream(dir);
            logDetail = IOUtils.toString(inputStream);

            if (inputStream != null) {
                inputStream.close();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    /*20181023_hoangnd_continue fail step_end*/

    public void handleChangeSelectOneMenu(final AjaxBehaviorEvent event) {
        for (ExeObject exeObject : waitingActions) {
            // Only for case shutdown
            if (exeObject.getAction() != null && exeObject.getAction().getActionRbSd().equals(2l)) {
//				if (!exeObject.getWaitDescription().toLowerCase().contains(MessageUtil.getResourceBundleMessage("popup.wait.description.user.confirm"))) {
//					renderWaitTimeShutdown = false;
//					break;
//				}
                // [Screen confirm] When select option is continue -> render input wait time out
                if (exeObject.getUserConfirmForShutdown() != null && exeObject.getUserConfirmForShutdown() == AamConstants.waitActionContinue) {
                    renderWaitTimeShutdown = true;
                } else {
                    renderWaitTimeShutdown = false;
                    exeObject.setOptionWhenTimeOut(null);
                }
            }
        }
//		RequestContext reqCtx = RequestContext.getCurrentInstance();
//		reqCtx.update("execute:waitingActions");
    }

    public void handleChangeConfirmWhenTimeOut(final AjaxBehaviorEvent event) {
        for (ExeObject exeObject : waitingActions) {
            // Only for case shutdown
            if (exeObject.getAction() != null && exeObject.getAction().getActionRbSd().equals(2l)) {
//				if (!exeObject.getWaitDescription().toLowerCase().contains(MessageUtil.getResourceBundleMessage("popup.wait.description.user.confirm"))) {
//					renderWaitTimeShutdown = false;
//					break;
//				}
                // [Screen time out] When select option is retry -> render input wait time out
                if (exeObject.getWaitAction() != null && exeObject.getWaitAction() == AamConstants.waitActionScreenTimeOut
                        && exeObject.getOptionWhenTimeOut() != null) {
                    if (exeObject.getOptionWhenTimeOut() == AamConstants.confirm || exeObject.getOptionWhenTimeOut() == AamConstants.crFail) {
                        renderWaitTimeShutdown = false;
                    } else {
                        renderWaitTimeShutdown = true;
                    }
                }
            }
        }
    }

    private void loadRenderWaitTimeShutdown() {
        if (waitingActions != null && waitingActions.size() > 0) {
            for (ExeObject exeObject : waitingActions) {
                // Only for case shutdown
                if (exeObject.getAction() != null && exeObject.getAction().getActionRbSd().equals(2l)) {
                    if (exeObject.isRunCaseRebootShutdown()) {
                        renderWaitTimeShutdown = true;
                        if (exeObject.getUserConfirmForShutdown() == null
                                || exeObject.getUserConfirmForShutdown() == AamConstants.confirm) {
                            renderWaitTimeShutdown = false;
                        }
                    }
//					if (exeObject.getWaitAction() != null
//							&& exeObject.getWaitAction() == AamConstants.waitActionContinue) {
//						renderWaitTimeShutdown = true;
//					}
//					else {
//						renderWaitTimeShutdown = false;
//						exeObject.setWaitTimeShutdown(null);
//						RequestContext reqCtx = RequestContext.getCurrentInstance();
//						//reqCtx.update(":execute:waitingActions:waitTimeShutdown");
//						reqCtx.update("execute:waitingActions");
//					}
                    // [Screen time out] When select option is retry -> render input wait time out
                    if (exeObject.getWaitAction() != null && exeObject.getWaitAction() == AamConstants.waitActionScreenTimeOut
                            && exeObject.getOptionWhenTimeOut() != null) {
                        if (exeObject.getOptionWhenTimeOut() == AamConstants.confirm || exeObject.getOptionWhenTimeOut() == AamConstants.crFail) {
                            renderWaitTimeShutdown = false;
                        } else {
                            renderWaitTimeShutdown = true;
                        }
                    }
                    if (exeObject.getWaitAction() != null && exeObject.getWaitAction() == AamConstants.waitActionScreenTimeIn) {
                        renderWaitTimeShutdown = false;
                    }
                }
            }
        }
    }

    public void exeScriptDetail(ExeObject exeObject, Integer type) {
        switch (type) {
            case 0:
                scriptDetail = exeObject.getActionDatabase().getBackupText();
                break;
            case 1:
                scriptDetail = exeObject.getActionDatabase().getScriptText();
                break;
            case 2:
                scriptDetail = exeObject.getActionDatabase().getRollbackText();
                break;
            default:
                scriptDetail = "";
                break;
        }
    }

    public StreamedContent downloadFile(String fileName) {
        StreamedContent fileInput = null;
        String filePath = UploadFileUtils.getDatabaseFolder(selectedAction) + File.separator + fileName;

        try {
            fileInput = new DefaultStreamedContent(new FileInputStream(filePath), "", fileName);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return fileInput;
    }

    public StreamedContent downloadDataFile(String fileName) {
        StreamedContent fileInput = null;

        String filePath = UploadFileUtils.getDataImportFolder(selectedAction) + File.separator + fileName;
        try (InputStream stream = new FileInputStream(filePath)) {

            fileInput = new DefaultStreamedContent(stream, "", fileName);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return fileInput;
    }


    public String getLogDetail() {
        return logDetail;
    }

    public String cutLog(String defaultLog) {
        if (defaultLog.length() > 600) {
            return "..." + defaultLog.substring(defaultLog.length() - 600);
        } else {
            return defaultLog;
        }

    }

    /*20190219_hoangnd_fix bug view log impact khi chay rollback_start*/
    public String cutLog(ExeObject exeObject) {
        String defaultLog = exeObject != null && StringUtils.isNotBlank(exeObject.getLog()) ? exeObject.getLog().toString() : "";
        if (defaultLog.length() > 600) {
            return "..." + defaultLog.substring(defaultLog.length() - 600);
        } else {
            exeLogDetail(exeObject);
            if (logDetail.length() > 600) {
                return "..." + logDetail.substring(logDetail.length() - 600);
            } else {
                return logDetail;
            }
        }
    }
    /*20190219_hoangnd_fix bug view log impact khi chay rollback_end*/

    // longlt6 add 2016 - 10 - 12

    private String createInfo(String label, List<ExeObject> listObj) {
        int successCount = 0;
        Date beginDate = new Date();
        boolean start = false;
        for (ExeObject o : listObj) {
            if (o.getRunStt() == Constant.FINISH_SUCCESS_STATUS || o.getRunStt() == Constant.FINISH_SUCCESS_WITH_WARNING) {
                successCount++;
            }
            if (o.getBeginDate() != null) {
                start = true;
                if (o.getBeginDate().compareTo(beginDate) < 0) {
                    beginDate = o.getBeginDate();
                }
            }

        }

        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");

        if (start) {
            label += " ( ".concat(String.valueOf(successCount)).concat("/").concat(String.valueOf(listObj.size())).concat(" - ").concat(sdf.format(beginDate)).concat(" )");
        } else {
            label += " ( ".concat(String.valueOf(listObj.size())).concat(" )");
        }

        return label;
    }

    public String getActionInfo(RunStep step) {

        try {
            String newLabel = step.getLabel();
            if (impactObjects.get(step.getValue()) != null && !impactObjects.get(step.getValue()).isEmpty()) {
                newLabel = this.createInfo(newLabel, new ArrayList<>(impactObjects.get(step.getValue())));
            } else if (rollbackObjects.get(step.getValue()) != null && !rollbackObjects.get(step.getValue()).isEmpty()) {
                newLabel = this.createInfo(newLabel, new ArrayList<>(rollbackObjects.get(step.getValue())));
            } else {
                if ((int) step.getValue().getKey() >= 121 && (int) step.getValue().getKey() <= 129) {
                    if (!this.customExeObjectMultimap.get(step.getValue()).isEmpty())
                        newLabel = this.createInfo(newLabel, new ArrayList<>(this.customExeObjectMultimap.get(step.getValue())));
                } else if ((int) step.getValue().getKey() >= 321 && (int) step.getValue().getKey() <= 329) {
                    if (!this.rollbackCustomExeObjectMultimap.get(step.getValue()).isEmpty())
                        newLabel = this.createInfo(newLabel, new ArrayList<>(this.rollbackCustomExeObjectMultimap.get(step.getValue())));
                } else {
                    return step.getLabel();
                }
            }
            return newLabel;

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return step.getLabel();
    }

    public void userConfirm(ExeObject exeObject) {
        logger.info(exeObject.getRunStt() + "\t" + runStatus);
        waitingActions.remove(exeObject);
        if (waitingActions.isEmpty()) {
            RequestContext.getCurrentInstance().execute("PF('waitinguserconfirm').hide()");
            dialogOpen = false;
            dialogSize = 0;
        }
    }

    private void sort(List<ExeObject> list) {
        if (list.size() > 0) {
            Collections.sort(list, (object1, object2) -> (object1.getOrderIndex() - object2.getOrderIndex()));
        }
    }

    private String getStatusName(int status) {
        if (Constant.getStatusMap().containsKey(status)) {
            return Constant.getStatusMap().get(status);
        }
        return "Unknown";
    }

    private void addActionInfo(HashMap<Long, ExcecuteInfoObj> hmData, String actionName, List<ExeObject> listObj) {
        if (listObj != null) {
            this.sort(listObj);
            for (ExeObject obj : listObj) {

                Module module = obj.getModule();
                if (module == null)
                    continue;

                StringBuilder info = new StringBuilder();
                info.append("\r\n**********************\r\n".concat(MessageUtil.getResourceBundleMessage("common.action") + ": " + actionName));
                if (obj.getBeginDate() != null) {
                    info.append("\r\n" + MessageUtil.getResourceBundleMessage("common.start.time") + ": " + this.dateFormat.format(obj.getBeginDate()));
                }
                if (obj.getEndDate() != null) {
                    info.append("\r\n" + MessageUtil.getResourceBundleMessage("common.end.time") + ": " + this.dateFormat.format(obj.getEndDate()));
                }
                info.append("\r\nTráº¡ng thÃ¡i " + this.getStatusName(obj.getRunStt()));
                if (hmData.containsKey(module.getModuleId())) {
                    hmData.get(module.getModuleId()).appendLog(info.toString());
                } else {

                    ExcecuteInfoObj excecuteInfoObj = new ExcecuteInfoObj();
                    excecuteInfoObj.setAppId(module.getModuleId());
                    excecuteInfoObj.setAppCode(module.getModuleCode());
                    excecuteInfoObj.setAppName(module.getModuleName());
                    excecuteInfoObj.setAppGroupCode(module.getServiceCode());
                    excecuteInfoObj.setAppGroupName(module.getServiceName());
                    excecuteInfoObj.appendLog(info.toString());
                    hmData.put(module.getModuleId(), excecuteInfoObj);
                }
            }
        }
    }

    public StreamedContent downloadExportFile(ExeObject exeObject) {
        String uploadFolder = UploadFileUtils.getDataExportFolder(exeObject.getAction());

        StreamedContent fileInput = null;
        String filePath = uploadFolder + File.separator + "export_" + exeObject.getCustomAction().getId() + ".zip";
        try {
            fileInput = new DefaultStreamedContent(new FileInputStream(filePath), "", "export.zip");
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return fileInput;
    }

    public StreamedContent downloadHdFile(ExeObject exeObject) {
        String uploadFolder = UploadFileUtils.getImpactFileFolder();

        StreamedContent fileInput = null;
        String filePath = uploadFolder + File.separator + exeObject.getActionDtFile().getLocalFilename();

        try {
            fileInput = new DefaultStreamedContent(new FileInputStream(filePath), "", exeObject.getActionDtFile().getImpactFile());
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return fileInput;
    }

    public void handleFileUploadResult(FileUploadEvent event) {
        ExeObject exeObject = (ExeObject) event.getComponent().getAttributes().get("exeObject");
        System.out.println(exeObject);
    }

    public void submitConfirm() {
        for (ExeObject waitingAction : waitingActions) {
            // 22-11-2018 KienPD confirm service dead start
            if (StringUtils.isEmpty(waitingAction.getUserComment()) && waitingAction.getWaitAction() != 20) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("execute.comment.do.not.enter"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                RequestContext reqCtx = RequestContext.getCurrentInstance();
                reqCtx.update("insertEditForm:designGrowl");
                return;
            }
            // 22-11-2018 KienPD confirm service dead end
            // Only for case shutdown
            if (waitingAction.getAction() != null && waitingAction.getAction().getActionRbSd().equals(2l)) {
                if ((waitingAction.getUserConfirmForShutdown() != null && waitingAction.getUserConfirmForShutdown() == AamConstants.confirm)
                        || (waitingAction.getOptionWhenTimeOut() != null && waitingAction.getOptionWhenTimeOut() == AamConstants.confirm)) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("execute.comment.do.not.enter"), "");
                    FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                    RequestContext reqCtx = RequestContext.getCurrentInstance();
                    reqCtx.update("insertEditForm:designGrowl");
                    return;
                }
                if (waitingAction.getWaitAction() != null
                        && (
                        waitingAction.getWaitAction() == AamConstants.userConfirmShutdown
                                || (
                                waitingAction.getWaitAction() == AamConstants.waitActionScreenTimeOut
                                        && waitingAction.getOptionWhenTimeOut() != null && waitingAction.getOptionWhenTimeOut() == AamConstants.waitActionRetryOk
                        )
                )
                        && StringUtils.isNotEmpty(waitingAction.getWaitTimeShutdown()) && StringUtils.isNumeric(waitingAction.getWaitTimeShutdown()) && Integer.valueOf(waitingAction.getWaitTimeShutdown()) < AamConstants.defaultOptionWhenTimeOut) {

                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(MessageUtil.getResourceBundleMessage("execute.option.when.time.out"), AamConstants.defaultOptionWhenTimeOut), "");
                    FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                    RequestContext reqCtx = RequestContext.getCurrentInstance();
                    reqCtx.update("insertEditForm:designGrowl");
                    return;
                }

                if (waitingAction.isRunCaseRebootShutdown()) {
                    renderWaitTimeShutdown = true;
                }

                if (waitingAction.getWaitAction() != null
                        && (waitingAction.getWaitAction() == AamConstants.userConfirmShutdown || waitingAction.getWaitAction() == AamConstants.waitActionScreenTimeOut)) {
                    //anhnt2
                    renderWaitTimeShutdown = true;
                } else {
                    renderWaitTimeShutdown = false;
                }

//				for (ExeObject exeObject: waitingActions) {
//					if (exeObject.getOptionWhenTimeOut() != null && exeObject.getOptionWhenTimeOut() == 0) {
//						exeObject.setOptionWhenTimeOut(exeObject.getWaitAction());
//						break;
//					}
//					if (exeObject.getConfirmWhenServerUp() != null && exeObject.getConfirmWhenServerUp() == 0) {
//						exeObject.setConfirmWhenServerUp(exeObject.getWaitAction());
//						break;
//					}
//				}
            }
//			if (StringUtils.isEmpty(waitingAction.getUserComment()) &&
//					waitingAction.getOptionWhenTimeOut() != null && waitingAction.getOptionWhenTimeOut() != 0
//					&& (waitingAction.getConfirmWhenServerUp() == null || waitingAction.getConfirmWhenServerUp() != 0)) {
//				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("execute.comment.do.not.enter"), "");
//				FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
//				RequestContext reqCtx = RequestContext.getCurrentInstance();
//				reqCtx.update("insertEditForm:designGrowl");
//				return;
//			}

//			int iActionContinue = AamConstants.waitActionContinue;
//			if (waitingAction.getOptionWhenTimeOut() != null && waitingAction.getOptionWhenTimeOut() == 0) {
//				// When "[Shutdown] - Stop wait when time more than xx'" then cr fail =1, retry = 2 -> WaitAction = 1 or 2 -> check WaitTimeShutdown only click retry -> value is 2
//				iActionContinue = AamConstants.waitActionCancel;
//			}
//			if (waitingAction.getWaitAction() != null && waitingAction.getWaitAction() == iActionContinue
//					&& waitingAction.getWaitTimeShutdown() != null && Integer.valueOf(waitingAction.getWaitTimeShutdown()) < AamConstants.defaultOptionWhenTimeOut
//					&& (waitingAction.getConfirmWhenServerUp() == null || waitingAction.getConfirmWhenServerUp() != 0)) {
//				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, String.format(MessageUtil.getResourceBundleMessage("execute.option.when.time.out"), AamConstants.defaultOptionWhenTimeOut), "");
//				FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
//				RequestContext reqCtx = RequestContext.getCurrentInstance();
//				reqCtx.update("insertEditForm:designGrowl");
//				return;
//			}
//			if (waitingAction.getWaitTimeShutdown() == null
//					&& waitingAction.getConfirmWhenServerUp() != null && waitingAction.getConfirmWhenServerUp() == 2) {
//				//anhnt2
//				renderWaitTimeShutdown = false;
//			}
        }

        ImpactProcess impactProcess = selectedAction.getImpactProcess();
        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("rmActionWaiting").queryParam("long", runId);
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        ChecklistInfo checklistInfo = new ChecklistInfo();
        checklistInfo.setWaitingActions(waitingActions);
        Response response = builder.post(Entity.json(checklistInfo));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
//			waitingActions = response.readEntity(new GenericType<List<ExeObject>>() {});
            waitingActions = response.readEntity(ChecklistInfo.class).getWaitingActions();
        }

        if (waitingActions.isEmpty()) {
            RequestContext.getCurrentInstance().execute("PF('waitinguserconfirm').hide()");
            dialogOpen = false;
            dialogSize = 0;
        }
    }

    public List<ExeObject> findCustomObject(Integer step) {
        List<ExeObject> exeObjects = new ArrayList<>(customExeObjectMultimap.get(new MapEntry(step, 1)));

        return exeObjects;
    }

    public List<ExeObject> findRollbackCustomObject(Integer step) {
        List<ExeObject> exeObjects = new ArrayList<>(rollbackCustomExeObjectMultimap.get(new MapEntry(step, 1)));

        return exeObjects;
    }

    public boolean render(String step) {
        return true;
    }

    public void actionSelectionChanged() {
        logger.info(selectedAction.getCrNumber());
    }

    public List<SelectItem> getRunSteps() {
        return runSteps;
    }

    public void setRunSteps(List<SelectItem> runSteps) {
        this.runSteps = runSteps;
    }

    public List<SelectItem> getLstActions() {
        return lstActions;
    }

    public void setLstActions(List<SelectItem> lstActions) {
        this.lstActions = lstActions;
    }

    public Action getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(Action selectedAction) {
        this.selectedAction = selectedAction;
    }

    public MapEntry getSelectedStep() {
//		return selectedStep.getValue();
        return selectedStep;
    }

    public void setSelectedStep(Integer selectedStep) {
        this.selectedStep.setValue(selectedStep);
    }

//	public List<ExeObject> getStopObjects() {
//		return stopObjects;
//	}

    public List<ExeObject> getLstImpactObjects() {
        if (selectedRunStep == null)
            return new ArrayList<>();
        else {
            MapEntry entryKey = selectedRunStep.getValue();
            Collection<ExeObject> exeObjects = impactObjects.get(entryKey);
            if (!exeObjects.isEmpty())
                return new ArrayList<>(exeObjects);

            exeObjects = rollbackObjects.get(entryKey);
            if (!exeObjects.isEmpty())
                return new ArrayList<>(exeObjects);

            exeObjects = customExeObjectMultimap.get(entryKey);
            if (!exeObjects.isEmpty())
                return new ArrayList<>(exeObjects);

            exeObjects = rollbackCustomExeObjectMultimap.get(entryKey);
            if (!exeObjects.isEmpty())
                return new ArrayList<>(exeObjects);
        }

        return new ArrayList<>();
    }

    public boolean checkRender(int step) {
        if (this.selectedRunStep != null && (int) this.selectedRunStep.getValue().getKey() == step)
            return true;
        return false;
    }

    public boolean checkImpactRender() {
        if (this.selectedRunStep != null && (int) this.selectedRunStep.getValue().getKey() >= 100 &&
                !Arrays.asList(AamConstants.ACTION.AFTER_STEP_CHECKLIST_APP, AamConstants.ACTION.AFTER_STEP_CHECKLIST_DB,
                        AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_APP, AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_DB,
                        AamConstants.ACTION.SUB_STEP_CHECKLIST_APP, AamConstants.ACTION.SUB_STEP_CHECKLIST_DB).contains(this.selectedRunStep.getValue().getKey())
            && !checkImpactDbRender()){
            return true;
        }

        return false;
    }
    public boolean checkImpactDbRender() {
        if (this.selectedRunStep != null && (int) this.selectedRunStep.getValue().getKey() >= 100 &&
                Arrays.asList(AamConstants.ACTION.SUB_STEP_BACKUP_DB, AamConstants.ACTION.SUB_STEP_TD_DB,AamConstants.ACTION.ROLLBACK_CUSTOM_STEP_TD_DB,
                        AamConstants.ACTION.CUSTOM_STEP_BACKUP_DB, AamConstants.ACTION.CUSTOM_STEP_TD_DB,
                        AamConstants.ACTION.ROLLBACK_STEP_DB, AamConstants.ACTION.ROLLBACK_CUSTOM_STEP_BACKUP_DB).contains(this.selectedRunStep.getValue().getKey())) {
            return true;
        }
        return false;
    }
    public String getSecureCode() {
        return secureCode;
    }

    public void setSecureCode(String secureCode) {
        this.secureCode = secureCode;
    }

    public RunStep getSelectedRunStep() {
//		selectedRunStep = Constant.getSteps().get(selectedStep);
        selectedRunStep = Constant.getRunStep(selectedStep);

        if (selectedRunStep == null) {
            Map<String, Object> filters = new HashMap<>();
            if ((int) selectedStep.getKey() >= 320) {
                Integer step = 0;
                switch ((int) selectedStep.getKey()) {
                    case Constant.ROLLBACK_CUSTOM_STEP_CHECK_STATUS:
                        step = Constant.ROLLBACK_STEP_CHECK_STATUS;
                        break;
                    case Constant.ROLLBACK_CUSTOM_STEP_STOP_APP:
                        step = Constant.ROLLBACK_STEP_STOP_APP;
                        break;
                    case Constant.ROLLBACK_CUSTOM_STEP_UPCODE:
                        step = Constant.ROLLBACK_STEP_SOURCE_CODE;
                        break;
                    case Constant.ROLLBACK_CUSTOM_STEP_TD_DB:
                        step = Constant.ROLLBACK_STEP_DB;
                        break;
                    case Constant.ROLLBACK_CUSTOM_STEP_CLEARCACHE:
                        step = Constant.ROLLBACK_STEP_CLEARCACHE;
                        break;
                    case Constant.ROLLBACK_CUSTOM_STEP_RESTART_APP:
                        step = Constant.ROLLBACK_STEP_RESTART_APP;
                        break;
                    case Constant.ROLLBACK_CUSTOM_STEP_START_APP:
                        step = Constant.ROLLBACK_STEP_START_APP;
                        break;
                    default:
                        break;
                }
                filters.put("rollbackAfter", step);
            } else {
                filters.put("afterGroup", (int) selectedStep.getKey() > 300 ? (int) selectedStep.getKey() - 20 - 200 : (int) selectedStep.getKey() - 20);
            }
            if (selectedAction != null && selectedAction.getId() != null)
                filters.put("actionId", selectedAction.getId());
            ActionCustomGroupService customGroupService = new ActionCustomGroupServiceImpl();
            try {
                List<ActionCustomGroup> customGroups = customGroupService.findList(filters, new HashMap<>());

                if (customGroups != null && !customGroups.isEmpty()) {
                    selectedRunStep = new RunStep();
//					selectedRunStep.setValue(Integer.valueOf(selectedStep.getValue()));
                    selectedRunStep.setValue(selectedStep);
                    selectedRunStep.setLabel(customGroups.get(0).getName());
                    selectedRunStep.setDescription(customGroups.get(0).getName());
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        }

        return selectedRunStep;
        // return selectedRunStep;
    }

    public void setSelectedRunStep(RunStep selectedRunStep) {
        this.selectedRunStep = selectedRunStep;
        if (change && selectedRunStep != null)
            selectedStep = selectedRunStep.getValue();
//			selectedStep.setValue(selectedRunStep.getValue());

        change = false;
    }

    public ActionHistory getHistory() {
        return history;
    }

    public void setHistory(ActionHistory history) {
        this.history = history;
    }

    public String getReasonRollback() {
        return reasonRollback;
    }

    public void setReasonRollback(String reasonRollback) {
        this.reasonRollback = reasonRollback;
    }

    public String getScriptDetail() {
        return scriptDetail;
    }

    public void setScriptDetail(String scriptDetail) {
        this.scriptDetail = scriptDetail;
    }

    public List<ExeObject> getCheckVersionAppObjects() {
        return checkVersionAppObjects;
    }

    public void setCheckVersionAppObjects(List<ExeObject> checkVersionAppObjects) {
        this.checkVersionAppObjects = checkVersionAppObjects;
    }

    public List<ExcecuteInfoObj> getInfoList() {
        return infoList;
    }

    public LazyExcecuteInfo getLazyExcecuteInfo() {
        return lazyExcecuteInfo;
    }

    public List<SelectItem> getStatusFitterList() {
        return statusFitterList;
    }

    public Multimap<MapEntry, ExeObject> getCustomExeObjectMultimap() {
        return customExeObjectMultimap;
    }

    public void setCustomExeObjectMultimap(Multimap<MapEntry, ExeObject> customExeObjectMultimap) {
        this.customExeObjectMultimap = customExeObjectMultimap;
    }

    public List<ExeObject> getWaitingActions() {
        return waitingActions;
    }

    public void setWaitingActions(List<ExeObject> waitingActions) {
        this.waitingActions = waitingActions;
    }

    public Integer getRunStatus() {
        return runStatus;
    }

    public void setRunStatus(Integer runStatus) {
        this.runStatus = runStatus;
    }

    public JsfCaptcha getCaptcha() {
        return captcha;
    }

    public void setCaptcha(JsfCaptcha captcha) {
        this.captcha = captcha;
    }

    public Boolean getRunning() {
        return isRunning;
    }

    public ActionModuleService getActionModuleService() {
        return actionModuleService;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public Long getRunId() {
        return runId;
    }

    public void setRunId(Long runId) {
        this.runId = runId;
    }

    public boolean isRenderWaitTimeShutdown() {
        return renderWaitTimeShutdown;
    }

    public void setRenderWaitTimeShutdown(boolean renderWaitTimeShutdown) {
        this.renderWaitTimeShutdown = renderWaitTimeShutdown;
    }

    public int getDEFAULT_OPTION_TIME_OUT() {
        return DEFAULT_OPTION_TIME_OUT;
    }

    public void setDEFAULT_OPTION_TIME_OUT(int DEFAULT_OPTION_TIME_OUT) {
        this.DEFAULT_OPTION_TIME_OUT = DEFAULT_OPTION_TIME_OUT;
    }

    //20181126_tudn_start kiem tra module tac dong rollback
    public String getMessageModule() {
        return messageModule;
    }

    public void setMessageModule(String messageModule) {
        this.messageModule = messageModule;
    }

    public Boolean getSufficientImpact() {
        return sufficientImpact;
    }

    public void setSufficientImpact(Boolean sufficientImpact) {
        this.sufficientImpact = sufficientImpact;
    }

    public Boolean getSufficientRollback() {
        return sufficientRollback;
    }

    public void setSufficientRollback(Boolean sufficientRollback) {
        this.sufficientRollback = sufficientRollback;
    }

    //20181126_tudn_end kiem tra module tac dong rollback
	/*20181009_hoangnd_continue fail step_start*/
    public List<RunStep> getLstSteps() {

        List<RunStep> lstSteps = new ArrayList<>();
        if (stepResult != null && !stepResult.isEmpty()) {
            RunStep step;
            for (Integer kbGroup : kbGroups) {
                //05-12-2018 KienPD
                step = Constant.getRunStep(new MapEntry(Constant.SUB_STEP_CHECK_STATUS, 1));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                //05-12-2018 KienPD
                step = Constant.getRunStep(new MapEntry(Constant.SUB_STEP_STOP_APP, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.SUB_STEP_BACKUP_APP, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.SUB_STEP_BACKUP_DB, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.SUB_STEP_UPCODE, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.SUB_STEP_TD_DB, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.SUB_STEP_CLEARCACHE, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.SUB_STEP_RESTART_APP, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.SUB_STEP_START_APP, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.SUB_STEP_UPCODE_START_APP, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
            }
            //05-12-2018 KienPD
            step = Constant.getRunStep(new MapEntry(Constant.SUB_STEP_CHECKLIST_APP, 1));
            if (stepResult.get(step.getValue()) != null) {
                lstSteps.add(step);
            }
            //05-12-2018 KienPD
            step = Constant.getRunStep(new MapEntry(Constant.SUB_STEP_CHECKLIST_DB, 1));
            if (stepResult.get(step.getValue()) != null) {
                lstSteps.add(step);
            }
        }
        return lstSteps;
    }

    public List<RunStep> getLstRollbackSteps() {

        List<RunStep> lstSteps = new ArrayList<>();
        if (stepResult != null && !stepResult.isEmpty()) {
            RunStep step;
            for (Integer kbGroup : kbGroups) {
                // 05-12-2018 KienPD add step check status start
                /*20190128_hoangnd_fix bug hien thi buoc check status_start*/
                step = Constant.getRunStep(new MapEntry(Constant.ROLLBACK_STEP_CHECK_STATUS, 1));
                /*20190128_hoangnd_fix bug hien thi buoc check status_end*/
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                // 05-12-2018 KienPD add step check status end
                step = Constant.getRunStep(new MapEntry(Constant.ROLLBACK_STEP_STOP_APP, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.ROLLBACK_STEP_SOURCE_CODE, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.ROLLBACK_STEP_DB, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.ROLLBACK_STEP_CLEARCACHE, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.ROLLBACK_STEP_RESTART_APP, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.ROLLBACK_STEP_START_APP, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }
                step = Constant.getRunStep(new MapEntry(Constant.ROLLBACK_STEP_UPCODE_START, kbGroup));
                if (stepResult.get(step.getValue()) != null) {
                    lstSteps.add(step);
                }

            }
            // 05-12-2018 KienPD
            step = Constant.getRunStep(new MapEntry(Constant.ROLLBACK_STEP_CHECKLIST_APP, 1));
            if (stepResult.get(step.getValue()) != null) {
                lstSteps.add(step);
            }
            // 05-12-2018 KienPD
            step = Constant.getRunStep(new MapEntry(Constant.ROLLBACK_STEP_CHECKLIST_DB, 1));
            if (stepResult.get(step.getValue()) != null) {
                lstSteps.add(step);
            }
        }
        return lstSteps;
    }

    public List<RunStep> getLstImpactSteps() {
        return lstImpactSteps;
    }

    public void setLstImpactSteps(List<RunStep> lstImpactSteps) {
        this.lstImpactSteps = lstImpactSteps;
    }

    public boolean isShowSelectProcess() {
        return showSelectProcess;
    }

    public void setShowSelectProcess(boolean showSelectProcess) {
        this.showSelectProcess = showSelectProcess;
    }

    public String getNewLine() {
        return newLine;
    }

    public HtmlOutputText lineBreak() {
        HtmlOutputText lineBreak = new HtmlOutputText();
        lineBreak.setValue("&lt;br/&gt;");
        return lineBreak;
    }

    public String getFailStep() {
        return failStep;
    }

    public void setFailStep(String failStep) {
        this.failStep = failStep;
    }
    /*20181009_hoangnd_continue fail step_end*/

    @Transactional
    public static Action getActions(Long id) {

        Session session = null;
        Transaction tx = null;
        Action action = null;
        try {
            session = HibernateUtil.openSession();
            tx = session.beginTransaction();
            action = session.get(Action.class, id, LockMode.PESSIMISTIC_READ);
            session.refresh(action);
            action.setReason("aaa");
            session.saveOrUpdate(action);
            session.flush();
            tx.commit();
        } catch (HibernateException e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            if (tx != null) {
                tx.rollback();
            }
            logger.error(e.getMessage(), e);
            throw new SysException();
        } finally {
            if (session != null) {
                session.close();
            }
        }
        return action;
    }

    //03-11-2018 KienPD add step failed to rollback list start
    private String confirm;
    private String labelStep;
    List<RunStep> lstStep;
    private Multimap<MapEntry, ExeObject> mapStepFail;
    private Multimap<MapEntry, ExeObject> stepFail;
    private List<SelectItem> runStepsOld;
    private Map<MapEntry, ExeObject> stepImpactFail;

    public void lstStepFail(String cfm) {
        lstStep = new ArrayList<>();
        List<RunStep> list = new ArrayList<>();
        try {
            if ("yes".equalsIgnoreCase(cfm)) {
                if (!stepFail.isEmpty()) {
                    RunStep step = Constant.getRunStep(stepFail.keySet().iterator().next());
                    for (MapEntry map : mapStepFail.keySet()) {
                        String lblFail = "";
                        RunStep runStep = Constant.getRunStep(map);
                        if (step != null && step.getLabel().equalsIgnoreCase(runStep.getLabel())) {
                            labelStep = runStep.getLabel();
                            lblFail = labelStep;
                        }
                        runStep.setCheckbox(StringUtils.isEmpty(lblFail));
                        list.add(runStep);
                    }
                }
            } else if ("no".equalsIgnoreCase(cfm)) {
                for (MapEntry map : rollbackObjects.keySet()) {
                    list.add(Constant.getRunStep(map));
                }
            }
            MapEntry mapEntry = new MapEntry(Constant.SUB_STEP_CHECKLIST_APP, 1);
            if (impactObjects != null && impactObjects.get(mapEntry) != null && !impactObjects.get(mapEntry).isEmpty()) {
                list.add(Constant.getRunStep(new MapEntry(Constant.ROLLBACK_STEP_CHECKLIST_APP, 1)));
            }
            /*20190124_hoangnd_add buoc rollback checklist db_start*/
            mapEntry = new MapEntry(Constant.SUB_STEP_CHECKLIST_DB, 1);
            if (impactObjects != null && impactObjects.get(mapEntry) != null && !impactObjects.get(mapEntry).isEmpty()) {
                list.add(Constant.getRunStep(new MapEntry(Constant.ROLLBACK_STEP_CHECKLIST_DB, 1)));
            }
            /*20190124_hoangnd_add buoc rollback checklist db_end*/
            TreeMap<Integer, RunStep> treeMap = new TreeMap<>();
            for (RunStep step : list) {
                if (step.getValue().getKey() == AamConstants.ACTION.ROLLBACK_STEP_CHECK_STATUS) {
                    treeMap.put(1, step);
                } else if (step.getValue().getKey() == AamConstants.ACTION.ROLLBACK_STEP_STOP_APP) {
                    treeMap.put(2, step);
                } else if (step.getValue().getKey() == AamConstants.ACTION.ROLLBACK_STEP_SOURCE_CODE) {
                    treeMap.put(3, step);
                } else if (step.getValue().getKey() == AamConstants.ACTION.ROLLBACK_STEP_DB) {
                    treeMap.put(4, step);
                } else if (step.getValue().getKey() == AamConstants.ACTION.ROLLBACK_STEP_CLEARCACHE) {
                    treeMap.put(5, step);
                } else if (step.getValue().getKey() == AamConstants.ACTION.ROLLBACK_STEP_RESTART_APP) {
                    treeMap.put(6, step);
                } else if (step.getValue().getKey() == AamConstants.ACTION.ROLLBACK_STEP_START_APP) {
                    treeMap.put(7, step);
                } else if (step.getValue().getKey() == AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_APP) {
                    treeMap.put(8, step);
                } else if (step.getValue().getKey() == AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_DB) {
                    treeMap.put(9, step);
                }
            }
            for (Map.Entry<Integer, RunStep> map : treeMap.entrySet()) {
                lstStep.add(map.getValue());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void confirmRunStep() {
        try {
            lstStepFail(confirm);
            if ("yes".equalsIgnoreCase(confirm)) {
                int indexOfRllback = 0;
                for (SelectItem item : runSteps) {
                    if (item.getLabel().equalsIgnoreCase("rollback")) {
                        indexOfRllback = runSteps.indexOf(item);
                        break;
                    }
                }
                Iterator<SelectItem> iterator = runSteps.iterator();
                while (iterator.hasNext()) {
                    if (runSteps.indexOf(iterator.next()) > indexOfRllback) {
                        iterator.remove();
                    }
                }
            }
            if ("no".equalsIgnoreCase(confirm)) {
                runSteps = new ArrayList<>(runStepsOld);
            } else if ("yes".equalsIgnoreCase(confirm)) {
                for (RunStep step : lstStep) {
                    if (labelStep != null && labelStep.equalsIgnoreCase(step.getLabel())) {
                        if ("yes".equalsIgnoreCase(confirm)) {
                            step.setCheckbox(true);
                        } else {
                            step.setCheckbox(false);
                        }
                    }
                    runSteps.add(new SelectItem(step, step.getLabel()));
                }
            }
            RequestContext.getCurrentInstance().update("execute:exelayout");
            RequestContext.getCurrentInstance().update("pnlStep");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void runRollbackStep() {
        try {
            if (StringUtils.isEmpty(confirm) && StringUtils.isNotEmpty(labelStep)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("msg.require.choose.run.rollback.step"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                return;
            }
            if (StringUtils.isEmpty(reasonRollback)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("reason.rollback.do.not.enter"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                return;
            }
            if ("yes".equalsIgnoreCase(confirm)) {
                /*20181217_hoangnd_save curr_step sau khi chon_start*/
                if(history != null && history.getCurrStep() != null && history.getCurrKbGroup() != null) {
                    new ActionHistoryServiceImpl().saveOrUpdate(history);
                }
                /*20181217_hoangnd_save curr_step sau khi chon_end*/
                rollbackObjects = mapStepFail;
                ExeObject obj = stepFail.values().iterator().next();
                if (obj != null && obj.getDetailApp() != null) {
                    ActionDetailApp app = stepImpactFail.values().iterator().next().getDetailApp();
					/*20181214_hoangnd_them trang thai add buoc fail vao list rollback_start*/
//					app.setRunStatus(2);
                    app.setIsAddRollback(1);
					/*20181214_hoangnd_them trang thai add buoc fail vao list rollback_end*/
                    new ActionDetailAppServiceImpl().saveOrUpdate(app);
                } else if (obj != null && obj.getActionDatabase() != null) {
                    ActionDetailDatabase database = obj.getActionDatabase();
					/*20181214_hoangnd_them trang thai add buoc fail vao list rollback_start*/
//					database.setRunStatus(2);
                    database.setIsAddRollback(1);
					/*20181214_hoangnd_them trang thai add buoc fail vao list rollback_end*/
                    new ActionDetailDatabaseServiceImpl().saveOrUpdate(database);
                }
            }
            execute(3);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    /*20181217_hoangnd_save curr_step sau khi chon_start*/
    public void runTdStep() {
        try {
            if (history != null && history.getCurrStep() != null && history.getCurrKbGroup() != null) {
                new ActionHistoryServiceImpl().saveOrUpdate(history);
            }
            execute(2);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    /*20181217_hoangnd_save curr_step sau khi chon_end*/

    public void cancel() {
        runSteps = new ArrayList<>(runStepsOld);
    }

    public String getConfirm() {
        return confirm;
    }

    public void setConfirm(String confirm) {
        this.confirm = confirm;
    }

    public List<RunStep> getLstStep() {
        return lstStep;
    }

    public void setLstStep(List<RunStep> lstStep) {
        this.lstStep = lstStep;
    }

    public String getLabelStep() {
        return labelStep;
    }

    public void setLabelStep(String labelStep) {
        this.labelStep = labelStep;
    }

    public Multimap<MapEntry, ExeObject> getStepFail() {
        return stepFail;
    }

    public void setStepFail(Multimap<MapEntry, ExeObject> stepFail) {
        this.stepFail = stepFail;
    }

    private int cntRetry = 0;

    //03-11-2018 KienPD add step failed to rollback list end

    public static void main(String[] args) {
        try {
            /*File currentDirFile = new File(".");
            String helper = currentDirFile.getAbsolutePath();
            String currentDir = helper.substring(0, helper.length() - currentDirFile.getCanonicalPath().length());
            System.out.println(currentDir);*/
            System.out.println(new File(".").getCanonicalPath());
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }
}
