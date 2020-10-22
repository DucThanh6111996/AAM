package com.viettel.controller;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.viettel.bean.*;
import com.viettel.exception.AppException;
import com.viettel.it.util.CommonExport;
import com.viettel.it.util.LanguageBean;
import com.viettel.it.util.MessageUtil;
import com.viettel.model.*;
import com.viettel.persistence.*;
import com.viettel.util.*;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.primefaces.component.tabview.Tab;
import org.primefaces.component.tabview.TabView;
import org.primefaces.context.RequestContext;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.util.*;

/**
 * @author quanns2
 */
@ViewScoped
@ManagedBean
public class VerifyController implements Serializable {
    private static Logger logger = LogManager.getLogger(VerifyController.class);

    private List<ExeObject> dataTable = new ArrayList<>();
    private List<ExeObject> dataTableFilter = new ArrayList<>();

    private List<ExeObject> dataTableDb = new ArrayList<>();
    private List<ExeObject> dataTableDbFilter = new ArrayList<>();

    //    private List<AppResult> checklistAppBefore;
//    private AppInfo[] inputChecklistAppBefore;
    private Collection<Checklist> filteredChecklistApp;

    //    private List<DbResult> checklistDbBefore;
//    private DbInfo[] inputChecklistDbBefore;
//    private List<DbResult> filteredChecklistDbBefore;
    private Collection<QueueChecklist> filteredChecklistDb;

    private ActionController actionController;

    private Boolean isValidApp;
    private Boolean isValidDb;
    private Boolean isValidClkApp;
    private Boolean isValidClkDb;

    private Multimap<Module, Checklist> multimap;
    //    private Multimap<ServiceDatabase, KpiDbSetting> checklistDbs;
    private Multimap<MonitorDatabase, QueueChecklist> checklistDbs;

    private List<ExeObject> checkApps;

    @ManagedProperty(value = "#{language}")
    LanguageBean languageBean;

    public LanguageBean getLanguageBean() {
        return languageBean;
    }

    public void setLanguageBean(LanguageBean languageBean) {
        this.languageBean = languageBean;
    }

    private String locate;

    @ManagedProperty(value = "#{actionDetailDatabaseController}")
    ActionDetailDatabaseController actionDetailDatabaseController;

    public void setActionDetailDatabaseController(ActionDetailDatabaseController actionDetailDatabaseController) {
        this.actionDetailDatabaseController = actionDetailDatabaseController;
    }

    @ManagedProperty(value = "#{checklistService}")
    ChecklistService checklistService;

    public void setChecklistService(ChecklistService checklistService) {
        this.checklistService = checklistService;
    }

    @ManagedProperty(value = "#{rstKpiDbSettingService}")
    RstKpiDbSettingService rstKpiDbSettingService;

    public void setRstKpiDbSettingService(RstKpiDbSettingService rstKpiDbSettingService) {
        this.rstKpiDbSettingService = rstKpiDbSettingService;
    }

    @ManagedProperty(value = "#{kpiServerSettingService}")
    KpiServerSettingService kpiServerSettingService;

    public void setKpiServerSettingService(KpiServerSettingService kpiServerSettingService) {
        this.kpiServerSettingService = kpiServerSettingService;
    }

    public void setIimService(IimService iimService) {
        this.iimService = iimService;
    }

    @ManagedProperty(value = "#{iimService}")
    IimService iimService;

    private List<ActionDetailDatabase> detailDatabases;
    private String username;

    private Long cklAppRunId;
    private Long cklDbRunId;
    private Long checkAppRunId;
    private Long checkDbRunId;

    private Boolean isRunningCklApp = Boolean.FALSE;
    private Boolean isRunningCklDb = Boolean.FALSE;
    private Boolean isRunningCheckApp = Boolean.FALSE;
    private Boolean isRunningCheckDb = Boolean.FALSE;
    private Boolean isCheckRunningApp = Boolean.FALSE;
    private Boolean isCheckRunningDb = Boolean.FALSE;
    private Boolean isCheckRunningClkApp = Boolean.FALSE;
    private Boolean isCheckRunningClkDb = Boolean.FALSE;


    @PostConstruct
    public void onStart() {

        username = SessionUtil.getCurrentUsername() == null ? "N/A" : SessionUtil.getCurrentUsername();
    }

    public Boolean checkVerify() {
        Boolean result = true;


        return result;
    }

    public void prepareValidate() {
        isCheckRunningApp = Boolean.FALSE;
        isCheckRunningDb = Boolean.FALSE;
        isCheckRunningClkApp = Boolean.FALSE;
        isCheckRunningClkDb = Boolean.FALSE;
        isValidApp = false;
        isValidDb = false;
        isValidClkApp = false;
        isValidClkDb = false;

        TabView tabView = (TabView) FacesContext.getCurrentInstance().getViewRoot().findComponent("mop").findComponent("lst").findComponent("tabVerify");

        if (dataTableDb.isEmpty()) {
            isValidDb = true;
            isCheckRunningDb = true;
            Tab tab = (Tab) tabView.findComponent("verifydbTab");
            tab.setRendered(false);
        } else {
            isValidDb = false;
            isCheckRunningDb = false;
            Tab tab = (Tab) tabView.findComponent("verifydbTab");
            tab.setRendered(true);
        }

        if (multimap.isEmpty()) {
            isValidClkApp = true;
            isCheckRunningClkApp = true;
            Tab tab = (Tab) tabView.findComponent("verifyCklAppTab");
            tab.setRendered(false);
        } else {
            isValidClkApp = false;
            isCheckRunningClkApp = false;
            Tab tab = (Tab) tabView.findComponent("verifyCklAppTab");
            tab.setRendered(true);
        }

        if (checklistDbs.isEmpty()) {
            isCheckRunningClkDb = true;
            isValidClkDb = true;
            Tab tab = (Tab) tabView.findComponent("verifyCklDbTab");
            tab.setRendered(false);
        } else {
            isValidClkDb = false;
            isCheckRunningClkDb = false;
            Tab tab = (Tab) tabView.findComponent("verifyCklDbTab");
            tab.setRendered(true);
        }
    }

