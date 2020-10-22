package com.viettel.it.controller;

import com.viettel.it.model.FlowTemplates;
import com.viettel.it.model.ProcedureGNOC;
import com.viettel.it.persistence.DaoSimpleService;
import com.viettel.it.persistence.ProcedureGNOCServiceImpl;
import com.viettel.it.util.CommonExport;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.util.SessionWrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.ss.usermodel.*;
import org.primefaces.component.treetable.TreeTable;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.ServletContext;
import java.io.*;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.regex.Pattern;

@ViewScoped
@ManagedBean
public class ProcedureCRTemplateController implements Serializable {
    protected static final Logger logger = LogManager.getLogger(ProcedureCRTemplateController.class);
    private String logAction = "";
    private String className = ProcedureCRTemplateController.class.getName();

    private TreeNode rootProcedureId;
    private TreeNode selectedNodeProcedure;
    private static final String PARENT_NODE = "parent";
    private static final String CHILD_NODE = "child";

    private Map<Long, List<FlowTemplates>> flowTemplatesMap = new HashMap<>();

    private Map<Long, TreeNode> treeNodeMap = new HashMap<>();

    private FlowTemplates selectedFlowTemplate;
    private ProcedureGNOC selectedProcedureGNOC;

    @PostConstruct
    public void onStart() {
        loadTemplate();
        loadTreeProcedureId();
        loadTemplateForTree(rootProcedureId);
    }

    private void loadTemplate() {
        flowTemplatesMap = new HashMap();
        StringBuilder query = new StringBuilder();
        query.append("select PGT.PROCEDURE_GNOC_ID,FT.FLOW_TEMPLATE_NAME, FT.CREATE_BY, FT.STATUS,FT.FLOW_TEMPLATES_ID from PROCEDURE_GNOC_TEMPLATE PGT ");
        query.append("join FLOW_TEMPLATES FT on PGT.FLOW_TEMPLATE_ID = ft.FLOW_TEMPLATES_ID");
        List<Object[]> listSQLAll = (List<Object[]>) new DaoSimpleService().findListSQLAll(query.toString());
        for (Object[] objects : listSQLAll) {
            FlowTemplates flow = new FlowTemplates();
            flow.setFlowTemplateName(((String) objects[1]));
            flow.setCreateBy(((String) objects[2]));
            if (objects[3] != null)
                flow.setStatus(((BigDecimal) objects[3]).intValue());
            flow.setFlowTemplatesId(((BigDecimal) objects[4]).longValue());
            List<FlowTemplates> flows;
            if (flowTemplatesMap.containsKey(((BigDecimal) objects[0]).longValue()))
                flows = flowTemplatesMap.get(((BigDecimal) objects[0]).longValue());
            else
                flows = new ArrayList<>();
            flows.add(flow);
            flowTemplatesMap.put(((BigDecimal) objects[0]).longValue(), flows);
        }
    }

