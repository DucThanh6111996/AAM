package com.viettel.controller;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.google.common.base.Splitter;
import com.viettel.bean.*;
import com.viettel.it.util.Config;
import com.viettel.exception.AppException;
import com.viettel.it.util.LanguageBean;
import com.viettel.it.util.MessageUtil;
import com.viettel.model.Action;
import com.viettel.model.ActionDetailDatabase;
import com.viettel.persistence.ActionDetailDatabaseService;
import com.viettel.persistence.IimService;
import com.viettel.util.*;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.UploadedFile;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.math.BigDecimal;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author quanns2
 */
//@SessionScoped
@ViewScoped
@ManagedBean
public class ActionDetailDatabaseController implements Serializable {
    private static Logger logger = LogManager.getLogger(ActionController.class);

    @ManagedProperty(value = "#{actionDetailDatabaseService}")
    ActionDetailDatabaseService actionDetailDatabaseService;

    @ManagedProperty(value = "#{language}")
    LanguageBean languageBean;

    public LanguageBean getLanguageBean() {
        return languageBean;
    }

    public void setLanguageBean(LanguageBean languageBean) {
        this.languageBean = languageBean;
    }

    public void setActionDetailDatabaseService(ActionDetailDatabaseService actionDetailDatabaseService) {
        this.actionDetailDatabaseService = actionDetailDatabaseService;
    }

    @ManagedProperty(value = "#{iimService}")
    IimService iimService;

    public void setIimService(IimService iimService) {
        this.iimService = iimService;
    }

    private LazyDataModel<ActionDetailDatabase> lazyDataModel;
    private ActionDetailDatabase selectedObj;
    private ActionDetailDatabase newObj;

    private boolean isEdit;

    private Long searchId;
    private Long searchActionId;
    private Long searchDbId;
    private String secriptBackup;
    private Long searchType;
    private String searchTemplate;
    private String searchTemplatePath;
    private Long searchActionOrder;
    private String searchScriptExecute;
    private String searchAction;
    private String searchScriptBackup;
    private String searchCmdCompile;

    private List<ActionDetailDatabase> detailDatabases;
    private List<SelectItem> listDatabases;

    private List<ServiceDatabase> listDetailsDb;

    private Action action;

    private ActionController actionController;

    private ServiceDatabase selectedServiceDb;

    private String searchUploadFilePath;

    private String locate;

    private boolean isShowNodeType;
    private boolean hasError;

    @PostConstruct
    public void onStart() {
        clear();

        listDatabases = new ArrayList<>();
    }

