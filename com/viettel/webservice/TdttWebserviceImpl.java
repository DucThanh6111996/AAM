package com.viettel.webservice;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.viettel.controller.Module;
import com.viettel.bean.Service;
import com.viettel.exception.AppException;
import com.viettel.gnoc.cr.CrForOtherSystemService;
import com.viettel.gnoc.cr.CrForOtherSystemServiceImplService;
import com.viettel.gnoc.cr.CrForOtherSystemServiceImplServiceLocator;
import com.viettel.gnoc.cr.service.CrOutputForQLTNDTO;
import com.viettel.iim.services.main.IimServices_PortType;
import com.viettel.iim.services.main.JsonResponseBO;
import com.viettel.iim.services.main.ParameterBO;
import com.viettel.iim.services.main.RequestInputBO;
import com.viettel.it.controller.GenerateFlowRunController;
import com.viettel.it.controller.ProcedureCRTemplateController;
import com.viettel.it.model.*;
import com.viettel.it.object.CheckParamCondition;
import com.viettel.it.object.ObjectImportDt;
import com.viettel.it.persistence.*;
import com.viettel.it.persistence.common.CatCountryServiceImpl;
import com.viettel.it.util.*;
import com.viettel.it.util.Config;
import com.viettel.it.util.LogUtils;
import com.viettel.it.webservice.object.*;
import com.viettel.jackson.AamJsonFactory;
import com.viettel.model.Action;
import com.viettel.model.ActionServer;
import com.viettel.model.CatCountryBO;
import com.viettel.persistence.*;
import com.viettel.persistence.ActionServiceImpl;
import com.viettel.util.*;
import com.viettel.util.PasswordEncoder;
import com.viettel.util.Util;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.io.*;
import java.math.BigDecimal;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.rmi.RemoteException;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author quanns2
 */
@WebService(endpointInterface = "com.viettel.webservice.TdttWebservice")
public class TdttWebserviceImpl implements TdttWebservice {
    private static Logger logger = LogManager.getLogger(TdttWebserviceImpl.class);
    private SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
    ProcedureCRTemplateController procedureCRTemplateController = new ProcedureCRTemplateController();

    //20180620_tudn_start ghi log DB
    @Resource
    private WebServiceContext context;

    public String getRemoteIp() {
        String ip = "";
        MessageContext mc = context.getMessageContext();
        HttpServletRequest req = (HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST);
        if (req != null) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }

