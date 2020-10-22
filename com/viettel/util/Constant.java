package com.viettel.util;

//import com.mchange.v2.collection.MapEntry;
import com.viettel.bean.MapEntry;
import com.viettel.bean.RunStep;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;
import javax.faces.model.SelectItem;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/*@Service(value = "constantUtils")*/
/*@Scope("session")*/
@ManagedBean( name="constantUtils")
@SessionScoped
public class Constant {
	private static Logger logger = LogManager.getLogger(Constant.class);

	public interface PATTERN {

		String DDL_CREATE_EXIST = "CREATE\\s+(OR REPLACE\\s+)?(PROCEDURE|FUNCTION|PACKAGE|TRIGGER)\\s+((\"?\\w+\"?\\.)?\"?\\w+\"?)";
		String DDL_CREATE_EXIST_VIEW = "CREATE\\s+(OR REPLACE\\s+)?VIEW\\s+((\"?\\w+\"?\\.)?\"?\\w+\"?)\\s+AS\\s+((WITH\\s+.+AS\\s+.+\\s+)?SELECT\\s+.+\\s+FROM\\s+.+)";
		String DDL_CREATE_NOT_EXIST = "CREATE\\s+((GLOBAL\\s+TEMPORARY\\s+)?TABLE|(UNIQUE|BITMAP\\s+)?INDEX|SEQUENCE)\\s+((\"?\\w+\"?\\.)?\"?\\w+\"?)";
		String DDL_CREATE_TABLE = "CREATE\\s+((GLOBAL\\s+TEMPORARY\\s+)?TABLE)\\s+((\"?\\w+\"?\\.)?\"?\\w+\"?)";
		String DDL_ALTER = "ALTER\\s+(TABLE|INDEX|SEQUENCE|VIEW)\\s+((\"?\\w+\"?\\.)?\"?\\w+\"?)";
		String DDL_DROP = "DROP\\s+(TABLE|INDEX|SEQUENCE|VIEW)\\s+((\"?\\w+\"?\\.)?\"?\\w+\"?)";
		String DDL_TRUNCATE = "TRUNCATE\\s+TABLE\\s+((\"?\\w+\"?\\.)?\"?\\w+\"?)";
		String DML_INSERT = "INSERT\\s+INTO\\s+((\"?\\w+\"?\\.)?\"?\\w+\"?)";
		String DML_UPDATE = "UPDATE\\s+.+\\s+SET";
		String DML_SELECT = "((WITH\\s+.+AS\\s+.+\\s+)?SELECT\\s+.+\\s+FROM(\\s+.+)*)";
		String DML_DELETE = "DELETE\\s+(FROM\\s+)?((\"?\\w+\"?\\.)?\"?\\w+\"?)";
		String DML_GRANT = "GRANT\\s+TO\\s+\\w*ON\\s+((\"?\\w+\"?\\.)?\"?\\w+\"?)";
		String DML_REVOKE = "REVOKE\\s+FROM\\s+\\w*ON\\s+((\"?\\w+\"?\\.)?\"?\\w+\"?)";
	}

