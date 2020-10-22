package com.viettel.controller;

import java.io.*;
import java.util.*;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.event.AjaxBehaviorEvent;
import javax.faces.model.SelectItem;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.Multimap;
import com.rits.cloning.Cloner;
import com.viettel.exception.AppException;
import com.viettel.persistence.*;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.model.FileHeader;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.docx4j.wml.SdtPr.Citation;

import com.viettel.common.util.MessageUtil;
import com.viettel.model.Action;
import com.viettel.model.ActionDetailDatabase;
import com.viettel.model.ActionHistory;
import com.viettel.model.ActionModule;
import com.viettel.util.Constant;
import com.viettel.util.UploadFileUtils;
import org.primefaces.model.DefaultStreamedContent;
import org.primefaces.model.StreamedContent;

@ManagedBean
@SessionScoped
public class HistoryDetailController implements Serializable {
	private static Logger logger = LogManager.getLogger(HistoryDetailController.class);

	@ManagedProperty(value = "#{actionDetailDatabaseService}")
	ActionDetailDatabaseService actionDetailDatabaseService;

	@ManagedProperty(value = "#{actionDetailAppService}")
	ActionDetailAppService actionDetailAppService;

	@ManagedProperty(value = "#{actionModuleService}")
	ActionModuleService actionModuleService;

	public void setIimService(IimService iimService) {
		this.iimService = iimService;
	}

	@ManagedProperty(value = "#{iimService}")
	IimService iimService;

	@ManagedProperty(value = "#{actionService}")
	ActionService actionService;

	public void setActionService(ActionService actionService) {
		this.actionService = actionService;
	}

	/*20181115_hoangnd_save all step_start*/
	ExecuteChecklistController executeChecklistController;

	public ExecuteChecklistController getExecuteChecklistController() {
		return executeChecklistController;
	}

	public void setExecuteChecklistController(ExecuteChecklistController executeChecklistController) {
		this.executeChecklistController = executeChecklistController;
	}

	@ManagedProperty(value = "#{aomService}")
	AomClientService aomClientService;

	public AomClientService getAomClientService() {
		return aomClientService;
	}

	public void setAomClientService(AomClientService aomClientService) {
		this.aomClientService = aomClientService;
	}
	/*20181115_hoangnd_save all step_end*/

/*private List<ExeObject> stopObjects;
	private List<ExeObject> backupObjects;
	private List<ExeObject> backupDbObjects;
	private List<ExeObject> upcodeObjects;
	private List<ExeObject> executeDbObjects;
	private List<ExeObject> clearCacheObjects;
	private List<ExeObject> restartObjects;
	private List<ExeObject> startObjects;
	private List<ExeObject> checkStatusObjects;*/

	private Multimap<MapEntry, ExeObject> impactObjects;
	private Multimap<MapEntry, ExeObject> rollbackObjects;

	private List<SelectItem> runSteps=new ArrayList<>();
	private Map<MapEntry, Integer> stepResult = new HashMap<>();
	private RunStep selectedRunStep;
	private String scriptDetail;
	private List<SelectItem> statusFitterList =Constant.getStatusList();
	private Action selectedAction;

	private String logDetail;

	private List<Integer> kbGroups;

	/*20181115_hoangnd_save all step_start*/
	boolean change;

	public boolean checkRender(int step) {
		if (this.selectedRunStep != null && (int)this.selectedRunStep.getValue().getKey() == step)
			return true;
		return false;
	}
    /*20181115_hoangnd_save all step_end*/

