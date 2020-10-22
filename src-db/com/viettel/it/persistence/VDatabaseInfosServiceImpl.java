/** Created on Tue Sep 06 09:17:10 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence;

import com.viettel.it.model.VDatabaseInfos;
import com.viettel.it.util.PasswordEncoder;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
* VendorServiceImpl.java
*
* @author Huy, Nguyen Xuan<huynx6@viettel.com.vn>
* @since Tue Sep 06 09:17:10 ICT 2016
* @version 1.0.0
*/
@Scope("session")
@Service(value = "vDatabaseInfosService")
public class VDatabaseInfosServiceImpl  extends GenericDaoImplNewV2<VDatabaseInfos, String> implements  Serializable{
	private static final long serialVersionUID = -4109611148855610L;

	public static void main(String[] args) {
		try {
//			System.out.println(new VDatabaseInfosServiceImpl().findList().get(0).getListIpVirtual());
			System.out.println(PasswordEncoder.encrypt("qwertyuiop"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
}