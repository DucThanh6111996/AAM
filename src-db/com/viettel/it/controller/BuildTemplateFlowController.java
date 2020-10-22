package com.viettel.it.controller;

import com.viettel.it.model.*;
import com.viettel.it.object.ComboBoxObject;
import com.viettel.it.persistence.*;
import com.viettel.it.persistence.Category.CategoryConfigGetNodeServiceImpl;
import com.viettel.it.persistence.Category.CategoryDomainServiceImpl;
import com.viettel.it.persistence.Category.CategoryGroupDomainServiceImpl;
import com.viettel.it.util.Config;
import com.viettel.it.util.*;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.PasswordEncoder;
import com.viettel.util.*;
import com.viettel.util.Util;
import org.apache.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.primefaces.component.datatable.DataTable;
import org.primefaces.context.RequestContext;
import org.primefaces.event.CellEditEvent;
import org.primefaces.event.ReorderEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ViewScoped
@ManagedBean
public class BuildTemplateFlowController implements Serializable {

    protected static final Logger logger = LoggerFactory.getLogger(BuildTemplateFlowController.class);

    public final static Long startIndexOrder = 1l;
    public final static Long DEFAULT_PARAM_GROUP_ID = -1l;
    public final static Long MAX_LENGTH_TEMPLATE = 200L;
    public final static Long MAX_LENGTH_GROUP_NAME = 200L;

    private FlowTemplates selectedFlowTemplate;
    private List<FlowTemplates> lstFlowTemplate = new ArrayList<>();
    private Map<String, Integer> mapGroupName = new LinkedHashMap<>();
    private List<List<ActionOfFlow>> actionOfFlowss = new LinkedList<>();
    private List<SelectItem> lstItemGroupName = new ArrayList<>();
    private List<ActionOfFlow> lstActionFlowDel = new ArrayList<>();
    private List<ParamGroup> lstParamGroup = new ArrayList<>();
    private List<ParamInOut> lstParamInOutObject = new ArrayList<>();
    private List<ActionOfFlow> lstActionFlow = new ArrayList<>();
    private List<ActionCommand> lstActionCommand = new ArrayList<>();
    private List<ParamInput> paramInputs = new ArrayList<>();

    private String copyFlowTemplateName;
    private String flowTemplateName;
    private Integer flowTemplateType; // 1 - tac dong qua Cr; 0 - tac dong uctt (qua voffice)

    private String userName;
    private TreeNode selectedTreeNodeAction;
    private String selectedGroupActionName;
    private boolean isAddNewGroupName;
    private ActionOfFlow selectedActionFlow;
    private Action selectedActionView;
    private ActionDetail selectedActionDetail;
    private ActionCommand selectedActionCmd;

    private ParamGroup selectedParamGroup;

    private boolean isEdit;
    private boolean isChangeGroupActionName;
    private boolean isChangeGroupCode;
    private String newGroupActionName;
    private String oldGroupActionName;
    private List<Node> nodes;
    private FlowRunAction flowRunAction;

    private ParamInOut selectedParamInOut;

    private Boolean[] defaultAddToTemplate = new Boolean[10];

    private Integer indexOfGroupActionToDel;

    private boolean isApproveTemplate;
    private boolean isPreApproveTemplate;
    private Node selectedNode;
//    private AutoConfigNodeTemplate configNodeTemplate;

    private List<String> preStepsActionSelected;
    //20180326_Quytv7_them cau hinh canh bao
    private List<ParamInput> lstParamInputInTemplates = new ArrayList<>();
    private ConcurrentHashMap<String, FlowTemplateMapAlarm> mapParamAlarmInTemplates = new ConcurrentHashMap<>();
    private List<FlowTemplateMapAlarm> lstParamAlarmInTempates = new ArrayList<>();
    private List<FlowTemplateMapAlarm> lstParamNodeAlarmInTempates = new ArrayList<>();
    private ComboBoxObject categoryConfigGetNodeGroup;
    private List<ComboBoxObject> categoryConfigGetNodeGroups;
    private CategoryConfigGetNode categoryConfigGetNode;
    private List<CategoryGroupDomain> categoryGroupDomains;
    private CategoryGroupDomain categoryGroupDomain;
    private String systemTypeMapParam;
    private List<CategoryDomain> categoryDomains;
    //Quytv7_20180523_Them moi template group start
    private TemplateGroup selectTemplateGroup;
    //Quytv7_20180523_Them moi template group end
    //20181119_tudn_start them danh sach lenh blacklist
    private List<CatConfig> lstCrType = new ArrayList<>();
//    private String selectedProcedureId;

    public List<CatConfig> getLstCrType() {
        return lstCrType;
    }

    public void setLstCrType(List<CatConfig> lstCrType) {
        this.lstCrType = lstCrType;
    }
//    public String getSelectedProcedureId() {
//        return selectedProcedureId;
//    }
//    public void setSelectedProcedureId(String selectedProcedureId) {
//        this.selectedProcedureId = selectedProcedureId;
//    }
    //20181119_tudn_end them danh sach lenh blacklist

    //20190401_tudn_start them dau viec quy trinh cho GNOC
    private List<ProcedureGNOC> lstProcedureId = new ArrayList<>();
    private List<ProcedureGNOC> lstProcedureWFId = new ArrayList<>();
    private Long selectedProcedureWFId;
    private Long selectedProcedureId;
    //20190401_tudn_end them dau viec quy trinh cho GNOC

    //20190729_tudn_start sua dau viec quy trinh cho GNOC
    private TreeNode rootProcedureId;
    private TreeNode selectedNodeProcedureId;
    private static final String PARENT_NODE = "parent";
    private static final String CHILD_NODE = "child";
    private String selectedProcedureName;
    private String selectedProcedureNameEn;
    private String keyProcedureSearch;
    private List<ProcedureGNOC> lstProcedureIdEn = new ArrayList<>();
    private TreeNode rootProcedureIdEn;
    private int statusApprove = 0;
    private final String selectedProcedureNameTemp = "---Chon quy trinh GNOC---";
    private final String selectedProcedureNameEnTemp = "---Choose procedure work flow GNOC---";
    private final String selectedProcedureWFNameTemp = "---Chon dau viec GNOC---";
    private final String selectedProcedureWFNameEnTemp = "---Choose procedure work flow GNOC---";
    private String selectedProcedureWFName = selectedProcedureWFNameTemp;
    private String selectedProcedureWFNameEn = "---Choose procedure work flow GNOC---";
    //20190729_tudn_end sua dau viec quy trinh cho GNOC

    /*20190408_chuongtq start check param when create MOP*/
    private Integer nIndex = 0;
    private List<ParamCondition> lstParamCondition = new ArrayList<>();
    private List<ParamInput> lstParamInputCondition = new ArrayList<>();
    /*20190408_chuongtq end check param when create MOP*/

