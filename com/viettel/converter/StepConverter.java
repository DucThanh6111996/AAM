package com.viettel.converter;

/**
 * Created by quan on 7/28/2016.
 */
import com.viettel.bean.RunStep;
import org.omnifaces.converter.SelectItemsConverter;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.FacesConverter;

/**
 * @author quanns2
 */
@FacesConverter("stepConverter")
public class StepConverter extends SelectItemsConverter {

    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
        if(object != null) {
            if (object instanceof  RunStep)
                return String.valueOf(((RunStep) object).getValue());
        }
        else {
            return null;
        }

        return null;
    }
}
