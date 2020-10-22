package com.viettel.it.thread;

import com.viettel.bean.ResultGetAccount;
import com.viettel.it.model.*;
import com.viettel.it.object.ExportParamOptionObject;
import com.viettel.it.object.ParamExportDbValObject;
import com.viettel.it.object.ParamExportObject;
import com.viettel.it.persistence.NodeServiceImpl;
import com.viettel.it.persistence.ParamExportDbServiceImpl;
import com.viettel.it.persistence.ParamInputServiceImpl;
import com.viettel.it.persistence.ParamValueServiceImpl;
import com.viettel.it.util.*;
import com.viettel.passprotector.PassProtector;
import com.viettel.util.AppConfig;
import com.viettel.util.Constant;
import com.viettel.it.util.SecurityService;
import com.viettel.util.SessionUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

public class GetParamExportTableThread extends GetParamThread {

    private SimpleDateFormat df = new SimpleDateFormat("yyyyMM");

    public GetParamExportTableThread(FlowRunAction flowRunAction,
                                     Node databaseNode, NodeRun nodeRun, Integer cycleMonths,
                                     String urlQltn, String usernameQltn, String passwordQltn,
                                     long keySession, Logger logger, Map<String, String> mapConfigSecurity, Map<String, ResultGetAccount> mapPassGet) {
        super(flowRunAction, databaseNode, nodeRun, cycleMonths, urlQltn, usernameQltn,
                passwordQltn, keySession, logger, mapConfigSecurity, mapPassGet);
        // TODO Auto-generated constructor stub
    }

    protected static final Logger logger = LoggerFactory.getLogger(GetParamExportTableThread.class);

