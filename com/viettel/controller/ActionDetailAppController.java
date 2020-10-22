package com.viettel.controller;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.mchange.v1.util.SimpleMapEntry;
import com.viettel.bean.*;

import com.viettel.model.*;
import com.viettel.persistence.IimService;
import com.viettel.util.*;

import com.viettel.util.Util;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.commons.beanutils.BeanUtilsBean;
import org.apache.commons.beanutils.DynaProperty;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.*;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.event.NodeSelectEvent;
import org.primefaces.model.*;

import javax.annotation.PostConstruct;
import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.GenericType;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import net.lingala.zip4j.core.ZipFile;

/**
 * @author quanns2
 */
@ViewScoped
@ManagedBean
public class ActionDetailAppController implements Serializable {
    private static Logger logger = LogManager.getLogger(ActionDetailAppController.class);

    private static final long serialVersionUID = 1L;
    @ManagedProperty(value = "#{actionDetailAppService}")
    ActionDetailAppService actionDetailAppService;

    public void setActionDetailAppService(ActionDetailAppService actionDetailAppService) {
        this.actionDetailAppService = actionDetailAppService;
    }

    public void setIimService(IimService iimService) {
        this.iimService = iimService;
    }

    @ManagedProperty(value = "#{iimService}")
    IimService iimService;

    public com.viettel.it.util.LanguageBean getLanguageBean() {
        return languageBean;
    }

    public void setLanguageBean(com.viettel.it.util.LanguageBean languageBean) {
        this.languageBean = languageBean;
    }

    @ManagedProperty(value = "#{language}")
    com.viettel.it.util.LanguageBean languageBean;



    private LazyDataModel<ActionDetailApp> lazyDataModel;
    private ActionDetailApp selectedObj;
    private ActionDetailApp newObj;

    private boolean isEdit;

    private Long searchId;
    private Long searchActionId;
    private Long searchModuleId;
    private String searchAction;
    private Long searchModuleOrder;
    private String searchActionRollback;
    private String searchGroupAction;
    private String searchBackupPath;
    private String searchUpcodePath;
    private String searchUploadFilePath;
    private String searchListFileCode;
    private String searchCheckCmd;
    private String searchCheckCmdResult;
    //	private List<ActionDetailApp> listDetailsApp;
//    private Map<Long, Module> mapApp;
    private Action action;
    private String sourceCode;
    private String listFile;
    private List<Long> listModuleSelected;
    private String checkCmdResult;

    private List<ActionDetailApp> lstBackup;
    private List<ActionDetailApp> lstStop;
    private List<ActionDetailApp> lstUpcode;
    private List<ActionDetailApp> lstRestart;
    private List<ActionDetailApp> lstClearCache;
    private List<ActionDetailApp> lstStart;
    private List<ActionDetailApp> lstUpcodeStart;
    private List<ActionDetailApp> lstRestartCmd;
    /*20181030_hoangnd_save all step_start*/
    private List<ActionDetailApp> lstCheckStatus;
    private List<ActionDetailApp> lstChecklistApp;
    private List<ActionDetailApp> lstChecklistDb;
    /*20181030_hoangnd_save all step_end*/

    private List<ActionDetailApp> filteredLstStop;
    private List<ActionDetailApp> filteredLstStart;
    private List<ActionDetailApp> filteredLstRestart;
    private List<ActionDetailApp> filteredLstRestartCmd;
    private List<ActionDetailApp> filteredBackup;
    private List<ActionDetailApp> filteredUpcode;
    private List<ActionDetailApp> filteredUpcodeStart;
    private List<ActionDetailApp> filteredClearcache;
    private Multimap<Map.Entry<Long, String>, ActionDetailApp> appMultimap;

    private ActionController actionController;

    private TreeNode upcodeRoot;
    private TreeNode selectedUpcodeDir;

    private TreeNode removeUpcodeRoot;
    private TreeNode selectedRemoveUpcodeFile;

    private DualListModel<Module> dualListModel;
    private List<Module> sources;
    private List<Module> targets;

    // 20190417_thenv_start change file config
    private boolean isShowChangeConfigButton = false;
    private String cfgShowChangeConfigButton = null;
    private List<RuleConfig> ruleConfigList;
    private List<RuleConfig> lstRuleConfigNewObj;
    private List<String> lstRuleEdit;
    private String fileContentOld;
    private String fileContentNew;
    private String backupFilePathNewObj;
    // 20190417_thenv_end change file config

    //20190416_tudn_start import rule config
    private List<Module> lstModuleUpcode;
    private List<Module> lstModuleStopStartUpcode;
    private StreamedContent resultImport;
    private String typeStep;
    //20190416_tudn_end import rule config

    private boolean isShowNodeType;
    private String locate;
    private boolean hasError;

    @PostConstruct
    public void onStart() {
        init();
        dualListModel = new DualListModel<>();
        appMultimap = HashMultimap.create();
        // 20190417_thenv_start change file config

        // 20190417_thenv_end change file config
    }

