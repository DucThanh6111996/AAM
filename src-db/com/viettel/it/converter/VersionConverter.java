/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.converter;

import com.viettel.it.model.Version;
import com.viettel.it.persistence.VersionServiceImpl;
import com.viettel.it.util.PasswordEncoder;
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
@FacesConverter(value = "versionConverter")
public class VersionConverter implements Converter {
    private static Logger logger = LogManager.getLogger(VersionConverter.class);

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component,
                              String submittedValue) {
        Version o;
        try {
            if (submittedValue != null && !"null".equalsIgnoreCase(submittedValue.trim())) {
                if ("".equals(submittedValue.trim())) {
                    return null;
                }
                Long id = Long.valueOf(submittedValue.trim());
                o = new VersionServiceImpl().findById(id);
                return o;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;

    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if (object != null && object instanceof Version) {
            Version new_name = (Version) object;
            try {
                if (new_name.getVersionId() != null)
                    return new_name.getVersionId().toString();
            } catch (Exception e) {
                // TODO Auto-generated catch block
                logger.error(e.getMessage(), e);
            }
        }
        return null;
    }

    public static void main(String args[]) {

    }
}
