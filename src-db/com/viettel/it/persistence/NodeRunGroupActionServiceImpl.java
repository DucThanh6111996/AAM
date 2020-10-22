/** Created on Tue Sep 06 09:17:09 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.it.model.NodeRunGroupAction;
import com.viettel.it.model.NodeRunGroupActionId;
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
import java.util.ArrayList;
import java.util.List;

/**
* NodeRunServiceImpl.java
*
* @author Huy, Nguyen Xuan<huynx6@viettel.com.vn>
* @since Tue Sep 06 09:17:09 ICT 2016
* @version 1.0.0
*/
@Scope("session")
@Service(value = "nodeRunGroupActionService")
public class NodeRunGroupActionServiceImpl  extends GenericDaoImplNewV2<NodeRunGroupAction, NodeRunGroupActionId> implements  Serializable{
	private static final long serialVersionUID = -41096155610L;

	public List<NodeRunGroupActionId> findNodeGroup(Long flowRunId)  throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<NodeRunGroupActionId> list = new ArrayList<>();
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();

			String sql = "select NODE_ID nodeId, FLOW_RUN_ID flowRunId, STEP_NUM stepNum from NODE_RUN_GROUP_ACTION where FLOW_RUN_ID=:flowRunId";

			Query query = session.createSQLQuery(sql).addScalar("nodeId", StandardBasicTypes.LONG)
					.addScalar("flowRunId", StandardBasicTypes.LONG)
					.addScalar("stepNum", StandardBasicTypes.LONG)
					.setParameter("flowRunId", flowRunId)
					.setResultTransformer(Transformers.aliasToBean(NodeRunGroupActionId.class));

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