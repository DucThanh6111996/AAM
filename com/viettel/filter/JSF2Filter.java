package com.viettel.filter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.viettel.util.XGCalConverter;
import com.viettel.vsa.token.ObjectToken;
import com.viettel.vsa.token.UserToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.ResourceHandler;
import javax.faces.context.FacesContext;
import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.xml.datatype.XMLGregorianCalendar;
import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.regex.Pattern;

/**
 * Filter chung cua ca he thong doi voi duong dan tuyet doi. Tat ca cac module
 * deu phai khai bao phan quyen o day.
 *
 * @author hanh45
 */
@WebFilter(dispatcherTypes = {DispatcherType.REQUEST, DispatcherType.FORWARD, DispatcherType.INCLUDE, DispatcherType.ERROR}, urlPatterns = {"/faces/*"})
public class JSF2Filter implements Filter {
    private static Logger logger = LogManager.getLogger(JSF2Filter.class);

    private String _REQUEST_PATH = "";
    private static final String _HOME_PATH = "/home";
    private static final String _ERROR_PATH = "/error";
    private static final String _OTP_PATH = "/otp";
    private static final String _XHTML = ".xhtml";
    private static final String _FACES = "/faces/";
    private static final String _NO_PERMISSION_PATH = "/permission";
    private static final String _VSA_USER_TOKEN = "vsaUserToken";

    public JSF2Filter() {
    }

    @Override
    public void init(FilterConfig fConfig) throws ServletException {
    }

    @Override
    public void destroy() {
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest req = (HttpServletRequest) request;
        HttpSession session = req.getSession();

        // -----------------------
        Gson gson = new GsonBuilder()
                .registerTypeAdapter(XMLGregorianCalendar.class, new XGCalConverter.Serializer())
                .registerTypeAdapter(XMLGregorianCalendar.class, new XGCalConverter.Deserializer()).create();
        BufferedReader bufferedReader = null;
        try {
            bufferedReader = new BufferedReader(new FileReader("quytv7.txt"));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        session.setAttribute(_VSA_USER_TOKEN, gson.fromJson(bufferedReader, UserToken.class));
        // -----------------------

        HttpServletResponse res = (HttpServletResponse) response;
        boolean checkAuth = false;

        // Skip JSF resources (CSS/JS/Images/etc)
        if (!req.getRequestURI().startsWith(req.getContextPath() + ResourceHandler.RESOURCE_IDENTIFIER)) {
            res.setHeader("Cache-Control", "no-cache, no-store, must-revalidate"); // HTTP 1.1.
            res.setHeader("Pragma", "no-cache"); // HTTP 1.0.
            res.setDateHeader("Expires", 0); // Proxies.
        }

        _REQUEST_PATH = req.getServletPath();
        int pos = _REQUEST_PATH.indexOf("/", _FACES.length());
        String SUB_REQUEST_PATH = _REQUEST_PATH.substring(0, pos);
        _REQUEST_PATH = _REQUEST_PATH.substring(0, _REQUEST_PATH.indexOf(_XHTML) + _XHTML.length());

        // Kiem tra session timeout.
        UserToken userToken = (UserToken) session.getAttribute(_VSA_USER_TOKEN);
        if (userToken != null) {
//			checkAuth = true;
            // Current session on.
            switch (SUB_REQUEST_PATH) {

                case "/faces/home":
                    // Neu la home thi cho qua.
                    checkAuth = true;
                    break;
                case "/faces/action":
                    checkAuth = getActionPermission(session, _REQUEST_PATH);
                    break;
                default:
                    checkAuth = getPermission(session, _REQUEST_PATH);
                    break;
            }
        } else {
            // Session timeout.
            // Xu ly cho ajax khi session timeout.
            handleSessionTimeout(req, res);
        }

        if (checkAuth) {
//			if (session.getAttribute("opt") == null) {
//				res.sendRedirect(req.getContextPath() + _OTP_PATH);
//			} else {
            chain.doFilter(request, response);
//			}
        } else
            // Dieu huong den trang bao loi.
            if (!res.isCommitted())
                res.sendRedirect(req.getContextPath() + _NO_PERMISSION_PATH);


    }

    private boolean getActionPermission(HttpSession session, String requestPath) {
        boolean checkAuth;

        switch (requestPath) {
            case "/faces/action/index.xhtml":
                checkAuth = getUrlPermission(session, "/action");
                break;
            case "/faces/action/config/index.xhtml":
                checkAuth = getUrlPermission(session, "/action/config");
                break;
            case "/faces/action/config/uctt.xhtml":
                checkAuth = getUrlPermission(session, "/action/uctt");
                break;
/*			case "/faces/action/uctt/index.xhtml":
				checkAuth = getUrlPermission(session, "/action/uctt");
				break;*/
            case "/faces/action/execute/index.xhtml":
                checkAuth = getUrlPermission(session, "/action/execute");
                break;
            case "/faces/action/monitor/index.xhtml":
                checkAuth = getUrlPermission(session, "/action/monitor");
//				checkAuth = true;
                break;
            case "/faces/action/service/index.xhtml":
                checkAuth = getUrlPermission(session, "/action/service");
                break;
            case "/faces/action/history/index.xhtml":
                checkAuth = getUrlPermission(session, "/action/history");
                break;
            case "/faces/action/guide/index.xhtml":
                checkAuth = getUrlPermission(session, "/action/guide");
                break;

            default:
                checkAuth = false;
                break;
        }

        return checkAuth;
    }

    /**
     * Xu ly session timeout.
     *
     * @throws IOException
     */
    private void handleSessionTimeout(HttpServletRequest req, HttpServletResponse res) throws IOException {
        if ("partial/ajax".equals(req.getHeader("Faces-Request"))) {
            res.setContentType("text/xml");
            res.getWriter().append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").printf(
                    "<partial-response><redirect url=\"%s\"></redirect></partial-response>",
                    req.getContextPath() + _HOME_PATH);
        } else {
            if (!res.isCommitted())
                res.sendRedirect(req.getContextPath() + _HOME_PATH);
        }
    }


    private boolean getPermission(HttpSession session, String requestPath) {
        // //System.out.println("vao getManagerPermisstion !!!!!!!!!!!!!!!!!!!!!!!!! ");
        // Logger.getLogger("huynx6_3").info(requestPath);
        boolean checkAuth = false;
        try {
            if (Pattern.compile("^" + _FACES + ".+?" + "/index.xhtml$").matcher(requestPath).find())
                checkAuth = getUrlPermission(session, requestPath.substring(_FACES.length() - 1, requestPath.indexOf("/index.xhtml")));
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
//        return checkAuth;
		return true;
    }

    /**
     * Ham kiem tra quyen cua user tren session
     *
     * @param urlCode
     * @return
     */
    private boolean getUrlPermission(HttpSession session, String urlCode) {
        boolean result = false;

        String objToken;
        UserToken userToken = (UserToken) session.getAttribute(_VSA_USER_TOKEN);
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
    }
}
