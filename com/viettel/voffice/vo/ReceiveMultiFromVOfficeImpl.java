/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.voffice.vo;

import com.viettel.exception.AppException;
import com.viettel.model.Action;
import com.viettel.persistence.ActionService;
import com.viettel.persistence.ActionServiceImpl;
import org.apache.log4j.Logger;

import java.util.List;
import javax.jws.WebService;
import javax.jws.WebMethod;
import javax.jws.WebParam;

/**
 *
 * @author ChucVQ
 */
@WebService(serviceName = "ReceiveMultiFromVOfficeImpl")
public class ReceiveMultiFromVOfficeImpl implements ReceiveFromVOfficeList {
    private Logger logger = Logger.getLogger(ReceiveMultiFromVOfficeImpl.class);

    /**
     * This is a sample web service operation
     */
    @WebMethod(operationName = "hello")
    public String hello(@WebParam(name = "name") String txt) {
        return "Hello " + txt + " !";
    }

    public Long returnMultiSignReult(ResultObjList resultObjlst) {
        logger.info("RECIEVE_MULTI_DOCUMENT");
        if (resultObjlst != null){
            List<ResultObj> lstRes = resultObjlst.getLstResultObj();
            for (int i = 0; i< lstRes.size(); i++){
                ResultObj obj = lstRes.get(i);
                System.out.println(""+obj.toString());
                Long result = returnSignReult(obj);
                if (result == null || !result.equals(1l))
                    return 2L;
            }
        }
        return 1L;
    }

    public Long returnSignReult(ResultObj resultObj) {
        logger.info("RECIEVE_1_DOCUMENT " + resultObj);

        ActionService actionService = new ActionServiceImpl();
        try {
            Action action = actionService.findActionByCode(resultObj.getTransCode());

            if (action == null)
                return 2L;

            actionService.updateVoResult(resultObj.getSignStatus(), resultObj.getDocumentCode(), resultObj.getLastSignEmail(), resultObj.getPublishDate(),
                    resultObj.getVoTextId(), resultObj.getTransCode());
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
            return 2L;
        }

        return 1L;
    }
}
