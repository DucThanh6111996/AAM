/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice;

import com.google.gson.Gson;
import com.viettel.bean.ResultGetAccount;
import com.viettel.it.controller.ItActionController;
import com.viettel.it.model.*;
import com.viettel.it.object.CmdNodeObject;
import com.viettel.it.object.CmdObject;
import com.viettel.it.object.MessageItBusObject;
import com.viettel.it.persistence.ActionServiceImpl;
import com.viettel.it.persistence.ItActionLogServiceImpl;
import com.viettel.it.util.*;
import com.viettel.it.webservice.object.*;
import com.viettel.it.webservice.utils.MopUtils;
import com.viettel.it.webservice.utils.MyResultTransformer;
import com.viettel.passprotector.PassProtector;
import com.viettel.sms.ws.SmsServer;
import com.viettel.sms.ws.SmsServerImplService;
import com.viettel.sms.ws.SmsServerImplServiceLocator;
import com.viettel.util.AppConfig;
import com.viettel.util.Constant;
import com.viettel.util.MailServiceUtil;
import com.viettel.it.util.SecurityService;
import com.viettel.util.SessionUtil;
import org.apache.axis.utils.StringUtils;
import org.apache.commons.collections.map.HashedMap;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.rpc.ServiceException;
import javax.xml.ws.WebServiceContext;
import java.net.MalformedURLException;
import java.net.URL;
import java.rmi.RemoteException;
import java.util.*;

/**
 * @author hienhv4
 */
@WebService(endpointInterface = "com.viettel.it.webservice.WsItBusiness")
public class WsItBusinessImpl implements WsItBusiness {

    protected final Logger logger = LoggerFactory.getLogger(WsItBusinessImpl.class);
    @Resource
    private WebServiceContext context;

    @Override
    public ResultDTO getActions(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                @WebParam(name = "parentId") String parentId, @WebParam(name = "actionId") String actionId) {
        ResultDTO resultDTO = new ResultDTO();
        try {
            Map<String, Object> filters = new HashedMap();
            if (parentId != null && !parentId.trim().isEmpty()) {
                filters.put("action.actionId", Long.valueOf(parentId));
            }
            if (actionId != null && !actionId.trim().isEmpty()) {
                filters.put("actionId", Long.valueOf(actionId));
            }

            List<Action> actions = new ActionServiceImpl().findListExac(filters, null);
            List<ActionDTO> actionDTOS = new ArrayList<>();
            if (actions != null) {
                for (Action action : actions) {
                    ActionDTO actionDTO = new ActionDTO(
                            action.getActionId() == null ? 0l : action.getActionId(),
                            action.getAction() == null ? 0l : action.getAction().getActionId(),
                            action.getName(),
                            action.getDescription() == null ? "" : action.getDescription(),
                            action.getItbusinessType() == null ? "" : action.getItbusinessType(),
                            action.getServiceBusinessId() == null ? 0l : action.getServiceBusinessId(),
                            action.getTreeLevel() == null ? 0l : action.getTreeLevel(),
                            action.getOpenBlockGroup() == null ? "" : action.getOpenBlockGroup(),
                            action.getProvisioningType() == null ? 0l : action.getProvisioningType());

                    actionDTOS.add(actionDTO);
                }
            }
            resultDTO.setData(new Gson().toJson(actionDTOS));
            resultDTO.setResultCode(1);
            resultDTO.setResultMessage("SUCCESS");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultDTO.setResultCode(2);
            resultDTO.setResultMessage("ERROR: " + e.getMessage());
        }
        return resultDTO;
    }

    @Override
    public ParamsDTO getParamByActionId(@WebParam(name = "userService") String userService,
                                        @WebParam(name = "passService") String passService,
                                        @WebParam(name = "actionId") Long actionId) {
        ParamsDTO paramsDTO = new ParamsDTO();

        logger.info("start get param input mop");
        try {
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                paramsDTO.setMessages("Username or password is not correct");
                return paramsDTO;
            }

            if (actionId == null || actionId.intValue() == 0) {
                paramsDTO.setMessages("No mop run was filled");
                return paramsDTO;
            }
        } catch (Exception ex) {
            paramsDTO.setMessages("Error when authenticate user/pass");
            logger.error(ex.getMessage(), ex);
            return paramsDTO;
        }

