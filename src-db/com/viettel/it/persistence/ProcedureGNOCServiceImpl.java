/** Created on Tue Sep 06 09:17:09 ICT 2016
*
* Copyright (C) 2013 by Viettel Network Company. All rights reserved
*/
package com.viettel.it.persistence;

import com.viettel.it.model.ProcedureGNOC;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;


@Scope("session")
@Service(value = "ProcedureGNOCService")
public class ProcedureGNOCServiceImpl extends GenericDaoImplNewV2<ProcedureGNOC, Long> implements GenericDaoServiceNewV2 <ProcedureGNOC, Long>, Serializable{
	private static final long serialVersionUID = -4109611148855610L;

}