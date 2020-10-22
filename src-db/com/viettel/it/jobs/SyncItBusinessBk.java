package com.viettel.it.jobs;

import com.viettel.it.model.*;
import com.viettel.it.persistence.*;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SyncItBusinessBk implements Job {
	
	protected static final Logger logger = LoggerFactory.getLogger(SyncItBusinessBk.class);
	
	public static final String GET_SERVERS_FOR_AAM = "GET_SERVERS_FOR_AAM";
	public static final String GET_SV_USER_FOR_AAM = "GET_SV_USER_FOR_AAM";
	public static final String GET_DATABASES_FOR_AAM = "GET_DATABASES_FOR_AAM";
	public static final String GET_DB_USER_FOR_AAM = "GET_DB_USER_FOR_AAM";

	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		try {
			logger.info(">>>>>>>>>>>>> start rebuild tree action <<<<<<<<<<<<<<<<<<<<<");
			rebuildTree();
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}

	/*
	Khoi tao lai cay dich vu
	 */
	private void rebuildTree() {
		try {
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
//							List<Action> lstServiceAction = mapBusiness.get(business.getBusinessId()).getActions();
							List<Action> lstServiceAction = new ArrayList<>();

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
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}


	
	public static void main(String[] args) {
		try {
//			System.out.println(PasswordEncoder.encrypt("prm"));
//			System.out.println(PasswordEncoder.decrypt("21csLJpDmsPptnfOT4u2yw=="));

//			new SyncServer().syncAllDevice("jdbc:oracle:thin:@10.60.6.177:1521/qlts", "qltn", "qL7n1234");
//			System.out.println(PasswordEncoder.decrypt("yaWVXaH9hP4nfKE6bzaGyg=="));
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