        try {
            Action action = new ActionServiceImpl().findById(actionId);
            if (action != null) {
                if (action.getActionDetails() == null || action.getActionDetails().isEmpty()) {
                    paramsDTO.setMessages("Error no commands are not declared");
                    return paramsDTO;
                }

                ActionDetail actionDetail = action.getActionDetails().get(0);
                List<ActionCommand> actionCommands = actionDetail.getActionCommands();
                if (actionCommands == null || actionCommands.isEmpty()) {
                    paramsDTO.setMessages("Error no commands are not declared in detail");
                    return paramsDTO;
                }

                Map<String, Integer> mapParamsCode = new HashedMap();
                for (ActionCommand actionCommand : actionCommands) {
                    if (actionCommand.getCommandDetail().getParamInputs() != null) {
                        paramsDTO.setCommand(actionCommand.getCommandDetail().getCommandTelnetParser().getCmd());
                        List<ParamInput> paramInputs = actionCommand.getCommandDetail().getParamInputs();
                        for (ParamInput paramInput : paramInputs) {
                            if (mapParamsCode.get(paramInput.getParamCode()) == null) {
                                mapParamsCode.put(paramInput.getParamCode(), 1);

                                ParamValueDTO paramValueDTO = new ParamValueDTO();
                                paramValueDTO.setParamCode(paramInput.getParamCode());
                                paramValueDTO.setDescription(paramInput.getParamCode());
                                paramValueDTO.setParamLabel(paramInput.getParamCode());
                                paramsDTO.getLstParamValues().add(paramValueDTO);
                            }
                        }
                    }
                }

            } else {
                paramsDTO.setMessages("Error no action was declared");
                return paramsDTO;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return paramsDTO;
    }

    @Override
    public ResultDTO runActionMop(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                  @WebParam(name = "actionId") Long actionId, @WebParam(name = "paramValues") String paramValues,
                                  @WebParam(name = "username") String username) {
        ResultDTO resultDTO = new ResultDTO();
        try {
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                resultDTO.setResultMessage("Username or password is not correct");
                return resultDTO;
            }

            if (actionId == null || actionId.intValue() == 0) {
                resultDTO.setResultMessage("No mop run was filled");
                return resultDTO;
            }

            if (paramValues == null) {
                resultDTO.setResultMessage("No mop run was filled");
                return resultDTO;
            }
        } catch (Exception ex) {
            resultDTO.setResultMessage("Error when authenticate user/pass");
            logger.error(ex.getMessage(), ex);
            return resultDTO;
        }

        try {

            Map<String, String> mapParamVals = MopUtils.paserParamVals(paramValues);
            if (mapParamVals == null) {
                mapParamVals = new HashMap<>();
            }

            Map<String, ParamInput> mapParamValue = new HashMap<>();
            for (Map.Entry<String, String> entry : mapParamVals.entrySet()) {
                mapParamValue.put(entry.getKey(), new ParamInput(entry.getKey(), entry.getValue()));
            }

            Date startTime = new Date();
            List<CmdObject> impactCmds;
            List<CmdObject> cmdLogRuns = new ArrayList<>();

            Action action = new ActionServiceImpl().findById(actionId);
            List<CommandDetail> commandDetails = new ArrayList<>();
            List<CommandDetail> commandLogDetails = new ArrayList<>();

            List<ActionCommand> actionCommands = action.getActionDetails().get(0).getActionCommands();
            for (ActionCommand actionCmd : actionCommands) {
                if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.IMPACT.value) {
                    commandDetails.add(actionCmd.getCommandDetail());
                } else if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.LOG.value) {
                    CommandDetail logCmdDetail = actionCmd.getCommandDetail();
                    logCmdDetail.setCmdLogOrderRun(actionCmd.getLogOrderRun());
                    commandLogDetails.add(actionCmd.getCommandDetail());
                }
            }

            impactCmds = new ItActionController().buildCmdObj(commandDetails, mapParamValue, false, null);
            if (!MopUtils.checkCommands(impactCmds)) {
                resultDTO.setResultMessage("Param values were not filled");
                return resultDTO;
            }

            if (commandLogDetails != null && !commandLogDetails.isEmpty()) {
                cmdLogRuns = new ItActionController().buildCmdObj(commandLogDetails, mapParamValue, true, null);
                if (!MopUtils.checkCommands(cmdLogRuns)) {
                    resultDTO.setResultMessage("Param values for command log were not filled");
                    return resultDTO;
                }
            }

