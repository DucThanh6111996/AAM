package com.viettel.util;

import com.google.common.base.Joiner;
import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.viettel.bean.ResultGetAccount;
import com.viettel.bean.ServiceDatabase;
import com.viettel.exception.AppException;
import com.viettel.it.util.*;
//import com.viettel.model.ServiceDb;
import com.viettel.it.util.Util;
import com.viettel.util.Constant.PATTERN;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.schema.Table;
import net.sf.jsqlparser.statement.alter.Alter;
import net.sf.jsqlparser.statement.create.table.CreateTable;
import net.sf.jsqlparser.statement.delete.Delete;
import net.sf.jsqlparser.statement.drop.Drop;
import net.sf.jsqlparser.statement.insert.Insert;
import net.sf.jsqlparser.statement.replace.Replace;
import net.sf.jsqlparser.statement.select.Select;
import net.sf.jsqlparser.statement.truncate.Truncate;
import net.sf.jsqlparser.statement.update.Update;
import org.apache.commons.io.FileUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.io.support.EncodedResource;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import java.io.*;
import java.sql.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author quanns2
 */
public class SqlUtils {
    public static final String DEFAULT_STATEMENT_SEPARATOR = ";";
    public static final String FALLBACK_STATEMENT_SEPARATOR = "\n";
    public static final String EOF_STATEMENT_SEPARATOR = "^^^ END OF SCRIPT ^^^";
    public static final String DEFAULT_COMMENT_PREFIX = "--";
    public static final String DEFAULT_BLOCK_COMMENT_START_DELIMITER = "/*";
    public static final String DEFAULT_BLOCK_COMMENT_END_DELIMITER = "*/";
    private static final Log logger = LogFactory.getLog(SqlUtils.class);

    public SqlUtils() {
    }