    public List<FlowTemplates> completeTemplate(String key) {
        List<FlowTemplates> flowTemplates = new ArrayList<>();
        try {
            if (key == null) {
                key = "";
            }
            StringBuilder query = new StringBuilder();
            query.append("select FLOW_TEMPLATE_NAME,CREATE_BY,STATUS,FLOW_TEMPLATES_ID from FLOW_TEMPLATES");
            query.append(" where lower(FLOW_TEMPLATE_NAME) like '%'||?||'%' order by FLOW_TEMPLATE_NAME");
            List<Object[]> listSQLAll = (List<Object[]>) new DaoSimpleService().findListSQLAll(query.toString(), key.toLowerCase());
            for (Object[] objects : listSQLAll) {
                FlowTemplates flow = new FlowTemplates();
                flow.setFlowTemplateName(((String) objects[0]));
                flow.setCreateBy(((String) objects[1]));
                if (objects[2] != null)
                    flow.setStatus(((BigDecimal) objects[2]).intValue());
                flow.setFlowTemplatesId(((BigDecimal) objects[3]).longValue());
                boolean isExist = false;
                if (selectedProcedureGNOC != null && selectedProcedureGNOC.getFlowTemplates() != null) {
                    for (FlowTemplates flowTemplate : selectedProcedureGNOC.getFlowTemplates()) {
                        if (flowTemplate.getFlowTemplatesId().equals(flow.getFlowTemplatesId())) {
                            isExist = true;
                            break;
                        }
                    }
                }
                if (!isExist)
                    flowTemplates.add(flow);
            }
            return flowTemplates;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return flowTemplates;
    }

    public void preDelTemp4Procedure(ProcedureGNOC procedureGNOC, FlowTemplates flowTemplate, TreeNode selectedNodeProcedureId) {
        this.selectedProcedureGNOC = procedureGNOC;
        this.selectedNodeProcedure = selectedNodeProcedureId;
        selectedFlowTemplate = flowTemplate;
    }

    public void preAddTemp4Procedure(ProcedureGNOC procedureGNOC, TreeNode selectedNodeProcedureId) {
        this.selectedProcedureGNOC = procedureGNOC;
        this.selectedNodeProcedure = selectedNodeProcedureId;
        selectedFlowTemplate = null;
    }

    public void delTempForProcedure() {
        if (selectedFlowTemplate == null) {
            return;
        }
        try {
            flowTemplatesMap.get(selectedProcedureGNOC.getProcedureGNOCId()).remove(selectedFlowTemplate);
            new DaoSimpleService().execteNativeBulk("delete from PROCEDURE_GNOC_TEMPLATE where PROCEDURE_GNOC_ID =? and FLOW_TEMPLATE_ID =?", selectedProcedureGNOC.getProcedureGNOCId(), selectedFlowTemplate.getFlowTemplatesId());
            loadTemplate4Node(treeNodeMap.get(((ProcedureGNOC) selectedNodeProcedure.getData()).getId()));
            loadTemplate4Node(selectedNodeProcedure);
            MessageUtil.setInfoMessageFromRes("info.delete.suceess");
            logAction = LogUtils.addContent("", "Delete Success");
            logAction = LogUtils.addContent(logAction,
                    String.format("PROCEDURE_GNOC_ID: %d, FLOW_TEMPLATE_ID: %d",
                            selectedProcedureGNOC.getProcedureGNOCId(), selectedFlowTemplate.getFlowTemplatesId()));
            selectedFlowTemplate = null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logAction = LogUtils.addContent(logAction, "Delete Fail");
            logAction = LogUtils.addContent(logAction, e.getMessage());
            MessageUtil.setErrorMessageFromRes("error.delete.unsuceess");
        }
        LogUtils.writelog(new Date(), className, new Object() {
        }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.DELETE.name(), logAction);
    }

    public void addTempForProcedure() {
        if (selectedFlowTemplate == null) {
            return;
        }
        try {
            List<FlowTemplates> flowTemplates = flowTemplatesMap.get(selectedProcedureGNOC.getProcedureGNOCId());
            if (flowTemplates == null) {
                flowTemplates = new ArrayList<>();
            }
            flowTemplates.add(selectedFlowTemplate);
            flowTemplatesMap.put(selectedProcedureGNOC.getProcedureGNOCId(), new ArrayList<>(flowTemplates));
            new DaoSimpleService().execteNativeBulk("insert into PROCEDURE_GNOC_TEMPLATE (PROCEDURE_GNOC_ID, FLOW_TEMPLATE_ID, FLOW_TEMPLATE_ADD_TIME, FLOW_TEMPLATE_ADD_BY) VALUES (?,?,?,?)",
                    selectedProcedureGNOC.getProcedureGNOCId(), selectedFlowTemplate.getFlowTemplatesId(), new Date(), SessionWrapper.getCurrentUsername());
            TreeTable treeTable = (TreeTable) FacesContext.getCurrentInstance().getViewRoot().findComponent(":formc:procedureId");

            loadTemplate4Node(treeNodeMap.get(((ProcedureGNOC) selectedNodeProcedure.getData()).getId()));
            loadTemplate4Node(selectedNodeProcedure);

            MessageUtil.setInfoMessageFromRes("info.save.success");
            logAction = LogUtils.addContent("", "Add Success");
            logAction = LogUtils.addContent(logAction,
                    String.format("PROCEDURE_GNOC_ID: %d, FLOW_TEMPLATE_ID: %d",
                            selectedProcedureGNOC.getProcedureGNOCId(), selectedFlowTemplate.getFlowTemplatesId()));
            selectedFlowTemplate = null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            logAction = LogUtils.addContent(logAction, "Add Fail");
            logAction = LogUtils.addContent(logAction, e.getMessage());
            MessageUtil.setErrorMessageFromRes("error.save.unsuccess");
        }
        LogUtils.writelog(new Date(), className, new Object() {
        }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.CREATE.name(), logAction);
    }

    private void loadTemplateForTree(TreeNode treeNode) {
        if (treeNode == null) {
            return;
        }
        if (treeNode.getData() != null) {
            treeNodeMap.put(((ProcedureGNOC) treeNode.getData()).getId(), treeNode);
            loadTemplate4Node(treeNode);
        }
        if (!treeNode.isLeaf()) {
            for (TreeNode child : treeNode.getChildren()) {
                loadTemplateForTree(child);
            }
        }
    }

    private void loadTemplate4Node(TreeNode treeNode) {
        List<FlowTemplates> flowTemplates1 = flowTemplatesMap.get(((ProcedureGNOC) treeNode.getData()).getProcedureGNOCId());
        if (((ProcedureGNOC) treeNode.getData()).getFlowTemplates() == null) {
            ((ProcedureGNOC) treeNode.getData()).setFlowTemplates(flowTemplates1);
        } else {
            ((ProcedureGNOC) treeNode.getData()).getFlowTemplates().clear();
            if (flowTemplates1 != null && !flowTemplates1.isEmpty()) {
                flowTemplates1 = new ArrayList<>(flowTemplates1);
                ((ProcedureGNOC) treeNode.getData()).getFlowTemplates().addAll(flowTemplates1);
            }
        }
        List<FlowTemplates> flowTemplates = ((ProcedureGNOC) treeNode.getData()).getFlowTemplates();
        StringBuilder name = new StringBuilder();
        if (flowTemplates != null) {
            for (FlowTemplates flowTemplate : flowTemplates) {
                name.append(flowTemplate.getFlowTemplateName()).append(";");
            }
        }
        ((ProcedureGNOC) treeNode.getData()).setFlowTemplateName(name.toString());
    }

    public List<ProcedureGNOC> loadChild(Long procedureGNOCId) {
        String hqlSubNode = "select distinct pro from ProcedureGNOC pro where pro.parentId = ? and pro.isActive = 1 order by pro.procedureGNOCName asc";
        List<ProcedureGNOC> lstProcedureIdSub = new ProcedureGNOCServiceImpl().findList(hqlSubNode, -1, -1, procedureGNOCId);
        Collections.sort(lstProcedureIdSub, new Comparator<ProcedureGNOC>() {
            @Override
            public int compare(ProcedureGNOC o1, ProcedureGNOC o2) {
                if (o1.getGnocImpactSegmentId().compareTo(o2.getGnocImpactSegmentId()) == 0) {
                    if (o1.getGnocDeviceTypeId() != null && o2.getGnocDeviceTypeId() != null) {
                        if (o1.getGnocDeviceTypeId().compareTo(o2.getGnocDeviceTypeId()) == 0) {
                            Pattern pattern = Pattern.compile("^\\d\\.");
                            String n1 = pattern.matcher(o1.getProcedureGNOCName()).find() ? "0" + o1.getProcedureGNOCName() : o1.getProcedureGNOCName();
                            String n2 = pattern.matcher(o2.getProcedureGNOCName()).find() ? "0" + o2.getProcedureGNOCName() : o2.getProcedureGNOCName();
                            return n1.compareTo(n2);
                        } else {
                            return o1.getGnocDeviceTypeId().compareTo(o2.getGnocDeviceTypeId());
                        }
                    } else {
                        return 1;
                    }
                } else {
                    return o1.getGnocImpactSegmentId().compareTo(o2.getGnocImpactSegmentId());
                }
            }
        });
        return lstProcedureIdSub;
    }

    private void loadSubParent(List<ProcedureGNOC> lstProcedureId, TreeNode rootNode) {
        Collections.sort(lstProcedureId, new Comparator<ProcedureGNOC>() {
            @Override
            public int compare(ProcedureGNOC o1, ProcedureGNOC o2) {
                if (o1.getGnocImpactSegmentId().compareTo(o2.getGnocImpactSegmentId()) == 0) {
                    if (o1.getGnocDeviceTypeId() != null && o2.getGnocDeviceTypeId() != null) {
                        if (o1.getGnocDeviceTypeId().compareTo(o2.getGnocDeviceTypeId()) == 0) {
                            Pattern pattern = Pattern.compile("^\\d\\.");
                            String n1 = pattern.matcher(o1.getProcedureGNOCName()).find() ? "0" + o1.getProcedureGNOCName() : o1.getProcedureGNOCName();
                            String n2 = pattern.matcher(o2.getProcedureGNOCName()).find() ? "0" + o2.getProcedureGNOCName() : o2.getProcedureGNOCName();
                            return n1.compareTo(n2);
                        } else {
                            return o1.getGnocDeviceTypeId().compareTo(o2.getGnocDeviceTypeId());
                        }
                    } else {
                        return 1;
                    }
                } else {
                    return o1.getGnocImpactSegmentId().compareTo(o2.getGnocImpactSegmentId());
                }
            }
        });

        // Load data for group module
        if (!lstProcedureId.isEmpty()) {
            for (ProcedureGNOC procedureGNOC : lstProcedureId) {
                TreeNode subParent = new DefaultTreeNode(PARENT_NODE, procedureGNOC, rootNode);
                subParent.setSelectable(false);
                List<ProcedureGNOC> lstProcedureIdSub = loadChild(procedureGNOC.getProcedureGNOCId());
                if (lstProcedureIdSub != null && !lstProcedureIdSub.isEmpty()) {
                    for (ProcedureGNOC gnocSub : lstProcedureIdSub) {
                        DefaultTreeNode defaultTreeNode = new DefaultTreeNode(CHILD_NODE, gnocSub, subParent);
                        List<ProcedureGNOC> procedureGNOCS = loadChild(gnocSub.getProcedureGNOCId());
                        if (procedureGNOCS != null) {
                            for (ProcedureGNOC gnocSub2 : procedureGNOCS) {
                                new DefaultTreeNode(CHILD_NODE, gnocSub2, defaultTreeNode);
                            }
                        }
                    }
                }
            }
        }
    }

    private void loadTreeProcedureId() {
        try {
            String hql = "select distinct pro from ProcedureGNOC pro where pro.parentId is null and pro.isActive = 1 order by pro.gnocImpactSegmentId, pro.gnocDeviceTypeId, pro.procedureGNOCName asc";
            List<ProcedureGNOC> lstProcedureId = new ProcedureGNOCServiceImpl().findList(hql, -1, -1);
            rootProcedureId = new DefaultTreeNode(new ProcedureGNOC(), null);
            rootProcedureId.setSelectable(false);
            loadSubParent(lstProcedureId, rootProcedureId);

        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    public boolean canAddTempToProcedure() {
        try {
            List<String> userAdmins = Arrays.asList("quytv7", "anttt2", "minhut", "namlh38");
            if (userAdmins.contains(SessionWrapper.getCurrentUsername())) {
                return true;
            }
        } catch (Exception e) {
            logger.debug(e.getMessage(), e);
        }
        return false;
    }

    public StreamedContent onExport() throws Exception {
        String pathOut = CommonExport.getPathSaveFileExport(MessageUtil.getResourceBundleMessage("export.list.procedure.cr.file.name"));
        Workbook workbook = null;
        ServletContext ctx = (ServletContext) FacesContext.getCurrentInstance()
                .getExternalContext().getContext();

        String pathTemplate = ctx.getRealPath("/")
                + File.separator + "templates" + File.separator + MessageUtil.getResourceBundleMessage("key.template.export.list.procedures.cr");
        try {
            workbook = exportWorkbook(pathTemplate);
            try {
                FileOutputStream fileOut = new FileOutputStream(pathOut);
                workbook.write(fileOut);
                workbook.close();
            } catch (IOException e) {
                logger.error(e.getMessage(), e);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (workbook != null) {
                try {
                    workbook.close();
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        File file = new File(pathOut);
        return new DefaultStreamedContent(new FileInputStream(file), ".xlsx", file.getName());
    }

    private void putDataToRow(Workbook workbook, int sheetNumber, int columnNumber, Map<Integer, List<String>> mapData) {
        Font font = workbook.createFont();
        font.setFontName("Times New Roman");
        CellStyle cellStyleLeft = workbook.createCellStyle();
        cellStyleLeft.setAlignment(HSSFCellStyle.ALIGN_LEFT);
        cellStyleLeft.setVerticalAlignment(HSSFCellStyle.VERTICAL_CENTER);
        cellStyleLeft.setBorderLeft(HSSFCellStyle.BORDER_THIN);
        cellStyleLeft.setBorderBottom(HSSFCellStyle.BORDER_THIN);
        cellStyleLeft.setBorderRight(HSSFCellStyle.BORDER_THIN);
        cellStyleLeft.setBorderTop(HSSFCellStyle.BORDER_THIN);
        cellStyleLeft.setFont(font);
        cellStyleLeft.setWrapText(false);
        Cell cell;
        Row row;
        Sheet worksheet = workbook.getSheetAt(sheetNumber);
        int currentColumnData = columnNumber;
        for (Integer rowNumber : mapData.keySet()) {
            row = worksheet.createRow(rowNumber);
            for (int i = 0; i < mapData.get(rowNumber).size(); i++) {
                cell = row.createCell(currentColumnData);
                cell.setCellValue(mapData.get(rowNumber).get(i));
                cell.setCellStyle(cellStyleLeft);
                currentColumnData++;
            }
            currentColumnData = columnNumber;
        }
    }

    private Workbook exportWorkbook(String templatePath) {
        Workbook workbook = null;
        try {
            String pathTemplate = templatePath;
            InputStream fileTemplate = new FileInputStream(pathTemplate);
            workbook = WorkbookFactory.create(fileTemplate);
            HashMap<Integer, List<String>> mapData = new HashMap<>();

            List<String> listData;
            Integer row = 7;
            Integer i = 1;

            StringBuilder sb = new StringBuilder();
            sb.append("SELECT PGT.FLOW_TEMPLATE_ADD_BY, PGT.FLOW_TEMPLATE_ADD_TIME, ft.FLOW_TEMPLATE_NAME, a.* FROM (");
            sb.append("SELECT LPAD(' ', 4 * LEVEL) || PROCEDURE_GNOC_NAME \"Quy trình/Đầu việc\", ");
            sb.append("CASE WHEN LEVEL = '3' THEN 'Đầu việc'  WHEN LEVEL  = '2' THEN 'Quy trình' ELSE 'Other' END QT_DV, l.* ");
            sb.append("FROM PROCEDURE_GNOC l WHERE LEVEL <= 50 AND GNOC_IMPACT_SEGMENT_ID = 94 ");
            sb.append("START WITH l.PARENT_ID IS NULL CONNECT BY PRIOR l.PROCEDURE_GNOC_ID = l.PARENT_ID ORDER BY l.PROCEDURE_GNOC_NAME) a ");
            sb.append("JOIN PROCEDURE_GNOC_TEMPLATE PGT ON  PGT.PROCEDURE_GNOC_ID = a.PROCEDURE_GNOC_ID ");
            sb.append("RIGHT JOIN FLOW_TEMPLATES FT ON FT.FLOW_TEMPLATES_ID = PGT.FLOW_TEMPLATE_ID");

            List<Object[]> objects = (List<Object[]>) new DaoSimpleService().findListSQLAll(sb.toString());
            for (Object[] object : objects) {
                listData = new ArrayList<>();
                listData.add(String.valueOf(i));
                listData.add(object[2] != null ? object[2].toString() : "");
                listData.add(object[6] != null ? object[6].toString() : "");
                listData.add(object[3] != null ? object[3].toString() : "");
                listData.add(object[4] != null ? object[4].toString() : "");
                listData.add(object[0] != null ? object[0].toString() : "");
                listData.add(object[1] != null ? new SimpleDateFormat("dd/MM/yyyy HH:mm:ss").format(new Date()) : "");
                listData.add(object[8] != null ? object[8].toString() : "");
                listData.add(object[11] != null ? object[11].toString() : "");
                listData.add(object[20] != null ? object[20].toString() : "");
                listData.add(object[21] != null ? object[21].toString() : "");
                listData.add(object[22] != null ? object[22].toString() : "");
                mapData.put(row, listData);
                row++;
                i++;
            }
            putDataToRow(workbook, 0, 0, mapData);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return workbook;
    }

    public TreeNode getRootProcedureId() {
        return rootProcedureId;
    }

    public void setRootProcedureId(TreeNode rootProcedureId) {
        this.rootProcedureId = rootProcedureId;
    }

    public FlowTemplates getSelectedFlowTemplate() {
        return selectedFlowTemplate;
    }

    public void setSelectedFlowTemplate(FlowTemplates selectedFlowTemplate) {
        this.selectedFlowTemplate = selectedFlowTemplate;
    }

    public ProcedureGNOC getSelectedProcedureGNOC() {
        return selectedProcedureGNOC;
    }

    public void setSelectedProcedureGNOC(ProcedureGNOC selectedProcedureGNOC) {
        this.selectedProcedureGNOC = selectedProcedureGNOC;
    }

    public TreeNode getSelectedNodeProcedure() {
        return selectedNodeProcedure;
    }

    public void setSelectedNodeProcedure(TreeNode selectedNodeProcedure) {
        this.selectedNodeProcedure = selectedNodeProcedure;
    }

    public static void main(String[] args) {

    }
}