	protected static final ArrayList<String> ALL_VENDOR = new ArrayList<String>() {

		/**
		 * @return
		 * 
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{

			add("Linux");
			add("Linux 1");
			add("Linux 2");
			add("Linux 3");
			
			add("Windows");
			add("Aix");
			add("Solaris");
			add("Dell");

			add("Oracle");
			add("MySql");
			add("SqlServer");
			add("Timeten");
			add("Postgre");

			add("Ericsson");
			add("Nokia");
			add("Huawei");
			add("Zte");
			add("Elcom");
			add("Amdocs");
			add("Converse");

		}

	};

	public static final int OPT_EQ = 1; // Bằng ( kiểu Int ) hoặc giống với ký
										// tự ( kiểu String )
	public static final int OPT_GT = 2; // Lớn hơn ( kiểu Int )
	public static final int OPT_GTE = 3; // Lớn hơn hoặc bằng ( kiểu Int )
	public static final int OPT_AND = 4; // Xuất hiện đồng thời các ký tự tần
											// xuất của chúng ( kiểu String )
	public static final int OPT_OR = 5; // xuất hiện một trong các giá trị (
										// kiểu String )

	public static final int OPT_HAVE_CURRENT_DATE = 7; // Xuất hiện giá trị của
														// ngày hiện tại ( kiểu
														// String )
	public static final int OPT_FROM_TO = 8; // Từ giá trị A đến giá trị B (
												// kiểu Int )
	public static final int OPT_HAVE_LOG = 9; // TỒN TẠI LOG TRẢ VỀ
	public static final int OPT_HAVE_ROW_WITH_LIST_STRING = 10; // dòng có chứa
																// các ký tự với
																// tần xuất
	public static final int OPT_XUAT_HIEN_MOT_KY_TU_VOI_TAN_SUAT_LON_HON_HOAC_BANG = 11; // CHứa
																							// một
																							// ký
																							// tự
																							// với
																							// tần
																							// xuất
																							// lơn
																							// hơn
																							// hoac
																							// bang

	public static final int OPT_NOT_EQ = -1; // Khác ( kiểu Int ) hoặc không
												// giống với ký tự ( kiểu String
												// )
	public static final int OPT_LT = -2; // Nhỏ hơn ( kiểu Int )
	public static final int OPT_LTE = -3;
	// Nhỏ hơn hoặc bằng ( kiểu Int )
	/*
	 * public static final int OPT_NOT_AND=-4; // Không xuất hiện đồng thời các
	 * giá trị ( kiểu String )
	 */ public static final int OPT_NOT_OR = -5; // Không xuất hiện một trong các
												// giá trị ( kiểu String )
	public static final int OPT_NOT_HAVE_CURRENT_DATE = -7; // Không xuất hiện
															// giá trị của ngày
															// hiện tại ( kiểu
															// String )
	public static final int OPT_NOT_HAVE_LOG = -9; // KHÔNG TỒN TẠI LOG TRẢ VỀ

	public static final int FULL = 1;
	public static final int NOT_FULL = 0;
	public static final int VARCHAR_TYPE = 24;
	public static final int NUMERIC_TYPE = 2;
	public static final String SPLIST_CHAR = "\\|";

	public static final String AND_CHAR = "\\(&\\)";

	public static final String IPADDRESS_PATTERN = "^([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\." + "([01]?\\d\\d?|2[0-4]\\d|25[0-5])\\."
			+ "([01]?\\d\\d?|2[0-4]\\d|25[0-5])$";


	public static final int NOT_OK = 0;
	public static final int OK = 1;
	public static final String NONE = "NONE";

	public static final String DATE_CHAR = "%DATE";
	public static final String IP_CHAR = "%IP";
	public static final String APPLY_PORT = "%APPLY_PORT";
	public static final String APPLY_LOG_DIRECTORY_PATCH = "%APPLY_LOG_DIRECTORY_PATCH";
	public static final String APPLY_LOG_FILE_PATCH = "%APPLY_LOG_FILE_PATCH";
	public static final String TOMCAT_DIR = "%TOMCAT_DIR";

	public static final String SEVER_IP_CHAR = "{IP}";
	public static final String SEVER_PORT_CHAR = "{PORT}";
	public static final String SEVER_USERNAME_CHAR = "{USERNAME}";
	public static final String SEVER_PASSWORD_CHAR = "{PASSWORD}";

	public static final int YES_NO_COMMAND_TYPE = 1;

	public static final int DATA_LOCATE_IN_COLUMN_NAME_TYPE = 0; // CỘT DATA NẰM
																	// CHỌN
																	// TRONG CỘT
																	// TIÊU ĐỀ
	public static final int DATA_BEGIN_WITH_COLUMN_NAME_TYPE = 1; // BẮT ĐẦU TỪ
																	// TIÊU ĐỀ
	public static final int DATA_END_WITH_COLUMN_NAME_TYPE = 2; // BẮT ĐẦU TỪ
																// TIÊU ĐỀ
	public static final int DATA_CONTAIN_COLUMN_NAME_TYPE = 3; // CỘT TIÊU ĐÈ
																// NẰM CHỌN
																// TRONG CỌT
																// DATA

	public static final int KPI_SERVER_TYPE =1;
	public static final int KPI_DB_TYPE =2;

	protected static final HashMap<Integer, String> operationMap = new HashMap<Integer, String>() {

		/**
		 *
		 */
		private static final long serialVersionUID = 1L;
		{
//			put( OPT_EQ,"Bằng");
//			put( OPT_GT,"Lớn hơn");
//			put( OPT_GTE,"Lớn hơn hoặc bằng");
//			put( OPT_AND,"Xuất hiện đồng thời các ký tự với tần xuất của chúng");
//			put( OPT_OR,"Xuất hiện một trong các giá trị");
//			put( OPT_HAVE_CURRENT_DATE,"Xuất hiện giá trị của ngày hiện tại");
//			put( OPT_FROM_TO,"Từ giá trị A đến giá trị B");
//			put( OPT_HAVE_LOG,"Tồn tại log kết quả trả về");
//			put( OPT_HAVE_ROW_WITH_LIST_STRING,"Dòng có chứa các các cụm từ với tần xuất");
//			put( OPT_NOT_EQ,"Khác");
//			put( OPT_LT,"Nhỏ hơn");
//			put( OPT_LTE,"Nhỏ hơn hoặc bằng");
			put( OPT_EQ,"Equal");
			put( OPT_GT,"Greater than");
			put( OPT_GTE,"Greater than or equal");
			put( OPT_AND,"Appear characters with their frequency concurrently");
			put( OPT_OR,"Appear one of the values");
			put( OPT_HAVE_CURRENT_DATE,"Appear values of current day");
			put( OPT_FROM_TO,"from value A to value B");
			put( OPT_HAVE_LOG,"Returned log is existed");
			put( OPT_HAVE_ROW_WITH_LIST_STRING,"Line contains many words with frequency");
			put( OPT_NOT_EQ,"Not equal");
			put( OPT_LT,"Less than");
			put( OPT_LTE,"Less than or equal");
			/*
			 * put( "Không xuất hiện đồng thời các giá trị",
			 * OPT_NOT_AND) );
			 */
//			put( OPT_NOT_OR,"Không xuất hiện một trong các giá trị");
//			put( OPT_NOT_HAVE_CURRENT_DATE,"Không xuất hiện giá trị của ngày hiện tại");
//			put( OPT_NOT_HAVE_LOG,"Không tồn tại log kết quả trả về");
//			put( OPT_XUAT_HIEN_MOT_KY_TU_VOI_TAN_SUAT_LON_HON_HOAC_BANG,"Xuất hiện một chuỗi ký tự với tần suất lơn hơn hoặc bằng");
			put( OPT_NOT_OR,"Disappear one of the values");
			put( OPT_NOT_HAVE_CURRENT_DATE,"Disappear values of cuerrent day");
			put( OPT_NOT_HAVE_LOG,"Returned result log is not existed");
			put( OPT_XUAT_HIEN_MOT_KY_TU_VOI_TAN_SUAT_LON_HON_HOAC_BANG,"Appear characters with frequency greater than or equal");
		}

	};

	public static HashMap<Integer, String> getOperationMap() {
		return operationMap;
	}

	public static final int VIEW_ACTION=1;
	public static final int STOP_ACTION=2;
	public static final int START_ACTION=3;
	public static final int RESTART_ACTION=4;

	public static final int STAND_BY_STATUS=0;
	public static final int RUNNING_STATUS=1;
	public static final int FINISH_SUCCESS_STATUS=2;
	public static final int FINISH_FAIL_STATUS=-2;
	public static final int CANCEL_STATUS=-10;
	public static final int NOT_ALLOW_STATUS=-11;
	public static final int WARNING_STATUS=-12;
	public static final int FINISH_SUCCESS_WITH_WARNING=3;

	protected static final List<SelectItem> statusList =new ArrayList<SelectItem>(){
		private static final long serialVersionUID = 1L;
		{
//		    add(new SelectItem( STAND_BY_STATUS,"Đang chờ"));
//			add(new SelectItem( RUNNING_STATUS,"Đang thực hiện"));
//			add(new SelectItem( FINISH_SUCCESS_STATUS,"Thành công"));
//			add(new SelectItem( FINISH_FAIL_STATUS,"Thất bại"));
//			add(new SelectItem( FINISH_SUCCESS_WITH_WARNING,"Thành công nhưng có Cảnh báo"));
			add(new SelectItem( STAND_BY_STATUS,"Waiting"));
			add(new SelectItem( RUNNING_STATUS,"Executing"));
			add(new SelectItem( FINISH_SUCCESS_STATUS,"Successful"));
			add(new SelectItem( FINISH_FAIL_STATUS,"Fail"));
			add(new SelectItem( FINISH_SUCCESS_WITH_WARNING,"Successful with warning"));
		}
	};

	public static List<SelectItem> getStatusList() {
		return statusList;
	}

	protected static final HashMap<Integer, String> statusMap = new HashMap<Integer, String>() {
		private static final long serialVersionUID = 1L;
		{
//			put( STAND_BY_STATUS,"Đang chờ thực hiện");
//			put( RUNNING_STATUS,"Đang thực hiện");
//			put( FINISH_SUCCESS_STATUS,"Kết thúc thành công");
//			put( FINISH_FAIL_STATUS,"Kết thúc thất bại");
//			put( CANCEL_STATUS,"Bị hủy bỏ");
//			put( NOT_ALLOW_STATUS,"Không được phép thực hiện");
//			put( WARNING_STATUS,"Cảnh báo");
//			put( FINISH_SUCCESS_WITH_WARNING,"Thành công nhưng có Cảnh báo");
			//tuanda38_20180629
			put( STAND_BY_STATUS,"Waiting to execute");
			put( RUNNING_STATUS,"Executing");
			put( FINISH_SUCCESS_STATUS,"Finish successfully");
			put( FINISH_FAIL_STATUS,"Finish fail");
			put( CANCEL_STATUS,"Cancel");
			put( NOT_ALLOW_STATUS,"No permission to execute");
			put( WARNING_STATUS,"Warning");
			put( FINISH_SUCCESS_WITH_WARNING,"Successful with warning");
	    }

	};

	public static HashMap<Integer, String> getStatusMap() {
		return statusMap;
	}

	public static final int RUN_FULL=1;
	public static final int RUN_AGAIN=2;

	public static final String LINUX_CMD_KILL = "kill -9 @pid";
	public static final String LINUX_CMD_CHECK_PORT = "netstat -apn | grep :@port | grep -v grep";

	public static final Long ACTION_TYPE_CR_NORMAL = 0L;
	public static final Long ACTION_TYPE_CR_UCTT = 1L;
	public static final Long ACTION_TYPE_KB_UCTT = 2L;

	public static final String STEP_BACKUP = "0";
	public static final String STEP_STOP = "1";
	public static final String STEP_UPCODE = "2";
	public static final String STEP_CLEARCACHE = "3";
	public static final String STEP_RESTART = "4";
	public static final String STEP_START = "5";
	public static final String STEP_RESTART_CMD = "6";
	public static final String STEP_CHECK_STATUS = "14";
	public static final String STEP_UPCODE_STOP_START = "15";
	public static final String STEP_ROLLBACK_CODE_STOP_START = "16";
    /*20181030_hoangnd_save all step_start*/
	public static final String STEP_CHECKLIST_APP = "17";
	public static final String STEP_CHECKLIST_DB = "18";
	public static final String STEP_ROLLBACK_DB = "19";
	public static final String STEP_ROLLBACK_CHECK_STATUS = "20";
    /*20181030_hoangnd_save all step_end*/

	public static final String STEP_ROLLBACKCODE = "7";
	public static final String STEP_ROLLBACK_STOP = "8";
	public static final String STEP_ROLLBACK_CLEARCACHE = "9";
	public static final String STEP_ROLLBACK_RESTART = "10";
	public static final String STEP_ROLLBACK_START = "11";
	public static final String STEP_ROLLBACK_RESTART_CMD = "12";
	public static final String STEP_CHECKVERSION_APP = "13";

	public static final Integer EXE_CHECKLIST = 1;
	public static final Integer EXE_TD = 2;
	public static final Integer EXE_ROLLBACK = 3;
	public static final Integer EXE_CHECKLIST_LAST = 4;
    /*20180824_hoangnd_hoangnd_send mail/sms action ci/cd_start*/
	public static final Integer EXE_CI_CD_SEND_BEFORE_RUN = 5;
	public static final Integer EXE_CI_CD_SEND_TIME_TEST = 6;
	public static final Integer EXE_CI_CD_SEND_BEFORE_ROLLBACK = 7;
    /*20180824_hoangnd_hoangnd_send mail/sms action ci/cd_end*/

	public static final Long TD_DB_TYPE_SCRIPT = 0L;
	public static final Long TD_DB_TYPE_IMPORT = 1L;
	public static final Long TD_DB_TYPE_STOP_START = 2L;

	protected static final Map<MapEntry, RunStep> steps = new HashMap<MapEntry, RunStep>() {
		{
			RunStep step = new RunStep();
			step.setValue(new MapEntry(1, 1));
			step.setLabel("Checklist app");
			//tuanda38_20180629
//			step.setDescription("Checklist ứng dụng trước tác động");
			step.setDescription("Checklist application before impacting");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(2, 1));
			step.setLabel("Checklist db");
//			step.setDescription("Checklist db trước tác động");
			step.setDescription("Checklist database before impacting");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(SUB_STEP_CHECK_STATUS, 1));
			step.setLabel("Check status");
//			step.setDescription("Kiểm tra trạng thái tiến trình");
			step.setDescription("Check status of process");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(SUB_STEP_STOP_APP, 1));
			step.setLabel("Stop app");
//			step.setDescription("Dừng ứng dụng");
			step.setDescription("Stop application");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(SUB_STEP_BACKUP_APP, 1));
			step.setLabel("Backup app");
