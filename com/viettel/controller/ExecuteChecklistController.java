package com.viettel.controller;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.rits.cloning.Cloner;
import com.viettel.it.util.MessageUtil;
import com.viettel.it.util.ObjectConcurrentMap;
import com.viettel.model.KpiDbSetting;
import com.viettel.exception.AppException;
import com.viettel.model.*;
import com.viettel.persistence.*;
import com.viettel.persistence.RstKpiDaoImpl;
import com.viettel.persistence.RstKpiDbSettingService;
import com.viettel.persistence.RstKpiService;
import com.viettel.util.Constant;
import com.viettel.util.PasswordEncoderQltn;
import org.apache.james.mime4j.message.Message;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.ocpsoft.rewrite.config.True;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.Visibility;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.io.Serializable;
import java.util.*;

/**
 * @author quanns2
 */
@ViewScoped
@ManagedBean
public class ExecuteChecklistController implements Serializable {
    //<editor-fold defaultstate="collapsed" desc="Param">
    private static Logger logger = LogManager.getLogger(ExecuteChecklistController.class);

    @ManagedProperty(value = "#{checklistService}")
    ChecklistService checklistService;

    private String messageModule = "";
    private String messageDb = MessageUtil.getResourceBundleMessage("get.database.count.sucess");
    private Map<String, String> mapResultMess;
    private Integer executeTypeModule;
    private boolean sufficientImpact = true;
    private boolean repeat = Boolean.FALSE;
    private int countModule = 0;
    private int countDb = 0;
    private boolean sufficientChecklist = Boolean.TRUE;

