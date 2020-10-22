/*
 * Created on Jun 7, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.it.util;

import com.viettel.it.model.*;
import com.viettel.it.object.CmdObject;
import com.viettel.it.object.SidnOpenBlockingObj;
import com.viettel.it.persistence.ActionServiceImpl;
import com.viettel.it.persistence.ItNodeActionServiceImpl;
import com.viettel.it.persistence.NodeAccountServiceImpl;
import com.viettel.util.Constant;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.model.UploadedFile;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.model.SelectItem;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.sql.*;
import java.util.*;


/**
 * 
 * Class chua cac ham tien ich dung chung cua ca project
 * 
 * @author Nguyen Hai Ha (hanh45@viettel.com.vn)
 * @since Jun 7, 2013
 * @version 1.0.0
 * 
 */
public class Util {
	private static Logger logger = LogManager.getLogger(Util.class);
	
	public static  ExternalContext externalContext;
	public static  File TOMCAT_DIR;
	public static  File TEMP_DIR;
	public static  File RESOURCES_DIR;

	static{
		try {
			setExternalContext(FacesContext.getCurrentInstance().getExternalContext());
			setTOMCAT_DIR(new File(((ServletContext) getExternalContext().getContext())
					.getRealPath("")).getParentFile().getParentFile());	//...../tomcat
			setTEMP_DIR(new File(getTOMCAT_DIR().getPath() + File.separator + "temp")); //...../tomcat/temp
			getTEMP_DIR().mkdirs();
			setRESOURCES_DIR(new File(getTOMCAT_DIR().getPath() + File.separator + "temp")); //...../tomcat/temp
		} catch (Exception ex) {
			logger.error(ex.getMessage(), ex);
			setRESOURCES_DIR(new File(""));
		}
	}
	
	/**
	 * Lay gia tri ip cua client.
	 */
	public static String getClientIp() {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest req = (HttpServletRequest) context.getRequest();

		return req.getRemoteHost();
	}

