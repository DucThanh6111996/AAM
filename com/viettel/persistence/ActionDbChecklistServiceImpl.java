package com.viettel.persistence;

// Created May 26, 2016 5:06:08 PM by quanns2


import com.viettel.model.ActionDbChecklist;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Criteria;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.criterion.Restrictions;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service implement for interface ActionDbChecklistService.
 *
 * @author quanns2
 * @see ActionDbChecklistService
 */

@Service(value = "actionDbChecklistService")
@Scope("session")
public class ActionDbChecklistServiceImpl extends GenericDaoImpl<ActionDbChecklist, Serializable> implements
        ActionDbChecklistService, Serializable {
    private static Logger logger = LogManager.getLogger(ActionDbChecklistServiceImpl.class);
    public Map<Long, List<Long>> getMapCheckList(Long actionModuleId) {
		Map<Long, List<Long>> data = new HashMap<>();
		Session session = null;
		Transaction tx = null;
		List<ActionDbChecklist> objects = null;
		try {
			session = com.viettel.util.HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ActionDbChecklist.class);
			criteria.add(Restrictions.eq("actionId", actionModuleId));
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
		Long checkListId = null;
		Long appDbId = null;
		List<Long> listChecklist = null;
		if (objects != null && objects.size() > 0)
			for (ActionDbChecklist actionModule : objects) {
				checkListId = actionModule.getChecklistId();
				appDbId = actionModule.getAppDbId();
				if (data.containsKey(appDbId))
					listChecklist = data.get(appDbId);
				else
					listChecklist = new ArrayList<>();
				listChecklist.add(checkListId);
				data.put(appDbId, listChecklist);

			}

		return data;
	}
}
