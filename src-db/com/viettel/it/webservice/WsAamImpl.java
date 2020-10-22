/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.viettel.bean.Database;
import com.viettel.bean.MdDependent;
import com.viettel.controller.Module;
import com.viettel.bean.Service;
import com.viettel.controller.IimClientService;
import com.viettel.controller.IimClientServiceImpl;
import com.viettel.exception.AppException;
import com.viettel.iim.services.main.IimServices_PortType;
import com.viettel.iim.services.main.JsonResponseBO;
import com.viettel.iim.services.main.ParameterBO;
import com.viettel.iim.services.main.RequestInputBO;
import com.viettel.it.controller.GenerateFlowRunController;
import com.viettel.it.controller.ItActionController;
import com.viettel.it.model.*;
import com.viettel.it.object.*;
import com.viettel.it.persistence.*;
import com.viettel.it.persistence.common.CatCountryServiceImpl;
import com.viettel.it.util.*;
import com.viettel.it.util.Config;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.PasswordEncoder;
import com.viettel.it.webservice.object.*;
import com.viettel.it.webservice.utils.MopUtils;
import com.viettel.controller.ActionDetailApp;
import com.viettel.model.ActionModule;
import com.viettel.model.CatCountryBO;
import com.viettel.model.ImpactProcess;
import com.viettel.passprotector.PassProtector;
import com.viettel.persistence.ActionServiceImpl;
import com.viettel.thread.AutoThread;
import com.viettel.util.*;
import com.viettel.util.Util;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author hienhv4
 */
@WebService(endpointInterface = "com.viettel.it.webservice.WsAam")
public class WsAamImpl implements WsAam {
    @Resource
    private static WebServiceContext context;

    protected static final Logger logger = LoggerFactory.getLogger(WsAamImpl.class);
    public static final int RESPONSE_SUCCESS = 1;
    public static final int RESPONSE_FAIL = 0;

    /**
     * Create thread action
     */
    @Override
    public com.viettel.model.Action createActionThread(@WebParam(name = "userService") String userService,
                                                       @WebParam(name = "passService") String passService,
                                                       @WebParam(name = "id") Long id) {
        try {
            ResultDTO resultDTO = new ResultDTO();
            resultDTO.setResultCode(RESPONSE_SUCCESS);
            resultDTO = checkLogin(userService, passService, resultDTO);
            if (resultDTO.getResultCode() == RESPONSE_FAIL) {
                logger.info("End method: createMopUpCode");
                return null;
            }
            FlowRunAction flowRunAction = new FlowRunActionServiceImpl().findById(id);
            if (flowRunAction == null || flowRunAction.getServiceActionId() == null) {
                return new com.viettel.model.Action();
            }

            com.viettel.model.Action action = new ActionServiceImpl().findById(flowRunAction.getServiceActionId());

            AutoThread autoThread = new AutoThread(action);

            // Auto run action BD database (Reboot)
            autoThread.run();

            // START Process wait autoThread.run() is finish (1')
            boolean isWait = true;
            // If running status is wait within 1 minutes, it will exit the loop
            long timeCounter = 0L;
            int runningStatus = -1;
            do {
                runningStatus = checkRunningStatus(action.getId());
                if (runningStatus == AamConstants.RUNNING_STATUS.SUCCESS) {
                    Thread.sleep(10000);
                    timeCounter += 10000;
                } else {
                    isWait = false;
                }
                if (timeCounter > 10000 * 6l) {
                    isWait = false;
                }
            } while (isWait);
            // END Process wait autoThread.run() is finish

            return action;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return new com.viettel.model.Action();
        }
    }

