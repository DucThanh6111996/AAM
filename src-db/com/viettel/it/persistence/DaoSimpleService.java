package com.viettel.it.persistence;

import java.io.Serializable;

/**
 * @author Nguyễn Xuân Huy <huynx6@viettel.com.vn>
 * @sin Nov 22, 2016
 * @version 1.0
 */
public class DaoSimpleService extends GenericDaoImplNewV2<Object, Serializable> {

    private static final long serialVersionUID = 1L;

    public DaoSimpleService() {
        super(false);
    }
}
