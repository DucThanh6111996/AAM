/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice;

import com.viettel.it.model.FlowRunAction;
import com.viettel.it.model.FlowTemplates;
import com.viettel.it.persistence.FlowRunActionServiceImpl;
import com.viettel.it.persistence.FlowTemplatesServiceImpl;
import com.viettel.it.util.MessageUtil;
import com.viettel.it.webservice.object.FlowTemplatesDTO;
import com.viettel.it.webservice.object.ResultCreateMop;
import com.viettel.controller.ResultDTO;
import com.viettel.it.webservice.object.ResultFlowTemplatesDTO;
import com.viettel.passprotector.PassProtector;
import com.viettel.persistence.ActionService;
import com.viettel.util.Util;
import org.apache.commons.collections.map.HashedMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.Resource;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.xml.ws.WebServiceContext;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author hienhv4
 */
@WebService(endpointInterface = "com.viettel.it.webservice.WsForNocpro")
public class WsForNocproImpl implements WsForNocpro {

    protected final Logger logger = LoggerFactory.getLogger(WsForNocproImpl.class);
    public static final int RESPONSE_SUCCESS = 1;
    public static final int RESPONSE_FAIL = 0;

    @Resource
    private WebServiceContext context;

    @Override
    public ResultFlowTemplatesDTO getListMopByGroup(@WebParam(name = "userService") String userService,
                                                    @WebParam(name = "passService") String passService,
                                                    @WebParam(name = "templateGroupId") Long templateGroupId) {

        ResultFlowTemplatesDTO result = new ResultFlowTemplatesDTO();
        result.setMessage("");
        logger.info("start get list template mop");
        try {
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setMessage("Username hoặc mật khẩu webservice không chính xác");
                return result;
            }
        } catch (Exception ex) {
            result.setMessage(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            return result;
        }

        ArrayList<FlowTemplatesDTO> lsTemplate = new ArrayList<>();
        try {
            Map<String, Object> filters = new HashedMap();
            if (templateGroupId != null) {
                filters.put("templateGroup.id", templateGroupId);
            }
            List<FlowTemplates> lstTemplate = new FlowTemplatesServiceImpl().findList(filters);
            if (lstTemplate != null) {
                for (FlowTemplates template : lstTemplate) {
                    lsTemplate.add(new FlowTemplatesDTO(template.getTemplateGroup() == null ? 0 : template.getTemplateGroup().getId(),
                            template.getFlowTemplatesId(), template.getFlowTemplateName(), template.getFlowTemplateName()));
                }
            }
            result.setLstFlowTemplate(lsTemplate);
            logger.info("List template size: " + lsTemplate.size());
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }

    @Override
    public ResultCreateMop startRunMop(String userService, String passService, String nocDatas, Long flowTemplateId, String requestId) {
        return null;
    }

    public static void main(String args[]) {
        try {
            System.out.println(PassProtector.decrypt("BmEhknjTeEta5RbH72yw+bPKLCghAPbJMbXxWDYTN/tytqNI4t4Z6yV3Xg4nl2Rp", "ipchange"));
            System.out.println(PassProtector.decrypt("E0m+EZRcciC50cFT/DS/daX2VqsZpkDIrvjbhkEAcHgz8ljFL5xt/Nx3oOqg8ep5RWmXE9aQwNeB1Er/mlo6/A==", "ipchange"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /*@Override
    public ResultCreateMop startRunMop(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                       @WebParam(name = "nocDatas") String nocDatas, @WebParam(name = "flowTemplateId") Long flowTemplateId,
                                       @WebParam(name = "requestId") String requestId) {
        ResultCreateMop result = null;
        try {
            String vipaUser = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_user_service"), "ipchange");
            String vipaPass = PassProtector.decrypt(MessageUtil.getResourceBundleConfig("vipa_pass_service"), "ipchange");

            if (!vipaUser.equals(userService) || !vipaPass.equals(passService)) {
                result.setResultCode(2);
                result.setResultMessage("UserService/PasswordService incorrect.");
                return result;
            }
            String nodeCode = MopUtils.getNodeCodeFromNocData(nocDatas);
            Map<String, String> params = new HashMap<>();

            // Start run DT
            return MopUtils.runMop(requestId, "", nodeCode,"root", params);

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return result;
    }*/

    //20190826_tudn_start lap lich tac dong tu dong GNOC
    @Override
    public ResultDTO updateStatusNocpro(String userService, String passService, String crNumber, Long cfStatusNocpro) {
        ResultDTO result = new ResultDTO();
        Util.checkAndPrintObject(logger, "updateStatusNocpro:", "cfStatusNocpro", cfStatusNocpro, "crNumber", crNumber);
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

        try {
            if (cfStatusNocpro == null || Util.isNullOrEmpty(crNumber)) {
                result.setResultCode(1);
                result.setResultMessage("Please enter all input information and enter correct");
                return result;
            }
            if (cfStatusNocpro != null && (cfStatusNocpro.compareTo(0L) != 0 && cfStatusNocpro.compareTo(1L) != 0)) {
                result.setResultCode(1);
                result.setResultMessage("cfStatusNocpro can only enter 0 or 1");
                return result;
            }
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage(ex.getMessage());
            logger.error(ex.getMessage(), ex);
            return result;
        }

        try {
            result.setResultCode(0);
            result.setResultMessage("SUCCESS");
            boolean findDichvu = true;
            boolean findHaTang = true;
            boolean saveDichVu = false;
            boolean saveHatang = false;
            try {
                FlowRunActionServiceImpl service = new FlowRunActionServiceImpl();
                String sql = "from FlowRunAction where status in (0,1,10,18)and crNumber = ?";
                List<FlowRunAction> flowRuns = service.findListAll(sql, crNumber);

                if (flowRuns != null && !flowRuns.isEmpty()) {
                    for (FlowRunAction flow : flowRuns) {
                        flow.setCfStatusNocpro(cfStatusNocpro);
                        Util.checkAndPrintObject(logger, "updateStatusNocpro:", "cfStatusNocpro", cfStatusNocpro, "crNumber", crNumber,"flowRunId", flow.getFlowRunId());
                    }
                    service.saveOrUpdate(flowRuns);
                    saveHatang = true;
                } else {
                    findDichvu = false;
                }

                ActionService actionService = new com.viettel.persistence.ActionServiceImpl();
                Map<String, Object> filters = new HashMap<>();
                filters.put("crNumber-EXAC", crNumber);
                filters.put("runningStatus", null);
                List<com.viettel.model.Action> actions = actionService.findList2(filters, new HashMap<>());

                if ((actions == null || actions.isEmpty())) {
                    findHaTang = false;
                } else {
                    for (com.viettel.model.Action updateAction : actions) {
                        actionService.updateAutoRunCr(updateAction.getId(), null, null, null, null, cfStatusNocpro);
                        Util.checkAndPrintObject(logger, "updateStatusNocpro:", "cfStatusNocpro", cfStatusNocpro, "crNumber", crNumber,"actionId", updateAction.getId());
                    }
                    saveDichVu = true;
                }
                if (!findDichvu && !findHaTang) {
                    result.setResultCode(1);
                    result.setResultMessage("Not found cr number in system: " + crNumber);
                    logger.info(result.getResultMessage());
                    return result;
                }
                if (saveHatang || saveDichVu) {
                    return result;
                }
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                result.setResultCode(1);
                result.setResultMessage(e.getMessage());
            }
        } catch (Exception ex) {
            result.setResultCode(1);
            result.setResultMessage(ex.getMessage());
            logger.error(ex.getMessage(), ex);
        }
        return result;
    }

    //20190826_tudn_end lap lich tac dong tu dong GNOC
    private boolean checkPass(String username, String password) {
        return username != null && password != null && "tdtt".equals(username) && "tdtt_vtnet$%^".equals(password);
    }
}
