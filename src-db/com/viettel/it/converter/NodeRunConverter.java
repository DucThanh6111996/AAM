package com.viettel.it.converter;

import com.viettel.it.model.NodeRun;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("nodeRunConverter")
public class NodeRunConverter implements Converter {
	private static Logger logger = LogManager.getLogger(NodeRunConverter.class);

	@Override
	public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
		// TODO Auto-generated method stub
		try {
			//return new NodeRunServiceImpl().findById(Long.valueOf(arg2));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			logger.error(e.getMessage(), e);
		}
		return null;
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
		// TODO Auto-generated method stub
		if (arg2 instanceof NodeRun) {
			NodeRun new_name = (NodeRun) arg2;
			return new_name.getId().toString();
		}
		return null;
	}

}
