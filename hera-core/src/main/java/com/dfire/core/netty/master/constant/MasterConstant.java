package com.dfire.core.netty.master.constant;


/**
 * master 相关静态配置参数
 * @author <a href="mailto:huoguo@2dfire.com">火锅</a>
 * @time 2018/11/28
 */
public class MasterConstant {

    /** 默认8点为早上（其他时区要动态处理） */
    public static final int MORNING_TIME = 8;

    /** 向前15分钟检查,尽量选择确定漏跑的任务，15分钟作为一个界限 */
    public static final long PRE_CHECK_MIN =  15000000;
}
