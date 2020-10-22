package com.viettel.persistence;

import com.viettel.exception.AppException;
import com.viettel.exception.SysException;
import com.viettel.model.Checklist;

import java.util.List;

public interface RstKpiService  extends GenericDao<Checklist, Long> {
	
	public Checklist getKpiByCode(String kpiCode) throws AppException, SysException;
	public List<Checklist> getListByType(Integer type) throws AppException, SysException;
	public List<Long> getKpiByCode(List<String> kpiCodes) throws AppException, SysException;


}
