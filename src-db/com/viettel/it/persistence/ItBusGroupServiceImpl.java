/** Created on Tue Sep 06 09:17:09 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.it.model.ItBusinessGroup;
import com.viettel.it.model.ItUsers;
import com.viettel.util.HibernateUtil;
import org.hibernate.*;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.*;

/**
* FlowRunActionServiceImpl.java
*
* @author Huy, Nguyen Xuan<huynx6@viettel.com.vn>
* @since Tue Sep 06 09:17:09 ICT 2016
* @version 1.0.0
*/
@Scope("session")
@Service(value = "itBusGroupService")
public class ItBusGroupServiceImpl extends GenericDaoImplNewV2<ItBusinessGroup, Long>
		implements GenericDaoServiceNewV2<ItBusinessGroup, Long>, Serializable {
	private static final long serialVersionUID = -4109611148855610L;

	/*20190116_hoangnd_them thi truong_start*/
	public List<ItBusinessGroup> findLstBusGroup(ItUsers user) throws AppException, SysException {
		Session session = null;
		Transaction tx = null;
		List<ItBusinessGroup> itBusinessGroups;
		List<Long> lstParentId = new ArrayList<>();
		try {
			session = HibernateUtil.openSession();
			tx = session.beginTransaction();

            String sql = "select a.BUSINESS_ID from IT_BUSINESS_GROUP a LEFT JOIN MAP_USER_COUNTRY b on a.BUSINESS_NAME = b.COUNTRY_CODE where b.USER_NAME = :userName and b.STATUS = 1";
            SQLQuery query = session.createSQLQuery(sql);
            query.setString("userName", user.getUserName());
			List<BigDecimal> lst = query.list();
			for (BigDecimal obj : lst) {
				lstParentId.add(obj.longValue());
			}

			Map<String, Object> filters = new HashMap<>();
			filters.put("parentId", lstParentId);
			itBusinessGroups = findList(filters);

			tx.commit();
		} catch (HibernateException e) {
			if (tx != null) {
				tx.rollback();
			}
			logger.error(e.getMessage(), e);
			throw new AppException();
		} catch (Exception e) {
			if (tx != null) {
				tx.rollback();
			}
			logger.error(e.getMessage(), e);
			throw new SysException();
		} finally {
			if (session != null) {
				session.close();
			}
		}

		return itBusinessGroups;
	}
    /*20190116_hoangnd_them thi truong_end*/
}