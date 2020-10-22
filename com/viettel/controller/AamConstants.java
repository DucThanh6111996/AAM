package com.viettel.controller;

/**
 * @author quanns2
 */
public interface AamConstants {
    interface IIM_WS_CODE {
        String GET_UNIT_AAM = "GET_UNIT_AAM";
        String GET_CHILDREN_UNIT_AAM = "GET_CHILDREN_UNIT_AAM";

        /*String GET_SERVICES_AAM = "GET_SERVICES_AAM";
        String GET_MODULE_AAM = "GET_MODULE_AAM";
        String GET_SERVICES_DB_SERVICES_AAM = "GET_SERVICES_DB_SERVICES_AAM";
        String GET_SERVICES_DB_AAM = "GET_SERVICES_DB_AAM";
        String COUNT_FILTER_MODULE_AAM = "COUNT_FILTER_MODULE_AAM";
        String GET_FILTER_MODULE_AAM = "GET_FILTER_MODULE_AAM";
        String GET_IP_SERVICE_AAM = "GET_IP_SERVICE_AAM";
        String GET_APP_LOG_OS_AAM = "GET_APP_LOG_OS_AAM";
        String GET_SERVICES_BY_MODULES = "GET_SERVICES_BY_MODULES";
        String GET_SERVICE_BY_USER_AAM = "GET_SERVICE_BY_USER_AAM";
        String GET_SERVICE_BY_ID_AAM = "GET_SERVICE_BY_ID_AAM";*/

        String GET_SERVICES_AAM = "GET_SERVICES_AAM_V2";
        String GET_MODULE_AAM = "GET_MODULE_AAM_V2";
        String GET_SERVICES_DB_SERVICES_AAM = "GET_SERVICES_DB_SERVICES_AAM_V2_HOST";
//        String GET_SERVICES_DB_AAM = "GET_SERVICES_DB_AAM_V2";
        String GET_SERVICES_DB_AAM = "GET_SERVICES_DB_AAM_V2_HOST";
        String COUNT_FILTER_MODULE_AAM = "COUNT_FILTER_MODULE_AAM_V2";
        String GET_FILTER_MODULE_AAM = "GET_FILTER_MODULE_AAM_V2";
        String GET_FILTER_MODULE_AAM_NEW = "GET_FILTER_MODULE_AAM_V2_NEW";
        String GET_IP_SERVICE_AAM = "GET_IP_SERVICE_AAM_V2";
        String GET_APP_LOG_OS_AAM = "GET_APP_LOG_OS_AAM_V2";
        String GET_SERVICES_BY_MODULES = "GET_SERVICES_BY_MODULES_V2";
        String GET_SERVICE_BY_USER_AAM = "GET_SERVICE_BY_USER_AAM_V2";
        String GET_SERVICE_BY_ID_AAM = "GET_SERVICE_BY_ID_AAM_V2";
        String GET_MODULE_PATH_AAM_V2 = "GET_MODULE_PATH_AAM_V2";
        String GET_MODULE_BY_CODE_AAM_V2 = "GET_MODULE_BY_CODE_AAM_V2";
        String GET_MODULE_BY_CODE_AAM_V3 = "GET_MODULE_BY_CODE_AAM_V3";

        String GET_SERVICE_FOR_AAM_V2 = "GET_SERVICE_FOR_AAM_V2";
        //new bd/uctt
        String GET_DATABASES_AAM = "GET_DATABASES_AAM_V2";
        String GET_SERVICES_AAM_TEST = "GET_SERVICES_AAM_V2_TEST";
        String GET_IPS_AAM_TEST = "GET_IPS_AAM_V2_TEST";
        String GET_MODULE_UCTT_AAM_V2 = "GET_MODULE_UCTT_AAM_V2";
        String GET_MD_DEPENDENT_AAM = "GET_MD_DEPENDENT_AAM_V2";
        String GET_MODULE_FUNC_GROUP_AAM = "GET_MODULE_FUNC_GROUP_AAM_V2";
        String GET_OS_ACCOUNT_AAM = "GET_OS_ACCOUNT_AAM";
        String GET_MD_ACTION_SPECIAL_AAM = "GET_MD_ACTION_SPECIAL_AAM";
        String GET_DB_MONITOR_AAM = "GET_DB_MONITOR_AAM";
        String GET_MODULE_UCTT_WITH_DB_AAM_V2 = "GET_MODULE_UCTT_WITH_DB_AAM_V2";
        String GET_MODULE_OFFLINE_AAM = "GET_MODULE_OFFLINE_AAM_V2";
        String GET_MODULE_DB_DR = "GET_MODULE_DB_DR";


        String GET_SERVICES_USER_AAM = "GET_SERVICES_USER_AAM";
        String GET_SERVERS_FOR_AAM = "GET_SERVERS_FOR_AAM";
        String GET_SV_USER_FOR_AAM = "GET_SV_USER_FOR_AAM";
        String GET_DATABASES_FOR_AAM = "GET_DATABASES_FOR_AAM";
        String V_AOM_INSTANCES_MYSQL = "V_AOM_INSTANCES_MYSQL";

        String GET_ALL_USER_DB_SERVICE_FOR_AAM = "GET_ALL_USER_DB_SERVICE_FOR_AAM";
        String GET_ALL_USER_SV_SERVICE_FOR_AAM = "GET_ALL_USER_SV_SERVICE_FOR_AAM";
    }

