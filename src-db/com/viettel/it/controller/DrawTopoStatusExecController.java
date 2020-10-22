package com.viettel.it.controller;

import com.google.gson.Gson;
import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.*;
import com.viettel.it.object.MessageException;
import com.viettel.it.object.MessageObject;
import com.viettel.it.persistence.*;
import com.viettel.it.util.*;
import com.viettel.it.util.Config;
import com.viettel.persistence.ActionServiceImpl;
import com.viettel.controller.AamConstants;
import com.viettel.util.SessionUtil;
import com.viettel.util.SessionWrapper;
import org.apache.commons.codec.binary.Base64;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.xssf.usermodel.*;
import org.primefaces.context.RequestContext;
import org.primefaces.event.SelectEvent;
import org.primefaces.event.TabChangeEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.LazyDataModel;
import org.primefaces.model.SortOrder;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.diagram.Connection;
import org.primefaces.model.diagram.DefaultDiagramModel;
import org.primefaces.model.diagram.Element;
import org.primefaces.model.diagram.connector.Connector;
import org.primefaces.model.diagram.connector.StateMachineConnector;
import org.primefaces.model.diagram.connector.StraightConnector;
import org.primefaces.model.diagram.endpoint.BlankEndPoint;
import org.primefaces.model.diagram.endpoint.EndPoint;
import org.primefaces.model.diagram.endpoint.EndPointAnchor;
import org.primefaces.model.diagram.overlay.ArrowOverlay;
import org.primefaces.model.diagram.overlay.LabelOverlay;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.text.MessageFormat;
import java.util.*;

@ViewScoped
@ManagedBean
public class DrawTopoStatusExecController {

    protected static final Logger logger = LoggerFactory.getLogger(BuildTemplateFlowController.class);

    public static final Integer STOP_FLOW_RUN_ACTION_FLAG = 3;

    @ManagedProperty(value = "#{flowRunLogCommandService}")
    private FlowRunLogCommandServiceImpl flowRunLogCommandService;

    @ManagedProperty(value = "#{flowRunLogService}")
    private FlowRunLogServiceImpl flowRunLogService;

    public void setFlowRunLogCommandService(
            FlowRunLogCommandServiceImpl flowRunLogCommandService) {
        this.flowRunLogCommandService = flowRunLogCommandService;
    }

    @ManagedProperty("#{flowRunActionService}")
    private FlowRunActionServiceImpl flowRunActionService;

    public static final Long ERROR_CMD = 0l;
    public static final Long SUCCESS_CMD = 1l;

    public static final int ACTION_DETAULT = 0;
    public static final int ACTION_RUNNING_EXEC = 1;
    public static final int ACTION_FAIL_EXEC = 2;
    public static final int ACTION_SUCCESS_EXEC = 3;
    public static final int ACTION_FINISH_EXEC = 4;

    public static final Long ACTION_RUN_AUTO = 0L;
    public static final Long ACTION_RUN_MANUAL = 1L;

    public static final Long ELEMENT_START = -1L;
    public static final Long ELEMENT_END = -2L;

    public static final Long FLOW_RUN_ACTION_NOT_RUN = 0L;

    private DefaultDiagramModel topoDiagram;

    private ActionOfFlow selectedActionFlow;
    private FlowRunLogCommand selectedLogCommand;

    private boolean isRunAuto = true;
    private Map<Long, Map<Long, ActionDetail>> mapActionRunInfo = new HashMap<Long, Map<Long, ActionDetail>>();
    private Map<String, List<FlowRunLogCommand>> mapLogCommand = new HashMap<String, List<FlowRunLogCommand>>();
    private Map<String, String> mapLogCommandOnline = new HashMap<String, String>();
    private Map<String, List<FlowRunLogCommand>> mapLogCommandManual = new HashMap<String, List<FlowRunLogCommand>>();
    private Map<Long, Integer> mapTotalCmdAction = new HashMap<>();
    private LazyDataModel<FlowRunLog> lstFlowRunLog;

    private FlowRunAction selectedFlowRunAction; // dau viec chay auto
    private FlowRunLog selectedFlowRunLog; // dau viec chay manual

    private Long actionOfFlowId;
    private int activeIndex;
    private int modeRun;

    private boolean isShowDiagramOnly;

    /*20180626_hoangnd_them_button_thuc_hien_rollback_start*/
    private String logAction = "";
    private String className = DrawTopoStatusExecController.class.getName();
    private int runType;
    private int runningType;
    /*20180626_hoangnd_them_button_thuc_hien_rollback_end*/

    /*20180626 anhnt2 start*/
    private boolean isReboot;
    /*20180626 anhnt2 end*/

    /*20181219_hoangnd_tien trinh tac dong tu dong_start*/
    private boolean isAutoMode = false;
    /*20181219_hoangnd_tien trinh tac dong tu dong_end*/

    //tudn_start fix hien thi lai tac dong kich ban
    private int x_max = 0;

    public int getX_max() {
        return x_max;
    }

    public void setX_max(int x_max) {
        this.x_max = x_max;
    }

    //tudn_end fix hien thi lai tac dong kich ban
    @PostConstruct
    public void onStart() {
        try {
            ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
            if (context.getRequestMap().get("javax.servlet.forward.request_uri").toString().endsWith("/action")) {
                isShowDiagramOnly = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void buildTopoDiagram(FlowTemplates flowTemplate, boolean showDiagramOnly) {
        FlowRunAction flowRunActionFake = new FlowRunAction();
        flowRunActionFake.setFlowTemplates(flowTemplate);
        buildTopoDiagram(flowRunActionFake);
    }

    public void buildTopoDiagram(FlowRunAction flowRunAction) {
        isReboot = false;
        List<ActionOfFlow> lstActionFlow;

        topoDiagram = new DefaultDiagramModel();
        topoDiagram.setMaxConnections(-1);

        try {
            if (flowRunAction.getFlowRunId() == null)
                selectedFlowRunAction = flowRunAction;
            else
                selectedFlowRunAction = new FlowRunActionServiceImpl().findById(flowRunAction.getFlowRunId());
            //selectedFlowRunLog = new FlowRunLogServiceImpl().findById(1L);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        if (selectedFlowRunAction != null) {
            try {
                lstActionFlow = selectedFlowRunAction.getFlowTemplates().getActionOfFlows();
                LinkedHashMap<String, Long> mapActionLastIndexGroup = new LinkedHashMap<>();
                //tudn_start fix hien thi lai tac dong kich ban
                int x_min = 0;
                x_max = 0;
                //tudn_end fix hien thi lai tac dong kich ban
                if (lstActionFlow != null) {

                    // lay thong tin cac node thuc hien theo dau viec
                    mapActionLastIndexGroup = buildMapActionRunData(lstActionFlow);
                    int yIndexStartExec = 2;

                    /*
                     * Them phan tu dau tien vao topo
                     */
                    ActionOfFlow start = new ActionOfFlow();
                    start.setAction(new Action());
                    start.setGroupActionName(MessageUtil.getResourceBundleMessage("label.diagram.element.start"));
                    start.setStepNum(ELEMENT_START);
                    Element elementStart = new Element(start, "40em", yIndexStartExec + "em");
                    elementStart.setStyleClass("ui-diagram-start");
                    elementStart.setDraggable(false);
                    elementStart.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
                    elementStart.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
                    elementStart.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
                    elementStart.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
                    topoDiagram.addElement(elementStart);
                    yIndexStartExec += 7;

                    Map<String, Element> mapElement = new HashMap<>();
                    Map<String, ActionOfFlow> mapAction = new HashMap<>();
                    for (ActionOfFlow action : lstActionFlow) {
                        Element element;
                        action.setActionName(action.getAction().getName());
                        // Check if conten contain "reboot" is reboot type
                        if (action.getAction() != null && action.getAction().getName() != null
                                && action.getAction().getName().toUpperCase().contains("reboot".toUpperCase())
                                && selectedFlowRunAction.getServiceActionId() != null) {
                            isReboot = true;
                        }
                        if (mapElement.containsKey(action.getGroupActionOrder() + "#" + action.getStepNumberLabel())) {
                            ActionOfFlow prevAction = mapAction.get(action.getGroupActionOrder() + "#" + action.getStepNumberLabel());
                            prevAction.getPreStepsNumber().add(action.getPreviousStep() + "");
                            prevAction.getIfValues().add(action.getIfValue() + "");
                            mapAction.put(action.getGroupActionOrder() + "#" + action.getStepNumberLabel(), prevAction);

                            continue;
                        }

                        /*
                         * Hien thi cac action khai bao
                         */
                        if (action.getIsRollback() == 0) {
                            element = new Element(action, "40em", yIndexStartExec + "em");
                            yIndexStartExec += 7;

                            /*
                             * Hien thi cac phan tu rollback sang phia trai cua cac action khai bao
                             */
                        } else {
                            Element previous = mapElement.get(action.getGroupActionOrder() + "#" + action.getPreviousStep());

                            if (previous != null) {
                                //tudn_start fix hien thi lai tac dong kich ban
//                                element = new Element(action, Integer.parseInt(previous.getX().replace("em", "")) - 20 + "em",
//                                		Integer.parseInt(previous.getY().replace("em", "")) + "em");
                                int x = Integer.parseInt(previous.getX().replace("em", "")) - 40;
                                element = new Element(action, x + "em",
                                        Integer.parseInt(previous.getY().replace("em", "")) + "em");
                                if (x < x_min) {
                                    x_min = x * (-1);
                                }
                                //tudn_end fix hien thi lai tac dong kich ban
                            } else {
                                yIndexStartExec -= 10;
                                element = new Element(action, "0em", yIndexStartExec + "em");
                                yIndexStartExec += 10;
                            }
                        }

                        element.setStyleClass((action.getIsRollback().equals(0l)) ? "ui-diagram-default" : "ui-diagram-rollback-default");
                        element.setDraggable(false);
                        element.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
                        element.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
                        element.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
                        element.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
                        element.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP_RIGHT));
                        element.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM_RIGHT));
                        element.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP_LEFT));
                        element.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM_LEFT));

