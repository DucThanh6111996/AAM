package com.viettel.persistence;

// Created Sep 12, 2016 1:55:33 PM by quanns2

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.ActionCustomAction;
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
import java.util.Date;

/**
 * Service implement for interface ActionCustomActionService.
 * @see com.viettel.persistence.ActionCustomActionService
 * @author quanns2
 */

@Service(value = "actionCustomActionService")
@Scope("session")
public class ActionCustomActionServiceImpl extends GenericDaoImpl<ActionCustomAction, Serializable> implements
		ActionCustomActionService, Serializable {
	private static Logger logger = LogManager.getLogger(ActionCustomActionServiceImpl.class);

	@Override
	public void updateRunStatus(Long id, Integer runStatus, Date startTime, Date endTime) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();

			if (id != null) {
				String sql = "update ACTION_CUSTOM_ACTION SET RUN_STATUS=:runStatus, ROLLBACK_TEST_STATUS=null ";
				if (startTime != null) {
					sql += " , RUN_START_TIME = :startTime  ";
				}
				if (endTime != null) {
					sql += " , RUN_END_TIME = :endTime  ";
				}
				sql		+= " where ID=:id";
				SQLQuery query = session.createSQLQuery(sql);
				query.setParameter("id", id);
				query.setParameter("runStatus", runStatus);
				if (startTime != null) {
					query.setTimestamp("startTime", startTime);
				}
				if (endTime != null) {
					query.setTimestamp("endTime", endTime);
				}
				query.executeUpdate();
			}

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

	@Override
	public void updateRollbackTest(Long id, Integer runStatus, Date startTime, Date endTime) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();

			if (id != null) {
				String sql = "update ACTION_CUSTOM_ACTION SET ROLLBACK_TEST_STATUS=:runStatus ";
				if (startTime != null) {
					sql += " , RUN_START_TIME = :startTime  ";
				}
				if (endTime != null) {
					sql += " , RUN_END_TIME = :endTime  ";
				}
				sql		+= " where ID=:id";
				SQLQuery query = session.createSQLQuery(sql);
				query.setParameter("id", id);
				query.setParameter("runStatus", runStatus);
				if (startTime != null) {
					query.setTimestamp("startTime", startTime);
				}
				if (endTime != null) {
					query.setTimestamp("endTime", endTime);
				}
				query.executeUpdate();
			}

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
