package com.viettel.it.webservice.utils;

import org.apache.commons.io.IOUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.transform.BasicTransformerAdapter;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Reader;
import java.sql.Clob;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;

public class MyResultTransformer extends BasicTransformerAdapter {
    private static Logger logger = LogManager.getLogger(MyResultTransformer.class);
    public final static MyResultTransformer INSTANCE;

    static {
        INSTANCE = new MyResultTransformer();
    }

    private MyResultTransformer() {

    }

    private static final long serialVersionUID = 1L;

//    @Override
//    public Object transformTuple(Object[] tuple, String[] aliases) {
//        Map<String, Object> map = new HashMap<String, Object>();
//        for (int i = 0; i < aliases.length; i++) {
//            Object t = tuple[i];
//            if (t != null && t instanceof Clob) {
//                Clob c = (Clob) tuple[i];
//                try {
//                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
//                    IOUtils.copy(c.getAsciiStream(), bos);
//                    t = new String(bos.toByteArray());
//                } catch (SQLException e) {
//                    e.printStackTrace();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//            map.put(aliases[i], t);
//        }
//        return map;
//    }

    @Override
    public Object transformTuple(Object[] tuple, String[] aliases) {
        Map<String, Object> map = new HashMap<String, Object>();
        for (int i = 0; i < aliases.length; i++) {
            Object t = tuple[i];
            if (t != null && t instanceof Clob) {
                Clob c = (Clob) tuple[i];
                try {
                    t = clobToString(c);
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
//                    LOG.error("Error", e);
                }
            }
            map.put(aliases[i], t);
        }
        return map;
    }

    private String clobToString(Clob data) {
        StringBuilder sb = new StringBuilder();
        try {
            Reader reader = data.getCharacterStream();
            BufferedReader br = new BufferedReader(reader);
            String line;
            while (null != (line = br.readLine())) {
                sb.append(line).append("\n");
            }
            br.close();
            reader.close();
        } catch (SQLException e) {
            logger.error(e.getMessage(), e);
//            LOG.error("Error", e);
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
//            LOG.error("Error", e);
        }
        return sb.toString();
    }

}
