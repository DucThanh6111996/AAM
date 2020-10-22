package com.viettel.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.KpiDbSetting;

import java.util.List;

public interface RstKpiDbSettingService extends GenericDao<KpiDbSetting, Long> {
	
	public List<KpiDbSetting> getlistByGroup(Long appGroupId) throws AppException, SysException;

	public KpiDbSetting findbyKpiId(Long kpiId, Long appDbId) throws AppException, SysException;

	public List<KpiDbSetting> findByIds(List<Long> ids) throws com.viettel.exception.AppException, com.viettel.exception.SysException;

}
