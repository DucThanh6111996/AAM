/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.vo;

import org.apache.log4j.Logger;

import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author ChucVQ
 */
@WebService(serviceName = "ReveiceFromVOfficeImpl")
public class ReveiceFromVOfficeImpl implements ReceiveFromVOffice {
    private Logger logger = Logger.getLogger(ReceiveMultiFromVOfficeImpl.class);

    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt) {
        return "Hello " + txt + " !";
    }

    public Long returnSignReult(ResultObj resultObj) {
        logger.info("RECIEVE_A_DOCUMENT");
        logger.info(""+resultObj.toString());

        ReceiveMultiFromVOfficeImpl receiveMultiFromVOffice = new ReceiveMultiFromVOfficeImpl();
        return receiveMultiFromVOffice.returnSignReult(resultObj);
    }
}
