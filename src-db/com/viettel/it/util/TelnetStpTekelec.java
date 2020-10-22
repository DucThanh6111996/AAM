/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.util;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author hienhv4
 */
public class TelnetStpTekelec extends TelnetClientUtil {

    private SSHTransporter sshTransporter;

    public TelnetStpTekelec(String hostName, Integer port, String vendor) {
        super(hostName, port, vendor);
        cmdExit = "logout";
    }

    @Override
    public String connect(String username, String password) throws Exception {
        try {
            //Create SSH bridge
            sshTransporter = new SSHTransporter(hostName + ":" + port);
            int portForward = sshTransporter.createBridge(hostName, port, username, password);
            if (portForward != -1) {
                log.info("portForward: " + portForward);
                
                hostName = "localhost";
                port = portForward;
                
                String termTypeStr = connect(">", false);
                
                List<String> termTypes = selectType(termTypeStr);
                
                String terminal = termTypes.get(termTypes.size() - 1);
                sendWait(terminal, "Connection established as terminal " + terminal + ".", timeout);
                
                Thread.sleep(1000);
                
                sendWait("login:uid=" + username, "Enter Password :", timeout);
                
                Thread.sleep(1000);
                String loginStr = sendWait(password, "Command Executed", timeout);

                if (!loginStr.contains("Last successful LOGIN was")) {
                    throw new Exception("LOGIN_FAIL");
                }
                
                write("");
                
                setShellPromt("(Command Executed|Cmd Rej:)");
                
                return loginStr;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            throw e;
        }

        return "";
    }

    @Override
    public void write(String value) throws Exception {
        try {
            out.write((value + "\r").getBytes());
            out.flush();
//            if (isDebug) {
//                System.out.println(value);
//            }
        } catch (Exception e) {
            throw e;
        }
    }
    
    @Override
    public void writeMore(String value) throws Exception {
        try {
            out.write(value.getBytes());
            out.flush();
//            if (isDebug) {
//                System.out.println(value);
//            }
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void disConnect() throws Exception {
        super.disConnect();
        
        try {
            if (sshTransporter != null) {
                sshTransporter.exit();
                sshTransporter = null;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
    
    private ArrayList<String> selectType(String tmp) {

        log.debug("selectType STR " + tmp);
        Pattern pattern = Pattern.compile("\\((\\d+\\,)*\\d+\\)");

        Matcher match = pattern.matcher(tmp);

        ArrayList<String> types = new ArrayList<>();
        if (match.find()) {
            String[] parts = match.group().replaceAll("(22|23|24|30|31|32),?", "").replace(",)", ")").split(",");

            for (int i = 0; i < parts.length; i++) {
                if (i == 0) {
                    types.add(parts[0].substring(1));
                } else {
                    if (i != parts.length - 1) {
                        types.add(parts[i]);
                    } else {
                        types.add(parts[parts.length - 1].substring(0, parts[parts.length - 1].length() - 1));
                    }
                }
            }
        }
        return types;
    }
}
