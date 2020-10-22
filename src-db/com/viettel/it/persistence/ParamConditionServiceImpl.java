package com.viettel.it.persistence;

import com.viettel.it.model.ParamCondition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;

/**
 * Created by VTN-PTPM-NV56 on 4/8/2019.
 */
@Scope("session")
@Service(value = "paramConditionService")
public class ParamConditionServiceImpl extends GenericDaoImplNewV2<ParamCondition, Long> implements Serializable {

}
