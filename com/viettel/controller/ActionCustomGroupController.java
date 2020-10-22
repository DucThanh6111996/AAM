package com.viettel.controller;

// Created Sep 12, 2016 1:55:33 PM by quanns2

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
//import com.mchange.v2.collection.MapEntry;
import com.viettel.bean.MapEntry;
import com.viettel.bean.ServiceDatabase;
import com.viettel.bean.TreeObject;
import com.viettel.exception.AppException;
import com.viettel.exception.SysException;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.util.*;
import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;

import com.viettel.it.util.MessageUtil;
import com.viettel.model.*;
import com.viettel.util.*;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.*;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.DiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.FlowChartConnector;
import org.primefaces.model.diagram.endpoint.BlankEndPoint;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.primefaces.model.diagram.overlay.ArrowOverlay;
import org.primefaces.model.diagram.overlay.LabelOverlay;

/**
 * @author quanns2
 */
@ViewScoped
@ManagedBean
public class ActionCustomGroupController implements Serializable {
    private static Logger logger = Logger.getLogger(ActionCustomGroupController.class);

    @ManagedProperty(value = "#{actionCustomGroupService}")
    ActionCustomGroupService actionCustomGroupService;

    public void setActionCustomGroupService(ActionCustomGroupService actionCustomGroupService) {
        this.actionCustomGroupService = actionCustomGroupService;
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

    private ActionController actionController;

    private List<ActionCustomGroup> customGroups;
    private ActionCustomGroup selectedObj;
    private ActionCustomAction selectedActionObj;
    private ActionCustomGroup newObj;
    private ActionCustomAction newActionObj;

    private boolean isEdit;

    private Long searchId;
    private Long searchActionId;
    private String searchName;
    private String searchAfterGroup;

    private DefaultDiagramModel model;
    private DefaultDiagramModel rollbackModel;

    private List<SelectItem> actionsSteps;
    private List<SelectItem> actionsRollbackSteps;
    private List<SelectItem> appGroups;

    private TreeNode upcodeRoot;
    private TreeNode selectedUpcodeDir;

    private TreeNode removeUpcodeRoot;
    private TreeNode selectedRemoveUpcodeFile;

    private Long selectedAppGroupId;

    private List<ActionDtFile> actionDtFiles;

    @PostConstruct
    public void onStart() {
        clear();
        clearAction();

        customGroups = new ArrayList<>();

        actionsSteps = new ArrayList<>();
        actionsRollbackSteps = new ArrayList<>();

        actionsSteps.add(new SelectItem(Constant.SUB_STEP_CHECK_STATUS, Constant.getSteps().get(new MapEntry(Constant.SUB_STEP_CHECK_STATUS, 1)).getLabel()));
        actionsSteps.add(new SelectItem(Constant.SUB_STEP_STOP_APP, Constant.getSteps().get(new MapEntry(Constant.SUB_STEP_STOP_APP, 1)).getLabel()));
        actionsSteps.add(new SelectItem(Constant.SUB_STEP_BACKUP_APP, Constant.getSteps().get(new MapEntry(Constant.SUB_STEP_BACKUP_APP, 1)).getLabel()));
        actionsSteps.add(new SelectItem(Constant.SUB_STEP_BACKUP_DB, Constant.getSteps().get(new MapEntry(Constant.SUB_STEP_BACKUP_DB, 1)).getLabel()));
        actionsSteps.add(new SelectItem(Constant.SUB_STEP_UPCODE, Constant.getSteps().get(new MapEntry(Constant.SUB_STEP_UPCODE, 1)).getLabel()));
        actionsSteps.add(new SelectItem(Constant.SUB_STEP_TD_DB, Constant.getSteps().get(new MapEntry(Constant.SUB_STEP_TD_DB, 1)).getLabel()));
        actionsSteps.add(new SelectItem(Constant.SUB_STEP_CLEARCACHE, Constant.getSteps().get(new MapEntry(Constant.SUB_STEP_CLEARCACHE, 1)).getLabel()));
        actionsSteps.add(new SelectItem(Constant.SUB_STEP_RESTART_APP, Constant.getSteps().get(new MapEntry(Constant.SUB_STEP_RESTART_APP, 1)).getLabel()));
        actionsSteps.add(new SelectItem(Constant.SUB_STEP_START_APP, Constant.getSteps().get(new MapEntry(Constant.SUB_STEP_START_APP, 1)).getLabel()));

        actionsRollbackSteps.add(new SelectItem(Constant.ROLLBACK_STEP_CHECK_STATUS, Constant.getSteps().get(new MapEntry(Constant.ROLLBACK_STEP_CHECK_STATUS, 1)).getLabel()));
        actionsRollbackSteps.add(new SelectItem(Constant.ROLLBACK_STEP_STOP_APP, Constant.getSteps().get(new MapEntry(Constant.ROLLBACK_STEP_STOP_APP, 1)).getLabel()));
        actionsRollbackSteps.add(new SelectItem(Constant.ROLLBACK_STEP_SOURCE_CODE, Constant.getSteps().get(new MapEntry(Constant.ROLLBACK_STEP_SOURCE_CODE, 1)).getLabel()));
        actionsRollbackSteps.add(new SelectItem(Constant.ROLLBACK_STEP_DB, Constant.getSteps().get(new MapEntry(Constant.ROLLBACK_STEP_DB, 1)).getLabel()));
        actionsRollbackSteps.add(new SelectItem(Constant.ROLLBACK_STEP_CLEARCACHE, Constant.getSteps().get(new MapEntry(Constant.ROLLBACK_STEP_CLEARCACHE, 1)).getLabel()));
        actionsRollbackSteps.add(new SelectItem(Constant.ROLLBACK_STEP_RESTART_APP, Constant.getSteps().get(new MapEntry(Constant.ROLLBACK_STEP_RESTART_APP, 1)).getLabel()));
        actionsRollbackSteps.add(new SelectItem(Constant.ROLLBACK_STEP_START_APP, Constant.getSteps().get(new MapEntry(Constant.ROLLBACK_STEP_START_APP, 1)).getLabel()));
    }

    public void search() {
        ((DataTable) FacesContext.getCurrentInstance().getViewRoot().findComponent("form").findComponent("objectTable")).setFirst(0);

        Map<String, String> filters = new HashMap<>();

        if (StringUtils.isNotEmpty(searchName))
            filters.put("name", searchName);
        if (StringUtils.isNotEmpty(searchAfterGroup))
            filters.put("afterGroup", searchAfterGroup);

    }

    public boolean verifyAction() {
        Boolean result = true;
        FacesMessage msg = null;

        for (ActionCustomGroup customGroup : customGroups) {
            Set<ActionCustomAction> customActions = customGroup.getActionCustomActions();

            if (customActions.isEmpty()) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("action.did.not.added"), "");
                result = false;
            } else {
                for (ActionCustomAction customAction : customActions) {

                }
            }
        }

        if (msg != null) {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
        }

        return result;
    }

    public void prepareEdit(ActionCustomGroup obj) {
        isEdit = true;
        selectedObj = obj;
        BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
        try {
            BeanUtils.copyProperties(newObj, obj);
            //newObj.setPassword("");
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public ActionCustomGroup findRollback(Integer rollbackStep) {
        for (ActionCustomGroup customGroup : customGroups) {
            if (customGroup.getRollbackAfter() != null && customGroup.getRollbackAfter().equals(rollbackStep))
                return customGroup;
        }

        return null;
    }

    public void clear() {
        isEdit = false;
        newObj = new ActionCustomGroup();
        newObj.setKbGroup(1);
        newActionObj = new ActionCustomAction();
    }

    public void init() {
        customGroups = new ArrayList<>();
    }

    public void duplicate(ActionCustomGroup obj) {
        isEdit = false;
        obj.setId(null);
        selectedObj = obj;
        BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
        try {
            BeanUtils.copyProperties(newObj, obj);
            // newObj.setPassword("");
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void clearAction() {
        isEdit = false;
        newActionObj = new ActionCustomAction();
        newActionObj.setPriority(1);
        newActionObj.setRollbackTestPriority(2);
        newActionObj.setDbAction(2);
        newActionObj.setSeparator(";");
    }

    public void prepareEditAction(ActionCustomAction obj) {
        isEdit = true;
        selectedActionObj = obj;
        BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
        try {
            BeanUtils.copyProperties(newActionObj, obj);
            //newObj.setPassword("");
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }

        if (newActionObj != null && newActionObj.getType() != null && newActionObj.getModuleAction() != null && newActionObj.getType().equals(0) && (newActionObj.getModuleAction().equals(100) || newActionObj.getModuleAction().equals(101))) {
            loadTree(newActionObj.getModuleId());
        }
    }

    public void saveOrUpdate() {
        FacesMessage msg = null;
        try {
            if (!isEdit) {
//				selectedObj.setId(null);
                if (customGroups == null)
                    customGroups = new ArrayList<>();
                if (StringUtils.isEmpty(newObj.getName())) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, actionController.getBundle().getString("you.have.not.entered.an.action.group"), "");
                    return;
                }
                for (ActionCustomGroup customGroup : customGroups) {
                    if (newObj.getAfterGroup().equals(customGroup.getAfterGroup())) {
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("do.not.create.two.groups.of.actions.after.with.the.same.action"), "");
                        return;
                    }
                }
                customGroups.add(newObj);
            } else {
                BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
                try {
                    BeanUtils.copyProperties(selectedObj, newObj);
                    // newObj.setPassword("");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.error(e.getMessage(), e);
                }
            }
//			actionDetailDatabaseService.saveOrUpdate(selectedObj);
            if (!isEdit) {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("insert.successful"), "");
            } else {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("update.successful"), "");
            }

            newObj = new ActionCustomGroup();
            isEdit = false;
//			FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            RequestContext.getCurrentInstance().execute("PF('editDialogCustom').hide()");
            createFlow();

            createFlowRollback();
        } catch (Exception e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("update.failed"), "");
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            newObj = new ActionCustomGroup();
            isEdit = false;
        }

