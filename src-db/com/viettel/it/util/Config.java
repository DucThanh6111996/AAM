package com.viettel.it.util;

import com.viettel.it.persistence.DaoSimpleService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.util.List;
import java.util.ResourceBundle;

public class Config {
	private static Logger logger = LogManager.getLogger(Config.class);

	public static final String SALT = "LKJFGHLSDKJGHLKFG039RFHDKFJHJLGHSDLFGJHLSDH934834943";

    // Trang error.
    public static final String _ERROR_PAGE = "/error";
    // Doi tuong UserToken trong du lieu VSA tra ve.
    public static final String _VSA_USER_TOKEN = "vsaUserToken";
	// Config default url cho ung dung.
    // Neu khong dung default thi gan gia tri ve ""
    public static final String _DEFAULT_URL = "/execute";
    public static final String _DEFAULT_URL2 = "/action";
    public static final String _DEFAULT_URL3 = "/command";
    public static final String _DEFAULT_URL4 = "/mop";
    
    
    public static final String ROOT_FOLDER_DATA = "datas";
    public static final String MEDIA_FOLDER = "medias";
    public static final String IMAGE_FOLDER = "images";
    public static final String AUDIO_FOLDER = "audios";
    
    public static final String PATH_OUT = File.separator + ".." + File.separator + ".." + File.separator + "report_out" + File.separator;
    public static final String XLSX_FILE_EXTENTION = ".xlsx";
	public static final Long EXECUTE_CMD = 1L;
	public static final Long ROLLBACK_CMD = 2L;
	public static final String SPLITTER_VALUE = ";";
	public static final Long EXECUTE_AVAILABLE = 1L;
	public static final Long SAVE_DRAFT = 0L;
	protected static final String[] COLORS = {"#7FFF00","#32CCFE","#FF8C00","#000080","#FF4500","#DDA0DD", "#D2B48C" ,"#FF6347","#00FFFF","#FFFF66","#33CC33",
		"#0099CC", "#CD853F","#97CDCC","#A2CD5A" ,"#FFD800"};

	public static String[] getCOLORS() {
		return COLORS;
	}

	public static final Long DT_BUIDING = -1L;
	public static final Long DT_FINISH_BUILD = 0L;
	public static final Long DT_APPROVED = 1L;
	public static final Long DT_RUNNING = 2L;
	public static final Long DT_SUCCESS = 3L;
	public static final Long DT_ROLLBACK = 4L;
	
    public static final Long SAVE_FLAG = 0l;
    public static final Long WAITTING_FLAG = 1l;
    public static final Long RUNNING_FLAG = 2l;
    public static final Long FINISH_FLAG = 3l;
    public static final Long FAIL_FLAG = 4l;
    public static final Long NODE_TYPE_ID_DEFAULT = -1L;
    public static final Long CMD_TYPE_IMPACT = 0l;
    public static final Long CMD_TYPE_VIEW = 1l;
	/*20180726_hoangnd_check dieu kien thuc hien_start*/
	public static final Long CMD_TYPE_GET_PARAM = 2l;
    /*20180726_hoangnd_check dieu kien thuc hien_end*/
    
	public static final Integer APPROVAL_STATUS_DEFAULT = 0;
//	20180828_tudn_start apppro template
//	public static final Integer APPROVALED_STATUS_LEVEL1 = 1;
	public static final Integer APPROVALED_STATUS_LEVEL1 = 8;
//	20180828_tudn_end apppro template
	public static final Integer APPROVALED_STATUS_LEVEL2 = 9;
	public static final Long STOP_FLAG = 6l;
	public static final Long GET_PASS_FAIL_FLAG = 18l;
	/*20180626_hoangnd_them_button_thuc_hien_rollback_start*/
	public static final Long PAUSE_FLAG = 8l;
	public static final Long LOGIN_FAIL_FLAG = 10l;
	public static final Long RUNNING_TYPE_INDEPENDECE = 1l; // Cac node chay doc lap
	public static final Long RUNNING_TYPE_DEPENDENT = 2L; // Cac node chay song song
    /*20180626_hoangnd_them_button_thuc_hien_rollback_end*/
    /*20180713_hoangnd_them approve rollback_start*/
	public static final Long WAITTING_ROLLBACK_FLAG = 13l;
    /*20180713_hoangnd_them approve rollback_end*/
	
