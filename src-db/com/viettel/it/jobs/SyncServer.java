package com.viettel.it.jobs;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ResourceBundle;

import com.viettel.controller.AamConstants;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.viettel.iim.services.main.AuthorityBO;
import com.viettel.iim.services.main.IimServices_PortType;
import com.viettel.iim.services.main.IimServices_ServiceLocator;
import com.viettel.iim.services.main.JsonResponseBO;
import com.viettel.iim.services.main.ParameterBO;
import com.viettel.iim.services.main.RequestInputBO;
import com.viettel.it.model.Node;
import com.viettel.it.model.NodeAccount;
import com.viettel.it.model.NodeType;
import com.viettel.it.model.Vendor;
import com.viettel.it.model.Version;
import com.viettel.it.persistence.NodeAccountServiceImpl;
import com.viettel.it.persistence.NodeServiceImpl;
import com.viettel.it.persistence.NodeTypeServiceImpl;
import com.viettel.it.persistence.VendorServiceImpl;
import com.viettel.it.persistence.VersionServiceImpl;
import com.viettel.it.util.Config;
import com.viettel.it.util.Config.APP_TYPE;
import com.viettel.it.util.Config.OS_TYPE;
import com.viettel.it.util.Config.VERSTION_TYPE;
import com.viettel.it.util.PasswordEncoder;

public class SyncServer implements Job {
	
	protected static final Logger logger = LoggerFactory.getLogger(SyncServer.class); 
	
	public static final String GET_SERVERS_FOR_AAM = "GET_SERVERS_FOR_AAM";
	public static final String GET_SV_USER_FOR_AAM = "GET_SV_USER_FOR_AAM";
	public static final String GET_DATABASES_FOR_AAM = "GET_DATABASES_FOR_AAM";
	public static final String GET_DB_USER_FOR_AAM = "GET_DB_USER_FOR_AAM";

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
			
			/*
			 * Lay danh sach vendor 
			 */
			List<Vendor> lstVendor = new VendorServiceImpl().findList();
			Map<Long, Vendor> mapVendor = new HashMap<Long, Vendor>();
			if (lstVendor != null) {
				for (Vendor vendor : lstVendor) {
					mapVendor.put(vendor.getVendorId(), vendor);
				}
			}
			
			/*
			 * Lay danh sach node type
			 */
			List<NodeType> lstNodeType = new NodeTypeServiceImpl().findList();
			Map<Long, NodeType> mapNodeType = new HashMap<Long, NodeType>();
			if (lstNodeType != null) {
				for (NodeType nodeType : lstNodeType) {
					mapNodeType.put(nodeType.getTypeId(), nodeType);
				}
			}
			
			/*
			 * Lay danh sach version
			 */
			List<Version> lstVersion = new VersionServiceImpl().findList();
			Map<Long, Version> mapVersion = new HashMap<Long, Version>();
			if (lstVersion != null) {
				for (Version version : lstVersion) {
					mapVersion.put(version.getVersionId(), version);
				}
			}
			
			/*
			 * synchronized server node
			 */
			syncServerDevice(mapVendor, mapNodeType, mapVersion);
			
			// sleep 1 minute
//			Thread.sleep(60000);
			
			/*
			 * synchronized account server node
			 */
			syncAccountServer();
			
			// sleep 1 minute
			Thread.sleep(60000);
			
			/*
			 * synchronized database node
			 */
			syncDbDevice(mapVendor, mapNodeType, mapVersion);
			
			// sleep 1 minute
//			Thread.sleep(60000);
			
