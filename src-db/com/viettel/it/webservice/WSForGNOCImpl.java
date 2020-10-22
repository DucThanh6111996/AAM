/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice;

import com.google.common.collect.ArrayListMultimap;
import com.google.common.collect.Multimap;
import com.google.gson.Gson;
import com.viettel.exception.AppException;
import com.viettel.it.controller.GenerateFlowRunController;
import com.viettel.it.model.*;
import com.viettel.it.object.CheckParamCondition;
import com.viettel.it.object.ObjectImportDt;
import com.viettel.it.persistence.*;
import com.viettel.it.persistence.common.CatCountryServiceImpl;
import com.viettel.it.util.*;
import com.viettel.it.util.Config;
import com.viettel.it.util.LogUtils;
import com.viettel.it.webservice.object.*;

import java.io.*;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.text.SimpleDateFormat;
import java.util.*;

import javax.annotation.Resource;
import javax.faces.context.FacesContext;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;

import com.viettel.passprotector.PassProtector;
import com.viettel.util.*;
import com.viettel.util.Util;
import org.apache.commons.beanutils.BasicDynaBean;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringEscapeUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author hienhv4
 */
@WebService(endpointInterface = "com.viettel.it.webservice.WSForGNOC")
public class WSForGNOCImpl implements WSForGNOC {

    protected final Logger logger = LoggerFactory.getLogger(WSForGNOCImpl.class);
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
    public ResultDTO updateCrCode(String userService, String passService, String mopId, String crCode) {
        ResultDTO result = new ResultDTO();
        Long mopIdLocal;
        logger.info("updateCrCode mopId: " + mopId + ", crCode: " + crCode);
        Date startTime = new Date();

        try {
            mopIdLocal = Long.parseLong(mopId);
        } catch (Exception ex) {
            result.setResultCode(3);
            result.setResultMessage("mopId phải là s");
            logger.error(ex.getMessage(), ex);
            return result;
        }

        try {
            FlowRunActionServiceImpl service = new FlowRunActionServiceImpl();
            FlowRunAction flowRun = service.findById(mopIdLocal);

            if (flowRun == null) {
                result.setResultCode(4);
                result.setResultMessage("MOP khÃ´ng tá»“n táº¡i");
                return result;
            }

            flowRun.setCrNumber(crCode);
            service.saveOrUpdate(flowRun);
            //20180620_tudn_start ghi log DB
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), getRemoteIp(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WSForGNOCImpl.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.CREATE,
                        flowRun.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            //20180620_tudn_end ghi log DB

            result.setResultCode(0);
            result.setResultMessage("SUCCESS");
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage(ex.getMessage());
            logger.error(ex.getMessage(), ex);
        }
        return result;
    }

    @Override
    public ResultDTO updateCrStatus(String userService, String passService, String crCode, String status) {
        ResultDTO result = new ResultDTO();
        Long statusLocal;
        logger.info("updateCrStatus status: " + status + ", crCode: " + crCode);
        Date startTime = new Date();

        try {
            statusLocal = Long.parseLong(status);
        } catch (Exception ex) {
            result.setResultCode(3);
            result.setResultMessage("status pháº£i lÃ  sá»‘ nguyÃªn");
            logger.error(ex.getMessage(), ex);
            return result;
        }

        try {
            FlowRunActionServiceImpl service = new FlowRunActionServiceImpl();

            Map<String, Object> filters = new HashMap<>();
            filters.put("crNumber-" + GenericDaoImplNewV2.EXAC, crCode);

            List<FlowRunAction> flowRuns = service.findList(filters);

            if (flowRuns != null && !flowRuns.isEmpty()) {
                for (FlowRunAction flow : flowRuns) {
                    if (statusLocal == 6) {
                        flow.setStatus(1l);
                    }
                    flow.setCrStatus(statusLocal);
                }
            }

            service.saveOrUpdate(flowRuns);
            //20180620_tudn_start ghi log DB
            try {
                LogUtils.logAction(LogUtils.appCode, startTime, new Date(), getRemoteIp(),
                        LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WSForGNOCImpl.class.getName(),
                        Thread.currentThread().getStackTrace()[1].getMethodName(),
                        LogUtils.ActionType.CREATE,
                        flowRuns.toString(), LogUtils.getRequestSessionId());
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
            }
            //20180620_tudn_end ghi log DB

            result.setResultCode(0);
            result.setResultMessage("SUCCESS");
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage(ex.getMessage());
            logger.error(ex.getMessage(), ex);
        }
        return result;
    }

