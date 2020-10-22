package com.viettel.bean;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.viettel.common.util.MessageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import sun.plugin2.message.Message;

import java.io.Serializable;


/**
 * @author quanns2
 */
public class ChecklistResult implements Serializable {
    private static Logger logger = LogManager.getLogger(ChecklistResult.class);
    
    private String log;
    private Integer mathOption;
    private String mathOptionText;
    private String operationData;
    private Integer status;
    private String threholdValue;
    private Long turnId;

    public String getLog() {
        return log;
    }

    public void setLog(String log) {
        this.log = log;
    }

    public Integer getMathOption() {
        return mathOption;
    }

    public void setMathOption(Integer mathOption) {
        this.mathOption = mathOption;
    }

    public String getOperationData() {
        return operationData;
    }

    public void setOperationData(String operationData) {
        this.operationData = operationData;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getThreholdValue() {
        return threholdValue;
    }

    public void setThreholdValue(String threholdValue) {
        this.threholdValue = threholdValue;
    }

    public Long getTurnId() {
        return turnId;
    }

    public void setTurnId(Long turnId) {
        this.turnId = turnId;
    }

    public String getMathOptionText() {
        return mathOptionText;
    }

    public void setMathOptionText(String mathOptionText) {
        this.mathOptionText = mathOptionText;
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }
}
