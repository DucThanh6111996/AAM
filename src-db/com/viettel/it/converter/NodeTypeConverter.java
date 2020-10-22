package com.viettel.it.converter;

// Created Aug 19, 2016 1:57:49 PM by quanns2

import com.viettel.it.model.NodeType;
import com.viettel.it.persistence.NodeTypeServiceImpl;
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
@FacesConverter(value = "nodeTypeConverter")
public class NodeTypeConverter implements Converter {
    private static Logger logger = LogManager.getLogger(NodeTypeConverter.class);

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component,
                              String submittedValue) {
        NodeType o = null;
        try {
            if (submittedValue != null && !"null".equalsIgnoreCase(submittedValue.trim())) {
                if ("".equals(submittedValue.trim())) {
                    return null;
                }
                Long id = Long.valueOf(submittedValue.trim());
                o = new NodeTypeServiceImpl().findById(id);
                return o;
            }
        } catch (Exception e) {//logger.error(e.getMessage(), e);
        	logger.error(e.getMessage(), e);
        }
        return null;

    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
    	// hanhnv68 add 20160913
        if (object != null && object instanceof NodeType) {
        // end hanhnv68 add 20160913
			NodeType new_name = (NodeType) object;
			try {
				if(new_name.getTypeId()!=null)
					return new_name.getTypeId().toString();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage(), e);
			}
		}
        return null;
    }
}
