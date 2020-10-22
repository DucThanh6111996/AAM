package com.viettel.it.thread;

import com.viettel.bean.ResultGetAccount;
import com.viettel.it.model.*;
import com.viettel.it.object.DropParamOptionObject;
import com.viettel.it.object.ParamExportObject;
import com.viettel.it.persistence.ParamExportDumpSizeDbServiceImpl;
import com.viettel.it.persistence.ParamInputServiceImpl;
import com.viettel.it.persistence.ParamValueServiceImpl;
import com.viettel.it.util.*;
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
import java.text.SimpleDateFormat;
import java.util.*;

public class GetParamDropTableThread extends GetParamThread {
	
	private SimpleDateFormat df = new SimpleDateFormat("yyyyMM");
	
	public GetParamDropTableThread(FlowRunAction flowRunAction,
                                   Node databaseNode, NodeRun nodeRun, Integer cycleMonths,
                                   String urlQltn, String usernameQltn, String passwordQltn,
                                   long keySession, Logger logger,Map<String, String> mapConfigSecurity, Map<String, ResultGetAccount> mapPassGet) {
		super(flowRunAction, databaseNode, nodeRun, cycleMonths, urlQltn, 
				usernameQltn, passwordQltn, keySession,logger,mapConfigSecurity,mapPassGet);
		// TODO Auto-generated constructor stub
	}

	protected static final Logger logger = LoggerFactory.getLogger(GetParamDropTableThread.class);
	
