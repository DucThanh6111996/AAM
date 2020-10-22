package com.viettel.it.util;

import com.jcraft.jsch.*;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author Nguyễn Xuân Huy <huynx6@viettel.com.vn>
 * @sin Mar 10, 2016
 * @version 1.0
 */
public class JSchSshUtil implements Serializable {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private String host;
    private int port;
    private String username;
    private String password;
    private String prompt;
    private String terminator;
    private BufferedInputStream dataIn;
    private BufferedOutputStream dataOut;
    private Session session;
    private Channel channel;
    private StringBuilder log = new StringBuilder();
    private boolean isConnect = false;
    private Logger logger = LogManager.getLogger(JSchSshUtil.class);
    private boolean isSaveLog = true;
    private boolean isDebug;
    private String ctrlC = "\u0003";
    private String vendor, version, nodeType;
    private PrintStream shellStream;

    public JSchSshUtil(String host, int port, String usename, final String password, String promt,
                       String terminator, int timeOut, boolean isLocal, String vendor, String version, String nodeType) throws Exception {
        this.host = host;
        this.port = port;
        this.username = usename;
        this.password = password;
        this.prompt = promt;
        this.vendor = vendor;
        this.version = version;
        this.nodeType = nodeType;
        this.terminator = isNullOrEmpty(terminator) ? "\r" : getTerminator(terminator);
        try {
            JSch shell = new JSch();
            // get a new session
            session = shell.getSession(username, isLocal ? "localhost" : this.host, this.port);
            // set user password and connect to a channel
            session.setUserInfo(new SSHUserInfo(password, true));
            java.util.Properties config = new java.util.Properties();
            config.put("StrictHostKeyChecking", "no");
            config.put("PreferredAuthentications", "privatekey,keyboard-interactive,password");
            if ("DSC".equalsIgnoreCase(nodeType)) {
                config.put("kex", "diffie-hellman-group1-sha1");
            }
            session.setConfig(config);
            session.setTimeout(timeOut);
        } catch (Exception e) {
            throw e;
        }
    }

    public boolean connect() throws Exception {
        try {
            this.session.connect();
            this.channel = session.openChannel("shell");
            if ("DSC".equalsIgnoreCase(nodeType)) {
                ((ChannelShell) channel).setPtyType("vt100");
            } else {
                ((ChannelShell) channel).setPtyType("dumb");
            }
            ((ChannelShell) channel).setPtySize(4096, 24, 640, 480);
            this.channel.connect();
            this.dataIn = new BufferedInputStream(channel.getInputStream());
            this.dataOut = new BufferedOutputStream(channel.getOutputStream());
            // Get prompt
            OutputStream out = new ByteArrayOutputStream();
            String sbuff = null;
            try {
                channel.setOutputStream(out);
                shellStream = new PrintStream(this.channel.getOutputStream());
                shellStream.print(this.terminator);
                shellStream.flush();

                if (version != null && "ATCA".equals(version.toUpperCase()) && vendor != null && "NOKIA".equals(version.toUpperCase())) {
                    shellStream.print("fsclish");
                    shellStream.flush();
                }

                long start = new Date().getTime();
                int timeOut;
                if (session.getTimeout() < 10000) {
                    timeOut = 10000;
                } else {
                    timeOut = session.getTimeout();
                }
                Thread.sleep(2000);
                sbuff = out.toString();

                if (this.prompt == null) {
                    if (sbuff != null && sbuff.length() > 2) {
                        prompt = sbuff.substring(sbuff.length() - 2).trim();
                    }

                    while (prompt.isEmpty() && new Date().getTime() < start + timeOut) {
                        sbuff = out.toString();
                        if (sbuff != null && sbuff.length() > 2) {
                            prompt = sbuff.substring(sbuff.length() - 2).trim();
                        }
                    }
                }

                if (this.prompt == null || this.prompt.trim().isEmpty()) {
                    this.prompt = "$";
                }

                log.append("\r\n").append(sbuff);
                logger.info("prompt: " + prompt);
            } catch (IOException e) {
                throw e;
            } catch (InterruptedException e) {
                throw e;
            }

            isConnect = true;
            logger.info("[" + new Date() + "] " + "Connected Server: " + host + " estabished!");
            log.append("[").append(new Date()).append("] " + "Connected Server: ").append(host).append(" estabished!\r\n");
        } catch (JSchException e) {
            isConnect = false;
            throw e;
        } catch (IOException e) {
            isConnect = false;
            throw e;
        } catch (InterruptedException e) {
            isConnect = false;
            throw e;
        }

        return isConnect;
    }

