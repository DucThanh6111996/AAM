package com.viettel.it.controller;

import com.viettel.controller.AppException;
import com.viettel.it.lazy.LazyDataModelBaseNew;
import com.viettel.it.model.MapUserCountryBO;
import com.viettel.it.persistence.DaoSimpleService;
import com.viettel.it.persistence.MapUserCountryServiceImpl;
import com.viettel.it.util.MessageUtil;
import org.primefaces.context.RequestContext;
import org.primefaces.model.LazyDataModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ViewScoped;
import java.util.*;

/**
 * Created by xuanhuy on 6/5/2017.
 */
@ManagedBean
@ViewScoped
public class UserCountryController {
    LazyDataModel<MapUserCountryBO> lazyDataModel;
    MapUserCountryBO userCountry;
    List<MapUserCountryBO> userCountries;
    MapUserCountryServiceImpl userCountryService;
    protected static final Logger logger = LoggerFactory.getLogger(UserCountryController.class);

    @PostConstruct
    public void onStart(){
        userCountryService = new MapUserCountryServiceImpl();
        LinkedHashMap<String, String> order = new LinkedHashMap<>();
        order.put("userName","ASC");
        /*20180705_hoangnd_fix_bug_loi_hien_thi_du_lieu_khi_phan_trang_start*/
        order.put("id","ASC");
        /*20180705_hoangnd_fix_bug_loi_hien_thi_du_lieu_khi_phan_trang_end*/
        lazyDataModel = new LazyDataModelBaseNew<MapUserCountryBO, Long>(userCountryService, null,order);
        clean();
    }

	// thenv_20180630_start
    public void saveOrUpdate() {
        try {

            /*20180706_hoangnd_check do dai username_start*/
            if(userCountry != null && userCountry.getUserName() != null && userCountry.getUserName().length() > 0) {
                int mUsername = userCountry.getUserName().getBytes("UTF-8").length;
                if(mUsername > 255) {
                    MessageUtil.setErrorMessageFromRes("error.maxlength.255");
                    return;
                }
            }
            /*20180706_hoangnd_check do dai username_end*/

            for (String countryCode : userCountry.getCountryCodes()) {
                StringBuilder sql = new StringBuilder();
                sql.append(" MERGE INTO MAP_USER_COUNTRY D ");
                /*20180705_hoangnd_update_check_trung_chu_hoa_thuong_start*/
                sql.append(" USING (SELECT ? username, ? COUNTRY_CODE, ? STATUS from dual) S ON (lower(D.USER_NAME) = lower(S.username) and D.COUNTRY_CODE = S.COUNTRY_CODE) ");
                /*20180705_hoangnd_update_check_trung_chu_hoa_thuong_end*/
                sql.append(" WHEN MATCHED THEN UPDATE SET D.STATUS = S.STATUS ");
                sql.append(" WHEN NOT MATCHED THEN INSERT (ID,USER_NAME,COUNTRY_CODE,STATUS) VALUES (MAP_USER_COUNTRY_SEQ.nextval, S.username,S.COUNTRY_CODE , S.STATUS) ");

                new DaoSimpleService().execteNativeBulk("DELETE MAP_USER_COUNTRY WHERE USER_NAME = ? AND COUNTRY_CODE is NULL", userCountry.getUserName());
                new DaoSimpleService().execteNativeBulk(sql.toString(), userCountry.getUserName(), countryCode, userCountry.getStatus());
            }
            MessageUtil.setInfoMessageFromRes("info.save.success");
            RequestContext.getCurrentInstance().execute("PF('insUpdateDlg2').hide()");
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
            MessageUtil.setErrorMessageFromRes("error.save.unsuccess");
        }
    }
	// thenv_20180630_end

    public void delete(){
        try {
            userCountryService.delete(userCountry);
            MessageUtil.setInfoMessageFromRes("info.delete.suceess");
        } catch (AppException e) {
            logger.error(e.getMessage(),e);
            MessageUtil.setErrorMessageFromRes("error.delete.unsuceess");
        }
    }
    public void preEdit(MapUserCountryBO userCountry){
        this.userCountry = userCountry;
        this.userCountry.setCountryCodes(new ArrayList<String>());
        Map<String, Object> filter = new HashMap<>();
        filter.put("userName-EXAC",userCountry.getUserName());
        try {
            List<MapUserCountryBO> users = userCountryService.findList(filter);
            for (MapUserCountryBO user : users) {
                this.userCountry.getCountryCodes().add(user.getCountryCode());
				// thenv_20180630_start
                this.userCountry.setStatus(user.getStatus());
				// thenv_20180630_end
            }
        } catch (Exception e) {
            logger.error(e.getMessage(),e);
        }

    }

    public void clean(){
        userCountry = new MapUserCountryBO();
    }

    public LazyDataModel<MapUserCountryBO> getLazyDataModel() {
        return lazyDataModel;
    }

    public void setLazyDataModel(LazyDataModel<MapUserCountryBO> lazyDataModel) {
        this.lazyDataModel = lazyDataModel;
    }

    public MapUserCountryBO getUserCountry() {
        return userCountry;
    }

    public void setUserCountry(MapUserCountryBO userCountry) {
        this.userCountry = userCountry;
    }

    public List<MapUserCountryBO> getUserCountries() {
        return userCountries;
    }

    public void setUserCountries(List<MapUserCountryBO> userCountries) {
        this.userCountries = userCountries;
    }
}
