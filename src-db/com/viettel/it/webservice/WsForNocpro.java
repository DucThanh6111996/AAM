/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice;

import com.viettel.it.webservice.object.ParamsDTO;
import com.viettel.it.webservice.object.ResultCreateMop;
import com.viettel.controller.ResultDTO;
import com.viettel.it.webservice.object.ResultFlowTemplatesDTO;

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
public interface WsForNocpro {

    @WebMethod(operationName = "getListMopByGroup")
    ResultFlowTemplatesDTO getListMopByGroup(@WebParam(name = "userService") String userService,
                                             @WebParam(name = "passService") String passService,
                                             @WebParam(name = "templateGroupId") Long templateGroupId);

    @WebMethod(operationName = "startRunMop")
    public ResultCreateMop startRunMop(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                       @WebParam(name = "nocDatas") String nocDatas, @WebParam(name = "flowTemplateId") Long flowTemplateId,
                                       @WebParam(name = "requestId") String requestId);

    //20190826_tudn_start lap lich tac dong tu dong GNOC
    @WebMethod(operationName = "updateStatusNocpro")
    ResultDTO updateStatusNocpro(@WebParam(name = "userService") String userService,
                                 @WebParam(name = "passService") String passService,
                                 @WebParam(name = "crCode") String crCode,
                                 @WebParam(name = "cfStatusNocpro") Long cfStatusNocpro );
    //20190826_tudn_end lap lich tac dong tu dong GNOC
}
