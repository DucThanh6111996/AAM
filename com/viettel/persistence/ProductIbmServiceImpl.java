package com.viettel.persistence;

// Created May 9, 2016 9:09:43 AM by quanns2

import com.viettel.model.ProductIbm;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Service implement for interface TableSqlService.
 * @see ProductIbmService
 * @author quanns2
 */

@Service(value = "productIbmService")
@Scope("session")
public class ProductIbmServiceImpl extends GenericDaoImpl<ProductIbm, Serializable> implements ProductIbmService,
		Serializable {
	private static Logger logger = LogManager.getLogger(ProductIbmServiceImpl.class);
	
	
}
