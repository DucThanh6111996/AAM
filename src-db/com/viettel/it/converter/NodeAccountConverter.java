package com.viettel.it.converter;

import com.viettel.it.model.NodeAccount;
import com.viettel.it.persistence.NodeAccountServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("nodeAccountConverter")
public class NodeAccountConverter implements Converter {
	private static Logger logger = LogManager.getLogger(NodeAccountConverter.class);

	@Override
	public Object getAsObject(FacesContext facesContext, UIComponent component,
                              String submittedValue) {
		NodeAccount o = null;
		try {
			if (submittedValue != null
					&& !"".equals(submittedValue.trim())
					&& !"null".equalsIgnoreCase(submittedValue.trim())) {
				if ("".equals(submittedValue.trim()))
					return null;
				Long id = null;
				try {
					id = Long.valueOf(submittedValue.trim());
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
				if (id != null) {
					o = new NodeAccountServiceImpl().findById(id);
				}
				return o;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return null;
		
	}
	
	@Override
	public String getAsString(FacesContext fc, UIComponent uic, Object object) {
		if (object != null && object instanceof NodeAccount) {
			return ((NodeAccount) object).getId() + "";
		}
		return null;
	}

}