    interface AOM_WS_CODE {
        String CHECK_LIST_AAM_SERVER = "CHECK_LIST_AAM_SERVER";
        String CHECK_LIST_AAM_MODULE = "CHECK_LIST_AAM_MODULE";
        String CHECK_LIST_AAM_DATABASE = "CHECK_LIST_AAM_DATABASE";
        String CHECK_LIST_AAM_DATABASE_BY_ID = "CHECK_LIST_AAM_DATABASE_BY_ID";
    }

    interface MODULE_FUNCTION_TYPE {
        String NORMAL = "Normal";
        String BACKUP_OFFLINE = "BackupOffline";
        String TESTBED = "TestBad";
    }

    interface MD_DEPENDENT {
        String STOP = "Stop";
        String START = "Start";
        String RESTART = "Restart";
        String CODE = "Restart";
        String START_RESTART = "Start_Restart";
    }

    interface MD_PATH {
        String EXECUTE_PATH = "EXECUTE_PATH";
        String LOG_PATH = "LOG_PATH";
        String SOURCE_CODE = "SOURCE_CODE";
    }

    interface MODULE_GROUP_ACTION {
        int STOP_START = 0;
        int UPCODE = 1;

        int STOP_START_UPCODE = 2;
        int RESTART_STOP_START = 3;
        int RESTART_STOP_START_UPCODE = 4;
        int RESTART = 5;
        int RESTART_UPCODE = 6;
        int CHECKAPP = 7;
        int STOP = 9;
        int START = 8;

        int SWICH_DR = 11;
        int SWICH_DR_STOP_START = 12;
    }

    interface AAM_WS_CODE {
        String UPDATE_ACTION_STATUS = "UPDATE_ACTION_STATUS";
        String UPDATE_TESTBED_RUN_STATUS = "UPDATE_TESTBED_RUN_STATUS";
        String UPDATE_BACKUP_STATUS_APP = "UPDATE_BACKUP_STATUS_APP";
        String UPDATE_RUN_STATUS_APP = "UPDATE_RUN_STATUS_APP";
        String UPDATE_ROLLBACK_STATUS_APP = "UPDATE_ROLLBACK_STATUS_APP";
        String UPDATE_BACKUP_STATUS_DB = "UPDATE_BACKUP_STATUS_DB";
        String UPDATE_RUN_STATUS_DB = "UPDATE_RUN_STATUS_DB";
        String UPDATE_ROLLBACK_STATUS_DB = "UPDATE_ROLLBACK_STATUS_DB";
        String INSERT_ACTION_HISTORY = "INSERT_ACTION_HISTORY_TEST";
        String UPDATE_ACTION_HISTORY = "UPDATE_ACTION_HISTORY_TEST";
        String UPDATE_RUN_STATUS_CUSTOM = "UPDATE_RUN_STATUS_CUSTOM";
        String UPDATE_ROLLBACK_STATUS_CUSTOM = "UPDATE_ROLLBACK_STATUS_CUSTOM";
        String UPDATE_ACTION_RUNNING_STATUS = "UPDATE_ACTION_RUNNING_STATUS";
        /*20181008_hoangnd_continue fail step_start*/
        String SELECT_ACTION_HISTORY = "SELECT_ACTION_HISTORY";
        /*20181008_hoangnd_continue fail step_end*/

        /*20181116_hoangnd_save all step_start*/
        String UPDATE_CHECKLIST_APP_STATUS_BEFORE = "UPDATE_CHECKLIST_APP_STATUS_BEFORE";
        String UPDATE_CHECKLIST_APP_STATUS_IMPACT = "UPDATE_CHECKLIST_APP_STATUS_IMPACT";
        String UPDATE_CHECKLIST_APP_STATUS_AFTER = "UPDATE_CHECKLIST_APP_STATUS_AFTER";
        String UPDATE_CHECKLIST_APP_STATUS_ROLLBACK = "UPDATE_CHECKLIST_APP_STATUS_ROLLBACK";

        String UPDATE_APP_BEFORE_STATUS = "UPDATE_APP_BEFORE_STATUS";
        String UPDATE_APP_AFTER_STATUS = "UPDATE_APP_AFTER_STATUS";

        String UPDATE_CHECKLIST_DB_STATUS_BEFORE = "UPDATE_CHECKLIST_DB_STATUS_BEFORE";
        String UPDATE_CHECKLIST_DB_STATUS_IMPACT = "UPDATE_CHECKLIST_DB_STATUS_IMPACT";
        String UPDATE_CHECKLIST_DB_STATUS_AFTER = "UPDATE_CHECKLIST_DB_STATUS_AFTER";
        String UPDATE_CHECKLIST_DB_STATUS_ROLLBACK = "UPDATE_CHECKLIST_DB_STATUS_ROLLBACK";
        /*20181116_hoangnd_save all step_end*/

        /*20181123_hoangnd_log command_start*/
        String UPDATE_TEMP_FILE = "UPDATE_TEMP_FILE";
        /*20181123_hoangnd_log command_end*/
    }

