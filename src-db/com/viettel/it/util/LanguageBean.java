package com.viettel.it.util;

import com.viettel.it.persistence.DaoSimpleService;
import com.viettel.it.persistence.common.CatCountryServiceImpl;
import com.viettel.model.CatCountryBO;
import com.viettel.util.SessionUtil;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ValueChangeEvent;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.Serializable;
import java.util.*;

@SuppressWarnings("serial")
@ManagedBean(name="language")
@SessionScoped
public class LanguageBean implements Serializable {
	
	protected static final Map<String, Object> locales ;
	
	static {
		locales = new HashMap<String, Object>();
		Locale vn = new Locale("vi", "VN");
		locales.put(vn.getLanguage(), vn);
		Locale us = new Locale("en", "US");
		locales.put(us.getLanguage(), us);
	}
        //20160917_hienhv4_fix ngon ngu mac dinh tieng viet_start
	private String localeCode = new Locale("en", "US").getLanguage();
        //20160917_hienhv4_fix ngon ngu mac dinh tieng viet_end

	//20190729_tudn_start sua dau viec quy trinh cho GNOC
	private String language = "en";
	//20190729_tudn_end sua dau viec quy trinh cho GNOC

	public List<String> getCountries(){
		List<String> countries = new ArrayList<>();
		for (Iterator<String> iterator = locales.keySet().iterator(); iterator.hasNext();) {
			String string = iterator.next();
			countries.add(string);
		}
		return countries;
	}
	public List<Locale> getCountrie2s(){
		List<Locale> countries = new ArrayList<>();
		for (Iterator<String> iterator = locales.keySet().iterator(); iterator.hasNext();) {
			String string = iterator.next();
			countries.add((Locale) locales.get(string));
		}
		return countries;
	}

	public void countryLocaleCodeChanged (ValueChangeEvent e){
		
		String newLocaleCode = e.getNewValue().toString();
		for (Iterator<String> iterator = locales.keySet().iterator(); iterator.hasNext();) {
			String localeCode = iterator.next();
			if(localeCode.equals(newLocaleCode)){
				FacesContext.getCurrentInstance().getViewRoot().setLocale((Locale) locales.get(localeCode));
				// hienhv4_20160910_fix loi message_start
				com.viettel.it.util.MessageUtil.setResourceBundle();
				// hienhv4_20160910_fix loi message_end
				//20190729_tudn_start sua dau viec quy trinh cho GNOC
				language = newLocaleCode;
				//20190729_tudn_end sua dau viec quy trinh cho GNOC
			}
		}
	}

	public String getLocaleCode() {
		return localeCode;
	}

	public void setLocaleCode(String localeCode) {
		this.localeCode = localeCode;
	}

	public String getLocaleName(String localeCode){
		switch (localeCode) {
			case "vi":
				return "Tiếng Việt";
			case "en":
				return "English";
			default :
				break;
		}
		return "";
	}

	public static Map<String, Object> getLocales() {
		return locales;
	}


	public List<String> getListCountryCode() {
		List<String> countryCode = new ArrayList<>();
		// thenv_20180630_start
		for (Iterator<String> iterator = countryCodes.iterator(); iterator.hasNext(); ) {
			String string = iterator.next();
			countryCode.add(string);
		}

		return countryCode;
	}


	// thenv_20180630_start
	protected static final Logger logger = LoggerFactory.getLogger(LanguageBean.class);
	static List<String> countryCodes;
	static Map<String, CatCountryBO> timeZones;
	static {
		LinkedHashMap<String, String> order = new LinkedHashMap<>();
		order.put("countryCode","ASC");
		try {
			timeZones = new HashMap<>();
			countryCodes = new ArrayList<>();
			HashMap<String, Object> _filter = new HashMap<>();
			_filter.put("status", 1l);
			List<CatCountryBO> _timeZones = new CatCountryServiceImpl().findList(_filter, order);
			for (CatCountryBO timeZone : _timeZones) {
				timeZones.put(timeZone.getCountryCode(),timeZone);
				countryCodes.add(timeZone.getCountryCode());
			}

		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
	}

	public static List<String> getCountryCodes() {
		return countryCodes;
	}

	public static void setCountryCodes(List<String> countryCodes) {
		LanguageBean.countryCodes = countryCodes;
	}

	public static Map<String, CatCountryBO> getTimeZones() {
		return timeZones;
	}

	public static void setTimeZones(Map<String, CatCountryBO> timeZones) {
		LanguageBean.timeZones = timeZones;
	}
	// thenv_20180630_end

	private String countryCode;

	public String getCountryCode() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = request.getSession();
		countryCode = (String) session.getAttribute("countryCodeCurrent");
		return countryCode;
	}

	public void setCountryCode(String countryCode) {
		this.countryCode = countryCode;
	}

	public String getTimeZoneDisplay(String countryCode) {
		if (timeZones.get(countryCode)!=null)
			return timeZones.get(countryCode).getTimeZone();
		return "";
	}
	public void countryCodeChanged2() {
		HttpServletRequest request = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext().getRequest();
		HttpSession session = request.getSession();
		session.setAttribute("countryCodeCurrent", countryCode);

		try {
			String sqlInsMapUserCountry = "MERGE INTO MAP_USER_COUNTRY D\n" +
					"USING (SELECT ? username, ? country from dual) S ON (D.USER_NAME = S.username and D.COUNTRY_CODE=S.country)\n" +
					"WHEN NOT MATCHED THEN INSERT (ID,USER_NAME,COUNTRY_CODE,STATUS,LAST_LOGIN) VALUES (MAP_USER_COUNTRY_SEQ.nextval, S.username,NULL ,1,sysdate)\n" +
					"WHEN MATCHED THEN UPDATE SET LAST_LOGIN = sysdate";
			new DaoSimpleService().execteNativeBulk(sqlInsMapUserCountry, SessionUtil.getCurrentUsername().toLowerCase(),countryCode);
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		loadTimeZoneForCalendar();
	}
	public void loadTimeZoneForCalendar() {
		String timezone = getTimeZoneDisplay(getCountryCode());
		String[] split = timezone.split("\\)\\s", -1);
		if (split.length==2)
			setTimeZoneForCalendar(split[1]);
	}
	String timeZoneForCalendar;
	public String getTimeZoneForCalendar() {
		if (timeZoneForCalendar==null)
			loadTimeZoneForCalendar();
		return timeZoneForCalendar;
	}

	public void setTimeZoneForCalendar(String timeZoneForCalendar) {
		this.timeZoneForCalendar = timeZoneForCalendar;
	}

	//20190729_tudn_start sua dau viec quy trinh cho GNOC
	public String getLanguage() {
		return language;
	}

	public void setLanguage(String language) {
		this.language = language;
	}
	//20190729_tudn_end sua dau viec quy trinh cho GNOC
}
