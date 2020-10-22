/*
 * Created on Jun 7, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.util;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import com.viettel.bean.ExeObject;
import com.viettel.bean.OsAccount;
import com.viettel.controller.IimClientService;
import com.viettel.controller.IimClientServiceImpl;
import com.viettel.it.util.MessageUtil;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Array;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author quanns2
 */
@Service(value = "utils")
@Scope("session")
public class Util {
	private static Logger logger = LogManager.getLogger(Util.class);
	// anhnt2
	private HashMap<Long, String> statusKbType = new HashMap<Long, String>();
	/**
	 * Lay gia tri ip cua client.
	 */
	public static String getClientIp() {
		ExternalContext context = FacesContext.getCurrentInstance().getExternalContext();
		HttpServletRequest req = (HttpServletRequest) context.getRequest();

		return req.getRemoteHost();
	}

	public static String convertUTF8ToNoSign(String org) {
		char arrChar[] = org.toCharArray();
		char result[] = new char[arrChar.length];
		for (int i = 0; i < arrChar.length; i++) {
			switch (arrChar[i]) {
			case '\u00E1':
			case '\u00E0':
			case '\u1EA3':
			case '\u00E3':
			case '\u1EA1':
			case '\u0103':
			case '\u1EAF':
			case '\u1EB1':
			case '\u1EB3':
			case '\u1EB5':
			case '\u1EB7':
			case '\u00E2':
			case '\u1EA5':
			case '\u1EA7':
			case '\u1EA9':
			case '\u1EAB':
			case '\u1EAD':
			case '\u0203':
			case '\u01CE': {
				result[i] = 'a';
				break;
			}
			case '\u00E9':
			case '\u00E8':
			case '\u1EBB':
			case '\u1EBD':
			case '\u1EB9':
			case '\u00EA':
			case '\u1EBF':
			case '\u1EC1':
			case '\u1EC3':
			case '\u1EC5':
			case '\u1EC7':
			case '\u0207': {
				result[i] = 'e';
				break;
			}
			case '\u00ED':
			case '\u00EC':
			case '\u1EC9':
			case '\u0129':
			case '\u1ECB': {
				result[i] = 'i';
				break;
			}
			case '\u00F3':
			case '\u00F2':
			case '\u1ECF':
			case '\u00F5':
			case '\u1ECD':
			case '\u00F4':
			case '\u1ED1':
			case '\u1ED3':
			case '\u1ED5':
			case '\u1ED7':
			case '\u1ED9':
			case '\u01A1':
			case '\u1EDB':
			case '\u1EDD':
			case '\u1EDF':
			case '\u1EE1':
			case '\u1EE3':
			case '\u020F': {
				result[i] = 'o';
				break;
			}
			case '\u00FA':
			case '\u00F9':
			case '\u1EE7':
			case '\u0169':
			case '\u1EE5':
			case '\u01B0':
			case '\u1EE9':
			case '\u1EEB':
			case '\u1EED':
			case '\u1EEF':
			case '\u1EF1': {
				result[i] = 'u';
				break;
			}
			case '\u00FD':
			case '\u1EF3':
			case '\u1EF7':
			case '\u1EF9':
			case '\u1EF5': {
				result[i] = 'y';
				break;
			}
			case '\u0111': {
				result[i] = 'd';
				break;
			}
			case '\u00C1':
			case '\u00C0':
			case '\u1EA2':
			case '\u00C3':
			case '\u1EA0':
			case '\u0102':
			case '\u1EAE':
			case '\u1EB0':
			case '\u1EB2':
			case '\u1EB4':
			case '\u1EB6':
			case '\u00C2':
			case '\u1EA4':
			case '\u1EA6':
			case '\u1EA8':
			case '\u1EAA':
			case '\u1EAC':
			case '\u0202':
			case '\u01CD': {
				result[i] = 'A';
				break;
			}
			case '\u00C9':
			case '\u00C8':
			case '\u1EBA':
			case '\u1EBC':
			case '\u1EB8':
			case '\u00CA':
			case '\u1EBE':
			case '\u1EC0':
			case '\u1EC2':
			case '\u1EC4':
			case '\u1EC6':
			case '\u0206': {
				result[i] = 'E';
				break;
			}
			case '\u00CD':
			case '\u00CC':
			case '\u1EC8':
			case '\u0128':
			case '\u1ECA': {
				result[i] = 'I';
				break;
			}
			case '\u00D3':
			case '\u00D2':
			case '\u1ECE':
			case '\u00D5':
			case '\u1ECC':
			case '\u00D4':
			case '\u1ED0':
			case '\u1ED2':
			case '\u1ED4':
			case '\u1ED6':
			case '\u1ED8':
			case '\u01A0':
			case '\u1EDA':
			case '\u1EDC':
			case '\u1EDE':
			case '\u1EE0':
			case '\u1EE2':
			case '\u020E': {
				result[i] = 'O';
				break;
			}
			case '\u00DA':
			case '\u00D9':
			case '\u1EE6':
			case '\u0168':
			case '\u1EE4':
			case '\u01AF':
			case '\u1EE8':
			case '\u1EEA':
			case '\u1EEC':
			case '\u1EEE':
			case '\u1EF0': {
				result[i] = 'U';
				break;
			}

			case '\u00DD':
			case '\u1EF2':
			case '\u1EF6':
			case '\u1EF8':
			case '\u1EF4': {
				result[i] = 'Y';
				break;
			}
			case '\u0110':
			case '\u00D0':
			case '\u0089': {
				result[i] = 'D';
				break;
			}
			default:
				result[i] = arrChar[i];
			}
		}
		return new String(result);
	}

	public String subContent(String content, Integer maxLength) {
		if (StringUtils.isEmpty(content))
			return "";
		else if (content.length() < maxLength)
			return content;
		else
			return content.substring(0, maxLength - 5) + " ...";
	}

