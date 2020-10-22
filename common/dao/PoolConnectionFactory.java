package com.viettel.dao;

import org.apache.commons.dbcp2.ConnectionFactory;
import org.apache.commons.dbcp2.DriverManagerConnectionFactory;
import org.apache.commons.dbcp2.PoolingDriver;
import org.apache.commons.pool2.ObjectPool;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author quanns2
 */
public class PoolConnectionFactory {
    private static Logger logger = LogManager.getLogger(PoolConnectionFactory.class);
    
    public static final String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    public static final String ORACLE_DRIVER = "oracle.jdbc.OracleDriver";
    public static final String DBCP_DRIVER = "org.apache.commons.dbcp2.PoolingDriver";
    private static Class dirverClass;
    private static PoolingDriver driver;

    /**
     *
     * @param driver
     */
    public synchronized static void registerJDBCDriver(String driver) {
        try {
            dirverClass = Class.forName(driver);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
    }

    /**
     * Get a Connection Factory, the default implementation is a
     * DriverManagerConnectionFactory
     *
     * @param connectionURI
     * @param user
     * @param password
     * @return The Factory
     */
    public static ConnectionFactory getConnFactory(String connectionURI,
                                                   String user, String password) {
        ConnectionFactory driverManagerConnectionFactory = new DriverManagerConnectionFactory(
                connectionURI, user, password);
        return driverManagerConnectionFactory;
    }

    /**
     *
     * @return the DBCP Driver
     */
    public synchronized static PoolingDriver getDBCPDriver() {
        try {
            Class.forName(DBCP_DRIVER);
        } catch (ClassNotFoundException e) {
            logger.error(e.getMessage(), e);
        }
        try {
            driver = (PoolingDriver) DriverManager
                    .getDriver("jdbc:apache:commons:dbcp:");
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
        }
        return driver;
    }

    /**
     * Registry a Pool in the DBCP Driver
     *
     * @param poolName
     * @param pool
     */
    public static void registerPool(String poolName, ObjectPool pool) {
        driver.registerPool(poolName, pool);
    }
}
