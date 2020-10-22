package com.viettel.it.persistence;

import com.viettel.it.model.TemplateGroup;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Created by hanh on 4/14/2017.
 */
@Scope("session")
@Service(value = "templateGroupService")
public class TemplateGroupServiceImpl extends GenericDaoImplNewV2<TemplateGroup, Long>
        implements GenericDaoServiceNewV2<TemplateGroup, Long>, Serializable {
}