	public String exeDescription(ExeObject exeObject) {
		String description = "";
		String file = null;

		if (exeObject.getAction() == null)
			return "";

		String date_time2 = new SimpleDateFormat("ddMMyyyy").format(exeObject.getAction().getCreatedTime());
		if (exeObject.getDetailApp() != null) {
			boolean checkStatus = StringUtils.isNotEmpty(exeObject.getActionModule().getViewStatus()) && StringUtils.isNotEmpty(exeObject.getActionModule().getKeyStatusStart()) && !exeObject.getActionModule().getViewStatus().toUpperCase().equals(Constant.NA_VALUE);
			boolean codetaptrung = (exeObject.getActionModule().getAppTypeCode() == null || !exeObject.getActionModule().getAppTypeCode().equals(Constant.CODETAPTRUNG_TYPE)) ? false : true;

			description = "Module: " + exeObject.getModule().getModuleName() + "\n";
			description += "Ip: " + exeObject.getActionModule().getIpServer() + "\n";
			description += "User: " + exeObject.getActionModule().getInstalledUser() + "\n";
			if(!Util.isNullOrEmpty(exeObject.getActionModule().getInstalledUser()) && exeObject.getActionModule().getInstalledUser().equalsIgnoreCase("root")){
				description += "Login with account monitor after run command su - " + "\n";
			}

//			IimClientService iimService = new IimClientServiceImpl();

			String key;
			String link;
			/*Long appId = exeObject.getActionModule().getModuleId();
			LogOs logOs = null;
			try {
				logOs = iimService.findLogByModule(appId, AamConstants.KEY_LOG_START);
			} catch (AppException e) {
				logger.error(e.getMessage(), e);
			}
			if (logOs != null) {
				key = logOs.getKeyWord();
				link = logOs.getFullPath();
			}*/

			link = exeObject.getActionModule().getLogLink();
			key = exeObject.getActionModule().getKeyword();

			switch (exeObject.getDetailApp().getGroupAction()) {
				case AamConstants.RUN_STEP.STEP_BACKUP:
					String cr = exeObject.getAction().getCrNumber().split("_")[exeObject.getAction().getCrNumber().split("_").length - 1];
					if (AamConstants.OS_TYPE.WINDOWS == exeObject.getActionModule().getOsType()) {
						String copyCmd = "cmd /C xcopy /E /Y /I";
						if (exeObject.getDetailApp().getFile() != null && exeObject.getDetailApp().getFile()) {
							copyCmd = "cmd /C echo F | xcopy /Y";
						}

						description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cmd /C mkdir \"" + exeObject.getActionModule().getPath() + "\\backup_toantrinh\\" + FilenameUtils.getPathNoEndSeparator(exeObject.getDetailApp().getUpcodePath()).replaceAll("\\.\\.\\\\", "") +"\"" + ";" + "\r\n" + copyCmd + " \"" + exeObject.getActionModule().getPath() + "\\" + exeObject.getDetailApp().getUpcodePath() + "\" \"" + exeObject.getActionModule().getPath() + "\\backup_toantrinh\\" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\.\\\\", "")  + "_bk" + date_time2 + "_" + cr + (false ? "_test\"" : "\"");
					} else {
						description += MessageUtil.getResourceBundleMessage("command") + ":\n"
								+ "cd " + exeObject.getActionModule().getPath() + ";\n"
								+ "mkdir -p backup_toantrinh/" + FilenameUtils.getPathNoEndSeparator(exeObject.getDetailApp().getUpcodePath()).replaceAll("\\.\\.", "") + ";"
								+ "\r\n" + "cp -r " + exeObject.getDetailApp().getUpcodePath() + " backup_toantrinh/" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + ";"
								+ "\r\n" + "diff -rq " + exeObject.getDetailApp().getUpcodePath() + " backup_toantrinh/" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + ";"
								+ "\r\n" + (exeObject.getDetailApp().getFile() != null && exeObject.getDetailApp().getFile() ? "ls -l " : "cd ") + "backup_toantrinh/" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr;
					}
					break;
				case AamConstants.RUN_STEP.STEP_STOP:
				case AamConstants.RUN_STEP.STEP_ROLLBACK_STOP:
					if (AamConstants.OS_TYPE.WINDOWS == exeObject.getActionModule().getOsType()) {
						if (codetaptrung) {
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + exeObject.getActionModule().getStopService();
						} else {
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + exeObject.getActionModule().getStopService() + ";\n"
									+ exeObject.getActionModule().getViewStatus() + ";";
						}
					} else {
						if (codetaptrung) {
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStopService();
						} else if (checkStatus) {
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStopService() + ";\n"
									+ exeObject.getActionModule().getViewStatus() + ";";
						} else {
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStopService() + ";\n"
									+ "ps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep; " + "\r\n" + "kill -9 PID;"
									+ "\nps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep; ";
						}
					}
					break;
				case AamConstants.RUN_STEP.STEP_CHECK_STATUS:
					if (AamConstants.OS_TYPE.WINDOWS == exeObject.getActionModule().getOsType()) {
						description += MessageUtil.getResourceBundleMessage("command") + ":\n" + exeObject.getActionModule().getViewStatus();
					} else {
						if (checkStatus) {
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n"
									+ exeObject.getActionModule().getViewStatus();
						} else {
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n"
									+ "ps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep;";
						}
					}
					break;
				case AamConstants.RUN_STEP.STEP_UPCODE:
					cr = exeObject.getAction().getCrNumber().split("_")[exeObject.getAction().getCrNumber().split("_").length - 1];
					file = FilenameUtils.getName(exeObject.getDetailApp().getUploadFilePath());
					if (AamConstants.OS_TYPE.WINDOWS == exeObject.getActionModule().getOsType()) {
						String rmCmd = "";
						if (StringUtils.isNotEmpty(exeObject.getDetailApp().getLstFileRemove())) {
							List<String> removes = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(exeObject.getDetailApp().getLstFileRemove().replaceAll("'", ""));
							List<String> deletes = new ArrayList<>();
							for (String remove : removes) {
								deletes.add("\"" + exeObject.getActionModule().getPath() + "\\" + remove + "\"");
							}
							rmCmd = "cmd /C del /F /Q /S " + Joiner.on(" ").join(deletes);
						}
						String upcodeFullPath = exeObject.getActionModule().getPath() + "\\" + exeObject.getDetailApp().getUpcodePath();
						if (exeObject.getDetailApp().getFile() != null && exeObject.getDetailApp().getFile()) {
							upcodeFullPath = upcodeFullPath.replaceAll("\\\\[^\\\\]*$", "");
						} else {
							upcodeFullPath = upcodeFullPath.replaceAll("\\\\$", "").replaceAll("\\\\[^\\\\]*$", "");
						}
						description += MessageUtil.getResourceBundleMessage("command") + ":\n" + (StringUtils.isNotEmpty(rmCmd) ? rmCmd + ";\n" : "") +
								"\"C:\\Program Files\\WinRAR\\winrar\" x -o \"" + exeObject.getActionModule().getPath() + "\\" + exeObject.getDetailApp().getUploadFilePath() + "\" *.* \"" + upcodeFullPath + "\"";
					} else {
						description += MessageUtil.getResourceBundleMessage("command") + ":\n";
						if(!Util.isNullOrEmpty(exeObject.getActionModule().getInstalledUser()) && exeObject.getActionModule().getInstalledUser().equalsIgnoreCase("root")){
							description += "cd backup_toantrinh_upload_file" + "\n";
							description += "mv 'backup_toantrinh_upload_file' '" + exeObject.getActionModule().getPath() + "'" + "\n";
						}
						description += "cd " + exeObject.getActionModule().getPath() + ";\n" + (StringUtils.isNotEmpty(exeObject.getDetailApp().getLstFileRemove()) ? "rm -rf " + exeObject.getDetailApp().getLstFileRemove().replaceAll(",", "") + ";\n" : "") +
								"unzip -o '" + file + "'" + (StringUtils.isNotEmpty(FilenameUtils.getPathNoEndSeparator(exeObject.getDetailApp().getUpcodePath())) ? " -d " + FilenameUtils.getPathNoEndSeparator(exeObject.getDetailApp().getUpcodePath()) : "") + ";" + "\r\n" + "mv '" + file + "' 'backup_toantrinh/" + FilenameUtils.getPath(exeObject.getDetailApp().getUpcodePath()).replaceAll("\\.\\./", "") + file.substring(0, file.length() - 4)
								+ "_" + cr + "_" + new SimpleDateFormat("ddMMyyyHHmmss").format(new Date()) + "." + FilenameUtils.getExtension(file) + "';";
					}
					break;
				case AamConstants.RUN_STEP.STEP_CLEARCACHE:
				case AamConstants.RUN_STEP.STEP_ROLLBACK_CLEARCACHE:
					if (AamConstants.OS_TYPE.WINDOWS == exeObject.getActionModule().getOsType()) {
						description += MessageUtil.getResourceBundleMessage("command") + ":\n" + exeObject.getActionModule().getDeleteCache() + ";";
					} else {
						description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getDeleteCache() + ";";
					}
					break;
				case AamConstants.RUN_STEP.STEP_RESTART:
				case AamConstants.RUN_STEP.STEP_ROLLBACK_RESTART:
					if (AamConstants.OS_TYPE.WINDOWS == exeObject.getActionModule().getOsType()) {
						if (codetaptrung) {
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + exeObject.getActionModule().getStopService() + ";\r\n" + exeObject.getActionModule().getStartService();
						} else {
							String startCmd = exeObject.getActionModule().getStartService();

							if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
								startCmd += ";\n" + "cmd /C find /i " + "\"" + key + "\" \"" + exeObject.getActionModule().getLogLink() + "\"";
							} else if (checkStatus) {
								startCmd += ";\n" + exeObject.getActionModule().getViewStatus();
							}
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + exeObject.getActionModule().getStopService() + ";\r\n";

							if (StringUtils.isNotEmpty(link)) {
								description += "cmd /C echo F | xcopy /Y \"" + link + "\" \"" + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + "\"" + ";\n"
										+ "cmd /C type NUL > \"" + link + "\"" + ";\n";
							}
							description += startCmd;
						}
					} else {
						if (codetaptrung) {
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStopService() + ";\r\n" + exeObject.getActionModule().getStartService();
						} else {
							String startCmd = exeObject.getActionModule().getStartService();

							if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
								startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
										+ "cat " + link + " | grep -a --color " + "\"" + key + "\";";
							} else if (checkStatus) {
								startCmd += ";\n" + exeObject.getActionModule().getViewStatus();
							} else {
								startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep;";
							}
							if (!checkStatus) {
								description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStopService() + ";\r\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep; " + "\r\n" + "kill -9 PID;\n"
										+ "ps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep;\n";
							}
							if (StringUtils.isNotEmpty(link)) {
								description += "cp " + link + " " + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + ";\n"
										+ "zip -m " + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + ".zip " + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + ";\n" + "> " + link + ";\n";
							}
							description += startCmd;
						}
					}
					break;
				case AamConstants.RUN_STEP.STEP_START:
				case AamConstants.RUN_STEP.STEP_ROLLBACK_START:
					if (AamConstants.OS_TYPE.WINDOWS == exeObject.getActionModule().getOsType()) {
						if (codetaptrung) {
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + exeObject.getActionModule().getStartService();
						} else {
							description += MessageUtil.getResourceBundleMessage("command") + ":\n";
							String startCmd = exeObject.getActionModule().getStartService();
							if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
								startCmd += ";\n" + "cmd /C find /i " + "\"" + key + "\" \"" + exeObject.getActionModule().getLogLink() + "\"";
							} else if (checkStatus) {
								startCmd += ";\n" + exeObject.getActionModule().getViewStatus();
							}
							if (StringUtils.isNotEmpty(link)) {
								description += "cmd /C echo F | xcopy /Y \"" + link + "\" \"" + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + "\"" + ";\n"
										+ "cmd /C type NUL > \"" + link + "\"" + ";\n";
							}
							description += startCmd;
						}
					} else {
						if (codetaptrung) {
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStartService();
						} else {
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n";
							String startCmd = exeObject.getActionModule().getStartService();
							if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
								startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
										+ "cat " + link + " | grep -a --color " + "\"" + key + "\";";
							} else if (checkStatus) {
								startCmd += ";\n" + exeObject.getActionModule().getViewStatus();
							} else {
								startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep;";
							}
							if (StringUtils.isNotEmpty(link)) {
								description += "cp " + link + " " + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + ";\n"
										+ "zip -m " + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + ".zip " + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + ";\n" + "> " + link + ";\n";
							}
							description += startCmd;
						}
					}
					break;
				case AamConstants.RUN_STEP.STEP_RESTART_CMD:
				case AamConstants.RUN_STEP.STEP_ROLLBACK_RESTART_CMD:
					if (AamConstants.OS_TYPE.WINDOWS == exeObject.getActionModule().getOsType()) {
						if (codetaptrung) {
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + exeObject.getActionModule().getRestartService();
						} else {
							String startCmd;
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + exeObject.getActionModule().getRestartService();
							if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
								startCmd = ";\n" + "cmd /C find /i " + "\"" + key + "\" \"" + exeObject.getActionModule().getLogLink() + "\"";
							} else {
								startCmd = ";\n" + exeObject.getActionModule().getViewStatus();
							}
							description += startCmd;
						}
					} else {
						if (codetaptrung) {
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getRestartService();
						} else {
							String startCmd;
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getRestartService();
							if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
								startCmd = ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
										+ "cat " + link + " | grep -a --color " + "\"" + key + "\"";
							} else if (checkStatus) {
								startCmd = ";\n" + exeObject.getActionModule().getViewStatus();
							} else {
								startCmd = ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep;";
							}
							description += startCmd;
						}
					}
					break;
				case AamConstants.RUN_STEP.STEP_ROLLBACKCODE:
					cr = exeObject.getAction().getCrNumber().split("_")[exeObject.getAction().getCrNumber().split("_").length - 1];
					if (AamConstants.OS_TYPE.WINDOWS == exeObject.getActionModule().getOsType()) {
						String copyCmd = "cmd /C xcopy /E /Y /I";
						if (exeObject.getDetailApp().getFile() != null && exeObject.getDetailApp().getFile()) {
							copyCmd = "cmd /C echo F | xcopy /Y";
						}

						description += MessageUtil.getResourceBundleMessage("command") + ":\n";
						description += "cmd /C move /Y \"" + exeObject.getActionModule().getPath() + "\\" + exeObject.getDetailApp().getUpcodePath() + "\" \"" + exeObject.getActionModule().getPath() + "\\backup_toantrinh\\" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\.\\\\", "") + "_fail_" + date_time2 + "_" + cr + "\";\n";
						description += copyCmd + " \""  + exeObject.getActionModule().getPath() +"\\backup_toantrinh\\" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\.\\\\", "") + "_bk" + date_time2 + "_" + cr + "\"" + " \"" + exeObject.getActionModule().getPath() + "\\" + exeObject.getDetailApp().getUpcodePath() + "\"";
					} else {
//						file = FilenameUtils.getName(exeObject.getDetailApp().getUploadFilePath());
						description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n";
						description += "mv " + exeObject.getDetailApp().getUpcodePath() + " backup_toantrinh/" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\./", "") + "_fail_" + date_time2
								+ "_" + cr + ";\n";
						description += "cp -r  backup_toantrinh/" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + " "
								+ exeObject.getDetailApp().getUpcodePath() + ";\n";
						description += "diff -rq backup_toantrinh/" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + " "
								+ exeObject.getDetailApp().getUpcodePath();
					}
					break;
				case AamConstants.RUN_STEP.STEP_CHECKVERSION_APP:
					cr = exeObject.getAction().getCrNumber().split("_")[exeObject.getAction().getCrNumber().split("_").length - 1];
					description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n";
					description += "diff -r " + exeObject.getDetailApp().getUpcodePath() + " backup_toantrinh/" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr;
					break;
				case AamConstants.RUN_STEP.STEP_UPCODE_STOP_START:
					cr = exeObject.getAction().getCrNumber().split("_")[exeObject.getAction().getCrNumber().split("_").length - 1];
					file = FilenameUtils.getName(exeObject.getDetailApp().getUploadFilePath());
					if (AamConstants.OS_TYPE.WINDOWS == exeObject.getActionModule().getOsType()) {
						String rmCmd = "";
						if (StringUtils.isNotEmpty(exeObject.getDetailApp().getLstFileRemove())) {
							List<String> removes = Splitter.on(",").trimResults().omitEmptyStrings().splitToList(exeObject.getDetailApp().getLstFileRemove().replaceAll("'", ""));
							List<String> deletes = new ArrayList<>();
							for (String remove : removes) {
								deletes.add("\"" + exeObject.getActionModule().getPath() + "\\" + remove + "\"");
							}
							rmCmd = "cmd /C del /F /Q /S " + Joiner.on(" ").join(deletes);
						}
						String upcodeFullPath = exeObject.getActionModule().getPath() + "\\" + exeObject.getDetailApp().getUpcodePath();
						if (exeObject.getDetailApp().getFile() != null && exeObject.getDetailApp().getFile()) {
							upcodeFullPath = upcodeFullPath.replaceAll("\\\\[^\\\\]*$", "");
						} else {
							upcodeFullPath = upcodeFullPath.replaceAll("\\\\$", "").replaceAll("\\\\[^\\\\]*$", "");
						}
						description += MessageUtil.getResourceBundleMessage("command") + ":\n" + (StringUtils.isNotEmpty(rmCmd) ? rmCmd + ";\n" : "") +
								"\"C:\\Program Files\\WinRAR\\winrar\" x -o \"" + exeObject.getActionModule().getPath() + "\\" + exeObject.getDetailApp().getUploadFilePath() + "\" *.* \"" + upcodeFullPath + "\";";


						if (codetaptrung) {
							description += "\n" + exeObject.getActionModule().getStopService() + ";";
						} else if (checkStatus) {
							description += "\n" + exeObject.getActionModule().getStopService() + ";\n" + exeObject.getActionModule().getViewStatus() + ";";
						}

						if (codetaptrung) {
							description += "\n" + exeObject.getActionModule().getStartService();
						} else {
							description += "\n";
							String startCmd = exeObject.getActionModule().getStartService();
							if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
								startCmd += ";\n" + "cmd /C find /i " + "\"" + key + "\" \"" + exeObject.getActionModule().getLogLink() + "\"";
							} else if (checkStatus) {
								startCmd += ";\n" + exeObject.getActionModule().getViewStatus();
							}
							if (StringUtils.isNotEmpty(link)) {
								description += "cmd /C echo F | xcopy /Y \"" + link + "\" \"" + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + "\"" + ";\n"
										+ "cmd /C type NUL > \"" + link + "\"" + ";\n";
							}
							description += startCmd;
						}
					} else {
						description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n" + (StringUtils.isNotEmpty(exeObject.getDetailApp().getLstFileRemove()) ? "rm -rf " + exeObject.getDetailApp().getLstFileRemove().replaceAll(",", "") + ";\n" : "") +
								"unzip -o '" + file + "'" + (StringUtils.isNotEmpty(FilenameUtils.getPathNoEndSeparator(exeObject.getDetailApp().getUpcodePath())) ? " -d " + FilenameUtils.getPathNoEndSeparator(exeObject.getDetailApp().getUpcodePath()) : "") + ";" + "\r\n" + "mv '" + file + "' 'backup_toantrinh/" + FilenameUtils.getPath(exeObject.getDetailApp().getUpcodePath()).replaceAll("\\.\\./", "") + file.substring(0, file.length() - 4)
								+ "_" + cr + "_" + new SimpleDateFormat("ddMMyyyHHmmss").format(new Date()) + "." + FilenameUtils.getExtension(file) + "';";

						if (codetaptrung) {
							description += "\n" + exeObject.getActionModule().getStopService() + ";";
						} else if (checkStatus) {
							description += "\n" + exeObject.getActionModule().getStopService() + ";\n" + exeObject.getActionModule().getViewStatus() + ";";
						} else {
							description += "\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStopService() + ";\n"
									+ "ps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep; " + "\r\n" + "kill -9 PID;"
									+ "\nps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep; ";
						}

						if (codetaptrung) {
							description += "\n" + exeObject.getActionModule().getStartService();
						} else {
							description += "\n";
							String startCmd = exeObject.getActionModule().getStartService();
							if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
								startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
										+ "cat " + link + " | grep -a --color " + "\"" + key + "\";";
							} else if (checkStatus) {
								startCmd += ";\n" + exeObject.getActionModule().getViewStatus();
							} else {
								startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep;";
							}
							if (StringUtils.isNotEmpty(link)) {
								description += "cp " + link + " " + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + ";\n"
										+ "zip -m " + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + ".zip " + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + ";\n" + "> " + link + ";\n";
							}
							description += startCmd;
						}
					}
					break;
				case AamConstants.RUN_STEP.STEP_ROLLBACK_CODE_STOP_START:
					cr = exeObject.getAction().getCrNumber().split("_")[exeObject.getAction().getCrNumber().split("_").length - 1];
					if (AamConstants.OS_TYPE.WINDOWS == exeObject.getActionModule().getOsType()) {
						String copyCmd = "cmd /C xcopy /E /Y /I";
						if (exeObject.getDetailApp().getFile() != null && exeObject.getDetailApp().getFile()) {
							copyCmd = "cmd /C echo F | xcopy /Y";
						}

						description += MessageUtil.getResourceBundleMessage("command") + ":\n";
						description += "cmd /C move /Y \"" + exeObject.getActionModule().getPath() + "\\" + exeObject.getDetailApp().getUpcodePath() + "\" \"" + exeObject.getActionModule().getPath() + "\\backup_toantrinh\\" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\.\\\\", "") + "_fail_" + date_time2 + "_" + cr + "\";\n";
						description += copyCmd + " \""  + exeObject.getActionModule().getPath() +"\\backup_toantrinh\\" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\.\\\\", "") + "_bk" + date_time2 + "_" + cr + "\"" + " \"" + exeObject.getActionModule().getPath() + "\\" + exeObject.getDetailApp().getUpcodePath() + "\"";

						if (codetaptrung) {
							description += "\n" + exeObject.getActionModule().getStopService() + ";";
						} else if (checkStatus) {
							description += "\n" + exeObject.getActionModule().getStopService() + ";\n" + exeObject.getActionModule().getViewStatus() + ";";
						}

						if (codetaptrung) {
							description += "\n" + exeObject.getActionModule().getStartService();
						} else {
							description += "\n";
							String startCmd = exeObject.getActionModule().getStartService();
							if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
								startCmd += ";\n" + "cmd /C find /i " + "\"" + key + "\" \"" + exeObject.getActionModule().getLogLink() + "\"";
							} else if (checkStatus) {
								startCmd += ";\n" + exeObject.getActionModule().getViewStatus();
							}
							if (StringUtils.isNotEmpty(link)) {
								description += "cmd /C echo F | xcopy /Y \"" + link + "\" \"" + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + "\"" + ";\n"
										+ "cmd /C type NUL > \"" + link + "\"" + ";\n";
							}
							description += startCmd;
						}
					} else {
						description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n";
						description += "mv " + exeObject.getDetailApp().getUpcodePath() + " backup_toantrinh/" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\./", "") + "_fail_" + date_time2
								+ "_" + cr + ";\n";
						description += "cp -r  backup_toantrinh/" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + " "
								+ exeObject.getDetailApp().getUpcodePath();

						if (codetaptrung) {
							description += "\n" + exeObject.getActionModule().getStopService() + ";";
						} else if (checkStatus) {
							description += "\n" + exeObject.getActionModule().getStopService() + ";\n" + exeObject.getActionModule().getViewStatus() + ";";
						} else {
							description += "\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStopService() + ";\n"
									+ "ps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep; " + "\r\n" + "kill -9 PID;"
									+ "\nps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep; ";
						}

						if (codetaptrung) {
							description += "\n" + exeObject.getActionModule().getStartService();
						} else {
							description += "\n";
							String startCmd = exeObject.getActionModule().getStartService();
							if (StringUtils.isNotEmpty(link) && StringUtils.isNotEmpty(key) && !link.trim().equals(Constant.NA_VALUE) && !key.trim().equals(Constant.NA_VALUE)) {
								startCmd += ";\n" + "tail -500f " + link + " | grep --color " + "\"" + key + "\";\n"
										+ "cat " + link + " | grep -a --color " + "\"" + key + "\";";
							} else if (checkStatus) {
								startCmd += ";\n" + exeObject.getActionModule().getViewStatus();
							} else {
								startCmd += ";\n" + "ps -ef | grep \"" + LinuxParser.getFullPath(exeObject.getActionModule().getPath()) + "\" | grep -v grep;";
							}
							if (StringUtils.isNotEmpty(link)) {
								description += "cp " + link + " " + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + ";\n"
										+ "zip -m " + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + ".zip " + link + "bk_" + exeObject.getAction().getCrNumber() + "_" + date_time2 + ";\n" + "> " + link + ";\n";
							}
							description += startCmd;
						}
					}
					break;
				default:
					break;
			}

		} else if (exeObject.getActionDatabase() != null) {
			description = "DB: " + exeObject.getServiceDb().getDbName() + "\n";
			description += "Ip: " + exeObject.getServiceDb().getIpVirtual() + "\n";
			description += "User: " + exeObject.getServiceDb().getUsername() + "\n";
			if (exeObject.getActionDatabase().getType() == 0) {
				switch (exeObject.getActionDb()) {
					case 0:
						if (exeObject.getActionDatabase().getTypeImport() == 1)
							description += "Script: " + exeObject.getActionDatabase().getScriptBackup() + "\n";
						else
							description += "Script: " + substr(exeObject.getActionDatabase().getBackupText()) + "\n";
						break;
					case 1:
						if (exeObject.getActionDatabase().getTypeImport() == 1)
							description += "Script: " + exeObject.getActionDatabase().getScriptExecute() + "\n";
						else
							description += "Script: " + substr(exeObject.getActionDatabase().getScriptText()) + "\n";
						break;
					case 2:
						if (exeObject.getActionDatabase().getTypeImport() == 1)
							description += "Script: " + exeObject.getActionDatabase().getRollbackFile() + "\n";
						else
							description += "Script: " + substr(exeObject.getActionDatabase().getRollbackText()) + "\n";
						break;
					default:
						break;
				}
			} else if (exeObject.getActionDatabase().getType() == 1) {
				switch (exeObject.getActionDb()) {
					case 0:
						description += "Script: " + exeObject.getActionDatabase().getScriptBackup() + "\n";
						break;
					case 1:
						description += "Import\n";
						description += exeObject.getActionDatabase().getTemplate();
						break;
					case 2:
						description += "Script: " + exeObject.getActionDatabase().getRollbackFile() + "\n";
						break;
					default:
						break;
				}
			} else if (exeObject.getActionDatabase().getType() == 2) {
				if (exeObject.isStartFlag()) {
					if (exeObject.getActionDatabase().getTypeImport() == 1)
						description += "Script: " + exeObject.getActionDatabase().getRollbackFile() + "\n";
					else
						description += "Script: " + substr(exeObject.getActionDatabase().getRollbackText()) + "\n";
				} else {
					if (exeObject.getActionDatabase().getTypeImport() == 1)
						description += "Script: " + exeObject.getActionDatabase().getScriptExecute() + "\n";
					else
						description += "Script: " + substr(exeObject.getActionDatabase().getScriptText()) + "\n";
				}
			}
		} else if (exeObject.getCustomAction() != null) {

			String cr = exeObject.getAction().getCrNumber().split("_")[exeObject.getAction().getCrNumber().split("_").length - 1];
//				String file = null;
			try {
				file = FilenameUtils.getName(exeObject.getCustomAction().getUploadCodePath());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			switch (exeObject.getCustomAction().getType()) {
				case 0:
					description = "Module: " + exeObject.getModule().getModuleName() + "\n";
					description += "Ip: " + exeObject.getActionModule().getIpServer() + "\n";
					description += "User: " + exeObject.getActionModule().getInstalledUser() + "\n";
					switch (exeObject.getCustomAction().getModuleAction()) {
						case 100:
							description += "Upcode test + stop/start\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n" + "mkdir -p backup_toantrinh/" + FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath()).replaceAll("\\.\\.", "") + ";" + "\r\n" + "cp -r " + exeObject.getCustomAction().getUpcodePath() + " backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test"
									+ ";" + "\r\n" + "cd " + "backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test";

							description += ";\n" + (StringUtils.isNotEmpty(exeObject.getCustomAction().getLstFileRemove()) ? "rm -rf " + exeObject.getCustomAction().getLstFileRemove().replaceAll(",", "") + ";\n" : "") +
									"unzip -o '" + file + "'" + (StringUtils.isNotEmpty(FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath())) ? " -d " + FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath()) : "") + ";" + "\r\n" + "mv '" + file + "' 'backup_toantrinh/" + FilenameUtils.getPath(exeObject.getCustomAction().getUpcodePath()).replaceAll("\\.\\./", "") + file.substring(0, file.length() - 4)
									+ "_" + cr + "_test" + "_" + new SimpleDateFormat("ddMMyyyHHmmss").format(new Date()) + "." + FilenameUtils.getExtension(file) + "';";
							description += "\n" + exeObject.getActionModule().getStopService() + "\n" + exeObject.getActionModule().getStartService();
							break;
						case 101:
							description += "Upcode test + restart\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n" + "mkdir -p backup_toantrinh/" + FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath()).replaceAll("\\.\\.", "") + ";" + "\r\n" + "cp -r " + exeObject.getCustomAction().getUpcodePath() + " backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test"
									+ ";" + "\r\n" + "cd " + "backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test";

							description += ";\n" + (StringUtils.isNotEmpty(exeObject.getCustomAction().getLstFileRemove()) ? "rm -rf " + exeObject.getCustomAction().getLstFileRemove().replaceAll(",", "") + ";\n" : "") +
									"unzip -o '" + file + "'" + (StringUtils.isNotEmpty(FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath())) ? " -d " + FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath()) : "") + ";" + "\r\n" + "mv '" + file + "' 'backup_toantrinh/" + FilenameUtils.getPath(exeObject.getCustomAction().getUpcodePath()).replaceAll("\\.\\./", "") + file.substring(0, file.length() - 4)
									+ "_" + cr + "_test" + "_" + new SimpleDateFormat("ddMMyyyHHmmss").format(new Date()) + "." + FilenameUtils.getExtension(file) + "';";
							description += "\n" + exeObject.getActionModule().getRestartService();
							break;
						case 102:
							description += "Upcode test + start\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n" + "mkdir -p backup_toantrinh/" + FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath()).replaceAll("\\.\\.", "") + ";" + "\r\n" + "cp -r " + exeObject.getCustomAction().getUpcodePath() + " backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test"
									+ ";" + "\r\n" + "cd " + "backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test";

							description += ";\n" + (StringUtils.isNotEmpty(exeObject.getCustomAction().getLstFileRemove()) ? "rm -rf " + exeObject.getCustomAction().getLstFileRemove().replaceAll(",", "") + ";\n" : "") +
									"unzip -o '" + file + "'" + (StringUtils.isNotEmpty(FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath())) ? " -d " + FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath()) : "") + ";" + "\r\n" + "mv '" + file + "' 'backup_toantrinh/" + FilenameUtils.getPath(exeObject.getCustomAction().getUpcodePath()).replaceAll("\\.\\./", "") + file.substring(0, file.length() - 4)
									+ "_" + cr + "_test" + "_" + new SimpleDateFormat("ddMMyyyHHmmss").format(new Date()) + "." + FilenameUtils.getExtension(file) + "';";
							description += "\n" + exeObject.getActionModule().getStartService();
							break;
						case AamConstants.ACTION.SPECIAL_UPCODETEST_STOP_START:
							description += "Upcode test + stop/start\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStopService() + "\n;mkdir -p backup_toantrinh/" + FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath()).replaceAll("\\.\\.", "") + ";" + "\r\n" + "cp -r " + exeObject.getCustomAction().getUpcodePath() + " backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test"
									+ ";" + "\r\n" + "cd " + "backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test";

							description += ";\n" + (StringUtils.isNotEmpty(exeObject.getCustomAction().getLstFileRemove()) ? "rm -rf " + exeObject.getCustomAction().getLstFileRemove().replaceAll(",", "") + ";\n" : "") +
									"unzip -o '" + file + "'" + (StringUtils.isNotEmpty(FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath())) ? " -d " + FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath()) : "") + ";" + "\r\n" + "mv '" + file + "' 'backup_toantrinh/" + FilenameUtils.getPath(exeObject.getCustomAction().getUpcodePath()).replaceAll("\\.\\./", "") + file.substring(0, file.length() - 4)
									+ "_" + cr + "_test" + "_" + new SimpleDateFormat("ddMMyyyHHmmss").format(new Date()) + "." + FilenameUtils.getExtension(file) + "';";
							description += "\n" + exeObject.getActionModule().getStartService();
							break;
						case AamConstants.ACTION.SPECIAL_RESTART_STOP_START:
							description += "Stop/start\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStopService() + ";\n" + exeObject.getActionModule().getStartService();
							break;
						case AamConstants.ACTION.SPECIAL_RESTART:
							description += "Restart\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getRestartService() + ";\n";
							break;
						case AamConstants.ACTION.SPECIAL_START:
							description += "Start\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStartService() + ";\n";
							break;
						case 200:
							description += "Rollback code test + stop/start\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n";
							description += "mv " + exeObject.getCustomAction().getUpcodePath() + " backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_fail_" + date_time2
									+ "_" + cr + "_test" + ";\n";
							description += "cp -r  backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test" + " "
									+ exeObject.getCustomAction().getUpcodePath();
							description += "\n" + exeObject.getActionModule().getStopService() + "\n" + exeObject.getActionModule().getStartService();
							break;
						case 201:
							description += "Rollback code test + restart\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n";
							description += "mv " + exeObject.getCustomAction().getUpcodePath() + " backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_fail_" + date_time2
									+ "_" + cr + "_test" + ";\n";
							description += "cp -r  backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test" + " "
									+ exeObject.getCustomAction().getUpcodePath();
							description += "\n" + exeObject.getActionModule().getRestartService();
							break;
						case 202:
							description += "Rollback code test + stop\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n";
							description += "mv " + exeObject.getCustomAction().getUpcodePath() + " backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_fail_" + date_time2
									+ "_" + cr + "_test" + ";\n";
							description += "cp -r  backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test" + " "
									+ exeObject.getCustomAction().getUpcodePath();
							description += "\n" + exeObject.getActionModule().getStopService();
							break;
						case AamConstants.ACTION.SPECIAL_STOP_START:
							description += "Rollback code test + stop/start\n";
							description += exeObject.getActionModule().getStopService() + "\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n";
							description += "mv " + exeObject.getCustomAction().getUpcodePath() + " backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_fail_" + date_time2
									+ "_" + cr + "_test" + ";\n";
							description += "cp -r  backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test" + " "
									+ exeObject.getCustomAction().getUpcodePath();
							description += "\n" + exeObject.getActionModule().getStartService();
							break;
						case AamConstants.ACTION.SPECIAL_ROLLBACK_RESTART_STOP_START:
							description += "Stop/start\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStopService() + ";\n" + exeObject.getActionModule().getStartService();
							break;
						case AamConstants.ACTION.SPECIAL_ROLLBACK_RESTART:
							description += "Restart\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getRestartService() + ";\n";
							break;
						case AamConstants.ACTION.SPECIAL_ROLLBACK_STOP:
							description += "Stop\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStopService() + ";\n";
							break;
						case 1:
							description += MessageUtil.getResourceBundleMessage("stop.process") + "\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStopService();
							break;
						case 2:
							description += "Upcode\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n" + "mkdir -p backup_toantrinh/" + FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath()).replaceAll("\\.\\.", "") + ";" + "\r\n" + "cp -r " + exeObject.getCustomAction().getUpcodePath() + " backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr
									+ ";" + "\r\n" + "cd " + "backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr;

							description += ";\n" + (StringUtils.isNotEmpty(exeObject.getCustomAction().getLstFileRemove()) ? "rm -rf " + exeObject.getCustomAction().getLstFileRemove().replaceAll(",", "") + ";\n" : "") +
									"unzip -o '" + file + "'" + (StringUtils.isNotEmpty(FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath())) ? " -d " + FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath()) : "") + ";" + "\r\n" + "mv '" + file + "' 'backup_toantrinh/" + FilenameUtils.getPath(exeObject.getCustomAction().getUpcodePath()).replaceAll("\\.\\./", "") + file.substring(0, file.length() - 4)
									+ "_" + cr + "_" + new SimpleDateFormat("ddMMyyyHHmmss").format(new Date()) + "." + FilenameUtils.getExtension(file) + "';";
							break;
						case 5:
							description += MessageUtil.getResourceBundleMessage("start.process") + "\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getStartService();
							break;
						case 6:
							description += MessageUtil.getResourceBundleMessage("restart.process") + "\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\ncd " + exeObject.getActionModule().getPath() + ";\n" + exeObject.getActionModule().getRestartService();
							break;
						case 11:
							description += "Upcode test\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n" + "mkdir -p backup_toantrinh/" + FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath()).replaceAll("\\.\\.", "") + ";" + "\r\n" + "cp -r " + exeObject.getCustomAction().getUpcodePath() + " backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test"
									+ ";" + "\r\n" + "cd " + "backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test";

							description += ";\n" + (StringUtils.isNotEmpty(exeObject.getCustomAction().getLstFileRemove()) ? "rm -rf " + exeObject.getCustomAction().getLstFileRemove().replaceAll(",", "") + ";\n" : "") +
									"unzip -o '" + file + "'" + (StringUtils.isNotEmpty(FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath())) ? " -d " + FilenameUtils.getPathNoEndSeparator(exeObject.getCustomAction().getUpcodePath()) : "") + ";" + "\r\n" + "mv '" + file + "' 'backup_toantrinh/" + FilenameUtils.getPath(exeObject.getCustomAction().getUpcodePath()).replaceAll("\\.\\./", "") + file.substring(0, file.length() - 4)
									+ "_" + cr + "_test" + "_" + new SimpleDateFormat("ddMMyyyHHmmss").format(new Date()) + "." + FilenameUtils.getExtension(file) + "';";
							break;
						case 12:

							description += "Rollback code test\n";
							description += MessageUtil.getResourceBundleMessage("command") + ":\n" + "cd " + exeObject.getActionModule().getPath() + ";\n";
							description += "mv " + exeObject.getCustomAction().getUpcodePath() + " backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_fail_" + date_time2
									+ "_" + cr + "_test" + ";\n";
							description += "cp -r  backup_toantrinh/" + exeObject.getCustomAction().getUpcodePath().replaceAll("\\.\\./", "") + "_bk" + date_time2 + "_" + cr + "_test" + " "
									+ exeObject.getCustomAction().getUpcodePath();
							break;
						default:
							break;
					}
					break;
				case 1:
					description = "DB: " + exeObject.getServiceDb().getDbName() + "\n";
					description += "Ip: " + exeObject.getServiceDb().getIpVirtual() + "\n";
					description += "User: " + exeObject.getServiceDb().getUsername() + "\n";
					switch (exeObject.getCustomAction().getDbAction()) {
						case 0:
							description += "Run script\n";
							description += exeObject.getCustomAction().getDbScriptFile();
							break;
						case 1:
							description += "Import\n";
							description += exeObject.getCustomAction().getSqlImport();
							break;
						case 2:
							description += "Export\n";
							description += exeObject.getCustomAction().getExportStatement();
							break;
						default:
							break;
					}
					break;
				case 2:
					description = MessageUtil.getResourceBundleMessage("execute.file") + ":\n" + exeObject.getActionDtFile().getImpactFile() + "\n" + exeObject.getActionDtFile().getImpactDescription();
					break;
				case 3:
					description = MessageUtil.getResourceBundleMessage("pause") + "!!!\n" + exeObject.getCustomAction().getWaitReason();
					break;
				case 4:
					try {
//						ActionService actionService = new ActionServiceImpl();
//						List<String> ipServers = actionService.findIpReboot(exeObject.getAction().getId());
						String ipServer = exeObject.getIpAddress();
//						IimClientService iimClientService = new IimClientServiceImpl();
						/*List<OsAccount> osAccounts = iimClientService.findOsAccount(ipServer);
						OsAccount monitorAccount = null;
						OsAccount rootAccount = null;
						for (OsAccount osAccount : osAccounts) {
                            if (osAccount.getUserType().equals(2)) {
                                monitorAccount = osAccount;
                            } else if (osAccount.getUsername().equals("root")) {
                                rootAccount = osAccount;
                            }
                        }*/

						if (exeObject.getAction() != null && exeObject.getAction().getKbType() != null) {
							setStatusKbType(statusKbType);
							HashMap<Long, String> hashMapStatusKBType = getStatusKbType();
							String actionKbType = "";
							for (Map.Entry<Long, String> entry: hashMapStatusKBType.entrySet()) {
								if (entry.getKey() == exeObject.getAction().getKbType()) {
									actionKbType = entry.getValue();
									break;
								}
							}
							description = actionKbType + "\n";
						}

						description += "Ip: " + ipServer + "\n";
//						description += "User: " + exeObject.get + "\n";
						description += MessageUtil.getResourceBundleMessage("command") + ":";

						// 1: reboot 2: shutdown
						if (exeObject.getAction() != null && exeObject.getAction().getActionRbSd().equals(2l)) {
							description += "\nshutdown;";
						} else {
							description += "\nreboot;";
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}


					break;
				default:
					break;
			}
		}
		return description;
	}

	public void setStatusKbType(HashMap<Long, String> statusKbType) {
		statusKbType.put( AamConstants.KB_TYPE.BD_SERVICE, MessageUtil.getResourceBundleMessage("mop.common.kb_type.bd_service"));
		statusKbType.put( AamConstants.KB_TYPE.BD_SERVER, MessageUtil.getResourceBundleMessage("mop.common.kb_type.bd_server"));
		statusKbType.put( AamConstants.KB_TYPE.UCTT_STOP, MessageUtil.getResourceBundleMessage("mop.common.kb_type.stop"));
		statusKbType.put( AamConstants.KB_TYPE.UCTT_START, MessageUtil.getResourceBundleMessage("mop.common.kb_type.start"));
		statusKbType.put( AamConstants.KB_TYPE.UCTT_RESTART_STOP_START, MessageUtil.getResourceBundleMessage("mop.common.kb_type.stop_start"));
		statusKbType.put( AamConstants.KB_TYPE.UCTT_SW_DB, MessageUtil.getResourceBundleMessage("mop.common.kb_type.sw_db"));
		statusKbType.put( AamConstants.KB_TYPE.UCTT_SW_MODULE, MessageUtil.getResourceBundleMessage("mop.common.kb_type.sw_module"));
		statusKbType.put( AamConstants.KB_TYPE.UC_SERVER, MessageUtil.getResourceBundleMessage("mop.common.kb_type.uc_server"));
		statusKbType.put( AamConstants.KB_TYPE.UCTT_RESTART, MessageUtil.getResourceBundleMessage("mop.common.kb_type.restart"));
		this.statusKbType = statusKbType;
	}

	public HashMap<Long, String> getStatusKbType() {
		return statusKbType;
	}

	private String substr(String script) {
		if (StringUtils.isEmpty(script))
			return "";
		else {
			return script.length() < 512 ? script : script.substring(0, 500) + " ...";
		}
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
			return Array.getLength(o) == 0;
		}
		return false;
	}
	public static <T> boolean hasNotNullValue(List<T> o) {
		for (T value : o) {
			if (value != null) {
				return true;
			}
		}
		return false;
	}
	public static String checkAndPrintObject(Object o){
		try {
			if (isNullOrEmpty(o)) {
				return "null";
			} else {
				return o.toString();
			}
		}catch(Exception ex){
			logger.error(ex.getMessage(),ex);
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

	public static void checkAndPrintObject(org.slf4j.Logger logger1, String title, Object... object) {
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
	public static OsAccount findOSAccountByIpServer(String ipServer) {
		OsAccount monitorAccount = null;
		try {
			IimClientService iimClientService = new IimClientServiceImpl();
			List<OsAccount> osAccounts = iimClientService.findOsAccount(AamConstants.NATION_CODE.VIETNAM, ipServer);
			for (OsAccount osAccount : osAccounts) {
//                if (osAccount.getUserType().equals(2)) {
				monitorAccount = osAccount;
//                }
			}
			return monitorAccount;
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
	}
}
