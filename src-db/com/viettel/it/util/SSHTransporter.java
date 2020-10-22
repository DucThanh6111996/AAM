/**
 *  SSHTransporter  version 1.0     
 *
 * Copyright YYYY Viettel Telecom. All rights reserved.
 * VIETTEL PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.viettel.it.util;

import com.jcraft.jsch.*;
import com.jscape.inet.ssh.SshConnectedEvent;
import com.jscape.inet.ssh.SshDataReceivedEvent;
import com.jscape.inet.ssh.SshDisconnectedEvent;
import com.jscape.inet.ssh.SshListener;
import org.apache.log4j.*;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.net.ConnectException;
import java.net.Socket;

/**
 * User: qlmvt_Trungnt42
 * Date: 5/9/11
 * Time: 2:11 PM
 * To change this template use File | Settings | File Templates.
 */
public class SSHTransporter implements SshListener {

    private static final Logger logger = org.apache.log4j.Logger.getLogger(SSHTransporter.class);

    private JSch jsch = null;
    private Session session = null;

    public SSHTransporter(String title) {
        jsch = new JSch();
    }

    public int createBridge(String ip, int port, String user, String pass) throws Exception {
        int assignedPort = findFreePort("localhost");
        try {
            session = jsch.getSession("", ip, 22);
            UserInfo ui = new MyUserInfo("");
            session.setUserInfo(ui);
            session.connect();
            assignedPort = session.setPortForwardingL(assignedPort, "localhost", port);
        } catch (JSchException ex) {
            throw new Exception(ex);
        }
        return assignedPort;
    }

    private int findFreePort(String hostName) throws Exception {
        while (true) {
            int port = (int) (65536 * Math.random());
            while (port < 10000) {
                port = (int) (65536 * Math.random());
            }
            try {
                Socket s = new Socket(hostName, port);
                s.close();
            } catch (ConnectException e) {
                logger.error(e.getMessage(), e);
                return port;
            } catch (IOException e) {
                if (e.getMessage().contains("refused")) {
                    return port;
                }
                throw e;
            } catch (Exception e) {
                throw e;
            }
        }
    }

    public void exit() {
        try {
            if (session != null) {
                session.disconnect();
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    @Override
    public void connected(SshConnectedEvent sshConnectedEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void disconnected(SshDisconnectedEvent sshDisconnectedEvent) {
        //To change body of implemented methods use File | Settings | File Templates.
    }

    @Override
    public void dataReceived(SshDataReceivedEvent sshDataReceivedEvent) {
    }

    public static class MyUserInfo implements UserInfo {

        String passwd;

        @Override
        public String getPassword() {
            return passwd;
        }

        public MyUserInfo(String password) {
            this.passwd = password;
        }

        @Override
        public boolean promptYesNo(String str) {
            return true;
        }

        @Override
        public String getPassphrase() {
            return null;
        }

        @Override
        public boolean promptPassphrase(String message) {
            return true;
        }

        @Override
        public boolean promptPassword(String message) {
            return true;
        }

        @Override
        public void showMessage(String message) {
            System.out.println(message);
        }

        public String[] promptKeyboardInteractive(String destination,
                String name,
                String instruction,
                String[] prompt,
                boolean[] echo) {
            return new String[3];
        }
    }
}