    public boolean connect(int timeOut) throws Exception {
        try {

            this.session.connect();
            this.channel = session.openChannel("shell");
            if ("DSC".equalsIgnoreCase(nodeType)) {
                ((ChannelShell) channel).setPtyType("vt100");
            } else {
                ((ChannelShell) channel).setPtyType("dumb");
            }
            ((ChannelShell) channel).setPtySize(4096, 24, 640, 480);
            this.channel.connect();
            this.dataIn = new BufferedInputStream(channel.getInputStream());
            this.dataOut = new BufferedOutputStream(channel.getOutputStream());
            // Get prompt
            OutputStream out = new ByteArrayOutputStream();
            String sbuff = null;
            try {
                channel.setOutputStream(out);
                shellStream = new PrintStream(this.channel.getOutputStream());
                shellStream.print(this.terminator);
                shellStream.flush();

                if (timeOut < 10000) {
                    timeOut = 10000;
                }

                //Wait for timeout.
                Thread.sleep(timeOut);
                sbuff = out.toString();

                if (this.prompt == null) {
                    if (sbuff != null && sbuff.length() > 2) {
                        prompt = sbuff.substring(sbuff.length() - 2).trim();
                    }
                }

                if (this.prompt == null || this.prompt.trim().isEmpty()) {
                    this.prompt = "$";
                }

                log.append("\r\n").append(sbuff);
                logger.info("prompt: " + prompt);
            } catch (IOException e) {
                throw e;
            } catch (InterruptedException e) {
                throw e;
            }

            isConnect = true;
            logger.info("[" + new Date() + "] " + "Connected Server: " + host + " estabished!");
            log.append("[").append(new Date()).append("] " + "Connected Server: ").append(host).append(" estabished!\r\n");
        } catch (JSchException e) {
            isConnect = false;
            throw e;
        } catch (IOException e) {
            isConnect = false;
            throw e;
        } catch (InterruptedException e) {
            isConnect = false;
            throw e;
        }

        return isConnect;
    }

    private String getTerminator(String terminator) {
        String ter = "";
        int i = 0;
        while (i < terminator.length()) {
            char ch = terminator.charAt(i);
            if (ch == '\\') {
                if (i < terminator.length() - 1) {
                    i++;
                    switch (terminator.charAt(i)) {
                        case 't':
                            ter += "\t";
                            break;
                        case 'r':
                            ter += "\r";
                            break;
                        case 'n':
                            ter += "\n";
                            break;
                        default:
                            ter += "\\" + terminator.charAt(i);
                            break;

                    }
                } else {
                    ter += ch;
                }
            } else {
                ter += ch;
            }
            i++;
        }
        return ter;
    }