    @Override
    public MopResult getListMop(String wsUser, String wsPass, String username) {
        logger.info("getListMop " + username);
        MopResult mopResult = new MopResult();
        mopResult.setStatus(0);
        if (!checkPass(wsUser, wsPass)) {
            mopResult.setMessage("Xác thực ws không đúng");
            return mopResult;
        }

        List<MopInfo> mopInfos = new ArrayList<>();

        ActionService actionService = new ActionServiceImpl();
//		ApplicationDetailService appService = new ApplicationDetailServiceImpl();
        IimService iimService = new IimServiceImpl();
        try {
            List<Action> actions = actionService.findByUser(username);
            logger.info("list action: " + (actions == null ? 0 : actions.size()));
            for (Action action : actions) {
                try {
                    MopInfo mopInfo = new MopInfo();
                    mopInfo.setCode(action.getTdCode());
                    mopInfo.setName(action.getCrName());
                    mopInfo.setNationCode(action.getImpactProcess() == null ? "VNM" : action.getImpactProcess().getNationCode());
                    mopInfo.setIps(new DocxUtil(action).getListImpactIP(action.getId()));
                    mopInfo.setCreatedDate(action.getCreatedTime());

                    List<Long> appIds = actionService.findListAppIds(action.getId());
                    logger.info("list appIds: " + (appIds == null ? 0 : appIds.size()));
                    logger.info(action.getCrNumber() + "\t" + appIds);
                    if (appIds != null && appIds.size() > 0) {
                        List<Service> services = iimService.findServicesByModules(action.getImpactProcess().getNationCode(), appIds);
                        List<String> appGroups = new ArrayList<>();
                        for (Service service : services) {
                            appGroups.add(service.getServiceCode());
                        }
                        mopInfo.setAffectServices(appGroups);
                        List<String> effectIps = iimService.findAllIpByServices(action.getImpactProcess().getNationCode(), appGroups);
                        mopInfo.setAffectIps(effectIps);

                        //Quytv7_20180911_Get list module start

                        List<Module> moduleList = iimService.findModulesByIds(action.getImpactProcess().getNationCode(), appIds);
                        logger.info("list moduleList: " + (moduleList == null ? 0 : moduleList.size()));
                        List<ModuleInfo> moduleInfos = new ArrayList<>();
                        ModuleInfo moduleInfo;
                        for (Module module : moduleList) {
                            moduleInfo = new ModuleInfo();
                            moduleInfo.setModuleCode(module.getModuleCode());
                            moduleInfo.setModuleName(module.getModuleName());
                            moduleInfo.setServiceCode(module.getServiceCode());
                            moduleInfos.add(moduleInfo);
                        }
                        mopInfo.setModuleInfos(moduleInfos);
                        //Quytv7_20180911_Get list module end
                    }

                    if (action.getKbType() != null && (action.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER) || action.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER))) {
                        HashMap<String, Object> filters = new HashMap<>();
                        filters.put("actionId", action.getId());
                        ActionServerService actionServerService = new ActionServerServiceImpl();
                        List<ActionServer> actionServers = actionServerService.findList(filters, new HashMap<>());
                        if (mopInfo.getAffectIps() == null) {
                            mopInfo.setAffectIps(new ArrayList<>());
                        }
                        if (mopInfo.getIps() == null) {
                            mopInfo.setIps(new ArrayList<>());
                        }
                        if (actionServers != null && actionServers.size() > 0) {
                            for (ActionServer actionServer : actionServers) {
                                if (actionServer.getIpServer() != null) {
                                    logger.info("add ip from actionserver: " + actionServer.getIpServer());
                                    mopInfo.getAffectIps().add(actionServer.getIpServer());
                                    mopInfo.getIps().add(actionServer.getIpServer());
                                }
                            }
                        }
                    }
                    if (mopInfo.getAffectIps() == null || mopInfo.getAffectIps().size() == 0 || mopInfo.getIps() == null || mopInfo.getIps().size() == 0) {
                        logger.info("Ip null, khong add gui sang noc: " + action.getCrId());
                        continue;
                    }
                    mopInfos.add(mopInfo);

                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }
            mopResult.setStatus(1);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        /*
         * hanhnv68 add 2016_12_09
         * lay thong tin mop cua ung dung ha tang
         */
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("createBy-" + GenericDaoImplNewV2.EXAC, username);
            filters.put("status", 0l);
            filters.put("mopType", 0);
            filters.put("crNumber-EXAC", Config.CR_DEFAULT);

            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            orders.put("createDate", "DESC");

            List<FlowRunAction> flowRunActions = (new FlowRunActionServiceImpl()).findList(filters, orders);
            if (flowRunActions != null) {
                MopInfo mopInfo;
                for (FlowRunAction flowAction : flowRunActions) {
                    try {
                        mopInfo = new MopInfo();
                        mopInfo.setNationCode((flowAction.getCountryCode() == null) ? "VNM" : flowAction.getCountryCode().getCountryCode());
                        mopInfo.setName(flowAction.getFlowRunName());
                        mopInfo.setCreatedDate(new Date());
                        mopInfo.setCode(Constant.PREFIX_MOP_INFRA + flowAction.getFlowRunId());

                        List<NodeRun> lstNodeRun = flowAction.getNodeRuns();
                        if (mopInfo.getAffectIps() == null) {
                            mopInfo.setAffectIps(new ArrayList<String>());
                        }
                        if (mopInfo.getAffectServices() == null) {
                            mopInfo.setAffectServices(new ArrayList<String>());
                        }
                        if (mopInfo.getIps() == null) {
                            mopInfo.setIps(new ArrayList<String>());
                        }
                        if (lstNodeRun != null && !lstNodeRun.isEmpty()) {
                            for (NodeRun nodeRun : lstNodeRun) {
                                mopInfo.getAffectIps().add(nodeRun.getNode().getEffectIp());
                                mopInfo.getAffectServices().add(nodeRun.getNode().getNodeCode());
                                mopInfo.getIps().add(nodeRun.getNode().getEffectIp());
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        mopInfo = null;
                    }

                    if (mopInfo != null) {
                        mopInfos.add(mopInfo);
                    }
                }
            }
            for (int i = 0; i < mopInfos.size(); i++) {
                logger.info("mop created: " + mopInfos.get(i).getCode());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        // end hanhnv68 add 2016_12_09


        mopResult.setMopInfos(mopInfos);

        return mopResult;
    }

    //20190401_tudn_start them dau viec quy trinh cho GNOC
    @Override
    public MopResult getListMopForGNOC(String wsUser, String wsPass, String username, ProcedureDTO procedureDTO) {
        //20181119_tudn_end them danh sach lenh blacklist
        logger.info("getListMopForGNOC " + username);
        MopResult mopResult = new MopResult();
        mopResult.setStatus(0);
        if (!checkPass(wsUser, wsPass)) {
            mopResult.setMessage("Webservice authentication is incorrect");
            return mopResult;
        }

        //20181119_tudn_start them danh sach lenh blacklist
        if (procedureDTO == null) {
            mopResult.setMessage("You must enter procedure");
            return mopResult;
        }

        if (Util.isNullOrEmpty(procedureDTO.getProcedureId())) {
            mopResult.setMessage("You must enter 'procedureId'");
            return mopResult;
        }
        //20181119_tudn_end them danh sach lenh blacklist
        if (Util.isNullOrEmpty(procedureDTO.getProcedureWorkFlowIds()) || !Util.hasNotNullValue(procedureDTO.getProcedureWorkFlowIds())) {
            procedureDTO.setProcedureWorkFlowIds(new ArrayList<Long>());
        }

        List<MopInfo> mopInfos = new ArrayList<>();

        ActionService actionService = new ActionServiceImpl();
        IimService iimService = new IimServiceImpl();
        try {
            List<Action> actions = actionService.findByUser(username);
            logger.info("list action: " + (actions == null ? 0 : actions.size()));
            if (actions != null && !actions.isEmpty()) {
                for (Action action : actions) {
                    try {
                        MopInfo mopInfo = new MopInfo();
                        mopInfo.setCode(action.getTdCode());
                        mopInfo.setName(action.getCrName());
                        mopInfo.setNationCode(action.getImpactProcess() == null ? "VNM" : action.getImpactProcess().getNationCode());
                        mopInfo.setIps(new DocxUtil(action).getListImpactIP(action.getId()));
                        mopInfo.setCreatedDate(action.getCreatedTime());

                        List<Long> appIds = actionService.findListAppIds(action.getId());
                        logger.info("list appIds: " + (appIds == null ? 0 : appIds.size()));
                        logger.info(action.getCrNumber() + "\t" + appIds);
                        if (appIds != null && appIds.size() > 0) {
                            List<Service> services = iimService.findServicesByModules(action.getImpactProcess().getNationCode(), appIds);
                            List<String> appGroups = new ArrayList<>();
                            for (Service service : services) {
                                appGroups.add(service.getServiceCode());
                            }
                            mopInfo.setAffectServices(appGroups);
                            List<String> effectIps = iimService.findAllIpByServices(action.getImpactProcess().getNationCode(), appGroups);
                            mopInfo.setAffectIps(effectIps);

                            //Quytv7_20180911_Get list module start

                            List<Module> moduleList = iimService.findModulesByIds(action.getImpactProcess().getNationCode(), appIds);
                            logger.info("list moduleList: " + (moduleList == null ? 0 : moduleList.size()));
                            List<ModuleInfo> moduleInfos = new ArrayList<>();
                            ModuleInfo moduleInfo;
                            for (Module module : moduleList) {
                                moduleInfo = new ModuleInfo();
                                moduleInfo.setModuleCode(module.getModuleCode());
                                moduleInfo.setModuleName(module.getModuleName());
                                moduleInfo.setServiceCode(module.getServiceCode());
                                moduleInfos.add(moduleInfo);
                            }
                            mopInfo.setModuleInfos(moduleInfos);
                            //Quytv7_20180911_Get list module end
                        }

                        if (action.getKbType() != null && (action.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER) || action.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER))) {
                            HashMap<String, Object> filters = new HashMap<>();
                            filters.put("actionId", action.getId());
                            ActionServerService actionServerService = new ActionServerServiceImpl();
                            List<ActionServer> actionServers = actionServerService.findList(filters, new HashMap<>());
                            if (mopInfo.getAffectIps() == null) {
                                mopInfo.setAffectIps(new ArrayList<>());
                            }
                            if (mopInfo.getIps() == null) {
                                mopInfo.setIps(new ArrayList<>());
                            }
                            if (actionServers != null && actionServers.size() > 0) {
                                for (ActionServer actionServer : actionServers) {
                                    if (actionServer.getIpServer() != null) {
                                        logger.info("add ip from actionserver: " + actionServer.getIpServer());
                                        mopInfo.getAffectIps().add(actionServer.getIpServer());
                                        mopInfo.getIps().add(actionServer.getIpServer());
                                    }
                                }
                            }
                        }
                        if (mopInfo.getAffectIps() == null || mopInfo.getAffectIps().size() == 0 || mopInfo.getIps() == null || mopInfo.getIps().size() == 0) {
                            logger.info("Ip null, khong add gui sang noc: " + action.getCrId());
                            continue;
                        }
                        mopInfos.add(mopInfo);

                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                    }
                }
            }
            mopResult.setStatus(1);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }

        /*
         * hanhnv68 add 2016_12_09
         * lay thong tin mop cua ung dung ha tang
         */
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("createBy-" + FlowRunActionServiceImpl.EXAC, username);
            filters.put("status", 0l);
            filters.put("mopType", 0);
            filters.put("crNumber-EXAC", Config.CR_DEFAULT);

            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            orders.put("createDate", "DESC");

            boolean checkProcedureId = false;
            try {
                CatConfigServiceImpl catConfigService = new CatConfigServiceImpl();
                Map<String, Object> filterCfg = new HashMap<>();
                filterCfg.put("id.configGroup-EXAC", AamConstants.CFG_PROCEDURE_GNOC);
                filterCfg.put("id.propertyKey-EXAC", AamConstants.CFG_DANGEROUS_PROCEDURE);
                List<CatConfig> cfgDangerousProcedure = catConfigService.findList(filterCfg);

                if (cfgDangerousProcedure != null && !Util.isNullOrEmpty(cfgDangerousProcedure.get(0).getPropertyValue())
                        && !Util.isNullOrEmpty(cfgDangerousProcedure.get(0).getMapValue())
                ) {
                    String[] gnocProcedureIds = cfgDangerousProcedure.get(0).getMapValue() == null ? null : cfgDangerousProcedure.get(0).getMapValue().split(",");
                    if (gnocProcedureIds != null && gnocProcedureIds.length > 0) {
                        for (String proId : gnocProcedureIds) {
                            if (proId.trim().equalsIgnoreCase(procedureDTO.getProcedureId().toString().trim())) {
                                checkProcedureId = true;
                                break;
                            }
                        }
                    }
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
            List<MopDTO> mopDTOs = new ArrayList<>();
            List<Long> templateIdsProcedures = new ArrayList<>();
            if (checkProcedureId) { // quy trinh nguy hiem
                filters.put("mopType", AamConstants.MOP_TYPE_DANGEROUS);
            } else {
//                if (!Util.isNullOrEmpty(procedureDTO.getProcedureId()) && Util.isNullOrEmpty(procedureDTO.getProcedureWorkFlowIds())) {
//                    filters.put("flowTemplates.procedureId", procedureDTO.getProcedureId());
//                } else if (!Util.isNullOrEmpty(procedureDTO.getProcedureId()) && !Util.isNullOrEmpty(procedureDTO.getProcedureWorkFlowIds())) {
//                    filters.put("flowTemplates.procedureId", procedureDTO.getProcedureId());
//                    filters.put("flowTemplates.procedureWorkFlowId", procedureDTO.getProcedureWorkFlowIds());
//                }
                if (procedureDTO != null) {
                    List<Long> procedureIds = new ArrayList<>();
                    if (!procedureDTO.getProcedureWorkFlowIds().isEmpty()) {
                        procedureIds.addAll(procedureDTO.getProcedureWorkFlowIds());
                    }
                    else {
                        procedureIds.add(procedureDTO.getProcedureId());
                        List<ProcedureGNOC> children = procedureCRTemplateController.loadChild(procedureDTO.getProcedureId());
                        for (ProcedureGNOC child : children) {
                            procedureDTO.setProcedureId(child.getProcedureGNOCId());
                            procedureIds.add(procedureDTO.getProcedureId());
                        }
                    }
                    String hql = "select FLOW_TEMPLATE_ID " +
                            "from PROCEDURE_GNOC_TEMPLATE where " +
                            " PROCEDURE_GNOC_ID in (" + StringUtils.join(procedureIds, ",") + ")" +
                            " OR PROCEDURE_GNOC_ID in (select PROCEDURE_GNOC_ID from PROCEDURE_GNOC where PARENT_ID in  (" + StringUtils.join(procedureIds, ",") + "))";
                    List<BigDecimal> tmpTemplateIdProcedures = (List<BigDecimal>) new FlowRunActionServiceImpl().findListSQLAll(hql);
                    for (BigDecimal tmpTemplateIdProcedure : tmpTemplateIdProcedures) {
                        templateIdsProcedures.add(tmpTemplateIdProcedure.longValue());
                    }
                }
            }
            // thenv_20181109_command blacklist_end
            List<FlowRunAction> flowRunActions = (new FlowRunActionServiceImpl()).findList(filters, orders);
            if (flowRunActions != null) {
                MopInfo mopInfo;
                for (FlowRunAction flowAction : flowRunActions) {
                    try {
                        mopInfo = new MopInfo();
                        mopInfo.setNationCode(flowAction.getCountryCode() == null ? "VNM" : flowAction.getCountryCode().getCountryCode());
                        mopInfo.setName(flowAction.getFlowRunName());
                        mopInfo.setCreatedDate(new Date());
                        mopInfo.setCode(Constant.PREFIX_MOP_INFRA + flowAction.getFlowRunId());

                        List<NodeRun> lstNodeRun = flowAction.getNodeRuns();
                        if (mopInfo.getAffectIps() == null) {
                            mopInfo.setAffectIps(new ArrayList<String>());
                        }
                        if (mopInfo.getAffectServices() == null) {
                            mopInfo.setAffectServices(new ArrayList<String>());
                        }
                        if (mopInfo.getIps() == null) {
                            mopInfo.setIps(new ArrayList<String>());
                        }
                        if (lstNodeRun != null && !lstNodeRun.isEmpty()) {
                            for (NodeRun nodeRun : lstNodeRun) {
                                mopInfo.getAffectIps().add(nodeRun.getNode().getEffectIp());
                                mopInfo.getAffectServices().add(nodeRun.getNode().getNodeCode());
                                mopInfo.getIps().add(nodeRun.getNode().getEffectIp());
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        mopInfo = null;
                    }

                    if (mopInfo != null) {
                        mopInfos.add(mopInfo);
                    }
                }
            }

            mopResult.setStatus(1);
            mopResult.setMessage("SUCCESS");
        } catch (Exception ex) {
            mopResult.setMessage(ex.getMessage());
            logger.error(ex.getMessage(), ex);
        }
        logger.info("list mop get success: " + mopInfos.size());
        mopResult.setMopInfos(mopInfos);

        return mopResult;
    }
    //20190401_tudn_end them dau viec quy trinh cho GNOC

    public static boolean isNullOrEmpty(String str) {
        return str == null || str.trim().isEmpty();
    }

    @Override
    public LinkCrResult linkCr(String wsUser, String wsPass, String crNumber, String mopCode, String
            username, String crName, String startTime, String endTime, Long crState) {
        Util.checkAndPrintObject(logger, "linkCr:", "crNumber", crNumber, "mopCode", mopCode,
                "username", username, "crName", crName, "startTime", startTime, "endTime", endTime, "crState", crState);
        LinkCrResult linkCrResult = new LinkCrResult();
        linkCrResult.setStatus(0);

        if (!checkPass(wsUser, wsPass)) {
            linkCrResult.setMessage("Xác thực ws không đúng");
            return linkCrResult;
        }
        Long mopId = null;
        Action action = null;

        // hanhnv68 add 2016_12_09
        if (mopCode.trim().startsWith(Constant.PREFIX_MOP_INFRA)) {
            try {

                mopId = Long.valueOf(mopCode.trim().substring(Constant.PREFIX_MOP_INFRA.length()));
                FlowRunActionServiceImpl service = new FlowRunActionServiceImpl();
                FlowRunAction flowRun = service.findById(mopId);
                if (flowRun == null) {
                    linkCrResult.setStatus(0);
                    linkCrResult.setMessage("Không tìm thấy mã " + mopCode);
                    Util.checkAndPrintObject(logger, "linkCr:", "Result", "Khong tim thay ma" + mopCode);
                } else if (!flowRun.getCrNumber().equals(Constant.DEFAULT_CR_NUMBER_INFRA) && !flowRun.getCrNumber().equals(crNumber)) {
                    linkCrResult.setStatus(0);
                    linkCrResult.setMessage("CR " + flowRun.getCrNumber() + " không hợp lệ");
                    Util.checkAndPrintObject(logger, "linkCr:", "Result", "CR " + flowRun.getCrNumber() + "khong hop le");
                } else {
                    if (crState.intValue() == 6) {
                        flowRun.setStatus(1l);
                    }
                    flowRun.setCrNumber(crNumber);
                    flowRun.setCrStatus(crState);
                    //Quytv7
                    Date startDate;
                    Date endDate;
                    try {
                        startDate = sdf.parse(startTime);
                        endDate = sdf.parse(endTime);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        linkCrResult.setStatus(0);
                        linkCrResult.setMessage("Sai định dạng ngày tháng dd/MM/yyyy HH:mm:ss");
                        Util.checkAndPrintObject(logger, "linkCr:", "Result", "Sai dinh dang ngay thang dd/MM/yyyy HH:mm:ss");
                        return linkCrResult;
                    }

                    flowRun.setTimeRun(startDate);
                    Util.checkAndPrintObject(logger, "Link Cr thanh cong cho mop ha tang:", "crNumber", crNumber, "mopCode", mopCode,
                            "username", username, "crName", crName, "startTime", startTime, "endTime", endTime, "crState", crState);
                    service.saveOrUpdate(flowRun);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, DateTimeUtils.convertStringToDate(startTime, "dd/MM/yyyy HH:mm:ss"), new Date(), getRemoteIp(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), TdttWebserviceImpl.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.CREATE,
                                flowRun.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                    linkCrResult.setStatus(1);
                    // In case BD database, create new record in table flow_run_action with service_action_id. Use it do update info in table action
                    if (flowRun.getServiceActionId() != null) {
                        action = new ActionServiceImpl().findById(flowRun.getServiceActionId());
                        if (action != null) {
                            logger.info("Set crStatus for mop service with mop ha tang " + crNumber + "\t" + action.getTdCode() + "\t" + username + "\t" + crName + "\t" + startTime + "\t" + endTime + "\t" + crState);
                            linkCrResult = processForActionService(linkCrResult, crNumber, action.getTdCode(), crName, startTime, endTime, crState);
                        }
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                linkCrResult.setStatus(0);
                Util.checkAndPrintObject(logger, "Link Cr that bai cho mop ha tang:", "crNumber", crNumber, "mopCode", mopCode,
                        "username", username, "crName", crName, "startTime", startTime, "endTime", endTime, "crState", crState);
            }
            // end hanhnv68 add 2016_12_09

        } else {

            linkCrResult = processForActionService(linkCrResult, crNumber, mopCode, crName, startTime, endTime, crState);

        }
        //Quytv7_20200206_bo sung gan lai CR khi chon lai mop start
        if (linkCrResult.getStatus() != null && linkCrResult.getStatus() == 1) {
            try {
                logger.info("Bat dau set default cho cac mops ha tang cu thuoc CR: " + crNumber);
                Map<String, Object> filters = new HashMap<>();
                filters.put("crNumber-" + FlowRunActionServiceImpl.EXAC, crNumber);
//            if (!Util.isNullOrEmpty(mopId)) {
//                filters.put("flowRunId-" + FlowRunActionServiceImpl.NEQ, mopId);
//            }

                List<FlowRunAction> flowRunsExist = new FlowRunActionServiceImpl().findList(filters);

                if (flowRunsExist != null && !flowRunsExist.isEmpty()) {
                    logger.info("Ton tai " + flowRunsExist.size() + " mops ha tang cho CR: " + crNumber);
                    for (FlowRunAction flow : flowRunsExist) {
                        if (mopId != null) {
                            if (mopId.equals(flow.getFlowRunId())) {
                                logger.info("Trung voi mop ha tang truyen sang nen khong set default " + mopId);
                                continue;
                            }
                        }
                        //20181003_Sua khong cho cap nhat so CR neu cr status la cac truong hop khong duoc add lai mop
                        logger.info("Gan lai status =0, cr_number = 'CR_DEFAULT',crStatus = null cho cac dt da map voi CR nay: " + flow.getFlowRunId());

                        flow.setStatus(0l);
                        flow.setCrStatus(null);
                        flow.setCrNumber(Config.CR_DEFAULT);
                    }
                    new FlowRunActionServiceImpl().saveOrUpdate(flowRunsExist);
                } else {
                    logger.info("Ton tai 0 mops ha tang khac mopId cho CR: " + crNumber);
                }
                logger.info("Bat dau set default cho cac mops dich vu cu thuoc CR: " + crNumber);
                filters.clear();
                filters.put("crNumber-" + FlowRunActionServiceImpl.EXAC, crNumber);

//            if (action != null) {
//                filters.put("id-" + FlowRunActionServiceImpl.NEQ, action.getId());
//            }
                List<Action> actionList = new ActionServiceImpl().findList2(filters, new HashMap<String, String>());
                if (actionList != null && !actionList.isEmpty()) {
                    logger.info("Ton tai " + actionList.size() + " mops dich vu cho CR: " + crNumber);
                    for (Action ac : actionList) {
                        if (action != null) {
                            if (action.getId().equals(ac.getId())) {
                                logger.info("Trung voi mop dich vu lien ket mop ha tang nen khong set default " + ac.getId());
                                continue;
                            }
                        }
                        if (mopCode != null) {
                            if (mopCode.equals(ac.getTdCode())) {
                                logger.info("Trung voi mop dich vu truyen sang nen khong set default " + ac.getId());
                                continue;
                            }
                        }
                        //20181003_Sua khong cho cap nhat so CR neu cr status la cac truong hop khong duoc add lai mop
                        logger.info("Gan lai setLinkCrTime =null, cr_number = 'CR_CNTT_TEMP_999999',crStatus = null cho cac dt dich vu da map voi CR nay: " + ac.getId());
                        ac.setLinkCrTime(null);
                        ac.setCrState(null);
                        ac.setCrNumber(Constant.CR_DEFAULT);
                        ac.setUserExecute(null);
                    }
                    new ActionServiceImpl().saveOrUpdate(actionList);
                } else {
                    logger.info("Ton tai 0 mops ha tang khac mopId cho CR: " + crNumber);
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
            //Quytv7_20200206_bo sung gan lai CR khi chon lai mop end
        }
        return linkCrResult;
    }

    /**
     * Handle for case mop Code is not contain MOP_HT
     */
    private LinkCrResult processForActionService(LinkCrResult linkCrResult, String crNumber, String mopCode, String
            crName, String startTime, String endTime, Long crState) {
        Date startDate;
        Date endDate;
        try {
            startDate = sdf.parse(startTime);
            endDate = sdf.parse(endTime);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            linkCrResult.setStatus(0);
            linkCrResult.setMessage("Sai định dạng ngày tháng dd/MM/yyyy HH:mm:ss");
            return linkCrResult;
        }

        ActionService actionService = new ActionServiceImpl();
        try {
            Action action = actionService.findActionByCode(mopCode);
            if (action == null) {
                linkCrResult.setStatus(0);
                linkCrResult.setMessage("Không tìm thấy mã " + mopCode);
            } else if (!"CR_CNTT_TEMP_999999".equals(action.getCrNumber()) && !action.getCrNumber().equals(crNumber)) {
                linkCrResult.setStatus(0);
                linkCrResult.setMessage("CR " + action.getCrNumber() + " không hợp lệ");
//				} else if (action.getCreatedBy().equals(username)) {
            } else {
                ResourceBundle bundle = ResourceBundle.getBundle("config");
                CrForOtherSystemServiceImplService service = new CrForOtherSystemServiceImplServiceLocator();
                CrForOtherSystemService systemService = service.getCrForOtherSystemServiceImplPort(new URL(bundle.getString("ws_gnoc_new")));

                if (action.getLinkCrTime() == null) {
                    try {
                        File codeFile = new File(UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "HD download source code tac dong.docx");
                        systemService.insertFile(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), action.getCreatedBy(), crNumber, "100", "HD download source code tac dong.docx", Base64.encodeBase64String(FileUtils.readFileToByteArray(codeFile)));

                        if (action.getKbType() != null && action.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER)) {
                            codeFile = new File(UploadFileUtils.getResourceFolder() + File.separator + "file-template" + File.separator + "Guideline Reboot Server.docx");
                            systemService.insertFile(bundle.getString("ws_gnoc_user"), PasswordEncoder.decrypt(bundle.getString("ws_gnoc_pass")), action.getCreatedBy(), crNumber, "100", "Guideline Reboot Server.docx", Base64.encodeBase64String(FileUtils.readFileToByteArray(codeFile)));
                        }
                    } catch (RemoteException e) {
                        logger.error(e.getMessage(), e);
                    } catch (IOException e) {
                        logger.error(e.getMessage(), e);
                    }
                }

                String userExecute = null;
                CrOutputForQLTNDTO crOutputForQLTNDTO = GNOCService.getCrByCode(crNumber);
                if (crOutputForQLTNDTO != null)
                    userExecute = crOutputForQLTNDTO.getUserExecute();

                actionService.updateCr(action.getId(), crNumber, crName, startDate, endDate, crState, userExecute);
                linkCrResult.setStatus(1);
                Util.checkAndPrintObject(logger, "Link Cr thanh cong cho mop dich vu:", "crNumber", crNumber, "mopCode", mopCode,
                        "crName", crName, "startTime", startTime, "endTime", endTime, "crState", crState, "actionServiceId", action.getId(), "tdCode", action.getTdCode());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            linkCrResult.setMessage("Link CR thất bại vào mop");
            linkCrResult.setStatus(0);
        }
        return linkCrResult;
    }
    /*//Quytv7_20180830_ws cap nhat worklog start
    @Override
    public LinkCrResult sysWorklog(String wsUser, String wsPass, String mopCode, String worklogContent, Long worklogId) {
        logger.info(mopCode + "\t" + worklogContent + "\t" +worklogId);
        LinkCrResult linkCrResult = new LinkCrResult();
        linkCrResult.setStatus(0);

        if (!checkPass(wsUser, wsPass)) {
            linkCrResult.setMessage("Xác thực ws không đúng");
            return linkCrResult;
        }
        ActionService actionService = new ActionServiceImpl();
        try {
            Action action = actionService.findActionByCode(mopCode);
            if (action == null) {
                linkCrResult.setStatus(0);
                linkCrResult.setMessage("Không tìm thấy mã " + mopCode);
            } else {
                action.setWorkLog(worklogId);
                action.setWorkLogContent(worklogContent);

                actionService.saveOrUpdate(action);
                linkCrResult.setMessage("Update success");
                linkCrResult.setStatus(1);
            }
            //20180620_tudn_start ghi log DB
            try {
                LogUtils.logAction(LogUtils.appCode, new Date(), new Date(), getRemoteIp(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), TdttWebserviceImpl.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.CREATE,
                        action.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            //20180620_tudn_end ghi log DB
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            linkCrResult.setStatus(0);
            try {
                LogUtils.logAction(LogUtils.appCode, new Date(), new Date(), getRemoteIp(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), TdttWebserviceImpl.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.CREATE,
                        "Error" + e.getMessage(), LogUtils.getRequestSessionId());
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
            }
        }

        return linkCrResult;
    }
    //Quytv7_20180830_ws cap nhat worklog end*/


    @Override
    public MopFileResult getMopFile(String wsUser, String wsPass, String mopCode, String crNumber) {
        MopFileResult fileResult = new MopFileResult();
        fileResult.setStatus(0);
        logger.info(crNumber + "\t" + mopCode + "\t" + wsUser + "\t" + crNumber);
        if (!checkPass(wsUser, wsPass)) {
            fileResult.setMessage("Xác thực ws không đúng");
            return fileResult;
        }
        if (mopCode == null || mopCode.trim().isEmpty()) {
            fileResult.setMessage("Mã mopCode trống");
            return fileResult;
        }
        // hanhnv68 add 2016_12_09
        if (mopCode.trim().startsWith(Constant.PREFIX_MOP_INFRA)) {
            try {
                logger.info("vao get mop info ht");
                Long mopId = Long.valueOf(mopCode.trim().substring(Constant.PREFIX_MOP_INFRA.length()));
                FlowRunAction flowRun = (new FlowRunActionServiceImpl()).findById(mopId);
                if (flowRun != null) {
                    fileResult.setMopFileContent(Base64.encodeBase64String(flowRun.getFileContent()));
                    fileResult.setMopFile(flowRun.getFlowRunId()
                            + "_" + ZipUtils.clearHornUnicode(flowRun.getFlowRunName()) + ".xlsx");
                    fileResult.setMopRollbackFile(flowRun.getFlowRunId()
                            + "_rollback_" + ZipUtils.clearHornUnicode(flowRun.getFlowRunName()) + ".xlsx");
                    fileResult.setMopRollbackFileContent(Base64.encodeBase64String(flowRun.getFileContent()));
                    fileResult.setStatus(1);

                } else {
                    fileResult.setMessage("Không tìm thấy mã " + mopCode);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                fileResult.setStatus(0);
            }
            // end hanhnv68 add 2016_12_09


        } else {
            ActionService actionService = new ActionServiceImpl();
            try {
                Action action = actionService.findActionByCode(mopCode);
                if (action == null) {
                    fileResult.setStatus(0);
                    fileResult.setMessage("Không tìm thấy mã " + mopCode);
                } else {
                    /*String template = getUploadFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_DT_NEW.docx";
					String templateRollBack = getUploadFolder() + File.separator + "file-template" + File.separator + "TEMPLATE_DT_ROLLBACK.docx";
					String source = UploadFileUtils.getMopFolder(action);
	
					File file1 = new File(source);
					if (!file1.exists())
						file1.mkdir();
	
					new DocxUtil().genericDT(action, template, templateRollBack, source, fileResult);*/

                    List<Integer> kbGroups = actionService.findKbGroups(action.getId());

                    File mopDir = new File(UploadFileUtils.getMopFolder(action));
                    //				if (mopDir.exists()) {
                    DocxUtil.export(action, crNumber);
                    //				}

                    File mopFile = null;
                    File mopRollbackFile = null;
                    File[] files = mopDir.listFiles();
                    if (files != null) {
                        for (File file : files) {
                            if (file.getPath().endsWith(kbGroups.size() < 2 ? "tacdong_" + kbGroups.get(0) + ".docx" : "tacdong_.zip"))
                                mopFile = file;

                            if (file.getPath().endsWith(kbGroups.size() < 2 ? "rollback_" + kbGroups.get(0) + ".docx" : "rollback_.zip"))
                                mopRollbackFile = file;
                        }
                    }

                    if (mopFile != null && mopRollbackFile != null) {
                        fileResult.setMopFile(mopFile.getName());
                        fileResult.setMopRollbackFile(mopRollbackFile.getName());
                        fileResult.setMopFileContent(Base64.encodeBase64String(FileUtils.readFileToByteArray(mopFile)));
                        fileResult.setMopRollbackFileContent(Base64.encodeBase64String(FileUtils.readFileToByteArray(mopRollbackFile)));

                        fileResult.setStatus(1);
                    }
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                fileResult.setStatus(0);
            }
        }
        return fileResult;
    }

    @Override
    public AppGroupResult getListAppGroup(String wsUser, String wsPass) {
        logger.info("getListAppGroup");
        AppGroupResult appGroupResult = new AppGroupResult();
        appGroupResult.setStatus(0);
        if (!checkPass(wsUser, wsPass)) {
            appGroupResult.setMessage("Xác thực ws không đúng");
            return appGroupResult;
        }

        try {
            List<AppGroup> appGroups = new ArrayList<>();
            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_SERVICES_AAM, 2457, null);
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, AamConstants.NATION_CODE.VIETNAM);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            List<Service> services = objectMapper.readValue(data, new TypeReference<List<Service>>() {
            });
            for (Service service : services) {
                AppGroup appGroup = new AppGroup();
                appGroup.setAppGroupId(service.getServiceId());
                appGroup.setApplicationName(service.getServiceName());
                appGroup.setApplicationCode(service.getServiceCode());
                appGroup.setServiceStatus(service.getServiceStatus());

                appGroups.add(appGroup);
            }

            appGroupResult.setStatus(1);
            appGroupResult.setAppGroups(appGroups);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return appGroupResult;
    }

    @Override
    public IpServiceResult getListIpGroup(String wsUser, String wsPass, Long appGroupId) {
        logger.info("getListIpGroup " + appGroupId);
        IpServiceResult ipServiceResult = new IpServiceResult();
        ipServiceResult.setStatus(0);
        if (!checkPass(wsUser, wsPass)) {
            ipServiceResult.setMessage("Xác thực ws không đúng");
            return ipServiceResult;
        }

        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "serviceId", null, null, String.valueOf(appGroupId)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_IP_SERVICE_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, AamConstants.NATION_CODE.VIETNAM);

            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            List<Map<String, Object>> mapObjects = objectMapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {
            });
            List<String> ips = new ArrayList<>();
            for (Map<String, Object> object : mapObjects) {
                ips.add((String) object.get("IP_SERVER"));
            }

            ipServiceResult.setIpAddress(ips);
            ipServiceResult.setStatus(1);
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return ipServiceResult;
    }

    @Override
    public LinkCrResult updateRunAutoStatus(String wsUser, String wsPass, String crNumber, String mopCode, Boolean runAuto) {
        logger.info("updateRunAutoStatus for " + crNumber + "\t" + mopCode + "\t" + runAuto);
        LinkCrResult linkCrResult = new LinkCrResult();
        linkCrResult.setStatus(1);
        linkCrResult.setMessage("OK");

        if (!checkPass(wsUser, wsPass)) {
            linkCrResult.setStatus(0);
            linkCrResult.setMessage("User or password  incorrect");
            logger.info(linkCrResult.getMessage());
            return linkCrResult;
        }
        if (isNullOrEmpty(crNumber) || isNullOrEmpty(mopCode) || runAuto == null) {
            linkCrResult.setStatus(0);
            linkCrResult.setMessage("This input not enough");
            logger.info(linkCrResult.getMessage());
            return linkCrResult;
        }
        if (crNumber.equalsIgnoreCase(Constant.CR_DEFAULT) || crNumber.equalsIgnoreCase(Constant.DEFAULT_CR_NUMBER_INFRA) || crNumber.equalsIgnoreCase(com.viettel.it.util.Config.CR_AUTO_DECLARE_CUSTOMER)) {
            linkCrResult.setStatus(0);
            linkCrResult.setMessage("CR number malformed");
            logger.info(linkCrResult.getMessage());
            return linkCrResult;
        }
        if (mopCode.trim().startsWith(Constant.PREFIX_MOP_INFRA)) {
            try {
                Long mopId = Long.valueOf(mopCode.trim().substring(Constant.PREFIX_MOP_INFRA.length()));
                if (mopId == null) {
                    linkCrResult.setStatus(0);
                    linkCrResult.setMessage("Not found mop in system: " + mopCode);
                    logger.info(linkCrResult.getMessage());
                    return linkCrResult;
                } else {
                    Map<String, Object> filters = new HashMap<>();
                    filters.put("crNumber-EXAC", crNumber);
                    filters.put("flowRunId-EXAC", mopId);
                    List<FlowRunAction> flowRunActions = new FlowRunActionServiceImpl().findList(filters);
                    if (flowRunActions == null || flowRunActions.isEmpty()) {
                        linkCrResult.setStatus(0);
                        linkCrResult.setMessage("Not found cr number in system: " + crNumber);
                        logger.info(linkCrResult.getMessage());
                        return linkCrResult;
                    } else {
                        for (FlowRunAction flowRunAction : flowRunActions) {
                            flowRunAction.setRunAuto((runAuto != null && runAuto) ? 1L : 0L);
                        }
                        new FlowRunActionServiceImpl().saveOrUpdate(flowRunActions);
                        logger.info("updateRunAutoStatus success for: " + crNumber + "\t" + mopCode + "\t" + runAuto);
                    }
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                linkCrResult.setStatus(0);
                linkCrResult.setMessage(ex.getMessage());
            }
        } else {
            try {
                ActionService actionService = new ActionServiceImpl();
                Map<String, Object> filters = new HashMap<>();
                filters.put("crNumber", crNumber);
                List<Action> actions = actionService.findList(filters, new HashMap<>());

                if ((actions == null || actions.isEmpty())) {
                    linkCrResult.setStatus(0);
                    linkCrResult.setMessage("Not found cr number in system: " + crNumber);
                    logger.info(linkCrResult.getMessage());
                } else {
                    Action updateAction = null;
                    for (Action action : actions) {
                        if (!action.getCrNumber().equals(crNumber)) {
                            continue;
                        }
                        if (mopCode.equals(action.getTdCode())) {
                            updateAction = action;
                        } else {
                            linkCrResult.setStatus(0);
                            linkCrResult.setMessage("CR " + crNumber + " was link with other service mop: " + mopCode);
                            logger.info(linkCrResult.getMessage());
                            return linkCrResult;
                        }
                    }

                    if (updateAction == null) {
                        linkCrResult.setStatus(0);
                        linkCrResult.setMessage("Not found cr number in system: " + crNumber);
                        logger.info(linkCrResult.getMessage());
                        return linkCrResult;
                    }
                    actionService.updateAutoRunCr(updateAction.getId(), runAuto, null, null, null, null);
                    logger.info("updateRunAutoStatus success for: " + crNumber + "\t" + mopCode + "\t" + runAuto);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                linkCrResult.setStatus(0);
                linkCrResult.setMessage(e.getMessage());
            }
        }
        return linkCrResult;
    }

    @Override
    public LinkCrResult updateRunAutoStatusNew(String wsUser, String wsPass, String crNumber, String mopCode, Boolean
            runAuto, Long typeConfirmGNOC, Long typeRunGNOC, String crLinkGNOC) {

        Util.checkAndPrintObject(logger, "updateRunAutoStatus:", "runAuto", runAuto, "crNumber", crNumber,
                "typeConfirmGNOC", typeConfirmGNOC, "typeRunGNOC", typeRunGNOC, "crLinkGNOC", crLinkGNOC, "mopCode", mopCode);
        LinkCrResult linkCrResult = new LinkCrResult();
        linkCrResult.setStatus(1);
        linkCrResult.setMessage("OK");

        if (!checkPass(wsUser, wsPass)) {
            linkCrResult.setStatus(0);
            linkCrResult.setMessage("User or password  incorrect");
            logger.info(linkCrResult.getMessage());
            return linkCrResult;
        }
        if (isNullOrEmpty(crNumber) || isNullOrEmpty(mopCode) || runAuto == null || typeConfirmGNOC == null || typeRunGNOC == null) {
            linkCrResult.setStatus(0);
            linkCrResult.setMessage("Please enter all input information and enter correct");
            logger.info(linkCrResult.getMessage());
            return linkCrResult;
        }
        if (typeConfirmGNOC != null && (typeConfirmGNOC.compareTo(0L) != 0 && typeConfirmGNOC.compareTo(1L) != 0)) {
            linkCrResult.setStatus(0);
            linkCrResult.setMessage("typeConfirmGNOC can only enter 0 or 1");
            return linkCrResult;
        }
        if (typeRunGNOC != null && (typeRunGNOC.compareTo(0L) != 0 && typeRunGNOC.compareTo(1L) != 0)) {
            linkCrResult.setStatus(0);
            linkCrResult.setMessage("typeRunGNOC can only enter 0 or 1");
            return linkCrResult;
        }
        if (crNumber.equalsIgnoreCase(Constant.CR_DEFAULT) || crNumber.equalsIgnoreCase(Constant.DEFAULT_CR_NUMBER_INFRA) || crNumber.equalsIgnoreCase(com.viettel.it.util.Config.CR_AUTO_DECLARE_CUSTOMER)) {
            linkCrResult.setStatus(1);
            linkCrResult.setMessage("CR number malformed");
            logger.info(linkCrResult.getMessage());
            return linkCrResult;
        }
        if (mopCode.trim().startsWith(Constant.PREFIX_MOP_INFRA)) {
            try {
                Long mopId = Long.valueOf(mopCode.trim().substring(Constant.PREFIX_MOP_INFRA.length()));
                if (mopId == null) {
                    linkCrResult.setStatus(0);
                    linkCrResult.setMessage("Not found mop in system: " + mopCode);
                    logger.info(linkCrResult.getMessage());
                    return linkCrResult;
                } else {
                    Map<String, Object> filters = new HashMap<>();
                    filters.put("crNumber-EXAC", crNumber);
                    filters.put("flowRunId-EXAC", mopId);
                    List<FlowRunAction> flowRunActions = new FlowRunActionServiceImpl().findList(filters);
                    if (flowRunActions == null || flowRunActions.isEmpty()) {
                        linkCrResult.setStatus(0);
                        linkCrResult.setMessage("Not found cr number in system: " + crNumber);
                        logger.info(linkCrResult.getMessage());
                        return linkCrResult;
                    } else {
                        for (FlowRunAction flowRunAction : flowRunActions) {
                            flowRunAction.setRunAuto((runAuto != null && runAuto) ? 1L : 0L);
                            flowRunAction.setTypeConfirmGNOC(typeConfirmGNOC);
                            flowRunAction.setTypeRunGNOC(typeRunGNOC);
                            flowRunAction.setCrLinkGNOC(crLinkGNOC);
                            Util.checkAndPrintObject(logger, "updateRunAutoStatus:", "runAuto", runAuto, "crNumber", crNumber,
                                    "typeConfirmGNOC", typeConfirmGNOC, "typeRunGNOC", typeRunGNOC, "crLinkGNOC", crLinkGNOC, "flowRunId", flowRunAction.getFlowRunId());
                        }
                        new FlowRunActionServiceImpl().saveOrUpdate(flowRunActions);
                        Util.checkAndPrintObject(logger, "updateRunAutoStatus success for:", "runAuto", runAuto, "crNumber", crNumber,
                                "typeConfirmGNOC", typeConfirmGNOC, "typeRunGNOC", typeRunGNOC, "crLinkGNOC", crLinkGNOC);
                    }
                }
            } catch (Exception ex) {
                logger.error(ex.getMessage(), ex);
                linkCrResult.setStatus(0);
                linkCrResult.setMessage(ex.getMessage());
            }
        } else {
            try {
                ActionService actionService = new ActionServiceImpl();
                Map<String, Object> filters = new HashMap<>();
                filters.put("crNumber-EXAC", crNumber);
                List<Action> actions = actionService.findList2(filters, new HashMap<>());

                if ((actions == null || actions.isEmpty())) {
                    linkCrResult.setStatus(0);
                    linkCrResult.setMessage("Not found cr number in system: " + crNumber);
                    logger.info(linkCrResult.getMessage());
                } else {
                    Action updateAction = null;
                    for (Action action : actions) {
                        if (!action.getCrNumber().equals(crNumber)) {
                            continue;
                        }
                        if (mopCode.equals(action.getTdCode())) {
                            updateAction = action;
                        } else {
                            linkCrResult.setStatus(0);
                            linkCrResult.setMessage("CR " + crNumber + " was link with other service mop: " + mopCode);
                            logger.info(linkCrResult.getMessage());
                            return linkCrResult;
                        }
                    }

                    if (updateAction == null) {
                        linkCrResult.setStatus(0);
                        linkCrResult.setMessage("Not found cr number in system: " + crNumber);
                        logger.info(linkCrResult.getMessage());
                        return linkCrResult;
                    }
                    actionService.updateAutoRunCr(updateAction.getId(), runAuto, typeConfirmGNOC, typeRunGNOC, crLinkGNOC, null);
                    Util.checkAndPrintObject(logger, "updateRunAutoStatus success for:", "runAuto", runAuto, "crNumber", crNumber,
                            "typeConfirmGNOC", typeConfirmGNOC, "typeRunGNOC", typeRunGNOC, "crLinkGNOC", crLinkGNOC, "actionId", updateAction.getId());
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                linkCrResult.setStatus(0);
                linkCrResult.setMessage(e.getMessage());
            }
        }
        return linkCrResult;
    }

	/*private List<String> getListIP(Long actionId) {
		List<String> ips = new ArrayList<>();
		ActionModuleServiceImpl service = new ActionModuleServiceImpl();
		ActionDetailDatabaseServiceImpl serviceDetailDb = new ActionDetailDatabaseServiceImpl();
		ApplicationDetailServiceImpl serviceApp = new ApplicationDetailServiceImpl();
		try {
			List<ActionDetailDatabase> listDetails = serviceDetailDb.findListDetailDb(actionId, true);
			List<Long> ids = service.findListModuleId(actionId);
			List<ApplicationDetail> lstApp = serviceApp.getObjByListAppId(ids);
			if (lstApp != null) {

				for (ApplicationDetail applicationDetail : lstApp) {
					if (!ips.contains(applicationDetail.getIpServer()))
						ips.add(applicationDetail.getIpServer());
				}
			}

			if (listDetails != null) {
				for (ActionDetailDatabase db : listDetails) {
					ips.add(db.getServiceDb().getIpServer());
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return ips;
	}*/

    private boolean checkPass(String username, String password) {
        return username != null && password != null && "tdtt".equals(username) && "tdtt_vtnet$%^".equals(password);
    }


    //20190524_tudn_start tac dong toan trinh SR GNOC
    public String getTemplateMultiExport(String fileName) {
        try {
            ServletContext ctx = (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
            return ctx.getRealPath("/") + File.separator + "templates" + File.separator + fileName;
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return "";
        }

    }

    public String getFolderSave() {
        String pathOut;
        ServletContext servletContext = (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);
        pathOut = servletContext.getRealPath("/") + com.viettel.it.util.Config.PATH_OUT;
        File folderOut = new File(pathOut);
        if (!folderOut.exists()) {
            folderOut.mkdirs();
        }
        return pathOut;
    }
    //20190524_tudn_start tac dong toan trinh SR GNOC


    //20190524_tudn_start tac dong toan trinh SR GNOC
    @Override
    public ResultGetListTemplatesByProcedure getListTemplatesByProcedure(String userService, String
            passService, String countryCode
            , String procedureId, String workFlowId) {
        ResultGetListTemplatesByProcedure output = new ResultGetListTemplatesByProcedure();
        FlowTemplatesServiceImpl flowTemplatesService = new FlowTemplatesServiceImpl();
        logger.info("getListTemplatesByProcedure procedureId: " + procedureId + ",workFlowId:" + workFlowId);
        try {
            if (!checkPass(userService, passService)) {
                output.setResultCode(1);
                output.setResultMessage("userService or passService is invalid");
                return output;
            }
        } catch (Exception ex) {
            output.setResultCode(1);
            output.setResultMessage(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            return output;
        }

        try {
            if (Util.isNullOrEmpty(countryCode)) {
                output.setResultCode(1);
                output.setResultMessage("countryCode is invalid");
                return output;
            } else {
                try {
                    Map<String, Object> filter = new HashMap<>();
                    filter.put("countryCode-EXAC", countryCode);
                    CatCountryServiceImpl countryService = new CatCountryServiceImpl();
                    List<CatCountryBO> lstCountryBOS = countryService.findList(filter);
                    if (lstCountryBOS == null || lstCountryBOS.isEmpty()) {
                        output.setResultCode(1);
                        output.setResultMessage("countryCode is invalid");
                        return output;
                    }
                } catch (Exception ex) {
                    output.setResultCode(1);
                    output.setResultMessage(ex.getMessage());
                    logger.error(ex.getMessage(), ex);
                    return output;
                }
            }
            if ("VNM".equals(countryCode))
                MessageUtil.setLocal(new Locale("vi", "VN"));
            else
                MessageUtil.setLocal(new Locale("en", "US"));
            if (Util.isNullOrEmpty(procedureId)) {
                output.setResultCode(1);
                output.setResultMessage("proceduleId is not empty");
                return output;
            }
            if (!NumberUtils.isNumber(procedureId)) {
                output.setResultCode(1);
                output.setResultMessage("Enter procedureId is a number");
                return output;
            }
            if (!Util.isNullOrEmpty(workFlowId) && !NumberUtils.isNumber(workFlowId)) {
                output.setResultCode(1);
                output.setResultMessage("Enter workFlowId is a number");
                return output;
            }
//            StringBuilder sql = new StringBuilder();
//            Map<String, Object> paramlist = new HashMap<>();
//            sql.append("select FLOW_TEMPLATES_ID from FLOW_TEMPLATES where STATUS = 9 AND PROCEDURE_ID = :proceduleId  ");
//            paramlist.put("proceduleId", procedureId);
//            if (!Util.isNullOrEmpty(workFlowId)) {
//                sql.append(" AND PROCEDURE_WORK_FLOW_ID = :workFlowId");
//                paramlist.put("workFlowId", workFlowId);
//            }
            StringBuilder sb = new StringBuilder();
            sb.append("select FLOW_TEMPLATE_ID from PROCEDURE_GNOC_TEMPLATE where PROCEDURE_GNOC_ID = ?");
            List<?> flowTemplateId = flowTemplatesService.findListSQLAll(sb.toString(), Long.valueOf(Util.isNullOrEmpty(workFlowId)?procedureId:workFlowId));
            List<FlowTemplateGNOCObjDTO> lstId = new ArrayList<>();
            if (flowTemplateId != null && !flowTemplateId.isEmpty()) {
                for (Object object : flowTemplateId) {
                    BigDecimal cols = (BigDecimal) object;
                    lstId.add(new FlowTemplateGNOCObjDTO(cols.toString()));
                }
            } else {
                output.setResultCode(1);
                output.setResultMessage("Input don’t have template");
                return output;
            }


            boolean approved = true;
            for (FlowTemplateGNOCObjDTO obj : lstId) {
                FlowTemplates flowTemplates = new FlowTemplatesServiceImpl().findById(Long.parseLong(obj.getTemplateId()));
                if (flowTemplates != null && flowTemplates.getStatus() != null) {
                    if (flowTemplates.getStatus() == 9) {
                        try {
                            boolean flagExport = true;
//                                String file = getTemplateMultiExport("VNM".equals(countryCode) ? "Template_Import_DT.xlsx" : "Template_Import_DT_EN.xlsx");
                            String file = getTemplateMultiExport(MessageUtil.getResourceBundleMessage("key.template.file.import.param"));
                            GenerateFlowRunController generateFlowRunController = new GenerateFlowRunController();
                            generateFlowRunController.setNodes(new ArrayList<>());
//                                generateFlowRunController.setSubFlowRuns(new ArrayList<>());
                            generateFlowRunController.setSelectedFlowTemplates(flowTemplates);
                            String fileName = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + "_" + flowTemplates.getFlowTemplateName() + "_" + MessageUtil.getResourceBundleMessage("key.template.file.import.param");
                            String fileOut = getFolderSave() + "OutputWs" + File.separator + "ResultFileTemplate" + File.separator + fileName;
                            File fileOutResult = new File(fileOut);
                            if (!fileOutResult.getParentFile().exists()) {
                                fileOutResult.getParentFile().mkdirs();
                            }
                            InputStream is = new FileInputStream(file);
                            OutputStream os = new FileOutputStream(fileOutResult);
                            Context context = new Context();
                            List<ObjectImportDt> params = new LinkedList<>();
                            List<String> sheetNames = new LinkedList<>();
                            generateFlowRunController.getContextVar(params, sheetNames);
                            context.putVar("params", params);
                            context.putVar("sheetNames", sheetNames);
                            if (params.size() > 0 && sheetNames.size() > 0) {
                                JxlsHelper.getInstance().setDeleteTemplateSheet(true).processTemplateAtCell(is, os, context, "Sheet2!A1");
                            } else {
                                flagExport = false;
                                approved = false;
                            }


                            if (flagExport) {
                                byte[] encodedResult = Base64.encodeBase64(FileUtils.readFileToByteArray(fileOutResult));
                                String content = new String(encodedResult, StandardCharsets.US_ASCII);
                                obj.setTempFileContent(content);
                                obj.setTempFileName(fileName);
                                obj.setResultCode(0);
                                obj.setResultMessage("Get tempFileContent for templateId: " + obj.getTemplateId() + " success");
                            } else {
                                obj.setResultCode(1);
                                obj.setResultMessage("Can’t get tempFileContent for templateId: " + obj.getTemplateId());
                            }
                        } catch (Exception e) {
                            approved = false;
                            logger.error(e.getMessage(), e);
                            obj.setResultCode(1);
                            obj.setResultMessage("Can’t get tempFileContent for templateId: " + obj.getTemplateId());
                        }
                    } else {
                        approved = false;
                        obj.setResultCode(1);
                        obj.setResultMessage("TemplateId: " + obj.getTemplateId() + " haven't approved");
                    }
                }
            }

            if (approved) {
                output.setResultCode(0);
                output.setResultMessage("SUCCESS");
                output.setFlowTemplatesObj(lstId);
            } else {
                output.setResultCode(1);
                output.setResultMessage("FAIL");
                output.setFlowTemplatesObj(lstId);
            }
        } catch (Exception ex) {
            output.setResultCode(1);
//            output.setResultMessage(ex.getMessage());
            output.setResultMessage("Input don’t have template");
            logger.error(ex.getMessage(), ex);
        }
        return output;
    }
    //20190524_tudn_end tac dong toan trinh SR GNOC

    //20190524_tudn_start tac dong toan trinh SR GNOC
    @Override
    public ResultDeleteDt deleteDts(String userService, String passService,
                                    String systemCreateId, List<DtObjDelete> listDtDelete) {
        ResultDeleteDt output = new ResultDeleteDt();

        Date startTime = new Date();
        logger.info("deleteDts mopIds: " + listDtDelete);
        try {
            if (!checkPass(userService, passService)) {
                output.setResultCode(1);
                output.setResultMessage("userService or passService is invalid");
                return output;
            }
        } catch (Exception ex) {
            output.setResultCode(1);
            output.setResultMessage(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            return output;
        }

        Map<Long, Boolean> mapResult = new HashMap<>();
        try {
            // check dau vao
            if (Util.isNullOrEmpty(systemCreateId)) {
                output.setResultCode(1);
                output.setResultMessage("Enter systemCreateId is a number");
                return output;
            } else {
                if (!NumberUtils.isNumber(systemCreateId)) {
                    output.setResultCode(1);
                    output.setResultMessage("Enter systemCreateId is a number");
                    return output;
                }
            }
            if (listDtDelete == null || listDtDelete.isEmpty()) {
                output.setResultCode(1);
                output.setResultMessage("The list of listDtDelete is empty");
                return output;
            }
            List<DtObjDTO> lstResultDel = new ArrayList<>();
            for (DtObjDelete mopDTO : listDtDelete) {
                if (Util.isNullOrEmpty(mopDTO.getDeleteDtId())) {
                    output.setResultCode(1);
                    output.setResultMessage("deleteDtId in the list of listDtDelete is not empty");
                    break;
                }
                if (!NumberUtils.isNumber(mopDTO.getDeleteDtId())) {
                    output.setResultCode(1);
                    output.setResultMessage("deleteDtId in the list of listDtDelete must be a number");
                    break;
                }

                DtObjDTO dto = new DtObjDTO();
                dto.setDeleteDtId(mopDTO.getDeleteDtId());
                delFlowRunAction(systemCreateId, dto, mapResult);
                lstResultDel.add(dto);
            }
            if (listDtDelete.size() == mapResult.size()) {
                output.setResultCode(0);
                output.setResultMessage("SUCCESS");
                output.setListDtDelete(lstResultDel);
            } else {
                output.setResultCode(1);
                output.setResultMessage("Delete MOP fail");
                output.setListDtDelete(lstResultDel);
            }
            if (mapResult.size() > 0) {
                String logAction = "CONTENT = Delete DT, Result: Success, List DT: DeleteDtId (";
                int i = 0;
                for (Map.Entry<Long, Boolean> entry : mapResult.entrySet()) {
                    i++;
                    if (entry.getValue())
                        logAction = logAction + entry.getKey() + (i == mapResult.size() ? "" : ", ");
                }
                logAction = logAction + ")";
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), "system",
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), TdttWebserviceImpl.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.DELETE,
                        logAction, LogUtils.getRequestSessionId());
            }
        } catch (Exception ex) {
            output.setResultCode(1);
            output.setResultMessage(ex.getMessage());
            logger.error(ex.getMessage(), ex);
        }
        return output;
    }

    private void delFlowRunAction(String systemCreateId, DtObjDTO mopDTO, Map<Long, Boolean> mapResult) {
        try {
            Map<String, Object> filter = new HashMap<>();
            filter.put("flowRunId-EXAC", Long.parseLong(mopDTO.getDeleteDtId()));
            filter.put("status-EXAC", 0L);
            filter.put("systemCreateId-EXAC", systemCreateId);
            FlowRunActionServiceImpl flowRunActionService = new FlowRunActionServiceImpl();
            List<FlowRunAction> lstFlowRunActions = flowRunActionService.findList(filter);

            if (lstFlowRunActions != null && !lstFlowRunActions.isEmpty()) {
                for (FlowRunAction flowRunAction : lstFlowRunActions) {
                    try {
                        Object[] objs = new FlowRunActionServiceImpl().openTransaction();
                        Session session = (Session) objs[0];
                        Transaction tx = (Transaction) objs[1];
                        new AccountGroupMopServiceImpl().execteBulk2("delete from AccountGroupMop where flowRunId = ?", session, tx, false, flowRunAction.getFlowRunId());
                        new ParamValueServiceImpl().execteBulk2("delete from ParamValue where nodeRun.id.flowRunId = ?", session, tx, false, flowRunAction.getFlowRunId());
                        new NodeRunGroupActionServiceImpl().execteBulk2("delete from NodeRunGroupAction where id.flowRunId = ? ", session, tx, false, flowRunAction.getFlowRunId());
                        new NodeRunServiceImpl().execteBulk2("delete from NodeRun where id.flowRunId = ?", session, tx, false, flowRunAction.getFlowRunId());
                        new FlowRunActionServiceImpl().execteBulk2("delete from FlowRunAction where flowRunId = ? ", session, tx, true, flowRunAction.getFlowRunId());
                        mopDTO.setResultCode(0);
                        mopDTO.setResultMessage("Delete data successfull");
                        mapResult.put(flowRunAction.getFlowRunId(), true);
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        mopDTO.setResultCode(1);
                        mopDTO.setResultMessage("Error delete data");
                    }
                }
            } else {
                mopDTO.setResultCode(1);
                mopDTO.setResultMessage(MessageFormat.format("Not found DT with deleteDtId: {0} and systemCreateId: {1}", mopDTO.getDeleteDtId(), systemCreateId));
            }
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
        }
    }

    private Boolean validTemplate(Workbook
                                          workbook, List<String> sheetNames, List<ObjectImportDt> params, DtObjDTO result) {

        for (int i = 0; i < sheetNames.size(); i++) {

            String sheetName = sheetNames.get(i);

            Importer<Serializable> importer = new Importer<Serializable>() {

                @Override
                protected Map<Integer, String> getIndexMapFieldClass() {
                    return null;
                }

                @Override
                protected String getDateFormat() {
                    return null;
                }
            };

            Map<Integer, String> indexMapFieldClass = new HashMap<>();
            importer.setIndexMapFieldClass(indexMapFieldClass);
            importer.setRowHeaderNumber(6);
            importer.setIsReplaceSpace(false);

            List<Serializable> objects = importer.getDatas(workbook, sheetName, "6");

            if (objects != null && !objects.isEmpty()) {
                List<?> headers = new LinkedList<>();
                ((List<Object>) headers).addAll(objects);
                boolean haveStt = true;
                boolean haveNodeCode = true;
                for (int j = 0; j < headers.size(); j++) {
                    BasicDynaBean basicDynaBean = (BasicDynaBean) headers.get(j);
                    try {
                        Object stt = basicDynaBean.getMap().get(MessageUtil.getResourceBundleMessage("datatable.header.stt").replace(".", "_").toLowerCase());
                        if (stt == null)
                            haveStt = false;
                        Object node = basicDynaBean.getMap().get(MessageUtil.getResourceBundleMessage("key.nodeCode").toLowerCase());
                        if (node == null)
                            haveNodeCode = false;
                        for (ObjectImportDt objectImportDt : params) {
                            if (sheetName.equals(objectImportDt.getNodeType().getTypeName() + "-" + objectImportDt.getVendor().getVendorName() + "-" + objectImportDt.getVersion().getVersionName())) {
                                for (String str : objectImportDt.getParamNames()) {
                                    if (!basicDynaBean.getMap().containsKey(str.toLowerCase())) {
                                        result.setResultMessage("Column names are not the same as template");
                                        result.setResultCode(1);
                                        return false;
                                    }
                                }
                            } else {
                                continue;
                            }
                        }
                    } catch (Exception ex) {
                        result.setResultMessage("tempFileContent is not the same as the template");
                        result.setResultCode(1);
                        return false;
                    }
                }
                if (haveStt && haveNodeCode) {
                } else {
                    result.setResultMessage("Column names are not the same as template");
                    result.setResultCode(1);
                    return false;
                }
            } else {
                result.setResultMessage("tempFileContent is not the same as the template");
                result.setResultCode(1);
                return false;
            }
        }
        return true;
    }

    private Boolean validateNodeCode(List<?> objectImports, DtObjDTO dto, String sheetName, String countryCode) {
        List<String> lstNodeCode = new ArrayList<>();
        for (int i = 0; i < objectImports.size(); i++) {
            BasicDynaBean basicDynaBean = (BasicDynaBean) objectImports.get(i);
            try {
                Object nodeCode = basicDynaBean.getMap().get(MessageUtil.getResourceBundleMessage("key.nodeCode").toLowerCase());
                if (nodeCode != null && !"".equals(nodeCode.toString())) {
                    if (!lstNodeCode.contains(nodeCode)) {
                        lstNodeCode.add(nodeCode.toString());
                        Node node = null;
                        HashMap<String, Object> filters = new HashMap<>();
                        filters.clear();
                        filters.put("nodeCode-EXAC", nodeCode);
                        logger.info("---bat dau lay danh sach node chay" + nodeCode + "---");
                        List<Node> nodesTemp = new NodeServiceImpl().findList(filters);
                        if (nodesTemp == null || nodesTemp.isEmpty()) {
                            dto.setResultCode(1);
                            dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage())
                                    ? "Node was not found:" + nodeCode
                                    : dto.getResultMessage() + ";\n Node was not found:" + nodeCode);
                            continue;
                        }
                        node = nodesTemp.get(0);
                        logger.info("---ket thuc lay danh sach node chay" + nodeCode + "---");
                        if (node == null) {
                            logger.info("---khong tim kiem duoc node mang: " + nodeCode + "---");
                            dto.setResultCode(1);
                            dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage())
                                    ? "Node was not found:" + nodeCode
                                    : dto.getResultMessage() + ";\n Node was not found:" + nodeCode);
                            continue;
                        } else {
                            if (!sheetName.equals(node.getNodeType().getTypeName() + "-" + node.getVendor().getVendorName() + "-" + node.getVersion().getVersionName())) {
                                dto.setResultCode(1);
                                dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage())
                                        ? "Node :" + nodeCode + " don't belong in vendor-version-nodetype of template"
                                        : dto.getResultMessage() + "\n;Node :" + nodeCode + " don't belong in vendor-version-nodetype of template");
                                continue;
                            }
                            if (!node.getActive().equals(1L)) {
                                dto.setResultCode(1);
                                dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage())
                                        ? "Node: " + nodeCode + " was not activated"
                                        : dto.getResultMessage() + "\n;Node: " + nodeCode + " was not activated");
                                continue;
                            }
                            if (!node.getCountryCode().getCountryCode().equals(countryCode)) {
                                dto.setResultCode(1);
                                dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage())
                                        ? "Node: " + nodeCode + " don't belong to countryCode " + countryCode
                                        : dto.getResultMessage() + "\n;Node :" + nodeCode + " don't belong to countryCode " + countryCode);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
        }
        if (dto.getResultCode() == 1) {
            return false;
        }
        return true;
    }

    @Override
    public ResultCreateDtByFileInput createDtByFileInput(String userService, String passService,
                                                         String countryCode, String systemCreateId,
                                                         List<DtObjDelete> listDtDelete,
                                                         List<FlowTemplateGNOCObj> flowTemplatesObj) {
        logger.info("Bat dau sinh dt: ");
        List<CatCountryBO> lstCountryBOS = null;
        ResultCreateDtByFileInput result = new ResultCreateDtByFileInput();
        result.setListDtCreate(new ArrayList<>());
        FlowTemplatesServiceImpl templatesService = new FlowTemplatesServiceImpl();
//        result.setResultCode(RESPONSE_SUCCESS);
//        result.setResultMessage("");
        try {
            if (!checkPass(userService, passService)) {
                result.setResultCode(1);
                result.setResultMessage("userService or passService is invalid");
                return result;
            }
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            return result;
        }


        // check input
        if (Util.isNullOrEmpty(countryCode)) {
            result.setResultCode(1);
            result.setResultMessage("countryCode is invalid");
            return result;
        } else {
            try {
                Map<String, Object> filter = new HashMap<>();
                filter.put("countryCode-EXAC", countryCode);
                CatCountryServiceImpl countryService = new CatCountryServiceImpl();
                lstCountryBOS = countryService.findList(filter);
                if (lstCountryBOS == null || lstCountryBOS.isEmpty()) {
                    result.setResultCode(1);
                    result.setResultMessage("countryCode is invalid");
                    return result;
                }
            } catch (Exception ex) {
                result.setResultCode(1);
                result.setResultMessage(ex.getMessage());
                logger.error(ex.getMessage(), ex);
                return result;
            }
        }

        MessageUtil.setLocal(new Locale("en", "US"));

        if (Util.isNullOrEmpty(systemCreateId)) {
            result.setResultCode(1);
            result.setResultMessage("Enter systemCreateId is a number");
            return result;
        } else {
            if (systemCreateId.length() > 200) {
                result.setResultCode(1);
                result.setResultMessage("systemCreateId entered in excess of 200 characters");
                return result;
            }

            if (!NumberUtils.isNumber(systemCreateId)) {
                result.setResultCode(1);
                result.setResultMessage("Enter systemCreateId is a number");
                return result;
            }
        }

        Date startTime = new Date();
        if (listDtDelete != null && !listDtDelete.isEmpty()) {
            Map<Long, Boolean> mapResult = new HashMap<>();
            for (DtObjDelete dto : listDtDelete) {
                if (!Util.isNullOrEmpty(dto.getDeleteDtId())) {
                    if (!NumberUtils.isNumber(dto.getDeleteDtId())) {
                        result.setResultCode(1);
                        result.setResultMessage("Enter DeleteDtId is a number");
                        return result;
                    }
                    DtObjDTO dtObjDTO = new DtObjDTO();
                    dtObjDTO.setDeleteDtId(dto.getDeleteDtId());
                    delFlowRunAction(systemCreateId, dtObjDTO, mapResult);
                }
            }

            if (mapResult.size() > 0) {
                String logAction = "CONTENT = Delete DT, Result: Success, List DT: DeleteDtId (";
                int i = 0;
                for (Map.Entry<Long, Boolean> entry : mapResult.entrySet()) {
                    i++;
                    if (entry.getValue())
                        logAction = logAction + entry.getKey() + (i == mapResult.size() ? "" : ", ");
                }
                logAction = logAction + ")";
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), "system",
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), TdttWebserviceImpl.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.CREATE,
                        logAction, LogUtils.getRequestSessionId());
            }
        }

        if (flowTemplatesObj == null || flowTemplatesObj.isEmpty()) {
            logger.info("flowTemplatesObj is required");
            result.setResultCode(1);
            result.setResultMessage("flowTemplatesObj is not empty");
            return result;
        } else {
            boolean checkNotOk = false;

            for (FlowTemplateGNOCObj templateId : flowTemplatesObj) {
                DtObjDTO dto = new DtObjDTO();
                dto.setTemplateId(templateId.getTemplateId());
                dto.setTempFileContent(templateId.getTempFileContent());
                result.setListDtCreate(new ArrayList<>());
                if (Util.isNullOrEmpty(templateId.getTemplateId())) {
                    dto.setResultCode(1);
                    dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage()) ? "templateid is not empty" : dto.getResultMessage() + "; templateid is not empty");
                    checkNotOk = true;
                } else {
                    if (!NumberUtils.isNumber(templateId.getTemplateId())) {
                        dto.setResultCode(1);
                        dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage()) ? "Enter templateid is a number" : dto.getResultMessage() + "; Enter templateid is a number");
                        checkNotOk = true;
                    }
                }
                if (Util.isNullOrEmpty(templateId.getTempFileContent())) {
                    dto.setResultCode(1);
                    dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage()) ? "tempFileContent is not empty" : dto.getResultMessage() + "; tempFileContent is not empty");
                    checkNotOk = true;
                }
                if (checkNotOk) {
                    result.setResultCode(1);
//                    result.setResultMessage("Enter flowTemplatesObj is invalid");
                    result.setResultMessage("Create DT failed");
                    result.getListDtCreate().add(dto);
                    return result;
                }
                try {

                    FlowTemplates templates = templatesService.findById(Long.parseLong(templateId.getTemplateId()));
                    if (templates == null) {
                        dto.setResultCode(1);
                        dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage()) ? "templateId is not exist" : dto.getResultMessage() + "; templateId is not exist");
                        checkNotOk = true;
                    } else {
                        if (templates.getStatus() != 9) {
                            dto.setResultCode(1);
                            dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage()) ? "templateId haven't approved" : dto.getResultMessage() + "; templateId haven't approved");
                            checkNotOk = true;
                        }
                    }
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                    dto.setResultCode(1);
                    dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage()) ? "templateId is not exist" : dto.getResultMessage() + "; templateId is not exist");
                    checkNotOk = true;
                }
                if (checkNotOk) {
                    result.getListDtCreate().add(dto);
                    result.setResultCode(1);
//                    result.setResultMessage("Enter flowTemplatesObj is invalid");
                    result.setResultMessage("Create DT failed");
                    return result;
                }
            }
        }

        for (FlowTemplateGNOCObj templateId : flowTemplatesObj) {

            logger.info("Bat dau sinh mop voi template: " + templateId);
            try {
                GenerateFlowRunController generateFlowRunController = new GenerateFlowRunController();
                Multimap<String, BasicDynaBean> multimapParam = ArrayListMultimap.create();
                String[] vs = null;
                logger.info("Bat dau lay template");
                FlowTemplates selectedFlowTemplate = new FlowTemplatesServiceImpl().findById(Long.parseLong(templateId.getTemplateId()));
                String CRName = "AUTO_" + systemCreateId + "_" + selectedFlowTemplate.getFlowTemplateName() + "_" + new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());

                if (result.getListDtCreate() == null) {
                    result.setListDtCreate(new ArrayList<>());
                }
                DtObjDTO dto = new DtObjDTO();
                dto.setTemplateId(templateId.getTemplateId());
                dto.setTempFileContent(templateId.getTempFileContent());
                logger.info("Bat dau lay danh sach Node mang va tham so");
                List<Node> nodes = new ArrayList<>();
                HashMap<String, Object> filters = new HashMap<>();

                logger.info("Bat dau lay du lieu tu fileconent");
                List<?> objectImports = new LinkedList<>();
                Workbook workbook = null;
                try {
                    //phan su dung de test
//                        File file = new File("C:\\Users\\vtn-ptpm-nv55\\Desktop\\New #'.folder\\Template_Import_DT_thuy+4.4.xlsx");
//                        byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
//                        String byteArrayString = new String(encoded, StandardCharsets.US_ASCII);
//                        templateId.setTempFileContent(byteArrayString);
                    //phan su dung de test

                    byte[] decode = Base64.decodeBase64(templateId.getTempFileContent());
                    InputStream inputstream = new ByteArrayInputStream(decode);

                    if (inputstream == null) {
                        throw new NullPointerException("  is not in the xls, xlsx format");
                    }
                    //Get the workbook instance for XLS/xlsx file
                    try {
                        workbook = WorkbookFactory.create(inputstream);

                    } catch (InvalidFormatException e2) {
                        logger.error(e2.getMessage(), e2);
                        throw new AppException("  is not in the xls, xlsx format");
                    } finally {

                    }

                    List<ObjectImportDt> params = new LinkedList<ObjectImportDt>();
                    List<String> sheetNames = new LinkedList<>();
                    generateFlowRunController.setNodes(new ArrayList<>());
//                        generateFlowRunController.setSubFlowRuns(new ArrayList<>());
                    generateFlowRunController.setSelectedFlowTemplates(selectedFlowTemplate);
                    generateFlowRunController.getContextVar(params, sheetNames);

                    if (!validTemplate(workbook, sheetNames, params, dto)) {
                        result.setResultCode(1);
                        result.setResultMessage("Create DT failed");
                        result.getListDtCreate().add(dto);
                        return result;
                    }
                    for (int i = 0; i < sheetNames.size(); i++) {
                        String sheetName = sheetNames.get(i);

                        Importer<Serializable> importer = new Importer<Serializable>() {

                            @Override
                            protected Map<Integer, String> getIndexMapFieldClass() {
                                return null;
                            }

                            @Override
                            protected String getDateFormat() {
                                return null;
                            }
                        };

                        Map<Integer, String> indexMapFieldClass = new HashMap<Integer, String>();
                        Integer key = 2;
                        indexMapFieldClass.put(1, MessageUtil.getResourceBundleMessage("key.nodeCode").toLowerCase());
                        for (String string : params.get(i).getParamNames()) {
                            indexMapFieldClass.put(key++, string);
                        }

                        importer.setIndexMapFieldClass(indexMapFieldClass);
                        Map<Integer, String> mapHeader = new HashMap<Integer, String>();
                        mapHeader.put(1, MessageUtil.getResourceBundleMessage("datatable.header.stt").toLowerCase());
                        importer.setMapHeader(mapHeader);
                        importer.setRowHeaderNumber(6);
                        importer.setIsReplaceSpace(false);
                        List<Serializable> objects = importer.getDatas(workbook, sheetName, "1-");
//                            workbook.close();
                        if (objects != null) {
                            if (!validateNodeCode(objects, dto, sheetName, countryCode)) {
                                dto.setResultCode(1);
                                result.setResultCode(1);
                                result.setResultMessage("Create DT failed");
                                result.getListDtCreate().add(dto);
                                return result;
                            }
                            ((List<Object>) objectImports).addAll(objects);
                        } else {
                            dto.setResultCode(1);
                            dto.setResultMessage("tempFileContent has no content");
                            result.setResultCode(1);
                            result.setResultMessage("Create DT failed");
                            result.getListDtCreate().add(dto);
                            return result;
                        }
                        generateFlowRunController.setObjectImports(objectImports);
                    }

                    if (generateFlowRunController.getObjectImports().size() == 0) {
                        dto.setResultCode(1);
                        dto.setResultMessage("tempFileContent has no content");
                        result.setResultCode(1);
                        result.setResultMessage("Create DT failed");
                        result.getListDtCreate().add(dto);
                        return result;
                    }

                    multimapParam = generateFlowRunController.joinParamsByNodeCode();
                    for (String nodeCode : multimapParam.keySet()) {
                        Node node = null;
                        filters.clear();
                        filters.put("nodeCode-EXAC", nodeCode);
                        logger.info("---bat dau lay danh sach node chay" + nodeCode + "---");
                        List<Node> nodesTemp = new NodeServiceImpl().findList(filters);
                        if (nodesTemp == null || nodesTemp.isEmpty()) {
                            dto.setResultCode(1);
                            dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage())
                                    ? "Node was not found:" + nodeCode
                                    : dto.getResultMessage() + "\n; Node was not found:" + nodeCode + "\n");
                            continue;
                        }
//                            Collection<BasicDynaBean> basicDynaBeans = multimapParam.get(node.getNodeCode());
                        node = nodesTemp.get(0);
                        logger.info("---ket thuc lay danh sach node chay" + nodeCode + "---");
                        if (node == null) {
                            logger.info("---khong tim kiem duoc node mang: " + nodeCode + "---");
                            dto.setResultCode(1);
                            dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage())
                                    ? "Node was not found:" + nodeCode
                                    : dto.getResultMessage() + "\n; Node was not found:" + nodeCode + "\n");
                        } else {
                            nodes.add(node);
                        }
                    }

                    if (nodes.size() == 0) {
                        dto.setResultCode(1);
                        dto.setResultMessage("No node found on tempFileContent");
                    }


                } catch (Exception e) {
                    logger.error(e.getMessage(), e);
                    dto.setResultCode(1);
                    dto.setResultMessage("tempFileContent: " + e.getMessage());
                } finally {
                    if (workbook != null)
                        try {
                            workbook.close();
                        } catch (IOException e) {
                            logger.error(e.getMessage(), e);
                        }
                }


                if (dto.getResultCode() == 1) {
                    result.setResultCode(1);
                    result.setResultMessage("Create DT failed");
                    result.getListDtCreate().add(dto);
                    return result;
                }
                logger.info("Ket thuc lay danh sach Node mang va tham so");

                FlowRunAction flowRunAction = new FlowRunAction();
                List<NodeRun> nodeRuns = new ArrayList<NodeRun>();
                flowRunAction.setCreateDate(new Date());
                flowRunAction.setFlowRunName(CRName);
                flowRunAction.setCrNumber(com.viettel.it.util.Config.CR_DEFAULT);
                flowRunAction.setCreateBy("System");