    /**
     * Get running status
     */
    public int checkRunningStatus(@WebParam(name = "id") Long id) {
        int runningStatus = -1;
        try {
            com.viettel.model.Action action = new ActionServiceImpl().findById(id);
            if (action != null && action.getRunningStatus() != null) {
                runningStatus = action.getRunningStatus();
            }
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        return runningStatus;
    }

    //20180620_tudn_start ghi log DB
    public String getRemoteIp() {
        try {
            String ip = "";
            MessageContext mc = context.getMessageContext();
            HttpServletRequest req = (HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST);
            if (req != null) {
                ip = req.getRemoteAddr();
            }
            return ip;
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
        }
        return "N/A";
    }
    //20180620_tudn_end ghi log DB

    @Override
    public ParamsDTO getParamMop(@WebParam(name = "userService") String userService,
                                 @WebParam(name = "passService") String passService,
                                 @WebParam(name = "templateId") Long templateId,
                                 @WebParam(name = "nodeCode") Long nodeCode) {
        ParamsDTO resultGetParamInputDTO = new ParamsDTO();
        logger.info("Start get param input reset mop");
        try {
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                resultGetParamInputDTO.setMessages("Incorrect user/pass");
                return resultGetParamInputDTO;
            }
        } catch (Exception ex) {
            resultGetParamInputDTO.setMessages("Incorrect user/pass");
            logger.error(ex.getMessage(), ex);
            return resultGetParamInputDTO;
        }

        try {
            // get node
            Map<String, Object> filters = new HashedMap();
            filters.put("nodeCode", nodeCode);
            filters.put("active", Constant.status.active);

            List<Node> nodes = new NodeServiceImpl().findListExac(filters, null);
            if (nodes == null || nodes.isEmpty()) {
                resultGetParamInputDTO.setMessages("Node node declared with node code: " + nodeCode);
                return resultGetParamInputDTO;
            }

            List<ParamValue> lstParamValue = new MopUtils().getLstParamInput(nodes.get(0), templateId);
            if (lstParamValue == null || lstParamValue.isEmpty()) {
                resultGetParamInputDTO.setMessages("Can not get parameters with node: " + nodes.get(0).getNodeCode());
                return resultGetParamInputDTO;
            } else {

                // Xoa cac tham so la tham so tham chieu
                List<ParamValue> lstParamValueTmp = new ArrayList<>();
                for (ParamValue p : lstParamValue) {
                    if (!p.isDisableByInOut()) {
                        lstParamValueTmp.add(p);
                    }
                }
                // Xoa bo cac tham so trung nhau
                lstParamValueTmp = new MopUtils().distinctParamValueSameParamCode(lstParamValueTmp);

                if (lstParamValueTmp != null) {
                    ParamValueDTO paramDto;
                    ArrayList<ParamValueDTO> lstParamValueDto = new ArrayList<>();

                    for (ParamValue p : lstParamValueTmp) {
                        paramDto = new ParamValueDTO();
                        paramDto.setDescription(p.getDescription() == null ? "" : p.getDescription());
                        paramDto.setDisableByInOut(p.isDisableByInOut());
                        paramDto.setFormula(p.getFormula() == null ? "" : p.getFormula());
                        paramDto.setGroupCode(p.getGroupCode());
                        paramDto.setParamInputId(p.getParamInput().getParamInputId());
                        paramDto.setParamLabel(p.getParamCode());
                        paramDto.setParamCode(p.getParamCode());

                        lstParamValueDto.add(paramDto);
                    }

//                    ParamValsDTO paramsNode = new ParamValsDTO();
//                    paramsNode.setLstParamValues(lstParamValueDto);
//                    paramsNode.setNodeId(nodes.get(0).getNodeId());
//                    paramsNode.setMessage("");
//                    paramsNode.setNodeCode(nodes.get(0).getNodeCode());

                    Gson gson = new Gson();
                    logger.info(gson.toJson(lstParamValueDto));

                    resultGetParamInputDTO.setMessages("SUCCESS");
                    return resultGetParamInputDTO;
                } else {
                    resultGetParamInputDTO.setMessages("Can not get distinct parameters with node: " + nodes.get(0).getNodeCode());
                    return resultGetParamInputDTO;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public ResultDTO startRunMop(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                 @WebParam(name = "paramValues") String paramValues, @WebParam(name = "flowTemplateId") Long mopId,
                                 @WebParam(name = "username") String username, @WebParam(name = "nodeCode") String nodeCode) {

        ResultDTO result = new ResultDTO();
        result.setResultCode(RESPONSE_FAIL);
        logger.info("start start run mop");
        Date startTime = new Date();
        try {
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultMessage("Username or password incorrect");
                return result;
            }
        } catch (Exception ex) {
            result.setResultMessage("Error when validate user/pass");
            logger.error(ex.getMessage(), ex);
            return result;
        }


        // Kiem tra xem cac tham so cua mop da dc dien day du hay chua
        List<ParamValueDTO> lstParamValDTO = new ArrayList<>();
        try {
            if (paramValues == null
                    || paramValues.isEmpty()) {
                result.setResultMessage("No param values were filled");
                return result;
            }
            Gson g = new Gson();
            Type listType = new TypeToken<List<ParamValueDTO>>() {
            }.getType();
            lstParamValDTO = new Gson().fromJson(paramValues, listType);

            for (ParamValueDTO p : lstParamValDTO) {
                if (p.getParamValue() == null || p.getParamValue().trim().isEmpty()) {
                    result.setResultMessage("Param: " + p.getParamCode() + " was not filled");
                    return result;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.setResultMessage("Error when check input param values");
            return result;
        }

        /**
         * Lay thong tin node mang tac dong dua vao nodecode
         */
        Node node = null;
        if (nodeCode != null && !nodeCode.trim().isEmpty()) {
            try {
                Map<String, Object> filtes = new HashedMap();
                filtes.put("nodeCode", nodeCode);
                filtes.put("active", Constant.status.active);
                List<Node> nodes = new NodeServiceImpl().findListExac(filtes, null);
                if (nodes != null && !nodes.isEmpty()) {
                    node = nodes.get(0);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (node == null) {
            result.setResultMessage("Cannot get node information from node code: " + nodeCode);
            return result;
        }

        /**
         * Lay thong tin account tac dong
         */
        NodeAccount nodeAccount = MopUtils.getNodeAccount(node, username);
        if (nodeAccount == null) {
            result.setResultMessage("Cannot get node account from node: " + nodeCode + "/username: " + username);
            return result;
        }

        try {
            FlowTemplates selectedFlowTemplate = new FlowTemplatesServiceImpl().findById(mopId);
            FlowRunAction flowRunAction = new FlowRunAction();
            List<NodeRun> nodeRuns = new ArrayList<NodeRun>();
            flowRunAction.setCreateDate(new Date());
            if (flowRunAction.getFlowRunName() != null) {
                flowRunAction.setFlowRunName(flowRunAction.getFlowRunName().trim());
            }
            flowRunAction.setFlowRunName(selectedFlowTemplate.getFlowTemplateName());
            flowRunAction.setStatus(1L);
            flowRunAction.setCrNumber("");
            flowRunAction.setCreateBy(username);
            flowRunAction.setFlowTemplates(selectedFlowTemplate);
            flowRunAction.setTimeRun(new Date());

            Object[] objs = new FlowRunActionServiceImpl().openTransaction();
            Session session = (Session) objs[0];
            Transaction tx = (Transaction) objs[1];
            try {
                Map<Long, List<ActionOfFlow>> mapGroupAction = new HashMap<>();
                for (ActionOfFlow actionOfFlow : selectedFlowTemplate.getActionOfFlows()) {
                    if (mapGroupAction.get(actionOfFlow.getGroupActionOrder()) == null) {
                        mapGroupAction.put(actionOfFlow.getGroupActionOrder(), new ArrayList<ActionOfFlow>());
                    }
                    mapGroupAction.get(actionOfFlow.getGroupActionOrder()).add(actionOfFlow);
                }

                Long flowRunActionId = new FlowRunActionServiceImpl().save(flowRunAction, session, tx, true);
                List<ParamValue> paramValuesObj = new ArrayList<ParamValue>();

                List<NodeRunGroupAction> nodeRunGroupActions = new ArrayList<NodeRunGroupAction>();
                List<AccountGroupMop> lstAccGroupMop = new ArrayList<>();
                if (paramValues != null && !paramValues.trim().isEmpty()) {

//                    for (ParamValsOfNodeDTO paramsNode : paramValsDto.getLstParamValOfNode()) {
                    logger.info("chay vao node :" + node.getNodeCode());
                    NodeRun nodeRun = new NodeRun(new NodeRunId(node.getNodeId(), flowRunAction.getFlowRunId()), flowRunAction, node);
                    nodeRuns.add(nodeRun);
                    List<ParamValue> lstParamValsOfNode = new MopUtils().getLstParamInput(node, mopId);
                    List<ParamValue> _paramValueFilledOfNode = new MopUtils().updateParamInputValues(lstParamValsOfNode, lstParamValDTO, nodeRun);
                    if (_paramValueFilledOfNode == null || _paramValueFilledOfNode.isEmpty()) {
                        logger.error("ERROR CANNOT GET PARAM VALUE FROM PARAM USER SET");
                        result.setResultMessage("ERROR CANNOT GET PARAM VALUE FROM PARAM USER SET");
                        return result;
                    }
//                        if (mapGroupAction.get(node) != null) {
                    logger.info(" co vao mapGroupAction size = " + mapGroupAction.get(node));
                    for (Map.Entry<Long, List<ActionOfFlow>> entry : mapGroupAction.entrySet()) {
                        NodeRunGroupAction nodeRunGroupAction = new NodeRunGroupAction(
                                new NodeRunGroupActionId(node.getNodeId(),
                                        flowRunAction.getFlowRunId(),
                                        entry.getValue().get(0).getStepNum()), entry.getValue().get(0), nodeRun);
                        nodeRunGroupActions.add(nodeRunGroupAction);

                        // hanhnv68 add 2016 12 01
                        // add thong tin account tac dong cho tung dau viec
                        AccountGroupMop accGroup = new AccountGroupMop();
//							accGroup.setGroupOrderRun(groupAction.getActionOfFlows().get(0).getGroupActionOrder());
                        accGroup.setNodeAccountId(nodeAccount.getId());
                        accGroup.setNodeId(node.getNodeId());
                        accGroup.setFlowRunId(flowRunActionId);
                        accGroup.setActionOfFlowId(entry.getValue().get(0).getStepNum());

                        lstAccGroupMop.add(accGroup);
                        // end hanhnv68 add 2016 12 01

                    } // end loop for group action
                    logger.info(" thoai khoi mapGroupAction");
//                        }
//                    }
                    logger.info(" xoa session");
//                    session.clear();
                    logger.info(" insert NodeRunServiceImpl ");
                    new NodeRunServiceImpl().saveOrUpdate(nodeRuns, session, tx, true);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), getRemoteIp(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WsAamImpl.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.CREATE,
                                nodeRuns.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                    logger.info(" insert ParamValueServiceImpl ");
                    new ParamValueServiceImpl().saveOrUpdate(_paramValueFilledOfNode, session, tx, true);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), getRemoteIp(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WsAamImpl.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.CREATE,
                                _paramValueFilledOfNode.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB

//                    session.flush();
//                    session.clear();
                    new NodeRunGroupActionServiceImpl().saveOrUpdate(nodeRunGroupActions, session, tx, true);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), getRemoteIp(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WsAamImpl.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.CREATE,
                                nodeRunGroupActions.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                    logger.info(" insert account group mop ");
                    new AccountGroupMopServiceImpl().saveOrUpdate(lstAccGroupMop);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), getRemoteIp(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WsAamImpl.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.CREATE,
                                lstAccGroupMop.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB

//				session.flush();
//		 		tx.commit();

                } else {

                }
                new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), getRemoteIp(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WsAamImpl.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.CREATE,
                            flowRunAction.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB


                // start run mop
                startProcessWithAcc(flowRunAction, nodeRuns, "", username);


                result.setResultCode(RESPONSE_SUCCESS);
                result.setResultMessage("Start running mop: " + selectedFlowTemplate.getFlowTemplateName());

            } catch (Exception e) {
                if (tx.getStatus() != TransactionStatus.ROLLED_BACK && tx.getStatus() != TransactionStatus.COMMITTED) {
                    tx.rollback();
                }
                logger.error(e.getMessage(), e);
                result.setResultMessage("Error when start run mop: " + selectedFlowTemplate.getFlowTemplateName());
                return result;
            } finally {
                if (session.isOpen()) {
                    session.close();
                }
            }
        } catch (Exception e) {
            result.setResultMessage("Error when start run mop: ");
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    private void startProcessWithAcc(FlowRunAction flowRunAction, List<NodeRun> lstNodeRun,
                                     String workorder, String username) throws Exception {
        try {
            MessageObject mesObj;
            mesObj = new MessageObject(flowRunAction.getFlowRunId(),
                    username, null, flowRunAction.getFlowRunName(), "");
            mesObj.setRunType(1);
            mesObj.setErrorMode(1);

            String encrytedMess = new String(org.apache.commons.codec.binary.Base64.encodeBase64((new Gson()).toJson(mesObj).getBytes("UTF-8")), "UTF-8");
            logger.info("vao send message den tien trinh");
            //20181112_tudn_start them cho tri truong ha tang
//            sendMsg2ThreadExecute(encrytedMess);
            CatCountryBO country = flowRunAction.getCountryCode();
            startExecute(encrytedMess, country == null ? "VNM" : country.getCountryCode(), false);
            //20181112_tudn_end them cho tri truong ha tang

            logger.info(">>>>>>>>>>>>> START RUN PROCESS WORKORDER: " + workorder);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }

    private void sendMsg2ThreadExecute(String encrytedMess, Boolean isBussine) throws IOException, Exception, MessageException {
        String serverIp = "";
        int serverPort = 0;
        if (isBussine) {
            serverIp = MessageUtil.getResourceBundleConfig("process_socket_it_business_ip");
            serverPort = Integer.parseInt(MessageUtil.getResourceBundleConfig("process_socket_it_business_port"));
        } else {
            serverIp = MessageUtil.getResourceBundleConfig("process_socket_ip");
            serverPort = Integer.parseInt(MessageUtil.getResourceBundleConfig("process_socket_port"));
        }
        logger.info("server: " + serverIp + "/ port: " + serverPort);
        if (serverIp == null || serverIp.equals("")) {
            throw new MessageException("Not found Ip/port impact");
        }

        SocketClient client = new SocketClient(serverIp, serverPort);
        client.sendMsg(encrytedMess);

        String socketResult = client.receiveResult();
        if (socketResult != null && socketResult.contains("NOK")) {
            throw new MessageException(socketResult);
        } else {
            return;
        }

    }

    //20181112_tudn_start them cho tri truong ha tang
    public void startExecute(String encrytedMess, String countryCode, Boolean isBusiness) throws Exception {
        String usingDbConfig = MessageUtil.getResourceBundleConfig("process_using_db_config");
        if (usingDbConfig != null && "true".equalsIgnoreCase(usingDbConfig.trim())) {
            sendMsg2ThreadExecute(encrytedMess, countryCode, isBusiness);
        } else {
            sendMsg2ThreadExecute(encrytedMess, isBusiness);
        }
    }

    private void sendMsg2ThreadExecute(String encrytedMess, String countryCode, Boolean itBusiness) throws IOException, Exception, MessageException {
        Map<String, Object> filters = new HashMap<>();
        if (itBusiness) {
            filters.put("typeModule", 2L);
        } else {
            filters.put("typeModule", 1L);
        }
        filters.put("countryCode.countryCode-" + MapProcessCountryServiceImpl.EXAC, countryCode);
        filters.put("status", 1l);

        List<MapProcessCountry> maps = new MapProcessCountryServiceImpl().findList(filters);

        if (maps != null && !maps.isEmpty()) {
            //Sap xep lai maps theo thu tu random
            Collections.shuffle(maps);

            int i = 0;
            for (MapProcessCountry process : maps) {
                int serverPort = process.getProcessPort();
                String serverIp = process.getProcessIp();

                SocketClient client = new SocketClient(serverIp, serverPort);
                client.sendMsg(encrytedMess);

                String socketResult = client.receiveResult();
                if (socketResult != null && socketResult.contains("NOK")) {
                    if (i == maps.size() - 1) {
                        throw new MessageException(socketResult);
                    }
                } else {
                    return;
                }
                i++;
            }
        }
    }
    //20181112_tudn_end them cho tri truong ha tang

    //    Quytv7 Ham sinh mop cho ticket canh bao
    @Override
    public ResultTicketAlarmDTO createDtForAlarm(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                                 @WebParam(name = "resultTicketAlarmDTO") ResultTicketAlarmDTO resultTicketAlarmDTO) {
        ResultTicketAlarmDTO result = resultTicketAlarmDTO;
        result.setResultCode(RESPONSE_SUCCESS);
        result.setResultMessage("Success");
        String username = "System";
        logger.info("---start ws---");
        try {
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                result.setResultCode(RESPONSE_FAIL);
                return result;
            }
        } catch (Exception ex) {
            result.setResultMessage("Xảy ra lỗi khi xác thực user/pass");
            logger.error(ex.getMessage(), ex);
            result.setResultCode(RESPONSE_FAIL);
            return result;
        }

        //<editor-fold defaultstate="collapsed" desc="Body">
        logger.info("Kiem tra tham so dau vao");
        try {
            if (resultTicketAlarmDTO == null || resultTicketAlarmDTO.getListTicketAlarmDTO() == null || resultTicketAlarmDTO.getListTicketAlarmDTO().isEmpty()) {
                result.setResultMessage(MessageUtil.getResourceBundleMessage("ws.error.not.found.input"));
                result.setResultCode(RESPONSE_FAIL);
                return result;
            }
            HashMap<String, Object> filters = new HashMap<>();
            logger.info("List ticketAlarm size: " + resultTicketAlarmDTO.getListTicketAlarmDTO().size());
            for (TicketAlarmDTO ticketAlarmDTO : resultTicketAlarmDTO.getListTicketAlarmDTO()) {
                HashMap<String, String> mapAlarmDomain = new HashMap<>();
                ticketAlarmDTO.setResultDetail("");
                ticketAlarmDTO.setResult(RESPONSE_SUCCESS);
                if (isNullOrEmpty(ticketAlarmDTO.getTicketCode()) || ticketAlarmDTO.getTicketId() == null) {
                    ticketAlarmDTO.setResult(RESPONSE_FAIL);
                    ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.ticket.is.null") + ";\n");
                }
                if (ticketAlarmDTO.getAlarmId() == null) {
                    ticketAlarmDTO.setResult(RESPONSE_FAIL);
                    ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.alarm.is.null") + ";\n");
                }
                if (ticketAlarmDTO.getTypeRunMop() == null) {
                    ticketAlarmDTO.setResult(RESPONSE_FAIL);
                    ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.type.run.mop.is.null") + ";\n");
                }
                if (ticketAlarmDTO.getTemplateGroupId() == null) {
                    ticketAlarmDTO.setResult(RESPONSE_FAIL);
                    ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.template.group.is.null") + ";\n");
                }
                if (ticketAlarmDTO.getTemplateId() == null) {
                    ticketAlarmDTO.setResult(RESPONSE_FAIL);
                    ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.template.is.null") + ";\n");
                }
                if (isNullOrEmpty(ticketAlarmDTO.getAlarmDomain())) {
                    ticketAlarmDTO.setResult(RESPONSE_FAIL);
                    ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.alarm.domain.is.null") + ";\n");
                }
                if (ticketAlarmDTO.getResult() == RESPONSE_FAIL) {
                    logger.info("ticketAlarm result fail");
                    continue;

                }
                if (Util.isNullOrEmpty(ticketAlarmDTO.getCountryCode()) || ticketAlarmDTO.getCountryCode().equalsIgnoreCase("?")) {
                    ticketAlarmDTO.setCountryCode("VNM");
                }
                logger.info("ticketAlarm result success, continue create dt");
                try {
                    ObjectMapper mapper = new ObjectMapper();
                    SimpleDateFormat formatDateJson = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    mapper.setDateFormat(formatDateJson);
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    mapAlarmDomain = mapper.readValue(ticketAlarmDTO.getAlarmDomain(), HashMap.class);

                } catch (Exception ex) {
                    logger.info("parse alarm domain fail");
                    ticketAlarmDTO.setResult(RESPONSE_FAIL);
                    ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.parse.domain.alarm.fail") + ";\n");
                    logger.error(ex.getMessage(), ex);
                    continue;
                }
                if (mapAlarmDomain != null && mapAlarmDomain.containsKey("unitToCountry") && !Util.isNullOrEmpty(mapAlarmDomain.get("unitToCountry"))) {
                    ticketAlarmDTO.setCountryCode(mapAlarmDomain.get("unitToCountry").trim());
                }
                logger.info("Bat dau sinh mop ticket: " + ticketAlarmDTO.getTicketCode() + ", alarm: " + ticketAlarmDTO.getAlarmId() + ", group_template: " + ticketAlarmDTO.getTemplateGroupId() + ", template: " + ticketAlarmDTO.getTemplateId());
                try {
                    //tuanda38_20180914_map param alarm_start
                    if (ticketAlarmDTO.getTemplateGroupId() == 5) {
                        logger.info("Bat dau lay template ITBUSINESS");
                        com.viettel.it.model.Action action = new com.viettel.it.persistence.ActionServiceImpl().findById(ticketAlarmDTO.getTemplateId());
                        if (action == null) {
                            ticketAlarmDTO.setResult(RESPONSE_FAIL);
                            ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.template.fail") + ";\n");
                            logger.info(MessageUtil.getResourceBundleMessage("ws.error.get.template.fail"));
                            continue;
                        }
                        logger.info("Ket thuc lay template ITBUSINESS");
                        logger.info("Bat dau phan tich lay tham so system, is_response, monitor_id start");
                        filters.clear();
                        filters.put("action.actionId", ticketAlarmDTO.getTemplateId());
                        filters.put("paramType", 1L);
                        List<FlowTemplateMapAlarm> flowTemplateMapAlarmNodes = new FlowTemplateMapAlarmServiceImpl().findList(filters);
                        boolean check = true;
                        Map<String, String> paramConfigs = new HashMap<>();
                        if (flowTemplateMapAlarmNodes != null && flowTemplateMapAlarmNodes.size() > 0) {
                            for (FlowTemplateMapAlarm flowTemplateMapAlarmNode : flowTemplateMapAlarmNodes) {
                                if (flowTemplateMapAlarmNode.getDomain() != null && mapAlarmDomain.containsKey(flowTemplateMapAlarmNode.getDomain().getDomainCode().toLowerCase().trim())) {
                                    List<String> valuesTemp = getValueRegex(mapAlarmDomain.get(flowTemplateMapAlarmNode.getDomain().getDomainCode().toLowerCase().trim()), flowTemplateMapAlarmNode.getRegex());
                                    if (valuesTemp.size() < 1) {
                                        logger.info("Khong tim duoc gia tri tham so :" + flowTemplateMapAlarmNode.getParamCode());
                                        ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.param.node.fail") + flowTemplateMapAlarmNode.getParamCode() + ";\n");
                                        check = false;
                                        break;
                                    } else if (valuesTemp.size() == 1) {
                                        flowTemplateMapAlarmNode.setParamValue(valuesTemp.get(0));
                                    } else {
                                        flowTemplateMapAlarmNode.setParamValue(org.apache.commons.lang3.StringUtils.join(valuesTemp, ";"));
                                    }
                                    paramConfigs.put(flowTemplateMapAlarmNode.getParamCode().toLowerCase(), flowTemplateMapAlarmNode.getParamValue());
                                    logger.info("Tim duoc gia tri tham so :" + flowTemplateMapAlarmNode.getParamCode() + ", Value = " + flowTemplateMapAlarmNode.getParamValue());
                                }
                            }
                        }
                        if (!check) {
                            ticketAlarmDTO.setResult(RESPONSE_FAIL);
                            continue;
                        }
                        logger.info("Ket thuc phan tich lay tham so system, is_response, monitor_id start");
                        check = true;
                        logger.info("Bat dau phan tich lay tham so tac dong start");
                        HashMap<String, String> mapParamValues = new HashMap<>();
                        filters.clear();
                        filters.put("action.actionId", ticketAlarmDTO.getTemplateId());
                        filters.put("paramType", 0L);
                        List<FlowTemplateMapAlarm> flowTemplateMapAlarm = new FlowTemplateMapAlarmServiceImpl().findList(filters);
                        if (flowTemplateMapAlarm.size() > 0) {
                            for (FlowTemplateMapAlarm flowTemplateMapAlarm1 : flowTemplateMapAlarm) {
                                List<String> valuesTemp = getValueRegex(flowTemplateMapAlarm1.getDomain() == null ? "" : mapAlarmDomain.get(flowTemplateMapAlarm1.getDomain().getDomainCode().toLowerCase().trim()), flowTemplateMapAlarm1.getRegex());
                                if (valuesTemp.size() < 1) {
                                    logger.info("Khong tim duoc gia tri tham so :" + flowTemplateMapAlarm1.getParamCode());
                                    ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.param.node.fail") + flowTemplateMapAlarm1.getParamCode() + ";\n");
                                    check = false;
                                    break;
                                } else {
                                    mapParamValues.put(flowTemplateMapAlarm1.getParamCode().trim(), org.apache.commons.lang3.StringUtils.join(valuesTemp, ";"));
                                    logger.info("Tim duoc gia tri tham so :" + flowTemplateMapAlarm1.getParamCode() + ", Value = " + org.apache.commons.lang3.StringUtils.join(valuesTemp, ";"));
                                }
                            }
                        }
                        if (!check) {
                            ticketAlarmDTO.setResult(RESPONSE_FAIL);
                            continue;
                        }
                        logger.info("Ket thuc phan tich lay tham so tac dong end");
                        ResultDTO resultDTO;
                        if (paramConfigs.size() > 1) {
                            resultDTO = runActionMop(action.getActionId(), mapParamValues, "system", paramConfigs.get("system"), Integer.parseInt(paramConfigs.get("is_response")), Long.parseLong(paramConfigs.get("monitor_id")), ticketAlarmDTO.getTicketCode(), ticketAlarmDTO.getTypeRunMop());
                        } else {
                            resultDTO = runActionMop(action.getActionId(), mapParamValues, "system", null, null, null, ticketAlarmDTO.getTicketCode(), ticketAlarmDTO.getTypeRunMop());
                        }
                        if (resultDTO != null && resultDTO.getResultCode() == 0) {
                            ticketAlarmDTO.setResult(RESPONSE_FAIL);
                            ticketAlarmDTO.setResultDetail(resultDTO.getResultMessage());
                            break;
                        } else {
                            ticketAlarmDTO.setResult(RESPONSE_SUCCESS);
                            ticketAlarmDTO.setResultDetail(resultDTO.getResultMessage());
//                            ticketAlarmDTO.setCreateTime(flowRunAction.getCreateDate());
//                            ticketAlarmDTO.setResultDetail("Create DT success");
//                            ticketAlarmDTO.setDtId(flowRunAction.getFlowRunId());
//                            ticketAlarmDTO.setDtName(flowRunAction.getFlowRunName());
//                            ticketAlarmDTO.setNodes(nodeDTOS);
//                            ticketAlarmDTO.setDtFileContent(org.apache.commons.codec.binary.Base64.encodeBase64String(flowRunAction.getFileContent()));
//                            ticketAlarmDTO.setDtFileName(ZipUtils.getSafeFileName(flowRunAction.getFlowRunId()
//                                    + "_" + ZipUtils.clearHornUnicode(flowRunAction.getFlowRunName()) + ".xlsx"));
//                            ticketAlarmDTO.setDtFileType("xlsx");
                        }
                        //tuanda38_20180914_map param alarm_end
                    } else {
                        if (Arrays.asList(AamConstants.SERVICE_TEMPLATE.STOP, AamConstants.SERVICE_TEMPLATE.START, AamConstants.SERVICE_TEMPLATE.RESTART).contains(ticketAlarmDTO.getTemplateId())) {
                            createServiceMop(ticketAlarmDTO, mapAlarmDomain);
                        } else {

                            GenerateFlowRunController generateFlowRunController = new GenerateFlowRunController();
                            logger.info("Bat dau lay template");
                            FlowTemplates selectedFlowTemplate = new FlowTemplatesServiceImpl().findById(ticketAlarmDTO.getTemplateId());
                            if (selectedFlowTemplate == null) {
                                ticketAlarmDTO.setResult(RESPONSE_FAIL);
                                ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.template.fail") + ";\n");
                                logger.info(MessageUtil.getResourceBundleMessage("ws.error.get.template.fail"));
                                continue;
                            }
                            logger.info("Ket thuc lay template");
                            logger.info("Bat dau phan tich lay tham so node mang start");
                            filters.clear();
                            filters.put("flowTemplates.flowTemplatesId", ticketAlarmDTO.getTemplateId());
                            filters.put("paramType", 1L);
                            List<FlowTemplateMapAlarm> flowTemplateMapAlarmNodes = new FlowTemplateMapAlarmServiceImpl().findList(filters);
                            boolean checkNode = true;
                            List<String> nodesStr = new ArrayList<>();
                            List<Node> nodes = new ArrayList<>();
                            ConcurrentHashMap<String, List<String>> mapParamNode = new ConcurrentHashMap<>();
                            if (flowTemplateMapAlarmNodes.size() == 0) {
                                ticketAlarmDTO.setResult(RESPONSE_FAIL);
                                ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.node.found.map.template.alarm") + ";\n");
//                        result.setResultCode(RESPONSE_FAIL);
                                logger.info(MessageUtil.getResourceBundleMessage("ws.error.node.found.map.template.alarm"));
                                continue;
                            }
                            if (flowTemplateMapAlarmNodes != null && flowTemplateMapAlarmNodes.size() > 0) {
                                for (FlowTemplateMapAlarm flowTemplateMapAlarmNode : flowTemplateMapAlarmNodes) {
                                    if (flowTemplateMapAlarmNode.getDomain() != null && mapAlarmDomain.containsKey(flowTemplateMapAlarmNode.getDomain().getDomainCode().toLowerCase().trim())) {
                                        List<String> valuesTemp = getValueRegex(mapAlarmDomain.get(flowTemplateMapAlarmNode.getDomain().getDomainCode().toLowerCase().trim()), flowTemplateMapAlarmNode.getRegex());
                                        if (valuesTemp.size() < 1) {
                                            logger.info("Khong tim duoc gia tri tham so node:" + flowTemplateMapAlarmNode.getParamCode());
                                            ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.param.node.fail") + flowTemplateMapAlarmNode.getParamCode() + ";\n");
                                            checkNode = false;
                                            break;
                                        } else if (valuesTemp.size() == 1) {
                                            flowTemplateMapAlarmNode.setParamValue(valuesTemp.get(0));
                                        } else {
                                            flowTemplateMapAlarmNode.setParamValue(org.apache.commons.lang3.StringUtils.join(valuesTemp, ";"));
                                        }
                                        mapParamNode.put(flowTemplateMapAlarmNode.getParamCode().toLowerCase().trim(), valuesTemp);
                                        logger.info("Tim duoc gia tri tham so node:" + flowTemplateMapAlarmNode.getParamCode() + ", Value = " + flowTemplateMapAlarmNode.getParamValue());
                                        if (flowTemplateMapAlarmNode.getConfigGetNode().getType() != null && flowTemplateMapAlarmNode.getConfigGetNode().getType().equals(0L)) {
                                            nodesStr = valuesTemp;
                                            break;
                                        }
                                    }
                                }
                                if (!checkNode) {
                                    ticketAlarmDTO.setResult(RESPONSE_FAIL);
                                    continue;
                                }
                                if (nodesStr.size() == 0) {
                                    if (flowTemplateMapAlarmNodes.size() > 0 && flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getType() != null
                                            && flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getType().equals(1L)
                                            && !isNullOrEmpty(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getClassName())
                                            && !isNullOrEmpty(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getFunctionName())
                                            ) {
                                        Class cls = Class.forName(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getClassName());
                                        Method method = cls.getMethod(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getFunctionName(), ConcurrentHashMap.class);
                                        logger.info("Call luong get node method: " + method.getName());
                                        nodesStr = (ArrayList<String>) (method.invoke(cls.newInstance(), mapParamNode));
                                    }
                                }
                            }
                            if (nodesStr.isEmpty()) {
                                logger.info("get node string null roi");
                                ticketAlarmDTO.setResult(RESPONSE_FAIL);
                                ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.node.not.found") + ";\n");
                                logger.info(MessageUtil.getResourceBundleMessage("ws.error.get.node.not.found"));
                                continue;
                            } else {
                                for (String nodeStr : nodesStr) {
                                    //Quytv7_20180523_Sua lay node mang theo ip, server start
                                    Node node = null;
                                    filters.clear();
                                    if (selectedFlowTemplate.getTemplateGroup() != null) {
                                        filters.put("countryCode.countryCode-EXAC", ticketAlarmDTO.getCountryCode());
                                        if (selectedFlowTemplate.getTemplateGroup().getGroupName().equalsIgnoreCase(Config.GroupTemplateName.DATABASE_NODE.value)) {
                                            logger.info("Lay node mang database: " + nodeStr);
                                            filters.put("dbNodeId-EXAC", nodeStr);
                                            filters.put("vendor.vendorId", Config.APP_TYPE.DATABASE.value);
                                        } else if (selectedFlowTemplate.getTemplateGroup().getGroupName().equalsIgnoreCase(Config.GroupTemplateName.DATABASE.value)) {
                                            logger.info("Lay node mang database node " + nodeStr);
                                            filters.put("serverId-EXAC", nodeStr);
                                            filters.put("vendor.vendorId", Config.APP_TYPE.DATABASE.value);
                                        } else {
                                            logger.info("Lay node mang server node " + nodeStr);
                                            filters.put("nodeIp-EXAC", nodeStr);
                                            filters.put("vendor.vendorId", Config.APP_TYPE.SERVER.value);
                                        }
                                    } else {
                                        logger.info("Lay node mang server node " + nodeStr);
                                        filters.put("nodeIp-EXAC", nodeStr);
                                        filters.put("vendor.vendorId", Config.APP_TYPE.SERVER.value);
                                    }

                                    logger.info("---bat dau lay danh sach node chay" + nodeStr + "---");
                                    filters.put("active", Constant.status.active);
                                    List<Node> nodesTemp = new NodeServiceImpl().findList(filters);
                                    if (nodesTemp.size() > 0) {
                                        node = nodesTemp.get(0);
                                    }
                                    logger.info("---ket thuc lay danh sach node chay" + nodeStr + "---");
                                    if (node == null) {
                                        logger.info("---khong tim kiem duoc node mang: " + nodeStr + "---");
                                        ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.node.not.found") + ";\n");
                                    } else {
                                        nodes.add(node);
                                    }
                                    //Quytv7_20180523_Sua lay node mang theo ip, server end
                                }
                            }
                            if (nodes.size() != nodesStr.size()) {
                                ticketAlarmDTO.setResult(RESPONSE_FAIL);
                                ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.node.not.found") + ";\n");
                                logger.info(MessageUtil.getResourceBundleMessage("ws.error.get.node.not.found"));
                                continue;
                            }

                            logger.info("So node mang lay duoc: " + nodes.size());
                            logger.info("Ket thuc phan tich lay tham so node mang end");
                            logger.info("Bat dau phan tich lay tham so tac dong start");
                            HashMap<String, String> mapParamValues = new HashMap<>();
                            filters.clear();
                            filters.put("flowTemplates.flowTemplatesId", ticketAlarmDTO.getTemplateId());
                            filters.put("paramType", 0L);
                            List<FlowTemplateMapAlarm> flowTemplateMapAlarm = new FlowTemplateMapAlarmServiceImpl().findList(filters);
                            if (flowTemplateMapAlarm.size() > 0) {
                                for (FlowTemplateMapAlarm flowTemplateMapAlarm1 : flowTemplateMapAlarm) {
                                    List<String> valuesTemp = getValueRegex(flowTemplateMapAlarm1.getDomain() == null ? "" : mapAlarmDomain.get(flowTemplateMapAlarm1.getDomain().getDomainCode().toLowerCase().trim()), flowTemplateMapAlarm1.getRegex());

                                    if (valuesTemp != null && !valuesTemp.isEmpty()) {
                                        mapParamValues.put(flowTemplateMapAlarm1.getParamCode().toLowerCase().trim(), org.apache.commons.lang3.StringUtils.join(valuesTemp, ";"));
                                    }
                                }
//                            if (mapParamValues == null || mapParamValues.isEmpty()) {
//                                ticketAlarmDTO.setResult(RESPONSE_FAIL);
//                                ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.param.not.found") + ";\n");
//                                logger.info(MessageUtil.getResourceBundleMessage("ws.error.get.param.not.found"));
//                                continue;
//                            }
                            }

                            logger.info("Ket thuc phan tich lay tham so tac dong end");

                            FlowRunAction flowRunAction = new FlowRunAction();
                            List<NodeRun> nodeRuns = new ArrayList<NodeRun>();
                            flowRunAction.setCreateDate(new Date());
                            if (flowRunAction.getFlowRunName() != null) {
                                flowRunAction.setFlowRunName(flowRunAction.getFlowRunName().trim());
                            }
                            flowRunAction.setFlowRunName(ticketAlarmDTO.getDtName());
                            flowRunAction.setCrNumber(ticketAlarmDTO.getTicketCode());
                            flowRunAction.setCreateBy("System");
                            flowRunAction.setFlowTemplates(selectedFlowTemplate);
                            flowRunAction.setTimeRun(new Date());
//                    flowRunAction.setPortRun(8860L);
//                    flowRunAction.setStatus(7L);
                            flowRunAction.setStatus(1L);

                            flowRunAction.setCountryCode(new CatCountryServiceImpl().findById(ticketAlarmDTO.getCountryCode()));

                            List<AccountGroupMop> lstAccGroupMop = new ArrayList<>();
                            logger.info("---Chay vao generateFlowRunController---");
                            generateFlowRunController.setFlowRunAction(flowRunAction);
                            generateFlowRunController.setSelectedFlowTemplates(selectedFlowTemplate);
                            generateFlowRunController.setNodes(new ArrayList<Node>());
                            flowRunAction.setSystemUpdateResult(Constant.systemUpdateResult.GNOC_TT);
//                    generateFlowRunController.loadGroupAction(0l);
                            logger.info("---thoat khoi generateFlowRunController---");

                            Object[] objs = new FlowRunActionServiceImpl().openTransaction();
                            Session session = (Session) objs[0];
                            Transaction tx = (Transaction) objs[1];
                            try {
                                Map<Long, List<ActionOfFlow>> mapGroupAction = new HashMap<>();
                                logger.info("---get mapGroupAction---");
                                for (ActionOfFlow actionOfFlow : selectedFlowTemplate.getActionOfFlows()) {
                                    if (mapGroupAction.get(actionOfFlow.getGroupActionOrder()) == null) {
                                        mapGroupAction.put(actionOfFlow.getGroupActionOrder(), new ArrayList<ActionOfFlow>());
                                    }
                                    mapGroupAction.get(actionOfFlow.getGroupActionOrder()).add(actionOfFlow);
                                }
                                logger.info("---get mapGroupAction thanh cong " + mapGroupAction.size() + "---");
                                new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction, session, tx, false);
                                logger.info("---save  flowRunAction" + flowRunAction + "---");
                                List<ParamValue> paramValues = new ArrayList<ParamValue>();

                                List<NodeRunGroupAction> nodeRunGroupActions = new ArrayList<NodeRunGroupAction>();
                                ArrayList<NodeDTO> nodeDTOS = new ArrayList<>();
                                if (nodes != null && nodes.size() > 0) {
                                    for (Node node : nodes) {
                                        String sql = "select distinct cd.command_detail_id from flow_templates a \n" +
                                                "join action_of_flow b on a.flow_templates_id = b.flow_templates_id\n" +
                                                "join ACTION_DB_SERVER c on c.action_id = b.action_id\n" +
                                                "join action_detail ad on ad.action_id = c.action_id\n" +
                                                "join action_command d on d.action_detail_id = ad.detail_id\n" +
                                                "join command_detail cd on d.command_detail_id = cd.command_detail_id\n" +
                                                "where a.flow_templates_id = ? and cd.vendor_id = ?\n" +
                                                "and cd.version_id = ? and cd.node_type_id = ?";

                                        List<?> commandDetail = new CommandDetailServiceImpl().findListSQLAll(sql, selectedFlowTemplate.getFlowTemplatesId(), node.getVendor().getVendorId(), node.getVersion().getVersionId(), node.getNodeType().getTypeId());
                                        if (commandDetail.size() == 0) {
                                            ticketAlarmDTO.setResult(RESPONSE_FAIL);
                                            ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.node.found.with.node") + ":" + node.getNodeCode() + ";\n");
                                            logger.info(MessageUtil.getResourceBundleMessage("ws.error.node.found.with.node"));
                                            break;
                                        }
                                        NodeDTO nodeDTO = new NodeDTO();
                                        nodeDTO.setNodeCode(node.getNodeCode());
                                        nodeDTO.setNodeIp(node.getNodeIp());
                                        nodeDTOS.add(nodeDTO);
                                        logger.info("chay vao node :" + node.getNodeCode());
                                        NodeRun nodeRun = new NodeRun(new NodeRunId(node.getNodeId(), flowRunAction.getFlowRunId()), flowRunAction, node);
                                        //Quytv7_02102017_thay doi cach lay account/pass tu bang node
//                                nodeRun.setAccount(node.getAccount());
//                                nodeRun.setPassword(node.getPassword());
                                        nodeRuns.add(nodeRun);
                                        /**
                                         * Lay thong tin account tac dong
                                         */
                                        NodeAccount nodeAccount;
                                        if (node.getVendor().getVendorId().equals(Config.APP_TYPE.SERVER.value)) {
                                            nodeAccount = getAccImpactDefault(node, Config.APP_TYPE.SERVER.value, Config.ACCOUNT_IMPACT_MONITOR_TYPE.MONITOR.value);
                                        } else {
                                            nodeAccount = getAccImpactDefault(node, Config.APP_TYPE.DATABASE.value, Config.ACCOUNT_IMPACT_MONITOR_TYPE.IMPACT.value);
                                        }

                                        if (nodeAccount == null) {
                                            result.setResultMessage("Cannot get node account from node: " + node.getNodeCode() + "/username: " + username);
                                            return result;
                                        }
                                        generateFlowRunController.loadGroupAction(node);
                                        paramValues = generateFlowRunController.getParamInputs(node);
                                        logger.info("---ket thuc lay paramValues" + paramValues.size() + "---");
                                        HashMap<String, ParamInput> mapParamInout = generateFlowRunController.getMapParamInOut().get(node);
                                        Multimap<Long, ParamValue> mapParamValueGroup = generateFlowRunController.getMapParamValueGroup().get(node);
                                        for (ParamValue paramValue : paramValues) {
                                            if (paramValue.getParamInput().getReadOnly() || paramValue.getParamInput().getInOut() || (mapParamInout != null && mapParamInout.containsKey(paramValue.getParamInput().getParamCode().trim().toLowerCase()))) {
                                                logger.info("Continue do param inout");
                                                paramValue.setNodeRun(nodeRun);
                                                paramValue.setCreateTime(new Date());
                                                continue;
                                            }
                                            if (mapParamValueGroup != null && mapParamValueGroup.containsKey(paramValue.getParamInput().getParamInputId())) {
                                                logger.info("Continue do param default");
                                                paramValue.setNodeRun(nodeRun);
                                                paramValue.setCreateTime(new Date());
                                                continue;
                                            }
                                            Object value = null;
                                            try {
                                                value = mapParamValues.get((paramValue.getParamCode().toLowerCase().trim().replace(" ", "_").replace(".", "_")));
                                            } catch (Exception e) {
                                                logger.error(e.getMessage(), e);
                                            }
                                            ResourceBundle bundle = ResourceBundle.getBundle("cas");
                                            if (bundle.getString("service").contains("10.61.127.190")) {
                                                if (value == null || value.toString().isEmpty()) {
                                                    value = "TEST_NOT_FOUND";
                                                }
                                            }
                                            if (value != null) {
                                                paramValue.setParamValue(value.toString());
                                            }
                                            paramValue.setNodeRun(nodeRun);
                                            paramValue.setCreateTime(new Date());
                                            paramValue.setParamValueId(null);

                                            if (paramValue.getParamValue() == null || paramValue.getParamValue().isEmpty()) {
                                                ticketAlarmDTO.setResult(RESPONSE_FAIL);
                                                ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.param.not.found") + ":" + paramValue.getParamCode() + ";\n");
                                            }
                                        }
                                        if (ticketAlarmDTO.getResult() == RESPONSE_FAIL) {
                                            break;
                                        }

                                        logger.info(" co vao mapGroupAction size = " + mapGroupAction.size());
                                        for (Map.Entry<Long, List<ActionOfFlow>> entry : mapGroupAction.entrySet()) {
                                            NodeRunGroupAction nodeRunGroupAction = new NodeRunGroupAction(
                                                    new NodeRunGroupActionId(node.getNodeId(),
                                                            flowRunAction.getFlowRunId(),
                                                            entry.getValue().get(0).getStepNum()), entry.getValue().get(0), nodeRun);
                                            nodeRunGroupActions.add(nodeRunGroupAction);
                                            AccountGroupMop accGroup = new AccountGroupMop();
//							accGroup.setGroupOrderRun(groupAction.getActionOfFlows().get(0).getGroupActionOrder());
                                            accGroup.setNodeAccountId(nodeAccount.getId());
                                            accGroup.setNodeId(node.getNodeId());
                                            accGroup.setFlowRunId(flowRunAction.getFlowRunId());
                                            accGroup.setActionOfFlowId(entry.getValue().get(0).getStepNum());

                                            lstAccGroupMop.add(accGroup);

                                        } // end loop for group action
                                        logger.info(" thoai khoi mapGroupAction");
                                    }
                                    if (ticketAlarmDTO.getResult() == RESPONSE_FAIL) {
                                        continue;
                                    }
                                    logger.info(" xoa session");
                                    logger.info(" insert NodeRunServiceImpl ");
                                    new NodeRunServiceImpl().saveOrUpdate(nodeRuns, session, tx, false);
                                    logger.info(" insert ParamValueServiceImpl ");
                                    new ParamValueServiceImpl().saveOrUpdate(paramValues, session, tx, false);

                                    session.flush();
                                    session.clear();
                                    //20180620_tudn_start ghi log DB
                                    try {
                                        LogUtils.logAction(LogUtils.appCode, new Date(), new Date(), getRemoteIp(),
                                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WsAamImpl.class.getName(),
                                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                                LogUtils.ActionType.CREATE,
                                                flowRunAction.toString(), LogUtils.getRequestSessionId());
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                    try {
                                        LogUtils.logAction(LogUtils.appCode, new Date(), new Date(), getRemoteIp(),
                                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WsAamImpl.class.getName(),
                                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                                LogUtils.ActionType.CREATE,
                                                nodeRuns.toString(), LogUtils.getRequestSessionId());
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                    try {
                                        LogUtils.logAction(LogUtils.appCode, new Date(), new Date(), getRemoteIp(),
                                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WsAamImpl.class.getName(),
                                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                                LogUtils.ActionType.CREATE,
                                                paramValues.toString(), LogUtils.getRequestSessionId());
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                    //20190408_chuongtq start check param when create MOP
                                    if(new GenerateFlowRunController().checkConfigCondition(AamConstants.CFG_CHK_PARAM_CONDITION_FOR_AAM)){
                                        LinkedHashMap<String, CheckParamCondition> mapCheckParamCondition = new LinkedHashMap<>();
                                        if(!(new CheckParamCondition().checkParamCondition(selectedFlowTemplate.getParamConditions(), nodes, generateFlowRunController.getMapParamValue(), mapCheckParamCondition,false))){
                                            if (tx.getStatus() != TransactionStatus.ROLLED_BACK && tx.getStatus() != TransactionStatus.COMMITTED) {
                                                tx.rollback();
                                            }
                                            result.setResultCode(RESPONSE_FAIL);
                                            List<CheckParamCondition> lstResult = new ArrayList<CheckParamCondition>(mapCheckParamCondition.values());
                                            String json = (new Gson()).toJson(lstResult);
                                            result.setResultMessage("Create DT fail, json result: ["+ StringEscapeUtils.unescapeJava(json) +"]");
                                            return result;
                                        }
                                    }
                                    //20190408_chuongtq end check param when create MOP
                                    //20180620_tudn_end ghi log DB
                                    new NodeRunGroupActionServiceImpl().saveOrUpdate(nodeRunGroupActions, session, tx, true);
                                    //20180620_tudn_start ghi log DB
                                    try {
                                        LogUtils.logAction(LogUtils.appCode, new Date(), new Date(), getRemoteIp(),
                                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WsAamImpl.class.getName(),
                                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                                LogUtils.ActionType.CREATE,
                                                nodeRunGroupActions.toString(), LogUtils.getRequestSessionId());
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                    //20180620_tudn_end ghi log DB
                                }
                                new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction);
                                //20180620_tudn_start ghi log DB
                                try {
                                    LogUtils.logAction(LogUtils.appCode, new Date(), new Date(), getRemoteIp(),
                                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WsAamImpl.class.getName(),
                                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                                            LogUtils.ActionType.CREATE,
                                            flowRunAction.toString(), LogUtils.getRequestSessionId());
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                                //20180620_tudn_end ghi log DB
                                logger.info(" insert account group mop ");
                                new AccountGroupMopServiceImpl().saveOrUpdate(lstAccGroupMop);
                                //20180620_tudn_start ghi log DB
                                try {
                                    LogUtils.logAction(LogUtils.appCode, new Date(), new Date(), getRemoteIp(),
                                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WsAamImpl.class.getName(),
                                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                                            LogUtils.ActionType.CREATE,
                                            lstAccGroupMop.toString(), LogUtils.getRequestSessionId());
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }
                                //20180620_tudn_end ghi log DB
                                try {
                                    //Save File to database
                                    String file2 = getTemplateMultiExport(MessageUtil.getResourceBundleMessage("key.template.export.dt"));
                                    logger.info(" luu file thanh cong ");
                                    File fileTemp2 = new File("tmp" + new Date().getTime() + ".xlsx");
                                    ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                                    generateFlowRunController.exportToFile(file2, fileTemp2, outStream);
                                    flowRunAction.setFileContent(outStream.toByteArray());
                                    IOUtils.closeQuietly(outStream);
                                    new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction);
                                    //20180620_tudn_start ghi log DB
                                    try {
                                        LogUtils.logAction(LogUtils.appCode, new Date(), new Date(), getRemoteIp(),
                                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WsAamImpl.class.getName(),
                                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                                LogUtils.ActionType.CREATE,
                                                flowRunAction.toString(), LogUtils.getRequestSessionId());
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                    //20180620_tudn_end ghi log DB
                                } catch (Exception ex) {
                                    logger.error(ex.getMessage(), ex);
                                }
                                ticketAlarmDTO.setResult(RESPONSE_SUCCESS);
                                ticketAlarmDTO.setCreateTime(flowRunAction.getCreateDate());
                                ticketAlarmDTO.setResultDetail("Create DT success");
                                ticketAlarmDTO.setDtId(flowRunAction.getFlowRunId());
                                ticketAlarmDTO.setDtName(flowRunAction.getFlowRunName());
                                ticketAlarmDTO.setNodes(nodeDTOS);
                                ticketAlarmDTO.setDtFileContent(org.apache.commons.codec.binary.Base64.encodeBase64String(flowRunAction.getFileContent()));
                                ticketAlarmDTO.setDtFileName(ZipUtils.getSafeFileName(flowRunAction.getFlowRunId()
                                        + "_" + ZipUtils.clearHornUnicode(flowRunAction.getFlowRunName()) + ".xlsx"));
                                ticketAlarmDTO.setDtFileType("xlsx");
                                logger.info("sinh mop success cho ticket: " + ticketAlarmDTO.getTicketCode() + ", alarm: " + ticketAlarmDTO.getAlarmId() + ", group_template: " + ticketAlarmDTO.getTemplateGroupId() + ", template: " + ticketAlarmDTO.getTemplateId());
                                //20180410_Quytv7_Start_DT_start
                                logger.info("Kiem tra loai run mop : " + (ticketAlarmDTO.getTypeRunMop() == null ? "null" : ticketAlarmDTO.getTypeRunMop()));
                                if (ticketAlarmDTO.getTypeRunMop() != null && (ticketAlarmDTO.getTypeRunMop().equals(1L) || ticketAlarmDTO.getTypeRunMop().equals(2L))) {
                                    logger.info("Thuc hien chay mop luon");
                                    try {
                                        MessageObject mesObj;
                                        mesObj = new MessageObject(flowRunAction.getFlowRunId(),
                                                "System", null, flowRunAction.getFlowRunName(), "");
                                        mesObj.setRunType(1);
                                        mesObj.setErrorMode(1);

                                        String encrytedMess = new String(org.apache.commons.codec.binary.Base64.encodeBase64((new Gson()).toJson(mesObj).getBytes("UTF-8")), "UTF-8");
                                        logger.info("vao send message den tien trinh");
                                        //20181112_tudn_start them cho tri truong ha tang
//                                  sendMsg2ThreadExecute(encrytedMess);
                                        CatCountryBO country = flowRunAction.getCountryCode();
                                        startExecute(encrytedMess, country == null ? "VNM" : country.getCountryCode(), false);
                                        //20181112_tudn_end them cho tri truong ha tang

                                        logger.info(">>>>>>>>>>>>> START RUN PROCESS flowRunAction.getFlowRunId(): " + flowRunAction.getFlowRunId());

                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                        throw new Exception(e);
                                    }
                                } else {
                                    logger.info("Loai chay mop la test, nen khong chay mop");
                                }
                                //20180410_Quytv7_Start_DT_end


                            } catch (Exception e) {
                                if (tx.getStatus() != TransactionStatus.ROLLED_BACK && tx.getStatus() != TransactionStatus.COMMITTED) {
                                    tx.rollback();
                                }
                                logger.error(e.getMessage(), e);
                                logger.info("sinh mop fail cho ticket: " + ticketAlarmDTO.getTicketCode() + ", alarm: " + ticketAlarmDTO.getAlarmId() + ", group_template: " + ticketAlarmDTO.getTemplateGroupId() + ", template: " + ticketAlarmDTO.getTemplateId());
                                ticketAlarmDTO.setResult(RESPONSE_FAIL);
                                ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.create.dt.fail") + ";\n");
                                continue;
                            } finally {
                                if (session.isOpen()) {
                                    session.close();
                                }
                            }
                        }
                    }
                } catch (Exception e) {
                    result.setResultMessage("Xảy ra lỗi khi chạy mop");
                    logger.error(e.getMessage(), e);
                    ticketAlarmDTO.setResult(RESPONSE_FAIL);
                    ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.create.dt.fail") + ";\n");
                    continue;
                }
                logger.info("Tao mop thanh cong, tra ve ket qua");

            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.setResultMessage("Xảy ra lỗi khi kiểm tra các tham số đầu vào đã được nhập đầy đủ hay chưa");
        }
        return result;
        //</editor-fold>
    }

    //Quytv7_20180730_Check_plan_service start
    @Override
    public ResultCheckPlanService checkPlanService(@WebParam(name = "userService") String userService,
                                                   @WebParam(name = "passService") String passService,
                                                   @WebParam(name = "serviceCode") String serviceCode) {
        ResultCheckPlanService result = new ResultCheckPlanService();
        result.setResultCode(RESPONSE_SUCCESS);
        result.setResultDetail("Success");
        String username = "vt_admin";
        logger.info("---start ws---");

        try {
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultDetail("Username hoặc mật khẩu webservice không chính xác");
                result.setResultCode(RESPONSE_FAIL);
                result.setStatus(0);
                return result;
            }
        } catch (Exception ex) {
            result.setResultDetail("Xảy ra lỗi khi xác thực user/pass");
            logger.error(ex.getMessage(), ex);
            result.setResultCode(RESPONSE_FAIL);
            result.setStatus(0);
            return result;
        }

        // Kiem tra xem cac tham so cua mop da dc dien day du hay chua
        logger.info("Kiem tra tham so dau vao");
        try {
            if (serviceCode == null) {
                result.setResultDetail(MessageUtil.getResourceBundleMessage("ws.error.not.found.input"));
                result.setResultCode(RESPONSE_FAIL);
                return result;
            }
            logger.info("serviceCode: " + serviceCode);
            List<Long> moduleIds = new ArrayList<>();
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "serviceCodes", null, null, serviceCode));


            RequestInputBO request = new RequestInputBO("AAM_GET_MODULE_IN_SERVICES", 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, "VNM");

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            List<Map<String, Object>> mapObjects = objectMapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {
            });
            for (Map<String, Object> object : mapObjects) {
                moduleIds.add(Long.valueOf((Integer) object.get("MODULE_ID")));
            }
            if (moduleIds.size() > 0) {
                Map<String, Object> filters = new HashMap<>();
                filters.put("moduleId", moduleIds);
                List<ActionModule> lsActionModules = new ActionModuleServiceImpl().findList(filters, null);
                if (lsActionModules != null && !lsActionModules.isEmpty() && lsActionModules.size() > 0) {
                    List<Long> actionIds = new ArrayList<>();
                    for (ActionModule actionModul : lsActionModules) {
                        if (actionModul.getActionId() != null) {
                            actionIds.add(actionModul.getActionId());
                        }
                    }

                    if (actionIds.size() > 0) {
                        List<Long> lstCrStatus = new ArrayList<>();
                        lstCrStatus.add(11L);
                        lstCrStatus.add(12L);
                        lstCrStatus.add(4L);
                        lstCrStatus.add(5L);
                        lstCrStatus.add(6L);
                        lstCrStatus.add(201L);
                        lstCrStatus.add(202L);
                        filters.clear();
                        filters.put("id", actionIds);
                        filters.put("crState", lstCrStatus);

                        List<com.viettel.model.Action> lsActions = new ActionServiceImpl().findList(filters, null);
                        if (lsActions != null && !lsActions.isEmpty() && lsActions.size() > 0) {
                            List<CheckPlanServiceObj> checkPlanServiceObjs = new ArrayList<>();
                            for (com.viettel.model.Action action : lsActions) {
                                CheckPlanServiceObj checkPlanServiceObj = new CheckPlanServiceObj();
                                checkPlanServiceObj.setCrCode(action.getCrNumber());
                                checkPlanServiceObj.setStatusCr(action.getCrState());
                                checkPlanServiceObj.setActionCode(action.getTdCode());
                                checkPlanServiceObjs.add(checkPlanServiceObj);
                                if (action.getCrState().equals(11L)) {
                                    checkPlanServiceObj.setStatusCrName(MessageUtil.getResourceBundleMessage("gnoc.cr.status.11"));
                                } else if (action.getCrState().equals(12L)) {
                                    checkPlanServiceObj.setStatusCrName(MessageUtil.getResourceBundleMessage("gnoc.cr.status.12"));
                                } else if (action.getCrState().equals(4L)) {
                                    checkPlanServiceObj.setStatusCrName(MessageUtil.getResourceBundleMessage("gnoc.cr.status.4"));
                                } else if (action.getCrState().equals(5L)) {
                                    checkPlanServiceObj.setStatusCrName(MessageUtil.getResourceBundleMessage("gnoc.cr.status.5"));
                                } else if (action.getCrState().equals(6L)) {
                                    checkPlanServiceObj.setStatusCrName(MessageUtil.getResourceBundleMessage("gnoc.cr.status.6"));
                                } else if (action.getCrState().equals(201L)) {
                                    checkPlanServiceObj.setStatusCrName(MessageUtil.getResourceBundleMessage("gnoc.cr.status.201"));
                                } else if (action.getCrState().equals(202L)) {
                                    checkPlanServiceObj.setStatusCrName(MessageUtil.getResourceBundleMessage("gnoc.cr.status.202"));
                                }

                            }
                            result.setResultDetail("Have CR have this service");
                            result.setResultCode(RESPONSE_SUCCESS);
                            result.setStatus(1);
                            result.setCheckPlanServiceObjs(checkPlanServiceObjs);
                        } else {
                            logger.info("Not found action have modules of service");
                            result.setResultDetail("Not found service in CR");
                            result.setResultCode(RESPONSE_SUCCESS);
                            result.setStatus(0);
                        }
                    } else {
                        logger.info("Not found action have modules of service");
                        result.setResultDetail("Not found action have modules of service");
                        result.setResultCode(RESPONSE_SUCCESS);
                        result.setStatus(0);
                    }
                } else {
                    logger.info("Not found action have modules of service1");
                    result.setResultDetail("Not found action have modules of service");
                    result.setResultCode(RESPONSE_SUCCESS);
                    result.setStatus(0);
                }
            } else {
                logger.info("List module in service is empty");
                result.setResultDetail("List module in service is empty");
                result.setResultCode(RESPONSE_SUCCESS);
                result.setStatus(0);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.setResultDetail("Xảy ra lỗi khi kiểm tra các tham số đầu vào đã được nhập đầy đủ hay chưa");
        }
        return result;
    }
    //Quytv7_20180730_Check_plan_service end

    private void createServiceMop(TicketAlarmDTO ticketAlarmDTO, HashMap<String, String> mapAlarmDomain) {
        ActionService actionService = new ActionServiceImpl();
        com.viettel.model.Action action = new com.viettel.model.Action();
        List<ActionDetailApp> actionDetailApps = new ArrayList<>();
        List<Module> modules = new ArrayList<>();
        String countryCode = "VNM";

        IimClientService iimClientService = new IimClientServiceImpl();
//        iimClientService.findModuleByCode(ticketAlarmDTO.get)


        Map<String, Object> filters = new HashMap<>();
        GenerateFlowRunController generateFlowRunController = new GenerateFlowRunController();
        logger.info("Bat dau lay template");
        FlowTemplates selectedFlowTemplate = null;
        try {
            selectedFlowTemplate = new FlowTemplatesServiceImpl().findById(ticketAlarmDTO.getTemplateId());
        } catch (com.viettel.it.exception.AppException e) {
            logger.error(e.getMessage(), e);
        }
        if (selectedFlowTemplate == null) {
            ticketAlarmDTO.setResult(RESPONSE_FAIL);
            ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.template.fail") + ";\n");
//            continue;
            return;
        }
        logger.info("Ket thuc lay template");
        logger.info("Bat dau phan tich lay tham so node mang start");
        filters.clear();
        filters.put("flowTemplates.flowTemplatesId", ticketAlarmDTO.getTemplateId());
        filters.put("paramType", 1L);
        List<FlowTemplateMapAlarm> flowTemplateMapAlarmNodes = null;
        try {
            flowTemplateMapAlarmNodes = new FlowTemplateMapAlarmServiceImpl().findList(filters);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        boolean checkNode = true;
        List<String> nodesStr = new ArrayList<>();
//        List<Node> nodes = new ArrayList<>();
        ConcurrentHashMap<String, List<String>> mapParamNode = new ConcurrentHashMap<>();
        if (flowTemplateMapAlarmNodes.size() == 0) {
            ticketAlarmDTO.setResult(RESPONSE_FAIL);
            ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.node.found.map.template.alarm") + ";\n");
//                        result.setResultCode(RESPONSE_FAIL);
//            continue;
            return;
        }
        if (flowTemplateMapAlarmNodes != null && flowTemplateMapAlarmNodes.size() > 0) {
            for (FlowTemplateMapAlarm flowTemplateMapAlarmNode : flowTemplateMapAlarmNodes) {
                if (flowTemplateMapAlarmNode.getDomain() != null && mapAlarmDomain.containsKey(flowTemplateMapAlarmNode.getDomain().getDomainCode().toLowerCase().trim())) {
                    List<String> valuesTemp = getValueRegex(mapAlarmDomain.get(flowTemplateMapAlarmNode.getDomain().getDomainCode().toLowerCase().trim()), flowTemplateMapAlarmNode.getRegex());
                    if (valuesTemp.size() < 1) {
                        logger.info("Khong tim duoc gia tri tham so node:" + flowTemplateMapAlarmNode.getParamCode());
                        ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.param.node.fail") + flowTemplateMapAlarmNode.getParamCode() + ";\n");
                        checkNode = false;
                        break;
                    } else if (valuesTemp.size() == 1) {
                        flowTemplateMapAlarmNode.setParamValue(valuesTemp.get(0));
                    } else {
                        flowTemplateMapAlarmNode.setParamValue(org.apache.commons.lang3.StringUtils.join(valuesTemp, ";"));
                    }
                    mapParamNode.put(flowTemplateMapAlarmNode.getParamCode().toLowerCase().trim(), valuesTemp);
                    logger.info("Tim duoc gia tri tham so node:" + flowTemplateMapAlarmNode.getParamCode() + ", Value = " + flowTemplateMapAlarmNode.getParamValue());
                    if (flowTemplateMapAlarmNode.getConfigGetNode().getType() != null && flowTemplateMapAlarmNode.getConfigGetNode().getType().equals(0L)) {
                        nodesStr = valuesTemp;
                        break;
                    }
                }

                if (!checkNode) {
                    ticketAlarmDTO.setResult(RESPONSE_FAIL);
                    continue;
                }
                if (nodesStr.size() == 0) {
                    if (flowTemplateMapAlarmNodes.size() > 0 && flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getType() != null
                            && flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getType().equals(1L)
                            && !isNullOrEmpty(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getClassName())
                            && !isNullOrEmpty(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getFunctionName())
                            ) {
                        try {
                            Class cls = Class.forName(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getClassName());
                            Method method = cls.getMethod(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getFunctionName(), ConcurrentHashMap.class);
                            logger.info("Call luong get node method: " + method.getName());
                            nodesStr = (ArrayList<String>) (method.invoke(cls.newInstance(), mapParamNode));
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
            if (nodesStr.isEmpty()) {
                logger.info("get node string null roi");
                ticketAlarmDTO.setResult(RESPONSE_FAIL);
                ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.node.not.found") + ";\n");
//                continue;
                return;
            } else {
                try {
                    modules = iimClientService.findModuleByCodes(countryCode, nodesStr);
                } catch (AppException e) {
                    logger.error(e.getMessage(), e);
                }
                /*for (String nodeStr : nodesStr) {
                    Node node = null;
                    filters.clear();
                    filters.put("nodeCode-EXAC", nodeStr);
                    logger.info("---bat dau lay danh sach node chay" + nodeStr + "---");
                    List<Node> nodesTemp = new NodeServiceImpl().findList(filters);
                    if (nodesTemp.size() > 0) {
                        node = nodesTemp.get(0);
                    }
                    logger.info("---ket thuc lay danh sach node chay" + node.getNodeCode() + "---");
                    if (node == null) {
                        logger.info("---khong tim kiem duoc node mang: " + node.getNodeCode() + "---");
                        ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.node.not.found") + ";\n");
                    } else {
                        nodes.add(node);
                    }
                }*/
            }
            if (modules == null || modules.size() != nodesStr.size()) {
                ticketAlarmDTO.setResult(RESPONSE_FAIL);
                ticketAlarmDTO.setResultDetail(ticketAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.node.not.found") + ";\n");
//                continue;
                return;
            } else {
                countryCode = modules.get(0).getCountryCode();
            }

            List<Long> moduleIds = new ArrayList<>();
            for (Module module : modules) {
                logger.info(module.toString());
                moduleIds.add(module.getModuleId());
            }

            Set<Long> impactModuleIds = new HashSet<>(moduleIds);

            List<MdDependent> mdDependents = null;
            try {
                if (AamConstants.SERVICE_TEMPLATE.STOP.equals(ticketAlarmDTO.getTemplateId())) {
                    mdDependents = iimClientService.findMdDependent(countryCode, moduleIds, AamConstants.MD_DEPENDENT.STOP);
                } else if (AamConstants.SERVICE_TEMPLATE.START.equals(ticketAlarmDTO.getTemplateId())) {
                    mdDependents = iimClientService.findMdDependent(countryCode, moduleIds, AamConstants.MD_DEPENDENT.START);
                } else if (AamConstants.SERVICE_TEMPLATE.RESTART.equals(ticketAlarmDTO.getTemplateId())) {
                    mdDependents = iimClientService.findMdDependent(countryCode, moduleIds, AamConstants.MD_DEPENDENT.RESTART);
                }

                if (mdDependents != null)
                    for (MdDependent mdDependent : mdDependents) {
                        impactModuleIds.add(mdDependent.getDependentId());
                    }

                modules = iimClientService.findModulesByIds(countryCode, new ArrayList<>(impactModuleIds));

                for (Module module : modules) {
                    module.setTestbedMode(0);
                    if (AamConstants.SERVICE_TEMPLATE.STOP.equals(ticketAlarmDTO.getTemplateId())) {
                        module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP);
                        ActionDetailApp detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(Constant.STEP_STOP);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(Constant.STEP_START);
                        actionDetailApps.add(detailApp);
                    } else if (AamConstants.SERVICE_TEMPLATE.START.equals(ticketAlarmDTO.getTemplateId())) {
                        module.setActionType(AamConstants.MODULE_GROUP_ACTION.START);
                        ActionDetailApp detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(Constant.STEP_START);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(Constant.STEP_STOP);
                        actionDetailApps.add(detailApp);
                    } else if (AamConstants.SERVICE_TEMPLATE.RESTART.equals(ticketAlarmDTO.getTemplateId())) {
                        module.setActionType(AamConstants.MODULE_GROUP_ACTION.RESTART_STOP_START);

                        ActionDetailApp detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(Constant.STEP_START);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(Constant.STEP_STOP);
                        actionDetailApps.add(detailApp);

                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(Constant.STEP_STOP);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(Constant.STEP_START);
                        actionDetailApps.add(detailApp);
                    }
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }

            action.setCreatedBy("SYSTEM");
            action.setUserExecute("SYSTEM");
//            action.setFullName(fullname);
//            action.setStaffCode(staffCode);
            action.setCreatedTime(new Date());
            action.setActionType(Constant.ACTION_TYPE_CR_UCTT);
            action.setReason(ticketAlarmDTO.getTicketCode());
            action.setBeginTime(org.joda.time.DateTime.now().toDate());
            action.setEndTime(org.joda.time.DateTime.now().plusDays(1).toDate());
            String folder = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            action.setSourceDir(folder);
            action.setId(null);
            action.setCrName(ticketAlarmDTO.getTicketCode());
            action.setCrNumber(ticketAlarmDTO.getTicketCode());
            action.setTicketId(ticketAlarmDTO.getTicketId());
            action.setTemplateId(ticketAlarmDTO.getTemplateId());
            action.setRunStatus(null);
            action.setLinkCrTime(null);
            action.setCrState(null);

            ImpactProcessService impactProcessService = new ImpactProcessServiceImpl();

            try {
                filters.clear();
                filters.put("nationCode", countryCode);
                filters.put("status", Constant.status.active);
                List<ImpactProcess> impactProcessList = impactProcessService.findList(filters,new HashMap<>());
                Collections.shuffle(impactProcessList);
                action.setImpactProcess(impactProcessList.get(0));
                //20190416_tudn_start import rule config
//                actionService.saveOrUpdateAction(action, actionDetailApps, new ArrayList<>(), modules, HashMultimap.create(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Service[0], new Database[0], new ArrayList<>(), new ArrayList<>());
                actionService.saveOrUpdateAction(action, actionDetailApps, new ArrayList<>(), modules, HashMultimap.create(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Service[0], new Database[0], new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                //20190416_tudn_end import rule config

                ticketAlarmDTO.setResult(RESPONSE_SUCCESS);
                ticketAlarmDTO.setCreateTime(action.getCreatedTime());
                ticketAlarmDTO.setResultDetail("Create DT success");
                ticketAlarmDTO.setDtId(action.getId());
                ticketAlarmDTO.setDtName(ticketAlarmDTO.getTicketCode());
                ticketAlarmDTO.setNodes(new ArrayList<>());

                DocxUtil.export(action, ticketAlarmDTO.getTicketCode());

                String prefixName = "MOP.CNTT.";
                String date_time2 = new SimpleDateFormat("ddMMyyyy").format(action.getCreatedTime());
                String cr = ticketAlarmDTO.getTicketCode().split("_")[ticketAlarmDTO.getTicketCode().split("_").length - 1];
                String appName = Util.convertUTF8ToNoSign(new DocxUtil(action).getAppGroupName(action.getId())).replaceAll("\\?", "");
                String mopAction = prefixName + appName + "_" + cr + "_" + date_time2 + "_tacdong_1" + ".docx";
//                String mopRollBack = prefixName + appName + "_" + cr + "_" + date_time2 + "_rollback_" + ".docx";

//                resultDTO = gnocService.createCRTraceFileAttach(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO.getChangeOrginator(), newObj.getCrId(), Constant.DT_EXECUTE, mopAction, org.apache.commons.codec.binary.Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(UploadFileUtils.getMopFolder(newObj) + File.separator + mopAction))));
//                logger.info(resultDTO.getKey());
//                resultDTO = gnocService.createCRTraceFileAttach(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), crDTO.getChangeOrginator(), newObj.getCrId(), Constant.DT_ROLLBACK, mopRollBack, Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(UploadFileUtils.getMopFolder(newObj) + File.separator + mopRollBack))));
//                logger.info(resultDTO.getKey());

                ticketAlarmDTO.setDtFileContent(org.apache.commons.codec.binary.Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(UploadFileUtils.getMopFolder(action) + File.separator + mopAction))));
                ticketAlarmDTO.setDtFileName(mopAction);
                ticketAlarmDTO.setDtFileType("docx");
                logger.info("sinh mop success cho ticket: " + ticketAlarmDTO.getTicketCode() + ", alarm: " + ticketAlarmDTO.getAlarmId() + ", group_template: " + ticketAlarmDTO.getTemplateGroupId() + ", template: " + ticketAlarmDTO.getTemplateId());
                logger.info("Kiem tra loai run mop : " + (ticketAlarmDTO.getTypeRunMop() == null ? "null" : ticketAlarmDTO.getTypeRunMop()));
                if (ticketAlarmDTO.getTypeRunMop() != null && (ticketAlarmDTO.getTypeRunMop().equals(1L) || ticketAlarmDTO.getTypeRunMop().equals(2L))) {
                    logger.info("Thuc hien chay mop luon");
                    AutoThread autoThread = new AutoThread(action);
                    autoThread.run();
                } else {
                    logger.info("Loai chay mop la test, nen khong chay mop");
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }

        /*actionService.saveOrUpdateAction(selectedObj, actionDetailAppController.getListDetailsApp(),
                actionDetailDatabaseController.getDetailDatabases(), new ArrayList<>(impactModules.values()), multimap, kpiDbs,
                testCaseController.getTestCases(), actionCustomGroupController.getCustomGroups(),
                selectedServices, selectedDatabases, actionServers, dualListModel.getTarget());*/
    }

    private NodeAccount getAccImpactDefault(Node node, Long accountType, Long impactOrMonitor) {
        NodeAccount acc = null;
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("impactOrMonitor", impactOrMonitor);
            filters.put("accountType", accountType);
            filters.put("serverId", node.getServerId());
            filters.put("active", Constant.status.active);

            List<NodeAccount> nodeAccounts = new NodeAccountServiceImpl().findList(filters, null);
            if (nodeAccounts != null) {
                for (NodeAccount nodeAccount : nodeAccounts) {
                    if (nodeAccount.getItBusinessNode() == null || nodeAccount.getItBusinessNode() == 0L) {
                        acc = nodeAccount;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            acc = null;
        }
        return acc;
    }

    public static List<String> getValueRegex(String content, String regex) {
        List<String> values = new ArrayList<>();
        try {
            if (isNullOrEmpty(content)) {
                return values;
            }
            if (isNullOrEmpty(regex)) {
                values.add(content);
                return values;
            }
            Pattern patRegex = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            Matcher matcher = patRegex.matcher(content.trim());
            while (matcher.find()) {
                String value = matcher.group(1).trim();
                if (!isNullOrEmpty(value) && !values.contains(value)) {
                    values.add(value);
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return values;
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    public static String getTemplateMultiExport(String fileName) {
        try {
            ServletContext ctx = (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
            return ctx.getRealPath("/") + File.separator + "templates" + File.separator + fileName;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return "";
        }

    }

    public static void main(String args[]) {
        NodeAccount acc = null;
        try {
            Node node = new NodeServiceImpl().get(483911l);
            Map<String, Object> filters = new HashMap<>();
            filters.put("impactOrMonitor", 1l);
            filters.put("accountType", 2l);
            filters.put("serverId", node.getServerId());
            filters.put("active", Constant.status.active);

            List<NodeAccount> nodeAccounts = new NodeAccountServiceImpl().findList(filters, null);
            System.out.println(nodeAccounts.size());
            if (nodeAccounts != null) {
                for (NodeAccount nodeAccount : nodeAccounts) {
                    if (nodeAccount.getItBusinessNode() == null || nodeAccount.getItBusinessNode() == 0L) {
                        acc = nodeAccount;
                        break;
                    }
                }
            }
            System.out.println(acc.getUsername());
        } catch (Exception e) {
            e.printStackTrace();
        }

//        try {
////            int runningStatus1 = -1;
////            //            Client client = AamClientFactory.create("HC7ldk+Org4=", "zt4SUkfJ+6c=");
////            Client client = AamClientFactory.create("bed0c2ddbfff2e0e3077e2dc2885b38a", "6f2efd0770ec8d37c22a0e885a32707f");
////            WebTarget webTarget = client.target("http://10.60.5.6:8092/aam").path("execute").path("get").path("1527671675975");
////            //            WebTarget webTarget = client.target("http://10.60.5.133:8092/aam").path("execute").path("get").path(action.getRunId().toString());
////            Invocation.Builder builder = webTarget.request(MediaType.APPLICATION_JSON_TYPE);
////            Response response = builder.get();
////            if (response.getStatus() == Response.Status.OK.getStatusCode()) {
////                ChecklistInfo checklistInfo = response.readEntity(ChecklistInfo.class);
////                logger.info("\t" + checklistInfo.getRunId() + "\t" + checklistInfo.getAction().getCrNumber());
////                Action actionByRunId = checklistInfo.getAction();
////                if (actionByRunId != null) {
////                    if (actionByRunId.getRunningStatus() != null
////                            && actionByRunId.getRunningStatus() != AamConstants.RUNNING_STATUS.RUNNING) {
////                        runningStatus1 = actionByRunId.getRunningStatus();
////                    } else {
////                        runningStatus1 = AamConstants.RUNNING_STATUS.RUNNING;
////                    }
////                }
////            }
//
////            Map<String, Object> prFilters = new HashMap<>();
////            prFilters.put("status", "1");
////            prFilters.put("name", "VIETTEL");
////            try {
////                List<ImpactProcess> processes = new ImpactProcessServiceImpl().findList(prFilters, new HashMap<>());
////                for (ImpactProcess process : processes) {
////                    System.out.println(process);
////                }
////            } catch (com.viettel.exception.AppException e) {
////                logger.error(e.getMessage(), e);
////            }
//
//            WsAamImpl ws = new WsAamImpl();
////            // domain code ncms test 1
//////                String alarmDomain = "{\n" +
//////            "  \"fault_name\" : \"UNEQUIPPED SDH LOWER ORDER PATH SIGNAL\",\n" +
//////            "  \"fault_group_name\" : \"Cảnh báo BSS\",\n" +
//////            "  \"content\" : \"BCPD63                 STMU-2      TRANSM    2017-10-10  23:52:57.75\\n*   ALARM  SET-10     1A001-03    0557H                                \\n    (1520) 3984 UNEQUIPPED SDH LOWER ORDER PATH SIGNAL                          \\n    01 27\",\n" +
//////            "  \"country_code\" : \"VNM\",\n" +
//////            "  \"device_type_name\" : \"BSC\",\n" +
//////            "  \"nims_device_code\" : \"BCPD63\",\n" +
//////            "  \"device_code\" : \"BCPD63\",\n" +
//////            "  \"addition_info\" : \"tult3\",\n" +
//////                        "  \"vendor_code\" : \"Huawei\",\n" +
//////                        "  \"module_code\" : \"VTN_CNTT_VAS_066_208\",\n" +
//////            "  \"device_ip\" : \"10.59.36.193\"\n" +
//////            "}";
//            // case run ok
////            String alarmDomain1 = "{\n" +
////                    "  \"type\" : \"2\",\n" +
////                    "  \"logserverId\" : \"485640\",\n" +
////                    "  \"xuan4\" : \"2\"\n" +
////                    "}";
//            // case run for templateId 21151	KB with other - groupId 0
//            String alarmDomain1 = "{\n" +
//                    "  \"type\" : \"2\",\n" +
//                    "  \"logserverId\" : \"10.60.5.6\",\n" +
//                    "  \"xuan3\" : \"2\"\n" +
//                    "}";
////            String alarmDomain2= "{\n" +
////                    "  \"node_code\" : \"antest11\",\n" +
////                    "  \"device_ip\" : \"10.60.105.103\",\n" +
////                    "  \"country_code\" : \"VNM\"\n" +
////                    "}";
//            List<String> lstDomain = new ArrayList<>();
//            lstDomain.add(alarmDomain1);
////            lstDomain.add(alarmDomain2);
//            AuditAlarmDTO auditAlarmDTO = new AuditAlarmDTO();
//            auditAlarmDTO.setAuditCode("AD123");
//            auditAlarmDTO.setAuditId(221L);
//            auditAlarmDTO.setDtName("create audit for alarm test");
//            // case run ok
////            auditAlarmDTO.setTemplateGroupId(0L);
////            auditAlarmDTO.setTemplateId(21145L);
//            // case run for templateId 21151	KB with other - groupId 0
//            auditAlarmDTO.setTemplateGroupId(0L);
//            auditAlarmDTO.setTemplateId(21151L);
//            auditAlarmDTO.setDomain(lstDomain);
//            auditAlarmDTO.setResult(RESPONSE_SUCCESS);
//
//            AuditAlarmDTO a = ws.createDtAudit("vipa", "Vipa@123", auditAlarmDTO);
//
//
////            String pass = com.viettel.util.PasswordEncoder.decrypt("7QyphZCvVZ8JLEVyavmPLg==");
////            System.out.println(pass);
//////            String alarmDomain = "{\n" +
//////                    "  \"fault_name\" : \"UNEQUIPPED SDH LOWER ORDER PATH SIGNAL\",\n" +
//////                    "  \"fault_group_name\" : \"Cảnh báo BSS\",\n" +
//////                    "  \"content\" : \"BCPD63                 STMU-2      TRANSM    2017-10-10  23:52:57.75\\n*   ALARM  SET-10     1A001-03    0557H                                \\n    (1520) 3984 UNEQUIPPED SDH LOWER ORDER PATH SIGNAL                          \\n    01 27\",\n" +
//////                    "  \"country_code\" : \"VNM\",\n" +
//////                    "  \"device_type_name\" : \"BSC\",\n" +
//////                    "  \"nims_device_code\" : \"BCPD63\",\n" +
//////                    "  \"device_code\" : \"BCPD63\",\n" +
//////                    "  \"addition_info\" : \"tult3\",\n" +
//////                    "  \"vendor_code\" : \"Huawei\",\n" +
//////                    "  \"device_ip\" : \"10.59.36.193\"\n" +
//////                    "}";
//////            ObjectMapper mapper = new ObjectMapper();
//////            SimpleDateFormat formatDateJson = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//////            mapper.setDateFormat(formatDateJson);
//////            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//////            HashMap<String, Object> tables = mapper.readValue(alarmDomain, HashMap.class);
//////            ArrayList arrayLists = (ArrayList) tables.get("data");
////
////
////            Pattern pattern = Pattern.compile("^(.+_.+_.+\\d+_\\d+).*");
////            String content = "VTN_CNTT_VAS_066_208";
////
////            Matcher matcher = pattern.matcher(content);
////            if (matcher.find()) {
////                logger.info(matcher.group(1));
////            }
//
//
////            // Test for auto create thread
////            WsAamImpl ws = new WsAamImpl();
////            Action action = ws.createActionThread(16484l);
//            int runningStatus = -1;
////            int i = 0;
////            do {
////
////                runningStatus = ws.executeImpactCr(action);
////                i ++;
////            } while (i <5);
////            System.out.println(runningStatus);
//
////            List<String> valuesTemp = getValueRegex("._._.12_21", "^(.+_.+_.+\\d+_\\d+).*");
//
////            WsAamImpl ws = new WsAamImpl();
////                String alarmDomain = "{\n" +
////            "  \"fault_name\" : \"UNEQUIPPED SDH LOWER ORDER PATH SIGNAL\",\n" +
////            "  \"fault_group_name\" : \"Cảnh báo BSS\",\n" +
////            "  \"content\" : \"BCPD63                 STMU-2      TRANSM    2017-10-10  23:52:57.75\\n*   ALARM  SET-10     1A001-03    0557H                                \\n    (1520) 3984 UNEQUIPPED SDH LOWER ORDER PATH SIGNAL                          \\n    01 27\",\n" +
////            "  \"country_code\" : \"VNM\",\n" +
////            "  \"device_type_name\" : \"BSC\",\n" +
////            "  \"nims_device_code\" : \"BCPD63\",\n" +
////            "  \"device_code\" : \"BCPD63\",\n" +
////            "  \"addition_info\" : \"tult3\",\n" +
////                        "  \"vendor_code\" : \"Huawei\",\n" +
////                        "  \"module_code\" : \"VTN_CNTT_VAS_066_208\",\n" +
////            "  \"device_ip\" : \"10.59.36.193\"\n" +
////            "}";
////            ResultTicketAlarmDTO paraResultTicketAlarmDTO = new ResultTicketAlarmDTO();
////            List<TicketAlarmDTO> lstTicketAlarmDTO = new ArrayList<>();
////            TicketAlarmDTO ticketAlarmDTO = new TicketAlarmDTO();
////            ticketAlarmDTO.setTicketCode("code");
////            ticketAlarmDTO.setTicketId(0L);
////            ticketAlarmDTO.setAlarmId(0L);
////            ticketAlarmDTO.setTemplateGroupId(0L);
////            ticketAlarmDTO.setTemplateId(20994L);
////            ticketAlarmDTO.setAlarmDomain(alarmDomain);
////            ticketAlarmDTO.setResult(RESPONSE_SUCCESS);
////            lstTicketAlarmDTO.add(ticketAlarmDTO);
////            paraResultTicketAlarmDTO.setListTicketAlarmDTO(lstTicketAlarmDTO);
////            ResultTicketAlarmDTO a = ws.createDtForAlarm("vipa", "Vipa@123", paraResultTicketAlarmDTO);
////            String pass = com.viettel.util.PasswordEncoder.decrypt("7QyphZCvVZ8JLEVyavmPLg==");
////            System.out.println(pass);
//////            String alarmDomain = "{\n" +
//////                    "  \"fault_name\" : \"UNEQUIPPED SDH LOWER ORDER PATH SIGNAL\",\n" +
//////                    "  \"fault_group_name\" : \"Cảnh báo BSS\",\n" +
//////                    "  \"content\" : \"BCPD63                 STMU-2      TRANSM    2017-10-10  23:52:57.75\\n*   ALARM  SET-10     1A001-03    0557H                                \\n    (1520) 3984 UNEQUIPPED SDH LOWER ORDER PATH SIGNAL                          \\n    01 27\",\n" +
//////                    "  \"country_code\" : \"VNM\",\n" +
//////                    "  \"device_type_name\" : \"BSC\",\n" +
//////                    "  \"nims_device_code\" : \"BCPD63\",\n" +
//////                    "  \"device_code\" : \"BCPD63\",\n" +
//////                    "  \"addition_info\" : \"tult3\",\n" +
//////                    "  \"vendor_code\" : \"Huawei\",\n" +
//////                    "  \"device_ip\" : \"10.59.36.193\"\n" +
//////                    "}";
//////            ObjectMapper mapper = new ObjectMapper();
//////            SimpleDateFormat formatDateJson = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
//////            mapper.setDateFormat(formatDateJson);
//////            mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
//////            HashMap<String, Object> tables = mapper.readValue(alarmDomain, HashMap.class);
//////            ArrayList arrayLists = (ArrayList) tables.get("data");
////
////
////            Pattern pattern = Pattern.compile("^(.+_.+_.+\\d+_\\d+).*");
////            String content = "VTN_CNTT_VAS_066_208";
////
////            Matcher matcher = pattern.matcher(content);
////            if (matcher.find()) {
////                logger.info(matcher.group(1));
////            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
    }

    // anhnt2 Ham sinh mop cho audit canh bao
    @Override
    public AuditAlarmDTO createDtAudit(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                       @WebParam(name = "auditAlarmDTO") AuditAlarmDTO auditAlarmDTO) {
        AuditAlarmDTO result = auditAlarmDTO;
        result.setResult(RESPONSE_SUCCESS);
        result.setResultDetail("Success");
        String username = "vt_admin";
        logger.info("---start ws---");
        try {
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultDetail("Username hoặc mật khẩu webservice không chính xác");
                result.setResult(RESPONSE_FAIL);
                return result;
            }
        } catch (Exception ex) {
            result.setResultDetail("Xảy ra lỗi khi xác thực user/pass");
            logger.error(ex.getMessage(), ex);
            result.setResult(RESPONSE_FAIL);
            return result;
        }

        // Kiem tra xem cac tham so cua mop da dc dien day du hay chua
        logger.info("Kiem tra tham so dau vao");
        try {
            if (auditAlarmDTO == null) {
                result.setResultDetail(MessageUtil.getResourceBundleMessage("ws.error.not.found.input"));
                result.setResult(RESPONSE_FAIL);
                return result;
            }
            HashMap<String, Object> filters = new HashMap<>();
            logger.info("List auditAlarm size: " + auditAlarmDTO);
//            for (AuditAlarmDTO auditAlarmDTO : resultAuditAlarmDTO.getListAuditAlarmDTO()) {
            List<HashMap<String, String>> lstMapAlarmDomain = new ArrayList<>();
            auditAlarmDTO.setResultDetail("");
            auditAlarmDTO.setResult(RESPONSE_SUCCESS);
            if (isNullOrEmpty(auditAlarmDTO.getAuditCode()) || auditAlarmDTO.getAuditId() == null) {
                auditAlarmDTO.setResult(RESPONSE_FAIL);
                auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.audit.is.null") + ";\n");
            }
            if (auditAlarmDTO.getTemplateGroupId() == null) {
                auditAlarmDTO.setResult(RESPONSE_FAIL);
                auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.template.group.is.null") + ";\n");
            }
            if (auditAlarmDTO.getTemplateId() == null) {
                auditAlarmDTO.setResult(RESPONSE_FAIL);
                auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.template.is.null") + ";\n");
            }
            if (auditAlarmDTO.getDomain() == null) {
                auditAlarmDTO.setResult(RESPONSE_FAIL);
                auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.alarm.domain.is.null") + ";\n");
            }
            if (auditAlarmDTO.getResult() == RESPONSE_FAIL) {
                logger.info("auditAlarm result fail");
                return result;

            }
            logger.info("auditAlarm result success, continue create dt");
            try {
                for (String domain : auditAlarmDTO.getDomain()) {
                    // anhnt2
                    HashMap<String, String> mapAlarmDomain = new HashMap<>();
                    ObjectMapper mapper = new ObjectMapper();
                    SimpleDateFormat formatDateJson = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
                    mapper.setDateFormat(formatDateJson);
                    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                    mapAlarmDomain = mapper.readValue(domain.toLowerCase().trim(), HashMap.class);
                    lstMapAlarmDomain.add(mapAlarmDomain);
                }

            } catch (Exception ex) {
                logger.info("parse alarm domain fail");
                auditAlarmDTO.setResult(RESPONSE_FAIL);
                auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.parse.domain.alarm.fail") + ";\n");
                logger.error(ex.getMessage(), ex);
                return result;
            }
            logger.info("Bat dau sinh mop audit: " + auditAlarmDTO.getAuditCode() + ", group_template: " + auditAlarmDTO.getTemplateGroupId() + ", template: " + auditAlarmDTO.getTemplateId());
            try {
                if (Arrays.asList(AamConstants.SERVICE_TEMPLATE.STOP, AamConstants.SERVICE_TEMPLATE.START, AamConstants.SERVICE_TEMPLATE.RESTART).contains(auditAlarmDTO.getTemplateId())) {
                    createServiceMopAudit(auditAlarmDTO, lstMapAlarmDomain);
                } else {

                    GenerateFlowRunController generateFlowRunController = new GenerateFlowRunController();
                    logger.info("Bat dau lay template");
                    FlowTemplates selectedFlowTemplate = new FlowTemplatesServiceImpl().findById(auditAlarmDTO.getTemplateId());
                    if (selectedFlowTemplate == null) {
                        auditAlarmDTO.setResult(RESPONSE_FAIL);
                        auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.template.fail") + ";\n");
                        return result;
                    }
                    logger.info("Ket thuc lay template");
                    logger.info("Bat dau phan tich lay tham so node mang start");
                    filters.clear();
                    filters.put("flowTemplates.flowTemplatesId", auditAlarmDTO.getTemplateId());
                    filters.put("paramType", 1L);
                    List<FlowTemplateMapAlarm> flowTemplateMapAlarmNodes = new FlowTemplateMapAlarmServiceImpl().findList(filters);
                    boolean checkNode = true;
                    List<String> nodesStr = new ArrayList<>();
                    List<Node> nodes = new ArrayList<>();
                    ConcurrentHashMap<String, List<String>> mapParamNode = new ConcurrentHashMap<>();
                    if (flowTemplateMapAlarmNodes.size() == 0) {
                        auditAlarmDTO.setResult(RESPONSE_FAIL);
                        auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.node.found.map.template.alarm") + ";\n");
//                        result.setResultCode(RESPONSE_FAIL);
                        return result;
                    }
                    if (flowTemplateMapAlarmNodes != null && flowTemplateMapAlarmNodes.size() > 0) {
                        for (FlowTemplateMapAlarm flowTemplateMapAlarmNode : flowTemplateMapAlarmNodes) {
                            // anhnt02
                            for (HashMap<String, String> mapAlarmDomain : lstMapAlarmDomain) {
                                if (flowTemplateMapAlarmNode.getDomain() != null && mapAlarmDomain.containsKey(flowTemplateMapAlarmNode.getDomain().getDomainCode().toLowerCase().trim())) {
                                    List<String> valuesTemp = getValueRegex(mapAlarmDomain.get(flowTemplateMapAlarmNode.getDomain().getDomainCode().toLowerCase().trim()), flowTemplateMapAlarmNode.getRegex());
                                    if (valuesTemp.size() < 1) {
                                        logger.info("Khong tim duoc gia tri tham so node:" + flowTemplateMapAlarmNode.getParamCode());
                                        auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.param.node.fail") + flowTemplateMapAlarmNode.getParamCode() + ";\n");
                                        checkNode = false;
                                        break;
                                    } else if (valuesTemp.size() == 1) {
                                        flowTemplateMapAlarmNode.setParamValue(valuesTemp.get(0));
                                    } else {
                                        flowTemplateMapAlarmNode.setParamValue(org.apache.commons.lang3.StringUtils.join(valuesTemp, ";"));
                                    }
                                    mapParamNode.put(flowTemplateMapAlarmNode.getParamCode().toLowerCase().trim(), valuesTemp);
                                    nodesStr.addAll(valuesTemp);
                                }
                            }
                            logger.info("Tim duoc gia tri tham so node:" + flowTemplateMapAlarmNode.getParamCode() + ", Value = " + flowTemplateMapAlarmNode.getParamValue());
                            if (flowTemplateMapAlarmNode.getConfigGetNode().getType() != null && flowTemplateMapAlarmNode.getConfigGetNode().getType().equals(0L)) {
                                break;
                            }
                        }
                        if (!checkNode) {
                            auditAlarmDTO.setResult(RESPONSE_FAIL);
                            return result;
                        }
                        if (nodesStr.size() == 0) {
                            if (flowTemplateMapAlarmNodes.size() > 0 && flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getType() != null
                                    && flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getType().equals(1L)
                                    && !isNullOrEmpty(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getClassName())
                                    && !isNullOrEmpty(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getFunctionName())
                                    ) {
                                Class cls = Class.forName(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getClassName());
                                Method method = cls.getMethod(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getFunctionName(), ConcurrentHashMap.class);
                                logger.info("Call luong get node method: " + method.getName());
                                nodesStr = (ArrayList<String>) (method.invoke(cls.newInstance(), mapParamNode));
                            }
                        }
                    }
                    if (nodesStr.isEmpty()) {
                        logger.info("get node string null roi");
                        auditAlarmDTO.setResult(RESPONSE_FAIL);
                        auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.node.not.found") + ";\n");
                        return result;
                    } else {
                        for (String nodeStr : nodesStr) {
                            //Quytv7_20180523_Sua lay node mang theo ip, server start
                            Node node = null;
                            filters.clear();
                            if (selectedFlowTemplate.getTemplateGroup() != null) {
                                if (selectedFlowTemplate.getTemplateGroup().getGroupName().equalsIgnoreCase(Config.GroupTemplateName.DATABASE_NODE.value)) {
                                    logger.info("Lay node mang database: " + nodeStr);
                                    filters.put("dbNodeId-EXAC", nodeStr);
                                    filters.put("vendor.vendorId", Config.APP_TYPE.DATABASE.value);
                                } else if (selectedFlowTemplate.getTemplateGroup().getGroupName().equalsIgnoreCase(Config.GroupTemplateName.DATABASE.value)) {
                                    logger.info("Lay node mang database node " + nodeStr);
                                    filters.put("serverId-EXAC", nodeStr);
                                    filters.put("vendor.vendorId", Config.APP_TYPE.DATABASE.value);
                                } else {
                                    logger.info("Lay node mang server node " + nodeStr);
                                    filters.put("nodeIp-EXAC", nodeStr);
                                    filters.put("vendor.vendorId", Config.APP_TYPE.SERVER.value);
                                }
                            } else {
                                logger.info("Lay node mang server node " + nodeStr);
                                filters.put("nodeIp-EXAC", nodeStr);
                                filters.put("vendor.vendorId", Config.APP_TYPE.SERVER.value);
                            }

                            logger.info("---bat dau lay danh sach node chay" + nodeStr + "---");
                            filters.put("active", Constant.status.active);
                            List<Node> nodesTemp = new NodeServiceImpl().findList(filters);
                            if (nodesTemp.size() > 0) {
                                node = nodesTemp.get(0);
                            }
                            logger.info("---ket thuc lay danh sach node chay" + nodeStr + "---");
                            if (node == null) {
                                logger.info("---khong tim kiem duoc node mang: " + node.getNodeCode() + "---");
                                auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.node.not.found") + ";\n");
                            } else {
                                nodes.add(node);
                            }
                            //Quytv7_20180523_Sua lay node mang theo ip, server end
                        }
                    }
                    if (nodes.size() != nodesStr.size()) {
                        auditAlarmDTO.setResult(RESPONSE_FAIL);
                        auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.node.not.found") + ";\n");
                        return result;
                    }

                    logger.info("So node mang lay duoc: " + nodes.size());
                    logger.info("Ket thuc phan tich lay tham so node mang end");
                    logger.info("Bat dau phan tich lay tham so tac dong start");
                    HashMap<String, String> mapParamValues = new HashMap<>();
                    filters.clear();
                    filters.put("flowTemplates.flowTemplatesId", auditAlarmDTO.getTemplateId());
                    filters.put("paramType", 0L);
                    List<FlowTemplateMapAlarm> flowTemplateMapAlarm = new FlowTemplateMapAlarmServiceImpl().findList(filters);
                    if (flowTemplateMapAlarm.size() > 0) {
                        for (FlowTemplateMapAlarm flowTemplateMapAlarm1 : flowTemplateMapAlarm) {
                            // anhnt02
                            for (HashMap<String, String> mapAlarmDomain : lstMapAlarmDomain) {
                                List<String> valuesTemp = getValueRegex(flowTemplateMapAlarm1.getDomain() == null ? "" : mapAlarmDomain.get(flowTemplateMapAlarm1.getDomain().getDomainCode().toLowerCase().trim()), flowTemplateMapAlarm1.getRegex());

                                if (valuesTemp != null && !valuesTemp.isEmpty()) {
                                    mapParamValues.put(flowTemplateMapAlarm1.getParamCode().toLowerCase().trim(), org.apache.commons.lang3.StringUtils.join(valuesTemp, ";"));
                                }
                            }
                        }
                    }
                    if (mapParamValues == null || mapParamValues.isEmpty()) {
                        auditAlarmDTO.setResult(RESPONSE_FAIL);
                        auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.param.not.found") + ";\n");
                        return result;
                    }
                    logger.info("Ket thuc phan tich lay tham so tac dong end");

                    FlowRunAction flowRunAction = new FlowRunAction();
                    List<NodeRun> nodeRuns = new ArrayList<NodeRun>();
                    flowRunAction.setCreateDate(new Date());
                    if (flowRunAction.getFlowRunName() != null) {
                        flowRunAction.setFlowRunName(flowRunAction.getFlowRunName().trim());
                    }
                    flowRunAction.setFlowRunName(auditAlarmDTO.getDtName());
                    flowRunAction.setCrNumber(auditAlarmDTO.getAuditCode());
                    flowRunAction.setCreateBy("System");
                    flowRunAction.setExecuteBy("System");
                    flowRunAction.setFlowTemplates(selectedFlowTemplate);
                    // anhnt2 - Luong tac dong
                    flowRunAction.setExecuteType(selectedFlowTemplate.getTemplateType());
                    flowRunAction.setTimeRun(new Date());
                    flowRunAction.setStatus(Long.valueOf(AamConstants.RUN_STATUS.RUNNING_STATUS));
                    if (nodes.size() > 0) {
                        flowRunAction.setCountryCode(nodes.get(0).getCountryCode());
                    }

                    List<AccountGroupMop> lstAccGroupMop = new ArrayList<>();
                    logger.info("---Chay vao generateFlowRunController---");
                    generateFlowRunController.setFlowRunAction(flowRunAction);
                    generateFlowRunController.setSelectedFlowTemplates(selectedFlowTemplate);
                    generateFlowRunController.setNodes(new ArrayList<Node>());
                    // anhnt2 - Create with NCMS
                    flowRunAction.setSystemUpdateResult(Constant.systemUpdateResult.NCMS);
                    logger.info("---thoat khoi generateFlowRunController---");
                    Object[] objs = new FlowRunActionServiceImpl().openTransaction();
                    Session session = (Session) objs[0];
                    Transaction tx = (Transaction) objs[1];
                    try {
                        Map<Long, List<ActionOfFlow>> mapGroupAction = new HashMap<>();
                        logger.info("---get mapGroupAction---");
                        for (ActionOfFlow actionOfFlow : selectedFlowTemplate.getActionOfFlows()) {
                            if (mapGroupAction.get(actionOfFlow.getGroupActionOrder()) == null) {
                                mapGroupAction.put(actionOfFlow.getGroupActionOrder(), new ArrayList<ActionOfFlow>());
                            }
                            mapGroupAction.get(actionOfFlow.getGroupActionOrder()).add(actionOfFlow);
                        }
                        logger.info("---get mapGroupAction thanh cong " + mapGroupAction.size() + "---");
                        new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction, session, tx, false);
                        logger.info("---save  flowRunAction" + flowRunAction + "---");
                        List<ParamValue> paramValues = new ArrayList<ParamValue>();

                        List<NodeRunGroupAction> nodeRunGroupActions = new ArrayList<NodeRunGroupAction>();
                        ArrayList<NodeDTO> nodeDTOS = new ArrayList<>();
                        Set<Long> lstCommandDetailId = new HashSet<>();
                        if (nodes != null && nodes.size() > 0) {
                            for (Node node : nodes) {
                                String sql = "select distinct cd.command_detail_id from flow_templates a \n" +
                                        "join action_of_flow b on a.flow_templates_id = b.flow_templates_id\n" +
                                        "join ACTION_DB_SERVER c on c.action_id = b.action_id\n" +
                                        "join action_detail ad on ad.action_id = c.action_id\n" +
                                        "join action_command d on d.action_detail_id = ad.detail_id\n" +
                                        "join command_detail cd on d.command_detail_id = cd.command_detail_id\n" +
                                        "where a.flow_templates_id = ? and cd.vendor_id = ?\n" +
                                        "and cd.version_id = ? and cd.node_type_id = ?";

                                List<?> commandDetail = new CommandDetailServiceImpl().findListSQLAll(sql, selectedFlowTemplate.getFlowTemplatesId(), node.getVendor().getVendorId(), node.getVersion().getVersionId(), node.getNodeType().getTypeId());
                                if (commandDetail.size() == 0) {
                                    auditAlarmDTO.setResult(RESPONSE_FAIL);
                                    auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.node.found.with.node") + ":" + node.getNodeCode() + ";\n");
                                    break;
                                }
                                for (Object o : commandDetail) {
                                    BigDecimal bdId = (BigDecimal) o;
                                    lstCommandDetailId.add(bdId.longValue());
                                }

                                NodeDTO nodeDTO = new NodeDTO();
                                nodeDTO.setNodeCode(node.getNodeCode());
                                nodeDTO.setNodeIp(node.getNodeIp());
                                nodeDTOS.add(nodeDTO);
                                logger.info("chay vao node :" + node.getNodeCode());
                                NodeRun nodeRun = new NodeRun(new NodeRunId(node.getNodeId(), flowRunAction.getFlowRunId()), flowRunAction, node);
                                //Quytv7_02102017_thay doi cach lay account/pass tu bang node
//                                nodeRun.setAccount(node.getAccount());
//                                nodeRun.setPassword(node.getPassword());
                                nodeRuns.add(nodeRun);
                                /**
                                 * Lay thong tin account tac dong
                                 */
                                NodeAccount nodeAccount;
                                if (node.getVendor().getVendorId().equals(Config.APP_TYPE.SERVER.value)) {
                                    nodeAccount = getAccImpactDefault(node, Config.APP_TYPE.SERVER.value, Config.ACCOUNT_IMPACT_MONITOR_TYPE.MONITOR.value);
                                } else {
                                    nodeAccount = getAccImpactDefault(node, Config.APP_TYPE.DATABASE.value, Config.ACCOUNT_IMPACT_MONITOR_TYPE.IMPACT.value);
                                }

                                if (nodeAccount == null) {
                                    result.setResultDetail("Cannot get node account from node: " + node.getNodeCode() + "/username: " + username);
                                    return result;
                                }
                                paramValues = generateFlowRunController.getParamInputs(node);
                                logger.info("---ket thuc lay paramValues" + paramValues.size() + "---");
                                for (ParamValue paramValue : paramValues) {
                                    if (paramValue.getParamInput().getReadOnly()) {
                                        continue;
                                    }
                                    Object value = null;
                                    try {
                                        value = mapParamValues.get((paramValue.getParamCode().toLowerCase().trim().replace(" ", "_").replace(".", "_")));
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                    }
                                    ResourceBundle bundle = ResourceBundle.getBundle("cas");
                                    if (bundle.getString("service").contains("10.61.127.190")) {
                                        if (value == null || value.toString().isEmpty()) {
                                            value = "TEST_NOT_FOUND";
                                        }
                                    }
                                    if (value != null) {
                                        paramValue.setParamValue(value.toString());
                                    }
                                    paramValue.setNodeRun(nodeRun);
                                    paramValue.setCreateTime(new Date());
                                    paramValue.setParamValueId(null);

                                    if (paramValue.getParamValue() == null || paramValue.getParamValue().isEmpty()) {
                                        auditAlarmDTO.setResult(RESPONSE_FAIL);
                                        auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.param.not.found") + ":" + paramValue.getParamCode() + ";\n");
                                    }
                                }
                                if (auditAlarmDTO.getResult() == RESPONSE_FAIL) {
                                    break;
                                }

                                logger.info(" co vao mapGroupAction size = " + mapGroupAction.size());
                                for (Map.Entry<Long, List<ActionOfFlow>> entry : mapGroupAction.entrySet()) {
                                    NodeRunGroupAction nodeRunGroupAction = new NodeRunGroupAction(
                                            new NodeRunGroupActionId(node.getNodeId(),
                                                    flowRunAction.getFlowRunId(),
                                                    entry.getValue().get(0).getStepNum()), entry.getValue().get(0), nodeRun);
                                    nodeRunGroupActions.add(nodeRunGroupAction);
                                    AccountGroupMop accGroup = new AccountGroupMop();
//							accGroup.setGroupOrderRun(groupAction.getActionOfFlows().get(0).getGroupActionOrder());
                                    accGroup.setNodeAccountId(nodeAccount.getId());
                                    accGroup.setNodeId(node.getNodeId());
                                    accGroup.setFlowRunId(flowRunAction.getFlowRunId());
                                    accGroup.setActionOfFlowId(entry.getValue().get(0).getStepNum());

                                    lstAccGroupMop.add(accGroup);

                                } // end loop for group action
                                logger.info(" thoai khoi mapGroupAction");
                            }
                            if (auditAlarmDTO.getResult() == RESPONSE_FAIL) {
                                return result;
                            }
                            logger.info(" xoa session");
                            logger.info(" insert NodeRunServiceImpl ");
                            new NodeRunServiceImpl().saveOrUpdate(nodeRuns, session, tx, false);
                            logger.info(" insert ParamValueServiceImpl ");
                            new ParamValueServiceImpl().saveOrUpdate(paramValues, session, tx, false);
                            //20190408_chuongtq start check param when create MOP
                            if(new GenerateFlowRunController().checkConfigCondition(AamConstants.CFG_CHK_PARAM_CONDITION_FOR_AAM)){
                                LinkedHashMap<String, CheckParamCondition> mapCheckParamCondition = new LinkedHashMap<>();
                                if(!(new CheckParamCondition().checkParamCondition(selectedFlowTemplate.getParamConditions(), nodes, generateFlowRunController.getMapParamValue(), mapCheckParamCondition,false))){
                                    if (tx.getStatus() != TransactionStatus.ROLLED_BACK && tx.getStatus() != TransactionStatus.COMMITTED) {
                                        tx.rollback();
                                    }
                                    result.setResult(RESPONSE_FAIL);
                                    List<CheckParamCondition> lstResult = new ArrayList<CheckParamCondition>(mapCheckParamCondition.values());
                                    String json = (new Gson()).toJson(lstResult);
                                    result.setResultDetail("Create DT fail, json result: ["+ StringEscapeUtils.unescapeJava(json) +"]");
                                    return result;
                                }
                            }
                            //20190408_chuongtq end check param when create MOP
                            session.flush();
                            session.clear();
                            new NodeRunGroupActionServiceImpl().saveOrUpdate(nodeRunGroupActions, session, tx, true);
                        }
                        // anhnt02
                        flowRunAction.setFlowRunCode(Constant.PREFIX_MOP_INFRA + flowRunAction.getFlowRunId());
                        boolean isCmdReboot = checkCmdReboot(lstCommandDetailId);
                        if (isCmdReboot) {
                            com.viettel.model.Action action = ActionUtil.createMobDb(nodes, flowRunAction);
//                                com.viettel.model.Action action = createMobDb(nodes, flowRunAction);
                            if (action != null && action.getId() != null) {
                                flowRunAction.setServiceActionId(action.getId());
                            }
                        }
                        new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction);
                        logger.info(" insert account group mop ");
                        new AccountGroupMopServiceImpl().saveOrUpdate(lstAccGroupMop);
                        try {
                            //Save File to database
                            String file2 = getTemplateMultiExport(MessageUtil.getResourceBundleMessage("key.template.export.dt"));
                            logger.info(" luu file thanh cong ");
                            File fileTemp2 = new File("tmp" + new Date().getTime() + ".xlsx");
                            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                            generateFlowRunController.exportToFile(file2, fileTemp2, outStream);
                            flowRunAction.setFileContent(outStream.toByteArray());
                            IOUtils.closeQuietly(outStream);
                            new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction);
                        } catch (Exception ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                        auditAlarmDTO.setResult(RESPONSE_SUCCESS);
                        auditAlarmDTO.setCreateTime(flowRunAction.getCreateDate());
                        auditAlarmDTO.setResultDetail("Create DT success");
                        auditAlarmDTO.setDtId(flowRunAction.getFlowRunId());
                        auditAlarmDTO.setDtName(flowRunAction.getFlowRunName());
                        auditAlarmDTO.setNodes(nodeDTOS);
                        auditAlarmDTO.setDtFileContent(org.apache.commons.codec.binary.Base64.encodeBase64String(flowRunAction.getFileContent()));
                        auditAlarmDTO.setDtFileName(ZipUtils.getSafeFileName(flowRunAction.getFlowRunId()
                                + "_" + ZipUtils.clearHornUnicode(flowRunAction.getFlowRunName()) + ".xlsx"));
                        auditAlarmDTO.setDtFileType("xlsx");
                        logger.info("sinh mop success cho audit: " + auditAlarmDTO.getAuditCode() + ", group_template: " + auditAlarmDTO.getTemplateGroupId() + ", template: " + auditAlarmDTO.getTemplateId());
                        //20180410_Quytv7_Start_DT_startx
                        //anhnt2
//                            if (isCmdReboot) {
//                                addThreadWhenCmdReboot(flowRunAction.getFlowRunId());
//                            }


                    } catch (Exception e) {
                        if (tx.getStatus() != TransactionStatus.ROLLED_BACK && tx.getStatus() != TransactionStatus.COMMITTED) {
                            tx.rollback();
                        }
                        logger.error(e.getMessage(), e);
                        logger.info("sinh mop fail cho audit: " + auditAlarmDTO.getAuditCode() + ", group_template: " + auditAlarmDTO.getTemplateGroupId() + ", template: " + auditAlarmDTO.getTemplateId());
                        auditAlarmDTO.setResult(RESPONSE_FAIL);
                        auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.create.dt.fail") + ";\n");
                        return result;
                    } finally {
                        if (session.isOpen()) {
                            session.close();
                        }
                    }
                }
            } catch (Exception e) {
                result.setResultDetail("Xảy ra lỗi khi chạy mop");
                logger.error(e.getMessage(), e);
                auditAlarmDTO.setResult(RESPONSE_FAIL);
                auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.create.dt.fail") + ";\n");
                return result;
            }
            logger.info("Tao mop thanh cong, tra ve ket qua");

//            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            result.setResultDetail("Xảy ra lỗi khi kiểm tra các tham số đầu vào đã được nhập đầy đủ hay chưa");
        }
        return result;
    }

    private void createServiceMopAudit(AuditAlarmDTO auditAlarmDTO, List<HashMap<String, String>> lstMapAlarmDomain) {
        ActionService actionService = new ActionServiceImpl();
        com.viettel.model.Action action = new com.viettel.model.Action();
        List<ActionDetailApp> actionDetailApps = new ArrayList<>();
        List<Module> modules = new ArrayList<>();
        String countryCode = "VNM";

        IimClientService iimClientService = new IimClientServiceImpl();
//        iimClientService.findModuleByCode(ticketAlarmDTO.get)


        Map<String, Object> filters = new HashMap<>();
        GenerateFlowRunController generateFlowRunController = new GenerateFlowRunController();
        logger.info("Bat dau lay template");
        FlowTemplates selectedFlowTemplate = null;
        try {
            selectedFlowTemplate = new FlowTemplatesServiceImpl().findById(auditAlarmDTO.getTemplateId());
        } catch (com.viettel.it.exception.AppException e) {
            logger.error(e.getMessage(), e);
        }
        if (selectedFlowTemplate == null) {
            auditAlarmDTO.setResult(RESPONSE_FAIL);
            auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.template.fail") + ";\n");
//            continue;
            return;
        }
        logger.info("Ket thuc lay template");
        logger.info("Bat dau phan tich lay tham so node mang start");
        filters.clear();
        filters.put("flowTemplates.flowTemplatesId", auditAlarmDTO.getTemplateId());
        filters.put("paramType", 1L);
        List<FlowTemplateMapAlarm> flowTemplateMapAlarmNodes = null;
        try {
            flowTemplateMapAlarmNodes = new FlowTemplateMapAlarmServiceImpl().findList(filters);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        boolean checkNode = true;
        List<String> nodesStr = new ArrayList<>();
//        List<Node> nodes = new ArrayList<>();
        ConcurrentHashMap<String, List<String>> mapParamNode = new ConcurrentHashMap<>();
        if (flowTemplateMapAlarmNodes.size() == 0) {
            auditAlarmDTO.setResult(RESPONSE_FAIL);
            auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.node.found.map.template.alarm") + ";\n");
//                        result.setResultCode(RESPONSE_FAIL);
//            continue;
            return;
        }
        if (flowTemplateMapAlarmNodes != null && flowTemplateMapAlarmNodes.size() > 0) {
            for (FlowTemplateMapAlarm flowTemplateMapAlarmNode : flowTemplateMapAlarmNodes) {
                // anhnt02
                for (HashMap<String, String> mapAlarmDomain : lstMapAlarmDomain) {
                    if (flowTemplateMapAlarmNode.getDomain() != null && mapAlarmDomain.containsKey(flowTemplateMapAlarmNode.getDomain().getDomainCode().toLowerCase().trim())) {
                        List<String> valuesTemp = getValueRegex(mapAlarmDomain.get(flowTemplateMapAlarmNode.getDomain().getDomainCode().toLowerCase().trim()), flowTemplateMapAlarmNode.getRegex());
                        if (valuesTemp.size() < 1) {
                            logger.info("Khong tim duoc gia tri tham so node:" + flowTemplateMapAlarmNode.getParamCode());
                            auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.param.node.fail") + flowTemplateMapAlarmNode.getParamCode() + ";\n");
                            checkNode = false;
                            break;
                        } else if (valuesTemp.size() == 1) {
                            flowTemplateMapAlarmNode.setParamValue(valuesTemp.get(0));
                        } else {
                            flowTemplateMapAlarmNode.setParamValue(org.apache.commons.lang3.StringUtils.join(valuesTemp, ";"));
                        }
                        mapParamNode.put(flowTemplateMapAlarmNode.getParamCode().toLowerCase().trim(), valuesTemp);
                        nodesStr = valuesTemp;
                        nodesStr.addAll(valuesTemp);
                    }

                }
                logger.info("Tim duoc gia tri tham so node:" + flowTemplateMapAlarmNode.getParamCode() + ", Value = " + flowTemplateMapAlarmNode.getParamValue());
                if (flowTemplateMapAlarmNode.getConfigGetNode().getType() != null && flowTemplateMapAlarmNode.getConfigGetNode().getType().equals(0L)) {
                    break;
                }
                if (!checkNode) {
                    auditAlarmDTO.setResult(RESPONSE_FAIL);
                    continue;
                }
                if (nodesStr.size() == 0) {
                    if (flowTemplateMapAlarmNodes.size() > 0 && flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getType() != null
                            && flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getType().equals(1L)
                            && !isNullOrEmpty(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getClassName())
                            && !isNullOrEmpty(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getFunctionName())
                            ) {
                        try {
                            Class cls = Class.forName(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getClassName());
                            Method method = cls.getMethod(flowTemplateMapAlarmNodes.get(0).getConfigGetNode().getFunctionName(), ConcurrentHashMap.class);
                            logger.info("Call luong get node method: " + method.getName());
                            nodesStr = (ArrayList<String>) (method.invoke(cls.newInstance(), mapParamNode));
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }
            if (nodesStr.isEmpty()) {
                logger.info("get node string null roi");
                auditAlarmDTO.setResult(RESPONSE_FAIL);
                auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.node.not.found") + ";\n");
//                continue;
                return;
            } else {
                try {
                    modules = iimClientService.findModuleByCodes(countryCode, nodesStr);
                } catch (AppException e) {
                    logger.error(e.getMessage(), e);
                }
            }
            if (modules == null || modules.size() != nodesStr.size()) {
                auditAlarmDTO.setResult(RESPONSE_FAIL);
                auditAlarmDTO.setResultDetail(auditAlarmDTO.getResultDetail() + MessageUtil.getResourceBundleMessage("ws.error.get.node.not.found") + ";\n");
//                continue;
                return;
            } else {
                countryCode = modules.get(0).getCountryCode();
            }

            List<Long> moduleIds = new ArrayList<>();
            for (Module module : modules) {
                logger.info(module.toString());
                moduleIds.add(module.getModuleId());
            }

            Set<Long> impactModuleIds = new HashSet<>(moduleIds);

            List<MdDependent> mdDependents = null;
            try {
                if (AamConstants.SERVICE_TEMPLATE.STOP.equals(auditAlarmDTO.getTemplateId())) {
                    mdDependents = iimClientService.findMdDependent(countryCode, moduleIds, AamConstants.MD_DEPENDENT.STOP);
                } else if (AamConstants.SERVICE_TEMPLATE.START.equals(auditAlarmDTO.getTemplateId())) {
                    mdDependents = iimClientService.findMdDependent(countryCode, moduleIds, AamConstants.MD_DEPENDENT.START);
                } else if (AamConstants.SERVICE_TEMPLATE.RESTART.equals(auditAlarmDTO.getTemplateId())) {
                    mdDependents = iimClientService.findMdDependent(countryCode, moduleIds, AamConstants.MD_DEPENDENT.RESTART);
                }

                if (mdDependents != null)
                    for (MdDependent mdDependent : mdDependents) {
                        impactModuleIds.add(mdDependent.getDependentId());
                    }

                modules = iimClientService.findModulesByIds(countryCode, new ArrayList<>(impactModuleIds));

                for (Module module : modules) {
                    module.setTestbedMode(0);
                    if (AamConstants.SERVICE_TEMPLATE.STOP.equals(auditAlarmDTO.getTemplateId())) {
                        module.setActionType(AamConstants.MODULE_GROUP_ACTION.STOP);
                        ActionDetailApp detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(Constant.STEP_STOP);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(Constant.STEP_START);
                        actionDetailApps.add(detailApp);
                    } else if (AamConstants.SERVICE_TEMPLATE.START.equals(auditAlarmDTO.getTemplateId())) {
                        module.setActionType(AamConstants.MODULE_GROUP_ACTION.START);
                        ActionDetailApp detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(Constant.STEP_START);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(Constant.STEP_STOP);
                        actionDetailApps.add(detailApp);
                    } else if (AamConstants.SERVICE_TEMPLATE.RESTART.equals(auditAlarmDTO.getTemplateId())) {
                        module.setActionType(AamConstants.MODULE_GROUP_ACTION.RESTART_STOP_START);

                        ActionDetailApp detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(Constant.STEP_START);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(Constant.STEP_STOP);
                        actionDetailApps.add(detailApp);

                        detailApp = new ActionDetailApp();
                        detailApp.setGroupAction(Constant.STEP_STOP);
                        detailApp.setModuleId(module.getModuleId());
                        detailApp.setModuleOrder(1L);
                        detailApp.setActionRollback(Constant.STEP_START);
                        actionDetailApps.add(detailApp);
                    }
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            }

            action.setCreatedBy("SYSTEM");
            action.setUserExecute("SYSTEM");
//            action.setFullName(fullname);
//            action.setStaffCode(staffCode);
            action.setCreatedTime(new Date());
            action.setActionType(Constant.ACTION_TYPE_CR_UCTT);
            action.setReason(auditAlarmDTO.getAuditCode());
            action.setBeginTime(org.joda.time.DateTime.now().toDate());
            action.setEndTime(org.joda.time.DateTime.now().plusDays(1).toDate());
            String folder = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

            action.setSourceDir(folder);
            action.setId(null);
            action.setCrName(auditAlarmDTO.getAuditCode());
            action.setCrNumber(auditAlarmDTO.getAuditCode());
            action.setRunStatus(null);
            action.setLinkCrTime(null);
            action.setCrState(null);

            ImpactProcessService impactProcessService = new ImpactProcessServiceImpl();
            filters.clear();
            filters.put("nationCode", countryCode);
            filters.put("status", Constant.status.active);

            try {
                List<ImpactProcess> impactProcessList = impactProcessService.findList(filters,new HashMap<>());
                Collections.shuffle(impactProcessList);

                action.setImpactProcess(impactProcessList.get(0));
                //20190416_tudn_start import rule config
//                actionService.saveOrUpdateAction(action, actionDetailApps, new ArrayList<>(), modules, HashMultimap.create(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Service[0], new Database[0], new ArrayList<>(), new ArrayList<>());
                actionService.saveOrUpdateAction(action, actionDetailApps, new ArrayList<>(), modules, HashMultimap.create(), new ArrayList<>(), new ArrayList<>(), new ArrayList<>(), new Service[0], new Database[0], new ArrayList<>(), new ArrayList<>(), new ArrayList<>());
                //20190416_tudn_end import rule config

                auditAlarmDTO.setResult(RESPONSE_SUCCESS);
                auditAlarmDTO.setCreateTime(action.getCreatedTime());
                auditAlarmDTO.setResultDetail("Create DT success");
                auditAlarmDTO.setDtId(action.getId());
                auditAlarmDTO.setDtName(auditAlarmDTO.getAuditCode());
                auditAlarmDTO.setNodes(new ArrayList<>());

                DocxUtil.export(action, auditAlarmDTO.getAuditCode());

                String prefixName = "MOP.CNTT.";
                String date_time2 = new SimpleDateFormat("ddMMyyyy").format(action.getCreatedTime());
                String cr = auditAlarmDTO.getAuditCode().split("_")[auditAlarmDTO.getAuditCode().split("_").length - 1];
                String appName = Util.convertUTF8ToNoSign(new DocxUtil().getAppGroupName(action.getId())).replaceAll("\\?", "");
                String mopAction = prefixName + appName + "_" + cr + "_" + date_time2 + "_tacdong_1" + ".docx";
                auditAlarmDTO.setDtFileContent(org.apache.commons.codec.binary.Base64.encodeBase64String(FileUtils.readFileToByteArray(new File(UploadFileUtils.getMopFolder(action) + File.separator + mopAction))));
                auditAlarmDTO.setDtFileName(mopAction);
                auditAlarmDTO.setDtFileType("docx");
                logger.info("sinh mop success cho ticket: " + auditAlarmDTO.getAuditCode() + ", group_template: " + auditAlarmDTO.getTemplateGroupId() + ", template: " + auditAlarmDTO.getTemplateId());
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

//    /**
//     * Call thread create action when cmd is reboot.
//     */
//    private void addThreadWhenCmdReboot(Long flowRunId) {
//        try {
////            for (Long commandDetailId:lstCommandDetailId) {
////                CommandDetail objCommandDetail = new CommandDetailServiceImpl().findById(commandDetailId);
////                if (objCommandDetail.getCommandTelnetParser().getCmd().toUpperCase().contains("reboot".toUpperCase())) {
//                    com.viettel.model.Action action = createActionThread(flowRunId);
//                    if (action != null && action.getId() != null) {
//                        // SUCCESS = 1, FAIL = 3, RUNNING = 2, WAIT = 4
//                        int runningStatus = executeImpactCr(action);
//                        boolean isWaited = false;
//                        // Update run id when running status is wait
//                        if (runningStatus == AamConstants.RUNNING_STATUS.WAIT) {
//                            new ActionServiceImpl().updateRunId(action.getId(), action.getRunId());
//                            isWaited = true;
//                        }
//                        // Case running status is wait then continue check
//                        boolean isWait = true;
//
//                        // If running status is wait within 15 minutes, it will exit the loop
//                        long timeCounter = 0L;
//                        do {
//                            if (runningStatus == AamConstants.RUNNING_STATUS.WAIT) {
//                                runningStatus = executeImpactCr(action);
//                                Thread.sleep(10000);
//                                timeCounter += 10000;
//                            } else {
//                                isWait = false;
//                            }
//                            if (timeCounter > 60000*15L) {
//                                isWait = false;
//                            }
//                        } while (isWait);
//
//                        // Have been waiting state, update run id is zero
//                        if (isWaited) {
//                            new ActionServiceImpl().updateRunId(action.getId(), 0l);
//                        }
//                    }
////                }
////            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//    }

    /**
     * Check command is exits reboot
     */
    private boolean checkCmdReboot(Set<Long> lstCommandDetailId) {
        try {
            for (Long commandDetailId : lstCommandDetailId) {
                CommandDetail objCommandDetail = new CommandDetailServiceImpl().findById(commandDetailId);
                if (objCommandDetail.getCommandTelnetParser().getCmd().toUpperCase().contains("reboot".toUpperCase())) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
//        for(NodeRunGroupAction nodeRunGroupAction: nodeRunGroupActions) {
//            ActionOfFlow actionOfFlow = nodeRunGroupAction.getActionOfFlow();
//            if (actionOfFlow != null) {
//                com.viettel.it.model.Action actionSub = actionOfFlow.getAction();
//                List<ActionDetail> actionDetails = actionSub.getActionDetails();
//                if (actionDetails != null) {
//                    for (ActionDetail actionDetail: actionDetails) {
//                        List<ActionCommand> actionCommands = actionDetail.getActionCommands();
//                        if (actionCommands != null && actionCommands.size() > 0) {
//                            for(ActionCommand actionCommand: actionCommands) {
//                                CommandDetail commandDetail = actionCommand.getCommandDetail();
//                                CommandTelnetParser commandTelnetParser = commandDetail.getCommandTelnetParser();
//                                if (commandTelnetParser != null) {
//                                    if (commandTelnetParser.getCmd().toUpperCase().contains("reboot".toUpperCase())) {
//                                        return true;
//                                    }
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }
        return false;
    }

    private ResultDTO checkLogin(String userService, String passService, ResultDTO resultDTO) {

        try {
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                resultDTO.setResultMessage("Incorrect user/pass");
                resultDTO.setResultCode(RESPONSE_FAIL);
                return resultDTO;
            }
        } catch (Exception ex) {
            resultDTO.setResultMessage("Incorrect user/pass");
            logger.error(ex.getMessage(), ex);
            resultDTO.setResultCode(RESPONSE_FAIL);
        }
        return resultDTO;
    }

    //tuanda38_20180914_map param alarm_start
    private ResultDTO runActionMop(Long actionId, Map<String, String> paramValues, String username, String system, Integer isResponse, Long monitorId, String ticketCode, long typeRunMop) {
        ResultDTO resultDTO = new ResultDTO();
        if (actionId == null || actionId.intValue() == 0) {
            resultDTO.setResultCode(0);
            resultDTO.setResultMessage("No mop run was filled");
            return resultDTO;
        }

        if (paramValues == null) {
            resultDTO.setResultCode(0);
            resultDTO.setResultMessage("No mop run was filled");
            return resultDTO;
        }

        try {

            Map<String, ParamInput> mapParamValue = new HashMap<>();
            for (Map.Entry<String, String> entry : paramValues.entrySet()) {
                mapParamValue.put(entry.getKey(), new ParamInput(entry.getKey(), entry.getValue()));
            }

            Date startTime = new Date();
            List<CmdObject> impactCmds;
            List<CmdObject> cmdLogRuns = new ArrayList<>();

            com.viettel.it.model.Action action = new com.viettel.it.persistence.ActionServiceImpl().findById(actionId);
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
//                String serverIp = MessageUtil.getResourceBundleConfig("process_socket_it_business_ip");
//                int serverPort = Integer.parseInt(MessageUtil.getResourceBundleConfig("process_socket_it_business_port"));

                List<ItNodeAction> nodeActionImpact = new ItActionController().getNodeAction(Config.ACTION_NODE_TYPE.IMPACT.value, action, null);
                List<ItNodeAction> nodeActionLog = new ItActionController().getNodeAction(Config.ACTION_NODE_TYPE.LOG.value, action, null);

                if (nodeActionLog != null) {
                    Collections.sort(nodeActionLog, new Comparator<ItNodeAction>() {
                        public int compare(final ItNodeAction object1, final ItNodeAction object2) {
                            return object1.getLogOrderRun().compareTo(object2.getLogOrderRun());
                        }
                    });
                }

                actionLog.setAction(action);
                actionLog.setStartTime(new Date());
                actionLog.setStatus(1l);
                actionLog.setUserRun(username);
                actionLog.setSystem(system);
                actionLog.setIsResponse(isResponse);
                actionLog.setMonitorId(monitorId);
                actionLog.setCrNumber(ticketCode);

                ParamSOC paramSOC = new ParamSOC();
                paramSOC.setSystem(system);
                paramSOC.setIsResponse(isResponse);
                paramSOC.setMonitorId(monitorId);
                paramSOC.setCrNumber(ticketCode);

                Long actionLogId = new ItActionLogServiceImpl().save(actionLog);
                actionLog.setId(actionLogId);
                if (typeRunMop == 1l || typeRunMop == 2l) {
                    logger.info("Thuc hien chay mop luon");
                    MessageItBusObject mesObj = new MessageItBusObject();
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
                                //20181023_tudn_start load pass security
//                                pass = PasswordEncoder.encrypt(PassProtector.decrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword(), Config.SALT));
                                try {
                                    pass = PasswordEncoder.encrypt(PassProtector.decrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword(), Config.SALT));
                                } catch (Exception e) {
                                    pass = nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword();
                                    logger.error(e.getMessage(), e);
                                }
                               /* String accType = null;
                                if(nodeAction.getActionAccounts().get(0).getNodeAccount() !=null && nodeAction.getActionAccounts().get(0).getNodeAccount().getAccountType()!=null  ) {
                                    if (Constant.ACCOUNT_TYPE_SERVER.equalsIgnoreCase(nodeAction.getActionAccounts().get(0).getNodeAccount().getAccountType().toString())) {
                                        accType = Constant.SECURITY_SERVER;
                                    } else if (Constant.ACCOUNT_TYPE_DATABASE.equalsIgnoreCase(nodeAction.getActionAccounts().get(0).getNodeAccount().getAccountType().toString())) {
                                        accType = Constant.SECURITY_DATABASE;
                                    }
                                }
                                ResultGetAccount resultGetAccount = SecurityService.getPassSecurity(nodeAction.getNode().getNodeIp(),"system"
                                        ,nodeAction.getActionAccounts().get(0).getNodeAccount().getUsername(),accType,null,null,nodeAction.getNode().getCountryCode().getCountryCode()
                                        ,nodeAction.getActionId().toString(),pass,mapConfigSecurity);
                                if(!resultGetAccount.getResultStatus() && SecurityService.isNullOrEmpty(resultGetAccount.getResult())){
                                    throw new Exception(resultGetAccount.getResultMessage());
                                }
                                pass = resultGetAccount.getResult();*/
                                //20181023_tudn_end load pass security
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
                                    //20181023_tudn_start load pass security
//                                    passLog = PasswordEncoder.encrypt(PassProtector.decrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword(), Config.SALT));
                                    try {
                                        passLog = PasswordEncoder.encrypt(PassProtector.decrypt(nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword(), Config.SALT));
                                    } catch (Exception e) {
                                        passLog = nodeAction.getActionAccounts().get(0).getNodeAccount().getPassword();
                                        logger.error(e.getMessage(), e);
                                    }
                                    /*String accType = null;
                                    if(nodeAction.getActionAccounts().get(0).getNodeAccount() !=null && nodeAction.getActionAccounts().get(0).getNodeAccount().getAccountType()!=null  ) {
                                        if (Constant.ACCOUNT_TYPE_SERVER.equalsIgnoreCase(nodeAction.getActionAccounts().get(0).getNodeAccount().getAccountType().toString())) {
                                            accType = Constant.SECURITY_SERVER;
                                        } else if (Constant.ACCOUNT_TYPE_DATABASE.equalsIgnoreCase(nodeAction.getActionAccounts().get(0).getNodeAccount().getAccountType().toString())) {
                                            accType = Constant.SECURITY_DATABASE;
                                        }
                                    }
                                    ResultGetAccount resultGetAccount = SecurityService.getPassSecurity(nodeAction.getNode().getNodeIp(),"system"
                                            ,nodeAction.getActionAccounts().get(0).getNodeAccount().getUsername(),accType,null,null,nodeAction.getNode().getCountryCode().getCountryCode()
                                            ,nodeAction.getActionId().toString(),passLog,mapConfigSecurity);
                                    if(!resultGetAccount.getResultStatus() && SecurityService.isNullOrEmpty(resultGetAccount.getResult())){
                                        throw new Exception(resultGetAccount.getResultMessage());
                                    }
                                    passLog = resultGetAccount.getResult();*/
                                    //20181023_tudn_end load pass security
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

                    mesObj.setParamSOC(paramSOC);
                    String encrytedMess = new String(org.apache.commons.codec.binary.Base64.encodeBase64((new Gson()).toJson(mesObj).getBytes("UTF-8")), "UTF-8");

                    //20190123_Quytv7_sua lay tien trinh theo thi truong start
                    ItActionController itActionController = new ItActionController();
                    itActionController.loadCountryCodeByAction(action);

                    startExecute(encrytedMess, itActionController.getCountryCode() == null ? "VNM" : itActionController.getCountryCode(), true);
                    //20190123_Quytv7_sua lay tien trinh theo thi truong end
//                    SocketClient client = new SocketClient(serverIp, serverPort);
//                    client.sendMsg(encrytedMess);
//
//                    String socketResult = client.receiveResult();
//                    if (socketResult != null && socketResult.contains("NOK")) {
//                        throw new Exception(socketResult);
//                    }
                } else {
                    actionLog.setStatus(5l);
                    new ItActionLogServiceImpl().saveOrUpdate(actionLog);
                    if (impactCmds != null && !impactCmds.isEmpty()) {
                        for (CmdObject cmdObject : impactCmds) {
                            ItCommandLog itCommandLog = new ItCommandLog();
                            itCommandLog.setActionLog(actionLog);
                            itCommandLog.setCommand(cmdObject.getCommand());
                            itCommandLog.setInsertTime(new Date());
                            itCommandLog.setLog("Mop Test");
                            itCommandLog.setOrderRun(cmdObject.getCmdOrder());
                            itCommandLog.setLogType(0l);
                            itCommandLog.setCommandDetail(new CommandDetailServiceImpl().get(cmdObject.getCmdDetailId()));
                            Long itCommandLogId = new ItCommandLogServiceImpl().save(itCommandLog);
                            itCommandLog.setId(itCommandLogId);
                        }
                    }
                    logger.info("Loai chay mop la test, nen khong chay mop");
                }
                    /*
                    Ghi log tac dong nguoi dung
                    */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), "system",
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ItActionController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.UPDATE,
                            action.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                resultDTO.setResultCode(1);
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
    //tuanda38_20180914_map param alarm_start

//    /**
//     * Create mob for case reboot database.
//     */
//    public com.viettel.model.Action createMobDb(@WebParam(name = "nodes")List<Node> nodes,
//                                                @WebParam(name = "flowRunAction")FlowRunAction flowRunAction) {
//        try {
//
//            String userName = "quytv7";
//            String staffCode = "168695";
//            String fullName = "";
//            try {
//                userName = SessionUtil.getCurrentUsername();
//                fullName = SessionUtil.getFullName();
//                staffCode = SessionUtil.getStaffCode();
//            } catch(Exception e) {
//                logger.error(e.getMessage(), e);
//            }
//            // START - Fill data for action
//            com.viettel.model.Action action = new com.viettel.model.Action();
//            Date date = new Date();
////			action.setCrNumber("TEST_VAS_"+ new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()));
//            action.setCrState(0l);
//            action.setRunStatus(AamConstants.RUN_STATUS.STAND_BY_STATUS);
//            action.setCrNumber(Constant.CR_DEFAULT);
//            action.setActionType(AamConstants.ACTION_TYPE.ACTION_TYPE_CR_NORMAL);
//            action.setKbGroup(AamConstants.KB_GROUP.BD_UCTT);
//            action.setKbType(AamConstants.KB_TYPE.BD_SERVER);
//            // 1: reboot, 2: shutdown
//            action.setActionRbSd(1l);
////			action.setCreatedBy("quytv7");
//            action.setCreatedBy(userName);
//            action.setCreatedTime(date);
//            action.setReason(flowRunAction.getFlowRunName());
//            action.setCrName(flowRunAction.getCrNumber());
//            action.setFullName(fullName);
//            action.setMaxConcurrent(3);
//            action.setSourceDir(new SimpleDateFormat("yyyyMMddHHmmss").format(date));
////			String staffCode = "168695";
//            action.setStaffCode(staffCode);
//            action.setVerifyStatus(Constant.FINISH_FAIL_STATUS);
//            action.setBeginTime(date);
//            Date dt = new Date();
//            Calendar c = Calendar.getInstance();
//            c.setTime(dt);
//            c.add(Calendar.DATE, 1);
//            dt = c.getTime();
//            action.setEndTime(dt);
//            Set<ActionServer> hashSetActionServers = new HashSet<>();
//            List<ActionServer> actionServers = new ArrayList<>();
//            // Get list node ip
//            // DU lieu gia xoa
////			nodes = new ArrayList<>();
////			nodes.add(new Node());
//            if (nodes != null) {
//                for (int i = 0; i < nodes.size(); i++) {
//                    ActionServer actionServer = new ActionServer();
////					actionServer.setMonitorAccount("");
////					actionServer.setIpServer("10.60.105.104");
////					actionServer.setMonitorAccount("vt_admin");
//                    actionServer.setIpServer(nodes.get(i).getNodeIp());
//
//                    // Set account has type monitor
//                    IimClientService iimClientService = new IimClientServiceImpl();
//                    List<OsAccount> osAccounts = iimClientService.findOsAccount(nodes.get(i).getNodeIp());
//                    if (osAccounts != null) {
//                        for (OsAccount osAccount : osAccounts) {
//                            if (osAccount.getUserType().equals(2)) {
//                                actionServer.setMonitorAccount(osAccount.getUsername());
//                                continue;
//                            }
//                        }
//                    }
//                    hashSetActionServers.add(actionServer);
//                    actionServers.add(actionServer);
//                }
//            }
//            action.setActionServers(hashSetActionServers);
//            Map<String, Object> prFilters = new HashMap<>();
//            prFilters.put("status", "1");
//            prFilters.put("name", "VIETTEL");
//            try {
//                List<ImpactProcess> processes = new ImpactProcessServiceImpl().findList(prFilters, new HashMap<>());
//                for (ImpactProcess process : processes) {
//                    action.setImpactProcess(process);
//                }
//            } catch (com.viettel.exception.AppException e) {
//                logger.error(e.getMessage(), e);
//            }
//            // END - Fill data for action
//
//            // START - Fill data for custom groups
//            ActionCustomGroup actionCustomGroup = new ActionCustomGroup();
//
//            actionCustomGroup.setName("Service maintenance");
//            actionCustomGroup.setAfterGroup(Constant.SUB_STEP_CLEARCACHE);
//            actionCustomGroup.setRollbackAfter(Constant.ROLLBACK_STEP_CLEARCACHE);
//            actionCustomGroup.setActionCustomActions(new HashSet<>());
//
//            List<ActionCustomGroup> customGroups = Arrays.asList(actionCustomGroup);
//
//            ActionCustomAction customAction = new ActionCustomAction();
//            // For case reboot
//            customAction.setType(4);
//            customAction.setPriority(1);
//            customAction.setWaitReason(MessageUtil.getResourceBundleMessage("mantemance.service"));
//            actionCustomGroup.getActionCustomActions().add(customAction);
//            // END - Fill data for custom groups
//
//            // Save DB
//            new ActionServiceImpl().saveOrUpdateAction(action, new ArrayList<>(),
//                    new ArrayList<>(), new ArrayList<>(), HashMultimap.create(), new ArrayList<>(),
//                    new ArrayList<>(), customGroups,
//                    null, null, actionServers, new ArrayList<>());
//
//            // Get new record
////			String userName = "quytv7";
//            List<com.viettel.model.Action> lstAction = new ActionServiceImpl().findByUser(userName);
//            if (lstAction != null && lstAction.size() > 0) {
//                return lstAction.get(0);
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            return null;
//        }
//        return null;
//    }
}
