package com.viettel.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Joiner;

import com.viettel.jackson.AamJsonFactory;
import com.viettel.util.AomClientFactory;
import org.apache.log4j.LogManager;
import org.apache.log4j.Logger;
import org.springframework.context.annotation.Scope;

import java.util.*;

/**
 * @author quanns2
 */
@org.springframework.stereotype.Service(value = "aomService")
@Scope("session")
public class AomClientServiceImpl implements AomClientService {
    private static Logger logger = LogManager.getLogger(AomClientServiceImpl.class);

    @Override
    public List<ServerChecklist> findModulesByIds(List<String> ips) throws AppException {
        List<ServerChecklist> serverChecklists = new ArrayList<>();
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "ip", ",", "STRING", Joiner.on(",").join(ips)));

            RequestInputBO request = new RequestInputBO(AamConstants.AOM_WS_CODE.CHECK_LIST_AAM_SERVER, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]), "");
            AomWebservice_PortType iimServices_portType = AomClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(AomClientFactory.getAuthor(), request);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            serverChecklists = objectMapper.readValue(data, new TypeReference<List<ServerChecklist>>() {
            });

            for (ServerChecklist serverChecklist : serverChecklists) {
                String alarm = serverChecklist.getTimeMonitor();
                List<ChecklistAlarm> checklistAlarms = objectMapper.readValue(alarm, new TypeReference<List<ChecklistAlarm>>() {
                });

                serverChecklist.setChecklistAlarms(checklistAlarms);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return serverChecklists;
    }

    @Override
    public List<ModuleChecklist> findChecklistModulesByIds(List<Long> moduleIds) throws AppException {
        List<ModuleChecklist> moduleChecklists = new ArrayList<>();
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "process_id", ",", "STRING", Joiner.on(",").join(moduleIds)));

            RequestInputBO request = new RequestInputBO(AamConstants.AOM_WS_CODE.CHECK_LIST_AAM_MODULE, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]), "");
            AomWebservice_PortType iimServices_portType = AomClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(AomClientFactory.getAuthor(), request);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            moduleChecklists = objectMapper.readValue(data, new TypeReference<List<ModuleChecklist>>() {
            });

            for (ModuleChecklist moduleChecklist : moduleChecklists) {
                String alarm = moduleChecklist.getTimeMonitor();
                List<ChecklistAlarm> checklistAlarms = objectMapper.readValue(alarm, new TypeReference<List<ChecklistAlarm>>() {
                });

                moduleChecklist.setChecklistAlarms(checklistAlarms);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return moduleChecklists;
    }

    @Override
    public List<QueueChecklist> findChecklistQueues(List<Long> serviceIds) throws AppException {
        List<QueueChecklist> queueChecklists = new ArrayList<>();
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "app_id", ",", "STRING", Joiner.on(",").join(serviceIds)));

            RequestInputBO request = new RequestInputBO(AamConstants.AOM_WS_CODE.CHECK_LIST_AAM_DATABASE, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]), "");
            AomWebservice_PortType iimServices_portType = AomClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(AomClientFactory.getAuthor(), request);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            queueChecklists = objectMapper.readValue(data, new TypeReference<List<QueueChecklist>>() {
            });


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return queueChecklists;
    }

    @Override
    public List<QueueChecklist> findChecklistQueueByIds(List<Long> queueIds) throws AppException {
        List<QueueChecklist> queueChecklists = new ArrayList<>();
        try {
            List<ParameterBO> parameterBOS = new ArrayList<>();
            parameterBOS.add(new ParameterBO(null, "queue_id", ",", "STRING", Joiner.on(",").join(queueIds)));

            RequestInputBO request = new RequestInputBO(AamConstants.AOM_WS_CODE.CHECK_LIST_AAM_DATABASE_BY_ID, 2457, parameterBOS.toArray(new ParameterBO[parameterBOS.size()]), "");
            AomWebservice_PortType iimServices_portType = AomClientFactory.create();
            JsonResponseBO jsonData = iimServices_portType.getDataJson(AomClientFactory.getAuthor(), request);

            ObjectMapper preMapper = new ObjectMapper(new AamJsonFactory());
            JsonNode node = preMapper.readTree(jsonData.getDataJson());
            String data = node.get("data").toString();

            ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES,false);
            queueChecklists = objectMapper.readValue(data, new TypeReference<List<QueueChecklist>>() {
            });


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }

        return queueChecklists;
    }


    public static void main(String[] args) {
        AomClientService aomClientService = new AomClientServiceImpl();
        try {
            List<ServerChecklist> serverChecklists = aomClientService.findModulesByIds(Arrays.asList("10.60.97.16", "10.61.97.17"));
            System.out.println(serverChecklists.size());
        } catch (AppException e) {
            logger.error(e.getMessage(), e);
        }
    }
}