			/*
			 * synchronized account database
			 */
			syncAccountDb();
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	public void syncServerDevice(Map<Long, Vendor> mapVendor, Map<Long, NodeType> mapNodeType, 
			Map<Long, Version> mapVersion) {
		try {
			logger.info(">>>>>>>>>>>>>>> Start synchronized server node");
			
				JsonResponseBO jsonData = getDataJson(GET_SERVERS_FOR_AAM);
				if (jsonData != null) {
					
					/*
					 * Lay danh sach cac node server
					 */
					Map<String, Object> filters = new HashMap<String, Object>();
					filters.put("vendor.vendorId", APP_TYPE.SERVER.value);
					List<Node> lstNodeOld = new NodeServiceImpl().findListExac(filters, null);
					Map<String, Node> mapNodeOld = new HashMap<String, Node>();
					if (lstNodeOld != null) {
						for (Node node : lstNodeOld) {
							mapNodeOld.put(node.getServerId() + "_" + APP_TYPE.SERVER.value, node);
						}
					}
					
					JSONParser pa = new JSONParser();
		            JSONObject objRes = (JSONObject) pa.parse(jsonData.getDataJson());
		            JSONArray arrayChecklist = (JSONArray) objRes.get(Config.DATA_FIELD_NAME);
		            
		            JSONObject objChecklist;
		            Node node;
		            List<Node> lstNodeUpdate = new ArrayList<Node>();
		            
		            for (int i = 0; i < arrayChecklist.size(); i++) {
		            	objChecklist = (JSONObject) arrayChecklist.get(i);
		            	try {
							node = new Node();
							if (mapNodeOld.get(objChecklist.get("SERVER_ID") + "_" + APP_TYPE.SERVER.value) != null) {
								node = mapNodeOld.get(objChecklist.get("SERVER_ID") + "_" + APP_TYPE.SERVER.value);
							} 
							
							node.setServerId((Long) objChecklist.get("SERVER_ID"));
							if (objChecklist.get("NODE_IP") != null) {
								node.setNodeCode((String)objChecklist.get("NODE_IP") + "_" + (String)objChecklist.get("NODE_CODE"));
							} else {
								node.setNodeCode((String) objChecklist.get("NODE_CODE"));
							}
							node.setNodeIp((String) objChecklist.get("NODE_IP"));
							node.setEffectIp((String) objChecklist.get("NODE_IP"));
							node.setNodeName((String) objChecklist.get("NODE_CODE"));
							node.setVendor(mapVendor.get(APP_TYPE.SERVER.value) == null ? null : mapVendor.get(APP_TYPE.SERVER.value));
							node.setNodeType(mapNodeType.get((Long) objChecklist.get("OS_TYPE")) == null ? null : mapNodeType.get((Long) objChecklist.get("OS_TYPE")));
							node.setVersion(mapVersion.get(VERSTION_TYPE.SERVER_DEFAULT.value) == null ? null : mapVersion.get(VERSTION_TYPE.SERVER_DEFAULT.value));
							node.setPort(22);
							
							lstNodeUpdate.add(node);
							if (!lstNodeUpdate.isEmpty()
									&& lstNodeUpdate.size() >= 1000) {
								new NodeServiceImpl().saveOrUpdate(lstNodeUpdate);
								lstNodeUpdate = new ArrayList<Node>();
								Thread.sleep(5000);
							}
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
						}
		            } // end loop for
		            
		            if (!lstNodeUpdate.isEmpty()) {
						new NodeServiceImpl().saveOrUpdate(lstNodeUpdate);
					}
				}
				logger.info(">>>>>>>>>>>>>>>> finish synchronized server node");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} 
	}
	
