/*
 * Created on Jun 7, 2013
 *
 * Copyright (C) 2013 by Viettel Network Company. All rights reserved
 */
package com.viettel.controller;

import java.io.IOException;
import java.io.Serializable;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.FacesContext;
import javax.faces.event.ComponentSystemEvent;
import javax.servlet.http.HttpServletRequest;

import com.viettel.exception.SysException;
import com.viettel.util.SessionUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Chức năng chính để forward các url tới phần phân quyền.
 * 
 * @author Nguyen Hai Ha (hanh45@viettel.com.vn)
 * @since Jun 7, 2013
 * @version 1.0.0
 */
@RequestScoped
@ManagedBean(name = "forwardService")
public class ForwardController implements Serializable {
	private static Logger logger = LogManager.getLogger(ForwardController.class);

	private static final long serialVersionUID = 4870520554535423726L;
	// Trang home.
	private static final String _HOME_PAGE = "/home";

	/**
	 * Dieu huong den trang home page.
	 * 
	 */
	private void homeForward() {
		FacesContext fc = FacesContext.getCurrentInstance();
		HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
				.getRequest();
		try {
			fc.getExternalContext().redirect(req.getContextPath() + _HOME_PAGE);
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	/**
	 * Redirect toi trang home.
	 * 
	 * @throws IOException
	 */
	public void doForward(final ComponentSystemEvent event) throws IOException {
		homeForward();
	}

	/**
	 * Dieu huong den trang mac dinh cua user.
	 * 
	 * @throws IOException
	 */
	public void doRedirect(final ComponentSystemEvent event) throws IOException {
		try {
			FacesContext fc = FacesContext.getCurrentInstance();
			HttpServletRequest req = (HttpServletRequest) FacesContext.getCurrentInstance().getExternalContext()
					.getRequest();

			// Lay gia tri menu default cua user dang nhap.
			String defaultUrl = SessionUtil.getMenuDefault();
			if (defaultUrl == "")
				homeForward();
			else
				fc.getExternalContext().redirect(req.getContextPath() + defaultUrl);
		} catch (SysException e) {
			logger.error(e.getMessage(), e);
		}
	}
}