                        element.setId(action.getStepNum() + "");
                        topoDiagram.addElement(element);

                        List<String> lstPreStep = new ArrayList<>();
                        lstPreStep.add(action.getPreviousStep() + "");
                        action.setPreStepsNumber(lstPreStep);
                        action.setIfValues(new ArrayList<String>());
                        action.getIfValues().add(action.getIfValue());

                        mapElement.put(action.getGroupActionOrder() + "#" + action.getStepNumberLabel(), element);
                        mapAction.put(action.getGroupActionOrder() + "#" + action.getStepNumberLabel(), action);

                    } // end loop for

                    /*
                     * Them phan tu cuoi cung vao topo
                     */
                    ActionOfFlow end = new ActionOfFlow();
                    end.setAction(new Action());
                    end.setGroupActionName(MessageUtil.getResourceBundleMessage("label.diagram.element.end"));
                    end.setStepNum(ELEMENT_END);
                    Element elementEnd = new Element(end, "40em", yIndexStartExec + "em");
                    elementEnd.setStyleClass("ui-diagram-end");
                    elementEnd.setDraggable(false);
                    elementEnd.addEndPoint(new BlankEndPoint(EndPointAnchor.TOP));
                    elementEnd.addEndPoint(new BlankEndPoint(EndPointAnchor.BOTTOM));
                    elementEnd.addEndPoint(new BlankEndPoint(EndPointAnchor.LEFT));
                    elementEnd.addEndPoint(new BlankEndPoint(EndPointAnchor.RIGHT));
                    topoDiagram.addElement(elementEnd);
                }

                //tudn_start fix hien thi lai tac dong kich ban
                // chinh lai toa do cho hinh khong bi mat neu chieu ngang qua dai
                // x_min de chinh lai toa do element
                // x_max de chinh lai width cua div bao ben ngoai
                for (Element e : topoDiagram.getElements()) {
                    int x_old = Integer.parseInt(e.getX().substring(0, e.getX().indexOf("em")));
                    if (x_min > 0) {
                        int x_new = x_old + x_min;
                        e.setX(x_new + "em");
                        if (x_max < x_new) {
                            x_max = x_new;
                        }
                    }
                }
                x_max += 40;
                //tudn_end fix hien thi lai tac dong kich ban

                // gan cac action link voi nhau
                Connector stateConnectorNotOk = new StateMachineConnector();
                stateConnectorNotOk.setPaintStyle("{strokeStyle:'#e0335e',lineWidth:1}");

                Connector stateConnectorOk = new StateMachineConnector();
                stateConnectorOk.setPaintStyle("{strokeStyle:'#4350e0',lineWidth:1}");

                Connector straightConnector = new StraightConnector();
                straightConnector.setPaintStyle("{strokeStyle:'#4350e0',lineWidth:1}");


                ActionOfFlow actionBefore;
                ActionOfFlow actionAfter;
                Map<String, Integer> mapElementCheck = new HashMap<>();

                // Danh sach lstActionFlow da duoc xap xep theo thu tu groupOrder va stepLaberOrder
                LinkedHashMap<Long, Long> mapGroupOrderIndex = buildMapGroupOrder(lstActionFlow);
                for (int i = 1; i < topoDiagram.getElements().size() - 1; i++) {
                    topoDiagram.getElements().get(i).setDraggable(false);
                    actionBefore = (ActionOfFlow) topoDiagram.getElements().get(i).getData();

                    for (int j = i + 1; j < topoDiagram.getElements().size() - 1; j++) {

                        actionAfter = (ActionOfFlow) topoDiagram.getElements().get(j).getData();
                        if (actionAfter.getGroupActionOrder().intValue() > actionBefore.getGroupActionOrder().intValue()) {

                            if (actionAfter.getIsRollback().equals(Config.EXECUTE_ACTION)
                                    && mapActionLastIndexGroup.get(actionAfter.getGroupActionOrder() + "_MIN") != null
                                    && mapActionLastIndexGroup.get(actionAfter.getGroupActionOrder() + "_MIN") == actionAfter.getStepNumberLabel()
                                    && mapElementCheck.get(actionAfter.getGroupActionOrder() + "#" + actionAfter.getPreStepsNumberLabel()) == null) {

                                ActionOfFlow actionTmp;

                                for (int j2 = j - 1; j2 >= 0; j2--) {
                                    try {
                                        actionTmp = (ActionOfFlow) topoDiagram.getElements().get(j2).getData();
                                    } catch (Exception e) {
                                        logger.error(e.getMessage(), e);
                                        actionTmp = null;
                                    }

                                    if (actionTmp == null
                                            || actionTmp.getGroupActionOrder() == null) {
                                        continue;
                                    }

                                    if (actionTmp.getGroupActionOrder().equals(actionBefore.getGroupActionOrder())
                                            && actionTmp.getIsRollback().equals(Config.EXECUTE_ACTION)
                                            && mapGroupOrderIndex.get(actionAfter.getGroupActionOrder()).equals(actionTmp.getGroupActionOrder())) {

                                        // Kiem tra xem action co phai la action cuoi cua dau viec truoc hay khong
                                        if (checkActionEndOfGroup(lstActionFlow, actionTmp.getGroupActionOrder(), actionTmp.getStepNumberLabel())) {

                                            mapElementCheck.put(actionAfter.getGroupActionOrder() + "#" + actionAfter.getPreStepsNumberLabel(), 1);

                                            if (mapActionLastIndexGroup.get(actionTmp.getGroupActionOrder() + "_MAX") != null
                                                    && mapActionLastIndexGroup.get(actionTmp.getGroupActionOrder() + "_MAX") == actionTmp.getStepNumberLabel()) {

                                                topoDiagram.connect(createConnection(topoDiagram.getElements().get(j2).getEndPoints().get(1),
                                                        topoDiagram.getElements().get(j).getEndPoints().get(0), "OK", straightConnector));
                                            } else {
                                                topoDiagram.connect(createConnection(topoDiagram.getElements().get(j2).getEndPoints().get(5),
                                                        topoDiagram.getElements().get(j).getEndPoints().get(4), "OK", stateConnectorOk));
                                            }
                                        }
                                    }

                                } // end loop for j2

                                break;
                            }

                        } else {
                            List<String> preStepsNumber = actionAfter.getPreStepsNumber();
                            for (int k = 0; k < preStepsNumber.size(); k++) {
                                String preStep = preStepsNumber.get(k);
                                if (!preStep.equals(actionBefore.getStepNumberLabel().toString())) {
                                    continue;
                                }
                                /*
                                 * Gan link cho action thuc thi
                                 */
                                if (actionAfter.getIsRollback() == 0) {
                                    String ifValue = actionAfter.getIfValues().get(k);
                                    if ("0".equals(ifValue)) {
                                        topoDiagram.connect(createConnection(topoDiagram.getElements().get(i).getEndPoints().get(5),
                                                topoDiagram.getElements().get(j).getEndPoints().get(4), "NOK", stateConnectorNotOk));
                                    } else if ("1".equals(ifValue)) {
                                        if (!checkTwoPointAdjacent(topoDiagram.getElements().get(i), topoDiagram.getElements().get(j), 10)) {
                                            topoDiagram.connect(createConnection(topoDiagram.getElements().get(i).getEndPoints().get(5),
                                                    topoDiagram.getElements().get(j).getEndPoints().get(4), "OK", stateConnectorOk));
                                        } else {
                                            topoDiagram.connect(createConnection(topoDiagram.getElements().get(i).getEndPoints().get(1),
                                                    topoDiagram.getElements().get(j).getEndPoints().get(0), "OK", straightConnector));
                                        }
                                    } else {
                                        topoDiagram.connect(createConnection(topoDiagram.getElements().get(i).getEndPoints().get(1),
                                                topoDiagram.getElements().get(j).getEndPoints().get(0), "OK", stateConnectorOk));

                                        topoDiagram.connect(createConnection(topoDiagram.getElements().get(i).getEndPoints().get(5),
                                                topoDiagram.getElements().get(j).getEndPoints().get(4), "NOK", stateConnectorNotOk));
                                    }

                                    /*
                                     * Gan link cho action rollback
                                     */
                                } else {
                                    topoDiagram.connect(createConnection(topoDiagram.getElements().get(i).getEndPoints().get(2),
                                            topoDiagram.getElements().get(j).getEndPoints().get(3), "NOK", straightConnector));

                                }
                            }
                        }
                    }
                } // ket thuc vong for cac phan tu topo

                /*
                 * gan ket noi cho phan tu dau tien va phan tu cuoi cung
                 */
                if (topoDiagram.getElements() != null
                        && topoDiagram.getElements().size() > 2) {
                    topoDiagram.connect(createConnection(topoDiagram.getElements().get(0).getEndPoints().get(1),
                            topoDiagram.getElements().get(1).getEndPoints().get(0), "OK", straightConnector));

                    // lay ra index max của group action
                    Long maxGroupIndex = lstActionFlow.get(lstActionFlow.size() - 1).getGroupActionOrder();
                    // lay ra action flow index max
                    Long maxIdxActionOfMaxGroup = mapActionLastIndexGroup.get(maxGroupIndex + "_MAX");
                    if (maxIdxActionOfMaxGroup != null) {

                        for (int i = topoDiagram.getElements().size() - 2; i >= 1; i--) {
                            try {
                                if (((ActionOfFlow) topoDiagram.getElements().get(i).getData()).getStepNumberLabel().equals(maxIdxActionOfMaxGroup)) {
                                    topoDiagram.connect(createConnection(topoDiagram.getElements().get(i).getEndPoints().get(1),
                                            topoDiagram.getElements().get(topoDiagram.getElements().size() - 1).getEndPoints().get(0), "OK", straightConnector));
                                    break;
                                }
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
                    }
                }

                logger.info("start update status");
                updateStatusAction();

//                if (selectedFlowRunAction.getStatus() > Config.RUNNING_FLAG) {
//                    updateStatusAction();
//                }

                if (selectedFlowRunAction.getStatus() == Config.RUNNING_FLAG.intValue()) {
                    RequestContext.getCurrentInstance().execute("PF('scheduleUpdateTopo').start();");
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    private boolean checkTwoPointAdjacent(Element beforeElement, Element afterElement, int valCheck) {
        boolean check = true;
        try {
            // lay toa do truc x cua action before
            int yValBefore = Integer.valueOf(beforeElement.getY().replace("em", ""));
            // lay toa do truc y cua action after
            int yValAfter = Integer.valueOf(afterElement.getY().replace("em", ""));

            if (yValAfter - yValBefore > valCheck) {
                check = false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return check;
    }

    private LinkedHashMap<Long, Long> buildMapGroupOrder(List<ActionOfFlow> lstActionOfFlow) {
        LinkedHashMap<Long, Long> mapGroupOrder = new LinkedHashMap<>();
        if (lstActionOfFlow != null) {
            Map<Long, Integer> mapActionFlow = new HashMap<>();
            List<Long> lstGroupOrder = new ArrayList<>();

            int size = lstActionOfFlow.size();
            for (int i = 0; i < size; i++) {
                if (mapActionFlow.get(lstActionOfFlow.get(i).getGroupActionOrder()) == null) {
                    lstGroupOrder.add(lstActionOfFlow.get(i).getGroupActionOrder());
                    mapActionFlow.put(lstActionOfFlow.get(i).getGroupActionOrder(), 1);
                }
            } // end loop for

            int sizeOrder = lstGroupOrder.size();
            for (int i = 0; i < sizeOrder; i++) {
                mapGroupOrder.put(lstGroupOrder.get(i), (i == 0 ? -1L : lstGroupOrder.get(i - 1)));
            }
        }
        return mapGroupOrder;
    }

    private boolean checkActionEndOfGroup(List<ActionOfFlow> lstActionOfFlow, Long groupOrder, Long preStep) {
        boolean check = true;
        if (lstActionOfFlow != null && !lstActionOfFlow.isEmpty()) {
            int count = 0;
            for (ActionOfFlow action : lstActionOfFlow) {
                if (action.getIsRollback().equals(Config.EXECUTE_ACTION)
                        && action.getGroupActionOrder().equals(groupOrder)
                        && action.getPreviousStep().equals(preStep)) {
                    count++;
                    if (count > 0) {
                        return false;
                    }
                }
            }
        }
        return check;
    }

    private LinkedHashMap<String, Long> buildMapActionRunData(List<ActionOfFlow> actions) {
        LinkedHashMap<String, Long> mapActionLastIndexGroup = new LinkedHashMap<>();
        try {

            mapActionRunInfo = new HashMap<Long, Map<Long, ActionDetail>>();

            Map<Long, ActionDetail> mapNodeActionDetail;
            List<NodeRunGroupAction> lstNodeRunAction;
            List<ActionDetail> lstActionDetail;
            for (ActionOfFlow action : actions) {

                if (action.getIsRollback() == 0) {
                    if (mapActionLastIndexGroup.get(action.getGroupActionOrder() + "_MAX") == null) {
                        mapActionLastIndexGroup.put(action.getGroupActionOrder() + "_MAX", action.getStepNumberLabel());
                    } else if (mapActionLastIndexGroup.get(action.getGroupActionOrder() + "_MAX") < action.getStepNumberLabel()) {
                        mapActionLastIndexGroup.put(action.getGroupActionOrder() + "_MAX", action.getStepNumberLabel());
                    }

                    if (mapActionLastIndexGroup.get(action.getGroupActionOrder() + "_MIN") == null) {
                        mapActionLastIndexGroup.put(action.getGroupActionOrder() + "_MIN", action.getStepNumberLabel());
                    } else if (mapActionLastIndexGroup.get(action.getGroupActionOrder() + "_MIN") > action.getStepNumberLabel()) {
                        mapActionLastIndexGroup.put(action.getGroupActionOrder() + "_MIN", action.getStepNumberLabel());
                    }
                }

                mapNodeActionDetail = new HashMap<Long, ActionDetail>();
                lstActionDetail = action.getAction().getActionDetails();
                lstNodeRunAction = action.getNodeRunGroupActions();
                if (lstNodeRunAction != null && !lstNodeRunAction.isEmpty()) {

                    for (NodeRunGroupAction nodeRunAction : lstNodeRunAction) {
                        for (ActionDetail detail : lstActionDetail) {
                            if (nodeRunAction.getNodeRun().getNode().getVendor().getVendorId().equals(detail.getVendor().getVendorId())
                                    && nodeRunAction.getNodeRun().getNode().getNodeType().getTypeId().equals(detail.getNodeType().getTypeId())
                                    && nodeRunAction.getNodeRun().getNode().getVersion().getVersionId().equals(detail.getVersion().getVersionId())) {
                                mapNodeActionDetail.put(nodeRunAction.getNodeRun().getNode().getNodeId(), detail);
                                break;
                            }
                        }
                    }
                }

                mapActionRunInfo.put(action.getStepNum(), mapNodeActionDetail);

            } // end loop for

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return mapActionLastIndexGroup;
    }

//    Map<EndPoint,Element> elemEnpoint = new HashMap<>();
//    
//    private Connection createConnection(Element from, Element to, String label) {
//    	int indexFrom = 1;
//    	int indexTo = 0;
//		while(elemEnpoint.get(from.getEndPoints().get(indexFrom))!=null && indexFrom < from.getEndPoints().size() ){
//			indexFrom ++;
//		}
//		while(elemEnpoint.get(to.getEndPoints().get(indexTo))!=null && indexTo < to.getEndPoints().size() ){
//			indexTo ++;
//		}
//		return createConnection(from.getEndPoints().get(indexFrom), to.getEndPoints().get(indexTo), label);
//    }

    private Connection createConnection(EndPoint from, EndPoint to, String label, Connector connector) {
        Connection conn = new Connection(from, to, connector);

        conn.getOverlays().add(new ArrowOverlay(10, 10, 1, 1));

        if (label != null) {
            if ("ok".equalsIgnoreCase(label))
                conn.getOverlays().add(new LabelOverlay(label, "flow-label-ok", 0.5));
            else
                conn.getOverlays().add(new LabelOverlay(label, "flow-label-nok", 0.5));
        }

        return conn;
    }

    /*
     * Cap nhat trang thai cua tung action trong topo
     */
    public void updateStatusAction() {

        if (selectedFlowRunAction == null || selectedFlowRunAction.getFlowRunId() == null) {
            return;
        }
        String nodeRunning = null;
        String nodeFinish = null;
        String nodeFail = null;

        if (topoDiagram != null) {

            Map<Long, Integer> mapTotalCmdAction = new HashMap<>();
            try {
                selectedFlowRunAction = new FlowRunActionServiceImpl().findById(selectedFlowRunAction.getFlowRunId());
                // anhnt2 - If running status of thread is wait, show message
                if (selectedFlowRunAction.getServiceActionId() != null) {
                    com.viettel.model.Action action = new ActionServiceImpl().findById(selectedFlowRunAction.getServiceActionId());
                    if (action != null && action.getRunningStatus() != null && action.getRunningStatus() == AamConstants.RUNNING_STATUS.WAIT) {
//                        MessageUtil.setErrorMessageFromRes("error.reboot.wait");
                        RequestContext.getCurrentInstance().update("formErrorActionScreen");
                        RequestContext.getCurrentInstance().execute("PF('dlgErrorActionScreen').show()");
                    }
                }
                Map<String, Object> filters = new HashMap<>();
                filters.put("flowRunId", selectedFlowRunAction.getFlowRunId());

                List<TotalCommandOfAction> lstTotalCmdAction = new TotalCmdActionServiceImpl().findList(filters);
                if (lstTotalCmdAction != null) {
                    for (TotalCommandOfAction totalCmdAction : lstTotalCmdAction) {
                        mapTotalCmdAction.put(totalCmdAction.getActionOfFlowId(), totalCmdAction.getTotalCommandRun());
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

//			if (isRunAuto) {
            ActionOfFlow actionFlow;
            int actionStatus;
            int cmdsFinishAction;
            List<ActionOfFlow> lstAction = selectedFlowRunAction.getFlowTemplates().getActionOfFlows();
            for (int i = 1; i < topoDiagram.getElements().size() - 1; i++) {
                try {
                    actionFlow = (ActionOfFlow) topoDiagram.getElements().get(i).getData();
                    actionStatus = getActionRunStatus(actionFlow);
                    switch (actionStatus) {
                        case ACTION_RUNNING_EXEC:
                            nodeRunning = topoDiagram.getElements().get(i).getId();
                            break;
                        case ACTION_SUCCESS_EXEC:
                            nodeFinish = topoDiagram.getElements().get(i).getId();
                            break;
                        case ACTION_FAIL_EXEC:
                            nodeFail = topoDiagram.getElements().get(i).getId();
                            break;
                        default:
                            for (ActionOfFlow action : lstAction) {
                                if (action.getGroupActionOrder().equals(actionFlow.getGroupActionOrder())
                                        && action.getStepNumberLabel().equals(actionFlow.getStepNumberLabel())
                                        && !action.getStepNum().equals(actionFlow.getStepNum())) {
                                    int actionStatusLocal = getActionRunStatus(action);
                                    if (actionStatus != actionStatusLocal) {
                                        actionStatus = actionStatusLocal;
                                        break;
                                    }
                                }
                            }
                            break;
                    }
                    topoDiagram.getElements().get(i).setStyleClass(getElementStyle(actionStatus, actionFlow.getIsRollback() > 0 ? "0" : "1"));
                    cmdsFinishAction = getTotalCmdExecuted(actionFlow.getStepNum(), selectedFlowRunAction.getFlowRunId());
                    String cmdProcessing = mapTotalCmdAction.get(actionFlow.getStepNum()) != null ? cmdsFinishAction + "/" + mapTotalCmdAction.get(actionFlow.getStepNum()) : "";
                    if (!cmdProcessing.trim().isEmpty()) {
                        actionFlow.setCommandExecStatus("Lenh thuc thi: " + cmdProcessing + "---" + actionFlow.getActionName());
                        topoDiagram.getElements().get(i).setData(actionFlow);
                    }
//                    try {
//                    	logger.info(actionFlow.getStepNum() + " " + mapTotalCmdAction.get(actionFlow.getStepNum()) + " " + cmdProcessing);
//					} catch (Exception e) {
//						// TODO: handle exception
//					}

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } // ket thuc vong lap

            /*
             * Cap nhat action dang thuc hien chay tac dong
             */
//			} else if (selectedFlowRunLog != null){
//				try {
//					ActionOfFlow actionFlow = new ActionOfFlowServiceImpl().findById(selectedFlowRunLog.getActionOfFlowId());
//					if (actionFlow != null) {
//						ActionOfFlow actionFlowCheck = null;
//						for (int i = 1; i < topoDiagram.getElements().size() - 1; i++) {
//							try {
//								actionFlowCheck = (ActionOfFlow) topoDiagram.getElements().get(i).getData();
//								if (actionFlow.equals(actionFlowCheck)) {
//									topoDiagram.getElements().get(i).setStyleClass(getElementStyle(getActionRunStatus(actionFlow), actionFlow.getIfValue()));
//								}
//								
//							} catch (Exception e) {
//								logger.error(e.getMessage(), e);
//							}
//						} // ket thuc vong lap
//					}
//				} catch (Exception e) {
//					logger.error(e.getMessage(), e);
//				}
//				
//			}
        }

        String idElementToScroll = null;
        if (nodeRunning != null) {
            idElementToScroll = nodeRunning;
        } else if (nodeFinish != null) {
            idElementToScroll = nodeFinish;
        } else if (nodeFail != null) {
            idElementToScroll = nodeFail;
        }

        if (idElementToScroll != null) {
            RequestContext.getCurrentInstance().addCallbackParam("idElementToScroll", idElementToScroll);
        }

        /*
         * Kiem tra xem CR da thuc hien xong hay chua
         */
        try {
            if (selectedFlowRunAction == null) {
                return;
            }
            selectedFlowRunAction = new FlowRunActionServiceImpl().findById(selectedFlowRunAction.getFlowRunId());
            if (selectedFlowRunAction.getStatus() != null) {

                String finalElementStatusClass = "ui-diagram-end";
                if (selectedFlowRunAction.getStatus().intValue() == Config.FINISH_FLAG) {
                    finalElementStatusClass = "ui-diagram-success";
//                    MessageUtil.setInfoMessageFromRes("note.diagram.success");
                    //RequestContext.getCurrentInstance().execute("PF('scheduleUpdateTopo').stop();");
                } else if (selectedFlowRunAction.getStatus().intValue() == Config.FAIL_FLAG
                        || selectedFlowRunAction.getStatus().intValue() == Config.STOP_FLAG) {
                    finalElementStatusClass = "ui-diagram-fail";
                    MessageUtil.setErrorMessageFromRes("note.diagram.fail");
                    //RequestContext.getCurrentInstance().execute("PF('scheduleUpdateTopo').stop();");
                }

                topoDiagram.getElements().get(topoDiagram.getElements().size() - 1).setStyleClass(finalElementStatusClass);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private int getTotalCmdExecuted(Long actionOfFlowId, Long flowRunLogId) {
        long total = 0;
        try {
            String hql = "select count(*) as totalCmd from FlowRunLogCommand as flowRunLogCmd"
                    + " left join FlowRunLogAction as flowRunLogAction on flowRunLogAction.runLogActionId = flowRunLogCmd.runLogActionId"
                    + " where flowRunLogAction.flowRunLogId = ? and flowRunLogAction.actionOfFlowId = ?";
            List<?> a = new FlowRunLogCommandServiceImpl().findList(hql, -1, -1, flowRunLogId, actionOfFlowId);
            if (!a.isEmpty()) {
                total = (Long) a.get(0);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return (int) total;
    }

    private String getElementStyle(Integer actionStatus, String actionType) {
        String elementStyleClass = "ui-diagram-default";
        try {
            switch (actionType) {

                // action rollback
                case "0":

                    switch (actionStatus) {
                        case ACTION_FAIL_EXEC:
                            elementStyleClass = "ui-diagram-rollback-fail";
                            break;
                        case ACTION_RUNNING_EXEC:
                            elementStyleClass = "ui-diagram-rollback-running";
                            break;
                        case ACTION_SUCCESS_EXEC:
                            elementStyleClass = "ui-diagram-rollback-success";
                            break;
                        default:
                            elementStyleClass = "ui-diagram-rollback-default";
                            break;
                    }
                    break;

                // action thuc thi
                case "1":
                    switch (actionStatus) {
                        case ACTION_FAIL_EXEC:
                            elementStyleClass = "ui-diagram-fail";
                            break;
                        case ACTION_RUNNING_EXEC:
                            elementStyleClass = "ui-diagram-running";
                            break;
                        case ACTION_SUCCESS_EXEC:
                            elementStyleClass = "ui-diagram-success";
                            break;
                        default:
                            elementStyleClass = "ui-diagram-default";
                            break;
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return elementStyleClass;
    }

    private Integer getActionRunStatus(ActionOfFlow action) {
        Integer status = ACTION_DETAULT;
        try {
            Map<String, Object> filters = new HashMap<String, Object>();
            filters.put("actionOfFlowId", action.getStepNum());
            filters.put("type", ACTION_RUN_AUTO);
            filters.put("flowRunLogId", selectedFlowRunAction.getFlowRunId());

            List<FlowRunLogAction> lstLogAction = new FlowRunLogActionServiceImpl().findList(filters);
            if (lstLogAction != null && !lstLogAction.isEmpty()) {

                // kiem tra trang thai action loi
                for (FlowRunLogAction logAction : lstLogAction) {
                    if (logAction.getResult() != null
                            && logAction.getResult().equals(ERROR_CMD)) {
                        status = ACTION_FAIL_EXEC;
                        break;
                    }
                }

                /*
                 *  Neu action dang thuc thi
                 *  Kiem tra xem action da thuc thi xong chua
                 */
                if (status != ACTION_FAIL_EXEC) {

                    if (selectedFlowRunAction.getStatus().equals(Config.FLOW_RUN_ACTION_FAIL_STATUS)
                            || selectedFlowRunAction.getStatus().equals(Config.FLOW_RUN_ACTION_FINISH_STATUS)) {
                        status = ACTION_SUCCESS_EXEC;

                    } else {
                        Map<Long, Integer> mapNodeTotalClone = new HashMap<Long, Integer>();
                        Map<Long, Integer> mapNodeCloneFinish = new HashMap<Long, Integer>();

                        for (FlowRunLogAction logAction : lstLogAction) {
                            if (mapNodeTotalClone.get(logAction.getNodeId()) == null) {
                                mapNodeTotalClone.put(logAction.getNodeId(), logAction.getCloneTotal().intValue());
                            }

                            if (logAction.getFinishTime() != null) {
                                if (mapNodeCloneFinish.get(logAction.getNodeId()) == null) {
                                    mapNodeCloneFinish.put(logAction.getNodeId(), 1);
                                } else {
                                    mapNodeCloneFinish.put(logAction.getNodeId(), mapNodeCloneFinish.get(logAction.getNodeId()) + 1);
                                }
                            }
                        }

                        boolean isFinish = true;
                        if (mapNodeTotalClone != null) {
                            for (Map.Entry<Long, Integer> entry : mapNodeTotalClone.entrySet()) {
                                if ((mapNodeCloneFinish.get(entry.getKey()) == null)
                                        || (mapNodeCloneFinish.get(entry.getKey()) < entry.getValue())) {
                                    isFinish = false;
                                    break;
                                }
                            }
                        }
                        status = (isFinish ? ACTION_SUCCESS_EXEC : ACTION_RUNNING_EXEC);
                    }
                }

            } else {
                status = ACTION_DETAULT;
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            status = ACTION_DETAULT;
        }
        return status;
    }

    public void onRightClickDiagram() {
        try {
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            actionOfFlowId = Long.parseLong(params.get("action_of_flow_id"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Ham xu ly khi goi su kien kich chuot vao action
     */
    public void onClickDiagram() {
        try {
            Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
            String actionFlowId = params.get("action_of_flow_id");

            /*
             * Neu la thuc hien chay auto ca luong
             */
            if (Long.valueOf(actionFlowId).equals(ELEMENT_START)) {

                /*
                 *  Kiem tra xem CR da chay hay chua
                 *  1.Neu chua chay thi hien thi man hinh nhap thong tin ket noi
                 *  2.Neu da chay roi thi dua ra thong bao loi template da chay
                 */
                if (selectedFlowRunAction.getFlowRunId() == null)
                    return;
                selectedFlowRunAction = new FlowRunActionServiceImpl().findById(selectedFlowRunAction.getFlowRunId());
                if (selectedFlowRunAction != null) {

                    //1. CR chua thuc hien
                    if (selectedFlowRunAction.getStatus().equals(Config.WAITTING_FLAG)
                            || selectedFlowRunAction.getStatus().equals(Config.STOP_FLAG)
                            || selectedFlowRunAction.getStatus().equals(Config.GET_PASS_FAIL_FLAG)) {

                        Integer status = new FlowTemplatesServiceImpl().findById(selectedFlowRunAction.getFlowTemplates().getFlowTemplatesId()).getStatus();
                        if (status == null || status != 9) {
                            MessageUtil.setErrorMessageFromRes("error.template.not.approved");
                            return;
                        }
                        if (new Date().after(selectedFlowRunAction.getTimeRun())) {
                            if (selectedFlowRunAction.getCrNumber() != null) {
                                Boolean canExecute = false;
                                try {
//									canExecute = GNOCService.isCanExecute(selectedFlowRunAction.getCrNumber());
                                    canExecute = true;
                                } catch (Exception e) {
                                    logger.error(e.getMessage(), e);
                                }

                                if (canExecute == null || !canExecute) {
                                    MessageUtil.setErrorMessageFromRes("error.user.cannot.execute");

                                } else {
                                    // de chay test
                                    modeRun = 1;
                                    if (selectedFlowRunAction.getRunAuto() != null && selectedFlowRunAction.getRunAuto().equals(1L)) {
                                        RequestContext.getCurrentInstance().execute("PF('dlgRunAutoConfirm').show()");
                                    } else {
                                        RequestContext.getCurrentInstance().update("formShowLog");
                                        RequestContext.getCurrentInstance().execute("PF('dlgAcountInfo').show()");
                                        activeIndex = 0;
                                    }
                                    // chay that bo ra
//									if (!selectedFlowRunAction.getCrNumber().equals(Config.CR_DEFAULT)) {
//			                            modeRun = 1;
//			                            RequestContext.getCurrentInstance().update("formShowLog");
//			                            RequestContext.getCurrentInstance().execute("PF('dlgAcountInfo').show()");
//			                            activeIndex = 0;
//			                        } else {
//			                            MessageUtil.setErrorMessageFromRes("error.cr.cannot.execute");
//			                        }
                                }
                            } else {
                                MessageUtil.setErrorMessageFromRes("label.err.cr.notfound");
                            }
                        } else {
                            MessageUtil.setErrorMessageFromRes("error.dt.cannot.execute.this.time");
                        }
                        //2. CR da thuc hien
                    } else {
                        MessageUtil.setErrorMessageFromRes("label.err.flow.not.allowrun");
                    }
                } else {
                    MessageUtil.setErrorMessageFromRes("label.err.flow.not.allowrun");
                }

            } else if (!Long.valueOf(actionFlowId).equals(ELEMENT_END)) {

                // Hien thi log tac dong cua action
                showLog(Long.valueOf(actionFlowId));

                RequestContext.getCurrentInstance().execute("PF('dlgShowLogAction').show()");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void onChangeTab(int index) {

    }

    public void showLogActionAuto(Long actionFlowId, boolean isOnline) {
        mapLogCommand.clear();
        mapLogCommandOnline.clear();

        selectedFlowRunAction.getFlowRunId();
        if (isOnline) {
            logger.info("GET LOG ONLINE ACTION_FLOW_ID: " + actionFlowId);
        }

//        Map<String, Object> filters = new HashMap<String, Object>();
//        filters.put("actionOfFlowId", selectedActionFlow.getStepNum());
//        filters.put("type", ACTION_RUN_AUTO);
//        filters.put("flowRunLogId", selectedFlowRunAction.getFlowRunId());
//
//        LinkedHashMap<String, String> orders = new LinkedHashMap<String, String>();
//        orders.put("nodeId", "ASC");
//        orders.put("cloneNumber", "ASC");
        String hql = "select new FlowRunLogAction(flowRunLogAction, actionOfFlow, node) from FlowRunLogAction as flowRunLogAction"
                + " left join ActionOfFlow as actionOfFlow on flowRunLogAction.actionOfFlowId = actionOfFlow.stepNum"
                + " left join Node as node on flowRunLogAction.nodeId = node.nodeId"
                + " where flowRunLogAction.type = 0 and flowRunLogAction.flowRunLogId = ? and actionOfFlow.stepNum = ?"
                + " order by node.nodeId, flowRunLogAction.startTime, actionOfFlow.stepNumberLabel, flowRunLogAction.cloneNumber";

        List<FlowRunLogAction> lstLogAction = new ArrayList<FlowRunLogAction>();
        try {
            lstLogAction = new FlowRunLogActionServiceImpl().findList(hql, -1, -1, selectedFlowRunAction.getFlowRunId(), actionFlowId);//.findList(filters, orders);

            if (lstLogAction == null || lstLogAction.isEmpty()) {
                List<ActionOfFlow> lstAction = selectedFlowRunAction.getFlowTemplates().getActionOfFlows();
                for (ActionOfFlow actionFlow : lstAction) {
                    if (actionFlow.getStepNum().equals(actionFlowId)) {
                        for (ActionOfFlow action : lstAction) {
                            if (action.getGroupActionOrder().equals(actionFlow.getGroupActionOrder())
                                    && action.getStepNumberLabel().equals(actionFlow.getStepNumberLabel())
                                    && !action.getStepNum().equals(actionFlowId)) {
                                lstLogAction = new FlowRunLogActionServiceImpl().findList(hql, -1, -1,
                                        selectedFlowRunAction.getFlowRunId(), action.getStepNum());//.findList(filters, orders);
                                if (lstLogAction != null && !lstLogAction.isEmpty()) {
                                    break;
                                }
                            }
                        }
                        break;
                    }
                }
            }

            if (lstLogAction != null && !lstLogAction.isEmpty()) {

                LinkedHashMap<String, String> ordersCmd = new LinkedHashMap<>();
                ordersCmd.put("orderRun", "ASC");
                ordersCmd.put("cloneNumber", "ASC");
                ordersCmd.put("insertTime", "DESC");
                Map<String, Object> filtersCmd;
                //Node node = null;

                for (FlowRunLogAction logAction : lstLogAction) {

                    try {
                        filtersCmd = new HashMap<String, Object>();
                        filtersCmd.put("runLogActionId", logAction.getRunLogActionId());

                        //node = new NodeServiceImpl().findById(logAction.getNode().getNodeId());
                        if (logAction.getNode() != null) {
                            List<FlowRunLogCommand> lstCmd;
                            if (isOnline) {
                                lstCmd = new FlowRunLogCommandServiceImpl().findList(0, 20, filtersCmd, ordersCmd);
                            } else {
                                lstCmd = new FlowRunLogCommandServiceImpl().findList(filtersCmd, ordersCmd);
                            }

                            String actionName = ((logAction.getActionOfFlow() != null &&
                                    logAction.getActionOfFlow().getAction() != null) ? logAction.getActionOfFlow().getAction().getName() : "")
                                    + "_" + logAction.getCloneNumber();

                            StringBuilder logCmdOnline = new StringBuilder();
                            int count = 0;
                            for (FlowRunLogCommand cmd : lstCmd) {
                                cmd.setActionName(actionName);
                                if (count < 20) {
                                    logCmdOnline.append(cmd.getResultDetail()).append("\r\n");
                                }
                                count++;
                            }

                            if (mapLogCommand.containsKey(logAction.getNode().getNodeCode())) {
                                mapLogCommand.get(logAction.getNode().getNodeCode()).addAll(lstCmd);
                            } else {
                                mapLogCommand.put(logAction.getNode().getNodeCode(), lstCmd);
                            }
                            mapLogCommandOnline.put(logAction.getNode().getNodeCode(), logCmdOnline.toString());
                        }

                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                } // ket thuc vong lap log action
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void showLog(Long actionFlowId) {
        try {
            selectedActionFlow = null;
            try {
                selectedActionFlow = new ActionOfFlowServiceImpl().findById(actionFlowId);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                selectedActionFlow = null;
            }

            showLogActionAuto(actionFlowId, false);

            showLogManual();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void showLogManual() {
        try {
            Map<String, Object> filters1 = new HashMap<>();
            filters1.put("actionOfFlowId", selectedActionFlow.getStepNum());
            filters1.put("flowRunId", selectedFlowRunAction.getFlowRunId());

            LinkedHashMap<String, String> orders1 = new LinkedHashMap<>();
            orders1.put("createTime", "DESC");

            lstFlowRunLog = new LazyDataModelBaseNew<>(flowRunLogService, filters1, orders1);

            List<FlowRunLog> lst = lstFlowRunLog.load(0, 1, "createTime", SortOrder.DESCENDING, filters1);
            if (lst != null && !lst.isEmpty()) {
                selectedFlowRunLog = lst.get(0);

                getLogActionManual(selectedFlowRunLog.getFlowRunLogId());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void getLogActionManual(SelectEvent event) {
        mapLogCommandManual.clear();
        getLogActionManual(((FlowRunLog) event.getObject()).getFlowRunLogId());
    }

    public void onChangeTabViewLog(TabChangeEvent event) {
//    	if (event != null) {
//	    	if ("tabOnline".equalsIgnoreCase(event.getTab().getId())) {
//	    		logger.info("START GET LOG ONLINE");
//	    		RequestContext.getCurrentInstance().execute("PF('scheduleUpdateLogOl').start()");
//	    	} else {
//	    		RequestContext.getCurrentInstance().execute("PF('scheduleUpdateLogOl').stop()");
//	    	}
//    	}
    }

    private void getLogActionManual(Long flowRunLogId) {
        try {
            mapLogCommandManual.clear();
            //Map<String, Object> filters2 = new HashMap<>();
            //filters2.put("actionOfFlowId", selectedActionFlow.getStepNum());
            //filters2.put("type", ACTION_RUN_MANUAL);
            //filters2.put("flowRunLogId", flowRunLogId);

//            LinkedHashMap<String, String> orders2 = new LinkedHashMap<>();
//            orders2.put("node.nodeId", "ASC");
//            orders2.put("actionOfFlow.stepNumberLabel", "ASC");
//            orders2.put("cloneNumber", "ASC");

            String hql = "select new FlowRunLogAction(flowRunLogAction, actionOfFlow, node) from FlowRunLogAction as flowRunLogAction"
                    + " left join ActionOfFlow as actionOfFlow on flowRunLogAction.actionOfFlowId = actionOfFlow.stepNum"
                    + " left join Node as node on flowRunLogAction.nodeId = node.nodeId"
                    + " where flowRunLogAction.type = 1 and flowRunLogAction.flowRunLogId = ?"
                    + " order by node.nodeId, flowRunLogAction.startTime, actionOfFlow.stepNumberLabel, flowRunLogAction.cloneNumber";

            List<FlowRunLogAction> lstLogAction = new FlowRunLogActionServiceImpl().findList(hql, -1, -1, flowRunLogId);//.findList(filters2, orders2);

            if (lstLogAction != null && !lstLogAction.isEmpty()) {

                LinkedHashMap<String, String> ordersCmd = new LinkedHashMap<>();
                ordersCmd.put("orderRun", "ASC");
                ordersCmd.put("cloneNumber", "ASC");

                for (FlowRunLogAction logAction : lstLogAction) {
                    try {
                        Map<String, Object> filtersCmd = new HashMap<>();
                        filtersCmd.put("runLogActionId", logAction.getRunLogActionId());

                        //Node node = new NodeServiceImpl().findById(logAction.getNodeId());
                        if (logAction.getNode() != null) {
                            List<FlowRunLogCommand> lstCmd = new FlowRunLogCommandServiceImpl().findList(filtersCmd, ordersCmd);

                            String actionName = ((logAction.getActionOfFlow() != null &&
                                    logAction.getActionOfFlow().getAction() != null) ? logAction.getActionOfFlow().getAction().getName() : "")
                                    + "_" + logAction.getCloneNumber();
                            for (FlowRunLogCommand cmd : lstCmd) {
                                cmd.setActionName(actionName);
                            }

                            if (mapLogCommandManual.containsKey(logAction.getNode().getNodeCode())) {
                                mapLogCommandManual.get(logAction.getNode().getNodeCode()).addAll(lstCmd);
                            } else {
                                mapLogCommandManual.put(logAction.getNode().getNodeCode(), lstCmd);
                            }
                        }

                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                } // ket thuc vong lap log action
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void prepareShowCmdLog(FlowRunLogCommand logCmd) {
        if (logCmd != null) {
            selectedLogCommand = logCmd;
        } else {
            selectedLogCommand = new FlowRunLogCommand();
        }
    }

    public void stopFlowRunAction() {
        try {
            MessageObject mesObj = new MessageObject(selectedFlowRunAction.getFlowRunId(),
                    SessionWrapper.getCurrentUsername(), null, selectedFlowRunAction.getFlowRunName(), "");
            mesObj.setRunType(STOP_FLOW_RUN_ACTION_FLAG);

            String serverIp = MessageUtil.getResourceBundleConfig("process_socket_ip");
            int serverPort = Integer.parseInt(MessageUtil.getResourceBundleConfig("process_socket_port"));

            String encrytedMess = new String(Base64.encodeBase64((new Gson()).toJson(mesObj).getBytes("UTF-8")), "UTF-8");

            //20180813_tudn_start tao webservice cho thi truong
            String countryCode = selectedFlowRunAction.getCountryCode() == null ? AamConstants.VNM : selectedFlowRunAction.getCountryCode().getCountryCode();
            startExecute(encrytedMess, countryCode);
//            SocketClient client = new SocketClient(serverIp, serverPort);
//            client.sendMsg(encrytedMess);
//
//            String socketResult = client.receiveResult();
//            if (socketResult != null && socketResult.contains("NOK")) {
//            	throw new Exception(socketResult);
//            }
            //20180813_tudn_end tao webservice cho thi truong
            MessageUtil.setInfoMessageFromRes("message.stop.dt.status");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            if (ex.getMessage() != null) {
                switch (ex.getMessage()) {
                    case "NOK_MAX_SESSION":
                        MessageUtil.setErrorMessageFromRes("message.error.max.session");
                        break;
                }
            }

            MessageUtil.setErrorMessageFromRes("message.execute.fail");
        }
    }

    /*20180626_hoangnd_them_button_thuc_hien_rollback_start*/
    public void onClickRollback() {
        try {
            actionOfFlowId = null;
            logAction = LogUtils.addContent("", "Click rolback DT");
            if (selectedFlowRunAction == null || selectedFlowRunAction.getFlowRunId() == null) {
                logAction = LogUtils.addContent(logAction, "FlowRunId: null");
                LogUtils.writelog(new Date(), className, new Object() {
                }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.VIEW.name(), logAction);
                return;
            }
            logAction = LogUtils.addContent(logAction, "FlowRunId: " + selectedFlowRunAction.getFlowRunId());
            logAction = LogUtils.addContent(logAction, "FlowRunName: " + selectedFlowRunAction.getFlowRunName());
            selectedFlowRunAction = new FlowRunActionServiceImpl().findById(selectedFlowRunAction.getFlowRunId());
            if (selectedFlowRunAction != null) {
                if (selectedFlowRunAction.getStatus() == 3
                        || selectedFlowRunAction.getStatus() == 4
                        || selectedFlowRunAction.getStatus().equals(Config.STOP_FLAG)
                        || selectedFlowRunAction.getStatus().equals(Config.LOGIN_FAIL_FLAG)
                        || selectedFlowRunAction.getStatus().equals(Config.PAUSE_FLAG)) {
                    Integer status = new FlowTemplatesServiceImpl().findById(selectedFlowRunAction.getFlowTemplates().getFlowTemplatesId()).getStatus();
                    if (status == null || status != 9) {
                        MessageUtil.setErrorMessageFromRes("error.template.not.approved");
                        logAction = LogUtils.addContent(logAction, "Result: " + MessageUtil.getResourceBundleMessage("error.template.not.approved"));
                        LogUtils.writelog(new Date(), className, new Object() {
                        }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.VIEW.name(), logAction);

                        return;
                    }

                    if (new Date().after(selectedFlowRunAction.getTimeRun())) {
                        if (selectedFlowRunAction.getCrNumber() != null) {
                            Boolean canExecute = GNOCService.isCanExecute(selectedFlowRunAction.getCrNumber());
                            if (canExecute == null) {
                                MessageUtil.setErrorMessageFromRes("error.user.cannot.execute");
                                logAction = LogUtils.addContent(logAction, "Result: " + MessageUtil.getResourceBundleMessage("error.user.cannot.execute"));
                            } else {
                                if (canExecute || selectedFlowRunAction.getCrNumber().equals(Config.CR_DEFAULT)
                                        || selectedFlowRunAction.getCrNumber().equals(Config.CR_AUTO_DECLARE_CUSTOMER)) {
                                    modeRun = 2;
                                    runType = 2;
                                    runningType = Config.RUNNING_TYPE_DEPENDENT.intValue();
                                    RequestContext.getCurrentInstance().update("formShowLog");
                                    RequestContext.getCurrentInstance().execute("PF('dlgAcountInfo').show()");
                                    activeIndex = 0;
                                    logAction = LogUtils.addContent(logAction, "Result: Show dialog rollback dt");
                                } else {
                                    logAction = LogUtils.addContent(logAction, "Result: " + MessageUtil.getResourceBundleMessage("error.cr.cannot.execute"));
                                    MessageUtil.setErrorMessageFromRes("error.cr.cannot.execute");
                                }
                            }
                        } else {
                            logAction = LogUtils.addContent(logAction, "Result: " + MessageUtil.getResourceBundleMessage("label.err.cr.notfound"));
                            MessageUtil.setErrorMessageFromRes("label.err.cr.notfound");
                        }
                    } else {


                        logAction = LogUtils.addContent(logAction, "Result: " + MessageUtil.getResourceBundleMessage("error.dt.cannot.execute.this.time"));
                        MessageUtil.setErrorMessageFromRes("error.dt.cannot.execute.this.time");
                    }
                } else {
                    logAction = LogUtils.addContent(logAction, "Result: " + MessageUtil.getResourceBundleMessage("label.err.flow.not.allowrun"));
                    MessageUtil.setErrorMessageFromRes("label.err.flow.not.allowrun");
                }
            } else {
                logAction = LogUtils.addContent(logAction, "Result: " + MessageUtil.getResourceBundleMessage("label.err.flow.not.allowrun"));
                MessageUtil.setErrorMessageFromRes("label.err.flow.not.allowrun");
            }
            LogUtils.writelog(new Date(), className, new Object() {
            }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.VIEW.name(), logAction);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    /*20180626_hoangnd_them_button_thuc_hien_rollback_end*/

    /**
     * Ham luu thong tin account/password cua node mang nguoi dung nhap vao
     */
    public void onSaveAccountNode() {
        FlowRunLog flowRunLog = null;
        try {
            Date startTime = new Date();
            String serverIp = MessageUtil.getResourceBundleConfig("process_socket_ip");
            int serverPort = Integer.parseInt(MessageUtil.getResourceBundleConfig("process_socket_port"));

            MessageObject mesObj;
            if (actionOfFlowId != null && actionOfFlowId > 0) {
                FlowRunAction flowRunAction = (new FlowRunActionServiceImpl()).findById(selectedFlowRunAction.getFlowRunId());
                if (flowRunAction.getStatus().equals(Config.RUNNING_FLAG)) {
                    MessageUtil.setErrorMessageFromRes("message.error.running.auto");
                }

                Map<String, Object> filters = new HashMap<>();
                filters.put("actionOfFlowId", actionOfFlowId);

                List<FlowRunLog> flowRunLogs = flowRunLogService.findList(filters);

                if (flowRunLogs != null && !flowRunLogs.isEmpty()) {
                    for (FlowRunLog flowRunLog1 : flowRunLogs) {
                        if (flowRunLog1.getFinishTime() == null) {
                            MessageUtil.setErrorMessageFromRes("message.error.running.manual");
                            return;
                        }
                    }
                }

                ActionOfFlow actionOfFlow = (new ActionOfFlowServiceImpl()).findById(actionOfFlowId);

                flowRunLog = new FlowRunLog();
                flowRunLog.setActionOfFlowId(actionOfFlowId);
                if (modeRun == 1) {
                    //truong hop chay 1 action
                    flowRunLog.setActionOfFlowIdEnd(actionOfFlowId);
                }
                flowRunLog.setCreateTime(new Date());
                flowRunLog.setFlowRunId(selectedFlowRunAction.getFlowRunId());
                flowRunLog.setUsername(SessionWrapper.getCurrentUsername());

                flowRunLogService.save(flowRunLog);

                mesObj = new MessageObject(selectedFlowRunAction.getFlowRunId(),
                        SessionWrapper.getCurrentUsername(), flowRunLog.getFlowRunLogId(),
                        selectedFlowRunAction.getFlowRunName(), actionOfFlow.getAction().getName());
                mesObj.setActionOfFlowIdStart(actionOfFlowId);
                if (modeRun == 1) {
                    //truong hop chay 1 action
                    mesObj.setActionOfFlowIdEnd(actionOfFlowId);
                }
                if (actionOfFlow.getIsRollback() > 0) {
                    //Truong hop rollback
                    mesObj.setRunType(2);
                } else {
                    mesObj.setRunType(1);
                }
            } else {
                /*20180628_hoangnd_them_button_thuc_hien_rollback_start*/
//                selectedFlowRunAction.setErrorMode(modeRun);
//                selectedFlowRunAction.setRunningType(runningType);
//                selectedFlowRunAction.setRunType(runType);
//                flowRunActionService.saveOrUpdate(selectedFlowRunAction);
                mesObj = new MessageObject(selectedFlowRunAction.getFlowRunId(),
                        SessionWrapper.getCurrentUsername(), null, selectedFlowRunAction.getFlowRunName(), "");
                mesObj.setRunType(runType);
                mesObj.setErrorMode(modeRun);
                mesObj.setRunningType(runningType);
                /*20180628_hoangnd_them_button_thuc_hien_rollback_end*/
            }

            String encrytedMess = new String(Base64.encodeBase64((new Gson()).toJson(mesObj).getBytes("UTF-8")), "UTF-8");

            String socketResult = "NOK";
            SocketClient client = null;

            //20180813_tudn_start tao webservice cho thi truong
//            try {
//                client = new SocketClient(serverIp, serverPort);
//                client.sendMsg(encrytedMess);
//                socketResult = client.receiveResult();
//            } catch (Exception e) {
//                logger.error(e.getMessage(), e);
//            }
            String countryCode = selectedFlowRunAction.getCountryCode() == null ? AamConstants.VNM : selectedFlowRunAction.getCountryCode().getCountryCode();
            startExecute(encrytedMess, countryCode);
            /*
			Ghi log tac dong nguoi dung
            */
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), DrawTopoStatusExecController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.IMPACT,
                        selectedFlowRunAction.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            if (!isAutoMode) {
                if (actionOfFlowId == null || actionOfFlowId <= 0) {
                    RequestContext.getCurrentInstance().execute("PF('scheduleUpdateTopo').start();");
                    MessageUtil.setInfoMessageFromRes("message.execute.success");
                    activeIndex = 0;
                    RequestContext.getCurrentInstance().execute("PF('dlgAcountInfo').hide()");
                } else {
                    MessageUtil.setInfoMessageFromRes("message.execute.success.manual");
                    RequestContext.getCurrentInstance().execute("PF('dlgAcountInfo').hide()");

                    showLog(actionOfFlowId);

                    RequestContext.getCurrentInstance().execute("PF('dlgShowLogAction').show()");
                    activeIndex = 1;
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            if (flowRunLog != null) {
                try {
                    Date startTime = new Date();
                    flowRunLogService.delete(flowRunLog);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDbServerController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.DELETE,
                                flowRunLog.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

            if (ex.getMessage() != null) {
                switch (ex.getMessage()) {
                    case "NOK_MAX_SESSION":
                        MessageUtil.setErrorMessageFromRes("message.error.max.session");
                        break;
                    case "NOK":
                        MessageUtil.setErrorMessageFromRes("message.execute.fail");
                        break;
                }
            }

            if (actionOfFlowId == null || actionOfFlowId <= 0) {
                MessageUtil.setErrorMessageFromRes("message.execute.fail");
            } else {
                MessageUtil.setErrorMessageFromRes("message.execute.fail.manual");
            }
        }
    }

    public void confirmRunDtAuto() {
        RequestContext.getCurrentInstance().update("formShowLog");
        RequestContext.getCurrentInstance().execute("PF('dlgAcountInfo').show()");
    }

    public static void startExecute(String encrytedMess, String countryCode) throws Exception {
        String usingDbConfig = MessageUtil.getResourceBundleConfig("process_using_db_config");
        if (usingDbConfig != null && "true".equalsIgnoreCase(usingDbConfig.trim())) {
            sendMsg2ThreadExecute(encrytedMess, countryCode);
        } else {
            sendMsg2ThreadExecute(encrytedMess);
        }
    }

    private static void sendMsg2ThreadExecute(String encrytedMess, String countryCode) throws IOException, Exception, MessageException {
        Map<String, Object> filters = new HashMap<>();
        filters.put("countryCode.countryCode-" + MapProcessCountryServiceImpl.EXAC, countryCode);
        filters.put("status", 1l);
        filters.put("typeModule", 1L);

        List<MapProcessCountry> maps = new MapProcessCountryServiceImpl().findList(filters);

        if (maps != null && !maps.isEmpty()) {
            //Sap xep lai maps theo thu tu random
            Collections.shuffle(maps);

            int i = 0;
            for (MapProcessCountry process : maps) {
                try {
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
                        logger.info("serverIp: " + serverIp + ", serverPort: " + serverPort);
                        return;
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    if (i == maps.size() - 1) {
                        throw new MessageException(MessageUtil.getResourceBundleMessage("error.not.impact.connect.refused"));
                    }
                }
                i++;
            }
        } else {
            throw new MessageException(MessageUtil.getResourceBundleMessage("error.not.impact.no.process"));
        }
    }

    private static void sendMsg2ThreadExecute(String encrytedMess) throws IOException, Exception, MessageException {
        String[] serverIps = MessageUtil.getResourceBundleConfig("process_socket_ip").split("[,;]", -1);
        String[] ports = MessageUtil.getResourceBundleConfig("process_socket_port").split("[,;]", -1);
        for (int i = 0; i < serverIps.length; i++) {
            int serverPort = Integer.parseInt(ports[i]);
            String serverIp = serverIps[i];
            SocketClient client = new SocketClient(serverIp, serverPort);
            client.sendMsg(encrytedMess);

            String socketResult = client.receiveResult();
            if (socketResult != null && socketResult.contains("NOK")) {
                if (i == serverIps.length - 1) {
                    throw new MessageException(socketResult);
                }
            } else {
                return;
            }
        }
    }

    public void prepareStartManual() {
        modeRun = 2;
        if (actionOfFlowId < 0) {
            MessageUtil.setErrorMessageFromRes("message.cannot.run.start");
            return;
        }
        Boolean canExecute = GNOCService.isCanExecute(selectedFlowRunAction.getCrNumber());
        if (canExecute == null) {
            MessageUtil.setErrorMessageFromRes("error.user.cannot.execute");
            return;
        } else {
            if (!canExecute && !"CR_DEFAULT".equals(selectedFlowRunAction.getCrNumber())) {
                MessageUtil.setErrorMessageFromRes("error.cr.cannot.execute");
                return;
            }
        }

        RequestContext.getCurrentInstance().execute("PF('dlgAcountInfo').show()");
    }

    public StreamedContent onExportLog() {
        try {
            if (selectedFlowRunAction.getLogFilePath() != null
                    && !selectedFlowRunAction.getLogFilePath().trim().isEmpty()) {
                File fileExport = new File(selectedFlowRunAction.getLogFilePath());

                if (fileExport.exists()) {
                    return new DefaultStreamedContent(new FileInputStream(fileExport), ".zip", fileExport.getName());
                } else {
                    if (selectedFlowRunAction.getLogFileContent() != null) {
                        return new DefaultStreamedContent(new ByteArrayInputStream(selectedFlowRunAction.getLogFileContent()), ".zip", fileExport.getName());
                    }
                    MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                            MessageUtil.getResourceBundleMessage("button.downoad.log")));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    MessageUtil.getResourceBundleMessage("button.downoad.log")));
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(new byte[]{}));
    }

    public void onExportResult() {
        if (selectedFlowRunAction == null)
            return;
        try {
            List<FlowActionResult> flowActionResults = flowRunActionService.findResult(selectedFlowRunAction.getFlowRunId());
            Map<String, Map<String, Integer>> results = new HashMap<>();
            Set<String> actions = new LinkedHashSet<>();
//            System.out.println(flowActionResults.size());
            for (FlowActionResult flowActionResult : flowActionResults) {
                actions.add(flowActionResult.getName() + " - " + flowActionResult.getGroupActionName());
                Map<String, Integer> serverResults = results.get(flowActionResult.getNodeIp());
                if (serverResults == null)
                    serverResults = new HashMap<>();

                serverResults.put(flowActionResult.getName() + " - " + flowActionResult.getGroupActionName(), flowActionResult.getResult());

                results.put(flowActionResult.getNodeIp(), serverResults);
            }

            for (Map<String, Integer> stringIntegerMap : results.values()) {
//                System.out.println(stringIntegerMap);
            }

            List<String> headers = new ArrayList<>(actions);


//            String excelFileName = "H:/Test.xlsx";//name of excel file

            String sheetName = "Sheet1";//name of sheet

            XSSFWorkbook wb = new XSSFWorkbook();
            XSSFSheet sheet = wb.createSheet(sheetName);

            XSSFCellStyle headerStyle = wb.createCellStyle();
            headerStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(7, 128, 51)));
            headerStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            headerStyle.setWrapText(true);

            XSSFCellStyle failStyle = wb.createCellStyle();
            failStyle.setFillForegroundColor(new XSSFColor(new java.awt.Color(199, 29, 26)));
            failStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
            headerStyle.setWrapText(true);
//			headerStyle.set

//			headerStyle.setFont(font);

            XSSFRow row = sheet.createRow(0);
            XSSFCell cell = row.createCell(0);
            sheet.autoSizeColumn(0);
            sheet.setColumnWidth(0, 5000);
            cell.setCellStyle(headerStyle);
            cell.setCellValue("IP");

            cell = row.createCell(1);
            sheet.autoSizeColumn(1);
            sheet.setColumnWidth(1, 5000);
            cell.setCellStyle(headerStyle);
            cell.setCellValue("Final");

            for (int i = 0; i < headers.size(); i++) {
                cell = row.createCell(i + 2);
                sheet.autoSizeColumn(i + 2);
                sheet.setColumnWidth(i + 2, 5000);

                cell.setCellStyle(headerStyle);
                cell.setCellValue(headers.get(i));
            }

            int r = 0;
            for (Map.Entry<String, Map<String, Integer>> mapEntry : results.entrySet()) {
                row = sheet.createRow(++r);
                Map<String, Integer> serverResults = mapEntry.getValue();
                for (int i = 0; i < headers.size(); i++) {
                    cell = row.createCell(0);
                    cell.setCellValue(mapEntry.getKey());

                    cell = row.createCell(1);
                    cell.setCellValue("OK");
                    for (Integer integer : serverResults.values()) {
                        if (integer != null && integer == 0) {
                            cell.setCellValue("NOK");
                            cell.setCellStyle(failStyle);
                            break;
                        }
                    }

                    cell = row.createCell(i + 2);
                    Integer actionResult = serverResults.get(headers.get(i));
                    if (actionResult != null && actionResult == 1) {
                        cell.setCellValue("OK");
                    } else {
                        cell.setCellValue("NOK");
                        cell.setCellStyle(failStyle);
                    }
//                    cell.setCellValue(actionResult != null && actionResult == 1 ? "OK" : "NOK");
                }
            }

//            OutputStream fileOut = new ByteArrayOutputStream();
            HttpServletResponse servletResponse = preHeader();
            OutputStream os = servletResponse.getOutputStream();

            //write this workbook to an Outputstream.
            wb.write(os);
            os.flush();
            os.close();
        } catch (IllegalStateException ie) {
            logger.debug(ie.getMessage());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
//            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
//                    MessageUtil.getResourceBundleMessage("button.downoad.result")));
        }
//        return new DefaultStreamedContent(new ByteArrayInputStream(new byte[] {}));
    }

    private HttpServletResponse preHeader() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpServletResponse servletResponse = (HttpServletResponse) facesContext.getExternalContext().getResponse();
        servletResponse.setContentType("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet");
        servletResponse.setHeader("Expires", "0");
        servletResponse.setHeader("Cache-Control", "must-revalidate, post-check=0, pre-check=0");
        servletResponse.setHeader("Pragma", "public");
        try {
            servletResponse.setHeader("Content-disposition", "attachment;filename=Result" + ".xlsx");
        } catch (Exception e1) {
            logger.error(e1.getMessage(), e1);
        }
        return servletResponse;
    }

    public StreamedContent onExportManualLog(FlowRunLog flowRunLog) {
        try {
            if (flowRunLog.getLogFilePath() != null
                    && !flowRunLog.getLogFilePath().trim().isEmpty()) {
                File fileExport = new File(flowRunLog.getLogFilePath());

                if (fileExport.exists()) {
                    return new DefaultStreamedContent(new FileInputStream(fileExport), ".zip", fileExport.getName());
                } else {
                    if (flowRunLog.getLogFileContent() != null) {
                        return new DefaultStreamedContent(new ByteArrayInputStream(flowRunLog.getLogFileContent()), ".zip", fileExport.getName());
                    }
                    MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                            MessageUtil.getResourceBundleMessage("button.downoad.log")));
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessage(MessageFormat.format(MessageUtil.getResourceBundleMessage("common.fail"),
                    MessageUtil.getResourceBundleMessage("button.downoad.log")));
        }
        return new DefaultStreamedContent(new ByteArrayInputStream(new byte[]{}));
    }

    /*20181219_hoangnd_tien trinh tac dong tu dong_start*/
    public void initAutoMode() {
        flowRunActionService = new FlowRunActionServiceImpl();
        isAutoMode = true;
    }

    /*20181219_hoangnd_tien trinh tac dong tu dong_end*/

    public DefaultDiagramModel getTopoDiagram() {
        return topoDiagram;
    }

    public void setTopoDiagram(DefaultDiagramModel topoDiagram) {
        this.topoDiagram = topoDiagram;
    }

    public ActionOfFlow getSelectedActionFlow() {
        return selectedActionFlow;
    }

    public void setSelectedActionFlow(ActionOfFlow selectedActionFlow) {
        this.selectedActionFlow = selectedActionFlow;
    }

    public FlowRunAction getSelectedFlowRunAction() {
        return selectedFlowRunAction;
    }

    public void setSelectedFlowRunAction(FlowRunAction selectedFlowRunAction) {
        this.selectedFlowRunAction = selectedFlowRunAction;
    }

    public Map<String, List<FlowRunLogCommand>> getMapLogCommand() {
        return mapLogCommand;
    }

    public void setMapLogCommand(Map<String, List<FlowRunLogCommand>> mapLogCommand) {
        this.mapLogCommand = mapLogCommand;
    }

    public FlowRunLogCommand getSelectedLogCommand() {
        return selectedLogCommand;
    }

    public void setSelectedLogCommand(FlowRunLogCommand selectedLogCommand) {
        this.selectedLogCommand = selectedLogCommand;
    }

    public void check() {
        System.out.println("aaaaaaaaaaaaaaaaaaa");
    }

    public FlowRunLog getSelectedFlowRunLog() {
        return selectedFlowRunLog;
    }

    public void setSelectedFlowRunLog(FlowRunLog selectedFlowRunLog) {
        this.selectedFlowRunLog = selectedFlowRunLog;
    }

    public Map<String, List<FlowRunLogCommand>> getMapLogCommandManual() {
        return mapLogCommandManual;
    }

    public void setMapLogCommandManual(Map<String, List<FlowRunLogCommand>> mapLogCommandManual) {
        this.mapLogCommandManual = mapLogCommandManual;
    }

    public LazyDataModel<FlowRunLog> getLstFlowRunLog() {
        return lstFlowRunLog;
    }

    public void setLstFlowRunLog(LazyDataModel<FlowRunLog> lstFlowRunLog) {
        this.lstFlowRunLog = lstFlowRunLog;
    }

    public FlowRunLogServiceImpl getFlowRunLogService() {
        return flowRunLogService;
    }

    public void setFlowRunLogService(FlowRunLogServiceImpl flowRunLogService) {
        this.flowRunLogService = flowRunLogService;
    }

    public Long getActionOfFlowId() {
        return actionOfFlowId;
    }

    public void setActionOfFlowId(Long actionOfFlowId) {
        this.actionOfFlowId = actionOfFlowId;
    }

    public int getActiveIndex() {
        return activeIndex;
    }

    public void setActiveIndex(int activeIndex) {
        this.activeIndex = activeIndex;
    }

    public int getModeRun() {
        return modeRun;
    }

    public void setModeRun(int modeRun) {
        this.modeRun = modeRun;
    }

    public boolean isShowDiagramOnly() {
        return isShowDiagramOnly;
    }

    public void setShowDiagramOnly(boolean isShowDiagramOnly) {
        this.isShowDiagramOnly = isShowDiagramOnly;
    }

    public Map<Long, Integer> getMapTotalCmdAction() {
        return mapTotalCmdAction;
    }

    public void setMapTotalCmdAction(Map<Long, Integer> mapTotalCmdAction) {
        this.mapTotalCmdAction = mapTotalCmdAction;
    }

    public Map<String, String> getMapLogCommandOnline() {
        return mapLogCommandOnline;
    }

    public void setMapLogCommandOnline(Map<String, String> mapLogCommandOnline) {
        this.mapLogCommandOnline = mapLogCommandOnline;
    }

    public void setFlowRunActionService(FlowRunActionServiceImpl flowRunActionService) {
        this.flowRunActionService = flowRunActionService;
    }

    public boolean isReboot() {
        return isReboot;
    }

    public void setReboot(boolean reboot) {
        isReboot = reboot;
    }
}