//                flowRunAction.setExecuteBy("System");
                flowRunAction.setFlowTemplates(selectedFlowTemplate);
                // anhnt2 - Luong tac dong
                flowRunAction.setExecuteType(selectedFlowTemplate.getTemplateType());
                flowRunAction.setTimeRun(new Date());
                flowRunAction.setStatus(0L);
                flowRunAction.setCountryCode(lstCountryBOS.get(0));
                flowRunAction.setSystemCreate("GNOC_SR");
                flowRunAction.setSystemCreateId(systemCreateId);
//                if (nodes.size() > 0) {
//                    flowRunAction.setCountryCode(nodes.get(0).getCountryCode());
//                }
                // namlh add hinh thuc 1 file sinh nhieu MOP
                if (selectedFlowTemplate.getIsGenerateDT().equals(1L)) {
                    int count = 0;
                    List<Node> nodeList = new ArrayList<>(nodes);
                    for (int i = 0; i < nodeList.size(); i++) {
                        dto = new DtObjDTO();
                        dto.setTemplateId(templateId.getTemplateId());
                        count++;
                        nodes = new ArrayList<>();
                        nodes.add(nodeList.get(i));
                        flowRunAction.setFlowRunId(null);
                        flowRunAction.setFlowRunName(CRName + "_NODE_" + nodeList.get(i).getNodeCode() + "_MOP" + count);
                        List<AccountGroupMop> lstAccGroupMop = new ArrayList<>();
                        logger.info("---Chay vao generateFlowRunController---");
                        generateFlowRunController.setFlowRunAction(flowRunAction);
                        generateFlowRunController.setSelectedFlowTemplates(selectedFlowTemplate);
                        generateFlowRunController.setNodes(new ArrayList<Node>());
                        // anhnt2 - Create with NCMS
//                flowRunAction.setSystemUpdateResult(Constant.systemUpdateResult.NCMS);
                        logger.info("---thoat khoi generateFlowRunController---");
                        Object[] objs = new FlowRunActionServiceImpl().openTransaction();
                        Session session = (Session) objs[0];
                        Transaction tx = (Transaction) objs[1];
                        try {
                            Map<Long, List<ActionOfFlow>> mapGroupAction = new HashMap<>();
                            logger.info("---get mapGroupAction---");
                            for (ActionOfFlow actionOfFlow : selectedFlowTemplate.getActionOfFlows()) {
                                if (mapGroupAction.get(actionOfFlow.getGroupActionOrder()) == null) {
                                    mapGroupAction.put(actionOfFlow.getGroupActionOrder(), new ArrayList<ActionOfFlow>());
                                }
                                mapGroupAction.get(actionOfFlow.getGroupActionOrder()).add(actionOfFlow);
                            }
                            logger.info("---get mapGroupAction thanh cong " + mapGroupAction.size() + "---");
                            new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction, session, tx, false);
                            logger.info("---save  flowRunAction" + flowRunAction + "---");
                            List<ParamValue> paramValues = new ArrayList<ParamValue>();

                            List<NodeRunGroupAction> nodeRunGroupActions = new ArrayList<NodeRunGroupAction>();
                            ArrayList<NodeDTO> nodeDTOS = new ArrayList<>();
                            Set<Long> lstCommandDetailId = new HashSet<>();
                            //lstCommandDetailId.size();
                            if (nodes != null && nodes.size() > 0) {
                                for (Node node : nodes) {
                                    String sql = "select distinct cd.command_detail_id from flow_templates a \n" +
                                            "join action_of_flow b on a.flow_templates_id = b.flow_templates_id\n" +
                                            "join ACTION_DB_SERVER c on c.action_id = b.action_id\n" +
                                            "join action_detail ad on ad.action_id = c.action_id\n" +
                                            "join action_command d on d.action_detail_id = ad.detail_id\n" +
                                            "join command_detail cd on d.command_detail_id = cd.command_detail_id\n" +
                                            "where a.flow_templates_id = ? and cd.vendor_id = ?\n" +
                                            "and cd.version_id = ? and cd.node_type_id = ?";

                                    List<?> commandDetail = new CommandDetailServiceImpl().findListSQLAll(sql, selectedFlowTemplate.getFlowTemplatesId(), node.getVendor().getVendorId(), node.getVersion().getVersionId(), node.getNodeType().getTypeId());
                                    if (commandDetail.size() == 0) {
                                        dto.setResultCode(1);
                                        dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage())
                                                ? "Could not found the command in the template that satisfies the node: " + node.getNodeCode()
                                                : dto.getResultMessage() + "\n; Could not found the command in the template that satisfies the node: " + node.getNodeCode());
                                        result.setResultCode(1);
                                        break;
                                    }
                                    for (Object o : commandDetail) {
                                        BigDecimal bdId = (BigDecimal) o;
                                        lstCommandDetailId.add(bdId.longValue());
                                    }

                                    NodeDTO nodeDTO = new NodeDTO();
                                    nodeDTO.setNodeCode(node.getNodeCode());
                                    nodeDTO.setNodeIp(node.getNodeIp());
                                    nodeDTOS.add(nodeDTO);
                                    dto.setListNode(nodeDTOS);
                                    logger.info("chay vao node :" + node.getNodeCode());
                                    NodeRun nodeRun = new NodeRun(new NodeRunId(node.getNodeId(), flowRunAction.getFlowRunId()), flowRunAction, node);
                                    //Quytv7_02102017_thay doi cach lay account/pass tu bang node
                                    nodeRuns.add(nodeRun);
                                    /**
                                     * Lay thong tin account tac dong
                                     */
                                    NodeAccount nodeAccount;
                                    if (node.getVendor().getVendorId().equals(com.viettel.it.util.Config.APP_TYPE.SERVER.value)) {
                                        nodeAccount = getAccImpactDefault(node, com.viettel.it.util.Config.APP_TYPE.SERVER.value, com.viettel.it.util.Config.ACCOUNT_IMPACT_MONITOR_TYPE.MONITOR.value);
                                    } else {
                                        nodeAccount = getAccImpactDefault(node, com.viettel.it.util.Config.APP_TYPE.DATABASE.value, com.viettel.it.util.Config.ACCOUNT_IMPACT_MONITOR_TYPE.IMPACT.value);
                                    }

                                    if (nodeAccount == null) {
                                        dto.setResultCode(1);
                                        dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage())
                                                ? "Cannot get node account from node: " + node.getNodeCode() + "/username: " + userService
                                                : dto.getResultMessage() + "\n; Cannot get node account from node: " + node.getNodeCode() + "/username: " + userService);
                                        result.setResultCode(1);
                                        result.setResultMessage("Create DT failed");
                                        result.getListDtCreate().add(dto);
                                        return result;
                                    }
                                    paramValues = generateFlowRunController.getParamInputs(node);
                                    Collection<BasicDynaBean> basicDynaBeans = multimapParam.get(node.getNodeCode());
                                    logger.info("---ket thuc lay paramValues" + paramValues.size() + "---");
                                    for (ParamValue paramValue : paramValues) {
                                        if (paramValue.getParamInput().getReadOnly()) {
                                            continue;
                                        }
                                        String value = "";
                                        try {
                                            for (BasicDynaBean basicDynaBean : basicDynaBeans) {
                                                Object object = null;
                                                try {
                                                    //20170817_hienhv4_fix import param to mop_start
                                                    object = basicDynaBean.getMap().get(Importer.normalizeParamCode(paramValue.getParamCode().replace(".", "")).toLowerCase());
                                                    //20170817_hienhv4_fix import param to mop_end
                                                } catch (Exception e) {
                                                    logger.debug(e.getMessage(), e);
                                                }
                                                if (object != null) {
                                                    value += object.toString() + com.viettel.it.util.Config.SPLITTER_VALUE;
                                                }
                                            }
                                            value = value.replaceAll(com.viettel.it.util.Config.SPLITTER_VALUE + "$", "");

                                        } catch (Exception e) {
                                            throw e;
                                        }
                                        if (!value.isEmpty()) {
                                            // 20181002_thenv_voi lenh FTP thi chi nhan value dau tien_start
                                            if ("FTP".equalsIgnoreCase(paramValue.getParamInput().getCommandDetail().getProtocol())) {
                                                vs = value.split(";");
                                                paramValue.setParamValue(vs.length > 0 ? vs[0] : "");
                                            } else {
                                                paramValue.setParamValue(value);//.substring(0, Math.min(3950, value.length())));
                                            }
                                            // 20181002_thenv_voi lenh FTP thi chi nhan value dau tien_end
                                        }
                                        paramValue.setNodeRun(nodeRun);
                                        paramValue.setCreateTime(new Date());
                                        paramValue.setParamValueId(null);

                                        if (paramValue.getParamValue() == null || paramValue.getParamValue().isEmpty()) {
//                                    dto.setResultCode(1);
//                                    dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage())
//                                            ? "Param impact was not found: " + paramValue.getParamCode()
//                                            : dto.getResultMessage() + "\n; Param impact was not found: " + paramValue.getParamCode());
                                        }
                                    }
                                    if (dto.getResultCode() == 1) {
                                        result.setResultCode(1);
                                        break;
                                    }

                                    logger.info(" co vao mapGroupAction size = " + mapGroupAction.size());
                                    for (Map.Entry<Long, List<ActionOfFlow>> entry : mapGroupAction.entrySet()) {
                                        NodeRunGroupAction nodeRunGroupAction = new NodeRunGroupAction(
                                                new NodeRunGroupActionId(node.getNodeId(),
                                                        flowRunAction.getFlowRunId(),
                                                        entry.getValue().get(0).getStepNum()), entry.getValue().get(0), nodeRun);
                                        nodeRunGroupActions.add(nodeRunGroupAction);
                                        AccountGroupMop accGroup = new AccountGroupMop();
                                        accGroup.setNodeAccountId(nodeAccount.getId());
                                        accGroup.setNodeId(node.getNodeId());
                                        accGroup.setFlowRunId(flowRunAction.getFlowRunId());
                                        accGroup.setActionOfFlowId(entry.getValue().get(0).getStepNum());

                                        lstAccGroupMop.add(accGroup);

                                    } // end loop for group action
                                    logger.info(" thoai khoi mapGroupAction");
                                }
                                if (dto.getResultCode() == 1) {
                                    result.setResultCode(1);
                                    result.setResultMessage("Create DT failed");
                                    result.getListDtCreate().add(dto);
                                    return result;
//                            continue;
                                }
                                logger.info(" xoa session");
                                logger.info(" insert NodeRunServiceImpl ");
                                new NodeRunServiceImpl().saveOrUpdate(nodeRuns, session, tx, false);
                                logger.info(" insert ParamValueServiceImpl ");
                                new ParamValueServiceImpl().saveOrUpdate(paramValues, session, tx, false);
                                //20190408_chuongtq start check param when create MOP
                                if (new GenerateFlowRunController().checkConfigCondition(AamConstants.CFG_CHK_PARAM_CONDITION_FOR_AAM)) {
                                    LinkedHashMap<String, CheckParamCondition> mapCheckParamCondition = new LinkedHashMap<>();
                                    if (!(new CheckParamCondition().checkParamCondition(selectedFlowTemplate.getParamConditions(), nodes, generateFlowRunController.getMapParamValue(), mapCheckParamCondition, false))) {
                                        if (tx.getStatus() != TransactionStatus.ROLLED_BACK && tx.getStatus() != TransactionStatus.COMMITTED) {
                                            tx.rollback();
                                        }
                                        result.setResultCode(1);
                                        List<CheckParamCondition> lstResult = new ArrayList<CheckParamCondition>(mapCheckParamCondition.values());
                                        String json = (new Gson()).toJson(lstResult);
                                        result.setResultMessage("Create DT fail, json result: [" + StringEscapeUtils.unescapeJava(json) + "]");
                                        return result;
                                    }
                                }
                                //20190408_chuongtq end check param when create MOP
                                session.flush();
                                session.clear();
                                new NodeRunGroupActionServiceImpl().saveOrUpdate(nodeRunGroupActions, session, tx, true);
                            }
                            // anhnt02
                            flowRunAction.setFlowRunCode(Constant.PREFIX_MOP_INFRA + flowRunAction.getFlowRunId());
                            boolean isCmdReboot = checkCmdReboot(lstCommandDetailId);
                            if (isCmdReboot) {
                                com.viettel.model.Action action = ActionUtil.createMobDb(nodes, flowRunAction);
                                if (action != null && action.getId() != null) {
                                    flowRunAction.setServiceActionId(action.getId());
                                }
                            }
                            new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction);
                            logger.info(" insert account group mop ");
                            new AccountGroupMopServiceImpl().saveOrUpdate(lstAccGroupMop);
                            try {
                                //Save File to database
                                String file2 = getTemplateMultiExport(MessageUtil.getResourceBundleMessage("key.template.export.dt"));
                                logger.info(" luu file thanh cong ");
                                File fileTemp2 = new File("tmp" + new Date().getTime() + ".xlsx");
                                ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                                generateFlowRunController.exportToFile(file2, fileTemp2, outStream);
                                flowRunAction.setFileContent(outStream.toByteArray());
                                IOUtils.closeQuietly(outStream);
                                new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction);
                            } catch (Exception ex) {
                                logger.error(ex.getMessage(), ex);
                            }
                            dto.setResultCode(0);
                            dto.setResultMessage("Create DT successful");
                            dto.setDtId(flowRunAction.getFlowRunId().toString());
                            dto.setDtName(flowRunAction.getFlowRunName());
                            dto.setTempFileContent(null);