	public static final String AGG_NODE_ODD = "AGG_NODE_ODD";
	public static final String AGG_NODE_EVEN = "AGG_NODE_EVEN";
	
	
	public static final String PROVINCE_CORE_NODE_TYPE = "CORE_PROVINCE";
//	public static final String PROVINCE_CORE_NODE_TYPE = "CV,CORE_AREA";
	public static final String AGG_NODE_TYPE = "AGG METRO";
	public static final String SRT_NODE_TYPE = "SRT";
	public static final String SWITCH_NODE_TYPE = "SWITCH_NODE_TYPE";
	public static final String AGG_NODE_CLIENT_CONNECT_DIRECTOR = "AGG_NODE_CLIENT_CONNECT_DIRECTOR"; 
	public static final String SRT_NODE_CLIENT_CONNECT_DIRECTOR = "SRT_NODE_CLIENT_CONNECT_DIRECTOR";
	public static final Long FLOW_RUN_ACTION_FINISH_STATUS = 3l;
	public static final Long FLOW_RUN_ACTION_FAIL_STATUS = 4L;
	
	public static final Long ROLLBACK_ACTION = 1l;
	public static final Long EXECUTE_ACTION = 0L;
	public static final Long NODE_DEFAULT_ID = 100L;
	public static final Long VERSION_DEFAULT_ID = 99L;

	public static final Long SERVER_ID = 1L;
	public static final Long DATABASE_ID = 2L;
	public static final Long PROVISIONING_ID = 3L;
	public static final Long WEB_SERVICE_ID = 4L;

	/*20180714_hoangnd_action manual_start*/
	public static final Long ACTION_TYPE_NORMAL = 0L;
	public static final Long ACTION_TYPE_MANUAL = 1L;
    /*20180714_hoangnd_action manual_end*/
        
    public static final String PROTOCOL_TELNET = "TELNET";
    public static final String PROTOCOL_SSH = "SSH";
    public static final String PROTOCOL_SQL = "SQL";
    public static final String PROTOCOL_EXCHANGE = "EXCHANGE";
	public static final String PROTOCOL_WEBSERVICE = "WEB_SERVICE";
	public static final String CR_AUTO_DECLARE_CUSTOMER = "CR_AUTO";
	public static final String CR_DEFAULT = "CR_DEFAULT";
	
	public static final String DEFAULT_ORACLE_ACCOUNT = "vt_admin";	
	public static final String QUERY_PARAM_ADD_PARTITION = "select distinct table_name,table_owner,partition_type," +
			"partition_prefix,tablespace_type,tablespace_prefix,db_id,status " +
			"from qltn.DB_PARAM_PARTITION p where p.db_id = ? and p.status = 1";
	public static final String QUERY_GET_HIGHT_VAL_PARTITION = "SELECT t.high_value "
			+ "FROM dba_tab_partitions t "
			+ "WHERE t.Table_Owner like ? "
			+ "AND t.Table_name like ? "
			+ "AND partition_position = ("
				+ "Select MAX(partition_position) "
				+ "From dba_tab_partitions "
				+ "Where table_name like ? "
				+ "And Table_Owner like ?"
			+ ")";
	public static final String ADD_PART_QUERY_CHECK_TABLESPACE_NAME = "Select count(*) as data_return From dba_Tablespaces Where Tablespace_Name like ?";
	public static final String ADD_PART_QUERY_CHECK_PARTITION_NAME = "Select count(*) as data_return From Dba_Tab_Partitions Where Table_Owner like ? And Table_Name like ? And Partition_Name like ?";
	
	public static final String ADD_PART_PARAM_CHECK_TABLESPACE_NAME = "ADD_PART_CHECK_TABLESPACE_NAME";
	public static final String ADD_PART_PARAM_CHECK_TABLE_OWNER = "ADD_PART_CHECK_TABLE_OWNER";
	public static final String ADD_PART_PARAM_CHECK_TABLENAME = "ADD_PART_CHECK_TABLE_NAME";
	public static final String ADD_PART_PARAM_CHECK_PARTITION_NAME = "ADD_PART_CHECK_PARTITION_NAME";
	public static final String ADD_PART_PARAM_ADD_TABLE_OWNER = "ADD_PART_TABLE_OWNER";
	public static final String ADD_PART_PARAM_ADD_TABLE_NAME = "ADD_PART_TABLE_NAME";
	public static final String ADD_PART_PARAM_ADD_PARTITION_NAME = "ADD_PART_PARTITION_NAME";
	public static final String ADD_PART_PARAM_ADD_P_CYCLE = "ADD_PART_P_CYCLE";
	public static final String ADD_PART_PARAM_ADD_TABLESPACE_NAME = "ADD_PART_TABLESPACE_NAME";
	
