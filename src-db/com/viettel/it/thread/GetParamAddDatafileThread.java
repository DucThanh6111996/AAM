package com.viettel.it.thread;

import com.viettel.bean.ResultGetAccount;
import com.viettel.it.model.*;
import com.viettel.it.object.ParamAddPartitionObj;
import com.viettel.it.object.TbsNodeObj;
import com.viettel.it.persistence.DbParamDatafileServiceImpl;
import com.viettel.it.persistence.NodeServiceImpl;
import com.viettel.it.persistence.ParamInputServiceImpl;
import com.viettel.it.persistence.ParamValueServiceImpl;
import com.viettel.it.util.*;
import com.viettel.passprotector.PassProtector;
import com.viettel.util.AppConfig;
import com.viettel.util.Constant;
import com.viettel.it.util.SecurityService;
import com.viettel.util.SessionUtil;
import org.joda.time.DateTime;
import org.json.simple.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

public class GetParamAddDatafileThread extends GetParamThread {

	public static final String LOCAL_DATAFILE_TYPE = "LOCAL";
	public static final String ASM_DATAFILE_TYPE = "ASM";
	
	public GetParamAddDatafileThread(FlowRunAction flowRunAction,
                                     Node databaseNode, NodeRun nodeRun, int cycleMonths,
                                     String urlQltn, String usernameQltn, String passwordQltn, long keySession,Logger logger,
									 Map<String, String> mapConfigSecurity, Map<String, ResultGetAccount> mapPassGet) {
		super(flowRunAction, databaseNode, nodeRun, cycleMonths, urlQltn, usernameQltn,
				passwordQltn, keySession,logger,mapConfigSecurity,mapPassGet);
		// TODO Auto-generated constructor stub
	}
	
//	public GetParamAddDatafileThread(FlowRunAction flowRunAction,
//			Node databaseNode, NodeRun nodeRun, int cycleMonths,
//			String urlQltn, String usernameQltn, String passwordQltn, long keySession) {
//		super(flowRunAction, databaseNode, nodeRun, cycleMonths, urlQltn,
//				usernameQltn, passwordQltn, keySession);
//	}

	protected static final Logger logger = LoggerFactory.getLogger(GetParamAddDatafileThread.class);

