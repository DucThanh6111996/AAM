package com.viettel.it.controller;


import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.CommandBlacklist;
import com.viettel.it.model.NodeType;
import com.viettel.it.model.Vendor;
import com.viettel.it.model.Version;
import com.viettel.it.object.ComboBoxObject;
import com.viettel.it.persistence.CommandBlacklistServiceImpl;
import com.viettel.it.persistence.NodeTypeServiceImpl;
import com.viettel.it.persistence.VendorServiceImpl;
import com.viettel.it.persistence.VersionServiceImpl;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.it.util.ParamUtil;
import com.viettel.it.util.Util;
import com.viettel.util.SessionUtil;
import com.viettel.util.SessionWrapper;
import org.apache.commons.beanutils.BeanUtils;
import org.apache.log4j.Logger;
import org.primefaces.context.RequestContext;
import org.primefaces.event.ToggleEvent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.Visibility;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 Created by VTN-PTPM-NV36
 */
@ViewScoped
@ManagedBean
public class CommandBlacklistController {
    protected static final Logger logger = Logger.getLogger(CommandBlacklistController.class);

    @ManagedProperty(value = "#{commandBlacklistService}")
    private CommandBlacklistServiceImpl commandBlacklistService;
    @ManagedProperty(value = "#{vendorService}")
    private VendorServiceImpl vendorService;
    @ManagedProperty(value = "#{nodeTypeService}")
    private NodeTypeServiceImpl nodeTypeService;
    @ManagedProperty(value = "#{versionService}")
    private VersionServiceImpl versionService;

    private LazyDataModel<CommandBlacklist> lazyDataModel;
    private String logAction = "";
    private String className = CommandBlacklistController.class.getName();
    private CommandBlacklist commandBlacklist;
    private List<CommandBlacklist> commandBlacklists;
    private boolean isUpdate = false;

    private List<Vendor> vendors;
    private List<NodeType> nodeTypes;
    private List<Version> versions;
    private List<ComboBoxObject> operators;

    private List<Vendor> vendorSeleteds;
    private List<NodeType> nodeTypeSelecteds;
    private List<Version> versionSelecteds;

    private Vendor vendorSelected;
    private NodeType nodeTypeSelected;
    private Version versionSelected;
    ;
    private ComboBoxObject opeSelected;

    private boolean standardValueRequired;
    private boolean standardValueReadonly;
    private String standardValuePrompt;

    private List<Boolean> columnVisibale =new ArrayList<>();

    public void onToggler(ToggleEvent e){
        this.columnVisibale.set((Integer) e.getData(),e.getVisibility()== Visibility.VISIBLE);
    }

