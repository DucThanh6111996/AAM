package com.viettel.it.converter;

import com.viettel.controller.AppException;
import com.viettel.controller.SysException;
import com.viettel.it.model.ItUsers;
import com.viettel.it.model.Node;
import com.viettel.it.persistence.ItUsersServicesImpl;
import com.viettel.it.persistence.NodeServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("itUserConverter")
public class ItUserConverter implements Converter {
	private static Logger logger = LogManager.getLogger(ItUserConverter.class);

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		// TODO Auto-generated method stub
		try {
			return new ItUsersServicesImpl().findById(Long.valueOf(arg2));
		} catch (NumberFormatException | SysException | AppException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		// TODO Auto-generated method stub
		if (arg2 instanceof ItUsers) {
			ItUsers new_name = (ItUsers) arg2;
			return new_name.getUserId().toString();
		}
		return null;
	}

}
