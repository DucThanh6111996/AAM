package com.viettel.util;

import com.viettel.iim.services.main.AuthorityBO;
import com.viettel.iim.services.main.IimServicesPortBindingStub;
import com.viettel.iim.services.main.IimServices_PortType;
import com.viettel.iim.services.main.IimServices_ServiceLocator;
import org.apache.axis.components.net.DefaultCommonsHTTPClientProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

/**
 * @author quanns2
 */
public final class IimClientFactory {
    private static Logger logger = LogManager.getLogger(com.viettel.controller.IimClientFactory.class);

    private static AuthorityBO author = new AuthorityBO(AppConfig.getInstance().getProperty("password_webservice_database_server_qltn"), 1L, AppConfig.getInstance().getProperty("user_webservice_database_server_qltn"));

    public static IimServices_PortType create() throws Exception {

        IimServices_ServiceLocator service = new IimServices_ServiceLocator();

        IimServicesPortBindingStub iimPortType = (IimServicesPortBindingStub) service.getIimServicesPort(new URL(AppConfig.getInstance().getProperty("link_webservice_database_server_qltn")));
//        iimPortType.setTimeout(60*60*1000);
        iimPortType._setProperty(DefaultCommonsHTTPClientProperties.CONNECTION_DEFAULT_CONNECTION_TIMEOUT_KEY, 60000);
        iimPortType._setProperty(DefaultCommonsHTTPClientProperties.CONNECTION_DEFAULT_SO_TIMEOUT_KEY, 60*60*1000);
        iimPortType._setProperty(DefaultCommonsHTTPClientProperties.CONNECTION_POOL_TIMEOUT_KEY, 60000);
        iimPortType._setProperty(DefaultCommonsHTTPClientProperties.MAXIMUM_CONNECTIONS_PER_HOST_PROPERTY_KEY, 100);
        iimPortType._setProperty(DefaultCommonsHTTPClientProperties.MAXIMUM_TOTAL_CONNECTIONS_PROPERTY_KEY, 100);

        return iimPortType;
    }

    public static AuthorityBO getAuthor() {
        return author;
    }
}