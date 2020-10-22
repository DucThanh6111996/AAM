package com.viettel.controller;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.io.Serializable;

/**
 * Created by quanns2 on 4/7/17.
 */
public class Module implements Serializable {
    private static Logger logger = LogManager.getLogger(Module.class);
    
    private Long serviceId;
    private String serviceCode;
    private String serviceName;
    private Long moduleId;
    private String moduleCode;
    private String moduleName;
    private Integer moduleStatus;
    private Integer moduleType;
    private String moduleTypeCode;
    private String moduleTypeName;
    private Integer backupStatus;
    private String executePath;
    private String logPath;
    private String logFileName;
    private Long serviceUserId;
    private String startService;
    private String restartService;
    private String stopService;
    private String viewStatus;
    private String deleteCache;
    private Long userManager;
    private String managerName;
    private Long unitId;
    private String ipServer;
    private Integer osType;
    private String osName;
    private String username;
    private String password;
    private Integer groupModuleId;
    private String groupModuleName;
    private String groupModuleCode;
    private String functionCode;

    private Integer actionType;//them
    private Integer kbGroup;
    private Integer testbedMode;

    private String logStartPath;
    private String logStartFileName;

    private String startSuccessKey;
    private String statusSuccessKey;

    private String restartSuccessKey;
    private String stopSuccessKey;
    private String deleteCacheSuccessKey;

    private String startFailKey;
    private String statusFailKey;
    private String restartFailKey;
    private String stopFailKey;
    private String deleteCacheFailKey;

    private String startOutput;
    private String statusOutput;
    private String restartOutput;
    private String stopOutput;
    private String deleteCacheOutput;
    private String countryCode;

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    // 0: module (Black text), 1: exclusion (Red text)
    private String typeModule;

    public Long getServiceId() {
        return serviceId;
    }

    public void setServiceId(Long serviceId) {
        this.serviceId = serviceId;
    }

    public String getServiceCode() {
        return serviceCode;
    }

