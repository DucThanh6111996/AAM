/*
 * Created on Jun 7, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.util;

import com.viettel.it.util.DateTimeUtils;
import com.viettel.it.util.MessageUtil;
import com.viettel.model.TimeZone;
import com.viettel.vsa.token.ObjectToken;
import com.viettel.vsa.token.RoleToken;
import com.viettel.vsa.token.UserToken;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.joda.time.DateTime;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.servlet.http.HttpSession;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.*;
import java.sql.Time;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.ZoneId;
import java.time.ZonedDateTime;

/**
* Dinh nghia cac ham thao tac voi session cua ca he thong.
*
* @author Nguyen Hai Ha (hanh45@viettel.com.vn)
* @since Jun 7, 2013
* @version 1.0.0
*/
@ManagedBean
@RequestScoped
public class SessionUtil extends SessionWrapper {
	private static Logger logger = LogManager.getLogger(SessionUtil.class);

	private static final long serialVersionUID = -7313741895804416337L;
	
	static String ACTION_ADMIN = "TDTT_HT_MOP_ACTION_ADMIN";
	static String ITBUSINESS_ADMIN="TDTT_ITBUSINESS_ADMIN";
	static String ITBUSINESS_USER_GROUP ="TDTT_ITBUSINESS_USER_GROUP";
	static String ITBUSINESS_USER_NORMAL = "TDTT_ITBUSINESS_USER_NORMAL";
	static String ACTION_TEMP = "TDTT_HT_MOP_ACTION_TEMP";
	static String ACTION_MOP = "TDTT_HT_MOP_ACTION_MOP";
	static String ACTION_COMMAND = "TDTT_HT_MOP_ACTION_COMMAND";
	static String ACTION_EXECUTE = "TDTT_HT_MOP_ACTION_EXECUTE";
	static String APPROVE_TEMP = "TDTT_HT_MOP_APPROVE_TEMP";
	static String APPROVE_COMMAND = "TDTT_HT_MOP_APPROVE_COMMAND";
	static String CREATE_MOP_SERVER = "TDTT_HT_CREATE_MOP_SERVER";
	
	static String EXECUTE_MOP = "TDTT_HT_EXECUTE_MOP";
	static String APPROVE_MOP = "TDTT_HT_APPROVE_MOP";
	static String CREATE_MOP = "TDTT_HT_CREATE_MOP";
	static String APPROVE_TEMP_COMPONENT = "TDTT_HT_APPROVE_TEMP";
	static String PRE_APPROVE_TEMP_COMPONENT = "TDTT_HT_PRE_APPROVE_TEMP";
	static String RESCUE_OPEN_BLOCK_SIDN = "TDTT-ITBUSINESS-RESCUE-OPEN-BLOCKING-SIDN";

	static String TDTT_Z78_APPROVE = "TDTT_Z78_APPROVE";

	/*20180713_hoangnd_them approve rollback_start*/
	static String APPROVE_ROLLBACK = "TDTT_HT_MOP_APPROVE_ROLLBACK";
	/*20180713_hoangnd_them approve rollback_end*/

	//20180828_tudn_start appprove template
	static String APPROVE_TEMP_EXEC_MANAGER = "TDTT_HT_MOP_APPROVE_TEMP_EXEC_MANAGER";
	//20180828_tudn_end appprove template
	static String CPTChangeconfig = "CPT_CHANGE_CONFIG";

	static {
		ACTION_ADMIN =  "TDTT_HT_MOP_ACTION_ADMIN";
		ACTION_TEMP = "TDTT_HT_MOP_ACTION_TEMP";
		ACTION_MOP = "TDTT_HT_MOP_ACTION_MOP";
		ACTION_COMMAND = "TDTT_HT_MOP_ACTION_COMMAND";
		ACTION_EXECUTE = "TDTT_HT_MOP_ACTION_EXECUTE";
		APPROVE_TEMP = "TDTT_HT_MOP_APPROVE_TEMP";
		APPROVE_COMMAND = "TDTT_HT_MOP_APPROVE_COMMAND";
		
		EXECUTE_MOP = "TDTT_HT_EXECUTE_MOP";
		APPROVE_MOP = "TDTT_HT_APPROVE_MOP";
		CREATE_MOP="TDTT_HT_CREATE_MOP";
		RESCUE_OPEN_BLOCK_SIDN="TDTT-ITBUSINESS-RESCUE-OPEN-BLOCKING-SIDN";
			
	}

