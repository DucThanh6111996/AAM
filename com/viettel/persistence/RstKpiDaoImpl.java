package com.viettel.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.Checklist;
import com.viettel.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.MatchMode;
import org.hibernate.criterion.Order;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

@Scope("session")
@Service(value="rstKpiService")
public class RstKpiDaoImpl extends GenericDaoImpl<Checklist, Long> implements RstKpiService, Serializable {
	private static Logger logger = LogManager.getLogger(RstKpiDaoImpl.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	@Override
	public Checklist getKpiByCode(String kpiCode) throws AppException, SysException {
		
		Session session = null;
		Transaction tx = null;
		List<Checklist> list=new ArrayList<>();
		Checklist obj=null;
		
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Checklist.class);
			criteria.add(Restrictions.eq("code", kpiCode));
			list = criteria.list();
			if(list!=null && list.size()>0){
				obj = list.get(0);
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
		return obj;
	}

	@Override
	public List<Long> getKpiByCode(List<String> kpiCodes) throws AppException, SysException {

		Session session = null;
		Transaction tx = null;
		List<Checklist> list = new ArrayList<>();
		List<Long> ids = new ArrayList<>();

		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Checklist.class);
			criteria.add(Restrictions.in("code", kpiCodes));
			list = criteria.list();
			for (Checklist rstKpi : list) {
				ids.add(rstKpi.getId());
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
		return ids;
	}

	@Override
	public List<Checklist> getListByType(Integer type) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<Checklist> list=new ArrayList<>();
		Checklist obj=null;
		
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(Checklist.class);
			criteria.add(Restrictions.eq("type", type));
			criteria.addOrder(Order.desc("code"));
			list = criteria.list();
			
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
		return list;
	}

	@Override
	public Checklist findById(Long id) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		Checklist object = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			switch (id.getClass().getName()) {
				case "java.lang.String":
					String identifierName = session.getSessionFactory().getClassMetadata(domainClass)
							.getIdentifierPropertyName();
					Criteria criteria = session.createCriteria(domainClass);
					criteria.add(Restrictions.ilike(identifierName, id.toString().toLowerCase(), MatchMode.EXACT));
					object = (Checklist) criteria.uniqueResult();
					break;
				default:
					object = (Checklist) session.get(domainClass, id);
					break;
			}

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null)
				tx.rollback();
//			logger.debug(e.getMessage());
			logger.error(e.getMessage(), e);
			throw new AppException();
		} catch (Exception e) {
			if (tx != null)
				tx.rollback();
//			logger.debug(e.getMessage());
			logger.error(e.getMessage(), e);
			throw new SysException();
		} finally {
			if (session != null)
				session.close();
		}
		return object;
	}
}
