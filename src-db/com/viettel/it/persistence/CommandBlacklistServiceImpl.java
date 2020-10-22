/** Created on Tue Sep 06 09:17:09 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence;


import com.viettel.it.model.CommandBlacklist;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 Created by VTN-PTPM-NV36
*/
@Scope("session")
@Service(value = "commandBlacklistService")
public class CommandBlacklistServiceImpl extends GenericDaoImplNewV2<CommandBlacklist, Long> implements  Serializable{
	private static final long serialVersionUID = -4109611148855610L;

}