/*
 * Created on Jun 7, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.viettel.vsa.token.ObjectToken;
import com.viettel.vsa.token.UserToken;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.*;

/**
 * Các hàm thao tác với session cơ bản của cả hệ thống.
 *
 * @author Nguyen Hai Ha (hanh45@viettel.com.vn)
 * @version 1.0.0
 * @since Jun 7, 2013
 */

public class SessionWrapper implements Serializable {
    private static final long serialVersionUID = -8318262775763386620L;
    public static final String _VSA_USER_TOKEN = "vsaUserToken";
    private static final String _VSA_USER_ID = "netID";
    protected static final Logger logger = LoggerFactory.getLogger(SessionWrapper.class);

    /**
     * Get current session cua he thong.
     *
     * @return current session
     */
    public static HttpSession getCurrentSession() {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(XMLGregorianCalendar.class, new XGCalConverter.Serializer())
                .registerTypeAdapter(XMLGregorianCalendar.class, new XGCalConverter.Deserializer()).create();
        HttpServletRequest request = (HttpServletRequest) FacesContext
                .getCurrentInstance().getExternalContext().getRequest();

        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("quytv7.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        UserToken userToken = gson.fromJson(bufferedReader, UserToken.class);

        HttpSession session = request.getSession();
        session.setAttribute(_VSA_USER_TOKEN, userToken);

        return session;
    }

    /**
     * Lay gia tri session attribute.
     *
     * @param attributeName
     * @return
     */
    public String getSessionAttribute(String attributeName) {
        return (String) getCurrentSession().getAttribute(attributeName);
    }

    /**
     * Lay thong tin cua user hien tai dang login.
     */
    public static String getCurrentUsername() {
        try {
            return ((UserToken) getCurrentSession().getAttribute(_VSA_USER_TOKEN)).getUserName();
        } catch (Exception ex) {
            logger.debug(ex.getMessage(), ex);
            return "system";
        }
    }

    public static String getStaffCode() {
        HttpSession session = getCurrentSession();
        String result = null;

        UserToken userToken = (UserToken) session.getAttribute(_VSA_USER_TOKEN);
        if (userToken != null) {
            result = userToken.getStaffCode();
        }

        return result;
    }

    public static String getFullName() {
        HttpSession session = getCurrentSession();
        String result = null;

        UserToken userToken = (UserToken) session.getAttribute(_VSA_USER_TOKEN);
        if (userToken != null) {
            result = userToken.getFullName();
        }

        return result;
    }

    /**
     * Kiem tra xem URL nay co duoc truy cap khong.
     *
     * @return
     */
    public boolean getUrlDisplay(String urlCode) {
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(XMLGregorianCalendar.class, new XGCalConverter.Serializer())
                .registerTypeAdapter(XMLGregorianCalendar.class, new XGCalConverter.Deserializer()).create();
        HttpSession session = getCurrentSession();
        boolean result = false;

		String objToken;
		UserToken userToken = (UserToken) session.getAttribute(_VSA_USER_TOKEN);

//        UserToken userToken = (UserToken) session.getAttribute(_VSA_USER_TOKEN);
//        try {
//            BufferedWriter writer = new BufferedWriter(new FileWriter("quytv7.txt"));
//            writer.write(gson.toJson(userToken));
//            writer.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }



        if (userToken != null) {
            for (ObjectToken ot : userToken.getObjectTokens()) {
                objToken = ot.getObjectUrl();
                if (objToken.equalsIgnoreCase(urlCode)) {
                    result = true;
                    break;
                }
            }
        }

        return result;
//		return true;
    }

    protected static Boolean getUrlByKey(String key) {
        HttpSession session = getCurrentSession();
        String url;
        UserToken userToken = (UserToken) session.getAttribute(_VSA_USER_TOKEN);
        if (userToken != null) {
            for (ObjectToken ot : userToken.getObjectTokens()) {
                url = ot.getObjectUrl();
                if (key.equalsIgnoreCase(url)) {
                    return true;
                } else {
//					url = "";
                }
            }
        } else {
            return false;
        }

        return false;
    }
}// End class