    public List<String> subLogToList(String log) {
        List<String> list = new ArrayList<>();
        try {
            list = Arrays.asList(log.split("\n"));
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
        return list;
    }

    public void logAppendLine(String log) {
        this.log.append("\r\n" + log);
    }

    public boolean connectShellSsh(String sshAccount, String sshPass) throws Exception {
        try {
            String output = "";
            Result result = sendLineWithTimeOutNew("ssh " + sshAccount + "@" + this.host, 10000, false, "?", "password:", "Password:");
            if (result == null || !result.isSuccessSent) {
                throw new Exception("");
            }
            output += result.getResult();
            if (output.trim().endsWith("?")) {
                result =  sendLineWithTimeOutNew("yes", 10000, false, "password:", "Password:");
                if (result == null || !result.isSuccessSent) {
                    throw new Exception("");
                }
                output += result.getResult();

                result =  sendLineWithTimeOutNew(sshPass, 10000, false, "#", ">");
                if (result == null || !result.isSuccessSent) {
                    throw new Exception("");
                }
                output += result.getResult();
            } else {
                result =  sendLineWithTimeOutNew(sshPass, 10000, false, "#", ">");
                if (result == null || !result.isSuccessSent) {
                    throw new Exception("");
                }
                output += result.getResult();
            }

            if (version != null && "ATCA".equals(version.toUpperCase()) && vendor != null && "NOKIA".equals(vendor.toUpperCase())) {
                result =  sendLineWithTimeOutNew("fsclish", 10000, false, "#", ">");
                if (result == null || !result.isSuccessSent) {
                    throw new Exception("");
                }
                output += result.getResult();
            }

            if (output.length() > 2) {
                prompt = output.substring(output.length() - 3);
                for (int i = output.length() - 4; i >= 0 ; i--) {
                    prompt = output.substring(i);
                    if (!prompt.startsWith(" ")) {
                        break;
                    }
                }
                prompt = prompt.trim();
            } else {
                prompt = output.trim();
            }
            logger.info("prompt2: " + prompt);

            isConnect = true;
        } catch (Exception e) {
            isConnect = false;
            throw e;
        }

        return isConnect;
    }

    public Result sendLineWithMore(String command, String morePrompt, String moreCommand, long timeOut, String... otherPrompt) throws InterruptedException, IOException, Exception {
        Result resultAll = new Result();
        boolean isMore = false;

        String response;
        String[] promts = null;
        if (otherPrompt != null) {
            promts = new String[otherPrompt.length + 1];
            for (int i = 0; i < otherPrompt.length; i++) {
                promts[i] = otherPrompt[i];
            }
            promts[promts.length - 1] = morePrompt;
        }
        do {
            Result result;
            if (isMore) {
                result = sendLineWithTimeOutNew(moreCommand, timeOut, false, promts);
            } else {
                result = sendLineWithTimeOutNew(command, timeOut, false, promts);
            }
            if (result == null) {
                resultAll.isSuccessSent = false;
                break;
            }
            response = result.getResult();
            if (response != null) {
                isMore = response.trim().endsWith(morePrompt);
            }

            if (response != null && response.lastIndexOf("\n") > 0) {
                response = response.substring(0, response.lastIndexOf("\n"));
            }

            resultAll.setResult((resultAll.getResult() == null ? "" : resultAll.getResult()) + (response == null ? "" : "\n" + response.trim()));
            resultAll.isSuccessSent = result.isSuccessSent;
            if (!result.isSuccessSent) {
                break;
            }
        } while (isMore);

        return resultAll;
    }

    /**
     * @param command
     * @param timeOut
     * @param otherPrompt
     * @return {@link Result}: Status and result of command  </br>
     * @author huynx6
     * @throws InterruptedException
     * @throws IOException
     *
     */
    public Result sendLineWithTimeOutNew(String command, long timeOut, boolean isRemoveLastLine, String... otherPrompt) throws InterruptedException, IOException, Exception {
        try {
            if (!this.getSession().isConnected()) {
                this.connect();
            }
        } catch (Exception e) {
            throw e;
        }
        if (timeOut < 10000) {
            timeOut = 10000;
        }
        OutputStream out = new ByteArrayOutputStream();
        Result result = new Result();
        channel.setOutputStream(out);
        shellStream = new PrintStream(this.channel.getOutputStream());
        shellStream.print(command + this.terminator);
        shellStream.flush();
        long start = new Date().getTime();
        while (new Date().getTime() < start + timeOut) {
            String sbuff = out.toString();
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw e;
            }
            if (sbuff.trim().endsWith(this.prompt)) {
                result.setSuccessSent(true);
                break;
            }
            System.out.println("sbuff: " + sbuff);
            if (otherPrompt != null) {
                boolean isBreak = false;
                for (String prpt : otherPrompt) {
                    if (sbuff.trim().endsWith(prpt)) {
                        result.setSuccessSent(true);
                        isBreak = true;
                        break;
                    }
                }
                if (isBreak) {
                    break;
                }
            }
            try {
                Thread.sleep(200);
            } catch (InterruptedException e) {
                throw e;
            }
            if (sbuff.length() < out.toString().length()) {
                timeOut += 20000;
            }
        }

        if (result.isSuccessSent == null) {
            result.setSuccessSent(false);
            logger.info("Send ctrl-c");
            shellStream.print(ctrlC + this.terminator);
            shellStream.flush();
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw e;
            }
        }
        String lastoutput = out.toString();
        if (isRemoveLastLine) {
            if (lastoutput != null && lastoutput.lastIndexOf("\n") > 0) {
                lastoutput = lastoutput.substring(0, lastoutput.lastIndexOf("\n"));
            }
        }
        if (isSaveLog) {
            log.append(out);
        }
        if (isDebug) {
            logger.info(lastoutput);
        }
        result.setResult(lastoutput);
        return result;
    }

    /**
     * @author : Longlt6
     *
     * @param command
     * @param timeOut
     * @param otherPrompt
     * @return
     * @throws InterruptedException
     * @throws IOException
     */
    public Result sendLineWithTimeOutAdvance(String command, long timeOut, String... otherPrompt) throws InterruptedException, IOException, Exception {
        try {
            if (!this.getSession().isConnected()) {
                this.connect();
            }
        } catch (Exception e) {
            throw e;
        }
        if (timeOut < 10000) {
            timeOut = 10000;
        }
        OutputStream out = new ByteArrayOutputStream();
        Result result = new Result();
        try {
            channel.setOutputStream(out);
            shellStream = new PrintStream(this.channel.getOutputStream());
            shellStream.print(command + this.terminator);
            shellStream.flush();

            boolean check = true;
            long start = new Date().getTime();
            long maxWaitingTime = start + 600 * 1000;
            long endTime = start + timeOut;
            int maxSentCtrl = 10;

            /*while (new Date().getTime() < start + timeOut) {*/
            while (check) {
                String sbuff = out.toString();
                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw e;
                }
                if (sbuff.trim().endsWith(this.prompt)) {

                    try {
                        Thread.sleep(2000); // sleep 2s
                    } catch (Exception e) {
                        throw e;
                    }

                    if (sbuff.length() == out.toString().length()) { //neu khong con nhan dc ky tu nao
                        result.setSuccessSent(true);
                        check = false;
                        break;
                    }
                }
                if (otherPrompt != null && otherPrompt.length > 0) {
                    boolean isBreak = false;
                    for (String prpt : otherPrompt) {
                        if (sbuff.trim().endsWith(prpt)) {
                            result.setSuccessSent(true);
                            isBreak = true;
                            check = false;
                            break;
                        }
                    }
                    if (isBreak) {
                        break;
                    }
                }

                try {
                    Thread.sleep(200);
                } catch (InterruptedException e) {
                    throw e;
                }
                if (sbuff.length() < out.toString().length()) {
                    endTime += 20000;
                } else {
                    if (new Date().getTime() > endTime) {
                        logger.info("Send ctrl-c");
                        shellStream.print(ctrlC + this.terminator);
                        shellStream.flush();
                        try {
                            Thread.sleep(2000);
                        } catch (InterruptedException e) {
                            throw e;
                        }
                    }
                }
                //neu vuot qua 10 phut thi gui lenh ctrl C
                if (new Date().getTime() > maxWaitingTime) {
                    maxSentCtrl--;
                    if (maxSentCtrl < 0) {
                        return null;
                    }
                    logger.info("Send ctrl-c");
                    shellStream.print(ctrlC + "\r");
                    shellStream.flush();
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        throw e;
                    }

                }
            }

        } catch (IOException e) {
            throw e;
        } finally {
            if (isSaveLog) {
                log.append(out);
            }
        }
        // printStream for convenience
        if (result.isSuccessSent == null) {
            result.setSuccessSent(false);
        }
        String lastoutput = out.toString();