//			step.setDescription("Backup code ứng dụng");
			step.setDescription("Backup application code");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(SUB_STEP_BACKUP_DB, 1));
			step.setLabel("Backup db");
			step.setDescription("Backup database");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(SUB_STEP_UPCODE, 1));
			step.setLabel("Upcode");
			step.setDescription("Upcode");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(SUB_STEP_TD_DB, 1));
			step.setLabel("Impact db");
//			step.setDescription("Tác động db");
			step.setDescription("Impact database");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(SUB_STEP_CLEARCACHE, 1));
			step.setLabel("Clear cache");
			step.setDescription("Clear cache");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(SUB_STEP_RESTART_APP, 1));
			step.setLabel("Restart app");
//			step.setDescription("Restart ứng dụng");
			step.setDescription("Restart application");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(SUB_STEP_START_APP, 1));
			step.setLabel("Start app");
//			step.setDescription("Start ứng dụng");
			step.setDescription("Start application");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(SUB_STEP_START_APP, 1));
			step.setLabel("Start app");
//			step.setDescription("Start ứng dụng");
			step.setDescription("Start application");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(SUB_STEP_UPCODE_START_APP, 1));
			step.setLabel("Upcode stop/start");
//			step.setDescription("Upcode và stop/start ứng dụng");
			step.setDescription("Upcode and stop/start application");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(SUB_STEP_CHECKLIST_APP, 1));
			step.setLabel("Checklist app");
