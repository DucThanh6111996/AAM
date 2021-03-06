/** Created on Tue Sep 06 09:17:09 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence.common;


import com.viettel.it.model.LogActionBO;
import com.viettel.it.persistence.GenericDaoImplNewV2;
import com.viettel.it.persistence.GenericDaoServiceNewV2;
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
@Service(value = "LogActionService")
public class LogActionServiceImpl extends GenericDaoImplNewV2<LogActionBO, Long>
		implements GenericDaoServiceNewV2 <LogActionBO, Long>, Serializable {
	private static final long serialVersionUID = -4109611148855610L;
	
}