    public void loadOldDb(ActionController actionController) {
        this.actionController = actionController;
        action = actionController.getNewObj();
        if (action != null && action.getId() != null) {
            Map<String, Object> filters = new HashMap<>();
            filters.put("actionId", action.getId() + "");
            try {
                detailDatabases = actionDetailDatabaseService.findList(filters, new HashMap<String, String>());
                if (detailDatabases != null) {
                    for (ActionDetailDatabase detailDatabase : detailDatabases) {
                        ServiceDatabase serviceDb = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), detailDatabase.getAppDbId());
                        detailDatabase.setServiceDatabase(serviceDb);
                    }
                }
            } catch (AppException | com.viettel.controller.AppException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            detailDatabases = new ArrayList<>();
        }
    }

    /*20181018_hoangnd_fix bug clone ActionDetailDatabase_start*/
    public void cloneOldDb(ActionController actionController) {
        this.actionController = actionController;
        action = actionController.getNewObj();
        if (action != null && action.getId() != null) {
            Map<String, Object> filters = new HashMap<>();
            filters.put("actionId", action.getId() + "");
            try {
                detailDatabases = actionDetailDatabaseService.findList(filters, new HashMap<String, String>());
                if (detailDatabases != null) {
                    for (ActionDetailDatabase detailDatabase : detailDatabases) {
                        ServiceDatabase serviceDb = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), detailDatabase.getAppDbId());
                        detailDatabase.setServiceDatabase(serviceDb);
                        detailDatabase.setRunStatus(null);
                        /*20181123_hoangnd_fix bug clone status_start*/
                        detailDatabase.setBackupStatus(null);
                        detailDatabase.setRollbackStatus(null);
                        /*20181123_hoangnd_fix bug clone status_end*/

                        //15-12-2018 KienPD start
                        detailDatabase.setIsAddRollback(null);
                        detailDatabase.setRunStartTime(null);
                        detailDatabase.setRunEndTime(null);
                        detailDatabase.setBackupStartTime(null);
                        detailDatabase.setBackupEndTime(null);
                        detailDatabase.setRollbackStartTime(null);
                        detailDatabase.setRollbackEndTime(null);
                        //15-12-2018 KienPD end
                    }
                }
            } catch (AppException | com.viettel.controller.AppException e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            detailDatabases = new ArrayList<>();
        }
    }
    /*20181018_hoangnd_fix bug clone ActionDetailDatabase_end*/

    public void viewSelectItems(ActionController actionController) {
        this.actionController = actionController;

        Set<Long> appGroupIds = new HashSet<>();
        Collection<Module> details = actionController.getImpactModules().values();
        for (Module detail : details) {
            appGroupIds.add(detail.getServiceId());
        }

        selectedServiceDb = null;
        try {
            //tuanda38_20180620_start
            List<ServiceDatabase> serviceDbs;
            if (action == null || action.getImpactProcess() == null) {
                serviceDbs = iimService.findServiceDbsByServices(AamConstants.NATION_CODE.VIETNAM, Arrays.asList(appGroupIds.toArray(new Long[appGroupIds.size()])));
            } else {
                serviceDbs = iimService.findServiceDbsByServices(action.getImpactProcess().getNationCode(), Arrays.asList(appGroupIds.toArray(new Long[appGroupIds.size()])));
            }
            //tuanda38_20180620_end
            listDatabases = new ArrayList<>();
            for (ServiceDatabase serviceDb : serviceDbs) {
                listDatabases.add(new SelectItem(serviceDb.getServiceDbId(), serviceDb.getUsername() + "@" + serviceDb.getDbName() + "@host:" + (serviceDb.getHost() != null ? serviceDb.getHost() : "null")));
                if (selectedServiceDb == null)
                    selectedServiceDb = serviceDb;
            }
            setListDetailsDb(serviceDbs);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        if (AamConstants.KB_GROUP.BD_UCTT.equals(actionController.getNewObj().getKbGroup())) {
            Set<Long> moduleIds = actionController.getImpactModules().keySet();
            if (moduleIds != null && !moduleIds.isEmpty()) {
                try {
                    Map<Long, ActionDetailDatabase> actionDetailDatabaseMap = new HashMap<>();
                    List<ActionSpecial> actionSpecials = iimService.findActionSpecial(action.getImpactProcess().getNationCode(), new ArrayList<>(moduleIds));

                    List<ActionDetailApp> actionDetailApps = actionController.getActionDetailAppController().getLstStop();
                    List<ActionDetailApp> detailApps = new ArrayList<>(actionDetailApps);
                    detailApps.addAll(actionController.getActionDetailAppController().getLstRestart());
                    Map<Long, Integer> moduleSpecials = new HashMap<>();
//
                    for (ActionDetailApp actionDetailApp : detailApps) {
                        for (ActionSpecial actionSpecial : actionSpecials) {
                            if (actionDetailApp.getModuleId().equals(actionSpecial.getMdId()) && AamConstants.ACTION_SPECIAL_MODE.STOP.equals(actionSpecial.getCodeMode())) {
/*                                ActionDetailDatabase actionDetailDatabase = actionDetailDatabaseMap.get(actionDetailApp.getModuleId());

                                if (actionDetailDatabase == null) {
                                    actionDetailDatabase = new ActionDetailDatabase();
                                }
                                actionDetailDatabase.setType(2L); //bat tat co
                                actionDetailDatabase.setAppDbId(actionSpecial.getServicesDbId());
                                actionDetailDatabase.setActionOrder(1L);
                                actionDetailDatabase.setTypeImport(0L);
                                actionDetailDatabase.setScriptText(actionSpecial.getActionContent());
                                actionDetailDatabase.setKbGroup(1);
                                actionDetailDatabase.setTestbedMode(0);*/

                                moduleSpecials.put(actionSpecial.getMdId(), AamConstants.ON_OFF_FLAG.STOP);
//                                actionDetailDatabase.setRunRollbackOnly(AamConstants.ON_OFF_FLAG.STOP);

//                                actionDetailDatabaseMap.put(actionDetailApp.getModuleId(), actionDetailDatabase);
                            }
                        }
                    }

                    actionDetailApps = actionController.getActionDetailAppController().getLstStart();
                    detailApps = new ArrayList<>(actionDetailApps);
                    detailApps.addAll(actionController.getActionDetailAppController().getLstRestart());

                    for (ActionDetailApp actionDetailApp : detailApps) {
                        for (ActionSpecial actionSpecial : actionSpecials) {
                            if (actionDetailApp.getModuleId().equals(actionSpecial.getMdId()) && AamConstants.ACTION_SPECIAL_MODE.START.equals(actionSpecial.getCodeMode())) {
/*                                ActionDetailDatabase actionDetailDatabase = actionDetailDatabaseMap.get(actionDetailApp.getModuleId());

                                if (actionDetailDatabase == null) {
                                    actionDetailDatabase = new ActionDetailDatabase();
                                }
                                actionDetailDatabase.setType(2L); //bat tat co
                                actionDetailDatabase.setAppDbId(actionSpecial.getServicesDbId());
                                actionDetailDatabase.setActionOrder(1L);
                                actionDetailDatabase.setTypeImport(0L);
                                actionDetailDatabase.setRollbackText(actionSpecial.getActionContent());
                                actionDetailDatabase.setKbGroup(1);
                                actionDetailDatabase.setTestbedMode(0);*/

//                                actionDetailDatabase.setRunRollbackOnly(moduleSpecials.get(actionSpecial.getMdId()) == null ? AamConstants.ON_OFF_FLAG.START : AamConstants.ON_OFF_FLAG.STOP_START);

                                moduleSpecials.put(actionSpecial.getMdId(), moduleSpecials.get(actionSpecial.getMdId()) == null ? AamConstants.ON_OFF_FLAG.START : AamConstants.ON_OFF_FLAG.STOP_START);

//                                actionDetailDatabaseMap.put(actionDetailApp.getModuleId(), actionDetailDatabase);
                            }
                        }
                    }

                    //+++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++++
                    actionDetailApps = actionController.getActionDetailAppController().getLstStop();
                    detailApps = new ArrayList<>(actionDetailApps);
                    detailApps.addAll(actionController.getActionDetailAppController().getLstRestart());
                    detailApps.addAll(actionController.getActionDetailAppController().getLstStart());
//
                    for (ActionDetailApp actionDetailApp : detailApps) {
                        for (ActionSpecial actionSpecial : actionSpecials) {
                            if (actionDetailApp.getModuleId().equals(actionSpecial.getMdId()) && (AamConstants.ACTION_SPECIAL_MODE.STOP.equals(actionSpecial.getCodeMode()) ||
                                    AamConstants.ACTION_SPECIAL_MODE.START.equals(actionSpecial.getCodeMode()))) {
                                ActionDetailDatabase actionDetailDatabase = actionDetailDatabaseMap.get(actionDetailApp.getModuleId());

                                if (actionDetailDatabase == null) {
                                    actionDetailDatabase = new ActionDetailDatabase();
                                }
                                actionDetailDatabase.setType(2L); //bat tat co
                                actionDetailDatabase.setAppDbId(actionSpecial.getServicesDbId());
                                actionDetailDatabase.setActionOrder(1L);
                                actionDetailDatabase.setTypeImport(0L);
                                if (AamConstants.ACTION_SPECIAL_MODE.STOP.equals(actionSpecial.getCodeMode()))
                                    actionDetailDatabase.setScriptText(actionSpecial.getActionContent());
                                else
                                    actionDetailDatabase.setRollbackText(actionSpecial.getActionContent());

                                actionDetailDatabase.setKbGroup(1);
                                actionDetailDatabase.setTestbedMode(0);

//                                moduleSpecials.put(actionSpecial.getMdId(), AamConstants.ON_OFF_FLAG.STOP);
                                actionDetailDatabase.setRunRollbackOnly(moduleSpecials.get(actionSpecial.getMdId()));

                                actionDetailDatabaseMap.put(actionDetailApp.getModuleId(), actionDetailDatabase);
                            }
                        }
                    }

                    detailDatabases = new ArrayList<>(actionDetailDatabaseMap.values());
                } catch (AppException | com.viettel.controller.AppException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                detailDatabases = new ArrayList<>();
            }
        }
    }

    public void handleFileUploadBakup(FileUploadEvent event) {
        logger.info(event.getFile().getFileName() + " is uploaded.");

        UploadedFile file = event.getFile();
        String uploadFolder = UploadFileUtils.getDatabaseFolder(action);
        if (file != null) {
            BigDecimal seq = null;
            try {
                seq = actionController.getActionService().nextFileSeq();
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
            String sourceCode = Util.convertUTF8ToNoSign(FilenameUtils.getBaseName(file.getFileName()) + "_" + seq + "." + FilenameUtils.getExtension(file.getFileName()));
            FileHelper.uploadFile(uploadFolder, file, sourceCode);

            String endCoding = UploadFileUtils.checkEncoding(uploadFolder + File.separator + sourceCode);
            File sqlFile = new File(uploadFolder + File.separator + sourceCode);
            if (endCoding == null || "UTF-8".equals(endCoding)) {
                try {
                    List<String> lines = FileUtils.readLines(sqlFile);
                    if (!SqlUtils.checkStatmentSeparator(lines)) {
                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("commands.must.be.character.finish"), "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", message);
                    } else if (!checkAt(FileUtils.readFileToString(sqlFile))) {
                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("begin.line.do.not.start.with.character.at"), "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", message);
                    } else {
                        newObj.setScriptBackup(sourceCode);
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("script.must.use.code.utf8"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", message);
            }
        }
        // 23-11-2018 KienPD check disabled time out start
        disabledTimeOutBackUp = newObj.getScriptBackup() == null || "".equals(newObj.getScriptBackup());
        // 23-11-2018 KienPD check disabled time out end
    }

    public void handleFileUploadRollback(FileUploadEvent event) {
        logger.info(event.getFile().getFileName() + " is uploaded.");

        UploadedFile file = event.getFile();
        String uploadFolder = UploadFileUtils.getDatabaseFolder(action);
        if (file != null) {
            BigDecimal seq = null;
            try {
                seq = actionController.getActionService().nextFileSeq();
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
            String sourceCode = Util.convertUTF8ToNoSign(FilenameUtils.getBaseName(file.getFileName()) + "_" + seq + "." + FilenameUtils.getExtension(file.getFileName()));

            FileHelper.uploadFile(uploadFolder, file, sourceCode);

            String endCoding = UploadFileUtils.checkEncoding(uploadFolder + File.separator + sourceCode);
            File sqlFile = new File(uploadFolder + File.separator + sourceCode);
            if (endCoding == null || "UTF-8".equals(endCoding)) {
                try {
                    List<String> lines = FileUtils.readLines(new File(uploadFolder + File.separator + sourceCode));
                    if (!SqlUtils.checkStatmentSeparator(lines)) {
                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("commands.must.be.character.finish"), "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", message);
                    } else if (!checkAt(FileUtils.readFileToString(sqlFile))) {
                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("begin.line.do.not.start.with.character.at"), "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", message);
                    } else {
                        newObj.setRollbackFile(sourceCode);
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("script.must.use.code.utf8"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", message);
            }
        }
        // 23-11-2018 KienPD check disabled time out start
        disabledTimeOutRollback = newObj.getRollbackFile() == null || "".equals(newObj.getRollbackFile());
        // 23-11-2018 KienPD check disabled time out end
    }

    public void handleFileUpload(FileUploadEvent event) {
        logger.info(event.getFile().getFileName() + " is uploaded.");

        UploadedFile file = event.getFile();
        String uploadFolder = UploadFileUtils.getDatabaseFolder(action);
        if (file != null) {
            BigDecimal seq = null;
            try {
                seq = actionController.getActionService().nextFileSeq();
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
            String sourceCode = Util.convertUTF8ToNoSign(FilenameUtils.getBaseName(file.getFileName()) + "_" + seq + "." + FilenameUtils.getExtension(file.getFileName()));

            FileHelper.uploadFile(uploadFolder, file, sourceCode);
            String endCoding = UploadFileUtils.checkEncoding(uploadFolder + File.separator + sourceCode);
            File sqlFile = new File(uploadFolder + File.separator + sourceCode);
            if (endCoding == null || "UTF-8".equals(endCoding)) {
                try {
                    List<String> lines = FileUtils.readLines(new File(uploadFolder + File.separator + sourceCode));
                    if (!SqlUtils.checkStatmentSeparator(lines)) {
                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("commands.must.be.character.finish"), "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", message);
                    } else if (!checkAt(FileUtils.readFileToString(sqlFile))) {
                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("begin.line.do.not.start.with.character.at"), "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", message);
                    } else {
                        newObj.setScriptExecute(sourceCode);
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("script.must.use.code.utf8"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", message);
            }
        }
        // 23-11-2018 KienPD check disabled time out start
        disabledTimeOutImpact = newObj.getScriptExecute() == null || "".equals(newObj.getScriptExecute());
        // 23-11-2018 KienPD check disabled time out end
    }

    public void handleFileUploadData(FileUploadEvent event) {
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

            String endCoding = UploadFileUtils.checkEncoding(uploadFolder + File.separator + sourceCode);
            if (endCoding == null || "UTF-8".equals(endCoding)) {
                newObj.setImportDataFile(sourceCode);
            } else {
                FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("script.must.use.code.utf8"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", message);
            }
        }
    }

    private boolean checkAt(String script) {
        if (StringUtils.isEmpty(script))
            return true;

        List<String> sqls = Splitter.on("\n").splitToList(script.replaceAll("\r", ""));
        for (String sql : sqls) {
            if (sql.startsWith("@"))
                return false;
        }

        return true;
    }

    public void prepareEdit(ActionDetailDatabase obj) {
        isEdit = true;
        selectedObj = obj;
        BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
        newObj = new ActionDetailDatabase();
        try {
            BeanUtils.copyProperties(newObj, obj);
            //newObj.setPassword("");
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
        //30-11-2018 KienPD check disabled time out start
        disabledTimeOutBackUp = !(StringUtils.isNotEmpty(newObj.getScriptBackup()) || StringUtils.isNotEmpty(newObj.getBackupText()));
        disabledTimeOutImpact = !(StringUtils.isNotEmpty(newObj.getScriptExecute()) || StringUtils.isNotEmpty(newObj.getScriptText()));
        disabledTimeOutRollback = !(StringUtils.isNotEmpty(newObj.getRollbackFile()) || StringUtils.isNotEmpty(newObj.getRollbackText()));
        //30-11-2018 KienPD check disabled time out end
        if (newObj.getAppDbId() != null)
            try {
                selectedServiceDb = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), newObj.getAppDbId());
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            } catch (com.viettel.controller.AppException e) {
                e.printStackTrace();
            }
    }

    public void clear() {
        isEdit = false;
        newObj = new ActionDetailDatabase();
        newObj.setActionOrder(1L);
        newObj.setSperator(";");
        newObj.setTypeImport(1L);
        newObj.setType(0L);
        newObj.setKbGroup(1);
        newObj.setTestbedMode(0);
        // 23-11-2018 KienPD check disabled time out start
        disabledTimeOutBackUp = true;
        disabledTimeOutImpact = true;
        disabledTimeOutRollback = true;
        // 23-11-2018 KienPD check disabled time out end
    }

    public void init() {
        clear();
        detailDatabases = new ArrayList<>();
    }

    public void duplicate(ActionDetailDatabase obj) {
        isEdit = false;
        obj.setId(null);
//		selectedObj = obj;
        newObj = new ActionDetailDatabase();
        BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
        try {
            BeanUtils.copyProperties(newObj, obj);
            // newObj.setPassword("");
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void loadDb() {
        try {
            if (newObj.getAppDbId() != null)
                selectedServiceDb = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), newObj.getAppDbId());
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void saveOrUpdate() {
        FacesMessage msg = null;
        try {
            if (newObj.getType() == 0) {
                newObj.setTimeOutTurnOff(null);
                newObj.setTimeOutTurnOn(null);
                if ((newObj.getScriptBackup() != null || newObj.getBackupText() != null) && newObj.getTimeOutBackup() != null &&
                        (newObj.getTimeOutBackup() < timeOutMin || newObj.getTimeOutBackup() > timeOutMax)) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.backup"), timeOutMin, timeOutMax));
                    return;
                } else if ((newObj.getScriptBackup() != null || newObj.getBackupText() != null) && newObj.getTimeOutBackup() == null) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.backup"), timeOutMin, timeOutMax));
                    return;
                }
                if ((newObj.getScriptExecute() != null || newObj.getScriptText() != null) && newObj.getTimeOutImpact() != null &&
                        (newObj.getTimeOutImpact() < timeOutMin || newObj.getTimeOutImpact() > timeOutMax)) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.impact"), timeOutMin, timeOutMax));
                    return;
                } else if ((newObj.getScriptExecute() != null || newObj.getScriptText() != null) && newObj.getTimeOutImpact() == null) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.impact"), timeOutMin, timeOutMax));
                    return;
                }
                if ((newObj.getRollbackFile() != null || newObj.getRollbackText() != null) && newObj.getTimeOutRollback() != null &&
                        (newObj.getTimeOutRollback() < timeOutMin || newObj.getTimeOutRollback() > timeOutMax)) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.rollback"), timeOutMin, timeOutMax));
                    return;
                } else if ((newObj.getRollbackFile() != null || newObj.getRollbackText() != null) && newObj.getTimeOutRollback() == null) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.rollback"), timeOutMin, timeOutMax));
                    return;
                }
            }
            if (newObj.getType() == 1) {
                newObj.setTimeOutTurnOff(null);
                newObj.setTimeOutTurnOn(null);
                newObj.setTimeOutImpact(null);
                if ((newObj.getScriptBackup() != null || newObj.getBackupText() != null) && newObj.getTimeOutBackup() != null &&
                        (newObj.getTimeOutBackup() < timeOutMin || newObj.getTimeOutBackup() > timeOutMax)) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.backup"), timeOutMin, timeOutMax));
                    return;
                } else if ((newObj.getScriptBackup() != null || newObj.getBackupText() != null) && newObj.getTimeOutBackup() == null) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.backup"), timeOutMin, timeOutMax));
                    return;
                }
                if ((newObj.getRollbackFile() != null || newObj.getRollbackText() != null) && newObj.getTimeOutRollback() != null &&
                        (newObj.getTimeOutRollback() < timeOutMin || newObj.getTimeOutRollback() > timeOutMax)) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.rollback"), timeOutMin, timeOutMax));
                    return;
                } else if ((newObj.getRollbackFile() != null || newObj.getRollbackText() != null) && newObj.getTimeOutRollback() == null) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.rollback"), timeOutMin, timeOutMax));
                    return;
                }
            }
            if (newObj.getType() == 2) {
                newObj.setTimeOutBackup(null);
                newObj.setTimeOutImpact(null);
                newObj.setTimeOutRollback(null);
                if ((newObj.getScriptExecute() != null || newObj.getScriptText() != null) && newObj.getTimeOutTurnOff() != null &&
                        (newObj.getTimeOutTurnOff() < timeOutMin || newObj.getTimeOutTurnOff() > timeOutMax)) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.off.flag"), timeOutMin, timeOutMax));
                    return;
                } else if ((newObj.getScriptExecute() != null || newObj.getScriptText() != null) && newObj.getTimeOutTurnOff() == null) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.off.flag"), timeOutMin, timeOutMax));
                    return;
                }
                if ((newObj.getRollbackFile() != null || newObj.getRollbackText() != null) && newObj.getTimeOutTurnOn() != null &&
                        (newObj.getTimeOutTurnOn() < timeOutMin || newObj.getTimeOutTurnOn() > timeOutMax)) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.on.flag"), timeOutMin, timeOutMax));
                    return;
                } else if ((newObj.getRollbackFile() != null || newObj.getRollbackText() != null) && newObj.getTimeOutTurnOn() == null) {
                    MessageUtil.setErrorMessage(String.format(MessageUtil.getResourceBundleMessage("msg.validate.time.out.on.flag"), timeOutMin, timeOutMax));
                    return;
                }
            }
            //01-11-2018 KienPD validate time out end
            if (newObj.getAppDbId() == null || newObj.getAppDbId() == 0) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("mop.db.null"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                return;
            }

            if (newObj.getKbGroup() == null || newObj.getKbGroup() <= 0) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("mop.db.kbgroup.null"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                return;
            }
