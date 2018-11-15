package com.dfire.common.vo;

import lombok.Data;

/**
 *
 * @author xiaosuda
 * @date 2018/11/14
 */
@Data
public class ProcessMonitorVo {

    /**
     * 进程id
     */
    private String pid;
    /**
     * 进程所有者的用户名
     */
    private String user;
    /**
     * 进程使用的虚拟内存总量
     */
    private String viri;
    /**
     * 进程使用的、未被换出的大小
     */
    private String res;
    /**
     * 上次更新到现在cpu时间占用百分比
     */
    private String cpu;
    /**
     *物理内存百分比
     */
    private String mem;
    /**
     * 进程使用的cpu时间总计
     */
    private String time;
    /**
     * 命令名/命令行
     */
    private String command;
}