    interface AOM_OPERATOR {
        String GE = ">=";
        String LE = "<=";
        String EQ = "=";
        String LT = "<";
        String GT = ">";
    }

    interface RUNNING_STATUS {
        int SUCCESS = 1;
        int FAIL = 3;
        int RUNNING = 2;
        int WAIT = 4;
    }

    interface TESTBED_MODE {
        int NORMAL = 0;
        int TESTBED = 1;
    }

    interface CHECKLIST_CODE {
        String CPU_SERVER = "CPU_SERVER";
        String CPU_MODULE = "CPU_MODULE";
        String RAM_SERVER = "RAM_SERVER";
        String RAM_MODULE = "RAM_MODULE";
        String IO = "IO";
        String DISK = "DISK";
        String WRITE_LOG = "WRITE_LOG";
        String LIVE_OR_DIE = "LIVE_OR_DIE";
        String CHECK_ERROR_LOG = "CHECK_ERORR_LOG";
        String CHECK_TRADE_LOG = "CHECK_TRADE_LOG";
    }

    interface AOM_CHECKLIST {
        int IO = 8;
        int CPU_SERVER = 3;
        int RAM_SERVER = 4;
        int DISK = 2;

        int CHECK_ERROR_LOG = 202;
        int CPU_MODULE = 204;
        int RAM_MODULE = 203;
    }

    interface CHECKLIST_ID {
        Long IO = 16L;
        Long CPU_SERVER = 12L;
        Long RAM_SERVER = 14L;
        Long DISK = 17L;

        Long CHECK_ERROR_LOG = 20L;
        Long CPU_MODULE = 13L;
        Long RAM_MODULE = 15L;
    }

    Integer EXE_CHECKLIST = 1;
    Integer EXE_TD = 2;
    Integer EXE_ROLLBACK = 3;
    Integer EXE_CHECKLIST_LAST = 4;

    Integer EXE_VERIFY_CHECK_APP = 11;
    Integer EXE_VERIFY_CHECKLIST_APP = 12;
    Integer EXE_VERIFY_CHECK_DB = 13;
    Integer EXE_VERIFY_CHECKLIST_DB = 14;

    interface OS_TYPE {
        int LINUX = 1;
        int SOLARIS = 2;
        int WINDOWS = 3;
    }

    interface DB_TYPE {
        int ORACLE = 1;
        int MYSQL = 2;
        int MSSQL = 3;
    }

    interface ACTION_TYPE {
        Long ACTION_TYPE_CR_NORMAL = 0L;
        Long ACTION_TYPE_CR_UCTT = 1L;
        Long ACTION_TYPE_KB_UCTT = 2L;
    }

    interface KB_TYPE {
        Long BD_SERVICE = 2L;
        Long BD_SERVER = 3L;

        Long UCTT_STOP = 4L;
        Long UCTT_START = 5L;
        Long UCTT_RESTART_STOP_START = 6L;
        Long UCTT_SW_DB = 7L;
        Long UCTT_SW_MODULE = 8L;

        Long BD_SERVER_SHUTDOWN = 9L;
        Long UC_SERVER = 10L;
        Long UCTT_RESTART = 11L;
    }
    interface SERVICE_TEMPLATE {
        Long STOP = 20993L;
        Long START = 20994L;
        Long RESTART = 20995L;
    }

    interface KB_GROUP {
        Long NORMAL = 0L;
        Long BD_UCTT = 1L;
        Long AUTO = 2L;
    }

    interface ACTION_SPECIAL {
        String MODULE_ACTION_TYPE_SPEC_DB = "MODULE_ACTION_TYPE_SPEC_DB";
        String MODULE_ACTION_TYPE_SPEC_WAIT = "MODULE_ACTION_TYPE_SPEC_WAIT";
    }

    interface ACTION_SPECIAL_MODE {
        String START = "START";
        String STOP = "STOP";
    }

    interface CKL_MATH_OPERATOR {
        int OPT_EQ = 1; // Bằng ( kiểu Int ) hoặc giống với ký tự ( kiểu String )
        int OPT_GT = 2; // Lớn hơn ( kiểu Int )
        int OPT_GTE = 3; // Lớn hơn hoặc bằng ( kiểu Int )
        int OPT_AND = 4; // Xuất hiện đồng thời các ký tự tần xuất của chúng ( kiểu String )
        int OPT_OR = 5; // xuất hiện một trong các giá trị (kiểu String )
        int OPT_HAVE_CURRENT_DATE = 7; // Xuất hiện giá trị của ngày hiện tại ( kiểu String )
        int OPT_FROM_TO = 8; // Từ giá trị A đến giá trị B (kiểu Int )
        int OPT_HAVE_LOG = 9; // TỒN TẠI LOG TRẢ VỀ
        int OPT_HAVE_ROW_WITH_LIST_STRING = 10; // dòng có chứa các ký tự với tần xuất
        int OPT_XUAT_HIEN_MOT_KY_TU_VOI_TAN_SUAT_LON_HON_HOAC_BANG = 11; // CHứa một ký tự với tần xuất lơn hơn hoac bang
        int OPT_NOT_EQ = -1; // Khác ( kiểu Int ) hoặc không giống với ký tự ( kiểu String )
        int OPT_LT = -2; // Nhỏ hơn ( kiểu Int )
        int OPT_LTE = -3; //Nhỏ hơn hoặc bằng ( kiểu Int )
        int OPT_NOT_OR = -5; // Không xuất hiện một trong các giá trị ( kiểu String )
        int OPT_NOT_HAVE_CURRENT_DATE = -7; // Không xuất hiện giá trị của ngày hiện tại ( kiểu String )
        int OPT_NOT_HAVE_LOG = -9; // KHÔNG TỒN TẠI LOG TRẢ VỀ
    }

