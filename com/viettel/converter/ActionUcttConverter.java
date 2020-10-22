package com.viettel.converter;

import com.viettel.model.Action;
import com.viettel.persistence.ActionService;
import com.viettel.persistence.ActionServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author quanns2
 */
@FacesConverter("actionUcttConverter")
public class ActionUcttConverter implements Converter {
	private static Logger logger = LogManager.getLogger(ActionUcttConverter.class);

	private ActionService actionService = new ActionServiceImpl();
	@Override
	public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
		try {

			if (value == null || "".equals(value.trim())) {
				return null;
			} else {

				try {
					Map<String, Object> filters = new HashMap<>();
					filters.put("tdCode", value);
					List<Action> actions = actionService.findList(filters, new HashMap<String, String>());

					if (actions != null && !actions.isEmpty())
						return actions.get(0);
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
	}

	@Override
	public String getAsString(FacesContext arg0, UIComponent arg1, Object value) {
		try {
			if (value == null || "".equals(value)) {
				return "";
			} else {
				return String.valueOf(((Action) value).getTdCode());
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return "";
		}
	}
}