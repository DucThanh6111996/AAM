/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice;

import com.google.gson.Gson;
import com.sun.net.httpserver.HttpExchange;
import com.sun.xml.internal.ws.developer.JAXWSProperties;
import com.viettel.exception.SysException;
import com.viettel.it.controller.GenerateFlowRunController;
import com.viettel.it.model.*;
import com.viettel.it.object.CheckParamCondition;
import com.viettel.it.object.MessageException;
import com.viettel.it.object.MessageObject;
import com.viettel.it.persistence.*;
import com.viettel.it.util.*;
import com.viettel.it.webservice.object.*;
import com.viettel.it.webservice.utils.MopUtils;
import com.viettel.passprotector.PassProtector;
import com.viettel.controller.AamConstants;
import com.viettel.util.Constant;
import com.viettel.util.SessionUtil;
import org.apache.commons.lang3.StringEscapeUtils;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.resource.transaction.spi.TransactionStatus;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jws.WebService;
import javax.servlet.http.HttpServletRequest;
import javax.xml.ws.WebServiceContext;
import javax.xml.ws.handler.MessageContext;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * @author hohien
 */
@WebService(endpointInterface = "com.viettel.it.webservice.WSForSecurity")
public class WSForSecurityImpl implements WSForSecurity {

    protected SimpleDateFormat S_DATE_FORMAT = new SimpleDateFormat("ddMMyyyy HH:mm:SS");
    protected static final Logger LOGGER = LoggerFactory.getLogger(WSForGNOCImpl.class);
    public static final int RESPONSE_SUCCESS = 1;
    public static final int RESPONSE_FAIL = 0;
    public static final String salt = "vipaForS2";

    @Resource
    private WebServiceContext context;

    //20180620_tudn_start ghi log DB
    public String getRemoteIp() {
        String ip = "";
        MessageContext mc = context.getMessageContext();
        HttpServletRequest req = (HttpServletRequest) mc.get(MessageContext.SERVLET_REQUEST);
        if (req != null) {
            ip = req.getRemoteAddr();
        }
        return ip;
    }
    //20180620_tudn_end ghi log DB


