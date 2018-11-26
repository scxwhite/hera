package com.dfire.common.constants;

/**
 * @author xiaosuda
 * @date 2018/7/2
 */
public class Constants {

    public final static String HERA_GROUP = "heraGroup";

    public final static String GROUP_PREFIX = "group_";

    public final static String PRE_ENV = "pre";

    public final static String WORK_PREFIX = "worker-";

    public final static String MASTER_PREFIX = "master-";


    public final static String STATUS_FAILED = "failed";

    public final static String STATUS_RUNNING = "running";

    public final static String STATUS_SUCCESS = "success";

    public final static String STATUS_WAIT = "wait";

    public final static String LEFT_BRACKET = "(";

    public final static String RIGHT_BRACKET = ")";

    public final static String ALL_JOB_ID = "-1024";

    public final static String CANCEL_JOB_MESSAGE = "任务手动取消";

    /**
     * jwt  过期时间 单位 天
     */
    public final static Integer JWT_TIME_OUT = 3;
    /**
     * cookie过期时间 单位 秒
     */
    public final static Integer LOGIN_TIME_OUT = JWT_TIME_OUT * 60 * 60 * 24;


}
