package com.viettel.dao;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.PoolableConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.dbcp2.Utils;
import org.apache.commons.pool2.ObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPool;
import org.apache.commons.pool2.impl.GenericObjectPoolConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.*;

/**
 * @author quanns2
 */
public class DbConnectionPool {
    private static Logger logger = LogManager.getLogger(DbConnectionPool.class);

    public interface DB_CONFIG {
        String AAM = "dbcp-aam";
    }

    /*private int numberFail = 0;
    private Map<String, BoneCP> mapPool = new HashMap<>();*/
    private static DbConnectionPool instance;

    public static synchronized DbConnectionPool getInstance() throws Exception {
        if (instance == null) {
            instance = new DbConnectionPool();
        }
        return instance;
    }

    private void initConnectionPool(String source) throws Exception {
        // 1. Register the Driver to the jbdc.driver java property
        PoolConnectionFactory.registerJDBCDriver(PoolConnectionFactory.ORACLE_DRIVER);

        // 2. Create the Connection Factory (DriverManagerConnectionFactory)
        ConnectionFactory connectionFactory = PoolConnectionFactory.getConnFactory("", "", "");

        // 3. Instantiate the Factory of Pooled Objects
        PoolableConnectionFactory poolfactory = new PoolableConnectionFactory(connectionFactory, null);

        GenericObjectPoolConfig config = new GenericObjectPoolConfig();
        config.setMaxTotal(50);
        config.setMaxIdle(10);
        config.setMinIdle(1);

        // 4. Create the Pool with the PoolableConnection objects
        ObjectPool connectionPool = new GenericObjectPool(poolfactory, config);

        // 5. Set the objectPool to enforces the association (prevent bugs)
        poolfactory.setPool(connectionPool);

        // 6. Get the Driver of the pool and register them
        PoolingDriver dbcpDriver = PoolConnectionFactory.getDBCPDriver();
        dbcpDriver.registerPool(DB_CONFIG.AAM, connectionPool);
    }


    public synchronized Connection getConnection(String source) throws Exception {
        Connection connJCG = DriverManager.getConnection("jdbc:apache:commons:dbcp:dbcp-aam");

        // Print Some Properties.
        System.out.println("Hashcode: " + connJCG.hashCode());
        System.out.println("JDBC Driver: " + connJCG.getMetaData().getDriverName());
        System.out.println("URI DB: " + connJCG.getMetaData().getURL());

        // 8. Close the connection to return them to the pool. Instead of
        // connJCG.close();
//        Utils.closeQuietly(connJCG);

        return connJCG;
    }

    public static void closeResource(Object resource) {
        try {
            if (resource != null) {
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
            }
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
