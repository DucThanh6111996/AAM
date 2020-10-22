/*
 * Created on Jun 7, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.it.validator;

import java.util.ResourceBundle;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.faces.application.FacesMessage;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.validator.FacesValidator;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

/**
* Validate viettel phone number.
*
* @author Nguyen Hai Ha (hanh45@viettel.com.vn)
* @since Jun 7, 2013
* @version 1.0.0
*/
@FacesValidator("form.ViettelPhoneValidator")
public class ViettelPhoneValidator implements Validator {

	private Pattern pattern;
	private Matcher matcher;

	private static final String PHONE_PATTERN = "^[0-9]{9,12}$";

	public ViettelPhoneValidator() {
		pattern = Pattern.compile(PHONE_PATTERN);
	}

	/**
	 * Validate phone number with regular expression
	 * 
	 * @param phone
	 *  phone for validation
	 *  	0982  0972
			0983  0973
			0984  0974
			0985  0975
			0986  0976
			0987  0977
			0988  0978
			0989  0979
			
			096
			
			01626 01627
			01628 01629
			0163
			0164  0165
			0166  0167
			0168  0169
			         
	 * @return true valid phone, false invalid phone
	 */
	@Override
	public void validate(FacesContext context, UIComponent component,
			Object value) throws ValidatorException {
		ResourceBundle bundle = context.getApplication().getResourceBundle(
				context, "msg");
		String message = bundle.getString("validator.viettelPhone");
		FacesMessage msg = new FacesMessage(message, message);
		msg.setSeverity(FacesMessage.SEVERITY_ERROR);
		
		matcher = pattern.matcher(value.toString().trim());
		String phone = value.toString().trim();
		if (matcher.matches()) {
			
			if(phone.length() == 9 ){
				if( !"98".equalsIgnoreCase(phone.substring(0, 2)) && !"97".equalsIgnoreCase(phone.substring(0, 2)) && !"96".equalsIgnoreCase(phone.substring(0, 2))){
					throw new ValidatorException(msg);
				}
				else {
					if("98".equalsIgnoreCase(phone.substring(0, 2)) || "97".equalsIgnoreCase(phone.substring(0, 2))){
						if("0".equalsIgnoreCase(phone.substring(2, 3)) || "1".equalsIgnoreCase(phone.substring(2, 3)))
							throw new ValidatorException(msg);
					}
				} 
			}
			
			if(phone.length() == 10 ){
				if( !"098".equalsIgnoreCase(phone.substring(0, 3)) && !"097".equalsIgnoreCase(phone.substring(0, 3)) && !"096".equalsIgnoreCase(phone.substring(0, 3)) && !"16".equalsIgnoreCase(phone.substring(0, 2))){
					throw new ValidatorException(msg);
				}
				else {
					if("098".equalsIgnoreCase(phone.substring(0, 3)) || "097".equalsIgnoreCase(phone.substring(0, 3))){
						if("0".equalsIgnoreCase(phone.substring(3, 4)) || "1".equalsIgnoreCase(phone.substring(3, 4)))
							throw new ValidatorException(msg);
					}
					
					if("16".equalsIgnoreCase(phone.substring(0, 2))){
						if("0".equalsIgnoreCase(phone.substring(2, 3)) || "1".equalsIgnoreCase(phone.substring(2, 3)))
							throw new ValidatorException(msg);
						if("2".equalsIgnoreCase(phone.substring(2, 3))){
							if(!"6".equalsIgnoreCase(phone.substring(3, 4)) && !"7".equalsIgnoreCase(phone.substring(3, 4)) && !"8".equalsIgnoreCase(phone.substring(3, 4)) && !"9".equalsIgnoreCase(phone.substring(3, 4)))
								throw new ValidatorException(msg);
						}
					}
				} 
			}
			
			if(phone.length() == 11){
				if( !"016".equalsIgnoreCase(phone.substring(0, 3)) && !"8498".equalsIgnoreCase(phone.substring(0, 4)) && !"8497".equalsIgnoreCase(phone.substring(0, 4)) && !"8496".equalsIgnoreCase(phone.substring(0, 4))){
					throw new ValidatorException(msg);
				}
				else{
					
					if("8498".equalsIgnoreCase(phone.substring(0, 4)) || "8497".equalsIgnoreCase(phone.substring(0, 4))){
						if("0".equalsIgnoreCase(phone.substring(4, 5)) || "1".equalsIgnoreCase(phone.substring(4, 5)))
							throw new ValidatorException(msg);
					}
					
					if("016".equalsIgnoreCase(phone.substring(0, 3))){
						if("0".equalsIgnoreCase(phone.substring(3, 4)) || "1".equalsIgnoreCase(phone.substring(3, 4)))
							throw new ValidatorException(msg);
						
						if("2".equalsIgnoreCase(phone.substring(3, 4))){
							if(!"6".equalsIgnoreCase(phone.substring(4, 5)) && !"7".equalsIgnoreCase(phone.substring(4, 5)) && !"8".equalsIgnoreCase(phone.substring(4, 5)) && !"9".equalsIgnoreCase(phone.substring(4, 5)))
								throw new ValidatorException(msg);
						}
					}
					
				}
			}
			
			if(phone.length() == 12){
				if(!"8416".equalsIgnoreCase(phone.substring(0, 4))){
					throw new ValidatorException(msg);
				}
				else {
					if("0".equalsIgnoreCase(phone.substring(4, 5)) || "1".equalsIgnoreCase(phone.substring(4, 5)))
						throw new ValidatorException(msg);
					
					if("2".equalsIgnoreCase(phone.substring(4, 5))){
						if(!"6".equalsIgnoreCase(phone.substring(5, 6)) && !"7".equalsIgnoreCase(phone.substring(5, 6)) && !"8".equalsIgnoreCase(phone.substring(5, 6)) && !"9".equalsIgnoreCase(phone.substring(5, 6)))
							throw new ValidatorException(msg);
					}
				}
			}
		}
		else {
			throw new ValidatorException(msg);
		}

	}
}
