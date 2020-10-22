package com.viettel.listener;

import javax.servlet.http.HttpSessionEvent;
import javax.servlet.http.HttpSessionListener;

import org.apache.log4j.Logger;

import com.viettel.thread.LogOnlineProgram;

public class SessionListener implements HttpSessionListener {

	Logger logger = Logger.getLogger(this.getClass());
	
	@Override
	public void sessionCreated(HttpSessionEvent event) {
		
//		System.out.println("sesId : "+event.getSession().getId());
	}

	@Override
	public void sessionDestroyed(HttpSessionEvent event) {
		
//		System.out.println("sesId : "+event.getSession().getId());
		
		Object obj =  event.getSession().getAttribute("def_ssh_con");
		if( obj ==null || ! (obj instanceof LogOnlineProgram) ){
			return;
		}
		LogOnlineProgram onlineProgram = (LogOnlineProgram) obj;
		try {
			onlineProgram.disconnect();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}
