package com.viettel.controller;


import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.ViewScoped;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpSession;

import com.viettel.bean.ResultGetAccount;
import com.viettel.it.util.*;
import com.viettel.persistence.IimService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.context.RequestContext;

import com.viettel.bean.ExcecuteInfoObj;
import com.viettel.thread.LogOnlineProgram;
import com.viettel.util.PasswordEncoderQltn;

@ManagedBean
@ViewScoped
public class LogOnlineController {
    private static Logger logger = LogManager.getLogger(LogOnlineController.class);

    public void setIimService(IimService iimService) {
        this.iimService = iimService;
    }

    @ManagedProperty(value = "#{iimService}")
    IimService iimService;

    private String searchLog;
    private String searchText;
    private LogOnlineProgram onlineProgram;
    private ExcecuteInfoObj selectObj = new ExcecuteInfoObj();
    private Module app;
    private String fullLogPacth = null;


    public void setup(ExcecuteInfoObj obj) {
        this.selectObj = obj;
        this.clear();
        try {
            this.app = iimService.findModuleById(AamConstants.NATION_CODE.VIETNAM, obj.getAppId());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


    public void clear() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        System.out.printf("sesId : " + session.getId());
        this.app = null;
        try {

            if (this.onlineProgram == null) {
                Object obj = session.getAttribute("def_ssh_con");
                if (obj != null && (obj instanceof LogOnlineProgram)) {
                    this.onlineProgram = (LogOnlineProgram) obj;
                } else {
                    this.onlineProgram = new LogOnlineProgram();
                }
            }

            if (this.onlineProgram != null && this.onlineProgram.isLive()) {
                this.onlineProgram.disconnect();
            }
            this.searchLog = "";
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            session.setAttribute("def_ssh_con", this.onlineProgram);
        }
    }

    public void connect() {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        System.out.printf("sesId : " + session.getId());
        try {
            if (this.onlineProgram != null && this.onlineProgram.isLive()) {
                this.onlineProgram.disconnect();
            }

            if (this.app == null) {
                this.searchLog = MessageUtil.getResourceBundleMessage("module.info.not.found");
                return;
            }

            String folderPacth = app.getExecutePath();
//            String logPatch = app.getLogPath();
            String logPatch = app.getFullLogPath();

            logPatch = logPatch.trim().replaceAll(" +", " ");
            String strNow = new SimpleDateFormat("yyyyMMdd").format(new Date());
            logPatch = logPatch.replace("$(date +\"%Y%m%d\")", strNow)
                    .replace("$(date +%Y%m%d)", strNow)
                    .replace("yyyymmdd", strNow);

            String[] allInfo = logPatch.split(" ");
            logPatch = allInfo[allInfo.length - 1];

            this.fullLogPacth = logPatch;
            if (logPatch.trim().startsWith("./") || logPatch.trim().startsWith("../")) {

                if (folderPacth != null && !folderPacth.trim().isEmpty()) {
                    folderPacth = folderPacth.trim();
                    while (logPatch.trim().startsWith("../")) {

                        logPatch = logPatch.substring(3);
                        int lastSeparator = folderPacth.lastIndexOf("/");
                        if (lastSeparator != -1) {
                            folderPacth = folderPacth.substring(0, lastSeparator);
                        }
                    }
                    if (logPatch.trim().startsWith("./")) {
                        logPatch = logPatch.substring(2);
                    }
                    if (logPatch.trim().startsWith("/"))
                        logPatch = logPatch.substring(1);
                    if (!folderPacth.trim().endsWith("/")) {
                        folderPacth = folderPacth.trim().concat("/");
                    }
                    this.fullLogPacth = folderPacth + logPatch;
                }
            }

            String password = "";
            try {
                password = PasswordEncoderQltn.decrypt(password);
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            //20181023_tudn_start load pass security
//                password = this.app.getPassword();
            Map<String, String> mapConfigSecurity = SecurityService.getConfigSecurity();
            ResultGetAccount resultGetAccount = SecurityService.getPassSecurity(app.getIpServer(),SessionUtil.getCurrentUsername()
                    ,app.getUsername(),null,null,null,null, com.viettel.it.util.LogUtils.getRequestSessionId()
                    ,password,mapConfigSecurity,null);
            if(!resultGetAccount.getResultStatus()){
                this.searchLog = "Not Connect to Server".concat("\r\n" + MessageUtil.getResourceBundleMessage("error.code") + ": " + resultGetAccount.getResultMessage());
                return;
            }else{
                password = resultGetAccount.getResult();
            }
            this.onlineProgram = new LogOnlineProgram(app.getIpServer(), 22, app.getUsername(), password, null);
            //this.onlineProgram = new LogOnlineProgram("10.60.5.133", 22, "ptpm_checklist", "P9^5c4sc*76s", null);

            boolean connect = this.onlineProgram.connect();
            if (!connect) {
                this.searchLog = "Not Connect to Server".concat("\r\n" + MessageUtil.getResourceBundleMessage("error.code") + ": " + this.onlineProgram.getConnectErrorLog());
                return;
            }
            this.searchLog = MessageUtil.getResourceBundleMessage("waiting.some.second");
            RequestContext.getCurrentInstance().execute(" if( ! PF('onlinePoll').isActive()) PF('onlinePoll').start()");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            session.setAttribute("def_ssh_con", this.onlineProgram);
        }
    }

    public void disconnect() {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        HttpSession session = (HttpSession) facesContext.getExternalContext().getSession(true);
        System.out.printf("sesId : " + session.getId());
        try {
            if (this.onlineProgram == null) {
                return;
            }
            this.onlineProgram.disconnect();
            this.onlineProgram = null;
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            session.setAttribute("def_ssh_con", this.onlineProgram);
        }

    }

    public void tailLogOnline() {
        try {
            if (this.onlineProgram == null || !this.onlineProgram.isLive()) {
                connect();
            }
            this.onlineProgram.sendBreakCommand();
            this.onlineProgram.sendBreakCommand();
            this.onlineProgram.sendCommand("tail -1000f " + this.fullLogPacth);
            //this.onlineProgram.sendCommand("tail -1000f "+"/u01/ptpm_checklist/ocs_tomcat/logs/catalina.out");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void refreshScreen() {
        try {
            System.out.println("Poll Running");
            this.searchLog = "";
            if (onlineProgram != null) {
                this.searchLog = onlineProgram.getDataReceive();
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void searchData() {
        try {

            if (this.onlineProgram == null || !this.onlineProgram.isLive()) {
                connect();
            }
            this.onlineProgram.sendBreakCommand();
            this.onlineProgram.sendBreakCommand();
            this.onlineProgram.sendCommand("tail -1000000 " + this.fullLogPacth + " | grep '" + searchText + "'");
            //this.onlineProgram.sendCommand("tail -100000 "+"/u01/ptpm_checklist/ocs_tomcat/logs/catalina.out"+" | grep '"+searchText+"'");

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public String getSearchLog() {
        return searchLog;
    }

    public void setSearchLog(String searchLog) {
        this.searchLog = searchLog;
    }

    public String getSearchText() {
        return searchText;
    }

    public void setSearchText(String searchText) {
        this.searchText = searchText;
    }

    public LogOnlineProgram getOnlineProgram() {
        return onlineProgram;
    }

    public void setOnlineProgram(LogOnlineProgram onlineProgram) {
        this.onlineProgram = onlineProgram;
    }

    public ExcecuteInfoObj getSelectObj() {
        return selectObj;
    }

    public void setSelectObj(ExcecuteInfoObj selectObj) {
        this.selectObj = selectObj;
    }

}