//                    dto.setFileContentResult(org.apache.commons.codec.binary.Base64.encodeBase64String(flowRunAction.getFileContent()));
                            result.getListDtCreate().add(dto);
                        } catch (Exception e) {
                            if (tx.getStatus() != TransactionStatus.ROLLED_BACK && tx.getStatus() != TransactionStatus.COMMITTED) {
                                tx.rollback();
                            }
                            logger.error(e.getMessage(), e);
                            dto.setResultCode(1);
                            dto.setResultMessage(e.getMessage());
                            result.setResultCode(1);
                            result.setResultMessage(result.getResultMessage() + MessageUtil.getResourceBundleMessage("ws.error.get.create.dt.fail") + ";\n");
                            result.getListDtCreate().add(dto);
                            return result;
                        } finally {
                            if (session.isOpen()) {
                                session.close();
                            }
                        }
                    }
                } else {
                    List<AccountGroupMop> lstAccGroupMop = new ArrayList<>();
                    logger.info("---Chay vao generateFlowRunController---");
                    generateFlowRunController.setFlowRunAction(flowRunAction);
                    generateFlowRunController.setSelectedFlowTemplates(selectedFlowTemplate);
                    generateFlowRunController.setNodes(new ArrayList<Node>());
                    // anhnt2 - Create with NCMS
//                flowRunAction.setSystemUpdateResult(Constant.systemUpdateResult.NCMS);
                    logger.info("---thoat khoi generateFlowRunController---");
                    Object[] objs = new FlowRunActionServiceImpl().openTransaction();
                    Session session = (Session) objs[0];
                    Transaction tx = (Transaction) objs[1];
                    try {
                        Map<Long, List<ActionOfFlow>> mapGroupAction = new HashMap<>();
                        logger.info("---get mapGroupAction---");
                        for (ActionOfFlow actionOfFlow : selectedFlowTemplate.getActionOfFlows()) {
                            if (mapGroupAction.get(actionOfFlow.getGroupActionOrder()) == null) {
                                mapGroupAction.put(actionOfFlow.getGroupActionOrder(), new ArrayList<ActionOfFlow>());
                            }
                            mapGroupAction.get(actionOfFlow.getGroupActionOrder()).add(actionOfFlow);
                        }
                        logger.info("---get mapGroupAction thanh cong " + mapGroupAction.size() + "---");
                        new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction, session, tx, false);
                        logger.info("---save  flowRunAction" + flowRunAction + "---");
                        List<ParamValue> paramValues = new ArrayList<ParamValue>();

                        List<NodeRunGroupAction> nodeRunGroupActions = new ArrayList<NodeRunGroupAction>();
                        ArrayList<NodeDTO> nodeDTOS = new ArrayList<>();
                        Set<Long> lstCommandDetailId = new HashSet<>();
                        if (nodes != null && nodes.size() > 0) {
                            for (Node node : nodes) {
                                String sql = "select distinct cd.command_detail_id from flow_templates a \n" +
                                        "join action_of_flow b on a.flow_templates_id = b.flow_templates_id\n" +
                                        "join ACTION_DB_SERVER c on c.action_id = b.action_id\n" +
                                        "join action_detail ad on ad.action_id = c.action_id\n" +
                                        "join action_command d on d.action_detail_id = ad.detail_id\n" +
                                        "join command_detail cd on d.command_detail_id = cd.command_detail_id\n" +
                                        "where a.flow_templates_id = ? and cd.vendor_id = ?\n" +
                                        "and cd.version_id = ? and cd.node_type_id = ?";

                                List<?> commandDetail = new CommandDetailServiceImpl().findListSQLAll(sql, selectedFlowTemplate.getFlowTemplatesId(), node.getVendor().getVendorId(), node.getVersion().getVersionId(), node.getNodeType().getTypeId());
                                if (commandDetail.size() == 0) {
                                    dto.setResultCode(1);
                                    dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage())
                                            ? "Could not found the command in the template that satisfies the node: " + node.getNodeCode()
                                            : dto.getResultMessage() + "\n; Could not found the command in the template that satisfies the node: " + node.getNodeCode());
                                    result.setResultCode(1);
                                    break;
                                }
                                for (Object o : commandDetail) {
                                    BigDecimal bdId = (BigDecimal) o;
                                    lstCommandDetailId.add(bdId.longValue());
                                }

                                NodeDTO nodeDTO = new NodeDTO();
                                nodeDTO.setNodeCode(node.getNodeCode());
                                nodeDTO.setNodeIp(node.getNodeIp());
                                nodeDTOS.add(nodeDTO);
                                dto.setListNode(nodeDTOS);
                                logger.info("chay vao node :" + node.getNodeCode());
                                NodeRun nodeRun = new NodeRun(new NodeRunId(node.getNodeId(), flowRunAction.getFlowRunId()), flowRunAction, node);
                                //Quytv7_02102017_thay doi cach lay account/pass tu bang node
                                nodeRuns.add(nodeRun);
                                /**
                                 * Lay thong tin account tac dong
                                 */
                                NodeAccount nodeAccount;
                                if (node.getVendor().getVendorId().equals(com.viettel.it.util.Config.APP_TYPE.SERVER.value)) {
                                    nodeAccount = getAccImpactDefault(node, com.viettel.it.util.Config.APP_TYPE.SERVER.value, com.viettel.it.util.Config.ACCOUNT_IMPACT_MONITOR_TYPE.MONITOR.value);
                                } else {
                                    nodeAccount = getAccImpactDefault(node, com.viettel.it.util.Config.APP_TYPE.DATABASE.value, com.viettel.it.util.Config.ACCOUNT_IMPACT_MONITOR_TYPE.IMPACT.value);
                                }

                                if (nodeAccount == null) {
                                    dto.setResultCode(1);
                                    dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage())
                                            ? "Cannot get node account from node: " + node.getNodeCode() + "/username: " + userService
                                            : dto.getResultMessage() + "\n; Cannot get node account from node: " + node.getNodeCode() + "/username: " + userService);
                                    result.setResultCode(1);
                                    result.setResultMessage("Create DT failed");
                                    result.getListDtCreate().add(dto);
                                    return result;
                                }
                                paramValues = generateFlowRunController.getParamInputs(node);
                                Collection<BasicDynaBean> basicDynaBeans = multimapParam.get(node.getNodeCode());
                                logger.info("---ket thuc lay paramValues" + paramValues.size() + "---");
                                for (ParamValue paramValue : paramValues) {
                                    if (paramValue.getParamInput().getReadOnly()) {
                                        continue;
                                    }
                                    String value = "";
                                    try {
                                        for (BasicDynaBean basicDynaBean : basicDynaBeans) {
                                            Object object = null;
                                            try {
                                                //20170817_hienhv4_fix import param to mop_start
                                                object = basicDynaBean.getMap().get(Importer.normalizeParamCode(paramValue.getParamCode().replace(".", "")).toLowerCase());
                                                //20170817_hienhv4_fix import param to mop_end
                                            } catch (Exception e) {
                                                logger.debug(e.getMessage(), e);
                                            }
                                            if (object != null) {
                                                value += object.toString() + com.viettel.it.util.Config.SPLITTER_VALUE;
                                            }
                                        }
                                        value = value.replaceAll(com.viettel.it.util.Config.SPLITTER_VALUE + "$", "");

                                    } catch (Exception e) {
                                        throw e;
                                    }
                                    if (!value.isEmpty()) {
                                        // 20181002_thenv_voi lenh FTP thi chi nhan value dau tien_start
                                        if ("FTP".equalsIgnoreCase(paramValue.getParamInput().getCommandDetail().getProtocol())) {
                                            vs = value.split(";");
                                            paramValue.setParamValue(vs.length > 0 ? vs[0] : "");
                                        } else {
                                            paramValue.setParamValue(value);//.substring(0, Math.min(3950, value.length())));
                                        }
                                        // 20181002_thenv_voi lenh FTP thi chi nhan value dau tien_end
                                    }
                                    paramValue.setNodeRun(nodeRun);
                                    paramValue.setCreateTime(new Date());
                                    paramValue.setParamValueId(null);

                                    if (paramValue.getParamValue() == null || paramValue.getParamValue().isEmpty()) {
//                                    dto.setResultCode(1);
//                                    dto.setResultMessage(Util.isNullOrEmpty(dto.getResultMessage())
//                                            ? "Param impact was not found: " + paramValue.getParamCode()
//                                            : dto.getResultMessage() + "\n; Param impact was not found: " + paramValue.getParamCode());
                                    }
                                }
                                if (dto.getResultCode() == 1) {
                                    result.setResultCode(1);
                                    break;
                                }
                                new ParamValueServiceImpl().saveOrUpdate(paramValues, session, tx, false);
                                logger.info(" co vao mapGroupAction size = " + mapGroupAction.size());
                                for (Map.Entry<Long, List<ActionOfFlow>> entry : mapGroupAction.entrySet()) {
                                    NodeRunGroupAction nodeRunGroupAction = new NodeRunGroupAction(
                                            new NodeRunGroupActionId(node.getNodeId(),
                                                    flowRunAction.getFlowRunId(),
                                                    entry.getValue().get(0).getStepNum()), entry.getValue().get(0), nodeRun);
                                    nodeRunGroupActions.add(nodeRunGroupAction);
                                    AccountGroupMop accGroup = new AccountGroupMop();
                                    accGroup.setNodeAccountId(nodeAccount.getId());
                                    accGroup.setNodeId(node.getNodeId());
                                    accGroup.setFlowRunId(flowRunAction.getFlowRunId());
                                    accGroup.setActionOfFlowId(entry.getValue().get(0).getStepNum());

                                    lstAccGroupMop.add(accGroup);

                                } // end loop for group action
                                logger.info(" thoai khoi mapGroupAction");
                            }
                            if (dto.getResultCode() == 1) {
                                result.setResultCode(1);
                                result.setResultMessage("Create DT failed");
                                result.getListDtCreate().add(dto);
                                return result;
//                            continue;
                            }
                            logger.info(" xoa session");
                            logger.info(" insert NodeRunServiceImpl ");
                            new NodeRunServiceImpl().saveOrUpdate(nodeRuns, session, tx, false);
                            logger.info(" insert ParamValueServiceImpl ");

                            //20190408_chuongtq start check param when create MOP
                            if (new GenerateFlowRunController().checkConfigCondition(AamConstants.CFG_CHK_PARAM_CONDITION_FOR_AAM)) {
                                LinkedHashMap<String, CheckParamCondition> mapCheckParamCondition = new LinkedHashMap<>();
                                if (!(new CheckParamCondition().checkParamCondition(selectedFlowTemplate.getParamConditions(), nodes, generateFlowRunController.getMapParamValue(), mapCheckParamCondition, false))) {
                                    if (tx.getStatus() != TransactionStatus.ROLLED_BACK && tx.getStatus() != TransactionStatus.COMMITTED) {
                                        tx.rollback();
                                    }
                                    result.setResultCode(1);
                                    List<CheckParamCondition> lstResult = new ArrayList<CheckParamCondition>(mapCheckParamCondition.values());
                                    String json = (new Gson()).toJson(lstResult);
                                    result.setResultMessage("Create DT fail, json result: [" + StringEscapeUtils.unescapeJava(json) + "]");
                                    return result;
                                }
                            }
                            //20190408_chuongtq end check param when create MOP
                            session.flush();
                            session.clear();
                            new NodeRunGroupActionServiceImpl().saveOrUpdate(nodeRunGroupActions, session, tx, true);
                        }
                        // anhnt02
                        flowRunAction.setFlowRunCode(Constant.PREFIX_MOP_INFRA + flowRunAction.getFlowRunId());
                        boolean isCmdReboot = checkCmdReboot(lstCommandDetailId);
                        if (isCmdReboot) {
                            com.viettel.model.Action action = ActionUtil.createMobDb(nodes, flowRunAction);
                            if (action != null && action.getId() != null) {
                                flowRunAction.setServiceActionId(action.getId());
                            }
                        }
                        new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction);
                        logger.info(" insert account group mop ");
                        new AccountGroupMopServiceImpl().saveOrUpdate(lstAccGroupMop);
                        try {
                            //Save File to database
                            String file2 = getTemplateMultiExport(MessageUtil.getResourceBundleMessage("key.template.export.dt"));
                            logger.info(" luu file thanh cong ");
                            File fileTemp2 = new File("tmp" + new Date().getTime() + ".xlsx");
                            ByteArrayOutputStream outStream = new ByteArrayOutputStream();
                            generateFlowRunController.exportToFile(file2, fileTemp2, outStream);
                            flowRunAction.setFileContent(outStream.toByteArray());
                            IOUtils.closeQuietly(outStream);
                            new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction);
                        } catch (Exception ex) {
                            logger.error(ex.getMessage(), ex);
                        }
                        dto.setResultCode(0);
                        dto.setResultMessage("Create DT successful");
                        dto.setDtId(flowRunAction.getFlowRunId().toString());
                        dto.setDtName(flowRunAction.getFlowRunName());
                        dto.setTempFileContent(null);
