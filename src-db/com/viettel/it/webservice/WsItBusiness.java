/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice;

import com.viettel.it.webservice.object.JsonResponseDTO;
import com.viettel.it.webservice.object.ParamsDTO;
import com.viettel.controller.ResultDTO;

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
public interface WsItBusiness {

    @WebMethod(operationName = "getActions")
    ResultDTO getActions(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                         @WebParam(name = "parentId") String parentId, @WebParam(name = "actionId") String actionId);

    @WebMethod(operationName = "getParamByActionId")
    ParamsDTO getParamByActionId(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                 @WebParam(name = "actionId") Long actionId);

    @WebMethod(operationName = "runActionMop")
    ResultDTO runActionMop(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                           @WebParam(name = "actionId") Long actionId, @WebParam(name = "paramValues") String paramValues,
                           @WebParam(name = "username") String username);

    @WebMethod(operationName = "runActionMopSocIT")
    ResultDTO runActionMopSocIT(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                           @WebParam(name = "actionId") Long actionId, @WebParam(name = "paramValues") String paramValues,
                           @WebParam(name = "username") String username);

    //tuanda38
    @WebMethod(operationName = "getResultImpact")
    ResultDTO getResultImpact(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService, @WebParam(name = "socitId") String socitId);

    @WebMethod(operationName = "getJsonData")
    JsonResponseDTO getJsonData(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService, @WebParam(name = "socitId") String socitId);

    @WebMethod(operationName = "sendNotification")
    ResultDTO sendNotification(@WebParam(name = "phone") String phones, @WebParam(name = "content") String content);

    @WebMethod(operationName = "sendNotificationEmail")
    ResultDTO sendNotificationEmail(@WebParam(name = "email") String emails, @WebParam(name = "content") String content);

    @WebMethod(operationName = "sendEmailSms")
    ResultDTO sendEmailSms(@WebParam(name = "subject") String subject,
                 @WebParam(name = "mailUser") String mailUser,
                 @WebParam(name = "phoneUser") String phoneUser,
                 @WebParam(name = "userName") String userName,
                 @WebParam(name = "content") String content);

}
