package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.ActionDetailDatabase;
import com.viettel.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.*;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.Date;
import java.util.List;

/**
 * Service implement for interface ActionDetailDatabaseService.
 * 
 * @see ActionDetailDatabaseService
 * @author quanns2
 */

@Service(value = "actionDetailDatabaseService")
@Scope("session")
public class ActionDetailDatabaseServiceImpl extends GenericDaoImpl<ActionDetailDatabase, Serializable> implements ActionDetailDatabaseService, Serializable {
	private static Logger logger = LogManager.getLogger(ActionDetailDatabaseServiceImpl.class);

	public List<ActionDetailDatabase> findListDetailDb(Long actionId, Integer kbGroup, boolean order, boolean includeTestbed) {
		Session session = null;
		Transaction tx = null;
		List<ActionDetailDatabase> objects = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ActionDetailDatabase.class);
			criteria.add(Restrictions.eq("actionId", actionId));
			if (kbGroup != null)
				criteria.add(Restrictions.eq("kbGroup", kbGroup));

			if (!includeTestbed) {
				criteria.add(Restrictions.eqOrIsNull("testbedMode", 0));
			}

			if (order)
				criteria.addOrder(Order.asc("actionOrder"));
			else
				criteria.addOrder(Order.desc("actionOrder"));
			objects = criteria.list();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();

			logger.error(e.getMessage(), e);

		} catch (Exception e) {
			if (tx != null)
				tx.rollback();

			logger.error(e.getMessage(), e);

		} finally {
			if (session != null)
				session.close();
		}

		return objects;

	}

	@Override
	public void updateRunStatus(Long id, Integer runStatus,Date startTime,Date endTime) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();

			if (id != null) {
				String sql = "update ACTION_DETAIL_DATABASE SET RUN_STATUS=:runStatus ";
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
	public void updateBackupStatus(Long id, Integer backupStatus,Date startTime,Date endTime) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();

			if (id != null) {
				String sql = "update ACTION_DETAIL_DATABASE SET BACKUP_STATUS=:backupStatus ";
				if (startTime != null) {
					sql += " , BACKUP_START_TIME = :startTime  ";
				}
				if (endTime != null) {
					sql += " , BACKUP_END_TIME = :endTime  ";
				}
				sql += " where ID=:id";
				
				SQLQuery query = session.createSQLQuery(sql);
				query.setParameter("id", id);
				query.setParameter("backupStatus", backupStatus);
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