//			step.setDescription("Checklist ứng dụng");
			step.setDescription("Checklist application");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(SUB_STEP_CHECKLIST_DB, 1));
			step.setLabel("Checklist db");
//			step.setDescription("Checklist db");
			step.setDescription("Checklist database");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(ROLLBACK_STEP_CHECK_STATUS, 1));
			step.setLabel("Check status");
//			step.setDescription("Kiểm tra trạng thái tiến trình");
			step.setDescription("Check status of process");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(ROLLBACK_STEP_STOP_APP, 1));
			step.setLabel("Stop app");
//			step.setDescription("Dừng ứng dụng");
			step.setDescription("Stop application");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(ROLLBACK_STEP_SOURCE_CODE, 1));
			step.setLabel("Rollback code");
			step.setDescription("Rollback code");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(ROLLBACK_STEP_DB, 1));
			step.setLabel("Rollback db");
			step.setDescription("Rollback database");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(ROLLBACK_STEP_CLEARCACHE, 1));
			step.setLabel("Clear cache");
			step.setDescription("Clear cache");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(ROLLBACK_STEP_RESTART_APP, 1));
			step.setLabel("Restart app");
//			step.setDescription("Restart ứng dụng");
			step.setDescription("Restart application");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(ROLLBACK_STEP_START_APP, 1));
			step.setLabel("Start app");
