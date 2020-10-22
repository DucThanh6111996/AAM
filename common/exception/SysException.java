/*
 * Created on Feb 15, 2012
 *
 * Copyright (C) 2003 - 2012 by Vega. All rights reserved
 */
package com.viettel.exception;

/**
 * Runtime exception (for unchecked exception)
 * 
 * @author hanh
 * 
 */
public class SysException extends RuntimeException {
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
	public SysException(String str) {
		super(str);
	}

	/**
	 * Default constructor. Takes no arguments
	 */
	public SysException() {
		super();
	}

	/**
	 * Constructor.
	 */
	public SysException(SysException se) {
		super(se.getMessage());
	}

}