//		RequestContext.getCurrentInstance().execute("editDialogCustom.hide()");
    }

    public void saveOrUpdateAction() {
        FacesMessage msg = null;
        if (newActionObj.getPriority() == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("order.execution.not.enter"), "");
        } else if (newActionObj.getType() == null) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("impact.type.did.not.selected"), "");
        } else {
            switch (newActionObj.getType()) {
                case 0:
                    if (newActionObj.getModuleId() == null)
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("module.did.not.selected"), "");
                    else if (newActionObj.getModuleAction() == null)
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("action.do.not.select"), "");
                    break;
                case 1:
                    if (newActionObj.getDbId() == null)
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("db.do.not.select"), "");
                    else if (newActionObj.getDbAction() == 2) {
                        if (StringUtils.isEmpty(newActionObj.getExportStatement()))
                            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("command.export.do.not.enter"), "");
                        else if (StringUtils.isEmpty(newActionObj.getExportCount()))
                            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("command.count.do.not.enter"), "");
                    }
                    break;
                case 2:
                    if (this.selectedAppGroupId == null)
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("service.did.not.selected"), "");
                    else if (newActionObj.getFileId() == null)
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("guide.impact.do.not.enter"), "");
                    break;
                case 3:
                    if (StringUtils.isEmpty(newActionObj.getWaitReason()))
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("reason.pause.do.not.enter"), "");
                    break;
                default:
                    break;
            }
        }

        if (msg != null) {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
            return;
        }

        try {
            if (!isEdit) {
//				selectedObj.setId(null);
                Set<ActionCustomAction> customActions = selectedObj.getActionCustomActions();
                if (customActions == null)
                    customActions = new TreeSet<>();

                for (ActionCustomAction customAction : customActions) {
                    if (newActionObj.getType() == 0) {
                        if (customAction.getType() == 0 && newActionObj.getModuleId().equals(customAction.getModuleId()) && newActionObj.getModuleAction().equals(customAction.getModuleAction())) {
                            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("do.not.create.two.actions.with.same.module"), "");
                            return;
                        }

                        if (customAction.getType() == 2) {
                            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("do.not.impact.to.module.and.file.with.same.group.impact"), "");
                            return;
                        }
                    }

                    if (newActionObj.getType() == 2) {
                        if (customAction.getType() == 0) {
                            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("do.not.impact.to.module.and.file.with.same.group.impact"), "");
                            return;
                        }
                    }
                }

                customActions.add(newActionObj);
                selectedObj.setActionCustomActions(customActions);
            } else {
                BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
                try {
                    BeanUtils.copyProperties(selectedActionObj, newActionObj);
                    // newObj.setPassword("");
                } catch (IllegalAccessException | InvocationTargetException e) {
                    logger.error(e.getMessage(), e);
                }
            }
//			actionDetailDatabaseService.saveOrUpdate(selectedObj);
            if (!isEdit) {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("insert.successful"), "");
            } else {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("update.successful"), "");
            }

            newActionObj = new ActionCustomAction();
            isEdit = false;
//			FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            RequestContext.getCurrentInstance().execute("PF('editDialogAction').hide()");
            createFlow();
            createFlowRollback();
        } catch (SysException e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("update.failed"), "");
            logger.error(e.getMessage(), e);
        } finally {
            if (msg == null)
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("have.some.error"), "");
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            RequestContext.getCurrentInstance().update("mop:lst:objectTableCustom");
            RequestContext.getCurrentInstance().update("panelAction");
            RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
            RequestContext.getCurrentInstance().update("mop:lst:actionflow");
            /*newObj = new ActionCustomGroup();
			isEdit = false;*/
        }

