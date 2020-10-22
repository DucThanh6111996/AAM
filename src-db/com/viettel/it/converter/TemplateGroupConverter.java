package com.viettel.it.converter;

// Created Aug 19, 2016 1:57:49 PM by quanns2

import com.viettel.it.model.TemplateGroup;
import com.viettel.it.persistence.TemplateGroupServiceImpl;
import com.viettel.it.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author quanns2
 *
 */
@FacesConverter(value = "templateGroupConverter")
public class TemplateGroupConverter implements Converter {
    
    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component,
                              String submittedValue) {
        TemplateGroup o = null;
        try {
            if (submittedValue != null && !"null".equalsIgnoreCase(submittedValue.trim()) && !MessageUtil.getResourceBundleMessage("common.choose").equalsIgnoreCase(submittedValue)) {
                if ("".equals(submittedValue.trim())) {
                    return null;
                }
                Long id = Long.valueOf(submittedValue.trim());
                o = new TemplateGroupServiceImpl().findById(id);
                return o;
            }
        } catch (Exception ex) {
            LOGGER.debug(ex.getMessage(), ex);
        }
        return null;

    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        // hanhnv68 add 20160913
        if (object != null && object instanceof TemplateGroup) {
            // end hanhnv68 add 20160913
            TemplateGroup new_name = (TemplateGroup) object;
            try {
                return new_name.getId().toString();
            } catch (Exception ex) {
                LOGGER.debug(ex.getMessage(), ex);
            }
        }
        return null;
    }
}