    @Override
    public ResultCreateMop changeEffectiveDateAccount(String requestId, String userService,
                                                      String passService, String crNumber, String accountName, String nodeCode,
                                                      String accountEffectiveDate, String accountAdmin, String passwordAdmin,String accountRoot,String passwordRoot) {
        ResultCreateMop result = new ResultCreateMop();
        result.setRequestId(requestId);
        try {
//            if (!checkWhiteListIp()) {
//                result.setResultCode(100);
//                result.setResultMessage("Bạn không có quyền gọi Webservice");
//                return result;
//            }

            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }

            Map<String, String> params = new HashMap<>();
            params.put("USERNAME", accountName);
            params.put("accountExpireDate", accountEffectiveDate);
            params.put("accountRoot",accountRoot);
            params.put("passwordRoot",passwordRoot);

            return saveDT(requestId, crNumber, "changeExpireDateAccount", nodeCode, accountAdmin, passwordAdmin, params);
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }
    }

    @Override
    public ResultCreateMop changeExpireDateAccount(String requestId, String userService, String passService, String serviceCode, String crNumber, 
                                                   String accountName, String nodeCode, String accountEffectiveDate, String accountExpireDate,
                                                   String accountAdmin, String passwordAdmin,String accountRoot,String passwordRoot) {
        ResultCreateMop result = new ResultCreateMop();
        result.setRequestId(requestId);
        try {
//            if (!checkWhiteListIp()) {
//                result.setResultCode(100);
//                result.setResultMessage("Bạn không có quyền gọi Webservice");
//                return result;
//            }

            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }

            Map<String, String> params = new HashMap<>();
            params.put("USERNAME", accountName);
            params.put("EFFECTIVE_DATE", accountEffectiveDate);
            params.put("EXPIRATION_DATE", accountExpireDate);
            params.put("ACCOUNT_ROOT",accountRoot);
            params.put("PASSWORD_ROOT",passwordRoot);

            return saveDT(requestId, crNumber, serviceCode, nodeCode, accountAdmin, passwordAdmin, params);
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }
    }

    @Override
    public ResultCreateMop changePasswordAccount(String requestId, String userService, String passService, String serviceCode, 
                                                 String crNumber, String accountName, String nodeCode, String passwordNew, String accountAdmin,
                                                 String passwordAdmin ,String accountRoot, String passwordRoot) {
        ResultCreateMop result = new ResultCreateMop();
        result.setRequestId(requestId);
        try {
//            if (!checkWhiteListIp()) {
//                result.setResultCode(100);
//                result.setResultMessage("Bạn không có quyền gọi Webservice");
//                return result;
//            }

            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }

            Map<String, String> params = new HashMap<>();
            params.put("USERNAME", accountName);
            params.put("PASSWORD", passwordNew);
            params.put("ACCOUNT_ROOT",accountRoot);
            params.put("PASSWORD_ROOT",passwordRoot);

            return saveDT(requestId, crNumber, serviceCode, nodeCode, accountAdmin, passwordAdmin, params);
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }
    }

    @Override
    public ResultCreateMop changePasswordAccountNormal(String requestId, String userService, String passService, String serviceCode, 
                                                       String crNumber, String accountName, String nodeCode, String accountAdmin,
                                                       String passwordAdmin, String passwordNew, String accountRoot,String passwordRoot) {
        ResultCreateMop result = new ResultCreateMop();
        result.setRequestId(requestId);
        try {
//            if (!checkWhiteListIp()) {
//                result.setResultCode(100);
//                result.setResultMessage("Bạn không có quyền gọi Webservice");
//                return result;
//            }

            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }

            Map<String, String> params = new HashMap<>();
            params.put("USERNAME", accountName);
            params.put("PASSWORD", passwordNew);
            params.put("ACCOUNT_ROOT",accountRoot);
            params.put("PASSWORD_ROOT",passwordRoot);

            return saveDT(requestId, crNumber, serviceCode, nodeCode, accountAdmin, passwordAdmin, params);
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }
    }

    @Override
    public ResultCreateMop changeRoleAccount(String requestId, String userService, String passService, String serviceCode, 
                                             String crNumber, String accountName, String nodeCode, String roles, String accountAdmin,
                                             String passwordAdmin,String accountRoot,String passwordRoot) {
        ResultCreateMop result = new ResultCreateMop();
        result.setRequestId(requestId);
        try {
//            if (!checkWhiteListIp()) {
//                result.setResultCode(100);
//                result.setResultMessage("Bạn không có quyền gọi Webservice");
//                return result;
//            }

            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }

            Map<String, String> params = new HashMap<>();
            params.put("USERNAME", accountName);
            params.put("ROLE", roles);
            params.put("ACCOUNT_ROOT",accountRoot);
            params.put("PASSWORD_ROOT",passwordRoot);

            return saveDT(requestId, crNumber, serviceCode, nodeCode, accountAdmin, passwordAdmin, params);
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }
    }

    @Override
    public ResultCreateMop deleteAccount(String requestId, String userService, String passService, String serviceCode,
                                         String crNumber, String accountName, String nodeCode, String accountAdmin, 
                                         String passwordAdmin, String accountRoot,String passwordRoot) {
        ResultCreateMop result = new ResultCreateMop();
        try {
//            if (!checkWhiteListIp()) {
//                result.setResultCode(100);
//                result.setResultMessage("Bạn không có quyền gọi Webservice");
//                return result;
//            }

            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }

            Map<String, String> params = new HashMap<>();
            params.put("USERNAME", accountName);
            params.put("ACCOUNT_ROOT",accountRoot);
            params.put("PASSWORD_ROOT",passwordRoot);

            return saveDT(requestId, crNumber, serviceCode, nodeCode, accountAdmin, passwordAdmin, params);
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }
    }

    @Override
    public ResultCreateMop createAccount(String requestId, String userService, String passService, String serviceCode,
                                         String crNumber, String accountName, String password, String nodeCode, String roles, String objectRole,
                                         String accountExpireDate, String accountEffectiveDate, String homeDir, String accountAdmin, String passwordAdmin,
                                         String accountRoot, String passwordRoot) {
        ResultCreateMop result = new ResultCreateMop();
        result.setRequestId(requestId);
        try {
//            if (!checkWhiteListIp()) {
//                result.setResultCode(100);
//                result.setResultMessage("Bạn không có quyền gọi Webservice");
//                return result;
//            }

            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }

            Map<String, String> params = new HashMap<>();
            params.put("USERNAME", accountName);
            params.put("PASSWORD", password);
            params.put("ROLE", roles);
            params.put("OBJECT_ROLE", objectRole);
            params.put("EFFECTIVE_DATE", accountEffectiveDate);
            params.put("EXPIRATION_DATE", accountExpireDate);
            params.put("ACCOUNT_ROOT",accountRoot);
            params.put("PASSWORD_ROOT",passwordRoot);
            params.put("HOME_DIR",homeDir);

            return saveDT(requestId, crNumber, serviceCode, nodeCode, accountAdmin, passwordAdmin, params);
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }
    }

    @Override
    public ResultCreateMop grantRoleAccount(String requestId, String userService, String passService, String serviceCode,
                                            String crNumber, String accountName, String nodeCode, String roles, String objectRole,
                                            String accountAdmin, String passwordAdmin,String accountRoot,String passwordRoot) {
        ResultCreateMop result = new ResultCreateMop();
        result.setRequestId(requestId);
        try {
//            if (!checkWhiteListIp()) {
//                result.setResultCode(100);
//                result.setResultMessage("Bạn không có quyền gọi Webservice");
//                return result;
//            }

            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }

            Map<String, String> params = new HashMap<>();
            params.put("USERNAME", accountName);
            params.put("ROLE", roles);
            params.put("OBJECT_ROLE", objectRole);
            params.put("ACCOUNT_ROOT",accountRoot);
            params.put("PASSWORD_ROOT",passwordRoot);

            return saveDT(requestId, crNumber, serviceCode, nodeCode, accountAdmin, passwordAdmin, params);
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }
    }

    @Override
    public ResultCreateMop revokeRoleAccount(String requestId, String userService, String passService, String serviceCode,
                                             String crNumber, String accountName, String nodeCode, String roles, String objectRole,
                                             String accountAdmin, String passwordAdmin,String accountRoot,String passwordRoot) {
        ResultCreateMop result = new ResultCreateMop();
        result.setRequestId(requestId);
        try {
//            if (!checkWhiteListIp()) {
//                result.setResultCode(100);
//                result.setResultMessage("Bạn không có quyền gọi Webservice");
//                return result;
//            }

            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }

            Map<String, String> params = new HashMap<>();
            params.put("USERNAME", accountName);
            params.put("ROLE", roles);
            params.put("OBJECT_ROLE", objectRole);
            params.put("ACCOUNT_ROOT",accountRoot);
            params.put("PASSWORD_ROOT",passwordRoot);

            return saveDT(requestId, crNumber, serviceCode, nodeCode, accountAdmin, passwordAdmin, params);
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }
    }

    @Override
    public ResultCreateMop viewAccount(String requestId, String userService, String passService, String serviceCode,
                                       String crNumber, String accountName, String nodeCode, String accountAdmin, 
                                       String passwordAdmin,String accountRoot,String passwordRoot) {
        ResultCreateMop result = new ResultCreateMop();
        result.setRequestId(requestId);
        try {
//            if (!checkWhiteListIp()) {
//                result.setResultCode(100);
//                result.setResultMessage("Bạn không có quyền gọi Webservice");
//                return result;
//            }
//
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }

            Map<String, String> params = new HashMap<>();
            params.put("USERNAME", accountName);
            params.put("ACCOUNT_ROOT",accountRoot);
            params.put("PASSWORD_ROOT",passwordRoot);
            
            result = saveDT(requestId, crNumber, serviceCode, nodeCode, accountAdmin, passwordAdmin, params);
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }

        return result;
    }

    @Override
    public ResultCreateMop ressetAccount(String requestId, String userService, String passService, String serviceCode,
            String crNumber, String accountName, String password, String nodeCode, 
            String accountAdmin, String passwordAdmin,String accountRoot,String passwordRoot) {
        ResultCreateMop result = new ResultCreateMop();
        result.setRequestId(requestId);
        try {
//            if (!checkWhiteListIp()) {
//                result.setResultCode(100);
//                result.setResultMessage("Bạn không có quyền gọi Webservice");
//                return result;
//            }
//
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }

            Map<String, String> params = new HashMap<>();
            params.put("USERNAME", accountName);
            params.put("PASSWORD", password);
            params.put("ACCOUNT_ROOT",accountRoot);
            params.put("PASSWORD_ROOT",passwordRoot);

            result = saveDT(requestId, crNumber, serviceCode, nodeCode, accountAdmin, passwordAdmin, params);
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }

        return result;
    }

    @Override
    public FlowTemplatesResult getServiceTemplates(String requestId, String userService, String passService) {
        FlowTemplatesResult result = new FlowTemplatesResult();
        result.setRequestId(requestId);
        try {
//            if (!checkWhiteListIp()) {
//                result.setResultCode(100);
//                result.setResultMessage("Bạn không có quyền gọi Webservice");
//                return result;
//            }
//
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }

            ArrayList<FlowTemplateObj> lsTemplate = new ArrayList<>();
            StringBuilder hql = new StringBuilder();
            //hql.append(" select new FlowTemplates(fts, stmp) from FlowTemplates as fts left join ServiceTemplateMapping as stmp on fts.flowTemplatesId = stmp.templateId");
            hql.append(" select fts from FlowTemplates as fts where fts.status = 9 order by fts.flowTemplateName");
            List<FlowTemplates> lstTemplate = new FlowTemplatesServiceImpl().findList(hql.toString(), -1, -1);
            if (lstTemplate != null) {

                List<ServiceTemplateMapping> mapping = new ServiceTemplateMappingServiceImpl().findList();
                Map<Long, String> mapService = new HashMap<>();
                if (mapping != null) {
                    for (ServiceTemplateMapping obj : mapping) {
                        mapService.put(obj.getTemplate().getFlowTemplatesId(), obj.getServiceCode());
                    }
                }

                for (FlowTemplates template : lstTemplate) {
                    lsTemplate.add(new FlowTemplateObj(template.getTemplateGroup() == null ? null : template.getTemplateGroup().getId(),
                            template.getFlowTemplatesId(), template.getFlowTemplateName(),
                            template.getFlowTemplateName(), mapService.get(template.getFlowTemplatesId())));
                }
            }
            result.setLstFlowTemplate(lsTemplate);
            result.setResultCode(0);
            result.setResultMessage("Success");
            LOGGER.info("List template size: " + lsTemplate.size());
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }

        return result;
    }

    @Override
    public ResultCreateMop executeService(String requestId, String userService, String passService,
                                          String crNumber, String serviceCode, String nodeCode, ParamInputDTO params,
                                          String accountAdmin,String passwordAdmin, String accountRoot,String passwordRoot) {
        ResultCreateMop result = new ResultCreateMop();
        result.setRequestId(requestId);
        try {
//            if (!checkWhiteListIp()) {
//                result.setResultCode(100);
//                result.setResultMessage("Bạn không có quyền gọi Webservice");
//                return result;
//            }
//
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }

            Map<String, String> paramMap = new HashMap<>();

            if (params.getParams() != null && !params.getParams().isEmpty()) {
                params.getParams().add(new ParamDTO("crNumber", crNumber));
                params.getParams().add(new ParamDTO("nodeCode", nodeCode));
                params.getParams().add(new ParamDTO("serviceCode", serviceCode));

                String effectiveDate = null, expirDate = null;

                for (ParamDTO pr : params.getParams()) {
                    if ("EFFECTIVE_DATE".equalsIgnoreCase(pr.getParamCode().trim())) {
                        effectiveDate = pr.getParamValue();
                    } else if ("EXPIRATION_DATE".equalsIgnoreCase(pr.getParamCode().trim())) {
                        expirDate = pr.getParamValue();
                    }
                }



                SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
                if (effectiveDate != null) {
                    Calendar effDate = Calendar.getInstance();
                    effDate.setTime(sdf.parse(effectiveDate));

                    DateTime dateTime = new DateTime(effDate);

                    params.getParams().add(new ParamDTO("EFFECTIVE_DATE_date", standardValue(dateTime.getDayOfMonth())));
                    params.getParams().add(new ParamDTO("EFFECTIVE_DATE_month", standardValue(dateTime.getMonthOfYear())));
                    params.getParams().add(new ParamDTO("EFFECTIVE_DATE_year", standardValue(dateTime.getYear())));

                    params.getParams().add(new ParamDTO("EFFECTIVE_DATE_day_from_now", String.valueOf(countDays(effDate.getTime(), new Date()))));
                }

                if (expirDate != null) {
                    Calendar expDate = Calendar.getInstance();
                    expDate.setTime(sdf.parse(expirDate));
                    DateTime dateTime = new DateTime(expDate);


                    params.getParams().add(new ParamDTO("EXPIRATION_DATE_date", standardValue(dateTime.getDayOfMonth())));
                    params.getParams().add(new ParamDTO("EXPIRATION_DATE_month", standardValue(dateTime.getMonthOfYear())));
                    params.getParams().add(new ParamDTO("EXPIRATION_DATE_year", standardValue(dateTime.getYear())));

                    params.getParams().add(new ParamDTO("EXPIRATION_DATE_day_from_now", String.valueOf(countDays(expDate.getTime(), new Date()))));
                }
            }
            paramMap.put("ACCOUNT_ROOT",accountRoot);
            paramMap.put("PASSWORD_ROOT",passwordRoot);

            for (ParamDTO pr : params.getParams()) {
                paramMap.put(pr.getParamCode(), pr.getParamValue());
            }

            return saveDT(requestId, crNumber, serviceCode, nodeCode, accountAdmin, passwordAdmin, paramMap);
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }
    }

    private String standardValue(int value) {
        return (value >= 0 && value < 10) ? "0" + value : String.valueOf(value);
    }

    private static int countDays(Date fromDate, Date toDate) {
        long delta = toDate.getTime() - fromDate.getTime();
        return (int) Math.ceil(delta / (1000.0 * 60 * 60 * 24));
    }

    private ResultCreateMop saveDT(String requestId, String crNumber, String serviceCode, String nodeCode, String account, String password, Map<String, String> params) {
        LOGGER.info(requestId + "\t" + crNumber + "\t" + serviceCode + "\t" + nodeCode + "\t" + account);
        Date startTime = new Date();

        ResultCreateMop result = new ResultCreateMop();
        result.setRequestId(requestId);
        try {
            ServiceTemplateMappingServiceImpl serviceTempService = new ServiceTemplateMappingServiceImpl();
//            ServiceTemplateMapping serviceMapping = serviceTempService.findById(serviceCode);

            /*if (serviceMapping == null) {
                result.setResultCode(3);
                result.setResultMessage("Service Code không tồn tại");
                return result;
            }*/

/*            FlowTemplates flowTemplate = new FlowTemplatesServiceImpl().findById(serviceMapping.getTemplateId());
            if (flowTemplate == null) {
                result.setResultCode(3);
                result.setResultMessage(MessageUtil.getResourceBundleMessage("error.not.found.template"));
                return result;
            } else if (flowTemplate.getStatus() != 9) {
                result.setResultCode(4);
                result.setResultMessage(MessageUtil.getResourceBundleMessage("error.template.not.approved"));
                return result;
            }*/

            Map filters = new HashMap<>();
//            filters.put("nodeCode", nodeCode);
            filters.put("serverId", nodeCode);
            filters.put("active", Constant.status.active);

            List<Node> nodes = new NodeServiceImpl().findList(filters);
            if (nodes == null || nodes.isEmpty()) {
                result.setResultCode(5);
                result.setResultMessage(MessageUtil.getResourceBundleMessage("common.message.noexists"));
                return result;
            }
            Node node = nodes.get(0);

            Map<String, Object> filters1 = new HashMap<>();
            filters1.put("serviceCode", serviceCode);
            List<ServiceTemplateMapping> serviceMappings = serviceTempService.findList(filters1);

            ServiceTemplateMapping serviceMapping = null;
            if (serviceMappings != null && !serviceMappings.isEmpty()) {
                for (ServiceTemplateMapping mapping : serviceMappings) {
                    if (mapping.getVendor() == null || mapping.getVersion() == null || mapping.getNodeType() == null) {
                        continue;
                    }
                    if (node.getVendor().getVendorId().equals(mapping.getVendor().getVendorId())
                            && node.getNodeType().getTypeId().equals(mapping.getNodeType().getTypeId())
                            && node.getVersion().getVersionId().equals(mapping.getVersion().getVersionId())) {
                        serviceMapping = mapping;
                        break;
                    }
                }

                if (serviceMapping == null) {
                    for (ServiceTemplateMapping mapping : serviceMappings) {
                        if (mapping.getVendor() == null || mapping.getNodeType() == null) {
                            continue;
                        }
                        if (node.getVendor().getVendorId().equals(mapping.getVendor().getVendorId())
                                && node.getNodeType().getTypeId().equals(mapping.getNodeType().getTypeId())
                                && mapping.getVersion() == null) {
                            serviceMapping = mapping;
                            break;
                        }
                    }
                }

                if (serviceMapping == null) {
                    for (ServiceTemplateMapping mapping : serviceMappings) {
                        if (mapping.getVendor() == null) {
                            continue;
                        }
                        if (node.getVendor().getVendorId().equals(mapping.getVendor().getVendorId())
                                && mapping.getNodeType() == null && mapping.getVersion() == null) {
                            serviceMapping = mapping;
                            break;
                        }
                    }
                }
            }

            if (serviceMapping == null) {
                result.setResultCode(3);
                result.setResultMessage("Service Code không tồn tại");
                return result;
            }

            FlowTemplates flowTemplate = serviceMapping.getTemplate();
            if (flowTemplate == null) {
                result.setResultCode(3);
                result.setResultMessage("Template không tồn tại");
                return result;
            } else if (flowTemplate.getStatus() != 9) {
                result.setResultCode(4);
                result.setResultMessage("Template chưa được phê duyệt");
                return result;
            }

            if (nodeCode == null || nodeCode.trim().isEmpty()) {
                result.setResultCode(5);
                result.setResultMessage("NodeCode không tồn tại");
                return result;
            }

            //hanhnv68 start
            /**
             * Lay thong tin account tac dong
             */
            NodeAccount nodeAccount = MopUtils.getNodeAccount(node, account);
            if (nodeAccount == null) {
                result.setResultMessage("Cannot get node account from node: "
                        + nodeCode + "/username: " + account + " ==> create new");
//                return result;\
//                password = PassProtector.decrypt(password, salt);
                nodeAccount = new NodeAccount();
                nodeAccount.setActive(Constant.status.active);
                nodeAccount.setUsername(account);
                nodeAccount.setPassword(PassProtector.encrypt(password, Config.SALT));
                nodeAccount.setAccountType(1l);
                nodeAccount.setImpactOrMonitor(1l);
//                nodeAccount.setServerId(node.getNodeId());
                nodeAccount.setServerId(node.getServerId());

                Long nodeAccountId = new NodeAccountServiceImpl().save(nodeAccount);
                nodeAccount.setId(nodeAccountId);
            }
            List<NodeRun> nodeRuns = new ArrayList<>();
            //hanhnv68 end

            GenerateFlowRunController generateFlowRunController = new GenerateFlowRunController();
            FlowRunAction flowRunAction = new FlowRunAction();
            flowRunAction.setRequestId(requestId);
            flowRunAction.setCrNumber(crNumber);
            flowRunAction.setFlowRunName(serviceCode + "_" + nodeCode + "_" + new SimpleDateFormat("yymmdd HHMMss").format(new Date()));
            while (FlowRunAction.isExistFlowName(flowRunAction.getFlowRunName())) {
                flowRunAction.setFlowRunName(FlowRunAction.createFlowRunName(flowRunAction.getFlowRunName()));
            }
            flowRunAction.setCreateDate(new Date());
            flowRunAction.setTimeRun(new Date());
            flowRunAction.setFlowTemplates(flowTemplate);
            flowRunAction.setCreateBy("SYSTEM");
//            flowRunAction.setMopType(0l);
//            flowRunAction.setPortRun(8860L);
            flowRunAction.setStatus(7L);
            //Quytv7_20180905_them ma quoc gia start
            flowRunAction.setCountryCode(node.getCountryCode());
            //Quytv7_20180905_them ma quoc gia end
            generateFlowRunController.setFlowRunAction(flowRunAction);
            generateFlowRunController.setSelectedFlowTemplates(flowTemplate);
            generateFlowRunController.setNodes(new ArrayList<Node>());
            generateFlowRunController.getNodes().add(node);
//            generateFlowRunController.loadGroupAction(0l);
            generateFlowRunController.loadGroupAction(node);


//            Object[] objs = new FlowRunActionServiceImpl().openTransaction();
//            Session session = (Session) objs[0];
//            Transaction tx = (Transaction) objs[1];
            try {
                Map<Long, List<ActionOfFlow>> mapGroupAction = new HashMap<>();
                LOGGER.info("---get mapGroupAction---");
                for (ActionOfFlow actionOfFlow : flowTemplate.getActionOfFlows()) {
                    if (mapGroupAction.get(actionOfFlow.getGroupActionOrder()) == null) {
                        mapGroupAction.put(actionOfFlow.getGroupActionOrder(), new ArrayList<ActionOfFlow>());
                    }
                    mapGroupAction.get(actionOfFlow.getGroupActionOrder()).add(actionOfFlow);
                }
                LOGGER.info("---get mapGroupAction thanh cong " + mapGroupAction.size() + "---");

//                new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction, session, tx, false);
                Long flowRunActionId = new FlowRunActionServiceImpl().save(flowRunAction, null, null, true);
                flowRunAction.setFlowRunId(flowRunActionId);
                LOGGER.info("---save  flowRunAction" + flowRunAction + "---");

                List<NodeRunGroupAction> nodeRunGroupActions = new ArrayList<>();
                List<AccountGroupMop> lstAccGroupMop = new ArrayList<>();
                if (params != null && !params.isEmpty()) {
                    Map<String, String> paramLower = new HashMap<>();
                    for (String key : params.keySet()) {
                        if (key == null) {
                            continue;
                        }
                        paramLower.put(key.trim().toLowerCase(), params.get(key));
                    }

                    nodeRuns = new ArrayList<>();
                    NodeRun nodeRun = new NodeRun(new NodeRunId(node.getNodeId(), flowRunAction.getFlowRunId()), flowRunAction, node);
//                    nodeRun.setAccount(account == null ? node.getAccount() : account);
//                    nodeRun.setPassword(password == null ? node.getPassword() : password);
                    nodeRuns.add(nodeRun);

                    List<ParamValue> paramValues = generateFlowRunController.getParamInputs(node);
                    LOGGER.info("---ket thuc lay paramValues" + paramValues.size() + "---");
                    for (ParamValue paramValue : paramValues) {
                        if (paramValue.getParamInput().getReadOnly()) {
                            continue;
                        }
                        Object value = null;
                        try {
                            value = paramLower.get((paramValue.getParamCode().toLowerCase().trim().replace(" ", "_").replace(".", "_")));
                        } catch (Exception e) {
                            LOGGER.error(e.getMessage(), e);
                        }
                        ResourceBundle bundle = ResourceBundle.getBundle("cas");
                        if (bundle.getString("service").contains("10.61.127.190")) {
                            if (value == null || value.toString().isEmpty()) {
                                value = "TEST_NOT_FOUND";
                            }
                        }
                        if (value == null || value.toString().trim().isEmpty()) {
//                            result.setResultCode(6);
//                            result.setResultMessage("Tham số: " + paramValue.getParamCode() + " chưa được điền");
//                            return result;
                            paramValue.setParamValue(" ");
                        } else {
                            paramValue.setParamValue(value.toString());
                        }
                        paramValue.setNodeRun(nodeRun);
                        paramValue.setCreateTime(new Date());
                        paramValue.setParamValueId(null);
                    }

                    LOGGER.info(" co vao mapGroupAction size = " + mapGroupAction.size());
                    for (Map.Entry<Long, List<ActionOfFlow>> entry : mapGroupAction.entrySet()) {
                        NodeRunGroupAction nodeRunGroupAction = new NodeRunGroupAction(
                                new NodeRunGroupActionId(node.getNodeId(),
                                        flowRunAction.getFlowRunId(),
                                        entry.getValue().get(0).getStepNum()), entry.getValue().get(0), nodeRun);
                        nodeRunGroupActions.add(nodeRunGroupAction);

                        //hanhnv68 start
                        AccountGroupMop accGroup = new AccountGroupMop();
                        accGroup.setNodeAccountId(nodeAccount.getId());
                        accGroup.setNodeId(node.getNodeId());
                        accGroup.setFlowRunId(flowRunActionId);
                        accGroup.setActionOfFlowId(entry.getValue().get(0).getStepNum());
                        lstAccGroupMop.add(accGroup);
                        //hanhnv68 end
                    }
                    LOGGER.info(" insert NodeRunServiceImpl ");
                    new NodeRunServiceImpl().saveOrUpdate(nodeRuns, null , null, true);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), getRemoteIp(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WSForSecurityImpl.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.CREATE,
                                nodeRuns.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB

                    LOGGER.info(" insert ParamValueServiceImpl ");
                    new ParamValueServiceImpl().saveOrUpdate(paramValues, null, null, true);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), getRemoteIp(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WSForSecurityImpl.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.CREATE,
                                paramValues.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                    //20190408_chuongtq start check param when create MOP
                    if(new GenerateFlowRunController().checkConfigCondition(AamConstants.CFG_CHK_PARAM_CONDITION_FOR_SECURITY)){
                        LinkedHashMap<String, CheckParamCondition> mapCheckParamCondition = new LinkedHashMap<>();
                        if(!(new CheckParamCondition().checkParamCondition(flowTemplate.getParamConditions(), nodes, generateFlowRunController.getMapParamValue(), mapCheckParamCondition,false))){
                           
                            result.setResultCode(RESPONSE_FAIL);
                            List<CheckParamCondition> lstResult = new ArrayList<CheckParamCondition>(mapCheckParamCondition.values());
                            String json = (new Gson()).toJson(lstResult);
                            result.setResultMessage("Create DT fail, json result: ["+ StringEscapeUtils.unescapeJava(json) +"]");
                            return result;
                        }
                    }
                    //20190408_chuongtq end check param when create MOP
//                    session.flush();
//                    session.clear();
                    new NodeRunGroupActionServiceImpl().saveOrUpdate(nodeRunGroupActions, null, null, true);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), getRemoteIp(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WSForSecurityImpl.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.CREATE,
                                nodeRunGroupActions.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB

                    //hanhnv68 start
                    LOGGER.info(" insert account group mop ");
                    new AccountGroupMopServiceImpl().saveOrUpdate(lstAccGroupMop,  null, null, true);
                    //20180620_tudn_start ghi log DB
                    try {
                        LogUtils.logAction(LogUtils.appCode, startTime, new Date(), getRemoteIp(),
                                LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WSForSecurityImpl.class.getName(),
                                Thread.currentThread().getStackTrace()[1].getMethodName(),
                                LogUtils.ActionType.CREATE,
                                lstAccGroupMop.toString(), LogUtils.getRequestSessionId());
                    } catch (Exception e) {
                        LOGGER.error(e.getMessage(), e);
                    }
                    //20180620_tudn_end ghi log DB
                    //hanhnv68 end
                }

                flowRunAction.setSecurityParamInput(new Gson().toJson(params));
                new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction);
                //20180620_tudn_start ghi log DB
                try {
                    LogUtils.logAction(LogUtils.appCode, startTime, new Date(), getRemoteIp(),
                            LogUtils.getRemoteIpClient(), LogUtils.getUrl(), WSForSecurityImpl.class.getName(),
                            Thread.currentThread().getStackTrace()[1].getMethodName(),
                            LogUtils.ActionType.CREATE,
                            flowRunAction.toString(), LogUtils.getRequestSessionId());
                } catch (Exception e) {
                    LOGGER.error(e.getMessage(), e);
                }
                //20180620_tudn_end ghi log DB


//                flowRunAction.setFlowRunId(flowRunActionId);
//                new FlowRunActionServiceImpl().saveOrUpdate(flowRunAction);
                result.setFlowRunId(flowRunAction.getFlowRunId());

//                startProcess(flowRunAction,nodeRuns,"",nodeAccount.getUsername(),nodeAccount.getPassword());
                startProcessWithAcc(flowRunAction, nodeRuns, "", account);

            } catch (SysException | HibernateException e) {
//                if (tx.getStatus() != TransactionStatus.ROLLED_BACK && tx.getStatus() != TransactionStatus.COMMITTED) {
//                    tx.rollback();
//                }
                throw e;
            } finally {
//                if (session.isOpen()) {
//                    session.close();
//                }
            }
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage("Có lỗi xảy ra khi gọi Webservice");
            LOGGER.error(ex.getMessage(), ex);
            return result;
        }

        return result;
    }

    private void startProcessWithAcc(FlowRunAction flowRunAction, List<NodeRun> lstNodeRun,
                                     String workorder, String username) throws Exception {
        try {
            MessageObject mesObj;
            mesObj = new MessageObject(flowRunAction.getFlowRunId(),
                    username, null, flowRunAction.getFlowRunName(), "");
            mesObj.setRunType(1);
            mesObj.setErrorMode(1);

            String encrytedMess = new String(org.apache.commons.codec.binary.Base64.encodeBase64((new Gson()).toJson(mesObj).getBytes("UTF-8")), "UTF-8");
            LOGGER.info("vao send message den tien trinh");
            sendMsg2ThreadExecute(encrytedMess);

            LOGGER.info(">>>>>>>>>>>>> START RUN PROCESS WORKORDER: " + workorder);

        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw new Exception(e);
        }
    }

    private void sendMsg2ThreadExecute(String encrytedMess) throws IOException, Exception, MessageException {

        String serverIp = MessageUtil.getResourceBundleConfig("process_socket_ip");
        int serverPort = Integer.parseInt(MessageUtil.getResourceBundleConfig("process_socket_port"));

        LOGGER.info("server: " + serverIp + "/ port: " + serverPort);

        SocketClient client = new SocketClient(serverIp, serverPort);
        client.sendMsg(encrytedMess);

        String socketResult = client.receiveResult();
        if (socketResult != null && socketResult.contains("NOK")) {
            throw new MessageException(socketResult);
        } else {
            return;
        }

    }

