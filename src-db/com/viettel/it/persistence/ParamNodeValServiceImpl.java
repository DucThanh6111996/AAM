/** Created on Tue Sep 06 09:17:09 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence;

import com.viettel.it.model.ParamNodeVal;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
* ActionServiceImpl.java
*
* @author Huy, Nguyen Xuan<huynx6@viettel.com.vn>
* @since Tue Sep 06 09:17:09 ICT 2016
* @version 1.0.0
*/
@Scope("session")
@Service(value = "paramNodeValService")
public class ParamNodeValServiceImpl  extends GenericDaoImplNewV2<ParamNodeVal, Long>
implements GenericDaoServiceNewV2<ParamNodeVal, Long>, Serializable {
	private static final long serialVersionUID = -4109611148855610L;

}