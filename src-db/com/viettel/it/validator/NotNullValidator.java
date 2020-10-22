package com.viettel.it.validator;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.primefaces.component.calendar.Calendar;
import org.primefaces.component.inputtext.InputText;
import org.primefaces.component.inputtextarea.InputTextarea;
import org.primefaces.component.selectonemenu.SelectOneMenu;

import com.viettel.it.util.MessageUtil;

@FacesValidator("notNullValidator")
public class NotNullValidator implements Validator {
	private static Logger logger = LogManager.getLogger(NotNullValidator.class);

	@Override
	public void validate(FacesContext arg0, UIComponent arg1, Object arg2) throws ValidatorException {
		try {
			String title ="";
			if(arg1 instanceof SelectOneMenu){
				title = ((SelectOneMenu) arg1).getTitle();
			}else if(arg1 instanceof InputText){
				title = ((InputText) arg1).getTitle();
			}else if(arg1 instanceof InputTextarea){
				title = ((InputTextarea) arg1).getTitle();
			}else if(arg1 instanceof Calendar){
				title = ((Calendar) arg1).getTitle();
			}
			
			if(arg2==null || arg2.toString().isEmpty()) {
				throw new Exception(MessageUtil.getResourceBundleMessage("common.required").replace("{0}",title+": "));
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			String message = e.getMessage();
			FacesMessage msg = new FacesMessage("", message);
			msg.setSeverity(FacesMessage.SEVERITY_ERROR);
			throw new ValidatorException(msg);
		}
	}

}
