package com.viettel.thread;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.viettel.exception.AppException;
import com.viettel.it.model.CatConfig;
import com.viettel.it.model.FlowRunAction;
import com.viettel.it.persistence.CatConfigServiceImpl;
import com.viettel.it.persistence.FlowRunActionServiceImpl;
import com.viettel.it.util.LogUtils;
import com.viettel.model.Action;
import com.viettel.nms.nocpro.service.AuthorityBO;
import com.viettel.nms.nocpro.service.JsonResponseBO;
import com.viettel.nms.nocpro.service.ParameterBO;
import com.viettel.nms.nocpro.service.RequestInputBO;
import com.viettel.persistence.ActionService;
import com.viettel.persistence.ActionServiceImpl;
import com.viettel.controller.AamConstants;
import com.viettel.util.NocProWebserviceUtils;
import com.viettel.util.Util;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.util.*;

/**
 * @author quanns2
 */
@DisallowConcurrentExecution
public class RunSchedule implements Job {
    private Logger logger = LogManager.getLogger(RunSchedule.class);

    public void execute(JobExecutionContext context) throws JobExecutionException {
        String appCode = System.getenv("APP_CODE");
        logger.info(appCode == null ? "null" : appCode);
        String logAction = "";
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        Map<String, Object> filters = new HashMap<>();
        Date date = new Date();
        if (StringUtils.isNotEmpty(appCode) && "VTN_IT_TOOL_005_001".equals(appCode)) {
            ActionService actionService = new ActionServiceImpl();
//            while (true) {
            try {
                logger.info("============START AUTO RUN MOP DICH VU============");


                List<Action> actions = actionService.findCrAuto(date);
                logger.info("List MOP Auto dich vu: " + (CollectionUtils.isNotEmpty(actions) ? actions.size() : "0"));

                for (Action action : actions) {
                    logger.info("Check validate AutoRun for Action: " + action.getId() + "_" + action.getCrName());
                    logAction = LogUtils.addContent(logAction, "\r\n AutoRun for Action: " + action.getId() + "_" + action.getCrName());
                    //20190826_tudn_start lap lich tac dong tu dong GNOC
                    //B2
                    if (action.getCfStatusNocpro() != null && action.getCfStatusNocpro().compareTo(1L) == 0) {
                        logger.info("Cr duoc Nocpro confirm cho phep chay : " + action.getId());
                        logAction = LogUtils.addContent(logAction, "Cr duoc Nocpro confirm cho phep chay : " + action.getId());
                    } else if (action.getCfStatusNocpro() != null && action.getCfStatusNocpro().compareTo(0L) == 0) {
                        logger.info("Cr duoc Nocpro confirm khong cho phep chay : " + action.getId());
                        logAction = LogUtils.addContent(logAction, "Cr duoc Nocpro confirm khong cho phep chay : " + action.getId());
                        continue;
                    } else {
                        //B2.1
                        if (action.getTypeConfirmGNOC() == null || action.getTypeConfirmGNOC().compareTo(0L) == 0) {
                            logger.info("Cr duoc GNOC khong can confirm de chay : " + action.getId());
                            AuthorityBO authorityBO = new AuthorityBO();
                            authorityBO.setUserName(bundle.getString("ws_nocpro_user"));
                            authorityBO.setPassword(bundle.getString("ws_nocpro_pass"));
                            authorityBO.setRequestId(Integer.valueOf(bundle.getString("ws_nocpro_requestId")));
                            ParameterBO parameterBO = new ParameterBO();
                            parameterBO.setName("p_crNumber");
                            parameterBO.setValue(action.getCrNumber());
                            parameterBO.setType("String");
                            RequestInputBO inputBO = new RequestInputBO();
                            inputBO.setCode("CHECK_ALARM_CR");
                            inputBO.getParams().add(parameterBO);

                            ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
                            String json = ow.writeValueAsString(inputBO);
                            logger.info("Goi WS check canh bao ham getDataJson voi input : " + json);
                            logAction = LogUtils.addContent(logAction, "Goi WS check canh bao voi input : " + json);
                            JsonResponseBO resultData = NocProWebserviceUtils.getService().getDataJson(authorityBO, inputBO);
                            json = ow.writeValueAsString(resultData);
                            logger.info("Output WS check canh bao : " + json);
                            logAction = LogUtils.addContent(logAction, "Output WS check canh bao : " + json);

                            if (resultData != null) {
                                if (resultData.getTotalDataJson() > 0) {
                                    logger.info("Cr co canh bao ben GNOC : " + action.getId());
                                    logAction = LogUtils.addContent(logAction, "Cr co canh bao ben GNOC : " + action.getId());
                                    continue;
                                }
                            } else {
                                logger.info("Cr khong ket noi duoc toi webservice canh bao NOCPRO : " + action.getId());
                                logAction = LogUtils.addContent(logAction, "Cr khong ket noi duoc toi webservice canh bao NOCPRO : " + action.getId());
                                continue;
                            }
                        } else {
                            logger.info("Cr duoc GNOC phai confirm moi cho phep chay : " + action.getId());
                            logAction = LogUtils.addContent(logAction, "Cr duoc GNOC phai confirm moi cho phep chay : " + action.getId());
                            continue;
                        }
                    }
                    //B3
                    if (!Util.isNullOrEmpty(action.getCrLinkGNOC())) {
                        if (action.getTypeRunGNOC() == null || action.getTypeRunGNOC().compareTo(0L) == 0) {
                            //B3.1
                            logger.info("Cr lien ket cha chay song song : " + action.getId());
                            logAction = LogUtils.addContent(logAction, "Cr lien ket cha chay song song : " + action.getId());
                        } else {
                            //B3.2
                            filters.clear();
                            filters.put("crNumber-EXAC", action.getCrLinkGNOC());
                            List<FlowRunAction> crParentFlowRun = new FlowRunActionServiceImpl().findList(filters);
                            filters.clear();
                            filters.put("crNumber-EXAC", action.getCrLinkGNOC());
                            List<Action> crParentAction = new ActionServiceImpl().findList2(filters, null);
                            boolean check = false;
                            if ((crParentAction == null || crParentAction.isEmpty()) && (crParentFlowRun == null || crParentFlowRun.isEmpty())) {
                                logger.info("Khong tim thay duoc Cr lien ket cha da thuc hien tac dong xong : " + action.getId());
                                logAction = LogUtils.addContent(logAction, "Khong tim thay duoc Cr lien ket cha da thuc hien tac dong xong : " + action.getId());
                                continue;

                            } else {
                                for (FlowRunAction flowRunActionParent : crParentFlowRun) {
                                    if (!flowRunActionParent.getStatus().equals(3L)) {
                                        logger.info("Cr co lien ket cha chua chay xong CR: " + action.getCrLinkGNOC() + ", Mop ha tang flowRunId: " + flowRunActionParent.getFlowRunId());
                                        check = true;
                                        break;
                                    }
                                }
                                for (Action actionTemp : crParentAction) {
                                    if (actionTemp.getRunningStatus() == null || (actionTemp.getRunningStatus() != null && !actionTemp.getRunningStatus().equals(AamConstants.RUNNING_STATUS.SUCCESS))) {
                                        logger.info("Cr co lien ket cha chua chay xong CR: " + action.getCrLinkGNOC() + ", Mop dich vu td_code: " + actionTemp.getTdCode());
                                        check = true;
                                        break;
                                    }
                                }
                                if (check) {
                                    logger.info("Cr co lien ket cha chua chay xong : " + action.getId());
                                    logAction = LogUtils.addContent(logAction, "Cr co lien ket cha chua chay xong : " + action.getId());
                                    continue;
                                } else {
                                    logger.info("Cr co lien ket cha da chay xong : " + action.getId());
                                    logAction = LogUtils.addContent(logAction, "Cr co lien ket cha da chay xong : " + action.getId());
                                }
                            }
                        }
                    } else {
                        logger.info("Khong co Cr lien ket cha: " + action.getId());
                        logAction = LogUtils.addContent(logAction, "Khong co Cr lien ket cha: " + action.getId());
                    }
                    //B4
                    Long deltaTime = 300000L;
                    CatConfigServiceImpl catConfigService = new CatConfigServiceImpl();
                    filters.clear();
                    filters.put("id.configGroup-EXAC", AamConstants.CFG_GROUP_RUN_AUTO_MOP);
                    filters.put("id.propertyKey-EXAC", AamConstants.CFG_KEY_DELTA_TIME);
                    filters.put("isActive-EXAC", 1L);
                    List<CatConfig> cfgRunAutoMop = catConfigService.findList(filters);
                    if (cfgRunAutoMop != null && !cfgRunAutoMop.isEmpty() && cfgRunAutoMop.get(0) != null) {

                        if (!Util.isNullOrEmpty(cfgRunAutoMop.get(0).getPropertyValue())) {
                            deltaTime = Long.parseLong(cfgRunAutoMop.get(0).getPropertyValue());
                        }
                        Date systemDate = new Date();
                        Long diff = (action.getEndTime().getTime() - systemDate.getTime());
                        if (diff.compareTo(deltaTime) < 0) {
                            logger.info("Kich ban khong du khoang thoi gian chay tac dong : " + action.getId());
                            logAction = LogUtils.addContent(logAction, "Kich ban khong du khoang thoi gian chay tac dong : " + action.getId());
                            continue;
                        }
                    } else {
                        logger.info("Kich ban khong tim thay deltaTime trong bang cat_config : " + action.getId());
                        logAction = LogUtils.addContent(logAction, "Kich ban khong tim thay deltaTime trong bang cat_config : " + action.getId());
                        continue;
                    }
                    //20190826_tudn_end lap lich tac dong tu dong GNOC
                    AutoThread autoThread = new AutoThread(action);
                    logger.info("Bat dau chay tu dong Mop co id " + action.getId());
                    logAction = LogUtils.addContent(logAction, "Bat dau chay tu dong Mop co id " + action.getId());
                    autoThread.start();
                }
            } catch (AppException e) {
                logger.error(e.getMessage(), e);
                logAction = LogUtils.addContent(logAction, "Run MOP auto fail.");
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                logAction = LogUtils.addContent(logAction, "Run MOP auto fail.");
            } finally {
                logger.info("============END AUTO RUN MOP DICH VU============");
                LogUtils.writelog(new Date(), RunSchedule.class.getName(), new Object() {
                }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.UPDATE.name(), logAction);
                try {
                    LogUtils.logAction(LogUtils.appCode, date, new Date(), "System",
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), RunSchedule.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.CREATE,
                            logAction, LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                }
            }

//                try {
//                    Thread.sleep(5 * 60000);
//                } catch (InterruptedException e) {
//                    logger.error(e.getMessage(), e);
//                }
//            }
        }
    }
}
