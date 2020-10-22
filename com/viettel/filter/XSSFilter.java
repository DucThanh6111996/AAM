/*
 * Created on Mar 24, 2014
 *
 * Copyright (C) 2014 by Viettel Network Company. All rights reserved
 */
package com.viettel.filter;

import java.io.IOException;

import javax.servlet.DispatcherType;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;

/**
 * XSSFilter.java
 * 
 * @author Nguyen Hai Ha (hanh45@viettel.com.vn)
 * @since Mar 24, 2014
 * @version 1.0.0
 */
//@WebFilter(dispatcherTypes = { DispatcherType.REQUEST, DispatcherType.FORWARD,
//		DispatcherType.INCLUDE, DispatcherType.ERROR }, urlPatterns = { "/faces/*" })
public class XSSFilter implements Filter {

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {
	}

	@Override
	public void destroy() {
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response,
			FilterChain chain) throws IOException, ServletException {

		chain.doFilter(new XSSRequestWrapper((HttpServletRequest) request),
				response);
	}
}