    public void setChecklistService(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @ManagedProperty(value = "#{rstKpiDbSettingService}")
    RstKpiDbSettingService rstKpiDbSettingService;

    public void setRstKpiDbSettingService(RstKpiDbSettingService rstKpiDbSettingService) {
        this.rstKpiDbSettingService = rstKpiDbSettingService;
    }

    public void setIimService(IimService iimService) {
        this.iimService = iimService;
    }

    @ManagedProperty(value = "#{iimService}")
    IimService iimService;

    private ExecuteController executeController;

    /*20181115_hoangnd_save all step_start*/
    private HistoryDetailController historyDetailController;
    /*20181115_hoangnd_save all step_end*/

//	private List<AppResult> checklistAppBefore;
//	private List<AppResult> checklistAppAfter;
//	private List<AppResult> checklistAppRollback;
//	private List<AppResult> checklistAppLast;

//	private AppInfo[] inputChecklistAppBefore;
//	private AppInfo[] inputChecklistAppAfter;
//	private AppInfo[] inputChecklistAppRollback;
//	private AppInfo[] inputChecklistAppLast;

    private List<Checklist> filteredChecklistAppBefore;
    private List<Checklist> filteredChecklistAppAfter;
    private List<Checklist> filteredChecklistAppRollback;
    private List<Checklist> filteredChecklistAppLast;

//	private List<DbResult> checklistDbBefore;
//	private List<DbResult> checklistDbAfter;
//	private List<DbResult> checklistDbRollback;
//	private List<DbResult> checklistDbLast;

//	private DbInfo[] inputChecklistDbBefore;
//	private DbInfo[] inputChecklistDbAfter;
//	private DbInfo[] inputChecklistDbRollback;
//	private DbInfo[] inputChecklistDbLast;

    private List<KpiDbSetting> filteredChecklistDbBefore;
    private List<KpiDbSetting> filteredChecklistDbAfter;
    private List<KpiDbSetting> filteredChecklistDbRollback;
    private List<KpiDbSetting> filteredChecklistDbLast;

    private Multimap<Module, Checklist> cklAppBefore;
    //	private Multimap<Module, Checklist> cklAppMain;
    private Multimap<Module, Checklist> cklAppAfter;
    //	private Multimap<Module, Checklist> cklAppRollback;
    private Map<MapEntry, Multimap<Module, Checklist>> newCklAppMain;
    private Map<MapEntry, Multimap<Module, Checklist>> newCklAppRollback;

    private Multimap<MonitorDatabase, QueueChecklist> cklDbBefore;
    private Multimap<MonitorDatabase, QueueChecklist> cklDbMain;
    private Multimap<MonitorDatabase, QueueChecklist> cklDbAfter;
    private Multimap<MonitorDatabase, QueueChecklist> cklDbRollback;
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Main">
    @PostConstruct
    public void onStart() {

    }

    /**
     * anhnt2 - 01/08/2018
     *
     * @param module
     * @param action
     * @param actionModule
     * @param checklist    Load checkapp for case window
     */
    public void loadCheckAppsCaseWindow(Module module, Action action, ActionModule actionModule, Checklist checklist, boolean isCklAppBefore) {
        try {
            if (module.getOsType() == AamConstants.OS_TYPE.WINDOWS) {
                List<OsAccount> osAccounts = new IimServiceImpl().findOsAccount(action.getImpactProcess().getNationCode(), module.getIpServer());
                if (osAccounts != null) {
                    for (OsAccount osAccount : osAccounts) {
                        if (osAccount.getUsername().equalsIgnoreCase(actionModule.getInstalledUser())) {
                            module.setUsername(osAccount.getUsername());
                            module.setPassword(osAccount.getPassword());
                            checklist.setModule(module);
                            /*20181116_hoangnd_save all step_start*/
                            Cloner cloner = new Cloner();
                            Checklist newChecklist = cloner.deepClone(checklist);
                            if (isCklAppBefore) {
                                newChecklist.setChecklistType(AamConstants.AAM_WS_CODE.UPDATE_CHECKLIST_APP_STATUS_BEFORE);
                                cklAppBefore.put(module, newChecklist);
                            } else {
                                newChecklist.setChecklistType(AamConstants.AAM_WS_CODE.UPDATE_CHECKLIST_APP_STATUS_AFTER);
                                cklAppAfter.put(module, newChecklist);
                            }
                            /*20181116_hoangnd_save all step_end*/
                            break;
                        }
                    }
                }
            } else {
                checklist.setModule(module);
                /*20181117_hoangnd_save all step_start*/
                Cloner cloner = new Cloner();
                Checklist newChecklist = cloner.deepClone(checklist);
                if (isCklAppBefore) {
                    newChecklist.setChecklistType(AamConstants.AAM_WS_CODE.UPDATE_CHECKLIST_APP_STATUS_BEFORE);
                    cklAppBefore.put(module, newChecklist);
                } else {
                    newChecklist.setChecklistType(AamConstants.AAM_WS_CODE.UPDATE_CHECKLIST_APP_STATUS_AFTER);
                    cklAppAfter.put(module, newChecklist);
                }
                /*20181117_hoangnd_save all step_end*/
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void init() {
        checklistService = new ChecklistServiceImpl();
        rstKpiDbSettingService = new RstKpiDbSettingDaoImpl();
        iimService = new IimServiceImpl();
    }

    public void reload() {
        cklAppBefore = HashMultimap.create();
//		cklAppMain = HashMultimap.create();
        cklAppAfter = HashMultimap.create();
//		cklAppRollback = HashMultimap.create();
        cklDbBefore = HashMultimap.create();
        cklDbMain = HashMultimap.create();
        cklDbAfter = HashMultimap.create();
        cklDbRollback = HashMultimap.create();

        newCklAppMain = new HashMap<>();
        newCklAppRollback = new HashMap<>();

        /*20181116_hoangnd_save all step_start*/
        Action action;
        if (historyDetailController != null)
            action = historyDetailController.getSelectedAction();
        else
            action = executeController.getSelectedAction();
        /*20181116_hoangnd_save all step_end*/

//		checklistAppBefore = new ArrayList<>();
//		checklistAppAfter = new ArrayList<>();
//		checklistAppRollback = new ArrayList<>();
//		checklistAppLast = new ArrayList<>();
//
//		checklistDbBefore = new ArrayList<>();
//		checklistDbAfter = new ArrayList<>();
//		checklistDbRollback = new ArrayList<>();
//		checklistDbLast = new ArrayList<>();

        RstKpiService kpiService = new RstKpiDaoImpl();
        List<Long> rstKpiIds = null;
        try {
            rstKpiIds = kpiService.getKpiByCode(Arrays.asList("LIVE_OR_DIE", "CPU_MODULE", "RAM_MODULE", "CHECK_ERORR_LOG"));
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        if (rstKpiIds == null)
            rstKpiIds = new ArrayList<>();

        ActionDetailAppService detailAppService = new ActionDetailAppServiceImpl();
        Multimap<Module, Checklist> multimap = HashMultimap.create();
        /*20181116_hoangnd_save all step_start*/
        if (action != null) {
            ActionModuleService actionModuleService;
            if (historyDetailController != null)
                actionModuleService = historyDetailController.getActionModuleService();
            else
                actionModuleService = executeController.getActionModuleService();
            /*20181116_hoangnd_save all step_end*/
            try {
                List<Checklist> selectedChecklists = checklistService.findCheckListAppByAction(action.getId());
                /*20181116_hoangnd_save all step_start*/
				/*Multimap<Long, Long> ckls = HashMultimap.create();
				for (Checklist checklist : selectedChecklists) {
					ckls.put(checklist.getModuleId(), checklist.getId());
				}*/
                Multimap<Long, Checklist> ckls = HashMultimap.create();
                for (Checklist checklist : selectedChecklists) {
                    ckls.put(checklist.getModuleId(), checklist);
                }

//				for (Map.Entry<Long,Collection<Long>> entry : ckls.asMap().entrySet()) {
                for (Map.Entry<Long, Collection<Checklist>> entry : ckls.asMap().entrySet()) {
                    /*20181116_hoangnd_save all step_end*/
                    try {
                        Module module = iimService.findModuleById(action.getImpactProcess().getNationCode(), entry.getKey());
                        //Module module = null;
                        if (module != null) {
                            /*20181116_hoangnd_save all step_start*/
                            ActionModule actionModule = actionModuleService.findModule(action.getId(), entry.getKey());
                            /*20181116_hoangnd_save all step_end*/
                            module.setKbGroup(actionModule.getKbGroup());
                            countModule++;
                            logger.info("GET MODULE IMPACT: " + module.getModuleName() + " " + countModule + "/" + ckls.asMap().entrySet().size() + " STEP STOP BY IIM");

                            /*20181116_hoangnd_save all step_start*/
                            if ((action.getTestbedMode() && actionModule.getTestbedMode() != 1) || (!action.getTestbedMode() && actionModule.getTestbedMode() == 1))
                                continue;

                            List<ActionDetailApp> moduleStop = detailAppService.findListDetailApp(action.getId(), Constant.STEP_STOP, module.getModuleId(), null, true);
                            List<ActionDetailApp> moduleStart = detailAppService.findListDetailApp(action.getId(), Constant.STEP_START, module.getModuleId(), null, true);

                            //					List<Checklist> checklists = checklistService.findCheckListByAction(entry.getValue());
                            List<Checklist> checklists = new ArrayList<>(entry.getValue());
                            /*20181116_hoangnd_save all step_end*/

                            for (Checklist checklist : checklists) {
                                multimap.put(module, checklist);

	/*						AppResult appResult = new AppResult();
							appResult.setAppCode(module.getModuleCode());
							appResult.setAppName(module.getModuleName());
							appResult.setAppGroupCode(module.getServiceCode());
							appResult.setAppGroupName(module.getServiceName());
							appResult.setAppId(module.getModuleId());
							appResult.setKpiName(checklist.getName());*/

                                if (moduleStop.isEmpty() && !moduleStart.isEmpty() && rstKpiIds.contains(checklist.getId())) {
                                    //chi co start
                                } else {
                                    //							checklistAppBefore.add(appResult);
                                    // anhnt2 - 01/08/2018
                                    loadCheckAppsCaseWindow(module, action, actionModule, checklist, true);
                                }

							/*appResult = new AppResult();
							appResult.setAppCode(module.getModuleCode());
							appResult.setAppName(module.getModuleName());
							appResult.setAppGroupCode(module.getServiceCode());
							appResult.setAppGroupName(module.getServiceName());
							appResult.setAppId(module.getModuleId());
							appResult.setKpiName(checklist.getName());*/

                                if (!moduleStop.isEmpty() && moduleStart.isEmpty() && rstKpiIds.contains(checklist.getId())) {
                                    //chi co stop
                                } else {
                                    //							checklistAppAfter.add(appResult);
                                    checklist.setModule(module);
                                    //							cklAppMain.put(module, checklist);
                                    /*20181116_hoangnd_save all step_start*/
                                    Cloner cloner = new Cloner();
                                    Checklist newChecklist = cloner.deepClone(checklist);
                                    newChecklist.setChecklistType(AamConstants.AAM_WS_CODE.UPDATE_CHECKLIST_APP_STATUS_IMPACT);
                                    /*20181116_hoangnd_save all step_end*/

                                    MapEntry mapEntry = new MapEntry(AamConstants.ACTION.SUB_STEP_CHECKLIST_APP, module.getKbGroup());
                                    Multimap<Module, Checklist> moduleChecklistMultimap = newCklAppMain.get(mapEntry);
                                    if (moduleChecklistMultimap == null)
                                        moduleChecklistMultimap = HashMultimap.create();
                                    /*20181117_hoangnd_save all step_start*/
                                    moduleChecklistMultimap.put(module, newChecklist);
                                    /*20181117_hoangnd_save all step_end*/

                                    newCklAppMain.put(mapEntry, moduleChecklistMultimap);
                                }

							/*appResult = new AppResult();
							appResult.setAppCode(module.getModuleCode());
							appResult.setAppName(module.getModuleName());
							appResult.setAppGroupCode(module.getServiceCode());
							appResult.setAppGroupName(module.getServiceName());
							appResult.setAppId(module.getModuleId());
							appResult.setKpiName(checklist.getName());*/

                                if (moduleStop.isEmpty() && !moduleStart.isEmpty() && rstKpiIds.contains(checklist.getId())) {
                                    //chi co start
                                } else {
                                    //							checklistAppRollback.add(appResult);
                                    checklist.setModule(module);
                                    //							cklAppRollback.put(module, checklist);
                                    /*20181116_hoangnd_save all step_start*/
                                    Cloner cloner = new Cloner();
                                    Checklist newChecklist = cloner.deepClone(checklist);
                                    newChecklist.setChecklistType(AamConstants.AAM_WS_CODE.UPDATE_CHECKLIST_APP_STATUS_ROLLBACK);
                                    /*20181116_hoangnd_save all step_end*/
                                    MapEntry mapEntry = new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_APP, module.getKbGroup());
                                    Multimap<Module, Checklist> moduleChecklistMultimap = newCklAppRollback.get(mapEntry);
                                    if (moduleChecklistMultimap == null)
                                        moduleChecklistMultimap = HashMultimap.create();
                                    /*20181117_hoangnd_save all step_start*/
                                    moduleChecklistMultimap.put(module, newChecklist);
                                    /*20181117_hoangnd_save all step_end*/

                                    newCklAppRollback.put(mapEntry, moduleChecklistMultimap);
                                }

							/*appResult = new AppResult();
							appResult.setAppCode(module.getModuleCode());
							appResult.setAppName(module.getModuleName());
							appResult.setAppGroupCode(module.getServiceCode());
							appResult.setAppGroupName(module.getServiceName());
							appResult.setAppId(module.getModuleId());
							appResult.setKpiName(checklist.getName());*/
                                if (!moduleStop.isEmpty() && moduleStart.isEmpty() && rstKpiIds.contains(checklist.getId())) {
                                    //chi co start
                                } else {
                                    //							checklistAppLast.add(appResult);
                                    // anhnt2 - 01/08/2018
                                    loadCheckAppsCaseWindow(module, action, actionModule, checklist, false);
                                }
                            }
                        }
                        // namlh_check tinh day du cua cac module get tu iim truoc tac dong_start
                        else {
                            ActionModule actionMod = actionModuleService.findModule(action.getId(), entry.getKey());
                            String moduleCode = actionMod != null && actionMod.getModuleName() != null ? actionMod.getModuleName() : String.valueOf(action.getId());
                            messageModule = String.format(MessageUtil.getResourceBundleMessage("get.module.count.sucess"),
                                    String.valueOf(countModule), String.valueOf(ckls.asMap().entrySet().size()), moduleCode);
                            logger.info("GET MODULE IMPACT FAIL: " + moduleCode + " STEP STOP BY IIM");
                            sufficientChecklist = Boolean.FALSE;
                            RequestContext.getCurrentInstance().execute("PF('confirmCountModuleChecklist').show()");
                            return;
                        }
                        // namlh_check tinh day du cua cac module get tu iim truoc tac dong_end

                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                if (countModule == ckls.asMap().entrySet().size()) {
                    logger.info("GET ENOUGH MODULES IMPACT FROM IIM");
                    RequestContext.getCurrentInstance().execute("PF('loadingDialog').hide()");
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        }

//		checklistDbBefore = new ArrayList<>();
//		checklistDbAfter = new ArrayList<>();
//		checklistDbRollback = new ArrayList<>();
//		checklistDbLast = new ArrayList<>();

//		Multimap<ServiceDatabase, KpiDbSetting> multimapDb = HashMultimap.create();
        /*20181116_hoangnd_save all step_start*/
        if (action != null) {
            AomClientService aomClientService;
            if (historyDetailController != null)
                aomClientService = historyDetailController.aomClientService;
            else
                aomClientService = executeController.aomClientService;
            /*20181116_hoangnd_save all step_end*/
            try {
                List<Checklist> selectedChecklists = checklistService.findCheckListDbByAction(action.getId());
                Multimap<Long, Long> ckls = HashMultimap.create();
                /*20181119_hoangnd_save all step_start*/
                Map<Long, Checklist> mapChecklist = new HashMap<>();
                /*20181119_hoangnd_save all step_end*/
                for (Checklist checklist : selectedChecklists) {
                    ckls.put(checklist.getModuleId(), checklist.getId());
                    /*20181119_hoangnd_save all step_start*/
                    mapChecklist.put(checklist.getId(), checklist);
                    /*20181119_hoangnd_save all step_end*/
                }

                for (Map.Entry<Long, Collection<Long>> entry : ckls.asMap().entrySet()) {
//					ServiceDatabase database = iimService.findServiceDbById(entry.getKey());
                    /*20181116_hoangnd_save all step_start*/
                    List<MonitorDatabase> databases = iimService.findDbMonitor(action.getImpactProcess().getNationCode(), action.getImpactProcess().getUnitId(), Arrays.asList(entry.getKey()));
                    /*20181116_hoangnd_save all step_end*/

                    //List<MonitorDatabase> databases = null;
                    if (databases != null && databases.size() > 0) {
                        MonitorDatabase database = databases.get(0);
                        countDb++;

//					List<Checklist> checklists = checklistService.findCheckListByAction(entry.getValue());
                        /*20181116_hoangnd_save all step_start*/
                        List<QueueChecklist> checklists = aomClientService.findChecklistQueueByIds(new ArrayList<>(entry.getValue()));
                        /*20181116_hoangnd_save all step_end*/

                        for (QueueChecklist checklist : checklists) {
                            /*20181119_hoangnd_save all step_start*/
                            if (mapChecklist != null && !mapChecklist.isEmpty()) {
                                Checklist oldChecklist = mapChecklist.get(checklist.getQueueId());
                                if (oldChecklist != null) {
                                    checklist.setActionDbChecklistId(oldChecklist.getActionDbChecklistId());
                                    checklist.setResultBefore(oldChecklist.getResultBefore());
                                    checklist.setResultImpact(oldChecklist.getResultImpact());
                                    checklist.setResultAfter(oldChecklist.getResultAfter());
                                    checklist.setResultRollback(oldChecklist.getResultRollback());
                                    checklist.setLimitedBefore(oldChecklist.getLimitedBefore());
                                    checklist.setLimitedImpact(oldChecklist.getLimitedImpact());
                                    checklist.setLimitedAfter(oldChecklist.getLimitedAfter());
                                    checklist.setLimitedRollback(oldChecklist.getLimitedRollback());
                                    checklist.setStatusBefore(oldChecklist.getStatusBefore());
                                    checklist.setStatusImpact(oldChecklist.getStatusImpact());
                                    checklist.setStatusAfter(oldChecklist.getStatusAfter());
                                    checklist.setStatusRollback(oldChecklist.getStatusRollback());
                                }
                            }
                            /*20181119_hoangnd_save all step_end*/
						/*DbResult dbResult = new DbResult();
						dbResult.setAppGroupId(new Long(database.getServiceId()));
						dbResult.setAppGroupCode(database.getServiceCode());
						dbResult.setAppGroupName(database.getServiceName());
						dbResult.setDbName(database.getDbName());
						dbResult.setDbCode(database.getDbCode());
						dbResult.setKpiName(checklist.getName());
						checklistDbBefore.add(dbResult);*/

//						KpiDbSetting dbSetting = rstKpiDbSettingService.findbyKpiId(checklist.getId(), new Long(database.getServiceDbId()));
//						dbSetting.setServiceDatabase(database);
                            checklist.setMonitorDatabase(database);
                            /*20181119_hoangnd_save all step_start*/
                            Cloner cloner = new Cloner();
                            QueueChecklist newChecklistBefore = cloner.deepClone(checklist);
                            newChecklistBefore.setChecklistType(AamConstants.AAM_WS_CODE.UPDATE_CHECKLIST_DB_STATUS_BEFORE);
                            cklDbBefore.put(database, newChecklistBefore);
                            /*20181119_hoangnd_save all step_end*/

/*						dbResult = new DbResult();
						dbResult.setAppGroupId(new Long(database.getServiceId()));
						dbResult.setAppGroupCode(database.getServiceCode());
						dbResult.setAppGroupName(database.getServiceName());
						dbResult.setDbName(database.getDbName());
						dbResult.setDbCode(database.getDbCode());
						dbResult.setKpiName(checklist.getName());*/
//						checklistDbAfter.add(dbResult);
                            /*20181119_hoangnd_save all step_start*/
                            QueueChecklist newChecklist = cloner.deepClone(checklist);
                            newChecklist.setChecklistType(AamConstants.AAM_WS_CODE.UPDATE_CHECKLIST_DB_STATUS_IMPACT);
                            cklDbMain.put(database, newChecklist);
                            /*20181119_hoangnd_save all step_end*/

/*						dbResult = new DbResult();
						dbResult.setAppGroupId(new Long(database.getServiceId()));
						dbResult.setAppGroupCode(database.getServiceCode());
						dbResult.setAppGroupName(database.getServiceCode());
						dbResult.setDbName(database.getDbName());
						dbResult.setDbCode(database.getDbCode());
						dbResult.setKpiName(checklist.getName());*/
//						checklistDbRollback.add(dbResult);
                            /*20181119_hoangnd_save all step_start*/
                            QueueChecklist newChecklistAfter = cloner.deepClone(checklist);
                            newChecklistAfter.setChecklistType(AamConstants.AAM_WS_CODE.UPDATE_CHECKLIST_DB_STATUS_AFTER);
                            cklDbAfter.put(database, newChecklistAfter);
                            /*20181119_hoangnd_save all step_end*/

/*						dbResult = new DbResult();
						dbResult.setAppGroupId(new Long(database.getServiceId()));
						dbResult.setAppGroupCode(database.getServiceCode());
						dbResult.setAppGroupName(database.getServiceName());
						dbResult.setDbName(database.getDbName());
						dbResult.setDbCode(database.getDbCode());
						dbResult.setKpiName(checklist.getName());*/
//						checklistDbLast.add(dbResult);
                            /*20181119_hoangnd_save all step_start*/
                            QueueChecklist newChecklistRb = cloner.deepClone(checklist);
                            newChecklistRb.setChecklistType(AamConstants.AAM_WS_CODE.UPDATE_CHECKLIST_DB_STATUS_ROLLBACK);
                            cklDbRollback.put(database, newChecklistRb);
                            /*20181119_hoangnd_save all step_end*/
                        }
                    } else {
                        messageModule = String.format(MessageUtil.getResourceBundleMessage("get.database.count.sucess"),
                                String.valueOf(countDb), String.valueOf(ckls.asMap().entrySet().size()), entry.getKey().toString());
                        sufficientChecklist = Boolean.FALSE;
                        RequestContext.getCurrentInstance().execute("PF('confirmCountModuleChecklist').show()");
                        return;

                    }
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        }

		/*inputChecklistAppAfter = new AppInfo[multimap.asMap().size()];
		inputChecklistAppBefore = new AppInfo[multimap.asMap().size()];
		inputChecklistAppRollback = new AppInfo[multimap.asMap().size()];
		inputChecklistAppLast = new AppInfo[multimap.asMap().size()];*/

/*		List<AppInfo> lstInputChecklistAppBefore = new ArrayList<>();
		List<AppInfo> lstInputChecklistAppAfter = new ArrayList<>();
		List<AppInfo> lstInputChecklistAppRollback = new ArrayList<>();
		List<AppInfo> lstInputChecklistAppLast = new ArrayList<>();*/

		/*int index = 0;
		for (Map.Entry<Module,Collection<Checklist>> entry : multimap.asMap().entrySet()) {
			AppInfo appInfo = new AppInfo();

			Module module = entry.getKey();
			*//*com.viettel.model.ApplicationDetailService service = applicationDetailServiceService.getObjByAppId(module.getModuleId());

//			AppInfo appInfo = new AppInfo();
			RstApp app =new RstApp();
			app.setAppCode(module.getModuleCode());
			app.setAppGroupCode(module.getServiceCode());
			app.setAppGroupId(module.getServiceId());
			app.setAppGroupName(module.getServiceName());
			app.setAppId(module.getModuleId());
			app.setAppName(module.getModuleName());
			app.setAppType(Long.valueOf(module.getModuleType()));
			app.setAppStatus(new Long(module.getModuleStatus()));
			app.setCheckLog(module.getLogPath());
			app.setInstalledUser(module.getInstalledUser());
			app.setIpSever(module.getIpServer());
			app.setOsName(module.getOsName());
			app.setOsType(module.getOsType());
			app.setPath(module.getExecutePath());
			app.setPwd(service.getPwd());
//			app.setShell(detail.g);
			app.setUnitId(new Long(module.getUnitId()));*//*

			List<Checklist> checklists = new ArrayList<>(entry.getValue());
			List<RstKpi> rstKpis = new ArrayList<>();
			for (int i = 0; i < checklists.size(); i++) {
				Checklist checklist = checklists.get(i);

				RstKpi kpi=new RstKpi();
				kpi.setCheckoutType(new Long(checklist.getCheckoutType()));
				kpi.setCode(checklist.getCode());
				kpi.setDefaultMathOption(new Long(checklist.getDefaultMathOption()));
				kpi.setId(checklist.getId());
				kpi.setName(checklist.getName());
				kpi.setNumberDefaultValue(checklist.getNumberDefaultValue());
				kpi.setStringDefaultValue(checklist.getStringDefaultValue());
				kpi.setType(checklist.getType());

				rstKpis.add(kpi);
			}

			appInfo.setApp(module);
			appInfo.setKpis(rstKpis);
			List<ActionDetailApp> moduleStop = detailAppService.findListDetailApp(executeController.getSelectedAction().getId(), Constant.STEP_STOP, module.getModuleId());
			List<ActionDetailApp> moduleStart = detailAppService.findListDetailApp(executeController.getSelectedAction().getId(), Constant.STEP_START, module.getModuleId());

			Cloner cloner = new Cloner();
			if (moduleStop.isEmpty() && !moduleStart.isEmpty()) {
				//chi co start
				AppInfo appInfoNew = cloner.deepClone(appInfo);

				List<RstKpi> rstKpiNews = new ArrayList<>();
				for (int i = 0; i < checklists.size(); i++) {
					Checklist checklist = checklists.get(i);

					if (rstKpiIds != null && rstKpiIds.contains(checklist.getId()))
						continue;

					RstKpi kpi=new RstKpi();
					kpi.setCheckoutType(new Long(checklist.getCheckoutType()));
					kpi.setCode(checklist.getCode());
					kpi.setDefaultMathOption(new Long(checklist.getDefaultMathOption()));
					kpi.setId(checklist.getId());
					kpi.setName(checklist.getName());
					kpi.setNumberDefaultValue(checklist.getNumberDefaultValue());
					kpi.setStringDefaultValue(checklist.getStringDefaultValue());
					kpi.setType(checklist.getType());

					rstKpiNews.add(kpi);
				}

				appInfoNew.setKpis(rstKpiNews);
*//*				lstInputChecklistAppBefore.add(appInfoNew);
				lstInputChecklistAppRollback.add(appInfoNew);*//*
			} else {
*//*				lstInputChecklistAppBefore.add(appInfo);
				lstInputChecklistAppRollback.add(appInfo);*//*
			}

			if (!moduleStop.isEmpty() && moduleStart.isEmpty()) {
				//chi co stop
				AppInfo appInfoNew = cloner.deepClone(appInfo);

				List<RstKpi> rstKpiNews = new ArrayList<>();
				for (int i = 0; i < checklists.size(); i++) {
					Checklist checklist = checklists.get(i);

					if (rstKpiIds != null && rstKpiIds.contains(checklist.getId()))
						continue;

					RstKpi kpi=new RstKpi();
					kpi.setCheckoutType(new Long(checklist.getCheckoutType()));
					kpi.setCode(checklist.getCode());
					kpi.setDefaultMathOption(new Long(checklist.getDefaultMathOption()));
					kpi.setId(checklist.getId());
					kpi.setName(checklist.getName());
					kpi.setNumberDefaultValue(checklist.getNumberDefaultValue());
					kpi.setStringDefaultValue(checklist.getStringDefaultValue());
					kpi.setType(checklist.getType());

					rstKpiNews.add(kpi);
				}

				appInfoNew.setKpis(rstKpiNews);
*//*				lstInputChecklistAppAfter.add(appInfoNew);
				lstInputChecklistAppLast.add(appInfoNew);*//*
			} else {
*//*				lstInputChecklistAppAfter.add(appInfo);
				lstInputChecklistAppLast.add(appInfo);*//*
			}
			*//*inputChecklistAppBefore[index] = appInfo;
			inputChecklistAppAfter[index] = appInfo;
			inputChecklistAppRollback[index] = appInfo;
			inputChecklistAppLast[index] = appInfo;*//*
			index ++;
		}
*//*		inputChecklistAppBefore = lstInputChecklistAppBefore.toArray(new AppInfo[lstInputChecklistAppBefore.size()]);
		inputChecklistAppAfter = lstInputChecklistAppAfter.toArray(new AppInfo[lstInputChecklistAppAfter.size()]);
		inputChecklistAppRollback = lstInputChecklistAppRollback.toArray(new AppInfo[lstInputChecklistAppRollback.size()]);
		inputChecklistAppLast = lstInputChecklistAppLast.toArray(new AppInfo[lstInputChecklistAppLast.size()]);*//*


		inputChecklistDbAfter = new DbInfo[multimapDb.size()];
		inputChecklistDbBefore = new DbInfo[multimapDb.size()];
		inputChecklistDbRollback = new DbInfo[multimapDb.size()];
		inputChecklistDbLast = new DbInfo[multimapDb.size()];

		index = 0;
		for (Map.Entry<ServiceDatabase,Collection<KpiDbSetting>> entry : multimapDb.asMap().entrySet()) {
			DbInfo dbInfo = new DbInfo();

			ServiceDatabase database = entry.getKey();

//			AppInfo appInfo = new AppInfo();
			*//*RstViewDb db =new RstViewDb();
			db.setAppGroupCode(database.getServiceCode());
			db.setAppGroupDbId(new Long(database.getServiceDbId()));
			db.setAppGroupId(new Long(database.getServiceId()));
			db.setAppGroupName(database.getServiceName());
			db.setConnectionString(database.getUrl());
			db.setDbCode(database.getDbCode());
			db.setDbId(new Long(database.getDbId()));
			db.setDbName(database.getDbName());
			db.setDbType(new Long(database.getDbType()));
			db.setDbVersion(database.getDbVersion());
			db.setIpServer(database.getIpVirtual());
			db.setPassword(database.getPassword());
			db.setUsername(database.getUsername());*//*

			List<KpiDbSetting> checklists = new ArrayList<>(entry.getValue());
			*//*KpiDbSetting[] rstKpis = new KpiDbSetting[entry.getValue().size()];
			for (int i = 0; i < checklists.size(); i++) {
				KpiDbSetting checklist = checklists.get(i);

				KpiDbSetting dbSetting = new KpiDbSetting();
				dbSetting.setAppGroupId(checklist.getAppGroupId());
				dbSetting.setDataColumnIndex(checklist.getDataColumnIndex());
				dbSetting.setDefaultvalue(checklist.getDefaultvalue());
				dbSetting.setId(checklist.getId());
				dbSetting.setKpiId(checklist.getKpiId());
				dbSetting.setMathOption(checklist.getMathOption());
				dbSetting.setSqlCommand(checklist.getSqlCommand());
				dbSetting.setUnitId(checklist.getUnitId());
				dbSetting.setViewDbId(checklist.getViewDbId());

				rstKpis[i] = dbSetting;
			}*//*

			dbInfo.setDb(database);
			dbInfo.setDbSettings(checklists);
			inputChecklistDbBefore[index] = dbInfo;
			inputChecklistDbAfter[index] = dbInfo;
			inputChecklistDbRollback[index] = dbInfo;
			inputChecklistDbLast[index] = dbInfo;
			index ++;
		}*/


		/*checklistAppBefore = new ArrayList<>();
		checklistAppAfter = new ArrayList<>();
		checklistAppRollback = new ArrayList<>();
		checklistAppLast = new ArrayList<>();

		checklistDbBefore = new ArrayList<>();
		checklistDbAfter = new ArrayList<>();
		checklistDbRollback = new ArrayList<>();
		checklistDbLast = new ArrayList<>();

		inputChecklistAppBefore = new AppInfo[0];
		inputChecklistAppAfter = new AppInfo[0];
		inputChecklistAppRollback = new AppInfo[0];
		inputChecklistAppLast = new AppInfo[0];

		inputChecklistDbAfter = new DbInfo[0];
		inputChecklistDbBefore = new DbInfo[0];
		inputChecklistDbRollback = new DbInfo[0];
		inputChecklistDbLast = new DbInfo[0];*/
    }

    //Start longlt- 2016 - 10 - 17
    private List<Boolean> clAppBfVisibale = new ArrayList<Boolean>() {
        private static final long serialVersionUID = 1L;

        {
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);

        }
    };

    public void onClAppBfToggler(ToggleEvent e) {
        this.clAppBfVisibale.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
    }

    private List<Boolean> clAppAtVisibale = new ArrayList<Boolean>() {
        private static final long serialVersionUID = 1L;

        {
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);
            add(Boolean.TRUE);
            add(Boolean.TRUE);

        }
    };

    public void onClAppAtToggler(ToggleEvent e) {
        this.clAppAtVisibale.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
    }

    private List<Boolean> clDbBfVisibale = new ArrayList<Boolean>() {
        private static final long serialVersionUID = 1L;

        {
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);

        }
    };


    public void onClDbBfVisibale(ToggleEvent e) {
        this.clDbBfVisibale.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
    }

    private List<Boolean> clDbAtVisibale = new ArrayList<Boolean>() {
        private static final long serialVersionUID = 1L;

        {
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);
            add(Boolean.FALSE);
            add(Boolean.TRUE);

        }
    };

    public void onClDbAtVisibale(ToggleEvent e) {
        this.clDbAtVisibale.set((Integer) e.getData(), e.getVisibility() == Visibility.VISIBLE);
    }


    //End longlt- 2016 - 10 - 17

    public void closeModule() {
        RequestContext.getCurrentInstance().execute("PF('confirmCountModuleChecklist').hide()");
    }
    //</editor-fold>
    //<editor-fold defaultstate="collapsed" desc="Get-Set">
    public List<Boolean> getClDbAtVisibale() {
        return clDbAtVisibale;
    }

    public List<Boolean> getClDbBfVisibale() {
        return clDbBfVisibale;
    }

    public List<Boolean> getClAppAtVisibale() {
        return clAppAtVisibale;
    }

    public List<Boolean> getClAppBfVisibale() {
        return clAppBfVisibale;
    }


    public ExecuteController getExecuteController() {
        return executeController;
    }

    public void setExecuteController(ExecuteController executeController) {
        this.executeController = executeController;
    }
//
//	public List<AppResult> getChecklistAppBefore() {
//		return checklistAppBefore;
//	}
//
//	public void setChecklistAppBefore(List<AppResult> checklistAppBefore) {
//		this.checklistAppBefore = checklistAppBefore;
//	}
//
//	public List<AppResult> getChecklistAppAfter() {
//		return checklistAppAfter;
//	}
//
//	public void setChecklistAppAfter(List<AppResult> checklistAppAfter) {
//		this.checklistAppAfter = checklistAppAfter;
//	}

    public List<Checklist> getFilteredChecklistAppBefore() {
        return filteredChecklistAppBefore;
    }

    public void setFilteredChecklistAppBefore(List<Checklist> filteredChecklistAppBefore) {
        this.filteredChecklistAppBefore = filteredChecklistAppBefore;
    }

    public List<Checklist> getFilteredChecklistAppAfter() {
        return filteredChecklistAppAfter;
    }

    public void setFilteredChecklistAppAfter(List<Checklist> filteredChecklistAppAfter) {
        this.filteredChecklistAppAfter = filteredChecklistAppAfter;
    }

//	public List<DbResult> getChecklistDbBefore() {
//		return checklistDbBefore;
//	}
//
//	public void setChecklistDbBefore(List<DbResult> checklistDbBefore) {
//		this.checklistDbBefore = checklistDbBefore;
//	}
//
//	public List<DbResult> getChecklistDbAfter() {
//		return checklistDbAfter;
//	}
//
//	public void setChecklistDbAfter(List<DbResult> checklistDbAfter) {
//		this.checklistDbAfter = checklistDbAfter;
//	}

    public List<KpiDbSetting> getFilteredChecklistDbBefore() {
        return filteredChecklistDbBefore;
    }

    public void setFilteredChecklistDbBefore(List<KpiDbSetting> filteredChecklistDbBefore) {
        this.filteredChecklistDbBefore = filteredChecklistDbBefore;
    }

    public List<KpiDbSetting> getFilteredChecklistDbAfter() {
        return filteredChecklistDbAfter;
    }

    public void setFilteredChecklistDbAfter(List<KpiDbSetting> filteredChecklistDbAfter) {
        this.filteredChecklistDbAfter = filteredChecklistDbAfter;
    }

//	public AppInfo[] getInputChecklistAppBefore() {
//		return inputChecklistAppBefore;
//	}
//
//	public void setInputChecklistAppBefore(AppInfo[] inputChecklistAppBefore) {
//		this.inputChecklistAppBefore = inputChecklistAppBefore;
//	}
//
//	public AppInfo[] getInputChecklistAppAfter() {
//		return inputChecklistAppAfter;
//	}
//
//	public void setInputChecklistAppAfter(AppInfo[] inputChecklistAppAfter) {
//		this.inputChecklistAppAfter = inputChecklistAppAfter;
//	}
//
//	public DbInfo[] getInputChecklistDbBefore() {
//		return inputChecklistDbBefore;
//	}
//
//	public void setInputChecklistDbBefore(DbInfo[] inputChecklistDbBefore) {
//		this.inputChecklistDbBefore = inputChecklistDbBefore;
//	}
//
//	public DbInfo[] getInputChecklistDbAfter() {
//		return inputChecklistDbAfter;
//	}
//
//	public void setInputChecklistDbAfter(DbInfo[] inputChecklistDbAfter) {
//		this.inputChecklistDbAfter = inputChecklistDbAfter;
//	}
//
//	public List<AppResult> getChecklistAppRollback() {
//		return checklistAppRollback;
//	}
//
//	public void setChecklistAppRollback(List<AppResult> checklistAppRollback) {
//		this.checklistAppRollback = checklistAppRollback;
//	}
//
//	public AppInfo[] getInputChecklistAppRollback() {
//		return inputChecklistAppRollback;
//	}
//
//	public void setInputChecklistAppRollback(AppInfo[] inputChecklistAppRollback) {
//		this.inputChecklistAppRollback = inputChecklistAppRollback;
//	}

    public List<Checklist> getFilteredChecklistAppRollback() {
        return filteredChecklistAppRollback;
    }

    public void setFilteredChecklistAppRollback(List<Checklist> filteredChecklistAppRollback) {
        this.filteredChecklistAppRollback = filteredChecklistAppRollback;
    }

//	public List<DbResult> getChecklistDbRollback() {
//		return checklistDbRollback;
//	}
//
//	public void setChecklistDbRollback(List<DbResult> checklistDbRollback) {
//		this.checklistDbRollback = checklistDbRollback;
//	}
//
//	public DbInfo[] getInputChecklistDbRollback() {
//		return inputChecklistDbRollback;
//	}
//
//	public void setInputChecklistDbRollback(DbInfo[] inputChecklistDbRollback) {
//		this.inputChecklistDbRollback = inputChecklistDbRollback;
//	}

    public List<KpiDbSetting> getFilteredChecklistDbRollback() {
        return filteredChecklistDbRollback;
    }

    public void setFilteredChecklistDbRollback(List<KpiDbSetting> filteredChecklistDbRollback) {
        this.filteredChecklistDbRollback = filteredChecklistDbRollback;
    }

//	public List<AppResult> getChecklistAppLast() {
//		return checklistAppLast;
//	}
//
//	public void setChecklistAppLast(List<AppResult> checklistAppLast) {
//		this.checklistAppLast = checklistAppLast;
//	}
//
//	public AppInfo[] getInputChecklistAppLast() {
//		return inputChecklistAppLast;
//	}
//
//	public void setInputChecklistAppLast(AppInfo[] inputChecklistAppLast) {
//		this.inputChecklistAppLast = inputChecklistAppLast;
//	}

    public List<Checklist> getFilteredChecklistAppLast() {
        return filteredChecklistAppLast;
    }

    public void setFilteredChecklistAppLast(List<Checklist> filteredChecklistAppLast) {
        this.filteredChecklistAppLast = filteredChecklistAppLast;
    }

//	public List<DbResult> getChecklistDbLast() {
//		return checklistDbLast;
//	}
//
//	public void setChecklistDbLast(List<DbResult> checklistDbLast) {
//		this.checklistDbLast = checklistDbLast;
//	}
//
//	public DbInfo[] getInputChecklistDbLast() {
//		return inputChecklistDbLast;
//	}
//
//	public void setInputChecklistDbLast(DbInfo[] inputChecklistDbLast) {
//		this.inputChecklistDbLast = inputChecklistDbLast;
//	}

    public List<KpiDbSetting> getFilteredChecklistDbLast() {
        return filteredChecklistDbLast;
    }

    public void setFilteredChecklistDbLast(List<KpiDbSetting> filteredChecklistDbLast) {
        this.filteredChecklistDbLast = filteredChecklistDbLast;
    }

    public List<Checklist> getCklAppBeforeValues() {

        /*20181025_hoangnd_check null_start*/
        if (cklAppBefore != null && cklAppBefore.values() != null)
            return new ArrayList<>(cklAppBefore.values());
        else
            return new ArrayList<>();
        /*20181025_hoangnd_check null_end*/
    }

    public List<Checklist> getCklAppMainValues() {
        if (executeController.getSelectedRunStep() == null)
            return new ArrayList<>();

        MapEntry entryKey = executeController.getSelectedRunStep().getValue();

        /*20181019_hoangnd_continue fail step_start*/
        if (newCklAppMain != null && entryKey != null &&
                newCklAppMain.get(entryKey) != null &&
                newCklAppMain.get(entryKey).values() != null)
            return new ArrayList<>(newCklAppMain.get(entryKey).values());
        else
            return new ArrayList<>();
        /*20181019_hoangnd_continue fail step_end*/
    }

    public List<Checklist> getCklAppAfterValues() {

        /*20181025_hoangnd_check null_start*/
        if (cklAppAfter != null && cklAppAfter.values() != null)
            return new ArrayList<>(cklAppAfter.values());
        else
            return new ArrayList<>();
        /*20181025_hoangnd_check null_end*/
    }

    public List<Checklist> getCklAppRollbackValues() {
//		return new ArrayList<>(cklAppRollback.values());
        if (executeController.getSelectedRunStep() == null)
            return new ArrayList<>();

        MapEntry entryKey = executeController.getSelectedRunStep().getValue();

        /*20181019_hoangnd_check null_start*/
        if (newCklAppRollback != null && entryKey != null &&
                newCklAppRollback.get(entryKey) != null &&
                newCklAppRollback.get(entryKey).values() != null)
            return new ArrayList<>(newCklAppRollback.get(entryKey).values());
        else
            return new ArrayList<>();
        /*20181019_hoangnd_check null_end*/
    }

    public List<QueueChecklist> getCklDbBeforeValues() {

        /*20181025_hoangnd_check null_start*/
        if (cklDbBefore != null && cklDbBefore.values() != null)
            return new ArrayList<>(cklDbBefore.values());
        else
            return new ArrayList<>();
        /*20181025_hoangnd_check null_end*/
    }

    public List<QueueChecklist> getCklDbMainValues() {

        /*20181025_hoangnd_check null_start*/
        if (cklDbMain != null && cklDbMain.values() != null)
            return new ArrayList<>(cklDbMain.values());
        else
            return new ArrayList<>();
        /*20181025_hoangnd_check null_end*/
    }

    public List<QueueChecklist> getCklDbAfterValues() {

        /*20181025_hoangnd_check null_start*/
        if (cklDbAfter != null && cklDbAfter.values() != null)
            return new ArrayList<>(cklDbAfter.values());
        else
            return new ArrayList<>();
        /*20181025_hoangnd_check null_end*/
    }

    public List<QueueChecklist> getCklDbRollbackValues() {

        /*20181025_hoangnd_check null_start*/
        if (cklDbRollback != null && cklDbRollback.values() != null)
            return new ArrayList<>(cklDbRollback.values());
        else
            return new ArrayList<>();
        /*20181025_hoangnd_check null_end*/

    }

    public Multimap<Module, Checklist> getCklAppBefore() {
        return cklAppBefore;
    }

    public void setCklAppBefore(Multimap<Module, Checklist> cklAppBefore) {
        this.cklAppBefore = cklAppBefore;
    }

    public Map<MapEntry, Multimap<Module, Checklist>> getNewCklAppMain() {
        return newCklAppMain;
    }

    public void setNewCklAppMain(Map<MapEntry, Multimap<Module, Checklist>> newCklAppMain) {
        this.newCklAppMain = newCklAppMain;
    }

    /*20181115_hoangnd_save all step_start*/
    public List<Checklist> getCklAppRollbackData() {
//		return new ArrayList<>(cklAppRollback.values());
        if (historyDetailController.getSelectedRunStep() == null)
            return new ArrayList<>();

        MapEntry entryKey = historyDetailController.getSelectedRunStep().getValue();

        if (newCklAppRollback != null && entryKey != null &&
                newCklAppRollback.get(entryKey) != null &&
                newCklAppRollback.get(entryKey).values() != null)
            return new ArrayList<>(newCklAppRollback.get(entryKey).values());
        else
            return new ArrayList<>();
    }

    public List<Checklist> getCklAppMainData() {
        if (historyDetailController.getSelectedRunStep() == null)
            return new ArrayList<>();

        MapEntry entryKey = historyDetailController.getSelectedRunStep().getValue();

        if (newCklAppMain != null && entryKey != null &&
                newCklAppMain.get(entryKey) != null &&
                newCklAppMain.get(entryKey).values() != null)
            return new ArrayList<>(newCklAppMain.get(entryKey).values());
        else
            return new ArrayList<>();
    }

    public HistoryDetailController getHistoryDetailController() {
        return historyDetailController;
    }

    public void setHistoryDetailController(HistoryDetailController historyDetailController) {
        this.historyDetailController = historyDetailController;
    }
    /*20181115_hoangnd_save all step_end*/

	/*
	public Multimap<Module, Checklist> getCklAppMain() {
		return cklAppMain;
	}

	public void setCklAppMain(Multimap<Module, Checklist> cklAppMain) {
		this.cklAppMain = cklAppMain;
	}
*/

    public Multimap<Module, Checklist> getCklAppAfter() {
        return cklAppAfter;
    }

    public void setCklAppAfter(Multimap<Module, Checklist> cklAppAfter) {
        this.cklAppAfter = cklAppAfter;
    }

/*	public Multimap<Module, Checklist> getCklAppRollback() {
		return cklAppRollback;
	}

	public void setCklAppRollback(Multimap<Module, Checklist> cklAppRollback) {
		this.cklAppRollback = cklAppRollback;
	}*/

    public Map<MapEntry, Multimap<Module, Checklist>> getNewCklAppRollback() {
        return newCklAppRollback;
    }

    public void setNewCklAppRollback(Map<MapEntry, Multimap<Module, Checklist>> newCklAppRollback) {
        this.newCklAppRollback = newCklAppRollback;
    }

    public Multimap<MonitorDatabase, QueueChecklist> getCklDbBefore() {
        return cklDbBefore;
    }

    public void setCklDbBefore(Multimap<MonitorDatabase, QueueChecklist> cklDbBefore) {
        this.cklDbBefore = cklDbBefore;
    }

    public Multimap<MonitorDatabase, QueueChecklist> getCklDbMain() {
        return cklDbMain;
    }

    public void setCklDbMain(Multimap<MonitorDatabase, QueueChecklist> cklDbMain) {
        this.cklDbMain = cklDbMain;
    }

    public Multimap<MonitorDatabase, QueueChecklist> getCklDbAfter() {
        return cklDbAfter;
    }

    public void setCklDbAfter(Multimap<MonitorDatabase, QueueChecklist> cklDbAfter) {
        this.cklDbAfter = cklDbAfter;
    }

    public Multimap<MonitorDatabase, QueueChecklist> getCklDbRollback() {
        return cklDbRollback;
    }

    public void setCklDbRollback(Multimap<MonitorDatabase, QueueChecklist> cklDbRollback) {
        this.cklDbRollback = cklDbRollback;
    }

    public String getMessageModule() {
        return messageModule;
    }

    public void setMessageModule(String messageModule) {
        this.messageModule = messageModule;
    }

    public String getMessageDb() {
        return messageDb;
    }

    public void setMessageDb(String messageDb) {
        this.messageDb = messageDb;
    }

    public boolean isSufficientChecklist() {
        return sufficientChecklist;
    }

    public void setSufficientChecklist(boolean sufficientChecklist) {
        this.sufficientChecklist = sufficientChecklist;
    }
    //</editor-fold>
}
