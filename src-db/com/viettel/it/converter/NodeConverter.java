package com.viettel.it.converter;

import com.viettel.controller.AppException;
import com.viettel.controller.SysException;
import com.viettel.it.model.Node;
import com.viettel.it.persistence.NodeServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("nodeConverter")
public class NodeConverter implements Converter {
	private static Logger logger = LogManager.getLogger(NodeConverter.class);

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		// TODO Auto-generated method stub
		try {
			return new NodeServiceImpl().findById(Long.valueOf(arg2));
		} catch (NumberFormatException | SysException | AppException e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		// TODO Auto-generated method stub
		if (arg2 != null && arg2 instanceof Node) {
			Node new_name = (Node) arg2;
			if (new_name.getNodeId() != null)
				return new_name.getNodeId().toString();
			else
				return null;
		} else {
			return null;
		}
	}

}
