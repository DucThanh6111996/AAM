package com.viettel.dao;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;
import com.viettel.bean.ParameterBO;
import com.viettel.bean.RequestInputBO;
import com.viettel.bean.ServiceDatabase;
import com.viettel.controller.*;
import com.viettel.exception.AppException;

import com.viettel.jackson.AamJsonFactory;
import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

/**
 * @author quanns2
 */
public class IimClientServiceImpl implements IimClientService {
    private static Logger logger = LogManager.getLogger(com.viettel.controller.IimClientServiceImpl.class);

    @Override
    public Module findModuleById(String nationCode, Long id) throws AppException {
        Module module = null;
        for(int i= 0; i< 3; i++) {
            try {
                List<ParameterBO> parameterBOS = new ArrayList<>();
                parameterBOS.add(new ParameterBO(null, "modules", ",", null, String.valueOf(id)));

                RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_MODULE_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
                IimServices_PortType iimServices_portType = IimClientFactory.create();
                JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

                ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
                JsonNode node = preMapper.readTree(jsonData.getDataJson());
                String data = node.get("data").toString();

                ObjectMapper objectMapper = new ObjectMapper();
                objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
                List<Module> modules = objectMapper.readValue(data, new TypeReference<List<Module>>() {
                });

                if (modules != null && !modules.isEmpty()) {
                    module = modules.get(0);
                }
                break;
            } catch (Exception e) {
                logger.error(e.getMessage(), e);
                TextUtils.checkAndPrintObject(logger, "findModuleById", "nationCode", nationCode,
                        "id", id,
                        "result", module, "Retry lai do Fail lan:" + i);
//            throw new AppException(e.getMessage());
            }
        }
        TextUtils.checkAndPrintObject(logger, "findModuleById", "nationCode", nationCode,
                "id", id,
                "result", module);
        return module;
    }

    @Override
    public Module findModuleByCode(String nationCode, String moduleCode) throws AppException {
        Module module = null;
        List<Module> modules = findModuleByCodes(nationCode, Arrays.asList(moduleCode));

        if (modules != null && !modules.isEmpty())
            module = modules.get(0);
        TextUtils.checkAndPrintObject(logger, "findModuleByCode", "nationCode", nationCode,
                "moduleCode", moduleCode,
                "result", module);
        return module;
    }

