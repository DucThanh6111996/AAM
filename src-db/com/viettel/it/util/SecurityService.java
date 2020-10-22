package com.viettel.it.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import com.viettel.bean.AccountForAppDTO;
import com.viettel.bean.ResultGetAccount;
import com.viettel.it.model.CatConfig;
import com.viettel.controller.AamConstants;
import com.viettel.util.AppConfig;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by quytv7 on 10/22/2018.
 */
public class SecurityService {
    private static Logger logger = LogManager.getLogger(SecurityService.class);
    //    private static String logAction = "";
    private static String className = SecurityService.class.getName();

    public static ResultGetAccount getPassSecurity(String ip, String userTD, String user, String type, String dbId, String host, String countryCode, String requestId, String passBackup, Map<String, String> mapConfigSecurity, Map<String, ResultGetAccount> mapPassGet) {
        Util.checkAndPrintObject(logger, "Get pass from security:= ","ip", ip, "user", user,
                "type", type, "dbId", dbId, "host", host, "countryCode", countryCode);

        if (mapPassGet == null) {
            mapPassGet = new HashMap<>();
            logger.info("mapPassGetSuccess is null");
        }
        String logAction = "";
        AccountForAppDTO accountForAppDTO = null;
        Date startTime = new Date();
        String result = "";
        int countPass = 0;
        boolean checkPrintLog = true;
        ResultGetAccount resultGetAccount = new ResultGetAccount();
        String key = "";
        try {
            String urlSecurity = "";
            String userSecurity = "";
            String passSecurity = "";
            String activeSecurity = "";
            String timeOutSecurity = "30000";
            String countryCodeSe = "";

            if (mapConfigSecurity == null) {
                urlSecurity = AppConfig.getInstance().getProperty("url_security");
                userSecurity = AppConfig.getInstance().getProperty("user_security");
                passSecurity = AppConfig.getInstance().getProperty("pass_security");
                activeSecurity = AppConfig.getInstance().getProperty("active_security");
                timeOutSecurity = AppConfig.getInstance().getProperty("timeout_security");
                countryCodeSe = AppConfig.getInstance().getProperty("country_code");
            } else {
                urlSecurity = mapConfigSecurity.get(AamConstants.CFG_URL_SECURITY);
                userSecurity = mapConfigSecurity.get(AamConstants.CFG_USER_SECURITY);
                passSecurity = mapConfigSecurity.get(AamConstants.CFG_PASS_SECURITY);
                activeSecurity = mapConfigSecurity.get(AamConstants.CFG_ACTIVE_SECURITY);
                timeOutSecurity = mapConfigSecurity.get(AamConstants.CFG_TIME_OUT_SECURITY);
                countryCodeSe = mapConfigSecurity.get(AamConstants.CFG_COUNTRY_CODE_SECURITY);
            }

            logAction = addContent("", "Start get Account Security\n");
            logger.info("Start get Account Security");
            HttpURLConnection conn = null;
            try {
                if (Util.isNullOrEmpty(ip)) {
                    resultGetAccount.setResultStatus(false);
                    resultGetAccount.setResultMessage("Ip is null \n");
                    logAction = addContent(logAction, "Ip is null \n");
                } else {
                    logAction = addContent(logAction, "Ip :" + ip + "\n");
                }
                if (Util.isNullOrEmpty(user)) {
                    resultGetAccount.setResultStatus(false);
                    resultGetAccount.setResultMessage("Account is null \n");
                    logAction = addContent(logAction, "Account is null \n");
                } else {
                    logAction = addContent(logAction, "Account :" + user + "\n");
                }
                if (Util.isNullOrEmpty(type)) {
                    logAction = addContent(logAction, "Type is null \n");
                } else {
                    logAction = addContent(logAction, "Type :" + type + "\n");
                }
                if (Util.isNullOrEmpty(dbId)) {
                    logAction = addContent(logAction, "dbId is null \n");
                } else {
                    logAction = addContent(logAction, "dbId :" + dbId + "\n");
                }
                if (Util.isNullOrEmpty(host)) {
                    logAction = addContent(logAction, "host is null \n");
                } else {
                    logAction = addContent(logAction, "host :" + host + "\n");
                }
                if (Util.isNullOrEmpty(countryCode)) {
                    countryCode = !Util.isNullOrEmpty(countryCodeSe) ? countryCodeSe : "VNM";
                }
                logAction = addContent(logAction, "Country code :" + countryCode + "\n");
                key = changeNulltoEmpty(countryCode) + "_" + changeNulltoEmpty(ip) + "_" + changeNulltoEmpty(user)
                        + "_" + changeNulltoEmpty(type) + "_" + changeNulltoEmpty(dbId) + "_" + changeNulltoEmpty(host);
                logger.info("Key: " + key);
                if (resultGetAccount != null && resultGetAccount.getResultStatus() != null && !resultGetAccount.getResultStatus() && !mapPassGet.containsKey(key)) {
                    logger.info("Get pass from security fail: Not enough data for(" + key + ")");
                    resultGetAccount.setResultMessage("Get pass from security fail: Not enough data for(" + key + ")");
                    resultGetAccount.setResultStatus(false);
                    mapPassGet.put(key, resultGetAccount);
                    return resultGetAccount;
                }
                if (mapPassGet != null && mapPassGet.containsKey(key)) {
                    checkPrintLog = false;
                    logger.info("Get pass in mapPassGet with result: " + (mapPassGet.get(key).getResultStatus() != null ? mapPassGet.get(key).getResultStatus() : "null"));
                    resultGetAccount = mapPassGet.get(key);
                    return resultGetAccount;
                }

                List<String> listIp = new ArrayList<>();
                listIp.add(ip);
                for (int i = 0; i < 3; i++) {
                    try {
                        URL url = new URL(urlSecurity);
                        conn = (HttpURLConnection) url.openConnection();
                        conn.setDoOutput(true);
                        conn.setRequestMethod("POST");
                        conn.setRequestProperty("Content-Type", "application/json");
                        conn.setConnectTimeout(Integer.parseInt(timeOutSecurity));
                        conn.setReadTimeout(Integer.parseInt(timeOutSecurity));
                        accountForAppDTO = new AccountForAppDTO();
                        accountForAppDTO.setCountryCode(countryCode);
                        accountForAppDTO.setIpList(listIp);
                        accountForAppDTO.setPasswordAuth(passSecurity);
                        accountForAppDTO.setUsernameAuth(userSecurity);
                        accountForAppDTO.setUserName(user);
                        accountForAppDTO.setType(type);
                        accountForAppDTO.setDbid(dbId);
                        accountForAppDTO.setHost(host);

                        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                        String input = ow.writeValueAsString(accountForAppDTO);
                        OutputStream os = conn.getOutputStream();
                        os.write(input.getBytes());
                        os.flush();

                        BufferedReader br = new BufferedReader(new InputStreamReader(
                                (conn.getInputStream())));
                        StringBuilder sb = new StringBuilder();
                        String output;

                        while ((output = br.readLine()) != null) {
                            sb.append(output);
                        }
                        result = sb.toString();
                        break;
                    } catch (Exception ex) {
                        logger.info("Loi ket noi toi security lan " + i);
                        logger.error(ex.getMessage());
                        if (i == 2) {
                            throw ex;
                        }
                    }
                }
                if (result.contains("\"messageResultCode\":\"0\"")) {
                    String json = result.substring(result.indexOf("["), result.indexOf("]") + 1);
                    TypeToken<List<AccountForAppDTO>> token = new TypeToken<List<AccountForAppDTO>>() {
                    };
                    List<AccountForAppDTO> list = new Gson().fromJson(json, token.getType());
                    countPass = list != null ? list.size() : 0;
                    if (list == null || list.size() == 0) {
                        resultGetAccount.setResultMessage("Get pass from security fail: Can not found password");
                        resultGetAccount.setResultStatus(false);
                        logAction = addContent(logAction, "Get pass from security fail: Can not found password");
                        logger.info("Get pass from security fail: Can not found password");
                    } else if (list != null && list.size() == 1) {
                        resultGetAccount.setResult(list.get(0).getPassword());
                        resultGetAccount.setResultStatus(true);
                        resultGetAccount.setResultMessage("Get pass success");
                        logAction = addContent(logAction, "Get pass success");
                        logger.info("Get pass success");
                    } else {
                        resultGetAccount.setResultMessage("Get pass from security fail: Found more than one password, count = " + list.size());
                        resultGetAccount.setResultStatus(false);
                        logAction = addContent(logAction, "Get pass from security fail: Found more than one password, count = " + list.size());
                        logger.info("Get pass from security fail: Found more than one password, count = " + list.size());
                    }
                } else {
                    resultGetAccount.setResultMessage("Get pass from security fail: " + result + "\n");
                    resultGetAccount.setResultStatus(false);
                    logAction = addContent(logAction, "Get pass from security fail: " + result + "\n");
                    logger.info("Get pass from security fail: " + result);
                }

            } catch (Exception e) {
                resultGetAccount.setResultMessage("Get pass from security fail: " + e.getMessage() + "\n");
                resultGetAccount.setResultStatus(false);
                logAction = addContent(logAction, "Get pass from security fail: " + e.getMessage() + "\n");
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
            if (resultGetAccount.getResultStatus() == null) {
                resultGetAccount.setResultStatus(false);
                resultGetAccount.setResultMessage("Get pass from security fail: Not found pass from security \n");
                logAction = addContent(logAction, resultGetAccount.getResultMessage() + "\n");
                logger.info("Not found pass from security");
            }
            if (resultGetAccount.getResultStatus()) {
                logger.info("Get pass success, return ket qua");
                return resultGetAccount;
            } else {
                if (!Util.isNullOrEmpty(activeSecurity) && "1".equals(activeSecurity)) {
                    logger.info("Get pass from security fail, do cau hinh activeSecurity = 1 nen check local lay pass");
                    if (Util.isNullOrEmpty(passBackup)) {
                        resultGetAccount.setResultStatus(false);
                        resultGetAccount.setResultMessage(resultGetAccount.getResultMessage() + "\n Get pass local fail return result");
                        logAction = addContent(logAction, resultGetAccount.getResultMessage() + "\n");
                        logger.info("Get pass local fail, return ket qua");
                    } else {
                        resultGetAccount.setResultStatus(true);
                        resultGetAccount.setResult(passBackup);
                        resultGetAccount.setResultMessage("\n Get pass local success return result");
                        logAction = addContent(logAction, resultGetAccount.getResultMessage() + "\n");
                        logger.info("Get pass local success return result");
                    }
                } else {
                    logger.info("Get pass from security fail, return ket qua");
                    return resultGetAccount;
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if(resultGetAccount.getResultStatus() == null){
                logger.error("ResultStatus is null with key = " + key);
            }
            if(!mapPassGet.containsKey(key)){
                mapPassGet.put(key, resultGetAccount);
                logger.info("Luu pass lay duoc vao map dung cho lan sau: key = " + key + ", size:" + mapPassGet.size());
            }
            if (checkPrintLog) {
                try {
                    String message = "";
                    if (!Util.isNullOrEmpty(result)) {
                        message = result;
                        if (result.indexOf("\"resultData\":[") > 1) {
                            String json = result.substring(result.indexOf("\"resultData\":["), result.indexOf("]") + 1);
                            message = message.replace(json, "");
                        }
                        message = message + "\n Get the password count is: " + countPass;
                    }
                    String resultFinal = "0";
                    if (resultGetAccount.getResultStatus()) {
                        if (resultGetAccount.getResultMessage().contains("Get pass local success return result")) {
                            resultFinal = "0";
                        } else {
                            resultFinal = "1";
                        }
                    } else {
                        resultFinal = "0";
                    }
                    LogUtils.writeLogSecurity("TDTT", startTime, new Date(), userTD, LogUtils.getRemoteIpClient(), LogUtils.getUrl(),
                            SecurityService.class.getName(), Thread.currentThread().getStackTrace()[1].getMethodName(), "GET_PASS_SECURITY"
                            , logAction, requestId, message, resultFinal);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }
        }
        return resultGetAccount;
    }

    public static String changeNulltoEmpty(String str) {
        if (str == null) {
            return "null";
        } else {
            return str;
        }
    }

    public static void main(String[] args) {
//        ResultGetAccount resultGetAccount= getPassSecurity("10.60.129.74", "vmsa", null, "VNM");
//        System.out.println("Result status:" + resultGetAccount.getResultStatus());
//        System.out.println("Result:" + resultGetAccount.getResult());
//        System.out.println("Result message:" + resultGetAccount.getResultMessage());
    }

    //    public static StringBuilder addContent(StringBuilder content, String addCont) {
//        try {
//            if ("".equals(content)) {
//                return new StringBuilder(addCont);
//            } else {
//                return content.append(( "\r, ---" + (addCont == null ? "null" : addCont)));
//            }
//        } catch (Exception ex) {
//            logger.error(ex.getMessage(), ex);
//            return content.append(content + ", " + ex.getMessage());
//        }
//    }
    public static String addContent(String content, String addCont) {
        try {
            if ("".equals(content)) {
                return addCont;
            } else {
                return content + ", " + (addCont == null ? "null" : addCont);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return content + ", " + ex.getMessage();
        }
    }

    public static Boolean CheckNumber(String num) {
        Pattern pattern = Pattern.compile("\\d*");
        Matcher matcher = pattern.matcher(num);
        return matcher.matches();
    }

    public static Map<String, String> getConfigSecurity() {
        Map<String, String> map = null;
        try {

            String urlSecurity = "";
            String userSecurity = "";
            String passSecurity = "";
            String activeSecurity = "";
            String timeOutSecurity = "30000";
            String countryCodeSec = "";

            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            Map<String, Object> filter = new HashMap<>();
            filter.put("id.configGroup-EXAC", AamConstants.CFG_SECURITY);
            filter.put("isActive-EXAC", 1L);
            List<com.viettel.it.model.CatConfig> catConfigs = new com.viettel.it.persistence.CatConfigServiceImpl().findList(filter, orders);

            if (catConfigs == null || catConfigs.isEmpty()) {
                return map;
            } else {
                for (CatConfig cfg : catConfigs) {
                    if (AamConstants.CFG_ACTIVE_SECURITY.equalsIgnoreCase(cfg.getId().getPropertyKey())) {
                        activeSecurity = cfg.getPropertyValue();
                    } else if (AamConstants.CFG_URL_SECURITY.equalsIgnoreCase(cfg.getId().getPropertyKey())) {
                        urlSecurity = cfg.getPropertyValue();
                    } else if (AamConstants.CFG_USER_SECURITY.equalsIgnoreCase(cfg.getId().getPropertyKey())) {
                        userSecurity = cfg.getPropertyValue();
                    } else if (AamConstants.CFG_PASS_SECURITY.equalsIgnoreCase(cfg.getId().getPropertyKey())) {
                        passSecurity = cfg.getPropertyValue();
                    } else if (AamConstants.CFG_TIME_OUT_SECURITY.equalsIgnoreCase(cfg.getId().getPropertyKey())) {
                        if (CheckNumber(cfg.getPropertyValue())) {
                            timeOutSecurity = cfg.getPropertyValue();
                        }
                    } else if (AamConstants.CFG_COUNTRY_CODE_SECURITY.equalsIgnoreCase(cfg.getId().getPropertyKey())) {
                        countryCodeSec = cfg.getPropertyValue();
                    }
                }

            }
            if (!Util.isNullOrEmpty(activeSecurity)
                    && !Util.isNullOrEmpty(urlSecurity) && !Util.isNullOrEmpty(userSecurity) && !Util.isNullOrEmpty(passSecurity)) {
                map = new HashMap<>();
                map.put(AamConstants.CFG_ACTIVE_SECURITY, activeSecurity);
                map.put(AamConstants.CFG_URL_SECURITY, urlSecurity);
                map.put(AamConstants.CFG_USER_SECURITY, userSecurity);
                map.put(AamConstants.CFG_PASS_SECURITY, passSecurity);
                map.put(AamConstants.CFG_TIME_OUT_SECURITY, timeOutSecurity);
                map.put(AamConstants.CFG_COUNTRY_CODE_SECURITY, countryCodeSec);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return map;
    }
}
