/*
 * Created on Feb 15, 2012
 *
 * Copyright (C) 2003 - 2012 by Vega. All rights reserved
 */
package com.viettel.exception;

/**
 * Application exception (for checked exception)
 * 
 * @author hanh
 * 
 */
public class AppException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	/**
	 * Constructor
	 * 
	 * @param str
	 *            a string that explains what the exception condition is
	 */
	public AppException(String str) {
		super(str);
	}

	/**
	 * Default constructor. Takes no arguments
	 */
	public AppException() {
		super();
	}

	/**
	 * Constructor.
	 */
	public AppException(AppException ae) {
		super(ae.getMessage());
	}
}
