/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.controller;

import com.rits.cloning.Cloner;
import com.viettel.bean.ResultGetAccount;
import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.*;
import com.viettel.it.object.ComboBoxObject;
import com.viettel.it.object.CommandObject;
import com.viettel.it.persistence.*;
import com.viettel.it.util.*;
import com.viettel.passprotector.PassProtector;
import com.viettel.util.Constant;
import com.viettel.it.util.SecurityService;
import com.viettel.util.SessionUtil;
import com.viettel.util.SessionWrapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.ss.util.CellRangeAddressList;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.FileUploadEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.StreamedContent;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.io.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.text.MessageFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author hienhv4
 */
@ViewScoped
@ManagedBean
public class ItCommandController implements Serializable {

    private static final Logger logger = LogManager.getLogger(CommandController.class);

    @ManagedProperty(value = "#{commandDetailService}")
    private CommandDetailServiceImpl commandDetailService;
    @ManagedProperty(value = "#{commandTelnetParserService}")
    private CommandTelnetParserServiceImpl commandTelnetParserService;
    @ManagedProperty(value = "#{vendorService}")
    private VendorServiceImpl vendorService;
    @ManagedProperty(value = "#{nodeTypeService}")
    private NodeTypeServiceImpl nodeTypeService;
    @ManagedProperty(value = "#{paramInputService}")
    private ParamInputServiceImpl paramInputService;
    @ManagedProperty(value = "#{actionCommandService}")
    private ActionCommandServiceImpl actionCommandService;
    @ManagedProperty(value = "#{versionService}")
    private VersionServiceImpl versionService;
    @ManagedProperty(value = "#{nodeService}")
    private NodeServiceImpl nodeService;
    @ManagedProperty(value = "#{paramGroupService}")
    private ParamGroupServiceImpl paramGroupService;
    @ManagedProperty(value = "#{paramValueService}")
    private ParamValueServiceImpl paramValueService;
    @ManagedProperty(value = "#{flowTemplatesService}")
    private FlowTemplatesServiceImpl flowTemplatesService;

    private LazyDataModel<CommandDetail> lazyModel;
    private CommandDetail obj;
    private CommandDetail oldObj;
    private List<Vendor> vendors;
    private List<NodeType> nodeTypes;
    private List<Version> versions;
    private List<ComboBoxObject> operators;
    private List<ComboBoxObject> noCheckCbb;
    private List<ComboBoxObject> protocols;
    private List<Node> nodeRuns;

    private List<Vendor> vendorSeleteds;
    private List<NodeType> nodeTypeSelecteds;
    private List<Version> versionSelecteds;

    private Vendor vendorSelected;
    private NodeType nodeTypeSelected;
    private Version versionSelected;
    private ComboBoxObject opeSelected;
    private ComboBoxObject protocolSelected;

    private boolean isEdit, isClone;

    private List<ParamInput> params = new ArrayList<>();
    private Map<String, String> mapParamDefaultVal = new HashMap<>();
    private String dialogHeader;
    private StreamedContent resultImport;
    private boolean hasError;
    private CommandObject commandSend;

    private boolean standardValueRequired;
    private boolean standardValueReadonly;
    private String standardValuePrompt;
    private boolean isShowNodeType;


    //default value
    private NodeType nodeDefault;
    private Version versionDefault;




    @PostConstruct
    protected void initialize() {
        try {
            logger.info("aaaaaaaaaaaa");
            obj = new CommandDetail();

            vendorSeleteds = new ArrayList<>();
            nodeTypeSelecteds = new ArrayList<>();
            versionSelecteds = new ArrayList<>();


            Map<String, String> orderVendor = new HashMap<>();
            orderVendor.put("vendorName", "asc");
            vendors = vendorService.findList(null, orderVendor);

            vendorSeleteds.add(vendors.get(0));

            isShowNodeType = Boolean.parseBoolean(MessageUtil.getResourceBundleConfig("SHOW_NODE_TYPE").toLowerCase());
            if (isShowNodeType) {
                Map<String, String> orderNodeType = new HashMap<>();
                orderNodeType.put("typeName", "asc");
                nodeTypes = nodeTypeService.findList(null, orderNodeType);
            } else {
                nodeTypes = new ArrayList<>();
                nodeTypes.add(nodeTypeService.get(-1l));
            }

            Map<String, String> orderVersion = new HashMap<>();
            orderVersion.put("versionName", "asc");
            versions = versionService.findList(null, orderVersion);

            operators = new ArrayList<>();
            operators.add(new ComboBoxObject("CONTAIN", "CONTAIN"));
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
            operators.add(new ComboBoxObject("NO CHECK", "NO CHECK"));
            operators.add(new ComboBoxObject("IS NULL OR CONTAIN", "IS NULL OR CONTAIN"));

            noCheckCbb = new ArrayList<>();
            noCheckCbb.add(new ComboBoxObject("NO CHECK", "NO CHECK"));

            Collections.sort(operators, new Comparator<ComboBoxObject>() {
                @Override
                public int compare(ComboBoxObject a, ComboBoxObject b) {
                    return a.getLabel().compareTo(b.getLabel());
                }
            });

            protocols = new ArrayList<>();
            //protocols.add(new ComboBoxObject(Config.PROTOCOL_TELNET, Config.PROTOCOL_TELNET));
            protocols.add(new ComboBoxObject(Config.PROTOCOL_SSH, Config.PROTOCOL_SSH));
            protocols.add(new ComboBoxObject(Config.PROTOCOL_SQL, Config.PROTOCOL_SQL));
            protocols.add(new ComboBoxObject(Config.PROTOCOL_EXCHANGE, Config.PROTOCOL_EXCHANGE));
            protocols.add(new ComboBoxObject(Config.PROTOCOL_WEBSERVICE, Config.PROTOCOL_WEBSERVICE));
            nodeDefault = nodeTypeService.findById(Config.NODE_DEFAULT_ID);
            versionDefault = versionService.findById(Config.VERSION_DEFAULT_ID);
//            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
//            orders.put("commandTelnetParser.cmd", "ASC");
//            orders.put("vendor.vendorName", "ASC");
//            orders.put("nodeType.typeName", "ASC");
            Map<String, Object> filters = getFilter();
            if(new SessionUtil().isItBusinessAdmin() == false){
                filters.put("userName", SessionWrapper.getCurrentUsername());
            }
            if (new SessionUtil().isOnlyViewCommand()) {
                filters.put("commandType", 1l);
            }

            filters.put("commandClassify", Config.COMMAND_CLASSIFY.ITBUSINESS.value);

            lazyModel = new LazyDataModelBaseNew(commandDetailService, filters, new LinkedHashMap<>());

        } catch (Exception e) {
            isShowNodeType = true;
            logger.error(e.getMessage(), e);
        }
    }

