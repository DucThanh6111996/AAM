/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice;

import com.viettel.it.webservice.object.*;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 *
 * @author hienhv4
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface WsAam {
    @WebMethod(operationName = "createActionThread")
    com.viettel.model.Action createActionThread(@WebParam(name = "userService") String userService,
                                                @WebParam(name = "passService") String passService,
                                                @WebParam(name = "id") Long id);


    @WebMethod(operationName = "checkRunningStatus")
    int checkRunningStatus(@WebParam(name = "id") Long id);

    @WebMethod(operationName = "getParamMop")
    ParamsDTO getParamMop(@WebParam(name = "userService") String userService,
                                    @WebParam(name = "passService") String passService,
                                    @WebParam(name = "templateId") Long templateId,
                                    @WebParam(name = "nodeCode") Long nodeCode);

    @WebMethod(operationName = "startRunMop")
    public ResultDTO startRunMop(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                 @WebParam(name = "paramValues") String paramValues, @WebParam(name = "flowTemplateId") Long flowTemplateId,
                                 @WebParam(name = "username") String username, @WebParam(name = "nodeCode") String nodeCode);

    @WebMethod(operationName = "createDtForAlarm")
    ResultTicketAlarmDTO createDtForAlarm(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                          @WebParam(name = "resultTicketAlarmDTO") ResultTicketAlarmDTO resultTicketAlarmDTO);

    @WebMethod(operationName = "createDtAudit")
    AuditAlarmDTO createDtAudit(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                              @WebParam(name = "auditAlarmDTO") AuditAlarmDTO auditAlarmDTO);

    ResultCheckPlanService checkPlanService(@WebParam(name = "userService") String userService,
                                            @WebParam(name = "passService") String passService,
                                            @WebParam(name = "serviceCode") String serviceCode);

}
