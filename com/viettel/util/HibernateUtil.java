/*
 * Created on Jun 7, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.util;

import com.viettel.bean.ResultGetAccount;
import com.viettel.it.util.SecurityService;
import com.viettel.passprotector.PassProtector;
import org.apache.commons.io.IOUtils;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.classloading.internal.ClassLoaderServiceImpl;
import org.hibernate.boot.registry.classloading.spi.ClassLoaderService;
import org.hibernate.cfg.Configuration;
import org.hibernate.metadata.ClassMetadata;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Hibernate Utility class with a convenient method to get Session Factory
 * object.
 * 
 * @author Nguyen Hai Ha (hanh45@viettel.com.vn)
 * @since Jun 7, 2013
 * @version 1.0.0
 * 
 */
public class HibernateUtil {
	//20181023_tudn_start load pass security
	private static final Logger logger = LoggerFactory.getLogger(HibernateUtil.class);
	private static Map<String,SessionFactory> sessionFactorys = new HashMap<String, SessionFactory>();
	private static SessionFactory buildSessionFactory(String resource) {
		try {
			if (sessionFactorys.get(resource) == null) {
				ClassLoaderService classLoaderService = new ClassLoaderServiceImpl();
				URL locateResource = classLoaderService.locateResource(resource);
				if (locateResource == null) {
					locateResource = classLoaderService.locateResource(resource);
				}
				String resourceFolder;
				InputStream inputStream;
				if (locateResource != null) {
					resourceFolder = new File(locateResource.toURI()).getParent();
					inputStream = classLoaderService.locateResourceStream(resource);
				} else {
					resourceFolder = "";
					logger.info(new File(resource).getAbsolutePath());
					if (new File(resource).exists()) {
						inputStream = new FileInputStream(resource);
					} else {
						inputStream = null;
					}
				}
//                logger.info(resourceFolder);
				//Đọc file cấu hình full

				if (inputStream != null) {

					String contentFile;
					try (BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream))) {
						contentFile = (IOUtils.toString(reader));
					}

					Configuration configure;
					if (locateResource != null) {
						configure = new Configuration().configure(resource);
					} else {
						configure = new Configuration().configure(new File(resourceFolder + resource).toURI().toURL());
					}

					//Lay thong tin da ma hoa
					String _username = configure.getProperty("hibernate.connection.username");
					// 20181024_thenv_Get pass from Security_start
					String _password = configure.getProperty("hibernate.connection.password");
//					try {
//						_password = com.viettel.util.PasswordEncoder.decrypt(_password);
//					} catch (Exception e) {
//						logger.error(e.getMessage());
//					}
					String _ip = "";
					if(configure.getProperty("hibernate.connection.url") != null) {
						String IPADDRESS_PATTERN = "(?:(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(?:25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)";
						Pattern pattern = Pattern.compile(IPADDRESS_PATTERN);
						Matcher matcher = pattern.matcher(configure.getProperty("hibernate.connection.url"));

						if (matcher.find()) {
							if (matcher.group(0) != null && !"".equals(matcher.group(0))) {
								_ip = matcher.group(0);
							}
						}
					}
//					ResultGetAccount resultGetAccount = SecurityService.getPassSecurity(_ip, "system",_username,
//							AamConstants.SECURITY_DATABASE, null, null, null, null, _password,null);
//					if (!resultGetAccount.getResultStatus() && isNullOrEmpty(resultGetAccount.getResult())) {
//						throw new Exception(resultGetAccount.getResultMessage());
//					}
//					_password = com.viettel.util.PasswordEncoder.encrypt(resultGetAccount.getResult());
					// 20181024_thenv_Get pass from Security_end

					String _url = configure.getProperty("hibernate.connection.url");

					if (_username != null && !_username.isEmpty()) {
						configure.setProperty("hibernate.connection.username", _username);
					}
					if (_password != null && !_password.isEmpty()) {
						configure.setProperty("hibernate.connection.password", _password);
					}

					if (_url != null && !_url.isEmpty()) {
						configure.setProperty("hibernate.connection.url", _url);
					}

					//Neu thay doi pass
					_username = configure.getProperty("hibernate.connection.username.new");
					// 20181024_thenv_Get pass from Security_start
					_password = configure.getProperty("hibernate.connection.password.new");
//					if(!isNullOrEmpty(_username)){
//						resultGetAccount = SecurityService.getPassSecurity(_ip, "system", _username,
//								AamConstants.SECURITY_DATABASE, null, null, null, null, _password,null);
//						_password = com.viettel.util.PasswordEncoder.encrypt(resultGetAccount.getResult());
//					}
					// 20181024_thenv_Get pass from Security_end

					_url = configure.getProperty("hibernate.connection.url.new");
					if (_username != null && !_username.isEmpty()) {
						configure.setProperty("hibernate.connection.username", _username);
						configure.setProperty("hibernate.connection.username.new", "");
						contentFile = contentFile.replaceAll("<property name=\"hibernate.connection.username\">(.*)</property>",
								"<property name=\"hibernate.connection.username\">" +_username + "</property>")
								.replaceAll("<property name=\"hibernate.connection.username.new\">(.*)</property>",
										"<property name=\"hibernate.connection.username.new\"></property>");
					}
					if (_password != null && !_password.isEmpty()) {
						configure.setProperty("hibernate.connection.password", _password);
						configure.setProperty("hibernate.connection.password.new", "");
						contentFile = contentFile.replaceAll("<property name=\"hibernate.connection.password\">(.*)</property>",
								"<property name=\"hibernate.connection.password\">" + com.viettel.util.PasswordEncoder.encrypt(_password) + "</property>")
								.replaceAll("<property name=\"hibernate.connection.password.new\">(.*)</property>",
										"<property name=\"hibernate.connection.password.new\"></property>");
					}
					if (_url != null && !_url.isEmpty()) {
						configure.setProperty("hibernate.connection.url", _url);
						configure.setProperty("hibernate.connection.url.new", "");
						contentFile = contentFile.replaceAll("<property name=\"hibernate.connection.url\">(.*)</property>",
								"<property name=\"hibernate.connection.url\">" + _url + "</property>")
								.replaceAll("<property name=\"hibernate.connection.url.new\">(.*)</property>",
										"<property name=\"hibernate.connection.url.new\"></property>");
					}

					try (PrintWriter printWriter = new PrintWriter(resourceFolder + resource)) {
						printWriter.println(contentFile);
					}
					sessionFactorys.put(resource, configure.buildSessionFactory());

//                    System.err.println(contentFileNoEncrypt.toString());
					//Mã hóa nội dung và ghi de lai file
				} else {
					throw new FileNotFoundException(resource + " not found!");
				}

			}
			return sessionFactorys.get(resource);
		} catch (Throwable ex) {
			logger.error("Initial SessionFactory creation failed." + ex);
			logger.error(ex.getMessage(), ex);
			throw new ExceptionInInitializerError(ex);
		}
	}
	//20181023_tudn_end load pass security

