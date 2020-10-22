package com.viettel.it.util;

import com.viettel.iim.services.main.*;
import com.viettel.controller.AamConstants;
import org.apache.log4j.Logger;

import java.net.URL;
import java.util.ResourceBundle;

/**
 * Created by hanh on 5/8/2017.
 */
public class IimWebservice {
    private static final Logger logger = Logger.getLogger(IimWebservice.class);
    private static IimServices_PortType service;
    private static ResourceBundle bundle;
    private static final int TIME_OUT = 30000;

    static {
        try {
            bundle = ResourceBundle.getBundle("config");
            IimServices_ServiceLocator serviceLocator = new IimServices_ServiceLocator();
            service = serviceLocator.getIimServicesPort(new URL(bundle.getString("link_webservice_database_server_qltn")));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static JsonResponseBO getDataJson(String request_code, ParameterBO param) {
        JsonResponseBO jsonData = null;
        try {
            AuthorityBO author = new AuthorityBO(bundle.getString("password_webservice_database_server_qltn"), 1L, bundle.getString("user_webservice_database_server_qltn"));
            ParameterBO[] params = new ParameterBO[1];
            params[0] = param;
            RequestInputBO request = new RequestInputBO(request_code, 2457, params);

            jsonData = service.getDataJson(author, request, AamConstants.NATION_CODE.VIETNAM);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return jsonData;
    }

    public static JsonResponseBO getParamValues(String request_code, Long db_id) {
        JsonResponseBO jsonData = null;
        try {
            AuthorityBO author = new AuthorityBO(bundle.getString("password_webservice_database_server_qltn"), 1L, bundle.getString("user_webservice_database_server_qltn"));
            ParameterBO param = new ParameterBO("db_id", "db_id", null, "Long", db_id + "");
            ParameterBO[] params = new ParameterBO[1];
            params[0] = param;
            RequestInputBO request = new RequestInputBO(request_code, 2457, params);

            jsonData = service.getDataJson(author, request, AamConstants.NATION_CODE.VIETNAM);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return jsonData;
    }
}
