package com.viettel.it.thread;

import com.viettel.bean.ResultGetAccount;
import com.viettel.it.model.*;
import com.viettel.it.object.ParamExportObject;
import com.viettel.it.object.ResizeParamOptionObject;
import com.viettel.it.object.ResizeParamsValsObject;
import com.viettel.it.persistence.ParamExportDumpSizeDbServiceImpl;
import com.viettel.it.persistence.ParamInputServiceImpl;
import com.viettel.it.persistence.ParamValueServiceImpl;
import com.viettel.it.util.Config;
import com.viettel.it.util.LogUtils;
import com.viettel.passprotector.PassProtector;
import com.viettel.util.AppConfig;
import com.viettel.util.Constant;
import com.viettel.it.util.SecurityService;
import com.viettel.util.PasswordEncoder;
import com.viettel.util.SessionUtil;
import org.joda.time.DateTime;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.util.*;

public class GetParamResizePartitionThread extends GetParamThread {
	
	private final SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
	
	public GetParamResizePartitionThread(FlowRunAction flowRunAction,
                                         Node databaseNode, NodeRun nodeRun, Integer cycleMonths,
                                         String urlQltn, String usernameQltn, String passwordQltn,
                                         long keySession, Logger logger, Map<String, String> mapConfigSecurity, Map<String, ResultGetAccount> mapPassGet) {
		super(flowRunAction, databaseNode, nodeRun, cycleMonths, urlQltn, 
				usernameQltn, passwordQltn, keySession,logger, mapConfigSecurity, mapPassGet);
		// TODO Auto-generated constructor stub
	}

	protected static final Logger logger = LoggerFactory.getLogger(GetParamResizePartitionThread.class);
	
	@Override
	public void run() {
		logger.info(">>>>>>>>>>>>>>>> START RUN GET MOP RESIZE TABLESPACE DATABASE: " + databaseNode.getNodeIp() + "_virtualIP: " + databaseNode.getNodeIpVirtual());
		Connection conn = null;
		try {
			NodeAccount impactSqlAcc = getAccImpactDefault(databaseNode.getServerId());
			if (impactSqlAcc == null) {
				logger.error("ERROR GET ACCOUNT TDHT FOR NODE: " + databaseNode.getNodeIp());
				return;
			}
			
			List<ParamExportObject> lstParamExport = getParamExportDb();
			ResizeParamsValsObject resizeParamVal = new ResizeParamsValsObject();
			conn = getConnection(databaseNode.getJdbcUrl(), impactSqlAcc.getUsername(), impactSqlAcc.getPassword());
			for (ParamExportObject param : lstParamExport) {
				procSingleResizetRow(param, impactSqlAcc, resizeParamVal, conn);
			}
			
			conn.close();
			
			// save param value
			saveParamVal(resizeParamVal);
			
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e2) {
					logger.error(e2.getMessage(), e2);
					// TODO: handle exception
				}
			}
			
			logger.info(">>>>>>> finish resize partition node: " + databaseNode.getNodeIp());
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
	
