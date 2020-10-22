package com.viettel.converter;

import org.omnifaces.converter.SelectItemsConverter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 * @author quanns2
 */
@FacesConverter("timeZoneConverter")
public class TimeZoneConverter extends SelectItemsConverter {
	@Override
	public String getAsString(FacesContext fc, UIComponent uic, Object object) {
		if (object != null) {
			return object.toString();
		}

		return null;
	}
}