            List<List<CmdObject>> lstCmdLogsRun = new ArrayList<>();
            Map<Long, List<CmdObject>> mapCmdLogs = new HashMap<>();
            if (cmdLogRuns != null && !cmdLogRuns.isEmpty()) {
                for (CmdObject cmdLog : cmdLogRuns) {
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

                List<ItNodeAction> nodeActionImpact = new ItActionController().getNodeAction(Config.ACTION_NODE_TYPE.IMPACT.value, action, null);
                List<ItNodeAction> nodeActionLog = new ItActionController().getNodeAction(Config.ACTION_NODE_TYPE.LOG.value, action, null);

                if (nodeActionLog != null) {
                    Collections.sort(nodeActionLog, new Comparator<ItNodeAction>() {
                        public int compare(final ItNodeAction object1, final ItNodeAction object2) {
                            return object1.getLogOrderRun().compareTo(object2.getLogOrderRun());
                        }
                    });
                }

                MessageItBusObject mesObj = new MessageItBusObject();

                actionLog.setAction(action);
                actionLog.setStartTime(new Date());
                actionLog.setStatus(1l);
                actionLog.setUserRun(username);

                Long actionLogId = new ItActionLogServiceImpl().save(actionLog);
                actionLog.setId(actionLogId);

                mesObj.setActionName(action.getName());
                mesObj.setFlowRunId(action.getActionId());
                mesObj.setLstImpactCmds(impactCmds);
                mesObj.setLstLogCmds(cmdLogRuns);

                /*
                Xu ly cac node tac dong
                */
                String user;
                String pass;
                List<CmdNodeObject> cmdNodeObjects = new ArrayList<>();

                //20181023_tudn_start load pass security
                Map<String, String> mapConfigSecurity = SecurityService.getConfigSecurity();
                //20181023_tudn_end load pass security

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
                            pass = PasswordEncoder.encrypt(PassProtector.decrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword(), Config.SALT));
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
                    mapCmdObjs.put(1l, impactCmds);
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
                                passLog = PasswordEncoder.encrypt(PassProtector.decrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword(), Config.SALT));
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

                SocketClient client = new SocketClient(serverIp, serverPort);
                client.sendMsg(encrytedMess);

                String socketResult = client.receiveResult();
                if (socketResult != null && socketResult.contains("NOK")) {
                    throw new Exception(socketResult);
                }