//		RequestContext.getCurrentInstance().execute("editDialogCustom.hide()");
    }

    public void delete() {
        FacesMessage msg = null;
        try {
            customGroups.remove(selectedObj);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("delete.successful"), "");
            createFlow();
            createFlowRollback();
        } catch (Exception e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("delete.failed"), "");
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }
    }

    public void deleteAction() {
        FacesMessage msg = null;
        try {
            selectedObj.getActionCustomActions().remove(selectedActionObj);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("delete.successful"), "");
        } catch (Exception e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("delete.failed"), "");
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }
    }

    public void loadAppGroup() {
        appGroups = new ArrayList<>();
        Set<Long> appIds = new HashSet<>();
        for (Module detail : actionController.getImpactModules().values()) {
            if (appIds.contains(detail.getServiceId()))
                continue;
            appGroups.add(new SelectItem(detail.getServiceId(), detail.getServiceName()));

            appIds.add(detail.getServiceId());
        }
    }

    public void loadDtFile() {
        actionDtFiles = new ArrayList<>();
        if (selectedAppGroupId != null) {
            Map<String, Object> filters = new HashMap<>();
            filters.put("appGroupId", selectedAppGroupId.toString());
            try {
                actionDtFiles = actionDtFileService.findList(filters, new HashMap<String, String>());
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void createFlow() {
        model = new DefaultDiagramModel();
        model.setMaxConnections(-1);

        FlowChartConnector connector = new FlowChartConnector();
        connector.setPaintStyle("{strokeStyle:'#C7B097',lineWidth:3}");
        model.setDefaultConnector(connector);

        Element afterCheckStatus = null;
        Element afterStopApp = null;
        Element afterBackupApp = null;
        Element afterBackupDb = null;
        Element afterUpcode = null;
        Element afterTddb = null;
        Element afterClearCache = null;
        Element afterRestartApp = null;
        Element afterStartApp = null;
        Element afterChecklistApp = null;
        Element afterChecklistDb = null;

        Map<Integer, ActionCustomGroup> groupMap = new HashMap<>();
        for (ActionCustomGroup customGroup : customGroups) {
            groupMap.put(customGroup.getAfterGroup(), customGroup);
        }

        int x = 5;
        int y = 4;
        int coll = 4;
        int xStep = 20;
        int yStep = 10;

        int counter = 0;

        Element checkStatus = new Element("Check status", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        checkStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        checkStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        checkStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        checkStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (!actionController.getActionDetailAppController().getLstStop().isEmpty() || !actionController.getActionDetailAppController().getLstRestart().isEmpty() || !actionController.getActionDetailAppController().getLstRestartCmd().isEmpty()) {
            checkStatus.setStyleClass("ui-diagram-success");
            checkStatus.setData(checkStatus.getData() + "(" + (actionController.getActionDetailAppController().getLstStop().size() + actionController.getActionDetailAppController().getLstRestart().size() + actionController.getActionDetailAppController().getLstRestartCmd().size()) + ")");
        }
        counter++;

        if (groupMap.get(Constant.SUB_STEP_CHECK_STATUS) != null) {
            afterCheckStatus = new Element(groupMap.get(Constant.SUB_STEP_CHECK_STATUS).getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterCheckStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterCheckStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterCheckStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterCheckStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterCheckStatus.setStyleClass("ui-diagram-fail");
            afterCheckStatus.setData(afterCheckStatus.getData() + "(" + groupMap.get(Constant.SUB_STEP_CHECK_STATUS).getActionCustomActions().size() + ")");
            counter++;
        }

        Element stopApp = new Element("Stop app", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        stopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        stopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        stopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        stopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (!actionController.getActionDetailAppController().getLstStop().isEmpty()) {
            stopApp.setStyleClass("ui-diagram-success");
            stopApp.setData(stopApp.getData() + "(" + actionController.getActionDetailAppController().getLstStop().size() + ")");
        }
        counter++;

        if (groupMap.get(Constant.SUB_STEP_STOP_APP) != null) {
            afterStopApp = new Element(groupMap.get(Constant.SUB_STEP_STOP_APP).getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterStopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterStopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterStopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterStopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterStopApp.setStyleClass("ui-diagram-fail");
            afterStopApp.setData(afterStopApp.getData() + "(" + groupMap.get(Constant.SUB_STEP_STOP_APP).getActionCustomActions().size() + ")");
            counter++;
        }

        Element backupApp = new Element("Backup app", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        backupApp.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        backupApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        backupApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        backupApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (!actionController.getActionDetailAppController().getLstUpcode().isEmpty()) {
            backupApp.setStyleClass("ui-diagram-success");
            backupApp.setData(backupApp.getData() + "(" + actionController.getActionDetailAppController().getLstUpcode().size() + ")");
        }
        counter++;

        if (groupMap.get(Constant.SUB_STEP_BACKUP_APP) != null) {
            afterBackupApp = new Element(groupMap.get(Constant.SUB_STEP_BACKUP_APP).getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterBackupApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterBackupApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterBackupApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterBackupApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterBackupApp.setStyleClass("ui-diagram-fail");
            afterBackupApp.setData(afterBackupApp.getData() + "(" + groupMap.get(Constant.SUB_STEP_BACKUP_APP).getActionCustomActions().size() + ")");
            counter++;
        }

        int counterDb = 0;
        for (ActionDetailDatabase database : actionController.getActionDetailDatabaseController().getDetailDatabases()) {
            if ((database.getTypeImport().equals(1L) && StringUtils.isNotEmpty(database.getScriptBackup()))
                    || (database.getTypeImport().equals(0L)
                    && StringUtils.isNotEmpty(database.getBackupText()))) {
                counterDb++;
            }
        }
        Element backupDb = new Element("Backup db", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        backupDb.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        backupDb.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        backupDb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        backupDb.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (counterDb != 0) {
            backupDb.setStyleClass("ui-diagram-success");
            backupDb.setData(backupDb.getData() + "(" + counterDb + ")");
        }
        counter++;

        if (groupMap.get(Constant.SUB_STEP_BACKUP_DB) != null) {
            afterBackupDb = new Element(groupMap.get(Constant.SUB_STEP_BACKUP_DB).getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterBackupDb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterBackupDb.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterBackupDb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterBackupDb.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterBackupDb.setStyleClass("ui-diagram-fail");
            afterBackupDb.setData(afterBackupDb.getData() + "(" + groupMap.get(Constant.SUB_STEP_BACKUP_DB).getActionCustomActions().size() + ")");
            counter++;
        }

        Element upcode = new Element("Upcode", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        upcode.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        upcode.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        upcode.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        upcode.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (!actionController.getActionDetailAppController().getLstUpcode().isEmpty()) {
            upcode.setStyleClass("ui-diagram-success");
            upcode.setData(upcode.getData() + "(" + actionController.getActionDetailAppController().getLstUpcode().size() + ")");
        }
        counter++;

        if (groupMap.get(Constant.SUB_STEP_UPCODE) != null) {
            afterUpcode = new Element(groupMap.get(Constant.SUB_STEP_UPCODE).getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterUpcode.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterUpcode.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterUpcode.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterUpcode.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterUpcode.setStyleClass("ui-diagram-fail");
            afterUpcode.setData(afterUpcode.getData() + "(" + groupMap.get(Constant.SUB_STEP_UPCODE).getActionCustomActions().size() + ")");
            counter++;
        }

        Element tddb = new Element(MessageUtil.getResourceBundleMessage("impact.db"), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        tddb.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        tddb.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        tddb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        tddb.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (!actionController.getActionDetailDatabaseController().getDetailDatabases().isEmpty()) {
            tddb.setStyleClass("ui-diagram-success");
            tddb.setData(tddb.getData() + "(" + actionController.getActionDetailDatabaseController().getDetailDatabases().size() + ")");
        }
        counter++;

        if (groupMap.get(Constant.SUB_STEP_TD_DB) != null) {
            afterTddb = new Element(groupMap.get(Constant.SUB_STEP_TD_DB).getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterTddb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterTddb.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterTddb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterTddb.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterTddb.setStyleClass("ui-diagram-fail");
            afterTddb.setData(afterTddb.getData() + "(" + groupMap.get(Constant.SUB_STEP_TD_DB).getActionCustomActions().size() + ")");
            counter++;
        }

        Element clearCache = new Element("Clear cache", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        clearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        clearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        clearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        clearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (!actionController.getActionDetailAppController().getLstClearCache().isEmpty()) {
            clearCache.setStyleClass("ui-diagram-success");
            clearCache.setData(clearCache.getData() + "(" + actionController.getActionDetailAppController().getLstClearCache().size() + ")");
        }
        counter++;

        if (groupMap.get(Constant.SUB_STEP_CLEARCACHE) != null) {
            afterClearCache = new Element(groupMap.get(Constant.SUB_STEP_CLEARCACHE).getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterClearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterClearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterClearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterClearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterClearCache.setStyleClass("ui-diagram-fail");
            afterClearCache.setData(afterClearCache.getData() + "(" + groupMap.get(Constant.SUB_STEP_CLEARCACHE).getActionCustomActions().size() + ")");
            counter++;
        }

        Element restartApp = new Element("Restart app", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        restartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        restartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        restartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        restartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (!actionController.getActionDetailAppController().getLstRestart().isEmpty() || !actionController.getActionDetailAppController().getLstRestartCmd().isEmpty()) {
            restartApp.setStyleClass("ui-diagram-success");
            restartApp.setData(restartApp.getData() + "(" + (actionController.getActionDetailAppController().getLstRestart().size() + actionController.getActionDetailAppController().getLstRestartCmd().size()) + ")");
        }
        counter++;

        if (groupMap.get(Constant.SUB_STEP_RESTART_APP) != null) {
            afterRestartApp = new Element(groupMap.get(Constant.SUB_STEP_RESTART_APP).getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterRestartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRestartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterRestartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRestartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterRestartApp.setStyleClass("ui-diagram-fail");
            afterRestartApp.setData(afterRestartApp.getData() + "(" + groupMap.get(Constant.SUB_STEP_RESTART_APP).getActionCustomActions().size() + ")");
            counter++;
        }

        Element startApp = new Element("Start app", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        startApp.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        startApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        startApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        startApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (!actionController.getActionDetailAppController().getLstStart().isEmpty()) {
            startApp.setStyleClass("ui-diagram-success");
            startApp.setData(startApp.getData() + "(" + actionController.getActionDetailAppController().getLstStart().size() + ")");
        }
        counter++;

        if (groupMap.get(Constant.SUB_STEP_START_APP) != null) {
            afterStartApp = new Element(groupMap.get(Constant.SUB_STEP_START_APP).getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterStartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterStartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterStartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterStartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterStartApp.setStyleClass("ui-diagram-fail");
            afterStartApp.setData(afterStartApp.getData() + "(" + groupMap.get(Constant.SUB_STEP_START_APP).getActionCustomActions().size() + ")");
            counter++;
        }

        Element checklistApp = new Element("Checklist app", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        checklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        checklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        checklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        checklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (actionController.getCklListSelectedNodes() != null && actionController.getCklListSelectedNodes().length != 0) {
            checklistApp.setStyleClass("ui-diagram-success");
            checklistApp.setData(checklistApp.getData() + "(" + actionController.getCklListSelectedNodes().length + ")");
        }
        counter++;

        if (groupMap.get(Constant.SUB_STEP_CHECKLIST_APP) != null) {
            afterChecklistApp = new Element(groupMap.get(Constant.SUB_STEP_CHECKLIST_APP).getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterChecklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterChecklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterChecklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterChecklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterChecklistApp.setStyleClass("ui-diagram-fail");
            afterChecklistApp.setData(afterChecklistApp.getData() + "(" + groupMap.get(Constant.SUB_STEP_CHECKLIST_APP).getActionCustomActions().size() + ")");
            counter++;
        }

        Element checklistDb = new Element("Checklist db", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        checklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        checklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        checklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        checklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (actionController.getCklDbListSelectedNodes() != null && actionController.getCklDbListSelectedNodes().length != 0) {
            checklistDb.setStyleClass("ui-diagram-success");
            checklistDb.setData(checklistDb.getData() + "(" + actionController.getCklDbListSelectedNodes().length + ")");
        }
        counter++;

        if (groupMap.get(Constant.SUB_STEP_CHECKLIST_DB) != null) {
            afterChecklistDb = new Element(groupMap.get(Constant.SUB_STEP_CHECKLIST_DB).getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterChecklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterChecklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterChecklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterChecklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterChecklistDb.setStyleClass("ui-diagram-fail");
            afterChecklistDb.setData(afterChecklistDb.getData() + "(" + groupMap.get(Constant.SUB_STEP_CHECKLIST_DB).getActionCustomActions().size() + ")");
//            counter++;
        }

        model.addElement(checkStatus);
        model.addElement(stopApp);
        model.addElement(backupApp);
        model.addElement(backupDb);
        model.addElement(upcode);
        model.addElement(tddb);
        model.addElement(clearCache);
        model.addElement(restartApp);
        model.addElement(startApp);
        model.addElement(checklistApp);
        model.addElement(checklistDb);

        if (afterCheckStatus != null)
            model.addElement(afterCheckStatus);
        if (afterStopApp != null)
            model.addElement(afterStopApp);
        if (afterBackupApp != null)
            model.addElement(afterBackupApp);
        if (afterBackupDb != null)
            model.addElement(afterBackupDb);
        if (afterUpcode != null)
            model.addElement(afterUpcode);
        if (afterTddb != null)
            model.addElement(afterTddb);
        if (afterClearCache != null)
            model.addElement(afterClearCache);
        if (afterRestartApp != null)
            model.addElement(afterRestartApp);
        if (afterStartApp != null)
            model.addElement(afterStartApp);
        if (afterChecklistApp != null)
            model.addElement(afterChecklistApp);
        if (afterChecklistDb != null)
            model.addElement(afterChecklistDb);

        if (afterCheckStatus == null) {
            model.connect(createConnection(checkStatus.getEndPoints().get(3), stopApp.getEndPoints().get(2), null));
        } else {
            model.connect(createConnection(checkStatus.getEndPoints().get(3), afterCheckStatus.getEndPoints().get(2), null));
            model.connect(createConnection(afterCheckStatus.getEndPoints().get(3), stopApp.getEndPoints().get(2), null));
        }
        if (afterStopApp == null) {
            model.connect(createConnection(stopApp.getEndPoints().get(3), backupApp.getEndPoints().get(2), null));
        } else {
            model.connect(createConnection(stopApp.getEndPoints().get(3), afterStopApp.getEndPoints().get(2), null));
            model.connect(createConnection(afterStopApp.getEndPoints().get(3), backupApp.getEndPoints().get(2), null));
        }
        if (afterBackupApp == null) {
            model.connect(createConnection(backupApp.getEndPoints().get(3), backupDb.getEndPoints().get(2), null));
        } else {
            model.connect(createConnection(backupApp.getEndPoints().get(3), afterBackupApp.getEndPoints().get(2), null));
            model.connect(createConnection(afterBackupApp.getEndPoints().get(3), backupDb.getEndPoints().get(2), null));
        }
        if (afterBackupDb == null) {
            model.connect(createConnection(backupDb.getEndPoints().get(3), upcode.getEndPoints().get(2), null));
        } else {
            model.connect(createConnection(backupDb.getEndPoints().get(3), afterBackupDb.getEndPoints().get(2), null));
            model.connect(createConnection(afterBackupDb.getEndPoints().get(3), upcode.getEndPoints().get(2), null));
        }
        if (afterUpcode == null) {
            model.connect(createConnection(upcode.getEndPoints().get(3), tddb.getEndPoints().get(2), null));
        } else {
            model.connect(createConnection(upcode.getEndPoints().get(3), afterUpcode.getEndPoints().get(2), null));
            model.connect(createConnection(afterUpcode.getEndPoints().get(3), tddb.getEndPoints().get(2), null));
        }
        if (afterTddb == null) {
            model.connect(createConnection(tddb.getEndPoints().get(3), clearCache.getEndPoints().get(2), null));
        } else {
            model.connect(createConnection(tddb.getEndPoints().get(3), afterTddb.getEndPoints().get(2), null));
            model.connect(createConnection(afterTddb.getEndPoints().get(3), clearCache.getEndPoints().get(2), null));
        }
        if (afterClearCache == null) {
            model.connect(createConnection(clearCache.getEndPoints().get(3), restartApp.getEndPoints().get(2), null));
        } else {
            model.connect(createConnection(clearCache.getEndPoints().get(3), afterClearCache.getEndPoints().get(2), null));
            model.connect(createConnection(afterClearCache.getEndPoints().get(3), restartApp.getEndPoints().get(2), null));
        }
        if (afterRestartApp == null) {
            model.connect(createConnection(restartApp.getEndPoints().get(3), startApp.getEndPoints().get(2), null));
        } else {
            model.connect(createConnection(restartApp.getEndPoints().get(3), afterRestartApp.getEndPoints().get(2), null));
            model.connect(createConnection(afterRestartApp.getEndPoints().get(3), startApp.getEndPoints().get(2), null));
        }
        if (afterStartApp == null) {
            model.connect(createConnection(startApp.getEndPoints().get(3), checklistApp.getEndPoints().get(2), null));
        } else {
            model.connect(createConnection(startApp.getEndPoints().get(3), afterStartApp.getEndPoints().get(2), null));
            model.connect(createConnection(afterStartApp.getEndPoints().get(3), checklistApp.getEndPoints().get(2), null));
        }
        if (afterChecklistApp == null) {
            model.connect(createConnection(checklistApp.getEndPoints().get(3), checklistDb.getEndPoints().get(2), null));
        } else {
            model.connect(createConnection(checklistApp.getEndPoints().get(3), afterChecklistApp.getEndPoints().get(2), null));
            model.connect(createConnection(afterChecklistApp.getEndPoints().get(3), checklistDb.getEndPoints().get(2), null));
        }

        if (afterChecklistDb != null) {
            model.connect(createConnection(checklistDb.getEndPoints().get(3), afterChecklistDb.getEndPoints().get(2), null));
        }
/*		model.connect(createConnection(giveup.getEndPoints().get(1), start.getEndPoints().get(1), "No"));
		model.connect(createConnection(trouble.getEndPoints().get(2), succeed.getEndPoints().get(0), "No"));
		model.connect(createConnection(giveup.getEndPoints().get(2), fail.getEndPoints().get(0), "Yes"));*/

    }

    public void createFlowRollback() {
        rollbackModel = new DefaultDiagramModel();
        rollbackModel.setMaxConnections(-1);

        FlowChartConnector connector = new FlowChartConnector();
        connector.setPaintStyle("{strokeStyle:'#C7B097',lineWidth:3}");
        rollbackModel.setDefaultConnector(connector);

        Element afterRollbackCheckStatus = null;
        Element afterRollbackStopApp = null;
/*		Element afterRollbackBackupApp = null;
		Element afterRollbackBackupDb = null;*/
        Element afterRollbackCode = null;
        Element afterRollbackDb = null;
        Element afterRollbackClearCache = null;
        Element afterRollbackRestartApp = null;
        Element afterRollbackStartApp = null;
        Element afterRollbackChecklistApp = null;
        Element afterRollbackChecklistDb = null;

        Map<Integer, ActionCustomGroup> groupMap = new HashMap<>();
        for (ActionCustomGroup customGroup : customGroups) {
            groupMap.put(customGroup.getAfterGroup(), customGroup);
        }

        int x = 5;
        int y = 4;
        int coll = 4;
        int xStep = 20;
        int yStep = 10;

        int counter = 0;

        Element checkStatus = new Element("Check status", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        checkStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        checkStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        checkStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        checkStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        /*20181225_hoangnd_khong add buoc check status vao rollback_start*/
        /*if (!actionController.getActionDetailAppController().getLstStop().isEmpty() || !actionController.getActionDetailAppController().getLstRestart().isEmpty() || !actionController.getActionDetailAppController().getLstRestartCmd().isEmpty()) {
            checkStatus.setStyleClass("ui-diagram-success");
            checkStatus.setData(checkStatus.getData() + "(" + (actionController.getActionDetailAppController().getLstStop().size() + actionController.getActionDetailAppController().getLstRestart().size() + actionController.getActionDetailAppController().getLstRestartCmd().size()) + ")");
        }*/
        /*20181225_hoangnd_khong add buoc check status vao rollback_end*/
        counter++;

        ActionCustomGroup rollback = findRollback(Constant.ROLLBACK_STEP_CHECK_STATUS);
        if (rollback != null) {
            afterRollbackCheckStatus = new Element(rollback.getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterRollbackCheckStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackCheckStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterRollbackCheckStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackCheckStatus.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterRollbackCheckStatus.setStyleClass("ui-diagram-fail");
            afterRollbackCheckStatus.setData(afterRollbackCheckStatus.getData() + "(" + rollback.getActionCustomActions().size() + ")");
            counter++;
        }

        Element stopApp = new Element("Stop app", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        stopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        stopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        stopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        stopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (!actionController.getActionDetailAppController().getLstStart().isEmpty()) {
            stopApp.setStyleClass("ui-diagram-success");
            stopApp.setData(stopApp.getData() + "(" + actionController.getActionDetailAppController().getLstStart().size() + ")");
        }
        counter++;

        rollback = findRollback(Constant.ROLLBACK_STEP_STOP_APP);
        if (rollback != null) {
            afterRollbackStopApp = new Element(rollback.getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterRollbackStopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackStopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterRollbackStopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackStopApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterRollbackStopApp.setStyleClass("ui-diagram-fail");
            afterRollbackStopApp.setData(afterRollbackStopApp.getData() + "(" + rollback.getActionCustomActions().size() + ")");
            counter++;
        }

        Element upcode = new Element("Rollback code", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        upcode.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        upcode.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        upcode.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        upcode.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (!actionController.getActionDetailAppController().getLstUpcode().isEmpty()) {
            upcode.setStyleClass("ui-diagram-success");
            upcode.setData(upcode.getData() + "(" + actionController.getActionDetailAppController().getLstUpcode().size() + ")");
        }
        counter++;

        rollback = findRollback(Constant.ROLLBACK_STEP_SOURCE_CODE);
        if (rollback != null) {
            afterRollbackCode = new Element(rollback.getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterRollbackCode.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackCode.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterRollbackCode.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackCode.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterRollbackCode.setStyleClass("ui-diagram-fail");
            afterRollbackCode.setData(afterRollbackCode.getData() + "(" + rollback.getActionCustomActions().size() + ")");
            counter++;
        }

        int counterDb = 0;
        for (ActionDetailDatabase database : actionController.getActionDetailDatabaseController().getDetailDatabases()) {
            if ((database.getTypeImport().equals(1L) && StringUtils.isNotEmpty(database.getRollbackFile()))
                    || (database.getTypeImport().equals(0L)
                    && StringUtils.isNotEmpty(database.getRollbackText()))) {
                counterDb++;
            }
        }

        Element tddb = new Element("Rollback db", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        tddb.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        tddb.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        tddb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        tddb.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (counterDb != 0) {
            tddb.setStyleClass("ui-diagram-success");
            tddb.setData(tddb.getData() + "(" + counterDb + ")");
        }
        counter++;

        rollback = findRollback(Constant.ROLLBACK_STEP_DB);
        if (rollback != null) {
            afterRollbackDb = new Element(rollback.getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterRollbackDb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackDb.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterRollbackDb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackDb.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterRollbackDb.setStyleClass("ui-diagram-fail");
            afterRollbackDb.setData(afterRollbackDb.getData() + "(" + rollback.getActionCustomActions().size() + ")");
            counter++;
        }

        Element clearCache = new Element("Clear cache", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        clearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        clearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        clearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        clearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (!actionController.getActionDetailAppController().getLstClearCache().isEmpty()) {
            clearCache.setStyleClass("ui-diagram-success");
            clearCache.setData(clearCache.getData() + "(" + actionController.getActionDetailAppController().getLstClearCache().size() + ")");
        }
        counter++;


        rollback = findRollback(Constant.ROLLBACK_STEP_CLEARCACHE);
        if (rollback != null) {
            afterRollbackClearCache = new Element(rollback.getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterRollbackClearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackClearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterRollbackClearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackClearCache.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterRollbackClearCache.setStyleClass("ui-diagram-fail");
            afterRollbackClearCache.setData(afterRollbackClearCache.getData() + "(" + rollback.getActionCustomActions().size() + ")");
            counter++;
        }

        Element restartApp = new Element("Restart app", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        restartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        restartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        restartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        restartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (!actionController.getActionDetailAppController().getLstRestart().isEmpty() || !actionController.getActionDetailAppController().getLstRestartCmd().isEmpty()) {
            restartApp.setStyleClass("ui-diagram-success");
            restartApp.setData(restartApp.getData() + "(" + (actionController.getActionDetailAppController().getLstRestart().size() + actionController.getActionDetailAppController().getLstRestartCmd().size()) + ")");
        }
        counter++;

        rollback = findRollback(Constant.ROLLBACK_STEP_RESTART_APP);
        if (rollback != null) {
            afterRollbackRestartApp = new Element(rollback.getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterRollbackRestartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackRestartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterRollbackRestartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackRestartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterRollbackRestartApp.setStyleClass("ui-diagram-fail");
            afterRollbackRestartApp.setData(afterRollbackRestartApp.getData() + "(" + rollback.getActionCustomActions().size() + ")");
            counter++;
        }

        Element startApp = new Element("Start app", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        startApp.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        startApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        startApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        startApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (!actionController.getActionDetailAppController().getLstStop().isEmpty()) {
            startApp.setStyleClass("ui-diagram-success");
            startApp.setData(startApp.getData() + "(" + actionController.getActionDetailAppController().getLstStop().size() + ")");
        }
        counter++;

        rollback = findRollback(Constant.ROLLBACK_STEP_START_APP);
        if (rollback != null) {
            afterRollbackStartApp = new Element(rollback.getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterRollbackStartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackStartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterRollbackStartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackStartApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterRollbackStartApp.setStyleClass("ui-diagram-fail");
            afterRollbackStartApp.setData(afterRollbackStartApp.getData() + "(" + rollback.getActionCustomActions().size() + ")");
            counter++;
        }

        Element checklistApp = new Element("Checklist app", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        checklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        checklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        checklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        checklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (actionController.getCklListSelectedNodes() != null && actionController.getCklListSelectedNodes().length != 0) {
            checklistApp.setStyleClass("ui-diagram-success");
            checklistApp.setData(checklistApp.getData() + "(" + actionController.getCklListSelectedNodes().length + ")");
        }
        counter++;

        if (groupMap.get(Constant.SUB_STEP_CHECKLIST_APP) != null) {
            afterRollbackChecklistApp = new Element(groupMap.get(Constant.SUB_STEP_CHECKLIST_APP).getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterRollbackChecklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackChecklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterRollbackChecklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackChecklistApp.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterRollbackChecklistApp.setStyleClass("ui-diagram-fail");
            afterRollbackChecklistApp.setData(afterRollbackChecklistApp.getData() + "(" + groupMap.get(Constant.SUB_STEP_CHECKLIST_APP).getActionCustomActions().size() + ")");
            counter++;
        }

        Element checklistDb = new Element("Checklist db", x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
        checklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
        checklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
        checklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
        checklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
        if (actionController.getCklDbListSelectedNodes() != null && actionController.getCklDbListSelectedNodes().length != 0) {
            checklistDb.setStyleClass("ui-diagram-success");
            checklistDb.setData(checklistDb.getData() + "(" + actionController.getCklDbListSelectedNodes().length + ")");
        }
        counter++;

        if (groupMap.get(Constant.SUB_STEP_CHECKLIST_DB) != null) {
            afterRollbackChecklistDb = new Element(groupMap.get(Constant.SUB_STEP_CHECKLIST_DB).getName(), x + (counter % coll) * xStep + "em", y + Math.round(counter / coll) * yStep + "em");
            afterRollbackChecklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackChecklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
            afterRollbackChecklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
            afterRollbackChecklistDb.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
            afterRollbackChecklistDb.setStyleClass("ui-diagram-fail");
            afterRollbackChecklistDb.setData(afterRollbackChecklistDb.getData() + "(" + groupMap.get(Constant.SUB_STEP_CHECKLIST_DB).getActionCustomActions().size() + ")");
//            counter++;
        }

        rollbackModel.addElement(checkStatus);
        rollbackModel.addElement(stopApp);
/*		model.addElement(backupApp);
		model.addElement(backupDb);*/
        rollbackModel.addElement(upcode);
        rollbackModel.addElement(tddb);
        rollbackModel.addElement(clearCache);
        rollbackModel.addElement(restartApp);
        rollbackModel.addElement(startApp);
        rollbackModel.addElement(checklistApp);
        rollbackModel.addElement(checklistDb);

        if (afterRollbackCheckStatus != null)
            rollbackModel.addElement(afterRollbackCheckStatus);
        if (afterRollbackStopApp != null)
            rollbackModel.addElement(afterRollbackStopApp);
/*		if (afterRollbackBackupApp != null)
			model.addElement(afterRollbackBackupApp);
		if (afterRollbackBackupDb != null)
			model.addElement(afterRollbackBackupDb);*/
        if (afterRollbackCode != null)
            rollbackModel.addElement(afterRollbackCode);
        if (afterRollbackDb != null)
            rollbackModel.addElement(afterRollbackDb);
        if (afterRollbackClearCache != null)
            rollbackModel.addElement(afterRollbackClearCache);
        if (afterRollbackRestartApp != null)
            rollbackModel.addElement(afterRollbackRestartApp);
        if (afterRollbackStartApp != null)
            rollbackModel.addElement(afterRollbackStartApp);
        if (afterRollbackChecklistApp != null)
            rollbackModel.addElement(afterRollbackChecklistApp);
        if (afterRollbackChecklistDb != null)
            rollbackModel.addElement(afterRollbackChecklistDb);

        if (afterRollbackCheckStatus == null) {
            rollbackModel.connect(createConnection(checkStatus.getEndPoints().get(3), stopApp.getEndPoints().get(2), null));
        } else {
            rollbackModel.connect(createConnection(checkStatus.getEndPoints().get(3), afterRollbackCheckStatus.getEndPoints().get(2), null));
            rollbackModel.connect(createConnection(afterRollbackCheckStatus.getEndPoints().get(3), stopApp.getEndPoints().get(2), null));
        }
        if (afterRollbackStopApp == null) {
            rollbackModel.connect(createConnection(stopApp.getEndPoints().get(3), upcode.getEndPoints().get(2), null));
        } else {
            rollbackModel.connect(createConnection(stopApp.getEndPoints().get(3), afterRollbackStopApp.getEndPoints().get(2), null));
            rollbackModel.connect(createConnection(afterRollbackStopApp.getEndPoints().get(3), upcode.getEndPoints().get(2), null));
        }
		/*if (afterRollbackBackupApp == null) {
			model.connect(createConnection(backupApp.getEndPoints().get(3), backupDb.getEndPoints().get(2), null));
		} else {
			model.connect(createConnection(backupApp.getEndPoints().get(3), afterRollbackBackupApp.getEndPoints().get(2), null));
			model.connect(createConnection(afterRollbackBackupApp.getEndPoints().get(3), backupDb.getEndPoints().get(2), null));
		}
		if (afterRollbackBackupDb == null) {
			model.connect(createConnection(backupDb.getEndPoints().get(3), upcode.getEndPoints().get(2), null));
		} else {
			model.connect(createConnection(backupDb.getEndPoints().get(3), afterRollbackBackupDb.getEndPoints().get(2), null));
			model.connect(createConnection(afterRollbackBackupDb.getEndPoints().get(3), upcode.getEndPoints().get(2), null));
		}*/
        if (afterRollbackCode == null) {
            rollbackModel.connect(createConnection(upcode.getEndPoints().get(3), tddb.getEndPoints().get(2), null));
        } else {
            rollbackModel.connect(createConnection(upcode.getEndPoints().get(3), afterRollbackCode.getEndPoints().get(2), null));
            rollbackModel.connect(createConnection(afterRollbackCode.getEndPoints().get(3), tddb.getEndPoints().get(2), null));
        }
        if (afterRollbackDb == null) {
            rollbackModel.connect(createConnection(tddb.getEndPoints().get(3), clearCache.getEndPoints().get(2), null));
        } else {
            rollbackModel.connect(createConnection(tddb.getEndPoints().get(3), afterRollbackDb.getEndPoints().get(2), null));
            rollbackModel.connect(createConnection(afterRollbackDb.getEndPoints().get(3), clearCache.getEndPoints().get(2), null));
        }
        if (afterRollbackClearCache == null) {
            rollbackModel.connect(createConnection(clearCache.getEndPoints().get(3), restartApp.getEndPoints().get(2), null));
        } else {
            rollbackModel.connect(createConnection(clearCache.getEndPoints().get(3), afterRollbackClearCache.getEndPoints().get(2), null));
            rollbackModel.connect(createConnection(afterRollbackClearCache.getEndPoints().get(3), restartApp.getEndPoints().get(2), null));
        }
        if (afterRollbackRestartApp == null) {
            rollbackModel.connect(createConnection(restartApp.getEndPoints().get(3), startApp.getEndPoints().get(2), null));
        } else {
            rollbackModel.connect(createConnection(restartApp.getEndPoints().get(3), afterRollbackRestartApp.getEndPoints().get(2), null));
            rollbackModel.connect(createConnection(afterRollbackRestartApp.getEndPoints().get(3), startApp.getEndPoints().get(2), null));
        }
        if (afterRollbackStartApp == null) {
            rollbackModel.connect(createConnection(startApp.getEndPoints().get(3), checklistApp.getEndPoints().get(2), null));
        } else {
            rollbackModel.connect(createConnection(startApp.getEndPoints().get(3), afterRollbackStartApp.getEndPoints().get(2), null));
            rollbackModel.connect(createConnection(afterRollbackStartApp.getEndPoints().get(3), checklistApp.getEndPoints().get(2), null));
        }
        if (afterRollbackChecklistApp == null) {
            rollbackModel.connect(createConnection(checklistApp.getEndPoints().get(3), checklistDb.getEndPoints().get(2), null));
        } else {
            rollbackModel.connect(createConnection(checklistApp.getEndPoints().get(3), afterRollbackChecklistApp.getEndPoints().get(2), null));
            rollbackModel.connect(createConnection(afterRollbackChecklistApp.getEndPoints().get(3), checklistDb.getEndPoints().get(2), null));
        }

        if (afterRollbackChecklistDb != null) {
            rollbackModel.connect(createConnection(checklistDb.getEndPoints().get(3), afterRollbackChecklistDb.getEndPoints().get(2), null));
        }
    }

    public void viewSelectItems() {
        Map<String, Object> filters = new HashMap<>();

        Action action = actionController.getNewObj();
        if (action != null && action.getId() != null) {
            filters.put("actionId", action.getId() + "");

            try {
                customGroups = actionCustomGroupService.findList(filters, new HashMap<String, String>());
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            customGroups = new ArrayList<>();
        }
    }

    public void handleFileUploadRollback(FileUploadEvent event) {
        logger.info(event.getFile().getFileName() + " is uploaded.");

        UploadedFile file = event.getFile();
        String uploadFolder = UploadFileUtils.getDatabaseFolder(actionController.getNewObj());
        if (file != null) {
            BigDecimal seq = null;
            try {
                seq = actionController.getActionService().nextFileSeq();
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
            String sourceCode = Util.convertUTF8ToNoSign(FilenameUtils.getBaseName(file.getFileName()) + "_" + seq + "." + FilenameUtils.getExtension(file.getFileName()));

            FileHelper.uploadFile(uploadFolder, file, sourceCode);
            newActionObj.setDbScriptRb(sourceCode);
        }
//		FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
//		FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void handleFileUpload(FileUploadEvent event) {
        logger.info(event.getFile().getFileName() + " is uploaded.");

        UploadedFile file = event.getFile();
        String uploadFolder = UploadFileUtils.getDatabaseFolder(actionController.getNewObj());
        if (file != null) {
            BigDecimal seq = null;
            try {
                seq = actionController.getActionService().nextFileSeq();
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
            String sourceCode = Util.convertUTF8ToNoSign(FilenameUtils.getBaseName(file.getFileName()) + "_" + seq + "." + FilenameUtils.getExtension(file.getFileName()));

            FileHelper.uploadFile(uploadFolder, file, sourceCode);
            newActionObj.setDbScriptFile(sourceCode);
        }
//		FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
//		FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void handleFileUploadBackup(FileUploadEvent event) {
        logger.info(event.getFile().getFileName() + " is uploaded.");

        UploadedFile file = event.getFile();
        String uploadFolder = UploadFileUtils.getDatabaseFolder(actionController.getNewObj());
        if (file != null) {
            BigDecimal seq = null;
            try {
                seq = actionController.getActionService().nextFileSeq();
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
            String sourceCode = Util.convertUTF8ToNoSign(FilenameUtils.getBaseName(file.getFileName()) + "_" + seq + "." + FilenameUtils.getExtension(file.getFileName()));

            FileHelper.uploadFile(uploadFolder, file, sourceCode);
            newActionObj.setDbScriptBackup(sourceCode);
        }
//		FacesMessage message = new FacesMessage("Succesful", event.getFile().getFileName() + " is uploaded.");
//		FacesContext.getCurrentInstance().addMessage(null, message);
    }

    public void handleFileUploadImportFile(FileUploadEvent event) {
        logger.info(event.getFile().getFileName() + " is uploaded.");

        UploadedFile file = event.getFile();
        String uploadFolder = UploadFileUtils.getDataImportFolder(actionController.getNewObj());
        if (file != null) {
            BigDecimal seq = null;
            try {
                seq = actionController.getActionService().nextFileSeq();
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
            String sourceCode = Util.convertUTF8ToNoSign(FilenameUtils.getBaseName(file.getFileName()) + "_" + seq + "." + FilenameUtils.getExtension(file.getFileName()));

            FileHelper.uploadFile(uploadFolder, file, sourceCode);
            newActionObj.setImportDataFile(sourceCode);
        }
    }

    public void upLevelTree() {
        Module module = actionController.getImpactModules().get(newActionObj.getModuleId());
        TreeObject cklDbParent = new TreeObject(MessageUtil.getResourceBundleMessage("up.code.directory"), FilenameUtils.getFullPathNoEndSeparator(module.getExecutePath().replaceAll("/$", "")));
        this.upcodeRoot = new DefaultTreeNode(cklDbParent, null);

        actionController.addChilds(upcodeRoot, module);
    }

    public void selectDir(TreeObject selectedUpcodeDir) {
//		this.selectedUpcodeDir = selectedUpcodeDir;
        if (selectedUpcodeDir != null) {
//			TreeObject object = (TreeObject) selectedUpcodeDir.getData();
            String fullPath = selectedUpcodeDir.getObj().toString();

            Module detail = actionController.getImpactModules().get(newActionObj.getModuleId());

            String installPath = detail.getExecutePath().replaceAll("/$", "");
            String path;
            if (fullPath.contains(installPath) && !fullPath.equals(installPath)) {
                path = fullPath.replaceFirst(detail.getExecutePath(), "").replaceFirst("^/", "");

            } else {
                path = ".." + fullPath.replaceFirst(FilenameUtils.getFullPathNoEndSeparator(installPath.replaceAll("/$", "")), "");

            }
            if (newActionObj.getUpcodePath() != null && (!newActionObj.getUpcodePath().equals(path))) {
                newActionObj.setLstFileRemove("");
            }
            newActionObj.setUpcodePath(path);

            if (selectedUpcodeDir.getIsDir()) {
                ((TreeObject) removeUpcodeRoot.getData()).setObj(fullPath);
                actionController.addChilds(removeUpcodeRoot, detail);
            }
        }
    }

    public void onNodeExpand(NodeExpandEvent event) {
        TreeNode treeNode = event.getTreeNode();
        Module module = actionController.getImpactModules().get(newActionObj.getModuleId());

        actionController.addChilds(treeNode, module);
    }

    public void upLevelTreeRemove(NodeExpandEvent event) {
        TreeNode treeNode = event.getTreeNode();
        Module module = actionController.getImpactModules().get(newActionObj.getModuleId());

        actionController.addChilds(treeNode, module);
    }

    public void onNodeExpandRemove(NodeExpandEvent event) {
        TreeNode treeNode = event.getTreeNode();
        Module module = actionController.getImpactModules().get(newActionObj.getModuleId());

        actionController.addChilds(treeNode, module);
    }

    public void removeFile() {
        if (selectedRemoveUpcodeFile != null) {
            String fullPath = ((TreeObject) selectedRemoveUpcodeFile.getData()).getObj().toString();

            Module detail = actionController.getImpactModules().get(newActionObj.getModuleId());

            String installPath = detail.getExecutePath().replaceAll("/$", "");
            String path;
            if (fullPath.contains(installPath) && !fullPath.equals(installPath)) {
                path = fullPath.replaceFirst(detail.getExecutePath(), "").replaceFirst("^/", "");
//				newObj.setUpcodePath(path);
            } else {
                path = ".." + fullPath.replaceFirst(FilenameUtils.getFullPathNoEndSeparator(installPath.replaceAll("/$", "")), "");
//				newObj.setUpcodePath(path);
            }
            logger.info(path);

            List<String> removeFiles = new ArrayList<>(Splitter.on(",").trimResults().omitEmptyStrings().splitToList(newActionObj.getLstFileRemove() == null ? "" : newActionObj.getLstFileRemove()));
            if (!removeFiles.contains("'" + path + "'")) {
                removeFiles.add("'" + path + "'");
            }
            newActionObj.setLstFileRemove(Joiner.on(", ").join(removeFiles));

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("selected.for.delete") + ": " + ((TreeObject) selectedRemoveUpcodeFile.getData()).getName(), "");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void loadTree(Long moduleId) {
        Module module = actionController.getImpactModules().get(moduleId);
        TreeObject cklDbParent = new TreeObject(MessageUtil.getResourceBundleMessage("up.code.directory"), module.getExecutePath());
        this.upcodeRoot = new DefaultTreeNode(cklDbParent, null);

        actionController.addChilds(upcodeRoot, module);

        TreeObject removeCklDbParent = new TreeObject(MessageUtil.getResourceBundleMessage("file.need.delete"), module.getExecutePath());
        this.removeUpcodeRoot = new DefaultTreeNode(removeCklDbParent, null);

        if (StringUtils.isNotEmpty(newActionObj.getUpcodePath())) {
            ((TreeObject)removeUpcodeRoot.getData()).setObj(module.getExecutePath() + "/" + newActionObj.getUpcodePath());
            actionController.addChilds(removeUpcodeRoot, module);
        }
    }

    public void loadCustomAction(ActionCustomAction action) {
//		RequestContext.getCurrentInstance().execute("PF('blockUiAppDialog').block()");
        if (action != null && action.getType() != null && action.getModuleAction() != null && action.getType().equals(0) && (action.getModuleAction().equals(100) || action.getModuleAction().equals(101))) {
            loadTree(action.getModuleId());
        }
    }

    public void handleUploadCode(FileUploadEvent event) {
        UploadedFile file = event.getFile();

//		String folder = "source_code";
        String uploadFolder = UploadFileUtils.getSourceCodeFolder(actionController.getNewObj());
        if (file != null) {
            Module detail = actionController.getImpactModules().get(newActionObj.getModuleId());

            if (detail == null)
                return;
            String sourceCode = Util.convertUTF8ToNoSign(detail.getModuleCode() + "_" + newActionObj.getUpcodePath().replaceAll("\\.\\.", "").replaceAll("/", "_") + "_" + FilenameUtils.getBaseName(file.getFileName()) + "_test." + FilenameUtils.getExtension(file.getFileName()));
            FileHelper.uploadFile(uploadFolder, file, sourceCode);
            newActionObj.setUploadCodePath(sourceCode);
        }

    }

    public void checkUpcodeFolderChosen() {
        if (StringUtils.isEmpty(newActionObj.getUpcodePath())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("folder.up.code.do.not.select"), "");
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            return;
        }
    }

    public void clearRemove() {
        newActionObj.setLstFileRemove("");
    }

    public StreamedContent downloadCode(ActionCustomAction obj) {
        StreamedContent fileInput = null;
        String filePath = UploadFileUtils.getSourceCodeFolder(actionController.getNewObj()) + File.separator + obj.getUploadCodePath();
        try {
            fileInput = new DefaultStreamedContent(new FileInputStream(filePath), "", obj.getUploadCodePath());
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return fileInput;
    }

    public void createConfirmGroup() {
        if (actionController.getNewObj().getKbType() != null) {
            if (actionController.getNewObj().getKbType().equals(AamConstants.KB_TYPE.BD_SERVICE)) {
                ActionCustomGroup actionCustomGroup = new ActionCustomGroup();
                if (customGroups != null && !customGroups.isEmpty()) {
//                    if ()
                }
                actionCustomGroup.setName("Service maintenance");
                actionCustomGroup.setAfterGroup(Constant.SUB_STEP_CLEARCACHE);
                actionCustomGroup.setRollbackAfter(Constant.ROLLBACK_STEP_CLEARCACHE);
                actionCustomGroup.setActionCustomActions(new HashSet<>());

                customGroups = Arrays.asList(actionCustomGroup);

                ActionCustomAction customAction = new ActionCustomAction();
                customAction.setType(3);
                customAction.setPriority(1);
                customAction.setWaitReason(MessageUtil.getResourceBundleMessage("mantemance.service"));
                actionCustomGroup.getActionCustomActions().add(customAction);
//                actionController.getNewObj().setAc
            } else if (actionController.getNewObj().getKbType().equals(AamConstants.KB_TYPE.BD_SERVER)
                    || actionController.getNewObj().getKbType().equals(AamConstants.KB_TYPE.UC_SERVER)) {
                ActionCustomGroup actionCustomGroup = new ActionCustomGroup();
                if (customGroups != null && !customGroups.isEmpty()) {
//                    if ()
                }
                actionCustomGroup.setName("Server maintenance");
                actionCustomGroup.setAfterGroup(Constant.SUB_STEP_CLEARCACHE);
                actionCustomGroup.setRollbackAfter(Constant.ROLLBACK_STEP_CLEARCACHE);

                customGroups = Arrays.asList(actionCustomGroup);
//                actionController.getNewObj().setAc
                ActionCustomAction customAction = new ActionCustomAction();
                customAction.setType(4);
                customAction.setPriority(1);
                customAction.setWaitReason(MessageUtil.getResourceBundleMessage("mantemance.server"));
                actionCustomGroup.getActionCustomActions().add(customAction);
            }
        }
    }

    public List<ActionCustomGroup> getCustomGroups() {
        return customGroups;
    }

    public void setCustomGroups(List<ActionCustomGroup> customGroups) {
        this.customGroups = customGroups;
    }

    public ActionCustomGroup getSelectedObj() {
        return selectedObj;
    }

    public void setSelectedObj(ActionCustomGroup selectedObj) {
        this.selectedObj = selectedObj;
    }

    public ActionCustomGroup getNewObj() {
        return newObj;
    }

    public void setNewObj(ActionCustomGroup newObj) {
        this.newObj = newObj;
    }

    public Boolean getIsEdit() {
        return isEdit;
    }

    public void setEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public Long getSearchId() {
        return this.searchId;
    }

    public void setSearchId(Long searchId) {
        this.searchId = searchId;
    }

    public Long getSearchActionId() {
        return this.searchActionId;
    }

    public void setSearchActionId(Long searchActionId) {
        this.searchActionId = searchActionId;
    }

    public String getSearchName() {
        return this.searchName;
    }

    public void setSearchName(String searchName) {
        this.searchName = searchName;
    }

    public String getSearchAfterGroup() {
        return this.searchAfterGroup;
    }

    public void setSearchAfterGroup(String searchAfterGroup) {
        this.searchAfterGroup = searchAfterGroup;
    }

    public DiagramModel getModel() {
        return model;
    }

    private Connection createConnection(EndPoint from, EndPoint to, String label) {
        Connection conn = new Connection(from, to);
        conn.getOverlays().add(new ArrowOverlay(20, 20, 1, 1));

        if (label != null) {
            conn.getOverlays().add(new LabelOverlay(label, "flow-label", 0.5));
        }

        return conn;
    }

    public ActionController getActionController() {
        return actionController;
    }

    public void setActionController(ActionController actionController) {
        this.actionController = actionController;
    }

    public List<SelectItem> getActionsSteps() {
        return actionsSteps;
    }

    public ActionCustomAction getNewActionObj() {
        return newActionObj;
    }

    public void setNewActionObj(ActionCustomAction newActionObj) {
        this.newActionObj = newActionObj;
    }

    public ActionCustomAction getSelectedActionObj() {
        return selectedActionObj;
    }

    public void setSelectedActionObj(ActionCustomAction selectedActionObj) {
        this.selectedActionObj = selectedActionObj;
    }

    public String actionType(ActionCustomAction action) {
        if (action == null || action.getType() == null)
            return "";
        String result = "";
        switch (action.getType()) {
            case 0:
                result = MessageUtil.getResourceBundleMessage("impact.module");
                break;
            case 1:
                result = MessageUtil.getResourceBundleMessage("impact.db");
                break;
            case 2:
                result = MessageUtil.getResourceBundleMessage("impact.according.to.the.file");
                break;
            case 3:
                result = MessageUtil.getResourceBundleMessage("wait.confirm");
                break;
            case 4:
                result = MessageUtil.getResourceBundleMessage("mantemance.server");
                break;
            default:
                break;
        }

        return result;
    }

    public TreeNode getSelectedUpcodeDir() {
        return selectedUpcodeDir;
    }

    public void setSelectedUpcodeDir(TreeNode selectedUpcodeDir) {
        this.selectedUpcodeDir = selectedUpcodeDir;
    }

    public TreeNode getUpcodeRoot() {
        return upcodeRoot;
    }

    public void setUpcodeRoot(TreeNode upcodeRoot) {
        this.upcodeRoot = upcodeRoot;
    }

    public List<SelectItem> getAppGroups() {
        return appGroups;
    }

    public void setAppGroups(List<SelectItem> appGroups) {
        this.appGroups = appGroups;
    }

    public Long getSelectedAppGroupId() {
        return selectedAppGroupId;
    }

    public void setSelectedAppGroupId(Long selectedAppGroupId) {
        this.selectedAppGroupId = selectedAppGroupId;
    }

    public List<ActionDtFile> getActionDtFiles() {
        return actionDtFiles;
    }

    public void setActionDtFiles(List<ActionDtFile> actionDtFiles) {
        this.actionDtFiles = actionDtFiles;
    }

    public TreeNode getRemoveUpcodeRoot() {
        return removeUpcodeRoot;
    }

    public void setRemoveUpcodeRoot(TreeNode removeUpcodeRoot) {
        this.removeUpcodeRoot = removeUpcodeRoot;
    }

    public TreeNode getSelectedRemoveUpcodeFile() {
        return selectedRemoveUpcodeFile;
    }

    public void setSelectedRemoveUpcodeFile(TreeNode selectedRemoveUpcodeFile) {
        this.selectedRemoveUpcodeFile = selectedRemoveUpcodeFile;
    }

    public DefaultDiagramModel getRollbackModel() {
        return rollbackModel;
    }

    public void setRollbackModel(DefaultDiagramModel rollbackModel) {
        this.rollbackModel = rollbackModel;
    }

    public void onNodeSelect(NodeSelectEvent event) {
        selectedRemoveUpcodeFile = event.getTreeNode();
		/*if (!event.isContextMenu()){
			System.out.println("ewgwegwegwe");
			selectedRemoveUpcodeFile = event.getTreeNode();
			//original code here.
		}*/
    }

    public String getCustomActionInfo(ActionCustomAction action) {
        String description = "";

        switch (action.getType()) {
            case 0:
                Module detail = actionController.getImpactModules().get(action.getModuleId());

                if (detail == null)
                    return description;
                switch (action.getModuleAction()) {
                    case Constant.SPECIAL_UPCODETEST_RESTART_STOP_START:
                        description += "Upcode test + restart(stop/start) " + detail.getModuleName();
                        break;
                    case Constant.SPECIAL_UPCODETEST_RESTART:
                        description += "Upcode test + restart " + detail.getModuleName();
                        break;
                    case Constant.SPECIAL_UPCODETEST_START:
                        description += "Upcode test + start " + detail.getModuleName();
                        break;
                    case Constant.SPECIAL_UPCODETEST_STOP_START:
                        description += "Upcode test + stop/start " + detail.getModuleName();
                        break;
                    case Constant.SPECIAL_RESTART_STOP_START:
                        description += "Restart(stop/start) " + detail.getModuleName();
                        break;
                    case Constant.SPECIAL_RESTART:
                        description += "Restart " + detail.getModuleName();
                        break;
                    case Constant.SPECIAL_START:
                        description += "Start " + detail.getModuleName();
                        break;
					/*case 5:
						description += "Start module " + detail.getAppName();
						break;
					case 6:
						description += "Restart module " + detail.getAppName();
						break;
					case 11:
						description += "Upcode test module " + detail.getAppName();
						break;*/
                }
                break;
            case 1:
                ServiceDatabase serviceDb = null;
                try {
                    serviceDb = iimService.findServiceDbById(actionController.getNewObj().getImpactProcess().getNationCode(), action.getDbId());
                } catch (AppException e) {
                    logger.error(e.getMessage(), e);
                }
                if (serviceDb == null)
                    break;
                switch (action.getDbAction()) {
                    case 0:
                        description += "Execute script " + action.getDbScriptFile() + ", Db: " + serviceDb.getUsername() + "@" + serviceDb.getDbName();
                        break;
                    case 1:
                        description += "Import file " + action.getImportDataFile() + ", Db " + serviceDb.getUsername() + "@" + serviceDb.getDbName();
                        break;
                    case 2:
                        description += "Export " + ", Db: " + serviceDb.getUsername() + "@" + serviceDb.getDbName();
                        break;
                }
                break;
            case 2:
                ActionDtFile dtFile = null;
                try {
                    dtFile = actionDtFileService.findById(action.getFileId());
                } catch (AppException e) {
                    logger.error(e.getMessage(), e);
                }
                if (dtFile != null)
                    description += MessageUtil.getResourceBundleMessage("common.execute") + " " + dtFile.getName();
                break;
            case 3:
                description += action.getWaitReason();
                break;
            case 4:
                description += MessageUtil.getResourceBundleMessage("mantemance.server");
                break;
            default:
                break;
        }

        return description;
    }

    public String findDtFile(Long id, boolean impact) {
        if (id == null)
            return "";
        String fileName = null;
        try {
            ActionDtFile dtFile = actionDtFileService.findById(id);

            if (dtFile == null)
                return "";

            fileName = impact ? dtFile.getImpactFile() : dtFile.getRollbackFile();
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        return fileName;
    }

    public StreamedContent downloadFile(Long id) {
        if (id == null)
            return null;

        InputStream stream = null;
        ActionDtFile dtFile = null;
        try {
            dtFile = actionDtFileService.findById(id);
            String filePath = UploadFileUtils.getImpactFileFolder() + File.separator + dtFile.getLocalFilename();
            stream = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        if (stream == null)
            return null;
        StreamedContent fileInput = new DefaultStreamedContent(stream, "", dtFile.getImpactFile());
        return fileInput;
    }

    public StreamedContent downloadRollbackFile(Long id) {
        InputStream stream = null;
        ActionDtFile dtFile = null;
        try {
            dtFile = actionDtFileService.findById(id);
            String filePath = UploadFileUtils.getImpactFileFolder() + File.separator + dtFile.getLocalRollbackFilename();
            stream = new FileInputStream(filePath);
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        if (stream == null)
            return null;
        StreamedContent fileInput = new DefaultStreamedContent(stream, "", dtFile.getRollbackFile());
        return fileInput;
    }

    public List<SelectItem> getActionsRollbackSteps() {
        return actionsRollbackSteps;
    }

    public void setActionsRollbackSteps(List<SelectItem> actionsRollbackSteps) {
        this.actionsRollbackSteps = actionsRollbackSteps;
    }
}
