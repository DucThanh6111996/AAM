package com.viettel.converter;

import com.viettel.bean.Service;
import com.viettel.exception.AppException;
import com.viettel.persistence.IimService;
import com.viettel.persistence.IimServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.ConverterException;
import javax.faces.convert.FacesConverter;
import java.util.List;

/**
 * @author quanns2
 */
@FacesConverter("serviceAutoConverter")
public class ServiceAutoConverter implements Converter {
	private static Logger logger = LogManager.getLogger(ServiceAutoConverter.class);
	public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
		if(value != null && value.trim().length() > 0) {
			try {
				IimService iimService = new IimServiceImpl();
//				service.findService()
				try {
					List<Service> services = iimService.findService((String)fc.getAttributes().get("nationCode"), (Long)fc.getAttributes().get("unitId"));
					if (services != null)
						for (Service service : services) {
							if (value.trim().equalsIgnoreCase(service.toString()))
								return service;
						}
				} catch (AppException e) {
					logger.error(e.getMessage(), e);
				}
				return null;
			} catch(NumberFormatException e) {
				throw new ConverterException(new FacesMessage(FacesMessage.SEVERITY_ERROR, "Conversion Error", "Not a valid theme."));
			}
		}
		else {
			return null;
		}
	}

	public String getAsString(FacesContext fc, UIComponent uic, Object object) {
		if(object != null) {
			return object.toString();
		}
		else {
			return null;
		}
	}
}