                    /*
                    Ghi log tac dong nguoi dung
                    */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.UPDATE,
                            action.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                resultDTO.setResultMessage("SUCCESS");

            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);

                try {
                    new ItActionLogServiceImpl().delete(actionLog);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                resultDTO.setResultMessage(ex.getMessage());
                return resultDTO;
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultDTO.setResultMessage(e.getMessage());
            return resultDTO;
        }

        return resultDTO;
    }

    @Override
    public ResultDTO runActionMopSocIT(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                       @WebParam(name = "actionId") Long actionId, @WebParam(name = "paramValues") String paramValues,
                                       @WebParam(name = "username") String username) {
        ResultDTO resultDTO = new ResultDTO();
        try {
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                resultDTO.setResultMessage("Username or password is not correct");
                return resultDTO;
            }

            if (actionId == null || actionId.intValue() == 0) {
                resultDTO.setResultMessage("No mop run was filled");
                return resultDTO;
            }

            if (paramValues == null) {
                resultDTO.setResultMessage("No mop run was filled");
                return resultDTO;
            }
        } catch (Exception ex) {
            resultDTO.setResultMessage("Error when authenticate user/pass");
            logger.error(ex.getMessage(), ex);
            return resultDTO;
        }

        try {

            //tuanda38 start - tach paramValue lay socitId
            String socitId = null;
            if (paramValues != null || !paramValues.trim().isEmpty()) {
                int splitIndex = paramValues.trim().lastIndexOf(';');
                String socitIdName = Arrays.asList(paramValues.substring(splitIndex + 1, paramValues.length()).split("=")).get(0);
                if ("socitId".equals(socitIdName)) {
                    socitId = Arrays.asList(paramValues.substring(splitIndex + 1, paramValues.length()).split("=")).get(1);
                    if (splitIndex != -1) {
                        paramValues = paramValues.substring(0, splitIndex);
                    } else {
                        paramValues = "";
                    }
                }
            }
            //tuanda38 end

            Map<String, String> mapParamVals = MopUtils.paserParamVals(paramValues);
            if (mapParamVals == null) {
                mapParamVals = new HashMap<>();
            }

            Map<String, ParamInput> mapParamValue = new HashMap<>();
            for (Map.Entry<String, String> entry : mapParamVals.entrySet()) {
                mapParamValue.put(entry.getKey(), new ParamInput(entry.getKey(), entry.getValue()));
            }

            Date startTime = new Date();
            List<CmdObject> impactCmds;
            List<CmdObject> cmdLogRuns = new ArrayList<>();

            Action action = new ActionServiceImpl().findById(actionId);
            List<CommandDetail> commandDetails = new ArrayList<>();
            List<CommandDetail> commandLogDetails = new ArrayList<>();

            List<ActionCommand> actionCommands = action.getActionDetails().get(0).getActionCommands();
            for (ActionCommand actionCmd : actionCommands) {
                if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.IMPACT.value) {
                    commandDetails.add(actionCmd.getCommandDetail());
                } else if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.LOG.value) {
                    CommandDetail logCmdDetail = actionCmd.getCommandDetail();
                    logCmdDetail.setCmdLogOrderRun(actionCmd.getLogOrderRun());
                    commandLogDetails.add(actionCmd.getCommandDetail());
                }
            }

            impactCmds = new ItActionController().buildCmdObj(commandDetails, mapParamValue, false, null);
            if (!MopUtils.checkCommands(impactCmds)) {
                resultDTO.setResultMessage("Param values were not filled");
                return resultDTO;
            }

            if (commandLogDetails != null && !commandLogDetails.isEmpty()) {
                cmdLogRuns = new ItActionController().buildCmdObj(commandLogDetails, mapParamValue, true, null);
                if (!MopUtils.checkCommands(cmdLogRuns)) {
                    resultDTO.setResultMessage("Param values for command log were not filled");
                    return resultDTO;
                }
            }

            List<List<CmdObject>> lstCmdLogsRun = new ArrayList<>();
            Map<Long, List<CmdObject>> mapCmdLogs = new HashMap<>();
            if (cmdLogRuns != null && !cmdLogRuns.isEmpty()) {
                for (CmdObject cmdLog : cmdLogRuns) {
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

                List<ItNodeAction> nodeActionImpact = new ItActionController().getNodeAction(Config.ACTION_NODE_TYPE.IMPACT.value, action, null);
                List<ItNodeAction> nodeActionLog = new ItActionController().getNodeAction(Config.ACTION_NODE_TYPE.LOG.value, action, null);

                if (nodeActionLog != null) {
                    Collections.sort(nodeActionLog, new Comparator<ItNodeAction>() {
                        public int compare(final ItNodeAction object1, final ItNodeAction object2) {
                            return object1.getLogOrderRun().compareTo(object2.getLogOrderRun());
                        }
                    });
                }

                MessageItBusObject mesObj = new MessageItBusObject();

                actionLog.setAction(action);
                actionLog.setStartTime(new Date());
                actionLog.setStatus(1l);
                actionLog.setUserRun(username);

                //tuanda38 start - them socitId de xac dinh tu SOCIT goi den WS
                actionLog.setSocitId(socitId);
                //tuanda38 end

                Long actionLogId = new ItActionLogServiceImpl().save(actionLog);
                actionLog.setId(actionLogId);

                mesObj.setActionName(action.getName());
                mesObj.setFlowRunId(action.getActionId());
                mesObj.setLstImpactCmds(impactCmds);
                mesObj.setLstLogCmds(cmdLogRuns);

                /*
                Xu ly cac node tac dong
                */
                String user;
                String pass;
                List<CmdNodeObject> cmdNodeObjects = new ArrayList<>();

                //20181023_tudn_start load pass security
                Map<String, String> mapConfigSecurity = SecurityService.getConfigSecurity();
                //20181023_tudn_end load pass security

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
                            pass = PasswordEncoder.encrypt(PassProtector.decrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword(), Config.SALT));
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
                    mapCmdObjs.put(1l, impactCmds);
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
                                passLog = PasswordEncoder.encrypt(PassProtector.decrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword(), Config.SALT));
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

                SocketClient client = new SocketClient(serverIp, serverPort);
                client.sendMsg(encrytedMess);

                String socketResult = client.receiveResult();
                if (socketResult != null && socketResult.contains("NOK")) {
                    throw new Exception(socketResult);
                }

                    /*
                    Ghi log tac dong nguoi dung
                    */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), actionLog.getUserRun(),
                            "N/A", "N/A", ItActionController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.UPDATE,
                            action.toString(), "N/A");
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                resultDTO.setResultMessage("SUCCESS");

            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);

                try {
                    new ItActionLogServiceImpl().delete(actionLog);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                resultDTO.setResultMessage(ex.getMessage());
                return resultDTO;
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            resultDTO.setResultMessage(e.getMessage());
            return resultDTO;
        }

        return resultDTO;
    }

    //tuanda38 start
    @Override
    public ResultDTO getResultImpact(String userService, String passService, String socitId) {

        ResultDTO resultDTO = new ResultDTO();
        try {
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                resultDTO.setResultMessage("Username or password is not correct");
                return resultDTO;
            }
        } catch (Exception ex) {
            resultDTO.setResultMessage("Error when authenticate user/pass");
            logger.error(ex.getMessage(), ex);
            return resultDTO;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT log,command,protocol,user_run,end_time FROM it_action_log ial,it_command_log icl,command_detail cd ");
        sql.append("WHERE ial.id = icl.action_log_id AND icl.command_detail_id = cd.command_detail_id AND ial.socit_id like :socitId ");
        sql.append("ORDER BY ial.end_time desc");

        List<Map<String, Object>> result;
        StringBuilder builder = new StringBuilder();
        String protocol = "";
        String command = "";
        List<String> userRun = new ArrayList<>();
        List<String> endTime = new ArrayList<>();

        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.openSession();
            transaction = session.beginTransaction();
            String _sql = fomatHQL(sql.toString());
            SQLQuery sqlQuery = session.createSQLQuery(_sql);
            sqlQuery.setParameter("socitId", socitId);
            sqlQuery.setResultTransformer(MyResultTransformer.INSTANCE);
            sqlQuery.setFetchSize(5000);
            result = sqlQuery.list();
            session.getTransaction().commit();

            if (result.isEmpty()) {
                resultDTO.setResultMessage("FAIL");
            } else {
                for (Map<String, Object> row : result) {
                    for (String key : row.keySet()) {
                        if ("LOG".equals(key)) {
                            builder.append(row.get(key).toString());
                        }
                        if (protocol.isEmpty() && "PROTOCOL".equals(key)) {
                            protocol = row.get(key).toString();
                        }
                        if (command.isEmpty() && "COMMAND".equals(key)) {
                            command = row.get(key).toString().replace("\r\n", " ");
                        }
                        if ("USER_RUN".equals(key)) {
                            userRun.add(row.get(key).toString());
                        }
                        if ("END_TIME".equals(key)) {
                            endTime.add(row.get(key).toString());
                        }
                    }
                }
                StringBuilder resultMessage = new StringBuilder();
                resultMessage.append(protocol).append(";").append(command).append(";").append("\r\n");
                for (int i = 0; i < userRun.size(); i++) {
                    resultMessage.append(userRun.get(i)).append(";").append(endTime.get(i)).append(";");
                    resultMessage.append("\r\n");
                }
                resultDTO.setResultMessage(resultMessage.toString());
                resultDTO.setData(builder.toString());
            }
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            logger.error(e.toString(), e);
        } finally {
            if (session != null)
                session.close();
        }
        return resultDTO;
    }

    @Override
    public JsonResponseDTO getJsonData(String userService, String passService, String socitId) {

        JsonResponseDTO res = new JsonResponseDTO();
        try {
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                res.setResultMessage("Username or password is not correct");
                return res;
            }
        } catch (Exception ex) {
            res.setResultMessage("Error when authenticate user/pass");
            logger.error(ex.getMessage(), ex);
            return res;
        }

        StringBuilder sql = new StringBuilder();
        sql.append("SELECT log,command,protocol,user_run,start_time,end_time,socit_id,action_id, command_name FROM it_action_log ial,it_command_log icl,command_detail cd ");
        sql.append("WHERE ial.id = icl.action_log_id AND icl.command_detail_id = cd.command_detail_id AND ial.socit_id like :socitId ");
        sql.append("ORDER BY ial.start_time desc");

        List<Map<String, Object>> result;
        String protocol = "";
        String command = "";

        Session session = null;
        Transaction transaction = null;
        try {
            session = HibernateUtil.openSession();
            transaction = session.beginTransaction();
            SQLQuery sqlQuery = session.createSQLQuery(sql.toString());
            sqlQuery.setParameter("socitId", socitId);
            sqlQuery.setResultTransformer(MyResultTransformer.INSTANCE);
            result = sqlQuery.list();
            session.getTransaction().commit();

            if (result.isEmpty()) {
                res.setResultMessage("FAIL");
            } else {
                JSONArray jsonArray = new JSONArray();
                for (Map<String, Object> row : result) {
                    JSONObject jsonObject = new JSONObject();
                    for (String key : row.keySet()) {
                        if ("LOG".equals(key)) {
                            jsonObject.put("LOG", row.get(key).toString());
                        }
                        if (protocol.isEmpty() && "PROTOCOL".equals(key)) {
                            jsonObject.put("PROTOCOL", row.get(key).toString());
                        }
                        if (command.isEmpty() && "COMMAND".equals(key)) {
                            jsonObject.put("COMMAND", row.get(key).toString().replace("\r\n", " "));
                        }
                        if ("USER_RUN".equals(key)) {
                            jsonObject.put("USER_RUN", row.get(key).toString());
                        }
                        if ("END_TIME".equals(key)) {
                            jsonObject.put("END_TIME", row.get(key).toString());
                        }
                        if ("START_TIME".equals(key)) {
                            jsonObject.put("START_TIME", row.get(key).toString());
                        }
                        if ("SOCIT_ID".equals(key)) {
                            jsonObject.put("SOCIT_ID", row.get(key).toString());
                        }
                        if ("ACTION_ID".equals(key)) {
                            jsonObject.put("ACTION_ID", Long.valueOf(row.get(key).toString()));
                        }
                        if ("COMMAND_NAME".equals(key)) {
                            jsonObject.put("COMMAND_NAME", row.get(key).toString());
                        }
                    }
                    jsonArray.add(jsonObject);
                }
                JSONObject mainJsonObj = new JSONObject();
                mainJsonObj.put("dataJson", jsonArray);
                res.setResultCode("0");
                res.setResultMessage("SUCCESS");
                res.setDataJson(mainJsonObj.toJSONString());
                res.setTotalDataJson(result.size());
            }
        } catch (Exception e) {
            if (transaction != null)
                transaction.rollback();
            logger.error(e.toString(), e);
        } finally {
            if (session != null)
                session.close();
        }
        return res;
    }

    @Override
    public ResultDTO sendNotification(String phones, String content) {
        ResultDTO resultDTO = new ResultDTO();
        SmsServerImplService smsService;
        try {
            smsService = new SmsServerImplServiceLocator();
            SmsServer smsServer = smsService.getSmsServerImplPort(new URL("http://10.60.97.113:8899/ws/sendsms"));
            List<String> phoneLst = Arrays.asList(phones.split(";", -1));
            for (String phone : phoneLst) {
                phone = phone.trim().replaceAll("^0", "84");
                if (phone.startsWith("9")) {
                    phone = "84" + phone;
                }
                int result = smsServer.sendSingleSms("AUTOMATION", "TEST " + content, phone, 1L, 1);
                logger.info(phone + "_" + content);
                resultDTO.setResultCode(result);
                resultDTO.setResultMessage("Send to " + phone + " with content: " + content);
            }
        } catch (ServiceException e) {
            logger.error(e.getMessage(), e);
        } catch (RemoteException e) {
            logger.error(e.getMessage(), e);
        } catch (MalformedURLException e) {
            logger.error(e.getMessage(), e);
        }
        return resultDTO;
    }

    @Override
    public ResultDTO sendNotificationEmail(String emails, String content) {
        ResultDTO resultDTO = new ResultDTO();
        try {
            String subject = "[AUTOMATION] Yêu cầu upcode";
            MailServiceUtil mailServiceUtil = new MailServiceUtil();
            List<String> emailLst = Arrays.asList(emails.split(";", -1));
            mailServiceUtil.sendMail(emailLst.toArray(new String[emailLst.size()]), null, null, subject, content, null, null);
            logger.info(emailLst + "_" + subject);
            resultDTO.setResultCode(0);
            resultDTO.setResultMessage("Send to " + emailLst + " with content: " + content);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return resultDTO;
    }

    @Override
    public ResultDTO sendEmailSms(String subject, String mailUser, String phoneUser, String userName, String content) {
        ResultDTO resultDTO = new ResultDTO();
        try {
            if (StringUtils.isEmpty(subject)) {
                subject = "Mail th�ng b�o cho FO ph� duy?t CI/CD";
            }
            List<String> mailUsers = new ArrayList<>();
            mailUsers = new ArrayList<String>(Arrays.asList(mailUser.split(",")));
            List<String> phoneUsers = new ArrayList<>();
            phoneUsers = new ArrayList<String>(Arrays.asList(phoneUser.split(",")));
            List<String> userNames = new ArrayList<>();
            userNames = new ArrayList<String>(Arrays.asList(userName.split(",")));
            List<String> contents = new ArrayList<>();
            contents = new ArrayList<String>(Arrays.asList(content.split(",")));
            // remove duplicate
            Set<String> tmp = new HashSet<>();
            tmp.addAll(mailUsers);
            mailUsers.clear();
            mailUsers.addAll(tmp);
            tmp = new HashSet<>();
            tmp.addAll(phoneUsers);
            phoneUsers.clear();
            phoneUsers.addAll(tmp);
            // add content
            StringBuilder contentMsg = new StringBuilder();
            if (contents != null && contents.size() > 0) {
                contentMsg.append("");
                for (String strContent : contents) {
                    contentMsg.append("\n" + strContent);
                }
            }
            new MailServiceUtil().sendMail(mailUsers.toArray(new String[mailUsers.size()]), null, null, subject, contentMsg.toString(), null, null);
            SmsServerImplService SmsService;
            SmsService = new SmsServerImplServiceLocator();
            SmsServer smsServer = SmsService.getSmsServerImplPort(new URL(MessageUtil.getResourceBundleConfig("sms_server_address")));
            for (String phone : phoneUsers) {
                smsServer.sendSingleSms(MessageUtil.getResourceBundleConfig("alias"), contentMsg.toString(), phone, 12L, 50);
            }
            resultDTO.setResultCode(1);
            resultDTO.setResultMessage("Success");
        } catch (Exception ex) {
            resultDTO.setResultCode(0);
            resultDTO.setResultMessage("Fail");
            logger.error(ex.getMessage(), ex);
        }
        return resultDTO;
    }

    private static String fomatHQL(CharSequence queryString) {
        StringBuffer buffer = new StringBuffer(queryString);
        int start = 0;
        int order = 0;
        while ((start = buffer.indexOf("?", start + 1)) > 0) {
            buffer.insert(start + 1, order);
            order++;
        }
        return buffer.toString();
    }
    //tuanda38 end

//    public static void main(String[] args) {
//        ParamDTO paramDTO = new ParamDTO();
//        Action action = null;
//        try {
//            action = new ActionServiceImpl().findById(18782l);
//        } catch (AppException e) {
//            e.printStackTrace();
//        }
//        if (action != null) {
//            if (action.getActionDetails() == null || action.getActionDetails().isEmpty()) {
////                paramDTO.setMessages("Error no commands are not declared");
////                return paramDTO;
//            }
//
//            ActionDetail actionDetail = action.getActionDetails().get(0);
//            List<ActionCommand> actionCommands = actionDetail.getActionCommands();
//            if (actionCommands == null || actionCommands.isEmpty()) {
////                paramsDTO.setMessages("Error no commands are not declared in detail");
////                return paramsDTO;
//            }
//
//            Map<String, Integer> mapParamsCode = new HashedMap();
//            for (ActionCommand actionCommand : actionCommands) {
//                if (actionCommand.getCommandDetail().getParamInputs() != null) {
//                    System.out.println("Command: " + actionCommand.getCommandDetail().getCommandTelnetParser().getCmd());
//
//                    List<ParamInput> paramInputs = actionCommand.getCommandDetail().getParamInputs();
//                    for (ParamInput paramInput : paramInputs) {
//                        if (mapParamsCode.get(paramInput.getParamCode()) == null) {
//                            mapParamsCode.put(paramInput.getParamCode(), 1);
////                            System.out.println("Command: " + actionCommand.getCommandDetail().getCommandTelnetParser().getCmd());
//                            System.out.println("Param Code: " + paramInput.getParamCode());
////                            System.out.println("Param ");
////                            ParamValueDTO paramValueDTO = new ParamValueDTO();
////                            paramValueDTO.setParamCode(paramInput.getParamCode());
////                            paramValueDTO.setDescription(paramInput.getParamCode());
////                            paramValueDTO.setParamLabel(paramInput.getParamCode());
////                            paramsDTO.getLstParamValues().add(paramValueDTO);
//                        }
//                    }
//                }
//            }
//        }
//    }
}