    @Override
    public List<Module> findModuleByCodes(String nationCode, List<String> moduleCodes) throws AppException {
        List<Module> modules = null;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "moduleCodes", ",", null, Joiner.on(",").join(moduleCodes)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_MODULE_BY_CODE_AAM_V3, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            modules = objectMapper.readValue(data, new TypeReference<List<Module>>() {
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            TextUtils.checkAndPrintObject(logger, "findModuleByCodes", "nationCode", nationCode,
                    "moduleCodes", moduleCodes,
                    "result", moduleCodes);
            throw new AppException(e.getMessage());
        }
        TextUtils.checkAndPrintObject(logger, "findModuleByCodes", "nationCode", nationCode,
                "moduleCodes", moduleCodes,
                "result", moduleCodes);
        return modules;
    }

    @Override
    public List<Module> findModulesByIds(String nationCode, List<Long> ids) throws AppException {
        if (ids.isEmpty()) {
            return new ArrayList<>();
        }
        List<Module> modules = null;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "modules", ",", null, Joiner.on(",").join(ids)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_MODULE_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            modules = objectMapper.readValue(data, new TypeReference<List<Module>>() {
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            TextUtils.checkAndPrintObject(logger, "findModulesByIds", "nationCode", nationCode,
                    "ids", ids,
                    "result", modules);
            throw new AppException(e.getMessage());
        }
        TextUtils.checkAndPrintObject(logger, "findModulesByIds", "nationCode", nationCode,
                "ids", ids,
                "result", modules);

        return modules;
    }

    @Override
    public List<Module> findFilterModule(String nationCode, int first, int pageSize, Map<String, Object> filters, Collection<Long> moduleIds, List<Long> unitId) throws AppException {
        List<Module> modules = null;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            if (moduleIds == null || moduleIds.isEmpty())
                parameterBOS.add(new ParameterBO(null, "moduleId", ",", "LIST", "-1"));
            else
                parameterBOS.add(new ParameterBO(null, "moduleId", ",", "LIST", Joiner.on(",").join(moduleIds)));

            parameterBOS.add(new ParameterBO(null, "unitId", ",", "LIST", Joiner.on(",").join(unitId)));
            parameterBOS.add(new ParameterBO(null, "first", ",", null, String.valueOf(first)));
            parameterBOS.add(new ParameterBO(null, "last", ",", null, String.valueOf(first + pageSize)));
            List<String> fields = Arrays.asList("path", "username", "serviceName", "moduleName", "ipServer", "moduleCode", "serviceCode", "moduleTypeName", "groupModuleName","moduleStatus");
            for (String field : fields) {
                String filterValue = filters.get(field) == null ? "" : String.valueOf(filters.get(field)).toLowerCase();
                if (field.equalsIgnoreCase("moduleStatus")) {
                    if (filterValue == null || filterValue.equals("")) {
                        parameterBOS.add(new ParameterBO(null, field, ",", "NUMBER",  "0,1"));
                    } else {
                        parameterBOS.add(new ParameterBO(null, field, ",", "NUMBER", filterValue ));
                    }
                    continue;
                }
                parameterBOS.add(new ParameterBO(null, field, ",", null, "%" + filterValue + "%"));
            }
            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_FILTER_MODULE_AAM_NEW, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            modules = objectMapper.readValue(data, new TypeReference<List<Module>>() {
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            TextUtils.checkAndPrintObject(logger, "findFilterModule", "nationCode", nationCode,
                    "first", first,
                    "pageSize", pageSize,
                    "filters", filters,
                    "moduleIds", moduleIds,
                    "unitId", unitId,
                    "result", modules);
            throw new AppException(e.getMessage());
        }
        TextUtils.checkAndPrintObject(logger, "findFilterModule", "nationCode", nationCode,
                "first", first,
                "pageSize", pageSize,
                "filters", filters,
                "moduleIds", moduleIds,
                "unitId", unitId,
                "result", modules);
        return modules;
    }

    @Override
    public Integer countFilterModule(String nationCode, Map<String, Object> filters, Collection<Long> moduleIds, List<Long> unitId) throws AppException {
        Integer count = 0;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            if (moduleIds == null || moduleIds.isEmpty())
                parameterBOS.add(new ParameterBO(null, "moduleId", ",", "LIST", "-1"));
            else
                parameterBOS.add(new ParameterBO(null, "moduleId", ",", "LIST", Joiner.on(",").join(moduleIds)));
            parameterBOS.add(new ParameterBO(null, "unitId", ",", "LIST", Joiner.on(",").join(unitId)));
            List<String> fields = Arrays.asList("path", "username", "serviceName", "moduleName", "ipServer", "moduleCode", "serviceCode", "moduleTypeName", "groupModuleName");
            for (String field : fields) {
                String filterValue = filters.get(field) == null ? "" : String.valueOf(filters.get(field)).toLowerCase();
                parameterBOS.add(new ParameterBO(null, field, ",", null, "%" + filterValue + "%"));
            }

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.COUNT_FILTER_MODULE_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<Map<String, Object>> objects = objectMapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {
            });

            if (objects != null && !objects.isEmpty())
                count = (Integer) objects.get(0).get("count");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            TextUtils.checkAndPrintObject(logger, "countFilterModule", "nationCode", nationCode,
                    "filters", filters,
                    "moduleIds", moduleIds,
                    "unitId", unitId,
                    "result", count);
            throw new AppException(e.getMessage());
        }
        TextUtils.checkAndPrintObject(logger, "countFilterModule", "nationCode", nationCode,
                "filters", filters,
                "moduleIds", moduleIds,
                "unitId", unitId,
                "result", count);

        return count;
    }

    @Override
    public ServiceDatabase findServiceDbById(String nationCode, Long id) throws AppException {
        ServiceDatabase database = null;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "servicesDbId", ",", null, String.valueOf(id)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_SERVICES_DB_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<ServiceDatabase> databases = objectMapper.readValue(data, new TypeReference<List<ServiceDatabase>>() {
            });

            if (databases != null && !databases.isEmpty())
                database = databases.get(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            TextUtils.checkAndPrintObject(logger, "findServiceDbById", "nationCode", nationCode,
                    "id", id,
                    "result", database);
            throw new AppException(e.getMessage());
        }
        TextUtils.checkAndPrintObject(logger, "findServiceDbById", "nationCode", nationCode,
                "id", id,
                "result", database);
        return database;
    }

    @Override
    public List<ServiceDatabase> findServiceDbsByServices(String nationCode, List<Long> ids) throws AppException {
        List<ServiceDatabase> databases = null;
        try {
            if (ids == null || ids.isEmpty())
                return new ArrayList<>();

            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "services", ",", null, Joiner.on(",").join(ids)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_SERVICES_DB_SERVICES_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            databases = objectMapper.readValue(data, new TypeReference<List<ServiceDatabase>>() {
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            TextUtils.checkAndPrintObject(logger, "findServiceDbsByServices", "nationCode", nationCode,
                    "ids", ids,
                    "result", databases);
            throw new AppException(e.getMessage());
        }
        TextUtils.checkAndPrintObject(logger, "findServiceDbsByServices", "nationCode", nationCode,
                "ids", ids,
                "result", databases);
        return databases;
    }

    @Override
    public List<String> findAllIpByServices(String nationCode, List<String> serviceCodes) throws AppException {
        if (serviceCodes == null)
            return new ArrayList<>();
        List<String> ipServers = new ArrayList<>();
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "serviceCodes", ",", null, Joiner.on(",").join(serviceCodes)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_IP_SERVICE_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper();
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<Map<String, Object>> objects = objectMapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {
            });

            for (Map<String, Object> object : objects) {
                ipServers.add((String) object.get("IP_SERVER"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            TextUtils.checkAndPrintObject(logger, "findAllIpByServices", "nationCode", nationCode,
                    "serviceCodes", serviceCodes,
                    "result", ipServers);
            throw new AppException(e.getMessage());
        }
        TextUtils.checkAndPrintObject(logger, "findAllIpByServices", "nationCode", nationCode,
                "serviceCodes", serviceCodes,
                "result", ipServers);

        return ipServers;
    }

    @Override
    public List<Service> findServicesByModules(String nationCode, List<Long> moduleIds) throws AppException {
        List<Service> services = null;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "moduleIds", ",", null, Joiner.on(",").join(moduleIds)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_SERVICES_BY_MODULES, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            services = objectMapper.readValue(data, new TypeReference<List<Service>>() {
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            TextUtils.checkAndPrintObject(logger, "findServicesByModules", "nationCode", nationCode,
                    "moduleIds", moduleIds,
                    "result", services);
            throw new AppException(e.getMessage());

        }

        TextUtils.checkAndPrintObject(logger, "findServicesByModules", "nationCode", nationCode,
                "moduleIds", moduleIds,
                "result", services);

        return services;
    }

    @Override
    public LogOs findLogByModule(String nationCode, Long moduleId, String logType) throws AppException {
        LogOs logOs = null;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "moduleId", ",", null, String.valueOf(moduleId)));
            parameterBOS.add(new ParameterBO(null, "logType", ",", null, logType));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_APP_LOG_OS_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<LogOs> logOsList = objectMapper.readValue(data, new TypeReference<List<LogOs>>() {
            });

            if (logOsList != null && !logOsList.isEmpty())
                logOs = logOsList.get(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return logOs;
    }

    @Override
    public Unit findUnit(String nationCode, Long unitId) throws AppException {
        Unit unit = null;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "unitId", ",", null, String.valueOf(unitId)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_UNIT_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<Unit> units = objectMapper.readValue(data, new TypeReference<List<Unit>>() {
            });

            if (units != null && !units.isEmpty())
                unit = units.get(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return unit;
    }

    @Override
    public List<Service> findServiceByUser(String nationCode, String username) throws AppException {
        List<Service> services;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "username", ",", null, username));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_SERVICE_BY_USER_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            services = objectMapper.readValue(data, new TypeReference<List<Service>>() {
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return services;
    }

    @Override
    public List<Service> findService(String nationCode, Long unitId) throws AppException {
        List<Service> services;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "unitId", null, null, String.valueOf(unitId)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_SERVICES_AAM_TEST, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            services = objectMapper.readValue(data, new TypeReference<List<Service>>() {
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return services;
    }

    @Override
    public Service findServiceById(String nationCode, Long serviceId) throws AppException {
        Service service = null;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "serviceId", ",", null, String.valueOf(serviceId)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_SERVICE_BY_ID_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            List<Service> services = objectMapper.readValue(data, new TypeReference<List<Service>>() {
            });

            if (services != null && !services.isEmpty())
                service = services.get(0);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return service;
    }

    @Override
    public List<Unit> findChildrenUnit(String nationCode, Long unitId) throws AppException {
        List<Unit> units;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "unitId", null, null, String.valueOf(unitId)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_CHILDREN_UNIT_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            units = objectMapper.readValue(data, new TypeReference<List<Unit>>() {
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return units;
    }

    @Override
    public List<Database> findDatabases(String nationCode, Long unitId) throws AppException {
        List<Database> databases;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "unitId", null, null, String.valueOf(unitId)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_DATABASES_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            databases = objectMapper.readValue(data, new TypeReference<List<Database>>() {
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return databases;
    }

    @Override
    public List<String> findIps(String nationCode, Long unitId) throws AppException {
        List<String> ips = new ArrayList<>();
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
//            parameterBOS.add(new ParameterBO(null, "unitId", null, null, String.valueOf(unitId)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_IPS_AAM_TEST, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

//            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            List<Map<String, Object>> mapObjects = objectMapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {
            });
            for (Map<String, Object> object : mapObjects) {
                ips.add((String) object.get("IP_SERVER"));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return ips;
    }

    @Override
    public List<LogOs> findMdPath(String nationCode, String mdPath, Long moduleId) throws AppException {
        List<LogOs> paths;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "mdPath", null, null, mdPath));
            parameterBOS.add(new ParameterBO(null, "moduleId", null, null, String.valueOf(moduleId)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_MODULE_PATH_AAM_V2, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            paths = objectMapper.readValue(data, new TypeReference<List<LogOs>>() {
            });
            /*List<Map<String, Object>> mapObjects = objectMapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {});
            for (Map<String, Object> object : mapObjects) {
                paths.add((String)object.get("PATH"));
            }*/
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return paths;
    }

    @Override
    public List<Module> findModules(String nationCode, List<Long> serviceIds, List<Long> databaseIds, List<String> ipServers, Long kbType) throws AppException {
        List<Module> modules = new ArrayList<>();

        if (serviceIds.isEmpty() && databaseIds.isEmpty() && ipServers.isEmpty())
            return modules;
        Set<Long> moduleIds = new HashSet<>();
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "serviceIds", ",", null, serviceIds.isEmpty() ? "-1" : Joiner.on(", ").join(serviceIds)));
            parameterBOS.add(new ParameterBO(null, "serviceId", null, null, String.valueOf(serviceIds.size())));
            parameterBOS.add(new ParameterBO(null, "ipServers", ",", null, ipServers.isEmpty() ? "-1" : Joiner.on(", ").join(ipServers)));
            parameterBOS.add(new ParameterBO(null, "ipServer", null, null, String.valueOf(ipServers.size())));
            parameterBOS.add(new ParameterBO(null, "dbIds", ",", null, databaseIds.isEmpty() ? "-1" : Joiner.on(", ").join(databaseIds)));
            parameterBOS.add(new ParameterBO(null, "dbId1", null, null, String.valueOf(databaseIds.size())));
            parameterBOS.add(new ParameterBO(null, "dbId2", null, null, String.valueOf(databaseIds.size())));
            parameterBOS.add(new ParameterBO(null, "kbType", null, null, String.valueOf(kbType)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_MODULE_UCTT_AAM_V2, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

//            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            List<Map<String, Object>> mapObjects = objectMapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {
            });
            for (Map<String, Object> object : mapObjects) {
                moduleIds.add(Long.valueOf((Integer) object.get("MODULE_ID")));
            }

            if (moduleIds.isEmpty())
                return modules;

            parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "modules", ",", null, Joiner.on(",").join(moduleIds)));
            request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_MODULE_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));

            jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            node = preMapper.readTree(jsonData.getDataJson());
            data = node.get("data").toString();

            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            modules = objectMapper.readValue(data, new TypeReference<List<Module>>() {
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return modules;
    }

    @Override
    public List<MdDependent> findMdDependent(String nationCode, List<Long> moduleIds, String dependentCode) throws AppException {
        List<MdDependent> mdDependents;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "mdCode", null, null, dependentCode));
            parameterBOS.add(new ParameterBO(null, "moduleIds", ",", null, Joiner.on(",").join(moduleIds)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_MD_DEPENDENT_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            mdDependents = objectMapper.readValue(data, new TypeReference<List<MdDependent>>() {
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return mdDependents;
    }

    @Override
    public List<Long> findOfflineModuleIds(String nationCode, List<Integer> groupModuleIds) throws AppException {
        return findOfflineModuleIds(nationCode, groupModuleIds, AamConstants.MODULE_FUNCTION_TYPE.BACKUP_OFFLINE);
    }

    @Override
    public List<Long> findOfflineModuleIds(String nationCode, List<Integer> groupModuleIds, String functionType) throws AppException {
        List<Long> moduleIds = new ArrayList<>();
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "functionCode", ",", null, functionType));
            parameterBOS.add(new ParameterBO(null, "groupModuleIds", ",", null, Joiner.on(",").join(groupModuleIds)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_MODULE_FUNC_GROUP_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

//            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            ObjectMapper objectMapper = new ObjectMapper();
            JsonNode node = objectMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            List<Map<String, Object>> mapObjects = objectMapper.readValue(data, new TypeReference<List<Map<String, Object>>>() {
            });
            for (Map<String, Object> object : mapObjects) {
                moduleIds.add(Long.valueOf((Integer) object.get("MODULE_ID")));
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return moduleIds;
    }

    @Override
    public List<ModuleDbDr> findModuleDbDr(String nationCode, List<Long> moduleIds, List<Long> dbIds) throws AppException {
        List<ModuleDbDr> moduleDbDrs;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "moduleIds", ",", null, Joiner.on(",").join(moduleIds)));
            parameterBOS.add(new ParameterBO(null, "dbIds", ",", null, Joiner.on(",").join(dbIds)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_MD_DEPENDENT_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            moduleDbDrs = objectMapper.readValue(data, new TypeReference<List<ModuleDbDr>>() {
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return moduleDbDrs;
    }

    @Override
    public List<OsAccount> findOsAccount(String nationCode, String ipServer) throws AppException {
        List<OsAccount> osAccounts;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "ipServer", ",", null, ipServer));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_OS_ACCOUNT_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            osAccounts = objectMapper.readValue(data, new TypeReference<List<OsAccount>>() {
            });

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return osAccounts;
    }

    @Override
    public List<ActionSpecial> findActionSpecial(String nationCode, List<Long> moduleIds) throws AppException {
        List<ActionSpecial> actionSpecials;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "moduleIds", ",", null, Joiner.on(",").join(moduleIds)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_MD_ACTION_SPECIAL_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            actionSpecials = objectMapper.readValue(data, new TypeReference<List<ActionSpecial>>() {
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return actionSpecials;
    }

    @Override
    public List<MonitorDatabase> findDbMonitor(String nationCode, Long unitId, List<Long> dbIds) throws AppException {
        List<MonitorDatabase> monitorDatabases;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "db_ids", ",", null, Joiner.on(",").join(dbIds)));
            parameterBOS.add(new ParameterBO(null, "unit_id", null, null, String.valueOf(unitId)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_DB_MONITOR_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            monitorDatabases = objectMapper.readValue(data, new TypeReference<List<MonitorDatabase>>() {
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return monitorDatabases;
    }

    @Override
    public List<Service> findFilterModule(String nationCode, String filter, Long unitId, String ip, List<Long> dbs) throws AppException {
        List<Service> services;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();

            List<String> fields = Arrays.asList("serviceCode", "serviceName", "moduleCode", "moduleName", "ipServer", "groupModuleName");
            parameterBOS.add(new ParameterBO(null, "unitId", ",", null, unitId.toString()));
            // Ip
            if (StringUtils.isNotEmpty(ip)) {
                parameterBOS.add(new ParameterBO(null, "findByIp", ",", null, "1"));
                parameterBOS.add(new ParameterBO(null, "ips", ",", "LIST", ip));

            } else {
                parameterBOS.add(new ParameterBO(null, "findByIp", ",", null, "0"));
                parameterBOS.add(new ParameterBO(null, "ips", ",", "LIST", "''"));

            }
            // Database
            String strDbs = "";
            if (dbs.size() > 0) {
                strDbs = dbs.stream().map(Object::toString).collect(Collectors.joining(","));
            }
            if (StringUtils.isNotEmpty(strDbs)) {
                parameterBOS.add(new ParameterBO(null, "findByDb", ",", null, "1"));
                parameterBOS.add(new ParameterBO(null, "dbIds", ",", "LIST", strDbs));

            } else {
                parameterBOS.add(new ParameterBO(null, "findByDb", ",", null, "0"));
                parameterBOS.add(new ParameterBO(null, "dbIds", ",", "LIST", "''"));

            }
            for (String field : fields) {
                parameterBOS.add(new ParameterBO(null, field, ",", null, "%" + filter + "%"));
            }

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_SERVICE_FOR_AAM_V2, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            services = objectMapper.readValue(data, new TypeReference<List<Service>>() {
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return services;
    }

    @Override
    public List<AccountForAppDTO> findAccountCachePass(String countryCode) throws AppException {
        List<AccountForAppDTO> accountForAppDTOS = new ArrayList<>();
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "unit_code", null, null, String.valueOf(countryCode)));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_ALL_USER_SV_SERVICE_FOR_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, countryCode);
            if (jsonData != null && jsonData.getDataJson() != null) {
                JSONParser pa = new JSONParser();
                JSONObject objRes = (JSONObject) pa.parse(jsonData.getDataJson());
                JSONArray arrayChecklist = (JSONArray) objRes.get("data");
                JSONObject objNode;
                logger.info("Total server node server IIM: " + arrayChecklist.size());
                AccountForAppDTO accountForAppDTO;
                for (int i = 0; i < arrayChecklist.size(); i++) {
                    objNode = (JSONObject) arrayChecklist.get(i);
                    if (objNode.get("IP_SERVER") != null && objNode.get("USERNAME") != null && objNode.get("COUNTRY_CODE") != null) {
                        accountForAppDTO = new AccountForAppDTO();
                        accountForAppDTO.setIp(objNode.get("IP_SERVER").toString());
                        accountForAppDTO.setUserName(objNode.get("USERNAME").toString());
                        accountForAppDTO.setCountryCode(objNode.get("COUNTRY_CODE").toString());
                        accountForAppDTO.setType(AamConstants.SECURITY_SERVER);
                        accountForAppDTOS.add(accountForAppDTO);
                    }
                }
            }

            request = new RequestInputBO(AamConstants.IIM_WS_CODE.GET_ALL_USER_DB_SERVICE_FOR_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            iimServices_portType = IimClientFactory.create();
            jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, countryCode);
            if (jsonData != null && jsonData.getDataJson() != null) {
                JSONParser pa = new JSONParser();
                JSONObject objRes = (JSONObject) pa.parse(jsonData.getDataJson());
                JSONArray arrayChecklist = (JSONArray) objRes.get("data");
                JSONObject objNode;
                logger.info("Total server node database IIM: " + arrayChecklist.size());
                AccountForAppDTO accountForAppDTO;
                for (int i = 0; i < arrayChecklist.size(); i++) {
                    objNode = (JSONObject) arrayChecklist.get(i);
                    if (objNode.get("DB_ID") != null && objNode.get("IP_VIRTUAL") != null && objNode.get("COUNTRY_CODE") != null && objNode.get("USERNAME") != null) {
                        accountForAppDTO = new AccountForAppDTO();
                        accountForAppDTO.setIp(objNode.get("IP_PHYSICAL").toString());
                        accountForAppDTO.setUserName(objNode.get("USERNAME").toString());
                        accountForAppDTO.setCountryCode(objNode.get("COUNTRY_CODE").toString());
                        accountForAppDTO.setDbid(objNode.get("DB_ID").toString());
                        if (objNode.get("HOST") != null) {
                            accountForAppDTO.setHost(objNode.get("HOST").toString());
                        }
                        accountForAppDTO.setType(AamConstants.SECURITY_DATABASE);
                        accountForAppDTOS.add(accountForAppDTO);
                    }
                }
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }
        logger.info("Total server node database IIM: " + accountForAppDTOS.size());

        return accountForAppDTOS;
    }

    // HungVC 28/09/2020
    @Override
    public List<Service> findServices(String nationCode, Long unitId) throws AppException {
        List<Service> services;
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();

            parameterBOS.add(new ParameterBO(null, "unitId", ",", null, unitId.toString()));

            RequestInputBO request = new RequestInputBO(AamConstants.IIM_WS_CODE.WS_GET_SERVICE_FOR_AAM, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]));
            IimServices_PortType iimServices_portType = IimClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(IimClientFactory.getAuthor(), request, nationCode);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
            services = objectMapper.readValue(data, new TypeReference<List<Service>>() {
            });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            throw new AppException(e.getMessage());
        }

        return services;
    }

    public static void main(String [] args){
        try {
            com.viettel.controller.IimClientServiceImpl iimService = new com.viettel.controller.IimClientServiceImpl();
            //List<String> moduleCodes = new ArrayList<>();
            List<Long> Ids = new ArrayList<>();
            Ids.add(1L);
            Ids.add(2L);
            //List<Module> modules = iimService.findModuleByCodes("VNM", moduleCodes);
            List<Module> modules = iimService.findModulesByIds("VNM", Ids);
            System.out.print(modules);
        }catch(Exception ex){
            logger.error(ex.getMessage(), ex);
        }
    }
}
