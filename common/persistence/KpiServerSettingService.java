package com.viettel.persistence;

// Created May 5, 2016 4:56:37 PM by quanns2

import com.viettel.controller.Module;
import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.KpiServerSetting;

import java.io.Serializable;
import java.util.List;

/**
 * Service interface for domain model class KpiServerSetting.
 * @see KpiServerSetting
 * @author quanns2
 */

public interface KpiServerSettingService extends GenericDao<KpiServerSetting, Serializable> {
    public List<KpiServerSetting> findSettingForModules(List<Long> moduleIds) throws AppException, SysException;

    public List<KpiServerSetting> findAomSettingForModules(List<Module> modules) throws AppException, SysException;
}