    public void onSearch() {
        try {
//            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
//            orders.put("commandTelnetParser.cmd", "ASC");
//            orders.put("vendor.vendorName", "ASC");
//            orders.put("nodeType.typeName", "ASC");

            Map<String, Object> filters = getFilter();
            if(new SessionUtil().isItBusinessAdmin() == false){
                filters.put("userName", SessionWrapper.getCurrentUsername());
            }
            if (new SessionUtil().isOnlyViewCommand()) {
                filters.put("commandType", 1l);
            }
            filters.put("commandClassify", Config.COMMAND_CLASSIFY.ITBUSINESS.value);
            lazyModel = new LazyDataModelBaseNew(commandDetailService, filters, new LinkedHashMap<>());
            DataTable dataTable = (DataTable)  FacesContext.getCurrentInstance().getViewRoot().findComponent("tableCommand");
            dataTable.setFirst(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private Map<String, Object> getFilter() {
        Map<String, Object> filters = new HashMap<>();
        if (obj.getCommandTelnetParser().getCmd() != null && !"".equals(obj.getCommandTelnetParser().getCmd().trim())) {
            filters.put("commandTelnetParser.cmd", obj.getCommandTelnetParser().getCmd().trim());
        }

        if (obj.getCommandName() != null && !"".equals(obj.getCommandName().trim())) {
            filters.put("commandName", obj.getCommandName().trim());
        }

        if (obj.getVendor() != null && obj.getVendor().getVendorId() != null && obj.getVendor().getVendorId() != -1) {
            filters.put("vendor.vendorId", obj.getVendor().getVendorId());
        }

        if (obj.getNodeType() != null && obj.getNodeType().getTypeId() != null && obj.getNodeType().getTypeId() != -1) {
            filters.put("nodeType.typeId", obj.getNodeType().getTypeId());
        }

        if (obj.getVersion() != null && obj.getVersion().getVersionId() != null
                && obj.getVersion().getVersionId() != -1) {
            filters.put("version.versionId", obj.getVersion().getVersionId());
        }

        if (obj.getCommandType() != null && obj.getCommandType() != -1) {
            filters.put("commandType", obj.getCommandType());
        }

        if (opeSelected != null && opeSelected.getValue() != null) {
            filters.put("operator", opeSelected.getValue());
        }

        if (protocolSelected != null && protocolSelected.getValue() != null) {
            filters.put("protocol", protocolSelected.getValue());
        }
        return filters;
    }

    public void onSaveOrUpdate() {
        boolean isExist = false;
        try {
            Date startTime = new Date();
            if (!validateInput(isEdit)) {
                return;
            }

            if (isClone) {
                nodeTypeSelecteds.clear();
                vendorSeleteds.clear();
                versionSelecteds.clear();

                nodeTypeSelecteds.add(nodeDefault);
                versionSelecteds.add(versionSelected);
            }

            if (isEdit && !isClone) {
                Map<String, Object> filters = new HashMap<>();
                filters.put("commandTelnetParser.cmd-" + GenericDaoImplNewV2.EXAC_IGNORE_CASE, obj.getCommandTelnetParser().getCmd().trim());
                filters.put("commandName-" + GenericDaoImplNewV2.EXAC_IGNORE_CASE, obj.getCommandName().trim());
                filters.put("commandDetailId-" + GenericDaoImplNewV2.NEQ, obj.getCommandDetailId());

                List<CommandDetail> lstCmd = commandDetailService.findList(filters);
                String msg = "";
                if (lstCmd != null && !lstCmd.isEmpty()) {
                    if (isShowNodeType) {
                        for (CommandDetail cmd : lstCmd) {
                            if (cmd.getVendor().getVendorId().equals(vendorSelected.getVendorId())
                                    && cmd.getNodeType().getTypeId().equals(nodeTypeSelected.getTypeId())
                                    && cmd.getVersion().getVersionId().equals(versionSelected.getVersionId())) {
                                msg = "\n" + vendorSelected.getVendorName() + " - " + nodeTypeSelected.getTypeName() + " - " + versionSelected.getVersionName();
                                break;
                            }
                        }
                    } else {
                        for (CommandDetail cmd : lstCmd) {
                            if (cmd.getVendor().getVendorId().equals(vendorSelected.getVendorId())
                                    && cmd.getVersion().getVersionId().equals(versionSelected.getVersionId())) {
                                msg = "\n" + vendorSelected.getVendorName() + " - " + versionSelected.getVersionName();
                                break;
                            }
                        }
                    }
                }

                if (!msg.isEmpty()) {
                    if (isShowNodeType) {
                        MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("message.choose.cmdExist"), msg));
                    } else {
                        MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("message.choose.cmdExist.noNodeType"), msg));
                    }
                    return;
                }

                CommandDetail cmdDetail = new CommandDetail();
                CommandTelnetParser parser = new CommandTelnetParser();
                if (obj.getCommandDetailId() != null) {
                    cmdDetail = commandDetailService.findById(obj.getCommandDetailId());
                }

                if (obj.getCommandTelnetParser().getTelnetParserId() != null) {
                    parser = commandTelnetParserService.findById(obj.getCommandTelnetParser().getTelnetParserId());
                }

                getParser(parser, obj);

                commandTelnetParserService.saveOrUpdate(parser);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItCommandController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            obj.getCommandTelnetParser().getTelnetParserId() != null ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE ,
                            parser.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB

                Vendor vendorSl;
                if(protocolSelected.getValue().equals(Config.PROTOCOL_SSH)){
                    vendorSl = vendorService.findById(Config.SERVER_ID);
                }else if(protocolSelected.getValue().equals(Config.PROTOCOL_SQL)){
                    vendorSl = vendorService.findById(Config.DATABASE_ID);
                }else if(protocolSelected.getValue().equals(Config.PROTOCOL_EXCHANGE)){
                    vendorSl = vendorService.findById(Config.PROVISIONING_ID);
                }else{
                    vendorSl = vendorService.findById(Config.WEB_SERVICE_ID);
                }

                cmdDetail.setCommandTelnetParser(parser);
                cmdDetail.setCreateTime(new Date());
                cmdDetail.setIsActive(1l);
                cmdDetail.setNodeType(nodeDefault);
                cmdDetail.setVendor(vendorSl);
                cmdDetail.setVersion(versionDefault);
                cmdDetail.setOperator(opeSelected.getValue());
                cmdDetail.setProtocol(protocolSelected.getValue());
                cmdDetail.setStandardValue(obj.getStandardValue() == null ? "" : obj.getStandardValue().trim());
                cmdDetail.setCommandName(obj.getCommandName() == null ? "" : obj.getCommandName().trim());
                cmdDetail.setDescription(obj.getDescription() == null ? "" : obj.getDescription().trim());
                cmdDetail.setExpectedResult(obj.getExpectedResult() == null ? "" : obj.getExpectedResult().trim());
                cmdDetail.setUserName(SessionWrapper.getCurrentUsername());
                cmdDetail.setCommandType(obj.getCommandType());
                cmdDetail.setWsUrl(obj.getWsUrl());
                cmdDetail.setWsTargetname(obj.getWsTargetname());
                cmdDetail.setCommandClassify(1l);

                commandDetailService.saveOrUpdate(cmdDetail);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItCommandController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            obj.getCommandDetailId() != null ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.UPDATE,
                            parser.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB

                reloadParam(parser.getCmd());

                List<ParamInput> currParam = obj.getParamInputs();
                for (ParamInput pr : params) {
                    ParamInput cur = checkValueInListDTO(currParam, pr.getParamCode());
                    if (cur != null) {
                        pr.setParamInputId(cur.getParamInputId());
                    }
                    pr.setCommandDetail(cmdDetail);
                    pr.setCreateTime(new Date());
                    pr.setIsActive(1l);
                    if (mapParamDefaultVal.get(pr.getParamCode()) != null) {
                        pr.setParamDefault(mapParamDefaultVal.get(pr.getParamCode()));
                    }
                    pr.setParamType(0l);
                    if (pr.getReadOnly() == null) {
                        pr.setReadOnly(false);
                    }
                    pr.setUserName(SessionWrapper.getCurrentUsername());
                }

                paramInputService.saveOrUpdate(params);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItCommandController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.UPDATE,
                            params.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB

                for (ParamInput pr : currParam) {
                    if (checkValueInListDTO(params, pr.getParamCode()) == null) {
                        paramGroupService.execteBulk("delete from ParamGroup where paramInput.paramInputId =?", pr.getParamInputId());

                        paramValueService.execteBulk("delete from ParamValue where paramInput.paramInputId =?", pr.getParamInputId());

                        paramInputService.execteBulk("delete from ParamInput where paramInputId =?", pr.getParamInputId());

                        //tuanda38_20181010_fix error remove param in flow template map alarm
                        new FlowTemplateMapAlarmServiceImpl().execteBulk("delete from FlowTemplateMapAlarm where paramInput.paramInputId =?",pr.getParamInputId());
                    }
                }

                if (isModify(oldObj, cmdDetail)) {
                    List<FlowTemplates> lstTemplate = new ArrayList<>();
                    if (cmdDetail.getActionCommands() != null && !cmdDetail.getActionCommands().isEmpty()) {
                        for (ActionCommand actionCmd : cmdDetail.getActionCommands()) {
                            if (actionCmd.getActionDetail().getAction().getActionOfFlows() != null
                                    && !actionCmd.getActionDetail().getAction().getActionOfFlows().isEmpty()) {
                                for (ActionOfFlow actionOfFlow : actionCmd.getActionDetail().getAction().getActionOfFlows()) {
                                    if ((actionOfFlow.getFlowTemplates().getStatus() == null || actionOfFlow.getFlowTemplates().getStatus() == 9)
                                            && !lstTemplate.contains(actionOfFlow.getFlowTemplates())) {
                                        lstTemplate.add(actionOfFlow.getFlowTemplates());
                                    }
                                }
                            }
                        }
                    }
                    for (FlowTemplates template : lstTemplate) {
                        template.setStatus(0);
                    }
                    flowTemplatesService.saveOrUpdate(lstTemplate);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItCommandController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.UPDATE,
                                lstTemplate.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                }

                MessageUtil.setInfoMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.success"),
                        MessageUtil.getResourceBundleMessage("title.updateCommand")));
            } else {
                Map<String, Object> filters = new HashMap<>();
                filters.put("commandTelnetParser.cmd-" + GenericDaoImplNewV2.EXAC_IGNORE_CASE, obj.getCommandTelnetParser().getCmd().trim());
                filters.put("commandName-" + GenericDaoImplNewV2.EXAC_IGNORE_CASE, obj.getCommandName().trim());

                List<CommandDetail> lstCmd = commandDetailService.findList(filters);
                String msg = "";
                if (lstCmd != null && !lstCmd.isEmpty()) {
                    if (isShowNodeType) {
                        for (NodeType node : nodeTypeSelecteds) {
                            for (Vendor vendor : vendorSeleteds) {
                                for (Version version : versionSelecteds) {
                                    for (CommandDetail cmd : lstCmd) {
                                        if (cmd.getVendor().getVendorId().equals(vendor.getVendorId())
                                                && cmd.getNodeType().getTypeId().equals(node.getTypeId())
                                                && cmd.getVersion().getVersionId().equals(version.getVersionId())) {
                                            msg = "\n" + vendor.getVendorName() + " - " + node.getTypeName() + " - " + version.getVersionName();
                                            break;
                                        }
                                    }
                                }
                            }
                        }
                    } else {
                        for (Vendor vendor : vendorSeleteds) {
                            for (Version version : versionSelecteds) {
                                for (CommandDetail cmd : lstCmd) {
                                    if (cmd.getVendor().getVendorId().equals(vendor.getVendorId())
                                            && cmd.getVersion().getVersionId().equals(version.getVersionId())) {
                                        msg = "\n" + vendor.getVendorName() + " - " + version.getVersionName();
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }

                if (!msg.isEmpty()) {
                    if (isShowNodeType) {
                        MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("message.choose.cmdExist"), msg));
                    } else {
                        MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("message.choose.cmdExist.noNodeType"), msg));
                    }
                    return;
                }
                String userName = SessionUtil.getCurrentUsername();
                ///for (Vendor vendor : vendorSeleteds) {
                    String commandName = obj.getCommandName();
                    List<CommandDetail> lstcmd;
                    try {
                        lstcmd = new CommandDetailServiceImpl().findList("from CommandDetail where userName = '" +userName +"' and commandName = '"+ commandName+"'",-1,-1);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        lstcmd = new ArrayList<>();
                    }

                Vendor vendorSl;
                if(protocolSelected.getValue().equals(Config.PROTOCOL_SSH)){
                    vendorSl = vendorService.findById(Config.SERVER_ID);
                }else if(protocolSelected.getValue().equals(Config.PROTOCOL_SQL)){
                    vendorSl = vendorService.findById(Config.DATABASE_ID);
                }else if(protocolSelected.getValue().equals(Config.PROTOCOL_EXCHANGE)){
                    vendorSl = vendorService.findById(Config.PROVISIONING_ID);
                }else{
                    vendorSl = vendorService.findById(Config.WEB_SERVICE_ID);
                }

                    if(lstcmd == null || lstcmd.size() == 0) {
                        CommandDetail cmdDetail = new CommandDetail();
                        CommandTelnetParser parser = new CommandTelnetParser();
                        getParser(parser, obj);
                        commandTelnetParserService.save(parser);
                        cmdDetail.setCommandTelnetParser(parser);
                        cmdDetail.setCreateTime(new Date());
                        cmdDetail.setIsActive(1l);
                        cmdDetail.setVendor(vendorSl);
                        //cmdDetail.setVendor(vendor);
                        cmdDetail.setNodeType(nodeDefault);
                        cmdDetail.setVersion(versionDefault);
                        cmdDetail.setOperator(new ComboBoxObject("NO CHECK", "NO CHECK").getValue());
                        cmdDetail.setProtocol(protocolSelected.getValue());
                        cmdDetail.setStandardValue(obj.getStandardValue() == null ? "" : obj.getStandardValue().trim());
                        cmdDetail.setCommandName(obj.getCommandName() == null ? "" : obj.getCommandName().trim());
                        cmdDetail.setDescription(obj.getDescription() == null ? "" : obj.getDescription().trim());
                        cmdDetail.setExpectedResult(obj.getExpectedResult() == null ? "" : obj.getExpectedResult().trim());
                        cmdDetail.setUserName(SessionWrapper.getCurrentUsername());
                        cmdDetail.setCommandType(obj.getCommandType());
                        cmdDetail.setWsUrl(obj.getWsUrl());
                        cmdDetail.setWsTargetname(obj.getWsTargetname());
                        cmdDetail.setCommandClassify(1l);
                        commandDetailService.save(cmdDetail);
                        reloadParam(obj.getCommandTelnetParser().getCmd());
                        for (ParamInput pr : params) {
                            pr.setCommandDetail(cmdDetail);
                            pr.setCreateTime(new Date());
                            pr.setIsActive(1l);
                            pr.setParamType(0l);
                            if (mapParamDefaultVal.get(pr.getParamCode()) != null) {
                                pr.setParamDefault(mapParamDefaultVal.get(pr.getParamCode()));
                            }
                            if (pr.getReadOnly() == null) {
                                pr.setReadOnly(false);
                            }
                            pr.setUserName(SessionWrapper.getCurrentUsername());
                        }

                        paramInputService.saveOrUpdate(params);
                        //20180620_tudn_start ghi log DB
                        try {
                            LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                    LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItCommandController.class.getName(),
                                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                                    LogUtils.ActionType.UPDATE,
                                    params.toString(), LogUtils.getRequestSessionId());
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        //20180620_tudn_end ghi log DB
                    }else{
                        isExist =true;
                    }
               // }
                if (!isClone && isExist == false) {
                    MessageUtil.setInfoMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.success"),
                            MessageUtil.getResourceBundleMessage("title.insertCommand")));
                }else if(isExist == true){
                    MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                            MessageUtil.getResourceBundleMessage("label.commanddetail.exist")));
                }
                else {


                    MessageUtil.setInfoMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.success"),
                            MessageUtil.getResourceBundleMessage("title.cloneCommand")));
                }
            }

            /*
			Ghi log tac dong nguoi dung
            */
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItCommandController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        (isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
                        obj.toString(),
                        LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            obj = new CommandDetail();
            RequestContext.getCurrentInstance().execute("PF('insertCmdConfigDialog').hide();");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    isEdit ? MessageUtil.getResourceBundleMessage("title.updateCommand") : MessageUtil.getResourceBundleMessage("title.insertCommand")));
        }
    }

    public void onDelete() {
        try {
            Date startTime = new Date();
            if (obj == null) {
                MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("message.choose.delete"));
                return;
            }
            Map<String, Object> filters = new HashMap<>();
            filters.put("commandDetail.commandDetailId", obj.getCommandDetailId());

            List<ActionCommand> lstActionCmd = actionCommandService.findList(filters);

            if (lstActionCmd != null && !lstActionCmd.isEmpty()) {
                MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("message.choose.using"));
                return;
            }

            paramInputService.execteBulk("delete from ParamInput where commandDetail.commandDetailId =?", obj.getCommandDetailId());

            commandDetailService.delete(obj);