//			step.setDescription("Start ứng dụng");
			step.setDescription("Start application");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(ROLLBACK_STEP_UPCODE_START, 1));
			step.setLabel("Rollback code stop/start");
//			step.setDescription("Rollback code và stop/start ứng dụng");
			step.setDescription("Rollback code and stop/start application");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(ROLLBACK_STEP_CHECKLIST_APP, 1));
			step.setLabel("Checklist app");
//			step.setDescription("Checklist app sau rollback");
			step.setDescription("Checklist application after rollback");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(ROLLBACK_STEP_CHECKLIST_DB, 1));
			step.setLabel("Checklist db");
//			step.setDescription("Checklist db sau rollback");
			step.setDescription("Checklist database after rollback");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(30, 1));
			step.setLabel("CR information");
//			step.setDescription("Thông tin chi tiết tác động CR");
			step.setDescription("CR information");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(AFTER_STEP_CHECKLIST_APP, 1));
			step.setLabel("Checklist app");
//			step.setDescription("Checklist ứng dụng sau tác động");
			step.setDescription("Checklist application after impacting");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(AFTER_STEP_CHECKLIST_DB, 1));
			step.setLabel("Checklist db");
//			step.setDescription("Checklist db sau tác động");
			step.setDescription("Checklist database after impacting");
			put(step.getValue(), step);

			/*20181115_hoangnd_save all step_start*/
			step = new RunStep();
			step.setValue(new MapEntry(BEFORE_STEP_CHECKLIST_APP, 1));
			step.setLabel("Checklist app");
//			step.setDescription("Checklist ứng dụng sau tác động");
			step.setDescription("Checklist application before impacting");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(BEFORE_STEP_CHECKLIST_DB, 1));
			step.setLabel("Checklist db");
//			step.setDescription("Checklist db sau tác động");
			step.setDescription("Checklist database before impacting");
			put(step.getValue(), step);
			/*20181115_hoangnd_save all step_end*/

			step = new RunStep();
			step.setValue(new MapEntry(AFTER_STEP_CHECK_VERSION, 1));
			step.setLabel("Check version");
//			step.setDescription("Kiểm tra version sau tác động");
			step.setDescription("Check version after impacting");
			put(step.getValue(), step);
			
			//longlt6 add
			step = new RunStep();
			step.setValue(new MapEntry(26, 1));
			step.setLabel("Overview");
//			step.setDescription("Tổng quan các bước tác động");
			step.setDescription("General information");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(-1, 1));
			step.setLabel("Before impacting");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(-2, 1));
			step.setLabel("Impacting");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(-3, 1));
			step.setLabel("After impacting");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(-4, 1));
			step.setLabel("Rollback");
			put(step.getValue(), step);

			step = new RunStep();
			step.setValue(new MapEntry(-5, 1));
			step.setLabel("Information");
			put(step.getValue(), step);
			
			step = new RunStep();
			step.setValue(new MapEntry(-6, 1));
			step.setLabel("Overview");
			put(step.getValue(), step);
		}
	};

	public static Map<MapEntry, RunStep> getSteps () {
		return steps;
	}

	public static RunStep getRunStep(Integer entrykey) {
		return getRunStep(new MapEntry(entrykey, 1));
	}

	public static RunStep getRunStep(MapEntry mapEntry) {
		RunStep runStep = steps.get(new MapEntry(mapEntry.getKey(), 1));
		if (runStep == null)
			return null;

		RunStep step = new RunStep();
		step.setValue(mapEntry);
		step.setLabel(runStep.getLabel());
		step.setDescription(runStep.getDescription());
		step.setStatus(runStep.getStatus());
		if (mapEntry.getKey() > 100) {
			step.setLabel(step.getLabel() + " - " + mapEntry.getValue());
		}

		return step;
	}

	public final static Long CR_STATE_DRAFT = 0L;//
	public final static Long CR_STATE_OPEN = 1L;
	public final static Long CR_STATE_QUEUE = 2L;
	public final static Long CR_STATE_COORDINATE = 3L;
	public final static Long CR_STATE_EVALUATE = 4L;
	public final static Long CR_STATE_APPROVE = 5L;
	public final static Long CR_STATE_ACCEPT = 6L;
	public final static Long CR_STATE_RESOLVE = 7L;
	public final static Long CR_STATE_INCOMPLETE = 8L;
	public final static Long CR_STATE_CLOSE = 9L;
	public final static Long CR_STATE_CANCEL = 10L;

	public final static String CR_DEFAULT = "CR_CNTT_TEMP_999999";