	public void syncDbDevice(Map<Long, Vendor> mapVendor, Map<Long, NodeType> mapNodeType, 
			Map<Long, Version> mapVersion) {
		try {
			logger.info(">>>>>>>>>>>>>> Synchronized database node");
			
			JsonResponseBO jsonData = getDataJson(GET_DATABASES_FOR_AAM);
			if (jsonData != null) {
				/*
				 * Lay danh sach cac node database
				 */
				Map<String, Object> filters = new HashMap<String, Object>();
				filters.put("vendor.vendorId", APP_TYPE.DATABASE.value);
				List<Node> lstNodeOld = new NodeServiceImpl().findListExac(filters, null);
				Map<String, Node> mapNodeOld = new HashMap<String, Node>();
				if (lstNodeOld != null) {
					for (Node node : lstNodeOld) {
						mapNodeOld.put(node.getServerId() + "_" + APP_TYPE.DATABASE.value, node);
					}
				}
				
				JSONParser pa = new JSONParser();
	            JSONObject objRes = (JSONObject) pa.parse(jsonData.getDataJson());
	            JSONArray arrayChecklist = (JSONArray) objRes.get(Config.DATA_FIELD_NAME);
	            
	            JSONObject objChecklist;
	            Node node;
	            List<Node> lstNodeUpdate = new ArrayList<Node>();
	            
	            for (int i = 0; i < arrayChecklist.size(); i++) {
	            	try {
	            		objChecklist = (JSONObject) arrayChecklist.get(i);
						node = new Node();
						if (mapNodeOld.get((Long) objChecklist.get("DB_ID") + "_" + APP_TYPE.DATABASE.value) != null) {
							node = mapNodeOld.get((Long) objChecklist.get("DB_ID") + "_" + APP_TYPE.DATABASE.value);
						} 
						node.setServerId((Long) objChecklist.get("DB_ID"));
						node.setNodeCode((String) objChecklist.get("IP_VIRTUAL") + "_" + (String) objChecklist.get("DB_CODE"));
						node.setNodeIp((String) objChecklist.get("IP_PHYSICAL"));
						node.setNodeName((String) objChecklist.get("DB_CODE"));
						node.setServiceName((String) objChecklist.get("SERVICE_NAME"));
						node.setNodeIpVirtual((String) objChecklist.get("IP_VIRTUAL"));
						node.setEffectIp((String) objChecklist.get("IP_VIRTUAL"));
						node.setPort(Integer.valueOf((String) objChecklist.get("PORT")));
						node.setVendor(mapVendor.get(APP_TYPE.DATABASE.value) == null ? null : mapVendor.get(APP_TYPE.DATABASE.value));
						node.setNodeType(mapNodeType.get(OS_TYPE.DEFAULT.value) == null ? null : mapNodeType.get(OS_TYPE.DEFAULT.value));
						node.setVersion(mapVersion.get((Long) objChecklist.get("VERSION")) == null ? null : mapVersion.get((Long) objChecklist.get("VERSION")));
						node.setJdbcUrl((String) objChecklist.get("URL"));
						
						lstNodeUpdate.add(node);
						if (!lstNodeUpdate.isEmpty()
								&& lstNodeUpdate.size() >= 1000) {
							new NodeServiceImpl().saveOrUpdate(lstNodeUpdate);
							logger.info("add batch db node");
							lstNodeUpdate = new ArrayList<Node>();
							Thread.sleep(3000);
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
	            	
	            } // end loop for
	            
	            if (!lstNodeUpdate.isEmpty()) {
					new NodeServiceImpl().saveOrUpdate(lstNodeUpdate);
					logger.info("add batch db node");
//					lstNodeUpdate = new ArrayList<Node>();
					Thread.sleep(3000);
				}
			}
				
			logger.info(">>>>>>>>>>>>>>>> finish synchronized database node");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} 
	}
	
	public NodeType getNodeType(Map<Long, NodeType> mapNodeType, String osName) {
		NodeType nodeType = null;
		if (osName != null 
				&& !osName.trim().isEmpty()
				&& mapNodeType != null
				&& !mapNodeType.isEmpty()) {
			
		}
		return nodeType;
	}
	
	public void syncAccountServer() {
		try {
			logger.info(">>>>>>>>>>>>>> Starting synchronized account server node");
			
			JsonResponseBO jsonData = getDataJson(GET_SV_USER_FOR_AAM);
			if (jsonData != null) {
				
				JSONParser pa = new JSONParser();
	            JSONObject objRes = (JSONObject) pa.parse(jsonData.getDataJson());
	            JSONArray arrayChecklist = (JSONArray) objRes.get(Config.DATA_FIELD_NAME);
	            
	            Map<String, Object> filters = new HashMap<String, Object>();
				filters.put("accountType", APP_TYPE.SERVER.value);
				List<NodeAccount> lstAccNodeOld = new NodeAccountServiceImpl().findListExac(filters, null);
				Map<String, NodeAccount> mapAccountNodeOld = new HashMap<String, NodeAccount>();
				if (lstAccNodeOld != null) {
					for (NodeAccount node : lstAccNodeOld) {
						mapAccountNodeOld.put(node.getServerId() + "_" + node.getUsername() + "_" + APP_TYPE.SERVER.value, node);
					}
				}
				
	            JSONObject objChecklist;
	            NodeAccount node;
	            List<NodeAccount> lstNodeAccountUpdate = new ArrayList<NodeAccount>();
	            
	            for (int i = 0; i < arrayChecklist.size(); i++) {
	            	objChecklist = (JSONObject) arrayChecklist.get(i);
	            	try {
	            		node = new NodeAccount();
						if (mapAccountNodeOld.get((Long)objChecklist.get("SERVER_ID") + "_" + (String)objChecklist.get("USERNAME") + "_" + APP_TYPE.SERVER.value) != null) {
							node = mapAccountNodeOld.get((Long)objChecklist.get("SERVER_ID") + "_" + (String)objChecklist.get("USERNAME") + "_" + APP_TYPE.SERVER.value);
						}
						
						node.setServerId((Long)objChecklist.get("SERVER_ID"));
						node.setPassword((String)objChecklist.get("PASSWORD"));
						node.setUsername((String)objChecklist.get("USERNAME"));
						node.setShell((String)objChecklist.get("SHELL"));
						node.setAccountType(APP_TYPE.SERVER.value);
						node.setImpactOrMonitor((Long)objChecklist.get("USER_TYPE"));
						
						lstNodeAccountUpdate.add(node);
						if (!lstNodeAccountUpdate.isEmpty()
								&& lstNodeAccountUpdate.size() >= 1000) {
							new NodeAccountServiceImpl().saveOrUpdate(lstNodeAccountUpdate);
							lstNodeAccountUpdate = new ArrayList<NodeAccount>();
							Thread.sleep(5000);
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
	            } // end loop for
	            
	            if (!lstNodeAccountUpdate.isEmpty()) {
					new NodeAccountServiceImpl().saveOrUpdate(lstNodeAccountUpdate);
				}
			}
				
			logger.info(">>>>>>>>>>>>>>>> finish synchronized database node");
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} 
	}
	
	public void syncAccountDb() {
		try {
			logger.info(">>>>>>>>>>>>>> Starting synchronized account database node");
			
			JsonResponseBO jsonData = getDataJson(GET_DB_USER_FOR_AAM);
			if (jsonData != null) {
				
				JSONParser pa = new JSONParser();
	            JSONObject objRes = (JSONObject) pa.parse(jsonData.getDataJson());
	            JSONArray arrayChecklist = (JSONArray) objRes.get(Config.DATA_FIELD_NAME);
	            
	            /*
				 * Lay danh sach cac node server
				 */
				Map<String, Object> filters = new HashMap<String, Object>();
				filters.put("accountType", APP_TYPE.DATABASE.value);
				List<NodeAccount> lstAccNodeOld = new NodeAccountServiceImpl().findListExac(filters, null);
				Map<String, NodeAccount> mapAccountNodeOld = new HashMap<String, NodeAccount>();
				if (lstAccNodeOld != null) {
					for (NodeAccount node : lstAccNodeOld) {
						mapAccountNodeOld.put(node.getServerId() + "_" + node.getUsername() + "_" + APP_TYPE.DATABASE.value, node);
					}
				}
				
				JSONObject objChecklist;
		        NodeAccount node;
		        List<NodeAccount> lstNodeAccountUpdate = new ArrayList<NodeAccount>();
		            
		        for (int i = 0; i < arrayChecklist.size(); i++) {
		        	try {
		        		objChecklist = (JSONObject) arrayChecklist.get(i);
		        		node = new NodeAccount();
		        		if (mapAccountNodeOld.get((Long)objChecklist.get("DB_ID") + "_"
		        				+ (String)objChecklist.get("USERNAME") + "_"
		        				+ APP_TYPE.DATABASE.value) != null) {
		        			
		        			node = mapAccountNodeOld.get((Long)objChecklist.get("DB_ID")
		        					+ "_" + (String)objChecklist.get("USERNAME") + "_"
		        					+ APP_TYPE.DATABASE.value);
		        		}	

		        		node.setServerId((Long)objChecklist.get("DB_ID"));
		        		node.setPassword((String)objChecklist.get("PASSWORD"));
		        		node.setUsername((String)objChecklist.get("USERNAME"));
		        		node.setShell("");
		        		node.setAccountType(APP_TYPE.DATABASE.value);
		        		node.setImpactOrMonitor((Long)objChecklist.get("TYPE"));

		        		lstNodeAccountUpdate.add(node);
		        		if (!lstNodeAccountUpdate.isEmpty() && lstNodeAccountUpdate.size() >= 1000) {
		        			logger.info(">>>> add batch acc db");
		        			new NodeAccountServiceImpl().saveOrUpdate(lstNodeAccountUpdate);
		        			lstNodeAccountUpdate = new ArrayList<NodeAccount>();
		        			Thread.sleep(5000);
		        		}
		        	} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
		         } // end loop for
		         
		         if (!lstNodeAccountUpdate.isEmpty()) {
					logger.info(">>>> add batch acc db");
					new NodeAccountServiceImpl().saveOrUpdate(lstNodeAccountUpdate);
		         }
				logger.info(">>>>>>>>>>>>>>>> finish synchronized database node");
			}
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
			AuthorityBO author = new AuthorityBO(bundle.getString("password_webservice_database_server_qltn"), 1L, bundle.getString("user_webservice_database_server_qltn"));
			
//			IimServices_PortType iimPortType = service.getIimServicesPort(new URL("http://10.58.15.114:8888/IIMSERVICES/IimServices"));
//			AuthorityBO author = new AuthorityBO("K4m7HMHP71MdfZQQzuatsPWeHPNaQtRG", 1, "t2PEDleyKBSfBkOYABmNGQ==");
			
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
			new SyncServer().syncAllDevice();
//			new SyncServer().syncAllDevice("jdbc:oracle:thin:@10.60.6.177:1521/qlts", "qltn", "qL7n1234");
			System.out.println(PasswordEncoder.decrypt("yaWVXaH9hP4nfKE6bzaGyg=="));
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
