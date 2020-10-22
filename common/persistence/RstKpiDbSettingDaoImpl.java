package com.viettel.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.KpiDbSetting;
import com.viettel.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Criterion;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;


@Scope("session")
@Service(value = "rstKpiDbSettingService")
public class RstKpiDbSettingDaoImpl extends GenericDaoImpl<KpiDbSetting, Long> implements RstKpiDbSettingService, Serializable {
	private static Logger logger = LogManager.getLogger(RstKpiDbSettingDaoImpl.class);
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final Integer PARAMETER_LIMIT =50;

	@Override
	public List<KpiDbSetting> getlistByGroup(Long appGroupId) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<KpiDbSetting> list=new ArrayList<>();
		
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(KpiDbSetting.class);
			criteria.add(Restrictions.eq("appGroupId", appGroupId));
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
	
	public KpiDbSetting findbyKpiId(Long kpiId, Long appDbId) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<KpiDbSetting> list=new ArrayList<>();
		
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(KpiDbSetting.class);
			criteria.add(Restrictions.eq("kpiId", kpiId));
			criteria.add(Restrictions.eq("viewDbId", appDbId));
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
		if(list!=null&& list.size()>0)
			
		return list.get(0);
		else
			return null;
	}

	@Override
	public List<KpiDbSetting> findByIds(List<Long> ids) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<KpiDbSetting> list;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(KpiDbSetting.class);
			criteria.add(this.buildInCriterion("id", ids));
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
			if (session != null)
				session.close();
		}
		return list;
	}

	private Criterion buildInCriterion(String propertyName, List<Long> values) {
		Criterion criterion = null;

		int length = values.size();
		int delay =  length /this.PARAMETER_LIMIT;
		if(length % this.PARAMETER_LIMIT !=0 ) delay++;

		for(int i=0;i<delay;i++){

			List<Long> subList;
			if((i+1)*this.PARAMETER_LIMIT <= length){
				subList = values.subList(i*this.PARAMETER_LIMIT, (i+1)*this.PARAMETER_LIMIT);
			}else{
				subList = values.subList(i*this.PARAMETER_LIMIT, length);
			}
			if (criterion != null) {
				criterion = Restrictions.or(criterion, Restrictions.in(propertyName, subList));
			} else {
				criterion = Restrictions.in(propertyName, subList);
			}
		}
		return criterion;
	}
}
