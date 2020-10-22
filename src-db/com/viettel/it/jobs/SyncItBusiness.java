package com.viettel.it.jobs;

import com.viettel.iim.services.main.*;
import com.viettel.it.model.*;
import com.viettel.it.persistence.*;
import com.viettel.it.util.Config;
import com.viettel.it.util.PasswordEncoder;
import com.viettel.passprotector.PassProtector;
import com.viettel.controller.AamConstants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
//import com.viettel.vsaadmin.service.Actor;
//import com.viettel.vsaadmin.service.Response;
//import com.viettel.vsaadmin.service.User;
//import com.viettel.vsaadmin.service.VsaadminService;
//import com.viettel.vsaadmin.service.VsaadminServiceService;

import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.ResourceBundle;

public class SyncItBusiness implements Job {

    protected static final Logger logger = LoggerFactory.getLogger(SyncItBusiness.class);

    public static final String HASH_KEY = "KSDHFKDSFJH9873423HD==DSFJGH";
    public static final String GET_SERVICES_GROUPS_AAM = "GET_SERVICES_GROUPS_AAM";
    public static final String SERVICES_DATABASES_AAM = "SERVICES_DATABASES_AAM";
    public static final String SERVICES_SERVERS_AAM = "SERVICES_SERVERS_AAM";

