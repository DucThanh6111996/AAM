package com.viettel.util;

import com.viettel.aom.webservice.AomWebservicePortBindingStub;
import com.viettel.aom.webservice.AomWebservice_PortType;
import com.viettel.aom.webservice.AomWebservice_ServiceLocator;
import com.viettel.aom.webservice.AuthorityBO;
//import com.viettel.aom.webservice.service.ChecklistWebservicePortBindingStub;
//import com.viettel.aom.webservice.service.ChecklistWebservice_PortType;
//import com.viettel.aom.webservice.service.ChecklistWebservice_ServiceLocator;
//import com.viettel.iim.services.main.IimServicesPortBindingStub;
import org.apache.axis.components.net.DefaultCommonsHTTPClientProperties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.net.URL;

/**
 * @author quanns2
 */
public final class AomClientFactory {
    private static Logger logger = LogManager.getLogger(AomClientFactory.class);

//    private static AuthorityBO author = new AuthorityBO(AppConfig.getInstance().getProperty("password_webservice_database_server_qltn"), 1, AppConfig.getInstance().getProperty("user_webservice_database_server_qltn"));
    private static AuthorityBO author = new AuthorityBO("bed0c2ddbfff2e0e3077e2dc2885b38a", 1, "6f2efd0770ec8d37c22a0e885a32707f");

    public static AomWebservice_PortType create() throws Exception {

        AomWebservice_ServiceLocator service = new AomWebservice_ServiceLocator();

//        AomWebservicePortBindingStub iimPortType = (AomWebservicePortBindingStub) service.getAomWebservicePort(new URL(AppConfig.getInstance().getProperty("link_webservice_database_server_qltn")));
//        AomWebservicePortBindingStub iimPortType = (AomWebservicePortBindingStub) service.getAomWebservicePort(new URL("http://10.60.97.17:8001/AOM_Webservice/AomWebservice"));
        AomWebservicePortBindingStub iimPortType = (AomWebservicePortBindingStub) service.getAomWebservicePort(new URL(AppConfig.getInstance().getProperty("url_ws_aom")));

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