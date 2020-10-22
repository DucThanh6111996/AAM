package com.viettel.it.util;

import com.viettel.bean.ResultGetAccount;
import com.viettel.it.model.*;
import com.viettel.it.object.CmdObject;
import com.viettel.it.object.CmdNodeObject;
import com.viettel.it.object.OpenBlockingCmdObj;
import com.viettel.it.object.SidnOpenBlockingObj;
import com.viettel.it.persistence.ActionServiceImpl;
import com.viettel.passprotector.PassProtector;
import com.viettel.util.AppConfig;
import com.viettel.util.Constant;
import org.apache.commons.collections.map.HashedMap;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by hanhnv68 on 9/12/2017.
 */
public class OpenBlockingUtils {

    private static final Logger logger = Logger.getLogger(OpenBlockingUtils.class);

    public static Map<Long, OpenBlockingCmdObj> buildActionGroupCmds(Long parentId, List<SidnOpenBlockingObj> sidnObjects,
                                                                     Map<Long, OpenBlockingCmdObj> mapActionCmds) {
        if (parentId != null && sidnObjects != null) {
            try {
                Map<String, Object> filters = new HashedMap();
                filters.put("action.actionId", parentId);
                List<Action> actions = new ActionServiceImpl().findList(filters);
                if (actions != null) {
                    for (Action action : actions) {
                        if (action.getActionDetails() != null
                                && action.getActionDetails().size() > 0
                                && action.getActionDetails().get(0).getActionCommands() != null
                                && !action.getActionDetails().get(0).getActionCommands().isEmpty()) {

                            mapActionCmds.put(action.getActionId(), new OpenBlockingCmdObj());

                            List<ActionCommand> actionCommands = action.getActionDetails().get(0).getActionCommands();
                            if (actionCommands != null && !actionCommands.isEmpty()) {
                                List<CommandDetail> commandDetails = new ArrayList<>();
                                List<CommandDetail> commandLogDetails = new ArrayList<>();

                                for (ActionCommand actionCmd : actionCommands) {
                                    if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.IMPACT.value) {
                                        commandDetails.add(actionCmd.getCommandDetail());
                                    } else if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.LOG.value) {
                                        CommandDetail logCmdDetail = actionCmd.getCommandDetail();
                                        logCmdDetail.setCmdLogOrderRun(actionCmd.getLogOrderRun());
                                        commandLogDetails.add(actionCmd.getCommandDetail());
                                    }
                                }

                                // set protocol for this action
                                mapActionCmds.get(action.getActionId()).setProtocol(actionCommands.get(0).getCommandDetail().getProtocol());

                                // Build list command impact for action
                                if (!commandDetails.isEmpty()) {
                                    for (SidnOpenBlockingObj obj : sidnObjects) {
                                        List<CmdObject> cmdImpacts = Util.buildCmdObj(commandDetails, obj.getParamVals(), false, null);
                                        if (cmdImpacts != null) {
                                            mapActionCmds.get(action.getActionId()).getCmdImpacts().addAll(cmdImpacts);
                                        }
                                    }
                                }

                                // Build list command write log for action
//                           if (!commandLogDetails.isEmpty()) {
//                               for (SidnOpenBlockingObj obj : sidnObjects) {
//                                   List<CmdObject> cmdLogs = Util.buildCmdObj(commandLogDetails, obj.getParamVals(), true, null);
//                                   if (cmdLogs != null) {
//                                       mapActionCmds.get(action.getActionId()).getCmdLogs().addAll(cmdLogs);
//                                   }
//                               }
//                           }
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return mapActionCmds;
    }

    public static Map<Long, List<CmdNodeObject>> buildActionGroupCmds(Long parentId, List<SidnOpenBlockingObj> sidnObjects) {
        Map<Long, List<CmdNodeObject>> actionNodeObjs = new HashedMap();
        if (parentId != null && sidnObjects != null) {
            try {
                Map<String, Object> filters = new HashedMap();
                filters.put("action.actionId", parentId);
                List<Action> actions = new ActionServiceImpl().findList(filters);
                if (actions != null) {
                    for (Action action : actions) {
                        if (action.getActionDetails() != null
                                && action.getActionDetails().size() > 0
                                && action.getActionDetails().get(0).getActionCommands() != null
                                && !action.getActionDetails().get(0).getActionCommands().isEmpty()) {

                            List<ActionCommand> actionCommands = action.getActionDetails().get(0).getActionCommands();
                            if (actionCommands != null && !actionCommands.isEmpty()) {

                                List<CmdNodeObject> nodeObjects = new ArrayList<>();
                                List<ItNodeAction> nodeAccounts = Util.getNodeAction(Config.ACTION_NODE_TYPE.IMPACT.value, null, action.getActionId());
                                if (nodeAccounts != null && !nodeAccounts.isEmpty()) {
                                    String username;
                                    String pass;
                                    for (ItNodeAction nodeAction : nodeAccounts) {
                                        CmdNodeObject nodeObject = new CmdNodeObject();
                                        nodeObject.setNodeName(nodeAction.getNode().getNodeName());
                                        nodeObject.setNodeId(nodeAction.getNode().getNodeId());
                                        nodeObject.setNodeCode(nodeAction.getNode().getNodeCode());

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
                                        nodeObject.setProtocol(actionCommands.get(0).getCommandDetail().getProtocol());
                                        nodeObject.setEffectIp(nodeAction.getNode().getEffectIp());
                                        nodeObject.setOsType(nodeAction.getNode().getOsType());
                                        nodeObject.setPort(nodeAction.getNode().getPort());
                                        nodeObject.setUrl(nodeAction.getNode().getJdbcUrl());
                                        nodeObject.setServerId(nodeAction.getNode().getServerId()+"");
                                        nodeObject.setVendorName(nodeAction.getNode().getVendor().getVendorName());
                                        nodeObject.setTotalTimeout(120000l);
                                        nodeObject.setType(Config.ACTION_NODE_TYPE.IMPACT.value);

                                        nodeObjects.add(nodeObject);
                                    }

                                    List<CommandDetail> commandDetails = new ArrayList<>();
                                    List<CommandDetail> commandLogDetails = new ArrayList<>();

                                    for (ActionCommand actionCmd : actionCommands) {
                                        if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.IMPACT.value) {
                                            commandDetails.add(actionCmd.getCommandDetail());
                                        } else if (actionCmd.getCommandDetail().getCommandType().longValue() == Config.COMMAND_TYPE.LOG.value) {
                                            CommandDetail logCmdDetail = actionCmd.getCommandDetail();
                                            logCmdDetail.setCmdLogOrderRun(actionCmd.getLogOrderRun());
                                            commandLogDetails.add(actionCmd.getCommandDetail());
                                        }
                                    }

                                    // Build list command impact for action
                                    if (!commandDetails.isEmpty()) {

                                        // set protocol for this action of all node
                                        for (int i = 0; i < nodeObjects.size(); i++) {
                                            nodeObjects.get(i).setProtocol(commandDetails.get(0).getProtocol());
                                        }

                                        // set commands for each node
                                        int i = 0;
                                        Map<Integer, AtomicLong> mapNodeCmdIndex = new HashedMap();
                                        for (int idx = 0; idx < nodeAccounts.size(); idx++) {
                                            mapNodeCmdIndex.put(idx, new AtomicLong(0));
                                        }
//                                        Map<Integer, List<List<CmdObject>>> mapCmdObjs;
                                        for (SidnOpenBlockingObj obj : sidnObjects) {
                                            int idxNodeObj = i % nodeObjects.size();
                                            List<CmdObject> cmdImpacts = Util.buildCmdObj(commandDetails, obj.getParamVals(), false, null);

                                            if (cmdImpacts != null && !cmdImpacts.isEmpty()) {
                                                for (CmdObject cmdObject : cmdImpacts) {
                                                    cmdObject.setCmdOrder(mapNodeCmdIndex.get(idxNodeObj).incrementAndGet());
                                                }
                                                if (cmdImpacts != null) {
                                                    nodeObjects.get(idxNodeObj).getCmdImpacts().put(obj.getId(), cmdImpacts);
                                                }
                                            }
                                            i++;
                                        }

                                        // set command order for each node
//                                        for (CmdNodeObject nodeObject : nodeObjects) {
//                                            int cmdSize = nodeObject.getCmdImpacts().size();
//                                            for (int idx = 0; idx < cmdSize; idx++) {
//                                                nodeObject.getCmdImpacts().get(idx).setCmdOrder(Long.valueOf(idx));
//                                            }
//                                        }
                                    }
                                }
                                actionNodeObjs.put(action.getActionId(), nodeObjects);
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return actionNodeObjs;
    }


}
