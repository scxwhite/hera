package com.dfire.common.constants;

/**
 * @author xiaosuda
 * @date 2018/7/2
 */
public class Constants {

    public final static String HERA_GROUP = "heraGroup";

    public final static String GROUP_PREFIX = "group_";

    public final static String PRE_ENV = "pre";

    public final static String PUB_ENV = "publish";

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

    public final static String ALL_JOB_ID = "-1024";

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

    public final static String HTML_NEW_LINE = "<br>";


    public final static String LOG_SPLIT = "<br><br>";

    public final static String FILE_ALL_NAME = "all";

    public final static String FILE_SELF = "个人文档";

    public final static String FILE_ALL = "共享文档";

    public final static String OPEN_STATUS = "开启";

    public final static String CLOSE_STATUS = "关闭";

    public final static String INVALID_STATUS = "失效";


}
