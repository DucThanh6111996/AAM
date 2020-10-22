package com.viettel.dao;

import com.google.common.base.Throwables;
import com.jolbox.bonecp.BoneCP;
import com.jolbox.bonecp.BoneCPConfig;
import com.jolbox.bonecp.ConnectionHandle;
import com.viettel.bean.ResultGetAccount;
import com.viettel.util.AppConfig;
import com.viettel.util.PasswordEncoder;
import com.viettel.util.SecurityServiceDao;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.InputStream;
import java.sql.*;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author quanns2
 */
public class ConnectionPool {
    public interface DB_CONFIG {
        String AAM = "aam";
    }

    private int numberFail = 0;

    private Map<String, BoneCP> mapPool = new HashMap<>();
    private static ConnectionPool instance;
    private static Logger logger = LogManager.getLogger(ConnectionPool.class);

    public static synchronized ConnectionPool getInstance() throws Exception {
        if (instance == null) {
            instance = new ConnectionPool();
        }
        return instance;
    }

    private void initConnectionPool(String source) throws Exception {
        String configFile = AppConfig.getInstance().getProperty("db.bonecp.config");
        // 1. Setup the connection pool
        try {
//            Class.forName("com.mysql.jdbc.Driver");
            Class.forName("oracle.jdbc.OracleDriver");
//            FileInputStream fis = new FileInputStream(configFile);
            InputStream fis = AppConfig.class.getResourceAsStream("/" + configFile);
            BoneCPConfig config = new BoneCPConfig(fis, source);
            config.setJdbcUrl(AppConfig.getInstance().getProperty("db.url"));
            config.setUsername(AppConfig.getInstance().getProperty("db.username"));
            //20181023_tudn_start load pass security
            config.setPassword(PasswordEncoder.decrypt(AppConfig.getInstance().getProperty("db.password")));
            String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";

            Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
            Matcher matcher = pattern.matcher(AppConfig.getInstance().getProperty("db.url"));
            String ip = "";
            if(matcher.find()){
                if (matcher.group(0)!=null && !"".equals(matcher.group(0))) {
                    ip = matcher.group(0);
                }
            }
//            String passBackup = "";
//            try {
//                passBackup = PasswordEncoder.decrypt(AppConfig.getInstance().getProperty("db.password"));
//            } catch (Exception e) {
//                logger.error(e.getMessage(), e);
//                passBackup = !SecurityServiceDao.isNullOrEmpty(AppConfig.getInstance().getProperty("db.password"))?AppConfig.getInstance().getProperty("db.password"):"";
//            }
//            ResultGetAccount resultGetAccount = SecurityServiceDao.getPassSecurity(ip
//                    , config.getUsername(), AamConstants.SECURITY_DATABASE, null, null, null
//                    , passBackup);
//            if (!resultGetAccount.getResultStatus() && SecurityServiceDao.isNullOrEmpty(resultGetAccount.getResult())) {
//                throw new Exception(resultGetAccount.getResultMessage());
//            }
//            config.setPassword(resultGetAccount.getResult());
            //20181023_tudn_end load pass security
            fis.close();
            mapPool.put(source, new BoneCP(config));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new Exception("Create connectionPool " + source + " failed");
        }

    }

    public String getCallerName() {
        try {
            StackTraceElement[] stacktrace = Thread.currentThread().getStackTrace();
//            StackTraceElement e = stacktrace[3];//maybe this number needs to be corrected
            StringBuilder str = new StringBuilder();
            for (StackTraceElement stackTraceElement : stacktrace) {
                if ("execute".equalsIgnoreCase(stackTraceElement.getMethodName())) {
                    break;
                }
                str.append(stackTraceElement.getMethodName() + "_");
            }

            return str.toString();
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return "UNKNOWN";
        }

    }

    public synchronized Connection getConnection(String source) throws Exception {
        Connection con;
        String callName = getCallerName();
        logger.info("Begin get connection " + source + " " + Thread.currentThread().getName());
        boolean status = true;
        try {
            if (mapPool.get(source) == null) {
                initConnectionPool(source);
            }
            con = mapPool.get(source).getConnection();
            boolean bConnectionError = con.isClosed() || !((ConnectionHandle) con).isConnectionAlive() || ((ConnectionHandle) con).isPossiblyBroken();
            int i = 0;
            while (bConnectionError) {
                try {
                    logger.info(Thread.currentThread().getName() + " Got connection pool " + source + " " + con +
                            ", Close: " + ((ConnectionHandle) con).isClosed() +
                            ", Alive: " + ((ConnectionHandle) con).isConnectionAlive() +
                            ", Broken: " + ((ConnectionHandle) con).isPossiblyBroken());
                    con.close();
                } catch (SQLException e) {
                    logger.error(source, e);
                }
                if (++i >= 5) {
                    throw new Exception("Get connection " + source + " failed i >" + i);
                }
                logger.info("Fail lan: " + i + " bat dau retry connection");
                initConnectionPool(source);
                con = mapPool.get(source).getConnection();
                bConnectionError = con.isClosed() || !((ConnectionHandle) con).isConnectionAlive() || ((ConnectionHandle) con).isPossiblyBroken();
            }
            numberFail = 0;
            logger.info(Thread.currentThread().getName() + " Got connection " + source + "OK");
            return con;
        } catch (SQLException e) {
            String msgError = Throwables.getStackTraceAsString(e);
            logger.error(source, e);
            logger.error(msgError);
            if (msgError.contains("Timed out waiting for a free available connection")) {
                numberFail++;
                logger.error(System.getProperty("topology.name") + ", port: " + System.getProperty("worker.port") + ", numberFail: " + numberFail);
                logger.info(Thread.currentThread().getName() + " : " + getCallerName() + " \nfree : " + mapPool.get(source).getTotalFree() + " , busy : " + mapPool.get(source).getTotalLeased());
                if (mapPool.get(source).getTotalFree() <= 0 &&
                        (mapPool.get(source).getTotalLeased() <= 0 || numberFail > 50)) {
                    logger.info("=============D ESTROY POOL DB ==============");
                    destroy(source);
                    logger.info("============= INNIT POOL DB ==============");
                    initConnectionPool(source);
                    logger.info("============= END POOL DB ==============");
                    logger.info("Begin get DB from new pool");
                    con = mapPool.get(source).getConnection();
                    logger.info("End get DB from new pool");
                    return con;
                } else {
                    status = false;
                    throw new Exception("Get connection " + source + " db failed");
                }
            } else {
                status = false;
                throw new Exception("Get connection " + source + " db failed");
            }
        } finally {
            logger.info("End get connection " + source + " " + Thread.currentThread().getName() + ": " + (status ? "OK" : "NOK"));
        }
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

    public void destroy() {
        try {
            if (mapPool != null) {
                for (Map.Entry<String, BoneCP> cpEntry : mapPool.entrySet()) {
                    BoneCP boneCP = cpEntry.getValue();
                    boneCP.shutdown();
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void destroy(String source) {
        try {
            if (mapPool.get(source) != null) {
                BoneCP boneCP = mapPool.get(source);
                boneCP.shutdown();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            mapPool.remove(source);
        }
    }
}
