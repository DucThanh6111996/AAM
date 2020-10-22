/** Created on Tue Sep 06 09:17:09 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence;

import com.viettel.it.model.AccountGroupMop;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * @author Nguyễn Xuân Huy <huynx6@viettel.com.vn>
 * @sin Oct 25, 2016
 * @version 1.0 
 */
@Scope("session")
@Service(value = "accountGroupMopService")
public class AccountGroupMopServiceImpl  extends GenericDaoImplNewV2<AccountGroupMop, Long>
		implements GenericDaoServiceNewV2<AccountGroupMop, Long>, Serializable{
	private static final long serialVersionUID = -41096111355610L;

}