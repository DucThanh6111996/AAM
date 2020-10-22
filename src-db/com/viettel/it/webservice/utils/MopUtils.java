package com.viettel.it.webservice.utils;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.viettel.it.model.*;
import com.viettel.it.object.CmdObject;
import com.viettel.it.persistence.*;
import com.viettel.it.webservice.object.ParamValueDTO;
import com.viettel.util.Config;
import com.viettel.util.Constant;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Created by hanh on 4/18/2017.
 */
public class MopUtils {

    protected static final List<String> STATION_PARAMS_CODE = new ArrayList<String>(
            Arrays.asList("CABINET_CODE", "STATION_CODE", "BTSNAME", "NODEBNAME")
    );

    protected static final org.slf4j.Logger logger = LoggerFactory.getLogger(MopUtils.class);


    public List<ParamValue> getLstParamInput(Node node, Long flowTemplateId) {
        FlowRunAction flowRunAction = new FlowRunAction();
        Map<Node, List<ParamValue>> mapParamValue = new HashMap<Node, List<ParamValue>>();
        if (node != null && flowTemplateId != null) {
            try {
                FlowTemplates selectedFlowTemplates = new FlowTemplatesServiceImpl().findById(flowTemplateId);
                Set<ParamInput> inputs = new LinkedHashSet<>();
                Map<Long, Long> mapParamInputGroupCode = new HashMap<>();
                Multimap<Long, ParamValue> _mapParamValueGroup = ArrayListMultimap.create();
//                Boolean checkRefenderParam = false;
//                HashMap<Long, Boolean> mapGroupActionDeclare = new HashMap<>();
//                List<GroupAction> lstGroupAction = mapGroupAction.get(node);
//                if (lstGroupAction != null) {
//                    for (GroupAction groupAction : lstGroupAction) {
//                        for (ActionOfFlow actionOfFlow : groupAction.getActionOfFlows()) {
//                            mapGroupActionDeclare.put(actionOfFlow.getStepNum(), groupAction.isDeclare());
//                        }
//                    }
//                }
                if (selectedFlowTemplates != null) {
                    for (ActionOfFlow actionOfFlow : selectedFlowTemplates.getActionOfFlows()) {
//                        if (mapGroupActionDeclare.containsKey(actionOfFlow.getStepNum())) {
//                            checkRefenderParam = mapGroupActionDeclare.get(actionOfFlow.getStepNum());
//                        }
                        List<ActionDetail> actionDetails = new ActionDetailServiceImpl().findList("from ActionDetail where action.actionId =? and vendor.vendorId = ? "
                                        + "and version.versionId =? and nodeType.typeId = ?", -1, -1, actionOfFlow.getAction().getActionId(), node.getVendor().getVendorId(),
                                node.getVersion().getVersionId(), node.getNodeType().getTypeId());
                        for (ActionDetail actionDetail : actionDetails) {
//						if(actionDetail.getVendor().equals(node.getVendor()) &&
//								actionDetail.getVersion().equals(node.getVersion()) &&
//								actionDetail.getNodeType().equals(node.getNodeType()))
                            {
                                List<ActionCommand> actionCommands = new ActionCommandServiceImpl().findList("from ActionCommand where actionDetail.detailId = ? and "
                                                + "commandDetail.vendor.vendorId =? and commandDetail.version.versionId =? ", -1, -1, actionDetail.getDetailId(), node.getVendor().getVendorId(),
                                        node.getVersion().getVersionId());
                                List<Long> commandDetailIds = new ArrayList<>();
                                String commandDetailIdString = "";
                                for (ActionCommand actionCommand : actionCommands) {
                                    CommandDetail commandDetail = actionCommand.getCommandDetail();
                                    commandDetailIds.add(commandDetail.getCommandDetailId());
                                    commandDetailIdString += "#" + commandDetail.getCommandDetailId();
                                }
                                Map<String, Collection<?>> map = new HashMap<>();
                                map.put("commandDetailIds", commandDetailIds);
                                List<ParamInput> paramInputs = new ParamInputServiceImpl().findListWithIn("from ParamInput where commandDetail.commandDetailId in (:commandDetailIds)", -1, -1, map);
                                Multimap<CommandDetail, ParamInput> mapParamInputs = ArrayListMultimap.create();
                                for (ParamInput paramInput2 : paramInputs) {
                                    mapParamInputs.put(paramInput2.getCommandDetail(), paramInput2);
                                }
                                if (mapParamInputs.size() > 0) {
                                    for (ActionCommand actionCommand : actionCommands) {
                                        CommandDetail commandDetail = actionCommand.getCommandDetail();
                                        Collection<ParamInput> collection = mapParamInputs.get(commandDetail);
                                        if (collection.size() > 0) {
                                            for (ParamInput paramInput : collection) {
                                                if (paramInput.getParamGroups().size() > 0) {
                                                    for (ParamGroup paramGroup : paramInput.getParamGroups()) {
                                                        if (paramGroup.getFlowTemplates().equals(selectedFlowTemplates)) {
                                                            paramInput.setParamDefault(paramGroup.getParamDefault());
                                                            if (paramGroup.getGroupCode() != null) {
                                                                mapParamInputGroupCode.put(paramInput.getParamInputId(), paramGroup.getGroupCode());
                                                            }
                                                        }
                                                    }
                                                }

                                                paramInput.setInOut(false);

                                                // Kiem tra xem tham so co phai la tham so tham chieu
                                                if (checkParamInOut(paramInput, actionOfFlow, commandDetail)) {
                                                    paramInput.setInOut(true);
                                                }
                                                inputs.add(paramInput);
                                            }
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
                List<ParamValue> paramValues = new LinkedList<>();
//                int colorIndex = 0;
//                Map<String, String> mapColor = new HashMap<>();
                for (ParamInput paramInput : inputs) {
//                    if (paramInput.getIsDeclare()) {
//                        System.out.println("True_" + node.getNodeCode() + "_" + paramInput.getActionOfFolowId() + "_" + paramInput.getParamCode());
//                    } else {
//                        System.out.println("False_" + node.getNodeCode() + "_" + paramInput.getActionOfFolowId() + "_" + paramInput.getParamCode());
//                    }
                    ParamValue paramValue = new ParamValue();
                    paramValue.setParamInput(paramInput);
                    paramValue.setParamCode(paramInput.getParamCode());
                    //paramValue.setFlowRunAction(flowRunAction);
                    paramValue.setNodeRun(new NodeRun(new NodeRunId(node.getNodeId(), flowRunAction.getFlowRunId()), flowRunAction, node));
                    paramValue.setDisableByInOut(paramInput.getInOut() == true ? true : false);

//                    if (mapColor.containsKey(paramInput.getParamCode())) {
//                        paramInput.setColor(mapColor.get(paramInput.getParamCode()));
//                    } else {
//                        String color = Config.getCOLORS()[colorIndex >= Config.getCOLORS().length ? Config.getCOLORS().length - 1 : colorIndex];
//                        mapColor.put(paramInput.getParamCode(), color);
//                        paramInput.setColor(color);
//                        colorIndex++;
//                    }

                    paramValue.setParamValue(paramInput.getParamDefault());
//                    if (paramValue.getParamCode().equals(ParamUtil.PARAMCODE.INTERFACE_UPLINK.value)) {
//                        String temp = "";
//                        if (node.getLstInterface() != null) {
//                            for (String string : node.getLstInterface()) {
//                                temp += string + ";";
//                            }
//                        }
//                        temp = temp.replaceAll(";$", "");
//                        paramValue.setParamValue(temp);
//                    }
                    Long groupCode = mapParamInputGroupCode.get(paramInput.getParamInputId());
                    paramValue.setGroupCode(groupCode);
                    if (groupCode != null) {
                        _mapParamValueGroup.put(groupCode, paramValue);
                    }
                    paramValues.add(paramValue);
                }
//                for (ParamValue paramValue2 : paramValues) {
//                    loadParam(paramValue2, paramValues);
//                }
//			createActionWithMultiParam(paramValues);

                mapParamValue.put(node, paramValues);
//                mapParamValueGroup.put(node, _mapParamValueGroup);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
        return mapParamValue.get(node);
    }

    private boolean checkParamInOut(ParamInput paramInput, ActionOfFlow actionOfFlow, CommandDetail commandDetail) {
        boolean check = false;
        if (paramInput != null) {
            try {
                if (paramInput.getParamInOuts() != null && !paramInput.getParamInOuts().isEmpty()) {
                    List<ParamInOut> paramInOuts = paramInput.getParamInOuts();
                    for (ParamInOut paramInOut : paramInOuts) {
                        if (paramInOut.getActionOfFlowByActionFlowInId().getStepNum().intValue() == actionOfFlow.getStepNum().intValue()
                                && paramInOut.getActionCommandByActionCommandInputId().getCommandDetail().getCommandDetailId().intValue() == commandDetail.getCommandDetailId().intValue()) {
                            check = true;
                        }
                    }
                }
//                Map<String, Object> filters = new HashedMap();
//                filters.put("paramInput.paramInputId", paramInput.getParamInputId());
//                filters.put("actionOfFlowByActionFlowInId.stepNum", actionOfFlow.getStepNum());
//
//                List<ParamInOut> paramInOuts = new ParamInOutServiceImpl().findListExac(filters, null);
//                if (paramInOuts != null && !paramInOuts.isEmpty()) {
//                    check = true;
//                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return check;
    }

    public ArrayList<ParamValue> distinctParamValueSameParamCode(List<ParamValue> paramValues) {
        ArrayList<ParamValue> _paramValues = new ArrayList<>();
        logger.info("distinctParamValueSameParamCode");
        try {
            Set<String> _tmp = new HashSet<>();
            Map<String, String> _tmpFormula = new HashMap<>();
            Map<String, String> _tmpDescription = new HashMap<>();

            Map<CommandDetail, Boolean> mapCommand = new HashMap<>();
            for (ParamValue paramValue : paramValues) {
                if (!mapCommand.containsKey(paramValue.getParamInput().getCommandDetail())) {
                    mapCommand.put(paramValue.getParamInput().getCommandDetail(), Boolean.TRUE);
                }
            }

            for (CommandDetail command : mapCommand.keySet()) {
                command.calculateParam(paramValues, logger);
            }

            for (ParamValue paramValue : paramValues) {
                String paramCode = paramValue.getParamInput().getParamCode();
                if (!_tmp.contains(paramCode)) {
                    paramValue.setIsDeclare(paramValue.getParamInput().getIsDeclare());
                    paramValue.setDisableByInOut(!paramValue.getParamInput().getParamInOuts().isEmpty());
                    _paramValues.add(paramValue);
                    _tmp.add(paramCode);
                } else {
                    if (paramValue.getParamInput().getInOut()) {
                        for (ParamValue paramValue2 : _paramValues) {
                            if (paramValue.getParamInput().equals(paramValue2.getParamInput())) {
                                paramValue2.getParamInput().setInOut(true);
                            }
                        }
                    }

                    if (paramValue.getParamInput().getParamInOuts().isEmpty()) {
                        for (ParamValue paramValue2 : _paramValues) {
                            if (paramValue.getParamInput().getParamCode().equals(paramValue2.getParamInput().getParamCode())) {
                                paramValue2.setDisableByInOut(false);
                            }
                        }
                    }

                    if (paramValue.getParamInput().getIsDeclare()) {
                        for (ParamValue paramValue2 : _paramValues) {
                            if (paramValue.getParamInput().getParamCode().equals(paramValue2.getParamInput().getParamCode())) {
                                paramValue2.setIsDeclare(true);
                            }
                        }
                    }
                }
                if (paramValue.getParamInput().getIsFormula()) {
                    _tmpFormula.put(paramCode, paramValue.getParamInput().getParamFormula());
                }
                if (paramValue.getParamInput().getDescription() != null
                        && !paramValue.getParamInput().getDescription().trim().isEmpty()) {
                    _tmpDescription.put(paramCode, paramValue.getParamInput().getDescription());
                }
            }

            for (ParamValue paramValue : _paramValues) {
                if (_tmpFormula.containsKey(paramValue.getParamInput().getParamCode())) {
                    paramValue.setFormula(_tmpFormula.get(paramValue.getParamInput().getParamCode()));
                }
                if (_tmpDescription.containsKey(paramValue.getParamInput().getParamCode())) {
                    paramValue.setDescription(_tmpDescription.get(paramValue.getParamInput().getParamCode()));
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return _paramValues;
    }

    public ArrayList<ParamValue> updateParamInputValues(List<ParamValue> allParamsOfNode,
                                                        List<ParamValueDTO> paramsDistinct,
                                                        NodeRun nodeRun) throws Exception {
        ArrayList<ParamValue> lstParamVals = new ArrayList<>();
        if (paramsDistinct != null && allParamsOfNode != null) {
            try {
                Map<Long, String> mapParamIdVal = new HashMap<Long, String>();
                Map<Long, String> mapGroupCodeVal = new HashMap<>();
                Map<String, String> mapParamCodeVal = new HashMap<>();
                for (ParamValueDTO p : paramsDistinct) {
                    mapParamIdVal.put(p.getParamInputId(), p.getParamValue());
                    if (p.getGroupCode() != null) {
                        mapGroupCodeVal.put(p.getGroupCode(), p.getParamValue());
                    }
                    mapParamCodeVal.put(p.getParamCode(), p.getParamValue());
                }

                ParamValue pVal;
                for (ParamValue p : allParamsOfNode) {
                    if (!p.isDisableByInOut()) {
                        pVal = new ParamValue();
                        pVal.setCreateTime(new Date());
                        pVal.setDescription(p.getDescription());
                        pVal.setFormula(p.getFormula());
                        pVal.setParamCode(p.getParamCode());
                        if (mapParamIdVal.get(p.getParamInput().getParamInputId()) != null) {
                            pVal.setParamValue(mapParamIdVal.get(p.getParamInput().getParamInputId()));
                        } else if (mapParamCodeVal.get(p.getParamCode()) != null) {
                            pVal.setParamValue(mapParamCodeVal.get(p.getParamCode()));
                        } else if (p.getGroupCode() != null && mapGroupCodeVal.get(p.getGroupCode()) != null) {
                            pVal.setParamValue(mapParamIdVal.get(mapGroupCodeVal.get(p.getGroupCode())));
                        } else {
                            throw new Exception("Can not get param value: " + p.getParamCode());
                        }
                        pVal.setParamValueId(null);
                        pVal.setParamInput(p.getParamInput());
                        pVal.setNodeRun(nodeRun);
                        lstParamVals.add(pVal);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                throw new Exception(e.getMessage());
            }
        }
        return lstParamVals;
    }

    public static NodeAccount getNodeAccount(Node node, String username) {
        if (node != null && username != null && !username.trim().isEmpty()) {
            try {
                Map<String, Object> filters = new HashedMap();
                filters.put("username", username);
                filters.put("serverId", node.getServerId());
                filters.put("active", Constant.status.active);

                List<NodeAccount> nodeAccounts = new NodeAccountServiceImpl().findListExac(filters, null);
                if (nodeAccounts != null && !nodeAccounts.isEmpty()) {
                    return nodeAccounts.get(0);
                } else {
//                    filters.put("serverId", node.getNodeId());
                    filters.put("serverId", node.getServerId());
                    nodeAccounts = new NodeAccountServiceImpl().findListExac(filters, null);
                    if (nodeAccounts != null && !nodeAccounts.isEmpty()) {
                        return nodeAccounts.get(0);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static Map<String, String> paserParamVals(String strParamVal) {
        if (strParamVal == null || strParamVal.trim().isEmpty()) {
            return null;
        } else {
            Map<String, String> mapParamValues = new HashMap<>();
            List<String> paramValues = Arrays.asList(strParamVal.trim().split(";"));
            if (paramValues != null && !paramValues.isEmpty()) {
                for (String param : paramValues) {
                    List<String> paramInfos = Arrays.asList(param.trim().split("="));
                    if (paramInfos != null && paramInfos.size() == 2) {
                        mapParamValues.put(paramInfos.get(0), paramInfos.get(1));
                    }
                }
            }
            return  mapParamValues;
        }
    }

    public static boolean checkCommands(List<CmdObject> cmdObjects) {
        if (cmdObjects != null && !cmdObjects.isEmpty()) {
            for (CmdObject cmd : cmdObjects) {
                if (cmd.getCommand().contains("@{")) {
                    return false;
                }
            }
            return true;
        } else {
            return true;
        }
    }
}
