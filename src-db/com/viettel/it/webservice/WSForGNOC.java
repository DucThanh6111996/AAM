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
import java.util.List;

/**
 *
 * @author hienhv4
 */
@WebService
//@SOAPBinding(style = SOAPBinding.Style.RPC)
@SOAPBinding(style = SOAPBinding.Style.DOCUMENT)
public interface WSForGNOC {

    @WebMethod(operationName = "updateCrCode")
    ResultDTO updateCrCode(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
            @WebParam(name = "mopId") String mopId, @WebParam(name = "crCode") String crCode);

    @WebMethod(operationName = "updateCrStatus")
    ResultDTO updateCrStatus(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
            @WebParam(name = "crCode") String crCode, @WebParam(name = "status") String status);

    @WebMethod(operationName = "getMopByUser")
    MopOutputDTO getMopByUser(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
            @WebParam(name = "username") String username);

    @WebMethod(operationName = "getMopInfo")
    MopDetailOutputDTO getMopInfo(@WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
            @WebParam(name = "mopId") String mopId);

}
