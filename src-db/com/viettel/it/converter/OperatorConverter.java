/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.converter;

import com.viettel.it.object.ComboBoxObject;
import com.viettel.it.util.MessageUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author hienhv4
 */
@FacesConverter(value = "operatorConverter")
public class OperatorConverter implements Converter {
    private static Logger logger = LogManager.getLogger(OperatorConverter.class);

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component,
                              String submittedValue) {
        try {
            if (submittedValue != null && !"null".equalsIgnoreCase(submittedValue.trim())
                    && !submittedValue.trim().equals(MessageUtil.getResourceBundleMessage("common.choose"))) {
                if ("".equals(submittedValue.trim())) {
                    return null;
                }
                return new ComboBoxObject(submittedValue.trim(), submittedValue.trim());
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;

    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        // hanhnv68 add 20160913
        if (object != null && object instanceof ComboBoxObject) {
            // end hanhnv68 add 20160913
            ComboBoxObject new_name = (ComboBoxObject) object;
            try {
                return new_name.getValue();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }
}
