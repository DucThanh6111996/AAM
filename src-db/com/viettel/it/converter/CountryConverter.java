/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.converter;

import com.viettel.it.persistence.common.CatCountryServiceImpl;
import com.viettel.it.util.MessageUtil;
import com.viettel.model.CatCountryBO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author hanv15
 */
@FacesConverter(value = "countryConverter")
public class CountryConverter implements Converter {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());
    
    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component,
                              String submittedValue) {
        CatCountryBO o = null;
        try {
            if (submittedValue != null && !"null".equalsIgnoreCase(submittedValue.trim()) && !MessageUtil.getResourceBundleMessage("common.choose").equalsIgnoreCase(submittedValue)) {
                if ("".equals(submittedValue.trim())) {
                    return null;
                }

                o = new CatCountryServiceImpl().findById(submittedValue.trim());
                return o;
            }
        } catch (Exception ex) {
            LOGGER.error(ex.getMessage(), ex);
        }
        return null;

    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        // hanhnv68 add 20160913
        if (object != null && object instanceof CatCountryBO) {
            // end hanhnv68 add 20160913
            CatCountryBO new_name = (CatCountryBO) object;
            try {
                return new_name.getCountryCode();
            } catch (Exception ex) {
                LOGGER.error(ex.getMessage(), ex);
            }
        }
        return null;
    }
}
