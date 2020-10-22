/** Created on Tue Sep 06 09:17:09 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence;

import com.viettel.it.model.CatConfig;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;


@Scope("session")
@Service(value = "CatConfigService")
public class CatConfigServiceImpl extends GenericDaoImplNewV2<CatConfig, Long> implements GenericDaoServiceNewV2<CatConfig, Long>, Serializable{
	private static final long serialVersionUID = -4109611148855610L;

}