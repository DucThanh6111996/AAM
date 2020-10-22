package com.viettel.persistence;

// Created May 9, 2016 9:09:43 AM by quanns2

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.UnitIbm;
import com.viettel.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Service implement for interface TableSqlService.
 * @see UnitIbmService
 * @author quanns2
 */

@Service(value = "unitIbmService")
@Scope("session")
public class UnitIbmServiceImpl extends GenericDaoImpl<UnitIbm, Serializable> implements UnitIbmService,
		Serializable {
	private static Logger logger = LogManager.getLogger(UnitIbmServiceImpl.class);


	@Override
	public UnitIbm findOwner(Long iimAppGroupId) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		UnitIbm owner = null;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();

			String sql = "select a.PM as pm, u.UNIT_NAME as unitName, p.name as productName from APP_GROUP_ISIM_IBM a left join PRODUCT_IBM p on a.PRODUCT_CODE=p.CODE left join UNIT_IBM u on p.UNIT_CODE=u.UNIT_CODE where a.APP_GROUP_ID=:iimAppGroupId";
			Query query = session.createSQLQuery(sql).addScalar("pm", StandardBasicTypes.STRING)
					.addScalar("unitName", StandardBasicTypes.STRING)
					.addScalar("productName", StandardBasicTypes.STRING)
					.setParameter("iimAppGroupId", iimAppGroupId).setResultTransformer(Transformers.aliasToBean(UnitIbm.class));

			owner = (UnitIbm) query.uniqueResult();
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
		return owner;
	}
}
