package com.viettel.util;

import com.viettel.nms.nocpro.service.*;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by hanhnv68 on 8/9/2017.
 */
public class NocProWebserviceUtils {

    private static final Logger logger = Logger.getLogger(NocProWebserviceUtils.class);
    private static NocproWebservice service;
    static ResourceBundle bundle = ResourceBundle.getBundle("config");

    static {
        try {
            service = new NocproWebservice_Service(new URL(bundle.getString("ws_nocpro_url"))).getNocproWebservicePort();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }
    public static void updateCrStartTime(String crNumber){
        try {
            AuthorityBO authorityBO = new AuthorityBO();
            authorityBO.setUserName(bundle.getString("ws_nocpro_user"));
            authorityBO.setPassword(bundle.getString("ws_nocpro_pass"));
            authorityBO.setRequestId(Integer.valueOf(bundle.getString("ws_nocpro_requestId")));
            JsonResponseBO jsonResponseBO = service.updateCRStartTime(authorityBO, crNumber);
            logger.info("---status " + jsonResponseBO.getStatus());
        }catch(Exception ex){
            logger.error(ex.getMessage(),ex);
        }
    }

    public static NocproWebservice getService() {
        return service;
    }

    public static void setService(NocproWebservice service) {
        NocProWebserviceUtils.service = service;
    }
    public static void main(String[] args){
        updateCrStartTime("quytv7");
    }
    
}