//    private void startProcess(FlowRunAction flowRunAction, List<NodeRun> lstNodeRun,
//            String username, String userRun, String passRun) throws Exception {
//        try {
////            passRun = PassProtector.decrypt(passRun, "ipchange");
//
////            Map<String, AccountObj> mapNodeAccount = new HashMap<>();
////            for (NodeRun nodeRun : lstNodeRun) {
////                mapNodeAccount.put(nodeRun.getNode().getNodeCode(), new AccountObj(userRun, passRun));
////            }
//            MessageObject mesObj = new MessageObject(flowRunAction.getFlowRunId(),
//                    username, userRun, passRun, null, flowRunAction.getFlowRunName(), "");
//            mesObj.setRunType(1);
//            mesObj.setErrorMode(1);
////            mesObj.setRunningType(1);
//
//            String encrytedMess = new String(Base64.encodeBase64((new Gson()).toJson(mesObj).getBytes("UTF-8")), "UTF-8");
//            LOGGER.info("vao send message den tien trinh");
////            sendMsg2ThreadExecute(encrytedMess, "VNM-RESET-HALTED");
//            sendMsg2ThreadExecute(encrytedMess);
//        } catch (Exception e) {
//            LOGGER.error(e.getMessage(), e);
//        }
//    }


//    private void sendMsg2ThreadExecute(String encrytedMess, String countryCode) throws Exception {
//        Map<String, Object> filters = new HashMap<>();
//        filters.put("countryCode.countryCode", countryCode);
//        filters.put("status", 1l);
//
//        List<MapProcessCountry> maps = new MapProcessCountryServiceImpl().findListExac(filters, null);
//
//        if (maps != null && !maps.isEmpty()) {
//            //Sap xep lai maps theo thu tu random
//            Collections.shuffle(maps);
//
//            int i = 0;
//            for (MapProcessCountry process : maps) {
//                int serverPort = process.getProcessPort();
//                String serverIp = process.getProcessIp();
//
//                LOGGER.info("server: " + serverIp + "/ port: " + serverPort);
//
//                SocketClient client = new SocketClient(serverIp, serverPort);
//                client.sendMsg(encrytedMess);
//
//                String socketResult = client.receiveResult();
//                if (socketResult != null && socketResult.contains("NOK")) {
//                    if (i == maps.size() - 1) {
//                        throw new MessageException(socketResult);
//                    }
//                } else {
//                    return;
//                }
//                i++;
//            }
//        } else {
//            LOGGER.error("Loi khong lay duoc thong tin country code: " + countryCode);
//        }
//    }

    private boolean checkWhiteListIp() {
        try {
            HttpExchange exchange = (HttpExchange) context.getMessageContext().get(JAXWSProperties.HTTP_EXCHANGE);

            if (exchange == null) {
                exchange = (HttpExchange) context.getMessageContext().get("com.sun.xml.ws.http.exchange");
            }

            String remoteHost;
            if (exchange == null) {
                HttpServletRequest req = (HttpServletRequest) context.getMessageContext().get(MessageContext.SERVLET_REQUEST);
                remoteHost = req.getRemoteAddr();
            } else {
                remoteHost = exchange.getRemoteAddress().getAddress().getHostAddress();
            }

            List<?> ips = new DaoSimpleService().findListSQLAll("SELECT IP from WHITELIST_WS WHERE SYSTEM_NAME='WS_FOR_NCMS'");
            LOGGER.info("IP remote: " + remoteHost);
            if (ips == null || ips.isEmpty()) {
                return true;
            }
            for (Object ip : ips) {
                if (ip != null && ip.equals(remoteHost)) {
                    return true;
                }
            }
            LOGGER.info("Forbidden access: " + remoteHost);
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
            throw e;
        }

        return false;
    }
}
