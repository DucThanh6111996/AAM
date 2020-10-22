/*
 * Copyright 2013 Viettel Telecom. All rights reserved.
 * VIETTEL PROPRIETARY/CONFIDENTIAL. Use is subject to license terms.
 */
package com.viettel.it.util;

import org.apache.commons.net.telnet.*;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.apache.logging.log4j.LogManager.getLogger;

/**
 *
 * @author vannh4
 */
public class TelnetClientUtil {
    private final TelnetClient telnetClient = new TelnetClient();
    protected PrintStream out;
    protected BufferedReader buf;
    protected String prompt = ">";
    private String terminalType = "VT100";
    protected String hostName;
    protected String cmdExit;
    protected Integer port;
    protected String vendor;
    protected Integer timeout = 30000;
    protected boolean isDebug = false;
    protected String currentOutStr;
    protected final Logger log = getLogger(TelnetClientUtil.class);
    private String os = null;
	private static int defaultCharBufferSize = 1024*2;
    private String sbuff;
	private boolean isStopPullBuff = false;

    /**
     * @return
     */
    public String getHostName() {
        return hostName;
    }

    public void setHostName(String hostName) {
        this.hostName = hostName;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getTerminalType() {
        return terminalType;
    }

    public TelnetClientUtil(String hostName, Integer port, String vendor) {
        this.hostName = hostName;
        this.port = port == null ? 23 : port;
        this.vendor = vendor;
    }

    public void setTerminalType(String terminalType) {
        this.terminalType = null == terminalType ? "\r\n" : terminalType;
    }

    public void setShellPromt(String promt) {
        this.prompt = null == promt ? ">" : promt;
    }

    public void setTimeoutDefault(Integer timeout) {
        //nhan 1.05 de tranh tinh trang inputstream bi close truoc khi timeout
        this.timeout = null == timeout ? 30000 : (int) (timeout * 1.05);
    }

    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

    public TelnetClient getTelnetClient() {
        return telnetClient;
    }

    public String getPrompt() {
        return prompt;
    }

    public String getCurrentOutStr() {
        return currentOutStr;
    }

    public String getCmdExit() {
        return cmdExit;
    }

    public void setCmdExit(String cmdExit) {
        this.cmdExit = cmdExit;
    }

    public void connect(boolean waitInput) throws InvalidTelnetOptionException, IOException, Exception {
        TerminalTypeOptionHandler terminalHandler = new TerminalTypeOptionHandler(this.terminalType, false, false, true, false);
        telnetClient.setDefaultTimeout(timeout);
        telnetClient.addOptionHandler(terminalHandler);

        //set buffer
        telnetClient.connect(hostName, port);
        telnetClient.setReceiveBufferSize(256);
        telnetClient.setSendBufferSize(256);
        buf = new BufferedReader(new InputStreamReader(telnetClient.getInputStream()), 256);
        out = new PrintStream(telnetClient.getOutputStream());

        //doc dau ra
        if (waitInput) {
            try {
                readUntil(timeout);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
                System.out.println(e);
            }
        }
    }

    public String connect(String promt, boolean isRegex) throws InvalidTelnetOptionException, IOException, Exception {
        TerminalTypeOptionHandler terminalHandler = new TerminalTypeOptionHandler(this.terminalType, false, false, true, false);
        telnetClient.setDefaultTimeout(timeout);
        telnetClient.addOptionHandler(terminalHandler);
        //set buffer
        telnetClient.connect(hostName, port);
        telnetClient.setReceiveBufferSize(256);
        telnetClient.setSendBufferSize(256);
        buf = new BufferedReader(new InputStreamReader(telnetClient.getInputStream()), 256);
        out = new PrintStream(telnetClient.getOutputStream());

        //doc dau ra
        if (isRegex) {
            return readUntilBelongRegex(promt, timeout);
        } else {
            return readUntil(promt, timeout);
        }
    }

    public String connect(String userPromt, String username, String passwordPromt, String password, boolean isRegex)
            throws InvalidTelnetOptionException, IOException, Exception {
        TerminalTypeOptionHandler terminalHandler = new TerminalTypeOptionHandler(this.terminalType, false, false, true, false);
        EchoOptionHandler echoopt = new EchoOptionHandler(true, false, true, false);
        SuppressGAOptionHandler gaopt = new SuppressGAOptionHandler(true, true, true, true);
        WindowSizeOptionHandler wsopt = new WindowSizeOptionHandler(4096, 24, true, true, true, true);
        telnetClient.setDefaultTimeout(timeout);
        telnetClient.setConnectTimeout(timeout);
        telnetClient.addOptionHandler(terminalHandler);
        telnetClient.addOptionHandler(echoopt);
        telnetClient.addOptionHandler(gaopt);
        telnetClient.addOptionHandler(wsopt);
        //set buffer
        try {
            telnetClient.connect(hostName, port);
            telnetClient.setSoTimeout(timeout);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            throw new Exception("CONN_TIMEOUT");
        }

        telnetClient.setReceiveBufferSize(256);
        telnetClient.setSendBufferSize(256);
        buf = new BufferedReader(new InputStreamReader(telnetClient.getInputStream()), 256);
        out = new PrintStream(telnetClient.getOutputStream());

        //login
        if (isRegex) {
            //log.info("Connecting...waiting for userPrompt : " + userPromt);
            readUntilBelongRegex(userPromt, timeout);
            write(username);

            //log.info("Connecting...waiting for passPrompt : " + passwordPromt);
            readUntilBelongRegex(passwordPromt, timeout);
            write(password);

            Thread.sleep(3000);
            return readUntilBelongRegex(prompt, "[Y/N]:", "N", timeout);
        } else {
            readUntil(userPromt, timeout);
            write(username);
            readUntil(passwordPromt, timeout);
            write(password);
            return readUntil(prompt, timeout);
        }
    }

    public String connect(String username, String password) throws Exception {
        String strLogin = connect("(?i)(login:|username:)", username, "(?i)(password:)", password, true);

        if (strLogin != null && strLogin.contains("\n")) {
            String loginPrompt = strLogin.substring(strLogin.lastIndexOf("\n"));
            this.setShellPromt(loginPrompt.trim());
        }

        return strLogin;
    }

    private String readUntil(String pattern, int timeOut) throws Exception {
    	isStopPullBuff=false;
        StringBuilder sb = new StringBuilder();
        try {
            if (checkInput()) {
                long startTime = System.currentTimeMillis();
                char[] chs = new char[defaultCharBufferSize];
                int t = buf.read(chs);

                while (t != -1) {
                    if (isDebug) {
                        System.out.print(chs);
                    }
                    sb.append(chs,0,t);
                    this.sbuff = sb.toString();
                    if (sb.toString().endsWith(pattern)) {
                        return sb.toString();
                    }
                    t = buf.read(chs);

                    if (System.currentTimeMillis() - startTime > timeOut) {
                        sendNoWait("\u0003");
                        throw new Exception("Time out for waitting promt: " + pattern);
                    }
                }
//                System.out.println("t = " + t);
            }
        } catch (Exception e) {
            throw e;
        } finally {
        	isStopPullBuff=true;
            this.currentOutStr = sb.toString();
        }

        return sb.toString().replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
    }

    private String readUntilBelongRegex(String pattern, int timeOut) throws Exception {
        StringBuilder sb = new StringBuilder();
        try {
            if (checkInput()) {
                long startTime = System.currentTimeMillis();
                char[] chs = new char[1];
                int t = buf.read(chs);

                while (t != -1) {
                    if (isDebug) {
                        System.out.print(chs[0]);
                    }
                    sb.append(chs);
                    if (checkRegex(sb.toString(), pattern)) {
                        return sb.toString();
                    }
                    t = buf.read(chs);

                    if (System.currentTimeMillis() - startTime > timeOut) {
                        sendNoWait("\u0003");
                        throw new Exception("Time out for waitting promt: " + pattern);
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            this.currentOutStr = sb.toString();
        }

        return sb.toString().replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
    }

    private String readUntilBelongRegex(String pattern, String acceptPattern, String acceptCmd, int timeOut) throws Exception {
        StringBuilder sb = new StringBuilder();
        try {
            if (checkInput()) {
                long startTime = System.currentTimeMillis();
                char[] chs = new char[1];
                int t = buf.read(chs);

                while (t != -1) {
                    if (isDebug) {
                        System.out.print(chs[0]);
                    }
                    sb.append(chs);
                    if (checkRegex(sb.toString(), pattern)) {
                        return sb.toString();
                    }

                    if (sb.toString().endsWith(acceptPattern)) {
                        sendNoWait(acceptCmd);
                    }

                    t = buf.read(chs);

                    if (System.currentTimeMillis() - startTime > timeOut) {
                        sendNoWait("\u0003");
                        throw new Exception("Time out for waitting promt: " + pattern);
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            this.currentOutStr = sb.toString();
        }

        return sb.toString().replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
    }

    protected String readUntil(int timeOut) throws Exception {
        StringBuilder sb = new StringBuilder();
        try {
            if (checkInput()) {
                long startTime = System.currentTimeMillis();
                char[] chs = new char[1];
                int t = buf.read(chs);

                while (t != -1) {
                    if (isDebug) {
                        System.out.print(chs[0]);
                    }
                    sb.append(chs);
                    t = buf.read(chs);

                    if (System.currentTimeMillis() - startTime > timeOut) {
                        sendNoWait("\u0003");
                        throw new Exception("Time out for waitting promt: ");
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            this.currentOutStr = sb.toString();
        }
        return sb.toString().replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
    }

    private String readUntilByRange(String strStart, String strEnd, int timeOut) throws Exception {
        StringBuilder sb = new StringBuilder();
        try {
            boolean firstFind = false;
            int startPoint = -1;
            int endPoint = -1;
            if (checkInput()) {
                long startTime = System.currentTimeMillis();
                char[] chs = new char[1];
                int t = buf.read(chs);

                while (t != -1) {
                    if (isDebug) {
                        System.out.print(chs[0]);
                    }
                    sb.append(chs);
                    if (sb.indexOf(strStart) > 0) {
                        startPoint = sb.indexOf(strStart);
                        firstFind = true;
                    }
                    if (firstFind) {
                        endPoint = sb.indexOf(strEnd, startPoint);
                        if (endPoint > 0) {
                            return sb.toString().substring(startPoint, endPoint);
                        }
                    }
                    t = buf.read(chs);

                    if (System.currentTimeMillis() - startTime > timeOut) {
                        sendNoWait("\u0003");
                        throw new Exception("Time out for waitting promt: start = " + strStart + ", end = " + strEnd);
                    }
                }
            }
        } catch (Exception e) {
            throw e;
        } finally {
            this.currentOutStr = sb.toString();
        }

        return sb.toString().replaceAll("[\\p{Cc}\\p{Cf}\\p{Co}\\p{Cn}]", "");
    }

    public void write(String value) throws Exception {
        try {
            out.println(value);
            out.flush();
            if (isDebug) {
                System.out.println(value);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public void writeMore(String value) throws Exception {
        try {
            out.print(value); //TriLM4  Luc dau : out.println(value);
            out.flush();
            if (isDebug) {
                System.out.println(value);
            }
        } catch (Exception e) {
            throw e;
        }
    }

    public String sendWait(String command, String promtStart, String promtEnd, Integer timeOut) throws Exception {
        try {
            write(command);
            int myTimeOut = timeOut == null ? this.timeout : timeOut;
            return readUntilByRange(promtStart, promtEnd, myTimeOut);
        } catch (Exception e) {
            throw e;
        }
    }

    public void sendNoWait(String command) throws Exception {
        try {
            write(command);
        } catch (Exception e) {
            throw e;
        }
    }

    public String sendWait(String command) throws Exception {
        try {
            write(command);
            return readUntil(prompt, timeout);
        } catch (Exception e) {
            throw e;
        }
    }

    public String sendWait(String command, String promt, Integer timeOut) throws Exception {
        try {
            write(command);
            int myTimeOut = timeOut == null ? this.timeout : timeOut;
            String myPromt = promt == null ? this.prompt : promt;
            return readUntil(myPromt, myTimeOut);
        } catch (Exception e) {
            throw e;
        }
    }

    public String sendWaitConfirm(String command, String promt, Integer timeOut) throws Exception {
        try {
            writeMore(command);
            int myTimeOut = timeOut == null ? this.timeout : timeOut;
            String myPromt = promt == null ? this.prompt : promt;
            return readUntil(myPromt, myTimeOut);
        } catch (Exception e) {
            throw e;
        }
    }

    public String sendWait(String command, String promt, boolean isRegex, Integer timeOut) throws Exception {
        try {
            write(command);
            int myTimeOut = timeOut == null ? this.timeout : timeOut;
            String myPromt = promt == null ? this.prompt : promt;
            //chuan hoa prompt

            return readUntilBelongRegex(myPromt, myTimeOut);
        } catch (Exception e) {
            throw e;
        }
    }

    public String sendWaitNoMore(String command, String promt, boolean isRegex, Integer timeOut) throws Exception {
        try {
            writeMore(command);
            int myTimeOut = timeOut == null ? this.timeout : timeOut;
            String myPromt = promt == null ? this.prompt : promt;

            return readUntilBelongRegex(myPromt, myTimeOut);
        } catch (Exception e) {
            throw e;
        }
    }

    //xu ly lenh co more
    public String sendWaitHasMore(String command, String promt, String morePromt, String commandForMore, Integer timeOut) throws Exception {
        String result = "";
        try {
            int myTimeOut = timeOut == null ? this.timeout : timeOut;
            String myPromt = promt == null ? (morePromt == null ? this.prompt : "(" + this.prompt + ")|(" + morePromt + ")")
                    : (morePromt == null ? promt : "(" + promt + ")|(" + morePromt + ")");
            boolean isMore = false;

            do {
                if (isMore) {
                    writeMore(commandForMore);
                } else {
                    write(command);
                }
                String response = readUntilBelongRegex(myPromt, myTimeOut);
                result += response;
                if (containString(morePromt, response)) {
                    if (response.lastIndexOf("\n") > 0) {
                        result = result.substring(0, result.lastIndexOf("\n"));
                    }
                    isMore = true;
                } else {
                    isMore = false;
                }
            } while (isMore);
        } catch (Exception e) {
            throw e;
        }
        return result;
    }
    
    public String sendWaitHasConfirm(String command, String promt, String confirmPromt, 
            String commandForConfirm, String morePromt, String commandForMore, Integer timeOut) throws Exception {
        String result = "";
        try {
            int myTimeOut = timeOut == null ? this.timeout : timeOut;
            String myPromt = promt == null ? (morePromt == null ? this.prompt : "(" + this.prompt + ")|(" + morePromt + ")")
                    : (morePromt == null ? promt : "(" + promt + ")|(" + morePromt + ")");
            String myConfirmPromt = promt == null ? (confirmPromt == null ? this.prompt : "(" + this.prompt + ")|(" + confirmPromt + ")")
                    : (confirmPromt == null ? promt : "(" + promt + ")|(" + confirmPromt + ")");
            boolean isMore = false;
            
            //Gui lenh va confirm
            write(command);
            String response = readUntilBelongRegex(myConfirmPromt, myTimeOut);
            result += response;
            if (containString(confirmPromt, response)) {
                writeMore(commandForConfirm);
                response = readUntilBelongRegex(myConfirmPromt, myTimeOut);
                result += response;
                
                if (morePromt != null && !morePromt.trim().isEmpty()) {
                    do {
                        if (isMore) {
                            writeMore(commandForMore);
                        }

                        response = readUntilBelongRegex(myPromt, myTimeOut);
                        result += response;
                        if (containString(morePromt, response)) {
                            if (response.lastIndexOf("\n") > 0) {
                                result = result.substring(0, result.lastIndexOf("\n"));
                            }
                            isMore = true;
                        } else {
                            isMore = false;
                        }
                    } while (isMore);
                }
            }
        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    public String sendWaitHasMoreAndManyShellPrompt(String command, String manyPromtpt, String morePromt, String commandForMore, Integer timeOut) throws Exception {
        String result = "";
        try {
            int myTimeOut = timeOut == null ? this.timeout : timeOut;
            String myPromt = manyPromtpt == null ? (morePromt == null ? this.prompt : this.prompt + "|(" + morePromt + ")")
                    : (morePromt == null ? manyPromtpt : manyPromtpt + "|(" + morePromt + ")");
            boolean isMore = false;

            do {
                if (isMore) {
                    writeMore(commandForMore);
                } else {
                    write(command);
                }
                String response = readUntilBelongRegex(myPromt, myTimeOut);
                result += response;
                if (containString(morePromt, response)) {
                    isMore = true;
                } else {
                    isMore = false;
                }
            } while (isMore);
        } catch (Exception e) {
            throw e;
        }
        return result;
    }

    public void disConnect() throws Exception {
        if (cmdExit != null) {
            sendNoWait(cmdExit);
        }

        try {
            if (out != null) {
                out.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            if (buf != null) {
                buf.close();
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }

        try {
            telnetClient.disconnect();
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean isConnected() {
        return telnetClient.isConnected();
    }

    //util
    public boolean checkRegex(String content, String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);
        Matcher m;
        m = p.matcher(content);
        if (m.find()) {
            return true;
        }

        return false;
    }

    protected boolean checkInput() throws IOException, Exception {
        boolean test = false;
        Long startTime = System.currentTimeMillis();
        do {
            if (buf.ready()) {
                test = true;
            }
            if (System.currentTimeMillis() - startTime > timeout) {
                throw new Exception("Time out for waiting input available");
            }
        } while (!test);

        return test;
    }

    private boolean containString(String strRegex, String sample) {
        boolean test = false;
        try {
            Pattern ptt = Pattern.compile(strRegex, Pattern.CASE_INSENSITIVE);
            Matcher ma = ptt.matcher(sample);
            if (ma.find()) {
                test = true;
            }
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            test = false;
        }
        return test;
    }

    public static void disConnect(TelnetClientUtil clientUtil) throws Exception {
        switch (clientUtil.getVendor().toLowerCase()) {
            case "tekelec":
                ((TelnetStpTekelec) clientUtil).disConnect();
                break;
            case "nokia":
                ((TelnetNokia) clientUtil).disConnect();
                break;
            case "huawei":
                ((TelnetHuawei) clientUtil).disConnect();
                break;
            case "ericsson":
                ((TelnetEricsson) clientUtil).disConnect();
                break;
            default:
                clientUtil.disConnect();
                break;
        }
    }

    public String getVendor() {
        return vendor;
    }

    public void setVendor(String vendor) {
        this.vendor = vendor;
    }

    public String getOs() {
        return os;
    }

    public void setOs(String os) {
        this.os = os;
    }
		public boolean isStopPullBuff() {
		return isStopPullBuff;
	}

	public void setStopPullBuff(boolean isStopPullBuff) {
		this.isStopPullBuff = isStopPullBuff;
	}

	public String getSbuff() {
		return sbuff;
	}

	public void setSbuff(String sbuff) {
		this.sbuff = sbuff;
	}
}
