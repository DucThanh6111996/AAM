package com.viettel.it.thread;

import com.viettel.bean.ResultGetAccount;
import com.viettel.it.model.*;
import com.viettel.it.object.ParamAddPartitionObj;
import com.viettel.it.object.ParamValAddPartitionObj;
import com.viettel.it.persistence.ParamInputServiceImpl;
import com.viettel.it.persistence.ParamValueServiceImpl;
import com.viettel.it.util.Config;
import com.viettel.it.util.LogUtils;
import com.viettel.it.util.PasswordEncoder;
import com.viettel.passprotector.PassProtector;
import com.viettel.util.AppConfig;
import com.viettel.util.Constant;
import com.viettel.it.util.SecurityService;
import com.viettel.util.SessionUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class GetParamAddPartitionThread extends GetParamThread {

	public GetParamAddPartitionThread(FlowRunAction flowRunAction,
                                      Node databaseNode, NodeRun nodeRun, int cycleMonths,
                                      String urlQltn, String usernameQltn, String passwordQltn, long keySession,Logger logger,
									  Map<String, String> mapConfigSecurity, Map<String, ResultGetAccount> mapPassGet) {
		super(flowRunAction, databaseNode, nodeRun, cycleMonths, urlQltn, usernameQltn,
				passwordQltn, keySession,logger,mapConfigSecurity,mapPassGet);
		// TODO Auto-generated constructor stub
	}

	protected static final Logger logger = LoggerFactory.getLogger(GetParamAddPartitionThread.class);

	@Override
	public void run() {
		logger.info(">>>>>>>>>>>>>>>> START RUN GET MOP ADD PARTITION DATABASE: " + databaseNode.getNodeIp() + "_virtualIP: " + databaseNode.getNodeIpVirtual());
		getParam();
	}

	private void getParam() {
		
		if (databaseNode != null && cycleMonths >= 0) {
			Connection conn = null;
			List<ParamAddPartitionObj> lstPartitionObj = new ArrayList<ParamAddPartitionObj>();
			try {
				conn = getConnection(urlQltn, usernameQltn, passwordQltn);
				if (conn != null) {
					PreparedStatement prepare;
					prepare = conn.prepareStatement(Config.QUERY_PARAM_ADD_PARTITION);
					prepare.setFetchSize(1000);
					prepare.setLong(1, databaseNode.getServerId());
					ResultSet rs = prepare.executeQuery();

					// get data
					ParamAddPartitionObj obj = null;
					while (rs.next()) {
						try {
							obj = new ParamAddPartitionObj(databaseNode.getServerId(),
									rs.getString("TABLE_NAME").toUpperCase(),
									rs.getString("TABLE_OWNER").toUpperCase(),
									rs.getInt("PARTITION_TYPE"),
									rs.getString("PARTITION_PREFIX").toUpperCase(),
									rs.getInt("TABLESPACE_TYPE"), 
									rs.getString("TABLESPACE_PREFIX").toUpperCase(),
									rs.getInt("STATUS"));
							lstPartitionObj.add(obj);
							
						} catch (Exception e) {
							logger.error(e.getMessage(), e);
							logger.error(e.getMessage());
						}
					} // end loop while
					
					prepare.close();
				} 
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				if (conn != null) {
					try {
						conn.close();
					} catch (Exception e2) {
						logger.error(e2.getMessage(), e2);
					}
				}
			}
			
			/*
			 * Lay thong tin param value cua template add partition
			 */
			try {
				if (!lstPartitionObj.isEmpty()) {
					List<ParamValAddPartitionObj> lstParamVal = buildParam(lstPartitionObj);
					saveParamVal(lstParamVal);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
				
			try {
				logger.info(">>>>>>> finish add partition node: " + databaseNode.getNodeIp());
				int currThreadRuning = ManagerThreadGetParamIns.getInstance().getMapSessionThreadStatus().get(keySession).decrementAndGet();
				if (currThreadRuning == 0) {
					setFinishBuildDT(flowRunAction);
				}
			} catch (Exception e2) {
				logger.error(e2.getMessage(), e2);
			}
		}
	}
	
	private void saveParamVal(List<ParamValAddPartitionObj> lstParamValObj) {
		if (lstParamValObj != null && !lstParamValObj.isEmpty()) {
			logger.info(">>>>>>>>>>>>>> Start save param add partition");
			List<ParamValue> lstParamValSave = new ArrayList<>();
			Map<String, Object> filters = new HashMap<String, Object>();
			Map<String, String> orders = new HashMap<>();
			orders.put("paramCode", "ASC");
			ParamInput paramCheckTablespaceName = null;
			ParamInput paramCheckPartitionName = null;
			ParamInput paramCheckTableOwner = null;
			ParamInput paramCheckTableName = null;
			ParamInput paramAddTableOwner = null;
			ParamInput paramAddTableName = null;
			ParamInput paramAddPartitionName = null;
			ParamInput paramAddPClycle = null;
			ParamInput paramAddtablespaceName = null;
			
			try {
				filters.put("paramCode", Config.ADD_PART_PARAM_CHECK_TABLESPACE_NAME);
				paramCheckTablespaceName = new ParamInputServiceImpl().findList(filters, orders).get(0);
				
				filters.put("paramCode", Config.ADD_PART_PARAM_CHECK_TABLE_OWNER);
				paramCheckTableOwner = new ParamInputServiceImpl().findList(filters, orders).get(0);
				
				filters.put("paramCode", Config.ADD_PART_PARAM_CHECK_TABLENAME);
				paramCheckTableName = new ParamInputServiceImpl().findListExac(filters, null).get(0);
				
				filters.put("paramCode", Config.ADD_PART_PARAM_CHECK_PARTITION_NAME);
				paramCheckPartitionName = new ParamInputServiceImpl().findList(filters, null).get(0);
				
				filters.put("paramCode", Config.ADD_PART_PARAM_ADD_TABLE_OWNER);
				paramAddTableOwner = new ParamInputServiceImpl().findList(filters, null).get(0);
				
				filters.put("paramCode", Config.ADD_PART_PARAM_ADD_TABLE_NAME);
				paramAddTableName = new ParamInputServiceImpl().findList(filters, null).get(0);
				
				filters.put("paramCode", Config.ADD_PART_PARAM_ADD_PARTITION_NAME);
				paramAddPartitionName = new ParamInputServiceImpl().findList(filters, null).get(0);
				
				filters.put("paramCode", Config.ADD_PART_PARAM_ADD_P_CYCLE);
				paramAddPClycle = new ParamInputServiceImpl().findList(filters, null).get(0);
				
				filters.put("paramCode", Config.ADD_PART_PARAM_ADD_TABLESPACE_NAME);
				paramAddtablespaceName = new ParamInputServiceImpl().findList(filters, null).get(0);
				
				logger.info(paramCheckTablespaceName.getParamCode()
						+ "=" + paramCheckTableOwner.getParamCode()
						+ "=" + paramCheckTableName.getParamCode()
						+ "=" + paramCheckPartitionName.getParamCode()
						+ "=" + paramAddTableOwner.getParamCode()
						+ "=" + paramAddTableName.getParamCode()
						+ "=" + paramAddPartitionName.getParamCode()
						+ "=" + paramAddPClycle.getParamCode()
						+ "=" + paramAddtablespaceName.getParamCode());
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}

			ParamValue valCheckTablespaceName = null;
			ParamValue valCheckTableName = null;
			ParamValue valCheckPartitionName = null;
			ParamValue valCheckTableOwner = null;
			ParamValue valAddTableName = null;
			ParamValue valAddTableOwner = null;
			ParamValue valAddPartitionName = null;
			ParamValue valAddPCycle = null;
			ParamValue valAddTablespaceName = null;
			
			StringBuilder strValCheckTablespaceName = new StringBuilder();
			StringBuilder strValCheckTableName = new StringBuilder();
			StringBuilder strValCheckPartitionName = new StringBuilder();;
			StringBuilder strValCheckTableOwner = new StringBuilder();;
			StringBuilder strValAddTableName = new StringBuilder();;
			StringBuilder strValAddTableOwner = new StringBuilder();;
			StringBuilder strValAddPartitionName = new StringBuilder();;
			StringBuilder strValAddPCycle = new StringBuilder();;
			StringBuilder strValAddTablespaceName = new StringBuilder();;
			
			try {
				for (ParamValAddPartitionObj paramValObj : lstParamValObj) {
					
					strValCheckTablespaceName.append(paramValObj.getTablespaceNameCheckSpace()).append(";");
					strValCheckTableName.append(paramValObj.getTableNameCheckPartition()).append(";");
					strValCheckPartitionName.append(paramValObj.getPartitionNameCheckpartition()).append(";");
					strValCheckTableOwner.append(paramValObj.getTableOwnerCheckPartition()).append(";");
					strValAddTableName.append(paramValObj.getTableNameAddPartition()).append(";");
					strValAddTableOwner.append(paramValObj.getTableOwnerAddPartition()).append(";");
					strValAddPartitionName.append(paramValObj.getPartitionNameAddPartition()).append(";");
					strValAddPCycle.append(paramValObj.getCycleAddPartition()).append(";");
					strValAddTablespaceName.append(paramValObj.getTablespaceNameAddPartition()).append(";");
						
				} // end loop for lstParamValObj

				if (paramCheckTablespaceName != null) {
					valCheckTablespaceName = new ParamValue(paramCheckTablespaceName, paramCheckTablespaceName.getParamCode(), new Date(),
                            strValCheckTablespaceName.toString().endsWith(";") ? strValCheckTablespaceName.substring(0, strValCheckTablespaceName.length() - 1) : strValCheckTablespaceName.toString(), nodeRun);
				}
				if (paramCheckTableName != null) {
					valCheckTableName = new ParamValue(paramCheckTableName, paramCheckTableName.getParamCode(), new Date(),
                            strValCheckTableName.toString().endsWith(";") ? strValCheckTableName.substring(0, strValCheckTableName.length() - 1) : strValCheckTableName.toString(), nodeRun);
				}
				if (paramCheckPartitionName != null)
				valCheckPartitionName = new ParamValue(paramCheckPartitionName, paramCheckPartitionName.getParamCode(), new Date(),
						strValCheckPartitionName.toString().endsWith(";") ? strValCheckPartitionName.substring(0, strValCheckPartitionName.length() - 1) : strValCheckPartitionName.toString(), nodeRun);
				if (paramCheckTableOwner != null)
				valCheckTableOwner = new ParamValue(paramCheckTableOwner, paramCheckTableOwner.getParamCode(), new Date(),
						strValCheckTableOwner.toString().endsWith(";") ? strValCheckTableOwner.substring(0, strValCheckTableOwner.length() - 1) : strValCheckTableOwner.toString(), nodeRun);
				if (paramAddTableName != null)
				valAddTableName = new ParamValue(paramAddTableName, paramAddTableName.getParamCode(), new Date(),
						strValAddTableName.toString().endsWith(";") ? strValAddTableName.substring(0, strValAddTableName.length() - 1) : strValAddTableName.toString(), nodeRun);
				if (paramAddTableOwner != null)
				valAddTableOwner = new ParamValue(paramAddTableOwner, paramAddTableOwner.getParamCode(), new Date(),
						strValAddTableOwner.toString().endsWith(";") ? strValAddTableOwner.substring(0, strValAddTableOwner.length() - 1) : strValAddTableOwner.toString(), nodeRun);
				if (paramAddPartitionName != null)
				valAddPartitionName = new ParamValue(paramAddPartitionName, paramAddPartitionName.getParamCode(), new Date(),
						strValAddPartitionName.toString().endsWith(";") ? strValAddPartitionName.substring(0, strValAddPartitionName.length() - 1) : strValAddPartitionName.toString(), nodeRun);
				if (paramAddPClycle != null)
				valAddPCycle = new ParamValue(paramAddPClycle, paramAddPClycle.getParamCode(), new Date(),
						strValAddPCycle.toString().endsWith(";") ? strValAddPCycle.substring(0, strValAddPCycle.length() - 1) : strValAddPCycle.toString(), nodeRun);
				if (paramAddtablespaceName != null)
				valAddTablespaceName = new ParamValue(paramAddtablespaceName, paramAddtablespaceName.getParamCode(), new Date(),
						strValAddTablespaceName.toString().endsWith(";") ? strValAddTablespaceName.substring(0, strValAddTablespaceName.length() - 1) : strValAddTablespaceName.toString(), nodeRun);
					
				lstParamValSave.add(valCheckTablespaceName);
				lstParamValSave.add(valCheckTableName);
				lstParamValSave.add(valCheckPartitionName);
				lstParamValSave.add(valCheckTableOwner);
				lstParamValSave.add(valAddTableName);
				lstParamValSave.add(valAddTableOwner);
				lstParamValSave.add(valAddPartitionName);
				lstParamValSave.add(valAddPCycle);
				lstParamValSave.add(valAddTablespaceName);
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			
			try {
				// Luu param values
				new ParamValueServiceImpl().saveOrUpdate(lstParamValSave);
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
			logger.info(">>>>>>>>>>>>>> Finish save param add Datafile");
		}
	}
	
	private List<ParamValAddPartitionObj> buildParam(List<ParamAddPartitionObj> lstParamObj) {
		logger.info(" vao buildParam");
		List<ParamValAddPartitionObj> lstParamValObj = new ArrayList<ParamValAddPartitionObj>();
		List<ParamValAddPartitionObj> lstParamValOneRecord = new ArrayList<ParamValAddPartitionObj>();
		if (lstParamObj != null) {
			// ket noi toi database node su dung account monitor
			NodeAccount accMonitor = getAccount(null, databaseNode.getServerId(), Config.APP_TYPE.DATABASE.value, true, 2l);
			if (accMonitor != null && getUrl(databaseNode) != null) {
				Connection monitorConnect = null;
				logger.info("account impact name: " + accMonitor.getUsername());
				try {
					monitorConnect = getConnection(getUrl(databaseNode), accMonitor.getUsername(), accMonitor.getPassword());
					for(ParamAddPartitionObj partition : lstParamObj) {
						lstParamValOneRecord = getParamValAddPartition(partition, monitorConnect);
						if (lstParamValOneRecord != null && !lstParamValOneRecord.isEmpty()) {
							lstParamValObj.addAll(lstParamValOneRecord);
						}
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					if (monitorConnect != null) {
						try {
							monitorConnect.close();
						} catch (Exception e2) {
							logger.error(e2.getMessage(), e2);
						}
					}
				}
			} else {
				logger.error("ERROR GET ACCOUNT OR GET URL NODE DATABASE: " + databaseNode.getNodeIp() + "---NODE_NAME: " + databaseNode.getNodeName());
			}
		}
		return lstParamValObj;
	}
	
	private List<ParamValAddPartitionObj> getParamValAddPartition(
            ParamAddPartitionObj paramObj, Connection conn) {
		List<ParamValAddPartitionObj> paramVals = new ArrayList<ParamValAddPartitionObj>();
		List<ParamValAddPartitionObj> paramValsReturn = new ArrayList<ParamValAddPartitionObj>();
		if (paramObj != null) {
			DateTime maxTime = DateTime.now().plusMonths(cycleMonths + 1);
			maxTime = maxTime.withDayOfMonth(1).withHourOfDay(0).withMinuteOfHour(0).withSecondOfMinute(0);
			DateTime hightValTime = getHightValDatetime(conn, paramObj);
			
			if (hightValTime == null) {
				logger.error(">>>>>>>> CANNOT GET HIGHT VALUE FROM TableName: " + paramObj.getTableName() + "===TableOwner: " + paramObj.getTableOwner());
				return null;
			}
			
			String nextPartitionVal;
			String nextTablespaceVal;
			String nextCycleTime;
			if (hightValTime.isBefore(maxTime)) {
				if (hightValTime.getYear() == maxTime.getYear()
						&& hightValTime.getMonthOfYear() == maxTime.getMonthOfYear()
						&& hightValTime.getDayOfMonth() == maxTime.getDayOfMonth()) {
					logger.error("ERROR >>>>>>>>>>>>>>>> server_ip:" + databaseNode.getNodeIp() 
							+ "--tableName:" + paramObj.getTableName()
							+ "--tableOwner:" + paramObj.getTableOwner()
							+ "--partitionType:" + paramObj.getPartitionType()
							+ "--partitionPrefix:" + paramObj.getPartitionPrefix()
							+ "--tablespaceType:" + paramObj.getTablespaceType()
							+ "--tablespacePrefix:" + paramObj.getTablespacePrefix());
					return paramValsReturn;
				}
				
				while(hightValTime.isBefore(maxTime)) {
					
					if (hightValTime.getYear() == maxTime.getYear()
							&& hightValTime.getMonthOfYear() == maxTime.getMonthOfYear()
							&& hightValTime.getDayOfMonth() == maxTime.getDayOfMonth()) {
						break;
					}
					
					nextPartitionVal = getNextPartitionVal(paramObj, hightValTime);
					nextTablespaceVal = getNextTablespaceVal(paramObj, hightValTime);
					
					// incre higth val datetime
					hightValTime = increHightValDatetime(hightValTime, paramObj);
					
					
					nextCycleTime = df.format(hightValTime.toDate());
					paramVals.add(
							new ParamValAddPartitionObj(
									nextTablespaceVal, 
									paramObj.getTableOwner(), 
									paramObj.getTableName(),
									nextPartitionVal, 
									paramObj.getTableOwner(), 
									paramObj.getTableName(), 
									nextPartitionVal, 
									nextCycleTime, 
									nextTablespaceVal)
							);
				}
			} else {
				logger.error("ERROR >>>>>>>>>>>>>>>> server_ip:" + databaseNode.getNodeIp() 
						+ "--tableName:" + paramObj.getTableName()
						+ "--tableOwner:" + paramObj.getTableOwner()
						+ "--partitionType:" + paramObj.getPartitionType()
						+ "--partitionPrefix:" + paramObj.getPartitionPrefix()
						+ "--tablespaceType:" + paramObj.getTablespaceType()
						+ "--tablespacePrefix:" + paramObj.getTablespacePrefix());
			}
		}
		
		try {
			paramValsReturn = validateParamAddPartition(conn, paramVals);
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
		return paramValsReturn;
	}
	
	private List<ParamValAddPartitionObj> validateParamAddPartition(Connection conn, List<ParamValAddPartitionObj> lstParamValidate) {
		List<ParamValAddPartitionObj> lstParamValValidated = new ArrayList<>();
		try {
			if (lstParamValidate != null) {
				PreparedStatement prepare = null;
				ResultSet rs = null;
				boolean checkStatus = false;
				for (ParamValAddPartitionObj param : lstParamValidate) {
					try {
						checkStatus = false;
						boolean checkTablespace = false;
						prepare = conn.prepareStatement(Config.ADD_PART_QUERY_CHECK_TABLESPACE_NAME);
						prepare.setFetchSize(1000);
						prepare.setString(1, param.getTablespaceNameCheckSpace());
						rs = prepare.executeQuery();
						while(rs.next()) {
							Long result = rs.getLong("data_return");
//							logger.info(">>>>>>>> result check tablespace (1->ok): " + result + " table space name: " + param.getTablespaceNameCheckSpace());
							if (result.intValue() == 1) {
								checkTablespace = true;
								break;
							}
						} // end loop while
						
						rs.close();
						prepare.close();
						
						if (checkTablespace) {
							prepare = conn.prepareStatement(Config.ADD_PART_QUERY_CHECK_PARTITION_NAME);
							prepare.setFetchSize(1000);
							prepare.setString(1, param.getTableOwnerAddPartition());
							prepare.setString(2, param.getTableNameCheckPartition());
							prepare.setString(3, param.getPartitionNameCheckpartition());
							rs = prepare.executeQuery();
							while(rs.next()) {
								Long result = rs.getLong("data_return");
//								logger.info(">>>>>>>> result check partition name (0->ok): " + result + " table space name: " + param.getTablespaceNameCheckSpace());
								if (result.intValue() == 0) {
									checkStatus = true;
									break;
								}
							} // end loop while
							
							rs.close();
							prepare.close();
						} else {
							
							// thoat khoi vong lap check
							break;
						}
						
						if (checkStatus) {
							lstParamValValidated.add(param);
						} else {
							break;
						}
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						logger.info(e.toString());
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
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			logger.info(e.toString());
		}
		return lstParamValValidated;
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
			DateTime dt = DateTime.now();
			dt = dt.plusDays(1);
			System.out.println(new PasswordEncoder().encrypt("qwertyuiop"));
			System.out.println(dt.getMonthOfYear());
			System.out.println(dt.getDayOfWeek());
//			System.out.println(new ParamInputServiceImpl().findList().size());
			for (int i = 0; i < 10; i++) {
				try {
					if (i == 5) {
						break;
					}
				} catch (Exception e) {
					logger.error(e.getMessage(), e);
				} finally {
					System.out.println("ok");
				}
				
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private DateTime getHightValDatetime(Connection conn, ParamAddPartitionObj partitionObj) {
		DateTime hightestDateTime = null;
		if (conn != null) {
			try {
				// la account monitor defaul cho toan bo database
				PreparedStatement prepare;
				prepare = conn.prepareStatement(Config.QUERY_GET_HIGHT_VAL_PARTITION);
				prepare.setFetchSize(1000);
				logger.info(partitionObj.getTableOwner() + "___" + partitionObj.getTableName());
				prepare.setString(1, partitionObj.getTableOwner().toUpperCase() );
				prepare.setString(2,  partitionObj.getTableName().toUpperCase() );
				prepare.setString(3,  partitionObj.getTableName().toUpperCase() );
				prepare.setString(4,  partitionObj.getTableOwner().toUpperCase() );
				
				ResultSet rs = prepare.executeQuery();
				
				// get data
				String hightVal = null;
				while (rs.next()) {
					hightVal = rs.getString("high_value");
				}
				prepare.close();
				
				if (hightVal != null) {
//					logger.info(">>>>>>>>>> HIGHT VALUES: " + hightVal.trim());
					// return SYYYY-MM-DD (2017-03-01)
					hightVal = Arrays.asList(hightVal.trim().replaceAll(" +", " ").split(" ")).get(1);
//					Date hightDateVal = df.parse(hightVal);
					
					hightestDateTime = DateTime.parse(hightVal);
//					logger.info(">>>>>>>>>> hight value: " + hightVal + "  - - " + hightestDateTime.toString());
				} else {
					logger.error("hightVal null " + partitionObj.getTableOwner().toUpperCase() + "__" + partitionObj.getTableOwner().toUpperCase());
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
		return hightestDateTime;
	}

	

}
