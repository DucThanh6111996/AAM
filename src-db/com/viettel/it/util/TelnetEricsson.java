/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.util;

/**
 *
 * @author hienhv4
 */
public class TelnetEricsson extends TelnetClientUtil {
    
    private final String nodeType;

    public TelnetEricsson(String hostName, Integer port, String vendor, String osType, String nodeType) {
        super(hostName, port, vendor);
        this.setOs(osType);
        cmdExit = "exit";
        this.nodeType = nodeType;
    }

    @Override
    public String connect(String username, String password) throws Exception {
        log.info("Ericsson, port = " + port + ", os_type = " + getOs() + ", isDebug = " + isDebug
                + ", ip = " + hostName);
        if (port == 5000) {
            return connectErissonPort5000(username, password);
        }
        if (getOs() != null && !getOs().trim().isEmpty() && getOs().toLowerCase().contains("linux")) {
            return connectErissonLinux(username, password);
        }

        log.info("LOGIN Window");
        setShellPromt("Windows NT Domain:");
        connect("(.*)login(.*):\\s*$", username + "\r\n", "(.*)ssword:\\s*$", password + "\r\n", true);
        setShellPromt(">");
        sendWait("", ">", false, timeout);
        setShellPromt("<");
        if (getCurrentOutStr().contains("Logon failure")) {
            throw new Exception("LOGIN_FAIL");
        }
        sendWait("mml", null, false, timeout);
        return getCurrentOutStr();
    }

    private String connectErissonPort5000(String username, String password) throws Exception {
        setShellPromt("DOMAIN:");
        connect("USERCODE:\\s*$", username, "PASSWORD:\\s*$", password, true);
        sendWait("\r", "<", false, timeout);
        setShellPromt("<");
        if (getCurrentOutStr().contains("NOT ACCEPTED")) {
            throw new Exception("LOGIN_FAIL");
        }
        return getCurrentOutStr();
    }

    private String connectErissonLinux(String username, String password) throws Exception {
        setShellPromt("(>|#)");
//        String strLogin = connect("(.*)login(.*):\\s*$", username, "(.*)ssword:\\s*$", password, true);
        setShellPromt("(<|#)");
        
//        if (strLogin != null && strLogin.contains("\n")) {
//            String loginPrompt = strLogin.substring(strLogin.lastIndexOf("\n"));
//            this.setShellPromt(loginPrompt.trim());
//        }
        
        if (getCurrentOutStr().contains("Login incorrect")) {
            throw new Exception("LOGIN_FAIL");
        }
        
        if (nodeType == null || !"SGSN".equals(nodeType.toUpperCase().trim())) {
            sendWait("mml", null, false, timeout);
        }
        return getCurrentOutStr();
    }

    private String connectErissonNoChangeMode(String username, String password) throws Exception {
        setShellPromt("Windows NT Domain:");
        connect("(.*)login(.*):\\s*$", username, "(.*)ssword:\\s*$", password, true);
        setShellPromt(">");
        sendWait("\r\n", ">", false, timeout);
        if (getCurrentOutStr().contains("Logon failure")) {
            throw new Exception("LOGIN_FAIL");
        }
        return getCurrentOutStr();
    }

    private String connectErissonLinuxNoChangeMode(String username, String password) throws Exception {
        setShellPromt(">");
        connect("(.*)login(.*):\\s*$", username + "\r\n", "(.*)ssword:\\s*$", password + "\r\n", true);
        if (getCurrentOutStr().contains("Login incorrect")) {
            throw new Exception("LOGIN_FAIL");
        }
        return getCurrentOutStr();
    }
    
    @Override
    public String sendWait(String command, String promt, boolean isRegex, Integer timeOut) throws Exception {
        //if (port == 5000 || (getOs() != null && getOs().toLowerCase().contains("linux"))) {
            command = (command + "\r\n");
        //}
        return super.sendWait(command, promt, isRegex, timeOut);
    }
    
    @Override
    public String sendWaitHasConfirm(String command, String promt, String confirmPromt, String commandForConfirm, String morePromt, String commandForMore, Integer timeOut) throws Exception {
        return super.sendWaitHasConfirm(command + "\r\n", promt, confirmPromt,
                commandForConfirm == null ? null : commandForConfirm + "\r\n", morePromt,
                commandForMore == null ? null : commandForMore + "\r\n", timeOut); //To change body of generated methods, choose Tools | Templates.
    }

    @Override
    public String sendWaitHasMore(String command, String promt, String morePromt, String commandForMore, Integer timeOut) throws Exception {
        return super.sendWaitHasMore(command + "\r\n", promt, morePromt, commandForMore == null ? null : commandForMore + "\r\n", timeOut); //To change body of generated methods, choose Tools | Templates.
    }
}