	public static final String ADD_TBS_CHECK_FILE_NAME = "ADD_TBS_CHECK_FILE_NAME";
	public static final String ADD_TBS_CHECK_TBS_NAME = "ADD_TBS_CHECK_TBS_NAME";
	public static final String ADD_TBS_TBS_NAME = "ADD_TBS_TBS_NAME";
	public static final String ADD_TBS_FILE_NAME = "ADD_TBS_FILE_NAME";
	public static final String ADD_TBS_DATAFILE_SIZE = "ADD_TBS_DATAFILE_SIZE";
	public static final String ADD_TBS_EXTEND_OPTION = "ADD_TBS_EXTEND_OPTION";
	
	public static final String ADD_DTF_CHECK_FILE_NAME = "ADD_DTF_CHECK_FILE_NAME";
	public static final String ADD_DTF_CHECK_TBS_NAME = "ADD_DTF_CHECK_TBS_NAME";
	public static final String ADD_DTF_TBS_NAME = "ADD_DTF_TBS_NAME";
	public static final String ADD_DTF_FILE_NAME = "ADD_DTF_FILE_NAME";
	public static final String ADD_DTF_DATAFILE_SIZE = "ADD_DTF_DATAFILE_SIZE";
	public static final String ADD_DTF_EXTEND_OPTION = "ADD_DTF_EXTEND_OPTION";
	
	public static final String EXPORT_DB_CONNECT = "EXPORT_DB_CONNECT";
	public static final String EXPORT_DB_OPTIONAL = "EXPORT_DB_OPTIONAL";
	public static final String EXPORT_DB_OWNER_TABLE_NAME = "EXPORT_DB_OWNER_TABLE_NAME";
	public static final String EXPORT_DB_DIRECTORY = "EXPORT_DB_DIRECTORY";
	public static final String EXPORT_DB_DUMPFILE = "EXPORT_DB_DUMPFILE";
	public static final String EXPORT_DB_LOG_FILE = "EXPORT_DB_LOG_FILE";
	
	public static final String DROP_DB_SQL_VAL = "DROP_DB_SQL_VAL";
	public static final String RESIZE_DB_SQL_RESIZE_TBS_VAL = "RESIZE_DB_SQL_RESIZE_TBS_VAL";
	public static final String RESIZE_DB_SQL_SET_READ_WRITE_TBS_VAL = "RESIZE_DB_SQL_SET_READ_WRITE_TBS_VAL";
	
	public static final String CONFIG_FILE = "config.properties";
	public static final String ROOT_ACCOUNT_OS = "root";
	public static final String DEFAULT_ACCOUNT_DB_MONITOR = "monitor";
	public static final String DEFAULT_ACCOUNT_IMPACT_OS = "vt_admin";
	public static final String DEFAULT_ACCOUNT_IMPACT_DB = "monitor";
	public static final Integer DEFAULT_SSH_OS_PORT = 22;
	public static final String GET_STORATE_DATA_SIZE_COMMAND = "df -k";
	