//	public static SessionFactory getSessionFactory() {
//		return buildSessionFactory("/hibernate.cfg.xml");
//	}
//	/**
//	 * @param resource: "/hibernate.cfg.xml";
//	 * @return
//	 * @author huynx6
//	 *
//	 */
//	public static SessionFactory getSessionFactory(String resource) {
//		if(resource==null)
//			return getSessionFactory();
//		return buildSessionFactory(resource);
//	}
//	public static Session openSession() {
//		return getSessionFactory().openSession();
//	}
//	public static Session getCurrentSession() {
//		return getSessionFactory().getCurrentSession();
//	}
//
//	public static Session openSession(String resource) {
//		return getSessionFactory(resource).openSession();
//	}
//
//	public static ClassMetadata getClassMetadata(Class class1) {
//		return getSessionFactory().getClassMetadata(class1);
//	}
//	public static void shutdown() {
//		getSessionFactory().close();
//	}
//	public static void main(String[] args) {
//
//	}
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}
	private static final SessionFactory sessionFactory = buildSessionFactory("hibernate.cfg.xml");

//	private static SessionFactory buildSessionFactory() {
//		try {
//			// Create the SshSessionFactory from hibernate.cfg.xml
//			return new Configuration().configure().buildSessionFactory();
//		} catch (Throwable ex) {
//			// Make sure you log the exception, as it might be swallowed
//			logger.debug("Loi trong qua trinh khoi tao SshSessionFactory: {}", ex.getMessage());
//			throw new ExceptionInInitializerError(ex);
//		}
//	}

	public static SessionFactory getSessionFactory() {
		return sessionFactory;
	}

	public static Session openSession() {
		return sessionFactory.openSession();
	}

	public static void shutdown() {
		// Close caches and connection pools
		getSessionFactory().close();
	}
}
