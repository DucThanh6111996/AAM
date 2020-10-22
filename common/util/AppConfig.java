package com.viettel.util;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * @author quanns2
 */
public class AppConfig {
	private static Logger logger = LogManager.getLogger(AppConfig.class);
	private static AppConfig instance;

	private Properties props;
	
	private AppConfig() {
		props = new Properties();
	}

	public synchronized static AppConfig getInstance() {
		if (instance == null) {
			instance = new AppConfig();
			try {
				instance.loadConfiguration();
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
		
		return instance;
	}
	
	private void loadConfiguration() throws IOException {
		InputStream is = null;
		try {
			is = AppConfig.class.getResourceAsStream("/config.properties");
			props.load(is);
		} finally {
			try {
				if (is != null) {
					is.close();
				}
			} catch (IOException e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	public String getProperty(String key) {
		String val = props.getProperty(key);
		try {
			if(val != null) {
				return new String(val.getBytes("ISO-8859-1"), "UTF-8");
			}else{
				return null;
			}
		} catch (UnsupportedEncodingException e) {
			logger.error(e.getMessage(), e);
			return val;
		}
	}
}

