package com.viettel.thread;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import com.viettel.gnoc.cr.service.CrOutputForQLTNDTO;
import com.viettel.it.model.CatConfig;
import com.viettel.it.model.FlowRunAction;
import com.viettel.it.persistence.CatConfigServiceImpl;
import com.viettel.it.persistence.FlowRunActionServiceImpl;
import com.viettel.it.util.GNOCService;
import com.viettel.it.util.LogUtils;
import com.viettel.model.Action;
import com.viettel.nms.nocpro.service.*;
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

import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by VTN-PTPM-NV55 on 2/13/2019.
 */
@DisallowConcurrentExecution
public class RunCrSchedule implements Job {

    private static Logger logger = LogManager.getLogger(RunSchedule.class);
    private static NocproWebservice nocproWebservice;

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        autoRunMop();
    }

    public static void autoRunMop() throws JobExecutionException {
        // TODO Auto-generated method stub
        ResourceBundle bundle = ResourceBundle.getBundle("config");
        List<FlowRunAction> flowRunActions;
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        String logAction = "";
        Date now = new Date();
        String appCode = System.getenv("APP_CODE");
        logger.info(appCode == null ? "null" : appCode);

//        while (true) {
        if (StringUtils.isNotEmpty(appCode) && "VTN_IT_TOOL_005_001".equals(appCode)) {
            try {
                logger.info("============START AUTO RUN MOP HA TANG============");


                Map<String, Object> filters = new HashMap<>();
                filters.put("crStatus", 6L);
                filters.put("runAuto", 1L);
                filters.put("status", 1L);
                flowRunActions = new FlowRunActionServiceImpl().findList(filters);
                logger.info("List MOP Auto Ha tang: " + (CollectionUtils.isNotEmpty(flowRunActions) ? flowRunActions.size() : "0"));
                logAction = LogUtils.addContent(logAction, "List MOP Auto: " + (CollectionUtils.isNotEmpty(flowRunActions) ? flowRunActions.size() : "0"));

                for (FlowRunAction flowRunAction : flowRunActions) {
                    logger.info("Check validate AutoRun for FlowRunAction: " + flowRunAction.getFlowRunId() + "_" + flowRunAction.getFlowRunName());
                    logAction = LogUtils.addContent(logAction, "\r\n AutoRun for FlowRunAction: " + flowRunAction.getFlowRunId() + "_" + flowRunAction.getFlowRunName());

                    logger.info("Goi WS CrForOtherSystemService ham getCrForQLTN voi input CrNumber : " + flowRunAction.getCrNumber());
                    logAction = LogUtils.addContent(logAction, "Goi WS CrForOtherSystemService ham getCrForQLTN voi input CrNumber : " + flowRunAction.getCrNumber());

                    CrOutputForQLTNDTO qltndto = GNOCService.getCrByCode(flowRunAction.getCrNumber());

                    logger.info("\r\n Goi WS CrForOtherSystemService ham getCrForQLTN voi output ResultCode: " + qltndto.getResultCode() + " ImpactStartTime: " + qltndto.getImpactStartTime() + " ImpactEndTime: " + qltndto.getImpactEndTime());
                    logAction = LogUtils.addContent(logAction, "\r\n Goi WS CrForOtherSystemService ham getCrForQLTN voi output ResultCode: " + qltndto.getResultCode() + " ImpactStartTime: " + qltndto.getImpactStartTime() + " ImpactEndTime: " + qltndto.getImpactEndTime());

                    if ("OK".equals(qltndto.getResultCode())) {
                        if (qltndto.getImpactStartTime() != null && qltndto.getImpactEndTime() != null) {
                            Date startDateTime = sdf.parse(qltndto.getImpactStartTime());
                            Date endDateTime = sdf.parse(qltndto.getImpactEndTime());
//                            Date startDateTime = DateTimeUtils.convertStringToDate(bundle.getString("startTimeGNOC"),"dd/MM/yyyy HH:mm");
//                            Date endDateTime = DateTimeUtils.convertStringToDate(bundle.getString("endTimeGNOC"),"dd/MM/yyyy HH:mm");
                            if (startDateTime != null && endDateTime != null && startDateTime.before(now) && endDateTime.after(now)) {
                                logger.info("Tac dong kich ban ID : " + flowRunAction.getFlowRunId());
                                if (flowRunAction.getCfStatusNocpro() != null && flowRunAction.getCfStatusNocpro().compareTo(1L) == 0) {
                                    logger.info("Cr duoc Nocpro confirm cho phep chay : " + flowRunAction.getFlowRunId());
                                    logAction = LogUtils.addContent(logAction, "Cr duoc Nocpro confirm cho phep chay : " + flowRunAction.getFlowRunId());
                                } else if (flowRunAction.getCfStatusNocpro() != null && flowRunAction.getCfStatusNocpro().compareTo(0L) == 0) {
                                    logger.info("Cr duoc Nocpro confirm khong cho phep chay : " + flowRunAction.getFlowRunId());
                                    logAction = LogUtils.addContent(logAction, "Cr duoc Nocpro confirm khong cho phep chay : " + flowRunAction.getFlowRunId());
                                    continue;
                                } else {
                                    //B2.1
                                    if (flowRunAction.getTypeConfirmGNOC() == null || flowRunAction.getTypeConfirmGNOC().compareTo(0L) == 0) {
                                        logger.info("Cr duoc GNOC khong can confirm de chay : " + flowRunAction.getFlowRunId());
                                        AuthorityBO authorityBO = new AuthorityBO();
                                        authorityBO.setUserName(bundle.getString("ws_nocpro_user"));
                                        authorityBO.setPassword(bundle.getString("ws_nocpro_pass"));
                                        authorityBO.setRequestId(Integer.valueOf(bundle.getString("ws_nocpro_requestId")));
                                        ParameterBO parameterBO = new ParameterBO();
                                        parameterBO.setName("p_crNumber");
                                        parameterBO.setValue(flowRunAction.getCrNumber());
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
                                                logger.info("Cr co canh bao ben GNOC : " + flowRunAction.getFlowRunId());
                                                logAction = LogUtils.addContent(logAction, "Cr co canh bao ben GNOC : " + flowRunAction.getFlowRunId());
                                                continue;
                                            }
                                        } else {
                                            logger.info("Cr khong ket noi duoc toi webservice canh bao NOCPRO : " + flowRunAction.getFlowRunId());
                                            logAction = LogUtils.addContent(logAction, "Cr khong ket noi duoc toi webservice canh bao NOCPRO : " + flowRunAction.getFlowRunId());
                                            continue;
                                        }
                                    } else {
                                        logger.info("Cr duoc GNOC phai confirm moi cho phep chay : " + flowRunAction.getFlowRunId());
                                        logAction = LogUtils.addContent(logAction, "Cr duoc GNOC phai confirm moi cho phep chay : " + flowRunAction.getFlowRunId());
                                        continue;
                                    }
                                }
                                //B3
                                if (!Util.isNullOrEmpty(flowRunAction.getCrLinkGNOC())) {
                                    if (flowRunAction.getTypeRunGNOC() == null || flowRunAction.getTypeRunGNOC().compareTo(0L) == 0) {
                                        //B3.1
                                        logger.info("Cr lien ket cha chay song song : " + flowRunAction.getFlowRunId());
                                        logAction = LogUtils.addContent(logAction, "Cr lien ket cha chay song song : " + flowRunAction.getFlowRunId());
                                    } else {
                                        //B3.2
                                        filters.clear();
                                        filters.put("crNumber-EXAC", flowRunAction.getCrLinkGNOC());
                                        List<FlowRunAction> crParentFlowRun = new FlowRunActionServiceImpl().findList(filters);
                                        filters.clear();
                                        filters.put("crNumber-EXAC", flowRunAction.getCrLinkGNOC());
                                        List<Action> crParentAction = new ActionServiceImpl().findList2(filters, null);
                                        boolean check = false;
                                        if ((crParentAction == null || crParentAction.isEmpty()) && (crParentFlowRun == null || crParentFlowRun.isEmpty())) {
                                            logger.info("Khong tim thay duoc Cr lien ket cha da thuc hien tac dong xong : " + flowRunAction.getFlowRunId());
                                            logAction = LogUtils.addContent(logAction, "Khong tim thay duoc Cr lien ket cha da thuc hien tac dong xong : " + flowRunAction.getFlowRunId());
                                            continue;

                                        } else {
                                            for (FlowRunAction flowRunActionParent : crParentFlowRun) {
                                                if (!flowRunActionParent.getStatus().equals(3L)) {
                                                    logger.info("Cr co lien ket cha chua chay xong CR: " + flowRunAction.getCrLinkGNOC() + ", Mop ha tang flowRunId: " + flowRunActionParent.getFlowRunId());
                                                    check = true;
                                                    break;
                                                }
                                            }
                                            for (Action actionTemp : crParentAction) {
                                                if (actionTemp.getRunningStatus() == null || (actionTemp.getRunningStatus() != null && !actionTemp.getRunningStatus().equals(AamConstants.RUNNING_STATUS.SUCCESS))) {
                                                    logger.info("Cr co lien ket cha chua chay xong CR: " + flowRunAction.getCrLinkGNOC() + ", Mop dich vu td_code: " + actionTemp.getTdCode());
                                                    check = true;
                                                    break;
                                                }
                                            }
                                            if (check) {
                                                logger.info("Cr co lien ket cha chua chay xong : " + flowRunAction.getFlowRunId());
                                                logAction = LogUtils.addContent(logAction, "Cr co lien ket cha chua chay xong : " + flowRunAction.getFlowRunId());
                                                continue;
                                            } else {
                                                logger.info("Cr co lien ket cha da chay xong : " + flowRunAction.getFlowRunId());
                                                logAction = LogUtils.addContent(logAction, "Cr co lien ket cha da chay xong : " + flowRunAction.getFlowRunId());
                                            }
                                        }
                                    }
                                } else {
                                    logger.info("Khong co Cr lien ket cha: " + flowRunAction.getFlowRunId());
                                    logAction = LogUtils.addContent(logAction, "Khong co Cr lien ket cha: " + flowRunAction.getFlowRunId());
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
                                    Long diff = (endDateTime.getTime() - systemDate.getTime());
                                    if (diff.compareTo(deltaTime) < 0) {
                                        logger.info("Kich ban khong du khoang thoi gian chay tac dong : " + flowRunAction.getFlowRunId());
                                        logAction = LogUtils.addContent(logAction, "Kich ban khong du khoang thoi gian chay tac dong : " + flowRunAction.getFlowRunId());
                                        continue;
                                    }
                                } else {
                                    logger.info("Kich ban khong tim thay deltaTime trong bang cat_config : " + flowRunAction.getFlowRunId());
                                    logAction = LogUtils.addContent(logAction, "Kich ban khong tim thay deltaTime trong bang cat_config : " + flowRunAction.getFlowRunId());
                                    continue;
                                }
                                //20190826_tudn_end lap lich tac dong tu dong GNOC
                                AutoCrThread autoThread = new AutoCrThread(flowRunAction);
                                logger.info("Bat dau chay tu dong Mop co id " + flowRunAction.getFlowRunId());
                                logAction = LogUtils.addContent(logAction, "Bat dau chay tu dong Mop co id " + flowRunAction.getFlowRunId());
                                autoThread.start();

                            } else {
                                logger.info("Kich ban khong nam trong khoang thoi gian tac dong : " + flowRunAction.getFlowRunId());
                                logAction = LogUtils.addContent(logAction, "Kich ban khong nam trong khoang thoi gian tac dong : " + flowRunAction.getFlowRunId());
                            }
                        } else {
                            logger.info("Ket qua ws tra ve thieu thong tin ImpactStartTime hoac ImpactEndTime : " + flowRunAction.getFlowRunId());
                            logAction = LogUtils.addContent(logAction, "Ket qua ws tra ve thieu thong tin ImpactStartTime hoac ImpactEndTime : " + flowRunAction.getFlowRunId());
                        }
                    } else {
                        logger.info("Kich ban call ws that bai : " + flowRunAction.getFlowRunId() + "Error: " + qltndto.getDescription());
                        logAction = LogUtils.addContent(logAction, "Kich ban call ws that bai : " + flowRunAction.getFlowRunId() + "Error: " + qltndto.getDescription());
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            } finally {
                logger.info("============END AUTO RUN MOP HA TANG============");
//            LogUtils.writelog(new Date(), RunCrSchedule.class.getName(), new Object() {
//            }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.IMPACT.name(), logAction);

                try {
                    LogUtils.logAction(LogUtils.appCode, now, new Date(), "System",
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), RunCrSchedule.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(), LogUtils.ActionType.CREATE,
                            logAction, LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
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

    public static void main(String[] args) {


//        ResourceBundle bundle = ResourceBundle.getBundle("config");
//        List<FlowRunAction> flowRunActions;
//        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
//
//        while (true) {
        try {
            autoRunMop();
//                logger.info("START AUTO RUN");
//                Date now = new Date();
//
//                Map<String, Object> filters = new HashMap<>();
//                filters.put("crStatus", 6L);
//                flowRunActions = new FlowRunActionServiceImpl().findList(filters);
//                logger.info("actions : " + (CollectionUtils.isNotEmpty(flowRunActions) ? flowRunActions.size() : ""));
//
//                for (FlowRunAction flowRunAction : flowRunActions) {
//                    CrForOtherSystemService service = new CrForOtherSystemServiceImplServiceLocator().getCrForOtherSystemServiceImplPort(new URL(bundle.getString("ws_gnoc_new")));
//                    CrOutputForQLTNDTO qltndto = service.getCrForQLTN(bundle.getString("ws_gnoc_user"), bundle.getString("ws_gnoc_pass"), flowRunAction.getCrNumber());
//                    if ("OK".equals(qltndto.getResultCode())) {
//                        if(qltndto.getImpactStartTime() != null && qltndto.getImpactEndTime() != null) {
//                            Date startDateTime = sdf.parse(qltndto.getImpactStartTime());
//                            Date endDateTime = sdf.parse(qltndto.getImpactEndTime());
////                            if (startDateTime != null && endDateTime != null && startDateTime.before(now) && endDateTime.after(now)) {
//                                if (flowRunAction.getStatus().equals(1L)) {
//                                    logger.info("Tac dong kich ban ID : " + flowRunAction.getFlowRunId());
//                                    AutoCrThread autoThread = new AutoCrThread(flowRunAction);
//                                    autoThread.start();
//                                } else if (flowRunAction.getStatus().equals(0L)) {
//                                    logger.info("Khong tac dong kich ban ID : " + flowRunAction.getFlowRunId() + ". Ly do : kich ban chua duoc phe duyet");
//                                    /*String logAction = "Khong tac dong kich ban ID : " + flowRunAction.getFlowRunId() + ". Ly do : kich ban chua duoc phe duyet";
//                                    LogUtils.writelog(new Date(), RunSchedule.class.getName(), new Object() {
//                                    }.getClass().getEnclosingMethod().getName(), LogUtils.ActionType.IMPACT.name(), logAction);*/
//                                }
//                            /*} else {
//                                logger.info("Kich ban khong nam trong khoang thoi gian tac dong : " + flowRunAction.getFlowRunId());
//                            }*/
//                        } else {
//                            logger.info("Ket qua ws tra ve thieu thong tin ImpactStartTime hoac ImpactEndTime : " + flowRunAction.getFlowRunId());
//                        }
//                    } else {
//                        logger.info("Kich ban call ws that bai : " + flowRunAction.getFlowRunId());
//                    }
//                }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        } finally {
            logger.info("END AUTO RUN");
            logger.info("================");
            try {
                Thread.sleep(5 * 60000);
            } catch (InterruptedException e) {
                logger.error(e.getMessage(), e);
            }
        }
//        }
    }
}
