package com.viettel.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.Test;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Service;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

@Service(value = "testService")
@Scope("session")
public class TestServiceImpl extends GenericDaoImpl<Test, Serializable>  implements  TestService {

    private static Logger logger = LogManager.getLogger(TestServiceImpl.class);

}