	@Override
	public void run() {
		logger.info(">>>>>>>>>>>>>>>> START RUN GET MOP ADD DATAFILE DATABASE: " + databaseNode.getNodeIp() + "_virtualIP: " + databaseNode.getNodeIpVirtual());
		try {

			JSONObject jsonDatafile = getDatafileParam();

			NodeAccount accMonitor = getAccount(null, databaseNode.getServerId(), Config.APP_TYPE.DATABASE.value, true, 2l);
			if (accMonitor == null) {
				logger.error("ERROR GET ACCOUNT MONITOR FOR NODE: " + databaseNode.getNodeIp());
				return;
			}
			
			// get and update datafile to database
			getDatafiles(accMonitor, jsonDatafile);
			
			// get tablespace
			List<String> lstTablespace = getTablespace(accMonitor);
			
			if (lstTablespace == null || lstTablespace.isEmpty()) {
				logger.error("ERROR LIST TABLESPACE VALUE IS EMPTY");
				return;
			} else {
				for(String name : lstTablespace) {
					logger.info(name);
				}
			}
			
			DbParamDatafile paramDatafile = getLstDbParamDatafies();
			if (paramDatafile == null) {
				logger.error("ERROR GET DB_PARAM_DATAFILE FROM " + databaseNode.getNodeIp());
				return;
			} else {
				logger.info(paramDatafile.getDatafileDir() + "---" + paramDatafile.getDirSize());
			}
			
			LinkedHashMap<String, List<String>> mapDatafileTbsName = checkDatafileName(lstTablespace, paramDatafile, accMonitor);
			if (!mapDatafileTbsName.isEmpty()) {
				for (Map.Entry<String, List<String>> entry : mapDatafileTbsName.entrySet()) {
					for (int i = 0; i < entry.getValue().size(); i++) {
						logger.info(entry.getKey() + "= = =" + entry.getValue().get(i));
					}
					
				}
				// save param to complete build add tablespace command sql
				saveParamVal(mapDatafileTbsName, paramDatafile);
			}	
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {

			// delete from param_datafile
			try {
				Map<String, Object> filters = new HashMap<String, Object>();
				filters.put("dbId", databaseNode.getServerId());
				filters.put("dbName", databaseNode.getServiceName());

				List<DbParamDatafile> lstDbParam = new DbParamDatafileServiceImpl().findListExac(filters, null);
				if (lstDbParam != null && !lstDbParam.isEmpty()) {
					new DbParamDatafileServiceImpl().delete(lstDbParam);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			logger.info(">>>>>>> finish add datafile node: " + databaseNode.getNodeIp());
			try {
				int currThreadRuning = ManagerThreadGetParamIns.getInstance().getMapSessionThreadStatus().get(keySession).decrementAndGet();
				if (currThreadRuning == 0) {
					setFinishBuildDT(flowRunAction);
				}
			} catch (Exception e2) {
				logger.error(e2.getMessage(), e2);
			}
		}
	}
	
	
	private void saveParamVal(Map<String, List<String>> mapDatafileTbsName, DbParamDatafile paramDatafile) {
		if (mapDatafileTbsName != null && !mapDatafileTbsName.isEmpty() && paramDatafile != null) {
			List<ParamValue> lstParamValSave = new ArrayList<>();
			Map<String, Object> filters = new HashMap<String, Object>();
			Map<String, String> orders = new HashMap<>();
			orders.put("paramCode", "ASC");
			
			ParamInput paramCheckTablespaceName = null;
			ParamInput paramCheckFileName = null;
			ParamInput paramAddTbsName = null;
			ParamInput paramAddFilename = null;
			ParamInput paramAddDatafileSize = null;
			ParamInput paramAddExtendOption = null;
			
			try {
				filters.put("paramCode", Config.ADD_DTF_CHECK_TBS_NAME);
				paramCheckTablespaceName = new ParamInputServiceImpl().findList(filters, orders).get(0);
				
				filters.put("paramCode", Config.ADD_DTF_CHECK_FILE_NAME);
				paramCheckFileName = new ParamInputServiceImpl().findList(filters, orders).get(0);
				
				filters.put("paramCode", Config.ADD_DTF_TBS_NAME);
				paramAddTbsName = new ParamInputServiceImpl().findListExac(filters, null).get(0);
				
				filters.put("paramCode", Config.ADD_DTF_FILE_NAME);
				paramAddFilename = new ParamInputServiceImpl().findListExac(filters, null).get(0);
				
				filters.put("paramCode", Config.ADD_DTF_DATAFILE_SIZE);
				paramAddDatafileSize = new ParamInputServiceImpl().findListExac(filters, null).get(0);
				
				filters.put("paramCode", Config.ADD_DTF_EXTEND_OPTION);
				paramAddExtendOption = new ParamInputServiceImpl().findListExac(filters, null).get(0);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			ParamValue valCheckTablespaceName = null;
			ParamValue valCheckFilename = null;
			ParamValue valAddTableName = null;
			ParamValue valAddFilename = null;
			ParamValue valAddDatafileSize = null;
			ParamValue valAddExtendOption = null;
			
			StringBuilder strValCheckTablespaceName = new StringBuilder();
			StringBuilder strValCheckFilename = new StringBuilder();
			StringBuilder strValAddTableName = new StringBuilder();
			StringBuilder strValAddFilename = new StringBuilder();
			StringBuilder strValAddDatafileSize = new StringBuilder();
			StringBuilder strValAddExtendOption = new StringBuilder();
			
			try {
				List<String> lstDatafile;
				for (Map.Entry<String, List<String>> entry : mapDatafileTbsName.entrySet()) {
					lstDatafile = entry.getValue();
					for (int i = 0; i < lstDatafile.size(); i++) {
						strValCheckTablespaceName.append(entry.getKey()).append(";");
						strValCheckFilename.append(lstDatafile.get(i)).append(";");
						strValAddTableName.append(entry.getKey()).append(";");
						strValAddFilename.append(lstDatafile.get(i)).append(";");
						strValAddDatafileSize.append(paramDatafile.getDatafileSize()).append(";");
						if ("ON".equalsIgnoreCase(paramDatafile.getAutoExtend().trim())) {
							strValAddExtendOption.append(" AUTOEXTEND ON NEXT "
									.concat(paramDatafile.getInitialSize()).concat(" MAXSIZE ").concat(paramDatafile.getMaxSize()).concat(";"));
						} else {
							strValAddExtendOption.append(" ;");
						}
					} // end sub loop for
				} // end loop for lstParamValObj

				if (paramCheckTablespaceName != null) {
					valCheckTablespaceName = new ParamValue(paramCheckTablespaceName, paramCheckTablespaceName.getParamCode(), new Date(),
                            strValCheckTablespaceName.toString().endsWith(";") ? strValCheckTablespaceName.substring(0, strValCheckTablespaceName.length() - 1) : strValCheckTablespaceName.toString(), nodeRun);
				}

				if (paramCheckFileName != null) {
					valCheckFilename = new ParamValue(paramCheckFileName, paramCheckFileName.getParamCode(), new Date(),
                            strValCheckFilename.toString().endsWith(";") ? strValCheckFilename.substring(0, strValCheckFilename.length() - 1) : strValCheckFilename.toString(), nodeRun);
				}

				if (paramAddTbsName != null) {
					valAddTableName = new ParamValue(paramAddTbsName, paramAddTbsName.getParamCode(), new Date(),
                            strValAddTableName.toString().endsWith(";") ? strValAddTableName.substring(0, strValAddTableName.length() - 1) : strValAddTableName.toString(), nodeRun);
				}

				if (paramAddFilename != null) {
					valAddFilename = new ParamValue(paramAddFilename, paramAddFilename.getParamCode(), new Date(),
                            strValAddFilename.toString().endsWith(";") ? strValAddFilename.substring(0, strValAddFilename.length() - 1) : strValAddFilename.toString(), nodeRun);
				}

				if (paramAddDatafileSize != null) {
					valAddDatafileSize = new ParamValue(paramAddDatafileSize, paramAddDatafileSize.getParamCode(), new Date(),
							strValAddDatafileSize.toString().endsWith(";") ? strValAddDatafileSize.substring(0, strValAddDatafileSize.length() - 1) : strValAddDatafileSize.toString(), nodeRun);
				}

				if (paramAddExtendOption != null) {
					valAddExtendOption = new ParamValue(paramAddExtendOption, paramAddExtendOption.getParamCode(), new Date(),
							strValAddExtendOption.toString().endsWith(";") ? strValAddExtendOption.substring(0, strValAddExtendOption.length() - 1) : strValAddExtendOption.toString(), nodeRun);
				}

				lstParamValSave.add(valCheckTablespaceName);
				lstParamValSave.add(valCheckFilename);
				lstParamValSave.add(valAddTableName);
				lstParamValSave.add(valAddFilename);
				lstParamValSave.add(valAddDatafileSize);
				lstParamValSave.add(valAddExtendOption);
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			
			try {
				// Luu param values
				new ParamValueServiceImpl().saveOrUpdate(lstParamValSave);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	private LinkedHashMap<String, List<String>> checkDatafileName(List<String> lstTablespace,
                                                                  DbParamDatafile paramDatafile, NodeAccount accMonitor) {
		LinkedHashMap<String, List<String>> mapDatafileTbsNameValided = new LinkedHashMap<>();
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmSS");
			String curDate = df.format(new Date());
			Map<String, Integer> mapTbsNumOfDatafile = new HashMap<>();
			for(TbsNodeObj tbs : nodeRun.getNode().getLstTbsObj()) {
				mapTbsNumOfDatafile.put(tbs.getTbsName(), tbs.getNumOfDatafile());
			}
			LinkedHashMap<String, List<String>> mapDatafileTbsName = new LinkedHashMap<>();
			List<String> lstDatafileNameCheck;
			for (int i = 0; i < lstTablespace.size(); i++) {
				lstDatafileNameCheck = new ArrayList<>();
				if (paramDatafile.getDirDetail().startsWith("+")) {
					
					for (int j = 0; j < mapTbsNumOfDatafile.get(lstTablespace.get(i)); j++) {
						lstDatafileNameCheck.add("+".concat(paramDatafile.getDatafileDir()));
					}
				} else {
					for (int j = 0; j < mapTbsNumOfDatafile.get(lstTablespace.get(i)); j++) {
						lstDatafileNameCheck.add(paramDatafile.getDirDetail().concat(lstTablespace.get(i)).concat("_").concat(curDate).concat("_" + (j + 1)).concat(".dbf"));
					}
				}
				mapDatafileTbsName.put(lstTablespace.get(i), lstDatafileNameCheck);
			} // end loop for lstTablespace
			
			if (!mapDatafileTbsName.isEmpty()) {

				Connection monitorConn = getConnection(databaseNode.getJdbcUrl(), accMonitor.getUsername(), accMonitor.getPassword());
				if (monitorConn != null) {
					
					PreparedStatement prepare;
					List<String> lstDatafileNameValidated ;
					List<String> lstDatafileCheck ;
					
					for (Map.Entry<String, List<String>> entry : mapDatafileTbsName.entrySet()) {
						
						lstDatafileCheck = entry.getValue();
						if (lstDatafileCheck == null || lstDatafileCheck.isEmpty()) {
							continue;
						}
						boolean checkIsAsm = false;
						lstDatafileNameValidated = new ArrayList<>();
						for (int i = 0; i < lstDatafileCheck.size(); i++) {
							if (checkIsAsm) {
								break;
							}
							prepare = monitorConn.prepareStatement(Config.CHECK_EXIST_DATAFILE_NAME);
							prepare.setString(1, lstDatafileCheck.get(i));
							prepare.setFetchSize(1000);
							ResultSet rs = prepare.executeQuery();
		
							// get data
							while (rs.next()) {
								if (lstDatafileCheck.get(i).startsWith("+")) {
									if (rs.getInt("data_return") == 0) {
										lstDatafileNameValidated.addAll(lstDatafileCheck);
									} 
									checkIsAsm = true;
								} else if (rs.getInt("data_return") == 0) {
									lstDatafileNameValidated.add(lstDatafileCheck.get(i));
								}
								break;
							}
							
							rs.close();
							prepare.close();
							
						} // end loop sub for
						
						if (!lstDatafileNameValidated.isEmpty()) {
							mapDatafileTbsNameValided.put(entry.getKey(), lstDatafileNameValidated);
						}
						
					} // end loop for
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return mapDatafileTbsNameValided;
	}
	
	private Map<String, String> mapDatafileTablespace(List<String> lstTablespaceName, NodeAccount monitorAcc) {
		Map<String, String> mapDatafileTablespace = new HashMap<>();
		if (lstTablespaceName != null && monitorAcc != null) {
			Connection monitorConn = null;
			try {
				monitorConn = getConnection(databaseNode.getJdbcUrl(), monitorAcc.getUsername(), monitorAcc.getPassword());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			
		}
		return mapDatafileTablespace;
	}
	
	private DbParamDatafile getLstDbParamDatafies() {
		DbParamDatafile paramDatafile= null;
		Connection conn = null;
		try {
			Map<String, Object> paramlist = new HashMap<String, Object>();
			paramlist.put("dbId1", databaseNode.getServerId());
			paramlist.put("dbId2", databaseNode.getServerId());
			paramlist.put("dbName1", databaseNode.getServiceName());
			paramlist.put("dbName2", databaseNode.getServiceName());
			
			List<?> lstValReturn = new DbParamDatafileServiceImpl().findListSQLWithMapParameters(null, Config.GET_MAX_DATAFILE_INFO, -1, -1, paramlist);
			for (Object object : lstValReturn) {
				Object[] cols = (Object[]) object;
				paramDatafile = new DbParamDatafile(
						(String)cols[3], 
						((BigDecimal) cols[0]).longValue(), 
						(String)cols[1], 
						(String)cols[2], 
						(String)cols[4], 
						(String)cols[5], 
						((BigDecimal) cols[6]).intValue(), 
						(String)cols[7], 
						(String)cols[8], 
						(String)cols[9], 
						(String)cols[10], 
						null
					);
				break;
			}
			
			if (paramDatafile != null) {
				conn = getConnection(urlQltn, usernameQltn, passwordQltn);
				if (conn != null) {
					PreparedStatement prepare;
					prepare = conn.prepareStatement(Config.SELECT_AUTO_EXTEND_DB_ID_INFOS);
					prepare.setFetchSize(100);
					prepare.setLong(1, paramDatafile.getDbId());
					ResultSet rs = prepare.executeQuery();

					while (rs.next()) {
						paramDatafile.setAutoExtend(rs.getString("AUTO_EXTEND") != null ? rs.getString("AUTO_EXTEND") : null);
						paramDatafile.setDatafileSize(rs.getString("DATAFILE_SIZE") != null ? rs.getString("DATAFILE_SIZE") : null);
						paramDatafile.setInitialSize(rs.getString("INITIAL_SIZE") != null ? rs.getString("INITIAL_SIZE") : null);
						paramDatafile.setMaxSize(rs.getString("MAX_SIZE") != null ? rs.getString("MAX_SIZE") : null);
						break;
					}
					
					rs.close();
					prepare.close();
				}
			} else {
				logger.error("get paramDatafile null from ");
				return null;
			}
			
			// Kiem tra thong tin AUTO_EXTEND, DATAFILE_SIZE, INITIAL_SIZE, MAX_SIZE tu QLTN
			if (paramDatafile.getDatafileSize() == null) {
				logger.error("ERROR: DATAFILE_SIZE NULL: " + databaseNode.getNodeIp() + "_DB_NAME: " + paramDatafile.getDbName());
				paramDatafile = null;
			} else if ("ON".equals(paramDatafile.getAutoExtend().toUpperCase())) {
				if (paramDatafile.getInitialSize() == null
						|| paramDatafile.getMaxSize() == null) {
					logger.error("ERROR: INITIAL_SIZE OR MAX_SIZE NULL: " + databaseNode.getNodeIp() + "_DB_NAME: " + paramDatafile.getDbName());
					paramDatafile = null;
				}
			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			paramDatafile = null;
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e2) {
					logger.error(e2.getMessage(), e2);
				}
			}
		}
		return paramDatafile;
	}
	
//	private void getDatafiles(NodeAccount accMonitor) {
//
//		if (accMonitor != null && getUrl(databaseNode) != null) {
//			Connection monitorConnect = null;
//			try {
//				monitorConnect = getConnection(getUrl(databaseNode), accMonitor.getUsername(), PasswordEncoder.decrypt(accMonitor.getPassword()));
//				if (monitorConnect != null) {
//					PreparedStatement prepare;
//					prepare = monitorConnect.prepareStatement(Config.QUERY_GET_DATAFILE_DETAIL_FROM_DBA_DATA_FILE);
//					prepare.setFetchSize(1000);
//					ResultSet rs = prepare.executeQuery();
//
//					// get data
//					List<String> dirsDetail = new ArrayList<>();
//					while (rs.next()) {
//						dirsDetail.add(rs.getString("datafile"));
//					}
//					prepare.close();
//
//					String datafileDir = null;
//					Map<String, String> mapDatafileDirs = new HashMap<>();
//					Map<String, String> mapDatafileType = new HashMap<>();
//					boolean isAsm = false;
//					for (String dirDetail : dirsDetail) {
//						if (dirDetail.startsWith("+")) {
//							isAsm = true;
//							datafileDir = getDatafileDir(dirDetail);
//							mapDatafileType.put(datafileDir, ASM_DATAFILE_TYPE);
//						} else {
//							datafileDir = dirDetail;
//							if (mapDatafileType.get(datafileDir) == null) {
//								boolean isMatch = false;
//								for(Map.Entry<String, String> entry : mapDatafileType.entrySet()) {
//									if (entry.getKey().startsWith(datafileDir)) {
//										isMatch = true;
//									}
//								} // end loop for
//
//								if (!isMatch) {
//									mapDatafileType.put(datafileDir, LOCAL_DATAFILE_TYPE);
//								}
//							}
//						}
//
//						mapDatafileDirs.put(dirDetail, datafileDir);
//					} // end loop for dirsDetail
//
//					Map<String, Integer> mapfullMaxDatasize = new HashMap<>();
//					if (isAsm) {
//						mapfullMaxDatasize = getMaxDatasizeAsm(mapDatafileDirs, monitorConnect);
//					} else {
//						Map<String, Integer> mapMaxDatasize = getMaxDatasizeLocal(monitorConnect);
//						for(Map.Entry<String, String> entry : mapDatafileDirs.entrySet()) {
//
//							int maxLength  = 0;
//							for (Map.Entry<String, Integer> subEntry : mapMaxDatasize.entrySet()) {
//								if (entry.getKey().trim().startsWith(subEntry.getKey().trim())
//										&& !"/".equalsIgnoreCase(subEntry.getKey().trim())
//										&& subEntry.getKey().length() > maxLength) {
//									maxLength = subEntry.getKey().length();
//									mapfullMaxDatasize.put(entry.getKey(), subEntry.getValue());
//								}
//							}
//						}
//					}
//
//					// update param tablespace to database
//					updateParamDataFile(mapDatafileDirs, mapfullMaxDatasize);
//				}
//			} catch(Exception e) {
//				logger.error(e.getMessage(), e);
//			} finally {
//				try {
//					if (monitorConnect != null) {
//						monitorConnect.close();
//					}
//				} catch (Exception e2) {
//					logger.error(e2.getMessage(), e2);
//					// TODO: handle exception
//				}
//			}
//		}
//	}

	private void getDatafiles(NodeAccount accMonitor, JSONObject jsonDatafile) {

		if (accMonitor != null && getUrl(databaseNode) != null) {
			Connection monitorConnect = null;
			try {
				List<String> dirsDetail = new ArrayList<>();
				monitorConnect = getConnection(databaseNode.getJdbcUrl(), accMonitor.getUsername(), accMonitor.getPassword());
				if (((String) jsonDatafile.get("DATAFILE_DIR")) != null
						&& !((String) jsonDatafile.get("DATAFILE_DIR")).trim().isEmpty()) {
					dirsDetail.add(((String) jsonDatafile.get("DATAFILE_DIR")).trim());
				} else {
					if (monitorConnect != null) {
						PreparedStatement prepare;
						prepare = monitorConnect.prepareStatement(Config.QUERY_GET_DATAFILE_DETAIL_FROM_DBA_DATA_FILE);
						prepare.setFetchSize(1000);
						ResultSet rs = prepare.executeQuery();

						// get dirsDetail
						while (rs.next()) {
							dirsDetail.add(rs.getString("datafile"));
						}
						prepare.close();
					}
				}

				String datafileDir = null;
				Map<String, String> mapDatafileDirs = new HashMap<>();
				Map<String, String> mapDatafileType = new HashMap<>();
				boolean isAsm = false;
				for (String dirDetail : dirsDetail) {
					if (dirDetail.startsWith("+")) {
						isAsm = true;
						datafileDir = getDatafileDir(dirDetail);
						mapDatafileType.put(datafileDir, ASM_DATAFILE_TYPE);
					} else {
						datafileDir = dirDetail;
						if (mapDatafileType.get(datafileDir) == null) {
							boolean isMatch = false;
							for (Map.Entry<String, String> entry : mapDatafileType.entrySet()) {
								if (entry.getKey().startsWith(datafileDir)) {
									isMatch = true;
								}
							} // end loop for

							if (!isMatch) {
								mapDatafileType.put(datafileDir, LOCAL_DATAFILE_TYPE);
							}
						}
					}

					mapDatafileDirs.put(dirDetail, datafileDir);
				} // end loop for dirsDetail

				Map<String, Integer> mapfullMaxDatasize = new HashMap<>();
				if (isAsm) {
					mapfullMaxDatasize = getMaxDatasizeAsm(mapDatafileDirs, monitorConnect);
				} else {
					Map<String, Integer> mapMaxDatasize = getMaxDatasizeLocal();
					for (Map.Entry<String, String> entry : mapDatafileDirs.entrySet()) {

						int maxLength = 0;
						for (Map.Entry<String, Integer> subEntry : mapMaxDatasize.entrySet()) {
							if (entry.getKey().trim().startsWith(subEntry.getKey().trim())
									&& !"/".equalsIgnoreCase(subEntry.getKey().trim())
									&& subEntry.getKey().length() > maxLength) {
								maxLength = subEntry.getKey().length();
								mapfullMaxDatasize.put(entry.getKey(), subEntry.getValue());
							}
						}
					}
				}

				// update param tablespace to database
				updateParamDataFile(mapDatafileDirs, mapfullMaxDatasize, jsonDatafile);

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				try {
					if (monitorConnect != null) {
						monitorConnect.close();
					}
				} catch (Exception e2) {
					logger.error(e2.getMessage(), e2);
				}
			}
		}
	}
	
//	private void updateParamDataFile(Map<String, String> datafileDirs, Map<String, Integer> mapMaxDatasize) {
//		if (datafileDirs != null && mapMaxDatasize != null) {
//			DbParamDatafile paramDatafile ;
//			List<DbParamDatafile> lstParamDatafile = new ArrayList<>();
//			Map<String, Integer> mapKeyParamDatafile = new HashMap<>();
//			for (Map.Entry<String, Integer> entry : mapMaxDatasize.entrySet()) {
//				logger.info(entry.getKey() + "===" + entry.getValue());
//			}
//			for (Map.Entry<String, String> entry : datafileDirs.entrySet()) {
//				try {
//					if (mapKeyParamDatafile.get(entry.getKey().concat("#")
//							.concat(databaseNode.getServiceName()).concat("#")
//							.concat(databaseNode.getServerId()+"")) != null) {
//						continue;
//					} else {
//						mapKeyParamDatafile.put(entry.getKey().concat("#")
//								.concat(databaseNode.getServiceName()).concat("#")
//								.concat(databaseNode.getServerId()+""), 1);
//					}
//
//					Map<String, Object> filters = new HashMap<>();
//					filters.put("dirDetail", entry.getKey());
//					filters.put("dbName", databaseNode.getServiceName());
//					filters.put("dbId", databaseNode.getServerId());
//
//					try {
//						paramDatafile = new DbParamDatafileServiceImpl().findListExac(filters, null).get(0);
//					} catch (Exception e) {
//						logger.error(e.getMessage(), e);
//						paramDatafile = null;
//					}
//
//					if (paramDatafile != null && mapMaxDatasize.get(entry.getKey()) != null) {
//						paramDatafile.setDirSize(mapMaxDatasize.get(entry.getKey()));
//						paramDatafile.setUpdateTime(new Date());
//						lstParamDatafile.add(paramDatafile);
//					} else if (paramDatafile == null) {
//						paramDatafile = new DbParamDatafile();
//						paramDatafile.setDatafileDir(entry.getValue());
//						paramDatafile.setDbId(databaseNode.getServerId());
//						paramDatafile.setDbIp(databaseNode.getNodeIp());
//						paramDatafile.setDbName(databaseNode.getServiceName());
//						paramDatafile.setDirDetail(entry.getKey());
//						paramDatafile.setDirSize(mapMaxDatasize.get(entry.getKey()));
//						paramDatafile.setDirType(entry.getValue().startsWith("+") ? Config.DATAFILE_TYPE.ASM.value : Config.DATAFILE_TYPE.LOCAL.value);
//						paramDatafile.setInserTime(new Date());
//						lstParamDatafile.add(paramDatafile);
//
//
//					}
//				} catch (Exception e) {
//					logger.error(e.getMessage(), e);
//				}
//			} // end loop for
//
//			// insert and update data
//			try {
//				new DbParamDatafileServiceImpl().saveOrUpdate(lstParamDatafile);
//			} catch (Exception e) {
//				logger.error(e.getMessage(), e);
//			}
//		}
//	}

	private void updateParamDataFile(Map<String, String> datafileDirs, Map<String, Integer> mapMaxDatasize, JSONObject jsonDatafile) {
		if (datafileDirs != null && mapMaxDatasize != null) {
			DbParamDatafile paramDatafile = null;
			List<DbParamDatafile> lstParamDatafile = new ArrayList<>();
			Map<String, Integer> mapKeyParamDatafile = new HashMap<>();

			for (Map.Entry<String, String> entry : datafileDirs.entrySet()) {
				try {
					if (mapKeyParamDatafile.get(entry.getKey().concat("#")
							.concat(databaseNode.getServiceName()).concat("#")
							.concat(databaseNode.getServerId() + "")) != null) {
						continue;
					} else {
						mapKeyParamDatafile.put(entry.getKey().concat("#")
								.concat(databaseNode.getServiceName()).concat("#")
								.concat(databaseNode.getServerId() + ""), 1);
					}

					Map<String, Object> filters = new HashMap<>();
					filters.put("dirDetail", entry.getKey());
					filters.put("dbName", databaseNode.getServiceName());
					filters.put("dbId", databaseNode.getServerId());

					try {
						List<DbParamDatafile> lstParamDatafileDb = new DbParamDatafileServiceImpl().findListExac(filters, null);
						if (lstParamDatafileDb != null && !lstParamDatafileDb.isEmpty()) {
							paramDatafile = lstParamDatafileDb.get(0);
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						paramDatafile = null;
					}

					if (paramDatafile != null && mapMaxDatasize.get(entry.getKey()) != null) {
						paramDatafile.setDirSize(mapMaxDatasize.get(entry.getKey()));
						paramDatafile.setUpdateTime(new Date());
						lstParamDatafile.add(paramDatafile);
					} else if (paramDatafile == null) {
						paramDatafile = new DbParamDatafile();
						paramDatafile.setDatafileDir(entry.getValue());
						paramDatafile.setDbId(databaseNode.getServerId());
						paramDatafile.setDbIp(databaseNode.getNodeIp());
						paramDatafile.setDbName(databaseNode.getServiceName());
						paramDatafile.setDirDetail(entry.getKey());
						paramDatafile.setDirSize(mapMaxDatasize.get(entry.getKey()));
						paramDatafile.setDirType(entry.getValue().startsWith("+") ? Config.DATAFILE_TYPE.ASM.value : Config.DATAFILE_TYPE.LOCAL.value);
						paramDatafile.setInserTime(new Date());
						lstParamDatafile.add(paramDatafile);


					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				}
			} // end loop for

			// insert and update data
			try {
				logger.info("lstParamDatafile size: " + lstParamDatafile.size());
				new DbParamDatafileServiceImpl().saveOrUpdate(lstParamDatafile);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	Map<String, Integer> getMaxDatasizeAsm(Map<String, String> mapDatafileDirs, Connection monitorConn) {
		Map<String, Integer> mapMaxDatasize = new HashMap<>();
		for (Map.Entry<String, String> entry : mapDatafileDirs.entrySet()) {
			PreparedStatement prepare = null;
			try {
				prepare = monitorConn.prepareStatement(Config.GET_MAX_SIZE_DATAFILE_ASM_TYPE);
				prepare.setFetchSize(1000);
				prepare.setString(1, entry.getValue());
				ResultSet rs = prepare.executeQuery();

				// get data
				String maxSize = null;
				while (rs.next()) {
					maxSize = rs.getString("max_size");
					break;
				} 
				
				if (maxSize != null) {
					mapMaxDatasize.put(entry.getKey(), Integer.valueOf(maxSize));
				}

				prepare.close();
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				try {
					if (prepare != null) {
						prepare.close();
					}
				} catch (Exception e2) {
					logger.error(e2.getMessage(), e2);
				}
			}
		} // end loop for 
		return mapMaxDatasize;
	}
	
	Map<String, Integer> getMaxDatasizeLocal() {
		Map<String, Integer> mapMaxDatasize = new HashMap<>();
			
		try {
			// get node server of database node
			Map<String, Object> filters = new HashMap<>();
			filters.put("nodeIp", databaseNode.getNodeIp());
			filters.put("active", Constant.status.active);
			List<Node> lstServerNode = new NodeServiceImpl().findListExac(filters, null);
			if (lstServerNode == null || lstServerNode.isEmpty()) {
				logger.error(">>>> ERROR GET SERVER NODE IP: " + databaseNode.getNodeIp());
				return mapMaxDatasize;
			}

			NodeAccount nodeAcc = getAccount(null, lstServerNode.get(0).getServerId(), Config.APP_TYPE.SERVER.value, false, 2l);
			if (nodeAcc != null) {

				JSchSshUtil ssh = new JSchSshUtil(databaseNode.getNodeIp(),
						Config.DEFAULT_SSH_OS_PORT, nodeAcc.getUsername(),
						nodeAcc.getPassword(),
						null, null, 60000,
						false, "N/A", "N/A", "N/A");

				if (ssh.connect()) {
					Result log = ssh.sendLineWithTimeOutAdvance(
							Config.GET_STORATE_DATA_SIZE_COMMAND, 60000);
					if (log.getResult() != null && !log.getResult().trim().isEmpty()) {
						mapMaxDatasize = getMapDirDatasize(log.getResult());
					}
				}
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		
		return mapMaxDatasize;
	}
	
	private Map<String, Integer> getMapDirDatasize(String logVal) {
		Map<String, Integer> mapDirDatasize = new HashMap<>();
		List<String> lstDirInfo = Arrays.asList(logVal.trim().split("\r\n"));
		for (int i = 0; i < lstDirInfo.size(); i++) {
			if (lstDirInfo.get(i).trim().toLowerCase().startsWith("filesystem")) {
				
				List<String> lstDirValInfo ;
				for (int j = i + 1; j < lstDirInfo.size(); j++) {
					if (lstDirInfo.get(j).contains("%")) {
						lstDirValInfo = Arrays.asList(lstDirInfo.get(j).trim().replaceAll(" +", " ").split(" "));
						if (lstDirValInfo != null ) {
							if (lstDirValInfo.size() == 6) {
								mapDirDatasize.put(lstDirValInfo.get(5), Integer.valueOf(lstDirValInfo.get(3)) / (1024 * 1024));
							} else if (lstDirValInfo.size() == 5) {
								mapDirDatasize.put(lstDirValInfo.get(4), Integer.valueOf(lstDirValInfo.get(2)) / (1024 * 1024));
							}
						}
					}
				}
			}
		}
		return mapDirDatasize;
	}
	
	private String getDatafileDir(String dirDetail) {
		if (dirDetail != null && !dirDetail.isEmpty()) {
			List<String> lstVal = Arrays.asList(dirDetail.trim().split("/"));
			if (lstVal != null && !lstVal.isEmpty()) {
				if (lstVal.get(0).startsWith("+")) {
					return lstVal.get(0).substring(1).trim();
				} else if (dirDetail.startsWith("/")){
					return lstVal.get(1);
				} else {
					return lstVal.get(0);
				}
			}
		}
		return null;
	}
	
	private List<String> getTablespace(NodeAccount accMonitor) {
		List<String> lstTablespaceValidated = new ArrayList<>();
		if (nodeRun.getNode().getLstTbsObj() != null && !nodeRun.getNode().getLstTbsObj().isEmpty()) {
			try {
				List<String> lstTablespaceName = new ArrayList<>();
				for (TbsNodeObj tbs : nodeRun.getNode().getLstTbsObj()) {
					lstTablespaceName.add(tbs.getTbsName());
				}
				if (!lstTablespaceName.isEmpty()) {
						lstTablespaceValidated = checkExistTablespace(lstTablespaceName, accMonitor);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return lstTablespaceValidated;
	}
	
	protected List<String> getLstSuffix(int type, DateTime dateTime, int cycle) {
		List<String> suffix = new ArrayList<>();
		try {
			switch (type) {
			case 2: // YYYYMM (201703)
				int year ;
				int month;
				for (int i = 0; i <= cycle; i++) {
					if (dateTime.getMonthOfYear() + i > 12) {
						year = dateTime.getYear() + 1;
					} else {
						year = dateTime.getYear();
					}
					month = (dateTime.getMonthOfYear() + i) % 12;
					if (month == 0) {
						month = 12;
					}
					suffix.add(year + "" + ((month < 10) ? "0" + month : month));
				}
				break;
			case 3: // YYYY (2017) 
				if (dateTime.getMonthOfYear() + cycle > 12) {
					suffix.add(dateTime.getYear() + 1 + "");
				}
				break;
			default:
				break;
		}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return suffix;
	}
	
	private List<String> checkExistTablespace(List<String> lstTablespace, NodeAccount monitorAcc) {
		List<String> lstTablespaceValidated = new ArrayList<>();
		if (lstTablespace != null) {
			try {

				//20181023_tudn_start load pass security
				Connection monitorConnect = getConnection(getUrl(databaseNode), monitorAcc.getUsername(), monitorAcc.getPassword());
				//20181023_tudn_end load pass security
				if (monitorConnect != null) {
					for (String tablespace : lstTablespace) {
						PreparedStatement prepare = monitorConnect.prepareStatement(Config.ADD_PART_QUERY_CHECK_TABLESPACE_NAME);
						prepare.setFetchSize(1000);
						prepare.setString(1, tablespace);
						ResultSet rs = prepare.executeQuery();
						
						while(rs.next()) {
							String result = rs.getString("data_return");
							if ("1".equals(result)) {
								lstTablespaceValidated.add(tablespace);
							}
						} // end loop while
						prepare.close();
					}
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return lstTablespaceValidated;
	}

	
	
	private DateTime increHightValDatetime(DateTime hightValTime, ParamAddPartitionObj paramObj) {
		switch (paramObj.getPartitionType()) {
			case 1:
				hightValTime = hightValTime.plusDays(1);
				break;
			case 2:
				hightValTime = hightValTime.plusMonths(1).withDayOfMonth(1);
				break;
			case 3:
				hightValTime = hightValTime.plusYears(1)
					.withDayOfMonth(1)
					.withMonthOfYear(1);
				break;
	
			default:
				break;
		}
		return hightValTime;
	}
	
	private String getNextPartitionVal(ParamAddPartitionObj paramObj, DateTime dateTime) {
		String partitionNextVal = paramObj.getPartitionPrefix();
		String suffix = getSuffix(paramObj.getPartitionType(), dateTime);
		if (suffix != null) {
			partitionNextVal = partitionNextVal.concat(suffix);
		}
		return partitionNextVal;
	}
	
	private String getNextTablespaceVal(ParamAddPartitionObj paramObj, DateTime dateTime) {
		String tablespaceNextVal = paramObj.getTablespacePrefix();
		String suffix = getSuffix(paramObj.getTablespaceType(), dateTime);
		if (suffix != null) {
			tablespaceNextVal = tablespaceNextVal.concat(suffix);
		} 
		return tablespaceNextVal;
	}
	
	
	
	
	public static void main(String[] args) {
		try {
//			DateTime dt = DateTime.now();
//			dt = dt.plusDays(1);
//			System.out.println(new PasswordEncoder().encrypt("qwertyuiop"));
//			System.out.println(dt.getMonthOfYear());
//			System.out.println(dt.getDayOfWeek());
////			System.out.println(new ParamInputServiceImpl().findList().size());
//			for (int i = 0; i < 10; i++) {
//				try {
//					if (i == 5) {
//						break;
//					}
//				} catch (Exception e) {
//					// TODO: handle exception
//				} finally {
//					System.out.println("ok");
//				}
//				
//			}
			
//			GetParamAddTableSpaceThread t = new GetParamAddTableSpaceThread(null, null, null, 1, null, null, null);
//			System.out.println(t.getDatafileDir("+DATA/vinamilk/"));
//			System.out.println(t.getDatafileDir("/DATA/vinamilk/"));
//			System.out.println(t.getDatafileDir("DATA/vinamilk/"));
			
//			BufferedReader buff = new BufferedReader(new FileReader(new File("D:\\test.txt")));
//			StringBuilder strBuilder = new StringBuilder();
//			String line = null;
//			
//			while((line = buff.readLine()) != null) {
//				strBuilder.append(line).append("\r\n");
//			}
//			
//			GetParamAddTableSpaceThread t = new GetParamAddTableSpaceThread(null, null, null, 1, null, null, null);
//			Map<String, Integer> val = t.getMapDirDatasize(strBuilder.toString());
//			for(Map.Entry<String, Integer> entry : val.entrySet()) {
//				System.out.println(entry.getKey() + "==" + entry.getValue());
//			}
			
//			try {
//				Map<String, Object> paramlist = new HashMap<String, Object>();
//				paramlist.put("dbId1", 1);
//				paramlist.put("dbId2", 1);
//				paramlist.put("dbName1", "a");
//				paramlist.put("dbName2", "a");
//				
//				List<?> lstValReturn = new DbParamDatafileServiceImpl().findListSQLWithMapParameters(null, Config.GET_MAX_DATAFILE_INFO, -1, -1, paramlist);
//				for (Object object : lstValReturn) {
//					Object[] cols = (Object[]) object;
//					System.out.println(Integer.valueOf(((String) cols[8])));
//					DbParamDatafile paramDatafile = new DbParamDatafile(
//							(String)cols[3], 
//							((BigDecimal) cols[0]).longValue(), 
//							(String)cols[1], 
//							(String)cols[2], 
//							(String)cols[4], 
//							(String)cols[5], 
//							((BigDecimal) cols[6]).intValue(), 
//							(String)cols[7], 
//							Integer.valueOf(((String) cols[8])), 
//							(String)cols[9], 
//							(String)cols[10], 
//							Integer.valueOf(((String) cols[11]))
//						);
//					
//					System.out.println(paramDatafile.getDatafileDir());
//				}
//			} catch (Exception e) {
//				logger.error(e.getMessage(), e);
//			}
			
//			Connection monitorConnect = new GetParamThread(null, null, null, 1, null, null, null).getConnection("jdbc:oracle:thin:@10.60.6.177:1521/qlts", "monitor", "qwertyuiop");
//			if (monitorConnect != null) {
//				PreparedStatement prepare = monitorConnect.prepareStatement(Config.GET_TABLESPACE_INFOS);
//				prepare.setFetchSize(1000);
//				prepare.setLong(1, 1);
//				prepare.setString(2, "a");
//				ResultSet rs = prepare.executeQuery();
//			}
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private DateTime getHightValDatetime(Connection conn, ParamAddPartitionObj partitionObj) {
		DateTime hightestDateTime = null;
		logger.info("vao ham get hight value");
		if (conn != null) {
			try {
				logger.info("ok");
				// la account monitor defaul cho toan bo database
				PreparedStatement prepare;
				prepare = conn.prepareStatement(Config.QUERY_GET_HIGHT_VAL_PARTITION);
				prepare.setFetchSize(1000);
				logger.info(partitionObj.getTableOwner() + "___" + partitionObj.getTableName());
				prepare.setString(1, partitionObj.getTableOwner() );
				prepare.setString(2,  partitionObj.getTableName() );
				prepare.setString(3,  partitionObj.getTableName() );
				prepare.setString(4,  partitionObj.getTableOwner() );
				
				ResultSet rs = prepare.executeQuery();
				
				// get data
				String hightVal = null;
				while (rs.next()) {
					hightVal = rs.getString("high_value");
					logger.info("hight value data: " + hightVal);
				}
				prepare.close();
				
				if (hightVal != null) {
					
					// return SYYYY-MM-DD (2017-03-01)
					hightVal = Arrays.asList(hightVal.trim().replaceAll(" +", " ").split(" ")).get(1);
//					Date hightDateVal = df.parse(hightVal);
					
					hightestDateTime = DateTime.parse(hightVal);
					logger.info(">>>>>>>>>> hight value: " + hightVal + "  - - " + hightestDateTime.toString());
				} else {
					logger.error("hightVal null");
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return hightestDateTime;
	}

	

}
