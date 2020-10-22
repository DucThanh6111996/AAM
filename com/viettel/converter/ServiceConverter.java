package com.viettel.converter;

import org.omnifaces.converter.SelectItemsConverter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 * @author quanns2
 */
@FacesConverter("serviceConverter")
public class ServiceConverter extends SelectItemsConverter {
	@Override
	public String getAsString(FacesContext fc, UIComponent uic, Object object) {
		if (object != null) {
			if (object instanceof String)
				return object.toString();
			else
				return object.toString();
		}

		return null;
	}
}