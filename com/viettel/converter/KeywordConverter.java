package com.viettel.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Created by VTN-PTPM-NV55 on 5/27/2019.
 */
@FacesConverter("keywordConverter")
public class KeywordConverter implements Converter {
        @Override
        public Object getAsObject(FacesContext context, UIComponent component, String value) {
            return value != null ? value : null;
        }

        @Override
        public String getAsString(FacesContext context, UIComponent component, Object value) {
            return (String) value;
        }

}