    @PostConstruct
    public void onStart() {
        try {
            isApproveTemplate = new SessionUtil().isApproveTemplate();
            isPreApproveTemplate = new SessionUtil().isPreApproveTemplate();
            isEdit = false;
            isAddNewGroupName = false;
            userName = SessionWrapper.getCurrentUsername();
            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            orders.put("flowTemplateName", "ASC");
            lstFlowTemplate = new FlowTemplatesServiceImpl().findList(null, orders);
            //selectedFlowTemplate=lstFlowTemplate.get(0);
            selectedFlowTemplate = null;
            onChangeFlowTemplate();
            //20181119_tudn_start them danh sach lenh blacklist
            loadCrTypes();
            //20181119_tudn_end them danh sach lenh blacklist

            //20190401_tudn_start them dau viec quy trinh cho GNOC
//            //20190729_tudn_start them dau viec quy trinh cho GNOC
////            loadLstProcedureId();
//            loadTreeProcedureId();
//            //20190729_tudn_end them dau viec quy trinh cho GNOC
            //20190401_tudn_end them dau viec quy trinh cho GNOC
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    //20181119_tudn_start them danh sach lenh blacklist
    private void loadCrTypes() {
        try {
            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            orders.put("cfgOrder", "ASC");
            Map<String, Object> filter = new HashMap<>();
            filter.put("id.configGroup-EXAC", AamConstants.CFG_CR_TYPE);
            filter.put("isActive-EXAC", 1L);
            lstCrType = new CatConfigServiceImpl().findList(filter, orders);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    //20181119_tudn_end them danh sach lenh blacklist
    public void onChangeFlowTemplate() {
        try {
            clean();
            getFlowTemplateSelected();

            if (selectedFlowTemplate != null) {
                selectedFlowTemplate = new FlowTemplatesServiceImpl().findById(selectedFlowTemplate.getFlowTemplatesId());
                flowTemplateName = selectedFlowTemplate.getFlowTemplateName();
                selectedFlowTemplate.setGenerationDT((selectedFlowTemplate.getIsGenerateDT()!=null && selectedFlowTemplate.getIsGenerateDT() == 1L)? true : false );

                isEdit = true;
                rebuildMapFlowAction(new ArrayList<ActionOfFlow>(selectedFlowTemplate.getActionOfFlows()));
//                selectedProcedureId = selectedFlowTemplate.getProcedureId();
//                selectedProcedureId = checkProcedureActive(selectedProcedureId);
//                loadTreeProcedureId();
//                onSetTreeName(selectedProcedureId);
//                procedureChanged();
//                selectedProcedureWFId = selectedFlowTemplate.getProcedureWorkFlowId();
//                statusApprove = selectedFlowTemplate.getStatus();
//                onClickProcedureWF();
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        loadDefaultAddToTemplate();
    }
    public Long checkProcedureActive(Long procedureId){
        try {
            Map<String, Object> filter = new HashMap<>();

            filter.put("isActive", 1L);
            filter.put("procedureGNOCId", procedureId);
            List lstProcedure = new ProcedureGNOCServiceImpl().findList(filter);
            if(lstProcedure != null && !lstProcedure.isEmpty()){
                return procedureId;
            }else {
                return null;
            }

        }catch(Exception ex){
            logger.error(ex.getMessage(),ex);
            return null;
        }
    }

    private void rebuildMapFlowAction(List<ActionOfFlow> lstActionFlow) {
        try {
            mapGroupName = new LinkedHashMap<>();
            if (lstActionFlow.size() > 0) {
                Collections.sort(lstActionFlow, new Comparator<ActionOfFlow>() {

                    @Override
                    public int compare(ActionOfFlow object1, ActionOfFlow object2) {
                        if (Long.compare(object1.getGroupActionOrder(), object2.getGroupActionOrder()) == 0) {
                            return Long.compare(object1.getStepNumberLabel(), object2.getStepNumberLabel());
                        } else {
                            return Long.compare(object1.getGroupActionOrder(), object2.getGroupActionOrder());
                        }
                    }
                });

                int index = 0;
                Map<String, Integer> mapActionName = new HashMap<>();
// 			    for (ActionOfFlow actionFlow : lstActionFlow) {
                int size = lstActionFlow.size();
                for (int i = 0; i < size; i++) {
                    if (mapGroupName.get(lstActionFlow.get(i).getGroupActionName()) == null) {
                        mapGroupName.put(lstActionFlow.get(i).getGroupActionName(), index);
                        actionOfFlowss.add(new LinkedList<ActionOfFlow>());
                        index++;
                    }

                    ActionOfFlow actionFlow = lstActionFlow.get(i);
                    if (mapActionName.get(actionFlow.getStepNumberLabel() + "_" + actionFlow.getGroupActionOrder() + "_" + actionFlow.getAction().getActionId()) == null) {

                        mapActionName.put(actionFlow.getStepNumberLabel() + "_" + actionFlow.getGroupActionOrder() + "_" + actionFlow.getAction().getActionId(), 1);

                        actionFlow.setPreStepsNumber(new ArrayList<String>(Arrays.asList(lstActionFlow.get(i).getPreviousStep() + "")));
                        actionFlow.setPreStepsNumberLabel(lstActionFlow.get(i).getPreviousStep() + "");

                        actionFlow.setPreStepsCondition(lstActionFlow.get(i).getIfValue());
                        actionFlow.setActionFlowIds(new ArrayList<Long>(Arrays.asList(lstActionFlow.get(i).getStepNum())));
                        actionFlow.setLstNodeRunGroupAction(new ArrayList<List<NodeRunGroupAction>>());
                        actionFlow.getLstNodeRunGroupAction().add(lstActionFlow.get(i).getNodeRunGroupActions());
                        if (lstActionFlow.get(i).getIsRollback() != null)
                            actionFlow.setRollbackStatus(lstActionFlow.get(i).getIsRollback().equals(Config.ROLLBACK_ACTION) ? true : false);

                        for (int j = i + 1; j < size; j++) {
                            if ((lstActionFlow.get(i).getStepNumberLabel().equals(lstActionFlow.get(j).getStepNumberLabel()))
                                    && (lstActionFlow.get(i).getGroupActionOrder().equals(lstActionFlow.get(j).getGroupActionOrder()))
                                    && (lstActionFlow.get(i).getAction().getActionId().equals(lstActionFlow.get(j).getAction().getActionId()))) {

                                actionFlow.getPreStepsNumber().add(lstActionFlow.get(j).getPreviousStep() + "");
                                actionFlow.setPreStepsNumberLabel(actionFlow.getPreStepsNumberLabel().concat(",").concat(lstActionFlow.get(j).getPreviousStep() + ""));

                                actionFlow.getActionFlowIds().add(lstActionFlow.get(j).getStepNum());
                                actionFlow.setPreStepsCondition(actionFlow.getPreStepsCondition().concat(",").concat(lstActionFlow.get(j).getIfValue()));
                                actionFlow.getLstNodeRunGroupAction().add(lstActionFlow.get(j).getNodeRunGroupActions());
                            } else {
                                break;
                            }
                        }

                        if (actionFlow.getPreStepsNumberLabel().endsWith(",")) {
                            actionFlow.setPreStepsNumberLabel(actionFlow.getPreStepsNumberLabel().substring(0, actionFlow.getPreStepsNumberLabel().length() - 1));
                        }
                        if (actionFlow.getPreStepsCondition().endsWith(",")) {
                            actionFlow.setPreStepsCondition(actionFlow.getPreStepsCondition().substring(0, actionFlow.getPreStepsCondition().length() - 1));
                        }
                        actionOfFlowss.get(actionOfFlowss.size() - 1).add(actionFlow);
                    }
                } // end loop for

                lstItemGroupName = new ArrayList<>();
                for (Map.Entry<String, Integer> entry : mapGroupName.entrySet()) {
                    lstItemGroupName.add(new SelectItem(entry.getKey(), entry.getKey()));
                }
            } else {
                MessageUtil.setWarnMessageFromRes("label.war.template.empty");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void addActiontoFlowTemplate(TreeNode selectedTreeNodeAction) {
        try {
            if (selectedTreeNodeAction != null
                    && selectedGroupActionName != null
                    && !selectedGroupActionName.trim().isEmpty()) {
                Action action = (Action) selectedTreeNodeAction.getData();
                ActionOfFlow actionOfFlow = new ActionOfFlow();
                actionOfFlow.setAction(action);
                actionOfFlow.setFlowTemplates(selectedFlowTemplate);
                actionOfFlow.setStepNumberLabel(Long.valueOf(actionOfFlowss.get(mapGroupName.get(selectedGroupActionName)).size() + 1));
                actionOfFlow.setGroupActionName(selectedGroupActionName);
                actionOfFlow.setIsRollback(Config.EXECUTE_ACTION);

                if (actionOfFlowss.get(mapGroupName.get(selectedGroupActionName)) == null
                        || actionOfFlowss.get(mapGroupName.get(selectedGroupActionName)).isEmpty()) {
                    actionOfFlow.setGroupActionOrder(Long.valueOf(getIndexGroupOrder(actionOfFlowss)));
                } else {
                    List<ActionOfFlow> lstActionFlow = actionOfFlowss.get(mapGroupName.get(selectedGroupActionName));
                    actionOfFlow.setGroupActionOrder(lstActionFlow.get(0).getGroupActionOrder());
                }
                actionOfFlowss.get(mapGroupName.get(selectedGroupActionName)).add(actionOfFlow);
                selectedGroupActionName = null;
                RequestContext.getCurrentInstance().execute("PF('dlgActionFlow').hide()");
            } else {
                MessageUtil.setErrorMessageFromRes("label.error.templateEmpty");
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private int getIndexGroupOrder(List<List<ActionOfFlow>> lstOfActionFlows) {
        int indexGroupOrder = 0;

        int indexOfActionFlowss = mapGroupName.get(selectedGroupActionName);
        if (actionOfFlowss.size() == 1) {
            indexGroupOrder = 1;

        } else if (indexOfActionFlowss <= (actionOfFlowss.size() - 1)) {

            boolean isGettedIndxGroup = false;
            // duyet ve cuoi cua list
            int countDown = 0;
            for (int i = indexOfActionFlowss + 1; i < actionOfFlowss.size(); i++) {
                countDown++;
                if (!actionOfFlowss.get(i).isEmpty()) {
                    indexGroupOrder = actionOfFlowss.get(i).get(0).getGroupActionOrder().intValue() - countDown;
                    isGettedIndxGroup = true;
                    break;
                }
            }

            // Duyet nguoc lai dau cua list
            if (!isGettedIndxGroup) {
                int countUp = 0;
                for (int i = indexOfActionFlowss - 1; i >= 0; i--) {
                    countUp++;
                    if (!actionOfFlowss.get(i).isEmpty()) {
                        indexGroupOrder = actionOfFlowss.get(i).get(0).getGroupActionOrder().intValue() + countUp;
                        isGettedIndxGroup = true;
                        break;
                    }
                }
            }

            // Neu tat ca cac action group deu trong thi tra ve index group trong actions
            if (!isGettedIndxGroup) {
                indexGroupOrder = indexOfActionFlowss + 1;
            }

        } else {
            for (List<ActionOfFlow> lstActionFlow : lstOfActionFlows) {
                if (!lstActionFlow.isEmpty()
                        && indexGroupOrder < lstActionFlow.get(0).getGroupActionOrder().intValue()) {
                    indexGroupOrder = lstActionFlow.get(0).getGroupActionOrder().intValue();
                }
            }
            indexGroupOrder += 1;
        }
        return indexGroupOrder;
    }

    //hienhv4_20160926_di chuyen dau viec_start
    public void moveUp(int indexToUp) {
        try {
            if (indexToUp == 0) {
                MessageUtil.setErrorMessageFromRes("message.move.up.cannot");
            }
            int indexToDown = indexToUp - 1;

            swapDvOrder(indexToDown, indexToUp);

            MessageUtil.setInfoMessageFromRes("message.move.up.success");
        } catch (Exception ex) {
            MessageUtil.setErrorMessageFromRes("message.move.up.fail");
            logger.error(ex.getMessage(), ex);
        }
    }

    private void swapDvOrder(int indexToDown, int indexToUp) {
        String groupNameDown = "";
        String groupNameUp = "";
        for (String key : mapGroupName.keySet()) {
            if (indexToDown == mapGroupName.get(key)) {
                groupNameDown = key;
            } else if (indexToUp == mapGroupName.get(key)) {
                groupNameUp = key;
            }
        }
        mapGroupName.put(groupNameDown, -1);
        mapGroupName.put(groupNameUp, indexToDown);
        mapGroupName.put(groupNameDown, indexToUp);

        List<ActionOfFlow> actionFlowDowns = new ArrayList<>();
        List<ActionOfFlow> actionFlowUps = new ArrayList<>();
        for (int i = 0; i < actionOfFlowss.size(); i++) {
            if (i == indexToDown) {
                actionFlowDowns = actionOfFlowss.get(indexToDown);
            } else if (i == indexToUp) {
                actionFlowUps = actionOfFlowss.get(indexToUp);
            }
        }
        actionOfFlowss.set(indexToUp, actionFlowDowns);
        actionOfFlowss.set(indexToDown, actionFlowUps);
        if (!actionFlowDowns.isEmpty() && !actionFlowUps.isEmpty()) {
            long temp = actionFlowDowns.get(0).getGroupActionOrder();
            for (ActionOfFlow action : actionFlowDowns) {
                action.setGroupActionOrder(actionFlowUps.get(0).getGroupActionOrder());
            }

            for (ActionOfFlow action : actionFlowUps) {
                action.setGroupActionOrder(temp);
            }
        } else if (actionFlowUps.isEmpty()) {
            for (ActionOfFlow action : actionFlowDowns) {
                action.setGroupActionOrder(action.getGroupActionOrder() + 1);
            }
        } else {
            for (ActionOfFlow action : actionFlowUps) {
                action.setGroupActionOrder(action.getGroupActionOrder() - 1);
            }
        }

        SelectItem itemGroupUp = null;
        SelectItem itemGroupDown = null;
        for (int i = 0; i < lstItemGroupName.size(); i++) {
            if (i == indexToDown) {
                itemGroupDown = lstItemGroupName.get(indexToDown);
            } else if (i == indexToUp) {
                itemGroupUp = lstItemGroupName.get(indexToUp);
            }
        }
        lstItemGroupName.set(indexToUp, itemGroupDown);
        lstItemGroupName.set(indexToDown, itemGroupUp);
    }

    public void moveDown(int indexToDown) {
        try {
            if (indexToDown == lstItemGroupName.size() - 1) {
                MessageUtil.setErrorMessageFromRes("message.move.down.cannot");
            }
            int indexToUp = indexToDown + 1;

            swapDvOrder(indexToDown, indexToUp);

            MessageUtil.setInfoMessageFromRes("message.move.down.success");
        } catch (Exception ex) {
            MessageUtil.setErrorMessageFromRes("message.move.down.fail");
            logger.error(ex.getMessage(), ex);
        }
    }
    //hienhv4_20160926_di chuyen dau viec_end

    public void saveOrUpdateGroupName() {
        try {
            if (newGroupActionName != null && !newGroupActionName.trim().isEmpty()) {
                if (newGroupActionName.trim().length() > MAX_LENGTH_GROUP_NAME) {
                    MessageUtil.setErrorMessageFromRes("label.validate.length.groupname");
                    return;
                }
                // kiem tra xem ten group da ton tai hay chua
                int count = 0;
                for (SelectItem item : lstItemGroupName) {
                    if (item.getLabel().trim().equalsIgnoreCase(newGroupActionName.trim())) {
                        count++;
                    }
                }

                // Neu la them moi group name
                if (!isChangeGroupActionName) {
                    if (count > 0) {
                        MessageUtil.setErrorMessageFromRes("label.error.exist");
                    } else {
                        actionOfFlowss.add(new LinkedList<ActionOfFlow>());
                        mapGroupName.put(newGroupActionName, actionOfFlowss.size() - 1);

                        lstItemGroupName.add(new SelectItem(newGroupActionName, newGroupActionName));
                        newGroupActionName = "";
                        MessageUtil.setInfoMessageFromRes("label.action.updateOk");
                        RequestContext.getCurrentInstance().execute("PF('dlgAddNewGroupName').hide()");
                    }
                    // Neu la cap nhat group name
                } else {
                    if (count >= 1) {
                        MessageUtil.setErrorMessageFromRes("error.group.action.exist");
                    } else {
                        Integer indexGroupAction = mapGroupName.get(oldGroupActionName.trim());
                        if (indexGroupAction != null && actionOfFlowss.get(indexGroupAction) != null) {

                            for (int i = 0; i < actionOfFlowss.get(indexGroupAction).size(); i++) {
                                actionOfFlowss.get(indexGroupAction).get(i).setGroupActionName(newGroupActionName);
                            }
                        }

                        int index = 0;
                        for (SelectItem item : lstItemGroupName) {
                            if (item.getLabel().trim().equalsIgnoreCase(oldGroupActionName.trim())) {
                                lstItemGroupName.remove(index);
                                break;
                            }
                            index++;
                        }
                        lstItemGroupName.add(index, new SelectItem(newGroupActionName.trim(), newGroupActionName.trim()));
                        mapGroupName.remove(oldGroupActionName);
                        mapGroupName.put(newGroupActionName, indexGroupAction);

                        newGroupActionName = "";
                        MessageUtil.setInfoMessageFromRes("label.action.updateOk");
                        RequestContext.getCurrentInstance().execute("PF('dlgAddNewGroupName').hide()");
                    }
                }
            } else {
                MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
            }

        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("label.require.set.data");
            logger.error(e.getMessage(), e);
        } finally {
            isChangeGroupActionName = false;
            oldGroupActionName = null;
            isAddNewGroupName = false;
        }
        loadDefaultAddToTemplate();
    }

    private void loadDefaultAddToTemplate() {
        defaultAddToTemplate = new Boolean[mapGroupName.size()];
        for (int i = 0; i < mapGroupName.size(); i++)
            defaultAddToTemplate[i] = false;

    }

    public void prepareRenameGroup(int index) {
        oldGroupActionName = getActionGroupNameData(index);
        isChangeGroupActionName = true;
        newGroupActionName = null;
    }

    public void prepareAddGroup() {
        isChangeGroupActionName = false;
        newGroupActionName = null;
    }

    public void deleteGroup(int index) {
        try {

            List<ActionOfFlow> listActionFlow = actionOfFlowss.get(index);

            // Kiem tra xem danh sach action flow da duoc sinh MOP hay chua
            if (listActionFlow != null && !listActionFlow.isEmpty()) {
                for (ActionOfFlow actionFlow : listActionFlow) {
                    List<Long> tmps = new ArrayList<Long>();
                    for (ActionOfFlow actionFLow : listActionFlow) {
                        tmps.add(actionFLow.getStepNum());
                    }
                    if (tmps.size() > 0) {
                        Map<String, Collection<?>> map = new HashMap<String, Collection<?>>();
                        map.put("stepNums", tmps);
                        List<NodeRunGroupAction> tmps2 = new NodeRunGroupActionServiceImpl().findListWithIn("from NodeRunGroupAction where id.stepNum in (:stepNums)", -1, -1, map);
                        if (tmps2.size() > 0) {
                            MessageUtil.setErrorMessageFromRes("label.group.action.created.mop");
                            return;
                        }
                    }
                    /*if (actionFlow.getNodeRunGroupActions() != null
							&& !actionFlow.getNodeRunGroupActions().isEmpty()) {
						MessageUtil.setErrorMessageFromRes("label.group.action.created.mop");
						return;
					}*/
                }
            }

            for (ActionOfFlow actionFlow : listActionFlow) {
                if (actionFlow.getActionFlowIds() != null) {
                    for (Long id : actionFlow.getActionFlowIds()) {
                        try {
                            ActionOfFlow actionFlowDel = new ActionOfFlowServiceImpl().findById(id);
                            if (actionFlowDel != null) {
                                lstActionFlowDel.add(actionFlowDel);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    }
                }
            }

//			lstActionFlowDel.addAll(listActionFlow);
            listActionFlow.clear();
            actionOfFlowss.remove(index);

            for (Iterator<String> iterator = mapGroupName.keySet().iterator(); iterator.hasNext(); ) {
                String groupName = iterator.next();
                if (mapGroupName.get(groupName) != null && mapGroupName.get(groupName) == index) {
                    iterator.remove();
                    continue;
                }
                if (mapGroupName.get(groupName) != null && mapGroupName.get(groupName) > index)
                    mapGroupName.put(groupName, mapGroupName.get(groupName) - 1);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        lstItemGroupName.clear();
        for (Map.Entry<String, Integer> entry : mapGroupName.entrySet()) {
            lstItemGroupName.add(new SelectItem(entry.getKey(), entry.getKey()));
        }
        loadDefaultAddToTemplate();
    }

    public void renameGroup() {
//		if ( newGroupActionName != null && !newGroupActionName.trim().isEmpty()) {
//			if (newGroupActionName.trim().length() > MAX_LENGTH_GROUP_NAME) {
//				MessageUtil.setErrorMessageFromRes("label.validate.length.groupname");
//				return;
//			}
//			// kiem tra xem ten group da ton tai hay chua
//			int countExist = 0;
//			for (SelectItem item : lstItemGroupName) {
//				if (item.getLabel().trim().equals(newGroupActionName.trim())) {
//					countExist++;
//				}
//			}
//
//			if (countExist > 1) {
//				MessageUtil.setErrorMessageFromRes("label.error.exist");
//			} else {
//				List<ActionOfFlow> lstActionFlow = new ArrayList<>();
//				lstActionFlow.addAll(mapActionOfFlow.get(oldGroupActionName));
//				mapActionOfFlow.remove(oldGroupActionName);
//				mapActionOfFlow.put(newGroupActionName, lstActionFlow);
//
//				int index = 0;
//				for(SelectItem item : lstItemGroupName) {
//					if (item.getLabel().trim().equals(oldGroupActionName.trim())) {
//						lstItemGroupName.remove(index);
//						break;
//					}
//					index++;
//				}
//
//				lstItemGroupName.add(index, new SelectItem(newGroupActionName.trim(), newGroupActionName.trim()));
//				newGroupActionName = "";
//				MessageUtil.setInfoMessageFromRes("label.action.updateOk");
//				RequestContext.getCurrentInstance().execute("PF('dlgAddNewGroupName').hide()");
//			}
//		} else {
//			MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
//		}
    }

    public void reorderDataTable(ReorderEvent event) {
        try {
            DataTable actionFlowData = (DataTable) event.getSource();
            ActionOfFlow actionFlow = (ActionOfFlow) actionFlowData.getRowData();

            for (int i = 0; i < actionOfFlowss.get(mapGroupName.get(actionFlow.getGroupActionName())).size(); i++) {
                actionOfFlowss.get(mapGroupName.get(actionFlow.getGroupActionName())).get(i).setStepNumberLabel(Long.valueOf(i + 1));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void approveTemplateLevel(int status) {
        //if (isApproveTemplate) {
//        approvalTemplate(Config.APPROVALED_STATUS_LEVEL2);
        approvalTemplate(status);
//		} else if (isPreApproveTemplate) {
//			approvalTemplate(Config.APPROVALED_STATUS_LEVEL1);
//		}
    }

    public void approvalTemplate(Integer templateStatus) {
        if (selectedFlowTemplate != null) {
            //20180828_tudn_start approve template
            SessionUtil sessionUtil = new SessionUtil();

            if (templateStatus == null) {
                MessageUtil.setErrorMessageFromRes("label.action.updateFail");
                return;
            } else if (templateStatus.compareTo(Config.APPROVALED_STATUS_LEVEL2) == 0) {
                if (!sessionUtil.isApproveTempExecMana()) {
                    MessageUtil.setErrorMessageFromRes("error.no.permission.approved.level2");
                    return;
                }
                if (selectedFlowTemplate.getStatus() != Config.APPROVALED_STATUS_LEVEL1) {
                    MessageUtil.setErrorMessageFromRes("label.action.updateFail.level2");
                    return;
                }
            } else if (templateStatus.compareTo(Config.APPROVALED_STATUS_LEVEL1) == 0) {
                if (!sessionUtil.isApproveTemplate()) {
                    MessageUtil.setErrorMessageFromRes("error.no.permission.approved.level1");
                    return;
                }
            }
            //20180828_tudn_end approve template

            //20190729_tudn_start sua dau viec quy trinh cho GNOC
//            if ((selectedProcedureId == null || selectedProcedureId.compareTo(0L) == 0) && templateStatus.compareTo(Config.APPROVALED_STATUS_LEVEL2) == 0) {
//                MessageUtil.setErrorMessageFromRes("label.error.fill.cr.type");
//                return;
//            }
            //20190729_tudn_end sua dau viec quy trinh cho GNOC

            Date startTime = new Date();
            selectedFlowTemplate.setStatus(templateStatus);

            try {
                //20190729_tudn_start sua dau viec quy trinh cho GNOC
                if (templateStatus == 8) {
                    new FlowTemplatesServiceImpl().execteBulk("update FlowTemplates set status = ? where flowTemplatesId =?", templateStatus, selectedFlowTemplate.getFlowTemplatesId());
                } else if (templateStatus == 9) {
                    new FlowTemplatesServiceImpl().execteBulk("update FlowTemplates set status = ?, procedureId = ?, procedureWorkFlowId = ? where flowTemplatesId =?", templateStatus, selectedProcedureId, selectedProcedureWFId, selectedFlowTemplate.getFlowTemplatesId());
                }
//                new FlowTemplatesServiceImpl().saveOrUpdate(selectedFlowTemplate);
                //20190729_tudn_start sua dau viec quy trinh cho GNOC
                statusApprove = templateStatus;
                //20190729_tudn_end sua dau viec quy trinh cho GNOC
                //20190729_tudn_end sua dau viec quy trinh cho GNOC

                /*
                    Ghi log tac dong nguoi dung
                    */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.UPDATE,
                            selectedFlowTemplate.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                MessageUtil.setInfoMessageFromRes("label.action.updateOk");
            } catch (Exception e) {
                MessageUtil.setErrorMessageFromRes("label.action.updateFail");
                logger.error(e.getMessage(), e);
            }

        } else {
            MessageUtil.setErrorMessageFromRes("datatable.empty");
        }
    }

    //20180828_tudn_start apppro template
//    public void cancelApprovalTemplate() {
    public void cancelApprovalTemplate(Integer templateStatus) {
        //20180828_tudn_start apppro template
        if (selectedFlowTemplate != null) {
            //20180828_tudn_start approve template
            SessionUtil sessionUtil = new SessionUtil();

            if (templateStatus == null) {
                MessageUtil.setErrorMessageFromRes("label.action.updateFail");
                return;
            } else if (templateStatus.compareTo(Config.APPROVAL_STATUS_DEFAULT) == 0) {
                if (!sessionUtil.isApproveTemplate()) {
                    MessageUtil.setErrorMessageFromRes("error.no.permission.cancel.approved.level1");
                    return;
                }
                if (selectedFlowTemplate.getStatus() == Config.APPROVALED_STATUS_LEVEL2) {
                    MessageUtil.setErrorMessageFromRes("label.action.cancelFail.level1");
                    return;
                }
            } else if (templateStatus.compareTo(Config.APPROVALED_STATUS_LEVEL1) == 0) {
                if (!sessionUtil.isApproveTempExecMana()) {
                    MessageUtil.setErrorMessageFromRes("error.no.permission.cancel.approved.level2");
                    return;
                }
            }
            //20180828_tudn_end approve template
            Date startTime = new Date();
//            selectedFlowTemplate.setStatus(Config.APPROVAL_STATUS_DEFAULT);
            selectedFlowTemplate.setStatus(templateStatus);
            try {
                //20190729_tudn_start sua dau viec quy trinh cho GNOC
                if (templateStatus == 0) {
                    new FlowTemplatesServiceImpl().execteBulk("update FlowTemplates set status = ? where flowTemplatesId =?", templateStatus, selectedFlowTemplate.getFlowTemplatesId());
                } else if (templateStatus == 8) {
                    new FlowTemplatesServiceImpl().execteBulk("update FlowTemplates set status = ? where flowTemplatesId =?", templateStatus, selectedFlowTemplate.getFlowTemplatesId());
                }
                onChangeFlowTemplate();
                //20190729_tudn_start sua dau viec quy trinh cho GNOC
                statusApprove = templateStatus;
                //20190729_tudn_end sua dau viec quy trinh cho GNOC
                /*
                Ghi log tac dong nguoi dung
                */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.UPDATE,
                            selectedFlowTemplate.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                MessageUtil.setInfoMessageFromRes("label.action.updateOk");
            } catch (Exception e) {
                MessageUtil.setErrorMessageFromRes("label.action.updateFail");
                logger.error(e.getMessage(), e);
            }

        } else {
            MessageUtil.setErrorMessageFromRes("datatable.empty");
        }
    }

    public void prepareRenameTemplate() {
        getFlowTemplateSelected();

        flowTemplateName = selectedFlowTemplate.getFlowTemplateName();
        selectTemplateGroup = selectedFlowTemplate.getTemplateGroup();
        flowTemplateType = selectedFlowTemplate.getTemplateType();
        isEdit = true;
    }

    public void updateFlowTemplate() {
        try {
            Date startTime = new Date();
            // kiem tra xem ten template da ton tai trong csdl hay chu
            if (flowTemplateName == null || flowTemplateName.trim().isEmpty() || selectTemplateGroup == null) {
                MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
                return;
            } else if (!isEdit && checkExistTemplate(flowTemplateName)) {
                MessageUtil.setErrorMessageFromRes("label.error.exist");
                return;
            } else if (flowTemplateName.replaceAll(" +", " ").trim().length() > MAX_LENGTH_TEMPLATE) {
                MessageUtil.setErrorMessageFromRes("label.error.maxlength.temp");
                return;
            } else if (flowTemplateType == null || flowTemplateType == -1) {
                MessageUtil.setErrorMessageFromRes("label.error.fill.flow.template.type");
                return;
            }

            if (!isEdit) {
                selectedFlowTemplate = new FlowTemplates();
                selectedFlowTemplate.setActionOfFlows(new ArrayList<ActionOfFlow>());
            } else {
                selectedFlowTemplate = new FlowTemplatesServiceImpl().findById(selectedFlowTemplate.getFlowTemplatesId());
                selectedFlowTemplate.setUpdateBy(userName);

            }
            selectedFlowTemplate.setCreateBy(userName);
            selectedFlowTemplate.setCreateDate(new Date());
            selectedFlowTemplate.setTemplateGroup(selectTemplateGroup);
            selectedFlowTemplate.setFlowTemplateName(flowTemplateName.replaceAll(" +", " ").trim());
            selectedFlowTemplate.setTemplateType(flowTemplateType);

            //20181119_tudn_start them danh sach lenh blacklist
            selectedFlowTemplate.setCrType(2);
            //20181119_tudn_end them danh sach lenh blacklist
            new FlowTemplatesServiceImpl().saveOrUpdate(selectedFlowTemplate);
            Map<String, String> orders = new LinkedHashMap<>();
            orders.put("flowTemplateName", "ASC");
            lstFlowTemplate = new FlowTemplatesServiceImpl().findList(null, orders);
            RequestContext.getCurrentInstance().execute("PF('dlgAddNewTemplate').hide()");
            MessageUtil.setInfoMessageFromRes("label.action.updateOk");
            //20190401_tudn_start them dau viec quy trinh cho GNOC
            clean();
            //20190401_tudn_end them dau viec quy trinh cho GNOC
            /*Ghi log tac dong nguoi dung
             */
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        (isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
                        selectedFlowTemplate.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            isEdit = false;
            flowTemplateName = "";
            actionOfFlowss = new LinkedList<>();
            selectedFlowTemplate = new FlowTemplates();
            //20181119_tudn_start them danh sach lenh blacklist
            //an hien cr type o ben giao dien
//            selectedFlowTemplate = new FlowTemplates();
            selectedFlowTemplate = null;
            //20181119_tudn_start them danh sach lenh blacklist
            selectTemplateGroup = new TemplateGroup();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
        }
    }

    public void onSelectCopyTemplate() {
        getFlowTemplateSelected();
        flowTemplateType = selectedFlowTemplate.getTemplateType();
        isEdit = false;
        copyFlowTemplateName = "";
    }

    public void prepareCopyTemplate() {
        try {
            actionOfFlowss.clear();

            // kiem tra xem ten template da ton tai trong csdl hay chua
            if (copyFlowTemplateName == null || copyFlowTemplateName.trim().isEmpty()) {
                MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
                return;
            } else if (checkExistTemplate(copyFlowTemplateName)) {
                MessageUtil.setErrorMessageFromRes("label.error.exist");
                return;
            }

            List<ActionOfFlow> lstActionFlow = selectedFlowTemplate.getActionOfFlows();
            List<ParamGroup> lstParamGroup = selectedFlowTemplate.getParamGroups();
            Integer crType = selectedFlowTemplate.getCrType();
            Long generateDtType = selectedFlowTemplate.getIsGenerateDT();
//			if (lstActionFlow != null) {
//				actionOfFlowss = new LinkedList<>();
//				rebuildMapFlowAction(lstActionFlow);
//			}

            selectedFlowTemplate = new FlowTemplates();
            selectedFlowTemplate.setFlowTemplateName(copyFlowTemplateName);
            selectedFlowTemplate.setCreateDate(new Date());
            selectedFlowTemplate.setCreateBy(userName);
            selectedFlowTemplate.setStatus(0);
            selectedFlowTemplate.setFlowTemplatesId(null);
            selectedFlowTemplate.setActionOfFlows(new ArrayList<ActionOfFlow>());
            selectedFlowTemplate.setParamGroups(null);
            selectedFlowTemplate.setFlowTemplates(null);
            selectedFlowTemplate.setFlowTemplateses(null);
            selectedFlowTemplate.setCrType(crType);
            // namlh clone hinh thuc sinh DT
            selectedFlowTemplate.setIsGenerateDT(generateDtType);
            //tudn_start fix sao cheo thong tin luong tac dong
            selectedFlowTemplate.setTemplateType(flowTemplateType);
            //tudn_end fix sao cheo thong tin luong tac dong

            saveCopyTempalte(lstActionFlow, lstParamGroup);

            RequestContext.getCurrentInstance().execute("PF('dlgCopyTemplate').hide()");

        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
            logger.error(e.getMessage(), e);
        }
    }

    public void saveCopyTempalte(List<ActionOfFlow> lstActionFlow, List<ParamGroup> lstParamGroup) {
        try {
            Date startTime = new Date();
            Long flowTemplateId = new FlowTemplatesServiceImpl().save(selectedFlowTemplate);

            selectedFlowTemplate = new FlowTemplatesServiceImpl().findById(flowTemplateId);

            List<ParamInOut> lstParamInOutClone = getLstParamInOut(lstActionFlow);

            // Luu danh sach action flow cua template
            if (!lstActionFlow.isEmpty()) {

                for (int i = 0; i < lstActionFlow.size(); i++) {
                    lstActionFlow.get(i).setFlowTemplates(selectedFlowTemplate);
                    lstActionFlow.get(i).setStepNum(null);
                }
                new ActionOfFlowServiceImpl().saveOrUpdate(lstActionFlow);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.UPDATE,
                            lstActionFlow.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
            }

            selectedFlowTemplate = new FlowTemplatesServiceImpl().findById(flowTemplateId);
            List<ActionOfFlow> lstActionFlowTmp = selectedFlowTemplate.getActionOfFlows();

            /*
             *  Luu danh sach param group default cua template
             */
            if (lstParamGroup != null && !lstParamGroup.isEmpty()) {
                ParamGroupId paramGroupId;
                for (int i = 0; i < lstParamGroup.size(); i++) {
                    paramGroupId = lstParamGroup.get(i).getId();
                    paramGroupId.setFlowTemplateId(flowTemplateId);
                    lstParamGroup.get(i).setFlowTemplates(selectedFlowTemplate);
                }
                new ParamGroupServiceImpl().saveOrUpdate(lstParamGroup);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.UPDATE,
                            lstParamGroup.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
            }

            /*
             * Luu danh sach param in out cua template
             */
            savedParamInOutClone(lstActionFlowTmp, lstParamInOutClone);

            /*
             * Build lai hien thi action flow cua template
             */
//				if (lstActionFlow != null) {
            actionOfFlowss = new LinkedList<>();
            rebuildMapFlowAction(lstActionFlowTmp);
//				}

            // add to list template selec item
            Map<String, String> orders = new LinkedHashMap<>();
            orders.put("flowTemplateName", "ASC");
            lstFlowTemplate = new FlowTemplatesServiceImpl().findList(null, orders);

				/*
				Ghi log tac dong nguoi dung
             	*/
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.CREATE,
                        selectedFlowTemplate.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            MessageUtil.setInfoMessageFromRes("label.action.updateOk");
        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
            logger.error(e.getMessage(), e);
        }

    }

    /*
     * Luu danh sach param in out cua template
     */
    private List<ParamInOut> savedParamInOutClone(List<ActionOfFlow> lstActionFlow, List<ParamInOut> lstParamInOut) {
        List<ParamInOut> lstParamInOutClone = new ArrayList<>();
        Date startTime = new Date();
        if (lstActionFlow != null
                && !lstActionFlow.isEmpty()
                && lstParamInOut != null
                && !lstParamInOut.isEmpty()) {
            try {
                Map<String, ActionOfFlow> mapActionFlow = new HashMap<>();
                for (ActionOfFlow actionFlow : lstActionFlow) {
                    mapActionFlow.put(actionFlow.getGroupActionOrder() + "_" + actionFlow.getStepNumberLabel(), actionFlow);
                }

                ParamInOutId paramId;
                ActionOfFlow actionFlowInput;
                ActionOfFlow actionFlowOutput;
                for (ParamInOut param : lstParamInOut) {
                    actionFlowInput = mapActionFlow.get(param.getActionOfFlowByActionFlowInId().getGroupActionOrder() + "_" + param.getActionOfFlowByActionFlowInId().getStepNumberLabel());
                    actionFlowOutput = mapActionFlow.get(param.getActionOfFlowByActionFlowOutId().getGroupActionOrder() + "_" + param.getActionOfFlowByActionFlowOutId().getStepNumberLabel());

                    paramId = param.getId();
                    paramId.setActionFlowInId(actionFlowInput.getStepNum());

                    param.setActionOfFlowByActionFlowInId(actionFlowInput);
                    param.setActionOfFlowByActionFlowOutId(actionFlowOutput);
                    lstParamInOutClone.add(param);
                }

                new ParamInOutServiceImpl().saveOrUpdate(lstParamInOut);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            (isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
                            lstParamInOut.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return lstParamInOutClone;
    }

    /*
     * Lay ra danh sach param in out cua template can clone
     */
    private List<ParamInOut> getLstParamInOut(List<ActionOfFlow> lstActionFlow) {
        List<ParamInOut> lstParamInOut = new ArrayList<>();
        int size = lstActionFlow.size();
        ActionOfFlow actionFlow;
        for (int i = size - 1; i >= 0; i--) {

            actionFlow = lstActionFlow.get(i);
            for (ActionDetail actionDetail : actionFlow.getAction().getActionDetails()) {

                for (ActionCommand actionCmd : actionDetail.getActionCommands()) {

                    ParamInOut paramInout;
                    for (ParamInput param : actionCmd.getCommandDetail().getParamInputs()) {

                        paramInout = getParamInOut(param.getParamInputId(),
                                actionCmd.getActionCommandId(),
                                actionFlow.getStepNum());
                        if (paramInout != null) {
                            lstParamInOut.add(paramInout);
                        }
                    }
                }
            }
        }

        return lstParamInOut;
    }

    public void prepareDelTemplate() {
        getFlowTemplateSelected();
    }

    public void deleteTemplate() {
        if (selectedFlowTemplate != null) {
            if (selectedFlowTemplate.getFlowRunActions() != null
                    && !selectedFlowTemplate.getFlowRunActions().isEmpty()) {
                MessageUtil.setErrorMessageFromRes("label.error.delete.template");
            } else {
                try {
                    Date startTime = new Date();
                    List<ActionOfFlow> actionOfFlows = selectedFlowTemplate.getActionOfFlows();
                    List<Long> stepNums = new ArrayList<Long>();
                    for (ActionOfFlow actionOfFlow : actionOfFlows) {
                        stepNums.add(actionOfFlow.getStepNum());
                    }
                    Object[] objs = new ParamGroupServiceImpl().openTransaction();

                    Session session = (Session) objs[0];
                    Transaction tx = (Transaction) objs[1];
                    new ParamGroupServiceImpl().execteBulk2("delete from ParamGroup where id.flowTemplateId = ?",
                            session, tx, false, selectedFlowTemplate.getFlowTemplatesId());
                    for (Long stepNum : stepNums) {
                        new ParamInOutServiceImpl().execteBulk2("delete from ParamInOut where actionOfFlowByActionFlowOutId.stepNum = ? or id.actionFlowInId = ?",
                                session, tx, false, stepNum, stepNum);

                    }
                    new FlowTemplatesServiceImpl().delete(selectedFlowTemplate, session, tx, true);
                    Map<String, String> orders = new LinkedHashMap<>();
                    orders.put("flowTemplateName", "ASC");
                    lstFlowTemplate = new FlowTemplatesServiceImpl().findList(null, orders);
                    actionOfFlowss = new LinkedList<>();
                    mapGroupName = new LinkedHashMap<>();

					/*
					Ghi log tac dong nguoi dung
             		*/
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.DELETE,
                                selectedFlowTemplate.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                    MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
                } catch (Exception e) {
                    MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
                    logger.error(e.getMessage(), e);
                }
            }
            clean();
            //20181119_tudn_start them danh sach lenh blacklist
            selectedFlowTemplate = null;
            //20181119_tudn_end them danh sach lenh blacklist
        } else {
            MessageUtil.setErrorMessageFromRes("message.choose.delete");
        }
    }

    private boolean checkExistTemplate(String templateName) {
        boolean check = false;
        if (templateName != null && !templateName.trim().isEmpty()) {
            try {
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("flowTemplateName", templateName);

                List<FlowTemplates> lstTemplate = new FlowTemplatesServiceImpl().findListExac(filters, null);
                if (lstTemplate != null && !lstTemplate.isEmpty()) {
                    check = true;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return check;
    }

    private void getFlowTemplateSelected() {
        Map<String, String> params =
                FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String templateId = params.get("templateId");
        if (templateId == null)
            return;
        for (FlowTemplates flowTemplates : lstFlowTemplate) {
            if (flowTemplates.getFlowTemplatesId().toString().equalsIgnoreCase(templateId)) {
                selectedFlowTemplate = flowTemplates;
                break;
            }
        }
    }

    public void deleteActionOfFlow(ActionOfFlow actionOfFlow, Integer idx) {
        try {
            if (actionOfFlow != null) {

                if (actionOfFlow.getStepNum() != null) {
                    // Kiem tra xem action flow da duoc sinh mop hay chua
					/*if (actionOfFlow.getNodeRunGroupActions() != null
							&& !actionOfFlow.getNodeRunGroupActions().isEmpty()) {
						MessageUtil.setErrorMessageFromRes("label.action.created.mop");
						return;

					} else*/
                    if (actionOfFlow.getActionFlowIds() != null && !actionOfFlow.getActionFlowIds().isEmpty()) {
                        for (Long id : actionOfFlow.getActionFlowIds()) {
                            try {
                                ActionOfFlow action = new ActionOfFlowServiceImpl().findById(id);
                                if (action != null) {
                                    lstActionFlowDel.add(action);
                                }
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        }
//						lstActionFlowDel.add(actionOfFlow);
                    }
                }

                Integer indexOfListActionOfFlow = mapGroupName.get(actionOfFlow.getGroupActionName());
                List<ActionOfFlow> actionOfFlows = actionOfFlowss.get(indexOfListActionOfFlow);
                actionOfFlows.remove(idx.intValue());

                // sap xep lai danh sach order
                for (int i = 0; i < actionOfFlows.size(); i++) {
                    actionOfFlows.get(i).setStepNumberLabel(Long.valueOf(i + 1));
                }

                MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
            }
        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
            logger.error(e.getMessage(), e);
        }
    }

    public void onCellEdit(CellEditEvent event) {

    }

    public void saveActionOfFlow() {
        Session session = null;
        Transaction tx = null;
        Date startTime = new Date();
        try {
            if (selectedFlowTemplate == null || selectedFlowTemplate.getFlowTemplatesId() == null) {
                MessageUtil.setInfoMessageFromRes("label.error.templateEmpty");
                return;
            } else if (!validateDataBeforeSave()) {
                return;
            }
            //20181119_tudn_start them danh sach lenh blacklist
            else {
//                if (selectedProcedureId == null || "-1".equals(selectedProcedureId)) {
//                    MessageUtil.setErrorMessageFromRes("label.error.fill.cr.type");
//                    return;
//                }
                try {
                    new FlowTemplatesServiceImpl().execteBulk("update FlowTemplates set status = ?, isGenerateDT = ?  where flowTemplatesId = ? ",
                            selectedFlowTemplate.getStatus() != null ? selectedFlowTemplate.getStatus() : 0,
                            selectedFlowTemplate.isGenerationDT() ? 1L : 0L,
                            selectedFlowTemplate.getFlowTemplatesId());

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    MessageUtil.setErrorMessageFromRes("label.action.updateFail");
                    return;
                }
            }
            //20181119_tudn_end them danh sach lenh blacklist

            try {
                // Luu lai danh sach action of flow moi
                List<ActionOfFlow> lstActionSaveId = new ArrayList<>();
                List<ActionOfFlow> lstActionSaveNoId = new ArrayList<>();
                List<ActionOfFlow> lstActionAll = new ArrayList<>();
                List<ActionOfFlow> lstSubAction = new ArrayList<ActionOfFlow>();

                for (List<ActionOfFlow> actions : actionOfFlowss) {
                    for (ActionOfFlow a : actions) {

                        lstSubAction = buildActionFlows(a, lstActionFlowDel);
                        if (lstSubAction != null && !lstSubAction.isEmpty()) {
                            for (ActionOfFlow action : lstSubAction) {
                                lstActionAll.add(action);
                                if (action.getStepNum() == null) {
                                    lstActionSaveNoId.add(action);
                                } else {
                                    lstActionSaveId.add(action);
                                }
                            }
                        }
                    }
                }

                /*
                 * Kiem tra cac dieu kien logic truoc khi thuc hien luu du lieu
                 */
                if (!validateIfCondition(lstActionAll)) {
                    return;

                } else {

                    Object[] objs = new ActionOfFlowServiceImpl().openTransaction();
                    session = (Session) objs[0];
                    tx = (Transaction) objs[1];

                    if (!lstActionFlowDel.isEmpty()) {
                        // Xoa cac param in out tham chieu den cac action xoa
                        for (ActionOfFlow actionFlow : lstActionFlowDel) {
                            new ParamInOutServiceImpl().execteBulk2("delete from ParamInOut where actionOfFlowByActionFlowOutId.stepNum = ? or id.actionFlowInId = ?",
                                    session, tx, false, actionFlow.getStepNum(), actionFlow.getStepNum());
                        }
                        new ActionOfFlowServiceImpl().delete(lstActionFlowDel, session, tx, false);
                        //20180620_tudn_start ghi log DB
                        try {
                            LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                    LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                                    LogUtils.ActionType.DELETE,
                                    lstActionFlowDel.toString(), LogUtils.getRequestSessionId());
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        //20180620_tudn_end ghi log DB
                    }

                    if (lstActionFlowDel != null && !lstActionFlowDel.isEmpty()) {

                        for (Iterator<ActionOfFlow> iterator = lstActionFlowDel.iterator(); iterator.hasNext(); ) {
                            ActionOfFlow action = iterator.next();
                            if (action.getStepNum() == null) {
                                iterator.remove();
                            }
                        }
                    }

                    new ActionOfFlowServiceImpl().saveOrUpdate(lstActionSaveId, session, tx, false);
                    new ActionOfFlowServiceImpl().saveOrUpdate(lstActionSaveNoId, session, tx, false);

                    tx.commit();
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.CREATE,
                                lstActionSaveId.toString(), LogUtils.getRequestSessionId());
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.CREATE,
                                lstActionSaveNoId.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                }

            } catch (Exception e) {
                if (tx != null)
                    if (tx.getStatus() != TransactionStatus.ROLLED_BACK)
                        tx.rollback();
                logger.error(e.getMessage(), e);
            } finally {
                if (session != null)
                    try {
                        session.close();
                    } catch (Exception e2) {
                        logger.error(e2.getMessage(), e2);
                    }
            }

            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            orders.put("flowTemplateName", "ASC");
            lstFlowTemplate = new FlowTemplatesServiceImpl().findList(null, orders);
            selectedFlowTemplate = new FlowTemplatesServiceImpl().findById(selectedFlowTemplate.getFlowTemplatesId());

            actionOfFlowss = new LinkedList<>();
            rebuildMapFlowAction(selectedFlowTemplate.getActionOfFlows());

            MessageUtil.setInfoMessageFromRes("label.action.updateOk");
            lstActionFlowDel.clear();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
        }

        lstItemGroupName.clear();
        for (Map.Entry<String, Integer> entry : mapGroupName.entrySet()) {
            lstItemGroupName.add(new SelectItem(entry.getKey(), entry.getKey()));
        }
    }

    private List<ActionOfFlow> buildActionFlows(ActionOfFlow actionFlow, List<ActionOfFlow> lstActionDel) {
        List<ActionOfFlow> lstActionFlowSave = new ArrayList<>();
        if (actionFlow != null) {
            try {

                ActionOfFlow newActionFlow;
                List<String> lstPreConditionVal = Arrays.asList(actionFlow.getPreStepsCondition().trim().split(","));

                int index = 0;
                for (String preStep : actionFlow.getPreStepsNumber()) {

                    newActionFlow = new ActionOfFlow();
                    newActionFlow.setAction(actionFlow.getAction());
                    if (actionFlow.getDelayTime() != null
                            && actionFlow.getDelayTime() != 0) {
                        newActionFlow.setDelayTime(actionFlow.getDelayTime());
                    }
                    newActionFlow.setFlowTemplates(actionFlow.getFlowTemplates());
                    newActionFlow.setGroupActionName(actionFlow.getGroupActionName());
                    newActionFlow.setGroupActionOrder(actionFlow.getGroupActionOrder());
                    newActionFlow.setIndexParamValue(actionFlow.getIndexParamValue());
                    newActionFlow.setIsRollback(actionFlow.isRollbackStatus() ? Config.ROLLBACK_ACTION : Config.EXECUTE_ACTION);
                    newActionFlow.setStepNumberLabel(actionFlow.getStepNumberLabel());

                    newActionFlow.setStepNum(null);
                    newActionFlow.setIfValue(lstPreConditionVal.get(index));
                    newActionFlow.setPreviousStep(Long.valueOf(preStep));

                    lstActionFlowSave.add(newActionFlow);
                    index++;
                }


                if (actionFlow.getActionFlowIds() != null && !actionFlow.getActionFlowIds().isEmpty()) {

                    int maxSize = actionFlow.getActionFlowIds().size();
                    if (actionFlow.getActionFlowIds().size() > actionFlow.getPreStepsNumber().size()) {

                        for (int i = actionFlow.getPreStepsNumber().size(); i < actionFlow.getActionFlowIds().size(); i++) {
                            lstActionDel.add(new ActionOfFlowServiceImpl().findById(actionFlow.getActionFlowIds().get(i)));
                        }
                        maxSize = actionFlow.getPreStepsNumber().size();
                    }

                    for (int i = 0; i < maxSize; i++) {
                        lstActionFlowSave.get(i).setStepNum(actionFlow.getActionFlowIds().get(i));
                        lstActionFlowSave.get(i).setNodeRunGroupActions(actionFlow.getLstNodeRunGroupAction().get(i));
                    }
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return lstActionFlowSave;
    }

    private boolean validateIfCondition(List<ActionOfFlow> lstActionOfFlow) {
        boolean check = true;
        if (actionOfFlowss != null) {

            // kiem tra xem da dien du lieu day du hay chua
            for (ActionOfFlow action : lstActionOfFlow) {
                if (action.getStepNumberLabel() == 1) {
                    if (action.getIsRollback().equals(Config.ROLLBACK_ACTION)) {
                        MessageUtil.setErrorMessageFromRes("label.err.first.action.condition");
                        return false;
                    } else if (action.getPreviousStep().intValue() != 0) {
                        MessageUtil.setErrorMessageFromRes("label.val.first.action.prestep");
                        return false;
                    }
                }
            }

            // kiem tra 2 cap tham so previous step va dieu kien co trung nhau
            // hay khong
            // Kiem tra xem 1 action co vuot qua 2 action tham chieu den hay
            // khong
            int size = lstActionOfFlow.size();
            for (int i = 0; i < size; i++) {
                int countReference = 0; // bien dem so lan tham chieu den action

                for (int j = 0; j < size; j++) {
                    if (i != j) {
                        if (lstActionOfFlow
                                .get(i)
                                .getGroupActionOrder()
                                .equals(lstActionOfFlow.get(j)
                                        .getGroupActionOrder())
                                && lstActionOfFlow
                                .get(i)
                                .getPreviousStep()
                                .equals(lstActionOfFlow.get(j)
                                        .getPreviousStep())
                                && lstActionOfFlow
                                .get(i)
                                .getIfValue()
                                .equals(lstActionOfFlow.get(j)
                                        .getIfValue())) {
                            MessageUtil.setErrorMessageFromRes("label.validate.prestep.condition.val");
                            return false;
                        }
                        if (lstActionOfFlow
                                .get(i)
                                .getGroupActionOrder()
                                .equals(lstActionOfFlow.get(j)
                                        .getGroupActionOrder())
                                && lstActionOfFlow
                                .get(j)
                                .getPreviousStep()
                                .toString()
                                .equals(lstActionOfFlow.get(i)
                                        .getStepNumberLabel())) {
                            countReference++;
                            if (countReference > 2) {
                                MessageUtil
                                        .setErrorMessageFromRes("message.err.max.action.reference");
                                return false;
                            }
                        }
                    }
                } // end loop for
            }

            // kiem tra dieu kien lenh thuc thi theo sau lenh rollback
            for (int i = 0; i < size; i++) {
                if (lstActionOfFlow.get(i).getIsRollback()
                        .equals(Config.ROLLBACK_ACTION)) {

                    for (int j = 0; j < size; j++) {
                        if (i != j
                                && lstActionOfFlow
                                .get(i)
                                .getGroupActionOrder()
                                .equals(lstActionOfFlow.get(j)
                                        .getGroupActionOrder())
                                && lstActionOfFlow
                                .get(j)
                                .getPreviousStep()
                                .equals(lstActionOfFlow.get(i)
                                        .getStepNumberLabel())
                                && lstActionOfFlow.get(j).getIsRollback()
                                .equals(Config.EXECUTE_ACTION)) {
                            MessageUtil
                                    .setErrorMessageFromRes("label.val.action.exec.after.rollback");
                            return false;
                        }
                    }
                }
            }

            // Kiem tra dieu kien 1 action khong co qua 2 dieu kien re nhanh
//			for (List<ActionOfFlow> actionOfFlows : actionOfFlowss) {
//				for (int i = 0; i < size - 1; i++) {
//					ActionOfFlow actionOfFlow = lstActionOfFlow.get(i);
//
//					if(actionOfFlow.getGroupActionOrder().equals(lstActionOfFlow.get(i+1).getGroupActionOrder())
//							&& actionOfFlow.getIsRollback().equals(Config.ROLLBACK_ACTION) && lstActionOfFlow.get(i+1).getIsRollback().equals(Config.ROLLBACK_ACTION)){
//						MessageUtil.setErrorMessageFromRes("error.two.action.consecutive");
//						return false;
//					}
//				}
//			}
        }
        return check;
    }

    private boolean validateDataBeforeSave() {
        boolean check = true;
        try {
            if (actionOfFlowss != null) {
                for (List<ActionOfFlow> actions : actionOfFlowss) {
                    if (check) {
                        for (ActionOfFlow action : actions) {
                            if (action.getPreStepsNumber() == null
                                    || action.getPreStepsNumber().isEmpty()
                                    || action.getPreStepsCondition() == null
                                    || action.getPreStepsCondition().isEmpty()) {
                                MessageUtil.setErrorMessageFromRes("label.validate.add.action_flow");
                                check = false;
                                break;
                            } else {
                                String pattern = "^(-1|0|1)$";
                                Pattern p = Pattern.compile(pattern);
                                Matcher m;
                                List<String> lstConditionVal = Arrays.asList(action.getPreStepsCondition().trim().split(","));
                                if (lstConditionVal != null && !lstConditionVal.isEmpty()) {
                                    for (String val : lstConditionVal) {
                                        m = p.matcher(val);
                                        if (!m.matches()) {
                                            MessageUtil.setErrorMessageFromRes("label.condition.action.tip");
                                            check = false;
                                            break;
                                        }
                                    }

                                    if (check) {
                                        // kiem tra xem do dai cua previous step va conditions co bang nhau hay khong
                                        if (action.getPreStepsNumber().size() != lstConditionVal.size()) {
                                            //tudn_start fix cau message
//                                            MessageUtil.setErrorMessageFromRes("message.err.condition.not.equal.prestep");
                                            MessageUtil.setErrorMessageFromRes("message.err.condition.prestep");
                                            check = false;
                                            //tudn_end fix cau message
                                        }
                                    }
                                }
                            }
                        }
                    } else {

                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return check;
    }

    public static void main(String[] args) {
        try {
            String pattern = "^[01]$";
            Pattern p = Pattern.compile(pattern);
            Matcher m = p.matcher("1,");
            boolean b = m.matches();
            System.out.println(b);
            System.out.println(PasswordEncoder.encrypt("qL7n1234"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void clean() {
        flowTemplateName = null;
        copyFlowTemplateName = null;
        isEdit = false;
        lstActionFlowDel = new ArrayList<>();
        lstItemGroupName = new ArrayList<>();
//		mapActionOfFlow = new LinkedHashMap<>();
        actionOfFlowss = new LinkedList<>();
        mapGroupName = new LinkedHashMap<>();
        lstParamGroup = new ArrayList<>();
        selectedParamGroup = new ParamGroup();

        //20181119_tudn_start them danh sach lenh blacklist
//        selectedFlowTemplate = null;
        selectedProcedureId = 0L;
        //20181119_tudn_end them danh sach lenh blacklist

        //20190401_tudn_start them dau viec quy trinh cho GNOC
        selectedProcedureWFId = 0L;
        //20190401_tudn_end them dau viec quy trinh cho GNOC

        //20190729_tudn_start sua dau viec quy trinh cho GNOC
        selectedProcedureName = selectedProcedureNameTemp;
        selectedProcedureNameEn = selectedProcedureNameEnTemp;
        keyProcedureSearch = "";
        selectedProcedureWFName = selectedProcedureWFNameTemp;
        selectedProcedureWFNameEn = selectedProcedureWFNameEnTemp;
        //20190729_tudn_end sua dau viec quy trinh cho GNOC

        /*20190408_chuongtq start check param when create MOP*/
        lstParamCondition = new ArrayList<>();
        /*20190408_chuongtq end check param when create MOP*/
    }

    public void cleanNotShowCrType() {
        flowTemplateName = null;
        copyFlowTemplateName = null;
        isEdit = false;
        lstActionFlowDel = new ArrayList<>();
        lstItemGroupName = new ArrayList<>();
//		mapActionOfFlow = new LinkedHashMap<>();
        actionOfFlowss = new LinkedList<>();
        mapGroupName = new LinkedHashMap<>();
        lstParamGroup = new ArrayList<>();
        selectedParamGroup = new ParamGroup();

        //20181119_tudn_start them danh sach lenh blacklist
        selectedFlowTemplate = null;
        selectedProcedureId = 0L;
        //20181119_tudn_end them danh sach lenh blacklist

        //20190401_tudn_start them dau viec quy trinh cho GNOC
        selectedProcedureWFId = 0L;
        //20190401_tudn_end them dau viec quy trinh cho GNOC
    }

    public void preAddTemplate() {
        selectedFlowTemplate = new FlowTemplates();
        selectTemplateGroup = new TemplateGroup();
        flowTemplateName = null;
        copyFlowTemplateName = null;
        isEdit = false;
        //20190401_tudn_start them dau viec quy trinh cho GNOC
        clean();
        //20190401_tudn_end them dau viec quy trinh cho GNOC
    }

    public List<SelectItem> buidLstPreStepItem(ActionOfFlow cmd) {
        List<SelectItem> lstItem = new ArrayList<>();
        Integer indexOfGroupAction = mapGroupName.get(cmd.getGroupActionName());
        if (indexOfGroupAction != null) {
            List<ActionOfFlow> lstActionFlow = actionOfFlowss.get(indexOfGroupAction);
            if (lstActionFlow != null) {
                for (int i = 0; i <= lstActionFlow.size(); i++) {
                    if (i >= cmd.getStepNumberLabel())
                        continue;
                    lstItem.add(new SelectItem(i, i + ""));
                }
                try {
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return lstItem;
    }

    public void buildParamGroup() {
        lstParamGroup = new ArrayList<>();
        isChangeGroupCode = false;
        Map<String, String> mapParamGroupKey = new HashMap<>();
        if (selectedFlowTemplate != null) {
            try {
                List<Action> lstAction = new ArrayList<>();
                for (ActionOfFlow actionFlow : selectedFlowTemplate.getActionOfFlows()) {
                    lstAction.add(actionFlow.getAction());
                }

                if (!lstAction.isEmpty()) {
                    List<ActionDetail> lstActionDetail = new ArrayList<>();
                    for (Action action : lstAction) {
                        lstActionDetail.addAll(action.getActionDetails());
                    }

                    if (!lstActionDetail.isEmpty()) {
                        List<ActionCommand> lstActionCommand = new ArrayList<>();
                        for (ActionDetail actionDetail : lstActionDetail) {
                            lstActionCommand.addAll(actionDetail.getActionCommands());
                        }

                        if (!lstActionCommand.isEmpty()) {
                            List<ParamInput> lstParamInput = new ArrayList<>();
                            for (ActionCommand actionCmd : lstActionCommand) {
                                lstParamInput.addAll(actionCmd.getCommandDetail().getParamInputs());
                            }

                            if (!lstParamInput.isEmpty()) {
                                ParamGroup paramGroup;
                                ParamGroupId paramGroupId;
                                String key;

                                for (ParamInput paramInput : lstParamInput) {
                                    key = paramInput.getParamInputId() + "_" + selectedFlowTemplate.getFlowTemplatesId();

                                    if (mapParamGroupKey.get(key) == null) {
                                        mapParamGroupKey.put(key, key);
                                        paramGroupId = new ParamGroupId(paramInput.getParamInputId(), selectedFlowTemplate.getFlowTemplatesId());
                                        paramGroup = new ParamGroupServiceImpl().findById(paramGroupId);

                                        if (paramGroup != null) {
                                            lstParamGroup.add(paramGroup);
                                        } else {
                                            lstParamGroup.add(new ParamGroup(paramGroupId, paramInput, selectedFlowTemplate));
                                        }
                                    }
                                } // end loop for param input
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            if (lstParamGroup != null && !lstParamGroup.isEmpty()) {
                RequestContext.getCurrentInstance().execute("PF('dlgParamDefault').show()");
            } else {
                MessageUtil.setErrorMessageFromRes("datatable.empty");
            }
        }
    }

    public List<Node> autoNodeMethod(String nodeIp) {
        List<Node> lstNode = new ArrayList<>();
        Map<String, Object> filters = new HashMap<>();
        filters.put("active", Constant.status.active);
        if (nodeIp != null && !nodeIp.trim().isEmpty()) {
            try {
                filters.put("nodeIp", nodeIp);
                lstNode = new NodeServiceImpl().findList(0, 20, filters);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            try {
                lstNode = new NodeServiceImpl().findList(0, 20, filters);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        return lstNode;
    }

    public void buildParamInOutData() {
        lstParamInOutObject = new ArrayList<>();
        if (selectedFlowTemplate != null) {
            try {
                ActionOfFlow actionFlow;
                int size = selectedFlowTemplate.getActionOfFlows().size();
                List<ActionOfFlow> lstActionFlow = selectedFlowTemplate.getActionOfFlows();
                for (int i = size - 1; i >= 0; i--) {

                    actionFlow = lstActionFlow.get(i);
                    for (ActionDetail actionDetail : actionFlow.getAction().getActionDetails()) {

                        for (ActionCommand actionCmd : actionDetail.getActionCommands()) {
							/*if (actionCmd.getCommandDetail().getCommandType().intValue() == 0)
								continue;*/

                            ParamInOut paramInout;
                            for (ParamInput param : actionCmd.getCommandDetail().getParamInputs()) {

                                paramInout = getParamInOut(param.getParamInputId(),
                                        actionCmd.getActionCommandId(),
                                        actionFlow.getStepNum());
                                if (paramInout == null) {
                                    paramInout = new ParamInOut();
                                    paramInout.setActionCommandByActionCommandInputId(actionCmd);
                                    paramInout.setActionOfFlowByActionFlowInId(actionFlow);
                                    paramInout.setParamInput(param);
                                }

                                paramInout.setParamInOutOrder(lstParamInOutObject.size());
                                lstParamInOutObject.add(paramInout);
                            }
                        }
                    } // end loop for command detail
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }

        if (lstParamInOutObject != null && !lstParamInOutObject.isEmpty()) {
            RequestContext.getCurrentInstance().execute("PF('dlgParamReference').show()");
        } else {
            MessageUtil.setErrorMessageFromRes("datatable.empty");
        }
    }

    private ParamInOut getParamInOut(Long paramInputId, Long actionCmdInId, Long actionFlowInId) {
        ParamInOut paramInOut = null;
        ParamInOutId id = new ParamInOutId(actionCmdInId, paramInputId, actionFlowInId);

        try {
            paramInOut = new ParamInOutServiceImpl().findById(id);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            paramInOut = null;
        }
        return paramInOut;
    }

    public void buildLstActionSelect(ParamInOut param) {
        try {
            selectedParamInOut = param;
            lstActionFlow = new ArrayList<>();
            lstActionCommand = new ArrayList<>();
            try {
                if (param != null && param.getNodeId() != null)
                    selectedNode = new NodeServiceImpl().findById(param.getNodeId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            if (param != null) {
                Map<String, Integer> filters = new HashMap<>();
                for (ActionOfFlow action : selectedFlowTemplate.getActionOfFlows()) {

                    if (action.getGroupActionOrder() <= param.getActionOfFlowByActionFlowInId().getGroupActionOrder()
                            && filters.get(action.getStepNumberLabel() + "#" + action.getGroupActionOrder()) == null) {

                        filters.put(action.getStepNumberLabel() + "#" + action.getGroupActionOrder(), 1);
                        lstActionFlow.add(action);
                    } else if (action.getGroupActionOrder() == param.getActionOfFlowByActionFlowInId().getGroupActionOrder()
                            && action.getStepNumberLabel() < param.getActionOfFlowByActionFlowInId().getStepNumberLabel()) {
                        lstActionFlow.add(action);
                    }
                }
            }

            if (lstActionFlow != null && !lstActionFlow.isEmpty()) {
                RequestContext.getCurrentInstance().execute("PF('dlgAddParamRefer').show()");
            } else {
                MessageUtil.setErrorMessageFromRes("datatable.empty");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void buildLstActionCmdSelect() {
        lstActionCommand = new ArrayList<>();
        if (selectedActionFlow != null) {
            try {

                if (selectedActionFlow.getGroupActionOrder().intValue() <= selectedParamInOut.getActionOfFlowByActionFlowInId().getGroupActionOrder().intValue()) {

                    // lay ra command detail selected cua param
                    List<ActionDetail> lstActionDetail = selectedActionFlow.getAction().getActionDetails();
                    if (lstActionDetail != null && !lstActionDetail.isEmpty()) {
                        for (ActionDetail detail : lstActionDetail) {
//							if (detail.getVendor().equals(selectedParamInOut.getActionCommandByActionCommandInputId().getActionDetail().getVendor())
//									&& detail.getVersion().equals(selectedParamInOut.getActionCommandByActionCommandInputId().getActionDetail().getVersion())) {

                            if (selectedActionFlow.getStepNumberLabel().intValue() < selectedParamInOut.getActionOfFlowByActionFlowInId().getStepNumberLabel().intValue()) {
                                lstActionCommand.addAll(detail.getActionCommands());

                            } else if (selectedActionFlow.getStepNumberLabel() == selectedParamInOut.getActionOfFlowByActionFlowInId().getStepNumberLabel()) {
                                for (ActionCommand action : detail.getActionCommands()) {
                                    // 1. khong them cau lenh neu trung voi cau lenh can xet tham chieu
                                    // 2. khong them cac lenh phia sau cau lenh can tham chieu
                                    if (!action.getCommandDetail().getCommandDetailId().equals(selectedParamInOut.getParamInput().getCommandDetail().getCommandDetailId())
                                            && action.getOrderRun().intValue() < selectedParamInOut.getActionCommandByActionCommandInputId().getOrderRun().intValue()) {
                                        lstActionCommand.add(action);
                                    }
                                }
                            }
//								break;
//							}
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void closeDlgCmdReference() {
        lstActionCommand = new ArrayList<>();
        selectedParamInOut = null;
        selectedActionFlow = null;
    }

    public void saveSelectedCmdReference() {
        if (selectedActionCmd != null && selectedActionFlow != null) {
            Date startTime = new Date();
            try {
                selectedParamInOut.setActionCommandByActionCommandOutputId(selectedActionCmd);
                selectedParamInOut.setActionOfFlowByActionFlowOutId(selectedActionFlow);

                ParamInOutId paramInOutId = new ParamInOutId();
                paramInOutId.setActionCommandInputId(selectedParamInOut.getActionCommandByActionCommandInputId().getActionCommandId());
                paramInOutId.setActionFlowInId(selectedParamInOut.getActionOfFlowByActionFlowInId().getStepNum());
                paramInOutId.setParamInputId(selectedParamInOut.getParamInput().getParamInputId());
                selectedParamInOut.setId(paramInOutId);
                if (selectedNode != null) {
                    logger.info(">>>>>>>>>>>>>>> " + selectedNode.getNodeIp());
                    selectedParamInOut.setNodeId(selectedNode.getNodeId());
                }

                new ParamInOutServiceImpl().saveOrUpdate(selectedParamInOut);
                lstParamInOutObject.remove(selectedParamInOut.getParamInOutOrder().intValue());
                lstParamInOutObject.add(selectedParamInOut.getParamInOutOrder(), selectedParamInOut);

                selectedActionFlow = null;
                selectedParamInOut = null;
                selectedNode = null;

                /*
                Ghi log tac dong nguoi dung
                */
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.CREATE,
                            selectedParamInOut.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_start ghi log DB

                selectedActionFlow = null;
                selectedParamInOut = null;
                selectedNode = null;

                MessageUtil.setInfoMessageFromRes("label.action.updateOk");
                RequestContext.getCurrentInstance().execute("PF('dlgAddParamRefer').hide()");
            } catch (Exception e) {
                MessageUtil.setErrorMessageFromRes("label.action.updateFail");
                logger.error(e.getMessage(), e);
            }

        } else {
            MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
        }
    }

    public void prepareDelCmdRef(ParamInOut param) {
        if (param != null) {
            selectedParamInOut = param;
        }
    }

    public void delCmdReference() {
        if (selectedParamInOut != null) {
            Date startTime = new Date();
            try {
                new ParamInOutServiceImpl().delete(selectedParamInOut);
                selectedParamInOut.setId(null);
                selectedParamInOut.setActionOfFlowByActionFlowOutId(null);
                selectedParamInOut.setActionCommandByActionCommandOutputId(null);
                lstParamInOutObject.remove(selectedParamInOut.getParamInOutOrder());
                lstParamInOutObject.add(selectedParamInOut.getParamInOutOrder(), selectedParamInOut);
                /*
                Ghi log tac dong nguoi dung
                */
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.DELETE,
                            selectedParamInOut.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_start ghi log DB
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    public void onUpdateParamDefault() {

        int countParamNameEqual = 0;
        for (int i = 0; i < lstParamGroup.size(); i++) {
            if (lstParamGroup.get(i).getParamInput().getParamCode().trim()
                    .equalsIgnoreCase(selectedParamGroup.getParamInput().getParamCode().trim())) {
                lstParamGroup.get(i).setParamDefault(selectedParamGroup.getParamDefault());
                countParamNameEqual++;
            }
        }

        if (countParamNameEqual > 1) {
            RequestContext.getCurrentInstance().update("form:paramGroupTable");
        }

//		if (selectedParamGroup == null) {
//			System.out.println("<><><><><><><><> not change value");
//		} else {
//			if (selectedParamGroup.getGroupCode() != null
//					&& selectedParamGroup.getGroupCode() != -1) {
//				if (isChangeGroupCode) {
//
//					// lay ra param group dau tien co group code trung voi selectedParamGroup
//					ParamGroup firstParamGroup = null;
//					for (int i = 0; i < lstParamGroup.size(); i++) {
//						if (lstParamGroup.get(i).equals(selectedParamGroup)) {
//							if (firstParamGroup != null) {
//								lstParamGroup.get(i).setParamDefault(firstParamGroup.getParamDefault());
//							} else {
//								for (int j = 0; j < lstParamGroup.size(); j++) {
//									if (lstParamGroup.get(j).getGroupCode() == selectedParamGroup.getGroupCode())
//										lstParamGroup.get(j).setParamDefault(selectedParamGroup.getParamDefault());
//								}
//								// thoat khoi vong for i
//								break;
//							}
//						} else if ((lstParamGroup.get(i).getGroupCode() == selectedParamGroup.getGroupCode())
//								&& (firstParamGroup == null)) {
//							firstParamGroup = lstParamGroup.get(i);
//						}
//					}
//
//				} else {
//					for (int i = 0; i < lstParamGroup.size(); i++) {
//						if (lstParamGroup.get(i).getGroupCode() == selectedParamGroup.getGroupCode()) {
//							lstParamGroup.get(i).setParamDefault(selectedParamGroup.getParamDefault());
//						}
//					}
//				}
//			}
//
//			RequestContext.getCurrentInstance().update("form:paramGroupTable");
//		}

    }

    public String getActionGroupNameData(int index) {
        if (mapGroupName != null) {
            for (Map.Entry<String, Integer> entry : mapGroupName.entrySet()) {
                if (entry.getValue() == index) {
                    return entry.getKey();
                }
            }
        }
        return "";
    }

    public void onCellEditParam(CellEditEvent event) {

        DataTable data = (DataTable) event.getSource();
        ParamGroup paramGroup = (ParamGroup) data.getRowData();
        selectedParamGroup = paramGroup;

//		String columnName = event.getColumn().getHeaderText();
//		if (columnName.trim().equalsIgnoreCase(MessageUtil.getResourceBundleMessage("label.paramDefault"))) {
//
//			String oldValue = (String) event.getOldValue();
//			String newValue = (String) event.getNewValue();
//
//			if (newValue != null) {
//				if (oldValue != null && oldValue.equals(newValue)) {
//					selectedParamGroup = null;
//
//				} else {
//					DataTable data = (DataTable) event.getSource();
//					ParamGroup paramGroup = (ParamGroup) data.getRowData();
//					selectedParamGroup = paramGroup;
//				}
//			}
//			isChangeGroupCode = false;
//
//		} else if (columnName.trim().equalsIgnoreCase(MessageUtil.getResourceBundleMessage("label.group.code"))) {
//			Long oldValue = (Long) event.getOldValue();
//			Long newValue = (Long) event.getNewValue();
//
//			if (newValue != null) {
//				if (oldValue != null && oldValue.equals(newValue)) {
//					selectedParamGroup = null;
//
//				} else {
//					DataTable data = (DataTable) event.getSource();
//					ParamGroup paramGroup = (ParamGroup) data.getRowData();
//					selectedParamGroup = paramGroup;
//				}
//			}
//
//			isChangeGroupCode = true;
//		} else {
//			isChangeGroupCode = false;
//			System.out.println(">>>>>>>> " + columnName);
//		}

    }

    public void saveParamGroupDefault() {
        try {
            /*
             * Xoa toan bo cac param default truoc khi thuc hien cap nhat
             */
            Date startTime = new Date();
            List<ParamGroup> lstParamDel = selectedFlowTemplate.getParamGroups();
            if (lstParamDel != null && !lstParamDel.isEmpty()) {
                new ParamGroupServiceImpl().delete(lstParamDel);
            }

            List<ParamGroup> lstParamGroupSave = new ArrayList<>();
            for (ParamGroup param : lstParamGroup) {
                if ((param.getParamDefault() != null && !"".equals(param.getParamDefault().trim()))) {
                    lstParamGroupSave.add(param);
                }
            }

            if (!lstParamGroupSave.isEmpty()) {
                new ParamGroupServiceImpl().saveOrUpdate(lstParamGroupSave);
            }
            //20180620_tudn_start ghi log DB
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.UPDATE,
                        lstParamGroupSave.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            //20180620_tudn_end ghi log DB
            MessageUtil.setInfoMessageFromRes("label.action.updateOk");
        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
            logger.error(e.getMessage(), e);
        }
    }

    public void prepareShowCmd(ActionOfFlow action) {
        if (action != null) {
            selectedActionView = action.getAction();
        }
    }

    public void setPreStepsData(ActionOfFlow actionFlow, int indexGroup, int indexActionFlow) {
        try {
            preStepsActionSelected = new ArrayList<>();
            for (int i = 0; i < actionFlow.getPreStepsNumber().size(); i++) {
                preStepsActionSelected.add(actionFlow.getPreStepsNumber().get(i));
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void updatePreStepAction() {
        try {
            if (preStepsActionSelected != null) {
                Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
                int groupIndex = Integer.valueOf(params.get("groupActionIndex"));
                int actionFlowIndex = Integer.valueOf(params.get("actionFlowIndex"));
                String actionFlowTableId = params.get("actionFlowRowId");

                String preStepActionTmp = "";
                for (String preStep : preStepsActionSelected) {
                    preStepActionTmp += preStep + ",";
                }

                preStepActionTmp = preStepActionTmp.endsWith(",") ? preStepActionTmp.substring(0, preStepActionTmp.length() - 1) : preStepActionTmp;
                actionOfFlowss.get(groupIndex).get(actionFlowIndex).setPreStepsNumberLabel(preStepActionTmp);
//				actionOfFlowss.get(groupIndex).get(actionFlowIndex).setPreStepsNumber(preStepsActionSelected);
                RequestContext.getCurrentInstance().update("form:" + actionFlowTableId);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Function config nocpro mop
     */
//    public void preNocproConfig() {
//
//        configNodeTemplate = new AutoConfigNodeTemplate();
//        paramInputs = new ArrayList<>();
//
//        try {
//            // get node's template info
//            Map<String, Object> filters = new HashMap<>();
//            filters.put("flowTemplateId",selectedFlowTemplate.getFlowTemplatesId());
//            List<AutoConfigNodeTemplate> configNodeTemplates = new AutoConfNodeTemServiceImpl().findListExac(filters, null);
//            if (configNodeTemplates != null && !configNodeTemplates.isEmpty()) {
//                configNodeTemplate = configNodeTemplates.get(0);
//            }
//
//            // get param from template
//            List<Action> lstAction = new ArrayList<>();
//            for (ActionOfFlow actionFlow : selectedFlowTemplate.getActionOfFlows()) {
//                lstAction.add(actionFlow.getAction());
//            }
//
//            if (!lstAction.isEmpty()) {
//                List<ActionDetail> lstActionDetail = new ArrayList<>();
//                for (Action action : lstAction) {
//                    lstActionDetail.addAll(action.getActionDetails());
//                }
//
//                if (!lstActionDetail.isEmpty()) {
//                    List<ActionCommand> lstActionCommand = new ArrayList<>();
//                    for (ActionDetail actionDetail : lstActionDetail) {
//                        lstActionCommand.addAll(actionDetail.getActionCommands());
//                    }
//
//                    if (!lstActionCommand.isEmpty()) {
//                        for (ActionCommand actionCmd : lstActionCommand) {
//                            paramInputs.addAll(actionCmd.getCommandDetail().getParamInputs());
//                        }
//                    }
//                }
//            }
//
//            if (!paramInputs.isEmpty()) {
//                AutoConfigMopParams configMopParam = null;
//                AutoConfigMopParamsId configMopParamsId = null;
//                Map<String, Integer> mapParamCode = new HashMap<>();
//
//                for (Iterator<ParamInput> iter = paramInputs.iterator(); iter.hasNext(); ) {
//                    ParamInput element = iter.next();
//                    if (mapParamCode.get(element.getParamCode()) == null) {
//
//                        mapParamCode.put(element.getParamCode(), 1);
//                        // set config mop param
//                        configMopParamsId = new AutoConfigMopParamsId(selectedFlowTemplate.getFlowTemplatesId(), element.getParamCode());
//                        configMopParam = new AutoMopParamsServiceImpl().findById(configMopParamsId);
//                        if (configMopParam != null) {
//                            element.setConfigParam(configMopParam);
//                        } else {
//                            element.setConfigParam(new AutoConfigMopParams(configMopParamsId));
//                        }
//                    } else {
//                        // remove param have the same param code
//                        iter.remove();
//                    }
//                }
//            }
//
//            RequestContext.getCurrentInstance().update("panelSetPaserParams:paserParamsTable");
//            RequestContext.getCurrentInstance().execute("PF('dlgSetPaserParams').show()");
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//            MessageUtil.setErrorMessageFromRes("update.failed");
//        }
//    }
    public void onRowEditConfParam() {

    }

    public void onRowCancelConfParam() {

    }

    /**
     * Function save config how get param value
     */
//    public  void saveConfParamVal() {
//        if (paramInputs != null && !paramInputs.isEmpty()) {
//            List<AutoConfigMopParams> configParams = new ArrayList<>();
//            for (ParamInput p : paramInputs) {
//                if (p.getConfigParam() != null) {
//                    configParams.add(p.getConfigParam());
//                }
//            }
//
//            if (!configParams.isEmpty()) {
//                try {
//                    new AutoMopParamsServiceImpl().saveOrUpdate(configParams);
//                    MessageUtil.setInfoMessageFromRes("update.successful");
//                } catch (Exception e) {
//                    logger.error(e.getMessage(), e);
//                    MessageUtil.setErrorMessageFromRes("update.failed");
//                }
//            }
//        }
//    }
//
//    /**
//     * Function save how get node template info
//     */
//    public void saveConfNodeNocpro() {
//        if (configNodeTemplate != null) {
//            try {
//                if (configNodeTemplate.getFlowTemplateId() == null) {
//                    configNodeTemplate.setFlowTemplateId(selectedFlowTemplate.getFlowTemplatesId());
//                }
//                new AutoConfNodeTemServiceImpl().saveOrUpdate(configNodeTemplate);
//                MessageUtil.setInfoMessageFromRes("update.successful");
//            } catch (Exception e) {
//                logger.error(e.getMessage(), e);
//                MessageUtil.setErrorMessageFromRes("update.failed");
//            }
//        }
//    }

    //    20180316_Quytv7_cau hinh map tham so canh bao start
    public void buildParamInTemplate() {
        try {
            lstParamInputInTemplates = new ArrayList<>();
            lstParamAlarmInTempates = new ArrayList<>();
            lstParamNodeAlarmInTempates = new ArrayList<>();
            mapParamAlarmInTemplates.clear();
            systemTypeMapParam = MessageUtil.getResourceBundleMessage("label.category.domain.system.type.nocpro");
            categoryGroupDomains = new ArrayList<>();
            categoryDomains = new ArrayList<>();
            categoryConfigGetNodeGroups = new ArrayList<>();
            categoryConfigGetNodeGroup = null;
            categoryGroupDomain = null;
            categoryConfigGetNode = null;
            afterSystemTypeSelected();
            HashMap<String, Object> filters = new HashMap<>();
            //Quytv7_lay tham so node mang start
            filters.clear();
            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            orders.put("groupId", "ASC");
            List<CategoryConfigGetNode> categoryConfigGetNodes = new CategoryConfigGetNodeServiceImpl().findList(filters, orders);
            HashMap<Long, CategoryConfigGetNode> mapGroupConfigGetNode = new HashMap<>();

            for (CategoryConfigGetNode object : categoryConfigGetNodes) {
                if (!mapGroupConfigGetNode.containsKey(object.getGroupId())) {
                    mapGroupConfigGetNode.put(object.getGroupId(), object);
                    ComboBoxObject boxObject = new ComboBoxObject();
                    boxObject.setLabel(object.getConfigName());
                    boxObject.setValue(object.getGroupId().toString());
                    categoryConfigGetNodeGroups.add(boxObject);
                }
            }
            filters.clear();
            filters.put("flowTemplates.flowTemplatesId", selectedFlowTemplate.getFlowTemplatesId());
            filters.put("paramType", 1L);
            lstParamNodeAlarmInTempates = new FlowTemplateMapAlarmServiceImpl().findList(filters);
            if (lstParamNodeAlarmInTempates != null && lstParamNodeAlarmInTempates.size() > 0) {
                if (categoryGroupDomain == null || systemTypeMapParam == null && lstParamNodeAlarmInTempates.get(0).getDomain() != null) {
                    categoryGroupDomain = lstParamNodeAlarmInTempates.get(0).getDomain().getGroupDomain();
                    systemTypeMapParam = lstParamNodeAlarmInTempates.get(0).getDomain().getSystemType();
                }
                categoryConfigGetNode = lstParamNodeAlarmInTempates.get(0).getConfigGetNode();
                categoryConfigGetNodeGroup = new ComboBoxObject();
                categoryConfigGetNodeGroup.setValue(lstParamNodeAlarmInTempates.get(0).getConfigGetNode().getGroupId().toString());
                categoryConfigGetNodeGroup.setLabel(lstParamNodeAlarmInTempates.get(0).getConfigGetNode().getConfigName());
            }
            getListCategoryDomain(false);
//            Quytv7_lay tham so node mang end
            filters.clear();
            filters.put("flowTemplates.flowTemplatesId", selectedFlowTemplate.getFlowTemplatesId());
            filters.put("paramType", 0L);
            List<FlowTemplateMapAlarm> lstTemp = new FlowTemplateMapAlarmServiceImpl().findList(filters);
            ConcurrentHashMap<String, String> mapTemp = new ConcurrentHashMap<>();
            if (lstTemp != null && lstTemp.size() > 0) {

                for (FlowTemplateMapAlarm flowTemplateMapAlarm1 : lstTemp) {

                    if (!mapParamAlarmInTemplates.containsKey(flowTemplateMapAlarm1.getParamInput().getParamCode().toLowerCase().trim())) {
                        mapParamAlarmInTemplates.put(flowTemplateMapAlarm1.getParamCode().toLowerCase().trim(), flowTemplateMapAlarm1);
                    }
                }
            }
            if (selectedFlowTemplate != null) {
                try {
                    int size = selectedFlowTemplate.getActionOfFlows().size();
                    List<ActionOfFlow> lstActionFlow = selectedFlowTemplate.getActionOfFlows();
                    for (int i = size - 1; i >= 0; i--) {
                        ActionOfFlow actionFlow = lstActionFlow.get(i);
                        for (ActionDetail actionDetail : actionFlow.getAction().getActionDetails()) {

                            for (ActionCommand actionCmd : actionDetail.getActionCommands()) {
                                lstParamInputInTemplates.addAll(actionCmd.getCommandDetail().getParamInputs());
                                for (ParamInput param : actionCmd.getCommandDetail().getParamInputs()) {
                                    mapTemp.put(param.getParamCode().toLowerCase().trim(), param.getParamCode().toLowerCase().trim());
                                    if (!mapParamAlarmInTemplates.containsKey(param.getParamCode().toLowerCase().trim())) {
                                        FlowTemplateMapAlarm flowTemplateMapAlarm = new FlowTemplateMapAlarm();
                                        flowTemplateMapAlarm.setParamInput(param);
                                        flowTemplateMapAlarm.setParamCode(param.getParamCode());
                                        mapParamAlarmInTemplates.put(param.getParamCode().toLowerCase().trim(), flowTemplateMapAlarm);
                                        lstParamAlarmInTempates.add(flowTemplateMapAlarm);
                                    }
                                }
                            }
                        }
                    }

                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
            for (FlowTemplateMapAlarm flowTemplateMapAlarm1 : lstTemp) {
                if (mapTemp.containsKey(flowTemplateMapAlarm1.getParamCode().toLowerCase().trim())) {
                    lstParamAlarmInTempates.add(flowTemplateMapAlarm1);
                }
            }

            Collections.sort(lstParamAlarmInTempates, new Comparator<FlowTemplateMapAlarm>() {
                @Override
                public int compare(final FlowTemplateMapAlarm object1, final FlowTemplateMapAlarm object2) {
                    return object1.getParamCode().compareTo(object2.getParamCode());
                }
            });
            RequestContext.getCurrentInstance().execute("PF('dlgMapParamAlarm').show()");
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void afterSystemTypeSelected() {
        try {
            if (systemTypeMapParam != null && !"".equalsIgnoreCase(systemTypeMapParam)) {
                LinkedHashMap<String, String> orders = new LinkedHashMap<>();
                orders.put("groupName", "ASC");
                HashMap<String, Object> filters = new HashMap<>();
                filters.put("systemType", systemTypeMapParam.toLowerCase());
                categoryGroupDomains = new CategoryGroupDomainServiceImpl().findList(filters, orders);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void getListCategoryDomain(boolean isReload) {
        try {
            if (categoryGroupDomain != null && categoryGroupDomain.getGroupName() != null) {
                LinkedHashMap<String, String> orders = new LinkedHashMap<>();
                orders.put("domainCode", "ASC");
                HashMap<String, Object> filters = new HashMap<>();
                filters.put("groupDomain.id", categoryGroupDomain.getId());
                categoryDomains = new CategoryDomainServiceImpl().findList(filters, orders);
            }
            if (isReload) {
                if (categoryGroupDomain == null) {
                    categoryDomains.clear();
                }
                for (FlowTemplateMapAlarm flowTemplateMapAlarmTemp : lstParamNodeAlarmInTempates) {
                    flowTemplateMapAlarmTemp.setDomain(null);
                }
                for (FlowTemplateMapAlarm flowTemplateMapAlarmTemp : lstParamAlarmInTempates) {
                    flowTemplateMapAlarmTemp.setDomain(null);
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void getListParamNodes() {
        try {
            HashMap<String, Object> filters = new HashMap<>();
            if (categoryConfigGetNodeGroup != null) {
                if (categoryConfigGetNode != null && categoryConfigGetNodeGroup.getValue().equalsIgnoreCase(categoryConfigGetNode.getGroupId().toString())) {

                    filters.clear();
                    filters.put("flowTemplates.flowTemplatesId", selectedFlowTemplate.getFlowTemplatesId());
                    filters.put("paramType", 1L);
                    lstParamNodeAlarmInTempates = new FlowTemplateMapAlarmServiceImpl().findList(filters);
                } else {
                    lstParamNodeAlarmInTempates.clear();
                    filters.clear();
                    filters.put("groupId", categoryConfigGetNodeGroup.getValue());
                    List<CategoryConfigGetNode> categoryConfigGetNodes = new CategoryConfigGetNodeServiceImpl().findList(filters);
                    FlowTemplateMapAlarm flowTemplateMapAlarmTemp;
                    for (CategoryConfigGetNode categoryConfigGetNode1 : categoryConfigGetNodes) {
                        flowTemplateMapAlarmTemp = new FlowTemplateMapAlarm();
                        flowTemplateMapAlarmTemp.setParamCode(categoryConfigGetNode1.getParamName());
                        flowTemplateMapAlarmTemp.setConfigGetNode(categoryConfigGetNode1);
                        lstParamNodeAlarmInTempates.add(flowTemplateMapAlarmTemp);
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public void onSaveOrUpdateMapParamAlarm() {
        try {
            List<FlowTemplateMapAlarm> lstFinal = new ArrayList<>();
            Date startTime = new Date();
            boolean check = true;
            if (!lstParamNodeAlarmInTempates.isEmpty()) {
                for (FlowTemplateMapAlarm flowTemplateMapAlarm : lstParamNodeAlarmInTempates) {
                    if (flowTemplateMapAlarm.getDomain() == null) {
                        MessageUtil.setErrorMessageFromRes("message.error.is.null.param.get.node.alarm.domain");
                        return;
                    }
                    flowTemplateMapAlarm.setFlowTemplates(selectedFlowTemplate);
                    flowTemplateMapAlarm.setCreateUser(SessionWrapper.getCurrentUsername());
                    flowTemplateMapAlarm.setUpdateTime(new Date());
                    flowTemplateMapAlarm.setParamType(1L);
                    flowTemplateMapAlarm.setId(null);
                    lstFinal.add(flowTemplateMapAlarm);
                }
            } else {
                MessageUtil.setErrorMessageFromRes("message.error.is.null.param.get.node.alarm");
                return;
            }
            if (!lstParamAlarmInTempates.isEmpty()) {
                for (FlowTemplateMapAlarm flowTemplateMapAlarm : lstParamAlarmInTempates) {
                    flowTemplateMapAlarm.setFlowTemplates(selectedFlowTemplate);
                    flowTemplateMapAlarm.setCreateUser(SessionWrapper.getCurrentUsername());
                    flowTemplateMapAlarm.setUpdateTime(new Date());
                    flowTemplateMapAlarm.setParamType(0L);
                    flowTemplateMapAlarm.setId(null);
                    lstFinal.add(flowTemplateMapAlarm);
                }
            }
            Object[] objs = new FlowTemplateMapAlarmServiceImpl().openTransaction();
            Session session = (Session) objs[0];
            Transaction tx = (Transaction) objs[1];
//            if (!lstParamAlarmInTempates.isEmpty() && selectedFlowTemplate != null && selectedFlowTemplate.getFlowTemplatesId() != null) {
            if (selectedFlowTemplate != null && selectedFlowTemplate.getFlowTemplatesId() != null) {
                try {
                    new FlowTemplateMapAlarmServiceImpl().execteBulk2("delete from FlowTemplateMapAlarm where flowTemplates.flowTemplatesId = ?", session, tx, false, selectedFlowTemplate.getFlowTemplatesId());
                    new FlowTemplateMapAlarmServiceImpl().saveOrUpdate(lstFinal, session, tx, true);
//                    new FlowTemplateMapAlarmServiceImpl().saveOrUpdate(lstParamAlarmInTempates);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), BuildTemplateFlowController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.CREATE,
                                lstFinal.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                    MessageUtil.setInfoMessageFromRes("info.save.success");
                    RequestContext.getCurrentInstance().execute("PF('dlgMapParamAlarm').hide()");
                } catch (Exception ex) {
                    logger.info(ex.getMessage(), ex);
                } finally {
                    if (tx.getStatus() != TransactionStatus.ROLLED_BACK && tx.getStatus() != TransactionStatus.COMMITTED) {
                        tx.rollback();
                    }
                    if (session.isOpen()) {
                        session.close();
                    }
                }
            } else {
                MessageUtil.setInfoMessage("Data is null or flow template is null");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("error.save.unsuccess");
        }
    }

    //    20180316_Quytv7_cau hinh map tham so canh bao end
    // 20180523_Quytv7_Them moi template start
    public List<TemplateGroup> autoCompleTemplateGroup(String actionName) {
        List<TemplateGroup> lstAction = new ArrayList<>();
        try {
            //lstAction = vendorServiceImpl.findList();
            Map<String, Object> filters = new HashMap<>();
            if (actionName != null) {
                filters.put("groupName", actionName);
            }
            LinkedHashMap<String, String> order = new LinkedHashMap<String, String>();
            order.put("groupName", "ASC");
            lstAction = new TemplateGroupServiceImpl().findList(filters, order);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstAction;
    }
    // 20180523_Quytv7_Them moi template end

    //<editor-fold defaultstate="collapsed" desc="Them dau viec quy trinh cho GNOC">
    //20190401_tudn_start them dau viec quy trinh cho GNOC
//    public void procedureChanged() {
//        try {
//            if (selectedProcedureId != null && !"".equals(selectedProcedureId) && !selectedProcedureId.equals(0L)) {
//                Map<String, Object> filter = new HashMap<>();
//                filter.put("isActive", 1L);
//                filter.put("parentId", selectedProcedureId);
//                lstProcedureWFId = new ProcedureGNOCServiceImpl().findList(filter);
//                if (lstProcedureWFId == null) {
//                    lstProcedureWFId = new ArrayList<>();
//                }
//            }
//        } catch (Exception ex) {
//            logger.error(ex.getMessage(), ex);
//        }
//    }

    private void loadLstProcedureId() {
        try {
            String hql = "select pro from ProcedureGNOC pro where pro.parentId is null and pro.isActive = 1";
            lstProcedureId = new ProcedureGNOCServiceImpl().findList(hql, -1, -1);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }
    //20190401_tudn_end them dau viec quy trinh cho GNOC

    //20190729_tudn_start them dau viec quy trinh cho GNOC

//    public void onSetTreeName(Long gnocId) {
//        try {
//            if (gnocId != null) {
//                Map<String, Object> filter = new HashMap<>();
//                filter.clear();
//                filter.put("isActive", 1L);
//                filter.put("procedureGNOCId", gnocId);
//                List<ProcedureGNOC> gnoc = new ProcedureGNOCServiceImpl().findList(filter);
//
//                if (gnoc != null && gnoc.size() > 0 && gnoc.get(0) != null && gnoc.get(0).getProcedureGNOCId() != null) {
//
//
//                    filter.clear();
//                    filter.put("isActive", 1L);
//                    filter.put("procedureGNOCId", gnoc.get(0).getParentId());
//                    List<ProcedureGNOC> gnocParent = new ProcedureGNOCServiceImpl().findList(filter);
//                    if (gnocParent != null && !gnocParent.isEmpty() && gnocParent.get(0) != null)
//                        selectedProcedureId = gnoc.get(0).getProcedureGNOCId();
//                    else
//                        selectedProcedureId = null;
//                    if (gnocParent != null && !gnocParent.isEmpty() && !Util.isNullOrEmpty(gnoc.get(0).getProcedureGNOCName()) && gnocParent.get(0) != null)
//                        selectedProcedureName = gnocParent.get(0).getProcedureGNOCName() + " ---> " + gnoc.get(0).getProcedureGNOCName();
//
//                    if (gnocParent != null && !gnocParent.isEmpty() && !Util.isNullOrEmpty(gnoc.get(0).getProcedureGNOCNameEn()) && gnocParent.get(0) != null)
//                        selectedProcedureNameEn = gnocParent.get(0).getProcedureGNOCNameEn() + " ---> " + gnoc.get(0).getProcedureGNOCNameEn();
//                } else {
//                    selectedProcedureId = null;
//                    selectedProcedureName = selectedProcedureNameTemp;
//                    selectedProcedureNameEn = selectedProcedureNameEnTemp;
//                }
//            } else {
//                selectedProcedureId = null;
//                selectedProcedureName = selectedProcedureNameTemp;
//                selectedProcedureNameEn = selectedProcedureNameEnTemp;
//            }
//        } catch (Exception ex) {
//            logger.error(ex.getMessage(), ex);
//        }
//    }

//    public void onNodeSelect(ProcedureGNOC gnoc) {
//        try {
//            lstProcedureWFId = new ArrayList<>();
//            selectedProcedureWFId = null;
//            Map<String, Object> filter = new HashMap<>();
//            if (gnoc != null && gnoc.getProcedureGNOCId() != null) {
//
//                filter.put("isActive", 1L);
//                filter.put("parentId", gnoc.getProcedureGNOCId());
//                lstProcedureWFId = new ProcedureGNOCServiceImpl().findList(filter);
//            }
//            onSetTreeName(gnoc.getProcedureGNOCId());
//
//        } catch (Exception ex) {
//            logger.error(ex.getMessage(), ex);
//        }
//    }

//    public void onClickProcedureWF() {
//        try {
//            if (selectedProcedureWFId != null) {
//                Map<String, Object> filter = new HashMap<>();
//
//                filter.put("isActive", 1L);
//                filter.put("procedureGNOCId", selectedProcedureWFId);
//                List<ProcedureGNOC> gnoc = new ProcedureGNOCServiceImpl().findList(filter);
//
//                if (gnoc != null && !gnoc.isEmpty() && gnoc.get(0) != null) {
//                    if (!Util.isNullOrEmpty(gnoc.get(0).getProcedureGNOCName())) {
//                        selectedProcedureWFName = gnoc.get(0).getProcedureGNOCName();
//                    }
//                    if (!Util.isNullOrEmpty(gnoc.get(0).getProcedureGNOCNameEn())) {
//                        selectedProcedureWFNameEn = gnoc.get(0).getProcedureGNOCNameEn();
//                    }
//                }
//            } else {
//                selectedProcedureWFName = selectedProcedureWFNameTemp;
//                selectedProcedureWFNameEn = selectedProcedureWFNameEnTemp;
//            }
//        } catch (Exception ex) {
//            logger.error(ex.getMessage(), ex);
//        }
//    }

    private List<ProcedureGNOC> loadChild(Long procedureGNOCId, String locale) {
        String hqlSubNode = "select distinct pro from ProcedureGNOC pro where pro.parentId = ? and pro.isActive = 1 order by pro.procedureGNOCName asc";
        String hqlSubNodeEn = "select distinct pro from ProcedureGNOC pro where pro.parentId = ? and pro.isActive = 1 order by pro.procedureGNOCNameEn asc";
        List<ProcedureGNOC> lstProcedureIdSub = new ProcedureGNOCServiceImpl().findList("vi".equals(locale) ? hqlSubNode : hqlSubNodeEn, -1, -1, procedureGNOCId);
        return lstProcedureIdSub;
    }

    private void loadSubParent(List<ProcedureGNOC> lstProcedureId, TreeNode rootNode, String keySearch, String localeTree) {
        Locale locale = (Locale) LanguageBean.getLocales().get(FacesContext.getCurrentInstance().getViewRoot().getLocale().getLanguage());
        String language = locale.getLanguage();

        // Load data for group module
        if (!lstProcedureId.isEmpty()) {
            for (ProcedureGNOC procedureGNOC : lstProcedureId) {
                TreeNode subParent = new DefaultTreeNode(PARENT_NODE, procedureGNOC, rootNode);
                subParent.setSelectable(false);
                List<ProcedureGNOC> lstProcedureIdSub = loadChild(procedureGNOC.getProcedureGNOCId(), localeTree);
                if (lstProcedureIdSub != null && !lstProcedureIdSub.isEmpty()) {
                    for (ProcedureGNOC gnocSub : lstProcedureIdSub) {
                        if (keySearch != null && !"".equals(keySearch)) {
                            subParent.setExpanded(true);
                            if ("vi".equals(language.toLowerCase())) {
                                if (gnocSub.getProcedureGNOCName() != null && gnocSub.getProcedureGNOCName().toLowerCase().contains(keySearch.toLowerCase())) {
                                    TreeNode child = new DefaultTreeNode(CHILD_NODE, gnocSub, subParent);
                                }
                            } else {
                                if (gnocSub.getProcedureGNOCNameEn() != null && gnocSub.getProcedureGNOCNameEn().toLowerCase().contains(keySearch.toLowerCase())) {
                                    TreeNode child = new DefaultTreeNode(CHILD_NODE, gnocSub, subParent);
                                }
                            }
                        } else {
                            TreeNode child = new DefaultTreeNode(CHILD_NODE, gnocSub, subParent);
                        }
                    }
                }
            }
            if (keySearch != null && !"".equals(keySearch)) {
                List<TreeNode> lstSubParent = rootNode.getChildren();
                for (Iterator<TreeNode> iterator = lstSubParent.iterator(); iterator.hasNext(); ) {
                    TreeNode node = iterator.next();
                    ProcedureGNOC gnoc = (ProcedureGNOC) node.getData();
                    if ("vi".equals(language.toLowerCase())) {
                        if ((gnoc.getProcedureGNOCName() == null || !gnoc.getProcedureGNOCName().toLowerCase().contains(keySearch.toLowerCase())) && node.getChildren().size() == 0) {
                            iterator.remove();
                        } else {
                            if (node.getChildren().size() == 0) {
                                node.setExpanded(false);
                                List<ProcedureGNOC> lstProcedureIdSub = loadChild(gnoc.getProcedureGNOCId(), localeTree);
                                if (lstProcedureIdSub != null && !lstProcedureIdSub.isEmpty()) {
                                    for (ProcedureGNOC gnocSub : lstProcedureIdSub) {
                                        TreeNode child = new DefaultTreeNode(CHILD_NODE, gnocSub, node);
                                    }
                                }
                            }
                        }
                    } else {
                        if ((gnoc.getProcedureGNOCNameEn() == null || !gnoc.getProcedureGNOCNameEn().toLowerCase().contains(keySearch.toLowerCase())) && node.getChildren().size() == 0) {
                            iterator.remove();
                        } else {
                            if (node.getChildren().size() == 0) {
                                node.setExpanded(false);
                                List<ProcedureGNOC> lstProcedureIdSub = loadChild(gnoc.getProcedureGNOCId(), localeTree);
                                if (lstProcedureIdSub != null && !lstProcedureIdSub.isEmpty()) {
                                    for (ProcedureGNOC gnocSub : lstProcedureIdSub) {
                                        TreeNode child = new DefaultTreeNode(CHILD_NODE, gnocSub, node);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

//    private void loadTreeProcedureId() {
//        try {
//            //tieng viet
//            String hql = "select distinct pro from ProcedureGNOC pro where pro.parentId is null and pro.isActive = 1 order by pro.procedureGNOCName asc";
//            lstProcedureId = new ProcedureGNOCServiceImpl().findList(hql, -1, -1);
//
//
//            rootProcedureId = new DefaultTreeNode(new ProcedureGNOC(), null);
//            rootProcedureId.setSelectable(false);
//
//
//            ProcedureGNOC gnocParent = new ProcedureGNOC();
//            gnocParent.setProcedureGNOCName(selectedProcedureNameTemp);
//            gnocParent.setProcedureGNOCNameEn(selectedProcedureNameEnTemp);
//            TreeNode parent = new DefaultTreeNode(gnocParent, rootProcedureId);
//
//            parent.setExpanded(true);
//            loadSubParent(lstProcedureId, parent, keyProcedureSearch, "vi");
//
//            //tieng anh
//            String hqlEn = "select distinct pro from ProcedureGNOC pro where pro.parentId is null and pro.isActive = 1 order by pro.procedureGNOCNameEn asc";
//            lstProcedureIdEn = new ProcedureGNOCServiceImpl().findList(hqlEn, -1, -1);
//
//
//            rootProcedureIdEn = new DefaultTreeNode(new ProcedureGNOC(), null);
//            rootProcedureIdEn.setSelectable(false);
//
//
//            ProcedureGNOC gnocParentEn = new ProcedureGNOC();
//            gnocParent.setProcedureGNOCName(selectedProcedureNameTemp);
//            gnocParent.setProcedureGNOCNameEn(selectedProcedureNameEnTemp);
//            TreeNode parentEn = new DefaultTreeNode(gnocParentEn, rootProcedureIdEn);
//
//            parentEn.setExpanded(true);
//            loadSubParent(lstProcedureIdEn, parentEn, keyProcedureSearch, "en");
//
//        } catch (Exception ex) {
//            logger.error(ex.getMessage(), ex);
//        }
//    }

//    public void searchProcedureNode() {
//        try {
//            if (keyProcedureSearch != null) {
//                rootProcedureId.clearParent();
//                loadTreeProcedureId();
//            }
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//    }
    //20190729_tudn_end them dau viec quy trinh cho GNOC
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Check param when create MOP">
    /*20190408_chuongtq start check param when create MOP*/
    public void saveParamCondition() {
        try {
            List<ParamCondition> lstParamDel = selectedFlowTemplate.getParamConditions();
//            Map<String, Object> filters = new HashMap<>();
//            filters.put("flowTemplates.flowTemplatesId", selectedFlowTemplate.getFlowTemplatesId());
//            List<ParamCondition> lstParamDel = new ParamConditionServiceImpl().findList(filters);
            if (lstParamDel != null && !lstParamDel.isEmpty()) {
                new ParamConditionServiceImpl().delete(lstParamDel);
            }

            List<String> lstParamConditionSaved = new ArrayList<>();
            List<ParamCondition> lstParamConditionSave = new ArrayList<>();
            for (ParamCondition param : lstParamCondition) {
                for (ParamCondition _param : param.getParamInput().getParamConditions()) {
                    if (_param.getConditionOperator() != null && _param.getConditionOperator().length() > 0
                            && _param.getFlowTemplates().getFlowTemplatesId().equals(selectedFlowTemplate.getFlowTemplatesId())) {
                        _param.setFlowTemplates(param.getFlowTemplates());
                        _param.setParamInput(param.getParamInput());
                        for (ParamInput paramInput : lstParamInputCondition) {
                            if (_param.getParamInput().getParamCode().equals(paramInput.getParamCode())) {
                                boolean flag = true;
                                for (String saved : lstParamConditionSaved) {
                                    if (saved.equals(paramInput.getParamInputId() + "-" + _param.getConditionOperator() + "-" + _param.getConditionValue())) {
                                        flag = false;
                                        break;
                                    }
                                }
                                if (flag) {
                                    _param.setParamInput(paramInput);
                                    _param.setConditionOperator(_param.getConditionOperator().trim());
                                    _param.setConditionValue(_param.getConditionValue().trim());
                                    lstParamConditionSaved.add(paramInput.getParamInputId() + "-" + _param.getConditionOperator() + "-" + _param.getConditionValue());
                                    lstParamConditionSave.add(new ParamCondition(_param));
                                }
                            }
                        }
                    }
                }
            }

            if (!lstParamConditionSave.isEmpty()) {
                new ParamConditionServiceImpl().saveOrUpdate(lstParamConditionSave);
                selectedFlowTemplate = new FlowTemplatesServiceImpl().findById(selectedFlowTemplate.getFlowTemplatesId());
            }
            if (selectedFlowTemplate != null && selectedFlowTemplate.getFlowTemplatesId() != null) {
                new FlowTemplatesServiceImpl().execteBulk("update FlowTemplates set updateBy = ? where flowTemplatesId = ? ",
                        userName,
                        selectedFlowTemplate.getFlowTemplatesId());
            }
            MessageUtil.setInfoMessageFromRes("label.action.updateOk");
        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
            logger.error(e.getMessage(), e);
        }
    }

    public void buildParamCondition() {
        lstParamCondition = new ArrayList<>();
        lstParamInputCondition = null;
        isChangeGroupCode = false;
        Map<String, String> mapParamGroupKey = new HashMap<>();
        if (selectedFlowTemplate != null) {
            try {
                List<Action> lstAction = new ArrayList<>();
                Map<String, Object> filters = new HashMap<String, Object>();
                filters.put("flowTemplates.flowTemplatesId", selectedFlowTemplate.getFlowTemplatesId());
                List<ActionOfFlow> actionOfFlows = new ActionOfFlowServiceImpl().findList("from ActionOfFlow where flowTemplates.flowTemplatesId =?", -1, -1, selectedFlowTemplate.getFlowTemplatesId());
                for (ActionOfFlow actionFlow : actionOfFlows) {
                    lstAction.add(actionFlow.getAction());
                }

                if (!lstAction.isEmpty()) {
                    List<ActionDetail> lstActionDetail = new ArrayList<>();
                    for (Action action : lstAction) {
                        lstActionDetail.addAll(action.getActionDetails());
                    }

                    if (!lstActionDetail.isEmpty()) {
                        List<ActionCommand> lstActionCommand = new ArrayList<>();
                        for (ActionDetail actionDetail : lstActionDetail) {
                            lstActionCommand.addAll(actionDetail.getActionCommands());
                        }

                        if (!lstActionCommand.isEmpty()) {
                            List<ParamInput> lstParamInput = new ArrayList<>();
                            for (ActionCommand actionCmd : lstActionCommand) {
                                lstParamInput.addAll(actionCmd.getCommandDetail().getParamInputs());
                            }

                            lstParamInputCondition = lstParamInput;
                            lstParamInput = distinctParamInputSameValuecode(lstParamInput);

                            if (!lstParamInput.isEmpty()) {
//                                List<ParamCondition> paramConditions = selectedFlowTemplate.getParamConditions();
                                List<ParamCondition> paramConditions = selectedFlowTemplate.getParamConditions();
                                Map<ParamGroupId, ParamCondition> mapParamConditions = new HashMap<>();

                                for (ParamCondition paramCondition2 : paramConditions) {
                                    mapParamConditions.put(paramCondition2.getParamGroupId(), paramCondition2);
                                }

                                for (ParamInput paramInput : lstParamInput) {
                                    String key = paramInput.getParamInputId() + "_" + selectedFlowTemplate.getFlowTemplatesId();

                                    if (mapParamGroupKey.get(key) == null) {
                                        mapParamGroupKey.put(key, key);
                                        ParamGroupId paramGroupId = new ParamGroupId(paramInput.getParamInputId(), selectedFlowTemplate.getFlowTemplatesId());
                                        if (mapParamConditions.get(paramGroupId) != null) {
                                            ParamCondition paramCondition = mapParamConditions.get(paramGroupId);
                                            lstParamCondition.add(paramCondition);
                                        } else {
                                            lstParamCondition.add(new ParamCondition(paramInput, selectedFlowTemplate));
                                        }
                                    }
                                } // end loop for param input
                                System.out.println("");
                            }
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            if (lstParamCondition != null && !lstParamCondition.isEmpty()) {
                RequestContext.getCurrentInstance().execute("PF('dlgParamCondition').show()");
            } else {
                MessageUtil.setErrorMessageFromRes("datatable.empty");
            }
        }
    }

    public ParamCondition createNewParamCondition() {
        return new ParamCondition(selectedFlowTemplate);
    }

    public Integer createNewIndex() {
        return nIndex++;
    }

    public List<ParamInput> distinctParamInputSameValuecode(List<ParamInput> lstParamInput) {

        List<ParamInput> _lstParamInput = new ArrayList<>();
        boolean flag = true;
        for (ParamInput paramInput : lstParamInput) {
            for (ParamInput _paramInput : _lstParamInput) {
                if (paramInput.getParamCode().equals(_paramInput.getParamCode())) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                _lstParamInput.add(paramInput);
            } else {
                flag = true;
            }
        }
        return _lstParamInput;
    }

    public List<ParamGroup> getAllParamGroup(ParamGroup paramGroup) {

        if (paramGroup != null) {
            ParamInput paramInput = paramGroup.getParamInput();

            return paramInput.getParamGroups();
        }
        return null;
    }

    /*20190408_chuongtq end check param when create MOP*/
    //</editor-fold>

    //<editor-fold defaultstate="collapsed" desc="Get&Set">

    public TemplateGroup getSelectTemplateGroup() {
        return selectTemplateGroup;
    }

    public void setSelectTemplateGroup(TemplateGroup selectTemplateGroup) {
        this.selectTemplateGroup = selectTemplateGroup;
    }

    public List<ParamInput> getLstParamInputInTemplates() {
        return lstParamInputInTemplates;
    }

    public void setLstParamInputInTemplates(List<ParamInput> lstParamInputInTemplates) {
        this.lstParamInputInTemplates = lstParamInputInTemplates;
    }

    public ConcurrentHashMap<String, FlowTemplateMapAlarm> getMapParamAlarmInTemplates() {
        return mapParamAlarmInTemplates;
    }

    public void setMapParamAlarmInTemplates(ConcurrentHashMap<String, FlowTemplateMapAlarm> mapParamAlarmInTemplates) {
        this.mapParamAlarmInTemplates = mapParamAlarmInTemplates;
    }

    public List<FlowTemplateMapAlarm> getLstParamAlarmInTempates() {
        return lstParamAlarmInTempates;
    }

    public void setLstParamAlarmInTempates(List<FlowTemplateMapAlarm> lstParamAlarmInTempates) {
        this.lstParamAlarmInTempates = lstParamAlarmInTempates;
    }

    public List<FlowTemplateMapAlarm> getLstParamNodeAlarmInTempates() {
        return lstParamNodeAlarmInTempates;
    }

    public void setLstParamNodeAlarmInTempates(List<FlowTemplateMapAlarm> lstParamNodeAlarmInTempates) {
        this.lstParamNodeAlarmInTempates = lstParamNodeAlarmInTempates;
    }

    public ComboBoxObject getCategoryConfigGetNodeGroup() {
        return categoryConfigGetNodeGroup;
    }

    public void setCategoryConfigGetNodeGroup(ComboBoxObject categoryConfigGetNodeGroup) {
        this.categoryConfigGetNodeGroup = categoryConfigGetNodeGroup;
    }

    public List<ComboBoxObject> getCategoryConfigGetNodeGroups() {
        return categoryConfigGetNodeGroups;
    }

    public void setCategoryConfigGetNodeGroups(List<ComboBoxObject> categoryConfigGetNodeGroups) {
        this.categoryConfigGetNodeGroups = categoryConfigGetNodeGroups;
    }

    public CategoryConfigGetNode getCategoryConfigGetNode() {
        return categoryConfigGetNode;
    }

    public void setCategoryConfigGetNode(CategoryConfigGetNode categoryConfigGetNode) {
        this.categoryConfigGetNode = categoryConfigGetNode;
    }

    public List<CategoryGroupDomain> getCategoryGroupDomains() {
        return categoryGroupDomains;
    }

    public void setCategoryGroupDomains(List<CategoryGroupDomain> categoryGroupDomains) {
        this.categoryGroupDomains = categoryGroupDomains;
    }

    public CategoryGroupDomain getCategoryGroupDomain() {
        return categoryGroupDomain;
    }

    public void setCategoryGroupDomain(CategoryGroupDomain categoryGroupDomain) {
        this.categoryGroupDomain = categoryGroupDomain;
    }

    public String getSystemTypeMapParam() {
        return systemTypeMapParam;
    }

    public void setSystemTypeMapParam(String systemTypeMapParam) {
        this.systemTypeMapParam = systemTypeMapParam;
    }

    public List<CategoryDomain> getCategoryDomains() {
        return categoryDomains;
    }

    public void setCategoryDomains(List<CategoryDomain> categoryDomains) {
        this.categoryDomains = categoryDomains;
    }

    public FlowTemplates getSelectedFlowTemplate() {
        return selectedFlowTemplate;
    }

    public void setSelectedFlowTemplate(FlowTemplates selectedFlowTemplate) {
        this.selectedFlowTemplate = selectedFlowTemplate;
    }

    public List<FlowTemplates> getLstFlowTemplate() {
        return lstFlowTemplate;
    }

    public void setLstFlowTemplate(List<FlowTemplates> lstFlowTemplate) {
        this.lstFlowTemplate = lstFlowTemplate;
    }

    public String getFlowTemplateName() {
        return flowTemplateName;
    }

    public void setFlowTemplateName(String flowTemplateName) {
        this.flowTemplateName = flowTemplateName;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public TreeNode getSelectedTreeNodeAction() {
        return selectedTreeNodeAction;
    }

    public void setSelectedTreeNodeAction(TreeNode selectedTreeNodeAction) {
        this.selectedTreeNodeAction = selectedTreeNodeAction;
    }

    public List<List<ActionOfFlow>> getActionOfFlowss() {
        return actionOfFlowss;
    }

    public void setActionOfFlowss(List<List<ActionOfFlow>> mapActionOfFlow) {
        this.actionOfFlowss = mapActionOfFlow;
    }

    public List<ActionOfFlow> getLstActionFlowDel() {
        return lstActionFlowDel;
    }

    public void setLstActionFlowDel(List<ActionOfFlow> lstActionFlowDel) {
        this.lstActionFlowDel = lstActionFlowDel;
    }

    public List<SelectItem> getLstItemGroupName() {
        return lstItemGroupName;
    }

    public void setLstItemGroupName(List<SelectItem> lstItemGroupName) {
        this.lstItemGroupName = lstItemGroupName;
    }

    public boolean isAddNewGroupName() {
        return isAddNewGroupName;
    }

    public void setAddNewGroupName(boolean isAddNewGroupName) {
        this.isAddNewGroupName = isAddNewGroupName;
    }

    public String getNewGroupActionName() {
        return newGroupActionName;
    }

    public void setNewGroupActionName(String newGroupActionName) {
        this.newGroupActionName = newGroupActionName;
    }

    public String getSelectedGroupActionName() {
        return selectedGroupActionName;
    }

    public void setSelectedGroupActionName(String selectedGroupActionName) {
        this.selectedGroupActionName = selectedGroupActionName;
    }

    public String getCopyFlowTemplateName() {
        return copyFlowTemplateName;
    }

    public void setCopyFlowTemplateName(String copyFlowTemplateName) {
        this.copyFlowTemplateName = copyFlowTemplateName;
    }

    public List<ParamGroup> getLstParamGroup() {
        return lstParamGroup;
    }

    public void setLstParamGroup(List<ParamGroup> lstParamGroup) {
        this.lstParamGroup = lstParamGroup;
    }

    public ParamGroup getSelectedParamGroup() {
        return selectedParamGroup;
    }

    public void setSelectedParamGroup(ParamGroup selectedParamGroup) {
        this.selectedParamGroup = selectedParamGroup;
    }

    public boolean isChangeGroupCode() {
        return isChangeGroupCode;
    }

    public void setChangeGroupCode(boolean isChangeGroupCode) {
        this.isChangeGroupCode = isChangeGroupCode;
    }

    public String getOldGroupActionName() {
        return oldGroupActionName;
    }

    public void setOldGroupActionName(String oldGroupActionName) {
        this.oldGroupActionName = oldGroupActionName;
    }

    public boolean isChangeGroupActionName() {
        return isChangeGroupActionName;
    }

    public void setChangeGroupActionName(boolean isChangeGroupActionName) {
        this.isChangeGroupActionName = isChangeGroupActionName;
    }

    public Map<String, Integer> getMapGroupName() {
        return mapGroupName;
    }

    public void setMapGroupName(Map<String, Integer> mapGroupName) {
        this.mapGroupName = mapGroupName;
    }

    public List<ParamInOut> getLstParamInOutObject() {
        return lstParamInOutObject;
    }

    public void setLstParamInOutObject(List<ParamInOut> lstParamInOutObject) {
        this.lstParamInOutObject = lstParamInOutObject;
    }

    public ActionOfFlow getSelectedActionFlow() {
        return selectedActionFlow;
    }

    public void setSelectedActionFlow(ActionOfFlow selectedActionFlow) {
        this.selectedActionFlow = selectedActionFlow;
    }

    public ActionCommand getSelectedActionCmd() {
        return selectedActionCmd;
    }

    public void setSelectedActionCmd(ActionCommand selectedActionCmd) {
        this.selectedActionCmd = selectedActionCmd;
    }

    public ActionDetail getSelectedActionDetail() {
        return selectedActionDetail;
    }

    public void setSelectedActionDetail(ActionDetail selectedActionDetail) {
        this.selectedActionDetail = selectedActionDetail;
    }

    public List<ActionOfFlow> getLstActionFlow() {
        return lstActionFlow;
    }

    public void setLstActionFlow(List<ActionOfFlow> lstActionFlow) {
        this.lstActionFlow = lstActionFlow;
    }

    public List<ActionCommand> getLstActionCommand() {
        return lstActionCommand;
    }

    public void setLstActionCommand(List<ActionCommand> lstActionCommand) {
        this.lstActionCommand = lstActionCommand;
    }

    public ParamInOut getSelectedParamInOut() {
        return selectedParamInOut;
    }

    public void setSelectedParamInOut(ParamInOut selectedParamInOut) {
        this.selectedParamInOut = selectedParamInOut;
    }

    public Action getSelectedActionView() {
        return selectedActionView;
    }

    public void setSelectedActionView(Action selectedActionView) {
        this.selectedActionView = selectedActionView;
    }

    public Boolean[] getDefaultAddToTemplate() {
        return defaultAddToTemplate;
    }

    public void setDefaultAddToTemplate(Boolean[] defaultAddToTemplate) {
        this.defaultAddToTemplate = defaultAddToTemplate;
    }

    public Integer getIndexOfGroupActionToDel() {
        return indexOfGroupActionToDel;
    }

    public void setIndexOfGroupActionToDel(Integer indexOfGroupActionToDel) {
        this.indexOfGroupActionToDel = indexOfGroupActionToDel;
    }

    public boolean isApproveTemplate() {
        return isApproveTemplate;
    }

    public void setApproveTemplate(boolean isApproveTemplate) {
        this.isApproveTemplate = isApproveTemplate;
    }

    public List<String> getPreStepsActionSelected() {
        return preStepsActionSelected;
    }

    public void setPreStepsActionSelected(List<String> preStepsActionSelected) {
        this.preStepsActionSelected = preStepsActionSelected;
    }

    public boolean isPreApproveTemplate() {
        return isPreApproveTemplate;
    }

    public void setPreApproveTemplate(boolean isPreApproveTemplate) {
        this.isPreApproveTemplate = isPreApproveTemplate;
    }

    public Integer getFlowTemplateType() {
        return flowTemplateType;
    }

    public void setFlowTemplateType(Integer flowTemplateType) {
        this.flowTemplateType = flowTemplateType;
    }

    public Node getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(Node selectedNode) {
        this.selectedNode = selectedNode;
    }


    public List<ParamInput> getParamInputs() {
        return paramInputs;
    }

    public void setParamInputs(List<ParamInput> paramInputs) {
        this.paramInputs = paramInputs;
    }

//    public AutoConfigNodeTemplate getConfigNodeTemplate() {
//        return configNodeTemplate;
//    }
//
//    public void setConfigNodeTemplate(AutoConfigNodeTemplate configNodeTemplate) {
//        this.configNodeTemplate = configNodeTemplate;
//    }

    //20190401_tudn_start them dau viec quy trinh cho GNOC
    public List<ProcedureGNOC> getLstProcedureId() {
        return lstProcedureId;
    }

    public void setLstProcedureId(List<ProcedureGNOC> lstProcedureId) {
        this.lstProcedureId = lstProcedureId;
    }

    public List<ProcedureGNOC> getLstProcedureWFId() {
        return lstProcedureWFId;
    }

    public void setLstProcedureWFId(List<ProcedureGNOC> lstProcedureWFId) {
        this.lstProcedureWFId = lstProcedureWFId;
    }

    public Long getSelectedProcedureWFId() {
        return selectedProcedureWFId;
    }

    public void setSelectedProcedureWFId(Long selectedProcedureWFId) {
        this.selectedProcedureWFId = selectedProcedureWFId;
    }

    public Long getSelectedProcedureId() {
        return selectedProcedureId;
    }

    public void setSelectedProcedureId(Long selectedProcedureId) {
        this.selectedProcedureId = selectedProcedureId;
    }

//20190401_tudn_END them dau viec quy trinh cho GNOC

    /*20190408_chuongtq start check param when create MOP*/
    public List<ParamCondition> getLstParamCondition() {
        return lstParamCondition;
    }

    public void setLstParamCondition(List<ParamCondition> lstParamCondition) {
        this.lstParamCondition = lstParamCondition;
    }

    public Integer getnIndex() {
        return nIndex;
    }

    public void setnIndex(Integer nIndex) {
        this.nIndex = nIndex;
    }
    /*20190408_chuongtq end check param when create MOP*/

    //20190729_tudn_start sua dau viec quy trinh cho GNOC

    public TreeNode getRootProcedureId() {
        return rootProcedureId;
    }

    public void setRootProcedureId(TreeNode rootProcedureId) {
        this.rootProcedureId = rootProcedureId;
    }

    public TreeNode getSelectedNodeProcedureId() {
        return selectedNodeProcedureId;
    }

    public void setSelectedNodeProcedureId(TreeNode selectedNodeProcedureId) {
        this.selectedNodeProcedureId = selectedNodeProcedureId;
    }

    public String getSelectedProcedureName() {
        return selectedProcedureName;
    }

    public void setSelectedProcedureName(String selectedProcedureName) {
        this.selectedProcedureName = selectedProcedureName;
    }

    public String getSelectedProcedureNameEn() {
        return selectedProcedureNameEn;
    }

    public void setSelectedProcedureNameEn(String selectedProcedureNameEn) {
        this.selectedProcedureNameEn = selectedProcedureNameEn;
    }

    public String getKeyProcedureSearch() {
        return keyProcedureSearch;
    }

    public void setKeyProcedureSearch(String keyProcedureSearch) {
        this.keyProcedureSearch = keyProcedureSearch;
    }

    public List<ProcedureGNOC> getLstProcedureIdEn() {
        return lstProcedureIdEn;
    }

    public void setLstProcedureIdEn(List<ProcedureGNOC> lstProcedureIdEn) {
        this.lstProcedureIdEn = lstProcedureIdEn;
    }

    public TreeNode getRootProcedureIdEn() {
        return rootProcedureIdEn;
    }

    public void setRootProcedureIdEn(TreeNode rootProcedureIdEn) {
        this.rootProcedureIdEn = rootProcedureIdEn;
    }

    public int getStatusApprove() {
        return statusApprove;
    }

    public void setStatusApprove(int statusApprove) {
        this.statusApprove = statusApprove;
    }

    public String getSelectedProcedureWFName() {
        return selectedProcedureWFName;
    }

    public void setSelectedProcedureWFName(String selectedProcedureWFName) {
        this.selectedProcedureWFName = selectedProcedureWFName;
    }

    public String getSelectedProcedureWFNameEn() {
        return selectedProcedureWFNameEn;
    }

    public void setSelectedProcedureWFNameEn(String selectedProcedureWFNameEn) {
        this.selectedProcedureWFNameEn = selectedProcedureWFNameEn;
    }
    //20190729_tudn_end sua dau viec quy trinh cho GNOC
    //</editor-fold>

}
