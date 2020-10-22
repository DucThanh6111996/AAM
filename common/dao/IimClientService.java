package com.viettel.dao;

import com.viettel.bean.*;
import com.viettel.exception.AppException;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by quanns2 on 4/10/17.
 */
public interface IimClientService {
    public Module findModuleById(String nationCode, Long id) throws AppException;

    public Module findModuleByCode(String nationCode, String moduleCode) throws AppException;

    public List<Module> findModuleByCodes(String nationCode, List<String> moduleCodes) throws AppException;

    public List<Module> findModulesByIds(String nationCode, List<Long> ids) throws AppException;

    public List<Module> findFilterModule(String nationCode, int first, int pageSize, Map<String, Object> filters, Collection<Long> moduleIds, List<Long> unitId) throws AppException;

    public Integer countFilterModule(String nationCode, Map<String, Object> filters, Collection<Long> moduleIds, List<Long> unitId) throws AppException;

    public ServiceDatabase findServiceDbById(String nationCode, Long id) throws AppException;

    public List<ServiceDatabase> findServiceDbsByServices(String nationCode, List<Long> ids) throws AppException;

    public List<String> findAllIpByServices(String nationCode, List<String> serviceCodes) throws AppException;

    public List<Service> findServicesByModules(String nationCode, List<Long> moduleIds) throws AppException;

    public LogOs findLogByModule(String nationCode, Long moduleId, String logType) throws AppException;

    public Unit findUnit(String nationCode, Long unitId) throws AppException;

    public List<Service> findServiceByUser(String nationCode, String username) throws AppException;

    public List<Service> findService(String nationCode, Long unitId) throws AppException;

    public Service findServiceById(String nationCode, Long serviceId) throws AppException;

    public List<Unit> findChildrenUnit(String nationCode, Long unitId) throws AppException;

    public List<Database> findDatabases(String nationCode, Long unitId) throws AppException;

    public List<String> findIps(String nationCode, Long unitId) throws AppException;

    public List<LogOs> findMdPath(String nationCode, String mdPath, Long moduleId) throws AppException;

    public List<Module> findModules(String nationCode, List<Long> serviceIds, List<Long> databaseIds, List<String> ipServers, Long kbType) throws AppException;

    public List<MdDependent> findMdDependent(String nationCode, List<Long> moduleIds, String dependentCode) throws AppException;

    public List<Long> findOfflineModuleIds(String nationCode, List<Integer> groupModuleIds, String functionType) throws AppException;

    public List<Long> findOfflineModuleIds(String nationCode, List<Integer> groupModuleIds) throws AppException;

    public List<ModuleDbDr> findModuleDbDr(String nationCode, List<Long> moduleIds, List<Long> dbIds) throws AppException;

    public List<OsAccount> findOsAccount(String nationCode, String ipServer) throws AppException;

    public List<ActionSpecial> findActionSpecial(String nationCode, List<Long> moduleIds) throws AppException;

    public List<MonitorDatabase> findDbMonitor(String nationCode, Long unitId, List<Long> dbIds) throws AppException;

    public List<Service> findFilterModule(String nationCode,String filter, Long unitId, String ip, List<Long> dbs) throws AppException;

    public List<AccountForAppDTO> findAccountCachePass(String countryCode) throws AppException;

    public List<Service> findServices(String nationCode, Long unitId) throws AppException;
}
