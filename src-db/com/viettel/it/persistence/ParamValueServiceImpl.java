/** Created on Tue Sep 06 09:17:10 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.it.model.ParamValue;
import com.viettel.util.HibernateUtil;
import org.hibernate.HibernateException;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.transform.Transformers;
import org.hibernate.type.StandardBasicTypes;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;

/**
* ParamValueServiceImpl.java
*
* @author Huy, Nguyen Xuan<huynx6@viettel.com.vn>
* @since Tue Sep 06 09:17:10 ICT 2016
* @version 1.0.0
*/
@Scope("session")
@Service(value = "paramValueService")
public class ParamValueServiceImpl  extends GenericDaoImplNewV2<ParamValue, Long> implements  Serializable{
	private static final long serialVersionUID = -4109611148855610L;

	public List<ParamValue> findNodeGroup(Long flowRunId, List<Long> nodeIds)  throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<ParamValue> list;
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();

			String sql = "select PARAM_VALUE_ID paramValueId, PARAM_INPUT_ID paramInputId, PARAM_VALUE paramValue, NODE_ID nodeId from PARAM_VALUE where FLOW_RUN_ID=:flowRunId and NODE_ID in(:nodeIds)";

			Query query = session.createSQLQuery(sql)
					.addScalar("paramValueId", StandardBasicTypes.LONG)
					.addScalar("paramInputId", StandardBasicTypes.LONG)
					.addScalar("paramValue", StandardBasicTypes.STRING)
					.addScalar("nodeId", StandardBasicTypes.LONG)
					.setParameter("flowRunId", flowRunId)
					.setParameterList("nodeIds", nodeIds)
					.setResultTransformer(Transformers.aliasToBean(ParamValue.class));

			list = query.list();
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
}