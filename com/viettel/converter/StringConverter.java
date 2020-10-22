/*
 * Created on Jun 7, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.converter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * Dung cho truong hop required ma nguoi dung danh toan ky tu space
 * 		Khong can khai bao.
 * 
 * @author Nguyen Hai Ha (hanh45@viettel.com.vn)
 * @since Jun 7, 2013
 * @version 1.0.0
*/
@FacesConverter(forClass=String.class)
public class StringConverter implements Converter {

	@Override
	public Object getAsObject(FacesContext context, UIComponent component, String value) {
		return value != null ? value.trim() : null;
	}

	@Override
	public String getAsString(FacesContext context, UIComponent component, Object value) {
		return (String) value;
	}

}
