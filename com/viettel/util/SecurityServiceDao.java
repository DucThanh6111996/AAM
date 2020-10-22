package com.viettel.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;

import com.viettel.bean.AccountForAppDTO;
import com.viettel.bean.ResultGetAccount;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


/**
 * Created by VTN-PTPM-NV36 on 1/9/2019.
 */
public class SecurityServiceDao {
    private static String CFG_URL_SECURITY = "URL_SECURITY";
    private static String CFG_USER_SECURITY = "USER_SECURITY";
    private static String CFG_PASS_SECURITY = "PASS_SECURITY";
    private static String CFG_ACTIVE_SECURITY = "ACTIVE_SECURITY";
    private static String CFG_TIME_OUT_SECURITY = "TIME_OUT_SECURITY";
    private static String CFG_COUNTRY_CODE_SECURITY = "CFG_COUNTRY_CODE_SECURITY";
    private static Logger logger = LogManager.getLogger(SecurityServiceDao.class);
    //    private static String logAction = "";
    private static String className = SecurityServiceDao.class.getName();

    public static ResultGetAccount getPassSecurity(String ip, String user, String type, String dbId, String host, String countryCode, String passBackup) {

        String logAction = "";
        Date start = new Date();
        AccountForAppDTO accountForAppDTO = null;
        ResultGetAccount resultGetAccount = new ResultGetAccount();
        resultGetAccount.setResultStatus(true);
        String result = "";
        int countPass = 0;
        String urlSecurity = "";
        String userSecurity = "";
        String passSecurity = "";
        String activeSecurity = "";
        String timeOutSecurity = "30000"; //30 giay, 30000 mili giay
        String countryCodeSe = "";

        urlSecurity = AppConfig.getInstance().getProperty("url_security");
        userSecurity = AppConfig.getInstance().getProperty("user_security");
        passSecurity = AppConfig.getInstance().getProperty("pass_security");
        activeSecurity = AppConfig.getInstance().getProperty("active_security");
        timeOutSecurity = AppConfig.getInstance().getProperty("timeout_security");
        countryCodeSe = AppConfig.getInstance().getProperty("country_code");

        if(!isNullOrEmpty(activeSecurity) && "1".equals(activeSecurity)){

            logAction = addContent("", "Start get Account Security \n");
            logger.info("Start get Account Security \n");

            try {
                if (isNullOrEmpty(ip)) {
                    resultGetAccount.setResultStatus(false);
                    resultGetAccount.setResultMessage("Ip is null \n");
                    logAction = addContent(logAction, "Ip is null \n");
                    logger.info("Ip is null \n");
                } else {
                    logAction = addContent(logAction, "Ip :" + ip + "\n");
                    logger.info("Ip :" + ip + "\n");
                }
                if (isNullOrEmpty(user)) {
                    resultGetAccount.setResultStatus(false);
                    resultGetAccount.setResultMessage("Account is null \n");
                    logAction = addContent(logAction, "Account is null \n");
                    logger.info("Account is null \n");
                } else {
                    logAction = addContent(logAction, "Account :" + user + "\n");
                    logger.info("Account :" + user + "\n");
                }
                if (isNullOrEmpty(type)) {
                    logAction = addContent(logAction, "Type is null \n");
                    logger.info("Type is null \n");
                } else {
                    logAction = addContent(logAction, "Type :" + type + "\n");
                    logger.info("Type :" + type + "\n");
                }
//                if (isNullOrEmpty(countryCode)) {
//                    logAction = addContent(logAction, "Country code is null \n");
//                    logger.info("Country code is null \n");
//                } else {
//                    logAction = addContent(logAction, "Country code :" + countryCode + "\n");
//                    logger.info("Country code :" + countryCode + "\n");
//                }
                if (isNullOrEmpty(countryCode)) {
                    countryCode = !isNullOrEmpty(countryCodeSe) ? countryCodeSe : "VNM";
                }
                logAction = addContent(logAction, "Country code :" + countryCode + "\n");
                logger.info("Country code :" + countryCode + "\n");

                List<String> listIp = new ArrayList<>();
                listIp.add(ip);
                URL url = new URL(urlSecurity);
                HttpURLConnection conn = (HttpURLConnection) url.openConnection();
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
                if (result.contains("\"messageResultCode\":\"0\"")) {
                    String json = result.substring(result.indexOf("["), result.indexOf("]") + 1);
                    TypeToken<List<AccountForAppDTO>> token = new TypeToken<List<AccountForAppDTO>>() {
                    };
                    List<AccountForAppDTO> list = new Gson().fromJson(json, token.getType());
                    countPass = list != null ? list.size() : 0;
                    if (list == null || list.size() == 0) {
                        resultGetAccount.setResultMessage("Get pass fail: Can not found password");
                        logAction = addContent(logAction, "Get pass fail: Can not found password");
                        logger.info("Get pass fail: Can not found password");
                    } else if (list != null && list.size() == 1) {
                        resultGetAccount.setResult(list.get(0).getPassword());
                        resultGetAccount.setResultMessage("Get pass success");
                        logAction = addContent(logAction, "Get pass success");
                        logger.info("Get pass success");
                    } else {
                        resultGetAccount.setResultMessage("Get pass fail: Found more than one password, count = " + list.size());
                        logAction = addContent(logAction, "Get pass fail: Found more than one password, count = " + list.size());
                        logger.info("Get pass fail: Found more than one password, count = " + list.size());
                    }
                } else {
                    resultGetAccount.setResultStatus(false);
                    resultGetAccount.setResultMessage("Get pass fail: " + result + "\n");
                    logAction = addContent(logAction, "Get pass fail: " + result + "\n");
                    logger.info("Get pass fail: " + result + "\n");
                }
                conn.disconnect();
            } catch (Exception e) {
                resultGetAccount.setResultStatus(false);
                logAction = addContent(logAction, "Get pass fail: " + e.getMessage());
                resultGetAccount.setResultMessage(logAction);
                logger.info("Get pass fail: " + e.getMessage());
                e.printStackTrace();
            }
            try {
                String message = "";
                if (!isNullOrEmpty(result)) {
                    message = result;
                    if (result.indexOf("\"resultData\":[") > 1) {
                        String json = result.substring(result.indexOf("\"resultData\":["), result.indexOf("]") + 1);
                        message = message.replace(json, "");
                    }
                    message = message + "\n Get the password count is: " + countPass;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            if (isNullOrEmpty(resultGetAccount.getResult())) {
                resultGetAccount.setResultStatus(false);
                resultGetAccount.setResultMessage("Not found pass from security \n");
                resultGetAccount.setResult(passBackup);
                logger.info("Not found pass from security \n");
            }
        }else{
            resultGetAccount.setResultStatus(true);
            resultGetAccount.setResultMessage("Get pass to system \n");
            resultGetAccount.setResult(passBackup);
        }

        return resultGetAccount;
    }

    public static void main(String[] args) {
//        ResultGetAccount resultGetAccount= getPassSecurity("a","10.60.5.6", "test_aam", null, null, "VNM",null,null,null,null,null);
//        System.out.println("Result status:" + resultGetAccount.getResultStatus());
//        System.out.println("Result:" + resultGetAccount.getResult());
//        System.out.println("Result message:" + resultGetAccount.getResultMessage());
    }

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

    public static Boolean CheckNumber(String num){
        Pattern pattern = Pattern.compile("\\d*");
        Matcher matcher = pattern.matcher(num);
        return matcher.matches();
    }

    public static boolean isNullOrEmpty(String content) {
        return content == null || content.trim().isEmpty();
    }
}
