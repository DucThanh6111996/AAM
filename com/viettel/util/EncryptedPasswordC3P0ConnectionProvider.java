package com.viettel.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.c3p0.internal.C3P0ConnectionProvider;
import org.hibernate.cfg.AvailableSettings;

import java.util.Map;

/**
 * @author quanns2
 */
public final class EncryptedPasswordC3P0ConnectionProvider extends C3P0ConnectionProvider {
    private static Logger logger = LogManager.getLogger(EncryptedPasswordC3P0ConnectionProvider.class);
    private static final long serialVersionUID = 5273353009914873806L;

    public EncryptedPasswordC3P0ConnectionProvider() {
        super();
    }

    public void configure(Map props) {
        final String password = (String) props.get(AvailableSettings.PASS);

        try {
            String pwd = PasswordEncoder.decrypt(password);
            props.put(AvailableSettings.PASS, pwd);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        super.configure(props);
    }
}
