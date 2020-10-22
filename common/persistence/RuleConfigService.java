package com.viettel.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.KpiServerSetting;
import com.viettel.model.RuleConfig;

import java.io.Serializable;
import java.util.List;

/**
 * Service interface for domain model class RuleConfig.
 * @see RuleConfig
 * @author quanns2
 */

public interface RuleConfigService extends GenericDao<RuleConfig, Serializable> {
    public List<RuleConfig> findByActionDetailAppIds(List<Long> actionDetailAppIds) throws AppException, SysException;
    public List<RuleConfig> findByActionId(Long actionId) throws AppException, SysException;
}