	@Override
	public void run() {
		logger.info(">>>>>>>>>>>>>>>> START RUN GET MOP DROP TABLE DATABASE: " + databaseNode.getNodeIp() + "_virtualIP: " + databaseNode.getNodeIpVirtual());
		Connection conn = null;
		try {
			NodeAccount impactSqlAcc = getAccImpactDefault(databaseNode.getServerId());
			if (impactSqlAcc == null) {
				logger.error("ERROR GET ACCOUNT TDHT FOR NODE: " + databaseNode.getNodeIp());
				return;
			}
			
			List<ParamExportObject> lstParamExport = getParamExportDb();
			StringBuilder strBuiderSqlDropVals = new StringBuilder("");
			conn = getConnection(databaseNode.getJdbcUrl(), impactSqlAcc.getUsername(), impactSqlAcc.getPassword());
			for (ParamExportObject param : lstParamExport) {
				procSingleExportRow(param, impactSqlAcc, strBuiderSqlDropVals, conn);
			}
			// save param value
			saveParamValue(strBuiderSqlDropVals);
			
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
			
			logger.info(">>>>>>> finish drop table node: " + databaseNode.getNodeIp());
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
	
	private void saveParamValue(StringBuilder strBuilderParamSqlVal) {
		if (strBuilderParamSqlVal != null) {
			Map<String, Object> filters = new HashMap<String, Object>();
			Map<String, String> orders = new HashMap<>();
			orders.put("paramCode", "ASC");
			
			ParamInput paramSqlDrop;
			try {
				filters.put("paramCode", Config.DROP_DB_SQL_VAL);
				paramSqlDrop = new ParamInputServiceImpl().findList(filters, orders).get(0);
				
				ParamValue valparamSqlDrop ;
				String strParamSqlVal = strBuilderParamSqlVal.toString().endsWith(";") 
						? strBuilderParamSqlVal.substring(0, strBuilderParamSqlVal.length() - 1).toString()
						: strBuilderParamSqlVal.toString();
				valparamSqlDrop = new ParamValue(paramSqlDrop, paramSqlDrop.getParamCode(), new Date(),
						strParamSqlVal, nodeRun);
				new ParamValueServiceImpl().saveOrUpdate(valparamSqlDrop);
				
			} catch (Exception e) {
				logger.error(e.getMessage(), e);
			}
		}
	}
	
	private void procSingleExportRow(ParamExportObject paramExport, NodeAccount impactSqlAcc,
                                     StringBuilder strBuiderSqlDropVals, Connection conn) {
		if (paramExport != null) {
			DropParamOptionObject paramOption = buildParamOption(paramExport);
			if (paramOption != null) {
					String paramDropVals = getLstParamDropPartition(paramOption, impactSqlAcc, 
							paramExport.getObject().trim(), conn);
					if (paramDropVals != null && !paramDropVals.trim().isEmpty()) {
						if (strBuiderSqlDropVals == null) {
							strBuiderSqlDropVals = new StringBuilder();
						}
						if (strBuiderSqlDropVals.toString().isEmpty()
								|| strBuiderSqlDropVals.toString().endsWith(";")) {
//							strBuiderSqlDropVals = strBuiderSqlDropVals.append(paramDropVals).append(";");
							strBuiderSqlDropVals.append(paramDropVals).append(";");
						} else {
//							strBuiderSqlDropVals = strBuiderSqlDropVals.append(";").append(paramDropVals).append(";");
							strBuiderSqlDropVals.append(";").append(paramDropVals).append(";");
						}					
					}
			} else {
				logger.error("ERROR CANNOT GET OPTION EXPORT PARAMETER: " + databaseNode.getNodeIp());
			}
		}
	}
	
	private String getLstParamDropPartition(DropParamOptionObject dropParamOpt, NodeAccount impactSqlAcc,
                                            String object, Connection conn) {
		String paramVals = "";
		if (dropParamOpt != null) {
			try {
				
				String sql;
				if ("partition".equals(object.toLowerCase().trim())) {
					sql = "select DISTINCT 'alter table '||owner||'.'||table_name||' drop partition '||partition_name as result_data "
							+ "from sysman.log_export where "
							+ dropParamOpt.getTbsNameCondition() + " " + dropParamOpt.getOwnerCondition() + " " + dropParamOpt.getTableNameCondition() + " "
							+ dropParamOpt.getParNameCondition() + " " + dropParamOpt.getRotateMonthCondition()
							+ " and status like 'OK' and partition_name is not null and (content not like 'METADATA_ONLY' or content is null) "
							+ "and OWNER||'.'||TABLE_NAME not in "
								+ "(SELECT DISTINCT OWNER||'.'||TABLE_NAME FROM DBA_INDEXES "
									+ "WHERE PARTITIONED = 'NO' "
										+ "AND OWNER||'.'||TABLE_NAME IN "
											+ "(SELECT DISTINCT TABLE_OWNER||'.'|| TABLE_NAME  FROM DBA_TAB_PARTITIONS where "
												+ dropParamOpt.getTbsNameCondition() + " " + dropParamOpt.getTbsOwnerCondition() + " " + dropParamOpt.getTableNameCondition() + " "
											+ ")"
								+ ")"
								+ " and OWNER||'.'||TABLE_NAME||'.'||PARTITION_NAME in "
									+ "(SELECT TABLE_OWNER ||'.'||TABLE_NAME||'.'||PARTITION_NAME "
										+ "FROM DBA_TAB_PARTITIONS WHERE " + dropParamOpt.getTbsNameCondition() + " " + dropParamOpt.getTbsOwnerCondition() + " " + dropParamOpt.getTableNameCondition()
									+ ") "
								+ "order by 1 desc";
					logger.info("sql partition: " + sql);
					
					} else {
						sql = "SELECT DISTINCT 'DROP TABLE '||OWNER||'.'||TABLE_NAME as result_data "
								+ "from sysman.log_export where "
								+ dropParamOpt.getTbsNameCondition() + " " + dropParamOpt.getOwnerCondition() + " " + dropParamOpt.getTableNameCondition()
								+ " AND (content not like 'METADATA_ONLY' or content is null) "
								+ "AND OWNER||'.'||TABLE_NAME IN "
									+ "(SELECT OWNER||'.'||TABLE_NAME FROM DBA_TABLES WHERE "
										+ dropParamOpt.getOwnerCondition1() + " " + dropParamOpt.getTableNameCondition() 
									+ ")";
						logger.info("sql table: " + sql);
					}
				
				PreparedStatement prepare;
				prepare = conn.prepareStatement(sql);
				prepare.setFetchSize(1000);
				ResultSet rs = prepare.executeQuery();

				while (rs.next()) {
					paramVals += (rs.getString("result_data")).concat(";");
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
	
	
	private DropParamOptionObject buildParamOption(ParamExportObject paramExport) {
		DropParamOptionObject dropParamOption = null;
		if (paramExport != null) {
			dropParamOption = new DropParamOptionObject();
			
			// set ROTATE_MONTH
			DateTime dt = new DateTime();
			dt = dt.plusMonths(-Integer.valueOf(paramExport.getRotateDrop()));
			dropParamOption.setRotateMonth(df.format(dt.toDate()));
			// set Tablespace_Name
			dropParamOption.setTbsName(paramExport.getTablespaceType() == 0 
					? paramExport.getTablespacePrefix() : paramExport.getTablespacePrefix().concat(dropParamOption.getRotateMonth()));
			// set Tablespace name condition
			dropParamOption.setTbsNameCondition(dropParamOption.getTbsName() == null
					? "1=1" : "TABLESPACE_NAME in ('" + dropParamOption.getTbsName() + "')");
			// set TABLE_OWNER
			dropParamOption.setTbsOwnerCondition(paramExport.getTableOwner() == null 
					? "and 2=2" : "and TABLE_OWNER in ('".concat(paramExport.getTableOwner()).concat("')"));
			// set OWNER
			dropParamOption.setOwnerCondition(paramExport.getTableOwner() == null 
					? "and 2=2" : "and OWNER in ('".concat(paramExport.getTableOwner()).concat("')"));
			// set OWNER 1
			dropParamOption.setOwnerCondition1(paramExport.getTableOwner() == null 
					? "" : "OWNER in ('".concat(paramExport.getTableOwner()).concat("')"));
			// set TABLE_NAME condition 
			dropParamOption.setTableNameCondition(paramExport.getTableName() == null 
					? "and 3=3" : "and TABLE_NAME in ('".concat(paramExport.getTableName()).concat("')"));
			// set PARTITION_NAME condition 
			dropParamOption.setParNameCondition(paramExport.getPatitionName() == null 
					? " and 4=4" : "and (PARTITION_NAME like '%".concat(paramExport.getPatitionName()).concat("%')"));
			// set ROTATE_MONTH condition
			if (paramExport.getRotateDrop() == null 
					|| paramExport.getTablespaceType() != 0 
					|| paramExport.getTableName() == null
					|| paramExport.getPatitionName() != null) {
				dropParamOption.setRotateMonthCondition("and 1=1");
			} else {
				dropParamOption.setRotateMonthCondition(" and partition_name like '%".concat(dropParamOption.getRotateMonth()).concat("%')"));
			}
		}
		return dropParamOption;
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
					paramExport.setDumpSize(Double.valueOf(rs.getString("DUMP_SIZE")));
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
					paramExport.setPatitionName(rs.getString("PARTITION_NAME"));
					paramExport.setRotateDrop(rs.getString("ROTATE_DROP"));
					paramExport.setRotateExport(rs.getString("ROTATE_EXPORT"));
					paramExport.setTableName(rs.getString("TABLE_NAME"));
					paramExport.setTableOwner(rs.getString("TABLE_OWNER"));
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
				}
			}
		}
		
		return lstParamExport;
	}
	

}
