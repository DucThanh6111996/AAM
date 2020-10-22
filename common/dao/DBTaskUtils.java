/*
 * Copyright (C) 2011 Viettel Telecom. All rights reserved.
 * VIETTEL PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.viettel.dao;

import com.viettel.controller.AamConstants;
import oracle.sql.CLOB;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.math.BigDecimal;
import java.sql.*;
import java.util.*;

/**
 * @author KhiemVK
 * @version 1.0
 * @since: 8/13/13 3:10 PM
 */
public abstract class DBTaskUtils {
    private static Logger logger = LogManager.getLogger(DBTaskUtils.class);

    public interface DATA_TYPE {
        public static final int STRING = 1;
        public static final int FLOAT = 2;
        public static final int DOUBLE = 3;
        public static final int INTEGER = 4;
        public static final int LONG = 5;
        public static final int DATE = 7;
        public static final int DATE_TIME = 8;
    }

    public static final int MAX_BATCH = 2000;

    public DBTaskUtils() {
    }

    public abstract Connection getConnection() throws Exception;

    public Connection getConnectionBeginTransaction() throws Exception {
        Connection connection = getConnection();
        connection.setAutoCommit(false);
        return connection;
    }

    public void commitTransaction(Connection connection) throws Exception {
        if (connection != null) {
            connection.commit();
            connection.setAutoCommit(false);
        }
    }

    public void commitAndCloseConnection(Connection connection) throws Exception {
        if (connection != null) {
            connection.commit();
            connection.setAutoCommit(false);
            ConnectionPool.closeResource(connection);
        }
    }