	/**
	 * Lay gia tri menu default.
	 * 
	 */
	public static String getMenuDefault() {
		if (getUrlByKey("/action")){
			return "/action/config";
		} else if (getUrlByKey("/it-business")) {
			return "/it-business/excute";
		}  else if (getUrlByKey("/db-server")) {
			return "/db-server/execute";
		}
		// Nguoi dung khong co url nao trong he thong
		// Tra ve trang bao loi.
		return Config._ERROR_PAGE;
	}
	
	Map<String,RoleToken> mapRoleCode = new HashMap<>();
    Map<String,ObjectToken> mapComponentCode = new HashMap<>();
    
    public SessionUtil() {
		mapRoleCode.clear();
    	HttpSession session = getCurrentSession();
		UserToken userToken = (UserToken) session.getAttribute("vsaUserToken");
		if (userToken != null && userToken.getRolesList()!=null) {
			for (RoleToken roleToken : userToken.getRolesList()) {
				mapRoleCode.put(roleToken.getRoleCode(), roleToken);
			}
		}
		mapComponentCode.clear();
        if (userToken != null && userToken.getComponentList() != null) {
            for (ObjectToken component : userToken.getComponentList()) {
                mapComponentCode.put(component.getObjectCode(), component);
            }
        }
    }

	public static Double getDiffZone() {
		HttpSession session = getCurrentSession();
		TimeZone timeZone = (TimeZone) session.getAttribute("timeZone");

		if (timeZone == null)
			return 0D;
		else {
			return timeZone.getGmt() - 7;
		}
	}

	public static Date toTimeZoneDate(Date date) {

        /*20180705_hoangnd_check_date_null_start*/
        if(date != null) {
            DateTime dateTime = new DateTime(date);
            //tuanda38_20180619_start
            Double gmt = getDiffZone();
            dateTime = dateTime.plusMinutes((int) (gmt * 60));
            //tuanda38_20180619_end

            return dateTime.toDate();
        }
        return null;
        /*20180705_hoangnd_check_date_null_end*/
	}

	public static void setTimeZone(TimeZone timeZone) {
		HttpSession session = getCurrentSession();
		session.setAttribute("timeZone", timeZone);
	}

    /*20180703_hoangnd_thay_doi_gio_theo_time_zone_start*/
	public String changeDateByTimeZone(Date date) {

		if(date != null) {
			HttpSession session = getCurrentSession();
			TimeZone timeZone = (TimeZone) session.getAttribute("timeZone");
			if(timeZone == null) {
				timeZone = new TimeZone();
				timeZone.setZone("Asia/Ho_Chi_Minh");
			}
			SimpleDateFormat sdf = new SimpleDateFormat(DateTimeUtils.DATE_TIME_FORMAT);
			java.util.TimeZone tz = java.util.TimeZone.getTimeZone(timeZone.getZone());
			sdf.setTimeZone(tz);
			return sdf.format(date);
		} else {
			return null;
		}
	}
    /*20180703_hoangnd_thay_doi_gio_theo_time_zone_end*/
    
    public  boolean checkRole(String roleCode){
    	if(mapRoleCode.get(roleCode)!=null)
    		return true;
    	return false;
//    	return true;
    }
    
    public  boolean checkComponent(String componentCode){
    	if(mapComponentCode.get(componentCode)!=null)
    		return true;
    	return false;
//    	return true;
    }

