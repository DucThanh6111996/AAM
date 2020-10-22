/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.webservice;

import com.viettel.it.webservice.object.FlowTemplatesResult;
import com.viettel.it.webservice.object.ParamInputDTO;
import com.viettel.it.webservice.object.ResultCreateMop;

import java.util.Map;
import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * @author hohien
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface WSForSecurity {
    /*
     accountEffectiveDate: ddMMyyy HH:mm:ss
     */

    @WebMethod(operationName = "changeEffectiveDateAccount")
    ResultCreateMop changeEffectiveDateAccount(@WebParam(name = "requestId") String requestId, @WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                               @WebParam(name = "crNumber") String crNumber, @WebParam(name = "accountName") String accountName,
                                               @WebParam(name = "nodeCode") String nodeCode, @WebParam(name = "accountEffectiveDate") String accountEffectiveDate,
                                               @WebParam(name = "accountAdmin") String accountAdmin, @WebParam(name = "passwordAdmin") String passwordAdmin, @WebParam(name = "accountRoot") String accountRoot, @WebParam(name = "passwordRoot") String passwordRoot);

    /*
     accountExpireDate: ddMMyyy HH:mm:ss
     */
    @WebMethod(operationName = "changeExpireDateAccount")
    ResultCreateMop changeExpireDateAccount(@WebParam(name = "requestId") String requestId, @WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                            @WebParam(name = "serviceCode") String serviceCode, @WebParam(name = "crNumber") String crNumber, @WebParam(name = "accountName") String accountName,
                                            @WebParam(name = "nodeCode") String nodeCode, @WebParam(name = "accountEffectiveDate") String accountEffectiveDate, @WebParam(name = "accountExpireDate") String accountExpireDate,
                                            @WebParam(name = "accountAdmin") String accountAdmin, @WebParam(name = "passwordAdmin") String passwordAdmin, @WebParam(name = "accountRoot") String accountRoot, @WebParam(name = "passwordRoot") String passwordRoot);

    @WebMethod(operationName = "changePasswordAccount")
    ResultCreateMop changePasswordAccount(@WebParam(name = "requestId") String requestId, @WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                          @WebParam(name = "serviceCode") String serviceCode, @WebParam(name = "crNumber") String crNumber, @WebParam(name = "accountName") String accountName,
                                          @WebParam(name = "nodeCode") String nodeCode, @WebParam(name = "passwordNew") String passwordNew,
                                          @WebParam(name = "accountAdmin") String accountAdmin, @WebParam(name = "passwordAdmin") String passwordAdmin, @WebParam(name = "accountRoot") String accountRoot, @WebParam(name = "passwordRoot") String passwordRoot);

    @WebMethod(operationName = "changePasswordAccountNormal")
    ResultCreateMop changePasswordAccountNormal(@WebParam(name = "requestId") String requestId, @WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                                @WebParam(name = "serviceCode") String serviceCode, @WebParam(name = "crNumber") String crNumber, @WebParam(name = "accountName") String accountName,
                                                @WebParam(name = "nodeCode") String nodeCode, @WebParam(name = "accountAdmin") String accountAdmin, @WebParam(name = "passwordAdmin") String passwordAdmin,
                                                @WebParam(name = "passwordNew") String passwordNew, @WebParam(name = "accountRoot") String accountRoot, @WebParam(name = "passwordRoot") String passwordRoot);

    @WebMethod(operationName = "changeRoleAccount")
    ResultCreateMop changeRoleAccount(@WebParam(name = "requestId") String requestId, @WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                      @WebParam(name = "serviceCode") String serviceCode, @WebParam(name = "crNumber") String crNumber, @WebParam(name = "accountName") String accountName,
                                      @WebParam(name = "nodeCode") String nodeCode, @WebParam(name = "roles") String roles,
                                      @WebParam(name = "accountAdmin") String accountAdmin, @WebParam(name = "passwordAdmin") String passwordAdmin, @WebParam(name = "accountRoot") String accountRoot, @WebParam(name = "passwordRoot") String passwordRoot);

    @WebMethod(operationName = "deleteAccount")
    ResultCreateMop deleteAccount(@WebParam(name = "requestId") String requestId, @WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                  @WebParam(name = "serviceCode") String serviceCode, @WebParam(name = "crNumber") String crNumber, @WebParam(name = "accountName") String accountName,
                                  @WebParam(name = "nodeCode") String nodeCode, @WebParam(name = "accountAdmin") String accountAdmin, @WebParam(name = "passwordAdmin") String passwordAdmin, @WebParam(name = "accountRoot") String accountRoot, @WebParam(name = "passwordRoot") String passwordRoot);

    @WebMethod(operationName = "createAccount")
    ResultCreateMop createAccount(@WebParam(name = "requestId") String requestId, @WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                  @WebParam(name = "serviceCode") String serviceCode, @WebParam(name = "crNumber") String crNumber, @WebParam(name = "accountName") String accountName,
                                  @WebParam(name = "password") String password, @WebParam(name = "nodeCode") String nodeCode,
                                  @WebParam(name = "roles") String roles, @WebParam(name = "objectRole") String objectRole, @WebParam(name = "accountExpireDate") String accountExpireDate,
                                  @WebParam(name = "accountEffectiveDate") String accountEffectiveDate,
                                  @WebParam(name = "homeDir") String homeDir,
                                  @WebParam(name = "accountAdmin") String accountAdmin, @WebParam(name = "passwordAdmin") String passwordAdmin, @WebParam(name = "accountRoot") String accountRoot, @WebParam(name = "passwordRoot") String passwordRoot);

    @WebMethod(operationName = "executeService")
    ResultCreateMop executeService(@WebParam(name = "requestId") String requestId, @WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                   @WebParam(name = "crNumber") String crNumber, @WebParam(name = "serviceCode") String serviceCode,
                                   @WebParam(name = "nodeCode") String nodeCode, @WebParam(name = "params") ParamInputDTO params,
                                   @WebParam(name = "accountAdmin") String accountAdmin, @WebParam(name = "passwordAdmin") String passwordAdmin,
                                   @WebParam(name = "accountRoot") String accountRoot, @WebParam(name = "passwordRoot") String passwordRoot);

    @WebMethod(operationName = "grantRoleAccount")
    ResultCreateMop grantRoleAccount(@WebParam(name = "requestId") String requestId, @WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                     @WebParam(name = "serviceCode") String serviceCode, @WebParam(name = "crNumber") String crNumber, @WebParam(name = "accountName") String accountName,
                                     @WebParam(name = "nodeCode") String nodeCode, @WebParam(name = "roles") String roles, @WebParam(name = "objectRole") String objectRole,
                                     @WebParam(name = "accountAdmin") String accountAdmin, @WebParam(name = "passwordAdmin") String passwordAdmin, @WebParam(name = "accountRoot") String accountRoot, @WebParam(name = "passwordRoot") String passwordRoot);

    @WebMethod(operationName = "revokeRoleAccount")
    ResultCreateMop revokeRoleAccount(@WebParam(name = "requestId") String requestId, @WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                      @WebParam(name = "serviceCode") String serviceCode, @WebParam(name = "crNumber") String crNumber, @WebParam(name = "accountName") String accountName,
                                      @WebParam(name = "nodeCode") String nodeCode, @WebParam(name = "roles") String roles, @WebParam(name = "objectRole") String objectRole,
                                      @WebParam(name = "accountAdmin") String accountAdmin, @WebParam(name = "passwordAdmin") String passwordAdmin, @WebParam(name = "accountRoot") String accountRoot, @WebParam(name = "passwordRoot") String passwordRoot);

    @WebMethod(operationName = "viewAccount")
    ResultCreateMop viewAccount(@WebParam(name = "requestId") String requestId, @WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                @WebParam(name = "serviceCode") String serviceCode, @WebParam(name = "crNumber") String crNumber, @WebParam(name = "accountName") String accountName,
                                @WebParam(name = "nodeCode") String nodeCode,
                                @WebParam(name = "accountAdmin") String accountAdmin, @WebParam(name = "passwordAdmin") String passwordAdmin, @WebParam(name = "accountRoot") String accountRoot, @WebParam(name = "passwordRoot") String passwordRoot);

    @WebMethod(operationName = "ressetAccount")
    ResultCreateMop ressetAccount(@WebParam(name = "requestId") String requestId, @WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService,
                                  @WebParam(name = "serviceCode") String serviceCode, @WebParam(name = "crNumber") String crNumber, @WebParam(name = "accountName") String accountName, @WebParam(name = "password") String password,
                                  @WebParam(name = "nodeCode") String nodeCode,
                                  @WebParam(name = "accountAdmin") String accountAdmin, @WebParam(name = "passwordAdmin") String passwordAdmin, @WebParam(name = "accountRoot") String accountRoot, @WebParam(name = "passwordRoot") String passwordRoot);

    @WebMethod(operationName = "getServiceTemplates")
    FlowTemplatesResult getServiceTemplates(@WebParam(name = "requestId") String requestId, @WebParam(name = "userService") String userService, @WebParam(name = "passService") String passService);
}
