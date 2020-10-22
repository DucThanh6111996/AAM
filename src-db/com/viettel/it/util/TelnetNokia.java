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
public class TelnetNokia extends TelnetClientUtil {

    public TelnetNokia(String hostName, Integer port, String vendor) {
        super(hostName, port, vendor);
        cmdExit = "logout";
    }

    @Override
    public String connect(String username, String password) throws Exception {
        setShellPromt("\n<");
        String login = connect("<", username, "<", password, false);
        if (getCurrentOutStr().contains("USER AUTHORIZATION FAILURE")) {
            throw new Exception("LOGIN_FAIL");
        }
        setShellPromt("\n<");
        return login;
    }

    @Override
    public void write(String value) throws Exception {
        try {
            out.write((value + "\r\n").getBytes());
            out.flush();
        } catch (Exception e) {
            throw e;
        }
    }

    @Override
    public void writeMore(String value) throws Exception {
        try {
            out.write(value.getBytes());
            out.flush();
        } catch (Exception e) {
            throw e;
        }
    }
}