//            ServiceDb serviceDb = getListDetailsDb().get(0);
            ServiceDatabase serviceDb = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), newObj.getAppDbId());
            newObj.setServiceDatabase(serviceDb);
            Long actionOrder = newObj.getActionOrder();
            List<String> listTableName = new ArrayList<>();
            for (ActionDetailDatabase detailDatabase : detailDatabases) {
                if (detailDatabase.getActionOrder() < actionOrder) {
                    String scriptText = detailDatabase.getScriptText();
                    if (StringUtils.isEmpty(scriptText)) {
                        File executeFile = new File(UploadFileUtils.getDatabaseFolder(action)
                                + File.separator + newObj.getScriptExecute());
                        scriptText = FileUtils.readFileToString(executeFile);
                    }

                    Pattern pattern;
                    Matcher matcher;
                    pattern = Pattern.compile(Constant.PATTERN.DDL_CREATE_EXIST, Pattern.CASE_INSENSITIVE);
                    matcher = pattern.matcher(scriptText);
                    if (!matcher.find()) {
                        String[] arrScript = scriptText.split("/|;");
                        for (String script : arrScript) {
                            pattern = Pattern.compile(Constant.PATTERN.DDL_CREATE_TABLE, Pattern.CASE_INSENSITIVE);
                            matcher = pattern.matcher(script);
                            if (matcher.find()) {
                                String schemasObject = matcher.group(3).replaceAll("\"", "");
                                if (schemasObject.contains(".")) {
                                    listTableName.add(schemasObject.toUpperCase());
                                } else {
                                    listTableName.add(serviceDb.getUsername().toUpperCase() + "." + schemasObject.toUpperCase());
                                }
                            }
                        }
                    }
                }
            }
            if (newObj.getType().equals(0L)) {
                if (newObj.getTypeImport().equals(0L)) {
                    String message = SqlUtils.checkTable(newObj.getBackupText(), newObj.getScriptText(), newObj.getRollbackText(),
                            serviceDb, listTableName);
                    if (StringUtils.isNotEmpty(message)) {
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                        return;
                    }
                } else if (newObj.getTypeImport().equals(1L)) {
                    File backupFile = new File(UploadFileUtils.getDatabaseFolder(action) + File.separator + newObj.getScriptBackup());
                    File executeFile = new File(UploadFileUtils.getDatabaseFolder(action) + File.separator + newObj.getScriptExecute());
                    File rollbackFile = new File(UploadFileUtils.getDatabaseFolder(action) + File.separator + newObj.getRollbackFile());
//				havt
//				newObj.setScript_path(UploadFileUtils.getDatabaseFolder(action));
                    String message = null;
                    try {
                        message = SqlUtils.checkTable(backupFile, executeFile, rollbackFile, serviceDb, listTableName);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        message = "";
                    } finally {
                    }
                    if (StringUtils.isNotEmpty(message)) {
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, message, "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                        return;
                    }
                }
            }

