/** Created on Tue Sep 06 09:17:09 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence;

import com.viettel.it.model.ItUserService;
import com.viettel.it.model.ItUserServiceId;
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
@Service(value = "itUserServicesService")
public class ItUserServicesServiceImpl extends GenericDaoImplNewV2<ItUserService, ItUserServiceId>
		implements GenericDaoServiceNewV2<ItUserService, ItUserServiceId>, Serializable {
	private static final long serialVersionUID = -4109611148855610L;
	
}