    int DATA_BEGIN_WITH_COLUMN_NAME_TYPE = 1; // BẮT ĐẦU TỪ TIÊU ĐỀ
    int DATA_END_WITH_COLUMN_NAME_TYPE = 2; // BẮT ĐẦU TỪ TIÊU ĐỀ
    int DATA_CONTAIN_COLUMN_NAME_TYPE = 3; // CỘT TIÊU ĐÈ NẰM CHỌN TRONG CỌT DATA
    String SPLIST_CHAR = "\\|";

    Integer MAX_PER_KEY_POOL = 1;
    Integer MAX_SESSION_PER_USER = 2;

    interface DATA_TYPE {
        Long STRING = 1L;
        Long FLOAT = 2L;
        Long DOUBLE = 3L;
        Long INTEGER = 4L;
        Long LONG = 5L;
        Long BOOLEAN = 6L;
        Long DATE = 7L;
        Long DATE_TIME = 8L;
        Long BYTE_ARRAY = 9L;
    }

    interface SQL_TEMPLATE {
        String EMPTY = "";
        String SPACE = " ";
        String COMMA = ",";
        String QUESTION = "?";
        String ORDER = "ORDER BY";
        String WHERE = "WHERE";
        String AND = "AND";
        String CONDITION = "conditon";
        String FROM = "FROM";
    }

    int BATCH_SIZE_1000 = 1000;
    int BATCH_SIZE_2000 = 2000;
    int BATCH_SIZE_3000 = 3000;
    int BATCH_SIZE_5000 = 5000;

    interface RUN_STATUS {
        int STAND_BY_STATUS=0;
        int RUNNING_STATUS=1;
        int FINISH_SUCCESS_STATUS=2;
        int FINISH_FAIL_STATUS=-2;
        int FINISH_SUCCESS_STATUS_STOP=-3;
        int FAIL_BUT_SKIPED_BY_USER=4;
        int CANCEL_STATUS=-10;
        int NOT_ALLOW_STATUS=-11;
        int WARNING_STATUS=-12;
        int FINISH_SUCCESS_WITH_WARNING=3;
    }

    interface UPDATE_STATUS {
        int updateActionDetailBackupStatus = 1;
        int updateActionDetailRunStatus = 2;
        int updateActionDetailRollbackStatus = 3;
        int updateActionDbRunStatus = 4;
        int updateActionDbRollbackStatus = 5;
        int updateActionDbBackupStatus = 6;
        int updateActionCustomRunStatus = 7;
        int updateActionCustomRollbackTest = 8;
    }

    interface ON_OFF_FLAG {
        int STOP = 1;
        int START = 2;
        int STOP_START = 3;
    }

    interface EXE_TYPE {
        Integer EXE_CHECKLIST = 1;
        Integer EXE_TD = 2;
        Integer EXE_ROLLBACK = 3;
        Integer EXE_CHECKLIST_LAST = 4;
    }

    interface RUN_STEP {
        String STEP_BACKUP = "0";
        String STEP_STOP = "1";
        String STEP_UPCODE = "2";
        String STEP_CLEARCACHE = "3";
        String STEP_RESTART = "4";
        String STEP_START = "5";
        String STEP_RESTART_CMD = "6";
        String STEP_CHECK_STATUS = "14";
        String STEP_UPCODE_STOP_START = "15";

        String STEP_ROLLBACK_CODE_STOP_START = "16";
        String STEP_ROLLBACKCODE = "7";
        String STEP_ROLLBACK_STOP = "8";
        String STEP_ROLLBACK_CLEARCACHE = "9";
        String STEP_ROLLBACK_RESTART = "10";
        String STEP_ROLLBACK_START = "11";
        String STEP_ROLLBACK_RESTART_CMD = "12";
        String STEP_CHECKVERSION_APP = "13";

        /*20181030_hoangnd_save all step_start*/
        String STEP_CHECKLIST_APP = "17";
        String STEP_CHECKLIST_DB = "18";
        String STEP_ROLLBACK_DB = "19";
        String STEP_ROLLBACK_CHECK_STATUS = "20";
        /*20181030_hoangnd_save all step_end*/
    }

    interface ACTION {
        int SPECIAL_UPCODETEST_RESTART_STOP_START = 100;
        int SPECIAL_UPCODETEST_RESTART = 101;
        int SPECIAL_UPCODETEST_START = 102;
        int SPECIAL_UPCODETEST_STOP_START = 103;
        int SPECIAL_RESTART_STOP_START = 110;
        int SPECIAL_RESTART = 111;
        int SPECIAL_START = 112;
        int SPECIAL_STOP_START = 113;

