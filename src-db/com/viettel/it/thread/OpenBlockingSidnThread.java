package com.viettel.it.thread;

import com.google.gson.Gson;
import com.viettel.it.model.Action;
import com.viettel.it.model.ItActionLog;
import com.viettel.it.object.SidnBccsObj;
import com.viettel.it.object.CmdNodeObject;
import com.viettel.it.object.MessageItBusObject;
import com.viettel.it.object.SidnOpenBlockingObj;
import com.viettel.it.persistence.ItActionLogServiceImpl;
import com.viettel.it.util.*;
import com.viettel.util.SessionUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;
import redis.clients.jedis.Jedis;

import java.sql.Connection;
import java.util.*;

/**
 * Created by hanhnv68 on 9/18/2017.
 */
public class OpenBlockingSidnThread implements Runnable {

    private static final Logger logger = Logger.getLogger(Config.OPEN_BLOCKING_SIDN_TYPE.class);

    private String urlSidnDb;
    private String userSidnDb;
    private String passSidnDb;
    private String sqlQuery;
    private Util util;
    private String userRun;
    private String ipClient;
    private String url;
    private String requestId;
    private Integer numOfThread;

    public OpenBlockingSidnThread(String urlSidnDb, String userSidnDb, String passSidnDb,
                                  String sqlQuery, String userRun, String ipClient, String url,
                                  String requestId, Util util, Integer numOfThread) {
        this.urlSidnDb = urlSidnDb;
        this.userSidnDb = userSidnDb;
        this.passSidnDb = passSidnDb;
        this.sqlQuery = sqlQuery;
        this.util = util;
        this.userRun = userRun;
        this.ipClient = ipClient;
        this.url = url;
        this.requestId = requestId;
        this.numOfThread = numOfThread;
    }

