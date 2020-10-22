package com.viettel.util;

import com.viettel.jackson.ObjectMapperResolver;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.glassfish.jersey.apache.connector.ApacheClientProperties;
import org.glassfish.jersey.apache.connector.ApacheConnectorProvider;
import org.glassfish.jersey.client.ClientConfig;
import org.glassfish.jersey.client.ClientProperties;
import org.glassfish.jersey.client.authentication.HttpAuthenticationFeature;
import org.glassfish.jersey.jackson.JacksonFeature;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;

/**
 * @author quanns2
 */
public final class AamClientFactory {

    public static Client create(String username, String password) {
//        HttpRoute httpRoute = new HttpRoute(new HttpHost("localhost", 8090));
        ClientConfig clientConfig = new ClientConfig();
        // values are in milliseconds
        clientConfig.property(ClientProperties.READ_TIMEOUT, 60000);
        clientConfig.property(ClientProperties.CONNECT_TIMEOUT, 60000);

        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(40);
//        connectionManager.setMaxPerRoute(httpRoute, 40);

        clientConfig.property(ApacheClientProperties.CONNECTION_MANAGER, connectionManager);
        clientConfig.connectorProvider(new ApacheConnectorProvider());           // jersey specific
        RequestConfig reqConfig = RequestConfig.custom()                         // apache HttpClient specific
                .setConnectTimeout(60000)
                .setSocketTimeout(60000)
                .setConnectionRequestTimeout(60000)
                .build();

        clientConfig.property(ApacheClientProperties.REQUEST_CONFIG, reqConfig);

        if(username!=null && password!= null) {
            HttpAuthenticationFeature feature = HttpAuthenticationFeature.basic(username, password);
            clientConfig.register(feature);
        }
        Client client = ClientBuilder.newClient(clientConfig);
        client.register(JacksonFeature.class);
        client.register(ObjectMapperResolver.class);

        return client;
    }
}