	public static final Long FLOW_TEMPLATE_EXPORT_DB = 10160L;
	public static final Long FLOW_TEMPLATE_ADD_DATAFILE_DB = 10121L;
	public static final Long FLOW_TEMPLATE_ADD_TABLESPACE_DB = 10083L;
	public static final Long FLOW_TEMPLATE_ADD_PARTITION_DB = 10082L;
	public static final Long FLOW_TEMPLATE_DROP_PAR_TBS_DB = 10180L;
	public static final Long FLOW_TEMPLATE_RESIZE_PARTITION = 10200L;
	public static final String GROUP_CODE_NAME = "GROUP_COMMAND";
	public static final String SIDN_OPEN_BLOCKING_ID = "ID";
	public static final String PREFIX_OPEN_BLOCKING_SIDN = "RESCUE_OPEN_BLOCKING_SIDN_";
	
//	public static final String QUERY_GET_DATAFILE_DETAIL_FROM_DBA_DATA_FILE = "Select distinct substr(a.file_name,0,instr(a.file_name,'/',-1)) as datafile From dba_data_files a";
	public static final String QUERY_GET_DATAFILE_DETAIL_FROM_DBA_DATA_FILE = "Select distinct substr(a.file_name,0,instr(a.file_name,'/',-1)) as datafile  From dba_data_files a "
			+ "where (upper (substr(a.file_name,0,instr(a.file_name,'/',-1))) not like '%SATA%' "
			+ "and upper (substr(a.file_name,0,instr(a.file_name,'/',-1))) not like '%ARC%')";
	
	public static final String GET_MAX_SIZE_DATAFILE_ASM_TYPE = "select round(free_mb/1024) as max_size from v$asm_diskgroup where name like ?";
	public static final String GET_TABLESPACE_INFOS = "select * from QLTN.DB_PARAM_TABLESPACE where db_id = ? and status like '1'";
	public static final String GET_MAX_DATAFILE_INFO = 
			"select a.DB_ID, a.DB_IP, a.DB_NAME, a.DIR_DETAIL, a.DATAFILE_DIR, a.DIR_TYPE, "
			+ "a.DIR_SIZE, a.DATAFILE_SIZE, a.AUTO_EXTEND, a.INITIAL_SIZE, a.MAX_SIZE, a.STATUS, "
			+ "a.INSERT_TIME, a.UPDATE_TIME "
			+ "FROM Db_Param_Datafile a "
			+ "WHERE a.DIR_SIZE IN (SELECT MAX(b.DIR_SIZE) FROM Db_Param_Datafile b WHERE b.DB_ID = (:dbId1) AND b.DB_NAME LIKE (:dbName1)) "
			+ "AND a.DB_ID = (:dbId2) "
			+ "AND a.DB_NAME LIKE (:dbName2) "
			+ "AND ROWNUM <=1 "
			+ "AND a.DIR_SIZE >= 20";
	public static final String GET_DATAFILE_INFO_IF_EXIST =
			"select a.DB_ID, a.DB_IP, a.DB_NAME, a.DIR_DETAIL, a.DATAFILE_DIR, a.DIR_TYPE, "
					+ "a.DIR_SIZE, a.DATAFILE_SIZE, a.AUTO_EXTEND, a.INITIAL_SIZE, a.MAX_SIZE, a.STATUS, "
					+ "a.INSERT_TIME, a.UPDATE_TIME "
					+ "FROM Db_Param_Datafile a "
					+ "WHERE a.DB_ID = (:dbId) "
					+ "AND a.DB_NAME LIKE (:dbName) "
					+ "AND a.DIR_DETAIL LIKE (:dirDetail) "
					+ "AND ROWNUM <=1 "
                    + "AND a.DIR_SIZE >= 20";
//	public static final String GET_MAX_DATAFILE_INFO =
//			"select * from Db_Param_Datafile a "
//			+ "WHERE a.DIR_SIZE IN (SELECT MAX(b.DIR_SIZE) FROM Db_Param_Datafile b WHERE b.DB_ID = (:dbId1) AND b.DB_NAME LIKE (:dbName1)) "
//			+ "AND a.DIR_SIZE >= 20";
	public static final String GET_PARAM_EXPORT_DB = "select * from QLTN.DB_PARAM_EXPORT where db_id = ? and status like '1'";
	public static final String CHECK_EXIST_DATAFILE_NAME = "Select count(*) as data_return From dba_data_files Where file_name like ?";
//	public static final String SELECT_TABLE_OWNER_TABLE_NAME_PARTITION = "select table_owner||';'||table_name||';'||partition_name as result_data from dba_tab_partitions where ? ? ? ? ? and table_name not like 'BIN$%' and table_owner||'.'||table_name||'.'||partition_name not in "
//			+ "(select owner||'.'||table_name||'.'||partition_name from sysman.log_export where ? ? ? ? and create_date > sysdate -7 and status like 'OK') order by table_owner,table_name,partition_name";
	public static final String SELECT_TABLE_OWNER_TABLE_NAME_PARTITION = "select table_owner||';'||table_name||';'||partition_name as result_data from dba_tab_partitions where TABLESPACE_NAME in ('DATA201701') and 2=2 and 3=3 and 4=4 and 1=1 and table_name not like 'BIN$%' and table_owner||'.'||table_name||'.'||partition_name not in "
			+ "(select owner||'.'||table_name||'.'||partition_name from sysman.log_export where TABLESPACE_NAME in ('DATA201701') and 2=2 and 3=3 and 4=4 and create_date > sysdate -30 and status like 'OK') order by table_owner,table_name,partition_name";
	