    public void checkModule() {
        ImpactProcess impactProcess = actionController.getNewObj().getImpactProcess();
        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("add").queryParam("runType", AamConstants.EXE_VERIFY_CHECK_APP);
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);

        ChecklistInfo checklistInfo = new ChecklistInfo();
        //20181023_tudn_start load pass security
        checklistInfo.setUserTD(username);
        actionController.getNewObj().setUserExecute(username);
        //20181023_tudn_start load pass security
        checklistInfo.setUsername(actionController.getUsername());
        checklistInfo.setCheckApps(checkApps);
        checklistInfo.setAction(actionController.getNewObj());
        checklistInfo.setExeType(AamConstants.EXE_VERIFY_CHECK_APP);

        Response response = builder.post(Entity.json(checklistInfo));
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            isCheckRunningApp = Boolean.TRUE;
            checkAppRunId = response.readEntity(Long.class);
            isRunningCheckApp = Boolean.TRUE;

            RequestContext reqCtx = RequestContext.getCurrentInstance();

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("start.check.cr") + " " + actionController.getNewObj().getCrNumber(), "");
            FacesContext.getCurrentInstance().addMessage("verifyGrowl", msg);

            reqCtx.update(":mop:lst:verifyGrowl");
            reqCtx.execute("PF('pollapp').start()");
        } else {
            logger.error(response.getStatus());
        }
    }

    public void checkDb() {
        ImpactProcess impactProcess = actionController.getNewObj().getImpactProcess();
        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("add").queryParam("runType", AamConstants.EXE_VERIFY_CHECK_DB);
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);

        ChecklistInfo checklistInfo = new ChecklistInfo();
        //20181023_tudn_start load pass security
        checklistInfo.setUserTD(username);
        actionController.getNewObj().setUserExecute(username);
        //20181023_tudn_start load pass security
        checklistInfo.setUsername(actionController.getUsername());
        checklistInfo.setCheckDbs(dataTableDb);
        checklistInfo.setAction(actionController.getNewObj());
        checklistInfo.setExeType(AamConstants.EXE_VERIFY_CHECK_DB);


        Response response = builder.post(Entity.json(checklistInfo));
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            isCheckRunningDb = Boolean.TRUE;
            checkDbRunId = response.readEntity(Long.class);
            isRunningCheckDb = Boolean.TRUE;

            RequestContext reqCtx = RequestContext.getCurrentInstance();

            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("start.check.cr") + " " + actionController.getNewObj().getCrNumber(), "");
            FacesContext.getCurrentInstance().addMessage("verifyGrowl", msg);

            reqCtx.update(":mop:lst:verifyGrowl");
            reqCtx.execute("PF('polldb').start()");
        } else {
            logger.error(response.getStatus());
        }
    }

    public void checkCklApp() {
        ImpactProcess impactProcess = actionController.getNewObj().getImpactProcess();
        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("add").queryParam("runType", AamConstants.EXE_VERIFY_CHECKLIST_APP);
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);

        ChecklistInfo checklistInfo = new ChecklistInfo();
        //20181023_tudn_start load pass security
        checklistInfo.setUserTD(username);
        actionController.getNewObj().setUserExecute(username);
        //20181023_tudn_start load pass security
        checklistInfo.setUsername(actionController.getUsername());
        checklistInfo.setChecklistApps(multimap);
        checklistInfo.setAction(actionController.getNewObj());
        checklistInfo.setExeType(AamConstants.EXE_VERIFY_CHECKLIST_APP);

        Map<MapEntry, KpiServerSetting> kpiServerSettingMap = new HashMap<>();
        try {
//            List<KpiServerSetting> kpiServerSettings = kpiServerSettingService.findSettingForModules(new ArrayList<>(actionController.getImpactModules().keySet()));
            List<KpiServerSetting> kpiServerSettings = kpiServerSettingService.findAomSettingForModules(new ArrayList<>(actionController.getImpactModules().values()));
            if (kpiServerSettings != null) {
                for (KpiServerSetting kpiServerSetting : kpiServerSettings) {
                    kpiServerSettingMap.put(new MapEntry(kpiServerSetting.getAppId().intValue(), kpiServerSetting.getKpiId().intValue()), kpiServerSetting);
                }
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        checklistInfo.setKpiServerSettingMap(kpiServerSettingMap);

        Response response = builder.post(Entity.json(checklistInfo));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            isCheckRunningClkApp = Boolean.TRUE;
            cklAppRunId = response.readEntity(Long.class);
            isRunningCklApp = Boolean.TRUE;

            RequestContext reqCtx = RequestContext.getCurrentInstance();
            reqCtx.execute("PF('pollcklapp').start()");
            logger.info(multimap.size());
        } else {
            logger.error(response.getStatus());
        }
    }

    public void checkCklDb() {
        ImpactProcess impactProcess = actionController.getNewObj().getImpactProcess();
        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("add").queryParam("runType", AamConstants.EXE_VERIFY_CHECKLIST_DB);
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);

        ChecklistInfo checklistInfo = new ChecklistInfo();
        //20181023_tudn_start load pass security
        checklistInfo.setUserTD(username);
        actionController.getNewObj().setUserExecute(username);
        //20181023_tudn_start load pass security
        checklistInfo.setUsername(actionController.getUsername());
        checklistInfo.setChecklistDbs(checklistDbs);
        checklistInfo.setAction(actionController.getNewObj());
        checklistInfo.setExeType(AamConstants.EXE_VERIFY_CHECKLIST_DB);

        Response response = builder.post(Entity.json(checklistInfo));
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            isCheckRunningClkDb = Boolean.TRUE;
            cklDbRunId = response.readEntity(Long.class);
            isRunningCklDb = Boolean.TRUE;
            RequestContext reqCtx = RequestContext.getCurrentInstance();
            reqCtx.execute("PF('pollckldb').start()");
            logger.info(multimap.size());
        } else {
            logger.error(response.getStatus());
        }
    }

    public void pollListener() {
        Long startTime = System.currentTimeMillis();
        RequestContext reqCtx = RequestContext.getCurrentInstance();

        ImpactProcess impactProcess = actionController.getNewObj().getImpactProcess();
        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("get").path(checkAppRunId.toString());
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);

        Response response = builder.get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            ChecklistInfo checklistInfo = response.readEntity(ChecklistInfo.class);
            logger.info(checklistInfo.getCheckApps().size() + "\t" + (System.currentTimeMillis() - startTime));
            checkApps = checklistInfo.getCheckApps();
            dataTableFilter = checklistInfo.getCheckApps();

            if (checklistInfo.getEndTime() != null) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("check.app.complete") + " " + actionController.getNewObj().getCrNumber(), "");
                FacesContext.getCurrentInstance().addMessage("verifyGrowl", msg);

                isValidApp = true;
                for (ExeObject exeObject : checkApps) {
                    if (exeObject.getRunStt() == null || !exeObject.getRunStt().equals(Constant.FINISH_SUCCESS_STATUS)) {
                        isValidApp = false;
                        break;
                    }
                }
                isRunningCheckApp = Boolean.FALSE;
                reqCtx.execute("PF('pollapp').stop()");
                reqCtx.update("mop:lst:processSelectAgain");
            }

            reqCtx.update(":mop:lst:verifyGrowl");
            reqCtx.update("mop:lst:tabVerify:verifyAppMsg");
        } else {
            logger.error(response.getStatus());
        }
    }

    public void pollDbListener() {
        RequestContext reqCtx = RequestContext.getCurrentInstance();

        ImpactProcess impactProcess = actionController.getNewObj().getImpactProcess();
        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("get").path(checkDbRunId.toString());
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);

        Response response = builder.get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            ChecklistInfo checklistInfo = response.readEntity(ChecklistInfo.class);

            logger.info("checklistInfo.getCheckDbs() :" + (checklistInfo.getCheckDbs() != null ? checklistInfo.getCheckDbs().size() : "null"));
            dataTableDb = checklistInfo.getCheckDbs();
            dataTableFilter = checklistInfo.getCheckApps();

            if (checklistInfo.getEndTime() != null) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("check.db.complete") + " " + actionController.getNewObj().getCrNumber(), "");
                FacesContext.getCurrentInstance().addMessage("verifyGrowl", msg);

                isValidDb = true;
                for (ExeObject exeObject : dataTableDb) {
                    if (exeObject.getRunStt() == null || !exeObject.getRunStt().equals(Constant.FINISH_SUCCESS_STATUS)) {
                        isValidDb = false;
                        break;
                    }
                }
                isRunningCheckDb = Boolean.FALSE;
                reqCtx.execute("PF('polldb').stop()");
                reqCtx.update("mop:lst:processSelectAgain");
            }

            reqCtx.update(":mop:lst:verifyGrowl");
            reqCtx.update("mop:lst:tabVerify:verifyDbMsg");
        } else {
            logger.error(response.getStatus());
        }
    }

    public void pollCklAppListener() {
        RequestContext reqCtx = RequestContext.getCurrentInstance();

        ImpactProcess impactProcess = actionController.getNewObj().getImpactProcess();
        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("get").path(cklAppRunId.toString());
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);

        Response response = builder.get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            ChecklistInfo checklistInfo = response.readEntity(ChecklistInfo.class);

            if (checklistInfo != null) {
                logger.info(checklistInfo.getChecklistApps().size());
                multimap = checklistInfo.getChecklistApps();
                filteredChecklistApp = multimap.values();
                if (checklistInfo.getEndTime() != null) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("check.list.app.complete") + " " + actionController.getNewObj().getCrNumber(), "");
                    FacesContext.getCurrentInstance().addMessage("verifyGrowl", msg);

                    isValidClkApp = true;
                    for (Checklist checklist : multimap.values()) {
                        if (checklist.getResult().getStatus() == null || !checklist.getResult().getStatus().equals(1)) {
                            isValidClkApp = false;
                            break;
                        }
                    }
                    isRunningCklApp = Boolean.FALSE;
                    reqCtx.execute("PF('pollcklapp').stop()");
                    reqCtx.update("mop:lst:processSelectAgain");
                }

                reqCtx.update("mop:lst:verifyGrowl");
                reqCtx.update("mop:lst:tabVerify:verifyCklAppMsg");
            }
        } else {
            logger.error(response.getStatus());
        }
