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
public class TelnetHuawei extends TelnetClientUtil {
    
    private final String version;

    public TelnetHuawei(String hostName, Integer port, String vendor, String version) {
        super(hostName, port, vendor);
        cmdExit = "logout";
        this.version = version;
    }

    @Override
    public String connect(String username, String password) throws Exception {
        if (this.version != null && "V200R005".equals(this.version.toUpperCase().trim())) {
            setShellPromt("(>|#)");
            return super.connect(username, password);
        }
        connect(false);
        String slogin = "LGI:op=\":username\",pwd=\":password\";";
        slogin = slogin.replace(":username", username);
        slogin = slogin.replace(":password", password);
        setShellPromt("  END");
        String sLog = sendWait(slogin);
        if (!sLog.contains("RETCODE = 0")) {
            throw new Exception("LOGIN_FAIL");
        }
        return sLog;
    }

    @Override
    public String sendWait(String command, String promt, boolean isRegex, Integer timeOut) throws Exception {
        return super.sendWait(command, command, getPrompt(), timeOut);
    }
}