	public StreamedContent downloadLogFile(ExeObject exeObject){
		try {
			File dir = new File(UploadFileUtils.getLogFolder(exeObject.getAction()) + ".zip");
			String logFile = null;
			if (exeObject.getDetailApp() != null) {
				switch (exeObject.getDetailApp().getGroupAction()) {
					case Constant.STEP_CHECK_STATUS:
						logFile = "tacdong" + File.separator + "status";
						break;
					case Constant.STEP_STOP:
						logFile = "tacdong" + File.separator + "stop";
						break;
					case Constant.STEP_ROLLBACK_STOP:
						logFile = "rollback" + File.separator + "stop";
						break;
					case Constant.STEP_BACKUP:
						logFile = "tacdong" + File.separator + "backup";
						break;
					case Constant.STEP_UPCODE:
						logFile = "tacdong" + File.separator + "upcode";
						break;
					case Constant.STEP_CLEARCACHE:
						logFile = "tacdong" + File.separator + "clearcache";
						break;
					case Constant.STEP_ROLLBACK_CLEARCACHE:
						logFile = "rollback" + File.separator + "clearcache";
						break;
					case Constant.STEP_RESTART:
						logFile = "tacdong" + File.separator + "restart";
						break;
					case Constant.STEP_ROLLBACK_RESTART:
						logFile = "rollback" + File.separator + "restart";
						break;
					case Constant.STEP_RESTART_CMD:
						logFile = "tacdong" + File.separator + "restart";
						break;
					case Constant.STEP_ROLLBACK_RESTART_CMD:
						logFile = "rollback" + File.separator + "restart";
						break;
					case Constant.STEP_START:
						logFile = "tacdong" + File.separator + "start";
						break;
					case Constant.STEP_ROLLBACK_START:
						logFile = "rollback" + File.separator + "start";
						break;
					case Constant.STEP_ROLLBACKCODE:
						logFile = "rollback" + File.separator + "code";
						break;
					case Constant.STEP_CHECKVERSION_APP:
						logFile = "sautacdong" + File.separator + "checkversion";
						break;
					default:
						break;
				}

				if (Arrays.asList(Constant.STEP_ROLLBACKCODE, Constant.STEP_BACKUP, Constant.STEP_UPCODE, Constant.STEP_CHECKVERSION_APP).contains(exeObject.getDetailApp().getGroupAction())) {
					logFile += File.separator + exeObject.getActionModule().getInstalledUser() + "_" + exeObject.getActionModule().getIpServer() + "_" + exeObject.getActionModule().getAppCode() + "_" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\./", "").replaceAll("/", "_") + ".log";
				} else {
					logFile += File.separator + exeObject.getActionModule().getInstalledUser() + "_" + exeObject.getActionModule().getIpServer() + "_" + exeObject.getActionModule().getAppCode() + ".log";
				}
			} else if (exeObject.getActionDatabase() != null) {
				if (exeObject.getActionDb() == 0) {
					logFile = "tacdong" + File.separator + "backupdb";
				} else if (exeObject.getActionDb() == 1) {
					logFile = "tacdong" + File.separator + "tddb";
				} else if (exeObject.getActionDb() == 2) {
					logFile = "rollback" + File.separator + "rollbackdb";
				}

				logFile += File.separator + exeObject.getServiceDb().getUsername() + "_" + exeObject.getServiceDb().getUsername() + "_" + exeObject.getServiceDb().getIpVirtual() + "_" + exeObject.getActionDatabase().getId() + ".log";
			}

			if(!dir.exists()){
				/*20181023_hoangnd_fix bug exception khong load duoc mess_start*/
//				logger.info("khong tim thay dir : " + dir);
//				logger.info("khong tim thay file : " + logFile);
//            	MessageUtil.setErrorMessage(MessageUtil.getResourceBundleMessage("not.found.folder.contains.file"));
				com.viettel.it.util.MessageUtil.setErrorMessageFromRes("not.found.folder.contains.file");
				/*20181023_hoangnd_fix bug exception khong load duoc mess_end*/
				return null;
			}

			ZipFile zipFile = new ZipFile(dir);
			FileHeader fileHeader = zipFile.getFileHeader(logFile);

			if (fileHeader != null) {
				InputStream inputStream = zipFile.getInputStream(fileHeader);
				StreamedContent dFile = new DefaultStreamedContent(inputStream, "", FilenameUtils.getName(logFile));
				return dFile;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}

		return null;
	}

	/*20181213_hoangnd_view log tu file log va file temp_start*/
	public void viewLogDetail(ExeObject exeObject){
		viewLogFile(exeObject);
		if(StringUtils.isBlank(logDetail) && exeObject.getActionDatabase() != null && exeObject.getActionDatabase().getTempFile().length() > 0) {
			viewTempFile(exeObject);
		}
	}

	public void viewLogFile(ExeObject exeObject) {
		logDetail = "";
		try {
			/*20181120_hoangnd_save all step_start*/
//			File dir = new File(UploadFileUtils.getLogFolder(exeObject.getAction()) + ".zip");
			File dir = new File(UploadFileUtils.getLogFolder(exeObject.getAction()));
			/*20181120_hoangnd_save all step_end*/
			String logFile = null;
			if (exeObject.getDetailApp() != null) {
				switch (exeObject.getDetailApp().getGroupAction()) {
					case Constant.STEP_CHECK_STATUS:
						logFile = "tacdong" + File.separator + "status";
						break;
					//11-12-2018 KienPD start
					case Constant.STEP_ROLLBACK_CHECK_STATUS:
						logFile = "rollback" + File.separator + "status";
						break;
					//11-12-2018 KienPD end
					case Constant.STEP_STOP:
						logFile = "tacdong" + File.separator + "stop";
						break;
					case Constant.STEP_ROLLBACK_STOP:
						logFile = "rollback" + File.separator + "stop";
						break;
					case Constant.STEP_BACKUP:
						logFile = "tacdong" + File.separator + "backup";
						break;
					case Constant.STEP_UPCODE:
						logFile = "tacdong" + File.separator + "upcode";
						break;
					case Constant.STEP_CLEARCACHE:
						logFile = "tacdong" + File.separator + "clearcache";
						break;
					case Constant.STEP_ROLLBACK_CLEARCACHE:
						logFile = "rollback" + File.separator + "clearcache";
						break;
					case Constant.STEP_RESTART:
						logFile = "tacdong" + File.separator + "restart";
						break;
					case Constant.STEP_ROLLBACK_RESTART:
						logFile = "rollback" + File.separator + "restart";
						break;
					case Constant.STEP_RESTART_CMD:
						logFile = "tacdong" + File.separator + "restart";
						break;
					case Constant.STEP_ROLLBACK_RESTART_CMD:
						logFile = "rollback" + File.separator + "restart";
						break;
					case Constant.STEP_START:
						logFile = "tacdong" + File.separator + "start";
						break;
					case Constant.STEP_ROLLBACK_START:
						logFile = "rollback" + File.separator + "start";
						break;
					case Constant.STEP_ROLLBACKCODE:
						logFile = "rollback" + File.separator + "code";
						break;
					case Constant.STEP_CHECKVERSION_APP:
						logFile = "sautacdong" + File.separator + "checkversion";
						break;
                    /*20181225_hoangnd_them buoc upcode special_start*/
					case Constant.STEP_UPCODE_STOP_START:
						logFile = "tacdong" + File.separator + "upcode_stop_start";
						break;
					case Constant.STEP_ROLLBACK_CODE_STOP_START:
						logFile = "rollback" + File.separator + "code_stop_start";
						break;
                    /*20181225_hoangnd_them buoc upcode special_end*/
					default:
						break;
				}

				if (Arrays.asList(Constant.STEP_ROLLBACKCODE, Constant.STEP_BACKUP, Constant.STEP_UPCODE, Constant.STEP_CHECKVERSION_APP).contains(exeObject.getDetailApp().getGroupAction())) {
					logFile += File.separator + exeObject.getActionModule().getInstalledUser() + "_" + exeObject.getActionModule().getIpServer() + "_" + exeObject.getActionModule().getAppCode() + "_" + exeObject.getDetailApp().getUpcodePath().replaceAll("\\.\\./", "").replaceAll("/", "_") + ".log";
				} else {
					logFile += File.separator + exeObject.getActionModule().getInstalledUser() + "_" + exeObject.getActionModule().getIpServer() + "_" + exeObject.getActionModule().getAppCode() + ".log";
				}
			} else if (exeObject.getActionDatabase() != null) {
				if (exeObject.getActionDb() == 0) {
					logFile = "tacdong" + File.separator + "backupdb";
				} else if (exeObject.getActionDb() == 1) {
					logFile = "tacdong" + File.separator + "tddb";
				} else if (exeObject.getActionDb() == 2) {
					logFile = "rollback" + File.separator + "rollbackdb";
				}

				logFile += File.separator + exeObject.getServiceDb().getUsername() + "_" + exeObject.getServiceDb().getDbName() + "_" + exeObject.getServiceDb().getIpVirtual() + "_" + exeObject.getActionDatabase().getId() + ".log";
			}

			/*20181120_hoangnd_save all step_start*/

			/*20190218_hoangnd_fix bug view log file_start*/
			File dirFile = new File(dir + File.separator + logFile);
			if(!dirFile.exists()) {
			/*20190218_hoangnd_fix bug view log file_end*/
				logger.info("Not found " + dir);
				dir = new File(UploadFileUtils.getLogFolder(exeObject.getAction()) + ".zip");
				if(!dir.exists()) {
					logger.info("Not found " + dir);
					com.viettel.it.util.MessageUtil.setErrorMessageFromRes("not.found.folder.contains.file");
					return;
				}

				logger.info("Found " + dir);
				ZipFile zipFile = new ZipFile(dir);
				FileHeader fileHeader = zipFile.getFileHeader(logFile);

				if (fileHeader != null) {
					InputStream inputStream = zipFile.getInputStream(fileHeader);
//				StreamedContent dFile = new DefaultStreamedContent(inputStream, "", FilenameUtils.getName(logFile));
					logDetail = IOUtils.toString(inputStream);

					if (inputStream != null) {
						inputStream.close();
					}
				}
				return;
			}

			logger.info("Start open file : " + dir + File.separator + logFile);
			InputStream inputStream = new FileInputStream(dir + File.separator + logFile);
			logDetail = IOUtils.toString(inputStream);

			if (inputStream != null) {
				inputStream.close();
			}
			logger.info("End open file : " + dir + File.separator + logFile);
			return;
			/*20181120_hoangnd_save all step_end*/

		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return;
	}

	public void viewTempFile(ExeObject exeObject) {
		logDetail = "";
		try {
			File dir = new File(exeObject.getActionDatabase().getTempFile());

			if(!dir.exists()){
				logger.info("Not found " + dir);
				com.viettel.it.util.MessageUtil.setErrorMessageFromRes("not.found.folder.contains.file");
				return;
			}

			logger.info("open file : " + dir);
			InputStream inputStream = new FileInputStream(dir);
			logDetail = IOUtils.toString(inputStream);

			if (inputStream != null) {
				inputStream.close();
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	/*20181213_hoangnd_view log tu file log va file temp_start*/

	public void handleChange(AjaxBehaviorEvent event) {
		/*20181115_hoangnd_save all step_start*/
		change = true;
		/*20181115_hoangnd_save all step_end*/
	}

	private void setupStatus(Collection<ExeObject> listObj,Integer subStep , Integer kbGroup){
		MapEntry mapEntry = new MapEntry(subStep, kbGroup);
		//05-12-2018 KienPD check icon status start
	   /*boolean standBy = false;
	   boolean running = false;
	   boolean finishWarning =false;
	   boolean finishSuccess =false;
	   for(ExeObject obj : listObj){
		   if (obj.getRunStt() == null) {
			   standBy = true;
		   } else if(obj.getRunStt() == Constant.FINISH_FAIL_STATUS){
			   this.stepResult.put(mapEntry, Constant.FINISH_FAIL_STATUS);
			   return ;
		   }else if(obj.getRunStt() == Constant.STAND_BY_STATUS){
			   standBy = true;
		   }else if(obj.getRunStt() == Constant.RUNNING_STATUS){
			   running = true;
		   }else if(obj.getRunStt() == Constant.FINISH_SUCCESS_WITH_WARNING || obj.getRunStt() == AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER){
			   finishWarning = true;
		   }else if(obj.getRunStt() == Constant.FINISH_SUCCESS_STATUS){
			   finishSuccess = true;
		   }
	   }

	   if(running){
		   this.stepResult.put(mapEntry, Constant.RUNNING_STATUS);
		   return ;
	   }else if(standBy) {
		   this.stepResult.put(mapEntry, Constant.STAND_BY_STATUS);
		   return ;
	   }else if(finishWarning) {
		   this.stepResult.put(mapEntry, Constant.FINISH_SUCCESS_WITH_WARNING);
		   return ;
	   }else if(finishSuccess) {
		   this.stepResult.put(mapEntry, Constant.FINISH_SUCCESS_STATUS);
		   return ;
	   }*/
			if (listObj.isEmpty()) {
			return;
		}
		int size = listObj.size();
		if (size == 1) {
			stepResult.put(mapEntry, listObj.iterator().next().getRunStt() == null ? 0 : listObj.iterator().next().getRunStt());
			return;
		} else {
			int cntNotRun = 0;
			int cntSuccess = 0;
			int cntWarning = 0;
			int cntRunning = 0;
			for (ExeObject obj : listObj) {
				if (obj != null && obj.getRunStt() != null){
					switch (obj.getRunStt()){
						case -2:
							stepResult.put(mapEntry, Constant.FINISH_FAIL_STATUS);
							return;
						case 0:
							cntNotRun++;
							break;
						case 1:
							cntRunning++;
							break;
						case 2:
							cntSuccess++;
							break;
						case 3:
							cntWarning++;
							break;
						case 4:
							cntWarning++;
							break;
						default:
							break;
					}
				}
			}
			if (cntNotRun > 0) {
				stepResult.put(mapEntry, Constant.STAND_BY_STATUS);
			} else if (cntRunning > 0 && cntNotRun == 0) {
				stepResult.put(mapEntry, Constant.RUNNING_STATUS);
			} else if (cntNotRun == 0 && cntRunning == 0 && cntWarning == 0 && cntSuccess > 0) {
				stepResult.put(mapEntry, Constant.FINISH_SUCCESS_STATUS);
			} else if (cntNotRun == 0 && cntRunning == 0 && cntWarning > 0) {
				stepResult.put(mapEntry, Constant.FINISH_SUCCESS_WITH_WARNING);
			}
			return;
		}
		//05-12-2018 KienPD check icon status end
	}


	public void exeScriptDetail(ExeObject exeObject, Integer type) {
		switch (type) {
			case 0:
				scriptDetail = exeObject.getActionDatabase().getBackupText();
				break;
			case 1:
				scriptDetail = exeObject.getActionDatabase().getScriptText();
				break;
			case 2:
				scriptDetail = exeObject.getActionDatabase().getRollbackText();
				break;
			default:
				scriptDetail = "";
				break;
		}
	}

	public String statusIcon(MapEntry step) {
		String fa = "";

		Integer status = stepResult.get(step);

		if (status == null)
			return "";

		switch (status) {
			case Constant.STAND_BY_STATUS:
				break;
			case Constant.RUNNING_STATUS:
				fa = " fa-forward Yellow";
				break;
			case Constant.FINISH_SUCCESS_STATUS:
				fa = " fa-check Green";
				break;
			case Constant.FINISH_FAIL_STATUS:
				fa = " fa-close Red";
				break;
			case Constant.FINISH_SUCCESS_WITH_WARNING:
				fa = " fa-warning Yellow";
				break;
			//05-12-2018 KienPD add icon FAIL_BUT_SKIPED_BY_USER start
			case AamConstants.RUN_STATUS.FAIL_BUT_SKIPED_BY_USER:
				fa = " fa-warning Yellow";
				break;
			//05-12-2018 KienPD add icon FAIL_BUT_SKIPED_BY_USER end
			default:
				break;

		}

		return fa;
	}

	private void setupRunStep() {
		this.runSteps.clear();
		try {
			/*20181115_hoangnd_save all step_start*/
			MapEntry mapEntry;
			RunStep step;
			SelectItem item;

			mapEntry = new MapEntry(-5, 1);
			step = Constant.getRunStep(mapEntry);
			item = new SelectItem(step, step.getLabel());
			item.setDisabled(true);
			runSteps.add(item);

			mapEntry = new MapEntry(30, 1);
			step = Constant.getRunStep(mapEntry);
			runSteps.add(new SelectItem(step, step.getLabel()));

			mapEntry = new MapEntry(-1, 1);
			item = new SelectItem(Constant.getSteps().get(mapEntry), Constant.getSteps().get(mapEntry).getLabel());
			item.setDisabled(true);
			runSteps.add(item);

			/*if (!executeChecklistController.getCklAppBefore().isEmpty()) {
				mapEntry = new MapEntry(1, 1);
				item = new SelectItem(Constant.getSteps().get(mapEntry), Constant.getSteps().get(mapEntry).getLabel());
				runSteps.add(item);
				stepResult.put(mapEntry, Constant.STAND_BY_STATUS);
			}

			if (!executeChecklistController.getCklDbBefore().isEmpty()) {
				mapEntry = new MapEntry(2, 1);
				runSteps.add(new SelectItem(Constant.getSteps().get(mapEntry), Constant.getSteps().get(mapEntry).getLabel()));
				stepResult.put(mapEntry, Constant.STAND_BY_STATUS);
			}*/
			for (Integer kbGroup : kbGroups) {
				mapEntry = new MapEntry(AamConstants.ACTION.BEFORE_STEP_CHECKLIST_APP, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.BEFORE_STEP_CHECKLIST_DB, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}
			}

			mapEntry = new MapEntry(-2, 1);
			step = Constant.getRunStep(mapEntry);
			item = new SelectItem(step, step.getLabel());
			item.setDisabled(true);
			runSteps.add(item);
			/*20181115_hoangnd_save all step_end*/

			for (Integer kbGroup : kbGroups) {
				/*20181115_hoangnd_save all step_start*/
				mapEntry = new MapEntry(AamConstants.ACTION.SUB_STEP_CHECK_STATUS, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}
				/*20181115_hoangnd_save all step_end*/

				mapEntry = new MapEntry(AamConstants.ACTION.SUB_STEP_STOP_APP, kbGroup);
//				if (!stopObjects.isEmpty()) {
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.SUB_STEP_BACKUP_APP, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.SUB_STEP_BACKUP_DB, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.SUB_STEP_UPCODE, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.SUB_STEP_TD_DB, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.SUB_STEP_CLEARCACHE, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.SUB_STEP_RESTART_APP, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.SUB_STEP_START_APP, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				/*20181115_hoangnd_save all step_start*/
				mapEntry = new MapEntry(AamConstants.ACTION.SUB_STEP_UPCODE_START_APP, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.SUB_STEP_CHECKLIST_APP, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.SUB_STEP_CHECKLIST_DB, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}
				/*20181115_hoangnd_save all step_end*/
			}

			/*20181115_hoangnd_save all step_start*/
			mapEntry = new MapEntry(-3, 1);
			item = new SelectItem(Constant.getSteps().get(mapEntry), Constant.getSteps().get(mapEntry).getLabel());
			item.setDisabled(true);
			runSteps.add(item);

			for (Integer kbGroup : kbGroups) {
				mapEntry = new MapEntry(AamConstants.ACTION.AFTER_STEP_CHECKLIST_APP, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.AFTER_STEP_CHECKLIST_DB, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}
			}
			/*20181115_hoangnd_save all step_end*/

			mapEntry = new MapEntry(-4, 1);
			step = Constant.getRunStep(mapEntry);
			item = new SelectItem(step, step.getLabel());
			item.setDisabled(true);
			runSteps.add(item);

			for (Integer kbGroup : kbGroups) {
				//05-12-2018 KienPD add step check status start
				mapEntry = new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECK_STATUS, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}
				//05-12-2018 KienPD add step check status end

				mapEntry = new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_STOP_APP, kbGroup);
//				if (!stopObjects.isEmpty()) {
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_SOURCE_CODE, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_DB, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}


				mapEntry = new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CLEARCACHE, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_RESTART_APP, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_START_APP, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				/*20181115_hoangnd_save all step_start*/
				mapEntry = new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_UPCODE_START, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_APP, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}

				mapEntry = new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_DB, kbGroup);
				if (!impactObjects.get(mapEntry).isEmpty()) {
					step = Constant.getRunStep(mapEntry);
					runSteps.add(new SelectItem(step, step.getLabel()));
				}
				/*20181115_hoangnd_save all step_end*/
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void setup(ActionHistory history) {
		this.clear();

		try {
			kbGroups = actionService.findKbGroups(history.getAction().getId());
		} catch (AppException e) {
			logger.error(e.getMessage(), e);
		}

		if (kbGroups == null)
			kbGroups = new ArrayList<>();
		if (!kbGroups.contains(1)) {
			kbGroups.add(1);
		}
		if (kbGroups.contains(null))
			kbGroups.remove(null);

		/*HashMap<String, List<ExeObject>> appMap = new HashMap<>();
		appMap.put(Constant.STEP_STOP, this.stopObjects);
		// appMap.put(Constant.STEP_UPCODE, this.upcodeObjects);
		appMap.put(Constant.STEP_CLEARCACHE, this.clearCacheObjects);
		// appMap.put(Constant.STEP_RESTART, this.restartObjects);
		appMap.put(Constant.STEP_START, this.startObjects);
		appMap.put(Constant.STEP_CHECK_STATUS, this.checkStatusObjects);*/

//		Cloner cloner = new Cloner();

		try {
			this.selectedAction = history.getAction();
			if (this.selectedAction == null){
				MessageUtil.setErrorMessage("Unknown Action");
				return;
			}
			/*20181116_hoangnd_save all step_start*/
			if(selectedAction!=null)
				selectedAction.setTestbedMode(false);
			/*20181116_hoangnd_save all step_end*/

			List<ActionDetailApp> appList = actionDetailAppService.findListDetailApp(this.selectedAction.getId());
			List<ActionDetailDatabase> dbList = actionDetailDatabaseService.findListDetailDb(this.selectedAction.getId(), null,false, true);

			/*20190130_hoangnd_khong hien thi lich su neu buoc rollback db chua dc chay_start*/
			boolean check = false;
			for (ActionDetailDatabase database : dbList) {
				if (database != null && database.getRollbackStatus() != null){
					if (database.getRollbackStatus().intValue() == -2 || database.getRollbackStatus().intValue() == 2
							|| database.getRollbackStatus().intValue() == 4) {
						check = true;
						break;
					}
				}
			}
			/*20190130_hoangnd_khong hien thi lich su neu buoc rollback db chua dc chay_end*/

			for (ActionDetailApp detailApp : appList) {
				String groupAction = detailApp.getGroupAction();
				/*20181115_hoangnd_save all step_start*/
				ActionModule actionModule = new ActionModule();
				Module app = new Module();
				if(detailApp.getModuleId() != null && !detailApp.getModuleId().equals(0L)) {
					app = iimService.findModuleById(selectedAction.getImpactProcess().getNationCode(), detailApp.getModuleId());
					if (app == null)
						continue;
					actionModule = this.actionModuleService.findModule(this.selectedAction.getId(), app.getModuleId());
					if (actionModule == null)
						continue;
				}
				/*20181115_hoangnd_save all step_end*/
				Cloner cloner = new Cloner();
//				ActionDetailApp rbDetailApp = cloner.deepClone(detailApp);

				if (groupAction.equals(Constant.STEP_RESTART) || groupAction.equals(Constant.STEP_RESTART_CMD)) {
					ExeObject o = new ExeObject();
					o.setModule(app);
					o.setActionModule(actionModule);
					o.setAction(this.selectedAction);
					o.setDetailApp(detailApp);
					o.setBeginDate(detailApp.getRunStartTime());
					o.setEndDate(detailApp.getRunEndTime());
					o.setRunStt(detailApp.getRunStatus());
//					this.restartObjects.add(o);
					impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), o);

					//05-12-2018 KienPD check add step rollback start
					if (detailApp.getRunStatus() != null && (detailApp.getRunStatus().intValue() == 2 ||detailApp.getRunStatus().intValue() == 3 ||detailApp.getRunStatus().intValue() == 4)
							|| (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().equals(1))) {
						ActionDetailApp actionDetailApp = cloner.deepClone(detailApp);

						o = new ExeObject();
						o.setModule(app);
						actionDetailApp.setGroupAction(Constant.STEP_ROLLBACK_RESTART);
						o.setDetailApp(actionDetailApp);
						o.setActionModule(actionModule);
						o.setAction(this.selectedAction);
						o.setBeginDate(actionDetailApp.getRollbackStartTime());
						o.setEndDate(actionDetailApp.getRollbackEndTime());
						o.setRunStt(actionDetailApp.getRollbackStatus());
						impactObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_RESTART_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), o);
					}
					//05-12-2018 KienPD check add step rollback end
				} else if (groupAction.equals(Constant.STEP_UPCODE)) {
					ActionDetailApp actionDetailApp = cloner.deepClone(detailApp);
					actionDetailApp.setGroupAction(Constant.STEP_BACKUP);

					ExeObject backupAppObj = new ExeObject();
					backupAppObj.setModule(app);
					backupAppObj.setDetailApp(actionDetailApp);
					backupAppObj.setActionModule(actionModule);
					backupAppObj.setAction(this.selectedAction);
					backupAppObj.setBeginDate(actionDetailApp.getBackupStartTime());
					backupAppObj.setEndDate(actionDetailApp.getBackupEndTime());
					backupAppObj.setRunStt(actionDetailApp.getBackupStatus());

//					this.backupObjects.add(backupAppObj);
					impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_BACKUP_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), backupAppObj);

					ExeObject upcodeAppObj = new ExeObject();
					upcodeAppObj.setModule(app);
					upcodeAppObj.setDetailApp(detailApp);
					upcodeAppObj.setActionModule(actionModule);
					upcodeAppObj.setAction(this.selectedAction);
					upcodeAppObj.setBeginDate(detailApp.getRunStartTime());
					upcodeAppObj.setEndDate(detailApp.getRunEndTime());
					upcodeAppObj.setRunStt(detailApp.getRunStatus());
//					this.upcodeObjects.add(upcodeAppObj);
					impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_UPCODE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), upcodeAppObj);


					//05-12-2018 KienPD check add step rollback start
					if (detailApp.getRunStatus() != null && (detailApp.getRunStatus().intValue() == 2 || detailApp.getRunStatus().intValue() == 3 || detailApp.getRunStatus().intValue() == 4)
							|| (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().equals(1))) {
						actionDetailApp = cloner.deepClone(detailApp);

						upcodeAppObj = new ExeObject();
						upcodeAppObj.setModule(app);
						actionDetailApp.setGroupAction(Constant.STEP_ROLLBACKCODE);
						upcodeAppObj.setDetailApp(actionDetailApp);
						upcodeAppObj.setActionModule(actionModule);
						upcodeAppObj.setAction(this.selectedAction);
						upcodeAppObj.setBeginDate(actionDetailApp.getRollbackStartTime());
						upcodeAppObj.setEndDate(actionDetailApp.getRollbackEndTime());
						upcodeAppObj.setRunStt(actionDetailApp.getRollbackStatus());

						impactObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_SOURCE_CODE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), upcodeAppObj);
					}
				} else {
					ExeObject obj = new ExeObject();
					obj.setModule(app);
					obj.setDetailApp(detailApp);
					obj.setActionModule(actionModule);
					obj.setAction(this.selectedAction);
					obj.setBeginDate(detailApp.getRunStartTime());
					obj.setEndDate(detailApp.getRunEndTime());
					obj.setRunStt(detailApp.getRunStatus());

					if (groupAction.equals(Constant.STEP_CHECK_STATUS)) {
						impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CHECK_STATUS, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);
						/*20181225_hoangnd_khong add buoc check status vao rollback_start*/
						//05-12-2018 KienPD add step check status start
						/*if (detailApp.getRunStatus() != null || (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().equals(1))) {
							ActionDetailApp actionDetailApp = cloner.deepClone(detailApp);

							obj = new ExeObject();
							obj.setModule(app);
							actionDetailApp.setGroupAction(Constant.STEP_ROLLBACK_CHECK_STATUS);
							obj.setDetailApp(actionDetailApp);
							obj.setActionModule(actionModule);
							obj.setAction(this.selectedAction);
							obj.setBeginDate(actionDetailApp.getRollbackStartTime());
							obj.setEndDate(actionDetailApp.getRollbackEndTime());
							obj.setRunStt(actionDetailApp.getRollbackStatus());

							impactObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECK_STATUS, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);
						}*/
						//05-12-2018 KienPD add step check status end
						/*20181225_hoangnd_khong add buoc check status vao rollback_end*/
					} else if (groupAction.equals(Constant.STEP_STOP)) {
						impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_STOP_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);

						//05-12-2018 KienPD check add step rollback start
						if (detailApp.getRunStatus() != null && (detailApp.getRunStatus().intValue() == 2 || detailApp.getRunStatus().intValue() == 3 || detailApp.getRunStatus().intValue() == 4)
								|| (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().equals(1))) {
							ActionDetailApp actionDetailApp = cloner.deepClone(detailApp);

							obj = new ExeObject();
							obj.setModule(app);
							actionDetailApp.setGroupAction(Constant.STEP_ROLLBACK_START);
							obj.setDetailApp(actionDetailApp);
							obj.setActionModule(actionModule);
							obj.setAction(this.selectedAction);
							obj.setBeginDate(actionDetailApp.getRollbackStartTime());
							obj.setEndDate(actionDetailApp.getRollbackEndTime());
							obj.setRunStt(actionDetailApp.getRollbackStatus());

							impactObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_START_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);
						}
						//05-12-2018 KienPD check add step rollback end
					} else if (groupAction.equals(Constant.STEP_CLEARCACHE)) {
						impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CLEARCACHE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);

						//05-12-2018 KienPD check add step rollback start
						if (detailApp.getRunStatus() != null && (detailApp.getRunStatus().intValue() == 2 || detailApp.getRunStatus().intValue() == 3 || detailApp.getRunStatus().intValue() == 4)
								|| (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().equals(1))) {
							ActionDetailApp actionDetailApp = cloner.deepClone(detailApp);

							obj = new ExeObject();
							obj.setModule(app);
							actionDetailApp.setGroupAction(Constant.STEP_ROLLBACK_CLEARCACHE);
							obj.setDetailApp(actionDetailApp);
							obj.setActionModule(actionModule);
							obj.setAction(this.selectedAction);
							obj.setBeginDate(actionDetailApp.getRollbackStartTime());
							obj.setEndDate(actionDetailApp.getRollbackEndTime());
							obj.setRunStt(actionDetailApp.getRollbackStatus());

							impactObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CLEARCACHE, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);
						}
						//05-12-2018 KienPD check add step rollback end
					} else if (groupAction.equals(Constant.STEP_START)) {
						impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_START_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);

						//05-12-2018 KienPD check add step rollback start
						if (detailApp.getRunStatus() != null && (detailApp.getRunStatus().intValue() == 2 || detailApp.getRunStatus().intValue() == 3 || detailApp.getRunStatus().intValue() == 4)) {
							ActionDetailApp actionDetailApp = cloner.deepClone(detailApp);

							obj = new ExeObject();
							obj.setModule(app);
							actionDetailApp.setGroupAction(Constant.STEP_ROLLBACK_STOP);
							obj.setDetailApp(actionDetailApp);
							obj.setActionModule(actionModule);
							obj.setAction(this.selectedAction);
							obj.setBeginDate(actionDetailApp.getRollbackStartTime());
							obj.setEndDate(actionDetailApp.getRollbackEndTime());
							obj.setRunStt(actionDetailApp.getRollbackStatus());

							impactObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_STOP_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);
						}
						//05-12-2018 KienPD check add step rollback end
					/*20181115_hoangnd_save all step_start*/
					} else if (groupAction.equals(Constant.STEP_UPCODE_STOP_START)) {
						impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_UPCODE_START_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);

						if (detailApp.getRunStatus() != null && (detailApp.getRunStatus().intValue() == 2 || detailApp.getRunStatus().intValue() == 3 || detailApp.getRunStatus().intValue() == 4)) {
							ActionDetailApp actionDetailApp = cloner.deepClone(detailApp);

							obj = new ExeObject();
							obj.setModule(app);
							actionDetailApp.setGroupAction(Constant.STEP_ROLLBACK_CODE_STOP_START);
							obj.setDetailApp(actionDetailApp);
							obj.setActionModule(actionModule);
							obj.setAction(this.selectedAction);
							obj.setBeginDate(actionDetailApp.getRollbackStartTime());
							obj.setEndDate(actionDetailApp.getRollbackEndTime());
							obj.setRunStt(actionDetailApp.getRollbackStatus());

							impactObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_UPCODE_START, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);
						}
					} else if (groupAction.equals(Constant.STEP_CHECKLIST_APP)) {
						impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CHECKLIST_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);

						obj = new ExeObject();
						obj.setModule(app);
						obj.setDetailApp(detailApp);
						obj.setActionModule(actionModule);
						obj.setAction(this.selectedAction);
						obj.setBeginDate(detailApp.getRunStartTime());
						obj.setEndDate(detailApp.getRunEndTime());
						obj.setRunStt(detailApp.getBeforeStatus());

						impactObjects.put(new MapEntry(AamConstants.ACTION.BEFORE_STEP_CHECKLIST_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);

						/*20190125_hoangnd_fix bug show rollback checklist_start*/
						if (detailApp.getRunStatus() != null || (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().equals(1))
								|| detailApp.getRollbackStatus() != null) {
						/*20190125_hoangnd_fix bug show rollback checklist_end*/
							ActionDetailApp actionDetailApp = cloner.deepClone(detailApp);

							obj = new ExeObject();
							obj.setModule(app);
							obj.setDetailApp(actionDetailApp);
							obj.setActionModule(actionModule);
							obj.setAction(this.selectedAction);
							obj.setBeginDate(actionDetailApp.getRollbackStartTime());
							obj.setEndDate(actionDetailApp.getRollbackEndTime());
							obj.setRunStt(actionDetailApp.getRollbackStatus());

							impactObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);

							obj = new ExeObject();
							obj.setModule(app);
							obj.setDetailApp(detailApp);
							obj.setActionModule(actionModule);
							obj.setAction(this.selectedAction);
							obj.setBeginDate(detailApp.getRunStartTime());
							obj.setEndDate(detailApp.getRunEndTime());
							obj.setRunStt(detailApp.getAfterStatus());

							impactObjects.put(new MapEntry(AamConstants.ACTION.AFTER_STEP_CHECKLIST_APP, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);
						}
					} else if (groupAction.equals(Constant.STEP_CHECKLIST_DB)) {
						impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_CHECKLIST_DB, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);

						obj = new ExeObject();
						obj.setModule(app);
						obj.setDetailApp(detailApp);
						obj.setActionModule(actionModule);
						obj.setAction(this.selectedAction);
						obj.setBeginDate(detailApp.getRunStartTime());
						obj.setEndDate(detailApp.getRunEndTime());
						obj.setRunStt(detailApp.getBeforeStatus());

						impactObjects.put(new MapEntry(AamConstants.ACTION.BEFORE_STEP_CHECKLIST_DB, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);

						/*20190125_hoangnd_fix bug show rollback checklist_start*/
						if (detailApp.getRunStatus() != null || (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().equals(1))
								|| detailApp.getRollbackStatus() != null) {
						/*20190125_hoangnd_fix bug show rollback checklist_end*/
							ActionDetailApp actionDetailApp = cloner.deepClone(detailApp);

							obj = new ExeObject();
							obj.setModule(app);
							obj.setDetailApp(actionDetailApp);
							obj.setActionModule(actionModule);
							obj.setAction(this.selectedAction);
							obj.setBeginDate(actionDetailApp.getRollbackStartTime());
							obj.setEndDate(actionDetailApp.getRollbackEndTime());
							obj.setRunStt(actionDetailApp.getRollbackStatus());

							/*20190125_hoangnd_fix bug show rollback checklist_start*/
							//05-12-2018 KienPD check add step rollback start
//							if (detailApp.getRunStatus() != null && (detailApp.getRunStatus().intValue() == 2 || detailApp.getRunStatus().intValue() == 3 || detailApp.getRunStatus().intValue() == 4)
//									|| (detailApp.getIsAddRollback() != null && detailApp.getIsAddRollback().equals(1))) {
							impactObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_DB, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);
//							}
							//05-12-2018 KienPD check add step rollback start
							/*20190125_hoangndfix bug show rollback checklist__end*/
							obj = new ExeObject();
							obj.setModule(app);
							obj.setDetailApp(detailApp);
							obj.setActionModule(actionModule);
							obj.setAction(this.selectedAction);
							obj.setBeginDate(detailApp.getRunStartTime());
							obj.setEndDate(detailApp.getRunEndTime());
							obj.setRunStt(detailApp.getAfterStatus());

							impactObjects.put(new MapEntry(AamConstants.ACTION.AFTER_STEP_CHECKLIST_DB, actionModule.getKbGroup() == null ? 1 : actionModule.getKbGroup()), obj);
						}
					}

					/*if (appMap.containsKey(groupAction)) {

						appMap.get(groupAction).add(obj);
					}*/

				}

			}
			// Xử lý DB

			for (ActionDetailDatabase detailDb : dbList) {

//				ServiceDb db = serviceDbService.findById(detailDb.getAppDbId());
				ServiceDatabase database = iimService.findServiceDbById(selectedAction.getImpactProcess().getNationCode(), detailDb.getAppDbId());
				if (database == null)
					continue;

				detailDb.setServiceDatabase(database);

				ExeObject backupDBObj = new ExeObject();
				backupDBObj.setServiceDb(database);
				backupDBObj.setActionDatabase(detailDb);
				backupDBObj.setAction(this.selectedAction);
				backupDBObj.setBeginDate(detailDb.getBackupStartTime());
				backupDBObj.setEndDate(detailDb.getBackupEndTime());
				backupDBObj.setRunStt(detailDb.getBackupStatus());
				backupDBObj.setActionDb(0);
//				this.backupDbObjects.add(backupDBObj);
				impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_BACKUP_DB, detailDb.getKbGroup() == null ? 1 : detailDb.getKbGroup()), backupDBObj);



				ExeObject executeDbObj = new ExeObject();
				executeDbObj.setServiceDb(database);
				executeDbObj.setActionDatabase(detailDb);
				executeDbObj.setAction(this.selectedAction);
				executeDbObj.setBeginDate(detailDb.getRunStartTime());
				executeDbObj.setEndDate(detailDb.getRunEndTime());
				executeDbObj.setRunStt(detailDb.getRunStatus());
				executeDbObj.setActionDb(1);
