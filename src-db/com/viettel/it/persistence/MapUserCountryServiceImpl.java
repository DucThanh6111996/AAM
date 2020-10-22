/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.;

import com.sun.org.slf4j.internal.LoggerFactory;
import com.viettel.it.model.MapUserCountryBO;
import com.viettel.it.util.MessageUtil;

import com.viettel.vsa.token.UserToken;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.faces.context.FacesContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
/**
 *
 * @author hanv15
 */
@Scope("session")
@Service(value = "mapUserCountryService")
public class MapUserCountryServiceImpl extends GenericDaoImplNewV2<MapUserCountryBO, Long> implements Serializable {

    private static final long serialVersionUID = -4109611148855610L;
    protected static final Logger LOGGER = LoggerFactory.getLogger(GenericDaoImplNewV2.class);

    public List<String> getListCountryForUser() {
        
        List<String> lstCountry = new ArrayList<>();
        
        try {
            HttpServletRequest request = (HttpServletRequest) FacesContext
                    .getCurrentInstance().getExternalContext().getRequest();
            HttpSession session = request.getSession();
            UserToken userToken = (UserToken) session.getAttribute("vsaUserToken");
            if (userToken!=null) {
                String userName = userToken.getUserName();
                Map<String, Object> filters = new HashMap<>();
                filters.put("userName", userName);
                filters.put("status", 1L);
                List<MapUserCountryBO> lstMapUserCountry = findList(filters);
                if (userName != null) {
                    for (MapUserCountryBO o : lstMapUserCountry) {
                        if (userName.equalsIgnoreCase(o.getUserName())) {
                            lstCountry.add(o.getCountryCode());
                        }
                    }
                }
            }
            if (lstCountry.isEmpty()) {
                lstCountry.add(MessageUtil.getResourceBundleMessage("common.choose"));
            }
        } catch (Exception e) {
            LOGGER.error(e.getMessage(), e);
        }
        return lstCountry;
    }
}