        int SPECIAL_ROLLBACKTEST_RESTART_STOP_START = 200;
        int SPECIAL_ROLLBACKTEST_RESTART = 201;
        int SPECIAL_ROLLBACKTEST_STOP = 202;
        int SPECIAL_ROLLBACKTEST_STOP_START = 203;
        int SPECIAL_ROLLBACK_RESTART_STOP_START = 210;
        int SPECIAL_ROLLBACK_RESTART = 211;
        int SPECIAL_ROLLBACK_STOP = 212;
        int SPECIAL_ROLLBACK_STOP_START = 213;
        //////////

        int SUB_STEP_CHECK_STATUS = 101;
        int SUB_STEP_STOP_APP = 102;
        int SUB_STEP_BACKUP_APP = 103;
        int SUB_STEP_BACKUP_DB = 104;
        int SUB_STEP_UPCODE = 105;
        int SUB_STEP_TD_DB = 106;
        int SUB_STEP_CLEARCACHE = 107;
        int SUB_STEP_RESTART_APP = 108;
        int SUB_STEP_START_APP = 109;
        int SUB_STEP_CHECKLIST_APP = 110;
        int SUB_STEP_CHECKLIST_DB = 111;
        int SUB_STEP_UPCODE_START_APP = 112;

        int CUSTOM_STEP_CHECK_STATUS = 121;
        int CUSTOM_STEP_STOP_APP = 122;
        int CUSTOM_STEP_BACKUP_APP = 123;
        int CUSTOM_STEP_BACKUP_DB = 124;
        int CUSTOM_STEP_UPCODE = 125;
        int CUSTOM_STEP_TD_DB = 126;
        int CUSTOM_STEP_CLEARCACHE = 127;
        int CUSTOM_STEP_RESTART_APP = 128;
        int CUSTOM_STEP_START_APP = 129;

        int AFTER_STEP_CHECKLIST_APP = 201;
        int AFTER_STEP_CHECKLIST_DB = 202;
        int AFTER_STEP_CHECK_VERSION = 203;
        /*20181115_hoangnd_save all step_start*/
        int BEFORE_STEP_CHECKLIST_APP = 1;
        int BEFORE_STEP_CHECKLIST_DB = 2;
        /*20181115_hoangnd_save all step_end*/

        int ROLLBACK_STEP_STOP_APP = 301;
        int ROLLBACK_STEP_SOURCE_CODE = 302;
        int ROLLBACK_STEP_DB = 303;
        int ROLLBACK_STEP_CLEARCACHE = 304;
        int ROLLBACK_STEP_RESTART_APP = 305;
        int ROLLBACK_STEP_START_APP = 306;
        int ROLLBACK_STEP_UPCODE_START = 309;
        int ROLLBACK_STEP_CHECK_STATUS = 310;
        int ROLLBACK_STEP_CHECKLIST_APP = 307;
        int ROLLBACK_STEP_CHECKLIST_DB = 308;

        int ROLLBACK_CUSTOM_STEP_CHECK_STATUS = 321;
        int ROLLBACK_CUSTOM_STEP_STOP_APP = 322;
        int ROLLBACK_CUSTOM_STEP_BACKUP_APP = 323;
        int ROLLBACK_CUSTOM_STEP_BACKUP_DB = 324;
        int ROLLBACK_CUSTOM_STEP_UPCODE = 325;
        int ROLLBACK_CUSTOM_STEP_TD_DB = 326;
        int ROLLBACK_CUSTOM_STEP_CLEARCACHE = 327;
        int ROLLBACK_CUSTOM_STEP_RESTART_APP = 328;
        int ROLLBACK_CUSTOM_STEP_START_APP = 329;
    }



    /*20181022_hoangnd_continue fail step_start*/
    public static enum Step {

        SPECIAL_UPCODETEST_RESTART_STOP_START("SPECIAL_UPCODETEST_RESTART_STOP_START", ACTION.SPECIAL_UPCODETEST_RESTART_STOP_START),
        SPECIAL_UPCODETEST_RESTART("SPECIAL_UPCODETEST_RESTART", ACTION.SPECIAL_UPCODETEST_RESTART),
        SPECIAL_UPCODETEST_START("SPECIAL_UPCODETEST_START", ACTION.SPECIAL_UPCODETEST_START),
        SPECIAL_UPCODETEST_STOP_START("SPECIAL_UPCODETEST_STOP_START", ACTION.SPECIAL_UPCODETEST_STOP_START),
        SPECIAL_RESTART_STOP_START("SPECIAL_RESTART_STOP_START", ACTION.SPECIAL_RESTART_STOP_START),
        SPECIAL_RESTART("SPECIAL_RESTART", ACTION.SPECIAL_RESTART),
        SPECIAL_START("SPECIAL_START", ACTION.SPECIAL_START),
        SPECIAL_STOP_START("SPECIAL_STOP_START", ACTION.SPECIAL_STOP_START),

