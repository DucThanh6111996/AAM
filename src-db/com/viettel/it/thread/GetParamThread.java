package com.viettel.it.thread;

import com.viettel.bean.ResultGetAccount;
import com.viettel.iim.services.main.JsonResponseBO;
import com.viettel.iim.services.main.ParameterBO;
import com.viettel.it.model.FlowRunAction;
import com.viettel.it.model.Node;
import com.viettel.it.model.NodeAccount;
import com.viettel.it.model.NodeRun;
import com.viettel.it.persistence.FlowRunActionServiceImpl;
import com.viettel.it.persistence.NodeAccountServiceImpl;
import com.viettel.it.util.*;
import com.viettel.passprotector.PassProtector;
import com.viettel.util.Constant;
import org.joda.time.DateTime;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.slf4j.Logger;

import java.sql.*;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Date;

public class GetParamThread implements Runnable {

    protected static Logger logger;
    protected SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");

    protected FlowRunAction flowRunAction;
    protected Node databaseNode;
    protected NodeRun nodeRun;
    protected Integer cycleMonths;

    protected String urlQltn;
    protected String usernameQltn;
    protected String passwordQltn;

    protected long keySession;
    protected Map<String, String> mapConfigSecurity;
    protected Map<String, ResultGetAccount> mapPassGet;


    public GetParamThread() {
        super();
    }

    public GetParamThread(FlowRunAction flowRunAction, Node databaseNode,
                          NodeRun nodeRun, Integer cycleMonths, String urlQltn,
                          String usernameQltn, String passwordQltn, long keySession, Logger logger,
                          Map<String, String> mapConfigSecurity, Map<String, ResultGetAccount> mapPassGet) {

        super();
        this.flowRunAction = flowRunAction;
        this.databaseNode = databaseNode;
        this.nodeRun = nodeRun;
        this.cycleMonths = cycleMonths;
        this.urlQltn = urlQltn;
        this.usernameQltn = usernameQltn;
        this.passwordQltn = passwordQltn;
        this.keySession = keySession;
        this.logger = logger;
        this.mapConfigSecurity = mapConfigSecurity;
        this.mapPassGet = mapPassGet;
    }

    @Override
    public void run() {

    }