    // 20190417_thenv_start change file config
    private void getConfigShowChangeConfig() {
        try {
            // Lay cau hinh danh sach loai file duoc phep change config
            Map<String, Object> filters = new LinkedHashMap<>();
            filters.put("id.configGroup-EXAC", Constant.ChangeFileConfigType.CFG_GROUP);
            filters.put("id.propertyKey-EXAC", Constant.ChangeFileConfigType.CFG_KEY);
            filters.put("isActive-EXAC", 1L);
            List<com.viettel.it.model.CatConfig> config = new com.viettel.it.persistence.CatConfigServiceImpl().findList(filters);
            if (config == null || config.size() == 0
                    || config.get(0).getPropertyValue() == null
                    || "".equalsIgnoreCase(config.get(0).getPropertyValue().trim())) {
                cfgShowChangeConfigButton = null;
                return;
            }
            cfgShowChangeConfigButton = config.get(0).getPropertyValue();
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    private void getRuleEdits() {
        lstRuleEdit = new ArrayList<>();
        lstRuleEdit.add(Constant.RuleConfig.RULE_ADD_ON_BOTTOM);
        lstRuleEdit.add(Constant.RuleConfig.RULE_ADD_ON_HEAD);
        lstRuleEdit.add(Constant.RuleConfig.RULE_ADD_BEFORE_KEYWORD);
        lstRuleEdit.add(Constant.RuleConfig.RULE_ADD_AFTER_KEYWORD);
        lstRuleEdit.add(Constant.RuleConfig.RULE_REPLACE_KEYWORD);
        lstRuleEdit.add(Constant.RuleConfig.RULE_ADD_BEFORE_LINE_KEYWORD);
        lstRuleEdit.add(Constant.RuleConfig.RULE_ADD_AFTER_LINE_KEYWORD);
        lstRuleEdit.add(Constant.RuleConfig.RULE_DELETE_KEYWORD);
    }

    private List<RuleConfig> getListRuleConfig(Long actionId) {
        List<RuleConfig> lst = new ArrayList<>();
        try {
            lst = new RuleConfigServiceImpl().findByActionId(actionId);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lst;
    }

    private void checkShowChangeConfig(String path) {
        // Check upcodePath co khop voi danh sach hay khong
        if (path == null || "".equalsIgnoreCase(path.trim()) || !path.contains(".")) {
            isShowChangeConfigButton = false;
            return;
        }
        if (cfgShowChangeConfigButton == null) {
            isShowChangeConfigButton = false;
        } else {
            String[] types = cfgShowChangeConfigButton.replaceAll("\\*", "").split(",", -1);
            String[] upcodePath = path.split("\\.");
            String upcodeFileType = "." + upcodePath[upcodePath.length - 1].trim().toLowerCase();
            isShowChangeConfigButton = false;
            for (String t : types) {
                if (t.trim().toLowerCase().equalsIgnoreCase(upcodeFileType)) {
                    isShowChangeConfigButton = true;
                    break;
                }
            }
        }
    }

    public void preChangeConfig() {
        try {
            Module module = actionController.getImpactModules().get(newObj.getModuleId());
            if(module.getUsername() != null && module.getUsername().equalsIgnoreCase("root")){
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, MessageUtil.getResourceBundleMessage("app.warn.not.support"), "");
                if (msg != null) {
                    FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                    RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
                }
                return;
            }
            // load list rule
            lstRuleConfigNewObj.clear();
            String moduleCode = actionController.getImpactModules().get(newObj.getModuleId()).getModuleCode();
            for (RuleConfig rule : ruleConfigList) {
                if (moduleCode.equalsIgnoreCase(rule.getModuleCode()) && newObj.getUpcodePath().equalsIgnoreCase(rule.getPathFile())) {
                    RuleConfig newRule = (RuleConfig) BeanUtils.cloneBean(rule);
                    lstRuleConfigNewObj.add(newRule);
                }
            }
            if (lstRuleConfigNewObj == null || lstRuleConfigNewObj.size() == 0) {
                RuleConfig rule = new RuleConfig();
                rule.setModuleCode(moduleCode);
                rule.setPathFile(newObj.getUpcodePath());
                lstRuleConfigNewObj.add(rule);
            }

            // load fileContentOld va fileContentNew
            backupFilePathNewObj = newObj.getBackupFilePath();
            boolean rs = getFileContent();
            if (rs) {
                RequestContext.getCurrentInstance().update("mop:lst:changeConfig");
                RequestContext.getCurrentInstance().execute("PF('changeConfig').show()");
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private boolean getFileContent() throws AppException {
        fileContentOld = "";
        boolean getContentFromServerUpcode = true;
        // Lay lai thong tin ActionDetailApp tu DB
        if (newObj.getId() != null) {
            Map<String, Object> filters = new HashMap<>();
            filters.put("id", newObj.getId());
            List<ActionDetailApp> lstActionDetailApps = actionDetailAppService.findList(filters, new HashMap<String, String>());
            if (lstActionDetailApps != null && lstActionDetailApps.size() != 0) {
                ActionDetailApp detailApp = lstActionDetailApps.get(0);
                // UpcodePath cu = UpcodePath moi
                if (detailApp.getUpcodePath() != null && !"".equalsIgnoreCase(detailApp.getUpcodePath()) && detailApp.getUpcodePath().equalsIgnoreCase(newObj.getUpcodePath())) {
                    newObj.setUploadFilePath(detailApp.getUploadFilePath());
                    // BackupFilePath != null -> da cau hinh ruleConfig
                    if (detailApp.getBackupFilePath() != null && !"".equalsIgnoreCase(detailApp.getBackupFilePath())) {
                        // Lay content tu UpcodePath cu
                        getContentFromServerUpcode = false;
                    }
                }
            }
        }

        if (getContentFromServerUpcode) {
            Module module = actionController.getImpactModules().get(newObj.getModuleId());
            String[] fileUpcode = newObj.getUpcodePath().split("/", -1);
            String fileName = fileUpcode[fileUpcode.length - 1];
            String filePath = module.getExecutePath().trim() + "/" + newObj.getUpcodePath().substring(0, newObj.getUpcodePath().indexOf(fileName) - 1);
            GetFileFromServer rs = this.actionController.getFileToServer(module, filePath, fileName, action);
            if (!rs.isOk()) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("err.file.not.found"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
                return false;
            }
            fileContentOld = rs.getContentFile();
            backupFilePathNewObj = rs.getBackupFileName();
        } else {
            String uploadFolder = UploadFileUtils.getSourceCodeFolder(action);
            fileContentOld = ChangeConfigFileUtil.unzipFileToString(uploadFolder, newObj.getUploadFilePath());
            fileContentNew = new String(fileContentOld);
            if (fileContentOld == null || "".equalsIgnoreCase(fileContentOld)) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("err.upcodepath.file.not.found"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
            }
        }
        return true;
    }

    public void addRuleConfigPanel() {
        RuleConfig rule = new RuleConfig();
        rule.setModuleCode(actionController.getImpactModules().get(newObj.getModuleId()).getModuleCode());
        rule.setPathFile(newObj.getUpcodePath());
        lstRuleConfigNewObj.add(rule);
    }

    public void delRuleConfigPanel(RuleConfig rule) {
        lstRuleConfigNewObj.remove(rule);
    }

    public void refreshConfig() {
        try {
            if (backupFilePathNewObj == null || "".equalsIgnoreCase(backupFilePathNewObj.trim())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("err.change.config.file.content.is.oldest"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
                return;
            }

            String uploadFolder = UploadFileUtils.getSourceCodeFolder(action) + File.separator + "file_backup";
            fileContentOld = ChangeConfigFileUtil.unzipFileToString(uploadFolder, backupFilePathNewObj);
            // Ko lay duoc file backup se tu dong lay moi file tu server ve
            if (fileContentOld == null || "".equalsIgnoreCase(fileContentOld)) {

                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("err.upcodepath.fileBackup.not.found"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");

                Module module = actionController.getImpactModules().get(newObj.getModuleId());
                String[] fileUpcode = newObj.getUpcodePath().split("/", -1);
                String fileName = fileUpcode[fileUpcode.length - 1];
                String filePath = module.getExecutePath().trim() + "/" + newObj.getUpcodePath().substring(0, newObj.getUpcodePath().indexOf(fileName) - 1);
                GetFileFromServer rs = this.actionController.getFileToServer(module, filePath, fileName, action);
                if (!rs.isOk()) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("err.file.not.found"), "");
                    FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                    RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
                    return;
                }
                fileContentOld = rs.getContentFile();
                backupFilePathNewObj = rs.getBackupFileName();
            }
            RequestContext.getCurrentInstance().update("mop:lst:panelContent");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void executeConfig() {
        try {
            // check dau vao
            FacesMessage msg = null;
            if (lstRuleConfigNewObj == null || lstRuleConfigNewObj.size() == 0) {
                msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("err.change.config.not.found"), "");
            }
            Map<String, String> checkDuplicateRule = new HashMap<>();
            String fileContentNewTemp = new String(fileContentOld);
            for (RuleConfig r : lstRuleConfigNewObj) {
                if (r.getRuleEdit() == null || "".equalsIgnoreCase(r.getRuleEdit().trim())) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("err.change.config.rule.empty"), "");
                    break;
                }
                if (checkDuplicateRule.containsKey(r.getRuleEdit() + "#" + r.getKeyword())) {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("err.change.config.rule.duplicate"), "");
                    break;
                } else {
                    checkDuplicateRule.put(r.getRuleEdit() + "#" + r.getKeyword(), "");
                }

                if (!r.getRuleEdit().equalsIgnoreCase(Constant.RuleConfig.RULE_ADD_ON_HEAD)
                        && !r.getRuleEdit().equalsIgnoreCase(Constant.RuleConfig.RULE_ADD_ON_BOTTOM)) {
                    if (r.getKeyword() == null || "".equalsIgnoreCase(r.getKeyword())) {
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                MessageFormat.format(MessageUtil.getResourceBundleMessage("err.change.config.keyword.empty"),
                                        MessageUtil.getResourceBundleMessage("title.rule." + r.getRuleEdit())), "");
                        break;
                    }
                    // check keyword not found
                    if (!fileContentNewTemp.contains(r.getKeyword())) {
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageFormat.format(MessageUtil.getResourceBundleMessage("validate.not.file.not.key.because.del.replace"), r.getKeyword()), "");
                        break;
                    }
                }

                if (!r.getRuleEdit().equalsIgnoreCase(Constant.RuleConfig.RULE_DELETE_KEYWORD)) {
                    if (r.getContent() == null || "".equalsIgnoreCase(r.getContent())) {
                        msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
                                MessageFormat.format(MessageUtil.getResourceBundleMessage("err.change.config.content.empty"),
                                        MessageUtil.getResourceBundleMessage("title.rule." + r.getRuleEdit())), "");
                        break;
                    }
                }

                // thuc hien change file config cho tung rule
                fileContentNewTemp = ChangeConfigFileUtil.changeConfigByRule(fileContentNewTemp, r);
            }
            if (msg != null) {
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
                return;
            }

            // thuc hien change file config
            fileContentNew = new String(fileContentNewTemp);
            RequestContext.getCurrentInstance().update("mop:lst:panelContent");
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public void submitChangeConfig() {
        try {
            if (fileContentNew == null || "".equalsIgnoreCase(fileContentNew.trim())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("err.change.config.fileContentNew.empty"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
                return;
            }

            Module detail = actionController.getImpactModules().get(newObj.getModuleId());

            if (detail == null)
                return;

            // create new file content
            String[] fileUpcode = newObj.getUpcodePath().split("/", -1);
            String filename = fileUpcode[fileUpcode.length - 1];
            sourceCode = Util.convertUTF8ToNoSign(detail.getModuleCode() + "_" + newObj.getUpcodePath().replaceAll("\\.\\.", "").replaceAll("/", "_").replaceAll("\\\\", "_") + "_" + filename.substring(0, filename.lastIndexOf(".")) + ".zip");
            String uploadFolder = UploadFileUtils.getSourceCodeFolder(action);
            FileWriter writer = new FileWriter(uploadFolder + "/" + filename);
            writer.write(fileContentNew);
            writer.close();
            File file = new File(uploadFolder + "/" + filename);
            com.viettel.it.util.ZipUtils.zipDirectory(file, uploadFolder + "/" + sourceCode);
            InputStream inputStream = new FileInputStream(file);
            String newMd5 = DigestUtils.md5Hex(inputStream);
            inputStream.close();
            FileUtils.forceDelete(file);
            logger.info("FileUtils.forceDelete(file);");
            newObj.setUploadFilePath(sourceCode);
            newObj.setMd5(newMd5);
            newObj.setFile(true);
            newObj.setBackupFilePath(backupFilePathNewObj);

            // update list rule config
            String moduleCode = actionController.getImpactModules().get(newObj.getModuleId()).getModuleCode();
            for (Iterator<RuleConfig> iter = ruleConfigList.listIterator(); iter.hasNext(); ) {
                RuleConfig rule = iter.next();
                if (moduleCode.equalsIgnoreCase(rule.getModuleCode()) && newObj.getUpcodePath().equalsIgnoreCase(rule.getPathFile())) {
                    iter.remove();
                }
            }
            ruleConfigList = new ArrayList<>(ruleConfigList);
            for (RuleConfig rule : lstRuleConfigNewObj) {
                RuleConfig newRule = (RuleConfig) BeanUtils.cloneBean(rule);
                ruleConfigList.add(newRule);
            }

            RequestContext.getCurrentInstance().execute("PF('changeConfig').hide()");
            RequestContext.getCurrentInstance().update("mop:lst:sourceCodePanel");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("have.some.error"), "");
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
        }
    }
    // 20190417_thenv_end change file config

    public void viewSelectItems(ActionController actionController, boolean isDuplicate, boolean includeFile) {
        this.actionController = actionController;

		/*if (action != null && action.getId() != null) {
            Map<String, Object> filters = new HashMap<>();
			filters.put("actionId", action.getId() + "");
			try {
				listDetailsApp = actionDetailAppService.findList(filters, new HashMap<String, String>());
			} catch (AppException e) {
				logger.error(e.getMessage(), e);
			}
		} else {
			listDetailsApp = new ArrayList<>();
		}*/

        List<ActionDetailApp> detailApps;
        if (action.getId() != null)
            detailApps = actionDetailAppService.findListDetailApp(action.getId(), null, null, true);
        else
            detailApps = new ArrayList<>();

        appMultimap = HashMultimap.create();
        for (ActionDetailApp detailApp : detailApps) {
            if (!includeFile && isDuplicate && detailApp.getGroupAction().equals(Constant.STEP_UPCODE)) {
                detailApp.setUpcodePath(null);
                detailApp.setUploadFilePath(null);
                detailApp.setLstFileRemove(null);
                detailApp.setMd5(null);
                // 20190417_thenv_start change file config
                detailApp.setBackupFilePath(null);
                // 20190417_thenv_end change file config
            }
            /*20181203_hoangnd_fix bug duplicate all status_start*/
            if (isDuplicate) {
                detailApp.setRollbackStatus(null);
                detailApp.setBeforeStatus(null);
                detailApp.setAfterStatus(null);
                //15-12-2018 KienPD start
                detailApp.setIsAddRollback(null);
                //15-12-2018 KienPD end
                detailApp.setRunStartTime(null);
                detailApp.setRunEndTime(null);
                detailApp.setRollbackStartTime(null);
                detailApp.setRollbackEndTime(null);
                detailApp.setBackupStartTime(null);
                detailApp.setBackupEndTime(null);
            }
            /*20181203_hoangnd_fix bug duplicate all status_end*/
            detailApp.setBackupStatus(0);
            detailApp.setRunStatus(0);

            appMultimap.put(new SimpleMapEntry(detailApp.getModuleId(), detailApp.getGroupAction()), detailApp);
        }
    }

    public void showStop() {
        System.out.println(lstStop.size());
    }

    public void buildLstAction() {
        lstBackup = new ArrayList<>();
        lstStop = new ArrayList<>();
        lstUpcode = new ArrayList<>();
        lstRestart = new ArrayList<>();
        lstClearCache = new ArrayList<>();
        lstStart = new ArrayList<>();
        lstUpcodeStart = new ArrayList<>();
        lstRestartCmd = new ArrayList<>();
        /*20181030_hoangnd_save all step_start*/
        lstCheckStatus = new ArrayList<>();
        /*20181030_hoangnd_save all step_end*/

        //20190416_tudn_start import rule config
        lstModuleUpcode = new ArrayList<>();
        lstModuleStopStartUpcode = new ArrayList<>();
        ruleConfigList = new ArrayList<>();
        //20190416_tudn_end import rule config

		/*List<ActionDetailApp> detailApps;
        if (action.getId() != null)
			detailApps = actionDetailAppService.findListDetailApp(action.getId(), null);
		else
			detailApps = new ArrayList<>();

		Multimap<Map.Entry<Long, String>, ActionDetailApp> appMultimap = HashMultimap.create();
		for (ActionDetailApp detailApp : detailApps) {
			appMultimap.put(new SimpleMapEntry(detailApp.getModuleId(), detailApp.getGroupAction()), detailApp);
		}*/
//							 Backup
//		<h:outputText value="Stop" rendered="#{obj.actionRollback == '1'}"/>
//		<h:outputText value="Upcode" rendered="#{obj.actionRollback == '2'}"/>
//		<h:outputText value="Clear cache" rendered="#{obj.actionRollback == '3'}"/>
//		<h:outputText value="Restart" rendered="#{obj.actionRollback == '4'}"/>
//		<h:outputText value="Start" rendered="#{obj.actionRollback == '5'}"/>

        ActionDetailApp detailApp;
        Collection<ActionDetailApp> apps;
        /*20181121_hoangnd_save all step_start*/
        boolean addCheckStatus = false;
        /*20181121_hoangnd_save all step_end*/
        for (Module module : actionController.getImpactModules().values()) {
            switch (module.getActionType()) {
                case 0: // Stop/Start
                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_STOP));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(Constant.STEP_STOP);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(Constant.STEP_START);
                        detailApp.setIpServer(module.getIpServer());
                        lstStop.add(detailApp);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    } else {
                        lstStop.addAll(apps);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_START));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(Constant.STEP_START);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(Constant.STEP_STOP);
                        detailApp.setIpServer(module.getIpServer());
                        lstStart.add(detailApp);
                    } else {
                        lstStart.addAll(apps);
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_CLEARCACHE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_CLEARCACHE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.add(detailApp);
                    } else {
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.addAll(apps);
                    }
                    break;
                case 1: // Upcode
                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_BACKUP));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_BACKUP);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        lstBackup.add(detailApp);
                    } else {
                        lstBackup.addAll(apps);
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_UPCODE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_UPCODE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        lstUpcode.add(detailApp);
                    } else {
                        lstUpcode.addAll(apps);
                    }
                    //20190416_tudn_start import rule config
                    lstModuleUpcode.add(module);
                    //20190416_tudn_end import rule config

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_CLEARCACHE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_CLEARCACHE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.add(detailApp);
                    } else {
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.addAll(apps);
                    }
                    break;
                case 2: // Upcode + Restart
                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_STOP));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_STOP);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(AamConstants.RUN_STEP.STEP_START);
                        detailApp.setIpServer(module.getIpServer());
                        lstStop.add(detailApp);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    } else {
                        lstStop.addAll(apps);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_START));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_START);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(AamConstants.RUN_STEP.STEP_STOP);
                        detailApp.setIpServer(module.getIpServer());
                        lstStart.add(detailApp);
                    } else {
                        lstStart.addAll(apps);
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_CLEARCACHE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_CLEARCACHE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.add(detailApp);
                    } else {
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.addAll(apps);
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_BACKUP));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_BACKUP);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        lstBackup.add(detailApp);
                    } else {
                        lstBackup.addAll(apps);
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_UPCODE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_UPCODE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        lstUpcode.add(detailApp);
                    } else {
                        lstUpcode.addAll(apps);
                    }
                    //20190416_tudn_start import rule config
                    lstModuleUpcode.add(module);
                    //20190416_tudn_end import rule config

                    break;
                case 3: // Restart
                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_RESTART));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_RESTART);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(AamConstants.RUN_STEP.STEP_RESTART);
                        detailApp.setIpServer(module.getIpServer());
                        lstRestart.add(detailApp);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    } else {
                        lstRestart.addAll(apps);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    }
                    break;
                case 4: // Restart + upcode
                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_RESTART));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_RESTART);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(AamConstants.RUN_STEP.STEP_RESTART);
                        detailApp.setIpServer(module.getIpServer());
                        lstRestart.add(detailApp);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    } else {
                        lstRestart.addAll(apps);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_CLEARCACHE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_CLEARCACHE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.add(detailApp);
                    } else {
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.addAll(apps);
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_BACKUP));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_BACKUP);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        lstBackup.add(detailApp);
                    } else {
                        lstBackup.addAll(apps);
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_UPCODE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_UPCODE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        lstUpcode.add(detailApp);
                    } else {
                        lstUpcode.addAll(apps);
                    }
                    //20190416_tudn_start import rule config
                    lstModuleUpcode.add(module);
                    //20190416_tudn_end import rule config

                    break;
                case 5: // Restart
                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_RESTART_CMD));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_RESTART_CMD);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(AamConstants.RUN_STEP.STEP_RESTART_CMD);
                        detailApp.setIpServer(module.getIpServer());
                        lstRestartCmd.add(detailApp);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    } else {
                        lstRestartCmd.addAll(apps);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_CLEARCACHE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_CLEARCACHE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.add(detailApp);
                    } else {
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.addAll(apps);
                    }
                    break;
                case 6: // Restart + upcode
                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_RESTART_CMD));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_RESTART_CMD);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(AamConstants.RUN_STEP.STEP_RESTART_CMD);
                        detailApp.setIpServer(module.getIpServer());
                        lstRestartCmd.add(detailApp);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    } else {
                        lstRestartCmd.addAll(apps);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_UPCODE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_UPCODE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        lstUpcode.add(detailApp);
                    } else {
                        lstUpcode.addAll(apps);
                    }
                    //20190416_tudn_start import rule config
                    lstModuleUpcode.add(module);
                    //20190416_tudn_end import rule config

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_CLEARCACHE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_CLEARCACHE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.add(detailApp);
                    } else {
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.addAll(apps);
                    }
                    break;
                case 8: // Start
                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_START));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_START);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(AamConstants.RUN_STEP.STEP_STOP);
                        detailApp.setIpServer(module.getIpServer());
                        lstStart.add(detailApp);
                    } else {
                        lstStart.addAll(apps);
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_CLEARCACHE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_CLEARCACHE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.add(detailApp);
                    } else {
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.addAll(apps);
                    }
                    break;
                case 9: // Stop
                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_STOP));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_STOP);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(AamConstants.RUN_STEP.STEP_START);
                        detailApp.setIpServer(module.getIpServer());
                        lstStop.add(detailApp);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    } else {
                        lstStop.addAll(apps);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    }
                    break;
                case 10: // Upcode + Start
                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), Constant.STEP_UPCODE_STOP_START));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(Constant.STEP_UPCODE_STOP_START);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(Constant.STEP_ROLLBACK_START);
                        detailApp.setIpServer(module.getIpServer());
                        lstUpcodeStart.add(detailApp);
                    } else {
                        lstUpcodeStart.addAll(apps);
                    }
                    //20190416_tudn_start import rule config
                    lstModuleStopStartUpcode.add(module);
                    //20190416_tudn_end import rule config

                    break;
                case AamConstants.MODULE_GROUP_ACTION.SWICH_DR:
                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_CLEARCACHE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_CLEARCACHE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.add(detailApp);
                    } else {
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.addAll(apps);
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_BACKUP));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_BACKUP);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        lstBackup.add(detailApp);
                    } else {
                        lstBackup.addAll(apps);
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_UPCODE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_UPCODE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        lstUpcode.add(detailApp);
                    } else {
                        lstUpcode.addAll(apps);
                    }
                    //20190416_tudn_start import rule config
                    lstModuleUpcode.add(module);
                    //20190416_tudn_end import rule config
                    break;
                case AamConstants.MODULE_GROUP_ACTION.SWICH_DR_STOP_START: // Upcode + Restart
                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_STOP));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_STOP);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(AamConstants.RUN_STEP.STEP_START);
                        detailApp.setIpServer(module.getIpServer());
                        lstStop.add(detailApp);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    } else {
                        lstStop.addAll(apps);
                        /*20181121_hoangnd_save all step_start*/
                        if (!addCheckStatus)
                            addCheckStatus = true;
                        /*20181121_hoangnd_save all step_end*/
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_START));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_START);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(AamConstants.RUN_STEP.STEP_STOP);
                        detailApp.setIpServer(module.getIpServer());
                        lstStart.add(detailApp);
                    } else {
                        lstStart.addAll(apps);
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_CLEARCACHE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_CLEARCACHE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.add(detailApp);
                    } else {
                        if (StringUtils.isNotEmpty(module.getDeleteCache()) && !"N/A".equals(module.getDeleteCache().trim().toUpperCase()))
                            lstClearCache.addAll(apps);
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_BACKUP));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_BACKUP);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        lstBackup.add(detailApp);
                    } else {
                        lstBackup.addAll(apps);
                    }

                    apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), AamConstants.RUN_STEP.STEP_UPCODE));
                    if (apps == null || apps.isEmpty()) {
                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_UPCODE);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setIpServer(module.getIpServer());
                        lstUpcode.add(detailApp);
                    } else {
                        lstUpcode.addAll(apps);
                    }
                    //20190416_tudn_start import rule config
                    lstModuleUpcode.add(module);
                    //20190416_tudn_end import rule config
                    break;
                default:
                    break;
            }
            /*20181121_hoangnd_save all step_start*/
            if (addCheckStatus) {
                apps = appMultimap.get(new SimpleMapEntry(module.getModuleId(), Constant.STEP_CHECK_STATUS));
                if (apps == null || apps.isEmpty()) {
                    detailApp = new ActionDetailApp();
                    detailApp.setGroupAction(Constant.STEP_CHECK_STATUS);
                    detailApp.setModuleId(module.getModuleId());
                    detailApp.setModuleOrder(1L);
                    detailApp.setIpServer(module.getIpServer());
                    lstCheckStatus.add(detailApp);
                } else {
                    lstCheckStatus.addAll(apps);
                }
                addCheckStatus = false;
            }
            /*20181121_hoangnd_save all step_end*/
        }

        // 20190417_thenv_start change file config
        // Lay danh sach moduleCode tuong ung actionDetailApp
        Map<Long, String> mapAdaIdModuleCode = new HashMap<>();
        for (ActionDetailApp ada : lstUpcode) {
            mapAdaIdModuleCode.put(ada.getId(), actionController.getImpactModules().get(ada.getModuleId()).getModuleCode());
        }
        for (ActionDetailApp ada : lstUpcodeStart) {
            mapAdaIdModuleCode.put(ada.getId(), actionController.getImpactModules().get(ada.getModuleId()).getModuleCode());
        }


        // Lay danh sach rule va add moduleCode
        ruleConfigList = getListRuleConfig(actionController.getNewObj().getId());
        for (RuleConfig rule : ruleConfigList) {
            rule.setModuleCode(mapAdaIdModuleCode.get(rule.getActionDetailAppId()));
        }
        // 20190417_thenv_end change file config

        // anhnt02 - Only change order when KBUCTT fire event change
        if (actionController.isBdUctt() && actionController.isHandleChange()) {
            Map<Long, Integer> mdLevels = new HashMap<>();
            int maxLevel = 0;
            if (actionController.getMdDependents() != null) {
                for (MdDependent mdDependent : actionController.getMdDependents()) {
                    if (mdDependent.getLevel() > maxLevel)
                        maxLevel = mdDependent.getLevel();

                    Integer level = mdLevels.get(mdDependent.getDependentId());
                    if (level == null || level < mdDependent.getLevel())
                        mdLevels.put(mdDependent.getDependentId(), mdDependent.getLevel());
                }
            }
            for (ActionDetailApp actionDetailApp : lstStop) {
                Integer level = mdLevels.get(actionDetailApp.getModuleId());
                if (level != null)
                    actionDetailApp.setModuleOrder(Long.valueOf(maxLevel - level + 1));
                else
                    actionDetailApp.setModuleOrder(Long.valueOf(maxLevel + 1));
            }

            for (ActionDetailApp actionDetailApp : lstStart) {

                Integer level = mdLevels.get(actionDetailApp.getModuleId());
                if (actionController.getNewObj().getKbType().equals(AamConstants.KB_TYPE.UCTT_START)) {
                    if (level != null)
                        actionDetailApp.setModuleOrder(Long.valueOf(maxLevel - level + 1));
                    else
                        actionDetailApp.setModuleOrder(Long.valueOf(maxLevel + 1));
                } else {
                    if (level != null)
                        actionDetailApp.setModuleOrder(Long.valueOf(level + 1));
                    else
                        actionDetailApp.setModuleOrder(Long.valueOf(1));
                }
            }


            if (actionController.getNewObj().getMaxConcurrent() > 0) {
                changeOrder(lstStop);
                changeOrder(lstUpcode);
                changeOrder(lstRestart);
                changeOrder(lstClearCache);
                changeOrder(lstStart);
                changeOrder(lstUpcodeStart);
                changeOrder(lstRestartCmd);
                /*20181030_hoangnd_save all step_start*/
                changeOrder(lstCheckStatus);
                /*20181030_hoangnd_save all step_end*/
            }
        }
    }

    /*20181101_hoangnd_save all step_start*/
    public void buildChecklistApp() {

        lstChecklistApp = new ArrayList<>();
        ActionDetailApp detailApp;
        if (actionController.getCklListSelectedNodes() != null && actionController.getCklListSelectedNodes().length > 0) {
            for (TreeNode treeNode : actionController.getCklListSelectedNodes()) {
                if (((TreeObject) treeNode.getData()).getObj() instanceof Checklist) {
                    detailApp = new ActionDetailApp();
                    detailApp.setGroupAction(Constant.STEP_CHECKLIST_APP);
                    detailApp.setModuleId(0L);
                    detailApp.setModuleOrder(1L);
                    lstChecklistApp.add(detailApp);
                    break;
                }
            }
        }

    public void buildChecklistDb() {

        lstChecklistDb = new ArrayList<>();
        ActionDetailApp detailApp;

        if (actionController.getCklDbListSelectedNodes() != null && actionController.getCklDbListSelectedNodes().length > 0) {
            for (TreeNode treeNode : actionController.getCklDbListSelectedNodes()) {
                if (((TreeObject) treeNode.getData()).getObj() instanceof QueueChecklist) {
                    detailApp = new ActionDetailApp();
                    detailApp.setGroupAction(Constant.STEP_CHECKLIST_DB);
                    detailApp.setModuleId(0L);
                    detailApp.setModuleOrder(1L);
                    lstChecklistDb.add(detailApp);
                    break;
                }
            }
        }


    private void changeOrder(List<ActionDetailApp> actionDetailApps) {
        // Process for ModuleOrder + ActionDetailApp
        Multimap<Long, ActionDetailApp> actionDetailAppMultimap = HashMultimap.create();
        for (ActionDetailApp actionDetailApp : actionDetailApps) {
            actionDetailAppMultimap.put(actionDetailApp.getModuleOrder(), actionDetailApp);
        }

        List<Long> keys = new ArrayList<>(actionDetailAppMultimap.keySet());
        Collections.sort(keys);
        Integer increase;
        for (int i = 0; i < keys.size(); i++) {
            Collection<ActionDetailApp> detailApps = actionDetailAppMultimap.get(keys.get(i));
            // Process for ServerId + ActionDetailApp
            Multimap<String, ActionDetailApp> actionDetailAppMultimapSub = HashMultimap.create();
            for (ActionDetailApp actionDetailAppSub : detailApps) {
                if (actionDetailAppSub.getIpServer() != null && !actionDetailAppSub.getIpServer().isEmpty() && !actionDetailAppSub.getIpServer().equals("")) {
                    actionDetailAppMultimapSub.put(actionDetailAppSub.getIpServer(), actionDetailAppSub);
                }
            }
            List<String> keysSub = new ArrayList<>(actionDetailAppMultimapSub.keySet());

            increase = 0;
            for (int iSub = 0; iSub < keysSub.size(); iSub++) {
                Collection<ActionDetailApp> detailAppsSub = actionDetailAppMultimapSub.get(keysSub.get(iSub));
                Integer counter = 0;
                Integer increaseSub = 0;
                for (ActionDetailApp detailAppSub : detailAppsSub) {
                    increaseSub = (int) Math.ceil(counter / actionController.getNewObj().getMaxConcurrent());
                    detailAppSub.setModuleOrder(detailAppSub.getModuleOrder() + increaseSub);

                    counter++;
                }
                if (increaseSub > increase) {
                    increase = increaseSub;
                }

            }
            if (increase > 0) {
                for (int j = 0; j < keys.size(); j++) {
                    if (keys.get(j) > keys.get(i)) {
                        Collection<ActionDetailApp> increaseDetailApps = actionDetailAppMultimap.get(keys.get(j));
                        for (ActionDetailApp increaseDetailApp : increaseDetailApps) {
                            increaseDetailApp.setModuleOrder(increaseDetailApp.getModuleOrder() + increase);
                        }
                    }
                }

                logger.info(actionDetailApps);

                changeOrder(actionDetailApps);
                break;
            }
        }
    }

    public String detailDt(ActionDetailApp detail, String actionType) {
        return DocxUtil.getDt(actionController.getImpactModules().get(detail.getModuleId()), actionType, detail);
    }

    public Module getModule(Long moduleId) {
        return actionController.getImpactModules().get(moduleId);
    }

    public String buidAppCodeByAppId(Long moduleId) {
        return actionController.getImpactModules().get(moduleId).getModuleCode();
    }

    public String buidAppGrNameByAppId(Long moduleId) {
        return actionController.getImpactModules().get(moduleId).getServiceName();

    }

    public void setLogCheck() {
        if (newObj != null)
            newObj.setCheckCmdResult(checkCmdResult);
    }

    public String buidIpByAppId(Long moduleId) {
        return actionController.getImpactModules().get(moduleId).getIpServer();

    }

    public String buidAppNameByApp(Module moduleId) {
        return actionController.getImpactModules().get(moduleId).getModuleName();
    }

    public String buidAppNameByAppId(Long moduleId) {
        return actionController.getImpactModules().get(moduleId).getModuleName();
    }

    public StreamedContent downloadFile(ActionDetailApp obj) {
        StreamedContent fileInput = null;
//        InputStream stream;
        String filePath = UploadFileUtils.getSourceCodeFolder(action) + File.separator + obj.getUploadFilePath();

        try {
            fileInput = new DefaultStreamedContent(new FileInputStream(filePath), "", obj.getUploadFilePath());
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return fileInput;
    }

    public StreamedContent downloadTemplate() {
        String template = getUploadFolder() + File.separator + "file-template" + File.separator + "TEMPLATE.txt";

        StreamedContent fileOutput = null;
        try {
            fileOutput = new DefaultStreamedContent(new FileInputStream(template), "", "template.txt");
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
            return null;
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return fileOutput;

    }

    private String getUploadFolder() {
        FacesContext context = FacesContext.getCurrentInstance();
        HttpServletRequest request = (HttpServletRequest) context.getExternalContext().getRequest();
        String folderPath = request.getRealPath("resources");

        return folderPath;
    }


    /**
     * Lay phan mo rong cua file.
     */
    private String getFileExtension(String fileName) {
        String fExt;
        int mid = fileName.lastIndexOf(".");
        fExt = fileName.substring(mid, fileName.length());
        return fExt;
    }

    // hàm lấy tên file
    private String getNameFile(String fileName) {
        String fName;
        int mid = fileName.lastIndexOf(".");
        fName = fileName.substring(0, mid);
        return fName;
    }

    public void handleUploadCode(FileUploadEvent event) {
        UploadedFile file = event.getFile();

        String uploadFolder = UploadFileUtils.getSourceCodeFolder(action);
        if (file != null) {
            Module detail = actionController.getImpactModules().get(newObj.getModuleId());

            if (detail == null)
                return;
            sourceCode = Util.convertUTF8ToNoSign(detail.getModuleCode() + "_" + newObj.getUpcodePath().replaceAll("\\.\\.", "").replaceAll("/", "_").replaceAll("\\\\", "_") + "_" + file.getFileName());
            FileHelper.uploadFile(uploadFolder, file, sourceCode);
            newObj.setUploadFilePath(sourceCode);

            try {
                String newMd5 = DigestUtils.md5Hex(file.getInputstream());
                newObj.setMd5(newMd5);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void handleUploadListFile(FileUploadEvent event) {
        UploadedFile file = event.getFile();
//        String folder = "source_code";
        String uploadFolder = UploadFileUtils.getSourceCodeFolder(action);
        if (file != null) {
            listFile = Util.convertUTF8ToNoSign(getNameFile(file.getFileName())) + getFileExtension(file.getFileName());
            FileHelper.uploadFile(uploadFolder, file, listFile);
            newObj.setListFileCode(listFile);
        }

    }

    public void checkUpcodeFolderChosen() {
        if (StringUtils.isEmpty(newObj.getUpcodePath())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("folder.up.code.do.not.select"), "");
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            return;
        }
    }

    public void addAppDetail() {
        if (lstUpcode == null)
            lstUpcode = new ArrayList<>();

        if (StringUtils.isEmpty(newObj.getUploadFilePath())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("file.code.have.not.uploaded.yet"), "");
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            return;
        }

        if (StringUtils.isEmpty(newObj.getUpcodePath())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("folder.up.code.do.not.select"), "");
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            return;
        }

        Module detail = actionController.getImpactModules().get(newObj.getModuleId());
        if (("../" + FilenameUtils.getName(detail.getExecutePath().trim().replaceAll("/$", ""))).equals(newObj.getUpcodePath())) {
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("up.code.to.folder.install.is.forbidden"), "");
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            return;
        }

        for (ActionDetailApp detailApp : lstUpcode) {
            if (detailApp != selectedObj && detailApp.getUpcodePath() != null && detailApp.getModuleId().equals(newObj.getModuleId()) && detailApp.getUpcodePath().equals(newObj.getUpcodePath())) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("folder.up.code.existed"), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                return;
            }
        }


		/*if (newObj.getFile()) {
			if (!newObj.getUploadFilePath().equals(newObj.getUpcodePath())) {
				FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, "Phải up file " + newObj.getUpcodePath(), "");
				FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
				return;
			}
		} else */
        if (!CheckZipUtils.checkUpcodeZip(newObj.getUploadFilePath(), newObj.getUpcodePath(), action)) {
            String errorDetail;
            if (newObj.getFile()) {
                errorDetail = MessageUtil.getResourceBundleMessage("only.up.file") + " " + newObj.getUpcodePath();
            } else {
                errorDetail = MessageUtil.getResourceBundleMessage("file.code.must.compress.in.folder") + " " + FilenameUtils.getName(newObj.getUpcodePath());
            }
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, errorDetail, "");
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            return;
        }

        List<String> removeFiles = new ArrayList<>(Splitter.on(",").trimResults().omitEmptyStrings().splitToList(newObj.getLstFileRemove() == null ? "" : newObj.getLstFileRemove().replaceAll("'", "")));
        for (String removeFile : removeFiles) {
            if (!removeFile.startsWith(newObj.getUpcodePath() + "/") && !removeFile.startsWith(newObj.getUpcodePath() + "\\")) {
                FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("only.delete.file.in.folder.up.code") + " " + newObj.getUpcodePath(), "");
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                return;
            }
        }
        newObj.setModifyDate(new Date());

/*		if (isEdit)
			lstUpcode.remove(selectedObj);*/

        if (!isEdit) {
            lstUpcode.add(newObj);
        } else {
            BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
            try {
                BeanUtils.copyProperties(selectedObj, newObj);
                // newObj.setPassword("");
            } catch (IllegalAccessException | InvocationTargetException e) {
                logger.error(e.getMessage(), e);
            }
        }

        logger.info(dualListModel.getTarget().size());

        for (ActionDetailApp actionDetailApp : lstUpcode) {
            for (Module module : dualListModel.getTarget()) {
                if (actionDetailApp.getModuleId().equals(module.getModuleId())) {
                    actionDetailApp.setUpcodePath(newObj.getUpcodePath());
                    actionDetailApp.setUploadFilePath(newObj.getUploadFilePath());
                    actionDetailApp.setLstFileRemove(newObj.getLstFileRemove());
                    actionDetailApp.setModuleOrder(newObj.getModuleOrder());
                    actionDetailApp.setModifyDate(newObj.getModifyDate());
                    break;
                }
            }
        }

        sourceCode = "";
        listFile = "";

        RequestContext.getCurrentInstance().execute("PF('editDialogApp').hide()");
    }

    public void preAddAppDetail() {
        newObj = new ActionDetailApp();
        // 20190417_thenv_start change file config
        checkShowChangeConfig(newObj.getUpcodePath());
        // 20190417_thenv_end change file config
    }

    public void getCheckLog(ActionDetailApp obj) {
        checkCmdResult = obj.getCheckCmdResult();
    }

    public void prepareEdit(ActionDetailApp obj) {
        dualListModel = new DualListModel<>();
        sources = new ArrayList<>();
        targets = new ArrayList<>();
        List<Long> moduleIds = new ArrayList<>();
        for (ActionDetailApp actionDetailApp : lstUpcode) {
            if (actionDetailApp != obj && StringUtils.isEmpty(actionDetailApp.getUpcodePath())) {
                moduleIds.add(actionDetailApp.getModuleId());
            }
        }


        try {
            if (moduleIds.isEmpty())
                sources = new ArrayList<>();
            else
                sources = iimService.findModulesByIds(action.getImpactProcess().getNationCode(), moduleIds);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        dualListModel.setSource(sources);
        dualListModel.setTarget(targets);

        isEdit = true;
        selectedObj = obj;
        newObj = new ActionDetailApp();
        checkCmdResult = newObj.getCheckCmdResult();
        BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
        try {
            BeanUtils.copyProperties(newObj, obj);
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        }

        Module module = actionController.getImpactModules().get(obj.getModuleId());
        TreeObject cklDbParent = new TreeObject(MessageUtil.getResourceBundleMessage("up.code.directory"), module.getExecutePath());
        this.upcodeRoot = new DefaultTreeNode(cklDbParent, null);

        actionController.addChilds(upcodeRoot, module);

        TreeObject removeCklDbParent = new TreeObject(MessageUtil.getResourceBundleMessage("file.need.delete"), module.getExecutePath());
        this.removeUpcodeRoot = new DefaultTreeNode(removeCklDbParent, null);

        if (StringUtils.isNotEmpty(newObj.getUpcodePath())) {
            ((TreeObject) removeUpcodeRoot.getData()).setObj(module.getExecutePath() + "/" + newObj.getUpcodePath());
            actionController.addChilds(removeUpcodeRoot, module);
        }

        // 20190417_thenv_start change file config
        checkShowChangeConfig(newObj.getUpcodePath());
        // 20190417_thenv_end change file config
    }

    public void onNodeExpand(NodeExpandEvent event) {
        TreeNode treeNode = event.getTreeNode();
        Module module = actionController.getImpactModules().get(newObj.getModuleId());

        actionController.addChilds(treeNode, module);
    }

    public void onNodeExpandRemove(NodeExpandEvent event) {
        TreeNode treeNode = event.getTreeNode();
        Module module = actionController.getImpactModules().get(newObj.getModuleId());

        actionController.addChilds(treeNode, module);
    }

    public void removeFile() {
        if (selectedRemoveUpcodeFile != null) {
            String fullPath = ((TreeObject) selectedRemoveUpcodeFile.getData()).getObj().toString();

            Module detail = actionController.getImpactModules().get(newObj.getModuleId());

            String installPath = detail.getExecutePath().replaceAll("/$", "").replaceAll("\\\\$", "");
            String path;
            if (fullPath.contains(installPath) && !fullPath.equals(installPath)) {
                path = fullPath.replaceFirst(detail.getExecutePath().replaceAll("\\\\", "\\\\\\\\"), "").replaceFirst("^/", "").replaceFirst("^\\\\", "");
            } else {
                path = ".." + fullPath.replaceFirst(FilenameUtils.getFullPathNoEndSeparator(installPath.replaceAll("/$", "").replaceAll("\\\\$", "")), "");
            }

            List<String> removeFiles = new ArrayList<>(Splitter.on(",").trimResults().omitEmptyStrings().splitToList(newObj.getLstFileRemove() == null ? "" : newObj.getLstFileRemove()));
            if (!removeFiles.contains("'" + path + "'")) {
                removeFiles.add("'" + path + "'");
            }
            newObj.setLstFileRemove(Joiner.on(", ").join(removeFiles));

            FacesMessage message = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("selected.for.delete") + ": " + ((TreeObject) selectedRemoveUpcodeFile.getData()).getName(), "");
            FacesContext.getCurrentInstance().addMessage(null, message);
        }
    }

    public void selectDir(TreeObject selectedUpcodeDir) {
//		this.selectedUpcodeDir = selectedUpcodeDir;
        if (selectedUpcodeDir != null) {
//			TreeObject object = (TreeObject) selectedUpcodeDir.getData();
            String fullPath = selectedUpcodeDir.getObj().toString();

            Module detail = actionController.getImpactModules().get(newObj.getModuleId());

            String installPath = detail.getExecutePath().replaceAll("/$", "").replaceAll("\\\\$", "");
            String path;
            if (fullPath.contains(installPath) && !fullPath.equals(installPath)) {
                path = fullPath.replaceFirst(detail.getExecutePath().replaceAll("\\\\", "\\\\\\\\"), "").replaceFirst("^/", "")
                        .replaceFirst("^\\\\", "");

            } else {
                path = ".." + fullPath.replaceFirst(FilenameUtils.getFullPathNoEndSeparator(installPath.replaceAll("/$", "").replaceAll("\\\\$", "")), "");
            }


            if ((newObj.getUpcodePath() != null && (!newObj.getUpcodePath().equals(path))) || !selectedUpcodeDir.getIsDir()) {
                newObj.setLstFileRemove("");
            }
            newObj.setUpcodePath(path);
            newObj.setFile(!selectedUpcodeDir.getIsDir());

            if (selectedUpcodeDir.getIsDir()) {
                ((TreeObject) removeUpcodeRoot.getData()).setObj(fullPath);
                actionController.addChilds(removeUpcodeRoot, detail);
            }

            // 20190417_thenv_start change file config
            checkShowChangeConfig(path);
            fileContentNew = "";
            // 20190417_thenv_end change file config
        }
    }

    public void init() {
        isEdit = false;
        newObj = new ActionDetailApp();
        newObj.setModuleOrder(1L);

        lstBackup = new ArrayList<>();
        lstStop = new ArrayList<>();
        lstUpcode = new ArrayList<>();
        lstRestart = new ArrayList<>();
        lstClearCache = new ArrayList<>();
        lstStart = new ArrayList<>();
        lstUpcodeStart = new ArrayList<>();
        lstRestartCmd = new ArrayList<>();
        /*20181030_hoangnd_save all step_start*/
        lstCheckStatus = new ArrayList<>();
        /*20181030_hoangnd_save all step_end*/
        ruleConfigList = new ArrayList<>();
        lstRuleConfigNewObj = new ArrayList<>();
        lstRuleEdit = new ArrayList<>();
        getConfigShowChangeConfig();
        getRuleEdits();
        TreeObject cklDbParent = new TreeObject(MessageUtil.getResourceBundleMessage("up.code.directory"), null);
        this.upcodeRoot = new DefaultTreeNode(cklDbParent, null);
    }

    public void duplicate(ActionDetailApp obj) {
        dualListModel = new DualListModel<>();
        sources = new ArrayList<>();
        targets = new ArrayList<>();
        List<Long> moduleIds = new ArrayList<>();
        for (ActionDetailApp actionDetailApp : lstUpcode) {
            if (actionDetailApp != obj && StringUtils.isEmpty(actionDetailApp.getUpcodePath())) {
                moduleIds.add(actionDetailApp.getModuleId());
            }
        }


        try {
            if (moduleIds.isEmpty())
                sources = new ArrayList<>();
            else
                sources = iimService.findModulesByIds(action.getImpactProcess().getNationCode(), moduleIds);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        dualListModel.setSource(sources);
        dualListModel.setTarget(targets);


        isEdit = false;

        checkCmdResult = obj.getCheckCmdResult();
        // selectedObj = obj;
        newObj = new ActionDetailApp();
        BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
        // 20190417_thenv_start change file config
        try {
            newObj = (ActionDetailApp) BeanUtils.cloneBean(obj);
            newObj.setId(null);
            // newObj.setPassword("");
        } catch (IllegalAccessException | InvocationTargetException e) {
            logger.error(e.getMessage(), e);
        } catch (NoSuchMethodException e) {
            logger.error(e.getMessage(), e);
        } catch (InstantiationException e) {
            logger.error(e.getMessage(), e);
        }
        // 20190417_thenv_end change file config
        newObj.setUpcodePath(null);
        newObj.setUploadFilePath(null);

        Module module = actionController.getImpactModules().get(obj.getModuleId());
        TreeObject cklDbParent = new TreeObject(MessageUtil.getResourceBundleMessage("up.code.directory"), module.getExecutePath());
        this.upcodeRoot = new DefaultTreeNode(cklDbParent, null);

        actionController.addChilds(upcodeRoot, module);

        TreeObject removeCklDbParent = new TreeObject(MessageUtil.getResourceBundleMessage("file.need.delete"), module.getExecutePath());
        this.removeUpcodeRoot = new DefaultTreeNode(removeCklDbParent, null);

        if (StringUtils.isNotEmpty(newObj.getUpcodePath())) {
            ((TreeObject) removeUpcodeRoot.getData()).setObj(module.getExecutePath() + "/" + newObj.getUpcodePath());
            actionController.addChilds(removeUpcodeRoot, module);
        }

        // 20190417_thenv_start change file config
        checkShowChangeConfig(newObj.getUpcodePath());
        // 20190417_thenv_end change file config
    }

    public void upLevelTree() {
        Module module = actionController.getImpactModules().get(newObj.getModuleId());
        TreeObject cklDbParent = new TreeObject(MessageUtil.getResourceBundleMessage("up.code.directory"), FilenameUtils.getFullPathNoEndSeparator(module.getExecutePath().replaceAll("/$", "")));
        this.upcodeRoot = new DefaultTreeNode(cklDbParent, null);

        actionController.addChilds(upcodeRoot, module);
    }

    public void upLevelTreeRemove() {
        Module module = actionController.getImpactModules().get(newObj.getModuleId());
        TreeObject cklDbParent = new TreeObject(MessageUtil.getResourceBundleMessage("up.code.directory"), FilenameUtils.getFullPathNoEndSeparator(module.getExecutePath().replaceAll("/$", "")));
        this.removeUpcodeRoot = new DefaultTreeNode(cklDbParent, null);
        actionController.addChilds(removeUpcodeRoot, module);
    }
    public void clearRemove() {
        newObj.setLstFileRemove("");
    }


    public void saveOrUpdate() {
        FacesMessage msg = null;
        Date startTime = new Date();
        try {
            if (isEdit) {
                // oldPass = selectedObj.getPassword();
            }

            selectedObj = new ActionDetailApp();

            BeanUtilsBean.getInstance().getConvertUtils().register(false, false, 0);
            BeanUtils.copyProperties(selectedObj, newObj);

            if (!isEdit) {
                selectedObj.setId(null);
            }
            actionDetailAppService.saveOrUpdate(selectedObj);
            if (!isEdit) {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("insert.successful"), "");
            } else {
                msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("update.successful"), "");
            }
            /*
            Ghi log tac dong nguoi dung
            */
            //20180620_tudn_start ghi log DB
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDetailAppController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        (isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
                        selectedObj.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            //20180620_tudn_end ghi log DB
        } catch (IllegalAccessException | InvocationTargetException | SysException | AppException e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("update.failed"), "");
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
            newObj = new ActionDetailApp();
            isEdit = false;
        }

        RequestContext.getCurrentInstance().execute("editDialog.hide()");
    }

    public void delete() {
        FacesMessage msg = null;
        try {
            if (selectedObj != null) {
                int counter = 0;
                for (ActionDetailApp detailApp : lstUpcode) {
                    if (detailApp.getModuleId().equals(selectedObj.getModuleId()))
                        counter++;
                }
                if (counter > 1) {
                    lstUpcode.remove(selectedObj);
                    msg = new FacesMessage(FacesMessage.SEVERITY_INFO, MessageUtil.getResourceBundleMessage("delete.successful"), "");
                } else {
                    msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("can.not.delete"), "");
                }
            }

        } catch (SysException e) {
            msg = new FacesMessage(FacesMessage.SEVERITY_ERROR, MessageUtil.getResourceBundleMessage("delete.failed"), "");
            logger.error(e.getMessage(), e);
        } finally {
            FacesContext.getCurrentInstance().addMessage("tdAppGrowl", msg);
        }
    }

    //20190416_tudn_start import rule config
