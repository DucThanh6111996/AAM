package com.viettel.converter;

import com.viettel.model.Action;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.omnifaces.converter.SelectItemsConverter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 * @author quanns2
 */
@FacesConverter("actionConverter")
public class ActionConverter extends SelectItemsConverter {
	private static Logger logger = LogManager.getLogger(ActionConverter.class);
	@Override
	public String getAsString(FacesContext fc, UIComponent uic, Object object) {
		if (object != null) {
			if (object instanceof String)
				return object.toString();
			else {
				Action action = (Action) object;
				if (action.getId() == null)
					return null;
				try {
					return action.getTestbedMode() ? action.getTdCode() + " - TESTBED" : action.getCrNumber();
				} catch (Exception e) {
					logger.error(action);
					logger.debug(e.getMessage(), e);
				}
			}
		}

		return null;
	}
}