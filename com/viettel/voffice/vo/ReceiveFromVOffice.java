package com.viettel.voffice.vo;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebService;
import javax.jws.soap.SOAPBinding;

/**
 * Created by quanns2 on 2/9/2017.
 */
@WebService
@SOAPBinding(style = SOAPBinding.Style.RPC)
public interface ReceiveFromVOffice {
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt);

    public Long returnSignReult(ResultObj resultObj);
}
