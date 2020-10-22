package com.viettel.it.converter;

import com.viettel.controller.AppException;
import com.viettel.controller.SysException;
import com.viettel.it.model.CategoryGroupDomain;
import com.viettel.it.persistence.Category.CategoryGroupDomainServiceImpl;
import com.viettel.it.util.MessageUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.convert.FacesConverter;

@FacesConverter("categoryGroupDomainConverter")
public class CategoryGroupDomainConverter implements Converter {

    protected final Logger logger = LoggerFactory.getLogger(getClass());

    @Override
    public Object getAsObject(FacesContext arg0, UIComponent arg1, String arg2) {
        // TODO Auto-generated method stub
        CategoryGroupDomain o;
        try {
            if (arg2 != null && !arg2.trim().isEmpty() && !"null".equalsIgnoreCase(arg2.trim())
                    && !MessageUtil.getResourceBundleMessage("common.choose").equalsIgnoreCase(arg2)) {
                if ("".equals(arg2.trim())) {
                    return null;
                }
                Long id = Long.valueOf(arg2.trim());
                o = new CategoryGroupDomainServiceImpl().findById(id);
                return o;
            }
        } catch (AppException | SysException e) {
            logger.error(e.getMessage(), e);
        } catch (NumberFormatException e) {
//            logger.error(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public String getAsString(FacesContext arg0, UIComponent arg1, Object arg2) {
        // TODO Auto-generated method stub
        if (arg2 instanceof CategoryGroupDomain) {
            CategoryGroupDomain obj = (CategoryGroupDomain) arg2;
            return obj.getId() + "";
        }
        return null;
    }

}
