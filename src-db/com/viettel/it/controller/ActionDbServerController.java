package com.viettel.it.controller;

import com.sun.faces.component.visit.FullVisitContext;
import com.viettel.controller.AppException;
import com.viettel.controller.SysException;
import com.viettel.it.model.*;
import com.viettel.it.persistence.*;
import com.viettel.it.util.ActionUtil;
import com.viettel.it.util.Config;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.util.SessionUtil;
import com.viettel.util.SessionWrapper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.primefaces.context.RequestContext;
import org.primefaces.event.DragDropEvent;
import org.primefaces.event.ReorderEvent;
import org.primefaces.event.SelectEvent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.TreeNode;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.component.UIComponent;
import javax.faces.component.UIViewRoot;
import javax.faces.component.visit.VisitCallback;
import javax.faces.component.visit.VisitContext;
import javax.faces.component.visit.VisitResult;
import javax.faces.context.FacesContext;
import java.io.Serializable;
import java.util.*;

//import sun.rmi.runtime.Log;


@ViewScoped
@ManagedBean
public class ActionDbServerController implements Serializable {

    protected static final Logger logger = LoggerFactory.getLogger(ActionDbServerController.class);

    public static final Long ROOT_TREE_ID = 1L;
    public static final Integer NODE_EXPENDED = 1;
    public static final Integer NODE_NOT_EXPEND = 0;
    public static final String PARENT_NODE = "parent";
    public static final String CHILD_NODE = "child";
    public static final String IMPACT_LABEL_NODE = "impact_label";
    public static final String ROLLBACK_LABEL_NODE = "rollback_label";
    public static final Integer MAX_LENGTH_ACTION_NAME = 500;

    @ManagedProperty(value = "#{actionItService}")
    private ActionServiceImpl actionServiceImpl;

    @ManagedProperty(value = "#{actionDetailService}")
    private ActionDetailServiceImpl actionDetailServiceImpl;

    @ManagedProperty(value = "#{vendorService}")
    private VendorServiceImpl vendorServiceImpl;

    @ManagedProperty(value = "#{nodeTypeService}")
    private NodeTypeServiceImpl nodeTypeServiceImpl;

    @ManagedProperty(value = "#{commandTelnetParserService}")
    private CommandTelnetParserServiceImpl commandTelnetParserServiceImpl;

    private TreeNode rootNode;
    private TreeNode selectedNode;

    private Action insertNode;
    private boolean isEdit = false;
    private boolean isClone = false;

    private Action selectedAction; // action ma action detail link den
    private Vendor selectedVendor;
    private NodeType selectedNodeType;
    private Version selectedVersion;
    private ActionDetail actionDetail;
    private ActionDetail selectedActionDetail;
    private CommandDetail selectedCmdDetail;
    private CommandTelnetParser selectedCmdTelnetParser;
    private ActionCommand selectedActionCommand;

    private Long stationDetailStatus;
    private boolean isEditActionDetail;

    private List<ActionCommand> lstActionCommand = new LinkedList<>();
    private String userName;

    private List<ActionDetail> lstActionDetail = new ArrayList<>();
    private List<ActionCommand> lstActionCmdDel = new ArrayList<>();

    private String keyActionSearch = null;
    private String actionNameToClone;

