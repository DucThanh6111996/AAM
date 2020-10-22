package com.viettel.persistence;

// Created May 9, 2016 9:09:43 AM by quanns2

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.UnitIbm;

import java.io.Serializable;

/**
 * Service interface for domain model class UnitIbm.
 * @see UnitIbm
 * @author quanns2
 */

public interface UnitIbmService extends GenericDao<UnitIbm, Serializable> {
    public UnitIbm findOwner(Long iimAppGroupId) throws AppException, SysException;
}
