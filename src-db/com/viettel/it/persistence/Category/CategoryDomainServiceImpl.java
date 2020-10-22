package com.viettel.it.persistence.Category;

import com.viettel.it.model.CategoryDomain;
import com.viettel.it.persistence.GenericDaoImplNewV2;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Created by quytv7 on 1/17/2018.
 */
@Scope("session")
@Service(value = "categoryDomainService")
public class CategoryDomainServiceImpl extends GenericDaoImplNewV2<CategoryDomain, Long> implements Serializable {
    private static final long serialVersionUID = -4109611148855610L;
}
