package com.viettel.it.controller;

import com.viettel.exception.AppException;
import com.viettel.model.TimeZone;
import com.viettel.persistence.TimeZoneService;
import com.viettel.util.SessionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;
import javax.faces.event.ValueChangeEvent;
import javax.faces.model.SelectItem;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@SuppressWarnings("serial")
@ManagedBean
@SessionScoped
public class TimeZoneController implements Serializable {
	private static Logger logger = LogManager.getLogger(TimeZoneController.class);

	@ManagedProperty(value = "#{timeZoneService}")
	private TimeZoneService timeZoneService;

	public void setTimeZoneService(TimeZoneService timeZoneService) {
		this.timeZoneService = timeZoneService;
	}

	private List<SelectItem> timeZones;
	private TimeZone selectedTimezone;

	@PostConstruct
	public void onStart() {
		timeZones = new ArrayList<>();
		try {
			List<TimeZone> zones = timeZoneService.findList();
			for (TimeZone zone : zones) {
				timeZones.add(new SelectItem(zone, zone.toString()));
			}
		} catch (AppException e) {
			logger.error(e.getMessage(), e);
		}
	}

	public void timeZoneChanged (ValueChangeEvent e) {
		TimeZone timeZone = (TimeZone) e.getNewValue();
		SessionUtil.setTimeZone(timeZone);
		logger.info(timeZone);
	}

	public List<SelectItem> getTimeZones() {
		return timeZones;
	}

	public TimeZone getSelectedTimezone() {
		return selectedTimezone;
	}

	public void setSelectedTimezone(TimeZone selectedTimezone) {
		this.selectedTimezone = selectedTimezone;
	}
}
