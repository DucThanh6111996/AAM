package com.viettel.it.util;

import com.viettel.bean.ResultGetAccount;
import com.viettel.passprotector.PassProtector;
import com.viettel.util.*;
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
 * @author Nguyễn Xuân Huy <huynx6@viettel.com.vn>
 * @sin Jul 29, 2016
 * @version 1.0 
 */
public class HibernateUtil {


	//20181023_tudn_start load pass security
	private static Map<String,SessionFactory> sessionFactorys = new HashMap<String, SessionFactory>();
	protected static final Logger logger = LoggerFactory.getLogger(com.viettel.util.HibernateUtil.class);

//	private static SessionFactory buildSessionFactory(String resource) {
//		try {
//			if(sessionFactorys.get(resource) ==null){
//				sessionFactorys.put(resource, new Configuration().configure(resource).buildSessionFactory());
//			}
//		    return sessionFactorys.get(resource);
//		} catch (Throwable ex) {
//			System.err.println("Initial SshSessionFactory creation failed." + ex);
//			throw new ExceptionInInitializerError(ex);
//		}
//	}

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
					_password = configure.getProperty("hibernate.connection.password.new");
					// 20181024_thenv_Get pass from Security_start
//					try {
//						_password = com.viettel.util.PasswordEncoder.decrypt(_password);
//					} catch (Exception e) {
//						logger.error(e.getMessage());
//					}
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

	public static SessionFactory getSessionFactory() {
		return buildSessionFactory("/hibernateIt.cfg.xml");
	}
	/**
	 * @param resource: "/hibernate.cfg.xml";
	 * @return
	 * @author huynx6
	 * 
	 */
	public static SessionFactory getSessionFactory(String resource) {
		if(resource==null)
			return getSessionFactory();
		return buildSessionFactory(resource);
	}
	public static Session openSession() {
		return getSessionFactory().openSession();
	}
	public static Session getCurrentSession() {
		return getSessionFactory().getCurrentSession();
	}

	public static Session openSession(String resource) {
		return getSessionFactory(resource).openSession();
	}

	public static ClassMetadata getClassMetadata(Class class1) {
		return getSessionFactory().getClassMetadata(class1);
	}
	public static void shutdown() {
		getSessionFactory().close();
	}
	public static void main(String[] args) {
	}
	public static boolean isNullOrEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}
}
