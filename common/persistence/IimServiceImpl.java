package com.viettel.persistence;

import com.viettel.controller.IimClientServiceImpl;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;

/**
 * Created by quanns2 on 4/10/17.
 */
@org.springframework.stereotype.Service(value = "iimService")
@Scope("session")
public class IimServiceImpl extends IimClientServiceImpl implements IimService {
    private static Logger logger = LogManager.getLogger(IimServiceImpl.class);
}