        SPECIAL_ROLLBACKTEST_RESTART_STOP_START("SPECIAL_ROLLBACKTEST_RESTART_STOP_START", ACTION.SPECIAL_ROLLBACKTEST_RESTART_STOP_START),
        SPECIAL_ROLLBACKTEST_RESTART("SPECIAL_ROLLBACKTEST_RESTART", ACTION.SPECIAL_ROLLBACKTEST_RESTART),
        SPECIAL_ROLLBACKTEST_STOP("SPECIAL_ROLLBACKTEST_STOP", ACTION.SPECIAL_ROLLBACKTEST_STOP),
        SPECIAL_ROLLBACKTEST_STOP_START("SPECIAL_ROLLBACKTEST_STOP_START", ACTION.SPECIAL_ROLLBACKTEST_STOP_START),
        SPECIAL_ROLLBACK_RESTART_STOP_START("SPECIAL_ROLLBACK_RESTART_STOP_START", ACTION.SPECIAL_ROLLBACK_RESTART_STOP_START),
        SPECIAL_ROLLBACK_RESTART("SPECIAL_ROLLBACK_RESTART", ACTION.SPECIAL_ROLLBACK_RESTART),
        SPECIAL_ROLLBACK_STOP("SPECIAL_ROLLBACK_STOP", ACTION.SPECIAL_ROLLBACK_STOP),
        SPECIAL_ROLLBACK_STOP_START("SPECIAL_ROLLBACK_STOP_START", ACTION.SPECIAL_ROLLBACK_STOP_START),

        SUB_STEP_CHECK_STATUS("SUB_STEP_CHECK_STATUS", ACTION.SUB_STEP_CHECK_STATUS),
        SUB_STEP_STOP_APP("SUB_STEP_STOP_APP", ACTION.SUB_STEP_STOP_APP),
        SUB_STEP_BACKUP_APP("SUB_STEP_BACKUP_APP", ACTION.SUB_STEP_BACKUP_APP),
        SUB_STEP_BACKUP_DB("SUB_STEP_BACKUP_DB", ACTION.SUB_STEP_BACKUP_DB),
        SUB_STEP_UPCODE("SUB_STEP_UPCODE", ACTION.SUB_STEP_UPCODE),
        SUB_STEP_TD_DB("SUB_STEP_TD_DB", ACTION.SUB_STEP_TD_DB),
        SUB_STEP_CLEARCACHE("SUB_STEP_CLEARCACHE", ACTION.SUB_STEP_CLEARCACHE),
        SUB_STEP_RESTART_APP("SUB_STEP_RESTART_APP", ACTION.SUB_STEP_RESTART_APP),
        SUB_STEP_START_APP("SUB_STEP_START_APP", ACTION.SUB_STEP_START_APP),
        SUB_STEP_CHECKLIST_APP("SUB_STEP_CHECKLIST_APP", ACTION.SUB_STEP_CHECKLIST_APP),
        SUB_STEP_CHECKLIST_DB("SUB_STEP_CHECKLIST_DB", ACTION.SUB_STEP_CHECKLIST_DB),
        SUB_STEP_UPCODE_START_APP("SUB_STEP_UPCODE_START_APP", ACTION.SUB_STEP_UPCODE_START_APP),

        CUSTOM_STEP_CHECK_STATUS("CUSTOM_STEP_CHECK_STATUS", ACTION.CUSTOM_STEP_CHECK_STATUS),
        CUSTOM_STEP_STOP_APP("CUSTOM_STEP_STOP_APP", ACTION.CUSTOM_STEP_STOP_APP),
        CUSTOM_STEP_BACKUP_APP("CUSTOM_STEP_BACKUP_APP", ACTION.CUSTOM_STEP_BACKUP_APP),
        CUSTOM_STEP_BACKUP_DB("CUSTOM_STEP_BACKUP_DB", ACTION.CUSTOM_STEP_BACKUP_DB),
        CUSTOM_STEP_UPCODE("CUSTOM_STEP_UPCODE", ACTION.CUSTOM_STEP_UPCODE),
        CUSTOM_STEP_TD_DB("CUSTOM_STEP_TD_DB", ACTION.CUSTOM_STEP_TD_DB),
        CUSTOM_STEP_CLEARCACHE("CUSTOM_STEP_CLEARCACHE", ACTION.CUSTOM_STEP_CLEARCACHE),
        CUSTOM_STEP_RESTART_APP("CUSTOM_STEP_RESTART_APP", ACTION.CUSTOM_STEP_RESTART_APP),
        CUSTOM_STEP_START_APP("CUSTOM_STEP_START_APP", ACTION.CUSTOM_STEP_START_APP),

        AFTER_STEP_CHECKLIST_APP("AFTER_STEP_CHECKLIST_APP", ACTION.AFTER_STEP_CHECKLIST_APP),
        AFTER_STEP_CHECKLIST_DB("AFTER_STEP_CHECKLIST_DB", ACTION.AFTER_STEP_CHECKLIST_DB),
        AFTER_STEP_CHECK_VERSION("AFTER_STEP_CHECK_VERSION", ACTION.AFTER_STEP_CHECK_VERSION),
        /*20181115_hoangnd_save all step_start*/
        BEFORE_STEP_CHECKLIST_APP("BEFORE_STEP_CHECKLIST_APP", ACTION.BEFORE_STEP_CHECKLIST_APP),
        BEFORE_STEP_CHECKLIST_DB("BEFORE_STEP_CHECKLIST_DB", ACTION.BEFORE_STEP_CHECKLIST_DB),
        /*20181115_hoangnd_save all step_end*/

