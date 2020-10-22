package com.viettel.it.controller;

import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.ItActionLog;
import com.viettel.it.model.ItCommandLog;
import com.viettel.it.object.XmlModel;
import com.viettel.it.persistence.ItActionLogServiceImpl;
import com.viettel.it.persistence.ItCommandLogServiceImpl;
import com.viettel.it.util.Config;
import com.viettel.it.util.MessageUtil;
import org.apache.log4j.Logger;
import org.jdom2.Document;
import org.jdom2.Element;
import org.jdom2.input.SAXBuilder;
import org.primefaces.context.RequestContext;
import org.primefaces.event.NodeCollapseEvent;
import org.primefaces.event.NodeExpandEvent;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.DefaultTreeNode;
import org.primefaces.model.StreamedContent;
import org.primefaces.model.TreeNode;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.Serializable;
import java.io.StringReader;
import java.util.*;

/**
 * Created by hanhnv68 on 9/9/2017.
 */
@ViewScoped
@ManagedBean
public class ItHistoryController implements Serializable {

    private static final Logger logger = Logger.getLogger(ItHistoryController.class);

    private LazyDataModelBaseNew<ItActionLog, Long> lazyActionLog;
    private List<ItCommandLog> commandLogList;
    private StreamedContent fileCommandLog;
    private ItActionLog selectedActionLog;
    private ItCommandLog selectedCmdLog;
    private List<List<String>> sqlDataTables;
    private List<String> columnsName;
    private TreeNode xmlDatas;