//        if (isSaveLog) {
//            log.append(lastoutput);
//        }
        if (isDebug) {
            logger.info(lastoutput);
        }
        result.setResult(lastoutput);

        return result;
    }

    public String creatResult(String log, String strBegin, String strEnd) {
        try {

            log = log.trim().replaceAll("\\s*\b", "");
            String result = log.trim().replaceAll("\\x1b[^m]*m", "").replaceAll("[\\x00\\x08\\x0B\\x0C\\x0E-\\x1F]", "");
            int beginSub = result.indexOf(strBegin.trim());
            if (beginSub != -1) {
                result = result.substring(beginSub + strBegin.length() + 1);
            }
            // result=result.trim(); // xóa các dòng trống
            int lastLF = result.lastIndexOf("\n");
            if (lastLF != -1) {
                String endLine = result.substring(lastLF);
                if (endLine.contains(strEnd)) {
                    result = result.substring(0, lastLF);
                }
            } else { // không có dấu zuống dòng

                if (result.contains(strEnd)) {
                    result = "";
                }
            }

            return result;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return log;
        }

    }

    public void disconnect() throws Exception {
        if (isConnect) {
            try {
                dataIn.close();
                dataOut.close();
                shellStream.close();
                channel.disconnect();
                session.disconnect();
                isConnect = false;
                logger.info("[" + new Date() + "] " + "Disconected Server " + host + " success.");
                log.append("[" + new Date() + "] " + "Disconected Server " + host + " success.\r\n");
            } catch (Exception e) {
                throw e;
            }
        }
    }
    
    private boolean isNullOrEmpty(String content) {
        return content == null || content.trim().isEmpty();
    }

    private Session getSession() {
        return session;
    }

    public String getLog() {
        return log.toString();
    }

    public ChannelSftp getSftpChanel() throws JSchException {
        ChannelSftp sftp = (ChannelSftp) session.openChannel("sftp");
        sftp.connect();
        return sftp;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isConnect() {
        return isConnect;
    }

    public boolean isDebug() {
        return isDebug;
    }

    public void setDebug(boolean isDebug) {
        this.isDebug = isDebug;
    }

}