//	public final static String CR_DEFAULT = "";

	protected static final ArrayList<String> PROMPTS = new ArrayList<String>() {

		private static final long serialVersionUID = 1L;
		{
			add("]$");
			add("$");
			add("]");
			add("#");
			add(">");
			add("<");
			add(":");
		}

	};

	public static ArrayList<String> getPROMPTS() {
		return PROMPTS;
	}

	public static final int SPECIAL_UPCODETEST_RESTART_STOP_START = 100;
	public static final int SPECIAL_UPCODETEST_RESTART = 101;
	public static final int SPECIAL_UPCODETEST_START = 102;
	public static final int SPECIAL_UPCODETEST_STOP_START = 103;
	public static final int SPECIAL_RESTART_STOP_START = 110;
	public static final int SPECIAL_RESTART = 111;
	public static final int SPECIAL_START = 112;
	public static final int SPECIAL_STOP_START = 113;

	public static final int SPECIAL_ROLLBACKTEST_RESTART_STOP_START = 200;
	public static final int SPECIAL_ROLLBACKTEST_RESTART = 201;
	public static final int SPECIAL_ROLLBACKTEST_STOP = 202;
	public static final int SPECIAL_ROLLBACKTEST_STOP_START = 203;
	public static final int SPECIAL_ROLLBACK_RESTART_STOP_START = 210;
	public static final int SPECIAL_ROLLBACK_RESTART = 211;
	public static final int SPECIAL_ROLLBACK_STOP = 212;
	public static final int SPECIAL_ROLLBACK_STOP_START = 213;

	public static final int SUB_STEP_CHECK_STATUS = 101;
	public static final int SUB_STEP_STOP_APP = 102;
	public static final int SUB_STEP_BACKUP_APP = 103;
	public static final int SUB_STEP_BACKUP_DB = 104;
	public static final int SUB_STEP_UPCODE = 105;
	public static final int SUB_STEP_TD_DB = 106;
	public static final int SUB_STEP_CLEARCACHE = 107;
	public static final int SUB_STEP_RESTART_APP = 108;
	public static final int SUB_STEP_START_APP = 109;
	public static final int SUB_STEP_CHECKLIST_APP = 110;
	public static final int SUB_STEP_CHECKLIST_DB = 111;
	public static final int SUB_STEP_UPCODE_START_APP = 112;


	public static final int CUSTOM_STEP_CHECK_STATUS = 121;
	public static final int CUSTOM_STEP_STOP_APP = 122;
	public static final int CUSTOM_STEP_BACKUP_APP = 123;
	public static final int CUSTOM_STEP_BACKUP_DB = 124;
	public static final int CUSTOM_STEP_UPCODE = 125;
	public static final int CUSTOM_STEP_TD_DB = 126;
	public static final int CUSTOM_STEP_CLEARCACHE = 127;
	public static final int CUSTOM_STEP_RESTART_APP = 128;
	public static final int CUSTOM_STEP_START_APP = 129;
	/*20181031_hoangnd_save all step_start*/
	public static final int CUSTOM_STEP_CHECKLIST_APP = 130;
	public static final int CUSTOM_STEP_CHECKLIST_DB = 131;
    /*20181031_hoangnd_save all step_end*/

	public static final int AFTER_STEP_CHECKLIST_APP = 201;
	public static final int AFTER_STEP_CHECKLIST_DB = 202;
	public static final int AFTER_STEP_CHECK_VERSION = 203;
	/*20181115_hoangnd_save all step_start*/
	public static final int BEFORE_STEP_CHECKLIST_APP = 1;
	public static final int BEFORE_STEP_CHECKLIST_DB = 2;
    /*20181115_hoangnd_save all step_end*/

	public static final int ROLLBACK_STEP_STOP_APP = 301;
	public static final int ROLLBACK_STEP_SOURCE_CODE = 302;
	public static final int ROLLBACK_STEP_DB = 303;
	public static final int ROLLBACK_STEP_CLEARCACHE = 304;
	public static final int ROLLBACK_STEP_RESTART_APP = 305;
	public static final int ROLLBACK_STEP_START_APP = 306;
	public static final int ROLLBACK_STEP_UPCODE_START = 309;
	public static final int ROLLBACK_STEP_CHECK_STATUS = 310;
	public static final int ROLLBACK_STEP_CHECKLIST_APP = 307;
	public static final int ROLLBACK_STEP_CHECKLIST_DB = 308;

	public static final int ROLLBACK_CUSTOM_STEP_CHECK_STATUS = 321;
	public static final int ROLLBACK_CUSTOM_STEP_STOP_APP = 322;
	public static final int ROLLBACK_CUSTOM_STEP_BACKUP_APP = 323;
	public static final int ROLLBACK_CUSTOM_STEP_BACKUP_DB = 324;
	public static final int ROLLBACK_CUSTOM_STEP_UPCODE = 325;
	public static final int ROLLBACK_CUSTOM_STEP_TD_DB = 326;
	public static final int ROLLBACK_CUSTOM_STEP_CLEARCACHE = 327;
	public static final int ROLLBACK_CUSTOM_STEP_RESTART_APP = 328;
	public static final int ROLLBACK_CUSTOM_STEP_START_APP = 329;
	
	public static final Integer OPEN_CHANNEL_SUCCESS = 1 ;
	public static final Integer OPEN_CHANNEL_FAIL = 0 ;

	/*20181117_hoangnd_save all step_start*/
	public int getBEFORE_STEP_CHECKLIST_APP() {
		return BEFORE_STEP_CHECKLIST_APP;
	}

	public int getBEFORE_STEP_CHECKLIST_DB() {
		return BEFORE_STEP_CHECKLIST_DB;
	}
    /*20181117_hoangnd_save all step_end*/

	public int getSUB_STEP_CHECK_STATUS () {
		return SUB_STEP_CHECK_STATUS;
	}
	public int getSUB_STEP_STOP_APP () {
		return SUB_STEP_STOP_APP;
	}
	public int getSUB_STEP_BACKUP_APP () {
		return SUB_STEP_BACKUP_APP;
	}
	public int getSUB_STEP_BACKUP_DB () {
		return SUB_STEP_BACKUP_DB;
	}
	public int getSUB_STEP_UPCODE () {
		return SUB_STEP_UPCODE;
	}
	public int getSUB_STEP_TD_DB () {
		return SUB_STEP_TD_DB;
	}
	public int getSUB_STEP_CLEARCACHE () {
		return SUB_STEP_CLEARCACHE;
	}
	public int getSUB_STEP_RESTART_APP () {
		return SUB_STEP_RESTART_APP;
	}
	public int getSUB_STEP_START_APP () {
		return SUB_STEP_START_APP;
	}
	public int getSUB_STEP_UPCODE_START_APP () {
		return SUB_STEP_UPCODE_START_APP;
	}
	public int getSUB_STEP_CHECKLIST_APP () {
		return SUB_STEP_CHECKLIST_APP;
	}
	public int getSUB_STEP_CHECKLIST_DB () {
		return SUB_STEP_CHECKLIST_DB;
	}
	public int getAFTER_STEP_CHECKLIST_APP () {
		return AFTER_STEP_CHECKLIST_APP;
	}
	public int getAFTER_STEP_CHECKLIST_DB () {
		return AFTER_STEP_CHECKLIST_DB;
	}
	public int getAFTER_STEP_CHECK_VERSION () {
		return AFTER_STEP_CHECK_VERSION;
	}
	public int getROLLBACK_STEP_STOP_APP () {
		return ROLLBACK_STEP_STOP_APP;
	}
	public int getROLLBACK_STEP_SOURCE_CODE () {
		return ROLLBACK_STEP_SOURCE_CODE;
	}
	public int getROLLBACK_STEP_DB () {
		return ROLLBACK_STEP_DB;
	}
	public int getROLLBACK_STEP_CLEARCACHE () {
		return ROLLBACK_STEP_CLEARCACHE;
	}
	public int getROLLBACK_STEP_RESTART_APP () {
		return ROLLBACK_STEP_RESTART_APP;
	}
	public int getROLLBACK_STEP_START_APP () {
		return ROLLBACK_STEP_START_APP;
	}
	public int getROLLBACK_STEP_UPCODE_START () {
		return ROLLBACK_STEP_UPCODE_START;
	}
	public int getROLLBACK_STEP_CHECKLIST_APP () {
		return ROLLBACK_STEP_CHECKLIST_APP;
	}
	public int getROLLBACK_STEP_CHECKLIST_DB () {
		return ROLLBACK_STEP_CHECKLIST_DB;
	}

	public int getCustomStepCheckStatus() {
		return CUSTOM_STEP_CHECK_STATUS;
	}

	public int getCustomStepStopApp() {
		return CUSTOM_STEP_STOP_APP;
	}

	public int getCustomStepBackupApp() {
		return CUSTOM_STEP_BACKUP_APP;
	}

	public int getCustomStepBackupDb() {
		return CUSTOM_STEP_BACKUP_DB;
	}

	public int getCustomStepUpcode() {
		return CUSTOM_STEP_UPCODE;
	}

	public int getCustomStepTdDb() {
		return CUSTOM_STEP_TD_DB;
	}

	public int getCustomStepClearcache() {
		return CUSTOM_STEP_CLEARCACHE;
	}

	public int getCustomStepRestartApp() {
		return CUSTOM_STEP_RESTART_APP;
	}

	public int getCustomStepStartApp() {
		return CUSTOM_STEP_START_APP;
	}

	public int getRollbackCustomStepCheckStatus() {
		return ROLLBACK_CUSTOM_STEP_CHECK_STATUS;
	}

	public int getRollbackCustomStepStopApp() {
		return ROLLBACK_CUSTOM_STEP_STOP_APP;
	}

	public int getRollbackCustomStepBackupApp() {
		return ROLLBACK_CUSTOM_STEP_BACKUP_APP;
	}

	public int getRollbackCustomStepBackupDb() {
		return ROLLBACK_CUSTOM_STEP_BACKUP_DB;
	}

	public int getRollbackCustomStepUpcode() {
		return ROLLBACK_CUSTOM_STEP_UPCODE;
	}

	public int getRollbackCustomStepTdDb() {
		return ROLLBACK_CUSTOM_STEP_TD_DB;
	}

	public int getRollbackCustomStepClearcache() {
		return ROLLBACK_CUSTOM_STEP_CLEARCACHE;
	}

	public int getRollbackCustomStepRestartApp() {
		return ROLLBACK_CUSTOM_STEP_RESTART_APP;
	}

	public int getRollbackCustomStepStartApp() {
		return ROLLBACK_CUSTOM_STEP_START_APP;
	}

	public static final String KEY_LOG_START = "start_success";
	public static final String KEY_STATUS_START = "output_status_start";

	public static void main(String[] args) {
		System.out.println(steps.get(new MapEntry(1, 1)).getLabel());
	}

	public static final String PREFIX_MOP_INFRA = "MOP_HT_";
