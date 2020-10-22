/**
 * 
 */
package com.viettel.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author huunv8
 * 
 */
public class TableUtil {
	/**
	 * static LinkedHashMap<String, LinkedHashMap<String, String>> buildNetworkNode(
	 
			List<String> columns, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			result = new LinkedHashMap<>();
			int row = 0;
			Map<String,String> listApp=new HashMap<String,String>();
			for (GdAppWithTomcatBO upcodeApp : appWithTomcatBOs) {
				if(listApp.get(upcodeApp.getAppCode())==null){
					listApp.put(upcodeApp.getAppCode(), upcodeApp.getNodeIp());
				}
				else{
				String temp="";
				if(!listApp.get(upcodeApp.getAppCode()).contains(upcodeApp.getNodeIp())){
					temp=listApp.get(upcodeApp.getAppCode());
					temp+=" \n"+upcodeApp.getNodeIp();
				listApp.put(upcodeApp.getAppCode(), temp);
				}
				}
			}
			for (GdAppWithoutTomcatBO obj : appWithoutTomcatBOs) {
				if(listApp.get(obj.getAppCode())==null){
					listApp.put(obj.getAppCode(), obj.getNodeIp());
				}
				else{
				String temp="";
				if(!listApp.get(obj.getAppCode()).contains(obj.getNodeIp())){
					temp=listApp.get(obj.getAppCode());
					temp+=" \n"+obj.getNodeIp();
				listApp.put(obj.getAppCode(), temp);
				}
				}
			}
			for (GdAppDatabaseBO obj : appDatabaseBOs) {
				if(listApp.get(obj.getAppCode())==null){
					listApp.put(obj.getAppCode(), obj.getNodeIp());
				}
				else{
				String temp="";
				if(!listApp.get(obj.getAppCode()).contains(obj.getNodeIp())){
					temp=listApp.get(obj.getAppCode());
					temp+=" \n"+obj.getNodeIp();
				listApp.put(obj.getAppCode(), temp);
				}
				}
			}
			for (Map.Entry<String, String> entry : listApp.entrySet()) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("TT", ""+row);
				rowData.put("Tên Hệ thống", entry.getKey());
				rowData.put("Node Mạng (Danh sách)", entry.getValue());
				result.put("row" + row, rowData);
			}
			if (row == 0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row+"");
				rowData.put("2", "Dánh sách rỗng");
				rowData.put("3", "Danh sách rỗng");
				result.put("row" + row, rowData);
			}

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	static LinkedHashMap<String, LinkedHashMap<String, String>> buildCheckStatusTomcat(
			List<String> columns, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			boolean flagStart = false;
			result = new LinkedHashMap<>();
			for (GdAppWithTomcatBO gdAppWithTomcatBO : appWithTomcatBOs) {
				flagStart = false;
				for (UpcodeApp up : upcodeApps) {
					if (up.getModuleCode().equals(
							gdAppWithTomcatBO.getModuleCode())) {						
							flagStart = checkLifeCircle(up.getLifeCircle(), "checkstatus") ;
						break;
					}
				}
				if(flagStart){
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", gdAppWithTomcatBO.getAppCode() + "\n"
						+ gdAppWithTomcatBO.getNodeIp() + "\n"
						+ gdAppWithTomcatBO.getUsername());
				rowData.put("3", gdAppWithTomcatBO.getModuleCode());
				String port="";
				if(gdAppWithTomcatBO.getWebPort()!=null && gdAppWithTomcatBO.getWebPort()!=0){
					port=gdAppWithTomcatBO.getWebPort().toString();
				}
				rowData.put("4", port);
				String temp=gdAppWithTomcatBO.getAppPath();
				temp=processPath(temp);
				rowData.put("5", "1. cd " +temp
						+ "\n 2. " + gdAppWithTomcatBO.getCheckStatus());
				rowData.put(
						"6",
						"Kiểm tra trạng thái ứng dụng trước tác động, Có process của ứng dụng đang hoạt động");
				rowData.put("7","0.5");
				rowData.put("8","");
				rowData.put("9","");
				result.put("row" + row, rowData);
				}
			}
			for (GdAppWithoutTomcatBO obj : appWithoutTomcatBOs) {
				flagStart = false;
				for (UpcodeApp up : upcodeApps) {
					if (up.getModuleCode().equals(
							obj.getModuleCode())) {						
							flagStart = checkLifeCircle(up.getLifeCircle(), "checkstatus") ;
						break;
					}
				}
				if(flagStart){
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", obj.getAppCode() + "\n" + obj.getNodeIp()
						+ "\n" + obj.getUsername());
				rowData.put("3", obj.getModuleCode());
				String port="";
				if(obj.getMmPort()!=null && obj.getMmPort()!=0){
					port=obj.getMmPort().toString();
				}
				rowData.put("4", port);
				String temp=obj.getAppPath();		
				temp=processPath(temp);
				rowData.put(
						"5",
						"1. cd " + temp + "\n 2. "
								+ obj.getCheckStatus());
				rowData.put(
						"6",
						"1. cd đến thư mục ứng dụng \r\n 2. Kiểm tra trạng thái tiến trình đang hoạt động: is running");
				rowData.put("7","0.5");
				rowData.put("8","");
				rowData.put("9","");
				result.put("row" + row, rowData);
				}
			}
			if (row == 0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");
				rowData.put("7","");
				
				rowData.put("8","");
				rowData.put("9","");
				result.put("row" + row, rowData);
			}

		} catch (Exception e) {

		}

		return result;
	}

	static LinkedHashMap<String, LinkedHashMap<String, String>> buildStopAppTomcat(
			List<String> columns, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			boolean flagStart = false;
			for (GdAppWithTomcatBO gdAppWithTomcatBO : appWithTomcatBOs) {
				flagStart = false;
				for (UpcodeApp up : upcodeApps) {
					if (up.getModuleCode().equals(
							gdAppWithTomcatBO.getModuleCode())) {						
							flagStart = checkLifeCircle(up.getLifeCircle(), "stop") ;
						break;
					}
				}
				if (flagStart) {
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					rowData.put("1", row + "");
					rowData.put("2", gdAppWithTomcatBO.getAppCode() + "\n"
							+ gdAppWithTomcatBO.getNodeIp() + "\n"
							+ gdAppWithTomcatBO.getUsername());
					rowData.put("3", gdAppWithTomcatBO.getModuleCode());
					String port="";
					if( gdAppWithTomcatBO.getWebPort()!=null &&  gdAppWithTomcatBO.getWebPort()!=0){
						port= gdAppWithTomcatBO.getWebPort().toString();
					}
					rowData.put("4", port);
					String temp=gdAppWithTomcatBO.getAppPath();
//					for (UpcodeApp up : upcodeApps) {
//						if (up.getModuleCode().equals(gdAppWithTomcatBO.getModuleCode())) {
//							temp+="/"+up.getFileList();
//							break;
//							}
//					}
					temp=processPath(temp);
					String cmd = "1. cd " + temp
							+ "{endLine}" 
							+ "2. " + gdAppWithTomcatBO.getStopCommand()
							+ "{endLine}" + "3. "
							+ gdAppWithTomcatBO.getCheckStatus() + "{endLine}";
					rowData.put("5", cmd.replace("{endLine}", "\r\n"));
					rowData.put(
							"6",
							"1. cd đến thư mục "
									+ temp
									+ "  \r\n"
									+ "2. Dừng ứng dụng tomcat \r\n"
									+ "3. Hiện thị danh sách các PID đang hoạt động của tomcat \r\n"
									+ gdAppWithTomcatBO
											.getCheckStoppedCommand());
					rowData.put("7","0.5");
					rowData.put("8","");
					rowData.put("9","");
					result.put("row" + row, rowData);
				}
			}
			//
			for (GdAppWithoutTomcatBO obj : appWithoutTomcatBOs) {
				 flagStart = false;
				for (UpcodeApp up : upcodeApps) {
					if (up.getModuleCode().equals(obj.getModuleCode())) {
						flagStart = checkLifeCircle(up.getLifeCircle(), "stop") ;
						break;
					}
				}
				if (flagStart) {
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					rowData.put("1", row + "");
					rowData.put("2", obj.getAppCode() + "\n" + obj.getNodeIp()
							+ "\n" + obj.getUsername());
					rowData.put("3", obj.getModuleCode());
					String port="";
					if(obj.getMmPort()!=null && obj.getMmPort()!=0){
						port=obj.getMmPort().toString();
					}
					rowData.put("4",  port);
					String temp=obj.getAppPath();
//					for (UpcodeApp up : upcodeApps) {
//						if (up.getModuleCode().equals(obj.getModuleCode())) {
//							temp+="/"+up.getFileList();
//							break;
//							}
//					}
					temp=processPath(temp);
					String cmd = "1. cd " + temp + "{endLine}"
							+ "2. " + obj.getStopCommand() + "{endLine}"
							+ "3. " + obj.getCheckStatus() + "{endLine}";
					rowData.put("5", cmd.replace("{endLine}", "\r\n"));
					rowData.put(
							"6",
							"1. cd đến thư mục "
									+ temp
									+ "  \r\n"
									
									+ "2. Dừng ứng dụng \r\n"
									+ "3. Tiến trình is not running. Nếu tiến trình chưa dừng thực hiện lại lệnh dừng"
									);
					rowData.put("7","0.5");
					rowData.put("8","");
					rowData.put("9","");
					result.put("row" + row, rowData);
				}
			}

			if (row == 0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");
				rowData.put("7","");
				rowData.put("8","");
				rowData.put("9","");
				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}

	static LinkedHashMap<String, LinkedHashMap<String, String>> buildTarget(
			List<String> columns, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			if (appDatabaseBOs!=null && appDatabaseBOs.size() > 0) {
				for (GdAppDatabaseBO obj : appDatabaseBOs) {
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					String temp="";
					String temp1="";
					String temp2="";
					for(EditDatabase ed:editDatabases){
						if(ed.getAppCode().equals(obj.getAppCode()) 
								&& ed.getModuleCode().equals(obj.getModuleCode())
								&& ed.getChangeTarget()!=null){
							temp=ed.getChangeTarget();
							if(ed.getSidOrService()!=null) temp1=ed.getSidOrService();
							if(ed.getAccount()!=null) temp2=ed.getAccount();
						}
							
					}
					if(temp1.equals("")) temp1=obj.getSidOrService();
					if(temp2.equals("")) temp2=obj.getUsername();
					rowData.put("1", row + "");
					rowData.put("2", obj.getNodeIp());
					rowData.put("3", temp1);
					rowData.put("4", temp2);
					
					rowData.put("5", "" + temp);
					result.put("row" + row, rowData);
				}
			} else {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}

	//
	static LinkedHashMap<String, LinkedHashMap<String, String>> buildBackupDB(
			List<String> columns, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			if (appDatabaseBOs!=null && appDatabaseBOs.size() > 0) {
				for (GdAppDatabaseBO obj : appDatabaseBOs) {
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					String temp="";
					String temp1="";
					String temp2="";
					String temp3="";
					for(EditDatabase ed:editDatabases){
						if(ed.getAppCode().equals(obj.getAppCode()) 
								&& ed.getModuleCode().equals(obj.getModuleCode())
								&& ed.getChangeTarget()!=null){
							if(ed.getBackup()!=null) temp1=ed.getBackup();
							temp=ed.getChangeTarget();
							if(ed.getSidOrService()!=null) temp2=ed.getSidOrService();
							if(ed.getAccount()!=null) temp3=ed.getAccount();
						}
							
					}
					if(temp2.equals("")) temp2=obj.getSidOrService();
					if(temp3.equals("")) temp3=obj.getUsername();
					rowData.put("1", row + "");
					rowData.put("2", obj.getNodeIp());
					rowData.put("3",temp2);
					rowData.put("4",temp3);
					
					rowData.put("5", "" + temp1);
					rowData.put("6","Backup " + temp);
					rowData.put("7","15");
					rowData.put("8","");
					rowData.put("9","");
					result.put("row" + row, rowData);
				}
			} else {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");
				rowData.put("7","");
				rowData.put("8","");
				rowData.put("9","");
				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}

	/**
	 * @param object
	 * @param editDatabases
	 * @param upcodeApps
	 * @param appDatabaseBOs
	 * @param appWithTomcatBOs
	 * @param appWithoutTomcatBOs
	 * @return
	 
	static String timeBackupCode = "";

	public static LinkedHashMap<String, LinkedHashMap<String, String>> buildBackupCode(
			Object object, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		// TODO Auto-generated method stub
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			for(UpcodeApp up : upcodeApps){
				if (up.getFileList().toLowerCase().equals("không thay đổi") ||up.getFileList()==null||
						up.getFileList().equals("") || checkLifeCircle(up.getLifeCircle(), "backup")==false){	
				}
				else{
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					rowData.put("1", row + "");
					rowData.put("2", up.getAppCode() + "\r\n"
							+ up.getNode() + "\r\n"
							+ up.getAccount());
					rowData.put("3", up.getModuleCode());
					String port="";
					GdAppWithTomcatBO app=null;
					GdAppWithoutTomcatBO process=null;
					for(GdAppWithTomcatBO obj: appWithTomcatBOs){
						if(obj.getAppCode().equals(up.getAppCode()) && obj.getModuleCode().equals(up.getModuleCode())){
							app=obj;
							break;
						}
					}
					for(GdAppWithoutTomcatBO obj: appWithoutTomcatBOs){
						if(obj.getAppCode().equals(up.getAppCode()) && obj.getModuleCode().equals(up.getModuleCode())){
							process=obj;
							break;
						}
					}
					
					String cmd="";
					String temp2="";
					if(app!=null){
						if(app.getWebPort()!=0)
						port=app.getWebPort().toString();
						temp2=app.getAppPath();
					}else if(process!=null){
						if(process.getMmPort()!=0)
						port=process.getMmPort().toString();
						temp2=process.getAppPath();
					}
					temp2+="/"+up.getFileList();
					rowData.put("4", port );
					if(up.getCmdBackupCode()==null || up.getCmdBackupCode().equals("")){
						if(up.getUpcodeFolder().equals("1"))
						cmd="cp -r";
						else cmd ="cp";
					}
					else {
					cmd=up.getCmdBackupCode().trim();
					}
					String tmp1=up.getChangepathFile();
					String []tmpArr=tmp1.split(",");
					String str1="";
					String str2="";
					int k=2;
					if(tmpArr!=null && tmpArr.length>0){
						for(int i=0;i<tmpArr.length;i++){
							
							str1+=k+". "+cmd+" "+tmpArr[i].trim()+" "+tmpArr[i].trim()+"_bk"+getCurrentTime()+";\r\n";
							str2+=k+". Backup dữ liệu vào thư mục "+tmpArr[i].trim()+"_bk"
									+ timeBackupCode + "\r\n";
							k++;
						}
					}
					temp2=processPath(temp2);
					rowData.put("5", "1. cd " + temp2
							+ "; \r\n" + str1);
					timeBackupCode = getCurrentTime();
					rowData.put("6",
							"1. cd đến thư mục " + temp2
									+ "  \r\n"
									+ str2);
					rowData.put("7","1");
					rowData.put("8","");
					rowData.put("9","");
					result.put("row" + row, rowData);
				}
					
				
			}		
			if (row == 0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");
				rowData.put("7","");
				rowData.put("8","");
				rowData.put("9","");
				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}

	public static LinkedHashMap<String, LinkedHashMap<String, String>> buildUpcode(
			Object object, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		// TODO Auto-generated method stub
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			
			for (UpcodeApp up : upcodeApps) {
				
					if (up.getFileList().toLowerCase().equals("không thay đổi") ||up.getFileList()==null||
							up.getFileList().equals("") || checkLifeCircle(up.getLifeCircle(), "upcode")==false){
						
					}
					else{
						row++;
						LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
						rowData.put("1", row + "");
						rowData.put("2", up.getAppCode() + "\n"
								+ up.getNode() +"\n" + up.getAccount());
						rowData.put("3", up.getModuleCode());
						String port="";
						String temp2="";
						GdAppWithTomcatBO app=null;
						GdAppWithoutTomcatBO process=null;
						for(GdAppWithTomcatBO obj: appWithTomcatBOs){
							if(obj.getAppCode().equals(up.getAppCode()) && obj.getModuleCode().equals(up.getModuleCode())){
								app=obj;
								break;
							}
						}
						for(GdAppWithoutTomcatBO obj: appWithoutTomcatBOs){
							if(obj.getAppCode().equals(up.getAppCode()) && obj.getModuleCode().equals(up.getModuleCode())){
								process=obj;
								break;
							}
						}
						if(app!=null){
							if(app.getWebPort()!=0) port=app.getWebPort().toString();
							 temp2=app.getAppPath();
						}
						else if(process!=null){
							if(process.getMmPort()!=0) port=process.getMmPort().toString();
							 temp2=process.getAppPath();
						}
						rowData.put("4", up.getAccount() + "/"
								+port );
						String str = up.getNewContent();
						String upCodeStr = up.getChangePurpose();
						temp2+="/"+up.getFileList();
						temp2=processPath(temp2);
						String temp = "1. cd " + temp2 + " \r\n" + "2. Upcode: " + str;
						temp = processPath(temp);
						rowData.put("5", temp);
						rowData.put("6",
								"1. cd đến thư mục " + temp2
										+ "  \r\n" + "2. " + upCodeStr);
						rowData.put("7","2");
						rowData.put("8","");
						rowData.put("9","");
						result.put("row" + row, rowData);
					}
						
					
			}
			 
			if (row == 0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");
				rowData.put("7","");
				rowData.put("8","");
				rowData.put("9","");
				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}
	
	public static LinkedHashMap<String, LinkedHashMap<String, String>> buildChangeConfigDB(
			Object object, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		// TODO Auto-generated method stub
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
				if(appDatabaseBOs!=null && appDatabaseBOs.size() > 0){
				for (GdAppDatabaseBO obj : appDatabaseBOs) {
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					String temp="";
					String temp1="";
					String temp3="";
					String temp4="";
					String temp5="";
					for(EditDatabase ed:editDatabases){
						if(ed.getAppCode().equals(obj.getAppCode()) 
								&& ed.getModuleCode().equals(obj.getModuleCode())
								&& ed.getCompileEffectObject()!=null){
							if(ed.getChangePurpose()!=null) temp1=ed.getChangePurpose();
							if(ed.getDeploy()!=null) temp3=ed.getDeploy();
							temp=ed.getCompileEffectObject();
							if(ed.getSidOrService()!=null) temp4=ed.getSidOrService();
							if(ed.getAccount()!=null) temp5=ed.getAccount();
						}
							
					}
					if(temp4.equals("")) temp4=obj.getSidOrService();
					if(temp5.equals("")) temp5=obj.getUsername();
					rowData.put("1", row + "");
					rowData.put("2", obj.getNodeIp());
					rowData.put("3", temp4);
					rowData.put("4", temp5);				
					rowData.put("5", temp3);
					rowData.put("6", temp);
					rowData.put("7", "" + temp1);
					rowData.put("8", "15");
					rowData.put("9","");
					rowData.put("10","");
					result.put("row" + row, rowData);
				}
				}
			if(row==0){
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");
				rowData.put("7", "Không thực hiện");
				rowData.put("8","");
				rowData.put("9","");
				rowData.put("10","");
				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}

	/**
	 * @param object
	 * @param editDatabases
	 * @param upcodeApps
	 * @param appDatabaseBOs
	 * @param appWithTomcatBOs
	 * @param appWithoutTomcatBOs
	 * @return
	
	public static LinkedHashMap<String, LinkedHashMap<String, String>> buildChangeDB(
			Object object, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		// TODO Auto-generated method stub
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			if(appDatabaseBOs!=null && appDatabaseBOs.size() > 0){
				for (GdAppDatabaseBO obj : appDatabaseBOs) {
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					rowData.put("1", row + "");
					rowData.put("2", obj.getNodeIp());
					rowData.put("3", obj.getSidOrService());
					rowData.put("4", obj.getUsername() + "");
					rowData.put("5", "Không có nội dung thay đổi DB");
					result.put("row" + row, rowData);
				}
			}
				if(row==0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");

				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}

	/**
	 * @param object
	 * @param editDatabases
	 * @param upcodeApps
	 * @param appDatabaseBOs
	 * @param appWithTomcatBOs
	 * @param appWithoutTomcatBOs
	 * @return
	 
	private static final String cmdClearcache = "1. cd cachePath \r\n"
			+ "2. pwd \r\n" + "3. rm -rf * \r\n";

	public static LinkedHashMap<String, LinkedHashMap<String, String>> buildClearCache(
			Object object, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		// TODO Auto-generated method stub
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			boolean flagStart = false;
			for (GdAppWithTomcatBO gdAppWithTomcatBO : appWithTomcatBOs) {
				flagStart = false;
				for (UpcodeApp up : upcodeApps) {
					if (up.getModuleCode().equals(
							gdAppWithTomcatBO.getModuleCode())) {
						if (up.getDeleteCache().toLowerCase().equals("có"))
							flagStart = true;
						break;
					}
				}
				if (flagStart) {

					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					rowData.put("1", row + "");
					rowData.put("2", gdAppWithTomcatBO.getAppCode() + "\n"
							+ gdAppWithTomcatBO.getNodeIp()+"\n"
							+gdAppWithTomcatBO.getUsername());
					rowData.put("3", gdAppWithTomcatBO.getModuleCode());
					String port="";
					if( gdAppWithTomcatBO.getWebPort()!=null &&  gdAppWithTomcatBO.getWebPort()!=0){
						port= gdAppWithTomcatBO.getWebPort().toString();
					}
					rowData.put("4", port);
					String temp2="";
					for(UpcodeApp upApp:upcodeApps){
						if (upApp.getModuleCode().equals(
								gdAppWithTomcatBO.getModuleCode())) {
						temp2+="/"+upApp.getFileList();
						}
					}
					String temp = cmdClearcache.replace("cachePath",
							gdAppWithTomcatBO.getAppPath() + "//"
									+ gdAppWithTomcatBO.getAppCachePath());
					temp = processPath(temp);
					rowData.put("5", temp);
					rowData.put("6", "Xóa cache ứng dụng");
					rowData.put("7", "1");
					rowData.put("8","");
					rowData.put("9","");
					result.put("row" + row, rowData);
				}
			}
			//
			for (GdAppWithoutTomcatBO obj : appWithoutTomcatBOs) {
				 flagStart = false;
				for (UpcodeApp up : upcodeApps) {
					if (up.getModuleCode().equals(obj.getModuleCode())) {
						if (up.getDeleteCache().toLowerCase().equals("có"))
							flagStart = true;
						break;
					}
				}
				if (flagStart) {

					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					rowData.put("1", row + "");
					rowData.put("2", obj.getAppCode() + "\n" + obj.getNodeIp() +"\n"+ obj.getUsername());
					rowData.put("3", obj.getModuleCode());
					String port="";
					if(obj.getMmPort()!=null && obj.getMmPort()!=0){
						port=obj.getMmPort().toString();
					}
					rowData.put("4", port);
					String temp2="";
					for(UpcodeApp upApp:upcodeApps){
						if (upApp.getModuleCode().equals(
								obj.getModuleCode())) {
						temp2+="/"+upApp.getFileList();
						}
					}
					String temp = cmdClearcache.replace("cachePath",
							obj.getAppPath() + "//" + obj.getAppLog());
					temp = processPath(temp);
					rowData.put("5", temp);
					rowData.put("6", "Xóa cache ứng dụng");
					rowData.put("7", "1");
					rowData.put("8","");
					rowData.put("9","");
					result.put("row" + row, rowData);
				}
			}
			if (row == 0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");
				rowData.put("8","");
				rowData.put("9","");
				rowData.put("7","");
				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}

	
	public static LinkedHashMap<String, LinkedHashMap<String, String>> buildStartApp(
			Object object, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		// TODO Auto-generated method stub
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			boolean flagStart = false;
			for (GdAppWithTomcatBO gdAppWithTomcatBO : appWithTomcatBOs) {
				flagStart = false;
				for (UpcodeApp up : upcodeApps) {
					if (up.getModuleCode().equals(
							gdAppWithTomcatBO.getModuleCode())) {
						flagStart = checkLifeCircle(up.getLifeCircle(), "start") ;
						break;
					}
				}
				if (flagStart) {
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					rowData.put("1", row + "");
					rowData.put("2", gdAppWithTomcatBO.getAppCode() + "\n"
							+ gdAppWithTomcatBO.getNodeIp() +"\n"
							+gdAppWithTomcatBO.getUsername());
					rowData.put("3", gdAppWithTomcatBO.getModuleCode());
					String port="";
					if(gdAppWithTomcatBO.getWebPort()!=null && gdAppWithTomcatBO.getWebPort()!=0){
						port=gdAppWithTomcatBO.getWebPort().toString();
					}
					rowData.put("4",  port);
					String fullLog=gdAppWithTomcatBO.getAppPath()+"/"+gdAppWithTomcatBO.getLogPath();
					fullLog=processPath(fullLog);
					String temp=gdAppWithTomcatBO.getAppPath();
					temp=processPath(temp);
					
					rowData.put("5", "1. cd " + temp
							+ "\r\n 2. " + gdAppWithTomcatBO.getStartCommand()+"; " +gdAppWithTomcatBO.getCheckStartedCommand()
							+ " \r\n" + "3. "+gdAppWithTomcatBO.getCheckStatus()+" \r\n");
					rowData.put(
							"6",
							"1. cd đến thư mục "
									+ temp
									+ "  \r\n 2. Start ứng dụng; Kiểm tra có log start \r\n 3. Kiểm tra trạng thái ứng dụng");
					rowData.put("7", "1");
					rowData.put("8","");
					rowData.put("9","");
					result.put("row" + row, rowData);
				}
			}
			//
			for (GdAppWithoutTomcatBO obj : appWithoutTomcatBOs) {
				 flagStart = false;
				for (UpcodeApp up : upcodeApps) {
					if (up.getModuleCode().equals(
							obj.getModuleCode())) {
						flagStart = checkLifeCircle(up.getLifeCircle(), "start") ;
						break;
					}
				}
				if (flagStart) {
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					rowData.put("1", row + "");
					rowData.put("2", obj.getAppCode() + "\n"
							+ obj.getNodeIp()+"\n"
							+obj.getUsername());
					rowData.put("3", obj.getModuleCode());
					String port="";
					if(obj.getMmPort()!=null && obj.getMmPort()!=0){
						port=obj.getMmPort().toString();
					}
					rowData.put("4", port);
					String temp=obj.getAppPath();
//					for(UpcodeApp upApp:upcodeApps){
//						if (upApp.getModuleCode().equals(
//								obj.getModuleCode())) {
//							temp+="/"+upApp.getFileList();
//							break;	
//						}
//						
//					}
					
					
					temp=processPath(temp);
					rowData.put("5", "1. cd " + temp
							+ "\r\n 2. " + obj.getStartCommand()
							+ " \r\n" + "3. "+obj.getCheckStatus()+" \r\n");
					rowData.put(
							"6",
							"1. cd đến thư mục "
									+ temp
									+ "  \r\n 2. Start ứng dụng \r\n 3. Kiểm tra trạng thái ứng dụng");
					rowData.put("7", "1");
					rowData.put("8","");
					rowData.put("9","");
					result.put("row" + row, rowData);
				}
			}
			if (row == 0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");
				rowData.put("8","");
				rowData.put("9","");
				rowData.put("7","");
				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}


	public static LinkedHashMap<String, LinkedHashMap<String, String>> buildRollBackData(
			Object object, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		// TODO Auto-generated method stub
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			if(appDatabaseBOs!=null && appDatabaseBOs.size() > 0){
				for (GdAppDatabaseBO obj : appDatabaseBOs) {
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					String temp3="";
					String temp4="";
					String temp5="";
					for(EditDatabase ed:editDatabases){
						if(ed.getAppCode().equals(obj.getAppCode()) 
								&& ed.getModuleCode().equals(obj.getModuleCode())
								&& ed.getRollback()!=null){
							temp3=ed.getRollback();
							temp4=ed.getAccount();
							temp5=ed.getSidOrService();
						}
							
					}
					if(temp4==null || temp4.equals("")) temp4=obj.getUsername();
					if(temp5==null || temp5.equals("")) temp5=obj.getSidOrService();
					rowData.put("1", row + "");
					rowData.put("2", obj.getNodeIp());
					rowData.put("3", temp5);
					rowData.put("4", temp4);
					
					rowData.put("5", temp3);
					rowData.put("6", "Rollback DB");
					result.put("row" + row, rowData);
				}
			}
				if(row==0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");

				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}

	

	public static LinkedHashMap<String, LinkedHashMap<String, String>> buildRollBackCode(
			Object object, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		// TODO Auto-generated method stub
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			for (UpcodeApp up : upcodeApps) {
						    if (up.getFileList().toLowerCase().equals("không thay đổi") ||up.getFileList()==null || 
							up.getFileList().equals("") || checkLifeCircle(up.getLifeCircle(), "rollback")==false){
									}
					else{
						row++;
						LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
						rowData.put("1", row + "");
						rowData.put("2", up.getAppCode() + "\n" + up.getNode() + "\n" + up.getAccount());
						rowData.put("3", up.getModuleCode());
						String port="";
						String temp2="";
						GdAppWithTomcatBO app=null;
						GdAppWithoutTomcatBO process=null;
						for(GdAppWithTomcatBO obj: appWithTomcatBOs){
							if(obj.getAppCode().equals(up.getAppCode()) && obj.getModuleCode().equals(up.getModuleCode())){
								app=obj;
								break;
							}
						}
						for(GdAppWithoutTomcatBO obj: appWithoutTomcatBOs){
							if(obj.getAppCode().equals(up.getAppCode()) && obj.getModuleCode().equals(up.getModuleCode())){
								process=obj;
								break;
							}
						}
						if(app!=null){
							if(app.getWebPort()!=0) port=app.getWebPort().toString();
							 temp2=app.getAppPath();
						}
						else if(process!=null){
							if(process.getMmPort()!=0) port=process.getMmPort().toString();
							 temp2=process.getAppPath();
						}
						String cmd="cp -r";
						if(up.getUpcodeFolder()==null || up.getUpcodeFolder().equals("0") || up.getUpcodeFolder().equals(""))
							cmd="cp";
						rowData.put("4", port);
						
						temp2+="/"+up.getFileList();
						temp2=processPath(temp2);
						String tmp1=up.getChangepathFile();
						String []tmpArr=tmp1.split(",");
						String str1="";
						String str2="";
						int k=2;
						int t=2;
						if(tmpArr!=null && tmpArr.length>0){
							for(int i=0;i<tmpArr.length;i++){								
								t=k+1;							
								str1+=k+". mv "+tmpArr[i].trim()+" "+tmpArr[i].trim()+"_fail"+getCurrentTime()+"; \r\n"
										+ t+". "+cmd+" "+tmpArr[i].trim()+"_bk"+timeBackupCode+" "+tmpArr[i].trim()+"; \r\n";
								str2+=tmpArr[i].trim()+"_bk"
										+ timeBackupCode + ", ";
								k+=2;
							}
						}
						
						rowData.put("5", "1. cd " + temp2 + "; \r\n"
								+ str1);
						rowData.put("6", "Thực hiện rollback thư mục "+str2);
						result.put("row" + row, rowData);
					}
			}
			
			if (row == 0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");
				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}

	private static String getCurrentTime() {
		String currentTime = new SimpleDateFormat("yyyyMMdd").format(Calendar
				.getInstance().getTime());
		return currentTime;
	}

	public static LinkedHashMap<String, LinkedHashMap<String, String>> buildCheckLogApp(
			Object object, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		// TODO Auto-generated method stub
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			boolean flagStart=false;
			for (GdAppWithTomcatBO obj : appWithTomcatBOs) {
				flagStart = false;
				for (UpcodeApp up : upcodeApps) {
					if (up.getModuleCode().equals(
							obj.getModuleCode())) {
						flagStart = checkLifeCircle(up.getLifeCircle(), "checklog") ;
						break;
					}
				}
				if (flagStart) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", obj.getAppCode() + "\n" + obj.getNodeIp() +"\n" + obj.getUsername());
				rowData.put("3", obj.getModuleCode());
				String port="";
				if(obj.getWebPort()!=null && obj.getWebPort()!=0){
					port=obj.getWebPort().toString();
				}
				rowData.put("4",  port);
				
				String temp = "1. cd " + obj.getAppPath() + "/"
						+ obj.getLogPath() + " \r\n" + "2. "
						+ obj.getMethodCheckLog();
				temp = processPath(temp);
				rowData.put("5", temp);
				rowData.put("6",
						"1. cd đến thư mục log  \r\n 2. Check log ứng dụng");
				rowData.put("7", "1");
				rowData.put("8","");
				rowData.put("9","");
				result.put("row" + row, rowData);
				}
			}

			for (GdAppWithoutTomcatBO obj : appWithoutTomcatBOs) {
				flagStart = false;
				for (UpcodeApp up : upcodeApps) {
					if (up.getModuleCode().equals(
							obj.getModuleCode())) {
						flagStart = checkLifeCircle(up.getLifeCircle(), "checklog") ;
						break;
					}
				}
				if (flagStart) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", obj.getAppCode() + "\n" + obj.getNodeIp() +"\n" + obj.getUsername());
				rowData.put("3", obj.getModuleCode());
				String port ="";
				if(obj.getLogPort() !=null && obj.getLogPort() !=0){
					port=obj.getLogPort() .toString();
				}
				rowData.put("4", port);
				
				String temp = "1. cd " + obj.getAppPath()+"/ \r\n"
						+"2. "+ obj.getAppLog() + " \r\n" + "3. "
						+ obj.getWrapperLog();
				temp = processPath(temp);
				rowData.put("5", temp);
				rowData.put("6",
						"1. cd đến thư mục log \r\n 2. Check log ứng dụng \r\n 3. Check log wrapper");
				rowData.put("7", "1");
				rowData.put("8","");
				rowData.put("9","");
				result.put("row" + row, rowData);
				}
			}
			if (row == 0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");
				rowData.put("8","");
				rowData.put("9","");
				rowData.put("7","");
				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}

	public static String processPath(String path) {
		String result = "";
		try {

			boolean flag = true;

			while (flag) {
				path = path.replace("//", "/");
				flag = path.contains("//");
			}
			result = path;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return result;
	}

	
	public static LinkedHashMap<String, LinkedHashMap<String, String>> buildCheckList(
			Object object, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		// TODO Auto-generated method stub
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			for (UpcodeApp up : upcodeApps) {
			    if (checkLifeCircle(up.getLifeCircle(), "checklist")==false){
						}
		else{
			row++;
			LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
			rowData.put("1", row + "");
			rowData.put("2", up.getAppCode() + "\n" + up.getNode()+ "\n" + up.getAccount());
			rowData.put("3", up.getModuleCode());
			String port="";
			String checklist="";
			GdAppWithTomcatBO app=null;
			GdAppWithoutTomcatBO process=null;
			for(GdAppWithTomcatBO obj: appWithTomcatBOs){
				if(obj.getAppCode().equals(up.getAppCode()) && obj.getModuleCode().equals(up.getModuleCode())){
					app=obj;
					break;
				}
			}
			for(GdAppWithoutTomcatBO obj: appWithoutTomcatBOs){
				if(obj.getAppCode().equals(up.getAppCode()) && obj.getModuleCode().equals(up.getModuleCode())){
					process=obj;
					break;
				}
			}
			if(app!=null){
				if(app.getWebPort()!=0) port=app.getWebPort().toString();
				if(up !=null && app.getChecklist()!=null && !app.getChecklist().equals("")){
					 checklist= "1. "+ app.getChecklist()+" \r\n";
					}
			}
			else if(process!=null){
				if(process.getMmPort()!=0) port=process.getMmPort().toString();
				if(up !=null && process.getChecklist()!=null && !process.getChecklist().equals("")){
					 checklist= "1. "+ process.getChecklist()+" \r\n";
					}
			}
			rowData.put("4", port);
			String temp2=up.getFileList();
			String testMethod=up.getTest();
			if(testMethod!=null && !testMethod.equals("")){
				checklist+="2. "+testMethod;
			}
			checklist = processPath(checklist);
			rowData.put("5", checklist);
			rowData.put("6",
					"Checklist ứng dụng");
			rowData.put("7", "1");
			rowData.put("8","");
			rowData.put("9","");
			result.put("row" + row, rowData);
		}
			}
			if (row == 0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");
				rowData.put("8","");
				rowData.put("9","");
				rowData.put("7","");
				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}

	
	public static LinkedHashMap<String, LinkedHashMap<String, String>> buildStopStartApp(
			Object object, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		// TODO Auto-generated method stub
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			boolean flagStart = false;
			for (GdAppWithTomcatBO gdAppWithTomcatBO : appWithTomcatBOs) {
				flagStart = false;
				for (UpcodeApp up : upcodeApps) {
					if (up.getModuleCode().equals(
							gdAppWithTomcatBO.getModuleCode())) {
						
							flagStart = checkLifeCircle(up.getLifeCircle(), "stop/start") ;
						break;
					}
				}
				if (flagStart) {
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					rowData.put("1", row + "");
					rowData.put("2", gdAppWithTomcatBO.getAppCode() + "\n"
							+ gdAppWithTomcatBO.getNodeIp()+"\n"
							+ gdAppWithTomcatBO.getUsername());
					rowData.put("3", gdAppWithTomcatBO.getModuleCode());
					String port="";
					if(gdAppWithTomcatBO.getWebPort()!=null && gdAppWithTomcatBO.getWebPort()!=0){
						port=gdAppWithTomcatBO.getWebPort().toString();
					}
					rowData.put("4",  port);
					String temp=gdAppWithTomcatBO.getAppPath();
					temp=processPath(temp);
					rowData.put("5", "1. cd " + temp
							+ "\r\n 2. " + gdAppWithTomcatBO.getStopCommand()
							+ "\r\n 3. " + gdAppWithTomcatBO.getStartCommand()
							+ " \r\n" + "4. "+gdAppWithTomcatBO.getCheckStatus()+" \r\n");
					rowData.put(
							"6",
							"1. cd đến thư mục "
									+ temp
									+ "  \r\n 2. Stop ứng dụng \r\n 3. Start ứng dụng \r\n 4. Kiểm tra trạng thái ứng dụng");
					result.put("row" + row, rowData);
				}
			}
			//
			for (GdAppWithoutTomcatBO obj : appWithoutTomcatBOs) {
				 flagStart = false;
				 for (UpcodeApp up : upcodeApps) {
						if (up.getModuleCode().equals(
								obj.getModuleCode())) {
							flagStart = checkLifeCircle(up.getLifeCircle(), "stop/start") ;
							break;
						}
					}
				if (flagStart) {
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					rowData.put("1", row + "");
					rowData.put("2", obj.getAppCode() + "\n"
							+ obj.getNodeIp() +"\n"
							+ obj.getUsername());
					rowData.put("3", obj.getModuleCode());
					String port="";
					if(obj.getMmPort()!=null && obj.getMmPort()!=0){
						port=obj.getMmPort().toString();
					}
					rowData.put("4", port);
					String temp=obj.getAppPath();
//					for(UpcodeApp upApp:upcodeApps){
//						if (upApp.getModuleCode().equals(
//								obj.getModuleCode())) {
//							temp+="/"+upApp.getFileList();
//							break;	
//						}
//						
//					}
					temp=processPath(temp);
					rowData.put("5", "1. cd " + temp
							+ "\r\n 2. " + obj.getStopCommand()
							+ "\r\n 3. " + obj.getStartCommand()
							+ " \r\n" + "4. "+obj.getCheckStatus()+" \r\n");
					rowData.put(
							"6",
							"1. cd đến thư mục "
									+ temp
									+ "  \r\n 2. Stop ứng dụng \r\n 3. Start ứng dụng \r\n 4. Kiểm tra trạng thái ứng dụng");
					result.put("row" + row, rowData);
				}
			}
			if (row == 0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");
				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}


	public static LinkedHashMap<String, LinkedHashMap<String, String>> buildRestartApp(
			Object object, List<EditDatabase> editDatabases,
			List<UpcodeApp> upcodeApps, List<GdAppDatabaseBO> appDatabaseBOs,
			List<GdAppWithTomcatBO> appWithTomcatBOs,
			List<GdAppWithoutTomcatBO> appWithoutTomcatBOs) {
		// TODO Auto-generated method stub
		LinkedHashMap<String, LinkedHashMap<String, String>> result = null;
		try {
			int row = 0;
			result = new LinkedHashMap<>();
			boolean flagStart = false;
			for (GdAppWithTomcatBO gdAppWithTomcatBO : appWithTomcatBOs) {
				flagStart = false;
				for (UpcodeApp up : upcodeApps) {
					if (up.getModuleCode().equals(
							gdAppWithTomcatBO.getModuleCode())) {
						
							flagStart = checkLifeCircle(up.getLifeCircle(), "restart") ;
						break;
					}
				}
				if (flagStart) {
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					rowData.put("1", row + "");
					rowData.put("2", gdAppWithTomcatBO.getAppCode() + "\n"
							+ gdAppWithTomcatBO.getNodeIp() +"\n"
							+ gdAppWithTomcatBO.getUsername());
					rowData.put("3", gdAppWithTomcatBO.getModuleCode());
					String port="";
					if(gdAppWithTomcatBO.getWebPort()!=null && gdAppWithTomcatBO.getWebPort()!=0){
						port=gdAppWithTomcatBO.getWebPort().toString();
					}
					rowData.put("4",  port);
					String temp=gdAppWithTomcatBO.getAppPath();
					temp=processPath(temp);
					rowData.put("5", "1. cd " + temp
							+ "\r\n 2. " + gdAppWithTomcatBO.getRestartCommand()
							+ " \r\n" + "3. "+gdAppWithTomcatBO.getCheckStatus()+" \r\n");
					rowData.put(
							"6",
							"1. cd đến thư mục "
									+ temp
									+ "  \r\n 2. Restart ứng dụng \r\n 3. Kiểm tra trạng thái ứng dụng");
					rowData.put("7", "1");
					rowData.put("8","");
					rowData.put("9","");
					result.put("row" + row, rowData);
				}
			}
			//
			for (GdAppWithoutTomcatBO obj : appWithoutTomcatBOs) {
				 flagStart = false;
				for (UpcodeApp up : upcodeApps) {
					if (up.getModuleCode().equals(
							obj.getModuleCode())) {
						flagStart = checkLifeCircle(up.getLifeCircle(), "restart") ;
						break;
					}
				}
				if (flagStart) {
					row++;
					LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
					rowData.put("1", row + "");
					rowData.put("2", obj.getAppCode() + "\n"
							+ obj.getNodeIp() +"\n" + obj.getUsername());
					rowData.put("3", obj.getModuleCode());
					String port="";
					if(obj.getMmPort()!=null && obj.getMmPort()!=0){
						port=obj.getMmPort().toString();
					}
					rowData.put("4", port);
					String temp=obj.getAppPath();
//					for(UpcodeApp upApp:upcodeApps){
//						if (upApp.getModuleCode().equals(
//								obj.getModuleCode())) {
//							temp+="/"+upApp.getFileList();
//							break;	
//						}
//						
//					}
					temp=processPath(temp);
					rowData.put("5", "1. cd " + temp
							+ "\r\n 2. " + obj.getRestartCommand()
							+ " \r\n" + "3. "+obj.getCheckStatus()+" \r\n");
					rowData.put(
							"6",
							"1. cd đến thư mục "
									+ temp
									+ "  \r\n 2. Restart ứng dụng \r\n 3. Kiểm tra trạng thái ứng dụng");
					rowData.put("7", "1");
					rowData.put("8","");
					rowData.put("9","");
					result.put("row" + row, rowData);
				}
			}
			if (row == 0) {
				row++;
				LinkedHashMap<String, String> rowData = new LinkedHashMap<>();
				rowData.put("1", row + "");
				rowData.put("2", "Không thực hiện");
				rowData.put("3", "Không thực hiện");
				rowData.put("4", "Không thực hiện");
				rowData.put("5", "Không thực hiện");
				rowData.put("6", "Không thực hiện");
				rowData.put("8","");
				rowData.put("9","");
				rowData.put("7","");
				result.put("row" + row, rowData);
			}
		} catch (Exception e) {

		}

		return result;
	}
static boolean checkLifeCircle(String text, String regex){
	if(text ==null || text.equals("")) return true;
	String []arr=text.split(",");
	try{
		if(arr!=null && arr.length>0){
			for(String str: arr){
				if(str.trim().toLowerCase().equals(regex.toLowerCase()))
					return true;
			}
		}
	}
	catch(Exception e){
		logger.error(e.getMessage(), e);
	}
	return false;
	
}

**/
}
