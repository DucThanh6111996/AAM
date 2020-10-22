/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.util;

import com.captcha.botdetect.internal.infrastructure.e.e;
import com.viettel.passprotector.PassProtector;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 *
 * @author hienhv4
 */
public class ParamUtil {

    private static final Logger logger = LogManager.getLogger(ParamUtil.class);
    private static final int TIME_OUT = 15000;
    public static final int TYPE_NORMAL = 1;
    public static final int TYPE_CASCADE = 2;
    public static final int SUB_RING = 3;
    
    public static enum PARAMCODE_ENODEB{
    	INTERFACE("Interface ID"),
    	DESCRIPTION_FOR_THIS_INTERFACE("Description for this interface"),
    	IP_SERVICE_ENODEB("IPv4 address of Service"),
    	IP_OAM_ENODEB("IPv4 address of OAM"),
    	IP_SERVICE("Network of Service"),
    	IP_OAM("Network of OAM"),
    	VLAN_SERVICE("VLAN ID Service"),
    	VLAN_OAM("VLAN ID OAM"),
    	IP_CONNECT_ENODEB_AGG_MASTER_MAIN("IP d?u n?i eNodeB tr�n Agg Master hu?ng Main"),
    	IP_CONNECT_ENODEB_AGG_MASTER_PROTECT("IP d?u n?i eNodeB tr�n Agg Master hu?ng Protect"),
    	IP_VIRTUAL_CONNECT_ENODEB_MAIN("IP Virtual d?u n?i eNodeB hu?ng Main"),
    	IP_VIRTUAL_CONNECT_ENODEB_PROTECT("IP Virtual d?u n?i eNodeB hu?ng Protect"),
    	IP_CONNECT_ENODEB_AGG_BACKUP_MAIN("IP d?u n?i eNodeB tr�n Agg Backup hu?ng Main"),
    	IP_CONNECT_ENODEB_AGG_BACKUP_PROTECT("IP d?u n?i eNodeB tr�n Agg Backup hu?ng Protect"),
    	IP_CONNECT_SRT_MAIN("IP d?u n?i tr�n SRT hu?ng Main"),
    	IP_CONNECT_SRT_PROTECT("IP d?u n?i tr�n SRT hu?ng Protect"),
    	RX("Rx Power"),
    	TX("Tx Power"),
    	;
    	private String value;
    	PARAMCODE_ENODEB(String v){
    		setValue(v);
    	}
    	
		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}
    }

    private static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    private static String getValue(String content, String start) {
        int index = content.indexOf(start);
        if (index > -1) {
            if (index == content.length() - 1) {
                return null;
            }
            return content.substring(index + start.length(), content.length()).trim();
        }
        return null;
    }

    private static String getValue(String content, String start, String end) {
        int index = content.indexOf(start);
        if (index > -1) {
            if (index == content.length() - 1) {
                return null;
            }

            String value = content.substring(index + start.length(), content.length()).trim();
            int indexEnd = value.indexOf(end);

            if (indexEnd < 0) {
                return value;
            } else {
                return value.substring(0, indexEnd).trim();
            }
        }
        return null;
    }

    private static boolean checkRegex(String content, String pattern) {
        Pattern p = Pattern.compile(pattern, Pattern.MULTILINE);
        Matcher m;
        m = p.matcher(content);
        if (m.find()) {
            return true;
        }

        return false;
    }

    //20181119_tudn_start them danh sach lenh blacklist
    public static List<String> getValuesRegex(String content, String regex) {
        List<String> values = new ArrayList<>();
        try {
            if (isNullOrEmpty(content)) {
                return values;
            }
            if (isNullOrEmpty(regex)) {
                values.add(content);
                return values;
            }
            Pattern patRegex = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            Matcher matcher = patRegex.matcher(content.trim());
            while (matcher.find()) {
                String value = matcher.group(1);
                if (!values.contains(value)) {
                    values.add(value);
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return values;
    }
    //20181119_tudn_end them danh sach lenh blacklist

    private static String getVlanCtrlHuaweiS5328(String content) {
        BufferedReader bf = null;
        StringReader sr = null;
        try {
            sr = new StringReader(content);
            bf = new BufferedReader(sr);

            String line;
            while ((line = bf.readLine()) != null) {
                if (line.contains("Control VLAN")) {
                    return getValue(line, "major", " ");
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (sr != null) {
                sr.close();
            }

            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
        return "";
    }

    private static String getVlanCtrlZte5928E(String content, String protectInst) {
        BufferedReader bf = null;
        StringReader sr = null;
        try {
            sr = new StringReader(content);
            bf = new BufferedReader(sr);

            String line;
            while ((line = bf.readLine()) != null) {
                if (line.contains("ctrl-vlan:")) {
                    String protectInstLocal = getValue(line, "protect-instance:", " ");
                    if (protectInst.equals(protectInstLocal)) {
                        return getValue(line, "ctrl-vlan:", " ");
                    }
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (sr != null) {
                sr.close();
            }

            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
        return "";
    }

    private static String getValueTelnet(TelnetClientUtil telnetClient, String command,
                                         int timeout, String cmdEnd, String pagingCmd, String pagingRegex) throws Exception {

        try {
            String content;
            if (pagingRegex == null || pagingRegex.trim().isEmpty()) {
                content = telnetClient.sendWait(command, cmdEnd, true, timeout);
            } else {
                if (pagingCmd == null) {
                    pagingCmd = " ";
                }
                content = telnetClient.sendWaitHasMore(command, cmdEnd, pagingRegex, pagingCmd, timeout);
            }

            if (content == null) {
                return null;
            }

            String firstLine = content.contains("\n") ? content.substring(0, content.indexOf("\n")) : null;
            if (firstLine != null) {
                content = content.substring(content.indexOf("\n"));
            }
            System.out.println("firstLine: " + firstLine);

            if (content.lastIndexOf("\n") > 0) {
                content = content.substring(0, content.lastIndexOf("\n"));
            } else if (checkRegex(content, isNullOrEmpty(cmdEnd) ? telnetClient.getPrompt() : cmdEnd)) {
                content = "";
            }

            return content.replaceAll("\\b", "")
                    .replaceAll("\u001B7+", "").replaceAll("[\\x00\\x08\\x0B\\x0C\\x0E-\\x1F]", "")
                    .replace("[42D", "")
                    .trim();
        } catch (Exception ex) {
            throw ex;
        }
    }

    private static TelnetClientUtil login(String username, String pass, String ip, int port, String prompt,
                                          String terminator, String cmdExit, Long totalTimeout) throws Exception {
        TelnetClientUtil telnetClient = new TelnetClientUtil(ip, port, null);
        telnetClient.setShellPromt(prompt);
        telnetClient.setCmdExit(cmdExit);
        telnetClient.setTerminalType(terminator);
        telnetClient.setTimeoutDefault(totalTimeout.intValue());

        try {
            telnetClient.connect(username, pass);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);

            if (!"CONN_TIMEOUT".equals(ex.getMessage())) {
                throw new Exception("LOGIN_FAIL");
            } else {
                throw new Exception("CONN_TIMEOUT");
            }
        }

        return telnetClient;
    }

    private static boolean checkVrrpId(String content) {
        if (content == null) {
            return false;
        }

        if (content.contains("The VRRP does not exist")) {
            return true;
        }

        BufferedReader bf = null;
        StringReader sr = null;
        try {
            sr = new StringReader(content);
            bf = new BufferedReader(sr);

            String line;
            boolean isStart = false;
            int count = 0;
            while ((line = bf.readLine()) != null) {
                if (line.contains("------------")) {
                    isStart = true;
                    continue;
                }

                if (isStart) {
                    count++;
                }
            }
            return count < 16;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        } finally {
            if (sr != null) {
                sr.close();
            }

            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
        return false;
    }

    private static boolean checkVlanId(String content) {
        if (content == null || content.contains("Wrong parameter found at")) {
            return true;
        }

        return false;
    }

    private static boolean checkVsiId(String content) {
        if (isNullOrEmpty(content)) {
            return true;
        }

        return false;
    }

    private static boolean checkVlanIdL3CiscoAsr901(String content) {
        if (isNullOrEmpty(content)) {
            return true;
        }

        return false;
    }

    private static boolean checkVlanIdL3HuaweiATN910B(String content) {
        if (isNullOrEmpty(content)) {
            return true;
        }

        if (content.trim().contains("\n")) {
            return true;
        }

        return false;
    }

    private static boolean checkVsiName(String content) {
        if (isNullOrEmpty(content)) {
            return true;
        }

        return false;
    }

    private static boolean checkVeId(String content) {
        if (content == null || content.contains("Wrong parameter found at")) {
            return true;
        }

        return false;
    }

    private static boolean checkRdRt(String content) {
        if (isNullOrEmpty(content)) {
            return true;
        }

        return false;
    }

    private static String getIpLoopback(String ipAGG) {
        try {
            if (isNullOrEmpty(ipAGG)) {
                return "";
            }

            String[] arr = ipAGG.trim().split("\\.");
            if (arr.length == 4) {
                int octet3 = Integer.parseInt(arr[2]);

                if (octet3 >= 0) {
                    return arr[0] + "." + arr[1] + "." + (octet3 - 32) + "." + arr[3];
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static String getVlanControl(TelnetClientUtil telnetClient, String vendor, String version, String domain) {
        try {
            if ("HUAWEI".equals(vendor.toUpperCase()) && "S5328".equals(version.toUpperCase())) {
                String content = getValueTelnet(telnetClient, "display rrpp verbose domain " + domain, TIME_OUT,
                        null, null, "---- More ----");

                return getVlanCtrlHuaweiS5328(content);
            } else if ("ZTE".equals(vendor.toUpperCase()) && "5928E".equals(version.toUpperCase())) {
                String content = getValueTelnet(telnetClient, "show zesr brief", TIME_OUT,
                        null, null, "--More--");

                return getVlanCtrlZte5928E(content, domain);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static String getVrrpId(TelnetClientUtil telnetClientMT, TelnetClientUtil telnetClientBK,
                                    int start, Integer end) {
        try {
            do {
                String cmdRun = "display vrrp " + start + " brief";

                //Check AGG Master
                String content = getValueTelnet(telnetClientMT, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                if (checkVrrpId(content)) {
                    //Check AGG Backup

                    content = getValueTelnet(telnetClientBK, cmdRun, TIME_OUT,
                            null, null, "---- More ----");
                    if (checkVrrpId(content)) {
                        return start + "";
                    }
                }

                start++;
                if (end != null && start > end) {
                    break;
                }
            } while (true);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static String getVlanIdL2(TelnetClientUtil telnetClientMT, TelnetClientUtil telnetClientBK, String interfaceMT,
                                      String interfaceBK) {
        try {
            int start = 2451;

            do {
                String cmdRun = "display current-configuration interface " + interfaceMT + "." + start;
                //Check AGG Master
                String content = getValueTelnet(telnetClientMT, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                if (checkVlanId(content)) {
                    //Check AGG Backup
                    cmdRun = "display current-configuration interface " + interfaceBK + "." + start;

                    content = getValueTelnet(telnetClientBK, cmdRun, TIME_OUT,
                            null, null, "---- More ----");
                    if (checkVlanId(content)) {
                        return start + "";
                    } else {
                        start++;
                    }
                } else {
                    start++;
                }
                if (start > 2500) {
                    return "";
                }
            } while (true);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static String getVlanIdL3(TelnetClientUtil telnetClient, String interfaceSrtDown,
                                      String vendor, String version) {
        try {
            if ("CISCO".equals(vendor.toUpperCase()) && "ASR901-6CZ-FT".equals(version.toUpperCase())) {
                int start = 3501, end = 4000;

                do {
                    String cmdRun = "show interfaces description | include Vl" + start;

                    //Check SRT
                    String content = getValueTelnet(telnetClient, cmdRun, TIME_OUT,
                            null, null, "--More--");

                    if (checkVlanIdL3CiscoAsr901(content)) {
                        return start + "";
                    }

                    start++;
                } while (start <= end);
            } else if ("HUAWEI".equals(vendor.toUpperCase()) && "ATN910B".equals(version.toUpperCase())) {
                int start = 3501, end = 4000;

                do {
                    String cmdRun = "display interface description | include " + interfaceSrtDown + "." + start;

                    //Check SRT
                    String content = getValueTelnet(telnetClient, cmdRun, TIME_OUT,
                            null, null, "---- More ----");

                    if (checkVlanIdL3HuaweiATN910B(content)) {
                        return start + "";
                    }

                    start++;
                } while (start <= end);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static int getVsiId(TelnetClientUtil telnetClientMT, TelnetClientUtil telnetClientBK,
                                int start, int end, int incNum) {
        try {
            do {
                String cmdRun = "display vsi verbose | include " + start;

                //Check AGG Master
                String content = getValueTelnet(telnetClientMT, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                if (checkVsiId(content)) {
                    //Check AGG Backup

                    content = getValueTelnet(telnetClientBK, cmdRun, TIME_OUT,
                            null, null, "---- More ----");
                    if (checkVsiId(content)) {
                        return start;
                    }
                }

                start += incNum;
            } while (start <= end);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return -1;
    }

    private static int getVeId(TelnetClientUtil telnetClientMT, TelnetClientUtil telnetClientBK,
                               int start, int end, int incNum) {
        try {
            do {
                String cmdRun = "display current-configuration interface Virtual-Ethernet 1/0/0." + start;

                //Check AGG Master
                String content = getValueTelnet(telnetClientMT, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                if (checkVeId(content)) {
                    cmdRun = "display current-configuration interface Virtual-Ethernet 1/0/1." + start;
                    content = getValueTelnet(telnetClientMT, cmdRun, TIME_OUT,
                            null, null, "---- More ----");

                    if (checkVeId(content)) {
                        //Check AGG Backup
                        cmdRun = "display current-configuration interface Virtual-Ethernet 1/0/0." + start;
                        content = getValueTelnet(telnetClientBK, cmdRun, TIME_OUT,
                                null, null, "---- More ----");
                        if (checkVeId(content)) {
                            //Check AGG Backup
                            cmdRun = "display current-configuration interface Virtual-Ethernet 1/0/1." + start;
                            content = getValueTelnet(telnetClientBK, cmdRun, TIME_OUT,
                                    null, null, "---- More ----");

                            if (checkVeId(content)) {
                                return start;
                            }
                        }
                    }
                }

                start += incNum;
            } while (start <= end);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return -1;
    }

    private static String getVsiName(TelnetClientUtil telnetClientMT, TelnetClientUtil telnetClientBK,
                                     String vsiNameStart) {
        try {
            int start = 0;

            do {
                String vsiName = vsiNameStart + (start <= 0 ? "" : "_" + start);
                String cmdRun = "display vsi name " + vsiName;

                //Check AGG Master
                String content = getValueTelnet(telnetClientMT, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                if (checkVsiName(content)) {
                    //Check AGG Backup

                    content = getValueTelnet(telnetClientBK, cmdRun, TIME_OUT,
                            null, null, "---- More ----");
                    if (checkVsiName(content)) {
                        return vsiName;
                    }
                }

                start++;
            } while (true);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static String getRdRt(TelnetClientUtil telnetClientCV, int start) {
        try {
            int end = 65565;
            do {
                String cmdRun = "display bgp vpnv4 route-distinguisher 7552:" + start + " routing-table";
                //Check AGG Master
                String content = getValueTelnet(telnetClientCV, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                if (checkRdRt(content)) {
                    return "7552:" + start;
                }

                start++;
            } while (start <= end);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static String getUsername(String areaCode) throws Exception {
        switch (areaCode.toUpperCase()) {
            case "KV1":
                return PassProtector.decrypt(MessageUtil.getResourceBundleConfig("username_kv1"), "ipchange");
            case "KV2":
                return PassProtector.decrypt(MessageUtil.getResourceBundleConfig("username_kv2"), "ipchange");
            case "KV3":
                return PassProtector.decrypt(MessageUtil.getResourceBundleConfig("username_kv3"), "ipchange");
            default:
                return "";
        }
    }

    private static String getPassword(String areaCode) throws Exception {
        switch (areaCode.toUpperCase()) {
            case "KV1":
                return PassProtector.decrypt(MessageUtil.getResourceBundleConfig("password_kv1"), "ipchange");
            case "KV2":
                return PassProtector.decrypt(MessageUtil.getResourceBundleConfig("password_kv2"), "ipchange");
            case "KV3":
                return PassProtector.decrypt(MessageUtil.getResourceBundleConfig("password_kv3"), "ipchange");
            default:
                return "";
        }
    }

    //---------------------------Cac tham so 4G_Start
    public static List<String> getValueRowColumnIndex(String content, int row, int column,
            int countRow, String rowHeader, String rowHeaderOperator, String rowFooter, String rowFooterOperator,
            String splitColumnChar, String regex) throws Exception {
        BufferedReader bf = null;
        StringReader sr = null;
        try {
            sr = new StringReader(content);
            bf = new BufferedReader(sr);

            String line = "";
            int i = 1;
            int count = 0;
            List<String> lstValue = new ArrayList<>();
            boolean isHeader = false;
            while ((line = bf.readLine()) != null) {
                if (rowHeader != null && !rowHeader.trim().isEmpty()) {
                    switch (rowHeaderOperator) {
                        case "CONTAIN":
                            if (line.trim().contains(rowHeader)) {
                                isHeader = true;
                            }
                            break;
                        case "START WITH":
                            if (line.trim().startsWith(rowHeader)) {
                                isHeader = true;
                            }
                            break;
                        case "END WITH":
                            if (line.trim().endsWith(rowHeader)) {
                                isHeader = true;
                            }
                            break;
                    }
                    if (isHeader) {
                        break;
                    }
                } else {
                    i = 1;
                    break;
                }
            }

            do {
                if (line == null) {
                    break;
                }
                boolean isFooter = false;
                if (!isNullOrEmpty(rowFooter)) {
                    switch (rowFooterOperator) {
                        case "CONTAIN":
                            if (line.trim().contains(rowFooter)) {
                                isFooter = true;
                            }
                            break;
                        case "START WITH":
                            if (line.trim().startsWith(rowFooter)) {
                                isFooter = true;
                            }
                            break;
                        case "END WITH":
                            if (line.trim().endsWith(rowFooter)) {
                                isFooter = true;
                            }
                            break;
                    }
                    if (isFooter) {
                        break;
                    }
                }
                if (row <= i) {
                    if (countRow > 0) {
                        count++;
                        if (count <= countRow) {
                            if (isNullOrEmpty(splitColumnChar)) {
                                String vl = getValueRegex(line.trim(), regex);

                                if (!isNullOrEmpty(vl) && !lstValue.contains(vl)) {
                                    lstValue.add(vl.trim());
                                }
                            } else {
                                String[] arr = line.trim().split(splitColumnChar);
                                if (arr != null && arr.length >= column) {
                                    String vl = getValueRegex(arr[column - 1], regex);

                                    if (!isNullOrEmpty(vl) && !lstValue.contains(vl)) {
                                        lstValue.add(vl.trim());
                                    }
                                }
                            }
                        } else {
                            break;
                        }
                    } else {
                        if (isNullOrEmpty(splitColumnChar)) {
                            String vl = getValueRegex(line, regex);

                            if (!isNullOrEmpty(vl) && !lstValue.contains(vl)) {
                                lstValue.add(vl.trim());
                            }
                        } else {
                            String[] arr = line.trim().split(splitColumnChar);
                            if (arr != null && arr.length >= column) {
                                String vl = getValueRegex(arr[column - 1], regex);

                                if (!isNullOrEmpty(vl) && !lstValue.contains(vl)) {
                                    lstValue.add(vl.trim());
                                }
                            }
                        }
                    }
                }
                i++;
            } while ((line = bf.readLine()) != null);

            return lstValue;
        } catch (Exception ex) {
            throw ex;
        } finally {
            if (sr != null) {
                sr.close();
            }

            if (bf != null) {
                try {
                    bf.close();
                } catch (IOException ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        }
    }

    private static String getValueRegex(String content, String regex) {
        String value = null;
        try {
            if (isNullOrEmpty(regex) || isNullOrEmpty(content)) {
                return content;
            }
            Pattern patRegex = Pattern.compile(regex, Pattern.MULTILINE | Pattern.CASE_INSENSITIVE | Pattern.UNICODE_CASE);
            Matcher matcher = patRegex.matcher(content.trim());
            if (matcher.find()) {
                value = matcher.group(1);
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return value;
    }

    private static boolean checkInterface4G(String portStatus) {
        if (isNullOrEmpty(portStatus)) {
            return false;
        }

        return "administratively down".equals(portStatus.toLowerCase());
    }

    private static int getRingId4G(TelnetClientUtil telnetClientMT, TelnetClientUtil telnetClientBK,
                                   int start) {
        try {
            do {
                String vsiNameSer = "4G_" + start + "_Service";
                String cmdRun = "display vsi name " + vsiNameSer;

                //Check AGG Master
                String content = getValueTelnet(telnetClientMT, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                if (checkVsiName(content)) {
                    //Check AGG Backup

                    content = getValueTelnet(telnetClientBK, cmdRun, TIME_OUT,
                            null, null, "---- More ----");
                    if (checkVsiName(content)) {
                        String vsiNameOAM = "4G_" + start + "_OAM";
                        String cmdRunOAM = "display vsi name " + vsiNameOAM;

                        //Check AGG Master
                        String content1 = getValueTelnet(telnetClientMT, cmdRunOAM, TIME_OUT,
                                null, null, "---- More ----");
                        if (checkVsiName(content1)) {
                            content1 = getValueTelnet(telnetClientBK, cmdRunOAM, TIME_OUT,
                                    null, null, "---- More ----");
                            if (checkVsiName(content1)) {
                                return start;
                            }
                        }
                    }
                }

                start++;
            } while (true);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return -1;
    }

    private static String getInterface4G(TelnetClientUtil telnetClientSrt, String vendor, String version) {
        try {
            if ("HUAWEI".equals(vendor.toUpperCase()) && "S5328".equals(version.toUpperCase())) {
                try {
                    String[] range = new String[]{"0/0/11", "0/0/12", "0/0/13"};

                    for (String inter : range) {
                        String cmdRun = "display interface GigabitEthernet " + inter;
                        String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                                null, null, "---- More ----");

                        String interfaceStatus = getValue(content, "GigabitEthernet" + inter + " current state :", "\n");

                        if (checkInterface4G(interfaceStatus)) {
                            return "GigabitEthernet"+inter;
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
                return "";
            } else if ("ZTE".equals(vendor.toUpperCase()) && "5928E".equals(version.toUpperCase())) {
                try {
                    String[] range = new String[]{"gei_0/11", "gei_0/12", "gei_0/13"};

                    for (String inter : range) {
                        String cmdRun = "show interface " + inter;
                        String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                                null, null, "--More--");

                        String interfaceStatus = getValue(content, inter + " is", ",");

                        if (checkInterface4G(interfaceStatus)) {
                            return inter;
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
                return "";
            } else if ("CISCO".equals(vendor.toUpperCase()) && "ASR901-6CZ-FT".equals(version.toUpperCase())) {
                try {
                    String[] range = new String[]{"0/4", "0/5", "0/6"};

                    for (String inter : range) {
                        String cmdRun = "show interfaces gigabitEthernet " + inter;
                        String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                                null, null, "--More--");

                        String interfaceStatus = getValue(content, "GigabitEthernet" + inter + " is", ",");

                        if (checkInterface4G(interfaceStatus)) {
                            return "GigabitEthernet"+inter;
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
                return "";
            } else if ("HUAWEI".equals(vendor.toUpperCase()) && "ATN910B".equals(version.toUpperCase())) {
                try {
                    String[] range = new String[]{"0/2/21", "0/2/22", "0/2/23"};

                    for (String inter : range) {
                        String cmdRun = "display interface GigabitEthernet " + inter;
                        String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                                null, null, "---- More ----");

                        String interfaceStatus = getValue(content, "GigabitEthernet" + inter + " current state :", "\n");

                        if (checkInterface4G(interfaceStatus)) {
                            return "GigabitEthernet"+inter;
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
                return "";
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static Map<String, String> getVendor4G(TelnetClientUtil telnetClientSrt, String interf, String vendor, String version) {
        Map<String, String> mapValue = new HashMap<>();
        try {
            if ("HUAWEI".equals(vendor.toUpperCase()) && "S5328".equals(version.toUpperCase())) {
                try {
                    String cmdRun = "display transceiver interface GigabitEthernet " + interf;
                    String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                            null, null, "---- More ----");

                    mapValue.put("Vendor PN", getValue(content, "Vendor PN", "\n").replace(":", ""));
                    mapValue.put("Vendor Name", getValue(content, "Vendor Name", "\n").replace(":", ""));
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            } else if ("ZTE".equals(vendor.toUpperCase()) && "5928E".equals(version.toUpperCase())) {
                BufferedReader bf = null;
                StringReader sr = null;
                try {
                    String cmdRun = "show optical-inform interface " + interf;
                    String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                            null, null, "--More--");

                    sr = new StringReader(content);
                    bf = new BufferedReader(sr);
                    String line;
                    while ((line = bf.readLine()) != null) {
                        if (line.contains(interf)) {
                            String[] strs = line.split("\\s+");
                            if (strs.length >= 5) {
                                mapValue.put("Vendor PN", strs[4]);
                                mapValue.put("Vendor Name", strs[3]);
                            }
                            break;
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                } finally {
                    if (sr != null) {
                        sr.close();
                    }

                    if (bf != null) {
                        try {
                            bf.close();
                        } catch (IOException ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                    }
                }
            } else if ("CISCO".equals(vendor.toUpperCase()) && "ASR901-6CZ-FT".equals(version.toUpperCase())) {
                try {
                    String cmdRun = "show controllers GigabitEthernet " + interf + " | include vendor";
                    String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                            null, null, "--More--");

                    mapValue.put("Vendor PN", getValue(content, "vendor_pn", "\n"));
                    mapValue.put("Vendor Name", getValue(content, "vendor_name", "\n"));
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            } else if ("HUAWEI".equals(vendor.toUpperCase()) && "ATN910B".equals(version.toUpperCase())) {
                try {
                    String cmdRun = "display interface GigabitEthernet " + interf + " | include Vendor";
                    String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                            null, null, "---- More ----");

                    mapValue.put("Vendor PN", getValue(content, "The Vendor PN is", "\n"));
                    mapValue.put("Vendor Name", getValue(content, "The Vendor Name is", "\n"));
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return mapValue;
    }

    private static String getMac4G(TelnetClientUtil telnetClientSrt, String interf, String vendor, String version) {
        try {
            if ("HUAWEI".equals(vendor.toUpperCase()) && "S5328".equals(version.toUpperCase())) {

                String cmdRun = "display mac-address dynamic GigabitEthernet " + interf;
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");
                List<String> values = getValueRowColumnIndex(content, 4, 1, 0, "PEVLAN CEVLAN Port", "CONTAIN",
                        null, null, "\\s+", null);

                return values.size() > 0 ? values.get(0) : "";
            } else if ("ZTE".equals(vendor.toUpperCase()) && "5928E".equals(version.toUpperCase())) {

                String cmdRun = "show mac interface " + interf;
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");
                List<String> values = getValueRowColumnIndex(content, 2, 1, 0, "-------------", "CONTAIN",
                        null, null, "\\s+", null);

                return values.size() > 0 ? values.get(0) : "";
            } else if ("CISCO".equals(vendor.toUpperCase()) && "ASR901-6CZ-FT".equals(version.toUpperCase())) {

                String cmdRun = "show mac-address-table interface gigabitEthernet " + interf;
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");
                List<String> values = getValueRowColumnIndex(content, 3, 2, 0, "Vlan", "CONTAIN",
                        null, null, "\\s+", null);

                return values.size() > 0 ? values.get(0) : "";
            } else if ("HUAWEI".equals(vendor.toUpperCase()) && "ATN910B".equals(version.toUpperCase())) {

                String cmdRun = "display arp vpn-instance VRF_4G_Service | include " + interf;
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");
                List<String> values = getValueRowColumnIndex(content, 1, 2, 0, "D-0", "CONTAIN",
                        null, null, "\\s+", null);

                return values.size() > 0 ? values.get(0) : "";
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static String getState4G(TelnetClientUtil telnetClientSrt, String interf, String vendor, String version) {
        try {
            if ("HUAWEI".equals(vendor.toUpperCase()) && "S5328".equals(version.toUpperCase())) {
                String cmdRun = "display interface GigabitEthernet " + interf;
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                return getValue(content, "GigabitEthernet" + interf + " current state :", "\n");
            } else if ("ZTE".equals(vendor.toUpperCase()) && "5928E".equals(version.toUpperCase())) {
                String cmdRun = "show interface " + interf;
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");

                return getValue(content, interf + " is", ",");
            } else if ("CISCO".equals(vendor.toUpperCase()) && "ASR901-6CZ-FT".equals(version.toUpperCase())) {
                String cmdRun = "show interfaces gigabitEthernet " + interf;
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");

                return getValue(content, "GigabitEthernet" + interf + " is", ",");
            } else if ("HUAWEI".equals(vendor.toUpperCase()) && "ATN910B".equals(version.toUpperCase())) {
                String cmdRun = "display interface GigabitEthernet " + interf;
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                return getValue(content, "GigabitEthernet" + interf + " current state :", "\n");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static Map<String, String> getRxTx4G(TelnetClientUtil telnetClientSrt, String interf, String vendor, String version) {
        Map<String, String> mapValue = new HashMap<>();
        try {
            if ("HUAWEI".equals(vendor.toUpperCase()) && "S5328".equals(version.toUpperCase())) {

                String cmdRun = "display transceiver interface GigabitEthernet " + interf + " verbose";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                mapValue.put(PARAMCODE_ENODEB.RX.value, getValue(content, "RX Power(dBM)", "\n").replace(":", ""));
                mapValue.put(PARAMCODE_ENODEB.TX.value, getValue(content, "TX Power(dBM)", "\n").replace(":", ""));
            } else if ("ZTE".equals(vendor.toUpperCase()) && "5928E".equals(version.toUpperCase())) {

                String cmdRun = "show optical-inform details rx-power interface " + interf;
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");
                List<String> values = getValueRowColumnIndex(content, 1, 2, 0, interf, "CONTAIN",
                        null, null, "\\s+", null);

                mapValue.put(PARAMCODE_ENODEB.RX.value, values.size() > 0 ? values.get(0) : "");

                cmdRun = "show optical-inform details tx-power interface " + interf;
                content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");
                values = getValueRowColumnIndex(content, 1, 2, 0, interf, "CONTAIN",
                        null, null, "\\s+", null);

                mapValue.put(PARAMCODE_ENODEB.TX.value, values.size() > 0 ? values.get(0) : "");
            } else if ("CISCO".equals(vendor.toUpperCase()) && "ASR901-6CZ-FT".equals(version.toUpperCase())) {
                String cmdRun = "show interfaces GigabitEthernet " + interf + " transceiver";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");

                List<String> values = getValueRowColumnIndex(content, 1, 1, 0, "Gi" + interf, "CONTAIN",
                        null, null, null, null);

                if (values.size() > 0) {
                    String line = values.get(0);
                    String[] strs = line.split("\\s+");

                    if (strs.length > 5) {
                        mapValue.put(PARAMCODE_ENODEB.TX.value, strs[4]);
                        mapValue.put(PARAMCODE_ENODEB.RX.value, strs[5]);
                    }
                }
            } else if ("HUAWEI".equals(vendor.toUpperCase()) && "ATN910B".equals(version.toUpperCase())) {
                String cmdRun = "display interface GigabitEthernet " + interf + " | include dBm";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                mapValue.put(PARAMCODE_ENODEB.RX.value, getValue(content, "Rx Power:", "dBm"));
                mapValue.put(PARAMCODE_ENODEB.TX.value, getValue(content, "Tx Power:", "dBm"));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return mapValue;
    }

    private static String getCrc4G(TelnetClientUtil telnetClientSrt, String interf, String vendor, String version) {
        try {
            if ("HUAWEI".equals(vendor.toUpperCase()) && "S5328".equals(version.toUpperCase())) {

                String cmdRun = "display interface GigabitEthernet " + interf + " | include CRC";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                return getValue(content, "CRC", ",").replace(":", "").trim();
            } else if ("ZTE".equals(vendor.toUpperCase()) && "5928E".equals(version.toUpperCase())) {

                String cmdRun = "show interface " + interf;
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");

                return getValue(content, "CRC-ERROR", "\n").replace(":", "").trim();
            } else if ("CISCO".equals(vendor.toUpperCase()) && "ASR901-6CZ-FT".equals(version.toUpperCase())) {
                String cmdRun = "show interfaces GigabitEthernet " + interf + " | include CRC";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");

                return getValue(content, ",", "CRC");
            } else if ("HUAWEI".equals(vendor.toUpperCase()) && "ATN910B".equals(version.toUpperCase())) {
                String cmdRun = "display interface GigabitEthernet " + interf + " | include CRC";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                return getValue(content, "CRC:", "packets");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static String getNegotiation4G(TelnetClientUtil telnetClientSrt, String interf, String vendor, String version) {
        try {
            if ("HUAWEI".equals(vendor.toUpperCase()) && "S5328".equals(version.toUpperCase())) {

                String cmdRun = "display interface GigabitEthernet " + interf + " | include Negotiation";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                return getValue(content, "Negotiation:", "\n");
            } else if ("ZTE".equals(vendor.toUpperCase()) && "5928E".equals(version.toUpperCase())) {

                String cmdRun = "show running-config interface " + interf + " | include negotiation";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");

                if (content != null && content.toLowerCase().contains("no negotiation auto")) {
                    return "DISABLE";
                } else {
                    return "ENABLE";
                }
            } else if ("CISCO".equals(vendor.toUpperCase()) && "ASR901-6CZ-FT".equals(version.toUpperCase())) {
                String cmdRun = "show running-config interface gigabitEthernet " + interf + " | include negotiation";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");

                if (content != null && content.toLowerCase().contains("no negotiation auto")) {
                    return "DISABLE";
                } else {
                    return "ENABLE";
                }
            } else if ("HUAWEI".equals(vendor.toUpperCase()) && "ATN910B".equals(version.toUpperCase())) {
                String cmdRun = "display interface GigabitEthernet " + interf + " | include negotiation";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                return getValue(content, "negotiation:", ",");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static String getSpeed4G(TelnetClientUtil telnetClientSrt, String interf, String vendor, String version) {
        try {
            if ("HUAWEI".equals(vendor.toUpperCase()) && "S5328".equals(version.toUpperCase())) {

                String cmdRun = "display interface GigabitEthernet " + interf + " | include Speed";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                return getValue(content, "Speed", ",").replace(":", "").trim();
            } else if ("ZTE".equals(vendor.toUpperCase()) && "5928E".equals(version.toUpperCase())) {

                String cmdRun = "show interface brief";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");

                List<String> values = getValueRowColumnIndex(content, 1, 4, 0, interf, "CONTAIN",
                        null, null, "\\s+", null);

                return values.size() > 0 ? values.get(0) : "";
            } else if ("CISCO".equals(vendor.toUpperCase()) && "ASR901-6CZ-FT".equals(version.toUpperCase())) {
                String cmdRun = "show interfaces gigabitEthernet " + interf + " status";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");

                List<String> values = getValueRowColumnIndex(content, 1, 6, 0, "Gi" + interf, "CONTAIN",
                        null, null, "\\s+", null);

                return values.size() > 0 ? values.get(0) : "";
            } else if ("HUAWEI".equals(vendor.toUpperCase()) && "ATN910B".equals(version.toUpperCase())) {
                String cmdRun = "display interface GigabitEthernet " + interf + " | include Current BW";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                return getValue(content, "Current BW:", ",");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static String getDuplex4G(TelnetClientUtil telnetClientSrt, String interf, String vendor, String version) {
        try {
            if ("HUAWEI".equals(vendor.toUpperCase()) && "S5328".equals(version.toUpperCase())) {

                String cmdRun = "display interface GigabitEthernet " + interf + " | include Duplex";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                return getValue(content, "Duplex", ",").replace(":", "").trim();
            } else if ("ZTE".equals(vendor.toUpperCase()) && "5928E".equals(version.toUpperCase())) {

                String cmdRun = "show interface " + interf;
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");

                return getValue(content, "Duplex", "\n");
            } else if ("CISCO".equals(vendor.toUpperCase()) && "ASR901-6CZ-FT".equals(version.toUpperCase())) {
                String cmdRun = "show interfaces gigabitEthernet " + interf + " status";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");

                List<String> values = getValueRowColumnIndex(content, 1, 5, 0, "Gi" + interf, "CONTAIN",
                        null, null, "\\s+", null);

                return values.size() > 0 ? values.get(0) : "";
            } else if ("HUAWEI".equals(vendor.toUpperCase()) && "ATN910B".equals(version.toUpperCase())) {
                String cmdRun = "display interface GigabitEthernet " + interf + " | include duplex";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                return getValue(content, ",", "mode");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    private static String getGiant4G(TelnetClientUtil telnetClientSrt, String interf, String vendor, String version) {
        try {
            if ("HUAWEI".equals(vendor.toUpperCase()) && "S5328".equals(version.toUpperCase())) {

                String cmdRun = "display interface GigabitEthernet " + interf + " | include Giants";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                return getValue(content, "Giants", "\n").replace(":", "").trim();
            } else if ("ZTE".equals(vendor.toUpperCase()) && "5928E".equals(version.toUpperCase())) {

                String cmdRun = "show interface " + interf;
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");

                return getValue(content, "Dropped", "Fragments").replace(":", "").trim();
            } else if ("CISCO".equals(vendor.toUpperCase()) && "ASR901-6CZ-FT".equals(version.toUpperCase())) {
                String cmdRun = "show interfaces gigabitEthernet " + interf;
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "--More--");

                return getValue(content, ",", "giants");
            } else if ("HUAWEI".equals(vendor.toUpperCase()) && "ATN910B".equals(version.toUpperCase())) {
                String cmdRun = "display interface GigabitEthernet " + interf + " | include Alignment";
                String content = getValueTelnet(telnetClientSrt, cmdRun, TIME_OUT,
                        null, null, "---- More ----");

                return getValue(content, "Alignment:", "packets");
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return "";
    }

    //---------------------------Cac tham so 4G_End

    public static void main(String[] args) throws Exception {
//        Node srt = new NodeServiceImpl().findById(100757l);
//        Node agg1 = new NodeServiceImpl().findById(100832l);//0002 GigabitEthernet2/1/1
//        Node agg2 = new NodeServiceImpl().findById(100275l);//0008
//        
//        CustomerNode client = new CustomerNode();
//        Customer customer = new Customer();
//        customer.setVrfName("L3VPN_AGRIBANK_HYN");
//        client.setInterfaceId("0/0/1");
//		client.setCustomer(customer);
//        
//        //System.out.println(getParamL3(srt, "KV1", client));
//        System.out.println(getParamL2(agg1, agg2, srt, "KV1", ParamUtil.TYPE_NORMAL, "GigabitEthernet1/1/0", "GigabitEthernet1/1/6", client));
        String content = "Interface      portattribute  mode  BW(Mbits) Admin Phy   Prot  Description \n"
                + "gei_1/1        optical    Duplex/full  1000   up    up    up    QNH0384AS03_150.\n"
                + "gei_1/2        optical    Duplex/full  1000   up    up    up    QNH0016AS14_150.\n"
                + "gei_1/3        optical    Duplex/full  1000   up    up    up    QNH0016AS13_190.\n"
                + "gei_1/4        optical    Duplex/full  1000   up    up    up    QNH0016AS10(180.\n"
                + "gei_1/5        electric   Duplex/full  100    up    down  down  CAMERA_TRAM_TD\n"
                + "gei_1/6        optical    Duplex/full  1000   up    down  down  OW_BOTC_KBNN_Tie\n"
                + "gei_1/7        optical    Duplex/full  1000   up    up    up    QNH0016AS09(150.\n"
                + "gei_1/8        optical    Duplex/full  1000   up    up    up    QNH0016AS08_180.\n"
                + "gei_1/9        electric   Duplex/full  100    up    up    up    MUL_TEST\n"
                + "gei_1/10       optical    Duplex/full  1000   up    up    up    QNH0016AS07_180.";
        List<String> values = getValueRowColumnIndex(content, 1, 4, 0, "gei_1/1", "CONTAIN",
                null, null, "\\s+", null);

        System.out.println(values.size() > 0 ? values.get(0) : "");
    }
}
