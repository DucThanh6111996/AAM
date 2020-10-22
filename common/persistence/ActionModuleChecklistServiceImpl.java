package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.model.ActionModuleChecklist;
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
 * Service implement for interface ActionModuleChecklistService.
 * 
 * @see com.viettel.persistence.ActionModuleChecklistService
 * @author quanns2
 */

@Service(value = "actionModuleChecklistService")
@Scope("session")
public class ActionModuleChecklistServiceImpl extends GenericDaoImpl<ActionModuleChecklist, Serializable> implements ActionModuleChecklistService, Serializable {
	private static Logger logger = LogManager.getLogger(ActionModuleChecklistServiceImpl.class);

	public Map<Long, List<Long>> getMapCheckList(List<Long> listActionModuleId) {
		Map<Long, List<Long>> data = new HashMap<>();
		Session session = null;
		Transaction tx = null;
		List<ActionModuleChecklist> objects = null;
		try {
			session = com.viettel.util.HibernateUtil.openSession();
			tx = session.beginTransaction();
			Criteria criteria = session.createCriteria(ActionModuleChecklist.class);
			criteria.add(Restrictions.in("actionModuleId", listActionModuleId));
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
		Long actionMuduleId = null;
		List<Long> listChecklist = null;
		if (objects != null && objects.size() > 0)
			for (ActionModuleChecklist actionModule : objects) {
				checkListId = actionModule.getChecklistId();
				actionMuduleId = actionModule.getActionModuleId();
				if (data.containsKey(actionMuduleId))
					listChecklist = data.get(actionMuduleId);
				else
					listChecklist = new ArrayList<>();
				listChecklist.add(checkListId);
				data.put(actionMuduleId, listChecklist);

			}

		return data;
	}

}
