package com.dfire.common.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 17:36 2018/1/11
 * @desc 原先zues中空闲的表，新系统中，作为任务变更记录表，记录任务变更日志
 */
@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class HeraLog {

    private Integer id;
    private String logType;
    private Date createTime;
    private String userName;
    private String ip;
    private String url;
    private String rpc;
    private String delegate;
    private String method;
    private String description;



}
