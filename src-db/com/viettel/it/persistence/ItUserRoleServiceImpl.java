/** Created on Tue Sep 06 09:17:09 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence;

import com.viettel.it.model.ItUserRole;
import com.viettel.it.model.ItUserRoleId;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
* FlowRunActionServiceImpl.java
*
* @author Huy, Nguyen Xuan<huynx6@viettel.com.vn>
* @since Tue Sep 06 09:17:09 ICT 2016
* @version 1.0.0
*/
@Scope("session")
@Service(value = "itUserRoleService")
public class ItUserRoleServiceImpl extends GenericDaoImplNewV2<ItUserRole, ItUserRoleId>
		implements GenericDaoServiceNewV2<ItUserRole, ItUserRoleId>, Serializable {
	private static final long serialVersionUID = -4109611148855610L;

	public static void main (String args[]) {
//		try {
//			List<ItUserRole> service = new ItUserRoleServiceImpl().findList();
//			System.out.println(service.size());
//		}catch (Exception e) {
//			e.printStackTrace();
//		}
	}
}