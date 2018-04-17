package com.dfire.core.netty.util;

/**
 * @author xiaosuda
 * @date 2018/4/16
 */
public class PropertyKeys extends RunningJobKeys {

    public static final String JOB_ID_KEY = "job.id";
    public static final String JOB_CLASSPATH_KEY = "job.classpath";
    public static final String JOB_CREATE_DATE_KEY = "job.create_date";
    public static final String JOB_MODIFIED_DATE_KEY = "job.modified_date";
    public static final String JOB_CRON_EXPRESSION_KEY = "job.cron_expression";
    public static final String JOB_DEPENDENCIES_KEY = "job.dependencies";
    public static final String JOB_NAME_KEY = "job.name";
    public static final String JOB_DESC_KEY = "job.desc";
    public static final String JOB_SCRIPT = "job.script";
    /**
     * 运行类型，比如： shell  hive mapreduce java
     */
    public static final String JOB_RUN_TYPE_KEY = "job.run_type";
    /**
     * 调度类型，有2种
     * 1.独立Job 不依赖其他JOB
     * 2.非独立Job 需要依赖其他Job
     */
    public static final String JOB_SCHEDULE_TYPE_KEY = "job.schedule_type";

    public static final String JOB_OWNER_KEY = "job.owner";

    //GROUP KEY定义内容

    public static final String GROUP_ID_KEY = "group.id";

    public static final String GROUP_OWNER_KEY = "group.owner";
    public static final String GROUP_NAME_KEY = "group.name";
    public static final String GROUP_DESC_KEY = "group.desc";
    public static final String GROUP_CREATE_DATE_KEY = "group.create_date";
    public static final String GROUP_MODIFIED_DATE_KEY = "group.modified_date";
    /**
     * group是否为目录
     * 如果为目录，下级可以继续创建group，但是不能创建job
     * 如果不为目录，下级只能创建job，不能创建group
     */
    public static final String GROUP_DIRECTORY_KEY = "group.directory";
    /**
     * 依赖关系的周期设定
     */
    public static final String DEPENDENCY_CYCLE = "zeus.dependency.cycle";

}