	public static final String SELECT_TABLE_OWNER_TABLE_NAME_TABLESPACE = "select owner||';'||object_name from dba_objects where object_type like 'TAB%' %s %s and object_name not in"
			+ "(SELECT TABLE_NAME FROM sysman.log_export WHERE create_date > sysdate -30 %s %s and partition_name is null AND status LIKE 'OK') GROUP BY owner, object_name order by owner,object_name";
	public static final String SELECT_AUTO_EXTEND_DB_ID_INFOS = "select * from QLTN.DB_PARAM_DATAFILE where DB_ID = ? and rownum <= 1 and status like '1'";
	public static final String SELECT_TBSNAME_FROM_NODE = "select tablespace_name from dba_tablespaces order by tablespace_name";
	
	public static final String USER_WS_CHECKLIST = "6f2efd0770ec8d37c22a0e885a32707f";
	public static final String PASS_WS_CHECKLIST = "bed0c2ddbfff2e0e3077e2dc2885b38a";
	public static final String DATA_FIELD_NAME = "data";

	public static final String GET_DB_PARAM_DATAFILE = "GET_DB_PARAM_DATAFILE";
	public static final String GET_DB_PARAM_PARTITION = "GET_DB_PARAM_PARTITION";
	public static final String GET_DB_PARAM_TABLESPACE = "GET_DB_PARAM_TABLESPACE";
	public static final String GET_DB_PARAM_EXPORT = "GET_DB_PARAM_EXPORT";