//        }
    }

    public Collection<Checklist> getCklApps() {
        if (multimap == null)
            return new ArrayList<>();
        return multimap.values();
    }

    public Collection<QueueChecklist> getCklDbs() {
        if (checklistDbs == null)
            return new ArrayList<>();
        return checklistDbs.values();
    }

    public void pollCklDbListener() {
        RequestContext reqCtx = RequestContext.getCurrentInstance();

        ImpactProcess impactProcess = actionController.getNewObj().getImpactProcess();
        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("get").path(cklDbRunId.toString());
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);

        Response response = builder.get();
        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            ChecklistInfo checklistInfo = response.readEntity(ChecklistInfo.class);

            if (checklistInfo != null) {
                logger.info(checklistInfo.getChecklistDbs().size());
                checklistDbs = checklistInfo.getChecklistDbs();
                filteredChecklistDb = checklistDbs.values();
                if (checklistInfo.getEndTime() != null) {
                    FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("check.list.db.complete") + " " + actionController.getNewObj().getCrNumber(), "");
                    FacesContext.getCurrentInstance().addMessage("verifyGrowl", msg);

                    isValidClkDb = true;
                    for (QueueChecklist kpiDbSetting : checklistDbs.values()) {
                        if (kpiDbSetting.getResult().getStatus() == null || !kpiDbSetting.getResult().getStatus().equals(1)) {
                            isValidClkDb = false;
                            break;
                        }
                    }
                    isRunningCklDb = Boolean.FALSE;
                    reqCtx.execute("PF('pollckldb').stop()");
                    reqCtx.update("mop:lst:processSelectAgain");
                }

                reqCtx.update("mop:lst:verifyGrowl");
                reqCtx.update("mop:lst:tabVerify:verifyCklDbMsg");
            }
        } else {
            logger.error(response.getStatus());
        }
    }

    public Boolean renderCheckApp(Integer type) {
//        return ThreadVerifyManager.getInstance().getExeCommandThread(username, type) == null ? Boolean.FALSE : Boolean.TRUE;
        if (type.equals(0)) {
            return isRunningCheckApp;
        } else if (type.equals(1)) {
            return isRunningCheckDb;
        } else if (type.equals(2)) {
            return isRunningCklApp;
        } else if (type.equals(3)) {
            return isRunningCklDb;
        }
        return Boolean.FALSE;
    }

    // Lay ra template mau va export ra file excel day du
    public StreamedContent onExport() throws Exception {
        String pathOut = CommonExport.getPathToExportFile(MessageUtil.getResourceBundleMessage("Template_Export_Verify_Checklist"));
        Workbook workbook = null;
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();

        String pathTemplate = ctx.getRealPath("/")
                + File.separator + "templates" + File.separator + MessageUtil.getResourceBundleMessage("Template_Export_Verify_Checklist_MOP");
        try {
            workbook = exportWorkbook(pathTemplate);
            try {
                FileOutputStream fileOut = new FileOutputStream(pathOut);
                workbook.write(fileOut);
                workbook.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        File file = new File(pathOut);
        return new DefaultStreamedContent(new FileInputStream(file), ".xlsx", file.getName());
    }

    public Workbook exportWorkbook(String templatePath) {
        Workbook workbook = null;
        try {
            String pathTemplate = templatePath;
            InputStream fileTemplate = new FileInputStream(pathTemplate);
            workbook = WorkbookFactory.create(fileTemplate);
            HashMap<Integer, List<String>> mapData = new HashMap<>();
            HashMap<Integer, List<String>> mapData1 = new HashMap<>();
            HashMap<Integer, List<String>> mapData2 = new HashMap<>();
            HashMap<Integer, List<String>> mapData3 = new HashMap<>();
            List<String> listData;
            Integer row = 5;
            for (ExeObject exeObject : checkApps) {
                listData = new ArrayList<>();
                listData.add(exeObject.getModule().getServiceCode());
                listData.add(exeObject.getModule().getServiceName());
                listData.add(exeObject.getModule().getModuleCode());
                listData.add(exeObject.getModule().getModuleName());
                listData.add(getInfoApp(exeObject.getModule()));
                listData.add(exeObject.getLog() == null ? "" : (exeObject.getLog().length() < 32767 ? exeObject.getLog().toString() : exeObject.getLog().toString().substring(0, 32767)));
                listData.add(exeObject.getRunStt() == null ? "" : (exeObject.getRunStt() == 0 ? "Waiting" : (exeObject.getRunStt() == 1 ? "Execute" : (exeObject.getRunStt() == 2 ? "Success" : "Failed"))));
                mapData.put(row, listData);
                row++;
            }
            putDataToRow(workbook,0,0,mapData);
            row = 5;
            for (ExeObject exeObject : dataTableDb) {
                listData = new ArrayList<>();
                listData.add(getInfoDb(exeObject.getActionDatabase().getServiceDatabase()));
                listData.add(getTableDetail(exeObject));
                listData.add(exeObject.getLog() == null ? "" : (exeObject.getLog().length() < 32767 ? exeObject.getLog().toString() : exeObject.getLog().toString().substring(0, 32767)));
                listData.add(exeObject.getRunStt() == null ? "" : (exeObject.getRunStt() == 0 ? "Waiting" : (exeObject.getRunStt() == 1 ? "Execute" : (exeObject.getRunStt() == 2 ? "Success" : "Failed"))));
                mapData1.put(row, listData);
                row++;
            }
            putDataToRow(workbook, 1, 0, mapData1);
            row = 5;
            int i = 1;
            for (Checklist checklist : getCklApps()) {
                listData = new ArrayList<>();
                listData.add(String.valueOf(i));
                listData.add(checklist.getModule().getModuleCode());
                listData.add(checklist.getModule().getModuleName());
                listData.add(checklist.getModule().getServiceCode());
                listData.add(checklist.getModule().getServiceName());
                listData.add(checklist.getName());
                if (isCheckRunningClkApp) {
                    listData.add(checklist.getResult().getLog() == null ? "" : (checklist.getResult().getLog().length() < 32767 ? checklist.getResult().getLog() : checklist.getResult().getLog().substring(0, 32767)));
                    listData.add(checklist.getResult().getMathOption() == null ? "" : getMathOperationText(checklist.getResult().getMathOption()));
                    listData.add(checklist.getResult().getOperationData() == null ? "" : checklist.getResult().getOperationData());
                    listData.add(checklist.getResult().getThreholdValue() == null ? "" : checklist.getResult().getThreholdValue());
                    listData.add(checklist.getResult().getStatus() == null ? "" : (checklist.getResult().getStatus() == 0 ? "Not OK" : "OK"));
                }
                mapData2.put(row, listData);
                row++;
                i++;
            }
            putDataToRow(workbook, 2, 0, mapData2);
            row = 5;
            i = 1;
            for (QueueChecklist queueChecklist : getCklDbs()) {
                listData = new ArrayList<>();
                listData.add(String.valueOf(i));
                listData.add(queueChecklist.getMonitorDatabase().getDbName());
                listData.add(queueChecklist.getQueueCode());
                if (isCheckRunningClkDb) {
                    listData.add(queueChecklist.getResult().getLog() == null ? "" : (queueChecklist.getResult().getLog().length() < 32767 ? queueChecklist.getResult().getLog() : queueChecklist.getResult().getLog().substring(0, 32767)));
                    listData.add(queueChecklist.getResult().getMathOption() == null ? "" : queueChecklist.getResult().getMathOption().toString());
                    listData.add(queueChecklist.getResult().getOperationData() == null ? "" : queueChecklist.getResult().getOperationData());
                    listData.add(queueChecklist.getResult().getThreholdValue() == null ? "" : queueChecklist.getResult().getThreholdValue());
                    listData.add(queueChecklist.getResult().getStatus() == null ? "" : (queueChecklist.getResult().getStatus() == 0 ? "Not OK" : "OK"));
                }
                mapData3.put(row, listData);
                row++;
                i++;
            }
            putDataToRow(workbook, 3, 0, mapData3);
        } catch (Exception ex) {
            logger.error(ex.getMessage(),ex);
        }
        return workbook;
    }

    public void putDataToRow(Workbook workbook, int sheetNumber, int columnNumber, Map<Integer,List<String>> mapData){
        Font font = workbook.createFont();
        font.setFontName("Times New Roman");
        CellStyle cellStyleLeft = workbook.createCellStyle();
        cellStyleLeft.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        cellStyleLeft.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        cellStyleLeft.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellStyleLeft.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellStyleLeft.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellStyleLeft.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellStyleLeft.setFont(font);
        cellStyleLeft.setWrapText(false);
        Cell cell;
        Row row;
        Sheet worksheet = workbook.getSheetAt(sheetNumber);
        int currentColumnData = columnNumber;
        for(Integer rowNumber : mapData.keySet()){
            row = worksheet.createRow(rowNumber);
            for(int i = 0; i < mapData.get(rowNumber).size(); i++){
                cell = row.createCell(currentColumnData);
                cell.setCellValue(mapData.get(rowNumber).get(i));
                cell.setCellStyle(cellStyleLeft);
                currentColumnData++;
            }
            currentColumnData = columnNumber;
        }
    }

    public String getMathOperationText(Integer mathOperation) {
        MessageUtil m = new MessageUtil();
        switch(mathOperation) {
            case 1:
                return m.getResourceBundleMessage("checklist.operation.1");
            case 2:
                return m.getResourceBundleMessage("checklist.operation.2");
            case 3:
                return m.getResourceBundleMessage("checklist.operation.3");
            case 4:
                return m.getResourceBundleMessage("checklist.operation.4");
            case 5:
                return m.getResourceBundleMessage("checklist.operation.5");
            case 6:
                return m.getResourceBundleMessage("checklist.operation.6");
            case 7:
                return m.getResourceBundleMessage("checklist.operation.7");
            case 8:
                return m.getResourceBundleMessage("checklist.operation.8");
            case 9:
                return m.getResourceBundleMessage("checklist.operation.9");
            case 10:
                return m.getResourceBundleMessage("checklist.operation.10");
            case 11:
                return m.getResourceBundleMessage("checklist.operation.11");
            case -1:
                return m.getResourceBundleMessage("checklist.operation.-1");
            case -2:
                return m.getResourceBundleMessage("checklist.operation.-2");
            case -3:
                return m.getResourceBundleMessage("checklist.operation.-3");
            case -4:
                return m.getResourceBundleMessage("checklist.operation.-4");
            case -5:
                return m.getResourceBundleMessage("checklist.operation.-5");
            case -6:
                return m.getResourceBundleMessage("checklist.operation.-6");
            case -7:
                return m.getResourceBundleMessage("checklist.operation.-7");
            case -8:
                return m.getResourceBundleMessage("checklist.operation.-8");
            case -9:
                return m.getResourceBundleMessage("checklist.operation.-9");
            case -10:
                return m.getResourceBundleMessage("checklist.operation.-10");
            case -11:
                return MessageUtil.getResourceBundleMessage("checklist.operation.-11");
            default:
                return "";
        }
    }

    public String getInfoApp(Module app) {
        if (app == null)
            return "";
        /*LogOs logOs = null;
        try {
            logOs = iimService.findLogByModule(app.getModuleId(), Constant.KEY_LOG_START);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }*/
        String info = "Module: " + app.getModuleCode() + "\n" +
                "Ip: " + app.getIpServer() + "\n" +
                "User: " + app.getUsername() + "\n" +
                MessageUtil.getResourceBundleMessage("path") + ": " + app.getExecutePath();
//                if (!app.getAppTypeCode().equals("CODE_TAP_TRUNG")) {
        info += "\n" + ((StringUtils.isNotEmpty(app.getStartService()) && !Constant.NA_VALUE.equals(app.getStartService())) ? MessageUtil.getResourceBundleMessage("command.start") + ": " + app.getStartService() + "\n" : "") +
                ((StringUtils.isNotEmpty(app.getStopService()) && !Constant.NA_VALUE.equals(app.getStopService())) ? MessageUtil.getResourceBundleMessage("command.stop") + ": " + app.getStopService() + "\n" : "") +
                ((StringUtils.isNotEmpty(app.getRestartService()) && !Constant.NA_VALUE.equals(app.getRestartService())) ? MessageUtil.getResourceBundleMessage("command.restart") + ": " + app.getRestartService() + "\n" : "");

        if (StringUtils.isNotEmpty(app.getFullLogStartPath())) {
            info += "\nLog start: " + app.getFullLogStartPath();
        }
//                }

        return info;
    }

    public String getInfoDb(ServiceDatabase db) {
        String info = "Db: " + db.getDbName() + "\n" +
                "Ip: " + db.getIpVirtual() + "\n" +
                "User: " + db.getUsername() + "\n" +
                MessageUtil.getResourceBundleMessage("connection.string") + ": \n" + db.getUrl();

        return info;
    }

    public List<ExeObject> getDataTable() {
        return dataTable;
    }

    public void setDataTable(List<ExeObject> dataTable) {
        this.dataTable = dataTable;
    }

    public void setListModules(ActionController actionController) {
        this.actionController = actionController;
        this.dataTable = new ArrayList<>();
        this.dataTableFilter = new ArrayList<>();
        checkApps = new ArrayList<>();
        Collection<Module> modules = actionController.getImpactModules().values();

        for (Module app : modules) {
            ExeObject rstObject = new ExeObject();
            rstObject.setCreateUser(SessionUtil.getCurrentUsername() == null ? "N/A" : SessionUtil.getCurrentUsername());
            rstObject.setModule(app);
            rstObject.setRunStt(Constant.STAND_BY_STATUS);

            dataTable.add(rstObject);
//            dataTableFilter.add(rstObject);
        }

        Multimap<Long, String> moduleActions = HashMultimap.create();
        for (ActionDetailApp action : actionController.getActionDetailAppController().getListDetailsApp()) {
            moduleActions.put(action.getModuleId(), action.getGroupAction());
        }

        for (Map.Entry<Long, Collection<String>> entry : moduleActions.asMap().entrySet()) {
            /*20181109_hoangnd_save all step_start*/
            if (entry.getKey() != null && !entry.getKey().equals(0L)) {
                ExeObject executeObject = new ExeObject();
                executeObject.setModule(actionController.getImpactModules().get(entry.getKey()));
                executeObject.setActions(new ArrayList<>(entry.getValue()));

                checkApps.add(executeObject);
                dataTableFilter.add(executeObject);
            }
            /*20181109_hoangnd_save all step_end*/
        }

        detailDatabases = actionDetailDatabaseController.getDetailDatabases();
    }

    public void loadVerifyDb() {
        if (detailDatabases != null) {
            Map<ServiceDatabase, ExeObject> dbExeObjectMap = new HashMap<>();
            Multimap<ServiceDatabase, ActionDetailDatabase> detailDatabaseMultimap = HashMultimap.create();
            for (ActionDetailDatabase detailDatabase : detailDatabases) {
                ServiceDatabase serviceDatabase;
                try {
                    serviceDatabase = iimService.findServiceDbById(actionController.getNewObj().getImpactProcess().getNationCode(), detailDatabase.getAppDbId());

                    detailDatabase.setServiceDatabase(serviceDatabase);
                    detailDatabaseMultimap.put(serviceDatabase, detailDatabase);

                    ExeObject exeObject = dbExeObjectMap.get(serviceDatabase);

                    if (exeObject == null) {
                        exeObject = new ExeObject();
//                    exeObject.setAction(actionController.getNewObj());
//                    exeObject.setServiceDb(detailDatabase.getServiceDb());
                        exeObject.setRunStt(Constant.STAND_BY_STATUS);
//                    exeObject.setCreateUser(SessionUtil.getCurrentUsername() == null ? "N/A" : SessionUtil.getCurrentUsername());
//                    havt
                        exeObject.setActionDatabase(detailDatabase);
//                    exeObject.setDetailDatabases(detailDatabaseMultimap.get(detailDatabase.getServiceDb()));
                    }

                    Multimap<String, String> tables = getTable(detailDatabase, 0);
                    exeObject.getBackupTables().putAll(tables);
                    tables = getTable(detailDatabase, 1);
                    exeObject.getExecuteTables().putAll(tables);
                    tables = getTable(detailDatabase, 2);
                    exeObject.getRollbackTables().putAll(tables);

                    dbExeObjectMap.put(serviceDatabase, exeObject);
                } catch (AppException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            dataTableDb = new ArrayList<>(dbExeObjectMap.values());
        } else {
            dataTableDb = new ArrayList<>();
        }
    }

    public void loadChecklist() {
        multimap = HashMultimap.create();
        checklistDbs = HashMultimap.create();

        RstKpiService kpiService = new RstKpiDaoImpl();
        List<Long> rstKpiIds = null;
        try {
            rstKpiIds = kpiService.getKpiByCode(Arrays.asList(AamConstants.CHECKLIST_CODE.LIVE_OR_DIE, AamConstants.CHECKLIST_CODE.CPU_MODULE, AamConstants.CHECKLIST_CODE.RAM_MODULE, AamConstants.CHECKLIST_CODE.CHECK_ERROR_LOG));
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        if (rstKpiIds == null)
            return;

        if (actionController.getNewObj() != null) {
            try {
                Multimap<Long, Long> ckls = HashMultimap.create();

                HashMap<Long, HashSet<Long>> appChecklistMp = actionController.getSelectAppKpiMap();
                if (appChecklistMp != null) {
                    for (Long key : appChecklistMp.keySet()) {
                        HashSet<Long> vals = appChecklistMp.get(key);
                        for (Long val : vals) {
                            ckls.put(key, val);
                        }

                    }
                }

                for (Map.Entry<Long, Collection<Long>> entry : ckls.asMap().entrySet()) {
                    Module module = iimService.findModuleById(actionController.getNewObj().getImpactProcess().getNationCode(), entry.getKey());

                    if (module == null)
                        continue;

                    List<ActionDetailApp> moduleStop = actionController.getActionDetailAppController().getLstStop();
                    List<ActionDetailApp> moduleStart = actionController.getActionDetailAppController().getLstStart();
                    int stopCount = 0;
                    int startCount = 0;
                    for (ActionDetailApp detailApp : moduleStop) {
                        if (detailApp.getModuleId().equals(entry.getKey()))
                            stopCount++;
                    }

                    for (ActionDetailApp detailApp : moduleStart) {
                        if (detailApp.getModuleId().equals(entry.getKey()))
                            startCount++;
                    }

                    List<Checklist> checklists = checklistService.findCheckListByAction(entry.getValue());

                    for (Checklist checklist : checklists) {
                        AppResult appResult = new AppResult();
                        appResult.setAppCode(module.getModuleCode());
                        appResult.setAppName(module.getModuleName());
                        appResult.setAppGroupCode(module.getServiceCode());
                        appResult.setAppGroupName(module.getServiceName());
                        appResult.setAppId(module.getModuleId());
                        appResult.setKpiName(checklist.getName());

                        if (stopCount == 0 && startCount != 0 && rstKpiIds.contains(checklist.getId())) {
                            //chi co start
                        } else {
                           /* Checklist checklistTmp = null;
                            try {
                                checklistTmp = (Checklist) BeanUtils.cloneBean(checklist);
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
//                            checklistAppBefore.add(appResult);
                            if (checklistTmp != null) {
                                checklistTmp.setModule(module);
                                multimap.put(module, checklistTmp);
                            }*/

                            // Start anhnt2 - 08/08/2018 - Checklist app: By account
                            if (module.getOsType()!= null && module.getOsType().equals(AamConstants.OS_TYPE.WINDOWS)) {
                                if (actionController.mapUsernames.get(module.getModuleId()) != null) {
                                    List<OsAccount> lstOsAccounts = new IimClientServiceImpl().findOsAccount(actionController.getNewObj().getImpactProcess().getNationCode(), module.getIpServer());
                                    for (OsAccount account : lstOsAccounts) {
                                        if (account.getUsername() != null && account.getUsername().equalsIgnoreCase(actionController.mapUsernames.get(module.getModuleId()))) {
                                            module.setUsername(account.getUsername());
                                            module.setPassword(account.getPassword());
                                            break;
                                        }
                                    }
                                }
                            }
                            // End anhnt2 - 08/08/2018 - Checklist app: By account

                            checklist.setModule(module);
                            multimap.put(module, checklist);
                        }
                    }
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        }

//        checklistDbBefore = new ArrayList<>();

        if (actionController.getNewObj() != null) {
            try {
                Multimap<Long, Long> ckls = HashMultimap.create();
                TreeNode[] cklDbListSelectedNodes = actionController.getCklDbListSelectedNodes();
                if (cklDbListSelectedNodes != null)
                    for (TreeNode treeNode : cklDbListSelectedNodes) {

                        if (((TreeObject) treeNode.getData()).getObj() instanceof QueueChecklist) {
                            ckls.put(((QueueChecklist) ((TreeObject) treeNode.getData()).getObj()).getQltnDbId(), ((QueueChecklist) ((TreeObject) treeNode.getData()).getObj()).getQueueId());
                        }
                    }

                for (Map.Entry<Long, Collection<Long>> entry : ckls.asMap().entrySet()) {
                    if (entry.getKey() == null)
                        continue;
//                    ServiceDatabase database = iimService.findServiceDbById(entry.getKey());
                    List<MonitorDatabase> databases = iimService.findDbMonitor(actionController.getNewObj().getImpactProcess().getNationCode(), actionController.getNewObj().getImpactProcess().getUnitId(), Arrays.asList(entry.getKey()));
                    MonitorDatabase database = databases.get(0);

//                    List<Checklist> checklists = checklistService.findCheckListByAction(entry.getValue());
                    List<QueueChecklist> checklists = actionController.aomClientService.findChecklistQueueByIds(new ArrayList<>(entry.getValue()));

                    for (QueueChecklist checklist : checklists) {
                        /*DbResult dbResult = new DbResult();
                        dbResult.setAppGroupId(new Long(database.getServiceId()));
                        dbResult.setAppGroupCode(database.getServiceCode());
                        dbResult.setAppGroupName(database.getServiceName());
                        dbResult.setDbName(database.getDbName());
                        dbResult.setDbCode(database.getDbCode());
                        dbResult.setKpiName(checklist.getName());
                        checklistDbBefore.add(dbResult);*/

//                        KpiDbSetting dbSetting = rstKpiDbSettingService.findbyKpiId(checklist.getId(), new Long(database.getServiceDbId()));
//                        dbSetting.setServiceDatabase(database);
                        checklist.setMonitorDatabase(database);
                        checklistDbs.put(database, checklist);
                    }
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        }

        filteredChecklistDb = new ArrayList<>(checklistDbs.values());
        filteredChecklistApp = new ArrayList<>(multimap.values());
    }

    public String getTableDetail(ExeObject exeObject) {
        String result = "";
        String table = getTableDetail(exeObject, 0);
        if (StringUtils.isNotEmpty(table)) {
            result += "***************BACKUP***************\n" + table;
        }

        table = getTableDetail(exeObject, 1);
        if (StringUtils.isNotEmpty(table)) {
            result += "***************EXECUTE***************\n" + table;
        }

        table = getTableDetail(exeObject, 2);
        if (StringUtils.isNotEmpty(table)) {
            result += "***************ROLLBACK***************\n" + table;
        }

        return result;
    }

    public String getTableDetail(ExeObject exeObject, Integer type) {
        String table = "";

        Multimap<String, String> tables = HashMultimap.create();

        switch (type) {
            case 0:
                tables = exeObject.getBackupTables();
                break;
            case 1:
                tables = exeObject.getExecuteTables();
                break;
            case 2:
                tables = exeObject.getRollbackTables();
                break;
            default:
                break;
        }

        for (Map.Entry<String, Collection<String>> entry : tables.asMap().entrySet()) {
            table += entry.getKey() + ": " + Joiner.on(", ").join(entry.getValue()) + "\n";
        }

        return table;
    }

    public Multimap<String, String> getTable(ActionDetailDatabase database, Integer type) {
        Multimap<String, String> backupTables = HashMultimap.create();
        String sql = "";
        String file = "";
        switch (type) {
            case 0:
                sql = database.getBackupText();
                file = database.getScriptBackup();
                break;
            case 1:
                sql = database.getScriptText();
                file = database.getScriptExecute();
                break;
            case 2:
                sql = database.getRollbackText();
                file = database.getRollbackFile();
                break;
            default:
                break;
        }

        if (database.getTypeImport() == 1) {
            try {
                sql = FileUtils.readFileToString(new File(UploadFileUtils.getDatabaseFolder(actionController.getNewObj()) + File.separator + file));
            } catch (IOException e) {
                logger.error(e.getMessage());
                logger.debug(e.getMessage(), e);
            }
        }

        try {
            if (StringUtils.isNotEmpty(sql))
                SqlUtils.splitSqlScript(sql, ';', backupTables);
        } catch (Exception e) {
            logger.error(e.getMessage());
            logger.debug(e.getMessage(), e);
        }

        return backupTables;
    }

    public List<ActionDetailDatabase> getDetailDatabases() {
        return detailDatabases;
    }


    public String getTableBackup(ActionDetailDatabase database) {
        Multimap<String, String> backupTables = HashMultimap.create();
        String result = "";
        String sql = database.getBackupText();
        if (database.getTypeImport() == 1) {
            try {
                sql = FileUtils.readFileToString(new File(UploadFileUtils.getDatabaseFolder(actionController.getNewObj()) + File.separator + database.getScriptBackup()));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        try {
            if (StringUtils.isNotEmpty(sql))
                SqlUtils.splitSqlScript(sql, ';', backupTables);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        for (Map.Entry<String, Collection<String>> entry : backupTables.asMap().entrySet()) {
            result += entry.getKey() + ": " + Joiner.on(", ").join(entry.getValue()) + "\n";
        }

        return result;
    }

    public String getTableExecute(ActionDetailDatabase database) {
        Multimap<String, String> backupTables = HashMultimap.create();
        String result = "";
        String sql = database.getScriptText();
        if (database.getTypeImport() == 1) {
            try {
                sql = FileUtils.readFileToString(new File(UploadFileUtils.getDatabaseFolder(actionController.getNewObj()) + File.separator + database.getScriptExecute()));
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        try {
            if (StringUtils.isNotEmpty(sql))
                SqlUtils.splitSqlScript(sql, ';', backupTables);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        for (Map.Entry<String, Collection<String>> entry : backupTables.asMap().entrySet()) {
            result += entry.getKey() + ": " + Joiner.on(", ").join(entry.getValue()) + "\n";
        }

        return result;
    }

    public String getTableRollback(ActionDetailDatabase database) {
        Multimap<String, String> backupTables = HashMultimap.create();
        String result = "";
        String sql = database.getRollbackText();
        if (database.getTypeImport() == 1) {
            try {
                File file = new File(UploadFileUtils.getDatabaseFolder(actionController.getNewObj()) + File.separator + database.getRollbackFile());
                sql = FileUtils.readFileToString(file);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        try {
            if (StringUtils.isNotEmpty(sql))
                SqlUtils.splitSqlScript(sql, ';', backupTables);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        for (Map.Entry<String, Collection<String>> entry : backupTables.asMap().entrySet()) {
            result += entry.getKey() + ": " + Joiner.on(", ").join(entry.getValue()) + "\n";
        }

        return result;
    }

    public List<ExeObject> getDataTableFilter() {
        return dataTableFilter;
    }

    public void setDataTableFilter(List<ExeObject> dataTableFilter) {
        this.dataTableFilter = dataTableFilter;
    }

    public List<ExeObject> getDataTableDbFilter() {
        return dataTableDbFilter;
    }

    public void setDataTableDbFilter(List<ExeObject> dataTableDbFilter) {
        this.dataTableDbFilter = dataTableDbFilter;
    }

    public List<ExeObject> getDataTableDb() {
        return dataTableDb;
    }

    public void setDataTableDb(List<ExeObject> dataTableDb) {
        this.dataTableDb = dataTableDb;
    }

    public Collection<Checklist> getFilteredChecklistApp() {
        return filteredChecklistApp;
    }

    public void setFilteredChecklistApp(Collection<Checklist> filteredChecklistApp) {
        this.filteredChecklistApp = filteredChecklistApp;
    }

    public Boolean getValid() {
        return isValidApp && isValidDb && isValidClkApp && isValidClkDb;
    }

    public List<ExeObject> getCheckApps() {
        return checkApps;
    }

    public Collection<QueueChecklist> getFilteredChecklistDb() {
        return filteredChecklistDb;
    }

    public void setFilteredChecklistDb(Collection<QueueChecklist> filteredChecklistDb) {
        this.filteredChecklistDb = filteredChecklistDb;
    }

    public Multimap<MonitorDatabase, QueueChecklist> getChecklistDbs() {
        return checklistDbs;
    }

    public void setChecklistDbs(Multimap<MonitorDatabase, QueueChecklist> checklistDbs) {
        this.checklistDbs = checklistDbs;
    }

    public Boolean getDisableCountrySelect() {
        return !((!isValidApp && isCheckRunningApp)
                || (!isValidDb && isCheckRunningDb)
                || (!isValidClkApp && isCheckRunningClkApp)
                || (!isValidClkDb && isCheckRunningClkDb))
                ;
    }

    public static void main(String[] args) {

    }
}
