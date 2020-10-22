package com.viettel.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.ActionHistory;
import com.viettel.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Created Aug 1, 2016 9:16:03 AM by quanns2

/**
 * Service implement for interface ActionHistoryService.
 * 
 * @see ActionHistoryService
 * @author quanns2
 */

@Service(value = "actionHistoryService")
@Scope("session")
public class ActionHistoryServiceImpl extends GenericDaoImpl<ActionHistory, Serializable>
		implements ActionHistoryService, Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private static Logger logger = LogManager.getLogger(ActionHistoryServiceImpl.class);

	@Override
	public List<ActionHistory> findNew(int first, int pageSize, Map<String, Object> filters, Map<String, String> orders)
			throws Exception {
		List<ActionHistory> listObj = new ArrayList<>();
		Session session = null;
		Transaction tx = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ActionHistory.class, "history");
			criteria.createAlias("history.action", "action");

			String filedName = "";
			Object fieldValue = null;

			for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
				filedName = it.next();
				fieldValue = filters.get(filedName);
				if (filedName.startsWith("action.")) {
					if (fieldValue instanceof String) {
						criteria.add(Restrictions.ilike(filedName, String.valueOf(fieldValue), MatchMode.ANYWHERE));
					} else {
						criteria.add(Restrictions.eq(filedName, fieldValue));
					}

				} else {

					String newfiledName = "history".concat(filedName);
					if (fieldValue instanceof String) {
						criteria.add(Restrictions.ilike(newfiledName, String.valueOf(fieldValue), MatchMode.ANYWHERE));
					} else {
						criteria.add(Restrictions.eq(newfiledName, fieldValue));
					}
					criteria.add(Restrictions.eq(newfiledName, fieldValue));
				}

			}

			final String _ASC = "ASC";
			final String _DESC = "DESC";
			String propertyName = "";
			String orderType = "";
			for (Iterator<String> it = orders.keySet().iterator(); it.hasNext();) {
				propertyName = it.next();
				orderType = orders.get(propertyName);

				switch (orderType.toUpperCase()) {
				case _ASC:
					criteria.addOrder(Order.asc(propertyName));
					break;
				case _DESC:
					criteria.addOrder(Order.desc(propertyName));
					break;
				default:
					criteria.addOrder(Order.asc(propertyName));
					break;
				}
			}

			listObj = criteria.list();
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

		return listObj;
	}

	@Override
	public int countNew(Map<String, Object> filters) throws Exception {
		Session session = null;
		Transaction tx = null;
		int count = 0;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ActionHistory.class, "history");
			criteria.createAlias("history.action", "action");
			
			String filedName = "";
			Object fieldValue = null;

			for (Iterator<String> it = filters.keySet().iterator(); it.hasNext();) {
				filedName = it.next();
				fieldValue = filters.get(filedName);
				if (filedName.startsWith("action.")) {
					if (fieldValue instanceof String) {
						criteria.add(Restrictions.ilike(filedName, String.valueOf(fieldValue), MatchMode.ANYWHERE));
					} else {
						criteria.add(Restrictions.eq(filedName, fieldValue));
					}
				} else {

					String newfiledName = "history".concat(filedName);
					if (fieldValue instanceof String) {
						criteria.add(Restrictions.ilike(newfiledName, String.valueOf(fieldValue), MatchMode.ANYWHERE));
					} else {
						criteria.add(Restrictions.eq(newfiledName, fieldValue));
					}
					criteria.add(Restrictions.eq(newfiledName, fieldValue));
				}
			}

			criteria.setProjection(Projections.rowCount());
			count = criteria.uniqueResult() == null ? 0 : ((Long) criteria.uniqueResult()).intValue();
			session.flush();
			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
			logger.debug(e.getMessage());
			logger.error(e.getMessage(), e);
			throw new AppException();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
			logger.debug(e.getMessage());
			logger.error(e.getMessage(), e);
			throw new SysException();
		} finally {
			if (session != null)
				session.close();
		}
		return count;
	}
}