    public void setServiceCode(String serviceCode) {
        this.serviceCode = serviceCode;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public Long getModuleId() {
        return moduleId;
    }

    public void setModuleId(Long moduleId) {
        this.moduleId = moduleId;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

    public Integer getModuleStatus() {
        return moduleStatus;
    }

    public void setModuleStatus(Integer moduleStatus) {
        this.moduleStatus = moduleStatus;
    }

    public Integer getModuleType() {
        return moduleType;
    }

    public void setModuleType(Integer moduleType) {
        this.moduleType = moduleType;
    }

    public String getModuleTypeCode() {
        return moduleTypeCode;
    }

    public void setModuleTypeCode(String moduleTypeCode) {
        this.moduleTypeCode = moduleTypeCode;
    }

    public String getModuleTypeName() {
        return moduleTypeName;
    }

    public void setModuleTypeName(String moduleTypeName) {
        this.moduleTypeName = moduleTypeName;
    }

    public Integer getBackupStatus() {
        return backupStatus;
    }

    public void setBackupStatus(Integer backupStatus) {
        this.backupStatus = backupStatus;
    }

    public String getExecutePath() {
        return executePath;
    }

    public void setExecutePath(String executePath) {
        this.executePath = executePath;
    }

    public String getLogPath() {
        return logPath;
    }

    @JsonIgnore
    public String getFullLogPath() {
        if (StringUtils.isEmpty(logPath) || StringUtils.isEmpty(logFileName)) {
            return null;
        } else {
            String separator = osType == AamConstants.OS_TYPE.WINDOWS ? "\\" : "/";
            return logPath + (logPath.endsWith(separator) ? "" : separator) + logFileName;
        }
    }

    @JsonIgnore
    public String getFullLogStartPath() {
        if (StringUtils.isEmpty(logStartPath) || StringUtils.isEmpty(logStartFileName)) {
            return null;
        } else {
            String separator = osType == AamConstants.OS_TYPE.WINDOWS ? "\\" : "/";
            return logStartPath + (logStartPath.endsWith(separator) ? "" : separator) + logStartFileName;
        }
    }

    public void setLogPath(String logPath) {
        this.logPath = logPath;
    }

    public String getLogFileName() {
        return logFileName;
    }

    public void setLogFileName(String logFileName) {
        this.logFileName = logFileName;
    }

    public Long getServiceUserId() {
        return serviceUserId;
    }

    public void setServiceUserId(Long serviceUserId) {
        this.serviceUserId = serviceUserId;
    }

    public String getStartService() {
        return startService;
    }

    public void setStartService(String startService) {
        this.startService = startService;
    }

    public String getRestartService() {
        return restartService;
    }

    public void setRestartService(String restartService) {
        this.restartService = restartService;
    }

    public String getStopService() {
        return stopService;
    }

    public void setStopService(String stopService) {
        this.stopService = stopService;
    }

    public String getViewStatus() {
        return viewStatus;
    }

    public void setViewStatus(String viewStatus) {
        this.viewStatus = viewStatus;
    }

    public Long getUserManager() {
        return userManager;
    }

    public void setUserManager(Long userManager) {
        this.userManager = userManager;
    }

    public String getManagerName() {
        return managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public Long getUnitId() {
        return unitId;
    }

    public void setUnitId(Long unitId) {
        this.unitId = unitId;
    }

    public String getDeleteCache() {
        return deleteCache;
    }

    public void setDeleteCache(String deleteCache) {
        this.deleteCache = deleteCache;
    }

    public Integer getActionType() {
        return actionType;
    }

    public void setActionType(Integer actionType) {
        this.actionType = actionType;
    }

    public String getIpServer() {
        return ipServer;
    }

    public void setIpServer(String ipServer) {
        this.ipServer = ipServer;
    }

    public Integer getOsType() {
        return osType;
    }

    public void setOsType(Integer osType) {
        this.osType = osType;
    }

    public String getOsName() {
        return osName;
    }

    public void setOsName(String osName) {
        this.osName = osName;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Integer getGroupModuleId() {
        return groupModuleId;
    }

    public void setGroupModuleId(Integer groupModuleId) {
        this.groupModuleId = groupModuleId;
    }

    public String getGroupModuleName() {
        return groupModuleName;
    }

    public void setGroupModuleName(String groupModuleName) {
        this.groupModuleName = groupModuleName;
    }

    public String getGroupModuleCode() {
        return groupModuleCode;
    }

    public void setGroupModuleCode(String groupModuleCode) {
        this.groupModuleCode = groupModuleCode;
    }

    public Integer getKbGroup() {
        return kbGroup == null ? 1 : kbGroup;
    }

    public void setKbGroup(Integer kbGroup) {
        this.kbGroup = kbGroup;
    }

    public Integer getTestbedMode() {
        return testbedMode;
    }

    public void setTestbedMode(Integer testbedMode) {
        this.testbedMode = testbedMode;
    }

    public String getFunctionCode() {
        return functionCode;
    }

    public void setFunctionCode(String functionCode) {
        this.functionCode = functionCode;
    }

    public String getLogStartPath() {
        return logStartPath;
    }

    public void setLogStartPath(String logStartPath) {
        this.logStartPath = logStartPath;
    }

    public String getLogStartFileName() {
        return logStartFileName;
    }

    public void setLogStartFileName(String logStartFileName) {
        this.logStartFileName = logStartFileName;
    }

    public String getStartSuccessKey() {
        return startSuccessKey;
    }

    public void setStartSuccessKey(String startSuccessKey) {
        this.startSuccessKey = startSuccessKey;
    }

    public String getStatusSuccessKey() {
        return statusSuccessKey;
    }

    public void setStatusSuccessKey(String statusSuccessKey) {
        this.statusSuccessKey = statusSuccessKey;
    }

    public String getRestartSuccessKey() {
        return restartSuccessKey;
    }

    public void setRestartSuccessKey(String restartSuccessKey) {
        this.restartSuccessKey = restartSuccessKey;
    }

    public String getStopSuccessKey() {
        return stopSuccessKey;
    }

    public void setStopSuccessKey(String stopSuccessKey) {
        this.stopSuccessKey = stopSuccessKey;
    }

    public String getDeleteCacheSuccessKey() {
        return deleteCacheSuccessKey;
    }

    public void setDeleteCacheSuccessKey(String deleteCacheSuccessKey) {
        this.deleteCacheSuccessKey = deleteCacheSuccessKey;
    }

    public String getStartFailKey() {
        return startFailKey;
    }

    public void setStartFailKey(String startFailKey) {
        this.startFailKey = startFailKey;
    }

    public String getStatusFailKey() {
        return statusFailKey;
    }

    public void setStatusFailKey(String statusFailKey) {
        this.statusFailKey = statusFailKey;
    }

    public String getRestartFailKey() {
        return restartFailKey;
    }

    public void setRestartFailKey(String restartFailKey) {
        this.restartFailKey = restartFailKey;
    }

    public String getStopFailKey() {
        return stopFailKey;
    }

    public void setStopFailKey(String stopFailKey) {
        this.stopFailKey = stopFailKey;
    }

    public String getDeleteCacheFailKey() {
        return deleteCacheFailKey;
    }

    public void setDeleteCacheFailKey(String deleteCacheFailKey) {
        this.deleteCacheFailKey = deleteCacheFailKey;
    }

    public String getStartOutput() {
        return startOutput;
    }

    public void setStartOutput(String startOutput) {
        this.startOutput = startOutput;
    }

    public String getStatusOutput() {
        return statusOutput;
    }

    public void setStatusOutput(String statusOutput) {
        this.statusOutput = statusOutput;
    }

    public String getRestartOutput() {
        return restartOutput;
    }

    public void setRestartOutput(String restartOutput) {
        this.restartOutput = restartOutput;
    }

    public String getStopOutput() {
        return stopOutput;
    }

    public void setStopOutput(String stopOutput) {
        this.stopOutput = stopOutput;
    }

    public String getDeleteCacheOutput() {
        return deleteCacheOutput;
    }

    public void setDeleteCacheOutput(String deleteCacheOutput) {
        this.deleteCacheOutput = deleteCacheOutput;
    }

    public String getTypeModule() {
        return typeModule;
    }

    public void setTypeModule(String typeModule) {
        this.typeModule = typeModule;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Module module = (Module) o;

        return new EqualsBuilder()
                .append(moduleId, module.moduleId)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(moduleId)
                .toHashCode();
    }

    @Override
    public String toString() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(this);
        } catch (JsonProcessingException e) {
            logger.error(e.getMessage(), e);
        }

        return null;
    }
}
