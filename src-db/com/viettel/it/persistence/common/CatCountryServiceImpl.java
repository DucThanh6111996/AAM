/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.viettel.it.persistence.common;

import com.viettel.it.persistence.GenericDaoImplNewV2;
import com.viettel.model.CatCountryBO;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 *
 * @author hanv15
 */
@Scope("session")
@Service(value = "catCountryService")
public class CatCountryServiceImpl extends GenericDaoImplNewV2<CatCountryBO, String> implements Serializable {

    private static final long serialVersionUID = -4109611148855610L;

}