        ROLLBACK_STEP_STOP_APP("ROLLBACK_STEP_STOP_APP", ACTION.ROLLBACK_STEP_STOP_APP),
        ROLLBACK_STEP_SOURCE_CODE("ROLLBACK_STEP_SOURCE_CODE", ACTION.ROLLBACK_STEP_SOURCE_CODE),
        ROLLBACK_STEP_DB("ROLLBACK_STEP_DB", ACTION.ROLLBACK_STEP_DB),
        ROLLBACK_STEP_CLEARCACHE("ROLLBACK_STEP_CLEARCACHE", ACTION.ROLLBACK_STEP_CLEARCACHE),
        ROLLBACK_STEP_RESTART_APP("ROLLBACK_STEP_RESTART_APP", ACTION.ROLLBACK_STEP_RESTART_APP),
        ROLLBACK_STEP_START_APP("ROLLBACK_STEP_START_APP", ACTION.ROLLBACK_STEP_START_APP),
        ROLLBACK_STEP_UPCODE_START("ROLLBACK_STEP_UPCODE_START", ACTION.ROLLBACK_STEP_UPCODE_START),
        ROLLBACK_STEP_CHECK_STATUS("ROLLBACK_STEP_CHECK_STATUS", ACTION.ROLLBACK_STEP_CHECK_STATUS),
        ROLLBACK_STEP_CHECKLIST_APP("ROLLBACK_STEP_CHECKLIST_APP", ACTION.ROLLBACK_STEP_CHECKLIST_APP),
        ROLLBACK_STEP_CHECKLIST_DB("ROLLBACK_STEP_CHECKLIST_DB", ACTION.ROLLBACK_STEP_CHECKLIST_DB),

        ROLLBACK_CUSTOM_STEP_CHECK_STATUS("ROLLBACK_CUSTOM_STEP_CHECK_STATUS", ACTION.ROLLBACK_CUSTOM_STEP_CHECK_STATUS),
        ROLLBACK_CUSTOM_STEP_STOP_APP("ROLLBACK_CUSTOM_STEP_STOP_APP", ACTION.ROLLBACK_CUSTOM_STEP_STOP_APP),
        ROLLBACK_CUSTOM_STEP_BACKUP_APP("ROLLBACK_CUSTOM_STEP_BACKUP_APP", ACTION.ROLLBACK_CUSTOM_STEP_BACKUP_APP),
        ROLLBACK_CUSTOM_STEP_BACKUP_DB("ROLLBACK_CUSTOM_STEP_BACKUP_DB", ACTION.ROLLBACK_CUSTOM_STEP_BACKUP_DB),
        ROLLBACK_CUSTOM_STEP_UPCODE("ROLLBACK_CUSTOM_STEP_UPCODE", ACTION.ROLLBACK_CUSTOM_STEP_UPCODE),
        ROLLBACK_CUSTOM_STEP_TD_DB("ROLLBACK_CUSTOM_STEP_TD_DB", ACTION.ROLLBACK_CUSTOM_STEP_TD_DB),
        ROLLBACK_CUSTOM_STEP_CLEARCACHE("ROLLBACK_CUSTOM_STEP_CLEARCACHE", ACTION.ROLLBACK_CUSTOM_STEP_CLEARCACHE),
        ROLLBACK_CUSTOM_STEP_RESTART_APP("ROLLBACK_CUSTOM_STEP_RESTART_APP", ACTION.ROLLBACK_CUSTOM_STEP_RESTART_APP),
        ROLLBACK_CUSTOM_STEP_START_APP("ROLLBACK_CUSTOM_STEP_START_APP", ACTION.ROLLBACK_CUSTOM_STEP_START_APP);

        private final String key;
        private final Integer value;

        Step(String key, Integer value) {
            this.key = key;
            this.value = value;
        }

        public String getKey() {
            return key;
        }
        public Integer getValue() {
            return value;
        }
    }
    /*20181022_hoangnd_continue fail step_end*/

    String NA_VALUE = "N/A";
    String CODETAPTRUNG_TYPE = "CODE_TAP_TRUNG";

    String KEY_LOG_START = "start_success";
    String KEY_STATUS_START = "output_status_start";

    interface FOLDER {
        int DATABASE = 0;
        int CODE = 1;
        int DATA_IMPORT = 2;
    }

    Integer CR_STATE_UCTT_CREATE = 201;
    Integer CR_STATE_UCTT_APPROVE = 202;

    String TAIL_LOG_SCRIPT = "@ECHO OFF\n" +
            "SETLOCAL ENABLEDELAYEDEXPANSION\n" +
            "SET directory=%~1\n" +
            "IF NOT DEFINED directory SET \"directory=%%^cd%%\"\n" +
            "set /A total=^0\n" +
            "set /A read=^1000\n" +
            "for /f %%I IN ('cmd.exe /c find /c /v  \"\" ^^^< \"%directory%\"') DO SET total=%%I\n" +
            "if %total% GEQ %read% ( @set/A read=\"%total%-%read%\"\n" +
            "more +!read! \"%directory%\")\n" +
            "if %total% LSS %read% ( more \"%directory%\")\n" +
            "ENDLOCAL";

    interface MODULE_TYPE {
        String WINDOWS_SERVICE = "WINDOWS_SERVICES";
    }


    public static final String SERVICE = "SERVICE";
    public static final String INFRA = "INFRA";
    public static final String BUSSINESS = "BUSSINESS";