    protected Connection getConnection(String url, String username, String password) {
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Connection conn = DriverManager.getConnection(url, username, password);
            return conn;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    protected JSONObject getDatafileParam() {
        JSONObject paramVal = null;
        try {
            ParameterBO paramBO = new ParameterBO("db_id", "db_id", null, "Long", databaseNode.getServerId() + "");
            JsonResponseBO jsonData = IimWebservice.getDataJson(Config.GET_DB_PARAM_DATAFILE, paramBO);
            if (jsonData != null) {
                JSONParser pa = new JSONParser();
                JSONObject objRes = (JSONObject) pa.parse(jsonData.getDataJson());
                JSONArray arrayParam = (JSONArray) objRes.get(Config.DATA_FIELD_NAME);
                if (arrayParam != null && arrayParam.size() > 0) {
                    paramVal = (JSONObject) arrayParam.get(0);
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return paramVal;
    }

    protected NodeAccount getAccount(String accountName, Long serverId,
                                     Long accountType, boolean isAnyCase, Long impactOrMonitorType) {
        NodeAccount account;
        Map<String, Object> filters = new HashMap<String, Object>();
        filters.put("active", Constant.status.active);

        if (accountName != null) {
            filters.put("username", accountName);
        }
        if (impactOrMonitorType != null) {
            filters.put("impactOrMonitor", impactOrMonitorType);
        }

        filters.put("serverId", serverId);
        filters.put("accountType", accountType);

//		filters.put("username", Config.DEFAULT_ACCOUNT_DB_MONITOR);
//		filters.put("serverId", databaseNode.getServerId());

        try {
            account = new NodeAccountServiceImpl().findListExac(filters, null).get(0);
            logger.info("account " + accountName + ": ok");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            account = null;
//			logger.error("Error get account : " + accountName + " === of node: " + databaseNode.getNodeIp());
        }
        // find upcase data
        if (isAnyCase && account == null) {
            try {
                if (accountName != null) {
                    filters.put("username", accountName.toUpperCase());
                }
                account = new NodeAccountServiceImpl().findListExac(filters, null).get(0);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                account = null;
                if (accountName != null) {
                    logger.error("Error get account monitor default : " + accountName.toUpperCase() + " === of node: " + databaseNode.getNodeIp());
                }
            }
        }
        if (account != null) {
            String passBackup = null;
            try {
                passBackup = PasswordEncoder.decrypt(account.getPassword());
            } catch (Exception ex) {
                try {
                    passBackup = PassProtector.decrypt(account.getPassword(), Config.SALT);
                } catch (Exception e) {
                    logger.debug(e.getMessage(), e);
                }
                logger.debug(ex.getMessage(), ex);
            }
            if (Util.isNullOrEmpty(passBackup)) {
                logger.info("Khong lay duoc pass back up");
            }

            String accType = null;
            if (account.getAccountType() != null) {
                if (Constant.ACCOUNT_TYPE_SERVER.equalsIgnoreCase(account.getAccountType().toString())) {
                    accType = Constant.SECURITY_SERVER;
                } else if (Constant.ACCOUNT_TYPE_DATABASE.equalsIgnoreCase(account.getAccountType().toString())) {
                    accType = Constant.SECURITY_DATABASE;
                }
            }
            ResultGetAccount resultGetAccount = SecurityService.getPassSecurity(databaseNode.getNodeIp(), "System"
                    , account.getUsername(), accType, account.getServerId().toString(), null, databaseNode.getCountryCode().getCountryCode()
                    , flowRunAction.getFlowRunId().toString(), passBackup, mapConfigSecurity, mapPassGet);
            if (!resultGetAccount.getResultStatus()) {
                logger.info("Khong lay duoc pass tu security dung chay");
                return null;
            } else {
                account.setPassword(resultGetAccount.getResult());
            }
        }
        return account;
    }

    protected NodeAccount getAccImpactDefault(Long serverId) {
        NodeAccount account = null;
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("impactOrMonitor", Config.ACCOUNT_IMPACT_MONITOR_TYPE.IMPACT.value);
            filters.put("accountType", Config.APP_TYPE.DATABASE.value);
            filters.put("serverId", serverId);
            filters.put("active", Constant.status.active);
//			filters.put("itBusinessNode-" + GenericDaoImplNewV2.NEQ, 1L);
            List<NodeAccount> nodeAccounts = new NodeAccountServiceImpl().findList(filters, null);
            if (nodeAccounts != null) {
                for (NodeAccount nodeAccount : nodeAccounts) {
                    if (nodeAccount.getItBusinessNode() == null || nodeAccount.getItBusinessNode() == 0L) {
                        account = nodeAccount;
                        break;
                    }
                }
            }
            if (account != null) {
                String passBackup = null;
                try {
                    passBackup = PasswordEncoder.decrypt(account.getPassword());
                } catch (Exception ex) {
                    try {
                        passBackup = PassProtector.decrypt(account.getPassword(), Config.SALT);
                    } catch (Exception e) {
                        logger.debug(e.getMessage(), e);
                    }
                    logger.debug(ex.getMessage(), ex);
                }
                if (Util.isNullOrEmpty(passBackup)) {
                    logger.info("Khong lay duoc pass back up");
                }
                String accType = null;
                if (account.getAccountType() != null) {
                    if (Constant.ACCOUNT_TYPE_SERVER.equalsIgnoreCase(account.getAccountType().toString())) {
                        accType = Constant.SECURITY_SERVER;
                    } else if (Constant.ACCOUNT_TYPE_DATABASE.equalsIgnoreCase(account.getAccountType().toString())) {
                        accType = Constant.SECURITY_DATABASE;
                    }
                }
                ResultGetAccount resultGetAccount = SecurityService.getPassSecurity(databaseNode.getNodeIp(), "System"
                        , account.getUsername(), accType, account.getServerId().toString(), null, databaseNode.getCountryCode().getCountryCode()
                        , flowRunAction.getFlowRunId().toString(), account.getPassword(), mapConfigSecurity, mapPassGet);
                if (!resultGetAccount.getResultStatus()) {
                    logger.info("Khong lay duoc pass tu security dung chay");
                    return null;
                } else {
                    account.setPassword(resultGetAccount.getResult());
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            account = null;
        }
        return account;
    }

    protected String getUrl(Node database) {
        String url = null;
        try {
            if (database.getJdbcUrl() != null) {
                url = database.getJdbcUrl();
            }
//			url = "jdbc:oracle:thin:@10.60.6.177:1521/qlts";
//			if (database.getNodeIp() != null 
//					&& database.getPort() != null
//					&& database.getServiceName() != null) {
//				url = "jdbc:oracle:thin:@".concat(database.getNodeIp()).concat(database.getPort()+"").concat(database.getServiceName());
//			}
        } catch (Exception e) {
            logger.error("Error", e);
        }
        return url;
    }

    protected String getSuffix(int type, DateTime dateTime) {
        String suffix = null;
        try {
            switch (type) {
                case 1: // YYYYMMDD (20170301)
                    suffix = dateTime.getYear() + ""
                            + ((dateTime.getMonthOfYear() < 10) ? "0" + dateTime.getMonthOfYear() : dateTime.getMonthOfYear())
                            + ((dateTime.getDayOfMonth() < 10) ? "0" + dateTime.getDayOfMonth() : dateTime.getDayOfMonth());
                    break;
                case 2: // YYYYMM (201703)
                    suffix = dateTime.getYear() + ""
                            + ((dateTime.getMonthOfYear() < 10) ? "0" + dateTime.getMonthOfYear() : dateTime.getMonthOfYear());
                    break;
                case 3: // YYYY (2017)
//				dateTime = dateTime.plusYears(1);
                    suffix = dateTime.getYear() + "";
                    break;
                default:
                    break;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return suffix;
    }


    protected void setFinishBuildDT(FlowRunAction flowRun) {
        try {
            if (flowRun != null) {
                flowRun.setStatus(Config.DT_FINISH_BUILD);
                new FlowRunActionServiceImpl().saveOrUpdate(flowRun);
                logger.info(">>>>>>> FINISH BUILD DT: " + flowRun.getFlowRunName());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public Node getDatabaseNode() {
        return databaseNode;
    }

    public void setDatabaseNode(Node databaseNode) {
        this.databaseNode = databaseNode;
    }

    public int getCycleMonths() {
        return cycleMonths;
    }

    public void setCycleMonths(int cycleMonths) {
        this.cycleMonths = cycleMonths;
    }

    public static void main(String[] args) {
//		DateTime dt = new DateTime();
        try {
            Connection conn = new GetParamThread().getConnection("jdbc:oracle:thin:@(DESCRIPTION=(FAILOVER=on)(LOAD_BALANCE=yes)(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=192.168.131.22)(PORT=1521))(ADDRESS=(PROTOCOL=TCP)(HOST=192.168.131.23)(PORT=1521)))(CONNECT_DATA=(FAILOVER_MODE=(TYPE=select)"
                    + "(METHOD=basic)(RETRIES=180)(DELAY=5))(SERVICE_NAME=ocskpi)))", "ptpm_checklist", "Qwbx123q");

            String sql2 = "update NODE set IMPACT_TIME = ? where node_id = ?";
            PreparedStatement pstmt;
            ResultSet rs;

            pstmt = conn.prepareStatement(sql2);
            pstmt.setDate(1, new java.sql.Date(new Date().getTime()));
            pstmt.setLong(2, 205218);
            rs = pstmt.executeQuery();

            rs.close();
            pstmt.close();
            conn.close();

        } catch (SQLException e) {
            // TODO Auto-generated catch block
            logger.error(e.getMessage(), e);
        }
    }

    public long getKeySession() {
        return keySession;
    }

    public void setKeySession(long keySession) {
        this.keySession = keySession;
    }

}