    public void rollbackTransaction(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
                connection.setAutoCommit(false);
            } catch (Exception ex) {
                logger.error("Rollback error", ex);
            }
        }
    }

    public void rollbackAndCloseConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.rollback();
                connection.setAutoCommit(false);
            } catch (Exception ex) {
                logger.error("Rollback error", ex);
            } finally {
                ConnectionPool.closeResource(connection);
            }
        }
    }

    public void closeConnection(Connection connection) {
        if (connection != null) {
            ConnectionPool.closeResource(connection);
        }
    }

    public List<Map<String, Object>> select(Connection conn, String sql) throws Exception {
        if (null == sql || sql.isEmpty()) {
            return null;
        }
        logger.info(getThreadName() + ": Begin select: " + sql);
        List<Map<String, Object>> resultMap = null;
        ResultSet result = null;
        PreparedStatement stm = null;
        try {
            stm = conn.prepareStatement(sql);
            result = stm.executeQuery();
            resultMap = convertResultSetToMap(result);
        } catch (Exception ex) {
            throw ex;
        } finally {
            ConnectionPool.closeResource(result);
            ConnectionPool.closeResource(stm);
        }
        logger.info(getThreadName() + ": End select...");
        return resultMap;
    }

    public Map<String, Map<String, Object>> selectToMapByColumn(Connection conn, String sql, String columnName) throws Exception {
        if (null == sql || sql.isEmpty()) {
            return null;
        }
        logger.info(getThreadName() + ": Begin select: " + sql);
        Map<String, Map<String, Object>> resultMap = null;
        ResultSet result = null;
        PreparedStatement stm = null;
        try {
            stm = conn.prepareStatement(sql);
            result = stm.executeQuery();
            resultMap = convertResultSetToMap(result, columnName);
        } catch (Exception ex) {
            throw ex;
        } finally {
            ConnectionPool.closeResource(result);
            ConnectionPool.closeResource(stm);
        }
        logger.info(getThreadName() + ": End select...");
        return resultMap;
    }

    public List<Map<String, Object>> select(Connection conn, String sql, List<SqlParam> Condition) throws Exception {
        if (null == sql || sql.isEmpty()) {
            return null;
        }
        logger.info(getThreadName() + ": Begin select: " + sql);
        List<Map<String, Object>> resultMap = null;
        ResultSet result = null;
        PreparedStatement stm = null;
        try {
            stm = conn.prepareStatement(sql);
            addSqlParam(stm, Condition);
            result = stm.executeQuery();
            resultMap = convertResultSetToMap(result);
        } catch (Exception ex) {
            throw ex;
        } finally {
            ConnectionPool.closeResource(result);
            ConnectionPool.closeResource(stm);
        }
        logger.info(getThreadName() + ": End select...");
        return resultMap;
    }

    public List<Map<String, Object>> select(Connection conn, String tableName, List<String> Column, List<SqlParam> Condition) throws Exception {
        if (null == Column || Column.isEmpty()) {
            return null;
        }
        logger.info(getThreadName() + ": Begin select...");
        List<Map<String, Object>> resultMap = null;
        ResultSet result = null;
        PreparedStatement stm = null;
        try {
            String sqlSelect = conn.nativeSQL(SqlTemplate.selectTemplate(tableName, Column, Condition));
            logger.info(getThreadName() + ": " + sqlSelect);
            stm = conn.prepareStatement(sqlSelect);
            addSqlParam(stm, Condition);
            result = stm.executeQuery();
            resultMap = convertResultSetToMap(result);
        } catch (Exception ex) {
            throw ex;
        } finally {
            ConnectionPool.closeResource(result);
            ConnectionPool.closeResource(stm);
        }
        logger.info(getThreadName() + ": End select...");
        return resultMap;
    }

    public Map<String, Map<String, Object>> select(Connection conn, String tableName, List<String> Column, List<SqlParam> Condition, String columnName) throws Exception {
        if (null == Column || Column.isEmpty()) {
            return null;
        }
        logger.info(getThreadName() + ": Begin select...");
        String sqlSelect = SqlTemplate.selectTemplate(tableName, Column, Condition);
        Map<String, Map<String, Object>> resultMap = null;
        ResultSet result = null;
        PreparedStatement stm = null;
        try {
            logger.info(getThreadName() + ": " + sqlSelect);
            stm = conn.prepareStatement(sqlSelect);
            addSqlParam(stm, Condition);
            result = stm.executeQuery();
            resultMap = convertResultSetToMap(result, columnName);
        } catch (Exception ex) {
            logger.error("Error:", ex);
            throw ex;
        } finally {
            ConnectionPool.closeResource(result);
            ConnectionPool.closeResource(stm);
        }
        logger.info(getThreadName() + ": End select...");
        return resultMap;
    }

    public Map<String, Map<String, Object>> select(Connection conn, String tableName, String column, List<SqlParam> Condition, String columnName) throws Exception {
        List<String> Column = new ArrayList<String>();
        Column.add(column);
        return select(conn, tableName, Column, Condition, columnName);
    }

    public Map<String, Map<String, Object>> select(Connection conn, String tableName, String column, Map<String, Object> Condition, String columnName) throws Exception {
        List<String> Column = new ArrayList<String>();
        Column.add(column);
        return select(conn, tableName, Column, convertMapToSqlParam(Condition, null), columnName);
    }

    /**
     * @param tableName
     * @param column
     * @param Condition
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> select(Connection conn, String tableName, String column, List<SqlParam> Condition) throws Exception {
        List<String> Column = new ArrayList<String>();
        Column.add(column);
        return select(conn, tableName, Column, Condition);
    }

    /**
     * @param tableName
     * @param Column
     * @param Condition
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> select(Connection conn, String tableName, List<String> Column, Map<String, Object> Condition) throws Exception {
        return select(conn, tableName, Column, convertMapToSqlParam(Condition, null));
    }

    /**
     * @param tableName
     * @param Column
     * @param Condition
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> select(Connection conn, String tableName, String Column, Map<String, Object> Condition) throws Exception {
        return select(conn, tableName, Column, convertMapToSqlParam(Condition, null));
    }

    /**
     * @param tableName
     * @param column
     * @return
     * @throws Exception
     */
    public List<Map<String, Object>> select(Connection conn, String tableName, String column) throws Exception {
        List<String> Column = new ArrayList<String>();//?
        Column.add(column);//?
        Map<String, Object> Condition = null;
        return select(conn, tableName, Column, Condition);
    }

    /**
     * @param tableName
     * @param Column
     * @throws Exception
     */
    public void insert(Connection conn, String tableName, List<Map<String, Object>> Column, Map<String, Integer> mapDataType) throws Exception {
        if (null == Column || Column.isEmpty() || 0 == Column.size()) {
            return;
        }
        logger.info(getThreadName() + ": Begin insert...");
        List<String> listColumn = new ArrayList<String>();
        ArrayList<SqlParam> param = convertMapToSqlParam(Column.get(0), null);
        for (int i = 0; i < param.size(); i++) {
            listColumn.add(param.get(i).getName());
        }
        String sqlInsert = SqlTemplate.insertTemplate(tableName, listColumn);
        PreparedStatement stm = null;
        try {
            logger.info(getThreadName() + ": " + sqlInsert);
            conn.setAutoCommit(false);
            stm = conn.prepareStatement(sqlInsert);
            int count = 1;
            for (int i = 0; i < Column.size(); i++) {
                ArrayList<SqlParam> paramlist = convertMapToSqlParam(Column.get(i), listColumn);
                addSqlParam(stm, paramlist, mapDataType);
                stm.addBatch();
                count++;
                if (count > MAX_BATCH) {
                    try {
                        stm.executeBatch();
                    } catch (Exception ex) {
                        throw ex;
                    } finally {
                        stm.clearBatch();
                        count = 0;
                    }
                }
            }
            if (count > 0) {
                stm.executeBatch();
            }
        } catch (Exception ex) {
            logger.error("Insert error:", ex);
            throw ex;
        } finally {
            ConnectionPool.closeResource(stm);
        }
        logger.info(getThreadName() + ": Insert Done!");
    }

    public void insert(Connection conn, String tableName, List<Map<String, Object>> Column, String idFileName, String sequenceName, Map<String, Integer> mapDataType) throws Exception {
        if (null == Column || 0 == Column.size()) {
            return;
        }
        logger.info(getThreadName() + ": Begin insert...");
        List<String> listColumn = new ArrayList<String>();
        ArrayList<SqlParam> param = convertMapToSqlParam(Column.get(0), null);
        for (int i = 0; i < param.size(); i++) {
            listColumn.add(param.get(i).getName());
        }
        String sqlInsert = SqlTemplate.insertTemplate(tableName, listColumn, idFileName, sequenceName);
        PreparedStatement stm = null;
        try {
            logger.info(getThreadName() + ": " + sqlInsert);
            conn.setAutoCommit(false);
            stm = conn.prepareStatement(sqlInsert);
            int count = 0;
            for (int i = 0; i < Column.size(); i++) {
                ArrayList<SqlParam> paramlist = convertMapToSqlParam(Column.get(i), listColumn);
                addSqlParam(stm, paramlist, mapDataType);
                stm.addBatch();
                count++;
                if (count > MAX_BATCH) {
                    try {
                        stm.executeBatch();
                    } catch (Exception ex) {
                        logger.error("Error:", ex);
                        throw ex;
                    } finally {
                        stm.clearBatch();
                        count = 0;
                    }
                }
            }
            if (count > 0) {
                stm.executeBatch();
            }
        } catch (Exception ex) {
            logger.error("Error:", ex);
            throw ex;
        } finally {
            ConnectionPool.closeResource(stm);
        }
        logger.info(getThreadName() + ": Insert Done!");
    }

    public void insert(Connection conn, String tableName, Map<String, Object> column, String idFileName, String sequenceName, Map<String, Integer> mapDataType) throws Exception {
        List<Map<String, Object>> Column = new ArrayList<Map<String, Object>>();
        Column.add(column);
        insert(conn, tableName, Column, idFileName, sequenceName, mapDataType);
    }

    /**
     * @param tableName
     * @param column
     * @throws Exception
     */
    public void insert(Connection conn, String tableName, Map<String, Object> column, Map<String, Integer> mapDataType) throws Exception {
        List<Map<String, Object>> Column = new ArrayList<Map<String, Object>>();
        Column.add(column);
        insert(conn, tableName, Column, mapDataType);
    }

    /**
     * @param tableName
     * @param Column
     * @param Condition
     * @throws Exception
     */
    public void update(Connection conn, String tableName, List<SqlParam> Column, List<SqlParam> Condition, Map<String, Integer> mapDataType) throws Exception {
        if (null == Column || Column.isEmpty()) {
            return;
        }
        logger.info(getThreadName() + ": Begin Update...");
        ArrayList<String> column = new ArrayList<String>();
        for (int i = 0; i < Column.size(); i++) {
            column.add(Column.get(i).getName());
        }
        ArrayList<String> cond = new ArrayList<String>();
        for (int i = 0; i < Condition.size(); i++) {
            cond.add(Condition.get(i).getName());
        }
        String sqlUpdate = SqlTemplate.updateTemplate(tableName, column, cond);
        ArrayList<SqlParam> total = new ArrayList<SqlParam>();
        for (int i = 0; i < Column.size(); i++) {
            total.add(Column.get(i));
        }
        for (int i = 0; i < Condition.size(); i++) {
            total.add(Condition.get(i));
        }

        PreparedStatement stm = null;
        try {
            logger.info(getThreadName() + ": " + sqlUpdate);
            conn.setAutoCommit(false);
            stm = conn.prepareStatement(sqlUpdate);
            addSqlParam(stm, total, mapDataType);
            stm.executeUpdate();
        } catch (Exception ex) {
            logger.error("Error", ex);
            throw ex;
        } finally {
            ConnectionPool.closeResource(stm);
        }

        logger.info(getThreadName() + ": Update Done!");
    }

    /**
     * @param tableName
     * @param Column
     * @param Condition
     * @throws Exception
     */
    public void update(Connection conn, String tableName, Map<String, Object> Column, Map<String, Object> Condition, Map<String, Integer> mapDataType) throws Exception {
        update(conn, tableName, convertMapToSqlParam(Column, null), convertMapToSqlParam(Condition, null), mapDataType);
    }

    public void updateBatch(Connection conn, String tableName, Map<String, Object> Column, Map<String, Object> conds, Map<String, Integer> mapDataType) throws Exception {
        if (null == Column || 0 == Column.size()) {
            return;
        }
        List<Map<String, Object>> Columns = new ArrayList<Map<String, Object>>();
        Columns.add(Column);
        if (conds != null) {
            List<Map<String, Object>> lstCondition = new ArrayList<Map<String, Object>>();
            lstCondition.add(conds);
            updateBatch(conn, tableName, Columns, lstCondition, mapDataType);
        } else {
            updateBatch(conn, tableName, Columns, null, mapDataType);
        }
    }

    public void updateBatch(Connection conn, String tableName, List<Map<String, Object>> Column, List<Map<String, Object>> conds, Map<String, Integer> mapDataType) throws Exception {
        if (null == Column || 0 == Column.size()) {
            return;
        }
        logger.info(getThreadName() + ": Begin update batch...");
        List<String> listColumn = new ArrayList<String>();
        ArrayList<SqlParam> param = convertMapToSqlParam(Column.get(0), null);
        for (int i = 0; i < param.size(); i++) {
            listColumn.add(param.get(i).getName());
        }
        List<SqlParam> Condition = convertMapToSqlParam(conds.get(0), null);
        ArrayList<String> cond = new ArrayList<String>();
        for (int i = 0; i < Condition.size(); i++) {
            cond.add(Condition.get(i).getName());
        }
        String sqlInsert = SqlTemplate.updateTemplate(tableName, listColumn, cond);

        PreparedStatement stm = null;
        try {
            logger.info(getThreadName() + ": " + sqlInsert);
            conn.setAutoCommit(false);
            stm = conn.prepareStatement(sqlInsert);
            int count = 0;
            for (int i = 0; i < Column.size(); i++) {
                ArrayList<SqlParam> total = new ArrayList<SqlParam>();
                ArrayList<SqlParam> param1 = convertMapToSqlParam(Column.get(i), listColumn);
                ArrayList<SqlParam> param2 = convertMapToSqlParam(conds.get(i), cond);
                for (int j = 0; j < param1.size(); j++) {
                    total.add(param1.get(j));
                }
                for (int j = 0; j < param2.size(); j++) {
                    total.add(param2.get(j));
                }

                addSqlParam(stm, total, mapDataType);
                stm.addBatch();
                count++;
                if (count > MAX_BATCH) {
                    try {
                        stm.executeBatch();
                    } catch (Exception ex) {
                        logger.error("Error:", ex);
                        throw ex;
                    } finally {
                        stm.clearBatch();
                        count = 0;
                    }
                }
            }
            if (count > 0) {
                stm.executeBatch();
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            ConnectionPool.closeResource(stm);
        }
        logger.info(getThreadName() + ": Update Batch Done!");
    }

    public void addNullSqlParam(PreparedStatement stm, int idxParam, Integer dataType) throws Exception {
        try {
            if (dataType == null) {
                stm.setNull(idxParam, Types.VARCHAR);
            } else {
                switch (dataType) {
                    case DATA_TYPE.INTEGER:
                    case DATA_TYPE.LONG:
                        stm.setNull(idxParam, Types.INTEGER);
                        break;
                    case DATA_TYPE.FLOAT:
                        stm.setNull(idxParam, Types.FLOAT);
                        break;
                    case DATA_TYPE.DOUBLE:
                        stm.setNull(idxParam, Types.DOUBLE);
                        break;
                    case DATA_TYPE.DATE:
                    case DATA_TYPE.DATE_TIME:
                        stm.setNull(idxParam, Types.TIMESTAMP);
                        break;
                    default:
                        stm.setNull(idxParam, Types.LONGVARBINARY);
                        break;
                }
            }
        } catch (Exception ex) {
            throw ex;
        }
    }

    public void addSqlParam(PreparedStatement stm, List<SqlParam> condition) throws Exception {
        addSqlParam(stm, condition, new HashMap<String, Integer>());
    }

    /**
     * @param stm
     * @param condition
     * @throws Exception
     */
    public void addSqlParam(PreparedStatement stm, List<SqlParam> condition, Map<String, Integer> mapDataType) throws Exception {
        if (null == condition || condition.isEmpty()) {
            return;
        }
        SqlParam sql;
        Object param;
        int count = 1;
//        StringBuilder sqlSet = new StringBuilder();
        for (int i = 0; i < condition.size(); i++) {
            // get param
            sql = condition.get(i);
            try {
                if (!StringUtils.equals(sql.getName(), AamConstants.SQL_TEMPLATE.ORDER)) {
                    param = sql.getObject();
                    if (param != null) {
//                        sqlSet.append(String.valueOf(param)+",");
                        if (param instanceof String) {
                            stm.setString(count++, String.valueOf(param));
                        } else if (param instanceof Integer) {
                            int value = Integer.parseInt(String.valueOf(param));
                            stm.setInt(count++, value);
                        } else if (param instanceof Timestamp) {
                            stm.setTimestamp(count++, Timestamp.valueOf(String.valueOf(param)));
                        } else if (param instanceof java.util.Date) {
                            stm.setTimestamp(count++, new Timestamp(((java.util.Date) param).getTime()));
                        } else if (param instanceof Double) {
                            stm.setDouble(count++, Double.parseDouble(String.valueOf(param)));
                        } else if (param instanceof java.sql.Date) {
                            stm.setDate(count++, convertDateToSqlDate((java.util.Date) param));
                        } else if (param instanceof BigDecimal) {
                            stm.setBigDecimal(count++, (BigDecimal) param);
                        } else if (param instanceof Float) {
                            stm.setDouble(count++, Float.parseFloat(String.valueOf(param)));
                        } else if (param instanceof Long) {//20150129_tult3_fix
                            stm.setLong(count++, Long.parseLong(String.valueOf(param)));
                        }
                    } else {
                        addNullSqlParam(stm, count++, mapDataType.get(sql.getName().toUpperCase()));
//                        sqlSet.append("null,");
                    }
                }
            } catch (Exception ex) {
//                System.out.println(count + "--" + sql.getName() + "***" + sql.getObject());
                throw ex;
            }
        }
//        logger.info("sqlSet: "+sqlSet.toString());
    }

    /**
     * @param _result
     * @return
     * @throws Exception
     */
    protected ArrayList<Map<String, Object>> convertResultSetToMap(ResultSet _result) throws Exception {
        ArrayList<Map<String, Object>> mapList = new ArrayList<Map<String, Object>>();
        Map<String, Object> resultMap;
        ResultSetMetaData rsmd;
        while (_result.next()) {
            rsmd = _result.getMetaData();
            resultMap = new HashMap<String, Object>();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                Object value = _result.getObject(i);
                if (value != null) {
                    if (value instanceof java.sql.Date) {
                        value = _result.getTimestamp(i);
                    } else if (value instanceof CLOB) {
                        CLOB clob = (CLOB) value;
                        Long len = clob.length();
                        value = clob.getSubString(1l, len.intValue());
                    }
                }
                resultMap.put(rsmd.getColumnName(i), value);
            }
            mapList.add(resultMap);
        }
        return mapList;
    }

    protected Map<String, Map<String, Object>> convertResultSetToMap(ResultSet _result, String columnName) throws Exception {
        Map<String, Map<String, Object>> map = new HashMap<String, Map<String, Object>>();
        Map<String, Object> resultMap;
        ResultSetMetaData rsmd;
        while (_result.next()) {
            rsmd = _result.getMetaData();
            resultMap = new HashMap<String, Object>();
            for (int i = 1; i <= rsmd.getColumnCount(); i++) {
                String columnNameTmp = rsmd.getColumnName(i);
                Object value = _result.getObject(i);
                if (columnNameTmp.trim().equalsIgnoreCase(columnName.trim())) {
                    if (value != null && !String.valueOf(value).trim().isEmpty()) {
                        resultMap.put(columnNameTmp, value);
                        map.put(String.valueOf(value).toUpperCase(), resultMap);
                    }
                } else {
                    resultMap.put(columnNameTmp, value);
                }
            }
        }
        return map;
    }

    /**
     * @param _map
     * @return
     */
    protected ArrayList<SqlParam> convertMapToSqlParam(Map<String, Object> _map, List<String> listColumn) {
        ArrayList<SqlParam> paramList = new ArrayList<SqlParam>();
        SqlParam sqlParam;
        String key;
        if (null != _map) {
            if (listColumn == null || listColumn.isEmpty()) {
                Iterator<String> iterator = (_map.keySet()).iterator();
                while (iterator.hasNext()) {
                    key = iterator.next().toString();
                    sqlParam = new SqlParam();
                    sqlParam.setName(key);
                    sqlParam.setObject(_map.get(key));
                    paramList.add(sqlParam);
                }
            } else {
                for (String column : listColumn) {
                    sqlParam = new SqlParam();
                    sqlParam.setName(column);
                    sqlParam.setObject(_map.get(column));
                    paramList.add(sqlParam);
                }
            }
        }
        return paramList;
    }

    protected List<List<SqlParam>> convertMapToSqlParam(List<Map<String, Object>> _lst, List<String> listColumn) {
        List<List<SqlParam>> paramList = new ArrayList<List<SqlParam>>();
        if (_lst != null) {
            for (Map<String, Object> map : _lst) {
                paramList.add(convertMapToSqlParam(map, listColumn));
            }
        }
        return paramList;
    }

    /**
     * @param SeqName
     * @return
     * @throws Exception
     */
    public Long getSequence(Connection conn, String SeqName) throws Exception {
        PreparedStatement stm = null;
        ResultSet resultSet = null;
        try {
            stm = conn.prepareStatement("select " + SeqName + ".nextval from dual");
            resultSet = stm.executeQuery();
            long id = 0;
            if (resultSet.next()) {
                id = resultSet.getLong(1);
            }
            return id;
        } catch (Exception ex) {
            throw ex;
        } finally {
            ConnectionPool.closeResource(resultSet);
            ConnectionPool.closeResource(stm);
        }
    }

    public Long getSequence(String SeqName) throws Exception {
        Connection conn = null;
        try {
            conn = getConnection();
            return getSequence(conn, SeqName);
        } catch (Exception ex) {
            throw ex;
        } finally {
            ConnectionPool.closeResource(conn);
        }
    }

    /**
     * @return
     */
    protected java.sql.Date convertDateToSqlDate(java.util.Date utildate) {
        if (utildate != null) {
            java.sql.Date sqlDate = new java.sql.Date(utildate.getTime());
            return sqlDate;
        }
        return null;
    }

    /**
     * @param tableName
     * @param _lst
     * @throws Exception
     */
    private void _delete(Connection conn, String tableName, List<List<SqlParam>> _lst) throws Exception {
        if (null == _lst || StringUtils.isEmpty(tableName)) {
            return;
        }
        logger.info(getThreadName() + " " + tableName + ": Delete size: " + _lst.size());
        List<SqlParam> condition = _lst.get(0);
        ArrayList<String> cond = new ArrayList<String>();
        for (int i = 0; i < condition.size(); i++) {
            cond.add(condition.get(i).getName());
        }
        String sqlUpdate = SqlTemplate.deleteTemplate(tableName, cond);
        PreparedStatement stm = null;
        try {
            logger.info(getThreadName() + ": " + sqlUpdate);
            conn.setAutoCommit(false);
            stm = conn.prepareStatement(sqlUpdate);
            int count = 1;
            for (List<SqlParam> condition1 : _lst) {
                addSqlParam(stm, condition1);
                stm.addBatch();
                count++;
                if (count > MAX_BATCH) {
                    try {
                        stm.executeBatch();
                    } catch (Exception ex) {
                        throw ex;
                    } finally {
                        stm.clearBatch();
                        count = 0;
                    }
                }
            }
            if (count > 0) {
                stm.executeBatch();
            }
        } catch (Exception ex) {
            throw ex;
        } finally {
            ConnectionPool.closeResource(stm);
        }
        logger.info(getThreadName() + ": Delete done!");
    }

    /**
     * @param tableName
     * @param condition
     * @throws Exception
     */
    public void delete(Connection conn, String tableName, Map<String, Object> condition) throws Exception {
        List<Map<String, Object>> _lst = new ArrayList<Map<String, Object>>();
        _lst.add(condition);
        _delete(conn, tableName, convertMapToSqlParam(_lst, null));
    }

    public void delete(Connection conn, String tableName, List<Map<String, Object>> _lst) throws Exception {
        if (_lst != null && !_lst.isEmpty()) {
            _delete(conn, tableName, convertMapToSqlParam(_lst, null));
        }
    }

    public String getThreadName() {
        return Thread.currentThread().getName();
    }

    public String getObjectClob(Object obj) {
        try {
            if (obj != null) {
                if (obj instanceof CLOB) {
                    CLOB clob = (CLOB) obj;
                    Long len = clob.length();
                    return clob.getSubString(1l, len.intValue());
                } else {
                    return obj.toString();
                }
            }
        } catch (Exception ex) {
            logger.error("Error", ex);
            return null;
        }
        return null;
    }


    //R12059_HaNV15_20151223_Add_Start

    /**
     * @param conn
     * @param tableName
     * @param Column
     * @param Condition
     * @param time      truyen vao dieu kien thoi gian
     * @return
     * @throws Exception
     * @Author HaNV15
     */
    public List<Map<String, Object>> selectTime(Connection conn, String tableName, List<String> Column, Map<String, Object> Condition, String time) throws Exception {
        return selectTime(conn, tableName, Column, convertMapToSqlParam(Condition, null), time);
    }

    //where data_time < sysdate - 180
    public List<Map<String, Object>> selectTime(Connection conn, String tableName, List<String> Column, List<SqlParam> Condition, String time) throws Exception {
        if (null == Column || Column.isEmpty()) {
            return null;
        }
        logger.info(getThreadName() + ": Begin select...");
        List<Map<String, Object>> resultMap = null;
        ResultSet result = null;
        PreparedStatement stm = null;
        try {
            String sqlSelect = conn.nativeSQL(SqlTemplate.selectTemplateTime(tableName, Column, Condition, time));
            logger.info(getThreadName() + ": " + sqlSelect);
            stm = conn.prepareStatement(sqlSelect);
            addSqlParam(stm, Condition);
            result = stm.executeQuery();
            resultMap = convertResultSetToMap(result);
        } catch (Exception ex) {
            throw ex;
        } finally {
            ConnectionPool.closeResource(result);
            ConnectionPool.closeResource(stm);
        }
        logger.info(getThreadName() + ": End select...");
        return resultMap;
    }
    //R12059_HaNV15_20151223_Add_Start


    protected Map<String, Integer> getDataTypeOfColumn(String owner, String tableName) throws Exception {
        PreparedStatement preStmt = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            String query = " select COLUMN_NAME, DATA_TYPE from all_tab_columns where owner =  ? and  table_name = ? ";
            conn = getConnection();
            preStmt = conn.prepareStatement(query);
            preStmt.setString(1, owner);
            preStmt.setString(2, tableName);
            rs = preStmt.executeQuery();
            SortedMap<String, Integer> data = new TreeMap<String, Integer>();
            while (rs.next()) {
                String dataType = rs.getString("COLUMN_NAME");
                if (dataType != null) {
                    if ("NUMBER".equals(dataType)) {
                        data.put(rs.getString("COLUMN_NAME"), Types.NUMERIC);
                    } else if ("DATE".equals(dataType)) {
                        data.put(rs.getString("COLUMN_NAME"), Types.TIMESTAMP);
                    } else if ("BLOB".equals(dataType)) {
                        data.put(rs.getString("COLUMN_NAME"), Types.BLOB);
                    } else if ("CLOB".equals(dataType)) {
                        data.put(rs.getString("COLUMN_NAME"), Types.CLOB);
                    } else if ("VARCHAR2".equals(dataType)) {
                        data.put(rs.getString("COLUMN_NAME"), Types.LONGVARCHAR);
                    } else if ("NVARCHAR2".equals(dataType)) {
                        data.put(rs.getString("COLUMN_NAME"), Types.LONGNVARCHAR);
                    } else {
                        data.put(rs.getString("COLUMN_NAME"), Types.LONGVARCHAR);
                    }
                } else {
                    data.put(rs.getString("COLUMN_NAME"), Types.LONGVARCHAR);
                }
            }
            return data;
        } catch (Exception e) {
            throw e;
        } finally {
            ConnectionPool.closeResource(preStmt);
            ConnectionPool.closeResource(rs);
            ConnectionPool.closeResource(conn);
        }
    }

    public void truncateTable(Connection conn, String table) throws Exception {
        PreparedStatement preStmt = null;
        StringBuilder query = new StringBuilder();
        logger.info("BEGIN: TRUNCATE TABLE " + table + " table");
        query.append("TRUNCATE TABLE " + table);
        try {
            preStmt = conn.prepareStatement(query.toString());
            preStmt.executeQuery();
            logger.info("Table " + table + " => Truncate success");
        } catch (Exception e) {
            logger.error("Table " + table + " => Truncate fail");
            throw e;
        } finally {
            ConnectionPool.closeResource(preStmt);
        }
    }

    public void deleteTable(Connection conn, String table) throws Exception {
        PreparedStatement preStmt = null;
        StringBuilder query = new StringBuilder();
        logger.info("BEGIN: DELETE TABLE " + table);
        query.append("DELETE FROM " + table);
        try {
            preStmt = conn.prepareStatement(query.toString());
            preStmt.executeUpdate();
            logger.info("Table " + table + " => delete success");
        } catch (Exception e) {
            logger.error("Table " + table + " => delete fail");
            throw e;
        } finally {
            ConnectionPool.closeResource(preStmt);
        }
    }

    public String getBlobAsString(Blob blob) {
        StringBuffer result = new StringBuffer();
        if (blob != null) {
            int read = 0;
            Reader reader = null;
            char[] buffer = new char[1024];
            try {
                reader = new InputStreamReader(blob.getBinaryStream(), "UTF-8");
                while ((read = reader.read(buffer)) != -1) {
                    result.append(buffer, 0, read);
                }
            } catch (SQLException ex) {
                throw new RuntimeException("Unable to read blob data.", ex);
            } catch (IOException ex) {
                throw new RuntimeException("Unable to read blob data.", ex);
            } finally {
                try {
                    if (reader != null) {
                        reader.close();
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
                ;
            }
        }
        return result.toString();
    }

    public void closeResource(Object... resources) {
        try {
            if (resources != null) {
                for (int i = 0; i < resources.length; i++) {
                    try {
                        Object resource = resources[i];
                        if (resource instanceof ResultSet) {
                            ((ResultSet) resource).close();
                        } else if (resource instanceof Statement) {
                            ((Statement) resource).close();
                        } else if (resource instanceof PreparedStatement) {
                            ((PreparedStatement) resource).close();
                        } else if (resource instanceof CallableStatement) {
                            ((CallableStatement) resource).close();
                        } else if (resource instanceof Connection) {
                            ((Connection) resource).close();
                        }
                    } catch (SQLException e) {
                        logger.error(e.getMessage(), e);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
}