//            commandTelnetParserService.delete(obj.getCommandTelnetParser());
            commandTelnetParserService.execteBulk("delete from CommandTelnetParser where telnetParserId in (select commandTelnetParser.telnetParserId from CommandDetail where commandDetailId = ?)", obj.getCommandDetailId());

            /*
			Ghi log tac dong nguoi dung
            */
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItCommandController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.DELETE,
                        obj.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            MessageUtil.setInfoMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.success"),
                    MessageUtil.getResourceBundleMessage("title.deleteCommand")));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    MessageUtil.getResourceBundleMessage("title.deleteCommand")));
        }
    }

    public void noAction(String value, String paramCode) {
        logger.info("????????? " + paramCode + " // " + value);
        mapParamDefaultVal.put(paramCode, value);
    }

    private void reloadParam(String cmd) {
        try {
            List<String> lstParam = getParamList(cmd);

            List<ParamInput> paramList = new ArrayList<>();
            for (ParamInput dto : params) {
                ParamInput pr = new ParamInput();
                pr.setParamCode(dto.getParamCode());
                pr.setReadOnly(dto.getReadOnly());
                paramList.add(pr);
            }

            if (lstParam != null && !lstParam.isEmpty()) {
                for (String str : lstParam) {
                    if (checkValueInListDTO(paramList, str) == null) {
                        ParamInput dto = new ParamInput();
                        dto.setParamCode(str);
                        paramList.add(dto);
                    }
                }

                List<ParamInput> paramLocal = new ArrayList<>();
                for (ParamInput dto : paramList) {
                    if (checkValueInList(lstParam, dto.getParamCode())) {
                        paramLocal.add(dto);
                    }
                }
                params = paramLocal;
            } else {
                logger.info("Param is empty");
                params = new ArrayList<>();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void reloadParam() {
        reloadParam(obj.getCommandTelnetParser().getCmd());
    }

    public StreamedContent onDownloadTemplate() {
        Workbook wb = null;
        try {
            ServletContext context = (ServletContext) FacesContext.getCurrentInstance().getExternalContext().getContext();
            String templatePath = context.getRealPath("/") + File.separator + "templates" + File.separator
                    + "import" + File.separator + (isShowNodeType ? "ImportCmdTemplate.xlsx" : "ImportCmdTemplate_noNodeType.xlsx");

            wb = WorkbookFactory.create(new File(templatePath));
            Sheet sheet = wb.getSheetAt(0);
            Sheet hiddenSheet = wb.createSheet("hidden");

            int maxValue = vendors == null ? 0 : vendors.size();
            if (nodeTypes != null && isShowNodeType) {
                maxValue = (maxValue >= nodeTypes.size()) ? maxValue : nodeTypes.size();
            }
            if (versions != null) {
                maxValue = (maxValue >= versions.size()) ? maxValue : versions.size();
            }
            if (operators != null) {
                maxValue = (maxValue >= operators.size()) ? maxValue : operators.size();
            }

            for (int i = 0; i < maxValue; i++) {
                Row row = hiddenSheet.createRow(i);
                if (vendors != null && vendors.size() > i) {
                    row.createCell(0).setCellValue(vendors.get(i).getVendorName());
                }
                if (versions != null && versions.size() > i) {
                    row.createCell(1).setCellValue(versions.get(i).getVersionName());
                }
                if (operators != null && operators.size() > i) {
                    row.createCell(2).setCellValue(operators.get(i).getLabel());
                }
                if (nodeTypes != null && nodeTypes.size() > i && isShowNodeType) {
                    row.createCell(3).setCellValue(nodeTypes.get(i).getTypeName());
                }
            }

            int i = 1;
            if (vendors != null && !vendors.isEmpty()) {
                DataValidationHelper dvHelper = sheet.getDataValidationHelper();
                DataValidationConstraint dvConstraint = dvHelper.createFormulaListConstraint("hidden!$A$1:$A$" + vendors.size());
                CellRangeAddressList addressList = new CellRangeAddressList(8, 100, i, i);
                DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
                validation.setShowErrorBox(true);
                sheet.addValidationData(validation);
            }

            if (isShowNodeType) {
                i++;
            }
            if (nodeTypes != null && !nodeTypes.isEmpty() && isShowNodeType) {
                DataValidationHelper dvHelper = sheet.getDataValidationHelper();
                DataValidationConstraint dvConstraint = dvHelper.createFormulaListConstraint("hidden!$D$1:$D$" + nodeTypes.size());
                CellRangeAddressList addressList = new CellRangeAddressList(8, 100, i, i);
                DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
                validation.setShowErrorBox(true);
                sheet.addValidationData(validation);
            }

            i++;
            if (versions != null && !versions.isEmpty()) {
                DataValidationHelper dvHelper = sheet.getDataValidationHelper();
                DataValidationConstraint dvConstraint = dvHelper.createFormulaListConstraint("hidden!$B$1:$B$" + versions.size());
                CellRangeAddressList addressList = new CellRangeAddressList(8, 100, i, i);
                DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
                validation.setShowErrorBox(true);
                sheet.addValidationData(validation);
            }

            i += 5;
            if (operators != null && !operators.isEmpty()) {
                DataValidationHelper dvHelper = sheet.getDataValidationHelper();
                DataValidationConstraint dvConstraint = dvHelper.createFormulaListConstraint("hidden!$C$1:$C$" + operators.size());
                CellRangeAddressList addressList = new CellRangeAddressList(8, 100, i, i);
                DataValidation validation = dvHelper.createValidation(dvConstraint, addressList);
                validation.setShowErrorBox(true);
                sheet.addValidationData(validation);
            }

            wb.setSheetHidden(1, true);

            ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                    .getExternalContext().getContext();
            String pathOut = ctx.getRealPath("/") + Config.PATH_OUT + (isShowNodeType ? "ImportCmdTemplate.xlsx" : "ImportCmdTemplate_noNodeType.xlsx");

            FileOutputStream fileOut = null;
            try {
                fileOut = new FileOutputStream(pathOut);
                wb.write(fileOut);

                return new DefaultStreamedContent(new FileInputStream(pathOut), ".xlsx", "ImportCmdTemplate.xlsx");
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

    public void onImport(FileUploadEvent event) {
        List<CommandDetail> result = new ArrayList<>();
        hasError = false;
        try {
            Workbook wb = WorkbookFactory.create(event.getFile().getInputstream());
            Sheet sheet = wb.getSheetAt(0);
            Row rowHeader = sheet.getRow(7);

            boolean check = checkHeader(rowHeader);

            if (check) {
                List<Vendor> lstVendor = vendorService.findList();
                List<NodeType> lstNodeType = nodeTypeService.findList();
                List<Version> lstVersion = versionService.findList();
                NodeType typeDefault = nodeTypeService.findById(-1l);

                Map<String, Long> mapVendor = new HashMap<>();
                Map<String, Long> mapNodeType = new HashMap<>();
                Map<String, Long> mapVersion = new HashMap<>();
                Map<String, String> mapOpers = new HashMap<>();

                for (Vendor vd : lstVendor) {
                    mapVendor.put(vd.getVendorName(), vd.getVendorId());
                }

                for (NodeType nt : lstNodeType) {
                    mapNodeType.put(nt.getTypeName(), nt.getTypeId());
                }

                for (Version vs : lstVersion) {
                    mapVersion.put(vs.getVersionName(), vs.getVersionId());
                }

                for (ComboBoxObject vs : operators) {
                    mapOpers.put(vs.getLabel(), vs.getValue());
                }

                int rowNum = sheet.getLastRowNum();
                for (int i = 8; i <= rowNum; i++) {
                    CommandDetail cmdDetail = new CommandDetail();
                    String err = checkRow(sheet, i, cmdDetail);
                    if (err != null) {
                        //Truong hop row co du lieu
                        if ("".equals(err)) {
                            Long vendorId = mapVendor.get(cmdDetail.getVendor().getVendorName());
                            Long nodeTypeId = mapNodeType.get(cmdDetail.getNodeType().getTypeName());
                            Long versionId = mapVersion.get(cmdDetail.getVersion().getVersionName());
                            String oper = mapOpers.get(cmdDetail.getOperator().toUpperCase());
                            Long cmdType = null;
                            if (cmdDetail.getCommandTypeStr() != null) {
                                if (cmdDetail.getCommandTypeStr().trim().toLowerCase().equals(MessageUtil.getResourceBundleMessage("label.command.type.impact").toLowerCase())) {
                                    cmdType = 0l;
                                } else if (cmdDetail.getCommandTypeStr().trim().toLowerCase().equals(MessageUtil.getResourceBundleMessage("label.command.type.view").toLowerCase())) {
                                    cmdType = 1l;
                                }
                            }

                            if (vendorId == null) {
                                err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.message.noexists"),
                                        MessageUtil.getResourceBundleMessage("label.vendorName")) + "\n";
                            }

                            if (nodeTypeId == null && isShowNodeType) {
                                err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.message.noexists"),
                                        MessageUtil.getResourceBundleMessage("label.nodeTypeName")) + "\n";
                            }

                            if (versionId == null) {
                                err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.message.noexists"),
                                        MessageUtil.getResourceBundleMessage("label.versionName")) + "\n";
                            }

                            if (cmdType == null) {
                                err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.message.noexists"),
                                        MessageUtil.getResourceBundleMessage("label.command.type")) + "\n";
                            } else {
                                cmdDetail.setCommandType(cmdType);
                            }

                            if (cmdDetail.getProtocol() == null || (!"TELNET".equals(cmdDetail.getProtocol().toUpperCase())
                                    && !"SSH".equals(cmdDetail.getProtocol().toUpperCase())
                                    && !"SQL".equals(cmdDetail.getProtocol().toUpperCase()))) {
                                err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.message.noexists"),
                                        MessageUtil.getResourceBundleMessage("label.protocol")) + "\n";
                            }

                            if (isShowNodeType) {
                                if (vendorId != null && nodeTypeId != null && versionId != null) {
                                    Map<String, Object> filters = new HashMap<>();
                                    filters.put("commandTelnetParser.cmd-" + GenericDaoImplNewV2.EXAC_IGNORE_CASE, cmdDetail.getCommandTelnetParser().getCmd().trim());
                                    filters.put("commandName-" + GenericDaoImplNewV2.EXAC_IGNORE_CASE, cmdDetail.getCommandName().trim());
                                    filters.put("vendor.vendorId", vendorId);
                                    filters.put("nodeType.typeId", nodeTypeId);
                                    filters.put("version.versionId", versionId);
                                    filters.put("isActive", 1l);

                                    List<CommandDetail> lstCmd = commandDetailService.findList(filters);
                                    if (lstCmd != null && !lstCmd.isEmpty()) {
                                        err += MessageUtil.getResourceBundleMessage("message.command.exists") + "\n";
                                    } else {
                                        cmdDetail.getVendor().setVendorId(vendorId);
                                        cmdDetail.getNodeType().setTypeId(nodeTypeId);
                                        cmdDetail.getVersion().setVersionId(versionId);
                                    }
                                }
                            } else {
                                if (vendorId != null && versionId != null) {
                                    Map<String, Object> filters = new HashMap<>();
                                    filters.put("commandTelnetParser.cmd-" + GenericDaoImplNewV2.EXAC_IGNORE_CASE, cmdDetail.getCommandTelnetParser().getCmd().trim());
                                    filters.put("commandName-" + GenericDaoImplNewV2.EXAC_IGNORE_CASE, cmdDetail.getCommandName().trim());
                                    filters.put("vendor.vendorId", vendorId);
                                    filters.put("version.versionId", versionId);
                                    filters.put("isActive", 1l);

                                    List<CommandDetail> lstCmd = commandDetailService.findList(filters);
                                    if (lstCmd != null && !lstCmd.isEmpty()) {
                                        err += MessageUtil.getResourceBundleMessage("message.choose.cmdExist.noNodeType") + "\n";
                                    } else {
                                        cmdDetail.getVendor().setVendorId(vendorId);
                                        cmdDetail.getNodeType().setTypeId(typeDefault.getTypeId());
                                        cmdDetail.getVersion().setVersionId(versionId);
                                    }
                                }
                            }

                            if (oper == null) {
                                err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.message.noexists"),
                                        MessageUtil.getResourceBundleMessage("label.operator")) + "\n";
                            } else {
                                cmdDetail.setOperator(oper);

                                switch (oper.toUpperCase()) {
                                    case "IS NULL":
                                    case "NOT NULL":
                                    case "NO CHECK":
                                        cmdDetail.setStandardValue("");
                                        break;
                                    case "IN":
                                    case "NOT IN":
                                    case "CONTAIN":
                                    case "IS NULL OR CONTAIN":
                                    case "NOT CONTAIN":
                                    case "=":
                                    case ">":
                                    case ">=":
                                    case "<":
                                    case "<=":
                                    case "<>":
                                        if (isNullOrEmpty(cmdDetail.getStandardValue())) {
                                            err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                                                    MessageUtil.getResourceBundleMessage("label.standardValue")) + "\n";
                                        }
                                        break;
                                    case "BETWEEN":
                                        if (isNullOrEmpty(cmdDetail.getStandardValue())) {
                                            err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                                                    MessageUtil.getResourceBundleMessage("label.standardValue")) + "\n";
                                            break;
                                        }

                                        if (!cmdDetail.getStandardValue().trim().contains(",")) {
                                            err += MessageUtil.getResourceBundleMessage("label.standardValue") + " "
                                                    + MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.between") + "\n";
                                            break;
                                        }

                                        String[] str = cmdDetail.getStandardValue().trim().split(",");
                                        if (str.length != 2) {
                                            err += MessageUtil.getResourceBundleMessage("label.standardValue") + " "
                                                    + MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.between") + "\n";
                                            break;
                                        }

                                        try {
                                            logger.debug(Double.parseDouble(str[0]));
                                            logger.debug(Double.parseDouble(str[1]));
                                        } catch (Exception ex) {
                                            logger.error(ex.getMessage(), ex);
                                            err += MessageUtil.getResourceBundleMessage("label.standardValue") + " "
                                                    + MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.between") + "\n";
                                            break;
                                        }
                                        break;
                                    case "LIKE":
                                    case "NOT LIKE":
                                        if (isNullOrEmpty(cmdDetail.getStandardValue())) {
                                            err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                                                    MessageUtil.getResourceBundleMessage("label.standardValue")) + "\n";
                                            break;
                                        }
                                        if (!cmdDetail.getStandardValue().trim().contains("%")) {
                                            err += MessageUtil.getResourceBundleMessage("label.standardValue") + " "
                                                    + MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.like") + "\n";
                                            break;
                                        }
                                        break;
                                }
                            }
                        }

                        if ("".equals(err)) {//neu khong co loi
                            try {
                                Date startTime = new Date();
                                CommandTelnetParser parser = new CommandTelnetParser();

                                getParser(parser, cmdDetail);

                                commandTelnetParserService.saveOrUpdate(parser);

                                cmdDetail.setCommandTelnetParser(parser);
                                cmdDetail.setCreateTime(new Date());
                                cmdDetail.setIsActive(1l);
                                //cmdDetail.setProtocol("TELNET");
                                cmdDetail.setUserName(SessionWrapper.getCurrentUsername());

                                commandDetailService.saveOrUpdate(cmdDetail);

                                List<String> lstParam = getParamList(cmdDetail.getCommandTelnetParser().getCmd());

                                List<ParamInput> lstPr = new ArrayList<>();
                                if (lstParam != null && !lstParam.isEmpty()) {
                                    for (String str : lstParam) {
                                        if (checkValueInListDTO(lstPr, str) == null) {
                                            ParamInput dto = new ParamInput();
                                            dto.setParamCode(str);
                                            dto.setReadOnly(false);
                                            lstPr.add(dto);
                                        }
                                    }

                                    for (ParamInput dto : lstPr) {
                                        if (!checkValueInList(lstParam, dto.getParamCode())) {
                                            lstPr.remove(dto);
                                        }
                                    }
                                }
                                for (ParamInput pr : lstPr) {
                                    pr.setCommandDetail(cmdDetail);
                                    pr.setCreateTime(new Date());
                                    pr.setIsActive(1l);
                                    pr.setParamType(0l);
                                    pr.setUserName(SessionWrapper.getCurrentUsername());
                                }

                                paramInputService.saveOrUpdate(lstPr);
                                //20180620_tudn_start ghi log DB
                                try {
                                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItCommandController.class.getName(),
                                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                                            LogUtils.ActionType.CREATE,
                                            lstPr.toString(), LogUtils.getRequestSessionId());
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                                //20180620_tudn_end ghi log DB
                                cmdDetail.setResultImport("Ok");
                                cmdDetail.setResultImportDetail(MessageUtil.getResourceBundleMessage("common.message.success"));
                            } catch (Exception ex) {
                                logger.error(ex.getMessage(), ex);

                                hasError = true;
                                cmdDetail.setResultImport("NOK");
                                cmdDetail.setResultImportDetail(MessageUtil.getResourceBundleMessage("common.message.fail"));
                            }

                            result.add(cmdDetail);
                        } else {
                            hasError = true;
                            cmdDetail.setResultImport("NOK");
                            cmdDetail.setResultImportDetail(err);
                            result.add(cmdDetail);
                        }
                    }
                }
            } else {
                MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("message.invalid.header"));
                return;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    MessageUtil.getResourceBundleMessage("title.importCommand")));
            return;
        }

        try {
            String[] header;
            String[] align;
            if (isShowNodeType) {
                header = new String[]{"vendor.vendorName=label.vendorName", "nodeType.typeName=label.nodeTypeName",
                        "version.versionName=label.versionName", "commandName=label.cmdName", "protocol=label.protocol", "commandTypeStr=label.command.type",
                        "commandTelnetParser.cmd=label.cmd", "operator=label.operator", "standardValue=label.standardValue",
                        "commandTelnetParser.cmdEnd=label.cmdEnd", "commandTelnetParser.cmdTimeout=label.cmdTimeout",
                        "commandTelnetParser.rowStart=label.rowStart", "commandTelnetParser.rowEnd=label.rowEnd",
                        "commandTelnetParser.rowOutput=label.rowOutput", "commandTelnetParser.columnOutput=label.columnOutput",
                        "commandTelnetParser.countRow=label.countRow", "commandTelnetParser.splitColumnRegex=label.splitColumnRegex",
                        "commandTelnetParser.regexOutput=label.regexOutput", "commandTelnetParser.pagingCmd=label.pagingCmd",
                        "commandTelnetParser.pagingRegex=label.pagingRegex", "description=label.cmdDescription",
                        "commandTelnetParser.confirmCmd=label.confirmCmd", "commandTelnetParser.confirmRegex=label.confirmRegex",
                        "resultImportDetail=report.result"};

                align = new String[]{
                        "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT",
                        "LEFT", "CENTER", "LEFT", "LEFT", "CENTER",
                        "CENTER", "CENTER", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT"};
            } else {
                header = new String[]{"vendor.vendorName=label.vendorName",
                        "version.versionName=label.versionName", "commandName=label.cmdName", "protocol=label.protocol", "commandTypeStr=label.command.type",
                        "commandTelnetParser.cmd=label.cmd", "operator=label.operator", "standardValue=label.standardValue",
                        "commandTelnetParser.cmdEnd=label.cmdEnd", "commandTelnetParser.cmdTimeout=label.cmdTimeout",
                        "commandTelnetParser.rowStart=label.rowStart", "commandTelnetParser.rowEnd=label.rowEnd",
                        "commandTelnetParser.rowOutput=label.rowOutput", "commandTelnetParser.columnOutput=label.columnOutput",
                        "commandTelnetParser.countRow=label.countRow", "commandTelnetParser.splitColumnRegex=label.splitColumnRegex",
                        "commandTelnetParser.regexOutput=label.regexOutput", "commandTelnetParser.pagingCmd=label.pagingCmd",
                        "commandTelnetParser.pagingRegex=label.pagingRegex", "description=label.cmdDescription",
                        "commandTelnetParser.confirmCmd=label.confirmCmd", "commandTelnetParser.confirmRegex=label.confirmRegex",
                        "resultImportDetail=report.result"};

                align = new String[]{
                        "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT",
                        "LEFT", "CENTER", "LEFT", "LEFT", "CENTER",
                        "CENTER", "CENTER", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT"};
            }

            List<AbstractMap.SimpleEntry<String, String>> headerAlign = CommonExport.buildExportHeader(header, align);

            String fileTemplate = CommonExport.getTemplateExport();

            File fileExport = CommonExport.exportFile(result, headerAlign, "", fileTemplate,
                    "CommandImportResult", 7, "", 4,
                    MessageUtil.getResourceBundleMessage("title.commandImportResult"));

            resultImport = new DefaultStreamedContent(new FileInputStream(fileExport), ".xlsx", fileExport.getName());

            RequestContext.getCurrentInstance().execute("PF('importDialog').hide();");
            RequestContext.getCurrentInstance().execute("PF('resultImportDialog').show();");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    MessageUtil.getResourceBundleMessage("title.importCommand")));
        }
    }

    public StreamedContent getImportResult() {
        return resultImport;
    }

    public boolean checkHeader(Row rowHeader) {
        boolean check = true;

        int i = 0;
        String STT = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String vendorName = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String nodeTypeName = "";
        if (isShowNodeType) {
            nodeTypeName = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
            i++;
        }
        String versionName = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String cmdName = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String protocol = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String cmdType = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String cmd = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String operator = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String standardValue = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String cmdEnd = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String cmdTimeout = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String rowStart = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String rowEnd = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String rowOutput = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String columnOutput = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String countRow = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String splitColumnRegex = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String regexOutput = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String pagingCmd = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String pagingRegex = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String confirmCmd = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String confirmRegex = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";
        i++;
        String description = rowHeader.getCell(i) != null && getCellValue(rowHeader.getCell(i)) != null ? getCellValue(rowHeader.getCell(i)).trim().toLowerCase() : "";

        if (!STT.equals(MessageUtil.getResourceBundleMessage("datatable.header.stt").toLowerCase())
                || !vendorName.equals(MessageUtil.getResourceBundleMessage("label.vendorName").toLowerCase())
                || (isShowNodeType && !nodeTypeName.equals(MessageUtil.getResourceBundleMessage("label.nodeTypeName").toLowerCase()))
                || !versionName.equals(MessageUtil.getResourceBundleMessage("label.versionName").toLowerCase())
                || !cmdName.equals(MessageUtil.getResourceBundleMessage("label.cmdName").toLowerCase())
                || !protocol.equals(MessageUtil.getResourceBundleMessage("label.protocol").toLowerCase())
                || !cmdType.equals(MessageUtil.getResourceBundleMessage("label.command.type").toLowerCase())
                || !cmd.equals(MessageUtil.getResourceBundleMessage("label.cmd").toLowerCase())
                || !operator.equals(MessageUtil.getResourceBundleMessage("label.operator").toLowerCase())
                || !standardValue.equals(MessageUtil.getResourceBundleMessage("label.standardValue").toLowerCase())
                || !cmdEnd.equals(MessageUtil.getResourceBundleMessage("label.cmdEnd").toLowerCase())
                || !cmdTimeout.equals(MessageUtil.getResourceBundleMessage("label.cmdTimeout").toLowerCase())
                || !rowStart.equals(MessageUtil.getResourceBundleMessage("label.rowStart").toLowerCase())
                || !rowEnd.equals(MessageUtil.getResourceBundleMessage("label.rowEnd").toLowerCase())
                || !rowOutput.equals(MessageUtil.getResourceBundleMessage("label.rowOutput").toLowerCase())
                || !columnOutput.equals(MessageUtil.getResourceBundleMessage("label.columnOutput").toLowerCase())
                || !countRow.equals(MessageUtil.getResourceBundleMessage("label.countRow").toLowerCase())
                || !splitColumnRegex.equals(MessageUtil.getResourceBundleMessage("label.splitColumnRegex").toLowerCase())
                || !regexOutput.equals(MessageUtil.getResourceBundleMessage("label.regexOutput").toLowerCase())
                || !pagingCmd.equals(MessageUtil.getResourceBundleMessage("label.pagingCmd").toLowerCase())
                || !pagingRegex.equals(MessageUtil.getResourceBundleMessage("label.pagingRegex").toLowerCase())
                || !confirmCmd.equals(MessageUtil.getResourceBundleMessage("label.confirmCmd").toLowerCase())
                || !confirmRegex.equals(MessageUtil.getResourceBundleMessage("label.confirmRegex").toLowerCase())
                || !description.equals(MessageUtil.getResourceBundleMessage("label.cmdDescription").toLowerCase())) {
            check = false;
        }
        return check;
    }

    public String checkRow(Sheet sheet, int i, CommandDetail cmdDetail) {
        String err = "";
        Row row = sheet.getRow(i);
        if (row != null) {
            int j = 0;
            String STT = getCellValue(row.getCell(j));
            j++;
            String vendorName = getCellValue(row.getCell(j));
            j++;
            String nodeTypeName = "";
            if (isShowNodeType) {
                nodeTypeName = getCellValue(row.getCell(j));
                j++;
            }
            String versionName = getCellValue(row.getCell(j));
            j++;
            String cmdName = getCellValue(row.getCell(j));
            j++;
            String protocol = getCellValue(row.getCell(j));
            j++;
            String cmdType = getCellValue(row.getCell(j));
            j++;
            String cmd = getCellValue(row.getCell(j));
            j++;
            String operator = getCellValue(row.getCell(j));
            j++;
            String standardValue = getCellValue(row.getCell(j));
            j++;
            String cmdEnd = getCellValue(row.getCell(j));
            j++;
            String cmdTimeout = getCellValue(row.getCell(j));
            j++;
            String rowStart = getCellValue(row.getCell(j));
            j++;
            String rowEnd = getCellValue(row.getCell(j));
            j++;
            String rowOutput = getCellValue(row.getCell(j));
            j++;
            String columnOutput = getCellValue(row.getCell(j));
            j++;
            String countRow = getCellValue(row.getCell(j));
            j++;
            String splitColumnRegex = getCellValue(row.getCell(j));
            j++;
            String regexOutput = getCellValue(row.getCell(j));
            j++;
            String pagingCmd = getCellValue(row.getCell(j));
            j++;
            String pagingRegex = getCellValue(row.getCell(j));
            j++;
            String confirmCmd = getCellValue(row.getCell(j));
            j++;
            String confirmRegex = getCellValue(row.getCell(j));
            j++;
            String description = getCellValue(row.getCell(j));
//            j++;

            if (!isNullOrEmpty(STT)
                    || !isNullOrEmpty(vendorName)
                    || (isShowNodeType && !isNullOrEmpty(nodeTypeName))
                    || !isNullOrEmpty(versionName)
                    || !isNullOrEmpty(cmdName)
                    || !isNullOrEmpty(protocol)
                    || !isNullOrEmpty(cmdType)
                    || !isNullOrEmpty(cmd)
                    || !isNullOrEmpty(operator)
                    || !isNullOrEmpty(standardValue)
                    || !isNullOrEmpty(cmdEnd)
                    || !isNullOrEmpty(cmdTimeout)
                    || !isNullOrEmpty(rowStart)
                    || !isNullOrEmpty(rowEnd)
                    || !isNullOrEmpty(rowOutput)
                    || !isNullOrEmpty(columnOutput)
                    || !isNullOrEmpty(countRow)
                    || !isNullOrEmpty(splitColumnRegex)
                    || !isNullOrEmpty(regexOutput)
                    || !isNullOrEmpty(pagingCmd)
                    || !isNullOrEmpty(pagingRegex)
                    || !isNullOrEmpty(confirmCmd)
                    || !isNullOrEmpty(confirmRegex)
                    || !isNullOrEmpty(description)) {

                if (isNullOrEmpty(vendorName)) {
                    err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.vendorName")) + "\n";
                } else {
                    cmdDetail.getVendor().setVendorName(vendorName);
                }

                if (isShowNodeType) {
                    if (isNullOrEmpty(nodeTypeName)) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                                MessageUtil.getResourceBundleMessage("label.nodeTypeName")) + "\n";
                    } else {
                        cmdDetail.getNodeType().setTypeName(nodeTypeName);
                    }
                }

                if (isNullOrEmpty(versionName)) {
                    err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.versionName")) + "\n";
                } else {
                    cmdDetail.getVersion().setVersionName(versionName);
                }

                if (!isNullOrEmpty(standardValue)) {
                    if (standardValue.length() > 500) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                                MessageUtil.getResourceBundleMessage("label.standardValue"), 500) + "\n";
                    } else {
                        cmdDetail.setStandardValue(standardValue);
                    }
                }

                if (isNullOrEmpty(cmdName)) {
                    err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.cmdName")) + "\n";
                } else {
                    if (cmdName.length() > 500) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                                MessageUtil.getResourceBundleMessage("label.cmdName"), 500) + "\n";
                    } else {
                        cmdDetail.setCommandName(cmdName);
                    }
                }

                if (isNullOrEmpty(protocol)) {
                    err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.protocol")) + "\n";
                } else {
                    cmdDetail.setProtocol(protocol);
                }

                if (isNullOrEmpty(cmdType)) {
                    err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.command.type")) + "\n";
                } else {
                    cmdDetail.setCommandTypeStr(cmdType);
                }

                if (isNullOrEmpty(cmd)) {
                    err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.cmd")) + "\n";
                } else {
                    if (cmd.length() > 500) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                                MessageUtil.getResourceBundleMessage("label.cmd"), 500) + "\n";
                    } else {
                        cmdDetail.getCommandTelnetParser().setCmd(cmd);
                    }
                }

                if (!isNullOrEmpty(cmdEnd)) {
//                    err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
//                            MessageUtil.getResourceBundleMessage("label.cmdEnd")) + "\n";
//                } else {
                    if (cmdEnd.length() > 100) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                                MessageUtil.getResourceBundleMessage("label.cmdEnd"), 100) + "\n";
                    } else {
                        cmdDetail.getCommandTelnetParser().setCmdEnd(cmdEnd);
                    }
                }

                if (isNullOrEmpty(operator)) {
                    err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.operator")) + "\n";
                } else {
                    cmdDetail.setOperator(operator);
                }

                if (!isNullOrEmpty(cmdTimeout)) {
                    if (!isInteger(cmdTimeout)) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.integer"),
                                MessageUtil.getResourceBundleMessage("label.cmdTimeout")) + "\n";
                    } else {
                        if (Long.valueOf(cmdTimeout) <= 0) {
                            err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.greater"),
                                    MessageUtil.getResourceBundleMessage("label.cmdTimeout"), 0) + "\n";
                        } else if (Long.valueOf(cmdTimeout) > 999) {
                            err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.lesser"),
                                    MessageUtil.getResourceBundleMessage("label.cmdTimeout"), 1000) + "\n";
                        } else {
                            cmdDetail.getCommandTelnetParser().setCmdTimeout(Long.valueOf(cmdTimeout));
                        }
                    }
                }

                if (!isNullOrEmpty(rowStart)) {
                    if (rowStart.length() > 500) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                                MessageUtil.getResourceBundleMessage("label.rowStart"), 500) + "\n";
                    } else {
                        cmdDetail.getCommandTelnetParser().setRowStart(rowStart);
                    }
                }

                if (!isNullOrEmpty(rowEnd)) {
                    if (rowEnd.length() > 500) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                                MessageUtil.getResourceBundleMessage("label.rowEnd"), 500) + "\n";
                    } else {
                        cmdDetail.getCommandTelnetParser().setRowEnd(rowEnd);
                    }
                }

                if (!isNullOrEmpty(rowOutput)) {
                    if (!isInteger(rowOutput)) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.integer"),
                                MessageUtil.getResourceBundleMessage("label.rowOutput")) + "\n";
                    } else {
                        if (Long.valueOf(rowOutput) <= 0) {
                            err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.greater"),
                                    MessageUtil.getResourceBundleMessage("label.rowOutput"), 0) + "\n";
                        } else if (Long.valueOf(rowOutput) > 99999) {
                            err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.lesser"),
                                    MessageUtil.getResourceBundleMessage("label.rowOutput"), 100000) + "\n";
                        } else {
                            cmdDetail.getCommandTelnetParser().setRowOutput(Long.valueOf(rowOutput));
                        }
                    }
                }

                if (!isNullOrEmpty(columnOutput)) {
                    if (!isInteger(columnOutput)) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.integer"),
                                MessageUtil.getResourceBundleMessage("label.columnOutput")) + "\n";
                    } else {
                        if (Long.valueOf(columnOutput) <= 0) {
                            err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.greater"),
                                    MessageUtil.getResourceBundleMessage("label.columnOutput"), 0) + "\n";
                        } else if (Long.valueOf(columnOutput) > 99999) {
                            err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.lesser"),
                                    MessageUtil.getResourceBundleMessage("label.columnOutput"), 100000) + "\n";
                        } else {
                            cmdDetail.getCommandTelnetParser().setColumnOutput(Long.valueOf(columnOutput));
                        }
                    }
                }

                if (!isNullOrEmpty(countRow)) {
                    if (!isInteger(countRow)) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.integer"),
                                MessageUtil.getResourceBundleMessage("label.countRow")) + "\n";
                    } else {
                        if (Long.valueOf(countRow) <= 0) {
                            err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.greater"),
                                    MessageUtil.getResourceBundleMessage("label.countRow"), 0) + "\n";
                        } else if (Long.valueOf(countRow) > 99999) {
                            err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.lesser"),
                                    MessageUtil.getResourceBundleMessage("label.countRow"), 100000) + "\n";
                        } else {
                            cmdDetail.getCommandTelnetParser().setCountRow(Long.valueOf(countRow));
                        }
                    }
                }

                if (!isNullOrEmpty(splitColumnRegex)) {
                    if (splitColumnRegex.length() > 500) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                                MessageUtil.getResourceBundleMessage("label.splitColumnRegex"), 500) + "\n";
                    } else {
                        cmdDetail.getCommandTelnetParser().setSplitColumnRegex(splitColumnRegex);
                    }
                }

                if (!isNullOrEmpty(regexOutput)) {
                    if (regexOutput.length() > 500) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                                MessageUtil.getResourceBundleMessage("label.regexOutput"), 500) + "\n";
                    } else {
                        cmdDetail.getCommandTelnetParser().setRegexOutput(regexOutput);
                    }
                }

                if (!isNullOrEmpty(pagingCmd)) {
                    if (pagingCmd.length() > 500) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                                MessageUtil.getResourceBundleMessage("label.pagingCmd"), 500) + "\n";
                    } else {
                        cmdDetail.getCommandTelnetParser().setPagingCmd(pagingCmd);
                    }
                }

                if (!isNullOrEmpty(pagingRegex)) {
                    if (pagingRegex.length() > 500) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                                MessageUtil.getResourceBundleMessage("label.pagingRegex"), 500) + "\n";
                    } else {
                        cmdDetail.getCommandTelnetParser().setPagingRegex(pagingRegex);
                    }
                }

                if (!isNullOrEmpty(confirmCmd)) {
                    if (confirmCmd.length() > 500) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                                MessageUtil.getResourceBundleMessage("label.confirmCmd"), 500) + "\n";
                    } else {
                        cmdDetail.getCommandTelnetParser().setConfirmCmd(confirmCmd);
                    }
                }

                if (!isNullOrEmpty(confirmRegex)) {
                    if (confirmRegex.length() > 500) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                                MessageUtil.getResourceBundleMessage("label.confirmRegex"), 500) + "\n";
                    } else {
                        cmdDetail.getCommandTelnetParser().setConfirmRegex(confirmRegex);
                    }
                    if (isNullOrEmpty(confirmCmd)) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                                MessageUtil.getResourceBundleMessage("label.confirmCmd")) + "\n";
                        cmdDetail.getCommandTelnetParser().setConfirmCmd("");
                    }
                }

                if (!isNullOrEmpty(description)) {
                    if (description.length() > 1000) {
                        err += MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                                MessageUtil.getResourceBundleMessage("label.cmdDescription"), 1000) + "\n";
                    } else {
                        cmdDetail.setDescription(description);
                    }
                }

                cmdDetail.setCommandType(0l);
                cmdDetail.getCommandTelnetParser().setRowStartOperator("CONTAIN");
                cmdDetail.getCommandTelnetParser().setRowEndOperator("CONTAIN");

                return err;
            } else {
                return null;
            }
        }
        return null;
    }

    private boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private boolean isNullOrEmpty(Object obj) {
        return obj == null;
    }

    private boolean isInteger(String str) {
        try {
            logger.debug(Integer.parseInt(str));
            return true;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return false;
    }

    private String getCellValue(Cell cell) {
        String result = "";
        switch (cell.getCellType()) {
            case Cell.CELL_TYPE_STRING:
                result = cell.getStringCellValue();
                break;
            case Cell.CELL_TYPE_FORMULA:
                result = cell.getStringCellValue();
                break;

            case Cell.CELL_TYPE_NUMERIC:
                if (DateUtil.isCellDateFormatted(cell)) {
                    result = cell.getDateCellValue().toString();
                } else {
                    Double number = cell.getNumericCellValue();
                    if (Math.round(number) == number) {
                        result = Long.toString(Math.round(number));
                    } else {
                        result = Double.toString(cell.getNumericCellValue());
                    }
                }
                break;

            case Cell.CELL_TYPE_BLANK:
                result = "";
                break;

            case Cell.CELL_TYPE_BOOLEAN:
                result = Boolean.toString(cell.getBooleanCellValue());
                break;
        }
        return result.trim();
    }

    public StreamedContent onExport() {
        try {
            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            orders.put("createTime", "DESC");

            Map<String, Object> filters = getFilter();
            filters.put("userName", SessionWrapper.getCurrentUsername());

            List<CommandDetail> lstCmd = commandDetailService.findList(filters, orders);

            String[] header;
            String[] align;
            if (isShowNodeType ){
                header = new String[]{"vendor.vendorName=label.vendorName",
                        "nodeType.typeName=label.nodeTypeName", "version.versionName=label.versionName", "protocol=label.protocol", "commandTypeStr=label.command.type",
                        "commandName=label.cmdName", "commandTelnetParser.cmd=label.cmd", "operator=label.operator",
                        "standardValue=label.standardValue", "commandTelnetParser.cmdEnd=label.cmdEnd",
                        "commandTelnetParser.cmdTimeout=label.cmdTimeout",
                        "commandTelnetParser.rowStart=label.rowStart", "commandTelnetParser.rowEnd=label.rowEnd",
                        "commandTelnetParser.rowOutput=label.rowOutput",
                        "commandTelnetParser.columnOutput=label.columnOutput",
                        "commandTelnetParser.countRow=label.countRow",
                        "commandTelnetParser.splitColumnRegex=label.splitColumnRegex",
                        "commandTelnetParser.regexOutput=label.regexOutput",
                        "commandTelnetParser.pagingCmd=label.pagingCmd",
                        "commandTelnetParser.pagingRegex=label.pagingRegex",
                        "commandTelnetParser.confirmCmd=label.confirmCmd", "commandTelnetParser.confirmRegex=label.confirmRegex",
                        "description=label.cmdDescription"};

                align = new String[]{
                        "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT",
                        "LEFT", "CENTER", "LEFT", "LEFT", "CENTER",
                        "CENTER", "CENTER", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT",};
            } else {
                header = new String[]{"vendor.vendorName=label.vendorName",
                        "version.versionName=label.versionName", "protocol=label.protocol", "commandTypeStr=label.command.type",
                        "commandName=label.cmdName", "commandTelnetParser.cmd=label.cmd", "operator=label.operator",
                        "standardValue=label.standardValue", "commandTelnetParser.cmdEnd=label.cmdEnd",
                        "commandTelnetParser.cmdTimeout=label.cmdTimeout",
                        "commandTelnetParser.rowStart=label.rowStart", "commandTelnetParser.rowEnd=label.rowEnd",
                        "commandTelnetParser.rowOutput=label.rowOutput",
                        "commandTelnetParser.columnOutput=label.columnOutput",
                        "commandTelnetParser.countRow=label.countRow",
                        "commandTelnetParser.splitColumnRegex=label.splitColumnRegex",
                        "commandTelnetParser.regexOutput=label.regexOutput",
                        "commandTelnetParser.pagingCmd=label.pagingCmd",
                        "commandTelnetParser.pagingRegex=label.pagingRegex",
                        "commandTelnetParser.confirmCmd=label.confirmCmd", "commandTelnetParser.confirmRegex=label.confirmRegex",
                        "description=label.cmdDescription"};

                align = new String[]{
                        "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT",
                        "LEFT", "CENTER", "LEFT", "LEFT", "CENTER",
                        "CENTER", "CENTER", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT", "LEFT",};
            }

            List<AbstractMap.SimpleEntry<String, String>> headerAlign = CommonExport.buildExportHeader(header, align);

            String fileTemplate = CommonExport.getTemplateExport();

            File fileExport = CommonExport.exportFile(lstCmd, headerAlign, "", fileTemplate,
                    "CommandReport", 7, "", 4,
                    MessageUtil.getResourceBundleMessage("title.commandReport"));

            return new DefaultStreamedContent(new FileInputStream(fileExport), ".xlsx", fileExport.getName());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    MessageUtil.getResourceBundleMessage("button.export")));
        }
        return null;
    }

    private ParamInput checkValueInListDTO(List<ParamInput> lst, String value) {
        for (ParamInput dto : lst) {
            if (dto.getParamCode().equals(value)) {
                return dto;
            }
        }
        return null;
    }

    private boolean checkValueInList(List<String> lst, String value) {
        for (String str : lst) {
            if (str.equals(value)) {
                return true;
            }
        }
        return false;
    }

    private boolean isModify(CommandDetail oldCommand, CommandDetail currCommand) {
        if (oldCommand == null || currCommand == null) {
            return true;
        }

        if (isNullOrEmpty(oldCommand.getOperator())) {
            if (!isNullOrEmpty(currCommand.getOperator())) {
                return true;
            }
        } else {
            if (isNullOrEmpty(currCommand.getOperator())) {
                return true;
            } else if (!oldCommand.getOperator().trim().equals(currCommand.getOperator().trim())) {
                return true;
            }
        }

        if (isNullOrEmpty(oldCommand.getProtocol())) {
            if (!isNullOrEmpty(currCommand.getProtocol())) {
                return true;
            }
        } else {
            if (isNullOrEmpty(currCommand.getProtocol())) {
                return true;
            } else if (!oldCommand.getProtocol().trim().equals(currCommand.getProtocol().trim())) {
                return true;
            }
        }

        if (isNullOrEmpty(oldCommand.getStandardValue())) {
            if (!isNullOrEmpty(currCommand.getStandardValue())) {
                return true;
            }
        } else {
            if (isNullOrEmpty(currCommand.getStandardValue())) {
                return true;
            } else if (!oldCommand.getStandardValue().trim().equals(currCommand.getStandardValue().trim())) {
                return true;
            }
        }

        if (isNullOrEmpty(oldCommand.getVendor())) {
            if (!isNullOrEmpty(currCommand.getVendor())) {
                return true;
            }
        } else {
            if (isNullOrEmpty(currCommand.getVendor())) {
                return true;
            } else if (!oldCommand.getVendor().equals(currCommand.getVendor())) {
                return true;
            }
        }

        if (isNullOrEmpty(oldCommand.getVersion())) {
            if (!isNullOrEmpty(currCommand.getVersion())) {
                return true;
            }
        } else {
            if (isNullOrEmpty(currCommand.getVersion())) {
                return true;
            } else if (!oldCommand.getVersion().equals(currCommand.getVersion())) {
                return true;
            }
        }

        if (isShowNodeType) {
            if (isNullOrEmpty(oldCommand.getNodeType())) {
                if (!isNullOrEmpty(currCommand.getNodeType())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getNodeType())) {
                    return true;
                } else if (!oldCommand.getNodeType().equals(currCommand.getNodeType())) {
                    return true;
                }
            }
        }

        if ("telnet".equals(currCommand.getProtocol().toLowerCase())
                || "ssh".equals(currCommand.getProtocol().toLowerCase())) {
            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getCmd())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getCmd())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getCmd())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getCmd().trim().equals(currCommand.getCommandTelnetParser().getCmd().trim())) {
                    return true;
                }
            }

            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getCmdEnd())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getCmdEnd())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getCmdEnd())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getCmdEnd().trim().equals(currCommand.getCommandTelnetParser().getCmdEnd().trim())) {
                    return true;
                }
            }

            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getCmdTimeout())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getCmdTimeout())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getCmdTimeout())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getCmdTimeout().equals(currCommand.getCommandTelnetParser().getCmdTimeout())) {
                    return true;
                }
            }

            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getRowStart())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getRowStart())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getRowStart())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getRowStart().trim().equals(currCommand.getCommandTelnetParser().getRowStart().trim())) {
                    return true;
                }
            }

            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getRowEnd())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getRowEnd())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getRowEnd())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getRowEnd().trim().equals(currCommand.getCommandTelnetParser().getRowEnd().trim())) {
                    return true;
                }
            }

            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getRowOutput())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getRowOutput())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getRowOutput())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getRowOutput().equals(currCommand.getCommandTelnetParser().getRowOutput())) {
                    return true;
                }
            }

            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getColumnOutput())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getColumnOutput())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getColumnOutput())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getColumnOutput().equals(currCommand.getCommandTelnetParser().getColumnOutput())) {
                    return true;
                }
            }

            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getCountRow())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getCountRow())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getCountRow())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getCountRow().equals(currCommand.getCommandTelnetParser().getCountRow())) {
                    return true;
                }
            }

            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getSplitColumnRegex())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getSplitColumnRegex())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getSplitColumnRegex())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getSplitColumnRegex().trim().equals(currCommand.getCommandTelnetParser().getSplitColumnRegex().trim())) {
                    return true;
                }
            }

            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getRegexOutput())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getRegexOutput())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getRegexOutput())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getRegexOutput().trim().equals(currCommand.getCommandTelnetParser().getRegexOutput().trim())) {
                    return true;
                }
            }

            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getPagingCmd())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getPagingCmd())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getPagingCmd())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getPagingCmd().trim().equals(currCommand.getCommandTelnetParser().getPagingCmd().trim())) {
                    return true;
                }
            }

            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getPagingRegex())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getPagingRegex())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getPagingRegex())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getPagingRegex().trim().equals(currCommand.getCommandTelnetParser().getPagingRegex().trim())) {
                    return true;
                }
            }
        } else if ("sql".equals(currCommand.getProtocol().toLowerCase())) {
            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getCmd())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getCmd())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getCmd())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getCmd().trim().equals(currCommand.getCommandTelnetParser().getCmd().trim())) {
                    return true;
                }
            }

            if (isNullOrEmpty(oldCommand.getCommandTelnetParser().getColumnOutput())) {
                if (!isNullOrEmpty(currCommand.getCommandTelnetParser().getColumnOutput())) {
                    return true;
                }
            } else {
                if (isNullOrEmpty(currCommand.getCommandTelnetParser().getColumnOutput())) {
                    return true;
                } else if (!oldCommand.getCommandTelnetParser().getColumnOutput().equals(currCommand.getCommandTelnetParser().getColumnOutput())) {
                    return true;
                }
            }
        }
        return false;
    }

    private void getParser(CommandTelnetParser parser, CommandDetail obj) {
        parser.setCmd(obj.getCommandTelnetParser().getCmd() == null ? "" : obj.getCommandTelnetParser().getCmd().trim());
        parser.setCmdEnd(obj.getCommandTelnetParser().getCmdEnd() == null ? "" : obj.getCommandTelnetParser().getCmdEnd().trim());
        parser.setCmdTimeout(obj.getCommandTelnetParser().getCmdTimeout());
        parser.setColumnOutput(obj.getCommandTelnetParser().getColumnOutput());
        parser.setCountRow(obj.getCommandTelnetParser().getCountRow());
        parser.setRowOutput(obj.getCommandTelnetParser().getRowOutput());
        parser.setPagingCmd(obj.getCommandTelnetParser().getPagingCmd() == null ? "" : obj.getCommandTelnetParser().getPagingCmd().trim());
        parser.setPagingRegex(obj.getCommandTelnetParser().getPagingRegex() == null ? "" : obj.getCommandTelnetParser().getPagingRegex().trim());
        parser.setRegexOutput(obj.getCommandTelnetParser().getRegexOutput() == null ? "" : obj.getCommandTelnetParser().getRegexOutput().trim());
        parser.setRowEnd(obj.getCommandTelnetParser().getRowEnd() == null ? "" : obj.getCommandTelnetParser().getRowEnd().trim());
        parser.setRowStart(obj.getCommandTelnetParser().getRowStart() == null ? "" : obj.getCommandTelnetParser().getRowStart().trim());
        parser.setSplitColumnRegex(obj.getCommandTelnetParser().getSplitColumnRegex() == null ? "" : obj.getCommandTelnetParser().getSplitColumnRegex().trim());
        parser.setRowStartOperator(obj.getCommandTelnetParser().getRowStartOperator());
        parser.setRowEndOperator(obj.getCommandTelnetParser().getRowEndOperator());
        parser.setConfirmCmd(obj.getCommandTelnetParser().getConfirmCmd() == null ? "" : obj.getCommandTelnetParser().getConfirmCmd().trim());
        parser.setConfirmRegex(obj.getCommandTelnetParser().getConfirmRegex() == null ? "" : obj.getCommandTelnetParser().getConfirmRegex().trim());
    }

    private boolean validateInput(boolean isEdit) {
        /*if ((vendorSelected == null && isEdit) || (!isEdit && (vendorSeleteds == null || vendorSeleteds.isEmpty()))) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                    MessageUtil.getResourceBundleMessage("label.vendorName")));
            return false;
        }*/

       /* if ((nodeTypeSelected == null && isEdit) || (!isEdit && (nodeTypeSelecteds == null || nodeTypeSelecteds.isEmpty()))) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                    MessageUtil.getResourceBundleMessage("label.nodeTypeName")));
            return false;
        }*/

        /*if ((versionSelected == null && isEdit) || (!isEdit && (versionSelecteds == null || versionSelecteds.isEmpty()))) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                    MessageUtil.getResourceBundleMessage("label.versionName")));
            return false;
        }*/

        if (obj.getCommandType() == null || obj.getCommandType() == -1) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                    MessageUtil.getResourceBundleMessage("label.command.type")));
            return false;
        }

      /*  if (opeSelected == null) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                    MessageUtil.getResourceBundleMessage("label.operator")));
            return false;
        }
*/
        if (obj.getCommandName() == null || obj.getCommandName().trim().isEmpty()) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                    MessageUtil.getResourceBundleMessage("label.cmdName")));
            return false;
        }

        if (obj.getCommandName().length() > 500) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                    MessageUtil.getResourceBundleMessage("label.cmdName"), 500));
            return false;
        }

        if (protocolSelected == null) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                    MessageUtil.getResourceBundleMessage("label.protocol")));
            return false;
        }

        if (obj.getCommandTelnetParser().getCmd() == null || obj.getCommandTelnetParser().getCmd().trim().isEmpty()) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                    MessageUtil.getResourceBundleMessage("label.cmd")));
            return false;
        }

        if (obj.getCommandTelnetParser().getCmd().length() > 2000) {
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                    MessageUtil.getResourceBundleMessage("label.cmd"), 2000));
            return false;
        }