	public static enum ACTION {
		NEW(1),UPDATE(2),DELETE(3),SUSPEND(4);
		public Long value;
		ACTION(long v){
			value = v;
		}
	};
	public static enum SERVICE {
		L2VPN(2),L3VPN(3),ENODEB_L2(4),ENODEB_L3(5);
		public long value;
		SERVICE(int v){
			value = v;
		}
	};
	public static enum NODE_TYPE{
		AGG_METRO(6),
		ASW_DCN(103),
		BB_INTRANET(114),
		BRAS(109),
		CKV(2),
		CORESW_LAYER(113),
		CORE_LAYER(105),
		CORE_PROVINCE(111),
		CV(3),
		DGW(5),
		DSLAM(100),
		GATEWAY_LAYER(112),
		GPON_AMP(102),
		GPON_OLT(8),
		IGW_PEERING(4),
		NODE_TYPE_DEFAULT(-1),
		PECD(1),
		PE_2G(110),
		SRT(7),
		STP(115),
		SWITCH(101);
		public Long value;
		NODE_TYPE(long value){
			this.value =value; 
		}
	}
	public static enum CONNECT_TYPE{
		L2_SW(2),DIRECT(1); 
		public Long value;
		CONNECT_TYPE(long value){
			this.value =value; 
		}
	}
	public static enum VERSION {
		CRS_X16_MC(4),
		ASR9010(5),
		Cisco_ASR9922(6),
		ASR9000(7),
		CX600_X16A(8),
		CRS_X16_SC(9),
		_5928E(100),
		IS2828F(101),
		FSAP9800(102),
		MA5300(103),
		IES5000(104),
		MA5605(105),
		S3100_28FC(106),
		MES3500_24F(107),
		SM3100_28TC_AC(108),
		ED5229_64(109),
		OVSxE64190(110),
		H89(111),
		ED5229_32(112),
		H88(113),
		CX600_8(114),
		MA5103(115),
		IES1000(116),
		CX600_X16(117),
		S3400(118),
		S2309(119),
		DCS_3950(120),
		ASR901_6CZ_FT(121),
		MEDFA_FHx64(122),
		MEDFA_AT5200x64(123),
		S3300(124),
		MEDFA_IPGx64(125),
		ES_3124F(126),
		CiscoCRS16(127),
		CX600_16(128),
		SF300_24(129),
		Cisco_ARS901(16),
		V45(254),
		CRS3(1),
		CX600_X8(2),
		S5328(3),
		C300(10),
		C320(11),
		MA5603T(12),
		MA5608(13),
		OLT_ALU(14),
		V8240(15),
		ATN910B(17),
		ZXCTN9008(130),
		MA5303(131),
		M41(132),
		MA5608T(133),
		IPNODEB(134),
		MA5105(135),
		MEDFA_FHx32(136),
		SAM960(137),
		Firewall_F800C(138),
		_3928A(139),
		NSH_5632(140),
		BRAS_MX960(141),
		R2126EA_MA(142),
		FG800C(143),
		ASR9922(144),
		UA5000(145),
		_24S(146),
		S5352(147),
		Fortigate800C(148),
		ES1248(149),
		S6509(150),
		NE80(151),
		MA5100(152),
		T160G(153),
		C2950(154),
		C3550(155),
		R7206(156),
		S5300(157),
		S3900(158),
		C3750(159),
		S5648(160),
		Q9300(161),
		C2850(162),
		MA5200G4(163),
		SE800(164),
		M6000(165),
		T64G(166),
		Q5648(167),
		SE400(168),
		Q3900(169),
		S3500(170),
		R7613(171),
		PC6248(172),
		R3845(173),
		Q5328(174),
		SCE8000(175),
		T640(176),
		SGSE_14(177),
		NS500(178),
		ASA5540(179),
		Q5352(180),
		SummitX440(181),
		IPS3500(182),
		Firewall_ISG1000(183),
		C2911(184),
		C3560(185),
		S3928(186),
		C4900(187),
		SSG550(188),
		S3528(189),
		HPA5500_48G_EI(190),
		C2900(191),
		E200(192),
		GS748T(193),
		AX1000(194),
		C2620(195),
		C1900(196),
		C2600(197),
		SSG350(198),
		BIGIP3600(199),
		BIGIP1600(200),
		FWS624(201),
		HSM(202),
		ZXR10T64G(203),
		AX2100(204),
		FG200B(205),
		MPX9500(206),
		AR46(207),
		S5600(208),
		TLSG1024(209),
		ZXR108902(210),
		ZXR105952(211),
		PCT6248(212),
		PCT5448(213),
		S3526(214),
		PCT6224(215),
		PTC6224(216),
		C880(217),
		cisco2960(218),
		FG620(219),
		HPA5500_24GEI(220),
		C2800(221),
		C870(222),
		C1800(223),
		PIX515(224),
		Z5952(225),
		FG110C(226),
		MCU_RMX2000(227),
		SRX5800(228),
		ASR903(229),
		TPLink24_Ge(230),
		FA200D(231),
		CTHtest(232),
		CP12407(233),
		Fortinet_1500D(234),
		MAG4610(235),
		C2960(236),
		FG310B(237),
		AR4640(238),
		HP2910(239),
		GGC(240),
		R3745(241),
		S3550(242),
		R2811(243),
		S3560(244),
		S3750(245),
		Q9309(246),
		Q3500(247),
		Q7810(248),
		_7810(249),
		R1741(250),
		R3850(251),
		S5352C_EI(252),
		OVSxE32190(253);

		public Long value;
		VERSION(long value){
			this.value = value;
		}
	}
	public static enum BAN_DVCD{
		KV1(ResourceBundle.getBundle("config").getString("BAN_DVCD_KV1")),
		KV2(ResourceBundle.getBundle("config").getString("BAN_DVCD_KV2")),
		KV3(ResourceBundle.getBundle("config").getString("BAN_DVCD_KV3"));
		private long value;
		BAN_DVCD(String v){
			this.value = Long.parseLong(v);
		}
		public long getValue() {
			return value;
		}
		public void setValue(long value) {
			this.value = value;
		}
		public void setValue(String value) {
			this.value = Long.parseLong(value);
		}
	}
	
	public static enum APP_TYPE{
		SERVER(1),DATABASE(2),PROVISIONING(3),WEB_SERVICE(4);
		public Long value;
		