    public  boolean isActionAdmin(){
    	return checkRole(ACTION_ADMIN);
    }
	public  boolean isActionTemp() {
		return checkRole(ACTION_TEMP);
	}
	public  boolean isActionMop() {
		return checkRole(ACTION_MOP) && !checkRole(ACTION_ADMIN);
	}
	public  boolean isActionExecute() {
		return checkRole(ACTION_EXECUTE) && !checkRole(ACTION_MOP) && !checkRole(ACTION_ADMIN);
	}
	public boolean isItBusinessAdmin(){return checkRole(ITBUSINESS_ADMIN);}
	public boolean isItBusinessUserGroup(){return checkRole(ITBUSINESS_USER_GROUP);}
	public boolean isItBusinessUserNormal(){return checkRole(ITBUSINESS_USER_NORMAL);}
	
    public boolean isOnlyViewCommand() {
        return checkRole(ACTION_MOP) && !checkRole(ACTION_ADMIN) && !checkRole(ACTION_COMMAND)
                && !checkRole(ACTION_EXECUTE) && !checkRole(ACTION_TEMP) 
                && !checkRole(APPROVE_COMMAND) && !checkRole(APPROVE_TEMP);
    }
	public boolean isCreateMop(){
		return checkComponent(CREATE_MOP) || checkComponent(APPROVE_MOP);
//		return true;
	}
	public boolean isApproveMop(){
		return checkComponent(APPROVE_MOP);
	}
	public boolean isExecute(){
		return checkComponent(EXECUTE_MOP);
	}

	public boolean isApproveTemplate() {
		return checkComponent(APPROVE_TEMP_COMPONENT) ||
				(checkRole(APPROVE_TEMP) || checkRole(ACTION_ADMIN)) ;
	}
	
	public boolean isPreApproveTemplate() {
		return checkComponent(PRE_APPROVE_TEMP_COMPONENT) && 
				(checkRole(APPROVE_TEMP) || checkRole(ACTION_ADMIN)) ;
	}
	public boolean isChangeConfig(){
		return checkComponent(CPTChangeconfig);
	}

    public boolean isShowNodeType() {
        try {
            return Boolean.parseBoolean(MessageUtil.getResourceBundleConfig("SHOW_NODE_TYPE"));
        } catch (Exception ex) {
        	logger.error(ex.getMessage(), ex);
		}
        return true;
    }
    public boolean isCreateCustomer(){
    	return isCreateMop() || checkRole("IP_MOP_CREATE_CUSTOMER");
    }
    public boolean isCreateEnodeB(){
    	return isCreateMop()|| checkRole("IP_MOP_CREATE_4NG");
    }
    
    public boolean isCreateMopServer() {
    	return checkComponent(CREATE_MOP_SERVER);
    }

	public boolean isRescueOpenBlockSidn(){
		return checkComponent(RESCUE_OPEN_BLOCK_SIDN);
//		return true;
	}

	/*20180713_hoangnd_them approve rollback_start*/
	public boolean isApproveRollback() {
		return checkComponent(APPROVE_ROLLBACK) || checkRole(ACTION_ADMIN);
	}
    /*20180713_hoangnd_them approve rollback_end*/

    /*20180822_hoangnd_check roll block rollback_start*/
	public boolean isBlockRollback() {
		return checkRole(TDTT_Z78_APPROVE) || checkRole(ACTION_ADMIN);
	}
    /*20180822_hoangnd_check roll block rollback_end*/

	//20180828_tudn_start appprove template
	public boolean isApproveTempExecMana() {
		return checkRole(APPROVE_TEMP_EXEC_MANAGER) || checkRole(ACTION_ADMIN);
	}

	public boolean isApproveTempNew() {
		return
				((checkComponent(APPROVE_TEMP_COMPONENT) && checkRole(APPROVE_TEMP)) || checkRole(APPROVE_TEMP_EXEC_MANAGER) || checkRole(ACTION_ADMIN)) ;
	}
	//20180828_tudn_end appprove template

}// End class
