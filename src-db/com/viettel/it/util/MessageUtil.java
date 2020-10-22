package com.viettel.it.util;

import com.viettel.resource.AppMessages;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.context.FacesContext;
import java.util.Locale;
import java.util.ResourceBundle;

@SuppressWarnings("serial")
public class MessageUtil {
	private static Logger logger = LogManager.getLogger(MessageUtil.class);
	private static ResourceBundle bundle;
	protected static Locale local;
	public static synchronized ResourceBundle getResourceBundle() {
		FacesContext context = FacesContext.getCurrentInstance();
		//if (bundle == null)
		{
			if (context != null)
				bundle = context.getApplication().getResourceBundle(context, "msg");
			else
				bundle = ResourceBundle.getBundle("com.viettel.resource.messages", local == null ? new Locale("vi","VN"): local) ;
//				bundle = ResourceBundle.getBundle("com.viettel.resource.messages", new Locale("en","US")) ;
		}
		return bundle;
	}
        
	public static synchronized void setResourceBundle() {
		FacesContext context = FacesContext.getCurrentInstance();
		bundle = context.getApplication()
					.getResourceBundle(context, "msg");
	}

	public static void setErrorMessage(String message) {
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_ERROR,
				"Error", message);

		FacesContext.getCurrentInstance().addMessage("mainMessage", msg);
	}

	public static void setInfoMessage(String message) {
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_INFO, "Info",
				message);

		FacesContext.getCurrentInstance().addMessage("mainMessage", msg);
	}

	public static void setWarnMessage(String message) {
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_WARN, "Warn",
				message);

		FacesContext.getCurrentInstance().addMessage("mainMessage", msg);
	}

	public static void setFatalMessage(String message) {
		FacesMessage msg = new FacesMessage(FacesMessage.SEVERITY_FATAL,
				"Fatal", message);

		FacesContext.getCurrentInstance().addMessage("mainMessage", msg);
	}
	
	public static synchronized ResourceBundle  getResourceBundle(String name) {
		FacesContext context = FacesContext.getCurrentInstance();
//		if (bundle == null) 
		{
			if(context==null)
				bundle = ResourceBundle.getBundle("com.viettel.resource.messages",local==null?new Locale("vi","VN"):local, new AppMessages.UTF8Control());
			else
				bundle = context.getApplication().getResourceBundle(context, name);
		}
		return bundle;
	}
	public static synchronized String getResourceBundleMessage(String key) {
		if(key==null)
			return key;
		if ("".equals(key)) {
			return "";
		}
		try {
			bundle = getResourceBundle();
			return bundle.getString(key);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return key;
	}
	public static void setInfoMessageFromRes(String key) {
		setInfoMessage(getResourceBundleMessage(key));
	}
	public static void setErrorMessageFromRes(String key) {
		setErrorMessage(getResourceBundleMessage(key));
	}
	public static void setWarnMessageFromRes(String key) {
		setWarnMessage(getResourceBundleMessage(key));
	}
        
    public static String getResourceBundleConfig(String key) {
        if (key == null) {
            return key;
        }
        if ("".equals(key)) {
            return "";
        }
        try {
            ResourceBundle configBundle = ResourceBundle.getBundle("config");
            return configBundle.getString(key);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return key;
    }

	public static Locale getLocal() {
		return local;
	}

	public static void setLocal(Locale local) {
		MessageUtil.local = local;
	}
}