//    public void exportTemplateImportRuleConfig() {
//        HttpServletResponse servletResponse = preHeader();
//        String file = CommonExport.getTemplateMultiExport(MessageUtil.getResourceBundleMessage("key.template.file.import.rule.config"));
//        try (InputStream is = new FileInputStream(file)) {
//            //servletResponse.getOutputStream()
//            try (OutputStream os = servletResponse.getOutputStream()) {
//                Context context = new Context();
//                List<ObjectImportDt> params = new LinkedList<ObjectImportDt>();
//                List<String> sheetNames = new LinkedList<>();
//                JxlsHelper.getInstance().setDeleteTemplateSheet(true).processTemplateAtCell(is, os, context, "Sheet2!A1");
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//        FacesContext.getCurrentInstance().responseComplete();
//    }


    public StreamedContent exportTemplateImportRuleConfig() {
        String filePath = CommonExport.getTemplateMultiExport(MessageUtil.getResourceBundleMessage("key.template.file.import.rule.config"));
        try {
            File file = new File(filePath);
            if (!file.exists())
                return null;
            return new DefaultStreamedContent(new ByteArrayInputStream(FileUtils.readFileToByteArray(file)), "application/xlsx", "Template_Import_Change_Config.xlsx");
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    public void preImportRule(int type) {
        Module module = actionController.getImpactModules().get(newObj.getModuleId());
        if(module.getUsername() != null && module.getUsername().equalsIgnoreCase("root")){
            FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, MessageUtil.getResourceBundleMessage("app.warn.not.support"), "");
            if (msg != null) {
                FacesContext.getCurrentInstance().addMessage("designGrowl", msg);
                RequestContext.getCurrentInstance().update("insertEditForm:designGrowl");
            }
            return;
        }
        RequestContext.getCurrentInstance().execute("PF('importDlg').show();");
        resultImport = null;
        if (type == 2) {
            typeStep = Constant.STEP_UPCODE;
        } else {
            typeStep = Constant.STEP_UPCODE_STOP_START;
        }
    }

    File fileOutPut;

    public StreamedContent downloadFileResultImport() {
        try {
            if (fileOutPut != null && fileOutPut.exists())
                return new DefaultStreamedContent(new ByteArrayInputStream(FileUtils.readFileToByteArray(fileOutPut)), "application/xlsx", fileOutPut.getName());
        } catch (FileNotFoundException e) {
            logger.error(e.getMessage(), e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    public void handleImportRuleConfig(FileUploadEvent event) {
        String paramTypeStep = (String) event.getComponent().getAttributes().get("paramTypeStep");
        Workbook workbook = null;
//        List<?> objectImports = new LinkedList<>();
        Map<Integer, ?> mapObjectImports = new LinkedHashMap<>();
        try {
            InputStream inputstream = event.getFile().getInputstream();

            if (inputstream == null) {
                throw new NullPointerException("inputstream is null");
            }
            //Get the workbook instance for XLS/xlsx file
            try {
                workbook = WorkbookFactory.create(inputstream);
            } catch (InvalidFormatException e2) {
                logger.error(e2.getMessage(), e2);
                throw new com.viettel.it.exception.AppException("File importÃ¯Â¿Â½ Excel 97-2012 (xls, xlsx)!");
            } finally {

            }


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

            importer.setRowHeaderNumber(1);
            importer.setIsReplaceSpace(false);
//            List<Serializable> objects = importer.getDatas(workbook, 0, "1-");
//            if (objects != null) {
//                ((List<Object>) objectImports).addAll(objects);
//            }
            Map<Integer, Serializable> objects = importer.getDatasLineExcel(workbook, 0, "1-", null);
            if (objects != null) {
                ((Map<Integer, Object>) mapObjectImports).putAll(objects);
            }
            ResultDTO resultDTO = new ResultDTO();
//            List<LinkedHashMap<String, String>> mapData = new ArrayList<>();
            Map<Integer, LinkedHashMap<String, String>> mapData = new LinkedHashMap<>();

            List<RuleConfig> ruleConfigBackup = new ArrayList<>();
            ruleConfigBackup.addAll(ruleConfigList);

            //validate file import
            if (paramTypeStep != null && Constant.STEP_UPCODE_STOP_START.equals(paramTypeStep)) {
                validateData(mapObjectImports, resultDTO, mapData, ruleConfigList, ruleConfigBackup, lstModuleStopStartUpcode, lstUpcodeStart);
            } else {
                validateData(mapObjectImports, resultDTO, mapData, ruleConfigList, ruleConfigBackup, lstModuleUpcode, lstUpcode);
            }

            //neu file loi dua ra thong bao
            if (resultDTO.getResultCode() == 1) {
                //xuat file ket qua neu file loi
                if (!isNullOrEmpty(resultDTO.getData())) {
                    fileOutPut = exportFileResult(workbook, mapData, 0, event.getFile().getFileName());

                    resultImport = new DefaultStreamedContent(new ByteArrayInputStream(FileUtils.readFileToByteArray(fileOutPut)), ".xlsx", fileOutPut.getName());
                }
                //thong bao message neu gap exception va file loi
                if (!isNullOrEmpty(resultDTO.getResultMessage())) {
                    MessageUtil.setErrorMessage(resultDTO.getResultMessage());
                    ruleConfigList.clear();
                    ruleConfigList.addAll(ruleConfigBackup);
                }
                return;
            }

            RequestContext.getCurrentInstance().execute("PF('importDlg').hide()");
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

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean validPath(String path) {
        Pattern linux_pattern = Pattern.compile("^[a-zA-Z0-9!#$%&'()+,-.;=?@\\[\\]^_`{}~ ]([a-zA-Z0-9!#$%&'()+,-.;=?@\\[\\]^_`{}~ ]*[\\\\\\/])*[a-zA-Z0-9!#$%&'()+,-.;=?@\\[\\]^_`{}~ ]+$");
        Matcher m1 = linux_pattern.matcher(path);

        return m1.matches();
    }

    public Boolean checkFilePath(String path, String fileName, Module module) {
        ImpactProcess impactProcess = actionController.getNewObj().getImpactProcess();

        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("lsdir").queryParam("curDir", path);
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = builder.post(Entity.json(module));

        if (response.getStatus() == Response.Status.OK.getStatusCode()) {
            List<Map.Entry<String, Boolean>> childFiles = response.readEntity(new GenericType<List<Map.Entry<String, Boolean>>>() {
            });
            if (childFiles != null) {
                for (Map.Entry<String, Boolean> childFile : childFiles) {
                    if (FilenameUtils.getName(childFile.getKey()).equals(fileName))
                        return true;
                }
            }
        }
        return false;
    }

    public void validateData(Map<Integer, ?> lstCellObjectImports, ResultDTO resultDTO, Map<Integer, LinkedHashMap<String, String>> mapData, List<RuleConfig> ruleConfigList, List<RuleConfig> ruleConfigBackup
            , List<Module> lstModuleUpcode, List<ActionDetailApp> lstUpcode) {
        //danh sach cot
        List<String> lstColumCell = new ArrayList<>();
        List<String> lstHeaderCell = new ArrayList<>();
        Map<String, Object> filter = new HashMap<>();
        Map<String, Module> mapModule = new HashMap<String, Module>();
        Map<String, ActionDetailApp> mapActionDetailApp = new HashMap<String, ActionDetailApp>();
        Map<String, RuleConfig> mapRuleConfig = new HashMap<String, RuleConfig>();
        LinkedHashMap<String, String> mapExecuteFile = new LinkedHashMap<String, String>();
        Map<String, GetFileFromServer> mapBackupFile = new HashMap<String, GetFileFromServer>();
        GetFileFromServer server;
        String contentFile = null;
        boolean result = true;
        try {
            lstColumCell.add(MessageUtil.getResourceBundleMessage("label.import.colum.module.code").toLowerCase());
            lstColumCell.add(MessageUtil.getResourceBundleMessage("label.import.colum.path.file.fix").toLowerCase());
            lstColumCell.add(MessageUtil.getResourceBundleMessage("label.import.colum.file.name.fix").toLowerCase());
            lstColumCell.add(MessageUtil.getResourceBundleMessage("label.import.colum.rule").toLowerCase());
            lstHeaderCell.addAll(lstColumCell);
            lstHeaderCell.add(MessageUtil.getResourceBundleMessage("label.import.colum.no").toLowerCase().replace(".", "_"));
            lstHeaderCell.add(MessageUtil.getResourceBundleMessage("label.import.colum.system.name").toLowerCase());
            lstHeaderCell.add(MessageUtil.getResourceBundleMessage("label.import.colum.group.module").toLowerCase());
            lstHeaderCell.add(MessageUtil.getResourceBundleMessage("label.import.colum.keyword").toLowerCase());
            lstHeaderCell.add(MessageUtil.getResourceBundleMessage("label.import.colum.content").toLowerCase());

            if (lstCellObjectImports.size() == 0) {
                MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("no.data.sheetname"));
                return;
            }
            BasicDynaBean basicDynaBeanHeader = (BasicDynaBean) lstCellObjectImports.values().toArray()[0];
            for (String str : lstHeaderCell) { //check ten cot co giong tempalte
                if (!basicDynaBeanHeader.getMap().containsKey(str)) {
                    resultDTO.setResultMessage((isNullOrEmpty(resultDTO.getResultMessage()) ? "" : (resultDTO.getResultMessage() + ";\n")) + MessageFormat.format(MessageUtil.getResourceBundleMessage("label.header.param.not.exits"), str));
                    resultDTO.setResultCode(1);
                }
            }
            if (resultDTO.getResultCode() == 1) {
                return;
            }

            for (Module i : lstModuleUpcode) mapModule.put(i.getModuleCode(), i);
            for (ActionDetailApp i : lstUpcode) mapActionDetailApp.put(i.getModuleId() + "#" + i.getUpcodePath(), i);
            for (RuleConfig i : ruleConfigList) {
                mapRuleConfig.put(i.getRuleEdit() + "#" + i.getKeyword() + "#" + i.getModuleCode() + "#" + i.getPathFile(), i);
            }
            filter.put("id.configGroup-EXAC", "CHANGE_CONFIG");
            filter.put("isActive-EXAC", 1L);
            List<CatConfig> lstFileExtension = new CatConfigServiceImpl().findList(filter);
//            for (int i = 1; i < lstCellObjectImports.size(); i++) {
            for (Map.Entry<Integer, ?> entry : lstCellObjectImports.entrySet()) {
                if (entry.getKey() == 0) {
                    continue;
                }

                String resultDetail = "";
                String resultCode = "";
                LinkedHashMap<String, String> mapParam = new LinkedHashMap<>();
                RuleConfig ruleConfig = new RuleConfig();
                BasicDynaBean basicDynaBean = (BasicDynaBean) entry.getValue();
                for (DynaProperty dynaProperty : basicDynaBean.getDynaClass().getDynaProperties()) {

                    String dataCell = basicDynaBean.getMap().get(dynaProperty.getName()) == null ? null : basicDynaBean.getMap().get(dynaProperty.getName()).toString();
                    if (basicDynaBean.getMap().get(dynaProperty.getName()) == null || "".equals(basicDynaBean.getMap().get(dynaProperty.getName()).toString())) {

                        if (lstColumCell.contains(dynaProperty.getName().toLowerCase())) {
                            if (dataCell == null || "".equals(dataCell.trim())) {
                                resultCode = "NOK";
                                result = false;
                                resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + (dynaProperty.getName() + " " + MessageUtil.getResourceBundleMessage("validate.not.file.empty"));
                            }
                        }
                        if (MessageUtil.getResourceBundleMessage("label.import.colum.keyword").toLowerCase().trim().equalsIgnoreCase(dynaProperty.getName().toLowerCase().trim())) {
                            if (ruleConfig.getRuleEdit() != null
                                    && (!ruleConfig.getRuleEdit().equals(Constant.RuleConfig.RULE_ADD_ON_BOTTOM) && !ruleConfig.getRuleEdit().equals(Constant.RuleConfig.RULE_ADD_ON_HEAD))) {
                                resultCode = "NOK";
                                result = false;
                                resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + (dynaProperty.getName() + " " + MessageUtil.getResourceBundleMessage("validate.not.file.empty"));
                            } else {
                                ruleConfig.setKeyword(dataCell == null || "".equals(dataCell) ? null : dataCell);
                            }
                        }
                        if (MessageUtil.getResourceBundleMessage("label.import.colum.content").toLowerCase().trim().equalsIgnoreCase(dynaProperty.getName().toLowerCase().trim())) {
                            if (ruleConfig.getRuleEdit() != null && !ruleConfig.getRuleEdit().equals(Constant.RuleConfig.RULE_DELETE_KEYWORD)) {
                                resultCode = "NOK";
                                result = false;
                                resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + (dynaProperty.getName() + " " + MessageUtil.getResourceBundleMessage("validate.not.file.empty"));
                            } else {
                                ruleConfig.setContent(dataCell);
                            }
                        }
                        mapParam.put(dynaProperty.getName(), "");
                    } else {
                        if (MessageUtil.getResourceBundleMessage("label.import.colum.module.code").toLowerCase().trim().equalsIgnoreCase(dynaProperty.getName().toLowerCase().trim())) {
                            dataCell = dataCell.trim();
                            if (!mapModule.containsKey(dataCell)) {
                                resultCode = "NOK";
                                result = false;
                                resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + MessageFormat.format(MessageUtil.getResourceBundleMessage("validate.not.exist.module.code"), dataCell);
                            } else {
                                ruleConfig.setModuleCode(dataCell);
                            }
                        }
                        if (MessageUtil.getResourceBundleMessage("label.import.colum.path.file.fix").toLowerCase().trim().equalsIgnoreCase(dynaProperty.getName().toLowerCase().trim())) {
                            dataCell = dataCell.trim();
                            if (dataCell.endsWith("/"))
                                dataCell = dataCell.substring(0, dataCell.lastIndexOf("/"));
                            if (dataCell.endsWith("\\"))
                                dataCell = dataCell.substring(0, dataCell.lastIndexOf("\\"));
                            if (dataCell.startsWith("/"))
                                dataCell = dataCell.substring(1, dataCell.length());
                            if (dataCell.startsWith("\\"))
                                dataCell = dataCell.substring(1, dataCell.length());
                            ruleConfig.setPath(dataCell);
                        }
                        if (MessageUtil.getResourceBundleMessage("label.import.colum.file.name.fix").toLowerCase().trim().equalsIgnoreCase(dynaProperty.getName().toLowerCase().trim())) {
                            dataCell = dataCell.trim();
                            if (lstFileExtension != null && lstFileExtension.size() > 0 && !isNullOrEmpty(lstFileExtension.get(0).getPropertyValue())) {
                                if (!lstFileExtension.get(0).getPropertyValue().contains(FilenameUtils.getExtension(dataCell))) {
                                    resultCode = "NOK";
                                    result = false;
                                    resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + MessageUtil.getResourceBundleMessage("validate.not.file.extension");
                                } else {
                                    ruleConfig.setFileName(dataCell);
                                }
                            } else {
                                ruleConfig.setFileName(dataCell);
                            }
                        }
                        if (MessageUtil.getResourceBundleMessage("label.import.colum.rule").toLowerCase().trim().equalsIgnoreCase(dynaProperty.getName().toLowerCase().trim())) {
                            dataCell = dataCell.trim();
                            if (!lstRuleEdit.contains(dataCell)) {
                                resultCode = "NOK";
                                result = false;
                                resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + MessageUtil.getResourceBundleMessage("validate.not.rule.config");
                            } else {
                                ruleConfig.setRuleEdit(dataCell);
                            }
                        }
                        if (MessageUtil.getResourceBundleMessage("label.import.colum.keyword").toLowerCase().trim().equalsIgnoreCase(dynaProperty.getName().toLowerCase().trim())) {
                            if (ruleConfig.getRuleEdit() != null
                                    && (!ruleConfig.getRuleEdit().equals(Constant.RuleConfig.RULE_ADD_ON_BOTTOM) && !ruleConfig.getRuleEdit().equals(Constant.RuleConfig.RULE_ADD_ON_HEAD))) {
                                if (dataCell == null || "".equals(dataCell)) {
                                    resultCode = "NOK";
                                    result = false;
                                    resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + (dynaProperty.getName() + " " + MessageUtil.getResourceBundleMessage("validate.not.file.empty"));
                                } else {
                                    if (dataCell.length() > 200) {
                                        resultCode = "NOK";
                                        result = false;
                                        resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + MessageUtil.getResourceBundleMessage("validate.not.key.max.length");
                                    } else {
                                        ruleConfig.setKeyword(dataCell);
                                    }
                                }
                            } else {
                                ruleConfig.setKeyword(dataCell == null || "".equals(dataCell) ? null : dataCell);
                            }
                        }
                        if (MessageUtil.getResourceBundleMessage("label.import.colum.content").toLowerCase().trim().equalsIgnoreCase(dynaProperty.getName().toLowerCase().trim())) {
                            if (ruleConfig.getRuleEdit() != null && !ruleConfig.getRuleEdit().equals(Constant.RuleConfig.RULE_DELETE_KEYWORD)) {
                                if (dataCell == null || "".equals(dataCell)) {
                                    resultCode = "NOK";
                                    result = false;
                                    resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + (dynaProperty.getName() + " " + MessageUtil.getResourceBundleMessage("validate.not.file.empty"));
                                } else {
                                    if (dataCell.length() > 1000) {
                                        resultCode = "NOK";
                                        result = false;
                                        resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + MessageUtil.getResourceBundleMessage("validate.not.content.max.length");
                                    } else {
                                        ruleConfig.setContent(dataCell);
                                    }
                                }
                            } else {
                                ruleConfig.setContent(dataCell);
                            }
                        }
                        mapParam.put(dynaProperty.getName(), dataCell);
                    }
                }

                if (result) {
                    //check duong dan tren server
                    String moduleCodeMap = ruleConfig.getModuleCode() + "#" + ruleConfig.getPath() + "#" + ruleConfig.getFileName();
                    if (mapBackupFile.containsKey(moduleCodeMap)) {
                        server = mapBackupFile.get(moduleCodeMap);
                        contentFile = server.getContentFile();
                    } else {
                        server = actionController.getFileToServer(mapModule.get(ruleConfig.getModuleCode()), mapModule.get(ruleConfig.getModuleCode()).getExecutePath() + "/" + ruleConfig.getPath(), ruleConfig.getFileName(), action);
                        if (server != null) {
                            contentFile = server.getContentFile();
                            if (contentFile == null) {
                                resultCode = "NOK";
                                result = false;
                                resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + MessageUtil.getResourceBundleMessage("validate.not.file.exist");
                            } else {
                                mapBackupFile.put(moduleCodeMap, server);
                            }
                        }
                    }
//                    }
                    //check trung ban ghi trong file

                    String keyRuleConfigMap = ruleConfig.getRuleEdit() + "#" + ruleConfig.getKeyword() + "#" + ruleConfig.getModuleCode() + "#" + ruleConfig.getPath() + "/" + ruleConfig.getFileName();
                    if (mapRuleConfig.containsKey(keyRuleConfigMap)) {
                        resultCode = "NOK";
                        result = false;
                        if (mapRuleConfig.get(keyRuleConfigMap).getActionDetailAppId() == null) {
                            resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + MessageUtil.getResourceBundleMessage("validate.not.exist.row");
                        } else {
                            resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + MessageUtil.getResourceBundleMessage("validate.not.exist.db");
                        }
                    } else {
                        mapRuleConfig.put(keyRuleConfigMap, ruleConfig);
                    }


                    //Lay file tren server ve execute xÃ†Â° ly tung rule
                    if (!"".equals(contentFile)) {
//                         String keyMap = ruleConfig.getKeyword() + "#" + ruleConfig.getModuleCode() + "#" + ruleConfig.getPath() + "#" + ruleConfig.getFileName();
                        if (mapExecuteFile.containsKey(moduleCodeMap)) {
                            if (!ruleConfig.getRuleEdit().equals(Constant.RuleConfig.RULE_ADD_ON_BOTTOM) && !ruleConfig.getRuleEdit().equals(Constant.RuleConfig.RULE_ADD_ON_HEAD)) {
                                if (!mapExecuteFile.get(moduleCodeMap).contains(ruleConfig.getKeyword())) {
                                    resultCode = "NOK";
                                    result = false;
                                    resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + MessageFormat.format(MessageUtil.getResourceBundleMessage("validate.not.file.not.key.because.del.replace"), ruleConfig.getKeyword());
                                } else {
                                    contentFile = ChangeConfigFileUtil.changeConfigByRule(mapExecuteFile.get(moduleCodeMap), ruleConfig);
                                    mapExecuteFile.put(moduleCodeMap, contentFile);
                                }
                            } else {
                                contentFile = ChangeConfigFileUtil.changeConfigByRule(mapExecuteFile.get(moduleCodeMap), ruleConfig);
                                mapExecuteFile.put(moduleCodeMap, contentFile);
                            }
                        } else {
                            mapExecuteFile.put(moduleCodeMap, contentFile);
                            if (!ruleConfig.getRuleEdit().equals(Constant.RuleConfig.RULE_ADD_ON_BOTTOM) && !ruleConfig.getRuleEdit().equals(Constant.RuleConfig.RULE_ADD_ON_HEAD)) {
                                if (!contentFile.contains(ruleConfig.getKeyword())) {
                                    resultCode = "NOK";
                                    result = false;
                                    resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + MessageFormat.format(MessageUtil.getResourceBundleMessage("validate.not.file.not.key.because.del.replace"), ruleConfig.getKeyword());
                                } else {
                                    contentFile = ChangeConfigFileUtil.changeConfigByRule(contentFile, ruleConfig);
                                    mapExecuteFile.put(moduleCodeMap, contentFile);
                                }
                            } else {
                                List<RuleConfig> rules = new ArrayList<>();
                                rules.add(ruleConfig);
                                contentFile = ChangeConfigFileUtil.changeConfigByRule(contentFile, ruleConfig);
                                mapExecuteFile.put(moduleCodeMap, contentFile);
                            }
                        }
                    } else {
                        resultCode = "NOK";
                        result = false;
                        resultDetail = (isNullOrEmpty(resultDetail) ? "" : (resultDetail + ";\n")) + MessageUtil.getResourceBundleMessage("validate.not.file.not.content");
                    }
                }

                if (result) {
                    ruleConfig.setPathFile(ruleConfig.getPath() + "/" + ruleConfig.getFileName());
                    ruleConfig.setActionId(action.getId());
                    ruleConfigList.add(ruleConfig);
                }
                mapParam.put("result_code", resultCode);
                mapParam.put("result_detail", resultDetail);
//                mapData.add(mapParam);
                mapData.put(entry.getKey(), mapParam);
            }

            if (!result) {
                resultDTO.setResultCode(1);
                resultDTO.setResultMessage(MessageUtil.getResourceBundleMessage("validate.not.file.fail"));
                resultDTO.setData("Error file");
            } else {
                //Neu thuc hien thanh cong cac buoc kia tien hanh luu file tren server AAM
                executeFileInImport(mapExecuteFile, lstUpcode, resultDTO, mapModule, mapBackupFile, mapActionDetailApp);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            ruleConfigList.clear();
            ruleConfigList.addAll(ruleConfigBackup);
            resultDTO.setResultCode(1);
            resultDTO.setResultMessage(e.getMessage());
        }
    }

    private void executeFileInImport(LinkedHashMap<String, String> mapExecuteFile, List<ActionDetailApp> lstUpcode, ResultDTO resultDTO, Map<String, Module> mapModule, Map<String, GetFileFromServer> mapBackupFile, Map<String, ActionDetailApp> mapActionDetailApp) {
        for (Map.Entry<String, String> entry : mapExecuteFile.entrySet()) {
            String moduleCode = entry.getKey().split("#")[0];
            String filePath = entry.getKey().split("#")[1];
            String filename = entry.getKey().split("#")[2];
            String upcodePath = filePath + "/" + filename;
            try {

                sourceCode = Util.convertUTF8ToNoSign(moduleCode + "_" + upcodePath.replaceAll("\\.\\.", "").replaceAll("/", "_").replaceAll("\\\\", "_") + "_" + filename.substring(0, filename.lastIndexOf(".")) + ".zip");
                String uploadFolder = UploadFileUtils.getSourceCodeFolder(action);
                fileWriter(uploadFolder + "/" + filename, entry.getValue());
                File file = new File(uploadFolder + "/" + filename);
                ZipUtils.zipDirectory(file, uploadFolder + "/" + sourceCode);
                InputStream inputStream = new FileInputStream(file);
                String newMd5 = DigestUtils.md5Hex(inputStream);
                inputStream.close();
                FileUtils.forceDelete(file);

                Module module = mapModule.get(moduleCode);
                if (mapActionDetailApp.containsKey(module.getModuleId() + "#" + upcodePath)) {
                    for (ActionDetailApp app : lstUpcode) {
                        if (app.getModuleId() == mapActionDetailApp.get(module.getModuleId() + "#" + upcodePath).getModuleId()
                                && app.getUpcodePath() == mapActionDetailApp.get(module.getModuleId() + "#" + upcodePath).getUpcodePath()) {
                            app.setBackupFilePath(mapBackupFile.get(entry.getKey()).getBackupFileName());
                        }
                    }
                } else {
                    ActionDetailApp detailApp = new ActionDetailApp();
                    detailApp.setGroupAction(AamConstants.RUN_STEP.STEP_UPCODE);
                    detailApp.setModuleId(module.getModuleId());
                    detailApp.setModuleOrder(1L);
                    detailApp.setIpServer(module.getIpServer());
                    detailApp.setMd5(newMd5);
                    detailApp.setUploadFilePath(sourceCode);
                    detailApp.setUpcodePath(upcodePath);
                    detailApp.setFile(true);
                    detailApp.setActionId(action.getId());
                    detailApp.setBackupFilePath(mapBackupFile.get(entry.getKey()).getBackupFileName());
                    detailApp.setRollbackStatus(0);
                    detailApp.setBeforeStatus(0);
                    detailApp.setAfterStatus(0);
                    detailApp.setIsAddRollback(0);
                    detailApp.setBackupStatus(0);
                    detailApp.setRunStatus(0);
                    detailApp.setModifyDate(new Date());
                    lstUpcode.add(detailApp);
                    mapActionDetailApp.put(module.getModuleId() + "#" + upcodePath, detailApp);
                }

                // Loai cac ban ghi chua chon file upload sau khi import thanh cong
                for (Iterator<ActionDetailApp> iter = lstUpcode.listIterator(); iter.hasNext(); ) {
                    ActionDetailApp item = iter.next();
                    if (module.getModuleId() != null && item.getModuleId() != null
                            && module.getModuleId().compareTo(item.getModuleId()) == 0
                            && (item.getUpcodePath() == null || "".equalsIgnoreCase(item.getUpcodePath()))
                            && (item.getUploadFilePath() == null || "".equalsIgnoreCase(item.getUploadFilePath()))) {
                        iter.remove();
                    }
                }
            } catch (IOException ioe) {
                logger.error(ioe.getMessage(), ioe);
                resultDTO.setResultCode(1);
                resultDTO.setResultMessage(ioe.getMessage());
            }
        }
    }

    private FileWriter fileWriter(String path, String content) {
        FileWriter writer = null;
        try {
            writer = new FileWriter(path);
            writer.write(content);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (writer != null) {
                try {
                    writer.close();
                } catch (IOException e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return writer;
    }

    private File exportFileResult(Workbook workbook, Map<Integer, LinkedHashMap<String, String>> mapData, int rowStart, String fileName) throws IOException, AppException {
        fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_" + fileName;
        String fileOut = CommonExport.getFolderSave() + "ResultFileImport" + File.separator + fileName;
        File fileOutResult = new File(fileOut);
        if (!fileOutResult.getParentFile().exists()) {
            fileOutResult.getParentFile().mkdirs();
        }
        fileOut = fileOutResult.getPath();
        try {
            if (workbook == null) {
                throw new NullPointerException();
            }
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
            cellStyle.setFillBackgroundColor(HSSFColor.YELLOW.index);
            cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
            Sheet sheet = null;

            sheet = workbook.getSheetAt(0);
//                int lastColumn = mapData.get(sheetName).get(0).size();
            //fix ket qua khong o cuoi cung
            int lastColumn = sheet.getRow(rowStart).getLastCellNum() + 2;
            Cell cellTitleCode = sheet.getRow(rowStart).createCell(lastColumn - 2);
            if (cellTitleCode != null) {
                cellTitleCode.setCellStyle(cellStyle);
                cellTitleCode.setCellValue("Result_code");
            }


            Cell cellTitleDetail = sheet.getRow(rowStart).createCell(lastColumn - 1);
            if (cellTitleDetail != null) {
                cellTitleDetail.setCellStyle(cellStyle);
                cellTitleDetail.setCellValue("Result_detail");
            }

            for (Map.Entry<Integer, LinkedHashMap<String, String>> entry : mapData.entrySet()) {
                logger.info("CellCode: " + entry.getValue().get("result_code"));
            }

            logger.info("mapData.size() " + mapData.size());
            logger.info("sheet.getLastRowNum(): " + sheet.getLastRowNum());
//            for (int i = rowStart; i < mapData.size(); i++) {
            for (Map.Entry<Integer, LinkedHashMap<String, String>> entry : mapData.entrySet()) {
                Cell cellCode = sheet.getRow(entry.getKey()).createCell(lastColumn - 2);
                if (cellCode != null) {
                    cellCode.setCellStyle(cellStyle);
                    cellCode.setCellValue(entry.getValue().get("result_code") == null ? "" : entry.getValue().get("result_code"));
                }
                Cell cellDetail = sheet.getRow(entry.getKey()).createCell(lastColumn - 1);
                if (cellDetail != null && cellStyle != null) {
                    cellDetail.setCellStyle(cellStyle);
                    cellDetail.setCellValue(entry.getValue().get("result_detail") == null ? "" : entry.getValue().get("result_detail"));
                }
            }
            try {
                FileOutputStream outputStream = new FileOutputStream(fileOut);
                workbook.write(outputStream);
                workbook.close();
            } catch (FileNotFoundException e) {
                logger.error(e.getMessage(), e);
                throw e;
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
                throw e;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw e;
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return fileOutResult;
    }

//    private HttpServletResponse preHeader() {
//        FacesContext facesContext = FacesContext.getCurrentInstance();
//        HttpServletResponse servletResponse = (HttpServletResponse) facesContext.getExternalContext().getResponse();
//        servletResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
//        servletResponse.setHeader("Expires", "0");
//        servletResponse.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
//        servletResponse.setHeader("Pragma", "public");
//        try {
//            servletResponse.setHeader("Content-disposition", "attachment;filename=" +
//                    URLEncoder.encode(MessageUtil.getResourceBundleMessage("key.template.file.import.rule.config").replace(".xlsx", "") + "_" + action.getCrName() + ".xlsx", "UTF-8"));
//        } catch (Exception e1) {
//            logger.error(e1.getMessage(), e1);
//        }
//        return servletResponse;
//    }
    //20190416_tudn_end import rule config

    public LazyDataModel<ActionDetailApp> getLazyDataModel() {
        return lazyDataModel;
    }

    public void setLazyDataModel(LazyDataModel<ActionDetailApp> lazyDataModel) {
        this.lazyDataModel = lazyDataModel;
    }

    public ActionDetailApp getSelectedObj() {
        return selectedObj;
    }

    public void setSelectedObj(ActionDetailApp selectedObj) {
        this.selectedObj = selectedObj;
    }

    public ActionDetailApp getNewObj() {
        return newObj;
    }

    public void setNewObj(ActionDetailApp newObj) {
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

    public Long getSearchModuleId() {
        return this.searchModuleId;
    }

    public void setSearchModuleId(Long searchModuleId) {
        this.searchModuleId = searchModuleId;
    }

    public String getSearchAction() {
        return this.searchAction;
    }

    public void setSearchAction(String searchAction) {
        this.searchAction = searchAction;
    }

    public Long getSearchModuleOrder() {
        return this.searchModuleOrder;
    }

    public void setSearchModuleOrder(Long searchModuleOrder) {
        this.searchModuleOrder = searchModuleOrder;
    }

    public String getSearchActionRollback() {
        return this.searchActionRollback;
    }

    public void setSearchActionRollback(String searchActionRollback) {
        this.searchActionRollback = searchActionRollback;
    }

    public String getSearchGroupAction() {
        return this.searchGroupAction;
    }

    public void setSearchGroupAction(String searchGroupAction) {
        this.searchGroupAction = searchGroupAction;
    }

    public String getSearchBackupPath() {
        return this.searchBackupPath;
    }

    public void setSearchBackupPath(String searchBackupPath) {
        this.searchBackupPath = searchBackupPath;
    }

    public String getSearchUpcodePath() {
        return this.searchUpcodePath;
    }

    public void setSearchUpcodePath(String searchUpcodePath) {
        this.searchUpcodePath = searchUpcodePath;
    }

    public String getSearchUploadFilePath() {
        return this.searchUploadFilePath;
    }

    public void setSearchUploadFilePath(String searchUploadFilePath) {
        this.searchUploadFilePath = searchUploadFilePath;
    }

    public String getSearchListFileCode() {
        return this.searchListFileCode;
    }

    public void setSearchListFileCode(String searchListFileCode) {
        this.searchListFileCode = searchListFileCode;
    }

    public String getSearchCheckCmd() {
        return this.searchCheckCmd;
    }

    public void setSearchCheckCmd(String searchCheckCmd) {
        this.searchCheckCmd = searchCheckCmd;
    }

    public String getSearchCheckCmdResult() {
        return this.searchCheckCmdResult;
    }

    public String getCheckCmdResult() {
        return checkCmdResult;
    }

    public void setCheckCmdResult(String checkCmdResult) {
        this.checkCmdResult = checkCmdResult;
    }

    public void setSearchCheckCmdResult(String searchCheckCmdResult) {
        this.searchCheckCmdResult = searchCheckCmdResult;
    }

    public List<ActionDetailApp> getListDetailsApp() {
        List<ActionDetailApp> detailApps = new ArrayList<>();
//		detailApps.addAll(lstBackup);
        detailApps.addAll(lstStop);
        detailApps.addAll(lstUpcode);
        detailApps.addAll(lstClearCache);
        detailApps.addAll(lstRestart);
        detailApps.addAll(lstStart);
        detailApps.addAll(lstUpcodeStart);
        detailApps.addAll(lstRestartCmd);
        /*20181030_hoangnd_save all step_start*/
        detailApps.addAll(lstCheckStatus);
        if (CollectionUtils.isNotEmpty(lstChecklistApp))
            detailApps.addAll(lstChecklistApp);
        if (CollectionUtils.isNotEmpty(lstChecklistDb))
            detailApps.addAll(lstChecklistDb);
        /*20181030_hoangnd_save all step_end*/
        return detailApps;
    }

/*	public void setListDetailsApp(List<ActionDetailApp> listDetailsApp) {
		this.listDetailsApp = listDetailsApp;
	}*/

    public Action getAction() {
        return action;
    }

    public void setAction(Action action) {
        this.action = action;
    }

/*    public Map<Long, Module> getMapApp() {
        return mapApp;
    }

    public void setMapApp(Map<Long, Module> mapApp) {
        this.mapApp = mapApp;
    }*/

    public ActionDetailAppService getActionDetailAppService() {
        return actionDetailAppService;
    }

    public String getSourceCode() {
        return sourceCode;
    }

    public String getListFile() {
        return listFile;
    }

    public void setSourceCode(String sourceCode) {
        this.sourceCode = sourceCode;
    }

    public void setListFile(String listFile) {
        this.listFile = listFile;
    }

    public List<Long> getListModuleSelected() {
        return listModuleSelected;
    }

    public void setListModuleSelected(List<Long> listModuleSelected) {
        this.listModuleSelected = listModuleSelected;
    }

    public List<ActionDetailApp> getLstBackup() {
        return lstBackup;
    }

    public void setLstBackup(List<ActionDetailApp> lstBackup) {
        this.lstBackup = lstBackup;
    }

    public List<ActionDetailApp> getLstStop() {
        return lstStop;
    }

    public void setLstStop(List<ActionDetailApp> lstStop) {
        this.lstStop = lstStop;
    }

    public List<ActionDetailApp> getLstUpcode() {
        return lstUpcode;
    }

    public void setLstUpcode(List<ActionDetailApp> lstUpcode) {
        this.lstUpcode = lstUpcode;
    }

    public List<ActionDetailApp> getLstRestart() {
        return lstRestart;
    }

    public void setLstRestart(List<ActionDetailApp> lstRestart) {
        this.lstRestart = lstRestart;
    }

    public List<ActionDetailApp> getLstClearCache() {
        return lstClearCache;
    }

    public void setLstClearCache(List<ActionDetailApp> lstClearCache) {
        this.lstClearCache = lstClearCache;
    }

    public List<ActionDetailApp> getLstStart() {
        return lstStart;
    }

    public void setLstStart(List<ActionDetailApp> lstStart) {
        this.lstStart = lstStart;
    }

    public List<ActionDetailApp> getLstUpcodeStart() {
        return lstUpcodeStart;
    }

    public void setLstUpcodeStart(List<ActionDetailApp> lstUpcodeStart) {
        this.lstUpcodeStart = lstUpcodeStart;
    }

    public ActionController getActionController() {
        return actionController;
    }

    public void setActionController(ActionController actionController) {
        this.actionController = actionController;
    }

    public TreeNode getUpcodeRoot() {
        return upcodeRoot;
    }

    public void setUpcodeRoot(TreeNode upcodeRoot) {
        this.upcodeRoot = upcodeRoot;
    }

    public TreeNode getSelectedUpcodeDir() {
        return selectedUpcodeDir;
    }

    public void setSelectedUpcodeDir(TreeNode selectedUpcodeDir) {
        this.selectedUpcodeDir = selectedUpcodeDir;
    }

    public List<ActionDetailApp> getFilteredLstStop() {
        return filteredLstStop;
    }

    public void setFilteredLstStop(List<ActionDetailApp> filteredLstStop) {
        this.filteredLstStop = filteredLstStop;
    }

    public List<ActionDetailApp> getFilteredLstStart() {
        return filteredLstStart;
    }

    public void setFilteredLstStart(List<ActionDetailApp> filteredLstStart) {
        this.filteredLstStart = filteredLstStart;
    }

    public List<ActionDetailApp> getFilteredLstRestart() {
        return filteredLstRestart;
    }

    public void setFilteredLstRestart(List<ActionDetailApp> filteredLstRestart) {
        this.filteredLstRestart = filteredLstRestart;
    }

    public List<ActionDetailApp> getLstRestartCmd() {
        return lstRestartCmd;
    }

    public void setLstRestartCmd(List<ActionDetailApp> lstRestartCmd) {
        this.lstRestartCmd = lstRestartCmd;
    }

    public List<ActionDetailApp> getFilteredLstRestartCmd() {
        return filteredLstRestartCmd;
    }

    public void setFilteredLstRestartCmd(List<ActionDetailApp> filteredLstRestartCmd) {
        this.filteredLstRestartCmd = filteredLstRestartCmd;
    }

    public List<ActionDetailApp> getFilteredBackup() {
        return filteredBackup;
    }

    public void setFilteredBackup(List<ActionDetailApp> filteredBackup) {
        this.filteredBackup = filteredBackup;
    }

    public List<ActionDetailApp> getFilteredUpcode() {
        return filteredUpcode;
    }

    public void setFilteredUpcode(List<ActionDetailApp> filteredUpcode) {
        this.filteredUpcode = filteredUpcode;
    }

    public List<ActionDetailApp> getFilteredUpcodeStart() {
        return filteredUpcodeStart;
    }

    public void setFilteredUpcodeStart(List<ActionDetailApp> filteredUpcodeStart) {
        this.filteredUpcodeStart = filteredUpcodeStart;
    }

    public List<ActionDetailApp> getFilteredClearcache() {
        return filteredClearcache;
    }

    public void setFilteredClearcache(List<ActionDetailApp> filteredClearcache) {
        this.filteredClearcache = filteredClearcache;
    }

    public TreeNode getSelectedRemoveUpcodeFile() {
        return selectedRemoveUpcodeFile;
    }

	/*public void setSelectedRemoveUpcodeFile(TreeNode selectedRemoveUpcodeFile) {
		this.selectedRemoveUpcodeFile = selectedRemoveUpcodeFile;
	}*/

    public void setSelectedRemoveUpcodeFile(TreeObject selectedRemoveUpcodeFile) {
        System.out.println(selectedRemoveUpcodeFile.getName());
    }

    public TreeNode getRemoveUpcodeRoot() {
        return removeUpcodeRoot;
    }

    public void setRemoveUpcodeRoot(TreeNode removeUpcodeRoot) {
        this.removeUpcodeRoot = removeUpcodeRoot;
    }

    public static void main(String[] args) {
        System.out.println(FilenameUtils.getFullPathNoEndSeparator("/u02/quanns2/tomcat_9191".replaceAll("/$", "")));
    }

    public void onNodeSelect(NodeSelectEvent event) {
        selectedRemoveUpcodeFile = event.getTreeNode();
		/*if (!event.isContextMenu()){
			System.out.println("ewgwegwegwe");
			selectedRemoveUpcodeFile = event.getTreeNode();
			//original code here.
		}*/
    }

    public DualListModel<Module> getDualListModel() {
        return dualListModel;
    }

    public void setDualListModel(DualListModel<Module> dualListModel) {
        this.dualListModel = dualListModel;
    }

    public List<Module> getSources() {
        return sources;
    }

    public void setSources(List<Module> sources) {
        this.sources = sources;
    }

    public List<Module> getTargets() {
        return targets;
    }

    public void setTargets(List<Module> targets) {
        this.targets = targets;
    }

    /*20181121_hoangnd_save all step_start*/
    public List<ActionDetailApp> getLstCheckStatus() {
        return lstCheckStatus;
    }

    public void setLstCheckStatus(List<ActionDetailApp> lstCheckStatus) {
        this.lstCheckStatus = lstCheckStatus;
    }

    public List<ActionDetailApp> getLstChecklistApp() {
        return lstChecklistApp;
    }

    public void setLstChecklistApp(List<ActionDetailApp> lstChecklistApp) {
        this.lstChecklistApp = lstChecklistApp;
    }

    public List<ActionDetailApp> getLstChecklistDb() {
        return lstChecklistDb;
    }

    public void setLstChecklistDb(List<ActionDetailApp> lstChecklistDb) {
        this.lstChecklistDb = lstChecklistDb;
    }
    /*20181121_hoangnd_save all step_end*/

    //20190416_tudn_start import rule config
    public StreamedContent getResultImport() {
        return resultImport;
    }

    public void setResultImport(StreamedContent resultImport) {
        this.resultImport = resultImport;
    }

    public String getTypeStep() {
        return typeStep;
    }

    public void setTypeStep(String typeStep) {
        this.typeStep = typeStep;
    }
    //20190416_tudn_end import rule config

    // 20190417_thenv_start change file config
    public boolean isShowChangeConfigButton() {
        return isShowChangeConfigButton;
    }

    public void setShowChangeConfigButton(boolean showChangeConfigButton) {
        isShowChangeConfigButton = showChangeConfigButton;
    }

    public List<RuleConfig> getRuleConfigList() {
        return ruleConfigList;
    }

    public void setRuleConfigList(List<RuleConfig> ruleConfigList) {
        this.ruleConfigList = ruleConfigList;
    }

    public List<RuleConfig> getLstRuleConfigNewObj() {
        return lstRuleConfigNewObj;
    }

    public void setLstRuleConfigNewObj(List<RuleConfig> lstRuleConfigNewObj) {
        this.lstRuleConfigNewObj = lstRuleConfigNewObj;
    }

    public String getFileContentOld() {
        return fileContentOld;
    }

    public void setFileContentOld(String fileContentOld) {
        this.fileContentOld = fileContentOld;
    }

    public String getFileContentNew() {
        return fileContentNew;
    }

    public void setFileContentNew(String fileContentNew) {
        this.fileContentNew = fileContentNew;
    }

    public List<String> getLstRuleEdit() {
        return lstRuleEdit;
    }

    public void setLstRuleEdit(List<String> lstRuleEdit) {
        this.lstRuleEdit = lstRuleEdit;
    }
    // 20190417_thenv_end change file config

    public boolean addAppImportDetail(TmplUpcodeDTO importObj, List<FileHeader> headers, String zipFolder) {
        if (lstUpcode == null)
            lstUpcode = new ArrayList<>();
        ActionDetailApp newImportObj = new ActionDetailApp();
        newImportObj.setUpcodePath(importObj.getUpcodePath());
        newImportObj.setUploadFilePath(importObj.getUploadFilePath());
        newImportObj.setLstFileRemove(importObj.getDeletePath());
        Module currentModule = null;

        //Tim moduleId
        Map<Long, Module> mapExample = actionController.getImpactModules();
        for(Map.Entry<Long, Module> pair : mapExample.entrySet()){
            if(pair.getValue().getModuleCode().equals(importObj.getModuleCode())) {
                newImportObj.setModuleId(pair.getValue().getModuleId());
                currentModule = pair.getValue();
                break;
            }
        }

        //Check zipfile existed
        boolean zipFileExisted = false;
        for (FileHeader header: headers
        ) {
            if(header.getFileName().contains(newImportObj.getUploadFilePath())) {
                zipFileExisted = true;
                File upFile = new File(zipFolder + "/" + header.getFileName());
                String uploadFolder = UploadFileUtils.getSourceCodeFolder(action);
                if (upFile != null) {
                    Module detail = actionController.getImpactModules().get(newImportObj.getModuleId());

                    if (detail == null)
                        return false;
                    sourceCode = Util.convertUTF8ToNoSign(detail.getModuleCode() + "_" + newImportObj.getUpcodePath().replaceAll("\\.\\.", "").replaceAll("/", "_").replaceAll("\\\\", "_") + "_" + header.getFileName().split("/")[header.getFileName().split("/").length - 1]);
                    FileHelper.uploadFileCode(uploadFolder, upFile, sourceCode);
                    newImportObj.setUploadFilePath(sourceCode);

                    try {
                        String newMd5 = DigestUtils.md5Hex(new FileInputStream(upFile));
                        newImportObj.setMd5(newMd5);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
                break;
            }
        }
        if(!zipFileExisted){
            importObj.setMessage(MessageUtil.getResourceBundleMessage("zip.file.not.existed"));
            importObj.setResult("NOK");
            return false;
        }

        if(currentModule != null) {
            boolean isExist = checkExistedUpcodePath(currentModule.getExecutePath(), currentModule, currentModule.getExecutePath() + "/" + newImportObj.getUpcodePath());
            if(!isExist)
            {
                importObj.setMessage(MessageUtil.getResourceBundleMessage("up.code.dir.not.existed"));
                importObj.setResult("NOK");
                return false;
            }
        }

        if(importObj.getDeletePath() != null && !"".equals(importObj.getDeletePath())){
            String[] listFileDelete = importObj.getDeletePath().split(",");
            for (String fileDel : listFileDelete
            ) {
                boolean isExist = checkExistedUpcodePath(currentModule.getExecutePath() + "/" + newImportObj.getUpcodePath(), currentModule, currentModule.getExecutePath() + "/" + fileDel.trim());
                if(!isExist)
                {
                    importObj.setMessage(MessageUtil.getResourceBundleMessage("delete.dir.not.existed") + " (" + fileDel + ")");
                    importObj.setResult("NOK");
                    return false;
                }
            }
        }

        if(newImportObj.getModuleId() == null){
            importObj.setMessage(MessageUtil.getResourceBundleMessage("module.up.code.not.existed"));
            importObj.setResult("NOK");
            return false;
        }

        if (StringUtils.isEmpty(newImportObj.getUploadFilePath())) {
            importObj.setMessage(MessageUtil.getResourceBundleMessage("file.code.have.not.uploaded.yet"));
            importObj.setResult("NOK");
            return false;
        }

        if (StringUtils.isEmpty(newImportObj.getUpcodePath())) {
            importObj.setMessage(MessageUtil.getResourceBundleMessage("folder.up.code.do.not.select"));
            importObj.setResult("NOK");
            return false;
        }

        Module detail = actionController.getImpactModules().get(newImportObj.getModuleId());
        if (("../" + FilenameUtils.getName(detail.getExecutePath().trim().replaceAll("/$", ""))).equals(newImportObj.getUpcodePath())) {
            importObj.setMessage(MessageUtil.getResourceBundleMessage("up.code.to.folder.install.is.forbidden"));
            importObj.setResult("NOK");
            return false;
        }

        String[] uploadFilePathArr = importObj.getUploadFilePath().split("/");
        String[] fileComponents = uploadFilePathArr[uploadFilePathArr.length - 1].split("\\.");
        String fileName = "";
        if(fileComponents.length >= 2){
            if(!fileComponents[fileComponents.length-1].toLowerCase().equals("zip")){
                importObj.setMessage(MessageUtil.getResourceBundleMessage("file.up.code.not.zipfile"));
                importObj.setResult("NOK");
                return false;
            }
            fileName = uploadFilePathArr[uploadFilePathArr.length - 1].replace("." + fileComponents[fileComponents.length-1], "");

        }
        else {
            importObj.setMessage(MessageUtil.getResourceBundleMessage("file.up.code.not.zipfile"));
            importObj.setResult("NOK");
            return false;
        }

        String[] upcodePathDirs = newImportObj.getUpcodePath().split("/");
        if (!upcodePathDirs[upcodePathDirs.length -1].equals(fileName)){
            importObj.setMessage(MessageUtil.getResourceBundleMessage("file.up.code.not.same.name.upcode.dir"));
            importObj.setResult("NOK");
            return false;
        }

        for (TmplUpcodeDTO item: templateUpcode) {
            if(item != importObj){
                if(item.getModuleCode() != null && item.getModuleCode().equals(importObj.getModuleCode())
                        && item.getUpcodePath() != null && item.getUpcodePath().equals(importObj.getUpcodePath())){
                    importObj.setMessage(MessageUtil.getResourceBundleMessage("folder.up.code.existed"));
                    importObj.setResult("NOK");
                    return false;
                }
            }else{
                break;
            }
        }

        for (ActionDetailApp detailApp : lstUpcode) {
            if (detailApp.getUpcodePath() != null && detailApp.getUpcodePath().equals(newImportObj.getUpcodePath())
                    && detailApp.getModuleId().equals(newImportObj.getModuleId()) &&
                    detailApp.getUpcodePath().equals(newImportObj.getUpcodePath())) {
                detailApp.setUploadFilePath(newImportObj.getUploadFilePath());
                detailApp.setUpcodePath(newImportObj.getUpcodePath());
                detailApp.setLstFileRemove(newImportObj.getLstFileRemove());
                detailApp.setModifyDate(new Date());
                importObj.setMessage("Success");
                importObj.setResult("OK");
                return true;
            }
            else if(detailApp.getUpcodePath() == null && detailApp.getModuleId().equals(newImportObj.getModuleId())){
                detailApp.setUploadFilePath(newImportObj.getUploadFilePath());
                detailApp.setUpcodePath(newImportObj.getUpcodePath());
                detailApp.setLstFileRemove(newImportObj.getLstFileRemove());
                detailApp.setModifyDate(new Date());
                importObj.setMessage("Success");
                importObj.setResult("OK");
                return true;
            }
        }

        importObj.setResult("OK");
        importObj.setMessage("Success");
        newImportObj.setModifyDate(new Date());
        lstUpcode.add(newImportObj);

        logger.info(dualListModel.getTarget().size());

        return true;
        //RequestContext.getCurrentInstance().execute("PF('editDialogApp').hide()");
    }


    /**
     * 2020-09-18 ThanhTD_ Download file template
     * **/
    public StreamedContent onDownloadUpcodeTemplate() {
        Workbook wb = null;
        try {
            ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            locate = languageBean.getLocaleCode();
            String templatePath = context.getRealPath("/") + "templates" + File.separator
                    + "import" + File.separator + (isShowNodeType ? (locate.equals("vi") ? "ImportUpcodeTemplate_vi.xlsx" : "ImportUpcodeTemplate_en.xlsx") : (locate.equals("vi") ? "ImportUpcodeTemplate_vi.xlsx" : "ImportUpcodeTemplate_en.xlsx"));

            wb = WorkbookFactory.create(new File(templatePath));
            Sheet sheet = wb.getSheetAt(0);

            ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                    .getExternalContext().getContext();
            String pathOut = ctx.getRealPath("/") + Config.PATH_OUT + (isShowNodeType ? "ImportUpcodeTemplate_vi.xlsx" : "ImportUpcodeTemplate_en.xlsx");

            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(pathOut);
                wb.write(fileOut);
                wb.close();
                return new DefaultStreamedContent(new FileInputStream(pathOut), ".xlsx", "Import_Upcode_Template_en.xlsx");
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



    /**
     * ThanhTD_Unikom 20200921
     **/
    List<TmplUpcodeDTO> templateUpcode;
    private boolean importSuccess = true;
    public boolean getImportSuccess()
    {
        return this.importSuccess;
    }

    public boolean checkExistedUpcodePath(String rootDir, Module module, String upcodePath) {
        ImpactProcess impactProcess = actionController.getNewObj().getImpactProcess();
        Client client = AamClientFactory.create(impactProcess.getUsername(), impactProcess.getPassword());

        WebTarget webTarget = client.target(impactProcess.getLink()).path("execute").path("lsdir").queryParam("curDir", rootDir).queryParam("countryCode", actionController.getNewObj().getCatCountryBO().getCountryCode()).queryParam("userTd", SessionUtil.getCurrentUsername());
        Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
        Response response = null;
        try {
            response = builder.post(Entity.json(module));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        if (response != null && response.getStatus() == Response.Status.OK.getStatusCode()) {
            List<Map.Entry<String, Boolean>> childFiles = response.readEntity(new GenericType<List<Map.Entry<String, Boolean>>>() {
            });
            for (Map.Entry<String, Boolean> childFile : childFiles) {
                if(childFile.getKey() != null) {
                    if(childFile.getKey().equals(upcodePath)){
                        return true;
                    }
                    if(upcodePath.contains(childFile.getKey())) {
                        return checkExistedUpcodePath(childFile.getKey(), module, upcodePath);
                    }
                }
            }
        }
        return false;
    }

    public StreamedContent onDownloadImportUpcodeResult() {
        Workbook wb = null;
        try {
            ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            locate = languageBean.getLocaleCode();
            String templatePath = context.getRealPath("/") + "templates" + File.separator
                    + "import" + File.separator + (isShowNodeType ? (locate.equals("vi") ? "ImportUpcodeResult_vi.xlsx" : "ImportUpcodeResult_en.xlsx") : (locate.equals("vi") ? "ImportUpcodeResult_vi.xlsx" : "ImportUpcodeResult_en.xlsx"));

            wb = WorkbookFactory.create(new File(templatePath));



            ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                    .getExternalContext().getContext();
            String pathOut = ctx.getRealPath("/") + Config.PATH_OUT + (isShowNodeType ? "ImportUpcodeResult_vi.xlsx" : "ImportUpcodeResult_en.xlsx");

            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(pathOut);
                Sheet sheet = wb.getSheetAt(0);


                int index = 1;
                for (TmplUpcodeDTO item : templateUpcode){
                    Row row = sheet.createRow(index);
                    Cell indexCell = row.createCell(0);
                    indexCell.setCellValue(index);
                    Cell moduleCodeCell = row.createCell(1);
                    moduleCodeCell.setCellValue(item.getModuleCode());
                    Cell moduleNameCell = row.createCell(2);
                    moduleNameCell.setCellValue(item.getModuleName());
                    Cell upcodePathCell = row.createCell(3);
                    upcodePathCell.setCellValue(item.getUpcodePath());
                    Cell uploadPathCell = row.createCell(4);
                    uploadPathCell.setCellValue(item.getUploadFilePath());
                    Cell deletePathCell = row.createCell(5);
                    deletePathCell.setCellValue(item.getDeletePath());
                    Cell resultCell = row.createCell(6);
                    resultCell.setCellValue(item.getResult());
                    Cell messageCell = row.createCell(7);
                    messageCell.setCellValue(item.getMessage());
                    index ++;
                }
                wb.write(fileOut);
                wb.close();

                return new DefaultStreamedContent(new FileInputStream(pathOut), ".xlsx", "Import_Upcode_Result_en.xlsx");
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

    public void onImportAppClick(){
        importSuccess = true;
        fileExcelUpcode = "";
        fileZipUpcode = "";
        nameFileZipUpcode = "";
        nameFileExcelUpcode = "";
    }

    private String fileExcelUpcode = "";
    private String nameFileExcelUpcode = "";
    public String getNameFileExcelUpcode(){
        return nameFileExcelUpcode;
    }
    public String getFileExcelUpcode(){
        return fileExcelUpcode;
    }
    public void handUploadFileExcelUpcode(FileUploadEvent event){

        UploadedFile file =  event.getFile();
        nameFileExcelUpcode = file.getFileName();
        //String uploadFolder = UploadFileUtils.getSourceCodeFolder(action);

        ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        locate = languageBean.getLocaleCode();
        String tempPath = context.getRealPath("/") + "TEMPORARY/UPCODE/" + actionController.getUsername() + "/EXCEL/";
        FileHelper.removeFile(tempPath);
        if (file != null) {
            listFile = Util.convertUTF8ToNoSign(getNameFile(file.getFileName())) + getFileExtension(file.getFileName());
            FileHelper.uploadFile(tempPath, file, listFile);
            fileExcelUpcode = tempPath + listFile;
        }
    }
    private String fileZipUpcode = "";
    private String nameFileZipUpcode = "";
    public String getNameFileZipUpcode(){
        return nameFileZipUpcode;
    }
    public String getFileZipUpcode(){
        return fileZipUpcode;
    }
    public void handUploadFileZipUpcode(FileUploadEvent event){

        UploadedFile file =  event.getFile();
        nameFileZipUpcode = file.getFileName();
        //String uploadFolder = UploadFileUtils.getSourceCodeFolder(action);

        ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        locate = languageBean.getLocaleCode();
        String tempPath = context.getRealPath("/") + "TEMPORARY/UPCODE/" + actionController.getUsername() + "/ZIP/";
        FileHelper.removeFile(tempPath);
        if (file != null) {
            listFile = Util.convertUTF8ToNoSign(getNameFile(file.getFileName())) + getFileExtension(file.getFileName());
            FileHelper.uploadFile(tempPath, file, listFile);
            fileZipUpcode = tempPath + listFile;
        }
    }
    public void importFileDataUpcode(){
        if("".equals(fileZipUpcode) || "".equals(fileExcelUpcode)){
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    MessageUtil.getResourceBundleMessage("file.excel.or.zip.not.upload.yet")));
            return;
        }
        ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
        String extractPath = context.getRealPath("/") + "TEMPORARY/UPCODE/" + actionController.getUsername() + "/ZIP/" + "EXTRACT/";
        File fileZip = new File(fileZipUpcode);


        if(fileZip == null) return;

        List<FileHeader> headers = null;
        try {
            ZipFile zipFile = new ZipFile(fileZip.getPath());
            headers = zipFile.getFileHeaders();
            zipFile.extractAll(extractPath);
        }catch (Exception ex){

        }

        try{
            importSuccess = true;
            Workbook wb = WorkbookFactory.create(new FileInputStream(new File(fileExcelUpcode)));
            Sheet sheet = wb.getSheetAt(0);

            Row row;
            DataFormatter format = new DataFormatter();
            templateUpcode = new ArrayList<>();
            row = sheet.getRow(0);
            if(format.formatCellValue(row.getCell(0)) == null || format.formatCellValue(row.getCell(1))== null
                    ||format.formatCellValue(row.getCell(2))== null||format.formatCellValue(row.getCell(3))== null
                    ||format.formatCellValue(row.getCell(4))== null||format.formatCellValue(row.getCell(5))== null){
                MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                        MessageUtil.getResourceBundleMessage("upload.file.application.wrong.format")));
            }

            for(int i=1 ; i<= sheet.getLastRowNum(); i++) {
                row = sheet.getRow(i);
                TmplUpcodeDTO tmplUpcodeDTO = new TmplUpcodeDTO();
                tmplUpcodeDTO.setStt(Integer.parseInt(format.formatCellValue(row.getCell(0))));
                tmplUpcodeDTO.setModuleCode(format.formatCellValue(row.getCell(1)));
                tmplUpcodeDTO.setModuleName(format.formatCellValue(row.getCell(2)));
                tmplUpcodeDTO.setUpcodePath(format.formatCellValue(row.getCell(3)));
                tmplUpcodeDTO.setUploadFilePath(format.formatCellValue(row.getCell(4)));
                tmplUpcodeDTO.setDeletePath(format.formatCellValue(row.getCell(5)));
                templateUpcode.add(tmplUpcodeDTO);
            }
            for (TmplUpcodeDTO tmpUpcodeDto: templateUpcode
            ) {
                if(!addAppImportDetail(tmpUpcodeDto, headers, extractPath))
                {
                    importSuccess = false;
                    break;
                }
            }
            if(importSuccess) {
                MessageUtil.setInfoMessageFromRes("import.application.success");
                RequestContext.getCurrentInstance().execute("PF('importDialog').hide()");
            }
            else{
                MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                        MessageUtil.getResourceBundleMessage("upload.impact.application.error")));
                //return;
            }
        }catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    MessageUtil.getResourceBundleMessage("upload.impact.crash")));
        }

    }
}

/***********************************************************************************************************************************/