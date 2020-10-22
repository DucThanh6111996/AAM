package com.viettel.it.converter;

// Created Aug 19, 2016 1:57:49 PM by quanns2

import com.viettel.it.model.Vendor;
import com.viettel.it.persistence.VendorServiceImpl;
import com.viettel.it.util.MessageUtil;
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
@FacesConverter(value = "vendorConverter")
public class VendorConverter implements Converter {
    private static Logger logger = LogManager.getLogger(VendorConverter.class);

    @Override
    public Object getAsObject(FacesContext facesContext, UIComponent component,
                              String submittedValue) {
        Vendor o = null;
        try {
            /*20181224_hoangnd_valid khi chua chon item_start*/
            if (submittedValue != null && !"null".equalsIgnoreCase(submittedValue.trim()) && !MessageUtil.getResourceBundleMessage("common.choose2").equalsIgnoreCase(submittedValue)) {
            /*20181224_hoangnd_valid khi chua chon item_end*/
                if ("".equals(submittedValue.trim())) {
                    return null;
                }
                Long id = Long.valueOf(submittedValue.trim());
                o = new VendorServiceImpl().findById(id);
                return o;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;

    }

    @Override
    public String getAsString(FacesContext fc, UIComponent uic, Object object) {
    	// hanhnv68 add 20160913
    	if (object != null && object instanceof Vendor) {
    	// end hanhnv68 add 20160913
			Vendor new_name = (Vendor) object;
			try {
			    if (new_name.getVendorId() != null)
				    return new_name.getVendorId().toString();
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.error(e.getMessage(), e);
			}
		}
        return null;
    }
}
