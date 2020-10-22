package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.model.ActionModule;
import com.viettel.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Projections;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implement for interface ActionModuleService.
 * 
 * @see ActionModuleService
 * @author quanns2
 */

@Service(value = "actionModuleService")
@Scope("session")
public class ActionModuleServiceImpl extends GenericDaoImpl<ActionModule, Serializable> implements ActionModuleService, Serializable {
	private static Logger logger = LogManager.getLogger(ActionModuleServiceImpl.class);

	public List<ActionModule> findList(int first, int pageSize, Long actionId) {
		Session session = null;
		Transaction tx = null;
		List<ActionModule> objects = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ActionModule.class);
			criteria.add(Restrictions.eq("actionId", actionId));
			criteria.setFirstResult(first);
			criteria.setMaxResults(pageSize);
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

	public int count(Long actionId) {
		Session session = null;
		Transaction tx = null;
		int count = 0;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ActionModule.class);
			criteria.add(Restrictions.eq("actionId", actionId));
			criteria.setProjection(Projections.rowCount());
			count = ((Long) criteria.uniqueResult()).intValue();
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
		return count;
	}

	public List<Long> findListActionModuleId(Long actionId) {
		Session session = null;
		Transaction tx = null;
		List<ActionModule> objects = null;
		List<Long> datas = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ActionModule.class);
			criteria.add(Restrictions.eq("actionId", actionId));

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
		if (objects != null) {
			datas = new ArrayList<>();
			for (ActionModule obj : objects) {
				datas.add(obj.getId());
			}
		}
		return datas;
	}

	public List<Long> findListModuleId(Long actionId, Integer kbGroup, Boolean includeTestbed) {
		Session session = null;
		Transaction tx = null;
		List<ActionModule> objects = null;
		List<Long> datas = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ActionModule.class);
			criteria.add(Restrictions.eq("actionId", actionId));
			if (kbGroup != null)
				criteria.add(Restrictions.eq("kbGroup", kbGroup));

			if (!includeTestbed) {
				criteria.add(Restrictions.eqOrIsNull("testbedMode", Integer.valueOf(0)));
			}

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
		if (objects != null) {
			datas = new ArrayList<>();
			for (ActionModule obj : objects) {
				datas.add(obj.getModuleId());
			}
		}
		return datas;
	}

	@Override
	public ActionModule findModule(Long actionId, Long moduleId) {
		Session session = null;
		Transaction tx = null;
		ActionModule object = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ActionModule.class);
			criteria.add(Restrictions.eq("actionId", actionId));
			criteria.add(Restrictions.eq("moduleId", moduleId));

			object = (ActionModule) criteria.uniqueResult();
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

		return object;
	}

	public Map<Long, Long> findMapActionModuleId(Long actionId) {
		Session session = null;
		Transaction tx = null;
		List<ActionModule> objects = null;
		Map<Long, Long> datas = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ActionModule.class);
			criteria.add(Restrictions.eq("actionId", actionId));

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
		if (objects != null) {
			datas = new HashMap<>();
			for (ActionModule obj : objects) {
				datas.put(obj.getId(), obj.getModuleId());
			}
		}
		return datas;
	}

	public Map<Long, ActionModule> getMapObjByListAppId(Long actionId) {
		Session session = null;
		Transaction tx = null;
		List<ActionModule> objects = null;
		Map<Long, ActionModule> datas = new HashMap<>();
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ActionModule.class);
//			criteria.add(Restrictions.in("moduleId", appIds));
			criteria.add(Restrictions.eq("actionId", actionId));
//			criteria.setResultTransformer(Criteria.DISTINCT_ROOT_ENTITY);
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
		if (objects != null)
			for (ActionModule applicationDetail : objects) {
				datas.put(applicationDetail.getModuleId(), applicationDetail);
			}
		return datas;
	}

}
