package com.viettel.converter;

// Created May 12, 2016 11:31:30 AM by quanns2

import com.viettel.bean.ServiceDatabase;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

import com.viettel.persistence.IimService;
import com.viettel.persistence.IimServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.converter.SelectItemsConverter;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 *
 * @author quanns2
 *
 */
@FacesConverter(value = "serviceDbConverter")
public class ServiceDbConverter extends SelectItemsConverter {
	private static Logger logger = LogManager.getLogger(ServiceDbConverter.class);

	private IimService iimService = new IimServiceImpl();

	/*@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String submittedValue) {
		try {
			if ("".equals(submittedValue.trim())) {
				return null;
			} else {

				try {
					Map<String, Object> filters = new HashMap<>();
					filters.put("databaseName", submittedValue);
					List<ServiceDb> serviceDbs = iimService.findList(filters, new HashMap<String, String>());

					if (serviceDbs != null && !serviceDbs.isEmpty())
						return serviceDbs.get(0);
				} catch (NumberFormatException exception) {
					logger.error(exception.getMessage(), exception);
					return null;
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return null;
		}
		return null;
	}*/

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {
		try {
			if (value == null || "".equals(value)) {
				return "";
			} else {
				return String.valueOf(((ServiceDatabase) value).getDbName());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "";
		}
	}
}