//			selectedObj = new ActionDetailDatabase();

/*			BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
            BeanUtils.copyProperties(selectedObj, newObj);*/

            if (!isEdit) {
//				selectedObj.setId(null);
                if (detailDatabases == null)
                    detailDatabases = new ArrayList<>();
                detailDatabases.add(newObj);
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

            newObj = new ActionDetailDatabase();
            isEdit = false;
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            RequestContext.getCurrentInstance().execute("PF('editDialogDb').hide()");
        } catch (Exception e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("update.failed"), "");
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            logger.error(e.getMessage(), e);
        } finally {

        }
    }

    public void delete() {
        FacesMessage msg = null;
        try {
            detailDatabases.remove(selectedObj);
            msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("delete.successful"), "");
        } catch (Exception e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("delete.failed"), "");
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
        }
    }

    public StreamedContent downloadFile(String fileName) {
        StreamedContent fileInput = null;
        String filePath = UploadFileUtils.getDatabaseFolder(action) + File.separator + fileName;

        try {
            fileInput = new DefaultStreamedContent(new FileInputStream(filePath), "", fileName);

        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return fileInput;
    }

    public LazyDataModel<ActionDetailDatabase> getLazyDataModel() {
        return lazyDataModel;
    }

    public void setLazyDataModel(LazyDataModel<ActionDetailDatabase> lazyDataModel) {
        this.lazyDataModel = lazyDataModel;
    }

    public ActionDetailDatabase getSelectedObj() {
        return selectedObj;
    }

    public void setSelectedObj(ActionDetailDatabase selectedObj) {
        this.selectedObj = selectedObj;
    }

    public ActionDetailDatabase getNewObj() {
        return newObj;
    }

    public void setNewObj(ActionDetailDatabase newObj) {
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

    public Long getSearchDbId() {
        return this.searchDbId;
    }

    public void setSearchDbId(Long searchDbId) {
        this.searchDbId = searchDbId;
    }

    public String getSearchAction() {
        return this.searchAction;
    }

    public void setSearchAction(String searchAction) {
        this.searchAction = searchAction;
    }

    public Long getSearchActionOrder() {
        return this.searchActionOrder;
    }

    public void setSearchActionOrder(Long searchActionOrder) {
        this.searchActionOrder = searchActionOrder;
    }

    public String getSearchScriptExecute() {
        return this.searchScriptExecute;
    }

    public void setSearchScriptExecute(String searchScriptExecute) {
        this.searchScriptExecute = searchScriptExecute;
    }

    public String getSearchScriptBackup() {
        return this.searchScriptBackup;
    }

    public void setSearchScriptBackup(String searchScriptBackup) {
        this.searchScriptBackup = searchScriptBackup;
    }

    public Long getSearchType() {
        return this.searchType;
    }

    public void setSearchType(Long searchType) {
        this.searchType = searchType;
    }

    public String getSearchTemplate() {
        return this.searchTemplate;
    }

    public void setSearchTemplate(String searchTemplate) {
        this.searchTemplate = searchTemplate;
    }

    public String getSearchTemplatePath() {
        return this.searchTemplatePath;
    }

    public void setSearchTemplatePath(String searchTemplatePath) {
        this.searchTemplatePath = searchTemplatePath;
    }

    public String getSearchCmdCompile() {
        return this.searchCmdCompile;
    }

    public void setSearchCmdCompile(String searchCmdCompile) {
        this.searchCmdCompile = searchCmdCompile;
    }

    public List<ActionDetailDatabase> getDetailDatabases() {
        return detailDatabases;
    }

    public void setDetailDatabases(List<ActionDetailDatabase> detailDatabases) {
        this.detailDatabases = detailDatabases;
    }

    public List<SelectItem> getListDatabases() {
        return listDatabases;
    }

    public void setListDatabases(List<SelectItem> listDatabases) {
        this.listDatabases = listDatabases;
    }

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

    public List<ServiceDatabase> getListDetailsDb() {
        return listDetailsDb;
    }

    public void setListDetailsDb(List<ServiceDatabase> listDetailsDb) {
        this.listDetailsDb = listDetailsDb;
    }

    public ActionDetailDatabaseService getActionDetailDatabaseService() {
        return actionDetailDatabaseService;
    }

    public ServiceDatabase getSelectedServiceDb() {
        return selectedServiceDb;
    }

    public void setSelectedServiceDb(ServiceDatabase selectedServiceDb) {
        this.selectedServiceDb = selectedServiceDb;
    }

    public ActionController getActionController() {
        return actionController;
    }

    public void setActionController(ActionController actionController) {
        this.actionController = actionController;
    }

    //01-11-2018 KienPD validate time out start
    private int timeOutMin;
    private int timeOutMax;

    public int getTimeOutMin() {
        return timeOutMin;
    }

    public void setTimeOutMin(int timeOutMin) {
        this.timeOutMin = timeOutMin;
    }

    public int getTimeOutMax() {
        return timeOutMax;
    }

    public void setTimeOutMax(int timeOutMax) {
        this.timeOutMax = timeOutMax;
    }

    private boolean disabledTimeOutBackUp;
    private boolean disabledTimeOutImpact;
    private boolean disabledTimeOutRollback;

    public boolean isDisabledTimeOutImpact() {
        return disabledTimeOutImpact;
    }

    public void setDisabledTimeOutImpact(boolean disabledTimeOutImpact) {
        this.disabledTimeOutImpact = disabledTimeOutImpact;
    }

    public boolean isDisabledTimeOutRollback() {
        return disabledTimeOutRollback;
    }

    public void setDisabledTimeOutRollback(boolean disabledTimeOutRollback) {
        this.disabledTimeOutRollback = disabledTimeOutRollback;
    }

    public boolean isDisabledTimeOutBackUp() {
        return disabledTimeOutBackUp;
    }

    public void setDisabledTimeOutBackUp(boolean disabledTimeOutBackUp) {
        this.disabledTimeOutBackUp = disabledTimeOutBackUp;
    }

    public void checkDisabledTimeOut() {
        disabledTimeOutBackUp = newObj.getBackupText() == null || "".equals(newObj.getBackupText());
        disabledTimeOutImpact = newObj.getScriptText() == null || "".equals(newObj.getScriptText());
        disabledTimeOutRollback = newObj.getRollbackText() == null || "".equals(newObj.getRollbackText());
    }

    public void disabledTimeOut() {
        if (!isEdit) {
            disabledTimeOutBackUp = true;
            disabledTimeOutImpact = true;
            disabledTimeOutRollback = true;
            newObj.setScriptText(null);
            newObj.setBackupText(null);
            newObj.setRollbackText(null);
            newObj.setScriptExecute(null);
            newObj.setScriptBackup(null);
            newObj.setRollbackFile(null);
            newObj.setTimeOutImpact(null);
            newObj.setTimeOutBackup(null);
            newObj.setTimeOutRollback(null);
        }
    }
    //01-11-2018 KienPD validate time out end

    public void setSearchUploadFilePath(String searchUploadFilePath) {
        this.searchUploadFilePath = searchUploadFilePath;
    }

    public String getSearchUploadFilePath() {
        return this.searchUploadFilePath;
    }

    /**
     * ThanhTD_Unikom
     **/

    List<MopDatabaseDTO> listMopDb = new ArrayList<>();

    private boolean importSuccess = true;

    public boolean isImportSuccess() {
        return importSuccess;
    }

    public void setImportSuccess(boolean importSuccess) {
        this.importSuccess = importSuccess;
    }

    private String getNameFile(String fileName) {
        String fName;
        int mid = fileName.lastIndexOf(".");
        fName = fileName.substring(0, mid);
        return fName;
    }

    private String getFileExtension(String fileName) {
        String fExt;
        int mid = fileName.lastIndexOf(".");
        fExt = fileName.substring(mid, fileName.length());
        return fExt;
    }

    public void importFileDatabase() {

        if ("".equals(fileExcelDb) || "".equals(fileZipDb)) {
            MessageUtil.setErrorMessage(MessageFormat.format("{0}",
                    MessageUtil.getResourceBundleMessage("file.excel.or.zip.not.upload.yet")));
            return;
        }

        ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        String extractPath = context.getRealPath("/") + "TEMPORARY/UPCODE/"
                + actionController.getUsername() + "/ZIP/" + "EXTRACT/";

        File fileZip = new File(fileZipDb);
        if (fileZip == null) return;

        List<FileHeader> headers = null;
        try {
            ZipFile zipFile = new ZipFile(fileZip.getPath());
            headers = zipFile.getFileHeaders();
            zipFile.extractAll(extractPath);
        } catch (Exception ex) {

        }

        try {
            importSuccess = true;
            Workbook wb = WorkbookFactory.create(new FileInputStream(new File(fileExcelDb)));
            Sheet sheet = wb.getSheetAt(0);

            Row row;
            DataFormatter format = new DataFormatter();
            listMopDb = new ArrayList<>();
            row = sheet.getRow(0);
            if(format.formatCellValue(row.getCell(0)) == null || format.formatCellValue(row.getCell(1))== null
                    ||format.formatCellValue(row.getCell(2))== null||format.formatCellValue(row.getCell(3))== null
                    ||format.formatCellValue(row.getCell(4))== null||format.formatCellValue(row.getCell(5))== null
                    ||format.formatCellValue(row.getCell(6))== null||format.formatCellValue(row.getCell(7))== null
                    ||format.formatCellValue(row.getCell(8))== null){
                MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                        MessageUtil.getResourceBundleMessage("upload.file.database.wrong.format")));
            }

            for (int i = 1; i <= sheet.getLastRowNum(); i++) {
                row = sheet.getRow(i);

                MopDatabaseDTO mopDatabaseDTO = new MopDatabaseDTO();
                mopDatabaseDTO.setActionOrder(Long.parseLong(format.formatCellValue(row.getCell(0))));
                mopDatabaseDTO.setAppDbId(Long.parseLong(format.formatCellValue(row.getCell(1))));
                mopDatabaseDTO.setDbMap(format.formatCellValue(row.getCell(2)));
                mopDatabaseDTO.setScriptBackup(format.formatCellValue(row.getCell(3)));
                mopDatabaseDTO.setTimeOutBackup(format.formatCellValue(row.getCell(4)));
                mopDatabaseDTO.setScriptExecute(format.formatCellValue(row.getCell(5)));
                mopDatabaseDTO.setTimeOutImpact(format.formatCellValue(row.getCell(6)));
                mopDatabaseDTO.setRollbackFile(format.formatCellValue(row.getCell(7)));
                mopDatabaseDTO.setTimeOutRollback(format.formatCellValue(row.getCell(8)));
                listMopDb.add(mopDatabaseDTO);
            }
            detailDatabases = new ArrayList<>();
            for (MopDatabaseDTO mopDb : listMopDb) {
                if (!addMopDataBase(mopDb, headers, extractPath)) {
                    importSuccess = false;
                    break;
                }
            }

            if (importSuccess) {
                MessageUtil.setInfoMessageFromRes("import.database.success");
                RequestContext.getCurrentInstance().execute("PF('importDbDialog').hide()");
            } else {
                MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                        MessageUtil.getResourceBundleMessage("upload.impact.database.error")));
                //return;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    MessageUtil.getResourceBundleMessage("upload.database.crash")));
        }

    }

    /**
     * 2020-09-18 ThanhTD_ Download file template
     **/
    public StreamedContent onDownloadDatabaseTemp() {
        Workbook wb = null;
        try {
            ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            locate = languageBean.getLocaleCode();
            String templatePath = context.getRealPath("/") + "templates" + File.separator
                    + "import" + File.separator + (isShowNodeType ? (locate.equals("vi")
                    ? "ImportDatabaseTemplate_vi.xlsx" : "ImportDatabaseTemplate_en.xlsx") :
                    (locate.equals("vi") ? "ImportDatabaseTemplate_vi.xlsx" : "ImportDatabaseTemplate_en.xlsx"));


            wb = WorkbookFactory.create(new File(templatePath));
            Sheet sheet = wb.getSheetAt(1);
            int index = 1;
            for (SelectItem item : listDatabases) {
                Row row = sheet.createRow(index);
                Cell indexCell = row.createCell(0);
                indexCell.setCellValue(index);
                Cell valueCell = row.createCell(1);
                valueCell.setCellValue(item.getValue().toString());
                Cell mapCell = row.createCell(2);
                mapCell.setCellValue(item.getLabel());
                index++;
            }

            ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                    .getExternalContext().getContext();
            String pathOut = ctx.getRealPath("/") + Config.PATH_OUT + (isShowNodeType ? "ImportDatabaseTemplate_vi.xlsx" : "ImportDatabaseTemplate_en.xlsx");

            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(pathOut);
                wb.write(fileOut);
                wb.close();

                return new DefaultStreamedContent(new FileInputStream(pathOut), ".xlsx", "Import_Database_template_en.xlsx");
            } catch (Exception ex) {
                MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                        MessageUtil.getResourceBundleMessage("button.download.template")));
                logger.error(ex.getMessage(), ex);
            } finally {
                try {
                    if (fileOut != null) {
                        fileOut.close();
                    }
                } catch (IOException ex) {
                    logger.error(ex);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    MessageUtil.getResourceBundleMessage("button.download.template")));
        } finally {
            if (wb != null)
                try {
                    wb.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
        }
        return null;
    }

    private boolean checkFormatSql(String input) {
        try {
            String[] formatScriptArr = input.split("\\.");
            String surfix = formatScriptArr[formatScriptArr.length - 1];
            if ("sql".toLowerCase().equals(surfix)) {
                return true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
        return false;
    }

    public StreamedContent onDownloadImportDbResult() {
        Workbook wb = null;
        try {
            ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            locate = languageBean.getLocaleCode();
            String templatePath = context.getRealPath("/") + "templates" + File.separator
                    + "import" + File.separator + (isShowNodeType ? (locate.equals("vi") ? "ImportDatabaseResult_vi.xlsx" : "ImportDatabaseResult_en.xlsx") : (locate.equals("vi") ? "ImportDatabaseResult_vi.xlsx" : "ImportDatabaseResult_en.xlsx"));


            wb = WorkbookFactory.create(new File(templatePath));


            ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                    .getExternalContext().getContext();
            String pathOut = ctx.getRealPath("/") + Config.PATH_OUT + (isShowNodeType ? "ImportDatabaseResult_vi.xlsx" : "ImportDatabaseResult_en.xlsx");

            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(pathOut);
                Sheet sheet = wb.getSheetAt(0);

                int index = 1;
                for (MopDatabaseDTO item : listMopDb) {
                    Row row = sheet.createRow(index);
                    Cell indexCell = row.createCell(0);
                    indexCell.setCellValue(index);
                    Cell moduleCodeCell = row.createCell(1);
                    moduleCodeCell.setCellValue(item.getActionOrder());
                    Cell moduleNameCell = row.createCell(2);
                    moduleNameCell.setCellValue(item.getAppDbId());
                    Cell upcodePathCell = row.createCell(3);
                    upcodePathCell.setCellValue(item.getDbMap());
                    Cell uploadPathCell = row.createCell(4);
                    uploadPathCell.setCellValue(item.getScriptBackup());
                    Cell timeoutBackupCell = row.createCell(5);
                    timeoutBackupCell.setCellValue(item.getTimeOutBackup());

                    Cell excuteell = row.createCell(6);
                    excuteell.setCellValue(item.getScriptExecute());
                    Cell timeoutExcuteCell = row.createCell(7);
                    timeoutExcuteCell.setCellValue(item.getTimeOutImpact());

                    Cell rollbackCell = row.createCell(8);
                    rollbackCell.setCellValue(item.getRollbackFile());
                    Cell timeoutRollbackCell = row.createCell(9);
                    timeoutRollbackCell.setCellValue(item.getTimeOutRollback());

                    Cell resultCell = row.createCell(10);
                    resultCell.setCellValue(item.getResult());

                    Cell messageCell = row.createCell(11);
                    messageCell.setCellValue(item.getMessage());
                    index++;
                }
                wb.write(fileOut);
                wb.close();
                return new DefaultStreamedContent(new FileInputStream(pathOut), ".xlsx", "Import_Database_Result_en.xlsx");
            } catch (Exception ex) {
                MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                        MessageUtil.getResourceBundleMessage("button.download.template")));
                logger.error(ex.getMessage(), ex);
            } finally {
                try {
                    if (fileOut != null) {
                        fileOut.close();
                    }
                } catch (IOException ex) {
                    logger.error(ex);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    MessageUtil.getResourceBundleMessage("button.download.template")));
        } finally {
            if (wb != null)
                try {
                    wb.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
        }
        return null;
    }

    private String fileExcelDb = "";
    private String nameFileExcelDb = "";

    public String getNameFileExcelDb() {
        return nameFileExcelDb;
    }

    public String getFileExcelDb() {
        return fileExcelDb;
    }

    public void handUploadFileExcelDb(FileUploadEvent event) {

        UploadedFile file = event.getFile();
        nameFileExcelDb = file.getFileName();

        ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        locate = languageBean.getLocaleCode();
        String tempPath = context.getRealPath("/") + "TEMPORARY/DB/" + actionController.getUsername() + "/EXCEL/";
        FileHelper.removeFile(tempPath);
        if (file != null) {
            String listFile = Util.convertUTF8ToNoSign(getNameFile(file.getFileName())) + getFileExtension(file.getFileName());
            FileHelper.uploadFile(tempPath, file, listFile);
            fileExcelDb = tempPath + listFile;
        }
    }

    private String fileZipDb = "";
    private String nameFileZipDb = "";

    public String getNameFileZipDb() {
        return nameFileZipDb;
    }

    public String getFileZipDb() {
        return fileZipDb;
    }

    public void handUploadFileZipDb(FileUploadEvent event) {

        UploadedFile file = event.getFile();
        nameFileZipDb = file.getFileName();

        ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        locate = languageBean.getLocaleCode();
        String tempPath = context.getRealPath("/") + "TEMPORARY/DB/" + actionController.getUsername() + "/ZIP/";
        FileHelper.removeFile(tempPath);
        if (file != null) {
            String listFile = Util.convertUTF8ToNoSign(getNameFile(file.getFileName())) + getFileExtension(file.getFileName());
            FileHelper.uploadFile(tempPath, file, listFile);
            fileZipDb = tempPath + listFile;
        }
    }

    public void onImportDbClick() {
        importSuccess = true;
        fileZipDb = "";
        fileExcelDb = "";
        nameFileZipDb = "";
        nameFileExcelDb = "";
    }


    public boolean uploadFileBakup(ActionDetailDatabase detailDbObj, File fileBackup, MopDatabaseDTO mopDb) {
        String uploadFolder = UploadFileUtils.getDatabaseFolder(action);
        if (fileBackup != null) {
            BigDecimal seq = null;
            try {
                seq = actionController.getActionService().nextFileSeq();
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
            String sourceCode = Util.convertUTF8ToNoSign(FilenameUtils.getBaseName(fileBackup.getName()) + "_" + seq + "." + FilenameUtils.getExtension(fileBackup.getName()));
            FileHelper.uploadFileCode(uploadFolder, fileBackup, sourceCode);

            String endCoding = UploadFileUtils.checkEncoding(uploadFolder + File.separator + sourceCode);
            File sqlFile = new File(uploadFolder + File.separator + sourceCode);
            if (endCoding == null || "UTF-8".equals(endCoding)) {
                try {
                    List<String> lines = FileUtils.readLines(sqlFile);
                    if (!SqlUtils.checkStatmentSeparator(lines)) {
                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("commands.must.be.character.finish"), "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", message);
                    } else if (!checkAt(FileUtils.readFileToString(sqlFile))) {
                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("begin.line.do.not.start.with.character.at"), "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", message);
                    } else {
                        detailDbObj.setScriptBackup(sourceCode);
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    mopDb.setMessage(MessageUtil.getResourceBundleMessage("script.must.use.code.utf8"));
                    mopDb.setResult("NOK");
                    return false;
                }
            } else {
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("script.must.use.code.utf8"));
                mopDb.setResult("NOK");
                return false;
            }
        }
        // 23-11-2018 KienPD check disabled time out start
        disabledTimeOutBackUp = detailDbObj.getScriptBackup() == null || "".equals(detailDbObj.getScriptBackup());
        // 23-11-2018 KienPD check disabled time out end
        return true;
    }


    public boolean uploadFileRollback(ActionDetailDatabase detailDbObj, File fileRollBack, MopDatabaseDTO mopDb) {
        String uploadFolder = UploadFileUtils.getDatabaseFolder(action);
        if (fileRollBack != null) {
            BigDecimal seq = null;
            try {
                seq = actionController.getActionService().nextFileSeq();
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
            String sourceCode = Util.convertUTF8ToNoSign(FilenameUtils.getBaseName(fileRollBack.getName()) + "_" + seq + "." + FilenameUtils.getExtension(fileRollBack.getName()));

            FileHelper.uploadFileCode(uploadFolder, fileRollBack, sourceCode);

            String endCoding = UploadFileUtils.checkEncoding(uploadFolder + File.separator + sourceCode);
            File sqlFile = new File(uploadFolder + File.separator + sourceCode);
            if (endCoding == null || "UTF-8".equals(endCoding)) {
                try {
                    List<String> lines = FileUtils.readLines(new File(uploadFolder + File.separator + sourceCode));
                    if (!SqlUtils.checkStatmentSeparator(lines)) {
                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("commands.must.be.character.finish"), "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", message);
                    } else if (!checkAt(FileUtils.readFileToString(sqlFile))) {
                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("begin.line.do.not.start.with.character.at"), "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", message);
                    } else {
                        detailDbObj.setRollbackFile(sourceCode);
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    mopDb.setMessage(MessageUtil.getResourceBundleMessage("script.must.use.code.utf8"));
                    mopDb.setResult("NOK");
                    return false;
                }
            } else {
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("script.must.use.code.utf8"));
                mopDb.setResult("NOK");
                return false;
            }
        }
        // 23-11-2018 KienPD check disabled time out start
        disabledTimeOutRollback = detailDbObj.getRollbackFile() == null || "".equals(detailDbObj.getRollbackFile());
        // 23-11-2018 KienPD check disabled time out end
        return true;
    }

    public boolean uploadFileExcute(ActionDetailDatabase detailDbObj, File fileExcute, MopDatabaseDTO mopDb) {
        String uploadFolder = UploadFileUtils.getDatabaseFolder(action);
        if (fileExcute != null) {
            BigDecimal seq = null;
            try {
                seq = actionController.getActionService().nextFileSeq();
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }
            String sourceCode = Util.convertUTF8ToNoSign(FilenameUtils.getBaseName(fileExcute.getName()) + "_" + seq + "." + FilenameUtils.getExtension(fileExcute.getName()));

            FileHelper.uploadFileCode(uploadFolder, fileExcute, sourceCode);
            String endCoding = UploadFileUtils.checkEncoding(uploadFolder + File.separator + sourceCode);
            File sqlFile = new File(uploadFolder + File.separator + sourceCode);
            if (endCoding == null || "UTF-8".equals(endCoding)) {
                try {
                    List<String> lines = FileUtils.readLines(new File(uploadFolder + File.separator + sourceCode));
                    if (!SqlUtils.checkStatmentSeparator(lines)) {
                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("commands.must.be.character.finish"), "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", message);
                    } else if (!checkAt(FileUtils.readFileToString(sqlFile))) {
                        FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("begin.line.do.not.start.with.character.at"), "");
                        FacesContext.getCurrentInstance().addMessage("designGrowl", message);
                    } else {
                        detailDbObj.setScriptExecute(sourceCode);
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                    mopDb.setMessage(MessageUtil.getResourceBundleMessage("script.must.use.code.utf8"));
                    mopDb.setResult("NOK");
                    return false;
                }
            } else {
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("script.must.use.code.utf8"));
                mopDb.setResult("NOK");
                return false;
            }
        }
        // 23-11-2018 KienPD check disabled time out start
        disabledTimeOutImpact = detailDbObj.getScriptExecute() == null || "".equals(detailDbObj.getScriptExecute());
        // 23-11-2018 KienPD check disabled time out end
        return true;
    }

    private boolean checkFormatInputTimeout(String inputData) {
        try{
            Integer.parseInt(inputData);
        }catch (Exception ex){
            return false;
        }
        return true;
    }

    public boolean addMopDataBase(MopDatabaseDTO mopDb, List<FileHeader> headers, String zipFolder) {
        if (detailDatabases == null)
            detailDatabases = new ArrayList<>();

        if (mopDb.getTimeOutImpact() != null && !checkFormatInputTimeout(mopDb.getTimeOutImpact())){
            mopDb.setMessage(MessageUtil.getResourceBundleMessage("error.time.out.impact"));
            mopDb.setResult("NOK");
            return false;
        }

        if (mopDb.getTimeOutRollback() != null && !checkFormatInputTimeout(mopDb.getTimeOutRollback())){
            mopDb.setMessage(MessageUtil.getResourceBundleMessage("error.time.out.rollback"));
            mopDb.setResult("NOK");
            return false;
        }

        if (mopDb.getTimeOutBackup() != null && !checkFormatInputTimeout(mopDb.getTimeOutBackup())){
            mopDb.setMessage(MessageUtil.getResourceBundleMessage("error.time.out.backup"));
            mopDb.setResult("NOK");
            return false;
        }

        ActionDetailDatabase impActionDetailDatabase = new ActionDetailDatabase();
        impActionDetailDatabase.setActionOrder(mopDb.getActionOrder());
        impActionDetailDatabase.setAppDbId(mopDb.getAppDbId());
        impActionDetailDatabase.setKbGroup(mopDb.getKbGroup());
        impActionDetailDatabase.setType(mopDb.getType());
        impActionDetailDatabase.setTypeImport(mopDb.getTypeImport());
        impActionDetailDatabase.setTestbedMode(mopDb.getTestbedMode());
        impActionDetailDatabase.setScriptBackup(mopDb.getScriptBackup());
        impActionDetailDatabase.setTimeOutBackup(mopDb.getTimeOutBackup() == null ? null : Integer.parseInt(mopDb.getTimeOutBackup()));
        impActionDetailDatabase.setScriptExecute(mopDb.getScriptExecute());
        impActionDetailDatabase.setTimeOutImpact(mopDb.getTimeOutImpact() == null ? null : Integer.parseInt(mopDb.getTimeOutImpact()));
        impActionDetailDatabase.setRollbackFile(mopDb.getRollbackFile());
        impActionDetailDatabase.setTimeOutRollback(mopDb.getTimeOutRollback()  == null ? null : Integer.parseInt(mopDb.getTimeOutRollback()));

        if (!impActionDetailDatabase.getScriptBackup().isEmpty()) {
            if (!checkFormatSql(impActionDetailDatabase.getScriptBackup())) {
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("error.script.backup.not.sqlfile"));
                mopDb.setResult("NOK");
                return false;
            }

            if (impActionDetailDatabase.getTimeOutBackup() == null) {
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("error.timeout.backup.not.set"));
                mopDb.setResult("NOK");
                return false;
            }
            if (impActionDetailDatabase.getTimeOutBackup() <= 0) {
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("error.timeout.backup.not.valid"));
                mopDb.setResult("NOK");
                return false;
            }
        }

        if (!impActionDetailDatabase.getScriptExecute().isEmpty()) {
            if (!checkFormatSql(impActionDetailDatabase.getScriptExecute())) {
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("error.script.impact.not.sqlfile"));
                mopDb.setResult("NOK");
                return false;
            }
            if (impActionDetailDatabase.getTimeOutImpact() == null) { // trường hợp bắt buộc phải có file script impact
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("error.time.out.impact.not.set"));
                mopDb.setResult("NOK");
                return false;
            }
            if (impActionDetailDatabase.getTimeOutImpact() <= 0) { // trường hợp bắt buộc phải có file script impact
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("error.time.out.impact.not.valid"));
                mopDb.setResult("NOK");
                return false;
            }
        } else {
            mopDb.setMessage(MessageUtil.getResourceBundleMessage("error.script.impact.is.null"));
            mopDb.setResult("NOK");
            return false;
        }


        if (!impActionDetailDatabase.getRollbackFile().isEmpty()) {
            if (!checkFormatSql(impActionDetailDatabase.getRollbackFile())) {
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("error.script.rollback.not.sqlfile"));
                mopDb.setResult("NOK");
                return false;
            }
            if (impActionDetailDatabase.getTimeOutRollback() == null) {
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("error.time.out.rollback.not.set"));
                mopDb.setResult("NOK");
                return false;
            }
            if (impActionDetailDatabase.getTimeOutRollback() <= 0) {
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("error.time.out.rollback.not.valid"));
                mopDb.setResult("NOK");
                return false;
            }
        }


        // Kiem tra dbappid va map co ton tai dong thoi
        boolean isExistDb = false;
        for(SelectItem item: listDatabases){
            if(item.getValue().equals(mopDb.getAppDbId()) && item.getLabel().equals(mopDb.getDbMap())){
                isExistDb = true;
                break;
            }
        }
        if(!isExistDb){
            mopDb.setMessage(MessageUtil.getResourceBundleMessage("dbmap.dbappid.not.existed"));
            mopDb.setResult("NOK");
            return false;
        }

        for (MopDatabaseDTO item: listMopDb) {
            if(item != mopDb){
                if(item.getRollbackFile() != null && !"".equals(item.getRollbackFile())
                        && item.getRollbackFile().equals(mopDb.getRollbackFile())){
                    mopDb.setMessage(MessageUtil.getResourceBundleMessage("script.rollback.duplicate"));
                    mopDb.setResult("NOK");
                    return false;
                }

                if(item.getScriptBackup() != null && !"".equals(item.getScriptBackup())
                        && item.getScriptBackup().equals(mopDb.getScriptBackup())){
                    mopDb.setMessage(MessageUtil.getResourceBundleMessage("script.backup.duplicate"));
                    mopDb.setResult("NOK");
                    return false;
                }

                if(item.getScriptExecute() != null && !"".equals(item.getScriptExecute())
                        && item.getScriptExecute().equals(mopDb.getScriptExecute())){
                    mopDb.setMessage(MessageUtil.getResourceBundleMessage("script.execute.duplicate"));
                    mopDb.setResult("NOK");
                    return false;
                }
            }else{
                break;
            }
        }

        //Check scripts existed
        boolean fileBackupExisted = false;
        boolean fileExcuteExisted = false;
        boolean fileRollbackExisted = false;
        for (FileHeader header: headers
        ) {
            if(mopDb.getScriptBackup() != null && !"".equals(mopDb.getScriptBackup()) && header.getFileName().contains(mopDb.getScriptBackup())
                    && fileBackupExisted == false) {
                File upFile = new File(zipFolder + "/" + header.getFileName());
                fileBackupExisted = uploadFileBakup(impActionDetailDatabase, upFile, mopDb);
            }
            if(mopDb.getScriptExecute() != null && !"".equals(mopDb.getScriptExecute()) && header.getFileName().contains(mopDb.getScriptExecute())
                    && fileExcuteExisted == false) {
                File upFile = new File(zipFolder + "/" + header.getFileName());
                fileExcuteExisted = uploadFileExcute(impActionDetailDatabase, upFile, mopDb);
            }

            if(mopDb.getRollbackFile() != null && !"".equals(mopDb.getRollbackFile()) && header.getFileName().contains(mopDb.getRollbackFile())
                    && fileRollbackExisted == false) {
                File upFile = new File(zipFolder + "/" + header.getFileName());
                fileRollbackExisted = uploadFileRollback(impActionDetailDatabase, upFile, mopDb);
            }
        }
        if(!fileBackupExisted && mopDb.getScriptBackup() != null && !"".equals(mopDb.getScriptBackup())
                && !"NOK".equals(mopDb.getResult())){
            mopDb.setMessage(MessageUtil.getResourceBundleMessage("backup.file.not.existed.in.zip"));
            mopDb.setResult("NOK");
            return false;
        }

        if(!fileExcuteExisted && mopDb.getScriptExecute() != null && !"".equals(mopDb.getScriptExecute())
                && !"NOK".equals(mopDb.getResult())){
            mopDb.setMessage(MessageUtil.getResourceBundleMessage("excute.file.not.existed.in.zip"));
            mopDb.setResult("NOK");
            return false;
        }

        if(!fileRollbackExisted && mopDb.getRollbackFile() != null && !"".equals(mopDb.getRollbackFile())
                && !"NOK".equals(mopDb.getResult())){
            mopDb.setMessage(MessageUtil.getResourceBundleMessage("rollback.file.not.existed.in.zip"));
            mopDb.setResult("NOK");
            return false;
        }

        try {
            ServiceDatabase serviceDb = iimService.findServiceDbById(action.getImpactProcess().getNationCode(), impActionDetailDatabase.getAppDbId());
            if(serviceDb == null)
            {
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("dbid.not.existed"));
                mopDb.setResult("NOK");
                return false;
            }
            else if (!mopDb.getDbMap().equals(serviceDb.getUsername() + "@" + serviceDb.getDbName() + "@host:" + serviceDb.getHost())){
                mopDb.setMessage(MessageUtil.getResourceBundleMessage("dbmap.not.existed"));
                mopDb.setResult("NOK");
                return false;
            }
            impActionDetailDatabase.setServiceDatabase(serviceDb);

        }catch (Exception ex){
            logger.error(ex.getMessage());
            mopDb.setMessage(MessageUtil.getResourceBundleMessage("dbid.not.existed"));
            mopDb.setResult("NOK");
            return false;
        }

        try {

            ServiceDatabase serviceDb = impActionDetailDatabase.getServiceDatabase();
            Long actionOrder = impActionDetailDatabase.getActionOrder();
            List<String> listTableName = new ArrayList<>();

            String scriptText = impActionDetailDatabase.getScriptText();
            if (StringUtils.isEmpty(scriptText)) {
                File executeFile = new File(UploadFileUtils.getDatabaseFolder(action)
                        + File.separator + impActionDetailDatabase.getScriptExecute());
                scriptText = FileUtils.readFileToString(executeFile);
            }

            Pattern pattern;
            Matcher matcher;
            pattern = Pattern.compile(Constant.PATTERN.DDL_CREATE_EXIST, Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(scriptText);
            if (!matcher.find()) {
                String[] arrScript = scriptText.split("/|;");
                for (String script : arrScript) {
                    pattern = Pattern.compile(Constant.PATTERN.DDL_CREATE_TABLE, Pattern.CASE_INSENSITIVE);
                    matcher = pattern.matcher(script);
                    if (matcher.find()) {
                        String schemasObject = matcher.group(3).replaceAll("\"", "");
                        if (schemasObject.contains(".")) {
                            listTableName.add(schemasObject.toUpperCase());
                        } else {
                            listTableName.add(serviceDb.getUsername().toUpperCase() + "." + schemasObject.toUpperCase());
                        }
                    }
                }
            }

            File backupFile = new File(UploadFileUtils.getDatabaseFolder(action) + File.separator + impActionDetailDatabase.getScriptBackup());
            File executeFile = new File(UploadFileUtils.getDatabaseFolder(action) + File.separator + impActionDetailDatabase.getScriptExecute());
            File rollbackFile = new File(UploadFileUtils.getDatabaseFolder(action) + File.separator + impActionDetailDatabase.getRollbackFile());

            String message = null;
            try {
                message = SqlUtils.checkTable(backupFile, executeFile, rollbackFile, serviceDb, listTableName);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                message = "";
            } finally {
            }
            if (StringUtils.isNotEmpty(message)) {
                mopDb.setMessage(message);
                mopDb.setResult("NOK");
                return false;
            }
        }
        catch(Exception ex){
            logger.error(ex.getMessage());
        }

        detailDatabases.add(impActionDetailDatabase);
        mopDb.setMessage("Success");
        mopDb.setResult("OK");
        return true;
    }
}