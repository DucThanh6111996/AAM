/*
 * Copyright (C) 2011 Viettel Telecom. All rights reserved.
 * VIETTEL PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.viettel.it.util;

import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author TrungNT42
 * @version 1.0
 * @since: 8/2/2014 12:01 PM
 */
public class ConnectionPool {
    private static Logger logger = LogManager.getLogger(ConnectionPool.class);

    public static final String ORACLE = "ORACLE";
    public static final String MYSQL = "MYSQL";
    public static final String SQLSERVER = "SQLSERVER";
    public static final String DB2 = "DB2";
    public static final String SYBASE = "SYBASE";
    public static final String ORACLE_TIMESTEN = "TIMESTEN";

    BoneCP pool;

    public ConnectionPool(String protocol, String url, String userName, String passWord) throws Exception {
        try {
            String className = "";
            switch (protocol) {
                case ORACLE:
                    className = "oracle.jdbc.OracleDriver";
                    break;
                case MYSQL:
                    className = "com.mysql.jdbc.Driver";
                    break;
                case SQLSERVER:
                    className = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                    break;
                case DB2:
                    className = "com.ibm.db2.jcc.DB2Driver";
                    break;
                case SYBASE:
                    className = "com.sybase.jdbc3.jdbc.SybDataSource";
                    break;
                default:
                    if (url.toLowerCase().contains(ORACLE.toLowerCase())) {
                        className = "oracle.jdbc.OracleDriver";
                    } else if (url.toLowerCase().contains(MYSQL.toLowerCase())) {
                        className = "com.mysql.jdbc.Driver";
                    } else if (url.toLowerCase().contains(SQLSERVER.toLowerCase())) {
                        className = "com.microsoft.sqlserver.jdbc.SQLServerDriver";
                    } else if (url.toLowerCase().contains(DB2.toLowerCase())) {
                        className = "com.ibm.db2.jcc.DB2Driver";
                    } else if (url.toLowerCase().contains(SYBASE.toLowerCase())) {
                        className = "com.sybase.jdbc3.jdbc.SybDataSource";
                    } else if (url.toLowerCase().contains(ORACLE_TIMESTEN.toLowerCase())) {
                        className = "com.timesten.jdbc.TimesTenDriver";
                    } else {
                        className = "oracle.jdbc.OracleDriver";
                    }
                    break;
            }
            Class.forName(className);
            BoneCPConfig config = new BoneCPConfig();
            config.setJdbcUrl(url);
            config.setUsername(userName);
            config.setPassword(passWord);
            pool = new BoneCP(config);
        } catch (Exception e) {
            throw e;
        }
    }

    public Connection reserveConnection() throws SQLException {
        if (pool != null) {
            return pool.getConnection();
        } else {
            return null;
        }

    }

    public void releaseConnection(Connection connection) {
        try {
            connection.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            //
        }
    }

    public void releaseConnection(PreparedStatement pst) {
        try {
            pst.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
            //
        }
    }

    public void destroy() {
        if (pool != null) {
            pool.shutdown();
        }
    }
}