    public static String checkTable(File backupFile, File executeFile, File rollbackFile, ServiceDatabase serviceDb, List<String> listTableName) {
        String backupScript = null;
        String executeScript = null;
        String rollbackScript = null;
        try {
            backupScript = FileUtils.readFileToString(backupFile);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        try {
            executeScript = FileUtils.readFileToString(executeFile);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
        try {
            rollbackScript = FileUtils.readFileToString(rollbackFile);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return checkTable(backupScript, executeScript, rollbackScript, serviceDb, listTableName);
    }

    public static String checkTable(String backupScript, String executeScript, String rollbackScript) {

        if (StringUtils.isEmpty(executeScript)) {
            return "Chưa nhập script";
        }

        if (backupScript == null)
            backupScript = "";
        if (rollbackScript == null)
            rollbackScript = "";

        String msg = "";

        Multimap<String, String> backupTables = HashMultimap.create();
        Multimap<String, String> executeTables = HashMultimap.create();
        Multimap<String, String> rollbackTables = HashMultimap.create();

        SqlUtils.getTable(backupScript, executeScript, rollbackScript, backupTables, executeTables, rollbackTables);

        List<String> backups = new ArrayList<>();
        List<String> rollbacks = new ArrayList<>();

        for (Map.Entry<String, String> entry : executeTables.entries()) {
            if (Arrays.asList("DELETE", "INSERT", "REPLACE", "UPDATE").contains(entry.getKey())
                    && !executeTables.get("CREATE TABLE").contains(entry.getValue())) {
                if (!backupScript.toUpperCase().contains(entry.getValue()))
                    backups.add(entry.getValue());

                if (!rollbackScript.toUpperCase().contains(entry.getValue()))
                    rollbacks.add(entry.getValue());
            }
        }

        if (!backups.isEmpty()) {
            msg = "Chưa backup bảng " + Joiner.on(",").join(backups);
            return msg;
        }

        if (!rollbacks.isEmpty()) {
            msg = "Chưa rollback bảng " + Joiner.on(",").join(rollbacks);
            return msg;
        }

        // havt : check backup : không được chứa các câu lệnh delete,
        // truncate,update, drop
        for (Map.Entry<String, String> entry : backupTables.entries()) {
            if (Arrays.asList("DELETE", "DROP", "TRUNCATE", "UPDATE").contains(entry.getKey())) {
                msg = "Backup có chứa " + entry.getKey();
            }
        }

        return msg;
    }

    public static String checkTable(String backupScript, String executeScript, String rollbackScript,
                                    ServiceDatabase serviceDb, List<String> listTableName) {

        if (StringUtils.isEmpty(executeScript)) {
            return MessageUtil.getResourceBundleMessage("script.do.not.enter");
        }

        if (backupScript == null)
            backupScript = "";
        if (rollbackScript == null)
            rollbackScript = "";

        String msg = "";

        Multimap<String, String> backupTables = HashMultimap.create();
        Multimap<String, String> executeTables = HashMultimap.create();
        Multimap<String, String> rollbackTables = HashMultimap.create();

        SqlUtils.getTable(backupScript, executeScript, rollbackScript, backupTables, executeTables, rollbackTables);

        List<String> backups = new ArrayList<>();
        List<String> rollbacks = new ArrayList<>();

        for (Map.Entry<String, String> entry : executeTables.entries()) {
            if (Arrays.asList("DELETE", "INSERT", "REPLACE", "UPDATE").contains(entry.getKey())
                    && !executeTables.get("CREATE TABLE").contains(entry.getValue())) {
                if (!backupScript.toUpperCase().contains(entry.getValue()))
                    backups.add(entry.getValue());

                if (!rollbackScript.toUpperCase().contains(entry.getValue()))
                    rollbacks.add(entry.getValue());
            }
        }

        if (!backups.isEmpty()) {
            msg = MessageUtil.getResourceBundleMessage("table.do.not.backup") + " " + Joiner.on(",").join(backups);
            return msg;
        }

        if (!rollbacks.isEmpty()) {
            msg = MessageUtil.getResourceBundleMessage("table.do.not.rollback") + " " + Joiner.on(",").join(rollbacks);
            return msg;
        }

        // havt : check backup : không được chứa các câu lệnh delete,
        // truncate,update, drop
        for (Map.Entry<String, String> entry : backupTables.entries()) {
            if (Arrays.asList("DELETE", "DROP", "TRUNCATE").contains(entry.getKey())) {
                msg = MessageUtil.getResourceBundleMessage("backup.have") + " " + entry.getKey();
                return msg;
            }
        }

       /* msg = checkScript(executeScript, serviceDb, listTableName);
        if (!StringUtils.isEmpty(msg)) {
            return "Script tác động: " + msg;
        }
        msg = checkScript(backupScript, serviceDb, listTableName);
        if (!StringUtils.isEmpty(msg)) {
            return "Script backup: " + msg;
        }*/
        /*msg = checkScript(rollbackScript, serviceDb, listTableName);
        if (!StringUtils.isEmpty(msg)) {
            return "Script rollback: " + msg;
        }*/
        return msg;
    }

    public static String checkScript(String scriptText, ServiceDatabase serviceDb, List<String> listTableName) {
        String msg = "";
        Pattern pattern;
        Matcher matcher;
        try {
            pattern = Pattern.compile(PATTERN.DDL_CREATE_EXIST, Pattern.CASE_INSENSITIVE);
            matcher = pattern.matcher(scriptText);
            if (matcher.find()) {
//                String[] arrScript = scriptText.split("/|;");
//                for (String script : arrScript) {
//                //DDL_CREATE_EXIST_VIEW
//                    msg = checkDDLCreateExistView(script, serviceDb);
//                    if (!StringUtils.isEmpty(msg)) {
//                        return msg;
//                    }
//                }
                return msg;
            } else {
                String[] arrScript = scriptText.split("/|;");
                for (String script : arrScript) {
                    Map<String, Object> map;
                    //DDL_CREATE_EXIST_VIEW
                    map = checkDDLCreateExistView(script, serviceDb);
                    msg = (String) map.get("msg");
                    if (!StringUtils.isEmpty(msg)) {
                        return msg;
                    } else {
                        if ((Boolean) map.get("find")) {
                            return "";
                        }
                    }
                    //DDL_CREATE_NOT_EXIST
                    map = checkDDLCreateNotExist(script, serviceDb);
                    msg = (String) map.get("msg");
                    if (!StringUtils.isEmpty(msg)) {
                        return msg;
                    } else {
                        if ((Boolean) map.get("find")) {
                            return "";
                        }
                    }
                    //DDL_ALTER
                    msg = checkDDLAlter(script, serviceDb);
                    if (!StringUtils.isEmpty(msg)) {
                        return msg;
                    }
                    //DDL_DROP
                    msg = checkDDLDrop(script, serviceDb);
                    if (!StringUtils.isEmpty(msg)) {
                        return msg;
                    }
                    //DDL_TRUNCATE
                    msg = checkDDLTruncate(script, serviceDb);
                    if (!StringUtils.isEmpty(msg)) {
                        return msg;
                    }
                    //them vao listTableName cac bang da duoc created
                    pattern = Pattern.compile(Constant.PATTERN.DDL_CREATE_TABLE, Pattern.CASE_INSENSITIVE);
                    matcher = pattern.matcher(script);
                    if (matcher.find()) {
                        String schemasObject = matcher.group(3).replaceAll("\"", "");
                        if (schemasObject.contains(".")) {
                            listTableName.add(schemasObject.toUpperCase());
                        } else {
                            listTableName.add(serviceDb.getUsername().toUpperCase() + "." + schemasObject.toUpperCase());
                        }
                    }
                    //DML_INSERT
                    msg = checkDMLInsert(script, serviceDb, listTableName);
                    if (!StringUtils.isEmpty(msg)) {
                        return msg;
                    }
                    //DML_OTHER
                    msg = checkDMLOther(script, serviceDb);
                    if (!StringUtils.isEmpty(msg)) {
                        return msg;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return msg;
    }

    public static String checkDMLInsert(String script, ServiceDatabase serviceDb, List<String> listTableName) {
        String msg = "";
        try {
            String regex = PATTERN.DML_INSERT;
            //DML
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(script);
            if (matcher.find()) {
                String schemasObject = matcher.group(1).replaceAll("\"", "");
                if (!schemasObject.contains(".")) {
                    schemasObject = serviceDb.getUsername().toUpperCase() + "." + schemasObject.toUpperCase();
                }

                //check xem bang da duoc tao tu script truoc chua
                boolean createdTable = false;
                for (String str : listTableName) {
                    if (str.equals(schemasObject)) {
                        createdTable = true;
                        break;
                    }
                }

                //Neu bang chua duoc tao tu script truoc
                if (!createdTable) {
                    //kiem tra cau lenh script
                    Map<String, Object> map = checkDMLExecuteScript(script, serviceDb);
                    if (Boolean.FALSE.equals(map.get("executed"))) {
                        return (String) map.get("message");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return msg;
    }

    public static String checkDMLOther(String script, ServiceDatabase serviceDb) {
        String msg = "";
        try {
            String regex = PATTERN.DML_UPDATE + "|" + PATTERN.DML_DELETE
                    + "|" + PATTERN.DML_GRANT + "|" + PATTERN.DML_REVOKE + "|" + PATTERN.DML_SELECT;
            //DML
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(script);
            if (matcher.find()) {
                //Kiem tra cau lenh script
                Map<String, Object> map = checkDMLExecuteScript(script, serviceDb);
                if (Boolean.FALSE.equals(map.get("executed"))) {
                    return (String) map.get("message");
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return msg;
    }

    public static Map<String, Object> checkDMLExecuteScript(String script, ServiceDatabase serviceDb) {
        String msg = "";
        String pwd = null;
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //20181023_tudn_start load pass security
//            pwd = PasswordEncoderQltn.decrypt(serviceDb.getPassword());
            String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

            Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
            Matcher matcher = pattern.matcher(serviceDb.getUrl());
            String ip = "";
            if(matcher.find()){
                if (matcher.group(0)!=null && !"".equals(matcher.group(0))) {
                    ip = matcher.group(0);
                }
            }
            String passBackup = "";
            try {
                passBackup = PasswordEncoderQltn.decrypt(serviceDb.getPassword());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                passBackup = serviceDb.getPassword();
            }
            Map<String, String> mapConfigSecurity = new HashMap<>();
            mapConfigSecurity = SecurityService.getConfigSecurity();
            ResultGetAccount resultGetAccount = SecurityService.getPassSecurity(ip,null,serviceDb.getUsername(),Constant.SECURITY_DATABASE, serviceDb.getDbId().toString(),null,null,null,
                    passBackup,mapConfigSecurity,null);
            if (!resultGetAccount.getResultStatus()) {
                return null;
            }else{
                pwd = resultGetAccount.getResult();
            }
            //20181023_tudn_end load pass security
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        Connection connn = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            connn = DriverManager.getConnection(serviceDb.getUrl(), serviceDb.getUsername(), pwd);

            StringBuilder sb = new StringBuilder()
                    .append("DECLARE\n")
                    .append("  c INTEGER;\n")
                    .append("  s VARCHAR2(4000) := ?;\n")
                    .append("BEGIN\n")
                    .append("  c := dbms_sql.open_cursor;\n")
                    .append("  dbms_sql.parse(c,s,1);\n")
                    .append("  dbms_sql.close_cursor(c);\n")
                    .append("  dbms_output.put_line('SQL Ok');\n")
                    .append("EXCEPTION\n")
                    .append("  WHEN OTHERS THEN\n")
                    .append("    dbms_sql.close_cursor(c);\n")
                    .append("    dbms_output.put_line('SQL Not Ok');\n")
                    .append("    ? := dbms_utility.format_error_stack;\n")
                    .append("    END;\n");
            cs = connn.prepareCall(sb.toString());
            cs.setString(1, script);
            cs.registerOutParameter(2, Types.CHAR);
            cs.execute();

            msg = cs.getString(2);
            if (!StringUtils.isEmpty(msg)) {
                map.put("executed", false);
                map.put("message", msg);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
/*            if (rs != null)
                try {
                    rs.close();

                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }*/
            if (cs != null)
                try {
                    cs.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }
            if (connn != null)
                try {
                    connn.close();

                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }
        }
        return map;
    }

    public static String checkDDLScript(String script, ServiceDatabase serviceDb, String regex) {
        String objectName = "";
        String schemas = "";
        String schemasObject = "";
        String msg = "";
        try {
            //DDL_ALTER
            Pattern pattern = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(script);
            if (matcher.find()) {
                schemasObject = matcher.group(4).replaceAll("\"", "");

                if (schemasObject.contains(".")) {
                    schemas = schemasObject.split("\\.")[0];
                    objectName = schemasObject.split("\\.")[1];
                } else {
                    schemas = serviceDb.getUsername();
                    objectName = schemasObject;
                }
                //Kiem tra do dai khong qua 30 kí tu
                if (objectName.length() > 30) {
                    msg = "Tên " + schemasObject + " không được quá 30 kí tự";
                    return msg;
                }
                //Kiem tra ton tai object
                Map<String, Object> map = checkExistObject(schemas, objectName, serviceDb);
                if (Boolean.TRUE.equals(map.get("existed"))) {
                    return (String) map.get("message");
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return msg;
    }

    public static Map<String, Object> checkDDLCreateExistView(String script, ServiceDatabase serviceDb) {
        String objectName = "";
        String schemas = "";
        String schemasObject = "";
        String msg = "";
        Map<String, Object> result = new HashMap<>();
        try {
            //DDL_CREATE_EXIST_VIEW
            Pattern pattern = Pattern.compile(PATTERN.DDL_CREATE_EXIST_VIEW, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(script);
            if (matcher.find()) {
                result.put("find", true);
                schemasObject = matcher.group(2).replaceAll("\"", "");

                if (schemasObject.contains(".")) {
                    schemas = schemasObject.split("\\.")[0];
                    objectName = schemasObject.split("\\.")[1];
                } else {
                    schemas = serviceDb.getUsername();
                    objectName = schemasObject;
                }
                //Kiem tra do dai khong qua 30 kí tu
                if (objectName.length() > 30) {
                    msg = MessageUtil.getResourceBundleMessage("name") + " " + schemasObject + " " + MessageUtil.getResourceBundleMessage("do.not.pass.30.character");
                    result.put("msg", msg);
                    return result;
                }
                //CREATE OR REPALCE
                String orReplace = matcher.group(1);
                if (orReplace != null && orReplace.length() > 1) {
                }
                //CREATE
                else {
                    //Kiem tra ton tai object
                    Map<String, Object> map = checkExistObject(schemas, objectName, serviceDb);
                    if (Boolean.TRUE.equals(map.get("existed"))) {
                        msg = (String) map.get("message");
                        result.put("msg", msg);
                        return result;
                    }
                }
                //Kiem tra neu co AS SELECT
//                pattern = Pattern.compile(PATTERN.DML_SELECT, Pattern.CASE_INSENSITIVE);
//                matcher = pattern.matcher(script);
//                if (matcher.find()) {
//                    String selectScript = matcher.group(1);
//                    if (selectScript != null && selectScript.length() > 0) {
//                        //Kiem tra cau lenh SELECT
//                        Map<String, Object> mapResult = checkDMLExecuteScript(selectScript, serviceDb);
//                        if (Boolean.FALSE.equals(mapResult.get("executed"))) {
//                            msg = (String) mapResult.get("message");
//                            result.put("msg", msg);
//                            return result;
//                        }
//                    }
//                }
                String selectScript = matcher.group(4);
                //Kiem tra cau lenh SELECT
                Map<String, Object> mapResult = checkDMLExecuteScript(selectScript, serviceDb);
                if (Boolean.FALSE.equals(mapResult.get("executed"))) {
                    msg = (String) mapResult.get("message");
                    result.put("msg", msg);
                    return result;
                }
            } else {
                result.put("find", false);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return result;
    }

    public static Map<String, Object> checkDDLCreateNotExist(String script, ServiceDatabase serviceDb) {
        String objectName = "";
        String schemas = "";
        String schemasObject = "";
        String msg = "";
        Map<String, Object> result = new HashMap<>();
        try {
            //DDL_ALTER
            Pattern pattern = Pattern.compile(PATTERN.DDL_CREATE_NOT_EXIST, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(script);
            if (matcher.find()) {
                result.put("find", true);
                schemasObject = matcher.group(4).replaceAll("\"", "");

                if (schemasObject.contains(".")) {
                    schemas = schemasObject.split("\\.")[0];
                    objectName = schemasObject.split("\\.")[1];
                } else {
                    schemas = serviceDb.getUsername();
                    objectName = schemasObject;
                }
                //Kiem tra do dai khong qua 30 kí tu
                if (objectName.length() > 30) {
                    msg = "Tên " + schemasObject + " không được quá 30 kí tự";
                    result.put("msg", msg);
                    return result;
                }
                //Kiem tra ton tai object
                Map<String, Object> map = checkExistObject(schemas, objectName, serviceDb);
                if (Boolean.TRUE.equals(map.get("existed"))) {
                    msg = (String) map.get("message");
                    result.put("msg", msg);
                    return result;
                }
                //Kiem tra neu co AS SELECT
                pattern = Pattern.compile(PATTERN.DML_SELECT, Pattern.CASE_INSENSITIVE);
                matcher = pattern.matcher(script);
                if (matcher.find()) {
                    String selectScript = matcher.group(1);
                    if (selectScript != null && selectScript.length() > 0) {
                        //Kiem tra cau lenh SELECT
                        Map<String, Object> mapResult = checkDMLExecuteScript(selectScript, serviceDb);
                        if (Boolean.FALSE.equals(mapResult.get("executed"))) {
                            msg = (String) mapResult.get("message");
                            result.put("msg", msg);
                            return result;
                        }
                    }
                }
            } else {
                result.put("find", false);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return result;
    }

    public static String checkDDLAlter(String script, ServiceDatabase serviceDb) {
        String objectName = "";
        String schemas = "";
        String schemasObject = "";
        String msg = "";
        try {
            //DDL_ALTER
            Pattern pattern = Pattern.compile(PATTERN.DDL_ALTER, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(script);
            if (matcher.find()) {
                schemasObject = matcher.group(2).replaceAll("\"", "");

                if (schemasObject.contains(".")) {
                    schemas = schemasObject.split("\\.")[0].replaceAll("\"", "");
                    objectName = schemasObject.split("\\.")[1].replaceAll("\"", "");
                } else {
                    schemas = serviceDb.getUsername();
                    objectName = schemasObject;
                }
                //Kiem tra do dai khong qua 30 kí tu
                if (objectName.length() > 30) {
                    msg = MessageUtil.getResourceBundleMessage("name") + " " + schemasObject + " " + MessageUtil.getResourceBundleMessage("do.not.pass.30.character");
                    return msg;
                }
                //Kiem tra ton tai object
                Map<String, Object> map = checkExistObject(schemas, objectName, serviceDb);
                if (Boolean.FALSE.equals(map.get("existed"))) {
                    return (String) map.get("message");
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return msg;
    }

    public static String checkDDLDrop(String script, ServiceDatabase serviceDb) {
        String objectName = "";
        String schemas = "";
        String schemasObject = "";
        String msg = "";
        try {
            //DDL_DROP
            Pattern pattern = Pattern.compile(PATTERN.DDL_DROP, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(script);
            if (matcher.find()) {
                schemasObject = matcher.group(2).replaceAll("\"", "");
                ;

                if (schemasObject.contains(".")) {
                    schemas = schemasObject.split("\\.")[0].replaceAll("\"", "");
                    objectName = schemasObject.split("\\.")[1].replaceAll("\"", "");
                } else {
                    schemas = serviceDb.getUsername();
                    objectName = schemasObject;
                }
                //Kiem tra do dai khong qua 30 kí tu
                if (objectName.length() > 30) {
                    msg = MessageUtil.getResourceBundleMessage("name") + " " + schemasObject + " " + MessageUtil.getResourceBundleMessage("do.not.pass.30.character");
                    return msg;
                }
                //Kiem tra ton tai object
                Map<String, Object> map = checkExistObject(schemas, objectName, serviceDb);
                if (Boolean.FALSE.equals(map.get("existed"))) {
                    return (String) map.get("message");
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return msg;
    }

    public static String checkDDLTruncate(String script, ServiceDatabase serviceDb) {
        String objectName = "";
        String schemas = "";
        String schemasObject = "";
        String msg = "";
        try {
            //DDL_TRUNCATE
            Pattern pattern = Pattern.compile(PATTERN.DDL_TRUNCATE, Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(script);
            if (matcher.find()) {
                schemasObject = matcher.group(1).replaceAll("\"", "");

                if (schemasObject.contains(".")) {
                    schemas = schemasObject.split("\\.")[0].replaceAll("\"", "");
                    objectName = schemasObject.split("\\.")[1].replaceAll("\"", "");
                } else {
                    schemas = serviceDb.getUsername();
                    objectName = schemasObject;
                }
                //Kiem tra do dai khong qua 30 kí tu
                if (objectName.length() > 30) {
                    msg = MessageUtil.getResourceBundleMessage("name") + " " + schemasObject + " " + MessageUtil.getResourceBundleMessage("do.not.pass.30.character");
                    return msg;
                }
                //Kiem tra ton tai object
                Map<String, Object> map = checkExistObject(schemas, objectName, serviceDb);
                if (Boolean.FALSE.equals(map.get("existed"))) {
                    return (String) map.get("message");
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }

        return msg;
    }

    public static Map<String, Object> checkExistObject(String schemas, String objectName, ServiceDatabase serviceDb) {
        String msg = "";
        String pwd = null;
        Map<String, Object> map = new HashMap<String, Object>();
        try {
            //20181023_tudn_start load pass security
//            pwd = PasswordEncoderQltn.decrypt(serviceDb.getPassword());
            String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

            Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
            Matcher matcher = pattern.matcher(serviceDb.getUrl());
            String ip = "";
            if(matcher.find()){
                if (matcher.group(0)!=null && !"".equals(matcher.group(0))) {
                    ip = matcher.group(0);
                }
            }
            String passBackup = "";
            try {
                passBackup = PasswordEncoderQltn.decrypt(serviceDb.getPassword());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                passBackup = serviceDb.getPassword();
            }
            Map<String, String> mapConfigSecurity = new HashMap<>();
            mapConfigSecurity = SecurityService.getConfigSecurity();
            ResultGetAccount resultGetAccount = SecurityService.getPassSecurity(ip,null,serviceDb.getUsername(),Constant.SECURITY_DATABASE,null,null,null
                    ,null, passBackup,mapConfigSecurity, null);
            if (!resultGetAccount.getResultStatus()) {
                return null;
            }else{
                pwd = resultGetAccount.getResult();
            }
            //20181023_tudn_end load pass security
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        Connection connn = null;
        PreparedStatement pstm = null;
        ResultSet rs = null;
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            connn = DriverManager.getConnection(serviceDb.getUrl(), serviceDb.getUsername(), pwd);

            //Kiem tra xem object co ton tai khong
            StringBuilder sb = new StringBuilder()
                    .append("SELECT owner,object_name,object_type \n")
                    .append("FROM ALL_OBJECTS \n")
                    .append("WHERE lower(owner) = lower(?) AND lower(object_name)= lower(?)  ");
            pstm = connn.prepareStatement(sb.toString());
            pstm.setString(1, schemas);
            pstm.setString(2, objectName);
            rs = pstm.executeQuery();
            if (rs.next()) {
                msg = rs.getString("object_type") + " "
                        + rs.getString("owner") + "." + rs.getString("object_name")
                        + " " + MessageUtil.getResourceBundleMessage("existed");
                map.put("existed", true);
                map.put("message", msg);
//                return msg;
            } else {
                msg = schemas + "." + objectName
                        + " " + MessageUtil.getResourceBundleMessage("not.exist");
                map.put("existed", false);
                map.put("message", msg);
            }

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            if (rs != null)
                try {
                    rs.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }
            if (pstm != null)
                try {
                    pstm.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }
            if (connn != null)
                try {
                    connn.close();
                } catch (SQLException e) {
                    logger.error(e.getMessage(), e);
                }
        }
        return map;
    }

    public static void getTable(String backupScript, String executeScript, String rollbackScript,
                                Multimap<String, String> backupTables, Multimap<String, String> executeTables,
                                Multimap<String, String> rollbackTables) {
        try {
            if (!StringUtils.isEmpty(backupScript))
                SqlUtils.splitSqlScript(null, backupScript, ";", "--", "/*", "*/", backupTables);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        try {
            if (!StringUtils.isEmpty(executeScript))
                splitSqlScript(null, executeScript, ";", "--", "/*", "*/", executeTables);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
        try {
            if (!StringUtils.isEmpty(rollbackScript))
                splitSqlScript(null, rollbackScript, ";", "--", "/*", "*/", rollbackTables);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void getTable(File backupFile, File executeFile, File rollbackFile,
                                Multimap<String, String> backupTables, Multimap<String, String> executeTables,
                                Multimap<String, String> rollbackTables) {
        try {
            getTable(FileUtils.readFileToString(backupFile), FileUtils.readFileToString(executeFile),
                    FileUtils.readFileToString(rollbackFile), backupTables, executeTables, rollbackTables);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void splitSqlScript(String script, char separator, Multimap<String, String> cmdTables)
            throws AppException {
        splitSqlScript(script, String.valueOf(separator), cmdTables);
    }

    public static void splitSqlScript(String script, String separator, Multimap<String, String> cmdTables)
            throws AppException {
        splitSqlScript((EncodedResource) null, script, separator, "--", "/*", "*/", cmdTables);
    }

    public static void splitSqlScript(EncodedResource resource, String script, String separator, String commentPrefix,
                                      String blockCommentStartDelimiter, String blockCommentEndDelimiter, Multimap<String, String> cmdTables)
            throws AppException {
        Assert.hasText(script, "script must not be null or empty");
        Assert.notNull(separator, "separator must not be null");
        Assert.hasText(commentPrefix, "commentPrefix must not be null or empty");
        Assert.hasText(blockCommentStartDelimiter, "blockCommentStartDelimiter must not be null or empty");
        Assert.hasText(blockCommentEndDelimiter, "blockCommentEndDelimiter must not be null or empty");
        StringBuilder sb = new StringBuilder();
        boolean inSingleQuote = false;
        boolean inDoubleQuote = false;
        boolean inEscape = false;
        char[] content = script.toCharArray();

        int i = 0;
        while (i < script.length()) {
            char c = content[i];
            if (inEscape) {
                inEscape = false;
                sb.append(c);
            } else if (c == 92) {
                inEscape = true;
                sb.append(c);
            } else {
                if (!inDoubleQuote && c == 39) {
                    inSingleQuote = !inSingleQuote;
                } else if (!inSingleQuote && c == 34) {
                    inDoubleQuote = !inDoubleQuote;
                }

                if (!inSingleQuote && !inDoubleQuote) {
                    if (script.startsWith(separator, i)) {
                        if (sb.length() > 0) {
                            // statements.add(sb.toString());
                            putCmd(sb, cmdTables);

                            sb = new StringBuilder();
                        }

                        i += separator.length() - 1;
                        ++i;
                        continue;
                    }

                    int indexOfCommentEnd;
                    if (script.startsWith(commentPrefix, i)) {
                        indexOfCommentEnd = script.indexOf("\n", i);
                        if (indexOfCommentEnd <= i) {
                            break;
                        }

                        i = indexOfCommentEnd;
                        ++i;
                        continue;
                    }

                    if (script.startsWith(blockCommentStartDelimiter, i)) {
                        indexOfCommentEnd = script.indexOf(blockCommentEndDelimiter, i);
                        if (indexOfCommentEnd <= i) {
                            throw new AppException(String.format("Missing block comment end delimiter [%s]."));
                        }

                        i = indexOfCommentEnd + blockCommentEndDelimiter.length() - 1;
                        ++i;
                        continue;
                    }

                    if (c == 32 || c == 10 || c == 9) {
                        if (sb.length() <= 0 || sb.charAt(sb.length() - 1) == 32) {
                            ++i;
                            continue;
                        }

                        c = 32;
                    }
                }

                sb.append(c);
            }
            ++i;
        }

        if (StringUtils.hasText(sb)) {
            // statements.add(sb.toString());
            putCmd(sb, cmdTables);
        }

    }

    private static void putCmd(StringBuilder sb, Multimap<String, String> cmdTables) {
        TablesNamesFinderExt tablesNamesFinder = new TablesNamesFinderExt();

        String trimCmd = sb.toString().replaceAll("\r", "").trim().toUpperCase();
        try {
            if (trimCmd.startsWith("DELETE")) {
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(trimCmd);
                Delete selectStatement = (Delete) statement;

                // List<String> tableList
                // =tablesNamesFinder.getTableList(selectStatement);
                // for (String table : tableList) {
                // cmdTables.put("DELETE", table.replaceAll("\"", ""));
                // }
                // havt sua them schema
                cmdTables.put("DELETE", selectStatement.getTable().getFullyQualifiedName().replaceAll("\"", ""));

            } else if (trimCmd.startsWith("INSERT")) {
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(trimCmd);
                Insert selectStatement = (Insert) statement;

                // List<String> tableList
                // =tablesNamesFinder.getTableList(selectStatement);
                // for (String table : tableList) {
                // cmdTables.put("INSERT", table.replaceAll("\"", ""));
                // }
                // havt sua them schema

                cmdTables.put("INSERT", selectStatement.getTable().getFullyQualifiedName().replaceAll("\"", ""));
                if (selectStatement.getSelect() != null) {
                    Select tmpSelect = selectStatement.getSelect();
                    List<String> tableList = tablesNamesFinder.getTableList(tmpSelect);
                    for (String table : tableList) {
                        cmdTables.put("SELECT", table.replaceAll("\"", ""));
                    }
                }

            } else if (trimCmd.startsWith("REPLACE")) {
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(trimCmd);
                Replace selectStatement = (Replace) statement;

                // List<String> tableList
                // =tablesNamesFinder.getTableList(selectStatement);
                // for (String table : tableList) {
                // cmdTables.put("REPLACE", table.replaceAll("\"", ""));
                // }
                // havt sua them schema

                cmdTables.put("REPLACE", selectStatement.getTable().getFullyQualifiedName().replaceAll("\"", ""));

            } else if (trimCmd.startsWith("SELECT")) {
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(trimCmd);
                Select selectStatement = (Select) statement;

                List<String> tableList = tablesNamesFinder.getTableList(selectStatement);
                for (String table : tableList) {
                    cmdTables.put("SELECT", table.replaceAll("\"", ""));
                }
            } else if (trimCmd.startsWith("UPDATE")) {
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(trimCmd);
                Update selectStatement = (Update) statement;

                // List<String> tableList =
                // tablesNamesFinder.getTableList(selectStatement);
                // for (String table : tableList) {
                // cmdTables.put("UPDATE", table.replaceAll("\"", ""));
                // }
                // havt sua them schema
                for (Table table : selectStatement.getTables()) {
                    cmdTables.put("UPDATE", table.getFullyQualifiedName().replaceAll("\"", ""));
                }
            } else if (trimCmd.startsWith("CREATE TABLE")) {
                // havt sửa bo NOLOGGING
                trimCmd = trimCmd.replace("NOLOGGING", "");
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(trimCmd);
                CreateTable selectStatement = (CreateTable) statement;

                // List<String> tableList
                // =tablesNamesFinder.getTableList(selectStatement);
                // for (String table : tableList) {
                // cmdTables.put("CREATE TABLE", table.replaceAll("\"", ""));
                // }
                cmdTables.put("CREATE TABLE", selectStatement.getTable().getFullyQualifiedName().replaceAll("\"", ""));
                if (selectStatement.getSelect() != null) {
                    Select tmpSelect = selectStatement.getSelect();
                    List<String> tableList = tablesNamesFinder.getTableList(tmpSelect);
                    for (String table : tableList) {
                        cmdTables.put("SELECT", table.replaceAll("\"", ""));
                    }
                }
            } else if (trimCmd.startsWith("DROP")) {
                // havt sửa
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(trimCmd);
                Drop selectStatement = (Drop) statement;

                cmdTables.put("DROP", selectStatement.getName().getFullyQualifiedName().replaceAll("\"", ""));
            } else if (trimCmd.startsWith("TRUNCATE")) {
                // havt sửa
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(trimCmd);
                Truncate selectStatement = (Truncate) statement;

                cmdTables.put("TRUNCATE", selectStatement.getTable().getFullyQualifiedName().replaceAll("\"", ""));
            } else if (trimCmd.startsWith("ALTER TABLE")) {
                // havt sửa
                net.sf.jsqlparser.statement.Statement statement = CCJSqlParserUtil.parse(trimCmd);
                Alter selectStatement = (Alter) statement;

                cmdTables.put("ALTER TABLE", selectStatement.getTable().getFullyQualifiedName().replaceAll("\"", ""));
            }

        } catch (JSQLParserException e) {
            logger.debug(e.getMessage(), e);
            logger.error(e.getMessage() + ":\t" + trimCmd);
        } finally {
        }
    }

    public static Map<Integer, List<String>> splitScriptTextToStatements(String scriptText) {
        Map<Integer, List<String>> statementMap = new HashMap<Integer, List<String>>();
        statementMap.put(1, new ArrayList<String>());
        statementMap.put(2, new ArrayList<String>());
        String[] lines = scriptText.split("\\r?\\n");
        String line;
        String cmdString;
        String dumbCase = "";
        int i = 0;
        while (i < lines.length) {
            line = lines[i];
            if (!dumbCase.isEmpty()) {
                line = dumbCase + " " + line.trim();
                dumbCase = "";
            }
            cmdString = line.trim();
            line = line.trim().toUpperCase();
            if (line.startsWith("CREATE PROCEDURE") || line.startsWith("CREATE PACKAGE")
                    || line.startsWith("CREATE TRIGGER") || line.startsWith("CREATE OR REPLACE PACKAGE")
                    || line.startsWith("CREATE OR REPLACE PROCEDURE")) {
                String previousLine = line;
                boolean isInserted = false;
                int j;
                for (j = i + 1; j < lines.length; j++) {
                    line = lines[j];
                    String[] commentSplits = line.split(";");
                    if (commentSplits.length > 1) {
                        if (commentSplits[1].trim().startsWith("--") || commentSplits[1].trim().startsWith("/*")) {
                            line = commentSplits[0] + ";";
                        }
                    }
                    if (line.trim().startsWith("/")) {
                        commentSplits = line.split("/");
                        if (commentSplits.length > 1) {
                            if (commentSplits[1].trim().startsWith("--")
                                    || (commentSplits[1].trim().isEmpty() && commentSplits[2].startsWith("*"))) {
                                line = "/";
                            }
                        }
                    }
                    if ("/".equals(line.trim()) && previousLine.trim().endsWith(";")) {
                        isInserted = true;
                        statementMap.get(1).add(cmdString);
                        break;
                    }
                    previousLine = line;
                    cmdString = cmdString + " \n" + line.trim();
                }
                i = j;
                if (!isInserted) {
                    statementMap.get(2).add(cmdString);
                }

            } else if (line.startsWith("--") || (line.startsWith("COMMIT") && line.endsWith(";")) || line.isEmpty()
                    || line == null || "/".equals(line) || line.startsWith("/*")) {
                i++;
                continue;

            } else if (line.startsWith("INSERT") || line.startsWith("SELECT") || line.startsWith("UPDATE")
                    || line.startsWith("DROP") || line.startsWith("ALTER") || line.startsWith("TRUNCATE")
                    || line.startsWith("GRANT") || line.startsWith("COMMENT")
                    || line.startsWith("CREATE OR REPLACE VIEW")
                    || line.startsWith("CREATE OR REPLACE FORCE VIEW")
                    || line.startsWith("CREATE TABLE")) {
                boolean isInserted = false;
                if (cmdString.trim().endsWith(";")) {
                    isInserted = true;
                    statementMap.get(1).add(cmdString.substring(0, cmdString.length() - 1));
                } else {
                    int j;
                    for (j = i + 1; j < lines.length; j++) {
                        line = lines[j];
                        String[] commentSplits = line.split(";");
                        if (commentSplits.length > 1) {
                            if (commentSplits[1].trim().startsWith("--")
                                    || commentSplits[1].trim().startsWith("/*")) {
                                line = commentSplits[0] + ";";
                            }
                        }
                        if (line.trim().startsWith("/")) {
                            commentSplits = line.split("/");
                            if (commentSplits.length > 1) {
                                if (commentSplits[1].trim().startsWith("--") || (commentSplits[1].trim().isEmpty()
                                        && commentSplits[2].startsWith("*"))) {
                                    line = "/";
                                }
                            }
                        }
                        if ("/".equals(line.trim())) {
                            if (cmdString.endsWith(";")) {
                                isInserted = true;
                                statementMap.get(1).add(cmdString.substring(0, cmdString.length() - 1));
                            } else {
                                isInserted = true;
                                statementMap.get(1).add(cmdString);
                            }
                            break;
                        } else {
                            cmdString = cmdString + " \n" + line.trim();
                            if (line.trim().endsWith(";")) {
                                isInserted = true;
                                statementMap.get(1).add(cmdString.substring(0, cmdString.length() - 1));
                                break;
                            }
                        }

                    }
                    i = j;
                }
                if (!isInserted) {
                    statementMap.get(2).add(cmdString);
                }
            } else if ("CREATE OR REPLACE".equalsIgnoreCase(line.trim())) {
                dumbCase = "CREATE OR REPLACE";
            } else if (line.startsWith("DECLARE") || line.startsWith("BEGIN")) {
                boolean isInserted = false;
                int j;
                for (j = i + 1; j < lines.length; j++) {
                    line = lines[j];
                    String[] commentSplits = line.split(";");
                    if (commentSplits.length > 1) {
                        if (commentSplits[1].trim().startsWith("--") || commentSplits[1].trim().startsWith("/*")) {
                            line = commentSplits[0] + ";";
                        }
                    }
                    cmdString = cmdString + " \n" + line.trim();
                    if ("END;".equals(line.trim())) {
                        statementMap.get(1).add(cmdString);
                        isInserted = true;
                        break;
                    }
                }
                i = j;
                if (!isInserted) {
                    statementMap.get(2).add(cmdString);
                }
            }
            // skip others
            i++;
        }

        return statementMap;
    }

    public static Map<Integer, List<String>> splitScriptFileToStatements(String filePath) {

        Map<Integer, List<String>> statementMap = new HashMap<Integer, List<String>>();
        statementMap.put(1, new ArrayList<>());
        statementMap.put(2, new ArrayList<>());
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(filePath)));
            String line;
            String cmdString;
            String dumbCase = "";
            while ((line = br.readLine()) != null) {
                if (!dumbCase.isEmpty()) {
                    line = dumbCase + " " + line.trim();
                    dumbCase = "";
                }
                cmdString = line.trim();
                line = line.trim().toUpperCase();
                if (line.startsWith("CREATE PROCEDURE") || line.startsWith("CREATE PACKAGE")
                        || line.startsWith("CREATE TRIGGER") || line.startsWith("CREATE OR REPLACE PACKAGE")
                        || line.startsWith("CREATE OR REPLACE PROCEDURE")) {
                    String previousLine = line;
                    boolean isInserted = false;
                    while ((line = br.readLine()) != null) {
                        String[] commentSplits = line.split(";");
                        if (commentSplits.length > 1) {
                            if (commentSplits[1].trim().startsWith("--") || commentSplits[1].trim().startsWith("/*")) {
                                line = commentSplits[0] + ";";
                            }
                        }
                        if (line.trim().startsWith("/")) {
                            commentSplits = line.split("/");
                            if (commentSplits.length > 1) {
                                if (commentSplits[1].trim().startsWith("--")
                                        || (commentSplits[1].trim().isEmpty() && commentSplits[2].startsWith("*"))) {
                                    line = "/";
                                }
                            }
                        }
                        if ("/".equals(line.trim()) && previousLine.trim().endsWith(";")) {
                            isInserted = true;
                            statementMap.get(1).add(cmdString);
                            break;
                        }
                        previousLine = line;
                        cmdString = cmdString + " \n" + line.trim();
                    }
                    if (!isInserted) {
                        statementMap.get(2).add(cmdString);
                    }

                } else if (line.startsWith("--") || (line.startsWith("COMMIT") && line.endsWith(";")) || line.isEmpty()
                        || line == null || "/".equals(line) || line.startsWith("/*")) {
                    continue;

                } else if (line.startsWith("INSERT") || line.startsWith("SELECT") || line.startsWith("UPDATE")
                        || line.startsWith("DROP") || line.startsWith("ALTER") || line.startsWith("TRUNCATE")
                        || line.startsWith("GRANT") || line.startsWith("COMMENT")
                        || line.startsWith("CREATE OR REPLACE VIEW")
                        || line.startsWith("CREATE OR REPLACE FORCE VIEW")
                        || line.startsWith("CREATE TABLE")) {
                    boolean isInserted = false;
                    if (cmdString.trim().endsWith(";")) {
                        isInserted = true;
                        statementMap.get(1).add(cmdString.substring(0, cmdString.length() - 1));
                    } else {
                        while ((line = br.readLine()) != null) {
                            String[] commentSplits = line.split(";");
                            if (commentSplits.length > 1) {
                                if (commentSplits[1].trim().startsWith("--")
                                        || commentSplits[1].trim().startsWith("/*")) {
                                    line = commentSplits[0] + ";";
                                }
                            }
                            if (line.trim().startsWith("/")) {
                                commentSplits = line.split("/");
                                if (commentSplits.length > 1) {
                                    if (commentSplits[1].trim().startsWith("--") || (commentSplits[1].trim().isEmpty()
                                            && commentSplits[2].startsWith("*"))) {
                                        line = "/";
                                    }
                                }
                            }
                            if ("/".equals(line.trim())) {
                                if (cmdString.endsWith(";")) {
                                    isInserted = true;
                                    statementMap.get(1).add(cmdString.substring(0, cmdString.length() - 1));
                                } else {
                                    isInserted = true;
                                    statementMap.get(1).add(cmdString);
                                }
                                break;
                            } else {
                                cmdString = cmdString + " \n" + line.trim();
                                if (line.trim().endsWith(";")) {
                                    isInserted = true;
                                    statementMap.get(1).add(cmdString.substring(0, cmdString.length() - 1));
                                    break;
                                }
                            }

                        }
                    }
                    if (!isInserted) {
                        statementMap.get(2).add(cmdString);
                    }
                } else if ("CREATE OR REPLACE".equalsIgnoreCase(line.trim())) {
                    dumbCase = "CREATE OR REPLACE";
                } else if (line.startsWith("DECLARE") || line.startsWith("BEGIN")) {
                    boolean isInserted = false;
                    while ((line = br.readLine()) != null) {
                        String[] commentSplits = line.split(";");
                        if (commentSplits.length > 1) {
                            if (commentSplits[1].trim().startsWith("--") || commentSplits[1].trim().startsWith("/*")) {
                                line = commentSplits[0] + ";";
                            }
                        }
                        cmdString = cmdString + " \n" + line.trim();
                        if ("END;".equals(line.trim())) {
                            statementMap.get(1).add(cmdString);
                            isInserted = true;
                            break;
                        }
                    }
                    if (!isInserted) {
                        statementMap.get(2).add(cmdString);
                    }
                }
                // skip others
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
        }

        return statementMap;
    }

    static String readScript(EncodedResource resource) throws IOException {
        return readScript(resource, "--", ";");
    }

    private static String readScript(EncodedResource resource, String commentPrefix, String separator)
            throws IOException {
        LineNumberReader lnr = new LineNumberReader(resource.getReader());

        String var4;
        try {
            var4 = readScript(lnr, commentPrefix, separator);
        } finally {
            lnr.close();
        }

        return var4;
    }

    public static String readScript(LineNumberReader lineNumberReader, String commentPrefix, String separator)
            throws IOException {
        String currentStatement = lineNumberReader.readLine();

        StringBuilder scriptBuilder;
        for (scriptBuilder = new StringBuilder(); currentStatement != null; currentStatement = lineNumberReader
                .readLine()) {
            if (commentPrefix != null && !currentStatement.startsWith(commentPrefix)) {
                if (scriptBuilder.length() > 0) {
                    scriptBuilder.append('\n');
                }

                scriptBuilder.append(currentStatement);
            }
        }

        appendSeparatorToScriptIfNecessary(scriptBuilder, separator);
        return scriptBuilder.toString();
    }

    private static void appendSeparatorToScriptIfNecessary(StringBuilder scriptBuilder, String separator) {
        if (separator != null) {
            String trimmed = separator.trim();
            if (trimmed.length() != separator.length()) {
                if (scriptBuilder.lastIndexOf(trimmed) == scriptBuilder.length() - trimmed.length()) {
                    scriptBuilder.append(separator.substring(trimmed.length()));
                }

            }
        }
    }

    public static String getNewConnectionString(String url) {
        String connectionStr;

        String tmp = url.replaceAll("\r", "").replaceAll("\n", "").split(":@")[1].replaceAll("\\s", "");

        if (tmp.toUpperCase().trim().startsWith("(DESCRIPTION"))
            return tmp;

        String ip;
        String port;
        String sid;
        if (tmp.contains("/")) {
            sid = "SERVICE_NAME=" + tmp.split("/")[1];
            ip = tmp.split("/")[0].split(":")[0];
            port = tmp.split("/")[0].split(":")[1];
        } else {
            ip = tmp.split(":")[0];
            port = tmp.split(":")[1];
            sid = "SID=" + tmp.split(":")[2];
        }

        connectionStr = String.format("(DESCRIPTION=(ADDRESS=(PROTOCOL=TCP)(HOST=%s)(PORT=%s))(CONNECT_DATA=(%s)))", ip,
                port, sid);

        return connectionStr;
    }

    public static Boolean checkStatmentSeparator(List<String> lines) {
        Boolean isValid = true;
        Boolean currentComment = false;
        for (int i = lines.size() - 1; i >= 0; i--) {
            String line = lines.get(i).replaceAll("--.*$", "").replaceAll("/\\*.*\\*/", "").replace("\uFEFF", "").trim();
            if (org.apache.commons.lang3.StringUtils.isEmpty(line))
                continue;

            if (currentComment) {
                if (line.contains("/*")) {
                    line = line.replaceAll("/\\*.*$", "").trim();
                    currentComment = false;
                }
            }

            if (line.endsWith("*/"))
                currentComment = true;

            if (org.apache.commons.lang3.StringUtils.isNotEmpty(line) && !currentComment) {
                if (!(line.endsWith(";") || (line.endsWith("/") && !line.endsWith("*/")))) {
                    isValid = false;
                } else {
                    isValid = true;
                }
                break;
            }
        }

        return isValid;
    }

    public static void main(String[] args) {
        // SqlUtils sqlUtils = new SqlUtils();
        /* List<String> statements = new ArrayList<>();
        try {
			BufferedReader br = new BufferedReader(new FileReader(new File("C:\\Users\\havt4.VIETTELGROUP\\Desktop\\data\\SALE_APP_PCK_SUM_REPORT_ddl_212.sql")));
			String line;
			String cmdString = "";
			String dumbCase = "";
			while ((line = br.readLine()) != null) {
				if (!dumbCase.isEmpty()) {
					line = dumbCase + " " + line.trim();
					dumbCase = "";
				}
				cmdString = line.trim();
				line=line.trim().toUpperCase();
				if (line.startsWith("CREATE PROCEDURE") || line.startsWith("CREATE PACKAGE")
						|| line.startsWith("CREATE TRIGGER") || line.startsWith("CREATE OR REPLACE PACKAGE")
						|| line.startsWith("CREATE OR REPLACE PROCEDURE")) {
					String previousLine = line;
					boolean isInserted=false;
					while ((line = br.readLine()) != null) {
						String[] commentSplits = line.split(";");
						if (commentSplits.length > 1) {
							if (commentSplits[1].trim().startsWith("--") || commentSplits[1].trim().startsWith("/*")) {
								line = commentSplits[0] + ";";
							}
						}
						if (line.trim().startsWith("/")) {
							commentSplits = line.split("/");
							if (commentSplits.length > 1) {
								if (commentSplits[1].trim().startsWith("--")
										|| (commentSplits[1].trim().isEmpty() && commentSplits[2].startsWith("*"))) {
									line = "/";
								}
							}
						}
						if (line.trim().equals("/") && previousLine.trim().endsWith(";")) {
							isInserted=true;
							statements.add(cmdString);
							break;
						}
						previousLine = line;
						cmdString = cmdString + " \n" + line.trim();
					}
					if(!isInserted){
						statements.add(cmdString);
					}

				} else if (line.startsWith("--") || (line.startsWith("COMMIT") && line.endsWith(";")) || line.isEmpty()
						|| line == null || line.equals("/") || line.startsWith("/*")) {
					continue;

				} else if (line.startsWith("INSERT") || line.startsWith("SELECT") || line.startsWith("UPDATE")
						|| line.startsWith("DROP") || line.startsWith("ALTER") || line.startsWith("TRUNCATE")
						|| line.startsWith("GRANT") || line.startsWith("COMMENT")
						|| line.startsWith("CREATE OR REPLACE VIEW")
						|| line.startsWith("CREATE OR REPLACE FORCE VIEW")
						|| line.startsWith("CREATE TABLE")) {
					boolean isInserted = false;
					if (cmdString.trim().endsWith(";")) {
						isInserted=true;
						statements.add(cmdString.substring(0,cmdString.length()-1));
					} else {
						while ((line = br.readLine()) != null) {
							String[] commentSplits = line.split(";");
							if (commentSplits.length > 1) {
								if (commentSplits[1].trim().startsWith("--")
										|| commentSplits[1].trim().startsWith("/*")) {
									line = commentSplits[0] + ";";
								}
							}
							if (line.trim().startsWith("/")) {
								commentSplits = line.split("/");
								if (commentSplits.length > 1) {
									if (commentSplits[1].trim().startsWith("--") || (commentSplits[1].trim().isEmpty()
											&& commentSplits[2].startsWith("*"))) {
										line = "/";
									}
								}
							}
							if (line.trim().equals("/")) {
								if (cmdString.endsWith(";")) {
									isInserted=true;
									statements.add(cmdString.substring(0,cmdString.length()-1));
								} else {
									isInserted=true;
									statements.add(cmdString);
								}
								break;
							} else {
								cmdString = cmdString + " \n" + line.trim();
								if (line.trim().endsWith(";")) {
									isInserted=true;
									statements.add(cmdString.substring(0,cmdString.length()-1));
									break;
								}
							}

						}
					}
					if(!isInserted){
						statements.add(cmdString);
					}
				} else if (line.trim().equalsIgnoreCase("CREATE OR REPLACE")) {
					dumbCase = "CREATE OR REPLACE";
				}else if (line.startsWith("DECLARE") || line.startsWith("BEGIN")){
					boolean isInserted=false;
					while ((line = br.readLine()) != null) {
						String[] commentSplits = line.split(";");
						if (commentSplits.length > 1) {
							if (commentSplits[1].trim().startsWith("--") || commentSplits[1].trim().startsWith("/*")) {
								line = commentSplits[0] + ";";
							}
						}
						cmdString = cmdString + " \n" + line.trim();
						if (line.trim().equals("END;")) {
							statements.add(cmdString);
							isInserted=true;
							break;
						}
					}
					if(!isInserted){
						statements.add(cmdString);
					}
				}
				// skip others
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
		 Session session = HibernateUtil.getSessionFactory().openSession();
		 Transaction tx = null;
		 CheckVerifyDb obj = null;
		 try {
		 tx = session.beginTransaction();
		 for (int i = 1; i < statements.size() + 1; i++) {
		 String sql = "SELECT VERIFY_DB_SEQ.nextval FROM DUAL";
		 SQLQuery query = session.createSQLQuery(sql);
		 BigDecimal seq = (BigDecimal) query.uniqueResult();
		 obj = new CheckVerifyDb(new Integer(seq.intValue()));
		 obj.setText(statements.get(i - 1));
		 session.saveOrUpdate(obj);
		 }
		 tx.commit();
		 } catch (HibernateException e) {
		 if (tx != null)
		 tx.rollback();
		
		 logger.error(e.getMessage(), e);
		 } catch (Exception e) {
		 if (tx != null)
		 tx.rollback();
		
		 logger.error(e.getMessage(), e);
		 throw new SysException();
		 } finally {
		 if (session != null) {
		 session.close();
		 }
		 }
		 for(String stm : statements){
		String plsql = "DECLARE " 
				+ "c integer;" 
				+ "sqlCheck clob;"
		+ "BEGIN "
				+ "sqlCheck :=?;"
				+ "c := dbms_sql.open_cursor;" 
				+ "dbms_sql.parse(c,sqlCheck,1);" 
				+ "dbms_sql.close_cursor(c); "
				+ "? := 'OK';" 
		+ "EXCEPTION when others then " 
				+ "dbms_sql.close_cursor(c);" 
				+ "? := sqlerrm;"
		+ "END;";
		Connection connection = null;
		CallableStatement cs = null;
		try {
			Class.forName("oracle.jdbc.OracleDriver");
			connection = DriverManager.getConnection("jdbc:oracle:thin:@10.58.137.38:9521:ctqt", "ACTION_TOOL","123456");
			cs = connection.prepareCall(plsql);
			cs.setString(1, stm);
			cs.registerOutParameter(2, OracleTypes.VARCHAR);
			cs.registerOutParameter(3, OracleTypes.LONGVARCHAR);
			cs.execute();

			System.out.println("Result = " + cs.getObject(2)+" "+cs.getObject(3));

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			try {
				if (cs != null) {
					cs.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException sqle) {
				sqllogger.error(e.getMessage(), e);
			}
		}
		 }
		 */
    }
}
