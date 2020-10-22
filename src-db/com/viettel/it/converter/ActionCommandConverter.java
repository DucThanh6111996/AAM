package com.viettel.it.converter;

// Created Aug 19, 2016 1:57:49 PM by quanns2

import com.viettel.it.model.ActionCommand;
import com.viettel.it.persistence.ActionCommandServiceImpl;
import com.viettel.util.PasswordEncoder;
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
@FacesConverter(value = "actionCommandConverter")
public class ActionCommandConverter implements Converter {
	private static Logger logger = LogManager.getLogger(ActionCommandConverter.class);
	
	@Override
	public Object getAsObject(FacesContext facesContext, UIComponent component,
                              String submittedValue) {
		ActionCommand o = null;
		try {
			if (submittedValue != null && !"null".equalsIgnoreCase(submittedValue.trim())) {
				if ("".equals(submittedValue.trim()))
					return null;
				Long id = null;
				try {
					id = Long.valueOf(submittedValue.trim()); 
				} catch (Exception e) {
					logger.debug(e.getMessage(), e);
					logger.error(e.getMessage());
				}
				if (id != null)
					o = new ActionCommandServiceImpl().findById(id);
				return o;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
		
	}
	
	@Override
	public String getAsString(FacesContext fc, UIComponent uic, Object object) {
		if (object != null && object instanceof ActionCommand) {
			return ((ActionCommand) object).getActionCommandId() + "";
		}
		return null;
	}
}
