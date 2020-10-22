/** Created on Tue Sep 06 09:17:09 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.persistence;

import com.viettel.bean.ParameterBO;
import com.viettel.bean.RequestInputBO;
import com.viettel.exception.AppException;
import com.viettel.model.RuleConfig;
import com.viettel.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;


@Scope("session")
@Service(value = "RuleConfigService")
public class RuleConfigServiceImpl extends GenericDaoImpl<RuleConfig, Serializable>
		implements RuleConfigService, Serializable {

	private static final long serialVersionUID = -4109611148855610L;
	private static Logger logger = LogManager.getLogger(RuleConfigServiceImpl.class);

	@Override
	public List<RuleConfig> findByActionDetailAppIds(List<Long> actionDetailAppIds) throws AppException {
		Session session = null;
		Transaction tx = null;
		List<RuleConfig> objects = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(domainClass);
			criteria.add(Restrictions.in("actionDetailAppId", actionDetailAppIds));
			criteria.addOrder(Order.asc("actionDetailAppId"));
			criteria.addOrder(Order.asc("ruleId"));
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
	public List<RuleConfig> findByActionId(Long actionId) throws AppException {
		Session session = null;
		Transaction tx = null;
		List<RuleConfig> objects = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(domainClass);
			criteria.add(Restrictions.eq("actionId", actionId));
			criteria.addOrder(Order.asc("actionDetailAppId"));
			criteria.addOrder(Order.asc("ruleId"));
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


}