    @PostConstruct
    public void onStart() {
        try {
            //selectedNodeType = new NodeTypeServiceImpl().findById(Config.NODE_TYPE_ID_DEFAULT);
            userName = SessionWrapper.getCurrentUsername();
            actionDetail = new ActionDetail();
            insertNode = new Action();
            selectedAction = new Action();
            createTree();
            selectedActionDetail = new ActionDetail();
            selectedActionDetail.setActionCommands(new ArrayList<ActionCommand>(0));

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * @param dragDropEvent
     * @author huynx6
     * Keo action vao template
     */
    public void onDrop(DragDropEvent dragDropEvent) {
        Object data = dragDropEvent.getData();
        System.err.println(data);
        final String dargId = dragDropEvent.getDragId();
        FacesContext context = FacesContext.getCurrentInstance();
        UIViewRoot viewRoot = FacesContext.getCurrentInstance().getViewRoot();
        final UIComponent[] found = new UIComponent[1];
        viewRoot.visitTree(new FullVisitContext(context), new VisitCallback() {
            @Override
            public VisitResult visit(VisitContext context, UIComponent component) {
                System.err.println(component.getId());
                if ("0_9_0_0".equals(component.getId())) {
                    found[0] = component;
                    return VisitResult.COMPLETE;
                }
                return VisitResult.ACCEPT;
            }
        });

//		UIComponent darg = viewRoot.findComponent(":form:treeAction:0_9_0_0");
        Map<String, String> params = FacesContext.getCurrentInstance().getExternalContext().getRequestParameterMap();
        String left = params.get(dargId + "_left");
        String top = params.get(dargId + "_top");
        System.err.println("Left: " + left + " Top: " + top);
    }

    /**
     * @author huynx6
     * Tim kiem tren tree
     */
    public void searchActionNode() {
        Action root;
        try {
            root = actionServiceImpl.findById(ROOT_TREE_ID);
            if (root != null) {
                rootNode = new DefaultTreeNode("action", "Root", null);

                Map<String, Object> filter = new HashMap<>();
                if (keyActionSearch != null)
                    filter.put("name", keyActionSearch);

                List<Action> resultSearch = actionServiceImpl.findList(filter);

                Set<Action> actionParents = new HashSet<>();
                actionParents.addAll(resultSearch);
                for (Action action : resultSearch) {
                    action.setExpanded(true);
                    getParent(action, actionParents);
                }

                buildTree(root, rootNode, new ArrayList<>(actionParents), 1);
                rootNode.setExpanded(true);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void getParent(Action action, Set<Action> actionParents) {
        if (action.getAction() != null) {
            action.setExpanded(true);
            actionParents.add(action.getAction());
            getParent(action.getAction(), actionParents);
        } else
            return;
    }


    public void prepareShowCmdInAction() {
        selectedAction = new Action();
        try {
            selectedAction = (Action) selectedNode.getData();
            selectedAction = actionServiceImpl.findById(selectedAction.getActionId());
            if (selectedAction.getActionDetails() != null
                    && !selectedAction.getActionDetails().isEmpty()) {
                RequestContext.getCurrentInstance().execute("PF('dlgShowCmdsInAction').show()");
            } else {
                MessageUtil.setErrorMessageFromRes("datatable.empty");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

    }

    public void preAddActionToTemplate() {

        if (selectedNode != null) {
            try {
                selectedAction = (Action) selectedNode.getData();
                selectedAction = actionServiceImpl.findById(selectedAction.getActionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

        }
    }

    /*
     * Tao cay danh muc action
     */
    public void createTree() {
        Action root;
        try {
            root = actionServiceImpl.findById(ROOT_TREE_ID);
            if (root != null) {
                rootNode = new DefaultTreeNode("action", "Root", null);

                LinkedHashMap<String, String> orders = new LinkedHashMap<>();
                orders.put("name", "ASC");

                List<Action> lstAllData = null;
                try {
                    lstAllData = actionServiceImpl.findList(null, orders);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                buildTree(root, rootNode, lstAllData, 1);
                rootNode.setExpanded(true);
            }
        } catch (SysException | AppException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /*
     * Ham tao cay theo nut
     */
    public TreeNode buildTree(Action treeObj, TreeNode parent, List<Action> lstAllData, int level) {
        TreeNode newNode = null;
        try {
            List<Action> childNode = getLstChid(lstAllData, treeObj.getActionId());

            if (childNode != null && childNode.size() > 0) {
                newNode = new DefaultTreeNode(PARENT_NODE, treeObj, parent);
                if (level == 1)
                    newNode.setExpanded(true);
                for (Action tt : childNode) {
                    buildTree(tt, newNode, lstAllData, (level + 1));
                }
            } else if (level <= 3) {
                newNode = new DefaultTreeNode(PARENT_NODE, treeObj, parent);

            } else {
                newNode = new DefaultTreeNode(CHILD_NODE, treeObj, parent);
            }
            if (treeObj.isExpanded())
                newNode.setExpanded(true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return newNode;
    }

    private List<Action> getLstChid(List<Action> lstAction, Long parentId) {
        List<Action> lstSubAction = new ArrayList<>();
        try {
            for (Iterator<Action> iterator = lstAction.iterator(); iterator.hasNext(); ) {
                Action ac = iterator.next();
                if ((ac.getAction() != null) && (ac.getAction().getActionId() == parentId)) {
                    lstSubAction.add(ac);
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        Collections.sort(lstSubAction, new Comparator<Action>() {

            @Override
            public int compare(Action o1, Action o2) {
                String preName1 = o1.getName().substring(0, Math.max(o1.getName().indexOf(" "), 0));
                String preName2 = o2.getName().substring(0, Math.max(o2.getName().indexOf(" "), 0));

                String name1 = o1.getName().substring(o1.getName().indexOf(" ") + 1);
                String name2 = o2.getName().substring(o2.getName().indexOf(" ") + 1);

                String[] tmpPre1s = preName1.split("[,.]", -1);
                String[] tmpPre2s = preName2.split("[,.]", -1);
                int r = comp(tmpPre1s, tmpPre2s);
                if (r == 0) {
                    return name1.trim().compareTo(name2.trim());
                }
                return r;
            }

            public int comp(String[] as, String[] bs) {
                int min = Math.min(as.length, bs.length);
                if (as.length < bs.length)
                    return 1;
                if (as.length > bs.length)
                    return -1;
                if (min == 0)
                    return 0;
                int i = Math.min(0, min - 1);
                if (as[i].equals(bs[i])) {
                    as = ArrayUtils.remove(as, i);
                    bs = ArrayUtils.remove(bs, i);
                    return comp(as, bs);
                }
                return as[i].compareTo(bs[i]);
            }
        });
        return lstSubAction;
    }

    public void prepareCloneAction() {
        logger.info("vao prepare clone");
        if (selectedNode != null) {
            try {
//                Date startTime = new Date();
                if (selectedNode.getParent() == null) {
                    logger.error("parent null cmnr !");
                }

                logger.info("chuan bi du lieu");
                isClone = true;
                isEdit = false;
                Action action = (Action) selectedNode.getData();
                action = actionServiceImpl.findById(action.getActionId());

                actionNameToClone = action.getName();

                insertNode = new Action();
                insertNode.setName("");
                insertNode.setDescription("");
                insertNode.setActionId(null);
                insertNode.setActionDetails(action.getActionDetails());
                insertNode.setActionOfFlows(null);
                insertNode.setActions(null);
                insertNode.setAction(action.getAction());

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public void prepareEdit() {
        isEdit = true;
        if (selectedNode != null) {
            try {
//                Date startTime = new Date();
                Action action = (Action) selectedNode.getData();
                action = actionServiceImpl.findById(action.getActionId());
                insertNode = new Action();
                insertNode.setName(action.getName());
                insertNode.setDescription(action.getDescription());
                insertNode.setActionId(action.getActionId());
                insertNode.setActionDetails(action.getActionDetails());
                insertNode.setActionOfFlows(action.getActionOfFlows());
                insertNode.setActions(action.getActions());
                insertNode.setAction(action.getAction());


            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        } else {
            logger.error(">>>>>>>> ERROR selectedNode is null");
        }
    }

    public void prepareDelAction() {
        boolean checkShowDlg = true;
        try {
            switch (selectedNode.getType()) {
                case PARENT_NODE:
                    if (selectedNode.getChildCount() > 0) {
                        checkShowDlg = false;
                        MessageUtil.setErrorMessageFromRes("label.err.action.exist.child");
                    }
                    break;
                case CHILD_NODE:
                    Action actionDel = (Action) selectedNode.getData();
                    actionDel = new ActionServiceImpl().findById(actionDel.getActionId());
                    if ((actionDel.getActionDetails() != null && !actionDel.getActionDetails().isEmpty())
                            || actionDel.getActionOfFlows() != null && !actionDel.getActionOfFlows().isEmpty()) {
                        checkShowDlg = false;
                        MessageUtil.setErrorMessageFromRes("label.error.action.have.detail");
                    }
                    break;

                default:
                    break;
            }
            if (checkShowDlg) {
                RequestContext.getCurrentInstance().execute("PF('confDlgDelAction').show()");
            }
        } catch (Exception e) {
//			checkShowDlg = false;
            logger.error(e.getMessage(), e);
        }

    }

    /**
     * Ham them node con moi cho node hien tai
     */
    public void insertChildNode() {

        try {
            Date startTime = new Date();
            if (StringUtils.isEmpty(insertNode.getName())) {
                MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
                return;
            } else if (insertNode.getName().trim().length() > MAX_LENGTH_ACTION_NAME) {
                MessageUtil.setErrorMessageFromRes("label.validate.length.action");
                return;
            }

            if (insertNode.getDescription() == null || "".equals(insertNode.getDescription().trim())) {
                MessageUtil.setErrorMessageFromRes("label.error.no.input.value");
                return;
            } else if (insertNode.getDescription().trim().length() > MAX_LENGTH_ACTION_NAME) {
                MessageUtil.setErrorMessageFromRes("label.validate.length.action.desc");
                return;
            }

            insertNode.setName(insertNode.getName().replaceAll(" +", " ").trim());
            insertNode.setDescription(insertNode.getDescription().replaceAll(" +", " ").trim());

            if (!isClone) {
                if (selectedNode == null || selectedNode.getData() == null) {
                    MessageUtil.setErrorMessageFromRes("message.err.no.node.selected");
                    return;
                }
                if (isEdit) {
                    insertNode.setActionId(((Action) selectedNode.getData()).getActionId());
                } else {
                    insertNode.setAction(actionServiceImpl.findById(((Action) selectedNode.getData()).getActionId()));
                }
                actionServiceImpl.saveOrUpdate(insertNode);
            } else {

				/*
                 * Ham clone du lieu cho action
				 */
                List<ActionDetail> actionsDetail = insertNode.getActionDetails();
                insertNode.setActionDetails(null);
                // luu action clone
                Long actionId = actionServiceImpl.save(insertNode);
                insertNode = actionServiceImpl.findById(actionId);

                // Tao moi cac action detail di theo action clone

                if (actionsDetail != null && !actionsDetail.isEmpty()) {
                    List<ActionCommand> lstActionCommand;
                    Long actionDetailId;
                    ActionDetail actionDetail;
                    for (int i = 0; i < actionsDetail.size(); i++) {

                        lstActionCommand = actionsDetail.get(i).getActionCommands();
                        actionsDetail.get(i).setDetailId(null);
                        actionsDetail.get(i).setAction(insertNode);
                        actionsDetail.get(i).setActionCommands(null);

                        actionDetailId = actionDetailServiceImpl.save(actionsDetail.get(i));
                        //20180620_tudn_start ghi log DB
                        try {
                            LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                    LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ParamNodeValController.class.getName(),
                                    Thread.currentThread().getStackTrace()[1].getMethodName(),
                                    LogUtils.ActionType.CREATE,
                                    actionsDetail.get(i).toString(), LogUtils.getRequestSessionId());
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                        //20180620_tudn_end ghi log DB
                        actionDetail = actionsDetail.get(i);
                        actionDetail.setDetailId(actionDetailId);

                        if (lstActionCommand != null) {
                            for (int j = 0; j < lstActionCommand.size(); j++) {
                                lstActionCommand.get(j).setActionCommandId(null);
                                lstActionCommand.get(j).setActionDetail(actionDetail);
                            }
                            new ActionCommandServiceImpl().saveOrUpdate(lstActionCommand);
                            //20180620_tudn_start ghi log DB
                            try {
                                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ParamNodeValController.class.getName(),
                                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                                        LogUtils.ActionType.CREATE,
                                        lstActionCommand.get(i).toString(), LogUtils.getRequestSessionId());
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                            //20180620_tudn_end ghi log DB
                        }
                    }

                }

            }


            // them moi node con vao node hien tai
            if (!isEdit && !isClone) {
                String label = (getNodeLevel(selectedNode) < 4 ? PARENT_NODE : CHILD_NODE);
                TreeNode node = new DefaultTreeNode(label, insertNode, selectedNode);
                selectedNode.getChildren().add(node);

            } else {

                // hanhnv68 20161017
                // Cap nhat lai trang thai cua cac template co action nay
                if (!isClone) {
                    updateTemplateReference(insertNode);
                }
                // end hanhnv68 20161017

                List<TreeNode> lstChildOfCurNode = selectedNode.getChildren();
                TreeNode parent = selectedNode.getParent();
                if (parent == null || parent.getChildren() == null) {
                    logger.info("Error parent null");
                    return;
                }

                List<TreeNode> lstChild = parent.getChildren();
                if (lstChild != null) {

                    String label = (getNodeLevel(selectedNode) <= 4 ? PARENT_NODE : CHILD_NODE);
                    if (!isClone) {
                        for (TreeNode node : lstChild) {
                            if (((Action) node.getData()).getActionId() == ((Action) selectedNode.getData()).getActionId()) {
                                lstChild.remove(node);
                                break;
                            }
                        } // end loop for
                    }

                    TreeNode newNode = new DefaultTreeNode(label, insertNode, parent);
                    newNode.setParent(parent);
                    newNode.getChildren().addAll(lstChildOfCurNode);

                    // add and sort list child node

                    parent.getChildren().add(0, newNode);
                    Collections.sort(parent.getChildren(), new Comparator<TreeNode>() {
                        @Override
                        public int compare(final TreeNode object1, final TreeNode object2) {
                            return ((Action) object1.getData()).getName().compareTo(((Action) object2.getData()).getName());
                        }
                    });

                }
            }

            /*20180710_hoangnd_fix bug hien thi sai sau khi rename_start*/
            if(isEdit) {
                createTree();
            }
            /*20180710_hoangnd_fix bug hien thi sai sau khi rename_end*/
            /*
             Ghi log tac dong nguoi dung
            */
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDbServerController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        (isEdit ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
                        insertNode.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            RequestContext.getCurrentInstance().execute("PF('dlgAddAction').hide()");
            MessageUtil.setInfoMessageFromRes("label.action.updateOk");

        } catch (Exception e) {

            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
        } finally {
            clean();
        }
    }

    private void updateTemplateReference(Action action) {
        if (action != null) {
            Date startTime = new Date();
            List<ActionOfFlow> lstActionFlow = action.getActionOfFlows();
            if (lstActionFlow != null && !lstActionFlow.isEmpty()) {
                List<FlowTemplates> lstFlowTemplate = new ArrayList<>();
                try {
                    for (ActionOfFlow actionFlow : lstActionFlow) {
                        FlowTemplates flowTemplate = actionFlow.getFlowTemplates();
                        if (flowTemplate != null) {
                            flowTemplate.setStatus(Config.APPROVAL_STATUS_DEFAULT);
                            lstFlowTemplate.add(flowTemplate);
                        }
                    }

                    new FlowTemplatesServiceImpl().saveOrUpdate(lstFlowTemplate);
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDbServerController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.UPDATE,
                                lstFlowTemplate.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

            }
        }
    }


    private Integer getNodeLevel(TreeNode node) {
        int level = 1;
        while (node != null) {
            node = node.getParent();
            if (node != null) {
                level++;
            }
        }
        return level;
    }

    /**
     * Ham xoa du lieu node con
     */
    public void deleteActionNode() {
        if (selectedNode != null) {
            try {
                Date startTime = new Date();
                deleteNodeOfTree(selectedNode);

                /*
                Ghi log tac dong nguoi dung
             	*/
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDbServerController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.DELETE,
                            ((Action) selectedNode.getData()).toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
            } catch (Exception e) {
                MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
                logger.error(e.getMessage(), e);
            } finally {
                clean();
            }
        }
    }

    public void deleteNodeOfTree(TreeNode selectedNode) {
        if (selectedNode != null) {
            try {
                selectedNode.getChildren().clear();
                selectedNode.getParent().getChildren().remove(selectedNode);

                List<TreeNode> lstNodeDelete = selectedNode.getChildren();
                if (lstNodeDelete != null && !lstNodeDelete.isEmpty()) {
                    for (TreeNode node : lstNodeDelete) {
                        try {
                            // goi de quy
                            deleteNodeOfTree(node);
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        }
                    } // end loop for
                }

                Date startTime = new Date();
                actionServiceImpl.delete((Action) selectedNode.getData());
                /*
                Ghi log tac dong nguoi dung
                */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDbServerController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.DELETE,
                            ((Action) selectedNode.getData()).toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
    }

    public boolean checkAddAction() {
        boolean check = false;
        try {
            if (selectedNode != null) {
//				System.out.println((Action) selec);
                if (selectedNode.getChildCount() > 0) {
                    check = true;
                } else {
                    Action action = (Action) selectedNode.getData();
                    if (action.getActionOfFlows() == null || action.getActionOfFlows().isEmpty()) {
                        check = true;
                    }
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return check;
    }

    public boolean checkIsLeft() {
        boolean check = false;
        try {
            if (selectedNode != null) {
                if (selectedNode.getChildCount() == 0) {
                    check = true;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return check;
    }

    public boolean checkAddActionToTemplate() {
        boolean check = false;
        try {

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return check;
    }

    public void clean() {
        isClone = false;
        isEdit = false;
         /*20180709_hoangnd_khong clear selectedNode_start*/
//        selectedNode = null;
        /*20180709_hoangnd_khong clear selectedNode_end*/
        insertNode = new Action();

        actionNameToClone = "";
        selectedAction = new Action();
        selectedActionCommand = new ActionCommand();
        selectedActionDetail = new ActionDetail();
        selectedVersion = new Version();
        selectedCmdDetail = new CommandDetail();
        selectedCmdTelnetParser = new CommandTelnetParser();
        selectedNodeType = new NodeType();
        selectedVendor = new Vendor();
    }

    public String getNameActionSelected() {
        if (selectedNode != null) {
            return ((Action) selectedNode.getData()).getName();
        }
        return "";
    }

    public TreeNode getRootNode() {
        return rootNode;
    }

    public void setRootNode(TreeNode rootNode) {
        this.rootNode = rootNode;
    }

    public ActionServiceImpl getActionServiceImpl() {
        return actionServiceImpl;
    }

    public void setActionServiceImpl(ActionServiceImpl actionServiceImpl) {
        this.actionServiceImpl = actionServiceImpl;
    }

    public TreeNode getSelectedNode() {
        return selectedNode;
    }

    public void setSelectedNode(TreeNode selectedNode) {
        this.selectedNode = selectedNode;
    }

    public Action getInsertNode() {
        return insertNode;
    }

    public void setInsertNode(Action insertNode) {
        this.insertNode = insertNode;
    }

    public boolean isEdit() {
        return isEdit;
    }

    public void setEdit(boolean isEdit) {
        this.isEdit = isEdit;
    }

    public List<Action> autoCompleAction(String actionName) {
        List<Action> lstAction = new ArrayList<>();
        try {
            Map<String, Object> filters = new HashMap<>();
            if (actionName != null) {
                filters.put("action.name", actionName);
            }
            LinkedHashMap<String, String> order = new LinkedHashMap<String, String>();
            order.put("action.name", "ASC");
            lstAction = actionServiceImpl.findList(filters, order);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstAction;
    }

    public List<Vendor> autoCompleVendor(String actionName) {
        List<Vendor> lstAction = new ArrayList<>();
        try {
            //lstAction = vendorServiceImpl.findList();
            Map<String, Object> filters = new HashMap<>();
            if (actionName != null) {
                filters.put("vendorName", actionName);
            }
            LinkedHashMap<String, String> order = new LinkedHashMap<String, String>();
            order.put("vendorName", "ASC");
            lstAction = vendorServiceImpl.findList(filters, order);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstAction;
    }

    public void addActionDetail() {
        if (selectedVendor != null
                && selectedNodeType != null
                && selectedAction != null
                && selectedVersion != null) {
            try {
                Date startTime = new Date();
                // kiem tra xem action detail them moi da ton tai hay chua
                if (!ActionUtil.checkExistActionDetail(selectedVendor.getVendorId(),
                        selectedNodeType.getTypeId(), selectedVersion.getVersionId(),
                        selectedAction.getActionId())) {

//                    String content = "";
                    if (!isEditActionDetail) {
                        ActionDetail newAcDetail = new ActionDetail();
                        newAcDetail.setAction(selectedAction);
                        newAcDetail.setNodeType(selectedNodeType);
                        newAcDetail.setVendor(selectedVendor);
                        newAcDetail.setUserName(userName);
                        newAcDetail.setActionCommands(new ArrayList<ActionCommand>(0));
                        newAcDetail.setIsActive(1l);
                        newAcDetail.setVersion(selectedVersion);

                        new ActionDetailServiceImpl().saveOrUpdate(newAcDetail);
                        isEditActionDetail = false;
                        selectedVendor = null;
                        selectedNodeType = null;
                        selectedVersion = null;

                    } else {
                        selectedActionDetail.setVendor(selectedVendor);
                        selectedActionDetail.setNodeType(selectedNodeType);
                        selectedActionDetail.setIsActive(stationDetailStatus);
                        new ActionDetailServiceImpl().saveOrUpdate(selectedActionDetail);
                    }

                    selectedAction = new ActionServiceImpl().findById(selectedAction.getActionId());

                    // hanhnv68 20161017
                    // Cap nhat lai trang thai cac template co tham chieu den action
                    updateTemplateReference(selectedAction);
                    // end hanhnv68 20161017

                    /*
                    Ghi log tac dong nguoi dung
                    */
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDbServerController.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                (isEditActionDetail ? LogUtils.ActionType.UPDATE : LogUtils.ActionType.CREATE),
                                selectedAction.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }

                    MessageUtil.setInfoMessageFromRes("label.action.updateOk");

                } else {
                    // thong bao loi ban ghi da ton tai
                    MessageUtil.setErrorMessageFromRes("label.error.exist");
                }
            } catch (Exception e) {
                MessageUtil.setErrorMessageFromRes("label.action.updateFail");
                logger.error(e.getMessage(), e);
            }

        } else {
            // thong bao loi chua nhap day du thong tin
            MessageUtil.setErrorMessageFromRes("label.error.notFillAllData");
        }
    }

    public List<NodeType> autoCompleNodeType(String actionName) {
        List<NodeType> lstAction = new ArrayList<>();
        try {
            Map<String, Object> filters = new HashMap<>();
            if (actionName != null) {
                filters.put("typeName", actionName);
            }
            LinkedHashMap<String, String> order = new LinkedHashMap<String, String>();
            order.put("typeName", "ASC");
            lstAction = nodeTypeServiceImpl.findList(filters, order);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstAction;
    }

    public List<Version> autoCompleteVersion(String version) {
        List<Version> lstVertion = new ArrayList<>();
        try {
            Map<String, Object> filters = new HashMap<>();
            if (version != null && !version.trim().isEmpty()) {
                filters.put("versionName", version);
            }
            LinkedHashMap<String, String> order = new LinkedHashMap<String, String>();
            order.put("versionName", "ASC");
            lstVertion = new VersionServiceImpl().findList(filters, order);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstVertion;
    }


    public void onSelectActionDetail(SelectEvent event) {
        try {
            selectedActionDetail = (ActionDetail) event.getObject();
            lstActionCommand = new LinkedList<>();
            lstActionCmdDel = new ArrayList<>();

            ActionCommand actionCmd;
            int size = selectedActionDetail.getActionCommands().size();
            for (int i = 0; i < size; i++) {
                actionCmd = selectedActionDetail.getActionCommands().get(i);
                actionCmd.setOrderRun(Long.valueOf(i));
                lstActionCommand.add(actionCmd);
            }

            if (lstActionCommand.size() > 0) {
                Collections.sort(lstActionCommand, new Comparator<ActionCommand>() {
                    @Override
                    public int compare(final ActionCommand object1, final ActionCommand object2) {
                        return object1.getOrderRun().compareTo(object2.getOrderRun());
                    }
                });
            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void prepareEditActionDetail() {
        try {
            isEditActionDetail = true;
            selectedNodeType = selectedActionDetail.getNodeType();
            selectedVendor = selectedActionDetail.getVendor();
            selectedVersion = selectedActionDetail.getVersion();
            stationDetailStatus = selectedActionDetail.getIsActive();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void deleteActionDetail() {
        try {
            Date startTime = new Date();
            if (selectedActionDetail != null) {
                if (selectedActionDetail.getActionCommands() != null && !selectedActionDetail.getActionCommands().isEmpty()) {
                    MessageUtil.setErrorMessageFromRes("label.err.del.action.have.cmd");
                    return;
                }
                new ActionDetailServiceImpl().delete(selectedActionDetail);
                lstActionCommand = new LinkedList<>();
                selectedActionDetail = new ActionDetail();
                selectedAction = new ActionServiceImpl().findById(selectedAction.getActionId());

                // hanhnv68 20161017
                // Cap nhat lai trang thai cua cac template co action nay
                updateTemplateReference(selectedAction);
                // end hanhnv68 20161017

                /*
                Ghi log tac dong nguoi dung
                */
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDbServerController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.DELETE,
                            selectedActionDetail.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }

                MessageUtil.setInfoMessageFromRes("label.action.delelteOk");
            } else {
                MessageUtil.setErrorMessageFromRes("message.choose.delete");
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
        }
    }

    public void closeDlgActionDetail() {
        rebuildTree();
        clean();
    }

    public void rebuildTree() {
        try {
            if (selectedNode != null && selectedNode.getParent() != null) {
                List<TreeNode> lstChildOfCurNode = selectedNode.getChildren();
                TreeNode parent = selectedNode.getParent();
                if (parent.getChildren() != null) {
                    List<TreeNode> lstChild = parent.getChildren();
                    if (lstChild != null) {
                        int count = 0;
                        for (TreeNode node : lstChild) {
                            count++;
                            if (((Action) node.getData()).getActionId() == ((Action) selectedNode.getData()).getActionId()) {
                                lstChild.remove(node);
                                break;
                            }
                        } // end loop for

                        TreeNode newNode = new DefaultTreeNode(CHILD_NODE, selectedAction, parent);
                        newNode.getChildren().addAll(lstChildOfCurNode);
                        parent.getChildren().add((count > 0 ? (count - 1) : count), newNode);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void onUnSelectActionDetail(SelectEvent event) {

    }

    public List<CommandDetail> autoCmdMethod(String cmd) {

        Map<String, Object> filters = new HashMap<>();
        if (cmd != null && !cmd.trim().isEmpty()) {
            filters.put("commandTelnetParser.cmd", cmd);
        }
        filters.put("vendor.vendorId", selectedActionDetail.getVendor().getVendorId());
//		filters.put("nodeType.typeId", selectedActionDetail.getNodeType().getTypeId());
        filters.put("version.versionId", selectedActionDetail.getVersion().getVersionId());
        List<CommandDetail> lstCmd = null;
        try {
            LinkedHashMap<String, String> order = new LinkedHashMap<String, String>();
            order.put("commandTelnetParser.cmd", "ASC");

            lstCmd = new CommandDetailServiceImpl().findList(0, 20, filters, order);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return lstCmd;
    }

    public static void main(String[] args) {
        List<Action> as;
        try {
            as = new ActionServiceImpl().findList();
            System.err.println(as);
        } catch (SysException | AppException e) {
            logger.error(e.getMessage(), e);
        }
//		Map<String, Object> filters = new HashMap<>();
//		filters.put("commandTelnetParser.cmd", "e");
//		filters.put("vendor.vendorId", 1L);
//		filters.put("nodeType.typeId", 3L);
//		List<CommandDetail> lstCmd = null;
//		try {
////			lstCmd = new CommandDetailServiceImpl().findList(0, 20, filters, null);
////			System.out.println(lstCmd.size());
//			System.out.println(MessageUtil.getResourceBundleMessage("label.scheduleImpact"));
//		} catch (Exception e) {
//			logger.error(e.getMessage(), e);
//		}
    }

    public void prepareAddActionDetail() {
        selectedAction = new Action();
        lstActionCommand = new LinkedList<>();
        lstActionDetail = new ArrayList<>();

        if (selectedNode != null) {
            selectedAction = ((Action) selectedNode.getData());
            try {
                selectedAction = actionServiceImpl.findById(selectedAction.getActionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            logger.info("selected action: " + selectedAction.getName());
        } else {
            selectedAction = new Action();
            selectedAction.setActionDetails(new ArrayList<ActionDetail>());
        }
    }

    public void addActionCommand() {
        try {
            if (selectedCmdDetail != null) {
                ActionCommand actionCmd = new ActionCommand();
                actionCmd.setActionDetail(selectedActionDetail);
                actionCmd.setCreateTime(new Date());
                actionCmd.setUserName(userName);
                actionCmd.setType(1L);
                actionCmd.setIsActive(1L);
                actionCmd.setCommandDetail(selectedCmdDetail);
                actionCmd.setOrderRun(Long.valueOf(lstActionCommand.size()));
                lstActionCommand.add(actionCmd);
            }
        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
            logger.error(e.getMessage(), e);
        }
    }

    public void deleteCmdDetail(ActionCommand cmdDel, int indexRow) {
        try {
            boolean isDelete = true;
            if (cmdDel.getActionCommandId() != null) {
                // Kiem tra xem cau lenh da duoc sinh MOP hay chua
//				if (cmdDel.getActionDetail().getAction().getActionOfFlows() != null
//						&& !cmdDel.getActionDetail().getAction().getActionOfFlows().isEmpty()) {
//					List<ActionOfFlow> lstActionFlow = cmdDel.getActionDetail().getAction().getActionOfFlows();
//					for (ActionOfFlow actionFLow : lstActionFlow) {
//						if (actionFLow.getNodeRunGroupActions() != null
//								&& !actionFLow.getNodeRunGroupActions().isEmpty()) {
//							MessageUtil.setErrorMessageFromRes("label.cmd.created.mop");
//							return;
//						}
//					}
//				}

                Map<String, Object> filters = new HashMap<>();
                filters.put("actionCommandByActionCommandInputId.actionCommandId", cmdDel.getActionCommandId());
                if (new ParamInOutServiceImpl().count2(filters) > 0) {
                    MessageUtil.setErrorMessageFromRes("message.choose.using");
                    isDelete = false;
                } else {
                    lstActionCmdDel.add(cmdDel);
                }
            }

            if (isDelete) {
                lstActionCommand.remove(indexRow);
                for (int i = 0; i < lstActionCommand.size(); i++) {
                    lstActionCommand.get(i).setOrderRun(Long.valueOf(i));
                }
            }
        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("label.action.deleteFail");
            logger.error(e.getMessage(), e);
        }
    }

    public void submitActionCommandData() {
        Session session = null;
        Date startTime = new Date();
        try {
            if (selectedActionDetail.getDetailId() == null) {
                return;
            }

            List<ActionCommand> lstActionCmdId = new ArrayList<>();
            List<ActionCommand> lstActionCmdNoId = new ArrayList<>();
            for (ActionCommand actionCmd : lstActionCommand) {
                if (actionCmd.getActionCommandId() == null) {
                    lstActionCmdNoId.add(actionCmd);
                } else {
                    lstActionCmdId.add(actionCmd);
                }
            }

            Object[] objs = new ActionCommandServiceImpl().openTransaction();

            session = (Session) objs[0];
            Transaction tx = (Transaction) objs[1];

            if (lstActionCmdDel != null) {
                new ActionCommandServiceImpl().delete(lstActionCmdDel, session, tx, false);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDbServerController.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.DELETE,
                            lstActionCmdDel.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB
            }

            new ActionCommandServiceImpl().saveOrUpdate(lstActionCmdNoId, session, tx, false);
            new ActionCommandServiceImpl().saveOrUpdate(lstActionCmdId, session, tx, false);

            tx.commit();

            selectedActionDetail = new ActionDetailServiceImpl().findById(selectedActionDetail.getDetailId());

            // hanhnv68 20161017
            // Cap nhat lai trang thai cua cac template co action nay
            updateTemplateReference(selectedAction);
            // end hanhnv68 20161017
            /*
            Ghi log tac dong nguoi dung
            */
            //20180620_tudn_start ghi log DB
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDbServerController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.UPDATE,
                        lstActionCmdNoId.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), SessionUtil.getCurrentUsername(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), ActionDbServerController.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.UPDATE,
                        lstActionCmdId.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            //20180620_tudn_end ghi log DB
            MessageUtil.setInfoMessageFromRes("label.action.updateOk");
        } catch (Exception e) {
            MessageUtil.setErrorMessageFromRes("label.action.updateFail");
            logger.error(e.getMessage(), e);
        } finally {
            lstActionCmdDel = new ArrayList<>();
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e2) {
                    logger.error(e2.getMessage(), e2);
                }
            }
        }
    }


    public void reorderDataTable(ReorderEvent event) {
        // luu lai danh sach action command moi
        for (int i = 0; i < lstActionCommand.size(); i++) {
            lstActionCommand.get(i).setOrderRun(Long.valueOf(i));
        }
    }

    public void closeActionDetailDlg() {
        selectedAction = new Action();
        selectedAction.setActionDetails(new ArrayList<ActionDetail>());
    }

    public ActionDetailServiceImpl getActionDetailServiceImpl() {
        return actionDetailServiceImpl;
    }

    public void setActionDetailServiceImpl(ActionDetailServiceImpl actionDetailServiceImpl) {
        this.actionDetailServiceImpl = actionDetailServiceImpl;
    }

    public Action getSelectedAction() {
        return selectedAction;
    }

    public void setSelectedAction(Action selectedAction) {
        this.selectedAction = selectedAction;
    }

    public VendorServiceImpl getVendorServiceImpl() {
        return vendorServiceImpl;
    }

    public void setVendorServiceImpl(VendorServiceImpl vendorServiceImpl) {
        this.vendorServiceImpl = vendorServiceImpl;
    }

    public NodeTypeServiceImpl getNodeTypeServiceImpl() {
        return nodeTypeServiceImpl;
    }

    public void setNodeTypeServiceImpl(NodeTypeServiceImpl nodeTypeServiceImpl) {
        this.nodeTypeServiceImpl = nodeTypeServiceImpl;
    }

    public Vendor getSelectedVendor() {
        return selectedVendor;
    }

    public void setSelectedVendor(Vendor selectedVendor) {
        this.selectedVendor = selectedVendor;
    }

    public NodeType getSelectedNodeType() {
        return selectedNodeType;
    }

    public void setSelectedNodeType(NodeType selectedNodeType) {
        this.selectedNodeType = selectedNodeType;
    }

    public ActionDetail getActionDetail() {
        return actionDetail;
    }

    public void setActionDetail(ActionDetail actionDetail) {
        this.actionDetail = actionDetail;
    }

    public CommandTelnetParserServiceImpl getCommandTelnetParserServiceImpl() {
        return commandTelnetParserServiceImpl;
    }

    public void setCommandTelnetParserServiceImpl(
            CommandTelnetParserServiceImpl commandTelnetParserServiceImpl) {
        this.commandTelnetParserServiceImpl = commandTelnetParserServiceImpl;
    }

    public List<ActionDetail> getLstActionDetail() {
        return lstActionDetail;
    }

    public void setLstActionDetail(List<ActionDetail> lstActionDetail) {
        this.lstActionDetail = lstActionDetail;
    }

    public ActionDetail getSelectedActionDetail() {
        return selectedActionDetail;
    }

    public void setSelectedActionDetail(ActionDetail selectedActionDetail) {
        this.selectedActionDetail = selectedActionDetail;
    }

    public CommandDetail getSelectedCmdDetail() {
        return selectedCmdDetail;
    }

    public void setSelectedCmdDetail(CommandDetail selectedCmdDetail) {
        this.selectedCmdDetail = selectedCmdDetail;
    }

    public CommandTelnetParser getSelectedCmdTelnetParser() {
        return selectedCmdTelnetParser;
    }

    public void setSelectedCmdTelnetParser(CommandTelnetParser selectedCmdTelnetParser) {
        this.selectedCmdTelnetParser = selectedCmdTelnetParser;
    }

    public List<ActionCommand> getLstActionCommand() {
        return lstActionCommand;
    }

    public void setLstActionCommand(List<ActionCommand> lstActionCommand) {
        this.lstActionCommand = lstActionCommand;
    }

    public Long getStationDetailStatus() {
        return stationDetailStatus;
    }

    public void setStationDetailStatus(Long stationDetailStatus) {
        this.stationDetailStatus = stationDetailStatus;
    }

    public boolean isEditActionDetail() {
        return isEditActionDetail;
    }

    public void setEditActionDetail(boolean isEditActionDetail) {
        this.isEditActionDetail = isEditActionDetail;
    }

    public ActionCommand getSelectedActionCommand() {
        return selectedActionCommand;
    }

    public void setSelectedActionCommand(ActionCommand selectedActionCommand) {
        this.selectedActionCommand = selectedActionCommand;
    }

    public List<ActionCommand> getLstActionCmdDel() {
        return lstActionCmdDel;
    }

    public void setLstActionCmdDel(List<ActionCommand> lstActionCmdDel) {
        this.lstActionCmdDel = lstActionCmdDel;
    }

    public Version getSelectedVersion() {
        return selectedVersion;
    }

    public void setSelectedVersion(Version selectedVersion) {
        this.selectedVersion = selectedVersion;
    }

    public String getKeyActionSearch() {
        return keyActionSearch;
    }

    public void setKeyActionSearch(String keyActionSearch) {
        this.keyActionSearch = keyActionSearch;
    }

    public boolean isClone() {
        return isClone;
    }

    public void setClone(boolean isClone) {
        this.isClone = isClone;
    }

    public String getActionNameToClone() {
        return actionNameToClone;
    }

    public void setActionNameToClone(String actionNameToClone) {
        this.actionNameToClone = actionNameToClone;
    }

}
