package com.viettel.converter;

// Created May 13, 2016 9:51:54 AM by quanns2

import com.viettel.model.Checklist;
import com.viettel.persistence.TimeZoneService;
import com.viettel.persistence.TimeZoneServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * @author quanns2
 */
@FacesConverter(value = "checklistConverter")
public class ChecklistConverter implements Converter {
    private static Logger logger = LogManager.getLogger(ChecklistConverter.class);

    private TimeZoneService serviceImpl = new TimeZoneServiceImpl();

    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String submittedValue) {
        try {

            if ("".equals(submittedValue.trim())) {
                return null;
            } else {

                try {
                    long number = Long.parseLong(submittedValue);
                    return serviceImpl.findById(number);

                } catch (NumberFormatException exception) {
                    logger.error(exception.getMessage(), exception);
                    return null;
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    @Override
    public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {
        try {
            if (value == null || "".equals(value)) {
                return "";
            } else {
                return String.valueOf(((Checklist) value).getId());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return "";
        }
    }
}