    @Override
    public MopOutputDTO getMopByUser(String userService, String passService, String username) {
        MopOutputDTO output = new MopOutputDTO();
        logger.info("getMopByUser username: " + username);
        try {
            Map<String, Object> filters = new HashMap<>();
            filters.put("createBy-" + GenericDaoImplNewV2.EXAC, username);
            filters.put("status", 0l);

            LinkedHashMap<String, String> orders = new LinkedHashMap<>();
            orders.put("createDate", "DESC");

            List<FlowRunAction> flowRunActions = (new FlowRunActionServiceImpl()).findList(filters, orders);

            List<MopDTO> mopDTOs = new ArrayList<>();
            if (flowRunActions != null) {
                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HHmmss");
                for (FlowRunAction flow : flowRunActions) {
                    if (flow.getCrNumber() != null && (flow.getCrNumber().equals(Config.CR_DEFAULT)
                            || flow.getCrNumber().equals(Config.CR_AUTO_DECLARE_CUSTOMER))) {
                        MopDTO mopDTO = new MopDTO();
                        mopDTO.setCreateTime(sdf.format(flow.getCreateDate()));
                        mopDTO.setMopId(flow.getFlowRunId().toString());
                        mopDTO.setMopName(flow.getFlowRunName());
                        mopDTO.setTemplateName(flow.getFlowTemplates().getFlowTemplateName());

                        mopDTOs.add(mopDTO);
                    }
                }
            }
            output.setResultCode(0);
            output.setResultMessage("SUCCESS");
            output.setMops(mopDTOs);
        } catch (Exception ex) {
            output.setResultCode(1);
            output.setResultMessage(ex.getMessage());
            logger.error(ex.getMessage(), ex);
        }
        return output;
    }

    @Override
    public MopDetailOutputDTO getMopInfo(String userService, String passService, String mopId) {
        MopDetailOutputDTO output = new MopDetailOutputDTO();
        logger.info("getMopInfo mopId: " + mopId);
//        FileOutputStream os = null;
//        File fileOut = null;
        try {
            FlowRunAction flowRun = (new FlowRunActionServiceImpl()).findById(Long.parseLong(mopId));
            List<Node> lstNode = new ArrayList<>();

            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HHmmss");
            MopDetailDTO mopDetail = new MopDetailDTO();
            mopDetail.setCreateTime(sdf.format(flowRun.getCreateDate()));
            mopDetail.setMopId(flowRun.getFlowRunId().toString());
            mopDetail.setMopName(flowRun.getFlowRunName());
            mopDetail.setTemplateName(flowRun.getFlowTemplates().getFlowTemplateName());

            //set danh sach node mang tac dong
            if (flowRun.getNodeRuns() != null) {
                mopDetail.setNodes(new ArrayList<NodeDTO>());
                for (NodeRun nodeRun : flowRun.getNodeRuns()) {
                    mopDetail.getNodes().add(new NodeDTO(nodeRun.getNode().getNodeCode(), nodeRun.getNode().getNodeIp()));
                    if (!lstNode.contains(nodeRun.getNode())) {
                        lstNode.add(nodeRun.getNode());
                    }
                }
            }

//            ServletContext servletContext = (ServletContext) context.getMessageContext().get(MessageContext.SERVLET_CONTEXT);

            mopDetail.setMopFileContent(Base64.encodeBase64String(flowRun.getFileContent()));
            mopDetail.setMopFileName(flowRun.getFlowRunId()
                    + "_" + ZipUtils.clearHornUnicode(flowRun.getFlowRunName()) + ".xlsx");
            mopDetail.setMopFileType("xlsx");

            output.setResultCode(0);
            output.setResultMessage("SUCCESS");
            output.setMopDetailDTO(mopDetail);
            return output;
        } catch (Exception ex) {
            output.setResultCode(1);
            output.setResultMessage(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            return output;
        } finally {
           /* if (os != null) {
                try {
                    os.close();
                } catch (Exception ex) {
                    logger.error(ex.getMessage(), ex);
                }
            }*/
            /*if (fileOut != null) {
                fileOut.delete();
            }*/
        }
    }



}