	public static String getUploadFolder(String handleFolder) {
		String dir = RESOURCES_DIR + File.separator+ Config.ROOT_FOLDER_DATA+File.separator+handleFolder;
		new File(dir).mkdirs();
		return dir;
	}
	public static boolean storeFile(String handleFoder, UploadedFile fileUpload){
		File file = new File(getUploadFolder(handleFoder) + File.separator + fileUpload.getFileName());
		FileOutputStream out = null;
		try {
			out = new FileOutputStream(file);
			out.write(fileUpload.getContents());
			return true;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}finally{
			if (out != null) {
				try {
					out.close();
				} catch (IOException e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return false;
	}
	public static String normalizeParamCode(String paramCode){
		if(paramCode!=null){
			return paramCode.replace("(", "").replace(")", "").replace("[", "").replace("]", ""); 
		}
		return null;
	}
	
	public List<SelectItem> getChecklistName() throws Exception {
    	List<SelectItem> checklistParams = new ArrayList<>();
    	try {
    		checklistParams = new ArrayList<>();
    		
//    		ChecklistWebserviceProxy webservice = new ChecklistWebserviceProxy();
//			AuthorityBO authorId = new AuthorityBO();
//			authorId.setPassword(Config.PASS_WS_CHECKLIST);
//			authorId.setUserName(Config.USER_WS_CHECKLIST);
//
//			RequestInputBO request = new RequestInputBO();
//			request.setCode("CFG_CHECKLIST");
//			ParameterBO[] params = new ParameterBO[1];
//			params[0] = new ParameterBO();
//			params[0].setName("type");
//			params[0].setType("STRING");
//			params[0].setValue("DB");
//
//			request.setParams(params);
//			JsonResponseBO jsonData = webservice.getDataJson(authorId, request);
			
//			JSONParser pa = new JSONParser();
//            JSONObject objRes = (JSONObject) pa.parse(jsonData.getDataJson());
//            JSONArray arrayChecklist = (JSONArray) objRes.get(Config.DATA_FIELD_NAME);
//
//            JSONObject objChecklist;
//            for (int i = 0; i < arrayChecklist.size(); i++) {
//            	objChecklist = (JSONObject) arrayChecklist.get(i);
//            	if (objChecklist.get("checklist_name") != null
//            			&& objChecklist.get("checklist_code") != null) {
//            		checklistParams.add(new SelectItem((String)objChecklist.get("checklist_code"), (String)objChecklist.get("checklist_name")));
//            	}
//            }
		} catch (Exception e) {
			throw e;
		}
    	return checklistParams;
    }

	public Connection getConnection(String url, String username, String password) {
		try {
			Class.forName("oracle.jdbc.driver.OracleDriver");
			Connection conn = DriverManager.getConnection(url, username, password);
			return conn;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			logger.error("Error", e);
			return null;
		}
	}

	public NodeAccount getAccount(String accountName, Long serverId,
                                  Long accountType, boolean isAnyCase, Long impactOrMonitorType) {
		NodeAccount account ;
		Map<String, Object> filters = new HashMap<String, Object>();
		filters.put("active", Constant.status.active);
		if (accountName != null) {
			filters.put("username", accountName);
		}
		if (impactOrMonitorType != null) {
			filters.put("impactOrMonitor", impactOrMonitorType);
		}

		filters.put("serverId", serverId);
		filters.put("accountType", accountType);

//		filters.put("username", Config.DEFAULT_ACCOUNT_DB_MONITOR);
//		filters.put("serverId", databaseNode.getServerId());

		try {
			account = new NodeAccountServiceImpl().findListExac(filters, null).get(0);
			logger.info("account " + accountName + ": ok");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			account = null;
//			logger.error("Error get account : " + accountName + " === of node: " + databaseNode.getNodeIp());
		}
		// find upcase data
		if (isAnyCase && account == null) {
			try {
				if (accountName != null) {
					filters.put("username", accountName.toUpperCase());
				}
				account = new NodeAccountServiceImpl().findListExac(filters, null).get(0);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
				account = null;
				if (accountName != null) {
					logger.error("Error get account monitor default : " + accountName.toUpperCase());
				}
			}
		}
		return account;
	}

	/**
	 * Ham lay danh sach thue bao can mo chan tin nhan
	 * @param sql
	 * @param connection
	 * @return
	 */
	public static List<SidnOpenBlockingObj> getSidnOpenBlocking(String sql, Connection connection) {
		List<SidnOpenBlockingObj> sidnDatas = new ArrayList<>();
		PreparedStatement pst = null;
		ResultSet rs = null;
		try {
			pst = connection.prepareStatement(sql);
			rs = pst.executeQuery();
			if (rs.getMetaData().getColumnCount() >= 1) {
				int maxSize = rs.getMetaData().getColumnCount();
				ResultSetMetaData rsmd = rs.getMetaData();
//				String rowVal = "";

				// get list header column
				List<String> columnNames = new LinkedList<>();
				for (int i = 1; i <= maxSize; i++) {
					columnNames.add(rsmd.getColumnName(i));
				}
				Map<String, String> paramVals;
				while (rs.next()) {
					try {
						SidnOpenBlockingObj obj = new SidnOpenBlockingObj();
						obj.setId(rs.getLong(Config.SIDN_OPEN_BLOCKING_ID));
						obj.setGroupcode(rs.getString(Config.GROUP_CODE_NAME));
						paramVals = new HashedMap();
						for (int i = 1; i <= maxSize; i++) {
							paramVals.put(columnNames.get(i - 1), rs.getString(i));
						}
						obj.setParamVals(paramVals);
						sidnDatas.add(obj);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (pst != null) {
				try {
					pst.close();
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			}
		}
		return  sidnDatas;
	}

	public static Map<String, Action> getMapActionGroup() {
		Map<String, Action> actionGroups = new HashedMap();
		try {
			List<Action> actions = new ActionServiceImpl().findList();
			if (actions != null) {
				for (Action action : actions) {
					if (action.getOpenBlockGroup() != null && !action.getOpenBlockGroup().trim().isEmpty()) {
						actionGroups.put(action.getOpenBlockGroup(), action);
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return actionGroups;
	}

	public static List<CmdObject> buildCmdObj(List<CommandDetail> commandDetails,
									   Map<String, String> mapParamValue, boolean isCmdLog,
									   Map<String, List<String>> mapParamInport) {
		String cmdRun = null;
		List<CmdObject> cmds = new ArrayList<>();
		try {

			// build command
			if (mapParamInport == null) {
				for (CommandDetail cmd : commandDetails) {
					cmdRun = cmd.getCommandTelnetParser().getCmd();
					for (ParamInput param : cmd.getParamInputs()) {
						cmdRun = cmdRun.replace("@{" + param.getParamCode() + "}",
								((mapParamValue != null && mapParamValue.get(param.getParamCode()) != null) ? mapParamValue.get(param.getParamCode()) : ""));
					}

					CmdObject cmdObj = new CmdObject();
					cmdObj.setCommand(cmdRun);
					cmdObj.setCmdDetailId(cmd.getCommandDetailId());
					if (isCmdLog) {
						cmdObj.setWriteLogOrder(cmd.getCmdLogOrderRun());
					}
					cmds.add(cmdObj);

				} // end loop for command detail

			} else {

				// build command import file
				for (CommandDetail cmd : commandDetails) {

					Long cmdClone = getMaxCmdClone(cmd, mapParamInport);
					for (int i = 0; i < cmdClone; i++) {
						cmdRun = cmd.getCommandTelnetParser().getCmd();
						for (ParamInput param : cmd.getParamInputs()) {
							int idx = (mapParamInport.get(param.getParamCode()).size() > i) ? i : mapParamInport.get(param.getParamCode()).size() - 1;
							cmdRun = cmdRun.replace("@{" + param.getParamCode() + "}", mapParamInport.get(param.getParamCode()).get(idx).trim());
						}

						CmdObject cmdObj = new CmdObject();
						cmdObj.setCommand(cmdRun);
						cmdObj.setCmdDetailId(cmd.getCommandDetailId());
						if (isCmdLog) {
							cmdObj.setWriteLogOrder(cmd.getCmdLogOrderRun());
						}
						cmds.add(cmdObj);
					}
				}
			}
//            }
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return cmds;
	}

	private static Long getMaxCmdClone(CommandDetail cmd, Map<String, List<String>> mapParams) {
		long maxLeng = 0;
		List<ParamInput> paramInputs = cmd.getParamInputs();
		if (paramInputs != null && !paramInputs.isEmpty()) {
			for (ParamInput p : paramInputs) {
				if (mapParams.get(p.getParamCode()).size() > maxLeng) {
					maxLeng = mapParams.get(p.getParamCode()).size();
				}
			}
		}
		return maxLeng;
	}

	/*
    Lay ra thong tin node mang tac dong cua dau viec
     */
	public static List<ItNodeAction> getNodeAction(Long type, Action action, Long actionId) {
		try {
			Map<String, Object> filters = new HashedMap();
			filters.put("actionId", action == null ? actionId : action.getActionId());
			filters.put("type", type);

			List<ItNodeAction> nodeActions = new ItNodeActionServiceImpl().findList(filters);
			if (nodeActions != null && !nodeActions.isEmpty()) {
				return nodeActions;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	public static boolean isNullOrEmpty(String str) {
		return str == null || str.trim().isEmpty();
	}
//    public static void main(String args[]) {
//		try {
//			List<SelectItem> lstData = new Util().getChecklistName();
//			for (SelectItem item : lstData) {
//				System.out.print(item.getValue() + "  _ " + item.getLabel() + "");
//			}
//		} catch (Exception e) {
//			e.printStackTrace();
//		}
//	}

	//20181119_tudn_start them danh sach lenh blacklist
	public static boolean checkValueOperator(String value, String compeValue, String comOper) {
		// value : gia tri nhap vao
		// compeValue: gia tri so sanh
		// comOper : toan tu so sanh
		boolean flag;
		String[] vals = null;
		if (value != null && value.length() > 0) {
			vals = value.split(";");
		}
		switch (comOper.toLowerCase()) {
			case "no check":
				return true;
			case ">":
				if (vals == null) {
					return false;
				}
				if (compeValue == null) {
					return false;
				}
				for (String val : vals) {
					val = val.trim();
					if (!NumberUtils.isNumber(val)) {
						return false;
					}
					if (NumberUtils.isNumber(val) && NumberUtils.isNumber(compeValue)) {
						flag = Double.parseDouble(val) > Double.parseDouble(compeValue);
					} else {
						flag = val.compareTo(compeValue) > 0;
					}
					if (!flag) {
						return false;
					}
				}
				return true;
			case ">=":
				if (vals == null) {
					return false;
				}
				if (compeValue == null) {
					return false;
				}
				for (String val : vals) {
					val = val.trim();
					if (!NumberUtils.isNumber(val)) {
						return false;
					}
					if (NumberUtils.isNumber(val) && NumberUtils.isNumber(compeValue)) {
						flag = Double.parseDouble(val) >= Double.parseDouble(compeValue);
					} else {
						flag = val.compareTo(compeValue) >= 0;
					}
					if (!flag) {
						return false;
					}
				}
				return true;
			case "<":
				if (vals == null) {
					return false;
				}
				if (compeValue == null) {
					return false;
				}
				for (String val : vals) {
					val = val.trim();
					if (!NumberUtils.isNumber(val)) {
						return false;
					}
					if (NumberUtils.isNumber(val) && NumberUtils.isNumber(compeValue)) {
						flag = Double.parseDouble(val) < Double.parseDouble(compeValue);
					} else {
						flag = val.compareTo(compeValue) < 0;
					}
					if (!flag) {
						return false;
					}
				}
				return true;
			case "<=":
				if (vals == null) {
					return false;
				}
				if (compeValue == null) {
					return false;
				}
				for (String val : vals) {
					val = val.trim();
					if (!NumberUtils.isNumber(val)) {
						return false;
					}
					if (NumberUtils.isNumber(val) && NumberUtils.isNumber(compeValue)) {
						flag = Double.parseDouble(val) <= Double.parseDouble(compeValue);
					} else {
						flag = val.compareTo(compeValue) <= 0;
					}
					if (!flag) {
						return false;
					}
				}
				return true;
			case "=":
				if (vals == null) {
					return false;
				}
				if (compeValue == null) {
					return false;
				}
				for (String val : vals) {
					val = val.trim();
					if (!val.equals(compeValue)) {
						return false;
					}
				}
				return true;
			case "<>":
				if (vals == null) {
					return false;
				}
				for (String val : vals) {
					val = val.trim();
					if (val.equals(compeValue)) {
						return false;
					}
				}
				return true;
			case "is null":
				return isNullOrEmpty(value);
			case "not null":
				return !isNullOrEmpty(value);
			case "in":
				if (vals == null || compeValue == null) {
					return false;
				}
				String[] strArr = compeValue.split(",");
				for (String str : strArr) {
					if (value.equals(str)) {
						return true;
					}
				}
				break;
			case "between":
				if (vals == null || compeValue == null) {
					return false;
				}
				String[] strArr2 = compeValue.split(",");

				if (strArr2 != null && strArr2.length > 1) {
					String below = strArr2[0];
					String behind = strArr2[1];

					if (NumberUtils.isNumber(value) && NumberUtils.isNumber(below)
							&& NumberUtils.isNumber(behind)) {
						return Double.parseDouble(value) <= Double.parseDouble(behind)
								&& Double.parseDouble(value) >= Double.parseDouble(below);
					} else {
						return value.compareTo(behind) <= 0 && value.compareTo(below) >= 0;
					}
				}
				break;
			case "is null or contain":
				if (isNullOrEmpty(value)) {
					return true;
				} else {
					if (vals != null && compeValue != null) {
						String[] strArr1 = compeValue.split(",");
						for (String str : strArr1) {
							for (String val : vals) {
								val = val.trim();
								if (val != null && val.contains(str)) {
									return true;
								}
							}
						}
						return false;
					} else {
						return false;
					}
				}
			case "contain":
				if (vals != null && compeValue != null) {
					String[] strArr1 = compeValue.split(",");
					for (String str : strArr1) {
						for (String val : vals) {
							val = val.trim();
							if (val != null && val.contains(str)) {
								return true;
							}
						}
					}
					return false;
				} else {
					return false;
				}
			case "contain all":
				if (vals != null && compeValue != null) {
					String[] strArr1 = compeValue.split(",");
					int i = 0;
					for (String str : strArr1) {
						for (String val : vals) {
							val = val.trim();
							if (val != null && val.contains(str)) {
								i++;
								break;
							}
						}
					}
					if (i == strArr1.length) {
						return true;
					} else {
						return false;
					}
				} else {
					return false;
				}
			case "not contain":
				if (vals != null && compeValue != null) {
					String[] strArr1 = compeValue.split(",");
					for (String str : strArr1) {
						for (String val : vals) {
							val = val.trim();
							if (val == null || val.contains(str)) {
								return false;
							}
						}
					}
					return true;
				} else {
					return false;
				}
			case "like":
				if (value == null) {
					return false;
				}
				return like(value, compeValue);
			case "not like":
				if (value == null) {
					return false;
				}
				return !like(value, compeValue);
			case "not in":
				if (vals == null || compeValue == null) {
					return false;
				}
				String[] strArr1 = compeValue.split(",");
				for (String str : strArr1) {
					if (value.equals(str)) {
						return false;
					}
				}
				return true;
		}

		return false;
	}

	private static boolean like(String str, String expr) {
		expr = expr.toLowerCase(); // ignoring locale for now
		expr = expr.replace(".", "\\."); // "\\" is escaped to "\"
		// ... escape any other potentially problematic characters here
		expr = expr.replace("?", ".");
		expr = expr.replace("%", ".*");
		str = str.toLowerCase();
		return str.matches(expr);
	}

	public static boolean isNullOrEmpty(Object o) {
		if (o == null) {
			return true;
		} else if (o instanceof CharSequence) {
			return ((CharSequence) o).length() == 0;
		} else if (o instanceof Collection) {
			return ((Collection) o).isEmpty();
		} else if (o instanceof Map) {
			return ((Map) o).isEmpty();
		} else if (o.getClass().isArray()) {
			return java.lang.reflect.Array.getLength(o) == 0;
		}
		return false;
	}
	public static String checkAndPrintObject(Object value) {
		try {
			if (isNullOrEmpty(value)) {
				return "null";
			} else {
				return value.toString();
			}
		} catch (Exception ex) {
			return "null";
		}
	}
	public static void checkAndPrintObject(Logger logger1, String title, Object... object) {
		try {
			StringBuilder stringBuilder = new StringBuilder();
			if (object != null) {
				stringBuilder.append("=====").append(title).append("(");
				for (int i = 0; i < object.length; i++) {
					if (i % 2 == 0) {
						stringBuilder.append(checkAndPrintObject(object[i]));
						stringBuilder.append(":");
					} else {
						stringBuilder.append(checkAndPrintObject(object[i]));
						stringBuilder.append(", ");
					}
				}
				stringBuilder.append(")=====");
			}
//            logger1.info("Vao khong");
			logger1.info(stringBuilder.toString());
		} catch (Exception e) {
			logger1.error("Du lieu dau vao khong dung");
		}
	}

	public static void main(String[] args) {
//        System.out.println(checkValueRegex("echo <b style='background: #7FFF00'>111</b>", "(^aDelete)"));
	}
	//20181119_tudn_end them danh sach lenh blacklist

	public static ExternalContext getExternalContext() {
		return externalContext;
	}

	public static void setExternalContext(ExternalContext externalContext) {
		Util.externalContext = externalContext;
	}

	public static File getTOMCAT_DIR() {
		return TOMCAT_DIR;
	}

	public static void setTOMCAT_DIR(File TOMCAT_DIR) {
		Util.TOMCAT_DIR = TOMCAT_DIR;
	}

	public static File getTEMP_DIR() {
		return TEMP_DIR;
	}

	public static void setTEMP_DIR(File TEMP_DIR) {
		Util.TEMP_DIR = TEMP_DIR;
	}

	public static File getRESOURCES_DIR() {
		return RESOURCES_DIR;
	}

	public static void setRESOURCES_DIR(File RESOURCES_DIR) {
		Util.RESOURCES_DIR = RESOURCES_DIR;
	}
}
