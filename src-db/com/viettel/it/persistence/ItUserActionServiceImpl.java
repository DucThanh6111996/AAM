/** Created on Tue Sep 06 09:17:09 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence;

import java.io.Serializable;

/**
* FlowRunActionServiceImpl.java
*
* @author Huy, Nguyen Xuan<huynx6@viettel.com.vn>
* @since Tue Sep 06 09:17:09 ICT 2016
* @version 1.0.0
*/
@Scope("session")
@Service(value = "itUserActionService")
public class ItUserActionServiceImpl extends GenericDaoImplNewV2<ItUserAction, ItUserActionId>
		implements GenericDaoServiceNewV2<ItUserAction, ItUserActionId>, Serializable {
	private static final long serialVersionUID = -4109611148855610L;

	public static void main(String args[]) {

	}


}