//                    dto.setFileContentResult(org.apache.commons.codec.binary.Base64.encodeBase64String(flowRunAction.getFileContent()));
                        result.getListDtCreate().add(dto);
                    } catch (Exception e) {
                        if (tx.getStatus() != TransactionStatus.ROLLED_BACK && tx.getStatus() != TransactionStatus.COMMITTED) {
                            tx.rollback();
                        }
                        logger.error(e.getMessage(), e);
                        dto.setResultCode(1);
                        dto.setResultMessage(e.getMessage());
                        result.setResultCode(1);
                        result.setResultMessage(result.getResultMessage() + MessageUtil.getResourceBundleMessage("ws.error.get.create.dt.fail") + ";\n");
                        result.getListDtCreate().add(dto);
                        return result;
                    } finally {
                        if (session.isOpen()) {
                            session.close();
                        }
                    }
                }


            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                result.setResultCode(1);
                result.setResultMessage(result.getResultMessage() + MessageUtil.getResourceBundleMessage("ws.error.get.create.dt.fail") + ";\n");
                return result;
            }
            logger.info("Tao mop thanh cong, tra ve ket qua");
            result.setResultMessage("Create DT successful");
            logger.info("Create DT successful for template: " + templateId.getTemplateId());
        }
        return result;
    }

    private boolean checkCmdReboot(Set<Long> lstCommandDetailId) {
        try {
            for (Long commandDetailId : lstCommandDetailId) {
                CommandDetail objCommandDetail = new CommandDetailServiceImpl().findById(commandDetailId);
                if (objCommandDetail.getCommandTelnetParser().getCmd().toUpperCase().contains("reboot".toUpperCase())) {
                    return true;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return false;
    }

    private NodeAccount getAccImpactDefault(Node node, Long accountType, Long impactOrMonitor) {
        NodeAccount acc = null;
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("impactOrMonitor", impactOrMonitor);
            filters.put("accountType", accountType);
            filters.put("serverId", node.getServerId());
            filters.put("active", Constant.status.active);

            List<NodeAccount> nodeAccounts = new NodeAccountServiceImpl().findList(filters, null);
            if (nodeAccounts != null) {
                for (NodeAccount nodeAccount : nodeAccounts) {
                    if (nodeAccount.getItBusinessNode() == null || nodeAccount.getItBusinessNode() == 0L) {
                        acc = nodeAccount;
                        break;
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            acc = null;
        }
        return acc;
    }
    //20190524_tudn_end tac dong toan trinh SR GNOC

    //20190722_tudn_start lay thong tin MOP
    public MopResult getMopInfo(String wsUser, String wsPass, String mopCode) {
        logger.info("getMopInfo " + mopCode);
        MopResult mopResult = new MopResult();
        mopResult.setStatus(0);
        if (!checkPass(wsUser, wsPass)) {
            mopResult.setMessage("Web service authentication is incorrect");
            return mopResult;
        }

        if (isNullOrEmpty(mopCode)) {
            mopResult.setMessage("You must enter mopCode");
            return mopResult;
        }

        List<MopInfo> mopInfos = new ArrayList<>();

        if (!mopCode.trim().startsWith(Constant.PREFIX_MOP_INFRA)) {
            ActionService actionService = new ActionServiceImpl();
            IimService iimService = new IimServiceImpl();
            try {
                Action action = actionService.findActionByCode(mopCode);
                if (action == null) {
                    mopResult.setStatus(0);
                    mopResult.setMessage("mopCode not found: " + mopCode);
                } else {
                    try {
                        MopInfo mopInfo = new MopInfo();
                        mopInfo.setCode(action.getTdCode());
                        mopInfo.setName(action.getCrName());
                        mopInfo.setNationCode(action.getImpactProcess() == null ? "VNM" : action.getImpactProcess().getNationCode());
                        mopInfo.setIps(new DocxUtil(action).getListImpactIP(action.getId()));
                        mopInfo.setCreatedDate(action.getCreatedTime());

                        List<Long> appIds = actionService.findListAppIds(action.getId());
                        logger.info("list appIds: " + (appIds == null ? 0 : appIds.size()));
                        logger.info(action.getCrNumber() + "\t" + appIds);
                        if (appIds != null && appIds.size() > 0) {
                            List<Service> services = iimService.findServicesByModules(action.getImpactProcess().getNationCode(), appIds);
                            List<String> appGroups = new ArrayList<>();
                            for (Service service : services) {
                                appGroups.add(service.getServiceCode());
                            }
                            mopInfo.setAffectServices(appGroups);
                            List<String> effectIps = iimService.findAllIpByServices(action.getImpactProcess().getNationCode(), appGroups);
                            mopInfo.setAffectIps(effectIps);

                            //Quytv7_20180911_Get list module start

                            List<Module> moduleList = iimService.findModulesByIds(action.getImpactProcess().getNationCode(), appIds);
                            logger.info("list moduleList: " + (moduleList == null ? 0 : moduleList.size()));
                            List<ModuleInfo> moduleInfos = new ArrayList<>();
                            ModuleInfo moduleInfo;
                            for (Module module : moduleList) {
                                moduleInfo = new ModuleInfo();
                                moduleInfo.setModuleCode(module.getModuleCode());
                                moduleInfo.setModuleName(module.getModuleName());
                                moduleInfo.setServiceCode(module.getServiceCode());
                                moduleInfos.add(moduleInfo);
                            }
                            mopInfo.setModuleInfos(moduleInfos);
                            //Quytv7_20180911_Get list module end
                        }

                        if (action.getKbType() != null && (action.getKbType().equals(AamConstants.KB_TYPE.BD_SERVER) || action.getKbType().equals(AamConstants.KB_TYPE.UC_SERVER))) {
                            HashMap<String, Object> filters = new HashMap<>();
                            filters.put("actionId", action.getId());
                            ActionServerService actionServerService = new ActionServerServiceImpl();
                            List<ActionServer> actionServers = actionServerService.findList(filters, new HashMap<>());
                            if (mopInfo.getAffectIps() == null) {
                                mopInfo.setAffectIps(new ArrayList<>());
                            }
                            if (mopInfo.getIps() == null) {
                                mopInfo.setIps(new ArrayList<>());
                            }
                            if (actionServers != null && actionServers.size() > 0) {
                                for (ActionServer actionServer : actionServers) {
                                    if (actionServer.getIpServer() != null) {
                                        logger.info("add ip from actionserver: " + actionServer.getIpServer());
                                        mopInfo.getAffectIps().add(actionServer.getIpServer());
                                        mopInfo.getIps().add(actionServer.getIpServer());
                                    }
                                }
                            }
                        }
                        if (mopInfo.getAffectIps() == null || mopInfo.getAffectIps().size() == 0 || mopInfo.getIps() == null || mopInfo.getIps().size() == 0) {
                            logger.info("Ip null, khong add gui sang noc: " + action.getCrId());
                        }

                        //bo sung them
                        List<Integer> kbGroups = actionService.findKbGroups(action.getId());

                        File mopDir = new File(UploadFileUtils.getMopFolder(action));
                        DocxUtil.export(action, "");

                        File mopFile = null;
                        File mopRollbackFile = null;
                        File[] files = mopDir.listFiles();
                        if (files != null) {
                            for (File file : files) {
                                if (file.getPath().endsWith(kbGroups.size() < 2 ? "tacdong_" + kbGroups.get(0) + ".docx" : "tacdong_.zip"))
                                    mopFile = file;

                                if (file.getPath().endsWith(kbGroups.size() < 2 ? "rollback_" + kbGroups.get(0) + ".docx" : "rollback_.zip"))
                                    mopRollbackFile = file;
                            }
                        }

                        if (mopFile != null && mopRollbackFile != null) {
                            mopInfo.setMopFile(mopFile.getName());
                            mopInfo.setMopRollbackFile(mopRollbackFile.getName());
                            mopInfo.setMopFileContent(Base64.encodeBase64String(FileUtils.readFileToByteArray(mopFile)));
                            mopInfo.setMopRollbackFileContent(Base64.encodeBase64String(FileUtils.readFileToByteArray(mopRollbackFile)));
                        }
                        mopInfos.add(mopInfo);

                    } catch (Exception ex) {
                        logger.error(ex.getMessage(), ex);
                        mopResult.setStatus(0);
                        mopResult.setMessage(ex.getMessage());
                    }
                    mopResult.setStatus(1);
                }

            } catch (AppException e) {
                mopResult.setStatus(0);
                mopResult.setMessage(e.getMessage());
            }

        } else {

            try {
                Long mopId = Long.valueOf(mopCode.trim().substring(Constant.PREFIX_MOP_INFRA.length()));
                FlowRunAction flowAction = (new FlowRunActionServiceImpl()).findById(mopId);
                if (flowAction != null) {

                    MopInfo mopInfo;
                    try {
                        mopInfo = new MopInfo();
                        mopInfo.setNationCode(flowAction.getCountryCode() == null ? "VNM" : flowAction.getCountryCode().getCountryCode());
                        mopInfo.setName(flowAction.getFlowRunName());
                        mopInfo.setCreatedDate(new Date());
                        mopInfo.setCode(Constant.PREFIX_MOP_INFRA + flowAction.getFlowRunId());

                        List<NodeRun> lstNodeRun = flowAction.getNodeRuns();
                        if (mopInfo.getAffectIps() == null) {
                            mopInfo.setAffectIps(new ArrayList<String>());
                        }
                        if (mopInfo.getAffectServices() == null) {
                            mopInfo.setAffectServices(new ArrayList<String>());
                        }
                        if (mopInfo.getIps() == null) {
                            mopInfo.setIps(new ArrayList<String>());
                        }
                        if (lstNodeRun != null && !lstNodeRun.isEmpty()) {
                            for (NodeRun nodeRun : lstNodeRun) {
                                mopInfo.getAffectIps().add(nodeRun.getNode().getEffectIp());
                                mopInfo.getAffectServices().add(nodeRun.getNode().getNodeCode());
                                mopInfo.getIps().add(nodeRun.getNode().getEffectIp());
                            }
                        }
                        //bo sung sau
                        mopInfo.setMopFileContent(Base64.encodeBase64String(flowAction.getFileContent()));
                        mopInfo.setMopFile(flowAction.getFlowRunId()
                                + "_" + ZipUtils.clearHornUnicode(flowAction.getFlowRunName()) + ".xlsx");
                        mopInfo.setMopRollbackFile(flowAction.getFlowRunId()
                                + "_rollback_" + ZipUtils.clearHornUnicode(flowAction.getFlowRunName()) + ".xlsx");
                        mopInfo.setMopRollbackFileContent(Base64.encodeBase64String(flowAction.getFileContent()));
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                        mopInfo = null;
                    }

                    if (mopInfo != null) {
                        mopInfos.add(mopInfo);
                        mopResult.setStatus(1);
                    }
                } else {
                    mopResult.setStatus(0);
                    mopResult.setMessage("mopCode not found: " + mopCode);
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                mopResult.setStatus(0);
                mopResult.setMessage(e.getMessage());
            }

        }
        mopResult.setMopInfos(mopInfos);
        return mopResult;
    }
    //20190722_tudn_end lay thong tin MOP

    public static void main(String[] args) {
        try {
            File file = new File("C:\\Users\\VTN-PTPM-NV68\\Downloads\\Template_Import_DT_Temp.VNM.10BO.MSS.Check+cell+2G.nguyennt10+(clone) (6).xlsx");
            byte[] encoded = Base64.encodeBase64(FileUtils.readFileToByteArray(file));
            String byteArrayString = new String(encoded, StandardCharsets.US_ASCII);
            System.out.println(byteArrayString);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