		APP_TYPE(long value){
			this.value =value; 
		}
	}
	
	public static enum OS_TYPE{
		LINUX(1), 
		SOLARIS(2), 
		WINDOW(3),
		AIX(4),
		DEFAULT(100);
		
		public Long value;
		OS_TYPE(long value){
			this.value =value; 
		}
	}
	
	public static enum VERSTION_TYPE {
		ORACLE(1), 
		MYSQL(2), 
		MSSQL(3),
		PostgreeSQL(4),
		SERVER_DEFAULT(99);
		
		public Long value;
		VERSTION_TYPE(long value){
			this.value =value; 
		}
	}
	
	public static enum EXECUTE_TYPE {
		GNOC(1),
		VOFFICE(2),
		NORMAL(3);
		
		public Integer value;
		
		EXECUTE_TYPE(Integer value) {
			this.value = value;
		}
	}
	
	public static enum DATAFILE_TYPE {
		ASM("ASM"),
		LOCAL("LOCAL");
		
		public String value;
		
		DATAFILE_TYPE(String value) {
			this.value = value;
		}
	}
	
	public static enum ACCOUNT_IMPACT_MONITOR_TYPE{
		IMPACT(1), 
		MONITOR(2);
		
		public Long value;
		ACCOUNT_IMPACT_MONITOR_TYPE(long value){
			this.value =value; 
		}
	}
	public static enum GroupTemplateName{
		DATABASE("DATABASE"),
		SERVER("SERVER"),
		SERVICE("SERVICE"),
		DATABASE_NODE("DATABASE NODE");

		public String value;
		GroupTemplateName(String value){
			this.value =value;
		}
	}
	public static void main (String args[]) {
		 String paramConnect = "a/Kz6ynKl/bHx8U2qaHd8y0A==";
		 String valReturn = "";
		 if ( paramConnect.contains("/")) {
	            int index = paramConnect.indexOf("/");
	            try {
	                String pass = paramConnect.substring(index + 1, paramConnect.length());
	            pass = PasswordEncoder.decrypt(pass);
	            valReturn = paramConnect.substring(0, index + 1).concat(pass);
	            } catch (Exception e) {
	                logger.error(e.getMessage(), e);
	            }
		 }
		 System.out.println(valReturn);
	}

	public static enum ACTION_NODE_TYPE{
		IMPACT(0),LOG(1);
		public Long value;
		ACTION_NODE_TYPE(long value){
			this.value =value;
		}
	}

	public static enum COMMAND_TYPE{
		IMPACT(0),VIEW(1), CHECKLIST(2), LOG(3);
		public Long value;
		COMMAND_TYPE(long value){
			this.value =value;
		}
	}

	public static enum COMMAND_CLASSIFY{
		DB(0),ITBUSINESS(1);
		public Long value;
		COMMAND_CLASSIFY(long value){
			this.value =value;
		}
	}

	public static enum COMMAND_LOG_TYPE{
		IMPACT(0),LOG(1);
		public Long value;
		COMMAND_LOG_TYPE(long value){
			this.value =value;
		}
	}

	public static enum OPEN_BLOCKING_SIDN_TYPE{
		MANUAL(1),AUTO(2);
		public Long value;
		OPEN_BLOCKING_SIDN_TYPE(long value){
			this.value =value;
		}
	}

	public static enum IT_BUSINESS_RUNNING_TYPE{
		IMPACT("IMPACT"),RESCUE_OPENNING("RESCUE_OPENNING");
		public String value;
		IT_BUSINESS_RUNNING_TYPE(String value){
			this.value =value;
		}
	}

	public static enum PROVISIONING_LIB_TYPE{
		AP(1),MOBILE(2);
		public int value;
		PROVISIONING_LIB_TYPE(int value){
			this.value =value;
		}
	}

	public static String getConfigFromDB(String key){
		try {
			if (key==null || key.isEmpty())
				return null;
			List<?> confs = new DaoSimpleService().findListSQLAll("SELECT VALUE FROM SYSTEM_CONFIG WHERE KEY=?", key);
			if (!confs.isEmpty()){
				return (String) confs.get(0);
			}
		} catch (Exception e) {
			logger.error(e.getMessage(),e);
		}
		return null;
	}

}
