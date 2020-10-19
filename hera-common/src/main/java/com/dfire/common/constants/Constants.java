package com.dfire.common.constants;

/**
 * @author xiaosuda
 * @date 2018/7/2
 */
public class Constants {

    public final static String HERA_GROUP = "heraGroup";

    public final static String GROUP_PREFIX = "group_";

    public final static String PRE_ENV = "pre";

    public final static String PUB_ENV = "public";

    public final static String DAILY_ENV = "daily";

    public final static String WORK_PREFIX = "worker-";

    public final static String MASTER_PREFIX = "master-";

    public final static String STATUS_NONE = "none";

    public final static Integer HTML_FONT_SIZE = 1;

    public final static Integer AVAILABLE_CORES = Runtime.getRuntime().availableProcessors();

    public final static String HTML_FONT_RED_LEFT = "<font color=\"red\" size=\"" + HTML_FONT_SIZE + "\" >";

    public final static String HTML_FONT_GREEN_LEFT = "<font color=\"green\" size=\"" + HTML_FONT_SIZE + "\">";

    public final static String HTML_FONT_BLUE_LEFT = "<font color=\"blue\" size=\"" + HTML_FONT_SIZE + "\">";

    public final static String HTML_FONT_LEFT = "<font size=\"" + HTML_FONT_SIZE + "\">";

    public final static String HTML_FONT_RIGHT = "</font>";


    public final static String LEFT_BRACKET = "(";

    public final static String RIGHT_BRACKET = ")";

    public final static Long ALL_JOB_ID = -1024L;

    public final static String CANCEL_JOB_MESSAGE = "任务手动取消";

    /**
     * jwt  过期时间 单位 天
     */
    public final static Integer JWT_TIME_OUT = 5;
    /**
     * cookie过期时间 单位 秒
     */
    public final static Integer LOGIN_TIME_OUT = JWT_TIME_OUT * 60 * 60 * 24;


    /**
     * 退出码相关
     */

    public final static int SUCCESS_EXIT_CODE = 0;

    public final static int DEFAULT_EXIT_CODE = -1;

    public final static int WAIT_EXIT_CODE = 38;

    public final static int LOG_EXIT_CODE = WAIT_EXIT_CODE + 1;

    public final static int INTERRUPTED_EXIT_CODE = WAIT_EXIT_CODE + 2;


    public final static String SPARK_FILE = "spark";

    public final static String HIVE_FILE = "hive";

    public final static String SHELL_FILE = "shell";


    public final static String POINT = ".";

    public final static String COMMA = ",";

    public final static String SEMICOLON = ";";


    public final static String SHELL_SUFFIX = POINT + "sh";

    public final static String HIVE_SUFFIX = POINT + HIVE_FILE;

    public final static String SPARK_SUFFIX = POINT + SPARK_FILE;

    public final static String NEW_LINE = "\n";

    public final static String BLANK_SPACE = " ";

    public final static String HTML_NEW_LINE = "<br>";


    public final static String LOG_SPLIT = "<br><br>";

    public final static String FILE_ALL_NAME = "all";

    public final static String FILE_SELF = "个人文档";

    public final static String FILE_ALL = "共享文档";

    public final static String OPEN_STATUS = "开启";

    public final static String CLOSE_STATUS = "关闭";

    public final static String INVALID_STATUS = "失效";


    public final static String QUARTZ_ID = "actionId";

    public final static String QUARTZ_DISPATCHER = "dispatcher";

    /**
     * 所有区域任务
     */
    public final static String ALL_AREA = "all";

    public final static String SSH_PREFIX = "<< eeooff";

    public final static String SSH_SUFFIX = "eeooff";

    public final static String HERA_EMR_FIXED = "hera.emr.fixed";

    public final static String HERA_EMR_FIXED_HOST = "hera.emr.fixed.host";

    public final static String AREA_INDIA = "IND";

    public final static String AREA_EUROPE = "EU";

    public final static String AREA_US = "US";

    public final static String AREA_UE = "UE";

    public final static String AREA_CHINA = "AY";

    public final static String HERA_SPARK_CONF = "hera.spark.conf";

    public final static String EMR_SELECT_WORK = "emr.select.work";

    public final static String TMP_PATH = "/tmp";


    public final static String SESSION_USERNAME = "username";

    public final static String SESSION_USER_ID = "userId";

    public final static String SESSION_SSO_ID = "ssoId";

    public final static String SESSION_SSO_NAME = "sso_name";

    public final static String TOKEN_NAME = "HERA_Token";

    public final static String DEFAULT_ID = "-1";

    public final static String PASSWORD_WORD = "password";

    public final static String HERA_SCRIPT_ECHO = "hera.script.echo";

    public final static String KAFKA_TOPIC = "hera_notice";

    public final static String ACTION_DONE = "action_done";

    public final static String ACTION_PROCESS_NUM = "action_process_num";

    public final static String ACTION_FAILED_NUM = "action_failed_num";

    public final static String ACTION_ALL_NUM = "action_all_num";

    public final static String RERUN_THREAD = "rerun_thread";

    public final static String RERUN_FAILED = "rerun_failed";

    public final static String RERUN_ID = "rerun_id";

    public final static String LAST_RERUN_ID = "last_rerun_id";

    public final static String APP_ID = "applicationId";
    public final static String HADOOP_ID = "hadoopJobId";
    public final static String FILE_CONTENT = "fileContent";

    public final static String EMR_ADDRESS = "emr_address";

    public final static String COLON = ":";

    public final static String SECRET_PREFIX = "secret.";

    public final static String RERUN_COUNT = "single";
    public final static String RERUN_INDEX = "index";

    public final static String RERUN_START_TIME = "startTime_";

    public final static String RERUN_END_TIME = "endTime_";

    public final static String RUN_FIXED_HOST = "run_fixed_host";

    public final static String FILE_COUNT_ECHO = "输出表最后一个分区统计信息:";

    public final static String ANYHOST_VALUE = "0.0.0.0";

    public final static String SERVER_KEY = "server.ip";

    public final static String LOCALHOST_VALUE = "127.0.0.1";


}