//				this.executeDbObjects.add(executeDbObj);
				impactObjects.put(new MapEntry(AamConstants.ACTION.SUB_STEP_TD_DB, detailDb.getKbGroup() == null ? 1 : detailDb.getKbGroup()), executeDbObj);

				/*20181121_hoangnd_fix bug show buoc rollback tbdb_start*/
				//05-12-2018 KienPD check add step rollback start
				if ((detailDb.getRunStatus() != null && (detailDb.getRunStatus().intValue() == 2 || detailDb.getRunStatus().intValue() == 3 || detailDb.getRunStatus().intValue() == 4))
						|| (detailDb.getIsAddRollback() != null && detailDb.getIsAddRollback().equals(1))) {
					Cloner cloner = new Cloner();
					ActionDetailDatabase actionDetailDatabase = cloner.deepClone(detailDb);

					ExeObject rollbackDbObj = new ExeObject();
					rollbackDbObj.setServiceDb(database);
					rollbackDbObj.setActionDatabase(actionDetailDatabase);
					rollbackDbObj.setAction(this.selectedAction);
					rollbackDbObj.setBeginDate(actionDetailDatabase.getRollbackStartTime());
					rollbackDbObj.setEndDate(actionDetailDatabase.getRollbackEndTime());
					rollbackDbObj.setRunStt(actionDetailDatabase.getRollbackStatus());
					rollbackDbObj.setActionDb(2);
					if (check) {
						impactObjects.put(new MapEntry(AamConstants.ACTION.ROLLBACK_STEP_DB, actionDetailDatabase.getKbGroup() == null ? 1 : actionDetailDatabase.getKbGroup()), rollbackDbObj);
					}
				}
				//05-12-2018 KienPD check add step rollback start
				/*20181121_hoangnd_fix bug show buoc rollback tbdb_end*/
			}

			/*20181116_hoangnd_save all step_start*/
			logger.info("Bat dau reload chekclist:" + new Date());
			executeChecklistController.reload();
			logger.info("Ket thuc reload chekclist:" + new Date());
			/*20181116_hoangnd_save all step_end*/

			/*20181115_hoangnd_save all step_start*/