	interface  NATION_CODE {
        String VIETNAM = "VNM";
    }

    public static final int OPERATION_1 = 1;
    public static final int OPERATION_2 = 2;
    public static final int OPERATION_3 = 3;
    public static final int OPERATION_4 = 4;
    public static final int OPERATION_5 = 5;
    public static final int OPERATION_6 = 6;
    public static final int OPERATION_7 = 7;
    public static final int OPERATION_8 = 8;
    public static final int OPERATION_9 = 9;
    public static final int OPERATION_10 = 10;
    public static final int OPERATION_11= 11;
    public static final int OPERATION_LT_1 = -1;
    public static final int OPERATION_LT_2 = -2;
    public static final int OPERATION_LT_3 = -3;
    public static final int OPERATION_LT_4 = -4;
    public static final int OPERATION_LT_5 = -5;
    public static final int OPERATION_LT_6 = -6;
    public static final int OPERATION_LT_7 = -7;
    public static final int OPERATION_LT_8 = -8;
    public static final int OPERATION_LT_9 = -9;

    public static final int defaultOptionWhenTimeOut = 15;
    public static final int waitActionContinue = 1;
    public static final int waitActionCancel = 2;
    public static final int userConfirmShutdown = 8;
    public static final int waitActionScreenTimeOut = 9;
    public static final int waitActionScreenTimeIn = 10;
    public static final int waitActionSDDoNotTurnBack = 3;
    public static final int waitActionRetryOk = 2;
    public static final int confirm = 0;
    public static final int crFail = 1;

    //thenv_20180614_USER COUNTRY_start
    public final static String COUNTRY_CODE_CURRENT = "countryCodeCurrent";
    public final static String VNM = "VNM";
    public final static String VTP = "VTP";
    public final static String VTZ = "VTZ";
//    thenv_20180614_USER COUNTRY_end
    //20181023_tudn_start load pass security
    public static final String ACCOUNT_TYPE_SERVER = "1";
    public static final String ACCOUNT_TYPE_DATABASE = "2";

    public static final String SECURITY_NODE = "1";
    public static final String SECURITY_SERVER = "2";
    public static final String SECURITY_DATABASE = "3";
    public static final String CFG_SECURITY = "SECURITY";
    public static final String CFG_ACTIVE_SECURITY = "ACTIVE_SECURITY";
    public static final String CFG_URL_SECURITY = "URL_SECURITY";
    public static final String CFG_URL_SECURITY_UPDATE = "URL_SECURITY_UPDATE";
    public static final String CFG_USER_SECURITY = "USER_SECURITY";
    public static final String CFG_PASS_SECURITY = "PASS_SECURITY";
    public static final String CFG_TIME_OUT_SECURITY = "TIME_OUT_SECURITY";
    public static final String CFG_COUNTRY_CODE_SECURITY = "COUNTRY_CODE_SECURITY";
    //20181023_tudn_end load pass security
    //20181119_tudn_start them danh sach lenh blacklist
    public static final String CFG_CR_TYPE = "CR_TYPE"; // Quy trinh nguy hiem
    public static final String CFG_PROCEDURE_GNOC = "PROCEDURE_GNOC"; // Quy trinh nguy hiem
    public static final String CFG_DANGEROUS_PROCEDURE = "DANGEROUS_PROCEDURE"; // Quy trinh nguy hiem
    public static final Long MOP_TYPE_DANGEROUS = 4L; // MOP nguy hiem
    public static final String CR_TYPE_NORMAL = "2"; // Loai CR thuong
    //20181119_tudn_end them danh sach lenh blacklist
	//    Quytv7_20180907_Timeout_config start
    public final static Long TIMEOUT_LOGIN = 30L;
    public final static Long TIMEOUT_IMPACT = 3600L;
	//    Quytv7_20180907_Timeout_config end

    // 22-11-2018 KienPD constant waiting service dead start
    public static final int WAIT_CONFIRM_SERVICE = 20;
    public static final int OPTION_RECHECK = 22;
    public static final int OPTION_ACCEPT = 23;
    // 22-11-2018 KienPD constant waiting service dead end

    //20190408_chuongtq start check param when create MOP
    public static final String CFG_CHK_PARAM_CONDITION_GROUP = "CHECK_PARAM_CONDITION";
    public static final String CFG_CHK_PARAM_CONDITION_FOR_SON = "CHECK_FOR_SON";
    public static final String CFG_CHK_PARAM_CONDITION_FOR_NOCPRO = "CHECK_FOR_NOCPRO";
    public static final String CFG_CHK_PARAM_CONDITION_FOR_AAM = "CHECK_FOR_AAM";
    public static final String CFG_CHK_PARAM_CONDITION_FOR_SECURITY = "CHECK_FOR_SECURITY";
    public static final String CFG_CHK_PARAM_CONDITION_FOR_WEB = "CHECK_FOR_WEB";
    //20190408_chuongtq end check param when create MOP

    //20190826_tudn_start lap lich tac dong tu dong GNOC
    public static final String CFG_GROUP_RUN_AUTO_MOP = "RUN_AUTO_MOP";
    public static final String CFG_KEY_DELTA_TIME = "DELTA_TIME";
    //20190826_tudn_end lap lich tac dong tu dong GNOC
}
