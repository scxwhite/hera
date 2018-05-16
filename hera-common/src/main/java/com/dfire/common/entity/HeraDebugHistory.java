package com.dfire.common.entity;

import com.dfire.common.constant.Status;
import com.dfire.common.vo.LogContent;
import lombok.Builder;
import lombok.Data;

import java.util.Date;

/**
 * @author: <a href="mailto:lingxiao@2dfire.com">凌霄</a>
 * @time: Created in 14:29 2018/3/22
 * @desc
 */
@Builder
@Data
public class HeraDebugHistory {

    private String id;

    private String fileId;

    private Date startTime;

    private Date endTime;

    private String executeHost;

    private Status status;

    private String owner;

    private Date gmtCreate ;

    private Date gmtModified;

    private String script;

    private String runType;

    private String log;

    private String host;

    private String hostGroupId;

}