//			this.setupStatus(impactObjects.get(new MapEntry(Constant.SUB_STEP_CHECK_STATUS, 1)), Constant.SUB_STEP_CHECK_STATUS, 1);
			/*20181115_hoangnd_save all step_end*/
			for (Integer kbGroup : kbGroups) {
				//05-12-2018 add icon start
				this.setupStatus(impactObjects.get(new MapEntry(Constant.ROLLBACK_STEP_CHECK_STATUS, kbGroup)), Constant.ROLLBACK_STEP_CHECK_STATUS, kbGroup);
				//05-12-2018 add icon end

				/*20181108_hoangnd_save all step_start*/
				this.setupStatus(impactObjects.get(new MapEntry(Constant.SUB_STEP_CHECK_STATUS, kbGroup)), Constant.SUB_STEP_CHECK_STATUS, kbGroup);
				/*20181108_hoangnd_save all step_end*/
				this.setupStatus(impactObjects.get(new MapEntry(Constant.SUB_STEP_STOP_APP, kbGroup)), Constant.SUB_STEP_STOP_APP, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.SUB_STEP_BACKUP_APP, kbGroup)), Constant.SUB_STEP_BACKUP_APP, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.SUB_STEP_BACKUP_DB, kbGroup)), Constant.SUB_STEP_BACKUP_DB, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.SUB_STEP_UPCODE, kbGroup)), Constant.SUB_STEP_UPCODE, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.SUB_STEP_TD_DB, kbGroup)), Constant.SUB_STEP_TD_DB, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.SUB_STEP_CLEARCACHE, kbGroup)), Constant.SUB_STEP_CLEARCACHE, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.SUB_STEP_RESTART_APP, kbGroup)), Constant.SUB_STEP_RESTART_APP, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.SUB_STEP_START_APP, kbGroup)), Constant.SUB_STEP_START_APP, kbGroup);
				/*20181108_hoangnd_save all step_start*/
				this.setupStatus(impactObjects.get(new MapEntry(Constant.SUB_STEP_UPCODE_START_APP, kbGroup)), Constant.SUB_STEP_UPCODE_START_APP, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.SUB_STEP_CHECKLIST_APP, kbGroup)), Constant.SUB_STEP_CHECKLIST_APP, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.SUB_STEP_CHECKLIST_DB, kbGroup)), Constant.SUB_STEP_CHECKLIST_DB, kbGroup);
				/*20181108_hoangnd_save all step_end*/

				this.setupStatus(impactObjects.get(new MapEntry(Constant.ROLLBACK_STEP_STOP_APP, kbGroup)), Constant.ROLLBACK_STEP_STOP_APP, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.ROLLBACK_STEP_SOURCE_CODE, kbGroup)), Constant.ROLLBACK_STEP_SOURCE_CODE, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.ROLLBACK_STEP_DB, kbGroup)), Constant.ROLLBACK_STEP_DB, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.ROLLBACK_STEP_CLEARCACHE, kbGroup)), Constant.ROLLBACK_STEP_CLEARCACHE, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.ROLLBACK_STEP_RESTART_APP, kbGroup)), Constant.ROLLBACK_STEP_RESTART_APP, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.ROLLBACK_STEP_START_APP, kbGroup)), Constant.ROLLBACK_STEP_START_APP, kbGroup);
				/*20181117_hoangnd_save all step_start*/
				this.setupStatus(impactObjects.get(new MapEntry(Constant.ROLLBACK_STEP_UPCODE_START, kbGroup)), Constant.ROLLBACK_STEP_UPCODE_START, kbGroup);

				this.setupStatus(impactObjects.get(new MapEntry(Constant.ROLLBACK_STEP_CHECKLIST_APP, kbGroup)), Constant.ROLLBACK_STEP_CHECKLIST_APP, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.ROLLBACK_STEP_CHECKLIST_DB, kbGroup)), Constant.ROLLBACK_STEP_CHECKLIST_DB, kbGroup);

				this.setupStatus(impactObjects.get(new MapEntry(Constant.BEFORE_STEP_CHECKLIST_APP, kbGroup)), Constant.BEFORE_STEP_CHECKLIST_APP, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.BEFORE_STEP_CHECKLIST_DB, kbGroup)), Constant.BEFORE_STEP_CHECKLIST_DB, kbGroup);

				this.setupStatus(impactObjects.get(new MapEntry(Constant.AFTER_STEP_CHECKLIST_APP, kbGroup)), Constant.AFTER_STEP_CHECKLIST_APP, kbGroup);
				this.setupStatus(impactObjects.get(new MapEntry(Constant.AFTER_STEP_CHECKLIST_DB, kbGroup)), Constant.AFTER_STEP_CHECKLIST_DB, kbGroup);
				/*20181117_hoangnd_save all step_end*/
			}


		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			MessageUtil.setErrorMessage(e.getMessage());
		} finally {
			this.setupRunStep();
//			appMap.clear();
		}
	}

	public boolean checkImpactRender() {
		if (this.selectedRunStep != null && (int)this.selectedRunStep.getValue().getKey() >= 100 &&
				/*20181117_hoangnd_save all step_start*/
				!Arrays.asList(AamConstants.ACTION.BEFORE_STEP_CHECKLIST_APP, AamConstants.ACTION.BEFORE_STEP_CHECKLIST_DB,
						AamConstants.ACTION.AFTER_STEP_CHECKLIST_APP, AamConstants.ACTION.AFTER_STEP_CHECKLIST_DB,
						AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_APP, AamConstants.ACTION.ROLLBACK_STEP_CHECKLIST_DB,
						AamConstants.ACTION.SUB_STEP_CHECKLIST_APP, AamConstants.ACTION.SUB_STEP_CHECKLIST_DB).contains(this.selectedRunStep.getValue().getKey()))
				/*20181117_hoangnd_save all step_end*/
			return true;
		return false;
	}

	public List<ExeObject> getLstImpactObjects() {
		if (selectedRunStep == null)
			return new ArrayList<>();
		else {
			MapEntry entryKey = selectedRunStep.getValue();
			Collection<ExeObject>  exeObjects = impactObjects.get(entryKey);
			if (!exeObjects.isEmpty())
				return new ArrayList<>(exeObjects);

			exeObjects = rollbackObjects.get(entryKey);
			if (!exeObjects.isEmpty())
				return new ArrayList<>(exeObjects);
		}

		return new ArrayList<>();
	}

	private void clear() {
		/*this.stopObjects.clear();
		this.stopObjects.clear();
		this.backupObjects.clear();
		this.backupDbObjects.clear();
		this.upcodeObjects.clear();
		this.executeDbObjects.clear();
		this.clearCacheObjects.clear();
		this.restartObjects.clear();
		this.startObjects.clear();
		this.checkStatusObjects.clear();*/
		impactObjects = HashMultimap.create();
		rollbackObjects = HashMultimap.create();
		this.stepResult.clear();
		this.selectedRunStep = Constant.getSteps().get(new MapEntry(30, 1));
	}

	@PostConstruct
	public void onStart() {
		impactObjects = HashMultimap.create();
		rollbackObjects = HashMultimap.create();
		/*this.stopObjects = new ArrayList<>();
		this.stopObjects = new ArrayList<>();
		this.backupObjects = new ArrayList<>();
		this.backupDbObjects = new ArrayList<>();
		this.upcodeObjects = new ArrayList<>();
		this.executeDbObjects = new ArrayList<>();
		this.clearCacheObjects = new ArrayList<>();
		this.restartObjects = new ArrayList<>();
		this.startObjects = new ArrayList<>();
		this.checkStatusObjects = new ArrayList<>();*/
		/*20181115_hoangnd_save all step_start*/
		change = false;
		executeChecklistController = new ExecuteChecklistController();
		executeChecklistController.setHistoryDetailController(this);
		executeChecklistController.init();
		/*20181115_hoangnd_save all step_end*/
	}

	public ActionDetailDatabaseService getActionDetailDatabaseService() {
		return actionDetailDatabaseService;
	}

	public void setActionDetailDatabaseService(ActionDetailDatabaseService actionDetailDatabaseService) {
		this.actionDetailDatabaseService = actionDetailDatabaseService;
	}

	public ActionDetailAppService getActionDetailAppService() {
		return actionDetailAppService;
	}

	public void setActionDetailAppService(ActionDetailAppService actionDetailAppService) {
		this.actionDetailAppService = actionDetailAppService;
	}

	/*public List<ExeObject> getStopObjects() {
		return stopObjects;
	}

	public List<ExeObject> getBackupObjects() {
		return backupObjects;
	}

	public List<ExeObject> getBackupDbObjects() {
		return backupDbObjects;
	}

	public List<ExeObject> getUpcodeObjects() {
		return upcodeObjects;
	}

	public List<ExeObject> getExecuteDbObjects() {
		return executeDbObjects;
	}

	public List<ExeObject> getClearCacheObjects() {
		return clearCacheObjects;
	}

	public List<ExeObject> getRestartObjects() {
		return restartObjects;
	}

	public List<ExeObject> getStartObjects() {
		return startObjects;
	}

	public List<ExeObject> getCheckStatusObjects() {
		return checkStatusObjects;
	}*/

	public List<SelectItem> getRunSteps() {
		return runSteps;
	}

	public RunStep getSelectedRunStep() {
		return selectedRunStep;
	}

	public String getScriptDetail() {
		return scriptDetail;
	}

	public List<SelectItem> getStatusFitterList() {
		return statusFitterList;
	}

	public Action getSelectedAction() {
		return selectedAction;
	}

	public void setSelectedRunStep(RunStep selectedRunStep) {
		this.selectedRunStep = selectedRunStep;
		/*20181115_hoangnd_save all step_start*/
//		if (change && selectedRunStep != null)
//			selectedStep = selectedRunStep.getValue();
		change = false;
		/*20181115_hoangnd_save all step_end*/
	}

	public ActionModuleService getActionModuleService() {
		return actionModuleService;
	}

	public void setActionModuleService(ActionModuleService actionModuleService) {
		this.actionModuleService = actionModuleService;
	}

	public String getLogDetail() {
		return logDetail;
	}

	public void setLogDetail(String logDetail) {
		this.logDetail = logDetail;
	}
}