	private void saveParamVal(ResizeParamsValsObject resizeParamVal) {
		if (resizeParamVal != null) {
			Map<String, Object> filters = new HashMap<String, Object>();
			Map<String, String> orders = new HashMap<>();
			orders.put("paramCode", "ASC");
			
			ParamInput paramSqlDrop = null;
			ParamInput paramSetReadWritePar = null;
			try {
				filters.put("paramCode", Config.RESIZE_DB_SQL_RESIZE_TBS_VAL);
				paramSqlDrop = new ParamInputServiceImpl().findList(filters, orders).get(0);
				
				filters.put("paramCode", Config.RESIZE_DB_SQL_SET_READ_WRITE_TBS_VAL);
				paramSetReadWritePar = new ParamInputServiceImpl().findList(filters, orders).get(0);
				
				if (resizeParamVal.getResizeCmd().endsWith(";")) {
					resizeParamVal.setResizeCmd(resizeParamVal.getResizeCmd().substring(0, resizeParamVal.getResizeCmd().length() - 1));
				}
				if (resizeParamVal.getCreateReadWriteCmd().endsWith(";")) {
					resizeParamVal.setCreateReadWriteCmd(resizeParamVal.getCreateReadWriteCmd().substring(0, resizeParamVal.getCreateReadWriteCmd().length() - 1));
				}
				List<ParamValue> lstParamVal = new ArrayList<>();
				lstParamVal.add(new ParamValue(paramSqlDrop, paramSqlDrop.getParamCode(), new Date(), resizeParamVal.getResizeCmd(), nodeRun));
				lstParamVal.add(new ParamValue(paramSetReadWritePar, paramSetReadWritePar.getParamCode(), new Date(), resizeParamVal.getCreateReadWriteCmd(), nodeRun));
				new ParamValueServiceImpl().saveOrUpdate(lstParamVal);
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	private ResizeParamsValsObject procSingleResizetRow(ParamExportObject paramExport, NodeAccount impactSqlAcc,
                                      ResizeParamsValsObject resizeParamVal, Connection conn) {
		if (paramExport != null) {
			ResizeParamOptionObject paramResize = buildParamResize(paramExport);
			if (paramResize != null) {
					resizeParamVal = getCmdsResize(paramResize, conn, resizeParamVal);

					// get command update table read write
//					String cmdUpdateTbs = getCmdUpdateReadWrite(paramResize, conn);
//					if (cmdUpdateTbs != null && !cmdUpdateTbs.isEmpty()) {
//						String newParamSetRreadWriteVals ;
//						if (resizeParamVal.getCreateReadWriteCmd().endsWith(";")) {
//							newParamSetRreadWriteVals = resizeParamVal.getCreateReadWriteCmd().concat(cmdUpdateTbs).concat(";");
//						} else if (!resizeParamVal.getCreateReadWriteCmd().trim().isEmpty()) {
//							newParamSetRreadWriteVals = resizeParamVal.getCreateReadWriteCmd().concat(";").concat(cmdUpdateTbs).concat(";");
//						} else {
//							newParamSetRreadWriteVals = cmdUpdateTbs.concat(";");
//						}
//						resizeParamVal.setCreateReadWriteCmd(newParamSetRreadWriteVals);
//					}
//
//					// get command resize
//					String paramResizeVals = getLstParamResizePartition(paramResize, impactSqlAcc, paramExport.getObject().trim(), conn);
//					if (paramResizeVals != null && !paramResizeVals.trim().isEmpty()) {
//						String newParamResizeVals;
//						if (resizeParamVal.getResizeCmd().endsWith(";")) {
//							newParamResizeVals = resizeParamVal.getResizeCmd().concat(paramResizeVals).concat(";");
//						} else if (!resizeParamVal.getResizeCmd().trim().isEmpty()){
//							newParamResizeVals = resizeParamVal.getResizeCmd().concat(";").concat(paramResizeVals).concat(";");
//						} else {
//							newParamResizeVals = paramResizeVals.concat(";");
//						}
//						resizeParamVal.setResizeCmd(newParamResizeVals);
//					}
			} else {
				logger.error("ERROR CANNOT GET OPTION RESIZE PARAMETER: " + databaseNode.getNodeIp());
			}
		}
		return resizeParamVal;
	}



	private ResizeParamsValsObject getCmdsResize(ResizeParamOptionObject resizeParamOpt, Connection conn, ResizeParamsValsObject resizeParamVal) {
		if (resizeParamOpt != null) {
			PreparedStatement prepare = null;
			try {
				String sql = "select status from dba_tablespaces where " + resizeParamOpt.getTableNameCondition();

				prepare = conn.prepareStatement(sql);
				prepare.setFetchSize(1000);
				ResultSet rs = prepare.executeQuery();

				boolean isReadOnly = false;
				String cmdUpdateReadOnly = null;
				while (rs.next()) {
					String status = rs.getString("status");
					if ("READ ONLY".equals(status.toUpperCase().trim())) {
						cmdUpdateReadOnly = "alter tablespace " + resizeParamOpt.getTbsName() + " read write";
						isReadOnly = true;
					} else {
						cmdUpdateReadOnly = "select 1 from dual";
					}

					if (cmdUpdateReadOnly != null) {
						break;
					}
				}
				rs.close();
				prepare.close();

				if (isReadOnly) {
					sql = "select count(*) as total_segment_type from dba_segments where segment_type not like 'TEMPORARY' and " + resizeParamOpt.getTableNameCondition();
					logger.info("sql total segment: " + sql);
					prepare = conn.prepareStatement(sql);
					prepare.setFetchSize(1000);
					rs = prepare.executeQuery();

					while (rs.next()) {
						logger.info("rs.getString(\"total_segment_type\"): " + rs.getString("total_segment_type"));
						if (!"0".equals(rs.getString("total_segment_type"))) {
							return null;
						}
					}
					rs.close();
					prepare.close();
				}

				// Update comamnd read write
				String newParamSetRreadWriteVals = null;
				if (resizeParamVal.getCreateReadWriteCmd().endsWith(";")) {
					newParamSetRreadWriteVals = resizeParamVal.getCreateReadWriteCmd().concat(cmdUpdateReadOnly).concat(";");
				} else if (!resizeParamVal.getCreateReadWriteCmd().trim().isEmpty()) {
					newParamSetRreadWriteVals = resizeParamVal.getCreateReadWriteCmd().concat(";").concat(cmdUpdateReadOnly).concat(";");
				} else {
					newParamSetRreadWriteVals = cmdUpdateReadOnly != null ? cmdUpdateReadOnly : "" .concat(";");
				}
				resizeParamVal.setCreateReadWriteCmd(newParamSetRreadWriteVals);

				// Update command resize tablespace
				String paramResizeVals = getLstParamResizePartition(resizeParamOpt, conn, isReadOnly);
				if (paramResizeVals != null && !paramResizeVals.trim().isEmpty()) {
					String newParamResizeVals;
					if (resizeParamVal.getResizeCmd().endsWith(";")) {
						newParamResizeVals = resizeParamVal.getResizeCmd().concat(paramResizeVals).concat(";");
					} else if (!resizeParamVal.getResizeCmd().trim().isEmpty()){
						newParamResizeVals = resizeParamVal.getResizeCmd().concat(";").concat(paramResizeVals).concat(";");
					} else {
						newParamResizeVals = paramResizeVals.concat(";");
					}
					resizeParamVal.setResizeCmd(newParamResizeVals);
				}

			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} finally {
				if (prepare != null) {
					try {
						prepare.close();
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
					}
				}
			}
		}
		return resizeParamVal;
	}
	
	private String getLstParamResizePartition(ResizeParamOptionObject resizeParamOpt, Connection conn, boolean isReadOnly) {
		String paramVals = "";
		if (resizeParamOpt != null) {
			try {
				String sql = "SELECT 'alter database datafile '''||file_name||''' resize '||CEIL ((NVL (hwm, 1) * 8192) / 1024 / 1024+1)||'M' as result_data "
						+ "FROM dba_data_files a,"
						+ "("
							+ "SELECT   file_id, MAX (block_id + blocks - 1) hwm FROM dba_extents WHERE " + resizeParamOpt.getTableNameCondition()
							+ " GROUP BY file_id "
						+ ") b "
						+ "WHERE a.file_id = b.file_id(+) and " + resizeParamOpt.getTableNameCondition();

				if (isReadOnly) {
					sql = "SELECT 'alter database datafile '''||file_name||''' resize 3M' as result_data FROM dba_data_files where 1=1 and " + resizeParamOpt.getTableNameCondition();
				}
				
				logger.info("sql resize: " + sql);
				PreparedStatement prepare;
				prepare = conn.prepareStatement(sql);
				prepare.setFetchSize(1000);
				ResultSet rs = prepare.executeQuery();

				while (rs.next()) {
					if (rs.getString("result_data") != null && !rs.getString("result_data").trim().isEmpty()) {
						logger.info(rs.getString("result_data"));
						paramVals += (rs.getString("result_data")).concat(";");
					}
				}
				logger.info("paramVals: " + paramVals);
				
				rs.close();
				prepare.close();
				
				if (paramVals.endsWith(";")) {
					paramVals = paramVals.substring(0, paramVals.length() - 1);
				}
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			} 
		}
		return paramVals;
	}
	
	
	private ResizeParamOptionObject buildParamResize(ParamExportObject paramExport) {
		ResizeParamOptionObject resizeParamOption = null;
		if (paramExport != null) {
			resizeParamOption = new ResizeParamOptionObject();
			
			// set ROTATE_MONTH
			DateTime dt = new DateTime();
			dt = dt.plusMonths(-Integer.valueOf(paramExport.getRotateDrop()));
			resizeParamOption.setRotateMonth(df.format(dt.toDate()));
			// set Tablespace_Name
			resizeParamOption.setTbsName((paramExport.getTablespaceType() == 0 || paramExport.getTablespaceType() == null) 
					? paramExport.getTablespacePrefix() : paramExport.getTablespacePrefix().concat(resizeParamOption.getRotateMonth()));
			// set Tablespace name condition
			resizeParamOption.setTableNameCondition(resizeParamOption.getTbsName() == null
					? "1=1" : "TABLESPACE_NAME in ('" + resizeParamOption.getTbsName() + "')");
			// set ROTATE_MONTH condition
			if (paramExport.getRotateDrop() == null 
					|| paramExport.getTablespaceType() != 0) {
				resizeParamOption.setRotateMonthCondition("and 1=1");
			} else {
				resizeParamOption.setRotateMonthCondition(" and tablespace_name like '%".concat(resizeParamOption.getRotateMonth()).concat("%')"));
			}
		}
		return resizeParamOption;
	}
	
	public static void main(String[] args) {
		try {
			SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
			DateTime dt = new DateTime();
			dt = dt.plusMonths(-Integer.valueOf(2));
			String cmd = "select * from %s %s ";
			System.out.println(String.format(cmd, "1", 9));
			System.out.println(df.format(dt.toDate()));
			
//			System.out.println(String.format(Config.SELECT_TABLE_OWNER_TABLE_NAME_PARTITION, "DATA201713", "1=1", "3=3", "and 4=4", "and 1=1", "DATA201713", "", "3=3", "and 4=4"));
			System.out.println(String.format(Config.SELECT_TABLE_OWNER_TABLE_NAME_PARTITION, "DATA201713", "1=1", "3=3", "and 4=4", "and 1=1", "DATA201713", "d", "3=3", "and 4=4"));
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		}
	}
	
	private List<ParamExportObject> getParamExportDb() {
		Connection conn = null;
		List<ParamExportObject> lstParamExport = new ArrayList<>();
		try {
			conn = getConnection(urlQltn, usernameQltn, passwordQltn);
			if (conn != null) {
				PreparedStatement prepare;
				prepare = conn.prepareStatement(Config.GET_PARAM_EXPORT_DB);
				prepare.setFetchSize(1000);
				prepare.setLong(1, databaseNode.getServerId());
				ResultSet rs = prepare.executeQuery();

				// get data
				List<String> dirsDetail = new ArrayList<>();
				
				ParamExportObject paramExport = null;
				while (rs.next()) {
					paramExport = new ParamExportObject();
					paramExport.setCompression(rs.getString("COMPRESSION"));
					paramExport.setContent(rs.getString("CONTENT"));
					paramExport.setDbId(rs.getLong("DB_ID"));
					paramExport.setDbParamExportId(rs.getLong("DB_PARAM_EXPORT_ID"));
					paramExport.setDirectory(rs.getString("DIRECTORY"));
					paramExport.setDumpSize(rs.getString("DUMP_SIZE") == null ? 0.0 : Double.valueOf(rs.getString("DUMP_SIZE")));
					paramExport.setDuration(rs.getString("DURATION"));
					paramExport.setExclude(rs.getString("EXCLUDE"));
					paramExport.setIpScp(rs.getString("IP_SCP") == null ? null : rs.getString("IP_SCP"));
					paramExport.setObject(rs.getString("OBJECT"));
					paramExport.setOther(rs.getString("OTHER"));
					paramExport.setParallel(rs.getString("PARALLEL"));
					paramExport.setPassScp(rs.getString("PASS_SCP") == null ? null : rs.getString("PASS_SCP"));
					paramExport.setPathDumpPrefix(rs.getString("PATH_DUMP_PREFIX"));
					paramExport.setPathDumpType(rs.getInt("PATH_DUMP_TYPE"));
					paramExport.setPathScp(rs.getString("PATH_SCP") == null ? null : rs.getString("PATH_SCP"));
					paramExport.setPatitionName(rs.getString("PARTITION_NAME") == null ? "" : rs.getString("PARTITION_NAME").toUpperCase());
					paramExport.setRotateDrop(rs.getString("ROTATE_DROP"));
					paramExport.setRotateExport(rs.getString("ROTATE_EXPORT"));
					paramExport.setTableName(rs.getString("TABLE_NAME") == null ? null : rs.getString("TABLE_NAME"));
					paramExport.setTableOwner(rs.getString("TABLE_OWNER") == null ? "" : rs.getString("TABLE_OWNER").toUpperCase());
					paramExport.setTablespaceDrop(rs.getString("TABLESPACE_DROP"));
					paramExport.setTablespacePrefix(rs.getString("TABLESPACE_PREFIX"));
					paramExport.setTablespaceType(rs.getInt("TABLESPACE_TYPE"));
					paramExport.setUserScp(rs.getString("USER_SCP"));
					
					lstParamExport.add(paramExport);
				} 
				prepare.close();
				
				// insert and update param export dump size
				List<ParamExportDumpSizeDb> lstParamExportDumpsize = new ArrayList<>();
				Map<String, Object> filters = new HashMap<>();
				ParamExportDumpSizeDb dumpsize = null;
				for (ParamExportObject param : lstParamExport) {
					filters.put("paramExportQltnId", param.getDbParamExportId());
					filters.put("dbId", param.getDbId());
					try {
						dumpsize = new ParamExportDumpSizeDbServiceImpl().findList(filters).get(0);
					} catch (Exception e) {
						logger.error(e.getMessage(), e);
						dumpsize = null;
					}
					
					if (dumpsize == null) {
						dumpsize = new ParamExportDumpSizeDb();
						dumpsize.setDbId(paramExport.getDbId());
						dumpsize.setParamExportQltnId(paramExport.getDbParamExportId());
						lstParamExportDumpsize.add(dumpsize);
					}
				}
				
				new ParamExportDumpSizeDbServiceImpl().saveOrUpdate(lstParamExportDumpsize);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception e2) {
					logger.error(e2.getMessage(), e2);
					// TODO: handle exception
				}
			}
		}
		
		return lstParamExport;
	}
	

}