    @PostConstruct
    public void onStart() {
        try {

            commandBlacklist = new CommandBlacklist();

            vendorSeleteds = new ArrayList<>();
            nodeTypeSelecteds = new ArrayList<>();
            versionSelecteds = new ArrayList<>();

            Map<String, String> orderVendor = new HashMap<>();
            orderVendor.put("vendorName", "asc");
            vendors = vendorService.findList(null, orderVendor);

            Map<String, String> orderNodeType = new HashMap<>();
            orderNodeType.put("typeName", "asc");
            nodeTypes = nodeTypeService.findList(null, orderNodeType);

            Map<String, String> orderVersion = new HashMap<>();
            orderVersion.put("versionName", "asc");
            versions = versionService.findList(null, orderVersion);

            operators = new ArrayList<>();
            operators.add(new ComboBoxObject("CONTAIN", "CONTAIN"));
            operators.add(new ComboBoxObject("CONTAIN ALL", "CONTAIN ALL"));
            operators.add(new ComboBoxObject("NOT CONTAIN", "NOT CONTAIN"));
            operators.add(new ComboBoxObject("IS NULL", "IS NULL"));
            operators.add(new ComboBoxObject("NOT NULL", "NOT NULL"));
            operators.add(new ComboBoxObject("=", "="));
            operators.add(new ComboBoxObject("<", "<"));
            operators.add(new ComboBoxObject("<=", "<="));
            operators.add(new ComboBoxObject(">", ">"));
            operators.add(new ComboBoxObject(">=", ">="));
            operators.add(new ComboBoxObject("<>", "<>"));
            operators.add(new ComboBoxObject("IN", "IN"));
            operators.add(new ComboBoxObject("BETWEEN", "BETWEEN"));
            operators.add(new ComboBoxObject("LIKE", "LIKE"));
            operators.add(new ComboBoxObject("NOT LIKE", "NOT LIKE"));
            operators.add(new ComboBoxObject("NOT IN", "NOT IN"));
            operators.add(new ComboBoxObject("IS NULL OR CONTAIN", "IS NULL OR CONTAIN"));

            Collections.sort(operators, new Comparator<ComboBoxObject>() {
                @Override
                public int compare(ComboBoxObject a, ComboBoxObject b) {
                    return a.getLabel().compareTo(b.getLabel());
                }
            });

            commandBlacklistService = new CommandBlacklistServiceImpl();
            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            orders.put("updateTime", "DESC");

            lazyDataModel = new LazyDataModelBaseNew<>(commandBlacklistService, null, orders);
            this.columnVisibale =new ArrayList<Boolean>(){
                private static final long serialVersionUID = 1L;
                {
                    add(Boolean.TRUE);
                    add(Boolean.TRUE);
                    add(Boolean.TRUE);
                    add(Boolean.TRUE);
                    add(Boolean.TRUE);
                    add(Boolean.TRUE);
                    add(Boolean.TRUE);
                    add(Boolean.TRUE);
                    add(Boolean.TRUE);
                    add(Boolean.TRUE);
                    add(Boolean.TRUE);
                    add(Boolean.TRUE);
                }
            };
            logAction = LogUtils.addContent("", "Login Function");
            LinkedHashMap<String, String> order = new LinkedHashMap<>();
            order.put("updateTime", "ASC");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        LogUtils.writelog(new Date(), className, new Object() {
        }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.VIEW.name(), logAction);
    }

    public void preAddCmdBlacklist() {
        try {
            isUpdate = false;
            clear();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void prepareEdit(CommandBlacklist vCmd) {
        logAction = LogUtils.addContent("", "Prepare Edit command");
        try {
            clear();
            if (vCmd == null) {
                return;
            }
            commandBlacklist = vCmd;
            logAction = LogUtils.addContent(logAction, "Command Old: " + commandBlacklist.toString());

            CommandBlacklist oldObj = (CommandBlacklist) BeanUtils.cloneBean(vCmd);//new Cloner().deepClone(vCmd);
            oldObj.setVendor((Vendor) BeanUtils.cloneBean(vCmd.getVendor()));
            oldObj.setVersion((Version) BeanUtils.cloneBean(vCmd.getVersion()));
            oldObj.setNodeType((NodeType) BeanUtils.cloneBean(vCmd.getNodeType()));
            isUpdate = true;
            vendorSelected = vCmd.getVendor();
            nodeTypeSelected = vCmd.getNodeType();
            versionSelected = vCmd.getVersion();
            opeSelected = new ComboBoxObject(vCmd.getOperator(), vCmd.getOperator());

            onChangeOperator(vCmd.getOperator());

            logAction = LogUtils.addContent(logAction, "Result: Sucsses ");
            LogUtils.writelog(new Date(), className, new Object() {
            }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.UPDATE.name(), logAction);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logAction = LogUtils.addContent(logAction, "Result Fail :" + e.getMessage());
            LogUtils.writelog(new Date(), className, new Object() {
            }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.UPDATE.name(), logAction);
        }
    }

    public void prepareDelete(CommandBlacklist vCmd) {
        try {
            logAction = LogUtils.addContent("", "Prepare Delete command");

            commandBlacklist = vCmd;
            logAction = LogUtils.addContent(logAction, "Command Blacklist delete: " + commandBlacklist.toString());
            LogUtils.writelog(new Date(), className, new Object() {
            }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.DELETE.name(), logAction);
        } catch (Exception e) {
            logAction = LogUtils.addContent(logAction, "Result fail: " + e.getMessage());
            LogUtils.writelog(new Date(), className, new Object() {
            }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.DELETE.name(), logAction);
            logger.error(e.getMessage(), e);
        }
    }

    public void onDelete() {
        try {
            Date startTime = new Date();
            if (commandBlacklist == null) {
                MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("message.choose.delete"));
                return;
            }
            commandBlacklistService.delete(commandBlacklist);

            MessageUtil.setInfoMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.success"),
                    MessageUtil.getResourceBundleMessage("title.deleteCommandBlacklist")));
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), CommandBlacklistController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.DELETE,
                        commandBlacklist.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    MessageUtil.getResourceBundleMessage("title.deleteCommandBlacklist")));
        }
    }

    public void onSaveOrUpdateCmdBlacklist() {
        try {
            if (!validateInput(isUpdate)) {
                return;
            }

            Date startTime = new Date();
            //check ban ghi trung nhau tat ca moi thu
            Map<String, Object> filters = new HashMap<>();
//            if (commandBlacklist.getCmdRegex() != null && !"".equals(commandBlacklist.getCmdRegex().trim()))
                filters.put("cmdRegex-EXAC", commandBlacklist.getCmdRegex() == null ? "" : commandBlacklist.getCmdRegex().trim());
            if (opeSelected != null)
                filters.put("operator-EXAC", opeSelected == null ? "" : opeSelected.getValue());
//            if (commandBlacklist.getStandardValue() != null && !"".equals(commandBlacklist.getStandardValue().trim()))
                filters.put("standardValue-EXAC", commandBlacklist.getStandardValue() == null ? "" : commandBlacklist.getStandardValue().trim());
            if (isUpdate) {
                filters.put("vendor.vendorId", vendorSelected.getVendorId());
                filters.put("version.versionId", versionSelected.getVersionId());
                filters.put("nodeType.typeId", nodeTypeSelected.getTypeId());
                filters.put("commandBlacklistId-NEQ", commandBlacklist.getCommandBlacklistId());
            } else {
                filters.put("vendor.vendorId", vendorSeleteds.get(0).getVendorId());
                filters.put("version.versionId", versionSelecteds.get(0).getVersionId());
                filters.put("nodeType.typeId", nodeTypeSelecteds.get(0).getTypeId());
            }
            List<CommandBlacklist> lstCommandBlacklists = commandBlacklistService.findList(filters);
            if (lstCommandBlacklists != null && lstCommandBlacklists.size() > 0) {
                MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("error.duplicate.statement"));
                return;
            }
            //ket thuc check trung

            if (isUpdate) {
                logAction = LogUtils.addContent("", "Edit command");

                CommandBlacklist cmdBlacklist = new CommandBlacklist();
                if (commandBlacklist.getCommandBlacklistId() != null) {
                    cmdBlacklist = commandBlacklistService.findById(commandBlacklist.getCommandBlacklistId());
                    if (cmdBlacklist == null) {

                    }
                    logAction = LogUtils.addContent(logAction, "Command Old: " + cmdBlacklist.toString());
                }
                logAction = LogUtils.addContent(logAction, "Command New: " + commandBlacklist.toString());

                cmdBlacklist.setVendor(vendorSelected);
                cmdBlacklist.setVersion(versionSelected);
                cmdBlacklist.setNodeType(nodeTypeSelected);
                cmdBlacklist.setCmdRegex(commandBlacklist.getCmdRegex() == null ? "" : commandBlacklist.getCmdRegex().trim());
                cmdBlacklist.setOperator(opeSelected == null ? "" : opeSelected.getValue());
                cmdBlacklist.setStandardValue(commandBlacklist.getStandardValue() == null ? "" : commandBlacklist.getStandardValue().trim());
                cmdBlacklist.setLastUpdateBy(SessionWrapper.getCurrentUsername());
                cmdBlacklist.setUpdateTime(new Date());

                commandBlacklistService.saveOrUpdate(cmdBlacklist);

                logAction = LogUtils.addContent(logAction, "Result: " + MessageFormat.format(MessageUtil.getResourceBundleMessage("common.success"),
                        MessageUtil.getResourceBundleMessage("title.updateCommand")));
                MessageUtil.setInfoMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.success"),
                        MessageUtil.getResourceBundleMessage("title.updateCommand")));
                LogUtils.writelog(new Date(), className, new Object() {
                }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.UPDATE.name(), logAction);
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), CommandBlacklistController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.UPDATE,
                            cmdBlacklist.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } else {
                logAction = LogUtils.addContent("", "Add command");
                logAction = LogUtils.addContent(logAction, "Command save: " + commandBlacklist.toString());

                for (NodeType node : nodeTypeSelecteds) {
                    for (Vendor vendor : vendorSeleteds) {
                        for (Version version : versionSelecteds) {
                            CommandBlacklist cmdBlacklist = new CommandBlacklist();
                            cmdBlacklist.setVendor(vendor);
                            cmdBlacklist.setVersion(version);
                            cmdBlacklist.setNodeType(node);
                            cmdBlacklist.setCmdRegex(commandBlacklist.getCmdRegex() == null ? "" : commandBlacklist.getCmdRegex().trim());
                            cmdBlacklist.setOperator(opeSelected == null ? "" : opeSelected.getValue());
                            cmdBlacklist.setStandardValue(commandBlacklist.getStandardValue() == null ? "" : commandBlacklist.getStandardValue().trim());
                            cmdBlacklist.setCreatedBy(SessionWrapper.getCurrentUsername());
                            cmdBlacklist.setCreateTime(new Date());
                            cmdBlacklist.setLastUpdateBy(SessionWrapper.getCurrentUsername());
                            cmdBlacklist.setUpdateTime(new Date());

                            commandBlacklistService.saveOrUpdate(cmdBlacklist);

                            try {
                                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), CommandBlacklistController.class.getName(),
                                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                                        LogUtils.ActionType.CREATE,
                                        cmdBlacklist.toString(), LogUtils.getRequestSessionId());
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                }

                MessageUtil.setInfoMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.success"),
                        MessageUtil.getResourceBundleMessage("title.insertCommand")));
                logAction = LogUtils.addContent(logAction, "Result: " + MessageFormat.format(MessageUtil.getResourceBundleMessage("common.success"),
                        MessageUtil.getResourceBundleMessage("title.insertCommand")));
                LogUtils.writelog(new Date(), className, new Object() {
                }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.CREATE.name(), logAction);

            }

            commandBlacklist = new CommandBlacklist();
            RequestContext.getCurrentInstance().execute("PF('addCmdBlacklist').hide();");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    isUpdate ? MessageUtil.getResourceBundleMessage("title.updateCommand") : MessageUtil.getResourceBundleMessage("title.insertCommand")));
            logAction = LogUtils.addContent(logAction, "Result: " + MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    isUpdate ? MessageUtil.getResourceBundleMessage("title.updateCommand") : MessageUtil.getResourceBundleMessage("title.insertCommand")));
            LogUtils.writelog(new Date(), className, new Object() {
            }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.UPDATE.name(), logAction);
        }
    }

    private boolean validateInput(boolean isEdit) {
        if ((vendorSelected == null && isEdit) || (!isEdit && (vendorSeleteds == null || vendorSeleteds.isEmpty()))) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                    MessageUtil.getResourceBundleMessage("label.vendorName")));
            return false;
        }

        if ((nodeTypeSelected == null && isEdit) || (!isEdit && (nodeTypeSelecteds == null || nodeTypeSelecteds.isEmpty()))) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                    MessageUtil.getResourceBundleMessage("label.nodeTypeName")));
            return false;
        }

        if ((versionSelected == null && isEdit) || (!isEdit && (versionSelecteds == null || versionSelecteds.isEmpty()))) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                    MessageUtil.getResourceBundleMessage("label.versionName")));
            return false;
        }

        if (opeSelected == null) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                    MessageUtil.getResourceBundleMessage("label.operator")));
            return false;
        }

        if (opeSelected != null) {
            switch (opeSelected.getValue().toUpperCase()) {
                case "IN":
                case "NOT IN":
                case "=":
                case "<":
                case ">":
                case "<=":
                case ">=":
                case "<>":
                    if (Util.isNullOrEmpty(commandBlacklist.getStandardValue())) {
                        MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                                MessageUtil.getResourceBundleMessage("label.standardValue")));
                        return false;
                    }
                    break;
                case "CONTAIN":
                case "IS NULL OR CONTAIN":
                case "NOT CONTAIN":
                    if (Util.isNullOrEmpty(commandBlacklist.getStandardValue())) {
                        MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                                MessageUtil.getResourceBundleMessage("label.standardValue")));
                        return false;
                    }
                    break;
                case "BETWEEN":
                    if (Util.isNullOrEmpty(commandBlacklist.getStandardValue())) {
                        MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                                MessageUtil.getResourceBundleMessage("label.standardValue")));
                        return false;
                    }

                    if (!commandBlacklist.getStandardValue().trim().contains(",")) {
                        MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("label.standardValue")
                                + MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.between"));
                        return false;
                    }

                    String[] str = commandBlacklist.getStandardValue().trim().split(",");
                    if (str.length != 2) {
                        MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("label.standardValue")
                                + MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.between"));
                        return false;
                    }

                    try {
                        System.out.println(Double.parseDouble(str[0]));
                        System.out.println(Double.parseDouble(str[1]));
                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                        MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("label.standardValue")
                                + MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.between"));
                        return false;
                    }
                    break;
                case "LIKE":
                case "NOT LIKE":
                    if (Util.isNullOrEmpty(commandBlacklist.getStandardValue())) {
                        MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                                MessageUtil.getResourceBundleMessage("label.standardValue")));
                        return false;
                    }
                    if (!commandBlacklist.getStandardValue().trim().contains("%")) {
                        MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("label.standardValue")
                                + MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.like"));
                        return false;
                    }
                    break;
            }
        }

        return true;
    }

    private void onChangeOperator(String value) {
        switch (value) {
            case "IS NULL":
            case "NOT NULL":
            case "CONTAIN":
            case "IS NULL OR CONTAIN":
            case "NOT CONTAIN":
            case "=":
            case "<":
            case ">":
            case ">=":
            case "<=":
            case "<>":
                standardValueReadonly = false;
                standardValueRequired = true;
                break;
            case "IN":
            case "NOT IN":
                standardValueReadonly = false;
                standardValueRequired = true;
                standardValuePrompt = MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.in");
                break;
            case "BETWEEN":
                standardValueReadonly = false;
                standardValueRequired = true;
                standardValuePrompt = MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.between");
                break;
            case "LIKE":
            case "NOT LIKE":
                standardValueReadonly = false;
                standardValueRequired = true;
                standardValuePrompt = MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.like");
                break;
            default:
                standardValueReadonly = false;
                standardValueRequired = false;
                standardValuePrompt = "";
                commandBlacklist.setStandardValue("");
                break;
        }
    }

    public void onChangeOperator() {
        try {
            String operator = opeSelected == null ? "" : opeSelected.getValue();

            onChangeOperator(operator);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private void clear() {
        commandBlacklist = new CommandBlacklist();

        if (vendorSeleteds != null) {
            vendorSeleteds.clear();
        }
        if (nodeTypeSelecteds != null) {
            nodeTypeSelecteds.clear();
        }
        if (versionSelecteds != null) {
            versionSelecteds.clear();
        }
        vendorSelected = null;
        nodeTypeSelected = null;
        versionSelected = null;
        opeSelected = null;

        standardValuePrompt = "";
        standardValueReadonly = false;
        standardValueRequired = false;
    }

    // return true: la cmd blacklist, false: ko phai
    public boolean checkCommandBlacklist(String cmd, List<CommandBlacklist> lstCmdBL) {
        logAction = "";
        try {
            if (!Util.isNullOrEmpty(cmd)) {
                for (CommandBlacklist cbl : lstCmdBL) {
                    if(Util.isNullOrEmpty(cbl.getOperator())) {
                        return true;
                    }

                    if (!Util.isNullOrEmpty(cbl.getCmdRegex())) {
                        // Cat chuoi Regex
//                        Pattern patRegex = Pattern.compile(cbl.getCmdRegex());
//                        Matcher matcher = patRegex.matcher(cmd.trim());
//                        while (matcher.find()) {
//                            // check operator
//                            String value = matcher.group();
//                            if (Util.checkValueOperator(value, cbl.getStandardValue(), cbl.getOperator())) {
//                                return true;
//                            }
//                        }
                        List<String> vls = ParamUtil.getValuesRegex(cmd.trim(), cbl.getCmdRegex());
                        for (String vl : vls) {
                            if (!Util.isNullOrEmpty(vl) && Util.checkValueOperator(vl, cbl.getStandardValue(), cbl.getOperator())) {
                                return true;
                            }
                        }
                    } else {
                        if (Util.checkValueOperator(cmd, cbl.getStandardValue(), cbl.getOperator())) {
                            return true;
                        }
                    }
                }
            }
        } catch (Exception ex) {
            logAction = LogUtils.addContent(logAction, "Exception checkCommandBlacklist: " + ex.getMessage());
            logger.error(ex.getMessage(), ex);
            return true;
        }

        LogUtils.writelog(new Date(), className, new Object() {
        }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.CREATE.name(), logAction);
        return false;
    }

    public boolean isUpdate() {
        return isUpdate;
    }

    public void setUpdate(boolean update) {
        isUpdate = update;
    }

    public LazyDataModel<CommandBlacklist> getLazyDataModel() {
        return lazyDataModel;
    }

    public void setLazyDataModel(LazyDataModel<CommandBlacklist> lazyDataModel) {
        this.lazyDataModel = lazyDataModel;
    }

    public CommandBlacklistServiceImpl getCommandBlacklistService() {
        return commandBlacklistService;
    }

    public void setCommandBlacklistService(CommandBlacklistServiceImpl commandBlacklistService) {
        this.commandBlacklistService = commandBlacklistService;
    }

    public String getLogAction() {
        return logAction;
    }

    public void setLogAction(String logAction) {
        this.logAction = logAction;
    }

    public String getClassName() {
        return className;
    }

    public void setClassName(String className) {
        this.className = className;
    }

    public CommandBlacklist getCommandBlacklist() {
        return commandBlacklist;
    }

    public void setCommandBlacklist(CommandBlacklist commandBlacklist) {
        this.commandBlacklist = commandBlacklist;
    }

    public List<CommandBlacklist> getCommandBlacklists() {
        return commandBlacklists;
    }

    public void setCommandBlacklists(List<CommandBlacklist> commandBlacklists) {
        this.commandBlacklists = commandBlacklists;
    }

    public List<Vendor> getVendors() {
        return vendors;
    }

    public void setVendors(List<Vendor> vendors) {
        this.vendors = vendors;
    }

    public List<NodeType> getNodeTypes() {
        return nodeTypes;
    }

    public void setNodeTypes(List<NodeType> nodeTypes) {
        this.nodeTypes = nodeTypes;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }

    public List<Vendor> getVendorSeleteds() {
        return vendorSeleteds;
    }

    public void setVendorSeleteds(List<Vendor> vendorSeleteds) {
        this.vendorSeleteds = vendorSeleteds;
    }

    public List<NodeType> getNodeTypeSelecteds() {
        return nodeTypeSelecteds;
    }

    public void setNodeTypeSelecteds(List<NodeType> nodeTypeSelecteds) {
        this.nodeTypeSelecteds = nodeTypeSelecteds;
    }

    public List<Version> getVersionSelecteds() {
        return versionSelecteds;
    }

    public void setVersionSelecteds(List<Version> versionSelecteds) {
        this.versionSelecteds = versionSelecteds;
    }

    public List<ComboBoxObject> getOperators() {
        return operators;
    }

    public void setOperators(List<ComboBoxObject> operators) {
        this.operators = operators;
    }

    public Vendor getVendorSelected() {
        return vendorSelected;
    }

    public void setVendorSelected(Vendor vendorSelected) {
        this.vendorSelected = vendorSelected;
    }

    public NodeType getNodeTypeSelected() {
        return nodeTypeSelected;
    }

    public void setNodeTypeSelected(NodeType nodeTypeSelected) {
        this.nodeTypeSelected = nodeTypeSelected;
    }

    public Version getVersionSelected() {
        return versionSelected;
    }

    public void setVersionSelected(Version versionSelected) {
        this.versionSelected = versionSelected;
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

    public ComboBoxObject getOpeSelected() {
        return opeSelected;
    }

    public void setOpeSelected(ComboBoxObject opeSelected) {
        this.opeSelected = opeSelected;
    }

    public boolean isStandardValueRequired() {
        return standardValueRequired;
    }

    public void setStandardValueRequired(boolean standardValueRequired) {
        this.standardValueRequired = standardValueRequired;
    }

    public boolean isStandardValueReadonly() {
        return standardValueReadonly;
    }

    public void setStandardValueReadonly(boolean standardValueReadonly) {
        this.standardValueReadonly = standardValueReadonly;
    }

    public String getStandardValuePrompt() {
        return standardValuePrompt;
    }

    public void setStandardValuePrompt(String standardValuePrompt) {
        this.standardValuePrompt = standardValuePrompt;
    }

    public List<Boolean> getColumnVisibale() {
        return columnVisibale;
    }
}
