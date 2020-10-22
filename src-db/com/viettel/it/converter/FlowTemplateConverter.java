package com.viettel.it.converter;

import com.viettel.controller.AppException;
import com.viettel.controller.SysException;
import com.viettel.it.model.FlowTemplates;
import com.viettel.it.persistence.FlowTemplatesServiceImpl;
import org.apache.commons.lang3.StringUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

/**
 * @author huynx6
 *
 */
@FacesConverter("flowTemplateConverter")
public class FlowTemplateConverter implements Converter {
	private static Logger logger = LogManager.getLogger(FlowTemplateConverter.class);

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		// TODO Auto-generated method stub
		try {
			if (arg2 == null || arg2.trim().isEmpty()) {
				return null;
			} else if (!StringUtils.isNumeric(arg2.trim())) {
				return null;
			}
			return new FlowTemplatesServiceImpl().findById(Long.parseLong(arg2));
		} catch (NumberFormatException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		} catch (SysException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		} catch (AppException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		// TODO Auto-generated method stub
		if (arg2 instanceof FlowTemplates) {
			FlowTemplates new_name = (FlowTemplates) arg2;
			if (new_name.getFlowTemplatesId() != null) {
				return new_name.getFlowTemplatesId().toString();
			} else {
				return null;
			}
		}
		return null;
	}

}
