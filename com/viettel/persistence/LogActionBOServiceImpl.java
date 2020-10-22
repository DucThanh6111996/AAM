package com.viettel.persistence;

// Created Sep 12, 2016 1:55:33 PM by quanns2

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.LogAction;
import com.viettel.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Service implement for interface ActionCustomActionService.
 * @author quanns2
 */

//@Service(value = "logActionBOService")
//@Scope("session")
public class LogActionBOServiceImpl extends GenericDaoImpl<LogAction, Serializable> implements LogActionBOService,
		Serializable{
	private static Logger logger = LogManager.getLogger(LogActionBOServiceImpl.class);

	@Override
	public void writeLog(java.util.Date startTime,java.util.Date endTime,String appCode,String user,String className,String actionMethod,String actionType,java.util.Date createDate,
						 String content,String detailResult,String requestId) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();

			String sql = "insert into LOG_ACTION a (a.LOG_ACTION_ID, a.START_TIME, a.END_TIME, a.APP_CODE, a.USER_RUN,"
					+ " a.CLASS_NAME, a.ACTION_METHOD, a.ACTION_TYPE, a.CONTENT, a.CREATE_DATE, a.DETAIL_RESULT, a.REQUEST_ID) " +
					"values (LOG_ACTION_SEQ.nextVal, :startTime, :endTime, :appCode, :userRun, :className, :actionMethod, :actionType, :content, :createDate, :detailResult, :requestId)";
			SQLQuery query = session.createSQLQuery(sql);
			query.setTimestamp("startTime", startTime);
			query.setTimestamp("endTime", endTime);
			query.setParameter("appCode", appCode);
			query.setParameter("userRun", user);
			query.setParameter("className", className);
			query.setParameter("actionMethod", actionMethod);
			query.setParameter("actionType", actionType);
			query.setParameter("content", content);
			query.setTimestamp("createDate", createDate);
			query.setParameter("detailResult", detailResult);
			query.setParameter("requestId", requestId);
			query.executeUpdate();

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();

			logger.error(e.getMessage(), e);
			throw new AppException();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();

			logger.error(e.getMessage(), e);
			throw new SysException();
		} finally {
			if (session != null) {
				session.close();
			}
		}
	}
}