//	public static final String DEFAULT_CR_NUMBER_INFRA = "DEFAULT_CR_NUMBER_INFRA_0001";
	public static final String DEFAULT_CR_NUMBER_INFRA = "CR_DEFAULT";


	public final static String DT_EXECUTE = "1";
	public final static String DT_ROLLBACK = "2";
	public final static String DT_SCRIPT = "3";
	public final static String FORM_TEST_SERVICE = "4";
	public final static String CHECK_KPI = "5";
	public final static String PLAN = "6";
	public final static String GUILINE = "7";
	public final static String AFFECTED = "8";
	public final static String RESULT = "9";
	public final static String LOG_IMPACT = "10";
	public final static String OTHER = "100";
	public final static String IMPORT_BY_PROCESS_IN = "101";
	public final static String IMPORT_BY_PROCESS_OUT = "102";
	public final static String IMPORT_RESULT = "103";

	public final static Integer CR_STATE_UCTT_CREATE = 201;
	public final static Integer CR_STATE_UCTT_APPROVE = 202;

	public final static Integer UCTT_TYPE_RESTART = 0;
	public final static Integer UCTT_TYPE_SERVER_DOWN = 1;
	public final static Integer UCTT_TYPE_SERVER_DB_DOWN = 2;
	public final static Integer UCTT_TYPE_SPECIAL = 3;

	public final static String NA_VALUE = "N/A";
	public final static String CODETAPTRUNG_TYPE = "CODE_TAP_TRUNG";

	public final static Integer MAX_PER_KEY_POOL = 2;
	public class systemUpdateResult {

		public static final int NoSystem = 0;
		public static final int GNOC_CR = 1;
		public static final int GNOC_TT = 2;
		public static final int NCMS = 3;

	}
	//01-11-2018 KienPD validate time out start
	public static final String TIME_OUT_MAX = "TIME_OUT_MAX";
	public static final String TIME_OUT_MIN  = "TIME_OUT_MIN";
	//01-11-2018 KienPD validate time out end

	// 03-12-2018 KienPD check server dead start
	public static final String TIME_CALL_SERVER_DEAD = "TIME_CALL_SERVER_DEAD";
	public static final String NUMBER_RETRY_SERVER_DEAD  = "NUMBER_RETRY_SERVER_DEAD";
	public static final String CONFIG_GROUP = "SERVER_DEAD";
	// 03-12-2018 KienPD check server dead end
	/*20181217_hoangnd_them trang thai runnibg status_start*/
    public static class RunningStatus {
		public static final Integer SUCCESS = 1;
		public static final Integer RUNNING = 2;
		public static final Integer FAIL = 3;
		public static final Integer CONFIRM = 4;
	}
    /*20181217_hoangnd_them trang thai runnibg status_end*/
	public static class status {
		public static final Long active = 1L;
		public static final Long inActive = 0L;
	}
	//20181023_tudn_start load pass security
	public static final String ACCOUNT_TYPE_SERVER = "1";
	public static final String ACCOUNT_TYPE_DATABASE = "2";

	public static final String SECURITY_NODE = "1";
	public static final String SECURITY_SERVER = "2";
	public static final String SECURITY_DATABASE = "3";
	//20181023_tudn_end load pass security

	//20190416_tudn_start import rule config
	public static class ChangeFileConfigType {
		public static final String CFG_GROUP = "CHANGE_CONFIG";
		public static final String CFG_KEY = "FILE_UPLOAD_TYPE";
	}

	public static class RuleConfig {
		public static final String RULE_ADD_ON_BOTTOM = "RULE_ADD_ON_BOTTOM";
		public static final String RULE_ADD_ON_HEAD = "RULE_ADD_ON_HEAD";
		public static final String RULE_ADD_BEFORE_KEYWORD = "RULE_ADD_BEFORE_KEYWORD";
		public static final String RULE_ADD_AFTER_KEYWORD = "RULE_ADD_AFTER_KEYWORD";
		public static final String RULE_REPLACE_KEYWORD = "RULE_REPLACE_KEYWORD";
		public static final String RULE_ADD_BEFORE_LINE_KEYWORD = "RULE_ADD_BEFORE_LINE_KEYWORD";
		public static final String RULE_ADD_AFTER_LINE_KEYWORD = "RULE_ADD_AFTER_LINE_KEYWORD";
		public static final String RULE_DELETE_KEYWORD = "RULE_DELETE_KEYWORD";
	}
	//20190416_tudn_END import rule config
}
