package com.viettel.model;

// Created May 5, 2016 4:56:36 PM by quanns2

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import javax.persistence.*;

/**
 * @author quanns2
 */
@Entity
@Table(name = "ACTION_MODULE")
public class ActionModule implements java.io.Serializable {

	private Long id;
	private Long actionId;
	private Long moduleId;
	private Integer actionType;

	private String ipServer;
	private String startService;
	private String stopService;
	private String restartService;
	private String viewStatus;
	private String deleteCache;
	private String installedUser;
	private String path;
	private String appCode;
	private String appTypeCode;

	private Integer osType;
	private String osName;
	private String logLink;
	private String keyword;
	private String keyStatusStart;
	private String keyStatusStop;

	private Integer kbGroup;
	private String functionCode;
	private Integer testbedMode;
	private String groupModuleName;
	private String groupModuleCode;
	private String moduleName;
	private String serviceName;

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

	public ActionModule() {
	}

	public ActionModule(Long id) {
		this.id = id;
	}

	public ActionModule(Long id, Long actionId, Long moduleId) {
		this.id = id;
		this.actionId = actionId;
		this.moduleId = moduleId;
	}

	@Id
	@Column(name = "ID", unique = true, nullable = false, precision = 22, scale = 0)
	/*@GeneratedValue(generator = "generator")
	@SequenceGenerator(name = "generator", sequenceName = "ACTION_MODULE_SEQ")*/
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "ID")
	@SequenceGenerator(name = "ID", sequenceName = "ACTION_MODULE_SEQ", allocationSize=1)
	public Long getId() {
		return this.id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	@Column(name = "ACTION_ID", precision = 22, scale = 0)
	public Long getActionId() {
		return this.actionId;
	}

	public void setActionId(Long actionId) {
		this.actionId = actionId;
	}

	@Column(name = "MODULE_ID", precision = 22, scale = 0)
	public Long getModuleId() {
		return this.moduleId;
	}

	public void setModuleId(Long moduleId) {
		this.moduleId = moduleId;
	}

	@Column(name = "ACTION_TYPE", precision = 10, scale = 0)
	public Integer getActionType() {
		return actionType;
	}

	public void setActionType(Integer actionType) {
		this.actionType = actionType;
	}

	@Column(name = "IP_SERVER", length = 50)
	public String getIpServer() {
		return this.ipServer;
	}

	public void setIpServer(String ipServer) {
		this.ipServer = ipServer;
	}

	@Column(name = "START_SERVICE", length = 250)
	public String getStartService() {
		return this.startService;
	}

	public void setStartService(String startService) {
		this.startService = startService;
	}

	@Column(name = "STOP_SERVICE", length = 250)
	public String getStopService() {
		return this.stopService;
	}

	public void setStopService(String stopService) {
		this.stopService = stopService;
	}

	@Column(name = "RESTART_SERVICE", length = 250)
	public String getRestartService() {
		return this.restartService;
	}

	public void setRestartService(String restartService) {
		this.restartService = restartService;
	}

	@Column(name = "VIEW_STATUS", length = 250)
	public String getViewStatus() {
		return this.viewStatus;
	}

	public void setViewStatus(String viewStatus) {
		this.viewStatus = viewStatus;
	}

	@Column(name = "DELETE_CACHE", length = 250)
	public String getDeleteCache() {
		return this.deleteCache;
	}

	public void setDeleteCache(String deleteCache) {
		this.deleteCache = deleteCache;
	}

	@Column(name = "INSTALLED_USER", length = 30)
	public String getInstalledUser() {
		return this.installedUser;
	}

	public void setInstalledUser(String installedUser) {
		this.installedUser = installedUser;
	}

	@Column(name = "PATH", length = 200)
	public String getPath() {
		return this.path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	@Column(name = "APP_CODE", length = 300)
	public String getAppCode() {
		return this.appCode;
	}

	public void setAppCode(String appCode) {
		this.appCode = appCode;
	}

	@Column(name = "APP_TYPE_CODE")
	public String getAppTypeCode() {
		return appTypeCode;
	}

	public void setAppTypeCode(String appTypeCode) {
		this.appTypeCode = appTypeCode;
	}

	@Column(name = "OS_TYPE")
	public Integer getOsType() {
		return osType;
	}

	public void setOsType(Integer osType) {
		this.osType = osType;
	}

	@Column(name = "OS_NAME")
	public String getOsName() {
		return osName;
	}

	public void setOsName(String osName) {
		this.osName = osName;
	}

	@Column(name = "LOG_LINK")
	public String getLogLink() {
		return logLink;
	}

	public void setLogLink(String logLink) {
		this.logLink = logLink;
	}

	@Column(name = "KEYWORD")
	public String getKeyword() {
		return keyword;
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	@Column(name = "KEY_STATUS_START")
	public String getKeyStatusStart() {
		return keyStatusStart;
	}

	public void setKeyStatusStart(String keyStatusStart) {
		this.keyStatusStart = keyStatusStart;
	}

	@Column(name = "KEY_STATUS_STOP")
	public String getKeyStatusStop() {
		return keyStatusStop;
	}

	public void setKeyStatusStop(String keyStatusStop) {
		this.keyStatusStop = keyStatusStop;
	}

	@Column(name = "KB_GROUP")
	public Integer getKbGroup() {
		return kbGroup;
	}

	public void setKbGroup(Integer kbGroup) {
		this.kbGroup = kbGroup;
	}

	@Column(name = "FUNCTION_CODE")
	public String getFunctionCode() {
		return functionCode;
	}

	public void setFunctionCode(String functionCode) {
		this.functionCode = functionCode;
	}

	@Column(name = "TESTBED_MODE")
	public Integer getTestbedMode() {
		return testbedMode;
	}

	public void setTestbedMode(Integer testbedMode) {
		this.testbedMode = testbedMode;
	}

	@Column(name = "GROUP_MODULE_NAME")
	public String getGroupModuleName() {
		return groupModuleName;
	}

	public void setGroupModuleName(String groupModuleName) {
		this.groupModuleName = groupModuleName;
	}

	@Column(name = "GROUP_MODULE_CODE")
	public String getGroupModuleCode() {
		return groupModuleCode;
	}

	public void setGroupModuleCode(String groupModuleCode) {
		this.groupModuleCode = groupModuleCode;
	}

    @Column(name = "MODULE_NAME")
    public String getModuleName() {
        return moduleName;
    }

    public void setModuleName(String moduleName) {
        this.moduleName = moduleName;
    }

	@Column(name = "SERVICE_NAME")
	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	@Column(name = "RESTART_SUCCESS_KEY")
	public String getRestartSuccessKey() {
		return restartSuccessKey;
	}

	public void setRestartSuccessKey(String restartSuccessKey) {
		this.restartSuccessKey = restartSuccessKey;
	}

	@Column(name = "STOP_SUCCESS_KEY")
	public String getStopSuccessKey() {
		return stopSuccessKey;
	}

	public void setStopSuccessKey(String stopSuccessKey) {
		this.stopSuccessKey = stopSuccessKey;
	}

	@Column(name = "DELETE_CACHE_SUCCESS_KEY")
	public String getDeleteCacheSuccessKey() {
		return deleteCacheSuccessKey;
	}

	public void setDeleteCacheSuccessKey(String deleteCacheSuccessKey) {
		this.deleteCacheSuccessKey = deleteCacheSuccessKey;
	}

	@Column(name = "START_FAIL_KEY")
	public String getStartFailKey() {
		return startFailKey;
	}

	public void setStartFailKey(String startFailKey) {
		this.startFailKey = startFailKey;
	}

	@Column(name = "STATUS_FAIL_KEY")
	public String getStatusFailKey() {
		return statusFailKey;
	}

	public void setStatusFailKey(String statusFailKey) {
		this.statusFailKey = statusFailKey;
	}

	@Column(name = "RESTART_FAIL_KEY")
	public String getRestartFailKey() {
		return restartFailKey;
	}

	public void setRestartFailKey(String restartFailKey) {
		this.restartFailKey = restartFailKey;
	}

	@Column(name = "STOP_FAIL_KEY")
	public String getStopFailKey() {
		return stopFailKey;
	}

	public void setStopFailKey(String stopFailKey) {
		this.stopFailKey = stopFailKey;
	}

	@Column(name = "DELETE_CACHE_FAIL_KEY")
	public String getDeleteCacheFailKey() {
		return deleteCacheFailKey;
	}

	public void setDeleteCacheFailKey(String deleteCacheFailKey) {
		this.deleteCacheFailKey = deleteCacheFailKey;
	}

	@Column(name = "START_OUTPUT")
	public String getStartOutput() {
		return startOutput;
	}

	public void setStartOutput(String startOutput) {
		this.startOutput = startOutput;
	}

	@Column(name = "STATUS_OUTPUT")
	public String getStatusOutput() {
		return statusOutput;
	}

	public void setStatusOutput(String statusOutput) {
		this.statusOutput = statusOutput;
	}

	@Column(name = "RESTART_OUTPUT")
	public String getRestartOutput() {
		return restartOutput;
	}

	public void setRestartOutput(String restartOutput) {
		this.restartOutput = restartOutput;
	}

	@Column(name = "STOP_OUTPUT")
	public String getStopOutput() {
		return stopOutput;
	}

	public void setStopOutput(String stopOutput) {
		this.stopOutput = stopOutput;
	}

	@Column(name = "DELETE_CACHE_OUTPUT")
	public String getDeleteCacheOutput() {
		return deleteCacheOutput;
	}

	public void setDeleteCacheOutput(String deleteCacheOutput) {
		this.deleteCacheOutput = deleteCacheOutput;
	}

	@Override
	public boolean equals(Object o) {
		if (this == o) return true;

		if (o == null || getClass() != o.getClass()) return false;

		ActionModule that = (ActionModule) o;

		return new EqualsBuilder()
				.append(actionId, that.actionId)
				.append(moduleId, that.moduleId)
				.isEquals();
	}

	@Override
	public int hashCode() {
		return new HashCodeBuilder(17, 37)
				.append(actionId)
				.append(moduleId)
				.toHashCode();
	}
}