//        if (obj.getCommandTelnetParser().getCmdEnd() == null || obj.getCommandTelnetParser().getCmdEnd().trim().isEmpty()) {
//            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
//                    MessageUtil.getResourceBundleMessage("label.cmdEnd")));
//            return false;
//        }

        if (Config.PROTOCOL_TELNET.equals(protocolSelected.getValue())
                || Config.PROTOCOL_SSH.equals(protocolSelected.getValue())) {
            if (!isNullOrEmpty(obj.getCommandTelnetParser().getCmdEnd()) && obj.getCommandTelnetParser().getCmdEnd().length() > 100) {
                MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.maxLength"),
                        MessageUtil.getResourceBundleMessage("label.cmdEnd"), 100));
                return false;
            }
        }

        switch (opeSelected.getValue().toUpperCase()) {
            case "IN":
            case "NOT IN":
            case "=":
            case "<":
            case ">":
            case "<=":
            case ">=":
            case "<>":
                if (isNullOrEmpty(obj.getStandardValue())) {
                    MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.standardValue")));
                    return false;
                }
                break;
            case "CONTAIN":
            case "IS NULL OR CONTAIN":
            case "NOT CONTAIN":
                if (isNullOrEmpty(obj.getStandardValue())) {
                    MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.standardValue")));
                    return false;
                }
                break;
            case "BETWEEN":
                if (isNullOrEmpty(obj.getStandardValue())) {
                    MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.standardValue")));
                    return false;
                }

                if (!obj.getStandardValue().trim().contains(",")) {
                    MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("label.standardValue")
                            + MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.between"));
                    return false;
                }

                String[] str = obj.getStandardValue().trim().split(",");
                if (str.length != 2) {
                    MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("label.standardValue")
                            + MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.between"));
                    return false;
                }

                try {
                    logger.debug(Double.parseDouble(str[0]));
                    logger.debug(Double.parseDouble(str[1]));
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("label.standardValue")
                            + MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.between"));
                    return false;
                }
                break;
            case "LIKE":
            case "NOT LIKE":
                if (isNullOrEmpty(obj.getStandardValue())) {
                    MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                            MessageUtil.getResourceBundleMessage("label.standardValue")));
                    return false;
                }
                if (!obj.getStandardValue().trim().contains("%")) {
                    MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("label.standardValue")
                            + MessageUtil.getResourceBundleMessage("paramMngt.compOpe.format.like"));
                    return false;
                }
                break;
        }

        return true;
    }

    private void clear() {
        obj = new CommandDetail();

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
        params.clear();

        standardValuePrompt = "";
        standardValueReadonly = false;
        standardValueRequired = false;
    }

    public void prepareEdit(CommandDetail vCmd) {
        try {
            clear();

            obj = vCmd;
            oldObj = new Cloner().deepClone(vCmd);
            isEdit = true;
            isClone = false;
            vendorSelected = vCmd.getVendor();
            nodeTypeSelected = vCmd.getNodeType();
            versionSelected = vCmd.getVersion();
            opeSelected = new ComboBoxObject(vCmd.getOperator(), vCmd.getOperator());
            protocolSelected = new ComboBoxObject(vCmd.getProtocol(), vCmd.getProtocol());

            params = new ArrayList(vCmd.getParamInputs());

//            Collections.sort(params, new Comparator<ParamInput>() {
//                @Override
//                public int compare(ParamInput a, ParamInput b) {
//                    return a.getParamCode().compareTo(b.getParamCode());
//                }
//            });

            onChangeOperator(vCmd.getOperator());

            dialogHeader = MessageUtil.getResourceBundleMessage("title.updateCommand");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void prepareDelete(CommandDetail vCmd) {
        try {
            obj = vCmd;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void prepareClone(CommandDetail vCmd) {
        try {
            clear();

            obj = vCmd;
            isEdit = true;
            isClone = true;

            vendorSelected = vCmd.getVendor();
            nodeTypeSelected = vCmd.getNodeType();
            versionSelected = vCmd.getVersion();
            opeSelected = new ComboBoxObject(vCmd.getOperator(), vCmd.getOperator());
            protocolSelected = new ComboBoxObject(vCmd.getProtocol(), vCmd.getProtocol());

            params = new ArrayList(vCmd.getParamInputs());

//            Collections.sort(params, new Comparator<ParamInput>() {
//                @Override
//                public int compare(ParamInput a, ParamInput b) {
//                    return a.getParamCode().compareTo(b.getParamCode());
//                }
//            });

            onChangeOperator(vCmd.getOperator());

            dialogHeader = MessageUtil.getResourceBundleMessage("title.cloneCommand");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    //20181023_tudn_start load pass security
    public void onChangeNodeCheckCmd() {
        try {
            if (commandSend.getAccount() != null && !"".equals(commandSend.getAccount())) {
                String accType = null;
                if(commandSend.getNodeRun() !=null && commandSend.getNodeRun().getNodeAccount() !=null && commandSend.getNodeRun().getNodeAccount().getAccountType()!=null  ) {
                    if (Constant.ACCOUNT_TYPE_SERVER.equalsIgnoreCase(commandSend.getNodeRun().getNodeAccount().getAccountType().toString())) {
                        accType = Constant.SECURITY_SERVER;
                    } else if (Constant.ACCOUNT_TYPE_DATABASE.equalsIgnoreCase(commandSend.getNodeRun().getNodeAccount().getAccountType().toString())) {
                        accType = Constant.SECURITY_DATABASE;
                    }
                }

                commandSend.setPassword(PassProtector.decrypt(commandSend.getPassword(), Config.SALT));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void onSelectNode(SelectEvent event) {
        Node node = (Node) event.getObject();
        commandSend.setNodeRun(node);
    }
    //20181023_tudn_end load pass security

    public void prepareCheck(CommandDetail vCmd) {
        commandSend = new CommandObject();
        try {
            commandSend.setCmd(vCmd.getCommandTelnetParser().getCmd());
            commandSend.setCmdSend(vCmd.getCommandTelnetParser().getCmd());
            commandSend.setCommandDetail(vCmd);
            params = new ArrayList(vCmd.getParamInputs());
            for (ParamInput pr : params) {
                pr.setParamValue("");
            }

            commandSend.setFilterNodeTypeId(vCmd.getNodeType().getTypeId());
            commandSend.setFilterVendorId(vCmd.getVendor().getVendorId());
            commandSend.setFilterVersionId(vCmd.getVersion().getVersionId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String buildCommand() {
        if (commandSend == null) {
            return "";
        }
        String cmdFormat = commandSend.getCmd();
        String cmd = commandSend.getCmd();
        try {
            for (ParamInput paramInput : params) {
                String value = "";
                if (paramInput.getParamValue() != null && !paramInput.getParamValue().trim().isEmpty()) {
                    value = paramInput.getParamValue();
                }
                cmd = cmd.replace("@{" + paramInput.getParamCode() + "}", value);
                cmdFormat = cmdFormat.replace("@{" + paramInput.getParamCode() + "}", "<b style=\"background: yellow\">" + value + "</b>");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        commandSend.setCmdSend(cmd);
        return cmdFormat;
    }

    public void checkCommand() {
        try {
            boolean isValid = true;

            if (commandSend.getNodeRun() == null) {
                MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                        MessageUtil.getResourceBundleMessage("label.node.run")));
                isValid = false;
            }

            if (commandSend.getAccount() == null || commandSend.getAccount().trim().isEmpty()) {
                MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                        MessageUtil.getResourceBundleMessage("label.account")));
                isValid = false;
            }

            if (commandSend.getPassword() == null || commandSend.getPassword().trim().isEmpty()) {
                MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
                        MessageUtil.getResourceBundleMessage("label.pass")));
                isValid = false;
            }

//            for (ParamInput pr : params) {
//                if (pr.getParamValue() == null || pr.getParamValue().trim().isEmpty()) {
//                    MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.required"),
//                            pr.getParamCode()));
//                    isValid = false;
//                }
//            }
            logger.info("commandSend.getCmdSend(): " + commandSend.getCmdSend());

            if (!isValid) {
                return;
            }

            String prompt = commandSend.getCommandDetail().getCommandTelnetParser().getCmdEnd();

            List<String> paramValue = new ArrayList<>();
            int timeout = commandSend.getCommandDetail().getCommandTelnetParser().getCmdTimeout() == null
                    || commandSend.getCommandDetail().getCommandTelnetParser().getCmdTimeout() <= 0
                    ? 30000 : commandSend.getCommandDetail().getCommandTelnetParser().getCmdTimeout().intValue() * 1000;
            String resultDetail;
            switch (commandSend.getCommandDetail().getProtocol().toUpperCase()) {
                case Config.PROTOCOL_TELNET:
                    resultDetail = getValueTelnet(commandSend.getAccount(), commandSend.getPassword(),
                            commandSend.getNodeRun().getNodeIp(),
                            commandSend.getNodeRun().getPort() == null ? 23 : commandSend.getNodeRun().getPort(), prompt, "exit", "VT100", commandSend.getCmdSend(), timeout,
                            commandSend.getCommandDetail().getCommandTelnetParser().getCmdEnd(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getPagingCmd(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getPagingRegex(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getConfirmCmd(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getConfirmRegex(),
                            commandSend.getNodeRun().getVendor().getVendorName(), commandSend.getNodeRun().getOsType(),
                            commandSend.getNodeRun().getVersion().getVersionName(),
                            commandSend.getNodeRun().getNodeType().getTypeName());

                    commandSend.setResultDetail(resultDetail);

                    paramValue = getValueRowColumnIndex(resultDetail,
                            commandSend.getCommandDetail().getCommandTelnetParser().getRowOutput() == null ? 0 : commandSend.getCommandDetail().getCommandTelnetParser().getRowOutput().intValue(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getColumnOutput() == null ? 0 : commandSend.getCommandDetail().getCommandTelnetParser().getColumnOutput().intValue(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getCountRow() == null ? 0 : commandSend.getCommandDetail().getCommandTelnetParser().getCountRow().intValue(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getRowStart(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getRowStartOperator(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getRowEnd(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getRowEndOperator(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getSplitColumnRegex(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getRegexOutput());
                    break;
                case Config.PROTOCOL_SSH:
                    resultDetail = getValueSsh(commandSend.getAccount(), commandSend.getPassword(),
                            commandSend.getNodeRun().getNodeIp(),
                            22, commandSend.getCmdSend(), timeout, prompt,
                            commandSend.getCommandDetail().getCommandTelnetParser().getPagingRegex(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getPagingCmd());

                    commandSend.setResultDetail(resultDetail);

                    paramValue = getValueRowColumnIndex(resultDetail,
                            commandSend.getCommandDetail().getCommandTelnetParser().getRowOutput() == null ? 0 : commandSend.getCommandDetail().getCommandTelnetParser().getRowOutput().intValue(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getColumnOutput() == null ? 0 : commandSend.getCommandDetail().getCommandTelnetParser().getColumnOutput().intValue(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getCountRow() == null ? 0 : commandSend.getCommandDetail().getCommandTelnetParser().getCountRow().intValue(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getRowStart(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getRowStartOperator(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getRowEnd(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getRowEndOperator(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getSplitColumnRegex(),
                            commandSend.getCommandDetail().getCommandTelnetParser().getRegexOutput());
                    break;
                case Config.PROTOCOL_SQL:
                    try {
                        paramValue = getValueSql(commandSend.getAccount(), commandSend.getPassword()
                                , commandSend.getNodeRun().getJdbcUrl(), commandSend.getCommandDetail().getProtocol(), commandSend.getCmdSend(),
                                commandSend.getCommandDetail().getCommandTelnetParser().getColumnOutput() == null
                                        ? 0 : commandSend.getCommandDetail().getCommandTelnetParser().getColumnOutput().intValue()
                                , params);
                    } catch (Exception ex) {
                        commandSend.setResultDetail(ex.getMessage());
                        throw ex;
                    }
                    break;
                case Config.PROTOCOL_EXCHANGE:
                    break;
            }

            commandSend.setResult(StringUtils.join(paramValue, "\n"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            if ("CONN_TIMEOUT".equals(e.getMessage())) {
                MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("message.login.fail"));
            } else if ("LOGIN_FAIL".equals(e.getMessage())) {
                MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("message.connect.fail"));
            } else {
                MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("message.error.run"));
            }
        }
    }

    public void prepareInsert() {
        try {
            clear();

            if (!isShowNodeType) {
                nodeTypeSelecteds.add(nodeTypeService.get(-1l));
            }
            protocolSelected = new ComboBoxObject(Config.PROTOCOL_TELNET, Config.PROTOCOL_TELNET);

            isEdit = false;
            isClone = false;

            dialogHeader = MessageUtil.getResourceBundleMessage("title.insertCommand");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private List<String> getParamList(String commandPattern) {
        List<String> lstParam = new ArrayList<>();

        if (commandPattern != null && !commandPattern.trim().isEmpty() && commandPattern.contains("@{")) {
            int preIndex = 0, endIndex;
            while (true) {
                preIndex = commandPattern.indexOf("@{", preIndex);

                if (preIndex < 0) {
                    break;
                } else {
                    preIndex += 2;
                }

                endIndex = commandPattern.indexOf("}", preIndex);

                if (endIndex < 0) {
                    break;
                } else {
                    lstParam.add(commandPattern.substring(preIndex, endIndex));
                    preIndex = endIndex;
                }
            }
        }

        return lstParam;
    }

    private List<String> getValueRowColumnIndex(String content, int row, int column,
                                                int countRow, String rowHeader, String rowHeaderOperator, String rowFooter, String rowFooterOperator,
                                                String splitColumnChar, String regex) {
        BufferedReader bf = null;
        StringReader sr = null;
        try {
            sr = new StringReader(content);
            bf = new BufferedReader(sr);

            String line = "";
            int i = 1;
            int count = 0;
            List<String> lstValue = new ArrayList<>();
            boolean isHeader = false;
            while ((line = bf.readLine()) != null) {
                if (rowHeader != null && !rowHeader.trim().isEmpty()) {
                    switch (rowHeaderOperator) {
                        case "CONTAIN":
                            if (line.trim().contains(rowHeader)) {
                                isHeader = true;
                            }
                            break;
                        case "START WITH":
                            if (line.trim().startsWith(rowHeader)) {
                                isHeader = true;
                            }
                            break;
                        case "END WITH":
                            if (line.trim().endsWith(rowHeader)) {
                                isHeader = true;
                            }
                            break;
                    }
                    if (isHeader) {
                        break;
                    }
                } else {
                    i = 1;
                    break;
                }
            }

            do {
                if (line == null) {
                    break;
                }
                boolean isFooter = false;
                if (!isNullOrEmpty(rowFooter)) {
                    switch (rowFooterOperator) {
                        case "CONTAIN":
                            if (line.trim().contains(rowFooter)) {
                                isFooter = true;
                            }
                            break;
                        case "START WITH":
                            if (line.trim().startsWith(rowFooter)) {
                                isFooter = true;
                            }
                            break;
                        case "END WITH":
                            if (line.trim().endsWith(rowFooter)) {
                                isFooter = true;
                            }
                            break;
                    }
                    if (isFooter) {
                        break;
                    }
                }
                if (row <= i) {
                    if (countRow > 0) {
                        count++;
                        if (count <= countRow) {
                            if (isNullOrEmpty(splitColumnChar)) {
                                String vl = getValueRegex(line.trim(), regex);

                                if (!isNullOrEmpty(vl) && !lstValue.contains(vl)) {
                                    lstValue.add(vl);
                                }
                            } else {
                                String[] arr = line.trim().split(splitColumnChar);
                                if (arr != null && arr.length >= column) {
                                    String vl = getValueRegex(arr[column - 1], regex);

                                    if (!isNullOrEmpty(vl) && !lstValue.contains(vl)) {
                                        lstValue.add(vl);
                                    }
                                }
                            }
                        } else {
                            break;
                        }
                    } else {
//                        if (line.trim().isEmpty() && isNullOrEmpty(rowFooter)) {
//                            break;
//                        } else {
                        if (isNullOrEmpty(splitColumnChar)) {
                            String vl = getValueRegex(line.trim(), regex);

                            if (!isNullOrEmpty(vl) && !lstValue.contains(vl)) {
                                lstValue.add(vl);
                            }
                        } else {
                            String[] arr = line.trim().split(splitColumnChar);
                            if (arr != null && arr.length >= column) {
                                String vl = getValueRegex(arr[column - 1], regex);

                                if (!isNullOrEmpty(vl) && !lstValue.contains(vl)) {
                                    lstValue.add(vl);
                                }
                            }
                        }
//                        }
                    }
                }
                i++;
            } while ((line = bf.readLine()) != null);

            return lstValue;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (sr != null) {
                sr.close();
            }

            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
        return new ArrayList<>();
    }

    private String getValueRegex(String content, String regex) {
        String value = null;
        try {
            if (isNullOrEmpty(regex) || isNullOrEmpty(content)) {
                return content;
            }
            Pattern patRegex = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            Matcher matcher = patRegex.matcher(content);
            if (matcher.find()) {
                value = matcher.group(1);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return value;
    }

    private List<String> getValueSql(String username, String pass, String url, String protocol, String command,
                                     int column, List<ParamInput> params) throws Exception {
        Connection conn = null;
        NamedParameterStatement pst = null;
        ResultSet rs = null;

        try {
            ConnectionPool pool = new ConnectionPool(protocol, url, username, pass);
            conn = pool.reserveConnection();

            pst = new NamedParameterStatement(conn, command);

//            if (params != null & !params.isEmpty()) {
//                for (ParamInput param : params) {
//                    pst.setString(param.getParamCode(), param.getParamValue());
//                }
//            }

            List<String> lstValue = new ArrayList<>();
            if (isImpactCmdSql(" " + command.toLowerCase())) {
                lstValue.add(pst.executeUpdate() + "");
            } else {
                rs = pst.executeQuery();

                if (rs.getMetaData().getColumnCount() >= 1) {
                    while (rs.next()) {
                        lstValue.add(rs.getString(column));
                    }
                }
            }
            return lstValue;
        } catch (Exception ex) {
            throw ex;
        } finally {
            try {
                if (rs != null) {
                    rs.close();
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }

            try {
                if (pst != null) {
                    pst.close();
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }

            try {
                if (conn != null) {
                    conn.close();
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }
    }

    private boolean isImpactCmdSql(String cmd) {
        String[] keywords = new String[]{" update ", " delete ", " insert ", " alter ", " drop ",
                " create ", " grant ", " revoke ", " analyze ", " audit ", " comment "};

        for (String keyword : keywords) {
            if (cmd.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    public static String getValueSsh(String username, String pass, String ip, int port, String command,
                                     int timeout, String cmdEnd, String morePrompt, String moreCommand) throws Exception {
        JSchSshUtil sshClient = null;
        try {
            try {
                sshClient = new JSchSshUtil(ip, port, username, pass, cmdEnd, "\r\n", timeout, false, "N/A", "N/A", "N/A");
                sshClient.connect();
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                throw new Exception("LOGIN_FAIL");
            }
            if (moreCommand == null || moreCommand.trim().isEmpty()) {
                moreCommand = " ";
            }
            Result result = (cmdEnd == null || cmdEnd.trim().isEmpty()) ?
                    ((morePrompt == null || morePrompt.trim().isEmpty()) ? sshClient.sendLineWithTimeOutAdvance(command, timeout) : sshClient.sendLineWithMore(command, morePrompt, moreCommand, timeout)) :
                    ((morePrompt == null || morePrompt.trim().isEmpty()) ? sshClient.sendLineWithTimeOutAdvance(command, timeout, cmdEnd) : sshClient.sendLineWithMore(command, morePrompt, moreCommand, timeout, cmdEnd));

            logger.info(result != null ? result.getResult() : "");

            if (result != null && result.isSuccessSent()) {
                String content = result.getResult();

                String firstLine = content.contains("\n") ? content.substring(0, content.indexOf("\n")) : null;
                if (firstLine != null) {
                    content = content.substring(content.indexOf("\n"));
                }

                if (content.lastIndexOf("\n") > 0) {
                    content = content.substring(0, content.lastIndexOf("\n"));
                } else if (content.endsWith(cmdEnd)) {
                    content = "";
                }

                return content.trim();
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (sshClient != null) {
                sshClient.disconnect();
            }
        }
        return "";
    }

    private String getValueTelnet(String username, String pass, String ip, int port, String prompt,
                                  String cmdExit, String terminator, String command, int timeout, String cmdEnd,
                                  String pagingCmd, String pagingRegex, String confirmCmd, String confirmRegex,
                                  String vendor, String osType, String version, String nodeType) throws Exception {

        TelnetClientUtil telnetClient = null;
        try {
            switch (vendor.toLowerCase()) {
                case "tekelec":
                    telnetClient = new TelnetStpTekelec(ip, port, vendor);
                    telnetClient.setShellPromt("(>|#)");
                    break;
                case "nokia":
                    telnetClient = new TelnetNokia(ip, port, vendor);
                    break;
                case "huawei":
                    telnetClient = new TelnetHuawei(ip, port, vendor, version);
                    break;
                case "ericsson":
                    telnetClient = new TelnetEricsson(ip, port, vendor, osType, nodeType);
                    break;
                default:
                    telnetClient = new TelnetClientUtil(ip, port, vendor);
                    telnetClient.setShellPromt("(>|#)");
                    break;
            }
//            telnetClient.setCmdExit(cmdExit);
            telnetClient.setTerminalType(terminator);
            telnetClient.setTimeoutDefault(timeout);

            String strLogin = "";
            try {
                strLogin = telnetClient.connect(username, pass).replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
            } catch (Exception ex) {
                if (!"CONN_TIMEOUT".equals(ex.getMessage())) {
                    throw new Exception("LOGIN_FAIL");
                } else {
                    throw ex;
                }
            }

//            if (!strLogin.endsWith(prompt)) {
//                throw new Exception("LOGIN_FAIL");
//            }
//            if (strLogin != null && strLogin.contains("\n")) {
//                String loginPrompt = strLogin.substring(strLogin.lastIndexOf("\n"));
//                telnetClient.setShellPromt(loginPrompt);
//
//                logger.info("loginPrompt: " + loginPrompt);
//            }
            logger.info("loginPrompt: " + telnetClient.getPrompt());

            if (!isNullOrEmpty(prompt)) {
                telnetClient.setShellPromt(prompt);
            }

            String content;
            if (isNullOrEmpty(pagingRegex) && isNullOrEmpty(confirmRegex)) {
                content = telnetClient.sendWait(command, cmdEnd, true, timeout);
            } else if (isNullOrEmpty(confirmRegex) && !isNullOrEmpty(pagingRegex)) {
                if (pagingCmd == null) {
                    pagingCmd = " ";
                }
                content = telnetClient.sendWaitHasMore(command, cmdEnd, pagingRegex, pagingCmd, timeout);
            } else {
                if (pagingCmd == null) {
                    pagingCmd = " ";
                }
                logger.info("vao day roi ne");
                content = telnetClient.sendWaitHasConfirm(command, cmdEnd, confirmRegex, confirmCmd, pagingRegex, pagingCmd, timeout);
            }

            if (content == null) {
                return null;
            }

            //content = content.replaceAll("[\\p{Cntrl}&&[^\r\n\t]]", "");
            String firstLine = content.contains("\n") ? content.substring(0, content.indexOf("\n")) : null;
            if (firstLine != null) {
                //firstLine = firstLine.replace(" [1D", "");

                //if (firstLine.contains(command)) {
                content = content.substring(content.indexOf("\n"));
                //}
            }
            logger.info("firstLine: " + firstLine + ", content: " + content);

            if (content.lastIndexOf("\n") > 0) {
                content = content.substring(0, content.lastIndexOf("\n"));
            } else if (telnetClient.checkRegex(content, isNullOrEmpty(cmdEnd) ? telnetClient.getPrompt() : cmdEnd)) {
                content = "";
            }

            return content.trim();
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (telnetClient != null) {
                try {
                    TelnetClientUtil.disConnect(telnetClient);
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    private void onChangeOperator(String value) {
        switch (value) {
            case "IS NULL":
            case "NOT NULL":
            case "NO CHECK":
                standardValueReadonly = true;
                obj.setStandardValue("");
                break;
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

    public List<Node> autoCompleNode(String nodeCode) {
        List<Node> lstNode;
        Map<String, Object> filters = new HashMap<>();
        filters.put("version.versionId", commandSend.getFilterVersionId());
        filters.put("vendor.vendorId", commandSend.getFilterVendorId());
        filters.put("active", Constant.status.active);
        if (isShowNodeType) {
            filters.put("nodeType.typeId", commandSend.getFilterNodeTypeId());
        }
        if (nodeCode != null) {
            filters.put("nodeCode", nodeCode.trim());
        }
        try {
            lstNode = nodeService.findList(0, 100, filters, null);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            lstNode = new ArrayList<>();
        }
        return lstNode;
    }

    public ComboBoxObject getProtocolSelected() {
        return protocolSelected;
    }

    public void setProtocolSelected(ComboBoxObject protocolSelected) {
        this.protocolSelected = protocolSelected;
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

    public CommandDetailServiceImpl getCommandDetailService() {
        return commandDetailService;
    }

    public void setCommandDetailService(CommandDetailServiceImpl commandDetailService) {
        this.commandDetailService = commandDetailService;
    }

    public LazyDataModel<CommandDetail> getLazyModel() {
        return lazyModel;
    }

    public void setLazyModel(LazyDataModel<CommandDetail> lazyModel) {
        this.lazyModel = lazyModel;
    }

    public CommandDetail getObj() {
        return obj;
    }

    public void setObj(CommandDetail obj) {
        this.obj = obj;
    }

    public CommandTelnetParserServiceImpl getCommandTelnetParserService() {
        return commandTelnetParserService;
    }

    public void setCommandTelnetParserService(CommandTelnetParserServiceImpl commandTelnetParserService) {
        this.commandTelnetParserService = commandTelnetParserService;
    }

    public boolean isIsEdit() {
        return isEdit;
    }

    public void setIsEdit(boolean isEdit) {
        this.isEdit = isEdit;
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

    public List<ParamInput> getParams() {
        return params;
    }

    public void setParams(List<ParamInput> params) {
        this.params = params;
    }

    public String getDialogHeader() {
        return dialogHeader;
    }

    public void setDialogHeader(String dialogHeader) {
        this.dialogHeader = dialogHeader;
    }

    public ParamInputServiceImpl getParamInputService() {
        return paramInputService;
    }

    public void setParamInputService(ParamInputServiceImpl paramInputService) {
        this.paramInputService = paramInputService;
    }

    public ActionCommandServiceImpl getActionCommandService() {
        return actionCommandService;
    }

    public void setActionCommandService(ActionCommandServiceImpl actionCommandService) {
        this.actionCommandService = actionCommandService;
    }

    public boolean getHasError() {
        return hasError;
    }

    public void setHasError(boolean hasError) {
        this.hasError = hasError;
    }

    public List<Version> getVersions() {
        return versions;
    }

    public void setVersions(List<Version> versions) {
        this.versions = versions;
    }

    public VersionServiceImpl getVersionService() {
        return versionService;
    }

    public void setVersionService(VersionServiceImpl versionService) {
        this.versionService = versionService;
    }

    public Version getVersionSelected() {
        return versionSelected;
    }

    public void setVersionSelected(Version versionSelected) {
        this.versionSelected = versionSelected;
    }

    public List<ComboBoxObject> getOperators() {
        return operators;
    }

    public void setOperators(List<ComboBoxObject> operators) {
        this.operators = operators;
    }

    public ComboBoxObject getOpeSelected() {
        return opeSelected;
    }

    public void setOpeSelected(ComboBoxObject opeSelected) {
        this.opeSelected = opeSelected;
    }

    public CommandObject getCommandSend() {
        return commandSend;
    }

    public void setCommandSend(CommandObject commandSend) {
        this.commandSend = commandSend;
    }

    public List<Node> getNodeRuns() {
        return nodeRuns;
    }

    public void setNodeRuns(List<Node> nodeRuns) {
        this.nodeRuns = nodeRuns;
    }

    public NodeServiceImpl getNodeService() {
        return nodeService;
    }

    public void setNodeService(NodeServiceImpl nodeService) {
        this.nodeService = nodeService;
    }

    public ParamGroupServiceImpl getParamGroupService() {
        return paramGroupService;
    }

    public void setParamGroupService(ParamGroupServiceImpl paramGroupService) {
        this.paramGroupService = paramGroupService;
    }

    public ParamValueServiceImpl getParamValueService() {
        return paramValueService;
    }

    public void setParamValueService(ParamValueServiceImpl paramValueService) {
        this.paramValueService = paramValueService;
    }

    public boolean isStandardValueRequired() {
        return standardValueRequired;
    }

    public void setStandardValueRequired(boolean standardValueRequired) {
        this.standardValueRequired = standardValueRequired;
    }

    public String getStandardValuePrompt() {
        return standardValuePrompt;
    }

    public void setStandardValuePrompt(String standardValuePrompt) {
        this.standardValuePrompt = standardValuePrompt;
    }

    public boolean isStandardValueReadonly() {
        return standardValueReadonly;
    }

    public void setStandardValueReadonly(boolean standardValueReadonly) {
        this.standardValueReadonly = standardValueReadonly;
    }

    public List<Version> getVersionSelecteds() {
        return versionSelecteds;
    }

    public void setVersionSelecteds(List<Version> versionSelecteds) {
        this.versionSelecteds = versionSelecteds;
    }

    public FlowTemplatesServiceImpl getFlowTemplatesService() {
        return flowTemplatesService;
    }

    public void setFlowTemplatesService(FlowTemplatesServiceImpl flowTemplatesService) {
        this.flowTemplatesService = flowTemplatesService;
    }

    public List<ComboBoxObject> getProtocols() {
        return protocols;
    }

    public void setProtocols(List<ComboBoxObject> protocols) {
        this.protocols = protocols;
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

    public List<ComboBoxObject> getNoCheckCbb() {
        return noCheckCbb;
    }

    public void setNoCheckCbb(List<ComboBoxObject> noCheckCbb) {
        this.noCheckCbb = noCheckCbb;
    }

    public Map<String, String> getMapParamDefaultVal() {
        return mapParamDefaultVal;
    }

    public void setMapParamDefaultVal(Map<String, String> mapParamDefaultVal) {
        this.mapParamDefaultVal = mapParamDefaultVal;
    }

//    public static void main(String args[]) {
//        try {
////            System.out.println(PasswordEncoder.decrypt("t2PEDleyKBSfBkOYABmNGQ=="));
////            System.out.println(PasswordEncoder.decrypt("K4m7HMHP71MdfZQQzuatsPWeHPNaQtRG"));
////
//
//                // dong bo cau lenh và action
////            File file = new File("E:\\ocs_command.txt");
////            BufferedReader reader = new BufferedReader(new FileReader(file));
////            String currLine;
////
////            List<CommandDetail> commandDetails = new ArrayList<>();
////            CommandDetail commandDetail;
////            CommandTelnetParser cmdPaser;
////            Vendor vendor = new VendorServiceImpl().findById(3l);
////            NodeType nodeType = new NodeTypeServiceImpl().findById(100l);
////            Version version = new VersionServiceImpl().findById(99l);
////            List<ParamInput> paramInputs = new ArrayList<>();
////            while((currLine = reader.readLine()) != null) {
////                if (!currLine.trim().replaceAll(" +", "").isEmpty()) {
////
////                    commandDetail = new CommandDetail();
////                    commandDetail.setCommandName(currLine);
////                    commandDetail.setProtocol(Config.PROTOCOL_EXCHANGE);
////                    commandDetail.setCommandType(Config.COMMAND_TYPE.IMPACT.value);
////                    commandDetail.setCommandClassify(1l);
////                    commandDetail.setVendor(vendor);
////                    commandDetail.setVersion(version);
////                    commandDetail.setNodeType(nodeType);
////                    commandDetail.setUserName("hunghq2");
////                    commandDetail.setOperator("NO CHECK");
////                    commandDetail.setCreateTime(new Date());
////                    commandDetail.setIsActive(1l);
////
////                    cmdPaser = new CommandTelnetParser();
////                    cmdPaser.setCmd(currLine.trim());
////                    cmdPaser.setCmdEnd("");
////                    Long cmdPaserId = new CommandTelnetParserServiceImpl().save(cmdPaser);
////                    cmdPaser.setTelnetParserId(cmdPaserId);
////
////                    commandDetail.setCommandTelnetParser(cmdPaser);
////                    Long cmdDetailId = new CommandDetailServiceImpl().save(commandDetail);
////
////                    List<String> params = ItCommandController.getLstParam(currLine);
////                    if (!params.isEmpty()) {
////
////                        for (String param : params) {
////                            ParamInput paramInput = new ParamInput();
////                            paramInput.setCommandDetail(commandDetail);
////                            paramInput.setCreateTime(new Date());
////                            paramInput.setIsActive(1l);
////                            paramInput.setParamCode(param);
////                            paramInput.setUserName("hunghq2");
////                            paramInput.setParamType(0l);
////                            paramInput.setReadOnly(false);
////
////                            paramInputs.add(paramInput);
////
////                        }
////                    }
////                }
////            }
////
////            new ParamInputServiceImpl().saveOrUpdate(paramInputs);
//
////            String t = "EFF_DATE=";
////            System.out.println(t.trim().split("=").length);
//
////            String data = "test:thamso1=@{thamso1}:thamso2=@{thamso2}";
////            if (data != null) {
////                int start = 0;
////                int end = 0;
////                while (data.indexOf("@{", start) != -1
////                        && data.indexOf("}", end) != -1) {
////                    start = data.indexOf("@{", start) + 1;
////                    end = data.indexOf("}", end) + 1;
////                    System.out.println(data.substring(start + 1, end -1));
////                    System.out.println(start + " --- " + end);
////                }
////            }
//
//
//            // dong bo thong tin account va node mang
//            Node node1 = new NodeServiceImpl().findById(344842l);
//            Node node2 = new NodeServiceImpl().findById(344843l);
//            Node node3 = new NodeServiceImpl().findById(344844l);
//
////            NodeAccount nodeAccount1 = new NodeAccountServiceImpl().findById(88386l);
////            NodeAccount nodeAccount2 = new NodeAccountServiceImpl().findById(88387l);
////            NodeAccount nodeAccount3 = new NodeAccountServiceImpl().findById(88388l);
//
//            List<Node> nodes = new ArrayList<>();
//            nodes.add(node1);
//            nodes.add(node2);
//            nodes.add(node3);
//
//            Map<String, Object> filters = new HashedMap();
//            filters.put("action.actionId", 11425l);
//            List<Action> lstAction = new ActionServiceImpl().findList(filters);
//            List<ItNodeAction> nodeActions = new ArrayList<>();
//            filters.clear();
//
//            List<ItNodeAction> nodeActionsDel = new ArrayList<>();
//
//            // xu ly them node mang tac dong
////            int i = 0;
////            for (Action action : lstAction) {
////                ItNodeAction nodeAction = new ItNodeAction();
////                nodeAction.setType(0l);
////                nodeAction.setNodeId(nodes.get(i%3).getNodeId());
////                nodeAction.setNode(nodes.get(i%3));
////                nodeAction.setActionId(action.getActionId());
////                nodeAction.setActionNode(action);
////                nodeActions.add(nodeAction);
////                i++;
////
////                filters.put("actionId", action.getActionId());
////                List<ItNodeAction> t = new ItNodeActionServiceImpl().findList(filters);
////                nodeActionsDel.addAll(t);
//////                System.out.println(nodeActionsDel.size());
////            }
//////            System.out.println(nodeActionsDel.size());
//////            new ItNodeActionServiceImpl().delete(nodeActionsDel);
////            if (lstAction != null) {
////                System.out.println(nodeActions.size());
////                new ItNodeActionServiceImpl().saveOrUpdate(nodeActions);
////            }
//
//            // xu ly them account tac dong
//            int i = 0;
//            filters.clear();
//            List<ItActionAccount> actionAccountsSave = new ArrayList<>();
//            for (Action action : lstAction) {
//                filters.clear();
//                filters.put("actionId", action.getActionId());
//                List<ItNodeAction> nodeAction = new ItNodeActionServiceImpl().findList(filters);
//
//                if (nodeAction != null && !nodeAction.isEmpty()) {
//
//                    filters.clear();
//                    filters.put("serverId", nodeAction.get(0).getNode().getNodeId());
//                    List<NodeAccount> nodeAccounts = new NodeAccountServiceImpl().findList(filters);
//                    if (nodeAccounts != null && !nodeAccounts.isEmpty()) {
//                        ItActionAccount actionAccount = new ItActionAccount();
//                        actionAccount.setNodeAction(nodeAction.get(0));
//                        actionAccount.setNodeActionId(nodeAction.get(0).getId());
//                        actionAccount.setNodeAccount(nodeAccounts.get(0));
//                        actionAccount.setNodeAccountId(nodeAccounts.get(0).getId());
//                        actionAccountsSave.add(actionAccount);
//                        i++;
//                    }
//                }
//            }
//
//            if (!actionAccountsSave.isEmpty()) {
//                new ItActionAccountServicesImpl().saveOrUpdate(actionAccountsSave);
//            }
//
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }

    public static List<String> getLstParam(String data) {
        List<String> params = new ArrayList<>();
        if (data != null) {
            try {
                int start = 0;
                int end = 0;
                while (data.indexOf("@{", start) != -1
                        && data.indexOf("}", end) != -1) {
                    start = data.indexOf("@{", start) + 1;
                    end = data.indexOf("}", end) + 1;
                    params.add(data.substring(start + 1, end -1));
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
        return  params;
    }
}
