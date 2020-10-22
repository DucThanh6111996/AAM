package com.viettel.it.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;
import java.security.*;

/**
 * Created by hienhv4 on 6/2/2017.
 */
public class EvalUtil {

    private String value;
    private final Logger logger = LoggerFactory.getLogger(getClass());

    private static class RestrictedAccessControlContext {

        private static final AccessControlContext INSTANCE;

        static {
            INSTANCE = new AccessControlContext(
                    new ProtectionDomain[]{
                        new ProtectionDomain(null, null) // No permissions
                    });
        }
    }

    public String evalScript(final String formula)
            throws ScriptException, PrivilegedActionException {
        ScriptEngineManager factory = new ScriptEngineManager();
        final ScriptEngine engine = factory.getEngineByName("JavaScript");
        // Restrict permission using the two-argument form of doPrivileged()
        try {
            AccessController.doPrivileged(
                    new PrivilegedExceptionAction<Object>() {

                        @Override
                        public Object run() throws ScriptException {
                            value = engine.eval(formula).toString();
                            return value;
                        }
                    },
                    // From nested class
                    RestrictedAccessControlContext.INSTANCE);

        } catch (PrivilegedActionException pae) {
            logger.error(pae.getMessage(), pae);
        }
        return value;
    }
    
    public static void main(String[] args) throws Exception {
        System.out.println(new EvalUtil().evalScript("\"D'1261\"!=\"\""));
    }
}