    @Override
    public void run() {
        try {
            openBlocking();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void openBlocking() {
        logger.info("Start running open blocking sidn thread");
        Date startTime = new Date();
        Connection connection = null;
        try {
            connection = util.getConnection(urlSidnDb, userSidnDb, passSidnDb);
            if (connection != null) {
                List<SidnOpenBlockingObj> sidnDatas = util.getSidnOpenBlocking(sqlQuery, connection);
                if (sidnDatas == null || sidnDatas.isEmpty()) {
                    return;
                } else {

                    // Lay ra danh sach cac thue bao chua thuc hien mo chan
                    List<SidnOpenBlockingObj> sidnDatasChecked = checkSidnsOpenBlocking(sidnDatas);
                    if (sidnDatasChecked == null || sidnDatasChecked.isEmpty()) {
                        return;
                    }

                    Map<String, Action> actionGroups = Util.getMapActionGroup();
                    Map<String, Action> actionApGroups = new HashedMap(); // list action for ap provisioning lib
                    Map<String, Action> actionMobileGroups = new HashedMap(); // list action for mobile provisioning lib
                    for (Map.Entry<String, Action> entry : actionGroups.entrySet()) {
                        if (entry.getValue().getProvisioningType() != null) {
                            if (entry.getValue().getProvisioningType().intValue() == Config.PROVISIONING_LIB_TYPE.AP.value) {
                                actionApGroups.put(entry.getKey(), entry.getValue());
                            } else if (entry.getValue().getProvisioningType().intValue() == Config.PROVISIONING_LIB_TYPE.MOBILE.value) {
                                actionMobileGroups.put(entry.getKey(), entry.getValue());
                            }
                        }
                    }

                    List<SidnBccsObj> sidnBccsAps = new ArrayList<>();
                    List<SidnBccsObj> sidnBccsMobile = new ArrayList<>();
                    List<Long> sidnApIds = new ArrayList<>();
                    List<Long> sidnMobileIds = new ArrayList<>();
                    Map<Long, List<SidnOpenBlockingObj>> mapActionApSidnObj = new HashedMap();
                    Map<Long, List<SidnOpenBlockingObj>> mapActionMobileSidnObj = new HashedMap();
                    for (SidnOpenBlockingObj sidn : sidnDatasChecked) {
                        if (sidn.getGroupcode() != null) {

                            // Put map sidn-action ap
                            if (actionApGroups.get(sidn.getGroupcode()) != null) {

                                SidnBccsObj sidnBccs = new SidnBccsObj();
                                sidnBccs.setCreatedDate(sidn.getParamVals().get("CREATE_DATE") == null ? "" : sidn.getParamVals().get("CREATE_DATE"));
                                sidnBccs.setGroupCommand(sidn.getGroupcode() == null ? "" : sidn.getGroupcode());
                                sidnBccs.setId(sidn.getId() == null ? 0l : sidn.getId());
                                sidnBccs.setPcenter(sidn.getParamVals().get("PCENTER") == null ? 0l : Long.valueOf(sidn.getParamVals().get("PCENTER")));
                                sidnBccs.setPcontracid(sidn.getParamVals().get("PCONTRACTID") == null ? 0l : Long.valueOf(sidn.getParamVals().get("PCONTRACTID")));
                                sidnBccs.setPimsi(sidn.getParamVals().get("PIMSI") == null ? "" : sidn.getParamVals().get("PIMSI"));
                                sidnBccs.setPisdn(sidn.getParamVals().get("PISDN") == null ? "" : sidn.getParamVals().get("PISDN"));
                                sidnBccs.setPsubid(sidn.getParamVals().get("PSUBID") == null ? 0l : Long.valueOf(sidn.getParamVals().get("PSUBID")));
                                sidnBccs.setPsubid(sidn.getParamVals().get("PSUBID") == null ? 0l : Long.valueOf(sidn.getParamVals().get("PSUBID")));
                                sidnBccs.setStatus(sidn.getParamVals().get("STATUS") == null ? -1l : Long.valueOf(sidn.getParamVals().get("STATUS")));
                                sidnBccs.setPserviceType(sidn.getParamVals().get("PSERVICE_TYPE") == null ? "" : sidn.getParamVals().get("PSERVICE_TYPE"));
                                sidnBccsAps.add(sidnBccs);

                                sidnApIds.add(sidn.getId());
                                Action action = actionApGroups.get(sidn.getGroupcode());
                                if (mapActionApSidnObj.get(action.getActionId()) == null) {
                                    mapActionApSidnObj.put(action.getActionId(), new ArrayList<>());
                                }
                                mapActionApSidnObj.get(action.getActionId()).add(sidn);
                            }

                            // Put map sidn-action mobile
                            if (actionMobileGroups.get(sidn.getGroupcode()) != null) {

                                SidnBccsObj sidnBccs = new SidnBccsObj();
                                sidnBccs.setCreatedDate(sidn.getParamVals().get("CREATE_DATE") == null ? "" : sidn.getParamVals().get("CREATE_DATE"));
                                sidnBccs.setGroupCommand(sidn.getGroupcode() == null ? "" : sidn.getGroupcode());
                                sidnBccs.setId(sidn.getId() == null ? 0l : sidn.getId());
                                sidnBccs.setPcenter(sidn.getParamVals().get("PCENTER") == null ? 0l : Long.valueOf(sidn.getParamVals().get("PCENTER")));
                                sidnBccs.setPcontracid(sidn.getParamVals().get("PCONTRACTID") == null ? 0l : Long.valueOf(sidn.getParamVals().get("PCONTRACTID")));
                                sidnBccs.setPimsi(sidn.getParamVals().get("PIMSI") == null ? "" : sidn.getParamVals().get("PIMSI"));
                                sidnBccs.setPisdn(sidn.getParamVals().get("PISDN") == null ? "" : sidn.getParamVals().get("PISDN"));
                                sidnBccs.setPsubid(sidn.getParamVals().get("PSUBID") == null ? 0l : Long.valueOf(sidn.getParamVals().get("PSUBID")));
                                sidnBccs.setPsubid(sidn.getParamVals().get("PSUBID") == null ? 0l : Long.valueOf(sidn.getParamVals().get("PSUBID")));
                                sidnBccs.setStatus(sidn.getParamVals().get("STATUS") == null ? -1l : Long.valueOf(sidn.getParamVals().get("STATUS")));
                                sidnBccs.setPserviceType(sidn.getParamVals().get("PSERVICE_TYPE") == null ? "" : sidn.getParamVals().get("PSERVICE_TYPE"));
                                sidnBccsMobile.add(sidnBccs);

                                sidnMobileIds.add(sidn.getId());
                                Action action = actionMobileGroups.get(sidn.getGroupcode());
                                if (mapActionMobileSidnObj.get(action.getActionId()) == null) {
                                    mapActionMobileSidnObj.put(action.getActionId(), new ArrayList<>());
                                }
                                mapActionMobileSidnObj.get(action.getActionId()).add(sidn);
                            }
                        }
                    }

                    Map<Long, List<CmdNodeObject>> actionApNodeObjects = new HashedMap();
                    if (!mapActionApSidnObj.isEmpty()) {
                        for (Map.Entry<Long, List<SidnOpenBlockingObj>> entry : mapActionApSidnObj.entrySet()) {
                            Map<Long, List<CmdNodeObject>> singleActionNodeObjs = OpenBlockingUtils.buildActionGroupCmds(entry.getKey(), entry.getValue());
                            if (singleActionNodeObjs != null && !singleActionNodeObjs.isEmpty()) {
                                actionApNodeObjects.putAll(singleActionNodeObjs);
                            }
                        }
                    }

                    Map<Long, List<CmdNodeObject>> actionMobileNodeObjects = new HashedMap();
                    if (!mapActionMobileSidnObj.isEmpty()) {
                        for (Map.Entry<Long, List<SidnOpenBlockingObj>> entry : mapActionMobileSidnObj.entrySet()) {
                            Map<Long, List<CmdNodeObject>> singleActionNodeObjs = OpenBlockingUtils.buildActionGroupCmds(entry.getKey(), entry.getValue());
                            if (singleActionNodeObjs != null && !singleActionNodeObjs.isEmpty()) {
                                actionMobileNodeObjects.putAll(singleActionNodeObjs);
                            }
                        }
                    }


                    /**
                     * Build lenh va gui xuong tien trinh tac dong
                     */
                    if (!actionApNodeObjects.isEmpty() || !actionMobileNodeObjects.isEmpty()) {

                        ItActionLog actionLog = new ItActionLog();
                        actionLog.setStartTime(new Date());
                        actionLog.setUserRun(userRun);
                        actionLog.setStatus(1l);
                        actionLog.setDescription("RESCUE OPEN BLOCKING SIDN");
                        Long actionLogId = new ItActionLogServiceImpl().save(actionLog);
                        actionLog.setId(actionLogId);

                        // AP
                        if (!actionApNodeObjects.isEmpty()) {
                            sendMsgToProcess(2, actionApNodeObjects, actionLogId, sidnApIds, sidnBccsAps);
                        }

                        // MOBILE
                        if (!actionMobileNodeObjects.isEmpty()) {
                            sendMsgToProcess(1, actionMobileNodeObjects, actionLogId, sidnMobileIds, sidnBccsMobile);
                        }
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            try {
                if (connection != null) {
                    connection.close();
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    /**
     * Ham gui danh sach lenh thuc hien mo chan ung cuu thong tin
     *
     * @param type
     * @param actionNodeObjects
     * @param actionLogId
     */
    private void sendMsgToProcess(int type, Map<Long, List<CmdNodeObject>> actionNodeObjects,
                                  Long actionLogId, List<Long> sidnIds, List<SidnBccsObj> sidnBccsDatas) {
        try {
            logger.info("start send message to node");
            Date startTime = new Date();
            List<CmdNodeObject> nodeImpactObjs = new LinkedList<>();
            for (Map.Entry<Long, List<CmdNodeObject>> entry : actionNodeObjects.entrySet()) {
                nodeImpactObjs.addAll(entry.getValue());
            }

            MessageItBusObject mesObj = new MessageItBusObject();
            mesObj.setSidnDatasId(sidnIds);
            mesObj.setImpactNodes(nodeImpactObjs);
            mesObj.setActionName("Rescue open blocking sidn");
            mesObj.setNumOfThread(numOfThread);
            mesObj.setSidnBccsDatas(sidnBccsDatas);
//                        mesObj.setFlowRunId(selectedAction.getActionId());

            mesObj.setFlowRunLogId(actionLogId);
            mesObj.setRunningType(Config.IT_BUSINESS_RUNNING_TYPE.RESCUE_OPENNING.value);

            String encrytedMess = new String(org.apache.commons.codec.binary.Base64.encodeBase64((new Gson()).toJson(mesObj).getBytes("UTF-8")), "UTF-8");

            String serverIp = MessageUtil.getResourceBundleConfig("process_socket_it_business_ip");
            int serverPort = Integer.parseInt(MessageUtil.getResourceBundleConfig("process_socket_it_business_port"));

            if (type == 2) {
                serverIp = MessageUtil.getResourceBundleConfig("process_socket_it_business_ip_lib_ap");
                serverPort = Integer.parseInt(MessageUtil.getResourceBundleConfig("process_socket_it_business_port_lib_ap"));
            }

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
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), userRun,
                        ipClient, url, OpenBlockingSidnThread.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.IMPACT,
                        "Rescue open blocking sidn", requestId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private List<SidnOpenBlockingObj> checkSidnsOpenBlocking(List<SidnOpenBlockingObj> sidnDatas) {
        if (sidnDatas != null && !sidnDatas.isEmpty()) {
            List<SidnOpenBlockingObj> sidnDatasChecked = new ArrayList<>();
            Jedis jedis = null;
            try {
                jedis = ConnectionPoolRedis.getRedis();
                for (SidnOpenBlockingObj sidn : sidnDatas) {
                    try {
                        if (jedis.get(Config.PREFIX_OPEN_BLOCKING_SIDN + sidn.getId()) == null) {
                            sidnDatasChecked.add(sidn);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                if (jedis != null) {
                    try {
                        jedis.close();
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
            return sidnDatasChecked;
        } else {
            return null;
        }
    }

}