    @Override
    public void execute(JobExecutionContext arg0) throws JobExecutionException {
        try {
            logger.info(">>>>>>>>>>>>> start synchronized server and database node <<<<<<<<<<<<<<<<<<<<<");
            syncAllDevice();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private void syncAllDevice() {
        try {
            List<ItServices> lstServices = new ItServicesImpl().findList();
            Map<String, ItServices> mapServices = new HashMap<String, ItServices>();
            if (lstServices != null) {
                for (ItServices service : lstServices) {
                    mapServices.put(service.getServiceCode(), service);
                }
            }
            List<ItBusinessGroup> lstBusGroups = new ItBusGroupServiceImpl().findList();
            Map<String, ItBusinessGroup> mapBusGroups = new HashMap<String, ItBusinessGroup>();
            if (lstBusGroups != null) {
                for (ItBusinessGroup busGroup : lstBusGroups) {
                    mapBusGroups.put(busGroup.getBusinessName(), busGroup);
                }
            }
            syncServices(mapServices, mapBusGroups);

            List<ItUsers> lstUsers = new ItUsersServicesImpl().findList();
            Map<String, ItUsers> mapUsers = new HashMap<String, ItUsers>();

            for (ItUsers user : lstUsers) {
                mapUsers.put(user.getUserName(), user);
            }

            syncUser(mapUsers);


            // Rebuild tree
            rebuildTree();

        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public void syncServices(Map<String, ItServices> mapServices, Map<String, ItBusinessGroup> mapBusGroups) {
        try {
            logger.info(">>>>> Start synchronized services");

            JsonResponseBO jsonData = getDataJson(GET_SERVICES_GROUPS_AAM);
            if (jsonData != null) {
                JSONParser pa = new JSONParser();
                JSONObject objRes = (JSONObject) pa.parse(jsonData.getDataJson());
                JSONArray arrayChecklist = (JSONArray) objRes.get(Config.DATA_FIELD_NAME);

                JSONObject objChecklist;
                List<ItBusinessGroup> lstBusGroups = new ArrayList<ItBusinessGroup>();
                List<String> lstTemp = new ArrayList<String>();


                for (int i = 0; i < arrayChecklist.size(); i++) {
                    objChecklist = (JSONObject) arrayChecklist.get(i);
                    try {
                        if (mapBusGroups.get(objChecklist.get("GROUP_CODE")) == null && objChecklist.get("GROUP_CODE") != null
                                && !"".equals(objChecklist.get("GROUP_CODE"))) {
                            if (!lstTemp.contains((String) objChecklist.get("GROUP_CODE"))) {
                                ItBusinessGroup busGroup = new ItBusinessGroup();
                                busGroup.setBusinessName((String) objChecklist.get("GROUP_CODE"));
                                lstBusGroups.add(busGroup);
                                lstTemp.add((String) objChecklist.get("GROUP_CODE"));
                            }
                        }
                        if (!lstBusGroups.isEmpty()
                                && lstBusGroups.size() >= 1000) {
                            new ItBusGroupServiceImpl().saveOrUpdate(lstBusGroups);
                            lstBusGroups = new ArrayList<ItBusinessGroup>();
                            Thread.sleep(5000);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                } // end loop for

                if (!lstBusGroups.isEmpty()) {
                    new ItBusGroupServiceImpl().saveOrUpdate(lstBusGroups);
                }

                List<ItServices> lstServices = new ArrayList<ItServices>();
                List<ItBusinessGroup> lstBusGroupsNew = new ItBusGroupServiceImpl().findList();
                Map<String, ItBusinessGroup> mapBusGroupsNew = new HashMap<String, ItBusinessGroup>();
                if (lstBusGroupsNew != null) {
                    for (ItBusinessGroup busGroup : lstBusGroupsNew) {
                        mapBusGroupsNew.put(busGroup.getBusinessName(), busGroup);
                    }
                }
                for (int i = 0; i < arrayChecklist.size(); i++) {
                    objChecklist = (JSONObject) arrayChecklist.get(i);
                    try {
                        if (mapServices.get(objChecklist.get("SERVICES_CODE")) == null) {
                            ItServices service = new ItServices();
                            service.setServiceCode((String) objChecklist.get("SERVICES_CODE"));
                            service.setServiceName((String) objChecklist.get("SERVICES_NAME"));
                            if (objChecklist.get("GROUP_CODE") != null) {
                                service.setBusinessId(mapBusGroupsNew.get((String) objChecklist.get("GROUP_CODE")).getBusinessId());
                                lstServices.add(service);
                            }
                        } else {
                            ItServices service = mapServices.get(objChecklist.get("SERVICES_CODE"));
                            if (!service.getServiceName().equals((String) objChecklist.get("SERVICES_NAME")) ||
                                    service.getBusinessId() != mapBusGroupsNew.get((String) objChecklist.get("GROUP_CODE")).getBusinessId()) {
                                service.setServiceName((String) objChecklist.get("SERVICES_NAME"));
                                if (objChecklist.get("GROUP_CODE") != null) {
                                    service.setBusinessId(mapBusGroupsNew.get((String) objChecklist.get("GROUP_CODE")).getBusinessId());
                                    lstServices.add(service);
                                }
                            }
                        }
                        if (!lstServices.isEmpty()
                                && lstServices.size() >= 1000) {
                            new ItServicesImpl().saveOrUpdate(lstServices);
                            lstServices = new ArrayList<ItServices>();
                            Thread.sleep(5000);
                        }
                    } catch (Exception e) {
                        logger.error(e.getMessage(), e);
                    }
                } // end loop for

                if (!lstServices.isEmpty()) {
                    new ItServicesImpl().saveOrUpdate(lstServices);
                }


            }
            logger.info(">>>>>>>>>>>>>>>> finish synchronized service");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    /*
	Khoi tao lai cay dich vu
	 */
    private void rebuildTree() {
        try {
            logger.info("Start rebuild it business tree");
            List<ItBusinessGroup> lstBusinessGroup = new ItBusGroupServiceImpl().findList();
            if (lstBusinessGroup != null && !lstBusinessGroup.isEmpty()) {
                Map<String, Object> filters = new HashMap<>();
                filters.put("action.actionId", -1l);
                List<Action> lstAction = new ActionServiceImpl().findList(filters);
                if (lstAction != null) {

                    Action root = new ActionServiceImpl().findById(-1l);
                    Map<Long, Action> mapBusiness = new HashMap<>();
                    for (Action action : lstAction) {
                        mapBusiness.put(action.getServiceBusinessId(), action);
                    }

                    for (ItBusinessGroup business : lstBusinessGroup) {
                        // Lay ra danh sach cac service thuoc business nay
                        List<ItServices> lstService = business.getLstService();
                        if (mapBusiness.get(business.getBusinessId()) == null) {

                            Action newBusAction = new Action();
                            newBusAction.setTreeLevel(2l);
                            newBusAction.setDescription(business.getBusinessName());
                            newBusAction.setName(business.getBusinessName());
                            newBusAction.setServiceBusinessId(business.getBusinessId());
                            newBusAction.setAction(root);
                            newBusAction.setItbusinessType("BUSINESS");
                            Long busActionId = new ActionServiceImpl().save(newBusAction);
                            newBusAction.setActionId(busActionId);

                            if (lstService != null) {
                                List<Action> lstActionService = new ArrayList<>();
                                for (ItServices service : lstService) {
                                    Action serviceAction = new Action();
                                    serviceAction.setTreeLevel(3l);
                                    serviceAction.setDescription(service.getServiceCode());
                                    serviceAction.setName(service.getServiceName());
                                    serviceAction.setServiceBusinessId(service.getServiceId());
                                    serviceAction.setAction(newBusAction);
                                    serviceAction.setItbusinessType("SERVICE");
                                    lstActionService.add(serviceAction);
                                }
                                new ActionServiceImpl().saveOrUpdate(lstActionService);
                            }
                        } else {

                            Action businessAction = mapBusiness.get(business.getBusinessId());
                            // Lay ra danh sach cac action service thuoc business action
							List<Action> lstServiceAction = businessAction.getActions();
//                            List<Action> lstServiceAction = new ArrayList<>();

                            if (lstServiceAction != null) {
                                Map<Long, Action> mapServiceAction = new HashMap<>();
                                for (Action service : lstServiceAction) {
                                    mapServiceAction.put(service.getServiceBusinessId(), service);
                                }

                                List<Action> lstActionService = new ArrayList<>();
                                for (ItServices service : lstService) {
                                    if (mapServiceAction.get(service.getServiceId()) == null) {
                                        Action serviceAction = new Action();
                                        serviceAction.setName(service.getServiceName());
                                        serviceAction.setDescription(service.getServiceCode());
                                        serviceAction.setAction(businessAction);
                                        serviceAction.setServiceBusinessId(service.getServiceId());
                                        serviceAction.setItbusinessType("SERVICE");
                                        serviceAction.setTreeLevel(3l);
                                        lstActionService.add(serviceAction);
                                    } else {
                                        Action serviceAction = mapServiceAction.get(service.getServiceId());
                                        serviceAction.setName(service.getServiceName());
                                        serviceAction.setDescription(service.getServiceCode());
                                        lstActionService.add(serviceAction);
                                    }
                                }

                                new ActionServiceImpl().saveOrUpdate(lstActionService);
                            }
                        }
                    }
                }
            }
            logger.info("End rebuild it business tree");
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

//    public void syncNodes(Map<String, ItServices> mapServices, Map<String, ItNode> mapNodes) {
//        try {
//            logger.info(">>>>> Start synchronized nodes");
//
//            JsonResponseBO jsonData = getDataJson(SERVICES_DATABASES_AAM);
//            if (jsonData != null) {
//                JSONParser pa = new JSONParser();
//                JSONObject objRes = (JSONObject) pa.parse(jsonData.getDataJson());
//                JSONArray arrayChecklist = (JSONArray) objRes.get(Config.DATA_FIELD_NAME);
//
//                JSONObject objChecklist;
//                List<ItNode> lstNodes = new ArrayList<ItNode>();
//                for (int i = 0; i < arrayChecklist.size(); i++) {
//                    objChecklist = (JSONObject) arrayChecklist.get(i);
//                    try {
//                        if (mapServices.get(objChecklist.get("SERVICES_CODE")) != null && objChecklist.get("USERNAME") != null
//                                && !"".equals(objChecklist.get("USERNAME")) && objChecklist.get("DB_CODE") != null && !"".equals(objChecklist.get("DB_CODE"))) {
//                            if (mapNodes.get(mapServices.get(objChecklist.get("SERVICES_CODE")).getServiceId() + "@" + objChecklist.get("DB_CODE") + "@" + objChecklist.get("USERNAME")) == null) {
//                                ItNode itNode = new ItNode();
//                                itNode.setNodeCode((String) objChecklist.get("DB_CODE"));
//                                itNode.setNodeName((String) objChecklist.get("DB_NAME"));
//                                itNode.setServiceId(mapServices.get(objChecklist.get("SERVICES_CODE")).getServiceId());
//                                itNode.setJdbcUrl((String) objChecklist.get("URL"));
//                                itNode.setUserName((String) objChecklist.get("USERNAME"));
//                                itNode.setPassword((String) objChecklist.get("PASSWORD"));
//                                itNode.setPort(objChecklist.get("PORT") == null ? null : Long.valueOf((String) objChecklist.get("PORT")));
//                                lstNodes.add(itNode);
//                            } else {
//                                ItNode itNode = mapNodes.get(mapServices.get(objChecklist.get("SERVICES_CODE")).getServiceId() + "@" + objChecklist.get("DB_CODE") + "@" + objChecklist.get("USERNAME"));
//                                boolean check = false;
//                                if (itNode.getNodeName() == null || !itNode.getNodeName().equals((String) objChecklist.get("DB_NAME"))) {
//                                    itNode.setNodeName((String) objChecklist.get("DB_NAME"));
//                                    check = true;
//                                }
//                                if (itNode.getJdbcUrl() == null || !itNode.getJdbcUrl().equals((String) objChecklist.get("URL"))) {
//                                    itNode.setJdbcUrl((String) objChecklist.get("URL"));
//                                    check = true;
//                                }
//                                if (itNode.getPassword() == null
//                                        || !itNode.getPassword().equals((String) objChecklist.get("PASSWORD"))) {
//                                    itNode.setPassword((String) objChecklist.get("PASSWORD"));
//                                    check = true;
//                                }
//                                if (itNode.getPort() == null || !itNode.getPort().equals(objChecklist.get("PORT") == null ? null : Long.valueOf((String) objChecklist.get("PORT")))) {
//                                    itNode.setPort(objChecklist.get("PORT") == null ? null : Long.valueOf((String) objChecklist.get("PORT")));
//                                    check = true;
//                                }
//                                if (check) {
//                                    lstNodes.add(itNode);
//                                }
//                            }
//                        }
//                        if (!lstNodes.isEmpty()
//                                && lstNodes.size() >= 1000) {
//                            new ItNodeServiceImpl().saveOrUpdate(lstNodes);
//                            lstNodes = new ArrayList<ItNode>();
//                            Thread.sleep(5000);
//                        }
//                    } catch (Exception e) {
//                        logger.error(e.getMessage(), e);
//                    }
//                } // end loop for
//
//                if (!lstNodes.isEmpty()) {
//                    new ItNodeServiceImpl().saveOrUpdate(lstNodes);
//                }
//            }
//
//
//            JsonResponseBO jsonDataServer = getDataJson(SERVICES_SERVERS_AAM);
//            if (jsonDataServer != null) {
//                JSONParser paServer = new JSONParser();
//                JSONObject objResServer = (JSONObject) paServer.parse(jsonDataServer.getDataJson());
//                JSONArray arrayChecklistServer = (JSONArray) objResServer.get(Config.DATA_FIELD_NAME);
//
//                JSONObject objChecklist;
//                List<ItNode> lstNodes = new ArrayList<ItNode>();
//
//                for (int i = 0; i < arrayChecklistServer.size(); i++) {
//                    objChecklist = (JSONObject) arrayChecklistServer.get(i);
//                    try {
//                        if (mapServices.get(objChecklist.get("SERVICES_CODE")) != null && objChecklist.get("USERNAME") != null
//                                && !"".equals(objChecklist.get("USERNAME")) && objChecklist.get("SERVER_CODE") != null && !"".equals(objChecklist.get("SERVER_CODE"))) {
//                            if (mapNodes.get(mapServices.get(objChecklist.get("SERVICES_CODE")).getServiceId() + "@" + objChecklist.get("SERVER_CODE") + "@" + objChecklist.get("USERNAME")) == null) {
//                                ItNode itNode = new ItNode();
//                                itNode.setNodeCode((String) objChecklist.get("SERVER_CODE"));
//                                itNode.setNodeName((String) objChecklist.get("SERVER_NAME"));
//                                itNode.setServiceId(mapServices.get(objChecklist.get("SERVICES_CODE")).getServiceId());
//                                itNode.setJdbcUrl((String) objChecklist.get("URL"));
//                                itNode.setUserName((String) objChecklist.get("USERNAME"));
//                                itNode.setPassword((String) objChecklist.get("PASSWORD"));
//                                itNode.setPort(objChecklist.get("PORT") == null ? null : Long.valueOf((String) objChecklist.get("PORT")));
//                                itNode.setNodeIp((String) objChecklist.get("IP_SERVER"));
//                                lstNodes.add(itNode);
//                            } else {
//                                ItNode itNode = mapNodes.get(mapServices.get(objChecklist.get("SERVICES_CODE")).getServiceId() + "@" + objChecklist.get("SERVER_CODE") + "@" + objChecklist.get("USERNAME"));
//                                boolean check = false;
//                                if (itNode.getNodeName() == null
//                                        || !itNode.getNodeName().equals((String) objChecklist.get("SERVER_NAME"))) {
//                                    itNode.setNodeName((String) objChecklist.get("SERVER_NAME"));
//                                    check = true;
//                                }
//                                if (itNode.getJdbcUrl() == null
//                                        || !itNode.getJdbcUrl().equals((String) objChecklist.get("URL"))) {
//                                    itNode.setJdbcUrl((String) objChecklist.get("URL"));
//                                    check = true;
//                                }
//                                if (itNode.getPassword() == null || !itNode.getPassword().equals((String) objChecklist.get("PASSWORD"))) {
//                                    itNode.setPassword((String) objChecklist.get("PASSWORD"));
//                                    check = true;
//                                }
//                                if (itNode.getPort() == null || !itNode.getPort().equals(objChecklist.get("PORT") == null ? null : Long.valueOf((String) objChecklist.get("PORT")))) {
//                                    itNode.setPort(objChecklist.get("PORT") == null ? null : Long.valueOf((String) objChecklist.get("PORT")));
//                                    check = true;
//                                }
//                                if (itNode.getNodeIp() == null || !itNode.getNodeIp().equals((String) objChecklist.get("IP_SERVER"))) {
//                                    itNode.setNodeIp((String) objChecklist.get("IP_SERVER"));
//                                    check = true;
//                                }
//                                if (check) {
//                                    lstNodes.add(itNode);
//                                }
//                            }
//                        }
//                        if (!lstNodes.isEmpty()
//                                && lstNodes.size() >= 1000) {
//                            new ItNodeServiceImpl().saveOrUpdate(lstNodes);
//                            lstNodes = new ArrayList<ItNode>();
//                            Thread.sleep(5000);
//                        }
//                    } catch (Exception e) {
//                        logger.error(e.getMessage(), e);
//                    }
//                } // end loop for
//
//                if (!lstNodes.isEmpty()) {
//                    new ItNodeServiceImpl().saveOrUpdate(lstNodes);
//                }
//            }
//            logger.info(">>>>>>>>>>>>>>>> finish synchronized server node");
//        } catch (Exception e) {
//            logger.error(e.getMessage(), e);
//        }
//    }

    public void syncUser(Map<String, ItUsers> mapUsers) {
        try {
            logger.info("Bat dau lay du lieu tu WS VSA...");
//            int countRepply = 1;
//            boolean checkWs = true;
//            ResourceBundle bundle = ResourceBundle.getBundle("config");
//            String urlStr = bundle.getString("vsaUrl");
//            String namespace = bundle.getString("vsaNamespace");
//            String localPart = bundle.getString("vsaLocalPart");
//            String username = bundle.getString("vsaUsername");
//            String password = PassProtector.decrypt(bundle.getString("vsaPassword"), HASH_KEY);
//            VsaadminService port = null;
//            URL url = null;
//            URL baseUrl;
//            while ((countRepply <= 3 && !checkWs) || countRepply == 1) {
//                try {
//                    baseUrl = VsaadminServiceService.class.getResource(".");
//                    try {
//                        url = new URL(baseUrl, urlStr);
//                    } catch (MalformedURLException ex) {
//                        logger.error(ex.getMessage(), ex);
//                    }
//                    VsaadminServiceService service = new VsaadminServiceService(url, new QName(namespace, localPart));
//                    port = service.getVsaadminServicePort();
//
//                    countRepply = countRepply + 1;
//                    checkWs = true;
//
//                    if (checkWs && port != null) {
//                        ((BindingProvider) port).getRequestContext().put(BindingProviderProperties.REQUEST_TIMEOUT, 10800000);
//                        ((BindingProvider) port).getRequestContext().put(BindingProviderProperties.CONNECT_TIMEOUT, 10800000);
//
//
//                        Actor actor = new Actor();
//                        actor.setUserName(username);
//                        actor.setPassword(password);
//
//                        Response response = port.getAllUser(true,"TDTT", actor);
//                        List<ItUsers> lstUsers = new ArrayList<ItUsers>();
//                        for (Object obj : response.getValues()) {
//                            User temp = (User) obj;
//                            try {
//                                if (mapUsers.get(temp.getUserName()) == null) {
//                                    ItUsers user = new ItUsers();
//                                    user.setUserName(temp.getUserName());
//                                    user.setEmail(temp.getEmail());
//                                    user.setFullName(temp.getFullName());
//                                    user.setPhone(temp.getCellPhone());
//                                    try {
//                                        user.setStaffCode(Long.parseLong(temp.getStaffCode()));
//                                    } catch (Exception e) {
//                                        logger.error(e.getMessage(), e);
//                                    }
//
//                                    user.setStatus(1L);
//                                    lstUsers.add(user);
//                                } else {
//                                    ItUsers user = mapUsers.get(temp.getUserName());
//                                    boolean check = false;
//                                    if (!user.getEmail().equals(temp.getEmail())) {
//                                        user.setEmail(temp.getEmail());
//                                        check = true;
//                                    }
//                                    if (!user.getFullName().equals(temp.getFullName())) {
//                                        user.setFullName(temp.getFullName());
//                                        check = true;
//                                    }
//                                    if (!user.getPhone().equals(temp.getCellPhone())) {
//                                        user.setPhone(temp.getCellPhone());
//                                        check = true;
//                                    }
//                                    if (!user.getStaffCode().equals(Long.parseLong(temp.getStaffCode()))) {
//                                        user.setStaffCode(Long.parseLong(temp.getStaffCode()));
//                                        check = true;
//                                    }
//                                    if (check) {
//                                        lstUsers.add(user);
//                                    }
//                                }
//                                if (!lstUsers.isEmpty()
//                                        && lstUsers.size() >= 1000) {
//                                    new ItUsersServicesImpl().saveOrUpdate(lstUsers);
//                                    lstUsers = new ArrayList<ItUsers>();
//                                    Thread.sleep(5000);
//                                }
//                            } catch (Exception e) {
//                                logger.error(e.getMessage(), e);
//                            }
//                        }
//                        if (!lstUsers.isEmpty()) {
//                            new ItUsersServicesImpl().saveOrUpdate(lstUsers);
//                        }
//                        logger.info("Ket thuc lay du lieu user tu WS VSA! ");
//                    }
//                } catch (Exception e) {
////                    logger.error(e);
//                    countRepply = countRepply + 1;
//                    checkWs = false;
//                    logger.info("Sleep 10s to try connect WS VSA....");
//                    Thread.sleep(10000);
//                }
//            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private JsonResponseBO getDataJson(String request_code) {
        JsonResponseBO jsonData = null;
        try {
            ResourceBundle bundle = ResourceBundle.getBundle("config");
            IimServices_ServiceLocator service = new IimServices_ServiceLocator();

            IimServices_PortType iimPortType = service.getIimServicesPort(new URL(bundle.getString("link_webservice_database_server_qltn")));
            AuthorityBO author = new AuthorityBO(bundle.getString("password_webservice_database_server_qltn"), 1l, bundle.getString("user_webservice_database_server_qltn"));

            ParameterBO param = new ParameterBO("unit_code", "unit_code", null, "String", "VIETTEL");
            ParameterBO[] params = new ParameterBO[1];
            params[0] = param;
            RequestInputBO request = new RequestInputBO(request_code, 2457, params);

            jsonData = iimPortType.getDataJson(author, request, AamConstants.NATION_CODE.VIETNAM);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            jsonData = null;
        }
        return jsonData;
    }


    public static void main(String[] args) {
        try {
//			System.out.println(PasswordEncoder.decrypt("21csLJpDmsPptnfOT4u2yw=="));
            new SyncItBusiness().syncAllDevice();
//			new SyncServer().syncAllDevice("jdbc:oracle:thin:@10.60.6.177:1521/qlts", "qltn", "qL7n1234");
//            System.out.println(PassProtector.encrypt("attt2014@#attt", HASH_KEY));
//			
//			ResourceBundle bundle = ResourceBundle.getBundle("config");
//			IimServices_ServiceLocator service = new IimServices_ServiceLocator();
//			
//			IimServices_PortType iimPortType = service.getIimServicesPort(new URL(bundle.getString("link_webservice_database_server_qltn")));
//			AuthorityBO author = new AuthorityBO(bundle.getString("password_webservice_database_server_qltn"), 1, bundle.getString("user_webservice_database_server_qltn"));
//			
//			ParameterBO param = new ParameterBO("unit_code", "unit_code", null, "String", "VIETTEL");
//			ParameterBO[] params = new ParameterBO[1];
//			params[0] = param;
//			RequestInputBO request = new RequestInputBO("GET_SV_USER_FOR_AAM", 0, params);
//			
//			JsonResponseBO response = iimPortType.getDataJson(author, request);
//			logger.info(response.getDataJson());
//			
//			JSONParser pa = new JSONParser();
//            JSONObject objRes = (JSONObject) pa.parse(response.getDataJson());
//            JSONArray arrayChecklist = (JSONArray) objRes.get(Config.DATA_FIELD_NAME);
//            
//            JSONObject objChecklist = null;
//            for (int i = 0; i < arrayChecklist.size(); i++) {
//            	objChecklist = (JSONObject) arrayChecklist.get(i);
////            	if (objChecklist.get("checklist_name") != null 
////            			&& objChecklist.get("checklist_code") != null) {
////            		checklistParams.add(new SelectItem((String)objChecklist.get("checklist_code"), (String)objChecklist.get("checklist_name")));
////            	}
//            }


        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }


}
