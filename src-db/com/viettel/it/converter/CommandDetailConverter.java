package com.viettel.it.converter;

// Created Aug 19, 2016 1:57:49 PM by quanns2

import com.viettel.it.model.CommandDetail;
import com.viettel.it.persistence.CommandDetailServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 *
 * @author quanns2
 *
 */
@FacesConverter(value = "commandDetailConverter")
public class CommandDetailConverter implements Converter {
	private static Logger logger = LogManager.getLogger(CommandDetailConverter.class);
	
	@Override
	public Object getAsObject(FacesContext facesContext, UIComponent component,
                              String submittedValue) {
		CommandDetail o = null;
		try {
			if (submittedValue != null && !"null".equalsIgnoreCase(submittedValue.trim())) {
				if ("".equals(submittedValue.trim()))
					return null;
				Long id = Long.valueOf(submittedValue.trim());
				o = new CommandDetailServiceImpl().findById(id);
				return o;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
		
	}
	
	@Override
	public String getAsString(FacesContext fc, UIComponent uic, Object object) {
		if (object != null) {
			return ((CommandDetail) object).getCommandDetailId() + "";
		}
		return null;
	}
}
