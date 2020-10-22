package com.viettel.util;

import java.net.Socket;
import java.util.Properties;

import org.apache.axis.AxisFault;
import org.apache.axis.AxisProperties;
import org.apache.axis.MessageContext;
import org.apache.axis.components.net.BooleanHolder;
import org.apache.axis.components.net.DefaultSocketFactory;
import org.apache.axis.components.net.SocketFactory;
import org.apache.axis.configuration.EngineConfigurationFactoryDefault;
import org.apache.axis.transport.http.HTTPSender;
import org.apache.axis.transport.http.SocketHolder;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * This class overrides the Axis intrinsic HTTPSender class in order to control
 * the connection timeout. It also provides a hook to monitor all web service
 * calls in order to log calls or gather statistics. The class path must be
 * modified in the Axis client configuration file "client-config.wsdd" to point
 * to this class instead.
 * <p>
 * The default Axis client configuration file is located in this area:
 * <pre>
 * {@literal org.apache.axis.client}
 * </pre>
 * You can copy this file and add it as a resource bundled within your own JAR
 * with the following change (to point to this class):
 * <pre>
 * {@literal OLD: <transport name="http" pivot="java:org.apache.axis.transport.http.HTTPSender"/>}
 * {@literal NEW: <transport name="http" pivot="your-path-here.CustomHttpSender"/>}
 * </pre>
 * Finally, you must point Axis to your client configuration file early, during
 * application startup. See the <code>install</code> static method below.
 *
 * @author mark.l.puzzo
 */
public class CustomHttpSender extends HTTPSender {
    private static Logger logger = LogManager.getLogger(CustomHttpSender.class);

    protected static final long serialVersionUID = 5864602359526312509L;
    protected static final SocketFactory socketFactory = new DefaultSocketFactory(getSocketFactoryProperties());
    protected static final int connectTimeout = 5000; // milliseconds, TODO - load from configuration/properties
    protected static final int socketTimeout = 60*60*1000; // milliseconds, TODO - load from configuration/properties

    public static void install() {
        AxisProperties.setProperty(EngineConfigurationFactoryDefault.OPTION_CLIENT_CONFIG_FILE,
                // TODO - modify the path to your own client configuration file
                "your-path-here/client-config.wsdd");
    }

    public CustomHttpSender() {
        super();
    }

    public void invoke(MessageContext msgContext) throws AxisFault {
        // TODO - add anything additional here (e.g., log all calls, monitor
        // timeouts and failures, etc.)
        try {
            super.invoke(msgContext);
        } catch (AxisFault fault) {
            // TODO - custom processing and/or logging
            throw (fault);
        }
    }

    protected void getSocket(SocketHolder sockHolder,
                             MessageContext msgContext, String protocol, String host, int port,
                             int timeout, StringBuffer otherHeaders, BooleanHolder useFullURL) throws Exception {
        logger.info(timeout);
        // Use our socket factory to create the socket (with timeout).
        Socket socket = socketFactory.create(host, port, otherHeaders, useFullURL);
        // Set the I/O timeout for the socket (optionally use the timeout value passed).
        socket.setSoTimeout(socketTimeout);
        // Copy the socket into the holder and return.
        sockHolder.setSocket(socket);
    }

    protected static Properties getSocketFactoryProperties() {
        Properties props = new Properties();
        props.setProperty(DefaultSocketFactory.CONNECT_TIMEOUT, Integer.toString(connectTimeout));
        return (props);
    }
}