    @Override
    public void run() {
        logger.info(">>>>>>>>>>>>>>>> START RUN GET MOP EXPORT TABLE DATABASE: " + databaseNode.getNodeIp() + "_virtualIP: " + databaseNode.getNodeIpVirtual());
        try {
            // get server node
            Node serverNode = getServerNode(databaseNode.getNodeIp());
            if (serverNode == null) {
                return;
            }

            NodeAccount vtAdminAcc = getAccount(null, serverNode.getServerId(), Config.APP_TYPE.SERVER.value, true, 2l);
            if (vtAdminAcc == null) {
                logger.error("ERROR GET ACCOUNT VT_ADMIN FOR NODE: " + serverNode.getNodeIp());
                return;
            }
            NodeAccount rootAcc = getAccount(null, serverNode.getServerId(), Config.APP_TYPE.SERVER.value, false, 1l);
            if (rootAcc == null) {
                logger.error("ERROR GET ACCOUNT ROOT FOR NODE: " + serverNode.getNodeIp());
                return;
            }
            NodeAccount impactSqlAcc = getAccImpactDefault(databaseNode.getServerId());
            if (impactSqlAcc == null) {
                logger.error("ERROR GET ACCOUNT TDHT FOR NODE: " + databaseNode.getNodeIp());
                return;
            }

            // get user sql plus
            String usernameRunSqlPlus = null;
            JSchSshUtil sshClient = null;
            try {
                sshClient = new JSchSshUtil(databaseNode.getEffectIp(), 22, vtAdminAcc.getUsername(), vtAdminAcc.getPassword(),
                        null, null, 120000, false, "N/A", "N/A", "N/A");
                sshClient.connect();
                Result log = sshClient.sendLineWithTimeOutAdvance("ps -ef |grep pmon_" + databaseNode.getServiceName() + " | grep -v grep | awk '{print $1}'", 360000);
                logger.info("log username sql plus: " + log.getResult());
                usernameRunSqlPlus = (String) paserData(log.getResult(), 1, 0);
                if (usernameRunSqlPlus == null || usernameRunSqlPlus.trim().isEmpty()) {
                    logger.error("ERROR CANNOT GET ACCOUNT IMPACT SQL PLUS");
                    return;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                try {
                    if (sshClient != null) {
                        sshClient.disconnect();
                    }
                } catch (Exception e2) {
                    logger.error(e2.getMessage(), e2);
                }
            }


            List<ParamExportObject> lstParamExport = getParamExportDb();
            List<ParamExportDb> lstParamExportRefer = new ArrayList<>();
            ParamExportDbValObject paramExportDbVal = new ParamExportDbValObject();
            for (ParamExportObject param : lstParamExport) {
                procSingleExportRow(param, vtAdminAcc, impactSqlAcc, rootAcc,
                        paramExportDbVal, lstParamExportRefer, usernameRunSqlPlus);
            }

            // save param value
            saveParam(paramExportDbVal, lstParamExportRefer);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.info(">>>>>>> finish export node: " + databaseNode.getNodeIp());
            try {
                int currThreadRuning = ManagerThreadGetParamIns.getInstance().getMapSessionThreadStatus().get(keySession).decrementAndGet();
                if (currThreadRuning == 0) {
                    setFinishBuildDT(flowRunAction);
                }
            } catch (Exception e2) {
                logger.error(e2.getMessage(), e2);
            }
        }
    }

    private Node getServerNode(String serverIp) {
        Node server;
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("nodeIp", serverIp);
            filters.put("vendor.vendorId", Config.APP_TYPE.SERVER.value);
            filters.put("active", Constant.status.active);

            return new NodeServiceImpl().findList(filters).get(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            server = null;
            logger.error("ERROR GET SERVER NODE FOR IP: " + serverIp);
        }
        return server;
    }

    private void procSingleExportRow(ParamExportObject paramExport,
                                     NodeAccount vtAdminAcc, NodeAccount impactSqlAcc,
                                     NodeAccount rootAcc, ParamExportDbValObject paramVal,
                                     List<ParamExportDb> lstParamExportRefer, String usernameRunSqlPlus) {
        if (paramExport != null) {
            ExportParamOptionObject paramOption = buildParamOption(paramExport);
            if (paramOption != null) {
                if (checkDumpSize(paramExport, vtAdminAcc, impactSqlAcc, rootAcc, paramOption, usernameRunSqlPlus)) {
                    Connection conn = null;
                    ParamExportDb paramExportRefer = null;
                    if ("partition".equalsIgnoreCase(paramExport.getObject().toLowerCase().trim())) {
                        try {

                            List<String> lstOwnerTableName = getListTbNameOwnerPar(paramOption, impactSqlAcc);
                            if (lstOwnerTableName.isEmpty()) {
                                logger.error("GET TABLE AND OWNER EMPTY ".concat(paramExport.getDirectory()));
                                return;
                            }
                            String dumpfile = null;
                            String logFile = null;
                            List<String> lstVal = new ArrayList<>();
                            for (String o : lstOwnerTableName) {
                                lstVal = Arrays.asList(o.trim().split(";"));

                                //20181023_tudn_start load pass security
//								paramVal.getStrValParamConnect().append(impactSqlAcc.getUsername().concat("/").concat(impactSqlAcc.getPassword())).append(";");
                                paramVal.getStrValParamConnect().append(impactSqlAcc.getUsername().concat("/").concat(impactSqlAcc.getPassword())).append(";");
                                //20181023_tudn_end load pass security
                                paramVal.getStrValParamOptional().append(paramOption.getExclude().concat(" "))
                                        .append(paramOption.getCompresstion().concat(" "))
                                        .append(paramOption.getContent().concat(" "))
                                        .append((paramOption.getParallel() == null ? "" : paramOption.getParallel()).concat(" "))
                                        .append(paramOption.getOther().concat(" "))
                                        .append(";");
                                paramVal.getStrValParamOwnerTableName().append(lstVal.get(0).concat(".").concat(lstVal.get(1).concat(":").concat(lstVal.get(2)))).append(";");
                                paramVal.getStrValParamDirectory().append(paramExport.getDirectory()).append(";");
                                if (paramExport.getParallel() == null) {
                                    dumpfile = databaseNode.getServiceName().concat("_").concat(lstVal.get(0)).concat("_").concat(lstVal.get(1)).concat("_").concat(lstVal.get(2)).concat(".dmp");
                                } else {
                                    dumpfile = databaseNode.getServiceName().concat("_").concat(lstVal.get(0)).concat("_").concat(lstVal.get(1)).concat("_").concat(lstVal.get(2)).concat("_%U.dmp");
                                }
                                paramVal.getStrValParamDumpfile().append(dumpfile).append(";");
                                logFile = databaseNode.getServiceName().concat("_").concat(lstVal.get(0)).concat("_").concat(lstVal.get(1)).concat("_").concat(lstVal.get(2)).concat(".log");
                                paramVal.getStrValParamLogfile().append(logFile).append(";");

                                paramExportRefer = new ParamExportDb();
                                paramExportRefer.setContent(paramExport.getContent());
                                paramExportRefer.setDbId(databaseNode.getServerId());
                                paramExportRefer.setFlowRunId(flowRunAction.getFlowRunId());
                                paramExportRefer.setIpFtp(paramExport.getIpScp());
                                paramExportRefer.setLogFile(logFile);
                                paramExportRefer.setOwner(lstVal.get(0));
                                paramExportRefer.setPar(lstVal.get(2));
                                paramExportRefer.setParamExportQltnId(paramExport.getDbParamExportId());
                                paramExportRefer.setPassFtp(paramExport.getPassScp());
                                paramExportRefer.setPathDump(paramOption.getPathDumName());
                                paramExportRefer.setPathFtp(paramExport.getPathScp());
                                paramExportRefer.setPortFtp(22);
                                paramExportRefer.setTab(lstVal.get(1));
                                paramExportRefer.setTablespaceName(paramOption.getTbsName());
                                paramExportRefer.setUserFtp(paramExport.getUserScp());
                                //20181023_tudn_start load pass security
//								paramExportRefer.setPasswordRoot(rootAcc.getPassword());
                                paramExportRefer.setPasswordRoot(rootAcc.getPassword());
                                //20181023_tudn_end load pass security
                                paramExportRefer.setUserSqlPlus(usernameRunSqlPlus);

                                lstParamExportRefer.add(paramExportRefer);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        } finally {
                            try {
                                if (conn != null) {
                                    conn.close();
                                }
                            } catch (Exception e2) {
                                logger.error(e2.getMessage(), e2);
                            }
                        }

                    } else {
                        try {
                            List<String> lstOwnerTableName = getListTbNameOwnerTable(paramOption, impactSqlAcc);
                            if (lstOwnerTableName.isEmpty()) {
                                logger.error("GET TABLE AND AND OWNER EMPTY ".concat(paramExport.getDirectory()));
                                return;
                            }
                            String dumpfile = null;
                            List<String> lstVal = new ArrayList<>();
                            String logFile = null;
                            for (String o : lstOwnerTableName) {
                                lstVal = Arrays.asList(o.trim().split(";"));
                                //20181023_tudn_start load pass security
//								paramVal.getStrValParamConnect().append(impactSqlAcc.getUsername().concat("/").concat(impactSqlAcc.getPassword())).append(";");
                                paramVal.getStrValParamConnect().append(impactSqlAcc.getUsername().concat("/").concat(impactSqlAcc.getPassword())).append(";");
                                //20181023_tudn_end load pass security
                                paramVal.getStrValParamOptional().append(paramOption.getExclude().concat(" "))
                                        .append(paramOption.getCompresstion().concat(" "))
                                        .append(paramOption.getContent().concat(" "))
                                        .append(paramOption.getOther().concat(" "))
                                        .append(paramOption.getParallel().concat(" "))
                                        .append(paramOption.getOther().concat(" "))
                                        .append(";");
                                paramVal.getStrValParamOwnerTableName().append(lstVal.get(0).concat(".").concat(lstVal.get(1))).append(";");
                                paramVal.getStrValParamDirectory().append(paramExport.getDirectory()).append(";");
                                if (paramExport.getParallel() == null) {
                                    dumpfile = databaseNode.getServiceName().concat("_").concat(lstVal.get(0)).concat("_").concat(lstVal.get(1)).concat(".dmp");
                                } else {
                                    dumpfile = databaseNode.getServiceName().concat("_").concat(lstVal.get(0)).concat("_").concat(lstVal.get(1)).concat("_%U.dmp");
                                }
                                paramVal.getStrValParamDumpfile().append(dumpfile).append(";");
                                logFile = databaseNode.getServiceName().concat("_").concat(lstVal.get(0)).concat("_").concat(lstVal.get(1)).concat(".log");
                                paramVal.getStrValParamLogfile().append(logFile).append(";");

                                paramExportRefer = new ParamExportDb();
                                paramExportRefer.setContent(paramExport.getContent());
                                paramExportRefer.setDbId(databaseNode.getServerId());
                                paramExportRefer.setFlowRunId(flowRunAction.getFlowRunId());
                                paramExportRefer.setIpFtp(paramExport.getIpScp());
                                paramExportRefer.setLogFile(logFile);
                                paramExportRefer.setOwner(lstVal.get(0));
                                paramExportRefer.setParamExportQltnId(paramExport.getDbParamExportId());
                                paramExportRefer.setPassFtp(paramExport.getPassScp());
                                paramExportRefer.setPathDump(paramOption.getPathDumName());
                                paramExportRefer.setPathFtp(paramExport.getPathScp());
                                paramExportRefer.setPortFtp(22);
                                paramExportRefer.setTab(lstVal.get(1));
                                paramExportRefer.setTablespaceName(paramOption.getTbsName());
                                paramExportRefer.setUserFtp(paramExport.getUserScp());
                                //20181023_tudn_start load pass security
//								paramExportRefer.setPasswordRoot(rootAcc.getPassword());
                                paramExportRefer.setPasswordRoot(rootAcc.getPassword());
                                //20181023_tudn_end load pass security
                                paramExportRefer.setUserSqlPlus(usernameRunSqlPlus);

                                lstParamExportRefer.add(paramExportRefer);
                            }
                        } catch (Exception e) {
                            logger.error(e.getMessage(), e);
                        } finally {
                            try {
                                conn.close();
                            } catch (Exception e2) {
                                logger.error(e2.getMessage(), e2);
                            }
                        }
                    } // end if else
                }
            } else {
                logger.error("ERROR CANNOT GET OPTION EXPORT PARAMETER: " + databaseNode.getNodeIp());
            }
        }
    }

    private void saveParam(ParamExportDbValObject paramVal, List<ParamExportDb> lstParamExportRefer) {
        try {
            Map<String, Object> filters = new HashMap<String, Object>();
            Map<String, String> orders = new HashMap<>();
            orders.put("paramCode", "ASC");

            ParamInput paramConnect = null;
            ParamInput paramOptional = null;
            ParamInput paramOwnerTableName = null;
            ParamInput paramDirectory = null;
            ParamInput paramDumpfile = null;
            ParamInput paramLogfile = null;
            try {
                filters.put("paramCode", Config.EXPORT_DB_CONNECT);
                paramConnect = new ParamInputServiceImpl().findList(filters, orders).get(0);

                filters.put("paramCode", Config.EXPORT_DB_OWNER_TABLE_NAME);
                paramOwnerTableName = new ParamInputServiceImpl().findList(filters, orders).get(0);

                filters.put("paramCode", Config.EXPORT_DB_OPTIONAL);
                paramOptional = new ParamInputServiceImpl().findList(filters, orders).get(0);

                filters.put("paramCode", Config.EXPORT_DB_DIRECTORY);
                paramDirectory = new ParamInputServiceImpl().findListExac(filters, null).get(0);

                filters.put("paramCode", Config.EXPORT_DB_DUMPFILE);
                paramDumpfile = new ParamInputServiceImpl().findList(filters, null).get(0);

                filters.put("paramCode", Config.EXPORT_DB_LOG_FILE);
                paramLogfile = new ParamInputServiceImpl().findList(filters, null).get(0);

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }

            ParamValue valParamConnect = null;
            ParamValue valParamOptional = null;
            ParamValue valParamOwnerTableName = null;
            ParamValue valParamDirectory = null;
            ParamValue valParamDumpfile = null;
            ParamValue valParamLogfile = null;


            if (paramConnect != null) {
                valParamConnect = new ParamValue(paramConnect, paramConnect.getParamCode(), new Date(),
                        paramVal.getStrValParamConnect().toString().endsWith(";") ? paramVal.getStrValParamConnect().substring(0, paramVal.getStrValParamConnect().length() - 1) : paramVal.getStrValParamConnect().toString(), nodeRun);
            }
            if (paramDirectory != null) {
                valParamDirectory = new ParamValue(paramDirectory, paramDirectory.getParamCode(), new Date(),
                        paramVal.getStrValParamDirectory().toString().endsWith(";") ? paramVal.getStrValParamDirectory().substring(0, paramVal.getStrValParamDirectory().length() - 1) : paramVal.getStrValParamDirectory().toString(), nodeRun);
            }
            if (paramDumpfile != null) {
                valParamDumpfile = new ParamValue(paramDumpfile, paramDumpfile.getParamCode(), new Date(),
                        paramVal.getStrValParamDumpfile().toString().endsWith(";") ? paramVal.getStrValParamDumpfile().substring(0, paramVal.getStrValParamDumpfile().length() - 1) : paramVal.getStrValParamDumpfile().toString(), nodeRun);
            }
            if (paramLogfile != null) {
                valParamLogfile = new ParamValue(paramLogfile, paramLogfile.getParamCode(), new Date(),
                        paramVal.getStrValParamLogfile().toString().endsWith(";") ? paramVal.getStrValParamLogfile().substring(0, paramVal.getStrValParamLogfile().length() - 1) : paramVal.getStrValParamLogfile().toString(), nodeRun);
            }
            if (paramOptional != null) {
                valParamOptional = new ParamValue(paramOptional, paramOptional.getParamCode(), new Date(),
                        paramVal.getStrValParamOptional().toString().endsWith(";") ? paramVal.getStrValParamOptional().substring(0, paramVal.getStrValParamOptional().length() - 1) : paramVal.getStrValParamOptional().toString(), nodeRun);
            }
            if (paramOwnerTableName != null) {
                valParamOwnerTableName = new ParamValue(paramOwnerTableName, paramOwnerTableName.getParamCode(), new Date(),
                        paramVal.getStrValParamOwnerTableName().toString().endsWith(";") ? paramVal.getStrValParamOwnerTableName().substring(0, paramVal.getStrValParamOwnerTableName().length() - 1) : paramVal.getStrValParamOwnerTableName().toString(), nodeRun);
            }

            List<ParamValue> lstParamValSave = new ArrayList<>();
            lstParamValSave.add(valParamConnect);
            lstParamValSave.add(valParamDirectory);
            lstParamValSave.add(valParamDumpfile);
            lstParamValSave.add(valParamLogfile);
            lstParamValSave.add(valParamOptional);
            lstParamValSave.add(valParamOwnerTableName);

            new ParamValueServiceImpl().saveOrUpdate(lstParamValSave);

            // save param export reference
            new ParamExportDbServiceImpl().saveOrUpdate(lstParamExportRefer);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private List<String> getListTbNameOwnerPar(ExportParamOptionObject paramOption, NodeAccount impactSqlAcc) {
        List<String> lstOwnerTableName = new ArrayList<>();
        Connection conn = null;
        try {
            conn = getConnection(databaseNode.getJdbcUrl(), impactSqlAcc.getUsername(), impactSqlAcc.getPassword());
            PreparedStatement prepare;
            String sql = "select table_owner||';'||table_name||';'||partition_name as result_data from dba_tab_partitions where "
                    + paramOption.getTbsNameCondition() + " " + paramOption.getTbsOwnerCondition() + " " + paramOption.getTableNameCondition1() + " "
                    + paramOption.getParNameCondition() + " " + paramOption.getRotateMonthCondition()
                    + " and table_name not like 'BIN$%' and table_owner||'.'||table_name||'.'||partition_name not in "
                    + "(select owner||'.'||table_name||'.'||partition_name from sysman.log_export where "
                    + paramOption.getTbsNameCondition() + " " + paramOption.getOwnerCondition() + " "
                    + paramOption.getTableNameCondition1() + " " + paramOption.getParNameCondition()
                    + " and create_date > sysdate -30 and status like 'OK') order by table_owner,table_name,partition_name";
            logger.info(sql);
            prepare = conn.prepareStatement(sql);
            prepare.setFetchSize(1000);
            ResultSet rs = prepare.executeQuery();

            while (rs.next()) {
                lstOwnerTableName.add(rs.getString("result_data"));
            }
            rs.close();
            prepare.close();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstOwnerTableName;
    }

    private List<String> getListTbNameOwnerTable(ExportParamOptionObject paramOption, NodeAccount impactSqlAcc) {
        List<String> lstOwnerTableName = new ArrayList<>();
        Connection conn = null;
        try {
            //20181023_tudn_start load pass security
//			conn = getConnection(databaseNode.getJdbcUrl(), impactSqlAcc.getUsername(), PasswordEncoder.decrypt(impactSqlAcc.getPassword()));
            conn = getConnection(databaseNode.getJdbcUrl(), impactSqlAcc.getUsername(), impactSqlAcc.getPassword());
            //20181023_tudn_end load pass security
            String sql = "select owner||';'||object_name as result_data from dba_objects where object_type like 'TAB%' "
                    + paramOption.getOwnerCondition() + " " + paramOption.getTableNameCondition2()
                    + " and object_name not in "
                    + "(SELECT TABLE_NAME FROM sysman.log_export WHERE create_date > sysdate -30 "
                    + paramOption.getOwnerCondition() + " " + paramOption.getTableNameCondition1()
                    + " and partition_name is null AND status LIKE 'OK') GROUP BY owner, object_name order by owner,object_name";
            logger.info(sql);
            PreparedStatement prepare;
            prepare = conn.prepareStatement(sql);
            prepare.setFetchSize(1000);
            ResultSet rs = prepare.executeQuery();

            while (rs.next()) {
                lstOwnerTableName.add(rs.getString("result_data"));
            }
            rs.close();
            prepare.close();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return lstOwnerTableName;
    }

    private boolean checkDumpSize(ParamExportObject paramExport,
                                  NodeAccount vtAdminAcc, NodeAccount impacSqlAcc,
                                  NodeAccount rootAcc, ExportParamOptionObject paramOption, String usernameRunSqlPlus) {
        boolean check = false;
        JSchSshUtil sshClient = null;
        try {
            if (paramExport.getPathDumpPrefix() == null
                    || paramExport.getPathDumpPrefix().trim().isEmpty()) {
                logger.error("ERROR PATH DUMP PREFIX IS EMPTY");
            } else if (!paramExport.getPathDumpPrefix().trim().startsWith("/")) {
                logger.error("ERROR PATH DUMP PREFIX IS NOT START WITH '/' SYMPOL");
            } else {
                String monthPath = "/".concat(Arrays.asList(paramExport.getPathDumpPrefix().trim().split("/")).get(1));
                sshClient = new JSchSshUtil(databaseNode.getEffectIp(), 22, vtAdminAcc.getUsername(), vtAdminAcc.getPassword(),
                        null, null, 120000, false, "N/A", "N/A", "N/A");
                sshClient.connect();

                String cmdCheck = "df -kP | grep ".concat(monthPath).concat(" | awk '{print $4}'");
                Result log = sshClient.sendLineWithTimeOutAdvance(cmdCheck, 120000);
                if (log != null && !log.getResult().isEmpty()) {
                    Double dumpSize = 0d;
                    if (getdumpSize(log.getResult()) != null) {
                        dumpSize = getdumpSize(log.getResult()) / (1024 * 1024);
                    } else {
                        cmdCheck = "df -k | grep ".concat(monthPath).concat(" | awk '{print $4}'");
                        log = sshClient.sendLineWithTimeOutAdvance(cmdCheck, 120000);
                        if (log != null && !log.getResult().isEmpty()) {
                            if (getdumpSize(log.getResult()) != null) {
                                dumpSize = getdumpSize(log.getResult()) / (1024 * 1024);
                            }
                        }
                    }

                    if (dumpSize > 20) {

                        // get sql plus account
//						log = sshClient.sendLineWithTimeOutNew("ps -ef |grep pmon_" + databaseNode.getServiceName() + " | grep -v grep | awk '{print $1}'", 120000, vtAdminAcc.getShell());
//						String usernameRunSqlPlus = (String) getDumpSize(log.getResult(), 1, 0);
//						if (usernameRunSqlPlus == null || usernameRunSqlPlus.trim().isEmpty()) {
//							logger.error("ERROR CANNOT GET ACCOUNT IMPACT SQL PLUS");
//							return false;
//						}
//						log = sshClient.sendLineWithMore("su -", "Password:", PasswordEncoder.decrypt(rootAcc.getPassword()), 30000, shell);
//						log = sshClient.sendLineWithMore("su -", "Password:", "h6M56)viKyA", 30000);
                        log = sshClient.sendLineWithTimeOutNew("su - root", 20000, false, ":");
                        logger.info(log.getResult());
                        //20181023_tudn_start load pass security
//						log = sshClient.sendLineWithTimeOutNew(PasswordEncoder.decrypt(rootAcc.getPassword()), 20000, false);

                        log = sshClient.sendLineWithTimeOutNew(rootAcc.getPassword(), 20000, false);
                        //20181023_tudn_end load pass security
                        logger.info(log.getResult());
                        log = sshClient.sendLineWithTimeOutNew("su - ".concat(usernameRunSqlPlus), 20000, false);
                        logger.info(log.getResult());
                        log = sshClient.sendLineWithTimeOutNew("mkdir -p " + paramOption.getPathDumName(), 20000, false, ">");
                        logger.info(log.getResult());
                        log = sshClient.sendLineWithTimeOutNew("$ORACLE_HOME/bin/sqlplus / as sysdba", 20000, false, ">");
                        logger.info(log.getResult());

                        String cmdCreate = "create or replace directory ".concat(paramExport.getDirectory()).concat(" as '").concat(paramOption.getPathDumName() + "';");
                        log = sshClient.sendLineWithTimeOutNew(cmdCreate, 20000, false, ">");
                        logger.info("log create dir: " + log.getResult());

                        String cmdGrant = "grant read, write on directory ".concat(paramExport.getDirectory()).concat(" to ").concat(impacSqlAcc.getUsername() + ";");
                        log = sshClient.sendLineWithTimeOutNew(cmdGrant, 20000, false, ">");
                        logger.info("log grant users: " + log.getResult());

                        log = sshClient.sendLineWithTimeOutAdvance("exit", 120000);
                        logger.info("log exit: " + log.getResult());
                        check = true;
                    } else {
                        logger.error("ERROR CURRENT DUMP SIZE: " + dumpSize + " < " + paramExport.getDumpSize());
                    }
                } else {
                    logger.error("ERROR GET EMPTY LOG CHECK PATH DUMP FILE");
                }
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (sshClient != null) {
                try {
                    sshClient.disconnect();
                } catch (Exception e2) {
                    logger.error(e2.getMessage(), e2);
                }
            }
        }
        return check;
    }

    private Double getdumpSize(String log) {
        if (log != null && !log.trim().isEmpty()) {
            logger.info(log);
            List<String> lstVal = Arrays.asList(log.trim().split("\r\n"));
            if (!lstVal.isEmpty()) {
                Double valReturn;
                for (String val : lstVal) {
                    try {
                        valReturn = Double.valueOf(val.trim());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        valReturn = null;
                    }
                    if (valReturn != null) {
                        return valReturn;
                    }
                }
            }
        }
        return null;
    }

    private Object paserData(String log, int rowNum, int type) {
        if (log != null && !log.isEmpty()) {
            List<String> lstVal = Arrays.asList(log.trim().split("\r\n"));
            if (lstVal != null && !lstVal.isEmpty()) {
                int i = 0;
                for (String val : lstVal) {
                    if (rowNum == i) {
                        if (type == 1) {
                            try {
                                if (Integer.valueOf(val) != null) {
                                    return Integer.valueOf(val) / (1024 * 1024);
                                }
                            } catch (Exception e) {
                                logger.error(e.getMessage(), e);
                            }
                        } else {
                            return val.trim();
                        }
                    }
                    i++;
                }
            }
        }
        return null;
    }

    private ExportParamOptionObject buildParamOption(ParamExportObject paramExport) {
        ExportParamOptionObject exportParamOption = null;
        if (paramExport != null) {
            exportParamOption = new ExportParamOptionObject();

            // set ROTATE_MONTH
            DateTime dt = new DateTime();
            dt = dt.plusMonths(-Integer.valueOf(paramExport.getRotateExport()));
            exportParamOption.setRotateMonth(df.format(dt.toDate()));
            // set Tablespace_Name
            exportParamOption.setTbsName(paramExport.getTablespaceType() == 0
                    ? paramExport.getTablespacePrefix() : paramExport.getTablespacePrefix().concat(exportParamOption.getRotateMonth()));
            // set Path_Dump_Name
            exportParamOption.setPathDumName(paramExport.getPathDumpType() == 0
                    ? paramExport.getPathDumpPrefix() : paramExport.getPathDumpPrefix().concat(exportParamOption.getRotateMonth()));
            // set EXCLUDE
            exportParamOption.setExclude(paramExport.getExclude() == null
                    ? "" : "EXCLUDE=".concat(paramExport.getExclude()));
            // set COMPRESSION
            exportParamOption.setCompresstion(paramExport.getCompression() == null
                    ? "" : "COMPRESSION=".concat(paramExport.getCompression()));
            // set CONTENT
            exportParamOption.setContent(paramExport.getContent() == null
                    ? "" : "CONTENT=".concat(paramExport.getContent()));
            // set PARALLEL
            exportParamOption.setParallel(paramExport.getParallel() == null
                    ? "" : "PARALLEL=".concat(paramExport.getParallel()));
            // set OTHER
            exportParamOption.setOther(paramExport.getOther() == null
                    ? "" : paramExport.getOther());
            // set TABLESPACE_NAME
            exportParamOption.setTbsNameCondition(exportParamOption.getTbsName() == null
                    ? "1=1" : "TABLESPACE_NAME in ('".concat(exportParamOption.getTbsName()).concat("')"));
            // set TABLE_OWNER
            exportParamOption.setTbsOwnerCondition(paramExport.getTableOwner() == null
                    ? "and 2=2" : "and TABLE_OWNER in ('".concat(paramExport.getTableOwner()).concat("')"));
            // set OWNER
            exportParamOption.setOwnerCondition(paramExport.getTableOwner() == null
                    ? "and 2=2" : "and OWNER in ('".concat(paramExport.getTableOwner()).concat("')"));
            // set TABLE_NAME condition 1
            exportParamOption.setTableNameCondition1(paramExport.getTableName() == null
                    ? "and 3=3" : "and TABLE_NAME in ('".concat(paramExport.getTableName()).concat("')"));
            // set TABLE_NAME condition 2
            exportParamOption.setTableNameCondition2(paramExport.getTableName() == null
                    ? "and 3=3" : "and object_name in ('".concat(paramExport.getTableName()).concat("')"));
            // set PARTITION_NAME condition
            exportParamOption.setParNameCondition(paramExport.getPatitionName() == null
                    ? " and 4=4" : "and (PARTITION_NAME like '%".concat(paramExport.getPatitionName()).concat("%')"));
            // set ROTATE_MONTH condition
            if (paramExport.getRotateDrop() == null
                    || paramExport.getTablespaceType() != 0
                    || paramExport.getTableName() == null
                    || paramExport.getPatitionName() != null) {
                exportParamOption.setRotateMonthCondition("and 1=1");
            } else {
                exportParamOption.setRotateMonthCondition(" and partition_name like '%".concat(exportParamOption.getRotateMonth()).concat("%'"));
            }
        }
        return exportParamOption;
    }

    public static void main(String[] args) {
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
            DateTime dt = new DateTime();
            dt = dt.plusMonths(-Integer.valueOf(2));
            String cmd = "select * from %s %s ";
            System.out.println(String.format(cmd, "1", 9));
            System.out.println(df.format(dt.toDate()));

//			System.out.println(String.format(Config.SELECT_TABLE_OWNER_TABLE_NAME_PARTITION, "DATA201713", "1=1", "3=3", "and 4=4", "and 1=1", "DATA201713", "", "3=3", "and 4=4"));
            System.out.println(String.format(Config.SELECT_TABLE_OWNER_TABLE_NAME_PARTITION, "DATA201713", "1=1", "3=3", "and 4=4", "and 1=1", "DATA201713", "d", "3=3", "and 4=4"));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private List<ParamExportObject> getParamExportDb() {
        Connection conn = null;
        List<ParamExportObject> lstParamExport = new ArrayList<>();
        try {
            conn = getConnection(urlQltn, usernameQltn, passwordQltn);
            if (conn != null) {
                PreparedStatement prepare;
                prepare = conn.prepareStatement(Config.GET_PARAM_EXPORT_DB);
                prepare.setFetchSize(1000);
                prepare.setLong(1, databaseNode.getServerId());
                ResultSet rs = prepare.executeQuery();

                // get data
                List<String> dirsDetail = new ArrayList<>();

                ParamExportObject paramExport = null;
                while (rs.next()) {
                    try {
                        logger.info(">>>>>>>>>>>>>> " + rs.getString("DUMP_SIZE"));
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    paramExport = new ParamExportObject();
                    paramExport.setCompression(rs.getString("COMPRESSION"));
                    paramExport.setContent(rs.getString("CONTENT"));
                    paramExport.setDbId(rs.getLong("DB_ID"));
                    paramExport.setDbParamExportId(rs.getLong("DB_PARAM_EXPORT_ID"));
                    paramExport.setDirectory(rs.getString("DIRECTORY"));
//					paramExport.setDumpSize(Double.valueOf(rs.getString("DUMP_SIZE")));
                    paramExport.setDuration(rs.getString("DURATION"));
                    paramExport.setExclude(rs.getString("EXCLUDE"));
                    paramExport.setIpScp(rs.getString("IP_SCP") == null ? null : rs.getString("IP_SCP"));
                    paramExport.setObject(rs.getString("OBJECT"));
                    paramExport.setOther(rs.getString("OTHER"));
                    paramExport.setParallel(rs.getString("PARALLEL"));
                    paramExport.setPassScp(rs.getString("PASS_SCP") == null ? null : rs.getString("PASS_SCP"));
                    paramExport.setPathDumpPrefix(rs.getString("PATH_DUMP_PREFIX"));
                    paramExport.setPathDumpType(rs.getInt("PATH_DUMP_TYPE"));
                    paramExport.setPathScp(rs.getString("PATH_SCP") == null ? null : rs.getString("PATH_SCP"));
                    paramExport.setPatitionName(rs.getString("PARTITION_NAME"));
                    paramExport.setRotateDrop(rs.getString("ROTATE_DROP"));
                    paramExport.setRotateExport(rs.getString("ROTATE_EXPORT"));
                    paramExport.setTableName(rs.getString("TABLE_NAME"));
                    paramExport.setTableOwner(rs.getString("TABLE_OWNER"));
                    paramExport.setTablespaceDrop(rs.getString("TABLESPACE_DROP"));
                    paramExport.setTablespacePrefix(rs.getString("TABLESPACE_PREFIX"));
                    paramExport.setTablespaceType(rs.getInt("TABLESPACE_TYPE"));
                    paramExport.setUserScp(rs.getString("USER_SCP"));
                    paramExport.setDumpSize(rs.getDouble("DUMP_SIZE"));

                    lstParamExport.add(paramExport);
                }
                prepare.close();

                // insert and update param export dump size
//				List<ParamExportDumpSizeDb> lstParamExportDumpsize = new ArrayList<>();
//				Map<String, Object> filters = new HashMap<>();
//				ParamExportDumpSizeDb dumpsize = null;
//				for (int i = 0; i < lstParamExport.size(); i++) {
//					filters.put("paramExportQltnId", lstParamExport.get(i).getDbParamExportId());
//					filters.put("dbId", lstParamExport.get(i).getDbId());
//					try {
//						dumpsize = new ParamExportDumpSizeDbServiceImpl().findList(filters).get(0);
//					} catch (Exception e) {
//						logger.error(e.getMessage(), e);
//						dumpsize = null;
//					}
//
//					if (dumpsize == null) {
//						dumpsize = new ParamExportDumpSizeDb();
//						dumpsize.setDbId(lstParamExport.get(i).getDbId());
//						dumpsize.setParamExportQltnId(lstParamExport.get(i).getDbParamExportId());
//						dumpsize.setDumpSize(lstParamExport.get(i).getDumpSize());
//						lstParamExportDumpsize.add(dumpsize);
//
//					} else {
//						lstParamExport.get(i).setDumpSize(dumpsize.getDumpSize());
//					}
//				}
//
//				new ParamExportDumpSizeDbServiceImpl().saveOrUpdate(lstParamExportDumpsize);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (conn != null) {
                try {
                    conn.close();
                } catch (Exception e2) {
                    logger.error(e2.getMessage(), e2);
                }
            }
        }

        return lstParamExport;
    }


}