    @PostConstruct
    public void onStart() {
        try {
            xmlDatas = new DefaultTreeNode();
            selectedCmdLog = new ItCommandLog();

            LinkedHashMap orders = new LinkedHashMap();
            orders.put("startTime", "DESC");
            orders.put("endTime", "DESC");
            orders.put("status", "DESC");
            orders.put("userRun", "DESC");
//            orders.put("action.name", "ASC");
            lazyActionLog = new LazyDataModelBaseNew<ItActionLog, Long>(new ItActionLogServiceImpl(), null, orders);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void prepareViewLog(ItCommandLog cmdLog) {

        selectedCmdLog = cmdLog;
        sqlDataTables = new ArrayList<>();
        columnsName = new ArrayList<>();
        if (selectedCmdLog != null) {
            try {
                if (selectedCmdLog.getCommandDetail() == null) {
                    RequestContext.getCurrentInstance().update("formLogDetail");
                    RequestContext.getCurrentInstance().execute("PF('dlgCmdLogDetail').show()");
                } else {
                    if (selectedCmdLog.getCommandDetail().getProtocol().equals(Config.PROTOCOL_SQL)
                            && selectedCmdLog.getCommand().toLowerCase().startsWith("select")) {
                        List<String> tbsDatas = Arrays.asList(selectedCmdLog.getLog().trim().split("\\{CRLF}"));
                        if (tbsDatas != null && !tbsDatas.isEmpty()) {
                            columnsName = Arrays.asList(tbsDatas.get(0).trim().split("\\{,}"));

                            if (tbsDatas.size() > 1) {
                                for (int i = 1; i < tbsDatas.size(); i++) {
                                    List<String> rowData = Arrays.asList(tbsDatas.get(i).trim().split("\\{,}"));
                                    sqlDataTables.add(rowData);
                                }
                            }
                        }
                        RequestContext.getCurrentInstance().update("formLogDetail");
                        RequestContext.getCurrentInstance().execute("PF('dlgSqlCmdLog').show()");
                    } else if (selectedCmdLog.getCommandDetail().getProtocol().equals(Config.PROTOCOL_WEBSERVICE)
                            || selectedCmdLog.getCommandDetail().getProtocol().equals(Config.PROTOCOL_EXCHANGE)) {
                        xmlDatas = buildTreeNodeData(cmdLog.getLog());
                        if (xmlDatas == null) {
                            xmlDatas = new DefaultTreeNode(new XmlModel("", ""), null);
                        }
                        RequestContext.getCurrentInstance().update("formLogDetail");
                        RequestContext.getCurrentInstance().execute("PF('dlgXmlCmdLog').show()");
                    } else {
                        RequestContext.getCurrentInstance().update("formLogDetail");
                        RequestContext.getCurrentInstance().execute("PF('dlgCmdLogDetail').show()");
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }


        } else {
            MessageUtil.setInfoMessageFromRes("datatable.empty");
        }
    }



    private TreeNode buildTreeNodeData(String xmlData) {
        TreeNode root = null;
        xmlData = xmlData.replaceAll("&"," and ");
        try {
            SAXBuilder saxBuilder = new SAXBuilder();
            Document doc = saxBuilder.build(new StringReader(xmlData));
            Element rootElement = doc.getRootElement();

            root = new DefaultTreeNode(new XmlModel("root", ""), null);
            root.setExpanded(true);
            TreeNode rootElementNode = new DefaultTreeNode(new XmlModel(rootElement.getName(), ""), root);
            rootElementNode.setExpanded(true);
            if (rootElement.getChildren() != null && !rootElement.getChildren().isEmpty()) {
                List<Element> childs = rootElement.getChildren();
                for (Element child : childs) {
                    recurBuildTreeNode(child, rootElementNode);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return root;
    }

    private void recurBuildTreeNode(Element element, TreeNode treeNode) {
        try {
//            TreeNode curNode = new DefaultTreeNode(new XmlModel(element.getName(), ""), treeNode);
            if (element.getChildren() != null && !element.getChildren().isEmpty()) {
                TreeNode curNode = new DefaultTreeNode(new XmlModel(element.getName(), ""), treeNode);
                curNode.setExpanded(true);
                List<Element> elements = element.getChildren();
                for (Element child : elements) {
                    recurBuildTreeNode(child, curNode);
                }
            } else {
                new DefaultTreeNode(new XmlModel(element.getName(), element.getText()), treeNode);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void nodeExpand(NodeExpandEvent event) {
        event.getTreeNode().setExpanded(true);
    }

    public void nodeCollapse(NodeCollapseEvent event) {
        event.getTreeNode().setExpanded(false);
    }

    public void downloadLog(ItActionLog obj) {
        try {
            getCmdLog(obj);
            StringBuilder commandLog = new StringBuilder();
            String filename = "log";
            if ( obj.getAction() != null) {
                filename = obj.getAction().getName().trim().replaceAll(" +", "_");
            }
            boolean isSqlImpact = false;
            if (commandLogList != null && !commandLogList.isEmpty()) {

                if (!commandLogList.isEmpty()) {
                    ItCommandLog cmdLog;
                    for (int i = 0; i < commandLogList.size(); i++) {
                        cmdLog = commandLogList.get(i);
                        commandLog.append("\r\n");
                        if (cmdLog.getLogType().equals(Config.COMMAND_LOG_TYPE.IMPACT.value)) {
                            /*20180704_hoangnd_fix_bug_da_ngon_ngu_start*/
                            commandLog.append(MessageUtil.getResourceBundleMessage("label.action.command") + ": \r\n").append(cmdLog.getCommand()).append("\r\n");
                            /*20180704_hoangnd_fix_bug_da_ngon_ngu_end*/
                        } else {
                            /*20180704_hoangnd_fix_bug_da_ngon_ngu_start*/
                            commandLog.append(MessageUtil.getResourceBundleMessage("label.log.command") + ": \r\n").append(cmdLog.getCommand()).append("\r\n");
                            /*20180704_hoangnd_fix_bug_da_ngon_ngu_end*/
                        }
                        if(cmdLog.getLog() != null) {
                            commandLog.append(cmdLog.getLog().replace("{,}", ",").replace("{CRLF}", "\r\n")).append("\r\n ------------------------------------------ \r\n");
                        }
                        if (Config.PROTOCOL_SQL.equalsIgnoreCase(commandLogList.get(i).getCommandDetail().getProtocol())
                                && (commandLogList.get(i).getLogType().longValue() == 0l)) {
                            isSqlImpact = true;
                        }
                    }
                }
            }

            InputStream stream = new ByteArrayInputStream(commandLog.toString().getBytes());
            fileCommandLog = new DefaultStreamedContent(stream, "txt", filename + (isSqlImpact ? ".csv" : ".txt"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void getCmdLog(ItActionLog obj) {
        try {
            commandLogList = new ArrayList<>();
            if (obj != null) {
                selectedActionLog = obj;
            }
            if (selectedActionLog != null) {
                Map<String, Object> filter = new HashMap<>();
                filter.put("actionLog.id", selectedActionLog.getId());
                LinkedHashMap<String, String> orders = new LinkedHashMap<>();
                orders.put("orderRun", "ASC");
                orders.put("logType", "ASC");
                commandLogList = new ItCommandLogServiceImpl().findList(filter, orders);
            }
        } catch (Exception e) {
            commandLogList = new ArrayList<>();
            logger.error(e.getMessage(), e);
        }
    }

    public void pollGetCmdLog() {
        try {
            logger.info("start get command log");

            commandLogList = new ArrayList<>();
            if (selectedActionLog != null) {
                Map<String, Object> filter = new HashMap<>();
                filter.put("actionLog.id", selectedActionLog.getId());
                LinkedHashMap<String, String> orders = new LinkedHashMap<>();
                orders.put("orderRun", "ASC");
                orders.put("logType", "ASC");
                commandLogList = new ItCommandLogServiceImpl().findList(filter, orders);
            }
        } catch (Exception e) {
            commandLogList = new ArrayList<>();
            logger.error(e.getMessage(), e);
        }
    }

    public LazyDataModelBaseNew<ItActionLog, Long> getLazyActionLog() {
        return lazyActionLog;
    }

    public void setLazyActionLog(LazyDataModelBaseNew<ItActionLog, Long> lazyActionLog) {
        this.lazyActionLog = lazyActionLog;
    }

    public List<ItCommandLog> getCommandLogList() {
        return commandLogList;
    }

    public void setCommandLogList(List<ItCommandLog> commandLogList) {
        this.commandLogList = commandLogList;
    }

    public StreamedContent getFileCommandLog() {
        return fileCommandLog;
    }

    public void setFileCommandLog(StreamedContent fileCommandLog) {
        this.fileCommandLog = fileCommandLog;
    }

    public ItActionLog getSelectedActionLog() {
        return selectedActionLog;
    }

    public void setSelectedActionLog(ItActionLog selectedActionLog) {
        this.selectedActionLog = selectedActionLog;
    }

    public ItCommandLog getSelectedCmdLog() {
        return selectedCmdLog;
    }

    public void setSelectedCmdLog(ItCommandLog selectedCmdLog) {
        this.selectedCmdLog = selectedCmdLog;
    }

    public List<List<String>> getSqlDataTables() {
        return sqlDataTables;
    }

    public void setSqlDataTables(List<List<String>> sqlDataTables) {
        this.sqlDataTables = sqlDataTables;
    }

    public List<String> getColumnsName() {
        return columnsName;
    }

    public void setColumnsName(List<String> columnsName) {
        this.columnsName = columnsName;
    }

    public TreeNode getXmlDatas() {
        return xmlDatas;
    }

    public void setXmlDatas(TreeNode xmlDatas) {
        this.xmlDatas = xmlDatas;
    }
}
